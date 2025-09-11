(ns potatoclient.cmd.rotary-test
  "Tests for Rotary Platform command functions."
  (:require
    [clojure.test :refer [deftest is testing]]
    [matcher-combinators.test] ;; extends clojure.test's `is` macro
    [matcher-combinators.matchers :as matchers]
    [potatoclient.cmd.rotary :as rotary]
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
;; Platform Control Tests
;; ============================================================================

(deftest test-platform-control
  (testing "start creates valid command"
    (let [result (rotary/start)]
      (is (m/validate :cmd/root result))
      (is (match? {:rotary {:start {}}}
                  result))
      (let [roundtrip-result (validation/validate-roundtrip-with-report result)]
        (is (:valid? roundtrip-result)
            (str "Should pass roundtrip validation"
                 (when-not (:valid? roundtrip-result)
                   (str "\nDiff:\n" (:pretty-diff roundtrip-result))))))))

  (testing "stop creates valid command"
    (let [result (rotary/stop)]
      (is (m/validate :cmd/root result))
      (is (match? {:rotary {:stop {}}}
                  result))
      (let [roundtrip-result (validation/validate-roundtrip-with-report result)]
        (is (:valid? roundtrip-result)))))

  (testing "halt creates valid command"
    (let [result (rotary/halt)]
      (is (m/validate :cmd/root result))
      (is (match? {:rotary {:halt {}}}
                  result))
      (let [roundtrip-result (validation/validate-roundtrip-with-report result)]
        (is (:valid? roundtrip-result))))))

;; ============================================================================
;; Platform Position Setting Tests
;; ============================================================================

(deftest test-platform-position
  (testing "set-platform-azimuth creates valid command"
    (let [result (rotary/set-platform-azimuth 45.5)]
      (is (m/validate :cmd/root result))
      (is (match? {:rotary {:set_platform_azimuth {:value 45.5}}}
                  result))
      (let [roundtrip-result (validation/validate-roundtrip-with-report result)]
        (is (:valid? roundtrip-result)))))

  (testing "set-platform-elevation creates valid command"
    (let [result (rotary/set-platform-elevation -30.0)]
      (is (m/validate :cmd/root result))
      (is (match? {:rotary {:set_platform_elevation {:value -30.0}}}
                  result))
      (let [roundtrip-result (validation/validate-roundtrip-with-report result)]
        (is (:valid? roundtrip-result)))))

  (testing "set-platform-bank creates valid command"
    (let [result (rotary/set-platform-bank 15.0)]
      (is (m/validate :cmd/root result))
      (is (match? {:rotary {:set_platform_bank {:value 15.0}}}
                  result))
      (let [roundtrip-result (validation/validate-roundtrip-with-report result)]
        (is (:valid? roundtrip-result))))))

;; ============================================================================
;; Mode and Configuration Tests
;; ============================================================================

(deftest test-mode-and-config
  (testing "set-mode creates valid command"
    (let [result (rotary/set-mode :JON_GUI_DATA_ROTARY_MODE_SPEED)]
      (is (m/validate :cmd/root result))
      (is (= :JON_GUI_DATA_ROTARY_MODE_SPEED (get-in result [:rotary :set_mode :mode])))
      (let [roundtrip-result (validation/validate-roundtrip-with-report result)]
        (is (:valid? roundtrip-result)))))

  (testing "set-use-rotary-as-compass creates valid command"
    (let [result (rotary/set-use-rotary-as-compass true)]
      (is (m/validate :cmd/root result))
      (is (= true (get-in result [:rotary :set_use_rotary_as_compass :flag])))
      (let [roundtrip-result (validation/validate-roundtrip-with-report result)]
        (is (:valid? roundtrip-result))))))

;; ============================================================================
;; Azimuth Control Tests
;; ============================================================================

(deftest test-azimuth-control
  (testing "halt-azimuth creates valid command"
    (let [result (rotary/halt-azimuth)]
      (is (m/validate :cmd/root result))
      (is (= {} (get-in result [:rotary :axis :azimuth :halt])))
      (let [roundtrip-result (validation/validate-roundtrip-with-report result)]
        (is (:valid? roundtrip-result)))))

  (testing "set-azimuth-value creates valid command"
    (let [result (rotary/set-azimuth-value 180.0 :JON_GUI_DATA_ROTARY_DIRECTION_CLOCKWISE)]
      (is (m/validate :cmd/root result))
      (is (= 180.0 (get-in result [:rotary :axis :azimuth :set_value :value])))
      (is (= :JON_GUI_DATA_ROTARY_DIRECTION_CLOCKWISE
             (get-in result [:rotary :axis :azimuth :set_value :direction])))
      (let [roundtrip-result (validation/validate-roundtrip-with-report result)]
        (is (:valid? roundtrip-result)))))

  (testing "rotate-azimuth-to creates valid command"
    (let [result (rotary/rotate-azimuth-to 270.0 0.5 :JON_GUI_DATA_ROTARY_DIRECTION_COUNTER_CLOCKWISE)]
      (is (m/validate :cmd/root result))
      (is (= 270.0 (get-in result [:rotary :axis :azimuth :rotate_to :target_value])))
      (is (= 0.5 (get-in result [:rotary :axis :azimuth :rotate_to :speed])))
      (let [roundtrip-result (validation/validate-roundtrip-with-report result)]
        (is (:valid? roundtrip-result)))))

  (testing "rotate-azimuth creates valid command"
    (let [result (rotary/rotate-azimuth 0.75 :JON_GUI_DATA_ROTARY_DIRECTION_CLOCKWISE)]
      (is (m/validate :cmd/root result))
      (is (= 0.75 (get-in result [:rotary :axis :azimuth :rotate :speed])))
      (let [roundtrip-result (validation/validate-roundtrip-with-report result)]
        (is (:valid? roundtrip-result)))))

  (testing "rotate-azimuth-relative creates valid command"
    (let [result (rotary/rotate-azimuth-relative 45.0 0.3 :JON_GUI_DATA_ROTARY_DIRECTION_CLOCKWISE)]
      (is (m/validate :cmd/root result))
      (is (= 45.0 (get-in result [:rotary :axis :azimuth :relative :value])))
      (is (= 0.3 (get-in result [:rotary :axis :azimuth :relative :speed])))
      (let [roundtrip-result (validation/validate-roundtrip-with-report result)]
        (is (:valid? roundtrip-result)))))

  (testing "rotate-azimuth-relative-set creates valid command"
    (let [result (rotary/rotate-azimuth-relative-set -90.0 :JON_GUI_DATA_ROTARY_DIRECTION_COUNTER_CLOCKWISE)]
      (is (m/validate :cmd/root result))
      (is (= -90.0 (get-in result [:rotary :axis :azimuth :relative_set :value])))
      (let [roundtrip-result (validation/validate-roundtrip-with-report result)]
        (is (:valid? roundtrip-result))))))

;; ============================================================================
;; Elevation Control Tests
;; ============================================================================

(deftest test-elevation-control
  (testing "halt-elevation creates valid command"
    (let [result (rotary/halt-elevation)]
      (is (m/validate :cmd/root result))
      (is (= {} (get-in result [:rotary :axis :elevation :halt])))
      (let [roundtrip-result (validation/validate-roundtrip-with-report result)]
        (is (:valid? roundtrip-result)))))

  (testing "set-elevation-value creates valid command"
    (let [result (rotary/set-elevation-value 45.0)]
      (is (m/validate :cmd/root result))
      (is (= 45.0 (get-in result [:rotary :axis :elevation :set_value :value])))
      (let [roundtrip-result (validation/validate-roundtrip-with-report result)]
        (is (:valid? roundtrip-result)))))

  (testing "rotate-elevation-to creates valid command"
    (let [result (rotary/rotate-elevation-to -15.0 0.25)]
      (is (m/validate :cmd/root result))
      (is (= -15.0 (get-in result [:rotary :axis :elevation :rotate_to :target_value])))
      (is (= 0.25 (get-in result [:rotary :axis :elevation :rotate_to :speed])))
      (let [roundtrip-result (validation/validate-roundtrip-with-report result)]
        (is (:valid? roundtrip-result)))))

  (testing "rotate-elevation creates valid command"
    (let [result (rotary/rotate-elevation 0.5 :JON_GUI_DATA_ROTARY_DIRECTION_CLOCKWISE)]
      (is (m/validate :cmd/root result))
      (is (= 0.5 (get-in result [:rotary :axis :elevation :rotate :speed])))
      (let [roundtrip-result (validation/validate-roundtrip-with-report result)]
        (is (:valid? roundtrip-result)))))

  (testing "rotate-elevation-relative creates valid command"
    (let [result (rotary/rotate-elevation-relative 30.0 0.4 :JON_GUI_DATA_ROTARY_DIRECTION_CLOCKWISE)]
      (is (m/validate :cmd/root result))
      (is (= 30.0 (get-in result [:rotary :axis :elevation :relative :value])))
      (is (= 0.4 (get-in result [:rotary :axis :elevation :relative :speed])))
      (let [roundtrip-result (validation/validate-roundtrip-with-report result)]
        (is (:valid? roundtrip-result)))))

  (testing "rotate-elevation-relative-set creates valid command"
    (let [result (rotary/rotate-elevation-relative-set -45.0 :JON_GUI_DATA_ROTARY_DIRECTION_COUNTER_CLOCKWISE)]
      (is (m/validate :cmd/root result))
      (is (= -45.0 (get-in result [:rotary :axis :elevation :relative_set :value])))
      (let [roundtrip-result (validation/validate-roundtrip-with-report result)]
        (is (:valid? roundtrip-result))))))

;; ============================================================================
;; Combined Axis Control Tests
;; ============================================================================

(deftest test-combined-axis-control
  (testing "halt-both creates valid command"
    (let [result (rotary/halt-both)]
      (is (m/validate :cmd/root result))
      (is (= {} (get-in result [:rotary :axis :azimuth :halt])))
      (is (= {} (get-in result [:rotary :axis :elevation :halt])))
      (let [roundtrip-result (validation/validate-roundtrip-with-report result)]
        (is (:valid? roundtrip-result)))))

  (testing "rotate-both-to creates valid command"
    (let [result (rotary/rotate-both-to
                   90.0 0.5 :JON_GUI_DATA_ROTARY_DIRECTION_CLOCKWISE
                   30.0 0.3)]
      (is (m/validate :cmd/root result))
      (is (= 90.0 (get-in result [:rotary :axis :azimuth :rotate_to :target_value])))
      (is (= 30.0 (get-in result [:rotary :axis :elevation :rotate_to :target_value])))
      (let [roundtrip-result (validation/validate-roundtrip-with-report result)]
        (is (:valid? roundtrip-result)))))

  (testing "rotate-both creates valid command"
    (let [result (rotary/rotate-both
                   0.6 :JON_GUI_DATA_ROTARY_DIRECTION_CLOCKWISE
                   0.4 :JON_GUI_DATA_ROTARY_DIRECTION_COUNTER_CLOCKWISE)]
      (is (m/validate :cmd/root result))
      (is (= 0.6 (get-in result [:rotary :axis :azimuth :rotate :speed])))
      (is (= 0.4 (get-in result [:rotary :axis :elevation :rotate :speed])))
      (let [roundtrip-result (validation/validate-roundtrip-with-report result)]
        (is (:valid? roundtrip-result)))))

  (testing "rotate-both-relative creates valid command"
    (let [result (rotary/rotate-both-relative
                   45.0 0.5 :JON_GUI_DATA_ROTARY_DIRECTION_CLOCKWISE
                   -30.0 0.3 :JON_GUI_DATA_ROTARY_DIRECTION_COUNTER_CLOCKWISE)]
      (is (m/validate :cmd/root result))
      (is (= 45.0 (get-in result [:rotary :axis :azimuth :relative :value])))
      (is (= -30.0 (get-in result [:rotary :axis :elevation :relative :value])))
      (let [roundtrip-result (validation/validate-roundtrip-with-report result)]
        (is (:valid? roundtrip-result)))))

  (testing "set-both-values creates valid command"
    (let [result (rotary/set-both-values
                   180.0 :JON_GUI_DATA_ROTARY_DIRECTION_CLOCKWISE
                   45.0)]
      (is (m/validate :cmd/root result))
      (is (= 180.0 (get-in result [:rotary :axis :azimuth :set_value :value])))
      (is (= 45.0 (get-in result [:rotary :axis :elevation :set_value :value])))
      (let [roundtrip-result (validation/validate-roundtrip-with-report result)]
        (is (:valid? roundtrip-result))))))

;; ============================================================================
;; GPS Integration Tests
;; ============================================================================

(deftest test-gps-integration
  (testing "rotate-to-gps creates valid command"
    (let [result (rotary/rotate-to-gps 40.7128 -74.0060 100.0)]
      (is (m/validate :cmd/root result))
      (is (= 40.7128 (get-in result [:rotary :rotate_to_gps :latitude])))
      (is (= -74.0060 (get-in result [:rotary :rotate_to_gps :longitude])))
      (is (= 100.0 (get-in result [:rotary :rotate_to_gps :altitude])))
      (let [roundtrip-result (validation/validate-roundtrip-with-report result)]
        (is (:valid? roundtrip-result)))))

  (testing "set-origin-gps creates valid command"
    (let [result (rotary/set-origin-gps 51.5074 -0.1278 50.0)]
      (is (m/validate :cmd/root result))
      (is (= 51.5074 (get-in result [:rotary :set_origin_gps :latitude])))
      (is (= -0.1278 (get-in result [:rotary :set_origin_gps :longitude])))
      (is (= 50.0 (get-in result [:rotary :set_origin_gps :altitude])))
      (let [roundtrip-result (validation/validate-roundtrip-with-report result)]
        (is (:valid? roundtrip-result))))))

;; ============================================================================
;; NDC Control Tests
;; ============================================================================

(deftest test-ndc-control
  (testing "rotate-to-ndc creates valid command"
    (let [frame-time 123456789
          result (rotary/rotate-to-ndc :JON_GUI_DATA_VIDEO_CHANNEL_DAY 0.5 -0.5 frame-time)]
      (is (m/validate :cmd/root result))
      (is (= :JON_GUI_DATA_VIDEO_CHANNEL_DAY (get-in result [:rotary :rotate_to_ndc :channel])))
      (is (= 0.5 (get-in result [:rotary :rotate_to_ndc :x])))
      (is (= -0.5 (get-in result [:rotary :rotate_to_ndc :y])))
      (is (= frame-time (get-in result [:rotary :rotate_to_ndc :frame_time])))
      (is (number? (get-in result [:rotary :rotate_to_ndc :state_time])))
      (let [roundtrip-result (validation/validate-roundtrip-with-report result)]
        (is (:valid? roundtrip-result)))))
  
  (testing "halt-with-ndc creates valid command"
    (let [frame-time 987654321
          result (rotary/halt-with-ndc :JON_GUI_DATA_VIDEO_CHANNEL_HEAT -0.75 0.25 frame-time)]
      (is (m/validate :cmd/root result))
      (is (= :JON_GUI_DATA_VIDEO_CHANNEL_HEAT (get-in result [:rotary :halt_with_ndc :channel])))
      (is (= -0.75 (get-in result [:rotary :halt_with_ndc :x])))
      (is (= 0.25 (get-in result [:rotary :halt_with_ndc :y])))
      (is (= frame-time (get-in result [:rotary :halt_with_ndc :frame_time])))
      (is (number? (get-in result [:rotary :halt_with_ndc :state_time])))
      (let [roundtrip-result (validation/validate-roundtrip-with-report result)]
        (is (:valid? roundtrip-result))))))

;; ============================================================================
;; Scan Operations Tests
;; ============================================================================

(deftest test-scan-operations
  (testing "scan-start creates valid command"
    (let [result (rotary/scan-start)]
      (is (m/validate :cmd/root result))
      (is (= {} (get-in result [:rotary :scan_start])))
      (let [roundtrip-result (validation/validate-roundtrip-with-report result)]
        (is (:valid? roundtrip-result)))))

  (testing "scan-stop creates valid command"
    (let [result (rotary/scan-stop)]
      (is (m/validate :cmd/root result))
      (is (= {} (get-in result [:rotary :scan_stop])))
      (let [roundtrip-result (validation/validate-roundtrip-with-report result)]
        (is (:valid? roundtrip-result)))))

  (testing "scan-pause creates valid command"
    (let [result (rotary/scan-pause)]
      (is (m/validate :cmd/root result))
      (is (= {} (get-in result [:rotary :scan_pause])))
      (let [roundtrip-result (validation/validate-roundtrip-with-report result)]
        (is (:valid? roundtrip-result)))))

  (testing "scan-unpause creates valid command"
    (let [result (rotary/scan-unpause)]
      (is (m/validate :cmd/root result))
      (is (= {} (get-in result [:rotary :scan_unpause])))
      (let [roundtrip-result (validation/validate-roundtrip-with-report result)]
        (is (:valid? roundtrip-result)))))

  (testing "scan-prev creates valid command"
    (let [result (rotary/scan-prev)]
      (is (m/validate :cmd/root result))
      (is (= {} (get-in result [:rotary :scan_prev])))
      (let [roundtrip-result (validation/validate-roundtrip-with-report result)]
        (is (:valid? roundtrip-result)))))

  (testing "scan-next creates valid command"
    (let [result (rotary/scan-next)]
      (is (m/validate :cmd/root result))
      (is (= {} (get-in result [:rotary :scan_next])))
      (let [roundtrip-result (validation/validate-roundtrip-with-report result)]
        (is (:valid? roundtrip-result)))))

  (testing "scan-refresh-node-list creates valid command"
    (let [result (rotary/scan-refresh-node-list)]
      (is (m/validate :cmd/root result))
      (is (= {} (get-in result [:rotary :scan_refresh_node_list])))
      (let [roundtrip-result (validation/validate-roundtrip-with-report result)]
        (is (:valid? roundtrip-result)))))

  (testing "scan-select-node creates valid command"
    (let [result (rotary/scan-select-node 5)]
      (is (m/validate :cmd/root result))
      (is (= 5 (get-in result [:rotary :scan_select_node :index])))
      (let [roundtrip-result (validation/validate-roundtrip-with-report result)]
        (is (:valid? roundtrip-result)))))

  (testing "scan-delete-node creates valid command"
    (let [result (rotary/scan-delete-node 3)]
      (is (m/validate :cmd/root result))
      (is (= 3 (get-in result [:rotary :scan_delete_node :index])))
      (let [roundtrip-result (validation/validate-roundtrip-with-report result)]
        (is (:valid? roundtrip-result)))))

  (testing "scan-update-node creates valid command"
    (let [result (rotary/scan-update-node 2 10 15 180.0 45.0 5.0 0.5)]
      (is (m/validate :cmd/root result))
      (is (= 2 (get-in result [:rotary :scan_update_node :index])))
      (is (= 10 (get-in result [:rotary :scan_update_node :DayZoomTableValue])))
      (is (= 15 (get-in result [:rotary :scan_update_node :HeatZoomTableValue])))
      (is (= 180.0 (get-in result [:rotary :scan_update_node :azimuth])))
      (is (= 45.0 (get-in result [:rotary :scan_update_node :elevation])))
      (is (= 5.0 (get-in result [:rotary :scan_update_node :linger])))
      (is (= 0.5 (get-in result [:rotary :scan_update_node :speed])))
      (let [roundtrip-result (validation/validate-roundtrip-with-report result)]
        (is (:valid? roundtrip-result)))))

  (testing "scan-add-node creates valid command"
    (let [result (rotary/scan-add-node 1 20 25 90.0 -30.0 3.0 0.75)]
      (is (m/validate :cmd/root result))
      (is (= 1 (get-in result [:rotary :scan_add_node :index])))
      (is (= 20 (get-in result [:rotary :scan_add_node :DayZoomTableValue])))
      (is (= 25 (get-in result [:rotary :scan_add_node :HeatZoomTableValue])))
      (is (= 90.0 (get-in result [:rotary :scan_add_node :azimuth])))
      (is (= -30.0 (get-in result [:rotary :scan_add_node :elevation])))
      (is (= 3.0 (get-in result [:rotary :scan_add_node :linger])))
      (is (= 0.75 (get-in result [:rotary :scan_add_node :speed])))
      (let [roundtrip-result (validation/validate-roundtrip-with-report result)]
        (is (:valid? roundtrip-result))))))

;; ============================================================================
;; Meteo Data Tests
;; ============================================================================

(deftest test-meteo
  (testing "get-meteo creates valid command"
    (let [result (rotary/get-meteo)]
      (is (m/validate :cmd/root result))
      (is (= {} (get-in result [:rotary :get_meteo])))
      (let [roundtrip-result (validation/validate-roundtrip-with-report result)]
        (is (:valid? roundtrip-result))))))

;; ============================================================================
;; Generative Testing
;; ============================================================================

