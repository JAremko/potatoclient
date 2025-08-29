(ns potatoclient.malli.oneof
  "Oneof schema for EDN maps - cleaner implementation with less nesting.
   
   Usage: 
   [:oneof 
    [:ping :cmd/ping] 
    [:cv :cmd/cv] 
    [:rotary :cmd/rotary]]
   
   With base fields (not counted in oneof constraint):
   [:oneof
    [:protocol_version {:base true} [:int {:min 1}]]  
    [:session_id {:base true} [:int {:min 1}]]        
    [:ping :cmd/ping]                                  
    [:noop :cmd/empty]]"
  (:require
    [clojure.set]
    [clojure.test.check.generators :as gen]
    [malli.core :as m]
    [malli.generator :as mg]))

(defn- parse-child 
  "Parse a child entry into [key props schema] format"
  [child options]
  (cond
    ;; [key props schema] format
    (and (vector? child)
         (>= (count child) 3)
         (keyword? (first child))
         (map? (second child)))
    [(first child) (second child) (m/schema (nth child 2) options)]
    
    ;; [key schema] format
    (and (vector? child)
         (= 2 (count child))
         (keyword? (first child)))
    [(first child) {} (m/schema (second child) options)]
    
    :else
    (m/-fail! ::invalid-child {:child child})))

(defn- validate-oneof-value
  "Validate a value against oneof constraints"
  [value field-keys base-fields oneof-fields validators]
  (and (map? value)
       ;; Check no extra keys (closed map)
       (let [value-keys (set (keys value))]
         (and (clojure.set/subset? value-keys field-keys)
              ;; Validate all base fields if present
              (every? (fn [k]
                        (if-let [v (get value k)]
                          ((get validators k) v)
                          true))
                      base-fields)
              ;; Exactly one non-nil oneof field
              (let [non-nil-oneof-fields (filter #(some? (get value %)) oneof-fields)]
                (and (= 1 (count non-nil-oneof-fields))
                     ;; Validate the active oneof field
                     (when-let [active-key (first non-nil-oneof-fields)]
                       (when-let [validator (get validators active-key)]
                         (validator (get value active-key))))))))))

(defn- explain-oneof-value
  "Explain validation failures for oneof value"
  [value path in acc form field-keys base-fields oneof-fields entries validators]
  (cond
    (not (map? value))
    (conj acc {:path path
               :in in
               :schema form
               :value value
               :message "should be a map"})
    
    :else
    (let [value-keys (set (keys value))
          extra-keys (clojure.set/difference value-keys field-keys)
          non-nil-oneof-fields (filter #(some? (get value %)) oneof-fields)]
      (cond
        (seq extra-keys)
        (conj acc {:path path
                   :in in
                   :schema form
                   :value value
                   :message (str "unexpected keys: " (vec extra-keys))})
        
        (zero? (count non-nil-oneof-fields))
        (conj acc {:path path
                   :in in
                   :schema form
                   :value value
                   :message "must have exactly one non-nil field"})
        
        (defn (count non-nil-oneof-fields) 1)
        (conj acc {:path path
                   :in in
                   :schema form
                   :value value
                   :message (str "multiple non-nil fields: " (vec non-nil-oneof-fields))})
        
        :else 
        ;; Check validation of all fields
        (let [;; First check base field validations
              base-errors (reduce (fn [acc k]
                                   (if-let [v (get value k)]
                                     (if-let [schema-entry (first (filter #(= (first %) k) entries))]
                                       (let [[_ _ schema] schema-entry]
                                         (if-not (m/validate schema v)
                                           ;; Use m/explain to get errors in the right format
                                           (if-let [explanation (m/explain schema v)]
                                             (into acc (map (fn [error]
                                                             (update error :in #(vec (concat in [k] %))))
                                                           (:errors explanation)))
                                             acc)
                                           acc))
                                       acc)
                                     acc))
                                 acc
                                 base-fields)
              ;; Then check the active oneof field
              active-key (first non-nil-oneof-fields)]
          (if active-key
            (if-let [schema-entry (first (filter #(= (first %) active-key) entries))]
              (let [[_ _ schema] schema-entry]
                (if-not (m/validate schema (get value active-key))
                  ;; Use m/explain to get errors in the right format
                  (if-let [explanation (m/explain schema (get value active-key))]
                    (into base-errors (map (fn [error]
                                            (update error :in #(vec (concat in [active-key] %))))
                                          (:errors explanation)))
                    base-errors)
                  base-errors))
              base-errors)
            base-errors))))))

(defn- parse-oneof-value
  "Parse value with oneof constraints"
  [value oneof-fields parsers]
  (when (map? value)
    (let [non-nil-oneof-fields (filter #(some? (get value %)) oneof-fields)]
      (when (= 1 (count non-nil-oneof-fields))
        (reduce-kv 
         (fn [acc k v]
           (if-let [parser (get parsers k)]
             (if-let [parsed (parser v)]
               (assoc acc k parsed)
               acc)
             (assoc acc k v)))
         {}
         value)))))

(defn- create-oneof-schema
  "Create the Schema instance for oneof"
  [parent properties entries field-keys base-fields oneof-fields validators form options]
  (reify
    m/Schema
    (-validator [_]
      (fn [value]
        (validate-oneof-value value field-keys base-fields oneof-fields validators)))
    
    (-explainer [_ path]
      (fn [value in acc]
        (explain-oneof-value value path in acc form field-keys base-fields oneof-fields entries validators)))
    
    (-parser [_]
      (let [parsers (into {} (map (fn [[k _ schema]]
                                    [k (m/parser schema)])
                                  entries))]
        (fn [value]
          (parse-oneof-value value oneof-fields parsers))))
    
    (-unparser [_]
      (let [unparsers (into {} (map (fn [[k _ schema]]
                                      [k (m/unparser schema)])
                                    entries))]
        (fn [value]
          (parse-oneof-value value oneof-fields unparsers))))
    
    (-transformer [this transformer method options]
      (let [this-transformer (m/-value-transformer transformer this method options)]
        (if (seq entries)
          (let [transformers (mapv (fn [[k _ schema]]
                                     [k (m/-transformer schema transformer method options)])
                                   entries)
                transform-map (into {} transformers)]
            (m/-intercepting this-transformer
              (fn [value]
                (if (map? value)
                  (reduce-kv (fn [acc k v]
                              (if-let [transformer (get transform-map k)]
                                (assoc acc k (transformer v))
                                (assoc acc k v)))
                            {}
                            value)
                  value))))
          this-transformer)))
    
    (-walk [this walker path options]
      (when (m/-accept walker this path options)
        (m/-outer walker this path (mapv (fn [[k props schema]]
                                           [k props (m/-walk schema walker (conj path k) options)])
                                         entries) options)))
    
    (-properties [_] properties)
    (-options [_] options)
    (-children [_] entries)
    (-parent [_] parent)
    (-form [_] form)))

(defn -oneof-schema
  "Creates the :oneof schema implementation"
  []
  (reify
    m/IntoSchema
    (-type [_] :oneof)
    (-type-properties [_] nil)
    (-properties-schema [_ _] nil)
    (-children-schema [_ _] nil)
    (-into-schema [parent properties children options]
      (when (empty? children)
        (m/-fail! ::no-children {:type :oneof}))
      (let [entries (mapv #(parse-child % options) children)
            field-keys (set (map first entries))
            base-fields (set (keep (fn [[k props _]] (when (:base props) k)) entries))
            oneof-fields (clojure.set/difference field-keys base-fields)
            validators (into {} (map (fn [[k _ schema]]
                                      [k (m/validator schema)])
                                    entries))
            form (m/-create-form :oneof properties children options)]
        (create-oneof-schema parent properties entries field-keys base-fields 
                             oneof-fields validators form options)))))

;; Create the schema instance
(def oneof-schema (-oneof-schema))

;; Generator for oneof schemas
(defmethod mg/-schema-generator :oneof [schema options]
  (let [children (m/children schema)
        ;; Separate base fields from oneof fields
        base-entries (filter #(get-in % [1 :base]) children)
        oneof-entries (remove #(get-in % [1 :base]) children)]
    (if (and (empty? base-entries) (empty? oneof-entries))
      (gen/return {})
      ;; Generate base fields and pick one oneof field to be active
      (gen/bind
       (if (empty? oneof-entries)
         (gen/return nil)
         (gen/elements oneof-entries))
       (fn [active-oneof]
         (gen/fmap
          (fn [generated-values]
            (let [[base-values oneof-value] generated-values]
              ;; Merge base fields with the active oneof field
              (merge base-values
                     (when active-oneof
                       {(first active-oneof) oneof-value})
                     ;; Add nil for inactive oneof fields
                     (into {}
                           (for [[k _ _] oneof-entries
                                 :when (and active-oneof (not= k (first active-oneof)))]
                             [k nil])))))
          (gen/tuple
           ;; Generate all base fields
           (if (empty? base-entries)
             (gen/return {})
             (apply gen/hash-map
                    (mapcat (fn [[k _ schema-or-ref]]
                              [k (mg/generator schema-or-ref options)])
                            base-entries)))
           ;; Generate the active oneof field value
           (if active-oneof
             (mg/generator (nth active-oneof 2) options)
             (gen/return nil)))))))))

(defn register-oneof-schema!
  "Register the :oneof schema type in a registry."
  [registry]
  (assoc registry :oneof oneof-schema))