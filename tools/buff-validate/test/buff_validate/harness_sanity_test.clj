(ns buff-validate.harness-sanity-test
  "Sanity tests to validate the test harness itself.
   Ensures embedded EDN data is valid and buff-validate detects errors correctly."
  (:require [clojure.test :refer [deftest testing is]]
            [buff-validate.validator :as v]
            [buff-validate.test-harness :as h]
            [pronto.core :as p]))

;; ============================================================================
;; HARNESS DATA VALIDITY
;; ============================================================================

(deftest test-harness-produces-valid-bytes
  (testing "Test harness produces non-empty byte arrays"
    (is (> (count (h/valid-state-bytes)) 100) 
        "State should produce substantial byte array")
    (is (> (count (h/valid-ping-bytes)) 10) 
        "Ping command should produce bytes")
    (is (> (count (h/valid-rotary-azimuth-bytes)) 10)
        "Complex rotary command should produce bytes")))

(deftest test-embedded-edn-creates-valid-proto-maps
  (testing "Embedded EDN converts to valid proto-maps"
    (let [state (h/valid-state)]
      (is (some? state) "State proto-map should be created")
      (is (= 1 (get state :protocol_version)) 
          "Protocol version should be preserved")
      (is (some? (get state :gps)) "GPS should be present")
      (is (some? (get state :system)) "System should be present"))))

(deftest test-real-state-edn-structure
  (testing "Real state EDN has expected structure"
    (let [edn h/real-state-edn]
      (is (map? edn) "Should be a map")
      (is (= 1 (:protocol-version edn)) "Should have protocol version 1")
      (is (number? (get-in edn [:gps :latitude])) "Should have GPS latitude")
      (is (keyword? (get-in edn [:gps :fix-type])) "Should have GPS fix type as keyword")
      (is (= :jon-gui-data-gps-fix-type-3d (get-in edn [:gps :fix-type]))
          "Should have 3D GPS fix"))))

;; ============================================================================
;; BUFF-VALIDATE INTEGRATION
;; ============================================================================

(deftest test-embedded-edn-passes-validation
  (testing "Embedded real state EDN passes buff-validate"
    (let [result (v/validate-binary (h/valid-state-bytes) :type :state)]
      (is (:valid? result) "Real state should be valid")
      (is (empty? (:violations result)) "Should have no violations")
      (is (= :state (:message-type result)) "Should be identified as state"))))

(deftest test-all-valid-commands-pass-validation
  (testing "All command types pass validation"
    (doseq [[name bytes-fn] [["ping" h/valid-ping-bytes]
                              ["noop" h/valid-noop-bytes]
                              ["frozen" h/valid-frozen-bytes]
                              ["rotary-azimuth" h/valid-rotary-azimuth-bytes]
                              ["rotary-stop" h/valid-rotary-stop-bytes]]]
      (testing (str "Command: " name)
        (let [result (v/validate-binary (bytes-fn) :type :cmd)]
          (is (:valid? result) (str name " should be valid"))
          (is (= :cmd (:message-type result)) "Should be cmd type"))))))

;; ============================================================================
;; ERROR DETECTION VALIDATION
;; ============================================================================

(deftest test-detects-gps-out-of-range
  (testing "Buff-validate detects out-of-range GPS coordinates"
    (let [result (v/validate-binary (h/invalid-gps-state-bytes) :type :state)]
      (is (not (:valid? result)) "Should be invalid with bad GPS")
      (is (seq (:violations result)) "Should have violations")
      (let [violations-str (str (:violations result))]
        (is (or (re-find #"latitude" violations-str)
                (re-find #"gps" violations-str))
            "Should mention GPS/latitude in violations")))))

(deftest test-detects-invalid-protocol
  (testing "Buff-validate detects invalid protocol version"
    (testing "State with protocol 0"
      (let [result (v/validate-binary (h/invalid-protocol-state-bytes) :type :state)]
        (is (not (:valid? result)) "Protocol 0 state should be invalid")
        (is (some #(re-find #"protocol" (str %)) (:violations result))
            "Should mention protocol in violations")))
    
    (testing "Command with protocol 0"
      (let [result (v/validate-binary (h/invalid-protocol-cmd-bytes) :type :cmd)]
        (is (not (:valid? result)) "Protocol 0 command should be invalid")))))

(deftest test-detects-invalid-client-type
  (testing "Buff-validate detects UNSPECIFIED client type"
    (let [result (v/validate-binary (h/invalid-client-cmd-bytes) :type :cmd)]
      (is (not (:valid? result)) "UNSPECIFIED client should be invalid")
      (is (some #(re-find #"client" (str %)) (:violations result))
          "Should mention client in violations"))))

(deftest test-detects-missing-fields
  (testing "Buff-validate detects missing required fields"
    (let [result (v/validate-binary (h/missing-fields-state-bytes) :type :state)]
      (is (not (:valid? result)) "State missing fields should be invalid")
      (is (seq (:violations result)) "Should have violations for missing fields"))))

;; ============================================================================
;; PRONTO OPERATIONS SANITY
;; ============================================================================

(deftest test-pronto-modifications-work
  (testing "Pronto p/p-> modifications produce valid proto-maps"
    (let [original (h/valid-state)
          modified (p/with-hints [(p/hint original ser.JonSharedData$JonGUIState h/state-mapper)]
                     (p/p-> original
                            (assoc-in [:gps :altitude] 1000.0)
                            (assoc :protocol_version 2)))]
      (is (= 1000.0 (get-in modified [:gps :altitude]))
          "Altitude should be modified")
      (is (= 2 (get modified :protocol_version))
          "Protocol should be modified")
      ;; Original should be unchanged (immutability)
      (is (not= (get-in original [:gps :altitude]) 1000.0)
          "Original should be unchanged"))))

(deftest test-boundary-values-validate
  (testing "Boundary values are accepted as valid"
    (let [result (v/validate-binary (h/boundary-state-bytes) :type :state)]
      (is (:valid? result) "Boundary values should be valid")
      (is (empty? (:violations result)) "Should have no violations"))))

;; ============================================================================
;; PERFORMANCE SANITY
;; ============================================================================

(deftest test-harness-performance
  (testing "Test harness operations are performant"
    (let [start (System/nanoTime)
          _ (dotimes [_ 1000]
              (h/valid-state-bytes))
          elapsed-ms (/ (- (System/nanoTime) start) 1000000.0)]
      (is (< elapsed-ms 100)
          (str "1000 memoized byte conversions should be fast, took " elapsed-ms "ms")))
    
    (let [start (System/nanoTime)
          state (h/valid-state)
          _ (dotimes [_ 100]
              (p/with-hints [(p/hint state ser.JonSharedData$JonGUIState h/state-mapper)]
                (p/p-> state
                       (assoc-in [:gps :altitude] (rand 1000)))))
          elapsed-ms (/ (- (System/nanoTime) start) 1000000.0)]
      (is (< elapsed-ms 100)
          (str "100 Pronto modifications should be fast, took " elapsed-ms "ms")))))

(comment
  ;; Run just these sanity tests
  (require '[clojure.test :as t])
  (t/run-tests 'buff-validate.harness-sanity-test))