(ns guardrails-migration.final
  "Final working migration using nodes and zippers appropriately"
  (:require [clojure.string :as str]
            [rewrite-clj.parser :as p]
            [rewrite-clj.node :as n]
            [rewrite-clj.zip :as z]))

;; ============================================================================
;; Spec conversion helpers
;; ============================================================================

(defn convert-spec-sexpr
  "Convert a Guardrails spec sexpr to Malli spec sexpr"
  [spec]
  (cond
    ;; (? spec) -> [:maybe spec]
    (and (list? spec) (= '? (first spec)))
    [:maybe (convert-spec-sexpr (second spec))]
    
    ;; Predicate function int? -> :int
    (and (symbol? spec) 
         (str/ends-with? (str spec) "?")
         (not= spec 'pos-int?)  ; Keep some predicates as-is
         (not= spec 'nat-int?)
         (not= spec 'neg-int?)
         (not= spec 'pos?)
         (not= spec 'neg?)
         (not= spec 'zero?))
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
    (mapv convert-spec-sexpr spec)
    
    ;; Already keyword or complex spec
    :else spec))

(defn parse-gspec-sexpr
  "Parse gspec vector sexpr into args and return value"
  [gspec]
  (when (and (vector? gspec)
             (some #(= '=> %) gspec))
    (let [arrow-idx (.indexOf gspec '=>)
          args (subvec gspec 0 arrow-idx)
          ret (get gspec (inc arrow-idx))
          ;; Filter out such-that clauses (| predicates)
          pipe-idx (.indexOf args '|)
          clean-args (if (>= pipe-idx 0)
                       (subvec args 0 pipe-idx)
                       args)
          ;; Filter out & for variadic
          final-args (filterv #(not= '& %) clean-args)]
      {:args (mapv convert-spec-sexpr final-args)
       :ret (convert-spec-sexpr ret)})))

(defn build-malli-schema-sexpr
  "Build Malli schema sexpr from parsed gspec"
  [{:keys [args ret]}]
  (when (or args ret)
    [:=> (into [:cat] args) (or ret :any)]))

;; ============================================================================
;; Transformation functions
;; ============================================================================

(defn remove-guardrails-require
  "Remove Guardrails require from ns zipper"
  [zloc]
  (-> zloc
      (z/prewalk
       ;; Find vectors in :require clause
       (fn [loc]
         (and (z/vector? loc)
              (let [first-elem (z/down loc)]
                (and first-elem
                     (#{`com.fulcrologic.guardrails.malli.core
                        `com.fulcrologic.guardrails.core}
                      (z/sexpr first-elem))))))
       z/remove)))

(defn transform-single-defn
  "Transform a single >defn form"
  [zloc]
  ;; Change >defn to defn
  (let [zloc (-> zloc
                 z/down
                 (z/edit (fn [sym]
                          (case sym
                            >defn 'defn
                            >defn- 'defn-
                            > 'defn)))
                 z/up)]
    ;; Now process the function
    (let [;; Get function name position
          name-loc (-> zloc z/down z/right)
          ;; Check for docstring
          after-name (z/right name-loc)
          has-docstring? (string? (z/sexpr after-name))
          after-doc (if has-docstring? 
                     (z/right after-name) 
                     after-name)
          ;; Check for existing metadata
          has-metadata? (map? (z/sexpr after-doc))
          after-meta (if has-metadata?
                      (z/right after-doc)
                      after-doc)]
      
      ;; Check if it's multi-arity
      (if (z/list? after-meta)
        ;; Multi-arity - collect gspecs from each arity
        (let [gspecs (atom [])
              ;; Process each arity form
              zloc-processed 
              (z/postwalk
               zloc
               ;; Select arity lists that are direct children
               (fn [loc]
                 (and (z/list? loc)
                      ;; Make sure it's an arity form (starts with vector)
                      (z/vector? (z/down loc))
                      ;; And it's at the right level (parent is the defn)
                      (let [parent (z/up loc)]
                        (and parent
                             (= 'defn (z/sexpr (z/down parent)))))))
               ;; Process each arity
               (fn [loc]
                 (let [args (z/down loc)
                       after-args (z/right args)]
                   (if (and (z/vector? after-args)
                           (str/includes? (z/string after-args) "=>"))
                     (let [gspec (z/sexpr after-args)
                           parsed (parse-gspec-sexpr gspec)]
                       (swap! gspecs conj parsed)
                       ;; Remove the gspec
                       (z/up (z/remove after-args)))
                     loc))))
              ;; Build function schema for multi-arity
              schemas (seq @gspecs)
              malli-schema (when schemas
                            (if (= 1 (count schemas))
                              (build-malli-schema-sexpr (first schemas))
                              (into [:function]
                                   (map build-malli-schema-sexpr schemas))))]
          ;; Add metadata if we have schema
          (if malli-schema
            (let [insert-point (if has-docstring?
                                after-name
                                name-loc)]
              (if has-metadata?
                ;; Merge into existing metadata
                (-> zloc-processed
                    z/down z/right ; at name
                    (z/find-value z/right map?) ; find the metadata map
                    (z/edit assoc :malli/schema malli-schema)
                    z/up)
                ;; Insert new metadata
                (-> zloc-processed
                    z/down z/right ; at name
                    (z/insert-right {:malli/schema malli-schema})
                    z/up)))
            zloc-processed))
        
        ;; Single arity
        (let [args after-meta
              after-args (z/right args)]
          (if (and (z/vector? after-args)
                  (str/includes? (z/string after-args) "=>"))
            (let [gspec (z/sexpr after-args)
                  parsed (parse-gspec-sexpr gspec)
                  malli-schema (build-malli-schema-sexpr parsed)
                  ;; Remove gspec
                  zloc-no-gspec (z/up (z/remove after-args))]
              ;; Add metadata if we have schema
              (if malli-schema
                (let [insert-point (if has-docstring?
                                    (-> zloc-no-gspec z/down z/right z/right)
                                    (-> zloc-no-gspec z/down z/right))]
                  (if has-metadata?
                    ;; Merge into existing metadata
                    (-> zloc-no-gspec
                        z/down z/right ; at name
                        (z/find-value z/right map?) ; find the metadata map
                        (z/edit assoc :malli/schema malli-schema)
                        z/up)
                    ;; Insert new metadata
                    (-> zloc-no-gspec
                        z/down z/right ; at name
                        (z/insert-right {:malli/schema malli-schema})
                        z/up)))
                zloc-no-gspec))
            ;; No gspec, just return with >defn changed
            zloc))))))

(defn migrate-string
  "Migrate a string of Clojure code"
  [code-str]
  (-> code-str
      z/of-string*
      ;; First remove Guardrails requires
      remove-guardrails-require
      ;; Then transform all >defn forms
      (z/prewalk
       ;; Find >defn forms
       (fn [loc]
         (and (z/list? loc)
              (let [first-elem (z/down loc)]
                (and first-elem
                     (#{'>defn '>defn- '>} (z/sexpr first-elem))))))
       ;; Transform them
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