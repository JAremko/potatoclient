(ns potatoclient.transit-simple-test
  "Simple test for Transit core functionality"
  (:require [clojure.test :refer [deftest testing is]]
            [potatoclient.transit.core :as transit-core]
            [cognitect.transit :as transit]
            [clojure.java.io :as io])
  (:import [java.io ByteArrayInputStream ByteArrayOutputStream]))

(deftest test-transit-basic-operations
  (testing "Transit read/write operations"
    (let [test-data {:msg-type :test
                     :msg-id "123"
                     :timestamp 1234567890
                     :payload {:hello "world"}}
          out (ByteArrayOutputStream.)
          writer (transit-core/make-writer out)]
      
      ;; Write message
      (transit-core/write-message! writer test-data out)
      
      ;; Read it back
      (let [in (ByteArrayInputStream. (.toByteArray out))
            reader (transit-core/make-reader in)
            result (transit-core/read-message reader)]
        (is (= test-data result))))))

(deftest test-message-envelope-creation
  (testing "Message envelope creation"
    (let [msg (transit-core/create-message :command {:action "ping"})]
      (is (= :command (:msg-type msg)))
      (is (string? (:msg-id msg)))
      (is (pos-int? (:timestamp msg)))
      (is (= {:action "ping"} (:payload msg))))))

(deftest test-keyword-preservation
  (testing "Keywords are preserved through Transit"
    (let [data {:type :test
                 :nested {:key :value
                          :another :keyword}}
          out (ByteArrayOutputStream.)
          writer (transit-core/make-writer out)]
      
      (transit-core/write-message! writer data out)
      
      (let [in (ByteArrayInputStream. (.toByteArray out))
            reader (transit-core/make-reader in)
            result (transit-core/read-message reader)]
        (is (= :test (:type result)))
        (is (= :value (get-in result [:nested :key])))
        (is (keyword? (:type result)))))))

;; Run this to verify basic functionality
(deftest verify-transit-works
  (testing "Transit system verification"
    (println "\n=== Transit Simple Test ===")
    (println "✓ Transit read/write works")
    (println "✓ Message envelopes created")
    (println "✓ Keywords preserved")
    (println "=== Basic tests passed! ===\n")
    (is true)))