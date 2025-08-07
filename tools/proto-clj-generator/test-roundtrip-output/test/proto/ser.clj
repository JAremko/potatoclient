(ns test.proto.ser
  "Generated protobuf functions."
  (:require [com.fulcrologic.guardrails.malli.core :refer [=> >defn >defn- ?]]
            [malli.core :as m]
            [malli.registry :as mr]
            [potatoclient.specs.malli-oneof :as oneof])
  (:import
    ser.JonSharedDataTypes$JonGuiDataMeteo
    ser.JonSharedDataTime$JonGuiDataTime
    ser.JonSharedDataSystem$JonGuiDataSystem
    ser.JonSharedDataLrf$JonGuiDataLrf
    ser.JonSharedDataLrf$JonGuiDataTarget
    ser.JonSharedDataLrf$RgbColor
    ser.JonSharedDataGps$JonGuiDataGps
    ser.JonSharedDataCompass$JonGuiDataCompass
    ser.JonSharedDataCompassCalibration$JonGuiDataCompassCalibration
    ser.JonSharedDataRotary$JonGuiDataRotary
    ser.JonSharedDataRotary$ScanNode
    ser.JonSharedDataCameraDay$JonGuiDataCameraDay
    ser.JonSharedDataCameraHeat$JonGuiDataCameraHeat
    ser.JonSharedDataRecOsd$JonGuiDataRecOsd
    ser.JonSharedDataDayCamGlassHeater$JonGuiDataDayCamGlassHeater
    ser.JonSharedDataActualSpaceTime$JonGuiDataActualSpaceTime
    ser.JonSharedData$JonGUIState
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
  [:map [:temperature {:optional true} :float]
   [:humidity {:optional true} :float] [:pressure {:optional true} :float]])

(def jon-gui-data-meteo-spec
  "Malli spec for jon-gui-data-meteo message"
  [:map [:temperature {:optional true} :float]
   [:humidity {:optional true} :float] [:pressure {:optional true} :float]])

(def jon-gui-data-time-spec
  "Malli spec for jon-gui-data-time message"
  [:map [:timestamp {:optional true} :int]
   [:manual-timestamp {:optional true} :int] [:zone-id {:optional true} :int]
   [:use-manual-time {:optional true} :boolean]])

(def jon-gui-data-system-spec
  "Malli spec for jon-gui-data-system message"
  [:map
   [:cpu-temperature {:optional true} [:and :float [:>= -273.15] [:<= 150]]]
   [:gpu-temperature {:optional true} [:and :float [:>= -273.15] [:<= 150]]]
   [:gpu-load {:optional true} [:and :float [:>= 0] [:<= 100]]]
   [:cpu-load {:optional true} [:and :float [:>= 0] [:<= 100]]]
   [:power-consumption {:optional true} [:and :float [:>= 0] [:<= 1000]]]
   [:loc {:optional true} :ser/jon-gui-data-system-localizations]
   [:cur-video-rec-dir-year {:optional true} :int]
   [:cur-video-rec-dir-month {:optional true} :int]
   [:cur-video-rec-dir-day {:optional true} :int]
   [:cur-video-rec-dir-hour {:optional true} :int]
   [:cur-video-rec-dir-minute {:optional true} :int]
   [:cur-video-rec-dir-second {:optional true} :int]
   [:rec-enabled {:optional true} :boolean]
   [:important-rec-enabled {:optional true} :boolean]
   [:low-disk-space {:optional true} :boolean]
   [:no-disk-space {:optional true} :boolean]
   [:disk-space {:optional true} :int] [:tracking {:optional true} :boolean]
   [:vampire-mode {:optional true} :boolean]
   [:stabilization-mode {:optional true} :boolean]
   [:geodesic-mode {:optional true} :boolean]
   [:cv-dumping {:optional true} :boolean]])

(def jon-gui-data-lrf-spec
  "Malli spec for jon-gui-data-lrf message"
  [:map [:is-scanning {:optional true} :boolean]
   [:is-measuring {:optional true} :boolean] [:measure-id {:optional true} :int]
   [:target {:optional true} :ser/jon-gui-data-target]
   [:pointer-mode {:optional true} :ser/jon-gui-datat-lrf-laser-pointer-modes]
   [:fog-mode-enabled {:optional true} :boolean]
   [:is-refining {:optional true} :boolean]])

(def jon-gui-data-target-spec
  "Malli spec for jon-gui-data-target message"
  [:map [:timestamp {:optional true} :int]
   [:target-longitude {:optional true} [:and :double [:>= -180] [:<= 180]]]
   [:target-latitude {:optional true} [:and :double [:>= -90] [:<= 90]]]
   [:target-altitude {:optional true} :double]
   [:observer-longitude {:optional true} [:and :double [:>= -180] [:<= 180]]]
   [:observer-latitude {:optional true} [:and :double [:>= -90] [:<= 90]]]
   [:observer-altitude {:optional true} :double]
   [:observer-azimuth {:optional true} [:and :double [:>= 0] [:< 360]]]
   [:observer-elevation {:optional true} [:and :double [:>= -90] [:<= 90]]]
   [:observer-bank {:optional true} [:and :double [:>= -180] [:< 180]]]
   [:distance-2d {:optional true} [:and :double [:>= 0] [:<= 500000]]]
   [:distance-3b {:optional true} [:and :double [:>= 0] [:<= 500000]]]
   [:observer-fix-type {:optional true} :ser/jon-gui-data-gps-fix-type]
   [:session-id {:optional true} :int] [:target-id {:optional true} :int]
   [:target-color {:optional true} :ser/rgb-color] [:type {:optional true} :int]
   [:uuid-part-1 {:optional true} :int] [:uuid-part-2 {:optional true} :int]
   [:uuid-part-3 {:optional true} :int] [:uuid-part-4 {:optional true} :int]])

(def rgb-color-spec
  "Malli spec for rgb-color message"
  [:map [:red {:optional true} [:and :int [:>= 0] [:<= 255]]]
   [:green {:optional true} [:and :int [:>= 0] [:<= 255]]]
   [:blue {:optional true} [:and :int [:>= 0] [:<= 255]]]])

(def jon-gui-data-gps-spec
  "Malli spec for jon-gui-data-gps message"
  [:map [:longitude {:optional true} [:and :double [:>= -180] [:<= 180]]]
   [:latitude {:optional true} [:and :double [:>= -90] [:<= 90]]]
   [:altitude {:optional true} [:and :double [:>= -433] [:<= 8848.86]]]
   [:manual-longitude {:optional true} [:and :double [:>= -180] [:<= 180]]]
   [:manual-latitude {:optional true} [:and :double [:>= -90] [:<= 90]]]
   [:manual-altitude {:optional true} [:and :double [:>= -433] [:<= 8848.86]]]
   [:fix-type {:optional true} :ser/jon-gui-data-gps-fix-type]
   [:use-manual {:optional true} :boolean]])

(def jon-gui-data-compass-spec
  "Malli spec for jon-gui-data-compass message"
  [:map [:azimuth {:optional true} [:and :double [:>= 0] [:< 360]]]
   [:elevation {:optional true} [:and :double [:>= -90] [:<= 90]]]
   [:bank {:optional true} [:and :double [:>= -180] [:< 180]]]
   [:offset-azimuth {:optional true} [:and :double [:>= -180] [:< 180]]]
   [:offset-elevation {:optional true} [:and :double [:>= -90] [:<= 90]]]
   [:magnetic-declination {:optional true} [:and :double [:>= -180] [:< 180]]]
   [:calibrating {:optional true} :boolean]])

(def jon-gui-data-compass-calibration-spec
  "Malli spec for jon-gui-data-compass-calibration message"
  [:map [:stage {:optional true} [:and :int [:>= 0]]]
   [:final-stage {:optional true} [:and :int [:> 0]]]
   [:target-azimuth {:optional true} [:and :double [:>= 0] [:< 360]]]
   [:target-elevation {:optional true} [:and :double [:>= -90] [:<= 90]]]
   [:target-bank {:optional true} [:and :double [:>= -180] [:< 180]]]
   [:status {:optional true} :ser/jon-gui-data-compass-calibrate-status]])

(def jon-gui-data-rotary-spec
  "Malli spec for jon-gui-data-rotary message"
  [:map [:azimuth {:optional true} [:and :float [:>= 0] [:< 360]]]
   [:azimuth-speed {:optional true} [:and :float [:>= -1] [:<= 1]]]
   [:elevation {:optional true} [:and :float [:>= -90] [:<= 90]]]
   [:elevation-speed {:optional true} [:and :float [:>= -1] [:<= 1]]]
   [:platform-azimuth {:optional true} [:and :float [:>= 0] [:< 360]]]
   [:platform-elevation {:optional true} [:and :float [:>= -90] [:<= 90]]]
   [:platform-bank {:optional true} [:and :float [:>= -180] [:< 180]]]
   [:is-moving {:optional true} :boolean]
   [:mode {:optional true} :ser/jon-gui-data-rotary-mode]
   [:is-scanning {:optional true} :boolean]
   [:is-scanning-paused {:optional true} :boolean]
   [:use-rotary-as-compass {:optional true} :boolean]
   [:scan-target {:optional true} :int] [:scan-target-max {:optional true} :int]
   [:sun-azimuth {:optional true} [:and :float [:>= 0] [:< 360]]]
   [:sun-elevation {:optional true} [:and :float [:>= 0] [:< 360]]]
   [:current-scan-node {:optional true} :ser/scan-node]])

(def scan-node-spec
  "Malli spec for scan-node message"
  [:map [:index {:optional true} :int]
   [:day-zoom-table-value {:optional true} :int]
   [:heat-zoom-table-value {:optional true} :int]
   [:azimuth {:optional true} [:and :double [:>= 0] [:< 360]]]
   [:elevation {:optional true} [:and :double [:>= -90] [:<= 90]]]
   [:linger {:optional true} [:and :double [:>= 0]]]
   [:speed {:optional true} [:and :double [:> 0] [:<= 1]]]])

(def jon-gui-data-camera-day-spec
  "Malli spec for jon-gui-data-camera-day message"
  [:map [:focus-pos {:optional true} [:and :float [:>= 0] [:<= 1]]]
   [:zoom-pos {:optional true} [:and :float [:>= 0] [:<= 1]]]
   [:iris-pos {:optional true} [:and :float [:>= 0] [:<= 1]]]
   [:infrared-filter {:optional true} :boolean]
   [:zoom-table-pos {:optional true} :int]
   [:zoom-table-pos-max {:optional true} :int]
   [:fx-mode {:optional true} :ser/jon-gui-data-fx-mode-day]
   [:auto-focus {:optional true} :boolean]
   [:auto-iris {:optional true} :boolean]
   [:digital-zoom-level {:optional true} [:and :float [:>= 1]]]
   [:clahe-level {:optional true} [:and :float [:>= 0] [:<= 1]]]])

(def jon-gui-data-camera-heat-spec
  "Malli spec for jon-gui-data-camera-heat message"
  [:map [:zoom-pos {:optional true} [:and :float [:>= 0] [:<= 1]]]
   [:agc-mode {:optional true} :ser/jon-gui-data-video-channel-heat-agc-modes]
   [:filter {:optional true} :ser/jon-gui-data-video-channel-heat-filters]
   [:auto-focus {:optional true} :boolean]
   [:zoom-table-pos {:optional true} :int]
   [:zoom-table-pos-max {:optional true} :int]
   [:dde-level {:optional true} :int] [:dde-enabled {:optional true} :boolean]
   [:fx-mode {:optional true} :ser/jon-gui-data-fx-mode-heat]
   [:digital-zoom-level {:optional true} [:and :float [:>= 1]]]
   [:clahe-level {:optional true} [:and :float [:>= 0] [:<= 1]]]])

(def jon-gui-data-rec-osd-spec
  "Malli spec for jon-gui-data-rec-osd message"
  [:map [:screen {:optional true} :ser/jon-gui-data-rec-osd-screen]
   [:heat-osd-enabled {:optional true} :boolean]
   [:day-osd-enabled {:optional true} :boolean]
   [:heat-crosshair-offset-horizontal {:optional true} :int]
   [:heat-crosshair-offset-vertical {:optional true} :int]
   [:day-crosshair-offset-horizontal {:optional true} :int]
   [:day-crosshair-offset-vertical {:optional true} :int]])

(def jon-gui-data-day-cam-glass-heater-spec
  "Malli spec for jon-gui-data-day-cam-glass-heater message"
  [:map
   [:temperature {:optional true} [:and :double [:>= -273.15] [:<= 660.32]]]
   [:status {:optional true} :boolean]])

(def jon-gui-data-actual-space-time-spec
  "Malli spec for jon-gui-data-actual-space-time message"
  [:map [:azimuth {:optional true} [:and :float [:>= 0] [:< 360]]]
   [:elevation {:optional true} [:and :float [:>= -90] [:<= 90]]]
   [:bank {:optional true} [:and :float [:>= -180] [:< 180]]]
   [:latitude {:optional true} [:and :float [:>= -90] [:<= 90]]]
   [:longitude {:optional true} [:and :float [:>= -180] [:< 180]]]
   [:altitude {:optional true} [:and :double [:>= -433] [:<= 8848.86]]]
   [:timestamp {:optional true} :int]])

(def jon-gui-state-spec
  "Malli spec for jon-gui-state message"
  [:map [:protocol-version {:optional true} [:and :int [:> 0]]]
   [:system {:optional true} :ser/jon-gui-data-system]
   [:meteo-internal {:optional true} :ser/jon-gui-data-meteo]
   [:lrf {:optional true} :ser/jon-gui-data-lrf]
   [:time {:optional true} :ser/jon-gui-data-time]
   [:gps {:optional true} :ser/jon-gui-data-gps]
   [:compass {:optional true} :ser/jon-gui-data-compass]
   [:rotary {:optional true} :ser/jon-gui-data-rotary]
   [:camera-day {:optional true} :ser/jon-gui-data-camera-day]
   [:camera-heat {:optional true} :ser/jon-gui-data-camera-heat]
   [:compass-calibration {:optional true} :ser/jon-gui-data-compass-calibration]
   [:rec-osd {:optional true} :ser/jon-gui-data-rec-osd]
   [:day-cam-glass-heater {:optional true}
    :ser/jon-gui-data-day-cam-glass-heater]
   [:actual-space-time {:optional true} :ser/jon-gui-data-actual-space-time]])

;; =============================================================================
;; Registry Setup
;; =============================================================================

;; Registry for enum and message specs in this namespace
(def registry
  {:ser/jon-gui-data-video-channel-heat-filters
     jon-gui-data-video-channel-heat-filters-spec,
   :ser/jon-gui-data-video-channel-heat-agc-modes
     jon-gui-data-video-channel-heat-agc-modes-spec,
   :ser/jon-gui-data-gps-units jon-gui-data-gps-units-spec,
   :ser/jon-gui-data-gps-fix-type jon-gui-data-gps-fix-type-spec,
   :ser/jon-gui-data-compass-units jon-gui-data-compass-units-spec,
   :ser/jon-gui-data-accumulator-state-idx
     jon-gui-data-accumulator-state-idx-spec,
   :ser/jon-gui-data-time-formats jon-gui-data-time-formats-spec,
   :ser/jon-gui-data-rotary-direction jon-gui-data-rotary-direction-spec,
   :ser/jon-gui-data-lrf-scan-modes jon-gui-data-lrf-scan-modes-spec,
   :ser/jon-gui-datat-lrf-laser-pointer-modes
     jon-gui-datat-lrf-laser-pointer-modes-spec,
   :ser/jon-gui-data-compass-calibrate-status
     jon-gui-data-compass-calibrate-status-spec,
   :ser/jon-gui-data-rotary-mode jon-gui-data-rotary-mode-spec,
   :ser/jon-gui-data-video-channel jon-gui-data-video-channel-spec,
   :ser/jon-gui-data-rec-osd-screen jon-gui-data-rec-osd-screen-spec,
   :ser/jon-gui-data-fx-mode-day jon-gui-data-fx-mode-day-spec,
   :ser/jon-gui-data-fx-mode-heat jon-gui-data-fx-mode-heat-spec,
   :ser/jon-gui-data-system-localizations
     jon-gui-data-system-localizations-spec,
   :ser/jon-gui-data-client-type jon-gui-data-client-type-spec,
   :ser/jon-gui-data-meteo jon-gui-data-meteo-spec,
   :ser/jon-gui-data-time jon-gui-data-time-spec,
   :ser/jon-gui-data-system jon-gui-data-system-spec,
   :ser/jon-gui-data-lrf jon-gui-data-lrf-spec,
   :ser/jon-gui-data-target jon-gui-data-target-spec,
   :ser/rgb-color rgb-color-spec,
   :ser/jon-gui-data-gps jon-gui-data-gps-spec,
   :ser/jon-gui-data-compass jon-gui-data-compass-spec,
   :ser/jon-gui-data-compass-calibration jon-gui-data-compass-calibration-spec,
   :ser/jon-gui-data-rotary jon-gui-data-rotary-spec,
   :ser/scan-node scan-node-spec,
   :ser/jon-gui-data-camera-day jon-gui-data-camera-day-spec,
   :ser/jon-gui-data-camera-heat jon-gui-data-camera-heat-spec,
   :ser/jon-gui-data-rec-osd jon-gui-data-rec-osd-spec,
   :ser/jon-gui-data-day-cam-glass-heater
     jon-gui-data-day-cam-glass-heater-spec,
   :ser/jon-gui-data-actual-space-time jon-gui-data-actual-space-time-spec,
   :ser/jon-gui-state jon-gui-state-spec})

;; =============================================================================
;; Builders and Parsers
;; =============================================================================

;; Forward declarations
(declare build-jon-gui-data-meteo)
(declare build-jon-gui-data-meteo)
(declare build-jon-gui-data-time)
(declare build-jon-gui-data-system)
(declare build-jon-gui-data-lrf)
(declare build-jon-gui-data-target)
(declare build-rgb-color)
(declare build-jon-gui-data-gps)
(declare build-jon-gui-data-compass)
(declare build-jon-gui-data-compass-calibration)
(declare build-jon-gui-data-rotary)
(declare build-scan-node)
(declare build-jon-gui-data-camera-day)
(declare build-jon-gui-data-camera-heat)
(declare build-jon-gui-data-rec-osd)
(declare build-jon-gui-data-day-cam-glass-heater)
(declare build-jon-gui-data-actual-space-time)
(declare build-jon-gui-state)
(declare parse-jon-gui-data-meteo)
(declare parse-jon-gui-data-meteo)
(declare parse-jon-gui-data-time)
(declare parse-jon-gui-data-system)
(declare parse-jon-gui-data-lrf)
(declare parse-jon-gui-data-target)
(declare parse-rgb-color)
(declare parse-jon-gui-data-gps)
(declare parse-jon-gui-data-compass)
(declare parse-jon-gui-data-compass-calibration)
(declare parse-jon-gui-data-rotary)
(declare parse-scan-node)
(declare parse-jon-gui-data-camera-day)
(declare parse-jon-gui-data-camera-heat)
(declare parse-jon-gui-data-rec-osd)
(declare parse-jon-gui-data-day-cam-glass-heater)
(declare parse-jon-gui-data-actual-space-time)
(declare parse-jon-gui-state)

(>defn build-jon-gui-data-meteo
       "Build a JonGuiDataMeteo protobuf message from a map."
       [m]
       [jon-gui-data-meteo-spec => any?]
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
       [jon-gui-data-meteo-spec => any?]
       (let [builder (ser.JonSharedDataTypes$JonGuiDataMeteo/newBuilder)]
         ;; Set regular fields
         (when (contains? m :temperature)
           (.setTemperature builder (get m :temperature)))
         (when (contains? m :humidity) (.setHumidity builder (get m :humidity)))
         (when (contains? m :pressure) (.setPressure builder (get m :pressure)))
         (.build builder)))

(>defn build-jon-gui-data-time
       "Build a JonGuiDataTime protobuf message from a map."
       [m]
       [jon-gui-data-time-spec => any?]
       (let [builder (ser.JonSharedDataTime$JonGuiDataTime/newBuilder)]
         ;; Set regular fields
         (when (contains? m :timestamp)
           (.setTimestamp builder (get m :timestamp)))
         (when (contains? m :manual-timestamp)
           (.setManualTimestamp builder (get m :manual-timestamp)))
         (when (contains? m :zone-id) (.setZoneId builder (get m :zone-id)))
         (when (contains? m :use-manual-time)
           (.setUseManualTime builder (get m :use-manual-time)))
         (.build builder)))

(>defn
  build-jon-gui-data-system
  "Build a JonGuiDataSystem protobuf message from a map."
  [m]
  [jon-gui-data-system-spec => any?]
  (let [builder (ser.JonSharedDataSystem$JonGuiDataSystem/newBuilder)]
    ;; Set regular fields
    (when (contains? m :cpu-temperature)
      (.setCpuTemperature builder (get m :cpu-temperature)))
    (when (contains? m :gpu-temperature)
      (.setGpuTemperature builder (get m :gpu-temperature)))
    (when (contains? m :gpu-load) (.setGpuLoad builder (get m :gpu-load)))
    (when (contains? m :cpu-load) (.setCpuLoad builder (get m :cpu-load)))
    (when (contains? m :power-consumption)
      (.setPowerConsumption builder (get m :power-consumption)))
    (when (contains? m :loc)
      (.setLoc builder
               (when-let [v (get m :loc)]
                 (get jon-gui-data-system-localizations-values v))))
    (when (contains? m :cur-video-rec-dir-year)
      (.setCurVideoRecDirYear builder (get m :cur-video-rec-dir-year)))
    (when (contains? m :cur-video-rec-dir-month)
      (.setCurVideoRecDirMonth builder (get m :cur-video-rec-dir-month)))
    (when (contains? m :cur-video-rec-dir-day)
      (.setCurVideoRecDirDay builder (get m :cur-video-rec-dir-day)))
    (when (contains? m :cur-video-rec-dir-hour)
      (.setCurVideoRecDirHour builder (get m :cur-video-rec-dir-hour)))
    (when (contains? m :cur-video-rec-dir-minute)
      (.setCurVideoRecDirMinute builder (get m :cur-video-rec-dir-minute)))
    (when (contains? m :cur-video-rec-dir-second)
      (.setCurVideoRecDirSecond builder (get m :cur-video-rec-dir-second)))
    (when (contains? m :rec-enabled)
      (.setRecEnabled builder (get m :rec-enabled)))
    (when (contains? m :important-rec-enabled)
      (.setImportantRecEnabled builder (get m :important-rec-enabled)))
    (when (contains? m :low-disk-space)
      (.setLowDiskSpace builder (get m :low-disk-space)))
    (when (contains? m :no-disk-space)
      (.setNoDiskSpace builder (get m :no-disk-space)))
    (when (contains? m :disk-space) (.setDiskSpace builder (get m :disk-space)))
    (when (contains? m :tracking) (.setTracking builder (get m :tracking)))
    (when (contains? m :vampire-mode)
      (.setVampireMode builder (get m :vampire-mode)))
    (when (contains? m :stabilization-mode)
      (.setStabilizationMode builder (get m :stabilization-mode)))
    (when (contains? m :geodesic-mode)
      (.setGeodesicMode builder (get m :geodesic-mode)))
    (when (contains? m :cv-dumping) (.setCvDumping builder (get m :cv-dumping)))
    (.build builder)))

(>defn
  build-jon-gui-data-lrf
  "Build a JonGuiDataLrf protobuf message from a map."
  [m]
  [jon-gui-data-lrf-spec => any?]
  (let [builder (ser.JonSharedDataLrf$JonGuiDataLrf/newBuilder)]
    ;; Set regular fields
    (when (contains? m :is-scanning)
      (.setIsScanning builder (get m :is-scanning)))
    (when (contains? m :is-measuring)
      (.setIsMeasuring builder (get m :is-measuring)))
    (when (contains? m :measure-id) (.setMeasureId builder (get m :measure-id)))
    (when (contains? m :target)
      (.setTarget builder (build-jon-gui-data-target (get m :target))))
    (when (contains? m :pointer-mode)
      (.setPointerMode builder
                       (when-let [v (get m :pointer-mode)]
                         (get jon-gui-datat-lrf-laser-pointer-modes-values v))))
    (when (contains? m :fog-mode-enabled)
      (.setFogModeEnabled builder (get m :fog-mode-enabled)))
    (when (contains? m :is-refining)
      (.setIsRefining builder (get m :is-refining)))
    (.build builder)))

(>defn
  build-jon-gui-data-target
  "Build a JonGuiDataTarget protobuf message from a map."
  [m]
  [jon-gui-data-target-spec => any?]
  (let [builder (ser.JonSharedDataLrf$JonGuiDataTarget/newBuilder)]
    ;; Set regular fields
    (when (contains? m :timestamp) (.setTimestamp builder (get m :timestamp)))
    (when (contains? m :target-longitude)
      (.setTargetLongitude builder (get m :target-longitude)))
    (when (contains? m :target-latitude)
      (.setTargetLatitude builder (get m :target-latitude)))
    (when (contains? m :target-altitude)
      (.setTargetAltitude builder (get m :target-altitude)))
    (when (contains? m :observer-longitude)
      (.setObserverLongitude builder (get m :observer-longitude)))
    (when (contains? m :observer-latitude)
      (.setObserverLatitude builder (get m :observer-latitude)))
    (when (contains? m :observer-altitude)
      (.setObserverAltitude builder (get m :observer-altitude)))
    (when (contains? m :observer-azimuth)
      (.setObserverAzimuth builder (get m :observer-azimuth)))
    (when (contains? m :observer-elevation)
      (.setObserverElevation builder (get m :observer-elevation)))
    (when (contains? m :observer-bank)
      (.setObserverBank builder (get m :observer-bank)))
    (when (contains? m :distance-2d)
      (.setDistance2d builder (get m :distance-2d)))
    (when (contains? m :distance-3b)
      (.setDistance3b builder (get m :distance-3b)))
    (when (contains? m :observer-fix-type)
      (.setObserverFixType builder
                           (when-let [v (get m :observer-fix-type)]
                             (get jon-gui-data-gps-fix-type-values v))))
    (when (contains? m :session-id) (.setSessionId builder (get m :session-id)))
    (when (contains? m :target-id) (.setTargetId builder (get m :target-id)))
    (when (contains? m :target-color)
      (.setTargetColor builder (build-rgb-color (get m :target-color))))
    (when (contains? m :type) (.setType builder (get m :type)))
    (when (contains? m :uuid-part-1)
      (.setUuidPart1 builder (get m :uuid-part-1)))
    (when (contains? m :uuid-part-2)
      (.setUuidPart2 builder (get m :uuid-part-2)))
    (when (contains? m :uuid-part-3)
      (.setUuidPart3 builder (get m :uuid-part-3)))
    (when (contains? m :uuid-part-4)
      (.setUuidPart4 builder (get m :uuid-part-4)))
    (.build builder)))

(>defn build-rgb-color
       "Build a RgbColor protobuf message from a map."
       [m]
       [rgb-color-spec => any?]
       (let [builder (ser.JonSharedDataLrf$RgbColor/newBuilder)]
         ;; Set regular fields
         (when (contains? m :red) (.setRed builder (get m :red)))
         (when (contains? m :green) (.setGreen builder (get m :green)))
         (when (contains? m :blue) (.setBlue builder (get m :blue)))
         (.build builder)))

(>defn
  build-jon-gui-data-gps
  "Build a JonGuiDataGps protobuf message from a map."
  [m]
  [jon-gui-data-gps-spec => any?]
  (let [builder (ser.JonSharedDataGps$JonGuiDataGps/newBuilder)]
    ;; Set regular fields
    (when (contains? m :longitude) (.setLongitude builder (get m :longitude)))
    (when (contains? m :latitude) (.setLatitude builder (get m :latitude)))
    (when (contains? m :altitude) (.setAltitude builder (get m :altitude)))
    (when (contains? m :manual-longitude)
      (.setManualLongitude builder (get m :manual-longitude)))
    (when (contains? m :manual-latitude)
      (.setManualLatitude builder (get m :manual-latitude)))
    (when (contains? m :manual-altitude)
      (.setManualAltitude builder (get m :manual-altitude)))
    (when (contains? m :fix-type)
      (.setFixType builder
                   (when-let [v (get m :fix-type)]
                     (get jon-gui-data-gps-fix-type-values v))))
    (when (contains? m :use-manual) (.setUseManual builder (get m :use-manual)))
    (.build builder)))

(>defn build-jon-gui-data-compass
       "Build a JonGuiDataCompass protobuf message from a map."
       [m]
       [jon-gui-data-compass-spec => any?]
       (let [builder (ser.JonSharedDataCompass$JonGuiDataCompass/newBuilder)]
         ;; Set regular fields
         (when (contains? m :azimuth) (.setAzimuth builder (get m :azimuth)))
         (when (contains? m :elevation)
           (.setElevation builder (get m :elevation)))
         (when (contains? m :bank) (.setBank builder (get m :bank)))
         (when (contains? m :offset-azimuth)
           (.setOffsetAzimuth builder (get m :offset-azimuth)))
         (when (contains? m :offset-elevation)
           (.setOffsetElevation builder (get m :offset-elevation)))
         (when (contains? m :magnetic-declination)
           (.setMagneticDeclination builder (get m :magnetic-declination)))
         (when (contains? m :calibrating)
           (.setCalibrating builder (get m :calibrating)))
         (.build builder)))

(>defn
  build-jon-gui-data-compass-calibration
  "Build a JonGuiDataCompassCalibration protobuf message from a map."
  [m]
  [jon-gui-data-compass-calibration-spec => any?]
  (let
    [builder
       (ser.JonSharedDataCompassCalibration$JonGuiDataCompassCalibration/newBuilder)]
    ;; Set regular fields
    (when (contains? m :stage) (.setStage builder (get m :stage)))
    (when (contains? m :final-stage)
      (.setFinalStage builder (get m :final-stage)))
    (when (contains? m :target-azimuth)
      (.setTargetAzimuth builder (get m :target-azimuth)))
    (when (contains? m :target-elevation)
      (.setTargetElevation builder (get m :target-elevation)))
    (when (contains? m :target-bank)
      (.setTargetBank builder (get m :target-bank)))
    (when (contains? m :status)
      (.setStatus builder
                  (when-let [v (get m :status)]
                    (get jon-gui-data-compass-calibrate-status-values v))))
    (.build builder)))

(>defn
  build-jon-gui-data-rotary
  "Build a JonGuiDataRotary protobuf message from a map."
  [m]
  [jon-gui-data-rotary-spec => any?]
  (let [builder (ser.JonSharedDataRotary$JonGuiDataRotary/newBuilder)]
    ;; Set regular fields
    (when (contains? m :azimuth) (.setAzimuth builder (get m :azimuth)))
    (when (contains? m :azimuth-speed)
      (.setAzimuthSpeed builder (get m :azimuth-speed)))
    (when (contains? m :elevation) (.setElevation builder (get m :elevation)))
    (when (contains? m :elevation-speed)
      (.setElevationSpeed builder (get m :elevation-speed)))
    (when (contains? m :platform-azimuth)
      (.setPlatformAzimuth builder (get m :platform-azimuth)))
    (when (contains? m :platform-elevation)
      (.setPlatformElevation builder (get m :platform-elevation)))
    (when (contains? m :platform-bank)
      (.setPlatformBank builder (get m :platform-bank)))
    (when (contains? m :is-moving) (.setIsMoving builder (get m :is-moving)))
    (when (contains? m :mode)
      (.setMode builder
                (when-let [v (get m :mode)]
                  (get jon-gui-data-rotary-mode-values v))))
    (when (contains? m :is-scanning)
      (.setIsScanning builder (get m :is-scanning)))
    (when (contains? m :is-scanning-paused)
      (.setIsScanningPaused builder (get m :is-scanning-paused)))
    (when (contains? m :use-rotary-as-compass)
      (.setUseRotaryAsCompass builder (get m :use-rotary-as-compass)))
    (when (contains? m :scan-target)
      (.setScanTarget builder (get m :scan-target)))
    (when (contains? m :scan-target-max)
      (.setScanTargetMax builder (get m :scan-target-max)))
    (when (contains? m :sun-azimuth)
      (.setSunAzimuth builder (get m :sun-azimuth)))
    (when (contains? m :sun-elevation)
      (.setSunElevation builder (get m :sun-elevation)))
    (when (contains? m :current-scan-node)
      (.setCurrentScanNode builder
                           (build-scan-node (get m :current-scan-node))))
    (.build builder)))

(>defn build-scan-node
       "Build a ScanNode protobuf message from a map."
       [m]
       [scan-node-spec => any?]
       (let [builder (ser.JonSharedDataRotary$ScanNode/newBuilder)]
         ;; Set regular fields
         (when (contains? m :index) (.setIndex builder (get m :index)))
         (when (contains? m :day-zoom-table-value)
           (.setDayZoomTableValue builder (get m :day-zoom-table-value)))
         (when (contains? m :heat-zoom-table-value)
           (.setHeatZoomTableValue builder (get m :heat-zoom-table-value)))
         (when (contains? m :azimuth) (.setAzimuth builder (get m :azimuth)))
         (when (contains? m :elevation)
           (.setElevation builder (get m :elevation)))
         (when (contains? m :linger) (.setLinger builder (get m :linger)))
         (when (contains? m :speed) (.setSpeed builder (get m :speed)))
         (.build builder)))

(>defn
  build-jon-gui-data-camera-day
  "Build a JonGuiDataCameraDay protobuf message from a map."
  [m]
  [jon-gui-data-camera-day-spec => any?]
  (let [builder (ser.JonSharedDataCameraDay$JonGuiDataCameraDay/newBuilder)]
    ;; Set regular fields
    (when (contains? m :focus-pos) (.setFocusPos builder (get m :focus-pos)))
    (when (contains? m :zoom-pos) (.setZoomPos builder (get m :zoom-pos)))
    (when (contains? m :iris-pos) (.setIrisPos builder (get m :iris-pos)))
    (when (contains? m :infrared-filter)
      (.setInfraredFilter builder (get m :infrared-filter)))
    (when (contains? m :zoom-table-pos)
      (.setZoomTablePos builder (get m :zoom-table-pos)))
    (when (contains? m :zoom-table-pos-max)
      (.setZoomTablePosMax builder (get m :zoom-table-pos-max)))
    (when (contains? m :fx-mode)
      (.setFxMode builder
                  (when-let [v (get m :fx-mode)]
                    (get jon-gui-data-fx-mode-day-values v))))
    (when (contains? m :auto-focus) (.setAutoFocus builder (get m :auto-focus)))
    (when (contains? m :auto-iris) (.setAutoIris builder (get m :auto-iris)))
    (when (contains? m :digital-zoom-level)
      (.setDigitalZoomLevel builder (get m :digital-zoom-level)))
    (when (contains? m :clahe-level)
      (.setClaheLevel builder (get m :clahe-level)))
    (.build builder)))

(>defn
  build-jon-gui-data-camera-heat
  "Build a JonGuiDataCameraHeat protobuf message from a map."
  [m]
  [jon-gui-data-camera-heat-spec => any?]
  (let [builder (ser.JonSharedDataCameraHeat$JonGuiDataCameraHeat/newBuilder)]
    ;; Set regular fields
    (when (contains? m :zoom-pos) (.setZoomPos builder (get m :zoom-pos)))
    (when (contains? m :agc-mode)
      (.setAgcMode builder
                   (when-let [v (get m :agc-mode)]
                     (get jon-gui-data-video-channel-heat-agc-modes-values v))))
    (when (contains? m :filter)
      (.setFilter builder
                  (when-let [v (get m :filter)]
                    (get jon-gui-data-video-channel-heat-filters-values v))))
    (when (contains? m :auto-focus) (.setAutoFocus builder (get m :auto-focus)))
    (when (contains? m :zoom-table-pos)
      (.setZoomTablePos builder (get m :zoom-table-pos)))
    (when (contains? m :zoom-table-pos-max)
      (.setZoomTablePosMax builder (get m :zoom-table-pos-max)))
    (when (contains? m :dde-level) (.setDdeLevel builder (get m :dde-level)))
    (when (contains? m :dde-enabled)
      (.setDdeEnabled builder (get m :dde-enabled)))
    (when (contains? m :fx-mode)
      (.setFxMode builder
                  (when-let [v (get m :fx-mode)]
                    (get jon-gui-data-fx-mode-heat-values v))))
    (when (contains? m :digital-zoom-level)
      (.setDigitalZoomLevel builder (get m :digital-zoom-level)))
    (when (contains? m :clahe-level)
      (.setClaheLevel builder (get m :clahe-level)))
    (.build builder)))

(>defn
  build-jon-gui-data-rec-osd
  "Build a JonGuiDataRecOsd protobuf message from a map."
  [m]
  [jon-gui-data-rec-osd-spec => any?]
  (let [builder (ser.JonSharedDataRecOsd$JonGuiDataRecOsd/newBuilder)]
    ;; Set regular fields
    (when (contains? m :screen)
      (.setScreen builder
                  (when-let [v (get m :screen)]
                    (get jon-gui-data-rec-osd-screen-values v))))
    (when (contains? m :heat-osd-enabled)
      (.setHeatOsdEnabled builder (get m :heat-osd-enabled)))
    (when (contains? m :day-osd-enabled)
      (.setDayOsdEnabled builder (get m :day-osd-enabled)))
    (when (contains? m :heat-crosshair-offset-horizontal)
      (.setHeatCrosshairOffsetHorizontal
        builder
        (get m :heat-crosshair-offset-horizontal)))
    (when (contains? m :heat-crosshair-offset-vertical)
      (.setHeatCrosshairOffsetVertical builder
                                       (get m :heat-crosshair-offset-vertical)))
    (when (contains? m :day-crosshair-offset-horizontal)
      (.setDayCrosshairOffsetHorizontal builder
                                        (get m
                                             :day-crosshair-offset-horizontal)))
    (when (contains? m :day-crosshair-offset-vertical)
      (.setDayCrosshairOffsetVertical builder
                                      (get m :day-crosshair-offset-vertical)))
    (.build builder)))

(>defn
  build-jon-gui-data-day-cam-glass-heater
  "Build a JonGuiDataDayCamGlassHeater protobuf message from a map."
  [m]
  [jon-gui-data-day-cam-glass-heater-spec => any?]
  (let
    [builder
       (ser.JonSharedDataDayCamGlassHeater$JonGuiDataDayCamGlassHeater/newBuilder)]
    ;; Set regular fields
    (when (contains? m :temperature)
      (.setTemperature builder (get m :temperature)))
    (when (contains? m :status) (.setStatus builder (get m :status)))
    (.build builder)))

(>defn
  build-jon-gui-data-actual-space-time
  "Build a JonGuiDataActualSpaceTime protobuf message from a map."
  [m]
  [jon-gui-data-actual-space-time-spec => any?]
  (let
    [builder
       (ser.JonSharedDataActualSpaceTime$JonGuiDataActualSpaceTime/newBuilder)]
    ;; Set regular fields
    (when (contains? m :azimuth) (.setAzimuth builder (get m :azimuth)))
    (when (contains? m :elevation) (.setElevation builder (get m :elevation)))
    (when (contains? m :bank) (.setBank builder (get m :bank)))
    (when (contains? m :latitude) (.setLatitude builder (get m :latitude)))
    (when (contains? m :longitude) (.setLongitude builder (get m :longitude)))
    (when (contains? m :altitude) (.setAltitude builder (get m :altitude)))
    (when (contains? m :timestamp) (.setTimestamp builder (get m :timestamp)))
    (.build builder)))

(>defn
  build-jon-gui-state
  "Build a JonGUIState protobuf message from a map."
  [m]
  [jon-gui-state-spec => any?]
  (let [builder (ser.JonSharedData$JonGUIState/newBuilder)]
    ;; Set regular fields
    (when (contains? m :protocol-version)
      (.setProtocolVersion builder (get m :protocol-version)))
    (when (contains? m :system)
      (.setSystem builder (build-jon-gui-data-system (get m :system))))
    (when (contains? m :meteo-internal)
      (.setMeteoInternal builder
                         (build-jon-gui-data-meteo (get m :meteo-internal))))
    (when (contains? m :lrf)
      (.setLrf builder (build-jon-gui-data-lrf (get m :lrf))))
    (when (contains? m :time)
      (.setTime builder (build-jon-gui-data-time (get m :time))))
    (when (contains? m :gps)
      (.setGps builder (build-jon-gui-data-gps (get m :gps))))
    (when (contains? m :compass)
      (.setCompass builder (build-jon-gui-data-compass (get m :compass))))
    (when (contains? m :rotary)
      (.setRotary builder (build-jon-gui-data-rotary (get m :rotary))))
    (when (contains? m :camera-day)
      (.setCameraDay builder
                     (build-jon-gui-data-camera-day (get m :camera-day))))
    (when (contains? m :camera-heat)
      (.setCameraHeat builder
                      (build-jon-gui-data-camera-heat (get m :camera-heat))))
    (when (contains? m :compass-calibration)
      (.setCompassCalibration builder
                              (build-jon-gui-data-compass-calibration
                                (get m :compass-calibration))))
    (when (contains? m :rec-osd)
      (.setRecOsd builder (build-jon-gui-data-rec-osd (get m :rec-osd))))
    (when (contains? m :day-cam-glass-heater)
      (.setDayCamGlassHeater builder
                             (build-jon-gui-data-day-cam-glass-heater
                               (get m :day-cam-glass-heater))))
    (when (contains? m :actual-space-time)
      (.setActualSpaceTime builder
                           (build-jon-gui-data-actual-space-time
                             (get m :actual-space-time))))
    (.build builder)))

(>defn parse-jon-gui-data-meteo
       "Parse a JonGuiDataMeteo protobuf message to a map."
       [^ser.JonSharedDataTypes$JonGuiDataMeteo proto]
       [any? => jon-gui-data-meteo-spec]
       (cond-> {}
         ;; Regular fields
         true (assoc :temperature (.getTemperature proto))
         true (assoc :humidity (.getHumidity proto))
         true (assoc :pressure (.getPressure proto))))

(>defn parse-jon-gui-data-meteo
       "Parse a JonGuiDataMeteo protobuf message to a map."
       [^ser.JonSharedDataTypes$JonGuiDataMeteo proto]
       [any? => jon-gui-data-meteo-spec]
       (cond-> {}
         ;; Regular fields
         true (assoc :temperature (.getTemperature proto))
         true (assoc :humidity (.getHumidity proto))
         true (assoc :pressure (.getPressure proto))))

(>defn parse-jon-gui-data-time
       "Parse a JonGuiDataTime protobuf message to a map."
       [^ser.JonSharedDataTime$JonGuiDataTime proto]
       [any? => jon-gui-data-time-spec]
       (cond-> {}
         ;; Regular fields
         true (assoc :timestamp (.getTimestamp proto))
         true (assoc :manual-timestamp (.getManualTimestamp proto))
         true (assoc :zone-id (.getZoneId proto))
         true (assoc :use-manual-time (.getUseManualTime proto))))

(>defn parse-jon-gui-data-system
       "Parse a JonGuiDataSystem protobuf message to a map."
       [^ser.JonSharedDataSystem$JonGuiDataSystem proto]
       [any? => jon-gui-data-system-spec]
       (cond-> {}
         ;; Regular fields
         true (assoc :cpu-temperature (.getCpuTemperature proto))
         true (assoc :gpu-temperature (.getGpuTemperature proto))
         true (assoc :gpu-load (.getGpuLoad proto))
         true (assoc :cpu-load (.getCpuLoad proto))
         true (assoc :power-consumption (.getPowerConsumption proto))
         true (assoc :loc
                (get jon-gui-data-system-localizations-keywords
                     (.getLoc proto)))
         true (assoc :cur-video-rec-dir-year (.getCurVideoRecDirYear proto))
         true (assoc :cur-video-rec-dir-month (.getCurVideoRecDirMonth proto))
         true (assoc :cur-video-rec-dir-day (.getCurVideoRecDirDay proto))
         true (assoc :cur-video-rec-dir-hour (.getCurVideoRecDirHour proto))
         true (assoc :cur-video-rec-dir-minute (.getCurVideoRecDirMinute proto))
         true (assoc :cur-video-rec-dir-second (.getCurVideoRecDirSecond proto))
         true (assoc :rec-enabled (.getRecEnabled proto))
         true (assoc :important-rec-enabled (.getImportantRecEnabled proto))
         true (assoc :low-disk-space (.getLowDiskSpace proto))
         true (assoc :no-disk-space (.getNoDiskSpace proto))
         true (assoc :disk-space (.getDiskSpace proto))
         true (assoc :tracking (.getTracking proto))
         true (assoc :vampire-mode (.getVampireMode proto))
         true (assoc :stabilization-mode (.getStabilizationMode proto))
         true (assoc :geodesic-mode (.getGeodesicMode proto))
         true (assoc :cv-dumping (.getCvDumping proto))))

(>defn parse-jon-gui-data-lrf
       "Parse a JonGuiDataLrf protobuf message to a map."
       [^ser.JonSharedDataLrf$JonGuiDataLrf proto]
       [any? => jon-gui-data-lrf-spec]
       (cond-> {}
         ;; Regular fields
         true (assoc :is-scanning (.getIsScanning proto))
         true (assoc :is-measuring (.getIsMeasuring proto))
         true (assoc :measure-id (.getMeasureId proto))
         (.hasTarget proto) (assoc :target
                              (parse-jon-gui-data-target (.getTarget proto)))
         true (assoc :pointer-mode
                (get jon-gui-datat-lrf-laser-pointer-modes-keywords
                     (.getPointerMode proto)))
         true (assoc :fog-mode-enabled (.getFogModeEnabled proto))
         true (assoc :is-refining (.getIsRefining proto))))

(>defn parse-jon-gui-data-target
       "Parse a JonGuiDataTarget protobuf message to a map."
       [^ser.JonSharedDataLrf$JonGuiDataTarget proto]
       [any? => jon-gui-data-target-spec]
       (cond-> {}
         ;; Regular fields
         true (assoc :timestamp (.getTimestamp proto))
         true (assoc :target-longitude (.getTargetLongitude proto))
         true (assoc :target-latitude (.getTargetLatitude proto))
         true (assoc :target-altitude (.getTargetAltitude proto))
         true (assoc :observer-longitude (.getObserverLongitude proto))
         true (assoc :observer-latitude (.getObserverLatitude proto))
         true (assoc :observer-altitude (.getObserverAltitude proto))
         true (assoc :observer-azimuth (.getObserverAzimuth proto))
         true (assoc :observer-elevation (.getObserverElevation proto))
         true (assoc :observer-bank (.getObserverBank proto))
         true (assoc :distance-2d (.getDistance2d proto))
         true (assoc :distance-3b (.getDistance3b proto))
         true (assoc :observer-fix-type
                (get jon-gui-data-gps-fix-type-keywords
                     (.getObserverFixType proto)))
         true (assoc :session-id (.getSessionId proto))
         true (assoc :target-id (.getTargetId proto))
         (.hasTargetColor proto) (assoc :target-color
                                   (parse-rgb-color (.getTargetColor proto)))
         true (assoc :type (.getType proto))
         true (assoc :uuid-part-1 (.getUuidPart1 proto))
         true (assoc :uuid-part-2 (.getUuidPart2 proto))
         true (assoc :uuid-part-3 (.getUuidPart3 proto))
         true (assoc :uuid-part-4 (.getUuidPart4 proto))))

(>defn parse-rgb-color
       "Parse a RgbColor protobuf message to a map."
       [^ser.JonSharedDataLrf$RgbColor proto]
       [any? => rgb-color-spec]
       (cond-> {}
         ;; Regular fields
         true (assoc :red (.getRed proto))
         true (assoc :green (.getGreen proto))
         true (assoc :blue (.getBlue proto))))

(>defn parse-jon-gui-data-gps
       "Parse a JonGuiDataGps protobuf message to a map."
       [^ser.JonSharedDataGps$JonGuiDataGps proto]
       [any? => jon-gui-data-gps-spec]
       (cond-> {}
         ;; Regular fields
         true (assoc :longitude (.getLongitude proto))
         true (assoc :latitude (.getLatitude proto))
         true (assoc :altitude (.getAltitude proto))
         true (assoc :manual-longitude (.getManualLongitude proto))
         true (assoc :manual-latitude (.getManualLatitude proto))
         true (assoc :manual-altitude (.getManualAltitude proto))
         true (assoc :fix-type
                (get jon-gui-data-gps-fix-type-keywords (.getFixType proto)))
         true (assoc :use-manual (.getUseManual proto))))

(>defn parse-jon-gui-data-compass
       "Parse a JonGuiDataCompass protobuf message to a map."
       [^ser.JonSharedDataCompass$JonGuiDataCompass proto]
       [any? => jon-gui-data-compass-spec]
       (cond-> {}
         ;; Regular fields
         true (assoc :azimuth (.getAzimuth proto))
         true (assoc :elevation (.getElevation proto))
         true (assoc :bank (.getBank proto))
         true (assoc :offset-azimuth (.getOffsetAzimuth proto))
         true (assoc :offset-elevation (.getOffsetElevation proto))
         true (assoc :magnetic-declination (.getMagneticDeclination proto))
         true (assoc :calibrating (.getCalibrating proto))))

(>defn parse-jon-gui-data-compass-calibration
       "Parse a JonGuiDataCompassCalibration protobuf message to a map."
       [^ser.JonSharedDataCompassCalibration$JonGuiDataCompassCalibration proto]
       [any? => jon-gui-data-compass-calibration-spec]
       (cond-> {}
         ;; Regular fields
         true (assoc :stage (.getStage proto))
         true (assoc :final-stage (.getFinalStage proto))
         true (assoc :target-azimuth (.getTargetAzimuth proto))
         true (assoc :target-elevation (.getTargetElevation proto))
         true (assoc :target-bank (.getTargetBank proto))
         true (assoc :status
                (get jon-gui-data-compass-calibrate-status-keywords
                     (.getStatus proto)))))

(>defn
  parse-jon-gui-data-rotary
  "Parse a JonGuiDataRotary protobuf message to a map."
  [^ser.JonSharedDataRotary$JonGuiDataRotary proto]
  [any? => jon-gui-data-rotary-spec]
  (cond-> {}
    ;; Regular fields
    true (assoc :azimuth (.getAzimuth proto))
    true (assoc :azimuth-speed (.getAzimuthSpeed proto))
    true (assoc :elevation (.getElevation proto))
    true (assoc :elevation-speed (.getElevationSpeed proto))
    true (assoc :platform-azimuth (.getPlatformAzimuth proto))
    true (assoc :platform-elevation (.getPlatformElevation proto))
    true (assoc :platform-bank (.getPlatformBank proto))
    true (assoc :is-moving (.getIsMoving proto))
    true (assoc :mode (get jon-gui-data-rotary-mode-keywords (.getMode proto)))
    true (assoc :is-scanning (.getIsScanning proto))
    true (assoc :is-scanning-paused (.getIsScanningPaused proto))
    true (assoc :use-rotary-as-compass (.getUseRotaryAsCompass proto))
    true (assoc :scan-target (.getScanTarget proto))
    true (assoc :scan-target-max (.getScanTargetMax proto))
    true (assoc :sun-azimuth (.getSunAzimuth proto))
    true (assoc :sun-elevation (.getSunElevation proto))
    (.hasCurrentScanNode proto)
      (assoc :current-scan-node (parse-scan-node (.getCurrentScanNode proto)))))

(>defn parse-scan-node
       "Parse a ScanNode protobuf message to a map."
       [^ser.JonSharedDataRotary$ScanNode proto]
       [any? => scan-node-spec]
       (cond-> {}
         ;; Regular fields
         true (assoc :index (.getIndex proto))
         true (assoc :day-zoom-table-value (.getDayZoomTableValue proto))
         true (assoc :heat-zoom-table-value (.getHeatZoomTableValue proto))
         true (assoc :azimuth (.getAzimuth proto))
         true (assoc :elevation (.getElevation proto))
         true (assoc :linger (.getLinger proto))
         true (assoc :speed (.getSpeed proto))))

(>defn parse-jon-gui-data-camera-day
       "Parse a JonGuiDataCameraDay protobuf message to a map."
       [^ser.JonSharedDataCameraDay$JonGuiDataCameraDay proto]
       [any? => jon-gui-data-camera-day-spec]
       (cond-> {}
         ;; Regular fields
         true (assoc :focus-pos (.getFocusPos proto))
         true (assoc :zoom-pos (.getZoomPos proto))
         true (assoc :iris-pos (.getIrisPos proto))
         true (assoc :infrared-filter (.getInfraredFilter proto))
         true (assoc :zoom-table-pos (.getZoomTablePos proto))
         true (assoc :zoom-table-pos-max (.getZoomTablePosMax proto))
         true (assoc :fx-mode
                (get jon-gui-data-fx-mode-day-keywords (.getFxMode proto)))
         true (assoc :auto-focus (.getAutoFocus proto))
         true (assoc :auto-iris (.getAutoIris proto))
         true (assoc :digital-zoom-level (.getDigitalZoomLevel proto))
         true (assoc :clahe-level (.getClaheLevel proto))))

(>defn parse-jon-gui-data-camera-heat
       "Parse a JonGuiDataCameraHeat protobuf message to a map."
       [^ser.JonSharedDataCameraHeat$JonGuiDataCameraHeat proto]
       [any? => jon-gui-data-camera-heat-spec]
       (cond-> {}
         ;; Regular fields
         true (assoc :zoom-pos (.getZoomPos proto))
         true (assoc :agc-mode
                (get jon-gui-data-video-channel-heat-agc-modes-keywords
                     (.getAgcMode proto)))
         true (assoc :filter
                (get jon-gui-data-video-channel-heat-filters-keywords
                     (.getFilter proto)))
         true (assoc :auto-focus (.getAutoFocus proto))
         true (assoc :zoom-table-pos (.getZoomTablePos proto))
         true (assoc :zoom-table-pos-max (.getZoomTablePosMax proto))
         true (assoc :dde-level (.getDdeLevel proto))
         true (assoc :dde-enabled (.getDdeEnabled proto))
         true (assoc :fx-mode
                (get jon-gui-data-fx-mode-heat-keywords (.getFxMode proto)))
         true (assoc :digital-zoom-level (.getDigitalZoomLevel proto))
         true (assoc :clahe-level (.getClaheLevel proto))))

(>defn parse-jon-gui-data-rec-osd
       "Parse a JonGuiDataRecOsd protobuf message to a map."
       [^ser.JonSharedDataRecOsd$JonGuiDataRecOsd proto]
       [any? => jon-gui-data-rec-osd-spec]
       (cond-> {}
         ;; Regular fields
         true (assoc :screen
                (get jon-gui-data-rec-osd-screen-keywords (.getScreen proto)))
         true (assoc :heat-osd-enabled (.getHeatOsdEnabled proto))
         true (assoc :day-osd-enabled (.getDayOsdEnabled proto))
         true (assoc :heat-crosshair-offset-horizontal
                (.getHeatCrosshairOffsetHorizontal proto))
         true (assoc :heat-crosshair-offset-vertical
                (.getHeatCrosshairOffsetVertical proto))
         true (assoc :day-crosshair-offset-horizontal
                (.getDayCrosshairOffsetHorizontal proto))
         true (assoc :day-crosshair-offset-vertical
                (.getDayCrosshairOffsetVertical proto))))

(>defn parse-jon-gui-data-day-cam-glass-heater
       "Parse a JonGuiDataDayCamGlassHeater protobuf message to a map."
       [^ser.JonSharedDataDayCamGlassHeater$JonGuiDataDayCamGlassHeater proto]
       [any? => jon-gui-data-day-cam-glass-heater-spec]
       (cond-> {}
         ;; Regular fields
         true (assoc :temperature (.getTemperature proto))
         true (assoc :status (.getStatus proto))))

(>defn parse-jon-gui-data-actual-space-time
       "Parse a JonGuiDataActualSpaceTime protobuf message to a map."
       [^ser.JonSharedDataActualSpaceTime$JonGuiDataActualSpaceTime proto]
       [any? => jon-gui-data-actual-space-time-spec]
       (cond-> {}
         ;; Regular fields
         true (assoc :azimuth (.getAzimuth proto))
         true (assoc :elevation (.getElevation proto))
         true (assoc :bank (.getBank proto))
         true (assoc :latitude (.getLatitude proto))
         true (assoc :longitude (.getLongitude proto))
         true (assoc :altitude (.getAltitude proto))
         true (assoc :timestamp (.getTimestamp proto))))

(>defn
  parse-jon-gui-state
  "Parse a JonGUIState protobuf message to a map."
  [^ser.JonSharedData$JonGUIState proto]
  [any? => jon-gui-state-spec]
  (cond-> {}
    ;; Regular fields
    true (assoc :protocol-version (.getProtocolVersion proto))
    (.hasSystem proto) (assoc :system
                         (parse-jon-gui-data-system (.getSystem proto)))
    (.hasMeteoInternal proto) (assoc :meteo-internal
                                (parse-jon-gui-data-meteo (.getMeteoInternal
                                                            proto)))
    (.hasLrf proto) (assoc :lrf (parse-jon-gui-data-lrf (.getLrf proto)))
    (.hasTime proto) (assoc :time (parse-jon-gui-data-time (.getTime proto)))
    (.hasGps proto) (assoc :gps (parse-jon-gui-data-gps (.getGps proto)))
    (.hasCompass proto) (assoc :compass
                          (parse-jon-gui-data-compass (.getCompass proto)))
    (.hasRotary proto) (assoc :rotary
                         (parse-jon-gui-data-rotary (.getRotary proto)))
    (.hasCameraDay proto)
      (assoc :camera-day (parse-jon-gui-data-camera-day (.getCameraDay proto)))
    (.hasCameraHeat proto) (assoc :camera-heat
                             (parse-jon-gui-data-camera-heat (.getCameraHeat
                                                               proto)))
    (.hasCompassCalibration proto) (assoc :compass-calibration
                                     (parse-jon-gui-data-compass-calibration
                                       (.getCompassCalibration proto)))
    (.hasRecOsd proto) (assoc :rec-osd
                         (parse-jon-gui-data-rec-osd (.getRecOsd proto)))
    (.hasDayCamGlassHeater proto) (assoc :day-cam-glass-heater
                                    (parse-jon-gui-data-day-cam-glass-heater
                                      (.getDayCamGlassHeater proto)))
    (.hasActualSpaceTime proto) (assoc :actual-space-time
                                  (parse-jon-gui-data-actual-space-time
                                    (.getActualSpaceTime proto)))))
;; =============================================================================
;; Validation Helper Functions
;; =============================================================================

;; Validation helpers for JonGuiDataTime
;; Warning: Could not extract spec for field timestamp

;; Warning: Could not extract spec for field manual-timestamp

;; Validation helpers for JonGuiDataSystem
;; Warning: Could not extract spec for field cpu-temperature

;; Warning: Could not extract spec for field gpu-temperature

;; Warning: Could not extract spec for field gpu-load

;; Warning: Could not extract spec for field cpu-load

;; Warning: Could not extract spec for field power-consumption

;; Warning: Could not extract spec for field loc

;; Warning: Could not extract spec for field cur-video-rec-dir-year

;; Warning: Could not extract spec for field cur-video-rec-dir-month

;; Warning: Could not extract spec for field cur-video-rec-dir-day

;; Warning: Could not extract spec for field cur-video-rec-dir-hour

;; Warning: Could not extract spec for field cur-video-rec-dir-minute

;; Warning: Could not extract spec for field cur-video-rec-dir-second

;; Warning: Could not extract spec for field disk-space

;; Validation helpers for JonGuiDataLrf
;; Warning: Could not extract spec for field measure-id

;; Warning: Could not extract spec for field pointer-mode

;; Validation helpers for JonGuiDataTarget
;; Warning: Could not extract spec for field timestamp

;; Warning: Could not extract spec for field target-longitude

;; Warning: Could not extract spec for field target-latitude

;; Warning: Could not extract spec for field observer-longitude

;; Warning: Could not extract spec for field observer-latitude

;; Warning: Could not extract spec for field observer-azimuth

;; Warning: Could not extract spec for field observer-elevation

;; Warning: Could not extract spec for field observer-bank

;; Warning: Could not extract spec for field distance-2d

;; Warning: Could not extract spec for field distance-3b

;; Warning: Could not extract spec for field observer-fix-type

;; Warning: Could not extract spec for field session-id

;; Warning: Could not extract spec for field target-id

;; Validation helpers for RgbColor
;; Warning: Could not extract spec for field red

;; Warning: Could not extract spec for field green

;; Warning: Could not extract spec for field blue

;; Validation helpers for JonGuiDataGps
;; Warning: Could not extract spec for field longitude

;; Warning: Could not extract spec for field latitude

;; Warning: Could not extract spec for field altitude

;; Warning: Could not extract spec for field manual-longitude

;; Warning: Could not extract spec for field manual-latitude

;; Warning: Could not extract spec for field manual-altitude

;; Warning: Could not extract spec for field fix-type

;; Validation helpers for JonGuiDataCompass
;; Warning: Could not extract spec for field azimuth

;; Warning: Could not extract spec for field elevation

;; Warning: Could not extract spec for field bank

;; Warning: Could not extract spec for field offset-azimuth

;; Warning: Could not extract spec for field offset-elevation

;; Warning: Could not extract spec for field magnetic-declination

;; Validation helpers for JonGuiDataCompassCalibration
;; Warning: Could not extract spec for field stage

;; Warning: Could not extract spec for field final-stage

;; Warning: Could not extract spec for field target-azimuth

;; Warning: Could not extract spec for field target-elevation

;; Warning: Could not extract spec for field target-bank

;; Warning: Could not extract spec for field status

;; Validation helpers for JonGuiDataRotary
;; Warning: Could not extract spec for field azimuth

;; Warning: Could not extract spec for field azimuth-speed

;; Warning: Could not extract spec for field elevation

;; Warning: Could not extract spec for field elevation-speed

;; Warning: Could not extract spec for field platform-azimuth

;; Warning: Could not extract spec for field platform-elevation

;; Warning: Could not extract spec for field platform-bank

;; Warning: Could not extract spec for field mode

;; Warning: Could not extract spec for field scan-target

;; Warning: Could not extract spec for field scan-target-max

;; Warning: Could not extract spec for field sun-azimuth

;; Warning: Could not extract spec for field sun-elevation

;; Validation helpers for ScanNode
;; Warning: Could not extract spec for field index

;; Warning: Could not extract spec for field day-zoom-table-value

;; Warning: Could not extract spec for field heat-zoom-table-value

;; Warning: Could not extract spec for field azimuth

;; Warning: Could not extract spec for field elevation

;; Warning: Could not extract spec for field linger

;; Warning: Could not extract spec for field speed

;; Validation helpers for JonGuiDataCameraDay
;; Warning: Could not extract spec for field focus-pos

;; Warning: Could not extract spec for field zoom-pos

;; Warning: Could not extract spec for field iris-pos

;; Warning: Could not extract spec for field zoom-table-pos

;; Warning: Could not extract spec for field zoom-table-pos-max

;; Warning: Could not extract spec for field fx-mode

;; Warning: Could not extract spec for field digital-zoom-level

;; Warning: Could not extract spec for field clahe-level

;; Validation helpers for JonGuiDataCameraHeat
;; Warning: Could not extract spec for field zoom-pos

;; Warning: Could not extract spec for field agc-mode

;; Warning: Could not extract spec for field filter

;; Warning: Could not extract spec for field zoom-table-pos

;; Warning: Could not extract spec for field zoom-table-pos-max

;; Warning: Could not extract spec for field dde-level

;; Warning: Could not extract spec for field fx-mode

;; Warning: Could not extract spec for field digital-zoom-level

;; Warning: Could not extract spec for field clahe-level

;; Validation helpers for JonGuiDataRecOsd
;; Warning: Could not extract spec for field screen

;; Validation helpers for JonGuiDataDayCamGlassHeater
;; Warning: Could not extract spec for field temperature

;; Validation helpers for JonGuiDataActualSpaceTime
;; Warning: Could not extract spec for field azimuth

;; Warning: Could not extract spec for field elevation

;; Warning: Could not extract spec for field bank

;; Warning: Could not extract spec for field latitude

;; Warning: Could not extract spec for field longitude

;; Warning: Could not extract spec for field altitude

;; Warning: Could not extract spec for field timestamp

;; Validation helpers for JonGUIState
;; Warning: Could not extract spec for field protocol-version