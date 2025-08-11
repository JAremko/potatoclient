(ns potatoclient.specs.oneof-edn
  "Closed oneof schema for EDN data that treats nil values as absent.
   Compatible with Pronto EDN where inactive branches have nil values.
   Acts as a closed map - rejects any keys not in the schema, even with nil values."
  (:require
   [clojure.set]
   [malli.core :as m]
   [malli.impl.util :as miu]
   [malli.generator :as mg]
   [clojure.test.check.generators :as gen]))

(def _ONeof-edn-schema
  "Custom :oneof_edn schema for validating EDN maps with exactly one non-nil field set.
   Treats nil values as absent, compatible with Pronto EDN representations.
   Acts as a closed map - rejects any extra keys not defined in the schema."
  (m/-simple-schema
   {:type :oneof_edn
    :type_properties {:generator true}
    :compile (fn [properties children options]
               (let [;; Handle both property-style and children-style definitions
                     field-map (if (and (empty? children) (map? properties))
                                 ;; Properties style: fields are in properties map
                                 (dissoc properties :error/message)
                                 ;; Children style: fields are in children vector
                                 (apply hash-map children))
                     field-names (set (keys field-map))
                     field-validators (into {} (map (fn [[k v]]
                                                     [k (m/validator v options)])
                                                   field-map))
                     ;; Helper to get non-nil fields from the specified field set
                     get-active-fields (fn [value]
                                         (filter #(and (contains? field-names %)
                                                      (some? (get value %)))
                                                (keys value)))]
                 {:pred (fn [value]
                          (and (map? value)
                               ;; Check for extra keys (closed map behavior)
                               (let [value-keys (set (keys value))
                                     extra-keys (clojure.set/difference value-keys field-names)]
                                 (and (empty? extra-keys)
                                      ;; Exactly one of the specified fields must be present and non-nil
                                      (let [active-fields (get-active-fields value)]
                                        (and (= 1 (count active-fields))
                                             ;; Validate the active field's value
                                             (let [active-field (first active-fields)
                                                   validator (get field-validators active-field)]
                                               (validator (get value active-field)))))))))
                  :min (* 2 (count field-map))
                  :max (* 2 (count field-map))
                  :type_properties properties
                  :error/fn (fn [{:keys [value]} _]
                              (cond
                                (not (map? value)) "must be a map"
                                :else
                                (let [value-keys (set (keys value))
                                      extra-keys (clojure.set/difference value-keys field-names)
                                      active-fields (get-active-fields value)]
                                  (cond
                                    (not (empty? extra-keys))
                                    (str "unexpected keys found: " (vec extra-keys) 
                                         ", allowed keys: " (vec field-names))
                                    (zero? (count active-fields))
                                    "must have exactly one non-nil field set"
                                    (> (count active-fields) 1)
                                    (str "must have exactly one non-nil field set, found: "
                                         (vec active-fields))
                                    :else nil))))}))}))

;; Generator for oneof-edn schemas
;; Efficient: generates exactly ONE field with a non-nil value
(defmethod mg/-schema-generator :oneof_edn [schema options]
  (let [children (m/children schema)
        properties (m/properties schema)
        ;; Handle both property-style and children-style definitions
        field-map (if (and (empty? children) (map? properties))
                    (dissoc properties :error/message)
                    (apply hash-map children))]
    (if (empty? field-map)
      (gen/return {})
      ;; Efficiently generate exactly one field - no wasted generation
      (gen/bind
       (gen/elements (keys field-map))
       (fn [field-name]
         (gen/fmap
          (fn [field-value]
            ;; Returns a map with only the selected field set
            {field-name field-value})
          (mg/generator (get field-map field-name) options)))))))

(defn register_ONeof-edn-schema!
  "Register the :oneof_edn schema type"
  []
  {:oneof_edn _ONeof-edn-schema})