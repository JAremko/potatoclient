(ns potatoclient.transit.keyword-conversion-test
  "Tests for automatic Transit keyword conversion"
  (:require [clojure.test :refer [deftest testing is]]
            [potatoclient.transit.keyword-handlers :as kw-handlers]
            [potatoclient.transit.core :as transit-core]))

(deftest test-enum-string-detection
  (testing "Enum string detection"
    (testing "Valid enum strings"
      (is (kw-handlers/enum-string? "command"))
      (is (kw-handlers/enum-string? "state-update"))
      (is (kw-handlers/enum-string? "heat"))
      (is (kw-handlers/enum-string? "tap"))
      (is (kw-handlers/enum-string? "doubletap"))
      (is (kw-handlers/enum-string? "up")))
    
    (testing "Invalid enum strings"
      (is (not (kw-handlers/enum-string? "Hello World")))
      (is (not (kw-handlers/enum-string? "UPPERCASE")))
      (is (not (kw-handlers/enum-string? "123")))
      (is (not (kw-handlers/enum-string? "with spaces")))
      (is (not (kw-handlers/enum-string? "unknown-value"))))))

(deftest test-convert-enums-to-keywords
  (testing "Convert enums to keywords in data structures"
    (testing "Simple string conversion"
      (is (= :command (kw-handlers/convert-enums-to-keywords "command")))
      (is (= :heat (kw-handlers/convert-enums-to-keywords "heat")))
      (is (= "Hello World" (kw-handlers/convert-enums-to-keywords "Hello World"))))
    
    (testing "Map conversion"
      (let [input {"msg-type" "command"
                   "action" "rotary-goto-ndc"
                   "stream" "heat"
                   "message" "Hello World"}
            expected {"msg-type" :command
                      "action" "rotary-goto-ndc"
                      "stream" :heat
                      "message" "Hello World"}]
        (is (= expected (kw-handlers/convert-enums-to-keywords input)))))
    
    (testing "Nested map conversion"
      (let [input {"type" "event"
                   "payload" {"gesture-type" "tap"
                             "stream-type" "day"
                             "direction" "left"}}
            expected {"type" :event
                      "payload" {"gesture-type" :tap
                               "stream-type" :day  
                               "direction" :left}}]
        (is (= expected (kw-handlers/convert-enums-to-keywords input)))))
    
    (testing "Vector conversion"
      (let [input ["command" "heat" "Hello"]
            expected [:command :heat "Hello"]]
        (is (= expected (kw-handlers/convert-enums-to-keywords input)))))))

(deftest test-transit-integration
  (testing "Transit read/write with automatic keyword conversion"
    (testing "Message envelope conversion"
      (let [test-msg {:msg-type :command
                      :msg-id "123"
                      :timestamp 1234567890
                      :payload {:action :rotary-goto-ndc
                               :params {:channel :heat
                                       :x 0.5
                                       :y -0.5}}}
            ;; Encode and decode through Transit
            encoded (transit-core/encode-to-bytes test-msg)
            decoded (transit-core/decode-from-bytes encoded)]
        ;; All enum values should be keywords
        (is (= :command (:msg-type decoded)))
        (is (= :rotary-goto-ndc (get-in decoded [:payload :action])))
        (is (= :heat (get-in decoded [:payload :params :channel])))))
    
    (testing "Gesture event conversion"
      (let [gesture-event {:msg-type :event
                          :msg-id "456"
                          :timestamp 1234567890
                          :payload {:type :gesture
                                   :gesture-type :doubletap
                                   :stream-type :day
                                   :x 100
                                   :y 200}}
            encoded (transit-core/encode-to-bytes gesture-event)
            decoded (transit-core/decode-from-bytes encoded)]
        (is (= :event (:msg-type decoded)))
        (is (= :gesture (get-in decoded [:payload :type])))
        (is (= :doubletap (get-in decoded [:payload :gesture-type])))
        (is (= :day (get-in decoded [:payload :stream-type])))))))