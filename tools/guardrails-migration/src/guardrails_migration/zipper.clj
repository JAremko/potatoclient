(ns guardrails-migration.zipper
  "Migration using rewrite-clj zipper API for cleaner code"
  (:require [clojure.string :as str]
            [rewrite-clj.zip :as z]
            [rewrite-clj.node :as n]))

;; ============================================================================
;; Spec conversion helpers
;; ============================================================================

(defn convert-spec
  "Convert a Guardrails spec to Malli spec"
  [spec-sexpr]
  (cond
    ;; (? spec) -> [:maybe spec]
    (and (list? spec-sexpr) (= '? (first spec-sexpr)))
    [:maybe (convert-spec (second spec-sexpr))]
    
    ;; Predicate function int? -> :int
    (and (symbol? spec-sexpr) 
         (str/ends-with? (str spec-sexpr) "?"))
    (let [s (str spec-sexpr)
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
        ;; Keep pos-int? and similar as predicates
        ("pos-int" "nat-int" "neg-int") spec-sexpr
        ;; Convert others to keywords
        (keyword base-name)))
    
    ;; Vector specs - recursively convert
    (vector? spec-sexpr)
    (mapv convert-spec spec-sexpr)
    
    ;; Already keyword or Malli spec
    :else spec-sexpr))

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
  (when (and args ret)
    [:=> (into [:cat] args) ret]))

(defn build-function-schema
  "Build function schema for multi-arity"
  [gspecs]
  (when (seq gspecs)
    (if (= 1 (count gspecs))
      (build-malli-schema (first gspecs))
      (into [:function]
            (map build-malli-schema gspecs)))))

;; ============================================================================
;; Zipper navigation and transformation
;; ============================================================================

(defn find-and-remove-guardrails-require
  "Find and remove Guardrails require from namespace"
  [zloc]
  (if-let [ns-loc (z/find zloc z/next #(and (z/list? %)
                                             (= 'ns (z/sexpr (z/down %)))))]
    ;; Find :require clause
    (if-let [require-loc (z/find ns-loc z/next 
                                 #(and (z/list? %)
                                       (= :require (z/sexpr (z/down %)))))]
      ;; Find and remove Guardrails vectors
      (loop [loc (z/down require-loc)]
        (if (z/end? loc)
          zloc
          (if (and (z/vector? loc)
                   (let [first-elem (z/down loc)]
                     (and first-elem
                          (#{`com.fulcrologic.guardrails.malli.core
                             `com.fulcrologic.guardrails.core}
                           (z/sexpr first-elem)))))
            (recur (z/remove loc))
            (recur (z/next loc)))))
      zloc)
    zloc))

(defn transform-defn
  "Transform a >defn at zloc to defn with Malli metadata"
  [zloc]
  (let [;; Change >defn to defn
        defn-loc (z/down zloc)
        new-defn (case (z/sexpr defn-loc)
                  >defn 'defn
                  >defn- 'defn-
                  > 'defn)
        zloc (-> zloc z/down (z/replace new-defn) z/up)
        
        ;; Navigate to function name
        name-loc (z/right (z/down zloc))
        
        ;; Check for docstring
        after-name (z/right name-loc)
        has-docstring? (string? (z/sexpr after-name))
        after-doc (if has-docstring? (z/right after-name) after-name)
        
        ;; Check for attr-map
        has-metadata? (map? (z/sexpr after-doc))
        after-meta (if has-metadata? (z/right after-doc) after-doc)
        
        ;; Now we're at args or multi-arity
        body-loc after-meta]
    
    ;; Check if multi-arity
    (if (z/list? body-loc)
      ;; Multi-arity function
      (let [;; Process each arity
            arities (loop [loc body-loc
                          gspecs []
                          processed zloc]
                     (if (not (z/list? loc))
                       {:gspecs gspecs :zloc processed}
                       (let [;; Navigate into arity
                             args-loc (z/down loc)
                             after-args (z/right args-loc)
                             ;; Check for gspec
                             has-gspec? (and (z/vector? after-args)
                                           (str/includes? (z/string after-args) "=>"))
                             gspec (when has-gspec? 
                                    (parse-gspec (z/sexpr after-args)))
                             ;; Remove gspec if present
                             new-processed (if has-gspec?
                                            (z/up (z/remove after-args))
                                            processed)]
                         (recur (z/right loc)
                               (if gspec (conj gspecs gspec) gspecs)
                               new-processed))))
            malli-schema (build-function-schema (:gspecs arities))]
        
        ;; Add metadata if we have a schema
        (if malli-schema
          (let [;; Navigate to insertion point
                insert-loc (if has-docstring?
                            after-name
                            name-loc)]
            (if has-metadata?
              ;; Add to existing metadata
              (z/up (z/assoc after-doc :malli/schema malli-schema))
              ;; Insert new metadata
              (z/up (z/insert-right insert-loc {:malli/schema malli-schema}))))
          (:zloc arities)))
      
      ;; Single arity function
      (let [args-loc body-loc
            after-args (z/right args-loc)
            ;; Check for gspec
            has-gspec? (and (z/vector? after-args)
                           (str/includes? (z/string after-args) "=>"))
            gspec (when has-gspec? 
                   (parse-gspec (z/sexpr after-args)))
            malli-schema (when gspec (build-malli-schema gspec))
            ;; Remove gspec if present
            zloc-no-gspec (if has-gspec?
                           (z/up (z/remove after-args))
                           zloc)]
        
        ;; Add metadata if we have a schema
        (if malli-schema
          (let [;; Navigate to insertion point
                insert-loc (if has-docstring?
                            (-> zloc-no-gspec z/down z/right z/right)
                            (-> zloc-no-gspec z/down z/right))]
            (if has-metadata?
              ;; Add to existing metadata
              (let [meta-loc (if has-docstring?
                              (-> zloc-no-gspec z/down z/right z/right z/right)
                              (-> zloc-no-gspec z/down z/right z/right))]
                (z/up (z/assoc meta-loc :malli/schema malli-schema)))
              ;; Insert new metadata
              (z/up (z/insert-right insert-loc {:malli/schema malli-schema}))))
          zloc-no-gspec)))))

(defn migrate-string
  "Migrate a string of Clojure code"
  [code-str]
  (-> code-str
      z/of-string*
      find-and-remove-guardrails-require
      (z/prewalk 
       #(and (z/list? %)
             (#{'>defn '>defn- '>} (z/sexpr (z/down %))))
       transform-defn)
      z/root-string))

(defn migrate-file
  "Migrate a file from Guardrails to Malli"
  [input-path output-path]
  (let [content (slurp input-path)
        migrated (migrate-string content)]
    (spit output-path migrated)
    {:status :success
     :file output-path}))