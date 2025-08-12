(ns potatoclient.specs.oneof-edn
  "Oneof schema for EDN maps - like :altn but for maps with exactly one non-nil value.
   Compatible with Pronto EDN where inactive branches have nil values.
   
   Usage: [:oneof_edn [:ping :cmd/ping] [:cv :cmd/cv] [:rotary :cmd/rotary]]
   
   This validates maps where:
   - Exactly one of the specified keys has a non-nil value
   - Other specified keys can be nil or absent
   - No extra keys are allowed (closed map behavior)
   - The non-nil value must validate against its schema"
  (:require
   [clojure.set]
   [malli.core :as m]
   [malli.impl.util :as miu]
   [malli.generator :as mg]
   [clojure.test.check.generators :as gen]))

(defn -oneof-edn-schema
  "Creates the :oneof_edn schema implementation"
  []
  (reify
    m/IntoSchema
    (-type [_] :oneof_edn)
    (-type-properties [_] nil)
    (-properties-schema [_ _] nil)
    (-children-schema [_ _] nil)
    (-into-schema [_ properties children options]
      (when (empty? children)
        (m/-fail! ::no-children {:type :oneof_edn}))
      ;; Parse children as [key schema] pairs
      (let [entries (mapv (fn [child]
                           (if (and (vector? child) 
                                   (= 2 (count child)) 
                                   (keyword? (first child)))
                             [(first child) (m/schema (second child) options)]
                             (m/-fail! ::invalid-child {:child child})))
                         children)
            field-keys (set (map first entries))
            validators (into {} (map (fn [[k schema]]
                                      [k (m/validator schema)])
                                    entries))
            form (m/-create-form :oneof_edn properties children options)]
        (reify
          m/Schema
          (-validator [_]
            (fn [value]
              (and (map? value)
                   ;; Check no extra keys (closed map)
                   (let [value-keys (set (keys value))]
                     (and (clojure.set/subset? value-keys field-keys)
                          ;; Exactly one non-nil field
                          (let [non-nil-fields (filter #(some? (get value %)) field-keys)]
                            (and (= 1 (count non-nil-fields))
                                 ;; Validate the non-nil field
                                 (let [active-key (first non-nil-fields)
                                       validator (get validators active-key)]
                                   (validator (get value active-key))))))))))
          
          (-explainer [_ path]
            (fn [value in acc]
              (cond
                (not (map? value))
                (conj acc {:path path
                          :in in
                          :schema [:oneof_edn]
                          :value value
                          :message "should be a map"})
                
                :else
                (let [value-keys (set (keys value))
                      extra-keys (clojure.set/difference value-keys field-keys)
                      non-nil-fields (filter #(some? (get value %)) field-keys)]
                  (cond
                    (seq extra-keys)
                    (conj acc {:path path
                              :in in
                              :schema [:oneof_edn]
                              :value value
                              :message (str "unexpected keys: " (vec extra-keys))})
                    
                    (zero? (count non-nil-fields))
                    (conj acc {:path path
                              :in in
                              :schema [:oneof_edn]
                              :value value
                              :message "must have exactly one non-nil field"})
                    
                    (> (count non-nil-fields) 1)
                    (conj acc {:path path
                              :in in
                              :schema [:oneof_edn]
                              :value value
                              :message (str "multiple non-nil fields: " (vec non-nil-fields))})
                    
                    :else acc)))))
          
          (-parser [_] 
            (let [parsers (into {} (map (fn [[k schema]]
                                          [k (m/parser schema)])
                                        entries))]
              (fn [value]
                (when (map? value)
                  (let [non-nil-fields (filter #(some? (get value %)) field-keys)]
                    (when (= 1 (count non-nil-fields))
                      (let [active-key (first non-nil-fields)
                            parser (get parsers active-key)]
                        (when-let [parsed (parser (get value active-key))]
                          {active-key parsed}))))))))
          
          (-unparser [_]
            (let [unparsers (into {} (map (fn [[k schema]]
                                            [k (m/unparser schema)])
                                          entries))]
              (fn [value]
                (when (map? value)
                  (let [non-nil-fields (filter #(some? (get value %)) field-keys)]
                    (when (= 1 (count non-nil-fields))
                      (let [active-key (first non-nil-fields)
                            unparser (get unparsers active-key)]
                        (when-let [unparsed (unparser (get value active-key))]
                          {active-key unparsed}))))))))
          
          (-transformer [this transformer method options]
            (let [this-transformer (m/-value-transformer transformer this method options)]
              (if (seq entries)
                (let [transformers (mapv (fn [[k schema]]
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
              (m/-outer walker this path (mapv (fn [[k schema]]
                                                 [k (m/-walk schema walker (conj path k) options)])
                                               entries) options)))
          
          (-properties [_] properties)
          (-options [_] options)
          (-children [_] entries)
          (-parent [_] :oneof_edn)
          (-form [_] form))))))

;; Register the schema
(def _ONeof-edn-schema (-oneof-edn-schema))

;; Generator for oneof_edn schemas
(defmethod mg/-schema-generator :oneof_edn [schema options]
  (let [children (m/children schema)]
    (if (empty? children)
      (gen/return {})
      ;; Pick one entry and generate only that field
      ;; Children are already [key schema] pairs where schema is a parsed Schema object
      (gen/bind
       (gen/elements children)
       (fn [[field-key field-schema]]
         (gen/fmap
          (fn [field-value]
            {field-key field-value})
          (mg/generator field-schema options)))))))

(defn register_ONeof-edn-schema!
  "Register the :oneof_edn schema type"
  []
  {:oneof_edn _ONeof-edn-schema})