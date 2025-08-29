(ns guardrails-migration.robust
  "Robust migration using rewrite-clj node API"
  (:require [clojure.string :as str]
            [rewrite-clj.parser :as p]
            [rewrite-clj.node :as n]
            [rewrite-clj.zip :as z]))

;; ============================================================================
;; Spec conversion using nodes
;; ============================================================================

(defn convert-spec-node
  "Convert a Guardrails spec node to Malli spec node"
  [node]
  (cond
    ;; nil - no node
    (nil? node) nil
    
    ;; (? spec) -> [:maybe spec]
    (and (= :list (n/tag node))
         (let [children (n/children node)]
           (and (seq children)
                (= :token (n/tag (first children)))
                (= '? (n/sexpr (first children))))))
    (let [children (n/children node)
          spec-node (nth children 2 nil)] ; skip ? and whitespace
      (n/vector-node
       [(n/keyword-node :maybe)
        (n/whitespace-node " ")
        (convert-spec-node spec-node)]))
    
    ;; Predicate function int? -> :int
    (and (= :token (n/tag node))
         (symbol? (n/sexpr node))
         (str/ends-with? (str (n/sexpr node)) "?"))
    (let [sym (n/sexpr node)
          sym-str (str sym)
          base-name (str/replace sym-str #"\?$" "")]
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
        ;; Keep pos-int? and other special predicates as-is
        ("pos-int" "nat-int" "neg-int" "pos?" "neg?" "zero?") node
        ;; Try to convert others to keywords
        (n/keyword-node (keyword base-name))))
    
    ;; [:sequential ...] and other Malli specs - keep as-is
    (and (= :vector (n/tag node))
         (let [children (n/children node)]
           (and (seq children)
                (= :token (n/tag (first children)))
                (keyword? (n/sexpr (first children)))
                (#{:sequential :vector :map :map-of :fn :maybe :*} 
                 (n/sexpr (first children))))))
    node
    
    ;; Regular vectors - recursively convert
    (= :vector (n/tag node))
    (n/vector-node
     (map convert-spec-node (n/children node)))
    
    ;; Keywords - keep as-is
    (= :token (n/tag node))
    (if (keyword? (n/sexpr node))
      node
      node)
    
    ;; Default - keep as-is
    :else node))

(defn extract-gspec-from-body
  "Extract gspec vector from function body, returns [gspec-node remaining-body]"
  [body-nodes]
  ;; Skip whitespace at start
  (let [non-ws-body (drop-while #(= :whitespace (n/tag %)) body-nodes)]
    (if (and (seq non-ws-body)
             (= :vector (n/tag (first non-ws-body)))
             ;; Check if it contains =>
             (some #(and (= :token (n/tag %))
                        (= '=> (n/sexpr %)))
                   (n/children (first non-ws-body))))
      [(first non-ws-body) (rest non-ws-body)]
      [nil body-nodes])))

(defn parse-gspec-node
  "Parse gspec vector node into args and return specs"
  [gspec-node]
  (when gspec-node
    (let [children (n/children gspec-node)
          ;; Find => position
          arrow-idx (first (keep-indexed 
                           #(when (and (= :token (n/tag %2))
                                      (= '=> (n/sexpr %2)))
                              %1) 
                           children))]
      (when arrow-idx
        (let [;; Split around =>
              before-arrow (take arrow-idx children)
              after-arrow (drop (inc arrow-idx) children)
              ;; Find | (such-that) position if any
              pipe-idx (first (keep-indexed 
                              #(when (and (= :token (n/tag %2))
                                         (= '| (n/sexpr %2)))
                                 %1) 
                              before-arrow))
              ;; Get arg specs (before | if present)
              arg-nodes (if pipe-idx
                         (take pipe-idx before-arrow)
                         before-arrow)
              ;; Filter out whitespace and & for args
              arg-specs (filter #(and (not= :whitespace (n/tag %))
                                     (not (and (= :token (n/tag %))
                                              (= '& (n/sexpr %))))) 
                               arg-nodes)
              ;; Get return spec (first non-whitespace after =>)
              ret-spec (first (filter #(not= :whitespace (n/tag %)) 
                                      after-arrow))]
          {:args (map convert-spec-node arg-specs)
           :ret (convert-spec-node ret-spec)})))))

(defn build-malli-schema-node
  "Build Malli schema node from parsed gspec"
  [{:keys [args ret]}]
  (when (or args ret)
    (let [cat-children (concat [(n/keyword-node :cat)]
                               (when (seq args)
                                 (cons (n/whitespace-node " ")
                                       (interpose (n/whitespace-node " ") args))))]
      (n/vector-node
       [(n/keyword-node :=>)
        (n/whitespace-node " ")
        (n/vector-node cat-children)
        (n/whitespace-node " ")
        (or ret (n/keyword-node :any))]))))

(defn build-malli-function-schema
  "Build function schema for multi-arity"
  [gspec-results]
  (when (seq gspec-results)
    (if (= 1 (count gspec-results))
      ;; Single arity
      (build-malli-schema-node (first gspec-results))
      ;; Multi-arity
      (n/vector-node
       (concat [(n/keyword-node :function)]
               (interpose (n/newline-node "\n                  ")
                         (map build-malli-schema-node gspec-results)))))))

;; ============================================================================
;; Node transformation
;; ============================================================================

(defn transform-defn-node
  "Transform a >defn list node to defn with metadata"
  [defn-node]
  (when (and (= :list (n/tag defn-node))
             (let [first-child (first (n/children defn-node))]
               (and (= :token (n/tag first-child))
                    (#{'>defn '>defn- '>} (n/sexpr first-child)))))
    (let [children (n/children defn-node)
          ;; Change >defn to defn
          new-defn-sym (case (n/sexpr (first children))
                        >defn (n/token-node 'defn)
                        >defn- (n/token-node 'defn-)
                        > (n/token-node 'defn))
          ;; Skip to function name (skip whitespace)
          [ws-before-name name-node & after-name] (rest children)
          ;; Check for docstring
          [potential-doc & after-doc] (drop-while #(= :whitespace (n/tag %)) after-name)
          has-docstring? (and potential-doc (= :string (n/tag potential-doc)))
          ;; Handle metadata map
          nodes-after-doc (if has-docstring? 
                            (drop-while #(= :whitespace (n/tag %)) after-doc)
                            (drop-while #(= :whitespace (n/tag %)) after-name))
          [potential-meta & after-meta] nodes-after-doc
          has-metadata? (and potential-meta (= :map (n/tag potential-meta)))
          ;; Get to the body
          body-start (if has-metadata?
                       after-meta
                       (if has-docstring?
                         (concat [(first (filter #(= :whitespace (n/tag %)) after-doc))] 
                                 nodes-after-doc)
                         after-name))]
      
      ;; Handle single vs multi-arity
      (if (= :list (n/tag (first (drop-while #(= :whitespace (n/tag %)) body-start))))
        ;; Multi-arity
        (let [arity-forms (filter #(= :list (n/tag %)) body-start)
              ws-between (filter #(= :whitespace (n/tag %)) body-start)
              ;; Process each arity
              processed-arities (map (fn [arity-node]
                                      (let [arity-children (n/children arity-node)
                                            [args-vec & body] (filter #(not= :whitespace (n/tag %)) 
                                                                     arity-children)
                                            [gspec remaining] (extract-gspec-from-body body)]
                                        {:gspec (parse-gspec-node gspec)
                                         :new-arity (n/list-node
                                                    (if gspec
                                                      ;; Remove gspec from body
                                                      (filter #(not= gspec %) arity-children)
                                                      arity-children))}))
                                    arity-forms)
              gspecs (keep :gspec processed-arities)
              new-arities (map :new-arity processed-arities)
              malli-schema (build-malli-function-schema gspecs)]
          ;; Rebuild the defn
          (n/list-node
           (concat
            [new-defn-sym ws-before-name name-node]
            (when has-docstring?
              (concat (take-while #(= :whitespace (n/tag %)) after-name)
                      [potential-doc]))
            (when (or has-metadata? malli-schema)
              (let [ws (if has-docstring?
                        (take-while #(= :whitespace (n/tag %)) after-doc)
                        (take-while #(= :whitespace (n/tag %)) after-name))]
                (concat ws
                        [(if has-metadata?
                           ;; Add to existing metadata
                           (n/map-node
                            (concat (n/children potential-meta)
                                    [(n/whitespace-node " ")
                                     (n/keyword-node :malli/schema)
                                     (n/whitespace-node " ")
                                     malli-schema]))
                           ;; Create new metadata
                           (n/meta-node
                            (n/map-node
                             [(n/keyword-node :malli/schema)
                              (n/whitespace-node " ")
                              malli-schema])
                            (n/token-node nil)))])))
            ;; Add the processed arities with whitespace
            (interleave new-arities ws-between))))
        
        ;; Single arity
        (let [[args-vec & body] (drop-while #(= :whitespace (n/tag %)) body-start)
              ws-before-args (take-while #(= :whitespace (n/tag %)) body-start)
              [gspec remaining-body] (extract-gspec-from-body body)
              parsed-gspec (parse-gspec-node gspec)
              malli-schema (when parsed-gspec (build-malli-schema-node parsed-gspec))]
          (n/list-node
           (concat
            [new-defn-sym ws-before-name name-node]
            (when has-docstring?
              (concat (take-while #(= :whitespace (n/tag %)) after-name)
                      [potential-doc]))
            (when (or has-metadata? malli-schema)
              (let [ws (if has-docstring?
                        (take-while #(= :whitespace (n/tag %)) after-doc)
                        (take-while #(= :whitespace (n/tag %)) after-name))]
                (concat ws
                        [(if has-metadata?
                           ;; Add to existing metadata
                           (n/map-node
                            (concat (n/children potential-meta)
                                    (when malli-schema
                                      [(n/whitespace-node " ")
                                       (n/keyword-node :malli/schema)
                                       (n/whitespace-node " ")
                                       malli-schema])))
                           ;; Create new metadata with just Malli schema
                           (when malli-schema
                             (n/map-node
                              [(n/keyword-node :malli/schema)
                               (n/whitespace-node " ")
                               malli-schema])))])))
            ws-before-args
            [args-vec]
            (if gspec
              ;; Skip the gspec, keep remaining body
              remaining-body
              ;; Keep all body
              body))))))))

(defn remove-guardrails-require
  "Remove Guardrails import from require vector"
  [require-vec]
  (when (= :vector (n/tag require-vec))
    (let [children (n/children require-vec)
          first-elem (first (filter #(not= :whitespace (n/tag %)) children))]
      (when (and first-elem
                 (= :token (n/tag first-elem))
                 (#{`com.fulcrologic.guardrails.malli.core
                    `com.fulcrologic.guardrails.core}
                  (n/sexpr first-elem)))
        ;; This is a Guardrails require, return nil to remove it
        nil))))

(defn clean-ns-form
  "Clean Guardrails imports from ns form node"
  [ns-node]
  (when (and (= :list (n/tag ns-node))
             (let [first-child (first (n/children ns-node))]
               (and (= :token (n/tag first-child))
                    (= 'ns (n/sexpr first-child)))))
    (n/list-node
     (mapcat (fn [child]
               (cond
                 ;; :require clause
                 (and (= :list (n/tag child))
                      (let [first-elem (first (filter #(not= :whitespace (n/tag %)) 
                                                      (n/children child)))]
                        (and first-elem
                             (= :token (n/tag first-elem))
                             (= :require (n/sexpr first-elem)))))
                 (let [require-children (n/children child)
                       cleaned (keep (fn [node]
                                      (if (= :vector (n/tag node))
                                        (remove-guardrails-require node)
                                        node))
                                    require-children)]
                   (if (> (count (filter #(= :vector (n/tag %)) cleaned)) 0)
                     [(n/list-node cleaned)]
                     []))
                 
                 ;; Keep other nodes
                 :else [child]))
             (n/children ns-node)))))

;; ============================================================================
;; File processing
;; ============================================================================

(defn migrate-string
  "Migrate a string of Clojure code"
  [code-str]
  (let [forms-node (p/parse-string-all code-str)
        nodes (n/children forms-node)
        transformed (map (fn [node]
                          (cond
                            ;; Clean namespace form
                            (and (= :list (n/tag node))
                                 (let [fc (first (n/children node))]
                                   (and (= :token (n/tag fc))
                                        (= 'ns (n/sexpr fc)))))
                            (clean-ns-form node)
                            
                            ;; Transform >defn forms
                            (and (= :list (n/tag node))
                                 (let [fc (first (n/children node))]
                                   (and (= :token (n/tag fc))
                                        (#{'>defn '>defn- '>} (n/sexpr fc)))))
                            (transform-defn-node node)
                            
                            ;; Keep everything else
                            :else node))
                        nodes)]
    (str/join "" (map n/string transformed))))

(defn migrate-file
  "Migrate a file from Guardrails to Malli"
  [input-path output-path]
  (let [content (slurp input-path)
        migrated (migrate-string content)]
    (spit output-path migrated)
    {:status :success
     :file output-path}))