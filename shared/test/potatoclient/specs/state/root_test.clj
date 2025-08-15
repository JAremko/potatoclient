(ns potatoclient.specs.state.root-test
  "Hardcoded tests for state.Root spec validation.
   Tests specific edge cases and negative scenarios."
  (:require
   [clojure.test :refer [deftest is testing]]
   [malli.core :as m]
   [malli.generator :as mg]
   [malli.error :as me]
   [potatoclient.malli.registry :as registry]))

;; Initialize registry with all specs
(registry/setup-global-registry!)

(deftest state-valid-messages
  (let [state-spec (m/schema :state/root)
        ;; Generate a complete valid message and override specific fields
        base-message (mg/generate state-spec)
        valid-message-1 (merge base-message
                              {:protocol_version 1
                               :gps {:latitude 45.5
                                     :longitude -122.6
                                     :altitude 100.5
                                     :manual_latitude 45.5
                                     :manual_longitude -122.6
                                     :manual_altitude 100.5
                                     :fix_type :JON_GUI_DATA_GPS_FIX_TYPE_3D
                                     :use_manual false}
                               :compass {:azimuth 180.0
                                         :elevation 10.0
                                         :bank 0.0
                                         :offsetAzimuth 5.0
                                         :offsetElevation -5.0
                                         :magneticDeclination 12.5
                                         :calibrating false}
                               :system {:cpu_temperature 45.0
                                        :gpu_temperature 55.0
                                        :gpu_load 75.0
                                        :cpu_load 60.0
                                        :power_consumption 150.0
                                        :loc :JON_GUI_DATA_SYSTEM_LOCALIZATION_EN
                                        :cur_video_rec_dir_year 2025
                                        :cur_video_rec_dir_month 1
                                        :cur_video_rec_dir_day 14
                                        :cur_video_rec_dir_hour 10
                                        :cur_video_rec_dir_minute 30
                                        :cur_video_rec_dir_second 45
                                        :rec_enabled true
                                        :important_rec_enabled false
                                        :low_disk_space false
                                        :no_disk_space false
                                        :disk_space 75
                                        :tracking true
                                        :vampire_mode false
                                        :stabilization_mode true
                                        :geodesic_mode false
                                        :cv_dumping false
                                        :recognition_mode false}
                               :rotary {:azimuth 270.0
                                        :azimuth_speed 0.5
                                        :elevation 45.0
                                        :elevation_speed -0.25
                                        :platform_azimuth 270.0
                                        :platform_elevation 45.0
                                        :platform_bank 0.0
                                        :is_moving true
                                        :mode :JON_GUI_DATA_ROTARY_MODE_SPEED
                                        :is_scanning false
                                        :is_scanning_paused false
                                        :use_rotary_as_compass false
                                        :scan_target 0
                                        :scan_target_max 10
                                        :sun_azimuth 180.0
                                        :sun_elevation 45.0
                                        :current_scan_node {:index 0
                                                           :DayZoomTableValue 1
                                                           :HeatZoomTableValue 1
                                                           :azimuth 0.0
                                                           :elevation 0.0
                                                           :linger 1.0
                                                           :speed 0.5}}})]
    
    (testing "Valid message with specific GPS, compass, system, and rotary values"
      (is (m/validate state-spec valid-message-1)
          "Message should be valid"))))

(deftest state-invalid-gps
  (let [state-spec (m/schema :state/root)
        base-message (mg/generate state-spec)
        invalid-gps-message
        (assoc base-message
               :protocol_version 1
               :gps {:latitude 91.0  ; Invalid: > 90
                     :longitude -181.0 ; Invalid: < -180
                     :altitude -500.0  ; Invalid: < -432
                     :manual_latitude 0.0
                     :manual_longitude 0.0
                     :manual_altitude 0.0
                     :fix_type :JON_GUI_DATA_GPS_FIX_TYPE_3D
                     :use_manual false})]
    
    (testing "Invalid GPS coordinates should fail"
      (is (not (m/validate state-spec invalid-gps-message)))
      
      (when-let [explanation (m/explain state-spec invalid-gps-message)]
        (let [humanized (me/humanize explanation)]
          (is (get-in humanized [:gps :latitude])
              "Should have error for invalid latitude")
          (is (get-in humanized [:gps :longitude])
              "Should have error for invalid longitude")
          (is (get-in humanized [:gps :altitude])
              "Should have error for invalid altitude"))))))

(deftest state-invalid-compass
  (let [state-spec (m/schema :state/root)
        base-message (mg/generate state-spec)
        invalid-compass-message
        (assoc base-message
               :protocol_version 1
               :compass {:azimuth 360.0  ; Invalid: must be < 360
                         :elevation 91.0  ; Invalid: > 90
                         :bank 180.0      ; Invalid: must be < 180
                         :offsetAzimuth 180.0  ; Invalid: must be < 180
                         :offsetElevation 91.0  ; Invalid: > 90
                         :magneticDeclination 180.0  ; Invalid: must be < 180
                         :calibrating false})]
    
    (testing "Invalid compass angles should fail"
      (is (not (m/validate state-spec invalid-compass-message))))))

(deftest state-invalid-lrf
  (let [state-spec (m/schema :state/root)
        base-message (mg/generate state-spec)
        invalid-lrf-message
        (assoc base-message
               :protocol_version 1
               :lrf {:is_scanning true
                     :is_measuring false
                     :measure_id -1  ; Invalid: must be >= 0
                     :target {:timestamp -1  ; Invalid: must be >= 0
                              :target_longitude 181.0  ; Invalid: > 180
                              :target_latitude -91.0    ; Invalid: < -90
                              :target_altitude 0.0
                              :observer_longitude 0.0
                              :observer_latitude 0.0
                              :observer_altitude 0.0
                              :observer_azimuth 360.0  ; Invalid: must be < 360
                              :observer_elevation -91.0 ; Invalid: < -90
                              :observer_bank 180.0      ; Invalid: must be < 180
                              :distance_2d -100.0       ; Invalid: must be >= 0
                              :distance_3b 50001.0      ; Invalid: > 50000
                              :observer_fix_type :JON_GUI_DATA_GPS_FIX_TYPE_3D
                              :session_id -1            ; Invalid: must be >= 0
                              :target_id -1             ; Invalid: must be >= 0
                              :target_color {:red 256   ; Invalid: > 255
                                            :green -1    ; Invalid: < 0
                                            :blue 300}   ; Invalid: > 255
                              :type 0
                              :uuid_part1 0
                              :uuid_part2 0
                              :uuid_part3 0
                              :uuid_part4 0}
                     :pointer_mode :JON_GUI_DATA_LRF_LASER_POINTER_MODE_OFF
                     :fogModeEnabled false
                     :is_refining false})]
    
    (testing "Invalid nested LRF target should fail"
      (is (not (m/validate state-spec invalid-lrf-message))))))

(deftest state-invalid-system
  (let [state-spec (m/schema :state/root)
        base-message (mg/generate state-spec)
        invalid-system-message
        (assoc base-message
               :protocol_version 1
               :system {:cpu_temperature -274.0  ; Invalid: below absolute zero
                        :gpu_temperature 151.0    ; Invalid: > 150
                        :gpu_load 101.0          ; Invalid: > 100
                        :cpu_load -1.0           ; Invalid: < 0
                        :power_consumption 1001.0 ; Invalid: > 1000
                        :loc :INVALID_LOCALIZATION ; Invalid enum
                        :cur_video_rec_dir_year -1  ; Invalid: < 0
                        :cur_video_rec_dir_month 13 ; Invalid: month > 12
                        :cur_video_rec_dir_day 32   ; Invalid: day > 31
                        :cur_video_rec_dir_hour 25  ; Invalid: hour > 23
                        :cur_video_rec_dir_minute 61 ; Invalid: minute > 59
                        :cur_video_rec_dir_second 60 ; Invalid: second > 59
                        :rec_enabled true
                        :important_rec_enabled false
                        :low_disk_space false
                        :no_disk_space false
                        :disk_space 101  ; Invalid: > 100
                        :tracking true
                        :vampire_mode false
                        :stabilization_mode true
                        :geodesic_mode false
                        :cv_dumping false
                        :recognition_mode false})]
    
    (testing "Invalid system values should fail"
      (is (not (m/validate state-spec invalid-system-message))))))

(deftest validate-specific-constraints
  (testing "Malli specs enforce buf.validate-compatible constraints"
    
    (testing "GPS constraints"
      (let [gps-spec (m/schema [:map {:closed true}
                                [:latitude :position/latitude]
                                [:longitude :position/longitude]
                                [:altitude :position/altitude]])]
        
        ;; Valid GPS data
        (is (m/validate gps-spec {:latitude 45.0
                                  :longitude -122.0
                                  :altitude 100.0})
            "Valid GPS data should pass")
        
        ;; Invalid latitude (> 90)
        (is (not (m/validate gps-spec {:latitude 91.0
                                       :longitude 0.0
                                       :altitude 0.0}))
            "Latitude > 90 should fail")
        
        ;; Invalid longitude (>= 180) - Note: spec requires < 180
        (is (not (m/validate gps-spec {:latitude 0.0
                                       :longitude 180.0
                                       :altitude 0.0}))
            "Longitude >= 180 should fail")))
    
    (testing "Nested rotary scan node constraints"
      (let [scan-node-spec (m/schema :rotary/scan-node)]
        
        ;; Valid scan node
        (is (m/validate scan-node-spec
                       {:index 5
                        :DayZoomTableValue 10
                        :HeatZoomTableValue 8
                        :azimuth 180.0
                        :elevation 45.0
                        :linger 2.0
                        :speed 0.75})
            "Valid scan node should pass")
        
        ;; Invalid scan node
        (is (not (m/validate scan-node-spec
                            {:index -1  ; Invalid: < 0
                             :DayZoomTableValue -1  ; Invalid: < 0
                             :HeatZoomTableValue -1 ; Invalid: < 0
                             :azimuth 360.0  ; Invalid: >= 360
                             :elevation 91.0  ; Invalid: > 90
                             :linger -1.0     ; Invalid: < 0
                             :speed 0.0}))    ; Invalid: must be > 0
            "Invalid scan node should fail")))))