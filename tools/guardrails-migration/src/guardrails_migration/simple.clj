(ns guardrails-migration.simple
  "Simpler approach to Guardrails migration - strip gspec and insert metadata"
  (:require [clojure.string :as str]))

;; ============================================================================
;; Gspec extraction and conversion
;; ============================================================================

(defn extract-gspec
  "Extract gspec from a body (single arity or one clause of multi-arity)"
  [body-forms]
  ;; body-forms is [args-vector & rest]
  ;; Check if first element after args is a gspec
  (when (and (>= (count body-forms) 2)
             (vector? (second body-forms)))
    ;; Could be empty gspec [] or have =>
    (let [potential-gspec (second body-forms)]
      (when (or (empty? potential-gspec)
                (some #(= '=> %) potential-gspec))
        potential-gspec))))

(defn remove-gspec
  "Remove gspec from body forms if present"
  [body-forms]
  (if (extract-gspec body-forms)
    (concat [(first body-forms)] (drop 2 body-forms))
    body-forms))

(defn convert-spec
  "Convert a Guardrails spec to Malli spec"
  [spec]
  (cond
    ;; (? spec) -> [:maybe spec]
    (and (list? spec) (= '? (first spec)))
    [:maybe (convert-spec (second spec))]
    
    ;; Already a Malli spec - keep as-is
    (and (vector? spec) 
         (keyword? (first spec))
         (contains? #{:fn :maybe :map :vector :sequential :map-of :*} (first spec)))
    spec
    
    ;; Predicate function int? -> :int
    (and (symbol? spec) (str/ends-with? (name spec) "?"))
    (let [base-name (str/replace (name spec) #"\?$" "")]
      (case base-name
        "nil" :nil
        "any" :any
        "fn" :fn
        "ifn" :ifn
        (keyword base-name)))
    
    ;; Qualified keyword already
    (keyword? spec) spec
    
    ;; Vector specs - recursively convert
    (vector? spec) (mapv convert-spec spec)
    
    ;; Lists other than (? ...) - keep as-is (might be predicates)
    (list? spec) spec
    
    ;; Qualified symbols to keywords
    (and (symbol? spec) (namespace spec))
    (keyword (namespace spec) (name spec))
    
    ;; Other symbols stay as-is
    :else spec))

(defn parse-gspec
  "Parse gspec into args and return value"
  [gspec]
  (when gspec
    ;; Handle empty gspec []
    (if (empty? gspec)
      nil
      (let [arrow-idx (.indexOf gspec '=>)]
        (when (>= arrow-idx 0)
          (let [args (subvec gspec 0 arrow-idx)
                ret (get gspec (inc arrow-idx))
                
                ;; Filter out such-that clauses (| predicates)
                pipe-idx (.indexOf args '|)
                clean-args (if (>= pipe-idx 0)
                             (subvec args 0 pipe-idx)
                             args)]
            {:args (mapv convert-spec clean-args)
             :ret (convert-spec ret)}))))))

(defn gspec->malli-schema
  "Convert gspec(s) to Malli schema"
  [gspecs]
  (let [parsed (keep parse-gspec gspecs)]
    (cond
      (empty? parsed) nil
      (= 1 (count parsed))
      (let [{:keys [args ret]} (first parsed)]
        [:=> (into [:cat] args) ret])
      :else
      (into [:function]
            (for [{:keys [args ret]} parsed]
              [:=> (into [:cat] args) ret])))))

;; ============================================================================
;; Form transformation
;; ============================================================================

(defn transform-form
  "Transform a >defn form to defn with Malli metadata"
  [form]
  (when (and (list? form)
             (contains? #{'> '>defn '>defn-} (first form)))
    (let [;; Change >defn to defn
          defn-sym (if (= '>defn- (first form)) 'defn- 'defn)
          [_ name-sym & rest] form
          
          ;; Extract optional docstring
          has-doc? (string? (first rest))
          docstring (when has-doc? (first rest))
          rest (if has-doc? (next rest) rest)
          
          ;; Extract optional attr-map
          has-attrs? (map? (first rest))
          attr-map (when has-attrs? (first rest))
          rest (if has-attrs? (next rest) rest)
          
          ;; Check if multi-arity
          multi-arity? (list? (first rest))
          
          ;; Extract gspecs and clean bodies
          [gspecs clean-bodies] (if multi-arity?
                                   ;; Multi-arity
                                   (let [clauses rest]
                                     [(mapv extract-gspec clauses)
                                      (mapv remove-gspec clauses)])
                                   ;; Single arity
                                   [(vector (extract-gspec rest))
                                    (remove-gspec rest)])
          
          ;; Build Malli schema
          malli-schema (gspec->malli-schema gspecs)
          
          ;; Build metadata map
          metadata (cond-> (or attr-map {})
                     malli-schema (assoc :malli/schema malli-schema))
          
          ;; Reconstruct the form
          result (concat
                  [defn-sym name-sym]
                  (when docstring [docstring])
                  (when (or malli-schema attr-map) [metadata])
                  (if multi-arity?
                    (map #(apply list %) clean-bodies)
                    clean-bodies))]
      (apply list result))))

;; ============================================================================
;; Namespace processing
;; ============================================================================

(defn clean-guardrails-from-require
  "Remove Guardrails imports from a require form"
  [require-form]
  (cond
    ;; Simple symbol or keyword
    (or (symbol? require-form) (keyword? require-form))
    require-form
    
    ;; Vector form like [com.fulcrologic.guardrails.core :refer [>defn]]
    (vector? require-form)
    (let [[ns-sym & opts] require-form]
      (if (and (symbol? ns-sym)
                   (or (= 'com.fulcrologic.guardrails.core ns-sym)
                       (= 'com.fulcrologic.guardrails.malli.core ns-sym)))
        nil ; Remove entire Guardrails require
        require-form))
    
    ;; List form (shouldn't happen in require but handle it)
    :else require-form))

(defn clean-ns-form
  "Clean Guardrails imports from ns form"
  [ns-form]
  (if (and (list? ns-form) (= 'ns (first ns-form)))
    (let [[ns-sym name-sym & clauses] ns-form
          cleaned-clauses (for [clause clauses]
                            (if (and (list? clause) (= :require (first clause)))
                              ;; Process :require clause
                              (let [requires (rest clause)
                                    cleaned (keep clean-guardrails-from-require requires)]
                                (when (seq cleaned)
                                  (cons :require cleaned)))
                              ;; Keep other clauses as-is
                              clause))]
      (apply list (concat [ns-sym name-sym] (keep identity cleaned-clauses))))
    ns-form))

;; ============================================================================
;; File processing
;; ============================================================================

(defn process-string
  "Process a string containing Clojure code"
  [code-str]
  (let [reader (java.io.PushbackReader. (java.io.StringReader. code-str))
        forms (take-while #(not= ::eof %)
                          (repeatedly #(try 
                                         (read reader false ::eof)
                                         (catch Exception e ::eof))))]
    (str/join "\n\n"
              (for [form forms
                    :when (not= ::eof form)]
                (cond
                  ;; Process ns form to remove Guardrails imports
                  (and (list? form) (= 'ns (first form)))
                  (pr-str (clean-ns-form form))
                  
                  ;; Process >defn forms
                  (and (list? form)
                       (contains? #{'> '>defn '>defn-} (first form)))
                  (pr-str (transform-form form))
                  
                  ;; Keep other forms as-is
                  :else
                  (pr-str form))))))

(defn migrate-file
  "Migrate a file from Guardrails to Malli"
  [input-path output-path]
  (let [content (slurp input-path)
        ;; Process entire file including ns form
        processed (process-string content)]
    (spit output-path processed)
    {:status :success
     :file output-path}))