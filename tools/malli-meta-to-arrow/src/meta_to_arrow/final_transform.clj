(ns meta-to-arrow.final-transform
  "Final working transformation based on guardrails migration approach."
  (:require [rewrite-clj.zip :as z]
            [rewrite-clj.node :as n]
            [clojure.string :as str]))

(defn transform-single-defn
  "Transform a single defn with metadata to m/=> form."
  [zloc require-alias]
  ;; First, get the function name
  (let [fn-name-loc (-> zloc z/down z/right)
        fn-name (z/sexpr fn-name-loc)
        ;; Check what comes after the name
        after-name (z/right fn-name-loc)
        has-docstring? (and after-name (string? (z/sexpr after-name)))
        after-doc (if has-docstring? 
                    (z/right after-name) 
                    after-name)
        has-metadata? (and after-doc (map? (z/sexpr after-doc)))
        metadata (when has-metadata? (z/sexpr after-doc))
        schema (when metadata (:malli/schema metadata))]
    
    (if schema
      ;; We have a schema to transform
      (let [;; Remove :malli/schema from metadata or remove metadata entirely
            cleaned-metadata (when metadata
                               (let [m (dissoc metadata :malli/schema)]
                                 (when (seq m) m)))
            ;; Update the defn form
            zloc-updated (if has-metadata?
                           (if cleaned-metadata
                             ;; Replace metadata with cleaned version
                             (-> zloc
                                 z/down z/right  ; to name
                                 (cond-> has-docstring? z/right)  ; skip docstring if present
                                 z/right  ; to metadata
                                 (z/replace (n/coerce cleaned-metadata))
                                 z/up)
                             ;; Remove metadata entirely
                             (-> zloc
                                 z/down z/right  ; to name
                                 (cond-> has-docstring? z/right)  ; skip docstring if present
                                 z/right  ; to metadata
                                 z/remove
                                 z/up))
                           zloc)
            ;; Create the m/=> form
            arrow-form (n/list-node
                        [(n/token-node (symbol require-alias "=>"))
                         (n/spaces 1)
                         (n/token-node fn-name)
                         (n/spaces 1)
                         (n/coerce schema)])]
        ;; Insert arrow form after defn
        (-> zloc-updated
            (z/insert-right (n/newlines 1))
            (z/insert-right arrow-form)))
      ;; No schema, return as-is
      zloc)))

(defn add-malli-require
  "Add malli.core require to ns form if not already present."
  [zloc require-alias]
  (if-let [ns-loc (z/find zloc z/next 
                          (fn [loc]
                            (and (z/list? loc)
                                 (= 'ns (z/sexpr (z/down loc))))))]
    ;; Check if already has require
    (let [require-loc (z/find (z/down ns-loc) z/right
                              (fn [loc]
                                (and (z/list? loc)
                                     (= :require (z/sexpr (z/down loc))))))]
      (if require-loc
        ;; Check if malli.core already required
        (if (z/find (z/down require-loc) z/right
                   (fn [loc]
                     (and (z/vector? loc)
                          (let [first-elem (z/sexpr (z/down loc))]
                            (= 'malli.core first-elem)))))
          zloc  ; Already has malli.core
          ;; Add to existing require
          (-> zloc
              (z/find-value z/next :require)
              (z/insert-right (n/spaces 1))
              (z/insert-right (n/vector-node
                               [(n/token-node 'malli.core)
                                (n/spaces 1)
                                (n/keyword-node :as)
                                (n/spaces 1)
                                (n/token-node (symbol require-alias))]))
              z/up))
        ;; No require clause, add one
        (-> ns-loc
            z/down z/right  ; to ns name
            (z/insert-right (n/newlines 1))
            (z/insert-right (n/spaces 2))
            (z/insert-right (n/list-node
                             [(n/keyword-node :require)
                              (n/spaces 1)
                              (n/vector-node
                               [(n/token-node 'malli.core)
                                (n/spaces 1)
                                (n/keyword-node :as)
                                (n/spaces 1)
                                (n/token-node (symbol require-alias))])]))
            z/up)))
    zloc))

(defn transform-file
  "Transform entire file content."
  [content {:keys [require-alias] :or {require-alias "m"}}]
  (let [zloc (z/of-string content)
        ;; Track if we made any changes
        changes (atom 0)
        
        ;; Transform all defns
        zloc-transformed
        (z/prewalk zloc
                   ;; Find defn forms
                   (fn [loc]
                     (and (z/list? loc)
                          (let [first-elem (z/down loc)]
                            (and first-elem
                                 (#{'defn 'defn-} (z/sexpr first-elem))))))
                   ;; Transform them
                   (fn [loc]
                     (let [;; Check if this defn has malli metadata
                           fn-name-loc (-> loc z/down z/right)
                           after-name (z/right fn-name-loc)
                           has-docstring? (and after-name (string? (z/sexpr after-name)))
                           after-doc (if has-docstring? 
                                       (z/right after-name) 
                                       after-name)
                           has-metadata? (and after-doc (map? (z/sexpr after-doc)))
                           metadata (when has-metadata? (z/sexpr after-doc))
                           has-schema? (and metadata (:malli/schema metadata))]
                       (if has-schema?
                         (do (swap! changes inc)
                             (transform-single-defn loc require-alias))
                         loc))))
        
        ;; Add require if we made changes
        zloc-final (if (pos? @changes)
                     (add-malli-require zloc-transformed require-alias)
                     zloc-transformed)]
    
    {:transformed (z/root-string zloc-final)
     :count @changes}))