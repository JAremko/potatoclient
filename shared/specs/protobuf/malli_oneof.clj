(ns proto-explorer.malli-oneof
  "Custom Malli schema type for protobuf oneofs"
  (:require [malli.core :as m]
            [malli.generator :as mg]
            [clojure.test.check.generators :as gen]))

;; Custom :oneof schema type that efficiently represents protobuf oneofs
(def -oneof-schema
  (m/-simple-schema
    {:type :oneof
     :min 0  ; Can have properties without children  
     :compile (fn [properties children options]
                ;; Check that we have an even number of children (field-name/spec pairs)
                (when (odd? (count children))
                  (m/-fail! ::odd-number-of-children {:children children}))
                ;; Check that we have at least one field
                (when (zero? (count children))
                  (m/-fail! ::no-children {:children children}))
                (let [{:keys [error/message] :or {message "Exactly one field must be set"}} properties
                      ;; Children are like [:field-1 spec-1, :field-2 spec-2, ...]
                      field-pairs (partition 2 children)
                      field-names (mapv first field-pairs)
                      field-schemas (mapv second field-pairs)
                      ;; Pre-compile validators for each field schema
                      field-validators (mapv #(m/validator % options) field-schemas)
                      ;; Pre-compile generators for each field schema
                      field-generators (mapv #(mg/generator % options) field-schemas)
                      ;; Create the combined generator
                      combined-generator (gen/bind 
                                          (gen/elements (range (count field-names)))
                                          (fn [idx]
                                            (let [field-name (nth field-names idx)
                                                  field-gen (nth field-generators idx)]
                                              (gen/fmap (fn [value]
                                                         {field-name value})
                                                       field-gen))))]
                  {:pred (fn [m]
                           (and (map? m)
                                ;; Exactly one field must be present
                                (= 1 (count (filter #(contains? m %) field-names)))
                                ;; And that field's value must be valid
                                (let [present-fields (filter #(contains? m %) field-names)]
                                  (when (= 1 (count present-fields))
                                    (let [field-name (first present-fields)
                                          field-idx (.indexOf field-names field-name)
                                          field-validator (nth field-validators field-idx)
                                          field-value (get m field-name)]
                                      (field-validator field-value))))))
                   :type-properties {:error/message message
                                    :error/fields field-names}
                   :gen (fn [_self _options]
                          ;; Return the pre-compiled generator
                          combined-generator)
                   :min (count children)  ; Accept exactly the number of children provided
                   :max (count children)  ; Accept exactly the number of children provided
                   :error/message message
                   :error/fn (fn [{:keys [value]} _]
                               (cond
                                 (not (map? value)) "must be a map"
                                 (zero? (count (filter #(contains? value %) field-names)))
                                 message
                                 (> (count (filter #(contains? value %) field-names)) 1)
                                 message
                                 :else nil))}))}))

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

;; Example usage and tests
(comment
  ;; Register the schema
  (register-oneof-schema!)
  
  ;; Create a oneof spec
  (def cmd-oneof
    (oneof [:ping [:map]
            :rotary [:map [:set_velocity [:map [:azimuth :double]]]]
            :cv [:map [:start_track [:map [:x :double] [:y :double]]]]]
           {:error/message "Exactly one command must be set"}))
  
  ;; Validate data
  (m/validate cmd-oneof {:ping {}})  ; => true
  (m/validate cmd-oneof {:rotary {:set_velocity {:azimuth 0.5}}})  ; => true
  (m/validate cmd-oneof {:ping {} :rotary {}})  ; => false (multiple fields)
  (m/validate cmd-oneof {})  ; => false (no fields)
  
  ;; Generate data - always produces exactly one field
  (mg/generate cmd-oneof)  ; => {:cv {:start_track {:x 0.123 :y -0.456}}}
  
  ;; Get error messages
  (-> (m/explain cmd-oneof {:ping {} :rotary {}})
      (m/error-messages))  ; => {:oneof ["Exactly one command must be set"]}
  
  ;; Use in larger specs
  (def full-message-spec
    [:map
     [:protocol_version :int]
     [:command cmd-oneof]])
  
  (mg/generate full-message-spec)
  ; => {:protocol_version 42, :command {:ping {}}}
  )