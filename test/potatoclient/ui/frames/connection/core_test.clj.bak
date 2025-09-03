(ns potatoclient.ui.frames.connection.core-test
  "Tests for connection frame functionality, especially ping handling."
  (:require [clojure.test :refer [deftest testing is use-fixtures]]
            [potatoclient.ui.frames.connection.core :as conn]
            [potatoclient.state :as state]
            [potatoclient.init :as init]))

;; Initialize registry for tests
(use-fixtures :once
  (fn [f]
    (init/initialize-for-tests!)
    (f)))

(deftest ping-host-test
  (testing "ping-host function with localhost (offline-safe tests)"
    (testing "localhost should be pingable"
      (let [result (#'conn/ping-host "localhost" 1000)]
        (is (some? result) "Localhost should be reachable")
        (is (nat-int? result) "Ping result should be a natural integer")
        (is (>= result 0) "Ping result should be >= 0")))
    
    (testing "127.0.0.1 should be pingable with very low latency"
      (let [result (#'conn/ping-host "127.0.0.1" 1000)]
        (is (some? result) "127.0.0.1 should be reachable")
        (is (nat-int? result) "Ping result should be a natural integer")
        (is (>= result 0) "Ping result should be >= 0")
        ;; On local loopback, we often get 0ms
        (is (<= result 10) "Loopback ping should be very fast (<=10ms)")))
    
    (testing "::1 (IPv6 localhost) may or may not work depending on system"
      (let [result (#'conn/ping-host "::1" 500)]
        (if result
          (do
            (is (nat-int? result) "If reachable, should return natural integer")
            (is (<= result 10) "IPv6 loopback should also be fast"))
          (is (nil? result) "IPv6 localhost may not be available"))))
    
    (testing "invalid/nonsense host should return nil"
      ;; Using clearly invalid hostnames that won't resolve
      (is (nil? (#'conn/ping-host "!!!invalid!!!" 200)))
      (is (nil? (#'conn/ping-host "not.a.real.host.test" 200)))
      (is (nil? (#'conn/ping-host "256.256.256.256" 200))))) ; Invalid IP

(deftest handle-successful-connection-test
  (testing "handle-successful-connection! with various latencies"
    (testing "0ms latency should be accepted"
      (reset! state/app-state state/initial-state)
      (let [callback-called (atom nil)
            callback (fn [status] (reset! callback-called status))]
        ;; Call the private function with 0ms latency
        (#'conn/handle-successful-connection! 
          "localhost" 0 nil nil nil callback)
        
        ;; Verify state was updated correctly
        (is (= 0 (state/get-connection-latency)))
        (is (true? (state/connected?)))
        
        ;; Wait a bit for the callback (it runs after a 500ms sleep)
        (Thread/sleep 600)
        (is (= :connected @callback-called))))
    
    (testing "positive latency should work"
      (reset! state/app-state state/initial-state)
      (let [callback-called (atom nil)
            callback (fn [status] (reset! callback-called status))]
        (#'conn/handle-successful-connection! 
          "localhost" 42 nil nil nil callback)
        
        (is (= 42 (state/get-connection-latency)))
        (is (true? (state/connected?)))
        
        (Thread/sleep 600)
        (is (= :connected @callback-called))))))

(deftest connection-state-validation-test
  (testing "connection state accepts 0ms latency"
    (reset! state/app-state state/initial-state)
    
    (testing "set-connection-latency! accepts 0"
      (is (map? (state/set-connection-latency! 0)))
      (is (= 0 (state/get-connection-latency))))
    
    (testing "set-connection-latency! accepts positive integers"
      (is (map? (state/set-connection-latency! 100)))
      (is (= 100 (state/get-connection-latency))))
    
    (testing "set-connection-latency! accepts nil"
      (is (map? (state/set-connection-latency! nil)))
      (is (nil? (state/get-connection-latency))))))

(deftest ping-monitoring-error-handling-test
  (testing "ping monitor should handle exceptions gracefully"
    ;; This is harder to test directly since it's inside a future
    ;; but we can at least verify the ping-host function is robust
    (testing "ping-host handles various edge cases"
      ;; Empty string actually resolves to localhost in Java
      (let [empty-result (#'conn/ping-host "" 1000)]
        (is (or (nil? empty-result) (nat-int? empty-result))
            "Empty string should either fail or resolve to localhost"))
      
      ;; Test with clearly invalid hostname
      (is (nil? (#'conn/ping-host "!!!invalid-host-name!!!" 1000))
          "Invalid hostname should return nil")))))