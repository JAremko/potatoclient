(ns potatoclient.specs.proto-type-mapping-nested
  "Hand-written logic for nested proto type mapping.
  
  Uses the auto-generated data from proto-type-mapping-nested-data
  to provide functions for enriching command structures with metadata."
  (:require [potatoclient.specs.proto-type-mapping-nested-data :as data]))

(defn get-child-class
  "Get the Java class for a child keyword given the parent class.
  Returns nil if not found."
  [parent-class child-keyword]
  (get-in data/class->children [parent-class child-keyword]))

(defn enrich-with-metadata
  "Recursively attach proto-type metadata to a command structure.
  Starts from a known parent type and traverses top-down.
  
  Each map in the structure gets metadata with its corresponding
  Java protobuf class, allowing unambiguous protobuf building."
  ([data parent-type]
   (enrich-with-metadata data parent-type []))
  ([data parent-type path]
   (cond
     ;; Map: enrich with current type and process children
     (map? data)
     (with-meta
       (reduce-kv
         (fn [acc k v]
           (if-let [child-type (get-child-class parent-type k)]
             ;; Found the child type, recursively enrich
             (assoc acc k
                    (enrich-with-metadata v child-type (conj path k)))
             ;; No child type found, keep as-is
             (assoc acc k v)))
         {}
         data)
       {:proto-type parent-type})
     
     ;; Vector/list: enrich each element with same parent type
     (sequential? data)
     (mapv #(enrich-with-metadata % parent-type path) data)
     
     ;; Leaf value: return as-is
     :else data)))

(defn enrich-command
  "Enrich a command with proto-type metadata.
  
  Starts from the root command type and recursively adds metadata
  to all nested maps, resolving ambiguous keywords based on parent context."
  [command]
  ;; Always start from the root command type
  (enrich-with-metadata command "cmd.JonSharedCmd$Root"))

;; Debugging helpers
(defn find-ambiguous-keywords
  "Find keywords that appear under multiple parent classes.
  Useful for understanding which keywords need parent context."
  []
  (let [keyword-parents (reduce-kv 
                          (fn [acc parent children]
                            (reduce-kv
                              (fn [acc2 k _]
                                (update acc2 k (fnil conj #{}) parent))
                              acc
                              children))
                          {}
                          data/class->children)]
    (into {}
          (filter (fn [[k parents]] (> (count parents) 1))
                  keyword-parents))))

(defn is-leaf-class?
  "Check if a Java class is a leaf node (has no children)."
  [java-class]
  (contains? data/leaf-classes java-class))

(defn is-valid-child?
  "Check if a Java class can be a child of a parent class."
  [parent-class child-class]
  (contains? (get data/class->direct-children parent-class #{}) child-class))

(defn get-all-children
  "Get all direct child classes of a parent class."
  [parent-class]
  (get data/class->direct-children parent-class #{}))