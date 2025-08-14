(ns potatoclient.test-harness
  "Test harness to ensure proto files are compiled and system is initialized.
   Automatically compiles proto classes if they're not available.
   Uses Clojure 1.12's new process features for better control."
  (:require
   [clojure.java.io :as io]
   [clojure.string :as str]
   [clojure.repl.deps :as deps])
  (:import
   [java.io File]))

(defn proto-classes-available?
  "Check if proto classes are compiled and on classpath."
  []
  (try
    ;; Try to load core proto classes
    (Class/forName "ser.JonSharedData")
    (Class/forName "cmd.JonSharedCmd")
    true
    (catch ClassNotFoundException _
      false)))

(defn pronto-classes-available?
  "Check if Pronto classes are available."
  []
  (try
    (Class/forName "pronto.ProtoMap")
    true
    (catch ClassNotFoundException _
      false)))

(defn find-proto-files
  "Find proto files in the examples directory."
  []
  (let [proto-dir (io/file "../examples/protogen/proto")]
    (when (.exists proto-dir)
      (->> (.listFiles proto-dir)
           (filter #(.endsWith (.getName %) ".proto"))
           (map #(.getAbsolutePath %))))))

;; We're not actually using these compile functions since we copy from tools
;; But if we ever need them, they should use proper Java/Clojure APIs
(defn compile-proto-classes!
  "Compile proto files to Java classes.
   This would require protoc to be available, which we avoid by copying from tools."
  []
  (throw (ex-info "Direct proto compilation not implemented. Copy from tools instead."
                  {:reason "Avoiding external protoc dependency"})))

(defn compile-java-sources!
  "Compile Java source files.
   This would require javac, which we avoid by copying from tools."
  [java-dir]
  (throw (ex-info "Direct Java compilation not implemented. Copy from tools instead."
                  {:reason "Avoiding external javac dependency"})))

(defn copy-directory!
  "Copy a directory recursively using Java NIO."
  [src-dir dst-dir]
  (let [src-path (.toPath (io/file src-dir))
        dst-path (.toPath (io/file dst-dir))]
    (doseq [src-file (file-seq (io/file src-dir))]
      (let [src-file-path (.toPath src-file)
            relative-path (.relativize src-path src-file-path)
            dst-file-path (.resolve dst-path relative-path)]
        (when (.isFile src-file)
          ;; Create parent directories if needed
          (io/make-parents (.toFile dst-file-path))
          ;; Copy the file
          (io/copy src-file (.toFile dst-file-path)))))))

(defn copy-proto-classes-from-tools!
  "Copy pre-compiled proto classes from state-explorer or validate tools if available."
  []
  (let [sources ["../tools/state-explorer/target/classes"
                 "../tools/validate/target/classes"
                 "../tools/proto-explorer/target/classes"]
        target-dir (io/file "target/classes")]
    
    ;; Find first available source
    (when-let [source-dir (->> sources
                               (map io/file)
                               (filter #(.exists %))
                               first)]
      (println (format "Copying proto classes from %s" source-dir))
      (.mkdirs target-dir)
      
      ;; Copy ser and cmd directories using Clojure's io functions
      (doseq [subdir ["ser" "cmd"]]
        (let [src (io/file source-dir subdir)
              dst (io/file target-dir subdir)]
          (when (.exists src)
            (try
              (copy-directory! src dst)
              (println (format "Copied %s successfully" subdir))
              (catch Exception e
                (println (format "Warning: Failed to copy %s: %s" subdir (.getMessage e))))))))
      true)))

(defn ensure-proto-classes!
  "Ensure proto classes are available, compiling if necessary."
  []
  (cond
    ;; Already available
    (proto-classes-available?)
    (do 
      (println "Proto classes already available")
      true)
    
    ;; Try to copy from existing tools
    (copy-proto-classes-from-tools!)
    (do
      (println "Proto classes copied from tools")
      true)
    
    ;; Compile from scratch
    :else
    (do
      (println "Compiling proto classes from source...")
      (compile-proto-classes!)
      true)))

(defn ensure-pronto!
  "Ensure Pronto is available on classpath.
   Uses Clojure 1.12's add-lib if needed."
  []
  (when-not (pronto-classes-available?)
    (println "Adding Pronto to classpath...")
    (try
      ;; In Clojure 1.12, we can add libs dynamically
      (deps/add-lib 'com.appsflyer/pronto 
                    {:git/url "https://github.com/JAremko/pronto.git"
                     :git/sha "0fb034bc9c943d6a04177b23eb97436f9ca817f7"})
      (println "Pronto added successfully")
      (catch Exception e
        (println "Note: Could not add Pronto dynamically. Ensure it's in deps.edn")
        (println (.getMessage e))))))

(defn initialize!
  "Initialize the test environment.
   Ensures all prerequisites are met before running tests."
  []
  (println "\n=== Initializing Test Harness ===")
  
  ;; Ensure Pronto is available
  (ensure-pronto!)
  
  ;; Ensure proto classes are compiled
  (ensure-proto-classes!)
  
  ;; Verify everything is ready
  (let [proto-ok? (proto-classes-available?)
        pronto-ok? (pronto-classes-available?)]
    
    (println "\nTest Harness Status:")
    (println (format "  Proto classes: %s" (if proto-ok? "✓" "✗")))
    (println (format "  Pronto library: %s" (if pronto-ok? "✓" "✗")))
    
    (when-not (and proto-ok? pronto-ok?)
      (throw (ex-info "Test harness initialization failed"
                      {:proto proto-ok?
                       :pronto pronto-ok?})))
    
    (println "  Ready for testing!")
    (println "=================================\n")
    true))

;; Auto-initialize when namespace is loaded
;; This ensures all test namespaces that require this will have the system ready
(defonce initialized? 
  (try
    (initialize!)
    (catch Exception e
      (println "Warning: Test harness initialization failed")
      (println (.getMessage e))
      false)))