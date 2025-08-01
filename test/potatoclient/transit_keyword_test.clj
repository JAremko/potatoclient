(ns potatoclient.transit-keyword-test
  "Tests for Transit keyword handling between Clojure and Kotlin"
  (:require [clojure.test :refer [deftest testing is use-fixtures]]
            [potatoclient.transit.core :as transit-core]
            [potatoclient.transit.commands :as commands]
            [potatoclient.transit.subprocess-launcher :as launcher]
            [potatoclient.transit.app-db :as app-db])
  (:import [java.io ByteArrayInputStream ByteArrayOutputStream]))

;; Test fixture
(defn reset-app-db-fixture
  "Reset app-db before each test"
  [f]
  (reset! app-db/app-db app-db/initial-state)
  (f)
  (reset! app-db/app-db app-db/initial-state))

(use-fixtures :each reset-app-db-fixture)

(deftest test-transit-keyword-encoding
  (testing "Transit messages use keyword keys"
    (let [msg (transit-core/create-message :command {:action "ping"})]
      ;; Message should have keyword keys
      (is (contains? msg :msg-type))
      (is (contains? msg :msg-id))
      (is (contains? msg :timestamp))
      (is (contains? msg :payload))
      (is (= :command (:msg-type msg))))))

(deftest test-transit-keyword-roundtrip
  (testing "Keywords survive Transit encoding/decoding"
    (let [original-msg {:msg-type :command
                        :msg-id "test-123"
                        :timestamp 12345
                        :payload {:action "ping"
                                  :params {:x 0.5 :y -0.5}}}
          ;; Encode to bytes
          encoded (transit-core/encode-to-bytes original-msg)
          ;; Decode back
          decoded (transit-core/decode-from-bytes encoded)]
      ;; All keys should still be keywords
      (is (keyword? (first (keys decoded))))
      (is (= :msg-type (first (keys decoded))))
      (is (= :command (:msg-type decoded)))
      (is (= "test-123" (:msg-id decoded)))
      (is (= 12345 (:timestamp decoded)))
      (is (= "ping" (get-in decoded [:payload :action]))))))

(deftest test-command-messages-use-keywords
  (testing "Command functions create messages with keyword keys"
    (let [ping-msg (transit-core/create-message :command (commands/ping))
          goto-msg (transit-core/create-message :command
                                                (commands/rotary-goto-ndc :heat 0.5 -0.5))]
      ;; Both should have keyword msg-type
      (is (= :command (:msg-type ping-msg)))
      (is (= :command (:msg-type goto-msg)))
      ;; Payload should contain expected data
      (is (= "ping" (get-in ping-msg [:payload :action])))
      (is (= "rotary-goto-ndc" (get-in goto-msg [:payload :action]))))))

(deftest test-transit-writer-reader-preserves-keywords
  (testing "Transit writer/reader preserve keyword types"
    (let [out-stream (ByteArrayOutputStream.)
          writer (transit-core/make-writer out-stream)
          test-msg {:msg-type :command
                    :payload {:action "test"
                              :params {:key1 "value1"
                                       :key2 42}}}]
      ;; Write message
      (transit-core/write-message! writer test-msg out-stream)
      ;; Read it back
      (let [in-stream (ByteArrayInputStream. (.toByteArray out-stream))
            reader (transit-core/make-reader in-stream)
            read-msg (transit-core/read-message reader)]
        ;; Should have keyword keys after reading
        (is (keyword? (first (keys read-msg))))
        (is (= :msg-type (first (filter #(= "msg-type" (name %)) (keys read-msg)))))
        (is (= :command (:msg-type read-msg)))))))

(deftest test-app-db-handlers-expect-keywords
  (testing "App-db handlers work with keyword keys"
    ;; Reset app-db
    (reset! app-db/app-db app-db/initial-state)

    ;; Simulate a state update with keyword keys
    (let [state-update {:system {:battery-level 85
                                 :has-data true}
                        :proto-received true}]
      ;; Use update-server-state! which is what should be called from handle-state-update
      (app-db/update-server-state! state-update)
      ;; Check that state was updated
      (let [state @app-db/app-db]
        (is (= 85 (get-in state [:server-state :system :battery-level])))
        (is (= true (get-in state [:server-state :system :has-data])))))))