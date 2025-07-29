(ns potatoclient.simple-websocket-test
  "Simple test to verify WebSocket functionality"
  (:require [clojure.test :refer :all]
            [potatoclient.cmd.core :as cmd])
  (:import [potatoclient.test TestWebSocketServer]
           [potatoclient.java.websocket WebSocketManager]))

(deftest test-websocket-manager-creation
  (testing "WebSocketManager can be instantiated"
    (let [manager (WebSocketManager. 
                    "localhost:8080"
                    (reify java.util.function.Consumer
                      (accept [_ msg] (println "Error:" msg)))
                    (reify java.util.function.Consumer
                      (accept [_ data] (println "State received"))))]
      (is (instance? WebSocketManager manager))
      (is (false? (.isConnected manager))))))

(deftest test-websocket-server-creation
  (testing "TestWebSocketServer can be instantiated"
    (let [server (TestWebSocketServer. 8889 8890)]
      (is (instance? TestWebSocketServer server)))))

(deftest test-protobuf-state-creation
  (testing "Can create test protobuf messages"
    (let [state (TestWebSocketServer/createTestState)]
      (is (some? state))
      (is (instance? ser.JonSharedData$JonGUIState state)))))