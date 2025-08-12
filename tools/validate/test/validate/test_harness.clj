(ns validate.test-harness
  "Test harness with real state EDN data and Pronto best practices.
   Uses minimal mappers - Pronto auto-discovers nested message classes."
  (:require [pronto.core :as p]
            [pronto.schema :as schema]
            [clojure.edn :as edn])
  (:import [cmd JonSharedCmd$Root]
           [ser JonSharedData$JonGUIState]))

;; ============================================================================
;; MINIMAL MAPPERS - Pronto auto-discovers nested message classes
;; ============================================================================

;; Command mapper - only needs root class, Pronto discovers the rest
(p/defmapper cmd-mapper [cmd.JonSharedCmd$Root])

;; State mapper - only needs root class, Pronto discovers the rest  
(p/defmapper state-mapper [ser.JonSharedData$JonGUIState])

;; ============================================================================
;; HELPER FUNCTIONS FOR CLASS DISCOVERY
;; ============================================================================

(defn discover-message-classes
  "Discover all message classes from a proto-map using schema introspection.
   Returns a set of all Java classes found in the message structure."
  [proto-map]
  (let [classes (atom #{})]
    ;; Add the root class first
    (swap! classes conj (class (p/proto-map->proto proto-map)))
    ;; Then walk the schema to find all nested classes
    (clojure.walk/postwalk
      (fn [x]
        (cond
          ;; If it's a class, add it to our set
          (class? x) (do (swap! classes conj x) x)
          ;; If it's a proto-map, get its schema
          (p/proto-map? x) (do
                            (swap! classes conj (class (p/proto-map->proto x)))
                            (doseq [[_ field-type] (schema/schema x)]
                              (when (class? field-type)
                                (swap! classes conj field-type)))
                            x)
          :else x))
      (schema/schema proto-map))
    @classes))

;; ============================================================================
;; REAL CAPTURED STATE DATA
;; This is actual state data from a running system
;; ============================================================================

(def real-state-edn
  "Real state captured from state-explorer at timestamp 1754664759800"
  {:actual_space_time {:altitude 0.289371
                       :azimuth 256.62
                       :elevation 7.04
                       :latitude 50.023632
                       :longitude 15.81521
                       :timestamp 1754664759}
   :camera_day {:clahe_level 0.16
                :digital_zoom_level 1.0
                :focus_pos 1.0
                :fx_mode :JON_GUI_DATA_FX_MODE_DAY_A
                :infrared_filter true
                :iris_pos 0.03
                :zoom_pos 0.59938735
                :zoom_table_pos 3
                :zoom_table_pos_max 4}
   :camera_heat {:agc_mode :JON_GUI_DATA_VIDEO_CHANNEL_HEAT_AGC_MODE_2
                 :clahe_level 0.5
                 :digital_zoom_level 1.0
                 :filter :JON_GUI_DATA_VIDEO_CHANNEL_HEAT_FILTER_HOT_WHITE
                 :fx_mode :JON_GUI_DATA_FX_MODE_HEAT_A
                 :zoom_table_pos 3
                 :zoom_table_pos_max 4}
   :compass {:azimuth 333.50625
             :bank 0.84375
             :elevation 3.54375}
   :compass_calibration {:final_stage 12
                         :status :JON_GUI_DATA_COMPASS_CALIBRATE_STATUS_NOT_CALIBRATING
                         :target_azimuth 56.25
                         :target_bank -5.625
                         :target_elevation 6.75}
   :day_cam_glass_heater {}
   :gps {:altitude 0.289371
         :fix_type :JON_GUI_DATA_GPS_FIX_TYPE_3D
         :latitude 50.023632
         :longitude 15.815209999999999
         :manual_latitude 50.023604
         :manual_longitude 15.815316}
   :lrf {:measure_id 52
         :pointer_mode :JON_GUI_DATA_LRF_LASER_POINTER_MODE_OFF
         :target {:observer_azimuth 356.40000000000003
                  :observer_elevation -0.675
                  :observer_fix_type :JON_GUI_DATA_GPS_FIX_TYPE_2D
                  :observer_latitude 8.0
                  :observer_longitude 7.0
                  :target_id 52
                  :target_latitude 50.023638999999996
                  :target_longitude 15.815211999999999
                  :timestamp 1754576916
                  :uuid_part1 -494581931
                  :uuid_part2 -224575107}}
   :meteo_internal {}
   :protocol_version 1
   :rec_osd {:day_osd_enabled true
             :heat_osd_enabled true
             :screen :JON_GUI_DATA_REC_OSD_SCREEN_MAIN}
   :rotary {:azimuth 333.50626
            :elevation 7.04
            :mode :JON_GUI_DATA_ROTARY_MODE_POSITION
            :current_scan_node {:speed 0.5
                                :azimuth 0.0
                                :elevation 0.0}}
   :system {:cpu_load 12.94
            :cpu_temperature 53.33
            :cur_video_rec_dir_day 7
            :cur_video_rec_dir_hour 17
            :cur_video_rec_dir_minute 28
            :cur_video_rec_dir_month 1
            :cur_video_rec_dir_second 47
            :cur_video_rec_dir_year 2025
            :disk_space 34
            :gpu_load 3.0
            :gpu_temperature 48.0
            :important_rec_enabled false
            :loc :JON_GUI_DATA_SYSTEM_LOCALIZATION_EN
            :low_disk_space false
            :no_disk_space false
            :power_consumption 0.0
            :rec_enabled true
            :recognition_mode false}
   :time {:manual_timestamp 1754664759
          :timestamp 1754664759
          :use_manual_time false
          :zone_id 3600}})

;; ============================================================================
;; COMPLEX COMMAND CREATION - Demonstrating nested command structures
;; ============================================================================

(defn valid-rotary-azimuth-cmd
  "Create a valid rotary azimuth command with nested structure."
  []
  (p/proto-map cmd-mapper cmd.JonSharedCmd$Root
               :protocol_version 1
               :session_id 4000
               :client_type :JON_GUI_DATA_CLIENT_TYPE_LOCAL_NETWORK
               :rotary (p/proto-map cmd-mapper cmd.RotaryPlatform.JonSharedCmdRotary$Root
                                   :set_platform_azimuth
                                   (p/proto-map cmd-mapper 
                                               cmd.RotaryPlatform.JonSharedCmdRotary$SetPlatformAzimuth
                                               :value 45.0))))

(defn valid-rotary-stop-cmd
  "Create a valid rotary stop command."
  []
  (p/proto-map cmd-mapper cmd.JonSharedCmd$Root
               :protocol_version 1
               :session_id 5000
               :client_type :JON_GUI_DATA_CLIENT_TYPE_LOCAL_NETWORK
               :rotary (p/proto-map cmd-mapper cmd.RotaryPlatform.JonSharedCmdRotary$Root
                                   :stop
                                   (p/proto-map cmd-mapper 
                                               cmd.RotaryPlatform.JonSharedCmdRotary$Stop))))

(defn valid-rotary-scan-cmd
  "Create a valid rotary scan command."
  []
  (p/proto-map cmd-mapper cmd.JonSharedCmd$Root
               :protocol_version 1
               :session_id 6000
               :client_type :JON_GUI_DATA_CLIENT_TYPE_INTERNAL_CV
               :rotary (p/proto-map cmd-mapper cmd.RotaryPlatform.JonSharedCmdRotary$Root
                                   :set_mode
                                   (p/proto-map cmd-mapper 
                                               cmd.RotaryPlatform.JonSharedCmdRotary$SetMode
                                               :mode :JON_GUI_DATA_ROTARY_MODE_POSITION))))

;; ============================================================================
;; VALID DATA CREATION - Idiomatic Pronto approach
;; ============================================================================

(defn valid-state
  "Create a valid state proto-map from real EDN data.
   This is the foundation for all state-based tests."
  []
  (p/clj-map->proto-map state-mapper ser.JonSharedData$JonGUIState real-state-edn))

(defn valid-ping-cmd
  "Create a valid ping command using direct proto-map creation."
  []
  (p/proto-map cmd-mapper cmd.JonSharedCmd$Root
               :protocol_version 1
               :session_id 1000
               :client_type :JON_GUI_DATA_CLIENT_TYPE_LOCAL_NETWORK
               :ping (p/proto-map cmd-mapper cmd.JonSharedCmd$Ping)))

(defn valid-noop-cmd
  "Create a valid noop command."
  []
  (p/proto-map cmd-mapper cmd.JonSharedCmd$Root
               :protocol_version 1
               :session_id 2000
               :client_type :JON_GUI_DATA_CLIENT_TYPE_INTERNAL_CV
               :noop (p/proto-map cmd-mapper cmd.JonSharedCmd$Noop)))

(defn valid-frozen-cmd
  "Create a valid frozen command."
  []
  (p/proto-map cmd-mapper cmd.JonSharedCmd$Root
               :protocol_version 1
               :session_id 3000
               :client_type :JON_GUI_DATA_CLIENT_TYPE_LOCAL_NETWORK
               :frozen (p/proto-map cmd-mapper cmd.JonSharedCmd$Frozen)))

;; ============================================================================
;; INVALID DATA CREATION - Performant modification of valid data
;; ============================================================================

(defn invalid-gps-state
  "Create state with out-of-range GPS coordinates.
   Demonstrates performant p/p-> usage with hints."
  []
  (let [state (valid-state)]
    (p/with-hints [(p/hint state ser.JonSharedData$JonGUIState state-mapper)]
      (p/p-> state
             (assoc-in [:gps :latitude] 200.0)      ; Invalid: > 90
             (assoc-in [:gps :longitude] -400.0))))) ; Invalid: < -180

(defn invalid-protocol-state
  "State with invalid protocol version."
  []
  (let [state (valid-state)]
    (p/with-hints [(p/hint state ser.JonSharedData$JonGUIState state-mapper)]
      (p/p-> state
             (assoc :protocol_version 0))))) ; Must be > 0

(defn invalid-client-type-cmd
  "Command with invalid client type (UNSPECIFIED)."
  []
  (p/proto-map cmd-mapper cmd.JonSharedCmd$Root
               :protocol_version 1
               :session_id 4000
               :client_type :JON_GUI_DATA_CLIENT_TYPE_UNSPECIFIED ; Not allowed
               :ping (p/proto-map cmd-mapper cmd.JonSharedCmd$Ping)))

(defn invalid-protocol-cmd
  "Command with invalid protocol version."
  []
  (p/proto-map cmd-mapper cmd.JonSharedCmd$Root
               :protocol_version 0  ; Must be > 0
               :session_id 5000
               :client_type :JON_GUI_DATA_CLIENT_TYPE_LOCAL_NETWORK
               :noop (p/proto-map cmd-mapper cmd.JonSharedCmd$Noop)))

(defn state-missing-required-fields
  "State missing all required nested messages.
   Tests validation of required fields."
  []
  (p/proto-map state-mapper ser.JonSharedData$JonGUIState
               :protocol_version 1)) ; Missing time, system, gps, etc.

;; ============================================================================
;; BOUNDARY TESTING - Valid but extreme values
;; ============================================================================

(defn state-with-boundary-values
  "State with valid but extreme values for boundary testing."
  []
  (let [state (valid-state)]
    (p/with-hints [(p/hint state ser.JonSharedData$JonGUIState state-mapper)]
      (p/p-> state
             (assoc-in [:gps :latitude] 90.0)        ; North Pole
             (assoc-in [:gps :longitude] 180.0)      ; International Date Line
             (assoc-in [:gps :altitude] 8848.86)     ; Mt. Everest
             (assoc-in [:system :cpu_temperature] 100.0)
             (assoc-in [:system :cpu_load] 100.0)
             (assoc-in [:compass :azimuth] 359.999)
             (assoc-in [:rotary :elevation] 90.0)))))

;; ============================================================================
;; UTILITY FUNCTIONS
;; ============================================================================

(defn ->bytes
  "Convert proto-map to byte array for validation."
  [proto-map]
  (p/proto-map->bytes proto-map))

(defn bytes->
  "Convert byte array back to proto-map."
  [mapper clazz byte-array]
  (p/bytes->proto-map mapper clazz byte-array))

(defn modify-state
  "Performantly modify a state with a function.
   The function receives the state and should return p/p-> operations."
  [state-proto-map modifications-fn]
  (p/with-hints [(p/hint state-proto-map ser.JonSharedData$JonGUIState state-mapper)]
    (modifications-fn state-proto-map)))

;; ============================================================================
;; TEST DATA ACCESSORS - Convenience functions for tests
;; ============================================================================

(def valid-state-bytes (memoize #(->bytes (valid-state))))
(def valid-ping-bytes (memoize #(->bytes (valid-ping-cmd))))
(def valid-noop-bytes (memoize #(->bytes (valid-noop-cmd))))
(def valid-frozen-bytes (memoize #(->bytes (valid-frozen-cmd))))
(def valid-rotary-azimuth-bytes (memoize #(->bytes (valid-rotary-azimuth-cmd))))
(def valid-rotary-stop-bytes (memoize #(->bytes (valid-rotary-stop-cmd))))
(def valid-rotary-scan-bytes (memoize #(->bytes (valid-rotary-scan-cmd))))

(def invalid-gps-state-bytes (memoize #(->bytes (invalid-gps-state))))
(def invalid-protocol-state-bytes (memoize #(->bytes (invalid-protocol-state))))
(def invalid-client-cmd-bytes (memoize #(->bytes (invalid-client-type-cmd))))
(def invalid-protocol-cmd-bytes (memoize #(->bytes (invalid-protocol-cmd))))
(def missing-fields-state-bytes (memoize #(->bytes (state-missing-required-fields))))
(def boundary-state-bytes (memoize #(->bytes (state-with-boundary-values))))

;; ============================================================================
;; PERFORMANCE NOTES
;; ============================================================================

(comment
  ;; This harness demonstrates Pronto best practices:
  
  ;; 1. Real data as foundation - using actual captured state
  ;; 2. Mappers defined once and reused
  ;; 3. Direct proto-map creation for new messages
  ;; 4. p/p-> with hints for modifications
  ;; 5. Memoized byte conversions for repeated test use
  ;; 6. No Java builder patterns
  ;; 7. No unnecessary EDN conversions
  ;; 8. Batch operations in single p/p-> calls
  
  ;; Usage in tests:
  ;; (require '[validate.test-harness :as h])
  ;; (validator/validate-binary (h/valid-state-bytes) :type :state)
  )