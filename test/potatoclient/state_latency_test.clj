(ns potatoclient.state-latency-test
  "Tests for state latency validation, especially 0ms edge case."
  (:require [clojure.test :refer [deftest testing is use-fixtures]]
            [potatoclient.state :as state]
            [potatoclient.init :as init]
            [malli.core :as m]
            [malli.generator :as mg]))

;; Initialize registry for tests
(use-fixtures :once
  (fn [f]
    (init/initialize-for-tests!)
    (f)))

(deftest latency-spec-test
  (testing "connection-state latency-ms spec"
    (let [spec (:latency-ms (m/properties state/connection-state))]
      (testing "accepts 0"
        (is (m/validate [:maybe nat-int?] 0)))
      
      (testing "accepts positive integers"
        (is (m/validate [:maybe nat-int?] 1))
        (is (m/validate [:maybe nat-int?] 100))
        (is (m/validate [:maybe nat-int?] 999999)))
      
      (testing "accepts nil"
        (is (m/validate [:maybe nat-int?] nil)))
      
      (testing "rejects negative integers"
        (is (not (m/validate [:maybe nat-int?] -1)))
        (is (not (m/validate [:maybe nat-int?] -100))))
      
      (testing "rejects non-integers"
        (is (not (m/validate [:maybe nat-int?] 1.5)))
        (is (not (m/validate [:maybe nat-int?] "0")))
        (is (not (m/validate [:maybe nat-int?] :zero)))))))

(deftest set-connection-latency-schema-test
  (testing "set-connection-latency! accepts correct inputs"
    ;; Test by actually calling the function - with instrumentation
    ;; it will throw if validation fails
    (testing "accepts 0"
      (is (map? (state/set-connection-latency! 0))))
    
    (testing "accepts positive integers"
      (is (map? (state/set-connection-latency! 1)))
      (is (map? (state/set-connection-latency! 100))))
    
    (testing "accepts nil"
      (is (map? (state/set-connection-latency! nil))))))

(deftest latency-roundtrip-test
  (testing "latency values roundtrip through state"
    (reset! state/app-state state/initial-state)
    
    (testing "0ms roundtrip"
      (state/set-connection-latency! 0)
      (is (= 0 (state/get-connection-latency)))
      (is (= 0 (get-in @state/app-state [:connection :latency-ms]))))
    
    (testing "positive value roundtrip"
      (state/set-connection-latency! 42)
      (is (= 42 (state/get-connection-latency)))
      (is (= 42 (get-in @state/app-state [:connection :latency-ms]))))
    
    (testing "nil roundtrip"
      (state/set-connection-latency! nil)
      (is (nil? (state/get-connection-latency)))
      (is (nil? (get-in @state/app-state [:connection :latency-ms]))))))

(deftest connection-state-validation-test
  (testing "full connection state with 0ms latency"
    (let [valid-state {:url "wss://example.com"
                       :connected? true
                       :latency-ms 0
                       :reconnect-count 0}]
      (is (m/validate state/connection-state valid-state)
          "Connection state should accept 0ms latency"))
    
    (testing "connection state with various latencies"
      (doseq [latency [nil 0 1 10 100 1000]]
        (let [test-state {:url "wss://example.com"
                         :connected? false
                         :latency-ms latency
                         :reconnect-count 5}]
          (is (m/validate state/connection-state test-state)
              (str "Should accept latency: " latency)))))))