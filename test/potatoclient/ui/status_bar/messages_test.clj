(ns potatoclient.ui.status-bar.messages-test
  "Tests for status bar message functions."
  (:require [clojure.test :refer [deftest is testing]]
            [potatoclient.state :as state]
            [potatoclient.ui.status-bar.messages :as msg]
            [potatoclient.ui.status-bar.helpers :as helpers]))

(deftest test-status-updates
  (testing "Status update functions"
    ;; Reset state before tests - use valid initial state
    (reset! state/app-state state/initial-state)

    (testing "set-status!"
      (msg/set-status! "Test message" :info)
      (is (= {:message "Test message" :type :info}
             (get-in @state/app-state [:ui :status]))
          "Should set status with message and type")

      (msg/set-status! nil :warning)
      (is (= {:message "" :type :warning}
             (get-in @state/app-state [:ui :status]))
          "Should handle nil message"))

    (testing "set-info!"
      (msg/set-info! "Info message")
      (is (= {:message "Info message" :type :info}
             (get-in @state/app-state [:ui :status]))
          "Should set info status"))

    (testing "set-warning!"
      (msg/set-warning! "Warning message")
      (is (= {:message "Warning message" :type :warning}
             (get-in @state/app-state [:ui :status]))
          "Should set warning status"))

    (testing "set-error!"
      (msg/set-error! "Error message")
      (is (= {:message "Error message" :type :error}
             (get-in @state/app-state [:ui :status]))
          "Should set error status"))

    (testing "clear!"
      (msg/set-info! "Some message")
      (msg/clear!)
      (is (= {:message "" :type :info}
             (get-in @state/app-state [:ui :status]))
          "Should clear status message"))))

(deftest test-action-status-helpers
  (testing "Action-specific status helpers"
    ;; Reset state before tests
    (reset! state/app-state state/initial-state)

    (testing "set-theme-changed!"
      (msg/set-theme-changed! :dark)
      (let [status (get-in @state/app-state [:ui :status])]
        (is (= :info (:type status))
            "Should set info type")
        (is (string? (:message status))
            "Should set string message")))

    (testing "set-language-changed!"
      (msg/set-language-changed! :english)
      (let [status (get-in @state/app-state [:ui :status])]
        (is (= :info (:type status))
            "Should set info type")
        (is (string? (:message status))
            "Should set string message")))

    (testing "set-connecting!"
      (msg/set-connecting! "localhost:8080")
      (let [status (get-in @state/app-state [:ui :status])]
        (is (= :info (:type status))
            "Should set info type")
        (is (string? (:message status))
            "Should include server info")))

    (testing "set-connected!"
      (msg/set-connected! "localhost:8080")
      (let [status (get-in @state/app-state [:ui :status])]
        (is (= :info (:type status))
            "Should set info type")))

    (testing "set-disconnected!"
      (msg/set-disconnected! "localhost:8080")
      (let [status (get-in @state/app-state [:ui :status])]
        (is (= :warning (:type status))
            "Should set warning type")))

    (testing "set-connection-failed!"
      (msg/set-connection-failed! "Connection refused")
      (let [status (get-in @state/app-state [:ui :status])]
        (is (= :error (:type status))
            "Should set error type")))))

(deftest test-stream-status-helpers
  (testing "Stream status helpers"
    ;; Reset state before tests
    (reset! state/app-state state/initial-state)

    (testing "set-stream-started!"
      (msg/set-stream-started! :heat)
      (let [status (get-in @state/app-state [:ui :status])]
        (is (= :info (:type status))
            "Should set info type for heat stream"))

      (msg/set-stream-started! :day)
      (let [status (get-in @state/app-state [:ui :status])]
        (is (= :info (:type status))
            "Should set info type for day stream")))

    (testing "set-stream-stopped!"
      (msg/set-stream-stopped! :heat)
      (let [status (get-in @state/app-state [:ui :status])]
        (is (= :info (:type status))
            "Should set info type")))))

(deftest test-config-and-log-status
  (testing "Config and log status helpers"
    ;; Reset state before tests
    (reset! state/app-state state/initial-state)

    (testing "set-config-saved!"
      (msg/set-config-saved!)
      (let [status (get-in @state/app-state [:ui :status])]
        (is (= :info (:type status))
            "Should set info type")))

    (testing "set-logs-exported!"
      (msg/set-logs-exported! "/path/to/logs.txt")
      (let [status (get-in @state/app-state [:ui :status])]
        (is (= :info (:type status))
            "Should set info type")))

    (testing "set-ready!"
      (msg/set-ready!)
      (let [status (get-in @state/app-state [:ui :status])]
        (is (= :info (:type status))
            "Should set info type")))))

(deftest test-error-handling
  (testing "Error handler"
    ;; Reset state and error atom
    (reset! state/app-state state/initial-state)
    (reset! helpers/last-error-atom nil)

    (testing "with-error-handler success"
      (let [result (msg/with-error-handler #(+ 1 2))]
        (is (= 3 result)
            "Should return function result on success")
        (is (nil? @helpers/last-error-atom)
            "Should not store error on success")))

    (testing "with-error-handler failure"
      (is (thrown? Exception
                   (msg/with-error-handler
                     #(throw (Exception. "Test error"))))
          "Should re-throw exception")

      (is (some? @helpers/last-error-atom)
          "Should store error information")

      (is (= "Test error" (:message @helpers/last-error-atom))
          "Should store error message")

      (is (string? (:stack-trace @helpers/last-error-atom))
          "Should store stack trace")

      (is (number? (:timestamp @helpers/last-error-atom))
          "Should store timestamp")

      (let [status (get-in @state/app-state [:ui :status])]
        (is (= :error (:type status))
            "Should set error status")))))

(deftest test-with-status-macro
  (testing "with-status macro"
    ;; Reset state
    (reset! state/app-state state/initial-state)

    (testing "successful execution"
      (msg/with-status "Processing..."
        (Thread/sleep 10)
        :done)

      ;; After completion, should be set to ready
      (let [status (get-in @state/app-state [:ui :status])]
        (is (string? (:message status))
            "Should have a message after completion")))

    (testing "execution with exception"
      (reset! state/app-state state/initial-state)

      (is (thrown? Exception
                   (msg/with-status "Processing..."
                     (throw (Exception. "Test error"))))
          "Should propagate exception")

      ;; Even with exception, finally block should run
      (let [status (get-in @state/app-state [:ui :status])]
        (is (some? status)
            "Should have status after exception")))))