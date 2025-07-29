(ns potatoclient.websocket-unified-test
  "Tests using unified WebSocket server"
  (:require [clojure.test :refer [deftest is testing use-fixtures]]
            [potatoclient.cmd.core :as cmd]
            [potatoclient.cmd.rotary :as rotary]
            [potatoclient.cmd.day-camera :as day-camera]
            [potatoclient.state.device :as device]
            [potatoclient.logging :as logging])
  (:import [potatoclient.test UnifiedTestWebSocketServer]
           [java.util.concurrent TimeUnit CountDownLatch]
           [java.util.concurrent.atomic AtomicInteger]))

;; Ensure clean state between tests
(defn cleanup-websocket [f]
  (cmd/stop-websocket!)
  (Thread/sleep 100)
  (f)
  (cmd/stop-websocket!)
  (Thread/sleep 100))

(use-fixtures :each cleanup-websocket)

(deftest test-unified-server-connections
  (testing "Both command and state connections work with unified server"
    (let [server (UnifiedTestWebSocketServer. 8900)
          error-count (atom 0)
          state-count (atom 0)]
      
      (try
        ;; Start unified server
        (.start server)
        (Thread/sleep 500)
        
        ;; Initialize WebSocket client
        (cmd/init-websocket!
          "localhost:8900"
          (fn [error] 
            (swap! error-count inc)
            (println "Error:" error))
          (fn [data] 
            (swap! state-count inc)
            (println "State received, size:" (count data))))
        
        ;; Both connections should establish
        (is (true? (.awaitCmdConnection server 5 TimeUnit/SECONDS))
            "Command connection should be established")
        (is (true? (.awaitStateConnection server 5 TimeUnit/SECONDS))
            "State connection should be established")
        
        ;; Send a command
        (cmd/send-cmd-ping)
        (Thread/sleep 200)
        
        ;; Check command was received
        (let [received (.pollCommand server 1 TimeUnit/SECONDS)]
          (is (some? received) "Should receive ping command")
          (when received
            (is (true? (.hasPing received)) "Should be a ping command")))
        
        ;; Send a state from server
        (.sendState server (UnifiedTestWebSocketServer/createTestState))
        (Thread/sleep 200)
        
        ;; Check state was received
        (is (> @state-count 0) "Should have received state")
        
        ;; No errors
        (is (= 0 @error-count) "Should have no errors")
        
        (finally
          (.stop server)
          (Thread/sleep 100))))))

(deftest test-multiple-commands-unified
  (testing "Can send multiple commands through unified server"
    (let [server (UnifiedTestWebSocketServer. 8901)
          error-count (atom 0)]
      
      (try
        (.start server)
        (Thread/sleep 500)
        
        (cmd/init-websocket!
          "localhost:8901"
          (fn [error] (swap! error-count inc))
          (fn [data] nil))
        
        ;; Wait for connections
        (is (true? (.awaitCmdConnection server 5 TimeUnit/SECONDS)))
        (is (true? (.awaitStateConnection server 5 TimeUnit/SECONDS)))
        
        ;; Send various commands
        (cmd/send-cmd-ping)
        (Thread/sleep 50)
        (cmd/send-cmd-noop)
        (Thread/sleep 50)
        (cmd/send-cmd-frozen)
        (Thread/sleep 50)
        (rotary/rotary-stop)
        (Thread/sleep 50)
        (day-camera/zoom-in)
        (Thread/sleep 300)
        
        ;; Check all commands received
        (let [commands (.getAllCommands server)]
          (is (>= (count commands) 5) "Should receive at least 5 commands")
          
          (let [types (set (map (fn [cmd]
                                 (cond
                                   (.hasPing cmd) :ping
                                   (.hasNoop cmd) :noop
                                   (.hasFrozen cmd) :frozen
                                   (.hasRotary cmd) :rotary
                                   (.hasDayCamera cmd) :day-camera
                                   :else :unknown))
                               commands))]
            (is (contains? types :ping))
            (is (contains? types :noop))
            (is (contains? types :frozen))
            (is (contains? types :rotary))
            (is (contains? types :day-camera))))
        
        (is (= 0 @error-count) "Should have no errors")
        
        (finally
          (.stop server)
          (Thread/sleep 100))))))

(deftest test-state-flow-unified
  (testing "State messages flow correctly through unified server"
    (let [server (UnifiedTestWebSocketServer. 8902)
          error-count (atom 0)
          states-received (atom [])
          state-latch (CountDownLatch. 3)]
      
      (try
        (.start server)
        (Thread/sleep 500)
        
        (cmd/init-websocket!
          "localhost:8902"
          (fn [error] (swap! error-count inc))
          (fn [data] 
            (swap! states-received conj data)
            (.countDown state-latch)))
        
        ;; Wait for connections
        (is (true? (.awaitCmdConnection server 5 TimeUnit/SECONDS)))
        (is (true? (.awaitStateConnection server 5 TimeUnit/SECONDS)))
        
        ;; Send multiple states
        (dotimes [i 3]
          (.sendState server (UnifiedTestWebSocketServer/createTestState))
          (Thread/sleep 100))
        
        ;; Wait for states to be received
        (is (true? (.await state-latch 3 TimeUnit/SECONDS))
            "Should receive 3 states")
        
        (is (= 3 (count @states-received)) "Should have exactly 3 states")
        (is (every? bytes? @states-received) "All states should be byte arrays")
        (is (= 0 @error-count) "Should have no errors")
        
        (finally
          (.stop server)
          (Thread/sleep 100))))))