(ns buff-validate.harness-test
  "Tests for the test harness data generation."
  (:require [clojure.test :refer [deftest testing is]]
            [buff-validate.test-harness :as h]))

(deftest test-harness-produces-bytes
  (testing "Valid state produces bytes"
    (is (> (count (h/valid-state-bytes)) 100)))
  (testing "Valid ping produces bytes"
    (is (> (count (h/valid-ping-bytes)) 10)))
  (testing "Valid noop produces bytes"
    (is (> (count (h/valid-noop-bytes)) 10)))
  (testing "Valid frozen produces bytes"
    (is (> (count (h/valid-frozen-bytes)) 10))))

(deftest test-edn-structure
  (testing "Real state EDN structure"
    (is (map? h/real-state-edn))
    (is (= 1 (:protocol-version h/real-state-edn)))
    (is (number? (get-in h/real-state-edn [:gps :latitude])))
    (is (= :jon-gui-data-gps-fix-type-3d 
           (get-in h/real-state-edn [:gps :fix-type])))))

(deftest test-proto-map-creation
  (testing "State proto-map creation"
    (let [state (h/valid-state)]
      (is (some? state))
      (is (= 1 (get state :protocol_version)))
      (is (some? (get state :gps)))
      (is (some? (get state :system))))))

(deftest test-invalid-data-generation
  (testing "Invalid GPS state generates"
    (is (some? (h/invalid-gps-state-bytes))))
  (testing "Invalid protocol state generates"
    (is (some? (h/invalid-protocol-state-bytes))))
  (testing "Invalid client command generates"
    (is (some? (h/invalid-client-cmd-bytes))))
  (testing "Missing fields state generates"
    (is (some? (h/missing-fields-state-bytes)))))

(deftest test-memoization
  (testing "Memoized functions return same bytes"
    (let [bytes1 (h/valid-state-bytes)
          bytes2 (h/valid-state-bytes)]
      (is (identical? bytes1 bytes2)))))