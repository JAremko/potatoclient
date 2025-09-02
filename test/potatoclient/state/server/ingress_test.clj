(ns potatoclient.state.server.ingress-test
  "Tests for state ingress functionality"
  (:require [clojure.test :refer :all]
            [potatoclient.state :as state]
            [potatoclient.state.server.throttle :as throttle]))

(deftest test-throttler
  (testing "Throttler limits update frequency"
    (let [call-count (atom 0)
          throttler (throttle/create-throttler
                     {:interval-ms 100
                      :on-drop (fn [_] nil)})
          increment-fn (fn [_] (swap! call-count inc))]
      
      ;; Submit multiple updates rapidly
      ((:submit throttler) increment-fn 1)
      (Thread/sleep 10)
      ((:submit throttler) increment-fn 2)
      (Thread/sleep 10)
      ((:submit throttler) increment-fn 3)
      
      ;; First should execute immediately
      (is (= 1 @call-count) "First call should execute immediately")
      
      ;; Wait for throttle interval
      (Thread/sleep 150)
      
      ;; Only the last queued should have executed
      (is (= 2 @call-count) "Only one additional call should have executed")
      
      ;; Cleanup
      (throttle/shutdown-throttler throttler))))

(deftest test-state-structure
  (testing "App state includes server-state field"
    (is (contains? @state/app-state :server-state))
    (is (nil? (:server-state @state/app-state)))))

(deftest test-server-state-accessors
  (testing "Server state accessor functions"
    (let [original-state (:server-state @state/app-state)]
      (try
        ;; Test get-server-state
        (is (nil? (state/get-server-state)))
        
        ;; Test update-server-state!
        (state/update-server-state! {:test-key :test-value})
        (is (= :test-value (:test-key (state/get-server-state))))
        
        ;; Test get-subsystem-state with proper state structure
        (swap! state/app-state assoc :server-state
               {:system {:cpu-load 42.0}
                :gps {:latitude 50.0}})
        
        (is (= {:cpu-load 42.0} (state/get-subsystem-state :system)))
        (is (= {:latitude 50.0} (state/get-subsystem-state :gps)))
        (is (nil? (state/get-subsystem-state :nonexistent)))
        
        (finally
          ;; Restore original state
          (swap! state/app-state assoc :server-state original-state))))))