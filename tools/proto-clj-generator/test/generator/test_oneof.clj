(ns generator.test-oneof
  "Basic tests for custom :oneof spec"
  (:require [clojure.test :refer :all]
            [potatoclient.specs.malli-oneof :as oneof]
            [malli.core :as m]
            [malli.generator :as mg]
            [malli.registry :as mr]))

(def test-registry
  (merge (m/default-schemas)
         (mr/schemas m/default-registry)
         {:oneof oneof/-oneof-schema}))

(deftest test-basic-oneof
  (testing "Basic oneof validation"
    (let [schema [:oneof {:a :string :b :int}]]
      (is (m/validate schema {:a "hello"} {:registry test-registry}))
      (is (m/validate schema {:b 42} {:registry test-registry}))
      (is (not (m/validate schema {} {:registry test-registry})))
      (is (not (m/validate schema {:a "hello" :b 42} {:registry test-registry}))))))

(deftest test-oneof-generator
  (testing "Oneof generator"
    (let [schema [:oneof {:a :string :b :int}]
          gen (mg/generator schema {:registry test-registry})]
      (dotimes [_ 5]
        (let [value (mg/generate gen)]
          (is (m/validate schema value {:registry test-registry}))
          (is (= 1 (count value))))))))