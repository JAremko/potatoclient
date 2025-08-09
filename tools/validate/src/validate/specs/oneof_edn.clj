(ns validate.specs.oneof-edn
  "Simple oneof schema for EDN data (not Pronto proto-maps)"
  (:require
   [malli.core :as m]
   [malli.generator :as mg]
   [clojure.test.check.generators :as gen]))

(def -oneof-edn-schema
  "Custom :oneof-edn schema for validating EDN maps with exactly one field set"
  (m/-simple-schema
   {:type :oneof-edn
    :type-properties {:generator true}
    :compile (fn [properties children options]
               (let [field-map (into {} (partition 2 children))
                     field-names (set (keys field-map))
                     field-validators (into {} (map (fn [[k v]]
                                                     [k (m/validator v options)])
                                                   field-map))]
                 {:pred (fn [value]
                          (and (map? value)
                               ;; Exactly one of the specified fields must be present
                               (= 1 (count (filter field-names (keys value))))
                               ;; And that field must validate
                               (let [present-field (first (filter field-names (keys value)))]
                                 (when present-field
                                   (let [validator (get field-validators present-field)]
                                     (validator (get value present-field)))))))
                  :min (count children)
                  :max (count children)
                  :type-properties properties
                  :error/fn (fn [{:keys [value]} _]
                              (cond
                                (not (map? value)) "must be a map"
                                (zero? (count (filter field-names (keys value))))
                                "must have exactly one field set"
                                (> (count (filter field-names (keys value))) 1)
                                (str "must have exactly one field set, found: "
                                     (vec (filter field-names (keys value))))
                                :else nil))}))
    :explain (fn [this {:keys [value]} _]
               (when (not (map? value))
                 [{:message "must be a map"
                   :type :oneof-edn}]))}))

;; Generator for oneof-edn schemas
(defmethod mg/-schema-generator :oneof-edn [schema options]
  (let [children (m/children schema)
        field-map (into {} (partition 2 children))]
    (if (empty? field-map)
      (gen/return {})
      ;; Generate exactly one field
      (gen/bind
       (gen/elements (keys field-map))
       (fn [field-name]
         (gen/fmap
          (fn [field-value]
            {field-name field-value})
          (mg/generator (get field-map field-name) options)))))))

(defn register-oneof-edn-schema!
  "Register the :oneof-edn schema type"
  []
  {:oneof-edn -oneof-edn-schema})