(ns malli-cleanup.analyzer
  "Analyze Malli metadata patterns in the codebase without modifying files"
  (:require [clojure.string :as str]
            [clojure.java.io :as io]
            [clojure.pprint :as pp]
            [rewrite-clj.zip :as z]
            [rewrite-clj.parser :as p]))

;; ============================================================================
;; Pattern collection
;; ============================================================================

(declare contains-lambda?)

(defn extract-malli-metadata-from-form
  "Extract Malli metadata from a defn or defn- form"
  [form]
  (when (and (list? form)
             (contains? #{'defn 'defn-} (first form)))
    (let [defn-type (first form)
          [_ name-sym & rest] form
          
          ;; Check for inline metadata on function name
          inline-meta (when (and (map? (first rest))
                                 (:malli/schema (first rest)))
                       (first rest))
          
          ;; Skip inline meta if present
          rest (if inline-meta (next rest) rest)
          
          ;; Skip optional docstring
          docstring (when (string? (first rest))
                      (first rest))
          rest (if docstring (next rest) rest)
          
          ;; Check for attr-map after docstring
          attr-map (when (map? (first rest))
                     (first rest))
          
          ;; Get malli schema from either inline meta or attr-map
          malli-schema (or (:malli/schema inline-meta)
                           (:malli/schema attr-map))
          
          ;; Check if multi-arity
          multi-arity? (list? (first (if attr-map (next rest) rest)))]
      
      (when malli-schema
        {:file nil  ; Will be filled by caller
         :line nil  ; Will be filled by caller
         :defn-type defn-type
         :name name-sym
         :multi-arity? multi-arity?
         :has-docstring? (boolean docstring)
         :metadata-position (if inline-meta :inline :attr-map)
         :malli-schema malli-schema
         :has-lambda? (contains-lambda? malli-schema)}))))

(defn contains-lambda?
  "Check if a form contains fn* lambdas that could be converted to partial"
  [form]
  (cond
    (list? form)
    (or (= 'fn* (first form))
        (some contains-lambda? form))
    
    (vector? form)
    (some contains-lambda? form)
    
    (map? form)
    (some contains-lambda? (concat (keys form) (vals form)))
    
    :else false))

(defn extract-lambda-patterns
  "Extract specific lambda patterns that can be converted to partial"
  [form]
  (let [lambdas (atom [])]
    (letfn [(walk [x]
              (cond
                (and (list? x)
                     (= 'fn* (first x))
                     (vector? (second x))
                     (= 1 (count (second x))))
                (let [[_ args body] x
                      arg (first args)]
                  ;; Check if it's a simple partial application pattern
                  (when (and (list? body)
                             (symbol? (first body))
                             (= 2 (count body))
                             (= arg (second body)))
                    (swap! lambdas conj {:lambda x
                                        :target-fn (first body)
                                        :arg arg})))
                
                (list? x) (doseq [item x] (walk item))
                (vector? x) (doseq [item x] (walk item))
                (map? x) (doseq [item (concat (keys x) (vals x))] (walk item))
                :else nil))]
      (walk form)
      @lambdas)))

(defn analyze-file
  "Analyze a single file for Malli patterns"
  [file-path]
  (try
    (let [content (slurp file-path)
          reader (java.io.PushbackReader. (java.io.StringReader. content))
          forms (take-while #(not= ::eof %)
                           (repeatedly #(try 
                                          (read reader false ::eof)
                                          (catch Exception e 
                                            (println "Error reading form in" file-path ":" (.getMessage e))
                                            ::eof))))
          patterns (for [form forms
                        :when (not= ::eof form)
                        :let [extracted (extract-malli-metadata-from-form form)]
                        :when extracted]
                    (assoc extracted :file file-path))]
      patterns)
    (catch Exception e
      (println "Error analyzing file" file-path ":" (.getMessage e))
      [])))

(defn find-clj-files
  "Recursively find all .clj files in a directory"
  [dir]
  (let [dir-file (io/file dir)]
    (->> (file-seq dir-file)
         (filter #(.isFile %))
         (filter #(str/ends-with? (.getName %) ".clj"))
         (map #(.getPath %)))))

(defn analyze-directory
  "Analyze all Clojure files in a directory"
  [dir-path]
  (let [files (find-clj-files dir-path)
        all-patterns (mapcat analyze-file files)]
    all-patterns))

;; ============================================================================
;; Pattern analysis
;; ============================================================================

(defn categorize-patterns
  "Categorize patterns by various criteria"
  [patterns]
  {:total-functions (count patterns)
   :by-type (frequencies (map :defn-type patterns))
   :by-metadata-position (frequencies (map :metadata-position patterns))
   :with-docstring (count (filter :has-docstring? patterns))
   :without-docstring (count (remove :has-docstring? patterns))
   :multi-arity (count (filter :multi-arity? patterns))
   :single-arity (count (remove :multi-arity? patterns))
   :with-lambdas (count (filter :has-lambda? patterns))
   :without-lambdas (count (remove :has-lambda? patterns))})

(defn analyze-lambda-patterns
  "Analyze lambda patterns that can be converted"
  [patterns]
  (let [all-lambdas (mapcat #(extract-lambda-patterns (:malli-schema %)) patterns)
        grouped (group-by :target-fn all-lambdas)]
    {:total-lambdas (count all-lambdas)
     :unique-target-fns (count grouped)
     :by-target-fn (into {} (map (fn [[k v]] [k (count v)]) grouped))
     :examples (take 5 all-lambdas)}))

(defn analyze-malli-schemas
  "Analyze the structure of Malli schemas"
  [patterns]
  (let [schemas (map :malli-schema patterns)]
    {:total-schemas (count schemas)
     :schema-types (frequencies (map #(if (vector? %)
                                        (first %)
                                        :other) schemas))
     :with-=> (count (filter #(and (vector? %)
                                   (some #{:=>} %)) schemas))
     :with-function (count (filter #(and (vector? %)
                                         (some #{:function} %)) schemas))
     :with-fn (count (filter #(and (vector? %)
                                   (some (fn [x]
                                           (and (vector? x)
                                                (= :fn (first x)))) %)) schemas))}))

;; ============================================================================
;; Reporting
;; ============================================================================

(defn generate-report
  "Generate a comprehensive report of Malli patterns"
  [patterns output-file]
  (let [report {:summary (categorize-patterns patterns)
                :lambda-analysis (analyze-lambda-patterns patterns)
                :schema-analysis (analyze-malli-schemas patterns)
                :all-patterns patterns}]
    
    (spit output-file (with-out-str 
                        (println "=" (str/join "=" (repeat 78 "=")))
                        (println "MALLI METADATA ANALYSIS REPORT")
                        (println "=" (str/join "=" (repeat 78 "=")))
                        (println)
                        
                        (println "## SUMMARY")
                        (pp/pprint (:summary report))
                        (println)
                        
                        (println "## MALLI SCHEMA ANALYSIS")
                        (pp/pprint (:schema-analysis report))
                        (println)
                        
                        (println "## LAMBDA PATTERN ANALYSIS")
                        (pp/pprint (:lambda-analysis report))
                        (println)
                        
                        (println "## FUNCTIONS NEEDING TRANSFORMATION")
                        (println "Functions with inline metadata that need moving:")
                        (doseq [p (filter #(= :inline (:metadata-position %)) patterns)]
                          (println "  " (:file p) ":" (:line p) "-" (:defn-type p) (:name p)))
                        (println)
                        
                        (println "Functions with lambdas that can be converted:")
                        (doseq [p (filter :has-lambda? patterns)]
                          (println "  " (:file p) ":" (:line p) "-" (:defn-type p) (:name p))
                          (let [lambdas (extract-lambda-patterns (:malli-schema p))]
                            (doseq [l lambdas]
                              (println "    " "fn* ->" (:target-fn l)))))
                        (println)
                        
                        (println "## DETAILED PATTERNS BY FILE")
                        (doseq [[file file-patterns] (sort-by first (group-by :file patterns))]
                          (println "\n### " file)
                          (doseq [pattern (sort-by :line file-patterns)]
                            (println "  Line" (:line pattern) ":" (:defn-type pattern) (:name pattern))
                            (println "    Position:" (:metadata-position pattern)
                                    "| Docstring:" (:has-docstring? pattern)
                                    "| Lambda:" (:has-lambda? pattern))))))
    
    ;; Also save the EDN data for programmatic analysis
    (spit (str output-file ".edn") (pr-str report))
    
    {:report-file output-file
     :data-file (str output-file ".edn")
     :summary (:summary report)}))

;; ============================================================================
;; Main entry point
;; ============================================================================

(defn analyze-codebase
  "Analyze the entire codebase for Malli patterns"
  [source-dirs output-file]
  (println "Analyzing" (str/join ", " source-dirs) "...")
  (let [patterns (mapcat analyze-directory source-dirs)
        result (generate-report patterns output-file)]
    (println "Analysis complete!")
    (println "Report saved to:" (:report-file result))
    (println "Data saved to:" (:data-file result))
    (println "Summary:" (:summary result))
    result))

(defn -main
  "Main entry point for CLI"
  [& args]
  (let [source-dirs (or (seq args) 
                        ["../../src" "../../shared/src"])
        output-file "malli-analysis-report.txt"]
    (analyze-codebase source-dirs output-file)
    (System/exit 0)))