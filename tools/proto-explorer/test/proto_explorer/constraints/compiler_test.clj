(ns proto-explorer.constraints.compiler-test
  "Tests for the constraint compiler."
  (:require [clojure.test :refer :all]
            [proto-explorer.constraints.compiler :as compiler]
            [malli.core :as m]
            [malli.generator :as mg]))

;; =============================================================================
;; Float Constraints
;; =============================================================================

(deftest test-float-constraints
  (testing "Float constraints with gte and lt"
    (let [field {:type :type-float
                 :constraints {:buf.validate {:float {:gte 0 :lt 360}}}}
          result (compiler/compile-field-constraints field)]
      (is (= [:>= 0] (first (:schema result))))
      (is (= [:< 360] (second (:schema result))))
      (is (= 0 (:min (:generator result))))
      (is (= 359.9999 (:max (:generator result))))))
  
  (testing "Float constraints with gt and lte"
    (let [field {:type :type-float
                 :constraints {:buf.validate {:float {:gt -1.0 :lte 1.0}}}}
          result (compiler/compile-field-constraints field)]
      (is (= [:> -1.0] (first (:schema result))))
      (is (= [:<= 1.0] (second (:schema result))))
      (is (= -0.9999 (:min (:generator result))))
      (is (= 1.0 (:max (:generator result))))))
  
  (testing "Float constant constraint"
    (let [field {:type :type-float
                 :constraints {:buf.validate {:float {:const 3.14}}}}
          result (compiler/compile-field-constraints field)]
      (is (= [:= 3.14] (first (:schema result)))))))

;; =============================================================================
;; Integer Constraints
;; =============================================================================

(deftest test-integer-constraints
  (testing "Integer constraints with range"
    (let [field {:type :type-int32
                 :constraints {:buf.validate {:int32 {:gte 0 :lte 100}}}}
          result (compiler/compile-field-constraints field)]
      (is (= [:>= 0] (first (:schema result))))
      (is (= [:<= 100] (second (:schema result))))
      (is (= 0 (:min (:generator result))))
      (is (= 100 (:max (:generator result))))))
  
  (testing "Integer in/not-in constraints"
    (let [field {:type :type-int32
                 :constraints {:buf.validate {:int32 {:in [1 2 3]}}}}
          result (compiler/compile-field-constraints field)]
      (is (= [:enum [1 2 3]] (first (:schema result)))))
    
    (let [field {:type :type-int32
                 :constraints {:buf.validate {:int32 {:not-in [4 5 6]}}}}
          result (compiler/compile-field-constraints field)
          not-in-constraint (first (:schema result))]
      (is (= :not (first not-in-constraint)))
      (is (= [:enum [4 5 6]] (second not-in-constraint))))))

;; =============================================================================
;; String Constraints
;; =============================================================================

(deftest test-string-constraints
  (testing "String length constraints"
    (let [field {:type :type-string
                 :constraints {:buf.validate {:string {:min-len 5 :max-len 20}}}}
          result (compiler/compile-field-constraints field)]
      (is (some? (:props result)))
      (is (= 5 (:min (:props result))))
      (is (= 20 (:max (:props result))))))
  
  (testing "String pattern constraint"
    (let [field {:type :type-string
                 :constraints {:buf.validate {:string {:pattern "^[A-Z]+$"}}}}
          result (compiler/compile-field-constraints field)]
      (is (= :re (first (first (:schema result)))))))
  
  (testing "String email constraint"
    (let [field {:type :type-string
                 :constraints {:buf.validate {:string {:email true}}}}
          result (compiler/compile-field-constraints field)]
      (is (= :re (first (first (:schema result)))))
      (is (= :email (:type (:generator result))))))
  
  (testing "String prefix/suffix constraints"
    (let [field {:type :type-string
                 :constraints {:buf.validate {:string {:prefix "http://"}}}}
          result (compiler/compile-field-constraints field)
          prefix-constraint (first (:schema result))]
      (is (= :re (first prefix-constraint)))
      (is (instance? java.util.regex.Pattern (second prefix-constraint))))))

;; =============================================================================
;; Schema Enhancement
;; =============================================================================

(deftest test-schema-enhancement
  (testing "Enhance simple schema with constraints"
    (let [base-schema :double
          compiled {:schema [[:>= 0] [:< 360]]}
          enhanced (compiler/enhance-schema-with-constraints base-schema compiled)]
      (is (= [:and :double [:>= 0] [:< 360]] enhanced))))
  
  (testing "Enhance with single constraint"
    (let [base-schema :string
          compiled {:schema [[:re #"test"]]}
          enhanced (compiler/enhance-schema-with-constraints base-schema compiled)]
      ;; Regex objects don't compare as equal, check structure instead
      (is (= :re (first enhanced)))
      (is (instance? java.util.regex.Pattern (second enhanced)))))
  
  (testing "No enhancement when no constraints"
    (let [base-schema :int
          enhanced (compiler/enhance-schema-with-constraints base-schema nil)]
      (is (= :int enhanced)))))

;; =============================================================================
;; Integration Tests
;; =============================================================================

(deftest test-apply-constraints-integration
  (testing "Apply constraints to float field"
    (let [field {:type :type-float
                 :constraints {:buf.validate {:float {:gte 0 :lt 360}}}}
          base-schema :double
          enhanced (compiler/apply-constraints base-schema field)]
      (is (= [:and :double [:>= 0] [:< 360]] enhanced))
      ;; Test validation
      (is (m/validate enhanced 180.0))
      (is (m/validate enhanced 0.0))
      (is (not (m/validate enhanced 360.0)))
      (is (not (m/validate enhanced -1.0)))))
  
  (testing "Apply constraints to string field"
    (let [field {:type :type-string
                 :constraints {:buf.validate {:string {:min-len 3 :max-len 10}}}}
          base-schema :string
          enhanced (compiler/apply-constraints base-schema field)]
      ;; Should return a string schema with properties
      (is (= [:string {:min 3 :max 10}] enhanced))
      (is (m/validate enhanced "hello"))
      (is (not (m/validate enhanced "hi")))
      (is (not (m/validate enhanced "this is too long"))))))

;; =============================================================================
;; Generator Tests
;; =============================================================================

(deftest test-constrained-generators
  (testing "Generate values within float constraints"
    (let [field {:type :type-float
                 :constraints {:buf.validate {:float {:gte 0 :lt 360}}}}
          base-schema :double
          enhanced (compiler/apply-constraints base-schema field)
          ;; Generate 100 samples
          samples (repeatedly 100 #(mg/generate enhanced))]
      (is (every? #(and (>= % 0) (< % 360)) samples))))
  
  (testing "Generate strings with length constraints"
    (let [field {:type :type-string
                 :constraints {:buf.validate {:string {:min-len 5 :max-len 10}}}}
          base-schema :string
          enhanced (compiler/apply-constraints base-schema field)
          samples (repeatedly 50 #(mg/generate enhanced))]
      (is (every? #(and (>= (count %) 5) (<= (count %) 10)) samples)))))

;; =============================================================================
;; Edge Cases
;; =============================================================================

(deftest test-edge-cases
  (testing "No constraints returns nil"
    (let [field {:type :type-float}
          result (compiler/compile-field-constraints field)]
      (is (nil? result))))
  
  (testing "Unknown constraint type uses default"
    (let [field {:type :type-float
                 :constraints {:buf.validate {:unknown {:foo "bar"}}}}
          result (compiler/compile-field-constraints field)]
      (is (nil? result))))
  
  (testing "Empty constraints map"
    (let [field {:type :type-float
                 :constraints {:buf.validate {:float {}}}}
          result (compiler/compile-field-constraints field)]
      (is (empty? (:schema result))))))