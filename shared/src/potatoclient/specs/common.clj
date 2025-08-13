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
;; Angle specs (degrees)
;; ====================================================================
;; Azimuth: 0-360 degrees (compass heading)
(def azimuth-spec
  [:double {:min 0.0 :max 360.0}])

(def elevation-spec
  [:double {:min -90.0 :max 90.0}])

(def bank-spec
  [:double {:min -180.0 :max 180.0}])

;; Register angle specs
(registry/register! :angle/azimuth azimuth-spec)
(registry/register! :angle/elevation elevation-spec)
(registry/register! :angle/bank bank-spec)

;; Range specs (normalized 0-1)
(def normalized-range-spec
  [:double {:min 0.0 :max 1.0}])

(def zoom-level-spec normalized-range-spec)
(def focus-level-spec normalized-range-spec)

;; Register range specs
(registry/register! :range/normalized normalized-range-spec)
(registry/register! :range/zoom zoom-level-spec)
(registry/register! :range/focus focus-level-spec)

;; ====================================================================
;; GPS Position specs (EXACT buf.validate constraints)
;; ====================================================================
;; Latitude: double ∈ [-90, 90]
(def latitude-spec
  [:double {:min -90.0 :max 90.0}])

;; Longitude: double ∈ [-180, 180] (note: some proto fields use < 180)
(def longitude-spec
  [:double {:min -180.0 :max 180.0
            :gen/gen (gen/double* {:NaN? false :min -180.0 :max 179.999999})}])

;; Altitude: double ∈ [-433, 8848.86] (Dead Sea to Mt. Everest)
(def altitude-spec
  [:double {:min -433.0 :max 8848.86
            :gen/gen (gen/double* {:NaN? false :min -433.0 :max 8848.86})}])

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
(def on_OFF-spec :boolean)

;; Register boolean specs
(registry/register! :mode/enable enable-disable-spec)
(registry/register! :mode/on_OFF on_OFF-spec)

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

;; ====================================================================
;; Rotary Specs (EXACT buf.validate constraints)
;; ====================================================================

;; Rotary Speed: float/double > 0 and ≤ 1
(def rotary-speed-spec
  [:double {:min 0.001 :max 1.0  ; > 0 means we need small positive value
            :gen/gen (gen/double* {:min 0.001 :max 1.0 :NaN? false})}])

(registry/register! :rotary/speed rotary-speed-spec)

;; Rotary Azimuth: float ≥ 0 and < 360
(def rotary-azimuth-spec
  [:double {:min 0.0 :max 359.999999
            :gen/gen (gen/double* {:min 0.0 :max 359.999 :NaN? false})}])

;; Rotary Elevation: float ≥ -90 and ≤ 90
(def rotary-elevation-spec
  [:double {:min -90.0 :max 90.0
            :gen/gen (gen/double* {:min -90.0 :max 90.0 :NaN? false})}])

;; Platform azimuth: float > -360 and < 360
(def platform-azimuth-spec
  [:double {:min -359.999999 :max 359.999999
            :gen/gen (gen/double* {:min -359.999 :max 359.999 :NaN? false})}])

;; Platform bank: float ≥ -180 and < 180
(def platform-bank-spec
  [:double {:min -180.0 :max 179.999999
            :gen/gen (gen/double* {:min -180.0 :max 179.999 :NaN? false})}])

(registry/register! :rotary/azimuth rotary-azimuth-spec)
(registry/register! :rotary/elevation rotary-elevation-spec)
(registry/register! :rotary/platform-azimuth platform-azimuth-spec)
(registry/register! :rotary/platform-bank platform-bank-spec)

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
   :JON_GUI_DATA_FX_MODE_DAY_DEFAULT
   :JON_GUI_DATA_FX_MODE_DAY_A
   :JON_GUI_DATA_FX_MODE_DAY_B
   :JON_GUI_DATA_FX_MODE_DAY_C
   :JON_GUI_DATA_FX_MODE_DAY_D
   :JON_GUI_DATA_FX_MODE_DAY_E
   :JON_GUI_DATA_FX_MODE_DAY_F])

(def fx-mode-heat-enum-spec
  [:enum
   :JON_GUI_DATA_FX_MODE_HEAT_DEFAULT
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

;; Register enum specs
(registry/register! :enum/client-type client-type-enum-spec)
(registry/register! :proto/client-type client-type-enum-spec) ; Also register as proto/client-type
(registry/register! :enum/gps-fix-type gps-fix-type-enum-spec)
(registry/register! :enum/rotary-direction rotary-direction-enum-spec)
(registry/register! :enum/rotary-mode rotary-mode-enum-spec)
(registry/register! :enum/video-channel video-channel-enum-spec)
(registry/register! :enum/fx-mode-day fx-mode-day-enum-spec)
(registry/register! :enum/fx-mode-heat fx-mode-heat-enum-spec)
(registry/register! :enum/heat-filter heat-filter-enum-spec)
(registry/register! :enum/heat-agc-mode heat-agc-mode-enum-spec)
(registry/register! :enum/lrf-scan-modes lrf-scan-modes-enum-spec)
(registry/register! :enum/compass-calibrate-status compass-calibrate-status-enum-spec)

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
