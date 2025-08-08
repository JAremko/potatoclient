(ns state-explorer.test-harness
  "Test harness to ensure proto files are compiled and system is initialized.
   This should be required by all test namespaces."
  (:require
   [clojure.java.io :as io]
   [clojure.java.shell :as shell])
  (:import
   [java.io File]))

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
  (println "State-Explorer test harness initialized:")
  (println "  ✓ Pronto classes available")
  (println "  ✓ Protobuf classes compiled")
  (println "  ✓ Ready for testing"))

;; Auto-initialize when namespace is loaded
;; This ensures all test namespaces that require this will have the system ready
(defonce ^:private initialized? 
  (do 
    (initialize!)
    true))