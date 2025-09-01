(ns potatoclient.state-validation-test
  "Tests for state validation and specs."
  (:require [clojure.test :refer [deftest is testing]]
            [potatoclient.state :as state]
            [malli.core :as m]))

(deftest test-initial-state-validity
  (testing "Initial state should be valid"
    (state/reset-state!)
    (is (state/valid-state?) "Initial state should pass validation")
    (is (nil? (state/validate-state)) "validate-state should return nil for valid state")))

(deftest test-invalid-state-detection
  (testing "Invalid state should be detected"
    (state/reset-state!)
    
    ;; Test invalid theme
    (swap! state/app-state assoc-in [:ui :theme] :invalid-theme)
    (is (not (state/valid-state?)) "State with invalid theme should fail validation")
    (let [errors (state/validate-state)]
      (is (some? errors) "validate-state should return errors for invalid state")
      (is (= :invalid-theme (get-in errors [:value :ui :theme])) "Error should show invalid theme value"))
    
    ;; Test invalid boolean
    (state/reset-state!)
    (swap! state/app-state assoc-in [:connection :connected?] "not-a-boolean")
    (is (not (state/valid-state?)) "State with non-boolean connected? should fail validation")
    
    ;; Test invalid number range
    (state/reset-state!)
    (swap! state/app-state assoc-in [:server-state :system :battery-level] 150)
    (is (not (state/valid-state?)) "Battery level > 100 should fail validation")
    
    ;; Test invalid enum value
    (state/reset-state!)
    (swap! state/app-state assoc-in [:processes :state-proc :status] :invalid-status)
    (is (not (state/valid-state?)) "Invalid process status should fail validation")))

(deftest test-partial-validation
  (testing "Partial state validation"
    (state/ensure-specs-registered!)
    
    ;; Test valid partial state
    (let [valid-connection {:url "example.com"
                           :connected? true
                           :latency-ms 50
                           :reconnect-count 3}]
      (is (nil? (state/validate-partial :potatoclient.state/connection-state valid-connection))
          "Valid connection state should pass"))
    
    ;; Test invalid partial state
    (let [invalid-connection {:url "example.com"
                             :connected? "not-boolean"
                             :latency-ms -1
                             :reconnect-count 3}]
      (is (some? (state/validate-partial :potatoclient.state/connection-state invalid-connection))
          "Invalid connection state should fail"))))

(deftest test-safe-swap
  (testing "safe-swap! in development mode"
    (state/reset-state!)
    
    ;; Test valid swap
    (let [result (state/safe-swap! assoc-in [:ui :theme] :sol-light)]
      (is (= :sol-light (get-in result [:ui :theme])) "Valid swap should succeed")
      (is (state/valid-state?) "State should remain valid after valid swap"))
    
    ;; Test invalid swap (should print warning but not throw in dev)
    (let [output (with-out-str
                   (state/safe-swap! assoc-in [:ui :theme] :invalid-theme))]
      (when-not (potatoclient.runtime/release-build?)
        (is (re-find #"WARNING" output) "Should print warning for invalid state in dev"))
      (is (= :invalid-theme (get-in @state/app-state [:ui :theme]))
          "Invalid swap should still complete"))))

(deftest test-safe-reset
  (testing "safe-reset! validation"
    (state/reset-state!)
    
    ;; Test valid reset
    (let [valid-state (assoc state/initial-state :connection 
                            {:url "test.com"
                             :connected? true
                             :latency-ms 100
                             :reconnect-count 5})]
      (state/safe-reset! valid-state)
      (is (= "test.com" (state/get-connection-url)) "Valid reset should succeed"))
    
    ;; Test invalid reset (should throw in dev)
    (when-not (potatoclient.runtime/release-build?)
      (let [invalid-state (assoc-in state/initial-state [:ui :theme] :invalid)]
        (is (thrown-with-msg? clojure.lang.ExceptionInfo #"Cannot reset to invalid state"
              (state/safe-reset! invalid-state))
            "Invalid reset should throw in development")))))

(deftest test-state-specs-registration
  (testing "State specs are properly registered"
    (state/ensure-specs-registered!)
    (let [registry (potatoclient.malli.registry/get-registry)]
      (is (contains? registry :potatoclient.state/app-state) "App state spec should be registered")
      (is (contains? registry :potatoclient.state/connection-state) "Connection state spec should be registered")
      (is (contains? registry :potatoclient.state/ui-state) "UI state spec should be registered")
      (is (contains? registry :potatoclient.state/process-info) "Process info spec should be registered"))))

(deftest test-state-spec-structure
  (testing "State specs have correct structure"
    (state/ensure-specs-registered!)
    
    ;; Test that specs are valid Malli schemas
    (is (m/schema state/app-state-spec) "app-state-spec should be a valid schema")
    (is (m/schema state/connection-state) "connection-state should be a valid schema")
    (is (m/schema state/ui-state) "ui-state should be a valid schema")
    
    ;; Test closed maps
    (let [extra-key-state (assoc state/initial-state :unexpected-key "value")]
      (is (not (m/validate state/app-state-spec extra-key-state))
          "Closed maps should reject extra keys"))))

(deftest test-validate-state-exception
  (testing "validate-state! throws on invalid state"
    (state/reset-state!)
    
    ;; Should not throw for valid state
    (is (nil? (state/validate-state!)) "validate-state! should return nil for valid state")
    
    ;; Should throw for invalid state
    (swap! state/app-state assoc-in [:ui :theme] :invalid)
    (is (thrown-with-msg? clojure.lang.ExceptionInfo #"Invalid app state"
          (state/validate-state!))
        "validate-state! should throw for invalid state")))

(deftest test-status-field-in-ui-state
  (testing "Status field is properly defined in UI state"
    (state/reset-state!)
    
    ;; Test valid status
    (swap! state/app-state assoc-in [:ui :status] {:message "Test message" :type :info})
    (is (state/valid-state?) "Valid status should pass validation")
    
    (swap! state/app-state assoc-in [:ui :status] {:message "Warning!" :type :warning})
    (is (state/valid-state?) "Warning status should pass validation")
    
    (swap! state/app-state assoc-in [:ui :status] {:message "Error!" :type :error})
    (is (state/valid-state?) "Error status should pass validation")
    
    ;; Test invalid status type
    (swap! state/app-state assoc-in [:ui :status] {:message "Test" :type :invalid-type})
    (is (not (state/valid-state?)) "Invalid status type should fail validation")))