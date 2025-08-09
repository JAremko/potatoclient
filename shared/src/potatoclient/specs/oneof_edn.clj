(ns potatoclient.specs.oneof-edn
  "Oneof schema for EDN data that treats nil values as absent.
   Compatible with Pronto EDN where inactive branches have nil values."
  (:require
   [malli.core :as m]
   [malli.generator :as mg]
   [clojure.test.check.generators :as gen]))

(def -oneof-edn-schema
  "Custom :oneof-edn schema for validating EDN maps with exactly one non-nil field set.
   Treats nil values as absent, compatible with Pronto EDN representations."
  (m/-simple-schema
   {:type :oneof-edn
    :type-properties {:generator true}
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
                               ;; Exactly one of the specified fields must be present and non-nil
                               (let [active-fields (get-active-fields value)]
                                 (and (= 1 (count active-fields))
                                      ;; Validate the active field's value
                                      (let [active-field (first active-fields)
                                            validator (get field-validators active-field)]
                                        (validator (get value active-field)))))))
                  :min (* 2 (count field-map))
                  :max (* 2 (count field-map))
                  :type-properties properties
                  :error/fn (fn [{:keys [value]} _]
                              (cond
                                (not (map? value)) "must be a map"
                                :else
                                (let [active-fields (get-active-fields value)]
                                  (cond
                                    (zero? (count active-fields))
                                    "must have exactly one non-nil field set"
                                    (> (count active-fields) 1)
                                    (str "must have exactly one non-nil field set, found: "
                                         (vec active-fields))
                                    :else nil))))}))
    :explain (fn [this {:keys [value] :as context} _]
               (cond
                 (not (map? value))
                 [{:message "must be a map"
                   :type :oneof-edn}]
                 
                 :else
                 (let [properties (m/properties this)
                       children (m/children this)
                       field-map (if (and (empty? children) (map? properties))
                                   (dissoc properties :error/message)
                                   (apply hash-map children))
                       field-names (set (keys field-map))
                       active-fields (filter #(and (contains? field-names %)
                                                  (some? (get value %)))
                                            (keys value))]
                   (cond
                     (zero? (count active-fields))
                     [{:message "must have exactly one non-nil field set"
                       :type :oneof-edn}]
                     
                     (> (count active-fields) 1)
                     [{:message (str "must have exactly one non-nil field set, found: "
                                    (vec active-fields))
                       :type :oneof-edn}]
                     
                     :else nil))))}))

;; Generator for oneof-edn schemas
;; Efficient: generates exactly ONE field with a non-nil value
(defmethod mg/-schema-generator :oneof-edn [schema options]
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

(defn register-oneof-edn-schema!
  "Register the :oneof-edn schema type"
  []
  {:oneof-edn -oneof-edn-schema})