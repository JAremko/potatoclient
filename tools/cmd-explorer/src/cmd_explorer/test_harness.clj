(ns cmd-explorer.test-harness
  "Test harness to ensure proto files are compiled and system is initialized.
   This should be required by all test namespaces."
  (:require
   [cmd-explorer.registry :as registry]
   [cmd-explorer.specs.oneof-payload :as oneof]
   [clojure.java.io :as io])
  (:import
   [java.io File]))

(defn proto-classes-compiled?
  "Check if protobuf classes are compiled and available."
  []
  (let [proto-class-dir (io/file "target/classes/cmd")]
    (and (.exists proto-class-dir)
         (.isDirectory proto-class-dir)
         (> (count (.listFiles proto-class-dir)) 0))))

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
    (let [pronto-path (str (System/getProperty "user.home") 
                          "/.gitlibs/libs/com.appsflyer/pronto/0fb034bc9c943d6a04177b23eb97436f9ca817f7")
          src-path (str pronto-path "/src/java")
          target-path "target/classes"
          java-files (file-seq (io/file src-path))
          java-files (filter #(and (.isFile %) 
                                  (.endsWith (.getName %) ".java")) 
                           java-files)]
      
      ;; Create target directory if it doesn't exist
      (.mkdirs (io/file target-path))
      
      ;; Compile Java files
      (when (seq java-files)
        (let [javac-args (concat ["-cp" (System/getProperty "java.class.path")
                                 "-d" target-path]
                               (map #(.getPath %) java-files))
              process (.exec (Runtime/getRuntime) 
                           (into-array String (cons "javac" javac-args)))]
          (.waitFor process)
          (when (not= 0 (.exitValue process))
            (throw (ex-info "Failed to compile Pronto Java sources" 
                           {:exit-code (.exitValue process)}))))
        
        ;; Copy compiled classes to our target directory
        (let [compiled-dir (io/file (str pronto-path "/target/classes/pronto"))
              target-dir (io/file (str target-path "/pronto"))]
          (when (.exists compiled-dir)
            (.mkdirs target-dir)
            (doseq [file (file-seq compiled-dir)]
              (when (.isFile file)
                (let [relative-path (subs (.getPath file) 
                                         (inc (count (.getPath compiled-dir))))
                      target-file (io/file target-dir relative-path)]
                  (.mkdirs (.getParentFile target-file))
                  (io/copy file target-file))))))
        
        (println "Pronto Java sources compiled successfully")))))

(defn setup-malli-registry!
  "Configure Malli with global registry and register custom schemas."
  []
  (registry/setup-global-registry!
   {:oneof-pronto (oneof/register-oneof-pronto-schema!)})
  ;; Also load cmd-root specs if available
  (try
    (require 'cmd-explorer.specs.cmd-root)
    (catch Exception e
      ;; It's ok if cmd-root doesn't exist yet
      nil)))

(defn initialize!
  "Initialize the test harness.
   Ensures all prerequisites are met before running tests."
  []
  ;; Check and compile Pronto if needed
  (compile-pronto-if-needed!)
  
  ;; Check if proto classes are compiled
  (when-not (proto-classes-compiled?)
    (println "WARNING: Protobuf classes not found in target/classes/cmd")
    (println "Please run 'make build' to compile protobuf files")
    (throw (ex-info "Protobuf classes not compiled" 
                   {:hint "Run 'make build' to compile protobuf files"})))
  
  ;; Set up Malli registry with custom schemas
  (setup-malli-registry!)
  
  ;; Success message
  (println "Test harness initialized:")
  (println "  ✓ Pronto classes available")
  (println "  ✓ Protobuf classes compiled")
  (println "  ✓ Malli global registry configured")
  (println "  ✓ Custom oneof-pronto schema registered")
  (println "  ✓ Ready for testing"))

;; Auto-initialize when namespace is loaded
;; This ensures all test namespaces that require this will have the system ready
(defonce ^:private initialized? 
  (do 
    (initialize!)
    true))