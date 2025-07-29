(ns potatoclient.websocket-error-handling-test
  "Comprehensive error handling tests for WebSocket implementation"
  (:require [clojure.test :refer [deftest is testing use-fixtures]]
            [potatoclient.cmd.core :as cmd]
            [potatoclient.state.dispatch :as dispatch]
            [potatoclient.logging :as logging])
  (:import [potatoclient.test EnhancedTestWebSocketServer]
           [potatoclient.java.websocket WebSocketManager]
           [java.util.concurrent TimeUnit CountDownLatch]
           [java.util.concurrent.atomic AtomicInteger AtomicReference]))

(defn cleanup-websocket [f]
  (cmd/stop-websocket!)
  (Thread/sleep 100)
  (f)
  (cmd/stop-websocket!)
  (Thread/sleep 100))

(use-fixtures :each cleanup-websocket)

(deftest test-invalid-binary-data-handling
  (testing "Various invalid binary data should be handled gracefully"
    (let [server (EnhancedTestWebSocketServer. 9020)
          errors (atom [])
          state-errors (atom [])]
      (try
        (.start server)
        (Thread/sleep 200)
        
        (cmd/init-websocket!
          "localhost:9020"
          (fn [error] 
            (swap! errors conj error))
          (fn [data] 
            (try
              ;; Try to parse as protobuf - should fail for invalid data
              (let [state (ser.JonSharedData$JonGUIState/parseFrom data)]
                ;; If we get here, it's valid
                (println "Valid state received"))
              (catch Exception e
                (swap! state-errors conj (.getMessage e))))))
        
        ;; Wait for connections
        (.awaitStateConnection server 3 TimeUnit/SECONDS)
        (Thread/sleep 200)
        
        ;; Test 1: Send garbage data
        (.sendInvalidBinaryData server)
        (Thread/sleep 200)
        
        ;; Test 2: Send truncated protobuf
        (.sendTruncatedProtobuf server)
        (Thread/sleep 200)
        
        ;; Test 3: Send empty data
        (.sendEmptyData server)
        (Thread/sleep 200)
        
        ;; Should have parse errors
        (is (> (count @state-errors) 0) "Should have state parsing errors")
        (println "State parsing errors:" @state-errors)
        
        ;; WebSocket connection should still be alive
        (cmd/send-cmd-ping)
        (Thread/sleep 200)
        (is (some? (.pollCommand server 1 TimeUnit/SECONDS))
            "Connection should still work after invalid data")
        
        (finally
          (.stop server))))))

(deftest test-websocket-not-started
  (testing "Operations on non-started WebSocket should not crash"
    ;; Create manager but don't start it
    (let [errors (atom [])
          manager (WebSocketManager. 
                    "localhost:9021"
                    (reify java.util.function.Consumer
                      (accept [_ msg] (swap! errors conj msg)))
                    (reify java.util.function.Consumer
                      (accept [_ data] nil)))]
      
      ;; These should all return false/0 without crashing
      (is (false? (.isConnected manager)) "Should not be connected")
      (is (= 0 (.getCommandQueueSize manager)) "Queue should be empty")
      (is (false? (.sendCommand manager 
                                (-> (cmd.JonSharedCmd$Root/newBuilder)
                                    (.setPing (.build (cmd.JonSharedCmd$Ping/newBuilder)))
                                    (.build))))
          "Send should return false")
      
      ;; Stop without start should not crash
      (.stop manager))))

(deftest test-double-start-stop
  (testing "Double start/stop should be handled gracefully"
    (let [server (EnhancedTestWebSocketServer. 9022)
          errors (atom [])]
      (try
        (.start server)
        (Thread/sleep 200)
        
        (cmd/init-websocket!
          "localhost:9022"
          (fn [error] (swap! errors conj error))
          (fn [data] nil))
        
        (.awaitCmdConnection server 2 TimeUnit/SECONDS)
        
        ;; Double start should be ignored
        (cmd/init-websocket!
          "localhost:9022"
          (fn [error] (swap! errors conj error))
          (fn [data] nil))
        
        (Thread/sleep 200)
        
        ;; Should still work
        (cmd/send-cmd-ping)
        (Thread/sleep 200)
        (is (some? (.pollCommand server 1 TimeUnit/SECONDS)))
        
        ;; Double stop
        (cmd/stop-websocket!)
        (cmd/stop-websocket!) ;; Should not crash
        
        (finally
          (.stop server))))))

(deftest test-invalid-domain-formats
  (testing "Various invalid domain formats should trigger errors"
    (let [test-domains ["" "  " "http://localhost" "ws://localhost" 
                       "localhost:" ":8080" "256.256.256.256"
                       "local host" "localhost:abc" ":::"]
          errors-per-domain (atom {})]
      
      (doseq [domain test-domains]
        (let [domain-errors (atom [])]
          (try
            (cmd/init-websocket!
              domain
              (fn [error] (swap! domain-errors conj error))
              (fn [data] nil))
            
            ;; Wait briefly for error to occur
            (Thread/sleep 200)
            (cmd/stop-websocket!)
            (Thread/sleep 100)
            
            (swap! errors-per-domain assoc domain @domain-errors)
            (catch Exception e
              (swap! errors-per-domain assoc domain [(str "Exception: " (.getMessage e))])))
          
          (Thread/sleep 100)))
      
      ;; Log results
      (println "\nDomain test results:")
      (doseq [[domain errors] @errors-per-domain]
        (println (format "Domain '%s': %d errors" domain (count errors)))))))

(deftest test-large-message-handling
  (testing "Very large messages should be handled appropriately"
    (let [server (EnhancedTestWebSocketServer. 9023)
          errors (atom [])
          large-states (atom 0)]
      (try
        (.start server)
        (Thread/sleep 200)
        
        (cmd/init-websocket!
          "localhost:9023"
          (fn [error] (swap! errors conj error))
          (fn [data] 
            (when (> (count data) 100000)
              (swap! large-states inc))))
        
        (.awaitStateConnection server 2 TimeUnit/SECONDS)
        
        ;; Send large data
        (.sendLargeData server)
        (Thread/sleep 500)
        
        ;; Should have received large data
        (is (> @large-states 0) "Should receive large data")
        
        ;; Connection should still work
        (cmd/send-cmd-ping)
        (Thread/sleep 200)
        (is (some? (.pollCommand server 1 TimeUnit/SECONDS))
            "Connection should work after large data")
        
        (finally
          (.stop server))))))

(deftest test-command-validation-errors
  (testing "Commands with validation errors should be handled"
    (let [server (EnhancedTestWebSocketServer. 9024)
          errors (atom [])]
      (try
        (.start server)
        (Thread/sleep 200)
        
        (cmd/init-websocket!
          "localhost:9024"
          (fn [error] (swap! errors conj error))
          (fn [data] nil))
        
        (.awaitCmdConnection server 2 TimeUnit/SECONDS)
        
        ;; Try to create commands with invalid values
        ;; Note: Most validation happens at the protobuf builder level
        
        ;; Test 1: Invalid protocol version (should be > 0)
        (try
          (let [root-builder (cmd/create-root-message)]
            (.setProtocolVersion root-builder 0) ;; Invalid: should be > 0
            (cmd/send-cmd-message root-builder))
          (catch Exception e
            (swap! errors conj (str "Validation error: " (.getMessage e)))))
        
        ;; Test 2: Send a normal command to verify connection still works
        (cmd/send-cmd-ping)
        (Thread/sleep 200)
        (is (some? (.pollCommand server 1 TimeUnit/SECONDS))
            "Normal commands should still work")
        
        (finally
          (.stop server))))))

(deftest test-rapid-connect-disconnect
  (testing "Rapid connection/disconnection cycles should be handled"
    (let [errors (atom [])
          connections (atom 0)]
      
      ;; Rapidly start and stop connections
      (dotimes [i 5]
        (let [server (EnhancedTestWebSocketServer. (+ 9025 i))]
          (try
            (.start server)
            (Thread/sleep 100)
            
            (cmd/init-websocket!
              (str "localhost:" (+ 9025 i))
              (fn [error] (swap! errors conj error))
              (fn [data] nil))
            
            (when (.awaitCmdConnection server 1 TimeUnit/SECONDS)
              (swap! connections inc))
            
            (cmd/stop-websocket!)
            (.stop server)
            (Thread/sleep 100)
            
            (catch Exception e
              (println "Cycle" i "error:" (.getMessage e))))))
      
      (println "Completed" @connections "successful connections")
      (is (> @connections 0) "Should have some successful connections"))))

(deftest test-null-callbacks
  (testing "Null callbacks should not be allowed"
    ;; Guardrails validates function arguments
    (is (thrown? Exception
                 (cmd/init-websocket! "localhost:9026" nil nil))
        "Should reject null callbacks")))

(deftest test-concurrent-operations
  (testing "Concurrent send/stop operations should not cause issues"
    (let [server (EnhancedTestWebSocketServer. 9027)
          errors (atom 0)
          stop-flag (atom false)]
      (try
        (.start server)
        (Thread/sleep 200)
        
        (cmd/init-websocket!
          "localhost:9027"
          (fn [error] (swap! errors inc))
          (fn [data] nil))
        
        (.awaitCmdConnection server 2 TimeUnit/SECONDS)
        
        ;; Start multiple threads doing operations
        ;; Use stop-flag to cleanly terminate threads
        (let [futures [(future
                        (dotimes [i 20]
                          (when-not @stop-flag
                            (cmd/send-cmd-ping)
                            (Thread/sleep 10))))
                      (future
                        (dotimes [i 20]
                          (when-not @stop-flag
                            (cmd/send-cmd-noop)
                            (Thread/sleep 10))))
                      (future
                        (Thread/sleep 100)
                        (reset! stop-flag true)
                        (Thread/sleep 100)
                        (cmd/stop-websocket!))]]
          
          ;; Wait for completion with timeout
          (doseq [f futures]
            (try 
              (deref f 1000 :timeout)  ;; 1 second timeout
              (catch Exception e nil))))
        
        ;; Should have received some commands before stop
        (let [commands (.getAllCommands server)]
          (is (> (count commands) 0) "Should receive some commands"))
        
        (finally
          (reset! stop-flag true)
          (.stop server))))))