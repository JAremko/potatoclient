(ns potatoclient.cmd.rotary-generative-test
  "Generative tests for rotary command functions.
   Uses the actual specs from common.clj to generate test data."
  (:require
    [clojure.test :refer [deftest is testing]]
    [matcher-combinators.test] ;; extends clojure.test's `is` macro
    [matcher-combinators.matchers :as matchers]
    [clojure.test.check :as tc]
    [clojure.test.check.clojure-test :refer [defspec]]
    [clojure.test.check.generators :as gen]
    [clojure.test.check.properties :as prop]
    [potatoclient.cmd.rotary :as rotary]
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

(def azimuth-gen
  "Generate valid azimuth values from spec"
  (mg/generator common/azimuth-spec))

(def elevation-gen
  "Generate valid elevation values from spec"
  (mg/generator common/elevation-spec))

(def bank-gen
  "Generate valid bank values from spec"
  (mg/generator common/bank-spec))

(def relative-azimuth-gen
  "Generate valid relative azimuth values from spec"
  (mg/generator common/relative-azimuth-spec))

(def relative-elevation-gen
  "Generate valid relative elevation values from spec"
  (mg/generator common/relative-elevation-spec))

(def normalized-speed-gen
  "Generate valid normalized speed values from spec"
  (mg/generator common/normalized-speed-spec))

(def normalized-range-gen
  "Generate valid normalized range values from spec"
  (mg/generator common/normalized-range-spec))

(def latitude-gen
  "Generate valid latitude values from spec"
  (mg/generator common/latitude-spec))

(def longitude-gen
  "Generate valid longitude values from spec"
  (mg/generator common/longitude-spec))

(def altitude-gen
  "Generate valid altitude values from spec"
  (mg/generator common/altitude-spec))

(def ndc-coord-gen
  "Generate valid NDC coordinate values from spec"
  (mg/generator common/ndc-coord-clamped-spec))

(def int32-positive-gen
  "Generate valid positive int32 values from spec"
  (mg/generator common/int32-positive-spec))

(def rotary-direction-gen
  "Generate valid rotary direction enum values"
  (mg/generator common/rotary-direction-enum-spec))

(def rotary-mode-gen
  "Generate valid rotary mode enum values"
  (mg/generator common/rotary-mode-enum-spec))

(def video-channel-gen
  "Generate valid video channel enum values"
  (gen/one-of [(gen/return :JON_GUI_DATA_VIDEO_CHANNEL_DAY)
               (gen/return :JON_GUI_DATA_VIDEO_CHANNEL_HEAT)]))

(def linger-gen
  "Generate valid linger values (>= 0.0)"
  (gen/double* {:min 0.0 :max 60.0 :infinite? false :NaN? false}))

(def frame-time-gen
  "Generate valid frame time values (uint64 range)"
  (gen/large-integer* {:min 0 :max common/long-max-value}))

;; ============================================================================
;; Platform Control Commands Tests
;; ============================================================================

(defspec start-command-always-valid num-tests
  (prop/for-all [_ gen/any]
                (let [cmd (rotary/start)
                      result (v/validate-roundtrip-with-report cmd)]
                  (and (= {} (get-in cmd [:rotary :start]))
                       (:valid? result)))))

(defspec stop-command-always-valid num-tests
  (prop/for-all [_ gen/any]
                (let [cmd (rotary/stop)
                      result (v/validate-roundtrip-with-report cmd)]
                  (and (= {} (get-in cmd [:rotary :stop]))
                       (:valid? result)))))

(defspec halt-command-always-valid num-tests
  (prop/for-all [_ gen/any]
                (let [cmd (rotary/halt)
                      result (v/validate-roundtrip-with-report cmd)]
                  (and (= {} (get-in cmd [:rotary :halt]))
                       (:valid? result)))))

;; ============================================================================
;; Platform Position Setting Tests
;; ============================================================================

(defspec set-platform-azimuth-with-valid-values num-tests
  (prop/for-all [value azimuth-gen]
                (let [cmd (rotary/set-platform-azimuth value)
                      result (v/validate-roundtrip-with-report cmd)]
                  (and (= value (get-in cmd [:rotary :set_platform_azimuth :value]))
                       (:valid? result)))))

(defspec set-platform-elevation-with-valid-values num-tests
  (prop/for-all [value elevation-gen]
                (let [cmd (rotary/set-platform-elevation value)
                      result (v/validate-roundtrip-with-report cmd)]
                  (and (= value (get-in cmd [:rotary :set_platform_elevation :value]))
                       (:valid? result)))))

(defspec set-platform-bank-with-valid-values num-tests
  (prop/for-all [value bank-gen]
                (let [cmd (rotary/set-platform-bank value)
                      result (v/validate-roundtrip-with-report cmd)]
                  (and (= value (get-in cmd [:rotary :set_platform_bank :value]))
                       (:valid? result)))))

;; ============================================================================
;; Mode and Configuration Tests
;; ============================================================================

(defspec set-mode-with-valid-enum num-tests
  (prop/for-all [mode rotary-mode-gen]
                (let [cmd (rotary/set-mode mode)
                      result (v/validate-roundtrip-with-report cmd)]
                  (and (= mode (get-in cmd [:rotary :set_mode :mode]))
                       (:valid? result)))))

(defspec set-use-rotary-as-compass-with-boolean num-tests
  (prop/for-all [flag gen/boolean]
                (let [cmd (rotary/set-use-rotary-as-compass flag)
                      result (v/validate-roundtrip-with-report cmd)]
                  (and (= flag (get-in cmd [:rotary :set_use_rotary_as_compass :flag]))
                       (:valid? result)))))

;; ============================================================================
;; Azimuth Control Tests
;; ============================================================================

(defspec halt-azimuth-command-always-valid num-tests
  (prop/for-all [_ gen/any]
                (let [cmd (rotary/halt-azimuth)
                      result (v/validate-roundtrip-with-report cmd)]
                  (and (= {} (get-in cmd [:rotary :axis :azimuth :halt]))
                       (:valid? result)))))

(defspec set-azimuth-value-with-valid-params num-tests
  (prop/for-all [value azimuth-gen
                 direction rotary-direction-gen]
                (let [cmd (rotary/set-azimuth-value value direction)
                      result (v/validate-roundtrip-with-report cmd)]
                  (and (= value (get-in cmd [:rotary :axis :azimuth :set_value :value]))
                       (= direction (get-in cmd [:rotary :axis :azimuth :set_value :direction]))
                       (:valid? result)))))

(defspec rotate-azimuth-to-with-valid-params num-tests
  (prop/for-all [target azimuth-gen
                 speed normalized-speed-gen
                 direction rotary-direction-gen]
                (let [cmd (rotary/rotate-azimuth-to target speed direction)
                      result (v/validate-roundtrip-with-report cmd)]
                  (and (= target (get-in cmd [:rotary :axis :azimuth :rotate_to :target_value]))
                       (= speed (get-in cmd [:rotary :axis :azimuth :rotate_to :speed]))
                       (= direction (get-in cmd [:rotary :axis :azimuth :rotate_to :direction]))
                       (:valid? result)))))

(defspec rotate-azimuth-with-valid-params num-tests
  (prop/for-all [speed normalized-speed-gen
                 direction rotary-direction-gen]
                (let [cmd (rotary/rotate-azimuth speed direction)
                      result (v/validate-roundtrip-with-report cmd)]
                  (and (= speed (get-in cmd [:rotary :axis :azimuth :rotate :speed]))
                       (= direction (get-in cmd [:rotary :axis :azimuth :rotate :direction]))
                       (:valid? result)))))

(defspec rotate-azimuth-relative-with-valid-params num-tests
  (prop/for-all [value relative-azimuth-gen
                 speed normalized-speed-gen
                 direction rotary-direction-gen]
                (let [cmd (rotary/rotate-azimuth-relative value speed direction)
                      result (v/validate-roundtrip-with-report cmd)]
                  (and (= value (get-in cmd [:rotary :axis :azimuth :relative :value]))
                       (= speed (get-in cmd [:rotary :axis :azimuth :relative :speed]))
                       (= direction (get-in cmd [:rotary :axis :azimuth :relative :direction]))
                       (:valid? result)))))

(defspec rotate-azimuth-relative-set-with-valid-params num-tests
  (prop/for-all [value relative-azimuth-gen
                 direction rotary-direction-gen]
                (let [cmd (rotary/rotate-azimuth-relative-set value direction)
                      result (v/validate-roundtrip-with-report cmd)]
                  (and (= value (get-in cmd [:rotary :axis :azimuth :relative_set :value]))
                       (= direction (get-in cmd [:rotary :axis :azimuth :relative_set :direction]))
                       (:valid? result)))))

;; ============================================================================
;; Elevation Control Tests
;; ============================================================================

(defspec halt-elevation-command-always-valid num-tests
  (prop/for-all [_ gen/any]
                (let [cmd (rotary/halt-elevation)
                      result (v/validate-roundtrip-with-report cmd)]
                  (and (= {} (get-in cmd [:rotary :axis :elevation :halt]))
                       (:valid? result)))))

(defspec set-elevation-value-with-valid-params num-tests
  (prop/for-all [value elevation-gen]
                (let [cmd (rotary/set-elevation-value value)
                      result (v/validate-roundtrip-with-report cmd)]
                  (and (= value (get-in cmd [:rotary :axis :elevation :set_value :value]))
                       (:valid? result)))))

(defspec rotate-elevation-to-with-valid-params num-tests
  (prop/for-all [target elevation-gen
                 speed normalized-speed-gen]
                (let [cmd (rotary/rotate-elevation-to target speed)
                      result (v/validate-roundtrip-with-report cmd)]
                  (and (= target (get-in cmd [:rotary :axis :elevation :rotate_to :target_value]))
                       (= speed (get-in cmd [:rotary :axis :elevation :rotate_to :speed]))
                       (:valid? result)))))

(defspec rotate-elevation-with-valid-params num-tests
  (prop/for-all [speed normalized-speed-gen
                 direction rotary-direction-gen]
                (let [cmd (rotary/rotate-elevation speed direction)
                      result (v/validate-roundtrip-with-report cmd)]
                  (and (= speed (get-in cmd [:rotary :axis :elevation :rotate :speed]))
                       (= direction (get-in cmd [:rotary :axis :elevation :rotate :direction]))
                       (:valid? result)))))

(defspec rotate-elevation-relative-with-valid-params num-tests
  (prop/for-all [value relative-elevation-gen
                 speed normalized-speed-gen
                 direction rotary-direction-gen]
                (let [cmd (rotary/rotate-elevation-relative value speed direction)
                      result (v/validate-roundtrip-with-report cmd)]
                  (and (= value (get-in cmd [:rotary :axis :elevation :relative :value]))
                       (= speed (get-in cmd [:rotary :axis :elevation :relative :speed]))
                       (= direction (get-in cmd [:rotary :axis :elevation :relative :direction]))
                       (:valid? result)))))

(defspec rotate-elevation-relative-set-with-valid-params num-tests
  (prop/for-all [value relative-elevation-gen
                 direction rotary-direction-gen]
                (let [cmd (rotary/rotate-elevation-relative-set value direction)
                      result (v/validate-roundtrip-with-report cmd)]
                  (and (= value (get-in cmd [:rotary :axis :elevation :relative_set :value]))
                       (= direction (get-in cmd [:rotary :axis :elevation :relative_set :direction]))
                       (:valid? result)))))

;; ============================================================================
;; Combined Axis Control Tests
;; ============================================================================

(defspec halt-both-command-always-valid num-tests
  (prop/for-all [_ gen/any]
                (let [cmd (rotary/halt-both)
                      result (v/validate-roundtrip-with-report cmd)]
                  (and (= {} (get-in cmd [:rotary :axis :azimuth :halt]))
                       (= {} (get-in cmd [:rotary :axis :elevation :halt]))
                       (:valid? result)))))

(defspec rotate-both-to-with-valid-params num-tests
  (prop/for-all [azimuth azimuth-gen
                 azimuth-speed normalized-speed-gen
                 azimuth-direction rotary-direction-gen
                 elevation elevation-gen
                 elevation-speed normalized-speed-gen]
                (let [cmd (rotary/rotate-both-to azimuth azimuth-speed azimuth-direction
                                                 elevation elevation-speed)
                      result (v/validate-roundtrip-with-report cmd)]
                  (and (= azimuth (get-in cmd [:rotary :axis :azimuth :rotate_to :target_value]))
                       (= azimuth-speed (get-in cmd [:rotary :axis :azimuth :rotate_to :speed]))
                       (= azimuth-direction (get-in cmd [:rotary :axis :azimuth :rotate_to :direction]))
                       (= elevation (get-in cmd [:rotary :axis :elevation :rotate_to :target_value]))
                       (= elevation-speed (get-in cmd [:rotary :axis :elevation :rotate_to :speed]))
                       (:valid? result)))))

(defspec rotate-both-with-valid-params num-tests
  (prop/for-all [azimuth-speed normalized-speed-gen
                 azimuth-direction rotary-direction-gen
                 elevation-speed normalized-speed-gen
                 elevation-direction rotary-direction-gen]
                (let [cmd (rotary/rotate-both azimuth-speed azimuth-direction
                                              elevation-speed elevation-direction)
                      result (v/validate-roundtrip-with-report cmd)]
                  (and (= azimuth-speed (get-in cmd [:rotary :axis :azimuth :rotate :speed]))
                       (= azimuth-direction (get-in cmd [:rotary :axis :azimuth :rotate :direction]))
                       (= elevation-speed (get-in cmd [:rotary :axis :elevation :rotate :speed]))
                       (= elevation-direction (get-in cmd [:rotary :axis :elevation :rotate :direction]))
                       (:valid? result)))))

(defspec rotate-both-relative-with-valid-params num-tests
  (prop/for-all [azimuth relative-azimuth-gen
                 azimuth-speed normalized-speed-gen
                 azimuth-direction rotary-direction-gen
                 elevation relative-elevation-gen
                 elevation-speed normalized-speed-gen
                 elevation-direction rotary-direction-gen]
                (let [cmd (rotary/rotate-both-relative azimuth azimuth-speed azimuth-direction
                                                       elevation elevation-speed elevation-direction)
                      result (v/validate-roundtrip-with-report cmd)]
                  (and (= azimuth (get-in cmd [:rotary :axis :azimuth :relative :value]))
                       (= azimuth-speed (get-in cmd [:rotary :axis :azimuth :relative :speed]))
                       (= azimuth-direction (get-in cmd [:rotary :axis :azimuth :relative :direction]))
                       (= elevation (get-in cmd [:rotary :axis :elevation :relative :value]))
                       (= elevation-speed (get-in cmd [:rotary :axis :elevation :relative :speed]))
                       (= elevation-direction (get-in cmd [:rotary :axis :elevation :relative :direction]))
                       (:valid? result)))))

(defspec set-both-values-with-valid-params num-tests
  (prop/for-all [azimuth azimuth-gen
                 azimuth-direction rotary-direction-gen
                 elevation elevation-gen]
                (let [cmd (rotary/set-both-values azimuth azimuth-direction elevation)
                      result (v/validate-roundtrip-with-report cmd)]
                  (and (= azimuth (get-in cmd [:rotary :axis :azimuth :set_value :value]))
                       (= azimuth-direction (get-in cmd [:rotary :axis :azimuth :set_value :direction]))
                       (= elevation (get-in cmd [:rotary :axis :elevation :set_value :value]))
                       (:valid? result)))))

;; ============================================================================
;; GPS Integration Tests
;; ============================================================================

(defspec rotate-to-gps-with-valid-params num-tests
  (prop/for-all [latitude latitude-gen
                 longitude longitude-gen
                 altitude altitude-gen]
                (let [cmd (rotary/rotate-to-gps latitude longitude altitude)
                      result (v/validate-roundtrip-with-report cmd)]
                  (and (= latitude (get-in cmd [:rotary :rotate_to_gps :latitude]))
                       (= longitude (get-in cmd [:rotary :rotate_to_gps :longitude]))
                       (= altitude (get-in cmd [:rotary :rotate_to_gps :altitude]))
                       (:valid? result)))))

(defspec set-origin-gps-with-valid-params num-tests
  (prop/for-all [latitude latitude-gen
                 longitude longitude-gen
                 altitude altitude-gen]
                (let [cmd (rotary/set-origin-gps latitude longitude altitude)
                      result (v/validate-roundtrip-with-report cmd)]
                  (and (= latitude (get-in cmd [:rotary :set_origin_gps :latitude]))
                       (= longitude (get-in cmd [:rotary :set_origin_gps :longitude]))
                       (= altitude (get-in cmd [:rotary :set_origin_gps :altitude]))
                       (:valid? result)))))

;; ============================================================================
;; NDC Control Tests
;; ============================================================================

(defspec rotate-to-ndc-with-valid-params num-tests
  (prop/for-all [channel video-channel-gen
                 x ndc-coord-gen
                 y ndc-coord-gen
                 frame-time frame-time-gen]
                (let [cmd (rotary/rotate-to-ndc channel x y frame-time)
                      result (v/validate-roundtrip-with-report cmd)]
                  (and (= channel (get-in cmd [:rotary :rotate_to_ndc :channel]))
                       (= x (get-in cmd [:rotary :rotate_to_ndc :x]))
                       (= y (get-in cmd [:rotary :rotate_to_ndc :y]))
                       (= frame-time (get-in cmd [:rotary :rotate_to_ndc :frame_time]))
                       ;; state_time should be set (from app-state)
                       (number? (get-in cmd [:rotary :rotate_to_ndc :state_time]))
                       (:valid? result)))))

;; ============================================================================
;; Scan Operations Tests
;; ============================================================================

(defspec scan-start-command-always-valid num-tests
  (prop/for-all [_ gen/any]
                (let [cmd (rotary/scan-start)
                      result (v/validate-roundtrip-with-report cmd)]
                  (and (= {} (get-in cmd [:rotary :scan_start]))
                       (:valid? result)))))

(defspec scan-stop-command-always-valid num-tests
  (prop/for-all [_ gen/any]
                (let [cmd (rotary/scan-stop)
                      result (v/validate-roundtrip-with-report cmd)]
                  (and (= {} (get-in cmd [:rotary :scan_stop]))
                       (:valid? result)))))

(defspec scan-pause-command-always-valid num-tests
  (prop/for-all [_ gen/any]
                (let [cmd (rotary/scan-pause)
                      result (v/validate-roundtrip-with-report cmd)]
                  (and (= {} (get-in cmd [:rotary :scan_pause]))
                       (:valid? result)))))

(defspec scan-unpause-command-always-valid num-tests
  (prop/for-all [_ gen/any]
                (let [cmd (rotary/scan-unpause)
                      result (v/validate-roundtrip-with-report cmd)]
                  (and (= {} (get-in cmd [:rotary :scan_unpause]))
                       (:valid? result)))))

(defspec scan-prev-command-always-valid num-tests
  (prop/for-all [_ gen/any]
                (let [cmd (rotary/scan-prev)
                      result (v/validate-roundtrip-with-report cmd)]
                  (and (= {} (get-in cmd [:rotary :scan_prev]))
                       (:valid? result)))))

(defspec scan-next-command-always-valid num-tests
  (prop/for-all [_ gen/any]
                (let [cmd (rotary/scan-next)
                      result (v/validate-roundtrip-with-report cmd)]
                  (and (= {} (get-in cmd [:rotary :scan_next]))
                       (:valid? result)))))

(defspec scan-refresh-node-list-command-always-valid num-tests
  (prop/for-all [_ gen/any]
                (let [cmd (rotary/scan-refresh-node-list)
                      result (v/validate-roundtrip-with-report cmd)]
                  (and (= {} (get-in cmd [:rotary :scan_refresh_node_list]))
                       (:valid? result)))))

(defspec scan-select-node-with-valid-index num-tests
  (prop/for-all [index int32-positive-gen]
                (let [cmd (rotary/scan-select-node index)
                      result (v/validate-roundtrip-with-report cmd)]
                  (and (= index (get-in cmd [:rotary :scan_select_node :index]))
                       (:valid? result)))))

(defspec scan-delete-node-with-valid-index num-tests
  (prop/for-all [index int32-positive-gen]
                (let [cmd (rotary/scan-delete-node index)
                      result (v/validate-roundtrip-with-report cmd)]
                  (and (= index (get-in cmd [:rotary :scan_delete_node :index]))
                       (:valid? result)))))

(defspec scan-update-node-with-valid-params num-tests
  (prop/for-all [index int32-positive-gen
                 day-zoom int32-positive-gen
                 heat-zoom int32-positive-gen
                 azimuth azimuth-gen
                 elevation elevation-gen
                 linger linger-gen
                 speed normalized-speed-gen]
                (let [cmd (rotary/scan-update-node index day-zoom heat-zoom azimuth elevation linger speed)
                      result (v/validate-roundtrip-with-report cmd)]
                  (and (= index (get-in cmd [:rotary :scan_update_node :index]))
                       (= day-zoom (get-in cmd [:rotary :scan_update_node :DayZoomTableValue]))
                       (= heat-zoom (get-in cmd [:rotary :scan_update_node :HeatZoomTableValue]))
                       (= azimuth (get-in cmd [:rotary :scan_update_node :azimuth]))
                       (= elevation (get-in cmd [:rotary :scan_update_node :elevation]))
                       (= linger (get-in cmd [:rotary :scan_update_node :linger]))
                       (= speed (get-in cmd [:rotary :scan_update_node :speed]))
                       (:valid? result)))))

(defspec scan-add-node-with-valid-params num-tests
  (prop/for-all [index int32-positive-gen
                 day-zoom int32-positive-gen
                 heat-zoom int32-positive-gen
                 azimuth azimuth-gen
                 elevation elevation-gen
                 linger linger-gen
                 speed normalized-speed-gen]
                (let [cmd (rotary/scan-add-node index day-zoom heat-zoom azimuth elevation linger speed)
                      result (v/validate-roundtrip-with-report cmd)]
                  (and (= index (get-in cmd [:rotary :scan_add_node :index]))
                       (= day-zoom (get-in cmd [:rotary :scan_add_node :DayZoomTableValue]))
                       (= heat-zoom (get-in cmd [:rotary :scan_add_node :HeatZoomTableValue]))
                       (= azimuth (get-in cmd [:rotary :scan_add_node :azimuth]))
                       (= elevation (get-in cmd [:rotary :scan_add_node :elevation]))
                       (= linger (get-in cmd [:rotary :scan_add_node :linger]))
                       (= speed (get-in cmd [:rotary :scan_add_node :speed]))
                       (:valid? result)))))

;; ============================================================================
;; Meteo Data Tests
;; ============================================================================

(defspec get-meteo-command-always-valid num-tests
  (prop/for-all [_ gen/any]
                (let [cmd (rotary/get-meteo)
                      result (v/validate-roundtrip-with-report cmd)]
                  (and (= {} (get-in cmd [:rotary :get_meteo]))
                       (:valid? result)))))

;; ============================================================================
;; All Commands Combined Test
;; ============================================================================

(defspec all-rotary-commands-roundtrip num-tests
  (prop/for-all [cmd-choice (gen/one-of
                              [(gen/return (rotary/start))
                               (gen/return (rotary/stop))
                               (gen/return (rotary/halt))
                               (gen/fmap rotary/set-platform-azimuth azimuth-gen)
                               (gen/fmap rotary/set-platform-elevation elevation-gen)
                               (gen/fmap rotary/set-platform-bank bank-gen)
                               (gen/fmap rotary/set-mode rotary-mode-gen)
                               (gen/fmap rotary/set-use-rotary-as-compass gen/boolean)
                               (gen/return (rotary/halt-azimuth))
                               (gen/return (rotary/halt-elevation))
                               (gen/return (rotary/halt-both))
                               (gen/return (rotary/scan-start))
                               (gen/return (rotary/scan-stop))
                               (gen/return (rotary/scan-pause))
                               (gen/return (rotary/scan-unpause))
                               (gen/return (rotary/scan-prev))
                               (gen/return (rotary/scan-next))
                               (gen/return (rotary/scan-refresh-node-list))
                               (gen/fmap rotary/scan-select-node int32-positive-gen)
                               (gen/fmap rotary/scan-delete-node int32-positive-gen)
                               (gen/return (rotary/get-meteo))])]
                (:valid? (v/validate-roundtrip-with-report cmd-choice))))