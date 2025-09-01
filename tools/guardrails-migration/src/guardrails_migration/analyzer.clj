(ns guardrails-migration.analyzer
  "Analyze Guardrails patterns in the codebase without modifying files"
  (:require [clojure.string :as str]
            [clojure.java.io :as io]
            [clojure.pprint :as pp]))

;; ============================================================================
;; Pattern collection
;; ============================================================================

(defn extract-gspecs-from-form
  "Extract all gspecs from a >defn form"
  [form]
  (when (and (list? form)
             (contains? #{'> '>defn '>defn-} (first form)))
    (let [defn-type (first form)
          [_ name-sym & rest] form
          
          ;; Skip optional docstring
          rest (if (string? (first rest))
                 (next rest)
                 rest)
          
          ;; Skip optional attr-map
          rest (if (map? (first rest))
                 (next rest)
                 rest)
          
          ;; Check if multi-arity
          multi-arity? (list? (first rest))
          
          ;; Extract gspecs
          gspecs (if multi-arity?
                   ;; Multi-arity - each clause may have a gspec
                   (for [clause rest
                         :let [[_args & body] clause
                               gspec (first body)]
                         :when (and (vector? gspec)
                                   (some #(= '=> %) gspec))]
                     gspec)
                   ;; Single arity
                   (let [[_args & body] rest
                         gspec (first body)]
                     (when (and (vector? gspec)
                               (some #(= '=> %) gspec))
                       [gspec])))]
      
      (when (seq gspecs)
        {:file nil  ; Will be filled by caller
         :line nil  ; Will be filled by caller
         :defn-type defn-type
         :name name-sym
         :multi-arity? multi-arity?
         :gspecs gspecs}))))

(defn analyze-file
  "Analyze a single file for Guardrails patterns"
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
                        :let [extracted (extract-gspecs-from-form form)]
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

(defn unique-gspec-shapes
  "Extract unique gspec shapes from patterns"
  [patterns]
  (let [all-gspecs (mapcat :gspecs patterns)]
    (distinct all-gspecs)))

(defn categorize-specs
  "Categorize individual spec elements"
  [patterns]
  (let [all-gspecs (mapcat :gspecs patterns)
        all-specs (flatten all-gspecs)
        categorized {:predicates (set (filter #(and (symbol? %)
                                                     (str/ends-with? (name %) "?"))
                                              all-specs))
                     :keywords (set (filter keyword? all-specs))
                     :qualified-symbols (set (filter #(and (symbol? %)
                                                           (namespace %))
                                                     all-specs))
                     :vectors (set (filter vector? all-specs))
                     :lists (set (filter list? all-specs))
                     :special-operators (set (filter #{'=> '| '?} all-specs))}]
    categorized))

(defn analyze-complexity
  "Analyze complexity of gspecs"
  [patterns]
  (let [all-gspecs (mapcat :gspecs patterns)]
    {:total-functions (count patterns)
     :multi-arity-functions (count (filter :multi-arity? patterns))
     :total-gspecs (count all-gspecs)
     :with-such-that (count (filter #(some #{'|} %) all-gspecs))
     :with-nilable (count (filter #(some (fn [x] (and (list? x) (= '? (first x)))) %) all-gspecs))
     :with-variadic (count (filter #(some (fn [x] (and (vector? x) (= :* (first x)))) %) all-gspecs))
     :max-args (apply max 0 (for [gspec all-gspecs]
                              (count (take-while #(not= '=> %) gspec))))}))

;; ============================================================================
;; Reporting
;; ============================================================================

(defn generate-report
  "Generate a comprehensive report of Guardrails patterns"
  [patterns output-file]
  (let [report {:summary {:total-files (count (distinct (map :file patterns)))
                          :total-functions (count patterns)
                          :by-type (frequencies (map :defn-type patterns))}
                :complexity (analyze-complexity patterns)
                :unique-gspec-shapes (unique-gspec-shapes patterns)
                :categorized-specs (categorize-specs patterns)
                :all-patterns patterns}]
    
    (spit output-file (with-out-str 
                        (println "=" (str/join "=" (repeat 78 "=")))
                        (println "GUARDRAILS PATTERN ANALYSIS REPORT")
                        (println "=" (str/join "=" (repeat 78 "=")))
                        (println)
                        
                        (println "## SUMMARY")
                        (println "Total files:" (get-in report [:summary :total-files]))
                        (println "Total functions:" (get-in report [:summary :total-functions]))
                        (println "By type:" (pr-str (get-in report [:summary :by-type])))
                        (println)
                        
                        (println "## COMPLEXITY ANALYSIS")
                        (pp/pprint (:complexity report))
                        (println)
                        
                        (println "## UNIQUE GSPEC PATTERNS")
                        (println "Found" (count (:unique-gspec-shapes report)) "unique patterns:")
                        (doseq [gspec (take 50 (:unique-gspec-shapes report))]
                          (println "  " (pr-str gspec)))
                        (when (> (count (:unique-gspec-shapes report)) 50)
                          (println "  ... and" (- (count (:unique-gspec-shapes report)) 50) "more"))
                        (println)
                        
                        (println "## SPEC CATEGORIES")
                        (let [cats (:categorized-specs report)]
                          (println "Predicates:" (count (:predicates cats)))
                          (doseq [pred (sort (:predicates cats))]
                            (println "  " pred))
                          (println)
                          
                          (println "Keywords:" (count (:keywords cats)))
                          (doseq [kw (sort (:keywords cats))]
                            (println "  " kw))
                          (println)
                          
                          (println "Qualified symbols:" (count (:qualified-symbols cats)))
                          (doseq [sym (sort (:qualified-symbols cats))]
                            (println "  " sym))
                          (println)
                          
                          (println "Vector patterns:" (count (:vectors cats)))
                          (doseq [v (take 20 (sort-by str (:vectors cats)))]
                            (println "  " (pr-str v)))
                          (when (> (count (:vectors cats)) 20)
                            (println "  ... and" (- (count (:vectors cats)) 20) "more"))
                          (println)
                          
                          (println "List patterns:" (count (:lists cats)))
                          (doseq [l (sort-by str (:lists cats))]
                            (println "  " (pr-str l))))
                        (println)
                        
                        (println "## DETAILED PATTERNS BY FILE")
                        (doseq [[file file-patterns] (group-by :file patterns)]
                          (println "\n### " file)
                          (doseq [pattern file-patterns]
                            (println "  " (:defn-type pattern) (:name pattern))
                            (doseq [gspec (:gspecs pattern)]
                              (println "    " (pr-str gspec)))))))
    
    ;; Also save the EDN data for programmatic analysis
    (spit (str output-file ".edn") (pr-str report))
    
    {:report-file output-file
     :data-file (str output-file ".edn")
     :summary (:summary report)}))

;; ============================================================================
;; Main entry point
;; ============================================================================

(defn analyze-codebase
  "Analyze the entire codebase for Guardrails patterns"
  [source-dir output-file]
  (println "Analyzing" source-dir "...")
  (let [patterns (analyze-directory source-dir)
        result (generate-report patterns output-file)]
    (println "Analysis complete!")
    (println "Report saved to:" (:report-file result))
    (println "Data saved to:" (:data-file result))
    (println "Summary:" (:summary result))
    result))