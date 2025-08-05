(ns proto-explorer.main
  "Main entry point for proto-explorer spec generation."
  (:require [proto-explorer.json-to-edn :as json-edn]
            [proto-explorer.spec-generator :as spec-gen]
            [proto-explorer.cli-jvm :as cli-jvm]
            [clojure.java.io :as io]
            [clojure.string :as str]
            [clojure.tools.cli :as cli]
            [clojure.java.shell :as shell])
  (:gen-class))

(def cli-options
  [["-i" "--input INPUT" "Input directory containing JSON descriptors"
    :default "output/json-descriptors"]
   ["-o" "--output OUTPUT" "Output directory for generated specs"
    :default "../../shared/specs/protobuf"]
   ["-w" "--width WIDTH" "Line width for formatting (default: 80)"
    :default 80
    :parse-fn #(Integer/parseInt %)]
   ["-v" "--verbose" "Verbose output"
    :default false]
   ["-h" "--help"]])

(defn process-descriptor-file
  "Process a single JSON descriptor file."
  [file {:keys [verbose]}]
  (when verbose
    (println "Processing:" (.getName file)))
  (let [descriptor-set (json-edn/load-json-descriptor (.getPath file))
        specs (spec-gen/generate-specs descriptor-set)]
    (when verbose
      (println "  Generated specs for packages:" (keys specs)))
    specs))

(defn merge-specs
  "Merge specs from multiple files, grouping by package."
  [all-specs]
  (reduce (fn [acc specs]
            (reduce (fn [acc2 [pkg pkg-specs]]
                     (update acc2 pkg merge pkg-specs))
                   acc
                   specs))
         {}
         all-specs))

(defn write-spec-file
  "Write a spec file for a package."
  [output-dir package specs & {:keys [width] :or {width 80}}]
  (let [file-name (str (json-edn/snake->kebab package) "-specs.clj")
        file-path (io/file output-dir file-name)
        content (spec-gen/generate-spec-file package specs :width width)]
    (.mkdirs (.getParentFile file-path))
    (spit file-path content)
    (println "Wrote:" (.getPath file-path))
    ;; Run zprint with line width configuration
    (try
      (let [result (shell/sh "clojure" "-M:zprint" 
                            (str "{:width " width "}") 
                            "-w"
                            (.getPath file-path))]
        (if (= 0 (:exit result))
          (println "Formatted:" (.getPath file-path) "with width" width)
          (println "Warning: Failed to format" (.getPath file-path) "- " (:err result))))
      (catch Exception e
        (println "Warning: Could not format" (.getPath file-path) "- " (.getMessage e))))))

(defn generate-all-specs
  "Generate specs from all JSON descriptors in input directory."
  [{:keys [input output verbose width]}]
  (println "Generating Malli specs from JSON descriptors...")
  (println "Input:" input)
  (println "Output:" output)
  (println "Line width:" width)
  
  (let [input-dir (io/file input)
        json-files (filter #(str/ends-with? (.getName %) ".json")
                          (.listFiles input-dir))
        ;; Skip the large descriptor-set.json
        relevant-files (remove #(= "descriptor-set.json" (.getName %))
                              json-files)]
    
    (println (str "\nFound " (count relevant-files) " descriptor files"))
    
    ;; Process all files
    (let [all-specs (map #(process-descriptor-file % {:verbose verbose})
                        relevant-files)
          merged-specs (merge-specs all-specs)]
      
      (println (str "\nGenerated specs for " (count merged-specs) " packages:"))
      (doseq [[pkg spec-count] (map (fn [[k v]] [k (count v)]) merged-specs)]
        (println (str "  " pkg ": " spec-count " specs")))
      
      ;; Write spec files
      (println "\nWriting spec files...")
      (doseq [[package specs] merged-specs]
        (write-spec-file output package specs :width width))
      
      (println "\nDone!"))))

(defn -main
  "Main entry point."
  [& args]
  (let [{:keys [options arguments errors summary]} (cli/parse-opts args cli-options)]
    (cond
      (:help options)
      (do (println "Proto Explorer - Generate Malli specs from protobuf descriptors")
          (println summary))
      
      errors
      (do (println "Errors:")
          (doseq [e errors]
            (println "  " e))
          (System/exit 1))
      
      (= (first arguments) "generate-specs")
      (generate-all-specs options)
      
      ;; Java class info commands
      (#{"java-class" "java-fields" "java-builder" "java-summary"} (first arguments))
      (cli-jvm/dispatch-command (first arguments) (rest arguments))
      
      :else
      (do (println "Usage: proto-explorer COMMAND [options]")
          (println "\nCommands:")
          (println "  generate-specs    Generate Malli specs from protobuf descriptors")
          (println "  java-class        Get Java class info for a protobuf message")
          (println "  java-fields       Get proto field to Java method mapping")
          (println "  java-builder      Get Java builder info")
          (println "  java-summary      Get human-readable Java class summary")
          (println "\nOptions for generate-specs:")
          (println summary)))))