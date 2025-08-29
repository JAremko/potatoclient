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

(deftest test-convenience-functions
  (testing "toggle-osd with both cameras enabled returns disable commands"
    (let [[day-cmd heat-cmd] (osd/toggle-osd true true)]
      ;; Test day disable command
      (is (m/validate :cmd/root day-cmd))
      (is (= {} (get-in day-cmd [:osd :disable_day_osd])))
      (let [roundtrip-result (validation/validate-roundtrip-with-report day-cmd)]
        (is (:valid? roundtrip-result)))
      ;; Test heat disable command
      (is (m/validate :cmd/root heat-cmd))
      (is (= {} (get-in heat-cmd [:osd :disable_heat_osd])))
      (let [roundtrip-result (validation/validate-roundtrip-with-report heat-cmd)]
        (is (:valid? roundtrip-result)))))
  
  (testing "toggle-osd with only day camera enabled returns disable commands"
    (let [[day-cmd heat-cmd] (osd/toggle-osd true false)]
      ;; Both should be disable commands
      (is (= {} (get-in day-cmd [:osd :disable_day_osd])))
      (is (= {} (get-in heat-cmd [:osd :disable_heat_osd])))))
  
  (testing "toggle-osd with both cameras disabled returns enable commands"
    (let [[day-cmd heat-cmd] (osd/toggle-osd false false)]
      ;; Test day enable command
      (is (m/validate :cmd/root day-cmd))
      (is (= {} (get-in day-cmd [:osd :enable_day_osd])))
      (let [roundtrip-result (validation/validate-roundtrip-with-report day-cmd)]
        (is (:valid? roundtrip-result)))
      ;; Test heat enable command
      (is (m/validate :cmd/root heat-cmd))
      (is (= {} (get-in heat-cmd [:osd :enable_heat_osd])))
      (let [roundtrip-result (validation/validate-roundtrip-with-report heat-cmd)]
        (is (:valid? roundtrip-result)))))
  
  (testing "show-lrf-workflow returns three valid commands"
    (let [[measure-cmd result-cmd default-cmd] (osd/show-lrf-workflow)]
      ;; Test measure screen command
      (is (m/validate :cmd/root measure-cmd))
      (is (= {} (get-in measure-cmd [:osd :show_lrf_measure_screen])))
      (let [roundtrip-result (validation/validate-roundtrip-with-report measure-cmd)]
        (is (:valid? roundtrip-result)))
      ;; Test result screen command
      (is (m/validate :cmd/root result-cmd))
      (is (= {} (get-in result-cmd [:osd :show_lrf_result_screen])))
      (let [roundtrip-result (validation/validate-roundtrip-with-report result-cmd)]
        (is (:valid? roundtrip-result)))
      ;; Test default screen command
      (is (m/validate :cmd/root default-cmd))
      (is (= {} (get-in default-cmd [:osd :show_default_screen])))
      (let [roundtrip-result (validation/validate-roundtrip-with-report default-cmd)]
        (is (:valid? roundtrip-result)))))
  
  (testing "disable-all-osd returns two disable commands"
    (let [[day-cmd heat-cmd] (osd/disable-all-osd)]
      ;; Test day disable command
      (is (m/validate :cmd/root day-cmd))
      (is (= {} (get-in day-cmd [:osd :disable_day_osd])))
      (let [roundtrip-result (validation/validate-roundtrip-with-report day-cmd)]
        (is (:valid? roundtrip-result)))
      ;; Test heat disable command
      (is (m/validate :cmd/root heat-cmd))
      (is (= {} (get-in heat-cmd [:osd :disable_heat_osd])))
      (let [roundtrip-result (validation/validate-roundtrip-with-report heat-cmd)]
        (is (:valid? roundtrip-result)))))
  
  (testing "enable-all-osd returns two enable commands"
    (let [[day-cmd heat-cmd] (osd/enable-all-osd)]
      ;; Test day enable command
      (is (m/validate :cmd/root day-cmd))
      (is (= {} (get-in day-cmd [:osd :enable_day_osd])))
      (let [roundtrip-result (validation/validate-roundtrip-with-report day-cmd)]
        (is (:valid? roundtrip-result)))
      ;; Test heat enable command
      (is (m/validate :cmd/root heat-cmd))
      (is (= {} (get-in heat-cmd [:osd :enable_heat_osd])))
      (let [roundtrip-result (validation/validate-roundtrip-with-report heat-cmd)]
        (is (:valid? roundtrip-result))))))

;; ============================================================================
;; Generative Testing
;; ============================================================================

