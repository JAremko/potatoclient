(ns potatoclient.cmd.gps-generative-test
  "Generative tests for GPS command functions.
   Uses the actual specs from common.clj to generate test data."
  (:require
    [clojure.test :refer [deftest is testing]]
    [matcher-combinators.test] ;; extends clojure.test's `is` macro
    [matcher-combinators.matchers :as matchers]
    [clojure.test.check :as tc]
    [clojure.test.check.clojure-test :refer [defspec]]
    [clojure.test.check.generators :as gen]
    [clojure.test.check.properties :as prop]
    [potatoclient.cmd.gps :as gps]
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

(def latitude-gen
  "Generate valid latitude values from spec"
  (mg/generator common/latitude-spec))

(def longitude-gen
  "Generate valid longitude values from spec"
  (mg/generator common/longitude-spec))

(def altitude-gen
  "Generate valid altitude values from spec"
  (mg/generator common/altitude-spec))

;; ============================================================================
;; GPS Control Tests
;; ============================================================================

(defspec start-command-always-valid num-tests
  (prop/for-all [_ gen/any]
                (let [cmd (gps/start)
                      result (v/validate-roundtrip-with-report cmd)]
                  (and (= {} (get-in cmd [:gps :start]))
                       (:valid? result)))))

(defspec stop-command-always-valid num-tests
  (prop/for-all [_ gen/any]
                (let [cmd (gps/stop)
                      result (v/validate-roundtrip-with-report cmd)]
                  (and (= {} (get-in cmd [:gps :stop]))
                       (:valid? result)))))

;; ============================================================================
;; Manual Position Control Tests
;; ============================================================================

(defspec set-manual-position-with-valid-params num-tests
  (prop/for-all [latitude latitude-gen
                 longitude longitude-gen
                 altitude altitude-gen]
                (let [cmd (gps/set-manual-position latitude longitude altitude)
                      result (v/validate-roundtrip-with-report cmd)]
                  (and (= latitude (get-in cmd [:gps :set_manual_position :latitude]))
                       (= longitude (get-in cmd [:gps :set_manual_position :longitude]))
                       (= altitude (get-in cmd [:gps :set_manual_position :altitude]))
                       (:valid? result)))))

(defspec set-use-manual-position-with-boolean num-tests
  (prop/for-all [flag gen/boolean]
                (let [cmd (gps/set-use-manual-position flag)
                      result (v/validate-roundtrip-with-report cmd)]
                  (and (= flag (get-in cmd [:gps :set_use_manual_position :flag]))
                       (:valid? result)))))

;; ============================================================================
;; Meteo Data Tests
;; ============================================================================

(defspec get-meteo-command-always-valid num-tests
  (prop/for-all [_ gen/any]
                (let [cmd (gps/get-meteo)
                      result (v/validate-roundtrip-with-report cmd)]
                  (and (= {} (get-in cmd [:gps :get_meteo]))
                       (:valid? result)))))

;; ============================================================================
;; All Commands Combined Test
;; ============================================================================

(defspec all-gps-commands-roundtrip num-tests
  (prop/for-all [cmd-choice (gen/one-of
                              [(gen/return (gps/start))
                               (gen/return (gps/stop))
                               (gen/fmap (fn [[lat lon alt]]
                                           (gps/set-manual-position lat lon alt))
                                         (gen/tuple latitude-gen longitude-gen altitude-gen))
                               (gen/fmap gps/set-use-manual-position gen/boolean)
                               (gen/return (gps/get-meteo))])]
                (:valid? (v/validate-roundtrip-with-report cmd-choice))))