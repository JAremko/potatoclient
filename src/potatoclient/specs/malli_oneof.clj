(ns potatoclient.specs.malli-oneof
  "Custom Malli schema type for protobuf oneofs"
  (:require [malli.core :as m]
            [malli.generator :as mg]
            [clojure.test.check.generators :as gen]))

;; Custom :oneof schema type that efficiently represents protobuf oneofs
(def -oneof-schema
  (m/-simple-schema
    {:type :oneof
     :min 0  ; Can have properties without children
     :type-properties {:generator true}  ; Indicate we support generation
     :compile (fn [properties children options]
                ;; For protobuf oneofs, the field definitions come in properties, not children
                ;; Properties is a map like {:ping [:map [:ping :cmd/ping]], :rotary [:map [:rotary :cmd/root]]}
                (let [{:keys [error/message] :or {message "Exactly one field must be set"}} properties
                      field-map (if (and (empty? children) (map? properties))
                                  ;; Properties contains the field definitions
                                  (dissoc properties :error/message)
                                  ;; Fallback to old behavior if children are provided
                                  (if (seq children)
                                    (do
                                      (when (odd? (count children))
                                        (m/-fail! ::odd-number-of-children {:children children}))
                                      (into {} (partition 2 children)))
                                    ;; No children and no valid properties - this is an error
                                    {}))
                      _ (when (empty? field-map)
                          (m/-fail! ::no-fields {:properties properties :children children}))
                      field-names (vec (keys field-map))
                      field-schemas (vec (vals field-map))
                      ;; Pre-compile validators for each field schema
                      field-validators (mapv #(try
                                              (m/validator % options)
                                              (catch Exception e
                                                (constantly false)))
                                           field-schemas)]
                  {:pred (fn [m]
                           (let [is-map? (map? m)
                                 present-fields (when is-map? (filter #(contains? m %) field-names))
                                 field-count (count present-fields)]
                             (and is-map?
                                  ;; Exactly one field must be present
                                  (= 1 field-count)
                                  ;; And that field's value must be valid
                                  (when (= 1 field-count)
                                    (let [field-name (first present-fields)
                                          field-idx (.indexOf field-names field-name)
                                          field-validator (when (and (>= field-idx 0)
                                                                    (< field-idx (count field-validators)))
                                                           (nth field-validators field-idx))
                                          field-value (get m field-name)]
                                      (if field-validator
                                        (field-validator field-value)
                                        ;; If no validator compiled, just check if value exists
                                        true))))))
                   :type-properties {:error/message message
                                    :error/fields field-names}
                   :min 0  ; Properties-based oneofs have no children
                   :max 0  ; Properties-based oneofs have no children
                   :error/message message
                   :error/fn (fn [{:keys [value]} _]
                               (cond
                                 (not (map? value)) "must be a map"
                                 (zero? (count (filter #(contains? value %) field-names)))
                                 message
                                 (> (count (filter #(contains? value %) field-names)) 1)
                                 message
                                 :else nil))}))}))

;; Implement Malli's generator protocol for oneof schemas
(defmethod mg/-schema-generator :oneof [schema options]
  ;; The schema here is the actual compiled schema object
  (let [properties (m/properties schema)
        ;; Extract field definitions from properties
        field-map (dissoc properties :error/message)
        field-names (vec (keys field-map))
        field-schemas (vec (vals field-map))]
    (if (empty? field-names)
      ;; No fields to generate
      (gen/return {})
      ;; Generate exactly one field
      (gen/bind
        ;; First, choose which field to generate
        (gen/elements (range (count field-names)))
        (fn [idx]
          (let [field-name (nth field-names idx)
                field-schema (nth field-schemas idx)]
            ;; Then generate value for that field
            (gen/fmap (fn [value]
                        {field-name value})
                      (mg/generator field-schema options))))))))

;; Helper function to create oneof specs
(defn oneof
  "Create a oneof spec from field definitions.
   
   Usage:
   (oneof [:ping [:map]
           :rotary [:ref :cmd/Rotary/Root]
           :cv [:ref :cmd/CV/Root]]
          {:error/message \"Exactly one command must be set\"})"
  ([fields] (oneof fields {}))
  ([fields props]
   (into [:oneof props] fields)))

;; Register the schema type globally (optional - can also use in local registries)
(defn register-oneof-schema!
  "Register the :oneof schema type in the default registry"
  []
  ;; For now, we'll use it in local registries rather than global registration
  ;; Users can include it in their registry like:
  ;; {:registry (merge (m/default-schemas) {:oneof -oneof-schema})}
  -oneof-schema)