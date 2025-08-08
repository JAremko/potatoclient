(ns buff-validate.test-harness
  "Test harness for buff-validate that generates test binary messages and ensures proto files are compiled."
  (:require
   [clojure.java.io :as io]
   [clojure.java.shell :as shell])
  (:import
   [java.io ByteArrayOutputStream File]
   [com.google.protobuf CodedOutputStream]))

(defn proto-classes-compiled?
  "Check if protobuf classes are compiled and available."
  []
  (let [ser-class-dir (io/file "target/classes/ser")
        cmd-class-dir (io/file "target/classes/cmd")]
    (and (.exists ser-class-dir)
         (.isDirectory ser-class-dir)
         (> (count (.listFiles ser-class-dir)) 0)
         (.exists cmd-class-dir)
         (.isDirectory cmd-class-dir)
         (> (count (.listFiles cmd-class-dir)) 0))))

(defn pronto-classes-available?
  "Check if Pronto Java classes are compiled and available."
  []
  (try
    ;; Try to load a core Pronto class
    (Class/forName "pronto.ProtoMap")
    true
    (catch ClassNotFoundException _
      false)))

(defn compile-pronto-if-needed!
  "Compile Pronto Java sources if not already compiled."
  []
  (when-not (pronto-classes-available?)
    (println "Compiling Pronto Java sources...")
    (let [result (shell/sh "clojure" "-T:build" "compile-pronto")]
      (if (zero? (:exit result))
        (println "Pronto compilation successful")
        (do
          (println "Pronto compilation failed:")
          (println (:err result))
          (throw (ex-info "Failed to compile Pronto" {:exit (:exit result)})))))))

(defn generate-proto-if-needed!
  "Generate proto Java sources if not already present."
  []
  (let [java-src-dir (io/file "src/java")]
    (when-not (and (.exists java-src-dir)
                   (.isDirectory java-src-dir)
                   (> (count (.listFiles java-src-dir)) 0))
      (println "Generating proto Java sources...")
      (let [result (shell/sh "clojure" "-T:build" "generate-proto")]
        (if (zero? (:exit result))
          (println "Proto generation successful")
          (do
            (println "Proto generation failed:")
            (println (:err result))
            (throw (ex-info "Failed to generate proto" {:exit (:exit result)}))))))))

(defn compile-proto-if-needed!
  "Compile proto Java sources if not already compiled."
  []
  (when-not (proto-classes-compiled?)
    (println "Compiling proto Java sources...")
    (let [result (shell/sh "clojure" "-T:build" "compile-proto")]
      (if (zero? (:exit result))
        (println "Proto compilation successful")
        (do
          (println "Proto compilation failed:")
          (println (:err result))
          (throw (ex-info "Failed to compile proto" {:exit (:exit result)})))))))

(defn initialize!
  "Initialize the test harness.
   Ensures all prerequisites are met before running tests."
  []
  ;; First ensure Pronto is compiled
  (compile-pronto-if-needed!)
  
  ;; Check and generate proto sources if needed
  (generate-proto-if-needed!)
  
  ;; Compile proto classes if needed
  (compile-proto-if-needed!)
  
  ;; Verify everything is ready
  (when-not (pronto-classes-available?)
    (throw (ex-info "Pronto classes not available after compilation" {})))
  
  (when-not (proto-classes-compiled?)
    (throw (ex-info "Proto classes not compiled after compilation" {})))
  
  ;; Success message
  (println "Buff-Validate test harness initialized:")
  (println "  ✓ Pronto classes available")
  (println "  ✓ Protobuf classes compiled")
  (println "  ✓ Ready for testing"))

;; Test data generation functions

(defn create-test-state-message
  "Create a test state message with various fields for validation testing.
   Returns a map that can be converted to protobuf."
  [& {:keys [include-invalid-data?]
      :or {include-invalid-data? false}}]
  (let [base-message {:ser.JonSharedData
                      {:time {:ser.JonSharedDataTime
                             {:msSinceBoot (if include-invalid-data? -1000 5000)
                              :clockOffsetObtained true
                              :clockOffsetNs 123456}}
                       :system {:ser.JonSharedDataSystem
                               {:cpuTemp (if include-invalid-data? 200.0 65.5)
                                :cpuLoad 45.2
                                :memoryUsed 1024000
                                :memoryTotal 2048000}}
                       :gps (when-not include-invalid-data?
                             {:ser.JonSharedDataGps
                              {:latitude 37.7749
                               :longitude -122.4194
                               :altitude 50.0
                               :speed 0.0
                               :course 0.0
                               :satellitesVisible 8
                               :fixQuality 2}})}}]
    base-message))

(defn create-test-cmd-message
  "Create a test command message for validation testing.
   Returns a map that can be converted to protobuf."
  [& {:keys [include-invalid-data?]
      :or {include-invalid-data? false}}]
  (let [base-message {:cmd.JonSharedCmd
                      {:system {:cmd.System.JonSharedCmdSystem
                               {:reboot (if include-invalid-data?
                                         {:cmd.System.JonSharedCmdSystem$Reboot
                                          {:delayMs -100}}
                                         {:cmd.System.JonSharedCmdSystem$Reboot
                                          {:delayMs 5000}})}}
                       :dayCamera (when-not include-invalid-data?
                                   {:cmd.DayCamera.JonSharedCmdDayCamera
                                    {:zoom {:cmd.DayCamera.JonSharedCmdDayCamera$Zoom
                                           {:level 2.5}}}})}}]
    base-message))

(defn message-to-bytes
  "Convert a protobuf message to byte array.
   Requires the message to be a protobuf object."
  [proto-message]
  (let [baos (ByteArrayOutputStream.)
        cos (CodedOutputStream/newInstance baos)]
    (.writeTo proto-message cos)
    (.flush cos)
    (.toByteArray baos)))

(defn create-invalid-binary
  "Create an invalid binary payload for testing error handling."
  []
  (byte-array [0x00 0xFF 0xDE 0xAD 0xBE 0xEF]))

(defn create-truncated-binary
  "Create a truncated binary payload for testing error handling.
   Takes a valid message and truncates it."
  [valid-bytes truncate-ratio]
  (let [truncate-point (int (* (count valid-bytes) truncate-ratio))]
    (byte-array (take truncate-point valid-bytes))))

(defn create-corrupted-binary
  "Create a corrupted binary payload by modifying random bytes."
  [valid-bytes corruption-count]
  (let [corrupted (byte-array valid-bytes)
        length (count corrupted)]
    (dotimes [_ corruption-count]
      (let [index (rand-int length)]
        (aset-byte corrupted index (byte (rand-int 256)))))
    corrupted))

;; Auto-initialize when namespace is loaded
;; This ensures all test namespaces that require this will have the system ready
(defonce ^:private initialized? 
  (do 
    (initialize!)
    true))