(ns potatoclient.ipc.core-test
  "Tests for IPC server functionality."
  (:require 
   [clojure.test :refer [deftest is testing use-fixtures]]
   [matcher-combinators.test]
   [matcher-combinators.matchers :as matchers]
   [potatoclient.ipc.core :as ipc]
   [potatoclient.ipc.transit :as transit])
  (:import 
   [java.util.concurrent CountDownLatch TimeUnit]
   [java.nio.file Files LinkOption]))

;; Test fixtures
(defn cleanup-servers
  "Cleanup any leftover servers after tests."
  [f]
  ;; Clean up any existing sockets
  (ipc/stop-all-servers)
  (Thread/sleep 100) ; Give time for sockets to clean up
  (try
    (f)
    (finally
      (ipc/stop-all-servers)
      (Thread/sleep 100)))) ; Give time for final cleanup

(use-fixtures :each cleanup-servers)

(deftest server-lifecycle-test
  (testing "Server creation and startup"
    (ipc/stop-all-servers) ; Extra cleanup before test
    (let [server (ipc/create-and-register-server :heat)]
      (is (not (nil? server)))
      (is (ipc/server-running? server))
      (is (Files/exists (:socket-path server) (make-array LinkOption 0)))
      
      ;; Clean up
      (ipc/stop-server server)))
  
  (testing "Server stop cleans up resources"
    (ipc/stop-all-servers) ; Extra cleanup before test
    (let [server (ipc/create-and-register-server :day)
          socket-path (:socket-path server)]
      (is (Files/exists socket-path (make-array LinkOption 0)))
      
      (ipc/stop-server server)
      (is (not (ipc/server-running? server)))
      (is (not (Files/exists socket-path (make-array LinkOption 0)))))))

(deftest message-queue-test
  (testing "Message queuing through handler"
    (let [received (atom nil)
          latch (CountDownLatch. 1)
          server (ipc/create-and-register-server :heat
                                                  :on-message (fn [msg]
                                                                (reset! received msg)
                                                                (.countDown latch)))]
      ;; Simulate receiving a message by putting it in the queue
      (let [test-message {:msg-type :test :data "hello"}]
        (.offer (:message-queue server) test-message)
        
        ;; Wait for message to be processed
        (is (.await latch 1 TimeUnit/SECONDS))
        (is (= test-message @received)))
      
      (ipc/stop-server server)))
  
  (testing "Try-receive returns nil when queue empty"
    (let [server (ipc/create-and-register-server :heat)]
      (is (nil? (ipc/try-receive-message server)))
      (ipc/stop-server server)))
  
  (testing "Receive with timeout"
    (let [server (ipc/create-and-register-server :day)]
      (is (nil? (ipc/receive-message server :timeout-ms 100)))
      (ipc/stop-server server))))

(deftest server-pool-test
  (testing "Server registration and retrieval"
    (let [server (ipc/create-and-register-server :heat)]
      (is (= server (ipc/get-server :heat)))
      (is (nil? (ipc/get-server :day)))))
  
  (testing "Can recreate servers (old one is stopped)"
    (let [server1 (ipc/create-and-register-server :heat)
          server2 (ipc/create-and-register-server :heat)]
      (is (not= server1 server2))
      (is (not (ipc/server-running? server1)))
      (is (ipc/server-running? server2))))
  
  (testing "Stop all servers"
    (ipc/create-and-register-server :heat)
    (ipc/create-and-register-server :day)
    
    (is (not (nil? (ipc/get-server :heat))))
    (is (not (nil? (ipc/get-server :day))))
    
    (ipc/stop-all-servers)
    
    (is (nil? (ipc/get-server :heat)))
    (is (nil? (ipc/get-server :day)))))