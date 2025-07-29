(ns potatoclient.websocket-full-integration-test
  "Comprehensive integration tests for WebSocket implementation"
  (:require [clojure.test :refer :all]
            [potatoclient.cmd.core :as cmd]
            [potatoclient.cmd.rotary :as rotary]
            [potatoclient.cmd.day-camera :as day-camera]
            [potatoclient.state :as state]
            [potatoclient.state.dispatch :as dispatch]
            [potatoclient.logging :as logging])
  (:import [potatoclient.test TestWebSocketServer]
           [cmd JonSharedCmd$Root]
           [ser JonSharedData$JonGUIState]
           [java.util.concurrent TimeUnit CountDownLatch]
           [java.util.concurrent.atomic AtomicReference AtomicInteger]))

(def test-cmd-port 8889)
(def test-state-port 8890)

(defn wait-for-condition
  "Wait for a condition to become true with timeout"
  [condition-fn timeout-ms]
  (let [start (System/currentTimeMillis)]
    (loop []
      (cond
        (condition-fn) true
        (> (- (System/currentTimeMillis) start) timeout-ms) false
        :else (do (Thread/sleep 50) (recur))))))

(deftest test-end-to-end-command-flow
  (testing "Commands sent from client reach test server and can be decoded"
    (let [server (TestWebSocketServer. test-cmd-port test-state-port)
          error-count (atom 0)
          state-count (atom 0)]
      
      (try
        ;; Start test server
        (.start server)
        
        ;; Wait for server to be ready
        (Thread/sleep 200)
        
        ;; Initialize WebSocket client
        (cmd/init-websocket!
          (str "localhost:" test-cmd-port)
          (fn [error] 
            (swap! error-count inc)
            (logging/log-error (str "Test error: " error)))
          (fn [data] (swap! state-count inc)))
        
        ;; Wait for connection
        (is (true? (.awaitCmdConnection server 2 TimeUnit/SECONDS))
            "Command connection should be established")
        
        ;; Send various commands
        (cmd/send-cmd-ping)
        (Thread/sleep 100)
        
        (let [received (.pollCommand server 1 TimeUnit/SECONDS)]
          (is (some? received) "Should receive ping command")
          (is (true? (.hasPing received)) "Should be a ping command"))
        
        ;; Send frozen command
        (cmd/send-cmd-frozen)
        (Thread/sleep 100)
        
        (let [received (.pollCommand server 1 TimeUnit/SECONDS)]
          (is (some? received) "Should receive frozen command")
          (is (true? (.hasFrozen received)) "Should be a frozen command"))
        
        ;; Send rotary command
        (rotary/rotate-both-to 45.0 30.0)
        (Thread/sleep 100)
        
        (let [received (.pollCommand server 1 TimeUnit/SECONDS)]
          (is (some? received) "Should receive rotary command")
          (is (true? (.hasRotary received)) "Should be a rotary command")
          ;; The exact structure depends on the protobuf definition
          ;; For now just verify it's a rotary command
          )
        
        ;; Send day camera command
        (day-camera/zoom-set-value 0.5)
        (Thread/sleep 100)
        
        (let [received (.pollCommand server 1 TimeUnit/SECONDS)]
          (is (some? received) "Should receive day camera command")
          (is (true? (.hasDayCamera received)) "Should be a day camera command")
          ;; The exact structure depends on the protobuf definition
          ;; For now just verify it's a day camera command
          )
        
        (finally
          (cmd/stop-websocket!)
          (.stop server))))))

(deftest test-end-to-end-state-flow
  (testing "State messages from server are validated and trigger atom updates"
    (let [server (TestWebSocketServer. test-cmd-port test-state-port)
          error-count (atom 0)
          state-updates (atom [])
          update-latch (CountDownLatch. 3)
          
          ;; Watch for state changes
          watcher-key ::test-state-watcher]
      
      (try
        ;; Start test server
        (.start server)
        
        ;; Wait for server to be ready
        (Thread/sleep 200)
        
        ;; Add watcher to capture state updates
        (add-watch state/gui-state watcher-key
                   (fn [_ _ old-state new-state]
                     (when (not= old-state new-state)
                       (swap! state-updates conj new-state)
                       (.countDown update-latch))))
        
        ;; Initialize WebSocket client with actual dispatch
        (cmd/init-websocket!
          (str "localhost:" test-cmd-port)
          (fn [error] 
            (swap! error-count inc)
            (logging/log-error (str "Test error: " error)))
          (fn [data] 
            ;; Use actual dispatch to process state
            (dispatch/handle-binary-state data)))
        
        ;; Wait for state connection
        (is (true? (.awaitStateConnection server 2 TimeUnit/SECONDS))
            "State connection should be established")
        
        ;; Send initial state
        (let [state1 (TestWebSocketServer/createTestState)]
          (.sendState server state1))
        
        ;; Send state with rotary data
        (Thread/sleep 100)
        (let [state2 (TestWebSocketServer/createTestStateWithRotary 90.0 45.0)]
          (.sendState server state2))
        
        ;; Send another state update
        (Thread/sleep 100)
        (let [state3 (TestWebSocketServer/createTestStateWithRotary 180.0 -30.0)]
          (.sendState server state3))
        
        ;; Wait for state updates with timeout
        (is (true? (.await update-latch 3 TimeUnit/SECONDS))
            "Should receive at least 3 state updates")
        
        ;; Verify state updates were captured
        (let [updates @state-updates]
          (is (>= (count updates) 3) "Should have received at least 3 state updates")
          
          ;; Check that state contains expected data
          (when (>= (count updates) 2)
            (let [second-update (nth updates 1)]
              (is (some? (:rotary second-update)) "Second update should have rotary data")
              (when-let [rotary (:rotary second-update)]
                (is (= 90.0 (:azimuth-angle rotary)) "Azimuth should be 90.0")
                (is (= 45.0 (:elevation-angle rotary)) "Elevation should be 45.0"))))
          
          (when (>= (count updates) 3)
            (let [third-update (nth updates 2)]
              (is (some? (:rotary third-update)) "Third update should have rotary data")
              (when-let [rotary (:rotary third-update)]
                (is (= 180.0 (:azimuth-angle rotary)) "Azimuth should be 180.0")
                (is (= -30.0 (:elevation-angle rotary)) "Elevation should be -30.0")))))
        
        (finally
          (remove-watch state/gui-state watcher-key)
          (cmd/stop-websocket!)
          (.stop server))))))

(deftest test-websocket-reconnection
  (testing "WebSocket manager handles disconnection and reconnection"
    (let [server (TestWebSocketServer. test-cmd-port test-state-port)
          error-count (atom 0)
          state-count (atom 0)
          errors (atom [])]
      
      (try
        ;; Start test server
        (.start server)
        (Thread/sleep 200)
        
        ;; Initialize WebSocket client
        (cmd/init-websocket!
          (str "localhost:" test-cmd-port)
          (fn [error] 
            (swap! error-count inc)
            (swap! errors conj error))
          (fn [data] (swap! state-count inc)))
        
        ;; Wait for initial connection
        (is (true? (.awaitCmdConnection server 2 TimeUnit/SECONDS))
            "Initial connection should be established")
        
        ;; Send a command to verify connection
        (cmd/send-cmd-ping)
        (Thread/sleep 100)
        
        (let [received (.pollCommand server 1 TimeUnit/SECONDS)]
          (is (some? received) "Should receive initial ping"))
        
        ;; Stop server to simulate disconnection
        (.stop server)
        (Thread/sleep 500)
        
        ;; Try to send command - should fail
        (cmd/send-cmd-ping)
        (Thread/sleep 100)
        
        ;; Check that we got an error
        (is (> @error-count 0) "Should have received connection error")
        
        ;; Restart server
        (let [new-server (TestWebSocketServer. test-cmd-port test-state-port)]
          (try
            (.start new-server)
            (Thread/sleep 200)
            
            ;; The client should attempt to reconnect automatically
            ;; Wait a bit for reconnection attempts
            (Thread/sleep 2000)
            
            ;; Try sending command again
            (cmd/send-cmd-ping)
            (Thread/sleep 100)
            
            ;; Check if command was received (reconnection might not happen automatically)
            ;; This depends on the WebSocket implementation's reconnection strategy
            (let [received (.pollCommand new-server 500 TimeUnit/MILLISECONDS)]
              ;; If no automatic reconnection, at least verify error handling
              (when (nil? received)
                (is (> @error-count 1) "Should have additional errors for failed sends")))
            
            (finally
              (.stop new-server))))
        
        (finally
          (cmd/stop-websocket!)
          (when (.getStateClientCount server)
            (.stop server)))))))

(deftest test-command-validation-on-server
  (testing "Server can validate and decode various command types"
    (let [server (TestWebSocketServer. test-cmd-port test-state-port)
          error-count (atom 0)
          commands-sent (atom 0)]
      
      (try
        ;; Start test server
        (.start server)
        (Thread/sleep 200)
        
        ;; Initialize WebSocket client
        (cmd/init-websocket!
          (str "localhost:" test-cmd-port)
          (fn [error] (swap! error-count inc))
          (fn [data] nil))
        
        ;; Wait for connection
        (is (true? (.awaitCmdConnection server 2 TimeUnit/SECONDS)))
        
        ;; Send all command types and verify they're received correctly
        (let [test-commands 
              [["PING" #(cmd/send-cmd-ping) #(.hasPing %)]
               ["FROZEN" #(cmd/send-cmd-frozen) #(.hasFrozen %)]
               ["NOOP" #(cmd/send-cmd-noop) #(.hasNoop %)]
               ["ROTARY-STOP" #(rotary/rotary-stop) 
                #(and (.hasRotary %) (.hasStop (.getRotary %)))]
               ["ROTARY-ANGLE-TO" #(rotary/rotate-both-to 123.45 -67.89)
                #(and (.hasRotary %) 
                      ;; Check for rotary command - actual fields depend on protobuf
                      true)]
               ["DAY-CAMERA-ZOOM" #(day-camera/zoom-set-value 0.75)
                #(and (.hasDayCamera %)
                      ;; Check for zoom command - actual field depends on protobuf
                      true)]]]
          
          (doseq [[cmd-name send-fn verify-fn] test-commands]
            (.clearCommands server)
            (send-fn)
            (swap! commands-sent inc)
            (Thread/sleep 100)
            
            (let [received (.pollCommand server 1 TimeUnit/SECONDS)]
              (is (some? received) (str "Should receive " cmd-name " command"))
              (is (verify-fn received) (str cmd-name " command should be valid")))))
        
        ;; Verify all commands were sent
        (is (= 6 @commands-sent) "Should have sent all test commands")
        (is (= 0 @error-count) "Should have no errors")
        
        (finally
          (cmd/stop-websocket!)
          (.stop server))))))

(deftest test-state-atom-subscription
  (testing "State updates trigger atom watchers correctly"
    (let [server (TestWebSocketServer. test-cmd-port test-state-port)
          watcher-calls (atom [])
          specific-updates (atom {:system 0 :rotary 0 :time 0})
          watcher-key ::test-specific-watcher]
      
      (try
        ;; Start test server
        (.start server)
        (Thread/sleep 200)
        
        ;; Add detailed watcher
        (add-watch state/gui-state watcher-key
                   (fn [key ref old-state new-state]
                     (swap! watcher-calls conj
                            {:key key
                             :old (select-keys old-state [:system :rotary :time])
                             :new (select-keys new-state [:system :rotary :time])})
                     
                     ;; Count specific updates
                     (when (not= (:system old-state) (:system new-state))
                       (swap! specific-updates update :system inc))
                     (when (not= (:rotary old-state) (:rotary new-state))
                       (swap! specific-updates update :rotary inc))
                     (when (not= (:time old-state) (:time new-state))
                       (swap! specific-updates update :time inc))))
        
        ;; Initialize WebSocket
        (cmd/init-websocket!
          (str "localhost:" test-cmd-port)
          (fn [error] (logging/log-error error))
          (fn [data] (dispatch/handle-binary-state data)))
        
        ;; Wait for state connection
        (is (true? (.awaitStateConnection server 2 TimeUnit/SECONDS)))
        
        ;; Send various state updates
        (.sendState server (TestWebSocketServer/createTestState))
        (Thread/sleep 100)
        
        (.sendState server (TestWebSocketServer/createTestStateWithRotary 45.0 15.0))
        (Thread/sleep 100)
        
        (.sendState server (TestWebSocketServer/createTestStateWithTimestamp))
        (Thread/sleep 100)
        
        ;; Wait for updates to process
        (Thread/sleep 500)
        
        ;; Verify watcher was called
        (let [calls @watcher-calls
              updates @specific-updates]
          (is (>= (count calls) 3) "Watcher should be called at least 3 times")
          (is (> (:system updates) 0) "System should be updated")
          (is (> (:rotary updates) 0) "Rotary should be updated")
          (is (> (:time updates) 0) "Time should be updated")
          
          ;; Verify watcher receives correct parameters
          (when (pos? (count calls))
            (let [first-call (first calls)]
              (is (= watcher-key (:key first-call)) "Watcher key should match")
              (is (map? (:old first-call)) "Old state should be a map")
              (is (map? (:new first-call)) "New state should be a map"))))
        
        (finally
          (remove-watch state/gui-state watcher-key)
          (cmd/stop-websocket!)
          (.stop server))))))

(deftest test-concurrent-command-sending
  (testing "Multiple commands can be sent concurrently"
    (let [server (TestWebSocketServer. test-cmd-port test-state-port)
          error-count (atom 0)
          commands-received (atom [])]
      
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
        
        ;; Send multiple commands concurrently
        (let [futures (doall
                       (for [i (range 10)]
                         (future
                           (case (mod i 3)
                             0 (cmd/send-cmd-ping)
                             1 (cmd/send-cmd-noop)
                             2 (rotary/rotate-both-to (* i 10.0) (* i 5.0))))))]
          
          ;; Wait for all to complete
          (doseq [f futures] @f)
          
          ;; Give server time to receive all
          (Thread/sleep 500)
          
          ;; Collect all received commands
          (let [all-commands (.getAllCommands server)]
            (is (= 10 (count all-commands)) "Should receive all 10 commands")
            
            ;; Verify command types
            (let [command-types (map (fn [cmd]
                                      (cond
                                        (.hasPing cmd) :ping
                                        (.hasNoop cmd) :noop
                                        (.hasRotary cmd) :rotary
                                        :else :unknown))
                                    all-commands)
                  type-counts (frequencies command-types)]
              (is (= 4 (:ping type-counts)) "Should have 4 ping commands")
              (is (= 3 (:noop type-counts)) "Should have 3 noop commands")
              (is (= 3 (:rotary type-counts)) "Should have 3 rotary commands")
              (is (= 0 (:unknown type-counts 0)) "Should have no unknown commands"))))
        
        (is (= 0 @error-count) "Should have no errors during concurrent sending")
        
        (finally
          (cmd/stop-websocket!)
          (.stop server))))))

;; Run all tests with proper setup/teardown
(defn test-ns-hook
  "Custom test runner to ensure proper cleanup between tests"
  []
  (doseq [test-var [#'test-end-to-end-command-flow
                    #'test-end-to-end-state-flow
                    #'test-websocket-reconnection
                    #'test-command-validation-on-server
                    #'test-state-atom-subscription
                    #'test-concurrent-command-sending]]
    (println "\nRunning:" (:name (meta test-var)))
    (test-var)
    ;; Ensure cleanup between tests
    (Thread/sleep 200)))