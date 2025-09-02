(ns potatoclient.cmd.lrf-alignment-test
  "Tests for LRF Alignment/Calibration command functions."
  (:require
    [clojure.test :refer [deftest is testing]]
    [matcher-combinators.test] ;; extends clojure.test's `is` macro
    [matcher-combinators.matchers :as matchers]
    [potatoclient.cmd.lrf-alignment :as lrf-align]
    [potatoclient.cmd.validation :as validation]
    [malli.core :as m]
    [malli.instrument :as mi]
    [potatoclient.test-harness :as harness]))

;; Ensure test harness is initialized
(when-not harness/initialized?
  (throw (ex-info "Test harness failed to initialize!"
                  {:initialized? harness/initialized?})))

;; ============================================================================

;; ============================================================================
;; Day Camera Offset Tests
;; ============================================================================

(deftest test-day-camera-offsets
  (testing "set-day-offsets creates valid command"
    (let [result (lrf-align/set-day-offsets 100 -50)]
      (is (m/validate :cmd/root result))
      (is (match? {:lrf_calib {:day {:set {:x 100
                                           :y -50}}}}
                  result))
      (let [roundtrip-result (validation/validate-roundtrip-with-report result)]
        (is (:valid? roundtrip-result)
            (str "Should pass roundtrip validation"
                 (when-not (:valid? roundtrip-result)
                   (str "\nDiff:\n" (:pretty-diff roundtrip-result))))))))

  (testing "shift-day-offsets creates valid command"
    (let [result (lrf-align/shift-day-offsets -200 150)]
      (is (m/validate :cmd/root result))
      (is (match? {:lrf_calib {:day {:shift {:x -200
                                             :y 150}}}}
                  result))
      (let [roundtrip-result (validation/validate-roundtrip-with-report result)]
        (is (:valid? roundtrip-result)))))

  (testing "save-day-offsets creates valid command"
    (let [result (lrf-align/save-day-offsets)]
      (is (m/validate :cmd/root result))
      (is (match? {:lrf_calib {:day {:save {}}}}
                  result))
      (let [roundtrip-result (validation/validate-roundtrip-with-report result)]
        (is (:valid? roundtrip-result)))))

  (testing "reset-day-offsets creates valid command"
    (let [result (lrf-align/reset-day-offsets)]
      (is (m/validate :cmd/root result))
      (is (match? {:lrf_calib {:day {:reset {}}}}
                  result))
      (let [roundtrip-result (validation/validate-roundtrip-with-report result)]
        (is (:valid? roundtrip-result))))))

;; ============================================================================
;; Heat Camera Offset Tests
;; ============================================================================

(deftest test-heat-camera-offsets
  (testing "set-heat-offsets creates valid command"
    (let [result (lrf-align/set-heat-offsets -300 250)]
      (is (m/validate :cmd/root result))
      (is (match? {:lrf_calib {:heat {:set {:x -300
                                            :y 250}}}}
                  result))
      (let [roundtrip-result (validation/validate-roundtrip-with-report result)]
        (is (:valid? roundtrip-result)))))

  (testing "shift-heat-offsets creates valid command"
    (let [result (lrf-align/shift-heat-offsets 50 -75)]
      (is (m/validate :cmd/root result))
      (is (= 50 (get-in result [:lrf_calib :heat :shift :x])))
      (is (= -75 (get-in result [:lrf_calib :heat :shift :y])))
      (let [roundtrip-result (validation/validate-roundtrip-with-report result)]
        (is (:valid? roundtrip-result)))))

  (testing "save-heat-offsets creates valid command"
    (let [result (lrf-align/save-heat-offsets)]
      (is (m/validate :cmd/root result))
      (is (= {} (get-in result [:lrf_calib :heat :save])))
      (let [roundtrip-result (validation/validate-roundtrip-with-report result)]
        (is (:valid? roundtrip-result)))))

  (testing "reset-heat-offsets creates valid command"
    (let [result (lrf-align/reset-heat-offsets)]
      (is (m/validate :cmd/root result))
      (is (= {} (get-in result [:lrf_calib :heat :reset])))
      (let [roundtrip-result (validation/validate-roundtrip-with-report result)]
        (is (:valid? roundtrip-result))))))

;; ============================================================================
;; Edge Cases Tests
;; ============================================================================

(deftest test-edge-cases
  (testing "set-day-offsets with maximum values"
    (let [result (lrf-align/set-day-offsets 1920 1080)]
      (is (m/validate :cmd/root result))
      (is (= 1920 (get-in result [:lrf_calib :day :set :x])))
      (is (= 1080 (get-in result [:lrf_calib :day :set :y])))
      (let [roundtrip-result (validation/validate-roundtrip-with-report result)]
        (is (:valid? roundtrip-result)))))

  (testing "set-heat-offsets with minimum values"
    (let [result (lrf-align/set-heat-offsets -1920 -1080)]
      (is (m/validate :cmd/root result))
      (is (= -1920 (get-in result [:lrf_calib :heat :set :x])))
      (is (= -1080 (get-in result [:lrf_calib :heat :set :y])))
      (let [roundtrip-result (validation/validate-roundtrip-with-report result)]
        (is (:valid? roundtrip-result)))))

  (testing "shift-day-offsets with zero values"
    (let [result (lrf-align/shift-day-offsets 0 0)]
      (is (m/validate :cmd/root result))
      (is (= 0 (get-in result [:lrf_calib :day :shift :x])))
      (is (= 0 (get-in result [:lrf_calib :day :shift :y])))
      (let [roundtrip-result (validation/validate-roundtrip-with-report result)]
        (is (:valid? roundtrip-result))))))

;; ============================================================================
;; Convenience Function Tests
;; ============================================================================

;; Removed test-convenience-functions since those functions were removed
;; Each cmd constructor should return a single valid cmd/root

;; ============================================================================
;; Generative Testing
;; ============================================================================

