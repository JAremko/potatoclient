(ns potatoclient.websocket-edge-cases-test
  "Edge case tests for WebSocket implementation"
  (:require [clojure.test :refer [deftest is testing use-fixtures]]
            [potatoclient.cmd.core :as cmd]
            [potatoclient.logging :as logging])
  (:import [potatoclient.test UnifiedTestWebSocketServer EnhancedTestWebSocketServer]
           [potatoclient.java.websocket WebSocketManager]
           [java.util.concurrent TimeUnit]
           [java.nio ByteBuffer]))

(defn cleanup-websocket [f]
  (cmd/stop-websocket!)
  (Thread/sleep 200)
  (f)
  (cmd/stop-websocket!)
  (Thread/sleep 200))

(use-fixtures :each cleanup-websocket)

(deftest test-commands-before-connection
  (testing "Commands sent before WebSocket starts should not crash"
    ;; No WebSocket initialized
    (is (nil? (cmd/send-cmd-ping)) "Ping should return nil")
    (is (nil? (cmd/send-cmd-noop)) "Noop should return nil")
    (is (nil? (cmd/send-cmd-frozen)) "Frozen should return nil")))

(deftest test-commands-after-stop
  (testing "Commands sent after stop should not crash"
    (let [server (UnifiedTestWebSocketServer. 8930)]
      (try
        (.start server)
        (Thread/sleep 200)
        
        ;; Start WebSocket
        (cmd/init-websocket!
          "localhost:8930"
          (fn [error] (println "Error:" error))
          (fn [data] nil))
        
        ;; Verify it works
        (.awaitCmdConnection server 2 TimeUnit/SECONDS)
        (cmd/send-cmd-ping)
        (Thread/sleep 200)
        (is (some? (.pollCommand server 1 TimeUnit/SECONDS)))
        
        ;; Stop it
        (cmd/stop-websocket!)
        (Thread/sleep 500)
        
        ;; These should not crash
        (is (nil? (cmd/send-cmd-ping)) "Should return nil after stop")
        (is (nil? (cmd/send-cmd-noop)) "Should return nil after stop")
        
        (finally
          (.stop server))))))

(deftest test-invalid-port-connection
  (testing "Connection to invalid port should trigger error callback"
    (let [errors (atom [])]
      ;; Try to connect to port that doesn't exist
      (cmd/init-websocket!
        "localhost:9999"
        (fn [error] 
          (swap! errors conj error))
        (fn [data] nil))
      
      ;; Wait for connection attempts
      (Thread/sleep 2000)
      
      ;; Should have connection errors
      (is (> (count @errors) 0) "Should have connection errors")
      (is (some #(re-find #"Connection refused" %) @errors)
          "Should have connection refused error"))))

(deftest test-empty-domain
  (testing "Empty domain should be handled"
    (let [errors (atom [])]
      (try
        (cmd/init-websocket!
          ""
          (fn [error] (swap! errors conj error))
          (fn [data] nil))
        
        (Thread/sleep 1000)
        
        ;; Should have errors
        (is (> (count @errors) 0) "Should have errors for empty domain")
        
        (catch Exception e
          ;; Also acceptable - might fail at validation level
          (is true "Exception is acceptable for empty domain"))))))

(deftest test-multiple-starts
  (testing "Multiple init calls should replace previous connection"
    (let [server1 (UnifiedTestWebSocketServer. 8931)
          server2 (UnifiedTestWebSocketServer. 8932)
          errors (atom [])]
      (try
        (.start server1)
        (.start server2)
        (Thread/sleep 200)
        
        ;; Connect to first server
        (cmd/init-websocket!
          "localhost:8931"
          (fn [error] (swap! errors conj error))
          (fn [data] nil))
        
        (.awaitCmdConnection server1 2 TimeUnit/SECONDS)
        
        ;; Send command to first server
        (cmd/send-cmd-ping)
        (Thread/sleep 200)
        (is (some? (.pollCommand server1 1 TimeUnit/SECONDS))
            "First server should receive command")
        
        ;; Connect to second server (should stop first)
        (cmd/init-websocket!
          "localhost:8932"
          (fn [error] (swap! errors conj error))
          (fn [data] nil))
        
        (.awaitCmdConnection server2 2 TimeUnit/SECONDS)
        (Thread/sleep 500)
        
        ;; Send command - should go to second server
        (cmd/send-cmd-noop)
        (Thread/sleep 200)
        (is (some? (.pollCommand server2 1 TimeUnit/SECONDS))
            "Second server should receive command")
        
        ;; First server should not receive new commands
        (cmd/send-cmd-frozen)
        (Thread/sleep 200)
        (is (nil? (.pollCommand server1 100 TimeUnit/MILLISECONDS))
            "First server should not receive new commands")
        
        (finally
          (.stop server1)
          (.stop server2))))))

(deftest test-websocket-with-garbage-data
  (testing "WebSocket should handle garbage data gracefully"
    (let [server (EnhancedTestWebSocketServer. 8933)
          errors (atom [])
          state-count (atom 0)]
      (try
        (.start server)
        (Thread/sleep 200)
        
        (cmd/init-websocket!
          "localhost:8933"
          (fn [error] (swap! errors conj error))
          (fn [data] 
            (swap! state-count inc)))
        
        (.awaitStateConnection server 2 TimeUnit/SECONDS)
        
        ;; Send garbage data using the enhanced server method
        (.sendInvalidBinaryData server)
        
        (Thread/sleep 500)
        
        ;; Should have received data (even if it couldn't be parsed)
        (is (> @state-count 0) "Should have received data")
        
        ;; Connection should still work for commands
        (cmd/send-cmd-ping)
        (Thread/sleep 200)
        (is (some? (.pollCommand server 1 TimeUnit/SECONDS))
            "Commands should still work after garbage data")
        
        (finally
          (.stop server))))))

(deftest test-rapid-commands
  (testing "Rapid command sending should not cause issues"
    (let [server (UnifiedTestWebSocketServer. 8934)
          errors (atom 0)]
      (try
        (.start server)
        (Thread/sleep 200)
        
        (cmd/init-websocket!
          "localhost:8934"
          (fn [error] (swap! errors inc))
          (fn [data] nil))
        
        (.awaitCmdConnection server 2 TimeUnit/SECONDS)
        
        ;; Send many commands rapidly
        (dotimes [_ 100]
          (cmd/send-cmd-ping))
        
        ;; Give time to process
        (Thread/sleep 1000)
        
        ;; Should receive many commands
        (let [commands (.getAllCommands server)]
          (is (> (count commands) 50) "Should receive most rapid commands")
          (is (<= (count commands) 100) "Should not exceed sent amount"))
        
        (is (= 0 @errors) "Should have no errors")
        
        (finally
          (.stop server))))))

(deftest test-state-callback-error
  (testing "Errors in state callback should not crash WebSocket"
    (let [server (UnifiedTestWebSocketServer. 8935)
          ws-errors (atom [])
          callback-errors (atom 0)]
      (try
        (.start server)
        (Thread/sleep 200)
        
        (cmd/init-websocket!
          "localhost:8935"
          (fn [error] (swap! ws-errors conj error))
          (fn [data]
            ;; Intentionally throw error in callback
            (swap! callback-errors inc)
            (throw (Exception. "Test callback error"))))
        
        (.awaitStateConnection server 2 TimeUnit/SECONDS)
        
        ;; Send valid state
        (.sendState server (UnifiedTestWebSocketServer/createTestState))
        (Thread/sleep 500)
        
        ;; Callback should have been called despite error
        (is (> @callback-errors 0) "Callback should have been invoked")
        
        ;; Connection should still work
        (cmd/send-cmd-ping)
        (Thread/sleep 200)
        (is (some? (.pollCommand server 1 TimeUnit/SECONDS))
            "WebSocket should still work after callback error")
        
        (finally
          (.stop server))))))

(deftest test-manager-lifecycle
  (testing "Direct WebSocketManager lifecycle operations"
    (let [errors (atom [])
          manager (WebSocketManager. 
                    "localhost:8936"
                    (reify java.util.function.Consumer
                      (accept [_ msg] (swap! errors conj msg)))
                    (reify java.util.function.Consumer
                      (accept [_ data] nil)))]
      
      ;; Before start
      (is (false? (.isConnected manager)) "Should not be connected before start")
      (is (= 0 (.getCommandQueueSize manager)) "Queue should be empty")
      
      ;; Start (no server, so it won't connect)
      (.start manager)
      (Thread/sleep 500)
      
      ;; Should have connection errors
      (is (> (count @errors) 0) "Should have connection errors")
      
      ;; Multiple starts should be safe
      (.start manager)
      
      ;; Stop
      (.stop manager)
      
      ;; Multiple stops should be safe
      (.stop manager)
      
      ;; After stop
      (is (false? (.isConnected manager)) "Should not be connected after stop"))))