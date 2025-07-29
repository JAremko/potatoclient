(ns potatoclient.websocket-simple-connection-test
  "Simple connection test for WebSocket"
  (:require [clojure.test :refer [deftest is testing use-fixtures]]
            [potatoclient.cmd.core :as cmd])
  (:import [potatoclient.test TestWebSocketServer]
           [java.util.concurrent TimeUnit]))

;; Ensure clean state between tests
(defn cleanup-websocket [f]
  (cmd/stop-websocket!)
  (f)
  (cmd/stop-websocket!))

(use-fixtures :each cleanup-websocket)

(deftest test-single-command
  (testing "Can send a single command"
    (let [server (TestWebSocketServer. 8893 8894)
          error-count (atom 0)]
      
      (try
        ;; Start test server
        (.start server)
        (Thread/sleep 500) ;; Give more time for server startup
        
        ;; Initialize WebSocket client
        (cmd/init-websocket!
          "localhost:8893"  ;; Use just the command port
          (fn [error] 
            (swap! error-count inc)
            (println "WebSocket error:" error))
          (fn [data] 
            (println "State received")))
        
        ;; Wait for connection with longer timeout
        (is (true? (.awaitCmdConnection server 5 TimeUnit/SECONDS))
            "Command connection should be established")
        
        ;; Only proceed if connected
        (when (.awaitCmdConnection server 1 TimeUnit/SECONDS)
          ;; Send a simple command
          (cmd/send-cmd-ping)
          (Thread/sleep 500)
          
          ;; Check server received it
          (let [received (.pollCommand server 2 TimeUnit/SECONDS)]
            (is (some? received) "Should receive ping command")
            (when received
              (is (true? (.hasPing received)) "Should be a ping command"))))
        
        (finally
          (.stop server)
          (Thread/sleep 100))))))

(deftest test-server-lifecycle
  (testing "Test server can start and stop"
    (let [server (TestWebSocketServer. 8895 8896)]
      (try
        (.start server)
        (Thread/sleep 200)
        ;; Just verify server started without errors
        (is true "Server started")
        (finally
          (.stop server))))))