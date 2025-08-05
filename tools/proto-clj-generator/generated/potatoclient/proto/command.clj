(ns potatoclient.proto.command
  "Generated protobuf functions."
  (:import
   cmd.CV.JonSharedCmdCv
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
  {:jon-gui-data-video-channel-heat-filter-unspecified ser.JonSharedDataTypes$JonGuiDataVideoChannelHeatFilters/JON_GUI_DATA_VIDEO_CHANNEL_HEAT_FILTER_UNSPECIFIED
   :jon-gui-data-video-channel-heat-filter-hot-white ser.JonSharedDataTypes$JonGuiDataVideoChannelHeatFilters/JON_GUI_DATA_VIDEO_CHANNEL_HEAT_FILTER_HOT_WHITE
   :jon-gui-data-video-channel-heat-filter-hot-black ser.JonSharedDataTypes$JonGuiDataVideoChannelHeatFilters/JON_GUI_DATA_VIDEO_CHANNEL_HEAT_FILTER_HOT_BLACK
   :jon-gui-data-video-channel-heat-filter-sepia ser.JonSharedDataTypes$JonGuiDataVideoChannelHeatFilters/JON_GUI_DATA_VIDEO_CHANNEL_HEAT_FILTER_SEPIA
   :jon-gui-data-video-channel-heat-filter-sepia-inverse ser.JonSharedDataTypes$JonGuiDataVideoChannelHeatFilters/JON_GUI_DATA_VIDEO_CHANNEL_HEAT_FILTER_SEPIA_INVERSE})

(def jon-gui-data-video-channel-heat-filters-keywords
  "Java enum to keyword mapping for JonGuiDataVideoChannelHeatFilters."
  {ser.JonSharedDataTypes$JonGuiDataVideoChannelHeatFilters/JON_GUI_DATA_VIDEO_CHANNEL_HEAT_FILTER_UNSPECIFIED :jon-gui-data-video-channel-heat-filter-unspecified
   ser.JonSharedDataTypes$JonGuiDataVideoChannelHeatFilters/JON_GUI_DATA_VIDEO_CHANNEL_HEAT_FILTER_HOT_WHITE :jon-gui-data-video-channel-heat-filter-hot-white
   ser.JonSharedDataTypes$JonGuiDataVideoChannelHeatFilters/JON_GUI_DATA_VIDEO_CHANNEL_HEAT_FILTER_HOT_BLACK :jon-gui-data-video-channel-heat-filter-hot-black
   ser.JonSharedDataTypes$JonGuiDataVideoChannelHeatFilters/JON_GUI_DATA_VIDEO_CHANNEL_HEAT_FILTER_SEPIA :jon-gui-data-video-channel-heat-filter-sepia
   ser.JonSharedDataTypes$JonGuiDataVideoChannelHeatFilters/JON_GUI_DATA_VIDEO_CHANNEL_HEAT_FILTER_SEPIA_INVERSE :jon-gui-data-video-channel-heat-filter-sepia-inverse})

;; Enum: JonGuiDataVideoChannelHeatAGCModes
(def jon-gui-data-video-channel-heat-agc-modes-values
  "Keyword to Java enum mapping for JonGuiDataVideoChannelHeatAGCModes."
  {:jon-gui-data-video-channel-heat-agc-mode-unspecified ser.JonSharedDataTypes$JonGuiDataVideoChannelHeatAGCModes/JON_GUI_DATA_VIDEO_CHANNEL_HEAT_AGC_MODE_UNSPECIFIED
   :jon-gui-data-video-channel-heat-agc-mode-1 ser.JonSharedDataTypes$JonGuiDataVideoChannelHeatAGCModes/JON_GUI_DATA_VIDEO_CHANNEL_HEAT_AGC_MODE_1
   :jon-gui-data-video-channel-heat-agc-mode-2 ser.JonSharedDataTypes$JonGuiDataVideoChannelHeatAGCModes/JON_GUI_DATA_VIDEO_CHANNEL_HEAT_AGC_MODE_2
   :jon-gui-data-video-channel-heat-agc-mode-3 ser.JonSharedDataTypes$JonGuiDataVideoChannelHeatAGCModes/JON_GUI_DATA_VIDEO_CHANNEL_HEAT_AGC_MODE_3})

(def jon-gui-data-video-channel-heat-agc-modes-keywords
  "Java enum to keyword mapping for JonGuiDataVideoChannelHeatAGCModes."
  {ser.JonSharedDataTypes$JonGuiDataVideoChannelHeatAGCModes/JON_GUI_DATA_VIDEO_CHANNEL_HEAT_AGC_MODE_UNSPECIFIED :jon-gui-data-video-channel-heat-agc-mode-unspecified
   ser.JonSharedDataTypes$JonGuiDataVideoChannelHeatAGCModes/JON_GUI_DATA_VIDEO_CHANNEL_HEAT_AGC_MODE_1 :jon-gui-data-video-channel-heat-agc-mode-1
   ser.JonSharedDataTypes$JonGuiDataVideoChannelHeatAGCModes/JON_GUI_DATA_VIDEO_CHANNEL_HEAT_AGC_MODE_2 :jon-gui-data-video-channel-heat-agc-mode-2
   ser.JonSharedDataTypes$JonGuiDataVideoChannelHeatAGCModes/JON_GUI_DATA_VIDEO_CHANNEL_HEAT_AGC_MODE_3 :jon-gui-data-video-channel-heat-agc-mode-3})

;; Enum: JonGuiDataGpsUnits
(def jon-gui-data-gps-units-values
  "Keyword to Java enum mapping for JonGuiDataGpsUnits."
  {:jon-gui-data-gps-units-unspecified ser.JonSharedDataTypes$JonGuiDataGpsUnits/JON_GUI_DATA_GPS_UNITS_UNSPECIFIED
   :jon-gui-data-gps-units-decimal-degrees ser.JonSharedDataTypes$JonGuiDataGpsUnits/JON_GUI_DATA_GPS_UNITS_DECIMAL_DEGREES
   :jon-gui-data-gps-units-degrees-minutes-seconds ser.JonSharedDataTypes$JonGuiDataGpsUnits/JON_GUI_DATA_GPS_UNITS_DEGREES_MINUTES_SECONDS
   :jon-gui-data-gps-units-degrees-decimal-minutes ser.JonSharedDataTypes$JonGuiDataGpsUnits/JON_GUI_DATA_GPS_UNITS_DEGREES_DECIMAL_MINUTES})

(def jon-gui-data-gps-units-keywords
  "Java enum to keyword mapping for JonGuiDataGpsUnits."
  {ser.JonSharedDataTypes$JonGuiDataGpsUnits/JON_GUI_DATA_GPS_UNITS_UNSPECIFIED :jon-gui-data-gps-units-unspecified
   ser.JonSharedDataTypes$JonGuiDataGpsUnits/JON_GUI_DATA_GPS_UNITS_DECIMAL_DEGREES :jon-gui-data-gps-units-decimal-degrees
   ser.JonSharedDataTypes$JonGuiDataGpsUnits/JON_GUI_DATA_GPS_UNITS_DEGREES_MINUTES_SECONDS :jon-gui-data-gps-units-degrees-minutes-seconds
   ser.JonSharedDataTypes$JonGuiDataGpsUnits/JON_GUI_DATA_GPS_UNITS_DEGREES_DECIMAL_MINUTES :jon-gui-data-gps-units-degrees-decimal-minutes})

;; Enum: JonGuiDataGpsFixType
(def jon-gui-data-gps-fix-type-values
  "Keyword to Java enum mapping for JonGuiDataGpsFixType."
  {:jon-gui-data-gps-fix-type-unspecified ser.JonSharedDataTypes$JonGuiDataGpsFixType/JON_GUI_DATA_GPS_FIX_TYPE_UNSPECIFIED
   :jon-gui-data-gps-fix-type-none ser.JonSharedDataTypes$JonGuiDataGpsFixType/JON_GUI_DATA_GPS_FIX_TYPE_NONE
   :jon-gui-data-gps-fix-type-1d ser.JonSharedDataTypes$JonGuiDataGpsFixType/JON_GUI_DATA_GPS_FIX_TYPE_1D
   :jon-gui-data-gps-fix-type-2d ser.JonSharedDataTypes$JonGuiDataGpsFixType/JON_GUI_DATA_GPS_FIX_TYPE_2D
   :jon-gui-data-gps-fix-type-3d ser.JonSharedDataTypes$JonGuiDataGpsFixType/JON_GUI_DATA_GPS_FIX_TYPE_3D
   :jon-gui-data-gps-fix-type-manual ser.JonSharedDataTypes$JonGuiDataGpsFixType/JON_GUI_DATA_GPS_FIX_TYPE_MANUAL})

(def jon-gui-data-gps-fix-type-keywords
  "Java enum to keyword mapping for JonGuiDataGpsFixType."
  {ser.JonSharedDataTypes$JonGuiDataGpsFixType/JON_GUI_DATA_GPS_FIX_TYPE_UNSPECIFIED :jon-gui-data-gps-fix-type-unspecified
   ser.JonSharedDataTypes$JonGuiDataGpsFixType/JON_GUI_DATA_GPS_FIX_TYPE_NONE :jon-gui-data-gps-fix-type-none
   ser.JonSharedDataTypes$JonGuiDataGpsFixType/JON_GUI_DATA_GPS_FIX_TYPE_1D :jon-gui-data-gps-fix-type-1d
   ser.JonSharedDataTypes$JonGuiDataGpsFixType/JON_GUI_DATA_GPS_FIX_TYPE_2D :jon-gui-data-gps-fix-type-2d
   ser.JonSharedDataTypes$JonGuiDataGpsFixType/JON_GUI_DATA_GPS_FIX_TYPE_3D :jon-gui-data-gps-fix-type-3d
   ser.JonSharedDataTypes$JonGuiDataGpsFixType/JON_GUI_DATA_GPS_FIX_TYPE_MANUAL :jon-gui-data-gps-fix-type-manual})

;; Enum: JonGuiDataCompassUnits
(def jon-gui-data-compass-units-values
  "Keyword to Java enum mapping for JonGuiDataCompassUnits."
  {:jon-gui-data-compass-units-unspecified ser.JonSharedDataTypes$JonGuiDataCompassUnits/JON_GUI_DATA_COMPASS_UNITS_UNSPECIFIED
   :jon-gui-data-compass-units-degrees ser.JonSharedDataTypes$JonGuiDataCompassUnits/JON_GUI_DATA_COMPASS_UNITS_DEGREES
   :jon-gui-data-compass-units-mils ser.JonSharedDataTypes$JonGuiDataCompassUnits/JON_GUI_DATA_COMPASS_UNITS_MILS
   :jon-gui-data-compass-units-grad ser.JonSharedDataTypes$JonGuiDataCompassUnits/JON_GUI_DATA_COMPASS_UNITS_GRAD
   :jon-gui-data-compass-units-mrad ser.JonSharedDataTypes$JonGuiDataCompassUnits/JON_GUI_DATA_COMPASS_UNITS_MRAD})

(def jon-gui-data-compass-units-keywords
  "Java enum to keyword mapping for JonGuiDataCompassUnits."
  {ser.JonSharedDataTypes$JonGuiDataCompassUnits/JON_GUI_DATA_COMPASS_UNITS_UNSPECIFIED :jon-gui-data-compass-units-unspecified
   ser.JonSharedDataTypes$JonGuiDataCompassUnits/JON_GUI_DATA_COMPASS_UNITS_DEGREES :jon-gui-data-compass-units-degrees
   ser.JonSharedDataTypes$JonGuiDataCompassUnits/JON_GUI_DATA_COMPASS_UNITS_MILS :jon-gui-data-compass-units-mils
   ser.JonSharedDataTypes$JonGuiDataCompassUnits/JON_GUI_DATA_COMPASS_UNITS_GRAD :jon-gui-data-compass-units-grad
   ser.JonSharedDataTypes$JonGuiDataCompassUnits/JON_GUI_DATA_COMPASS_UNITS_MRAD :jon-gui-data-compass-units-mrad})

;; Enum: JonGuiDataAccumulatorStateIdx
(def jon-gui-data-accumulator-state-idx-values
  "Keyword to Java enum mapping for JonGuiDataAccumulatorStateIdx."
  {:jon-gui-data-accumulator-state-unspecified ser.JonSharedDataTypes$JonGuiDataAccumulatorStateIdx/JON_GUI_DATA_ACCUMULATOR_STATE_UNSPECIFIED
   :jon-gui-data-accumulator-state-unknown ser.JonSharedDataTypes$JonGuiDataAccumulatorStateIdx/JON_GUI_DATA_ACCUMULATOR_STATE_UNKNOWN
   :jon-gui-data-accumulator-state-empty ser.JonSharedDataTypes$JonGuiDataAccumulatorStateIdx/JON_GUI_DATA_ACCUMULATOR_STATE_EMPTY
   :jon-gui-data-accumulator-state-1 ser.JonSharedDataTypes$JonGuiDataAccumulatorStateIdx/JON_GUI_DATA_ACCUMULATOR_STATE_1
   :jon-gui-data-accumulator-state-2 ser.JonSharedDataTypes$JonGuiDataAccumulatorStateIdx/JON_GUI_DATA_ACCUMULATOR_STATE_2
   :jon-gui-data-accumulator-state-3 ser.JonSharedDataTypes$JonGuiDataAccumulatorStateIdx/JON_GUI_DATA_ACCUMULATOR_STATE_3
   :jon-gui-data-accumulator-state-4 ser.JonSharedDataTypes$JonGuiDataAccumulatorStateIdx/JON_GUI_DATA_ACCUMULATOR_STATE_4
   :jon-gui-data-accumulator-state-5 ser.JonSharedDataTypes$JonGuiDataAccumulatorStateIdx/JON_GUI_DATA_ACCUMULATOR_STATE_5
   :jon-gui-data-accumulator-state-6 ser.JonSharedDataTypes$JonGuiDataAccumulatorStateIdx/JON_GUI_DATA_ACCUMULATOR_STATE_6
   :jon-gui-data-accumulator-state-full ser.JonSharedDataTypes$JonGuiDataAccumulatorStateIdx/JON_GUI_DATA_ACCUMULATOR_STATE_FULL
   :jon-gui-data-accumulator-state-charging ser.JonSharedDataTypes$JonGuiDataAccumulatorStateIdx/JON_GUI_DATA_ACCUMULATOR_STATE_CHARGING})

(def jon-gui-data-accumulator-state-idx-keywords
  "Java enum to keyword mapping for JonGuiDataAccumulatorStateIdx."
  {ser.JonSharedDataTypes$JonGuiDataAccumulatorStateIdx/JON_GUI_DATA_ACCUMULATOR_STATE_UNSPECIFIED :jon-gui-data-accumulator-state-unspecified
   ser.JonSharedDataTypes$JonGuiDataAccumulatorStateIdx/JON_GUI_DATA_ACCUMULATOR_STATE_UNKNOWN :jon-gui-data-accumulator-state-unknown
   ser.JonSharedDataTypes$JonGuiDataAccumulatorStateIdx/JON_GUI_DATA_ACCUMULATOR_STATE_EMPTY :jon-gui-data-accumulator-state-empty
   ser.JonSharedDataTypes$JonGuiDataAccumulatorStateIdx/JON_GUI_DATA_ACCUMULATOR_STATE_1 :jon-gui-data-accumulator-state-1
   ser.JonSharedDataTypes$JonGuiDataAccumulatorStateIdx/JON_GUI_DATA_ACCUMULATOR_STATE_2 :jon-gui-data-accumulator-state-2
   ser.JonSharedDataTypes$JonGuiDataAccumulatorStateIdx/JON_GUI_DATA_ACCUMULATOR_STATE_3 :jon-gui-data-accumulator-state-3
   ser.JonSharedDataTypes$JonGuiDataAccumulatorStateIdx/JON_GUI_DATA_ACCUMULATOR_STATE_4 :jon-gui-data-accumulator-state-4
   ser.JonSharedDataTypes$JonGuiDataAccumulatorStateIdx/JON_GUI_DATA_ACCUMULATOR_STATE_5 :jon-gui-data-accumulator-state-5
   ser.JonSharedDataTypes$JonGuiDataAccumulatorStateIdx/JON_GUI_DATA_ACCUMULATOR_STATE_6 :jon-gui-data-accumulator-state-6
   ser.JonSharedDataTypes$JonGuiDataAccumulatorStateIdx/JON_GUI_DATA_ACCUMULATOR_STATE_FULL :jon-gui-data-accumulator-state-full
   ser.JonSharedDataTypes$JonGuiDataAccumulatorStateIdx/JON_GUI_DATA_ACCUMULATOR_STATE_CHARGING :jon-gui-data-accumulator-state-charging})

;; Enum: JonGuiDataTimeFormats
(def jon-gui-data-time-formats-values
  "Keyword to Java enum mapping for JonGuiDataTimeFormats."
  {:jon-gui-data-time-format-unspecified ser.JonSharedDataTypes$JonGuiDataTimeFormats/JON_GUI_DATA_TIME_FORMAT_UNSPECIFIED
   :jon-gui-data-time-format-h-m-s ser.JonSharedDataTypes$JonGuiDataTimeFormats/JON_GUI_DATA_TIME_FORMAT_H_M_S
   :jon-gui-data-time-format-y-m-d-h-m-s ser.JonSharedDataTypes$JonGuiDataTimeFormats/JON_GUI_DATA_TIME_FORMAT_Y_m_D_H_M_S})

(def jon-gui-data-time-formats-keywords
  "Java enum to keyword mapping for JonGuiDataTimeFormats."
  {ser.JonSharedDataTypes$JonGuiDataTimeFormats/JON_GUI_DATA_TIME_FORMAT_UNSPECIFIED :jon-gui-data-time-format-unspecified
   ser.JonSharedDataTypes$JonGuiDataTimeFormats/JON_GUI_DATA_TIME_FORMAT_H_M_S :jon-gui-data-time-format-h-m-s
   ser.JonSharedDataTypes$JonGuiDataTimeFormats/JON_GUI_DATA_TIME_FORMAT_Y_m_D_H_M_S :jon-gui-data-time-format-y-m-d-h-m-s})

;; Enum: JonGuiDataRotaryDirection
(def jon-gui-data-rotary-direction-values
  "Keyword to Java enum mapping for JonGuiDataRotaryDirection."
  {:jon-gui-data-rotary-direction-unspecified ser.JonSharedDataTypes$JonGuiDataRotaryDirection/JON_GUI_DATA_ROTARY_DIRECTION_UNSPECIFIED
   :jon-gui-data-rotary-direction-clockwise ser.JonSharedDataTypes$JonGuiDataRotaryDirection/JON_GUI_DATA_ROTARY_DIRECTION_CLOCKWISE
   :jon-gui-data-rotary-direction-counter-clockwise ser.JonSharedDataTypes$JonGuiDataRotaryDirection/JON_GUI_DATA_ROTARY_DIRECTION_COUNTER_CLOCKWISE})

(def jon-gui-data-rotary-direction-keywords
  "Java enum to keyword mapping for JonGuiDataRotaryDirection."
  {ser.JonSharedDataTypes$JonGuiDataRotaryDirection/JON_GUI_DATA_ROTARY_DIRECTION_UNSPECIFIED :jon-gui-data-rotary-direction-unspecified
   ser.JonSharedDataTypes$JonGuiDataRotaryDirection/JON_GUI_DATA_ROTARY_DIRECTION_CLOCKWISE :jon-gui-data-rotary-direction-clockwise
   ser.JonSharedDataTypes$JonGuiDataRotaryDirection/JON_GUI_DATA_ROTARY_DIRECTION_COUNTER_CLOCKWISE :jon-gui-data-rotary-direction-counter-clockwise})

;; Enum: JonGuiDataLrfScanModes
(def jon-gui-data-lrf-scan-modes-values
  "Keyword to Java enum mapping for JonGuiDataLrfScanModes."
  {:jon-gui-data-lrf-scan-mode-unspecified ser.JonSharedDataTypes$JonGuiDataLrfScanModes/JON_GUI_DATA_LRF_SCAN_MODE_UNSPECIFIED
   :jon-gui-data-lrf-scan-mode-1-hz-continuous ser.JonSharedDataTypes$JonGuiDataLrfScanModes/JON_GUI_DATA_LRF_SCAN_MODE_1_HZ_CONTINUOUS
   :jon-gui-data-lrf-scan-mode-4-hz-continuous ser.JonSharedDataTypes$JonGuiDataLrfScanModes/JON_GUI_DATA_LRF_SCAN_MODE_4_HZ_CONTINUOUS
   :jon-gui-data-lrf-scan-mode-10-hz-continuous ser.JonSharedDataTypes$JonGuiDataLrfScanModes/JON_GUI_DATA_LRF_SCAN_MODE_10_HZ_CONTINUOUS
   :jon-gui-data-lrf-scan-mode-20-hz-continuous ser.JonSharedDataTypes$JonGuiDataLrfScanModes/JON_GUI_DATA_LRF_SCAN_MODE_20_HZ_CONTINUOUS
   :jon-gui-data-lrf-scan-mode-100-hz-continuous ser.JonSharedDataTypes$JonGuiDataLrfScanModes/JON_GUI_DATA_LRF_SCAN_MODE_100_HZ_CONTINUOUS
   :jon-gui-data-lrf-scan-mode-200-hz-continuous ser.JonSharedDataTypes$JonGuiDataLrfScanModes/JON_GUI_DATA_LRF_SCAN_MODE_200_HZ_CONTINUOUS})

(def jon-gui-data-lrf-scan-modes-keywords
  "Java enum to keyword mapping for JonGuiDataLrfScanModes."
  {ser.JonSharedDataTypes$JonGuiDataLrfScanModes/JON_GUI_DATA_LRF_SCAN_MODE_UNSPECIFIED :jon-gui-data-lrf-scan-mode-unspecified
   ser.JonSharedDataTypes$JonGuiDataLrfScanModes/JON_GUI_DATA_LRF_SCAN_MODE_1_HZ_CONTINUOUS :jon-gui-data-lrf-scan-mode-1-hz-continuous
   ser.JonSharedDataTypes$JonGuiDataLrfScanModes/JON_GUI_DATA_LRF_SCAN_MODE_4_HZ_CONTINUOUS :jon-gui-data-lrf-scan-mode-4-hz-continuous
   ser.JonSharedDataTypes$JonGuiDataLrfScanModes/JON_GUI_DATA_LRF_SCAN_MODE_10_HZ_CONTINUOUS :jon-gui-data-lrf-scan-mode-10-hz-continuous
   ser.JonSharedDataTypes$JonGuiDataLrfScanModes/JON_GUI_DATA_LRF_SCAN_MODE_20_HZ_CONTINUOUS :jon-gui-data-lrf-scan-mode-20-hz-continuous
   ser.JonSharedDataTypes$JonGuiDataLrfScanModes/JON_GUI_DATA_LRF_SCAN_MODE_100_HZ_CONTINUOUS :jon-gui-data-lrf-scan-mode-100-hz-continuous
   ser.JonSharedDataTypes$JonGuiDataLrfScanModes/JON_GUI_DATA_LRF_SCAN_MODE_200_HZ_CONTINUOUS :jon-gui-data-lrf-scan-mode-200-hz-continuous})

;; Enum: JonGuiDatatLrfLaserPointerModes
(def jon-gui-datat-lrf-laser-pointer-modes-values
  "Keyword to Java enum mapping for JonGuiDatatLrfLaserPointerModes."
  {:jon-gui-data-lrf-laser-pointer-mode-unspecified ser.JonSharedDataTypes$JonGuiDatatLrfLaserPointerModes/JON_GUI_DATA_LRF_LASER_POINTER_MODE_UNSPECIFIED
   :jon-gui-data-lrf-laser-pointer-mode-off ser.JonSharedDataTypes$JonGuiDatatLrfLaserPointerModes/JON_GUI_DATA_LRF_LASER_POINTER_MODE_OFF
   :jon-gui-data-lrf-laser-pointer-mode-on-1 ser.JonSharedDataTypes$JonGuiDatatLrfLaserPointerModes/JON_GUI_DATA_LRF_LASER_POINTER_MODE_ON_1
   :jon-gui-data-lrf-laser-pointer-mode-on-2 ser.JonSharedDataTypes$JonGuiDatatLrfLaserPointerModes/JON_GUI_DATA_LRF_LASER_POINTER_MODE_ON_2})

(def jon-gui-datat-lrf-laser-pointer-modes-keywords
  "Java enum to keyword mapping for JonGuiDatatLrfLaserPointerModes."
  {ser.JonSharedDataTypes$JonGuiDatatLrfLaserPointerModes/JON_GUI_DATA_LRF_LASER_POINTER_MODE_UNSPECIFIED :jon-gui-data-lrf-laser-pointer-mode-unspecified
   ser.JonSharedDataTypes$JonGuiDatatLrfLaserPointerModes/JON_GUI_DATA_LRF_LASER_POINTER_MODE_OFF :jon-gui-data-lrf-laser-pointer-mode-off
   ser.JonSharedDataTypes$JonGuiDatatLrfLaserPointerModes/JON_GUI_DATA_LRF_LASER_POINTER_MODE_ON_1 :jon-gui-data-lrf-laser-pointer-mode-on-1
   ser.JonSharedDataTypes$JonGuiDatatLrfLaserPointerModes/JON_GUI_DATA_LRF_LASER_POINTER_MODE_ON_2 :jon-gui-data-lrf-laser-pointer-mode-on-2})

;; Enum: JonGuiDataCompassCalibrateStatus
(def jon-gui-data-compass-calibrate-status-values
  "Keyword to Java enum mapping for JonGuiDataCompassCalibrateStatus."
  {:jon-gui-data-compass-calibrate-status-unspecified ser.JonSharedDataTypes$JonGuiDataCompassCalibrateStatus/JON_GUI_DATA_COMPASS_CALIBRATE_STATUS_UNSPECIFIED
   :jon-gui-data-compass-calibrate-status-not-calibrating ser.JonSharedDataTypes$JonGuiDataCompassCalibrateStatus/JON_GUI_DATA_COMPASS_CALIBRATE_STATUS_NOT_CALIBRATING
   :jon-gui-data-compass-calibrate-status-calibrating-short ser.JonSharedDataTypes$JonGuiDataCompassCalibrateStatus/JON_GUI_DATA_COMPASS_CALIBRATE_STATUS_CALIBRATING_SHORT
   :jon-gui-data-compass-calibrate-status-calibrating-long ser.JonSharedDataTypes$JonGuiDataCompassCalibrateStatus/JON_GUI_DATA_COMPASS_CALIBRATE_STATUS_CALIBRATING_LONG
   :jon-gui-data-compass-calibrate-status-finished ser.JonSharedDataTypes$JonGuiDataCompassCalibrateStatus/JON_GUI_DATA_COMPASS_CALIBRATE_STATUS_FINISHED
   :jon-gui-data-compass-calibrate-status-error ser.JonSharedDataTypes$JonGuiDataCompassCalibrateStatus/JON_GUI_DATA_COMPASS_CALIBRATE_STATUS_ERROR})

(def jon-gui-data-compass-calibrate-status-keywords
  "Java enum to keyword mapping for JonGuiDataCompassCalibrateStatus."
  {ser.JonSharedDataTypes$JonGuiDataCompassCalibrateStatus/JON_GUI_DATA_COMPASS_CALIBRATE_STATUS_UNSPECIFIED :jon-gui-data-compass-calibrate-status-unspecified
   ser.JonSharedDataTypes$JonGuiDataCompassCalibrateStatus/JON_GUI_DATA_COMPASS_CALIBRATE_STATUS_NOT_CALIBRATING :jon-gui-data-compass-calibrate-status-not-calibrating
   ser.JonSharedDataTypes$JonGuiDataCompassCalibrateStatus/JON_GUI_DATA_COMPASS_CALIBRATE_STATUS_CALIBRATING_SHORT :jon-gui-data-compass-calibrate-status-calibrating-short
   ser.JonSharedDataTypes$JonGuiDataCompassCalibrateStatus/JON_GUI_DATA_COMPASS_CALIBRATE_STATUS_CALIBRATING_LONG :jon-gui-data-compass-calibrate-status-calibrating-long
   ser.JonSharedDataTypes$JonGuiDataCompassCalibrateStatus/JON_GUI_DATA_COMPASS_CALIBRATE_STATUS_FINISHED :jon-gui-data-compass-calibrate-status-finished
   ser.JonSharedDataTypes$JonGuiDataCompassCalibrateStatus/JON_GUI_DATA_COMPASS_CALIBRATE_STATUS_ERROR :jon-gui-data-compass-calibrate-status-error})

;; Enum: JonGuiDataRotaryMode
(def jon-gui-data-rotary-mode-values
  "Keyword to Java enum mapping for JonGuiDataRotaryMode."
  {:jon-gui-data-rotary-mode-unspecified ser.JonSharedDataTypes$JonGuiDataRotaryMode/JON_GUI_DATA_ROTARY_MODE_UNSPECIFIED
   :jon-gui-data-rotary-mode-initialization ser.JonSharedDataTypes$JonGuiDataRotaryMode/JON_GUI_DATA_ROTARY_MODE_INITIALIZATION
   :jon-gui-data-rotary-mode-speed ser.JonSharedDataTypes$JonGuiDataRotaryMode/JON_GUI_DATA_ROTARY_MODE_SPEED
   :jon-gui-data-rotary-mode-position ser.JonSharedDataTypes$JonGuiDataRotaryMode/JON_GUI_DATA_ROTARY_MODE_POSITION
   :jon-gui-data-rotary-mode-stabilization ser.JonSharedDataTypes$JonGuiDataRotaryMode/JON_GUI_DATA_ROTARY_MODE_STABILIZATION
   :jon-gui-data-rotary-mode-targeting ser.JonSharedDataTypes$JonGuiDataRotaryMode/JON_GUI_DATA_ROTARY_MODE_TARGETING
   :jon-gui-data-rotary-mode-video-tracker ser.JonSharedDataTypes$JonGuiDataRotaryMode/JON_GUI_DATA_ROTARY_MODE_VIDEO_TRACKER})

(def jon-gui-data-rotary-mode-keywords
  "Java enum to keyword mapping for JonGuiDataRotaryMode."
  {ser.JonSharedDataTypes$JonGuiDataRotaryMode/JON_GUI_DATA_ROTARY_MODE_UNSPECIFIED :jon-gui-data-rotary-mode-unspecified
   ser.JonSharedDataTypes$JonGuiDataRotaryMode/JON_GUI_DATA_ROTARY_MODE_INITIALIZATION :jon-gui-data-rotary-mode-initialization
   ser.JonSharedDataTypes$JonGuiDataRotaryMode/JON_GUI_DATA_ROTARY_MODE_SPEED :jon-gui-data-rotary-mode-speed
   ser.JonSharedDataTypes$JonGuiDataRotaryMode/JON_GUI_DATA_ROTARY_MODE_POSITION :jon-gui-data-rotary-mode-position
   ser.JonSharedDataTypes$JonGuiDataRotaryMode/JON_GUI_DATA_ROTARY_MODE_STABILIZATION :jon-gui-data-rotary-mode-stabilization
   ser.JonSharedDataTypes$JonGuiDataRotaryMode/JON_GUI_DATA_ROTARY_MODE_TARGETING :jon-gui-data-rotary-mode-targeting
   ser.JonSharedDataTypes$JonGuiDataRotaryMode/JON_GUI_DATA_ROTARY_MODE_VIDEO_TRACKER :jon-gui-data-rotary-mode-video-tracker})

;; Enum: JonGuiDataVideoChannel
(def jon-gui-data-video-channel-values
  "Keyword to Java enum mapping for JonGuiDataVideoChannel."
  {:jon-gui-data-video-channel-unspecified ser.JonSharedDataTypes$JonGuiDataVideoChannel/JON_GUI_DATA_VIDEO_CHANNEL_UNSPECIFIED
   :jon-gui-data-video-channel-heat ser.JonSharedDataTypes$JonGuiDataVideoChannel/JON_GUI_DATA_VIDEO_CHANNEL_HEAT
   :jon-gui-data-video-channel-day ser.JonSharedDataTypes$JonGuiDataVideoChannel/JON_GUI_DATA_VIDEO_CHANNEL_DAY})

(def jon-gui-data-video-channel-keywords
  "Java enum to keyword mapping for JonGuiDataVideoChannel."
  {ser.JonSharedDataTypes$JonGuiDataVideoChannel/JON_GUI_DATA_VIDEO_CHANNEL_UNSPECIFIED :jon-gui-data-video-channel-unspecified
   ser.JonSharedDataTypes$JonGuiDataVideoChannel/JON_GUI_DATA_VIDEO_CHANNEL_HEAT :jon-gui-data-video-channel-heat
   ser.JonSharedDataTypes$JonGuiDataVideoChannel/JON_GUI_DATA_VIDEO_CHANNEL_DAY :jon-gui-data-video-channel-day})

;; Enum: JonGuiDataRecOsdScreen
(def jon-gui-data-rec-osd-screen-values
  "Keyword to Java enum mapping for JonGuiDataRecOsdScreen."
  {:jon-gui-data-rec-osd-screen-unspecified ser.JonSharedDataTypes$JonGuiDataRecOsdScreen/JON_GUI_DATA_REC_OSD_SCREEN_UNSPECIFIED
   :jon-gui-data-rec-osd-screen-main ser.JonSharedDataTypes$JonGuiDataRecOsdScreen/JON_GUI_DATA_REC_OSD_SCREEN_MAIN
   :jon-gui-data-rec-osd-screen-lrf-measure ser.JonSharedDataTypes$JonGuiDataRecOsdScreen/JON_GUI_DATA_REC_OSD_SCREEN_LRF_MEASURE
   :jon-gui-data-rec-osd-screen-lrf-result ser.JonSharedDataTypes$JonGuiDataRecOsdScreen/JON_GUI_DATA_REC_OSD_SCREEN_LRF_RESULT
   :jon-gui-data-rec-osd-screen-lrf-result-simplified ser.JonSharedDataTypes$JonGuiDataRecOsdScreen/JON_GUI_DATA_REC_OSD_SCREEN_LRF_RESULT_SIMPLIFIED})

(def jon-gui-data-rec-osd-screen-keywords
  "Java enum to keyword mapping for JonGuiDataRecOsdScreen."
  {ser.JonSharedDataTypes$JonGuiDataRecOsdScreen/JON_GUI_DATA_REC_OSD_SCREEN_UNSPECIFIED :jon-gui-data-rec-osd-screen-unspecified
   ser.JonSharedDataTypes$JonGuiDataRecOsdScreen/JON_GUI_DATA_REC_OSD_SCREEN_MAIN :jon-gui-data-rec-osd-screen-main
   ser.JonSharedDataTypes$JonGuiDataRecOsdScreen/JON_GUI_DATA_REC_OSD_SCREEN_LRF_MEASURE :jon-gui-data-rec-osd-screen-lrf-measure
   ser.JonSharedDataTypes$JonGuiDataRecOsdScreen/JON_GUI_DATA_REC_OSD_SCREEN_LRF_RESULT :jon-gui-data-rec-osd-screen-lrf-result
   ser.JonSharedDataTypes$JonGuiDataRecOsdScreen/JON_GUI_DATA_REC_OSD_SCREEN_LRF_RESULT_SIMPLIFIED :jon-gui-data-rec-osd-screen-lrf-result-simplified})

;; Enum: JonGuiDataFxModeDay
(def jon-gui-data-fx-mode-day-values
  "Keyword to Java enum mapping for JonGuiDataFxModeDay."
  {:jon-gui-data-fx-mode-day-default ser.JonSharedDataTypes$JonGuiDataFxModeDay/JON_GUI_DATA_FX_MODE_DAY_DEFAULT
   :jon-gui-data-fx-mode-day-a ser.JonSharedDataTypes$JonGuiDataFxModeDay/JON_GUI_DATA_FX_MODE_DAY_A
   :jon-gui-data-fx-mode-day-b ser.JonSharedDataTypes$JonGuiDataFxModeDay/JON_GUI_DATA_FX_MODE_DAY_B
   :jon-gui-data-fx-mode-day-c ser.JonSharedDataTypes$JonGuiDataFxModeDay/JON_GUI_DATA_FX_MODE_DAY_C
   :jon-gui-data-fx-mode-day-d ser.JonSharedDataTypes$JonGuiDataFxModeDay/JON_GUI_DATA_FX_MODE_DAY_D
   :jon-gui-data-fx-mode-day-e ser.JonSharedDataTypes$JonGuiDataFxModeDay/JON_GUI_DATA_FX_MODE_DAY_E
   :jon-gui-data-fx-mode-day-f ser.JonSharedDataTypes$JonGuiDataFxModeDay/JON_GUI_DATA_FX_MODE_DAY_F})

(def jon-gui-data-fx-mode-day-keywords
  "Java enum to keyword mapping for JonGuiDataFxModeDay."
  {ser.JonSharedDataTypes$JonGuiDataFxModeDay/JON_GUI_DATA_FX_MODE_DAY_DEFAULT :jon-gui-data-fx-mode-day-default
   ser.JonSharedDataTypes$JonGuiDataFxModeDay/JON_GUI_DATA_FX_MODE_DAY_A :jon-gui-data-fx-mode-day-a
   ser.JonSharedDataTypes$JonGuiDataFxModeDay/JON_GUI_DATA_FX_MODE_DAY_B :jon-gui-data-fx-mode-day-b
   ser.JonSharedDataTypes$JonGuiDataFxModeDay/JON_GUI_DATA_FX_MODE_DAY_C :jon-gui-data-fx-mode-day-c
   ser.JonSharedDataTypes$JonGuiDataFxModeDay/JON_GUI_DATA_FX_MODE_DAY_D :jon-gui-data-fx-mode-day-d
   ser.JonSharedDataTypes$JonGuiDataFxModeDay/JON_GUI_DATA_FX_MODE_DAY_E :jon-gui-data-fx-mode-day-e
   ser.JonSharedDataTypes$JonGuiDataFxModeDay/JON_GUI_DATA_FX_MODE_DAY_F :jon-gui-data-fx-mode-day-f})

;; Enum: JonGuiDataFxModeHeat
(def jon-gui-data-fx-mode-heat-values
  "Keyword to Java enum mapping for JonGuiDataFxModeHeat."
  {:jon-gui-data-fx-mode-heat-default ser.JonSharedDataTypes$JonGuiDataFxModeHeat/JON_GUI_DATA_FX_MODE_HEAT_DEFAULT
   :jon-gui-data-fx-mode-heat-a ser.JonSharedDataTypes$JonGuiDataFxModeHeat/JON_GUI_DATA_FX_MODE_HEAT_A
   :jon-gui-data-fx-mode-heat-b ser.JonSharedDataTypes$JonGuiDataFxModeHeat/JON_GUI_DATA_FX_MODE_HEAT_B
   :jon-gui-data-fx-mode-heat-c ser.JonSharedDataTypes$JonGuiDataFxModeHeat/JON_GUI_DATA_FX_MODE_HEAT_C
   :jon-gui-data-fx-mode-heat-d ser.JonSharedDataTypes$JonGuiDataFxModeHeat/JON_GUI_DATA_FX_MODE_HEAT_D
   :jon-gui-data-fx-mode-heat-e ser.JonSharedDataTypes$JonGuiDataFxModeHeat/JON_GUI_DATA_FX_MODE_HEAT_E
   :jon-gui-data-fx-mode-heat-f ser.JonSharedDataTypes$JonGuiDataFxModeHeat/JON_GUI_DATA_FX_MODE_HEAT_F})

(def jon-gui-data-fx-mode-heat-keywords
  "Java enum to keyword mapping for JonGuiDataFxModeHeat."
  {ser.JonSharedDataTypes$JonGuiDataFxModeHeat/JON_GUI_DATA_FX_MODE_HEAT_DEFAULT :jon-gui-data-fx-mode-heat-default
   ser.JonSharedDataTypes$JonGuiDataFxModeHeat/JON_GUI_DATA_FX_MODE_HEAT_A :jon-gui-data-fx-mode-heat-a
   ser.JonSharedDataTypes$JonGuiDataFxModeHeat/JON_GUI_DATA_FX_MODE_HEAT_B :jon-gui-data-fx-mode-heat-b
   ser.JonSharedDataTypes$JonGuiDataFxModeHeat/JON_GUI_DATA_FX_MODE_HEAT_C :jon-gui-data-fx-mode-heat-c
   ser.JonSharedDataTypes$JonGuiDataFxModeHeat/JON_GUI_DATA_FX_MODE_HEAT_D :jon-gui-data-fx-mode-heat-d
   ser.JonSharedDataTypes$JonGuiDataFxModeHeat/JON_GUI_DATA_FX_MODE_HEAT_E :jon-gui-data-fx-mode-heat-e
   ser.JonSharedDataTypes$JonGuiDataFxModeHeat/JON_GUI_DATA_FX_MODE_HEAT_F :jon-gui-data-fx-mode-heat-f})

;; Enum: JonGuiDataSystemLocalizations
(def jon-gui-data-system-localizations-values
  "Keyword to Java enum mapping for JonGuiDataSystemLocalizations."
  {:jon-gui-data-system-localization-unspecified ser.JonSharedDataTypes$JonGuiDataSystemLocalizations/JON_GUI_DATA_SYSTEM_LOCALIZATION_UNSPECIFIED
   :jon-gui-data-system-localization-en ser.JonSharedDataTypes$JonGuiDataSystemLocalizations/JON_GUI_DATA_SYSTEM_LOCALIZATION_EN
   :jon-gui-data-system-localization-ua ser.JonSharedDataTypes$JonGuiDataSystemLocalizations/JON_GUI_DATA_SYSTEM_LOCALIZATION_UA
   :jon-gui-data-system-localization-ar ser.JonSharedDataTypes$JonGuiDataSystemLocalizations/JON_GUI_DATA_SYSTEM_LOCALIZATION_AR
   :jon-gui-data-system-localization-cs ser.JonSharedDataTypes$JonGuiDataSystemLocalizations/JON_GUI_DATA_SYSTEM_LOCALIZATION_CS})

(def jon-gui-data-system-localizations-keywords
  "Java enum to keyword mapping for JonGuiDataSystemLocalizations."
  {ser.JonSharedDataTypes$JonGuiDataSystemLocalizations/JON_GUI_DATA_SYSTEM_LOCALIZATION_UNSPECIFIED :jon-gui-data-system-localization-unspecified
   ser.JonSharedDataTypes$JonGuiDataSystemLocalizations/JON_GUI_DATA_SYSTEM_LOCALIZATION_EN :jon-gui-data-system-localization-en
   ser.JonSharedDataTypes$JonGuiDataSystemLocalizations/JON_GUI_DATA_SYSTEM_LOCALIZATION_UA :jon-gui-data-system-localization-ua
   ser.JonSharedDataTypes$JonGuiDataSystemLocalizations/JON_GUI_DATA_SYSTEM_LOCALIZATION_AR :jon-gui-data-system-localization-ar
   ser.JonSharedDataTypes$JonGuiDataSystemLocalizations/JON_GUI_DATA_SYSTEM_LOCALIZATION_CS :jon-gui-data-system-localization-cs})

;; Enum: JonGuiDataClientType
(def jon-gui-data-client-type-values
  "Keyword to Java enum mapping for JonGuiDataClientType."
  {:jon-gui-data-client-type-unspecified ser.JonSharedDataTypes$JonGuiDataClientType/JON_GUI_DATA_CLIENT_TYPE_UNSPECIFIED
   :jon-gui-data-client-type-internal-cv ser.JonSharedDataTypes$JonGuiDataClientType/JON_GUI_DATA_CLIENT_TYPE_INTERNAL_CV
   :jon-gui-data-client-type-local-network ser.JonSharedDataTypes$JonGuiDataClientType/JON_GUI_DATA_CLIENT_TYPE_LOCAL_NETWORK
   :jon-gui-data-client-type-certificate-protected ser.JonSharedDataTypes$JonGuiDataClientType/JON_GUI_DATA_CLIENT_TYPE_CERTIFICATE_PROTECTED
   :jon-gui-data-client-type-lira ser.JonSharedDataTypes$JonGuiDataClientType/JON_GUI_DATA_CLIENT_TYPE_LIRA})

(def jon-gui-data-client-type-keywords
  "Java enum to keyword mapping for JonGuiDataClientType."
  {ser.JonSharedDataTypes$JonGuiDataClientType/JON_GUI_DATA_CLIENT_TYPE_UNSPECIFIED :jon-gui-data-client-type-unspecified
   ser.JonSharedDataTypes$JonGuiDataClientType/JON_GUI_DATA_CLIENT_TYPE_INTERNAL_CV :jon-gui-data-client-type-internal-cv
   ser.JonSharedDataTypes$JonGuiDataClientType/JON_GUI_DATA_CLIENT_TYPE_LOCAL_NETWORK :jon-gui-data-client-type-local-network
   ser.JonSharedDataTypes$JonGuiDataClientType/JON_GUI_DATA_CLIENT_TYPE_CERTIFICATE_PROTECTED :jon-gui-data-client-type-certificate-protected
   ser.JonSharedDataTypes$JonGuiDataClientType/JON_GUI_DATA_CLIENT_TYPE_LIRA :jon-gui-data-client-type-lira})

;; =============================================================================
;; Builders and Parsers
;; =============================================================================

(defn build-root-payload
  "Build the oneof payload for Root."
  [builder [field-key value]]
  (case field-key
    :start (.setStart builder (build-start value))
    :stop (.setStop builder (build-stop value))
    :set-magnetic-declination (.setSetMagneticDeclination builder (build-set-magnetic-declination value))
    :set-offset-angle-azimuth (.setSetOffsetAngleAzimuth builder (build-set-offset-angle-azimuth value))
    :set-offset-angle-elevation (.setSetOffsetAngleElevation builder (build-set-offset-angle-elevation value))
    :set-use-rotary-position (.setSetUseRotaryPosition builder (build-set-use-rotary-position value))
    :start-calibrate-long (.setStartCalibrateLong builder (build-calibrate-start-long value))
    :start-calibrate-short (.setStartCalibrateShort builder (build-calibrate-start-short value))
    :calibrate-next (.setCalibrateNext builder (build-calibrate-next value))
    :calibrate-cencel (.setCalibrateCencel builder (build-calibrate-cencel value))
    :get-meteo (.setGetMeteo builder (build-get-meteo value))
    (throw (ex-info "Unknown oneof field" {:field field-key :oneof ":cmd"}))))

(defn parse-root-payload
  "Parse the oneof payload from Root."
  [^cmd.Compass.JonSharedCmdCompass$Root proto]
  (cond
    (.hasStart proto) {:start (parse-start (.getStart proto))}
    (.hasStop proto) {:stop (parse-stop (.getStop proto))}
    (.hasSetMagneticDeclination proto) {:set-magnetic-declination (parse-set-magnetic-declination (.getSetMagneticDeclination proto))}
    (.hasSetOffsetAngleAzimuth proto) {:set-offset-angle-azimuth (parse-set-offset-angle-azimuth (.getSetOffsetAngleAzimuth proto))}
    (.hasSetOffsetAngleElevation proto) {:set-offset-angle-elevation (parse-set-offset-angle-elevation (.getSetOffsetAngleElevation proto))}
    (.hasSetUseRotaryPosition proto) {:set-use-rotary-position (parse-set-use-rotary-position (.getSetUseRotaryPosition proto))}
    (.hasStartCalibrateLong proto) {:start-calibrate-long (parse-calibrate-start-long (.getStartCalibrateLong proto))}
    (.hasStartCalibrateShort proto) {:start-calibrate-short (parse-calibrate-start-short (.getStartCalibrateShort proto))}
    (.hasCalibrateNext proto) {:calibrate-next (parse-calibrate-next (.getCalibrateNext proto))}
    (.hasCalibrateCencel proto) {:calibrate-cencel (parse-calibrate-cencel (.getCalibrateCencel proto))}
    (.hasGetMeteo proto) {:get-meteo (parse-get-meteo (.getGetMeteo proto))}))

(defn build-root
  "Build a Root protobuf message from a map."
  [m]
  (let [builder (cmd.Compass.JonSharedCmdCompass$Root/newBuilder)]

;; Handle oneof: cmd
    (when-let [cmd-field (first (filter (fn [[k v]] (#{:start :stop :set-magnetic-declination :set-offset-angle-azimuth :set-offset-angle-elevation :set-use-rotary-position :start-calibrate-long :start-calibrate-short :calibrate-next :calibrate-cencel :get-meteo} k)) m))]
      (build-root-payload builder cmd-field))
    (.build builder)))

(defn parse-root
  "Parse a Root protobuf message to a map."
  [^cmd.Compass.JonSharedCmdCompass$Root proto]
  (cond-> {}

;; Oneof payload
    true (merge (parse-root-payload proto))))

(defn build-start
  "Build a Start protobuf message from a map."
  [m]
  (let [builder (cmd.Compass.JonSharedCmdCompass$Start/newBuilder)]

    (.build builder)))

(defn parse-start
  "Parse a Start protobuf message to a map."
  [^cmd.Compass.JonSharedCmdCompass$Start proto]
  (cond-> {}))

(defn build-stop
  "Build a Stop protobuf message from a map."
  [m]
  (let [builder (cmd.Compass.JonSharedCmdCompass$Stop/newBuilder)]

    (.build builder)))

(defn parse-stop
  "Parse a Stop protobuf message to a map."
  [^cmd.Compass.JonSharedCmdCompass$Stop proto]
  (cond-> {}))

(defn build-next
  "Build a Next protobuf message from a map."
  [m]
  (let [builder (cmd.Compass.JonSharedCmdCompass$Next/newBuilder)]

    (.build builder)))

(defn parse-next
  "Parse a Next protobuf message to a map."
  [^cmd.Compass.JonSharedCmdCompass$Next proto]
  (cond-> {}))

(defn build-calibrate-start-long
  "Build a CalibrateStartLong protobuf message from a map."
  [m]
  (let [builder (cmd.Compass.JonSharedCmdCompass$CalibrateStartLong/newBuilder)]

    (.build builder)))

(defn parse-calibrate-start-long
  "Parse a CalibrateStartLong protobuf message to a map."
  [^cmd.Compass.JonSharedCmdCompass$CalibrateStartLong proto]
  (cond-> {}))

(defn build-calibrate-start-short
  "Build a CalibrateStartShort protobuf message from a map."
  [m]
  (let [builder (cmd.Compass.JonSharedCmdCompass$CalibrateStartShort/newBuilder)]

    (.build builder)))

(defn parse-calibrate-start-short
  "Parse a CalibrateStartShort protobuf message to a map."
  [^cmd.Compass.JonSharedCmdCompass$CalibrateStartShort proto]
  (cond-> {}))

(defn build-calibrate-next
  "Build a CalibrateNext protobuf message from a map."
  [m]
  (let [builder (cmd.Compass.JonSharedCmdCompass$CalibrateNext/newBuilder)]

    (.build builder)))

(defn parse-calibrate-next
  "Parse a CalibrateNext protobuf message to a map."
  [^cmd.Compass.JonSharedCmdCompass$CalibrateNext proto]
  (cond-> {}))

(defn build-calibrate-cencel
  "Build a CalibrateCencel protobuf message from a map."
  [m]
  (let [builder (cmd.Compass.JonSharedCmdCompass$CalibrateCencel/newBuilder)]

    (.build builder)))

(defn parse-calibrate-cencel
  "Parse a CalibrateCencel protobuf message to a map."
  [^cmd.Compass.JonSharedCmdCompass$CalibrateCencel proto]
  (cond-> {}))

(defn build-get-meteo
  "Build a GetMeteo protobuf message from a map."
  [m]
  (let [builder (cmd.Compass.JonSharedCmdCompass$GetMeteo/newBuilder)]

    (.build builder)))

(defn parse-get-meteo
  "Parse a GetMeteo protobuf message to a map."
  [^cmd.Compass.JonSharedCmdCompass$GetMeteo proto]
  (cond-> {}))

(defn build-set-magnetic-declination
  "Build a SetMagneticDeclination protobuf message from a map."
  [m]
  (let [builder (cmd.Compass.JonSharedCmdCompass$SetMagneticDeclination/newBuilder)]
    ;; Set regular fields
    (when (contains? m :value)
      (.setValue builder (get m :value)))

    (.build builder)))

(defn parse-set-magnetic-declination
  "Parse a SetMagneticDeclination protobuf message to a map."
  [^cmd.Compass.JonSharedCmdCompass$SetMagneticDeclination proto]
  (cond-> {}
    ;; Regular fields
    (.hasValue proto) (assoc :value (.getValue proto))))

(defn build-set-offset-angle-azimuth
  "Build a SetOffsetAngleAzimuth protobuf message from a map."
  [m]
  (let [builder (cmd.Compass.JonSharedCmdCompass$SetOffsetAngleAzimuth/newBuilder)]
    ;; Set regular fields
    (when (contains? m :value)
      (.setValue builder (get m :value)))

    (.build builder)))

(defn parse-set-offset-angle-azimuth
  "Parse a SetOffsetAngleAzimuth protobuf message to a map."
  [^cmd.Compass.JonSharedCmdCompass$SetOffsetAngleAzimuth proto]
  (cond-> {}
    ;; Regular fields
    (.hasValue proto) (assoc :value (.getValue proto))))

(defn build-set-offset-angle-elevation
  "Build a SetOffsetAngleElevation protobuf message from a map."
  [m]
  (let [builder (cmd.Compass.JonSharedCmdCompass$SetOffsetAngleElevation/newBuilder)]
    ;; Set regular fields
    (when (contains? m :value)
      (.setValue builder (get m :value)))

    (.build builder)))

(defn parse-set-offset-angle-elevation
  "Parse a SetOffsetAngleElevation protobuf message to a map."
  [^cmd.Compass.JonSharedCmdCompass$SetOffsetAngleElevation proto]
  (cond-> {}
    ;; Regular fields
    (.hasValue proto) (assoc :value (.getValue proto))))

(defn build-set-use-rotary-position
  "Build a SetUseRotaryPosition protobuf message from a map."
  [m]
  (let [builder (cmd.Compass.JonSharedCmdCompass$SetUseRotaryPosition/newBuilder)]
    ;; Set regular fields
    (when (contains? m :flag)
      (.setFlag builder (get m :flag)))

    (.build builder)))

(defn parse-set-use-rotary-position
  "Parse a SetUseRotaryPosition protobuf message to a map."
  [^cmd.Compass.JonSharedCmdCompass$SetUseRotaryPosition proto]
  (cond-> {}
    ;; Regular fields
    (.hasFlag proto) (assoc :flag (.getFlag proto))))

(defn build-root-payload
  "Build the oneof payload for Root."
  [builder [field-key value]]
  (case field-key
    :start (.setStart builder (build-start value))
    :stop (.setStop builder (build-stop value))
    :set-manual-position (.setSetManualPosition builder (build-set-manual-position value))
    :set-use-manual-position (.setSetUseManualPosition builder (build-set-use-manual-position value))
    :get-meteo (.setGetMeteo builder (build-get-meteo value))
    (throw (ex-info "Unknown oneof field" {:field field-key :oneof ":cmd"}))))

(defn parse-root-payload
  "Parse the oneof payload from Root."
  [^cmd.Gps.JonSharedCmdGps$Root proto]
  (cond
    (.hasStart proto) {:start (parse-start (.getStart proto))}
    (.hasStop proto) {:stop (parse-stop (.getStop proto))}
    (.hasSetManualPosition proto) {:set-manual-position (parse-set-manual-position (.getSetManualPosition proto))}
    (.hasSetUseManualPosition proto) {:set-use-manual-position (parse-set-use-manual-position (.getSetUseManualPosition proto))}
    (.hasGetMeteo proto) {:get-meteo (parse-get-meteo (.getGetMeteo proto))}))

(defn build-root
  "Build a Root protobuf message from a map."
  [m]
  (let [builder (cmd.Gps.JonSharedCmdGps$Root/newBuilder)]

;; Handle oneof: cmd
    (when-let [cmd-field (first (filter (fn [[k v]] (#{:start :stop :set-manual-position :set-use-manual-position :get-meteo} k)) m))]
      (build-root-payload builder cmd-field))
    (.build builder)))

(defn parse-root
  "Parse a Root protobuf message to a map."
  [^cmd.Gps.JonSharedCmdGps$Root proto]
  (cond-> {}

;; Oneof payload
    true (merge (parse-root-payload proto))))

(defn build-start
  "Build a Start protobuf message from a map."
  [m]
  (let [builder (cmd.Gps.JonSharedCmdGps$Start/newBuilder)]

    (.build builder)))

(defn parse-start
  "Parse a Start protobuf message to a map."
  [^cmd.Gps.JonSharedCmdGps$Start proto]
  (cond-> {}))

(defn build-stop
  "Build a Stop protobuf message from a map."
  [m]
  (let [builder (cmd.Gps.JonSharedCmdGps$Stop/newBuilder)]

    (.build builder)))

(defn parse-stop
  "Parse a Stop protobuf message to a map."
  [^cmd.Gps.JonSharedCmdGps$Stop proto]
  (cond-> {}))

(defn build-get-meteo
  "Build a GetMeteo protobuf message from a map."
  [m]
  (let [builder (cmd.Gps.JonSharedCmdGps$GetMeteo/newBuilder)]

    (.build builder)))

(defn parse-get-meteo
  "Parse a GetMeteo protobuf message to a map."
  [^cmd.Gps.JonSharedCmdGps$GetMeteo proto]
  (cond-> {}))

(defn build-set-use-manual-position
  "Build a SetUseManualPosition protobuf message from a map."
  [m]
  (let [builder (cmd.Gps.JonSharedCmdGps$SetUseManualPosition/newBuilder)]
    ;; Set regular fields
    (when (contains? m :flag)
      (.setFlag builder (get m :flag)))

    (.build builder)))

(defn parse-set-use-manual-position
  "Parse a SetUseManualPosition protobuf message to a map."
  [^cmd.Gps.JonSharedCmdGps$SetUseManualPosition proto]
  (cond-> {}
    ;; Regular fields
    (.hasFlag proto) (assoc :flag (.getFlag proto))))

(defn build-set-manual-position
  "Build a SetManualPosition protobuf message from a map."
  [m]
  (let [builder (cmd.Gps.JonSharedCmdGps$SetManualPosition/newBuilder)]
    ;; Set regular fields
    (when (contains? m :latitude)
      (.setLatitude builder (get m :latitude)))
    (when (contains? m :longitude)
      (.setLongitude builder (get m :longitude)))
    (when (contains? m :altitude)
      (.setAltitude builder (get m :altitude)))

    (.build builder)))

(defn parse-set-manual-position
  "Parse a SetManualPosition protobuf message to a map."
  [^cmd.Gps.JonSharedCmdGps$SetManualPosition proto]
  (cond-> {}
    ;; Regular fields
    (.hasLatitude proto) (assoc :latitude (.getLatitude proto))
    (.hasLongitude proto) (assoc :longitude (.getLongitude proto))
    (.hasAltitude proto) (assoc :altitude (.getAltitude proto))))

(defn build-jon-gui-data-meteo
  "Build a JonGuiDataMeteo protobuf message from a map."
  [m]
  (let [builder (ser.JonSharedDataTypes$JonGuiDataMeteo/newBuilder)]
    ;; Set regular fields
    (when (contains? m :temperature)
      (.setTemperature builder (get m :temperature)))
    (when (contains? m :humidity)
      (.setHumidity builder (get m :humidity)))
    (when (contains? m :pressure)
      (.setPressure builder (get m :pressure)))

    (.build builder)))

(defn parse-jon-gui-data-meteo
  "Parse a JonGuiDataMeteo protobuf message to a map."
  [^ser.JonSharedDataTypes$JonGuiDataMeteo proto]
  (cond-> {}
    ;; Regular fields
    (.hasTemperature proto) (assoc :temperature (.getTemperature proto))
    (.hasHumidity proto) (assoc :humidity (.getHumidity proto))
    (.hasPressure proto) (assoc :pressure (.getPressure proto))))

(defn build-root-payload
  "Build the oneof payload for Root."
  [builder [field-key value]]
  (case field-key
    :measure (.setMeasure builder (build-measure value))
    :scan-on (.setScanOn builder (build-scan-on value))
    :scan-off (.setScanOff builder (build-scan-off value))
    :start (.setStart builder (build-start value))
    :stop (.setStop builder (build-stop value))
    :target-designator-off (.setTargetDesignatorOff builder (build-target-designator-off value))
    :target-designator-on-mode-a (.setTargetDesignatorOnModeA builder (build-target-designator-on-mode-a value))
    :target-designator-on-mode-b (.setTargetDesignatorOnModeB builder (build-target-designator-on-mode-b value))
    :enable-fog-mode (.setEnableFogMode builder (build-enable-fog-mode value))
    :disable-fog-mode (.setDisableFogMode builder (build-disable-fog-mode value))
    :set-scan-mode (.setSetScanMode builder (build-set-scan-mode value))
    :new-session (.setNewSession builder (build-new-session value))
    :get-meteo (.setGetMeteo builder (build-get-meteo value))
    :refine-on (.setRefineOn builder (build-refine-on value))
    :refine-off (.setRefineOff builder (build-refine-off value))
    (throw (ex-info "Unknown oneof field" {:field field-key :oneof ":cmd"}))))

(defn parse-root-payload
  "Parse the oneof payload from Root."
  [^cmd.Lrf.JonSharedCmdLrf$Root proto]
  (cond
    (.hasMeasure proto) {:measure (parse-measure (.getMeasure proto))}
    (.hasScanOn proto) {:scan-on (parse-scan-on (.getScanOn proto))}
    (.hasScanOff proto) {:scan-off (parse-scan-off (.getScanOff proto))}
    (.hasStart proto) {:start (parse-start (.getStart proto))}
    (.hasStop proto) {:stop (parse-stop (.getStop proto))}
    (.hasTargetDesignatorOff proto) {:target-designator-off (parse-target-designator-off (.getTargetDesignatorOff proto))}
    (.hasTargetDesignatorOnModeA proto) {:target-designator-on-mode-a (parse-target-designator-on-mode-a (.getTargetDesignatorOnModeA proto))}
    (.hasTargetDesignatorOnModeB proto) {:target-designator-on-mode-b (parse-target-designator-on-mode-b (.getTargetDesignatorOnModeB proto))}
    (.hasEnableFogMode proto) {:enable-fog-mode (parse-enable-fog-mode (.getEnableFogMode proto))}
    (.hasDisableFogMode proto) {:disable-fog-mode (parse-disable-fog-mode (.getDisableFogMode proto))}
    (.hasSetScanMode proto) {:set-scan-mode (parse-set-scan-mode (.getSetScanMode proto))}
    (.hasNewSession proto) {:new-session (parse-new-session (.getNewSession proto))}
    (.hasGetMeteo proto) {:get-meteo (parse-get-meteo (.getGetMeteo proto))}
    (.hasRefineOn proto) {:refine-on (parse-refine-on (.getRefineOn proto))}
    (.hasRefineOff proto) {:refine-off (parse-refine-off (.getRefineOff proto))}))

(defn build-root
  "Build a Root protobuf message from a map."
  [m]
  (let [builder (cmd.Lrf.JonSharedCmdLrf$Root/newBuilder)]

;; Handle oneof: cmd
    (when-let [cmd-field (first (filter (fn [[k v]] (#{:measure :scan-on :scan-off :start :stop :target-designator-off :target-designator-on-mode-a :target-designator-on-mode-b :enable-fog-mode :disable-fog-mode :set-scan-mode :new-session :get-meteo :refine-on :refine-off} k)) m))]
      (build-root-payload builder cmd-field))
    (.build builder)))

(defn parse-root
  "Parse a Root protobuf message to a map."
  [^cmd.Lrf.JonSharedCmdLrf$Root proto]
  (cond-> {}

;; Oneof payload
    true (merge (parse-root-payload proto))))

(defn build-get-meteo
  "Build a GetMeteo protobuf message from a map."
  [m]
  (let [builder (cmd.Lrf.JonSharedCmdLrf$GetMeteo/newBuilder)]

    (.build builder)))

(defn parse-get-meteo
  "Parse a GetMeteo protobuf message to a map."
  [^cmd.Lrf.JonSharedCmdLrf$GetMeteo proto]
  (cond-> {}))

(defn build-start
  "Build a Start protobuf message from a map."
  [m]
  (let [builder (cmd.Lrf.JonSharedCmdLrf$Start/newBuilder)]

    (.build builder)))

(defn parse-start
  "Parse a Start protobuf message to a map."
  [^cmd.Lrf.JonSharedCmdLrf$Start proto]
  (cond-> {}))

(defn build-stop
  "Build a Stop protobuf message from a map."
  [m]
  (let [builder (cmd.Lrf.JonSharedCmdLrf$Stop/newBuilder)]

    (.build builder)))

(defn parse-stop
  "Parse a Stop protobuf message to a map."
  [^cmd.Lrf.JonSharedCmdLrf$Stop proto]
  (cond-> {}))

(defn build-measure
  "Build a Measure protobuf message from a map."
  [m]
  (let [builder (cmd.Lrf.JonSharedCmdLrf$Measure/newBuilder)]

    (.build builder)))

(defn parse-measure
  "Parse a Measure protobuf message to a map."
  [^cmd.Lrf.JonSharedCmdLrf$Measure proto]
  (cond-> {}))

(defn build-scan-on
  "Build a ScanOn protobuf message from a map."
  [m]
  (let [builder (cmd.Lrf.JonSharedCmdLrf$ScanOn/newBuilder)]

    (.build builder)))

(defn parse-scan-on
  "Parse a ScanOn protobuf message to a map."
  [^cmd.Lrf.JonSharedCmdLrf$ScanOn proto]
  (cond-> {}))

(defn build-scan-off
  "Build a ScanOff protobuf message from a map."
  [m]
  (let [builder (cmd.Lrf.JonSharedCmdLrf$ScanOff/newBuilder)]

    (.build builder)))

(defn parse-scan-off
  "Parse a ScanOff protobuf message to a map."
  [^cmd.Lrf.JonSharedCmdLrf$ScanOff proto]
  (cond-> {}))

(defn build-refine-off
  "Build a RefineOff protobuf message from a map."
  [m]
  (let [builder (cmd.Lrf.JonSharedCmdLrf$RefineOff/newBuilder)]

    (.build builder)))

(defn parse-refine-off
  "Parse a RefineOff protobuf message to a map."
  [^cmd.Lrf.JonSharedCmdLrf$RefineOff proto]
  (cond-> {}))

(defn build-refine-on
  "Build a RefineOn protobuf message from a map."
  [m]
  (let [builder (cmd.Lrf.JonSharedCmdLrf$RefineOn/newBuilder)]

    (.build builder)))

(defn parse-refine-on
  "Parse a RefineOn protobuf message to a map."
  [^cmd.Lrf.JonSharedCmdLrf$RefineOn proto]
  (cond-> {}))

(defn build-target-designator-off
  "Build a TargetDesignatorOff protobuf message from a map."
  [m]
  (let [builder (cmd.Lrf.JonSharedCmdLrf$TargetDesignatorOff/newBuilder)]

    (.build builder)))

(defn parse-target-designator-off
  "Parse a TargetDesignatorOff protobuf message to a map."
  [^cmd.Lrf.JonSharedCmdLrf$TargetDesignatorOff proto]
  (cond-> {}))

(defn build-target-designator-on-mode-a
  "Build a TargetDesignatorOnModeA protobuf message from a map."
  [m]
  (let [builder (cmd.Lrf.JonSharedCmdLrf$TargetDesignatorOnModeA/newBuilder)]

    (.build builder)))

(defn parse-target-designator-on-mode-a
  "Parse a TargetDesignatorOnModeA protobuf message to a map."
  [^cmd.Lrf.JonSharedCmdLrf$TargetDesignatorOnModeA proto]
  (cond-> {}))

(defn build-target-designator-on-mode-b
  "Build a TargetDesignatorOnModeB protobuf message from a map."
  [m]
  (let [builder (cmd.Lrf.JonSharedCmdLrf$TargetDesignatorOnModeB/newBuilder)]

    (.build builder)))

(defn parse-target-designator-on-mode-b
  "Parse a TargetDesignatorOnModeB protobuf message to a map."
  [^cmd.Lrf.JonSharedCmdLrf$TargetDesignatorOnModeB proto]
  (cond-> {}))

(defn build-enable-fog-mode
  "Build a EnableFogMode protobuf message from a map."
  [m]
  (let [builder (cmd.Lrf.JonSharedCmdLrf$EnableFogMode/newBuilder)]

    (.build builder)))

(defn parse-enable-fog-mode
  "Parse a EnableFogMode protobuf message to a map."
  [^cmd.Lrf.JonSharedCmdLrf$EnableFogMode proto]
  (cond-> {}))

(defn build-disable-fog-mode
  "Build a DisableFogMode protobuf message from a map."
  [m]
  (let [builder (cmd.Lrf.JonSharedCmdLrf$DisableFogMode/newBuilder)]

    (.build builder)))

(defn parse-disable-fog-mode
  "Parse a DisableFogMode protobuf message to a map."
  [^cmd.Lrf.JonSharedCmdLrf$DisableFogMode proto]
  (cond-> {}))

(defn build-set-scan-mode
  "Build a SetScanMode protobuf message from a map."
  [m]
  (let [builder (cmd.Lrf.JonSharedCmdLrf$SetScanMode/newBuilder)]
    ;; Set regular fields
    (when (contains? m :mode)
      (.setMode builder (get m :mode)))

    (.build builder)))

(defn parse-set-scan-mode
  "Parse a SetScanMode protobuf message to a map."
  [^cmd.Lrf.JonSharedCmdLrf$SetScanMode proto]
  (cond-> {}
    ;; Regular fields
    (.hasMode proto) (assoc :mode (.getMode proto))))

(defn build-new-session
  "Build a NewSession protobuf message from a map."
  [m]
  (let [builder (cmd.Lrf.JonSharedCmdLrf$NewSession/newBuilder)]

    (.build builder)))

(defn parse-new-session
  "Parse a NewSession protobuf message to a map."
  [^cmd.Lrf.JonSharedCmdLrf$NewSession proto]
  (cond-> {}))

(defn build-set-value
  "Build a SetValue protobuf message from a map."
  [m]
  (let [builder (cmd.DayCamera.JonSharedCmdDayCamera$SetValue/newBuilder)]
    ;; Set regular fields
    (when (contains? m :value)
      (.setValue builder (get m :value)))

    (.build builder)))

(defn parse-set-value
  "Parse a SetValue protobuf message to a map."
  [^cmd.DayCamera.JonSharedCmdDayCamera$SetValue proto]
  (cond-> {}
    ;; Regular fields
    (.hasValue proto) (assoc :value (.getValue proto))))

(defn build-move
  "Build a Move protobuf message from a map."
  [m]
  (let [builder (cmd.DayCamera.JonSharedCmdDayCamera$Move/newBuilder)]
    ;; Set regular fields
    (when (contains? m :target-value)
      (.setTargetValue builder (get m :target-value)))
    (when (contains? m :speed)
      (.setSpeed builder (get m :speed)))

    (.build builder)))

(defn parse-move
  "Parse a Move protobuf message to a map."
  [^cmd.DayCamera.JonSharedCmdDayCamera$Move proto]
  (cond-> {}
    ;; Regular fields
    (.hasTargetValue proto) (assoc :target-value (.getTargetValue proto))
    (.hasSpeed proto) (assoc :speed (.getSpeed proto))))

(defn build-offset
  "Build a Offset protobuf message from a map."
  [m]
  (let [builder (cmd.DayCamera.JonSharedCmdDayCamera$Offset/newBuilder)]
    ;; Set regular fields
    (when (contains? m :offset-value)
      (.setOffsetValue builder (get m :offset-value)))

    (.build builder)))

(defn parse-offset
  "Parse a Offset protobuf message to a map."
  [^cmd.DayCamera.JonSharedCmdDayCamera$Offset proto]
  (cond-> {}
    ;; Regular fields
    (.hasOffsetValue proto) (assoc :offset-value (.getOffsetValue proto))))

(defn build-set-clahe-level
  "Build a SetClaheLevel protobuf message from a map."
  [m]
  (let [builder (cmd.DayCamera.JonSharedCmdDayCamera$SetClaheLevel/newBuilder)]
    ;; Set regular fields
    (when (contains? m :value)
      (.setValue builder (get m :value)))

    (.build builder)))

(defn parse-set-clahe-level
  "Parse a SetClaheLevel protobuf message to a map."
  [^cmd.DayCamera.JonSharedCmdDayCamera$SetClaheLevel proto]
  (cond-> {}
    ;; Regular fields
    (.hasValue proto) (assoc :value (.getValue proto))))

(defn build-shift-clahe-level
  "Build a ShiftClaheLevel protobuf message from a map."
  [m]
  (let [builder (cmd.DayCamera.JonSharedCmdDayCamera$ShiftClaheLevel/newBuilder)]
    ;; Set regular fields
    (when (contains? m :value)
      (.setValue builder (get m :value)))

    (.build builder)))

(defn parse-shift-clahe-level
  "Parse a ShiftClaheLevel protobuf message to a map."
  [^cmd.DayCamera.JonSharedCmdDayCamera$ShiftClaheLevel proto]
  (cond-> {}
    ;; Regular fields
    (.hasValue proto) (assoc :value (.getValue proto))))

(defn build-root-payload
  "Build the oneof payload for Root."
  [builder [field-key value]]
  (case field-key
    :focus (.setFocus builder (build-focus value))
    :zoom (.setZoom builder (build-zoom value))
    :set-iris (.setSetIris builder (build-set-iris value))
    :set-infra-red-filter (.setSetInfraRedFilter builder (build-set-infra-red-filter value))
    :start (.setStart builder (build-start value))
    :stop (.setStop builder (build-stop value))
    :photo (.setPhoto builder (build-photo value))
    :set-auto-iris (.setSetAutoIris builder (build-set-auto-iris value))
    :halt-all (.setHaltAll builder (build-halt-all value))
    :set-fx-mode (.setSetFxMode builder (build-set-fx-mode value))
    :next-fx-mode (.setNextFxMode builder (build-next-fx-mode value))
    :prev-fx-mode (.setPrevFxMode builder (build-prev-fx-mode value))
    :get-meteo (.setGetMeteo builder (build-get-meteo value))
    :refresh-fx-mode (.setRefreshFxMode builder (build-refresh-fx-mode value))
    :set-digital-zoom-level (.setSetDigitalZoomLevel builder (build-set-digital-zoom-level value))
    :set-clahe-level (.setSetClaheLevel builder (build-set-clahe-level value))
    :shift-clahe-level (.setShiftClaheLevel builder (build-shift-clahe-level value))
    (throw (ex-info "Unknown oneof field" {:field field-key :oneof ":cmd"}))))

(defn parse-root-payload
  "Parse the oneof payload from Root."
  [^cmd.DayCamera.JonSharedCmdDayCamera$Root proto]
  (cond
    (.hasFocus proto) {:focus (parse-focus (.getFocus proto))}
    (.hasZoom proto) {:zoom (parse-zoom (.getZoom proto))}
    (.hasSetIris proto) {:set-iris (parse-set-iris (.getSetIris proto))}
    (.hasSetInfraRedFilter proto) {:set-infra-red-filter (parse-set-infra-red-filter (.getSetInfraRedFilter proto))}
    (.hasStart proto) {:start (parse-start (.getStart proto))}
    (.hasStop proto) {:stop (parse-stop (.getStop proto))}
    (.hasPhoto proto) {:photo (parse-photo (.getPhoto proto))}
    (.hasSetAutoIris proto) {:set-auto-iris (parse-set-auto-iris (.getSetAutoIris proto))}
    (.hasHaltAll proto) {:halt-all (parse-halt-all (.getHaltAll proto))}
    (.hasSetFxMode proto) {:set-fx-mode (parse-set-fx-mode (.getSetFxMode proto))}
    (.hasNextFxMode proto) {:next-fx-mode (parse-next-fx-mode (.getNextFxMode proto))}
    (.hasPrevFxMode proto) {:prev-fx-mode (parse-prev-fx-mode (.getPrevFxMode proto))}
    (.hasGetMeteo proto) {:get-meteo (parse-get-meteo (.getGetMeteo proto))}
    (.hasRefreshFxMode proto) {:refresh-fx-mode (parse-refresh-fx-mode (.getRefreshFxMode proto))}
    (.hasSetDigitalZoomLevel proto) {:set-digital-zoom-level (parse-set-digital-zoom-level (.getSetDigitalZoomLevel proto))}
    (.hasSetClaheLevel proto) {:set-clahe-level (parse-set-clahe-level (.getSetClaheLevel proto))}
    (.hasShiftClaheLevel proto) {:shift-clahe-level (parse-shift-clahe-level (.getShiftClaheLevel proto))}))

(defn build-root
  "Build a Root protobuf message from a map."
  [m]
  (let [builder (cmd.DayCamera.JonSharedCmdDayCamera$Root/newBuilder)]

;; Handle oneof: cmd
    (when-let [cmd-field (first (filter (fn [[k v]] (#{:focus :zoom :set-iris :set-infra-red-filter :start :stop :photo :set-auto-iris :halt-all :set-fx-mode :next-fx-mode :prev-fx-mode :get-meteo :refresh-fx-mode :set-digital-zoom-level :set-clahe-level :shift-clahe-level} k)) m))]
      (build-root-payload builder cmd-field))
    (.build builder)))

(defn parse-root
  "Parse a Root protobuf message to a map."
  [^cmd.DayCamera.JonSharedCmdDayCamera$Root proto]
  (cond-> {}

;; Oneof payload
    true (merge (parse-root-payload proto))))

(defn build-get-pos
  "Build a GetPos protobuf message from a map."
  [m]
  (let [builder (cmd.DayCamera.JonSharedCmdDayCamera$GetPos/newBuilder)]

    (.build builder)))

(defn parse-get-pos
  "Parse a GetPos protobuf message to a map."
  [^cmd.DayCamera.JonSharedCmdDayCamera$GetPos proto]
  (cond-> {}))

(defn build-next-fx-mode
  "Build a NextFxMode protobuf message from a map."
  [m]
  (let [builder (cmd.DayCamera.JonSharedCmdDayCamera$NextFxMode/newBuilder)]

    (.build builder)))

(defn parse-next-fx-mode
  "Parse a NextFxMode protobuf message to a map."
  [^cmd.DayCamera.JonSharedCmdDayCamera$NextFxMode proto]
  (cond-> {}))

(defn build-prev-fx-mode
  "Build a PrevFxMode protobuf message from a map."
  [m]
  (let [builder (cmd.DayCamera.JonSharedCmdDayCamera$PrevFxMode/newBuilder)]

    (.build builder)))

(defn parse-prev-fx-mode
  "Parse a PrevFxMode protobuf message to a map."
  [^cmd.DayCamera.JonSharedCmdDayCamera$PrevFxMode proto]
  (cond-> {}))

(defn build-refresh-fx-mode
  "Build a RefreshFxMode protobuf message from a map."
  [m]
  (let [builder (cmd.DayCamera.JonSharedCmdDayCamera$RefreshFxMode/newBuilder)]

    (.build builder)))

(defn parse-refresh-fx-mode
  "Parse a RefreshFxMode protobuf message to a map."
  [^cmd.DayCamera.JonSharedCmdDayCamera$RefreshFxMode proto]
  (cond-> {}))

(defn build-halt-all
  "Build a HaltAll protobuf message from a map."
  [m]
  (let [builder (cmd.DayCamera.JonSharedCmdDayCamera$HaltAll/newBuilder)]

    (.build builder)))

(defn parse-halt-all
  "Parse a HaltAll protobuf message to a map."
  [^cmd.DayCamera.JonSharedCmdDayCamera$HaltAll proto]
  (cond-> {}))

(defn build-set-fx-mode
  "Build a SetFxMode protobuf message from a map."
  [m]
  (let [builder (cmd.DayCamera.JonSharedCmdDayCamera$SetFxMode/newBuilder)]
    ;; Set regular fields
    (when (contains? m :mode)
      (.setMode builder (get m :mode)))

    (.build builder)))

(defn parse-set-fx-mode
  "Parse a SetFxMode protobuf message to a map."
  [^cmd.DayCamera.JonSharedCmdDayCamera$SetFxMode proto]
  (cond-> {}
    ;; Regular fields
    (.hasMode proto) (assoc :mode (.getMode proto))))

(defn build-set-digital-zoom-level
  "Build a SetDigitalZoomLevel protobuf message from a map."
  [m]
  (let [builder (cmd.DayCamera.JonSharedCmdDayCamera$SetDigitalZoomLevel/newBuilder)]
    ;; Set regular fields
    (when (contains? m :value)
      (.setValue builder (get m :value)))

    (.build builder)))

(defn parse-set-digital-zoom-level
  "Parse a SetDigitalZoomLevel protobuf message to a map."
  [^cmd.DayCamera.JonSharedCmdDayCamera$SetDigitalZoomLevel proto]
  (cond-> {}
    ;; Regular fields
    (.hasValue proto) (assoc :value (.getValue proto))))

(defn build-focus-payload
  "Build the oneof payload for Focus."
  [builder [field-key value]]
  (case field-key
    :set-value (.setSetValue builder (build-set-value value))
    :move (.setMove builder (build-move value))
    :halt (.setHalt builder (build-halt value))
    :offset (.setOffset builder (build-offset value))
    :reset-focus (.setResetFocus builder (build-reset-focus value))
    :save-to-table-focus (.setSaveToTableFocus builder (build-save-to-table-focus value))
    (throw (ex-info "Unknown oneof field" {:field field-key :oneof ":cmd"}))))

(defn parse-focus-payload
  "Parse the oneof payload from Focus."
  [^cmd.DayCamera.JonSharedCmdDayCamera$Focus proto]
  (cond
    (.hasSetValue proto) {:set-value (parse-set-value (.getSetValue proto))}
    (.hasMove proto) {:move (parse-move (.getMove proto))}
    (.hasHalt proto) {:halt (parse-halt (.getHalt proto))}
    (.hasOffset proto) {:offset (parse-offset (.getOffset proto))}
    (.hasResetFocus proto) {:reset-focus (parse-reset-focus (.getResetFocus proto))}
    (.hasSaveToTableFocus proto) {:save-to-table-focus (parse-save-to-table-focus (.getSaveToTableFocus proto))}))

(defn build-focus
  "Build a Focus protobuf message from a map."
  [m]
  (let [builder (cmd.DayCamera.JonSharedCmdDayCamera$Focus/newBuilder)]

;; Handle oneof: cmd
    (when-let [cmd-field (first (filter (fn [[k v]] (#{:set-value :move :halt :offset :reset-focus :save-to-table-focus} k)) m))]
      (build-focus-payload builder cmd-field))
    (.build builder)))

(defn parse-focus
  "Parse a Focus protobuf message to a map."
  [^cmd.DayCamera.JonSharedCmdDayCamera$Focus proto]
  (cond-> {}

;; Oneof payload
    true (merge (parse-focus-payload proto))))

(defn build-zoom-payload
  "Build the oneof payload for Zoom."
  [builder [field-key value]]
  (case field-key
    :set-value (.setSetValue builder (build-set-value value))
    :move (.setMove builder (build-move value))
    :halt (.setHalt builder (build-halt value))
    :set-zoom-table-value (.setSetZoomTableValue builder (build-set-zoom-table-value value))
    :next-zoom-table-pos (.setNextZoomTablePos builder (build-next-zoom-table-pos value))
    :prev-zoom-table-pos (.setPrevZoomTablePos builder (build-prev-zoom-table-pos value))
    :offset (.setOffset builder (build-offset value))
    :reset-zoom (.setResetZoom builder (build-reset-zoom value))
    :save-to-table (.setSaveToTable builder (build-save-to-table value))
    (throw (ex-info "Unknown oneof field" {:field field-key :oneof ":cmd"}))))

(defn parse-zoom-payload
  "Parse the oneof payload from Zoom."
  [^cmd.DayCamera.JonSharedCmdDayCamera$Zoom proto]
  (cond
    (.hasSetValue proto) {:set-value (parse-set-value (.getSetValue proto))}
    (.hasMove proto) {:move (parse-move (.getMove proto))}
    (.hasHalt proto) {:halt (parse-halt (.getHalt proto))}
    (.hasSetZoomTableValue proto) {:set-zoom-table-value (parse-set-zoom-table-value (.getSetZoomTableValue proto))}
    (.hasNextZoomTablePos proto) {:next-zoom-table-pos (parse-next-zoom-table-pos (.getNextZoomTablePos proto))}
    (.hasPrevZoomTablePos proto) {:prev-zoom-table-pos (parse-prev-zoom-table-pos (.getPrevZoomTablePos proto))}
    (.hasOffset proto) {:offset (parse-offset (.getOffset proto))}
    (.hasResetZoom proto) {:reset-zoom (parse-reset-zoom (.getResetZoom proto))}
    (.hasSaveToTable proto) {:save-to-table (parse-save-to-table (.getSaveToTable proto))}))

(defn build-zoom
  "Build a Zoom protobuf message from a map."
  [m]
  (let [builder (cmd.DayCamera.JonSharedCmdDayCamera$Zoom/newBuilder)]

;; Handle oneof: cmd
    (when-let [cmd-field (first (filter (fn [[k v]] (#{:set-value :move :halt :set-zoom-table-value :next-zoom-table-pos :prev-zoom-table-pos :offset :reset-zoom :save-to-table} k)) m))]
      (build-zoom-payload builder cmd-field))
    (.build builder)))

(defn parse-zoom
  "Parse a Zoom protobuf message to a map."
  [^cmd.DayCamera.JonSharedCmdDayCamera$Zoom proto]
  (cond-> {}

;; Oneof payload
    true (merge (parse-zoom-payload proto))))

(defn build-next-zoom-table-pos
  "Build a NextZoomTablePos protobuf message from a map."
  [m]
  (let [builder (cmd.DayCamera.JonSharedCmdDayCamera$NextZoomTablePos/newBuilder)]

    (.build builder)))

(defn parse-next-zoom-table-pos
  "Parse a NextZoomTablePos protobuf message to a map."
  [^cmd.DayCamera.JonSharedCmdDayCamera$NextZoomTablePos proto]
  (cond-> {}))

(defn build-prev-zoom-table-pos
  "Build a PrevZoomTablePos protobuf message from a map."
  [m]
  (let [builder (cmd.DayCamera.JonSharedCmdDayCamera$PrevZoomTablePos/newBuilder)]

    (.build builder)))

(defn parse-prev-zoom-table-pos
  "Parse a PrevZoomTablePos protobuf message to a map."
  [^cmd.DayCamera.JonSharedCmdDayCamera$PrevZoomTablePos proto]
  (cond-> {}))

(defn build-set-iris
  "Build a SetIris protobuf message from a map."
  [m]
  (let [builder (cmd.DayCamera.JonSharedCmdDayCamera$SetIris/newBuilder)]
    ;; Set regular fields
    (when (contains? m :value)
      (.setValue builder (get m :value)))

    (.build builder)))

(defn parse-set-iris
  "Parse a SetIris protobuf message to a map."
  [^cmd.DayCamera.JonSharedCmdDayCamera$SetIris proto]
  (cond-> {}
    ;; Regular fields
    (.hasValue proto) (assoc :value (.getValue proto))))

(defn build-set-infra-red-filter
  "Build a SetInfraRedFilter protobuf message from a map."
  [m]
  (let [builder (cmd.DayCamera.JonSharedCmdDayCamera$SetInfraRedFilter/newBuilder)]
    ;; Set regular fields
    (when (contains? m :value)
      (.setValue builder (get m :value)))

    (.build builder)))

(defn parse-set-infra-red-filter
  "Parse a SetInfraRedFilter protobuf message to a map."
  [^cmd.DayCamera.JonSharedCmdDayCamera$SetInfraRedFilter proto]
  (cond-> {}
    ;; Regular fields
    (.hasValue proto) (assoc :value (.getValue proto))))

(defn build-set-auto-iris
  "Build a SetAutoIris protobuf message from a map."
  [m]
  (let [builder (cmd.DayCamera.JonSharedCmdDayCamera$SetAutoIris/newBuilder)]
    ;; Set regular fields
    (when (contains? m :value)
      (.setValue builder (get m :value)))

    (.build builder)))

(defn parse-set-auto-iris
  "Parse a SetAutoIris protobuf message to a map."
  [^cmd.DayCamera.JonSharedCmdDayCamera$SetAutoIris proto]
  (cond-> {}
    ;; Regular fields
    (.hasValue proto) (assoc :value (.getValue proto))))

(defn build-set-zoom-table-value
  "Build a SetZoomTableValue protobuf message from a map."
  [m]
  (let [builder (cmd.DayCamera.JonSharedCmdDayCamera$SetZoomTableValue/newBuilder)]
    ;; Set regular fields
    (when (contains? m :value)
      (.setValue builder (get m :value)))

    (.build builder)))

(defn parse-set-zoom-table-value
  "Parse a SetZoomTableValue protobuf message to a map."
  [^cmd.DayCamera.JonSharedCmdDayCamera$SetZoomTableValue proto]
  (cond-> {}
    ;; Regular fields
    (.hasValue proto) (assoc :value (.getValue proto))))

(defn build-stop
  "Build a Stop protobuf message from a map."
  [m]
  (let [builder (cmd.DayCamera.JonSharedCmdDayCamera$Stop/newBuilder)]

    (.build builder)))

(defn parse-stop
  "Parse a Stop protobuf message to a map."
  [^cmd.DayCamera.JonSharedCmdDayCamera$Stop proto]
  (cond-> {}))

(defn build-start
  "Build a Start protobuf message from a map."
  [m]
  (let [builder (cmd.DayCamera.JonSharedCmdDayCamera$Start/newBuilder)]

    (.build builder)))

(defn parse-start
  "Parse a Start protobuf message to a map."
  [^cmd.DayCamera.JonSharedCmdDayCamera$Start proto]
  (cond-> {}))

(defn build-photo
  "Build a Photo protobuf message from a map."
  [m]
  (let [builder (cmd.DayCamera.JonSharedCmdDayCamera$Photo/newBuilder)]

    (.build builder)))

(defn parse-photo
  "Parse a Photo protobuf message to a map."
  [^cmd.DayCamera.JonSharedCmdDayCamera$Photo proto]
  (cond-> {}))

(defn build-halt
  "Build a Halt protobuf message from a map."
  [m]
  (let [builder (cmd.DayCamera.JonSharedCmdDayCamera$Halt/newBuilder)]

    (.build builder)))

(defn parse-halt
  "Parse a Halt protobuf message to a map."
  [^cmd.DayCamera.JonSharedCmdDayCamera$Halt proto]
  (cond-> {}))

(defn build-get-meteo
  "Build a GetMeteo protobuf message from a map."
  [m]
  (let [builder (cmd.DayCamera.JonSharedCmdDayCamera$GetMeteo/newBuilder)]

    (.build builder)))

(defn parse-get-meteo
  "Parse a GetMeteo protobuf message to a map."
  [^cmd.DayCamera.JonSharedCmdDayCamera$GetMeteo proto]
  (cond-> {}))

(defn build-reset-zoom
  "Build a ResetZoom protobuf message from a map."
  [m]
  (let [builder (cmd.DayCamera.JonSharedCmdDayCamera$ResetZoom/newBuilder)]

    (.build builder)))

(defn parse-reset-zoom
  "Parse a ResetZoom protobuf message to a map."
  [^cmd.DayCamera.JonSharedCmdDayCamera$ResetZoom proto]
  (cond-> {}))

(defn build-reset-focus
  "Build a ResetFocus protobuf message from a map."
  [m]
  (let [builder (cmd.DayCamera.JonSharedCmdDayCamera$ResetFocus/newBuilder)]

    (.build builder)))

(defn parse-reset-focus
  "Parse a ResetFocus protobuf message to a map."
  [^cmd.DayCamera.JonSharedCmdDayCamera$ResetFocus proto]
  (cond-> {}))

(defn build-save-to-table
  "Build a SaveToTable protobuf message from a map."
  [m]
  (let [builder (cmd.DayCamera.JonSharedCmdDayCamera$SaveToTable/newBuilder)]

    (.build builder)))

(defn parse-save-to-table
  "Parse a SaveToTable protobuf message to a map."
  [^cmd.DayCamera.JonSharedCmdDayCamera$SaveToTable proto]
  (cond-> {}))

(defn build-save-to-table-focus
  "Build a SaveToTableFocus protobuf message from a map."
  [m]
  (let [builder (cmd.DayCamera.JonSharedCmdDayCamera$SaveToTableFocus/newBuilder)]

    (.build builder)))

(defn parse-save-to-table-focus
  "Parse a SaveToTableFocus protobuf message to a map."
  [^cmd.DayCamera.JonSharedCmdDayCamera$SaveToTableFocus proto]
  (cond-> {}))

(defn build-root-payload
  "Build the oneof payload for Root."
  [builder [field-key value]]
  (case field-key
    :zoom (.setZoom builder (build-zoom value))
    :set-agc (.setSetAgc builder (build-set-agc value))
    :set-filter (.setSetFilter builder (build-set-filters value))
    :start (.setStart builder (build-start value))
    :stop (.setStop builder (build-stop value))
    :photo (.setPhoto builder (build-photo value))
    :zoom-in (.setZoomIn builder (build-zoom-in value))
    :zoom-out (.setZoomOut builder (build-zoom-out value))
    :zoom-stop (.setZoomStop builder (build-zoom-stop value))
    :focus-in (.setFocusIn builder (build-focus-in value))
    :focus-out (.setFocusOut builder (build-focus-out value))
    :focus-stop (.setFocusStop builder (build-focus-stop value))
    :calibrate (.setCalibrate builder (build-calibrate value))
    :set-dde-level (.setSetDdeLevel builder (build-set-dde-level value))
    :enable-dde (.setEnableDde builder (build-enable-dde value))
    :disable-dde (.setDisableDde builder (build-disable-dde value))
    :set-auto-focus (.setSetAutoFocus builder (build-set-auto-focus value))
    :focus-step-plus (.setFocusStepPlus builder (build-focus-step-plus value))
    :focus-step-minus (.setFocusStepMinus builder (build-focus-step-minus value))
    :set-fx-mode (.setSetFxMode builder (build-set-fx-mode value))
    :next-fx-mode (.setNextFxMode builder (build-next-fx-mode value))
    :prev-fx-mode (.setPrevFxMode builder (build-prev-fx-mode value))
    :get-meteo (.setGetMeteo builder (build-get-meteo value))
    :shift-dde (.setShiftDde builder (build-shift-dde value))
    :refresh-fx-mode (.setRefreshFxMode builder (build-refresh-fx-mode value))
    :reset-zoom (.setResetZoom builder (build-reset-zoom value))
    :save-to-table (.setSaveToTable builder (build-save-to-table value))
    :set-calib-mode (.setSetCalibMode builder (build-set-calib-mode value))
    :set-digital-zoom-level (.setSetDigitalZoomLevel builder (build-set-digital-zoom-level value))
    :set-clahe-level (.setSetClaheLevel builder (build-set-clahe-level value))
    :shift-clahe-level (.setShiftClaheLevel builder (build-shift-clahe-level value))
    (throw (ex-info "Unknown oneof field" {:field field-key :oneof ":cmd"}))))

(defn parse-root-payload
  "Parse the oneof payload from Root."
  [^cmd.HeatCamera.JonSharedCmdHeatCamera$Root proto]
  (cond
    (.hasZoom proto) {:zoom (parse-zoom (.getZoom proto))}
    (.hasSetAgc proto) {:set-agc (parse-set-agc (.getSetAgc proto))}
    (.hasSetFilter proto) {:set-filter (parse-set-filters (.getSetFilter proto))}
    (.hasStart proto) {:start (parse-start (.getStart proto))}
    (.hasStop proto) {:stop (parse-stop (.getStop proto))}
    (.hasPhoto proto) {:photo (parse-photo (.getPhoto proto))}
    (.hasZoomIn proto) {:zoom-in (parse-zoom-in (.getZoomIn proto))}
    (.hasZoomOut proto) {:zoom-out (parse-zoom-out (.getZoomOut proto))}
    (.hasZoomStop proto) {:zoom-stop (parse-zoom-stop (.getZoomStop proto))}
    (.hasFocusIn proto) {:focus-in (parse-focus-in (.getFocusIn proto))}
    (.hasFocusOut proto) {:focus-out (parse-focus-out (.getFocusOut proto))}
    (.hasFocusStop proto) {:focus-stop (parse-focus-stop (.getFocusStop proto))}
    (.hasCalibrate proto) {:calibrate (parse-calibrate (.getCalibrate proto))}
    (.hasSetDdeLevel proto) {:set-dde-level (parse-set-dde-level (.getSetDdeLevel proto))}
    (.hasEnableDde proto) {:enable-dde (parse-enable-dde (.getEnableDde proto))}
    (.hasDisableDde proto) {:disable-dde (parse-disable-dde (.getDisableDde proto))}
    (.hasSetAutoFocus proto) {:set-auto-focus (parse-set-auto-focus (.getSetAutoFocus proto))}
    (.hasFocusStepPlus proto) {:focus-step-plus (parse-focus-step-plus (.getFocusStepPlus proto))}
    (.hasFocusStepMinus proto) {:focus-step-minus (parse-focus-step-minus (.getFocusStepMinus proto))}
    (.hasSetFxMode proto) {:set-fx-mode (parse-set-fx-mode (.getSetFxMode proto))}
    (.hasNextFxMode proto) {:next-fx-mode (parse-next-fx-mode (.getNextFxMode proto))}
    (.hasPrevFxMode proto) {:prev-fx-mode (parse-prev-fx-mode (.getPrevFxMode proto))}
    (.hasGetMeteo proto) {:get-meteo (parse-get-meteo (.getGetMeteo proto))}
    (.hasShiftDde proto) {:shift-dde (parse-shift-dde (.getShiftDde proto))}
    (.hasRefreshFxMode proto) {:refresh-fx-mode (parse-refresh-fx-mode (.getRefreshFxMode proto))}
    (.hasResetZoom proto) {:reset-zoom (parse-reset-zoom (.getResetZoom proto))}
    (.hasSaveToTable proto) {:save-to-table (parse-save-to-table (.getSaveToTable proto))}
    (.hasSetCalibMode proto) {:set-calib-mode (parse-set-calib-mode (.getSetCalibMode proto))}
    (.hasSetDigitalZoomLevel proto) {:set-digital-zoom-level (parse-set-digital-zoom-level (.getSetDigitalZoomLevel proto))}
    (.hasSetClaheLevel proto) {:set-clahe-level (parse-set-clahe-level (.getSetClaheLevel proto))}
    (.hasShiftClaheLevel proto) {:shift-clahe-level (parse-shift-clahe-level (.getShiftClaheLevel proto))}))

(defn build-root
  "Build a Root protobuf message from a map."
  [m]
  (let [builder (cmd.HeatCamera.JonSharedCmdHeatCamera$Root/newBuilder)]

;; Handle oneof: cmd
    (when-let [cmd-field (first (filter (fn [[k v]] (#{:zoom :set-agc :set-filter :start :stop :photo :zoom-in :zoom-out :zoom-stop :focus-in :focus-out :focus-stop :calibrate :set-dde-level :enable-dde :disable-dde :set-auto-focus :focus-step-plus :focus-step-minus :set-fx-mode :next-fx-mode :prev-fx-mode :get-meteo :shift-dde :refresh-fx-mode :reset-zoom :save-to-table :set-calib-mode :set-digital-zoom-level :set-clahe-level :shift-clahe-level} k)) m))]
      (build-root-payload builder cmd-field))
    (.build builder)))

(defn parse-root
  "Parse a Root protobuf message to a map."
  [^cmd.HeatCamera.JonSharedCmdHeatCamera$Root proto]
  (cond-> {}

;; Oneof payload
    true (merge (parse-root-payload proto))))

(defn build-set-fx-mode
  "Build a SetFxMode protobuf message from a map."
  [m]
  (let [builder (cmd.HeatCamera.JonSharedCmdHeatCamera$SetFxMode/newBuilder)]
    ;; Set regular fields
    (when (contains? m :mode)
      (.setMode builder (get m :mode)))

    (.build builder)))

(defn parse-set-fx-mode
  "Parse a SetFxMode protobuf message to a map."
  [^cmd.HeatCamera.JonSharedCmdHeatCamera$SetFxMode proto]
  (cond-> {}
    ;; Regular fields
    (.hasMode proto) (assoc :mode (.getMode proto))))

(defn build-set-clahe-level
  "Build a SetClaheLevel protobuf message from a map."
  [m]
  (let [builder (cmd.HeatCamera.JonSharedCmdHeatCamera$SetClaheLevel/newBuilder)]
    ;; Set regular fields
    (when (contains? m :value)
      (.setValue builder (get m :value)))

    (.build builder)))

(defn parse-set-clahe-level
  "Parse a SetClaheLevel protobuf message to a map."
  [^cmd.HeatCamera.JonSharedCmdHeatCamera$SetClaheLevel proto]
  (cond-> {}
    ;; Regular fields
    (.hasValue proto) (assoc :value (.getValue proto))))

(defn build-shift-clahe-level
  "Build a ShiftClaheLevel protobuf message from a map."
  [m]
  (let [builder (cmd.HeatCamera.JonSharedCmdHeatCamera$ShiftClaheLevel/newBuilder)]
    ;; Set regular fields
    (when (contains? m :value)
      (.setValue builder (get m :value)))

    (.build builder)))

(defn parse-shift-clahe-level
  "Parse a ShiftClaheLevel protobuf message to a map."
  [^cmd.HeatCamera.JonSharedCmdHeatCamera$ShiftClaheLevel proto]
  (cond-> {}
    ;; Regular fields
    (.hasValue proto) (assoc :value (.getValue proto))))

(defn build-next-fx-mode
  "Build a NextFxMode protobuf message from a map."
  [m]
  (let [builder (cmd.HeatCamera.JonSharedCmdHeatCamera$NextFxMode/newBuilder)]

    (.build builder)))

(defn parse-next-fx-mode
  "Parse a NextFxMode protobuf message to a map."
  [^cmd.HeatCamera.JonSharedCmdHeatCamera$NextFxMode proto]
  (cond-> {}))

(defn build-prev-fx-mode
  "Build a PrevFxMode protobuf message from a map."
  [m]
  (let [builder (cmd.HeatCamera.JonSharedCmdHeatCamera$PrevFxMode/newBuilder)]

    (.build builder)))

(defn parse-prev-fx-mode
  "Parse a PrevFxMode protobuf message to a map."
  [^cmd.HeatCamera.JonSharedCmdHeatCamera$PrevFxMode proto]
  (cond-> {}))

(defn build-refresh-fx-mode
  "Build a RefreshFxMode protobuf message from a map."
  [m]
  (let [builder (cmd.HeatCamera.JonSharedCmdHeatCamera$RefreshFxMode/newBuilder)]

    (.build builder)))

(defn parse-refresh-fx-mode
  "Parse a RefreshFxMode protobuf message to a map."
  [^cmd.HeatCamera.JonSharedCmdHeatCamera$RefreshFxMode proto]
  (cond-> {}))

(defn build-enable-dde
  "Build a EnableDDE protobuf message from a map."
  [m]
  (let [builder (cmd.HeatCamera.JonSharedCmdHeatCamera$EnableDDE/newBuilder)]

    (.build builder)))

(defn parse-enable-dde
  "Parse a EnableDDE protobuf message to a map."
  [^cmd.HeatCamera.JonSharedCmdHeatCamera$EnableDDE proto]
  (cond-> {}))

(defn build-disable-dde
  "Build a DisableDDE protobuf message from a map."
  [m]
  (let [builder (cmd.HeatCamera.JonSharedCmdHeatCamera$DisableDDE/newBuilder)]

    (.build builder)))

(defn parse-disable-dde
  "Parse a DisableDDE protobuf message to a map."
  [^cmd.HeatCamera.JonSharedCmdHeatCamera$DisableDDE proto]
  (cond-> {}))

(defn build-set-value
  "Build a SetValue protobuf message from a map."
  [m]
  (let [builder (cmd.HeatCamera.JonSharedCmdHeatCamera$SetValue/newBuilder)]
    ;; Set regular fields
    (when (contains? m :value)
      (.setValue builder (get m :value)))

    (.build builder)))

(defn parse-set-value
  "Parse a SetValue protobuf message to a map."
  [^cmd.HeatCamera.JonSharedCmdHeatCamera$SetValue proto]
  (cond-> {}
    ;; Regular fields
    (.hasValue proto) (assoc :value (.getValue proto))))

(defn build-set-dde-level
  "Build a SetDDELevel protobuf message from a map."
  [m]
  (let [builder (cmd.HeatCamera.JonSharedCmdHeatCamera$SetDDELevel/newBuilder)]
    ;; Set regular fields
    (when (contains? m :value)
      (.setValue builder (get m :value)))

    (.build builder)))

(defn parse-set-dde-level
  "Parse a SetDDELevel protobuf message to a map."
  [^cmd.HeatCamera.JonSharedCmdHeatCamera$SetDDELevel proto]
  (cond-> {}
    ;; Regular fields
    (.hasValue proto) (assoc :value (.getValue proto))))

(defn build-set-digital-zoom-level
  "Build a SetDigitalZoomLevel protobuf message from a map."
  [m]
  (let [builder (cmd.HeatCamera.JonSharedCmdHeatCamera$SetDigitalZoomLevel/newBuilder)]
    ;; Set regular fields
    (when (contains? m :value)
      (.setValue builder (get m :value)))

    (.build builder)))

(defn parse-set-digital-zoom-level
  "Parse a SetDigitalZoomLevel protobuf message to a map."
  [^cmd.HeatCamera.JonSharedCmdHeatCamera$SetDigitalZoomLevel proto]
  (cond-> {}
    ;; Regular fields
    (.hasValue proto) (assoc :value (.getValue proto))))

(defn build-shift-dde
  "Build a ShiftDDE protobuf message from a map."
  [m]
  (let [builder (cmd.HeatCamera.JonSharedCmdHeatCamera$ShiftDDE/newBuilder)]
    ;; Set regular fields
    (when (contains? m :value)
      (.setValue builder (get m :value)))

    (.build builder)))

(defn parse-shift-dde
  "Parse a ShiftDDE protobuf message to a map."
  [^cmd.HeatCamera.JonSharedCmdHeatCamera$ShiftDDE proto]
  (cond-> {}
    ;; Regular fields
    (.hasValue proto) (assoc :value (.getValue proto))))

(defn build-zoom-in
  "Build a ZoomIn protobuf message from a map."
  [m]
  (let [builder (cmd.HeatCamera.JonSharedCmdHeatCamera$ZoomIn/newBuilder)]

    (.build builder)))

(defn parse-zoom-in
  "Parse a ZoomIn protobuf message to a map."
  [^cmd.HeatCamera.JonSharedCmdHeatCamera$ZoomIn proto]
  (cond-> {}))

(defn build-zoom-out
  "Build a ZoomOut protobuf message from a map."
  [m]
  (let [builder (cmd.HeatCamera.JonSharedCmdHeatCamera$ZoomOut/newBuilder)]

    (.build builder)))

(defn parse-zoom-out
  "Parse a ZoomOut protobuf message to a map."
  [^cmd.HeatCamera.JonSharedCmdHeatCamera$ZoomOut proto]
  (cond-> {}))

(defn build-zoom-stop
  "Build a ZoomStop protobuf message from a map."
  [m]
  (let [builder (cmd.HeatCamera.JonSharedCmdHeatCamera$ZoomStop/newBuilder)]

    (.build builder)))

(defn parse-zoom-stop
  "Parse a ZoomStop protobuf message to a map."
  [^cmd.HeatCamera.JonSharedCmdHeatCamera$ZoomStop proto]
  (cond-> {}))

(defn build-focus-in
  "Build a FocusIn protobuf message from a map."
  [m]
  (let [builder (cmd.HeatCamera.JonSharedCmdHeatCamera$FocusIn/newBuilder)]

    (.build builder)))

(defn parse-focus-in
  "Parse a FocusIn protobuf message to a map."
  [^cmd.HeatCamera.JonSharedCmdHeatCamera$FocusIn proto]
  (cond-> {}))

(defn build-focus-out
  "Build a FocusOut protobuf message from a map."
  [m]
  (let [builder (cmd.HeatCamera.JonSharedCmdHeatCamera$FocusOut/newBuilder)]

    (.build builder)))

(defn parse-focus-out
  "Parse a FocusOut protobuf message to a map."
  [^cmd.HeatCamera.JonSharedCmdHeatCamera$FocusOut proto]
  (cond-> {}))

(defn build-focus-stop
  "Build a FocusStop protobuf message from a map."
  [m]
  (let [builder (cmd.HeatCamera.JonSharedCmdHeatCamera$FocusStop/newBuilder)]

    (.build builder)))

(defn parse-focus-stop
  "Parse a FocusStop protobuf message to a map."
  [^cmd.HeatCamera.JonSharedCmdHeatCamera$FocusStop proto]
  (cond-> {}))

(defn build-focus-step-plus
  "Build a FocusStepPlus protobuf message from a map."
  [m]
  (let [builder (cmd.HeatCamera.JonSharedCmdHeatCamera$FocusStepPlus/newBuilder)]

    (.build builder)))

(defn parse-focus-step-plus
  "Parse a FocusStepPlus protobuf message to a map."
  [^cmd.HeatCamera.JonSharedCmdHeatCamera$FocusStepPlus proto]
  (cond-> {}))

(defn build-focus-step-minus
  "Build a FocusStepMinus protobuf message from a map."
  [m]
  (let [builder (cmd.HeatCamera.JonSharedCmdHeatCamera$FocusStepMinus/newBuilder)]

    (.build builder)))

(defn parse-focus-step-minus
  "Parse a FocusStepMinus protobuf message to a map."
  [^cmd.HeatCamera.JonSharedCmdHeatCamera$FocusStepMinus proto]
  (cond-> {}))

(defn build-calibrate
  "Build a Calibrate protobuf message from a map."
  [m]
  (let [builder (cmd.HeatCamera.JonSharedCmdHeatCamera$Calibrate/newBuilder)]

    (.build builder)))

(defn parse-calibrate
  "Parse a Calibrate protobuf message to a map."
  [^cmd.HeatCamera.JonSharedCmdHeatCamera$Calibrate proto]
  (cond-> {}))

(defn build-zoom-payload
  "Build the oneof payload for Zoom."
  [builder [field-key value]]
  (case field-key
    :set-zoom-table-value (.setSetZoomTableValue builder (build-set-zoom-table-value value))
    :next-zoom-table-pos (.setNextZoomTablePos builder (build-next-zoom-table-pos value))
    :prev-zoom-table-pos (.setPrevZoomTablePos builder (build-prev-zoom-table-pos value))
    (throw (ex-info "Unknown oneof field" {:field field-key :oneof ":cmd"}))))

(defn parse-zoom-payload
  "Parse the oneof payload from Zoom."
  [^cmd.HeatCamera.JonSharedCmdHeatCamera$Zoom proto]
  (cond
    (.hasSetZoomTableValue proto) {:set-zoom-table-value (parse-set-zoom-table-value (.getSetZoomTableValue proto))}
    (.hasNextZoomTablePos proto) {:next-zoom-table-pos (parse-next-zoom-table-pos (.getNextZoomTablePos proto))}
    (.hasPrevZoomTablePos proto) {:prev-zoom-table-pos (parse-prev-zoom-table-pos (.getPrevZoomTablePos proto))}))

(defn build-zoom
  "Build a Zoom protobuf message from a map."
  [m]
  (let [builder (cmd.HeatCamera.JonSharedCmdHeatCamera$Zoom/newBuilder)]

;; Handle oneof: cmd
    (when-let [cmd-field (first (filter (fn [[k v]] (#{:set-zoom-table-value :next-zoom-table-pos :prev-zoom-table-pos} k)) m))]
      (build-zoom-payload builder cmd-field))
    (.build builder)))

(defn parse-zoom
  "Parse a Zoom protobuf message to a map."
  [^cmd.HeatCamera.JonSharedCmdHeatCamera$Zoom proto]
  (cond-> {}

;; Oneof payload
    true (merge (parse-zoom-payload proto))))

(defn build-next-zoom-table-pos
  "Build a NextZoomTablePos protobuf message from a map."
  [m]
  (let [builder (cmd.HeatCamera.JonSharedCmdHeatCamera$NextZoomTablePos/newBuilder)]

    (.build builder)))

(defn parse-next-zoom-table-pos
  "Parse a NextZoomTablePos protobuf message to a map."
  [^cmd.HeatCamera.JonSharedCmdHeatCamera$NextZoomTablePos proto]
  (cond-> {}))

(defn build-prev-zoom-table-pos
  "Build a PrevZoomTablePos protobuf message from a map."
  [m]
  (let [builder (cmd.HeatCamera.JonSharedCmdHeatCamera$PrevZoomTablePos/newBuilder)]

    (.build builder)))

(defn parse-prev-zoom-table-pos
  "Parse a PrevZoomTablePos protobuf message to a map."
  [^cmd.HeatCamera.JonSharedCmdHeatCamera$PrevZoomTablePos proto]
  (cond-> {}))

(defn build-set-calib-mode
  "Build a SetCalibMode protobuf message from a map."
  [m]
  (let [builder (cmd.HeatCamera.JonSharedCmdHeatCamera$SetCalibMode/newBuilder)]

    (.build builder)))

(defn parse-set-calib-mode
  "Parse a SetCalibMode protobuf message to a map."
  [^cmd.HeatCamera.JonSharedCmdHeatCamera$SetCalibMode proto]
  (cond-> {}))

(defn build-set-zoom-table-value
  "Build a SetZoomTableValue protobuf message from a map."
  [m]
  (let [builder (cmd.HeatCamera.JonSharedCmdHeatCamera$SetZoomTableValue/newBuilder)]
    ;; Set regular fields
    (when (contains? m :value)
      (.setValue builder (get m :value)))

    (.build builder)))

(defn parse-set-zoom-table-value
  "Parse a SetZoomTableValue protobuf message to a map."
  [^cmd.HeatCamera.JonSharedCmdHeatCamera$SetZoomTableValue proto]
  (cond-> {}
    ;; Regular fields
    (.hasValue proto) (assoc :value (.getValue proto))))

(defn build-set-agc
  "Build a SetAGC protobuf message from a map."
  [m]
  (let [builder (cmd.HeatCamera.JonSharedCmdHeatCamera$SetAGC/newBuilder)]
    ;; Set regular fields
    (when (contains? m :value)
      (.setValue builder (get m :value)))

    (.build builder)))

(defn parse-set-agc
  "Parse a SetAGC protobuf message to a map."
  [^cmd.HeatCamera.JonSharedCmdHeatCamera$SetAGC proto]
  (cond-> {}
    ;; Regular fields
    (.hasValue proto) (assoc :value (.getValue proto))))

(defn build-set-filters
  "Build a SetFilters protobuf message from a map."
  [m]
  (let [builder (cmd.HeatCamera.JonSharedCmdHeatCamera$SetFilters/newBuilder)]
    ;; Set regular fields
    (when (contains? m :value)
      (.setValue builder (get m :value)))

    (.build builder)))

(defn parse-set-filters
  "Parse a SetFilters protobuf message to a map."
  [^cmd.HeatCamera.JonSharedCmdHeatCamera$SetFilters proto]
  (cond-> {}
    ;; Regular fields
    (.hasValue proto) (assoc :value (.getValue proto))))

(defn build-start
  "Build a Start protobuf message from a map."
  [m]
  (let [builder (cmd.HeatCamera.JonSharedCmdHeatCamera$Start/newBuilder)]

    (.build builder)))

(defn parse-start
  "Parse a Start protobuf message to a map."
  [^cmd.HeatCamera.JonSharedCmdHeatCamera$Start proto]
  (cond-> {}))

(defn build-stop
  "Build a Stop protobuf message from a map."
  [m]
  (let [builder (cmd.HeatCamera.JonSharedCmdHeatCamera$Stop/newBuilder)]

    (.build builder)))

(defn parse-stop
  "Parse a Stop protobuf message to a map."
  [^cmd.HeatCamera.JonSharedCmdHeatCamera$Stop proto]
  (cond-> {}))

(defn build-halt
  "Build a Halt protobuf message from a map."
  [m]
  (let [builder (cmd.HeatCamera.JonSharedCmdHeatCamera$Halt/newBuilder)]

    (.build builder)))

(defn parse-halt
  "Parse a Halt protobuf message to a map."
  [^cmd.HeatCamera.JonSharedCmdHeatCamera$Halt proto]
  (cond-> {}))

(defn build-photo
  "Build a Photo protobuf message from a map."
  [m]
  (let [builder (cmd.HeatCamera.JonSharedCmdHeatCamera$Photo/newBuilder)]

    (.build builder)))

(defn parse-photo
  "Parse a Photo protobuf message to a map."
  [^cmd.HeatCamera.JonSharedCmdHeatCamera$Photo proto]
  (cond-> {}))

(defn build-get-meteo
  "Build a GetMeteo protobuf message from a map."
  [m]
  (let [builder (cmd.HeatCamera.JonSharedCmdHeatCamera$GetMeteo/newBuilder)]

    (.build builder)))

(defn parse-get-meteo
  "Parse a GetMeteo protobuf message to a map."
  [^cmd.HeatCamera.JonSharedCmdHeatCamera$GetMeteo proto]
  (cond-> {}))

(defn build-set-auto-focus
  "Build a SetAutoFocus protobuf message from a map."
  [m]
  (let [builder (cmd.HeatCamera.JonSharedCmdHeatCamera$SetAutoFocus/newBuilder)]
    ;; Set regular fields
    (when (contains? m :value)
      (.setValue builder (get m :value)))

    (.build builder)))

(defn parse-set-auto-focus
  "Parse a SetAutoFocus protobuf message to a map."
  [^cmd.HeatCamera.JonSharedCmdHeatCamera$SetAutoFocus proto]
  (cond-> {}
    ;; Regular fields
    (.hasValue proto) (assoc :value (.getValue proto))))

(defn build-reset-zoom
  "Build a ResetZoom protobuf message from a map."
  [m]
  (let [builder (cmd.HeatCamera.JonSharedCmdHeatCamera$ResetZoom/newBuilder)]

    (.build builder)))

(defn parse-reset-zoom
  "Parse a ResetZoom protobuf message to a map."
  [^cmd.HeatCamera.JonSharedCmdHeatCamera$ResetZoom proto]
  (cond-> {}))

(defn build-save-to-table
  "Build a SaveToTable protobuf message from a map."
  [m]
  (let [builder (cmd.HeatCamera.JonSharedCmdHeatCamera$SaveToTable/newBuilder)]

    (.build builder)))

(defn parse-save-to-table
  "Parse a SaveToTable protobuf message to a map."
  [^cmd.HeatCamera.JonSharedCmdHeatCamera$SaveToTable proto]
  (cond-> {}))

(defn build-root-payload
  "Build the oneof payload for Root."
  [builder [field-key value]]
  (case field-key
    :start (.setStart builder (build-start value))
    :stop (.setStop builder (build-stop value))
    :axis (.setAxis builder (build-axis value))
    :set-platform-azimuth (.setSetPlatformAzimuth builder (build-set-platform-azimuth value))
    :set-platform-elevation (.setSetPlatformElevation builder (build-set-platform-elevation value))
    :set-platform-bank (.setSetPlatformBank builder (build-set-platform-bank value))
    :halt (.setHalt builder (build-halt value))
    :set-use-rotary-as-compass (.setSetUseRotaryAsCompass builder (build-set-use-rotary-as-compass value))
    :rotate-to-gps (.setRotateToGps builder (build-rotate-to-gps value))
    :set-origin-gps (.setSetOriginGps builder (build-set-origin-gps value))
    :set-mode (.setSetMode builder (build-set-mode value))
    :rotate-to-ndc (.setRotateToNdc builder (build-rotate-to-ndc value))
    :scan-start (.setScanStart builder (build-scan-start value))
    :scan-stop (.setScanStop builder (build-scan-stop value))
    :scan-pause (.setScanPause builder (build-scan-pause value))
    :scan-unpause (.setScanUnpause builder (build-scan-unpause value))
    :get-meteo (.setGetMeteo builder (build-get-meteo value))
    :scan-prev (.setScanPrev builder (build-scan-prev value))
    :scan-next (.setScanNext builder (build-scan-next value))
    :scan-refresh-node-list (.setScanRefreshNodeList builder (build-scan-refresh-node-list value))
    :scan-select-node (.setScanSelectNode builder (build-scan-select-node value))
    :scan-delete-node (.setScanDeleteNode builder (build-scan-delete-node value))
    :scan-update-node (.setScanUpdateNode builder (build-scan-update-node value))
    :scan-add-node (.setScanAddNode builder (build-scan-add-node value))
    (throw (ex-info "Unknown oneof field" {:field field-key :oneof ":cmd"}))))

(defn parse-root-payload
  "Parse the oneof payload from Root."
  [^cmd.RotaryPlatform.JonSharedCmdRotary$Root proto]
  (cond
    (.hasStart proto) {:start (parse-start (.getStart proto))}
    (.hasStop proto) {:stop (parse-stop (.getStop proto))}
    (.hasAxis proto) {:axis (parse-axis (.getAxis proto))}
    (.hasSetPlatformAzimuth proto) {:set-platform-azimuth (parse-set-platform-azimuth (.getSetPlatformAzimuth proto))}
    (.hasSetPlatformElevation proto) {:set-platform-elevation (parse-set-platform-elevation (.getSetPlatformElevation proto))}
    (.hasSetPlatformBank proto) {:set-platform-bank (parse-set-platform-bank (.getSetPlatformBank proto))}
    (.hasHalt proto) {:halt (parse-halt (.getHalt proto))}
    (.hasSetUseRotaryAsCompass proto) {:set-use-rotary-as-compass (parse-set-use-rotary-as-compass (.getSetUseRotaryAsCompass proto))}
    (.hasRotateToGps proto) {:rotate-to-gps (parse-rotate-to-gps (.getRotateToGps proto))}
    (.hasSetOriginGps proto) {:set-origin-gps (parse-set-origin-gps (.getSetOriginGps proto))}
    (.hasSetMode proto) {:set-mode (parse-set-mode (.getSetMode proto))}
    (.hasRotateToNdc proto) {:rotate-to-ndc (parse-rotate-to-ndc (.getRotateToNdc proto))}
    (.hasScanStart proto) {:scan-start (parse-scan-start (.getScanStart proto))}
    (.hasScanStop proto) {:scan-stop (parse-scan-stop (.getScanStop proto))}
    (.hasScanPause proto) {:scan-pause (parse-scan-pause (.getScanPause proto))}
    (.hasScanUnpause proto) {:scan-unpause (parse-scan-unpause (.getScanUnpause proto))}
    (.hasGetMeteo proto) {:get-meteo (parse-get-meteo (.getGetMeteo proto))}
    (.hasScanPrev proto) {:scan-prev (parse-scan-prev (.getScanPrev proto))}
    (.hasScanNext proto) {:scan-next (parse-scan-next (.getScanNext proto))}
    (.hasScanRefreshNodeList proto) {:scan-refresh-node-list (parse-scan-refresh-node-list (.getScanRefreshNodeList proto))}
    (.hasScanSelectNode proto) {:scan-select-node (parse-scan-select-node (.getScanSelectNode proto))}
    (.hasScanDeleteNode proto) {:scan-delete-node (parse-scan-delete-node (.getScanDeleteNode proto))}
    (.hasScanUpdateNode proto) {:scan-update-node (parse-scan-update-node (.getScanUpdateNode proto))}
    (.hasScanAddNode proto) {:scan-add-node (parse-scan-add-node (.getScanAddNode proto))}))

(defn build-root
  "Build a Root protobuf message from a map."
  [m]
  (let [builder (cmd.RotaryPlatform.JonSharedCmdRotary$Root/newBuilder)]

;; Handle oneof: cmd
    (when-let [cmd-field (first (filter (fn [[k v]] (#{:start :stop :axis :set-platform-azimuth :set-platform-elevation :set-platform-bank :halt :set-use-rotary-as-compass :rotate-to-gps :set-origin-gps :set-mode :rotate-to-ndc :scan-start :scan-stop :scan-pause :scan-unpause :get-meteo :scan-prev :scan-next :scan-refresh-node-list :scan-select-node :scan-delete-node :scan-update-node :scan-add-node} k)) m))]
      (build-root-payload builder cmd-field))
    (.build builder)))

(defn parse-root
  "Parse a Root protobuf message to a map."
  [^cmd.RotaryPlatform.JonSharedCmdRotary$Root proto]
  (cond-> {}

;; Oneof payload
    true (merge (parse-root-payload proto))))

(defn build-axis
  "Build a Axis protobuf message from a map."
  [m]
  (let [builder (cmd.RotaryPlatform.JonSharedCmdRotary$Axis/newBuilder)]
    ;; Set regular fields
    (when (contains? m :azimuth)
      (.setAzimuth builder (get m :azimuth)))
    (when (contains? m :elevation)
      (.setElevation builder (get m :elevation)))

    (.build builder)))

(defn parse-axis
  "Parse a Axis protobuf message to a map."
  [^cmd.RotaryPlatform.JonSharedCmdRotary$Axis proto]
  (cond-> {}
    ;; Regular fields
    (.hasAzimuth proto) (assoc :azimuth (.getAzimuth proto))
    (.hasElevation proto) (assoc :elevation (.getElevation proto))))

(defn build-set-mode
  "Build a SetMode protobuf message from a map."
  [m]
  (let [builder (cmd.RotaryPlatform.JonSharedCmdRotary$SetMode/newBuilder)]
    ;; Set regular fields
    (when (contains? m :mode)
      (.setMode builder (get m :mode)))

    (.build builder)))

(defn parse-set-mode
  "Parse a SetMode protobuf message to a map."
  [^cmd.RotaryPlatform.JonSharedCmdRotary$SetMode proto]
  (cond-> {}
    ;; Regular fields
    (.hasMode proto) (assoc :mode (.getMode proto))))

(defn build-set-azimuth-value
  "Build a SetAzimuthValue protobuf message from a map."
  [m]
  (let [builder (cmd.RotaryPlatform.JonSharedCmdRotary$SetAzimuthValue/newBuilder)]
    ;; Set regular fields
    (when (contains? m :value)
      (.setValue builder (get m :value)))
    (when (contains? m :direction)
      (.setDirection builder (get m :direction)))

    (.build builder)))

(defn parse-set-azimuth-value
  "Parse a SetAzimuthValue protobuf message to a map."
  [^cmd.RotaryPlatform.JonSharedCmdRotary$SetAzimuthValue proto]
  (cond-> {}
    ;; Regular fields
    (.hasValue proto) (assoc :value (.getValue proto))
    (.hasDirection proto) (assoc :direction (.getDirection proto))))

(defn build-rotate-azimuth-to
  "Build a RotateAzimuthTo protobuf message from a map."
  [m]
  (let [builder (cmd.RotaryPlatform.JonSharedCmdRotary$RotateAzimuthTo/newBuilder)]
    ;; Set regular fields
    (when (contains? m :target-value)
      (.setTargetValue builder (get m :target-value)))
    (when (contains? m :speed)
      (.setSpeed builder (get m :speed)))
    (when (contains? m :direction)
      (.setDirection builder (get m :direction)))

    (.build builder)))

(defn parse-rotate-azimuth-to
  "Parse a RotateAzimuthTo protobuf message to a map."
  [^cmd.RotaryPlatform.JonSharedCmdRotary$RotateAzimuthTo proto]
  (cond-> {}
    ;; Regular fields
    (.hasTargetValue proto) (assoc :target-value (.getTargetValue proto))
    (.hasSpeed proto) (assoc :speed (.getSpeed proto))
    (.hasDirection proto) (assoc :direction (.getDirection proto))))

(defn build-rotate-azimuth
  "Build a RotateAzimuth protobuf message from a map."
  [m]
  (let [builder (cmd.RotaryPlatform.JonSharedCmdRotary$RotateAzimuth/newBuilder)]
    ;; Set regular fields
    (when (contains? m :speed)
      (.setSpeed builder (get m :speed)))
    (when (contains? m :direction)
      (.setDirection builder (get m :direction)))

    (.build builder)))

(defn parse-rotate-azimuth
  "Parse a RotateAzimuth protobuf message to a map."
  [^cmd.RotaryPlatform.JonSharedCmdRotary$RotateAzimuth proto]
  (cond-> {}
    ;; Regular fields
    (.hasSpeed proto) (assoc :speed (.getSpeed proto))
    (.hasDirection proto) (assoc :direction (.getDirection proto))))

(defn build-rotate-elevation
  "Build a RotateElevation protobuf message from a map."
  [m]
  (let [builder (cmd.RotaryPlatform.JonSharedCmdRotary$RotateElevation/newBuilder)]
    ;; Set regular fields
    (when (contains? m :speed)
      (.setSpeed builder (get m :speed)))
    (when (contains? m :direction)
      (.setDirection builder (get m :direction)))

    (.build builder)))

(defn parse-rotate-elevation
  "Parse a RotateElevation protobuf message to a map."
  [^cmd.RotaryPlatform.JonSharedCmdRotary$RotateElevation proto]
  (cond-> {}
    ;; Regular fields
    (.hasSpeed proto) (assoc :speed (.getSpeed proto))
    (.hasDirection proto) (assoc :direction (.getDirection proto))))

(defn build-set-elevation-value
  "Build a SetElevationValue protobuf message from a map."
  [m]
  (let [builder (cmd.RotaryPlatform.JonSharedCmdRotary$SetElevationValue/newBuilder)]
    ;; Set regular fields
    (when (contains? m :value)
      (.setValue builder (get m :value)))

    (.build builder)))

(defn parse-set-elevation-value
  "Parse a SetElevationValue protobuf message to a map."
  [^cmd.RotaryPlatform.JonSharedCmdRotary$SetElevationValue proto]
  (cond-> {}
    ;; Regular fields
    (.hasValue proto) (assoc :value (.getValue proto))))

(defn build-rotate-elevation-to
  "Build a RotateElevationTo protobuf message from a map."
  [m]
  (let [builder (cmd.RotaryPlatform.JonSharedCmdRotary$RotateElevationTo/newBuilder)]
    ;; Set regular fields
    (when (contains? m :target-value)
      (.setTargetValue builder (get m :target-value)))
    (when (contains? m :speed)
      (.setSpeed builder (get m :speed)))

    (.build builder)))

(defn parse-rotate-elevation-to
  "Parse a RotateElevationTo protobuf message to a map."
  [^cmd.RotaryPlatform.JonSharedCmdRotary$RotateElevationTo proto]
  (cond-> {}
    ;; Regular fields
    (.hasTargetValue proto) (assoc :target-value (.getTargetValue proto))
    (.hasSpeed proto) (assoc :speed (.getSpeed proto))))

(defn build-rotate-elevation-relative
  "Build a RotateElevationRelative protobuf message from a map."
  [m]
  (let [builder (cmd.RotaryPlatform.JonSharedCmdRotary$RotateElevationRelative/newBuilder)]
    ;; Set regular fields
    (when (contains? m :value)
      (.setValue builder (get m :value)))
    (when (contains? m :speed)
      (.setSpeed builder (get m :speed)))
    (when (contains? m :direction)
      (.setDirection builder (get m :direction)))

    (.build builder)))

(defn parse-rotate-elevation-relative
  "Parse a RotateElevationRelative protobuf message to a map."
  [^cmd.RotaryPlatform.JonSharedCmdRotary$RotateElevationRelative proto]
  (cond-> {}
    ;; Regular fields
    (.hasValue proto) (assoc :value (.getValue proto))
    (.hasSpeed proto) (assoc :speed (.getSpeed proto))
    (.hasDirection proto) (assoc :direction (.getDirection proto))))

(defn build-rotate-elevation-relative-set
  "Build a RotateElevationRelativeSet protobuf message from a map."
  [m]
  (let [builder (cmd.RotaryPlatform.JonSharedCmdRotary$RotateElevationRelativeSet/newBuilder)]
    ;; Set regular fields
    (when (contains? m :value)
      (.setValue builder (get m :value)))
    (when (contains? m :direction)
      (.setDirection builder (get m :direction)))

    (.build builder)))

(defn parse-rotate-elevation-relative-set
  "Parse a RotateElevationRelativeSet protobuf message to a map."
  [^cmd.RotaryPlatform.JonSharedCmdRotary$RotateElevationRelativeSet proto]
  (cond-> {}
    ;; Regular fields
    (.hasValue proto) (assoc :value (.getValue proto))
    (.hasDirection proto) (assoc :direction (.getDirection proto))))

(defn build-rotate-azimuth-relative
  "Build a RotateAzimuthRelative protobuf message from a map."
  [m]
  (let [builder (cmd.RotaryPlatform.JonSharedCmdRotary$RotateAzimuthRelative/newBuilder)]
    ;; Set regular fields
    (when (contains? m :value)
      (.setValue builder (get m :value)))
    (when (contains? m :speed)
      (.setSpeed builder (get m :speed)))
    (when (contains? m :direction)
      (.setDirection builder (get m :direction)))

    (.build builder)))

(defn parse-rotate-azimuth-relative
  "Parse a RotateAzimuthRelative protobuf message to a map."
  [^cmd.RotaryPlatform.JonSharedCmdRotary$RotateAzimuthRelative proto]
  (cond-> {}
    ;; Regular fields
    (.hasValue proto) (assoc :value (.getValue proto))
    (.hasSpeed proto) (assoc :speed (.getSpeed proto))
    (.hasDirection proto) (assoc :direction (.getDirection proto))))

(defn build-rotate-azimuth-relative-set
  "Build a RotateAzimuthRelativeSet protobuf message from a map."
  [m]
  (let [builder (cmd.RotaryPlatform.JonSharedCmdRotary$RotateAzimuthRelativeSet/newBuilder)]
    ;; Set regular fields
    (when (contains? m :value)
      (.setValue builder (get m :value)))
    (when (contains? m :direction)
      (.setDirection builder (get m :direction)))

    (.build builder)))

(defn parse-rotate-azimuth-relative-set
  "Parse a RotateAzimuthRelativeSet protobuf message to a map."
  [^cmd.RotaryPlatform.JonSharedCmdRotary$RotateAzimuthRelativeSet proto]
  (cond-> {}
    ;; Regular fields
    (.hasValue proto) (assoc :value (.getValue proto))
    (.hasDirection proto) (assoc :direction (.getDirection proto))))

(defn build-set-platform-azimuth
  "Build a SetPlatformAzimuth protobuf message from a map."
  [m]
  (let [builder (cmd.RotaryPlatform.JonSharedCmdRotary$SetPlatformAzimuth/newBuilder)]
    ;; Set regular fields
    (when (contains? m :value)
      (.setValue builder (get m :value)))

    (.build builder)))

(defn parse-set-platform-azimuth
  "Parse a SetPlatformAzimuth protobuf message to a map."
  [^cmd.RotaryPlatform.JonSharedCmdRotary$SetPlatformAzimuth proto]
  (cond-> {}
    ;; Regular fields
    (.hasValue proto) (assoc :value (.getValue proto))))

(defn build-set-platform-elevation
  "Build a SetPlatformElevation protobuf message from a map."
  [m]
  (let [builder (cmd.RotaryPlatform.JonSharedCmdRotary$SetPlatformElevation/newBuilder)]
    ;; Set regular fields
    (when (contains? m :value)
      (.setValue builder (get m :value)))

    (.build builder)))

(defn parse-set-platform-elevation
  "Parse a SetPlatformElevation protobuf message to a map."
  [^cmd.RotaryPlatform.JonSharedCmdRotary$SetPlatformElevation proto]
  (cond-> {}
    ;; Regular fields
    (.hasValue proto) (assoc :value (.getValue proto))))

(defn build-set-platform-bank
  "Build a SetPlatformBank protobuf message from a map."
  [m]
  (let [builder (cmd.RotaryPlatform.JonSharedCmdRotary$SetPlatformBank/newBuilder)]
    ;; Set regular fields
    (when (contains? m :value)
      (.setValue builder (get m :value)))

    (.build builder)))

(defn parse-set-platform-bank
  "Parse a SetPlatformBank protobuf message to a map."
  [^cmd.RotaryPlatform.JonSharedCmdRotary$SetPlatformBank proto]
  (cond-> {}
    ;; Regular fields
    (.hasValue proto) (assoc :value (.getValue proto))))

(defn build-get-meteo
  "Build a GetMeteo protobuf message from a map."
  [m]
  (let [builder (cmd.RotaryPlatform.JonSharedCmdRotary$GetMeteo/newBuilder)]

    (.build builder)))

(defn parse-get-meteo
  "Parse a GetMeteo protobuf message to a map."
  [^cmd.RotaryPlatform.JonSharedCmdRotary$GetMeteo proto]
  (cond-> {}))

(defn build-azimuth-payload
  "Build the oneof payload for Azimuth."
  [builder [field-key value]]
  (case field-key
    :set-value (.setSetValue builder (build-set-azimuth-value value))
    :rotate-to (.setRotateTo builder (build-rotate-azimuth-to value))
    :rotate (.setRotate builder (build-rotate-azimuth value))
    :relative (.setRelative builder (build-rotate-azimuth-relative value))
    :relative-set (.setRelativeSet builder (build-rotate-azimuth-relative-set value))
    :halt (.setHalt builder (build-halt-azimuth value))
    (throw (ex-info "Unknown oneof field" {:field field-key :oneof ":cmd"}))))

(defn parse-azimuth-payload
  "Parse the oneof payload from Azimuth."
  [^cmd.RotaryPlatform.JonSharedCmdRotary$Azimuth proto]
  (cond
    (.hasSetValue proto) {:set-value (parse-set-azimuth-value (.getSetValue proto))}
    (.hasRotateTo proto) {:rotate-to (parse-rotate-azimuth-to (.getRotateTo proto))}
    (.hasRotate proto) {:rotate (parse-rotate-azimuth (.getRotate proto))}
    (.hasRelative proto) {:relative (parse-rotate-azimuth-relative (.getRelative proto))}
    (.hasRelativeSet proto) {:relative-set (parse-rotate-azimuth-relative-set (.getRelativeSet proto))}
    (.hasHalt proto) {:halt (parse-halt-azimuth (.getHalt proto))}))

(defn build-azimuth
  "Build a Azimuth protobuf message from a map."
  [m]
  (let [builder (cmd.RotaryPlatform.JonSharedCmdRotary$Azimuth/newBuilder)]

;; Handle oneof: cmd
    (when-let [cmd-field (first (filter (fn [[k v]] (#{:set-value :rotate-to :rotate :relative :relative-set :halt} k)) m))]
      (build-azimuth-payload builder cmd-field))
    (.build builder)))

(defn parse-azimuth
  "Parse a Azimuth protobuf message to a map."
  [^cmd.RotaryPlatform.JonSharedCmdRotary$Azimuth proto]
  (cond-> {}

;; Oneof payload
    true (merge (parse-azimuth-payload proto))))

(defn build-start
  "Build a Start protobuf message from a map."
  [m]
  (let [builder (cmd.RotaryPlatform.JonSharedCmdRotary$Start/newBuilder)]

    (.build builder)))

(defn parse-start
  "Parse a Start protobuf message to a map."
  [^cmd.RotaryPlatform.JonSharedCmdRotary$Start proto]
  (cond-> {}))

(defn build-stop
  "Build a Stop protobuf message from a map."
  [m]
  (let [builder (cmd.RotaryPlatform.JonSharedCmdRotary$Stop/newBuilder)]

    (.build builder)))

(defn parse-stop
  "Parse a Stop protobuf message to a map."
  [^cmd.RotaryPlatform.JonSharedCmdRotary$Stop proto]
  (cond-> {}))

(defn build-halt
  "Build a Halt protobuf message from a map."
  [m]
  (let [builder (cmd.RotaryPlatform.JonSharedCmdRotary$Halt/newBuilder)]

    (.build builder)))

(defn parse-halt
  "Parse a Halt protobuf message to a map."
  [^cmd.RotaryPlatform.JonSharedCmdRotary$Halt proto]
  (cond-> {}))

(defn build-scan-start
  "Build a ScanStart protobuf message from a map."
  [m]
  (let [builder (cmd.RotaryPlatform.JonSharedCmdRotary$ScanStart/newBuilder)]

    (.build builder)))

(defn parse-scan-start
  "Parse a ScanStart protobuf message to a map."
  [^cmd.RotaryPlatform.JonSharedCmdRotary$ScanStart proto]
  (cond-> {}))

(defn build-scan-stop
  "Build a ScanStop protobuf message from a map."
  [m]
  (let [builder (cmd.RotaryPlatform.JonSharedCmdRotary$ScanStop/newBuilder)]

    (.build builder)))

(defn parse-scan-stop
  "Parse a ScanStop protobuf message to a map."
  [^cmd.RotaryPlatform.JonSharedCmdRotary$ScanStop proto]
  (cond-> {}))

(defn build-scan-pause
  "Build a ScanPause protobuf message from a map."
  [m]
  (let [builder (cmd.RotaryPlatform.JonSharedCmdRotary$ScanPause/newBuilder)]

    (.build builder)))

(defn parse-scan-pause
  "Parse a ScanPause protobuf message to a map."
  [^cmd.RotaryPlatform.JonSharedCmdRotary$ScanPause proto]
  (cond-> {}))

(defn build-scan-unpause
  "Build a ScanUnpause protobuf message from a map."
  [m]
  (let [builder (cmd.RotaryPlatform.JonSharedCmdRotary$ScanUnpause/newBuilder)]

    (.build builder)))

(defn parse-scan-unpause
  "Parse a ScanUnpause protobuf message to a map."
  [^cmd.RotaryPlatform.JonSharedCmdRotary$ScanUnpause proto]
  (cond-> {}))

(defn build-halt-azimuth
  "Build a HaltAzimuth protobuf message from a map."
  [m]
  (let [builder (cmd.RotaryPlatform.JonSharedCmdRotary$HaltAzimuth/newBuilder)]

    (.build builder)))

(defn parse-halt-azimuth
  "Parse a HaltAzimuth protobuf message to a map."
  [^cmd.RotaryPlatform.JonSharedCmdRotary$HaltAzimuth proto]
  (cond-> {}))

(defn build-halt-elevation
  "Build a HaltElevation protobuf message from a map."
  [m]
  (let [builder (cmd.RotaryPlatform.JonSharedCmdRotary$HaltElevation/newBuilder)]

    (.build builder)))

(defn parse-halt-elevation
  "Parse a HaltElevation protobuf message to a map."
  [^cmd.RotaryPlatform.JonSharedCmdRotary$HaltElevation proto]
  (cond-> {}))

(defn build-scan-prev
  "Build a ScanPrev protobuf message from a map."
  [m]
  (let [builder (cmd.RotaryPlatform.JonSharedCmdRotary$ScanPrev/newBuilder)]

    (.build builder)))

(defn parse-scan-prev
  "Parse a ScanPrev protobuf message to a map."
  [^cmd.RotaryPlatform.JonSharedCmdRotary$ScanPrev proto]
  (cond-> {}))

(defn build-scan-next
  "Build a ScanNext protobuf message from a map."
  [m]
  (let [builder (cmd.RotaryPlatform.JonSharedCmdRotary$ScanNext/newBuilder)]

    (.build builder)))

(defn parse-scan-next
  "Parse a ScanNext protobuf message to a map."
  [^cmd.RotaryPlatform.JonSharedCmdRotary$ScanNext proto]
  (cond-> {}))

(defn build-scan-refresh-node-list
  "Build a ScanRefreshNodeList protobuf message from a map."
  [m]
  (let [builder (cmd.RotaryPlatform.JonSharedCmdRotary$ScanRefreshNodeList/newBuilder)]

    (.build builder)))

(defn parse-scan-refresh-node-list
  "Parse a ScanRefreshNodeList protobuf message to a map."
  [^cmd.RotaryPlatform.JonSharedCmdRotary$ScanRefreshNodeList proto]
  (cond-> {}))

(defn build-scan-select-node
  "Build a ScanSelectNode protobuf message from a map."
  [m]
  (let [builder (cmd.RotaryPlatform.JonSharedCmdRotary$ScanSelectNode/newBuilder)]
    ;; Set regular fields
    (when (contains? m :index)
      (.setIndex builder (get m :index)))

    (.build builder)))

(defn parse-scan-select-node
  "Parse a ScanSelectNode protobuf message to a map."
  [^cmd.RotaryPlatform.JonSharedCmdRotary$ScanSelectNode proto]
  (cond-> {}
    ;; Regular fields
    (.hasIndex proto) (assoc :index (.getIndex proto))))

(defn build-scan-delete-node
  "Build a ScanDeleteNode protobuf message from a map."
  [m]
  (let [builder (cmd.RotaryPlatform.JonSharedCmdRotary$ScanDeleteNode/newBuilder)]
    ;; Set regular fields
    (when (contains? m :index)
      (.setIndex builder (get m :index)))

    (.build builder)))

(defn parse-scan-delete-node
  "Parse a ScanDeleteNode protobuf message to a map."
  [^cmd.RotaryPlatform.JonSharedCmdRotary$ScanDeleteNode proto]
  (cond-> {}
    ;; Regular fields
    (.hasIndex proto) (assoc :index (.getIndex proto))))

(defn build-scan-update-node
  "Build a ScanUpdateNode protobuf message from a map."
  [m]
  (let [builder (cmd.RotaryPlatform.JonSharedCmdRotary$ScanUpdateNode/newBuilder)]
    ;; Set regular fields
    (when (contains? m :index)
      (.setIndex builder (get m :index)))
    (when (contains? m :day-zoom-table-value)
      (.setDayZoomTableValue builder (get m :day-zoom-table-value)))
    (when (contains? m :heat-zoom-table-value)
      (.setHeatZoomTableValue builder (get m :heat-zoom-table-value)))
    (when (contains? m :azimuth)
      (.setAzimuth builder (get m :azimuth)))
    (when (contains? m :elevation)
      (.setElevation builder (get m :elevation)))
    (when (contains? m :linger)
      (.setLinger builder (get m :linger)))
    (when (contains? m :speed)
      (.setSpeed builder (get m :speed)))

    (.build builder)))

(defn parse-scan-update-node
  "Parse a ScanUpdateNode protobuf message to a map."
  [^cmd.RotaryPlatform.JonSharedCmdRotary$ScanUpdateNode proto]
  (cond-> {}
    ;; Regular fields
    (.hasIndex proto) (assoc :index (.getIndex proto))
    (.hasDayZoomTableValue proto) (assoc :day-zoom-table-value (.getDayZoomTableValue proto))
    (.hasHeatZoomTableValue proto) (assoc :heat-zoom-table-value (.getHeatZoomTableValue proto))
    (.hasAzimuth proto) (assoc :azimuth (.getAzimuth proto))
    (.hasElevation proto) (assoc :elevation (.getElevation proto))
    (.hasLinger proto) (assoc :linger (.getLinger proto))
    (.hasSpeed proto) (assoc :speed (.getSpeed proto))))

(defn build-scan-add-node
  "Build a ScanAddNode protobuf message from a map."
  [m]
  (let [builder (cmd.RotaryPlatform.JonSharedCmdRotary$ScanAddNode/newBuilder)]
    ;; Set regular fields
    (when (contains? m :index)
      (.setIndex builder (get m :index)))
    (when (contains? m :day-zoom-table-value)
      (.setDayZoomTableValue builder (get m :day-zoom-table-value)))
    (when (contains? m :heat-zoom-table-value)
      (.setHeatZoomTableValue builder (get m :heat-zoom-table-value)))
    (when (contains? m :azimuth)
      (.setAzimuth builder (get m :azimuth)))
    (when (contains? m :elevation)
      (.setElevation builder (get m :elevation)))
    (when (contains? m :linger)
      (.setLinger builder (get m :linger)))
    (when (contains? m :speed)
      (.setSpeed builder (get m :speed)))

    (.build builder)))

(defn parse-scan-add-node
  "Parse a ScanAddNode protobuf message to a map."
  [^cmd.RotaryPlatform.JonSharedCmdRotary$ScanAddNode proto]
  (cond-> {}
    ;; Regular fields
    (.hasIndex proto) (assoc :index (.getIndex proto))
    (.hasDayZoomTableValue proto) (assoc :day-zoom-table-value (.getDayZoomTableValue proto))
    (.hasHeatZoomTableValue proto) (assoc :heat-zoom-table-value (.getHeatZoomTableValue proto))
    (.hasAzimuth proto) (assoc :azimuth (.getAzimuth proto))
    (.hasElevation proto) (assoc :elevation (.getElevation proto))
    (.hasLinger proto) (assoc :linger (.getLinger proto))
    (.hasSpeed proto) (assoc :speed (.getSpeed proto))))

(defn build-elevation-payload
  "Build the oneof payload for Elevation."
  [builder [field-key value]]
  (case field-key
    :set-value (.setSetValue builder (build-set-elevation-value value))
    :rotate-to (.setRotateTo builder (build-rotate-elevation-to value))
    :rotate (.setRotate builder (build-rotate-elevation value))
    :relative (.setRelative builder (build-rotate-elevation-relative value))
    :relative-set (.setRelativeSet builder (build-rotate-elevation-relative-set value))
    :halt (.setHalt builder (build-halt-elevation value))
    (throw (ex-info "Unknown oneof field" {:field field-key :oneof ":cmd"}))))

(defn parse-elevation-payload
  "Parse the oneof payload from Elevation."
  [^cmd.RotaryPlatform.JonSharedCmdRotary$Elevation proto]
  (cond
    (.hasSetValue proto) {:set-value (parse-set-elevation-value (.getSetValue proto))}
    (.hasRotateTo proto) {:rotate-to (parse-rotate-elevation-to (.getRotateTo proto))}
    (.hasRotate proto) {:rotate (parse-rotate-elevation (.getRotate proto))}
    (.hasRelative proto) {:relative (parse-rotate-elevation-relative (.getRelative proto))}
    (.hasRelativeSet proto) {:relative-set (parse-rotate-elevation-relative-set (.getRelativeSet proto))}
    (.hasHalt proto) {:halt (parse-halt-elevation (.getHalt proto))}))

(defn build-elevation
  "Build a Elevation protobuf message from a map."
  [m]
  (let [builder (cmd.RotaryPlatform.JonSharedCmdRotary$Elevation/newBuilder)]

;; Handle oneof: cmd
    (when-let [cmd-field (first (filter (fn [[k v]] (#{:set-value :rotate-to :rotate :relative :relative-set :halt} k)) m))]
      (build-elevation-payload builder cmd-field))
    (.build builder)))

(defn parse-elevation
  "Parse a Elevation protobuf message to a map."
  [^cmd.RotaryPlatform.JonSharedCmdRotary$Elevation proto]
  (cond-> {}

;; Oneof payload
    true (merge (parse-elevation-payload proto))))

(defn build-set-use-rotary-as-compass
  "Build a setUseRotaryAsCompass protobuf message from a map."
  [m]
  (let [builder (cmd.RotaryPlatform.JonSharedCmdRotary$setUseRotaryAsCompass/newBuilder)]
    ;; Set regular fields
    (when (contains? m :flag)
      (.setFlag builder (get m :flag)))

    (.build builder)))

(defn parse-set-use-rotary-as-compass
  "Parse a setUseRotaryAsCompass protobuf message to a map."
  [^cmd.RotaryPlatform.JonSharedCmdRotary$setUseRotaryAsCompass proto]
  (cond-> {}
    ;; Regular fields
    (.hasFlag proto) (assoc :flag (.getFlag proto))))

(defn build-rotate-to-gps
  "Build a RotateToGPS protobuf message from a map."
  [m]
  (let [builder (cmd.RotaryPlatform.JonSharedCmdRotary$RotateToGPS/newBuilder)]
    ;; Set regular fields
    (when (contains? m :latitude)
      (.setLatitude builder (get m :latitude)))
    (when (contains? m :longitude)
      (.setLongitude builder (get m :longitude)))
    (when (contains? m :altitude)
      (.setAltitude builder (get m :altitude)))

    (.build builder)))

(defn parse-rotate-to-gps
  "Parse a RotateToGPS protobuf message to a map."
  [^cmd.RotaryPlatform.JonSharedCmdRotary$RotateToGPS proto]
  (cond-> {}
    ;; Regular fields
    (.hasLatitude proto) (assoc :latitude (.getLatitude proto))
    (.hasLongitude proto) (assoc :longitude (.getLongitude proto))
    (.hasAltitude proto) (assoc :altitude (.getAltitude proto))))

(defn build-set-origin-gps
  "Build a SetOriginGPS protobuf message from a map."
  [m]
  (let [builder (cmd.RotaryPlatform.JonSharedCmdRotary$SetOriginGPS/newBuilder)]
    ;; Set regular fields
    (when (contains? m :latitude)
      (.setLatitude builder (get m :latitude)))
    (when (contains? m :longitude)
      (.setLongitude builder (get m :longitude)))
    (when (contains? m :altitude)
      (.setAltitude builder (get m :altitude)))

    (.build builder)))

(defn parse-set-origin-gps
  "Parse a SetOriginGPS protobuf message to a map."
  [^cmd.RotaryPlatform.JonSharedCmdRotary$SetOriginGPS proto]
  (cond-> {}
    ;; Regular fields
    (.hasLatitude proto) (assoc :latitude (.getLatitude proto))
    (.hasLongitude proto) (assoc :longitude (.getLongitude proto))
    (.hasAltitude proto) (assoc :altitude (.getAltitude proto))))

(defn build-rotate-to-ndc
  "Build a RotateToNDC protobuf message from a map."
  [m]
  (let [builder (cmd.RotaryPlatform.JonSharedCmdRotary$RotateToNDC/newBuilder)]
    ;; Set regular fields
    (when (contains? m :channel)
      (.setChannel builder (get m :channel)))
    (when (contains? m :x)
      (.setX builder (get m :x)))
    (when (contains? m :y)
      (.setY builder (get m :y)))

    (.build builder)))

(defn parse-rotate-to-ndc
  "Parse a RotateToNDC protobuf message to a map."
  [^cmd.RotaryPlatform.JonSharedCmdRotary$RotateToNDC proto]
  (cond-> {}
    ;; Regular fields
    (.hasChannel proto) (assoc :channel (.getChannel proto))
    (.hasX proto) (assoc :x (.getX proto))
    (.hasY proto) (assoc :y (.getY proto))))

(defn build-root-payload
  "Build the oneof payload for Root."
  [builder [field-key value]]
  (case field-key
    :show-default-screen (.setShowDefaultScreen builder (build-show-default-screen value))
    :show-lrf-measure-screen (.setShowLrfMeasureScreen builder (build-show-lrf-measure-screen value))
    :show-lrf-result-screen (.setShowLrfResultScreen builder (build-show-lrf-result-screen value))
    :show-lrf-result-simplified-screen (.setShowLrfResultSimplifiedScreen builder (build-show-lrf-result-simplified-screen value))
    :enable-heat-osd (.setEnableHeatOsd builder (build-enable-heat-osd value))
    :disable-heat-osd (.setDisableHeatOsd builder (build-disable-heat-osd value))
    :enable-day-osd (.setEnableDayOsd builder (build-enable-day-osd value))
    :disable-day-osd (.setDisableDayOsd builder (build-disable-day-osd value))
    (throw (ex-info "Unknown oneof field" {:field field-key :oneof ":cmd"}))))

(defn parse-root-payload
  "Parse the oneof payload from Root."
  [^cmd.OSD.JonSharedCmdOsd$Root proto]
  (cond
    (.hasShowDefaultScreen proto) {:show-default-screen (parse-show-default-screen (.getShowDefaultScreen proto))}
    (.hasShowLrfMeasureScreen proto) {:show-lrf-measure-screen (parse-show-lrf-measure-screen (.getShowLrfMeasureScreen proto))}
    (.hasShowLrfResultScreen proto) {:show-lrf-result-screen (parse-show-lrf-result-screen (.getShowLrfResultScreen proto))}
    (.hasShowLrfResultSimplifiedScreen proto) {:show-lrf-result-simplified-screen (parse-show-lrf-result-simplified-screen (.getShowLrfResultSimplifiedScreen proto))}
    (.hasEnableHeatOsd proto) {:enable-heat-osd (parse-enable-heat-osd (.getEnableHeatOsd proto))}
    (.hasDisableHeatOsd proto) {:disable-heat-osd (parse-disable-heat-osd (.getDisableHeatOsd proto))}
    (.hasEnableDayOsd proto) {:enable-day-osd (parse-enable-day-osd (.getEnableDayOsd proto))}
    (.hasDisableDayOsd proto) {:disable-day-osd (parse-disable-day-osd (.getDisableDayOsd proto))}))

(defn build-root
  "Build a Root protobuf message from a map."
  [m]
  (let [builder (cmd.OSD.JonSharedCmdOsd$Root/newBuilder)]

;; Handle oneof: cmd
    (when-let [cmd-field (first (filter (fn [[k v]] (#{:show-default-screen :show-lrf-measure-screen :show-lrf-result-screen :show-lrf-result-simplified-screen :enable-heat-osd :disable-heat-osd :enable-day-osd :disable-day-osd} k)) m))]
      (build-root-payload builder cmd-field))
    (.build builder)))

(defn parse-root
  "Parse a Root protobuf message to a map."
  [^cmd.OSD.JonSharedCmdOsd$Root proto]
  (cond-> {}

;; Oneof payload
    true (merge (parse-root-payload proto))))

(defn build-show-default-screen
  "Build a ShowDefaultScreen protobuf message from a map."
  [m]
  (let [builder (cmd.OSD.JonSharedCmdOsd$ShowDefaultScreen/newBuilder)]

    (.build builder)))

(defn parse-show-default-screen
  "Parse a ShowDefaultScreen protobuf message to a map."
  [^cmd.OSD.JonSharedCmdOsd$ShowDefaultScreen proto]
  (cond-> {}))

(defn build-show-lrf-measure-screen
  "Build a ShowLRFMeasureScreen protobuf message from a map."
  [m]
  (let [builder (cmd.OSD.JonSharedCmdOsd$ShowLRFMeasureScreen/newBuilder)]

    (.build builder)))

(defn parse-show-lrf-measure-screen
  "Parse a ShowLRFMeasureScreen protobuf message to a map."
  [^cmd.OSD.JonSharedCmdOsd$ShowLRFMeasureScreen proto]
  (cond-> {}))

(defn build-show-lrf-result-screen
  "Build a ShowLRFResultScreen protobuf message from a map."
  [m]
  (let [builder (cmd.OSD.JonSharedCmdOsd$ShowLRFResultScreen/newBuilder)]

    (.build builder)))

(defn parse-show-lrf-result-screen
  "Parse a ShowLRFResultScreen protobuf message to a map."
  [^cmd.OSD.JonSharedCmdOsd$ShowLRFResultScreen proto]
  (cond-> {}))

(defn build-show-lrf-result-simplified-screen
  "Build a ShowLRFResultSimplifiedScreen protobuf message from a map."
  [m]
  (let [builder (cmd.OSD.JonSharedCmdOsd$ShowLRFResultSimplifiedScreen/newBuilder)]

    (.build builder)))

(defn parse-show-lrf-result-simplified-screen
  "Parse a ShowLRFResultSimplifiedScreen protobuf message to a map."
  [^cmd.OSD.JonSharedCmdOsd$ShowLRFResultSimplifiedScreen proto]
  (cond-> {}))

(defn build-enable-heat-osd
  "Build a EnableHeatOSD protobuf message from a map."
  [m]
  (let [builder (cmd.OSD.JonSharedCmdOsd$EnableHeatOSD/newBuilder)]

    (.build builder)))

(defn parse-enable-heat-osd
  "Parse a EnableHeatOSD protobuf message to a map."
  [^cmd.OSD.JonSharedCmdOsd$EnableHeatOSD proto]
  (cond-> {}))

(defn build-disable-heat-osd
  "Build a DisableHeatOSD protobuf message from a map."
  [m]
  (let [builder (cmd.OSD.JonSharedCmdOsd$DisableHeatOSD/newBuilder)]

    (.build builder)))

(defn parse-disable-heat-osd
  "Parse a DisableHeatOSD protobuf message to a map."
  [^cmd.OSD.JonSharedCmdOsd$DisableHeatOSD proto]
  (cond-> {}))

(defn build-enable-day-osd
  "Build a EnableDayOSD protobuf message from a map."
  [m]
  (let [builder (cmd.OSD.JonSharedCmdOsd$EnableDayOSD/newBuilder)]

    (.build builder)))

(defn parse-enable-day-osd
  "Parse a EnableDayOSD protobuf message to a map."
  [^cmd.OSD.JonSharedCmdOsd$EnableDayOSD proto]
  (cond-> {}))

(defn build-disable-day-osd
  "Build a DisableDayOSD protobuf message from a map."
  [m]
  (let [builder (cmd.OSD.JonSharedCmdOsd$DisableDayOSD/newBuilder)]

    (.build builder)))

(defn parse-disable-day-osd
  "Parse a DisableDayOSD protobuf message to a map."
  [^cmd.OSD.JonSharedCmdOsd$DisableDayOSD proto]
  (cond-> {}))

(defn build-root-payload
  "Build the oneof payload for Root."
  [builder [field-key value]]
  (case field-key
    :day (.setDay builder (build-offsets value))
    :heat (.setHeat builder (build-offsets value))
    (throw (ex-info "Unknown oneof field" {:field field-key :oneof ":channel"}))))

(defn parse-root-payload
  "Parse the oneof payload from Root."
  [^cmd.Lrf_calib.JonSharedCmdLrfAlign$Root proto]
  (cond
    (.hasDay proto) {:day (parse-offsets (.getDay proto))}
    (.hasHeat proto) {:heat (parse-offsets (.getHeat proto))}))

(defn build-root
  "Build a Root protobuf message from a map."
  [m]
  (let [builder (cmd.Lrf_calib.JonSharedCmdLrfAlign$Root/newBuilder)]

;; Handle oneof: channel
    (when-let [channel-field (first (filter (fn [[k v]] (#{:day :heat} k)) m))]
      (build-root-payload builder channel-field))
    (.build builder)))

(defn parse-root
  "Parse a Root protobuf message to a map."
  [^cmd.Lrf_calib.JonSharedCmdLrfAlign$Root proto]
  (cond-> {}

;; Oneof payload
    true (merge (parse-root-payload proto))))

(defn build-offsets-payload
  "Build the oneof payload for Offsets."
  [builder [field-key value]]
  (case field-key
    :set (.setSet builder (build-set-offsets value))
    :save (.setSave builder (build-save-offsets value))
    :reset (.setReset builder (build-reset-offsets value))
    :shift (.setShift builder (build-shift-offsets-by value))
    (throw (ex-info "Unknown oneof field" {:field field-key :oneof ":cmd"}))))

(defn parse-offsets-payload
  "Parse the oneof payload from Offsets."
  [^cmd.Lrf_calib.JonSharedCmdLrfAlign$Offsets proto]
  (cond
    (.hasSet proto) {:set (parse-set-offsets (.getSet proto))}
    (.hasSave proto) {:save (parse-save-offsets (.getSave proto))}
    (.hasReset proto) {:reset (parse-reset-offsets (.getReset proto))}
    (.hasShift proto) {:shift (parse-shift-offsets-by (.getShift proto))}))

(defn build-offsets
  "Build a Offsets protobuf message from a map."
  [m]
  (let [builder (cmd.Lrf_calib.JonSharedCmdLrfAlign$Offsets/newBuilder)]

;; Handle oneof: cmd
    (when-let [cmd-field (first (filter (fn [[k v]] (#{:set :save :reset :shift} k)) m))]
      (build-offsets-payload builder cmd-field))
    (.build builder)))

(defn parse-offsets
  "Parse a Offsets protobuf message to a map."
  [^cmd.Lrf_calib.JonSharedCmdLrfAlign$Offsets proto]
  (cond-> {}

;; Oneof payload
    true (merge (parse-offsets-payload proto))))

(defn build-set-offsets
  "Build a SetOffsets protobuf message from a map."
  [m]
  (let [builder (cmd.Lrf_calib.JonSharedCmdLrfAlign$SetOffsets/newBuilder)]
    ;; Set regular fields
    (when (contains? m :x)
      (.setX builder (get m :x)))
    (when (contains? m :y)
      (.setY builder (get m :y)))

    (.build builder)))

(defn parse-set-offsets
  "Parse a SetOffsets protobuf message to a map."
  [^cmd.Lrf_calib.JonSharedCmdLrfAlign$SetOffsets proto]
  (cond-> {}
    ;; Regular fields
    (.hasX proto) (assoc :x (.getX proto))
    (.hasY proto) (assoc :y (.getY proto))))

(defn build-shift-offsets-by
  "Build a ShiftOffsetsBy protobuf message from a map."
  [m]
  (let [builder (cmd.Lrf_calib.JonSharedCmdLrfAlign$ShiftOffsetsBy/newBuilder)]
    ;; Set regular fields
    (when (contains? m :x)
      (.setX builder (get m :x)))
    (when (contains? m :y)
      (.setY builder (get m :y)))

    (.build builder)))

(defn parse-shift-offsets-by
  "Parse a ShiftOffsetsBy protobuf message to a map."
  [^cmd.Lrf_calib.JonSharedCmdLrfAlign$ShiftOffsetsBy proto]
  (cond-> {}
    ;; Regular fields
    (.hasX proto) (assoc :x (.getX proto))
    (.hasY proto) (assoc :y (.getY proto))))

(defn build-reset-offsets
  "Build a ResetOffsets protobuf message from a map."
  [m]
  (let [builder (cmd.Lrf_calib.JonSharedCmdLrfAlign$ResetOffsets/newBuilder)]

    (.build builder)))

(defn parse-reset-offsets
  "Parse a ResetOffsets protobuf message to a map."
  [^cmd.Lrf_calib.JonSharedCmdLrfAlign$ResetOffsets proto]
  (cond-> {}))

(defn build-save-offsets
  "Build a SaveOffsets protobuf message from a map."
  [m]
  (let [builder (cmd.Lrf_calib.JonSharedCmdLrfAlign$SaveOffsets/newBuilder)]

    (.build builder)))

(defn parse-save-offsets
  "Parse a SaveOffsets protobuf message to a map."
  [^cmd.Lrf_calib.JonSharedCmdLrfAlign$SaveOffsets proto]
  (cond-> {}))

(defn build-root-payload
  "Build the oneof payload for Root."
  [builder [field-key value]]
  (case field-key
    :start-all (.setStartAll builder (build-start-a-ll value))
    :stop-all (.setStopAll builder (build-stop-a-ll value))
    :reboot (.setReboot builder (build-reboot value))
    :power-off (.setPowerOff builder (build-power-off value))
    :localization (.setLocalization builder (build-set-localization value))
    :reset-configs (.setResetConfigs builder (build-reset-configs value))
    :start-rec (.setStartRec builder (build-start-rec value))
    :stop-rec (.setStopRec builder (build-stop-rec value))
    :mark-rec-important (.setMarkRecImportant builder (build-mark-rec-important value))
    :unmark-rec-important (.setUnmarkRecImportant builder (build-unmark-rec-important value))
    :enter-transport (.setEnterTransport builder (build-enter-transport value))
    :geodesic-mode-enable (.setGeodesicModeEnable builder (build-enable-geodesic-mode value))
    :geodesic-mode-disable (.setGeodesicModeDisable builder (build-disable-geodesic-mode value))
    (throw (ex-info "Unknown oneof field" {:field field-key :oneof ":cmd"}))))

(defn parse-root-payload
  "Parse the oneof payload from Root."
  [^cmd.System.JonSharedCmdSystem$Root proto]
  (cond
    (.hasStartAll proto) {:start-all (parse-start-a-ll (.getStartAll proto))}
    (.hasStopAll proto) {:stop-all (parse-stop-a-ll (.getStopAll proto))}
    (.hasReboot proto) {:reboot (parse-reboot (.getReboot proto))}
    (.hasPowerOff proto) {:power-off (parse-power-off (.getPowerOff proto))}
    (.hasLocalization proto) {:localization (parse-set-localization (.getLocalization proto))}
    (.hasResetConfigs proto) {:reset-configs (parse-reset-configs (.getResetConfigs proto))}
    (.hasStartRec proto) {:start-rec (parse-start-rec (.getStartRec proto))}
    (.hasStopRec proto) {:stop-rec (parse-stop-rec (.getStopRec proto))}
    (.hasMarkRecImportant proto) {:mark-rec-important (parse-mark-rec-important (.getMarkRecImportant proto))}
    (.hasUnmarkRecImportant proto) {:unmark-rec-important (parse-unmark-rec-important (.getUnmarkRecImportant proto))}
    (.hasEnterTransport proto) {:enter-transport (parse-enter-transport (.getEnterTransport proto))}
    (.hasGeodesicModeEnable proto) {:geodesic-mode-enable (parse-enable-geodesic-mode (.getGeodesicModeEnable proto))}
    (.hasGeodesicModeDisable proto) {:geodesic-mode-disable (parse-disable-geodesic-mode (.getGeodesicModeDisable proto))}))

(defn build-root
  "Build a Root protobuf message from a map."
  [m]
  (let [builder (cmd.System.JonSharedCmdSystem$Root/newBuilder)]

;; Handle oneof: cmd
    (when-let [cmd-field (first (filter (fn [[k v]] (#{:start-all :stop-all :reboot :power-off :localization :reset-configs :start-rec :stop-rec :mark-rec-important :unmark-rec-important :enter-transport :geodesic-mode-enable :geodesic-mode-disable} k)) m))]
      (build-root-payload builder cmd-field))
    (.build builder)))

(defn parse-root
  "Parse a Root protobuf message to a map."
  [^cmd.System.JonSharedCmdSystem$Root proto]
  (cond-> {}

;; Oneof payload
    true (merge (parse-root-payload proto))))

(defn build-start-a-ll
  "Build a StartALl protobuf message from a map."
  [m]
  (let [builder (cmd.System.JonSharedCmdSystem$StartALl/newBuilder)]

    (.build builder)))

(defn parse-start-a-ll
  "Parse a StartALl protobuf message to a map."
  [^cmd.System.JonSharedCmdSystem$StartALl proto]
  (cond-> {}))

(defn build-stop-a-ll
  "Build a StopALl protobuf message from a map."
  [m]
  (let [builder (cmd.System.JonSharedCmdSystem$StopALl/newBuilder)]

    (.build builder)))

(defn parse-stop-a-ll
  "Parse a StopALl protobuf message to a map."
  [^cmd.System.JonSharedCmdSystem$StopALl proto]
  (cond-> {}))

(defn build-reboot
  "Build a Reboot protobuf message from a map."
  [m]
  (let [builder (cmd.System.JonSharedCmdSystem$Reboot/newBuilder)]

    (.build builder)))

(defn parse-reboot
  "Parse a Reboot protobuf message to a map."
  [^cmd.System.JonSharedCmdSystem$Reboot proto]
  (cond-> {}))

(defn build-power-off
  "Build a PowerOff protobuf message from a map."
  [m]
  (let [builder (cmd.System.JonSharedCmdSystem$PowerOff/newBuilder)]

    (.build builder)))

(defn parse-power-off
  "Parse a PowerOff protobuf message to a map."
  [^cmd.System.JonSharedCmdSystem$PowerOff proto]
  (cond-> {}))

(defn build-reset-configs
  "Build a ResetConfigs protobuf message from a map."
  [m]
  (let [builder (cmd.System.JonSharedCmdSystem$ResetConfigs/newBuilder)]

    (.build builder)))

(defn parse-reset-configs
  "Parse a ResetConfigs protobuf message to a map."
  [^cmd.System.JonSharedCmdSystem$ResetConfigs proto]
  (cond-> {}))

(defn build-start-rec
  "Build a StartRec protobuf message from a map."
  [m]
  (let [builder (cmd.System.JonSharedCmdSystem$StartRec/newBuilder)]

    (.build builder)))

(defn parse-start-rec
  "Parse a StartRec protobuf message to a map."
  [^cmd.System.JonSharedCmdSystem$StartRec proto]
  (cond-> {}))

(defn build-stop-rec
  "Build a StopRec protobuf message from a map."
  [m]
  (let [builder (cmd.System.JonSharedCmdSystem$StopRec/newBuilder)]

    (.build builder)))

(defn parse-stop-rec
  "Parse a StopRec protobuf message to a map."
  [^cmd.System.JonSharedCmdSystem$StopRec proto]
  (cond-> {}))

(defn build-mark-rec-important
  "Build a MarkRecImportant protobuf message from a map."
  [m]
  (let [builder (cmd.System.JonSharedCmdSystem$MarkRecImportant/newBuilder)]

    (.build builder)))

(defn parse-mark-rec-important
  "Parse a MarkRecImportant protobuf message to a map."
  [^cmd.System.JonSharedCmdSystem$MarkRecImportant proto]
  (cond-> {}))

(defn build-unmark-rec-important
  "Build a UnmarkRecImportant protobuf message from a map."
  [m]
  (let [builder (cmd.System.JonSharedCmdSystem$UnmarkRecImportant/newBuilder)]

    (.build builder)))

(defn parse-unmark-rec-important
  "Parse a UnmarkRecImportant protobuf message to a map."
  [^cmd.System.JonSharedCmdSystem$UnmarkRecImportant proto]
  (cond-> {}))

(defn build-enter-transport
  "Build a EnterTransport protobuf message from a map."
  [m]
  (let [builder (cmd.System.JonSharedCmdSystem$EnterTransport/newBuilder)]

    (.build builder)))

(defn parse-enter-transport
  "Parse a EnterTransport protobuf message to a map."
  [^cmd.System.JonSharedCmdSystem$EnterTransport proto]
  (cond-> {}))

(defn build-enable-geodesic-mode
  "Build a EnableGeodesicMode protobuf message from a map."
  [m]
  (let [builder (cmd.System.JonSharedCmdSystem$EnableGeodesicMode/newBuilder)]

    (.build builder)))

(defn parse-enable-geodesic-mode
  "Parse a EnableGeodesicMode protobuf message to a map."
  [^cmd.System.JonSharedCmdSystem$EnableGeodesicMode proto]
  (cond-> {}))

(defn build-disable-geodesic-mode
  "Build a DisableGeodesicMode protobuf message from a map."
  [m]
  (let [builder (cmd.System.JonSharedCmdSystem$DisableGeodesicMode/newBuilder)]

    (.build builder)))

(defn parse-disable-geodesic-mode
  "Parse a DisableGeodesicMode protobuf message to a map."
  [^cmd.System.JonSharedCmdSystem$DisableGeodesicMode proto]
  (cond-> {}))

(defn build-set-localization
  "Build a SetLocalization protobuf message from a map."
  [m]
  (let [builder (cmd.System.JonSharedCmdSystem$SetLocalization/newBuilder)]
    ;; Set regular fields
    (when (contains? m :loc)
      (.setLoc builder (get m :loc)))

    (.build builder)))

(defn parse-set-localization
  "Parse a SetLocalization protobuf message to a map."
  [^cmd.System.JonSharedCmdSystem$SetLocalization proto]
  (cond-> {}
    ;; Regular fields
    (.hasLoc proto) (assoc :loc (.getLoc proto))))

(defn build-root-payload
  "Build the oneof payload for Root."
  [builder [field-key value]]
  (case field-key
    :set-auto-focus (.setSetAutoFocus builder (build-set-auto-focus value))
    :start-track-ndc (.setStartTrackNdc builder (build-start-track-ndc value))
    :stop-track (.setStopTrack builder (build-stop-track value))
    :vampire-mode-enable (.setVampireModeEnable builder (build-vampire-mode-enable value))
    :vampire-mode-disable (.setVampireModeDisable builder (build-vampire-mode-disable value))
    :stabilization-mode-enable (.setStabilizationModeEnable builder (build-stabilization-mode-enable value))
    :stabilization-mode-disable (.setStabilizationModeDisable builder (build-stabilization-mode-disable value))
    :dump-start (.setDumpStart builder (build-dump-start value))
    :dump-stop (.setDumpStop builder (build-dump-stop value))
    (throw (ex-info "Unknown oneof field" {:field field-key :oneof ":cmd"}))))

(defn parse-root-payload
  "Parse the oneof payload from Root."
  [^cmd.CV.JonSharedCmdCv$Root proto]
  (cond
    (.hasSetAutoFocus proto) {:set-auto-focus (parse-set-auto-focus (.getSetAutoFocus proto))}
    (.hasStartTrackNdc proto) {:start-track-ndc (parse-start-track-ndc (.getStartTrackNdc proto))}
    (.hasStopTrack proto) {:stop-track (parse-stop-track (.getStopTrack proto))}
    (.hasVampireModeEnable proto) {:vampire-mode-enable (parse-vampire-mode-enable (.getVampireModeEnable proto))}
    (.hasVampireModeDisable proto) {:vampire-mode-disable (parse-vampire-mode-disable (.getVampireModeDisable proto))}
    (.hasStabilizationModeEnable proto) {:stabilization-mode-enable (parse-stabilization-mode-enable (.getStabilizationModeEnable proto))}
    (.hasStabilizationModeDisable proto) {:stabilization-mode-disable (parse-stabilization-mode-disable (.getStabilizationModeDisable proto))}
    (.hasDumpStart proto) {:dump-start (parse-dump-start (.getDumpStart proto))}
    (.hasDumpStop proto) {:dump-stop (parse-dump-stop (.getDumpStop proto))}))

(defn build-root
  "Build a Root protobuf message from a map."
  [m]
  (let [builder (cmd.CV.JonSharedCmdCv$Root/newBuilder)]

;; Handle oneof: cmd
    (when-let [cmd-field (first (filter (fn [[k v]] (#{:set-auto-focus :start-track-ndc :stop-track :vampire-mode-enable :vampire-mode-disable :stabilization-mode-enable :stabilization-mode-disable :dump-start :dump-stop} k)) m))]
      (build-root-payload builder cmd-field))
    (.build builder)))

(defn parse-root
  "Parse a Root protobuf message to a map."
  [^cmd.CV.JonSharedCmdCv$Root proto]
  (cond-> {}

;; Oneof payload
    true (merge (parse-root-payload proto))))

(defn build-vampire-mode-enable
  "Build a VampireModeEnable protobuf message from a map."
  [m]
  (let [builder (cmd.CV.JonSharedCmdCv$VampireModeEnable/newBuilder)]

    (.build builder)))

(defn parse-vampire-mode-enable
  "Parse a VampireModeEnable protobuf message to a map."
  [^cmd.CV.JonSharedCmdCv$VampireModeEnable proto]
  (cond-> {}))

(defn build-dump-start
  "Build a DumpStart protobuf message from a map."
  [m]
  (let [builder (cmd.CV.JonSharedCmdCv$DumpStart/newBuilder)]

    (.build builder)))

(defn parse-dump-start
  "Parse a DumpStart protobuf message to a map."
  [^cmd.CV.JonSharedCmdCv$DumpStart proto]
  (cond-> {}))

(defn build-dump-stop
  "Build a DumpStop protobuf message from a map."
  [m]
  (let [builder (cmd.CV.JonSharedCmdCv$DumpStop/newBuilder)]

    (.build builder)))

(defn parse-dump-stop
  "Parse a DumpStop protobuf message to a map."
  [^cmd.CV.JonSharedCmdCv$DumpStop proto]
  (cond-> {}))

(defn build-vampire-mode-disable
  "Build a VampireModeDisable protobuf message from a map."
  [m]
  (let [builder (cmd.CV.JonSharedCmdCv$VampireModeDisable/newBuilder)]

    (.build builder)))

(defn parse-vampire-mode-disable
  "Parse a VampireModeDisable protobuf message to a map."
  [^cmd.CV.JonSharedCmdCv$VampireModeDisable proto]
  (cond-> {}))

(defn build-stabilization-mode-enable
  "Build a StabilizationModeEnable protobuf message from a map."
  [m]
  (let [builder (cmd.CV.JonSharedCmdCv$StabilizationModeEnable/newBuilder)]

    (.build builder)))

(defn parse-stabilization-mode-enable
  "Parse a StabilizationModeEnable protobuf message to a map."
  [^cmd.CV.JonSharedCmdCv$StabilizationModeEnable proto]
  (cond-> {}))

(defn build-stabilization-mode-disable
  "Build a StabilizationModeDisable protobuf message from a map."
  [m]
  (let [builder (cmd.CV.JonSharedCmdCv$StabilizationModeDisable/newBuilder)]

    (.build builder)))

(defn parse-stabilization-mode-disable
  "Parse a StabilizationModeDisable protobuf message to a map."
  [^cmd.CV.JonSharedCmdCv$StabilizationModeDisable proto]
  (cond-> {}))

(defn build-set-auto-focus
  "Build a SetAutoFocus protobuf message from a map."
  [m]
  (let [builder (cmd.CV.JonSharedCmdCv$SetAutoFocus/newBuilder)]
    ;; Set regular fields
    (when (contains? m :channel)
      (.setChannel builder (get m :channel)))
    (when (contains? m :value)
      (.setValue builder (get m :value)))

    (.build builder)))

(defn parse-set-auto-focus
  "Parse a SetAutoFocus protobuf message to a map."
  [^cmd.CV.JonSharedCmdCv$SetAutoFocus proto]
  (cond-> {}
    ;; Regular fields
    (.hasChannel proto) (assoc :channel (.getChannel proto))
    (.hasValue proto) (assoc :value (.getValue proto))))

(defn build-start-track-ndc
  "Build a StartTrackNDC protobuf message from a map."
  [m]
  (let [builder (cmd.CV.JonSharedCmdCv$StartTrackNDC/newBuilder)]
    ;; Set regular fields
    (when (contains? m :channel)
      (.setChannel builder (get m :channel)))
    (when (contains? m :x)
      (.setX builder (get m :x)))
    (when (contains? m :y)
      (.setY builder (get m :y)))
    (when (contains? m :frame-time)
      (.setFrameTime builder (get m :frame-time)))

    (.build builder)))

(defn parse-start-track-ndc
  "Parse a StartTrackNDC protobuf message to a map."
  [^cmd.CV.JonSharedCmdCv$StartTrackNDC proto]
  (cond-> {}
    ;; Regular fields
    (.hasChannel proto) (assoc :channel (.getChannel proto))
    (.hasX proto) (assoc :x (.getX proto))
    (.hasY proto) (assoc :y (.getY proto))
    (.hasFrameTime proto) (assoc :frame-time (.getFrameTime proto))))

(defn build-stop-track
  "Build a StopTrack protobuf message from a map."
  [m]
  (let [builder (cmd.CV.JonSharedCmdCv$StopTrack/newBuilder)]

    (.build builder)))

(defn parse-stop-track
  "Parse a StopTrack protobuf message to a map."
  [^cmd.CV.JonSharedCmdCv$StopTrack proto]
  (cond-> {}))

(defn build-root-payload
  "Build the oneof payload for Root."
  [builder [field-key value]]
  (case field-key
    :start (.setStart builder (build-start value))
    :stop (.setStop builder (build-stop value))
    :turn-on (.setTurnOn builder (build-turn-on value))
    :turn-off (.setTurnOff builder (build-turn-off value))
    :get-meteo (.setGetMeteo builder (build-get-meteo value))
    (throw (ex-info "Unknown oneof field" {:field field-key :oneof ":cmd"}))))

(defn parse-root-payload
  "Parse the oneof payload from Root."
  [^cmd.DayCamGlassHeater.JonSharedCmdDayCamGlassHeater$Root proto]
  (cond
    (.hasStart proto) {:start (parse-start (.getStart proto))}
    (.hasStop proto) {:stop (parse-stop (.getStop proto))}
    (.hasTurnOn proto) {:turn-on (parse-turn-on (.getTurnOn proto))}
    (.hasTurnOff proto) {:turn-off (parse-turn-off (.getTurnOff proto))}
    (.hasGetMeteo proto) {:get-meteo (parse-get-meteo (.getGetMeteo proto))}))

(defn build-root
  "Build a Root protobuf message from a map."
  [m]
  (let [builder (cmd.DayCamGlassHeater.JonSharedCmdDayCamGlassHeater$Root/newBuilder)]

;; Handle oneof: cmd
    (when-let [cmd-field (first (filter (fn [[k v]] (#{:start :stop :turn-on :turn-off :get-meteo} k)) m))]
      (build-root-payload builder cmd-field))
    (.build builder)))

(defn parse-root
  "Parse a Root protobuf message to a map."
  [^cmd.DayCamGlassHeater.JonSharedCmdDayCamGlassHeater$Root proto]
  (cond-> {}

;; Oneof payload
    true (merge (parse-root-payload proto))))

(defn build-start
  "Build a Start protobuf message from a map."
  [m]
  (let [builder (cmd.DayCamGlassHeater.JonSharedCmdDayCamGlassHeater$Start/newBuilder)]

    (.build builder)))

(defn parse-start
  "Parse a Start protobuf message to a map."
  [^cmd.DayCamGlassHeater.JonSharedCmdDayCamGlassHeater$Start proto]
  (cond-> {}))

(defn build-stop
  "Build a Stop protobuf message from a map."
  [m]
  (let [builder (cmd.DayCamGlassHeater.JonSharedCmdDayCamGlassHeater$Stop/newBuilder)]

    (.build builder)))

(defn parse-stop
  "Parse a Stop protobuf message to a map."
  [^cmd.DayCamGlassHeater.JonSharedCmdDayCamGlassHeater$Stop proto]
  (cond-> {}))

(defn build-turn-on
  "Build a TurnOn protobuf message from a map."
  [m]
  (let [builder (cmd.DayCamGlassHeater.JonSharedCmdDayCamGlassHeater$TurnOn/newBuilder)]

    (.build builder)))

(defn parse-turn-on
  "Parse a TurnOn protobuf message to a map."
  [^cmd.DayCamGlassHeater.JonSharedCmdDayCamGlassHeater$TurnOn proto]
  (cond-> {}))

(defn build-turn-off
  "Build a TurnOff protobuf message from a map."
  [m]
  (let [builder (cmd.DayCamGlassHeater.JonSharedCmdDayCamGlassHeater$TurnOff/newBuilder)]

    (.build builder)))

(defn parse-turn-off
  "Parse a TurnOff protobuf message to a map."
  [^cmd.DayCamGlassHeater.JonSharedCmdDayCamGlassHeater$TurnOff proto]
  (cond-> {}))

(defn build-get-meteo
  "Build a GetMeteo protobuf message from a map."
  [m]
  (let [builder (cmd.DayCamGlassHeater.JonSharedCmdDayCamGlassHeater$GetMeteo/newBuilder)]

    (.build builder)))

(defn parse-get-meteo
  "Parse a GetMeteo protobuf message to a map."
  [^cmd.DayCamGlassHeater.JonSharedCmdDayCamGlassHeater$GetMeteo proto]
  (cond-> {}))

(defn build-root-payload
  "Build the oneof payload for Root."
  [builder [field-key value]]
  (case field-key
    :refine-target (.setRefineTarget builder (build-refine-target value))
    (throw (ex-info "Unknown oneof field" {:field field-key :oneof ":cmd"}))))

(defn parse-root-payload
  "Parse the oneof payload from Root."
  [^cmd.Lira.JonSharedCmdLira$Root proto]
  (cond
    (.hasRefineTarget proto) {:refine-target (parse-refine-target (.getRefineTarget proto))}))

(defn build-root
  "Build a Root protobuf message from a map."
  [m]
  (let [builder (cmd.Lira.JonSharedCmdLira$Root/newBuilder)]

;; Handle oneof: cmd
    (when-let [cmd-field (first (filter (fn [[k v]] (#{:refine-target} k)) m))]
      (build-root-payload builder cmd-field))
    (.build builder)))

(defn parse-root
  "Parse a Root protobuf message to a map."
  [^cmd.Lira.JonSharedCmdLira$Root proto]
  (cond-> {}

;; Oneof payload
    true (merge (parse-root-payload proto))))

(defn build-refine-target
  "Build a Refine_target protobuf message from a map."
  [m]
  (let [builder (cmd.Lira.JonSharedCmdLira$Refine_target/newBuilder)]
    ;; Set regular fields
    (when (contains? m :target)
      (.setTarget builder (get m :target)))

    (.build builder)))

(defn parse-refine-target
  "Parse a Refine_target protobuf message to a map."
  [^cmd.Lira.JonSharedCmdLira$Refine_target proto]
  (cond-> {}
    ;; Regular fields
    (.hasTarget proto) (assoc :target (.getTarget proto))))

(defn build-jon-gui-data-lira-target
  "Build a JonGuiDataLiraTarget protobuf message from a map."
  [m]
  (let [builder (cmd.Lira.JonSharedCmdLira$JonGuiDataLiraTarget/newBuilder)]
    ;; Set regular fields
    (when (contains? m :timestamp)
      (.setTimestamp builder (get m :timestamp)))
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
    (when (contains? m :distance)
      (.setDistance builder (get m :distance)))
    (when (contains? m :uuid-part-1)
      (.setUuidPart1 builder (get m :uuid-part-1)))
    (when (contains? m :uuid-part-2)
      (.setUuidPart2 builder (get m :uuid-part-2)))
    (when (contains? m :uuid-part-3)
      (.setUuidPart3 builder (get m :uuid-part-3)))
    (when (contains? m :uuid-part-4)
      (.setUuidPart4 builder (get m :uuid-part-4)))

    (.build builder)))

(defn parse-jon-gui-data-lira-target
  "Parse a JonGuiDataLiraTarget protobuf message to a map."
  [^cmd.Lira.JonSharedCmdLira$JonGuiDataLiraTarget proto]
  (cond-> {}
    ;; Regular fields
    (.hasTimestamp proto) (assoc :timestamp (.getTimestamp proto))
    (.hasTargetLongitude proto) (assoc :target-longitude (.getTargetLongitude proto))
    (.hasTargetLatitude proto) (assoc :target-latitude (.getTargetLatitude proto))
    (.hasTargetAltitude proto) (assoc :target-altitude (.getTargetAltitude proto))
    (.hasTargetAzimuth proto) (assoc :target-azimuth (.getTargetAzimuth proto))
    (.hasTargetElevation proto) (assoc :target-elevation (.getTargetElevation proto))
    (.hasDistance proto) (assoc :distance (.getDistance proto))
    (.hasUuidPart1 proto) (assoc :uuid-part-1 (.getUuidPart1 proto))
    (.hasUuidPart2 proto) (assoc :uuid-part-2 (.getUuidPart2 proto))
    (.hasUuidPart3 proto) (assoc :uuid-part-3 (.getUuidPart3 proto))
    (.hasUuidPart4 proto) (assoc :uuid-part-4 (.getUuidPart4 proto))))

(defn build-root-payload
  "Build the oneof payload for Root."
  [builder [field-key value]]
  (case field-key
    :day-camera (.setDayCamera builder (build-root value))
    :heat-camera (.setHeatCamera builder (build-root value))
    :gps (.setGps builder (build-root value))
    :compass (.setCompass builder (build-root value))
    :lrf (.setLrf builder (build-root value))
    :lrf-calib (.setLrfCalib builder (build-root value))
    :rotary (.setRotary builder (build-root value))
    :osd (.setOsd builder (build-root value))
    :ping (.setPing builder (build-ping value))
    :noop (.setNoop builder (build-noop value))
    :frozen (.setFrozen builder (build-frozen value))
    :system (.setSystem builder (build-root value))
    :cv (.setCv builder (build-root value))
    :day-cam-glass-heater (.setDayCamGlassHeater builder (build-root value))
    :lira (.setLira builder (build-root value))
    (throw (ex-info "Unknown oneof field" {:field field-key :oneof ":payload"}))))

(defn parse-root-payload
  "Parse the oneof payload from Root."
  [^cmd.JonSharedCmd$Root proto]
  (cond
    (.hasDayCamera proto) {:day-camera (parse-root (.getDayCamera proto))}
    (.hasHeatCamera proto) {:heat-camera (parse-root (.getHeatCamera proto))}
    (.hasGps proto) {:gps (parse-root (.getGps proto))}
    (.hasCompass proto) {:compass (parse-root (.getCompass proto))}
    (.hasLrf proto) {:lrf (parse-root (.getLrf proto))}
    (.hasLrfCalib proto) {:lrf-calib (parse-root (.getLrfCalib proto))}
    (.hasRotary proto) {:rotary (parse-root (.getRotary proto))}
    (.hasOsd proto) {:osd (parse-root (.getOsd proto))}
    (.hasPing proto) {:ping (parse-ping (.getPing proto))}
    (.hasNoop proto) {:noop (parse-noop (.getNoop proto))}
    (.hasFrozen proto) {:frozen (parse-frozen (.getFrozen proto))}
    (.hasSystem proto) {:system (parse-root (.getSystem proto))}
    (.hasCv proto) {:cv (parse-root (.getCv proto))}
    (.hasDayCamGlassHeater proto) {:day-cam-glass-heater (parse-root (.getDayCamGlassHeater proto))}
    (.hasLira proto) {:lira (parse-root (.getLira proto))}))

(defn build-root
  "Build a Root protobuf message from a map."
  [m]
  (let [builder (cmd.JonSharedCmd$Root/newBuilder)]
    ;; Set regular fields
    (when (contains? m :protocol-version)
      (.setProtocolVersion builder (get m :protocol-version)))
    (when (contains? m :session-id)
      (.setSessionId builder (get m :session-id)))
    (when (contains? m :important)
      (.setImportant builder (get m :important)))
    (when (contains? m :from-cv-subsystem)
      (.setFromCvSubsystem builder (get m :from-cv-subsystem)))
    (when (contains? m :client-type)
      (.setClientType builder (get m :client-type)))

    ;; Handle oneof: payload
    (when-let [payload-field (first (filter (fn [[k v]] (#{:day-camera :heat-camera :gps :compass :lrf :lrf-calib :rotary :osd :ping :noop :frozen :system :cv :day-cam-glass-heater :lira} k)) m))]
      (build-root-payload builder payload-field))
    (.build builder)))

(defn parse-root
  "Parse a Root protobuf message to a map."
  [^cmd.JonSharedCmd$Root proto]
  (cond-> {}
    ;; Regular fields
    (.hasProtocolVersion proto) (assoc :protocol-version (.getProtocolVersion proto))
    (.hasSessionId proto) (assoc :session-id (.getSessionId proto))
    (.hasImportant proto) (assoc :important (.getImportant proto))
    (.hasFromCvSubsystem proto) (assoc :from-cv-subsystem (.getFromCvSubsystem proto))
    (.hasClientType proto) (assoc :client-type (.getClientType proto))

    ;; Oneof payload
    true (merge (parse-root-payload proto))))

(defn build-ping
  "Build a Ping protobuf message from a map."
  [m]
  (let [builder (cmd.JonSharedCmd$Ping/newBuilder)]

    (.build builder)))

(defn parse-ping
  "Parse a Ping protobuf message to a map."
  [^cmd.JonSharedCmd$Ping proto]
  (cond-> {}))

(defn build-noop
  "Build a Noop protobuf message from a map."
  [m]
  (let [builder (cmd.JonSharedCmd$Noop/newBuilder)]

    (.build builder)))

(defn parse-noop
  "Parse a Noop protobuf message to a map."
  [^cmd.JonSharedCmd$Noop proto]
  (cond-> {}))

(defn build-frozen
  "Build a Frozen protobuf message from a map."
  [m]
  (let [builder (cmd.JonSharedCmd$Frozen/newBuilder)]

    (.build builder)))

(defn parse-frozen
  "Parse a Frozen protobuf message to a map."
  [^cmd.JonSharedCmd$Frozen proto]
  (cond-> {}))