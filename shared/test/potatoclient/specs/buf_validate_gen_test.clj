(ns potatoclient.specs.buf-validate-gen-test
  "Test to verify that generated Malli samples are valid.
   Generates 1000 samples and includes hand-crafted sanity checks."
  (:require
   [clojure.test :refer [deftest is testing]]
   [malli.core :as m]
   [malli.generator :as mg]
   [malli.error :as me]
   [potatoclient.malli.registry :as registry]
   [potatoclient.specs.state.root]))

;; Initialize registry with all specs
(registry/setup-global-registry!)

(deftest sanity-check-hand-crafted-messages
  (testing "Hand-crafted valid nested messages"
    (let [state-spec (m/schema :state/root)
          ;; Generate a base valid sample and merge our test data into it
          base-sample (mg/generate state-spec)]
      
      (testing "Valid complete state message with nested structures"
        (let [valid-message-1
              (merge base-sample
                     {:protocol_version 1
                      :gps (merge (:gps base-sample)
                                  {:latitude 45.5
                                   :longitude -122.6
                                   :altitude 100.5
                                   :manual_latitude 45.5
                                   :manual_longitude -122.6
                                   :manual_altitude 100.5
                                   :fix_type :JON_GUI_DATA_GPS_FIX_TYPE_3D
                                   :use_manual false})
                      :compass (merge (:compass base-sample)
                                      {:azimuth 180.0
                                       :elevation 10.0
                                       :bank 0.0
                                       :offsetAzimuth 5.0
                                       :offsetElevation -5.0
                                       :magneticDeclination 12.5
                                       :calibrating false})
                      :system (merge (:system base-sample)
                                     {:cpu_temperature 45.0
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
                                      :recognition_mode false})
                      :rotary (merge (:rotary base-sample)
                                     {:azimuth 270.0
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
                                                         :speed 0.5}})})
              
              valid-message-2
              (merge base-sample
                     {:protocol_version 2
                      :camera_day (merge (:camera_day base-sample)
                                         {:focus_pos 0.5
                                          :zoom_pos 0.75
                                          :iris_pos 0.25
                                          :infrared_filter false
                                          :zoom_table_pos 5
                                          :zoom_table_pos_max 10
                                          :fx_mode :JON_GUI_DATA_FX_MODE_DAY_DEFAULT
                                          :auto_focus true
                                          :auto_iris true
                                          :digital_zoom_level 2.0
                                          :clahe_level 0.5})
                      :camera_heat (merge (:camera_heat base-sample)
                                          {:zoom_pos 0.5
                                           :agc_mode :JON_GUI_DATA_VIDEO_CHANNEL_HEAT_AGC_MODE_1
                                           :filter :JON_GUI_DATA_VIDEO_CHANNEL_HEAT_FILTER_HOT_WHITE
                                           :auto_focus false
                                           :zoom_table_pos 3
                                           :zoom_table_pos_max 10
                                           :dde_level 256
                                           :dde_enabled true
                                           :fx_mode :JON_GUI_DATA_FX_MODE_HEAT_DEFAULT
                                           :digital_zoom_level 1.5
                                           :clahe_level 0.75})
                      :lrf (merge (:lrf base-sample)
                                  {:is_scanning true
                                   :is_measuring false
                                   :measure_id 42
                                   :target (merge (or (:target (:lrf base-sample)) {})
                                                  {:timestamp 1736849000
                                                   :target_longitude 15.5
                                                   :target_latitude 50.5
                                                   :target_altitude 200.0
                                                   :observer_longitude 15.0
                                                   :observer_latitude 50.0
                                                   :observer_altitude 150.0
                                                   :observer_azimuth 45.0
                                                   :observer_elevation 10.0
                                                   :observer_bank -5.0
                                                   :distance_2d 1000.0
                                                   :distance_3b 1005.0
                                                   :observer_fix_type :JON_GUI_DATA_GPS_FIX_TYPE_3D
                                                   :session_id 1
                                                   :target_id 100
                                                   :target_color {:red 255
                                                                 :green 0
                                                                 :blue 0}
                                                   :type 1
                                                   :uuid_part1 123456
                                                   :uuid_part2 789012
                                                   :uuid_part3 345678
                                                   :uuid_part4 901234})
                                   :pointer_mode :JON_GUI_DATA_LRF_LASER_POINTER_MODE_ON_1
                                   :fogModeEnabled false
                                   :is_refining false})})
              
              valid-message-3
              (merge base-sample
                     {:protocol_version 1
                      :time (merge (:time base-sample)
                                   {:timestamp 1736849000
                                    :manual_timestamp 1736849000
                                    :zone_id 1
                                    :use_manual_time false})
                      :actual_space_time (merge (:actual_space_time base-sample)
                                                {:azimuth 90.0
                                                 :elevation 30.0
                                                 :bank 5.0
                                                 :latitude 40.0
                                                 :longitude 10.0
                                                 :altitude 500.0
                                                 :timestamp 1736849000})
                      :rec_osd (merge (:rec_osd base-sample)
                                      {:screen :JON_GUI_DATA_REC_OSD_SCREEN_MAIN
                                       :heat_osd_enabled true
                                       :day_osd_enabled true
                                       :heat_crosshair_offset_horizontal 10
                                       :heat_crosshair_offset_vertical -10
                                       :day_crosshair_offset_horizontal 5
                                       :day_crosshair_offset_vertical -5})})]
          
          (is (m/validate state-spec valid-message-1)
              "Valid message 1 with GPS, compass, system, and rotary should pass")
          
          (is (m/validate state-spec valid-message-2)
              "Valid message 2 with cameras and LRF should pass")
          
          (is (m/validate state-spec valid-message-3)
              "Valid message 3 with time, actual_space_time, and rec_osd should pass"))))
  
  (testing "Hand-crafted invalid nested messages"
    (let [state-spec (m/schema :state/root)
          base-sample (mg/generate state-spec)]
      
      (testing "Invalid GPS coordinates"
        (let [invalid-gps-message
              (merge base-sample
                     {:protocol_version 1
                      :gps (merge (:gps base-sample)
                                  {:latitude 91.0  ; Invalid: > 90
                                   :longitude -181.0 ; Invalid: < -180
                                   :altitude -500.0  ; Invalid: < -432
                                   :manual_latitude 0.0
                                   :manual_longitude 0.0
                                   :manual_altitude 0.0
                                   :fix_type :JON_GUI_DATA_GPS_FIX_TYPE_3D
                                   :use_manual false})})]
          
          (is (not (m/validate state-spec invalid-gps-message))
              "Message with invalid GPS coordinates should fail")
          
          (when-let [explanation (m/explain state-spec invalid-gps-message)]
            (let [humanized (me/humanize explanation)]
              (is (get-in humanized [:gps :latitude])
                  "Should have error for invalid latitude")
              (is (get-in humanized [:gps :longitude])
                  "Should have error for invalid longitude")
              (is (get-in humanized [:gps :altitude])
                  "Should have error for invalid altitude")))))
      
      (testing "Invalid compass angles"
        (let [invalid-compass-message
              (merge base-sample
                     {:protocol_version 1
                      :compass (merge (:compass base-sample)
                                      {:azimuth 360.0  ; Invalid: must be < 360
                                       :elevation 91.0  ; Invalid: > 90
                                       :bank 180.0      ; Invalid: must be < 180
                                       :offsetAzimuth 180.0  ; Invalid: must be < 180
                                       :offsetElevation 91.0  ; Invalid: > 90
                                       :magneticDeclination 180.0  ; Invalid: must be < 180
                                       :calibrating false})})]
          
          (is (not (m/validate state-spec invalid-compass-message))
              "Message with invalid compass angles should fail")))
      
      (testing "Invalid nested LRF target"
        (let [invalid-lrf-message
              (merge base-sample
                     {:protocol_version 1
                      :lrf (merge (:lrf base-sample)
                                  {:is_scanning true
                                   :is_measuring false
                                   :measure_id -1  ; Invalid: must be >= 0
                                   :target (merge (or (:target (:lrf base-sample)) {})
                                                  {:timestamp -1  ; Invalid: must be >= 0
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
                                                   :uuid_part4 0})
                                   :pointer_mode :JON_GUI_DATA_LRF_LASER_POINTER_MODE_OFF
                                   :fogModeEnabled false
                                   :is_refining false})})]
          
          (is (not (m/validate state-spec invalid-lrf-message))
              "Message with invalid nested LRF target should fail")))
      
      (testing "Invalid system values"
        (let [invalid-system-message
              (merge base-sample
                     {:protocol_version 1
                      :system (merge (:system base-sample)
                                     {:cpu_temperature -274.0  ; Invalid: below absolute zero
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
                                      :recognition_mode false})})]
          
          (is (not (m/validate state-spec invalid-system-message))
              "Message with invalid system values should fail")))))))

(deftest generate-valid-state-samples
  (testing "Can generate 1000 valid state samples from Malli specs"
    (let [state-spec (m/schema :state/root)
          ;; Generate 1000 samples - force evaluation with doall
          samples (doall (mg/sample state-spec {:size 1000}))]
      
      ;; Basic checks
      (is (= 1000 (count samples)) "Should generate 1000 samples")
      
      ;; Validate all samples
      (let [validation-results (mapv #(m/validate state-spec %) samples)
            valid-count (count (filter true? validation-results))
            invalid-samples (keep-indexed 
                           (fn [idx valid?] 
                             (when-not valid? idx))
                           validation-results)]
        
        (is (= 1000 valid-count)
            (format "All 1000 samples should be valid. Invalid samples at indices: %s"
                   (take 10 invalid-samples)))
        
        ;; If there are invalid samples, show why the first one failed
        (when (seq invalid-samples)
          (let [first-invalid-idx (first invalid-samples)
                invalid-sample (nth samples first-invalid-idx)
                explanation (m/explain state-spec invalid-sample)]
            (println "\nFirst invalid sample explanation:")
            (println (me/humanize explanation)))))
      
      ;; Spot check structure of some samples
      (doseq [sample (take 10 samples)] ; Check first 10 for performance
        (testing "Sample structure"
          (is (map? sample) "Sample should be a map")
          (is (contains? sample :protocol_version) "Should have protocol_version")
          
          ;; Check nested structures if present
          (when (:gps sample)
            (is (every? #(contains? (:gps sample) %)
                       [:latitude :longitude :altitude :manual_latitude 
                        :manual_longitude :manual_altitude :fix_type :use_manual])
                "GPS should have all required fields")
            (is (<= -90 (get-in sample [:gps :latitude]) 90)
                "GPS latitude should be in valid range"))
          
          (when (:compass sample)
            (let [azimuth (get-in sample [:compass :azimuth])]
              (is (and (>= azimuth 0) (< azimuth 360))
                  "Compass azimuth should be in [0, 360)")))
          
          (when (:lrf sample)
            (is (map? (get-in sample [:lrf :target]))
                "LRF should have nested target map")
            (when-let [target-color (get-in sample [:lrf :target :target_color])]
              (is (and (<= 0 (:red target-color) 255)
                      (<= 0 (:green target-color) 255)
                      (<= 0 (:blue target-color) 255))
                  "LRF target color RGB values should be in [0, 255]")))
          
          (when (:rotary sample)
            (is (map? (get-in sample [:rotary :current_scan_node]))
                "Rotary should have nested current_scan_node map")))))))

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

(deftest generate-samples-performance
  (testing "Sample generation performance"
    (let [state-spec (m/schema :state/root)
          start-time (System/currentTimeMillis)
          samples (doall (mg/sample state-spec {:size 1000}))
          end-time (System/currentTimeMillis)
          duration (- end-time start-time)]
      
      (is (= 1000 (count samples))
          "Should generate 1000 samples")
      
      (println (format "\nGenerated 1000 samples in %d ms (%.1f samples/sec)"
                      duration
                      (/ 1000.0 (/ duration 1000.0))))
      
      ;; Check that generation is reasonably fast
      (is (< duration 30000)
          "Should generate 1000 samples in less than 30 seconds"))))