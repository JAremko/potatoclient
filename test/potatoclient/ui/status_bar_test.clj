(ns potatoclient.ui.status-bar-test
  "Tests for status bar functionality."
  (:require [clojure.test :refer [deftest is testing]]
            [potatoclient.state :as state]
            [potatoclient.ui.status-bar.messages :as status-bar]
            [potatoclient.ui.status-bar.helpers :as helpers]
            [potatoclient.i18n :as i18n]))

(deftest test-status-updates
  (testing "Basic status update functions"
    (state/reset-state!)

    ;; Test info status
    (status-bar/set-info! "Test info message")
    (is (= "Test info message" (get-in @state/app-state [:ui :status :message]))
        "Info message should be set")
    (is (= :info (get-in @state/app-state [:ui :status :type]))
        "Info type should be set")

    ;; Test warning status
    (status-bar/set-warning! "Test warning")
    (is (= "Test warning" (get-in @state/app-state [:ui :status :message]))
        "Warning message should be set")
    (is (= :warning (get-in @state/app-state [:ui :status :type]))
        "Warning type should be set")

    ;; Test error status
    (status-bar/set-error! "Test error")
    (is (= "Test error" (get-in @state/app-state [:ui :status :message]))
        "Error message should be set")
    (is (= :error (get-in @state/app-state [:ui :status :type]))
        "Error type should be set")

    ;; Test clear
    (status-bar/clear!)
    (is (= "" (get-in @state/app-state [:ui :status :message]))
        "Clear should empty message")
    (is (= :info (get-in @state/app-state [:ui :status :type]))
        "Clear should reset type to info")))

(deftest test-action-status-helpers
  (testing "Action-specific status helpers"
    (state/reset-state!)
    (i18n/load-translations!) ; Ensure translations are loaded

    ;; Test theme change
    (status-bar/set-theme-changed! :sol-light)
    (let [message (get-in @state/app-state [:ui :status :message])]
      (is (string? message) "Theme change should set a message")
      (is (re-find #"Sol Light" message) "Message should contain theme name"))

    ;; Test language change
    (status-bar/set-language-changed! :english)
    (let [message (get-in @state/app-state [:ui :status :message])]
      (is (string? message) "Language change should set a message")
      (is (re-find #"English" message) "Message should contain language name"))

    ;; Test connection statuses
    (status-bar/set-connecting! "example.com")
    (is (re-find #"example\.com" (get-in @state/app-state [:ui :status :message]))
        "Connecting message should contain server")

    (status-bar/set-connected! "example.com")
    (is (re-find #"example\.com" (get-in @state/app-state [:ui :status :message]))
        "Connected message should contain server")

    (status-bar/set-disconnected! "example.com")
    (is (re-find #"example\.com" (get-in @state/app-state [:ui :status :message]))
        "Disconnected message should contain server")
    (is (= :warning (get-in @state/app-state [:ui :status :type]))
        "Disconnected should be a warning")

    (status-bar/set-connection-failed! "timeout")
    (is (re-find #"timeout" (get-in @state/app-state [:ui :status :message]))
        "Connection failed should contain error")
    (is (= :error (get-in @state/app-state [:ui :status :type]))
        "Connection failed should be an error")))

(deftest test-stream-status-helpers
  (testing "Stream status helpers"
    (state/reset-state!)
    (i18n/load-translations!)

    ;; Test stream started
    (status-bar/set-stream-started! :heat)
    (let [message (get-in @state/app-state [:ui :status :message])]
      (is (string? message) "Stream start should set a message")
      (is (or (re-find #"Thermal" message)
              (re-find #"heat" message))
          "Message should contain stream type"))

    ;; Test stream stopped
    (status-bar/set-stream-stopped! :day)
    (let [message (get-in @state/app-state [:ui :status :message])]
      (is (string? message) "Stream stop should set a message")
      (is (or (re-find #"Day" message)
              (re-find #"day" message))
          "Message should contain stream type"))))

(deftest test-config-and-log-status
  (testing "Config and log status helpers"
    (state/reset-state!)
    (i18n/load-translations!)

    ;; Test config saved
    (status-bar/set-config-saved!)
    (is (string? (get-in @state/app-state [:ui :status :message]))
        "Config saved should set a message")

    ;; Test logs exported
    (status-bar/set-logs-exported! "/path/to/logs.txt")
    (let [message (get-in @state/app-state [:ui :status :message])]
      (is (string? message) "Logs exported should set a message")
      (is (re-find #"/path/to/logs\.txt" message)
          "Message should contain log path"))

    ;; Test ready status
    (status-bar/set-ready!)
    (is (string? (get-in @state/app-state [:ui :status :message]))
        "Ready should set a message")))

(deftest test-error-handling
  (testing "Error handling functionality"
    (state/reset-state!)

    ;; Test error handler
    (let [test-fn (fn [] (throw (Exception. "Test error")))]
      (is (thrown? Exception
                   (status-bar/with-error-handler test-fn))
          "with-error-handler should re-throw exception")

      ;; Check that error was recorded
      (is (= :error (get-in @state/app-state [:ui :status :type]))
          "Error status should be set")
      (is (some? @helpers/last-error-atom)
          "Last error should be stored")
      (is (= "Test error" (:message @helpers/last-error-atom))
          "Error message should be stored")
      (is (string? (:stack-trace @helpers/last-error-atom))
          "Stack trace should be stored"))

    ;; Test successful execution
    (reset! helpers/last-error-atom nil)
    (let [result (status-bar/with-error-handler (fn [] "success"))]
      (is (= "success" result) "Should return function result on success")
      (is (nil? @helpers/last-error-atom) "No error should be stored on success"))))

(deftest test-with-status-macro
  (testing "with-status macro"
    (state/reset-state!)
    (i18n/load-translations!)

    ;; Test successful execution
    (let [result (status-bar/with-status "Processing..."
                   (Thread/sleep 10)
                   "done")]
      (is (= "done" result) "Should return body result"))

    ;; After completion, should show ready
    (is (string? (get-in @state/app-state [:ui :status :message]))
        "Should have a status message")

    ;; Test with exception (should still set ready in finally)
    (try
      (status-bar/with-status "Processing with error..."
        (throw (Exception. "Test")))
      (catch Exception _))

    ;; Even after exception, finally should set ready
    (Thread/sleep 50) ; Give finally time to execute
    (is (string? (get-in @state/app-state [:ui :status :message]))
        "Should have status after exception")))

(deftest test-status-type-validation
  (testing "Status type must be valid"
    (state/reset-state!)

    ;; Valid types should work
    (status-bar/set-status! "Info" :info)
    (is (state/valid-state?) "Info status should be valid")

    (status-bar/set-status! "Warning" :warning)
    (is (state/valid-state?) "Warning status should be valid")

    (status-bar/set-status! "Error" :error)
    (is (state/valid-state?) "Error status should be valid")

    ;; Invalid type should make state invalid
    (swap! state/app-state assoc-in [:ui :status :type] :invalid-type)
    (is (not (state/valid-state?)) "Invalid status type should fail validation")))

(deftest test-status-message-localization
  (testing "Status messages use i18n"
    (state/reset-state!)
    (i18n/load-translations!)

    ;; Set to English
    (state/set-locale! :english)
    (status-bar/set-ready!)
    (let [en-message (get-in @state/app-state [:ui :status :message])]
      (is (= "Ready" en-message) "English ready message"))

    ;; Set to Ukrainian
    (state/set-locale! :ukrainian)
    (status-bar/set-ready!)
    (let [uk-message (get-in @state/app-state [:ui :status :message])]
      (is (= "Готово" uk-message) "Ukrainian ready message"))

    ;; Reset to English
    (state/set-locale! :english)))