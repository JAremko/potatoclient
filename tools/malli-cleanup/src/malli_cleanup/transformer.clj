(ns malli-cleanup.transformer
  "Transform Malli metadata to canonical position and convert lambdas"
  (:require [clojure.string :as str]
            [rewrite-clj.zip :as z]
            [rewrite-clj.node :as n]))

;; ============================================================================
;; Lambda transformation
;; ============================================================================

(defn transform-lambda-node
  "Transform a lambda node to partial if possible"
  [node]
  (if-not (n/list? node)
    node
    (let [children (n/children node)]
      (when (and (>= (count children) 3)
                 (= 'fn* (n/sexpr (first children)))
                 (n/vector? (second children)))
        (let [args-node (second children)
              body-node (nth children 2)
              args (n/children args-node)]
          (when (= 1 (count args))
            (let [arg-sym (n/sexpr (first args))]
              ;; Check for simple instance? pattern: (fn* [x] (instance? Type x))
              (when (and (n/list? body-node)
                         (let [body-children (n/children body-node)]
                           (and (= 3 (count body-children))
                                (= 'instance? (n/sexpr (first body-children)))
                                (= arg-sym (n/sexpr (nth body-children 2))))))
                (let [body-children (n/children body-node)
                      type-node (second body-children)]
                  (n/list [(n/token 'partial)
                           (n/whitespace " ")
                           (n/token 'instance?)
                           (n/whitespace " ")
                           type-node])))
              
              ;; Check for simple predicate pattern: (fn* [x] (pred? x))
              (when (and (n/list? body-node)
                         (let [body-children (n/children body-node)]
                           (and (= 2 (count body-children))
                                (let [pred (n/sexpr (first body-children))]
                                  (and (symbol? pred)
                                       (str/ends-with? (name pred) "?")))
                                (= arg-sym (n/sexpr (second body-children))))))
                (let [body-children (n/children body-node)
                      pred-node (first body-children)]
                  pred-node)))))))))

(defn transform-fn-node-in-context
  "Transform :fn node with consideration for error messages"
  [zloc]
  (if-not (and (z/vector? zloc) 
               (>= (count (z/sexpr zloc)) 2)
               (= :fn (z/sexpr (z/down zloc))))
    zloc
    (let [down-loc (z/down zloc)
          right-loc (z/right down-loc)]
      (cond
        ;; Has map metadata (e.g., {:error/message "..."})
        (and (z/right right-loc)
             (z/map? right-loc))
        (let [map-loc right-loc
              fn-loc (z/right map-loc)]
          (if-let [transformed (transform-lambda-node (z/node fn-loc))]
            (-> zloc
                (z/down)
                (z/right) ; to map
                (z/right) ; to fn
                (z/replace transformed)
                (z/up))
            zloc))
        
        ;; Direct lambda
        :else
        (if-let [transformed (transform-lambda-node (z/node right-loc))]
          (-> zloc
              (z/down)
              (z/right) ; to lambda
              (z/replace transformed)
              (z/up))
          zloc)))))

(defn transform-lambdas-in-schema
  "Recursively transform all lambdas in a schema"
  [zloc]
  (loop [loc zloc]
    (if (z/end? loc)
      (z/root loc)
      (recur (z/next (if (and (z/vector? loc)
                              (= :fn (z/sexpr (z/down loc))))
                       (transform-fn-node-in-context loc)
                       loc))))))

;; ============================================================================
;; Metadata positioning
;; ============================================================================

(defn find-defn-components
  "Find and categorize components of a defn form"
  [zloc]
  (let [defn-loc (z/down zloc)
        name-loc (z/right defn-loc)
        rest-loc (z/right name-loc)]
    
    (loop [loc rest-loc
           components {:defn defn-loc
                      :name name-loc
                      :inline-meta nil
                      :docstring nil
                      :attr-map nil
                      :params nil}]
      (if (or (not loc) (z/end? loc))
        components
        (cond
          ;; Inline metadata on name
          (and (not (:inline-meta components))
               (z/map? loc)
               (:malli/schema (z/sexpr loc)))
          (recur (z/right loc) (assoc components :inline-meta loc))
          
          ;; Docstring
          (and (not (:docstring components))
               (string? (z/sexpr loc)))
          (recur (z/right loc) (assoc components :docstring loc))
          
          ;; Attr-map after docstring
          (and (not (:attr-map components))
               (:docstring components)
               (z/map? loc))
          (recur (z/right loc) (assoc components :attr-map loc))
          
          ;; Parameters or body
          :else
          (assoc components :params loc))))))

(defn needs-metadata-move?
  "Check if metadata needs to be moved"
  [components]
  (and (:inline-meta components)
       (not= (:attr-map components) (:inline-meta components))))

(defn move-metadata
  "Move inline metadata to proper position"
  [zloc]
  (let [components (find-defn-components zloc)]
    (if (not (needs-metadata-move? components))
      zloc
      (let [meta-map (z/sexpr (:inline-meta components))]
        (cond
          ;; Has docstring - add/merge with attr-map after it
          (:docstring components)
          (if (:attr-map components)
            ;; Merge with existing attr-map
            (-> zloc
                (z/down) ; to defn
                (z/right) ; to name
                (z/right) ; to inline-meta
                (z/remove) ; remove inline-meta
                (z/right) ; to attr-map (was after inline-meta, now after docstring)
                (z/edit #(merge % meta-map))
                (z/up))
            ;; Add new attr-map after docstring
            (-> zloc
                (z/down) ; to defn
                (z/right) ; to name
                (z/right) ; to inline-meta
                (z/remove) ; remove inline-meta
                (z/insert-right (n/newlines 1))
                (z/insert-right (n/spaces 2))
                (z/insert-right meta-map)
                (z/up)))
          
          ;; No docstring - keep as first element after name
          :else
          zloc)))))

;; ============================================================================
;; Main transformation
;; ============================================================================

(defn transform-defn
  "Transform a single defn form"
  [zloc]
  (if-not (and (z/list? zloc)
               (contains? #{'defn 'defn-} (z/sexpr (z/down zloc))))
    zloc
    (-> zloc
        move-metadata
        transform-lambdas-in-schema)))

(defn transform-file
  "Transform all defn forms in a file"
  [content]
  (let [zloc (z/of-string content {:track-position? true})]
    (loop [loc zloc
           transformed loc]
      (if (z/end? loc)
        (z/root-string transformed)
        (let [new-loc (if (and (z/list? loc)
                              (contains? #{'defn 'defn-} (z/sexpr (z/down loc))))
                       (transform-defn loc)
                       loc)]
          (recur (z/next new-loc) new-loc))))))

(defn transform-file-path
  "Transform a file at the given path"
  [file-path]
  (let [content (slurp file-path)
        transformed (transform-file content)]
    transformed))