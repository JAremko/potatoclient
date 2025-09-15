(ns docstring-checker.core
  "Tool to detect Clojure definitions without docstrings.
   Finds all def, defn, defn-, and defonce forms lacking documentation."
  (:require [clojure.java.io :as io]
            [clojure.string :as str]
            [clojure.set :as set]
            [rewrite-clj.zip :as z]
            [rewrite-clj.node :as n]))

;; ============================================================================
;; File Discovery
;; ============================================================================

(defn find-clojure-files
  "Recursively find all .clj files in directory (excluding .cljc, .cljs, .bb)."
  [dir]
  (let [dir-file (io/file dir)]
    (->> (file-seq dir-file)
         (filter #(.isFile %))
         (filter #(re-matches #".*\.clj$" (.getName %)))
         (map #(.getPath %)))))

;; ============================================================================
;; Docstring Detection
;; ============================================================================

(defn has-docstring-in-metadata?
  "Check if a metadata map contains a :doc key."
  [metadata-node]
  (when (and metadata-node (n/sexpr-able? metadata-node))
    (let [metadata (n/sexpr metadata-node)]
      (and (map? metadata)
           (contains? metadata :doc)
           (not (str/blank? (:doc metadata)))))))

(defn has-string-docstring?
  "Check if the form has a string docstring after the name.
   For defn/defn-: (defn name \"docstring\" [...] ...)
   For def/defonce: (def name \"docstring\" value)"
  [loc]
  (try
    ;; Move to the name position
    (when-let [name-loc (-> loc z/down z/right)]
      ;; Check if there's a next element after the name
      (when-let [potential-doc-loc (z/right name-loc)]
        ;; For defn/defn-, check if next element is a string
        ;; Need to skip metadata if present
        (let [node (z/node potential-doc-loc)]
          (cond
            ;; Direct string docstring (non-empty)
            (and (n/sexpr-able? node)
                 (string? (n/sexpr node)))
            (not (str/blank? (n/sexpr node)))

            ;; Skip metadata and check next
            (and (= :meta (n/tag node))
                 (z/right potential-doc-loc))
            (let [after-meta (z/right potential-doc-loc)
                  after-meta-node (z/node after-meta)]
              (and (n/sexpr-able? after-meta-node)
                   (string? (n/sexpr after-meta-node))
                   (not (str/blank? (n/sexpr after-meta-node)))))

            :else false))))
    (catch Exception _ false)))

(defn extract-definition-info
  "Extract name and check for docstring in various forms."
  [loc def-type]
  (try
    (let [;; Get the name (second element)
          name-loc (-> loc z/down z/right)
          _ (when-not name-loc (throw (ex-info "No name" {})))

          ;; Check if name has metadata
          name-node (z/node name-loc)
          has-metadata? (= :meta (n/tag name-node))

          ;; Extract actual name
          actual-name (if has-metadata?
                        (when-let [children (n/children name-node)]
                          (when (n/sexpr-able? (second children))
                            (n/sexpr (second children))))
                        (when (n/sexpr-able? name-node)
                          (n/sexpr name-node)))

          ;; Check for docstring in metadata
          metadata-doc? (when has-metadata?
                          (when-let [meta-map (first (n/children name-node))]
                            (has-docstring-in-metadata? meta-map)))

          ;; Check for string docstring
          string-doc? (has-string-docstring? loc)

          ;; Has any form of documentation?
          has-doc? (or metadata-doc? string-doc?)]

      (when actual-name
        {:name actual-name
         :type def-type
         :has-doc? has-doc?}))
    (catch Exception _ nil)))

(defn analyze-file
  "Analyze a single file for definitions and their docstrings.
   Returns {:documented [...] :undocumented [...]} where each item contains
   {:name symbol :file path :line number :type def-type}"
  [file-path]
  (try
    (let [content (slurp file-path)
          zloc (z/of-string content)
          documented (atom [])
          undocumented (atom [])]

      ;; Traverse the entire file
      (loop [loc zloc]
        (when-not (z/end? loc)
          ;; Check if this is a list node
          (when (z/list? loc)
            (when-let [first-loc (try (z/down loc) (catch Exception _ nil))]
              (when (and first-loc (z/sexpr-able? first-loc))
                (when-let [first-elem (try (z/sexpr first-loc) (catch Exception _ nil))]
                  (when (contains? #{'def 'defn 'defn- 'defonce
                                     'defmacro 'defmulti 'defprotocol
                                     'defrecord 'deftype} first-elem)
                    (when-let [info (extract-definition-info loc first-elem)]
                      (let [node (z/node loc)
                            line (get (meta node) :row 1)
                            entry {:name (:name info)
                                   :file file-path
                                   :line line
                                   :type (:type info)}]
                        (if (:has-doc? info)
                          (swap! documented conj entry)
                          (swap! undocumented conj entry)))))))))

          (recur (z/next loc))))

      {:documented @documented
       :undocumented @undocumented})
    (catch Exception e
      (println "Error analyzing file:" file-path "-" (.getMessage e))
      {:documented []
       :undocumented []})))

;; ============================================================================
;; Main Analysis
;; ============================================================================

(defn find-missing-docstrings
  "Find all definitions without docstrings in the given directory.
   Returns a map with statistics and details."
  [dir-path]
  (let [files (find-clojure-files dir-path)
        ;; Process files in parallel for speed
        results (pmap analyze-file files)
        ;; Combine all results
        all-documented (apply concat (map :documented results))
        all-undocumented (apply concat (map :undocumented results))
        ;; Group by type for statistics
        by-type (group-by :type all-undocumented)]
    {:total-definitions (+ (count all-documented) (count all-undocumented))
     :documented (count all-documented)
     :undocumented (count all-undocumented)
     :by-type {:def (count (get by-type 'def []))
               :defn (count (get by-type 'defn []))
               :defn- (count (get by-type 'defn- []))
               :defonce (count (get by-type 'defonce []))
               :defmacro (count (get by-type 'defmacro []))
               :defmulti (count (get by-type 'defmulti []))
               :defprotocol (count (get by-type 'defprotocol []))
               :defrecord (count (get by-type 'defrecord []))
               :deftype (count (get by-type 'deftype []))}
     :missing (sort-by (juxt :file :line) all-undocumented)}))

;; ============================================================================
;; Output Formatting
;; ============================================================================

(defn format-output
  "Format the analysis results for clean, concise output."
  [analysis]
  (let [{:keys [total-definitions documented undocumented by-type missing]} analysis
        coverage (if (pos? total-definitions)
                   (int (* 100 (/ documented total-definitions)))
                   100)]
    (println (format "\n📊 Docstring Coverage: %d%% (%d/%d)"
                     coverage documented total-definitions))
    (println)

    (when (pos? undocumented)
      (println "📈 Missing by type:")
      (doseq [[type-key count] (sort by-type)]
        (when (pos? count)
          (println (format "  %s: %d" (name type-key) count))))
      (println)

      (println (format "⚠️ Found %d definitions without docstrings:\n" undocumented))
      ;; Group by file for cleaner output
      (let [by-file (group-by :file missing)]
        (doseq [[file defs] (sort-by key by-file)]
          (println file)
          (doseq [{:keys [name line type]} (sort-by :line defs)]
            (println (format "  %4d: %s %s" line type name)))
          (println))))

    (when (zero? undocumented)
      (println "✅ All definitions have docstrings!"))))

;; ============================================================================
;; CLI Entry Point
;; ============================================================================

(defn -main
  "Main entry point. Takes optional directory path to scan.
   Without arguments, scans both src/ and test/ directories."
  [& args]
  (cond
    ;; No arguments - scan both src and test
    (empty? args)
    (let [;; Get project root - hardcode relative path from tool location
          current-dir (io/file (System/getProperty "user.dir"))
          ;; If we're in tools/docstring-checker, go up 2 levels to project root
          ;; Otherwise look for deps.edn with src/test dirs
          project-root (if (str/ends-with? (.getPath current-dir) "tools/docstring-checker")
                         (-> current-dir (.getParentFile) (.getParentFile) (.getAbsolutePath))
                         ;; Otherwise find project root by looking for deps.edn with src/test
                         (loop [dir current-dir]
                           (let [deps-file (io/file dir "deps.edn")
                                 src-dir (io/file dir "src")
                                 test-dir (io/file dir "test")]
                             (cond
                               ;; Found deps.edn with src and/or test dirs
                               (and (.exists deps-file)
                                    (or (.exists src-dir) (.exists test-dir)))
                               (.getAbsolutePath dir)

                               ;; Can't go up anymore
                               (nil? (.getParentFile dir))
                               (.getAbsolutePath current-dir)

                               ;; Go up one level
                               :else
                               (recur (.getParentFile dir))))))
          src-dir (io/file project-root "src")
          test-dir (io/file project-root "test")
          dirs-to-scan (filter #(.exists %) [src-dir test-dir])]
      (if (empty? dirs-to-scan)
        (do
          (println "Error: No src/ or test/ directories found in project root.")
          (System/exit 1))
        (do
          (println "Scanning project for missing docstrings...")
          (println (format "  Project root: %s" project-root))
          ;; Analyze each directory
          (let [all-results (for [dir dirs-to-scan]
                              (do
                                (println (format "  Scanning: %s" (.getName dir)))
                                (find-missing-docstrings (.getAbsolutePath dir))))
                ;; Combine results
                all-documented (apply concat (map (comp :missing #(hash-map :missing (:documented %))) all-results))
                all-undocumented (apply concat (map :missing all-results))
                total-defs (apply + (map :total-definitions all-results))
                documented-count (apply + (map :documented all-results))
                ;; Aggregate by type
                by-type (reduce (fn [acc result]
                                  (merge-with + acc (:by-type result)))
                                {} all-results)
                combined-analysis {:total-definitions total-defs
                                   :documented documented-count
                                   :undocumented (count all-undocumented)
                                   :by-type by-type
                                   :missing all-undocumented}]
            (format-output combined-analysis)
            ;; Exit with non-zero if missing docstrings found
            (System/exit (if (zero? (:undocumented combined-analysis)) 0 1))))))

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
          (println (format "Scanning %s for missing docstrings..." dir-path))
          (let [analysis (find-missing-docstrings dir-path)]
            (format-output analysis)
            ;; Exit with non-zero if missing docstrings found
            (System/exit (if (zero? (:undocumented analysis)) 0 1))))))))