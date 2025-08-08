(ns cmd-explorer.specs.shared
  "Common reusable specs for cmd-explorer"
  (:require
   [malli.core :as m]
   [malli.generator :as mg]
   [cmd-explorer.registry :as registry]
   [clojure.test.check.generators :as gen])
  (:import
   [ser JonSharedDataTypes$JonGuiDataClientType
    JonSharedDataTypes$JonGuiDataGpsFixType
    JonSharedDataTypes$JonGuiDataRotaryDirection
    JonSharedDataTypes$JonGuiDataRotaryMode
    JonSharedDataTypes$JonGuiDataVideoChannel
    JonSharedDataTypes$JonGuiDataFxModeDay
    JonSharedDataTypes$JonGuiDataFxModeHeat
    JonSharedDataTypes$JonGuiDataLrfScanModes
    JonSharedDataTypes$JonGuiDataCompassCalibrateStatus]))

;; Angle specs (degrees)
(def azimuth-spec
  [:double {:min -180.0 :max 180.0
            :gen/gen (gen/double* {:min -180.0 :max 180.0 :NaN? false})}])

(def elevation-spec
  [:double {:min -90.0 :max 90.0
            :gen/gen (gen/double* {:min -90.0 :max 90.0 :NaN? false})}])

(def bank-spec
  [:double {:min -180.0 :max 180.0
            :gen/gen (gen/double* {:min -180.0 :max 180.0 :NaN? false})}])

;; Register angle specs
(registry/register! :angle/azimuth azimuth-spec)
(registry/register! :angle/elevation elevation-spec)
(registry/register! :angle/bank bank-spec)

;; Range specs (normalized 0-1)
(def normalized-range-spec
  [:double {:min 0.0 :max 1.0
            :gen/gen (gen/double* {:NaN? false :min 0.0 :max 1.0})}])

(def zoom-level-spec normalized-range-spec)
(def focus-level-spec normalized-range-spec)

;; Register range specs
(registry/register! :range/normalized normalized-range-spec)
(registry/register! :range/zoom zoom-level-spec)
(registry/register! :range/focus focus-level-spec)

;; Position specs (GPS coordinates)
(def latitude-spec
  [:double {:min -90.0 :max 90.0
            :gen/gen (gen/double* {:NaN? false :min -90.0 :max 90.0})}])

(def longitude-spec
  [:double {:min -180.0 :max 180.0
            :gen/gen (gen/double* {:NaN? false :min -180.0 :max 180.0})}])

(def altitude-spec
  [:double {:min -1000.0 :max 10000.0
            :gen/gen (gen/double* {:NaN? false :min -1000.0 :max 10000.0})}])

;; Register position specs
(registry/register! :position/latitude latitude-spec)
(registry/register! :position/longitude longitude-spec)
(registry/register! :position/altitude altitude-spec)

;; Speed specs
(def speed-kmh-spec
  [:double {:min 0.0 :max 500.0
            :gen/gen (gen/double* {:NaN? false :min 0.0 :max 500.0})}])

(def speed-ms-spec
  [:double {:min 0.0 :max 150.0
            :gen/gen (gen/double* {:NaN? false :min 0.0 :max 150.0})}])

;; Register speed specs
(registry/register! :speed/kmh speed-kmh-spec)
(registry/register! :speed/ms speed-ms-spec)

;; Temperature specs
(def temperature-celsius-spec
  [:double {:min -50.0 :max 100.0
            :gen/gen (gen/double* {:NaN? false :min -50.0 :max 100.0})}])

;; Register temperature specs
(registry/register! :temperature/celsius temperature-celsius-spec)

;; Distance specs
(def distance-meters-spec
  [:double {:min 0.0 :max 50000.0
            :gen/gen (gen/double* {:NaN? false :min 0.0 :max 50000.0})}])

;; Register distance specs
(registry/register! :distance/meters distance-meters-spec)

;; Screen coordinate specs (NDC - Normalized Device Coordinates)
(def ndc-coord-spec
  [:double {:min -1.0 :max 1.0
            :gen/gen (gen/double* {:NaN? false :min -1.0 :max 1.0})}])

;; Register NDC specs
(registry/register! :screen/ndc-x ndc-coord-spec)
(registry/register! :screen/ndc-y ndc-coord-spec)

;; Pixel coordinate specs
(def pixel-coord-spec
  [:int {:min 0 :max 4096
         :gen/gen (gen/choose 0 4096)}])

;; Register pixel specs
(registry/register! :screen/pixel-x pixel-coord-spec)
(registry/register! :screen/pixel-y pixel-coord-spec)

;; Time specs
(def unix-timestamp-spec
  [:int {:min 0 :max 2147483647
         :gen/gen (gen/choose 1000000000 2147483647)}])

(def duration-seconds-spec
  [:int {:min 0 :max 86400
         :gen/gen (gen/choose 0 86400)}])

;; Register time specs
(registry/register! :time/unix-timestamp unix-timestamp-spec)
(registry/register! :time/duration-seconds duration-seconds-spec)

;; Boolean mode specs
(def enable-disable-spec :boolean)
(def on-off-spec :boolean)

;; Register boolean specs
(registry/register! :mode/enable enable-disable-spec)
(registry/register! :mode/on-off on-off-spec)

;; ID specs
(def session-id-spec
  [:int {:min 0 :max 2147483647
         :gen/gen (gen/choose 0 2147483647)}])

(def track-id-spec
  [:int {:min 0 :max 65535
         :gen/gen (gen/choose 0 65535)}])

;; Register ID specs
(registry/register! :id/session session-id-spec)
(registry/register! :id/track track-id-spec)

;; Percentage specs
(def percentage-spec
  [:double {:min 0.0 :max 100.0
            :gen/gen (gen/double* {:NaN? false :min 0.0 :max 100.0})}])

;; Register percentage spec
(registry/register! :percentage percentage-spec)

;; Enum specs (Proto enums)
(def client-type-enum-spec
  [:enum
   JonSharedDataTypes$JonGuiDataClientType/JON_GUI_DATA_CLIENT_TYPE_UNSPECIFIED
   JonSharedDataTypes$JonGuiDataClientType/JON_GUI_DATA_CLIENT_TYPE_INTERNAL_CV
   JonSharedDataTypes$JonGuiDataClientType/JON_GUI_DATA_CLIENT_TYPE_LOCAL_NETWORK
   JonSharedDataTypes$JonGuiDataClientType/JON_GUI_DATA_CLIENT_TYPE_CERTIFICATE_PROTECTED
   JonSharedDataTypes$JonGuiDataClientType/JON_GUI_DATA_CLIENT_TYPE_LIRA])

(def gps-fix-type-enum-spec
  [:enum
   JonSharedDataTypes$JonGuiDataGpsFixType/JON_GUI_DATA_GPS_FIX_TYPE_UNSPECIFIED
   JonSharedDataTypes$JonGuiDataGpsFixType/JON_GUI_DATA_GPS_FIX_TYPE_NONE
   JonSharedDataTypes$JonGuiDataGpsFixType/JON_GUI_DATA_GPS_FIX_TYPE_1D
   JonSharedDataTypes$JonGuiDataGpsFixType/JON_GUI_DATA_GPS_FIX_TYPE_2D
   JonSharedDataTypes$JonGuiDataGpsFixType/JON_GUI_DATA_GPS_FIX_TYPE_3D
   JonSharedDataTypes$JonGuiDataGpsFixType/JON_GUI_DATA_GPS_FIX_TYPE_MANUAL])

(def rotary-direction-enum-spec
  [:enum
   JonSharedDataTypes$JonGuiDataRotaryDirection/JON_GUI_DATA_ROTARY_DIRECTION_UNSPECIFIED
   JonSharedDataTypes$JonGuiDataRotaryDirection/JON_GUI_DATA_ROTARY_DIRECTION_CLOCKWISE
   JonSharedDataTypes$JonGuiDataRotaryDirection/JON_GUI_DATA_ROTARY_DIRECTION_COUNTER_CLOCKWISE])

(def rotary-mode-enum-spec
  [:enum
   JonSharedDataTypes$JonGuiDataRotaryMode/JON_GUI_DATA_ROTARY_MODE_UNSPECIFIED
   JonSharedDataTypes$JonGuiDataRotaryMode/JON_GUI_DATA_ROTARY_MODE_INITIALIZATION
   JonSharedDataTypes$JonGuiDataRotaryMode/JON_GUI_DATA_ROTARY_MODE_SPEED
   JonSharedDataTypes$JonGuiDataRotaryMode/JON_GUI_DATA_ROTARY_MODE_POSITION
   JonSharedDataTypes$JonGuiDataRotaryMode/JON_GUI_DATA_ROTARY_MODE_STABILIZATION
   JonSharedDataTypes$JonGuiDataRotaryMode/JON_GUI_DATA_ROTARY_MODE_TARGETING
   JonSharedDataTypes$JonGuiDataRotaryMode/JON_GUI_DATA_ROTARY_MODE_VIDEO_TRACKER])

(def video-channel-enum-spec
  [:enum
   JonSharedDataTypes$JonGuiDataVideoChannel/JON_GUI_DATA_VIDEO_CHANNEL_UNSPECIFIED
   JonSharedDataTypes$JonGuiDataVideoChannel/JON_GUI_DATA_VIDEO_CHANNEL_HEAT
   JonSharedDataTypes$JonGuiDataVideoChannel/JON_GUI_DATA_VIDEO_CHANNEL_DAY])

(def fx-mode-day-enum-spec
  [:enum
   JonSharedDataTypes$JonGuiDataFxModeDay/JON_GUI_DATA_FX_MODE_DAY_DEFAULT
   JonSharedDataTypes$JonGuiDataFxModeDay/JON_GUI_DATA_FX_MODE_DAY_A
   JonSharedDataTypes$JonGuiDataFxModeDay/JON_GUI_DATA_FX_MODE_DAY_B
   JonSharedDataTypes$JonGuiDataFxModeDay/JON_GUI_DATA_FX_MODE_DAY_C
   JonSharedDataTypes$JonGuiDataFxModeDay/JON_GUI_DATA_FX_MODE_DAY_D
   JonSharedDataTypes$JonGuiDataFxModeDay/JON_GUI_DATA_FX_MODE_DAY_E
   JonSharedDataTypes$JonGuiDataFxModeDay/JON_GUI_DATA_FX_MODE_DAY_F])

(def fx-mode-heat-enum-spec
  [:enum
   JonSharedDataTypes$JonGuiDataFxModeHeat/JON_GUI_DATA_FX_MODE_HEAT_DEFAULT
   JonSharedDataTypes$JonGuiDataFxModeHeat/JON_GUI_DATA_FX_MODE_HEAT_A
   JonSharedDataTypes$JonGuiDataFxModeHeat/JON_GUI_DATA_FX_MODE_HEAT_B
   JonSharedDataTypes$JonGuiDataFxModeHeat/JON_GUI_DATA_FX_MODE_HEAT_C
   JonSharedDataTypes$JonGuiDataFxModeHeat/JON_GUI_DATA_FX_MODE_HEAT_D
   JonSharedDataTypes$JonGuiDataFxModeHeat/JON_GUI_DATA_FX_MODE_HEAT_E
   JonSharedDataTypes$JonGuiDataFxModeHeat/JON_GUI_DATA_FX_MODE_HEAT_F])

(def lrf-scan-modes-enum-spec
  [:enum
   JonSharedDataTypes$JonGuiDataLrfScanModes/JON_GUI_DATA_LRF_SCAN_MODE_UNSPECIFIED
   JonSharedDataTypes$JonGuiDataLrfScanModes/JON_GUI_DATA_LRF_SCAN_MODE_1_HZ_CONTINUOUS
   JonSharedDataTypes$JonGuiDataLrfScanModes/JON_GUI_DATA_LRF_SCAN_MODE_4_HZ_CONTINUOUS
   JonSharedDataTypes$JonGuiDataLrfScanModes/JON_GUI_DATA_LRF_SCAN_MODE_10_HZ_CONTINUOUS
   JonSharedDataTypes$JonGuiDataLrfScanModes/JON_GUI_DATA_LRF_SCAN_MODE_20_HZ_CONTINUOUS
   JonSharedDataTypes$JonGuiDataLrfScanModes/JON_GUI_DATA_LRF_SCAN_MODE_100_HZ_CONTINUOUS
   JonSharedDataTypes$JonGuiDataLrfScanModes/JON_GUI_DATA_LRF_SCAN_MODE_200_HZ_CONTINUOUS])

(def compass-calibrate-status-enum-spec
  [:enum
   JonSharedDataTypes$JonGuiDataCompassCalibrateStatus/JON_GUI_DATA_COMPASS_CALIBRATE_STATUS_UNSPECIFIED
   JonSharedDataTypes$JonGuiDataCompassCalibrateStatus/JON_GUI_DATA_COMPASS_CALIBRATE_STATUS_NOT_CALIBRATING
   JonSharedDataTypes$JonGuiDataCompassCalibrateStatus/JON_GUI_DATA_COMPASS_CALIBRATE_STATUS_CALIBRATING_SHORT
   JonSharedDataTypes$JonGuiDataCompassCalibrateStatus/JON_GUI_DATA_COMPASS_CALIBRATE_STATUS_CALIBRATING_LONG
   JonSharedDataTypes$JonGuiDataCompassCalibrateStatus/JON_GUI_DATA_COMPASS_CALIBRATE_STATUS_FINISHED
   JonSharedDataTypes$JonGuiDataCompassCalibrateStatus/JON_GUI_DATA_COMPASS_CALIBRATE_STATUS_ERROR])

;; Register enum specs
(registry/register! :enum/client-type client-type-enum-spec)
(registry/register! :enum/gps-fix-type gps-fix-type-enum-spec)
(registry/register! :enum/rotary-direction rotary-direction-enum-spec)
(registry/register! :enum/rotary-mode rotary-mode-enum-spec)
(registry/register! :enum/video-channel video-channel-enum-spec)
(registry/register! :enum/fx-mode-day fx-mode-day-enum-spec)
(registry/register! :enum/fx-mode-heat fx-mode-heat-enum-spec)
(registry/register! :enum/lrf-scan-modes lrf-scan-modes-enum-spec)
(registry/register! :enum/compass-calibrate-status compass-calibrate-status-enum-spec)

;; Composite specs (combining basic specs)
(def gps-position-spec
  [:map
   [:latitude :position/latitude]
   [:longitude :position/longitude]
   [:altitude :position/altitude]])

(def compass-orientation-spec
  [:map
   [:azimuth :angle/azimuth]
   [:elevation :angle/elevation]
   [:bank :angle/bank]])

(def screen-point-ndc-spec
  [:map
   [:x :screen/ndc-x]
   [:y :screen/ndc-y]])

(def screen-point-pixel-spec
  [:map
   [:x :screen/pixel-x]
   [:y :screen/pixel-y]])

;; Register composite specs
(registry/register! :composite/gps-position gps-position-spec)
(registry/register! :composite/compass-orientation compass-orientation-spec)
(registry/register! :composite/screen-point-ndc screen-point-ndc-spec)
(registry/register! :composite/screen-point-pixel screen-point-pixel-spec)

;; Helper functions for validation
(defn validate-spec
  "Validate a value against a registered spec"
  [spec-key value]
  (m/validate spec-key value))

(defn explain-spec
  "Explain validation errors for a value against a spec"
  [spec-key value]
  (m/explain spec-key value))

(defn generate-spec
  "Generate a sample value for a registered spec"
  [spec-key]
  (mg/generate spec-key))