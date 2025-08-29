(ns guardrails-migration.complete
  "Complete working migration tool using node API"
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
  [gspec-sexpr]
  (when (and (vector? gspec-sexpr)
             (some #(= '=> %) gspec-sexpr))
    (let [arrow-idx (.indexOf gspec-sexpr '=>)
          args (subvec gspec-sexpr 0 arrow-idx)
          ret (get gspec-sexpr (inc arrow-idx))
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
;; Node transformation
;; ============================================================================

(defn transform-defn-form
  "Transform a single >defn form node"
  [form-node]
  (let [children (n/children form-node)
        defn-sym (first children)
        
        ;; Check if it's a >defn
        is-gdefn? (and (= :token (n/tag defn-sym))
                      (#{'> '>defn '>defn-} (n/sexpr defn-sym)))]
    
    (if is-gdefn?
      ;; Extract parts, handling whitespace
      (let [nodes (filter #(not= :whitespace (n/tag %)) children)
            [_ fn-name & rest-nodes] nodes
            
            ;; Check for docstring
            has-docstring? (and (seq rest-nodes)
                               (= :string (n/tag (first rest-nodes))))
            after-doc (if has-docstring? (rest rest-nodes) rest-nodes)
            
            ;; Check for metadata
            has-metadata? (and (seq after-doc)
                              (= :map (n/tag (first after-doc))))
            after-meta (if has-metadata? (rest after-doc) after-doc)
            
            ;; Check if multi-arity
            is-multi? (and (seq after-meta)
                          (= :list (n/tag (first after-meta))))
            
            ;; Process based on arity type
            result (if is-multi?
                    ;; Multi-arity
                    (let [arity-forms after-meta
                          gspecs (atom [])
                          ;; Process each arity
                          new-arities (map (fn [arity-node]
                                            (if (= :list (n/tag arity-node))
                                              (let [arity-children (n/children arity-node)
                                                    arity-nodes (filter #(not= :whitespace (n/tag %)) 
                                                                       arity-children)
                                                    [args & body] arity-nodes]
                                                (if (and (seq body)
                                                        (= :vector (n/tag (first body)))
                                                        (str/includes? (n/string (first body)) "=>"))
                                                  ;; Has gspec
                                                  (let [gspec (first body)
                                                        parsed (parse-gspec (n/sexpr gspec))]
                                                    (swap! gspecs conj parsed)
                                                    ;; Return arity without gspec
                                                    (n/list-node
                                                     (filter #(not= gspec %) arity-children)))
                                                  ;; No gspec
                                                  arity-node))
                                              arity-node))
                                          after-meta)
                          ;; Build function schema
                          schemas (seq @gspecs)
                          malli-schema (when schemas
                                        (if (= 1 (count schemas))
                                          (build-malli-schema (first schemas))
                                          (into [:function]
                                               (map build-malli-schema schemas))))]
                      {:new-arities new-arities
                       :malli-schema malli-schema})
                    
                    ;; Single arity
                    (let [[args & body] after-meta]
                      (if (and (seq body)
                              (= :vector (n/tag (first body)))
                              (str/includes? (n/string (first body)) "=>"))
                        ;; Has gspec
                        (let [gspec (first body)
                              parsed (parse-gspec (n/sexpr gspec))
                              malli-schema (build-malli-schema parsed)]
                          {:args args
                           :body (rest body)
                           :malli-schema malli-schema})
                        ;; No gspec
                        {:args args
                         :body body
                         :malli-schema nil})))
            
            ;; Build new form
            new-defn (n/token-node (case (n/sexpr defn-sym)
                                    > 'defn
                                    >defn 'defn
                                    >defn- 'defn-))
            
            ;; Find whitespace positions
            ws-indices (keep-indexed #(when (= :whitespace (n/tag %2)) %1) children)
            ws-nodes (map #(nth children %) ws-indices)
            [ws1 ws2 ws3 ws4 ws5] ws-nodes
            
            ;; Build metadata
            meta-node (when (:malli-schema result)
                       (if has-metadata?
                         ;; Merge with existing
                         (n/map-node
                          (concat (n/children (first after-doc))
                                  [(n/whitespace-node " ")
                                   (n/keyword-node :malli/schema)
                                   (n/whitespace-node " ")
                                   (n/coerce (:malli-schema result))]))
                         ;; Create new
                         (n/map-node
                          [(n/keyword-node :malli/schema)
                           (n/whitespace-node " ")
                           (n/coerce (:malli-schema result))])))
            
            ;; Assemble new children
            new-children (concat
                         [new-defn]
                         (when ws1 [ws1])
                         [fn-name]
                         (when has-docstring?
                           [(when ws2 ws2) (first rest-nodes)])
                         (when (or meta-node has-metadata?)
                           [(when ws3 ws3) (or meta-node (first after-doc))])
                         (if is-multi?
                           ;; Multi-arity bodies
                           (interleave (:new-arities result) 
                                      (repeat (n/newline-node "\n  ")))
                           ;; Single arity body
                           (concat
                            [(when ws4 ws4) (:args result)]
                            (when ws5 [ws5])
                            (:body result))))]
        
        (n/list-node new-children))
      ;; Not a >defn, return unchanged
      form-node)))

(defn clean-ns-form
  "Remove Guardrails imports from namespace form"
  [ns-form]
  (if (and (= :list (n/tag ns-form))
           (= 'ns (n/sexpr (first (n/children ns-form)))))
    (n/list-node
     (mapcat (fn [child]
               (if (and (= :list (n/tag child))
                       (= :require (n/sexpr (first (filter #(not= :whitespace (n/tag %))
                                                           (n/children child))))))
                 ;; Process :require clause
                 (let [require-children (n/children child)
                       cleaned (filter (fn [node]
                                        (not (and (= :vector (n/tag node))
                                                 (let [first-elem (first (filter #(not= :whitespace (n/tag %))
                                                                                (n/children node)))]
                                                   (and first-elem
                                                        (#{`com.fulcrologic.guardrails.malli.core
                                                           `com.fulcrologic.guardrails.core}
                                                         (n/sexpr first-elem)))))))
                                      require-children)]
                   (if (> (count (filter #(= :vector (n/tag %)) cleaned)) 0)
                     [(n/list-node cleaned)]
                     []))
                 ;; Keep other clauses
                 [child]))
             (n/children ns-form)))
    ns-form))

;; ============================================================================
;; File processing
;; ============================================================================

(defn migrate-string
  "Migrate a string of Clojure code"
  [code-str]
  (let [forms-node (p/parse-string-all code-str)
        forms (n/children forms-node)
        transformed (map (fn [node]
                          (cond
                            ;; Clean namespace form
                            (and (= :list (n/tag node))
                                 (= 'ns (n/sexpr (first (n/children node)))))
                            (clean-ns-form node)
                            
                            ;; Transform >defn forms
                            (and (= :list (n/tag node))
                                 (let [first-child (first (n/children node))]
                                   (and (= :token (n/tag first-child))
                                        (#{'> '>defn '>defn-} (n/sexpr first-child)))))
                            (transform-defn-form node)
                            
                            ;; Keep everything else
                            :else node))
                        forms)]
    (str/join "" (map n/string transformed))))

(defn migrate-file
  "Migrate a file from Guardrails to Malli"
  [input-path output-path]
  (let [content (slurp input-path)
        migrated (migrate-string content)]
    (spit output-path migrated)
    {:status :success
     :file output-path}))