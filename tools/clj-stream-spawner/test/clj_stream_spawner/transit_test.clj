(ns clj-stream-spawner.transit-test
  "Tests for Transit serialization/deserialization."
  (:require 
   [clojure.test :refer [deftest is testing]]
   [matcher-combinators.test]
   [matcher-combinators.matchers :as matchers]
   [clj-stream-spawner.transit :as transit])
  (:import 
   [com.cognitect.transit Keyword]))

(deftest write-read-roundtrip-test
  (testing "Basic message roundtrip"
    (let [message {:msg-type :event
                   :type :gesture
                   :x 100
                   :y 200}
          bytes (transit/write-message message)
          result (transit/read-message bytes)]
      (is (match? message result))))
  
  (testing "Complex nested message"
    (let [message {:msg-type :command
                   :action :rotary-goto-ndc
                   :data {:ndc-x 0.5
                          :ndc-y -0.5
                          :stream-type :heat}
                   :metadata {:timestamp 123456789
                              :sender "test"}}
          bytes (transit/write-message message)
          result (transit/read-message bytes)]
      (is (match? message result))))
  
  (testing "Empty map roundtrip"
    (let [message {}
          bytes (transit/write-message message)
          result (transit/read-message bytes)]
      (is (= message result)))))

(deftest keyword-conversion-test
  (testing "Clojure keyword to Transit Keyword"
    (let [kw :test-keyword
          transit-kw (transit/keyword->transit kw)]
      (is (instance? Keyword transit-kw))
      (is (= ":test-keyword" (.toString transit-kw)))))
  
  (testing "Transit Keyword to Clojure keyword"
    (let [transit-kw (transit/keyword->transit :my-key)
          clj-kw (transit/transit->keyword transit-kw)]
      (is (= :my-key clj-kw)))))

(deftest message-construction-test
  (testing "create-message adds envelope fields"
    (let [message (transit/create-message :test {:foo :bar})]
      (is (match? {:msg-type :test
                   :foo :bar
                   :timestamp number?}
                  message))))
  
  (testing "create-event"
    (let [event (transit/create-event :gesture {:x 10 :y 20})]
      (is (match? {:msg-type :event
                   :type :gesture
                   :x 10
                   :y 20
                   :timestamp number?}
                  event))))
  
  (testing "create-log without data"
    (let [log (transit/create-log :info "Test message")]
      (is (match? {:msg-type :log
                   :level :info
                   :message "Test message"
                   :timestamp number?}
                  log))))
  
  (testing "create-log with data"
    (let [log (transit/create-log :error "Error occurred" {:code 500})]
      (is (match? {:msg-type :log
                   :level :error
                   :message "Error occurred"
                   :data {:code 500}
                   :timestamp number?}
                  log))))
  
  (testing "create-command"
    (let [cmd (transit/create-command :close-request {:stream-type :heat})]
      (is (match? {:msg-type :command
                   :action :close-request
                   :stream-type :heat
                   :timestamp number?}
                  cmd))))
  
  (testing "create-metric"
    (let [metric (transit/create-metric {:fps 30 :bitrate 1000000})]
      (is (match? {:msg-type :metric
                   :fps 30
                   :bitrate 1000000
                   :timestamp number?}
                  metric)))))