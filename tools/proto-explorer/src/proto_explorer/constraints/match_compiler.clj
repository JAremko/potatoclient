(ns proto-explorer.constraints.match-compiler
  "Constraint compiler using core.match for cleaner pattern matching.
  
  This version uses core.match to elegantly handle presence/absence
  of constraint keys and their combinations."
  (:require [clojure.core.match :refer [match]]
            [clojure.string :as str]
            [malli.generator :as mg]))

;; =============================================================================
;; Numeric Constraints
;; =============================================================================

(defn compile-numeric-constraints
  "Compile numeric constraints using pattern matching."
  [constraints base-type]
  (match [constraints]
    ;; Range constraints
    [{:gte min :lte max}] 
    {:schema [:and [:>= min] [:<= max]]
     :generator {:min min :max max}}
    
    [{:gt min :lt max}]
    {:schema [:and [:> min] [:< max]]
     :generator {:min (if (= base-type :int) (inc min) (+ min 0.0001))
                 :max (if (= base-type :int) (dec max) (- max 0.0001))}}
    
    [{:gte min :lt max}]
    {:schema [:and [:>= min] [:< max]]
     :generator {:min min 
                 :max (if (= base-type :int) (dec max) (- max 0.0001))}}
    
    [{:gt min :lte max}]
    {:schema [:and [:> min] [:<= max]]
     :generator {:min (if (= base-type :int) (inc min) (+ min 0.0001))
                 :max max}}
    
    ;; Single bound constraints
    [{:gte min}]
    {:schema [:>= min]
     :generator {:min min}}
    
    [{:gt min}]
    {:schema [:> min]
     :generator {:min (if (= base-type :int) (inc min) (+ min 0.0001))}}
    
    [{:lte max}]
    {:schema [:<= max]
     :generator {:max max}}
    
    [{:lt max}]
    {:schema [:< max]
     :generator {:max (if (= base-type :int) (dec max) (- max 0.0001))}}
    
    ;; Constant value
    [{:const value}]
    {:schema [:= value]
     :generator {:const value}}
    
    ;; Set membership
    [{:in values}]
    {:schema [:enum values]
     :generator {:values values}}
    
    [{:not-in values}]
    {:schema [:fn {:error/message (str "must not be one of: " values)}
              (fn [v] (not (contains? (set values) v)))]
     :generator {:exclude values}}
    
    ;; No constraints
    [{}] nil
    :else nil))

;; =============================================================================
;; String Constraints
;; =============================================================================

(defn compile-string-constraints
  "Compile string constraints using pattern matching."
  [constraints]
  (match [constraints]
    ;; Length constraints
    [{:min-len min :max-len max}]
    {:schema [:and [:min-length min] [:max-length max]]
     :generator {:min-length min :max-length max}}
    
    [{:len exact}]
    {:schema [:and [:min-length exact] [:max-length exact]]
     :generator {:length exact}}
    
    [{:min-len min}]
    {:schema [:min-length min]
     :generator {:min-length min}}
    
    [{:max-len max}]
    {:schema [:max-length max]
     :generator {:max-length max}}
    
    ;; Pattern constraints
    [{:pattern pattern}]
    {:schema [:re (re-pattern pattern)]
     :generator {:pattern pattern}}
    
    ;; Well-known formats
    [{:email true}]
    {:schema [:re #"^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$"]
     :generator {:format :email}}
    
    [{:uuid true}]
    {:schema [:re #"^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$"]
     :generator {:format :uuid}}
    
    [{:hostname true}]
    {:schema [:re #"^[a-zA-Z0-9]([a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?(\.[a-zA-Z0-9]([a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?)*$"]
     :generator {:format :hostname}}
    
    [{:uri true}]
    {:schema [:re #"^[a-zA-Z][a-zA-Z0-9+.-]*:[^\s]*$"]
     :generator {:format :uri}}
    
    ;; Prefix/suffix constraints
    [{:prefix prefix :suffix suffix}]
    {:schema [:and 
              [:fn {:error/message (str "must start with: " prefix)}
               #(str/starts-with? % prefix)]
              [:fn {:error/message (str "must end with: " suffix)}
               #(str/ends-with? % suffix)]]
     :generator {:prefix prefix :suffix suffix}}
    
    [{:prefix prefix}]
    {:schema [:fn {:error/message (str "must start with: " prefix)}
              #(str/starts-with? % prefix)]
     :generator {:prefix prefix}}
    
    [{:suffix suffix}]
    {:schema [:fn {:error/message (str "must end with: " suffix)}
              #(str/ends-with? % suffix)]
     :generator {:suffix suffix}}
    
    ;; Contains constraints
    [{:contains substring}]
    {:schema [:fn {:error/message (str "must contain: " substring)}
              #(str/includes? % substring)]
     :generator {:contains substring}}
    
    [{:not-contains substring}]
    {:schema [:fn {:error/message (str "must not contain: " substring)}
              #(not (str/includes? % substring))]
     :generator {:not-contains substring}}
    
    ;; No constraints
    [{}] nil
    :else nil))

;; =============================================================================
;; Enum Constraints
;; =============================================================================

(defn compile-enum-constraints
  "Compile enum constraints using pattern matching."
  [constraints]
  (match [constraints]
    ;; Not-in with defined-only
    [{:not-in excluded :defined-only true}]
    {:schema [:fn {:error/message (str "must be a defined enum value, not one of: " excluded)}
              (fn [v] (not (contains? (set excluded) v)))]
     :generator {:exclude excluded}}
    
    ;; Just not-in
    [{:not-in excluded}]
    {:schema [:fn {:error/message (str "must not be one of: " excluded)}
              (fn [v] (not (contains? (set excluded) v)))]
     :generator {:exclude excluded}}
    
    ;; Just defined-only (handled by enum reference)
    [{:defined-only true}] nil
    
    ;; In constraint
    [{:in values}]
    {:schema [:enum values]
     :generator {:values values}}
    
    ;; No constraints
    [{}] nil
    :else nil))

;; =============================================================================
;; Collection Constraints
;; =============================================================================

(defn compile-repeated-constraints
  "Compile repeated field constraints using pattern matching."
  [constraints]
  (match [constraints]
    ;; Size constraints with uniqueness
    [{:min-items min :max-items max :unique true}]
    {:schema [:and 
              [:min min] 
              [:max max]
              [:fn {:error/message "items must be unique"}
               #(= (count %) (count (distinct %)))]]
     :generator {:min min :max max :unique true}}
    
    ;; Just size constraints
    [{:min-items min :max-items max}]
    {:schema [:and [:min min] [:max max]]
     :generator {:min min :max max}}
    
    [{:min-items min}]
    {:schema [:min min]
     :generator {:min min}}
    
    [{:max-items max}]
    {:schema [:max max]
     :generator {:max max}}
    
    ;; Just uniqueness
    [{:unique true}]
    {:schema [:fn {:error/message "items must be unique"}
              #(= (count %) (count (distinct %)))]
     :generator {:unique true}}
    
    ;; No constraints
    [{}] nil
    :else nil))

;; =============================================================================
;; Main Compilation Function
;; =============================================================================

(defn compile-constraints
  "Main entry point for compiling constraints based on field type and constraint type."
  [{:keys [type constraints] :as field}]
  (when-let [buf-validate (:buf.validate constraints)]
    (match [type buf-validate]
      ;; Float/Double constraints
      [:type-float {:float rules}] (compile-numeric-constraints rules :float)
      [:type-double {:float rules}] (compile-numeric-constraints rules :float)
      
      ;; Integer constraints
      [:type-int32 {:int32 rules}] (compile-numeric-constraints rules :int)
      [:type-int64 {:int64 rules}] (compile-numeric-constraints rules :int)
      [:type-uint32 {:uint32 rules}] (compile-numeric-constraints rules :int)
      [:type-uint64 {:uint64 rules}] (compile-numeric-constraints rules :int)
      
      ;; String constraints
      [:type-string {:string rules}] (compile-string-constraints rules)
      
      ;; Bytes constraints (similar to string)
      [:type-bytes {:bytes rules}] (compile-string-constraints rules)
      
      ;; Enum constraints
      [:type-enum {:enum rules}] (compile-enum-constraints rules)
      
      ;; Repeated field constraints
      [_ {:repeated rules}] (compile-repeated-constraints rules)
      
      ;; Message constraints
      [:type-message {:message {:required true}}]
      {:schema [:fn {:error/message "message is required"} some?]}
      
      ;; No matching constraints
      :else nil)))

;; =============================================================================
;; Schema Enhancement
;; =============================================================================

(defn enhance-schema
  "Enhance a base schema with compiled constraints."
  [base-schema compiled-constraints]
  (if-let [{:keys [schema]} compiled-constraints]
    (cond
      ;; Single constraint
      (and (vector? schema) 
           (#{:= :> :>= :< :<= :min :max :min-length :max-length :re :enum} (first schema)))
      schema
      
      ;; Multiple constraints - wrap with :and
      (vector? schema)
      (into [:and base-schema] (if (= :and (first schema)) (rest schema) [schema]))
      
      ;; No constraints
      :else base-schema)
    base-schema))

;; =============================================================================
;; Generator Creation
;; =============================================================================

(defn create-generator
  "Create a constrained generator based on compiled constraints."
  [base-schema {:keys [generator] :as compiled}]
  (when generator
    (match [generator]
      ;; Numeric with bounds
      [{:min min :max max}]
      (mg/generator [:double {:min min :max max}])
      
      [{:min min}]
      (mg/generator [:double {:min min}])
      
      [{:max max}]
      (mg/generator [:double {:max max}])
      
      ;; Constant value
      [{:const value}]
      (mg/generator [:= value])
      
      ;; Enum values
      [{:values values}]
      (mg/generator [:enum values])
      
      ;; String formats
      [{:format :email}]
      (mg/generator [:re #"^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$"])
      
      [{:format :uuid}]
      (mg/generator [:re #"^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$"])
      
      ;; String with length
      [{:min-length min :max-length max}]
      (mg/generator [:string {:min min :max max}])
      
      ;; Collection with size
      [{:min min :max max}]
      (mg/generator [:vector {:min min :max max}])
      
      :else nil)))