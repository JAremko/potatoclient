(ns generator.constraints.compiler
  "Constraint compiler that transforms buf.validate constraints into Malli schemas.
  
  Uses multimethod dispatch on [constraint-type spec-type] to handle different
  combinations of constraints and target schema types.
  
  Ported from proto-explorer's constraints/compiler.clj"
  (:require [clojure.string :as str]))

;; =============================================================================
;; Multimethod Dispatch
;; =============================================================================

(defmulti compile-constraint
  "Compile a constraint into Malli schema additions.
  
  Dispatches on [constraint-type spec-type] where:
  - constraint-type: :numeric, :string, :enum, :collection, :message
  - spec-type: :double, :int, :string, :keyword, :vector, :map, etc.
  
  Returns a map with:
  - :schema - Additional schema elements to merge
  - :props - Schema properties (for string min/max)
  - :generator - Optional generator constraints
  - :predicates - Raw predicate functions if needed"
  (fn [constraint-type spec-type constraints]
    [constraint-type spec-type]))

;; =============================================================================
;; Numeric Constraints
;; =============================================================================

(defmethod compile-constraint [:numeric :double]
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
                             (+ (:gt constraints) 0.0001)))
                   :max (or (:lte constraints)
                           (when (:lt constraints)
                             (- (:lt constraints) 0.0001)))})}))

(defmethod compile-constraint [:numeric :int]
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
                       
                       (:hostname constraints)
                       (conj [:re #"^(?:[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?\.)*[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?$"])
                       
                       (:ipv4 constraints)
                       (conj [:re #"^(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$"])
                       
                       (:ipv6 constraints)
                       (conj [:re #"^(([0-9a-fA-F]{1,4}:){7,7}[0-9a-fA-F]{1,4}|([0-9a-fA-F]{1,4}:){1,7}:|([0-9a-fA-F]{1,4}:){1,6}:[0-9a-fA-F]{1,4}|([0-9a-fA-F]{1,4}:){1,5}(:[0-9a-fA-F]{1,4}){1,2}|([0-9a-fA-F]{1,4}:){1,4}(:[0-9a-fA-F]{1,4}){1,3}|([0-9a-fA-F]{1,4}:){1,3}(:[0-9a-fA-F]{1,4}){1,4}|([0-9a-fA-F]{1,4}:){1,2}(:[0-9a-fA-F]{1,4}){1,5}|[0-9a-fA-F]{1,4}:((:[0-9a-fA-F]{1,4}){1,6})|:((:[0-9a-fA-F]{1,4}){1,7}|:)|fe80:(:[0-9a-fA-F]{0,4}){0,4}%[0-9a-zA-Z]{1,}|::(ffff(:0{1,4}){0,1}:){0,1}((25[0-5]|(2[0-4]|1{0,1}[0-9]){0,1}[0-9])\.){3,3}(25[0-5]|(2[0-4]|1{0,1}[0-9]){0,1}[0-9])|([0-9a-fA-F]{1,4}:){1,4}:((25[0-5]|(2[0-4]|1{0,1}[0-9]){0,1}[0-9])\.){3,3}(25[0-5]|(2[0-4]|1{0,1}[0-9]){0,1}[0-9]))$"])
                       
                       (:ip constraints)
                       (conj [:or 
                              [:re #"^(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$"]
                              [:re #"^(([0-9a-fA-F]{1,4}:){7,7}[0-9a-fA-F]{1,4}|([0-9a-fA-F]{1,4}:){1,7}:|([0-9a-fA-F]{1,4}:){1,6}:[0-9a-fA-F]{1,4}|([0-9a-fA-F]{1,4}:){1,5}(:[0-9a-fA-F]{1,4}){1,2}|([0-9a-fA-F]{1,4}:){1,4}(:[0-9a-fA-F]{1,4}){1,3}|([0-9a-fA-F]{1,4}:){1,3}(:[0-9a-fA-F]{1,4}){1,4}|([0-9a-fA-F]{1,4}:){1,2}(:[0-9a-fA-F]{1,4}){1,5}|[0-9a-fA-F]{1,4}:((:[0-9a-fA-F]{1,4}){1,6})|:((:[0-9a-fA-F]{1,4}){1,7}|:)|fe80:(:[0-9a-fA-F]{0,4}){0,4}%[0-9a-zA-Z]{1,}|::(ffff(:0{1,4}){0,1}:){0,1}((25[0-5]|(2[0-4]|1{0,1}[0-9]){0,1}[0-9])\.){3,3}(25[0-5]|(2[0-4]|1{0,1}[0-9]){0,1}[0-9])|([0-9a-fA-F]{1,4}:){1,4}:((25[0-5]|(2[0-4]|1{0,1}[0-9]){0,1}[0-9])\.){3,3}(25[0-5]|(2[0-4]|1{0,1}[0-9]){0,1}[0-9]))$"]])
                       
                       (:uri constraints)
                       (conj [:re #"^[a-zA-Z][a-zA-Z0-9+.-]*:[^\\s]*$"])
                       
                       (:uuid constraints)
                       (conj [:re #"^[0-9a-f]{8}-[0-9a-f]{4}-[1-5][0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$"])
                       
                       (:prefix constraints)
                       (conj [:fn {:error/message (str "must start with \"" (:prefix constraints) "\"")}
                              (fn [s] (str/starts-with? s (:prefix constraints)))])
                       
                       (:suffix constraints)
                       (conj [:fn {:error/message (str "must end with \"" (:suffix constraints) "\"")}
                              (fn [s] (str/ends-with? s (:suffix constraints)))])
                       
                       (:contains constraints)
                       (conj [:fn {:error/message (str "must contain \"" (:contains constraints) "\"")}
                              (fn [s] (str/includes? s (:contains constraints)))])
                       
                       (:not-contains constraints)
                       (conj [:fn {:error/message (str "must not contain \"" (:not-contains constraints) "\"")}
                              (fn [s] (not (str/includes? s (:not-contains constraints))))])
                       
                       (:in constraints)
                       (conj [:enum (:in constraints)])
                       
                       (:not-in constraints)
                       (conj [:not [:enum (:not-in constraints)]]))]
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
                       (:const constraints)
                       (conj [:= (:const constraints)])
                       
                       (:not-in constraints)
                       (conj [:not [:enum (:not-in constraints)]])
                       
                       (:in constraints)
                       (conj [:enum (:in constraints)])
                       
                       ;; defined-only is more of a validation rule than a schema constraint
                       (:defined-only constraints)
                       (conj [:fn {:error/message "must be a defined enum value"}
                              (fn [v] (keyword? v))]))]
    {:schema schema-parts
     :generator (when (:in constraints)
                  {:values (:in constraints)})}))

;; =============================================================================
;; Collection Constraints
;; =============================================================================

(defmethod compile-constraint [:collection :vector]
  [_ _ constraints]
  (let [props (cond-> {}
                (:min-items constraints) (assoc :min (:min-items constraints))
                (:max-items constraints) (assoc :max (:max-items constraints)))
        schema-parts (cond-> []
                       (:unique constraints)
                       (conj [:fn {:error/message "all items must be unique"}
                              #(= (count %) (count (distinct %)))]))]
    {:schema schema-parts
     :props (when (seq props) props)
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
                       (conj [:fn {:error/message "message is required (cannot be nil)"}
                              some?])
                       
                       ;; skip is handled at code generation level, not schema level
                       )]
    {:schema schema-parts}))

;; =============================================================================
;; Boolean Constraints
;; =============================================================================

(defmethod compile-constraint [:boolean :boolean]
  [_ _ constraints]
  (let [schema-parts (cond-> []
                       (contains? constraints :const)
                       (conj [:= (:const constraints)]))]
    {:schema schema-parts}))

;; =============================================================================
;; Default Fallback
;; =============================================================================

(defmethod compile-constraint :default
  [constraint-type spec-type constraints]
  ;; For unknown combinations, return empty
  {:schema []})

;; =============================================================================
;; Helper Functions
;; =============================================================================

(defn determine-constraint-type
  "Determine the constraint type based on field type."
  [field-type]
  (case field-type
    ;; Numeric types
    (:type-double :type-float 
     :type-int64 :type-uint64 :type-int32 
     :type-fixed64 :type-fixed32 :type-uint32 
     :type-sfixed32 :type-sfixed64 
     :type-sint32 :type-sint64
     :type-uint-32 :type-uint-64) :numeric
    
    ;; String and bytes
    (:type-string :type-bytes) :string
    
    ;; Boolean
    :type-bool :boolean
    
    ;; Message
    :type-message :message
    
    ;; Enum
    :type-enum :enum
    
    ;; Default
    nil))

(defn determine-malli-type
  "Map proto field type to Malli schema type."
  [field-type]
  (case field-type
    (:type-float :type-double) :double
    (:type-int32 :type-sint32 :type-sfixed32
     :type-int64 :type-sint64 :type-sfixed64
     :type-uint32 :type-fixed32
     :type-uint64 :type-fixed64
     :type-uint-32 :type-uint-64) :int
    :type-string :string
    :type-bytes :bytes
    :type-bool :boolean
    :type-enum :keyword
    :type-message :map
    ;; Default
    :any))

;; =============================================================================
;; Main Compilation Function
;; =============================================================================

(defn compile-field-constraints
  "Compile all constraints for a field into Malli schema components.
  
  Takes the field metadata with extracted constraints and returns a map with:
  - :schema - Additional schema constraints to apply
  - :props - Schema properties (for collections and strings)
  - :generator - Generator configuration
  - :predicates - Raw predicates for custom validation"
  [{:keys [type constraints label] :as field}]
  (when constraints
    (let [;; Handle nested type structure
          actual-type (cond
                       ;; Direct scalar type
                       (keyword? type) type
                       ;; Unknown type with proto-type
                       (and (map? type) (:unknown type))
                       (get-in type [:unknown :proto-type])
                       ;; Other cases
                       :else type)
          ;; Determine types
          constraint-type (determine-constraint-type actual-type)
          malli-type (determine-malli-type actual-type)
          
          ;; Compile field constraints
          field-result (when (and constraint-type (:field-constraints constraints))
                        (compile-constraint constraint-type malli-type 
                                          (:field-constraints constraints)))
          
          ;; Compile repeated constraints if applicable
          repeated-result (when (and (= label :label-repeated)
                                   (:repeated-constraints constraints))
                           (compile-constraint :collection :vector
                                             (:repeated-constraints constraints)))]
      
      ;; Merge results
      (cond-> {}
        field-result (merge field-result)
        repeated-result (-> (update :schema concat (:schema repeated-result))
                           (update :props merge (:props repeated-result))
                           (update :generator merge (:generator repeated-result)))))))

;; =============================================================================
;; Schema Enhancement
;; =============================================================================

(defn enhance-schema-with-constraints
  "Enhance a base Malli schema with compiled constraints.
  
  Takes a base schema and constraint compilation result,
  returns an enhanced schema."
  [base-schema {:keys [schema props generator] :as compiled}]
  (cond
    ;; No constraints
    (nil? compiled) 
    base-schema
    
    ;; String with properties
    (and (= base-schema :string) props (empty? schema))
    [:string props]
    
    ;; String with properties and additional constraints
    (and (= base-schema :string) props (seq schema))
    (into [:and [:string props]] schema)
    
    ;; Vector with properties
    (and (vector? base-schema) 
         (= (first base-schema) :vector)
         props)
    (let [[tag item-schema & rest] base-schema]
      (into [:vector props item-schema] rest))
    
    ;; Simple constraint additions
    (seq schema)
    (if (= 1 (count schema))
      (first schema)
      (into [:and base-schema] schema))
    
    ;; Just base schema
    :else 
    base-schema))

;; =============================================================================
;; Integration Point
;; =============================================================================

(defn apply-constraints
  "Main entry point for applying constraints to a schema.
  
  Takes a base schema and field definition with extracted constraints,
  returns the enhanced schema with all constraints applied."
  [base-schema field]
  (if-let [compiled (compile-field-constraints field)]
    (enhance-schema-with-constraints base-schema compiled)
    base-schema))