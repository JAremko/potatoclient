(ns malli-cleanup.core
  "Main entry point for Malli cleanup tool"
  (:require [clojure.string :as str]
            [clojure.java.io :as io]
            [clojure.tools.cli :as cli]
            [malli-cleanup.analyzer :as analyzer]
            [malli-cleanup.simple-transformer :as transformer]))

(def cli-options
  [["-a" "--analyze" "Run analysis only (no transformation)"]
   ["-t" "--transform" "Transform files (default is dry-run)"]
   ["-o" "--output DIR" "Output directory for transformed files"
    :default "malli-migrated"]
   ["-r" "--report FILE" "Report file path"
    :default "malli-analysis-report.txt"]
   ["-h" "--help"]])

(defn usage [options-summary]
  (str/join
   \newline
   ["Malli Cleanup Tool"
    ""
    "Usage: clojure -M:run [options] [source-dirs...]"
    ""
    "Options:"
    options-summary
    ""
    "Actions:"
    "  --analyze    Analyze codebase and generate report"
    "  --transform  Transform files (saves to output directory)"
    "  (default)    Dry run - shows what would be changed"
    ""
    "Examples:"
    "  # Analyze the codebase"
    "  clojure -M:run --analyze ../../src ../../shared/src"
    ""
    "  # Transform files to output directory"
    "  clojure -M:run --transform -o migrated ../../src"]))

(defn error-msg [errors]
  (str "The following errors occurred while parsing your command:\n\n"
       (str/join \newline errors)))

(defn validate-args
  "Validate command line arguments"
  [args]
  (let [{:keys [options arguments errors summary]} (cli/parse-opts args cli-options)]
    (cond
      (:help options)
      {:exit-message (usage summary) :ok? true}
      
      errors
      {:exit-message (error-msg errors)}
      
      (empty? arguments)
      {:exit-message "Error: Please specify at least one source directory"}
      
      :else
      {:options options :arguments arguments})))

(defn find-clj-files
  "Recursively find all .clj files in directories"
  [dirs]
  (mapcat (fn [dir]
            (let [dir-file (io/file dir)]
              (->> (file-seq dir-file)
                   (filter #(.isFile %))
                   (filter #(str/ends-with? (.getName %) ".clj"))
                   (map #(.getPath %)))))
          dirs))

(defn relativize-path
  "Get relative path from base directory"
  [base-dir file-path]
  (let [base (io/file base-dir)
        file (io/file file-path)
        relative-path (.relativize (.toPath base) (.toPath file))]
    (str relative-path)))

(defn transform-files
  "Transform files and save to output directory"
  [source-dirs output-dir]
  (let [files (find-clj-files source-dirs)]
    (println "Found" (count files) "Clojure files")
    (println "Output directory:" output-dir)
    (io/make-parents (io/file output-dir "dummy"))
    
    (doseq [file-path files]
      (let [content (slurp file-path)
            transformed (transformer/transform-file-content content)
            ;; Find which source dir this file belongs to
            source-dir (first (filter #(str/starts-with? file-path %) source-dirs))
            rel-path (relativize-path source-dir file-path)
            output-path (io/file output-dir rel-path)]
        
        (when (not= content transformed)
          (println "Transforming:" file-path "→" (.getPath output-path))
          (io/make-parents output-path)
          (spit output-path transformed))))
    
    (println "Transformation complete!")))

(defn dry-run
  "Show what would be changed without modifying files"
  [source-dirs]
  (let [files (find-clj-files source-dirs)
        changes (atom [])]
    
    (println "Analyzing" (count files) "files for potential changes...")
    
    (doseq [file-path files]
      (let [content (slurp file-path)
            transformed (transformer/transform-file-content content)]
        (when (not= content transformed)
          (swap! changes conj file-path)
          (println "\nFile:" file-path)
          (println "Changes:")
          ;; Show a diff-like output
          (let [original-lines (str/split-lines content)
                transformed-lines (str/split-lines transformed)]
            (doseq [[idx orig trans] (map vector 
                                          (range)
                                          original-lines 
                                          transformed-lines)]
              (when (not= orig trans)
                (println (format "  Line %d:" (inc idx)))
                (println "    -" orig)
                (println "    +" trans)))))))
    
    (println "\n" "=" (str/join "=" (repeat 78 "=")))
    (println "Summary:" (count @changes) "files would be modified")
    @changes))

(defn -main
  "Main entry point"
  [& args]
  (let [{:keys [options arguments exit-message ok?]} (validate-args args)]
    (if exit-message
      (do (println exit-message)
          (System/exit (if ok? 0 1)))
      (let [source-dirs arguments]
        (cond
          (:analyze options)
          (do (println "Running analysis...")
              (analyzer/analyze-codebase source-dirs (:report options)))
          
          (:transform options)
          (do (println "Transforming files...")
              (transform-files source-dirs (:output options)))
          
          :else
          (do (println "Running dry-run...")
              (dry-run source-dirs)))
        
        (System/exit 0)))))