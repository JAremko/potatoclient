(ns potatoclient.cmd.compass-generative-test
  "Generative tests for compass command functions.
   Uses the actual specs from common.clj to generate test data."
  (:require
   [clojure.test :refer [deftest is testing]]
   [clojure.test.check :as tc]
   [clojure.test.check.clojure-test :refer [defspec]]
   [clojure.test.check.generators :as gen]
   [clojure.test.check.properties :as prop]
   [potatoclient.cmd.compass :as compass]
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

(def magnetic-declination-gen
  "Generate valid magnetic declination values from spec"
  (mg/generator common/magnetic-declination-spec))

(def offset-azimuth-gen
  "Generate valid offset azimuth values from spec"
  (mg/generator common/offset-azimuth-spec))

(def offset-elevation-gen
  "Generate valid offset elevation values from spec"
  (mg/generator common/offset-elevation-spec))

;; ============================================================================
;; Control Commands Tests
;; ============================================================================

(defspec start-command-always-valid num-tests
  (prop/for-all [_ gen/any]
    (let [cmd (compass/start)
          result (v/validate-roundtrip-with-report cmd)]
      (and (= {} (get-in cmd [:compass :start]))
           (:valid? result)))))

(defspec stop-command-always-valid num-tests
  (prop/for-all [_ gen/any]
    (let [cmd (compass/stop)
          result (v/validate-roundtrip-with-report cmd)]
      (and (= {} (get-in cmd [:compass :stop]))
           (:valid? result)))))

;; ============================================================================
;; Data Request Commands Tests
;; ============================================================================

(defspec get-meteo-command-always-valid num-tests
  (prop/for-all [_ gen/any]
    (let [cmd (compass/get-meteo)
          result (v/validate-roundtrip-with-report cmd)]
      (and (= {} (get-in cmd [:compass :get_meteo]))
           (:valid? result)))))

;; ============================================================================
;; Configuration Commands Tests
;; ============================================================================

(defspec set-magnetic-declination-with-valid-values num-tests
  (prop/for-all [value magnetic-declination-gen]
    (let [cmd (compass/set-magnetic-declination value)
          result (v/validate-roundtrip-with-report cmd)]
      (and (= value (get-in cmd [:compass :set_magnetic_declination :value]))
           (:valid? result)))))

(defspec set-offset-angle-azimuth-with-valid-values num-tests
  (prop/for-all [value offset-azimuth-gen]
    (let [cmd (compass/set-offset-angle-azimuth value)
          result (v/validate-roundtrip-with-report cmd)]
      (and (= value (get-in cmd [:compass :set_offset_angle_azimuth :value]))
           (:valid? result)))))

(defspec set-offset-angle-elevation-with-valid-values num-tests
  (prop/for-all [value offset-elevation-gen]
    (let [cmd (compass/set-offset-angle-elevation value)
          result (v/validate-roundtrip-with-report cmd)]
      (and (= value (get-in cmd [:compass :set_offset_angle_elevation :value]))
           (:valid? result)))))

(defspec set-use-rotary-position-with-boolean num-tests
  (prop/for-all [flag gen/boolean]
    (let [cmd (compass/set-use-rotary-position flag)
          result (v/validate-roundtrip-with-report cmd)]
      (and (= flag (get-in cmd [:compass :set_use_rotary_position :flag]))
           (:valid? result)))))

;; ============================================================================
;; Calibration Commands Tests
;; ============================================================================

(defspec calibrate-long-start-command-always-valid num-tests
  (prop/for-all [_ gen/any]
    (let [cmd (compass/calibrate-long-start)
          result (v/validate-roundtrip-with-report cmd)]
      (and (= {} (get-in cmd [:compass :start_calibrate_long]))
           (:valid? result)))))

(defspec calibrate-short-start-command-always-valid num-tests
  (prop/for-all [_ gen/any]
    (let [cmd (compass/calibrate-short-start)
          result (v/validate-roundtrip-with-report cmd)]
      (and (= {} (get-in cmd [:compass :start_calibrate_short]))
           (:valid? result)))))

(defspec calibrate-next-command-always-valid num-tests
  (prop/for-all [_ gen/any]
    (let [cmd (compass/calibrate-next)
          result (v/validate-roundtrip-with-report cmd)]
      (and (= {} (get-in cmd [:compass :calibrate_next]))
           (:valid? result)))))

(defspec calibrate-cancel-command-always-valid num-tests
  (prop/for-all [_ gen/any]
    (let [cmd (compass/calibrate-cancel)
          result (v/validate-roundtrip-with-report cmd)]
      (and (= {} (get-in cmd [:compass :calibrate_cencel])) ; Note: proto typo
           (:valid? result)))))

;; ============================================================================
;; All Commands Combined Test
;; ============================================================================

(defspec all-compass-commands-roundtrip num-tests
  (prop/for-all [cmd-choice (gen/one-of
                              [(gen/return (compass/start))
                               (gen/return (compass/stop))
                               (gen/return (compass/get-meteo))
                               (gen/fmap compass/set-magnetic-declination magnetic-declination-gen)
                               (gen/fmap compass/set-offset-angle-azimuth offset-azimuth-gen)
                               (gen/fmap compass/set-offset-angle-elevation offset-elevation-gen)
                               (gen/fmap compass/set-use-rotary-position gen/boolean)
                               (gen/return (compass/calibrate-long-start))
                               (gen/return (compass/calibrate-short-start))
                               (gen/return (compass/calibrate-next))
                               (gen/return (compass/calibrate-cancel))])]
    (:valid? (v/validate-roundtrip-with-report cmd-choice))))