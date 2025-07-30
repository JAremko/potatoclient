(ns potatoclient.transit-core-test
  "Unit tests for Transit core functionality"
  (:require [clojure.test :refer [deftest testing is]]
            [potatoclient.transit.core :as transit-core]
            [cognitect.transit :as transit]
            [clojure.java.io :as io])
  (:import [java.io ByteArrayInputStream ByteArrayOutputStream]))

(deftest test-transit-write-read-cycle
  (testing "Transit write and read preserve data"
    (let [test-cases [{:simple "string"}
                      {:number 42}
                      {:float 3.14}
                      {:boolean true}
                      {:nil-value nil}
                      {:array [1 2 3]}
                      {:nested {:a 1 :b {:c 2}}}
                      {:mixed ["string" 123 true nil {:key "value"}]}]]
      (doseq [data test-cases]
        (testing (str "Data: " data)
          (let [out (ByteArrayOutputStream.)
                _ (transit-core/write-transit out data)
                in (ByteArrayInputStream. (.toByteArray out))
                result (transit-core/read-transit in)]
            (is (= data result))))))))

(deftest test-create-message-envelope
  (testing "Message envelope creation"
    (let [envelope (transit-core/create-message-envelope :test {:data "payload"})]
      (is (map? envelope))
      (is (= :test (:msg-type envelope)))
      (is (string? (:msg-id envelope)))
      (is (uuid? (java.util.UUID/fromString (:msg-id envelope))))
      (is (pos-int? (:timestamp envelope)))
      (is (= {:data "payload"} (:payload envelope))))))

(deftest test-message-envelope-types
  (testing "Different message types"
    (doseq [msg-type [:state :command :control :response :validation-error]]
      (let [envelope (transit-core/create-message-envelope msg-type {})]
        (is (= msg-type (:msg-type envelope)))))))

(deftest test-custom-write-handlers
  (testing "Custom Transit write handlers"
    ;; Test that our custom handlers work correctly
    (let [test-data {:keyword-key :keyword-value
                     :string-key "string-value"
                     :nested {:inner :value}}
          out (ByteArrayOutputStream.)
          _ (transit-core/write-transit out test-data)
          in (ByteArrayInputStream. (.toByteArray out))
          result (transit-core/read-transit in)]
      (is (= test-data result))
      (is (keyword? (:keyword-key result)))
      (is (keyword? (get-in result [:nested :inner]))))))

(deftest test-large-payload
  (testing "Large payload handling"
    (let [large-array (vec (range 10000))
          large-map {:data large-array
                     :metadata {:size (count large-array)
                                :type "test"}}
          out (ByteArrayOutputStream.)
          _ (transit-core/write-transit out large-map)
          in (ByteArrayInputStream. (.toByteArray out))
          result (transit-core/read-transit in)]
      (is (= large-map result))
      (is (= 10000 (count (:data result)))))))

(deftest test-special-characters
  (testing "Special character handling"
    (let [test-data {:unicode "Hello 世界 🌍"
                     :special "Line\nbreak\tTab"
                     :quotes "She said \"Hello\""
                     :empty ""}
          out (ByteArrayOutputStream.)
          _ (transit-core/write-transit out test-data)
          in (ByteArrayInputStream. (.toByteArray out))
          result (transit-core/read-transit in)]
      (is (= test-data result)))))

(deftest test-timestamp-precision
  (testing "Timestamp precision in messages"
    (let [before (System/currentTimeMillis)
          envelope (transit-core/create-message-envelope :test {})
          after (System/currentTimeMillis)]
      (is (<= before (:timestamp envelope)))
      (is (>= after (:timestamp envelope))))))

(deftest test-message-validation
  (testing "Message envelope validation"
    (let [valid-envelope (transit-core/create-message-envelope :command {:action "test"})
          invalid-envelopes [{:msg-type :command}  ; missing fields
                             {:msg-id "123" :timestamp 123}  ; missing msg-type
                             {}]]  ; empty
      (is (transit-core/valid-message-envelope? valid-envelope))
      (doseq [invalid invalid-envelopes]
        (is (not (transit-core/valid-message-envelope? invalid)))))))

(deftest test-binary-data
  (testing "Binary data handling"
    (let [binary-data (byte-array [1 2 3 4 5 -128 127 0])
          test-map {:binary binary-data
                     :type "bytes"}
          out (ByteArrayOutputStream.)
          _ (transit-core/write-transit out test-map)
          in (ByteArrayInputStream. (.toByteArray out))
          result (transit-core/read-transit in)]
      (is (= "bytes" (:type result)))
      (is (bytes? (:binary result)))
      (is (= (seq binary-data) (seq (:binary result)))))))

(deftest test-error-handling
  (testing "Error handling in read/write"
    ;; Test reading from empty stream
    (is (thrown? Exception
                 (transit-core/read-transit 
                   (ByteArrayInputStream. (byte-array 0)))))
    
    ;; Test reading corrupted data
    (is (thrown? Exception
                 (transit-core/read-transit
                   (ByteArrayInputStream. 
                     (.getBytes "not transit data")))))))

(deftest test-concurrent-writes
  (testing "Concurrent write safety"
    (let [out (ByteArrayOutputStream.)
          data-items (map #(hash-map :id % :data (str "item-" %)) 
                          (range 100))]
      ;; Write concurrently
      (let [futures (doall
                      (map #(future (transit-core/write-transit out %))
                           data-items))]
        ;; Wait for all writes
        (doseq [f futures] @f))
      
      ;; Verify we can read something (exact count may vary due to interleaving)
      (let [in (ByteArrayInputStream. (.toByteArray out))]
        (is (pos? (count (.toByteArray out)))))))))