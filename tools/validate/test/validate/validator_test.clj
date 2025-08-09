(ns validate.validator-test
  "Core validator tests using idiomatic Pronto and Clojure patterns."
  (:require [clojure.test :refer [deftest testing is]]
            [validate.validator :as v]
            [validate.test-harness :as h]))

;; ============================================================================
;; VALIDATOR CREATION
;; ============================================================================

(deftest test-validator-creation
  (testing "Validator instance creation"
    (let [validator (v/create-validator)]
      (is (some? validator) "Validator should be created")
      (is (instance? build.buf.protovalidate.Validator validator)
          "Should be correct type"))))

;; ============================================================================
;; VALID MESSAGE VALIDATION
;; ============================================================================

(deftest test-valid-state-validation
  (testing "Valid state message from real data"
    (let [result (v/validate-binary (h/valid-state-bytes) :type :state)]
      (is (:valid? result) "Real state data should be valid")
      (is (= :state (:message-type result)) "Should identify as state")
      (is (pos? (:message-size result)) "Should report size")
      (is (empty? (:violations result)) "Should have no violations"))))

(deftest test-valid-command-validation
  (testing "Valid command messages"
    (testing "Ping command"
      (let [result (v/validate-binary (h/valid-ping-bytes) :type :cmd)]
        (is (:valid? result) "Ping should be valid")
        (is (= :cmd (:message-type result)) "Should identify as cmd")))
    
    (testing "Noop command"
      (let [result (v/validate-binary (h/valid-noop-bytes) :type :cmd)]
        (is (:valid? result) "Noop should be valid")))
    
    (testing "Frozen command"
      (let [result (v/validate-binary (h/valid-frozen-bytes) :type :cmd)]
        (is (:valid? result) "Frozen should be valid")))))

;; ============================================================================
;; AUTO-DETECTION
;; ============================================================================

(deftest test-message-auto-detection
  (testing "Auto-detection of message types"
    (testing "State auto-detection"
      (let [result (v/validate-binary (h/valid-state-bytes))]
        (is (:valid? result) "Should validate without type hint")
        (is (= :state (:message-type result)) "Should detect state type")))
    
    (testing "Command auto-detection"
      (let [result (v/validate-binary (h/valid-ping-bytes))]
        (is (:valid? result) "Should validate without type hint")
        (is (= :cmd (:message-type result)) "Should detect cmd type")))))

;; ============================================================================
;; INVALID MESSAGE VALIDATION
;; ============================================================================

(deftest test-invalid-gps-validation
  (testing "Invalid GPS coordinates"
    (let [result (v/validate-binary (h/invalid-gps-state-bytes) :type :state)]
      (is (map? result) "Should return result even for invalid data")
      (is (= :state (:message-type result)) "Should identify type")
      (when (seq (:violations result))
        (testing "GPS validation violations"
          (is (some #(re-find #"latitude" (str %)) (:violations result))
              "Should flag invalid latitude")
          (is (some #(re-find #"longitude" (str %)) (:violations result))
              "Should flag invalid longitude"))))))

(deftest test-invalid-protocol-validation
  (testing "Invalid protocol versions"
    (testing "State with protocol 0"
      (let [result (v/validate-binary (h/invalid-protocol-state-bytes) :type :state)]
        (is (not (:valid? result)) "Protocol 0 should be invalid")
        (is (seq (:violations result)) "Should have violations")))
    
    (testing "Command with protocol 0"
      (let [result (v/validate-binary (h/invalid-protocol-cmd-bytes) :type :cmd)]
        (is (not (:valid? result)) "Protocol 0 should be invalid")
        (is (seq (:violations result)) "Should have violations")))))

(deftest test-invalid-client-type
  (testing "Command with UNSPECIFIED client type"
    (let [result (v/validate-binary (h/invalid-client-cmd-bytes) :type :cmd)]
      (is (not (:valid? result)) "UNSPECIFIED client should be invalid")
      (is (some #(re-find #"client_type" (str %)) (:violations result))
          "Should flag client_type violation"))))

(deftest test-missing-required-fields
  (testing "State missing required nested messages"
    (let [result (v/validate-binary (h/missing-fields-state-bytes) :type :state)]
      (is (not (:valid? result)) "Missing fields should be invalid")
      (is (seq (:violations result)) "Should have multiple violations")
      (is (some #(re-find #"required" (str %)) (:violations result))
          "Should mention required fields"))))

;; ============================================================================
;; BOUNDARY VALUE TESTING
;; ============================================================================

(deftest test-boundary-values
  (testing "Valid boundary values"
    (let [result (v/validate-binary (h/boundary-state-bytes) :type :state)]
      (is (:valid? result) "Boundary values should be valid")
      (is (empty? (:violations result)) "Should have no violations"))))

;; ============================================================================
;; ERROR HANDLING
;; ============================================================================

(deftest test-empty-binary
  (testing "Empty binary data"
    (is (thrown? Exception
                 (v/validate-binary (byte-array 0)))
        "Empty data should throw")))

(deftest test-garbage-data
  (testing "Random garbage data"
    (let [garbage (byte-array (repeatedly 100 #(rand-int 256)))]
      (is (thrown? Exception
                   (v/validate-binary garbage :type :state))
          "Garbage should throw parse exception"))))

(deftest test-nil-input
  (testing "Nil input handling"
    (is (thrown? Exception (v/validate-binary nil))
        "Nil should throw")))

(deftest test-wrong-type-hint
  (testing "Wrong type hint"
    (let [state-bytes (h/valid-state-bytes)]
      (is (thrown? Exception
                   (v/validate-binary state-bytes :type :cmd))
          "Wrong type hint should cause parse error"))))

;; ============================================================================
;; PERFORMANCE CHARACTERISTICS
;; ============================================================================

(deftest test-validation-performance
  (testing "Validation completes in reasonable time"
    (let [state-bytes (h/valid-state-bytes)
          start (System/nanoTime)
          _ (dotimes [_ 100]
              (v/validate-binary state-bytes :type :state))
          elapsed-ms (/ (- (System/nanoTime) start) 1000000.0)]
      (is (< elapsed-ms 1000) 
          (str "100 validations should complete within 1s, took " elapsed-ms "ms")))))

(comment
  ;; Run tests
  (require '[clojure.test :as t])
  (t/run-tests 'validate.validator-test))