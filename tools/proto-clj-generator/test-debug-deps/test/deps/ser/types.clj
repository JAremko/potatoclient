(ns test.deps.ser.types
  "Generated protobuf functions."
  (:require [com.fulcrologic.guardrails.malli.core :refer [=> >defn >defn- ?]]
            [malli.core :as m])
  (:import ser.JonSharedDataTypes$JonGuiDataMeteo
           ser.JonSharedDataTypes$JonGuiDataVideoChannelHeatFilters
           ser.JonSharedDataTypes$JonGuiDataVideoChannelHeatAGCModes
           ser.JonSharedDataTypes$JonGuiDataGpsUnits
           ser.JonSharedDataTypes$JonGuiDataGpsFixType
           ser.JonSharedDataTypes$JonGuiDataCompassUnits
           ser.JonSharedDataTypes$JonGuiDataAccumulatorStateIdx
           ser.JonSharedDataTypes$JonGuiDataTimeFormats
           ser.JonSharedDataTypes$JonGuiDataRotaryDirection
           ser.JonSharedDataTypes$JonGuiDataLrfScanModes
           ser.JonSharedDataTypes$JonGuiDatatLrfLaserPointerModes
           ser.JonSharedDataTypes$JonGuiDataCompassCalibrateStatus
           ser.JonSharedDataTypes$JonGuiDataRotaryMode
           ser.JonSharedDataTypes$JonGuiDataVideoChannel
           ser.JonSharedDataTypes$JonGuiDataRecOsdScreen
           ser.JonSharedDataTypes$JonGuiDataFxModeDay
           ser.JonSharedDataTypes$JonGuiDataFxModeHeat
           ser.JonSharedDataTypes$JonGuiDataSystemLocalizations
           ser.JonSharedDataTypes$JonGuiDataClientType))

;; =============================================================================
;; Enums
;; =============================================================================

;; Enum: JonGuiDataVideoChannelHeatFilters
(def jon-gui-data-video-channel-heat-filters-values
  "Keyword to Java enum mapping for JonGuiDataVideoChannelHeatFilters."
  {:jon-gui-data-video-channel-heat-filter-unspecified
     ser.JonSharedDataTypes$JonGuiDataVideoChannelHeatFilters/JON_GUI_DATA_VIDEO_CHANNEL_HEAT_FILTER_UNSPECIFIED,
   :jon-gui-data-video-channel-heat-filter-hot-white
     ser.JonSharedDataTypes$JonGuiDataVideoChannelHeatFilters/JON_GUI_DATA_VIDEO_CHANNEL_HEAT_FILTER_HOT_WHITE,
   :jon-gui-data-video-channel-heat-filter-hot-black
     ser.JonSharedDataTypes$JonGuiDataVideoChannelHeatFilters/JON_GUI_DATA_VIDEO_CHANNEL_HEAT_FILTER_HOT_BLACK,
   :jon-gui-data-video-channel-heat-filter-sepia
     ser.JonSharedDataTypes$JonGuiDataVideoChannelHeatFilters/JON_GUI_DATA_VIDEO_CHANNEL_HEAT_FILTER_SEPIA,
   :jon-gui-data-video-channel-heat-filter-sepia-inverse
     ser.JonSharedDataTypes$JonGuiDataVideoChannelHeatFilters/JON_GUI_DATA_VIDEO_CHANNEL_HEAT_FILTER_SEPIA_INVERSE})

(def jon-gui-data-video-channel-heat-filters-keywords
  "Java enum to keyword mapping for JonGuiDataVideoChannelHeatFilters."
  {ser.JonSharedDataTypes$JonGuiDataVideoChannelHeatFilters/JON_GUI_DATA_VIDEO_CHANNEL_HEAT_FILTER_UNSPECIFIED
     :jon-gui-data-video-channel-heat-filter-unspecified,
   ser.JonSharedDataTypes$JonGuiDataVideoChannelHeatFilters/JON_GUI_DATA_VIDEO_CHANNEL_HEAT_FILTER_HOT_WHITE
     :jon-gui-data-video-channel-heat-filter-hot-white,
   ser.JonSharedDataTypes$JonGuiDataVideoChannelHeatFilters/JON_GUI_DATA_VIDEO_CHANNEL_HEAT_FILTER_HOT_BLACK
     :jon-gui-data-video-channel-heat-filter-hot-black,
   ser.JonSharedDataTypes$JonGuiDataVideoChannelHeatFilters/JON_GUI_DATA_VIDEO_CHANNEL_HEAT_FILTER_SEPIA
     :jon-gui-data-video-channel-heat-filter-sepia,
   ser.JonSharedDataTypes$JonGuiDataVideoChannelHeatFilters/JON_GUI_DATA_VIDEO_CHANNEL_HEAT_FILTER_SEPIA_INVERSE
     :jon-gui-data-video-channel-heat-filter-sepia-inverse})

;; Enum: JonGuiDataVideoChannelHeatAGCModes
(def jon-gui-data-video-channel-heat-agc-modes-values
  "Keyword to Java enum mapping for JonGuiDataVideoChannelHeatAGCModes."
  {:jon-gui-data-video-channel-heat-agc-mode-unspecified
     ser.JonSharedDataTypes$JonGuiDataVideoChannelHeatAGCModes/JON_GUI_DATA_VIDEO_CHANNEL_HEAT_AGC_MODE_UNSPECIFIED,
   :jon-gui-data-video-channel-heat-agc-mode-1
     ser.JonSharedDataTypes$JonGuiDataVideoChannelHeatAGCModes/JON_GUI_DATA_VIDEO_CHANNEL_HEAT_AGC_MODE_1,
   :jon-gui-data-video-channel-heat-agc-mode-2
     ser.JonSharedDataTypes$JonGuiDataVideoChannelHeatAGCModes/JON_GUI_DATA_VIDEO_CHANNEL_HEAT_AGC_MODE_2,
   :jon-gui-data-video-channel-heat-agc-mode-3
     ser.JonSharedDataTypes$JonGuiDataVideoChannelHeatAGCModes/JON_GUI_DATA_VIDEO_CHANNEL_HEAT_AGC_MODE_3})

(def jon-gui-data-video-channel-heat-agc-modes-keywords
  "Java enum to keyword mapping for JonGuiDataVideoChannelHeatAGCModes."
  {ser.JonSharedDataTypes$JonGuiDataVideoChannelHeatAGCModes/JON_GUI_DATA_VIDEO_CHANNEL_HEAT_AGC_MODE_UNSPECIFIED
     :jon-gui-data-video-channel-heat-agc-mode-unspecified,
   ser.JonSharedDataTypes$JonGuiDataVideoChannelHeatAGCModes/JON_GUI_DATA_VIDEO_CHANNEL_HEAT_AGC_MODE_1
     :jon-gui-data-video-channel-heat-agc-mode-1,
   ser.JonSharedDataTypes$JonGuiDataVideoChannelHeatAGCModes/JON_GUI_DATA_VIDEO_CHANNEL_HEAT_AGC_MODE_2
     :jon-gui-data-video-channel-heat-agc-mode-2,
   ser.JonSharedDataTypes$JonGuiDataVideoChannelHeatAGCModes/JON_GUI_DATA_VIDEO_CHANNEL_HEAT_AGC_MODE_3
     :jon-gui-data-video-channel-heat-agc-mode-3})

;; Enum: JonGuiDataGpsUnits
(def jon-gui-data-gps-units-values
  "Keyword to Java enum mapping for JonGuiDataGpsUnits."
  {:jon-gui-data-gps-units-unspecified
     ser.JonSharedDataTypes$JonGuiDataGpsUnits/JON_GUI_DATA_GPS_UNITS_UNSPECIFIED,
   :jon-gui-data-gps-units-decimal-degrees
     ser.JonSharedDataTypes$JonGuiDataGpsUnits/JON_GUI_DATA_GPS_UNITS_DECIMAL_DEGREES,
   :jon-gui-data-gps-units-degrees-minutes-seconds
     ser.JonSharedDataTypes$JonGuiDataGpsUnits/JON_GUI_DATA_GPS_UNITS_DEGREES_MINUTES_SECONDS,
   :jon-gui-data-gps-units-degrees-decimal-minutes
     ser.JonSharedDataTypes$JonGuiDataGpsUnits/JON_GUI_DATA_GPS_UNITS_DEGREES_DECIMAL_MINUTES})

(def jon-gui-data-gps-units-keywords
  "Java enum to keyword mapping for JonGuiDataGpsUnits."
  {ser.JonSharedDataTypes$JonGuiDataGpsUnits/JON_GUI_DATA_GPS_UNITS_UNSPECIFIED
     :jon-gui-data-gps-units-unspecified,
   ser.JonSharedDataTypes$JonGuiDataGpsUnits/JON_GUI_DATA_GPS_UNITS_DECIMAL_DEGREES
     :jon-gui-data-gps-units-decimal-degrees,
   ser.JonSharedDataTypes$JonGuiDataGpsUnits/JON_GUI_DATA_GPS_UNITS_DEGREES_MINUTES_SECONDS
     :jon-gui-data-gps-units-degrees-minutes-seconds,
   ser.JonSharedDataTypes$JonGuiDataGpsUnits/JON_GUI_DATA_GPS_UNITS_DEGREES_DECIMAL_MINUTES
     :jon-gui-data-gps-units-degrees-decimal-minutes})

;; Enum: JonGuiDataGpsFixType
(def jon-gui-data-gps-fix-type-values
  "Keyword to Java enum mapping for JonGuiDataGpsFixType."
  {:jon-gui-data-gps-fix-type-unspecified
     ser.JonSharedDataTypes$JonGuiDataGpsFixType/JON_GUI_DATA_GPS_FIX_TYPE_UNSPECIFIED,
   :jon-gui-data-gps-fix-type-none
     ser.JonSharedDataTypes$JonGuiDataGpsFixType/JON_GUI_DATA_GPS_FIX_TYPE_NONE,
   :jon-gui-data-gps-fix-type-1d
     ser.JonSharedDataTypes$JonGuiDataGpsFixType/JON_GUI_DATA_GPS_FIX_TYPE_1D,
   :jon-gui-data-gps-fix-type-2d
     ser.JonSharedDataTypes$JonGuiDataGpsFixType/JON_GUI_DATA_GPS_FIX_TYPE_2D,
   :jon-gui-data-gps-fix-type-3d
     ser.JonSharedDataTypes$JonGuiDataGpsFixType/JON_GUI_DATA_GPS_FIX_TYPE_3D,
   :jon-gui-data-gps-fix-type-manual
     ser.JonSharedDataTypes$JonGuiDataGpsFixType/JON_GUI_DATA_GPS_FIX_TYPE_MANUAL})

(def jon-gui-data-gps-fix-type-keywords
  "Java enum to keyword mapping for JonGuiDataGpsFixType."
  {ser.JonSharedDataTypes$JonGuiDataGpsFixType/JON_GUI_DATA_GPS_FIX_TYPE_UNSPECIFIED
     :jon-gui-data-gps-fix-type-unspecified,
   ser.JonSharedDataTypes$JonGuiDataGpsFixType/JON_GUI_DATA_GPS_FIX_TYPE_NONE
     :jon-gui-data-gps-fix-type-none,
   ser.JonSharedDataTypes$JonGuiDataGpsFixType/JON_GUI_DATA_GPS_FIX_TYPE_1D
     :jon-gui-data-gps-fix-type-1d,
   ser.JonSharedDataTypes$JonGuiDataGpsFixType/JON_GUI_DATA_GPS_FIX_TYPE_2D
     :jon-gui-data-gps-fix-type-2d,
   ser.JonSharedDataTypes$JonGuiDataGpsFixType/JON_GUI_DATA_GPS_FIX_TYPE_3D
     :jon-gui-data-gps-fix-type-3d,
   ser.JonSharedDataTypes$JonGuiDataGpsFixType/JON_GUI_DATA_GPS_FIX_TYPE_MANUAL
     :jon-gui-data-gps-fix-type-manual})

;; Enum: JonGuiDataCompassUnits
(def jon-gui-data-compass-units-values
  "Keyword to Java enum mapping for JonGuiDataCompassUnits."
  {:jon-gui-data-compass-units-unspecified
     ser.JonSharedDataTypes$JonGuiDataCompassUnits/JON_GUI_DATA_COMPASS_UNITS_UNSPECIFIED,
   :jon-gui-data-compass-units-degrees
     ser.JonSharedDataTypes$JonGuiDataCompassUnits/JON_GUI_DATA_COMPASS_UNITS_DEGREES,
   :jon-gui-data-compass-units-mils
     ser.JonSharedDataTypes$JonGuiDataCompassUnits/JON_GUI_DATA_COMPASS_UNITS_MILS,
   :jon-gui-data-compass-units-grad
     ser.JonSharedDataTypes$JonGuiDataCompassUnits/JON_GUI_DATA_COMPASS_UNITS_GRAD,
   :jon-gui-data-compass-units-mrad
     ser.JonSharedDataTypes$JonGuiDataCompassUnits/JON_GUI_DATA_COMPASS_UNITS_MRAD})

(def jon-gui-data-compass-units-keywords
  "Java enum to keyword mapping for JonGuiDataCompassUnits."
  {ser.JonSharedDataTypes$JonGuiDataCompassUnits/JON_GUI_DATA_COMPASS_UNITS_UNSPECIFIED
     :jon-gui-data-compass-units-unspecified,
   ser.JonSharedDataTypes$JonGuiDataCompassUnits/JON_GUI_DATA_COMPASS_UNITS_DEGREES
     :jon-gui-data-compass-units-degrees,
   ser.JonSharedDataTypes$JonGuiDataCompassUnits/JON_GUI_DATA_COMPASS_UNITS_MILS
     :jon-gui-data-compass-units-mils,
   ser.JonSharedDataTypes$JonGuiDataCompassUnits/JON_GUI_DATA_COMPASS_UNITS_GRAD
     :jon-gui-data-compass-units-grad,
   ser.JonSharedDataTypes$JonGuiDataCompassUnits/JON_GUI_DATA_COMPASS_UNITS_MRAD
     :jon-gui-data-compass-units-mrad})

;; Enum: JonGuiDataAccumulatorStateIdx
(def jon-gui-data-accumulator-state-idx-values
  "Keyword to Java enum mapping for JonGuiDataAccumulatorStateIdx."
  {:jon-gui-data-accumulator-state-unspecified
     ser.JonSharedDataTypes$JonGuiDataAccumulatorStateIdx/JON_GUI_DATA_ACCUMULATOR_STATE_UNSPECIFIED,
   :jon-gui-data-accumulator-state-unknown
     ser.JonSharedDataTypes$JonGuiDataAccumulatorStateIdx/JON_GUI_DATA_ACCUMULATOR_STATE_UNKNOWN,
   :jon-gui-data-accumulator-state-empty
     ser.JonSharedDataTypes$JonGuiDataAccumulatorStateIdx/JON_GUI_DATA_ACCUMULATOR_STATE_EMPTY,
   :jon-gui-data-accumulator-state-1
     ser.JonSharedDataTypes$JonGuiDataAccumulatorStateIdx/JON_GUI_DATA_ACCUMULATOR_STATE_1,
   :jon-gui-data-accumulator-state-2
     ser.JonSharedDataTypes$JonGuiDataAccumulatorStateIdx/JON_GUI_DATA_ACCUMULATOR_STATE_2,
   :jon-gui-data-accumulator-state-3
     ser.JonSharedDataTypes$JonGuiDataAccumulatorStateIdx/JON_GUI_DATA_ACCUMULATOR_STATE_3,
   :jon-gui-data-accumulator-state-4
     ser.JonSharedDataTypes$JonGuiDataAccumulatorStateIdx/JON_GUI_DATA_ACCUMULATOR_STATE_4,
   :jon-gui-data-accumulator-state-5
     ser.JonSharedDataTypes$JonGuiDataAccumulatorStateIdx/JON_GUI_DATA_ACCUMULATOR_STATE_5,
   :jon-gui-data-accumulator-state-6
     ser.JonSharedDataTypes$JonGuiDataAccumulatorStateIdx/JON_GUI_DATA_ACCUMULATOR_STATE_6,
   :jon-gui-data-accumulator-state-full
     ser.JonSharedDataTypes$JonGuiDataAccumulatorStateIdx/JON_GUI_DATA_ACCUMULATOR_STATE_FULL,
   :jon-gui-data-accumulator-state-charging
     ser.JonSharedDataTypes$JonGuiDataAccumulatorStateIdx/JON_GUI_DATA_ACCUMULATOR_STATE_CHARGING})

(def jon-gui-data-accumulator-state-idx-keywords
  "Java enum to keyword mapping for JonGuiDataAccumulatorStateIdx."
  {ser.JonSharedDataTypes$JonGuiDataAccumulatorStateIdx/JON_GUI_DATA_ACCUMULATOR_STATE_UNSPECIFIED
     :jon-gui-data-accumulator-state-unspecified,
   ser.JonSharedDataTypes$JonGuiDataAccumulatorStateIdx/JON_GUI_DATA_ACCUMULATOR_STATE_UNKNOWN
     :jon-gui-data-accumulator-state-unknown,
   ser.JonSharedDataTypes$JonGuiDataAccumulatorStateIdx/JON_GUI_DATA_ACCUMULATOR_STATE_EMPTY
     :jon-gui-data-accumulator-state-empty,
   ser.JonSharedDataTypes$JonGuiDataAccumulatorStateIdx/JON_GUI_DATA_ACCUMULATOR_STATE_1
     :jon-gui-data-accumulator-state-1,
   ser.JonSharedDataTypes$JonGuiDataAccumulatorStateIdx/JON_GUI_DATA_ACCUMULATOR_STATE_2
     :jon-gui-data-accumulator-state-2,
   ser.JonSharedDataTypes$JonGuiDataAccumulatorStateIdx/JON_GUI_DATA_ACCUMULATOR_STATE_3
     :jon-gui-data-accumulator-state-3,
   ser.JonSharedDataTypes$JonGuiDataAccumulatorStateIdx/JON_GUI_DATA_ACCUMULATOR_STATE_4
     :jon-gui-data-accumulator-state-4,
   ser.JonSharedDataTypes$JonGuiDataAccumulatorStateIdx/JON_GUI_DATA_ACCUMULATOR_STATE_5
     :jon-gui-data-accumulator-state-5,
   ser.JonSharedDataTypes$JonGuiDataAccumulatorStateIdx/JON_GUI_DATA_ACCUMULATOR_STATE_6
     :jon-gui-data-accumulator-state-6,
   ser.JonSharedDataTypes$JonGuiDataAccumulatorStateIdx/JON_GUI_DATA_ACCUMULATOR_STATE_FULL
     :jon-gui-data-accumulator-state-full,
   ser.JonSharedDataTypes$JonGuiDataAccumulatorStateIdx/JON_GUI_DATA_ACCUMULATOR_STATE_CHARGING
     :jon-gui-data-accumulator-state-charging})

;; Enum: JonGuiDataTimeFormats
(def jon-gui-data-time-formats-values
  "Keyword to Java enum mapping for JonGuiDataTimeFormats."
  {:jon-gui-data-time-format-unspecified
     ser.JonSharedDataTypes$JonGuiDataTimeFormats/JON_GUI_DATA_TIME_FORMAT_UNSPECIFIED,
   :jon-gui-data-time-format-h-m-s
     ser.JonSharedDataTypes$JonGuiDataTimeFormats/JON_GUI_DATA_TIME_FORMAT_H_M_S,
   :jon-gui-data-time-format-y-m-d-h-m-s
     ser.JonSharedDataTypes$JonGuiDataTimeFormats/JON_GUI_DATA_TIME_FORMAT_Y_m_D_H_M_S})

(def jon-gui-data-time-formats-keywords
  "Java enum to keyword mapping for JonGuiDataTimeFormats."
  {ser.JonSharedDataTypes$JonGuiDataTimeFormats/JON_GUI_DATA_TIME_FORMAT_UNSPECIFIED
     :jon-gui-data-time-format-unspecified,
   ser.JonSharedDataTypes$JonGuiDataTimeFormats/JON_GUI_DATA_TIME_FORMAT_H_M_S
     :jon-gui-data-time-format-h-m-s,
   ser.JonSharedDataTypes$JonGuiDataTimeFormats/JON_GUI_DATA_TIME_FORMAT_Y_m_D_H_M_S
     :jon-gui-data-time-format-y-m-d-h-m-s})

;; Enum: JonGuiDataRotaryDirection
(def jon-gui-data-rotary-direction-values
  "Keyword to Java enum mapping for JonGuiDataRotaryDirection."
  {:jon-gui-data-rotary-direction-unspecified
     ser.JonSharedDataTypes$JonGuiDataRotaryDirection/JON_GUI_DATA_ROTARY_DIRECTION_UNSPECIFIED,
   :jon-gui-data-rotary-direction-clockwise
     ser.JonSharedDataTypes$JonGuiDataRotaryDirection/JON_GUI_DATA_ROTARY_DIRECTION_CLOCKWISE,
   :jon-gui-data-rotary-direction-counter-clockwise
     ser.JonSharedDataTypes$JonGuiDataRotaryDirection/JON_GUI_DATA_ROTARY_DIRECTION_COUNTER_CLOCKWISE})

(def jon-gui-data-rotary-direction-keywords
  "Java enum to keyword mapping for JonGuiDataRotaryDirection."
  {ser.JonSharedDataTypes$JonGuiDataRotaryDirection/JON_GUI_DATA_ROTARY_DIRECTION_UNSPECIFIED
     :jon-gui-data-rotary-direction-unspecified,
   ser.JonSharedDataTypes$JonGuiDataRotaryDirection/JON_GUI_DATA_ROTARY_DIRECTION_CLOCKWISE
     :jon-gui-data-rotary-direction-clockwise,
   ser.JonSharedDataTypes$JonGuiDataRotaryDirection/JON_GUI_DATA_ROTARY_DIRECTION_COUNTER_CLOCKWISE
     :jon-gui-data-rotary-direction-counter-clockwise})

;; Enum: JonGuiDataLrfScanModes
(def jon-gui-data-lrf-scan-modes-values
  "Keyword to Java enum mapping for JonGuiDataLrfScanModes."
  {:jon-gui-data-lrf-scan-mode-unspecified
     ser.JonSharedDataTypes$JonGuiDataLrfScanModes/JON_GUI_DATA_LRF_SCAN_MODE_UNSPECIFIED,
   :jon-gui-data-lrf-scan-mode-1-hz-continuous
     ser.JonSharedDataTypes$JonGuiDataLrfScanModes/JON_GUI_DATA_LRF_SCAN_MODE_1_HZ_CONTINUOUS,
   :jon-gui-data-lrf-scan-mode-4-hz-continuous
     ser.JonSharedDataTypes$JonGuiDataLrfScanModes/JON_GUI_DATA_LRF_SCAN_MODE_4_HZ_CONTINUOUS,
   :jon-gui-data-lrf-scan-mode-10-hz-continuous
     ser.JonSharedDataTypes$JonGuiDataLrfScanModes/JON_GUI_DATA_LRF_SCAN_MODE_10_HZ_CONTINUOUS,
   :jon-gui-data-lrf-scan-mode-20-hz-continuous
     ser.JonSharedDataTypes$JonGuiDataLrfScanModes/JON_GUI_DATA_LRF_SCAN_MODE_20_HZ_CONTINUOUS,
   :jon-gui-data-lrf-scan-mode-100-hz-continuous
     ser.JonSharedDataTypes$JonGuiDataLrfScanModes/JON_GUI_DATA_LRF_SCAN_MODE_100_HZ_CONTINUOUS,
   :jon-gui-data-lrf-scan-mode-200-hz-continuous
     ser.JonSharedDataTypes$JonGuiDataLrfScanModes/JON_GUI_DATA_LRF_SCAN_MODE_200_HZ_CONTINUOUS})

(def jon-gui-data-lrf-scan-modes-keywords
  "Java enum to keyword mapping for JonGuiDataLrfScanModes."
  {ser.JonSharedDataTypes$JonGuiDataLrfScanModes/JON_GUI_DATA_LRF_SCAN_MODE_UNSPECIFIED
     :jon-gui-data-lrf-scan-mode-unspecified,
   ser.JonSharedDataTypes$JonGuiDataLrfScanModes/JON_GUI_DATA_LRF_SCAN_MODE_1_HZ_CONTINUOUS
     :jon-gui-data-lrf-scan-mode-1-hz-continuous,
   ser.JonSharedDataTypes$JonGuiDataLrfScanModes/JON_GUI_DATA_LRF_SCAN_MODE_4_HZ_CONTINUOUS
     :jon-gui-data-lrf-scan-mode-4-hz-continuous,
   ser.JonSharedDataTypes$JonGuiDataLrfScanModes/JON_GUI_DATA_LRF_SCAN_MODE_10_HZ_CONTINUOUS
     :jon-gui-data-lrf-scan-mode-10-hz-continuous,
   ser.JonSharedDataTypes$JonGuiDataLrfScanModes/JON_GUI_DATA_LRF_SCAN_MODE_20_HZ_CONTINUOUS
     :jon-gui-data-lrf-scan-mode-20-hz-continuous,
   ser.JonSharedDataTypes$JonGuiDataLrfScanModes/JON_GUI_DATA_LRF_SCAN_MODE_100_HZ_CONTINUOUS
     :jon-gui-data-lrf-scan-mode-100-hz-continuous,
   ser.JonSharedDataTypes$JonGuiDataLrfScanModes/JON_GUI_DATA_LRF_SCAN_MODE_200_HZ_CONTINUOUS
     :jon-gui-data-lrf-scan-mode-200-hz-continuous})

;; Enum: JonGuiDatatLrfLaserPointerModes
(def jon-gui-datat-lrf-laser-pointer-modes-values
  "Keyword to Java enum mapping for JonGuiDatatLrfLaserPointerModes."
  {:jon-gui-data-lrf-laser-pointer-mode-unspecified
     ser.JonSharedDataTypes$JonGuiDatatLrfLaserPointerModes/JON_GUI_DATA_LRF_LASER_POINTER_MODE_UNSPECIFIED,
   :jon-gui-data-lrf-laser-pointer-mode-off
     ser.JonSharedDataTypes$JonGuiDatatLrfLaserPointerModes/JON_GUI_DATA_LRF_LASER_POINTER_MODE_OFF,
   :jon-gui-data-lrf-laser-pointer-mode-on-1
     ser.JonSharedDataTypes$JonGuiDatatLrfLaserPointerModes/JON_GUI_DATA_LRF_LASER_POINTER_MODE_ON_1,
   :jon-gui-data-lrf-laser-pointer-mode-on-2
     ser.JonSharedDataTypes$JonGuiDatatLrfLaserPointerModes/JON_GUI_DATA_LRF_LASER_POINTER_MODE_ON_2})

(def jon-gui-datat-lrf-laser-pointer-modes-keywords
  "Java enum to keyword mapping for JonGuiDatatLrfLaserPointerModes."
  {ser.JonSharedDataTypes$JonGuiDatatLrfLaserPointerModes/JON_GUI_DATA_LRF_LASER_POINTER_MODE_UNSPECIFIED
     :jon-gui-data-lrf-laser-pointer-mode-unspecified,
   ser.JonSharedDataTypes$JonGuiDatatLrfLaserPointerModes/JON_GUI_DATA_LRF_LASER_POINTER_MODE_OFF
     :jon-gui-data-lrf-laser-pointer-mode-off,
   ser.JonSharedDataTypes$JonGuiDatatLrfLaserPointerModes/JON_GUI_DATA_LRF_LASER_POINTER_MODE_ON_1
     :jon-gui-data-lrf-laser-pointer-mode-on-1,
   ser.JonSharedDataTypes$JonGuiDatatLrfLaserPointerModes/JON_GUI_DATA_LRF_LASER_POINTER_MODE_ON_2
     :jon-gui-data-lrf-laser-pointer-mode-on-2})

;; Enum: JonGuiDataCompassCalibrateStatus
(def jon-gui-data-compass-calibrate-status-values
  "Keyword to Java enum mapping for JonGuiDataCompassCalibrateStatus."
  {:jon-gui-data-compass-calibrate-status-unspecified
     ser.JonSharedDataTypes$JonGuiDataCompassCalibrateStatus/JON_GUI_DATA_COMPASS_CALIBRATE_STATUS_UNSPECIFIED,
   :jon-gui-data-compass-calibrate-status-not-calibrating
     ser.JonSharedDataTypes$JonGuiDataCompassCalibrateStatus/JON_GUI_DATA_COMPASS_CALIBRATE_STATUS_NOT_CALIBRATING,
   :jon-gui-data-compass-calibrate-status-calibrating-short
     ser.JonSharedDataTypes$JonGuiDataCompassCalibrateStatus/JON_GUI_DATA_COMPASS_CALIBRATE_STATUS_CALIBRATING_SHORT,
   :jon-gui-data-compass-calibrate-status-calibrating-long
     ser.JonSharedDataTypes$JonGuiDataCompassCalibrateStatus/JON_GUI_DATA_COMPASS_CALIBRATE_STATUS_CALIBRATING_LONG,
   :jon-gui-data-compass-calibrate-status-finished
     ser.JonSharedDataTypes$JonGuiDataCompassCalibrateStatus/JON_GUI_DATA_COMPASS_CALIBRATE_STATUS_FINISHED,
   :jon-gui-data-compass-calibrate-status-error
     ser.JonSharedDataTypes$JonGuiDataCompassCalibrateStatus/JON_GUI_DATA_COMPASS_CALIBRATE_STATUS_ERROR})

(def jon-gui-data-compass-calibrate-status-keywords
  "Java enum to keyword mapping for JonGuiDataCompassCalibrateStatus."
  {ser.JonSharedDataTypes$JonGuiDataCompassCalibrateStatus/JON_GUI_DATA_COMPASS_CALIBRATE_STATUS_UNSPECIFIED
     :jon-gui-data-compass-calibrate-status-unspecified,
   ser.JonSharedDataTypes$JonGuiDataCompassCalibrateStatus/JON_GUI_DATA_COMPASS_CALIBRATE_STATUS_NOT_CALIBRATING
     :jon-gui-data-compass-calibrate-status-not-calibrating,
   ser.JonSharedDataTypes$JonGuiDataCompassCalibrateStatus/JON_GUI_DATA_COMPASS_CALIBRATE_STATUS_CALIBRATING_SHORT
     :jon-gui-data-compass-calibrate-status-calibrating-short,
   ser.JonSharedDataTypes$JonGuiDataCompassCalibrateStatus/JON_GUI_DATA_COMPASS_CALIBRATE_STATUS_CALIBRATING_LONG
     :jon-gui-data-compass-calibrate-status-calibrating-long,
   ser.JonSharedDataTypes$JonGuiDataCompassCalibrateStatus/JON_GUI_DATA_COMPASS_CALIBRATE_STATUS_FINISHED
     :jon-gui-data-compass-calibrate-status-finished,
   ser.JonSharedDataTypes$JonGuiDataCompassCalibrateStatus/JON_GUI_DATA_COMPASS_CALIBRATE_STATUS_ERROR
     :jon-gui-data-compass-calibrate-status-error})

;; Enum: JonGuiDataRotaryMode
(def jon-gui-data-rotary-mode-values
  "Keyword to Java enum mapping for JonGuiDataRotaryMode."
  {:jon-gui-data-rotary-mode-unspecified
     ser.JonSharedDataTypes$JonGuiDataRotaryMode/JON_GUI_DATA_ROTARY_MODE_UNSPECIFIED,
   :jon-gui-data-rotary-mode-initialization
     ser.JonSharedDataTypes$JonGuiDataRotaryMode/JON_GUI_DATA_ROTARY_MODE_INITIALIZATION,
   :jon-gui-data-rotary-mode-speed
     ser.JonSharedDataTypes$JonGuiDataRotaryMode/JON_GUI_DATA_ROTARY_MODE_SPEED,
   :jon-gui-data-rotary-mode-position
     ser.JonSharedDataTypes$JonGuiDataRotaryMode/JON_GUI_DATA_ROTARY_MODE_POSITION,
   :jon-gui-data-rotary-mode-stabilization
     ser.JonSharedDataTypes$JonGuiDataRotaryMode/JON_GUI_DATA_ROTARY_MODE_STABILIZATION,
   :jon-gui-data-rotary-mode-targeting
     ser.JonSharedDataTypes$JonGuiDataRotaryMode/JON_GUI_DATA_ROTARY_MODE_TARGETING,
   :jon-gui-data-rotary-mode-video-tracker
     ser.JonSharedDataTypes$JonGuiDataRotaryMode/JON_GUI_DATA_ROTARY_MODE_VIDEO_TRACKER})

(def jon-gui-data-rotary-mode-keywords
  "Java enum to keyword mapping for JonGuiDataRotaryMode."
  {ser.JonSharedDataTypes$JonGuiDataRotaryMode/JON_GUI_DATA_ROTARY_MODE_UNSPECIFIED
     :jon-gui-data-rotary-mode-unspecified,
   ser.JonSharedDataTypes$JonGuiDataRotaryMode/JON_GUI_DATA_ROTARY_MODE_INITIALIZATION
     :jon-gui-data-rotary-mode-initialization,
   ser.JonSharedDataTypes$JonGuiDataRotaryMode/JON_GUI_DATA_ROTARY_MODE_SPEED
     :jon-gui-data-rotary-mode-speed,
   ser.JonSharedDataTypes$JonGuiDataRotaryMode/JON_GUI_DATA_ROTARY_MODE_POSITION
     :jon-gui-data-rotary-mode-position,
   ser.JonSharedDataTypes$JonGuiDataRotaryMode/JON_GUI_DATA_ROTARY_MODE_STABILIZATION
     :jon-gui-data-rotary-mode-stabilization,
   ser.JonSharedDataTypes$JonGuiDataRotaryMode/JON_GUI_DATA_ROTARY_MODE_TARGETING
     :jon-gui-data-rotary-mode-targeting,
   ser.JonSharedDataTypes$JonGuiDataRotaryMode/JON_GUI_DATA_ROTARY_MODE_VIDEO_TRACKER
     :jon-gui-data-rotary-mode-video-tracker})

;; Enum: JonGuiDataVideoChannel
(def jon-gui-data-video-channel-values
  "Keyword to Java enum mapping for JonGuiDataVideoChannel."
  {:jon-gui-data-video-channel-unspecified
     ser.JonSharedDataTypes$JonGuiDataVideoChannel/JON_GUI_DATA_VIDEO_CHANNEL_UNSPECIFIED,
   :jon-gui-data-video-channel-heat
     ser.JonSharedDataTypes$JonGuiDataVideoChannel/JON_GUI_DATA_VIDEO_CHANNEL_HEAT,
   :jon-gui-data-video-channel-day
     ser.JonSharedDataTypes$JonGuiDataVideoChannel/JON_GUI_DATA_VIDEO_CHANNEL_DAY})

(def jon-gui-data-video-channel-keywords
  "Java enum to keyword mapping for JonGuiDataVideoChannel."
  {ser.JonSharedDataTypes$JonGuiDataVideoChannel/JON_GUI_DATA_VIDEO_CHANNEL_UNSPECIFIED
     :jon-gui-data-video-channel-unspecified,
   ser.JonSharedDataTypes$JonGuiDataVideoChannel/JON_GUI_DATA_VIDEO_CHANNEL_HEAT
     :jon-gui-data-video-channel-heat,
   ser.JonSharedDataTypes$JonGuiDataVideoChannel/JON_GUI_DATA_VIDEO_CHANNEL_DAY
     :jon-gui-data-video-channel-day})

;; Enum: JonGuiDataRecOsdScreen
(def jon-gui-data-rec-osd-screen-values
  "Keyword to Java enum mapping for JonGuiDataRecOsdScreen."
  {:jon-gui-data-rec-osd-screen-unspecified
     ser.JonSharedDataTypes$JonGuiDataRecOsdScreen/JON_GUI_DATA_REC_OSD_SCREEN_UNSPECIFIED,
   :jon-gui-data-rec-osd-screen-main
     ser.JonSharedDataTypes$JonGuiDataRecOsdScreen/JON_GUI_DATA_REC_OSD_SCREEN_MAIN,
   :jon-gui-data-rec-osd-screen-lrf-measure
     ser.JonSharedDataTypes$JonGuiDataRecOsdScreen/JON_GUI_DATA_REC_OSD_SCREEN_LRF_MEASURE,
   :jon-gui-data-rec-osd-screen-lrf-result
     ser.JonSharedDataTypes$JonGuiDataRecOsdScreen/JON_GUI_DATA_REC_OSD_SCREEN_LRF_RESULT,
   :jon-gui-data-rec-osd-screen-lrf-result-simplified
     ser.JonSharedDataTypes$JonGuiDataRecOsdScreen/JON_GUI_DATA_REC_OSD_SCREEN_LRF_RESULT_SIMPLIFIED})

(def jon-gui-data-rec-osd-screen-keywords
  "Java enum to keyword mapping for JonGuiDataRecOsdScreen."
  {ser.JonSharedDataTypes$JonGuiDataRecOsdScreen/JON_GUI_DATA_REC_OSD_SCREEN_UNSPECIFIED
     :jon-gui-data-rec-osd-screen-unspecified,
   ser.JonSharedDataTypes$JonGuiDataRecOsdScreen/JON_GUI_DATA_REC_OSD_SCREEN_MAIN
     :jon-gui-data-rec-osd-screen-main,
   ser.JonSharedDataTypes$JonGuiDataRecOsdScreen/JON_GUI_DATA_REC_OSD_SCREEN_LRF_MEASURE
     :jon-gui-data-rec-osd-screen-lrf-measure,
   ser.JonSharedDataTypes$JonGuiDataRecOsdScreen/JON_GUI_DATA_REC_OSD_SCREEN_LRF_RESULT
     :jon-gui-data-rec-osd-screen-lrf-result,
   ser.JonSharedDataTypes$JonGuiDataRecOsdScreen/JON_GUI_DATA_REC_OSD_SCREEN_LRF_RESULT_SIMPLIFIED
     :jon-gui-data-rec-osd-screen-lrf-result-simplified})

;; Enum: JonGuiDataFxModeDay
(def jon-gui-data-fx-mode-day-values
  "Keyword to Java enum mapping for JonGuiDataFxModeDay."
  {:jon-gui-data-fx-mode-day-default
     ser.JonSharedDataTypes$JonGuiDataFxModeDay/JON_GUI_DATA_FX_MODE_DAY_DEFAULT,
   :jon-gui-data-fx-mode-day-a
     ser.JonSharedDataTypes$JonGuiDataFxModeDay/JON_GUI_DATA_FX_MODE_DAY_A,
   :jon-gui-data-fx-mode-day-b
     ser.JonSharedDataTypes$JonGuiDataFxModeDay/JON_GUI_DATA_FX_MODE_DAY_B,
   :jon-gui-data-fx-mode-day-c
     ser.JonSharedDataTypes$JonGuiDataFxModeDay/JON_GUI_DATA_FX_MODE_DAY_C,
   :jon-gui-data-fx-mode-day-d
     ser.JonSharedDataTypes$JonGuiDataFxModeDay/JON_GUI_DATA_FX_MODE_DAY_D,
   :jon-gui-data-fx-mode-day-e
     ser.JonSharedDataTypes$JonGuiDataFxModeDay/JON_GUI_DATA_FX_MODE_DAY_E,
   :jon-gui-data-fx-mode-day-f
     ser.JonSharedDataTypes$JonGuiDataFxModeDay/JON_GUI_DATA_FX_MODE_DAY_F})

(def jon-gui-data-fx-mode-day-keywords
  "Java enum to keyword mapping for JonGuiDataFxModeDay."
  {ser.JonSharedDataTypes$JonGuiDataFxModeDay/JON_GUI_DATA_FX_MODE_DAY_DEFAULT
     :jon-gui-data-fx-mode-day-default,
   ser.JonSharedDataTypes$JonGuiDataFxModeDay/JON_GUI_DATA_FX_MODE_DAY_A
     :jon-gui-data-fx-mode-day-a,
   ser.JonSharedDataTypes$JonGuiDataFxModeDay/JON_GUI_DATA_FX_MODE_DAY_B
     :jon-gui-data-fx-mode-day-b,
   ser.JonSharedDataTypes$JonGuiDataFxModeDay/JON_GUI_DATA_FX_MODE_DAY_C
     :jon-gui-data-fx-mode-day-c,
   ser.JonSharedDataTypes$JonGuiDataFxModeDay/JON_GUI_DATA_FX_MODE_DAY_D
     :jon-gui-data-fx-mode-day-d,
   ser.JonSharedDataTypes$JonGuiDataFxModeDay/JON_GUI_DATA_FX_MODE_DAY_E
     :jon-gui-data-fx-mode-day-e,
   ser.JonSharedDataTypes$JonGuiDataFxModeDay/JON_GUI_DATA_FX_MODE_DAY_F
     :jon-gui-data-fx-mode-day-f})

;; Enum: JonGuiDataFxModeHeat
(def jon-gui-data-fx-mode-heat-values
  "Keyword to Java enum mapping for JonGuiDataFxModeHeat."
  {:jon-gui-data-fx-mode-heat-default
     ser.JonSharedDataTypes$JonGuiDataFxModeHeat/JON_GUI_DATA_FX_MODE_HEAT_DEFAULT,
   :jon-gui-data-fx-mode-heat-a
     ser.JonSharedDataTypes$JonGuiDataFxModeHeat/JON_GUI_DATA_FX_MODE_HEAT_A,
   :jon-gui-data-fx-mode-heat-b
     ser.JonSharedDataTypes$JonGuiDataFxModeHeat/JON_GUI_DATA_FX_MODE_HEAT_B,
   :jon-gui-data-fx-mode-heat-c
     ser.JonSharedDataTypes$JonGuiDataFxModeHeat/JON_GUI_DATA_FX_MODE_HEAT_C,
   :jon-gui-data-fx-mode-heat-d
     ser.JonSharedDataTypes$JonGuiDataFxModeHeat/JON_GUI_DATA_FX_MODE_HEAT_D,
   :jon-gui-data-fx-mode-heat-e
     ser.JonSharedDataTypes$JonGuiDataFxModeHeat/JON_GUI_DATA_FX_MODE_HEAT_E,
   :jon-gui-data-fx-mode-heat-f
     ser.JonSharedDataTypes$JonGuiDataFxModeHeat/JON_GUI_DATA_FX_MODE_HEAT_F})

(def jon-gui-data-fx-mode-heat-keywords
  "Java enum to keyword mapping for JonGuiDataFxModeHeat."
  {ser.JonSharedDataTypes$JonGuiDataFxModeHeat/JON_GUI_DATA_FX_MODE_HEAT_DEFAULT
     :jon-gui-data-fx-mode-heat-default,
   ser.JonSharedDataTypes$JonGuiDataFxModeHeat/JON_GUI_DATA_FX_MODE_HEAT_A
     :jon-gui-data-fx-mode-heat-a,
   ser.JonSharedDataTypes$JonGuiDataFxModeHeat/JON_GUI_DATA_FX_MODE_HEAT_B
     :jon-gui-data-fx-mode-heat-b,
   ser.JonSharedDataTypes$JonGuiDataFxModeHeat/JON_GUI_DATA_FX_MODE_HEAT_C
     :jon-gui-data-fx-mode-heat-c,
   ser.JonSharedDataTypes$JonGuiDataFxModeHeat/JON_GUI_DATA_FX_MODE_HEAT_D
     :jon-gui-data-fx-mode-heat-d,
   ser.JonSharedDataTypes$JonGuiDataFxModeHeat/JON_GUI_DATA_FX_MODE_HEAT_E
     :jon-gui-data-fx-mode-heat-e,
   ser.JonSharedDataTypes$JonGuiDataFxModeHeat/JON_GUI_DATA_FX_MODE_HEAT_F
     :jon-gui-data-fx-mode-heat-f})

;; Enum: JonGuiDataSystemLocalizations
(def jon-gui-data-system-localizations-values
  "Keyword to Java enum mapping for JonGuiDataSystemLocalizations."
  {:jon-gui-data-system-localization-unspecified
     ser.JonSharedDataTypes$JonGuiDataSystemLocalizations/JON_GUI_DATA_SYSTEM_LOCALIZATION_UNSPECIFIED,
   :jon-gui-data-system-localization-en
     ser.JonSharedDataTypes$JonGuiDataSystemLocalizations/JON_GUI_DATA_SYSTEM_LOCALIZATION_EN,
   :jon-gui-data-system-localization-ua
     ser.JonSharedDataTypes$JonGuiDataSystemLocalizations/JON_GUI_DATA_SYSTEM_LOCALIZATION_UA,
   :jon-gui-data-system-localization-ar
     ser.JonSharedDataTypes$JonGuiDataSystemLocalizations/JON_GUI_DATA_SYSTEM_LOCALIZATION_AR,
   :jon-gui-data-system-localization-cs
     ser.JonSharedDataTypes$JonGuiDataSystemLocalizations/JON_GUI_DATA_SYSTEM_LOCALIZATION_CS})

(def jon-gui-data-system-localizations-keywords
  "Java enum to keyword mapping for JonGuiDataSystemLocalizations."
  {ser.JonSharedDataTypes$JonGuiDataSystemLocalizations/JON_GUI_DATA_SYSTEM_LOCALIZATION_UNSPECIFIED
     :jon-gui-data-system-localization-unspecified,
   ser.JonSharedDataTypes$JonGuiDataSystemLocalizations/JON_GUI_DATA_SYSTEM_LOCALIZATION_EN
     :jon-gui-data-system-localization-en,
   ser.JonSharedDataTypes$JonGuiDataSystemLocalizations/JON_GUI_DATA_SYSTEM_LOCALIZATION_UA
     :jon-gui-data-system-localization-ua,
   ser.JonSharedDataTypes$JonGuiDataSystemLocalizations/JON_GUI_DATA_SYSTEM_LOCALIZATION_AR
     :jon-gui-data-system-localization-ar,
   ser.JonSharedDataTypes$JonGuiDataSystemLocalizations/JON_GUI_DATA_SYSTEM_LOCALIZATION_CS
     :jon-gui-data-system-localization-cs})

;; Enum: JonGuiDataClientType
(def jon-gui-data-client-type-values
  "Keyword to Java enum mapping for JonGuiDataClientType."
  {:jon-gui-data-client-type-unspecified
     ser.JonSharedDataTypes$JonGuiDataClientType/JON_GUI_DATA_CLIENT_TYPE_UNSPECIFIED,
   :jon-gui-data-client-type-internal-cv
     ser.JonSharedDataTypes$JonGuiDataClientType/JON_GUI_DATA_CLIENT_TYPE_INTERNAL_CV,
   :jon-gui-data-client-type-local-network
     ser.JonSharedDataTypes$JonGuiDataClientType/JON_GUI_DATA_CLIENT_TYPE_LOCAL_NETWORK,
   :jon-gui-data-client-type-certificate-protected
     ser.JonSharedDataTypes$JonGuiDataClientType/JON_GUI_DATA_CLIENT_TYPE_CERTIFICATE_PROTECTED,
   :jon-gui-data-client-type-lira
     ser.JonSharedDataTypes$JonGuiDataClientType/JON_GUI_DATA_CLIENT_TYPE_LIRA})

(def jon-gui-data-client-type-keywords
  "Java enum to keyword mapping for JonGuiDataClientType."
  {ser.JonSharedDataTypes$JonGuiDataClientType/JON_GUI_DATA_CLIENT_TYPE_UNSPECIFIED
     :jon-gui-data-client-type-unspecified,
   ser.JonSharedDataTypes$JonGuiDataClientType/JON_GUI_DATA_CLIENT_TYPE_INTERNAL_CV
     :jon-gui-data-client-type-internal-cv,
   ser.JonSharedDataTypes$JonGuiDataClientType/JON_GUI_DATA_CLIENT_TYPE_LOCAL_NETWORK
     :jon-gui-data-client-type-local-network,
   ser.JonSharedDataTypes$JonGuiDataClientType/JON_GUI_DATA_CLIENT_TYPE_CERTIFICATE_PROTECTED
     :jon-gui-data-client-type-certificate-protected,
   ser.JonSharedDataTypes$JonGuiDataClientType/JON_GUI_DATA_CLIENT_TYPE_LIRA
     :jon-gui-data-client-type-lira})

;; Enum: JonGuiDataVideoChannelHeatFilters
(def jon-gui-data-video-channel-heat-filters-values
  "Keyword to Java enum mapping for JonGuiDataVideoChannelHeatFilters."
  {:jon-gui-data-video-channel-heat-filter-unspecified
     ser.JonSharedDataTypes$JonGuiDataVideoChannelHeatFilters/JON_GUI_DATA_VIDEO_CHANNEL_HEAT_FILTER_UNSPECIFIED,
   :jon-gui-data-video-channel-heat-filter-hot-white
     ser.JonSharedDataTypes$JonGuiDataVideoChannelHeatFilters/JON_GUI_DATA_VIDEO_CHANNEL_HEAT_FILTER_HOT_WHITE,
   :jon-gui-data-video-channel-heat-filter-hot-black
     ser.JonSharedDataTypes$JonGuiDataVideoChannelHeatFilters/JON_GUI_DATA_VIDEO_CHANNEL_HEAT_FILTER_HOT_BLACK,
   :jon-gui-data-video-channel-heat-filter-sepia
     ser.JonSharedDataTypes$JonGuiDataVideoChannelHeatFilters/JON_GUI_DATA_VIDEO_CHANNEL_HEAT_FILTER_SEPIA,
   :jon-gui-data-video-channel-heat-filter-sepia-inverse
     ser.JonSharedDataTypes$JonGuiDataVideoChannelHeatFilters/JON_GUI_DATA_VIDEO_CHANNEL_HEAT_FILTER_SEPIA_INVERSE})

(def jon-gui-data-video-channel-heat-filters-keywords
  "Java enum to keyword mapping for JonGuiDataVideoChannelHeatFilters."
  {ser.JonSharedDataTypes$JonGuiDataVideoChannelHeatFilters/JON_GUI_DATA_VIDEO_CHANNEL_HEAT_FILTER_UNSPECIFIED
     :jon-gui-data-video-channel-heat-filter-unspecified,
   ser.JonSharedDataTypes$JonGuiDataVideoChannelHeatFilters/JON_GUI_DATA_VIDEO_CHANNEL_HEAT_FILTER_HOT_WHITE
     :jon-gui-data-video-channel-heat-filter-hot-white,
   ser.JonSharedDataTypes$JonGuiDataVideoChannelHeatFilters/JON_GUI_DATA_VIDEO_CHANNEL_HEAT_FILTER_HOT_BLACK
     :jon-gui-data-video-channel-heat-filter-hot-black,
   ser.JonSharedDataTypes$JonGuiDataVideoChannelHeatFilters/JON_GUI_DATA_VIDEO_CHANNEL_HEAT_FILTER_SEPIA
     :jon-gui-data-video-channel-heat-filter-sepia,
   ser.JonSharedDataTypes$JonGuiDataVideoChannelHeatFilters/JON_GUI_DATA_VIDEO_CHANNEL_HEAT_FILTER_SEPIA_INVERSE
     :jon-gui-data-video-channel-heat-filter-sepia-inverse})

;; Enum: JonGuiDataVideoChannelHeatAGCModes
(def jon-gui-data-video-channel-heat-agc-modes-values
  "Keyword to Java enum mapping for JonGuiDataVideoChannelHeatAGCModes."
  {:jon-gui-data-video-channel-heat-agc-mode-unspecified
     ser.JonSharedDataTypes$JonGuiDataVideoChannelHeatAGCModes/JON_GUI_DATA_VIDEO_CHANNEL_HEAT_AGC_MODE_UNSPECIFIED,
   :jon-gui-data-video-channel-heat-agc-mode-1
     ser.JonSharedDataTypes$JonGuiDataVideoChannelHeatAGCModes/JON_GUI_DATA_VIDEO_CHANNEL_HEAT_AGC_MODE_1,
   :jon-gui-data-video-channel-heat-agc-mode-2
     ser.JonSharedDataTypes$JonGuiDataVideoChannelHeatAGCModes/JON_GUI_DATA_VIDEO_CHANNEL_HEAT_AGC_MODE_2,
   :jon-gui-data-video-channel-heat-agc-mode-3
     ser.JonSharedDataTypes$JonGuiDataVideoChannelHeatAGCModes/JON_GUI_DATA_VIDEO_CHANNEL_HEAT_AGC_MODE_3})

(def jon-gui-data-video-channel-heat-agc-modes-keywords
  "Java enum to keyword mapping for JonGuiDataVideoChannelHeatAGCModes."
  {ser.JonSharedDataTypes$JonGuiDataVideoChannelHeatAGCModes/JON_GUI_DATA_VIDEO_CHANNEL_HEAT_AGC_MODE_UNSPECIFIED
     :jon-gui-data-video-channel-heat-agc-mode-unspecified,
   ser.JonSharedDataTypes$JonGuiDataVideoChannelHeatAGCModes/JON_GUI_DATA_VIDEO_CHANNEL_HEAT_AGC_MODE_1
     :jon-gui-data-video-channel-heat-agc-mode-1,
   ser.JonSharedDataTypes$JonGuiDataVideoChannelHeatAGCModes/JON_GUI_DATA_VIDEO_CHANNEL_HEAT_AGC_MODE_2
     :jon-gui-data-video-channel-heat-agc-mode-2,
   ser.JonSharedDataTypes$JonGuiDataVideoChannelHeatAGCModes/JON_GUI_DATA_VIDEO_CHANNEL_HEAT_AGC_MODE_3
     :jon-gui-data-video-channel-heat-agc-mode-3})

;; Enum: JonGuiDataGpsUnits
(def jon-gui-data-gps-units-values
  "Keyword to Java enum mapping for JonGuiDataGpsUnits."
  {:jon-gui-data-gps-units-unspecified
     ser.JonSharedDataTypes$JonGuiDataGpsUnits/JON_GUI_DATA_GPS_UNITS_UNSPECIFIED,
   :jon-gui-data-gps-units-decimal-degrees
     ser.JonSharedDataTypes$JonGuiDataGpsUnits/JON_GUI_DATA_GPS_UNITS_DECIMAL_DEGREES,
   :jon-gui-data-gps-units-degrees-minutes-seconds
     ser.JonSharedDataTypes$JonGuiDataGpsUnits/JON_GUI_DATA_GPS_UNITS_DEGREES_MINUTES_SECONDS,
   :jon-gui-data-gps-units-degrees-decimal-minutes
     ser.JonSharedDataTypes$JonGuiDataGpsUnits/JON_GUI_DATA_GPS_UNITS_DEGREES_DECIMAL_MINUTES})

(def jon-gui-data-gps-units-keywords
  "Java enum to keyword mapping for JonGuiDataGpsUnits."
  {ser.JonSharedDataTypes$JonGuiDataGpsUnits/JON_GUI_DATA_GPS_UNITS_UNSPECIFIED
     :jon-gui-data-gps-units-unspecified,
   ser.JonSharedDataTypes$JonGuiDataGpsUnits/JON_GUI_DATA_GPS_UNITS_DECIMAL_DEGREES
     :jon-gui-data-gps-units-decimal-degrees,
   ser.JonSharedDataTypes$JonGuiDataGpsUnits/JON_GUI_DATA_GPS_UNITS_DEGREES_MINUTES_SECONDS
     :jon-gui-data-gps-units-degrees-minutes-seconds,
   ser.JonSharedDataTypes$JonGuiDataGpsUnits/JON_GUI_DATA_GPS_UNITS_DEGREES_DECIMAL_MINUTES
     :jon-gui-data-gps-units-degrees-decimal-minutes})

;; Enum: JonGuiDataGpsFixType
(def jon-gui-data-gps-fix-type-values
  "Keyword to Java enum mapping for JonGuiDataGpsFixType."
  {:jon-gui-data-gps-fix-type-unspecified
     ser.JonSharedDataTypes$JonGuiDataGpsFixType/JON_GUI_DATA_GPS_FIX_TYPE_UNSPECIFIED,
   :jon-gui-data-gps-fix-type-none
     ser.JonSharedDataTypes$JonGuiDataGpsFixType/JON_GUI_DATA_GPS_FIX_TYPE_NONE,
   :jon-gui-data-gps-fix-type-1d
     ser.JonSharedDataTypes$JonGuiDataGpsFixType/JON_GUI_DATA_GPS_FIX_TYPE_1D,
   :jon-gui-data-gps-fix-type-2d
     ser.JonSharedDataTypes$JonGuiDataGpsFixType/JON_GUI_DATA_GPS_FIX_TYPE_2D,
   :jon-gui-data-gps-fix-type-3d
     ser.JonSharedDataTypes$JonGuiDataGpsFixType/JON_GUI_DATA_GPS_FIX_TYPE_3D,
   :jon-gui-data-gps-fix-type-manual
     ser.JonSharedDataTypes$JonGuiDataGpsFixType/JON_GUI_DATA_GPS_FIX_TYPE_MANUAL})

(def jon-gui-data-gps-fix-type-keywords
  "Java enum to keyword mapping for JonGuiDataGpsFixType."
  {ser.JonSharedDataTypes$JonGuiDataGpsFixType/JON_GUI_DATA_GPS_FIX_TYPE_UNSPECIFIED
     :jon-gui-data-gps-fix-type-unspecified,
   ser.JonSharedDataTypes$JonGuiDataGpsFixType/JON_GUI_DATA_GPS_FIX_TYPE_NONE
     :jon-gui-data-gps-fix-type-none,
   ser.JonSharedDataTypes$JonGuiDataGpsFixType/JON_GUI_DATA_GPS_FIX_TYPE_1D
     :jon-gui-data-gps-fix-type-1d,
   ser.JonSharedDataTypes$JonGuiDataGpsFixType/JON_GUI_DATA_GPS_FIX_TYPE_2D
     :jon-gui-data-gps-fix-type-2d,
   ser.JonSharedDataTypes$JonGuiDataGpsFixType/JON_GUI_DATA_GPS_FIX_TYPE_3D
     :jon-gui-data-gps-fix-type-3d,
   ser.JonSharedDataTypes$JonGuiDataGpsFixType/JON_GUI_DATA_GPS_FIX_TYPE_MANUAL
     :jon-gui-data-gps-fix-type-manual})

;; Enum: JonGuiDataCompassUnits
(def jon-gui-data-compass-units-values
  "Keyword to Java enum mapping for JonGuiDataCompassUnits."
  {:jon-gui-data-compass-units-unspecified
     ser.JonSharedDataTypes$JonGuiDataCompassUnits/JON_GUI_DATA_COMPASS_UNITS_UNSPECIFIED,
   :jon-gui-data-compass-units-degrees
     ser.JonSharedDataTypes$JonGuiDataCompassUnits/JON_GUI_DATA_COMPASS_UNITS_DEGREES,
   :jon-gui-data-compass-units-mils
     ser.JonSharedDataTypes$JonGuiDataCompassUnits/JON_GUI_DATA_COMPASS_UNITS_MILS,
   :jon-gui-data-compass-units-grad
     ser.JonSharedDataTypes$JonGuiDataCompassUnits/JON_GUI_DATA_COMPASS_UNITS_GRAD,
   :jon-gui-data-compass-units-mrad
     ser.JonSharedDataTypes$JonGuiDataCompassUnits/JON_GUI_DATA_COMPASS_UNITS_MRAD})

(def jon-gui-data-compass-units-keywords
  "Java enum to keyword mapping for JonGuiDataCompassUnits."
  {ser.JonSharedDataTypes$JonGuiDataCompassUnits/JON_GUI_DATA_COMPASS_UNITS_UNSPECIFIED
     :jon-gui-data-compass-units-unspecified,
   ser.JonSharedDataTypes$JonGuiDataCompassUnits/JON_GUI_DATA_COMPASS_UNITS_DEGREES
     :jon-gui-data-compass-units-degrees,
   ser.JonSharedDataTypes$JonGuiDataCompassUnits/JON_GUI_DATA_COMPASS_UNITS_MILS
     :jon-gui-data-compass-units-mils,
   ser.JonSharedDataTypes$JonGuiDataCompassUnits/JON_GUI_DATA_COMPASS_UNITS_GRAD
     :jon-gui-data-compass-units-grad,
   ser.JonSharedDataTypes$JonGuiDataCompassUnits/JON_GUI_DATA_COMPASS_UNITS_MRAD
     :jon-gui-data-compass-units-mrad})

;; Enum: JonGuiDataAccumulatorStateIdx
(def jon-gui-data-accumulator-state-idx-values
  "Keyword to Java enum mapping for JonGuiDataAccumulatorStateIdx."
  {:jon-gui-data-accumulator-state-unspecified
     ser.JonSharedDataTypes$JonGuiDataAccumulatorStateIdx/JON_GUI_DATA_ACCUMULATOR_STATE_UNSPECIFIED,
   :jon-gui-data-accumulator-state-unknown
     ser.JonSharedDataTypes$JonGuiDataAccumulatorStateIdx/JON_GUI_DATA_ACCUMULATOR_STATE_UNKNOWN,
   :jon-gui-data-accumulator-state-empty
     ser.JonSharedDataTypes$JonGuiDataAccumulatorStateIdx/JON_GUI_DATA_ACCUMULATOR_STATE_EMPTY,
   :jon-gui-data-accumulator-state-1
     ser.JonSharedDataTypes$JonGuiDataAccumulatorStateIdx/JON_GUI_DATA_ACCUMULATOR_STATE_1,
   :jon-gui-data-accumulator-state-2
     ser.JonSharedDataTypes$JonGuiDataAccumulatorStateIdx/JON_GUI_DATA_ACCUMULATOR_STATE_2,
   :jon-gui-data-accumulator-state-3
     ser.JonSharedDataTypes$JonGuiDataAccumulatorStateIdx/JON_GUI_DATA_ACCUMULATOR_STATE_3,
   :jon-gui-data-accumulator-state-4
     ser.JonSharedDataTypes$JonGuiDataAccumulatorStateIdx/JON_GUI_DATA_ACCUMULATOR_STATE_4,
   :jon-gui-data-accumulator-state-5
     ser.JonSharedDataTypes$JonGuiDataAccumulatorStateIdx/JON_GUI_DATA_ACCUMULATOR_STATE_5,
   :jon-gui-data-accumulator-state-6
     ser.JonSharedDataTypes$JonGuiDataAccumulatorStateIdx/JON_GUI_DATA_ACCUMULATOR_STATE_6,
   :jon-gui-data-accumulator-state-full
     ser.JonSharedDataTypes$JonGuiDataAccumulatorStateIdx/JON_GUI_DATA_ACCUMULATOR_STATE_FULL,
   :jon-gui-data-accumulator-state-charging
     ser.JonSharedDataTypes$JonGuiDataAccumulatorStateIdx/JON_GUI_DATA_ACCUMULATOR_STATE_CHARGING})

(def jon-gui-data-accumulator-state-idx-keywords
  "Java enum to keyword mapping for JonGuiDataAccumulatorStateIdx."
  {ser.JonSharedDataTypes$JonGuiDataAccumulatorStateIdx/JON_GUI_DATA_ACCUMULATOR_STATE_UNSPECIFIED
     :jon-gui-data-accumulator-state-unspecified,
   ser.JonSharedDataTypes$JonGuiDataAccumulatorStateIdx/JON_GUI_DATA_ACCUMULATOR_STATE_UNKNOWN
     :jon-gui-data-accumulator-state-unknown,
   ser.JonSharedDataTypes$JonGuiDataAccumulatorStateIdx/JON_GUI_DATA_ACCUMULATOR_STATE_EMPTY
     :jon-gui-data-accumulator-state-empty,
   ser.JonSharedDataTypes$JonGuiDataAccumulatorStateIdx/JON_GUI_DATA_ACCUMULATOR_STATE_1
     :jon-gui-data-accumulator-state-1,
   ser.JonSharedDataTypes$JonGuiDataAccumulatorStateIdx/JON_GUI_DATA_ACCUMULATOR_STATE_2
     :jon-gui-data-accumulator-state-2,
   ser.JonSharedDataTypes$JonGuiDataAccumulatorStateIdx/JON_GUI_DATA_ACCUMULATOR_STATE_3
     :jon-gui-data-accumulator-state-3,
   ser.JonSharedDataTypes$JonGuiDataAccumulatorStateIdx/JON_GUI_DATA_ACCUMULATOR_STATE_4
     :jon-gui-data-accumulator-state-4,
   ser.JonSharedDataTypes$JonGuiDataAccumulatorStateIdx/JON_GUI_DATA_ACCUMULATOR_STATE_5
     :jon-gui-data-accumulator-state-5,
   ser.JonSharedDataTypes$JonGuiDataAccumulatorStateIdx/JON_GUI_DATA_ACCUMULATOR_STATE_6
     :jon-gui-data-accumulator-state-6,
   ser.JonSharedDataTypes$JonGuiDataAccumulatorStateIdx/JON_GUI_DATA_ACCUMULATOR_STATE_FULL
     :jon-gui-data-accumulator-state-full,
   ser.JonSharedDataTypes$JonGuiDataAccumulatorStateIdx/JON_GUI_DATA_ACCUMULATOR_STATE_CHARGING
     :jon-gui-data-accumulator-state-charging})

;; Enum: JonGuiDataTimeFormats
(def jon-gui-data-time-formats-values
  "Keyword to Java enum mapping for JonGuiDataTimeFormats."
  {:jon-gui-data-time-format-unspecified
     ser.JonSharedDataTypes$JonGuiDataTimeFormats/JON_GUI_DATA_TIME_FORMAT_UNSPECIFIED,
   :jon-gui-data-time-format-h-m-s
     ser.JonSharedDataTypes$JonGuiDataTimeFormats/JON_GUI_DATA_TIME_FORMAT_H_M_S,
   :jon-gui-data-time-format-y-m-d-h-m-s
     ser.JonSharedDataTypes$JonGuiDataTimeFormats/JON_GUI_DATA_TIME_FORMAT_Y_m_D_H_M_S})

(def jon-gui-data-time-formats-keywords
  "Java enum to keyword mapping for JonGuiDataTimeFormats."
  {ser.JonSharedDataTypes$JonGuiDataTimeFormats/JON_GUI_DATA_TIME_FORMAT_UNSPECIFIED
     :jon-gui-data-time-format-unspecified,
   ser.JonSharedDataTypes$JonGuiDataTimeFormats/JON_GUI_DATA_TIME_FORMAT_H_M_S
     :jon-gui-data-time-format-h-m-s,
   ser.JonSharedDataTypes$JonGuiDataTimeFormats/JON_GUI_DATA_TIME_FORMAT_Y_m_D_H_M_S
     :jon-gui-data-time-format-y-m-d-h-m-s})

;; Enum: JonGuiDataRotaryDirection
(def jon-gui-data-rotary-direction-values
  "Keyword to Java enum mapping for JonGuiDataRotaryDirection."
  {:jon-gui-data-rotary-direction-unspecified
     ser.JonSharedDataTypes$JonGuiDataRotaryDirection/JON_GUI_DATA_ROTARY_DIRECTION_UNSPECIFIED,
   :jon-gui-data-rotary-direction-clockwise
     ser.JonSharedDataTypes$JonGuiDataRotaryDirection/JON_GUI_DATA_ROTARY_DIRECTION_CLOCKWISE,
   :jon-gui-data-rotary-direction-counter-clockwise
     ser.JonSharedDataTypes$JonGuiDataRotaryDirection/JON_GUI_DATA_ROTARY_DIRECTION_COUNTER_CLOCKWISE})

(def jon-gui-data-rotary-direction-keywords
  "Java enum to keyword mapping for JonGuiDataRotaryDirection."
  {ser.JonSharedDataTypes$JonGuiDataRotaryDirection/JON_GUI_DATA_ROTARY_DIRECTION_UNSPECIFIED
     :jon-gui-data-rotary-direction-unspecified,
   ser.JonSharedDataTypes$JonGuiDataRotaryDirection/JON_GUI_DATA_ROTARY_DIRECTION_CLOCKWISE
     :jon-gui-data-rotary-direction-clockwise,
   ser.JonSharedDataTypes$JonGuiDataRotaryDirection/JON_GUI_DATA_ROTARY_DIRECTION_COUNTER_CLOCKWISE
     :jon-gui-data-rotary-direction-counter-clockwise})

;; Enum: JonGuiDataLrfScanModes
(def jon-gui-data-lrf-scan-modes-values
  "Keyword to Java enum mapping for JonGuiDataLrfScanModes."
  {:jon-gui-data-lrf-scan-mode-unspecified
     ser.JonSharedDataTypes$JonGuiDataLrfScanModes/JON_GUI_DATA_LRF_SCAN_MODE_UNSPECIFIED,
   :jon-gui-data-lrf-scan-mode-1-hz-continuous
     ser.JonSharedDataTypes$JonGuiDataLrfScanModes/JON_GUI_DATA_LRF_SCAN_MODE_1_HZ_CONTINUOUS,
   :jon-gui-data-lrf-scan-mode-4-hz-continuous
     ser.JonSharedDataTypes$JonGuiDataLrfScanModes/JON_GUI_DATA_LRF_SCAN_MODE_4_HZ_CONTINUOUS,
   :jon-gui-data-lrf-scan-mode-10-hz-continuous
     ser.JonSharedDataTypes$JonGuiDataLrfScanModes/JON_GUI_DATA_LRF_SCAN_MODE_10_HZ_CONTINUOUS,
   :jon-gui-data-lrf-scan-mode-20-hz-continuous
     ser.JonSharedDataTypes$JonGuiDataLrfScanModes/JON_GUI_DATA_LRF_SCAN_MODE_20_HZ_CONTINUOUS,
   :jon-gui-data-lrf-scan-mode-100-hz-continuous
     ser.JonSharedDataTypes$JonGuiDataLrfScanModes/JON_GUI_DATA_LRF_SCAN_MODE_100_HZ_CONTINUOUS,
   :jon-gui-data-lrf-scan-mode-200-hz-continuous
     ser.JonSharedDataTypes$JonGuiDataLrfScanModes/JON_GUI_DATA_LRF_SCAN_MODE_200_HZ_CONTINUOUS})

(def jon-gui-data-lrf-scan-modes-keywords
  "Java enum to keyword mapping for JonGuiDataLrfScanModes."
  {ser.JonSharedDataTypes$JonGuiDataLrfScanModes/JON_GUI_DATA_LRF_SCAN_MODE_UNSPECIFIED
     :jon-gui-data-lrf-scan-mode-unspecified,
   ser.JonSharedDataTypes$JonGuiDataLrfScanModes/JON_GUI_DATA_LRF_SCAN_MODE_1_HZ_CONTINUOUS
     :jon-gui-data-lrf-scan-mode-1-hz-continuous,
   ser.JonSharedDataTypes$JonGuiDataLrfScanModes/JON_GUI_DATA_LRF_SCAN_MODE_4_HZ_CONTINUOUS
     :jon-gui-data-lrf-scan-mode-4-hz-continuous,
   ser.JonSharedDataTypes$JonGuiDataLrfScanModes/JON_GUI_DATA_LRF_SCAN_MODE_10_HZ_CONTINUOUS
     :jon-gui-data-lrf-scan-mode-10-hz-continuous,
   ser.JonSharedDataTypes$JonGuiDataLrfScanModes/JON_GUI_DATA_LRF_SCAN_MODE_20_HZ_CONTINUOUS
     :jon-gui-data-lrf-scan-mode-20-hz-continuous,
   ser.JonSharedDataTypes$JonGuiDataLrfScanModes/JON_GUI_DATA_LRF_SCAN_MODE_100_HZ_CONTINUOUS
     :jon-gui-data-lrf-scan-mode-100-hz-continuous,
   ser.JonSharedDataTypes$JonGuiDataLrfScanModes/JON_GUI_DATA_LRF_SCAN_MODE_200_HZ_CONTINUOUS
     :jon-gui-data-lrf-scan-mode-200-hz-continuous})

;; Enum: JonGuiDatatLrfLaserPointerModes
(def jon-gui-datat-lrf-laser-pointer-modes-values
  "Keyword to Java enum mapping for JonGuiDatatLrfLaserPointerModes."
  {:jon-gui-data-lrf-laser-pointer-mode-unspecified
     ser.JonSharedDataTypes$JonGuiDatatLrfLaserPointerModes/JON_GUI_DATA_LRF_LASER_POINTER_MODE_UNSPECIFIED,
   :jon-gui-data-lrf-laser-pointer-mode-off
     ser.JonSharedDataTypes$JonGuiDatatLrfLaserPointerModes/JON_GUI_DATA_LRF_LASER_POINTER_MODE_OFF,
   :jon-gui-data-lrf-laser-pointer-mode-on-1
     ser.JonSharedDataTypes$JonGuiDatatLrfLaserPointerModes/JON_GUI_DATA_LRF_LASER_POINTER_MODE_ON_1,
   :jon-gui-data-lrf-laser-pointer-mode-on-2
     ser.JonSharedDataTypes$JonGuiDatatLrfLaserPointerModes/JON_GUI_DATA_LRF_LASER_POINTER_MODE_ON_2})

(def jon-gui-datat-lrf-laser-pointer-modes-keywords
  "Java enum to keyword mapping for JonGuiDatatLrfLaserPointerModes."
  {ser.JonSharedDataTypes$JonGuiDatatLrfLaserPointerModes/JON_GUI_DATA_LRF_LASER_POINTER_MODE_UNSPECIFIED
     :jon-gui-data-lrf-laser-pointer-mode-unspecified,
   ser.JonSharedDataTypes$JonGuiDatatLrfLaserPointerModes/JON_GUI_DATA_LRF_LASER_POINTER_MODE_OFF
     :jon-gui-data-lrf-laser-pointer-mode-off,
   ser.JonSharedDataTypes$JonGuiDatatLrfLaserPointerModes/JON_GUI_DATA_LRF_LASER_POINTER_MODE_ON_1
     :jon-gui-data-lrf-laser-pointer-mode-on-1,
   ser.JonSharedDataTypes$JonGuiDatatLrfLaserPointerModes/JON_GUI_DATA_LRF_LASER_POINTER_MODE_ON_2
     :jon-gui-data-lrf-laser-pointer-mode-on-2})

;; Enum: JonGuiDataCompassCalibrateStatus
(def jon-gui-data-compass-calibrate-status-values
  "Keyword to Java enum mapping for JonGuiDataCompassCalibrateStatus."
  {:jon-gui-data-compass-calibrate-status-unspecified
     ser.JonSharedDataTypes$JonGuiDataCompassCalibrateStatus/JON_GUI_DATA_COMPASS_CALIBRATE_STATUS_UNSPECIFIED,
   :jon-gui-data-compass-calibrate-status-not-calibrating
     ser.JonSharedDataTypes$JonGuiDataCompassCalibrateStatus/JON_GUI_DATA_COMPASS_CALIBRATE_STATUS_NOT_CALIBRATING,
   :jon-gui-data-compass-calibrate-status-calibrating-short
     ser.JonSharedDataTypes$JonGuiDataCompassCalibrateStatus/JON_GUI_DATA_COMPASS_CALIBRATE_STATUS_CALIBRATING_SHORT,
   :jon-gui-data-compass-calibrate-status-calibrating-long
     ser.JonSharedDataTypes$JonGuiDataCompassCalibrateStatus/JON_GUI_DATA_COMPASS_CALIBRATE_STATUS_CALIBRATING_LONG,
   :jon-gui-data-compass-calibrate-status-finished
     ser.JonSharedDataTypes$JonGuiDataCompassCalibrateStatus/JON_GUI_DATA_COMPASS_CALIBRATE_STATUS_FINISHED,
   :jon-gui-data-compass-calibrate-status-error
     ser.JonSharedDataTypes$JonGuiDataCompassCalibrateStatus/JON_GUI_DATA_COMPASS_CALIBRATE_STATUS_ERROR})

(def jon-gui-data-compass-calibrate-status-keywords
  "Java enum to keyword mapping for JonGuiDataCompassCalibrateStatus."
  {ser.JonSharedDataTypes$JonGuiDataCompassCalibrateStatus/JON_GUI_DATA_COMPASS_CALIBRATE_STATUS_UNSPECIFIED
     :jon-gui-data-compass-calibrate-status-unspecified,
   ser.JonSharedDataTypes$JonGuiDataCompassCalibrateStatus/JON_GUI_DATA_COMPASS_CALIBRATE_STATUS_NOT_CALIBRATING
     :jon-gui-data-compass-calibrate-status-not-calibrating,
   ser.JonSharedDataTypes$JonGuiDataCompassCalibrateStatus/JON_GUI_DATA_COMPASS_CALIBRATE_STATUS_CALIBRATING_SHORT
     :jon-gui-data-compass-calibrate-status-calibrating-short,
   ser.JonSharedDataTypes$JonGuiDataCompassCalibrateStatus/JON_GUI_DATA_COMPASS_CALIBRATE_STATUS_CALIBRATING_LONG
     :jon-gui-data-compass-calibrate-status-calibrating-long,
   ser.JonSharedDataTypes$JonGuiDataCompassCalibrateStatus/JON_GUI_DATA_COMPASS_CALIBRATE_STATUS_FINISHED
     :jon-gui-data-compass-calibrate-status-finished,
   ser.JonSharedDataTypes$JonGuiDataCompassCalibrateStatus/JON_GUI_DATA_COMPASS_CALIBRATE_STATUS_ERROR
     :jon-gui-data-compass-calibrate-status-error})

;; Enum: JonGuiDataRotaryMode
(def jon-gui-data-rotary-mode-values
  "Keyword to Java enum mapping for JonGuiDataRotaryMode."
  {:jon-gui-data-rotary-mode-unspecified
     ser.JonSharedDataTypes$JonGuiDataRotaryMode/JON_GUI_DATA_ROTARY_MODE_UNSPECIFIED,
   :jon-gui-data-rotary-mode-initialization
     ser.JonSharedDataTypes$JonGuiDataRotaryMode/JON_GUI_DATA_ROTARY_MODE_INITIALIZATION,
   :jon-gui-data-rotary-mode-speed
     ser.JonSharedDataTypes$JonGuiDataRotaryMode/JON_GUI_DATA_ROTARY_MODE_SPEED,
   :jon-gui-data-rotary-mode-position
     ser.JonSharedDataTypes$JonGuiDataRotaryMode/JON_GUI_DATA_ROTARY_MODE_POSITION,
   :jon-gui-data-rotary-mode-stabilization
     ser.JonSharedDataTypes$JonGuiDataRotaryMode/JON_GUI_DATA_ROTARY_MODE_STABILIZATION,
   :jon-gui-data-rotary-mode-targeting
     ser.JonSharedDataTypes$JonGuiDataRotaryMode/JON_GUI_DATA_ROTARY_MODE_TARGETING,
   :jon-gui-data-rotary-mode-video-tracker
     ser.JonSharedDataTypes$JonGuiDataRotaryMode/JON_GUI_DATA_ROTARY_MODE_VIDEO_TRACKER})

(def jon-gui-data-rotary-mode-keywords
  "Java enum to keyword mapping for JonGuiDataRotaryMode."
  {ser.JonSharedDataTypes$JonGuiDataRotaryMode/JON_GUI_DATA_ROTARY_MODE_UNSPECIFIED
     :jon-gui-data-rotary-mode-unspecified,
   ser.JonSharedDataTypes$JonGuiDataRotaryMode/JON_GUI_DATA_ROTARY_MODE_INITIALIZATION
     :jon-gui-data-rotary-mode-initialization,
   ser.JonSharedDataTypes$JonGuiDataRotaryMode/JON_GUI_DATA_ROTARY_MODE_SPEED
     :jon-gui-data-rotary-mode-speed,
   ser.JonSharedDataTypes$JonGuiDataRotaryMode/JON_GUI_DATA_ROTARY_MODE_POSITION
     :jon-gui-data-rotary-mode-position,
   ser.JonSharedDataTypes$JonGuiDataRotaryMode/JON_GUI_DATA_ROTARY_MODE_STABILIZATION
     :jon-gui-data-rotary-mode-stabilization,
   ser.JonSharedDataTypes$JonGuiDataRotaryMode/JON_GUI_DATA_ROTARY_MODE_TARGETING
     :jon-gui-data-rotary-mode-targeting,
   ser.JonSharedDataTypes$JonGuiDataRotaryMode/JON_GUI_DATA_ROTARY_MODE_VIDEO_TRACKER
     :jon-gui-data-rotary-mode-video-tracker})

;; Enum: JonGuiDataVideoChannel
(def jon-gui-data-video-channel-values
  "Keyword to Java enum mapping for JonGuiDataVideoChannel."
  {:jon-gui-data-video-channel-unspecified
     ser.JonSharedDataTypes$JonGuiDataVideoChannel/JON_GUI_DATA_VIDEO_CHANNEL_UNSPECIFIED,
   :jon-gui-data-video-channel-heat
     ser.JonSharedDataTypes$JonGuiDataVideoChannel/JON_GUI_DATA_VIDEO_CHANNEL_HEAT,
   :jon-gui-data-video-channel-day
     ser.JonSharedDataTypes$JonGuiDataVideoChannel/JON_GUI_DATA_VIDEO_CHANNEL_DAY})

(def jon-gui-data-video-channel-keywords
  "Java enum to keyword mapping for JonGuiDataVideoChannel."
  {ser.JonSharedDataTypes$JonGuiDataVideoChannel/JON_GUI_DATA_VIDEO_CHANNEL_UNSPECIFIED
     :jon-gui-data-video-channel-unspecified,
   ser.JonSharedDataTypes$JonGuiDataVideoChannel/JON_GUI_DATA_VIDEO_CHANNEL_HEAT
     :jon-gui-data-video-channel-heat,
   ser.JonSharedDataTypes$JonGuiDataVideoChannel/JON_GUI_DATA_VIDEO_CHANNEL_DAY
     :jon-gui-data-video-channel-day})

;; Enum: JonGuiDataRecOsdScreen
(def jon-gui-data-rec-osd-screen-values
  "Keyword to Java enum mapping for JonGuiDataRecOsdScreen."
  {:jon-gui-data-rec-osd-screen-unspecified
     ser.JonSharedDataTypes$JonGuiDataRecOsdScreen/JON_GUI_DATA_REC_OSD_SCREEN_UNSPECIFIED,
   :jon-gui-data-rec-osd-screen-main
     ser.JonSharedDataTypes$JonGuiDataRecOsdScreen/JON_GUI_DATA_REC_OSD_SCREEN_MAIN,
   :jon-gui-data-rec-osd-screen-lrf-measure
     ser.JonSharedDataTypes$JonGuiDataRecOsdScreen/JON_GUI_DATA_REC_OSD_SCREEN_LRF_MEASURE,
   :jon-gui-data-rec-osd-screen-lrf-result
     ser.JonSharedDataTypes$JonGuiDataRecOsdScreen/JON_GUI_DATA_REC_OSD_SCREEN_LRF_RESULT,
   :jon-gui-data-rec-osd-screen-lrf-result-simplified
     ser.JonSharedDataTypes$JonGuiDataRecOsdScreen/JON_GUI_DATA_REC_OSD_SCREEN_LRF_RESULT_SIMPLIFIED})

(def jon-gui-data-rec-osd-screen-keywords
  "Java enum to keyword mapping for JonGuiDataRecOsdScreen."
  {ser.JonSharedDataTypes$JonGuiDataRecOsdScreen/JON_GUI_DATA_REC_OSD_SCREEN_UNSPECIFIED
     :jon-gui-data-rec-osd-screen-unspecified,
   ser.JonSharedDataTypes$JonGuiDataRecOsdScreen/JON_GUI_DATA_REC_OSD_SCREEN_MAIN
     :jon-gui-data-rec-osd-screen-main,
   ser.JonSharedDataTypes$JonGuiDataRecOsdScreen/JON_GUI_DATA_REC_OSD_SCREEN_LRF_MEASURE
     :jon-gui-data-rec-osd-screen-lrf-measure,
   ser.JonSharedDataTypes$JonGuiDataRecOsdScreen/JON_GUI_DATA_REC_OSD_SCREEN_LRF_RESULT
     :jon-gui-data-rec-osd-screen-lrf-result,
   ser.JonSharedDataTypes$JonGuiDataRecOsdScreen/JON_GUI_DATA_REC_OSD_SCREEN_LRF_RESULT_SIMPLIFIED
     :jon-gui-data-rec-osd-screen-lrf-result-simplified})

;; Enum: JonGuiDataFxModeDay
(def jon-gui-data-fx-mode-day-values
  "Keyword to Java enum mapping for JonGuiDataFxModeDay."
  {:jon-gui-data-fx-mode-day-default
     ser.JonSharedDataTypes$JonGuiDataFxModeDay/JON_GUI_DATA_FX_MODE_DAY_DEFAULT,
   :jon-gui-data-fx-mode-day-a
     ser.JonSharedDataTypes$JonGuiDataFxModeDay/JON_GUI_DATA_FX_MODE_DAY_A,
   :jon-gui-data-fx-mode-day-b
     ser.JonSharedDataTypes$JonGuiDataFxModeDay/JON_GUI_DATA_FX_MODE_DAY_B,
   :jon-gui-data-fx-mode-day-c
     ser.JonSharedDataTypes$JonGuiDataFxModeDay/JON_GUI_DATA_FX_MODE_DAY_C,
   :jon-gui-data-fx-mode-day-d
     ser.JonSharedDataTypes$JonGuiDataFxModeDay/JON_GUI_DATA_FX_MODE_DAY_D,
   :jon-gui-data-fx-mode-day-e
     ser.JonSharedDataTypes$JonGuiDataFxModeDay/JON_GUI_DATA_FX_MODE_DAY_E,
   :jon-gui-data-fx-mode-day-f
     ser.JonSharedDataTypes$JonGuiDataFxModeDay/JON_GUI_DATA_FX_MODE_DAY_F})

(def jon-gui-data-fx-mode-day-keywords
  "Java enum to keyword mapping for JonGuiDataFxModeDay."
  {ser.JonSharedDataTypes$JonGuiDataFxModeDay/JON_GUI_DATA_FX_MODE_DAY_DEFAULT
     :jon-gui-data-fx-mode-day-default,
   ser.JonSharedDataTypes$JonGuiDataFxModeDay/JON_GUI_DATA_FX_MODE_DAY_A
     :jon-gui-data-fx-mode-day-a,
   ser.JonSharedDataTypes$JonGuiDataFxModeDay/JON_GUI_DATA_FX_MODE_DAY_B
     :jon-gui-data-fx-mode-day-b,
   ser.JonSharedDataTypes$JonGuiDataFxModeDay/JON_GUI_DATA_FX_MODE_DAY_C
     :jon-gui-data-fx-mode-day-c,
   ser.JonSharedDataTypes$JonGuiDataFxModeDay/JON_GUI_DATA_FX_MODE_DAY_D
     :jon-gui-data-fx-mode-day-d,
   ser.JonSharedDataTypes$JonGuiDataFxModeDay/JON_GUI_DATA_FX_MODE_DAY_E
     :jon-gui-data-fx-mode-day-e,
   ser.JonSharedDataTypes$JonGuiDataFxModeDay/JON_GUI_DATA_FX_MODE_DAY_F
     :jon-gui-data-fx-mode-day-f})

;; Enum: JonGuiDataFxModeHeat
(def jon-gui-data-fx-mode-heat-values
  "Keyword to Java enum mapping for JonGuiDataFxModeHeat."
  {:jon-gui-data-fx-mode-heat-default
     ser.JonSharedDataTypes$JonGuiDataFxModeHeat/JON_GUI_DATA_FX_MODE_HEAT_DEFAULT,
   :jon-gui-data-fx-mode-heat-a
     ser.JonSharedDataTypes$JonGuiDataFxModeHeat/JON_GUI_DATA_FX_MODE_HEAT_A,
   :jon-gui-data-fx-mode-heat-b
     ser.JonSharedDataTypes$JonGuiDataFxModeHeat/JON_GUI_DATA_FX_MODE_HEAT_B,
   :jon-gui-data-fx-mode-heat-c
     ser.JonSharedDataTypes$JonGuiDataFxModeHeat/JON_GUI_DATA_FX_MODE_HEAT_C,
   :jon-gui-data-fx-mode-heat-d
     ser.JonSharedDataTypes$JonGuiDataFxModeHeat/JON_GUI_DATA_FX_MODE_HEAT_D,
   :jon-gui-data-fx-mode-heat-e
     ser.JonSharedDataTypes$JonGuiDataFxModeHeat/JON_GUI_DATA_FX_MODE_HEAT_E,
   :jon-gui-data-fx-mode-heat-f
     ser.JonSharedDataTypes$JonGuiDataFxModeHeat/JON_GUI_DATA_FX_MODE_HEAT_F})

(def jon-gui-data-fx-mode-heat-keywords
  "Java enum to keyword mapping for JonGuiDataFxModeHeat."
  {ser.JonSharedDataTypes$JonGuiDataFxModeHeat/JON_GUI_DATA_FX_MODE_HEAT_DEFAULT
     :jon-gui-data-fx-mode-heat-default,
   ser.JonSharedDataTypes$JonGuiDataFxModeHeat/JON_GUI_DATA_FX_MODE_HEAT_A
     :jon-gui-data-fx-mode-heat-a,
   ser.JonSharedDataTypes$JonGuiDataFxModeHeat/JON_GUI_DATA_FX_MODE_HEAT_B
     :jon-gui-data-fx-mode-heat-b,
   ser.JonSharedDataTypes$JonGuiDataFxModeHeat/JON_GUI_DATA_FX_MODE_HEAT_C
     :jon-gui-data-fx-mode-heat-c,
   ser.JonSharedDataTypes$JonGuiDataFxModeHeat/JON_GUI_DATA_FX_MODE_HEAT_D
     :jon-gui-data-fx-mode-heat-d,
   ser.JonSharedDataTypes$JonGuiDataFxModeHeat/JON_GUI_DATA_FX_MODE_HEAT_E
     :jon-gui-data-fx-mode-heat-e,
   ser.JonSharedDataTypes$JonGuiDataFxModeHeat/JON_GUI_DATA_FX_MODE_HEAT_F
     :jon-gui-data-fx-mode-heat-f})

;; Enum: JonGuiDataSystemLocalizations
(def jon-gui-data-system-localizations-values
  "Keyword to Java enum mapping for JonGuiDataSystemLocalizations."
  {:jon-gui-data-system-localization-unspecified
     ser.JonSharedDataTypes$JonGuiDataSystemLocalizations/JON_GUI_DATA_SYSTEM_LOCALIZATION_UNSPECIFIED,
   :jon-gui-data-system-localization-en
     ser.JonSharedDataTypes$JonGuiDataSystemLocalizations/JON_GUI_DATA_SYSTEM_LOCALIZATION_EN,
   :jon-gui-data-system-localization-ua
     ser.JonSharedDataTypes$JonGuiDataSystemLocalizations/JON_GUI_DATA_SYSTEM_LOCALIZATION_UA,
   :jon-gui-data-system-localization-ar
     ser.JonSharedDataTypes$JonGuiDataSystemLocalizations/JON_GUI_DATA_SYSTEM_LOCALIZATION_AR,
   :jon-gui-data-system-localization-cs
     ser.JonSharedDataTypes$JonGuiDataSystemLocalizations/JON_GUI_DATA_SYSTEM_LOCALIZATION_CS})

(def jon-gui-data-system-localizations-keywords
  "Java enum to keyword mapping for JonGuiDataSystemLocalizations."
  {ser.JonSharedDataTypes$JonGuiDataSystemLocalizations/JON_GUI_DATA_SYSTEM_LOCALIZATION_UNSPECIFIED
     :jon-gui-data-system-localization-unspecified,
   ser.JonSharedDataTypes$JonGuiDataSystemLocalizations/JON_GUI_DATA_SYSTEM_LOCALIZATION_EN
     :jon-gui-data-system-localization-en,
   ser.JonSharedDataTypes$JonGuiDataSystemLocalizations/JON_GUI_DATA_SYSTEM_LOCALIZATION_UA
     :jon-gui-data-system-localization-ua,
   ser.JonSharedDataTypes$JonGuiDataSystemLocalizations/JON_GUI_DATA_SYSTEM_LOCALIZATION_AR
     :jon-gui-data-system-localization-ar,
   ser.JonSharedDataTypes$JonGuiDataSystemLocalizations/JON_GUI_DATA_SYSTEM_LOCALIZATION_CS
     :jon-gui-data-system-localization-cs})

;; Enum: JonGuiDataClientType
(def jon-gui-data-client-type-values
  "Keyword to Java enum mapping for JonGuiDataClientType."
  {:jon-gui-data-client-type-unspecified
     ser.JonSharedDataTypes$JonGuiDataClientType/JON_GUI_DATA_CLIENT_TYPE_UNSPECIFIED,
   :jon-gui-data-client-type-internal-cv
     ser.JonSharedDataTypes$JonGuiDataClientType/JON_GUI_DATA_CLIENT_TYPE_INTERNAL_CV,
   :jon-gui-data-client-type-local-network
     ser.JonSharedDataTypes$JonGuiDataClientType/JON_GUI_DATA_CLIENT_TYPE_LOCAL_NETWORK,
   :jon-gui-data-client-type-certificate-protected
     ser.JonSharedDataTypes$JonGuiDataClientType/JON_GUI_DATA_CLIENT_TYPE_CERTIFICATE_PROTECTED,
   :jon-gui-data-client-type-lira
     ser.JonSharedDataTypes$JonGuiDataClientType/JON_GUI_DATA_CLIENT_TYPE_LIRA})

(def jon-gui-data-client-type-keywords
  "Java enum to keyword mapping for JonGuiDataClientType."
  {ser.JonSharedDataTypes$JonGuiDataClientType/JON_GUI_DATA_CLIENT_TYPE_UNSPECIFIED
     :jon-gui-data-client-type-unspecified,
   ser.JonSharedDataTypes$JonGuiDataClientType/JON_GUI_DATA_CLIENT_TYPE_INTERNAL_CV
     :jon-gui-data-client-type-internal-cv,
   ser.JonSharedDataTypes$JonGuiDataClientType/JON_GUI_DATA_CLIENT_TYPE_LOCAL_NETWORK
     :jon-gui-data-client-type-local-network,
   ser.JonSharedDataTypes$JonGuiDataClientType/JON_GUI_DATA_CLIENT_TYPE_CERTIFICATE_PROTECTED
     :jon-gui-data-client-type-certificate-protected,
   ser.JonSharedDataTypes$JonGuiDataClientType/JON_GUI_DATA_CLIENT_TYPE_LIRA
     :jon-gui-data-client-type-lira})

;; =============================================================================
;; Malli Specs
;; =============================================================================

(def jon-gui-data-video-channel-heat-filters-spec
  "Malli spec for jon-gui-data-video-channel-heat-filters enum"
  [:enum :jon-gui-data-video-channel-heat-filter-unspecified
   :jon-gui-data-video-channel-heat-filter-hot-white
   :jon-gui-data-video-channel-heat-filter-hot-black
   :jon-gui-data-video-channel-heat-filter-sepia
   :jon-gui-data-video-channel-heat-filter-sepia-inverse])

(def jon-gui-data-video-channel-heat-agc-modes-spec
  "Malli spec for jon-gui-data-video-channel-heat-agc-modes enum"
  [:enum :jon-gui-data-video-channel-heat-agc-mode-unspecified
   :jon-gui-data-video-channel-heat-agc-mode-1
   :jon-gui-data-video-channel-heat-agc-mode-2
   :jon-gui-data-video-channel-heat-agc-mode-3])

(def jon-gui-data-gps-units-spec
  "Malli spec for jon-gui-data-gps-units enum"
  [:enum :jon-gui-data-gps-units-unspecified
   :jon-gui-data-gps-units-decimal-degrees
   :jon-gui-data-gps-units-degrees-minutes-seconds
   :jon-gui-data-gps-units-degrees-decimal-minutes])

(def jon-gui-data-gps-fix-type-spec
  "Malli spec for jon-gui-data-gps-fix-type enum"
  [:enum :jon-gui-data-gps-fix-type-unspecified :jon-gui-data-gps-fix-type-none
   :jon-gui-data-gps-fix-type-1-d :jon-gui-data-gps-fix-type-2-d
   :jon-gui-data-gps-fix-type-3-d :jon-gui-data-gps-fix-type-manual])

(def jon-gui-data-compass-units-spec
  "Malli spec for jon-gui-data-compass-units enum"
  [:enum :jon-gui-data-compass-units-unspecified
   :jon-gui-data-compass-units-degrees :jon-gui-data-compass-units-mils
   :jon-gui-data-compass-units-grad :jon-gui-data-compass-units-mrad])

(def jon-gui-data-accumulator-state-idx-spec
  "Malli spec for jon-gui-data-accumulator-state-idx enum"
  [:enum :jon-gui-data-accumulator-state-unspecified
   :jon-gui-data-accumulator-state-unknown :jon-gui-data-accumulator-state-empty
   :jon-gui-data-accumulator-state-1 :jon-gui-data-accumulator-state-2
   :jon-gui-data-accumulator-state-3 :jon-gui-data-accumulator-state-4
   :jon-gui-data-accumulator-state-5 :jon-gui-data-accumulator-state-6
   :jon-gui-data-accumulator-state-full
   :jon-gui-data-accumulator-state-charging])

(def jon-gui-data-time-formats-spec
  "Malli spec for jon-gui-data-time-formats enum"
  [:enum :jon-gui-data-time-format-unspecified :jon-gui-data-time-format-h-m-s
   :jon-gui-data-time-format-y-m-d-h-m-s])

(def jon-gui-data-rotary-direction-spec
  "Malli spec for jon-gui-data-rotary-direction enum"
  [:enum :jon-gui-data-rotary-direction-unspecified
   :jon-gui-data-rotary-direction-clockwise
   :jon-gui-data-rotary-direction-counter-clockwise])

(def jon-gui-data-lrf-scan-modes-spec
  "Malli spec for jon-gui-data-lrf-scan-modes enum"
  [:enum :jon-gui-data-lrf-scan-mode-unspecified
   :jon-gui-data-lrf-scan-mode-1-hz-continuous
   :jon-gui-data-lrf-scan-mode-4-hz-continuous
   :jon-gui-data-lrf-scan-mode-10-hz-continuous
   :jon-gui-data-lrf-scan-mode-20-hz-continuous
   :jon-gui-data-lrf-scan-mode-100-hz-continuous
   :jon-gui-data-lrf-scan-mode-200-hz-continuous])

(def jon-gui-datat-lrf-laser-pointer-modes-spec
  "Malli spec for jon-gui-datat-lrf-laser-pointer-modes enum"
  [:enum :jon-gui-data-lrf-laser-pointer-mode-unspecified
   :jon-gui-data-lrf-laser-pointer-mode-off
   :jon-gui-data-lrf-laser-pointer-mode-on-1
   :jon-gui-data-lrf-laser-pointer-mode-on-2])

(def jon-gui-data-compass-calibrate-status-spec
  "Malli spec for jon-gui-data-compass-calibrate-status enum"
  [:enum :jon-gui-data-compass-calibrate-status-unspecified
   :jon-gui-data-compass-calibrate-status-not-calibrating
   :jon-gui-data-compass-calibrate-status-calibrating-short
   :jon-gui-data-compass-calibrate-status-calibrating-long
   :jon-gui-data-compass-calibrate-status-finished
   :jon-gui-data-compass-calibrate-status-error])

(def jon-gui-data-rotary-mode-spec
  "Malli spec for jon-gui-data-rotary-mode enum"
  [:enum :jon-gui-data-rotary-mode-unspecified
   :jon-gui-data-rotary-mode-initialization :jon-gui-data-rotary-mode-speed
   :jon-gui-data-rotary-mode-position :jon-gui-data-rotary-mode-stabilization
   :jon-gui-data-rotary-mode-targeting :jon-gui-data-rotary-mode-video-tracker])

(def jon-gui-data-video-channel-spec
  "Malli spec for jon-gui-data-video-channel enum"
  [:enum :jon-gui-data-video-channel-unspecified
   :jon-gui-data-video-channel-heat :jon-gui-data-video-channel-day])

(def jon-gui-data-rec-osd-screen-spec
  "Malli spec for jon-gui-data-rec-osd-screen enum"
  [:enum :jon-gui-data-rec-osd-screen-unspecified
   :jon-gui-data-rec-osd-screen-main :jon-gui-data-rec-osd-screen-lrf-measure
   :jon-gui-data-rec-osd-screen-lrf-result
   :jon-gui-data-rec-osd-screen-lrf-result-simplified])

(def jon-gui-data-fx-mode-day-spec
  "Malli spec for jon-gui-data-fx-mode-day enum"
  [:enum :jon-gui-data-fx-mode-day-default :jon-gui-data-fx-mode-day-a
   :jon-gui-data-fx-mode-day-b :jon-gui-data-fx-mode-day-c
   :jon-gui-data-fx-mode-day-d :jon-gui-data-fx-mode-day-e
   :jon-gui-data-fx-mode-day-f])

(def jon-gui-data-fx-mode-heat-spec
  "Malli spec for jon-gui-data-fx-mode-heat enum"
  [:enum :jon-gui-data-fx-mode-heat-default :jon-gui-data-fx-mode-heat-a
   :jon-gui-data-fx-mode-heat-b :jon-gui-data-fx-mode-heat-c
   :jon-gui-data-fx-mode-heat-d :jon-gui-data-fx-mode-heat-e
   :jon-gui-data-fx-mode-heat-f])

(def jon-gui-data-system-localizations-spec
  "Malli spec for jon-gui-data-system-localizations enum"
  [:enum :jon-gui-data-system-localization-unspecified
   :jon-gui-data-system-localization-en :jon-gui-data-system-localization-ua
   :jon-gui-data-system-localization-ar :jon-gui-data-system-localization-cs])

(def jon-gui-data-client-type-spec
  "Malli spec for jon-gui-data-client-type enum"
  [:enum :jon-gui-data-client-type-unspecified
   :jon-gui-data-client-type-internal-cv :jon-gui-data-client-type-local-network
   :jon-gui-data-client-type-certificate-protected
   :jon-gui-data-client-type-lira])

(def jon-gui-data-video-channel-heat-filters-spec
  "Malli spec for jon-gui-data-video-channel-heat-filters enum"
  [:enum :jon-gui-data-video-channel-heat-filter-unspecified
   :jon-gui-data-video-channel-heat-filter-hot-white
   :jon-gui-data-video-channel-heat-filter-hot-black
   :jon-gui-data-video-channel-heat-filter-sepia
   :jon-gui-data-video-channel-heat-filter-sepia-inverse])

(def jon-gui-data-video-channel-heat-agc-modes-spec
  "Malli spec for jon-gui-data-video-channel-heat-agc-modes enum"
  [:enum :jon-gui-data-video-channel-heat-agc-mode-unspecified
   :jon-gui-data-video-channel-heat-agc-mode-1
   :jon-gui-data-video-channel-heat-agc-mode-2
   :jon-gui-data-video-channel-heat-agc-mode-3])

(def jon-gui-data-gps-units-spec
  "Malli spec for jon-gui-data-gps-units enum"
  [:enum :jon-gui-data-gps-units-unspecified
   :jon-gui-data-gps-units-decimal-degrees
   :jon-gui-data-gps-units-degrees-minutes-seconds
   :jon-gui-data-gps-units-degrees-decimal-minutes])

(def jon-gui-data-gps-fix-type-spec
  "Malli spec for jon-gui-data-gps-fix-type enum"
  [:enum :jon-gui-data-gps-fix-type-unspecified :jon-gui-data-gps-fix-type-none
   :jon-gui-data-gps-fix-type-1-d :jon-gui-data-gps-fix-type-2-d
   :jon-gui-data-gps-fix-type-3-d :jon-gui-data-gps-fix-type-manual])

(def jon-gui-data-compass-units-spec
  "Malli spec for jon-gui-data-compass-units enum"
  [:enum :jon-gui-data-compass-units-unspecified
   :jon-gui-data-compass-units-degrees :jon-gui-data-compass-units-mils
   :jon-gui-data-compass-units-grad :jon-gui-data-compass-units-mrad])

(def jon-gui-data-accumulator-state-idx-spec
  "Malli spec for jon-gui-data-accumulator-state-idx enum"
  [:enum :jon-gui-data-accumulator-state-unspecified
   :jon-gui-data-accumulator-state-unknown :jon-gui-data-accumulator-state-empty
   :jon-gui-data-accumulator-state-1 :jon-gui-data-accumulator-state-2
   :jon-gui-data-accumulator-state-3 :jon-gui-data-accumulator-state-4
   :jon-gui-data-accumulator-state-5 :jon-gui-data-accumulator-state-6
   :jon-gui-data-accumulator-state-full
   :jon-gui-data-accumulator-state-charging])

(def jon-gui-data-time-formats-spec
  "Malli spec for jon-gui-data-time-formats enum"
  [:enum :jon-gui-data-time-format-unspecified :jon-gui-data-time-format-h-m-s
   :jon-gui-data-time-format-y-m-d-h-m-s])

(def jon-gui-data-rotary-direction-spec
  "Malli spec for jon-gui-data-rotary-direction enum"
  [:enum :jon-gui-data-rotary-direction-unspecified
   :jon-gui-data-rotary-direction-clockwise
   :jon-gui-data-rotary-direction-counter-clockwise])

(def jon-gui-data-lrf-scan-modes-spec
  "Malli spec for jon-gui-data-lrf-scan-modes enum"
  [:enum :jon-gui-data-lrf-scan-mode-unspecified
   :jon-gui-data-lrf-scan-mode-1-hz-continuous
   :jon-gui-data-lrf-scan-mode-4-hz-continuous
   :jon-gui-data-lrf-scan-mode-10-hz-continuous
   :jon-gui-data-lrf-scan-mode-20-hz-continuous
   :jon-gui-data-lrf-scan-mode-100-hz-continuous
   :jon-gui-data-lrf-scan-mode-200-hz-continuous])

(def jon-gui-datat-lrf-laser-pointer-modes-spec
  "Malli spec for jon-gui-datat-lrf-laser-pointer-modes enum"
  [:enum :jon-gui-data-lrf-laser-pointer-mode-unspecified
   :jon-gui-data-lrf-laser-pointer-mode-off
   :jon-gui-data-lrf-laser-pointer-mode-on-1
   :jon-gui-data-lrf-laser-pointer-mode-on-2])

(def jon-gui-data-compass-calibrate-status-spec
  "Malli spec for jon-gui-data-compass-calibrate-status enum"
  [:enum :jon-gui-data-compass-calibrate-status-unspecified
   :jon-gui-data-compass-calibrate-status-not-calibrating
   :jon-gui-data-compass-calibrate-status-calibrating-short
   :jon-gui-data-compass-calibrate-status-calibrating-long
   :jon-gui-data-compass-calibrate-status-finished
   :jon-gui-data-compass-calibrate-status-error])

(def jon-gui-data-rotary-mode-spec
  "Malli spec for jon-gui-data-rotary-mode enum"
  [:enum :jon-gui-data-rotary-mode-unspecified
   :jon-gui-data-rotary-mode-initialization :jon-gui-data-rotary-mode-speed
   :jon-gui-data-rotary-mode-position :jon-gui-data-rotary-mode-stabilization
   :jon-gui-data-rotary-mode-targeting :jon-gui-data-rotary-mode-video-tracker])

(def jon-gui-data-video-channel-spec
  "Malli spec for jon-gui-data-video-channel enum"
  [:enum :jon-gui-data-video-channel-unspecified
   :jon-gui-data-video-channel-heat :jon-gui-data-video-channel-day])

(def jon-gui-data-rec-osd-screen-spec
  "Malli spec for jon-gui-data-rec-osd-screen enum"
  [:enum :jon-gui-data-rec-osd-screen-unspecified
   :jon-gui-data-rec-osd-screen-main :jon-gui-data-rec-osd-screen-lrf-measure
   :jon-gui-data-rec-osd-screen-lrf-result
   :jon-gui-data-rec-osd-screen-lrf-result-simplified])

(def jon-gui-data-fx-mode-day-spec
  "Malli spec for jon-gui-data-fx-mode-day enum"
  [:enum :jon-gui-data-fx-mode-day-default :jon-gui-data-fx-mode-day-a
   :jon-gui-data-fx-mode-day-b :jon-gui-data-fx-mode-day-c
   :jon-gui-data-fx-mode-day-d :jon-gui-data-fx-mode-day-e
   :jon-gui-data-fx-mode-day-f])

(def jon-gui-data-fx-mode-heat-spec
  "Malli spec for jon-gui-data-fx-mode-heat enum"
  [:enum :jon-gui-data-fx-mode-heat-default :jon-gui-data-fx-mode-heat-a
   :jon-gui-data-fx-mode-heat-b :jon-gui-data-fx-mode-heat-c
   :jon-gui-data-fx-mode-heat-d :jon-gui-data-fx-mode-heat-e
   :jon-gui-data-fx-mode-heat-f])

(def jon-gui-data-system-localizations-spec
  "Malli spec for jon-gui-data-system-localizations enum"
  [:enum :jon-gui-data-system-localization-unspecified
   :jon-gui-data-system-localization-en :jon-gui-data-system-localization-ua
   :jon-gui-data-system-localization-ar :jon-gui-data-system-localization-cs])

(def jon-gui-data-client-type-spec
  "Malli spec for jon-gui-data-client-type enum"
  [:enum :jon-gui-data-client-type-unspecified
   :jon-gui-data-client-type-internal-cv :jon-gui-data-client-type-local-network
   :jon-gui-data-client-type-certificate-protected
   :jon-gui-data-client-type-lira])

(def jon-gui-data-meteo-spec
  "Malli spec for jon-gui-data-meteo message"
  [:map [:temperature [:maybe :float]] [:humidity [:maybe :float]]
   [:pressure [:maybe :float]]])

(def jon-gui-data-meteo-spec
  "Malli spec for jon-gui-data-meteo message"
  [:map [:temperature [:maybe :float]] [:humidity [:maybe :float]]
   [:pressure [:maybe :float]]])

;; =============================================================================
;; Builders and Parsers
;; =============================================================================

;; Forward declarations
(declare build-jon-gui-data-meteo)
(declare build-jon-gui-data-meteo)
(declare parse-jon-gui-data-meteo)
(declare parse-jon-gui-data-meteo)

(>defn build-jon-gui-data-meteo
       "Build a JonGuiDataMeteo protobuf message from a map."
       [m]
       [jon-gui-data-meteo-spec =>
        #(instance? ser.JonSharedDataTypes$JonGuiDataMeteo %)]
       (let [builder (ser.JonSharedDataTypes$JonGuiDataMeteo/newBuilder)]
         ;; Set regular fields
         (when (contains? m :temperature)
           (.setTemperature builder (get m :temperature)))
         (when (contains? m :humidity) (.setHumidity builder (get m :humidity)))
         (when (contains? m :pressure) (.setPressure builder (get m :pressure)))
         (.build builder)))

(>defn build-jon-gui-data-meteo
       "Build a JonGuiDataMeteo protobuf message from a map."
       [m]
       [jon-gui-data-meteo-spec =>
        #(instance? ser.JonSharedDataTypes$JonGuiDataMeteo %)]
       (let [builder (ser.JonSharedDataTypes$JonGuiDataMeteo/newBuilder)]
         ;; Set regular fields
         (when (contains? m :temperature)
           (.setTemperature builder (get m :temperature)))
         (when (contains? m :humidity) (.setHumidity builder (get m :humidity)))
         (when (contains? m :pressure) (.setPressure builder (get m :pressure)))
         (.build builder)))

(>defn parse-jon-gui-data-meteo
       "Parse a JonGuiDataMeteo protobuf message to a map."
       [^ser.JonSharedDataTypes$JonGuiDataMeteo proto]
       [#(instance? ser.JonSharedDataTypes$JonGuiDataMeteo %) =>
        jon-gui-data-meteo-spec]
       (cond-> {}
         ;; Regular fields
         true (assoc :temperature (.getTemperature proto))
         true (assoc :humidity (.getHumidity proto))
         true (assoc :pressure (.getPressure proto))))

(>defn parse-jon-gui-data-meteo
       "Parse a JonGuiDataMeteo protobuf message to a map."
       [^ser.JonSharedDataTypes$JonGuiDataMeteo proto]
       [#(instance? ser.JonSharedDataTypes$JonGuiDataMeteo %) =>
        jon-gui-data-meteo-spec]
       (cond-> {}
         ;; Regular fields
         true (assoc :temperature (.getTemperature proto))
         true (assoc :humidity (.getHumidity proto))
         true (assoc :pressure (.getPressure proto))))