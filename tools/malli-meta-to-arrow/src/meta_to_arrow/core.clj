(ns meta-to-arrow.core
  "Convert :malli/schema metadata to m/=> declarations for better instrumentation.
   
   The problem: When malli schemas are in metadata, malli.dev/start! doesn't 
   automatically discover them during instrumentation.
   
   The solution: Convert to m/=> declarations which are automatically discovered."
  (:require [clojure.string :as str]
            [clojure.java.io :as io]
            [meta-to-arrow.working-transform :as transform]
            [meta-to-arrow.backup :as backup]
            [clojure.tools.cli :refer [parse-opts]]))

;; ============================================================================
;; File Processing
;; ============================================================================

(defn find-clojure-files
  "Recursively find all .clj and .cljc files in directory."
  [dir]
  (let [dir-file (io/file dir)]
    (->> (file-seq dir-file)
         (filter #(.isFile %))
         (filter #(re-matches #".*\.clj[c]?$" (.getName %)))
         (map #(.getPath %)))))

(defn process-file
  "Process a single file, converting metadata schemas to m/=> forms."
  [input-path output-path {:keys [dry-run verbose require-alias]}]
  (try
    (let [content (slurp input-path)
          result (transform/transform-file content {:require-alias require-alias})
          {:keys [transformed count additions]} result]
      
      (when (and verbose (pos? count))
        (println (format "  %s: %d functions converted" input-path count))
        (when (seq additions)
          (doseq [add additions]
            (println (format "    + %s" (:name add))))))
      
      (when-not dry-run
        (io/make-parents output-path)
        (spit output-path transformed))
      
      {:status :success
       :file input-path
       :output output-path
       :count count
       :additions additions})
    
    (catch Exception e
      {:status :error
       :file input-path
       :message (.getMessage e)
       :exception e})))

(defn process-directory
  "Process all Clojure files in a directory."
  [input-dir output-dir options]
  (let [files (find-clojure-files input-dir)
        results (doall
                 (for [file files]
                   (let [rel-path (str/replace file (str input-dir "/") "")
                         output-path (str output-dir "/" rel-path)]
                     (process-file file output-path options))))]
    
    {:total (count files)
     :success (count (filter #(= :success (:status %)) results))
     :errors (filter #(= :error (:status %)) results)
     :total-functions (->> results
                           (filter #(= :success (:status %)))
                           (map :count)
                           (reduce + 0))
     :files results}))

;; ============================================================================
;; CLI
;; ============================================================================

(def cli-options
  [["-i" "--input PATH" "Input file or directory"
    :required true]
   ["-o" "--output PATH" "Output file or directory (defaults to input for in-place)"
    :default nil]
   ["-b" "--backup" "Create backup before in-place modification"
    :default true]
   ["-d" "--dry-run" "Show what would be changed without writing files"]
   ["-v" "--verbose" "Show detailed progress"]
   ["-r" "--require-alias ALIAS" "Alias to use for malli.core (e.g., 'm' or 'malli')"
    :default "m"]
   ["-h" "--help"]])

(defn -main [& args]
  (let [{:keys [options errors summary]} (parse-opts args cli-options)]
    (cond
      (:help options)
      (do (println "Malli Metadata to Arrow (m/=>) Migration Tool")
          (println)
          (println "Converts :malli/schema metadata to m/=> declarations for better")
          (println "instrumentation support with malli.dev/start!")
          (println)
          (println "Usage: clojure -M:run [options]")
          (println)
          (println summary))
      
      errors
      (do (println "Errors:")
          (doseq [err errors]
            (println "  " err))
          (System/exit 1))
      
      :else
      (let [{:keys [input output backup dry-run verbose require-alias]} options
            input-file (io/file input)
            output-path (or output input)  ; Default to in-place
            in-place? (= input output-path)]
        
        ;; Create backup if doing in-place modification
        (when (and in-place? backup (not dry-run))
          (println "Creating backup...")
          (let [backup-result (if (.isDirectory input-file)
                                (backup/backup-directory input)
                                (backup/backup-file input))]
            (println "Backup saved to:" (:backup-path backup-result))))
        
        (cond
          ;; Directory processing
          (.isDirectory input-file)
          (let [result (process-directory input output-path options)]
            (println)
            (println "Migration complete!")
            (println (format "Total files processed: %d" (:total result)))
            (println (format "Successfully migrated: %d files" (:success result)))
            (println (format "Total functions converted: %d" (:total-functions result)))
            
            (when (seq (:errors result))
              (println)
              (println "Errors:")
              (doseq [err (:errors result)]
                (println "  -" (:file err) ":" (:message err))))
            
            (when dry-run
              (println)
              (println "[DRY RUN] No files were actually modified")))
          
          ;; Single file processing
          (.isFile input-file)
          (let [result (process-file input output-path options)]
            (if (= :success (:status result))
              (do
                (println (format "Successfully migrated %s" input))
                (println (format "Functions converted: %d" (:count result)))
                (when dry-run
                  (println "[DRY RUN] File was not actually modified")))
              (println "Error:" (:message result))))
          
          :else
          (println "Input path does not exist:" input))))))