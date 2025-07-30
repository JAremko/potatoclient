(ns potatoclient.state.edn-validation-test
  "Tests for EDN state validation to ensure spec violations are caught"
  (:require [clojure.test :refer [deftest testing is]]
            [potatoclient.state.edn-validation :as validation]
            [malli.core :as m]))

(deftest test-system-validation
  (testing "Valid system data passes validation"
    (let [valid-system {:cpu-temperature 45.5
                        :gpu-temperature 60.0
                        :gpu-load 75.0
                        :cpu-load 50.0
                        :power-consumption 150.0
                        :loc "JON_GUI_DATA_SYSTEM_LOCALIZATION_EN"
                        :cur-video-rec-dir-year 2024
                        :cur-video-rec-dir-month 1
                        :cur-video-rec-dir-day 15
                        :cur-video-rec-dir-hour 14
                        :cur-video-rec-dir-minute 30
                        :cur-video-rec-dir-second 45
                        :rec-enabled true
                        :important-rec-enabled false
                        :low-disk-space false
                        :no-disk-space false
                        :disk-space 75
                        :tracking true
                        :vampire-mode false
                        :stabilization-mode true
                        :geodesic-mode false
                        :cv-dumping false}]
      (is (= valid-system (validation/validate-subsystem :system valid-system)))))

  (testing "Invalid temperature below absolute zero fails"
    (let [invalid-system {:cpu-temperature -300.0  ; Below absolute zero
                          :gpu-temperature 60.0
                          :gpu-load 75.0
                          :cpu-load 50.0
                          :power-consumption 150.0
                          :loc "JON_GUI_DATA_SYSTEM_LOCALIZATION_EN"
                          :cur-video-rec-dir-year 2024
                          :cur-video-rec-dir-month 1
                          :cur-video-rec-dir-day 15
                          :cur-video-rec-dir-hour 14
                          :cur-video-rec-dir-minute 30
                          :cur-video-rec-dir-second 45
                          :rec-enabled true
                          :important-rec-enabled false
                          :low-disk-space false
                          :no-disk-space false
                          :disk-space 75
                          :tracking true
                          :vampire-mode false
                          :stabilization-mode true
                          :geodesic-mode false
                          :cv-dumping false}]
      (is (nil? (validation/validate-subsystem :system invalid-system)))))

  (testing "Invalid temperature above max fails"
    (let [invalid-system (assoc (first (:valid-system (meta #'test-system-validation)))
                                :gpu-temperature 200.0)]  ; Above 150 max
      (is (nil? (validation/validate-subsystem :system invalid-system)))))

  (testing "Invalid disk space percentage fails"
    (let [invalid-system (assoc (first (:valid-system (meta #'test-system-validation)))
                                :disk-space 150)]  ; Above 100%
      (is (nil? (validation/validate-subsystem :system invalid-system)))))

  (testing "Invalid enum value fails"
    (let [invalid-system (assoc (first (:valid-system (meta #'test-system-validation)))
                                :loc "INVALID_LOCALIZATION")]
      (is (nil? (validation/validate-subsystem :system invalid-system))))))

(deftest test-gps-validation
  (testing "Valid GPS data passes validation"
    (let [valid-gps {:longitude 30.5234
                     :latitude 50.4501
                     :altitude 150.0
                     :manual-longitude 0.0
                     :manual-latitude 0.0
                     :manual-altitude 0.0
                     :fix-type "JON_GUI_DATA_GPS_FIX_TYPE_3D"
                     :use-manual false}]
      (is (= valid-gps (validation/validate-subsystem :gps valid-gps)))))

  (testing "Invalid longitude fails"
    (let [invalid-gps {:longitude 200.0  ; > 180
                       :latitude 50.4501
                       :altitude 150.0
                       :manual-longitude 0.0
                       :manual-latitude 0.0
                       :manual-altitude 0.0
                       :fix-type "JON_GUI_DATA_GPS_FIX_TYPE_3D"
                       :use-manual false}]
      (is (nil? (validation/validate-subsystem :gps invalid-gps)))))

  (testing "Invalid latitude fails"
    (let [invalid-gps {:longitude 30.5234
                       :latitude -100.0  ; < -90
                       :altitude 150.0
                       :manual-longitude 0.0
                       :manual-latitude 0.0
                       :manual-altitude 0.0
                       :fix-type "JON_GUI_DATA_GPS_FIX_TYPE_3D"
                       :use-manual false}]
      (is (nil? (validation/validate-subsystem :gps invalid-gps)))))

  (testing "Invalid altitude below Dead Sea fails"
    (let [invalid-gps {:longitude 30.5234
                       :latitude 50.4501
                       :altitude -500.0  ; Below Dead Sea
                       :manual-longitude 0.0
                       :manual-latitude 0.0
                       :manual-altitude 0.0
                       :fix-type "JON_GUI_DATA_GPS_FIX_TYPE_3D"
                       :use-manual false}]
      (is (nil? (validation/validate-subsystem :gps invalid-gps)))))

  (testing "Invalid altitude above Everest fails"
    (let [invalid-gps {:longitude 30.5234
                       :latitude 50.4501
                       :altitude 9000.0  ; Above Everest
                       :manual-longitude 0.0
                       :manual-latitude 0.0
                       :manual-altitude 0.0
                       :fix-type "JON_GUI_DATA_GPS_FIX_TYPE_3D"
                       :use-manual false}]
      (is (nil? (validation/validate-subsystem :gps invalid-gps))))))

(deftest test-compass-validation
  (testing "Valid compass data passes validation"
    (let [valid-compass {:azimuth 180.0
                         :elevation 45.0
                         :bank 90.0
                         :offset-azimuth 30.0
                         :offset-elevation 15.0
                         :magnetic-declination 10.0
                         :calibrating false}]
      (is (= valid-compass (validation/validate-subsystem :compass valid-compass)))))

  (testing "Invalid azimuth >= 360 fails"
    (let [invalid-compass {:azimuth 360.0  ; Should be < 360
                           :elevation 45.0
                           :bank 90.0
                           :offset-azimuth 30.0
                           :offset-elevation 15.0
                           :magnetic-declination 10.0
                           :calibrating false}]
      (is (nil? (validation/validate-subsystem :compass invalid-compass)))))

  (testing "Invalid elevation > 90 fails"
    (let [invalid-compass {:azimuth 180.0
                           :elevation 95.0  ; > 90
                           :bank 90.0
                           :offset-azimuth 30.0
                           :offset-elevation 15.0
                           :magnetic-declination 10.0
                           :calibrating false}]
      (is (nil? (validation/validate-subsystem :compass invalid-compass))))))

(deftest test-camera-day-validation
  (testing "Valid day camera data passes validation"
    (let [valid-camera {:focus-pos 0.5
                        :zoom-pos 0.75
                        :iris-pos 0.3
                        :infrared-filter true
                        :zoom-table-pos 10
                        :zoom-table-pos-max 20
                        :fx-mode "JON_GUI_DATA_FX_MODE_DAY_DEFAULT"
                        :auto-focus true
                        :auto-iris false
                        :digital-zoom-level 2.5
                        :clahe-level 0.6}]
      (is (= valid-camera (validation/validate-subsystem :camera-day valid-camera)))))

  (testing "Invalid position > 1.0 fails"
    (let [invalid-camera {:focus-pos 1.5  ; > 1.0
                          :zoom-pos 0.75
                          :iris-pos 0.3
                          :infrared-filter true
                          :zoom-table-pos 10
                          :zoom-table-pos-max 20
                          :fx-mode "JON_GUI_DATA_FX_MODE_DAY_DEFAULT"
                          :auto-focus true
                          :auto-iris false
                          :digital-zoom-level 2.5
                          :clahe-level 0.6}]
      (is (nil? (validation/validate-subsystem :camera-day invalid-camera)))))

  (testing "Invalid digital zoom < 1.0 fails"
    (let [invalid-camera {:focus-pos 0.5
                          :zoom-pos 0.75
                          :iris-pos 0.3
                          :infrared-filter true
                          :zoom-table-pos 10
                          :zoom-table-pos-max 20
                          :fx-mode "JON_GUI_DATA_FX_MODE_DAY_DEFAULT"
                          :auto-focus true
                          :auto-iris false
                          :digital-zoom-level 0.5  ; < 1.0
                          :clahe-level 0.6}]
      (is (nil? (validation/validate-subsystem :camera-day invalid-camera))))))

(deftest test-complete-state-validation
  (testing "Valid complete state passes validation"
    (let [valid-state {:protocol-version 1
                       :system {:cpu-temperature 45.5
                                :gpu-temperature 60.0
                                :gpu-load 75.0
                                :cpu-load 50.0
                                :power-consumption 150.0
                                :loc "JON_GUI_DATA_SYSTEM_LOCALIZATION_EN"
                                :cur-video-rec-dir-year 2024
                                :cur-video-rec-dir-month 1
                                :cur-video-rec-dir-day 15
                                :cur-video-rec-dir-hour 14
                                :cur-video-rec-dir-minute 30
                                :cur-video-rec-dir-second 45
                                :rec-enabled true
                                :important-rec-enabled false
                                :low-disk-space false
                                :no-disk-space false
                                :disk-space 75
                                :tracking true
                                :vampire-mode false
                                :stabilization-mode true
                                :geodesic-mode false
                                :cv-dumping false}
                       :time {:timestamp 1705337400
                              :manual-timestamp 0
                              :zone-id 0
                              :use-manual-time false}}]
      (is (= valid-state (validation/validate-edn-state valid-state)))))

  (testing "Invalid protocol version fails"
    (let [invalid-state {:protocol-version 0  ; Must be positive
                         :system {:cpu-temperature 45.5
                                  :gpu-temperature 60.0
                                  :gpu-load 75.0
                                  :cpu-load 50.0
                                  :power-consumption 150.0
                                  :loc "JON_GUI_DATA_SYSTEM_LOCALIZATION_EN"
                                  :cur-video-rec-dir-year 2024
                                  :cur-video-rec-dir-month 1
                                  :cur-video-rec-dir-day 15
                                  :cur-video-rec-dir-hour 14
                                  :cur-video-rec-dir-minute 30
                                  :cur-video-rec-dir-second 45
                                  :rec-enabled true
                                  :important-rec-enabled false
                                  :low-disk-space false
                                  :no-disk-space false
                                  :disk-space 75
                                  :tracking true
                                  :vampire-mode false
                                  :stabilization-mode true
                                  :geodesic-mode false
                                  :cv-dumping false}}]
      (is (nil? (validation/validate-edn-state invalid-state)))))

  (testing "Unknown subsystem key fails with closed map"
    (let [invalid-state {:protocol-version 1
                         :unknown-subsystem {}}]  ; Not allowed
      (is (nil? (validation/validate-edn-state invalid-state))))))

(deftest test-validation-enabled
  (testing "Validation is disabled by default"
    (is (not (validation/validation-enabled?))))

  (testing "Validation can be enabled via system property"
    (System/setProperty "potatoclient.validation.enabled" "true")
    (is (validation/validation-enabled?))
    ;; Reset
    (System/clearProperty "potatoclient.validation.enabled")))

(deftest test-human-readable-errors
  (testing "Validation errors produce human-readable explanations"
    (let [invalid-state {:protocol-version 1
                         :system {:cpu-temperature -300.0}}  ; Invalid temperature
          error-msg (validation/explain-validation-error invalid-state)]
      (is (string? error-msg))
      (is (re-find #"cpu-temperature" error-msg)))))