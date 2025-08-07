(ns generator.oneof-basic-test-temp
  "Basic tests for custom :oneof spec without dependencies on generated code"
  (:require [clojure.test :refer :all]
            [potatoclient.specs.malli-oneof :as oneof]
            [malli.core :as m]
            [malli.generator :as mg]
            [malli.registry :as mr]))

;; =============================================================================
;; Registry Setup
;; =============================================================================

(def test-registry
  "Test registry with custom :oneof schema"
  (merge (m/default-schemas)
         (mr/schemas m/default-registry)
         {:oneof oneof/-oneof-schema}))

;; =============================================================================
;; Basic Oneof Tests
;; =============================================================================

(deftest test-oneof-validation
  (testing "Basic oneof validation"
    (let [schema [:oneof {:ping [:map [:value :string]]
                         :pong [:map [:value :int]]}]]
      
      (testing "Valid - single option set"
        (is (m/validate schema {:ping {:value "hello"}} {:registry test-registry}))
        (is (m/validate schema {:pong {:value 42}} {:registry test-registry})))
      
      (testing "Invalid - no option set"
        (is (not (m/validate schema {} {:registry test-registry}))))
      
      (testing "Invalid - multiple options set"
        (is (not (m/validate schema {:ping {:value "hello"} :pong {:value 42}} {:registry test-registry}))))
      
      (testing "Invalid - unknown option"
        (is (not (m/validate schema {:unknown {:value "test"}} {:registry test-registry})))))))

(deftest test-oneof-generator
  (testing "Oneof generator"
    (let [schema [:oneof {:a :string :b :int :c :boolean}]]
      
      (testing "Can create generator"
        (is (some? (mg/generator schema {:registry test-registry}))))
      
      (testing "Generated values are valid"
        (let [gen (mg/generator schema {:registry test-registry})]
          (dotimes [_ 10]
            (let [value (mg/generate gen)]
              (is (m/validate schema value {:registry test-registry}))
              (is (= 1 (count value)) "Should have exactly one key")
              (is (contains? #{:a :b :c} (first (keys value))) "Should be a valid option"))))))))

(deftest test-oneof-with-constraints
  (testing "Oneof with constrained values"
    (let [schema [:oneof {:temp [:and :double [:>= -273.15] [:<= 1000]]
                         :pressure [:and :double [:> 0] [:<= 10000]]}]]
      
      (testing "Valid constrained values"
        (is (m/validate schema {:temp 25.0} {:registry test-registry}))
        (is (m/validate schema {:pressure 101.325} {:registry test-registry})))
      
      (testing "Invalid - constraint violation"
        (is (not (m/validate schema {:temp -300.0} {:registry test-registry})))
        (is (not (m/validate schema {:pressure 0.0} {:registry test-registry}))) ; Must be > 0
        (is (not (m/validate schema {:pressure 10001.0} {:registry test-registry})))))))

(deftest test-nested-oneof
  (testing "Nested oneof structures"
    (let [inner [:oneof {:metric :double :imperial :double}]
          outer [:oneof {:distance [:map [:value inner]]
                        :time [:map [:seconds :int]]}]]
      
      (testing "Valid nested structure"
        (is (m/validate outer {:distance {:value {:metric 100.0}}} {:registry test-registry}))
        (is (m/validate outer {:distance {:value {:imperial 328.084}}} {:registry test-registry}))
        (is (m/validate outer {:time {:seconds 60}} {:registry test-registry})))
      
      (testing "Invalid - multiple inner options"
        (is (not (m/validate outer {:distance {:value {:metric 100.0 :imperial 328.084}}} {:registry test-registry})))))))

(deftest test-error-messages
  (testing "Oneof error messages"
    (let [schema [:oneof {:a :string :b :int}]]
      
      (testing "Empty map error"
        (let [result (m/explain schema {} {:registry test-registry})]
          (is (some? result))
          (is (seq (:errors result)))))
      
      (testing "Multiple options error"
        (let [result (m/explain schema {:a "hello" :b 42} {:registry test-registry})]
          (is (some? result))
          (is (seq (:errors result))))))))

(deftest test-schema-properties
  (testing "Oneof schema properties"
    (let [schema [:oneof {:option1 :string
                         :option2 :int
                         :option3 :boolean}]
          s (m/schema schema {:registry test-registry})]
      
      (testing "Schema type"
        (is (= :oneof (m/type s))))
      
      (testing "Schema properties contain options"
        (let [props (m/properties s)]
          (is (contains? props :option1))
          (is (contains? props :option2))
          (is (contains? props :option3))))
      
      (testing "Schema form"
        (is (= schema (m/form s)))))))

(deftest test-performance
  (testing "Oneof validation performance"
    (let [schema [:oneof {:a :string :b :int :c :boolean}]
          test-data {:b 42}
          
          ;; Warm up
          _ (dotimes [_ 100]
              (m/validate schema test-data {:registry test-registry}))
          
          ;; Measure
          start (System/nanoTime)
          iterations 1000
          _ (dotimes [_ iterations]
              (m/validate schema test-data {:registry test-registry}))
          end (System/nanoTime)
          duration-ms (/ (- end start) 1e6)
          per-validation-us (/ duration-ms iterations 0.001)]
      
      (println (format "\nOneof validation: %.2f μs per validation" per-validation-us))
      
      (is (< per-validation-us 100) 
          "Validation should be fast")))))