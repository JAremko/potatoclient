(ns proto-explorer.constraints.compiler-v2
  "Improved constraint compiler that properly handles Malli's schema format.
  
  Key improvements:
  - String constraints use :string with properties {:min :max}
  - Better handling of schema combination
  - Proper return format for enhance-schema-with-constraints"
  (:require [clojure.core.match :refer [match]]
            [malli.generator :as mg]))

;; =============================================================================
;; Multimethod Dispatch
;; =============================================================================

(defmulti compile-constraint
  "Compile a constraint into Malli schema additions.
  
  Dispatches on [constraint-type spec-type] where:
  - constraint-type: :float, :int32, :string, :enum, etc.
  - spec-type: :double, :int, :string, :keyword, etc.
  
  Returns a map with:
  - :base-schema - Modified base schema (if needed)
  - :additional-schemas - Additional schemas to combine with :and
  - :generator - Optional generator constraints"
  (fn [constraint-type spec-type constraint-value]
    [constraint-type spec-type]))

;; =============================================================================
;; Numeric Constraints
;; =============================================================================

(defmethod compile-constraint [:float :double]
  [_ _ constraints]
  (let [schemas (cond-> []
                  (:gt constraints) (conj [:> (:gt constraints)])
                  (:gte constraints) (conj [:>= (:gte constraints)])
                  (:lt constraints) (conj [:< (:lt constraints)])
                  (:lte constraints) (conj [:<= (:lte constraints)])
                  (:const constraints) (conj [:= (:const constraints)]))]
    {:additional-schemas schemas
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
  (let [schemas (cond-> []
                  (:gt constraints) (conj [:> (:gt constraints)])
                  (:gte constraints) (conj [:>= (:gte constraints)])
                  (:lt constraints) (conj [:< (:lt constraints)])
                  (:lte constraints) (conj [:<= (:lte constraints)])
                  (:const constraints) (conj [:= (:const constraints)])
                  (:in constraints) (conj [:enum (:in constraints)])
                  (:not-in constraints) (conj [:fn {:error/message (str "must not be one of: " (:not-in constraints))}
                                              (fn [v] (not (contains? (set (:not-in constraints)) v)))]))]
    {:additional-schemas schemas
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
  (let [;; Build properties map for string base schema
        props (cond-> {}
                (:min-len constraints) (assoc :min (:min-len constraints))
                (:max-len constraints) (assoc :max (:max-len constraints))
                (:len constraints) (assoc :min (:len constraints) 
                                         :max (:len constraints)))
        ;; Build base schema with properties if needed
        base-schema (if (seq props)
                      [:string props]
                      :string)
        ;; Build additional schemas for other constraints
        additional (cond-> []
                     (:pattern constraints)
                     (conj [:re (re-pattern (:pattern constraints))])
                     
                     (:email constraints)
                     (conj [:re #"^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$"])
                     
                     (:uuid constraints)
                     (conj [:re #"^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$"])
                     
                     (:prefix constraints)
                     (conj [:fn {:error/message (str "must start with: " (:prefix constraints))}
                            #(clojure.string/starts-with? % (:prefix constraints))])
                     
                     (:suffix constraints)
                     (conj [:fn {:error/message (str "must end with: " (:suffix constraints))}
                            #(clojure.string/ends-with? % (:suffix constraints))])]
    {:base-schema base-schema
     :additional-schemas additional
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
  (let [schemas (cond-> []
                  (:not-in constraints)
                  (conj [:fn {:error/message (str "must not be one of: " (:not-in constraints))}
                         (fn [v] (not (contains? (set (:not-in constraints)) v)))])
                  
                  (:in constraints)
                  (conj [:enum (:in constraints)]))]
    {:additional-schemas schemas
     :generator (when (:in constraints)
                  {:values (:in constraints)})}))

;; =============================================================================
;; Collection Constraints
;; =============================================================================

(defmethod compile-constraint [:repeated :vector]
  [_ _ constraints]
  (let [;; Build properties for vector schema
        props (cond-> {}
                (:min-items constraints) (assoc :min (:min-items constraints))
                (:max-items constraints) (assoc :max (:max-items constraints)))
        ;; Additional schemas
        additional (cond-> []
                     (:unique constraints)
                     (conj [:fn {:error/message "items must be unique"}
                            #(= (count %) (count (distinct %)))]))]
    {:props props
     :additional-schemas additional
     :generator (when (seq props)
                  {:min (or (:min-items constraints) 0)
                   :max (or (:max-items constraints) 100)})}))

;; =============================================================================
;; Message Constraints
;; =============================================================================

(defmethod compile-constraint [:message :map]
  [_ _ constraints]
  (let [schemas (cond-> []
                  (:required constraints)
                  (conj [:fn {:error/message "message is required"}
                         some?]))]
    {:additional-schemas schemas}))

;; =============================================================================
;; Default Fallback
;; =============================================================================

(defmethod compile-constraint :default
  [constraint-type spec-type constraints]
  ;; For unknown combinations, try to create predicates
  {:additional-schemas (when (map? constraints)
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
  - :base-schema - Modified base schema (if needed)
  - :additional-schemas - Additional schemas to combine
  - :generator - Generator configuration"
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
                     :type-float :double
                     :type-double :double
                     :type-int32 :int
                     :type-int64 :int
                     :type-uint32 :int
                     :type-uint64 :int
                     :type-string :string
                     :type-bytes :bytes
                     :type-enum :keyword
                     :type-message :map
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
  [original-schema {:keys [base-schema additional-schemas props] :as compiled}]
  (cond
    ;; No constraints
    (nil? compiled) 
    original-schema
    
    ;; Replaced base schema (e.g., string with length constraints)
    (and base-schema (empty? additional-schemas))
    base-schema
    
    ;; Only additional schemas, no base replacement
    (and (not base-schema) (seq additional-schemas))
    (if (= 1 (count additional-schemas))
      (first additional-schemas)
      (into [:and original-schema] additional-schemas))
    
    ;; Both base schema replacement and additional schemas
    (and base-schema (seq additional-schemas))
    (into [:and base-schema] additional-schemas)
    
    ;; Just original schema
    :else 
    original-schema))

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