(ns potatoclient.cmd.cam-day-glass-heater-test
  "Tests for Day Camera Glass Heater command functions."
  (:require
   [clojure.test :refer [deftest is testing]]
   [matcher-combinators.test] ;; extends clojure.test's `is` macro
   [matcher-combinators.matchers :as matchers]
   [potatoclient.cmd.cam-day-glass-heater :as heater]
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
;; Device Control Tests
;; ============================================================================

(deftest test-device-control
  (testing "start creates valid command"
    (let [result (heater/start)]
      (is (m/validate :cmd/root result))
      (is (match? {:day_cam_glass_heater {:start {}}}
                  result))
      (let [roundtrip-result (validation/validate-roundtrip-with-report result)]
        (is (:valid? roundtrip-result) 
            (str "Should pass roundtrip validation" 
                 (when-not (:valid? roundtrip-result) 
                   (str "\nDiff:\n" (:pretty-diff roundtrip-result))))))))
  
  (testing "stop creates valid command"
    (let [result (heater/stop)]
      (is (m/validate :cmd/root result))
      (is (match? {:day_cam_glass_heater {:stop {}}}
                  result))
      (let [roundtrip-result (validation/validate-roundtrip-with-report result)]
        (is (:valid? roundtrip-result))))))

;; ============================================================================
;; Heater Control Tests
;; ============================================================================

(deftest test-heater-control
  (testing "turn-on creates valid command"
    (let [result (heater/turn-on)]
      (is (m/validate :cmd/root result))
      (is (match? {:day_cam_glass_heater {:turn_on {}}}
                  result))
      (let [roundtrip-result (validation/validate-roundtrip-with-report result)]
        (is (:valid? roundtrip-result)))))
  
  (testing "turn-off creates valid command"
    (let [result (heater/turn-off)]
      (is (m/validate :cmd/root result))
      (is (match? {:day_cam_glass_heater {:turn_off {}}}
                  result))
      (let [roundtrip-result (validation/validate-roundtrip-with-report result)]
        (is (:valid? roundtrip-result))))))

;; ============================================================================
;; Meteo Data Tests
;; ============================================================================

(deftest test-meteo-data
  (testing "get-meteo creates valid command"
    (let [result (heater/get-meteo)]
      (is (m/validate :cmd/root result))
      (is (match? {:day_cam_glass_heater {:get_meteo {}}}
                  result))
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

