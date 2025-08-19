(ns potatoclient.cmd.lrf-generative-test
  "Generative tests for LRF (Laser Range Finder) command functions.
   Uses the actual specs from common.clj to generate test data."
  (:require
   [clojure.test :refer [deftest is testing]]
   [matcher-combinators.test] ;; extends clojure.test's `is` macro
   [matcher-combinators.matchers :as matchers]
   [clojure.test.check :as tc]
   [clojure.test.check.clojure-test :refer [defspec]]
   [clojure.test.check.generators :as gen]
   [clojure.test.check.properties :as prop]
   [potatoclient.cmd.lrf :as lrf]
   [potatoclient.cmd.validation :as v]
   [potatoclient.specs.common :as common]
   [potatoclient.test-harness :as harness]
   [malli.core :as m]
   [malli.generator :as mg]))

;; Ensure test harness is initialized
(when-not harness/initialized?
  (throw (ex-info "Test harness failed to initialize!" 
                  {:initialized? harness/initialized?})))

;; ============================================================================
;; Configuration
;; ============================================================================

(def num-tests 
  "Number of test cases to generate for each property"
  100)

;; ============================================================================
;; Generators from specs
;; ============================================================================

(def lrf-scan-mode-gen
  "Generate valid LRF scan mode enum values"
  (mg/generator common/lrf-scan-modes-enum-spec))

;; ============================================================================
;; Measurement Operations Tests
;; ============================================================================

(defspec measure-command-always-valid num-tests
  (prop/for-all [_ gen/any]
    (let [cmd (lrf/measure)
          result (v/validate-roundtrip-with-report cmd)]
      (and (= {} (get-in cmd [:lrf :measure]))
           (:valid? result)))))

;; ============================================================================
;; Scan Operations Tests
;; ============================================================================

(defspec scan-on-command-always-valid num-tests
  (prop/for-all [_ gen/any]
    (let [cmd (lrf/scan-on)
          result (v/validate-roundtrip-with-report cmd)]
      (and (= {} (get-in cmd [:lrf :scan_on]))
           (:valid? result)))))

(defspec scan-off-command-always-valid num-tests
  (prop/for-all [_ gen/any]
    (let [cmd (lrf/scan-off)
          result (v/validate-roundtrip-with-report cmd)]
      (and (= {} (get-in cmd [:lrf :scan_off]))
           (:valid? result)))))

(defspec set-scan-mode-with-valid-enum num-tests
  (prop/for-all [mode lrf-scan-mode-gen]
    (let [cmd (lrf/set-scan-mode mode)
          result (v/validate-roundtrip-with-report cmd)]
      (and (= mode (get-in cmd [:lrf :set_scan_mode :mode]))
           (:valid? result)))))

;; ============================================================================
;; Device Control Tests
;; ============================================================================

(defspec start-command-always-valid num-tests
  (prop/for-all [_ gen/any]
    (let [cmd (lrf/start)
          result (v/validate-roundtrip-with-report cmd)]
      (and (= {} (get-in cmd [:lrf :start]))
           (:valid? result)))))

(defspec stop-command-always-valid num-tests
  (prop/for-all [_ gen/any]
    (let [cmd (lrf/stop)
          result (v/validate-roundtrip-with-report cmd)]
      (and (= {} (get-in cmd [:lrf :stop]))
           (:valid? result)))))

;; ============================================================================
;; Target Designator Control Tests
;; ============================================================================

(defspec target-designator-off-command-always-valid num-tests
  (prop/for-all [_ gen/any]
    (let [cmd (lrf/target-designator-off)
          result (v/validate-roundtrip-with-report cmd)]
      (and (= {} (get-in cmd [:lrf :target_designator_off]))
           (:valid? result)))))

(defspec target-designator-on-mode-a-command-always-valid num-tests
  (prop/for-all [_ gen/any]
    (let [cmd (lrf/target-designator-on-mode-a)
          result (v/validate-roundtrip-with-report cmd)]
      (and (= {} (get-in cmd [:lrf :target_designator_on_mode_a]))
           (:valid? result)))))

(defspec target-designator-on-mode-b-command-always-valid num-tests
  (prop/for-all [_ gen/any]
    (let [cmd (lrf/target-designator-on-mode-b)
          result (v/validate-roundtrip-with-report cmd)]
      (and (= {} (get-in cmd [:lrf :target_designator_on_mode_b]))
           (:valid? result)))))

;; ============================================================================
;; Fog Mode Control Tests
;; ============================================================================

(defspec enable-fog-mode-command-always-valid num-tests
  (prop/for-all [_ gen/any]
    (let [cmd (lrf/enable-fog-mode)
          result (v/validate-roundtrip-with-report cmd)]
      (and (= {} (get-in cmd [:lrf :enable_fog_mode]))
           (:valid? result)))))

(defspec disable-fog-mode-command-always-valid num-tests
  (prop/for-all [_ gen/any]
    (let [cmd (lrf/disable-fog-mode)
          result (v/validate-roundtrip-with-report cmd)]
      (and (= {} (get-in cmd [:lrf :disable_fog_mode]))
           (:valid? result)))))

;; ============================================================================
;; Session Management Tests
;; ============================================================================

(defspec new-session-command-always-valid num-tests
  (prop/for-all [_ gen/any]
    (let [cmd (lrf/new-session)
          result (v/validate-roundtrip-with-report cmd)]
      (and (= {} (get-in cmd [:lrf :new_session]))
           (:valid? result)))))

;; ============================================================================
;; Meteo Data Tests
;; ============================================================================

(defspec get-meteo-command-always-valid num-tests
  (prop/for-all [_ gen/any]
    (let [cmd (lrf/get-meteo)
          result (v/validate-roundtrip-with-report cmd)]
      (and (= {} (get-in cmd [:lrf :get_meteo]))
           (:valid? result)))))

;; ============================================================================
;; Refine Mode Control Tests
;; ============================================================================

(defspec refine-on-command-always-valid num-tests
  (prop/for-all [_ gen/any]
    (let [cmd (lrf/refine-on)
          result (v/validate-roundtrip-with-report cmd)]
      (and (= {} (get-in cmd [:lrf :refine_on]))
           (:valid? result)))))

(defspec refine-off-command-always-valid num-tests
  (prop/for-all [_ gen/any]
    (let [cmd (lrf/refine-off)
          result (v/validate-roundtrip-with-report cmd)]
      (and (= {} (get-in cmd [:lrf :refine_off]))
           (:valid? result)))))

;; ============================================================================
;; All Commands Combined Test
;; ============================================================================

(defspec all-lrf-commands-roundtrip num-tests
  (prop/for-all [cmd-choice (gen/one-of
                              [(gen/return (lrf/measure))
                               (gen/return (lrf/scan-on))
                               (gen/return (lrf/scan-off))
                               (gen/fmap lrf/set-scan-mode lrf-scan-mode-gen)
                               (gen/return (lrf/start))
                               (gen/return (lrf/stop))
                               (gen/return (lrf/target-designator-off))
                               (gen/return (lrf/target-designator-on-mode-a))
                               (gen/return (lrf/target-designator-on-mode-b))
                               (gen/return (lrf/enable-fog-mode))
                               (gen/return (lrf/disable-fog-mode))
                               (gen/return (lrf/new-session))
                               (gen/return (lrf/get-meteo))
                               (gen/return (lrf/refine-on))
                               (gen/return (lrf/refine-off))])]
    (:valid? (v/validate-roundtrip-with-report cmd-choice))))