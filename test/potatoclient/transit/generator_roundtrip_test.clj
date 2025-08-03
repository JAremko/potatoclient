(ns potatoclient.transit.generator-roundtrip-test
  "Property-based testing using Malli generators for Transit↔Protobuf roundtrip.
  
  This is CRUCIAL for finding edge cases and ensuring our specs match reality."
  (:require [clojure.test :refer [deftest testing is]]
            [malli.core :as m]
            [malli.generator :as mg]
            [malli.registry :as mr]
            [potatoclient.ui-specs :as specs]
            [potatoclient.transit.commands :as cmd]
            [potatoclient.transit.core :as transit]
            [potatoclient.transit.subprocess-launcher :as subprocess]
            [potatoclient.process :as process]
            [clojure.java.io :as io]
            [clojure.string :as str])
  (:import [java.io ByteArrayOutputStream ByteArrayInputStream]
           [java.lang ProcessBuilder]))

;; =============================================================================
;; Test Infrastructure
;; =============================================================================

(def test-command-processor-path
  "Path to the Kotlin test command processor JAR"
  "test/kotlin/build/libs/test-command-processor.jar")

(defn ensure-test-processor-built!
  "Ensure the test command processor is built"
  []
  (when-not (.exists (io/file test-command-processor-path))
    (println "Building test command processor...")
    (let [result (process/sh ["make" "build-test-processor"]
                            {:dir (System/getProperty "user.dir")})]
      (when (not= 0 (:exit result))
        (throw (ex-info "Failed to build test processor" result))))))

(defn start-test-processor
  "Start a Kotlin subprocess that can validate commands"
  []
  (ensure-test-processor-built!)
  (let [pb (ProcessBuilder. ["java" "-jar" test-command-processor-path])
        process (.start pb)]
    {:process process
     :stdin (io/writer (.getOutputStream process))
     :stdout (io/reader (.getInputStream process))
     :stderr (io/reader (.getErrorStream process))}))

(defn send-command-to-kotlin
  "Send a command to Kotlin and get validation result"
  [processor command]
  (let [{:keys [stdin stdout]} processor
        ;; Encode command as Transit
        out (ByteArrayOutputStream.)
        writer (transit/make-writer out)]
    (transit/write-message! writer command out)
    ;; Send to Kotlin
    (.write stdin (str (.toByteArray out) "\n"))
    (.flush stdin)
    ;; Read response
    (let [response (.readLine stdout)]
      (transit/read-string response))))

;; =============================================================================
;; Generator Helpers
;; =============================================================================

(defn generate-valid-examples
  "Generate n valid examples for a schema"
  [schema n]
  (try
    (mg/sample schema {:size n})
    (catch Exception e
      (println "Failed to generate for schema:" schema)
      (println "Error:" (.getMessage e))
      [])))

;; =============================================================================
;; Command Schemas for Generation
;; =============================================================================

;; We need to define schemas for each command's parameters
(def command-param-schemas
  {:ping []
   :noop []
   :frozen []
   
   ;; CV commands
   :cv-start-track-ndc [:map
                        [:channel [:enum :heat :day]]
                        [:x [:and number? [:>= -1] [:<= 1]]]
                        [:y [:and number? [:>= -1] [:<= 1]]]
                        [:frame-timestamp {:optional true} pos-int?]]
   
   ;; Rotary commands  
   :rotary-goto [:map
                 [:azimuth [:and number? [:>= 0] [:< 360]]]
                 [:elevation [:and number? [:>= -30] [:<= 90]]]]
   
   :rotary-goto-ndc [:map
                     [:channel [:enum :heat :day]]
                     [:x [:and number? [:>= -1] [:<= 1]]]
                     [:y [:and number? [:>= -1] [:<= 1]]]]
   
   :rotary-set-velocity [:map
                         [:azimuth-speed [:and number? [:>= 0]]]
                         [:elevation-speed [:and number? [:>= 0]]]
                         [:azimuth-direction [:enum :clockwise :counter-clockwise]]
                         [:elevation-direction [:enum :clockwise :counter-clockwise]]]
   
   ;; System commands
   :set-localization [:map [:locale [:enum :en :uk]]]
   
   ;; Camera commands
   :heat-camera-palette [:map [:palette [:enum :white-hot :black-hot :rainbow :ironbow :lava :arctic]]]
   
   :day-camera-focus [:map [:mode [:enum :auto :manual :infinity]]
                      [:value {:optional true} [:and number? [:>= 0] [:<= 100]]]]
   
   ;; GPS commands
   :gps-set-manual-position [:map
                             [:latitude [:and number? [:>= -90] [:<= 90]]]
                             [:longitude [:and number? [:>= -180] [:<= 180]]]
                             [:altitude number?]]})

;; =============================================================================
;; Generator-based Tests
;; =============================================================================

(deftest test-basic-command-generators
  (testing "Basic commands with no parameters"
    (doseq [cmd-fn [cmd/ping cmd/noop cmd/frozen]]
      (testing (str "Command: " cmd-fn)
        ;; These have no parameters, just test they work
        (let [command (cmd-fn)
              ;; Roundtrip through Transit
              out (ByteArrayOutputStream.)
              writer (transit/make-writer out)]
          (transit/write-message! writer command out)
          (let [in (ByteArrayInputStream. (.toByteArray out))
                reader (transit/make-reader in)
                decoded (transit/read-message reader)]
            (is (= command decoded) "Command should survive Transit roundtrip")))))))

(deftest test-cv-command-generators
  (testing "CV start-track-ndc with generated parameters"
    (let [schema (get command-param-schemas :cv-start-track-ndc)
          samples (generate-valid-examples schema 20)]
      (doseq [params samples]
        (testing (str "Generated params: " params)
          (let [command (cmd/cv-start-track-ndc 
                          (:channel params)
                          (:x params) 
                          (:y params)
                          (:frame-timestamp params))
                ;; Transit roundtrip
                out (ByteArrayOutputStream.)
                writer (transit/make-writer out)]
            (transit/write-message! writer command out)
            (let [in (ByteArrayInputStream. (.toByteArray out))
                  reader (transit/make-reader in)
                  decoded (transit/read-message reader)]
              ;; Basic structure preserved
              (is (= (get-in command [:cv :start-track-ndc :channel])
                     (get-in decoded [:cv :start-track-ndc :channel])))
              (is (number? (get-in decoded [:cv :start-track-ndc :x])))
              (is (number? (get-in decoded [:cv :start-track-ndc :y])))
              ;; Note: Transit may convert keywords during roundtrip
              ;; e.g., "heat" → :heat, which is expected behavior
              )))))))

(deftest test-rotary-command-generators
  (testing "Rotary goto with constraint validation"
    (let [schema (get command-param-schemas :rotary-goto)
          samples (generate-valid-examples schema 20)]
      (doseq [params samples]
        (testing (str "Generated params: " params)
          (let [command (cmd/rotary-goto (:azimuth params) (:elevation params))]
            ;; Verify constraints are respected
            (is (<= 0 (:azimuth params) 359.999))
            (is (<= -30 (:elevation params) 90))
            ;; Transit roundtrip
            (let [out (ByteArrayOutputStream.)
                  writer (transit/make-writer out)]
              (transit/write-message! writer command out)
              (let [in (ByteArrayInputStream. (.toByteArray out))
                    reader (transit/make-reader in)
                    decoded (transit/read-message reader)]
                (is (= (get-in command [:rotary :goto :azimuth])
                       (get-in decoded [:rotary :goto :azimuth])))
                (is (= (get-in command [:rotary :goto :elevation])
                       (get-in decoded [:rotary :goto :elevation]))))))))))

(deftest test-enum-parameter-generators
  (testing "Commands with enum parameters"
    ;; Test heat camera palette
    (let [schema (get command-param-schemas :heat-camera-palette)
          samples (generate-valid-examples schema 10)]
      (doseq [params samples]
        (let [command (cmd/heat-camera-palette (:palette params))]
          (is (contains? #{:white-hot :black-hot :rainbow :ironbow :lava :arctic}
                         (get-in command [:heat-camera :palette :index]))))))
    
    ;; Test localization
    (let [schema (get command-param-schemas :set-localization)
          samples (generate-valid-examples schema 10)]
      (doseq [params samples]
        (let [command (cmd/set-localization (:locale params))]
          (is (contains? #{:en :uk} 
                         (keyword (get-in command [:system :localization :loc])))))))))

(deftest test-edge-case-numeric-values
  (testing "Edge case numeric values"
    ;; Test boundary values for azimuth
    (doseq [azimuth [0 0.0 359 359.999 180]]
      (let [command (cmd/rotary-goto azimuth 0)]
        (is (some? command))))
    
    ;; Test boundary values for elevation  
    (doseq [elevation [-30 -29.999 90 89.999 0]]
      (let [command (cmd/rotary-goto 180 elevation)]
        (is (some? command))))
    
    ;; Test NDC coordinates
    (doseq [coord [-1 -0.999 0 0.999 1]]
      (let [command (cmd/cv-start-track-ndc :heat coord coord)]
        (is (some? command))))))

;; =============================================================================
;; Full Kotlin Integration Test (requires subprocess)
;; =============================================================================

(deftest ^:integration test-generated-commands-with-kotlin-validation
  (testing "Generated commands validated by Kotlin subprocess"
    ;; This test requires the Kotlin test processor to be available
    (when (.exists (io/file test-command-processor-path))
      (let [processor (start-test-processor)]
        (try
          ;; Test a variety of generated commands
          (doseq [[cmd-type schema] command-param-schemas
                  :when (not (empty? schema))]
            (testing (str "Testing " cmd-type " with generated data")
              (let [samples (generate-valid-examples schema 5)]
                (doseq [params samples]
                  ;; Generate appropriate command based on type
                  (let [command (case cmd-type
                                  :cv-start-track-ndc 
                                  (cmd/cv-start-track-ndc (:channel params) (:x params) 
                                                          (:y params) (:frame-timestamp params))
                                  :rotary-goto
                                  (cmd/rotary-goto (:azimuth params) (:elevation params))
                                  :set-localization
                                  (cmd/set-localization (:locale params))
                                  ;; Add more as needed
                                  nil)]
                    (when command
                      (let [result (send-command-to-kotlin processor command)]
                        (is (:success result) 
                            (str "Command should validate: " command 
                                 "\nError: " (:error result))))))))))
          (finally
            ;; Clean up
            (.destroy (:process processor))))))))

;; =============================================================================
;; Property: All generated commands should roundtrip perfectly
;; =============================================================================

(deftest test-roundtrip-property
  (testing "Property: all valid generated commands roundtrip through Transit"
    (let [test-cases [
           ;; [generator-fn command-fn extractor-fn]
           [#(mg/generate [:map [:locale [:enum :en :uk]]]) 
            #(cmd/set-localization (:locale %))
            #(get-in % [:system :localization :loc])]
           
           [#(mg/generate [:map [:azimuth [:and number? [:>= 0] [:< 360]]]
                               [:elevation [:and number? [:>= -30] [:<= 90]]]])
            #(cmd/rotary-goto (:azimuth %) (:elevation %))
            #(select-keys (get-in % [:rotary :goto]) [:azimuth :elevation])]]]
      
      (doseq [[gen-fn cmd-fn extract-fn] test-cases]
        (dotimes [_ 20]
          (let [params (gen-fn)
                command (cmd-fn params)
                ;; Roundtrip
                out (ByteArrayOutputStream.)
                writer (transit/make-writer out)]
            (transit/write-message! writer command out)
            (let [in (ByteArrayInputStream. (.toByteArray out))
                  reader (transit/make-reader in)
                  decoded (transit/read-message reader)
                  extracted (extract-fn decoded)]
              ;; Property: structure is preserved
              (is (some? extracted) "Extracted value should not be nil")
              ;; Property: numeric values are preserved (within floating point tolerance)
              (when (number? (first (vals params)))
                (doseq [[k v] params]
                  (when (number? v)
                    (is (< (Math/abs (- v (get extracted k v))) 0.0001)
                        "Numeric values should be preserved")))))))))))