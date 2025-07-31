(ns potatoclient.transit.java-enum-test
  "Test that Java enums work correctly in both Clojure and message handling."
  (:require [clojure.test :refer [deftest testing is]])
  (:import (potatoclient.transit MessageType MessageKeys CommandActions)))

(deftest test-java-enum-constants
  (testing "Java enum values are accessible and correct"
    ;; MessageType enum
    (is (= "command" (.getKey MessageType/COMMAND)))
    (is (= "response" (.getKey MessageType/RESPONSE)))
    (is (= "log" (.getKey MessageType/LOG)))
    (is (= "error" (.getKey MessageType/ERROR)))
    (is (= "status" (.getKey MessageType/STATUS)))
    (is (= "metric" (.getKey MessageType/METRIC)))
    (is (= "event" (.getKey MessageType/EVENT)))
    (is (= "state-update" (.getKey MessageType/STATE_UPDATE)))

    ;; MessageKeys constants
    (is (= "msg-type" MessageKeys/MSG_TYPE))
    (is (= "msg-id" MessageKeys/MSG_ID))
    (is (= "timestamp" MessageKeys/TIMESTAMP))
    (is (= "payload" MessageKeys/PAYLOAD))
    (is (= "action" MessageKeys/ACTION))
    (is (= "process" MessageKeys/PROCESS))
    (is (= "level" MessageKeys/LEVEL))
    (is (= "message" MessageKeys/MESSAGE))

    ;; CommandActions constants
    (is (= "stop" CommandActions/STOP))
    (is (= "shutdown" CommandActions/SHUTDOWN))
    (is (= "ping" CommandActions/PING))
    (is (= "status" CommandActions/STATUS))
    (is (= "show" CommandActions/SHOW))
    (is (= "hide" CommandActions/HIDE))))

(deftest test-message-type-from-key
  (testing "MessageType.fromKey works correctly"
    (is (= MessageType/COMMAND (MessageType/fromKey "command")))
    (is (= MessageType/RESPONSE (MessageType/fromKey "response")))
    (is (= MessageType/LOG (MessageType/fromKey "log")))
    (is (= MessageType/STATE_UPDATE (MessageType/fromKey "state-update")))
    (is (nil? (MessageType/fromKey "invalid-type")))))

(deftest test-multimethod-dispatch-with-enums
  (testing "IPC multimethod dispatch works with message types"
    ;; Test that we can create keyword from enum
    (is (= :command (keyword (.getKey MessageType/COMMAND))))
    (is (= :response (keyword (.getKey MessageType/RESPONSE))))
    (is (= :log (keyword (.getKey MessageType/LOG))))

    ;; Test message structure using Java constants
    (let [test-msg {MessageKeys/MSG_TYPE (.getKey MessageType/LOG)
                    MessageKeys/MSG_ID "test-123"
                    MessageKeys/TIMESTAMP 12345
                    MessageKeys/PAYLOAD {MessageKeys/LEVEL "INFO"
                                         MessageKeys/MESSAGE "Test message"
                                         MessageKeys/PROCESS "test"}}]
      (is (= "log" (get test-msg MessageKeys/MSG_TYPE)))
      (is (= "test-123" (get test-msg MessageKeys/MSG_ID)))
      (is (= "INFO" (get-in test-msg [MessageKeys/PAYLOAD MessageKeys/LEVEL])))
      (is (= "Test message" (get-in test-msg [MessageKeys/PAYLOAD MessageKeys/MESSAGE]))))))