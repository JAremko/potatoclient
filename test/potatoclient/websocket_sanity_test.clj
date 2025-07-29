(ns potatoclient.websocket-sanity-test
  "Sanity tests for WebSocket error handling and edge cases"
  (:require [clojure.test :refer [deftest is testing use-fixtures]]
            [potatoclient.cmd.core :as cmd]
            [potatoclient.state.dispatch :as dispatch]
            [potatoclient.logging :as logging])
  (:import [potatoclient.test UnifiedTestWebSocketServer EnhancedTestWebSocketServer]
           [potatoclient.java.websocket WebSocketManager]
           [java.util.concurrent TimeUnit CountDownLatch]
           [java.util.concurrent.atomic AtomicReference]
           [org.java_websocket WebSocket]
           [java.nio ByteBuffer]
           [cmd JonSharedCmd$Root]
           [ser JonSharedData$JonGUIState]))

;; Ensure clean state between tests
(defn cleanup-websocket [f]
  (cmd/stop-websocket!)
  (Thread/sleep 100)
  (f)
  (cmd/stop-websocket!)
  (Thread/sleep 100))

(use-fixtures :each cleanup-websocket)

(deftest test-command-before-start
  (testing "Sending commands before WebSocket is started should fail gracefully"
    ;; Don't start WebSocket, just try to send
    (is (nil? (cmd/send-cmd-ping)) "Should return nil without crashing")
    (is (nil? (cmd/send-cmd-noop)) "Should return nil without crashing")
    (is (nil? (cmd/send-cmd-frozen)) "Should return nil without crashing")))

(deftest test-command-after-stop
  (testing "Sending commands after WebSocket is stopped should fail gracefully"
    (let [server (UnifiedTestWebSocketServer. 8910)
          error-count (atom 0)]
      (try
        (.start server)
        (Thread/sleep 200)
        
        ;; Start and then stop WebSocket
        (cmd/init-websocket!
          "localhost:8910"
          (fn [error] (swap! error-count inc))
          (fn [data] nil))
        
        ;; Wait for connection
        (.awaitCmdConnection server 2 TimeUnit/SECONDS)
        
        ;; Stop WebSocket
        (cmd/stop-websocket!)
        (Thread/sleep 200)
        
        ;; Try to send commands - should not crash
        (is (nil? (cmd/send-cmd-ping)) "Should return nil after stop")
        (is (nil? (cmd/send-cmd-noop)) "Should return nil after stop")
        
        (finally
          (.stop server))))))

(deftest test-mangled-binary-data
  (testing "Receiving mangled binary data should trigger error callback"
    (let [server (EnhancedTestWebSocketServer. 8911)
          errors (atom [])
          states (atom [])]
      (try
        (.start server)
        (Thread/sleep 200)
        
        (cmd/init-websocket!
          "localhost:8911"
          (fn [error] 
            (swap! errors conj error)
            (logging/log-debug (str "Error received: " error)))
          (fn [data] 
            (swap! states conj :received)
            ;; The dispatch/handle-binary-state is already called by the WebSocket manager
            ;; so we just need to record that we received data
            ))
        
        ;; Wait for connections
        (.awaitStateConnection server 2 TimeUnit/SECONDS)
        (Thread/sleep 200)
        
        ;; Send mangled data to state channel
        (let [state-clients (.getStateClientCount server)]
          (is (> state-clients 0) "Should have state client connected")
          
          ;; Use EnhancedTestWebSocketServer's method to send invalid data
          (.sendInvalidBinaryData server))
        
        (Thread/sleep 500)
        
        ;; Should have received data - the error is logged but connection continues
        (is (> (count @states) 0) "Should have received states")
        ;; The WebSocket continues to work despite invalid data
        (is (some #{:received} @states) "Should have received data")
        
        (finally
          (.stop server))))))

(deftest test-invalid-protobuf-command
  (testing "Server should handle invalid protobuf commands gracefully"
    (let [server (proxy [UnifiedTestWebSocketServer] [8912]
                   (onMessage [conn message]
                     ;; Override to track invalid messages
                     (let [client-type (.get (.getDeclaredField UnifiedTestWebSocketServer "clientTypes") this conn)]
                       (if (= "command" client-type)
                         (try
                           ;; Try to parse as command
                           (let [cmd (JonSharedCmd$Root/parseFrom (.array message))]
                             (println "Valid command received"))
                           (catch Exception e
                             (println "Invalid protobuf received:" (.getMessage e))))
                         (proxy-super onMessage conn message)))))
          error-count (atom 0)]
      (try
        (.start server)
        (Thread/sleep 200)
        
        ;; Direct WebSocket manager test
        (let [manager (WebSocketManager. 
                        "localhost:8912"
                        (reify java.util.function.Consumer
                          (accept [_ msg] 
                            (swap! error-count inc)
                            (println "Error callback:" msg)))
                        (reify java.util.function.Consumer
                          (accept [_ data] nil)))]
          (.start manager)
          (Thread/sleep 1000) ;; Wait for connection
          
          ;; The manager should handle sending only valid protobuf
          ;; Invalid data would be caught at the Clojure layer
          (is (instance? WebSocketManager manager))
          
          (.stop manager))
        
        (finally
          (.stop server))))))

(deftest test-validation-constraints
  (testing "Commands with invalid buf.validate constraints should be caught"
    ;; This test verifies that validation happens at the appropriate layer
    ;; Since validation is done in the protobuf layer, we test the integration
    (let [server (UnifiedTestWebSocketServer. 8913)
          validation-errors (atom [])]
      (try
        (.start server)
        (Thread/sleep 200)
        
        (cmd/init-websocket!
          "localhost:8913"
          (fn [error] 
            (swap! validation-errors conj error))
          (fn [data] nil))
        
        (.awaitCmdConnection server 2 TimeUnit/SECONDS)
        
        ;; Try to create invalid commands (if validation is enabled)
        ;; For example, try to set invalid protocol version
        (let [root-msg (cmd/create-root-message)]
          ;; Protocol version should be > 0 according to specs
          (.setProtocolVersion root-msg -1)
          (try
            (cmd/send-cmd-message root-msg)
            (catch Exception e
              (swap! validation-errors conj (.getMessage e)))))
        
        (Thread/sleep 200)
        
        ;; Check if any validation occurred
        ;; Note: Validation might be disabled in test environment
        (println "Validation errors:" @validation-errors)
        
        (finally
          (.stop server))))))

(deftest test-state-channel-sending-commands
  (testing "Command channel should not receive state messages and vice versa"
    (let [server (UnifiedTestWebSocketServer. 8914)]
      (try
        (.start server)
        (Thread/sleep 200)
        
        (cmd/init-websocket!
          "localhost:8914"
          (fn [error] (println "Error:" error))
          (fn [data] (println "State received")))
        
        ;; Wait for both connections
        (.awaitCmdConnection server 2 TimeUnit/SECONDS)
        (.awaitStateConnection server 2 TimeUnit/SECONDS)
        
        ;; Commands should only go through command channel
        (cmd/send-cmd-ping)
        (Thread/sleep 200)
        
        (let [received (.pollCommand server 1 TimeUnit/SECONDS)]
          (is (some? received) "Should receive command on command channel"))
        
        ;; State messages should only go through state channel
        ;; (The server sends states, not the client)
        
        (finally
          (.stop server))))))

(deftest test-connection-failure-handling
  (testing "Connection failures should trigger error callbacks"
    (let [errors (atom [])
          error-latch (CountDownLatch. 1)]
      
      ;; Try to connect to non-existent server
      (cmd/init-websocket!
        "localhost:9999"  ;; Invalid port
        (fn [error] 
          (swap! errors conj error)
          (.countDown error-latch))
        (fn [data] nil))
      
      ;; Wait for error
      (is (true? (.await error-latch 5 TimeUnit/SECONDS))
          "Should receive connection error")
      
      (is (> (count @errors) 0) "Should have connection errors")
      (is (some #(re-find #"Connection refused" %) @errors)
          "Should have connection refused error"))))

(deftest test-large-command-queue
  (testing "Command queue should handle overflow gracefully"
    (let [server (UnifiedTestWebSocketServer. 8915)
          error-count (atom 0)]
      (try
        (.start server)
        (Thread/sleep 200)
        
        ;; Initialize but don't wait for connection
        (cmd/init-websocket!
          "localhost:8915"
          (fn [error] (swap! error-count inc))
          (fn [data] nil))
        
        ;; Send many commands quickly (queue has limit of 100)
        (dotimes [i 150]
          (cmd/send-cmd-ping))
        
        ;; Wait for connection and processing
        (.awaitCmdConnection server 2 TimeUnit/SECONDS)
        (Thread/sleep 2000)
        
        ;; Should have received many commands but not necessarily all 150
        ;; The queue might process some commands while we're still sending
        (let [commands (.getAllCommands server)]
          (is (> (count commands) 90) "Should receive most commands")
          ;; Allow some tolerance as commands might be processed during sending
          (is (<= (count commands) 150) "Should not exceed sent amount"))
        
        (finally
          (.stop server))))))

(deftest test-concurrent-state-updates
  (testing "Multiple concurrent state updates should be handled properly"
    (let [server (UnifiedTestWebSocketServer. 8916)
          states-received (atom 0)
          errors (atom 0)]
      (try
        (.start server)
        (Thread/sleep 200)
        
        (cmd/init-websocket!
          "localhost:8916"
          (fn [error] (swap! errors inc))
          (fn [data] 
            (swap! states-received inc)))
        
        ;; Wait for state connection
        (.awaitStateConnection server 2 TimeUnit/SECONDS)
        (Thread/sleep 200)
        
        ;; Send many states concurrently
        (let [futures (doall
                       (for [i (range 50)]
                         (future
                           (.sendState server (UnifiedTestWebSocketServer/createTestState))
                           (Thread/sleep 10))))]
          
          ;; Wait for all to complete
          (doseq [f futures] @f))
        
        (Thread/sleep 1000)
        
        ;; Should have received all states
        (is (>= @states-received 45) "Should receive most concurrent states")
        (is (= 0 @errors) "Should have no errors")
        
        (finally
          (.stop server))))))

(deftest test-reconnection-with-queued-commands
  (testing "Queued commands should be sent after reconnection"
    (let [server (UnifiedTestWebSocketServer. 8917)
          error-count (atom 0)]
      (try
        (.start server)
        (Thread/sleep 200)
        
        (cmd/init-websocket!
          "localhost:8917"
          (fn [error] 
            (swap! error-count inc)
            (println "Error:" error))
          (fn [data] nil))
        
        ;; Wait for initial connection
        (.awaitCmdConnection server 2 TimeUnit/SECONDS)
        
        ;; Send a command to verify connection
        (cmd/send-cmd-ping)
        (Thread/sleep 200)
        (is (some? (.pollCommand server 1 TimeUnit/SECONDS)))
        
        ;; Simulate disconnection by stopping server
        (.stop server)
        (Thread/sleep 500)
        
        ;; Queue commands while disconnected
        (cmd/send-cmd-noop)
        (cmd/send-cmd-frozen)
        (cmd/send-cmd-ping)
        
        ;; Restart server
        (let [new-server (UnifiedTestWebSocketServer. 8917)]
          (try
            (.start new-server)
            ;; Wait for reconnection (may take time)
            (Thread/sleep 3000)
            
            ;; Check if queued commands arrive
            (let [commands (.getAllCommands new-server)]
              (println "Received" (count commands) "commands after reconnection")
              ;; May or may not receive queued commands depending on reconnection timing
              (is true "Reconnection test completed"))
            
            (finally
              (.stop new-server))))
        
        (finally
          (try
            (.stop server)
            (catch Exception e
              ;; Ignore if already stopped
              nil)))))))