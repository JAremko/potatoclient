(ns potatoclient.cmd.cv-generative-test
  "Generative tests for CV (Computer Vision) command functions.
   Uses the actual specs from common.clj to generate test data."
  (:require
   [clojure.test :refer [deftest is testing]]
   [matcher-combinators.test] ;; extends clojure.test's `is` macro
   [matcher-combinators.matchers :as matchers]
   [clojure.test.check :as tc]
   [clojure.test.check.clojure-test :refer [defspec]]
   [clojure.test.check.generators :as gen]
   [clojure.test.check.properties :as prop]
   [potatoclient.cmd.cv :as cv]
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

(def video-channel-gen
  "Generate valid video channel enum values"
  (mg/generator common/video-channel-enum-spec))

(def ndc-coord-gen
  "Generate valid NDC coordinate values (-1.0 to 1.0)"
  (mg/generator common/ndc-coord-clamped-spec))

(def frame-time-gen
  "Generate valid frame time values (uint64)"
  (mg/generator common/frame-time-spec))

;; ============================================================================
;; Tracking Commands Tests
;; ============================================================================

(defspec start-track-ndc-with-valid-params num-tests
  (prop/for-all [channel video-channel-gen
                 x ndc-coord-gen
                 y ndc-coord-gen
                 frame-time frame-time-gen]
    (let [cmd (cv/start-track-ndc channel x y frame-time)
          result (v/validate-roundtrip-with-report cmd)]
      (and (= channel (get-in cmd [:cv :start_track_ndc :channel]))
           (= x (get-in cmd [:cv :start_track_ndc :x]))
           (= y (get-in cmd [:cv :start_track_ndc :y]))
           (= frame-time (get-in cmd [:cv :start_track_ndc :frame_time]))
           (:valid? result)))))

(defspec stop-track-command-always-valid num-tests
  (prop/for-all [_ gen/any]
    (let [cmd (cv/stop-track)
          result (v/validate-roundtrip-with-report cmd)]
      (and (= {} (get-in cmd [:cv :stop_track]))
           (:valid? result)))))

;; ============================================================================
;; Focus Control Tests
;; ============================================================================

(defspec set-auto-focus-with-valid-params num-tests
  (prop/for-all [channel video-channel-gen
                 enabled? gen/boolean]
    (let [cmd (cv/set-auto-focus channel enabled?)
          result (v/validate-roundtrip-with-report cmd)]
      (and (= channel (get-in cmd [:cv :set_auto_focus :channel]))
           (= enabled? (get-in cmd [:cv :set_auto_focus :value]))
           (:valid? result)))))

;; ============================================================================
;; Vampire Mode Tests
;; ============================================================================

(defspec enable-vampire-mode-command-always-valid num-tests
  (prop/for-all [_ gen/any]
    (let [cmd (cv/enable-vampire-mode)
          result (v/validate-roundtrip-with-report cmd)]
      (and (= {} (get-in cmd [:cv :vampire_mode_enable]))
           (:valid? result)))))

(defspec disable-vampire-mode-command-always-valid num-tests
  (prop/for-all [_ gen/any]
    (let [cmd (cv/disable-vampire-mode)
          result (v/validate-roundtrip-with-report cmd)]
      (and (= {} (get-in cmd [:cv :vampire_mode_disable]))
           (:valid? result)))))

;; ============================================================================
;; Stabilization Mode Tests
;; ============================================================================

(defspec enable-stabilization-mode-command-always-valid num-tests
  (prop/for-all [_ gen/any]
    (let [cmd (cv/enable-stabilization-mode)
          result (v/validate-roundtrip-with-report cmd)]
      (and (= {} (get-in cmd [:cv :stabilization_mode_enable]))
           (:valid? result)))))

(defspec disable-stabilization-mode-command-always-valid num-tests
  (prop/for-all [_ gen/any]
    (let [cmd (cv/disable-stabilization-mode)
          result (v/validate-roundtrip-with-report cmd)]
      (and (= {} (get-in cmd [:cv :stabilization_mode_disable]))
           (:valid? result)))))

;; ============================================================================
;; Data Dump Commands Tests
;; ============================================================================

(defspec start-dump-command-always-valid num-tests
  (prop/for-all [_ gen/any]
    (let [cmd (cv/start-dump)
          result (v/validate-roundtrip-with-report cmd)]
      (and (= {} (get-in cmd [:cv :dump_start]))
           (:valid? result)))))

(defspec stop-dump-command-always-valid num-tests
  (prop/for-all [_ gen/any]
    (let [cmd (cv/stop-dump)
          result (v/validate-roundtrip-with-report cmd)]
      (and (= {} (get-in cmd [:cv :dump_stop]))
           (:valid? result)))))

;; ============================================================================
;; Recognition Mode Tests
;; ============================================================================

(defspec enable-recognition-mode-command-always-valid num-tests
  (prop/for-all [_ gen/any]
    (let [cmd (cv/enable-recognition-mode)
          result (v/validate-roundtrip-with-report cmd)]
      (and (= {} (get-in cmd [:cv :recognition_mode_enable]))
           (:valid? result)))))

(defspec disable-recognition-mode-command-always-valid num-tests
  (prop/for-all [_ gen/any]
    (let [cmd (cv/disable-recognition-mode)
          result (v/validate-roundtrip-with-report cmd)]
      (and (= {} (get-in cmd [:cv :recognition_mode_disable]))
           (:valid? result)))))

;; ============================================================================
;; All Commands Combined Test
;; ============================================================================

(defspec all-cv-commands-roundtrip num-tests
  (prop/for-all [cmd-choice (gen/one-of
                              [(gen/fmap (fn [[ch x y t]] (cv/start-track-ndc ch x y t))
                                        (gen/tuple video-channel-gen ndc-coord-gen 
                                                  ndc-coord-gen frame-time-gen))
                               (gen/return (cv/stop-track))
                               (gen/fmap (fn [[ch e]] (cv/set-auto-focus ch e))
                                        (gen/tuple video-channel-gen gen/boolean))
                               (gen/return (cv/enable-vampire-mode))
                               (gen/return (cv/disable-vampire-mode))
                               (gen/return (cv/enable-stabilization-mode))
                               (gen/return (cv/disable-stabilization-mode))
                               (gen/return (cv/start-dump))
                               (gen/return (cv/stop-dump))
                               (gen/return (cv/enable-recognition-mode))
                               (gen/return (cv/disable-recognition-mode))])]
    (:valid? (v/validate-roundtrip-with-report cmd-choice))))