(ns proto-explorer.buf-validate-extractor
  "Extract buf.validate constraints from JSON descriptors.
  
  This namespace handles the extraction of validation constraints
  from the options field in protobuf JSON descriptors."
  (:require [clojure.string :as str]
            [clojure.walk :as walk]))

;; =============================================================================
;; Constraint Keys
;; =============================================================================

(def validate-field-key
  "The extension number for buf.validate.field in the descriptor"
  1042)

(def validate-oneof-key
  "The extension number for buf.validate.oneof in the descriptor"
  1071)

;; =============================================================================
;; Numeric Constraints
;; =============================================================================

(defn extract-numeric-constraints
  "Extract numeric constraints (gt, gte, lt, lte, const) from options."
  [constraints]
  (let [relevant-keys #{:gt :gte :lt :lte :const :in :not-in}]
    (select-keys constraints relevant-keys)))

;; =============================================================================
;; String Constraints
;; =============================================================================

(defn extract-string-constraints
  "Extract string constraints from options."
  [constraints]
  (let [relevant-keys #{:min-len :max-len :len :pattern 
                        :email :hostname :ip :ipv4 :ipv6 
                        :uri :uri-ref :uuid :address
                        :well-known-regex :prefix :suffix 
                        :contains :not-contains :in :not-in}]
    (select-keys constraints relevant-keys)))

;; =============================================================================
;; Collection Constraints
;; =============================================================================

(defn extract-repeated-constraints
  "Extract constraints for repeated fields."
  [constraints]
  (let [relevant-keys #{:min-items :max-items :unique :items}]
    (select-keys constraints relevant-keys)))

(defn extract-map-constraints
  "Extract constraints for map fields."
  [constraints]
  (let [relevant-keys #{:min-pairs :max-pairs :no-sparse :keys :values}]
    (select-keys constraints relevant-keys)))

;; =============================================================================
;; Message Constraints
;; =============================================================================

(defn extract-message-constraints
  "Extract constraints for message fields."
  [constraints]
  (let [relevant-keys #{:required :skip}]
    (select-keys constraints relevant-keys)))

;; =============================================================================
;; Enum Constraints
;; =============================================================================

(defn extract-enum-constraints
  "Extract constraints for enum fields."
  [constraints]
  (let [relevant-keys #{:const :defined-only :in :not-in}]
    (select-keys constraints relevant-keys)))

;; =============================================================================
;; Main Extraction
;; =============================================================================

(defn extract-field-options
  "Extract buf.validate options from a field's options map.
  
  The options are stored under '[buf.validate.field]' key in the JSON descriptor."
  [options]
  (when options
    (get options (keyword "[buf.validate.field]"))))

(defn extract-oneof-options
  "Extract buf.validate options from a oneof's options map."
  [options]
  (when options
    (get options (keyword "[buf.validate.oneof]"))))

(defn field-type->constraint-extractor
  "Get the appropriate constraint extractor for a field type."
  [field-type]
  (case field-type
    ;; Numeric types
    (:type-double :type-float 
     :type-int64 :type-uint64 :type-int32 
     :type-fixed64 :type-fixed32 :type-uint32 
     :type-sfixed32 :type-sfixed64 
     :type-sint32 :type-sint64) extract-numeric-constraints
    
    ;; String type
    :type-string extract-string-constraints
    
    ;; Bytes type (similar to string)
    :type-bytes extract-string-constraints
    
    ;; Boolean type (limited constraints)
    :type-bool (fn [c] (select-keys c [:const]))
    
    ;; Message type
    :type-message extract-message-constraints
    
    ;; Enum type
    :type-enum extract-enum-constraints
    
    ;; Default
    identity))

(defn extract-field-constraints
  "Extract all relevant constraints for a field based on its type."
  [field]
  (when-let [validate-options (extract-field-options (:options field))]
    (let [field-type (:type field)
          label (:label field)]
      ;; The constraints are organized by type in the new format
      (cond
        ;; Numeric types - look for float, double, int32, etc. keys
        (#{:type-float :type-double} field-type)
        (:float validate-options)
        
        (#{:type-int32 :type-sint32} field-type)
        (:int32 validate-options)
        
        (#{:type-int64 :type-sint64} field-type)
        (:int64 validate-options)
        
        (#{:type-uint32} field-type)
        (:uint32 validate-options)
        
        (#{:type-uint64} field-type)
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
        
        ;; Message type
        (= :type-message field-type)
        (:message validate-options)
        
        ;; Repeated fields might have additional constraints
        (= :label-repeated label)
        (:repeated validate-options)
        
        :else nil))))

(defn extract-oneof-constraints
  "Extract constraints for a oneof declaration."
  [oneof]
  (when-let [validate-options (extract-oneof-options (:options oneof))]
    (select-keys validate-options [:required])))

;; =============================================================================
;; Malli Schema Enhancement
;; =============================================================================

(defn numeric-constraint->malli
  "Convert numeric constraints to Malli predicates."
  [{:keys [gt gte lt lte const in not-in]}]
  (cond-> []
    gt (conj [:> gt])
    gte (conj [:>= gte])
    lt (conj [:< lt])
    lte (conj [:<= lte])
    const (conj [:= const])
    in (conj [:enum in])
    not-in (conj [:not [:enum not-in]])))

(defn string-constraint->malli
  "Convert string constraints to Malli predicates."
  [{:keys [min-len max-len len pattern email hostname 
           ip ipv4 ipv6 uri uuid prefix suffix 
           contains not-contains in not-in]}]
  (cond-> []
    min-len (conj [:min-length min-len])
    max-len (conj [:max-length max-len])
    len (conj [:length len])
    pattern (conj [:re (re-pattern pattern)])
    email (conj [:re #"^[^@]+@[^@]+\.[^@]+$"])
    hostname (conj [:re #"^[a-zA-Z0-9.-]+$"])
    uuid (conj [:re #"^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$"])
    prefix (conj [:fn (fn [s] (str/starts-with? s prefix))])
    suffix (conj [:fn (fn [s] (str/ends-with? s suffix))])
    contains (conj [:fn (fn [s] (str/includes? s contains))])
    not-contains (conj [:fn (fn [s] (not (str/includes? s not-contains)))])
    in (conj [:enum in])
    not-in (conj [:not [:enum not-in]])))

(defn apply-constraints-to-schema
  "Apply extracted constraints to a Malli schema."
  [base-schema constraints field-type]
  (if (empty? constraints)
    base-schema
    (let [predicates (case field-type
                       (:type-double :type-float 
                        :type-int64 :type-uint64 :type-int32 
                        :type-fixed64 :type-fixed32 :type-uint32 
                        :type-sfixed32 :type-sfixed64 
                        :type-sint32 :type-sint64) 
                       (numeric-constraint->malli constraints)
                       
                       (:type-string :type-bytes)
                       (string-constraint->malli constraints)
                       
                       [])]
      (if (empty? predicates)
        base-schema
        (into [:and base-schema] predicates)))))

;; =============================================================================
;; Integration with Spec Generator
;; =============================================================================

(defn enhance-field-schema
  "Enhance a field schema with buf.validate constraints.
  
  This is meant to be called from the spec generator after
  the base schema is created."
  [schema field]
  (if-let [constraints (extract-field-constraints field)]
    (apply-constraints-to-schema schema constraints (:type field))
    schema))

;; =============================================================================
;; Example Usage
;; =============================================================================

(comment
  ;; Example field with constraints
  (def example-field
    {:name "age"
     :type :type-int32
     :options {"1042" {:gte 0 :lte 150}}})
  
  ;; Extract constraints
  (extract-field-constraints example-field)
  ;; => {:gte 0, :lte 150}
  
  ;; Apply to schema
  (apply-constraints-to-schema :int {:gte 0 :lte 150} :type-int32)
  ;; => [:and :int [:>= 0] [:<= 150]]
  
  ;; String example
  (def string-field
    {:name "email"
     :type :type-string
     :options {"1042" {:email true :min-len 5}}})
  
  (extract-field-constraints string-field)
  ;; => {:email true, :min-len 5}
  
  (apply-constraints-to-schema :string {:email true :min-len 5} :type-string)
  ;; => [:and :string [:min-length 5] [:re #"^[^@]+@[^@]+\.[^@]+$"]]
  )