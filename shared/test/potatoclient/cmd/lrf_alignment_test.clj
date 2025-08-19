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
   [com.fulcrologic.guardrails.malli.core :as gm]
   [potatoclient.test-harness :as harness]))

;; Ensure test harness is initialized
(when-not harness/initialized?
  (throw (ex-info "Test harness failed to initialize!" 
                  {:initialized? harness/initialized?})))

;; ============================================================================
;; Enable instrumentation for generative testing
;; ============================================================================

(defn test-ns-hook
  "Enable Guardrails checking for this namespace"
  []
  (gm/=> true))

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

(deftest test-convenience-functions
  (testing "calibrate-day-camera returns two valid commands"
    (let [[set-cmd save-cmd] (lrf-align/calibrate-day-camera 640 480)]
      ;; Test set command
      (is (m/validate :cmd/root set-cmd))
      (is (= 640 (get-in set-cmd [:lrf_calib :day :set :x])))
      (is (= 480 (get-in set-cmd [:lrf_calib :day :set :y])))
      (let [roundtrip-result (validation/validate-roundtrip-with-report set-cmd)]
        (is (:valid? roundtrip-result)))
      ;; Test save command
      (is (m/validate :cmd/root save-cmd))
      (is (= {} (get-in save-cmd [:lrf_calib :day :save])))
      (let [roundtrip-result (validation/validate-roundtrip-with-report save-cmd)]
        (is (:valid? roundtrip-result)))))
  
  (testing "calibrate-heat-camera returns two valid commands"
    (let [[set-cmd save-cmd] (lrf-align/calibrate-heat-camera -320 -240)]
      ;; Test set command
      (is (m/validate :cmd/root set-cmd))
      (is (= -320 (get-in set-cmd [:lrf_calib :heat :set :x])))
      (is (= -240 (get-in set-cmd [:lrf_calib :heat :set :y])))
      (let [roundtrip-result (validation/validate-roundtrip-with-report set-cmd)]
        (is (:valid? roundtrip-result)))
      ;; Test save command
      (is (m/validate :cmd/root save-cmd))
      (is (= {} (get-in save-cmd [:lrf_calib :heat :save])))
      (let [roundtrip-result (validation/validate-roundtrip-with-report save-cmd)]
        (is (:valid? roundtrip-result))))))

;; ============================================================================
;; Generative Testing
;; ============================================================================

(deftest test-lrf-alignment-functions-generative
  (testing "All LRF alignment functions pass generative testing with mi/check"
    ;; Functions with parameters
    (is (nil? (mi/check {:filters [(gm/=>)]
                        :num-tests 10}
                       [#'lrf-align/set-day-offsets
                        #'lrf-align/shift-day-offsets
                        #'lrf-align/set-heat-offsets
                        #'lrf-align/shift-heat-offsets
                        #'lrf-align/calibrate-day-camera
                        #'lrf-align/calibrate-heat-camera])))
    
    ;; Functions without parameters
    (is (nil? (mi/check {:filters [(gm/=>)]
                        :num-tests 5}
                       [#'lrf-align/save-day-offsets
                        #'lrf-align/reset-day-offsets
                        #'lrf-align/save-heat-offsets
                        #'lrf-align/reset-heat-offsets])))))