(ns cmd-explorer.specs.oneof-payload
  "Custom Malli schema type for protobuf oneofs adapted for Pronto proto-maps.
   Unlike regular Clojure maps, Pronto proto-maps have all fields present with defaults."
  (:require
   [malli.core :as m]
   [malli.generator :as mg]
   [clojure.test.check.generators :as gen]
   [pronto.core :as p]))

(def -oneof-pronto-schema
  "Custom :oneof-pronto schema for validating Pronto proto-maps with oneof constraints."
  (m/-simple-schema
   {:type :oneof-pronto
    :min 0  ; Can have properties without children
    :type-properties {:generator true}
    :compile (fn [properties children options]
               (let [{:keys [error/message proto-class proto-mapper]
                      :or {message "Exactly one field must be set"}} properties
                     _ (when-not proto-class
                         (m/-fail! ::missing-proto-class {:properties properties}))
                     _ (when-not proto-mapper
                         (m/-fail! ::missing-proto-mapper {:properties properties}))
                     field-map (if (and (empty? children) (map? properties))
                                 (dissoc properties :error/message :proto-class :proto-mapper)
                                 (if (seq children)
                                   (do
                                     (when (odd? (count children))
                                       (m/-fail! ::odd-number-of-children {:children children}))
                                     (into {} (partition 2 children)))
                                   {}))
                     _ (when (empty? field-map)
                         (m/-fail! ::no-fields {:properties properties :children children}))
                     field-names (vec (keys field-map))
                     field-schemas (vec (vals field-map))
                     field-validators (mapv #(try
                                              (m/validator % options)
                                              (catch Exception e
                                                (constantly false)))
                                          field-schemas)]
                 {:pred (fn [proto-map]
                          (try
                            ;; Check if it's a Pronto proto-map
                            (when (p/proto-map? proto-map)
                              ;; Use Pronto's which-one-of to check active field
                              (let [active-field (p/which-one-of proto-map)
                                    field-idx (when active-field
                                              (.indexOf field-names active-field))]
                                (and
                                 ;; Exactly one field must be active
                                 (some? active-field)
                                 (>= field-idx 0)
                                 (< field-idx (count field-validators))
                                 ;; Validate the active field's value
                                 (let [field-value (p/one-of proto-map active-field)
                                       field-validator (nth field-validators field-idx)]
                                   (if field-validator
                                     (field-validator field-value)
                                     true)))))
                            (catch Exception e
                              false)))
                  :type-properties {:error/message message
                                   :error/fields field-names
                                   :proto-class proto-class
                                   :proto-mapper proto-mapper}
                  :min 0
                  :max 0
                  :error/message message
                  :error/fn (fn [{:keys [value]} _]
                              (cond
                                (not (p/proto-map? value)) "must be a Pronto proto-map"
                                :else
                                (try
                                  (let [active-field (p/which-one-of value)]
                                    (cond
                                      (nil? active-field) message
                                      (not (contains? (set field-names) active-field))
                                      (str "Unknown field: " active-field)
                                      :else nil))
                                  (catch Exception e
                                    (.getMessage e)))))}))
    :explain (fn [this {:keys [value]} _]
               (when-not (p/proto-map? value)
                 [{:message "must be a Pronto proto-map"
                   :type :oneof-pronto}]))}))

;; Generator for oneof-pronto schemas
(defmethod mg/-schema-generator :oneof-pronto [schema options]
  (let [properties (m/properties schema)
        proto-class (:proto-class properties)
        proto-mapper (:proto-mapper properties)
        field-map (dissoc properties :error/message :proto-class :proto-mapper)
        field-names (vec (keys field-map))
        field-schemas (vec (vals field-map))]
    (if (or (empty? field-names) (nil? proto-class) (nil? proto-mapper))
      (gen/return nil)
      ;; Generate exactly one field
      (gen/bind
       ;; Choose which field to generate
       (gen/elements (range (count field-names)))
       (fn [idx]
         (let [field-name (nth field-names idx)
               field-schema (nth field-schemas idx)]
           ;; Generate value for that field and create proto-map
           (gen/fmap (fn [value]
                       ;; Create proto-map with the single field set
                       (p/proto-map proto-mapper proto-class field-name value))
                     (mg/generator field-schema options))))))))

(defn oneof-pronto
  "Create a oneof-pronto spec for Pronto proto-maps.
   
   Usage:
   (oneof-pronto {:proto-class JonCommand
                  :proto-mapper mapper
                  :ping [:map]
                  :rotary [:ref :cmd/Rotary/Root]
                  :cv [:ref :cmd/CV/Root]
                  :error/message \"Exactly one command must be set\"})"
  [props]
  [:oneof-pronto props])

(defn register-oneof-pronto-schema!
  "Register the :oneof-pronto schema type in the given registry or return for inclusion."
  []
  -oneof-pronto-schema)