(ns potatoclient.test-harness
  "Test harness to ensure proto files are compiled and system is initialized.
   Automatically compiles proto classes if they're not available.
   Uses Clojure 1.12's new process features for better control."
  (:require
   [clojure.java.io :as io]
   [clojure.java.process :as process]
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

(defn compile-proto-classes!
  "Compile proto files to Java classes using protoc.
   Uses Clojure 1.12's new process features."
  []
  (let [proto-files (find-proto-files)
        java-out-dir "target/classes"
        proto-dir "../examples/protogen/proto"]
    
    (when (empty? proto-files)
      (throw (ex-info "No proto files found" {:dir proto-dir})))
    
    ;; Create output directory
    (.mkdirs (io/file java-out-dir))
    
    (println "Compiling proto files...")
    (println (format "Found %d proto files in %s" (count proto-files) proto-dir))
    
    ;; Use protoc to generate Java classes
    (let [protoc-cmd (into ["protoc"
                            (str "--java_out=" java-out-dir)
                            (str "--proto_path=" proto-dir)]
                           (map #(.getName (io/file %)) proto-files))
          result (process/exec protoc-cmd)]
      
      (if (zero? (:exit result))
        (println "Proto compilation successful")
        (throw (ex-info "Proto compilation failed" 
                        {:exit (:exit result)
                         :err (:err result)}))))))

(defn compile-java-sources!
  "Compile generated Java source files to classes."
  [java-dir]
  (println "Compiling Java sources...")
  
  ;; Find all .java files
  (let [java-files (atom [])]
    (doseq [^File file (file-seq (io/file java-dir))]
      (when (.endsWith (.getName file) ".java")
        (swap! java-files conj (.getAbsolutePath file))))
    
    (if (empty? @java-files)
      (println "No Java files to compile")
      (let [javac-cmd (into ["javac" "-d" java-dir "-cp" 
                             (System/getProperty "java.class.path")]
                            @java-files)
            result (process/exec javac-cmd)]
        
        (if (zero? (:exit result))
          (println "Java compilation successful")
          (throw (ex-info "Java compilation failed"
                          {:exit (:exit result)
                           :err (:err result)})))))))

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
      
      ;; Copy ser and cmd directories
      (doseq [subdir ["ser" "cmd"]]
        (let [src (io/file source-dir subdir)
              dst (io/file target-dir subdir)]
          (when (.exists src)
            (.mkdirs dst)
            ;; Use process to copy files
            (let [result (process/exec ["cp" "-r" 
                                        (.getAbsolutePath src)
                                        (.getAbsolutePath (.getParentFile dst))])]
              (when-not (zero? (:exit result))
                (println (format "Warning: Failed to copy %s" subdir)))))))
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
(defonce ^:private initialized? 
  (try
    (initialize!)
    (catch Exception e
      (println "Warning: Test harness initialization failed")
      (println (.getMessage e))
      false)))