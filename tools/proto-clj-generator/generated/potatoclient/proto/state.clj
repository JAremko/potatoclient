(ns potatoclient.proto.state
  "Generated protobuf functions."
  (:import
   [buf.validate.Validate
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

(defn build-jon-gui-data-time
  "Build a JonGuiDataTime protobuf message from a map."
  [m]
  (let [builder (ser.Ser$JonGuiDataTime/newBuilder)]
    ;; Set regular fields
    (when (contains? m :timestamp)
      (.setTimestamp builder (get m :timestamp)))
    (when (contains? m :manual-timestamp)
      (.setManualTimestamp builder (get m :manual-timestamp)))
    (when (contains? m :zone-id)
      (.setZoneId builder (get m :zone-id)))
    (when (contains? m :use-manual-time)
      (.setUseManualTime builder (get m :use-manual-time)))

    (.build builder)))

(defn parse-jon-gui-data-time
  "Parse a JonGuiDataTime protobuf message to a map."
  [^ser.Ser$JonGuiDataTime proto]
  (cond-> {}
    ;; Regular fields
    (.hasTimestamp proto) (assoc :timestamp (.getTimestamp proto))
    (.hasManualTimestamp proto) (assoc :manual-timestamp (.getManualTimestamp proto))
    (.hasZoneId proto) (assoc :zone-id (.getZoneId proto))
    (.hasUseManualTime proto) (assoc :use-manual-time (.getUseManualTime proto))))

(defn build-jon-gui-data-system
  "Build a JonGuiDataSystem protobuf message from a map."
  [m]
  (let [builder (ser.Ser$JonGuiDataSystem/newBuilder)]
    ;; Set regular fields
    (when (contains? m :cpu-temperature)
      (.setCpuTemperature builder (get m :cpu-temperature)))
    (when (contains? m :gpu-temperature)
      (.setGpuTemperature builder (get m :gpu-temperature)))
    (when (contains? m :gpu-load)
      (.setGpuLoad builder (get m :gpu-load)))
    (when (contains? m :cpu-load)
      (.setCpuLoad builder (get m :cpu-load)))
    (when (contains? m :power-consumption)
      (.setPowerConsumption builder (get m :power-consumption)))
    (when (contains? m :loc)
      (.setLoc builder (get m :loc)))
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
    (when (contains? m :disk-space)
      (.setDiskSpace builder (get m :disk-space)))
    (when (contains? m :tracking)
      (.setTracking builder (get m :tracking)))
    (when (contains? m :vampire-mode)
      (.setVampireMode builder (get m :vampire-mode)))
    (when (contains? m :stabilization-mode)
      (.setStabilizationMode builder (get m :stabilization-mode)))
    (when (contains? m :geodesic-mode)
      (.setGeodesicMode builder (get m :geodesic-mode)))
    (when (contains? m :cv-dumping)
      (.setCvDumping builder (get m :cv-dumping)))

    (.build builder)))

(defn parse-jon-gui-data-system
  "Parse a JonGuiDataSystem protobuf message to a map."
  [^ser.Ser$JonGuiDataSystem proto]
  (cond-> {}
    ;; Regular fields
    (.hasCpuTemperature proto) (assoc :cpu-temperature (.getCpuTemperature proto))
    (.hasGpuTemperature proto) (assoc :gpu-temperature (.getGpuTemperature proto))
    (.hasGpuLoad proto) (assoc :gpu-load (.getGpuLoad proto))
    (.hasCpuLoad proto) (assoc :cpu-load (.getCpuLoad proto))
    (.hasPowerConsumption proto) (assoc :power-consumption (.getPowerConsumption proto))
    (.hasLoc proto) (assoc :loc (.getLoc proto))
    (.hasCurVideoRecDirYear proto) (assoc :cur-video-rec-dir-year (.getCurVideoRecDirYear proto))
    (.hasCurVideoRecDirMonth proto) (assoc :cur-video-rec-dir-month (.getCurVideoRecDirMonth proto))
    (.hasCurVideoRecDirDay proto) (assoc :cur-video-rec-dir-day (.getCurVideoRecDirDay proto))
    (.hasCurVideoRecDirHour proto) (assoc :cur-video-rec-dir-hour (.getCurVideoRecDirHour proto))
    (.hasCurVideoRecDirMinute proto) (assoc :cur-video-rec-dir-minute (.getCurVideoRecDirMinute proto))
    (.hasCurVideoRecDirSecond proto) (assoc :cur-video-rec-dir-second (.getCurVideoRecDirSecond proto))
    (.hasRecEnabled proto) (assoc :rec-enabled (.getRecEnabled proto))
    (.hasImportantRecEnabled proto) (assoc :important-rec-enabled (.getImportantRecEnabled proto))
    (.hasLowDiskSpace proto) (assoc :low-disk-space (.getLowDiskSpace proto))
    (.hasNoDiskSpace proto) (assoc :no-disk-space (.getNoDiskSpace proto))
    (.hasDiskSpace proto) (assoc :disk-space (.getDiskSpace proto))
    (.hasTracking proto) (assoc :tracking (.getTracking proto))
    (.hasVampireMode proto) (assoc :vampire-mode (.getVampireMode proto))
    (.hasStabilizationMode proto) (assoc :stabilization-mode (.getStabilizationMode proto))
    (.hasGeodesicMode proto) (assoc :geodesic-mode (.getGeodesicMode proto))
    (.hasCvDumping proto) (assoc :cv-dumping (.getCvDumping proto))))

(defn build-jon-gui-data-lrf
  "Build a JonGuiDataLrf protobuf message from a map."
  [m]
  (let [builder (ser.Ser$JonGuiDataLrf/newBuilder)]
    ;; Set regular fields
    (when (contains? m :is-scanning)
      (.setIsScanning builder (get m :is-scanning)))
    (when (contains? m :is-measuring)
      (.setIsMeasuring builder (get m :is-measuring)))
    (when (contains? m :measure-id)
      (.setMeasureId builder (get m :measure-id)))
    (when (contains? m :target)
      (.setTarget builder (get m :target)))
    (when (contains? m :pointer-mode)
      (.setPointerMode builder (get m :pointer-mode)))
    (when (contains? m :fog-mode-enabled)
      (.setFogModeEnabled builder (get m :fog-mode-enabled)))
    (when (contains? m :is-refining)
      (.setIsRefining builder (get m :is-refining)))

    (.build builder)))

(defn parse-jon-gui-data-lrf
  "Parse a JonGuiDataLrf protobuf message to a map."
  [^ser.Ser$JonGuiDataLrf proto]
  (cond-> {}
    ;; Regular fields
    (.hasIsScanning proto) (assoc :is-scanning (.getIsScanning proto))
    (.hasIsMeasuring proto) (assoc :is-measuring (.getIsMeasuring proto))
    (.hasMeasureId proto) (assoc :measure-id (.getMeasureId proto))
    (.hasTarget proto) (assoc :target (.getTarget proto))
    (.hasPointerMode proto) (assoc :pointer-mode (.getPointerMode proto))
    (.hasFogModeEnabled proto) (assoc :fog-mode-enabled (.getFogModeEnabled proto))
    (.hasIsRefining proto) (assoc :is-refining (.getIsRefining proto))))

(defn build-jon-gui-data-target
  "Build a JonGuiDataTarget protobuf message from a map."
  [m]
  (let [builder (ser.Ser$JonGuiDataTarget/newBuilder)]
    ;; Set regular fields
    (when (contains? m :timestamp)
      (.setTimestamp builder (get m :timestamp)))
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
      (.setObserverFixType builder (get m :observer-fix-type)))
    (when (contains? m :session-id)
      (.setSessionId builder (get m :session-id)))
    (when (contains? m :target-id)
      (.setTargetId builder (get m :target-id)))
    (when (contains? m :target-color)
      (.setTargetColor builder (get m :target-color)))
    (when (contains? m :type)
      (.setType builder (get m :type)))
    (when (contains? m :uuid-part-1)
      (.setUuidPart1 builder (get m :uuid-part-1)))
    (when (contains? m :uuid-part-2)
      (.setUuidPart2 builder (get m :uuid-part-2)))
    (when (contains? m :uuid-part-3)
      (.setUuidPart3 builder (get m :uuid-part-3)))
    (when (contains? m :uuid-part-4)
      (.setUuidPart4 builder (get m :uuid-part-4)))

    (.build builder)))

(defn parse-jon-gui-data-target
  "Parse a JonGuiDataTarget protobuf message to a map."
  [^ser.Ser$JonGuiDataTarget proto]
  (cond-> {}
    ;; Regular fields
    (.hasTimestamp proto) (assoc :timestamp (.getTimestamp proto))
    (.hasTargetLongitude proto) (assoc :target-longitude (.getTargetLongitude proto))
    (.hasTargetLatitude proto) (assoc :target-latitude (.getTargetLatitude proto))
    (.hasTargetAltitude proto) (assoc :target-altitude (.getTargetAltitude proto))
    (.hasObserverLongitude proto) (assoc :observer-longitude (.getObserverLongitude proto))
    (.hasObserverLatitude proto) (assoc :observer-latitude (.getObserverLatitude proto))
    (.hasObserverAltitude proto) (assoc :observer-altitude (.getObserverAltitude proto))
    (.hasObserverAzimuth proto) (assoc :observer-azimuth (.getObserverAzimuth proto))
    (.hasObserverElevation proto) (assoc :observer-elevation (.getObserverElevation proto))
    (.hasObserverBank proto) (assoc :observer-bank (.getObserverBank proto))
    (.hasDistance2d proto) (assoc :distance-2d (.getDistance2d proto))
    (.hasDistance3b proto) (assoc :distance-3b (.getDistance3b proto))
    (.hasObserverFixType proto) (assoc :observer-fix-type (.getObserverFixType proto))
    (.hasSessionId proto) (assoc :session-id (.getSessionId proto))
    (.hasTargetId proto) (assoc :target-id (.getTargetId proto))
    (.hasTargetColor proto) (assoc :target-color (.getTargetColor proto))
    (.hasType proto) (assoc :type (.getType proto))
    (.hasUuidPart1 proto) (assoc :uuid-part-1 (.getUuidPart1 proto))
    (.hasUuidPart2 proto) (assoc :uuid-part-2 (.getUuidPart2 proto))
    (.hasUuidPart3 proto) (assoc :uuid-part-3 (.getUuidPart3 proto))
    (.hasUuidPart4 proto) (assoc :uuid-part-4 (.getUuidPart4 proto))))

(defn build-rgb-color
  "Build a RgbColor protobuf message from a map."
  [m]
  (let [builder (ser.Ser$RgbColor/newBuilder)]
    ;; Set regular fields
    (when (contains? m :red)
      (.setRed builder (get m :red)))
    (when (contains? m :green)
      (.setGreen builder (get m :green)))
    (when (contains? m :blue)
      (.setBlue builder (get m :blue)))

    (.build builder)))

(defn parse-rgb-color
  "Parse a RgbColor protobuf message to a map."
  [^ser.Ser$RgbColor proto]
  (cond-> {}
    ;; Regular fields
    (.hasRed proto) (assoc :red (.getRed proto))
    (.hasGreen proto) (assoc :green (.getGreen proto))
    (.hasBlue proto) (assoc :blue (.getBlue proto))))

(defn build-jon-gui-data-gps
  "Build a JonGuiDataGps protobuf message from a map."
  [m]
  (let [builder (ser.Ser$JonGuiDataGps/newBuilder)]
    ;; Set regular fields
    (when (contains? m :longitude)
      (.setLongitude builder (get m :longitude)))
    (when (contains? m :latitude)
      (.setLatitude builder (get m :latitude)))
    (when (contains? m :altitude)
      (.setAltitude builder (get m :altitude)))
    (when (contains? m :manual-longitude)
      (.setManualLongitude builder (get m :manual-longitude)))
    (when (contains? m :manual-latitude)
      (.setManualLatitude builder (get m :manual-latitude)))
    (when (contains? m :manual-altitude)
      (.setManualAltitude builder (get m :manual-altitude)))
    (when (contains? m :fix-type)
      (.setFixType builder (get m :fix-type)))
    (when (contains? m :use-manual)
      (.setUseManual builder (get m :use-manual)))

    (.build builder)))

(defn parse-jon-gui-data-gps
  "Parse a JonGuiDataGps protobuf message to a map."
  [^ser.Ser$JonGuiDataGps proto]
  (cond-> {}
    ;; Regular fields
    (.hasLongitude proto) (assoc :longitude (.getLongitude proto))
    (.hasLatitude proto) (assoc :latitude (.getLatitude proto))
    (.hasAltitude proto) (assoc :altitude (.getAltitude proto))
    (.hasManualLongitude proto) (assoc :manual-longitude (.getManualLongitude proto))
    (.hasManualLatitude proto) (assoc :manual-latitude (.getManualLatitude proto))
    (.hasManualAltitude proto) (assoc :manual-altitude (.getManualAltitude proto))
    (.hasFixType proto) (assoc :fix-type (.getFixType proto))
    (.hasUseManual proto) (assoc :use-manual (.getUseManual proto))))

(defn build-jon-gui-data-compass
  "Build a JonGuiDataCompass protobuf message from a map."
  [m]
  (let [builder (ser.Ser$JonGuiDataCompass/newBuilder)]
    ;; Set regular fields
    (when (contains? m :azimuth)
      (.setAzimuth builder (get m :azimuth)))
    (when (contains? m :elevation)
      (.setElevation builder (get m :elevation)))
    (when (contains? m :bank)
      (.setBank builder (get m :bank)))
    (when (contains? m :offset-azimuth)
      (.setOffsetAzimuth builder (get m :offset-azimuth)))
    (when (contains? m :offset-elevation)
      (.setOffsetElevation builder (get m :offset-elevation)))
    (when (contains? m :magnetic-declination)
      (.setMagneticDeclination builder (get m :magnetic-declination)))
    (when (contains? m :calibrating)
      (.setCalibrating builder (get m :calibrating)))

    (.build builder)))

(defn parse-jon-gui-data-compass
  "Parse a JonGuiDataCompass protobuf message to a map."
  [^ser.Ser$JonGuiDataCompass proto]
  (cond-> {}
    ;; Regular fields
    (.hasAzimuth proto) (assoc :azimuth (.getAzimuth proto))
    (.hasElevation proto) (assoc :elevation (.getElevation proto))
    (.hasBank proto) (assoc :bank (.getBank proto))
    (.hasOffsetAzimuth proto) (assoc :offset-azimuth (.getOffsetAzimuth proto))
    (.hasOffsetElevation proto) (assoc :offset-elevation (.getOffsetElevation proto))
    (.hasMagneticDeclination proto) (assoc :magnetic-declination (.getMagneticDeclination proto))
    (.hasCalibrating proto) (assoc :calibrating (.getCalibrating proto))))

(defn build-jon-gui-data-compass-calibration
  "Build a JonGuiDataCompassCalibration protobuf message from a map."
  [m]
  (let [builder (ser.Ser$JonGuiDataCompassCalibration/newBuilder)]
    ;; Set regular fields
    (when (contains? m :stage)
      (.setStage builder (get m :stage)))
    (when (contains? m :final-stage)
      (.setFinalStage builder (get m :final-stage)))
    (when (contains? m :target-azimuth)
      (.setTargetAzimuth builder (get m :target-azimuth)))
    (when (contains? m :target-elevation)
      (.setTargetElevation builder (get m :target-elevation)))
    (when (contains? m :target-bank)
      (.setTargetBank builder (get m :target-bank)))
    (when (contains? m :status)
      (.setStatus builder (get m :status)))

    (.build builder)))

(defn parse-jon-gui-data-compass-calibration
  "Parse a JonGuiDataCompassCalibration protobuf message to a map."
  [^ser.Ser$JonGuiDataCompassCalibration proto]
  (cond-> {}
    ;; Regular fields
    (.hasStage proto) (assoc :stage (.getStage proto))
    (.hasFinalStage proto) (assoc :final-stage (.getFinalStage proto))
    (.hasTargetAzimuth proto) (assoc :target-azimuth (.getTargetAzimuth proto))
    (.hasTargetElevation proto) (assoc :target-elevation (.getTargetElevation proto))
    (.hasTargetBank proto) (assoc :target-bank (.getTargetBank proto))
    (.hasStatus proto) (assoc :status (.getStatus proto))))

(defn build-jon-gui-data-rotary
  "Build a JonGuiDataRotary protobuf message from a map."
  [m]
  (let [builder (ser.Ser$JonGuiDataRotary/newBuilder)]
    ;; Set regular fields
    (when (contains? m :azimuth)
      (.setAzimuth builder (get m :azimuth)))
    (when (contains? m :azimuth-speed)
      (.setAzimuthSpeed builder (get m :azimuth-speed)))
    (when (contains? m :elevation)
      (.setElevation builder (get m :elevation)))
    (when (contains? m :elevation-speed)
      (.setElevationSpeed builder (get m :elevation-speed)))
    (when (contains? m :platform-azimuth)
      (.setPlatformAzimuth builder (get m :platform-azimuth)))
    (when (contains? m :platform-elevation)
      (.setPlatformElevation builder (get m :platform-elevation)))
    (when (contains? m :platform-bank)
      (.setPlatformBank builder (get m :platform-bank)))
    (when (contains? m :is-moving)
      (.setIsMoving builder (get m :is-moving)))
    (when (contains? m :mode)
      (.setMode builder (get m :mode)))
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
      (.setCurrentScanNode builder (get m :current-scan-node)))

    (.build builder)))

(defn parse-jon-gui-data-rotary
  "Parse a JonGuiDataRotary protobuf message to a map."
  [^ser.Ser$JonGuiDataRotary proto]
  (cond-> {}
    ;; Regular fields
    (.hasAzimuth proto) (assoc :azimuth (.getAzimuth proto))
    (.hasAzimuthSpeed proto) (assoc :azimuth-speed (.getAzimuthSpeed proto))
    (.hasElevation proto) (assoc :elevation (.getElevation proto))
    (.hasElevationSpeed proto) (assoc :elevation-speed (.getElevationSpeed proto))
    (.hasPlatformAzimuth proto) (assoc :platform-azimuth (.getPlatformAzimuth proto))
    (.hasPlatformElevation proto) (assoc :platform-elevation (.getPlatformElevation proto))
    (.hasPlatformBank proto) (assoc :platform-bank (.getPlatformBank proto))
    (.hasIsMoving proto) (assoc :is-moving (.getIsMoving proto))
    (.hasMode proto) (assoc :mode (.getMode proto))
    (.hasIsScanning proto) (assoc :is-scanning (.getIsScanning proto))
    (.hasIsScanningPaused proto) (assoc :is-scanning-paused (.getIsScanningPaused proto))
    (.hasUseRotaryAsCompass proto) (assoc :use-rotary-as-compass (.getUseRotaryAsCompass proto))
    (.hasScanTarget proto) (assoc :scan-target (.getScanTarget proto))
    (.hasScanTargetMax proto) (assoc :scan-target-max (.getScanTargetMax proto))
    (.hasSunAzimuth proto) (assoc :sun-azimuth (.getSunAzimuth proto))
    (.hasSunElevation proto) (assoc :sun-elevation (.getSunElevation proto))
    (.hasCurrentScanNode proto) (assoc :current-scan-node (.getCurrentScanNode proto))))

(defn build-scan-node
  "Build a ScanNode protobuf message from a map."
  [m]
  (let [builder (ser.Ser$ScanNode/newBuilder)]
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

(defn parse-scan-node
  "Parse a ScanNode protobuf message to a map."
  [^ser.Ser$ScanNode proto]
  (cond-> {}
    ;; Regular fields
    (.hasIndex proto) (assoc :index (.getIndex proto))
    (.hasDayZoomTableValue proto) (assoc :day-zoom-table-value (.getDayZoomTableValue proto))
    (.hasHeatZoomTableValue proto) (assoc :heat-zoom-table-value (.getHeatZoomTableValue proto))
    (.hasAzimuth proto) (assoc :azimuth (.getAzimuth proto))
    (.hasElevation proto) (assoc :elevation (.getElevation proto))
    (.hasLinger proto) (assoc :linger (.getLinger proto))
    (.hasSpeed proto) (assoc :speed (.getSpeed proto))))

(defn build-jon-gui-data-camera-day
  "Build a JonGuiDataCameraDay protobuf message from a map."
  [m]
  (let [builder (ser.Ser$JonGuiDataCameraDay/newBuilder)]
    ;; Set regular fields
    (when (contains? m :focus-pos)
      (.setFocusPos builder (get m :focus-pos)))
    (when (contains? m :zoom-pos)
      (.setZoomPos builder (get m :zoom-pos)))
    (when (contains? m :iris-pos)
      (.setIrisPos builder (get m :iris-pos)))
    (when (contains? m :infrared-filter)
      (.setInfraredFilter builder (get m :infrared-filter)))
    (when (contains? m :zoom-table-pos)
      (.setZoomTablePos builder (get m :zoom-table-pos)))
    (when (contains? m :zoom-table-pos-max)
      (.setZoomTablePosMax builder (get m :zoom-table-pos-max)))
    (when (contains? m :fx-mode)
      (.setFxMode builder (get m :fx-mode)))
    (when (contains? m :auto-focus)
      (.setAutoFocus builder (get m :auto-focus)))
    (when (contains? m :auto-iris)
      (.setAutoIris builder (get m :auto-iris)))
    (when (contains? m :digital-zoom-level)
      (.setDigitalZoomLevel builder (get m :digital-zoom-level)))
    (when (contains? m :clahe-level)
      (.setClaheLevel builder (get m :clahe-level)))

    (.build builder)))

(defn parse-jon-gui-data-camera-day
  "Parse a JonGuiDataCameraDay protobuf message to a map."
  [^ser.Ser$JonGuiDataCameraDay proto]
  (cond-> {}
    ;; Regular fields
    (.hasFocusPos proto) (assoc :focus-pos (.getFocusPos proto))
    (.hasZoomPos proto) (assoc :zoom-pos (.getZoomPos proto))
    (.hasIrisPos proto) (assoc :iris-pos (.getIrisPos proto))
    (.hasInfraredFilter proto) (assoc :infrared-filter (.getInfraredFilter proto))
    (.hasZoomTablePos proto) (assoc :zoom-table-pos (.getZoomTablePos proto))
    (.hasZoomTablePosMax proto) (assoc :zoom-table-pos-max (.getZoomTablePosMax proto))
    (.hasFxMode proto) (assoc :fx-mode (.getFxMode proto))
    (.hasAutoFocus proto) (assoc :auto-focus (.getAutoFocus proto))
    (.hasAutoIris proto) (assoc :auto-iris (.getAutoIris proto))
    (.hasDigitalZoomLevel proto) (assoc :digital-zoom-level (.getDigitalZoomLevel proto))
    (.hasClaheLevel proto) (assoc :clahe-level (.getClaheLevel proto))))

(defn build-jon-gui-data-camera-heat
  "Build a JonGuiDataCameraHeat protobuf message from a map."
  [m]
  (let [builder (ser.Ser$JonGuiDataCameraHeat/newBuilder)]
    ;; Set regular fields
    (when (contains? m :zoom-pos)
      (.setZoomPos builder (get m :zoom-pos)))
    (when (contains? m :agc-mode)
      (.setAgcMode builder (get m :agc-mode)))
    (when (contains? m :filter)
      (.setFilter builder (get m :filter)))
    (when (contains? m :auto-focus)
      (.setAutoFocus builder (get m :auto-focus)))
    (when (contains? m :zoom-table-pos)
      (.setZoomTablePos builder (get m :zoom-table-pos)))
    (when (contains? m :zoom-table-pos-max)
      (.setZoomTablePosMax builder (get m :zoom-table-pos-max)))
    (when (contains? m :dde-level)
      (.setDdeLevel builder (get m :dde-level)))
    (when (contains? m :dde-enabled)
      (.setDdeEnabled builder (get m :dde-enabled)))
    (when (contains? m :fx-mode)
      (.setFxMode builder (get m :fx-mode)))
    (when (contains? m :digital-zoom-level)
      (.setDigitalZoomLevel builder (get m :digital-zoom-level)))
    (when (contains? m :clahe-level)
      (.setClaheLevel builder (get m :clahe-level)))

    (.build builder)))

(defn parse-jon-gui-data-camera-heat
  "Parse a JonGuiDataCameraHeat protobuf message to a map."
  [^ser.Ser$JonGuiDataCameraHeat proto]
  (cond-> {}
    ;; Regular fields
    (.hasZoomPos proto) (assoc :zoom-pos (.getZoomPos proto))
    (.hasAgcMode proto) (assoc :agc-mode (.getAgcMode proto))
    (.hasFilter proto) (assoc :filter (.getFilter proto))
    (.hasAutoFocus proto) (assoc :auto-focus (.getAutoFocus proto))
    (.hasZoomTablePos proto) (assoc :zoom-table-pos (.getZoomTablePos proto))
    (.hasZoomTablePosMax proto) (assoc :zoom-table-pos-max (.getZoomTablePosMax proto))
    (.hasDdeLevel proto) (assoc :dde-level (.getDdeLevel proto))
    (.hasDdeEnabled proto) (assoc :dde-enabled (.getDdeEnabled proto))
    (.hasFxMode proto) (assoc :fx-mode (.getFxMode proto))
    (.hasDigitalZoomLevel proto) (assoc :digital-zoom-level (.getDigitalZoomLevel proto))
    (.hasClaheLevel proto) (assoc :clahe-level (.getClaheLevel proto))))

(defn build-jon-gui-data-rec-osd
  "Build a JonGuiDataRecOsd protobuf message from a map."
  [m]
  (let [builder (ser.Ser$JonGuiDataRecOsd/newBuilder)]
    ;; Set regular fields
    (when (contains? m :screen)
      (.setScreen builder (get m :screen)))
    (when (contains? m :heat-osd-enabled)
      (.setHeatOsdEnabled builder (get m :heat-osd-enabled)))
    (when (contains? m :day-osd-enabled)
      (.setDayOsdEnabled builder (get m :day-osd-enabled)))
    (when (contains? m :heat-crosshair-offset-horizontal)
      (.setHeatCrosshairOffsetHorizontal builder (get m :heat-crosshair-offset-horizontal)))
    (when (contains? m :heat-crosshair-offset-vertical)
      (.setHeatCrosshairOffsetVertical builder (get m :heat-crosshair-offset-vertical)))
    (when (contains? m :day-crosshair-offset-horizontal)
      (.setDayCrosshairOffsetHorizontal builder (get m :day-crosshair-offset-horizontal)))
    (when (contains? m :day-crosshair-offset-vertical)
      (.setDayCrosshairOffsetVertical builder (get m :day-crosshair-offset-vertical)))

    (.build builder)))

(defn parse-jon-gui-data-rec-osd
  "Parse a JonGuiDataRecOsd protobuf message to a map."
  [^ser.Ser$JonGuiDataRecOsd proto]
  (cond-> {}
    ;; Regular fields
    (.hasScreen proto) (assoc :screen (.getScreen proto))
    (.hasHeatOsdEnabled proto) (assoc :heat-osd-enabled (.getHeatOsdEnabled proto))
    (.hasDayOsdEnabled proto) (assoc :day-osd-enabled (.getDayOsdEnabled proto))
    (.hasHeatCrosshairOffsetHorizontal proto) (assoc :heat-crosshair-offset-horizontal (.getHeatCrosshairOffsetHorizontal proto))
    (.hasHeatCrosshairOffsetVertical proto) (assoc :heat-crosshair-offset-vertical (.getHeatCrosshairOffsetVertical proto))
    (.hasDayCrosshairOffsetHorizontal proto) (assoc :day-crosshair-offset-horizontal (.getDayCrosshairOffsetHorizontal proto))
    (.hasDayCrosshairOffsetVertical proto) (assoc :day-crosshair-offset-vertical (.getDayCrosshairOffsetVertical proto))))

(defn build-jon-gui-data-day-cam-glass-heater
  "Build a JonGuiDataDayCamGlassHeater protobuf message from a map."
  [m]
  (let [builder (ser.Ser$JonGuiDataDayCamGlassHeater/newBuilder)]
    ;; Set regular fields
    (when (contains? m :temperature)
      (.setTemperature builder (get m :temperature)))
    (when (contains? m :status)
      (.setStatus builder (get m :status)))

    (.build builder)))

(defn parse-jon-gui-data-day-cam-glass-heater
  "Parse a JonGuiDataDayCamGlassHeater protobuf message to a map."
  [^ser.Ser$JonGuiDataDayCamGlassHeater proto]
  (cond-> {}
    ;; Regular fields
    (.hasTemperature proto) (assoc :temperature (.getTemperature proto))
    (.hasStatus proto) (assoc :status (.getStatus proto))))

(defn build-jon-gui-data-actual-space-time
  "Build a JonGuiDataActualSpaceTime protobuf message from a map."
  [m]
  (let [builder (ser.Ser$JonGuiDataActualSpaceTime/newBuilder)]
    ;; Set regular fields
    (when (contains? m :azimuth)
      (.setAzimuth builder (get m :azimuth)))
    (when (contains? m :elevation)
      (.setElevation builder (get m :elevation)))
    (when (contains? m :bank)
      (.setBank builder (get m :bank)))
    (when (contains? m :latitude)
      (.setLatitude builder (get m :latitude)))
    (when (contains? m :longitude)
      (.setLongitude builder (get m :longitude)))
    (when (contains? m :altitude)
      (.setAltitude builder (get m :altitude)))
    (when (contains? m :timestamp)
      (.setTimestamp builder (get m :timestamp)))

    (.build builder)))

(defn parse-jon-gui-data-actual-space-time
  "Parse a JonGuiDataActualSpaceTime protobuf message to a map."
  [^ser.Ser$JonGuiDataActualSpaceTime proto]
  (cond-> {}
    ;; Regular fields
    (.hasAzimuth proto) (assoc :azimuth (.getAzimuth proto))
    (.hasElevation proto) (assoc :elevation (.getElevation proto))
    (.hasBank proto) (assoc :bank (.getBank proto))
    (.hasLatitude proto) (assoc :latitude (.getLatitude proto))
    (.hasLongitude proto) (assoc :longitude (.getLongitude proto))
    (.hasAltitude proto) (assoc :altitude (.getAltitude proto))
    (.hasTimestamp proto) (assoc :timestamp (.getTimestamp proto))))

(defn build-jon-gui-state
  "Build a JonGUIState protobuf message from a map."
  [m]
  (let [builder (ser.Ser$JonGUIState/newBuilder)]
    ;; Set regular fields
    (when (contains? m :protocol-version)
      (.setProtocolVersion builder (get m :protocol-version)))
    (when (contains? m :system)
      (.setSystem builder (get m :system)))
    (when (contains? m :meteo-internal)
      (.setMeteoInternal builder (get m :meteo-internal)))
    (when (contains? m :lrf)
      (.setLrf builder (get m :lrf)))
    (when (contains? m :time)
      (.setTime builder (get m :time)))
    (when (contains? m :gps)
      (.setGps builder (get m :gps)))
    (when (contains? m :compass)
      (.setCompass builder (get m :compass)))
    (when (contains? m :rotary)
      (.setRotary builder (get m :rotary)))
    (when (contains? m :camera-day)
      (.setCameraDay builder (get m :camera-day)))
    (when (contains? m :camera-heat)
      (.setCameraHeat builder (get m :camera-heat)))
    (when (contains? m :compass-calibration)
      (.setCompassCalibration builder (get m :compass-calibration)))
    (when (contains? m :rec-osd)
      (.setRecOsd builder (get m :rec-osd)))
    (when (contains? m :day-cam-glass-heater)
      (.setDayCamGlassHeater builder (get m :day-cam-glass-heater)))
    (when (contains? m :actual-space-time)
      (.setActualSpaceTime builder (get m :actual-space-time)))

    (.build builder)))

(defn parse-jon-gui-state
  "Parse a JonGUIState protobuf message to a map."
  [^ser.Ser$JonGUIState proto]
  (cond-> {}
    ;; Regular fields
    (.hasProtocolVersion proto) (assoc :protocol-version (.getProtocolVersion proto))
    (.hasSystem proto) (assoc :system (.getSystem proto))
    (.hasMeteoInternal proto) (assoc :meteo-internal (.getMeteoInternal proto))
    (.hasLrf proto) (assoc :lrf (.getLrf proto))
    (.hasTime proto) (assoc :time (.getTime proto))
    (.hasGps proto) (assoc :gps (.getGps proto))
    (.hasCompass proto) (assoc :compass (.getCompass proto))
    (.hasRotary proto) (assoc :rotary (.getRotary proto))
    (.hasCameraDay proto) (assoc :camera-day (.getCameraDay proto))
    (.hasCameraHeat proto) (assoc :camera-heat (.getCameraHeat proto))
    (.hasCompassCalibration proto) (assoc :compass-calibration (.getCompassCalibration proto))
    (.hasRecOsd proto) (assoc :rec-osd (.getRecOsd proto))
    (.hasDayCamGlassHeater proto) (assoc :day-cam-glass-heater (.getDayCamGlassHeater proto))
    (.hasActualSpaceTime proto) (assoc :actual-space-time (.getActualSpaceTime proto))))