(ns potatoclient.cmd.lrf-test
  "Tests for LRF (Laser Range Finder) command functions."
  (:require
   [clojure.test :refer [deftest is testing]]
   [matcher-combinators.test] ;; extends clojure.test's `is` macro
   [matcher-combinators.matchers :as matchers]
   [potatoclient.cmd.lrf :as lrf]
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
;; Measurement Operations Tests
;; ============================================================================

(deftest test-measurement-operations
  (testing "measure creates valid command"
    (let [result (lrf/measure)]
      (is (m/validate :cmd/root result))
      (is (match? {:lrf {:measure {}}}
                  result))
      (let [roundtrip-result (validation/validate-roundtrip-with-report result)]
        (is (:valid? roundtrip-result) 
            (str "Should pass roundtrip validation" 
                 (when-not (:valid? roundtrip-result) 
                   (str "\nDiff:\n" (:pretty-diff roundtrip-result)))))))))

;; ============================================================================
;; Scan Operations Tests
;; ============================================================================

(deftest test-scan-operations
  (testing "scan-on creates valid command"
    (let [result (lrf/scan-on)]
      (is (m/validate :cmd/root result))
      (is (match? {:lrf {:scan_on {}}}
                  result))
      (let [roundtrip-result (validation/validate-roundtrip-with-report result)]
        (is (:valid? roundtrip-result)))))
  
  (testing "scan-off creates valid command"
    (let [result (lrf/scan-off)]
      (is (m/validate :cmd/root result))
      (is (match? {:lrf {:scan_off {}}}
                  result))
      (let [roundtrip-result (validation/validate-roundtrip-with-report result)]
        (is (:valid? roundtrip-result)))))
  
  (testing "set-scan-mode creates valid command with 1 Hz continuous mode"
    (let [result (lrf/set-scan-mode :JON_GUI_DATA_LRF_SCAN_MODE_1_HZ_CONTINUOUS)]
      (is (m/validate :cmd/root result))
      (is (match? {:lrf {:set_scan_mode {:mode :JON_GUI_DATA_LRF_SCAN_MODE_1_HZ_CONTINUOUS}}}
                  result))
      (let [roundtrip-result (validation/validate-roundtrip-with-report result)]
        (is (:valid? roundtrip-result)))))
  
  (testing "set-scan-mode creates valid command with 10 Hz continuous mode"
    (let [result (lrf/set-scan-mode :JON_GUI_DATA_LRF_SCAN_MODE_10_HZ_CONTINUOUS)]
      (is (m/validate :cmd/root result))
      (is (match? {:lrf {:set_scan_mode {:mode :JON_GUI_DATA_LRF_SCAN_MODE_10_HZ_CONTINUOUS}}}
                  result))
      (let [roundtrip-result (validation/validate-roundtrip-with-report result)]
        (is (:valid? roundtrip-result)))))
  
  (testing "set-scan-mode creates valid command with 200 Hz continuous mode"
    (let [result (lrf/set-scan-mode :JON_GUI_DATA_LRF_SCAN_MODE_200_HZ_CONTINUOUS)]
      (is (m/validate :cmd/root result))
      (is (= :JON_GUI_DATA_LRF_SCAN_MODE_200_HZ_CONTINUOUS 
             (get-in result [:lrf :set_scan_mode :mode])))
      (let [roundtrip-result (validation/validate-roundtrip-with-report result)]
        (is (:valid? roundtrip-result))))))

;; ============================================================================
;; Device Control Tests
;; ============================================================================

(deftest test-device-control
  (testing "start creates valid command"
    (let [result (lrf/start)]
      (is (m/validate :cmd/root result))
      (is (= {} (get-in result [:lrf :start])))
      (let [roundtrip-result (validation/validate-roundtrip-with-report result)]
        (is (:valid? roundtrip-result)))))
  
  (testing "stop creates valid command"
    (let [result (lrf/stop)]
      (is (m/validate :cmd/root result))
      (is (= {} (get-in result [:lrf :stop])))
      (let [roundtrip-result (validation/validate-roundtrip-with-report result)]
        (is (:valid? roundtrip-result))))))

;; ============================================================================
;; Target Designator Control Tests
;; ============================================================================

(deftest test-target-designator-control
  (testing "target-designator-off creates valid command"
    (let [result (lrf/target-designator-off)]
      (is (m/validate :cmd/root result))
      (is (= {} (get-in result [:lrf :target_designator_off])))
      (let [roundtrip-result (validation/validate-roundtrip-with-report result)]
        (is (:valid? roundtrip-result)))))
  
  (testing "target-designator-on-mode-a creates valid command"
    (let [result (lrf/target-designator-on-mode-a)]
      (is (m/validate :cmd/root result))
      (is (= {} (get-in result [:lrf :target_designator_on_mode_a])))
      (let [roundtrip-result (validation/validate-roundtrip-with-report result)]
        (is (:valid? roundtrip-result)))))
  
  (testing "target-designator-on-mode-b creates valid command"
    (let [result (lrf/target-designator-on-mode-b)]
      (is (m/validate :cmd/root result))
      (is (= {} (get-in result [:lrf :target_designator_on_mode_b])))
      (let [roundtrip-result (validation/validate-roundtrip-with-report result)]
        (is (:valid? roundtrip-result))))))

;; ============================================================================
;; Fog Mode Control Tests
;; ============================================================================

(deftest test-fog-mode-control
  (testing "enable-fog-mode creates valid command"
    (let [result (lrf/enable-fog-mode)]
      (is (m/validate :cmd/root result))
      (is (= {} (get-in result [:lrf :enable_fog_mode])))
      (let [roundtrip-result (validation/validate-roundtrip-with-report result)]
        (is (:valid? roundtrip-result)))))
  
  (testing "disable-fog-mode creates valid command"
    (let [result (lrf/disable-fog-mode)]
      (is (m/validate :cmd/root result))
      (is (= {} (get-in result [:lrf :disable_fog_mode])))
      (let [roundtrip-result (validation/validate-roundtrip-with-report result)]
        (is (:valid? roundtrip-result))))))

;; ============================================================================
;; Session Management Tests
;; ============================================================================

(deftest test-session-management
  (testing "new-session creates valid command"
    (let [result (lrf/new-session)]
      (is (m/validate :cmd/root result))
      (is (= {} (get-in result [:lrf :new_session])))
      (let [roundtrip-result (validation/validate-roundtrip-with-report result)]
        (is (:valid? roundtrip-result))))))

;; ============================================================================
;; Meteo Data Tests
;; ============================================================================

(deftest test-meteo-data
  (testing "get-meteo creates valid command"
    (let [result (lrf/get-meteo)]
      (is (m/validate :cmd/root result))
      (is (= {} (get-in result [:lrf :get_meteo])))
      (let [roundtrip-result (validation/validate-roundtrip-with-report result)]
        (is (:valid? roundtrip-result))))))

;; ============================================================================
;; Refine Mode Control Tests
;; ============================================================================

(deftest test-refine-mode-control
  (testing "refine-on creates valid command"
    (let [result (lrf/refine-on)]
      (is (m/validate :cmd/root result))
      (is (= {} (get-in result [:lrf :refine_on])))
      (let [roundtrip-result (validation/validate-roundtrip-with-report result)]
        (is (:valid? roundtrip-result)))))
  
  (testing "refine-off creates valid command"
    (let [result (lrf/refine-off)]
      (is (m/validate :cmd/root result))
      (is (= {} (get-in result [:lrf :refine_off])))
      (let [roundtrip-result (validation/validate-roundtrip-with-report result)]
        (is (:valid? roundtrip-result))))))

;; ============================================================================
;; Generative Testing
;; ============================================================================

(deftest test-lrf-functions-generative
  (testing "All LRF functions pass generative testing with mi/check"
    ;; Function with parameter - test set-scan-mode
    (is (nil? (mi/check {:filters [(gm/=>)]
                        :num-tests 10}
                       [#'lrf/set-scan-mode])))
    
    ;; Functions without parameters
    (is (nil? (mi/check {:filters [(gm/=>)]
                        :num-tests 5}
                       [#'lrf/measure
                        #'lrf/scan-on
                        #'lrf/scan-off
                        #'lrf/start
                        #'lrf/stop
                        #'lrf/target-designator-off
                        #'lrf/target-designator-on-mode-a
                        #'lrf/target-designator-on-mode-b
                        #'lrf/enable-fog-mode
                        #'lrf/disable-fog-mode
                        #'lrf/new-session
                        #'lrf/get-meteo
                        #'lrf/refine-on
                        #'lrf/refine-off])))))