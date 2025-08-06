(ns test.proto.single.command
  "Generated protobuf functions."
  (:require [malli.core :as m])
  (:import cmd.CV.JonSharedCmdCv
           cmd.Compass.JonSharedCmdCompass
           cmd.DayCamGlassHeater.JonSharedCmdDayCamGlassHeater
           cmd.DayCamera.JonSharedCmdDayCamera
           cmd.Gps.JonSharedCmdGps
           cmd.HeatCamera.JonSharedCmdHeatCamera
           cmd.JonSharedCmd
           cmd.Lira.JonSharedCmdLira
           cmd.Lrf.JonSharedCmdLrf
           cmd.Lrf_calib.JonSharedCmdLrfAlign
           cmd.OSD.JonSharedCmdOsd
           cmd.RotaryPlatform.JonSharedCmdRotary
           cmd.System.JonSharedCmdSystem
           ser.JonSharedDataTypes))

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

(def root-spec
  "Malli spec for root message"
  [:map
   [:cmd
    [:altn
     {:calibrate-cencel [:map
                         [:calibrate-cencel :cmd.compass/calibrate-cencel]],
      :start [:map [:start :cmd.compass/start]],
      :set-offset-angle-elevation [:map
                                   [:set-offset-angle-elevation
                                    :cmd.compass/set-offset-angle-elevation]],
      :stop [:map [:stop :cmd.compass/stop]],
      :calibrate-next [:map [:calibrate-next :cmd.compass/calibrate-next]],
      :get-meteo [:map [:get-meteo :cmd.compass/get-meteo]],
      :set-use-rotary-position
        [:map [:set-use-rotary-position :cmd.compass/set-use-rotary-position]],
      :set-magnetic-declination [:map
                                 [:set-magnetic-declination
                                  :cmd.compass/set-magnetic-declination]],
      :start-calibrate-short
        [:map [:start-calibrate-short :cmd.compass/calibrate-start-short]],
      :start-calibrate-long
        [:map [:start-calibrate-long :cmd.compass/calibrate-start-long]],
      :set-offset-angle-azimuth [:map
                                 [:set-offset-angle-azimuth
                                  :cmd.compass/set-offset-angle-azimuth]]}]]])

(def start-spec "Malli spec for start message" [:map])

(def stop-spec "Malli spec for stop message" [:map])

(def next-spec "Malli spec for next message" [:map])

(def calibrate-start-long-spec
  "Malli spec for calibrate-start-long message"
  [:map])

(def calibrate-start-short-spec
  "Malli spec for calibrate-start-short message"
  [:map])

(def calibrate-next-spec "Malli spec for calibrate-next message" [:map])

(def calibrate-cencel-spec "Malli spec for calibrate-cencel message" [:map])

(def get-meteo-spec "Malli spec for get-meteo message" [:map])

(def set-magnetic-declination-spec
  "Malli spec for set-magnetic-declination message"
  [:map [:value [:maybe :float]]])

(def set-offset-angle-azimuth-spec
  "Malli spec for set-offset-angle-azimuth message"
  [:map [:value [:maybe :float]]])

(def set-offset-angle-elevation-spec
  "Malli spec for set-offset-angle-elevation message"
  [:map [:value [:maybe :float]]])

(def set-use-rotary-position-spec
  "Malli spec for set-use-rotary-position message"
  [:map [:flag [:maybe :boolean]]])

(def root-spec
  "Malli spec for root message"
  [:map
   [:cmd
    [:altn
     {:start [:map [:start :cmd.gps/start]],
      :stop [:map [:stop :cmd.gps/stop]],
      :set-manual-position
        [:map [:set-manual-position :cmd.gps/set-manual-position]],
      :set-use-manual-position
        [:map [:set-use-manual-position :cmd.gps/set-use-manual-position]],
      :get-meteo [:map [:get-meteo :cmd.gps/get-meteo]]}]]])

(def start-spec "Malli spec for start message" [:map])

(def stop-spec "Malli spec for stop message" [:map])

(def get-meteo-spec "Malli spec for get-meteo message" [:map])

(def set-use-manual-position-spec
  "Malli spec for set-use-manual-position message"
  [:map [:flag [:maybe :boolean]]])

(def set-manual-position-spec
  "Malli spec for set-manual-position message"
  [:map [:latitude [:maybe :float]] [:longitude [:maybe :float]]
   [:altitude [:maybe :float]]])

(def jon-gui-data-meteo-spec
  "Malli spec for jon-gui-data-meteo message"
  [:map [:temperature [:maybe :float]] [:humidity [:maybe :float]]
   [:pressure [:maybe :float]]])

(def root-spec
  "Malli spec for root message"
  [:map
   [:cmd
    [:altn
     {:target-designator-off
        [:map [:target-designator-off :cmd.lrf/target-designator-off]],
      :target-designator-on-mode-b [:map
                                    [:target-designator-on-mode-b
                                     :cmd.lrf/target-designator-on-mode-b]],
      :disable-fog-mode [:map [:disable-fog-mode :cmd.lrf/disable-fog-mode]],
      :set-scan-mode [:map [:set-scan-mode :cmd.lrf/set-scan-mode]],
      :refine-off [:map [:refine-off :cmd.lrf/refine-off]],
      :scan-off [:map [:scan-off :cmd.lrf/scan-off]],
      :refine-on [:map [:refine-on :cmd.lrf/refine-on]],
      :start [:map [:start :cmd.lrf/start]],
      :measure [:map [:measure :cmd.lrf/measure]],
      :scan-on [:map [:scan-on :cmd.lrf/scan-on]],
      :stop [:map [:stop :cmd.lrf/stop]],
      :new-session [:map [:new-session :cmd.lrf/new-session]],
      :get-meteo [:map [:get-meteo :cmd.lrf/get-meteo]],
      :enable-fog-mode [:map [:enable-fog-mode :cmd.lrf/enable-fog-mode]],
      :target-designator-on-mode-a [:map
                                    [:target-designator-on-mode-a
                                     :cmd.lrf/target-designator-on-mode-a]]}]]])

(def get-meteo-spec "Malli spec for get-meteo message" [:map])

(def start-spec "Malli spec for start message" [:map])

(def stop-spec "Malli spec for stop message" [:map])

(def measure-spec "Malli spec for measure message" [:map])

(def scan-on-spec "Malli spec for scan-on message" [:map])

(def scan-off-spec "Malli spec for scan-off message" [:map])

(def refine-off-spec "Malli spec for refine-off message" [:map])

(def refine-on-spec "Malli spec for refine-on message" [:map])

(def target-designator-off-spec
  "Malli spec for target-designator-off message"
  [:map])

(def target-designator-on-mode-a-spec
  "Malli spec for target-designator-on-mode-a message"
  [:map])

(def target-designator-on-mode-b-spec
  "Malli spec for target-designator-on-mode-b message"
  [:map])

(def enable-fog-mode-spec "Malli spec for enable-fog-mode message" [:map])

(def disable-fog-mode-spec "Malli spec for disable-fog-mode message" [:map])

(def set-scan-mode-spec
  "Malli spec for set-scan-mode message"
  [:map [:mode [:maybe :ser/jon-gui-data-lrf-scan-modes]]])

(def new-session-spec "Malli spec for new-session message" [:map])

(def set-value-spec
  "Malli spec for set-value message"
  [:map [:value [:maybe :float]]])

(def move-spec
  "Malli spec for move message"
  [:map [:target-value [:maybe :float]] [:speed [:maybe :float]]])

(def offset-spec
  "Malli spec for offset message"
  [:map [:offset-value [:maybe :float]]])

(def set-clahe-level-spec
  "Malli spec for set-clahe-level message"
  [:map [:value [:maybe :float]]])

(def shift-clahe-level-spec
  "Malli spec for shift-clahe-level message"
  [:map [:value [:maybe :float]]])

(def root-spec
  "Malli spec for root message"
  [:map
   [:cmd
    [:altn
     {:zoom [:map [:zoom :cmd.daycamera/zoom]],
      :set-infra-red-filter
        [:map [:set-infra-red-filter :cmd.daycamera/set-infra-red-filter]],
      :set-clahe-level [:map [:set-clahe-level :cmd.daycamera/set-clahe-level]],
      :prev-fx-mode [:map [:prev-fx-mode :cmd.daycamera/prev-fx-mode]],
      :start [:map [:start :cmd.daycamera/start]],
      :halt-all [:map [:halt-all :cmd.daycamera/halt-all]],
      :set-digital-zoom-level
        [:map [:set-digital-zoom-level :cmd.daycamera/set-digital-zoom-level]],
      :stop [:map [:stop :cmd.daycamera/stop]],
      :photo [:map [:photo :cmd.daycamera/photo]],
      :get-meteo [:map [:get-meteo :cmd.daycamera/get-meteo]],
      :focus [:map [:focus :cmd.daycamera/focus]],
      :set-fx-mode [:map [:set-fx-mode :cmd.daycamera/set-fx-mode]],
      :set-iris [:map [:set-iris :cmd.daycamera/set-iris]],
      :refresh-fx-mode [:map [:refresh-fx-mode :cmd.daycamera/refresh-fx-mode]],
      :set-auto-iris [:map [:set-auto-iris :cmd.daycamera/set-auto-iris]],
      :next-fx-mode [:map [:next-fx-mode :cmd.daycamera/next-fx-mode]],
      :shift-clahe-level
        [:map [:shift-clahe-level :cmd.daycamera/shift-clahe-level]]}]]])

(def get-pos-spec "Malli spec for get-pos message" [:map])

(def next-fx-mode-spec "Malli spec for next-fx-mode message" [:map])

(def prev-fx-mode-spec "Malli spec for prev-fx-mode message" [:map])

(def refresh-fx-mode-spec "Malli spec for refresh-fx-mode message" [:map])

(def halt-all-spec "Malli spec for halt-all message" [:map])

(def set-fx-mode-spec
  "Malli spec for set-fx-mode message"
  [:map [:mode [:maybe :ser/jon-gui-data-fx-mode-day]]])

(def set-digital-zoom-level-spec
  "Malli spec for set-digital-zoom-level message"
  [:map [:value [:maybe :float]]])

(def focus-spec
  "Malli spec for focus message"
  [:map
   [:cmd
    [:altn
     {:set-value [:map [:set-value :cmd.daycamera/set-value]],
      :move [:map [:move :cmd.daycamera/move]],
      :halt [:map [:halt :cmd.daycamera/halt]],
      :offset [:map [:offset :cmd.daycamera/offset]],
      :reset-focus [:map [:reset-focus :cmd.daycamera/reset-focus]],
      :save-to-table-focus
        [:map [:save-to-table-focus :cmd.daycamera/save-to-table-focus]]}]]])

(def zoom-spec
  "Malli spec for zoom message"
  [:map
   [:cmd
    [:altn
     {:prev-zoom-table-pos
        [:map [:prev-zoom-table-pos :cmd.daycamera/prev-zoom-table-pos]],
      :offset [:map [:offset :cmd.daycamera/offset]],
      :move [:map [:move :cmd.daycamera/move]],
      :reset-zoom [:map [:reset-zoom :cmd.daycamera/reset-zoom]],
      :next-zoom-table-pos
        [:map [:next-zoom-table-pos :cmd.daycamera/next-zoom-table-pos]],
      :set-value [:map [:set-value :cmd.daycamera/set-value]],
      :set-zoom-table-value
        [:map [:set-zoom-table-value :cmd.daycamera/set-zoom-table-value]],
      :halt [:map [:halt :cmd.daycamera/halt]],
      :save-to-table [:map [:save-to-table :cmd.daycamera/save-to-table]]}]]])

(def next-zoom-table-pos-spec
  "Malli spec for next-zoom-table-pos message"
  [:map])

(def prev-zoom-table-pos-spec
  "Malli spec for prev-zoom-table-pos message"
  [:map])

(def set-iris-spec
  "Malli spec for set-iris message"
  [:map [:value [:maybe :float]]])

(def set-infra-red-filter-spec
  "Malli spec for set-infra-red-filter message"
  [:map [:value [:maybe :boolean]]])

(def set-auto-iris-spec
  "Malli spec for set-auto-iris message"
  [:map [:value [:maybe :boolean]]])

(def set-zoom-table-value-spec
  "Malli spec for set-zoom-table-value message"
  [:map [:value [:maybe :int]]])

(def stop-spec "Malli spec for stop message" [:map])

(def start-spec "Malli spec for start message" [:map])

(def photo-spec "Malli spec for photo message" [:map])

(def halt-spec "Malli spec for halt message" [:map])

(def get-meteo-spec "Malli spec for get-meteo message" [:map])

(def reset-zoom-spec "Malli spec for reset-zoom message" [:map])

(def reset-focus-spec "Malli spec for reset-focus message" [:map])

(def save-to-table-spec "Malli spec for save-to-table message" [:map])

(def save-to-table-focus-spec
  "Malli spec for save-to-table-focus message"
  [:map])

(def root-spec
  "Malli spec for root message"
  [:map
   [:cmd
    [:altn
     {:set-dde-level [:map [:set-dde-level :cmd.heatcamera/set-dde-level]],
      :set-calib-mode [:map [:set-calib-mode :cmd.heatcamera/set-calib-mode]],
      :zoom [:map [:zoom :cmd.heatcamera/zoom]],
      :set-agc [:map [:set-agc :cmd.heatcamera/set-agc]],
      :shift-dde [:map [:shift-dde :cmd.heatcamera/shift-dde]],
      :set-filter [:map [:set-filter :cmd.heatcamera/set-filters]],
      :set-clahe-level [:map
                        [:set-clahe-level :cmd.heatcamera/set-clahe-level]],
      :disable-dde [:map [:disable-dde :cmd.heatcamera/disable-dde]],
      :prev-fx-mode [:map [:prev-fx-mode :cmd.heatcamera/prev-fx-mode]],
      :start [:map [:start :cmd.heatcamera/start]],
      :focus-step-minus [:map
                         [:focus-step-minus :cmd.heatcamera/focus-step-minus]],
      :set-digital-zoom-level
        [:map [:set-digital-zoom-level :cmd.heatcamera/set-digital-zoom-level]],
      :enable-dde [:map [:enable-dde :cmd.heatcamera/enable-dde]],
      :focus-stop [:map [:focus-stop :cmd.heatcamera/focus-stop]],
      :stop [:map [:stop :cmd.heatcamera/stop]],
      :reset-zoom [:map [:reset-zoom :cmd.heatcamera/reset-zoom]],
      :zoom-out [:map [:zoom-out :cmd.heatcamera/zoom-out]],
      :photo [:map [:photo :cmd.heatcamera/photo]],
      :zoom-in [:map [:zoom-in :cmd.heatcamera/zoom-in]],
      :get-meteo [:map [:get-meteo :cmd.heatcamera/get-meteo]],
      :focus-step-plus [:map
                        [:focus-step-plus :cmd.heatcamera/focus-step-plus]],
      :set-fx-mode [:map [:set-fx-mode :cmd.heatcamera/set-fx-mode]],
      :refresh-fx-mode [:map
                        [:refresh-fx-mode :cmd.heatcamera/refresh-fx-mode]],
      :focus-out [:map [:focus-out :cmd.heatcamera/focus-out]],
      :set-auto-focus [:map [:set-auto-focus :cmd.heatcamera/set-auto-focus]],
      :zoom-stop [:map [:zoom-stop :cmd.heatcamera/zoom-stop]],
      :save-to-table [:map [:save-to-table :cmd.heatcamera/save-to-table]],
      :next-fx-mode [:map [:next-fx-mode :cmd.heatcamera/next-fx-mode]],
      :calibrate [:map [:calibrate :cmd.heatcamera/calibrate]],
      :shift-clahe-level
        [:map [:shift-clahe-level :cmd.heatcamera/shift-clahe-level]],
      :focus-in [:map [:focus-in :cmd.heatcamera/focus-in]]}]]])

(def set-fx-mode-spec
  "Malli spec for set-fx-mode message"
  [:map [:mode [:maybe :ser/jon-gui-data-fx-mode-heat]]])

(def set-clahe-level-spec
  "Malli spec for set-clahe-level message"
  [:map [:value [:maybe :float]]])

(def shift-clahe-level-spec
  "Malli spec for shift-clahe-level message"
  [:map [:value [:maybe :float]]])

(def next-fx-mode-spec "Malli spec for next-fx-mode message" [:map])

(def prev-fx-mode-spec "Malli spec for prev-fx-mode message" [:map])

(def refresh-fx-mode-spec "Malli spec for refresh-fx-mode message" [:map])

(def enable-dde-spec "Malli spec for enable-dde message" [:map])

(def disable-dde-spec "Malli spec for disable-dde message" [:map])

(def set-value-spec
  "Malli spec for set-value message"
  [:map [:value [:maybe :float]]])

(def set-dde-level-spec
  "Malli spec for set-dde-level message"
  [:map [:value [:maybe :int]]])

(def set-digital-zoom-level-spec
  "Malli spec for set-digital-zoom-level message"
  [:map [:value [:maybe :float]]])

(def shift-dde-spec
  "Malli spec for shift-dde message"
  [:map [:value [:maybe :int]]])

(def zoom-in-spec "Malli spec for zoom-in message" [:map])

(def zoom-out-spec "Malli spec for zoom-out message" [:map])

(def zoom-stop-spec "Malli spec for zoom-stop message" [:map])

(def focus-in-spec "Malli spec for focus-in message" [:map])

(def focus-out-spec "Malli spec for focus-out message" [:map])

(def focus-stop-spec "Malli spec for focus-stop message" [:map])

(def focus-step-plus-spec "Malli spec for focus-step-plus message" [:map])

(def focus-step-minus-spec "Malli spec for focus-step-minus message" [:map])

(def calibrate-spec "Malli spec for calibrate message" [:map])

(def zoom-spec
  "Malli spec for zoom message"
  [:map
   [:cmd
    [:altn
     {:set-zoom-table-value
        [:map [:set-zoom-table-value :cmd.heatcamera/set-zoom-table-value]],
      :next-zoom-table-pos
        [:map [:next-zoom-table-pos :cmd.heatcamera/next-zoom-table-pos]],
      :prev-zoom-table-pos
        [:map [:prev-zoom-table-pos :cmd.heatcamera/prev-zoom-table-pos]]}]]])

(def next-zoom-table-pos-spec
  "Malli spec for next-zoom-table-pos message"
  [:map])

(def prev-zoom-table-pos-spec
  "Malli spec for prev-zoom-table-pos message"
  [:map])

(def set-calib-mode-spec "Malli spec for set-calib-mode message" [:map])

(def set-zoom-table-value-spec
  "Malli spec for set-zoom-table-value message"
  [:map [:value [:maybe :int]]])

(def set-agc-spec
  "Malli spec for set-agc message"
  [:map [:value [:maybe :ser/jon-gui-data-video-channel-heat-agc-modes]]])

(def set-filters-spec
  "Malli spec for set-filters message"
  [:map [:value [:maybe :ser/jon-gui-data-video-channel-heat-filters]]])

(def start-spec "Malli spec for start message" [:map])

(def stop-spec "Malli spec for stop message" [:map])

(def halt-spec "Malli spec for halt message" [:map])

(def photo-spec "Malli spec for photo message" [:map])

(def get-meteo-spec "Malli spec for get-meteo message" [:map])

(def set-auto-focus-spec
  "Malli spec for set-auto-focus message"
  [:map [:value [:maybe :boolean]]])

(def reset-zoom-spec "Malli spec for reset-zoom message" [:map])

(def save-to-table-spec "Malli spec for save-to-table message" [:map])

(def root-spec
  "Malli spec for root message"
  [:map
   [:cmd
    [:altn
     {:rotate-to-gps [:map [:rotate-to-gps :cmd.rotaryplatform/rotate-to-gps]],
      :scan-pause [:map [:scan-pause :cmd.rotaryplatform/scan-pause]],
      :rotate-to-ndc [:map [:rotate-to-ndc :cmd.rotaryplatform/rotate-to-ndc]],
      :scan-start [:map [:scan-start :cmd.rotaryplatform/scan-start]],
      :set-platform-azimuth
        [:map [:set-platform-azimuth :cmd.rotaryplatform/set-platform-azimuth]],
      :scan-stop [:map [:scan-stop :cmd.rotaryplatform/scan-stop]],
      :start [:map [:start :cmd.rotaryplatform/start]],
      :stop [:map [:stop :cmd.rotaryplatform/stop]],
      :set-origin-gps [:map
                       [:set-origin-gps :cmd.rotaryplatform/set-origin-gps]],
      :scan-next [:map [:scan-next :cmd.rotaryplatform/scan-next]],
      :set-platform-bank
        [:map [:set-platform-bank :cmd.rotaryplatform/set-platform-bank]],
      :get-meteo [:map [:get-meteo :cmd.rotaryplatform/get-meteo]],
      :set-use-rotary-as-compass
        [:map
         [:set-use-rotary-as-compass
          :cmd.rotaryplatform/set-use-rotary-as-compass]],
      :scan-prev [:map [:scan-prev :cmd.rotaryplatform/scan-prev]],
      :scan-add-node [:map [:scan-add-node :cmd.rotaryplatform/scan-add-node]],
      :set-platform-elevation [:map
                               [:set-platform-elevation
                                :cmd.rotaryplatform/set-platform-elevation]],
      :scan-select-node
        [:map [:scan-select-node :cmd.rotaryplatform/scan-select-node]],
      :halt [:map [:halt :cmd.rotaryplatform/halt]],
      :scan-delete-node
        [:map [:scan-delete-node :cmd.rotaryplatform/scan-delete-node]],
      :axis [:map [:axis :cmd.rotaryplatform/axis]],
      :scan-unpause [:map [:scan-unpause :cmd.rotaryplatform/scan-unpause]],
      :set-mode [:map [:set-mode :cmd.rotaryplatform/set-mode]],
      :scan-refresh-node-list [:map
                               [:scan-refresh-node-list
                                :cmd.rotaryplatform/scan-refresh-node-list]],
      :scan-update-node
        [:map [:scan-update-node :cmd.rotaryplatform/scan-update-node]]}]]])

(def axis-spec
  "Malli spec for axis message"
  [:map [:azimuth [:maybe :cmd.rotaryplatform/azimuth]]
   [:elevation [:maybe :cmd.rotaryplatform/elevation]]])

(def set-mode-spec
  "Malli spec for set-mode message"
  [:map [:mode [:maybe :ser/jon-gui-data-rotary-mode]]])

(def set-azimuth-value-spec
  "Malli spec for set-azimuth-value message"
  [:map [:value [:maybe :float]]
   [:direction [:maybe :ser/jon-gui-data-rotary-direction]]])

(def rotate-azimuth-to-spec
  "Malli spec for rotate-azimuth-to message"
  [:map [:target-value [:maybe :float]] [:speed [:maybe :float]]
   [:direction [:maybe :ser/jon-gui-data-rotary-direction]]])

(def rotate-azimuth-spec
  "Malli spec for rotate-azimuth message"
  [:map [:speed [:maybe :float]]
   [:direction [:maybe :ser/jon-gui-data-rotary-direction]]])

(def rotate-elevation-spec
  "Malli spec for rotate-elevation message"
  [:map [:speed [:maybe :float]]
   [:direction [:maybe :ser/jon-gui-data-rotary-direction]]])

(def set-elevation-value-spec
  "Malli spec for set-elevation-value message"
  [:map [:value [:maybe :float]]])

(def rotate-elevation-to-spec
  "Malli spec for rotate-elevation-to message"
  [:map [:target-value [:maybe :float]] [:speed [:maybe :float]]])

(def rotate-elevation-relative-spec
  "Malli spec for rotate-elevation-relative message"
  [:map [:value [:maybe :float]] [:speed [:maybe :float]]
   [:direction [:maybe :ser/jon-gui-data-rotary-direction]]])

(def rotate-elevation-relative-set-spec
  "Malli spec for rotate-elevation-relative-set message"
  [:map [:value [:maybe :float]]
   [:direction [:maybe :ser/jon-gui-data-rotary-direction]]])

(def rotate-azimuth-relative-spec
  "Malli spec for rotate-azimuth-relative message"
  [:map [:value [:maybe :float]] [:speed [:maybe :float]]
   [:direction [:maybe :ser/jon-gui-data-rotary-direction]]])

(def rotate-azimuth-relative-set-spec
  "Malli spec for rotate-azimuth-relative-set message"
  [:map [:value [:maybe :float]]
   [:direction [:maybe :ser/jon-gui-data-rotary-direction]]])

(def set-platform-azimuth-spec
  "Malli spec for set-platform-azimuth message"
  [:map [:value [:maybe :float]]])

(def set-platform-elevation-spec
  "Malli spec for set-platform-elevation message"
  [:map [:value [:maybe :float]]])

(def set-platform-bank-spec
  "Malli spec for set-platform-bank message"
  [:map [:value [:maybe :float]]])

(def get-meteo-spec "Malli spec for get-meteo message" [:map])

(def azimuth-spec
  "Malli spec for azimuth message"
  [:map
   [:cmd
    [:altn
     {:set-value [:map [:set-value :cmd.rotaryplatform/set-azimuth-value]],
      :rotate-to [:map [:rotate-to :cmd.rotaryplatform/rotate-azimuth-to]],
      :rotate [:map [:rotate :cmd.rotaryplatform/rotate-azimuth]],
      :relative [:map [:relative :cmd.rotaryplatform/rotate-azimuth-relative]],
      :relative-set
        [:map [:relative-set :cmd.rotaryplatform/rotate-azimuth-relative-set]],
      :halt [:map [:halt :cmd.rotaryplatform/halt-azimuth]]}]]])

(def start-spec "Malli spec for start message" [:map])

(def stop-spec "Malli spec for stop message" [:map])

(def halt-spec "Malli spec for halt message" [:map])

(def scan-start-spec "Malli spec for scan-start message" [:map])

(def scan-stop-spec "Malli spec for scan-stop message" [:map])

(def scan-pause-spec "Malli spec for scan-pause message" [:map])

(def scan-unpause-spec "Malli spec for scan-unpause message" [:map])

(def halt-azimuth-spec "Malli spec for halt-azimuth message" [:map])

(def halt-elevation-spec "Malli spec for halt-elevation message" [:map])

(def scan-prev-spec "Malli spec for scan-prev message" [:map])

(def scan-next-spec "Malli spec for scan-next message" [:map])

(def scan-refresh-node-list-spec
  "Malli spec for scan-refresh-node-list message"
  [:map])

(def scan-select-node-spec
  "Malli spec for scan-select-node message"
  [:map [:index [:maybe :int]]])

(def scan-delete-node-spec
  "Malli spec for scan-delete-node message"
  [:map [:index [:maybe :int]]])

(def scan-update-node-spec
  "Malli spec for scan-update-node message"
  [:map [:index [:maybe :int]] [:day-zoom-table-value [:maybe :int]]
   [:heat-zoom-table-value [:maybe :int]] [:azimuth [:maybe :double]]
   [:elevation [:maybe :double]] [:linger [:maybe :double]]
   [:speed [:maybe :double]]])

(def scan-add-node-spec
  "Malli spec for scan-add-node message"
  [:map [:index [:maybe :int]] [:day-zoom-table-value [:maybe :int]]
   [:heat-zoom-table-value [:maybe :int]] [:azimuth [:maybe :double]]
   [:elevation [:maybe :double]] [:linger [:maybe :double]]
   [:speed [:maybe :double]]])

(def elevation-spec
  "Malli spec for elevation message"
  [:map
   [:cmd
    [:altn
     {:set-value [:map [:set-value :cmd.rotaryplatform/set-elevation-value]],
      :rotate-to [:map [:rotate-to :cmd.rotaryplatform/rotate-elevation-to]],
      :rotate [:map [:rotate :cmd.rotaryplatform/rotate-elevation]],
      :relative [:map
                 [:relative :cmd.rotaryplatform/rotate-elevation-relative]],
      :relative-set [:map
                     [:relative-set
                      :cmd.rotaryplatform/rotate-elevation-relative-set]],
      :halt [:map [:halt :cmd.rotaryplatform/halt-elevation]]}]]])

(def set-use-rotary-as-compass-spec
  "Malli spec for set-use-rotary-as-compass message"
  [:map [:flag [:maybe :boolean]]])

(def rotate-to-gps-spec
  "Malli spec for rotate-to-gps message"
  [:map [:latitude [:maybe :float]] [:longitude [:maybe :float]]
   [:altitude [:maybe :float]]])

(def set-origin-gps-spec
  "Malli spec for set-origin-gps message"
  [:map [:latitude [:maybe :float]] [:longitude [:maybe :float]]
   [:altitude [:maybe :float]]])

(def rotate-to-ndc-spec
  "Malli spec for rotate-to-ndc message"
  [:map [:channel [:maybe :ser/jon-gui-data-video-channel]] [:x [:maybe :float]]
   [:y [:maybe :float]]])

(def root-spec
  "Malli spec for root message"
  [:map
   [:cmd
    [:altn
     {:show-default-screen
        [:map [:show-default-screen :cmd.osd/show-default-screen]],
      :show-lrf-measure-screen
        [:map [:show-lrf-measure-screen :cmd.osd/show-lrf-measure-screen]],
      :show-lrf-result-screen
        [:map [:show-lrf-result-screen :cmd.osd/show-lrf-result-screen]],
      :show-lrf-result-simplified-screen
        [:map
         [:show-lrf-result-simplified-screen
          :cmd.osd/show-lrf-result-simplified-screen]],
      :enable-heat-osd [:map [:enable-heat-osd :cmd.osd/enable-heat-osd]],
      :disable-heat-osd [:map [:disable-heat-osd :cmd.osd/disable-heat-osd]],
      :enable-day-osd [:map [:enable-day-osd :cmd.osd/enable-day-osd]],
      :disable-day-osd [:map [:disable-day-osd :cmd.osd/disable-day-osd]]}]]])

(def show-default-screen-spec
  "Malli spec for show-default-screen message"
  [:map])

(def show-lrf-measure-screen-spec
  "Malli spec for show-lrf-measure-screen message"
  [:map])

(def show-lrf-result-screen-spec
  "Malli spec for show-lrf-result-screen message"
  [:map])

(def show-lrf-result-simplified-screen-spec
  "Malli spec for show-lrf-result-simplified-screen message"
  [:map])

(def enable-heat-osd-spec "Malli spec for enable-heat-osd message" [:map])

(def disable-heat-osd-spec "Malli spec for disable-heat-osd message" [:map])

(def enable-day-osd-spec "Malli spec for enable-day-osd message" [:map])

(def disable-day-osd-spec "Malli spec for disable-day-osd message" [:map])

(def root-spec
  "Malli spec for root message"
  [:map
   [:channel
    [:altn
     {:day [:map [:day :cmd.lrf_calib/offsets]],
      :heat [:map [:heat :cmd.lrf_calib/offsets]]}]]])

(def offsets-spec
  "Malli spec for offsets message"
  [:map
   [:cmd
    [:altn
     {:set [:map [:set :cmd.lrf_calib/set-offsets]],
      :save [:map [:save :cmd.lrf_calib/save-offsets]],
      :reset [:map [:reset :cmd.lrf_calib/reset-offsets]],
      :shift [:map [:shift :cmd.lrf_calib/shift-offsets-by]]}]]])

(def set-offsets-spec
  "Malli spec for set-offsets message"
  [:map [:x [:maybe :int]] [:y [:maybe :int]]])

(def shift-offsets-by-spec
  "Malli spec for shift-offsets-by message"
  [:map [:x [:maybe :int]] [:y [:maybe :int]]])

(def reset-offsets-spec "Malli spec for reset-offsets message" [:map])

(def save-offsets-spec "Malli spec for save-offsets message" [:map])

(def root-spec
  "Malli spec for root message"
  [:map
   [:cmd
    [:altn
     {:geodesic-mode-disable
        [:map [:geodesic-mode-disable :cmd.system/disable-geodesic-mode]],
      :start-all [:map [:start-all :cmd.system/start-a-ll]],
      :geodesic-mode-enable
        [:map [:geodesic-mode-enable :cmd.system/enable-geodesic-mode]],
      :localization [:map [:localization :cmd.system/set-localization]],
      :unmark-rec-important
        [:map [:unmark-rec-important :cmd.system/unmark-rec-important]],
      :stop-rec [:map [:stop-rec :cmd.system/stop-rec]],
      :reboot [:map [:reboot :cmd.system/reboot]],
      :start-rec [:map [:start-rec :cmd.system/start-rec]],
      :power-off [:map [:power-off :cmd.system/power-off]],
      :reset-configs [:map [:reset-configs :cmd.system/reset-configs]],
      :stop-all [:map [:stop-all :cmd.system/stop-a-ll]],
      :enter-transport [:map [:enter-transport :cmd.system/enter-transport]],
      :mark-rec-important
        [:map [:mark-rec-important :cmd.system/mark-rec-important]]}]]])

(def start-a-ll-spec "Malli spec for start-a-ll message" [:map])

(def stop-a-ll-spec "Malli spec for stop-a-ll message" [:map])

(def reboot-spec "Malli spec for reboot message" [:map])

(def power-off-spec "Malli spec for power-off message" [:map])

(def reset-configs-spec "Malli spec for reset-configs message" [:map])

(def start-rec-spec "Malli spec for start-rec message" [:map])

(def stop-rec-spec "Malli spec for stop-rec message" [:map])

(def mark-rec-important-spec "Malli spec for mark-rec-important message" [:map])

(def unmark-rec-important-spec
  "Malli spec for unmark-rec-important message"
  [:map])

(def enter-transport-spec "Malli spec for enter-transport message" [:map])

(def enable-geodesic-mode-spec
  "Malli spec for enable-geodesic-mode message"
  [:map])

(def disable-geodesic-mode-spec
  "Malli spec for disable-geodesic-mode message"
  [:map])

(def set-localization-spec
  "Malli spec for set-localization message"
  [:map [:loc [:maybe :ser/jon-gui-data-system-localizations]]])

(def root-spec
  "Malli spec for root message"
  [:map
   [:cmd
    [:altn
     {:vampire-mode-enable [:map
                            [:vampire-mode-enable :cmd.cv/vampire-mode-enable]],
      :vampire-mode-disable
        [:map [:vampire-mode-disable :cmd.cv/vampire-mode-disable]],
      :dump-stop [:map [:dump-stop :cmd.cv/dump-stop]],
      :stabilization-mode-disable
        [:map [:stabilization-mode-disable :cmd.cv/stabilization-mode-disable]],
      :set-auto-focus [:map [:set-auto-focus :cmd.cv/set-auto-focus]],
      :start-track-ndc [:map [:start-track-ndc :cmd.cv/start-track-ndc]],
      :dump-start [:map [:dump-start :cmd.cv/dump-start]],
      :stop-track [:map [:stop-track :cmd.cv/stop-track]],
      :stabilization-mode-enable [:map
                                  [:stabilization-mode-enable
                                   :cmd.cv/stabilization-mode-enable]]}]]])

(def vampire-mode-enable-spec
  "Malli spec for vampire-mode-enable message"
  [:map])

(def dump-start-spec "Malli spec for dump-start message" [:map])

(def dump-stop-spec "Malli spec for dump-stop message" [:map])

(def vampire-mode-disable-spec
  "Malli spec for vampire-mode-disable message"
  [:map])

(def stabilization-mode-enable-spec
  "Malli spec for stabilization-mode-enable message"
  [:map])

(def stabilization-mode-disable-spec
  "Malli spec for stabilization-mode-disable message"
  [:map])

(def set-auto-focus-spec
  "Malli spec for set-auto-focus message"
  [:map [:channel [:maybe :ser/jon-gui-data-video-channel]]
   [:value [:maybe :boolean]]])

(def start-track-ndc-spec
  "Malli spec for start-track-ndc message"
  [:map [:channel [:maybe :ser/jon-gui-data-video-channel]] [:x [:maybe :float]]
   [:y [:maybe :float]] [:frame-time [:maybe :int]]])

(def stop-track-spec "Malli spec for stop-track message" [:map])

(def root-spec
  "Malli spec for root message"
  [:map
   [:cmd
    [:altn
     {:start [:map [:start :cmd.daycamglassheater/start]],
      :stop [:map [:stop :cmd.daycamglassheater/stop]],
      :turn-on [:map [:turn-on :cmd.daycamglassheater/turn-on]],
      :turn-off [:map [:turn-off :cmd.daycamglassheater/turn-off]],
      :get-meteo [:map [:get-meteo :cmd.daycamglassheater/get-meteo]]}]]])

(def start-spec "Malli spec for start message" [:map])

(def stop-spec "Malli spec for stop message" [:map])

(def turn-on-spec "Malli spec for turn-on message" [:map])

(def turn-off-spec "Malli spec for turn-off message" [:map])

(def get-meteo-spec "Malli spec for get-meteo message" [:map])

(def root-spec
  "Malli spec for root message"
  [:map
   [:cmd
    [:altn {:refine-target [:map [:refine-target :cmd.lira/refine-target]]}]]])

(def refine-target-spec
  "Malli spec for refine-target message"
  [:map [:target [:maybe :cmd.lira/jon-gui-data-lira-target]]])

(def jon-gui-data-lira-target-spec
  "Malli spec for jon-gui-data-lira-target message"
  [:map [:timestamp [:maybe :int]] [:target-longitude [:maybe :double]]
   [:target-latitude [:maybe :double]] [:target-altitude [:maybe :double]]
   [:target-azimuth [:maybe :double]] [:target-elevation [:maybe :double]]
   [:distance [:maybe :double]] [:uuid-part-1 [:maybe :int]]
   [:uuid-part-2 [:maybe :int]] [:uuid-part-3 [:maybe :int]]
   [:uuid-part-4 [:maybe :int]]])

(def root-spec
  "Malli spec for root message"
  [:map [:protocol-version [:maybe :int]] [:session-id [:maybe :int]]
   [:important [:maybe :boolean]] [:from-cv-subsystem [:maybe :boolean]]
   [:client-type [:maybe :ser/jon-gui-data-client-type]]
   [:payload
    [:altn
     {:osd [:map [:osd :cmd.osd/root]],
      :ping [:map [:ping :cmd/ping]],
      :system [:map [:system :cmd.system/root]],
      :noop [:map [:noop :cmd/noop]],
      :cv [:map [:cv :cmd.cv/root]],
      :gps [:map [:gps :cmd.gps/root]],
      :lrf [:map [:lrf :cmd.lrf/root]],
      :day-cam-glass-heater
        [:map [:day-cam-glass-heater :cmd.daycamglassheater/root]],
      :day-camera [:map [:day-camera :cmd.daycamera/root]],
      :heat-camera [:map [:heat-camera :cmd.heatcamera/root]],
      :lira [:map [:lira :cmd.lira/root]],
      :lrf-calib [:map [:lrf-calib :cmd.lrf_calib/root]],
      :rotary [:map [:rotary :cmd.rotaryplatform/root]],
      :compass [:map [:compass :cmd.compass/root]],
      :frozen [:map [:frozen :cmd/frozen]]}]]])

(def ping-spec "Malli spec for ping message" [:map])

(def noop-spec "Malli spec for noop message" [:map])

(def frozen-spec "Malli spec for frozen message" [:map])

;; =============================================================================
;; Builders and Parsers
;; =============================================================================

;; Forward declarations
(declare build-root)
(declare build-start)
(declare build-stop)
(declare build-next)
(declare build-calibrate-start-long)
(declare build-calibrate-start-short)
(declare build-calibrate-next)
(declare build-calibrate-cencel)
(declare build-get-meteo)
(declare build-set-magnetic-declination)
(declare build-set-offset-angle-azimuth)
(declare build-set-offset-angle-elevation)
(declare build-set-use-rotary-position)
(declare build-root)
(declare build-start)
(declare build-stop)
(declare build-get-meteo)
(declare build-set-use-manual-position)
(declare build-set-manual-position)
(declare build-jon-gui-data-meteo)
(declare build-root)
(declare build-get-meteo)
(declare build-start)
(declare build-stop)
(declare build-measure)
(declare build-scan-on)
(declare build-scan-off)
(declare build-refine-off)
(declare build-refine-on)
(declare build-target-designator-off)
(declare build-target-designator-on-mode-a)
(declare build-target-designator-on-mode-b)
(declare build-enable-fog-mode)
(declare build-disable-fog-mode)
(declare build-set-scan-mode)
(declare build-new-session)
(declare build-set-value)
(declare build-move)
(declare build-offset)
(declare build-set-clahe-level)
(declare build-shift-clahe-level)
(declare build-root)
(declare build-get-pos)
(declare build-next-fx-mode)
(declare build-prev-fx-mode)
(declare build-refresh-fx-mode)
(declare build-halt-all)
(declare build-set-fx-mode)
(declare build-set-digital-zoom-level)
(declare build-focus)
(declare build-zoom)
(declare build-next-zoom-table-pos)
(declare build-prev-zoom-table-pos)
(declare build-set-iris)
(declare build-set-infra-red-filter)
(declare build-set-auto-iris)
(declare build-set-zoom-table-value)
(declare build-stop)
(declare build-start)
(declare build-photo)
(declare build-halt)
(declare build-get-meteo)
(declare build-reset-zoom)
(declare build-reset-focus)
(declare build-save-to-table)
(declare build-save-to-table-focus)
(declare build-root)
(declare build-set-fx-mode)
(declare build-set-clahe-level)
(declare build-shift-clahe-level)
(declare build-next-fx-mode)
(declare build-prev-fx-mode)
(declare build-refresh-fx-mode)
(declare build-enable-dde)
(declare build-disable-dde)
(declare build-set-value)
(declare build-set-dde-level)
(declare build-set-digital-zoom-level)
(declare build-shift-dde)
(declare build-zoom-in)
(declare build-zoom-out)
(declare build-zoom-stop)
(declare build-focus-in)
(declare build-focus-out)
(declare build-focus-stop)
(declare build-focus-step-plus)
(declare build-focus-step-minus)
(declare build-calibrate)
(declare build-zoom)
(declare build-next-zoom-table-pos)
(declare build-prev-zoom-table-pos)
(declare build-set-calib-mode)
(declare build-set-zoom-table-value)
(declare build-set-agc)
(declare build-set-filters)
(declare build-start)
(declare build-stop)
(declare build-halt)
(declare build-photo)
(declare build-get-meteo)
(declare build-set-auto-focus)
(declare build-reset-zoom)
(declare build-save-to-table)
(declare build-root)
(declare build-axis)
(declare build-set-mode)
(declare build-set-azimuth-value)
(declare build-rotate-azimuth-to)
(declare build-rotate-azimuth)
(declare build-rotate-elevation)
(declare build-set-elevation-value)
(declare build-rotate-elevation-to)
(declare build-rotate-elevation-relative)
(declare build-rotate-elevation-relative-set)
(declare build-rotate-azimuth-relative)
(declare build-rotate-azimuth-relative-set)
(declare build-set-platform-azimuth)
(declare build-set-platform-elevation)
(declare build-set-platform-bank)
(declare build-get-meteo)
(declare build-azimuth)
(declare build-start)
(declare build-stop)
(declare build-halt)
(declare build-scan-start)
(declare build-scan-stop)
(declare build-scan-pause)
(declare build-scan-unpause)
(declare build-halt-azimuth)
(declare build-halt-elevation)
(declare build-scan-prev)
(declare build-scan-next)
(declare build-scan-refresh-node-list)
(declare build-scan-select-node)
(declare build-scan-delete-node)
(declare build-scan-update-node)
(declare build-scan-add-node)
(declare build-elevation)
(declare build-set-use-rotary-as-compass)
(declare build-rotate-to-gps)
(declare build-set-origin-gps)
(declare build-rotate-to-ndc)
(declare build-root)
(declare build-show-default-screen)
(declare build-show-lrf-measure-screen)
(declare build-show-lrf-result-screen)
(declare build-show-lrf-result-simplified-screen)
(declare build-enable-heat-osd)
(declare build-disable-heat-osd)
(declare build-enable-day-osd)
(declare build-disable-day-osd)
(declare build-root)
(declare build-offsets)
(declare build-set-offsets)
(declare build-shift-offsets-by)
(declare build-reset-offsets)
(declare build-save-offsets)
(declare build-root)
(declare build-start-a-ll)
(declare build-stop-a-ll)
(declare build-reboot)
(declare build-power-off)
(declare build-reset-configs)
(declare build-start-rec)
(declare build-stop-rec)
(declare build-mark-rec-important)
(declare build-unmark-rec-important)
(declare build-enter-transport)
(declare build-enable-geodesic-mode)
(declare build-disable-geodesic-mode)
(declare build-set-localization)
(declare build-root)
(declare build-vampire-mode-enable)
(declare build-dump-start)
(declare build-dump-stop)
(declare build-vampire-mode-disable)
(declare build-stabilization-mode-enable)
(declare build-stabilization-mode-disable)
(declare build-set-auto-focus)
(declare build-start-track-ndc)
(declare build-stop-track)
(declare build-root)
(declare build-start)
(declare build-stop)
(declare build-turn-on)
(declare build-turn-off)
(declare build-get-meteo)
(declare build-root)
(declare build-refine-target)
(declare build-jon-gui-data-lira-target)
(declare build-root)
(declare build-ping)
(declare build-noop)
(declare build-frozen)
(declare parse-root)
(declare parse-start)
(declare parse-stop)
(declare parse-next)
(declare parse-calibrate-start-long)
(declare parse-calibrate-start-short)
(declare parse-calibrate-next)
(declare parse-calibrate-cencel)
(declare parse-get-meteo)
(declare parse-set-magnetic-declination)
(declare parse-set-offset-angle-azimuth)
(declare parse-set-offset-angle-elevation)
(declare parse-set-use-rotary-position)
(declare parse-root)
(declare parse-start)
(declare parse-stop)
(declare parse-get-meteo)
(declare parse-set-use-manual-position)
(declare parse-set-manual-position)
(declare parse-jon-gui-data-meteo)
(declare parse-root)
(declare parse-get-meteo)
(declare parse-start)
(declare parse-stop)
(declare parse-measure)
(declare parse-scan-on)
(declare parse-scan-off)
(declare parse-refine-off)
(declare parse-refine-on)
(declare parse-target-designator-off)
(declare parse-target-designator-on-mode-a)
(declare parse-target-designator-on-mode-b)
(declare parse-enable-fog-mode)
(declare parse-disable-fog-mode)
(declare parse-set-scan-mode)
(declare parse-new-session)
(declare parse-set-value)
(declare parse-move)
(declare parse-offset)
(declare parse-set-clahe-level)
(declare parse-shift-clahe-level)
(declare parse-root)
(declare parse-get-pos)
(declare parse-next-fx-mode)
(declare parse-prev-fx-mode)
(declare parse-refresh-fx-mode)
(declare parse-halt-all)
(declare parse-set-fx-mode)
(declare parse-set-digital-zoom-level)
(declare parse-focus)
(declare parse-zoom)
(declare parse-next-zoom-table-pos)
(declare parse-prev-zoom-table-pos)
(declare parse-set-iris)
(declare parse-set-infra-red-filter)
(declare parse-set-auto-iris)
(declare parse-set-zoom-table-value)
(declare parse-stop)
(declare parse-start)
(declare parse-photo)
(declare parse-halt)
(declare parse-get-meteo)
(declare parse-reset-zoom)
(declare parse-reset-focus)
(declare parse-save-to-table)
(declare parse-save-to-table-focus)
(declare parse-root)
(declare parse-set-fx-mode)
(declare parse-set-clahe-level)
(declare parse-shift-clahe-level)
(declare parse-next-fx-mode)
(declare parse-prev-fx-mode)
(declare parse-refresh-fx-mode)
(declare parse-enable-dde)
(declare parse-disable-dde)
(declare parse-set-value)
(declare parse-set-dde-level)
(declare parse-set-digital-zoom-level)
(declare parse-shift-dde)
(declare parse-zoom-in)
(declare parse-zoom-out)
(declare parse-zoom-stop)
(declare parse-focus-in)
(declare parse-focus-out)
(declare parse-focus-stop)
(declare parse-focus-step-plus)
(declare parse-focus-step-minus)
(declare parse-calibrate)
(declare parse-zoom)
(declare parse-next-zoom-table-pos)
(declare parse-prev-zoom-table-pos)
(declare parse-set-calib-mode)
(declare parse-set-zoom-table-value)
(declare parse-set-agc)
(declare parse-set-filters)
(declare parse-start)
(declare parse-stop)
(declare parse-halt)
(declare parse-photo)
(declare parse-get-meteo)
(declare parse-set-auto-focus)
(declare parse-reset-zoom)
(declare parse-save-to-table)
(declare parse-root)
(declare parse-axis)
(declare parse-set-mode)
(declare parse-set-azimuth-value)
(declare parse-rotate-azimuth-to)
(declare parse-rotate-azimuth)
(declare parse-rotate-elevation)
(declare parse-set-elevation-value)
(declare parse-rotate-elevation-to)
(declare parse-rotate-elevation-relative)
(declare parse-rotate-elevation-relative-set)
(declare parse-rotate-azimuth-relative)
(declare parse-rotate-azimuth-relative-set)
(declare parse-set-platform-azimuth)
(declare parse-set-platform-elevation)
(declare parse-set-platform-bank)
(declare parse-get-meteo)
(declare parse-azimuth)
(declare parse-start)
(declare parse-stop)
(declare parse-halt)
(declare parse-scan-start)
(declare parse-scan-stop)
(declare parse-scan-pause)
(declare parse-scan-unpause)
(declare parse-halt-azimuth)
(declare parse-halt-elevation)
(declare parse-scan-prev)
(declare parse-scan-next)
(declare parse-scan-refresh-node-list)
(declare parse-scan-select-node)
(declare parse-scan-delete-node)
(declare parse-scan-update-node)
(declare parse-scan-add-node)
(declare parse-elevation)
(declare parse-set-use-rotary-as-compass)
(declare parse-rotate-to-gps)
(declare parse-set-origin-gps)
(declare parse-rotate-to-ndc)
(declare parse-root)
(declare parse-show-default-screen)
(declare parse-show-lrf-measure-screen)
(declare parse-show-lrf-result-screen)
(declare parse-show-lrf-result-simplified-screen)
(declare parse-enable-heat-osd)
(declare parse-disable-heat-osd)
(declare parse-enable-day-osd)
(declare parse-disable-day-osd)
(declare parse-root)
(declare parse-offsets)
(declare parse-set-offsets)
(declare parse-shift-offsets-by)
(declare parse-reset-offsets)
(declare parse-save-offsets)
(declare parse-root)
(declare parse-start-a-ll)
(declare parse-stop-a-ll)
(declare parse-reboot)
(declare parse-power-off)
(declare parse-reset-configs)
(declare parse-start-rec)
(declare parse-stop-rec)
(declare parse-mark-rec-important)
(declare parse-unmark-rec-important)
(declare parse-enter-transport)
(declare parse-enable-geodesic-mode)
(declare parse-disable-geodesic-mode)
(declare parse-set-localization)
(declare parse-root)
(declare parse-vampire-mode-enable)
(declare parse-dump-start)
(declare parse-dump-stop)
(declare parse-vampire-mode-disable)
(declare parse-stabilization-mode-enable)
(declare parse-stabilization-mode-disable)
(declare parse-set-auto-focus)
(declare parse-start-track-ndc)
(declare parse-stop-track)
(declare parse-root)
(declare parse-start)
(declare parse-stop)
(declare parse-turn-on)
(declare parse-turn-off)
(declare parse-get-meteo)
(declare parse-root)
(declare parse-refine-target)
(declare parse-jon-gui-data-lira-target)
(declare parse-root)
(declare parse-ping)
(declare parse-noop)
(declare parse-frozen)
(declare build-root-payload)
(declare build-root-payload)
(declare build-root-payload)
(declare build-root-payload)
(declare build-focus-payload)
(declare build-zoom-payload)
(declare build-root-payload)
(declare build-zoom-payload)
(declare build-root-payload)
(declare build-azimuth-payload)
(declare build-elevation-payload)
(declare build-root-payload)
(declare build-root-payload)
(declare build-offsets-payload)
(declare build-root-payload)
(declare build-root-payload)
(declare build-root-payload)
(declare build-root-payload)
(declare build-root-payload)
(declare parse-root-payload)
(declare parse-root-payload)
(declare parse-root-payload)
(declare parse-root-payload)
(declare parse-focus-payload)
(declare parse-zoom-payload)
(declare parse-root-payload)
(declare parse-zoom-payload)
(declare parse-root-payload)
(declare parse-azimuth-payload)
(declare parse-elevation-payload)
(declare parse-root-payload)
(declare parse-root-payload)
(declare parse-offsets-payload)
(declare parse-root-payload)
(declare parse-root-payload)
(declare parse-root-payload)
(declare parse-root-payload)
(declare parse-root-payload)

(defn build-root
  "Build a Root protobuf message from a map."
  [m]
  (let [builder (cmd.Compass.JonSharedCmdCompass$Root/newBuilder)]
    ;; Handle oneof: cmd
    (when-let [cmd-field (first
                           (filter (fn [[k v]]
                                     (#{:start :stop :set-magnetic-declination
                                        :set-offset-angle-azimuth
                                        :set-offset-angle-elevation
                                        :set-use-rotary-position
                                        :start-calibrate-long
                                        :start-calibrate-short :calibrate-next
                                        :calibrate-cencel :get-meteo}
                                      k))
                             m))]
      (build-root-payload builder cmd-field))
    (.build builder)))

(defn build-start
  "Build a Start protobuf message from a map."
  [m]
  (let [builder (cmd.Compass.JonSharedCmdCompass$Start/newBuilder)]
    (.build builder)))

(defn build-stop
  "Build a Stop protobuf message from a map."
  [m]
  (let [builder (cmd.Compass.JonSharedCmdCompass$Stop/newBuilder)]
    (.build builder)))

(defn build-next
  "Build a Next protobuf message from a map."
  [m]
  (let [builder (cmd.Compass.JonSharedCmdCompass$Next/newBuilder)]
    (.build builder)))

(defn build-calibrate-start-long
  "Build a CalibrateStartLong protobuf message from a map."
  [m]
  (let [builder (cmd.Compass.JonSharedCmdCompass$CalibrateStartLong/newBuilder)]
    (.build builder)))

(defn build-calibrate-start-short
  "Build a CalibrateStartShort protobuf message from a map."
  [m]
  (let [builder
          (cmd.Compass.JonSharedCmdCompass$CalibrateStartShort/newBuilder)]
    (.build builder)))

(defn build-calibrate-next
  "Build a CalibrateNext protobuf message from a map."
  [m]
  (let [builder (cmd.Compass.JonSharedCmdCompass$CalibrateNext/newBuilder)]
    (.build builder)))

(defn build-calibrate-cencel
  "Build a CalibrateCencel protobuf message from a map."
  [m]
  (let [builder (cmd.Compass.JonSharedCmdCompass$CalibrateCencel/newBuilder)]
    (.build builder)))

(defn build-get-meteo
  "Build a GetMeteo protobuf message from a map."
  [m]
  (let [builder (cmd.Compass.JonSharedCmdCompass$GetMeteo/newBuilder)]
    (.build builder)))

(defn build-set-magnetic-declination
  "Build a SetMagneticDeclination protobuf message from a map."
  [m]
  (let [builder
          (cmd.Compass.JonSharedCmdCompass$SetMagneticDeclination/newBuilder)]
    ;; Set regular fields
    (when (contains? m :value) (.setValue builder (get m :value)))
    (.build builder)))

(defn build-set-offset-angle-azimuth
  "Build a SetOffsetAngleAzimuth protobuf message from a map."
  [m]
  (let [builder
          (cmd.Compass.JonSharedCmdCompass$SetOffsetAngleAzimuth/newBuilder)]
    ;; Set regular fields
    (when (contains? m :value) (.setValue builder (get m :value)))
    (.build builder)))

(defn build-set-offset-angle-elevation
  "Build a SetOffsetAngleElevation protobuf message from a map."
  [m]
  (let [builder
          (cmd.Compass.JonSharedCmdCompass$SetOffsetAngleElevation/newBuilder)]
    ;; Set regular fields
    (when (contains? m :value) (.setValue builder (get m :value)))
    (.build builder)))

(defn build-set-use-rotary-position
  "Build a SetUseRotaryPosition protobuf message from a map."
  [m]
  (let [builder
          (cmd.Compass.JonSharedCmdCompass$SetUseRotaryPosition/newBuilder)]
    ;; Set regular fields
    (when (contains? m :flag) (.setFlag builder (get m :flag)))
    (.build builder)))

(defn build-root
  "Build a Root protobuf message from a map."
  [m]
  (let [builder (cmd.Gps.JonSharedCmdGps$Root/newBuilder)]
    ;; Handle oneof: cmd
    (when-let [cmd-field (first (filter (fn [[k v]]
                                          (#{:start :stop :set-manual-position
                                             :set-use-manual-position
                                             :get-meteo}
                                           k))
                                  m))]
      (build-root-payload builder cmd-field))
    (.build builder)))

(defn build-start
  "Build a Start protobuf message from a map."
  [m]
  (let [builder (cmd.Gps.JonSharedCmdGps$Start/newBuilder)] (.build builder)))

(defn build-stop
  "Build a Stop protobuf message from a map."
  [m]
  (let [builder (cmd.Gps.JonSharedCmdGps$Stop/newBuilder)] (.build builder)))

(defn build-get-meteo
  "Build a GetMeteo protobuf message from a map."
  [m]
  (let [builder (cmd.Gps.JonSharedCmdGps$GetMeteo/newBuilder)]
    (.build builder)))

(defn build-set-use-manual-position
  "Build a SetUseManualPosition protobuf message from a map."
  [m]
  (let [builder (cmd.Gps.JonSharedCmdGps$SetUseManualPosition/newBuilder)]
    ;; Set regular fields
    (when (contains? m :flag) (.setFlag builder (get m :flag)))
    (.build builder)))

(defn build-set-manual-position
  "Build a SetManualPosition protobuf message from a map."
  [m]
  (let [builder (cmd.Gps.JonSharedCmdGps$SetManualPosition/newBuilder)]
    ;; Set regular fields
    (when (contains? m :latitude) (.setLatitude builder (get m :latitude)))
    (when (contains? m :longitude) (.setLongitude builder (get m :longitude)))
    (when (contains? m :altitude) (.setAltitude builder (get m :altitude)))
    (.build builder)))

(defn build-jon-gui-data-meteo
  "Build a JonGuiDataMeteo protobuf message from a map."
  [m]
  (let [builder (ser.JonSharedDataTypes$JonGuiDataMeteo/newBuilder)]
    ;; Set regular fields
    (when (contains? m :temperature)
      (.setTemperature builder (get m :temperature)))
    (when (contains? m :humidity) (.setHumidity builder (get m :humidity)))
    (when (contains? m :pressure) (.setPressure builder (get m :pressure)))
    (.build builder)))

(defn build-root
  "Build a Root protobuf message from a map."
  [m]
  (let [builder (cmd.Lrf.JonSharedCmdLrf$Root/newBuilder)]
    ;; Handle oneof: cmd
    (when-let [cmd-field (first (filter (fn [[k v]]
                                          (#{:measure :scan-on :scan-off :start
                                             :stop :target-designator-off
                                             :target-designator-on-mode-a
                                             :target-designator-on-mode-b
                                             :enable-fog-mode :disable-fog-mode
                                             :set-scan-mode :new-session
                                             :get-meteo :refine-on :refine-off}
                                           k))
                                  m))]
      (build-root-payload builder cmd-field))
    (.build builder)))

(defn build-get-meteo
  "Build a GetMeteo protobuf message from a map."
  [m]
  (let [builder (cmd.Lrf.JonSharedCmdLrf$GetMeteo/newBuilder)]
    (.build builder)))

(defn build-start
  "Build a Start protobuf message from a map."
  [m]
  (let [builder (cmd.Lrf.JonSharedCmdLrf$Start/newBuilder)] (.build builder)))

(defn build-stop
  "Build a Stop protobuf message from a map."
  [m]
  (let [builder (cmd.Lrf.JonSharedCmdLrf$Stop/newBuilder)] (.build builder)))

(defn build-measure
  "Build a Measure protobuf message from a map."
  [m]
  (let [builder (cmd.Lrf.JonSharedCmdLrf$Measure/newBuilder)] (.build builder)))

(defn build-scan-on
  "Build a ScanOn protobuf message from a map."
  [m]
  (let [builder (cmd.Lrf.JonSharedCmdLrf$ScanOn/newBuilder)] (.build builder)))

(defn build-scan-off
  "Build a ScanOff protobuf message from a map."
  [m]
  (let [builder (cmd.Lrf.JonSharedCmdLrf$ScanOff/newBuilder)] (.build builder)))

(defn build-refine-off
  "Build a RefineOff protobuf message from a map."
  [m]
  (let [builder (cmd.Lrf.JonSharedCmdLrf$RefineOff/newBuilder)]
    (.build builder)))

(defn build-refine-on
  "Build a RefineOn protobuf message from a map."
  [m]
  (let [builder (cmd.Lrf.JonSharedCmdLrf$RefineOn/newBuilder)]
    (.build builder)))

(defn build-target-designator-off
  "Build a TargetDesignatorOff protobuf message from a map."
  [m]
  (let [builder (cmd.Lrf.JonSharedCmdLrf$TargetDesignatorOff/newBuilder)]
    (.build builder)))

(defn build-target-designator-on-mode-a
  "Build a TargetDesignatorOnModeA protobuf message from a map."
  [m]
  (let [builder (cmd.Lrf.JonSharedCmdLrf$TargetDesignatorOnModeA/newBuilder)]
    (.build builder)))

(defn build-target-designator-on-mode-b
  "Build a TargetDesignatorOnModeB protobuf message from a map."
  [m]
  (let [builder (cmd.Lrf.JonSharedCmdLrf$TargetDesignatorOnModeB/newBuilder)]
    (.build builder)))

(defn build-enable-fog-mode
  "Build a EnableFogMode protobuf message from a map."
  [m]
  (let [builder (cmd.Lrf.JonSharedCmdLrf$EnableFogMode/newBuilder)]
    (.build builder)))

(defn build-disable-fog-mode
  "Build a DisableFogMode protobuf message from a map."
  [m]
  (let [builder (cmd.Lrf.JonSharedCmdLrf$DisableFogMode/newBuilder)]
    (.build builder)))

(defn build-set-scan-mode
  "Build a SetScanMode protobuf message from a map."
  [m]
  (let [builder (cmd.Lrf.JonSharedCmdLrf$SetScanMode/newBuilder)]
    ;; Set regular fields
    (when (contains? m :mode)
      (.setMode builder
                (get types/jon-gui-data-lrf-scan-modes-values (get m :mode))))
    (.build builder)))

(defn build-new-session
  "Build a NewSession protobuf message from a map."
  [m]
  (let [builder (cmd.Lrf.JonSharedCmdLrf$NewSession/newBuilder)]
    (.build builder)))

(defn build-set-value
  "Build a SetValue protobuf message from a map."
  [m]
  (let [builder (cmd.DayCamera.JonSharedCmdDayCamera$SetValue/newBuilder)]
    ;; Set regular fields
    (when (contains? m :value) (.setValue builder (get m :value)))
    (.build builder)))

(defn build-move
  "Build a Move protobuf message from a map."
  [m]
  (let [builder (cmd.DayCamera.JonSharedCmdDayCamera$Move/newBuilder)]
    ;; Set regular fields
    (when (contains? m :target-value)
      (.setTargetValue builder (get m :target-value)))
    (when (contains? m :speed) (.setSpeed builder (get m :speed)))
    (.build builder)))

(defn build-offset
  "Build a Offset protobuf message from a map."
  [m]
  (let [builder (cmd.DayCamera.JonSharedCmdDayCamera$Offset/newBuilder)]
    ;; Set regular fields
    (when (contains? m :offset-value)
      (.setOffsetValue builder (get m :offset-value)))
    (.build builder)))

(defn build-set-clahe-level
  "Build a SetClaheLevel protobuf message from a map."
  [m]
  (let [builder (cmd.DayCamera.JonSharedCmdDayCamera$SetClaheLevel/newBuilder)]
    ;; Set regular fields
    (when (contains? m :value) (.setValue builder (get m :value)))
    (.build builder)))

(defn build-shift-clahe-level
  "Build a ShiftClaheLevel protobuf message from a map."
  [m]
  (let [builder
          (cmd.DayCamera.JonSharedCmdDayCamera$ShiftClaheLevel/newBuilder)]
    ;; Set regular fields
    (when (contains? m :value) (.setValue builder (get m :value)))
    (.build builder)))

(defn build-root
  "Build a Root protobuf message from a map."
  [m]
  (let [builder (cmd.DayCamera.JonSharedCmdDayCamera$Root/newBuilder)]
    ;; Handle oneof: cmd
    (when-let [cmd-field (first (filter
                                  (fn [[k v]]
                                    (#{:focus :zoom :set-iris
                                       :set-infra-red-filter :start :stop :photo
                                       :set-auto-iris :halt-all :set-fx-mode
                                       :next-fx-mode :prev-fx-mode :get-meteo
                                       :refresh-fx-mode :set-digital-zoom-level
                                       :set-clahe-level :shift-clahe-level}
                                     k))
                                  m))]
      (build-root-payload builder cmd-field))
    (.build builder)))

(defn build-get-pos
  "Build a GetPos protobuf message from a map."
  [m]
  (let [builder (cmd.DayCamera.JonSharedCmdDayCamera$GetPos/newBuilder)]
    (.build builder)))

(defn build-next-fx-mode
  "Build a NextFxMode protobuf message from a map."
  [m]
  (let [builder (cmd.DayCamera.JonSharedCmdDayCamera$NextFxMode/newBuilder)]
    (.build builder)))

(defn build-prev-fx-mode
  "Build a PrevFxMode protobuf message from a map."
  [m]
  (let [builder (cmd.DayCamera.JonSharedCmdDayCamera$PrevFxMode/newBuilder)]
    (.build builder)))

(defn build-refresh-fx-mode
  "Build a RefreshFxMode protobuf message from a map."
  [m]
  (let [builder (cmd.DayCamera.JonSharedCmdDayCamera$RefreshFxMode/newBuilder)]
    (.build builder)))

(defn build-halt-all
  "Build a HaltAll protobuf message from a map."
  [m]
  (let [builder (cmd.DayCamera.JonSharedCmdDayCamera$HaltAll/newBuilder)]
    (.build builder)))

(defn build-set-fx-mode
  "Build a SetFxMode protobuf message from a map."
  [m]
  (let [builder (cmd.DayCamera.JonSharedCmdDayCamera$SetFxMode/newBuilder)]
    ;; Set regular fields
    (when (contains? m :mode)
      (.setMode builder
                (get types/jon-gui-data-fx-mode-day-values (get m :mode))))
    (.build builder)))

(defn build-set-digital-zoom-level
  "Build a SetDigitalZoomLevel protobuf message from a map."
  [m]
  (let [builder
          (cmd.DayCamera.JonSharedCmdDayCamera$SetDigitalZoomLevel/newBuilder)]
    ;; Set regular fields
    (when (contains? m :value) (.setValue builder (get m :value)))
    (.build builder)))

(defn build-focus
  "Build a Focus protobuf message from a map."
  [m]
  (let [builder (cmd.DayCamera.JonSharedCmdDayCamera$Focus/newBuilder)]
    ;; Handle oneof: cmd
    (when-let [cmd-field (first (filter (fn [[k v]]
                                          (#{:set-value :move :halt :offset
                                             :reset-focus :save-to-table-focus}
                                           k))
                                  m))]
      (build-focus-payload builder cmd-field))
    (.build builder)))

(defn build-zoom
  "Build a Zoom protobuf message from a map."
  [m]
  (let [builder (cmd.DayCamera.JonSharedCmdDayCamera$Zoom/newBuilder)]
    ;; Handle oneof: cmd
    (when-let [cmd-field (first (filter (fn [[k v]]
                                          (#{:set-value :move :halt
                                             :set-zoom-table-value
                                             :next-zoom-table-pos
                                             :prev-zoom-table-pos :offset
                                             :reset-zoom :save-to-table}
                                           k))
                                  m))]
      (build-zoom-payload builder cmd-field))
    (.build builder)))

(defn build-next-zoom-table-pos
  "Build a NextZoomTablePos protobuf message from a map."
  [m]
  (let [builder
          (cmd.DayCamera.JonSharedCmdDayCamera$NextZoomTablePos/newBuilder)]
    (.build builder)))

(defn build-prev-zoom-table-pos
  "Build a PrevZoomTablePos protobuf message from a map."
  [m]
  (let [builder
          (cmd.DayCamera.JonSharedCmdDayCamera$PrevZoomTablePos/newBuilder)]
    (.build builder)))

(defn build-set-iris
  "Build a SetIris protobuf message from a map."
  [m]
  (let [builder (cmd.DayCamera.JonSharedCmdDayCamera$SetIris/newBuilder)]
    ;; Set regular fields
    (when (contains? m :value) (.setValue builder (get m :value)))
    (.build builder)))

(defn build-set-infra-red-filter
  "Build a SetInfraRedFilter protobuf message from a map."
  [m]
  (let [builder
          (cmd.DayCamera.JonSharedCmdDayCamera$SetInfraRedFilter/newBuilder)]
    ;; Set regular fields
    (when (contains? m :value) (.setValue builder (get m :value)))
    (.build builder)))

(defn build-set-auto-iris
  "Build a SetAutoIris protobuf message from a map."
  [m]
  (let [builder (cmd.DayCamera.JonSharedCmdDayCamera$SetAutoIris/newBuilder)]
    ;; Set regular fields
    (when (contains? m :value) (.setValue builder (get m :value)))
    (.build builder)))

(defn build-set-zoom-table-value
  "Build a SetZoomTableValue protobuf message from a map."
  [m]
  (let [builder
          (cmd.DayCamera.JonSharedCmdDayCamera$SetZoomTableValue/newBuilder)]
    ;; Set regular fields
    (when (contains? m :value) (.setValue builder (get m :value)))
    (.build builder)))

(defn build-stop
  "Build a Stop protobuf message from a map."
  [m]
  (let [builder (cmd.DayCamera.JonSharedCmdDayCamera$Stop/newBuilder)]
    (.build builder)))

(defn build-start
  "Build a Start protobuf message from a map."
  [m]
  (let [builder (cmd.DayCamera.JonSharedCmdDayCamera$Start/newBuilder)]
    (.build builder)))

(defn build-photo
  "Build a Photo protobuf message from a map."
  [m]
  (let [builder (cmd.DayCamera.JonSharedCmdDayCamera$Photo/newBuilder)]
    (.build builder)))

(defn build-halt
  "Build a Halt protobuf message from a map."
  [m]
  (let [builder (cmd.DayCamera.JonSharedCmdDayCamera$Halt/newBuilder)]
    (.build builder)))

(defn build-get-meteo
  "Build a GetMeteo protobuf message from a map."
  [m]
  (let [builder (cmd.DayCamera.JonSharedCmdDayCamera$GetMeteo/newBuilder)]
    (.build builder)))

(defn build-reset-zoom
  "Build a ResetZoom protobuf message from a map."
  [m]
  (let [builder (cmd.DayCamera.JonSharedCmdDayCamera$ResetZoom/newBuilder)]
    (.build builder)))

(defn build-reset-focus
  "Build a ResetFocus protobuf message from a map."
  [m]
  (let [builder (cmd.DayCamera.JonSharedCmdDayCamera$ResetFocus/newBuilder)]
    (.build builder)))

(defn build-save-to-table
  "Build a SaveToTable protobuf message from a map."
  [m]
  (let [builder (cmd.DayCamera.JonSharedCmdDayCamera$SaveToTable/newBuilder)]
    (.build builder)))

(defn build-save-to-table-focus
  "Build a SaveToTableFocus protobuf message from a map."
  [m]
  (let [builder
          (cmd.DayCamera.JonSharedCmdDayCamera$SaveToTableFocus/newBuilder)]
    (.build builder)))

(defn build-root
  "Build a Root protobuf message from a map."
  [m]
  (let [builder (cmd.HeatCamera.JonSharedCmdHeatCamera$Root/newBuilder)]
    ;; Handle oneof: cmd
    (when-let [cmd-field (first (filter
                                  (fn [[k v]]
                                    (#{:zoom :set-agc :set-filter :start :stop
                                       :photo :zoom-in :zoom-out :zoom-stop
                                       :focus-in :focus-out :focus-stop
                                       :calibrate :set-dde-level :enable-dde
                                       :disable-dde :set-auto-focus
                                       :focus-step-plus :focus-step-minus
                                       :set-fx-mode :next-fx-mode :prev-fx-mode
                                       :get-meteo :shift-dde :refresh-fx-mode
                                       :reset-zoom :save-to-table
                                       :set-calib-mode :set-digital-zoom-level
                                       :set-clahe-level :shift-clahe-level}
                                     k))
                                  m))]
      (build-root-payload builder cmd-field))
    (.build builder)))

(defn build-set-fx-mode
  "Build a SetFxMode protobuf message from a map."
  [m]
  (let [builder (cmd.HeatCamera.JonSharedCmdHeatCamera$SetFxMode/newBuilder)]
    ;; Set regular fields
    (when (contains? m :mode)
      (.setMode builder
                (get types/jon-gui-data-fx-mode-heat-values (get m :mode))))
    (.build builder)))

(defn build-set-clahe-level
  "Build a SetClaheLevel protobuf message from a map."
  [m]
  (let [builder
          (cmd.HeatCamera.JonSharedCmdHeatCamera$SetClaheLevel/newBuilder)]
    ;; Set regular fields
    (when (contains? m :value) (.setValue builder (get m :value)))
    (.build builder)))

(defn build-shift-clahe-level
  "Build a ShiftClaheLevel protobuf message from a map."
  [m]
  (let [builder
          (cmd.HeatCamera.JonSharedCmdHeatCamera$ShiftClaheLevel/newBuilder)]
    ;; Set regular fields
    (when (contains? m :value) (.setValue builder (get m :value)))
    (.build builder)))

(defn build-next-fx-mode
  "Build a NextFxMode protobuf message from a map."
  [m]
  (let [builder (cmd.HeatCamera.JonSharedCmdHeatCamera$NextFxMode/newBuilder)]
    (.build builder)))

(defn build-prev-fx-mode
  "Build a PrevFxMode protobuf message from a map."
  [m]
  (let [builder (cmd.HeatCamera.JonSharedCmdHeatCamera$PrevFxMode/newBuilder)]
    (.build builder)))

(defn build-refresh-fx-mode
  "Build a RefreshFxMode protobuf message from a map."
  [m]
  (let [builder
          (cmd.HeatCamera.JonSharedCmdHeatCamera$RefreshFxMode/newBuilder)]
    (.build builder)))

(defn build-enable-dde
  "Build a EnableDDE protobuf message from a map."
  [m]
  (let [builder (cmd.HeatCamera.JonSharedCmdHeatCamera$EnableDDE/newBuilder)]
    (.build builder)))

(defn build-disable-dde
  "Build a DisableDDE protobuf message from a map."
  [m]
  (let [builder (cmd.HeatCamera.JonSharedCmdHeatCamera$DisableDDE/newBuilder)]
    (.build builder)))

(defn build-set-value
  "Build a SetValue protobuf message from a map."
  [m]
  (let [builder (cmd.HeatCamera.JonSharedCmdHeatCamera$SetValue/newBuilder)]
    ;; Set regular fields
    (when (contains? m :value) (.setValue builder (get m :value)))
    (.build builder)))

(defn build-set-dde-level
  "Build a SetDDELevel protobuf message from a map."
  [m]
  (let [builder (cmd.HeatCamera.JonSharedCmdHeatCamera$SetDDELevel/newBuilder)]
    ;; Set regular fields
    (when (contains? m :value) (.setValue builder (get m :value)))
    (.build builder)))

(defn build-set-digital-zoom-level
  "Build a SetDigitalZoomLevel protobuf message from a map."
  [m]
  (let
    [builder
       (cmd.HeatCamera.JonSharedCmdHeatCamera$SetDigitalZoomLevel/newBuilder)]
    ;; Set regular fields
    (when (contains? m :value) (.setValue builder (get m :value)))
    (.build builder)))

(defn build-shift-dde
  "Build a ShiftDDE protobuf message from a map."
  [m]
  (let [builder (cmd.HeatCamera.JonSharedCmdHeatCamera$ShiftDDE/newBuilder)]
    ;; Set regular fields
    (when (contains? m :value) (.setValue builder (get m :value)))
    (.build builder)))

(defn build-zoom-in
  "Build a ZoomIn protobuf message from a map."
  [m]
  (let [builder (cmd.HeatCamera.JonSharedCmdHeatCamera$ZoomIn/newBuilder)]
    (.build builder)))

(defn build-zoom-out
  "Build a ZoomOut protobuf message from a map."
  [m]
  (let [builder (cmd.HeatCamera.JonSharedCmdHeatCamera$ZoomOut/newBuilder)]
    (.build builder)))

(defn build-zoom-stop
  "Build a ZoomStop protobuf message from a map."
  [m]
  (let [builder (cmd.HeatCamera.JonSharedCmdHeatCamera$ZoomStop/newBuilder)]
    (.build builder)))

(defn build-focus-in
  "Build a FocusIn protobuf message from a map."
  [m]
  (let [builder (cmd.HeatCamera.JonSharedCmdHeatCamera$FocusIn/newBuilder)]
    (.build builder)))

(defn build-focus-out
  "Build a FocusOut protobuf message from a map."
  [m]
  (let [builder (cmd.HeatCamera.JonSharedCmdHeatCamera$FocusOut/newBuilder)]
    (.build builder)))

(defn build-focus-stop
  "Build a FocusStop protobuf message from a map."
  [m]
  (let [builder (cmd.HeatCamera.JonSharedCmdHeatCamera$FocusStop/newBuilder)]
    (.build builder)))

(defn build-focus-step-plus
  "Build a FocusStepPlus protobuf message from a map."
  [m]
  (let [builder
          (cmd.HeatCamera.JonSharedCmdHeatCamera$FocusStepPlus/newBuilder)]
    (.build builder)))

(defn build-focus-step-minus
  "Build a FocusStepMinus protobuf message from a map."
  [m]
  (let [builder
          (cmd.HeatCamera.JonSharedCmdHeatCamera$FocusStepMinus/newBuilder)]
    (.build builder)))

(defn build-calibrate
  "Build a Calibrate protobuf message from a map."
  [m]
  (let [builder (cmd.HeatCamera.JonSharedCmdHeatCamera$Calibrate/newBuilder)]
    (.build builder)))

(defn build-zoom
  "Build a Zoom protobuf message from a map."
  [m]
  (let [builder (cmd.HeatCamera.JonSharedCmdHeatCamera$Zoom/newBuilder)]
    ;; Handle oneof: cmd
    (when-let [cmd-field (first (filter (fn [[k v]]
                                          (#{:set-zoom-table-value
                                             :next-zoom-table-pos
                                             :prev-zoom-table-pos}
                                           k))
                                  m))]
      (build-zoom-payload builder cmd-field))
    (.build builder)))

(defn build-next-zoom-table-pos
  "Build a NextZoomTablePos protobuf message from a map."
  [m]
  (let [builder
          (cmd.HeatCamera.JonSharedCmdHeatCamera$NextZoomTablePos/newBuilder)]
    (.build builder)))

(defn build-prev-zoom-table-pos
  "Build a PrevZoomTablePos protobuf message from a map."
  [m]
  (let [builder
          (cmd.HeatCamera.JonSharedCmdHeatCamera$PrevZoomTablePos/newBuilder)]
    (.build builder)))

(defn build-set-calib-mode
  "Build a SetCalibMode protobuf message from a map."
  [m]
  (let [builder (cmd.HeatCamera.JonSharedCmdHeatCamera$SetCalibMode/newBuilder)]
    (.build builder)))

(defn build-set-zoom-table-value
  "Build a SetZoomTableValue protobuf message from a map."
  [m]
  (let [builder
          (cmd.HeatCamera.JonSharedCmdHeatCamera$SetZoomTableValue/newBuilder)]
    ;; Set regular fields
    (when (contains? m :value) (.setValue builder (get m :value)))
    (.build builder)))

(defn build-set-agc
  "Build a SetAGC protobuf message from a map."
  [m]
  (let [builder (cmd.HeatCamera.JonSharedCmdHeatCamera$SetAGC/newBuilder)]
    ;; Set regular fields
    (when (contains? m :value)
      (.setValue builder
                 (get types/jon-gui-data-video-channel-heat-agc-modes-values
                      (get m :value))))
    (.build builder)))

(defn build-set-filters
  "Build a SetFilters protobuf message from a map."
  [m]
  (let [builder (cmd.HeatCamera.JonSharedCmdHeatCamera$SetFilters/newBuilder)]
    ;; Set regular fields
    (when (contains? m :value)
      (.setValue builder
                 (get types/jon-gui-data-video-channel-heat-filters-values
                      (get m :value))))
    (.build builder)))

(defn build-start
  "Build a Start protobuf message from a map."
  [m]
  (let [builder (cmd.HeatCamera.JonSharedCmdHeatCamera$Start/newBuilder)]
    (.build builder)))

(defn build-stop
  "Build a Stop protobuf message from a map."
  [m]
  (let [builder (cmd.HeatCamera.JonSharedCmdHeatCamera$Stop/newBuilder)]
    (.build builder)))

(defn build-halt
  "Build a Halt protobuf message from a map."
  [m]
  (let [builder (cmd.HeatCamera.JonSharedCmdHeatCamera$Halt/newBuilder)]
    (.build builder)))

(defn build-photo
  "Build a Photo protobuf message from a map."
  [m]
  (let [builder (cmd.HeatCamera.JonSharedCmdHeatCamera$Photo/newBuilder)]
    (.build builder)))

(defn build-get-meteo
  "Build a GetMeteo protobuf message from a map."
  [m]
  (let [builder (cmd.HeatCamera.JonSharedCmdHeatCamera$GetMeteo/newBuilder)]
    (.build builder)))

(defn build-set-auto-focus
  "Build a SetAutoFocus protobuf message from a map."
  [m]
  (let [builder (cmd.HeatCamera.JonSharedCmdHeatCamera$SetAutoFocus/newBuilder)]
    ;; Set regular fields
    (when (contains? m :value) (.setValue builder (get m :value)))
    (.build builder)))

(defn build-reset-zoom
  "Build a ResetZoom protobuf message from a map."
  [m]
  (let [builder (cmd.HeatCamera.JonSharedCmdHeatCamera$ResetZoom/newBuilder)]
    (.build builder)))

(defn build-save-to-table
  "Build a SaveToTable protobuf message from a map."
  [m]
  (let [builder (cmd.HeatCamera.JonSharedCmdHeatCamera$SaveToTable/newBuilder)]
    (.build builder)))

(defn build-root
  "Build a Root protobuf message from a map."
  [m]
  (let [builder (cmd.RotaryPlatform.JonSharedCmdRotary$Root/newBuilder)]
    ;; Handle oneof: cmd
    (when-let [cmd-field
                 (first (filter (fn [[k v]]
                                  (#{:start :stop :axis :set-platform-azimuth
                                     :set-platform-elevation :set-platform-bank
                                     :halt :set-use-rotary-as-compass
                                     :rotate-to-gps :set-origin-gps :set-mode
                                     :rotate-to-ndc :scan-start :scan-stop
                                     :scan-pause :scan-unpause :get-meteo
                                     :scan-prev :scan-next
                                     :scan-refresh-node-list :scan-select-node
                                     :scan-delete-node :scan-update-node
                                     :scan-add-node}
                                   k))
                          m))]
      (build-root-payload builder cmd-field))
    (.build builder)))

(defn build-axis
  "Build a Axis protobuf message from a map."
  [m]
  (let [builder (cmd.RotaryPlatform.JonSharedCmdRotary$Axis/newBuilder)]
    ;; Set regular fields
    (when (contains? m :azimuth)
      (.setAzimuth builder (build-azimuth (get m :azimuth))))
    (when (contains? m :elevation)
      (.setElevation builder (build-elevation (get m :elevation))))
    (.build builder)))

(defn build-set-mode
  "Build a SetMode protobuf message from a map."
  [m]
  (let [builder (cmd.RotaryPlatform.JonSharedCmdRotary$SetMode/newBuilder)]
    ;; Set regular fields
    (when (contains? m :mode)
      (.setMode builder
                (get types/jon-gui-data-rotary-mode-values (get m :mode))))
    (.build builder)))

(defn build-set-azimuth-value
  "Build a SetAzimuthValue protobuf message from a map."
  [m]
  (let [builder
          (cmd.RotaryPlatform.JonSharedCmdRotary$SetAzimuthValue/newBuilder)]
    ;; Set regular fields
    (when (contains? m :value) (.setValue builder (get m :value)))
    (when (contains? m :direction)
      (.setDirection builder
                     (get types/jon-gui-data-rotary-direction-values
                          (get m :direction))))
    (.build builder)))

(defn build-rotate-azimuth-to
  "Build a RotateAzimuthTo protobuf message from a map."
  [m]
  (let [builder
          (cmd.RotaryPlatform.JonSharedCmdRotary$RotateAzimuthTo/newBuilder)]
    ;; Set regular fields
    (when (contains? m :target-value)
      (.setTargetValue builder (get m :target-value)))
    (when (contains? m :speed) (.setSpeed builder (get m :speed)))
    (when (contains? m :direction)
      (.setDirection builder
                     (get types/jon-gui-data-rotary-direction-values
                          (get m :direction))))
    (.build builder)))

(defn build-rotate-azimuth
  "Build a RotateAzimuth protobuf message from a map."
  [m]
  (let [builder
          (cmd.RotaryPlatform.JonSharedCmdRotary$RotateAzimuth/newBuilder)]
    ;; Set regular fields
    (when (contains? m :speed) (.setSpeed builder (get m :speed)))
    (when (contains? m :direction)
      (.setDirection builder
                     (get types/jon-gui-data-rotary-direction-values
                          (get m :direction))))
    (.build builder)))

(defn build-rotate-elevation
  "Build a RotateElevation protobuf message from a map."
  [m]
  (let [builder
          (cmd.RotaryPlatform.JonSharedCmdRotary$RotateElevation/newBuilder)]
    ;; Set regular fields
    (when (contains? m :speed) (.setSpeed builder (get m :speed)))
    (when (contains? m :direction)
      (.setDirection builder
                     (get types/jon-gui-data-rotary-direction-values
                          (get m :direction))))
    (.build builder)))

(defn build-set-elevation-value
  "Build a SetElevationValue protobuf message from a map."
  [m]
  (let [builder
          (cmd.RotaryPlatform.JonSharedCmdRotary$SetElevationValue/newBuilder)]
    ;; Set regular fields
    (when (contains? m :value) (.setValue builder (get m :value)))
    (.build builder)))

(defn build-rotate-elevation-to
  "Build a RotateElevationTo protobuf message from a map."
  [m]
  (let [builder
          (cmd.RotaryPlatform.JonSharedCmdRotary$RotateElevationTo/newBuilder)]
    ;; Set regular fields
    (when (contains? m :target-value)
      (.setTargetValue builder (get m :target-value)))
    (when (contains? m :speed) (.setSpeed builder (get m :speed)))
    (.build builder)))

(defn build-rotate-elevation-relative
  "Build a RotateElevationRelative protobuf message from a map."
  [m]
  (let
    [builder
       (cmd.RotaryPlatform.JonSharedCmdRotary$RotateElevationRelative/newBuilder)]
    ;; Set regular fields
    (when (contains? m :value) (.setValue builder (get m :value)))
    (when (contains? m :speed) (.setSpeed builder (get m :speed)))
    (when (contains? m :direction)
      (.setDirection builder
                     (get types/jon-gui-data-rotary-direction-values
                          (get m :direction))))
    (.build builder)))

(defn build-rotate-elevation-relative-set
  "Build a RotateElevationRelativeSet protobuf message from a map."
  [m]
  (let
    [builder
       (cmd.RotaryPlatform.JonSharedCmdRotary$RotateElevationRelativeSet/newBuilder)]
    ;; Set regular fields
    (when (contains? m :value) (.setValue builder (get m :value)))
    (when (contains? m :direction)
      (.setDirection builder
                     (get types/jon-gui-data-rotary-direction-values
                          (get m :direction))))
    (.build builder)))

(defn build-rotate-azimuth-relative
  "Build a RotateAzimuthRelative protobuf message from a map."
  [m]
  (let
    [builder
       (cmd.RotaryPlatform.JonSharedCmdRotary$RotateAzimuthRelative/newBuilder)]
    ;; Set regular fields
    (when (contains? m :value) (.setValue builder (get m :value)))
    (when (contains? m :speed) (.setSpeed builder (get m :speed)))
    (when (contains? m :direction)
      (.setDirection builder
                     (get types/jon-gui-data-rotary-direction-values
                          (get m :direction))))
    (.build builder)))

(defn build-rotate-azimuth-relative-set
  "Build a RotateAzimuthRelativeSet protobuf message from a map."
  [m]
  (let
    [builder
       (cmd.RotaryPlatform.JonSharedCmdRotary$RotateAzimuthRelativeSet/newBuilder)]
    ;; Set regular fields
    (when (contains? m :value) (.setValue builder (get m :value)))
    (when (contains? m :direction)
      (.setDirection builder
                     (get types/jon-gui-data-rotary-direction-values
                          (get m :direction))))
    (.build builder)))

(defn build-set-platform-azimuth
  "Build a SetPlatformAzimuth protobuf message from a map."
  [m]
  (let [builder
          (cmd.RotaryPlatform.JonSharedCmdRotary$SetPlatformAzimuth/newBuilder)]
    ;; Set regular fields
    (when (contains? m :value) (.setValue builder (get m :value)))
    (.build builder)))

(defn build-set-platform-elevation
  "Build a SetPlatformElevation protobuf message from a map."
  [m]
  (let
    [builder
       (cmd.RotaryPlatform.JonSharedCmdRotary$SetPlatformElevation/newBuilder)]
    ;; Set regular fields
    (when (contains? m :value) (.setValue builder (get m :value)))
    (.build builder)))

(defn build-set-platform-bank
  "Build a SetPlatformBank protobuf message from a map."
  [m]
  (let [builder
          (cmd.RotaryPlatform.JonSharedCmdRotary$SetPlatformBank/newBuilder)]
    ;; Set regular fields
    (when (contains? m :value) (.setValue builder (get m :value)))
    (.build builder)))

(defn build-get-meteo
  "Build a GetMeteo protobuf message from a map."
  [m]
  (let [builder (cmd.RotaryPlatform.JonSharedCmdRotary$GetMeteo/newBuilder)]
    (.build builder)))

(defn build-azimuth
  "Build a Azimuth protobuf message from a map."
  [m]
  (let [builder (cmd.RotaryPlatform.JonSharedCmdRotary$Azimuth/newBuilder)]
    ;; Handle oneof: cmd
    (when-let [cmd-field (first (filter (fn [[k v]]
                                          (#{:set-value :rotate-to :rotate
                                             :relative :relative-set :halt}
                                           k))
                                  m))]
      (build-azimuth-payload builder cmd-field))
    (.build builder)))

(defn build-start
  "Build a Start protobuf message from a map."
  [m]
  (let [builder (cmd.RotaryPlatform.JonSharedCmdRotary$Start/newBuilder)]
    (.build builder)))

(defn build-stop
  "Build a Stop protobuf message from a map."
  [m]
  (let [builder (cmd.RotaryPlatform.JonSharedCmdRotary$Stop/newBuilder)]
    (.build builder)))

(defn build-halt
  "Build a Halt protobuf message from a map."
  [m]
  (let [builder (cmd.RotaryPlatform.JonSharedCmdRotary$Halt/newBuilder)]
    (.build builder)))

(defn build-scan-start
  "Build a ScanStart protobuf message from a map."
  [m]
  (let [builder (cmd.RotaryPlatform.JonSharedCmdRotary$ScanStart/newBuilder)]
    (.build builder)))

(defn build-scan-stop
  "Build a ScanStop protobuf message from a map."
  [m]
  (let [builder (cmd.RotaryPlatform.JonSharedCmdRotary$ScanStop/newBuilder)]
    (.build builder)))

(defn build-scan-pause
  "Build a ScanPause protobuf message from a map."
  [m]
  (let [builder (cmd.RotaryPlatform.JonSharedCmdRotary$ScanPause/newBuilder)]
    (.build builder)))

(defn build-scan-unpause
  "Build a ScanUnpause protobuf message from a map."
  [m]
  (let [builder (cmd.RotaryPlatform.JonSharedCmdRotary$ScanUnpause/newBuilder)]
    (.build builder)))

(defn build-halt-azimuth
  "Build a HaltAzimuth protobuf message from a map."
  [m]
  (let [builder (cmd.RotaryPlatform.JonSharedCmdRotary$HaltAzimuth/newBuilder)]
    (.build builder)))

(defn build-halt-elevation
  "Build a HaltElevation protobuf message from a map."
  [m]
  (let [builder
          (cmd.RotaryPlatform.JonSharedCmdRotary$HaltElevation/newBuilder)]
    (.build builder)))

(defn build-scan-prev
  "Build a ScanPrev protobuf message from a map."
  [m]
  (let [builder (cmd.RotaryPlatform.JonSharedCmdRotary$ScanPrev/newBuilder)]
    (.build builder)))

(defn build-scan-next
  "Build a ScanNext protobuf message from a map."
  [m]
  (let [builder (cmd.RotaryPlatform.JonSharedCmdRotary$ScanNext/newBuilder)]
    (.build builder)))

(defn build-scan-refresh-node-list
  "Build a ScanRefreshNodeList protobuf message from a map."
  [m]
  (let
    [builder
       (cmd.RotaryPlatform.JonSharedCmdRotary$ScanRefreshNodeList/newBuilder)]
    (.build builder)))

(defn build-scan-select-node
  "Build a ScanSelectNode protobuf message from a map."
  [m]
  (let [builder
          (cmd.RotaryPlatform.JonSharedCmdRotary$ScanSelectNode/newBuilder)]
    ;; Set regular fields
    (when (contains? m :index) (.setIndex builder (get m :index)))
    (.build builder)))

(defn build-scan-delete-node
  "Build a ScanDeleteNode protobuf message from a map."
  [m]
  (let [builder
          (cmd.RotaryPlatform.JonSharedCmdRotary$ScanDeleteNode/newBuilder)]
    ;; Set regular fields
    (when (contains? m :index) (.setIndex builder (get m :index)))
    (.build builder)))

(defn build-scan-update-node
  "Build a ScanUpdateNode protobuf message from a map."
  [m]
  (let [builder
          (cmd.RotaryPlatform.JonSharedCmdRotary$ScanUpdateNode/newBuilder)]
    ;; Set regular fields
    (when (contains? m :index) (.setIndex builder (get m :index)))
    (when (contains? m :day-zoom-table-value)
      (.setDayZoomTableValue builder (get m :day-zoom-table-value)))
    (when (contains? m :heat-zoom-table-value)
      (.setHeatZoomTableValue builder (get m :heat-zoom-table-value)))
    (when (contains? m :azimuth) (.setAzimuth builder (get m :azimuth)))
    (when (contains? m :elevation) (.setElevation builder (get m :elevation)))
    (when (contains? m :linger) (.setLinger builder (get m :linger)))
    (when (contains? m :speed) (.setSpeed builder (get m :speed)))
    (.build builder)))

(defn build-scan-add-node
  "Build a ScanAddNode protobuf message from a map."
  [m]
  (let [builder (cmd.RotaryPlatform.JonSharedCmdRotary$ScanAddNode/newBuilder)]
    ;; Set regular fields
    (when (contains? m :index) (.setIndex builder (get m :index)))
    (when (contains? m :day-zoom-table-value)
      (.setDayZoomTableValue builder (get m :day-zoom-table-value)))
    (when (contains? m :heat-zoom-table-value)
      (.setHeatZoomTableValue builder (get m :heat-zoom-table-value)))
    (when (contains? m :azimuth) (.setAzimuth builder (get m :azimuth)))
    (when (contains? m :elevation) (.setElevation builder (get m :elevation)))
    (when (contains? m :linger) (.setLinger builder (get m :linger)))
    (when (contains? m :speed) (.setSpeed builder (get m :speed)))
    (.build builder)))

(defn build-elevation
  "Build a Elevation protobuf message from a map."
  [m]
  (let [builder (cmd.RotaryPlatform.JonSharedCmdRotary$Elevation/newBuilder)]
    ;; Handle oneof: cmd
    (when-let [cmd-field (first (filter (fn [[k v]]
                                          (#{:set-value :rotate-to :rotate
                                             :relative :relative-set :halt}
                                           k))
                                  m))]
      (build-elevation-payload builder cmd-field))
    (.build builder)))

(defn build-set-use-rotary-as-compass
  "Build a setUseRotaryAsCompass protobuf message from a map."
  [m]
  (let
    [builder
       (cmd.RotaryPlatform.JonSharedCmdRotary$setUseRotaryAsCompass/newBuilder)]
    ;; Set regular fields
    (when (contains? m :flag) (.setFlag builder (get m :flag)))
    (.build builder)))

(defn build-rotate-to-gps
  "Build a RotateToGPS protobuf message from a map."
  [m]
  (let [builder (cmd.RotaryPlatform.JonSharedCmdRotary$RotateToGPS/newBuilder)]
    ;; Set regular fields
    (when (contains? m :latitude) (.setLatitude builder (get m :latitude)))
    (when (contains? m :longitude) (.setLongitude builder (get m :longitude)))
    (when (contains? m :altitude) (.setAltitude builder (get m :altitude)))
    (.build builder)))

(defn build-set-origin-gps
  "Build a SetOriginGPS protobuf message from a map."
  [m]
  (let [builder (cmd.RotaryPlatform.JonSharedCmdRotary$SetOriginGPS/newBuilder)]
    ;; Set regular fields
    (when (contains? m :latitude) (.setLatitude builder (get m :latitude)))
    (when (contains? m :longitude) (.setLongitude builder (get m :longitude)))
    (when (contains? m :altitude) (.setAltitude builder (get m :altitude)))
    (.build builder)))

(defn build-rotate-to-ndc
  "Build a RotateToNDC protobuf message from a map."
  [m]
  (let [builder (cmd.RotaryPlatform.JonSharedCmdRotary$RotateToNDC/newBuilder)]
    ;; Set regular fields
    (when (contains? m :channel)
      (.setChannel builder
                   (get types/jon-gui-data-video-channel-values
                        (get m :channel))))
    (when (contains? m :x) (.setX builder (get m :x)))
    (when (contains? m :y) (.setY builder (get m :y)))
    (.build builder)))

(defn build-root
  "Build a Root protobuf message from a map."
  [m]
  (let [builder (cmd.OSD.JonSharedCmdOsd$Root/newBuilder)]
    ;; Handle oneof: cmd
    (when-let [cmd-field (first (filter (fn [[k v]]
                                          (#{:show-default-screen
                                             :show-lrf-measure-screen
                                             :show-lrf-result-screen
                                             :show-lrf-result-simplified-screen
                                             :enable-heat-osd :disable-heat-osd
                                             :enable-day-osd :disable-day-osd}
                                           k))
                                  m))]
      (build-root-payload builder cmd-field))
    (.build builder)))

(defn build-show-default-screen
  "Build a ShowDefaultScreen protobuf message from a map."
  [m]
  (let [builder (cmd.OSD.JonSharedCmdOsd$ShowDefaultScreen/newBuilder)]
    (.build builder)))

(defn build-show-lrf-measure-screen
  "Build a ShowLRFMeasureScreen protobuf message from a map."
  [m]
  (let [builder (cmd.OSD.JonSharedCmdOsd$ShowLRFMeasureScreen/newBuilder)]
    (.build builder)))

(defn build-show-lrf-result-screen
  "Build a ShowLRFResultScreen protobuf message from a map."
  [m]
  (let [builder (cmd.OSD.JonSharedCmdOsd$ShowLRFResultScreen/newBuilder)]
    (.build builder)))

(defn build-show-lrf-result-simplified-screen
  "Build a ShowLRFResultSimplifiedScreen protobuf message from a map."
  [m]
  (let [builder
          (cmd.OSD.JonSharedCmdOsd$ShowLRFResultSimplifiedScreen/newBuilder)]
    (.build builder)))

(defn build-enable-heat-osd
  "Build a EnableHeatOSD protobuf message from a map."
  [m]
  (let [builder (cmd.OSD.JonSharedCmdOsd$EnableHeatOSD/newBuilder)]
    (.build builder)))

(defn build-disable-heat-osd
  "Build a DisableHeatOSD protobuf message from a map."
  [m]
  (let [builder (cmd.OSD.JonSharedCmdOsd$DisableHeatOSD/newBuilder)]
    (.build builder)))

(defn build-enable-day-osd
  "Build a EnableDayOSD protobuf message from a map."
  [m]
  (let [builder (cmd.OSD.JonSharedCmdOsd$EnableDayOSD/newBuilder)]
    (.build builder)))

(defn build-disable-day-osd
  "Build a DisableDayOSD protobuf message from a map."
  [m]
  (let [builder (cmd.OSD.JonSharedCmdOsd$DisableDayOSD/newBuilder)]
    (.build builder)))

(defn build-root
  "Build a Root protobuf message from a map."
  [m]
  (let [builder (cmd.Lrf_calib.JonSharedCmdLrfAlign$Root/newBuilder)]
    ;; Handle oneof: channel
    (when-let [channel-field (first (filter (fn [[k v]] (#{:day :heat} k)) m))]
      (build-root-payload builder channel-field))
    (.build builder)))

(defn build-offsets
  "Build a Offsets protobuf message from a map."
  [m]
  (let [builder (cmd.Lrf_calib.JonSharedCmdLrfAlign$Offsets/newBuilder)]
    ;; Handle oneof: cmd
    (when-let [cmd-field (first (filter (fn [[k v]]
                                          (#{:set :save :reset :shift} k))
                                  m))]
      (build-offsets-payload builder cmd-field))
    (.build builder)))

(defn build-set-offsets
  "Build a SetOffsets protobuf message from a map."
  [m]
  (let [builder (cmd.Lrf_calib.JonSharedCmdLrfAlign$SetOffsets/newBuilder)]
    ;; Set regular fields
    (when (contains? m :x) (.setX builder (get m :x)))
    (when (contains? m :y) (.setY builder (get m :y)))
    (.build builder)))

(defn build-shift-offsets-by
  "Build a ShiftOffsetsBy protobuf message from a map."
  [m]
  (let [builder (cmd.Lrf_calib.JonSharedCmdLrfAlign$ShiftOffsetsBy/newBuilder)]
    ;; Set regular fields
    (when (contains? m :x) (.setX builder (get m :x)))
    (when (contains? m :y) (.setY builder (get m :y)))
    (.build builder)))

(defn build-reset-offsets
  "Build a ResetOffsets protobuf message from a map."
  [m]
  (let [builder (cmd.Lrf_calib.JonSharedCmdLrfAlign$ResetOffsets/newBuilder)]
    (.build builder)))

(defn build-save-offsets
  "Build a SaveOffsets protobuf message from a map."
  [m]
  (let [builder (cmd.Lrf_calib.JonSharedCmdLrfAlign$SaveOffsets/newBuilder)]
    (.build builder)))

(defn build-root
  "Build a Root protobuf message from a map."
  [m]
  (let [builder (cmd.System.JonSharedCmdSystem$Root/newBuilder)]
    ;; Handle oneof: cmd
    (when-let [cmd-field (first
                           (filter (fn [[k v]]
                                     (#{:start-all :stop-all :reboot :power-off
                                        :localization :reset-configs :start-rec
                                        :stop-rec :mark-rec-important
                                        :unmark-rec-important :enter-transport
                                        :geodesic-mode-enable
                                        :geodesic-mode-disable}
                                      k))
                             m))]
      (build-root-payload builder cmd-field))
    (.build builder)))

(defn build-start-a-ll
  "Build a StartALl protobuf message from a map."
  [m]
  (let [builder (cmd.System.JonSharedCmdSystem$StartALl/newBuilder)]
    (.build builder)))

(defn build-stop-a-ll
  "Build a StopALl protobuf message from a map."
  [m]
  (let [builder (cmd.System.JonSharedCmdSystem$StopALl/newBuilder)]
    (.build builder)))

(defn build-reboot
  "Build a Reboot protobuf message from a map."
  [m]
  (let [builder (cmd.System.JonSharedCmdSystem$Reboot/newBuilder)]
    (.build builder)))

(defn build-power-off
  "Build a PowerOff protobuf message from a map."
  [m]
  (let [builder (cmd.System.JonSharedCmdSystem$PowerOff/newBuilder)]
    (.build builder)))

(defn build-reset-configs
  "Build a ResetConfigs protobuf message from a map."
  [m]
  (let [builder (cmd.System.JonSharedCmdSystem$ResetConfigs/newBuilder)]
    (.build builder)))

(defn build-start-rec
  "Build a StartRec protobuf message from a map."
  [m]
  (let [builder (cmd.System.JonSharedCmdSystem$StartRec/newBuilder)]
    (.build builder)))

(defn build-stop-rec
  "Build a StopRec protobuf message from a map."
  [m]
  (let [builder (cmd.System.JonSharedCmdSystem$StopRec/newBuilder)]
    (.build builder)))

(defn build-mark-rec-important
  "Build a MarkRecImportant protobuf message from a map."
  [m]
  (let [builder (cmd.System.JonSharedCmdSystem$MarkRecImportant/newBuilder)]
    (.build builder)))

(defn build-unmark-rec-important
  "Build a UnmarkRecImportant protobuf message from a map."
  [m]
  (let [builder (cmd.System.JonSharedCmdSystem$UnmarkRecImportant/newBuilder)]
    (.build builder)))

(defn build-enter-transport
  "Build a EnterTransport protobuf message from a map."
  [m]
  (let [builder (cmd.System.JonSharedCmdSystem$EnterTransport/newBuilder)]
    (.build builder)))

(defn build-enable-geodesic-mode
  "Build a EnableGeodesicMode protobuf message from a map."
  [m]
  (let [builder (cmd.System.JonSharedCmdSystem$EnableGeodesicMode/newBuilder)]
    (.build builder)))

(defn build-disable-geodesic-mode
  "Build a DisableGeodesicMode protobuf message from a map."
  [m]
  (let [builder (cmd.System.JonSharedCmdSystem$DisableGeodesicMode/newBuilder)]
    (.build builder)))

(defn build-set-localization
  "Build a SetLocalization protobuf message from a map."
  [m]
  (let [builder (cmd.System.JonSharedCmdSystem$SetLocalization/newBuilder)]
    ;; Set regular fields
    (when (contains? m :loc)
      (.setLoc builder
               (get types/jon-gui-data-system-localizations-values
                    (get m :loc))))
    (.build builder)))

(defn build-root
  "Build a Root protobuf message from a map."
  [m]
  (let [builder (cmd.CV.JonSharedCmdCv$Root/newBuilder)]
    ;; Handle oneof: cmd
    (when-let [cmd-field (first (filter (fn [[k v]]
                                          (#{:set-auto-focus :start-track-ndc
                                             :stop-track :vampire-mode-enable
                                             :vampire-mode-disable
                                             :stabilization-mode-enable
                                             :stabilization-mode-disable
                                             :dump-start :dump-stop}
                                           k))
                                  m))]
      (build-root-payload builder cmd-field))
    (.build builder)))

(defn build-vampire-mode-enable
  "Build a VampireModeEnable protobuf message from a map."
  [m]
  (let [builder (cmd.CV.JonSharedCmdCv$VampireModeEnable/newBuilder)]
    (.build builder)))

(defn build-dump-start
  "Build a DumpStart protobuf message from a map."
  [m]
  (let [builder (cmd.CV.JonSharedCmdCv$DumpStart/newBuilder)] (.build builder)))

(defn build-dump-stop
  "Build a DumpStop protobuf message from a map."
  [m]
  (let [builder (cmd.CV.JonSharedCmdCv$DumpStop/newBuilder)] (.build builder)))

(defn build-vampire-mode-disable
  "Build a VampireModeDisable protobuf message from a map."
  [m]
  (let [builder (cmd.CV.JonSharedCmdCv$VampireModeDisable/newBuilder)]
    (.build builder)))

(defn build-stabilization-mode-enable
  "Build a StabilizationModeEnable protobuf message from a map."
  [m]
  (let [builder (cmd.CV.JonSharedCmdCv$StabilizationModeEnable/newBuilder)]
    (.build builder)))

(defn build-stabilization-mode-disable
  "Build a StabilizationModeDisable protobuf message from a map."
  [m]
  (let [builder (cmd.CV.JonSharedCmdCv$StabilizationModeDisable/newBuilder)]
    (.build builder)))

(defn build-set-auto-focus
  "Build a SetAutoFocus protobuf message from a map."
  [m]
  (let [builder (cmd.CV.JonSharedCmdCv$SetAutoFocus/newBuilder)]
    ;; Set regular fields
    (when (contains? m :channel)
      (.setChannel builder
                   (get types/jon-gui-data-video-channel-values
                        (get m :channel))))
    (when (contains? m :value) (.setValue builder (get m :value)))
    (.build builder)))

(defn build-start-track-ndc
  "Build a StartTrackNDC protobuf message from a map."
  [m]
  (let [builder (cmd.CV.JonSharedCmdCv$StartTrackNDC/newBuilder)]
    ;; Set regular fields
    (when (contains? m :channel)
      (.setChannel builder
                   (get types/jon-gui-data-video-channel-values
                        (get m :channel))))
    (when (contains? m :x) (.setX builder (get m :x)))
    (when (contains? m :y) (.setY builder (get m :y)))
    (when (contains? m :frame-time) (.setFrameTime builder (get m :frame-time)))
    (.build builder)))

(defn build-stop-track
  "Build a StopTrack protobuf message from a map."
  [m]
  (let [builder (cmd.CV.JonSharedCmdCv$StopTrack/newBuilder)] (.build builder)))

(defn build-root
  "Build a Root protobuf message from a map."
  [m]
  (let [builder
          (cmd.DayCamGlassHeater.JonSharedCmdDayCamGlassHeater$Root/newBuilder)]
    ;; Handle oneof: cmd
    (when-let [cmd-field (first (filter (fn [[k v]]
                                          (#{:start :stop :turn-on :turn-off
                                             :get-meteo}
                                           k))
                                  m))]
      (build-root-payload builder cmd-field))
    (.build builder)))

(defn build-start
  "Build a Start protobuf message from a map."
  [m]
  (let
    [builder
       (cmd.DayCamGlassHeater.JonSharedCmdDayCamGlassHeater$Start/newBuilder)]
    (.build builder)))

(defn build-stop
  "Build a Stop protobuf message from a map."
  [m]
  (let [builder
          (cmd.DayCamGlassHeater.JonSharedCmdDayCamGlassHeater$Stop/newBuilder)]
    (.build builder)))

(defn build-turn-on
  "Build a TurnOn protobuf message from a map."
  [m]
  (let
    [builder
       (cmd.DayCamGlassHeater.JonSharedCmdDayCamGlassHeater$TurnOn/newBuilder)]
    (.build builder)))

(defn build-turn-off
  "Build a TurnOff protobuf message from a map."
  [m]
  (let
    [builder
       (cmd.DayCamGlassHeater.JonSharedCmdDayCamGlassHeater$TurnOff/newBuilder)]
    (.build builder)))

(defn build-get-meteo
  "Build a GetMeteo protobuf message from a map."
  [m]
  (let
    [builder
       (cmd.DayCamGlassHeater.JonSharedCmdDayCamGlassHeater$GetMeteo/newBuilder)]
    (.build builder)))

(defn build-root
  "Build a Root protobuf message from a map."
  [m]
  (let [builder (cmd.Lira.JonSharedCmdLira$Root/newBuilder)]
    ;; Handle oneof: cmd
    (when-let [cmd-field (first (filter (fn [[k v]] (#{:refine-target} k)) m))]
      (build-root-payload builder cmd-field))
    (.build builder)))

(defn build-refine-target
  "Build a Refine_target protobuf message from a map."
  [m]
  (let [builder (cmd.Lira.JonSharedCmdLira$Refine_target/newBuilder)]
    ;; Set regular fields
    (when (contains? m :target)
      (.setTarget builder (build-jon-gui-data-lira-target (get m :target))))
    (.build builder)))

(defn build-jon-gui-data-lira-target
  "Build a JonGuiDataLiraTarget protobuf message from a map."
  [m]
  (let [builder (cmd.Lira.JonSharedCmdLira$JonGuiDataLiraTarget/newBuilder)]
    ;; Set regular fields
    (when (contains? m :timestamp) (.setTimestamp builder (get m :timestamp)))
    (when (contains? m :target-longitude)
      (.setTargetLongitude builder (get m :target-longitude)))
    (when (contains? m :target-latitude)
      (.setTargetLatitude builder (get m :target-latitude)))
    (when (contains? m :target-altitude)
      (.setTargetAltitude builder (get m :target-altitude)))
    (when (contains? m :target-azimuth)
      (.setTargetAzimuth builder (get m :target-azimuth)))
    (when (contains? m :target-elevation)
      (.setTargetElevation builder (get m :target-elevation)))
    (when (contains? m :distance) (.setDistance builder (get m :distance)))
    (when (contains? m :uuid-part-1)
      (.setUuidPart1 builder (get m :uuid-part-1)))
    (when (contains? m :uuid-part-2)
      (.setUuidPart2 builder (get m :uuid-part-2)))
    (when (contains? m :uuid-part-3)
      (.setUuidPart3 builder (get m :uuid-part-3)))
    (when (contains? m :uuid-part-4)
      (.setUuidPart4 builder (get m :uuid-part-4)))
    (.build builder)))

(defn build-root
  "Build a Root protobuf message from a map."
  [m]
  (let [builder (cmd.JonSharedCmd$Root/newBuilder)]
    ;; Set regular fields
    (when (contains? m :protocol-version)
      (.setProtocolVersion builder (get m :protocol-version)))
    (when (contains? m :session-id) (.setSessionId builder (get m :session-id)))
    (when (contains? m :important) (.setImportant builder (get m :important)))
    (when (contains? m :from-cv-subsystem)
      (.setFromCvSubsystem builder (get m :from-cv-subsystem)))
    (when (contains? m :client-type)
      (.setClientType builder
                      (get types/jon-gui-data-client-type-values
                           (get m :client-type))))
    ;; Handle oneof: payload
    (when-let [payload-field
                 (first (filter (fn [[k v]]
                                  (#{:day-camera :heat-camera :gps :compass :lrf
                                     :lrf-calib :rotary :osd :ping :noop :frozen
                                     :system :cv :day-cam-glass-heater :lira}
                                   k))
                          m))]
      (build-root-payload builder payload-field))
    (.build builder)))

(defn build-ping
  "Build a Ping protobuf message from a map."
  [m]
  (let [builder (cmd.JonSharedCmd$Ping/newBuilder)] (.build builder)))

(defn build-noop
  "Build a Noop protobuf message from a map."
  [m]
  (let [builder (cmd.JonSharedCmd$Noop/newBuilder)] (.build builder)))

(defn build-frozen
  "Build a Frozen protobuf message from a map."
  [m]
  (let [builder (cmd.JonSharedCmd$Frozen/newBuilder)] (.build builder)))

(defn parse-root
  "Parse a Root protobuf message to a map."
  [^cmd.Compass.JonSharedCmdCompass$Root proto]
  (cond-> {}
    ;; Oneof payload
    true (merge (parse-root-payload proto))))

(defn parse-start
  "Parse a Start protobuf message to a map."
  [^cmd.Compass.JonSharedCmdCompass$Start proto]
  {})

(defn parse-stop
  "Parse a Stop protobuf message to a map."
  [^cmd.Compass.JonSharedCmdCompass$Stop proto]
  {})

(defn parse-next
  "Parse a Next protobuf message to a map."
  [^cmd.Compass.JonSharedCmdCompass$Next proto]
  {})

(defn parse-calibrate-start-long
  "Parse a CalibrateStartLong protobuf message to a map."
  [^cmd.Compass.JonSharedCmdCompass$CalibrateStartLong proto]
  {})

(defn parse-calibrate-start-short
  "Parse a CalibrateStartShort protobuf message to a map."
  [^cmd.Compass.JonSharedCmdCompass$CalibrateStartShort proto]
  {})

(defn parse-calibrate-next
  "Parse a CalibrateNext protobuf message to a map."
  [^cmd.Compass.JonSharedCmdCompass$CalibrateNext proto]
  {})

(defn parse-calibrate-cencel
  "Parse a CalibrateCencel protobuf message to a map."
  [^cmd.Compass.JonSharedCmdCompass$CalibrateCencel proto]
  {})

(defn parse-get-meteo
  "Parse a GetMeteo protobuf message to a map."
  [^cmd.Compass.JonSharedCmdCompass$GetMeteo proto]
  {})

(defn parse-set-magnetic-declination
  "Parse a SetMagneticDeclination protobuf message to a map."
  [^cmd.Compass.JonSharedCmdCompass$SetMagneticDeclination proto]
  (cond-> {}
    ;; Regular fields
    true (assoc :value (.getValue proto))))

(defn parse-set-offset-angle-azimuth
  "Parse a SetOffsetAngleAzimuth protobuf message to a map."
  [^cmd.Compass.JonSharedCmdCompass$SetOffsetAngleAzimuth proto]
  (cond-> {}
    ;; Regular fields
    true (assoc :value (.getValue proto))))

(defn parse-set-offset-angle-elevation
  "Parse a SetOffsetAngleElevation protobuf message to a map."
  [^cmd.Compass.JonSharedCmdCompass$SetOffsetAngleElevation proto]
  (cond-> {}
    ;; Regular fields
    true (assoc :value (.getValue proto))))

(defn parse-set-use-rotary-position
  "Parse a SetUseRotaryPosition protobuf message to a map."
  [^cmd.Compass.JonSharedCmdCompass$SetUseRotaryPosition proto]
  (cond-> {}
    ;; Regular fields
    true (assoc :flag (.getFlag proto))))

(defn parse-root
  "Parse a Root protobuf message to a map."
  [^cmd.Gps.JonSharedCmdGps$Root proto]
  (cond-> {}
    ;; Oneof payload
    true (merge (parse-root-payload proto))))

(defn parse-start
  "Parse a Start protobuf message to a map."
  [^cmd.Gps.JonSharedCmdGps$Start proto]
  {})

(defn parse-stop
  "Parse a Stop protobuf message to a map."
  [^cmd.Gps.JonSharedCmdGps$Stop proto]
  {})

(defn parse-get-meteo
  "Parse a GetMeteo protobuf message to a map."
  [^cmd.Gps.JonSharedCmdGps$GetMeteo proto]
  {})

(defn parse-set-use-manual-position
  "Parse a SetUseManualPosition protobuf message to a map."
  [^cmd.Gps.JonSharedCmdGps$SetUseManualPosition proto]
  (cond-> {}
    ;; Regular fields
    true (assoc :flag (.getFlag proto))))

(defn parse-set-manual-position
  "Parse a SetManualPosition protobuf message to a map."
  [^cmd.Gps.JonSharedCmdGps$SetManualPosition proto]
  (cond-> {}
    ;; Regular fields
    true (assoc :latitude (.getLatitude proto))
    true (assoc :longitude (.getLongitude proto))
    true (assoc :altitude (.getAltitude proto))))

(defn parse-jon-gui-data-meteo
  "Parse a JonGuiDataMeteo protobuf message to a map."
  [^ser.JonSharedDataTypes$JonGuiDataMeteo proto]
  (cond-> {}
    ;; Regular fields
    true (assoc :temperature (.getTemperature proto))
    true (assoc :humidity (.getHumidity proto))
    true (assoc :pressure (.getPressure proto))))

(defn parse-root
  "Parse a Root protobuf message to a map."
  [^cmd.Lrf.JonSharedCmdLrf$Root proto]
  (cond-> {}
    ;; Oneof payload
    true (merge (parse-root-payload proto))))

(defn parse-get-meteo
  "Parse a GetMeteo protobuf message to a map."
  [^cmd.Lrf.JonSharedCmdLrf$GetMeteo proto]
  {})

(defn parse-start
  "Parse a Start protobuf message to a map."
  [^cmd.Lrf.JonSharedCmdLrf$Start proto]
  {})

(defn parse-stop
  "Parse a Stop protobuf message to a map."
  [^cmd.Lrf.JonSharedCmdLrf$Stop proto]
  {})

(defn parse-measure
  "Parse a Measure protobuf message to a map."
  [^cmd.Lrf.JonSharedCmdLrf$Measure proto]
  {})

(defn parse-scan-on
  "Parse a ScanOn protobuf message to a map."
  [^cmd.Lrf.JonSharedCmdLrf$ScanOn proto]
  {})

(defn parse-scan-off
  "Parse a ScanOff protobuf message to a map."
  [^cmd.Lrf.JonSharedCmdLrf$ScanOff proto]
  {})

(defn parse-refine-off
  "Parse a RefineOff protobuf message to a map."
  [^cmd.Lrf.JonSharedCmdLrf$RefineOff proto]
  {})

(defn parse-refine-on
  "Parse a RefineOn protobuf message to a map."
  [^cmd.Lrf.JonSharedCmdLrf$RefineOn proto]
  {})

(defn parse-target-designator-off
  "Parse a TargetDesignatorOff protobuf message to a map."
  [^cmd.Lrf.JonSharedCmdLrf$TargetDesignatorOff proto]
  {})

(defn parse-target-designator-on-mode-a
  "Parse a TargetDesignatorOnModeA protobuf message to a map."
  [^cmd.Lrf.JonSharedCmdLrf$TargetDesignatorOnModeA proto]
  {})

(defn parse-target-designator-on-mode-b
  "Parse a TargetDesignatorOnModeB protobuf message to a map."
  [^cmd.Lrf.JonSharedCmdLrf$TargetDesignatorOnModeB proto]
  {})

(defn parse-enable-fog-mode
  "Parse a EnableFogMode protobuf message to a map."
  [^cmd.Lrf.JonSharedCmdLrf$EnableFogMode proto]
  {})

(defn parse-disable-fog-mode
  "Parse a DisableFogMode protobuf message to a map."
  [^cmd.Lrf.JonSharedCmdLrf$DisableFogMode proto]
  {})

(defn parse-set-scan-mode
  "Parse a SetScanMode protobuf message to a map."
  [^cmd.Lrf.JonSharedCmdLrf$SetScanMode proto]
  (cond-> {}
    ;; Regular fields
    true (assoc :mode
           (get types/jon-gui-data-lrf-scan-modes-keywords (.getMode proto)))))

(defn parse-new-session
  "Parse a NewSession protobuf message to a map."
  [^cmd.Lrf.JonSharedCmdLrf$NewSession proto]
  {})

(defn parse-set-value
  "Parse a SetValue protobuf message to a map."
  [^cmd.DayCamera.JonSharedCmdDayCamera$SetValue proto]
  (cond-> {}
    ;; Regular fields
    true (assoc :value (.getValue proto))))

(defn parse-move
  "Parse a Move protobuf message to a map."
  [^cmd.DayCamera.JonSharedCmdDayCamera$Move proto]
  (cond-> {}
    ;; Regular fields
    true (assoc :target-value (.getTargetValue proto))
    true (assoc :speed (.getSpeed proto))))

(defn parse-offset
  "Parse a Offset protobuf message to a map."
  [^cmd.DayCamera.JonSharedCmdDayCamera$Offset proto]
  (cond-> {}
    ;; Regular fields
    true (assoc :offset-value (.getOffsetValue proto))))

(defn parse-set-clahe-level
  "Parse a SetClaheLevel protobuf message to a map."
  [^cmd.DayCamera.JonSharedCmdDayCamera$SetClaheLevel proto]
  (cond-> {}
    ;; Regular fields
    true (assoc :value (.getValue proto))))

(defn parse-shift-clahe-level
  "Parse a ShiftClaheLevel protobuf message to a map."
  [^cmd.DayCamera.JonSharedCmdDayCamera$ShiftClaheLevel proto]
  (cond-> {}
    ;; Regular fields
    true (assoc :value (.getValue proto))))

(defn parse-root
  "Parse a Root protobuf message to a map."
  [^cmd.DayCamera.JonSharedCmdDayCamera$Root proto]
  (cond-> {}
    ;; Oneof payload
    true (merge (parse-root-payload proto))))

(defn parse-get-pos
  "Parse a GetPos protobuf message to a map."
  [^cmd.DayCamera.JonSharedCmdDayCamera$GetPos proto]
  {})

(defn parse-next-fx-mode
  "Parse a NextFxMode protobuf message to a map."
  [^cmd.DayCamera.JonSharedCmdDayCamera$NextFxMode proto]
  {})

(defn parse-prev-fx-mode
  "Parse a PrevFxMode protobuf message to a map."
  [^cmd.DayCamera.JonSharedCmdDayCamera$PrevFxMode proto]
  {})

(defn parse-refresh-fx-mode
  "Parse a RefreshFxMode protobuf message to a map."
  [^cmd.DayCamera.JonSharedCmdDayCamera$RefreshFxMode proto]
  {})

(defn parse-halt-all
  "Parse a HaltAll protobuf message to a map."
  [^cmd.DayCamera.JonSharedCmdDayCamera$HaltAll proto]
  {})

(defn parse-set-fx-mode
  "Parse a SetFxMode protobuf message to a map."
  [^cmd.DayCamera.JonSharedCmdDayCamera$SetFxMode proto]
  (cond-> {}
    ;; Regular fields
    true (assoc :mode
           (get types/jon-gui-data-fx-mode-day-keywords (.getMode proto)))))

(defn parse-set-digital-zoom-level
  "Parse a SetDigitalZoomLevel protobuf message to a map."
  [^cmd.DayCamera.JonSharedCmdDayCamera$SetDigitalZoomLevel proto]
  (cond-> {}
    ;; Regular fields
    true (assoc :value (.getValue proto))))

(defn parse-focus
  "Parse a Focus protobuf message to a map."
  [^cmd.DayCamera.JonSharedCmdDayCamera$Focus proto]
  (cond-> {}
    ;; Oneof payload
    true (merge (parse-focus-payload proto))))

(defn parse-zoom
  "Parse a Zoom protobuf message to a map."
  [^cmd.DayCamera.JonSharedCmdDayCamera$Zoom proto]
  (cond-> {}
    ;; Oneof payload
    true (merge (parse-zoom-payload proto))))

(defn parse-next-zoom-table-pos
  "Parse a NextZoomTablePos protobuf message to a map."
  [^cmd.DayCamera.JonSharedCmdDayCamera$NextZoomTablePos proto]
  {})

(defn parse-prev-zoom-table-pos
  "Parse a PrevZoomTablePos protobuf message to a map."
  [^cmd.DayCamera.JonSharedCmdDayCamera$PrevZoomTablePos proto]
  {})

(defn parse-set-iris
  "Parse a SetIris protobuf message to a map."
  [^cmd.DayCamera.JonSharedCmdDayCamera$SetIris proto]
  (cond-> {}
    ;; Regular fields
    true (assoc :value (.getValue proto))))

(defn parse-set-infra-red-filter
  "Parse a SetInfraRedFilter protobuf message to a map."
  [^cmd.DayCamera.JonSharedCmdDayCamera$SetInfraRedFilter proto]
  (cond-> {}
    ;; Regular fields
    true (assoc :value (.getValue proto))))

(defn parse-set-auto-iris
  "Parse a SetAutoIris protobuf message to a map."
  [^cmd.DayCamera.JonSharedCmdDayCamera$SetAutoIris proto]
  (cond-> {}
    ;; Regular fields
    true (assoc :value (.getValue proto))))

(defn parse-set-zoom-table-value
  "Parse a SetZoomTableValue protobuf message to a map."
  [^cmd.DayCamera.JonSharedCmdDayCamera$SetZoomTableValue proto]
  (cond-> {}
    ;; Regular fields
    true (assoc :value (.getValue proto))))

(defn parse-stop
  "Parse a Stop protobuf message to a map."
  [^cmd.DayCamera.JonSharedCmdDayCamera$Stop proto]
  {})

(defn parse-start
  "Parse a Start protobuf message to a map."
  [^cmd.DayCamera.JonSharedCmdDayCamera$Start proto]
  {})

(defn parse-photo
  "Parse a Photo protobuf message to a map."
  [^cmd.DayCamera.JonSharedCmdDayCamera$Photo proto]
  {})

(defn parse-halt
  "Parse a Halt protobuf message to a map."
  [^cmd.DayCamera.JonSharedCmdDayCamera$Halt proto]
  {})

(defn parse-get-meteo
  "Parse a GetMeteo protobuf message to a map."
  [^cmd.DayCamera.JonSharedCmdDayCamera$GetMeteo proto]
  {})

(defn parse-reset-zoom
  "Parse a ResetZoom protobuf message to a map."
  [^cmd.DayCamera.JonSharedCmdDayCamera$ResetZoom proto]
  {})

(defn parse-reset-focus
  "Parse a ResetFocus protobuf message to a map."
  [^cmd.DayCamera.JonSharedCmdDayCamera$ResetFocus proto]
  {})

(defn parse-save-to-table
  "Parse a SaveToTable protobuf message to a map."
  [^cmd.DayCamera.JonSharedCmdDayCamera$SaveToTable proto]
  {})

(defn parse-save-to-table-focus
  "Parse a SaveToTableFocus protobuf message to a map."
  [^cmd.DayCamera.JonSharedCmdDayCamera$SaveToTableFocus proto]
  {})

(defn parse-root
  "Parse a Root protobuf message to a map."
  [^cmd.HeatCamera.JonSharedCmdHeatCamera$Root proto]
  (cond-> {}
    ;; Oneof payload
    true (merge (parse-root-payload proto))))

(defn parse-set-fx-mode
  "Parse a SetFxMode protobuf message to a map."
  [^cmd.HeatCamera.JonSharedCmdHeatCamera$SetFxMode proto]
  (cond-> {}
    ;; Regular fields
    true (assoc :mode
           (get types/jon-gui-data-fx-mode-heat-keywords (.getMode proto)))))

(defn parse-set-clahe-level
  "Parse a SetClaheLevel protobuf message to a map."
  [^cmd.HeatCamera.JonSharedCmdHeatCamera$SetClaheLevel proto]
  (cond-> {}
    ;; Regular fields
    true (assoc :value (.getValue proto))))

(defn parse-shift-clahe-level
  "Parse a ShiftClaheLevel protobuf message to a map."
  [^cmd.HeatCamera.JonSharedCmdHeatCamera$ShiftClaheLevel proto]
  (cond-> {}
    ;; Regular fields
    true (assoc :value (.getValue proto))))

(defn parse-next-fx-mode
  "Parse a NextFxMode protobuf message to a map."
  [^cmd.HeatCamera.JonSharedCmdHeatCamera$NextFxMode proto]
  {})

(defn parse-prev-fx-mode
  "Parse a PrevFxMode protobuf message to a map."
  [^cmd.HeatCamera.JonSharedCmdHeatCamera$PrevFxMode proto]
  {})

(defn parse-refresh-fx-mode
  "Parse a RefreshFxMode protobuf message to a map."
  [^cmd.HeatCamera.JonSharedCmdHeatCamera$RefreshFxMode proto]
  {})

(defn parse-enable-dde
  "Parse a EnableDDE protobuf message to a map."
  [^cmd.HeatCamera.JonSharedCmdHeatCamera$EnableDDE proto]
  {})

(defn parse-disable-dde
  "Parse a DisableDDE protobuf message to a map."
  [^cmd.HeatCamera.JonSharedCmdHeatCamera$DisableDDE proto]
  {})

(defn parse-set-value
  "Parse a SetValue protobuf message to a map."
  [^cmd.HeatCamera.JonSharedCmdHeatCamera$SetValue proto]
  (cond-> {}
    ;; Regular fields
    true (assoc :value (.getValue proto))))

(defn parse-set-dde-level
  "Parse a SetDDELevel protobuf message to a map."
  [^cmd.HeatCamera.JonSharedCmdHeatCamera$SetDDELevel proto]
  (cond-> {}
    ;; Regular fields
    true (assoc :value (.getValue proto))))

(defn parse-set-digital-zoom-level
  "Parse a SetDigitalZoomLevel protobuf message to a map."
  [^cmd.HeatCamera.JonSharedCmdHeatCamera$SetDigitalZoomLevel proto]
  (cond-> {}
    ;; Regular fields
    true (assoc :value (.getValue proto))))

(defn parse-shift-dde
  "Parse a ShiftDDE protobuf message to a map."
  [^cmd.HeatCamera.JonSharedCmdHeatCamera$ShiftDDE proto]
  (cond-> {}
    ;; Regular fields
    true (assoc :value (.getValue proto))))

(defn parse-zoom-in
  "Parse a ZoomIn protobuf message to a map."
  [^cmd.HeatCamera.JonSharedCmdHeatCamera$ZoomIn proto]
  {})

(defn parse-zoom-out
  "Parse a ZoomOut protobuf message to a map."
  [^cmd.HeatCamera.JonSharedCmdHeatCamera$ZoomOut proto]
  {})

(defn parse-zoom-stop
  "Parse a ZoomStop protobuf message to a map."
  [^cmd.HeatCamera.JonSharedCmdHeatCamera$ZoomStop proto]
  {})

(defn parse-focus-in
  "Parse a FocusIn protobuf message to a map."
  [^cmd.HeatCamera.JonSharedCmdHeatCamera$FocusIn proto]
  {})

(defn parse-focus-out
  "Parse a FocusOut protobuf message to a map."
  [^cmd.HeatCamera.JonSharedCmdHeatCamera$FocusOut proto]
  {})

(defn parse-focus-stop
  "Parse a FocusStop protobuf message to a map."
  [^cmd.HeatCamera.JonSharedCmdHeatCamera$FocusStop proto]
  {})

(defn parse-focus-step-plus
  "Parse a FocusStepPlus protobuf message to a map."
  [^cmd.HeatCamera.JonSharedCmdHeatCamera$FocusStepPlus proto]
  {})

(defn parse-focus-step-minus
  "Parse a FocusStepMinus protobuf message to a map."
  [^cmd.HeatCamera.JonSharedCmdHeatCamera$FocusStepMinus proto]
  {})

(defn parse-calibrate
  "Parse a Calibrate protobuf message to a map."
  [^cmd.HeatCamera.JonSharedCmdHeatCamera$Calibrate proto]
  {})

(defn parse-zoom
  "Parse a Zoom protobuf message to a map."
  [^cmd.HeatCamera.JonSharedCmdHeatCamera$Zoom proto]
  (cond-> {}
    ;; Oneof payload
    true (merge (parse-zoom-payload proto))))

(defn parse-next-zoom-table-pos
  "Parse a NextZoomTablePos protobuf message to a map."
  [^cmd.HeatCamera.JonSharedCmdHeatCamera$NextZoomTablePos proto]
  {})

(defn parse-prev-zoom-table-pos
  "Parse a PrevZoomTablePos protobuf message to a map."
  [^cmd.HeatCamera.JonSharedCmdHeatCamera$PrevZoomTablePos proto]
  {})

(defn parse-set-calib-mode
  "Parse a SetCalibMode protobuf message to a map."
  [^cmd.HeatCamera.JonSharedCmdHeatCamera$SetCalibMode proto]
  {})

(defn parse-set-zoom-table-value
  "Parse a SetZoomTableValue protobuf message to a map."
  [^cmd.HeatCamera.JonSharedCmdHeatCamera$SetZoomTableValue proto]
  (cond-> {}
    ;; Regular fields
    true (assoc :value (.getValue proto))))

(defn parse-set-agc
  "Parse a SetAGC protobuf message to a map."
  [^cmd.HeatCamera.JonSharedCmdHeatCamera$SetAGC proto]
  (cond-> {}
    ;; Regular fields
    true (assoc :value
           (get types/jon-gui-data-video-channel-heat-agc-modes-keywords
                (.getValue proto)))))

(defn parse-set-filters
  "Parse a SetFilters protobuf message to a map."
  [^cmd.HeatCamera.JonSharedCmdHeatCamera$SetFilters proto]
  (cond-> {}
    ;; Regular fields
    true (assoc :value
           (get types/jon-gui-data-video-channel-heat-filters-keywords
                (.getValue proto)))))

(defn parse-start
  "Parse a Start protobuf message to a map."
  [^cmd.HeatCamera.JonSharedCmdHeatCamera$Start proto]
  {})

(defn parse-stop
  "Parse a Stop protobuf message to a map."
  [^cmd.HeatCamera.JonSharedCmdHeatCamera$Stop proto]
  {})

(defn parse-halt
  "Parse a Halt protobuf message to a map."
  [^cmd.HeatCamera.JonSharedCmdHeatCamera$Halt proto]
  {})

(defn parse-photo
  "Parse a Photo protobuf message to a map."
  [^cmd.HeatCamera.JonSharedCmdHeatCamera$Photo proto]
  {})

(defn parse-get-meteo
  "Parse a GetMeteo protobuf message to a map."
  [^cmd.HeatCamera.JonSharedCmdHeatCamera$GetMeteo proto]
  {})

(defn parse-set-auto-focus
  "Parse a SetAutoFocus protobuf message to a map."
  [^cmd.HeatCamera.JonSharedCmdHeatCamera$SetAutoFocus proto]
  (cond-> {}
    ;; Regular fields
    true (assoc :value (.getValue proto))))

(defn parse-reset-zoom
  "Parse a ResetZoom protobuf message to a map."
  [^cmd.HeatCamera.JonSharedCmdHeatCamera$ResetZoom proto]
  {})

(defn parse-save-to-table
  "Parse a SaveToTable protobuf message to a map."
  [^cmd.HeatCamera.JonSharedCmdHeatCamera$SaveToTable proto]
  {})

(defn parse-root
  "Parse a Root protobuf message to a map."
  [^cmd.RotaryPlatform.JonSharedCmdRotary$Root proto]
  (cond-> {}
    ;; Oneof payload
    true (merge (parse-root-payload proto))))

(defn parse-axis
  "Parse a Axis protobuf message to a map."
  [^cmd.RotaryPlatform.JonSharedCmdRotary$Axis proto]
  (cond-> {}
    ;; Regular fields
    (.hasAzimuth proto) (assoc :azimuth (parse-azimuth (.getAzimuth proto)))
    (.hasElevation proto) (assoc :elevation
                            (parse-elevation (.getElevation proto)))))

(defn parse-set-mode
  "Parse a SetMode protobuf message to a map."
  [^cmd.RotaryPlatform.JonSharedCmdRotary$SetMode proto]
  (cond-> {}
    ;; Regular fields
    true (assoc :mode
           (get types/jon-gui-data-rotary-mode-keywords (.getMode proto)))))

(defn parse-set-azimuth-value
  "Parse a SetAzimuthValue protobuf message to a map."
  [^cmd.RotaryPlatform.JonSharedCmdRotary$SetAzimuthValue proto]
  (cond-> {}
    ;; Regular fields
    true (assoc :value (.getValue proto))
    true (assoc :direction
           (get types/jon-gui-data-rotary-direction-keywords
                (.getDirection proto)))))

(defn parse-rotate-azimuth-to
  "Parse a RotateAzimuthTo protobuf message to a map."
  [^cmd.RotaryPlatform.JonSharedCmdRotary$RotateAzimuthTo proto]
  (cond-> {}
    ;; Regular fields
    true (assoc :target-value (.getTargetValue proto))
    true (assoc :speed (.getSpeed proto))
    true (assoc :direction
           (get types/jon-gui-data-rotary-direction-keywords
                (.getDirection proto)))))

(defn parse-rotate-azimuth
  "Parse a RotateAzimuth protobuf message to a map."
  [^cmd.RotaryPlatform.JonSharedCmdRotary$RotateAzimuth proto]
  (cond-> {}
    ;; Regular fields
    true (assoc :speed (.getSpeed proto))
    true (assoc :direction
           (get types/jon-gui-data-rotary-direction-keywords
                (.getDirection proto)))))

(defn parse-rotate-elevation
  "Parse a RotateElevation protobuf message to a map."
  [^cmd.RotaryPlatform.JonSharedCmdRotary$RotateElevation proto]
  (cond-> {}
    ;; Regular fields
    true (assoc :speed (.getSpeed proto))
    true (assoc :direction
           (get types/jon-gui-data-rotary-direction-keywords
                (.getDirection proto)))))

(defn parse-set-elevation-value
  "Parse a SetElevationValue protobuf message to a map."
  [^cmd.RotaryPlatform.JonSharedCmdRotary$SetElevationValue proto]
  (cond-> {}
    ;; Regular fields
    true (assoc :value (.getValue proto))))

(defn parse-rotate-elevation-to
  "Parse a RotateElevationTo protobuf message to a map."
  [^cmd.RotaryPlatform.JonSharedCmdRotary$RotateElevationTo proto]
  (cond-> {}
    ;; Regular fields
    true (assoc :target-value (.getTargetValue proto))
    true (assoc :speed (.getSpeed proto))))

(defn parse-rotate-elevation-relative
  "Parse a RotateElevationRelative protobuf message to a map."
  [^cmd.RotaryPlatform.JonSharedCmdRotary$RotateElevationRelative proto]
  (cond-> {}
    ;; Regular fields
    true (assoc :value (.getValue proto))
    true (assoc :speed (.getSpeed proto))
    true (assoc :direction
           (get types/jon-gui-data-rotary-direction-keywords
                (.getDirection proto)))))

(defn parse-rotate-elevation-relative-set
  "Parse a RotateElevationRelativeSet protobuf message to a map."
  [^cmd.RotaryPlatform.JonSharedCmdRotary$RotateElevationRelativeSet proto]
  (cond-> {}
    ;; Regular fields
    true (assoc :value (.getValue proto))
    true (assoc :direction
           (get types/jon-gui-data-rotary-direction-keywords
                (.getDirection proto)))))

(defn parse-rotate-azimuth-relative
  "Parse a RotateAzimuthRelative protobuf message to a map."
  [^cmd.RotaryPlatform.JonSharedCmdRotary$RotateAzimuthRelative proto]
  (cond-> {}
    ;; Regular fields
    true (assoc :value (.getValue proto))
    true (assoc :speed (.getSpeed proto))
    true (assoc :direction
           (get types/jon-gui-data-rotary-direction-keywords
                (.getDirection proto)))))

(defn parse-rotate-azimuth-relative-set
  "Parse a RotateAzimuthRelativeSet protobuf message to a map."
  [^cmd.RotaryPlatform.JonSharedCmdRotary$RotateAzimuthRelativeSet proto]
  (cond-> {}
    ;; Regular fields
    true (assoc :value (.getValue proto))
    true (assoc :direction
           (get types/jon-gui-data-rotary-direction-keywords
                (.getDirection proto)))))

(defn parse-set-platform-azimuth
  "Parse a SetPlatformAzimuth protobuf message to a map."
  [^cmd.RotaryPlatform.JonSharedCmdRotary$SetPlatformAzimuth proto]
  (cond-> {}
    ;; Regular fields
    true (assoc :value (.getValue proto))))

(defn parse-set-platform-elevation
  "Parse a SetPlatformElevation protobuf message to a map."
  [^cmd.RotaryPlatform.JonSharedCmdRotary$SetPlatformElevation proto]
  (cond-> {}
    ;; Regular fields
    true (assoc :value (.getValue proto))))

(defn parse-set-platform-bank
  "Parse a SetPlatformBank protobuf message to a map."
  [^cmd.RotaryPlatform.JonSharedCmdRotary$SetPlatformBank proto]
  (cond-> {}
    ;; Regular fields
    true (assoc :value (.getValue proto))))

(defn parse-get-meteo
  "Parse a GetMeteo protobuf message to a map."
  [^cmd.RotaryPlatform.JonSharedCmdRotary$GetMeteo proto]
  {})

(defn parse-azimuth
  "Parse a Azimuth protobuf message to a map."
  [^cmd.RotaryPlatform.JonSharedCmdRotary$Azimuth proto]
  (cond-> {}
    ;; Oneof payload
    true (merge (parse-azimuth-payload proto))))

(defn parse-start
  "Parse a Start protobuf message to a map."
  [^cmd.RotaryPlatform.JonSharedCmdRotary$Start proto]
  {})

(defn parse-stop
  "Parse a Stop protobuf message to a map."
  [^cmd.RotaryPlatform.JonSharedCmdRotary$Stop proto]
  {})

(defn parse-halt
  "Parse a Halt protobuf message to a map."
  [^cmd.RotaryPlatform.JonSharedCmdRotary$Halt proto]
  {})

(defn parse-scan-start
  "Parse a ScanStart protobuf message to a map."
  [^cmd.RotaryPlatform.JonSharedCmdRotary$ScanStart proto]
  {})

(defn parse-scan-stop
  "Parse a ScanStop protobuf message to a map."
  [^cmd.RotaryPlatform.JonSharedCmdRotary$ScanStop proto]
  {})

(defn parse-scan-pause
  "Parse a ScanPause protobuf message to a map."
  [^cmd.RotaryPlatform.JonSharedCmdRotary$ScanPause proto]
  {})

(defn parse-scan-unpause
  "Parse a ScanUnpause protobuf message to a map."
  [^cmd.RotaryPlatform.JonSharedCmdRotary$ScanUnpause proto]
  {})

(defn parse-halt-azimuth
  "Parse a HaltAzimuth protobuf message to a map."
  [^cmd.RotaryPlatform.JonSharedCmdRotary$HaltAzimuth proto]
  {})

(defn parse-halt-elevation
  "Parse a HaltElevation protobuf message to a map."
  [^cmd.RotaryPlatform.JonSharedCmdRotary$HaltElevation proto]
  {})

(defn parse-scan-prev
  "Parse a ScanPrev protobuf message to a map."
  [^cmd.RotaryPlatform.JonSharedCmdRotary$ScanPrev proto]
  {})

(defn parse-scan-next
  "Parse a ScanNext protobuf message to a map."
  [^cmd.RotaryPlatform.JonSharedCmdRotary$ScanNext proto]
  {})

(defn parse-scan-refresh-node-list
  "Parse a ScanRefreshNodeList protobuf message to a map."
  [^cmd.RotaryPlatform.JonSharedCmdRotary$ScanRefreshNodeList proto]
  {})

(defn parse-scan-select-node
  "Parse a ScanSelectNode protobuf message to a map."
  [^cmd.RotaryPlatform.JonSharedCmdRotary$ScanSelectNode proto]
  (cond-> {}
    ;; Regular fields
    true (assoc :index (.getIndex proto))))

(defn parse-scan-delete-node
  "Parse a ScanDeleteNode protobuf message to a map."
  [^cmd.RotaryPlatform.JonSharedCmdRotary$ScanDeleteNode proto]
  (cond-> {}
    ;; Regular fields
    true (assoc :index (.getIndex proto))))

(defn parse-scan-update-node
  "Parse a ScanUpdateNode protobuf message to a map."
  [^cmd.RotaryPlatform.JonSharedCmdRotary$ScanUpdateNode proto]
  (cond-> {}
    ;; Regular fields
    true (assoc :index (.getIndex proto))
    true (assoc :day-zoom-table-value (.getDayZoomTableValue proto))
    true (assoc :heat-zoom-table-value (.getHeatZoomTableValue proto))
    true (assoc :azimuth (.getAzimuth proto))
    true (assoc :elevation (.getElevation proto))
    true (assoc :linger (.getLinger proto))
    true (assoc :speed (.getSpeed proto))))

(defn parse-scan-add-node
  "Parse a ScanAddNode protobuf message to a map."
  [^cmd.RotaryPlatform.JonSharedCmdRotary$ScanAddNode proto]
  (cond-> {}
    ;; Regular fields
    true (assoc :index (.getIndex proto))
    true (assoc :day-zoom-table-value (.getDayZoomTableValue proto))
    true (assoc :heat-zoom-table-value (.getHeatZoomTableValue proto))
    true (assoc :azimuth (.getAzimuth proto))
    true (assoc :elevation (.getElevation proto))
    true (assoc :linger (.getLinger proto))
    true (assoc :speed (.getSpeed proto))))

(defn parse-elevation
  "Parse a Elevation protobuf message to a map."
  [^cmd.RotaryPlatform.JonSharedCmdRotary$Elevation proto]
  (cond-> {}
    ;; Oneof payload
    true (merge (parse-elevation-payload proto))))

(defn parse-set-use-rotary-as-compass
  "Parse a setUseRotaryAsCompass protobuf message to a map."
  [^cmd.RotaryPlatform.JonSharedCmdRotary$setUseRotaryAsCompass proto]
  (cond-> {}
    ;; Regular fields
    true (assoc :flag (.getFlag proto))))

(defn parse-rotate-to-gps
  "Parse a RotateToGPS protobuf message to a map."
  [^cmd.RotaryPlatform.JonSharedCmdRotary$RotateToGPS proto]
  (cond-> {}
    ;; Regular fields
    true (assoc :latitude (.getLatitude proto))
    true (assoc :longitude (.getLongitude proto))
    true (assoc :altitude (.getAltitude proto))))

(defn parse-set-origin-gps
  "Parse a SetOriginGPS protobuf message to a map."
  [^cmd.RotaryPlatform.JonSharedCmdRotary$SetOriginGPS proto]
  (cond-> {}
    ;; Regular fields
    true (assoc :latitude (.getLatitude proto))
    true (assoc :longitude (.getLongitude proto))
    true (assoc :altitude (.getAltitude proto))))

(defn parse-rotate-to-ndc
  "Parse a RotateToNDC protobuf message to a map."
  [^cmd.RotaryPlatform.JonSharedCmdRotary$RotateToNDC proto]
  (cond-> {}
    ;; Regular fields
    true (assoc :channel
           (get types/jon-gui-data-video-channel-keywords (.getChannel proto)))
    true (assoc :x (.getX proto))
    true (assoc :y (.getY proto))))

(defn parse-root
  "Parse a Root protobuf message to a map."
  [^cmd.OSD.JonSharedCmdOsd$Root proto]
  (cond-> {}
    ;; Oneof payload
    true (merge (parse-root-payload proto))))

(defn parse-show-default-screen
  "Parse a ShowDefaultScreen protobuf message to a map."
  [^cmd.OSD.JonSharedCmdOsd$ShowDefaultScreen proto]
  {})

(defn parse-show-lrf-measure-screen
  "Parse a ShowLRFMeasureScreen protobuf message to a map."
  [^cmd.OSD.JonSharedCmdOsd$ShowLRFMeasureScreen proto]
  {})

(defn parse-show-lrf-result-screen
  "Parse a ShowLRFResultScreen protobuf message to a map."
  [^cmd.OSD.JonSharedCmdOsd$ShowLRFResultScreen proto]
  {})

(defn parse-show-lrf-result-simplified-screen
  "Parse a ShowLRFResultSimplifiedScreen protobuf message to a map."
  [^cmd.OSD.JonSharedCmdOsd$ShowLRFResultSimplifiedScreen proto]
  {})

(defn parse-enable-heat-osd
  "Parse a EnableHeatOSD protobuf message to a map."
  [^cmd.OSD.JonSharedCmdOsd$EnableHeatOSD proto]
  {})

(defn parse-disable-heat-osd
  "Parse a DisableHeatOSD protobuf message to a map."
  [^cmd.OSD.JonSharedCmdOsd$DisableHeatOSD proto]
  {})

(defn parse-enable-day-osd
  "Parse a EnableDayOSD protobuf message to a map."
  [^cmd.OSD.JonSharedCmdOsd$EnableDayOSD proto]
  {})

(defn parse-disable-day-osd
  "Parse a DisableDayOSD protobuf message to a map."
  [^cmd.OSD.JonSharedCmdOsd$DisableDayOSD proto]
  {})

(defn parse-root
  "Parse a Root protobuf message to a map."
  [^cmd.Lrf_calib.JonSharedCmdLrfAlign$Root proto]
  (cond-> {}
    ;; Oneof payload
    true (merge (parse-root-payload proto))))

(defn parse-offsets
  "Parse a Offsets protobuf message to a map."
  [^cmd.Lrf_calib.JonSharedCmdLrfAlign$Offsets proto]
  (cond-> {}
    ;; Oneof payload
    true (merge (parse-offsets-payload proto))))

(defn parse-set-offsets
  "Parse a SetOffsets protobuf message to a map."
  [^cmd.Lrf_calib.JonSharedCmdLrfAlign$SetOffsets proto]
  (cond-> {}
    ;; Regular fields
    true (assoc :x (.getX proto))
    true (assoc :y (.getY proto))))

(defn parse-shift-offsets-by
  "Parse a ShiftOffsetsBy protobuf message to a map."
  [^cmd.Lrf_calib.JonSharedCmdLrfAlign$ShiftOffsetsBy proto]
  (cond-> {}
    ;; Regular fields
    true (assoc :x (.getX proto))
    true (assoc :y (.getY proto))))

(defn parse-reset-offsets
  "Parse a ResetOffsets protobuf message to a map."
  [^cmd.Lrf_calib.JonSharedCmdLrfAlign$ResetOffsets proto]
  {})

(defn parse-save-offsets
  "Parse a SaveOffsets protobuf message to a map."
  [^cmd.Lrf_calib.JonSharedCmdLrfAlign$SaveOffsets proto]
  {})

(defn parse-root
  "Parse a Root protobuf message to a map."
  [^cmd.System.JonSharedCmdSystem$Root proto]
  (cond-> {}
    ;; Oneof payload
    true (merge (parse-root-payload proto))))

(defn parse-start-a-ll
  "Parse a StartALl protobuf message to a map."
  [^cmd.System.JonSharedCmdSystem$StartALl proto]
  {})

(defn parse-stop-a-ll
  "Parse a StopALl protobuf message to a map."
  [^cmd.System.JonSharedCmdSystem$StopALl proto]
  {})

(defn parse-reboot
  "Parse a Reboot protobuf message to a map."
  [^cmd.System.JonSharedCmdSystem$Reboot proto]
  {})

(defn parse-power-off
  "Parse a PowerOff protobuf message to a map."
  [^cmd.System.JonSharedCmdSystem$PowerOff proto]
  {})

(defn parse-reset-configs
  "Parse a ResetConfigs protobuf message to a map."
  [^cmd.System.JonSharedCmdSystem$ResetConfigs proto]
  {})

(defn parse-start-rec
  "Parse a StartRec protobuf message to a map."
  [^cmd.System.JonSharedCmdSystem$StartRec proto]
  {})

(defn parse-stop-rec
  "Parse a StopRec protobuf message to a map."
  [^cmd.System.JonSharedCmdSystem$StopRec proto]
  {})

(defn parse-mark-rec-important
  "Parse a MarkRecImportant protobuf message to a map."
  [^cmd.System.JonSharedCmdSystem$MarkRecImportant proto]
  {})

(defn parse-unmark-rec-important
  "Parse a UnmarkRecImportant protobuf message to a map."
  [^cmd.System.JonSharedCmdSystem$UnmarkRecImportant proto]
  {})

(defn parse-enter-transport
  "Parse a EnterTransport protobuf message to a map."
  [^cmd.System.JonSharedCmdSystem$EnterTransport proto]
  {})

(defn parse-enable-geodesic-mode
  "Parse a EnableGeodesicMode protobuf message to a map."
  [^cmd.System.JonSharedCmdSystem$EnableGeodesicMode proto]
  {})

(defn parse-disable-geodesic-mode
  "Parse a DisableGeodesicMode protobuf message to a map."
  [^cmd.System.JonSharedCmdSystem$DisableGeodesicMode proto]
  {})

(defn parse-set-localization
  "Parse a SetLocalization protobuf message to a map."
  [^cmd.System.JonSharedCmdSystem$SetLocalization proto]
  (cond-> {}
    ;; Regular fields
    true (assoc :loc
           (get types/jon-gui-data-system-localizations-keywords
                (.getLoc proto)))))

(defn parse-root
  "Parse a Root protobuf message to a map."
  [^cmd.CV.JonSharedCmdCv$Root proto]
  (cond-> {}
    ;; Oneof payload
    true (merge (parse-root-payload proto))))

(defn parse-vampire-mode-enable
  "Parse a VampireModeEnable protobuf message to a map."
  [^cmd.CV.JonSharedCmdCv$VampireModeEnable proto]
  {})

(defn parse-dump-start
  "Parse a DumpStart protobuf message to a map."
  [^cmd.CV.JonSharedCmdCv$DumpStart proto]
  {})

(defn parse-dump-stop
  "Parse a DumpStop protobuf message to a map."
  [^cmd.CV.JonSharedCmdCv$DumpStop proto]
  {})

(defn parse-vampire-mode-disable
  "Parse a VampireModeDisable protobuf message to a map."
  [^cmd.CV.JonSharedCmdCv$VampireModeDisable proto]
  {})

(defn parse-stabilization-mode-enable
  "Parse a StabilizationModeEnable protobuf message to a map."
  [^cmd.CV.JonSharedCmdCv$StabilizationModeEnable proto]
  {})

(defn parse-stabilization-mode-disable
  "Parse a StabilizationModeDisable protobuf message to a map."
  [^cmd.CV.JonSharedCmdCv$StabilizationModeDisable proto]
  {})

(defn parse-set-auto-focus
  "Parse a SetAutoFocus protobuf message to a map."
  [^cmd.CV.JonSharedCmdCv$SetAutoFocus proto]
  (cond-> {}
    ;; Regular fields
    true (assoc :channel
           (get types/jon-gui-data-video-channel-keywords (.getChannel proto)))
    true (assoc :value (.getValue proto))))

(defn parse-start-track-ndc
  "Parse a StartTrackNDC protobuf message to a map."
  [^cmd.CV.JonSharedCmdCv$StartTrackNDC proto]
  (cond-> {}
    ;; Regular fields
    true (assoc :channel
           (get types/jon-gui-data-video-channel-keywords (.getChannel proto)))
    true (assoc :x (.getX proto))
    true (assoc :y (.getY proto))
    true (assoc :frame-time (.getFrameTime proto))))

(defn parse-stop-track
  "Parse a StopTrack protobuf message to a map."
  [^cmd.CV.JonSharedCmdCv$StopTrack proto]
  {})

(defn parse-root
  "Parse a Root protobuf message to a map."
  [^cmd.DayCamGlassHeater.JonSharedCmdDayCamGlassHeater$Root proto]
  (cond-> {}
    ;; Oneof payload
    true (merge (parse-root-payload proto))))

(defn parse-start
  "Parse a Start protobuf message to a map."
  [^cmd.DayCamGlassHeater.JonSharedCmdDayCamGlassHeater$Start proto]
  {})

(defn parse-stop
  "Parse a Stop protobuf message to a map."
  [^cmd.DayCamGlassHeater.JonSharedCmdDayCamGlassHeater$Stop proto]
  {})

(defn parse-turn-on
  "Parse a TurnOn protobuf message to a map."
  [^cmd.DayCamGlassHeater.JonSharedCmdDayCamGlassHeater$TurnOn proto]
  {})

(defn parse-turn-off
  "Parse a TurnOff protobuf message to a map."
  [^cmd.DayCamGlassHeater.JonSharedCmdDayCamGlassHeater$TurnOff proto]
  {})

(defn parse-get-meteo
  "Parse a GetMeteo protobuf message to a map."
  [^cmd.DayCamGlassHeater.JonSharedCmdDayCamGlassHeater$GetMeteo proto]
  {})

(defn parse-root
  "Parse a Root protobuf message to a map."
  [^cmd.Lira.JonSharedCmdLira$Root proto]
  (cond-> {}
    ;; Oneof payload
    true (merge (parse-root-payload proto))))

(defn parse-refine-target
  "Parse a Refine_target protobuf message to a map."
  [^cmd.Lira.JonSharedCmdLira$Refine_target proto]
  (cond-> {}
    ;; Regular fields
    (.hasTarget proto) (assoc :target
                         (parse-jon-gui-data-lira-target (.getTarget proto)))))

(defn parse-jon-gui-data-lira-target
  "Parse a JonGuiDataLiraTarget protobuf message to a map."
  [^cmd.Lira.JonSharedCmdLira$JonGuiDataLiraTarget proto]
  (cond-> {}
    ;; Regular fields
    true (assoc :timestamp (.getTimestamp proto))
    true (assoc :target-longitude (.getTargetLongitude proto))
    true (assoc :target-latitude (.getTargetLatitude proto))
    true (assoc :target-altitude (.getTargetAltitude proto))
    true (assoc :target-azimuth (.getTargetAzimuth proto))
    true (assoc :target-elevation (.getTargetElevation proto))
    true (assoc :distance (.getDistance proto))
    true (assoc :uuid-part-1 (.getUuidPart1 proto))
    true (assoc :uuid-part-2 (.getUuidPart2 proto))
    true (assoc :uuid-part-3 (.getUuidPart3 proto))
    true (assoc :uuid-part-4 (.getUuidPart4 proto))))

(defn parse-root
  "Parse a Root protobuf message to a map."
  [^cmd.JonSharedCmd$Root proto]
  (cond-> {}
    ;; Regular fields
    true (assoc :protocol-version (.getProtocolVersion proto))
    true (assoc :session-id (.getSessionId proto))
    true (assoc :important (.getImportant proto))
    true (assoc :from-cv-subsystem (.getFromCvSubsystem proto))
    true (assoc :client-type
           (get types/jon-gui-data-client-type-keywords (.getClientType proto)))
    ;; Oneof payload
    true (merge (parse-root-payload proto))))

(defn parse-ping
  "Parse a Ping protobuf message to a map."
  [^cmd.JonSharedCmd$Ping proto]
  {})

(defn parse-noop
  "Parse a Noop protobuf message to a map."
  [^cmd.JonSharedCmd$Noop proto]
  {})

(defn parse-frozen
  "Parse a Frozen protobuf message to a map."
  [^cmd.JonSharedCmd$Frozen proto]
  {})

(defn build-root-payload
  "Build the oneof payload for Root."
  [builder [field-key value]]
  (case field-key
    :start (.setStart builder (compass/build-start value))
    :stop (.setStop builder (compass/build-stop value))
    :set-magnetic-declination (.setSetMagneticDeclination
                                builder
                                (compass/build-set-magnetic-declination value))
    :set-offset-angle-azimuth (.setSetOffsetAngleAzimuth
                                builder
                                (compass/build-set-offset-angle-azimuth value))
    :set-offset-angle-elevation (.setSetOffsetAngleElevation
                                  builder
                                  (compass/build-set-offset-angle-elevation
                                    value))
    :set-use-rotary-position (.setSetUseRotaryPosition
                               builder
                               (compass/build-set-use-rotary-position value))
    :start-calibrate-long (.setStartCalibrateLong
                            builder
                            (compass/build-calibrate-start-long value))
    :start-calibrate-short (.setStartCalibrateShort
                             builder
                             (compass/build-calibrate-start-short value))
    :calibrate-next (.setCalibrateNext builder
                                       (compass/build-calibrate-next value))
    :calibrate-cencel
      (.setCalibrateCencel builder (compass/build-calibrate-cencel value))
    :get-meteo (.setGetMeteo builder (compass/build-get-meteo value))
    (throw (ex-info "Unknown oneof field" {:field field-key, :oneof ":cmd"}))))

(defn build-root-payload
  "Build the oneof payload for Root."
  [builder [field-key value]]
  (case field-key
    :start (.setStart builder (gps/build-start value))
    :stop (.setStop builder (gps/build-stop value))
    :set-manual-position
      (.setSetManualPosition builder (gps/build-set-manual-position value))
    :set-use-manual-position (.setSetUseManualPosition
                               builder
                               (gps/build-set-use-manual-position value))
    :get-meteo (.setGetMeteo builder (gps/build-get-meteo value))
    (throw (ex-info "Unknown oneof field" {:field field-key, :oneof ":cmd"}))))

(defn build-root-payload
  "Build the oneof payload for Root."
  [builder [field-key value]]
  (case field-key
    :measure (.setMeasure builder (lrf/build-measure value))
    :scan-on (.setScanOn builder (lrf/build-scan-on value))
    :scan-off (.setScanOff builder (lrf/build-scan-off value))
    :start (.setStart builder (lrf/build-start value))
    :stop (.setStop builder (lrf/build-stop value))
    :target-designator-off
      (.setTargetDesignatorOff builder (lrf/build-target-designator-off value))
    :target-designator-on-mode-a (.setTargetDesignatorOnModeA
                                   builder
                                   (lrf/build-target-designator-on-mode-a
                                     value))
    :target-designator-on-mode-b (.setTargetDesignatorOnModeB
                                   builder
                                   (lrf/build-target-designator-on-mode-b
                                     value))
    :enable-fog-mode (.setEnableFogMode builder
                                        (lrf/build-enable-fog-mode value))
    :disable-fog-mode (.setDisableFogMode builder
                                          (lrf/build-disable-fog-mode value))
    :set-scan-mode (.setSetScanMode builder (lrf/build-set-scan-mode value))
    :new-session (.setNewSession builder (lrf/build-new-session value))
    :get-meteo (.setGetMeteo builder (lrf/build-get-meteo value))
    :refine-on (.setRefineOn builder (lrf/build-refine-on value))
    :refine-off (.setRefineOff builder (lrf/build-refine-off value))
    (throw (ex-info "Unknown oneof field" {:field field-key, :oneof ":cmd"}))))

(defn build-root-payload
  "Build the oneof payload for Root."
  [builder [field-key value]]
  (case field-key
    :focus (.setFocus builder (daycamera/build-focus value))
    :zoom (.setZoom builder (daycamera/build-zoom value))
    :set-iris (.setSetIris builder (daycamera/build-set-iris value))
    :set-infra-red-filter (.setSetInfraRedFilter
                            builder
                            (daycamera/build-set-infra-red-filter value))
    :start (.setStart builder (daycamera/build-start value))
    :stop (.setStop builder (daycamera/build-stop value))
    :photo (.setPhoto builder (daycamera/build-photo value))
    :set-auto-iris (.setSetAutoIris builder
                                    (daycamera/build-set-auto-iris value))
    :halt-all (.setHaltAll builder (daycamera/build-halt-all value))
    :set-fx-mode (.setSetFxMode builder (daycamera/build-set-fx-mode value))
    :next-fx-mode (.setNextFxMode builder (daycamera/build-next-fx-mode value))
    :prev-fx-mode (.setPrevFxMode builder (daycamera/build-prev-fx-mode value))
    :get-meteo (.setGetMeteo builder (daycamera/build-get-meteo value))
    :refresh-fx-mode (.setRefreshFxMode builder
                                        (daycamera/build-refresh-fx-mode value))
    :set-digital-zoom-level (.setSetDigitalZoomLevel
                              builder
                              (daycamera/build-set-digital-zoom-level value))
    :set-clahe-level (.setSetClaheLevel builder
                                        (daycamera/build-set-clahe-level value))
    :shift-clahe-level
      (.setShiftClaheLevel builder (daycamera/build-shift-clahe-level value))
    (throw (ex-info "Unknown oneof field" {:field field-key, :oneof ":cmd"}))))

(defn build-focus-payload
  "Build the oneof payload for Focus."
  [builder [field-key value]]
  (case field-key
    :set-value (.setSetValue builder (daycamera/build-set-value value))
    :move (.setMove builder (daycamera/build-move value))
    :halt (.setHalt builder (daycamera/build-halt value))
    :offset (.setOffset builder (daycamera/build-offset value))
    :reset-focus (.setResetFocus builder (daycamera/build-reset-focus value))
    :save-to-table-focus
      (.setSaveToTableFocus builder (daycamera/build-save-to-table-focus value))
    (throw (ex-info "Unknown oneof field" {:field field-key, :oneof ":cmd"}))))

(defn build-zoom-payload
  "Build the oneof payload for Zoom."
  [builder [field-key value]]
  (case field-key
    :set-value (.setSetValue builder (daycamera/build-set-value value))
    :move (.setMove builder (daycamera/build-move value))
    :halt (.setHalt builder (daycamera/build-halt value))
    :set-zoom-table-value (.setSetZoomTableValue
                            builder
                            (daycamera/build-set-zoom-table-value value))
    :next-zoom-table-pos
      (.setNextZoomTablePos builder (daycamera/build-next-zoom-table-pos value))
    :prev-zoom-table-pos
      (.setPrevZoomTablePos builder (daycamera/build-prev-zoom-table-pos value))
    :offset (.setOffset builder (daycamera/build-offset value))
    :reset-zoom (.setResetZoom builder (daycamera/build-reset-zoom value))
    :save-to-table (.setSaveToTable builder
                                    (daycamera/build-save-to-table value))
    (throw (ex-info "Unknown oneof field" {:field field-key, :oneof ":cmd"}))))

(defn build-root-payload
  "Build the oneof payload for Root."
  [builder [field-key value]]
  (case field-key
    :zoom (.setZoom builder (heatcamera/build-zoom value))
    :set-agc (.setSetAgc builder (heatcamera/build-set-agc value))
    :set-filter (.setSetFilter builder (heatcamera/build-set-filters value))
    :start (.setStart builder (heatcamera/build-start value))
    :stop (.setStop builder (heatcamera/build-stop value))
    :photo (.setPhoto builder (heatcamera/build-photo value))
    :zoom-in (.setZoomIn builder (heatcamera/build-zoom-in value))
    :zoom-out (.setZoomOut builder (heatcamera/build-zoom-out value))
    :zoom-stop (.setZoomStop builder (heatcamera/build-zoom-stop value))
    :focus-in (.setFocusIn builder (heatcamera/build-focus-in value))
    :focus-out (.setFocusOut builder (heatcamera/build-focus-out value))
    :focus-stop (.setFocusStop builder (heatcamera/build-focus-stop value))
    :calibrate (.setCalibrate builder (heatcamera/build-calibrate value))
    :set-dde-level (.setSetDdeLevel builder
                                    (heatcamera/build-set-dde-level value))
    :enable-dde (.setEnableDde builder (heatcamera/build-enable-dde value))
    :disable-dde (.setDisableDde builder (heatcamera/build-disable-dde value))
    :set-auto-focus (.setSetAutoFocus builder
                                      (heatcamera/build-set-auto-focus value))
    :focus-step-plus
      (.setFocusStepPlus builder (heatcamera/build-focus-step-plus value))
    :focus-step-minus
      (.setFocusStepMinus builder (heatcamera/build-focus-step-minus value))
    :set-fx-mode (.setSetFxMode builder (heatcamera/build-set-fx-mode value))
    :next-fx-mode (.setNextFxMode builder (heatcamera/build-next-fx-mode value))
    :prev-fx-mode (.setPrevFxMode builder (heatcamera/build-prev-fx-mode value))
    :get-meteo (.setGetMeteo builder (heatcamera/build-get-meteo value))
    :shift-dde (.setShiftDde builder (heatcamera/build-shift-dde value))
    :refresh-fx-mode
      (.setRefreshFxMode builder (heatcamera/build-refresh-fx-mode value))
    :reset-zoom (.setResetZoom builder (heatcamera/build-reset-zoom value))
    :save-to-table (.setSaveToTable builder
                                    (heatcamera/build-save-to-table value))
    :set-calib-mode (.setSetCalibMode builder
                                      (heatcamera/build-set-calib-mode value))
    :set-digital-zoom-level (.setSetDigitalZoomLevel
                              builder
                              (heatcamera/build-set-digital-zoom-level value))
    :set-clahe-level
      (.setSetClaheLevel builder (heatcamera/build-set-clahe-level value))
    :shift-clahe-level
      (.setShiftClaheLevel builder (heatcamera/build-shift-clahe-level value))
    (throw (ex-info "Unknown oneof field" {:field field-key, :oneof ":cmd"}))))

(defn build-zoom-payload
  "Build the oneof payload for Zoom."
  [builder [field-key value]]
  (case field-key
    :set-zoom-table-value (.setSetZoomTableValue
                            builder
                            (heatcamera/build-set-zoom-table-value value))
    :next-zoom-table-pos (.setNextZoomTablePos
                           builder
                           (heatcamera/build-next-zoom-table-pos value))
    :prev-zoom-table-pos (.setPrevZoomTablePos
                           builder
                           (heatcamera/build-prev-zoom-table-pos value))
    (throw (ex-info "Unknown oneof field" {:field field-key, :oneof ":cmd"}))))

(defn build-root-payload
  "Build the oneof payload for Root."
  [builder [field-key value]]
  (case field-key
    :start (.setStart builder (rotaryplatform/build-start value))
    :stop (.setStop builder (rotaryplatform/build-stop value))
    :axis (.setAxis builder (rotaryplatform/build-axis value))
    :set-platform-azimuth (.setSetPlatformAzimuth
                            builder
                            (rotaryplatform/build-set-platform-azimuth value))
    :set-platform-elevation (.setSetPlatformElevation
                              builder
                              (rotaryplatform/build-set-platform-elevation
                                value))
    :set-platform-bank (.setSetPlatformBank
                         builder
                         (rotaryplatform/build-set-platform-bank value))
    :halt (.setHalt builder (rotaryplatform/build-halt value))
    :set-use-rotary-as-compass (.setSetUseRotaryAsCompass
                                 builder
                                 (rotaryplatform/build-set-use-rotary-as-compass
                                   value))
    :rotate-to-gps (.setRotateToGps builder
                                    (rotaryplatform/build-rotate-to-gps value))
    :set-origin-gps
      (.setSetOriginGps builder (rotaryplatform/build-set-origin-gps value))
    :set-mode (.setSetMode builder (rotaryplatform/build-set-mode value))
    :rotate-to-ndc (.setRotateToNdc builder
                                    (rotaryplatform/build-rotate-to-ndc value))
    :scan-start (.setScanStart builder (rotaryplatform/build-scan-start value))
    :scan-stop (.setScanStop builder (rotaryplatform/build-scan-stop value))
    :scan-pause (.setScanPause builder (rotaryplatform/build-scan-pause value))
    :scan-unpause (.setScanUnpause builder
                                   (rotaryplatform/build-scan-unpause value))
    :get-meteo (.setGetMeteo builder (rotaryplatform/build-get-meteo value))
    :scan-prev (.setScanPrev builder (rotaryplatform/build-scan-prev value))
    :scan-next (.setScanNext builder (rotaryplatform/build-scan-next value))
    :scan-refresh-node-list (.setScanRefreshNodeList
                              builder
                              (rotaryplatform/build-scan-refresh-node-list
                                value))
    :scan-select-node
      (.setScanSelectNode builder (rotaryplatform/build-scan-select-node value))
    :scan-delete-node
      (.setScanDeleteNode builder (rotaryplatform/build-scan-delete-node value))
    :scan-update-node
      (.setScanUpdateNode builder (rotaryplatform/build-scan-update-node value))
    :scan-add-node (.setScanAddNode builder
                                    (rotaryplatform/build-scan-add-node value))
    (throw (ex-info "Unknown oneof field" {:field field-key, :oneof ":cmd"}))))

(defn build-azimuth-payload
  "Build the oneof payload for Azimuth."
  [builder [field-key value]]
  (case field-key
    :set-value (.setSetValue builder
                             (rotaryplatform/build-set-azimuth-value value))
    :rotate-to (.setRotateTo builder
                             (rotaryplatform/build-rotate-azimuth-to value))
    :rotate (.setRotate builder (rotaryplatform/build-rotate-azimuth value))
    :relative (.setRelative builder
                            (rotaryplatform/build-rotate-azimuth-relative
                              value))
    :relative-set (.setRelativeSet
                    builder
                    (rotaryplatform/build-rotate-azimuth-relative-set value))
    :halt (.setHalt builder (rotaryplatform/build-halt-azimuth value))
    (throw (ex-info "Unknown oneof field" {:field field-key, :oneof ":cmd"}))))

(defn build-elevation-payload
  "Build the oneof payload for Elevation."
  [builder [field-key value]]
  (case field-key
    :set-value (.setSetValue builder
                             (rotaryplatform/build-set-elevation-value value))
    :rotate-to (.setRotateTo builder
                             (rotaryplatform/build-rotate-elevation-to value))
    :rotate (.setRotate builder (rotaryplatform/build-rotate-elevation value))
    :relative (.setRelative builder
                            (rotaryplatform/build-rotate-elevation-relative
                              value))
    :relative-set (.setRelativeSet
                    builder
                    (rotaryplatform/build-rotate-elevation-relative-set value))
    :halt (.setHalt builder (rotaryplatform/build-halt-elevation value))
    (throw (ex-info "Unknown oneof field" {:field field-key, :oneof ":cmd"}))))

(defn build-root-payload
  "Build the oneof payload for Root."
  [builder [field-key value]]
  (case field-key
    :show-default-screen
      (.setShowDefaultScreen builder (osd/build-show-default-screen value))
    :show-lrf-measure-screen (.setShowLrfMeasureScreen
                               builder
                               (osd/build-show-lrf-measure-screen value))
    :show-lrf-result-screen
      (.setShowLrfResultScreen builder (osd/build-show-lrf-result-screen value))
    :show-lrf-result-simplified-screen
      (.setShowLrfResultSimplifiedScreen
        builder
        (osd/build-show-lrf-result-simplified-screen value))
    :enable-heat-osd (.setEnableHeatOsd builder
                                        (osd/build-enable-heat-osd value))
    :disable-heat-osd (.setDisableHeatOsd builder
                                          (osd/build-disable-heat-osd value))
    :enable-day-osd (.setEnableDayOsd builder (osd/build-enable-day-osd value))
    :disable-day-osd (.setDisableDayOsd builder
                                        (osd/build-disable-day-osd value))
    (throw (ex-info "Unknown oneof field" {:field field-key, :oneof ":cmd"}))))

(defn build-root-payload
  "Build the oneof payload for Root."
  [builder [field-key value]]
  (case field-key
    :day (.setDay builder (lrf-calib/build-offsets value))
    :heat (.setHeat builder (lrf-calib/build-offsets value))
    (throw (ex-info "Unknown oneof field"
                    {:field field-key, :oneof ":channel"}))))

(defn build-offsets-payload
  "Build the oneof payload for Offsets."
  [builder [field-key value]]
  (case field-key
    :set (.setSet builder (lrf-calib/build-set-offsets value))
    :save (.setSave builder (lrf-calib/build-save-offsets value))
    :reset (.setReset builder (lrf-calib/build-reset-offsets value))
    :shift (.setShift builder (lrf-calib/build-shift-offsets-by value))
    (throw (ex-info "Unknown oneof field" {:field field-key, :oneof ":cmd"}))))

(defn build-root-payload
  "Build the oneof payload for Root."
  [builder [field-key value]]
  (case field-key
    :start-all (.setStartAll builder (system/build-start-a-ll value))
    :stop-all (.setStopAll builder (system/build-stop-a-ll value))
    :reboot (.setReboot builder (system/build-reboot value))
    :power-off (.setPowerOff builder (system/build-power-off value))
    :localization (.setLocalization builder
                                    (system/build-set-localization value))
    :reset-configs (.setResetConfigs builder (system/build-reset-configs value))
    :start-rec (.setStartRec builder (system/build-start-rec value))
    :stop-rec (.setStopRec builder (system/build-stop-rec value))
    :mark-rec-important
      (.setMarkRecImportant builder (system/build-mark-rec-important value))
    :unmark-rec-important
      (.setUnmarkRecImportant builder (system/build-unmark-rec-important value))
    :enter-transport (.setEnterTransport builder
                                         (system/build-enter-transport value))
    :geodesic-mode-enable
      (.setGeodesicModeEnable builder (system/build-enable-geodesic-mode value))
    :geodesic-mode-disable (.setGeodesicModeDisable
                             builder
                             (system/build-disable-geodesic-mode value))
    (throw (ex-info "Unknown oneof field" {:field field-key, :oneof ":cmd"}))))

(defn build-root-payload
  "Build the oneof payload for Root."
  [builder [field-key value]]
  (case field-key
    :set-auto-focus (.setSetAutoFocus builder (cv/build-set-auto-focus value))
    :start-track-ndc (.setStartTrackNdc builder
                                        (cv/build-start-track-ndc value))
    :stop-track (.setStopTrack builder (cv/build-stop-track value))
    :vampire-mode-enable
      (.setVampireModeEnable builder (cv/build-vampire-mode-enable value))
    :vampire-mode-disable
      (.setVampireModeDisable builder (cv/build-vampire-mode-disable value))
    :stabilization-mode-enable (.setStabilizationModeEnable
                                 builder
                                 (cv/build-stabilization-mode-enable value))
    :stabilization-mode-disable (.setStabilizationModeDisable
                                  builder
                                  (cv/build-stabilization-mode-disable value))
    :dump-start (.setDumpStart builder (cv/build-dump-start value))
    :dump-stop (.setDumpStop builder (cv/build-dump-stop value))
    (throw (ex-info "Unknown oneof field" {:field field-key, :oneof ":cmd"}))))

(defn build-root-payload
  "Build the oneof payload for Root."
  [builder [field-key value]]
  (case field-key
    :start (.setStart builder (daycamglassheater/build-start value))
    :stop (.setStop builder (daycamglassheater/build-stop value))
    :turn-on (.setTurnOn builder (daycamglassheater/build-turn-on value))
    :turn-off (.setTurnOff builder (daycamglassheater/build-turn-off value))
    :get-meteo (.setGetMeteo builder (daycamglassheater/build-get-meteo value))
    (throw (ex-info "Unknown oneof field" {:field field-key, :oneof ":cmd"}))))

(defn build-root-payload
  "Build the oneof payload for Root."
  [builder [field-key value]]
  (case field-key
    :refine-target (.setRefineTarget builder (lira/build-refine-target value))
    (throw (ex-info "Unknown oneof field" {:field field-key, :oneof ":cmd"}))))

(defn build-root-payload
  "Build the oneof payload for Root."
  [builder [field-key value]]
  (case field-key
    :day-camera (.setDayCamera builder (daycamera/build-root value))
    :heat-camera (.setHeatCamera builder (heatcamera/build-root value))
    :gps (.setGps builder (gps/build-root value))
    :compass (.setCompass builder (compass/build-root value))
    :lrf (.setLrf builder (lrf/build-root value))
    :lrf-calib (.setLrfCalib builder (lrf-calib/build-root value))
    :rotary (.setRotary builder (rotaryplatform/build-root value))
    :osd (.setOsd builder (osd/build-root value))
    :ping (.setPing builder (build-ping value))
    :noop (.setNoop builder (build-noop value))
    :frozen (.setFrozen builder (build-frozen value))
    :system (.setSystem builder (system/build-root value))
    :cv (.setCv builder (cv/build-root value))
    :day-cam-glass-heater
      (.setDayCamGlassHeater builder (daycamglassheater/build-root value))
    :lira (.setLira builder (lira/build-root value))
    (throw (ex-info "Unknown oneof field"
                    {:field field-key, :oneof ":payload"}))))

(defn parse-root-payload
  "Parse the oneof payload from Root."
  [^cmd.Compass.JonSharedCmdCompass$Root proto]
  (cond (.hasStart proto) {:start (compass/parse-start (.getStart proto))}
        (.hasStop proto) {:stop (compass/parse-stop (.getStop proto))}
        (.hasSetMagneticDeclination proto)
          {:set-magnetic-declination (compass/parse-set-magnetic-declination
                                       (.getSetMagneticDeclination proto))}
        (.hasSetOffsetAngleAzimuth proto)
          {:set-offset-angle-azimuth (compass/parse-set-offset-angle-azimuth
                                       (.getSetOffsetAngleAzimuth proto))}
        (.hasSetOffsetAngleElevation proto)
          {:set-offset-angle-elevation (compass/parse-set-offset-angle-elevation
                                         (.getSetOffsetAngleElevation proto))}
        (.hasSetUseRotaryPosition proto)
          {:set-use-rotary-position (compass/parse-set-use-rotary-position
                                      (.getSetUseRotaryPosition proto))}
        (.hasStartCalibrateLong proto) {:start-calibrate-long
                                          (compass/parse-calibrate-start-long
                                            (.getStartCalibrateLong proto))}
        (.hasStartCalibrateShort proto) {:start-calibrate-short
                                           (compass/parse-calibrate-start-short
                                             (.getStartCalibrateShort proto))}
        (.hasCalibrateNext proto) {:calibrate-next (compass/parse-calibrate-next
                                                     (.getCalibrateNext proto))}
        (.hasCalibrateCencel proto) {:calibrate-cencel
                                       (compass/parse-calibrate-cencel
                                         (.getCalibrateCencel proto))}
        (.hasGetMeteo proto) {:get-meteo (compass/parse-get-meteo (.getGetMeteo
                                                                    proto))}))

(defn parse-root-payload
  "Parse the oneof payload from Root."
  [^cmd.Gps.JonSharedCmdGps$Root proto]
  (cond (.hasStart proto) {:start (gps/parse-start (.getStart proto))}
        (.hasStop proto) {:stop (gps/parse-stop (.getStop proto))}
        (.hasSetManualPosition proto) {:set-manual-position
                                         (gps/parse-set-manual-position
                                           (.getSetManualPosition proto))}
        (.hasSetUseManualPosition proto) {:set-use-manual-position
                                            (gps/parse-set-use-manual-position
                                              (.getSetUseManualPosition proto))}
        (.hasGetMeteo proto) {:get-meteo (gps/parse-get-meteo (.getGetMeteo
                                                                proto))}))

(defn parse-root-payload
  "Parse the oneof payload from Root."
  [^cmd.Lrf.JonSharedCmdLrf$Root proto]
  (cond
    (.hasMeasure proto) {:measure (lrf/parse-measure (.getMeasure proto))}
    (.hasScanOn proto) {:scan-on (lrf/parse-scan-on (.getScanOn proto))}
    (.hasScanOff proto) {:scan-off (lrf/parse-scan-off (.getScanOff proto))}
    (.hasStart proto) {:start (lrf/parse-start (.getStart proto))}
    (.hasStop proto) {:stop (lrf/parse-stop (.getStop proto))}
    (.hasTargetDesignatorOff proto) {:target-designator-off
                                       (lrf/parse-target-designator-off
                                         (.getTargetDesignatorOff proto))}
    (.hasTargetDesignatorOnModeA proto)
      {:target-designator-on-mode-a (lrf/parse-target-designator-on-mode-a
                                      (.getTargetDesignatorOnModeA proto))}
    (.hasTargetDesignatorOnModeB proto)
      {:target-designator-on-mode-b (lrf/parse-target-designator-on-mode-b
                                      (.getTargetDesignatorOnModeB proto))}
    (.hasEnableFogMode proto) {:enable-fog-mode (lrf/parse-enable-fog-mode
                                                  (.getEnableFogMode proto))}
    (.hasDisableFogMode proto) {:disable-fog-mode (lrf/parse-disable-fog-mode
                                                    (.getDisableFogMode proto))}
    (.hasSetScanMode proto) {:set-scan-mode (lrf/parse-set-scan-mode
                                              (.getSetScanMode proto))}
    (.hasNewSession proto) {:new-session (lrf/parse-new-session (.getNewSession
                                                                  proto))}
    (.hasGetMeteo proto) {:get-meteo (lrf/parse-get-meteo (.getGetMeteo proto))}
    (.hasRefineOn proto) {:refine-on (lrf/parse-refine-on (.getRefineOn proto))}
    (.hasRefineOff proto) {:refine-off (lrf/parse-refine-off (.getRefineOff
                                                               proto))}))

(defn parse-root-payload
  "Parse the oneof payload from Root."
  [^cmd.DayCamera.JonSharedCmdDayCamera$Root proto]
  (cond (.hasFocus proto) {:focus (daycamera/parse-focus (.getFocus proto))}
        (.hasZoom proto) {:zoom (daycamera/parse-zoom (.getZoom proto))}
        (.hasSetIris proto) {:set-iris (daycamera/parse-set-iris (.getSetIris
                                                                   proto))}
        (.hasSetInfraRedFilter proto) {:set-infra-red-filter
                                         (daycamera/parse-set-infra-red-filter
                                           (.getSetInfraRedFilter proto))}
        (.hasStart proto) {:start (daycamera/parse-start (.getStart proto))}
        (.hasStop proto) {:stop (daycamera/parse-stop (.getStop proto))}
        (.hasPhoto proto) {:photo (daycamera/parse-photo (.getPhoto proto))}
        (.hasSetAutoIris proto) {:set-auto-iris (daycamera/parse-set-auto-iris
                                                  (.getSetAutoIris proto))}
        (.hasHaltAll proto) {:halt-all (daycamera/parse-halt-all (.getHaltAll
                                                                   proto))}
        (.hasSetFxMode proto) {:set-fx-mode (daycamera/parse-set-fx-mode
                                              (.getSetFxMode proto))}
        (.hasNextFxMode proto) {:next-fx-mode (daycamera/parse-next-fx-mode
                                                (.getNextFxMode proto))}
        (.hasPrevFxMode proto) {:prev-fx-mode (daycamera/parse-prev-fx-mode
                                                (.getPrevFxMode proto))}
        (.hasGetMeteo proto) {:get-meteo (daycamera/parse-get-meteo
                                           (.getGetMeteo proto))}
        (.hasRefreshFxMode proto) {:refresh-fx-mode
                                     (daycamera/parse-refresh-fx-mode
                                       (.getRefreshFxMode proto))}
        (.hasSetDigitalZoomLevel proto)
          {:set-digital-zoom-level (daycamera/parse-set-digital-zoom-level
                                     (.getSetDigitalZoomLevel proto))}
        (.hasSetClaheLevel proto) {:set-clahe-level
                                     (daycamera/parse-set-clahe-level
                                       (.getSetClaheLevel proto))}
        (.hasShiftClaheLevel proto) {:shift-clahe-level
                                       (daycamera/parse-shift-clahe-level
                                         (.getShiftClaheLevel proto))}))

(defn parse-focus-payload
  "Parse the oneof payload from Focus."
  [^cmd.DayCamera.JonSharedCmdDayCamera$Focus proto]
  (cond (.hasSetValue proto) {:set-value (daycamera/parse-set-value
                                           (.getSetValue proto))}
        (.hasMove proto) {:move (daycamera/parse-move (.getMove proto))}
        (.hasHalt proto) {:halt (daycamera/parse-halt (.getHalt proto))}
        (.hasOffset proto) {:offset (daycamera/parse-offset (.getOffset proto))}
        (.hasResetFocus proto) {:reset-focus (daycamera/parse-reset-focus
                                               (.getResetFocus proto))}
        (.hasSaveToTableFocus proto) {:save-to-table-focus
                                        (daycamera/parse-save-to-table-focus
                                          (.getSaveToTableFocus proto))}))

(defn parse-zoom-payload
  "Parse the oneof payload from Zoom."
  [^cmd.DayCamera.JonSharedCmdDayCamera$Zoom proto]
  (cond (.hasSetValue proto) {:set-value (daycamera/parse-set-value
                                           (.getSetValue proto))}
        (.hasMove proto) {:move (daycamera/parse-move (.getMove proto))}
        (.hasHalt proto) {:halt (daycamera/parse-halt (.getHalt proto))}
        (.hasSetZoomTableValue proto) {:set-zoom-table-value
                                         (daycamera/parse-set-zoom-table-value
                                           (.getSetZoomTableValue proto))}
        (.hasNextZoomTablePos proto) {:next-zoom-table-pos
                                        (daycamera/parse-next-zoom-table-pos
                                          (.getNextZoomTablePos proto))}
        (.hasPrevZoomTablePos proto) {:prev-zoom-table-pos
                                        (daycamera/parse-prev-zoom-table-pos
                                          (.getPrevZoomTablePos proto))}
        (.hasOffset proto) {:offset (daycamera/parse-offset (.getOffset proto))}
        (.hasResetZoom proto) {:reset-zoom (daycamera/parse-reset-zoom
                                             (.getResetZoom proto))}
        (.hasSaveToTable proto) {:save-to-table (daycamera/parse-save-to-table
                                                  (.getSaveToTable proto))}))

(defn parse-root-payload
  "Parse the oneof payload from Root."
  [^cmd.HeatCamera.JonSharedCmdHeatCamera$Root proto]
  (cond
    (.hasZoom proto) {:zoom (heatcamera/parse-zoom (.getZoom proto))}
    (.hasSetAgc proto) {:set-agc (heatcamera/parse-set-agc (.getSetAgc proto))}
    (.hasSetFilter proto) {:set-filter (heatcamera/parse-set-filters
                                         (.getSetFilter proto))}
    (.hasStart proto) {:start (heatcamera/parse-start (.getStart proto))}
    (.hasStop proto) {:stop (heatcamera/parse-stop (.getStop proto))}
    (.hasPhoto proto) {:photo (heatcamera/parse-photo (.getPhoto proto))}
    (.hasZoomIn proto) {:zoom-in (heatcamera/parse-zoom-in (.getZoomIn proto))}
    (.hasZoomOut proto) {:zoom-out (heatcamera/parse-zoom-out (.getZoomOut
                                                                proto))}
    (.hasZoomStop proto) {:zoom-stop (heatcamera/parse-zoom-stop (.getZoomStop
                                                                   proto))}
    (.hasFocusIn proto) {:focus-in (heatcamera/parse-focus-in (.getFocusIn
                                                                proto))}
    (.hasFocusOut proto) {:focus-out (heatcamera/parse-focus-out (.getFocusOut
                                                                   proto))}
    (.hasFocusStop proto) {:focus-stop (heatcamera/parse-focus-stop
                                         (.getFocusStop proto))}
    (.hasCalibrate proto) {:calibrate (heatcamera/parse-calibrate (.getCalibrate
                                                                    proto))}
    (.hasSetDdeLevel proto) {:set-dde-level (heatcamera/parse-set-dde-level
                                              (.getSetDdeLevel proto))}
    (.hasEnableDde proto) {:enable-dde (heatcamera/parse-enable-dde
                                         (.getEnableDde proto))}
    (.hasDisableDde proto) {:disable-dde (heatcamera/parse-disable-dde
                                           (.getDisableDde proto))}
    (.hasSetAutoFocus proto) {:set-auto-focus (heatcamera/parse-set-auto-focus
                                                (.getSetAutoFocus proto))}
    (.hasFocusStepPlus proto) {:focus-step-plus
                                 (heatcamera/parse-focus-step-plus
                                   (.getFocusStepPlus proto))}
    (.hasFocusStepMinus proto) {:focus-step-minus
                                  (heatcamera/parse-focus-step-minus
                                    (.getFocusStepMinus proto))}
    (.hasSetFxMode proto) {:set-fx-mode (heatcamera/parse-set-fx-mode
                                          (.getSetFxMode proto))}
    (.hasNextFxMode proto) {:next-fx-mode (heatcamera/parse-next-fx-mode
                                            (.getNextFxMode proto))}
    (.hasPrevFxMode proto) {:prev-fx-mode (heatcamera/parse-prev-fx-mode
                                            (.getPrevFxMode proto))}
    (.hasGetMeteo proto) {:get-meteo (heatcamera/parse-get-meteo (.getGetMeteo
                                                                   proto))}
    (.hasShiftDde proto) {:shift-dde (heatcamera/parse-shift-dde (.getShiftDde
                                                                   proto))}
    (.hasRefreshFxMode proto) {:refresh-fx-mode
                                 (heatcamera/parse-refresh-fx-mode
                                   (.getRefreshFxMode proto))}
    (.hasResetZoom proto) {:reset-zoom (heatcamera/parse-reset-zoom
                                         (.getResetZoom proto))}
    (.hasSaveToTable proto) {:save-to-table (heatcamera/parse-save-to-table
                                              (.getSaveToTable proto))}
    (.hasSetCalibMode proto) {:set-calib-mode (heatcamera/parse-set-calib-mode
                                                (.getSetCalibMode proto))}
    (.hasSetDigitalZoomLevel proto) {:set-digital-zoom-level
                                       (heatcamera/parse-set-digital-zoom-level
                                         (.getSetDigitalZoomLevel proto))}
    (.hasSetClaheLevel proto) {:set-clahe-level
                                 (heatcamera/parse-set-clahe-level
                                   (.getSetClaheLevel proto))}
    (.hasShiftClaheLevel proto) {:shift-clahe-level
                                   (heatcamera/parse-shift-clahe-level
                                     (.getShiftClaheLevel proto))}))

(defn parse-zoom-payload
  "Parse the oneof payload from Zoom."
  [^cmd.HeatCamera.JonSharedCmdHeatCamera$Zoom proto]
  (cond (.hasSetZoomTableValue proto) {:set-zoom-table-value
                                         (heatcamera/parse-set-zoom-table-value
                                           (.getSetZoomTableValue proto))}
        (.hasNextZoomTablePos proto) {:next-zoom-table-pos
                                        (heatcamera/parse-next-zoom-table-pos
                                          (.getNextZoomTablePos proto))}
        (.hasPrevZoomTablePos proto) {:prev-zoom-table-pos
                                        (heatcamera/parse-prev-zoom-table-pos
                                          (.getPrevZoomTablePos proto))}))

(defn parse-root-payload
  "Parse the oneof payload from Root."
  [^cmd.RotaryPlatform.JonSharedCmdRotary$Root proto]
  (cond
    (.hasStart proto) {:start (rotaryplatform/parse-start (.getStart proto))}
    (.hasStop proto) {:stop (rotaryplatform/parse-stop (.getStop proto))}
    (.hasAxis proto) {:axis (rotaryplatform/parse-axis (.getAxis proto))}
    (.hasSetPlatformAzimuth proto) {:set-platform-azimuth
                                      (rotaryplatform/parse-set-platform-azimuth
                                        (.getSetPlatformAzimuth proto))}
    (.hasSetPlatformElevation proto)
      {:set-platform-elevation (rotaryplatform/parse-set-platform-elevation
                                 (.getSetPlatformElevation proto))}
    (.hasSetPlatformBank proto) {:set-platform-bank
                                   (rotaryplatform/parse-set-platform-bank
                                     (.getSetPlatformBank proto))}
    (.hasHalt proto) {:halt (rotaryplatform/parse-halt (.getHalt proto))}
    (.hasSetUseRotaryAsCompass proto)
      {:set-use-rotary-as-compass
         (rotaryplatform/parse-set-use-rotary-as-compass
           (.getSetUseRotaryAsCompass proto))}
    (.hasRotateToGps proto) {:rotate-to-gps (rotaryplatform/parse-rotate-to-gps
                                              (.getRotateToGps proto))}
    (.hasSetOriginGps proto) {:set-origin-gps
                                (rotaryplatform/parse-set-origin-gps
                                  (.getSetOriginGps proto))}
    (.hasSetMode proto) {:set-mode (rotaryplatform/parse-set-mode (.getSetMode
                                                                    proto))}
    (.hasRotateToNdc proto) {:rotate-to-ndc (rotaryplatform/parse-rotate-to-ndc
                                              (.getRotateToNdc proto))}
    (.hasScanStart proto) {:scan-start (rotaryplatform/parse-scan-start
                                         (.getScanStart proto))}
    (.hasScanStop proto) {:scan-stop (rotaryplatform/parse-scan-stop
                                       (.getScanStop proto))}
    (.hasScanPause proto) {:scan-pause (rotaryplatform/parse-scan-pause
                                         (.getScanPause proto))}
    (.hasScanUnpause proto) {:scan-unpause (rotaryplatform/parse-scan-unpause
                                             (.getScanUnpause proto))}
    (.hasGetMeteo proto) {:get-meteo (rotaryplatform/parse-get-meteo
                                       (.getGetMeteo proto))}
    (.hasScanPrev proto) {:scan-prev (rotaryplatform/parse-scan-prev
                                       (.getScanPrev proto))}
    (.hasScanNext proto) {:scan-next (rotaryplatform/parse-scan-next
                                       (.getScanNext proto))}
    (.hasScanRefreshNodeList proto)
      {:scan-refresh-node-list (rotaryplatform/parse-scan-refresh-node-list
                                 (.getScanRefreshNodeList proto))}
    (.hasScanSelectNode proto) {:scan-select-node
                                  (rotaryplatform/parse-scan-select-node
                                    (.getScanSelectNode proto))}
    (.hasScanDeleteNode proto) {:scan-delete-node
                                  (rotaryplatform/parse-scan-delete-node
                                    (.getScanDeleteNode proto))}
    (.hasScanUpdateNode proto) {:scan-update-node
                                  (rotaryplatform/parse-scan-update-node
                                    (.getScanUpdateNode proto))}
    (.hasScanAddNode proto) {:scan-add-node (rotaryplatform/parse-scan-add-node
                                              (.getScanAddNode proto))}))

(defn parse-azimuth-payload
  "Parse the oneof payload from Azimuth."
  [^cmd.RotaryPlatform.JonSharedCmdRotary$Azimuth proto]
  (cond (.hasSetValue proto) {:set-value (rotaryplatform/parse-set-azimuth-value
                                           (.getSetValue proto))}
        (.hasRotateTo proto) {:rotate-to (rotaryplatform/parse-rotate-azimuth-to
                                           (.getRotateTo proto))}
        (.hasRotate proto) {:rotate (rotaryplatform/parse-rotate-azimuth
                                      (.getRotate proto))}
        (.hasRelative proto) {:relative
                                (rotaryplatform/parse-rotate-azimuth-relative
                                  (.getRelative proto))}
        (.hasRelativeSet proto)
          {:relative-set (rotaryplatform/parse-rotate-azimuth-relative-set
                           (.getRelativeSet proto))}
        (.hasHalt proto) {:halt (rotaryplatform/parse-halt-azimuth (.getHalt
                                                                     proto))}))

(defn parse-elevation-payload
  "Parse the oneof payload from Elevation."
  [^cmd.RotaryPlatform.JonSharedCmdRotary$Elevation proto]
  (cond (.hasSetValue proto) {:set-value
                                (rotaryplatform/parse-set-elevation-value
                                  (.getSetValue proto))}
        (.hasRotateTo proto) {:rotate-to
                                (rotaryplatform/parse-rotate-elevation-to
                                  (.getRotateTo proto))}
        (.hasRotate proto) {:rotate (rotaryplatform/parse-rotate-elevation
                                      (.getRotate proto))}
        (.hasRelative proto) {:relative
                                (rotaryplatform/parse-rotate-elevation-relative
                                  (.getRelative proto))}
        (.hasRelativeSet proto)
          {:relative-set (rotaryplatform/parse-rotate-elevation-relative-set
                           (.getRelativeSet proto))}
        (.hasHalt proto) {:halt (rotaryplatform/parse-halt-elevation
                                  (.getHalt proto))}))

(defn parse-root-payload
  "Parse the oneof payload from Root."
  [^cmd.OSD.JonSharedCmdOsd$Root proto]
  (cond
    (.hasShowDefaultScreen proto) {:show-default-screen
                                     (osd/parse-show-default-screen
                                       (.getShowDefaultScreen proto))}
    (.hasShowLrfMeasureScreen proto) {:show-lrf-measure-screen
                                        (osd/parse-show-lrf-measure-screen
                                          (.getShowLrfMeasureScreen proto))}
    (.hasShowLrfResultScreen proto) {:show-lrf-result-screen
                                       (osd/parse-show-lrf-result-screen
                                         (.getShowLrfResultScreen proto))}
    (.hasShowLrfResultSimplifiedScreen proto)
      {:show-lrf-result-simplified-screen
         (osd/parse-show-lrf-result-simplified-screen
           (.getShowLrfResultSimplifiedScreen proto))}
    (.hasEnableHeatOsd proto) {:enable-heat-osd (osd/parse-enable-heat-osd
                                                  (.getEnableHeatOsd proto))}
    (.hasDisableHeatOsd proto) {:disable-heat-osd (osd/parse-disable-heat-osd
                                                    (.getDisableHeatOsd proto))}
    (.hasEnableDayOsd proto) {:enable-day-osd (osd/parse-enable-day-osd
                                                (.getEnableDayOsd proto))}
    (.hasDisableDayOsd proto) {:disable-day-osd (osd/parse-disable-day-osd
                                                  (.getDisableDayOsd proto))}))

(defn parse-root-payload
  "Parse the oneof payload from Root."
  [^cmd.Lrf_calib.JonSharedCmdLrfAlign$Root proto]
  (cond (.hasDay proto) {:day (lrf-calib/parse-offsets (.getDay proto))}
        (.hasHeat proto) {:heat (lrf-calib/parse-offsets (.getHeat proto))}))

(defn parse-offsets-payload
  "Parse the oneof payload from Offsets."
  [^cmd.Lrf_calib.JonSharedCmdLrfAlign$Offsets proto]
  (cond (.hasSet proto) {:set (lrf-calib/parse-set-offsets (.getSet proto))}
        (.hasSave proto) {:save (lrf-calib/parse-save-offsets (.getSave proto))}
        (.hasReset proto) {:reset (lrf-calib/parse-reset-offsets (.getReset
                                                                   proto))}
        (.hasShift proto) {:shift (lrf-calib/parse-shift-offsets-by (.getShift
                                                                      proto))}))

(defn parse-root-payload
  "Parse the oneof payload from Root."
  [^cmd.System.JonSharedCmdSystem$Root proto]
  (cond
    (.hasStartAll proto) {:start-all (system/parse-start-a-ll (.getStartAll
                                                                proto))}
    (.hasStopAll proto) {:stop-all (system/parse-stop-a-ll (.getStopAll proto))}
    (.hasReboot proto) {:reboot (system/parse-reboot (.getReboot proto))}
    (.hasPowerOff proto) {:power-off (system/parse-power-off (.getPowerOff
                                                               proto))}
    (.hasLocalization proto) {:localization (system/parse-set-localization
                                              (.getLocalization proto))}
    (.hasResetConfigs proto) {:reset-configs (system/parse-reset-configs
                                               (.getResetConfigs proto))}
    (.hasStartRec proto) {:start-rec (system/parse-start-rec (.getStartRec
                                                               proto))}
    (.hasStopRec proto) {:stop-rec (system/parse-stop-rec (.getStopRec proto))}
    (.hasMarkRecImportant proto) {:mark-rec-important
                                    (system/parse-mark-rec-important
                                      (.getMarkRecImportant proto))}
    (.hasUnmarkRecImportant proto) {:unmark-rec-important
                                      (system/parse-unmark-rec-important
                                        (.getUnmarkRecImportant proto))}
    (.hasEnterTransport proto) {:enter-transport (system/parse-enter-transport
                                                   (.getEnterTransport proto))}
    (.hasGeodesicModeEnable proto) {:geodesic-mode-enable
                                      (system/parse-enable-geodesic-mode
                                        (.getGeodesicModeEnable proto))}
    (.hasGeodesicModeDisable proto) {:geodesic-mode-disable
                                       (system/parse-disable-geodesic-mode
                                         (.getGeodesicModeDisable proto))}))

(defn parse-root-payload
  "Parse the oneof payload from Root."
  [^cmd.CV.JonSharedCmdCv$Root proto]
  (cond (.hasSetAutoFocus proto) {:set-auto-focus (cv/parse-set-auto-focus
                                                    (.getSetAutoFocus proto))}
        (.hasStartTrackNdc proto) {:start-track-ndc (cv/parse-start-track-ndc
                                                      (.getStartTrackNdc
                                                        proto))}
        (.hasStopTrack proto) {:stop-track (cv/parse-stop-track (.getStopTrack
                                                                  proto))}
        (.hasVampireModeEnable proto) {:vampire-mode-enable
                                         (cv/parse-vampire-mode-enable
                                           (.getVampireModeEnable proto))}
        (.hasVampireModeDisable proto) {:vampire-mode-disable
                                          (cv/parse-vampire-mode-disable
                                            (.getVampireModeDisable proto))}
        (.hasStabilizationModeEnable proto)
          {:stabilization-mode-enable (cv/parse-stabilization-mode-enable
                                        (.getStabilizationModeEnable proto))}
        (.hasStabilizationModeDisable proto)
          {:stabilization-mode-disable (cv/parse-stabilization-mode-disable
                                         (.getStabilizationModeDisable proto))}
        (.hasDumpStart proto) {:dump-start (cv/parse-dump-start (.getDumpStart
                                                                  proto))}
        (.hasDumpStop proto) {:dump-stop (cv/parse-dump-stop (.getDumpStop
                                                               proto))}))

(defn parse-root-payload
  "Parse the oneof payload from Root."
  [^cmd.DayCamGlassHeater.JonSharedCmdDayCamGlassHeater$Root proto]
  (cond (.hasStart proto) {:start (daycamglassheater/parse-start (.getStart
                                                                   proto))}
        (.hasStop proto) {:stop (daycamglassheater/parse-stop (.getStop proto))}
        (.hasTurnOn proto) {:turn-on (daycamglassheater/parse-turn-on
                                       (.getTurnOn proto))}
        (.hasTurnOff proto) {:turn-off (daycamglassheater/parse-turn-off
                                         (.getTurnOff proto))}
        (.hasGetMeteo proto) {:get-meteo (daycamglassheater/parse-get-meteo
                                           (.getGetMeteo proto))}))

(defn parse-root-payload
  "Parse the oneof payload from Root."
  [^cmd.Lira.JonSharedCmdLira$Root proto]
  (cond (.hasRefineTarget proto) {:refine-target (lira/parse-refine-target
                                                   (.getRefineTarget proto))}))

(defn parse-root-payload
  "Parse the oneof payload from Root."
  [^cmd.JonSharedCmd$Root proto]
  (cond (.hasDayCamera proto) {:day-camera (daycamera/parse-root (.getDayCamera
                                                                   proto))}
        (.hasHeatCamera proto) {:heat-camera (heatcamera/parse-root
                                               (.getHeatCamera proto))}
        (.hasGps proto) {:gps (gps/parse-root (.getGps proto))}
        (.hasCompass proto) {:compass (compass/parse-root (.getCompass proto))}
        (.hasLrf proto) {:lrf (lrf/parse-root (.getLrf proto))}
        (.hasLrfCalib proto) {:lrf-calib (lrf-calib/parse-root (.getLrfCalib
                                                                 proto))}
        (.hasRotary proto) {:rotary (rotaryplatform/parse-root (.getRotary
                                                                 proto))}
        (.hasOsd proto) {:osd (osd/parse-root (.getOsd proto))}
        (.hasPing proto) {:ping (parse-ping (.getPing proto))}
        (.hasNoop proto) {:noop (parse-noop (.getNoop proto))}
        (.hasFrozen proto) {:frozen (parse-frozen (.getFrozen proto))}
        (.hasSystem proto) {:system (system/parse-root (.getSystem proto))}
        (.hasCv proto) {:cv (cv/parse-root (.getCv proto))}
        (.hasDayCamGlassHeater proto) {:day-cam-glass-heater
                                         (daycamglassheater/parse-root
                                           (.getDayCamGlassHeater proto))}
        (.hasLira proto) {:lira (lira/parse-root (.getLira proto))}))