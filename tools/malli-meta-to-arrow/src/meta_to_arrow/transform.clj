(ns meta-to-arrow.transform
  "Core transformation logic using rewrite-clj to preserve formatting."
  (:require [rewrite-clj.zip :as z]
            [rewrite-clj.node :as n]
            [clojure.string :as str]))

;; ============================================================================
;; Helper Functions
;; ============================================================================

(defn find-ns-form
  "Find the ns form in a zipper."
  [zloc]
  (z/find zloc z/next 
         (fn [loc]
           (and (z/list? loc)
                (= 'ns (z/sexpr (z/down loc)))))))

(defn add-malli-require
  "Add malli.core require if not present."
  [zloc require-alias]
  (let [ns-loc (find-ns-form zloc)]
    (if-not ns-loc
      zloc  ; No ns form, return as-is
      (let [ns-loc (z/down ns-loc)  ; Move to 'ns symbol
            ns-name-loc (z/right ns-loc)]  ; Move to ns name
        ;; Find :require clause
        (if-let [require-loc (z/find ns-name-loc z/right
                                     (fn [loc]
                                       (and (z/list? loc)
                                            (= :require (z/sexpr (z/down loc))))))]
          ;; Check if malli.core already required
          (let [has-malli? (z/find (z/down require-loc) z/right
                                   (fn [loc]
                                     (and (z/vector? loc)
                                          (let [first-elem (z/down loc)]
                                            (and first-elem
                                                 (= 'malli.core (z/sexpr first-elem)))))))]
            (if has-malli?
              zloc  ; Already has malli.core
              ;; Add malli.core require
              (-> require-loc
                  z/down  ; Move to :require keyword
                  (z/insert-right (n/spaces 1))
                  (z/insert-right (n/vector-node
                                   [(n/token-node 'malli.core)
                                    (n/spaces 1)
                                    (n/keyword-node :as)
                                    (n/spaces 1)
                                    (n/token-node (symbol require-alias))]))
                  z/up
                  z/up)))
          ;; No :require clause, add one
          (-> ns-name-loc
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
              z/up))))))

(defn extract-malli-schema
  "Extract :malli/schema from metadata map."
  [metadata-map]
  (when (map? metadata-map)
    (:malli/schema metadata-map)))

(defn remove-malli-from-metadata
  "Remove :malli/schema from metadata map, returns nil if map becomes empty."
  [metadata-map]
  (when (map? metadata-map)
    (let [cleaned (dissoc metadata-map :malli/schema)]
      (when (seq cleaned)
        cleaned))))

(defn create-arrow-form
  "Create an m/=> form node."
  [fn-name schema require-alias]
  (n/list-node
   [(n/token-node (symbol require-alias "=>"))
    (n/spaces 1)
    (n/token-node fn-name)
    (n/spaces 1)
    ;; Convert schema sexpr back to node
    (n/coerce schema)]))

;; ============================================================================
;; Main Transformation
;; ============================================================================

(defn transform-defn
  "Transform a single defn with metadata schema to m/=> form.
   Returns updated zloc and transformation info."
  [zloc require-alias]
  (let [defn-loc (z/down zloc)  ; Move to defn/defn- symbol
        fn-name-loc (z/right defn-loc)  ; Move to function name
        fn-name (z/sexpr fn-name-loc)
        after-name (z/right fn-name-loc)]  ; What comes after name
    
    ;; Check different positions for metadata
    (cond
      ;; Metadata map directly after name
      (and after-name (z/map? after-name))
      (let [metadata (z/sexpr after-name)
            schema (extract-malli-schema metadata)]
        (if schema
          (let [cleaned-metadata (remove-malli-from-metadata metadata)
                ;; Navigate to metadata and either replace or remove it
                metadata-loc (-> zloc z/down z/right z/right)  ; defn, name, metadata
                zloc-with-cleaned (if cleaned-metadata
                                     (-> metadata-loc
                                         (z/replace (n/coerce cleaned-metadata))
                                         z/up)  ; Back to defn
                                     (-> metadata-loc
                                         z/remove
                                         z/up))  ; Back to defn
                ;; Create m/=> form
                arrow-form (create-arrow-form fn-name schema require-alias)]
            {:zloc zloc-with-cleaned
             :arrow-form arrow-form
             :transformed? true
             :name fn-name
             :schema schema})
          {:zloc zloc
           :transformed? false}))
      
      ;; Check for docstring then metadata
      (and after-name (string? (z/sexpr after-name)))
      (let [after-doc (z/right after-name)]
        (if (and after-doc (z/map? after-doc))
          (let [metadata (z/sexpr after-doc)
                schema (extract-malli-schema metadata)]
            (if schema
              (let [cleaned-metadata (remove-malli-from-metadata metadata)
                    ;; Navigate to metadata after docstring
                    metadata-loc (-> zloc z/down z/right z/right z/right)  ; defn, name, doc, metadata
                    zloc-with-cleaned (if cleaned-metadata
                                         (-> metadata-loc
                                             (z/replace (n/coerce cleaned-metadata))
                                             z/up)  ; Back to defn
                                         (-> metadata-loc
                                             z/remove
                                             z/up))  ; Back to defn
                    arrow-form (create-arrow-form fn-name schema require-alias)]
                {:zloc zloc-with-cleaned
                 :arrow-form arrow-form
                 :transformed? true
                 :name fn-name
                 :schema schema})
              {:zloc zloc
               :transformed? false}))
          {:zloc zloc
           :transformed? false}))
      
      :else
      {:zloc zloc
       :transformed? false})))

(defn insert-arrow-after-defn
  "Insert an arrow form after a defn, handling different contexts."
  [zloc arrow-form]
  ;; Check if we can insert at current position
  (if (z/rightmost? zloc)
    ;; If we're the last form, append with newline
    (-> zloc
        (z/insert-right (n/newlines 1))
        (z/insert-right arrow-form))
    ;; Otherwise insert normally
    (-> zloc
        (z/insert-right (n/newlines 1))
        (z/insert-right arrow-form))))

(defn transform-file
  "Transform an entire file, converting all defn metadata schemas to m/=> forms."
  [content {:keys [require-alias] :or {require-alias "m"}}]
  (let [zloc (z/of-string content {:track-position? true})
        ;; Collect all transformations first
        transformations (atom [])
        arrow-forms (atom [])  ; Store arrow forms to insert
        needs-require? (atom false)
        
        ;; First pass: find and transform all defns, collect arrow forms
        zloc-transformed
        (z/postwalk zloc
                    (fn [loc]
                      (and (z/list? loc)
                           (let [first-elem (z/down loc)]
                             (and first-elem
                                  (#{'defn 'defn-} (z/sexpr first-elem))))))
                    (fn [loc]
                      (let [result (transform-defn loc require-alias)]
                        (when (:transformed? result)
                          (swap! transformations conj
                                 {:name (:name result)
                                  :schema (:schema result)})
                          (swap! arrow-forms conj
                                 {:zloc (:zloc result)
                                  :arrow-form (:arrow-form result)})
                          (reset! needs-require? true))
                        (:zloc result))))
        
        ;; Second pass: insert arrow forms after their corresponding defns
        zloc-with-arrows
        (reduce (fn [z {:keys [zloc arrow-form]}]
                  ;; Find the defn form that was transformed
                  (if-let [defn-loc (z/find z z/next
                                            (fn [loc]
                                              (and (z/list? loc)
                                                   (= zloc loc))))]
                    (insert-arrow-after-defn defn-loc arrow-form)
                    z))
                zloc-transformed
                @arrow-forms)
        
        ;; Third pass: add require if needed
        zloc-final (if @needs-require?
                     (add-malli-require zloc-with-arrows require-alias)
                     zloc-with-arrows)]
    
    {:transformed (z/root-string zloc-final)
     :count (count @transformations)
     :additions @transformations}))