(ns proto-explorer.test-data-generator-test
  (:require [clojure.test :refer :all]
            [proto-explorer.test-data-generator :as gen]
            [proto-explorer.generated-specs :as specs]
            [malli.core :as m]
            [malli.generator :as mg]))

(deftest test-analyze-schema
  (testing "Analyze simple schemas"
    (is (= {:type :string} 
           (gen/analyze-schema :string)))
    (is (= {:type :int} 
           (gen/analyze-schema :int))))
  
  (testing "Analyze schemas with properties"
    (is (= {:type :string :props {:min 5 :max 10}}
           (gen/analyze-schema [:string {:min 5 :max 10}]))))
  
  (testing "Analyze :and schemas"
    (let [result (gen/analyze-schema [:and :double [:>= 0] [:< 360]])]
      (is (= :double (:type result)))
      (is (= [[:>= 0] [:< 360]] (:constraints result)))))
  
  (testing "Analyze :not schemas"
    (let [result (gen/analyze-schema [:not [:enum [0]]])]
      (is (= :not (:type result)))
      (is (= :enum (get-in result [:inner :type]))))))

(deftest test-constraint->generator-hint
  (testing "Numeric constraints"
    (is (= {:gte 0} (gen/constraint->generator-hint [:>= 0])))
    (is (= {:gt 0} (gen/constraint->generator-hint [:> 0])))
    (is (= {:lte 100} (gen/constraint->generator-hint [:<= 100])))
    (is (= {:lt 100} (gen/constraint->generator-hint [:< 100])))
    (is (= {:const 42} (gen/constraint->generator-hint [:= 42]))))
  
  (testing "Enum constraints"
    (is (= {:in [1 2 3]} (gen/constraint->generator-hint [:enum [1 2 3]])))
    (is (= {:not-in [0]} (gen/constraint->generator-hint [:not [:enum [0]]])))))

(deftest test-merge-generator-hints
  (testing "Merge multiple constraints"
    (is (= {:gte 0 :lt 360}
           (gen/merge-generator-hints [[:>= 0] [:< 360]])))
    (is (= {:gt -90 :lte 90}
           (gen/merge-generator-hints [[:> -90] [:<= 90]])))))

(deftest test-generate-constrained-string
  (testing "String with length constraints"
    (let [gen (gen/generate-constrained-string {:min 5 :max 10})]
      (dotimes [_ 10]
        (let [s (mg/generate gen)]
          (is (>= (count s) 5))
          (is (<= (count s) 10))))))
  
  ;; Skip pattern test - requires test.chuck library
  #_(testing "String with pattern"
    (let [gen (gen/generate-constrained-string {:pattern "^[A-Z]+$"})]
      (dotimes [_ 10]
        (let [s (mg/generate gen)]
          (is (re-matches #"^[A-Z]+$" s))))))
  
  (testing "String with prefix"
    (let [gen (gen/generate-constrained-string {:prefix "http://" :min 10})]
      ;; Note: This returns a generator that generates strings
      (dotimes [_ 5]
        (let [s (mg/generate gen)]
          (is (string? s))
          (is (clojure.string/starts-with? s "http://")))))))

(deftest test-generate-constrained-number
  (testing "Number with range"
    (let [gen (gen/generate-constrained-number :double {:gte 0 :lt 360})]
      (dotimes [_ 20]
        (let [n (mg/generate gen)]
          (is (>= n 0))
          (is (< n 360))))))
  
  (testing "Number with constant"
    (let [gen (gen/generate-constrained-number :int {:const 42})]
      (is (= 42 (mg/generate gen)))
      (is (= 42 (mg/generate gen)))))
  
  (testing "Number from allowed values"
    (let [gen (gen/generate-constrained-number :int {:in [1 2 3]})]
      (dotimes [_ 10]
        (is (contains? #{1 2 3} (mg/generate gen))))))
  
  (testing "Number excluding values"
    (let [gen (gen/generate-constrained-number :int {:not-in [0]})]
      (dotimes [_ 20]
        (is (not= 0 (mg/generate gen)))))))

(deftest test-create-generator
  (testing "Simple type generator"
    (let [gen (gen/create-generator :string)]
      (is (string? (mg/generate gen)))))
  
  (testing "Constrained number generator"
    (let [schema [:and :double [:>= -1] [:<= 1]]
          gen (gen/create-generator schema)]
      (dotimes [_ 10]
        (let [n (mg/generate gen)]
          (is (number? n))
          (is (>= n -1))
          (is (<= n 1))))))
  
  (testing ":not [:enum [0]] generator"
    (let [generator (gen/create-generator [:not [:enum [0]]])]
      (dotimes [_ 20]
        (let [val (mg/generate generator {:registry (specs/proto-registry)})]
          (is (not= 0 val))))))
  
  (testing ":maybe generator"
    (let [gen (gen/create-generator [:maybe :string])
          results (repeatedly 50 #(mg/generate gen {:registry (specs/proto-registry)}))]
      ;; Should have both nil and string values
      (is (some nil? results))
      (is (some string? results)))))

;; Integration test with actual specs
(deftest test-generate-data-integration
  ;; First load the specs
  (specs/load-all-specs! "../../shared/specs/protobuf")
  
  (testing "Generate data for constrained spec"
    ;; This spec has constraints: value [0, 360), direction not 0
    (when (specs/get-spec :cmd.RotaryPlatform/set-azimuth-value)
      (dotimes [_ 10]
        (let [data (gen/generate-data :cmd.RotaryPlatform/set-azimuth-value)]
          (is (map? data))
          (when (:value data)
            (is (>= (:value data) 0))
            (is (< (:value data) 360)))
          (when (:direction data)
            (is (not= 0 (:direction data))))))))
  
  (testing "Validate round-trip"
    (when (specs/get-spec :cmd/ping)
      (let [result (gen/validate-roundtrip :cmd/ping)]
        (is (:valid? result))
        (is (map? (:data result)))))))

(deftest test-property-based-testing
  (testing "Property test for constrained values"
    ;; Create a simple test schema
    (let [test-schema [:and :int [:>= 0] [:<= 100]]
          test-fn (fn [data]
                    (and (>= data 0) (<= data 100)))
          gen (gen/create-generator test-schema)]
      ;; Run property test
      (dotimes [_ 50]
        (is (test-fn (mg/generate gen)))))))

(deftest test-generate-examples
  ;; Load specs first
  (specs/load-all-specs! "../../shared/specs/protobuf")
  
  (testing "Generate multiple examples"
    (when (specs/get-spec :cmd/ping)
      (let [examples (gen/generate-examples :cmd/ping 5)]
        (is (= 5 (count examples)))
        (is (every? map? examples))))))