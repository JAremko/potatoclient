(ns potatoclient.cmd.osd-test
  "Tests for OSD (On-Screen Display) command functions."
  (:require
   [clojure.test :refer [deftest is testing]]
   [matcher-combinators.test] ;; extends clojure.test's `is` macro
   [matcher-combinators.matchers :as matchers]
   [potatoclient.cmd.osd :as osd]
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
;; Screen Display Commands Tests
;; ============================================================================

(deftest test-screen-display-commands
  (testing "show-default-screen creates valid command"
    (let [result (osd/show-default-screen)]
      (is (m/validate :cmd/root result))
      (is (match? {:osd {:show_default_screen {}}}
                  result))
      (let [roundtrip-result (validation/validate-roundtrip-with-report result)]
        (is (:valid? roundtrip-result) 
            (str "Should pass roundtrip validation" 
                 (when-not (:valid? roundtrip-result) 
                   (str "\nDiff:\n" (:pretty-diff roundtrip-result))))))))
  
  (testing "show-lrf-measure-screen creates valid command"
    (let [result (osd/show-lrf-measure-screen)]
      (is (m/validate :cmd/root result))
      (is (match? {:osd {:show_lrf_measure_screen {}}}
                  result))
      (let [roundtrip-result (validation/validate-roundtrip-with-report result)]
        (is (:valid? roundtrip-result)))))
  
  (testing "show-lrf-result-screen creates valid command"
    (let [result (osd/show-lrf-result-screen)]
      (is (m/validate :cmd/root result))
      (is (match? {:osd {:show_lrf_result_screen {}}}
                  result))
      (let [roundtrip-result (validation/validate-roundtrip-with-report result)]
        (is (:valid? roundtrip-result)))))
  
  (testing "show-lrf-result-simplified-screen creates valid command"
    (let [result (osd/show-lrf-result-simplified-screen)]
      (is (m/validate :cmd/root result))
      (is (match? {:osd {:show_lrf_result_simplified_screen {}}}
                  result))
      (let [roundtrip-result (validation/validate-roundtrip-with-report result)]
        (is (:valid? roundtrip-result))))))

;; ============================================================================
;; Heat Camera OSD Control Tests
;; ============================================================================

(deftest test-heat-camera-osd-control
  (testing "enable-heat-osd creates valid command"
    (let [result (osd/enable-heat-osd)]
      (is (m/validate :cmd/root result))
      (is (match? {:osd {:enable_heat_osd {}}}
                  result))
      (let [roundtrip-result (validation/validate-roundtrip-with-report result)]
        (is (:valid? roundtrip-result)))))
  
  (testing "disable-heat-osd creates valid command"
    (let [result (osd/disable-heat-osd)]
      (is (m/validate :cmd/root result))
      (is (match? {:osd {:disable_heat_osd {}}}
                  result))
      (let [roundtrip-result (validation/validate-roundtrip-with-report result)]
        (is (:valid? roundtrip-result))))))

;; ============================================================================
;; Day Camera OSD Control Tests
;; ============================================================================

(deftest test-day-camera-osd-control
  (testing "enable-day-osd creates valid command"
    (let [result (osd/enable-day-osd)]
      (is (m/validate :cmd/root result))
      (is (= {} (get-in result [:osd :enable_day_osd])))
      (let [roundtrip-result (validation/validate-roundtrip-with-report result)]
        (is (:valid? roundtrip-result)))))
  
  (testing "disable-day-osd creates valid command"
    (let [result (osd/disable-day-osd)]
      (is (m/validate :cmd/root result))
      (is (= {} (get-in result [:osd :disable_day_osd])))
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

