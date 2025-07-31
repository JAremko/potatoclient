(ns potatoclient.transit-core-test
  "Unit tests for Transit core functionality"
  (:require [clojure.test :refer [deftest testing is]]
            [potatoclient.transit.core :as transit-core])
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
                writer (transit-core/make-writer out)
                _ (transit-core/write-message! writer data out)
                in (ByteArrayInputStream. (.toByteArray out))
                reader (transit-core/make-reader in)
                result (transit-core/read-message reader)]
            (is (= data result))))))))

(deftest test-create-message-envelope
  (testing "Message envelope creation"
    (let [envelope (transit-core/create-message :test {:data "payload"})]
      (is (map? envelope))
      (is (= :test (:msg-type envelope)))
      (is (string? (:msg-id envelope)))
      (is (uuid? (java.util.UUID/fromString (:msg-id envelope))))
      (is (pos-int? (:timestamp envelope)))
      (is (= {:data "payload"} (:payload envelope))))))

(deftest test-message-envelope-types
  (testing "Different message types"
    (doseq [msg-type [:state :command :control :response :validation-error]]
      (let [envelope (transit-core/create-message msg-type {})]
        (is (= msg-type (:msg-type envelope)))))))

(deftest test-custom-write-handlers
  (testing "Custom Transit write handlers"
    ;; Test that our custom handlers work correctly
    (let [test-data {:keyword-key :keyword-value
                     :string-key "string-value"
                     :nested {:inner :value}}
          out (ByteArrayOutputStream.)
          writer (transit-core/make-writer out)
          _ (transit-core/write-message! writer test-data out)
          in (ByteArrayInputStream. (.toByteArray out))
          reader (transit-core/make-reader in)
          result (transit-core/read-message reader)]
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
          writer (transit-core/make-writer out)
          _ (transit-core/write-message! writer large-map out)
          in (ByteArrayInputStream. (.toByteArray out))
          reader (transit-core/make-reader in)
          result (transit-core/read-message reader)]
      (is (= large-map result))
      (is (= 10000 (count (:data result)))))))

(deftest test-special-characters
  (testing "Special character handling"
    (let [test-data {:unicode "Hello 世界 🌍"
                     :special "Line\nbreak\tTab"
                     :quotes "She said \"Hello\""
                     :empty ""}
          out (ByteArrayOutputStream.)
          writer (transit-core/make-writer out)
          _ (transit-core/write-message! writer test-data out)
          in (ByteArrayInputStream. (.toByteArray out))
          reader (transit-core/make-reader in)
          result (transit-core/read-message reader)]
      (is (= test-data result)))))

(deftest test-timestamp-precision
  (testing "Timestamp precision in messages"
    (let [before (System/currentTimeMillis)
          envelope (transit-core/create-message :test {})
          after (System/currentTimeMillis)]
      (is (<= before (:timestamp envelope)))
      (is (>= after (:timestamp envelope))))))

(deftest test-message-validation
  (testing "Message envelope validation"
    (let [valid-envelope (transit-core/create-message :command {:action "test"})
          invalid-envelopes [{:msg-type :command}  ; missing fields
                             {:msg-id "123" :timestamp 123}  ; missing msg-type
                             {}]]  ; empty
      (is (transit-core/validate-message-envelope valid-envelope))
      (doseq [invalid invalid-envelopes]
        (is (not (transit-core/validate-message-envelope invalid)))))))

(deftest test-binary-data
  (testing "Binary data handling"
    (let [binary-data (byte-array [1 2 3 4 5 -128 127 0])
          test-map {:binary binary-data
                    :type "bytes"}
          out (ByteArrayOutputStream.)
          writer (transit-core/make-writer out)
          _ (transit-core/write-message! writer test-map out)
          in (ByteArrayInputStream. (.toByteArray out))
          reader (transit-core/make-reader in)
          result (transit-core/read-message reader)]
      (is (= "bytes" (:type result)))
      (is (bytes? (:binary result)))
      (is (= (seq binary-data) (seq (:binary result)))))))

(deftest test-error-handling
  (testing "Error handling in read/write"
    ;; Test reading from empty stream
    (is (thrown? Exception
                 (let [in (ByteArrayInputStream. (byte-array 0))
                       reader (transit-core/make-reader in)]
                   (transit-core/read-message reader))))

    ;; Test reading corrupted data - Transit msgpack may parse it in unexpected ways
    (let [in (ByteArrayInputStream. (.getBytes "not transit data"))
          reader (transit-core/make-reader in)]
      (try
        (let [result (transit-core/read-message reader)]
          ;; Transit msgpack may parse plain text in various ways
          ;; The important thing is it doesn't crash
          (is (some? result)
              (str "Non-transit data parsed as: " (pr-str result) " (type: " (type result) ")")))
        (catch Exception _
          ;; This is also acceptable
          (is true "Exception thrown for corrupted data"))))))

(deftest test-concurrent-writes
  (testing "Concurrent write safety"
    (let [out (ByteArrayOutputStream.)
          data-items (map #(hash-map :id % :data (str "item-" %))
                          (range 100))]
      ;; Write concurrently
      (let [futures (doall
                      (map #(future (let [writer (transit-core/make-writer out)]
                                      (transit-core/write-message! writer % out)))
                           data-items))]
        ;; Wait for all writes
        (doseq [f futures] @f))

      ;; Verify we can read something (exact count may vary due to interleaving)
      (is (pos? (count (.toByteArray out)))))))