(ns clj-stream-spawner.coordinator-test
  "Tests for the stream coordinator."
  (:require 
   [clojure.test :refer [deftest is testing use-fixtures]]
   [matcher-combinators.test]
   [matcher-combinators.matchers :as matchers]
   [clj-stream-spawner.coordinator :as coordinator]
   [clj-stream-spawner.ipc :as ipc]
   [clj-stream-spawner.process :as process])
  (:import 
   [java.util.concurrent CountDownLatch]))

;; Test fixtures
(defn cleanup-coordinator
  "Cleanup coordinator state after tests."
  [f]
  (try
    (f)
    (finally
      (coordinator/shutdown))))

(use-fixtures :each cleanup-coordinator)

(deftest initialization-test
  (testing "Coordinator initialization"
    (let [state (coordinator/initialize "test.local" :debug? true)]
      (is (match? {:host "test.local"
                   :debug? true
                   :streams {:heat {:status :stopped
                                   :ipc-server nil
                                   :process nil
                                   :error nil}
                            :day {:status :stopped
                                  :ipc-server nil
                                  :process nil
                                  :error nil}}
                   :shutdown-latch #(instance? CountDownLatch %)}
                  state)))))

(deftest status-monitoring-test
  (testing "Get status of streams"
    (coordinator/initialize "test.local")
    (let [status (coordinator/get-status)]
      (is (match? {:heat {:status :stopped}
                   :day {:status :stopped}}
                  status))))
  
  (testing "Check stream running status"
    (coordinator/initialize "test.local")
    (is (not (coordinator/stream-running? :heat)))
    (is (not (coordinator/stream-running? :day)))
    (is (not (coordinator/all-streams-running?)))))

(deftest send-close-request-test
  (testing "Cannot send close request when stream not running"
    (coordinator/initialize "test.local")
    (is (not (coordinator/send-close-request :heat)))))

(deftest shutdown-test
  (testing "Shutdown releases resources"
    (coordinator/initialize "test.local")
    (coordinator/shutdown)
    
    ;; After shutdown, status should return nil (state is nil)
    (is (nil? (coordinator/get-status)))))

(deftest wait-for-shutdown-test
  (testing "Wait with timeout returns false when not shutdown"
    (coordinator/initialize "test.local")
    (is (not (coordinator/wait-for-shutdown :timeout-seconds 0)))))