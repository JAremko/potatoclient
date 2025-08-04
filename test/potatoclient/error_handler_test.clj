(ns potatoclient.error-handler-test
  "Tests for Transit error message handling"
  (:require [clojure.test :refer [deftest testing is use-fixtures]]
            [potatoclient.transit.error-handler :as error-handler]
            [potatoclient.transit.app-db :as app-db]
            [potatoclient.logging :as logging]))

;; Reset app-db between tests
(use-fixtures :each (fn [f]
                      (app-db/reset-to-initial-state!)
                      (f)))

(deftest test-handle-websocket-error
  (testing "WebSocket error updates connection state"
    ;; First set connection as active
    (app-db/set-connection-state! true "wss://example.com" 50)
    (is (app-db/connected?))

    (let [msg {"msg-type" "error"
               "msg-id" "error-123"
               "timestamp" 1234567890
               "payload" {"type" "websocket-error"
                          "message" "Connection refused"
                          "process" "command"}}]

      ;; Handle the error
      (error-handler/handle-subprocess-error msg)

      ;; Verify connection state was updated
      (is (not (app-db/connected?))))))

(deftest test-handle-command-error
  (testing "Command building error adds validation error"
    (let [msg {"msg-type" "error"
               "msg-id" "error-456"
               "timestamp" 1234567890
               "payload" {"type" "command-error"
                          "message" "Invalid command structure"
                          "stackTrace" "at line 42..."}}]

      ;; Handle the error
      (error-handler/handle-subprocess-error msg)

      ;; Verify validation error was added
      (let [errors (get-in @app-db/app-db [:validation :errors])]
        (is (= 1 (count errors)))
        (is (= "Invalid command structure" (-> errors first :errors first :error)))))))

(deftest test-handle-state-parse-error
  (testing "State parse error increments counter"
    (let [msg {"msg-type" "error"
               "msg-id" "error-789"
               "timestamp" 1234567890
               "payload" {"type" "state-parse-error"
                          "message" "Malformed protobuf"}}]

      ;; Handle multiple errors
      (dotimes [_ 3]
        (error-handler/handle-subprocess-error msg))

      ;; Verify counter was incremented
      (is (= 3 (get-in @app-db/app-db [:validation :stats :state-parse-errors]))))))

(deftest test-handle-subprocess-fatal-error
  (testing "Fatal subprocess error logs restart warning"
    (let [msg {"msg-type" "error"
               "msg-id" "error-fatal"
               "timestamp" 1234567890
               "payload" {"type" "subprocess-error"
                          "message" "FATAL: Out of memory"
                          "process" "state"
                          "stackTrace" "java.lang.OutOfMemoryError..."}}]

      ;; Just verify it doesn't throw
      (is (nil? (error-handler/handle-subprocess-error msg))))))

(deftest test-handle-unknown-error-type
  (testing "Unknown error type is handled gracefully"
    (let [msg {"msg-type" "error"
               "msg-id" "error-unknown"
               "timestamp" 1234567890
               "payload" {"type" "unknown-error-type"
                          "message" "Something went wrong"}}]

      ;; Should not throw
      (is (nil? (error-handler/handle-subprocess-error msg))))))

(deftest test-handle-validation-errors
  (testing "Validation errors are properly stored"
    (let [msg {"msg-type" "validation-error"
               "msg-id" "val-error-123"
               "timestamp" 1234567890
               "payload" {"source" "buf-validate"
                          "subsystem" "rotary"
                          "errors" [{"field" "azimuth"
                                     "message" "Value must be between 0 and 360"}
                                    {"field" "speed"
                                     "message" "Speed cannot be negative"}]}}]

      ;; Handle the validation error
      (error-handler/handle-validation-error msg)

      ;; Verify errors were stored
      (let [errors (get-in @app-db/app-db [:validation :errors])]
        (is (= 1 (count errors)))
        (let [error-entry (first errors)]
          (is (= :buf-validate (:source error-entry)))
          (is (= :rotary (:subsystem error-entry)))
          (is (= 2 (count (:errors error-entry)))))))))

(deftest test-error-message-routing
  (testing "Error messages are routed to error handler"
    (let [error-msg {"msg-type" "error"
                     "msg-id" "test-routing"
                     "timestamp" 1234567890
                     "payload" {"type" "transit-error"
                                "message" "Transit decode failed"
                                "process" "command"}}]

      ;; Main entry point
      (is (nil? (error-handler/handle-error-message error-msg))))))

;; Test fixture
(defn- error-test-fixture [f]
  (logging/log-info "Starting error handler tests")
  (f)
  (logging/log-info "Completed error handler tests"))

(use-fixtures :once error-test-fixture)