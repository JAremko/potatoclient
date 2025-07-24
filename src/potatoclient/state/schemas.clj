(ns potatoclient.state.schemas
  "Malli schemas for device state data.
  
  These schemas are converted from buf.validate constraints in the proto files
  and provide runtime validation for EDN data after protobuf deserialization."
  (:require [malli.core :as m]))

;; ============================================================================
;; Common Schemas and Constraints
;; ============================================================================

(def angle-azimuth
  "Azimuth angle in degrees [0, 360)"
  [:double {:min 0.0 :max 359.999999}])

(def angle-elevation
  "Elevation angle in degrees [-90, 90]"
  [:double {:min -90.0 :max 90.0}])

(def angle-bank
  "Bank/roll angle in degrees [-180, 180)"
  [:double {:min -180.0 :max 179.999999}])

(def angle-offset
  "Angle offset in degrees [-180, 180)"
  [:double {:min -180.0 :max 179.999999}])

(def normalized-value
  "Normalized value [0.0, 1.0]"
  [:double {:min 0.0 :max 1.0}])

(def percentage
  "Percentage value [0, 100]"
  [:double {:min 0.0 :max 100.0}])

(def temperature-celsius
  "Temperature in Celsius [-273.15, 660.32]"
  [:double {:min -273.15 :max 660.32}])

(def gps-longitude
  "GPS longitude in degrees [-180, 180]"
  [:double {:min -180.0 :max 180.0}])

(def gps-latitude
  "GPS latitude in degrees [-90, 90]"
  [:double {:min -90.0 :max 90.0}])

(def gps-altitude
  "GPS altitude in meters [-433, 8848.86]"
  [:double {:min -433.0 :max 8848.86}])

(def rgb-value
  "RGB color component [0, 255]"
  [:int {:min 0 :max 255}])

(def distance-decimeters
  "Distance in decimeters [0, 500000] (50km)"
  [:double {:min 0.0 :max 500000.0}])

(def non-negative-int
  "Non-negative integer"
  [:int {:min 0}])

(def zoom-factor
  "Digital zoom factor [1.0, ∞)"
  [:double {:min 1.0}])

(def sun-azimuth
  "Sun azimuth angle [0, 360)"
  [:float {:min 0.0 :max 359.999999}])

(def sun-elevation
  "Sun elevation angle [0, 360) - unusual but matches proto"
  [:float {:min 0.0 :max 359.999999}])

(def power-consumption
  "Power consumption in watts [0, 1000]"
  [:float {:min 0.0 :max 1000.0}])

(def disk-space
  "Disk space percentage [0, 100]"
  [:int {:min 0 :max 100}])

(def timestamp-value
  "Non-negative timestamp"
  [:int {:min 0}])

(def measure-id
  "LRF measure ID [0, ∞)"
  [:int {:min 0}])

(def session-id
  "Session ID [0, ∞)"
  [:int {:min 0}])

(def target-id
  "Target ID [0, ∞)"
  [:int {:min 0}])

(def compass-calibration-stage
  "Compass calibration stage [0, ∞)"
  [:int {:min 0}])

(def compass-calibration-final-stage
  "Compass calibration final stage (> 0)"
  [:int {:min 1}])

;; ============================================================================
;; Enum Schemas
;; ============================================================================

(def gps-fix-type
  "GPS fix type enum (excluding UNSPECIFIED)"
  [:enum 
   "JON_GUI_DATA_GPS_FIX_TYPE_NONE"
   "JON_GUI_DATA_GPS_FIX_TYPE_1D"
   "JON_GUI_DATA_GPS_FIX_TYPE_2D"
   "JON_GUI_DATA_GPS_FIX_TYPE_3D"
   "JON_GUI_DATA_GPS_FIX_TYPE_MANUAL"])

(def system-localization
  "System localization enum (excluding UNSPECIFIED)"
  [:enum
   "JON_GUI_DATA_SYSTEM_LOCALIZATION_EN"
   "JON_GUI_DATA_SYSTEM_LOCALIZATION_UA"
   "JON_GUI_DATA_SYSTEM_LOCALIZATION_AR"
   "JON_GUI_DATA_SYSTEM_LOCALIZATION_CS"])

(def lrf-laser-pointer-mode
  "LRF laser pointer mode enum (excluding UNSPECIFIED)"
  [:enum
   "JON_GUI_DATA_LRF_LASER_POINTER_MODE_OFF"
   "JON_GUI_DATA_LRF_LASER_POINTER_MODE_ON_1"
   "JON_GUI_DATA_LRF_LASER_POINTER_MODE_ON_2"])

(def rotary-mode
  "Rotary platform mode enum (excluding UNSPECIFIED)"
  [:enum
   "JON_GUI_DATA_ROTARY_MODE_INITIALIZATION"
   "JON_GUI_DATA_ROTARY_MODE_SPEED"
   "JON_GUI_DATA_ROTARY_MODE_POSITION"
   "JON_GUI_DATA_ROTARY_MODE_STABILIZATION"
   "JON_GUI_DATA_ROTARY_MODE_TARGETING"
   "JON_GUI_DATA_ROTARY_MODE_VIDEO_TRACKER"])

(def heat-agc-mode
  "Heat camera AGC mode enum (excluding UNSPECIFIED)"
  [:enum
   "JON_GUI_DATA_VIDEO_CHANNEL_HEAT_AGC_MODE_1"
   "JON_GUI_DATA_VIDEO_CHANNEL_HEAT_AGC_MODE_2"
   "JON_GUI_DATA_VIDEO_CHANNEL_HEAT_AGC_MODE_3"])

(def heat-filter
  "Heat camera filter enum (excluding UNSPECIFIED)"
  [:enum
   "JON_GUI_DATA_VIDEO_CHANNEL_HEAT_FILTER_HOT_WHITE"
   "JON_GUI_DATA_VIDEO_CHANNEL_HEAT_FILTER_HOT_BLACK"
   "JON_GUI_DATA_VIDEO_CHANNEL_HEAT_FILTER_SEPIA"
   "JON_GUI_DATA_VIDEO_CHANNEL_HEAT_FILTER_SEPIA_INVERSE"])

(def day-fx-mode
  "Day camera FX mode enum"
  [:enum
   "JON_GUI_DATA_FX_MODE_DAY_DEFAULT"
   "JON_GUI_DATA_FX_MODE_DAY_A"
   "JON_GUI_DATA_FX_MODE_DAY_B"
   "JON_GUI_DATA_FX_MODE_DAY_C"
   "JON_GUI_DATA_FX_MODE_DAY_D"
   "JON_GUI_DATA_FX_MODE_DAY_E"
   "JON_GUI_DATA_FX_MODE_DAY_F"])

(def heat-fx-mode
  "Heat camera FX mode enum"
  [:enum
   "JON_GUI_DATA_FX_MODE_HEAT_DEFAULT"
   "JON_GUI_DATA_FX_MODE_HEAT_A"
   "JON_GUI_DATA_FX_MODE_HEAT_B"
   "JON_GUI_DATA_FX_MODE_HEAT_C"
   "JON_GUI_DATA_FX_MODE_HEAT_D"
   "JON_GUI_DATA_FX_MODE_HEAT_E"
   "JON_GUI_DATA_FX_MODE_HEAT_F"])

(def compass-calibrate-status
  "Compass calibration status enum (excluding UNSPECIFIED)"
  [:enum
   "JON_GUI_DATA_COMPASS_CALIBRATE_STATUS_NOT_CALIBRATING"
   "JON_GUI_DATA_COMPASS_CALIBRATE_STATUS_CALIBRATING_SHORT"
   "JON_GUI_DATA_COMPASS_CALIBRATE_STATUS_CALIBRATING_LONG"
   "JON_GUI_DATA_COMPASS_CALIBRATE_STATUS_FINISHED"
   "JON_GUI_DATA_COMPASS_CALIBRATE_STATUS_ERROR"])

(def time-format
  "Time format enum (excluding UNSPECIFIED)"
  [:enum
   "JON_GUI_DATA_TIME_FORMAT_H_M_S"
   "JON_GUI_DATA_TIME_FORMAT_Y_m_D_H_M_S"])

(def rec-osd-screen
  "Recording OSD screen enum (excluding UNSPECIFIED)"
  [:enum
   "JON_GUI_DATA_REC_OSD_SCREEN_MAIN"
   "JON_GUI_DATA_REC_OSD_SCREEN_LRF_MEASURE"
   "JON_GUI_DATA_REC_OSD_SCREEN_LRF_RESULT"
   "JON_GUI_DATA_REC_OSD_SCREEN_LRF_RESULT_SIMPLIFIED"])

;; ============================================================================
;; Composite Schemas
;; ============================================================================

(def rgb-color-schema
  "RGB color schema"
  [:map
   [:red rgb-value]
   [:green rgb-value]
   [:blue rgb-value]])

(def scan-node-schema
  "Scan node for rotary platform"
  [:map
   [:index non-negative-int]
   [:day-zoom-table-value non-negative-int]
   [:heat-zoom-table-value non-negative-int]
   [:azimuth angle-azimuth]
   [:elevation angle-elevation]
   [:linger [:double {:min 0.0}]]
   [:speed [:double {:min 0.00001 :max 1.0}]]])

(def target-schema
  "LRF target data"
  [:map
   [:timestamp timestamp-value]
   [:target-longitude gps-longitude]
   [:target-latitude gps-latitude]
   [:target-altitude :double]
   [:observer-longitude gps-longitude]
   [:observer-latitude gps-latitude]
   [:observer-altitude :double]
   [:observer-azimuth angle-azimuth]
   [:observer-elevation angle-elevation]
   [:observer-bank angle-bank]
   [:distance-2d distance-decimeters]
   [:distance-3b distance-decimeters]
   [:observer-fix-type gps-fix-type]
   [:session-id session-id]
   [:target-id target-id]
   [:target-color rgb-color-schema]
   [:type :int]
   [:uuid-part1 :int]
   [:uuid-part2 :int]
   [:uuid-part3 :int]
   [:uuid-part4 :int]])

;; ============================================================================
;; Main Subsystem Schemas
;; ============================================================================

(def compass-schema
  "Compass data schema"
  [:map
   [:azimuth angle-azimuth]
   [:elevation angle-elevation]
   [:bank angle-bank]
   [:offset-azimuth angle-offset]
   [:offset-elevation angle-elevation]
   [:magnetic-declination angle-offset]
   [:calibrating :boolean]])

(def system-schema
  "System data schema"
  [:map
   [:cpu-temperature temperature-celsius]
   [:gpu-temperature temperature-celsius]
   [:gpu-load percentage]
   [:cpu-load percentage]
   [:power-consumption power-consumption]
   [:loc system-localization]
   [:cur-video-rec-dir-year non-negative-int]
   [:cur-video-rec-dir-month non-negative-int]
   [:cur-video-rec-dir-day non-negative-int]
   [:cur-video-rec-dir-hour non-negative-int]
   [:cur-video-rec-dir-minute non-negative-int]
   [:cur-video-rec-dir-second non-negative-int]
   [:rec-enabled :boolean]
   [:important-rec-enabled :boolean]
   [:low-disk-space :boolean]
   [:no-disk-space :boolean]
   [:disk-space disk-space]
   [:tracking :boolean]
   [:vampire-mode :boolean]
   [:stabilization-mode :boolean]
   [:geodesic-mode :boolean]
   [:cv-dumping :boolean]])

(def lrf-schema
  "Laser range finder data schema"
  [:map
   [:is-scanning :boolean]
   [:is-measuring :boolean]
   [:measure-id measure-id]
   [:target {:optional true} target-schema]
   [:pointer-mode lrf-laser-pointer-mode]
   [:fog-mode-enabled :boolean]
   [:is-refining :boolean]])

(def gps-schema
  "GPS data schema"
  [:map
   [:longitude gps-longitude]
   [:latitude gps-latitude]
   [:altitude gps-altitude]
   [:manual-longitude gps-longitude]
   [:manual-latitude gps-latitude]
   [:manual-altitude gps-altitude]
   [:fix-type gps-fix-type]
   [:use-manual :boolean]])

(def rotary-schema
  "Rotary platform data schema"
  [:map
   [:azimuth angle-azimuth]
   [:azimuth-speed [:double {:min -1.0 :max 1.0}]]
   [:elevation angle-elevation]
   [:elevation-speed [:double {:min -1.0 :max 1.0}]]
   [:platform-azimuth angle-azimuth]
   [:platform-elevation angle-elevation]
   [:platform-bank angle-bank]
   [:is-moving :boolean]
   [:mode rotary-mode]
   [:is-scanning :boolean]
   [:is-scanning-paused :boolean]
   [:use-rotary-as-compass :boolean]
   [:scan-target non-negative-int]
   [:scan-target-max non-negative-int]
   [:sun-azimuth sun-azimuth]
   [:sun-elevation sun-elevation]
   [:current-scan-node scan-node-schema]])

(def camera-day-schema
  "Day camera data schema"
  [:map
   [:focus-pos normalized-value]
   [:zoom-pos normalized-value]
   [:iris-pos normalized-value]
   [:infrared-filter :boolean]
   [:zoom-table-pos non-negative-int]
   [:zoom-table-pos-max non-negative-int]
   [:fx-mode day-fx-mode]
   [:auto-focus :boolean]
   [:auto-iris :boolean]
   [:digital-zoom-level zoom-factor]
   [:clahe-level normalized-value]])

(def camera-heat-schema
  "Heat camera data schema"
  [:map
   [:zoom-pos normalized-value]
   [:agc-mode heat-agc-mode]
   [:filter heat-filter]
   [:auto-focus :boolean]
   [:zoom-table-pos non-negative-int]
   [:zoom-table-pos-max non-negative-int]
   [:dde-level [:int {:min 0 :max 512}]]
   [:dde-enabled :boolean]
   [:fx-mode heat-fx-mode]
   [:digital-zoom-level zoom-factor]
   [:clahe-level normalized-value]])

(def compass-calibration-schema
  "Compass calibration data schema"
  [:map
   [:stage compass-calibration-stage]
   [:final-stage compass-calibration-final-stage]
   [:target-azimuth angle-azimuth]
   [:target-elevation angle-elevation]
   [:target-bank angle-bank]
   [:status compass-calibrate-status]])

(def time-schema
  "Time data schema"
  [:map
   [:timestamp timestamp-value]
   [:utc-time {:optional true} :string]
   [:format time-format]])

(def rec-osd-schema
  "Recording/OSD data schema"
  [:map
   [:recording :boolean]
   [:osd-enabled :boolean]
   [:screen rec-osd-screen]])

(def day-cam-glass-heater-schema
  "Day camera glass heater schema"
  [:map
   [:enabled :boolean]
   [:auto-mode :boolean]
   [:temperature {:optional true} temperature-celsius]])

(def actual-space-time-schema
  "Actual space-time data schema"
  [:map
   [:timestamp timestamp-value]
   [:data {:optional true} :map]]) ; Schema depends on specific implementation

(def meteo-schema
  "Meteorological data schema"
  [:map
   [:temperature :double]
   [:humidity percentage]
   [:pressure [:double {:min 0.0}]]])

;; ============================================================================
;; Root State Schema
;; ============================================================================

(def jon-gui-state-schema
  "Complete JonGUIState schema"
  [:map
   [:protocol-version [:int {:min 1}]]
   [:system system-schema]
   [:meteo-internal meteo-schema]
   [:lrf lrf-schema]
   [:time time-schema]
   [:gps gps-schema]
   [:compass compass-schema]
   [:rotary rotary-schema]
   [:camera-day camera-day-schema]
   [:camera-heat camera-heat-schema]
   [:compass-calibration compass-calibration-schema]
   [:rec-osd rec-osd-schema]
   [:day-cam-glass-heater day-cam-glass-heater-schema]
   [:actual-space-time actual-space-time-schema]])

;; ============================================================================
;; Validation Functions
;; ============================================================================

(defn validate-state
  "Validate a complete state map against the schema"
  [state-map]
  (m/validate jon-gui-state-schema state-map))

(defn explain-state
  "Get validation errors for a state map"
  [state-map]
  (m/explain jon-gui-state-schema state-map))

(defn validate-subsystem
  "Validate a specific subsystem's data"
  [subsystem-key data]
  (let [schema (case subsystem-key
                 :system system-schema
                 :lrf lrf-schema
                 :time time-schema
                 :gps gps-schema
                 :compass compass-schema
                 :rotary rotary-schema
                 :camera-day camera-day-schema
                 :camera-heat camera-heat-schema
                 :compass-calibration compass-calibration-schema
                 :rec-osd rec-osd-schema
                 :day-cam-glass-heater day-cam-glass-heater-schema
                 :actual-space-time actual-space-time-schema
                 :meteo-internal meteo-schema
                 (throw (ex-info "Unknown subsystem" {:subsystem subsystem-key})))]
    (m/validate schema data)))

;; ============================================================================
;; Schema Registry
;; ============================================================================

(def all-schemas
  "Map of all subsystem schemas for iteration"
  {:system system-schema
   :lrf lrf-schema
   :time time-schema
   :gps gps-schema
   :compass compass-schema
   :rotary rotary-schema
   :camera-day camera-day-schema
   :camera-heat camera-heat-schema
   :compass-calibration compass-calibration-schema
   :rec-osd rec-osd-schema
   :day-cam-glass-heater day-cam-glass-heater-schema
   :actual-space-time actual-space-time-schema
   :meteo-internal meteo-schema})