(ns arrow-spec-checker.core
  "Tool to detect functions without arrow (=>) specs defined.
   Finds all defn/defn- definitions and all => specs, then reports the difference."
  (:require [clojure.java.io :as io]
            [clojure.string :as str]
            [clojure.set :as set]
            [clojure.pprint :as pp]
            [rewrite-clj.zip :as z]
            [rewrite-clj.parser :as p]))

;; ============================================================================
;; File Discovery
;; ============================================================================

(def ^:private excluded-paths
  "Paths relative to src directory to exclude from spec checking since they run before registry initialization."
  #{;; Main init file - runs before registry is fully initialized
    "potatoclient/init.clj"
    ;; Malli init file - part of registry initialization
    "potatoclient/malli/init.clj"
    ;; Oneof schema implementation - part of registry initialization
    "potatoclient/malli/oneof.clj"
    ;; Registry itself - defines the registry
    "potatoclient/malli/registry.clj"})

(defn get-relative-path
  "Get path relative to the base directory."
  [base-dir file-path]
  (let [base-path (.toPath (io/file base-dir))
        file-path (.toPath (io/file file-path))
        relative-path (.relativize base-path file-path)]
    (.toString relative-path)))

(defn excluded-file?
  "Check if a file should be excluded from spec checking based on relative path."
  [base-dir file-path]
  (let [relative-path (get-relative-path base-dir file-path)
        ;; Normalize path separators to forward slashes for consistency
        normalized-path (str/replace relative-path "\\" "/")]
    (or (contains? excluded-paths normalized-path)
        ;; Exclude all files in specs directory
        (str/starts-with? normalized-path "potatoclient/specs/"))))

(defn find-clojure-files
  "Recursively find all .clj files in directory, excluding those in the exclusion list."
  [dir]
  (let [dir-file (io/file dir)]
    (->> (file-seq dir-file)
         (filter #(.isFile %))
         (filter #(str/ends-with? (.getName %) ".clj"))
         (map #(.getPath %))
         (remove #(excluded-file? dir %)))))

;; ============================================================================
;; AST Analysis
;; ============================================================================

(defn extract-function-name
  "Extract function name from defn/defn- form at location."
  [loc]
  (try
    (when-let [name-loc (-> loc z/down z/right)]
      (when (z/sexpr-able? name-loc)
        (z/sexpr name-loc)))
    (catch Exception _ nil)))

(defn extract-arrow-spec-name
  "Extract function name from => spec form at location."
  [loc]
  (try
    ;; => forms are like: (m/=> function-name spec)
    ;; We want the second element (function-name)
    (when-let [name-loc (-> loc z/down z/right)]
      (when (z/sexpr-able? name-loc)
        (z/sexpr name-loc)))
    (catch Exception _ nil)))

(defn analyze-file
  "Analyze a single file for function definitions and arrow specs.
   Returns {:functions #{...} :specs #{...}} where each set contains
   {:name symbol :file path :line number}"
  [file-path]
  (try
    (let [content (slurp file-path)
          zloc (z/of-string content)
          functions (atom #{})
          specs (atom #{})]
      
      ;; Traverse the entire file
      (loop [loc zloc]
        (when-not (z/end? loc)
          ;; Check if this is a list node
          (when (z/list? loc)
            (when-let [first-loc (try (z/down loc) (catch Exception _ nil))]
              (when (and first-loc (z/sexpr-able? first-loc))
                (when-let [first-elem (try (z/sexpr first-loc) (catch Exception _ nil))]
                  (cond
                    ;; Found defn or defn-
                    (contains? #{'defn 'defn-} first-elem)
                    (when-let [fn-name (extract-function-name loc)]
                      (let [node (z/node loc)
                            line (get (meta node) :row 1)]
                        (swap! functions conj {:name fn-name
                                               :file file-path
                                               :line line})))
                    
                    ;; Found arrow spec (any namespace, but symbol ends with =>)
                    (and (symbol? first-elem)
                         (str/ends-with? (name first-elem) "=>"))
                    (when-let [fn-name (extract-arrow-spec-name loc)]
                      (let [node (z/node loc)
                            line (get (meta node) :row 1)]
                        (swap! specs conj {:name fn-name
                                          :file file-path
                                          :line line}))))))))
          
          (recur (z/next loc))))
      
      {:functions @functions
       :specs @specs})
    (catch Exception e
      (println "Error analyzing file:" file-path "-" (.getMessage e))
      {:functions #{}
       :specs #{}})))

;; ============================================================================
;; Main Analysis
;; ============================================================================

(defn find-missing-specs
  "Find all functions without arrow specs in the given directory.
   Returns a sequence of {:name symbol :file path :line number} maps."
  [dir-path]
  (let [files (find-clojure-files dir-path)
        ;; Process files in parallel for speed
        results (pmap analyze-file files)
        ;; Combine all results
        all-functions (apply set/union (map :functions results))
        all-specs (apply set/union (map :specs results))
        ;; Build lookup sets by function name
        spec-names (set (map :name all-specs))
        ;; Find functions without specs
        missing (filter #(not (contains? spec-names (:name %))) all-functions)]
    ;; Sort by file path and line number for clean output
    (sort-by (juxt :file :line) missing)))

;; ============================================================================
;; Output Formatting
;; ============================================================================

(defn format-output
  "Format the missing specs for clean, concise output."
  [missing-specs]
  (if (empty? missing-specs)
    (println "\n✓ All functions have arrow specs defined!")
    (do
      (println (format "\n⚠ Found %d functions without arrow specs:\n" (count missing-specs)))
      ;; Group by file for cleaner output
      (let [by-file (group-by :file missing-specs)]
        (doseq [[file funcs] (sort-by key by-file)]
          (println file)
          (doseq [{:keys [name line]} (sort-by :line funcs)]
            (println (format "  %4d: %s" line name)))
          (println))))))

;; ============================================================================
;; CLI Entry Point
;; ============================================================================

(defn -main
  "Main entry point. Takes a single argument: directory path to scan."
  [& args]
  (cond
    (empty? args)
    (do
      (println "Usage: arrow-spec-checker <directory>")
      (println "")
      (println "Detects functions (defn/defn-) without arrow (=>) specs.")
      (System/exit 1))
    
    (> (count args) 1)
    (do
      (println "Error: Too many arguments. Expected only directory path.")
      (System/exit 1))
    
    :else
    (let [dir-path (first args)
          dir-file (io/file dir-path)]
      (cond
        (not (.exists dir-file))
        (do
          (println (format "Error: Directory '%s' does not exist." dir-path))
          (System/exit 1))
        
        (not (.isDirectory dir-file))
        (do
          (println (format "Error: '%s' is not a directory." dir-path))
          (System/exit 1))
        
        :else
        (do
          (println (format "Scanning %s for missing arrow specs..." dir-path))
          (println (format "Excluding: %s, potatoclient/specs/**" (str/join ", " (sort excluded-paths))))
          (let [missing (find-missing-specs dir-path)]
            (format-output missing)
            ;; Exit with non-zero if missing specs found
            (System/exit (if (empty? missing) 0 1))))))))