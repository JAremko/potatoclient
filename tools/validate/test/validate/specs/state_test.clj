(ns validate.specs.state-test
  "Test state message specs with proper validation"
  (:require
   [clojure.test :refer [deftest testing is]]
   [malli.core :as m]
   [malli.error :as me]
   [malli.generator :as mg]
   [malli.util :as mu]
   [clojure.pprint :refer [pprint]]
   [potatoclient.malli.registry :as registry]
   [potatoclient.specs.oneof-edn :as oneof-edn]
   ;; Load all specs
   [potatoclient.specs.common]
   [potatoclient.specs.state.root]))

;; Initialize the global registry with oneof-edn schema
(defn init-registry! []
  (registry/setup-global-registry!
    (oneof-edn/register_ONeof-edn-schema!)))

;; Load sample EDN data using snake_case (Pronto's default)
(def sample-state-edn
  {:actual_space_time {:altitude 0.291143
                       :azimuth 256.62
                       :elevation 7.04
                       :latitude 50.02363
                       :longitude 15.815215
                       :timestamp 1754665407}
   :camera_day {:clahe_level 0.16
                :digital_zoom_level 1.0
                :focus_pos 1.0
                :fx_mode :JON_GUI_DATA_FX_MODE_DAY_A
                :infrared_filter true
                :iris_pos 0.03
                :zoom_pos 0.59938735
                :zoom_table_pos 3
                :zoom_table_pos_max 4
                :auto_focus false
                :auto_iris false}
   :camera_heat {:agc_mode :JON_GUI_DATA_VIDEO_CHANNEL_HEAT_AGC_MODE_2
                 :clahe_level 0.5
                 :digital_zoom_level 1.0
                 :filter :JON_GUI_DATA_VIDEO_CHANNEL_HEAT_FILTER_HOT_WHITE
                 :fx_mode :JON_GUI_DATA_FX_MODE_HEAT_A
                 :zoom_table_pos 3
                 :zoom_table_pos_max 4
                 :zoom_pos 0.0
                 :dde_level 0
                 :dde_enabled false
                 :auto_focus false}
   :compass {:azimuth 335.3625
             :bank 0.7312500000000001
             :elevation 3.6
             :offsetAzimuth 0.0
             :offsetElevation 0.0
             :magneticDeclination 0.0
             :calibrating false}
   :compass_calibration {:final_stage 12
                         :status :JON_GUI_DATA_COMPASS_CALIBRATE_STATUS_NOT_CALIBRATING
                         :target_azimuth 56.25
                         :target_bank -5.625
                         :target_elevation 6.75}
   :day_cam_glass_heater {}
   :gps {:altitude 0.291143
         :fix_type :JON_GUI_DATA_GPS_FIX_TYPE_3D
         :latitude 50.023629
         :longitude 15.815214999999998
         :manual_latitude 50.023604
         :manual_longitude 15.815316
         :manual_altitude 0.0
         :use_manual false}
   :lrf {:is_scanning false
         :is_measuring false
         :is_refining false
         :measure_id 52
         :pointer_mode :JON_GUI_DATA_LRF_LASER_POINTER_MODE_OFF
         :fogModeEnabled false
         :target {:observer_azimuth 356.40000000000003
                  :observer_elevation -0.675
                  :observer_fix_type :JON_GUI_DATA_GPS_FIX_TYPE_2D
                  :observer_latitude 8.0
                  :observer_longitude 7.0
                  :observer_altitude 0.0
                  :observer_bank 0.0
                  :target_id 52
                  :target_latitude 50.023638999999996
                  :target_longitude 15.815211999999999
                  :target_altitude 0.0
                  :target_color {:red 255 :green 0 :blue 0}
                  :distance_2d 1000.0
                  :distance_3b 1000.0
                  :session_id 1
                  :type 0
                  :timestamp 1754576916
                  :uuid_part1 -494581931
                  :uuid_part2 -224575107
                  :uuid_part3 -1771114019
                  :uuid_part4 879344611}}
   :meteo_internal {}
   :protocol_version 1
   :rec_osd {:day_osd_enabled true
             :heat_osd_enabled true
             :screen :JON_GUI_DATA_REC_OSD_SCREEN_MAIN}
   :rotary {:azimuth 335.3625
            :azimuth_speed 0.0
            :elevation 7.04
            :elevation_speed 0.0
            :platform_azimuth 256.62
            :platform_elevation 7.04
            :platform_bank 0.0
            :is_moving false
            :is_scanning false
            :is_scanning_paused false
            :use_rotary_as_compass false
            :current_scan_node {:index 1
                                :DayZoomTableValue 1
                                :HeatZoomTableValue 1
                                :azimuth 0.001
                                :elevation 0.001
                                :linger 0.001
                                :speed 0.001}
            :mode :JON_GUI_DATA_ROTARY_MODE_POSITION
            :scan_target 1
            :scan_target_max 1
            :sun_azimuth 0.0
            :sun_elevation 0.0}
   :system {:cpu_load 42.0
            :cpu_temperature 42.0
            :cur_video_rec_dir_day 8
            :cur_video_rec_dir_hour 15
            :cur_video_rec_dir_minute 1
            :cur_video_rec_dir_month 8
            :cur_video_rec_dir_second 32
            :cur_video_rec_dir_year 2025
            :disk_space 95
            :gpu_load 42.0
            :gpu_temperature 42.0
            :loc :JON_GUI_DATA_SYSTEM_LOCALIZATION_EN
            :low_disk_space true
            :power_consumption 42.0
            :rec_enabled true
            :important_rec_enabled false
            :no_disk_space false
            :cv_dumping false
            :vampire_mode false
            :tracking false
            :stabilization_mode false
            :geodesic_mode false}
   :time {:manual_timestamp 1754665407
          :timestamp 1754665407}})

(deftest test-state-specs
  (init-registry!)
  
  (testing "Individual message validation"
    (testing "GPS message"
      (let [gps-data (:gps sample-state-edn)]
        (is (m/validate :state/gps gps-data)
            (str "GPS validation failed: " (me/humanize (m/explain :state/gps gps-data))))))
    
    (testing "System message"
      (let [system-data (:system sample-state-edn)]
        (is (m/validate :state/system system-data)
            (str "System validation failed: " (me/humanize (m/explain :state/system system-data))))))
    
    (testing "Compass message"
      (let [compass-data (:compass sample-state-edn)]
        (is (m/validate :state/compass compass-data)
            (str "Compass validation failed: " (me/humanize (m/explain :state/compass compass-data))))))
    
    (testing "Camera Day message"
      (let [camera-day-data (:camera_day sample-state-edn)]
        (is (m/validate :state/camera-day camera-day-data)
            (str "Camera Day validation failed: " (me/humanize (m/explain :state/camera-day camera-day-data))))))
    
    (testing "Camera Heat message"
      (let [camera-heat-data (:camera_heat sample-state-edn)]
        (is (m/validate :state/camera-heat camera-heat-data)
            (str "Camera Heat validation failed: " (me/humanize (m/explain :state/camera-heat camera-heat-data))))))
    
    (testing "LRF message"
      (let [lrf-data (:lrf sample-state-edn)]
        (is (m/validate :state/lrf lrf-data)
            (str "LRF validation failed: " (me/humanize (m/explain :state/lrf lrf-data))))))
    
    (testing "Rotary message"
      (let [rotary-data (:rotary sample-state-edn)]
        (is (m/validate :state/rotary rotary-data)
            (str "Rotary validation failed: " (me/humanize (m/explain :state/rotary rotary-data)))))))
  
  (testing "Complete State root validation"
    (is (m/validate :state/root sample-state-edn)
        (str "Root State validation failed: " (me/humanize (m/explain :state/root sample-state-edn))))))

(deftest test-state-generation
  (init-registry!)
  
  (testing "State message generation"
    (testing "Can generate valid GPS messages"
      (let [samples (repeatedly 10 #(mg/generate :state/gps))]
        (is (every? #(m/validate :state/gps %) samples)
            "All generated GPS messages should be valid")))
    
    (testing "Can generate valid System messages"
      (let [samples (repeatedly 10 #(mg/generate :state/system))]
        (is (every? #(m/validate :state/system %) samples)
            "All generated System messages should be valid")))
    
    (testing "Can generate valid Compass messages"
      (let [samples (repeatedly 10 #(mg/generate :state/compass))]
        (is (every? #(m/validate :state/compass %) samples)
            "All generated Compass messages should be valid")))))

(deftest test-state-round-trip
  (init-registry!)
  
  (testing "State generation and validation round-trip"
    (testing "Generated root state validates"
      (dotimes [_ 5]
        (let [generated (mg/generate :state/root)]
          (is (m/validate :state/root generated)
              "Generated state should validate")
          (is (= 13 (count (dissoc generated :protocol_version)))
              (str "Should have all 13 required sub-messages, got: " 
                   (count (dissoc generated :protocol_version))
                   " keys: " (keys (dissoc generated :protocol_version))))
          (is (pos? (:protocol_version generated))
              "Protocol version should be positive"))))
    
    (testing "State field constraints are respected"
      (let [samples (repeatedly 20 #(mg/generate :state/root))]
        (doseq [state samples]
          (let [sys (:system state)]
            (is (and (number? (:cpu_temperature sys))
                     (<= -273.15 (:cpu_temperature sys) 150)) 
                (str "CPU temp in valid range: " (:cpu_temperature sys)))
            (is (<= 0 (:gpu_load sys) 100) "GPU load in valid range")
            (is (<= 0 (:disk_space sys) 100) "Disk space in valid range"))
          
          (let [gps (:gps state)]
            (is (<= -90 (:latitude gps) 90) "Latitude in valid range")
            (is (<= -180 (:longitude gps) 180) "Longitude in valid range"))
          
          (let [compass (:compass state)]
            (is (<= 0 (:azimuth compass) 360) "Azimuth in valid range")
            (is (<= -90 (:elevation compass) 90) "Elevation in valid range")))))))