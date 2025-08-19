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

(deftest test-convenience-functions
  (testing "enable-heater returns two valid commands"
    (let [[start-cmd turn-on-cmd] (heater/enable-heater)]
      ;; Test start command
      (is (m/validate :cmd/root start-cmd))
      (is (= {} (get-in start-cmd [:day_cam_glass_heater :start])))
      (let [roundtrip-result (validation/validate-roundtrip-with-report start-cmd)]
        (is (:valid? roundtrip-result)))
      ;; Test turn-on command
      (is (m/validate :cmd/root turn-on-cmd))
      (is (= {} (get-in turn-on-cmd [:day_cam_glass_heater :turn_on])))
      (let [roundtrip-result (validation/validate-roundtrip-with-report turn-on-cmd)]
        (is (:valid? roundtrip-result)))))
  
  (testing "disable-heater returns two valid commands"
    (let [[turn-off-cmd stop-cmd] (heater/disable-heater)]
      ;; Test turn-off command
      (is (m/validate :cmd/root turn-off-cmd))
      (is (= {} (get-in turn-off-cmd [:day_cam_glass_heater :turn_off])))
      (let [roundtrip-result (validation/validate-roundtrip-with-report turn-off-cmd)]
        (is (:valid? roundtrip-result)))
      ;; Test stop command
      (is (m/validate :cmd/root stop-cmd))
      (is (= {} (get-in stop-cmd [:day_cam_glass_heater :stop])))
      (let [roundtrip-result (validation/validate-roundtrip-with-report stop-cmd)]
        (is (:valid? roundtrip-result)))))
  
  (testing "cycle-heater returns two valid commands"
    (let [[off-cmd on-cmd] (heater/cycle-heater)]
      ;; Test turn-off command
      (is (m/validate :cmd/root off-cmd))
      (is (= {} (get-in off-cmd [:day_cam_glass_heater :turn_off])))
      (let [roundtrip-result (validation/validate-roundtrip-with-report off-cmd)]
        (is (:valid? roundtrip-result)))
      ;; Test turn-on command
      (is (m/validate :cmd/root on-cmd))
      (is (= {} (get-in on-cmd [:day_cam_glass_heater :turn_on])))
      (let [roundtrip-result (validation/validate-roundtrip-with-report on-cmd)]
        (is (:valid? roundtrip-result))))))

;; ============================================================================
;; Generative Testing
;; ============================================================================

(deftest test-heater-functions-generative
  (testing "All heater functions pass generative testing with mi/check"
    ;; All functions without parameters
    (is (nil? (mi/check {:filters [(gm/=>)]
                        :num-tests 5}
                       [#'heater/start
                        #'heater/stop
                        #'heater/turn-on
                        #'heater/turn-off
                        #'heater/get-meteo
                        #'heater/enable-heater
                        #'heater/disable-heater
                        #'heater/cycle-heater])))))