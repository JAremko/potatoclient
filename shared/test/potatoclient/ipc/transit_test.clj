(ns potatoclient.ipc.transit-test
  "Tests for Transit serialization/deserialization."
  (:require
    [clojure.test :refer [deftest is testing]]
    [matcher-combinators.test]
    [matcher-combinators.matchers :as matchers]
    [potatoclient.ipc.transit :as transit])
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

(deftest message-helpers-test
  (testing "Create event message"
    (let [message (transit/create-event :gesture {:x 100 :y 200})]
      (is (= :event (:msg-type message)))
      (is (= :gesture (:type message)))
      (is (= 100 (:x message)))
      (is (number? (:timestamp message)))))

  (testing "Create log message with data"
    (let [message (transit/create-log :info "Test message" {:count 5})]
      (is (= :log (:msg-type message)))
      (is (= :info (:level message)))
      (is (= "Test message" (:message message)))
      (is (= {:count 5} (:data message)))))

  (testing "Create command message"
    (let [message (transit/create-command :close-request {:stream-type :heat})]
      (is (= :command (:msg-type message)))
      (is (= :close-request (:action message)))
      (is (= :heat (:stream-type message)))))

  (testing "Create metric message"
    (let [message (transit/create-metric {:fps 30 :latency 25})]
      (is (= :metric (:msg-type message)))
      (is (= 30 (:fps message)))
      (is (= 25 (:latency message))))))