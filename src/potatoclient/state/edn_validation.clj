(ns potatoclient.state.edn-validation
  "Comprehensive validation specs for EDN state data.
  
  These specs mirror the protobuf validation constraints to ensure
  data integrity after proto->EDN conversion."
  (:require [malli.core :as m]
            [malli.error :as me]
            [com.fulcrologic.guardrails.malli.core :refer [=> >defn >defn- ?]]
            [potatoclient.logging :as logging]))

;; ============================================================================
;; Enums - matching protobuf enum definitions
;; ============================================================================

(def gps-fix-type
  "GPS fix type enum values"
  [:enum
   "JON_GUI_DATA_GPS_FIX_TYPE_NONE"
   "JON_GUI_DATA_GPS_FIX_TYPE_1D"
   "JON_GUI_DATA_GPS_FIX_TYPE_2D"
   "JON_GUI_DATA_GPS_FIX_TYPE_3D"
   "JON_GUI_DATA_GPS_FIX_TYPE_MANUAL"])

(def system-localization
  "System localization enum values"
  [:enum
   "JON_GUI_DATA_SYSTEM_LOCALIZATION_EN"
   "JON_GUI_DATA_SYSTEM_LOCALIZATION_UA"
   "JON_GUI_DATA_SYSTEM_LOCALIZATION_AR"
   "JON_GUI_DATA_SYSTEM_LOCALIZATION_CS"])

(def compass-units
  "Compass units enum values"
  [:enum
   "JON_GUI_DATA_COMPASS_UNITS_DEGREES"
   "JON_GUI_DATA_COMPASS_UNITS_MILS"
   "JON_GUI_DATA_COMPASS_UNITS_GRAD"
   "JON_GUI_DATA_COMPASS_UNITS_MRAD"])

(def rotary-direction
  "Rotary direction enum values"
  [:enum
   "JON_GUI_DATA_ROTARY_DIRECTION_CLOCKWISE"
   "JON_GUI_DATA_ROTARY_DIRECTION_COUNTER_CLOCKWISE"])

(def rotary-mode
  "Rotary mode enum values"
  [:enum
   "JON_GUI_DATA_ROTARY_MODE_INITIALIZATION"
   "JON_GUI_DATA_ROTARY_MODE_SPEED"
   "JON_GUI_DATA_ROTARY_MODE_POSITION"
   "JON_GUI_DATA_ROTARY_MODE_STABILIZATION"
   "JON_GUI_DATA_ROTARY_MODE_TARGETING"
   "JON_GUI_DATA_ROTARY_MODE_VIDEO_TRACKER"])

(def lrf-scan-mode
  "LRF scan mode enum values"
  [:enum
   "JON_GUI_DATA_LRF_SCAN_MODE_1_HZ_CONTINUOUS"
   "JON_GUI_DATA_LRF_SCAN_MODE_4_HZ_CONTINUOUS"
   "JON_GUI_DATA_LRF_SCAN_MODE_10_HZ_CONTINUOUS"
   "JON_GUI_DATA_LRF_SCAN_MODE_20_HZ_CONTINUOUS"
   "JON_GUI_DATA_LRF_SCAN_MODE_100_HZ_CONTINUOUS"
   "JON_GUI_DATA_LRF_SCAN_MODE_200_HZ_CONTINUOUS"])

(def heat-filter
  "Heat camera filter enum values"
  [:enum
   "JON_GUI_DATA_VIDEO_CHANNEL_HEAT_FILTER_HOT_WHITE"
   "JON_GUI_DATA_VIDEO_CHANNEL_HEAT_FILTER_HOT_BLACK"
   "JON_GUI_DATA_VIDEO_CHANNEL_HEAT_FILTER_SEPIA"
   "JON_GUI_DATA_VIDEO_CHANNEL_HEAT_FILTER_SEPIA_INVERSE"])

(def heat-agc-mode
  "Heat camera AGC mode enum values"
  [:enum
   "JON_GUI_DATA_VIDEO_CHANNEL_HEAT_AGC_MODE_1"
   "JON_GUI_DATA_VIDEO_CHANNEL_HEAT_AGC_MODE_2"
   "JON_GUI_DATA_VIDEO_CHANNEL_HEAT_AGC_MODE_3"])

(def compass-calibrate-status
  "Compass calibration status enum values"
  [:enum
   "JON_GUI_DATA_COMPASS_CALIBRATE_STATUS_NOT_CALIBRATING"
   "JON_GUI_DATA_COMPASS_CALIBRATE_STATUS_CALIBRATING_SHORT"
   "JON_GUI_DATA_COMPASS_CALIBRATE_STATUS_CALIBRATING_LONG"
   "JON_GUI_DATA_COMPASS_CALIBRATE_STATUS_FINISHED"
   "JON_GUI_DATA_COMPASS_CALIBRATE_STATUS_ERROR"])

;; ============================================================================
;; Constraint helpers
;; ============================================================================

(defn- between 
  "Creates a spec for a number between min and max (inclusive)"
  [min max]
  [:and number? [:>= min] [:<= max]])

(defn- between-exclusive-max
  "Creates a spec for a number between min (inclusive) and max (exclusive)"
  [min max]
  [:and number? [:>= min] [:< max]])

(defn- gte 
  "Creates a spec for a number greater than or equal to min"
  [min]
  [:and number? [:>= min]])

;; ============================================================================
;; System subsystem validation
;; ============================================================================

(def system-schema
  "Validation schema for system subsystem matching protobuf constraints"
  [:map {:closed true}
   [:cpu-temperature (between -273.15 150.0)]
   [:gpu-temperature (between -273.15 150.0)]
   [:gpu-load (between 0.0 100.0)]
   [:cpu-load (between 0.0 100.0)]
   [:power-consumption (between 0.0 1000.0)]
   [:loc system-localization]
   [:cur-video-rec-dir-year (gte 0)]
   [:cur-video-rec-dir-month (gte 0)]
   [:cur-video-rec-dir-day (gte 0)]
   [:cur-video-rec-dir-hour (gte 0)]
   [:cur-video-rec-dir-minute (gte 0)]
   [:cur-video-rec-dir-second (gte 0)]
   [:rec-enabled boolean?]
   [:important-rec-enabled boolean?]
   [:low-disk-space boolean?]
   [:no-disk-space boolean?]
   [:disk-space (between 0 100)]
   [:tracking boolean?]
   [:vampire-mode boolean?]
   [:stabilization-mode boolean?]
   [:geodesic-mode boolean?]
   [:cv-dumping boolean?]])

;; ============================================================================
;; GPS subsystem validation
;; ============================================================================

(def gps-schema
  "Validation schema for GPS subsystem matching protobuf constraints"
  [:map {:closed true}
   [:longitude (between -180.0 180.0)]
   [:latitude (between -90.0 90.0)]
   [:altitude (between -433.0 8848.86)]  ; Dead Sea to Everest
   [:manual-longitude (between -180.0 180.0)]
   [:manual-latitude (between -90.0 90.0)]
   [:manual-altitude (between -433.0 8848.86)]
   [:fix-type gps-fix-type]
   [:use-manual boolean?]])

;; ============================================================================
;; Time subsystem validation
;; ============================================================================

(def time-schema
  "Validation schema for time subsystem matching protobuf constraints"
  [:map {:closed true}
   [:timestamp (gte 0)]
   [:manual-timestamp (gte 0)]
   [:zone-id int?]
   [:use-manual-time boolean?]])

;; ============================================================================
;; Compass subsystem validation
;; ============================================================================

(def compass-schema
  "Validation schema for compass subsystem matching protobuf constraints"
  [:map {:closed true}
   [:azimuth (between-exclusive-max 0.0 360.0)]
   [:elevation (between -90.0 90.0)]
   [:bank (between-exclusive-max -180.0 180.0)]
   [:offset-azimuth (between-exclusive-max -180.0 180.0)]
   [:offset-elevation (between -90.0 90.0)]
   [:magnetic-declination (between-exclusive-max -180.0 180.0)]
   [:calibrating boolean?]])

;; ============================================================================
;; LRF subsystem validation
;; ============================================================================

(def rgb-color-schema
  "RGB color validation schema"
  [:map {:closed true}
   [:red (between 0 255)]
   [:green (between 0 255)]
   [:blue (between 0 255)]])

(def lrf-target-schema
  "LRF target validation schema"
  [:map {:closed true}
   [:timestamp (gte 0)]
   [:target-longitude (between -180.0 180.0)]
   [:target-latitude (between -90.0 90.0)]
   [:target-altitude number?]
   [:observer-longitude (between -180.0 180.0)]
   [:observer-latitude (between -90.0 90.0)]
   [:observer-altitude number?]
   [:observer-azimuth (between-exclusive-max 0.0 360.0)]
   [:observer-elevation (between -90.0 90.0)]
   [:observer-bank (between-exclusive-max -180.0 180.0)]
   [:distance-2d (between 0.0 500000.0)]  ; 50km in decimeters
   [:distance-3b (between 0.0 500000.0)]  ; 50km in decimeters
   [:observer-fix-type gps-fix-type]
   [:session-id (gte 0)]
   [:target-id (gte 0)]
   [:target-color rgb-color-schema]
   [:type nat-int?]
   [:uuid-part1 int?]
   [:uuid-part2 int?]
   [:uuid-part3 int?]
   [:uuid-part4 int?]])

(def lrf-laser-pointer-mode
  "LRF laser pointer mode enum values"
  [:enum
   "JON_GUI_DATA_LRF_LASER_POINTER_MODE_OFF"
   "JON_GUI_DATA_LRF_LASER_POINTER_MODE_ON_1"
   "JON_GUI_DATA_LRF_LASER_POINTER_MODE_ON_2"])

(def lrf-schema
  "Validation schema for LRF subsystem matching protobuf constraints"
  [:map {:closed true}
   [:is-scanning boolean?]
   [:is-measuring boolean?]
   [:measure-id (gte 0)]
   [:target lrf-target-schema]
   [:pointer-mode lrf-laser-pointer-mode]
   [:fog-mode-enabled boolean?]
   [:is-refining boolean?]])

;; ============================================================================
;; Rotary subsystem validation
;; ============================================================================

(def scan-node-schema
  "Scan node validation schema"
  [:map {:closed true}
   [:index (gte 0)]
   [:day-zoom-table-value (gte 0)]
   [:heat-zoom-table-value (gte 0)]
   [:azimuth (between-exclusive-max 0.0 360.0)]
   [:elevation (between -90.0 90.0)]
   [:linger (gte 0.0)]
   [:speed [:and number? [:> 0.0] [:<= 1.0]]]])

(def rotary-schema
  "Validation schema for rotary platform subsystem matching protobuf constraints"
  [:map {:closed true}
   [:azimuth (between-exclusive-max 0.0 360.0)]
   [:azimuth-speed (between -1.0 1.0)]
   [:elevation (between -90.0 90.0)]
   [:elevation-speed (between -1.0 1.0)]
   [:platform-azimuth (between-exclusive-max 0.0 360.0)]
   [:platform-elevation (between -90.0 90.0)]
   [:platform-bank (between-exclusive-max -180.0 180.0)]
   [:is-moving boolean?]
   [:mode rotary-mode]
   [:is-scanning boolean?]
   [:is-scanning-paused boolean?]
   [:use-rotary-as-compass boolean?]
   [:scan-target (gte 0)]
   [:scan-target-max (gte 0)]
   [:sun-azimuth (between-exclusive-max 0.0 360.0)]
   [:sun-elevation (between-exclusive-max 0.0 360.0)]
   [:current-scan-node scan-node-schema]])

;; ============================================================================
;; Camera Day subsystem validation
;; ============================================================================

(def fx-mode-day
  "Day camera FX mode enum values"
  [:enum
   "JON_GUI_DATA_FX_MODE_DAY_DEFAULT"
   "JON_GUI_DATA_FX_MODE_DAY_A"
   "JON_GUI_DATA_FX_MODE_DAY_B"
   "JON_GUI_DATA_FX_MODE_DAY_C"
   "JON_GUI_DATA_FX_MODE_DAY_D"
   "JON_GUI_DATA_FX_MODE_DAY_E"
   "JON_GUI_DATA_FX_MODE_DAY_F"])

(def camera-day-schema
  "Validation schema for day camera subsystem matching protobuf constraints"
  [:map {:closed true}
   [:focus-pos (between 0.0 1.0)]
   [:zoom-pos (between 0.0 1.0)]
   [:iris-pos (between 0.0 1.0)]
   [:infrared-filter boolean?]
   [:zoom-table-pos (gte 0)]
   [:zoom-table-pos-max (gte 0)]
   [:fx-mode fx-mode-day]
   [:auto-focus boolean?]
   [:auto-iris boolean?]
   [:digital-zoom-level (gte 1.0)]
   [:clahe-level (between 0.0 1.0)]])

;; ============================================================================
;; Camera Heat subsystem validation
;; ============================================================================

(def fx-mode-heat
  "Heat camera FX mode enum values"
  [:enum
   "JON_GUI_DATA_FX_MODE_HEAT_DEFAULT"
   "JON_GUI_DATA_FX_MODE_HEAT_A"
   "JON_GUI_DATA_FX_MODE_HEAT_B"
   "JON_GUI_DATA_FX_MODE_HEAT_C"
   "JON_GUI_DATA_FX_MODE_HEAT_D"
   "JON_GUI_DATA_FX_MODE_HEAT_E"
   "JON_GUI_DATA_FX_MODE_HEAT_F"])

(def camera-heat-schema
  "Validation schema for heat camera subsystem matching protobuf constraints"
  [:map {:closed true}
   [:zoom-pos (between 0.0 1.0)]
   [:agc-mode heat-agc-mode]
   [:filter heat-filter]
   [:auto-focus boolean?]
   [:zoom-table-pos (gte 0)]
   [:zoom-table-pos-max (gte 0)]
   [:dde-level (between 0 512)]
   [:dde-enabled boolean?]
   [:fx-mode fx-mode-heat]
   [:digital-zoom-level (gte 1.0)]
   [:clahe-level (between 0.0 1.0)]])

;; ============================================================================
;; Compass Calibration subsystem validation
;; ============================================================================

(def compass-calibration-schema
  "Validation schema for compass calibration subsystem matching protobuf constraints"
  [:map {:closed true}
   [:stage (gte 0)]
   [:final-stage pos-int?]
   [:target-azimuth (between-exclusive-max 0.0 360.0)]
   [:target-elevation (between -90.0 90.0)]
   [:target-bank (between-exclusive-max -180.0 180.0)]
   [:status compass-calibrate-status]])

;; ============================================================================
;; Recording OSD subsystem validation
;; ============================================================================

(def rec-osd-screen
  "Recording OSD screen enum values"
  [:enum
   "JON_GUI_DATA_REC_OSD_SCREEN_MAIN"
   "JON_GUI_DATA_REC_OSD_SCREEN_LRF_MEASURE"
   "JON_GUI_DATA_REC_OSD_SCREEN_LRF_RESULT"
   "JON_GUI_DATA_REC_OSD_SCREEN_LRF_RESULT_SIMPLIFIED"])

(def rec-osd-schema
  "Validation schema for recording OSD subsystem matching protobuf constraints"
  [:map {:closed true}
   [:screen rec-osd-screen]
   [:heat-osd-enabled boolean?]
   [:day-osd-enabled boolean?]
   [:heat-crosshair-offset-horizontal int?]
   [:heat-crosshair-offset-vertical int?]
   [:day-crosshair-offset-horizontal int?]
   [:day-crosshair-offset-vertical int?]])

;; ============================================================================
;; Day Camera Glass Heater subsystem validation
;; ============================================================================

(def day-cam-glass-heater-schema
  "Validation schema for day camera glass heater subsystem matching protobuf constraints"
  [:map {:closed true}
   [:temperature (between -273.15 660.32)]  ; Absolute zero to melting point of aluminum
   [:status boolean?]])

;; ============================================================================
;; Actual Space-Time subsystem validation
;; ============================================================================

(def actual-space-time-schema
  "Validation schema for actual space-time subsystem matching protobuf constraints"
  [:map {:closed true}
   [:azimuth (between-exclusive-max 0.0 360.0)]
   [:elevation (between -90.0 90.0)]
   [:bank (between-exclusive-max -180.0 180.0)]
   [:latitude (between -90.0 90.0)]
   [:longitude (between-exclusive-max -180.0 180.0)]
   [:altitude (between -433.0 8848.86)]  ; Dead Sea to Everest
   [:timestamp (gte 0)]])

;; ============================================================================
;; Meteo subsystem validation (internal)
;; ============================================================================

(def meteo-schema
  "Validation schema for meteo data"
  [:map {:closed true}
   [:temperature {:optional true} number?]
   [:humidity {:optional true} (between 0 100)]
   [:pressure {:optional true} (gte 0)]])

(def meteo-internal-schema
  "Validation schema for internal meteo subsystem"
  [:map {:closed true}
   [:meteo {:optional true} meteo-schema]])

;; ============================================================================
;; Complete GUI state validation
;; ============================================================================

(def gui-state-schema
  "Complete validation schema for GUI state matching protobuf structure.
  Note: In protobuf all subsystems are required, but in EDN they're optional
  since we may receive partial updates."
  [:map {:closed true}
   [:protocol-version pos-int?]
   [:system {:optional true} system-schema]
   [:gps {:optional true} gps-schema]
   [:time {:optional true} time-schema]
   [:compass {:optional true} compass-schema]
   [:lrf {:optional true} lrf-schema]
   [:rotary {:optional true} rotary-schema]
   [:camera-day {:optional true} camera-day-schema]
   [:camera-heat {:optional true} camera-heat-schema]
   [:compass-calibration {:optional true} compass-calibration-schema]
   [:rec-osd {:optional true} rec-osd-schema]
   [:day-cam-glass-heater {:optional true} day-cam-glass-heater-schema]
   [:actual-space-time {:optional true} actual-space-time-schema]
   [:meteo-internal {:optional true} meteo-internal-schema]])

;; ============================================================================
;; Validation functions
;; ============================================================================

(>defn validate-edn-state
  "Validate an EDN state map against the GUI state schema.
  Returns the state if valid, logs error and returns nil if invalid."
  [state]
  [map? => (? map?)]
  (if (m/validate gui-state-schema state)
    state
    (do
      (logging/log-error {:msg "EDN state validation failed"
                          :errors (me/humanize (m/explain gui-state-schema state))})
      nil)))

(>defn validate-subsystem
  "Validate a specific subsystem against its schema.
  Returns the subsystem data if valid, logs error and returns nil if invalid."
  [subsystem-key data]
  [keyword? map? => (? map?)]
  (let [schema (case subsystem-key
                 :system system-schema
                 :gps gps-schema
                 :time time-schema
                 :compass compass-schema
                 :lrf lrf-schema
                 :rotary rotary-schema
                 :camera-day camera-day-schema
                 :camera-heat camera-heat-schema
                 :compass-calibration compass-calibration-schema
                 :rec-osd rec-osd-schema
                 :day-cam-glass-heater day-cam-glass-heater-schema
                 :actual-space-time actual-space-time-schema
                 :meteo-internal meteo-internal-schema
                 nil)]
    (if (and schema (m/validate schema data))
      data
      (do
        (logging/log-error {:msg "Subsystem validation failed"
                            :subsystem subsystem-key
                            :errors (when schema
                                      (me/humanize (m/explain schema data)))})
        nil))))

(>defn explain-validation-error
  "Get human-readable explanation of validation errors for a state"
  [state]
  [map? => (? string?)]
  (when-not (m/validate gui-state-schema state)
    (pr-str (me/humanize (m/explain gui-state-schema state)))))

(>defn validation-enabled?
  "Check if validation is enabled (only in dev/test)"
  []
  [=> boolean?]
  (= "true" (System/getProperty "potatoclient.validation.enabled" "false")))