(ns validate.harness-test
  "Tests for the test harness data generation."
  (:require [clojure.test :refer [deftest testing is]]
            [validate.test-harness :as h]))

(deftest test-harness-produces-bytes
  (testing "Valid state produces bytes"
    (is (> (count (h/valid-state-bytes)) 100)))
  (testing "Valid ping produces bytes"
    (is (>= (count (h/valid-ping-bytes)) 10)))
  (testing "Valid noop produces bytes"
    (is (>= (count (h/valid-noop-bytes)) 10)))
  (testing "Valid frozen produces bytes"
    (is (>= (count (h/valid-frozen-bytes)) 10)))
  (testing "Valid rotary azimuth produces bytes"
    (is (>= (count (h/valid-rotary-azimuth-bytes)) 10)))
  (testing "Valid rotary stop produces bytes"
    (is (>= (count (h/valid-rotary-stop-bytes)) 10)))
  (testing "Valid rotary scan produces bytes"
    (is (>= (count (h/valid-rotary-scan-bytes)) 10))))

(deftest test-edn-structure
  (testing "Real state EDN structure"
    (is (map? h/real-state-edn))
    (is (= 1 (:protocol_version h/real-state-edn)))
    (is (number? (get-in h/real-state-edn [:gps :latitude])))
    (is (= :JON_GUI_DATA_GPS_FIX_TYPE_3D 
           (get-in h/real-state-edn [:gps :fix_type])))))

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