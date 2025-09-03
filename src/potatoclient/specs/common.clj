(ns potatoclient.specs.common
  "Common reusable specs for all potatoclient applications.
   All map specs use {:closed true} to catch typos and invalid keys.
   Specs match exact buf.validate constraints from proto files."
  (:require
    [malli.core :as m]
    [malli.generator :as mg]
    [potatoclient.malli.registry :as registry]
    [clojure.test.check.generators :as gen]))

;; ====================================================================
;; Basic type specs
;; ====================================================================
(def nat-int-spec
  [:and :int [:>= 0]])

(def bytes-spec
  "Spec for byte array (Java byte[])."
  [:fn {:error/message "must be a byte array"}
   #(instance? (Class/forName "[B") %)])

;; ====================================================================
;; Angle specs (degrees)
;; ====================================================================
;; Azimuth: 0-360 degrees (compass heading)
;; All proto fields now use double
(def azimuth-spec
  [:and [:double {:min 0.0 :max 360.0}]
   [:< 360.0]])

(def elevation-spec
  [:double {:min -90.0 :max 90.0}])

(def bank-spec
  [:and [:double {:min -180.0 :max 180.0}]
   [:< 180.0]])

;; Offset/Relative angle specs (for compass offsets and relative rotations)
(def offset-azimuth-spec
  [:and [:double {:min -180.0 :max 180.0}]
   [:< 180.0]])

(def offset-elevation-spec
  [:double {:min -90.0 :max 90.0}])

(def relative-azimuth-spec
  [:and [:double {:min -180.0 :max 180.0}]
   [:< 180.0]])

(def relative-elevation-spec
  [:double {:min -90.0 :max 90.0}])

(def magnetic-declination-spec
  [:and [:double {:min -180.0 :max 180.0}]
   [:< 180.0]])

;; Sun elevation spec - sun can be below horizon but proto incorrectly constrains to 0-360
;; We'll match proto's constraint even though it's semantically wrong
(def sun-elevation-spec
  [:and [:double {:min 0.0 :max 360.0}]
   [:< 360.0]])

;; Register angle specs
(registry/register-spec! :angle/azimuth azimuth-spec)
(registry/register-spec! :angle/elevation elevation-spec)
(registry/register-spec! :angle/bank bank-spec)
;; Special case for sun elevation (proto bug)
(registry/register-spec! :angle/sun-elevation sun-elevation-spec)
;; Offset angles
(registry/register-spec! :angle/offset-azimuth offset-azimuth-spec)
(registry/register-spec! :angle/offset-elevation offset-elevation-spec)
(registry/register-spec! :angle/relative-azimuth relative-azimuth-spec)
(registry/register-spec! :angle/relative-elevation relative-elevation-spec)
(registry/register-spec! :angle/magnetic-declination magnetic-declination-spec)

;; ====================================================================
;; Speed specs (normalized 0-1)
;; ====================================================================
(def normalized-speed-spec
  [:and [:double {:min 0.0 :max 1.0}]
   [:> 0.0]])

(registry/register-spec! :speed/normalized normalized-speed-spec)

;; ====================================================================
;; Range specs
;; ====================================================================
;; Range values in proto (zoom_pos, focus_pos, iris_pos, clahe_level)

(def normalized-range-spec
  [:double {:min 0.0 :max 1.0}])

(def normalized-offset-spec
  [:double {:min -1.0 :max 1.0}])

(def zoom-level-spec normalized-range-spec)
(def focus-level-spec normalized-range-spec)

;; Digital zoom spec (must be >= 1.0, with reasonable bounds)
(def digital-zoom-spec
  [:double {:min 1.0 :max 100.0}])

;; Register range specs
(registry/register-spec! :range/normalized normalized-range-spec)
(registry/register-spec! :range/normalized-offset normalized-offset-spec)
(registry/register-spec! :range/zoom zoom-level-spec)
(registry/register-spec! :range/focus focus-level-spec)
(registry/register-spec! :range/digital-zoom digital-zoom-spec)

;; ====================================================================
;; GPS Position specs (EXACT buf.validate constraints)
;; ====================================================================
;; Latitude: double ∈ [-90, 90]
(def latitude-spec
  [:double {:min -90.0 :max 90.0}])

;; Longitude: double ∈ [-180, 180] (note: some proto fields use < 180)
(def longitude-spec
  [:and [:double {:min -180.0 :max 180.0}]
   [:< 180.0]])

;; Altitude: double ∈ [-430, 100000] Kármán line (Dead Sea shore to edge of space)
(def altitude-spec
  [:double {:min -430.0 :max 100000.0}])

;; Register position specs
(registry/register-spec! :position/latitude latitude-spec)
(registry/register-spec! :position/longitude longitude-spec)
(registry/register-spec! :position/altitude altitude-spec)

;; Temperature specs
;; Temperature specs
(def component-temperature-spec
  [:double {:min -273.15 :max 150.0}])

;; Register temperature specs
(registry/register-spec! :temperature/component component-temperature-spec)

;; Distance specs
(def distance-decimeters-spec
  [:double {:min 0.0 :max 50000.0}])

;; Register distance specs
(registry/register-spec! :distance/meters distance-decimeters-spec)

;; Screen coordinate specs (NDC - Normalized Device Coordinates)
(def ndc-coord-clamped-spec
  [:double {:min -1.0 :max 1.0}])

;; Register NDC specs
(registry/register-spec! :screen/ndc-x ndc-coord-clamped-spec)
(registry/register-spec! :screen/ndc-y ndc-coord-clamped-spec)

;; Pixel coordinate specs
(def pixel-coord-spec
  [:int {:min 0}])

;; Pixel offset specs (for LRF alignment)
(def pixel-offset-x-spec
  [:int {:min -1920 :max 1920}])

(def pixel-offset-y-spec
  [:int {:min -1080 :max 1080}])

;; Register pixel specs
(registry/register-spec! :screen/pixel-x pixel-coord-spec)
(registry/register-spec! :screen/pixel-y pixel-coord-spec)
(registry/register-spec! :screen/pixel-offset-x pixel-offset-x-spec)
(registry/register-spec! :screen/pixel-offset-y pixel-offset-y-spec)

;; Integer type specs for protobuf compatibility
;; int32: -2147483648 to 2147483647
;; uint32: 0 to 4294967295 in protobuf, but in Java it's stored as signed int
;;         so we need to limit to int32 max to avoid overflow
(def int32-spec
  [:int {:min -2147483648 :max 2147483647}])

(def uint32-spec
  ;; Even though protobuf uint32 can be 0 to 4294967295,
  ;; in Java it's represented as signed int, so max is 2147483647
  [:int {:min 0 :max 2147483647}])

(def int32-positive-spec
  [:int {:min 0 :max 2147483647}])

;; Protocol and session specs
(def protocol-version-spec
  ;; uint32 with gt: 0 constraint (must be > 0)
  [:and :proto/uint32 [:> 0]])

(def session-id-spec
  ;; uint32 with no additional constraints (can be 0)
  :proto/uint32)

;; Time specs
(def unix-timestamp-spec
  [:int {:min 0 :max 2147483647}])

(def unix-timestamp-int64-spec
  ;; int64 timestamp (milliseconds since epoch)
  [:int {:min 0 :max 9223372036854775807}])

(def frame-time-spec
  ;; uint64 frame time in nanoseconds/microseconds
  [:int {:min 0 :max 9223372036854775807}])

;; Register integer specs
(registry/register-spec! :nat-int nat-int-spec)
(registry/register-spec! :bytes bytes-spec)
(registry/register-spec! :proto/int32 int32-spec)
(registry/register-spec! :proto/uint32 uint32-spec)
(registry/register-spec! :proto/int32-positive int32-positive-spec)
(registry/register-spec! :proto/protocol-version protocol-version-spec)
(registry/register-spec! :proto/session-id session-id-spec)

;; Register time specs
(registry/register-spec! :time/unix-timestamp unix-timestamp-spec)
(registry/register-spec! :time/unix-timestamp-int64 unix-timestamp-int64-spec)
(registry/register-spec! :time/duration-seconds unix-timestamp-spec)
(registry/register-spec! :time/frame-time frame-time-spec)

;; Percentage specs
;; Percentage specs (cpu_load, gpu_load, etc.)
(def percentage-spec
  [:double {:min 0.0 :max 100.0}])

;; Register percentage spec
(registry/register-spec! :percentage percentage-spec)

;; ====================================================================
;; Enum specs (Using keywords as in Pronto EDN output)
;; ====================================================================

;; Client Type (Cannot be UNSPECIFIED per buf.validate)
(def client-type-enum-spec
  [:enum
   :JON_GUI_DATA_CLIENT_TYPE_INTERNAL_CV
   :JON_GUI_DATA_CLIENT_TYPE_LOCAL_NETWORK
   :JON_GUI_DATA_CLIENT_TYPE_CERTIFICATE_PROTECTED
   :JON_GUI_DATA_CLIENT_TYPE_LIRA])

;; GPS Fix Type (Cannot be UNSPECIFIED per buf.validate)
(def gps-fix-type-enum-spec
  [:enum
   :JON_GUI_DATA_GPS_FIX_TYPE_NONE
   :JON_GUI_DATA_GPS_FIX_TYPE_1D
   :JON_GUI_DATA_GPS_FIX_TYPE_2D
   :JON_GUI_DATA_GPS_FIX_TYPE_3D
   :JON_GUI_DATA_GPS_FIX_TYPE_MANUAL])

;; Rotary Direction (Cannot be UNSPECIFIED per buf.validate)
(def rotary-direction-enum-spec
  [:enum
   :JON_GUI_DATA_ROTARY_DIRECTION_CLOCKWISE
   :JON_GUI_DATA_ROTARY_DIRECTION_COUNTER_CLOCKWISE])

;; Rotary Mode (Cannot be UNSPECIFIED per buf.validate)
(def rotary-mode-enum-spec
  [:enum
   :JON_GUI_DATA_ROTARY_MODE_INITIALIZATION
   :JON_GUI_DATA_ROTARY_MODE_SPEED
   :JON_GUI_DATA_ROTARY_MODE_POSITION
   :JON_GUI_DATA_ROTARY_MODE_STABILIZATION
   :JON_GUI_DATA_ROTARY_MODE_TARGETING
   :JON_GUI_DATA_ROTARY_MODE_VIDEO_TRACKER])

;; Video Channel (Cannot be UNSPECIFIED per buf.validate)
(def video-channel-enum-spec
  [:enum
   :JON_GUI_DATA_VIDEO_CHANNEL_HEAT
   :JON_GUI_DATA_VIDEO_CHANNEL_DAY])

(def fx-mode-day-enum-spec
  [:enum
   ;; Note: DEFAULT is not allowed by buf.validate (not_in: [0])
   :JON_GUI_DATA_FX_MODE_DAY_A
   :JON_GUI_DATA_FX_MODE_DAY_B
   :JON_GUI_DATA_FX_MODE_DAY_C
   :JON_GUI_DATA_FX_MODE_DAY_D
   :JON_GUI_DATA_FX_MODE_DAY_E
   :JON_GUI_DATA_FX_MODE_DAY_F])

(def fx-mode-heat-enum-spec
  [:enum
   ;; Note: DEFAULT is not allowed by buf.validate (not_in: [0])
   :JON_GUI_DATA_FX_MODE_HEAT_A
   :JON_GUI_DATA_FX_MODE_HEAT_B
   :JON_GUI_DATA_FX_MODE_HEAT_C
   :JON_GUI_DATA_FX_MODE_HEAT_D
   :JON_GUI_DATA_FX_MODE_HEAT_E
   :JON_GUI_DATA_FX_MODE_HEAT_F])

;; Heat Camera Filter enum (thermal visualization modes)
(def heat-filter-enum-spec
  [:enum
   :JON_GUI_DATA_VIDEO_CHANNEL_HEAT_FILTER_HOT_WHITE
   :JON_GUI_DATA_VIDEO_CHANNEL_HEAT_FILTER_HOT_BLACK
   :JON_GUI_DATA_VIDEO_CHANNEL_HEAT_FILTER_SEPIA
   :JON_GUI_DATA_VIDEO_CHANNEL_HEAT_FILTER_SEPIA_INVERSE])

;; Heat Camera AGC Mode enum
(def heat-agc-mode-enum-spec
  [:enum
   :JON_GUI_DATA_VIDEO_CHANNEL_HEAT_AGC_MODE_1
   :JON_GUI_DATA_VIDEO_CHANNEL_HEAT_AGC_MODE_2
   :JON_GUI_DATA_VIDEO_CHANNEL_HEAT_AGC_MODE_3])

(def lrf-scan-modes-enum-spec
  [:enum
   :JON_GUI_DATA_LRF_SCAN_MODE_1_HZ_CONTINUOUS
   :JON_GUI_DATA_LRF_SCAN_MODE_4_HZ_CONTINUOUS
   :JON_GUI_DATA_LRF_SCAN_MODE_10_HZ_CONTINUOUS
   :JON_GUI_DATA_LRF_SCAN_MODE_20_HZ_CONTINUOUS
   :JON_GUI_DATA_LRF_SCAN_MODE_100_HZ_CONTINUOUS
   :JON_GUI_DATA_LRF_SCAN_MODE_200_HZ_CONTINUOUS])

(def compass-calibrate-status-enum-spec
  [:enum
   :JON_GUI_DATA_COMPASS_CALIBRATE_STATUS_NOT_CALIBRATING
   :JON_GUI_DATA_COMPASS_CALIBRATE_STATUS_CALIBRATING_SHORT
   :JON_GUI_DATA_COMPASS_CALIBRATE_STATUS_CALIBRATING_LONG
   :JON_GUI_DATA_COMPASS_CALIBRATE_STATUS_FINISHED
   :JON_GUI_DATA_COMPASS_CALIBRATE_STATUS_ERROR])

;; System Localizations enum (Cannot be UNSPECIFIED per buf.validate)
(def system-localizations-enum-spec
  [:enum
   :JON_GUI_DATA_SYSTEM_LOCALIZATION_EN
   :JON_GUI_DATA_SYSTEM_LOCALIZATION_UA
   :JON_GUI_DATA_SYSTEM_LOCALIZATION_AR
   :JON_GUI_DATA_SYSTEM_LOCALIZATION_CS])

;; GPS Units enum
(def gps-units-enum-spec
  [:enum
   :JON_GUI_DATA_GPS_UNITS_UNSPECIFIED
   :JON_GUI_DATA_GPS_UNITS_DECIMAL_DEGREES
   :JON_GUI_DATA_GPS_UNITS_DEGREES_MINUTES_SECONDS
   :JON_GUI_DATA_GPS_UNITS_DEGREES_DECIMAL_MINUTES])

;; Compass Units enum
(def compass-units-enum-spec
  [:enum
   :JON_GUI_DATA_COMPASS_UNITS_UNSPECIFIED
   :JON_GUI_DATA_COMPASS_UNITS_DEGREES
   :JON_GUI_DATA_COMPASS_UNITS_MILS
   :JON_GUI_DATA_COMPASS_UNITS_GRAD
   :JON_GUI_DATA_COMPASS_UNITS_MRAD])

;; Accumulator State enum
(def accumulator-state-enum-spec
  [:enum
   :JON_GUI_DATA_ACCUMULATOR_STATE_UNSPECIFIED
   :JON_GUI_DATA_ACCUMULATOR_STATE_UNKNOWN
   :JON_GUI_DATA_ACCUMULATOR_STATE_EMPTY
   :JON_GUI_DATA_ACCUMULATOR_STATE_1
   :JON_GUI_DATA_ACCUMULATOR_STATE_2
   :JON_GUI_DATA_ACCUMULATOR_STATE_3
   :JON_GUI_DATA_ACCUMULATOR_STATE_4
   :JON_GUI_DATA_ACCUMULATOR_STATE_5
   :JON_GUI_DATA_ACCUMULATOR_STATE_6
   :JON_GUI_DATA_ACCUMULATOR_STATE_FULL
   :JON_GUI_DATA_ACCUMULATOR_STATE_CHARGING])

;; Time Formats enum
(def time-formats-enum-spec
  [:enum
   :JON_GUI_DATA_TIME_FORMAT_UNSPECIFIED
   :JON_GUI_DATA_TIME_FORMAT_H_M_S
   :JON_GUI_DATA_TIME_FORMAT_Y_m_D_H_M_S])

;; LRF Laser Pointer Modes enum
(def lrf-laser-pointer-modes-enum-spec
  [:enum
   :JON_GUI_DATA_LRF_LASER_POINTER_MODE_UNSPECIFIED
   :JON_GUI_DATA_LRF_LASER_POINTER_MODE_OFF
   :JON_GUI_DATA_LRF_LASER_POINTER_MODE_ON_1
   :JON_GUI_DATA_LRF_LASER_POINTER_MODE_ON_2])

;; REC OSD Screen enum (Cannot be UNSPECIFIED per buf.validate)
(def rec-osd-screen-enum-spec
  [:enum
   :JON_GUI_DATA_REC_OSD_SCREEN_MAIN
   :JON_GUI_DATA_REC_OSD_SCREEN_LRF_MEASURE
   :JON_GUI_DATA_REC_OSD_SCREEN_LRF_RESULT
   :JON_GUI_DATA_REC_OSD_SCREEN_LRF_RESULT_SIMPLIFIED])

;; Register enum specs
(registry/register-spec! :enum/client-type client-type-enum-spec)
(registry/register-spec! :proto/client-type client-type-enum-spec) ; Also register as proto/client-type
(registry/register-spec! :enum/gps-fix-type gps-fix-type-enum-spec)
(registry/register-spec! :enum/rotary-direction rotary-direction-enum-spec)
(registry/register-spec! :enum/rotary-mode rotary-mode-enum-spec)
(registry/register-spec! :enum/video-channel video-channel-enum-spec)
(registry/register-spec! :enum/fx-mode-day fx-mode-day-enum-spec)
(registry/register-spec! :enum/fx-mode-heat fx-mode-heat-enum-spec)
(registry/register-spec! :enum/heat-filter heat-filter-enum-spec)
(registry/register-spec! :enum/heat-agc-mode heat-agc-mode-enum-spec)
(registry/register-spec! :enum/lrf-scan-modes lrf-scan-modes-enum-spec)
(registry/register-spec! :enum/compass-calibrate-status compass-calibrate-status-enum-spec)
(registry/register-spec! :enum/system-localizations system-localizations-enum-spec)
(registry/register-spec! :enum/gps-units gps-units-enum-spec)
(registry/register-spec! :enum/compass-units compass-units-enum-spec)
(registry/register-spec! :enum/accumulator-state accumulator-state-enum-spec)
(registry/register-spec! :enum/time-formats time-formats-enum-spec)
(registry/register-spec! :enum/lrf-laser-pointer-modes lrf-laser-pointer-modes-enum-spec)
(registry/register-spec! :enum/rec-osd-screen rec-osd-screen-enum-spec)

;; ====================================================================
;; Composite specs (CLOSED MAPS - catch typos and invalid keys)
;; ====================================================================
(def gps-position-spec
  [:map {:closed true}
   [:latitude :position/latitude]
   [:longitude :position/longitude]
   [:altitude :position/altitude]])

(def compass-orientation-spec
  [:map {:closed true}
   [:azimuth :angle/azimuth]
   [:elevation :angle/elevation]
   [:bank :angle/bank]])

(def screen-point-ndc-spec
  [:map {:closed true}
   [:x :screen/ndc-x]
   [:y :screen/ndc-y]])

(def screen-point-pixel-spec
  [:map {:closed true}
   [:x :screen/pixel-x]
   [:y :screen/pixel-y]])

;; Register composite specs
(registry/register-spec! :composite/gps-position gps-position-spec)
(registry/register-spec! :composite/compass-orientation compass-orientation-spec)
(registry/register-spec! :composite/screen-point-ndc screen-point-ndc-spec)
(registry/register-spec! :composite/screen-point-pixel screen-point-pixel-spec)

;; ====================================================================
;; Common message specs
;; ====================================================================
(def meteo-spec
  "JonGuiDataMeteo message spec - meteorological data"
  [:map {:closed true}
   [:temperature [:double {:min -273.15 :max 150.0}]]   ; Absolute zero to max sensor reading
   [:humidity [:double {:min 0.0 :max 100.0}]]          ; Percentage
   [:pressure [:double {:min 0.0 :max 120000.0}]]])       ; Max atmospheric pressure with margin

;; ====================================================================
;; Common command specs (empty messages used across multiple commands)
;; ====================================================================
(def empty-command-spec
  "Empty command message - used for Start, Stop, GetMeteo, etc."
  [:map {:closed true}])

;; Register common message specs
(registry/register-spec! :common/meteo meteo-spec)

;; Register common command specs
(registry/register-spec! :cmd/empty empty-command-spec)
