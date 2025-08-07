(ns generator.validation-helpers
  "Generate validation helper functions for fields with constraints"
  (:require [clojure.string :as str]
            [potatoclient.proto.conversion :as conv]))

;; =============================================================================
;; Helper Functions
;; =============================================================================

(defn field-has-constraints?
  "Check if a field has any constraints"
  [field]
  (seq (:constraints field)))

(defn constraint-description
  "Generate human-readable description of constraints"
  [constraints]
  (cond
    ;; Numeric constraints
    (and (:gt constraints) (:lt constraints))
    (str "must be > " (:gt constraints) " and < " (:lt constraints))
    
    (and (:gte constraints) (:lte constraints))
    (str "must be >= " (:gte constraints) " and <= " (:lte constraints))
    
    (:gt constraints)
    (str "must be > " (:gt constraints))
    
    (:gte constraints)
    (str "must be >= " (:gte constraints))
    
    (:lt constraints)
    (str "must be < " (:lt constraints))
    
    (:lte constraints)
    (str "must be <= " (:lte constraints))
    
    ;; String constraints
    (:min-len constraints)
    (str "minimum length " (:min-len constraints))
    
    (:max-len constraints)
    (str "maximum length " (:max-len constraints))
    
    (:pattern constraints)
    (str "must match pattern: " (:pattern constraints))
    
    ;; Collection constraints
    (:min-items constraints)
    (str "minimum " (:min-items constraints) " items")
    
    (:max-items constraints)
    (str "maximum " (:max-items constraints) " items")
    
    :else
    "has validation constraints"))

(defn field->validation-fn-name
  "Generate validation function name for a field"
  [field message-name]
  (str "valid-" (name message-name) "-" (name (:name field)) "?"))

(defn field->spec-name
  "Generate spec def name for a field with constraints"
  [field message]
  (str (name (conv/string->keyword (:proto-name message))) "-" (name (:name field)) "-spec"))

;; =============================================================================
;; Code Generation
;; =============================================================================

(defn generate-field-spec-def
  "Generate a spec definition for a field with constraints"
  [field message spec-schema]
  (let [spec-name (field->spec-name field message)]
    (str "(def " spec-name "\n"
         "  \"Validation spec for " (name (:name field)) " field\"\n"
         "  " (pr-str spec-schema) ")")))

(defn generate-validation-helper
  "Generate a single validation helper function"
  [field message spec-schema]
  (let [fn-name (field->validation-fn-name field (:name message))
        spec-name (field->spec-name field message)
        desc (constraint-description (:constraints field))]
    (str ";; " (:proto-name field) " field"
         (when desc (str " - " desc)) "\n"
         (generate-field-spec-def field message spec-schema) "\n\n"
         "(defn " fn-name "\n"
         "  \"Validate " (name (:name field)) " - " (or desc "has constraints") "\"\n"
         "  [value]\n"
         "  (m/validate " spec-name " value))")))

(defn extract-field-spec
  "Extract the spec for a field from the message spec"
  [field message-spec]
  ;; Message spec is [:map [:field1 spec1] [:field2 spec2] ...]
  ;; Find the field spec in the map schema
  (when (and (vector? message-spec) 
             (= :map (first message-spec)))
    (some (fn [field-def]
            (when (and (vector? field-def)
                       (>= (count field-def) 2)
                       (= (:name field) (first field-def)))
              (second field-def)))
          (rest message-spec))))

(defn generate-validation-helpers
  "Generate all validation helper functions for a message"
  [message message-spec]
  (let [constrained-fields (filter field-has-constraints? (:fields message))]
    (when (seq constrained-fields)
      (str ";; Validation helpers for " (:proto-name message) "\n"
           (str/join "\n\n" 
                     (map (fn [field]
                            (if-let [field-spec (extract-field-spec field message-spec)]
                              (generate-validation-helper field message field-spec)
                              (str ";; Warning: Could not extract spec for field " 
                                   (name (:name field)))))
                          constrained-fields))))))

(defn generate-namespace-validation-helpers
  "Generate all validation helpers for messages in a namespace"
  [messages]
  ;; For now, we'll use a simplified approach without the actual specs
  ;; In a real implementation, we'd need to pass the generated specs along
  (let [helpers (keep (fn [message]
                       (generate-validation-helpers message nil))
                     messages)]
    (when (seq helpers)
      (str "\n;; =============================================================================\n"
           ";; Validation Helper Functions\n" 
           ";; =============================================================================\n\n"
           (str/join "\n\n" helpers)))))