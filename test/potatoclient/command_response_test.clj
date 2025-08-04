(ns potatoclient.command-response-test
  "Tests for command response handling"
  (:require [clojure.test :refer [deftest testing is use-fixtures]]
            [potatoclient.transit.app-db :as app-db]
            [potatoclient.logging :as logging]))

;; Reset app-db between tests
(use-fixtures :each (fn [f]
                      (app-db/reset-to-initial-state!)
                      (f)))

(deftest test-handle-command-sent-response
  (testing "Command sent response updates stats"
    (let [msg {"msg-type" "response"
               "msg-id" "test-123"
               "timestamp" 1234567890
               "payload" {"status" "sent"
                          "proto-type" "cmd.JonSharedCmd$Root"
                          "size" 42}}]

      ;; Handle the response
      (app-db/handle-command-response msg)

      ;; Verify stats were updated
      (let [stats (app-db/get-validation-state)]
        (is (= 1 (get-in stats [:stats :total-commands-sent])))))))

(deftest test-handle-subprocess-stopped-response
  (testing "Subprocess stopped response updates process state"
    ;; First set the process as running
    (app-db/set-process-state! :cmd-proc 12345 :running)
    (is (app-db/process-running? :cmd-proc))

    (let [msg {"msg-type" "response"
               "msg-id" "test-456"
               "timestamp" 1234567890
               "payload" {"status" "stopped"}}]

      ;; Handle the response
      (app-db/handle-command-response msg)

      ;; Verify process state was updated
      (is (not (app-db/process-running? :cmd-proc)))
      (is (= :stopped (get-in @app-db/app-db [:app-state :processes :cmd-proc :status]))))))

(deftest test-handle-pong-response
  (testing "Pong response calculates latency"
    ;; Store ping timestamp
    (let [ping-time 1234567000
          pong-time 1234567100
          msg-id "ping-789"]

      ;; Simulate sending a ping
      (swap! app-db/app-db assoc-in [:app-state :ping-timestamps msg-id] ping-time)

      (let [msg {"msg-type" "response"
                 "msg-id" msg-id
                 "timestamp" pong-time
                 "payload" {"status" "pong"}}]

        ;; Handle the response
        (app-db/handle-command-response msg)

        ;; Verify latency was calculated (100ms)
        (is (= 100 (get-in @app-db/app-db [:app-state :connection :latency-ms])))
        ;; Verify ping timestamp was cleaned up
        (is (nil? (get-in @app-db/app-db [:app-state :ping-timestamps msg-id])))))))

(deftest test-handle-ack-response
  (testing "Command acknowledgment is logged"
    (let [msg {"msg-type" "response"
               "msg-id" "test-ack"
               "timestamp" 1234567890
               "payload" {"status" "ack"
                          "command" "rotary-goto"
                          "commandId" 42}}]

      ;; Just verify it doesn't throw - logging is the main action
      (is (nil? (app-db/handle-command-response msg))))))

(deftest test-handle-unknown-response-status
  (testing "Unknown response status is handled gracefully"
    (let [msg {"msg-type" "response"
               "msg-id" "test-unknown"
               "timestamp" 1234567890
               "payload" {"status" "unknown-status"
                          "data" "some-data"}}]

      ;; Should not throw
      (is (nil? (app-db/handle-command-response msg))))))

(deftest test-handle-response-without-payload
  (testing "Response without payload doesn't crash"
    (let [msg {"msg-type" "response"
               "msg-id" "test-no-payload"
               "timestamp" 1234567890}]

      ;; Should not throw
      (is (nil? (app-db/handle-command-response msg))))))

(deftest test-multiple-command-sent-responses
  (testing "Multiple command sent responses increment counter"
    ;; Send multiple responses
    (dotimes [i 5]
      (let [msg {"msg-type" "response"
                 "msg-id" (str "test-" i)
                 "timestamp" 1234567890
                 "payload" {"status" "sent"}}]
        (app-db/handle-command-response msg)))

    ;; Verify counter incremented correctly
    (let [stats (app-db/get-validation-state)]
      (is (= 5 (get-in stats [:stats :total-commands-sent]))))))

;; Test fixture
(defn- response-test-fixture [f]
  (logging/log-info "Starting command response tests")
  (f)
  (logging/log-info "Completed command response tests"))

(use-fixtures :once response-test-fixture)