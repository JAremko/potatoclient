(ns potatoclient.ui.status-bar.validation-test
  "Tests for status bar validation functionality."
  (:require [clojure.test :refer [deftest is testing]]
            [malli.core :as m]
            [potatoclient.state :as state]
            [potatoclient.ui.status-bar.validation :as validation]))

(deftest test-validate
  (testing "validate function"
    (testing "with valid values"
      ;; Reset state before test
      (reset! state/app-state state/initial-state)

      (is (= true (validation/validate :int 42))
          "Should return true for valid integer")

      (is (= true (validation/validate :string "hello"))
          "Should return true for valid string")

      (is (= true (validation/validate [:map [:x :int]] {:x 1}))
          "Should return true for valid map")

      ;; Check no error was set (empty string is the initial state)
      (is (= "" (get-in @state/app-state [:ui :status :message]))
          "Should not set error message for valid values"))

    (testing "with invalid values"
      ;; Reset state before test
      (reset! state/app-state state/initial-state)

      ;; Capture logs to verify logging happens
      (let [log-output (with-out-str
                         (binding [*err* *out*]
                           (validation/validate :int "not-int")))]
        (is (= false (validation/validate :int "not-int"))
            "Should return false for invalid integer")

        ;; Check that logging occurred
        (is (re-find #"WARN.*Validation failed" log-output)
            "Should log validation failure")

        (is (re-find #":spec :int" log-output)
            "Should log the spec")

        (is (re-find #":value \"not-int\"" log-output)
            "Should log the invalid value"))

      ;; Check error was set
      (is (= :error (get-in @state/app-state [:ui :status :type]))
          "Should set error status for invalid values")

      (is (string? (get-in @state/app-state [:ui :status :message]))
          "Should set error message for invalid values")

      ;; Reset state
      (reset! state/app-state state/initial-state)

      (is (= false (validation/validate [:map [:x :int]] {:x "not-int"}))
          "Should return false for invalid map")

      (is (= :error (get-in @state/app-state [:ui :status :type]))
          "Should set error status for invalid map"))

    (testing "with keyword specs from registry"
      ;; Assuming we have some registered specs
      (is (boolean? (validation/validate :int 42))
          "Should work with keyword specs from registry"))))

(deftest test-validate-with-details
  (testing "validate-with-details function"
    (testing "with valid values"
      ;; Reset state
      (reset! state/app-state state/initial-state)

      (let [result (validation/validate-with-details :int 42)]
        (is (= true (:valid? result))
            "Should return valid? true for valid value")

        (is (nil? (:errors result))
            "Should not have errors for valid value")))

    (testing "with invalid values"
      ;; Reset state
      (reset! state/app-state state/initial-state)

      (let [result (validation/validate-with-details :int "not-int")]
        (is (= false (:valid? result))
            "Should return valid? false for invalid value")

        (is (some? (:errors result))
            "Should have errors for invalid value")

        (is (= :error (get-in @state/app-state [:ui :status :type]))
            "Should set error status"))

      ;; Test complex validation
      (reset! state/app-state state/initial-state)

      (let [result (validation/validate-with-details
                     [:map [:x :int] [:y :string]]
                     {:x "not-int" :y 123})]
        (is (= false (:valid? result))
            "Should return false for invalid map")

        (is (map? (:errors result))
            "Should return error details as map")

        (is (contains? (:errors result) :x)
            "Should have error for :x field")

        (is (contains? (:errors result) :y)
            "Should have error for :y field")))))

(deftest test-valid?
  (testing "valid? predicate"
    (testing "does not report to status bar"
      ;; Reset state
      (reset! state/app-state state/initial-state)

      (is (= false (validation/valid? :int "not-int"))
          "Should return false for invalid value")

      ;; Check status message remains as initial state (empty string)
      (is (= "" (get-in @state/app-state [:ui :status :message]))
          "Should not set error message")

      (is (= true (validation/valid? :int 42))
          "Should return true for valid value")

      ;; Check status message still empty
      (is (= "" (get-in @state/app-state [:ui :status :message]))
          "Should not set any message"))))

(deftest test-explain-validation
  (testing "explain-validation function"
    (testing "with valid values"
      ;; Reset state before test
      (reset! state/app-state state/initial-state)
      
      (is (nil? (validation/explain-validation :int 42))
          "Should return nil for valid values"))

    (testing "with invalid values"
      ;; Reset state before test
      (reset! state/app-state state/initial-state)
      
      (let [errors (validation/explain-validation :int "not-int")]
        (is (some? errors)
            "Should return errors for invalid value")

        (is (or (string? errors) (vector? errors) (map? errors))
            "Should return humanized errors"))

      (let [errors (validation/explain-validation
                     [:map [:x :int]]
                     {:x "not-int"})]
        (is (map? errors)
            "Should return map of errors for invalid map")

        (is (contains? errors :x)
            "Should have error for invalid field")))

    (testing "does not report to status bar"
      ;; Reset state
      (reset! state/app-state state/initial-state)

      (validation/explain-validation :int "not-int")

      ;; Check status message remains as initial state (empty string)
      (is (= "" (get-in @state/app-state [:ui :status :message]))
          "Should not set any status message"))))

(deftest test-complex-schemas
  (testing "validation with complex Malli schemas"
    (testing "nested maps"
      ;; Reset state before test
      (reset! state/app-state state/initial-state)
      
      (let [schema [:map
                    [:user [:map
                            [:name :string]
                            [:age [:int {:min 0 :max 150}]]]]]]
        (is (validation/validate schema {:user {:name "John" :age 30}})
            "Should validate valid nested map")

        (is (not (validation/validate schema {:user {:name "John" :age -1}}))
            "Should reject invalid age")

        (is (not (validation/validate schema {:user {:name 123 :age 30}}))
            "Should reject invalid name type")))

    (testing "vectors and sequences"
      ;; Reset state before test
      (reset! state/app-state state/initial-state)
      
      (let [schema [:vector :int]]
        (is (validation/validate schema [1 2 3])
            "Should validate vector of integers")

        (is (not (validation/validate schema [1 "two" 3]))
            "Should reject mixed types"))

      (let [schema [:sequential {:min 1 :max 5} :keyword]]
        (is (validation/validate schema [:a :b :c])
            "Should validate sequence of keywords")

        (is (not (validation/validate schema []))
            "Should reject empty sequence when min is 1")

        (is (not (validation/validate schema [:a :b :c :d :e :f]))
            "Should reject sequence longer than max")))

    (testing "optional keys"
      ;; Reset state before test
      (reset! state/app-state state/initial-state)
      
      (let [schema [:map
                    [:required :string]
                    [:optional {:optional true} :int]]]
        (is (validation/validate schema {:required "test"})
            "Should validate without optional key")

        (is (validation/validate schema {:required "test" :optional 42})
            "Should validate with optional key")

        (is (not (validation/validate schema {:optional 42}))
            "Should reject missing required key")))))