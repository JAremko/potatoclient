(ns generator.constraints.extractor
  "Extract buf.validate constraints from JSON descriptors.
  
  This namespace handles the extraction of validation constraints
  from the options field in protobuf JSON descriptors. 
  
  Ported from proto-explorer's buf_validate_extractor.clj"
  (:require [clojure.string :as str]
            [clojure.set]))

;; =============================================================================
;; Constraint Extraction
;; =============================================================================

(defn extract-field-options
  "Extract buf.validate options from a field's options map.
  
  The options are stored under '[buf.validate.field]' key in the JSON descriptor."
  [options]
  (when options
    ;; Try both forms of the key that might appear
    (or (get options "[buf.validate.field]")
        (get options (keyword "[buf.validate.field]")))))

(defn extract-oneof-options
  "Extract buf.validate options from a oneof's options map."
  [options]
  (when options
    (or (get options "[buf.validate.oneof]")
        (get options (keyword "[buf.validate.oneof]")))))

(defn extract-field-constraints
  "Extract all relevant constraints for a field based on its type.
  
  Returns the constraint map for the specific field type, or nil if no constraints."
  [field]
  (when-let [validate-options (extract-field-options (:options field))]
    (let [field-type (:type field)
          label (:label field)]
      ;; The constraints are organized by type in the buf.validate format
      (cond
        ;; Numeric types - look for float, double, int32, etc. keys
        (= :type-float field-type)
        (:float validate-options)
        
        (= :type-double field-type)
        (:double validate-options)
        
        (#{:type-int32 :type-sint32 :type-sfixed32} field-type)
        (:int32 validate-options)
        
        (#{:type-int64 :type-sint64 :type-sfixed64} field-type)
        (:int64 validate-options)
        
        (#{:type-uint32 :type-fixed32} field-type)
        (:uint32 validate-options)
        
        (#{:type-uint64 :type-fixed64} field-type)
        (:uint64 validate-options)
        
        ;; String type
        (= :type-string field-type)
        (:string validate-options)
        
        ;; Bytes type
        (= :type-bytes field-type)
        (:bytes validate-options)
        
        ;; Boolean type
        (= :type-bool field-type)
        (:bool validate-options)
        
        ;; Enum type
        (= :type-enum field-type)
        (:enum validate-options)
        
        ;; Message type - check for message-level constraints
        (= :type-message field-type)
        (:message validate-options)
        
        ;; Default: check if we have uint32 or other constraints at top level
        :else 
        (or (:uint32 validate-options)
            (:uint64 validate-options)
            (:int32 validate-options)
            (:int64 validate-options)
            (:float validate-options)
            (:double validate-options)
            (:string validate-options)
            (:bytes validate-options)
            (:bool validate-options)
            (:enum validate-options)
            (:message validate-options)
            (:repeated validate-options)
            validate-options)))))

(defn extract-repeated-constraints
  "Extract constraints specific to repeated fields."
  [field]
  (when (= (:label field) :label-repeated)
    (when-let [validate-options (extract-field-options (:options field))]
      (:repeated validate-options))))

(defn extract-map-constraints
  "Extract constraints specific to map fields."
  [field]
  (when-let [validate-options (extract-field-options (:options field))]
    (:map validate-options)))

(defn extract-oneof-constraints
  "Extract constraints for a oneof declaration."
  [oneof]
  (when-let [validate-options (extract-oneof-options (:options oneof))]
    validate-options))

;; =============================================================================
;; Constraint Normalization
;; =============================================================================

(defn normalize-numeric-constraints
  "Normalize numeric constraints to a consistent format."
  [constraints]
  (when constraints
    (let [relevant-keys #{:gt :gte :lt :lte :const :in :not_in}]
      (-> constraints
          (select-keys relevant-keys)
          ;; Convert not_in to not-in for consistency
          (clojure.set/rename-keys {:not_in :not-in})))))

(defn normalize-string-constraints
  "Normalize string constraints to a consistent format."
  [constraints]
  (when constraints
    (let [relevant-keys #{:min_len :max_len :len :pattern 
                         :email :hostname :ip :ipv4 :ipv6 
                         :uri :uri_ref :uuid :address
                         :well_known_regex :prefix :suffix 
                         :contains :not_contains :in :not_in}]
      (-> constraints
          (select-keys relevant-keys)
          ;; Convert underscores to hyphens
          (clojure.set/rename-keys {:min_len :min-len
                                   :max_len :max-len
                                   :not_contains :not-contains
                                   :not_in :not-in
                                   :uri_ref :uri-ref
                                   :well_known_regex :well-known-regex})))))

(defn normalize-collection-constraints
  "Normalize collection constraints to a consistent format."
  [constraints]
  (when constraints
    (let [relevant-keys #{:min_items :max_items :unique :items}]
      (-> constraints
          (select-keys relevant-keys)
          (clojure.set/rename-keys {:min_items :min-items
                                   :max_items :max-items})))))

(defn normalize-map-constraints
  "Normalize map constraints to a consistent format."
  [constraints]
  (when constraints
    (let [relevant-keys #{:min_pairs :max_pairs :no_sparse :keys :values}]
      (-> constraints
          (select-keys relevant-keys)
          (clojure.set/rename-keys {:min_pairs :min-pairs
                                   :max_pairs :max-pairs
                                   :no_sparse :no-sparse})))))

(defn normalize-message-constraints
  "Normalize message constraints to a consistent format."
  [constraints]
  (when constraints
    (select-keys constraints #{:required :skip})))

(defn normalize-enum-constraints
  "Normalize enum constraints to a consistent format."
  [constraints]
  (when constraints
    (let [relevant-keys #{:const :defined_only :in :not_in}]
      (-> constraints
          (select-keys relevant-keys)
          (clojure.set/rename-keys {:defined_only :defined-only
                                   :not_in :not-in})))))

;; =============================================================================
;; Main API
;; =============================================================================

(defn extract-and-normalize-constraints
  "Extract and normalize all constraints for a field.
  
  Returns a map with:
  - :field-constraints - The main field type constraints (normalized)
  - :repeated-constraints - Constraints for repeated fields (if applicable)
  - :raw - The raw constraint data for debugging"
  [field]
  (let [field-constraints (extract-field-constraints field)
        repeated-constraints (extract-repeated-constraints field)
        field-type (:type field)]
    (when (or field-constraints repeated-constraints)
      (cond-> {}
        field-constraints
        (assoc :field-constraints
               (case field-type
                 ;; Numeric types
                 (:type-float :type-double
                  :type-int32 :type-sint32 :type-sfixed32
                  :type-int64 :type-sint64 :type-sfixed64
                  :type-uint32 :type-fixed32
                  :type-uint64 :type-fixed64)
                 (normalize-numeric-constraints field-constraints)
                 
                 ;; String type
                 :type-string
                 (normalize-string-constraints field-constraints)
                 
                 ;; Bytes type
                 :type-bytes
                 (normalize-string-constraints field-constraints)
                 
                 ;; Boolean type
                 :type-bool
                 (select-keys field-constraints #{:const})
                 
                 ;; Enum type
                 :type-enum
                 (normalize-enum-constraints field-constraints)
                 
                 ;; Message type
                 :type-message
                 (normalize-message-constraints field-constraints)
                 
                 ;; Default - try to detect the type from the constraint keys
                 (cond
                   ;; Check if it's a map with known constraint types
                   (and (map? field-constraints)
                        (or (contains? field-constraints :uint32)
                            (contains? field-constraints :uint64)
                            (contains? field-constraints :int32)
                            (contains? field-constraints :int64)
                            (contains? field-constraints :float)
                            (contains? field-constraints :double)))
                   (or (:uint32 field-constraints)
                       (:uint64 field-constraints)
                       (:int32 field-constraints)
                       (:int64 field-constraints)
                       (:float field-constraints)
                       (:double field-constraints))
                   
                   :else field-constraints)))
        
        repeated-constraints
        (assoc :repeated-constraints
               (normalize-collection-constraints repeated-constraints))
        
        ;; Always include raw for debugging
        (or field-constraints repeated-constraints)
        (assoc :raw {:field field-constraints
                     :repeated repeated-constraints})))))

(defn field-has-constraints?
  "Check if a field has any buf.validate constraints."
  [field]
  (or (some? (extract-field-options (:options field)))
      (and (= (:label field) :label-repeated)
           (some? (extract-repeated-constraints field)))))

(defn extract-all-constraints
  "Extract constraints from all fields in a message.
  
  Returns a map of field names to their constraints."
  [message]
  (reduce (fn [acc field]
            (if-let [constraints (extract-and-normalize-constraints field)]
              (assoc acc (:name field) constraints)
              acc))
          {}
          (:fields message [])))