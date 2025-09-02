(ns potatoclient.transit-simple-test
  "Simple test for transit serialization to debug the issue."
  (:require
    [clojure.test :refer [deftest is testing]]
    [potatoclient.ipc.transit :as transit]
    [potatoclient.test-harness :as harness]))

;; Ensure test harness is initialized
(when-not harness/initialized?
  (throw (ex-info "Test harness failed to initialize!"
                  {:initialized? harness/initialized?})))

(deftest basic-transit-test
  (testing "Basic transit roundtrip"
    (testing "Simple values"
      (is (= 42 (-> 42 transit/write-message transit/read-message)))
      (is (= "hello" (-> "hello" transit/write-message transit/read-message)))
      (is (= :keyword (-> :keyword transit/write-message transit/read-message)))
      (is (= true (-> true transit/write-message transit/read-message)))
      (is (= nil (-> nil transit/write-message transit/read-message))))
    
    (testing "Collections"
      (is (= [1 2 3] (-> [1 2 3] transit/write-message transit/read-message)))
      (is (= {:a 1 :b 2} (-> {:a 1 :b 2} transit/write-message transit/read-message)))
      (is (= #{1 2 3} (-> #{1 2 3} transit/write-message transit/read-message))))
    
    (testing "Nested structures"
      (let [data {:type :event
                  :id 123
                  :data {:foo "bar"
                         :baz [1 2 3]}}]
        (is (= data (-> data transit/write-message transit/read-message)))))
    
    (testing "Empty collections"
      (is (= [] (-> [] transit/write-message transit/read-message)))
      (is (= {} (-> {} transit/write-message transit/read-message)))
      (is (= #{} (-> #{} transit/write-message transit/read-message))))))

(deftest problematic-types-test
  (testing "Types that might cause issues"
    (testing "Ratios"
      (is (= 1/2 (-> 1/2 transit/write-message transit/read-message))))
    
    (testing "BigInt and BigDecimal"
      (is (= 123N (-> 123N transit/write-message transit/read-message)))
      (is (= 123.456M (-> 123.456M transit/write-message transit/read-message))))
    
    (testing "Characters"
      (is (= \a (-> \a transit/write-message transit/read-message))))
    
    (testing "Symbols"
      (is (= 'symbol (-> 'symbol transit/write-message transit/read-message))))))