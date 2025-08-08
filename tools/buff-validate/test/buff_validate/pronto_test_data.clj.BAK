(ns buff-validate.pronto-test-data
  "Idiomatic and performant Pronto-based test data generation.
   This serves as a reference implementation for using Pronto throughout the application."
  (:require [pronto.core :as p])
  (:import [cmd JonSharedCmd$Root JonSharedCmd$Ping JonSharedCmd$Noop JonSharedCmd$Frozen]
           [ser JonSharedData$JonGUIState JonSharedDataTime$JonGuiDataTime
            JonSharedDataSystem$JonGuiDataSystem JonSharedDataGps$JonGuiDataGps
            JonSharedDataCompass$JonGuiDataCompass JonSharedDataRotaryPlatform$JonGuiDataRotaryPlatform
            JonSharedDataCameraDay$JonGuiDataCameraDay JonSharedDataCameraHeat$JonGuiDataCameraHeat
            JonSharedDataGlassHeater$JonGuiDataGlassHeater JonSharedDataMeteoInternal$JonGuiDataMeteoInternal
            JonSharedDataLRF$JonGuiDataLRF JonSharedDataCompassCalibration$JonGuiDataCompassCalibration
            JonSharedDataRecOSD$JonGuiDataRecOSD JonSharedDataActualSpaceTime$JonGuiDataActualSpaceTime]))

;; ============================================================================
;; MAPPER DEFINITIONS
;; Define mappers once and reuse them for performance
;; ============================================================================

(p/defmapper cmd-mapper [cmd.JonSharedCmd$Root])
(p/defmapper state-mapper [ser.JonSharedData$JonGUIState])

;; ============================================================================
;; COMMAND MESSAGE CREATION - Idiomatic Pronto Style
;; ============================================================================

(defn create-ping-cmd
  "Create a valid Ping command using performant Pronto proto-map creation."
  []
  (p/proto-map cmd-mapper cmd.JonSharedCmd$Root
               :protocol_version 1
               :session_id 1000
               :client_type :JON_GUI_DATA_CLIENT_TYPE_LOCAL_NETWORK
               :ping (p/proto-map cmd-mapper cmd.JonSharedCmd$Ping)))

(defn create-noop-cmd
  "Create a valid Noop command."
  []
  (p/proto-map cmd-mapper cmd.JonSharedCmd$Root
               :protocol_version 1
               :session_id 2000
               :client_type :JON_GUI_DATA_CLIENT_TYPE_LOCAL_NETWORK
               :noop (p/proto-map cmd-mapper cmd.JonSharedCmd$Noop)))

(defn create-frozen-cmd
  "Create a valid Frozen command."
  []
  (p/proto-map cmd-mapper cmd.JonSharedCmd$Root
               :protocol_version 1
               :session_id 3000
               :client_type :JON_GUI_DATA_CLIENT_TYPE_LOCAL_NETWORK
               :frozen (p/proto-map cmd-mapper cmd.JonSharedCmd$Frozen)))

(defn create-invalid-client-type-cmd
  "Create a command with invalid client type (0) - should fail validation."
  []
  (p/proto-map cmd-mapper cmd.JonSharedCmd$Root
               :protocol_version 1
               :session_id 4000
               :client_type :JON_GUI_DATA_CLIENT_TYPE_UNSPECIFIED  ; Value 0 - not allowed
               :ping (p/proto-map cmd-mapper cmd.JonSharedCmd$Ping)))

(defn create-invalid-protocol-cmd
  "Create a command with invalid protocol version."
  []
  (p/proto-map cmd-mapper cmd.JonSharedCmd$Root
               :protocol_version 0  ; Invalid: should be > 0
               :session_id 5000
               :client_type :JON_GUI_DATA_CLIENT_TYPE_LOCAL_NETWORK
               :ping (p/proto-map cmd-mapper cmd.JonSharedCmd$Ping)))

;; ============================================================================
;; STATE MESSAGE CREATION - Complete Valid State
;; ============================================================================

(defn create-minimal-valid-state
  "Create a minimal but valid state message with all required fields.
   This demonstrates performant creation of complex nested structures."
  []
  (p/proto-map state-mapper ser.JonSharedData$JonGUIState
               :protocol_version 1
               :time (p/proto-map state-mapper ser.JonSharedDataTime$JonGuiDataTime
                                  :timestamp 1754664759
                                  :manual_timestamp 0
                                  :zone_id 0
                                  :use_manual_time false)
               :system (p/proto-map state-mapper ser.JonSharedDataSystem$JonGuiDataSystem
                                    :cpu_temperature 42.0
                                    :cpu_load 25.0
                                    :gpu_temperature 40.0
                                    :gpu_load 20.0
                                    :memory_used 1000000
                                    :memory_total 2000000
                                    :disk_space 50
                                    :power_consumption 100.0
                                    :rec_enabled false
                                    :low_disk_space false
                                    :loc :JON_GUI_DATA_SYSTEM_LOCALIZATION_EN)
               :gps (p/proto-map state-mapper ser.JonSharedDataGps$JonGuiDataGps
                                 :latitude 50.023632
                                 :longitude 15.815209999999999
                                 :altitude 0.289371
                                 :fix_type :JON_GUI_DATA_GPS_FIX_TYPE_3D)
               :compass (p/proto-map state-mapper ser.JonSharedDataCompass$JonGuiDataCompass
                                     :azimuth 333.50625
                                     :bank 0.84375
                                     :elevation 3.54375)
               :rotary (p/proto-map state-mapper ser.JonSharedDataRotaryPlatform$JonGuiDataRotaryPlatform
                                    :azimuth 333.50626
                                    :elevation 7.04
                                    :mode :JON_GUI_DATA_ROTARY_MODE_POSITION)
               :camera_day (p/proto-map state-mapper ser.JonSharedDataCameraDay$JonGuiDataCameraDay
                                        :zoom_pos 0.59938735
                                        :focus_pos 1.0
                                        :iris_pos 0.03
                                        :digital_zoom_level 1.0
                                        :zoom_table_pos 3
                                        :zoom_table_pos_max 4
                                        :fx_mode :JON_GUI_DATA_FX_MODE_DAY_A
                                        :infrared_filter true
                                        :clahe_level 0.16)
               :camera_heat (p/proto-map state-mapper ser.JonSharedDataCameraHeat$JonGuiDataCameraHeat
                                         :zoom_table_pos 3
                                         :zoom_table_pos_max 4
                                         :digital_zoom_level 1.0
                                         :fx_mode :JON_GUI_DATA_FX_MODE_HEAT_A
                                         :filter :JON_GUI_DATA_VIDEO_CHANNEL_HEAT_FILTER_HOT_WHITE
                                         :agc_mode :JON_GUI_DATA_VIDEO_CHANNEL_HEAT_AGC_MODE_2
                                         :clahe_level 0.5)
               :day_cam_glass_heater (p/proto-map state-mapper ser.JonSharedDataGlassHeater$JonGuiDataGlassHeater)
               :meteo_internal (p/proto-map state-mapper ser.JonSharedDataMeteoInternal$JonGuiDataMeteoInternal)
               :lrf (p/proto-map state-mapper ser.JonSharedDataLRF$JonGuiDataLRF
                                 :measure_id 52
                                 :pointer_mode :JON_GUI_DATA_LRF_LASER_POINTER_MODE_OFF)
               :compass_calibration (p/proto-map state-mapper ser.JonSharedDataCompassCalibration$JonGuiDataCompassCalibration
                                                 :status :JON_GUI_DATA_COMPASS_CALIBRATE_STATUS_NOT_CALIBRATING
                                                 :final_stage 12)
               :rec_osd (p/proto-map state-mapper ser.JonSharedDataRecOSD$JonGuiDataRecOSD
                                     :day_osd_enabled true
                                     :heat_osd_enabled true
                                     :screen :JON_GUI_DATA_REC_OSD_SCREEN_MAIN)
               :actual_space_time (p/proto-map state-mapper ser.JonSharedDataActualSpaceTime$JonGuiDataActualSpaceTime
                                                :timestamp 1754664759
                                                :latitude 50.023632
                                                :longitude 15.81521
                                                :altitude 0.289371
                                                :azimuth 256.62
                                                :elevation 7.04)))

(defn create-invalid-protocol-state
  "Create a state with invalid protocol version and missing required fields."
  []
  (p/proto-map state-mapper ser.JonSharedData$JonGUIState
               :protocol_version 0))  ; Invalid and missing required fields

;; ============================================================================
;; PERFORMANT UPDATE OPERATIONS using p->
;; ============================================================================

(defn update-cmd-session-id
  "Demonstrate performant update of command using p-> macro with hints.
   This is the idiomatic way to update proto-maps."
  [cmd-proto-map new-session-id]
  (p/with-hints [(p/hint cmd-proto-map cmd.JonSharedCmd$Root cmd-mapper)]
    (p/p-> cmd-proto-map
           (assoc :session_id new-session-id))))

(defn update-state-multiple-fields
  "Demonstrate performant multiple field updates using p-> with hints.
   This batches all updates into a single builder round-trip."
  [state-proto-map]
  (p/with-hints [(p/hint state-proto-map ser.JonSharedData$JonGUIState state-mapper)]
    (p/p-> state-proto-map
           (assoc :protocol_version 2)
           (update-in [:system :cpu_load] + 10.0)
           (assoc-in [:gps :latitude] 51.5074)
           (assoc-in [:gps :longitude] -0.1278))))

;; ============================================================================
;; READING OPERATIONS with hints for performance
;; ============================================================================

(defn read-cmd-fields
  "Demonstrate performant reading using p-> with hints.
   This compiles to direct Java getter calls."
  [cmd-proto-map]
  (p/with-hints [(p/hint cmd-proto-map cmd.JonSharedCmd$Root cmd-mapper)]
    {:protocol-version (p/p-> cmd-proto-map :protocol_version)
     :session-id (p/p-> cmd-proto-map :session_id)
     :client-type (p/p-> cmd-proto-map :client_type)}))

(defn read-nested-state-fields
  "Demonstrate performant nested field access."
  [state-proto-map]
  (p/with-hints [(p/hint state-proto-map ser.JonSharedData$JonGUIState state-mapper)]
    {:gps-lat (p/p-> state-proto-map :gps :latitude)
     :gps-lon (p/p-> state-proto-map :gps :longitude)
     :cpu-load (p/p-> state-proto-map :system :cpu_load)}))

;; ============================================================================
;; CONVERSION UTILITIES
;; ============================================================================

(defn proto-map->bytes
  "Convert a proto-map to byte array."
  [proto-map]
  (.toByteArray proto-map))

(defn proto-map->edn
  "Convert proto-map to EDN for inspection.
   Note: For performance-critical code, prefer working with proto-maps directly."
  [proto-map]
  (p/proto-map->clj-map proto-map))

;; ============================================================================
;; BATCH OPERATIONS - Demonstrating efficient batch processing
;; ============================================================================

(defn create-command-batch
  "Create multiple commands efficiently using transients under the hood."
  [session-ids]
  (mapv (fn [session-id]
          (p/proto-map cmd-mapper cmd.JonSharedCmd$Root
                       :protocol_version 1
                       :session_id session-id
                       :client_type :JON_GUI_DATA_CLIENT_TYPE_LOCAL_NETWORK
                       :ping (p/proto-map cmd-mapper cmd.JonSharedCmd$Ping)))
        session-ids))

;; ============================================================================
;; TEST DATA GETTERS - Convenience functions for tests
;; ============================================================================

(defn get-ping-cmd-bytes []
  (proto-map->bytes (create-ping-cmd)))

(defn get-noop-cmd-bytes []
  (proto-map->bytes (create-noop-cmd)))

(defn get-frozen-cmd-bytes []
  (proto-map->bytes (create-frozen-cmd)))

(defn get-valid-state-bytes []
  (proto-map->bytes (create-minimal-valid-state)))

(defn get-invalid-client-type-cmd-bytes []
  (proto-map->bytes (create-invalid-client-type-cmd)))

(defn get-invalid-protocol-cmd-bytes []
  (proto-map->bytes (create-invalid-protocol-cmd)))

(defn get-invalid-protocol-state-bytes []
  (proto-map->bytes (create-invalid-protocol-state)))

;; ============================================================================
;; EXAMPLE: Complex state creation from EDN (when needed)
;; ============================================================================

(defn edn->proto-map
  "Convert EDN to proto-map when receiving data from external sources.
   For internal use, prefer direct proto-map creation for performance."
  [edn-map]
  (cond
    (contains? edn-map :cmd.JonSharedCmd$Root)
    (p/clj-map->proto cmd-mapper edn-map)
    
    (contains? edn-map :ser.JonSharedData$JonGUIState)
    (p/clj-map->proto state-mapper edn-map)
    
    :else
    (throw (ex-info "Unknown message type in EDN" {:keys (keys edn-map)}))))

;; ============================================================================
;; PERFORMANCE NOTES
;; ============================================================================

(comment
  ;; Performance tips for using this as a reference:
  
  ;; 1. Always define mappers once and reuse them
  ;; 2. Use p/proto-map for initial creation with all fields
  ;; 3. Use p/p-> with hints for updates (batches operations)
  ;; 4. Use p/with-hints to avoid repeating hints in a scope
  ;; 5. For reading, use p/p-> with hints for direct getter calls
  ;; 6. Avoid unnecessary conversions to/from EDN in hot paths
  ;; 7. When updating multiple fields, batch them in a single p/p-> call
  
  ;; Example of what NOT to do:
  ;; (-> proto-map
  ;;     (assoc :field1 val1)  ; Multiple builder round-trips!
  ;;     (assoc :field2 val2)
  ;;     (assoc :field3 val3))
  
  ;; Instead do:
  ;; (p/p-> proto-map
  ;;        (assoc :field1 val1)  ; Single builder round-trip
  ;;        (assoc :field2 val2)
  ;;        (assoc :field3 val3))
  )