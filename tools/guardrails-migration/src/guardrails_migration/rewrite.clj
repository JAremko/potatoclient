(ns guardrails-migration.rewrite
  "Migration using rewrite-clj to properly preserve formatting"
  (:require [clojure.string :as str]
            [rewrite-clj.zip :as z]
            [rewrite-clj.node :as n]))

;; ============================================================================
;; Spec conversion helpers
;; ============================================================================

(defn convert-spec-node
  "Convert a Guardrails spec node to Malli spec node"
  [node]
  (cond
    ;; (? spec) -> [:maybe spec]
    (and (= :list (n/tag node))
         (= '? (n/sexpr (first (n/children node)))))
    (let [spec-node (second (n/children node))]
      (n/vector-node [(n/keyword-node :maybe)
                      (n/whitespace-node " ")
                      (convert-spec-node spec-node)]))
    
    ;; Predicate function int? -> :int
    (and (= :token (n/tag node))
         (symbol? (n/sexpr node))
         (str/ends-with? (name (n/sexpr node)) "?"))
    (let [sym (n/sexpr node)
          base-name (str/replace (name sym) #"\?$" "")]
      (case base-name
        "nil" (n/keyword-node :nil)
        "any" (n/keyword-node :any)
        "boolean" (n/keyword-node :boolean)
        "string" (n/keyword-node :string)
        "int" (n/keyword-node :int)
        "double" (n/keyword-node :double)
        "number" (n/keyword-node :number)
        "fn" (n/keyword-node :fn)
        "ifn" (n/keyword-node :ifn)
        "pos-int" node  ; Keep as predicate
        (n/keyword-node (keyword base-name))))
    
    ;; Vector specs - recursively convert children
    (= :vector (n/tag node))
    (n/vector-node
     (interpose (n/whitespace-node " ")
                (map convert-spec-node (n/children node))))
    
    ;; Already a keyword or complex spec
    :else node))

(defn parse-gspec
  "Parse gspec vector node into args and return value"
  [gspec-node]
  (when (and gspec-node (= :vector (n/tag gspec-node)))
    (let [children (n/children gspec-node)
          ;; Find the => separator
          arrow-idx (first (keep-indexed 
                           #(when (and (= :token (n/tag %2))
                                       (= '=> (n/sexpr %2)))
                              %1) 
                           children))]
      (when arrow-idx
        (let [;; Split around =>
              args-nodes (take arrow-idx children)
              ret-nodes (drop (inc arrow-idx) children)
              ;; Filter out whitespace and such-that clauses (| predicates)
              pipe-idx (first (keep-indexed 
                               #(when (and (= :token (n/tag %2))
                                           (= '| (n/sexpr %2)))
                                  %1) 
                               args-nodes))
              clean-args (if pipe-idx
                           (take pipe-idx args-nodes)
                           args-nodes)
              ;; Filter out just the spec nodes (skip whitespace)
              args-specs (filter #(not= :whitespace (n/tag %)) clean-args)
              ;; Handle variadic
              variadic-idx (first (keep-indexed 
                                   #(when (and (= :token (n/tag %2))
                                               (= '& (n/sexpr %2)))
                                      %1) 
                                   args-specs))
              regular-args (if variadic-idx
                             (take variadic-idx args-specs)
                             args-specs)
              ret-spec (first (filter #(not= :whitespace (n/tag %)) ret-nodes))]
          {:args (map convert-spec-node regular-args)
           :ret (when ret-spec (convert-spec-node ret-spec))})))))

(defn build-malli-schema-node
  "Build Malli schema node from parsed gspec"
  [{:keys [args ret]}]
  (when (and args ret)
    (n/vector-node
     (concat
      [(n/keyword-node :=>)
       (n/whitespace-node " ")
       (n/vector-node
        (concat [(n/keyword-node :cat)]
                (when (seq args)
                  (cons (n/whitespace-node " ")
                        (interpose (n/whitespace-node " ") args)))))]
      [(n/whitespace-node " ")
       ret]))))

;; ============================================================================
;; Zip operations
;; ============================================================================

(defn remove-guardrails-require
  "Remove Guardrails imports from namespace form"
  [zloc]
  (z/prewalk
   zloc
   (fn [loc]
     ;; Check if this is a require vector with Guardrails
     (and (z/vector? loc)
          (let [first-child (z/down loc)]
            (and first-child
                 (#{`com.fulcrologic.guardrails.malli.core
                    `com.fulcrologic.guardrails.core}
                  (z/sexpr first-child))))))
   z/remove))

(defn transform-defn
  "Transform a >defn form to defn with Malli metadata"
  [zloc]
  (let [;; Navigate through the form
        fn-name (-> zloc z/down z/right)
        ;; Check for docstring
        potential-doc (z/right fn-name)
        has-doc? (and potential-doc (string? (z/sexpr potential-doc)))
        after-doc (if has-doc? (z/right potential-doc) potential-doc)
        ;; Check for attr-map
        has-attrs? (and after-doc (map? (z/sexpr after-doc)))
        after-attrs (if has-attrs? (z/right after-doc) after-doc)
        ;; Check for args vector
        args (when after-attrs after-attrs)
        ;; Check for gspec (vector after args)
        potential-gspec (when args (z/right args))
        has-gspec? (and potential-gspec
                        (z/vector? potential-gspec)
                        (str/includes? (z/string potential-gspec) "=>"))
        gspec-node (when has-gspec? (z/node potential-gspec))
        parsed (when gspec-node (parse-gspec gspec-node))
        malli-schema (when parsed (build-malli-schema-node parsed))]
    
    (if malli-schema
      (let [;; Build metadata map
            metadata (if has-attrs?
                       ;; Add to existing map
                       (-> after-doc
                           (z/edit (fn [m]
                                     (assoc m :malli/schema 
                                            (n/sexpr malli-schema)))))
                       ;; Create new map
                       (if has-doc?
                         (-> potential-doc
                             (z/insert-right
                              (n/map-node
                               [(n/keyword-node :malli/schema)
                                (n/whitespace-node " ")
                                malli-schema])))
                         (-> fn-name
                             (z/insert-right
                              (n/map-node
                               [(n/keyword-node :malli/schema)
                                (n/whitespace-node " ")
                                malli-schema])))))
            ;; Change >defn to defn
            result (-> zloc
                       z/down
                       (z/edit (fn [sym]
                                 (case sym
                                   >defn 'defn
                                   >defn- 'defn-
                                   > 'defn
                                   sym)))
                       z/up)]
        ;; Remove the gspec
        (if has-gspec?
          (-> result
              (z/find-value z/next '=>)
              z/up
              z/remove)
          result))
      ;; No gspec, just change >defn to defn
      (-> zloc
          z/down
          (z/edit (fn [sym]
                    (case sym
                      >defn 'defn
                      >defn- 'defn-
                      > 'defn
                      sym)))
          z/up))))

(defn migrate-string
  "Migrate a string of Clojure code"
  [code-str]
  (let [zloc (z/of-string* code-str)]
    (-> zloc
        ;; First remove Guardrails from namespace
        (z/prewalk
         (fn [loc]
           (and (z/list? loc)
                (= 'ns (z/sexpr (z/down loc)))))
         remove-guardrails-require)
        ;; Then transform all >defn forms
        (z/prewalk
         (fn [loc]
           (and (z/list? loc)
                (let [first-elem (z/down loc)]
                  (and first-elem
                       (#{'>defn '>defn- '>} (z/sexpr first-elem))))))
         transform-defn)
        z/root-string)))

(defn migrate-file
  "Migrate a file from Guardrails to Malli"
  [input-path output-path]
  (let [content (slurp input-path)
        migrated (migrate-string content)]
    (spit output-path migrated)
    {:status :success
     :file output-path}))