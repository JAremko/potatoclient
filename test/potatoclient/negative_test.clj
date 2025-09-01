(ns potatoclient.negative-test
  "Negative tests and sanity checks for edge cases and invalid inputs."
  (:require [clojure.test :refer [deftest is testing]]
            [potatoclient.state :as state]
            [potatoclient.ui.status-bar :as status-bar]
            [potatoclient.ui.status-bar.helpers :as helpers]
            [potatoclient.i18n :as i18n]))

;; ============================================================================
;; State Negative Tests
;; ============================================================================

(deftest test-invalid-state-modifications
  (testing "Invalid state modifications are caught"
    (state/reset-state!)

    ;; Test nil values where not allowed
    (swap! state/app-state assoc-in [:connection :connected?] nil)
    (is (not (state/valid-state?)) "nil boolean should fail validation")

    ;; Test wrong type for numeric fields
    (state/reset-state!)
    (swap! state/app-state assoc-in [:server-state :system :battery-level] "not-a-number")
    (is (not (state/valid-state?)) "String instead of number should fail")

    ;; Test negative values where not allowed
    (state/reset-state!)
    (swap! state/app-state assoc-in [:server-state :system :battery-level] -10)
    (is (not (state/valid-state?)) "Negative battery level should fail")

    ;; Test values exceeding max range
    (state/reset-state!)
    (swap! state/app-state assoc-in [:server-state :camera-day :brightness] 101)
    (is (not (state/valid-state?)) "Brightness > 100 should fail")

    ;; Test empty string where string expected
    (state/reset-state!)
    (swap! state/app-state assoc-in [:connection :url] nil)
    (is (not (state/valid-state?)) "nil URL should fail validation")))

(deftest test-extra-keys-rejected
  (testing "Closed maps reject extra keys"
    (state/reset-state!)

    ;; Add unexpected key to connection
    (swap! state/app-state update :connection assoc :unexpected-key "value")
    (is (not (state/valid-state?)) "Extra key in connection should fail")

    ;; Add unexpected key to UI state
    (state/reset-state!)
    (swap! state/app-state update :ui assoc :random-field 123)
    (is (not (state/valid-state?)) "Extra key in UI should fail")

    ;; Add unexpected key at root level
    (state/reset-state!)
    (swap! state/app-state assoc :new-root-key {})
    (is (not (state/valid-state?)) "Extra root key should fail")))

(deftest test-invalid-enum-values
  (testing "Invalid enum values are rejected"
    (state/reset-state!)

    ;; Invalid theme
    (swap! state/app-state assoc-in [:ui :theme] :nonexistent-theme)
    (is (not (state/valid-state?)) "Invalid theme should fail")

    ;; Invalid locale
    (state/reset-state!)
    (swap! state/app-state assoc-in [:ui :locale] :french)
    (is (not (state/valid-state?)) "Unsupported locale should fail")

    ;; Invalid process status
    (state/reset-state!)
    (swap! state/app-state assoc-in [:processes :state-proc :status] :crashed)
    (is (not (state/valid-state?)) "Invalid process status should fail")

    ;; Invalid camera mode
    (state/reset-state!)
    (swap! state/app-state assoc-in [:server-state :camera-day :focus-mode] :ai-enhanced)
    (is (not (state/valid-state?)) "Invalid focus mode should fail")))

;; ============================================================================
;; Status Bar Negative Tests
;; ============================================================================

(deftest test-status-bar-invalid-types
  (testing "Status bar handles invalid status types gracefully"
    (state/reset-state!)

    ;; Direct invalid type should fail validation
    (swap! state/app-state assoc-in [:ui :status :type] :critical)
    (is (not (state/valid-state?)) "Invalid status type should fail validation")

    ;; But the helper functions should only accept valid types
    (state/reset-state!)
    (status-bar/set-status! "Test" :info)
    (is (= :info (get-in @state/app-state [:ui :status :type])))

    (status-bar/set-status! "Test" :warning)
    (is (= :warning (get-in @state/app-state [:ui :status :type])))

    (status-bar/set-status! "Test" :error)
    (is (= :error (get-in @state/app-state [:ui :status :type])))))

(deftest test-status-bar-nil-handling
  (testing "Status bar handles nil values"
    (state/reset-state!)

    ;; Setting nil message should work (empty string)
    (status-bar/set-info! nil)
    (is (= "" (get-in @state/app-state [:ui :status :message]))
        "nil message should become empty string")

    ;; Clear should reset properly
    (status-bar/set-error! "Error")
    (status-bar/clear!)
    (is (= "" (get-in @state/app-state [:ui :status :message])))
    (is (= :info (get-in @state/app-state [:ui :status :type])))))

(deftest test-error-handler-nil-exception
  (testing "Error handler with nil or empty exceptions"
    (state/reset-state!)
    (reset! helpers/last-error-atom nil)

    ;; Exception with empty message
    (try
      (status-bar/with-error-handler
        (fn [] (throw (Exception. ""))))
      (catch Exception e
        (is (= "" (.getMessage e)) "Exception message should be empty")))

    (is (some? @helpers/last-error-atom) "Error should still be stored")
    (is (= "" (:message @helpers/last-error-atom)) "Stored message should be empty")
    (is (string? (:stack-trace @helpers/last-error-atom)) "Stack trace should exist")))

;; ============================================================================
;; I18n Negative Tests
;; ============================================================================

(deftest test-missing-translation-keys
  (testing "Missing translation keys are handled"
    (i18n/load-translations!)

    ;; Non-existent key should return placeholder
    (let [result (i18n/tr :nonexistent-key)]
      (is (string? result) "Should return a string")
      (is (re-find #"MISSING" result) "Should indicate missing key"))

    ;; With arguments
    (let [result (i18n/tr :another-missing-key ["arg1" "arg2"])]
      (is (string? result) "Should return a string with args")
      (is (re-find #"MISSING" result) "Should indicate missing key"))))

;; ============================================================================
;; Boundary and Edge Case Tests
;; ============================================================================

(deftest test-numeric-boundaries
  (testing "Numeric field boundaries"
    (state/reset-state!)

    ;; Test exact boundaries for battery level
    (swap! state/app-state assoc-in [:server-state :system :battery-level] 0)
    (is (state/valid-state?) "Battery level 0 should be valid")

    (swap! state/app-state assoc-in [:server-state :system :battery-level] 100)
    (is (state/valid-state?) "Battery level 100 should be valid")

    (swap! state/app-state assoc-in [:server-state :system :battery-level] -1)
    (is (not (state/valid-state?)) "Battery level -1 should fail")

    (swap! state/app-state assoc-in [:server-state :system :battery-level] 101)
    (is (not (state/valid-state?)) "Battery level 101 should fail")

    ;; Test brightness boundaries
    (state/reset-state!)
    (swap! state/app-state assoc-in [:server-state :camera-day :brightness] 0)
    (is (state/valid-state?) "Brightness 0 should be valid")

    (swap! state/app-state assoc-in [:server-state :camera-day :brightness] 100)
    (is (state/valid-state?) "Brightness 100 should be valid")))

(deftest test-large-values
  (testing "Very large values"
    (state/reset-state!)

    ;; Large reconnect count should be fine
    (swap! state/app-state assoc-in [:connection :reconnect-count] 999999)
    (is (state/valid-state?) "Large reconnect count should be valid")

    ;; Large latency should be fine
    (swap! state/app-state assoc-in [:connection :latency-ms] 999999)
    (is (state/valid-state?) "Large latency should be valid")

    ;; But negative latency should fail
    (swap! state/app-state assoc-in [:connection :latency-ms] -1)
    (is (not (state/valid-state?)) "Negative latency should fail")))

(deftest test-empty-collections
  (testing "Empty collections and maps"
    (state/reset-state!)

    ;; Empty stream-processes map should be valid
    (swap! state/app-state assoc :stream-processes {})
    (is (state/valid-state?) "Empty stream-processes should be valid")

    ;; But nil should not be
    (swap! state/app-state assoc :stream-processes nil)
    (is (not (state/valid-state?)) "nil stream-processes should fail")))

;; ============================================================================
;; Concurrency and Thread Safety Tests
;; ============================================================================

(deftest test-concurrent-state-updates
  (testing "Concurrent state updates"
    (state/reset-state!)

    ;; Launch multiple threads updating different parts of state
    (let [futures (doall
                    (for [i (range 10)]
                      (future
                        (state/set-connection-url! (str "url-" i))
                        (state/set-theme! (if (even? i) :sol-dark :sol-light))
                        (state/set-locale! (if (even? i) :english :ukrainian)))))]

      ;; Wait for all to complete
      (doseq [f futures] @f)

      ;; State should still be valid
      (is (state/valid-state?) "State should remain valid after concurrent updates"))))

(deftest test-rapid-status-updates
  (testing "Rapid status bar updates"
    (state/reset-state!)

    ;; Rapidly update status
    (dotimes [i 100]
      (case (mod i 3)
        0 (status-bar/set-info! (str "Info " i))
        1 (status-bar/set-warning! (str "Warning " i))
        2 (status-bar/set-error! (str "Error " i))))

    ;; Should end with last update
    (is (string? (get-in @state/app-state [:ui :status :message]))
        "Should have a status message")
    (is (state/valid-state?) "State should remain valid")))

;; ============================================================================
;; Sanity Checks
;; ============================================================================

(deftest test-sanity-initial-state
  (testing "Sanity check: initial state is sane"
    (state/reset-state!)

    ;; Check initial values make sense
    (is (= false (state/connected?)) "Should not be connected initially")
    (is (= 0 (state/get-reconnect-count)) "Reconnect count should be 0")
    (is (= "" (state/get-connection-url)) "URL should be empty")
    (is (keyword? (state/get-theme)) "Theme should be a keyword")
    (is (keyword? (state/get-locale)) "Locale should be a keyword")
    (is (map? @state/app-state) "App state should be a map")
    (is (state/valid-state?) "Initial state must be valid")))

(deftest test-sanity-state-persistence
  (testing "Sanity check: state changes persist"
    (state/reset-state!)

    ;; Make changes
    (state/set-connection-url! "test.com")
    (state/set-theme! :sol-light)
    (state/set-connected! true)

    ;; Verify they persist
    (is (= "test.com" (state/get-connection-url)))
    (is (= :sol-light (state/get-theme)))
    (is (true? (state/connected?)))

    ;; Reset and verify clean
    (state/reset-state!)
    (is (= "" (state/get-connection-url)))
    (is (not (state/connected?)))))

(deftest test-sanity-error-recovery
  (testing "Sanity check: system recovers from errors"
    (state/reset-state!)

    ;; Cause an error
    (try
      (status-bar/with-error-handler
        (fn [] (throw (Exception. "Test error"))))
      (catch Exception _))

    ;; System should still be functional
    (is (some? @helpers/last-error-atom) "Error should be captured")

    ;; Can still update status
    (status-bar/set-info! "Recovered")
    (is (= "Recovered" (get-in @state/app-state [:ui :status :message])))

    ;; Can clear error
    (reset! helpers/last-error-atom nil)
    (is (nil? @helpers/last-error-atom))))