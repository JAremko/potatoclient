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

(deftest sanity-check-valid-messages
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
      (if (m/validate state-spec valid-message-1)
        (is true "Message validated successfully")
        (do
          (println "\nValidation failed for valid-message-1:")
          (println "Errors:" (me/humanize (m/explain state-spec valid-message-1)))
          (is false "Message should have been valid"))))))

(deftest sanity-check-invalid-gps
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
      (is (not (m/validate state-spec invalid-gps-message))))))

(deftest sanity-check-invalid-compass
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

(deftest generate-valid-state-samples
  (testing "Can generate 5000 valid state samples from Malli specs"
    (let [state-spec (m/schema :state/root)
          samples (doall (mg/sample state-spec {:size 5000}))
          validation-results (mapv #(m/validate state-spec %) samples)
          valid-count (count (filter true? validation-results))
          invalid-samples (keep-indexed 
                         (fn [idx valid?] 
                           (when-not valid? idx))
                         validation-results)]
      
      (is (= 5000 (count samples)) "Should generate 5000 samples")
      
      (is (= 5000 valid-count)
          (format "All 5000 samples should be valid. Invalid samples at indices: %s"
                 (take 10 invalid-samples))))))

(deftest validate-gps-constraints
  (let [gps-spec (m/schema [:map {:closed true}
                            [:latitude :position/latitude]
                            [:longitude :position/longitude]
                            [:altitude :position/altitude]])]
    
    (testing "Valid GPS data should pass"
      (is (m/validate gps-spec {:latitude 45.0
                                :longitude -122.0
                                :altitude 100.0})))
    
    (testing "Invalid latitude > 90 should fail"
      (is (not (m/validate gps-spec {:latitude 91.0
                                     :longitude 0.0
                                     :altitude 0.0}))))
    
    (testing "Invalid longitude >= 180 should fail"  
      (is (not (m/validate gps-spec {:latitude 0.0
                                     :longitude 180.0
                                     :altitude 0.0}))))))

(deftest generate-samples-performance
  (testing "Sample generation performance"
    (let [state-spec (m/schema :state/root)
          start-time (System/currentTimeMillis)
          samples (doall (mg/sample state-spec {:size 5000}))
          end-time (System/currentTimeMillis)
          duration (- end-time start-time)]
      
      (is (= 5000 (count samples))
          "Should generate 5000 samples")
      
      (println (format "\nGenerated 5000 samples in %d ms (%.1f samples/sec)"
                      duration
                      (/ 5000.0 (/ duration 1000.0))))
      
      (is (< duration 60000)
          "Should generate 5000 samples in less than 60 seconds"))))