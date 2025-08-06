(ns test-helper
  "Helper functions for setting up test environment"
  (:require [clojure.java.io :as io]
            [clojure.java.shell :as shell]
            [clojure.string :as str]))

(defn compile-java-protos!
  "Compile Java protobuf classes for tests"
  []
  (println "Compiling protobuf Java classes...")
  (let [proto-dir "../proto-explorer/output/java-src"
        target-dir "target/classes"
        ;; Find all .java files
        java-files (->> (file-seq (io/file proto-dir))
                       (filter #(str/ends-with? (.getName %) ".java"))
                       (map #(.getPath %)))]
    
    ;; Create target directory
    (.mkdirs (io/file target-dir))
    
    ;; Compile Java files
    (when (seq java-files)
      (let [result (apply shell/sh 
                         "javac" 
                         "-d" target-dir
                         "-cp" (System/getProperty "java.class.path")
                         java-files)]
        (when-not (zero? (:exit result))
          (throw (ex-info "Failed to compile Java protobuf classes" result)))))
    
    (println "Compiled" (count java-files) "Java files")))

(defn setup-test-classpath!
  "Add necessary paths to classpath for tests"
  []
  ;; Add target/classes to classpath if needed
  ;; This is usually handled by deps.edn but we can ensure it here
  (let [target-classes (io/file "target/classes")]
    (when (.exists target-classes)
      ;; Classpath is already set by deps.edn, but we can verify
      (println "Test classpath includes:" target-classes))))

(defn clean-test-output!
  "Clean test output directories"
  []
  (doseq [dir ["test-output" "test-roundtrip-output" 
               "test-validate-output" "test-malli-output"]]
    (when (.exists (io/file dir))
      (println "Cleaning" dir)
      ;; Recursively delete
      (doseq [file (reverse (file-seq (io/file dir)))]
        (.delete file)))))

(defn ensure-proto-classes!
  "Ensure protobuf Java classes are available"
  []
  (try
    ;; Try to load a proto class
    (Class/forName "cmd.JonSharedCmd")
    (println "Protobuf classes already available")
    (catch ClassNotFoundException _
      (println "Protobuf classes not found, compiling...")
      (compile-java-protos!))))