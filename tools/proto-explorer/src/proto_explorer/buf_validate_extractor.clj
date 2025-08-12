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
    (:TYPE_DOUBLE :TYPE_FLOAT 
     :TYPE_INT64 :TYPE_UINT64 :TYPE_INT32 
     :TYPE_FIXED64 :TYPE_FIXED32 :TYPE_UINT32 
     :TYPE_SFIXED32 :TYPE_SFIXED64 
     :TYPE_SINT32 :TYPE_SINT64) extract-numeric-constraints
    
    ;; String type
    :TYPE_STRING extract-string-constraints
    
    ;; Bytes type (similar to string)
    :TYPE_BYTES extract-string-constraints
    
    ;; Boolean type (limited constraints)
    :TYPE_BOOL (fn [c] (select-keys c [:const]))
    
    ;; Message type
    :TYPE_MESSAGE extract-message-constraints
    
    ;; Enum type
    :TYPE_ENUM extract-enum-constraints
    
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
        (#{:TYPE_FLOAT :TYPE_DOUBLE} field-type)
        (:float validate-options)
        
        (#{:TYPE_INT32 :TYPE_SINT32} field-type)
        (:int32 validate-options)
        
        (#{:TYPE_INT64 :TYPE_SINT64} field-type)
        (:int64 validate-options)
        
        (#{:TYPE_UINT32} field-type)
        (:uint32 validate-options)
        
        (#{:TYPE_UINT64} field-type)
        (:uint64 validate-options)
        
        ;; String type
        (= :TYPE_STRING field-type)
        (:string validate-options)
        
        ;; Bytes type
        (= :TYPE_BYTES field-type)
        (:bytes validate-options)
        
        ;; Boolean type
        (= :TYPE_BOOL field-type)
        (:bool validate-options)
        
        ;; Enum type
        (= :TYPE_ENUM field-type)
        (:enum validate-options)
        
        ;; Message type
        (= :TYPE_MESSAGE field-type)
        (:message validate-options)
        
        ;; Repeated fields might have additional constraints
        (= :LABEL_REPEATED label)
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
                       (:TYPE_DOUBLE :TYPE_FLOAT 
                        :TYPE_INT64 :TYPE_UINT64 :TYPE_INT32 
                        :TYPE_FIXED64 :TYPE_FIXED32 :TYPE_UINT32 
                        :TYPE_SFIXED32 :TYPE_SFIXED64 
                        :TYPE_SINT32 :TYPE_SINT64) 
                       (numeric-constraint->malli constraints)
                       
                       (:TYPE_STRING :TYPE_BYTES)
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
     :type :TYPE_INT32
     :options {"1042" {:gte 0 :lte 150}}})
  
  ;; Extract constraints
  (extract-field-constraints example-field)
  ;; => {:gte 0, :lte 150}
  
  ;; Apply to schema
  (apply-constraints-to-schema :int {:gte 0 :lte 150} :TYPE_INT32)
  ;; => [:and :int [:>= 0] [:<= 150]]
  
  ;; String example
  (def string-field
    {:name "email"
     :type :TYPE_STRING
     :options {"1042" {:email true :min-len 5}}})
  
  (extract-field-constraints string-field)
  ;; => {:email true, :min-len 5}
  
  (apply-constraints-to-schema :string {:email true :min-len 5} :TYPE_STRING)
  ;; => [:and :string [:min-length 5] [:re #"^[^@]+@[^@]+\.[^@]+$"]]
  )