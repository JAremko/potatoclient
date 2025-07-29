(ns potatoclient.websocket-simple-connection-test
  "Simple connection test for WebSocket - now using stubs instead of real server"
  (:require [clojure.test :refer [deftest is testing]]
            [potatoclient.cmd.core :as cmd]
            [potatoclient.test-utils :as test-utils]))

(deftest test-single-command
  (testing "Can send a single command"
    (test-utils/with-mock-websocket
      (fn [{:keys [commands-ch errors]}]
        ;; Send a simple command
        (cmd/send-cmd-ping)
        
        ;; Check we received it
        (let [commands (test-utils/get-commands commands-ch 100)]
          (is (= 1 (count commands)) "Should receive ping command")
          (when (= 1 (count commands))
            (let [cmd (first commands)]
              (is (true? (.hasPing cmd)) "Should be a ping command"))))
        
        ;; Verify no errors
        (is (empty? @errors) "Should have no errors")))))

(deftest test-server-lifecycle
  (testing "Mock server lifecycle"
    (let [mock-ctx (test-utils/create-mock-websocket-manager nil nil)]
      (try
        ;; Verify starts disconnected
        (is (false? @(:connected? mock-ctx)) "Should start disconnected")
        
        ;; Start server
        (.start (:manager mock-ctx))
        (is (true? @(:connected? mock-ctx)) "Should be connected after start")
        
        ;; Stop server
        (.stop (:manager mock-ctx))
        (is (false? @(:connected? mock-ctx)) "Should be disconnected after stop")
        
        (finally
          (when (.isConnected (:manager mock-ctx))
            (.stop (:manager mock-ctx))))))))