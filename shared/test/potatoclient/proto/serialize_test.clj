(ns potatoclient.proto.serialize-test
  "Tests for proto serialization utility.
   Tests both fast (*) and validating versions of serialize functions."
  (:require
   [clojure.test :refer [deftest is testing use-fixtures]]
   [malli.core :as m]
   [malli.generator :as mg]
   [pronto.core :as pronto]
   [pronto.utils]
   [potatoclient.malli.registry :as registry]
   [potatoclient.proto.serialize :as serialize]
   [potatoclient.proto.deserialize :as deserialize]
   [potatoclient.specs.cmd.root]
   [potatoclient.specs.state.root]
   [potatoclient.test-harness :as harness])
  (:import
   [com.google.protobuf ByteString]))

;; Ensure test harness is initialized
(when-not harness/initialized?
  (throw (ex-info "Test harness failed to initialize!" 
                  {:initialized? harness/initialized?})))

;; Initialize registry
(registry/setup-global-registry!)

;; ============================================================================
;; Test Helpers
;; ============================================================================

(defn generate-valid-cmd
  "Generate a valid CMD message using Malli specs."
  []
  (mg/generate (m/schema :cmd/root)))

(defn generate-valid-state
  "Generate a valid State message using Malli specs."
  []
  (mg/generate (m/schema :state/root)))

(defn valid-cmd-sample
  "Return a known valid CMD message for testing."
  []
  {:protocol_version 1
   :session_id 12345
   :important false
   :from_cv_subsystem false
   :client_type :JON_GUI_DATA_CLIENT_TYPE_LOCAL_NETWORK
   :ping {}})

(defn valid-state-sample
  "Return a known valid State message for testing."
  []
  {:protocol_version 1
   :system {:cpu_temperature 25.0
            :gpu_temperature 30.0
            :gpu_load 50.0
            :cpu_load 40.0
            :power_consumption 100.0
            :loc :JON_GUI_DATA_SYSTEM_LOCALIZATION_EN
            :cur_video_rec_dir_year 2024
            :cur_video_rec_dir_month 1
            :cur_video_rec_dir_day 15
            :cur_video_rec_dir_hour 10
            :cur_video_rec_dir_minute 30
            :cur_video_rec_dir_second 45
            :rec_enabled false
            :important_rec_enabled false
            :low_disk_space false
            :no_disk_space false
            :disk_space 50
            :tracking false
            :vampire_mode false
            :stabilization_mode false
            :geodesic_mode false
            :cv_dumping false
            :recognition_mode false}
   :meteo_internal {:temperature 20.0
                    :humidity 50.0
                    :pressure 1013.25}
   :gps {:latitude 45.5
         :longitude -122.6
         :altitude 100.0
         :manual_latitude 45.5
         :manual_longitude -122.6
         :manual_altitude 100.0
         :fix_type :JON_GUI_DATA_GPS_FIX_TYPE_3D
         :use_manual false}
   :compass {:azimuth 45.0
             :elevation 0.0
             :bank 0.0
             :offsetAzimuth 0.0
             :offsetElevation 0.0
             :magneticDeclination 0.0
             :calibrating false}
   :time {:timestamp 1234567890
          :manual_timestamp 1234567890
          :zone_id 0
          :use_manual_time false}
   :rotary {:azimuth 0.0
            :azimuth_speed 0.0
            :elevation 0.0
            :elevation_speed 0.0
            :platform_azimuth 0.0
            :platform_elevation 0.0
            :platform_bank 0.0
            :is_moving false
            :mode :JON_GUI_DATA_ROTARY_MODE_SPEED
            :is_scanning false
            :is_scanning_paused false
            :use_rotary_as_compass false
            :scan_target 1
            :scan_target_max 10
            :sun_azimuth 180.0
            :sun_elevation 45.0
            :current_scan_node {:index 1
                               :DayZoomTableValue 5
                               :HeatZoomTableValue 5
                               :azimuth 0.0
                               :elevation 0.0
                               :linger 1.0
                               :speed 0.5}}
   :lrf {:is_scanning false
         :is_measuring false
         :measure_id 1
         :target {:timestamp 1234567890
                  :target_longitude -122.6
                  :target_latitude 45.5
                  :target_altitude 100.0
                  :observer_longitude -122.6
                  :observer_latitude 45.5
                  :observer_altitude 100.0
                  :observer_azimuth 0.0
                  :observer_elevation 0.0
                  :observer_bank 0.0
                  :distance_2d 0.0
                  :distance_3b 0.0
                  :observer_fix_type :JON_GUI_DATA_GPS_FIX_TYPE_3D
                  :session_id 1
                  :target_id 1
                  :target_color {:red 255 :green 0 :blue 0}
                  :type 0
                  :uuid_part1 0
                  :uuid_part2 0
                  :uuid_part3 0
                  :uuid_part4 0}
         :pointer_mode :JON_GUI_DATA_LRF_LASER_POINTER_MODE_OFF
         :fogModeEnabled false
         :is_refining false}
   :camera_day {:focus_pos 0.5
                :zoom_pos 0.5
                :iris_pos 0.5
                :infrared_filter false
                :zoom_table_pos 5
                :zoom_table_pos_max 10
                :fx_mode :JON_GUI_DATA_FX_MODE_DAY_A
                :auto_focus true
                :auto_iris true
                :digital_zoom_level 2.0
                :clahe_level 0.5}
   :camera_heat {:zoom_pos 0.5
                 :agc_mode :JON_GUI_DATA_VIDEO_CHANNEL_HEAT_AGC_MODE_1
                 :filter :JON_GUI_DATA_VIDEO_CHANNEL_HEAT_FILTER_HOT_WHITE
                 :auto_focus false
                 :zoom_table_pos 5
                 :zoom_table_pos_max 10
                 :dde_level 256
                 :dde_enabled true
                 :fx_mode :JON_GUI_DATA_FX_MODE_HEAT_A
                 :digital_zoom_level 1.5
                 :clahe_level 0.5}
   :compass_calibration {:stage 1
                         :final_stage 10
                         :target_azimuth 0.0
                         :target_elevation 0.0
                         :target_bank 0.0
                         :status :JON_GUI_DATA_COMPASS_CALIBRATE_STATUS_NOT_CALIBRATING}
   :actual_space_time {:azimuth 45.0
                       :elevation 30.0
                       :bank 0.0
                       :latitude 45.5
                       :longitude -122.6
                       :altitude 100.0
                       :timestamp 1234567890}
   :day_cam_glass_heater {:temperature 25.0
                          :status false}
   :rec_osd {:screen :JON_GUI_DATA_REC_OSD_SCREEN_MAIN
             :heat_osd_enabled false
             :day_osd_enabled false
             :heat_crosshair_offset_horizontal 0
             :heat_crosshair_offset_vertical 0
             :day_crosshair_offset_horizontal 0
             :day_crosshair_offset_vertical 0}})

;; ============================================================================
;; CMD Serialization Tests
;; ============================================================================

(deftest test-serialize-cmd-payload*
  (testing "Fast CMD serialization without validation"
    
    (testing "Valid CMD message serializes to bytes"
      (let [edn-data (valid-cmd-sample)
            binary-data (serialize/serialize-cmd-payload* edn-data)]
        (is (bytes? binary-data) "Should return byte array")
        (is (pos? (count binary-data)) "Should have non-empty byte array")))
    
    (testing "Serialized data can be deserialized back"
      (let [original-edn (valid-cmd-sample)
            binary-data (serialize/serialize-cmd-payload* original-edn)
            deserialized (deserialize/deserialize-cmd-payload* binary-data)]
        (is (= (:protocol_version original-edn) 
               (:protocol_version deserialized))
            "Protocol version should match after round-trip")
        (is (= (:session_id original-edn)
               (:session_id deserialized))
            "Session ID should match after round-trip")))
    
    (testing "Invalid EDN structure throws exception"
      (is (thrown? clojure.lang.ExceptionInfo
                   (serialize/serialize-cmd-payload* 
                    {:not-a-valid-field "test"}))
          "Should throw on invalid EDN structure"))
    
    (testing "Empty map serializes to minimal proto"
      (let [binary-data (serialize/serialize-cmd-payload* {})]
        (is (bytes? binary-data) "Should return byte array for empty map")))))

(deftest test-serialize-cmd-payload
  (testing "CMD serialization with full validation"
    
    (testing "Valid CMD message passes all validations and serializes"
      (let [edn-data (valid-cmd-sample)
            binary-data (serialize/serialize-cmd-payload edn-data)]
        (is (bytes? binary-data) "Should return byte array")
        (is (pos? (count binary-data)) "Should have non-empty byte array")
        ;; Verify it can be deserialized
        (let [deserialized (deserialize/deserialize-cmd-payload binary-data)]
          (is (m/validate :cmd/root deserialized)
              "Deserialized data should be valid"))))
    
    (testing "CMD with invalid protocol_version fails Malli validation"
      (let [invalid-edn {:protocol_version 0  ; Invalid: must be > 0
                         :session_id 123
                         :important false
                         :from_cv_subsystem false
                         :client_type :JON_GUI_DATA_CLIENT_TYPE_LOCAL_NETWORK
                         :ping {}}]
        (is (thrown-with-msg? clojure.lang.ExceptionInfo
                              #"Malli validation failed"
                              (serialize/serialize-cmd-payload invalid-edn))
            "Should throw Malli validation error for invalid protocol_version")))
    
    (testing "CMD with missing client_type gets default enum value that fails buf.validate"
      (let [invalid-edn {:protocol_version 1
                         :session_id 123
                         :important false
                         :from_cv_subsystem false
                         ;; Missing client_type will default to 0 (unspecified)
                         :ping {}}]
        (is (thrown-with-msg? clojure.lang.ExceptionInfo
                              #"buf.validate validation failed"
                              (serialize/serialize-cmd-payload invalid-edn))
            "Should throw buf.validate error for unspecified enum value")))
    
    (testing "CMD with no oneof field fails Malli validation"
      (let [invalid-edn {:protocol_version 1
                         :session_id 123
                         :important false
                         :from_cv_subsystem false
                         :client_type :JON_GUI_DATA_CLIENT_TYPE_LOCAL_NETWORK
                         ;; No oneof field
                         }]
        (is (thrown-with-msg? clojure.lang.ExceptionInfo
                              #"Malli validation failed"
                              (serialize/serialize-cmd-payload invalid-edn))
            "Should throw Malli validation error for missing oneof field")))
    
    (testing "Valid CMD passes both Malli and buf.validate"
      (let [edn-data (valid-cmd-sample)
            binary-data (serialize/serialize-cmd-payload edn-data)]
        ;; Verify the happy path works
        (is (bytes? binary-data) "Should successfully serialize valid data")
        (is (pos? (count binary-data)) "Should have non-empty binary data")))
    
    (testing "Multiple validation layers work correctly"
      ;; Test that a message with multiple oneof fields fails Malli
      (let [invalid-edn {:protocol_version 1
                         :session_id 123
                         :important false
                         :from_cv_subsystem false
                         :client_type :JON_GUI_DATA_CLIENT_TYPE_LOCAL_NETWORK
                         :ping {}
                         :noop {}}]  ; Two oneof fields - invalid
        (is (thrown-with-msg? clojure.lang.ExceptionInfo
                              #"Malli validation failed"
                              (serialize/serialize-cmd-payload invalid-edn))
            "Should throw Malli validation error for multiple oneof fields"))))

;; ============================================================================
;; State Serialization Tests
;; ============================================================================

(deftest test-serialize-state-payload*
  (testing "Fast State serialization without validation"
    
    (testing "Valid State message serializes to bytes"
      (let [edn-data (valid-state-sample)
            binary-data (serialize/serialize-state-payload* edn-data)]
        (is (bytes? binary-data) "Should return byte array")
        (is (pos? (count binary-data)) "Should have non-empty byte array")))
    
    (testing "Serialized data can be deserialized back"
      (let [original-edn (valid-state-sample)
            binary-data (serialize/serialize-state-payload* original-edn)
            deserialized (deserialize/deserialize-state-payload* binary-data)]
        (is (= (:protocol_version original-edn) 
               (:protocol_version deserialized))
            "Protocol version should match after round-trip")
        (is (= (get-in original-edn [:gps :latitude])
               (get-in deserialized [:gps :latitude]))
            "GPS latitude should match after round-trip")))
    
    (testing "Invalid EDN structure throws exception"
      (is (thrown? clojure.lang.ExceptionInfo
                   (serialize/serialize-state-payload* 
                    {:not-a-valid-field "test"}))
          "Should throw on invalid EDN structure"))))

(deftest test-serialize-state-payload
  (testing "State serialization with full validation"
    
    (testing "Valid State message passes all validations and serializes"
      (let [edn-data (valid-state-sample)
            binary-data (serialize/serialize-state-payload edn-data)]
        (is (bytes? binary-data) "Should return byte array")
        (is (pos? (count binary-data)) "Should have non-empty byte array")
        ;; Verify it can be deserialized
        (let [deserialized (deserialize/deserialize-state-payload binary-data)]
          (is (m/validate :state/root deserialized)
              "Deserialized data should be valid"))))
    
    (testing "State with invalid GPS coordinates fails Malli validation"
      (let [base-state (valid-state-sample)
            invalid-edn (assoc base-state
                              :gps (assoc (:gps base-state)
                                         :latitude 91.0  ; Invalid: > 90
                                         :longitude -181.0))]  ; Invalid: < -180
        (is (thrown-with-msg? clojure.lang.ExceptionInfo
                              #"Malli validation failed"
                              (serialize/serialize-state-payload invalid-edn))
            "Should throw Malli validation error for invalid GPS coordinates")))
    
    (testing "State with missing required field fails Malli validation"
      (let [invalid-edn (dissoc (valid-state-sample) :system)]
        (is (thrown-with-msg? clojure.lang.ExceptionInfo
                              #"Malli validation failed"
                              (serialize/serialize-state-payload invalid-edn))
            "Should throw Malli validation error for missing required field")))))

;; ============================================================================
;; Round-trip Tests with Both Serialize and Deserialize
;; ============================================================================

(deftest test-cmd-full-round-trip
  (testing "CMD full round-trip: EDN -> serialize -> deserialize -> EDN"
    (dotimes [_ 100]  ; Test with 100 random samples
      (let [original-edn (generate-valid-cmd)
            binary-data (serialize/serialize-cmd-payload original-edn)
            deserialized (deserialize/deserialize-cmd-payload binary-data)]
        ;; Check all top-level fields are preserved
        (is (= (:protocol_version original-edn)
               (:protocol_version deserialized))
            "Protocol version should be preserved")
        (is (= (:session_id original-edn)
               (:session_id deserialized))
            "Session ID should be preserved")
        (is (= (:client_type original-edn)
               (:client_type deserialized))
            "Client type should be preserved")
        (is (= (:important original-edn)
               (:important deserialized))
            "Important flag should be preserved")
        (is (= (:from_cv_subsystem original-edn)
               (:from_cv_subsystem deserialized))
            "from_cv_subsystem flag should be preserved")))))

(deftest test-state-full-round-trip
  (testing "State full round-trip: EDN -> serialize -> deserialize -> EDN"
    (dotimes [_ 100]  ; Test with 100 random samples
      (let [original-edn (generate-valid-state)
            binary-data (serialize/serialize-state-payload original-edn)
            deserialized (deserialize/deserialize-state-payload binary-data)]
        ;; Check key fields are preserved
        (is (= (:protocol_version original-edn)
               (:protocol_version deserialized))
            "Protocol version should be preserved")
        ;; Check nested structures
        (when (:gps original-edn)
          (is (= (get-in original-edn [:gps :latitude])
                 (get-in deserialized [:gps :latitude]))
              "GPS latitude should be preserved")
          (is (= (get-in original-edn [:gps :longitude])
                 (get-in deserialized [:gps :longitude]))
              "GPS longitude should be preserved"))
        (when (:system original-edn)
          (is (= (get-in original-edn [:system :control_mode])
                 (get-in deserialized [:system :control_mode]))
              "System control mode should be preserved"))))))

;; ============================================================================
;; Performance Comparison Tests
;; ============================================================================

(deftest test-serialization-performance-comparison
  (testing "Performance comparison between validating and non-validating serialize versions"
    (let [cmd-samples (repeatedly 1000 generate-valid-cmd)
          
          ;; Time fast version
          start-fast (System/currentTimeMillis)
          fast-results (doall (map serialize/serialize-cmd-payload* cmd-samples))
          fast-time (- (System/currentTimeMillis) start-fast)
          
          ;; Time validating version
          start-validating (System/currentTimeMillis)
          validating-results (doall (map serialize/serialize-cmd-payload cmd-samples))
          validating-time (- (System/currentTimeMillis) start-validating)]
      
      (println (format "\nSerialization performance comparison (1000 CMD messages):"))
      (println (format "  Fast version (*): %d ms" fast-time))
      (println (format "  Validating version: %d ms" validating-time))
      
      ;; Since we disabled Malli validation, they should be similar  
      ;; Just check that both complete reasonably fast
      
      ;; Both should complete in reasonable time
      (is (< fast-time 5000)
          "Fast version should complete in reasonable time")
      (is (< validating-time 10000)
          "Validating version should complete in reasonable time"))))

;; ============================================================================
;; Error Information Tests
;; ============================================================================

(deftest test-serialization-error-information
  (testing "Serialization errors contain useful debugging information"
    
    (testing "Malli validation errors contain field information"
      (let [invalid-edn {:protocol_version 0  ; Invalid
                         :session_id 123
                         :important false
                         :from_cv_subsystem false
                         :client_type :JON_GUI_DATA_CLIENT_TYPE_LOCAL_NETWORK
                         :ping {}}]
        (try
          (serialize/serialize-cmd-payload invalid-edn)
          (is false "Should have thrown exception")
          (catch clojure.lang.ExceptionInfo e
            (let [data (ex-data e)]
              (is (= :malli-validation-error (:type data)))
              (is (= :cmd/root (:spec data)))
              (is (contains? data :errors) "Should contain error details"))))))
    
    (testing "Serialization errors for invalid structure"
      (try
        (serialize/serialize-cmd-payload* {:completely-invalid-field "test"})
        (is false "Should have thrown exception")
        (catch clojure.lang.ExceptionInfo e
          (let [data (ex-data e)]
            (is (= :serialization-error (:type data)))
            (is (= :cmd (:proto-type data)))
            (is (string? (:error data)) "Should contain error message")))))))

;; ============================================================================
;; Consistency Tests
;; ============================================================================

(deftest test-serialize-deserialize-consistency
  (testing "Serialize and deserialize functions are consistent"
    
    (testing "CMD: serialize then deserialize preserves key fields"
      (let [original (valid-cmd-sample)
            serialized (serialize/serialize-cmd-payload original)
            deserialized (deserialize/deserialize-cmd-payload serialized)]
        ;; Proto adds nil fields for all oneof options, so we can't do exact equality
        ;; Instead check that the important fields match
        (is (= (:protocol_version original) (:protocol_version deserialized))
            "Protocol version should match")
        (is (= (:session_id original) (:session_id deserialized))
            "Session ID should match")
        (is (= (:client_type original) (:client_type deserialized))
            "Client type should match")
        (is (= (:ping original) (:ping deserialized))
            "Ping field should match")))
    
    (testing "State: serialize then deserialize preserves key fields"
      (let [original (valid-state-sample)
            serialized (serialize/serialize-state-payload original)
            deserialized (deserialize/deserialize-state-payload serialized)]
        ;; Check key fields are preserved
        (is (= (:protocol_version original) (:protocol_version deserialized))
            "Protocol version should match")
        (is (= (get-in original [:system :cpu_temperature])
               (get-in deserialized [:system :cpu_temperature]))
            "System CPU temperature should match")
        (is (= (get-in original [:gps :latitude])
               (get-in deserialized [:gps :latitude]))
            "GPS latitude should match")))
    
    (testing "Fast versions are consistent with validating versions for valid data"
      (let [cmd-data (valid-cmd-sample)
            fast-bytes (serialize/serialize-cmd-payload* cmd-data)
            validating-bytes (serialize/serialize-cmd-payload cmd-data)
            fast-deserialized (deserialize/deserialize-cmd-payload* fast-bytes)
            validating-deserialized (deserialize/deserialize-cmd-payload validating-bytes)]
        (is (= fast-deserialized validating-deserialized)
            "Fast and validating versions should produce equivalent results"))))))