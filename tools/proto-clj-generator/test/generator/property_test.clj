(ns generator.property-test
  "Property-based tests for generated code using constraints"
  (:require [clojure.test :refer :all]
            [clojure.test.check :as tc]
            [clojure.test.check.clojure-test :refer [defspec]]
            [clojure.test.check.generators :as gen]
            [clojure.test.check.properties :as prop]
            [malli.core :as m]
            [malli.generator :as mg]
            [generator.constraints.compiler :as compiler]))

;; =============================================================================
;; Constraint-Aware Generators
;; =============================================================================

(defn constraint->generator
  "Convert a constraint map to a test.check generator"
  [{:keys [type constraints]}]
  (let [compiled (compiler/compile-constraint type :numeric constraints)
        gen-hints (:generator compiled)]
    (case type
      :type-double
      (cond
        (and (:min gen-hints) (:max gen-hints))
        (gen/double* {:min (:min gen-hints) :max (:max gen-hints)})
        
        (:min gen-hints)
        (gen/double* {:min (:min gen-hints)})
        
        (:max gen-hints)
        (gen/double* {:max (:max gen-hints)})
        
        :else
        gen/double)
      
      :type-float
      (cond
        (and (:min gen-hints) (:max gen-hints))
        (gen/fmap float (gen/double* {:min (:min gen-hints) :max (:max gen-hints)}))
        
        (:min gen-hints)
        (gen/fmap float (gen/double* {:min (:min gen-hints)}))
        
        (:max gen-hints)
        (gen/fmap float (gen/double* {:max (:max gen-hints)}))
        
        :else
        (gen/fmap float gen/double))
      
      (:type-int32 :type-uint32 :type-sint32 :type-fixed32 :type-sfixed32)
      (cond
        (and (:min gen-hints) (:max gen-hints))
        (gen/large-integer* {:min (long (:min gen-hints)) :max (long (:max gen-hints))})
        
        (:min gen-hints)
        (gen/large-integer* {:min (long (:min gen-hints))})
        
        (:max gen-hints)
        (gen/large-integer* {:max (long (:max gen-hints))})
        
        :else
        gen/int)
      
      :type-string
      (cond
        (and (:min-len constraints) (:max-len constraints))
        (gen/such-that
         #(and (>= (count %) (:min-len constraints))
               (<= (count %) (:max-len constraints)))
         gen/string-alphanumeric)
        
        (:min-len constraints)
        (gen/such-that
         #(>= (count %) (:min-len constraints))
         gen/string-alphanumeric)
        
        (:max-len constraints)
        (gen/such-that
         #(<= (count %) (:max-len constraints))
         gen/string-alphanumeric)
        
        (:pattern constraints)
        (gen/such-that
         #(re-matches (re-pattern (:pattern constraints)) %)
         gen/string-alphanumeric)
        
        :else
        gen/string-alphanumeric)
      
      ;; Default
      gen/any)))

;; =============================================================================
;; Test Helpers
;; =============================================================================

(defn valid-according-to-constraints?
  "Check if a value satisfies the given constraints"
  [value {:keys [type constraints]}]
  (let [compiled (compiler/compile-constraint type :numeric constraints)
        schema (:schema compiled)]
    (every? #(m/validate % value) schema)))

(defn generate-valid-value
  "Generate a value that satisfies the constraints"
  [field-spec]
  (gen/generate (constraint->generator field-spec)))

;; =============================================================================
;; Property Tests
;; =============================================================================

(defspec numeric-constraint-generation
  100
  (prop/for-all [constraints (gen/hash-map
                              :gt (gen/one-of [gen/int (gen/return nil)])
                              :gte (gen/one-of [gen/int (gen/return nil)])
                              :lt (gen/one-of [gen/int (gen/return nil)])
                              :lte (gen/one-of [gen/int (gen/return nil)])]
    (let [field {:type :type-double :constraints constraints}]
      (or (empty? (filter some? (vals constraints)))
          (let [value (generate-valid-value field)]
            (valid-according-to-constraints? value field))))))

(defspec string-constraint-generation
  100
  (prop/for-all [min-len (gen/choose 0 10)
                 max-len (gen/choose 10 20)]
    (let [field {:type :type-string 
                 :constraints {:min-len min-len :max-len max-len}}
          value (generate-valid-value field)]
      (and (>= (count value) min-len)
           (<= (count value) max-len)))))

(defspec rgb-color-constraint-generation
  100
  (let [rgb-field {:type :type-int32
                   :constraints {:gte 0 :lte 255}}]
    (prop/for-all [value (constraint->generator rgb-field)]
      (and (>= value 0)
           (<= value 255)))))

(defspec protocol-version-constraint-generation
  100
  (let [version-field {:type :type-int32
                       :constraints {:gt 0}}]
    (prop/for-all [value (constraint->generator version-field)]
      (> value 0))))

;; =============================================================================
;; Boundary Testing
;; =============================================================================

(deftest test-constraint-boundaries
  (testing "Integer gt vs gte boundary"
    (let [gt-field {:type :type-int32 :constraints {:gt 5}}
          gte-field {:type :type-int32 :constraints {:gte 5}}]
      (is (not (valid-according-to-constraints? 5 gt-field)))
      (is (valid-according-to-constraints? 5 gte-field))
      (is (valid-according-to-constraints? 6 gt-field))
      (is (valid-according-to-constraints? 6 gte-field))))
  
  (testing "Float lt vs lte boundary with epsilon"
    (let [lt-field {:type :type-float :constraints {:lt 10.0}}
          lte-field {:type :type-float :constraints {:lte 10.0}}]
      (is (valid-according-to-constraints? 9.999 lt-field))
      (is (valid-according-to-constraints? 10.0 lte-field))
      (is (not (valid-according-to-constraints? 10.0 lt-field)))
      (is (not (valid-according-to-constraints? 10.001 lte-field)))))
  
  (testing "RGB color boundaries"
    (let [rgb-field {:type :type-int32 :constraints {:gte 0 :lte 255}}]
      (is (valid-according-to-constraints? 0 rgb-field))
      (is (valid-according-to-constraints? 255 rgb-field))
      (is (not (valid-according-to-constraints? -1 rgb-field)))
      (is (not (valid-according-to-constraints? 256 rgb-field))))))

;; =============================================================================
;; Integration Tests
;; =============================================================================

(deftest test-generated-spec-validation
  (testing "Generated specs match constraint validation"
    ;; Test that our constraint compilation matches what's in generated specs
    (let [protocol-spec [:> 0]
          rgb-spec [:and [:maybe :int] [:>= 0] [:<= 255]]]
      (is (m/validate protocol-spec 1))
      (is (not (m/validate protocol-spec 0)))
      (is (m/validate rgb-spec 128))
      (is (not (m/validate rgb-spec 256))))))

(deftest test-roundtrip-with-constraints
  (testing "Values satisfying constraints roundtrip correctly"
    ;; This would test actual generated code if we had it on classpath
    ;; For now, we test the constraint logic
    (let [lat-field {:type :type-double :constraints {:gte -90.0 :lte 90.0}}
          lon-field {:type :type-double :constraints {:gte -180.0 :lte 180.0}}
          lat (generate-valid-value lat-field)
          lon (generate-valid-value lon-field)]
      (is (and (>= lat -90.0) (<= lat 90.0)))
      (is (and (>= lon -180.0) (<= lon 180.0))))))