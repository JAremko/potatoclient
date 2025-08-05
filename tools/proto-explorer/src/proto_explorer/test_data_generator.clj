(ns proto-explorer.test-data-generator
  "Generate test data from Malli specs with constraint awareness.
  
  This namespace provides functions to generate valid test data that
  respects all buf.validate constraints embedded in the specs."
  (:require [malli.core :as m]
            [malli.generator :as mg]
            [proto-explorer.generated-specs :as specs]
            [potatoclient.specs.malli-oneof :as oneof]
            [clojure.string :as str]
            [clojure.test.check.generators :as gen]))

;; =============================================================================
;; Custom Generators for Constrained Types
;; =============================================================================

(defn generate-constrained-string
  "Generate a string that satisfies the given constraints."
  [{:keys [min max pattern prefix suffix]}]
  (cond
    pattern
    ;; For patterns, use malli's regex generator
    (mg/generator [:re (re-pattern pattern)])
    
    (or prefix suffix)
    ;; For prefix/suffix, use gen/fmap to transform the base string
    (let [base-gen (mg/generator [:string {:min (or min 1) :max (or max 20)}])]
      (gen/fmap (fn [base-str]
                  (str (or prefix "") 
                       base-str
                       (or suffix "")))
                base-gen))
    
    :else
    ;; Default string generator with length constraints
    (mg/generator [:string (cond-> {}
                            min (assoc :min min)
                            max (assoc :max max))])))

(defn generate-constrained-number
  "Generate a number that satisfies the given constraints."
  [spec-type {:keys [gt gte lt lte const in not-in]}]
  (cond
    const
    ;; Constant value
    (gen/return const)
    
    in
    ;; One of the allowed values
    (gen/elements in)
    
    (or gt gte lt lte)
    ;; Range constraint
    (let [min-val (or gte (when gt (+ gt 0.0001)) Double/NEGATIVE_INFINITY)
          max-val (or lte (when lt (- lt 0.0001)) Double/POSITIVE_INFINITY)]
      (mg/generator [spec-type {:min min-val :max max-val}]))
    
    not-in
    ;; Generate values excluding the not-in set
    (gen/such-that (fn [val] (not (contains? (set not-in) val)))
                   (mg/generator spec-type)
                   100)
    
    :else
    ;; Default generator
    (mg/generator spec-type)))

(defn generate-constrained-collection
  "Generate a collection that satisfies the given constraints."
  [item-spec {:keys [min-items max-items unique]}]
  (let [size (if (or min-items max-items)
               (+ (or min-items 0)
                  (rand-int (inc (- (or max-items 100) (or min-items 0)))))
               (rand-int 10))
        gen-fn #(mg/generate (mg/generator item-spec {:registry (specs/proto-registry)}))]
    (if unique
      ;; Generate unique items
      (loop [items #{}]
        (if (>= (count items) size)
          (vec items)
          (recur (conj items (gen-fn)))))
      ;; Generate possibly duplicate items
      (vec (repeatedly size gen-fn)))))

;; =============================================================================
;; Schema Analysis and Generator Creation
;; =============================================================================

(defn analyze-schema
  "Analyze a Malli schema to extract constraint information."
  [schema]
  (cond
    ;; Simple keyword schema
    (keyword? schema)
    {:type schema}
    
    ;; Vector schema with properties
    (and (vector? schema) (map? (second schema)))
    {:type (first schema)
     :props (second schema)}
    
    ;; :and schema with constraints
    (and (vector? schema) (= :and (first schema)))
    (let [base-type (some #(when (keyword? %) %) (rest schema))
          constraints (filter vector? (rest schema))]
      {:type base-type
       :constraints constraints})
    
    ;; :not schema
    (and (vector? schema) (= :not (first schema)))
    {:type :not
     :inner (analyze-schema (second schema))}
    
    ;; Other vector schemas
    (vector? schema)
    {:type (first schema)
     :args (rest schema)}
    
    :else
    {:type :unknown
     :schema schema}))

(defn constraint->generator-hint
  "Convert a constraint to generator hints."
  [constraint]
  (when (vector? constraint)
    (case (first constraint)
      :>= {:gte (second constraint)}
      :> {:gt (second constraint)}
      :<= {:lte (second constraint)}
      :< {:lt (second constraint)}
      := {:const (second constraint)}
      :enum {:in (second constraint)}
      :not (when (and (vector? (second constraint))
                      (= :enum (first (second constraint))))
             (let [enum-values (rest (second constraint))]
               ;; Handle both [:enum [0]] and [:enum 0] formats
               (if (and (= 1 (count enum-values))
                        (sequential? (first enum-values)))
                 {:not-in (first enum-values)}
                 {:not-in enum-values})))
      nil)))

(defn merge-generator-hints
  "Merge multiple generator hints."
  [hints]
  (reduce (fn [acc hint]
            (merge acc (constraint->generator-hint hint)))
          {}
          hints))

;; =============================================================================
;; Main Generator API
;; =============================================================================

(defn create-generator
  "Create a generator for a Malli schema with constraint awareness."
  [schema]
  (let [{:keys [type props constraints inner args]} (analyze-schema schema)]
    (case type
      ;; String types
      :string
      (if (or props constraints)
        (let [hints (merge props (when constraints
                                  (merge-generator-hints constraints)))]
          (generate-constrained-string hints))
        (mg/generator :string))
      
      ;; Numeric types
      (:int :double :float)
      (if constraints
        (let [hints (merge-generator-hints constraints)]
          (generate-constrained-number type hints))
        (mg/generator type))
      
      ;; Collection types
      :vector
      (if (and args props)
        (let [item-gen (create-generator (first args))
              min-size (or (:min-items props) 0)
              max-size (or (:max-items props) 10)]
          (if (:unique props)
            ;; Generate unique items
            (gen/bind (gen/choose min-size max-size)
                      (fn [size]
                        (gen/fmap vec (gen/set item-gen {:min-elements size
                                                         :max-elements size}))))
            ;; Generate possibly duplicate items
            (gen/bind (gen/choose min-size max-size)
                      (fn [size]
                        (gen/vector item-gen size)))))
        (mg/generator schema {:registry (specs/proto-registry)}))
      
      ;; :and schemas - need to handle all constraints
      :and
      (let [base-type (first args)
            constraints (rest args)]
        ;; Check if we have a [:not [:enum]] constraint
        (if-let [not-enum-constraint (first (filter #(and (vector? %)
                                                          (= :not (first %))
                                                          (vector? (second %))
                                                          (= :enum (first (second %))))
                                                   constraints))]
          ;; Extract excluded values from [:not [:enum [0]]] or [:not [:enum 0]]
          (let [enum-spec (second not-enum-constraint)
                enum-values (rest enum-spec)
                ;; Handle both [:enum [0]] and [:enum 0] formats
                excluded-values (if (and (= 1 (count enum-values))
                                        (sequential? (first enum-values)))
                                  (set (first enum-values))
                                  (set enum-values))
                base-gen (create-generator base-type)]
            (if (= excluded-values #{0})
              ;; Special case for excluding 0
              (gen/such-that #(not= % 0) base-gen 100)
              ;; General case
              (gen/such-that #(not (contains? excluded-values %)) base-gen 100)))
          ;; No :not [:enum] constraint, just use the base type
          (create-generator base-type)))
      
      ;; :maybe schemas
      :maybe
      (let [inner-gen (create-generator (first args))]
        (gen/frequency [[3 (gen/return nil)]
                        [7 inner-gen]]))
      
      ;; :map schemas
      :map
      (mg/generator schema {:registry (specs/proto-registry)})
      
      ;; :oneof schemas
      :oneof
      (mg/generator schema {:registry (specs/proto-registry)})
      
      ;; :not schemas with :enum
      :not
      ;; For :not schemas, we have inner information from analyze-schema
      (if (and inner (= :enum (:type inner)))
        ;; Handle [:not [:enum [0]]] or [:not [:enum 0]]
        (let [enum-values (:args inner)
              ;; Handle both [:enum [0]] and [:enum 0] and [:enum 0 1 2] formats
              excluded (cond
                        ;; Single value in vector: [:enum [0]]
                        (and (= 1 (count enum-values))
                             (sequential? (first enum-values)))
                        (set (first enum-values))
                        ;; Direct values: [:enum 0] or [:enum 0 1 2]
                        :else
                        (set enum-values))]
          ;; Special case for :not [:enum [0]] or [:not [:enum 0]]
          (if (= excluded #{0})
            ;; Generate non-zero integers
            (gen/such-that #(not= % 0) gen/int 100)
            ;; For other excluded sets, generate values that don't match
            (gen/such-that #(not (contains? excluded %)) gen/int 100)))
        ;; For other :not schemas, default to int generator
        (mg/generator :int))
      
      ;; Default
      (try
        (mg/generator (or schema type) {:registry (specs/proto-registry)})
        (catch Exception e
          ;; If we can't create a generator, return a simple one
          (mg/generator :any))))))

(defn generate-data
  "Generate test data from a spec with constraint awareness."
  [spec-key]
  (if-let [spec (specs/get-spec spec-key)]
    (let [generator (create-generator spec)]
      (mg/generate generator {:registry (specs/proto-registry)}))
    (throw (ex-info "Spec not found" {:spec spec-key}))))

(defn generate-examples
  "Generate multiple examples from a spec."
  [spec-key n]
  (repeatedly n #(generate-data spec-key)))

;; =============================================================================
;; Property-Based Testing Support
;; =============================================================================

(defn for-all
  "Create a property test for a spec.
  
  Usage:
  (for-all :cmd/ping
    (fn [data]
      (is (m/validate cmd/ping data))))"
  [spec-key test-fn]
  (let [spec (specs/get-spec spec-key)
        generator (create-generator spec)]
    (fn []
      (dotimes [_ 100] ; Run 100 tests
        (let [data (mg/generate generator {:registry (specs/proto-registry)})]
          (test-fn data))))))

;; =============================================================================
;; Round-Trip Testing
;; =============================================================================

(defn validate-roundtrip
  "Validate that generated data conforms to the spec."
  [spec-key]
  (let [spec (specs/get-spec spec-key)
        data (generate-data spec-key)]
    {:valid? (m/validate spec data {:registry (specs/proto-registry)})
     :data data
     :errors (when-not (m/validate spec data {:registry (specs/proto-registry)})
               (m/explain spec data {:registry (specs/proto-registry)}))}))

;; =============================================================================
;; Example Usage
;; =============================================================================

(comment
  ;; Load specs first
  (specs/load-specs! "../../shared/specs/protobuf")
  
  ;; Generate data with constraints
  (generate-data :cmd.RotaryPlatform/set-azimuth-value)
  ;; => {:value 245.3, :direction 1}  ; value always 0-360
  
  ;; Generate multiple examples
  (generate-examples :ser/jon-gui-data 5)
  
  ;; Validate round-trip
  (validate-roundtrip :cmd/ping)
  ;; => {:valid? true, :data {}}
  
  ;; Property-based test
  (let [test (for-all :cmd.RotaryPlatform/set-azimuth-value
                (fn [data]
                  (and (>= (:value data) 0)
                       (< (:value data) 360)
                       (not= (:direction data) 0))))]
    (test))
  )