(ns guardrails-migration.working
  "Simplified migration that actually works"
  (:require [clojure.string :as str]
            [rewrite-clj.parser :as p]
            [rewrite-clj.node :as n]
            [rewrite-clj.zip :as z]))

;; ============================================================================
;; Spec conversion helpers
;; ============================================================================

(defn convert-spec
  "Convert a Guardrails spec to Malli spec"
  [spec]
  (cond
    ;; (? spec) -> [:maybe spec]
    (and (list? spec) (= '? (first spec)))
    [:maybe (convert-spec (second spec))]
    
    ;; Predicate function int? -> :int
    (and (symbol? spec) 
         (str/ends-with? (str spec) "?")
         ;; Keep some predicates as-is
         (not (#{`pos-int? `nat-int? `neg-int? `pos? `neg? `zero?} spec)))
    (let [s (str spec)
          base-name (str/replace s #"\?$" "")]
      (case base-name
        "nil" :nil
        "any" :any
        "boolean" :boolean
        "string" :string
        "int" :int
        "double" :double
        "number" :number
        "fn" :fn
        "ifn" :ifn
        (keyword base-name)))
    
    ;; Vector specs - recursively convert
    (vector? spec)
    (mapv convert-spec spec)
    
    ;; Already keyword or complex spec
    :else spec))

(defn parse-gspec
  "Parse gspec vector into args and return value"
  [gspec-vec]
  (when (and (vector? gspec-vec)
             (some #(= '=> %) gspec-vec))
    (let [arrow-idx (.indexOf gspec-vec '=>)
          args (subvec gspec-vec 0 arrow-idx)
          ret (get gspec-vec (inc arrow-idx))
          ;; Filter out such-that clauses (| predicates)
          pipe-idx (.indexOf args '|)
          clean-args (if (>= pipe-idx 0)
                       (subvec args 0 pipe-idx)
                       args)
          ;; Filter out & for variadic
          final-args (filterv #(not= '& %) clean-args)]
      {:args (mapv convert-spec final-args)
       :ret (convert-spec ret)})))

(defn build-malli-schema
  "Build Malli schema from parsed gspec"
  [{:keys [args ret]}]
  (when (or args ret)
    [:=> (into [:cat] (or args [])) (or ret :any)]))

;; ============================================================================
;; Transformation using zippers
;; ============================================================================

(defn remove-guardrails-requires
  "Remove Guardrails imports from namespace"
  [zloc]
  (z/prewalk
   zloc
   ;; Find vectors in :require
   (fn [loc]
     (and (z/vector? loc)
          (let [first-elem (z/down loc)]
            (and first-elem
                 (#{`com.fulcrologic.guardrails.malli.core
                    `com.fulcrologic.guardrails.core}
                  (z/sexpr first-elem))))))
   z/remove))

(defn transform-single-defn
  "Transform a single >defn to defn with metadata"
  [zloc]
  ;; First change >defn to defn
  (let [zloc (-> zloc
                 z/down
                 (z/edit (fn [sym]
                          (case sym
                            > 'defn
                            >defn 'defn
                            >defn- 'defn-)))
                 z/up)]
    ;; Navigate to find the gspec
    (loop [loc (-> zloc z/down z/right) ; Start at function name
           state :name]
      (cond
        ;; End of form
        (z/end? loc) zloc
        
        ;; Found a vector that could be gspec
        (and (z/vector? loc)
             (let [s (z/string loc)]
               (str/includes? s "=>")))
        (let [gspec (z/sexpr loc)
              parsed (parse-gspec gspec)
              schema (build-malli-schema parsed)]
          (if schema
            ;; Remove gspec and add metadata
            (let [;; Remove the gspec
                  zloc-no-gspec (z/subedit-> zloc
                                             z/down ; to defn  
                                             (z/find z/right #(and (z/vector? %)
                                                                  (str/includes? (z/string %) "=>")))
                                             z/remove)
                  ;; Add metadata after the name
                  zloc-with-meta (z/subedit-> zloc-no-gspec
                                              z/down ; to defn
                                              z/right ; to name
                                              (z/insert-right {:malli/schema schema}))]
              zloc-with-meta)
            zloc))
        
        ;; Continue searching
        :else (recur (z/right loc) state)))))

(defn migrate-string
  "Migrate a string of Clojure code"
  [code-str]
  (-> code-str
      z/of-string*
      remove-guardrails-requires
      (z/prewalk
       ;; Find >defn forms
       (fn [loc]
         (and (z/list? loc)
              (let [first-elem (z/down loc)]
                (and first-elem
                     (#{'> '>defn '>defn-} (z/sexpr first-elem))))))
       transform-single-defn)
      z/root-string))

(defn migrate-file
  "Migrate a file from Guardrails to Malli"
  [input-path output-path]
  (let [content (slurp input-path)
        migrated (migrate-string content)]
    (spit output-path migrated)
    {:status :success
     :file output-path}))