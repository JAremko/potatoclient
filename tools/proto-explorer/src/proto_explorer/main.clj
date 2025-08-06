(ns proto-explorer.main
  "Main entry point for proto-explorer."
  (:require [proto-explorer.json-to-edn :as json-edn]
            [proto-explorer.cli-jvm :as cli-jvm]
            [clojure.java.io :as io]
            [clojure.string :as str]
            [clojure.tools.cli :as cli]
            [clojure.java.shell :as shell])
  (:gen-class))

(def cli-options
  [["-i" "--input INPUT" "Input directory containing JSON descriptors"
    :default "output/json-descriptors"]
   ["-v" "--verbose" "Verbose output"
    :default false]
   ["-h" "--help"]])

(defn process-descriptor-file
  "Process a single JSON descriptor file."
  [file {:keys [verbose]}]
  (when verbose
    (println "Processing:" (.getName file)))
  (let [descriptor-set (json-edn/load-json-descriptor (.getPath file))]
    (when verbose
      (println "  Loaded descriptor:" (.getName file)))
    descriptor-set))

(defn analyze-json
  "Analyze JSON descriptor files."
  [{:keys [input verbose] :as options}]
  (println "Analyzing JSON descriptors in:" input)
  (let [files (file-seq (io/file input))
        json-files (filter #(and (.isFile %) 
                                (.endsWith (.getName %) ".json")) 
                          files)]
    (println "Found" (count json-files) "JSON descriptor files")
    (doseq [file json-files]
      (process-descriptor-file file options))))

(defn -main
  [& args]
  (let [{:keys [options arguments errors summary]} (cli/parse-opts args cli-options)
        command (first arguments)]
    (cond
      (:help options)
      (do
        (println "Proto Explorer - Protobuf schema analysis tool")
        (println)
        (println "Usage: proto-explorer [options] command")
        (println)
        (println "Commands:")
        (println "  analyze-json    Analyze JSON descriptor files")
        (println "  java-class      Get Java class info for a message")
        (println "  java-fields     Get proto field mapping")
        (println "  java-builder    Get Java builder info")
        (println "  java-summary    Get Java class summary")
        (println)
        (println "Options:")
        (println summary))

      errors
      (do
        (println "Errors:")
        (doseq [error errors]
          (println "  " error))
        (System/exit 1))

      (= command "analyze-json")
      (analyze-json options)

      ;; Delegate Java-related commands to cli-jvm
      (#{"java-class" "java-fields" "java-builder" "java-summary"} command)
      (cli-jvm/-main command (second arguments))

      :else
      (do
        (println "Unknown command:" command)
        (println "Use --help for usage information")
        (System/exit 1)))))