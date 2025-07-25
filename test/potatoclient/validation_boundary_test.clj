(ns potatoclient.validation-boundary-test
  "Comprehensive boundary condition tests for all Malli schemas.
   Tests exact boundary values to ensure validation matches protobuf constraints."
  (:require [clojure.test :refer [deftest testing is]]
            [malli.core :as m]
            [potatoclient.specs :as specs]
            [potatoclient.state.schemas :as state-schemas]))

;; ============================================================================
;; Angle Boundary Tests
;; ============================================================================

(deftest angle-boundary-tests
  (testing "Azimuth angles [0, 360)"
    ;; Command spec
    (is (m/validate ::specs/azimuth-degrees 0.0))
    (is (m/validate ::specs/azimuth-degrees 180.0))
    (is (m/validate ::specs/azimuth-degrees 359.999999))
    (is (not (m/validate ::specs/azimuth-degrees 360.0)))
    (is (not (m/validate ::specs/azimuth-degrees -0.1)))

    ;; State spec
    (is (m/validate state-schemas/angle-azimuth 0.0))
    (is (m/validate state-schemas/angle-azimuth 359.999999))
    (is (not (m/validate state-schemas/angle-azimuth 360.0))))

  (testing "Elevation angles [-90, 90]"
    (is (m/validate ::specs/elevation-degrees -90.0))
    (is (m/validate ::specs/elevation-degrees 0.0))
    (is (m/validate ::specs/elevation-degrees 90.0))
    (is (not (m/validate ::specs/elevation-degrees -90.1)))
    (is (not (m/validate ::specs/elevation-degrees 90.1))))

  (testing "Bank angles [-180, 180)"
    (is (m/validate ::specs/bank-angle -180.0))
    (is (m/validate ::specs/bank-angle 0.0))
    (is (m/validate ::specs/bank-angle 179.999999))
    (is (not (m/validate ::specs/bank-angle 180.0)))
    (is (not (m/validate ::specs/bank-angle -180.1))))

  (testing "Relative angles (-360, 360)"
    (is (m/validate ::specs/relative-angle -359.999999))
    (is (m/validate ::specs/relative-angle 0.0))
    (is (m/validate ::specs/relative-angle 359.999999))
    (is (not (m/validate ::specs/relative-angle -360.0)))
    (is (not (m/validate ::specs/relative-angle 360.0))))

  (testing "Platform angles"
    ;; Platform azimuth (-360, 360)
    (is (m/validate ::specs/platform-azimuth -359.999999))
    (is (m/validate ::specs/platform-azimuth 359.999999))
    (is (not (m/validate ::specs/platform-azimuth -360.0)))
    (is (not (m/validate ::specs/platform-azimuth 360.0)))

    ;; Platform elevation [-90, 90]
    (is (m/validate ::specs/platform-elevation -90.0))
    (is (m/validate ::specs/platform-elevation 90.0))

    ;; Platform bank [-180, 180)
    (is (m/validate ::specs/platform-bank -180.0))
    (is (m/validate ::specs/platform-bank 179.999999))
    (is (not (m/validate ::specs/platform-bank 180.0)))))

;; ============================================================================
;; GPS Boundary Tests
;; ============================================================================

(deftest gps-boundary-tests
  (testing "GPS latitude [-90, 90]"
    (is (m/validate ::specs/gps-latitude -90.0))
    (is (m/validate ::specs/gps-latitude 0.0))
    (is (m/validate ::specs/gps-latitude 90.0))
    (is (not (m/validate ::specs/gps-latitude -90.1)))
    (is (not (m/validate ::specs/gps-latitude 90.1))))

  (testing "GPS longitude [-180, 180)"
    (is (m/validate ::specs/gps-longitude -180.0))
    (is (m/validate ::specs/gps-longitude 0.0))
    (is (m/validate ::specs/gps-longitude 179.999999))
    (is (not (m/validate ::specs/gps-longitude 180.0)))
    (is (not (m/validate ::specs/gps-longitude -180.1))))

  (testing "GPS altitude [-433, 8848.86]"
    (is (m/validate ::specs/gps-altitude -433.0))     ; Dead Sea
    (is (m/validate ::specs/gps-altitude 0.0))
    (is (m/validate ::specs/gps-altitude 8848.86))   ; Mt. Everest
    (is (not (m/validate ::specs/gps-altitude -433.1)))
    (is (not (m/validate ::specs/gps-altitude 8848.87)))))

;; ============================================================================
;; Normalized Value Tests
;; ============================================================================

(deftest normalized-value-tests
  (testing "Normalized values [0, 1]"
    (doseq [spec [::specs/normalized-value
                  ::specs/rotation-speed
                  ::specs/focus-value
                  ::specs/iris-value
                  ::specs/clahe-level
                  state-schemas/normalized-value]]
      (is (m/validate spec 0.0))
      (is (m/validate spec 0.5))
      (is (m/validate spec 1.0))
      (is (not (m/validate spec -0.1)))
      (is (not (m/validate spec 1.1)))))

  (testing "CLAHE shift [-1, 1]"
    (is (m/validate ::specs/clahe-shift -1.0))
    (is (m/validate ::specs/clahe-shift 0.0))
    (is (m/validate ::specs/clahe-shift 1.0))
    (is (not (m/validate ::specs/clahe-shift -1.1)))
    (is (not (m/validate ::specs/clahe-shift 1.1)))))

;; ============================================================================
;; LRF Offset Tests
;; ============================================================================

(deftest lrf-offset-tests
  (testing "LRF X offset [-1920, 1920]"
    (is (m/validate ::specs/offset-value -1920))
    (is (m/validate ::specs/offset-value 0))
    (is (m/validate ::specs/offset-value 1920))
    (is (not (m/validate ::specs/offset-value -1921)))
    (is (not (m/validate ::specs/offset-value 1921))))

  (testing "LRF Y offset [-1080, 1080]"
    (is (m/validate ::specs/offset-y-value -1080))
    (is (m/validate ::specs/offset-y-value 0))
    (is (m/validate ::specs/offset-y-value 1080))
    (is (not (m/validate ::specs/offset-y-value -1081)))
    (is (not (m/validate ::specs/offset-y-value 1081)))))

;; ============================================================================
;; DDE Tests
;; ============================================================================

(deftest dde-tests
  (testing "DDE level [0, 512]"
    (is (m/validate ::specs/dde-level 0))
    (is (m/validate ::specs/dde-level 256))
    (is (m/validate ::specs/dde-level 512))
    (is (not (m/validate ::specs/dde-level -1)))
    (is (not (m/validate ::specs/dde-level 513))))

  (testing "DDE shift [-100, 100]"
    (is (m/validate ::specs/dde-shift -100))
    (is (m/validate ::specs/dde-shift 0))
    (is (m/validate ::specs/dde-shift 100))
    (is (not (m/validate ::specs/dde-shift -101)))
    (is (not (m/validate ::specs/dde-shift 101)))))

;; ============================================================================
;; Scan Parameter Tests
;; ============================================================================

(deftest scan-parameter-tests
  (testing "Scan speed (0, 1.0]"
    (is (m/validate ::specs/scan-speed 0.0000001))  ; > 0
    (is (m/validate ::specs/scan-speed 0.5))
    (is (m/validate ::specs/scan-speed 1.0))
    (is (not (m/validate ::specs/scan-speed 0.0)))
    (is (not (m/validate ::specs/scan-speed 1.1))))

  (testing "Scan linger time [0, ∞)"
    (is (m/validate ::specs/scan-linger-time 0.0))
    (is (m/validate ::specs/scan-linger-time 10.0))
    (is (m/validate ::specs/scan-linger-time 3600.0))
    (is (not (m/validate ::specs/scan-linger-time -0.1))))

  (testing "Scan node index [0, ∞)"
    (is (m/validate ::specs/scan-node-index 0))
    (is (m/validate ::specs/scan-node-index 100))
    (is (not (m/validate ::specs/scan-node-index -1)))))

;; ============================================================================
;; Special Value Tests
;; ============================================================================

(deftest special-value-tests
  (testing "Protocol version > 0"
    (is (m/validate ::specs/protocol-version 1))
    (is (m/validate ::specs/protocol-version 100))
    (is (not (m/validate ::specs/protocol-version 0)))
    (is (not (m/validate ::specs/protocol-version -1))))

  (testing "Timestamps >= 0"
    (is (m/validate ::specs/timestamp 0))
    (is (m/validate ::specs/timestamp 1234567890))
    (is (not (m/validate ::specs/timestamp -1))))

  (testing "Speed percentage [0, 100]"
    (is (m/validate ::specs/speed-percentage 0))
    (is (m/validate ::specs/speed-percentage 50))
    (is (m/validate ::specs/speed-percentage 100))
    (is (not (m/validate ::specs/speed-percentage -1)))
    (is (not (m/validate ::specs/speed-percentage 101))))

  (testing "Magnetic declination [-180, 180)"
    (is (m/validate ::specs/magnetic-declination -180.0))
    (is (m/validate ::specs/magnetic-declination 0.0))
    (is (m/validate ::specs/magnetic-declination 179.999999))
    (is (not (m/validate ::specs/magnetic-declination 180.0))))

  (testing "NDC coordinates [-1, 1]"
    (is (m/validate ::specs/ndc-coordinate -1.0))
    (is (m/validate ::specs/ndc-coordinate 0.0))
    (is (m/validate ::specs/ndc-coordinate 1.0))
    (is (not (m/validate ::specs/ndc-coordinate -1.1)))
    (is (not (m/validate ::specs/ndc-coordinate 1.1)))))

;; ============================================================================
;; State-specific Tests
;; ============================================================================

(deftest state-specific-tests
  (testing "Temperature [-273.15, 660.32]"
    (is (m/validate state-schemas/temperature-celsius -273.15))  ; Absolute zero
    (is (m/validate state-schemas/temperature-celsius 20.0))
    (is (m/validate state-schemas/temperature-celsius 660.32))   ; Aluminum melting point
    (is (not (m/validate state-schemas/temperature-celsius -273.16)))
    (is (not (m/validate state-schemas/temperature-celsius 660.33))))

  (testing "Sun position (unusual 0-360 for elevation)"
    (is (m/validate state-schemas/sun-azimuth 0.0))
    (is (m/validate state-schemas/sun-azimuth 359.999999))
    (is (not (m/validate state-schemas/sun-azimuth 360.0)))

    ;; Sun elevation uses 0-360 instead of -90 to 90!
    (is (m/validate state-schemas/sun-elevation 0.0))
    (is (m/validate state-schemas/sun-elevation 359.999999))
    (is (not (m/validate state-schemas/sun-elevation 360.0))))

  (testing "Power consumption [0, 1000]"
    (is (m/validate state-schemas/power-consumption 0.0))
    (is (m/validate state-schemas/power-consumption 500.0))
    (is (m/validate state-schemas/power-consumption 1000.0))
    (is (not (m/validate state-schemas/power-consumption -0.1)))
    (is (not (m/validate state-schemas/power-consumption 1000.1))))

  (testing "Disk space percentage [0, 100]"
    (is (m/validate state-schemas/disk-space 0))
    (is (m/validate state-schemas/disk-space 50))
    (is (m/validate state-schemas/disk-space 100))
    (is (not (m/validate state-schemas/disk-space -1)))
    (is (not (m/validate state-schemas/disk-space 101))))

  (testing "Distance in decimeters [0, 500000]"
    (is (m/validate state-schemas/distance-decimeters 0.0))
    (is (m/validate state-schemas/distance-decimeters 250000.0))
    (is (m/validate state-schemas/distance-decimeters 500000.0))  ; 50km
    (is (not (m/validate state-schemas/distance-decimeters -0.1)))
    (is (not (m/validate state-schemas/distance-decimeters 500000.1))))

  (testing "RGB values [0, 255]"
    (is (m/validate state-schemas/rgb-value 0))
    (is (m/validate state-schemas/rgb-value 128))
    (is (m/validate state-schemas/rgb-value 255))
    (is (not (m/validate state-schemas/rgb-value -1)))
    (is (not (m/validate state-schemas/rgb-value 256)))))

;; ============================================================================
;; Zoom Tests
;; ============================================================================

(deftest zoom-tests
  (testing "Camera zoom level [0.0, 1.0]"
    (is (m/validate ::specs/zoom-level 0.0))
    (is (m/validate ::specs/zoom-level 0.5))
    (is (m/validate ::specs/zoom-level 1.0))
    (is (not (m/validate ::specs/zoom-level -0.1)))
    (is (not (m/validate ::specs/zoom-level 1.1))))

  (testing "Digital zoom level [1.0, ∞)"
    (is (m/validate ::specs/digital-zoom-level 1.0))
    (is (m/validate ::specs/digital-zoom-level 10.0))
    (is (m/validate ::specs/digital-zoom-level 1000.0))
    (is (not (m/validate ::specs/digital-zoom-level 0.9)))
    (is (not (m/validate ::specs/digital-zoom-level 0.0))))

  (testing "Zoom table index [0, ∞)"
    (is (m/validate ::specs/zoom-table-index 0))
    (is (m/validate ::specs/zoom-table-index 100))
    (is (not (m/validate ::specs/zoom-table-index -1)))))

;; ============================================================================
;; Compass Calibration Tests
;; ============================================================================

(deftest compass-calibration-tests
  (testing "Calibration stage [0, ∞)"
    (is (m/validate state-schemas/compass-calibration-stage 0))
    (is (m/validate state-schemas/compass-calibration-stage 10))
    (is (not (m/validate state-schemas/compass-calibration-stage -1))))

  (testing "Final calibration stage > 0"
    (is (m/validate state-schemas/compass-calibration-final-stage 1))
    (is (m/validate state-schemas/compass-calibration-final-stage 10))
    (is (not (m/validate state-schemas/compass-calibration-final-stage 0)))
    (is (not (m/validate state-schemas/compass-calibration-final-stage -1)))))

;; ============================================================================
;; Edge Case Float/Double Tests
;; ============================================================================

(deftest edge-case-numeric-tests
  (testing "NaN and Infinity are not valid"
    (doseq [spec [::specs/azimuth-degrees
                  ::specs/elevation-degrees
                  ::specs/gps-latitude
                  ::specs/gps-longitude
                  state-schemas/normalized-value]]
      (is (not (m/validate spec Double/NaN)))
      (is (not (m/validate spec Double/POSITIVE_INFINITY)))
      (is (not (m/validate spec Double/NEGATIVE_INFINITY)))
      (is (not (m/validate spec Float/NaN)))))

  (testing "Very small positive values for gt: 0 constraints"
    (is (m/validate ::specs/scan-speed 0.0000001))
    (is (m/validate ::specs/scan-speed 0.00001))
    (is (not (m/validate ::specs/scan-speed 0.0)))))

;; ============================================================================
;; Type Tests
;; ============================================================================

(deftest type-boundary-tests
  (testing "Integer types reject non-integers"
    (doseq [spec [::specs/offset-value
                  ::specs/offset-y-value
                  ::specs/dde-level
                  ::specs/speed-percentage
                  ::specs/protocol-version]]
      (is (not (m/validate spec 1.5)))
      (is (not (m/validate spec "1")))
      (is (not (m/validate spec nil)))))

  (testing "Float/Double types accept both"
    (is (m/validate ::specs/azimuth-degrees 45.0))
    (is (m/validate ::specs/azimuth-degrees (double 45.0)))))

;; ============================================================================
;; Required Field Tests
;; ============================================================================

(deftest required-field-tests
  (testing "Root state requires all subsystems"
    (let [minimal-valid-state {:protocol-version 1
                               :system {:cpu-temperature 20.0
                                        :gpu-temperature 30.0
                                        :gpu-load 50.0
                                        :cpu-load 40.0
                                        :power-consumption 100.0
                                        :loc "JON_GUI_DATA_SYSTEM_LOCALIZATION_EN"
                                        :cur-video-rec-dir-year 2024
                                        :cur-video-rec-dir-month 7
                                        :cur-video-rec-dir-day 24
                                        :cur-video-rec-dir-hour 12
                                        :cur-video-rec-dir-minute 30
                                        :cur-video-rec-dir-second 0
                                        :rec-enabled false
                                        :important-rec-enabled false
                                        :low-disk-space false
                                        :no-disk-space false
                                        :disk-space 80
                                        :tracking false
                                        :vampire-mode false
                                        :stabilization-mode false
                                        :geodesic-mode false
                                        :cv-dumping false}
                               :meteo-internal {:temperature 25.0
                                                :humidity 60.0
                                                :pressure 1013.25}
                               :lrf {:is-scanning false
                                     :is-measuring false
                                     :measure-id 0
                                     :pointer-mode "JON_GUI_DATA_LRF_LASER_POINTER_MODE_OFF"
                                     :fog-mode-enabled false
                                     :is-refining false}
                               :time {:timestamp 1234567890
                                      :format "JON_GUI_DATA_TIME_FORMAT_H_M_S"}
                               :gps {:longitude 0.0
                                     :latitude 0.0
                                     :altitude 0.0
                                     :manual-longitude 0.0
                                     :manual-latitude 0.0
                                     :manual-altitude 0.0
                                     :fix-type "JON_GUI_DATA_GPS_FIX_TYPE_NONE"
                                     :use-manual false}
                               :compass {:azimuth 0.0
                                         :elevation 0.0
                                         :bank 0.0
                                         :offset-azimuth 0.0
                                         :offset-elevation 0.0
                                         :magnetic-declination 0.0
                                         :calibrating false}
                               :rotary {:azimuth 0.0
                                        :azimuth-speed 0.0
                                        :elevation 0.0
                                        :elevation-speed 0.0
                                        :platform-azimuth 0.0
                                        :platform-elevation 0.0
                                        :platform-bank 0.0
                                        :is-moving false
                                        :mode "JON_GUI_DATA_ROTARY_MODE_INITIALIZATION"
                                        :is-scanning false
                                        :is-scanning-paused false
                                        :use-rotary-as-compass false
                                        :scan-target 0
                                        :scan-target-max 0
                                        :sun-azimuth 0.0
                                        :sun-elevation 0.0
                                        :current-scan-node {:index 0
                                                            :day-zoom-table-value 0
                                                            :heat-zoom-table-value 0
                                                            :azimuth 0.0
                                                            :elevation 0.0
                                                            :linger 0.0
                                                            :speed 0.5}}
                               :camera-day {:focus-pos 0.5
                                            :zoom-pos 0.5
                                            :iris-pos 0.5
                                            :infrared-filter false
                                            :zoom-table-pos 0
                                            :zoom-table-pos-max 10
                                            :fx-mode "JON_GUI_DATA_FX_MODE_DAY_DEFAULT"
                                            :auto-focus false
                                            :auto-iris false
                                            :digital-zoom-level 1.0
                                            :clahe-level 0.0}
                               :camera-heat {:zoom-pos 0.5
                                             :agc-mode "JON_GUI_DATA_VIDEO_CHANNEL_HEAT_AGC_MODE_1"
                                             :filter "JON_GUI_DATA_VIDEO_CHANNEL_HEAT_FILTER_HOT_WHITE"
                                             :auto-focus false
                                             :zoom-table-pos 0
                                             :zoom-table-pos-max 10
                                             :dde-level 0
                                             :dde-enabled false
                                             :fx-mode "JON_GUI_DATA_FX_MODE_HEAT_DEFAULT"
                                             :digital-zoom-level 1.0
                                             :clahe-level 0.0}
                               :compass-calibration {:stage 0
                                                     :final-stage 1
                                                     :target-azimuth 0.0
                                                     :target-elevation 0.0
                                                     :target-bank 0.0
                                                     :status "JON_GUI_DATA_COMPASS_CALIBRATE_STATUS_NOT_CALIBRATING"}
                               :rec-osd {:recording false
                                         :osd-enabled false
                                         :screen "JON_GUI_DATA_REC_OSD_SCREEN_MAIN"}
                               :day-cam-glass-heater {:enabled false
                                                      :auto-mode false}
                               :actual-space-time {:timestamp 0}}]

      ;; Valid state should pass
      (is (m/validate state-schemas/jon-gui-state-schema minimal-valid-state))

      ;; Missing any required field should fail
      (doseq [field [:system :meteo-internal :lrf :time :gps :compass
                     :rotary :camera-day :camera-heat :compass-calibration
                     :rec-osd :day-cam-glass-heater :actual-space-time]]
        (is (not (m/validate state-schemas/jon-gui-state-schema
                             (dissoc minimal-valid-state field)))
            (str field " should be required"))))))