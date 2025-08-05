(ns potatoclient.proto.command
  "Generated protobuf functions."
  (:import
   [buf.validate.Validate
    cmd.CV.Cv
    cmd.Cmd
    cmd.Compass.Compass
    cmd.DayCamGlassHeater.DayCamGlassHeater
    cmd.DayCamera.DayCamera
    cmd.Gps.Gps
    cmd.HeatCamera.HeatCamera
    cmd.Lira.Lira
    cmd.Lrf.Lrf
    cmd.Lrf_calib.LrfCalib
    cmd.OSD.Osd
    cmd.RotaryPlatform.RotaryPlatform
    cmd.System.System
    google.protobuf.Protobuf
    ser.Ser]))

;; =============================================================================
;; Enums
;; =============================================================================

;; Enum: Edition
(def edition-values
  "Keyword to Java enum mapping for Edition."
  {:edition-unknown google.protobuf.Protobuf$Edition/EDITION_UNKNOWN
   :edition-legacy google.protobuf.Protobuf$Edition/EDITION_LEGACY
   :edition-proto-2 google.protobuf.Protobuf$Edition/EDITION_PROTO_2
   :edition-proto-3 google.protobuf.Protobuf$Edition/EDITION_PROTO_3
   :edition-2023 google.protobuf.Protobuf$Edition/EDITION_2023
   :edition-2024 google.protobuf.Protobuf$Edition/EDITION_2024
   :edition-1-test-only google.protobuf.Protobuf$Edition/EDITION_1_TEST_ONLY
   :edition-2-test-only google.protobuf.Protobuf$Edition/EDITION_2_TEST_ONLY
   :edition-99997-test-only google.protobuf.Protobuf$Edition/EDITION_99997_TEST_ONLY
   :edition-99998-test-only google.protobuf.Protobuf$Edition/EDITION_99998_TEST_ONLY
   :edition-99999-test-only google.protobuf.Protobuf$Edition/EDITION_99999_TEST_ONLY
   :edition-max google.protobuf.Protobuf$Edition/EDITION_MAX})

(def edition-keywords
  "Java enum to keyword mapping for Edition."
  {google.protobuf.Protobuf$Edition/EDITION_UNKNOWN :edition-unknown
   google.protobuf.Protobuf$Edition/EDITION_LEGACY :edition-legacy
   google.protobuf.Protobuf$Edition/EDITION_PROTO_2 :edition-proto-2
   google.protobuf.Protobuf$Edition/EDITION_PROTO_3 :edition-proto-3
   google.protobuf.Protobuf$Edition/EDITION_2023 :edition-2023
   google.protobuf.Protobuf$Edition/EDITION_2024 :edition-2024
   google.protobuf.Protobuf$Edition/EDITION_1_TEST_ONLY :edition-1-test-only
   google.protobuf.Protobuf$Edition/EDITION_2_TEST_ONLY :edition-2-test-only
   google.protobuf.Protobuf$Edition/EDITION_99997_TEST_ONLY :edition-99997-test-only
   google.protobuf.Protobuf$Edition/EDITION_99998_TEST_ONLY :edition-99998-test-only
   google.protobuf.Protobuf$Edition/EDITION_99999_TEST_ONLY :edition-99999-test-only
   google.protobuf.Protobuf$Edition/EDITION_MAX :edition-max})

;; Enum: SymbolVisibility
(def symbol-visibility-values
  "Keyword to Java enum mapping for SymbolVisibility."
  {:visibility-unset google.protobuf.Protobuf$SymbolVisibility/VISIBILITY_UNSET
   :visibility-local google.protobuf.Protobuf$SymbolVisibility/VISIBILITY_LOCAL
   :visibility-export google.protobuf.Protobuf$SymbolVisibility/VISIBILITY_EXPORT})

(def symbol-visibility-keywords
  "Java enum to keyword mapping for SymbolVisibility."
  {google.protobuf.Protobuf$SymbolVisibility/VISIBILITY_UNSET :visibility-unset
   google.protobuf.Protobuf$SymbolVisibility/VISIBILITY_LOCAL :visibility-local
   google.protobuf.Protobuf$SymbolVisibility/VISIBILITY_EXPORT :visibility-export})

;; Enum: Ignore
(def ignore-values
  "Keyword to Java enum mapping for Ignore."
  {:ignore-unspecified buf.validate.Validate$Ignore/IGNORE_UNSPECIFIED
   :ignore-if-zero-value buf.validate.Validate$Ignore/IGNORE_IF_ZERO_VALUE
   :ignore-always buf.validate.Validate$Ignore/IGNORE_ALWAYS})

(def ignore-keywords
  "Java enum to keyword mapping for Ignore."
  {buf.validate.Validate$Ignore/IGNORE_UNSPECIFIED :ignore-unspecified
   buf.validate.Validate$Ignore/IGNORE_IF_ZERO_VALUE :ignore-if-zero-value
   buf.validate.Validate$Ignore/IGNORE_ALWAYS :ignore-always})

;; Enum: KnownRegex
(def known-regex-values
  "Keyword to Java enum mapping for KnownRegex."
  {:known-regex-unspecified buf.validate.Validate$KnownRegex/KNOWN_REGEX_UNSPECIFIED
   :known-regex-http-header-name buf.validate.Validate$KnownRegex/KNOWN_REGEX_HTTP_HEADER_NAME
   :known-regex-http-header-value buf.validate.Validate$KnownRegex/KNOWN_REGEX_HTTP_HEADER_VALUE})

(def known-regex-keywords
  "Java enum to keyword mapping for KnownRegex."
  {buf.validate.Validate$KnownRegex/KNOWN_REGEX_UNSPECIFIED :known-regex-unspecified
   buf.validate.Validate$KnownRegex/KNOWN_REGEX_HTTP_HEADER_NAME :known-regex-http-header-name
   buf.validate.Validate$KnownRegex/KNOWN_REGEX_HTTP_HEADER_VALUE :known-regex-http-header-value})

;; Enum: JonGuiDataVideoChannelHeatFilters
(def jon-gui-data-video-channel-heat-filters-values
  "Keyword to Java enum mapping for JonGuiDataVideoChannelHeatFilters."
  {:jon-gui-data-video-channel-heat-filter-unspecified ser.Ser$JonGuiDataVideoChannelHeatFilters/JON_GUI_DATA_VIDEO_CHANNEL_HEAT_FILTER_UNSPECIFIED
   :jon-gui-data-video-channel-heat-filter-hot-white ser.Ser$JonGuiDataVideoChannelHeatFilters/JON_GUI_DATA_VIDEO_CHANNEL_HEAT_FILTER_HOT_WHITE
   :jon-gui-data-video-channel-heat-filter-hot-black ser.Ser$JonGuiDataVideoChannelHeatFilters/JON_GUI_DATA_VIDEO_CHANNEL_HEAT_FILTER_HOT_BLACK
   :jon-gui-data-video-channel-heat-filter-sepia ser.Ser$JonGuiDataVideoChannelHeatFilters/JON_GUI_DATA_VIDEO_CHANNEL_HEAT_FILTER_SEPIA
   :jon-gui-data-video-channel-heat-filter-sepia-inverse ser.Ser$JonGuiDataVideoChannelHeatFilters/JON_GUI_DATA_VIDEO_CHANNEL_HEAT_FILTER_SEPIA_INVERSE})

(def jon-gui-data-video-channel-heat-filters-keywords
  "Java enum to keyword mapping for JonGuiDataVideoChannelHeatFilters."
  {ser.Ser$JonGuiDataVideoChannelHeatFilters/JON_GUI_DATA_VIDEO_CHANNEL_HEAT_FILTER_UNSPECIFIED :jon-gui-data-video-channel-heat-filter-unspecified
   ser.Ser$JonGuiDataVideoChannelHeatFilters/JON_GUI_DATA_VIDEO_CHANNEL_HEAT_FILTER_HOT_WHITE :jon-gui-data-video-channel-heat-filter-hot-white
   ser.Ser$JonGuiDataVideoChannelHeatFilters/JON_GUI_DATA_VIDEO_CHANNEL_HEAT_FILTER_HOT_BLACK :jon-gui-data-video-channel-heat-filter-hot-black
   ser.Ser$JonGuiDataVideoChannelHeatFilters/JON_GUI_DATA_VIDEO_CHANNEL_HEAT_FILTER_SEPIA :jon-gui-data-video-channel-heat-filter-sepia
   ser.Ser$JonGuiDataVideoChannelHeatFilters/JON_GUI_DATA_VIDEO_CHANNEL_HEAT_FILTER_SEPIA_INVERSE :jon-gui-data-video-channel-heat-filter-sepia-inverse})

;; Enum: JonGuiDataVideoChannelHeatAGCModes
(def jon-gui-data-video-channel-heat-agc-modes-values
  "Keyword to Java enum mapping for JonGuiDataVideoChannelHeatAGCModes."
  {:jon-gui-data-video-channel-heat-agc-mode-unspecified ser.Ser$JonGuiDataVideoChannelHeatAGCModes/JON_GUI_DATA_VIDEO_CHANNEL_HEAT_AGC_MODE_UNSPECIFIED
   :jon-gui-data-video-channel-heat-agc-mode-1 ser.Ser$JonGuiDataVideoChannelHeatAGCModes/JON_GUI_DATA_VIDEO_CHANNEL_HEAT_AGC_MODE_1
   :jon-gui-data-video-channel-heat-agc-mode-2 ser.Ser$JonGuiDataVideoChannelHeatAGCModes/JON_GUI_DATA_VIDEO_CHANNEL_HEAT_AGC_MODE_2
   :jon-gui-data-video-channel-heat-agc-mode-3 ser.Ser$JonGuiDataVideoChannelHeatAGCModes/JON_GUI_DATA_VIDEO_CHANNEL_HEAT_AGC_MODE_3})

(def jon-gui-data-video-channel-heat-agc-modes-keywords
  "Java enum to keyword mapping for JonGuiDataVideoChannelHeatAGCModes."
  {ser.Ser$JonGuiDataVideoChannelHeatAGCModes/JON_GUI_DATA_VIDEO_CHANNEL_HEAT_AGC_MODE_UNSPECIFIED :jon-gui-data-video-channel-heat-agc-mode-unspecified
   ser.Ser$JonGuiDataVideoChannelHeatAGCModes/JON_GUI_DATA_VIDEO_CHANNEL_HEAT_AGC_MODE_1 :jon-gui-data-video-channel-heat-agc-mode-1
   ser.Ser$JonGuiDataVideoChannelHeatAGCModes/JON_GUI_DATA_VIDEO_CHANNEL_HEAT_AGC_MODE_2 :jon-gui-data-video-channel-heat-agc-mode-2
   ser.Ser$JonGuiDataVideoChannelHeatAGCModes/JON_GUI_DATA_VIDEO_CHANNEL_HEAT_AGC_MODE_3 :jon-gui-data-video-channel-heat-agc-mode-3})

;; Enum: JonGuiDataGpsUnits
(def jon-gui-data-gps-units-values
  "Keyword to Java enum mapping for JonGuiDataGpsUnits."
  {:jon-gui-data-gps-units-unspecified ser.Ser$JonGuiDataGpsUnits/JON_GUI_DATA_GPS_UNITS_UNSPECIFIED
   :jon-gui-data-gps-units-decimal-degrees ser.Ser$JonGuiDataGpsUnits/JON_GUI_DATA_GPS_UNITS_DECIMAL_DEGREES
   :jon-gui-data-gps-units-degrees-minutes-seconds ser.Ser$JonGuiDataGpsUnits/JON_GUI_DATA_GPS_UNITS_DEGREES_MINUTES_SECONDS
   :jon-gui-data-gps-units-degrees-decimal-minutes ser.Ser$JonGuiDataGpsUnits/JON_GUI_DATA_GPS_UNITS_DEGREES_DECIMAL_MINUTES})

(def jon-gui-data-gps-units-keywords
  "Java enum to keyword mapping for JonGuiDataGpsUnits."
  {ser.Ser$JonGuiDataGpsUnits/JON_GUI_DATA_GPS_UNITS_UNSPECIFIED :jon-gui-data-gps-units-unspecified
   ser.Ser$JonGuiDataGpsUnits/JON_GUI_DATA_GPS_UNITS_DECIMAL_DEGREES :jon-gui-data-gps-units-decimal-degrees
   ser.Ser$JonGuiDataGpsUnits/JON_GUI_DATA_GPS_UNITS_DEGREES_MINUTES_SECONDS :jon-gui-data-gps-units-degrees-minutes-seconds
   ser.Ser$JonGuiDataGpsUnits/JON_GUI_DATA_GPS_UNITS_DEGREES_DECIMAL_MINUTES :jon-gui-data-gps-units-degrees-decimal-minutes})

;; Enum: JonGuiDataGpsFixType
(def jon-gui-data-gps-fix-type-values
  "Keyword to Java enum mapping for JonGuiDataGpsFixType."
  {:jon-gui-data-gps-fix-type-unspecified ser.Ser$JonGuiDataGpsFixType/JON_GUI_DATA_GPS_FIX_TYPE_UNSPECIFIED
   :jon-gui-data-gps-fix-type-none ser.Ser$JonGuiDataGpsFixType/JON_GUI_DATA_GPS_FIX_TYPE_NONE
   :jon-gui-data-gps-fix-type-1-d ser.Ser$JonGuiDataGpsFixType/JON_GUI_DATA_GPS_FIX_TYPE_1_D
   :jon-gui-data-gps-fix-type-2-d ser.Ser$JonGuiDataGpsFixType/JON_GUI_DATA_GPS_FIX_TYPE_2_D
   :jon-gui-data-gps-fix-type-3-d ser.Ser$JonGuiDataGpsFixType/JON_GUI_DATA_GPS_FIX_TYPE_3_D
   :jon-gui-data-gps-fix-type-manual ser.Ser$JonGuiDataGpsFixType/JON_GUI_DATA_GPS_FIX_TYPE_MANUAL})

(def jon-gui-data-gps-fix-type-keywords
  "Java enum to keyword mapping for JonGuiDataGpsFixType."
  {ser.Ser$JonGuiDataGpsFixType/JON_GUI_DATA_GPS_FIX_TYPE_UNSPECIFIED :jon-gui-data-gps-fix-type-unspecified
   ser.Ser$JonGuiDataGpsFixType/JON_GUI_DATA_GPS_FIX_TYPE_NONE :jon-gui-data-gps-fix-type-none
   ser.Ser$JonGuiDataGpsFixType/JON_GUI_DATA_GPS_FIX_TYPE_1_D :jon-gui-data-gps-fix-type-1-d
   ser.Ser$JonGuiDataGpsFixType/JON_GUI_DATA_GPS_FIX_TYPE_2_D :jon-gui-data-gps-fix-type-2-d
   ser.Ser$JonGuiDataGpsFixType/JON_GUI_DATA_GPS_FIX_TYPE_3_D :jon-gui-data-gps-fix-type-3-d
   ser.Ser$JonGuiDataGpsFixType/JON_GUI_DATA_GPS_FIX_TYPE_MANUAL :jon-gui-data-gps-fix-type-manual})

;; Enum: JonGuiDataCompassUnits
(def jon-gui-data-compass-units-values
  "Keyword to Java enum mapping for JonGuiDataCompassUnits."
  {:jon-gui-data-compass-units-unspecified ser.Ser$JonGuiDataCompassUnits/JON_GUI_DATA_COMPASS_UNITS_UNSPECIFIED
   :jon-gui-data-compass-units-degrees ser.Ser$JonGuiDataCompassUnits/JON_GUI_DATA_COMPASS_UNITS_DEGREES
   :jon-gui-data-compass-units-mils ser.Ser$JonGuiDataCompassUnits/JON_GUI_DATA_COMPASS_UNITS_MILS
   :jon-gui-data-compass-units-grad ser.Ser$JonGuiDataCompassUnits/JON_GUI_DATA_COMPASS_UNITS_GRAD
   :jon-gui-data-compass-units-mrad ser.Ser$JonGuiDataCompassUnits/JON_GUI_DATA_COMPASS_UNITS_MRAD})

(def jon-gui-data-compass-units-keywords
  "Java enum to keyword mapping for JonGuiDataCompassUnits."
  {ser.Ser$JonGuiDataCompassUnits/JON_GUI_DATA_COMPASS_UNITS_UNSPECIFIED :jon-gui-data-compass-units-unspecified
   ser.Ser$JonGuiDataCompassUnits/JON_GUI_DATA_COMPASS_UNITS_DEGREES :jon-gui-data-compass-units-degrees
   ser.Ser$JonGuiDataCompassUnits/JON_GUI_DATA_COMPASS_UNITS_MILS :jon-gui-data-compass-units-mils
   ser.Ser$JonGuiDataCompassUnits/JON_GUI_DATA_COMPASS_UNITS_GRAD :jon-gui-data-compass-units-grad
   ser.Ser$JonGuiDataCompassUnits/JON_GUI_DATA_COMPASS_UNITS_MRAD :jon-gui-data-compass-units-mrad})

;; Enum: JonGuiDataAccumulatorStateIdx
(def jon-gui-data-accumulator-state-idx-values
  "Keyword to Java enum mapping for JonGuiDataAccumulatorStateIdx."
  {:jon-gui-data-accumulator-state-unspecified ser.Ser$JonGuiDataAccumulatorStateIdx/JON_GUI_DATA_ACCUMULATOR_STATE_UNSPECIFIED
   :jon-gui-data-accumulator-state-unknown ser.Ser$JonGuiDataAccumulatorStateIdx/JON_GUI_DATA_ACCUMULATOR_STATE_UNKNOWN
   :jon-gui-data-accumulator-state-empty ser.Ser$JonGuiDataAccumulatorStateIdx/JON_GUI_DATA_ACCUMULATOR_STATE_EMPTY
   :jon-gui-data-accumulator-state-1 ser.Ser$JonGuiDataAccumulatorStateIdx/JON_GUI_DATA_ACCUMULATOR_STATE_1
   :jon-gui-data-accumulator-state-2 ser.Ser$JonGuiDataAccumulatorStateIdx/JON_GUI_DATA_ACCUMULATOR_STATE_2
   :jon-gui-data-accumulator-state-3 ser.Ser$JonGuiDataAccumulatorStateIdx/JON_GUI_DATA_ACCUMULATOR_STATE_3
   :jon-gui-data-accumulator-state-4 ser.Ser$JonGuiDataAccumulatorStateIdx/JON_GUI_DATA_ACCUMULATOR_STATE_4
   :jon-gui-data-accumulator-state-5 ser.Ser$JonGuiDataAccumulatorStateIdx/JON_GUI_DATA_ACCUMULATOR_STATE_5
   :jon-gui-data-accumulator-state-6 ser.Ser$JonGuiDataAccumulatorStateIdx/JON_GUI_DATA_ACCUMULATOR_STATE_6
   :jon-gui-data-accumulator-state-full ser.Ser$JonGuiDataAccumulatorStateIdx/JON_GUI_DATA_ACCUMULATOR_STATE_FULL
   :jon-gui-data-accumulator-state-charging ser.Ser$JonGuiDataAccumulatorStateIdx/JON_GUI_DATA_ACCUMULATOR_STATE_CHARGING})

(def jon-gui-data-accumulator-state-idx-keywords
  "Java enum to keyword mapping for JonGuiDataAccumulatorStateIdx."
  {ser.Ser$JonGuiDataAccumulatorStateIdx/JON_GUI_DATA_ACCUMULATOR_STATE_UNSPECIFIED :jon-gui-data-accumulator-state-unspecified
   ser.Ser$JonGuiDataAccumulatorStateIdx/JON_GUI_DATA_ACCUMULATOR_STATE_UNKNOWN :jon-gui-data-accumulator-state-unknown
   ser.Ser$JonGuiDataAccumulatorStateIdx/JON_GUI_DATA_ACCUMULATOR_STATE_EMPTY :jon-gui-data-accumulator-state-empty
   ser.Ser$JonGuiDataAccumulatorStateIdx/JON_GUI_DATA_ACCUMULATOR_STATE_1 :jon-gui-data-accumulator-state-1
   ser.Ser$JonGuiDataAccumulatorStateIdx/JON_GUI_DATA_ACCUMULATOR_STATE_2 :jon-gui-data-accumulator-state-2
   ser.Ser$JonGuiDataAccumulatorStateIdx/JON_GUI_DATA_ACCUMULATOR_STATE_3 :jon-gui-data-accumulator-state-3
   ser.Ser$JonGuiDataAccumulatorStateIdx/JON_GUI_DATA_ACCUMULATOR_STATE_4 :jon-gui-data-accumulator-state-4
   ser.Ser$JonGuiDataAccumulatorStateIdx/JON_GUI_DATA_ACCUMULATOR_STATE_5 :jon-gui-data-accumulator-state-5
   ser.Ser$JonGuiDataAccumulatorStateIdx/JON_GUI_DATA_ACCUMULATOR_STATE_6 :jon-gui-data-accumulator-state-6
   ser.Ser$JonGuiDataAccumulatorStateIdx/JON_GUI_DATA_ACCUMULATOR_STATE_FULL :jon-gui-data-accumulator-state-full
   ser.Ser$JonGuiDataAccumulatorStateIdx/JON_GUI_DATA_ACCUMULATOR_STATE_CHARGING :jon-gui-data-accumulator-state-charging})

;; Enum: JonGuiDataTimeFormats
(def jon-gui-data-time-formats-values
  "Keyword to Java enum mapping for JonGuiDataTimeFormats."
  {:jon-gui-data-time-format-unspecified ser.Ser$JonGuiDataTimeFormats/JON_GUI_DATA_TIME_FORMAT_UNSPECIFIED
   :jon-gui-data-time-format-h-m-s ser.Ser$JonGuiDataTimeFormats/JON_GUI_DATA_TIME_FORMAT_H_M_S
   :jon-gui-data-time-format-y-m-d-h-m-s ser.Ser$JonGuiDataTimeFormats/JON_GUI_DATA_TIME_FORMAT_Y_M_D_H_M_S})

(def jon-gui-data-time-formats-keywords
  "Java enum to keyword mapping for JonGuiDataTimeFormats."
  {ser.Ser$JonGuiDataTimeFormats/JON_GUI_DATA_TIME_FORMAT_UNSPECIFIED :jon-gui-data-time-format-unspecified
   ser.Ser$JonGuiDataTimeFormats/JON_GUI_DATA_TIME_FORMAT_H_M_S :jon-gui-data-time-format-h-m-s
   ser.Ser$JonGuiDataTimeFormats/JON_GUI_DATA_TIME_FORMAT_Y_M_D_H_M_S :jon-gui-data-time-format-y-m-d-h-m-s})

;; Enum: JonGuiDataRotaryDirection
(def jon-gui-data-rotary-direction-values
  "Keyword to Java enum mapping for JonGuiDataRotaryDirection."
  {:jon-gui-data-rotary-direction-unspecified ser.Ser$JonGuiDataRotaryDirection/JON_GUI_DATA_ROTARY_DIRECTION_UNSPECIFIED
   :jon-gui-data-rotary-direction-clockwise ser.Ser$JonGuiDataRotaryDirection/JON_GUI_DATA_ROTARY_DIRECTION_CLOCKWISE
   :jon-gui-data-rotary-direction-counter-clockwise ser.Ser$JonGuiDataRotaryDirection/JON_GUI_DATA_ROTARY_DIRECTION_COUNTER_CLOCKWISE})

(def jon-gui-data-rotary-direction-keywords
  "Java enum to keyword mapping for JonGuiDataRotaryDirection."
  {ser.Ser$JonGuiDataRotaryDirection/JON_GUI_DATA_ROTARY_DIRECTION_UNSPECIFIED :jon-gui-data-rotary-direction-unspecified
   ser.Ser$JonGuiDataRotaryDirection/JON_GUI_DATA_ROTARY_DIRECTION_CLOCKWISE :jon-gui-data-rotary-direction-clockwise
   ser.Ser$JonGuiDataRotaryDirection/JON_GUI_DATA_ROTARY_DIRECTION_COUNTER_CLOCKWISE :jon-gui-data-rotary-direction-counter-clockwise})

;; Enum: JonGuiDataLrfScanModes
(def jon-gui-data-lrf-scan-modes-values
  "Keyword to Java enum mapping for JonGuiDataLrfScanModes."
  {:jon-gui-data-lrf-scan-mode-unspecified ser.Ser$JonGuiDataLrfScanModes/JON_GUI_DATA_LRF_SCAN_MODE_UNSPECIFIED
   :jon-gui-data-lrf-scan-mode-1-hz-continuous ser.Ser$JonGuiDataLrfScanModes/JON_GUI_DATA_LRF_SCAN_MODE_1_HZ_CONTINUOUS
   :jon-gui-data-lrf-scan-mode-4-hz-continuous ser.Ser$JonGuiDataLrfScanModes/JON_GUI_DATA_LRF_SCAN_MODE_4_HZ_CONTINUOUS
   :jon-gui-data-lrf-scan-mode-10-hz-continuous ser.Ser$JonGuiDataLrfScanModes/JON_GUI_DATA_LRF_SCAN_MODE_10_HZ_CONTINUOUS
   :jon-gui-data-lrf-scan-mode-20-hz-continuous ser.Ser$JonGuiDataLrfScanModes/JON_GUI_DATA_LRF_SCAN_MODE_20_HZ_CONTINUOUS
   :jon-gui-data-lrf-scan-mode-100-hz-continuous ser.Ser$JonGuiDataLrfScanModes/JON_GUI_DATA_LRF_SCAN_MODE_100_HZ_CONTINUOUS
   :jon-gui-data-lrf-scan-mode-200-hz-continuous ser.Ser$JonGuiDataLrfScanModes/JON_GUI_DATA_LRF_SCAN_MODE_200_HZ_CONTINUOUS})

(def jon-gui-data-lrf-scan-modes-keywords
  "Java enum to keyword mapping for JonGuiDataLrfScanModes."
  {ser.Ser$JonGuiDataLrfScanModes/JON_GUI_DATA_LRF_SCAN_MODE_UNSPECIFIED :jon-gui-data-lrf-scan-mode-unspecified
   ser.Ser$JonGuiDataLrfScanModes/JON_GUI_DATA_LRF_SCAN_MODE_1_HZ_CONTINUOUS :jon-gui-data-lrf-scan-mode-1-hz-continuous
   ser.Ser$JonGuiDataLrfScanModes/JON_GUI_DATA_LRF_SCAN_MODE_4_HZ_CONTINUOUS :jon-gui-data-lrf-scan-mode-4-hz-continuous
   ser.Ser$JonGuiDataLrfScanModes/JON_GUI_DATA_LRF_SCAN_MODE_10_HZ_CONTINUOUS :jon-gui-data-lrf-scan-mode-10-hz-continuous
   ser.Ser$JonGuiDataLrfScanModes/JON_GUI_DATA_LRF_SCAN_MODE_20_HZ_CONTINUOUS :jon-gui-data-lrf-scan-mode-20-hz-continuous
   ser.Ser$JonGuiDataLrfScanModes/JON_GUI_DATA_LRF_SCAN_MODE_100_HZ_CONTINUOUS :jon-gui-data-lrf-scan-mode-100-hz-continuous
   ser.Ser$JonGuiDataLrfScanModes/JON_GUI_DATA_LRF_SCAN_MODE_200_HZ_CONTINUOUS :jon-gui-data-lrf-scan-mode-200-hz-continuous})

;; Enum: JonGuiDatatLrfLaserPointerModes
(def jon-gui-datat-lrf-laser-pointer-modes-values
  "Keyword to Java enum mapping for JonGuiDatatLrfLaserPointerModes."
  {:jon-gui-data-lrf-laser-pointer-mode-unspecified ser.Ser$JonGuiDatatLrfLaserPointerModes/JON_GUI_DATA_LRF_LASER_POINTER_MODE_UNSPECIFIED
   :jon-gui-data-lrf-laser-pointer-mode-off ser.Ser$JonGuiDatatLrfLaserPointerModes/JON_GUI_DATA_LRF_LASER_POINTER_MODE_OFF
   :jon-gui-data-lrf-laser-pointer-mode-on-1 ser.Ser$JonGuiDatatLrfLaserPointerModes/JON_GUI_DATA_LRF_LASER_POINTER_MODE_ON_1
   :jon-gui-data-lrf-laser-pointer-mode-on-2 ser.Ser$JonGuiDatatLrfLaserPointerModes/JON_GUI_DATA_LRF_LASER_POINTER_MODE_ON_2})

(def jon-gui-datat-lrf-laser-pointer-modes-keywords
  "Java enum to keyword mapping for JonGuiDatatLrfLaserPointerModes."
  {ser.Ser$JonGuiDatatLrfLaserPointerModes/JON_GUI_DATA_LRF_LASER_POINTER_MODE_UNSPECIFIED :jon-gui-data-lrf-laser-pointer-mode-unspecified
   ser.Ser$JonGuiDatatLrfLaserPointerModes/JON_GUI_DATA_LRF_LASER_POINTER_MODE_OFF :jon-gui-data-lrf-laser-pointer-mode-off
   ser.Ser$JonGuiDatatLrfLaserPointerModes/JON_GUI_DATA_LRF_LASER_POINTER_MODE_ON_1 :jon-gui-data-lrf-laser-pointer-mode-on-1
   ser.Ser$JonGuiDatatLrfLaserPointerModes/JON_GUI_DATA_LRF_LASER_POINTER_MODE_ON_2 :jon-gui-data-lrf-laser-pointer-mode-on-2})

;; Enum: JonGuiDataCompassCalibrateStatus
(def jon-gui-data-compass-calibrate-status-values
  "Keyword to Java enum mapping for JonGuiDataCompassCalibrateStatus."
  {:jon-gui-data-compass-calibrate-status-unspecified ser.Ser$JonGuiDataCompassCalibrateStatus/JON_GUI_DATA_COMPASS_CALIBRATE_STATUS_UNSPECIFIED
   :jon-gui-data-compass-calibrate-status-not-calibrating ser.Ser$JonGuiDataCompassCalibrateStatus/JON_GUI_DATA_COMPASS_CALIBRATE_STATUS_NOT_CALIBRATING
   :jon-gui-data-compass-calibrate-status-calibrating-short ser.Ser$JonGuiDataCompassCalibrateStatus/JON_GUI_DATA_COMPASS_CALIBRATE_STATUS_CALIBRATING_SHORT
   :jon-gui-data-compass-calibrate-status-calibrating-long ser.Ser$JonGuiDataCompassCalibrateStatus/JON_GUI_DATA_COMPASS_CALIBRATE_STATUS_CALIBRATING_LONG
   :jon-gui-data-compass-calibrate-status-finished ser.Ser$JonGuiDataCompassCalibrateStatus/JON_GUI_DATA_COMPASS_CALIBRATE_STATUS_FINISHED
   :jon-gui-data-compass-calibrate-status-error ser.Ser$JonGuiDataCompassCalibrateStatus/JON_GUI_DATA_COMPASS_CALIBRATE_STATUS_ERROR})

(def jon-gui-data-compass-calibrate-status-keywords
  "Java enum to keyword mapping for JonGuiDataCompassCalibrateStatus."
  {ser.Ser$JonGuiDataCompassCalibrateStatus/JON_GUI_DATA_COMPASS_CALIBRATE_STATUS_UNSPECIFIED :jon-gui-data-compass-calibrate-status-unspecified
   ser.Ser$JonGuiDataCompassCalibrateStatus/JON_GUI_DATA_COMPASS_CALIBRATE_STATUS_NOT_CALIBRATING :jon-gui-data-compass-calibrate-status-not-calibrating
   ser.Ser$JonGuiDataCompassCalibrateStatus/JON_GUI_DATA_COMPASS_CALIBRATE_STATUS_CALIBRATING_SHORT :jon-gui-data-compass-calibrate-status-calibrating-short
   ser.Ser$JonGuiDataCompassCalibrateStatus/JON_GUI_DATA_COMPASS_CALIBRATE_STATUS_CALIBRATING_LONG :jon-gui-data-compass-calibrate-status-calibrating-long
   ser.Ser$JonGuiDataCompassCalibrateStatus/JON_GUI_DATA_COMPASS_CALIBRATE_STATUS_FINISHED :jon-gui-data-compass-calibrate-status-finished
   ser.Ser$JonGuiDataCompassCalibrateStatus/JON_GUI_DATA_COMPASS_CALIBRATE_STATUS_ERROR :jon-gui-data-compass-calibrate-status-error})

;; Enum: JonGuiDataRotaryMode
(def jon-gui-data-rotary-mode-values
  "Keyword to Java enum mapping for JonGuiDataRotaryMode."
  {:jon-gui-data-rotary-mode-unspecified ser.Ser$JonGuiDataRotaryMode/JON_GUI_DATA_ROTARY_MODE_UNSPECIFIED
   :jon-gui-data-rotary-mode-initialization ser.Ser$JonGuiDataRotaryMode/JON_GUI_DATA_ROTARY_MODE_INITIALIZATION
   :jon-gui-data-rotary-mode-speed ser.Ser$JonGuiDataRotaryMode/JON_GUI_DATA_ROTARY_MODE_SPEED
   :jon-gui-data-rotary-mode-position ser.Ser$JonGuiDataRotaryMode/JON_GUI_DATA_ROTARY_MODE_POSITION
   :jon-gui-data-rotary-mode-stabilization ser.Ser$JonGuiDataRotaryMode/JON_GUI_DATA_ROTARY_MODE_STABILIZATION
   :jon-gui-data-rotary-mode-targeting ser.Ser$JonGuiDataRotaryMode/JON_GUI_DATA_ROTARY_MODE_TARGETING
   :jon-gui-data-rotary-mode-video-tracker ser.Ser$JonGuiDataRotaryMode/JON_GUI_DATA_ROTARY_MODE_VIDEO_TRACKER})

(def jon-gui-data-rotary-mode-keywords
  "Java enum to keyword mapping for JonGuiDataRotaryMode."
  {ser.Ser$JonGuiDataRotaryMode/JON_GUI_DATA_ROTARY_MODE_UNSPECIFIED :jon-gui-data-rotary-mode-unspecified
   ser.Ser$JonGuiDataRotaryMode/JON_GUI_DATA_ROTARY_MODE_INITIALIZATION :jon-gui-data-rotary-mode-initialization
   ser.Ser$JonGuiDataRotaryMode/JON_GUI_DATA_ROTARY_MODE_SPEED :jon-gui-data-rotary-mode-speed
   ser.Ser$JonGuiDataRotaryMode/JON_GUI_DATA_ROTARY_MODE_POSITION :jon-gui-data-rotary-mode-position
   ser.Ser$JonGuiDataRotaryMode/JON_GUI_DATA_ROTARY_MODE_STABILIZATION :jon-gui-data-rotary-mode-stabilization
   ser.Ser$JonGuiDataRotaryMode/JON_GUI_DATA_ROTARY_MODE_TARGETING :jon-gui-data-rotary-mode-targeting
   ser.Ser$JonGuiDataRotaryMode/JON_GUI_DATA_ROTARY_MODE_VIDEO_TRACKER :jon-gui-data-rotary-mode-video-tracker})

;; Enum: JonGuiDataVideoChannel
(def jon-gui-data-video-channel-values
  "Keyword to Java enum mapping for JonGuiDataVideoChannel."
  {:jon-gui-data-video-channel-unspecified ser.Ser$JonGuiDataVideoChannel/JON_GUI_DATA_VIDEO_CHANNEL_UNSPECIFIED
   :jon-gui-data-video-channel-heat ser.Ser$JonGuiDataVideoChannel/JON_GUI_DATA_VIDEO_CHANNEL_HEAT
   :jon-gui-data-video-channel-day ser.Ser$JonGuiDataVideoChannel/JON_GUI_DATA_VIDEO_CHANNEL_DAY})

(def jon-gui-data-video-channel-keywords
  "Java enum to keyword mapping for JonGuiDataVideoChannel."
  {ser.Ser$JonGuiDataVideoChannel/JON_GUI_DATA_VIDEO_CHANNEL_UNSPECIFIED :jon-gui-data-video-channel-unspecified
   ser.Ser$JonGuiDataVideoChannel/JON_GUI_DATA_VIDEO_CHANNEL_HEAT :jon-gui-data-video-channel-heat
   ser.Ser$JonGuiDataVideoChannel/JON_GUI_DATA_VIDEO_CHANNEL_DAY :jon-gui-data-video-channel-day})

;; Enum: JonGuiDataRecOsdScreen
(def jon-gui-data-rec-osd-screen-values
  "Keyword to Java enum mapping for JonGuiDataRecOsdScreen."
  {:jon-gui-data-rec-osd-screen-unspecified ser.Ser$JonGuiDataRecOsdScreen/JON_GUI_DATA_REC_OSD_SCREEN_UNSPECIFIED
   :jon-gui-data-rec-osd-screen-main ser.Ser$JonGuiDataRecOsdScreen/JON_GUI_DATA_REC_OSD_SCREEN_MAIN
   :jon-gui-data-rec-osd-screen-lrf-measure ser.Ser$JonGuiDataRecOsdScreen/JON_GUI_DATA_REC_OSD_SCREEN_LRF_MEASURE
   :jon-gui-data-rec-osd-screen-lrf-result ser.Ser$JonGuiDataRecOsdScreen/JON_GUI_DATA_REC_OSD_SCREEN_LRF_RESULT
   :jon-gui-data-rec-osd-screen-lrf-result-simplified ser.Ser$JonGuiDataRecOsdScreen/JON_GUI_DATA_REC_OSD_SCREEN_LRF_RESULT_SIMPLIFIED})

(def jon-gui-data-rec-osd-screen-keywords
  "Java enum to keyword mapping for JonGuiDataRecOsdScreen."
  {ser.Ser$JonGuiDataRecOsdScreen/JON_GUI_DATA_REC_OSD_SCREEN_UNSPECIFIED :jon-gui-data-rec-osd-screen-unspecified
   ser.Ser$JonGuiDataRecOsdScreen/JON_GUI_DATA_REC_OSD_SCREEN_MAIN :jon-gui-data-rec-osd-screen-main
   ser.Ser$JonGuiDataRecOsdScreen/JON_GUI_DATA_REC_OSD_SCREEN_LRF_MEASURE :jon-gui-data-rec-osd-screen-lrf-measure
   ser.Ser$JonGuiDataRecOsdScreen/JON_GUI_DATA_REC_OSD_SCREEN_LRF_RESULT :jon-gui-data-rec-osd-screen-lrf-result
   ser.Ser$JonGuiDataRecOsdScreen/JON_GUI_DATA_REC_OSD_SCREEN_LRF_RESULT_SIMPLIFIED :jon-gui-data-rec-osd-screen-lrf-result-simplified})

;; Enum: JonGuiDataFxModeDay
(def jon-gui-data-fx-mode-day-values
  "Keyword to Java enum mapping for JonGuiDataFxModeDay."
  {:jon-gui-data-fx-mode-day-default ser.Ser$JonGuiDataFxModeDay/JON_GUI_DATA_FX_MODE_DAY_DEFAULT
   :jon-gui-data-fx-mode-day-a ser.Ser$JonGuiDataFxModeDay/JON_GUI_DATA_FX_MODE_DAY_A
   :jon-gui-data-fx-mode-day-b ser.Ser$JonGuiDataFxModeDay/JON_GUI_DATA_FX_MODE_DAY_B
   :jon-gui-data-fx-mode-day-c ser.Ser$JonGuiDataFxModeDay/JON_GUI_DATA_FX_MODE_DAY_C
   :jon-gui-data-fx-mode-day-d ser.Ser$JonGuiDataFxModeDay/JON_GUI_DATA_FX_MODE_DAY_D
   :jon-gui-data-fx-mode-day-e ser.Ser$JonGuiDataFxModeDay/JON_GUI_DATA_FX_MODE_DAY_E
   :jon-gui-data-fx-mode-day-f ser.Ser$JonGuiDataFxModeDay/JON_GUI_DATA_FX_MODE_DAY_F})

(def jon-gui-data-fx-mode-day-keywords
  "Java enum to keyword mapping for JonGuiDataFxModeDay."
  {ser.Ser$JonGuiDataFxModeDay/JON_GUI_DATA_FX_MODE_DAY_DEFAULT :jon-gui-data-fx-mode-day-default
   ser.Ser$JonGuiDataFxModeDay/JON_GUI_DATA_FX_MODE_DAY_A :jon-gui-data-fx-mode-day-a
   ser.Ser$JonGuiDataFxModeDay/JON_GUI_DATA_FX_MODE_DAY_B :jon-gui-data-fx-mode-day-b
   ser.Ser$JonGuiDataFxModeDay/JON_GUI_DATA_FX_MODE_DAY_C :jon-gui-data-fx-mode-day-c
   ser.Ser$JonGuiDataFxModeDay/JON_GUI_DATA_FX_MODE_DAY_D :jon-gui-data-fx-mode-day-d
   ser.Ser$JonGuiDataFxModeDay/JON_GUI_DATA_FX_MODE_DAY_E :jon-gui-data-fx-mode-day-e
   ser.Ser$JonGuiDataFxModeDay/JON_GUI_DATA_FX_MODE_DAY_F :jon-gui-data-fx-mode-day-f})

;; Enum: JonGuiDataFxModeHeat
(def jon-gui-data-fx-mode-heat-values
  "Keyword to Java enum mapping for JonGuiDataFxModeHeat."
  {:jon-gui-data-fx-mode-heat-default ser.Ser$JonGuiDataFxModeHeat/JON_GUI_DATA_FX_MODE_HEAT_DEFAULT
   :jon-gui-data-fx-mode-heat-a ser.Ser$JonGuiDataFxModeHeat/JON_GUI_DATA_FX_MODE_HEAT_A
   :jon-gui-data-fx-mode-heat-b ser.Ser$JonGuiDataFxModeHeat/JON_GUI_DATA_FX_MODE_HEAT_B
   :jon-gui-data-fx-mode-heat-c ser.Ser$JonGuiDataFxModeHeat/JON_GUI_DATA_FX_MODE_HEAT_C
   :jon-gui-data-fx-mode-heat-d ser.Ser$JonGuiDataFxModeHeat/JON_GUI_DATA_FX_MODE_HEAT_D
   :jon-gui-data-fx-mode-heat-e ser.Ser$JonGuiDataFxModeHeat/JON_GUI_DATA_FX_MODE_HEAT_E
   :jon-gui-data-fx-mode-heat-f ser.Ser$JonGuiDataFxModeHeat/JON_GUI_DATA_FX_MODE_HEAT_F})

(def jon-gui-data-fx-mode-heat-keywords
  "Java enum to keyword mapping for JonGuiDataFxModeHeat."
  {ser.Ser$JonGuiDataFxModeHeat/JON_GUI_DATA_FX_MODE_HEAT_DEFAULT :jon-gui-data-fx-mode-heat-default
   ser.Ser$JonGuiDataFxModeHeat/JON_GUI_DATA_FX_MODE_HEAT_A :jon-gui-data-fx-mode-heat-a
   ser.Ser$JonGuiDataFxModeHeat/JON_GUI_DATA_FX_MODE_HEAT_B :jon-gui-data-fx-mode-heat-b
   ser.Ser$JonGuiDataFxModeHeat/JON_GUI_DATA_FX_MODE_HEAT_C :jon-gui-data-fx-mode-heat-c
   ser.Ser$JonGuiDataFxModeHeat/JON_GUI_DATA_FX_MODE_HEAT_D :jon-gui-data-fx-mode-heat-d
   ser.Ser$JonGuiDataFxModeHeat/JON_GUI_DATA_FX_MODE_HEAT_E :jon-gui-data-fx-mode-heat-e
   ser.Ser$JonGuiDataFxModeHeat/JON_GUI_DATA_FX_MODE_HEAT_F :jon-gui-data-fx-mode-heat-f})

;; Enum: JonGuiDataSystemLocalizations
(def jon-gui-data-system-localizations-values
  "Keyword to Java enum mapping for JonGuiDataSystemLocalizations."
  {:jon-gui-data-system-localization-unspecified ser.Ser$JonGuiDataSystemLocalizations/JON_GUI_DATA_SYSTEM_LOCALIZATION_UNSPECIFIED
   :jon-gui-data-system-localization-en ser.Ser$JonGuiDataSystemLocalizations/JON_GUI_DATA_SYSTEM_LOCALIZATION_EN
   :jon-gui-data-system-localization-ua ser.Ser$JonGuiDataSystemLocalizations/JON_GUI_DATA_SYSTEM_LOCALIZATION_UA
   :jon-gui-data-system-localization-ar ser.Ser$JonGuiDataSystemLocalizations/JON_GUI_DATA_SYSTEM_LOCALIZATION_AR
   :jon-gui-data-system-localization-cs ser.Ser$JonGuiDataSystemLocalizations/JON_GUI_DATA_SYSTEM_LOCALIZATION_CS})

(def jon-gui-data-system-localizations-keywords
  "Java enum to keyword mapping for JonGuiDataSystemLocalizations."
  {ser.Ser$JonGuiDataSystemLocalizations/JON_GUI_DATA_SYSTEM_LOCALIZATION_UNSPECIFIED :jon-gui-data-system-localization-unspecified
   ser.Ser$JonGuiDataSystemLocalizations/JON_GUI_DATA_SYSTEM_LOCALIZATION_EN :jon-gui-data-system-localization-en
   ser.Ser$JonGuiDataSystemLocalizations/JON_GUI_DATA_SYSTEM_LOCALIZATION_UA :jon-gui-data-system-localization-ua
   ser.Ser$JonGuiDataSystemLocalizations/JON_GUI_DATA_SYSTEM_LOCALIZATION_AR :jon-gui-data-system-localization-ar
   ser.Ser$JonGuiDataSystemLocalizations/JON_GUI_DATA_SYSTEM_LOCALIZATION_CS :jon-gui-data-system-localization-cs})

;; Enum: JonGuiDataClientType
(def jon-gui-data-client-type-values
  "Keyword to Java enum mapping for JonGuiDataClientType."
  {:jon-gui-data-client-type-unspecified ser.Ser$JonGuiDataClientType/JON_GUI_DATA_CLIENT_TYPE_UNSPECIFIED
   :jon-gui-data-client-type-internal-cv ser.Ser$JonGuiDataClientType/JON_GUI_DATA_CLIENT_TYPE_INTERNAL_CV
   :jon-gui-data-client-type-local-network ser.Ser$JonGuiDataClientType/JON_GUI_DATA_CLIENT_TYPE_LOCAL_NETWORK
   :jon-gui-data-client-type-certificate-protected ser.Ser$JonGuiDataClientType/JON_GUI_DATA_CLIENT_TYPE_CERTIFICATE_PROTECTED
   :jon-gui-data-client-type-lira ser.Ser$JonGuiDataClientType/JON_GUI_DATA_CLIENT_TYPE_LIRA})

(def jon-gui-data-client-type-keywords
  "Java enum to keyword mapping for JonGuiDataClientType."
  {ser.Ser$JonGuiDataClientType/JON_GUI_DATA_CLIENT_TYPE_UNSPECIFIED :jon-gui-data-client-type-unspecified
   ser.Ser$JonGuiDataClientType/JON_GUI_DATA_CLIENT_TYPE_INTERNAL_CV :jon-gui-data-client-type-internal-cv
   ser.Ser$JonGuiDataClientType/JON_GUI_DATA_CLIENT_TYPE_LOCAL_NETWORK :jon-gui-data-client-type-local-network
   ser.Ser$JonGuiDataClientType/JON_GUI_DATA_CLIENT_TYPE_CERTIFICATE_PROTECTED :jon-gui-data-client-type-certificate-protected
   ser.Ser$JonGuiDataClientType/JON_GUI_DATA_CLIENT_TYPE_LIRA :jon-gui-data-client-type-lira})

;; =============================================================================
;; Builders and Parsers
;; =============================================================================

(defn build-file-descriptor-set
  "Build a FileDescriptorSet protobuf message from a map."
  [m]
  (let [builder (google.protobuf.Protobuf$FileDescriptorSet/newBuilder)]
    ;; Set regular fields
    (when (contains? m :file)
      (.addAllFile builder (get m :file)))

    (.build builder)))

(defn parse-file-descriptor-set
  "Parse a FileDescriptorSet protobuf message to a map."
  [^google.protobuf.Protobuf$FileDescriptorSet proto]
  (cond-> {}
    ;; Regular fields
    true (assoc :file (vec (.getFileList proto)))))

(defn build-file-descriptor-proto
  "Build a FileDescriptorProto protobuf message from a map."
  [m]
  (let [builder (google.protobuf.Protobuf$FileDescriptorProto/newBuilder)]
    ;; Set regular fields
    (when (contains? m :name)
      (.setName builder (get m :name)))
    (when (contains? m :package)
      (.setPackage builder (get m :package)))
    (when (contains? m :dependency)
      (.addAllDependency builder (get m :dependency)))
    (when (contains? m :public-dependency)
      (.addAllPublicDependency builder (get m :public-dependency)))
    (when (contains? m :weak-dependency)
      (.addAllWeakDependency builder (get m :weak-dependency)))
    (when (contains? m :option-dependency)
      (.addAllOptionDependency builder (get m :option-dependency)))
    (when (contains? m :message-type)
      (.addAllMessageType builder (get m :message-type)))
    (when (contains? m :enum-type)
      (.addAllEnumType builder (get m :enum-type)))
    (when (contains? m :service)
      (.addAllService builder (get m :service)))
    (when (contains? m :extension)
      (.addAllExtension builder (get m :extension)))
    (when (contains? m :options)
      (.setOptions builder (get m :options)))
    (when (contains? m :source-code-info)
      (.setSourceCodeInfo builder (get m :source-code-info)))
    (when (contains? m :syntax)
      (.setSyntax builder (get m :syntax)))
    (when (contains? m :edition)
      (.setEdition builder (get m :edition)))

    (.build builder)))

(defn parse-file-descriptor-proto
  "Parse a FileDescriptorProto protobuf message to a map."
  [^google.protobuf.Protobuf$FileDescriptorProto proto]
  (cond-> {}
    ;; Regular fields
    (.hasName proto) (assoc :name (.getName proto))
    (.hasPackage proto) (assoc :package (.getPackage proto))
    true (assoc :dependency (vec (.getDependencyList proto)))
    true (assoc :public-dependency (vec (.getPublicDependencyList proto)))
    true (assoc :weak-dependency (vec (.getWeakDependencyList proto)))
    true (assoc :option-dependency (vec (.getOptionDependencyList proto)))
    true (assoc :message-type (vec (.getMessageTypeList proto)))
    true (assoc :enum-type (vec (.getEnumTypeList proto)))
    true (assoc :service (vec (.getServiceList proto)))
    true (assoc :extension (vec (.getExtensionList proto)))
    (.hasOptions proto) (assoc :options (.getOptions proto))
    (.hasSourceCodeInfo proto) (assoc :source-code-info (.getSourceCodeInfo proto))
    (.hasSyntax proto) (assoc :syntax (.getSyntax proto))
    (.hasEdition proto) (assoc :edition (.getEdition proto))))

(defn build-descriptor-proto
  "Build a DescriptorProto protobuf message from a map."
  [m]
  (let [builder (google.protobuf.Protobuf$DescriptorProto/newBuilder)]
    ;; Set regular fields
    (when (contains? m :name)
      (.setName builder (get m :name)))
    (when (contains? m :field)
      (.addAllField builder (get m :field)))
    (when (contains? m :extension)
      (.addAllExtension builder (get m :extension)))
    (when (contains? m :nested-type)
      (.addAllNestedType builder (get m :nested-type)))
    (when (contains? m :enum-type)
      (.addAllEnumType builder (get m :enum-type)))
    (when (contains? m :extension-range)
      (.addAllExtensionRange builder (get m :extension-range)))
    (when (contains? m :oneof-decl)
      (.addAllOneofDecl builder (get m :oneof-decl)))
    (when (contains? m :options)
      (.setOptions builder (get m :options)))
    (when (contains? m :reserved-range)
      (.addAllReservedRange builder (get m :reserved-range)))
    (when (contains? m :reserved-name)
      (.addAllReservedName builder (get m :reserved-name)))
    (when (contains? m :visibility)
      (.setVisibility builder (get m :visibility)))

    (.build builder)))

(defn parse-descriptor-proto
  "Parse a DescriptorProto protobuf message to a map."
  [^google.protobuf.Protobuf$DescriptorProto proto]
  (cond-> {}
    ;; Regular fields
    (.hasName proto) (assoc :name (.getName proto))
    true (assoc :field (vec (.getFieldList proto)))
    true (assoc :extension (vec (.getExtensionList proto)))
    true (assoc :nested-type (vec (.getNestedTypeList proto)))
    true (assoc :enum-type (vec (.getEnumTypeList proto)))
    true (assoc :extension-range (vec (.getExtensionRangeList proto)))
    true (assoc :oneof-decl (vec (.getOneofDeclList proto)))
    (.hasOptions proto) (assoc :options (.getOptions proto))
    true (assoc :reserved-range (vec (.getReservedRangeList proto)))
    true (assoc :reserved-name (vec (.getReservedNameList proto)))
    (.hasVisibility proto) (assoc :visibility (.getVisibility proto))))

(defn build-extension-range-options
  "Build a ExtensionRangeOptions protobuf message from a map."
  [m]
  (let [builder (google.protobuf.Protobuf$ExtensionRangeOptions/newBuilder)]
    ;; Set regular fields
    (when (contains? m :uninterpreted-option)
      (.addAllUninterpretedOption builder (get m :uninterpreted-option)))
    (when (contains? m :declaration)
      (.addAllDeclaration builder (get m :declaration)))
    (when (contains? m :features)
      (.setFeatures builder (get m :features)))
    (when (contains? m :verification)
      (.setVerification builder (get m :verification)))

    (.build builder)))

(defn parse-extension-range-options
  "Parse a ExtensionRangeOptions protobuf message to a map."
  [^google.protobuf.Protobuf$ExtensionRangeOptions proto]
  (cond-> {}
    ;; Regular fields
    true (assoc :uninterpreted-option (vec (.getUninterpretedOptionList proto)))
    true (assoc :declaration (vec (.getDeclarationList proto)))
    (.hasFeatures proto) (assoc :features (.getFeatures proto))
    (.hasVerification proto) (assoc :verification (.getVerification proto))))

(defn build-field-descriptor-proto
  "Build a FieldDescriptorProto protobuf message from a map."
  [m]
  (let [builder (google.protobuf.Protobuf$FieldDescriptorProto/newBuilder)]
    ;; Set regular fields
    (when (contains? m :name)
      (.setName builder (get m :name)))
    (when (contains? m :number)
      (.setNumber builder (get m :number)))
    (when (contains? m :label)
      (.setLabel builder (get m :label)))
    (when (contains? m :type)
      (.setType builder (get m :type)))
    (when (contains? m :type-name)
      (.setTypeName builder (get m :type-name)))
    (when (contains? m :extendee)
      (.setExtendee builder (get m :extendee)))
    (when (contains? m :default-value)
      (.setDefaultValue builder (get m :default-value)))
    (when (contains? m :oneof-index)
      (.setOneofIndex builder (get m :oneof-index)))
    (when (contains? m :json-name)
      (.setJsonName builder (get m :json-name)))
    (when (contains? m :options)
      (.setOptions builder (get m :options)))
    (when (contains? m :proto-3-optional)
      (.setProto3Optional builder (get m :proto-3-optional)))

    (.build builder)))

(defn parse-field-descriptor-proto
  "Parse a FieldDescriptorProto protobuf message to a map."
  [^google.protobuf.Protobuf$FieldDescriptorProto proto]
  (cond-> {}
    ;; Regular fields
    (.hasName proto) (assoc :name (.getName proto))
    (.hasNumber proto) (assoc :number (.getNumber proto))
    (.hasLabel proto) (assoc :label (.getLabel proto))
    (.hasType proto) (assoc :type (.getType proto))
    (.hasTypeName proto) (assoc :type-name (.getTypeName proto))
    (.hasExtendee proto) (assoc :extendee (.getExtendee proto))
    (.hasDefaultValue proto) (assoc :default-value (.getDefaultValue proto))
    (.hasOneofIndex proto) (assoc :oneof-index (.getOneofIndex proto))
    (.hasJsonName proto) (assoc :json-name (.getJsonName proto))
    (.hasOptions proto) (assoc :options (.getOptions proto))
    (.hasProto3Optional proto) (assoc :proto-3-optional (.getProto3Optional proto))))

(defn build-oneof-descriptor-proto
  "Build a OneofDescriptorProto protobuf message from a map."
  [m]
  (let [builder (google.protobuf.Protobuf$OneofDescriptorProto/newBuilder)]
    ;; Set regular fields
    (when (contains? m :name)
      (.setName builder (get m :name)))
    (when (contains? m :options)
      (.setOptions builder (get m :options)))

    (.build builder)))

(defn parse-oneof-descriptor-proto
  "Parse a OneofDescriptorProto protobuf message to a map."
  [^google.protobuf.Protobuf$OneofDescriptorProto proto]
  (cond-> {}
    ;; Regular fields
    (.hasName proto) (assoc :name (.getName proto))
    (.hasOptions proto) (assoc :options (.getOptions proto))))

(defn build-enum-descriptor-proto
  "Build a EnumDescriptorProto protobuf message from a map."
  [m]
  (let [builder (google.protobuf.Protobuf$EnumDescriptorProto/newBuilder)]
    ;; Set regular fields
    (when (contains? m :name)
      (.setName builder (get m :name)))
    (when (contains? m :value)
      (.addAllValue builder (get m :value)))
    (when (contains? m :options)
      (.setOptions builder (get m :options)))
    (when (contains? m :reserved-range)
      (.addAllReservedRange builder (get m :reserved-range)))
    (when (contains? m :reserved-name)
      (.addAllReservedName builder (get m :reserved-name)))
    (when (contains? m :visibility)
      (.setVisibility builder (get m :visibility)))

    (.build builder)))

(defn parse-enum-descriptor-proto
  "Parse a EnumDescriptorProto protobuf message to a map."
  [^google.protobuf.Protobuf$EnumDescriptorProto proto]
  (cond-> {}
    ;; Regular fields
    (.hasName proto) (assoc :name (.getName proto))
    true (assoc :value (vec (.getValueList proto)))
    (.hasOptions proto) (assoc :options (.getOptions proto))
    true (assoc :reserved-range (vec (.getReservedRangeList proto)))
    true (assoc :reserved-name (vec (.getReservedNameList proto)))
    (.hasVisibility proto) (assoc :visibility (.getVisibility proto))))

(defn build-enum-value-descriptor-proto
  "Build a EnumValueDescriptorProto protobuf message from a map."
  [m]
  (let [builder (google.protobuf.Protobuf$EnumValueDescriptorProto/newBuilder)]
    ;; Set regular fields
    (when (contains? m :name)
      (.setName builder (get m :name)))
    (when (contains? m :number)
      (.setNumber builder (get m :number)))
    (when (contains? m :options)
      (.setOptions builder (get m :options)))

    (.build builder)))

(defn parse-enum-value-descriptor-proto
  "Parse a EnumValueDescriptorProto protobuf message to a map."
  [^google.protobuf.Protobuf$EnumValueDescriptorProto proto]
  (cond-> {}
    ;; Regular fields
    (.hasName proto) (assoc :name (.getName proto))
    (.hasNumber proto) (assoc :number (.getNumber proto))
    (.hasOptions proto) (assoc :options (.getOptions proto))))

(defn build-service-descriptor-proto
  "Build a ServiceDescriptorProto protobuf message from a map."
  [m]
  (let [builder (google.protobuf.Protobuf$ServiceDescriptorProto/newBuilder)]
    ;; Set regular fields
    (when (contains? m :name)
      (.setName builder (get m :name)))
    (when (contains? m :method)
      (.addAllMethod builder (get m :method)))
    (when (contains? m :options)
      (.setOptions builder (get m :options)))

    (.build builder)))

(defn parse-service-descriptor-proto
  "Parse a ServiceDescriptorProto protobuf message to a map."
  [^google.protobuf.Protobuf$ServiceDescriptorProto proto]
  (cond-> {}
    ;; Regular fields
    (.hasName proto) (assoc :name (.getName proto))
    true (assoc :method (vec (.getMethodList proto)))
    (.hasOptions proto) (assoc :options (.getOptions proto))))

(defn build-method-descriptor-proto
  "Build a MethodDescriptorProto protobuf message from a map."
  [m]
  (let [builder (google.protobuf.Protobuf$MethodDescriptorProto/newBuilder)]
    ;; Set regular fields
    (when (contains? m :name)
      (.setName builder (get m :name)))
    (when (contains? m :input-type)
      (.setInputType builder (get m :input-type)))
    (when (contains? m :output-type)
      (.setOutputType builder (get m :output-type)))
    (when (contains? m :options)
      (.setOptions builder (get m :options)))
    (when (contains? m :client-streaming)
      (.setClientStreaming builder (get m :client-streaming)))
    (when (contains? m :server-streaming)
      (.setServerStreaming builder (get m :server-streaming)))

    (.build builder)))

(defn parse-method-descriptor-proto
  "Parse a MethodDescriptorProto protobuf message to a map."
  [^google.protobuf.Protobuf$MethodDescriptorProto proto]
  (cond-> {}
    ;; Regular fields
    (.hasName proto) (assoc :name (.getName proto))
    (.hasInputType proto) (assoc :input-type (.getInputType proto))
    (.hasOutputType proto) (assoc :output-type (.getOutputType proto))
    (.hasOptions proto) (assoc :options (.getOptions proto))
    (.hasClientStreaming proto) (assoc :client-streaming (.getClientStreaming proto))
    (.hasServerStreaming proto) (assoc :server-streaming (.getServerStreaming proto))))

(defn build-file-options
  "Build a FileOptions protobuf message from a map."
  [m]
  (let [builder (google.protobuf.Protobuf$FileOptions/newBuilder)]
    ;; Set regular fields
    (when (contains? m :java-package)
      (.setJavaPackage builder (get m :java-package)))
    (when (contains? m :java-outer-classname)
      (.setJavaOuterClassname builder (get m :java-outer-classname)))
    (when (contains? m :java-multiple-files)
      (.setJavaMultipleFiles builder (get m :java-multiple-files)))
    (when (contains? m :java-generate-equals-and-hash)
      (.setJavaGenerateEqualsAndHash builder (get m :java-generate-equals-and-hash)))
    (when (contains? m :java-string-check-utf-8)
      (.setJavaStringCheckUtf8 builder (get m :java-string-check-utf-8)))
    (when (contains? m :optimize-for)
      (.setOptimizeFor builder (get m :optimize-for)))
    (when (contains? m :go-package)
      (.setGoPackage builder (get m :go-package)))
    (when (contains? m :cc-generic-services)
      (.setCcGenericServices builder (get m :cc-generic-services)))
    (when (contains? m :java-generic-services)
      (.setJavaGenericServices builder (get m :java-generic-services)))
    (when (contains? m :py-generic-services)
      (.setPyGenericServices builder (get m :py-generic-services)))
    (when (contains? m :deprecated)
      (.setDeprecated builder (get m :deprecated)))
    (when (contains? m :cc-enable-arenas)
      (.setCcEnableArenas builder (get m :cc-enable-arenas)))
    (when (contains? m :objc-class-prefix)
      (.setObjcClassPrefix builder (get m :objc-class-prefix)))
    (when (contains? m :csharp-namespace)
      (.setCsharpNamespace builder (get m :csharp-namespace)))
    (when (contains? m :swift-prefix)
      (.setSwiftPrefix builder (get m :swift-prefix)))
    (when (contains? m :php-class-prefix)
      (.setPhpClassPrefix builder (get m :php-class-prefix)))
    (when (contains? m :php-namespace)
      (.setPhpNamespace builder (get m :php-namespace)))
    (when (contains? m :php-metadata-namespace)
      (.setPhpMetadataNamespace builder (get m :php-metadata-namespace)))
    (when (contains? m :ruby-package)
      (.setRubyPackage builder (get m :ruby-package)))
    (when (contains? m :features)
      (.setFeatures builder (get m :features)))
    (when (contains? m :uninterpreted-option)
      (.addAllUninterpretedOption builder (get m :uninterpreted-option)))

    (.build builder)))

(defn parse-file-options
  "Parse a FileOptions protobuf message to a map."
  [^google.protobuf.Protobuf$FileOptions proto]
  (cond-> {}
    ;; Regular fields
    (.hasJavaPackage proto) (assoc :java-package (.getJavaPackage proto))
    (.hasJavaOuterClassname proto) (assoc :java-outer-classname (.getJavaOuterClassname proto))
    (.hasJavaMultipleFiles proto) (assoc :java-multiple-files (.getJavaMultipleFiles proto))
    (.hasJavaGenerateEqualsAndHash proto) (assoc :java-generate-equals-and-hash (.getJavaGenerateEqualsAndHash proto))
    (.hasJavaStringCheckUtf8 proto) (assoc :java-string-check-utf-8 (.getJavaStringCheckUtf8 proto))
    (.hasOptimizeFor proto) (assoc :optimize-for (.getOptimizeFor proto))
    (.hasGoPackage proto) (assoc :go-package (.getGoPackage proto))
    (.hasCcGenericServices proto) (assoc :cc-generic-services (.getCcGenericServices proto))
    (.hasJavaGenericServices proto) (assoc :java-generic-services (.getJavaGenericServices proto))
    (.hasPyGenericServices proto) (assoc :py-generic-services (.getPyGenericServices proto))
    (.hasDeprecated proto) (assoc :deprecated (.getDeprecated proto))
    (.hasCcEnableArenas proto) (assoc :cc-enable-arenas (.getCcEnableArenas proto))
    (.hasObjcClassPrefix proto) (assoc :objc-class-prefix (.getObjcClassPrefix proto))
    (.hasCsharpNamespace proto) (assoc :csharp-namespace (.getCsharpNamespace proto))
    (.hasSwiftPrefix proto) (assoc :swift-prefix (.getSwiftPrefix proto))
    (.hasPhpClassPrefix proto) (assoc :php-class-prefix (.getPhpClassPrefix proto))
    (.hasPhpNamespace proto) (assoc :php-namespace (.getPhpNamespace proto))
    (.hasPhpMetadataNamespace proto) (assoc :php-metadata-namespace (.getPhpMetadataNamespace proto))
    (.hasRubyPackage proto) (assoc :ruby-package (.getRubyPackage proto))
    (.hasFeatures proto) (assoc :features (.getFeatures proto))
    true (assoc :uninterpreted-option (vec (.getUninterpretedOptionList proto)))))

(defn build-message-options
  "Build a MessageOptions protobuf message from a map."
  [m]
  (let [builder (google.protobuf.Protobuf$MessageOptions/newBuilder)]
    ;; Set regular fields
    (when (contains? m :message-set-wire-format)
      (.setMessageSetWireFormat builder (get m :message-set-wire-format)))
    (when (contains? m :no-standard-descriptor-accessor)
      (.setNoStandardDescriptorAccessor builder (get m :no-standard-descriptor-accessor)))
    (when (contains? m :deprecated)
      (.setDeprecated builder (get m :deprecated)))
    (when (contains? m :map-entry)
      (.setMapEntry builder (get m :map-entry)))
    (when (contains? m :deprecated-legacy-json-field-conflicts)
      (.setDeprecatedLegacyJsonFieldConflicts builder (get m :deprecated-legacy-json-field-conflicts)))
    (when (contains? m :features)
      (.setFeatures builder (get m :features)))
    (when (contains? m :uninterpreted-option)
      (.addAllUninterpretedOption builder (get m :uninterpreted-option)))

    (.build builder)))

(defn parse-message-options
  "Parse a MessageOptions protobuf message to a map."
  [^google.protobuf.Protobuf$MessageOptions proto]
  (cond-> {}
    ;; Regular fields
    (.hasMessageSetWireFormat proto) (assoc :message-set-wire-format (.getMessageSetWireFormat proto))
    (.hasNoStandardDescriptorAccessor proto) (assoc :no-standard-descriptor-accessor (.getNoStandardDescriptorAccessor proto))
    (.hasDeprecated proto) (assoc :deprecated (.getDeprecated proto))
    (.hasMapEntry proto) (assoc :map-entry (.getMapEntry proto))
    (.hasDeprecatedLegacyJsonFieldConflicts proto) (assoc :deprecated-legacy-json-field-conflicts (.getDeprecatedLegacyJsonFieldConflicts proto))
    (.hasFeatures proto) (assoc :features (.getFeatures proto))
    true (assoc :uninterpreted-option (vec (.getUninterpretedOptionList proto)))))

(defn build-field-options
  "Build a FieldOptions protobuf message from a map."
  [m]
  (let [builder (google.protobuf.Protobuf$FieldOptions/newBuilder)]
    ;; Set regular fields
    (when (contains? m :ctype)
      (.setCtype builder (get m :ctype)))
    (when (contains? m :packed)
      (.setPacked builder (get m :packed)))
    (when (contains? m :jstype)
      (.setJstype builder (get m :jstype)))
    (when (contains? m :lazy)
      (.setLazy builder (get m :lazy)))
    (when (contains? m :unverified-lazy)
      (.setUnverifiedLazy builder (get m :unverified-lazy)))
    (when (contains? m :deprecated)
      (.setDeprecated builder (get m :deprecated)))
    (when (contains? m :weak)
      (.setWeak builder (get m :weak)))
    (when (contains? m :debug-redact)
      (.setDebugRedact builder (get m :debug-redact)))
    (when (contains? m :retention)
      (.setRetention builder (get m :retention)))
    (when (contains? m :targets)
      (.addAllTargets builder (get m :targets)))
    (when (contains? m :edition-defaults)
      (.addAllEditionDefaults builder (get m :edition-defaults)))
    (when (contains? m :features)
      (.setFeatures builder (get m :features)))
    (when (contains? m :feature-support)
      (.setFeatureSupport builder (get m :feature-support)))
    (when (contains? m :uninterpreted-option)
      (.addAllUninterpretedOption builder (get m :uninterpreted-option)))

    (.build builder)))

(defn parse-field-options
  "Parse a FieldOptions protobuf message to a map."
  [^google.protobuf.Protobuf$FieldOptions proto]
  (cond-> {}
    ;; Regular fields
    (.hasCtype proto) (assoc :ctype (.getCtype proto))
    (.hasPacked proto) (assoc :packed (.getPacked proto))
    (.hasJstype proto) (assoc :jstype (.getJstype proto))
    (.hasLazy proto) (assoc :lazy (.getLazy proto))
    (.hasUnverifiedLazy proto) (assoc :unverified-lazy (.getUnverifiedLazy proto))
    (.hasDeprecated proto) (assoc :deprecated (.getDeprecated proto))
    (.hasWeak proto) (assoc :weak (.getWeak proto))
    (.hasDebugRedact proto) (assoc :debug-redact (.getDebugRedact proto))
    (.hasRetention proto) (assoc :retention (.getRetention proto))
    true (assoc :targets (vec (.getTargetsList proto)))
    true (assoc :edition-defaults (vec (.getEditionDefaultsList proto)))
    (.hasFeatures proto) (assoc :features (.getFeatures proto))
    (.hasFeatureSupport proto) (assoc :feature-support (.getFeatureSupport proto))
    true (assoc :uninterpreted-option (vec (.getUninterpretedOptionList proto)))))

(defn build-oneof-options
  "Build a OneofOptions protobuf message from a map."
  [m]
  (let [builder (google.protobuf.Protobuf$OneofOptions/newBuilder)]
    ;; Set regular fields
    (when (contains? m :features)
      (.setFeatures builder (get m :features)))
    (when (contains? m :uninterpreted-option)
      (.addAllUninterpretedOption builder (get m :uninterpreted-option)))

    (.build builder)))

(defn parse-oneof-options
  "Parse a OneofOptions protobuf message to a map."
  [^google.protobuf.Protobuf$OneofOptions proto]
  (cond-> {}
    ;; Regular fields
    (.hasFeatures proto) (assoc :features (.getFeatures proto))
    true (assoc :uninterpreted-option (vec (.getUninterpretedOptionList proto)))))

(defn build-enum-options
  "Build a EnumOptions protobuf message from a map."
  [m]
  (let [builder (google.protobuf.Protobuf$EnumOptions/newBuilder)]
    ;; Set regular fields
    (when (contains? m :allow-alias)
      (.setAllowAlias builder (get m :allow-alias)))
    (when (contains? m :deprecated)
      (.setDeprecated builder (get m :deprecated)))
    (when (contains? m :deprecated-legacy-json-field-conflicts)
      (.setDeprecatedLegacyJsonFieldConflicts builder (get m :deprecated-legacy-json-field-conflicts)))
    (when (contains? m :features)
      (.setFeatures builder (get m :features)))
    (when (contains? m :uninterpreted-option)
      (.addAllUninterpretedOption builder (get m :uninterpreted-option)))

    (.build builder)))

(defn parse-enum-options
  "Parse a EnumOptions protobuf message to a map."
  [^google.protobuf.Protobuf$EnumOptions proto]
  (cond-> {}
    ;; Regular fields
    (.hasAllowAlias proto) (assoc :allow-alias (.getAllowAlias proto))
    (.hasDeprecated proto) (assoc :deprecated (.getDeprecated proto))
    (.hasDeprecatedLegacyJsonFieldConflicts proto) (assoc :deprecated-legacy-json-field-conflicts (.getDeprecatedLegacyJsonFieldConflicts proto))
    (.hasFeatures proto) (assoc :features (.getFeatures proto))
    true (assoc :uninterpreted-option (vec (.getUninterpretedOptionList proto)))))

(defn build-enum-value-options
  "Build a EnumValueOptions protobuf message from a map."
  [m]
  (let [builder (google.protobuf.Protobuf$EnumValueOptions/newBuilder)]
    ;; Set regular fields
    (when (contains? m :deprecated)
      (.setDeprecated builder (get m :deprecated)))
    (when (contains? m :features)
      (.setFeatures builder (get m :features)))
    (when (contains? m :debug-redact)
      (.setDebugRedact builder (get m :debug-redact)))
    (when (contains? m :feature-support)
      (.setFeatureSupport builder (get m :feature-support)))
    (when (contains? m :uninterpreted-option)
      (.addAllUninterpretedOption builder (get m :uninterpreted-option)))

    (.build builder)))

(defn parse-enum-value-options
  "Parse a EnumValueOptions protobuf message to a map."
  [^google.protobuf.Protobuf$EnumValueOptions proto]
  (cond-> {}
    ;; Regular fields
    (.hasDeprecated proto) (assoc :deprecated (.getDeprecated proto))
    (.hasFeatures proto) (assoc :features (.getFeatures proto))
    (.hasDebugRedact proto) (assoc :debug-redact (.getDebugRedact proto))
    (.hasFeatureSupport proto) (assoc :feature-support (.getFeatureSupport proto))
    true (assoc :uninterpreted-option (vec (.getUninterpretedOptionList proto)))))

(defn build-service-options
  "Build a ServiceOptions protobuf message from a map."
  [m]
  (let [builder (google.protobuf.Protobuf$ServiceOptions/newBuilder)]
    ;; Set regular fields
    (when (contains? m :features)
      (.setFeatures builder (get m :features)))
    (when (contains? m :deprecated)
      (.setDeprecated builder (get m :deprecated)))
    (when (contains? m :uninterpreted-option)
      (.addAllUninterpretedOption builder (get m :uninterpreted-option)))

    (.build builder)))

(defn parse-service-options
  "Parse a ServiceOptions protobuf message to a map."
  [^google.protobuf.Protobuf$ServiceOptions proto]
  (cond-> {}
    ;; Regular fields
    (.hasFeatures proto) (assoc :features (.getFeatures proto))
    (.hasDeprecated proto) (assoc :deprecated (.getDeprecated proto))
    true (assoc :uninterpreted-option (vec (.getUninterpretedOptionList proto)))))

(defn build-method-options
  "Build a MethodOptions protobuf message from a map."
  [m]
  (let [builder (google.protobuf.Protobuf$MethodOptions/newBuilder)]
    ;; Set regular fields
    (when (contains? m :deprecated)
      (.setDeprecated builder (get m :deprecated)))
    (when (contains? m :idempotency-level)
      (.setIdempotencyLevel builder (get m :idempotency-level)))
    (when (contains? m :features)
      (.setFeatures builder (get m :features)))
    (when (contains? m :uninterpreted-option)
      (.addAllUninterpretedOption builder (get m :uninterpreted-option)))

    (.build builder)))

(defn parse-method-options
  "Parse a MethodOptions protobuf message to a map."
  [^google.protobuf.Protobuf$MethodOptions proto]
  (cond-> {}
    ;; Regular fields
    (.hasDeprecated proto) (assoc :deprecated (.getDeprecated proto))
    (.hasIdempotencyLevel proto) (assoc :idempotency-level (.getIdempotencyLevel proto))
    (.hasFeatures proto) (assoc :features (.getFeatures proto))
    true (assoc :uninterpreted-option (vec (.getUninterpretedOptionList proto)))))

(defn build-uninterpreted-option
  "Build a UninterpretedOption protobuf message from a map."
  [m]
  (let [builder (google.protobuf.Protobuf$UninterpretedOption/newBuilder)]
    ;; Set regular fields
    (when (contains? m :name)
      (.addAllName builder (get m :name)))
    (when (contains? m :identifier-value)
      (.setIdentifierValue builder (get m :identifier-value)))
    (when (contains? m :positive-int-value)
      (.setPositiveIntValue builder (get m :positive-int-value)))
    (when (contains? m :negative-int-value)
      (.setNegativeIntValue builder (get m :negative-int-value)))
    (when (contains? m :double-value)
      (.setDoubleValue builder (get m :double-value)))
    (when (contains? m :string-value)
      (.setStringValue builder (get m :string-value)))
    (when (contains? m :aggregate-value)
      (.setAggregateValue builder (get m :aggregate-value)))

    (.build builder)))

(defn parse-uninterpreted-option
  "Parse a UninterpretedOption protobuf message to a map."
  [^google.protobuf.Protobuf$UninterpretedOption proto]
  (cond-> {}
    ;; Regular fields
    true (assoc :name (vec (.getNameList proto)))
    (.hasIdentifierValue proto) (assoc :identifier-value (.getIdentifierValue proto))
    (.hasPositiveIntValue proto) (assoc :positive-int-value (.getPositiveIntValue proto))
    (.hasNegativeIntValue proto) (assoc :negative-int-value (.getNegativeIntValue proto))
    (.hasDoubleValue proto) (assoc :double-value (.getDoubleValue proto))
    (.hasStringValue proto) (assoc :string-value (.getStringValue proto))
    (.hasAggregateValue proto) (assoc :aggregate-value (.getAggregateValue proto))))

(defn build-feature-set
  "Build a FeatureSet protobuf message from a map."
  [m]
  (let [builder (google.protobuf.Protobuf$FeatureSet/newBuilder)]
    ;; Set regular fields
    (when (contains? m :field-presence)
      (.setFieldPresence builder (get m :field-presence)))
    (when (contains? m :enum-type)
      (.setEnumType builder (get m :enum-type)))
    (when (contains? m :repeated-field-encoding)
      (.setRepeatedFieldEncoding builder (get m :repeated-field-encoding)))
    (when (contains? m :utf-8-validation)
      (.setUtf8Validation builder (get m :utf-8-validation)))
    (when (contains? m :message-encoding)
      (.setMessageEncoding builder (get m :message-encoding)))
    (when (contains? m :json-format)
      (.setJsonFormat builder (get m :json-format)))
    (when (contains? m :enforce-naming-style)
      (.setEnforceNamingStyle builder (get m :enforce-naming-style)))
    (when (contains? m :default-symbol-visibility)
      (.setDefaultSymbolVisibility builder (get m :default-symbol-visibility)))

    (.build builder)))

(defn parse-feature-set
  "Parse a FeatureSet protobuf message to a map."
  [^google.protobuf.Protobuf$FeatureSet proto]
  (cond-> {}
    ;; Regular fields
    (.hasFieldPresence proto) (assoc :field-presence (.getFieldPresence proto))
    (.hasEnumType proto) (assoc :enum-type (.getEnumType proto))
    (.hasRepeatedFieldEncoding proto) (assoc :repeated-field-encoding (.getRepeatedFieldEncoding proto))
    (.hasUtf8Validation proto) (assoc :utf-8-validation (.getUtf8Validation proto))
    (.hasMessageEncoding proto) (assoc :message-encoding (.getMessageEncoding proto))
    (.hasJsonFormat proto) (assoc :json-format (.getJsonFormat proto))
    (.hasEnforceNamingStyle proto) (assoc :enforce-naming-style (.getEnforceNamingStyle proto))
    (.hasDefaultSymbolVisibility proto) (assoc :default-symbol-visibility (.getDefaultSymbolVisibility proto))))

(defn build-feature-set-defaults
  "Build a FeatureSetDefaults protobuf message from a map."
  [m]
  (let [builder (google.protobuf.Protobuf$FeatureSetDefaults/newBuilder)]
    ;; Set regular fields
    (when (contains? m :defaults)
      (.addAllDefaults builder (get m :defaults)))
    (when (contains? m :minimum-edition)
      (.setMinimumEdition builder (get m :minimum-edition)))
    (when (contains? m :maximum-edition)
      (.setMaximumEdition builder (get m :maximum-edition)))

    (.build builder)))

(defn parse-feature-set-defaults
  "Parse a FeatureSetDefaults protobuf message to a map."
  [^google.protobuf.Protobuf$FeatureSetDefaults proto]
  (cond-> {}
    ;; Regular fields
    true (assoc :defaults (vec (.getDefaultsList proto)))
    (.hasMinimumEdition proto) (assoc :minimum-edition (.getMinimumEdition proto))
    (.hasMaximumEdition proto) (assoc :maximum-edition (.getMaximumEdition proto))))

(defn build-source-code-info
  "Build a SourceCodeInfo protobuf message from a map."
  [m]
  (let [builder (google.protobuf.Protobuf$SourceCodeInfo/newBuilder)]
    ;; Set regular fields
    (when (contains? m :location)
      (.addAllLocation builder (get m :location)))

    (.build builder)))

(defn parse-source-code-info
  "Parse a SourceCodeInfo protobuf message to a map."
  [^google.protobuf.Protobuf$SourceCodeInfo proto]
  (cond-> {}
    ;; Regular fields
    true (assoc :location (vec (.getLocationList proto)))))

(defn build-generated-code-info
  "Build a GeneratedCodeInfo protobuf message from a map."
  [m]
  (let [builder (google.protobuf.Protobuf$GeneratedCodeInfo/newBuilder)]
    ;; Set regular fields
    (when (contains? m :annotation)
      (.addAllAnnotation builder (get m :annotation)))

    (.build builder)))

(defn parse-generated-code-info
  "Parse a GeneratedCodeInfo protobuf message to a map."
  [^google.protobuf.Protobuf$GeneratedCodeInfo proto]
  (cond-> {}
    ;; Regular fields
    true (assoc :annotation (vec (.getAnnotationList proto)))))

(defn build-duration
  "Build a Duration protobuf message from a map."
  [m]
  (let [builder (google.protobuf.Protobuf$Duration/newBuilder)]
    ;; Set regular fields
    (when (contains? m :seconds)
      (.setSeconds builder (get m :seconds)))
    (when (contains? m :nanos)
      (.setNanos builder (get m :nanos)))

    (.build builder)))

(defn parse-duration
  "Parse a Duration protobuf message to a map."
  [^google.protobuf.Protobuf$Duration proto]
  (cond-> {}
    ;; Regular fields
    (.hasSeconds proto) (assoc :seconds (.getSeconds proto))
    (.hasNanos proto) (assoc :nanos (.getNanos proto))))

(defn build-timestamp
  "Build a Timestamp protobuf message from a map."
  [m]
  (let [builder (google.protobuf.Protobuf$Timestamp/newBuilder)]
    ;; Set regular fields
    (when (contains? m :seconds)
      (.setSeconds builder (get m :seconds)))
    (when (contains? m :nanos)
      (.setNanos builder (get m :nanos)))

    (.build builder)))

(defn parse-timestamp
  "Parse a Timestamp protobuf message to a map."
  [^google.protobuf.Protobuf$Timestamp proto]
  (cond-> {}
    ;; Regular fields
    (.hasSeconds proto) (assoc :seconds (.getSeconds proto))
    (.hasNanos proto) (assoc :nanos (.getNanos proto))))

(defn build-rule
  "Build a Rule protobuf message from a map."
  [m]
  (let [builder (buf.validate.Validate$Rule/newBuilder)]
    ;; Set regular fields
    (when (contains? m :id)
      (.setId builder (get m :id)))
    (when (contains? m :message)
      (.setMessage builder (get m :message)))
    (when (contains? m :expression)
      (.setExpression builder (get m :expression)))

    (.build builder)))

(defn parse-rule
  "Parse a Rule protobuf message to a map."
  [^buf.validate.Validate$Rule proto]
  (cond-> {}
    ;; Regular fields
    (.hasId proto) (assoc :id (.getId proto))
    (.hasMessage proto) (assoc :message (.getMessage proto))
    (.hasExpression proto) (assoc :expression (.getExpression proto))))

(defn build-message-rules
  "Build a MessageRules protobuf message from a map."
  [m]
  (let [builder (buf.validate.Validate$MessageRules/newBuilder)]
    ;; Set regular fields
    (when (contains? m :cel)
      (.addAllCel builder (get m :cel)))
    (when (contains? m :oneof)
      (.addAllOneof builder (get m :oneof)))

    (.build builder)))

(defn parse-message-rules
  "Parse a MessageRules protobuf message to a map."
  [^buf.validate.Validate$MessageRules proto]
  (cond-> {}
    ;; Regular fields
    true (assoc :cel (vec (.getCelList proto)))
    true (assoc :oneof (vec (.getOneofList proto)))))

(defn build-message-oneof-rule
  "Build a MessageOneofRule protobuf message from a map."
  [m]
  (let [builder (buf.validate.Validate$MessageOneofRule/newBuilder)]
    ;; Set regular fields
    (when (contains? m :fields)
      (.addAllFields builder (get m :fields)))
    (when (contains? m :required)
      (.setRequired builder (get m :required)))

    (.build builder)))

(defn parse-message-oneof-rule
  "Parse a MessageOneofRule protobuf message to a map."
  [^buf.validate.Validate$MessageOneofRule proto]
  (cond-> {}
    ;; Regular fields
    true (assoc :fields (vec (.getFieldsList proto)))
    (.hasRequired proto) (assoc :required (.getRequired proto))))

(defn build-oneof-rules
  "Build a OneofRules protobuf message from a map."
  [m]
  (let [builder (buf.validate.Validate$OneofRules/newBuilder)]
    ;; Set regular fields
    (when (contains? m :required)
      (.setRequired builder (get m :required)))

    (.build builder)))

(defn parse-oneof-rules
  "Parse a OneofRules protobuf message to a map."
  [^buf.validate.Validate$OneofRules proto]
  (cond-> {}
    ;; Regular fields
    (.hasRequired proto) (assoc :required (.getRequired proto))))

(defn build-field-rules
  "Build a FieldRules protobuf message from a map."
  [m]
  (let [builder (buf.validate.Validate$FieldRules/newBuilder)]
    ;; Set regular fields
    (when (contains? m :cel)
      (.addAllCel builder (get m :cel)))
    (when (contains? m :required)
      (.setRequired builder (get m :required)))
    (when (contains? m :ignore)
      (.setIgnore builder (get m :ignore)))

    (.build builder)))

(defn parse-field-rules
  "Parse a FieldRules protobuf message to a map."
  [^buf.validate.Validate$FieldRules proto]
  (cond-> {}
    ;; Regular fields
    true (assoc :cel (vec (.getCelList proto)))
    (.hasRequired proto) (assoc :required (.getRequired proto))
    (.hasIgnore proto) (assoc :ignore (.getIgnore proto))

    ;; Oneof payload
    true (merge (parse-field-rules-payload proto))))

(defn build-predefined-rules
  "Build a PredefinedRules protobuf message from a map."
  [m]
  (let [builder (buf.validate.Validate$PredefinedRules/newBuilder)]
    ;; Set regular fields
    (when (contains? m :cel)
      (.addAllCel builder (get m :cel)))

    (.build builder)))

(defn parse-predefined-rules
  "Parse a PredefinedRules protobuf message to a map."
  [^buf.validate.Validate$PredefinedRules proto]
  (cond-> {}
    ;; Regular fields
    true (assoc :cel (vec (.getCelList proto)))))

(defn build-float-rules
  "Build a FloatRules protobuf message from a map."
  [m]
  (let [builder (buf.validate.Validate$FloatRules/newBuilder)]
    ;; Set regular fields
    (when (contains? m :const)
      (.setConst builder (get m :const)))
    (when (contains? m :in)
      (.addAllIn builder (get m :in)))
    (when (contains? m :not-in)
      (.addAllNotIn builder (get m :not-in)))
    (when (contains? m :finite)
      (.setFinite builder (get m :finite)))
    (when (contains? m :example)
      (.addAllExample builder (get m :example)))

    (.build builder)))

(defn parse-float-rules
  "Parse a FloatRules protobuf message to a map."
  [^buf.validate.Validate$FloatRules proto]
  (cond-> {}
    ;; Regular fields
    (.hasConst proto) (assoc :const (.getConst proto))
    true (assoc :in (vec (.getInList proto)))
    true (assoc :not-in (vec (.getNotInList proto)))
    (.hasFinite proto) (assoc :finite (.getFinite proto))
    true (assoc :example (vec (.getExampleList proto)))

    ;; Oneof payload
    true (merge (parse-float-rules-payload proto))))

(defn build-double-rules
  "Build a DoubleRules protobuf message from a map."
  [m]
  (let [builder (buf.validate.Validate$DoubleRules/newBuilder)]
    ;; Set regular fields
    (when (contains? m :const)
      (.setConst builder (get m :const)))
    (when (contains? m :in)
      (.addAllIn builder (get m :in)))
    (when (contains? m :not-in)
      (.addAllNotIn builder (get m :not-in)))
    (when (contains? m :finite)
      (.setFinite builder (get m :finite)))
    (when (contains? m :example)
      (.addAllExample builder (get m :example)))

    (.build builder)))

(defn parse-double-rules
  "Parse a DoubleRules protobuf message to a map."
  [^buf.validate.Validate$DoubleRules proto]
  (cond-> {}
    ;; Regular fields
    (.hasConst proto) (assoc :const (.getConst proto))
    true (assoc :in (vec (.getInList proto)))
    true (assoc :not-in (vec (.getNotInList proto)))
    (.hasFinite proto) (assoc :finite (.getFinite proto))
    true (assoc :example (vec (.getExampleList proto)))

    ;; Oneof payload
    true (merge (parse-double-rules-payload proto))))

(defn build-int-32-rules
  "Build a Int32Rules protobuf message from a map."
  [m]
  (let [builder (buf.validate.Validate$Int32Rules/newBuilder)]
    ;; Set regular fields
    (when (contains? m :const)
      (.setConst builder (get m :const)))
    (when (contains? m :in)
      (.addAllIn builder (get m :in)))
    (when (contains? m :not-in)
      (.addAllNotIn builder (get m :not-in)))
    (when (contains? m :example)
      (.addAllExample builder (get m :example)))

    (.build builder)))

(defn parse-int-32-rules
  "Parse a Int32Rules protobuf message to a map."
  [^buf.validate.Validate$Int32Rules proto]
  (cond-> {}
    ;; Regular fields
    (.hasConst proto) (assoc :const (.getConst proto))
    true (assoc :in (vec (.getInList proto)))
    true (assoc :not-in (vec (.getNotInList proto)))
    true (assoc :example (vec (.getExampleList proto)))

    ;; Oneof payload
    true (merge (parse-int-32-rules-payload proto))))

(defn build-int-64-rules
  "Build a Int64Rules protobuf message from a map."
  [m]
  (let [builder (buf.validate.Validate$Int64Rules/newBuilder)]
    ;; Set regular fields
    (when (contains? m :const)
      (.setConst builder (get m :const)))
    (when (contains? m :in)
      (.addAllIn builder (get m :in)))
    (when (contains? m :not-in)
      (.addAllNotIn builder (get m :not-in)))
    (when (contains? m :example)
      (.addAllExample builder (get m :example)))

    (.build builder)))

(defn parse-int-64-rules
  "Parse a Int64Rules protobuf message to a map."
  [^buf.validate.Validate$Int64Rules proto]
  (cond-> {}
    ;; Regular fields
    (.hasConst proto) (assoc :const (.getConst proto))
    true (assoc :in (vec (.getInList proto)))
    true (assoc :not-in (vec (.getNotInList proto)))
    true (assoc :example (vec (.getExampleList proto)))

    ;; Oneof payload
    true (merge (parse-int-64-rules-payload proto))))

(defn build-u-int-32-rules
  "Build a UInt32Rules protobuf message from a map."
  [m]
  (let [builder (buf.validate.Validate$UInt32Rules/newBuilder)]
    ;; Set regular fields
    (when (contains? m :const)
      (.setConst builder (get m :const)))
    (when (contains? m :in)
      (.addAllIn builder (get m :in)))
    (when (contains? m :not-in)
      (.addAllNotIn builder (get m :not-in)))
    (when (contains? m :example)
      (.addAllExample builder (get m :example)))

    (.build builder)))

(defn parse-u-int-32-rules
  "Parse a UInt32Rules protobuf message to a map."
  [^buf.validate.Validate$UInt32Rules proto]
  (cond-> {}
    ;; Regular fields
    (.hasConst proto) (assoc :const (.getConst proto))
    true (assoc :in (vec (.getInList proto)))
    true (assoc :not-in (vec (.getNotInList proto)))
    true (assoc :example (vec (.getExampleList proto)))

    ;; Oneof payload
    true (merge (parse-u-int-32-rules-payload proto))))

(defn build-u-int-64-rules
  "Build a UInt64Rules protobuf message from a map."
  [m]
  (let [builder (buf.validate.Validate$UInt64Rules/newBuilder)]
    ;; Set regular fields
    (when (contains? m :const)
      (.setConst builder (get m :const)))
    (when (contains? m :in)
      (.addAllIn builder (get m :in)))
    (when (contains? m :not-in)
      (.addAllNotIn builder (get m :not-in)))
    (when (contains? m :example)
      (.addAllExample builder (get m :example)))

    (.build builder)))

(defn parse-u-int-64-rules
  "Parse a UInt64Rules protobuf message to a map."
  [^buf.validate.Validate$UInt64Rules proto]
  (cond-> {}
    ;; Regular fields
    (.hasConst proto) (assoc :const (.getConst proto))
    true (assoc :in (vec (.getInList proto)))
    true (assoc :not-in (vec (.getNotInList proto)))
    true (assoc :example (vec (.getExampleList proto)))

    ;; Oneof payload
    true (merge (parse-u-int-64-rules-payload proto))))

(defn build-s-int-32-rules
  "Build a SInt32Rules protobuf message from a map."
  [m]
  (let [builder (buf.validate.Validate$SInt32Rules/newBuilder)]
    ;; Set regular fields
    (when (contains? m :const)
      (.setConst builder (get m :const)))
    (when (contains? m :in)
      (.addAllIn builder (get m :in)))
    (when (contains? m :not-in)
      (.addAllNotIn builder (get m :not-in)))
    (when (contains? m :example)
      (.addAllExample builder (get m :example)))

    (.build builder)))

(defn parse-s-int-32-rules
  "Parse a SInt32Rules protobuf message to a map."
  [^buf.validate.Validate$SInt32Rules proto]
  (cond-> {}
    ;; Regular fields
    (.hasConst proto) (assoc :const (.getConst proto))
    true (assoc :in (vec (.getInList proto)))
    true (assoc :not-in (vec (.getNotInList proto)))
    true (assoc :example (vec (.getExampleList proto)))

    ;; Oneof payload
    true (merge (parse-s-int-32-rules-payload proto))))

(defn build-s-int-64-rules
  "Build a SInt64Rules protobuf message from a map."
  [m]
  (let [builder (buf.validate.Validate$SInt64Rules/newBuilder)]
    ;; Set regular fields
    (when (contains? m :const)
      (.setConst builder (get m :const)))
    (when (contains? m :in)
      (.addAllIn builder (get m :in)))
    (when (contains? m :not-in)
      (.addAllNotIn builder (get m :not-in)))
    (when (contains? m :example)
      (.addAllExample builder (get m :example)))

    (.build builder)))

(defn parse-s-int-64-rules
  "Parse a SInt64Rules protobuf message to a map."
  [^buf.validate.Validate$SInt64Rules proto]
  (cond-> {}
    ;; Regular fields
    (.hasConst proto) (assoc :const (.getConst proto))
    true (assoc :in (vec (.getInList proto)))
    true (assoc :not-in (vec (.getNotInList proto)))
    true (assoc :example (vec (.getExampleList proto)))

    ;; Oneof payload
    true (merge (parse-s-int-64-rules-payload proto))))

(defn build-fixed-32-rules
  "Build a Fixed32Rules protobuf message from a map."
  [m]
  (let [builder (buf.validate.Validate$Fixed32Rules/newBuilder)]
    ;; Set regular fields
    (when (contains? m :const)
      (.setConst builder (get m :const)))
    (when (contains? m :in)
      (.addAllIn builder (get m :in)))
    (when (contains? m :not-in)
      (.addAllNotIn builder (get m :not-in)))
    (when (contains? m :example)
      (.addAllExample builder (get m :example)))

    (.build builder)))

(defn parse-fixed-32-rules
  "Parse a Fixed32Rules protobuf message to a map."
  [^buf.validate.Validate$Fixed32Rules proto]
  (cond-> {}
    ;; Regular fields
    (.hasConst proto) (assoc :const (.getConst proto))
    true (assoc :in (vec (.getInList proto)))
    true (assoc :not-in (vec (.getNotInList proto)))
    true (assoc :example (vec (.getExampleList proto)))

    ;; Oneof payload
    true (merge (parse-fixed-32-rules-payload proto))))

(defn build-fixed-64-rules
  "Build a Fixed64Rules protobuf message from a map."
  [m]
  (let [builder (buf.validate.Validate$Fixed64Rules/newBuilder)]
    ;; Set regular fields
    (when (contains? m :const)
      (.setConst builder (get m :const)))
    (when (contains? m :in)
      (.addAllIn builder (get m :in)))
    (when (contains? m :not-in)
      (.addAllNotIn builder (get m :not-in)))
    (when (contains? m :example)
      (.addAllExample builder (get m :example)))

    (.build builder)))

(defn parse-fixed-64-rules
  "Parse a Fixed64Rules protobuf message to a map."
  [^buf.validate.Validate$Fixed64Rules proto]
  (cond-> {}
    ;; Regular fields
    (.hasConst proto) (assoc :const (.getConst proto))
    true (assoc :in (vec (.getInList proto)))
    true (assoc :not-in (vec (.getNotInList proto)))
    true (assoc :example (vec (.getExampleList proto)))

    ;; Oneof payload
    true (merge (parse-fixed-64-rules-payload proto))))

(defn build-s-fixed-32-rules
  "Build a SFixed32Rules protobuf message from a map."
  [m]
  (let [builder (buf.validate.Validate$SFixed32Rules/newBuilder)]
    ;; Set regular fields
    (when (contains? m :const)
      (.setConst builder (get m :const)))
    (when (contains? m :in)
      (.addAllIn builder (get m :in)))
    (when (contains? m :not-in)
      (.addAllNotIn builder (get m :not-in)))
    (when (contains? m :example)
      (.addAllExample builder (get m :example)))

    (.build builder)))

(defn parse-s-fixed-32-rules
  "Parse a SFixed32Rules protobuf message to a map."
  [^buf.validate.Validate$SFixed32Rules proto]
  (cond-> {}
    ;; Regular fields
    (.hasConst proto) (assoc :const (.getConst proto))
    true (assoc :in (vec (.getInList proto)))
    true (assoc :not-in (vec (.getNotInList proto)))
    true (assoc :example (vec (.getExampleList proto)))

    ;; Oneof payload
    true (merge (parse-s-fixed-32-rules-payload proto))))

(defn build-s-fixed-64-rules
  "Build a SFixed64Rules protobuf message from a map."
  [m]
  (let [builder (buf.validate.Validate$SFixed64Rules/newBuilder)]
    ;; Set regular fields
    (when (contains? m :const)
      (.setConst builder (get m :const)))
    (when (contains? m :in)
      (.addAllIn builder (get m :in)))
    (when (contains? m :not-in)
      (.addAllNotIn builder (get m :not-in)))
    (when (contains? m :example)
      (.addAllExample builder (get m :example)))

    (.build builder)))

(defn parse-s-fixed-64-rules
  "Parse a SFixed64Rules protobuf message to a map."
  [^buf.validate.Validate$SFixed64Rules proto]
  (cond-> {}
    ;; Regular fields
    (.hasConst proto) (assoc :const (.getConst proto))
    true (assoc :in (vec (.getInList proto)))
    true (assoc :not-in (vec (.getNotInList proto)))
    true (assoc :example (vec (.getExampleList proto)))

    ;; Oneof payload
    true (merge (parse-s-fixed-64-rules-payload proto))))

(defn build-bool-rules
  "Build a BoolRules protobuf message from a map."
  [m]
  (let [builder (buf.validate.Validate$BoolRules/newBuilder)]
    ;; Set regular fields
    (when (contains? m :const)
      (.setConst builder (get m :const)))
    (when (contains? m :example)
      (.addAllExample builder (get m :example)))

    (.build builder)))

(defn parse-bool-rules
  "Parse a BoolRules protobuf message to a map."
  [^buf.validate.Validate$BoolRules proto]
  (cond-> {}
    ;; Regular fields
    (.hasConst proto) (assoc :const (.getConst proto))
    true (assoc :example (vec (.getExampleList proto)))))

(defn build-string-rules
  "Build a StringRules protobuf message from a map."
  [m]
  (let [builder (buf.validate.Validate$StringRules/newBuilder)]
    ;; Set regular fields
    (when (contains? m :const)
      (.setConst builder (get m :const)))
    (when (contains? m :len)
      (.setLen builder (get m :len)))
    (when (contains? m :min-len)
      (.setMinLen builder (get m :min-len)))
    (when (contains? m :max-len)
      (.setMaxLen builder (get m :max-len)))
    (when (contains? m :len-bytes)
      (.setLenBytes builder (get m :len-bytes)))
    (when (contains? m :min-bytes)
      (.setMinBytes builder (get m :min-bytes)))
    (when (contains? m :max-bytes)
      (.setMaxBytes builder (get m :max-bytes)))
    (when (contains? m :pattern)
      (.setPattern builder (get m :pattern)))
    (when (contains? m :prefix)
      (.setPrefix builder (get m :prefix)))
    (when (contains? m :suffix)
      (.setSuffix builder (get m :suffix)))
    (when (contains? m :contains)
      (.setContains builder (get m :contains)))
    (when (contains? m :not-contains)
      (.setNotContains builder (get m :not-contains)))
    (when (contains? m :in)
      (.addAllIn builder (get m :in)))
    (when (contains? m :not-in)
      (.addAllNotIn builder (get m :not-in)))
    (when (contains? m :strict)
      (.setStrict builder (get m :strict)))
    (when (contains? m :example)
      (.addAllExample builder (get m :example)))

    (.build builder)))

(defn parse-string-rules
  "Parse a StringRules protobuf message to a map."
  [^buf.validate.Validate$StringRules proto]
  (cond-> {}
    ;; Regular fields
    (.hasConst proto) (assoc :const (.getConst proto))
    (.hasLen proto) (assoc :len (.getLen proto))
    (.hasMinLen proto) (assoc :min-len (.getMinLen proto))
    (.hasMaxLen proto) (assoc :max-len (.getMaxLen proto))
    (.hasLenBytes proto) (assoc :len-bytes (.getLenBytes proto))
    (.hasMinBytes proto) (assoc :min-bytes (.getMinBytes proto))
    (.hasMaxBytes proto) (assoc :max-bytes (.getMaxBytes proto))
    (.hasPattern proto) (assoc :pattern (.getPattern proto))
    (.hasPrefix proto) (assoc :prefix (.getPrefix proto))
    (.hasSuffix proto) (assoc :suffix (.getSuffix proto))
    (.hasContains proto) (assoc :contains (.getContains proto))
    (.hasNotContains proto) (assoc :not-contains (.getNotContains proto))
    true (assoc :in (vec (.getInList proto)))
    true (assoc :not-in (vec (.getNotInList proto)))
    (.hasStrict proto) (assoc :strict (.getStrict proto))
    true (assoc :example (vec (.getExampleList proto)))

    ;; Oneof payload
    true (merge (parse-string-rules-payload proto))))

(defn build-bytes-rules
  "Build a BytesRules protobuf message from a map."
  [m]
  (let [builder (buf.validate.Validate$BytesRules/newBuilder)]
    ;; Set regular fields
    (when (contains? m :const)
      (.setConst builder (get m :const)))
    (when (contains? m :len)
      (.setLen builder (get m :len)))
    (when (contains? m :min-len)
      (.setMinLen builder (get m :min-len)))
    (when (contains? m :max-len)
      (.setMaxLen builder (get m :max-len)))
    (when (contains? m :pattern)
      (.setPattern builder (get m :pattern)))
    (when (contains? m :prefix)
      (.setPrefix builder (get m :prefix)))
    (when (contains? m :suffix)
      (.setSuffix builder (get m :suffix)))
    (when (contains? m :contains)
      (.setContains builder (get m :contains)))
    (when (contains? m :in)
      (.addAllIn builder (get m :in)))
    (when (contains? m :not-in)
      (.addAllNotIn builder (get m :not-in)))
    (when (contains? m :example)
      (.addAllExample builder (get m :example)))

    (.build builder)))

(defn parse-bytes-rules
  "Parse a BytesRules protobuf message to a map."
  [^buf.validate.Validate$BytesRules proto]
  (cond-> {}
    ;; Regular fields
    (.hasConst proto) (assoc :const (.getConst proto))
    (.hasLen proto) (assoc :len (.getLen proto))
    (.hasMinLen proto) (assoc :min-len (.getMinLen proto))
    (.hasMaxLen proto) (assoc :max-len (.getMaxLen proto))
    (.hasPattern proto) (assoc :pattern (.getPattern proto))
    (.hasPrefix proto) (assoc :prefix (.getPrefix proto))
    (.hasSuffix proto) (assoc :suffix (.getSuffix proto))
    (.hasContains proto) (assoc :contains (.getContains proto))
    true (assoc :in (vec (.getInList proto)))
    true (assoc :not-in (vec (.getNotInList proto)))
    true (assoc :example (vec (.getExampleList proto)))

    ;; Oneof payload
    true (merge (parse-bytes-rules-payload proto))))

(defn build-enum-rules
  "Build a EnumRules protobuf message from a map."
  [m]
  (let [builder (buf.validate.Validate$EnumRules/newBuilder)]
    ;; Set regular fields
    (when (contains? m :const)
      (.setConst builder (get m :const)))
    (when (contains? m :defined-only)
      (.setDefinedOnly builder (get m :defined-only)))
    (when (contains? m :in)
      (.addAllIn builder (get m :in)))
    (when (contains? m :not-in)
      (.addAllNotIn builder (get m :not-in)))
    (when (contains? m :example)
      (.addAllExample builder (get m :example)))

    (.build builder)))

(defn parse-enum-rules
  "Parse a EnumRules protobuf message to a map."
  [^buf.validate.Validate$EnumRules proto]
  (cond-> {}
    ;; Regular fields
    (.hasConst proto) (assoc :const (.getConst proto))
    (.hasDefinedOnly proto) (assoc :defined-only (.getDefinedOnly proto))
    true (assoc :in (vec (.getInList proto)))
    true (assoc :not-in (vec (.getNotInList proto)))
    true (assoc :example (vec (.getExampleList proto)))))

(defn build-repeated-rules
  "Build a RepeatedRules protobuf message from a map."
  [m]
  (let [builder (buf.validate.Validate$RepeatedRules/newBuilder)]
    ;; Set regular fields
    (when (contains? m :min-items)
      (.setMinItems builder (get m :min-items)))
    (when (contains? m :max-items)
      (.setMaxItems builder (get m :max-items)))
    (when (contains? m :unique)
      (.setUnique builder (get m :unique)))
    (when (contains? m :items)
      (.setItems builder (get m :items)))

    (.build builder)))

(defn parse-repeated-rules
  "Parse a RepeatedRules protobuf message to a map."
  [^buf.validate.Validate$RepeatedRules proto]
  (cond-> {}
    ;; Regular fields
    (.hasMinItems proto) (assoc :min-items (.getMinItems proto))
    (.hasMaxItems proto) (assoc :max-items (.getMaxItems proto))
    (.hasUnique proto) (assoc :unique (.getUnique proto))
    (.hasItems proto) (assoc :items (.getItems proto))))

(defn build-map-rules
  "Build a MapRules protobuf message from a map."
  [m]
  (let [builder (buf.validate.Validate$MapRules/newBuilder)]
    ;; Set regular fields
    (when (contains? m :min-pairs)
      (.setMinPairs builder (get m :min-pairs)))
    (when (contains? m :max-pairs)
      (.setMaxPairs builder (get m :max-pairs)))
    (when (contains? m :keys)
      (.setKeys builder (get m :keys)))
    (when (contains? m :values)
      (.setValues builder (get m :values)))

    (.build builder)))

(defn parse-map-rules
  "Parse a MapRules protobuf message to a map."
  [^buf.validate.Validate$MapRules proto]
  (cond-> {}
    ;; Regular fields
    (.hasMinPairs proto) (assoc :min-pairs (.getMinPairs proto))
    (.hasMaxPairs proto) (assoc :max-pairs (.getMaxPairs proto))
    (.hasKeys proto) (assoc :keys (.getKeys proto))
    (.hasValues proto) (assoc :values (.getValues proto))))

(defn build-any-rules
  "Build a AnyRules protobuf message from a map."
  [m]
  (let [builder (buf.validate.Validate$AnyRules/newBuilder)]
    ;; Set regular fields
    (when (contains? m :in)
      (.addAllIn builder (get m :in)))
    (when (contains? m :not-in)
      (.addAllNotIn builder (get m :not-in)))

    (.build builder)))

(defn parse-any-rules
  "Parse a AnyRules protobuf message to a map."
  [^buf.validate.Validate$AnyRules proto]
  (cond-> {}
    ;; Regular fields
    true (assoc :in (vec (.getInList proto)))
    true (assoc :not-in (vec (.getNotInList proto)))))

(defn build-duration-rules
  "Build a DurationRules protobuf message from a map."
  [m]
  (let [builder (buf.validate.Validate$DurationRules/newBuilder)]
    ;; Set regular fields
    (when (contains? m :const)
      (.setConst builder (get m :const)))
    (when (contains? m :in)
      (.addAllIn builder (get m :in)))
    (when (contains? m :not-in)
      (.addAllNotIn builder (get m :not-in)))
    (when (contains? m :example)
      (.addAllExample builder (get m :example)))

    (.build builder)))

(defn parse-duration-rules
  "Parse a DurationRules protobuf message to a map."
  [^buf.validate.Validate$DurationRules proto]
  (cond-> {}
    ;; Regular fields
    (.hasConst proto) (assoc :const (.getConst proto))
    true (assoc :in (vec (.getInList proto)))
    true (assoc :not-in (vec (.getNotInList proto)))
    true (assoc :example (vec (.getExampleList proto)))

    ;; Oneof payload
    true (merge (parse-duration-rules-payload proto))))

(defn build-timestamp-rules
  "Build a TimestampRules protobuf message from a map."
  [m]
  (let [builder (buf.validate.Validate$TimestampRules/newBuilder)]
    ;; Set regular fields
    (when (contains? m :const)
      (.setConst builder (get m :const)))
    (when (contains? m :within)
      (.setWithin builder (get m :within)))
    (when (contains? m :example)
      (.addAllExample builder (get m :example)))

    (.build builder)))

(defn parse-timestamp-rules
  "Parse a TimestampRules protobuf message to a map."
  [^buf.validate.Validate$TimestampRules proto]
  (cond-> {}
    ;; Regular fields
    (.hasConst proto) (assoc :const (.getConst proto))
    (.hasWithin proto) (assoc :within (.getWithin proto))
    true (assoc :example (vec (.getExampleList proto)))

    ;; Oneof payload
    true (merge (parse-timestamp-rules-payload proto))))

(defn build-violations
  "Build a Violations protobuf message from a map."
  [m]
  (let [builder (buf.validate.Validate$Violations/newBuilder)]
    ;; Set regular fields
    (when (contains? m :violations)
      (.addAllViolations builder (get m :violations)))

    (.build builder)))

(defn parse-violations
  "Parse a Violations protobuf message to a map."
  [^buf.validate.Validate$Violations proto]
  (cond-> {}
    ;; Regular fields
    true (assoc :violations (vec (.getViolationsList proto)))))

(defn build-violation
  "Build a Violation protobuf message from a map."
  [m]
  (let [builder (buf.validate.Validate$Violation/newBuilder)]
    ;; Set regular fields
    (when (contains? m :field)
      (.setField builder (get m :field)))
    (when (contains? m :rule)
      (.setRule builder (get m :rule)))
    (when (contains? m :rule-id)
      (.setRuleId builder (get m :rule-id)))
    (when (contains? m :message)
      (.setMessage builder (get m :message)))
    (when (contains? m :for-key)
      (.setForKey builder (get m :for-key)))

    (.build builder)))

(defn parse-violation
  "Parse a Violation protobuf message to a map."
  [^buf.validate.Validate$Violation proto]
  (cond-> {}
    ;; Regular fields
    (.hasField proto) (assoc :field (.getField proto))
    (.hasRule proto) (assoc :rule (.getRule proto))
    (.hasRuleId proto) (assoc :rule-id (.getRuleId proto))
    (.hasMessage proto) (assoc :message (.getMessage proto))
    (.hasForKey proto) (assoc :for-key (.getForKey proto))))

(defn build-field-path
  "Build a FieldPath protobuf message from a map."
  [m]
  (let [builder (buf.validate.Validate$FieldPath/newBuilder)]
    ;; Set regular fields
    (when (contains? m :elements)
      (.addAllElements builder (get m :elements)))

    (.build builder)))

(defn parse-field-path
  "Parse a FieldPath protobuf message to a map."
  [^buf.validate.Validate$FieldPath proto]
  (cond-> {}
    ;; Regular fields
    true (assoc :elements (vec (.getElementsList proto)))))

(defn build-field-path-element
  "Build a FieldPathElement protobuf message from a map."
  [m]
  (let [builder (buf.validate.Validate$FieldPathElement/newBuilder)]
    ;; Set regular fields
    (when (contains? m :field-number)
      (.setFieldNumber builder (get m :field-number)))
    (when (contains? m :field-name)
      (.setFieldName builder (get m :field-name)))
    (when (contains? m :field-type)
      (.setFieldType builder (get m :field-type)))
    (when (contains? m :key-type)
      (.setKeyType builder (get m :key-type)))
    (when (contains? m :value-type)
      (.setValueType builder (get m :value-type)))

    (.build builder)))

(defn parse-field-path-element
  "Parse a FieldPathElement protobuf message to a map."
  [^buf.validate.Validate$FieldPathElement proto]
  (cond-> {}
    ;; Regular fields
    (.hasFieldNumber proto) (assoc :field-number (.getFieldNumber proto))
    (.hasFieldName proto) (assoc :field-name (.getFieldName proto))
    (.hasFieldType proto) (assoc :field-type (.getFieldType proto))
    (.hasKeyType proto) (assoc :key-type (.getKeyType proto))
    (.hasValueType proto) (assoc :value-type (.getValueType proto))

    ;; Oneof payload
    true (merge (parse-field-path-element-payload proto))))

(defn build-root
  "Build a Root protobuf message from a map."
  [m]
  (let [builder (cmd.Compass.Compass$Root/newBuilder)]

    (.build builder)))

(defn parse-root
  "Parse a Root protobuf message to a map."
  [^cmd.Compass.Compass$Root proto]
  (cond-> {}

;; Oneof payload
    true (merge (parse-root-payload proto))))

(defn build-start
  "Build a Start protobuf message from a map."
  [m]
  (let [builder (cmd.Compass.Compass$Start/newBuilder)]

    (.build builder)))

(defn parse-start
  "Parse a Start protobuf message to a map."
  [^cmd.Compass.Compass$Start proto]
  (cond-> {}))

(defn build-stop
  "Build a Stop protobuf message from a map."
  [m]
  (let [builder (cmd.Compass.Compass$Stop/newBuilder)]

    (.build builder)))

(defn parse-stop
  "Parse a Stop protobuf message to a map."
  [^cmd.Compass.Compass$Stop proto]
  (cond-> {}))

(defn build-next
  "Build a Next protobuf message from a map."
  [m]
  (let [builder (cmd.Compass.Compass$Next/newBuilder)]

    (.build builder)))

(defn parse-next
  "Parse a Next protobuf message to a map."
  [^cmd.Compass.Compass$Next proto]
  (cond-> {}))

(defn build-calibrate-start-long
  "Build a CalibrateStartLong protobuf message from a map."
  [m]
  (let [builder (cmd.Compass.Compass$CalibrateStartLong/newBuilder)]

    (.build builder)))

(defn parse-calibrate-start-long
  "Parse a CalibrateStartLong protobuf message to a map."
  [^cmd.Compass.Compass$CalibrateStartLong proto]
  (cond-> {}))

(defn build-calibrate-start-short
  "Build a CalibrateStartShort protobuf message from a map."
  [m]
  (let [builder (cmd.Compass.Compass$CalibrateStartShort/newBuilder)]

    (.build builder)))

(defn parse-calibrate-start-short
  "Parse a CalibrateStartShort protobuf message to a map."
  [^cmd.Compass.Compass$CalibrateStartShort proto]
  (cond-> {}))

(defn build-calibrate-next
  "Build a CalibrateNext protobuf message from a map."
  [m]
  (let [builder (cmd.Compass.Compass$CalibrateNext/newBuilder)]

    (.build builder)))

(defn parse-calibrate-next
  "Parse a CalibrateNext protobuf message to a map."
  [^cmd.Compass.Compass$CalibrateNext proto]
  (cond-> {}))

(defn build-calibrate-cencel
  "Build a CalibrateCencel protobuf message from a map."
  [m]
  (let [builder (cmd.Compass.Compass$CalibrateCencel/newBuilder)]

    (.build builder)))

(defn parse-calibrate-cencel
  "Parse a CalibrateCencel protobuf message to a map."
  [^cmd.Compass.Compass$CalibrateCencel proto]
  (cond-> {}))

(defn build-get-meteo
  "Build a GetMeteo protobuf message from a map."
  [m]
  (let [builder (cmd.Compass.Compass$GetMeteo/newBuilder)]

    (.build builder)))

(defn parse-get-meteo
  "Parse a GetMeteo protobuf message to a map."
  [^cmd.Compass.Compass$GetMeteo proto]
  (cond-> {}))

(defn build-set-magnetic-declination
  "Build a SetMagneticDeclination protobuf message from a map."
  [m]
  (let [builder (cmd.Compass.Compass$SetMagneticDeclination/newBuilder)]
    ;; Set regular fields
    (when (contains? m :value)
      (.setValue builder (get m :value)))

    (.build builder)))

(defn parse-set-magnetic-declination
  "Parse a SetMagneticDeclination protobuf message to a map."
  [^cmd.Compass.Compass$SetMagneticDeclination proto]
  (cond-> {}
    ;; Regular fields
    (.hasValue proto) (assoc :value (.getValue proto))))

(defn build-set-offset-angle-azimuth
  "Build a SetOffsetAngleAzimuth protobuf message from a map."
  [m]
  (let [builder (cmd.Compass.Compass$SetOffsetAngleAzimuth/newBuilder)]
    ;; Set regular fields
    (when (contains? m :value)
      (.setValue builder (get m :value)))

    (.build builder)))

(defn parse-set-offset-angle-azimuth
  "Parse a SetOffsetAngleAzimuth protobuf message to a map."
  [^cmd.Compass.Compass$SetOffsetAngleAzimuth proto]
  (cond-> {}
    ;; Regular fields
    (.hasValue proto) (assoc :value (.getValue proto))))

(defn build-set-offset-angle-elevation
  "Build a SetOffsetAngleElevation protobuf message from a map."
  [m]
  (let [builder (cmd.Compass.Compass$SetOffsetAngleElevation/newBuilder)]
    ;; Set regular fields
    (when (contains? m :value)
      (.setValue builder (get m :value)))

    (.build builder)))

(defn parse-set-offset-angle-elevation
  "Parse a SetOffsetAngleElevation protobuf message to a map."
  [^cmd.Compass.Compass$SetOffsetAngleElevation proto]
  (cond-> {}
    ;; Regular fields
    (.hasValue proto) (assoc :value (.getValue proto))))

(defn build-set-use-rotary-position
  "Build a SetUseRotaryPosition protobuf message from a map."
  [m]
  (let [builder (cmd.Compass.Compass$SetUseRotaryPosition/newBuilder)]
    ;; Set regular fields
    (when (contains? m :flag)
      (.setFlag builder (get m :flag)))

    (.build builder)))

(defn parse-set-use-rotary-position
  "Parse a SetUseRotaryPosition protobuf message to a map."
  [^cmd.Compass.Compass$SetUseRotaryPosition proto]
  (cond-> {}
    ;; Regular fields
    (.hasFlag proto) (assoc :flag (.getFlag proto))))

(defn build-root
  "Build a Root protobuf message from a map."
  [m]
  (let [builder (cmd.Gps.Gps$Root/newBuilder)]

    (.build builder)))

(defn parse-root
  "Parse a Root protobuf message to a map."
  [^cmd.Gps.Gps$Root proto]
  (cond-> {}

;; Oneof payload
    true (merge (parse-root-payload proto))))

(defn build-start
  "Build a Start protobuf message from a map."
  [m]
  (let [builder (cmd.Gps.Gps$Start/newBuilder)]

    (.build builder)))

(defn parse-start
  "Parse a Start protobuf message to a map."
  [^cmd.Gps.Gps$Start proto]
  (cond-> {}))

(defn build-stop
  "Build a Stop protobuf message from a map."
  [m]
  (let [builder (cmd.Gps.Gps$Stop/newBuilder)]

    (.build builder)))

(defn parse-stop
  "Parse a Stop protobuf message to a map."
  [^cmd.Gps.Gps$Stop proto]
  (cond-> {}))

(defn build-get-meteo
  "Build a GetMeteo protobuf message from a map."
  [m]
  (let [builder (cmd.Gps.Gps$GetMeteo/newBuilder)]

    (.build builder)))

(defn parse-get-meteo
  "Parse a GetMeteo protobuf message to a map."
  [^cmd.Gps.Gps$GetMeteo proto]
  (cond-> {}))

(defn build-set-use-manual-position
  "Build a SetUseManualPosition protobuf message from a map."
  [m]
  (let [builder (cmd.Gps.Gps$SetUseManualPosition/newBuilder)]
    ;; Set regular fields
    (when (contains? m :flag)
      (.setFlag builder (get m :flag)))

    (.build builder)))

(defn parse-set-use-manual-position
  "Parse a SetUseManualPosition protobuf message to a map."
  [^cmd.Gps.Gps$SetUseManualPosition proto]
  (cond-> {}
    ;; Regular fields
    (.hasFlag proto) (assoc :flag (.getFlag proto))))

(defn build-set-manual-position
  "Build a SetManualPosition protobuf message from a map."
  [m]
  (let [builder (cmd.Gps.Gps$SetManualPosition/newBuilder)]
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
  [^cmd.Gps.Gps$SetManualPosition proto]
  (cond-> {}
    ;; Regular fields
    (.hasLatitude proto) (assoc :latitude (.getLatitude proto))
    (.hasLongitude proto) (assoc :longitude (.getLongitude proto))
    (.hasAltitude proto) (assoc :altitude (.getAltitude proto))))

(defn build-jon-gui-data-meteo
  "Build a JonGuiDataMeteo protobuf message from a map."
  [m]
  (let [builder (ser.Ser$JonGuiDataMeteo/newBuilder)]
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
  [^ser.Ser$JonGuiDataMeteo proto]
  (cond-> {}
    ;; Regular fields
    (.hasTemperature proto) (assoc :temperature (.getTemperature proto))
    (.hasHumidity proto) (assoc :humidity (.getHumidity proto))
    (.hasPressure proto) (assoc :pressure (.getPressure proto))))

(defn build-root
  "Build a Root protobuf message from a map."
  [m]
  (let [builder (cmd.Lrf.Lrf$Root/newBuilder)]

    (.build builder)))

(defn parse-root
  "Parse a Root protobuf message to a map."
  [^cmd.Lrf.Lrf$Root proto]
  (cond-> {}

;; Oneof payload
    true (merge (parse-root-payload proto))))

(defn build-get-meteo
  "Build a GetMeteo protobuf message from a map."
  [m]
  (let [builder (cmd.Lrf.Lrf$GetMeteo/newBuilder)]

    (.build builder)))

(defn parse-get-meteo
  "Parse a GetMeteo protobuf message to a map."
  [^cmd.Lrf.Lrf$GetMeteo proto]
  (cond-> {}))

(defn build-start
  "Build a Start protobuf message from a map."
  [m]
  (let [builder (cmd.Lrf.Lrf$Start/newBuilder)]

    (.build builder)))

(defn parse-start
  "Parse a Start protobuf message to a map."
  [^cmd.Lrf.Lrf$Start proto]
  (cond-> {}))

(defn build-stop
  "Build a Stop protobuf message from a map."
  [m]
  (let [builder (cmd.Lrf.Lrf$Stop/newBuilder)]

    (.build builder)))

(defn parse-stop
  "Parse a Stop protobuf message to a map."
  [^cmd.Lrf.Lrf$Stop proto]
  (cond-> {}))

(defn build-measure
  "Build a Measure protobuf message from a map."
  [m]
  (let [builder (cmd.Lrf.Lrf$Measure/newBuilder)]

    (.build builder)))

(defn parse-measure
  "Parse a Measure protobuf message to a map."
  [^cmd.Lrf.Lrf$Measure proto]
  (cond-> {}))

(defn build-scan-on
  "Build a ScanOn protobuf message from a map."
  [m]
  (let [builder (cmd.Lrf.Lrf$ScanOn/newBuilder)]

    (.build builder)))

(defn parse-scan-on
  "Parse a ScanOn protobuf message to a map."
  [^cmd.Lrf.Lrf$ScanOn proto]
  (cond-> {}))

(defn build-scan-off
  "Build a ScanOff protobuf message from a map."
  [m]
  (let [builder (cmd.Lrf.Lrf$ScanOff/newBuilder)]

    (.build builder)))

(defn parse-scan-off
  "Parse a ScanOff protobuf message to a map."
  [^cmd.Lrf.Lrf$ScanOff proto]
  (cond-> {}))

(defn build-refine-off
  "Build a RefineOff protobuf message from a map."
  [m]
  (let [builder (cmd.Lrf.Lrf$RefineOff/newBuilder)]

    (.build builder)))

(defn parse-refine-off
  "Parse a RefineOff protobuf message to a map."
  [^cmd.Lrf.Lrf$RefineOff proto]
  (cond-> {}))

(defn build-refine-on
  "Build a RefineOn protobuf message from a map."
  [m]
  (let [builder (cmd.Lrf.Lrf$RefineOn/newBuilder)]

    (.build builder)))

(defn parse-refine-on
  "Parse a RefineOn protobuf message to a map."
  [^cmd.Lrf.Lrf$RefineOn proto]
  (cond-> {}))

(defn build-target-designator-off
  "Build a TargetDesignatorOff protobuf message from a map."
  [m]
  (let [builder (cmd.Lrf.Lrf$TargetDesignatorOff/newBuilder)]

    (.build builder)))

(defn parse-target-designator-off
  "Parse a TargetDesignatorOff protobuf message to a map."
  [^cmd.Lrf.Lrf$TargetDesignatorOff proto]
  (cond-> {}))

(defn build-target-designator-on-mode-a
  "Build a TargetDesignatorOnModeA protobuf message from a map."
  [m]
  (let [builder (cmd.Lrf.Lrf$TargetDesignatorOnModeA/newBuilder)]

    (.build builder)))

(defn parse-target-designator-on-mode-a
  "Parse a TargetDesignatorOnModeA protobuf message to a map."
  [^cmd.Lrf.Lrf$TargetDesignatorOnModeA proto]
  (cond-> {}))

(defn build-target-designator-on-mode-b
  "Build a TargetDesignatorOnModeB protobuf message from a map."
  [m]
  (let [builder (cmd.Lrf.Lrf$TargetDesignatorOnModeB/newBuilder)]

    (.build builder)))

(defn parse-target-designator-on-mode-b
  "Parse a TargetDesignatorOnModeB protobuf message to a map."
  [^cmd.Lrf.Lrf$TargetDesignatorOnModeB proto]
  (cond-> {}))

(defn build-enable-fog-mode
  "Build a EnableFogMode protobuf message from a map."
  [m]
  (let [builder (cmd.Lrf.Lrf$EnableFogMode/newBuilder)]

    (.build builder)))

(defn parse-enable-fog-mode
  "Parse a EnableFogMode protobuf message to a map."
  [^cmd.Lrf.Lrf$EnableFogMode proto]
  (cond-> {}))

(defn build-disable-fog-mode
  "Build a DisableFogMode protobuf message from a map."
  [m]
  (let [builder (cmd.Lrf.Lrf$DisableFogMode/newBuilder)]

    (.build builder)))

(defn parse-disable-fog-mode
  "Parse a DisableFogMode protobuf message to a map."
  [^cmd.Lrf.Lrf$DisableFogMode proto]
  (cond-> {}))

(defn build-set-scan-mode
  "Build a SetScanMode protobuf message from a map."
  [m]
  (let [builder (cmd.Lrf.Lrf$SetScanMode/newBuilder)]
    ;; Set regular fields
    (when (contains? m :mode)
      (.setMode builder (get m :mode)))

    (.build builder)))

(defn parse-set-scan-mode
  "Parse a SetScanMode protobuf message to a map."
  [^cmd.Lrf.Lrf$SetScanMode proto]
  (cond-> {}
    ;; Regular fields
    (.hasMode proto) (assoc :mode (.getMode proto))))

(defn build-new-session
  "Build a NewSession protobuf message from a map."
  [m]
  (let [builder (cmd.Lrf.Lrf$NewSession/newBuilder)]

    (.build builder)))

(defn parse-new-session
  "Parse a NewSession protobuf message to a map."
  [^cmd.Lrf.Lrf$NewSession proto]
  (cond-> {}))

(defn build-set-value
  "Build a SetValue protobuf message from a map."
  [m]
  (let [builder (cmd.DayCamera.DayCamera$SetValue/newBuilder)]
    ;; Set regular fields
    (when (contains? m :value)
      (.setValue builder (get m :value)))

    (.build builder)))

(defn parse-set-value
  "Parse a SetValue protobuf message to a map."
  [^cmd.DayCamera.DayCamera$SetValue proto]
  (cond-> {}
    ;; Regular fields
    (.hasValue proto) (assoc :value (.getValue proto))))

(defn build-move
  "Build a Move protobuf message from a map."
  [m]
  (let [builder (cmd.DayCamera.DayCamera$Move/newBuilder)]
    ;; Set regular fields
    (when (contains? m :target-value)
      (.setTargetValue builder (get m :target-value)))
    (when (contains? m :speed)
      (.setSpeed builder (get m :speed)))

    (.build builder)))

(defn parse-move
  "Parse a Move protobuf message to a map."
  [^cmd.DayCamera.DayCamera$Move proto]
  (cond-> {}
    ;; Regular fields
    (.hasTargetValue proto) (assoc :target-value (.getTargetValue proto))
    (.hasSpeed proto) (assoc :speed (.getSpeed proto))))

(defn build-offset
  "Build a Offset protobuf message from a map."
  [m]
  (let [builder (cmd.DayCamera.DayCamera$Offset/newBuilder)]
    ;; Set regular fields
    (when (contains? m :offset-value)
      (.setOffsetValue builder (get m :offset-value)))

    (.build builder)))

(defn parse-offset
  "Parse a Offset protobuf message to a map."
  [^cmd.DayCamera.DayCamera$Offset proto]
  (cond-> {}
    ;; Regular fields
    (.hasOffsetValue proto) (assoc :offset-value (.getOffsetValue proto))))

(defn build-set-clahe-level
  "Build a SetClaheLevel protobuf message from a map."
  [m]
  (let [builder (cmd.DayCamera.DayCamera$SetClaheLevel/newBuilder)]
    ;; Set regular fields
    (when (contains? m :value)
      (.setValue builder (get m :value)))

    (.build builder)))

(defn parse-set-clahe-level
  "Parse a SetClaheLevel protobuf message to a map."
  [^cmd.DayCamera.DayCamera$SetClaheLevel proto]
  (cond-> {}
    ;; Regular fields
    (.hasValue proto) (assoc :value (.getValue proto))))

(defn build-shift-clahe-level
  "Build a ShiftClaheLevel protobuf message from a map."
  [m]
  (let [builder (cmd.DayCamera.DayCamera$ShiftClaheLevel/newBuilder)]
    ;; Set regular fields
    (when (contains? m :value)
      (.setValue builder (get m :value)))

    (.build builder)))

(defn parse-shift-clahe-level
  "Parse a ShiftClaheLevel protobuf message to a map."
  [^cmd.DayCamera.DayCamera$ShiftClaheLevel proto]
  (cond-> {}
    ;; Regular fields
    (.hasValue proto) (assoc :value (.getValue proto))))

(defn build-root
  "Build a Root protobuf message from a map."
  [m]
  (let [builder (cmd.DayCamera.DayCamera$Root/newBuilder)]

    (.build builder)))

(defn parse-root
  "Parse a Root protobuf message to a map."
  [^cmd.DayCamera.DayCamera$Root proto]
  (cond-> {}

;; Oneof payload
    true (merge (parse-root-payload proto))))

(defn build-get-pos
  "Build a GetPos protobuf message from a map."
  [m]
  (let [builder (cmd.DayCamera.DayCamera$GetPos/newBuilder)]

    (.build builder)))

(defn parse-get-pos
  "Parse a GetPos protobuf message to a map."
  [^cmd.DayCamera.DayCamera$GetPos proto]
  (cond-> {}))

(defn build-next-fx-mode
  "Build a NextFxMode protobuf message from a map."
  [m]
  (let [builder (cmd.DayCamera.DayCamera$NextFxMode/newBuilder)]

    (.build builder)))

(defn parse-next-fx-mode
  "Parse a NextFxMode protobuf message to a map."
  [^cmd.DayCamera.DayCamera$NextFxMode proto]
  (cond-> {}))

(defn build-prev-fx-mode
  "Build a PrevFxMode protobuf message from a map."
  [m]
  (let [builder (cmd.DayCamera.DayCamera$PrevFxMode/newBuilder)]

    (.build builder)))

(defn parse-prev-fx-mode
  "Parse a PrevFxMode protobuf message to a map."
  [^cmd.DayCamera.DayCamera$PrevFxMode proto]
  (cond-> {}))

(defn build-refresh-fx-mode
  "Build a RefreshFxMode protobuf message from a map."
  [m]
  (let [builder (cmd.DayCamera.DayCamera$RefreshFxMode/newBuilder)]

    (.build builder)))

(defn parse-refresh-fx-mode
  "Parse a RefreshFxMode protobuf message to a map."
  [^cmd.DayCamera.DayCamera$RefreshFxMode proto]
  (cond-> {}))

(defn build-halt-all
  "Build a HaltAll protobuf message from a map."
  [m]
  (let [builder (cmd.DayCamera.DayCamera$HaltAll/newBuilder)]

    (.build builder)))

(defn parse-halt-all
  "Parse a HaltAll protobuf message to a map."
  [^cmd.DayCamera.DayCamera$HaltAll proto]
  (cond-> {}))

(defn build-set-fx-mode
  "Build a SetFxMode protobuf message from a map."
  [m]
  (let [builder (cmd.DayCamera.DayCamera$SetFxMode/newBuilder)]
    ;; Set regular fields
    (when (contains? m :mode)
      (.setMode builder (get m :mode)))

    (.build builder)))

(defn parse-set-fx-mode
  "Parse a SetFxMode protobuf message to a map."
  [^cmd.DayCamera.DayCamera$SetFxMode proto]
  (cond-> {}
    ;; Regular fields
    (.hasMode proto) (assoc :mode (.getMode proto))))

(defn build-set-digital-zoom-level
  "Build a SetDigitalZoomLevel protobuf message from a map."
  [m]
  (let [builder (cmd.DayCamera.DayCamera$SetDigitalZoomLevel/newBuilder)]
    ;; Set regular fields
    (when (contains? m :value)
      (.setValue builder (get m :value)))

    (.build builder)))

(defn parse-set-digital-zoom-level
  "Parse a SetDigitalZoomLevel protobuf message to a map."
  [^cmd.DayCamera.DayCamera$SetDigitalZoomLevel proto]
  (cond-> {}
    ;; Regular fields
    (.hasValue proto) (assoc :value (.getValue proto))))

(defn build-focus
  "Build a Focus protobuf message from a map."
  [m]
  (let [builder (cmd.DayCamera.DayCamera$Focus/newBuilder)]

    (.build builder)))

(defn parse-focus
  "Parse a Focus protobuf message to a map."
  [^cmd.DayCamera.DayCamera$Focus proto]
  (cond-> {}

;; Oneof payload
    true (merge (parse-focus-payload proto))))

(defn build-zoom
  "Build a Zoom protobuf message from a map."
  [m]
  (let [builder (cmd.DayCamera.DayCamera$Zoom/newBuilder)]

    (.build builder)))

(defn parse-zoom
  "Parse a Zoom protobuf message to a map."
  [^cmd.DayCamera.DayCamera$Zoom proto]
  (cond-> {}

;; Oneof payload
    true (merge (parse-zoom-payload proto))))

(defn build-next-zoom-table-pos
  "Build a NextZoomTablePos protobuf message from a map."
  [m]
  (let [builder (cmd.DayCamera.DayCamera$NextZoomTablePos/newBuilder)]

    (.build builder)))

(defn parse-next-zoom-table-pos
  "Parse a NextZoomTablePos protobuf message to a map."
  [^cmd.DayCamera.DayCamera$NextZoomTablePos proto]
  (cond-> {}))

(defn build-prev-zoom-table-pos
  "Build a PrevZoomTablePos protobuf message from a map."
  [m]
  (let [builder (cmd.DayCamera.DayCamera$PrevZoomTablePos/newBuilder)]

    (.build builder)))

(defn parse-prev-zoom-table-pos
  "Parse a PrevZoomTablePos protobuf message to a map."
  [^cmd.DayCamera.DayCamera$PrevZoomTablePos proto]
  (cond-> {}))

(defn build-set-iris
  "Build a SetIris protobuf message from a map."
  [m]
  (let [builder (cmd.DayCamera.DayCamera$SetIris/newBuilder)]
    ;; Set regular fields
    (when (contains? m :value)
      (.setValue builder (get m :value)))

    (.build builder)))

(defn parse-set-iris
  "Parse a SetIris protobuf message to a map."
  [^cmd.DayCamera.DayCamera$SetIris proto]
  (cond-> {}
    ;; Regular fields
    (.hasValue proto) (assoc :value (.getValue proto))))

(defn build-set-infra-red-filter
  "Build a SetInfraRedFilter protobuf message from a map."
  [m]
  (let [builder (cmd.DayCamera.DayCamera$SetInfraRedFilter/newBuilder)]
    ;; Set regular fields
    (when (contains? m :value)
      (.setValue builder (get m :value)))

    (.build builder)))

(defn parse-set-infra-red-filter
  "Parse a SetInfraRedFilter protobuf message to a map."
  [^cmd.DayCamera.DayCamera$SetInfraRedFilter proto]
  (cond-> {}
    ;; Regular fields
    (.hasValue proto) (assoc :value (.getValue proto))))

(defn build-set-auto-iris
  "Build a SetAutoIris protobuf message from a map."
  [m]
  (let [builder (cmd.DayCamera.DayCamera$SetAutoIris/newBuilder)]
    ;; Set regular fields
    (when (contains? m :value)
      (.setValue builder (get m :value)))

    (.build builder)))

(defn parse-set-auto-iris
  "Parse a SetAutoIris protobuf message to a map."
  [^cmd.DayCamera.DayCamera$SetAutoIris proto]
  (cond-> {}
    ;; Regular fields
    (.hasValue proto) (assoc :value (.getValue proto))))

(defn build-set-zoom-table-value
  "Build a SetZoomTableValue protobuf message from a map."
  [m]
  (let [builder (cmd.DayCamera.DayCamera$SetZoomTableValue/newBuilder)]
    ;; Set regular fields
    (when (contains? m :value)
      (.setValue builder (get m :value)))

    (.build builder)))

(defn parse-set-zoom-table-value
  "Parse a SetZoomTableValue protobuf message to a map."
  [^cmd.DayCamera.DayCamera$SetZoomTableValue proto]
  (cond-> {}
    ;; Regular fields
    (.hasValue proto) (assoc :value (.getValue proto))))

(defn build-stop
  "Build a Stop protobuf message from a map."
  [m]
  (let [builder (cmd.DayCamera.DayCamera$Stop/newBuilder)]

    (.build builder)))

(defn parse-stop
  "Parse a Stop protobuf message to a map."
  [^cmd.DayCamera.DayCamera$Stop proto]
  (cond-> {}))

(defn build-start
  "Build a Start protobuf message from a map."
  [m]
  (let [builder (cmd.DayCamera.DayCamera$Start/newBuilder)]

    (.build builder)))

(defn parse-start
  "Parse a Start protobuf message to a map."
  [^cmd.DayCamera.DayCamera$Start proto]
  (cond-> {}))

(defn build-photo
  "Build a Photo protobuf message from a map."
  [m]
  (let [builder (cmd.DayCamera.DayCamera$Photo/newBuilder)]

    (.build builder)))

(defn parse-photo
  "Parse a Photo protobuf message to a map."
  [^cmd.DayCamera.DayCamera$Photo proto]
  (cond-> {}))

(defn build-halt
  "Build a Halt protobuf message from a map."
  [m]
  (let [builder (cmd.DayCamera.DayCamera$Halt/newBuilder)]

    (.build builder)))

(defn parse-halt
  "Parse a Halt protobuf message to a map."
  [^cmd.DayCamera.DayCamera$Halt proto]
  (cond-> {}))

(defn build-get-meteo
  "Build a GetMeteo protobuf message from a map."
  [m]
  (let [builder (cmd.DayCamera.DayCamera$GetMeteo/newBuilder)]

    (.build builder)))

(defn parse-get-meteo
  "Parse a GetMeteo protobuf message to a map."
  [^cmd.DayCamera.DayCamera$GetMeteo proto]
  (cond-> {}))

(defn build-reset-zoom
  "Build a ResetZoom protobuf message from a map."
  [m]
  (let [builder (cmd.DayCamera.DayCamera$ResetZoom/newBuilder)]

    (.build builder)))

(defn parse-reset-zoom
  "Parse a ResetZoom protobuf message to a map."
  [^cmd.DayCamera.DayCamera$ResetZoom proto]
  (cond-> {}))

(defn build-reset-focus
  "Build a ResetFocus protobuf message from a map."
  [m]
  (let [builder (cmd.DayCamera.DayCamera$ResetFocus/newBuilder)]

    (.build builder)))

(defn parse-reset-focus
  "Parse a ResetFocus protobuf message to a map."
  [^cmd.DayCamera.DayCamera$ResetFocus proto]
  (cond-> {}))

(defn build-save-to-table
  "Build a SaveToTable protobuf message from a map."
  [m]
  (let [builder (cmd.DayCamera.DayCamera$SaveToTable/newBuilder)]

    (.build builder)))

(defn parse-save-to-table
  "Parse a SaveToTable protobuf message to a map."
  [^cmd.DayCamera.DayCamera$SaveToTable proto]
  (cond-> {}))

(defn build-save-to-table-focus
  "Build a SaveToTableFocus protobuf message from a map."
  [m]
  (let [builder (cmd.DayCamera.DayCamera$SaveToTableFocus/newBuilder)]

    (.build builder)))

(defn parse-save-to-table-focus
  "Parse a SaveToTableFocus protobuf message to a map."
  [^cmd.DayCamera.DayCamera$SaveToTableFocus proto]
  (cond-> {}))

(defn build-root
  "Build a Root protobuf message from a map."
  [m]
  (let [builder (cmd.HeatCamera.HeatCamera$Root/newBuilder)]

    (.build builder)))

(defn parse-root
  "Parse a Root protobuf message to a map."
  [^cmd.HeatCamera.HeatCamera$Root proto]
  (cond-> {}

;; Oneof payload
    true (merge (parse-root-payload proto))))

(defn build-set-fx-mode
  "Build a SetFxMode protobuf message from a map."
  [m]
  (let [builder (cmd.HeatCamera.HeatCamera$SetFxMode/newBuilder)]
    ;; Set regular fields
    (when (contains? m :mode)
      (.setMode builder (get m :mode)))

    (.build builder)))

(defn parse-set-fx-mode
  "Parse a SetFxMode protobuf message to a map."
  [^cmd.HeatCamera.HeatCamera$SetFxMode proto]
  (cond-> {}
    ;; Regular fields
    (.hasMode proto) (assoc :mode (.getMode proto))))

(defn build-set-clahe-level
  "Build a SetClaheLevel protobuf message from a map."
  [m]
  (let [builder (cmd.HeatCamera.HeatCamera$SetClaheLevel/newBuilder)]
    ;; Set regular fields
    (when (contains? m :value)
      (.setValue builder (get m :value)))

    (.build builder)))

(defn parse-set-clahe-level
  "Parse a SetClaheLevel protobuf message to a map."
  [^cmd.HeatCamera.HeatCamera$SetClaheLevel proto]
  (cond-> {}
    ;; Regular fields
    (.hasValue proto) (assoc :value (.getValue proto))))

(defn build-shift-clahe-level
  "Build a ShiftClaheLevel protobuf message from a map."
  [m]
  (let [builder (cmd.HeatCamera.HeatCamera$ShiftClaheLevel/newBuilder)]
    ;; Set regular fields
    (when (contains? m :value)
      (.setValue builder (get m :value)))

    (.build builder)))

(defn parse-shift-clahe-level
  "Parse a ShiftClaheLevel protobuf message to a map."
  [^cmd.HeatCamera.HeatCamera$ShiftClaheLevel proto]
  (cond-> {}
    ;; Regular fields
    (.hasValue proto) (assoc :value (.getValue proto))))

(defn build-next-fx-mode
  "Build a NextFxMode protobuf message from a map."
  [m]
  (let [builder (cmd.HeatCamera.HeatCamera$NextFxMode/newBuilder)]

    (.build builder)))

(defn parse-next-fx-mode
  "Parse a NextFxMode protobuf message to a map."
  [^cmd.HeatCamera.HeatCamera$NextFxMode proto]
  (cond-> {}))

(defn build-prev-fx-mode
  "Build a PrevFxMode protobuf message from a map."
  [m]
  (let [builder (cmd.HeatCamera.HeatCamera$PrevFxMode/newBuilder)]

    (.build builder)))

(defn parse-prev-fx-mode
  "Parse a PrevFxMode protobuf message to a map."
  [^cmd.HeatCamera.HeatCamera$PrevFxMode proto]
  (cond-> {}))

(defn build-refresh-fx-mode
  "Build a RefreshFxMode protobuf message from a map."
  [m]
  (let [builder (cmd.HeatCamera.HeatCamera$RefreshFxMode/newBuilder)]

    (.build builder)))

(defn parse-refresh-fx-mode
  "Parse a RefreshFxMode protobuf message to a map."
  [^cmd.HeatCamera.HeatCamera$RefreshFxMode proto]
  (cond-> {}))

(defn build-enable-dde
  "Build a EnableDDE protobuf message from a map."
  [m]
  (let [builder (cmd.HeatCamera.HeatCamera$EnableDDE/newBuilder)]

    (.build builder)))

(defn parse-enable-dde
  "Parse a EnableDDE protobuf message to a map."
  [^cmd.HeatCamera.HeatCamera$EnableDDE proto]
  (cond-> {}))

(defn build-disable-dde
  "Build a DisableDDE protobuf message from a map."
  [m]
  (let [builder (cmd.HeatCamera.HeatCamera$DisableDDE/newBuilder)]

    (.build builder)))

(defn parse-disable-dde
  "Parse a DisableDDE protobuf message to a map."
  [^cmd.HeatCamera.HeatCamera$DisableDDE proto]
  (cond-> {}))

(defn build-set-value
  "Build a SetValue protobuf message from a map."
  [m]
  (let [builder (cmd.HeatCamera.HeatCamera$SetValue/newBuilder)]
    ;; Set regular fields
    (when (contains? m :value)
      (.setValue builder (get m :value)))

    (.build builder)))

(defn parse-set-value
  "Parse a SetValue protobuf message to a map."
  [^cmd.HeatCamera.HeatCamera$SetValue proto]
  (cond-> {}
    ;; Regular fields
    (.hasValue proto) (assoc :value (.getValue proto))))

(defn build-set-dde-level
  "Build a SetDDELevel protobuf message from a map."
  [m]
  (let [builder (cmd.HeatCamera.HeatCamera$SetDDELevel/newBuilder)]
    ;; Set regular fields
    (when (contains? m :value)
      (.setValue builder (get m :value)))

    (.build builder)))

(defn parse-set-dde-level
  "Parse a SetDDELevel protobuf message to a map."
  [^cmd.HeatCamera.HeatCamera$SetDDELevel proto]
  (cond-> {}
    ;; Regular fields
    (.hasValue proto) (assoc :value (.getValue proto))))

(defn build-set-digital-zoom-level
  "Build a SetDigitalZoomLevel protobuf message from a map."
  [m]
  (let [builder (cmd.HeatCamera.HeatCamera$SetDigitalZoomLevel/newBuilder)]
    ;; Set regular fields
    (when (contains? m :value)
      (.setValue builder (get m :value)))

    (.build builder)))

(defn parse-set-digital-zoom-level
  "Parse a SetDigitalZoomLevel protobuf message to a map."
  [^cmd.HeatCamera.HeatCamera$SetDigitalZoomLevel proto]
  (cond-> {}
    ;; Regular fields
    (.hasValue proto) (assoc :value (.getValue proto))))

(defn build-shift-dde
  "Build a ShiftDDE protobuf message from a map."
  [m]
  (let [builder (cmd.HeatCamera.HeatCamera$ShiftDDE/newBuilder)]
    ;; Set regular fields
    (when (contains? m :value)
      (.setValue builder (get m :value)))

    (.build builder)))

(defn parse-shift-dde
  "Parse a ShiftDDE protobuf message to a map."
  [^cmd.HeatCamera.HeatCamera$ShiftDDE proto]
  (cond-> {}
    ;; Regular fields
    (.hasValue proto) (assoc :value (.getValue proto))))

(defn build-zoom-in
  "Build a ZoomIn protobuf message from a map."
  [m]
  (let [builder (cmd.HeatCamera.HeatCamera$ZoomIn/newBuilder)]

    (.build builder)))

(defn parse-zoom-in
  "Parse a ZoomIn protobuf message to a map."
  [^cmd.HeatCamera.HeatCamera$ZoomIn proto]
  (cond-> {}))

(defn build-zoom-out
  "Build a ZoomOut protobuf message from a map."
  [m]
  (let [builder (cmd.HeatCamera.HeatCamera$ZoomOut/newBuilder)]

    (.build builder)))

(defn parse-zoom-out
  "Parse a ZoomOut protobuf message to a map."
  [^cmd.HeatCamera.HeatCamera$ZoomOut proto]
  (cond-> {}))

(defn build-zoom-stop
  "Build a ZoomStop protobuf message from a map."
  [m]
  (let [builder (cmd.HeatCamera.HeatCamera$ZoomStop/newBuilder)]

    (.build builder)))

(defn parse-zoom-stop
  "Parse a ZoomStop protobuf message to a map."
  [^cmd.HeatCamera.HeatCamera$ZoomStop proto]
  (cond-> {}))

(defn build-focus-in
  "Build a FocusIn protobuf message from a map."
  [m]
  (let [builder (cmd.HeatCamera.HeatCamera$FocusIn/newBuilder)]

    (.build builder)))

(defn parse-focus-in
  "Parse a FocusIn protobuf message to a map."
  [^cmd.HeatCamera.HeatCamera$FocusIn proto]
  (cond-> {}))

(defn build-focus-out
  "Build a FocusOut protobuf message from a map."
  [m]
  (let [builder (cmd.HeatCamera.HeatCamera$FocusOut/newBuilder)]

    (.build builder)))

(defn parse-focus-out
  "Parse a FocusOut protobuf message to a map."
  [^cmd.HeatCamera.HeatCamera$FocusOut proto]
  (cond-> {}))

(defn build-focus-stop
  "Build a FocusStop protobuf message from a map."
  [m]
  (let [builder (cmd.HeatCamera.HeatCamera$FocusStop/newBuilder)]

    (.build builder)))

(defn parse-focus-stop
  "Parse a FocusStop protobuf message to a map."
  [^cmd.HeatCamera.HeatCamera$FocusStop proto]
  (cond-> {}))

(defn build-focus-step-plus
  "Build a FocusStepPlus protobuf message from a map."
  [m]
  (let [builder (cmd.HeatCamera.HeatCamera$FocusStepPlus/newBuilder)]

    (.build builder)))

(defn parse-focus-step-plus
  "Parse a FocusStepPlus protobuf message to a map."
  [^cmd.HeatCamera.HeatCamera$FocusStepPlus proto]
  (cond-> {}))

(defn build-focus-step-minus
  "Build a FocusStepMinus protobuf message from a map."
  [m]
  (let [builder (cmd.HeatCamera.HeatCamera$FocusStepMinus/newBuilder)]

    (.build builder)))

(defn parse-focus-step-minus
  "Parse a FocusStepMinus protobuf message to a map."
  [^cmd.HeatCamera.HeatCamera$FocusStepMinus proto]
  (cond-> {}))

(defn build-calibrate
  "Build a Calibrate protobuf message from a map."
  [m]
  (let [builder (cmd.HeatCamera.HeatCamera$Calibrate/newBuilder)]

    (.build builder)))

(defn parse-calibrate
  "Parse a Calibrate protobuf message to a map."
  [^cmd.HeatCamera.HeatCamera$Calibrate proto]
  (cond-> {}))

(defn build-zoom
  "Build a Zoom protobuf message from a map."
  [m]
  (let [builder (cmd.HeatCamera.HeatCamera$Zoom/newBuilder)]

    (.build builder)))

(defn parse-zoom
  "Parse a Zoom protobuf message to a map."
  [^cmd.HeatCamera.HeatCamera$Zoom proto]
  (cond-> {}

;; Oneof payload
    true (merge (parse-zoom-payload proto))))

(defn build-next-zoom-table-pos
  "Build a NextZoomTablePos protobuf message from a map."
  [m]
  (let [builder (cmd.HeatCamera.HeatCamera$NextZoomTablePos/newBuilder)]

    (.build builder)))

(defn parse-next-zoom-table-pos
  "Parse a NextZoomTablePos protobuf message to a map."
  [^cmd.HeatCamera.HeatCamera$NextZoomTablePos proto]
  (cond-> {}))

(defn build-prev-zoom-table-pos
  "Build a PrevZoomTablePos protobuf message from a map."
  [m]
  (let [builder (cmd.HeatCamera.HeatCamera$PrevZoomTablePos/newBuilder)]

    (.build builder)))

(defn parse-prev-zoom-table-pos
  "Parse a PrevZoomTablePos protobuf message to a map."
  [^cmd.HeatCamera.HeatCamera$PrevZoomTablePos proto]
  (cond-> {}))

(defn build-set-calib-mode
  "Build a SetCalibMode protobuf message from a map."
  [m]
  (let [builder (cmd.HeatCamera.HeatCamera$SetCalibMode/newBuilder)]

    (.build builder)))

(defn parse-set-calib-mode
  "Parse a SetCalibMode protobuf message to a map."
  [^cmd.HeatCamera.HeatCamera$SetCalibMode proto]
  (cond-> {}))

(defn build-set-zoom-table-value
  "Build a SetZoomTableValue protobuf message from a map."
  [m]
  (let [builder (cmd.HeatCamera.HeatCamera$SetZoomTableValue/newBuilder)]
    ;; Set regular fields
    (when (contains? m :value)
      (.setValue builder (get m :value)))

    (.build builder)))

(defn parse-set-zoom-table-value
  "Parse a SetZoomTableValue protobuf message to a map."
  [^cmd.HeatCamera.HeatCamera$SetZoomTableValue proto]
  (cond-> {}
    ;; Regular fields
    (.hasValue proto) (assoc :value (.getValue proto))))

(defn build-set-agc
  "Build a SetAGC protobuf message from a map."
  [m]
  (let [builder (cmd.HeatCamera.HeatCamera$SetAGC/newBuilder)]
    ;; Set regular fields
    (when (contains? m :value)
      (.setValue builder (get m :value)))

    (.build builder)))

(defn parse-set-agc
  "Parse a SetAGC protobuf message to a map."
  [^cmd.HeatCamera.HeatCamera$SetAGC proto]
  (cond-> {}
    ;; Regular fields
    (.hasValue proto) (assoc :value (.getValue proto))))

(defn build-set-filters
  "Build a SetFilters protobuf message from a map."
  [m]
  (let [builder (cmd.HeatCamera.HeatCamera$SetFilters/newBuilder)]
    ;; Set regular fields
    (when (contains? m :value)
      (.setValue builder (get m :value)))

    (.build builder)))

(defn parse-set-filters
  "Parse a SetFilters protobuf message to a map."
  [^cmd.HeatCamera.HeatCamera$SetFilters proto]
  (cond-> {}
    ;; Regular fields
    (.hasValue proto) (assoc :value (.getValue proto))))

(defn build-start
  "Build a Start protobuf message from a map."
  [m]
  (let [builder (cmd.HeatCamera.HeatCamera$Start/newBuilder)]

    (.build builder)))

(defn parse-start
  "Parse a Start protobuf message to a map."
  [^cmd.HeatCamera.HeatCamera$Start proto]
  (cond-> {}))

(defn build-stop
  "Build a Stop protobuf message from a map."
  [m]
  (let [builder (cmd.HeatCamera.HeatCamera$Stop/newBuilder)]

    (.build builder)))

(defn parse-stop
  "Parse a Stop protobuf message to a map."
  [^cmd.HeatCamera.HeatCamera$Stop proto]
  (cond-> {}))

(defn build-halt
  "Build a Halt protobuf message from a map."
  [m]
  (let [builder (cmd.HeatCamera.HeatCamera$Halt/newBuilder)]

    (.build builder)))

(defn parse-halt
  "Parse a Halt protobuf message to a map."
  [^cmd.HeatCamera.HeatCamera$Halt proto]
  (cond-> {}))

(defn build-photo
  "Build a Photo protobuf message from a map."
  [m]
  (let [builder (cmd.HeatCamera.HeatCamera$Photo/newBuilder)]

    (.build builder)))

(defn parse-photo
  "Parse a Photo protobuf message to a map."
  [^cmd.HeatCamera.HeatCamera$Photo proto]
  (cond-> {}))

(defn build-get-meteo
  "Build a GetMeteo protobuf message from a map."
  [m]
  (let [builder (cmd.HeatCamera.HeatCamera$GetMeteo/newBuilder)]

    (.build builder)))

(defn parse-get-meteo
  "Parse a GetMeteo protobuf message to a map."
  [^cmd.HeatCamera.HeatCamera$GetMeteo proto]
  (cond-> {}))

(defn build-set-auto-focus
  "Build a SetAutoFocus protobuf message from a map."
  [m]
  (let [builder (cmd.HeatCamera.HeatCamera$SetAutoFocus/newBuilder)]
    ;; Set regular fields
    (when (contains? m :value)
      (.setValue builder (get m :value)))

    (.build builder)))

(defn parse-set-auto-focus
  "Parse a SetAutoFocus protobuf message to a map."
  [^cmd.HeatCamera.HeatCamera$SetAutoFocus proto]
  (cond-> {}
    ;; Regular fields
    (.hasValue proto) (assoc :value (.getValue proto))))

(defn build-reset-zoom
  "Build a ResetZoom protobuf message from a map."
  [m]
  (let [builder (cmd.HeatCamera.HeatCamera$ResetZoom/newBuilder)]

    (.build builder)))

(defn parse-reset-zoom
  "Parse a ResetZoom protobuf message to a map."
  [^cmd.HeatCamera.HeatCamera$ResetZoom proto]
  (cond-> {}))

(defn build-save-to-table
  "Build a SaveToTable protobuf message from a map."
  [m]
  (let [builder (cmd.HeatCamera.HeatCamera$SaveToTable/newBuilder)]

    (.build builder)))

(defn parse-save-to-table
  "Parse a SaveToTable protobuf message to a map."
  [^cmd.HeatCamera.HeatCamera$SaveToTable proto]
  (cond-> {}))

(defn build-root
  "Build a Root protobuf message from a map."
  [m]
  (let [builder (cmd.RotaryPlatform.RotaryPlatform$Root/newBuilder)]

    (.build builder)))

(defn parse-root
  "Parse a Root protobuf message to a map."
  [^cmd.RotaryPlatform.RotaryPlatform$Root proto]
  (cond-> {}

;; Oneof payload
    true (merge (parse-root-payload proto))))

(defn build-axis
  "Build a Axis protobuf message from a map."
  [m]
  (let [builder (cmd.RotaryPlatform.RotaryPlatform$Axis/newBuilder)]
    ;; Set regular fields
    (when (contains? m :azimuth)
      (.setAzimuth builder (get m :azimuth)))
    (when (contains? m :elevation)
      (.setElevation builder (get m :elevation)))

    (.build builder)))

(defn parse-axis
  "Parse a Axis protobuf message to a map."
  [^cmd.RotaryPlatform.RotaryPlatform$Axis proto]
  (cond-> {}
    ;; Regular fields
    (.hasAzimuth proto) (assoc :azimuth (.getAzimuth proto))
    (.hasElevation proto) (assoc :elevation (.getElevation proto))))

(defn build-set-mode
  "Build a SetMode protobuf message from a map."
  [m]
  (let [builder (cmd.RotaryPlatform.RotaryPlatform$SetMode/newBuilder)]
    ;; Set regular fields
    (when (contains? m :mode)
      (.setMode builder (get m :mode)))

    (.build builder)))

(defn parse-set-mode
  "Parse a SetMode protobuf message to a map."
  [^cmd.RotaryPlatform.RotaryPlatform$SetMode proto]
  (cond-> {}
    ;; Regular fields
    (.hasMode proto) (assoc :mode (.getMode proto))))

(defn build-set-azimuth-value
  "Build a SetAzimuthValue protobuf message from a map."
  [m]
  (let [builder (cmd.RotaryPlatform.RotaryPlatform$SetAzimuthValue/newBuilder)]
    ;; Set regular fields
    (when (contains? m :value)
      (.setValue builder (get m :value)))
    (when (contains? m :direction)
      (.setDirection builder (get m :direction)))

    (.build builder)))

(defn parse-set-azimuth-value
  "Parse a SetAzimuthValue protobuf message to a map."
  [^cmd.RotaryPlatform.RotaryPlatform$SetAzimuthValue proto]
  (cond-> {}
    ;; Regular fields
    (.hasValue proto) (assoc :value (.getValue proto))
    (.hasDirection proto) (assoc :direction (.getDirection proto))))

(defn build-rotate-azimuth-to
  "Build a RotateAzimuthTo protobuf message from a map."
  [m]
  (let [builder (cmd.RotaryPlatform.RotaryPlatform$RotateAzimuthTo/newBuilder)]
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
  [^cmd.RotaryPlatform.RotaryPlatform$RotateAzimuthTo proto]
  (cond-> {}
    ;; Regular fields
    (.hasTargetValue proto) (assoc :target-value (.getTargetValue proto))
    (.hasSpeed proto) (assoc :speed (.getSpeed proto))
    (.hasDirection proto) (assoc :direction (.getDirection proto))))

(defn build-rotate-azimuth
  "Build a RotateAzimuth protobuf message from a map."
  [m]
  (let [builder (cmd.RotaryPlatform.RotaryPlatform$RotateAzimuth/newBuilder)]
    ;; Set regular fields
    (when (contains? m :speed)
      (.setSpeed builder (get m :speed)))
    (when (contains? m :direction)
      (.setDirection builder (get m :direction)))

    (.build builder)))

(defn parse-rotate-azimuth
  "Parse a RotateAzimuth protobuf message to a map."
  [^cmd.RotaryPlatform.RotaryPlatform$RotateAzimuth proto]
  (cond-> {}
    ;; Regular fields
    (.hasSpeed proto) (assoc :speed (.getSpeed proto))
    (.hasDirection proto) (assoc :direction (.getDirection proto))))

(defn build-rotate-elevation
  "Build a RotateElevation protobuf message from a map."
  [m]
  (let [builder (cmd.RotaryPlatform.RotaryPlatform$RotateElevation/newBuilder)]
    ;; Set regular fields
    (when (contains? m :speed)
      (.setSpeed builder (get m :speed)))
    (when (contains? m :direction)
      (.setDirection builder (get m :direction)))

    (.build builder)))

(defn parse-rotate-elevation
  "Parse a RotateElevation protobuf message to a map."
  [^cmd.RotaryPlatform.RotaryPlatform$RotateElevation proto]
  (cond-> {}
    ;; Regular fields
    (.hasSpeed proto) (assoc :speed (.getSpeed proto))
    (.hasDirection proto) (assoc :direction (.getDirection proto))))

(defn build-set-elevation-value
  "Build a SetElevationValue protobuf message from a map."
  [m]
  (let [builder (cmd.RotaryPlatform.RotaryPlatform$SetElevationValue/newBuilder)]
    ;; Set regular fields
    (when (contains? m :value)
      (.setValue builder (get m :value)))

    (.build builder)))

(defn parse-set-elevation-value
  "Parse a SetElevationValue protobuf message to a map."
  [^cmd.RotaryPlatform.RotaryPlatform$SetElevationValue proto]
  (cond-> {}
    ;; Regular fields
    (.hasValue proto) (assoc :value (.getValue proto))))

(defn build-rotate-elevation-to
  "Build a RotateElevationTo protobuf message from a map."
  [m]
  (let [builder (cmd.RotaryPlatform.RotaryPlatform$RotateElevationTo/newBuilder)]
    ;; Set regular fields
    (when (contains? m :target-value)
      (.setTargetValue builder (get m :target-value)))
    (when (contains? m :speed)
      (.setSpeed builder (get m :speed)))

    (.build builder)))

(defn parse-rotate-elevation-to
  "Parse a RotateElevationTo protobuf message to a map."
  [^cmd.RotaryPlatform.RotaryPlatform$RotateElevationTo proto]
  (cond-> {}
    ;; Regular fields
    (.hasTargetValue proto) (assoc :target-value (.getTargetValue proto))
    (.hasSpeed proto) (assoc :speed (.getSpeed proto))))

(defn build-rotate-elevation-relative
  "Build a RotateElevationRelative protobuf message from a map."
  [m]
  (let [builder (cmd.RotaryPlatform.RotaryPlatform$RotateElevationRelative/newBuilder)]
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
  [^cmd.RotaryPlatform.RotaryPlatform$RotateElevationRelative proto]
  (cond-> {}
    ;; Regular fields
    (.hasValue proto) (assoc :value (.getValue proto))
    (.hasSpeed proto) (assoc :speed (.getSpeed proto))
    (.hasDirection proto) (assoc :direction (.getDirection proto))))

(defn build-rotate-elevation-relative-set
  "Build a RotateElevationRelativeSet protobuf message from a map."
  [m]
  (let [builder (cmd.RotaryPlatform.RotaryPlatform$RotateElevationRelativeSet/newBuilder)]
    ;; Set regular fields
    (when (contains? m :value)
      (.setValue builder (get m :value)))
    (when (contains? m :direction)
      (.setDirection builder (get m :direction)))

    (.build builder)))

(defn parse-rotate-elevation-relative-set
  "Parse a RotateElevationRelativeSet protobuf message to a map."
  [^cmd.RotaryPlatform.RotaryPlatform$RotateElevationRelativeSet proto]
  (cond-> {}
    ;; Regular fields
    (.hasValue proto) (assoc :value (.getValue proto))
    (.hasDirection proto) (assoc :direction (.getDirection proto))))

(defn build-rotate-azimuth-relative
  "Build a RotateAzimuthRelative protobuf message from a map."
  [m]
  (let [builder (cmd.RotaryPlatform.RotaryPlatform$RotateAzimuthRelative/newBuilder)]
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
  [^cmd.RotaryPlatform.RotaryPlatform$RotateAzimuthRelative proto]
  (cond-> {}
    ;; Regular fields
    (.hasValue proto) (assoc :value (.getValue proto))
    (.hasSpeed proto) (assoc :speed (.getSpeed proto))
    (.hasDirection proto) (assoc :direction (.getDirection proto))))

(defn build-rotate-azimuth-relative-set
  "Build a RotateAzimuthRelativeSet protobuf message from a map."
  [m]
  (let [builder (cmd.RotaryPlatform.RotaryPlatform$RotateAzimuthRelativeSet/newBuilder)]
    ;; Set regular fields
    (when (contains? m :value)
      (.setValue builder (get m :value)))
    (when (contains? m :direction)
      (.setDirection builder (get m :direction)))

    (.build builder)))

(defn parse-rotate-azimuth-relative-set
  "Parse a RotateAzimuthRelativeSet protobuf message to a map."
  [^cmd.RotaryPlatform.RotaryPlatform$RotateAzimuthRelativeSet proto]
  (cond-> {}
    ;; Regular fields
    (.hasValue proto) (assoc :value (.getValue proto))
    (.hasDirection proto) (assoc :direction (.getDirection proto))))

(defn build-set-platform-azimuth
  "Build a SetPlatformAzimuth protobuf message from a map."
  [m]
  (let [builder (cmd.RotaryPlatform.RotaryPlatform$SetPlatformAzimuth/newBuilder)]
    ;; Set regular fields
    (when (contains? m :value)
      (.setValue builder (get m :value)))

    (.build builder)))

(defn parse-set-platform-azimuth
  "Parse a SetPlatformAzimuth protobuf message to a map."
  [^cmd.RotaryPlatform.RotaryPlatform$SetPlatformAzimuth proto]
  (cond-> {}
    ;; Regular fields
    (.hasValue proto) (assoc :value (.getValue proto))))

(defn build-set-platform-elevation
  "Build a SetPlatformElevation protobuf message from a map."
  [m]
  (let [builder (cmd.RotaryPlatform.RotaryPlatform$SetPlatformElevation/newBuilder)]
    ;; Set regular fields
    (when (contains? m :value)
      (.setValue builder (get m :value)))

    (.build builder)))

(defn parse-set-platform-elevation
  "Parse a SetPlatformElevation protobuf message to a map."
  [^cmd.RotaryPlatform.RotaryPlatform$SetPlatformElevation proto]
  (cond-> {}
    ;; Regular fields
    (.hasValue proto) (assoc :value (.getValue proto))))

(defn build-set-platform-bank
  "Build a SetPlatformBank protobuf message from a map."
  [m]
  (let [builder (cmd.RotaryPlatform.RotaryPlatform$SetPlatformBank/newBuilder)]
    ;; Set regular fields
    (when (contains? m :value)
      (.setValue builder (get m :value)))

    (.build builder)))

(defn parse-set-platform-bank
  "Parse a SetPlatformBank protobuf message to a map."
  [^cmd.RotaryPlatform.RotaryPlatform$SetPlatformBank proto]
  (cond-> {}
    ;; Regular fields
    (.hasValue proto) (assoc :value (.getValue proto))))

(defn build-get-meteo
  "Build a GetMeteo protobuf message from a map."
  [m]
  (let [builder (cmd.RotaryPlatform.RotaryPlatform$GetMeteo/newBuilder)]

    (.build builder)))

(defn parse-get-meteo
  "Parse a GetMeteo protobuf message to a map."
  [^cmd.RotaryPlatform.RotaryPlatform$GetMeteo proto]
  (cond-> {}))

(defn build-azimuth
  "Build a Azimuth protobuf message from a map."
  [m]
  (let [builder (cmd.RotaryPlatform.RotaryPlatform$Azimuth/newBuilder)]

    (.build builder)))

(defn parse-azimuth
  "Parse a Azimuth protobuf message to a map."
  [^cmd.RotaryPlatform.RotaryPlatform$Azimuth proto]
  (cond-> {}

;; Oneof payload
    true (merge (parse-azimuth-payload proto))))

(defn build-start
  "Build a Start protobuf message from a map."
  [m]
  (let [builder (cmd.RotaryPlatform.RotaryPlatform$Start/newBuilder)]

    (.build builder)))

(defn parse-start
  "Parse a Start protobuf message to a map."
  [^cmd.RotaryPlatform.RotaryPlatform$Start proto]
  (cond-> {}))

(defn build-stop
  "Build a Stop protobuf message from a map."
  [m]
  (let [builder (cmd.RotaryPlatform.RotaryPlatform$Stop/newBuilder)]

    (.build builder)))

(defn parse-stop
  "Parse a Stop protobuf message to a map."
  [^cmd.RotaryPlatform.RotaryPlatform$Stop proto]
  (cond-> {}))

(defn build-halt
  "Build a Halt protobuf message from a map."
  [m]
  (let [builder (cmd.RotaryPlatform.RotaryPlatform$Halt/newBuilder)]

    (.build builder)))

(defn parse-halt
  "Parse a Halt protobuf message to a map."
  [^cmd.RotaryPlatform.RotaryPlatform$Halt proto]
  (cond-> {}))

(defn build-scan-start
  "Build a ScanStart protobuf message from a map."
  [m]
  (let [builder (cmd.RotaryPlatform.RotaryPlatform$ScanStart/newBuilder)]

    (.build builder)))

(defn parse-scan-start
  "Parse a ScanStart protobuf message to a map."
  [^cmd.RotaryPlatform.RotaryPlatform$ScanStart proto]
  (cond-> {}))

(defn build-scan-stop
  "Build a ScanStop protobuf message from a map."
  [m]
  (let [builder (cmd.RotaryPlatform.RotaryPlatform$ScanStop/newBuilder)]

    (.build builder)))

(defn parse-scan-stop
  "Parse a ScanStop protobuf message to a map."
  [^cmd.RotaryPlatform.RotaryPlatform$ScanStop proto]
  (cond-> {}))

(defn build-scan-pause
  "Build a ScanPause protobuf message from a map."
  [m]
  (let [builder (cmd.RotaryPlatform.RotaryPlatform$ScanPause/newBuilder)]

    (.build builder)))

(defn parse-scan-pause
  "Parse a ScanPause protobuf message to a map."
  [^cmd.RotaryPlatform.RotaryPlatform$ScanPause proto]
  (cond-> {}))

(defn build-scan-unpause
  "Build a ScanUnpause protobuf message from a map."
  [m]
  (let [builder (cmd.RotaryPlatform.RotaryPlatform$ScanUnpause/newBuilder)]

    (.build builder)))

(defn parse-scan-unpause
  "Parse a ScanUnpause protobuf message to a map."
  [^cmd.RotaryPlatform.RotaryPlatform$ScanUnpause proto]
  (cond-> {}))

(defn build-halt-azimuth
  "Build a HaltAzimuth protobuf message from a map."
  [m]
  (let [builder (cmd.RotaryPlatform.RotaryPlatform$HaltAzimuth/newBuilder)]

    (.build builder)))

(defn parse-halt-azimuth
  "Parse a HaltAzimuth protobuf message to a map."
  [^cmd.RotaryPlatform.RotaryPlatform$HaltAzimuth proto]
  (cond-> {}))

(defn build-halt-elevation
  "Build a HaltElevation protobuf message from a map."
  [m]
  (let [builder (cmd.RotaryPlatform.RotaryPlatform$HaltElevation/newBuilder)]

    (.build builder)))

(defn parse-halt-elevation
  "Parse a HaltElevation protobuf message to a map."
  [^cmd.RotaryPlatform.RotaryPlatform$HaltElevation proto]
  (cond-> {}))

(defn build-scan-prev
  "Build a ScanPrev protobuf message from a map."
  [m]
  (let [builder (cmd.RotaryPlatform.RotaryPlatform$ScanPrev/newBuilder)]

    (.build builder)))

(defn parse-scan-prev
  "Parse a ScanPrev protobuf message to a map."
  [^cmd.RotaryPlatform.RotaryPlatform$ScanPrev proto]
  (cond-> {}))

(defn build-scan-next
  "Build a ScanNext protobuf message from a map."
  [m]
  (let [builder (cmd.RotaryPlatform.RotaryPlatform$ScanNext/newBuilder)]

    (.build builder)))

(defn parse-scan-next
  "Parse a ScanNext protobuf message to a map."
  [^cmd.RotaryPlatform.RotaryPlatform$ScanNext proto]
  (cond-> {}))

(defn build-scan-refresh-node-list
  "Build a ScanRefreshNodeList protobuf message from a map."
  [m]
  (let [builder (cmd.RotaryPlatform.RotaryPlatform$ScanRefreshNodeList/newBuilder)]

    (.build builder)))

(defn parse-scan-refresh-node-list
  "Parse a ScanRefreshNodeList protobuf message to a map."
  [^cmd.RotaryPlatform.RotaryPlatform$ScanRefreshNodeList proto]
  (cond-> {}))

(defn build-scan-select-node
  "Build a ScanSelectNode protobuf message from a map."
  [m]
  (let [builder (cmd.RotaryPlatform.RotaryPlatform$ScanSelectNode/newBuilder)]
    ;; Set regular fields
    (when (contains? m :index)
      (.setIndex builder (get m :index)))

    (.build builder)))

(defn parse-scan-select-node
  "Parse a ScanSelectNode protobuf message to a map."
  [^cmd.RotaryPlatform.RotaryPlatform$ScanSelectNode proto]
  (cond-> {}
    ;; Regular fields
    (.hasIndex proto) (assoc :index (.getIndex proto))))

(defn build-scan-delete-node
  "Build a ScanDeleteNode protobuf message from a map."
  [m]
  (let [builder (cmd.RotaryPlatform.RotaryPlatform$ScanDeleteNode/newBuilder)]
    ;; Set regular fields
    (when (contains? m :index)
      (.setIndex builder (get m :index)))

    (.build builder)))

(defn parse-scan-delete-node
  "Parse a ScanDeleteNode protobuf message to a map."
  [^cmd.RotaryPlatform.RotaryPlatform$ScanDeleteNode proto]
  (cond-> {}
    ;; Regular fields
    (.hasIndex proto) (assoc :index (.getIndex proto))))

(defn build-scan-update-node
  "Build a ScanUpdateNode protobuf message from a map."
  [m]
  (let [builder (cmd.RotaryPlatform.RotaryPlatform$ScanUpdateNode/newBuilder)]
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
  [^cmd.RotaryPlatform.RotaryPlatform$ScanUpdateNode proto]
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
  (let [builder (cmd.RotaryPlatform.RotaryPlatform$ScanAddNode/newBuilder)]
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
  [^cmd.RotaryPlatform.RotaryPlatform$ScanAddNode proto]
  (cond-> {}
    ;; Regular fields
    (.hasIndex proto) (assoc :index (.getIndex proto))
    (.hasDayZoomTableValue proto) (assoc :day-zoom-table-value (.getDayZoomTableValue proto))
    (.hasHeatZoomTableValue proto) (assoc :heat-zoom-table-value (.getHeatZoomTableValue proto))
    (.hasAzimuth proto) (assoc :azimuth (.getAzimuth proto))
    (.hasElevation proto) (assoc :elevation (.getElevation proto))
    (.hasLinger proto) (assoc :linger (.getLinger proto))
    (.hasSpeed proto) (assoc :speed (.getSpeed proto))))

(defn build-elevation
  "Build a Elevation protobuf message from a map."
  [m]
  (let [builder (cmd.RotaryPlatform.RotaryPlatform$Elevation/newBuilder)]

    (.build builder)))

(defn parse-elevation
  "Parse a Elevation protobuf message to a map."
  [^cmd.RotaryPlatform.RotaryPlatform$Elevation proto]
  (cond-> {}

;; Oneof payload
    true (merge (parse-elevation-payload proto))))

(defn build-set-use-rotary-as-compass
  "Build a setUseRotaryAsCompass protobuf message from a map."
  [m]
  (let [builder (cmd.RotaryPlatform.RotaryPlatform$setUseRotaryAsCompass/newBuilder)]
    ;; Set regular fields
    (when (contains? m :flag)
      (.setFlag builder (get m :flag)))

    (.build builder)))

(defn parse-set-use-rotary-as-compass
  "Parse a setUseRotaryAsCompass protobuf message to a map."
  [^cmd.RotaryPlatform.RotaryPlatform$setUseRotaryAsCompass proto]
  (cond-> {}
    ;; Regular fields
    (.hasFlag proto) (assoc :flag (.getFlag proto))))

(defn build-rotate-to-gps
  "Build a RotateToGPS protobuf message from a map."
  [m]
  (let [builder (cmd.RotaryPlatform.RotaryPlatform$RotateToGPS/newBuilder)]
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
  [^cmd.RotaryPlatform.RotaryPlatform$RotateToGPS proto]
  (cond-> {}
    ;; Regular fields
    (.hasLatitude proto) (assoc :latitude (.getLatitude proto))
    (.hasLongitude proto) (assoc :longitude (.getLongitude proto))
    (.hasAltitude proto) (assoc :altitude (.getAltitude proto))))

(defn build-set-origin-gps
  "Build a SetOriginGPS protobuf message from a map."
  [m]
  (let [builder (cmd.RotaryPlatform.RotaryPlatform$SetOriginGPS/newBuilder)]
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
  [^cmd.RotaryPlatform.RotaryPlatform$SetOriginGPS proto]
  (cond-> {}
    ;; Regular fields
    (.hasLatitude proto) (assoc :latitude (.getLatitude proto))
    (.hasLongitude proto) (assoc :longitude (.getLongitude proto))
    (.hasAltitude proto) (assoc :altitude (.getAltitude proto))))

(defn build-rotate-to-ndc
  "Build a RotateToNDC protobuf message from a map."
  [m]
  (let [builder (cmd.RotaryPlatform.RotaryPlatform$RotateToNDC/newBuilder)]
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
  [^cmd.RotaryPlatform.RotaryPlatform$RotateToNDC proto]
  (cond-> {}
    ;; Regular fields
    (.hasChannel proto) (assoc :channel (.getChannel proto))
    (.hasX proto) (assoc :x (.getX proto))
    (.hasY proto) (assoc :y (.getY proto))))

(defn build-root
  "Build a Root protobuf message from a map."
  [m]
  (let [builder (cmd.OSD.Osd$Root/newBuilder)]

    (.build builder)))

(defn parse-root
  "Parse a Root protobuf message to a map."
  [^cmd.OSD.Osd$Root proto]
  (cond-> {}

;; Oneof payload
    true (merge (parse-root-payload proto))))

(defn build-show-default-screen
  "Build a ShowDefaultScreen protobuf message from a map."
  [m]
  (let [builder (cmd.OSD.Osd$ShowDefaultScreen/newBuilder)]

    (.build builder)))

(defn parse-show-default-screen
  "Parse a ShowDefaultScreen protobuf message to a map."
  [^cmd.OSD.Osd$ShowDefaultScreen proto]
  (cond-> {}))

(defn build-show-lrf-measure-screen
  "Build a ShowLRFMeasureScreen protobuf message from a map."
  [m]
  (let [builder (cmd.OSD.Osd$ShowLRFMeasureScreen/newBuilder)]

    (.build builder)))

(defn parse-show-lrf-measure-screen
  "Parse a ShowLRFMeasureScreen protobuf message to a map."
  [^cmd.OSD.Osd$ShowLRFMeasureScreen proto]
  (cond-> {}))

(defn build-show-lrf-result-screen
  "Build a ShowLRFResultScreen protobuf message from a map."
  [m]
  (let [builder (cmd.OSD.Osd$ShowLRFResultScreen/newBuilder)]

    (.build builder)))

(defn parse-show-lrf-result-screen
  "Parse a ShowLRFResultScreen protobuf message to a map."
  [^cmd.OSD.Osd$ShowLRFResultScreen proto]
  (cond-> {}))

(defn build-show-lrf-result-simplified-screen
  "Build a ShowLRFResultSimplifiedScreen protobuf message from a map."
  [m]
  (let [builder (cmd.OSD.Osd$ShowLRFResultSimplifiedScreen/newBuilder)]

    (.build builder)))

(defn parse-show-lrf-result-simplified-screen
  "Parse a ShowLRFResultSimplifiedScreen protobuf message to a map."
  [^cmd.OSD.Osd$ShowLRFResultSimplifiedScreen proto]
  (cond-> {}))

(defn build-enable-heat-osd
  "Build a EnableHeatOSD protobuf message from a map."
  [m]
  (let [builder (cmd.OSD.Osd$EnableHeatOSD/newBuilder)]

    (.build builder)))

(defn parse-enable-heat-osd
  "Parse a EnableHeatOSD protobuf message to a map."
  [^cmd.OSD.Osd$EnableHeatOSD proto]
  (cond-> {}))

(defn build-disable-heat-osd
  "Build a DisableHeatOSD protobuf message from a map."
  [m]
  (let [builder (cmd.OSD.Osd$DisableHeatOSD/newBuilder)]

    (.build builder)))

(defn parse-disable-heat-osd
  "Parse a DisableHeatOSD protobuf message to a map."
  [^cmd.OSD.Osd$DisableHeatOSD proto]
  (cond-> {}))

(defn build-enable-day-osd
  "Build a EnableDayOSD protobuf message from a map."
  [m]
  (let [builder (cmd.OSD.Osd$EnableDayOSD/newBuilder)]

    (.build builder)))

(defn parse-enable-day-osd
  "Parse a EnableDayOSD protobuf message to a map."
  [^cmd.OSD.Osd$EnableDayOSD proto]
  (cond-> {}))

(defn build-disable-day-osd
  "Build a DisableDayOSD protobuf message from a map."
  [m]
  (let [builder (cmd.OSD.Osd$DisableDayOSD/newBuilder)]

    (.build builder)))

(defn parse-disable-day-osd
  "Parse a DisableDayOSD protobuf message to a map."
  [^cmd.OSD.Osd$DisableDayOSD proto]
  (cond-> {}))

(defn build-root
  "Build a Root protobuf message from a map."
  [m]
  (let [builder (cmd.Lrf_calib.LrfCalib$Root/newBuilder)]

    (.build builder)))

(defn parse-root
  "Parse a Root protobuf message to a map."
  [^cmd.Lrf_calib.LrfCalib$Root proto]
  (cond-> {}

;; Oneof payload
    true (merge (parse-root-payload proto))))

(defn build-offsets
  "Build a Offsets protobuf message from a map."
  [m]
  (let [builder (cmd.Lrf_calib.LrfCalib$Offsets/newBuilder)]

    (.build builder)))

(defn parse-offsets
  "Parse a Offsets protobuf message to a map."
  [^cmd.Lrf_calib.LrfCalib$Offsets proto]
  (cond-> {}

;; Oneof payload
    true (merge (parse-offsets-payload proto))))

(defn build-set-offsets
  "Build a SetOffsets protobuf message from a map."
  [m]
  (let [builder (cmd.Lrf_calib.LrfCalib$SetOffsets/newBuilder)]
    ;; Set regular fields
    (when (contains? m :x)
      (.setX builder (get m :x)))
    (when (contains? m :y)
      (.setY builder (get m :y)))

    (.build builder)))

(defn parse-set-offsets
  "Parse a SetOffsets protobuf message to a map."
  [^cmd.Lrf_calib.LrfCalib$SetOffsets proto]
  (cond-> {}
    ;; Regular fields
    (.hasX proto) (assoc :x (.getX proto))
    (.hasY proto) (assoc :y (.getY proto))))

(defn build-shift-offsets-by
  "Build a ShiftOffsetsBy protobuf message from a map."
  [m]
  (let [builder (cmd.Lrf_calib.LrfCalib$ShiftOffsetsBy/newBuilder)]
    ;; Set regular fields
    (when (contains? m :x)
      (.setX builder (get m :x)))
    (when (contains? m :y)
      (.setY builder (get m :y)))

    (.build builder)))

(defn parse-shift-offsets-by
  "Parse a ShiftOffsetsBy protobuf message to a map."
  [^cmd.Lrf_calib.LrfCalib$ShiftOffsetsBy proto]
  (cond-> {}
    ;; Regular fields
    (.hasX proto) (assoc :x (.getX proto))
    (.hasY proto) (assoc :y (.getY proto))))

(defn build-reset-offsets
  "Build a ResetOffsets protobuf message from a map."
  [m]
  (let [builder (cmd.Lrf_calib.LrfCalib$ResetOffsets/newBuilder)]

    (.build builder)))

(defn parse-reset-offsets
  "Parse a ResetOffsets protobuf message to a map."
  [^cmd.Lrf_calib.LrfCalib$ResetOffsets proto]
  (cond-> {}))

(defn build-save-offsets
  "Build a SaveOffsets protobuf message from a map."
  [m]
  (let [builder (cmd.Lrf_calib.LrfCalib$SaveOffsets/newBuilder)]

    (.build builder)))

(defn parse-save-offsets
  "Parse a SaveOffsets protobuf message to a map."
  [^cmd.Lrf_calib.LrfCalib$SaveOffsets proto]
  (cond-> {}))

(defn build-root
  "Build a Root protobuf message from a map."
  [m]
  (let [builder (cmd.System.System$Root/newBuilder)]

    (.build builder)))

(defn parse-root
  "Parse a Root protobuf message to a map."
  [^cmd.System.System$Root proto]
  (cond-> {}

;; Oneof payload
    true (merge (parse-root-payload proto))))

(defn build-start-a-ll
  "Build a StartALl protobuf message from a map."
  [m]
  (let [builder (cmd.System.System$StartALl/newBuilder)]

    (.build builder)))

(defn parse-start-a-ll
  "Parse a StartALl protobuf message to a map."
  [^cmd.System.System$StartALl proto]
  (cond-> {}))

(defn build-stop-a-ll
  "Build a StopALl protobuf message from a map."
  [m]
  (let [builder (cmd.System.System$StopALl/newBuilder)]

    (.build builder)))

(defn parse-stop-a-ll
  "Parse a StopALl protobuf message to a map."
  [^cmd.System.System$StopALl proto]
  (cond-> {}))

(defn build-reboot
  "Build a Reboot protobuf message from a map."
  [m]
  (let [builder (cmd.System.System$Reboot/newBuilder)]

    (.build builder)))

(defn parse-reboot
  "Parse a Reboot protobuf message to a map."
  [^cmd.System.System$Reboot proto]
  (cond-> {}))

(defn build-power-off
  "Build a PowerOff protobuf message from a map."
  [m]
  (let [builder (cmd.System.System$PowerOff/newBuilder)]

    (.build builder)))

(defn parse-power-off
  "Parse a PowerOff protobuf message to a map."
  [^cmd.System.System$PowerOff proto]
  (cond-> {}))

(defn build-reset-configs
  "Build a ResetConfigs protobuf message from a map."
  [m]
  (let [builder (cmd.System.System$ResetConfigs/newBuilder)]

    (.build builder)))

(defn parse-reset-configs
  "Parse a ResetConfigs protobuf message to a map."
  [^cmd.System.System$ResetConfigs proto]
  (cond-> {}))

(defn build-start-rec
  "Build a StartRec protobuf message from a map."
  [m]
  (let [builder (cmd.System.System$StartRec/newBuilder)]

    (.build builder)))

(defn parse-start-rec
  "Parse a StartRec protobuf message to a map."
  [^cmd.System.System$StartRec proto]
  (cond-> {}))

(defn build-stop-rec
  "Build a StopRec protobuf message from a map."
  [m]
  (let [builder (cmd.System.System$StopRec/newBuilder)]

    (.build builder)))

(defn parse-stop-rec
  "Parse a StopRec protobuf message to a map."
  [^cmd.System.System$StopRec proto]
  (cond-> {}))

(defn build-mark-rec-important
  "Build a MarkRecImportant protobuf message from a map."
  [m]
  (let [builder (cmd.System.System$MarkRecImportant/newBuilder)]

    (.build builder)))

(defn parse-mark-rec-important
  "Parse a MarkRecImportant protobuf message to a map."
  [^cmd.System.System$MarkRecImportant proto]
  (cond-> {}))

(defn build-unmark-rec-important
  "Build a UnmarkRecImportant protobuf message from a map."
  [m]
  (let [builder (cmd.System.System$UnmarkRecImportant/newBuilder)]

    (.build builder)))

(defn parse-unmark-rec-important
  "Parse a UnmarkRecImportant protobuf message to a map."
  [^cmd.System.System$UnmarkRecImportant proto]
  (cond-> {}))

(defn build-enter-transport
  "Build a EnterTransport protobuf message from a map."
  [m]
  (let [builder (cmd.System.System$EnterTransport/newBuilder)]

    (.build builder)))

(defn parse-enter-transport
  "Parse a EnterTransport protobuf message to a map."
  [^cmd.System.System$EnterTransport proto]
  (cond-> {}))

(defn build-enable-geodesic-mode
  "Build a EnableGeodesicMode protobuf message from a map."
  [m]
  (let [builder (cmd.System.System$EnableGeodesicMode/newBuilder)]

    (.build builder)))

(defn parse-enable-geodesic-mode
  "Parse a EnableGeodesicMode protobuf message to a map."
  [^cmd.System.System$EnableGeodesicMode proto]
  (cond-> {}))

(defn build-disable-geodesic-mode
  "Build a DisableGeodesicMode protobuf message from a map."
  [m]
  (let [builder (cmd.System.System$DisableGeodesicMode/newBuilder)]

    (.build builder)))

(defn parse-disable-geodesic-mode
  "Parse a DisableGeodesicMode protobuf message to a map."
  [^cmd.System.System$DisableGeodesicMode proto]
  (cond-> {}))

(defn build-set-localization
  "Build a SetLocalization protobuf message from a map."
  [m]
  (let [builder (cmd.System.System$SetLocalization/newBuilder)]
    ;; Set regular fields
    (when (contains? m :loc)
      (.setLoc builder (get m :loc)))

    (.build builder)))

(defn parse-set-localization
  "Parse a SetLocalization protobuf message to a map."
  [^cmd.System.System$SetLocalization proto]
  (cond-> {}
    ;; Regular fields
    (.hasLoc proto) (assoc :loc (.getLoc proto))))

(defn build-root
  "Build a Root protobuf message from a map."
  [m]
  (let [builder (cmd.CV.Cv$Root/newBuilder)]

    (.build builder)))

(defn parse-root
  "Parse a Root protobuf message to a map."
  [^cmd.CV.Cv$Root proto]
  (cond-> {}

;; Oneof payload
    true (merge (parse-root-payload proto))))

(defn build-vampire-mode-enable
  "Build a VampireModeEnable protobuf message from a map."
  [m]
  (let [builder (cmd.CV.Cv$VampireModeEnable/newBuilder)]

    (.build builder)))

(defn parse-vampire-mode-enable
  "Parse a VampireModeEnable protobuf message to a map."
  [^cmd.CV.Cv$VampireModeEnable proto]
  (cond-> {}))

(defn build-dump-start
  "Build a DumpStart protobuf message from a map."
  [m]
  (let [builder (cmd.CV.Cv$DumpStart/newBuilder)]

    (.build builder)))

(defn parse-dump-start
  "Parse a DumpStart protobuf message to a map."
  [^cmd.CV.Cv$DumpStart proto]
  (cond-> {}))

(defn build-dump-stop
  "Build a DumpStop protobuf message from a map."
  [m]
  (let [builder (cmd.CV.Cv$DumpStop/newBuilder)]

    (.build builder)))

(defn parse-dump-stop
  "Parse a DumpStop protobuf message to a map."
  [^cmd.CV.Cv$DumpStop proto]
  (cond-> {}))

(defn build-vampire-mode-disable
  "Build a VampireModeDisable protobuf message from a map."
  [m]
  (let [builder (cmd.CV.Cv$VampireModeDisable/newBuilder)]

    (.build builder)))

(defn parse-vampire-mode-disable
  "Parse a VampireModeDisable protobuf message to a map."
  [^cmd.CV.Cv$VampireModeDisable proto]
  (cond-> {}))

(defn build-stabilization-mode-enable
  "Build a StabilizationModeEnable protobuf message from a map."
  [m]
  (let [builder (cmd.CV.Cv$StabilizationModeEnable/newBuilder)]

    (.build builder)))

(defn parse-stabilization-mode-enable
  "Parse a StabilizationModeEnable protobuf message to a map."
  [^cmd.CV.Cv$StabilizationModeEnable proto]
  (cond-> {}))

(defn build-stabilization-mode-disable
  "Build a StabilizationModeDisable protobuf message from a map."
  [m]
  (let [builder (cmd.CV.Cv$StabilizationModeDisable/newBuilder)]

    (.build builder)))

(defn parse-stabilization-mode-disable
  "Parse a StabilizationModeDisable protobuf message to a map."
  [^cmd.CV.Cv$StabilizationModeDisable proto]
  (cond-> {}))

(defn build-set-auto-focus
  "Build a SetAutoFocus protobuf message from a map."
  [m]
  (let [builder (cmd.CV.Cv$SetAutoFocus/newBuilder)]
    ;; Set regular fields
    (when (contains? m :channel)
      (.setChannel builder (get m :channel)))
    (when (contains? m :value)
      (.setValue builder (get m :value)))

    (.build builder)))

(defn parse-set-auto-focus
  "Parse a SetAutoFocus protobuf message to a map."
  [^cmd.CV.Cv$SetAutoFocus proto]
  (cond-> {}
    ;; Regular fields
    (.hasChannel proto) (assoc :channel (.getChannel proto))
    (.hasValue proto) (assoc :value (.getValue proto))))

(defn build-start-track-ndc
  "Build a StartTrackNDC protobuf message from a map."
  [m]
  (let [builder (cmd.CV.Cv$StartTrackNDC/newBuilder)]
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
  [^cmd.CV.Cv$StartTrackNDC proto]
  (cond-> {}
    ;; Regular fields
    (.hasChannel proto) (assoc :channel (.getChannel proto))
    (.hasX proto) (assoc :x (.getX proto))
    (.hasY proto) (assoc :y (.getY proto))
    (.hasFrameTime proto) (assoc :frame-time (.getFrameTime proto))))

(defn build-stop-track
  "Build a StopTrack protobuf message from a map."
  [m]
  (let [builder (cmd.CV.Cv$StopTrack/newBuilder)]

    (.build builder)))

(defn parse-stop-track
  "Parse a StopTrack protobuf message to a map."
  [^cmd.CV.Cv$StopTrack proto]
  (cond-> {}))

(defn build-root
  "Build a Root protobuf message from a map."
  [m]
  (let [builder (cmd.DayCamGlassHeater.DayCamGlassHeater$Root/newBuilder)]

    (.build builder)))

(defn parse-root
  "Parse a Root protobuf message to a map."
  [^cmd.DayCamGlassHeater.DayCamGlassHeater$Root proto]
  (cond-> {}

;; Oneof payload
    true (merge (parse-root-payload proto))))

(defn build-start
  "Build a Start protobuf message from a map."
  [m]
  (let [builder (cmd.DayCamGlassHeater.DayCamGlassHeater$Start/newBuilder)]

    (.build builder)))

(defn parse-start
  "Parse a Start protobuf message to a map."
  [^cmd.DayCamGlassHeater.DayCamGlassHeater$Start proto]
  (cond-> {}))

(defn build-stop
  "Build a Stop protobuf message from a map."
  [m]
  (let [builder (cmd.DayCamGlassHeater.DayCamGlassHeater$Stop/newBuilder)]

    (.build builder)))

(defn parse-stop
  "Parse a Stop protobuf message to a map."
  [^cmd.DayCamGlassHeater.DayCamGlassHeater$Stop proto]
  (cond-> {}))

(defn build-turn-on
  "Build a TurnOn protobuf message from a map."
  [m]
  (let [builder (cmd.DayCamGlassHeater.DayCamGlassHeater$TurnOn/newBuilder)]

    (.build builder)))

(defn parse-turn-on
  "Parse a TurnOn protobuf message to a map."
  [^cmd.DayCamGlassHeater.DayCamGlassHeater$TurnOn proto]
  (cond-> {}))

(defn build-turn-off
  "Build a TurnOff protobuf message from a map."
  [m]
  (let [builder (cmd.DayCamGlassHeater.DayCamGlassHeater$TurnOff/newBuilder)]

    (.build builder)))

(defn parse-turn-off
  "Parse a TurnOff protobuf message to a map."
  [^cmd.DayCamGlassHeater.DayCamGlassHeater$TurnOff proto]
  (cond-> {}))

(defn build-get-meteo
  "Build a GetMeteo protobuf message from a map."
  [m]
  (let [builder (cmd.DayCamGlassHeater.DayCamGlassHeater$GetMeteo/newBuilder)]

    (.build builder)))

(defn parse-get-meteo
  "Parse a GetMeteo protobuf message to a map."
  [^cmd.DayCamGlassHeater.DayCamGlassHeater$GetMeteo proto]
  (cond-> {}))

(defn build-root
  "Build a Root protobuf message from a map."
  [m]
  (let [builder (cmd.Lira.Lira$Root/newBuilder)]

    (.build builder)))

(defn parse-root
  "Parse a Root protobuf message to a map."
  [^cmd.Lira.Lira$Root proto]
  (cond-> {}

;; Oneof payload
    true (merge (parse-root-payload proto))))

(defn build-refine-target
  "Build a Refine_target protobuf message from a map."
  [m]
  (let [builder (cmd.Lira.Lira$Refine_target/newBuilder)]
    ;; Set regular fields
    (when (contains? m :target)
      (.setTarget builder (get m :target)))

    (.build builder)))

(defn parse-refine-target
  "Parse a Refine_target protobuf message to a map."
  [^cmd.Lira.Lira$Refine_target proto]
  (cond-> {}
    ;; Regular fields
    (.hasTarget proto) (assoc :target (.getTarget proto))))

(defn build-jon-gui-data-lira-target
  "Build a JonGuiDataLiraTarget protobuf message from a map."
  [m]
  (let [builder (cmd.Lira.Lira$JonGuiDataLiraTarget/newBuilder)]
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
  [^cmd.Lira.Lira$JonGuiDataLiraTarget proto]
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

(defn build-root
  "Build a Root protobuf message from a map."
  [m]
  (let [builder (cmd.Cmd$Root/newBuilder)]
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

    (.build builder)))

(defn parse-root
  "Parse a Root protobuf message to a map."
  [^cmd.Cmd$Root proto]
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
  (let [builder (cmd.Cmd$Ping/newBuilder)]

    (.build builder)))

(defn parse-ping
  "Parse a Ping protobuf message to a map."
  [^cmd.Cmd$Ping proto]
  (cond-> {}))

(defn build-noop
  "Build a Noop protobuf message from a map."
  [m]
  (let [builder (cmd.Cmd$Noop/newBuilder)]

    (.build builder)))

(defn parse-noop
  "Parse a Noop protobuf message to a map."
  [^cmd.Cmd$Noop proto]
  (cond-> {}))

(defn build-frozen
  "Build a Frozen protobuf message from a map."
  [m]
  (let [builder (cmd.Cmd$Frozen/newBuilder)]

    (.build builder)))

(defn parse-frozen
  "Parse a Frozen protobuf message to a map."
  [^cmd.Cmd$Frozen proto]
  (cond-> {}))