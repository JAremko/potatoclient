(ns proto-explorer.oneof-test
  "Comprehensive tests for custom :oneof spec and its generators"
  (:require [clojure.test :refer :all]
            [potatoclient.specs.malli-oneof :as oneof]
            [proto-explorer.test-data-generator :as tdg]
            [proto-explorer.generated-specs :as gs]
            [malli.core :as m]
            [malli.generator :as mg]))

(deftest test-oneof-schema
  (testing "Basic oneof validation"
    (let [schema [:oneof {:ping [:maybe :map]
                         :noop [:maybe :map]
                         :frozen [:maybe :map]}]
          registry (merge (m/default-schemas)
                         {:oneof oneof/-oneof-schema})]
      
      (testing "Valid single option"
        (is (m/validate schema {:ping {}} {:registry registry}))
        (is (m/validate schema {:noop {}} {:registry registry}))
        (is (m/validate schema {:frozen {}} {:registry registry})))
      
      (testing "Invalid - no options"
        (is (not (m/validate schema {} {:registry registry}))))
      
      (testing "Invalid - multiple options"
        (is (not (m/validate schema {:ping {} :noop {}} {:registry registry}))))
      
      (testing "Invalid - unknown option"
        (is (not (m/validate schema {:unknown {}} {:registry registry})))))))

(deftest test-nested-oneof
  (testing "Nested oneof structures"
    (let [inner-oneof [:oneof {:a :string
                              :b :int}]
          outer-oneof [:oneof {:option1 [:map [:data inner-oneof]]
                              :option2 :double}]
          registry (merge (m/default-schemas)
                         {:oneof oneof/-oneof-schema})]
      
      (testing "Valid nested structure"
        (is (m/validate outer-oneof {:option1 {:data {:a "hello"}}} {:registry registry}))
        (is (m/validate outer-oneof {:option1 {:data {:b 42}}} {:registry registry}))
        (is (m/validate outer-oneof {:option2 3.14} {:registry registry})))
      
      (testing "Invalid nested structure"
        (is (not (m/validate outer-oneof {:option1 {:data {:a "hello" :b 42}}} {:registry registry})))
        (is (not (m/validate outer-oneof {:option1 {:data {}}} {:registry registry})))))))

(deftest test-oneof-generator
  (testing "Oneof generator produces valid data"
    (let [schema [:oneof {:ping [:maybe :map]
                         :noop [:maybe :map]
                         :frozen [:maybe :map]}]
          registry (merge (m/default-schemas)
                         {:oneof oneof/-oneof-schema})
          generator (mg/generator schema {:registry registry})]
      
      (testing "Generated data is valid"
        (dotimes [_ 50]
          (let [data (mg/generate generator)]
            (is (m/validate schema data {:registry registry}))
            (is (= 1 (count data)) "Should have exactly one key")
            (is (contains? #{:ping :noop :frozen} (first (keys data))))))))))

(deftest test-oneof-with-complex-options
  (testing "Oneof with complex nested structures"
    (let [schema [:oneof {:rotary [:map 
                                  [:channel [:enum "heat" "day"]]
                                  [:x :double]
                                  [:y :double]]
                         :gps [:map
                              [:lat [:and :double [:>= -90] [:<= 90]]]
                              [:lon [:and :double [:>= -180] [:<= 180]]]
                              [:alt [:maybe :double]]]
                         :command [:map
                                  [:action :string]
                                  [:params [:vector :any]]]}]
          registry (merge (m/default-schemas)
                         {:oneof oneof/-oneof-schema})]
      
      (testing "Valid complex data"
        (is (m/validate schema {:rotary {:channel "heat" :x 0.5 :y -0.5}} {:registry registry}))
        (is (m/validate schema {:gps {:lat 45.0 :lon -122.0 :alt 100.0}} {:registry registry}))
        (is (m/validate schema {:command {:action "test" :params [1 2 3]}} {:registry registry})))
      
      (testing "Invalid complex data"
        (is (not (m/validate schema {:rotary {:channel "invalid" :x 0.5 :y -0.5}} {:registry registry})))
        (is (not (m/validate schema {:gps {:lat 91.0 :lon -122.0}} {:registry registry})))))))

(deftest test-oneof-generator-with-tdg
  (testing "Test data generator handles oneof correctly"
    (let [schema [:oneof {:ping [:maybe :map]
                         :noop [:maybe :map]
                         :frozen [:maybe :map]}]
          generator (tdg/create-generator schema)]
      
      (testing "TDG generator produces valid oneof data"
        (dotimes [_ 20]
          (let [data (mg/generate generator {:registry (gs/proto-registry)})]
            (is (map? data))
            (is (= 1 (count data)))
            (is (contains? #{:ping :noop :frozen} (first (keys data))))))))))

(deftest test-nested-oneof-generator
  (testing "Nested oneof generation"
    (let [inner-schema [:oneof {:a :string
                               :b :int}]
          outer-schema [:oneof {:outer1 [:map [:inner inner-schema]]
                               :outer2 :boolean}]
          generator (tdg/create-generator outer-schema)]
      
      (testing "Generates valid nested structures"
        (dotimes [_ 20]
          (let [data (mg/generate generator {:registry (gs/proto-registry)})]
            (is (map? data))
            (is (= 1 (count data)))
            (when (contains? data :outer1)
              (let [inner (:inner (:outer1 data))]
                (is (map? inner))
                (is (or (contains? inner :a)
                       (contains? inner :b)))))))))))

(deftest test-oneof-explain
  (testing "Oneof validation explanations"
    (let [schema [:oneof {:ping [:maybe :map]
                         :noop [:maybe :map]}]
          registry (merge (m/default-schemas)
                         {:oneof oneof/-oneof-schema})]
      
      (testing "Explain for empty map"
        (let [explanation (m/explain schema {} {:registry registry})]
          (is (some? explanation))
          (is (seq (:errors explanation)))))
      
      (testing "Explain for multiple options"
        (let [explanation (m/explain schema {:ping {} :noop {}} {:registry registry})]
          (is (some? explanation))
          (is (seq (:errors explanation)))))
      
      (testing "Explain for invalid option"
        (let [explanation (m/explain schema {:unknown {}} {:registry registry})]
          (is (some? explanation))
          (is (seq (:errors explanation))))))))

(deftest test-oneof-properties
  (testing "Oneof schema properties"
    (let [schema [:oneof {:a :string
                         :b :int
                         :c :boolean}]
          registry (merge (m/default-schemas)
                         {:oneof oneof/-oneof-schema})]
      
      (testing "Schema type detection"
        (let [s (m/schema schema {:registry registry})]
          (is (= :oneof (m/type s)))))
      
      (testing "Schema properties"
        (let [s (m/schema schema {:registry registry})
              props (m/properties s)]
          (is (map? props))
          (is (contains? props :a))
          (is (contains? props :b))
          (is (contains? props :c)))))))

(deftest test-oneof-with-constraints
  (testing "Oneof with constrained values"
    (let [schema [:oneof {:velocity [:map 
                                   [:azimuth [:and :double [:>= 0] [:< 360]]]
                                   [:elevation [:and :double [:>= -90] [:<= 90]]]
                                   [:direction [:and :int [:not [:enum 0]]]]]  ; Changed [0] to 0
                         :position [:map
                                   [:x [:and :double [:>= -1] [:<= 1]]]
                                   [:y [:and :double [:>= -1] [:<= 1]]]]}]
          generator (tdg/create-generator schema)]
      
      (testing "Generated data respects constraints"
        (dotimes [_ 50]
          (let [data (mg/generate generator {:registry (gs/proto-registry)})]
            (cond
              (contains? data :velocity)
              (let [vel (:velocity data)]
                (is (and (>= (:azimuth vel) 0) (< (:azimuth vel) 360)))
                (is (and (>= (:elevation vel) -90) (<= (:elevation vel) 90)))
                (is (not= (:direction vel) 0)))
              
              (contains? data :position)
              (let [pos (:position data)]
                (is (and (>= (:x pos) -1) (<= (:x pos) 1)))
                (is (and (>= (:y pos) -1) (<= (:y pos) 1)))))))))))

(deftest test-oneof-schema-form
  (testing "Oneof schema form and children"
    (let [schema [:oneof {:a [:map [:a :string]]
                         :b [:map [:b :int]]}]
          registry (merge (m/default-schemas)
                         {:oneof oneof/-oneof-schema})]
      
      (testing "Schema form"
        (is (= schema (m/form schema {:registry registry}))))
      
      (testing "Schema ast"
        (let [ast (m/ast schema {:registry registry})]
          (is (= :oneof (:type ast)))
          (is (map? (:properties ast))))))))

(deftest test-oneof-edge-cases
  (testing "Edge cases for oneof"
    (let [registry (merge (m/default-schemas)
                         {:oneof oneof/-oneof-schema})]
      
      (testing "Empty oneof options"
        ;; Empty oneofs should throw an error during schema creation
        (is (thrown? Exception
                     (m/schema [:oneof {}] {:registry registry}))))
      
      (testing "Oneof with nil values"
        (let [schema [:oneof {:a [:maybe :string]
                             :b [:maybe :int]}]]
          (is (m/validate schema {:a nil} {:registry registry}))
          (is (m/validate schema {:b nil} {:registry registry}))))
      
      (testing "Deeply nested oneofs"
        (let [level3 [:oneof {:x [:map [:x :int]] :y [:map [:y :int]]}]
              level2 [:oneof {:m [:map [:data level3]] :n [:map [:flag :boolean]]}]
              level1 [:oneof {:p [:map [:nested level2]] :q [:map [:simple :string]]}]]
          ;; The correct data structure: level3 expects {:x {:x 42}} not {:x 42}
          (is (m/validate level1 
                         {:p {:nested {:m {:data {:x {:x 42}}}}}} 
                         {:registry registry})))))))