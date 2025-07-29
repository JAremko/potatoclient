(ns potatoclient.websocket-basic-test
  "Basic integration tests for WebSocket functionality"
  (:require [clojure.test :refer [deftest is testing]]
            [potatoclient.cmd.core :as cmd]
            [potatoclient.cmd.rotary :as rotary]
            [potatoclient.cmd.day-camera :as day-camera]
            [potatoclient.state.device :as device]
            [potatoclient.state.dispatch :as dispatch]
            [potatoclient.logging :as logging])
  (:import [potatoclient.test TestWebSocketServer]
           [java.util.concurrent TimeUnit]))

(def test-cmd-port 8891)
(def test-state-port 8892)

(deftest test-basic-command-flow
  (testing "Basic command sending works"
    (let [server (TestWebSocketServer. test-cmd-port test-state-port)
          error-count (atom 0)]
      
      (try
        ;; Start test server
        (.start server)
        (Thread/sleep 200)
        
        ;; Initialize WebSocket client
        (cmd/init-websocket!
          (str "localhost:" test-cmd-port)
          (fn [error] 
            (swap! error-count inc)
            (logging/log-error (str "Test error: " error)))
          (fn [data] 
            ;; For now, just log that we received data
            (logging/log-debug "State data received")))
        
        ;; Wait for connection
        (is (true? (.awaitCmdConnection server 2 TimeUnit/SECONDS))
            "Command connection should be established")
        
        ;; Send a simple command
        (cmd/send-cmd-ping)
        (Thread/sleep 100)
        
        ;; Check server received it
        (let [received (.pollCommand server 1 TimeUnit/SECONDS)]
          (is (some? received) "Should receive ping command")
          (is (true? (.hasPing received)) "Should be a ping command"))
        
        ;; Check no errors
        (is (= 0 @error-count) "Should have no errors")
        
        (finally
          (cmd/stop-websocket!)
          (.stop server))))))

(deftest test-basic-state-flow
  (testing "Basic state receiving works"
    (let [server (TestWebSocketServer. test-cmd-port test-state-port)
          error-count (atom 0)
          states-received (atom 0)]
      
      (try
        ;; Start test server
        (.start server)
        (Thread/sleep 200)
        
        ;; Initialize WebSocket client
        (cmd/init-websocket!
          (str "localhost:" test-cmd-port)
          (fn [error] 
            (swap! error-count inc))
          (fn [data] 
            (swap! states-received inc)
            ;; In real implementation, this would be:
            ;; (dispatch/handle-binary-state data)
            ))
        
        ;; Wait for state connection
        (is (true? (.awaitStateConnection server 2 TimeUnit/SECONDS))
            "State connection should be established")
        
        ;; Send a test state
        (.sendState server (TestWebSocketServer/createTestState))
        
        ;; Wait a bit for processing
        (Thread/sleep 500)
        
        ;; Check we received something
        (is (> @states-received 0) "Should have received at least one state")
        (is (= 0 @error-count) "Should have no errors")
        
        (finally
          (cmd/stop-websocket!)
          (.stop server))))))

(deftest test-multiple-commands
  (testing "Can send multiple command types"
    (let [server (TestWebSocketServer. test-cmd-port test-state-port)
          error-count (atom 0)]
      
      (try
        ;; Start test server
        (.start server)
        (Thread/sleep 200)
        
        ;; Initialize WebSocket
        (cmd/init-websocket!
          (str "localhost:" test-cmd-port)
          (fn [error] (swap! error-count inc))
          (fn [data] nil))
        
        ;; Wait for connection
        (is (true? (.awaitCmdConnection server 2 TimeUnit/SECONDS)))
        
        ;; Send different command types
        (cmd/send-cmd-ping)
        (Thread/sleep 50)
        (cmd/send-cmd-noop)
        (Thread/sleep 50)
        (cmd/send-cmd-frozen)
        (Thread/sleep 50)
        
        ;; Collect all commands
        (Thread/sleep 200)
        (let [all-commands (.getAllCommands server)]
          (is (>= (count all-commands) 3) "Should receive at least 3 commands")
          
          ;; Verify command types were received
          (let [command-types (set (map (fn [cmd]
                                          (cond
                                            (.hasPing cmd) :ping
                                            (.hasNoop cmd) :noop
                                            (.hasFrozen cmd) :frozen
                                            :else :unknown))
                                        all-commands))]
            (is (contains? command-types :ping) "Should have ping")
            (is (contains? command-types :noop) "Should have noop")
            (is (contains? command-types :frozen) "Should have frozen")))
        
        (is (= 0 @error-count) "Should have no errors")
        
        (finally
          (cmd/stop-websocket!)
          (.stop server))))))