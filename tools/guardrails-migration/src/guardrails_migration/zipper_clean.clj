(ns guardrails-migration.zipper-clean
  "Clean zipper-based migration using what we learned"
  (:require [clojure.string :as str]
            [rewrite-clj.zip :as z]
            [rewrite-clj.node :as n]))

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
;; Zipper-based transformation
;; ============================================================================

(defn remove-guardrails-requires
  "Remove Guardrails imports from namespace"
  [zloc]
  (z/prewalk
   zloc
   ;; Find vectors in :require that have Guardrails
   (fn [loc]
     (and (z/vector? loc)
          (let [first-elem (z/down loc)]
            (and first-elem
                 (#{`com.fulcrologic.guardrails.malli.core
                    `com.fulcrologic.guardrails.core}
                  (z/sexpr first-elem))))))
   z/remove))

(defn process-single-arity
  "Process a single arity list form"
  [arity-loc]
  (if (z/list? arity-loc)
    (let [;; Go into the arity
          args-loc (z/down arity-loc)
          after-args (z/right args-loc)]
      (if (and (z/vector? after-args)
               (str/includes? (z/string after-args) "=>"))
        ;; Has gspec - extract and remove it
        {:gspec (z/sexpr after-args)
         :arity (z/up (z/remove after-args))}
        ;; No gspec
        {:gspec nil
         :arity arity-loc}))
    {:gspec nil :arity arity-loc}))

(defn transform-defn
  "Transform a >defn form to defn with Malli metadata"
  [zloc]
  ;; First change >defn to defn
  (let [defn-sym (z/sexpr (z/down zloc))
        new-defn-sym (case defn-sym
                      > 'defn
                      >defn 'defn
                      >defn- 'defn-)
        zloc (z/subedit-> zloc
                         z/down
                         (z/replace new-defn-sym))]
    
    ;; Navigate to analyze structure
    (let [name-loc (-> zloc z/down z/right)
          after-name (z/right name-loc)
          
          ;; Check what comes after name
          has-docstring? (string? (z/sexpr after-name))
          after-doc (if has-docstring? (z/right after-name) after-name)
          
          has-metadata? (map? (z/sexpr after-doc))
          after-meta (if has-metadata? (z/right after-doc) after-doc)
          
          ;; Check if multi-arity
          is-multi? (z/list? after-meta)]
      
      (if is-multi?
        ;; Multi-arity - process each arity
        (let [;; Collect all gspecs
              gspecs (atom [])
              ;; Process all arities
              processed (z/prewalk
                        zloc
                        ;; Find arity lists with gspecs
                        (fn [loc]
                          (and (z/list? loc)
                               ;; Is direct child of defn (arity)
                               (let [parent (z/up loc)]
                                 (and parent
                                      (= new-defn-sym (z/sexpr (z/down parent)))))
                               ;; Has args vector as first child
                               (z/vector? (z/down loc))
                               ;; Has gspec as second child
                               (let [second (-> loc z/down z/right)]
                                 (and (z/vector? second)
                                      (str/includes? (z/string second) "=>")))))
                        ;; Remove gspec and collect it
                        (fn [loc]
                          (let [gspec (-> loc z/down z/right)]
                            (swap! gspecs conj (parse-gspec (z/sexpr gspec)))
                            (z/subedit-> loc
                                        z/down ; to args
                                        z/right ; to gspec
                                        z/remove))))
              
              ;; Build function schema
              schemas (seq @gspecs)
              malli-schema (when schemas
                            (if (= 1 (count schemas))
                              (build-malli-schema (first schemas))
                              (into [:function]
                                   (map build-malli-schema schemas))))]
          
          ;; Add metadata if we have schema
          (if malli-schema
            (if has-metadata?
              ;; Merge with existing metadata
              (z/subedit-> processed
                          z/down ; to defn
                          z/right ; to name
                          (z/find z/right z/map?) ; find metadata
                          (z/edit assoc :malli/schema malli-schema))
              ;; Insert new metadata
              (if has-docstring?
                (z/subedit-> processed
                            z/down ; to defn
                            z/right ; to name
                            z/right ; to docstring
                            (z/insert-right {:malli/schema malli-schema}))
                (z/subedit-> processed
                            z/down ; to defn
                            z/right ; to name
                            (z/insert-right {:malli/schema malli-schema}))))
            processed))
        
        ;; Single arity
        (let [;; Check for gspec after args
              args-loc after-meta
              after-args (z/right args-loc)]
          (if (and (z/vector? after-args)
                  (str/includes? (z/string after-args) "=>"))
            ;; Has gspec
            (let [gspec (parse-gspec (z/sexpr after-args))
                  malli-schema (build-malli-schema gspec)
                  ;; Remove gspec
                  zloc-no-gspec (z/subedit-> zloc
                                             (z/find z/next #(and (z/vector? %)
                                                                 (str/includes? (z/string %) "=>")))
                                             z/remove)]
              ;; Add metadata
              (if malli-schema
                (if has-metadata?
                  ;; Merge with existing
                  (z/subedit-> zloc-no-gspec
                              z/down
                              z/right
                              (z/find z/right z/map?)
                              (z/edit assoc :malli/schema malli-schema))
                  ;; Insert new
                  (if has-docstring?
                    (z/subedit-> zloc-no-gspec
                                z/down ; to defn
                                z/right ; to name
                                z/right ; to docstring
                                (z/insert-right {:malli/schema malli-schema}))
                    (z/subedit-> zloc-no-gspec
                                z/down ; to defn
                                z/right ; to name
                                (z/insert-right {:malli/schema malli-schema}))))
                zloc-no-gspec))
            ;; No gspec, just return with defn changed
            zloc))))))

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