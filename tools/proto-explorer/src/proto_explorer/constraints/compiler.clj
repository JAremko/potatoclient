(ns proto-explorer.constraints.compiler
  "Constraint compiler that transforms buf.validate constraints into Malli schemas.
  
  Uses multimethod dispatch on [constraint-type spec-type] to handle different
  combinations of constraints and target schema types."
  (:require [clojure.core.match :refer [match]]
            [malli.generator :as mg]))

;; =============================================================================
;; Multimethod Dispatch
;; =============================================================================

(defmulti compile-constraint
  "Compile a constraint into Malli schema additions.
  
  Dispatches on [constraint-type spec-type] where:
  - constraint-type: :float, :int32, :string, :enum, etc.
  - spec-type: :float, :int, :string, :keyword, etc.
  
  Returns a map with:
  - :schema - Additional schema elements to merge
  - :generator - Optional generator constraints
  - :predicates - Raw predicate functions if needed"
  (fn [constraint-type spec-type constraint-value]
    [constraint-type spec-type]))

;; =============================================================================
;; Numeric Constraints
;; =============================================================================

(defmethod compile-constraint [:float :double]
  [_ _ constraints]
  (let [schema-parts (cond-> []
                       (:gt constraints) (conj [:> (:gt constraints)])
                       (:gte constraints) (conj [:>= (:gte constraints)])
                       (:lt constraints) (conj [:< (:lt constraints)])
                       (:lte constraints) (conj [:<= (:lte constraints)])
                       (:const constraints) (conj [:= (:const constraints)]))]
    {:schema schema-parts
     :generator (when (or (:gte constraints) (:gt constraints) 
                         (:lte constraints) (:lt constraints))
                  {:min (or (:gte constraints) 
                           (when (:gt constraints) 
                             (+ (:gt constraints) 0.0001)))
                   :max (or (:lte constraints)
                           (when (:lt constraints)
                             (- (:lt constraints) 0.0001)))})}))

(defmethod compile-constraint [:int32 :int]
  [_ _ constraints]
  (let [schema-parts (cond-> []
                       (:gt constraints) (conj [:> (:gt constraints)])
                       (:gte constraints) (conj [:>= (:gte constraints)])
                       (:lt constraints) (conj [:< (:lt constraints)])
                       (:lte constraints) (conj [:<= (:lte constraints)])
                       (:const constraints) (conj [:= (:const constraints)])
                       (:in constraints) (conj [:enum (:in constraints)])
                       (:not-in constraints) (conj [:not [:enum (:not-in constraints)]]))]
    {:schema schema-parts
     :generator (when (or (:gte constraints) (:gt constraints)
                         (:lte constraints) (:lt constraints))
                  {:min (or (:gte constraints)
                           (when (:gt constraints)
                             (inc (:gt constraints))))
                   :max (or (:lte constraints)
                           (when (:lt constraints)
                             (dec (:lt constraints))))})}))

;; =============================================================================
;; String Constraints
;; =============================================================================

(defmethod compile-constraint [:string :string]
  [_ _ constraints]
  (let [;; Handle string length constraints with props
        props (cond-> {}
                (:min-len constraints) (assoc :min (:min-len constraints))
                (:max-len constraints) (assoc :max (:max-len constraints))
                (:len constraints) (assoc :min (:len constraints)
                                         :max (:len constraints)))
        ;; Other constraints as additional schemas
        schema-parts (cond-> []
                       (:pattern constraints)
                       (conj [:re (re-pattern (:pattern constraints))])
                       
                       (:email constraints)
                       (conj [:re #"^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$"])
                       
                       (:uuid constraints)
                       (conj [:re #"^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$"])
                       
                       (:prefix constraints)
                       (conj [:re (re-pattern (str "^" (java.util.regex.Pattern/quote (:prefix constraints))))])
                       
                       (:suffix constraints)
                       (conj [:re (re-pattern (str (java.util.regex.Pattern/quote (:suffix constraints)) "$"))]))]
    {:schema schema-parts
     :props (when (seq props) props)
     :generator (cond
                  (:email constraints) {:type :email}
                  (:uuid constraints) {:type :uuid}
                  (:pattern constraints) {:type :regex :pattern (:pattern constraints)}
                  :else nil)}))

;; =============================================================================
;; Enum Constraints
;; =============================================================================

(defmethod compile-constraint [:enum :keyword]
  [_ _ constraints]
  (let [schema-parts (cond-> []
                       (:not-in constraints)
                       (conj [:not [:enum (:not-in constraints)]])
                       
                       (:in constraints)
                       (conj [:enum (:in constraints)]))]
    {:schema schema-parts
     :generator (when (:in constraints)
                  {:values (:in constraints)})}))

;; =============================================================================
;; Collection Constraints
;; =============================================================================

(defmethod compile-constraint [:repeated :vector]
  [_ _ constraints]
  (let [schema-parts (cond-> []
                       (:min-items constraints)
                       (conj [:min (:min-items constraints)])
                       
                       (:max-items constraints)
                       (conj [:max (:max-items constraints)])
                       
                       (:unique constraints)
                       (conj [:fn 'distinct?]))]
    {:schema schema-parts
     :generator (when (or (:min-items constraints) (:max-items constraints))
                  {:min (or (:min-items constraints) 0)
                   :max (or (:max-items constraints) 100)})}))

;; =============================================================================
;; Message Constraints
;; =============================================================================

(defmethod compile-constraint [:message :map]
  [_ _ constraints]
  (let [schema-parts (cond-> []
                       (:required constraints)
                       (conj [:fn {:error/message "message is required"}
                              some?]))]
    {:schema schema-parts}))

;; =============================================================================
;; Default Fallback
;; =============================================================================

(defmethod compile-constraint :default
  [constraint-type spec-type constraints]
  ;; For unknown combinations, try to create predicates
  {:predicates (when (map? constraints)
                 (for [[k v] constraints]
                   (case k
                     :gt [:> v]
                     :gte [:>= v]
                     :lt [:< v]
                     :lte [:<= v]
                     :eq [:= v]
                     :ne [:not= v]
                     nil)))})

;; =============================================================================
;; Main Compilation Function
;; =============================================================================

(defn compile-field-constraints
  "Compile all constraints for a field into Malli schema components.
  
  Takes the field metadata and returns a map with:
  - :schema - Additional schema constraints to apply
  - :generator - Generator configuration
  - :predicates - Raw predicates for custom validation"
  [{:keys [type constraints] :as field}]
  (when constraints
    (let [buf-validate (:buf.validate constraints)
          ;; Determine the constraint type from the buf.validate structure
          constraint-type (cond
                           (:float buf-validate) :float
                           (:int32 buf-validate) :int32
                           (:int64 buf-validate) :int64
                           (:string buf-validate) :string
                           (:bytes buf-validate) :bytes
                           (:enum buf-validate) :enum
                           (:repeated buf-validate) :repeated
                           (:message buf-validate) :message
                           :else nil)
          ;; Map proto type to Malli type
          spec-type (case type
                     :TYPE_FLOAT :double
                     :TYPE_DOUBLE :double
                     :TYPE_INT32 :int
                     :TYPE_INT64 :int
                     :TYPE_UINT32 :int
                     :TYPE_UINT64 :int
                     :TYPE_STRING :string
                     :TYPE_BYTES :bytes
                     :TYPE_ENUM :keyword
                     :TYPE_MESSAGE :map
                     :else type)]
      (when constraint-type
        (compile-constraint constraint-type spec-type 
                           (get buf-validate constraint-type))))))

;; =============================================================================
;; Schema Enhancement
;; =============================================================================

(defn enhance-schema-with-constraints
  "Enhance a base Malli schema with compiled constraints.
  
  Takes a base schema and constraint compilation result,
  returns an enhanced schema."
  [base-schema {:keys [schema generator predicates props] :as compiled}]
  (cond
    ;; No constraints
    (nil? compiled) base-schema
    
    ;; String with min/max constraints - return schema with properties
    (and (= base-schema :string) props)
    (if (empty? schema)
      [:string props]
      (into [:and [:string props]] schema))
    
    ;; Simple constraint additions
    (and (seq schema) (not (seq predicates)))
    (if (= 1 (count schema))
      (first schema)
      (into [:and base-schema] schema))
    
    ;; Complex with predicates
    (seq predicates)
    (into [:and base-schema] (concat schema predicates))
    
    ;; Just base schema
    :else base-schema))

;; =============================================================================
;; Generator Support
;; =============================================================================

(defn create-constrained-generator
  "Create a Malli generator that respects constraints."
  [base-schema {:keys [generator] :as compiled}]
  (when generator
    (case (:type generator)
      :email (mg/generator [:re #"^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$"])
      :uuid (mg/generator [:re #"^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$"])
      :regex (mg/generator [:re (re-pattern (:pattern generator))])
      ;; Numeric with bounds
      (when (and (:min generator) (:max generator))
        (mg/generator [:double {:min (:min generator) :max (:max generator)}])))))

;; =============================================================================
;; Integration Point
;; =============================================================================

(defn apply-constraints
  "Main entry point for applying constraints to a schema.
  
  Returns the enhanced schema with all constraints applied."
  [base-schema field]
  (if-let [compiled (compile-field-constraints field)]
    (enhance-schema-with-constraints base-schema compiled)
    base-schema))