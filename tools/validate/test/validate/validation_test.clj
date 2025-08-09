(ns validate.validation-test
  "Tests for validate validation functionality."
  (:require [clojure.test :refer [deftest testing is]]
            [validate.validator :as v]
            [validate.test-harness :as h]))

(deftest test-valid-state
  (testing "Valid state passes validation"
    (let [result (v/validate-binary (h/valid-state-bytes) :type :state)]
      (is (:valid? result))
      (is (empty? (:violations result)))
      (is (= :state (:message-type result))))))

(deftest test-valid-commands
  (testing "Ping command validates"
    (let [result (v/validate-binary (h/valid-ping-bytes) :type :cmd)]
      (is (:valid? result))
      (is (= :cmd (:message-type result)))))
  
  (testing "Noop command validates"
    (let [result (v/validate-binary (h/valid-noop-bytes) :type :cmd)]
      (is (:valid? result))))
  
  (testing "Frozen command validates"
    (let [result (v/validate-binary (h/valid-frozen-bytes) :type :cmd)]
      (is (:valid? result)))))

(deftest test-invalid-gps
  (testing "Out-of-range GPS detected"
    (let [result (v/validate-binary (h/invalid-gps-state-bytes) :type :state)]
      (is (not (:valid? result)))
      (is (seq (:violations result))))))

(deftest test-invalid-protocol
  (testing "Invalid protocol in state"
    (let [result (v/validate-binary (h/invalid-protocol-state-bytes) :type :state)]
      (is (not (:valid? result)))))
  
  (testing "Invalid protocol in command"
    (let [result (v/validate-binary (h/invalid-protocol-cmd-bytes) :type :cmd)]
      (is (not (:valid? result))))))

(deftest test-invalid-client-type
  (testing "UNSPECIFIED client type rejected"
    (let [result (v/validate-binary (h/invalid-client-cmd-bytes) :type :cmd)]
      (is (not (:valid? result))))))

(deftest test-missing-fields
  (testing "Missing required fields detected"
    (let [result (v/validate-binary (h/missing-fields-state-bytes) :type :state)]
      (is (not (:valid? result)))
      (is (seq (:violations result))))))

(deftest test-boundary-values
  (testing "Boundary values accepted"
    (let [result (v/validate-binary (h/boundary-state-bytes) :type :state)]
      (is (:valid? result))
      (is (empty? (:violations result))))))

(deftest test-auto-detection
  (testing "Auto-detects state type"
    (let [result (v/validate-binary (h/valid-state-bytes))]
      (is (:valid? result))
      (is (= :state (:message-type result)))))
  
  (testing "Auto-detects command type"
    (let [result (v/validate-binary (h/valid-ping-bytes))]
      (is (:valid? result))
      (is (= :cmd (:message-type result))))))

(deftest test-error-handling
  (testing "Empty data throws"
    (is (thrown? Exception 
                 (v/validate-binary (byte-array 0)))))
  
  (testing "Nil input throws"
    (is (thrown? Exception 
                 (v/validate-binary nil))))
  
  (testing "Wrong type hint results in invalid"
    (let [result (v/validate-binary (h/valid-state-bytes) :type :cmd)]
      (is (not (:valid? result)))
      (is (> (count (:violations result)) 1)))))