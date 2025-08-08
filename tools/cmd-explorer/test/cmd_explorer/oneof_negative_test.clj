(ns cmd-explorer.oneof-negative-test
  "Negative tests for oneof-pronto spec"
  (:require
   [clojure.test :refer [deftest testing is]]
   [malli.core :as m]
   [cmd-explorer.core :as core]
   [pronto.core :as p])
  (:import
   [cmd JonSharedCmd$Root JonSharedCmd$Ping JonSharedCmd$Noop JonSharedCmd$Frozen]))

;; Initialize the system
(core/initialize!)

;; Define mapper for tests
(p/defmapper test-mapper [JonSharedCmd$Root JonSharedCmd$Ping JonSharedCmd$Noop JonSharedCmd$Frozen])

(deftest test-oneof-negative-cases
  (testing "Negative test cases for oneof validation"
    (let [spec [:oneof-pronto
                {:proto-class JonSharedCmd$Root
                 :proto-mapper test-mapper
                 :oneof-name :payload
                 :ping [:fn #(instance? JonSharedCmd$Ping %)]
                 :noop [:fn #(instance? JonSharedCmd$Noop %)]
                 :frozen [:fn #(instance? JonSharedCmd$Frozen %)]}]]
      
      (testing "Rejects nil value"
        (is (not (m/validate spec nil))))
      
      (testing "Rejects non-proto-map values"
        (is (not (m/validate spec {})))
        (is (not (m/validate spec {:ping "not-a-proto"})))
        (is (not (m/validate spec "string")))
        (is (not (m/validate spec 123)))
        (is (not (m/validate spec []))))
      
      (testing "Rejects proto-map with no oneof field set"
        (let [empty-proto (p/proto-map test-mapper JonSharedCmd$Root)]
          (is (not (m/validate spec empty-proto)))
          (is (nil? (p/which-one-of empty-proto :payload)))))
      
      (testing "Rejects wrong proto-map class"
        ;; Try to validate a Ping proto-map against Root spec
        (let [wrong-class (p/proto-map test-mapper JonSharedCmd$Ping)]
          (is (not (m/validate spec wrong-class)))))
      
      (testing "Explains validation errors properly"
        (let [empty-proto (p/proto-map test-mapper JonSharedCmd$Root)
              explanation (m/explain spec empty-proto)]
          (is (some? explanation))
          (is (seq (:errors explanation))))))))

(deftest test-oneof-wrong-field-names
  (testing "Oneof spec with wrong field names"
    (let [spec [:oneof-pronto
                {:proto-class JonSharedCmd$Root
                 :proto-mapper test-mapper
                 :oneof-name :payload
                 ;; These are wrong field names - should never match
                 :wrong_field1 [:any]
                 :wrong_field2 [:any]}]
          proto-with-ping (p/proto-map test-mapper JonSharedCmd$Root
                                      :ping (p/proto-map test-mapper JonSharedCmd$Ping))]
      
      (testing "Rejects proto-map with actual fields when spec expects wrong fields"
        ;; The proto has :ping set, but spec only knows about :wrong_field1 and :wrong_field2
        (is (not (m/validate spec proto-with-ping)))))))

(deftest test-oneof-wrong-oneof-name
  (testing "Oneof spec with wrong oneof field name"
    (let [spec [:oneof-pronto
                {:proto-class JonSharedCmd$Root
                 :proto-mapper test-mapper
                 :oneof-name :wrong_oneof_name  ;; This is not a real oneof field
                 :ping [:any]
                 :noop [:any]}]
          proto-with-ping (p/proto-map test-mapper JonSharedCmd$Root
                                      :ping (p/proto-map test-mapper JonSharedCmd$Ping))]
      
      (testing "Returns false when oneof field name doesn't exist"
        ;; Validation should fail gracefully
        (is (not (m/validate spec proto-with-ping)))))))

(deftest test-oneof-field-validation
  (testing "Oneof field value validation"
    (let [;; Spec that expects Ping instances for :ping field
          strict-spec [:oneof-pronto
                       {:proto-class JonSharedCmd$Root
                        :proto-mapper test-mapper
                        :oneof-name :payload
                        :ping [:fn #(instance? JonSharedCmd$Ping %)]
                        :noop [:fn #(instance? JonSharedCmd$Noop %)]
                        :frozen [:fn #(instance? JonSharedCmd$Frozen %)]}]
          valid-proto (p/proto-map test-mapper JonSharedCmd$Root
                                 :ping (p/proto-map test-mapper JonSharedCmd$Ping))]
      
      (testing "Accepts valid proto-map with correct field type"
        ;; Debug: check what p/which-one-of returns
        (is (= :ping (p/which-one-of valid-proto :payload)))
        (is (m/validate strict-spec valid-proto)))
      
      (testing "Field validation works correctly"
        ;; The field validator should check that the value is a Ping instance
        (let [ping-value (.getPing valid-proto)]
          (is (instance? JonSharedCmd$Ping ping-value)))))))