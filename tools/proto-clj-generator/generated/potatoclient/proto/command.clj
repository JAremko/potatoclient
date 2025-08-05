(ns potatoclient.proto.command
  "Generated protobuf functions."
  (:import
      buf.validate.Validate
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
   ser.Ser))

;; =============================================================================
;; Enums
;; =============================================================================

;; Enum: Edition
(def edition-values
  "Keyword to Java enum mapping for Edition."
  {:edition-unknown google.protobuf.Protobuf$Edition$:edition-unknown
   :edition-legacy google.protobuf.Protobuf$Edition$:edition-legacy
   :edition-proto-2 google.protobuf.Protobuf$Edition$:edition-proto-2
   :edition-proto-3 google.protobuf.Protobuf$Edition$:edition-proto-3
   :edition-2023 google.protobuf.Protobuf$Edition$:edition-2023
   :edition-2024 google.protobuf.Protobuf$Edition$:edition-2024
   :edition-1-test-only google.protobuf.Protobuf$Edition$:edition-1-test-only
   :edition-2-test-only google.protobuf.Protobuf$Edition$:edition-2-test-only
   :edition-99997-test-only google.protobuf.Protobuf$Edition$:edition-99997-test-only
   :edition-99998-test-only google.protobuf.Protobuf$Edition$:edition-99998-test-only
   :edition-99999-test-only google.protobuf.Protobuf$Edition$:edition-99999-test-only
   :edition-max google.protobuf.Protobuf$Edition$:edition-max})

(def edition-keywords
  "Java enum to keyword mapping for Edition."
  {google.protobuf.Protobuf$Edition$:edition-unknown :edition-unknown
   google.protobuf.Protobuf$Edition$:edition-legacy :edition-legacy
   google.protobuf.Protobuf$Edition$:edition-proto-2 :edition-proto-2
   google.protobuf.Protobuf$Edition$:edition-proto-3 :edition-proto-3
   google.protobuf.Protobuf$Edition$:edition-2023 :edition-2023
   google.protobuf.Protobuf$Edition$:edition-2024 :edition-2024
   google.protobuf.Protobuf$Edition$:edition-1-test-only :edition-1-test-only
   google.protobuf.Protobuf$Edition$:edition-2-test-only :edition-2-test-only
   google.protobuf.Protobuf$Edition$:edition-99997-test-only :edition-99997-test-only
   google.protobuf.Protobuf$Edition$:edition-99998-test-only :edition-99998-test-only
   google.protobuf.Protobuf$Edition$:edition-99999-test-only :edition-99999-test-only
   google.protobuf.Protobuf$Edition$:edition-max :edition-max})

;; Enum: SymbolVisibility
(def symbol-visibility-values
  "Keyword to Java enum mapping for SymbolVisibility."
  {:visibility-unset google.protobuf.Protobuf$SymbolVisibility$:visibility-unset
   :visibility-local google.protobuf.Protobuf$SymbolVisibility$:visibility-local
   :visibility-export google.protobuf.Protobuf$SymbolVisibility$:visibility-export})

(def symbol-visibility-keywords
  "Java enum to keyword mapping for SymbolVisibility."
  {google.protobuf.Protobuf$SymbolVisibility$:visibility-unset :visibility-unset
   google.protobuf.Protobuf$SymbolVisibility$:visibility-local :visibility-local
   google.protobuf.Protobuf$SymbolVisibility$:visibility-export :visibility-export})

;; Enum: Ignore
(def ignore-values
  "Keyword to Java enum mapping for Ignore."
  {:ignore-unspecified buf.validate.Validate$Ignore$:ignore-unspecified
   :ignore-if-zero-value buf.validate.Validate$Ignore$:ignore-if-zero-value
   :ignore-always buf.validate.Validate$Ignore$:ignore-always})

(def ignore-keywords
  "Java enum to keyword mapping for Ignore."
  {buf.validate.Validate$Ignore$:ignore-unspecified :ignore-unspecified
   buf.validate.Validate$Ignore$:ignore-if-zero-value :ignore-if-zero-value
   buf.validate.Validate$Ignore$:ignore-always :ignore-always})

;; Enum: KnownRegex
(def known-regex-values
  "Keyword to Java enum mapping for KnownRegex."
  {:known-regex-unspecified buf.validate.Validate$KnownRegex$:known-regex-unspecified
   :known-regex-http-header-name buf.validate.Validate$KnownRegex$:known-regex-http-header-name
   :known-regex-http-header-value buf.validate.Validate$KnownRegex$:known-regex-http-header-value})

(def known-regex-keywords
  "Java enum to keyword mapping for KnownRegex."
  {buf.validate.Validate$KnownRegex$:known-regex-unspecified :known-regex-unspecified
   buf.validate.Validate$KnownRegex$:known-regex-http-header-name :known-regex-http-header-name
   buf.validate.Validate$KnownRegex$:known-regex-http-header-value :known-regex-http-header-value})

;; Enum: JonGuiDataVideoChannelHeatFilters
(def jon-gui-data-video-channel-heat-filters-values
  "Keyword to Java enum mapping for JonGuiDataVideoChannelHeatFilters."
  {:jon-gui-data-video-channel-heat-filter-unspecified ser.Ser$JonGuiDataVideoChannelHeatFilters$:jon-gui-data-video-channel-heat-filter-unspecified
   :jon-gui-data-video-channel-heat-filter-hot-white ser.Ser$JonGuiDataVideoChannelHeatFilters$:jon-gui-data-video-channel-heat-filter-hot-white
   :jon-gui-data-video-channel-heat-filter-hot-black ser.Ser$JonGuiDataVideoChannelHeatFilters$:jon-gui-data-video-channel-heat-filter-hot-black
   :jon-gui-data-video-channel-heat-filter-sepia ser.Ser$JonGuiDataVideoChannelHeatFilters$:jon-gui-data-video-channel-heat-filter-sepia
   :jon-gui-data-video-channel-heat-filter-sepia-inverse ser.Ser$JonGuiDataVideoChannelHeatFilters$:jon-gui-data-video-channel-heat-filter-sepia-inverse})

(def jon-gui-data-video-channel-heat-filters-keywords
  "Java enum to keyword mapping for JonGuiDataVideoChannelHeatFilters."
  {ser.Ser$JonGuiDataVideoChannelHeatFilters$:jon-gui-data-video-channel-heat-filter-unspecified :jon-gui-data-video-channel-heat-filter-unspecified
   ser.Ser$JonGuiDataVideoChannelHeatFilters$:jon-gui-data-video-channel-heat-filter-hot-white :jon-gui-data-video-channel-heat-filter-hot-white
   ser.Ser$JonGuiDataVideoChannelHeatFilters$:jon-gui-data-video-channel-heat-filter-hot-black :jon-gui-data-video-channel-heat-filter-hot-black
   ser.Ser$JonGuiDataVideoChannelHeatFilters$:jon-gui-data-video-channel-heat-filter-sepia :jon-gui-data-video-channel-heat-filter-sepia
   ser.Ser$JonGuiDataVideoChannelHeatFilters$:jon-gui-data-video-channel-heat-filter-sepia-inverse :jon-gui-data-video-channel-heat-filter-sepia-inverse})

;; Enum: JonGuiDataVideoChannelHeatAGCModes
(def jon-gui-data-video-channel-heat-agc-modes-values
  "Keyword to Java enum mapping for JonGuiDataVideoChannelHeatAGCModes."
  {:jon-gui-data-video-channel-heat-agc-mode-unspecified ser.Ser$JonGuiDataVideoChannelHeatAGCModes$:jon-gui-data-video-channel-heat-agc-mode-unspecified
   :jon-gui-data-video-channel-heat-agc-mode-1 ser.Ser$JonGuiDataVideoChannelHeatAGCModes$:jon-gui-data-video-channel-heat-agc-mode-1
   :jon-gui-data-video-channel-heat-agc-mode-2 ser.Ser$JonGuiDataVideoChannelHeatAGCModes$:jon-gui-data-video-channel-heat-agc-mode-2
   :jon-gui-data-video-channel-heat-agc-mode-3 ser.Ser$JonGuiDataVideoChannelHeatAGCModes$:jon-gui-data-video-channel-heat-agc-mode-3})

(def jon-gui-data-video-channel-heat-agc-modes-keywords
  "Java enum to keyword mapping for JonGuiDataVideoChannelHeatAGCModes."
  {ser.Ser$JonGuiDataVideoChannelHeatAGCModes$:jon-gui-data-video-channel-heat-agc-mode-unspecified :jon-gui-data-video-channel-heat-agc-mode-unspecified
   ser.Ser$JonGuiDataVideoChannelHeatAGCModes$:jon-gui-data-video-channel-heat-agc-mode-1 :jon-gui-data-video-channel-heat-agc-mode-1
   ser.Ser$JonGuiDataVideoChannelHeatAGCModes$:jon-gui-data-video-channel-heat-agc-mode-2 :jon-gui-data-video-channel-heat-agc-mode-2
   ser.Ser$JonGuiDataVideoChannelHeatAGCModes$:jon-gui-data-video-channel-heat-agc-mode-3 :jon-gui-data-video-channel-heat-agc-mode-3})

;; Enum: JonGuiDataGpsUnits
(def jon-gui-data-gps-units-values
  "Keyword to Java enum mapping for JonGuiDataGpsUnits."
  {:jon-gui-data-gps-units-unspecified ser.Ser$JonGuiDataGpsUnits$:jon-gui-data-gps-units-unspecified
   :jon-gui-data-gps-units-decimal-degrees ser.Ser$JonGuiDataGpsUnits$:jon-gui-data-gps-units-decimal-degrees
   :jon-gui-data-gps-units-degrees-minutes-seconds ser.Ser$JonGuiDataGpsUnits$:jon-gui-data-gps-units-degrees-minutes-seconds
   :jon-gui-data-gps-units-degrees-decimal-minutes ser.Ser$JonGuiDataGpsUnits$:jon-gui-data-gps-units-degrees-decimal-minutes})

(def jon-gui-data-gps-units-keywords
  "Java enum to keyword mapping for JonGuiDataGpsUnits."
  {ser.Ser$JonGuiDataGpsUnits$:jon-gui-data-gps-units-unspecified :jon-gui-data-gps-units-unspecified
   ser.Ser$JonGuiDataGpsUnits$:jon-gui-data-gps-units-decimal-degrees :jon-gui-data-gps-units-decimal-degrees
   ser.Ser$JonGuiDataGpsUnits$:jon-gui-data-gps-units-degrees-minutes-seconds :jon-gui-data-gps-units-degrees-minutes-seconds
   ser.Ser$JonGuiDataGpsUnits$:jon-gui-data-gps-units-degrees-decimal-minutes :jon-gui-data-gps-units-degrees-decimal-minutes})

;; Enum: JonGuiDataGpsFixType
(def jon-gui-data-gps-fix-type-values
  "Keyword to Java enum mapping for JonGuiDataGpsFixType."
  {:jon-gui-data-gps-fix-type-unspecified ser.Ser$JonGuiDataGpsFixType$:jon-gui-data-gps-fix-type-unspecified
   :jon-gui-data-gps-fix-type-none ser.Ser$JonGuiDataGpsFixType$:jon-gui-data-gps-fix-type-none
   :jon-gui-data-gps-fix-type-1-d ser.Ser$JonGuiDataGpsFixType$:jon-gui-data-gps-fix-type-1-d
   :jon-gui-data-gps-fix-type-2-d ser.Ser$JonGuiDataGpsFixType$:jon-gui-data-gps-fix-type-2-d
   :jon-gui-data-gps-fix-type-3-d ser.Ser$JonGuiDataGpsFixType$:jon-gui-data-gps-fix-type-3-d
   :jon-gui-data-gps-fix-type-manual ser.Ser$JonGuiDataGpsFixType$:jon-gui-data-gps-fix-type-manual})

(def jon-gui-data-gps-fix-type-keywords
  "Java enum to keyword mapping for JonGuiDataGpsFixType."
  {ser.Ser$JonGuiDataGpsFixType$:jon-gui-data-gps-fix-type-unspecified :jon-gui-data-gps-fix-type-unspecified
   ser.Ser$JonGuiDataGpsFixType$:jon-gui-data-gps-fix-type-none :jon-gui-data-gps-fix-type-none
   ser.Ser$JonGuiDataGpsFixType$:jon-gui-data-gps-fix-type-1-d :jon-gui-data-gps-fix-type-1-d
   ser.Ser$JonGuiDataGpsFixType$:jon-gui-data-gps-fix-type-2-d :jon-gui-data-gps-fix-type-2-d
   ser.Ser$JonGuiDataGpsFixType$:jon-gui-data-gps-fix-type-3-d :jon-gui-data-gps-fix-type-3-d
   ser.Ser$JonGuiDataGpsFixType$:jon-gui-data-gps-fix-type-manual :jon-gui-data-gps-fix-type-manual})

;; Enum: JonGuiDataCompassUnits
(def jon-gui-data-compass-units-values
  "Keyword to Java enum mapping for JonGuiDataCompassUnits."
  {:jon-gui-data-compass-units-unspecified ser.Ser$JonGuiDataCompassUnits$:jon-gui-data-compass-units-unspecified
   :jon-gui-data-compass-units-degrees ser.Ser$JonGuiDataCompassUnits$:jon-gui-data-compass-units-degrees
   :jon-gui-data-compass-units-mils ser.Ser$JonGuiDataCompassUnits$:jon-gui-data-compass-units-mils
   :jon-gui-data-compass-units-grad ser.Ser$JonGuiDataCompassUnits$:jon-gui-data-compass-units-grad
   :jon-gui-data-compass-units-mrad ser.Ser$JonGuiDataCompassUnits$:jon-gui-data-compass-units-mrad})

(def jon-gui-data-compass-units-keywords
  "Java enum to keyword mapping for JonGuiDataCompassUnits."
  {ser.Ser$JonGuiDataCompassUnits$:jon-gui-data-compass-units-unspecified :jon-gui-data-compass-units-unspecified
   ser.Ser$JonGuiDataCompassUnits$:jon-gui-data-compass-units-degrees :jon-gui-data-compass-units-degrees
   ser.Ser$JonGuiDataCompassUnits$:jon-gui-data-compass-units-mils :jon-gui-data-compass-units-mils
   ser.Ser$JonGuiDataCompassUnits$:jon-gui-data-compass-units-grad :jon-gui-data-compass-units-grad
   ser.Ser$JonGuiDataCompassUnits$:jon-gui-data-compass-units-mrad :jon-gui-data-compass-units-mrad})

;; Enum: JonGuiDataAccumulatorStateIdx
(def jon-gui-data-accumulator-state-idx-values
  "Keyword to Java enum mapping for JonGuiDataAccumulatorStateIdx."
  {:jon-gui-data-accumulator-state-unspecified ser.Ser$JonGuiDataAccumulatorStateIdx$:jon-gui-data-accumulator-state-unspecified
   :jon-gui-data-accumulator-state-unknown ser.Ser$JonGuiDataAccumulatorStateIdx$:jon-gui-data-accumulator-state-unknown
   :jon-gui-data-accumulator-state-empty ser.Ser$JonGuiDataAccumulatorStateIdx$:jon-gui-data-accumulator-state-empty
   :jon-gui-data-accumulator-state-1 ser.Ser$JonGuiDataAccumulatorStateIdx$:jon-gui-data-accumulator-state-1
   :jon-gui-data-accumulator-state-2 ser.Ser$JonGuiDataAccumulatorStateIdx$:jon-gui-data-accumulator-state-2
   :jon-gui-data-accumulator-state-3 ser.Ser$JonGuiDataAccumulatorStateIdx$:jon-gui-data-accumulator-state-3
   :jon-gui-data-accumulator-state-4 ser.Ser$JonGuiDataAccumulatorStateIdx$:jon-gui-data-accumulator-state-4
   :jon-gui-data-accumulator-state-5 ser.Ser$JonGuiDataAccumulatorStateIdx$:jon-gui-data-accumulator-state-5
   :jon-gui-data-accumulator-state-6 ser.Ser$JonGuiDataAccumulatorStateIdx$:jon-gui-data-accumulator-state-6
   :jon-gui-data-accumulator-state-full ser.Ser$JonGuiDataAccumulatorStateIdx$:jon-gui-data-accumulator-state-full
   :jon-gui-data-accumulator-state-charging ser.Ser$JonGuiDataAccumulatorStateIdx$:jon-gui-data-accumulator-state-charging})

(def jon-gui-data-accumulator-state-idx-keywords
  "Java enum to keyword mapping for JonGuiDataAccumulatorStateIdx."
  {ser.Ser$JonGuiDataAccumulatorStateIdx$:jon-gui-data-accumulator-state-unspecified :jon-gui-data-accumulator-state-unspecified
   ser.Ser$JonGuiDataAccumulatorStateIdx$:jon-gui-data-accumulator-state-unknown :jon-gui-data-accumulator-state-unknown
   ser.Ser$JonGuiDataAccumulatorStateIdx$:jon-gui-data-accumulator-state-empty :jon-gui-data-accumulator-state-empty
   ser.Ser$JonGuiDataAccumulatorStateIdx$:jon-gui-data-accumulator-state-1 :jon-gui-data-accumulator-state-1
   ser.Ser$JonGuiDataAccumulatorStateIdx$:jon-gui-data-accumulator-state-2 :jon-gui-data-accumulator-state-2
   ser.Ser$JonGuiDataAccumulatorStateIdx$:jon-gui-data-accumulator-state-3 :jon-gui-data-accumulator-state-3
   ser.Ser$JonGuiDataAccumulatorStateIdx$:jon-gui-data-accumulator-state-4 :jon-gui-data-accumulator-state-4
   ser.Ser$JonGuiDataAccumulatorStateIdx$:jon-gui-data-accumulator-state-5 :jon-gui-data-accumulator-state-5
   ser.Ser$JonGuiDataAccumulatorStateIdx$:jon-gui-data-accumulator-state-6 :jon-gui-data-accumulator-state-6
   ser.Ser$JonGuiDataAccumulatorStateIdx$:jon-gui-data-accumulator-state-full :jon-gui-data-accumulator-state-full
   ser.Ser$JonGuiDataAccumulatorStateIdx$:jon-gui-data-accumulator-state-charging :jon-gui-data-accumulator-state-charging})

;; Enum: JonGuiDataTimeFormats
(def jon-gui-data-time-formats-values
  "Keyword to Java enum mapping for JonGuiDataTimeFormats."
  {:jon-gui-data-time-format-unspecified ser.Ser$JonGuiDataTimeFormats$:jon-gui-data-time-format-unspecified
   :jon-gui-data-time-format-h-m-s ser.Ser$JonGuiDataTimeFormats$:jon-gui-data-time-format-h-m-s
   :jon-gui-data-time-format-y-m-d-h-m-s ser.Ser$JonGuiDataTimeFormats$JON_GUI_DATA_TIME_FORMAT_Y_m_D_H_M_S})

(def jon-gui-data-time-formats-keywords
  "Java enum to keyword mapping for JonGuiDataTimeFormats."
  {ser.Ser$JonGuiDataTimeFormats$:jon-gui-data-time-format-unspecified :jon-gui-data-time-format-unspecified
   ser.Ser$JonGuiDataTimeFormats$:jon-gui-data-time-format-h-m-s :jon-gui-data-time-format-h-m-s
   ser.Ser$JonGuiDataTimeFormats$JON_GUI_DATA_TIME_FORMAT_Y_m_D_H_M_S :jon-gui-data-time-format-y-m-d-h-m-s})

;; Enum: JonGuiDataRotaryDirection
(def jon-gui-data-rotary-direction-values
  "Keyword to Java enum mapping for JonGuiDataRotaryDirection."
  {:jon-gui-data-rotary-direction-unspecified ser.Ser$JonGuiDataRotaryDirection$:jon-gui-data-rotary-direction-unspecified
   :jon-gui-data-rotary-direction-clockwise ser.Ser$JonGuiDataRotaryDirection$:jon-gui-data-rotary-direction-clockwise
   :jon-gui-data-rotary-direction-counter-clockwise ser.Ser$JonGuiDataRotaryDirection$:jon-gui-data-rotary-direction-counter-clockwise})

(def jon-gui-data-rotary-direction-keywords
  "Java enum to keyword mapping for JonGuiDataRotaryDirection."
  {ser.Ser$JonGuiDataRotaryDirection$:jon-gui-data-rotary-direction-unspecified :jon-gui-data-rotary-direction-unspecified
   ser.Ser$JonGuiDataRotaryDirection$:jon-gui-data-rotary-direction-clockwise :jon-gui-data-rotary-direction-clockwise
   ser.Ser$JonGuiDataRotaryDirection$:jon-gui-data-rotary-direction-counter-clockwise :jon-gui-data-rotary-direction-counter-clockwise})

;; Enum: JonGuiDataLrfScanModes
(def jon-gui-data-lrf-scan-modes-values
  "Keyword to Java enum mapping for JonGuiDataLrfScanModes."
  {:jon-gui-data-lrf-scan-mode-unspecified ser.Ser$JonGuiDataLrfScanModes$:jon-gui-data-lrf-scan-mode-unspecified
   :jon-gui-data-lrf-scan-mode-1-hz-continuous ser.Ser$JonGuiDataLrfScanModes$:jon-gui-data-lrf-scan-mode-1-hz-continuous
   :jon-gui-data-lrf-scan-mode-4-hz-continuous ser.Ser$JonGuiDataLrfScanModes$:jon-gui-data-lrf-scan-mode-4-hz-continuous
   :jon-gui-data-lrf-scan-mode-10-hz-continuous ser.Ser$JonGuiDataLrfScanModes$:jon-gui-data-lrf-scan-mode-10-hz-continuous
   :jon-gui-data-lrf-scan-mode-20-hz-continuous ser.Ser$JonGuiDataLrfScanModes$:jon-gui-data-lrf-scan-mode-20-hz-continuous
   :jon-gui-data-lrf-scan-mode-100-hz-continuous ser.Ser$JonGuiDataLrfScanModes$:jon-gui-data-lrf-scan-mode-100-hz-continuous
   :jon-gui-data-lrf-scan-mode-200-hz-continuous ser.Ser$JonGuiDataLrfScanModes$:jon-gui-data-lrf-scan-mode-200-hz-continuous})

(def jon-gui-data-lrf-scan-modes-keywords
  "Java enum to keyword mapping for JonGuiDataLrfScanModes."
  {ser.Ser$JonGuiDataLrfScanModes$:jon-gui-data-lrf-scan-mode-unspecified :jon-gui-data-lrf-scan-mode-unspecified
   ser.Ser$JonGuiDataLrfScanModes$:jon-gui-data-lrf-scan-mode-1-hz-continuous :jon-gui-data-lrf-scan-mode-1-hz-continuous
   ser.Ser$JonGuiDataLrfScanModes$:jon-gui-data-lrf-scan-mode-4-hz-continuous :jon-gui-data-lrf-scan-mode-4-hz-continuous
   ser.Ser$JonGuiDataLrfScanModes$:jon-gui-data-lrf-scan-mode-10-hz-continuous :jon-gui-data-lrf-scan-mode-10-hz-continuous
   ser.Ser$JonGuiDataLrfScanModes$:jon-gui-data-lrf-scan-mode-20-hz-continuous :jon-gui-data-lrf-scan-mode-20-hz-continuous
   ser.Ser$JonGuiDataLrfScanModes$:jon-gui-data-lrf-scan-mode-100-hz-continuous :jon-gui-data-lrf-scan-mode-100-hz-continuous
   ser.Ser$JonGuiDataLrfScanModes$:jon-gui-data-lrf-scan-mode-200-hz-continuous :jon-gui-data-lrf-scan-mode-200-hz-continuous})

;; Enum: JonGuiDatatLrfLaserPointerModes
(def jon-gui-datat-lrf-laser-pointer-modes-values
  "Keyword to Java enum mapping for JonGuiDatatLrfLaserPointerModes."
  {:jon-gui-data-lrf-laser-pointer-mode-unspecified ser.Ser$JonGuiDatatLrfLaserPointerModes$:jon-gui-data-lrf-laser-pointer-mode-unspecified
   :jon-gui-data-lrf-laser-pointer-mode-off ser.Ser$JonGuiDatatLrfLaserPointerModes$:jon-gui-data-lrf-laser-pointer-mode-off
   :jon-gui-data-lrf-laser-pointer-mode-on-1 ser.Ser$JonGuiDatatLrfLaserPointerModes$:jon-gui-data-lrf-laser-pointer-mode-on-1
   :jon-gui-data-lrf-laser-pointer-mode-on-2 ser.Ser$JonGuiDatatLrfLaserPointerModes$:jon-gui-data-lrf-laser-pointer-mode-on-2})

(def jon-gui-datat-lrf-laser-pointer-modes-keywords
  "Java enum to keyword mapping for JonGuiDatatLrfLaserPointerModes."
  {ser.Ser$JonGuiDatatLrfLaserPointerModes$:jon-gui-data-lrf-laser-pointer-mode-unspecified :jon-gui-data-lrf-laser-pointer-mode-unspecified
   ser.Ser$JonGuiDatatLrfLaserPointerModes$:jon-gui-data-lrf-laser-pointer-mode-off :jon-gui-data-lrf-laser-pointer-mode-off
   ser.Ser$JonGuiDatatLrfLaserPointerModes$:jon-gui-data-lrf-laser-pointer-mode-on-1 :jon-gui-data-lrf-laser-pointer-mode-on-1
   ser.Ser$JonGuiDatatLrfLaserPointerModes$:jon-gui-data-lrf-laser-pointer-mode-on-2 :jon-gui-data-lrf-laser-pointer-mode-on-2})

;; Enum: JonGuiDataCompassCalibrateStatus
(def jon-gui-data-compass-calibrate-status-values
  "Keyword to Java enum mapping for JonGuiDataCompassCalibrateStatus."
  {:jon-gui-data-compass-calibrate-status-unspecified ser.Ser$JonGuiDataCompassCalibrateStatus$:jon-gui-data-compass-calibrate-status-unspecified
   :jon-gui-data-compass-calibrate-status-not-calibrating ser.Ser$JonGuiDataCompassCalibrateStatus$:jon-gui-data-compass-calibrate-status-not-calibrating
   :jon-gui-data-compass-calibrate-status-calibrating-short ser.Ser$JonGuiDataCompassCalibrateStatus$:jon-gui-data-compass-calibrate-status-calibrating-short
   :jon-gui-data-compass-calibrate-status-calibrating-long ser.Ser$JonGuiDataCompassCalibrateStatus$:jon-gui-data-compass-calibrate-status-calibrating-long
   :jon-gui-data-compass-calibrate-status-finished ser.Ser$JonGuiDataCompassCalibrateStatus$:jon-gui-data-compass-calibrate-status-finished
   :jon-gui-data-compass-calibrate-status-error ser.Ser$JonGuiDataCompassCalibrateStatus$:jon-gui-data-compass-calibrate-status-error})

(def jon-gui-data-compass-calibrate-status-keywords
  "Java enum to keyword mapping for JonGuiDataCompassCalibrateStatus."
  {ser.Ser$JonGuiDataCompassCalibrateStatus$:jon-gui-data-compass-calibrate-status-unspecified :jon-gui-data-compass-calibrate-status-unspecified
   ser.Ser$JonGuiDataCompassCalibrateStatus$:jon-gui-data-compass-calibrate-status-not-calibrating :jon-gui-data-compass-calibrate-status-not-calibrating
   ser.Ser$JonGuiDataCompassCalibrateStatus$:jon-gui-data-compass-calibrate-status-calibrating-short :jon-gui-data-compass-calibrate-status-calibrating-short
   ser.Ser$JonGuiDataCompassCalibrateStatus$:jon-gui-data-compass-calibrate-status-calibrating-long :jon-gui-data-compass-calibrate-status-calibrating-long
   ser.Ser$JonGuiDataCompassCalibrateStatus$:jon-gui-data-compass-calibrate-status-finished :jon-gui-data-compass-calibrate-status-finished
   ser.Ser$JonGuiDataCompassCalibrateStatus$:jon-gui-data-compass-calibrate-status-error :jon-gui-data-compass-calibrate-status-error})

;; Enum: JonGuiDataRotaryMode
(def jon-gui-data-rotary-mode-values
  "Keyword to Java enum mapping for JonGuiDataRotaryMode."
  {:jon-gui-data-rotary-mode-unspecified ser.Ser$JonGuiDataRotaryMode$:jon-gui-data-rotary-mode-unspecified
   :jon-gui-data-rotary-mode-initialization ser.Ser$JonGuiDataRotaryMode$:jon-gui-data-rotary-mode-initialization
   :jon-gui-data-rotary-mode-speed ser.Ser$JonGuiDataRotaryMode$:jon-gui-data-rotary-mode-speed
   :jon-gui-data-rotary-mode-position ser.Ser$JonGuiDataRotaryMode$:jon-gui-data-rotary-mode-position
   :jon-gui-data-rotary-mode-stabilization ser.Ser$JonGuiDataRotaryMode$:jon-gui-data-rotary-mode-stabilization
   :jon-gui-data-rotary-mode-targeting ser.Ser$JonGuiDataRotaryMode$:jon-gui-data-rotary-mode-targeting
   :jon-gui-data-rotary-mode-video-tracker ser.Ser$JonGuiDataRotaryMode$:jon-gui-data-rotary-mode-video-tracker})

(def jon-gui-data-rotary-mode-keywords
  "Java enum to keyword mapping for JonGuiDataRotaryMode."
  {ser.Ser$JonGuiDataRotaryMode$:jon-gui-data-rotary-mode-unspecified :jon-gui-data-rotary-mode-unspecified
   ser.Ser$JonGuiDataRotaryMode$:jon-gui-data-rotary-mode-initialization :jon-gui-data-rotary-mode-initialization
   ser.Ser$JonGuiDataRotaryMode$:jon-gui-data-rotary-mode-speed :jon-gui-data-rotary-mode-speed
   ser.Ser$JonGuiDataRotaryMode$:jon-gui-data-rotary-mode-position :jon-gui-data-rotary-mode-position
   ser.Ser$JonGuiDataRotaryMode$:jon-gui-data-rotary-mode-stabilization :jon-gui-data-rotary-mode-stabilization
   ser.Ser$JonGuiDataRotaryMode$:jon-gui-data-rotary-mode-targeting :jon-gui-data-rotary-mode-targeting
   ser.Ser$JonGuiDataRotaryMode$:jon-gui-data-rotary-mode-video-tracker :jon-gui-data-rotary-mode-video-tracker})

;; Enum: JonGuiDataVideoChannel
(def jon-gui-data-video-channel-values
  "Keyword to Java enum mapping for JonGuiDataVideoChannel."
  {:jon-gui-data-video-channel-unspecified ser.Ser$JonGuiDataVideoChannel$:jon-gui-data-video-channel-unspecified
   :jon-gui-data-video-channel-heat ser.Ser$JonGuiDataVideoChannel$:jon-gui-data-video-channel-heat
   :jon-gui-data-video-channel-day ser.Ser$JonGuiDataVideoChannel$:jon-gui-data-video-channel-day})

(def jon-gui-data-video-channel-keywords
  "Java enum to keyword mapping for JonGuiDataVideoChannel."
  {ser.Ser$JonGuiDataVideoChannel$:jon-gui-data-video-channel-unspecified :jon-gui-data-video-channel-unspecified
   ser.Ser$JonGuiDataVideoChannel$:jon-gui-data-video-channel-heat :jon-gui-data-video-channel-heat
   ser.Ser$JonGuiDataVideoChannel$:jon-gui-data-video-channel-day :jon-gui-data-video-channel-day})

;; Enum: JonGuiDataRecOsdScreen
(def jon-gui-data-rec-osd-screen-values
  "Keyword to Java enum mapping for JonGuiDataRecOsdScreen."
  {:jon-gui-data-rec-osd-screen-unspecified ser.Ser$JonGuiDataRecOsdScreen$:jon-gui-data-rec-osd-screen-unspecified
   :jon-gui-data-rec-osd-screen-main ser.Ser$JonGuiDataRecOsdScreen$:jon-gui-data-rec-osd-screen-main
   :jon-gui-data-rec-osd-screen-lrf-measure ser.Ser$JonGuiDataRecOsdScreen$:jon-gui-data-rec-osd-screen-lrf-measure
   :jon-gui-data-rec-osd-screen-lrf-result ser.Ser$JonGuiDataRecOsdScreen$:jon-gui-data-rec-osd-screen-lrf-result
   :jon-gui-data-rec-osd-screen-lrf-result-simplified ser.Ser$JonGuiDataRecOsdScreen$:jon-gui-data-rec-osd-screen-lrf-result-simplified})

(def jon-gui-data-rec-osd-screen-keywords
  "Java enum to keyword mapping for JonGuiDataRecOsdScreen."
  {ser.Ser$JonGuiDataRecOsdScreen$:jon-gui-data-rec-osd-screen-unspecified :jon-gui-data-rec-osd-screen-unspecified
   ser.Ser$JonGuiDataRecOsdScreen$:jon-gui-data-rec-osd-screen-main :jon-gui-data-rec-osd-screen-main
   ser.Ser$JonGuiDataRecOsdScreen$:jon-gui-data-rec-osd-screen-lrf-measure :jon-gui-data-rec-osd-screen-lrf-measure
   ser.Ser$JonGuiDataRecOsdScreen$:jon-gui-data-rec-osd-screen-lrf-result :jon-gui-data-rec-osd-screen-lrf-result
   ser.Ser$JonGuiDataRecOsdScreen$:jon-gui-data-rec-osd-screen-lrf-result-simplified :jon-gui-data-rec-osd-screen-lrf-result-simplified})

;; Enum: JonGuiDataFxModeDay
(def jon-gui-data-fx-mode-day-values
  "Keyword to Java enum mapping for JonGuiDataFxModeDay."
  {:jon-gui-data-fx-mode-day-default ser.Ser$JonGuiDataFxModeDay$:jon-gui-data-fx-mode-day-default
   :jon-gui-data-fx-mode-day-a ser.Ser$JonGuiDataFxModeDay$:jon-gui-data-fx-mode-day-a
   :jon-gui-data-fx-mode-day-b ser.Ser$JonGuiDataFxModeDay$:jon-gui-data-fx-mode-day-b
   :jon-gui-data-fx-mode-day-c ser.Ser$JonGuiDataFxModeDay$:jon-gui-data-fx-mode-day-c
   :jon-gui-data-fx-mode-day-d ser.Ser$JonGuiDataFxModeDay$:jon-gui-data-fx-mode-day-d
   :jon-gui-data-fx-mode-day-e ser.Ser$JonGuiDataFxModeDay$:jon-gui-data-fx-mode-day-e
   :jon-gui-data-fx-mode-day-f ser.Ser$JonGuiDataFxModeDay$:jon-gui-data-fx-mode-day-f})

(def jon-gui-data-fx-mode-day-keywords
  "Java enum to keyword mapping for JonGuiDataFxModeDay."
  {ser.Ser$JonGuiDataFxModeDay$:jon-gui-data-fx-mode-day-default :jon-gui-data-fx-mode-day-default
   ser.Ser$JonGuiDataFxModeDay$:jon-gui-data-fx-mode-day-a :jon-gui-data-fx-mode-day-a
   ser.Ser$JonGuiDataFxModeDay$:jon-gui-data-fx-mode-day-b :jon-gui-data-fx-mode-day-b
   ser.Ser$JonGuiDataFxModeDay$:jon-gui-data-fx-mode-day-c :jon-gui-data-fx-mode-day-c
   ser.Ser$JonGuiDataFxModeDay$:jon-gui-data-fx-mode-day-d :jon-gui-data-fx-mode-day-d
   ser.Ser$JonGuiDataFxModeDay$:jon-gui-data-fx-mode-day-e :jon-gui-data-fx-mode-day-e
   ser.Ser$JonGuiDataFxModeDay$:jon-gui-data-fx-mode-day-f :jon-gui-data-fx-mode-day-f})

;; Enum: JonGuiDataFxModeHeat
(def jon-gui-data-fx-mode-heat-values
  "Keyword to Java enum mapping for JonGuiDataFxModeHeat."
  {:jon-gui-data-fx-mode-heat-default ser.Ser$JonGuiDataFxModeHeat$:jon-gui-data-fx-mode-heat-default
   :jon-gui-data-fx-mode-heat-a ser.Ser$JonGuiDataFxModeHeat$:jon-gui-data-fx-mode-heat-a
   :jon-gui-data-fx-mode-heat-b ser.Ser$JonGuiDataFxModeHeat$:jon-gui-data-fx-mode-heat-b
   :jon-gui-data-fx-mode-heat-c ser.Ser$JonGuiDataFxModeHeat$:jon-gui-data-fx-mode-heat-c
   :jon-gui-data-fx-mode-heat-d ser.Ser$JonGuiDataFxModeHeat$:jon-gui-data-fx-mode-heat-d
   :jon-gui-data-fx-mode-heat-e ser.Ser$JonGuiDataFxModeHeat$:jon-gui-data-fx-mode-heat-e
   :jon-gui-data-fx-mode-heat-f ser.Ser$JonGuiDataFxModeHeat$:jon-gui-data-fx-mode-heat-f})

(def jon-gui-data-fx-mode-heat-keywords
  "Java enum to keyword mapping for JonGuiDataFxModeHeat."
  {ser.Ser$JonGuiDataFxModeHeat$:jon-gui-data-fx-mode-heat-default :jon-gui-data-fx-mode-heat-default
   ser.Ser$JonGuiDataFxModeHeat$:jon-gui-data-fx-mode-heat-a :jon-gui-data-fx-mode-heat-a
   ser.Ser$JonGuiDataFxModeHeat$:jon-gui-data-fx-mode-heat-b :jon-gui-data-fx-mode-heat-b
   ser.Ser$JonGuiDataFxModeHeat$:jon-gui-data-fx-mode-heat-c :jon-gui-data-fx-mode-heat-c
   ser.Ser$JonGuiDataFxModeHeat$:jon-gui-data-fx-mode-heat-d :jon-gui-data-fx-mode-heat-d
   ser.Ser$JonGuiDataFxModeHeat$:jon-gui-data-fx-mode-heat-e :jon-gui-data-fx-mode-heat-e
   ser.Ser$JonGuiDataFxModeHeat$:jon-gui-data-fx-mode-heat-f :jon-gui-data-fx-mode-heat-f})

;; Enum: JonGuiDataSystemLocalizations
(def jon-gui-data-system-localizations-values
  "Keyword to Java enum mapping for JonGuiDataSystemLocalizations."
  {:jon-gui-data-system-localization-unspecified ser.Ser$JonGuiDataSystemLocalizations$:jon-gui-data-system-localization-unspecified
   :jon-gui-data-system-localization-en ser.Ser$JonGuiDataSystemLocalizations$:jon-gui-data-system-localization-en
   :jon-gui-data-system-localization-ua ser.Ser$JonGuiDataSystemLocalizations$:jon-gui-data-system-localization-ua
   :jon-gui-data-system-localization-ar ser.Ser$JonGuiDataSystemLocalizations$:jon-gui-data-system-localization-ar
   :jon-gui-data-system-localization-cs ser.Ser$JonGuiDataSystemLocalizations$:jon-gui-data-system-localization-cs})

(def jon-gui-data-system-localizations-keywords
  "Java enum to keyword mapping for JonGuiDataSystemLocalizations."
  {ser.Ser$JonGuiDataSystemLocalizations$:jon-gui-data-system-localization-unspecified :jon-gui-data-system-localization-unspecified
   ser.Ser$JonGuiDataSystemLocalizations$:jon-gui-data-system-localization-en :jon-gui-data-system-localization-en
   ser.Ser$JonGuiDataSystemLocalizations$:jon-gui-data-system-localization-ua :jon-gui-data-system-localization-ua
   ser.Ser$JonGuiDataSystemLocalizations$:jon-gui-data-system-localization-ar :jon-gui-data-system-localization-ar
   ser.Ser$JonGuiDataSystemLocalizations$:jon-gui-data-system-localization-cs :jon-gui-data-system-localization-cs})

;; Enum: JonGuiDataClientType
(def jon-gui-data-client-type-values
  "Keyword to Java enum mapping for JonGuiDataClientType."
  {:jon-gui-data-client-type-unspecified ser.Ser$JonGuiDataClientType$:jon-gui-data-client-type-unspecified
   :jon-gui-data-client-type-internal-cv ser.Ser$JonGuiDataClientType$:jon-gui-data-client-type-internal-cv
   :jon-gui-data-client-type-local-network ser.Ser$JonGuiDataClientType$:jon-gui-data-client-type-local-network
   :jon-gui-data-client-type-certificate-protected ser.Ser$JonGuiDataClientType$:jon-gui-data-client-type-certificate-protected
   :jon-gui-data-client-type-lira ser.Ser$JonGuiDataClientType$:jon-gui-data-client-type-lira})

(def jon-gui-data-client-type-keywords
  "Java enum to keyword mapping for JonGuiDataClientType."
  {ser.Ser$JonGuiDataClientType$:jon-gui-data-client-type-unspecified :jon-gui-data-client-type-unspecified
   ser.Ser$JonGuiDataClientType$:jon-gui-data-client-type-internal-cv :jon-gui-data-client-type-internal-cv
   ser.Ser$JonGuiDataClientType$:jon-gui-data-client-type-local-network :jon-gui-data-client-type-local-network
   ser.Ser$JonGuiDataClientType$:jon-gui-data-client-type-certificate-protected :jon-gui-data-client-type-certificate-protected
   ser.Ser$JonGuiDataClientType$:jon-gui-data-client-type-lira :jon-gui-data-client-type-lira})


;; =============================================================================
;; Builders and Parsers
;; =============================================================================

(defn BUILD-FN-NAME
  "Build a PROTO-NAME protobuf message from a map."
  [m]
  (let [builder (JAVA-CLASS/newBuilder)]
    REGULAR-FIELDS
    ONEOF-PAYLOAD
    (.build builder)))
(defn PARSE-FN-NAME
  "Parse a PROTO-NAME protobuf message to a map."
  [^JAVA-CLASS proto]
  (cond-> {}
    REGULAR-FIELDS
    ONEOF-PAYLOAD))
(defn BUILD-FN-NAME
  "Build a PROTO-NAME protobuf message from a map."
  [m]
  (let [builder (JAVA-CLASS/newBuilder)]
    REGULAR-FIELDS
    ONEOF-PAYLOAD
    (.build builder)))
(defn PARSE-FN-NAME
  "Parse a PROTO-NAME protobuf message to a map."
  [^JAVA-CLASS proto]
  (cond-> {}
    REGULAR-FIELDS
    ONEOF-PAYLOAD))
(defn BUILD-FN-NAME
  "Build a PROTO-NAME protobuf message from a map."
  [m]
  (let [builder (JAVA-CLASS/newBuilder)]
    REGULAR-FIELDS
    ONEOF-PAYLOAD
    (.build builder)))
(defn PARSE-FN-NAME
  "Parse a PROTO-NAME protobuf message to a map."
  [^JAVA-CLASS proto]
  (cond-> {}
    REGULAR-FIELDS
    ONEOF-PAYLOAD))
(defn BUILD-FN-NAME
  "Build a PROTO-NAME protobuf message from a map."
  [m]
  (let [builder (JAVA-CLASS/newBuilder)]
    REGULAR-FIELDS
    ONEOF-PAYLOAD
    (.build builder)))
(defn PARSE-FN-NAME
  "Parse a PROTO-NAME protobuf message to a map."
  [^JAVA-CLASS proto]
  (cond-> {}
    REGULAR-FIELDS
    ONEOF-PAYLOAD))
(defn BUILD-FN-NAME
  "Build a PROTO-NAME protobuf message from a map."
  [m]
  (let [builder (JAVA-CLASS/newBuilder)]
    REGULAR-FIELDS
    ONEOF-PAYLOAD
    (.build builder)))
(defn PARSE-FN-NAME
  "Parse a PROTO-NAME protobuf message to a map."
  [^JAVA-CLASS proto]
  (cond-> {}
    REGULAR-FIELDS
    ONEOF-PAYLOAD))
(defn BUILD-FN-NAME
  "Build a PROTO-NAME protobuf message from a map."
  [m]
  (let [builder (JAVA-CLASS/newBuilder)]
    REGULAR-FIELDS
    ONEOF-PAYLOAD
    (.build builder)))
(defn PARSE-FN-NAME
  "Parse a PROTO-NAME protobuf message to a map."
  [^JAVA-CLASS proto]
  (cond-> {}
    REGULAR-FIELDS
    ONEOF-PAYLOAD))
(defn BUILD-FN-NAME
  "Build a PROTO-NAME protobuf message from a map."
  [m]
  (let [builder (JAVA-CLASS/newBuilder)]
    REGULAR-FIELDS
    ONEOF-PAYLOAD
    (.build builder)))
(defn PARSE-FN-NAME
  "Parse a PROTO-NAME protobuf message to a map."
  [^JAVA-CLASS proto]
  (cond-> {}
    REGULAR-FIELDS
    ONEOF-PAYLOAD))
(defn BUILD-FN-NAME
  "Build a PROTO-NAME protobuf message from a map."
  [m]
  (let [builder (JAVA-CLASS/newBuilder)]
    REGULAR-FIELDS
    ONEOF-PAYLOAD
    (.build builder)))
(defn PARSE-FN-NAME
  "Parse a PROTO-NAME protobuf message to a map."
  [^JAVA-CLASS proto]
  (cond-> {}
    REGULAR-FIELDS
    ONEOF-PAYLOAD))
(defn BUILD-FN-NAME
  "Build a PROTO-NAME protobuf message from a map."
  [m]
  (let [builder (JAVA-CLASS/newBuilder)]
    REGULAR-FIELDS
    ONEOF-PAYLOAD
    (.build builder)))
(defn PARSE-FN-NAME
  "Parse a PROTO-NAME protobuf message to a map."
  [^JAVA-CLASS proto]
  (cond-> {}
    REGULAR-FIELDS
    ONEOF-PAYLOAD))
(defn BUILD-FN-NAME
  "Build a PROTO-NAME protobuf message from a map."
  [m]
  (let [builder (JAVA-CLASS/newBuilder)]
    REGULAR-FIELDS
    ONEOF-PAYLOAD
    (.build builder)))
(defn PARSE-FN-NAME
  "Parse a PROTO-NAME protobuf message to a map."
  [^JAVA-CLASS proto]
  (cond-> {}
    REGULAR-FIELDS
    ONEOF-PAYLOAD))
(defn BUILD-FN-NAME
  "Build a PROTO-NAME protobuf message from a map."
  [m]
  (let [builder (JAVA-CLASS/newBuilder)]
    REGULAR-FIELDS
    ONEOF-PAYLOAD
    (.build builder)))
(defn PARSE-FN-NAME
  "Parse a PROTO-NAME protobuf message to a map."
  [^JAVA-CLASS proto]
  (cond-> {}
    REGULAR-FIELDS
    ONEOF-PAYLOAD))
(defn BUILD-FN-NAME
  "Build a PROTO-NAME protobuf message from a map."
  [m]
  (let [builder (JAVA-CLASS/newBuilder)]
    REGULAR-FIELDS
    ONEOF-PAYLOAD
    (.build builder)))
(defn PARSE-FN-NAME
  "Parse a PROTO-NAME protobuf message to a map."
  [^JAVA-CLASS proto]
  (cond-> {}
    REGULAR-FIELDS
    ONEOF-PAYLOAD))
(defn BUILD-FN-NAME
  "Build a PROTO-NAME protobuf message from a map."
  [m]
  (let [builder (JAVA-CLASS/newBuilder)]
    REGULAR-FIELDS
    ONEOF-PAYLOAD
    (.build builder)))
(defn PARSE-FN-NAME
  "Parse a PROTO-NAME protobuf message to a map."
  [^JAVA-CLASS proto]
  (cond-> {}
    REGULAR-FIELDS
    ONEOF-PAYLOAD))
(defn BUILD-FN-NAME
  "Build a PROTO-NAME protobuf message from a map."
  [m]
  (let [builder (JAVA-CLASS/newBuilder)]
    REGULAR-FIELDS
    ONEOF-PAYLOAD
    (.build builder)))
(defn PARSE-FN-NAME
  "Parse a PROTO-NAME protobuf message to a map."
  [^JAVA-CLASS proto]
  (cond-> {}
    REGULAR-FIELDS
    ONEOF-PAYLOAD))
(defn BUILD-FN-NAME
  "Build a PROTO-NAME protobuf message from a map."
  [m]
  (let [builder (JAVA-CLASS/newBuilder)]
    REGULAR-FIELDS
    ONEOF-PAYLOAD
    (.build builder)))
(defn PARSE-FN-NAME
  "Parse a PROTO-NAME protobuf message to a map."
  [^JAVA-CLASS proto]
  (cond-> {}
    REGULAR-FIELDS
    ONEOF-PAYLOAD))
(defn BUILD-FN-NAME
  "Build a PROTO-NAME protobuf message from a map."
  [m]
  (let [builder (JAVA-CLASS/newBuilder)]
    REGULAR-FIELDS
    ONEOF-PAYLOAD
    (.build builder)))
(defn PARSE-FN-NAME
  "Parse a PROTO-NAME protobuf message to a map."
  [^JAVA-CLASS proto]
  (cond-> {}
    REGULAR-FIELDS
    ONEOF-PAYLOAD))
(defn BUILD-FN-NAME
  "Build a PROTO-NAME protobuf message from a map."
  [m]
  (let [builder (JAVA-CLASS/newBuilder)]
    REGULAR-FIELDS
    ONEOF-PAYLOAD
    (.build builder)))
(defn PARSE-FN-NAME
  "Parse a PROTO-NAME protobuf message to a map."
  [^JAVA-CLASS proto]
  (cond-> {}
    REGULAR-FIELDS
    ONEOF-PAYLOAD))
(defn BUILD-FN-NAME
  "Build a PROTO-NAME protobuf message from a map."
  [m]
  (let [builder (JAVA-CLASS/newBuilder)]
    REGULAR-FIELDS
    ONEOF-PAYLOAD
    (.build builder)))
(defn PARSE-FN-NAME
  "Parse a PROTO-NAME protobuf message to a map."
  [^JAVA-CLASS proto]
  (cond-> {}
    REGULAR-FIELDS
    ONEOF-PAYLOAD))
(defn BUILD-FN-NAME
  "Build a PROTO-NAME protobuf message from a map."
  [m]
  (let [builder (JAVA-CLASS/newBuilder)]
    REGULAR-FIELDS
    ONEOF-PAYLOAD
    (.build builder)))
(defn PARSE-FN-NAME
  "Parse a PROTO-NAME protobuf message to a map."
  [^JAVA-CLASS proto]
  (cond-> {}
    REGULAR-FIELDS
    ONEOF-PAYLOAD))
(defn BUILD-FN-NAME
  "Build a PROTO-NAME protobuf message from a map."
  [m]
  (let [builder (JAVA-CLASS/newBuilder)]
    REGULAR-FIELDS
    ONEOF-PAYLOAD
    (.build builder)))
(defn PARSE-FN-NAME
  "Parse a PROTO-NAME protobuf message to a map."
  [^JAVA-CLASS proto]
  (cond-> {}
    REGULAR-FIELDS
    ONEOF-PAYLOAD))
(defn BUILD-FN-NAME
  "Build a PROTO-NAME protobuf message from a map."
  [m]
  (let [builder (JAVA-CLASS/newBuilder)]
    REGULAR-FIELDS
    ONEOF-PAYLOAD
    (.build builder)))
(defn PARSE-FN-NAME
  "Parse a PROTO-NAME protobuf message to a map."
  [^JAVA-CLASS proto]
  (cond-> {}
    REGULAR-FIELDS
    ONEOF-PAYLOAD))
(defn BUILD-FN-NAME
  "Build a PROTO-NAME protobuf message from a map."
  [m]
  (let [builder (JAVA-CLASS/newBuilder)]
    REGULAR-FIELDS
    ONEOF-PAYLOAD
    (.build builder)))
(defn PARSE-FN-NAME
  "Parse a PROTO-NAME protobuf message to a map."
  [^JAVA-CLASS proto]
  (cond-> {}
    REGULAR-FIELDS
    ONEOF-PAYLOAD))
(defn BUILD-FN-NAME
  "Build a PROTO-NAME protobuf message from a map."
  [m]
  (let [builder (JAVA-CLASS/newBuilder)]
    REGULAR-FIELDS
    ONEOF-PAYLOAD
    (.build builder)))
(defn PARSE-FN-NAME
  "Parse a PROTO-NAME protobuf message to a map."
  [^JAVA-CLASS proto]
  (cond-> {}
    REGULAR-FIELDS
    ONEOF-PAYLOAD))
(defn BUILD-FN-NAME
  "Build a PROTO-NAME protobuf message from a map."
  [m]
  (let [builder (JAVA-CLASS/newBuilder)]
    REGULAR-FIELDS
    ONEOF-PAYLOAD
    (.build builder)))
(defn PARSE-FN-NAME
  "Parse a PROTO-NAME protobuf message to a map."
  [^JAVA-CLASS proto]
  (cond-> {}
    REGULAR-FIELDS
    ONEOF-PAYLOAD))
(defn BUILD-FN-NAME
  "Build a PROTO-NAME protobuf message from a map."
  [m]
  (let [builder (JAVA-CLASS/newBuilder)]
    REGULAR-FIELDS
    ONEOF-PAYLOAD
    (.build builder)))
(defn PARSE-FN-NAME
  "Parse a PROTO-NAME protobuf message to a map."
  [^JAVA-CLASS proto]
  (cond-> {}
    REGULAR-FIELDS
    ONEOF-PAYLOAD))
(defn BUILD-FN-NAME
  "Build a PROTO-NAME protobuf message from a map."
  [m]
  (let [builder (JAVA-CLASS/newBuilder)]
    REGULAR-FIELDS
    ONEOF-PAYLOAD
    (.build builder)))
(defn PARSE-FN-NAME
  "Parse a PROTO-NAME protobuf message to a map."
  [^JAVA-CLASS proto]
  (cond-> {}
    REGULAR-FIELDS
    ONEOF-PAYLOAD))
(defn BUILD-FN-NAME
  "Build a PROTO-NAME protobuf message from a map."
  [m]
  (let [builder (JAVA-CLASS/newBuilder)]
    REGULAR-FIELDS
    ONEOF-PAYLOAD
    (.build builder)))
(defn PARSE-FN-NAME
  "Parse a PROTO-NAME protobuf message to a map."
  [^JAVA-CLASS proto]
  (cond-> {}
    REGULAR-FIELDS
    ONEOF-PAYLOAD))
(defn BUILD-FN-NAME
  "Build a PROTO-NAME protobuf message from a map."
  [m]
  (let [builder (JAVA-CLASS/newBuilder)]
    REGULAR-FIELDS
    ONEOF-PAYLOAD
    (.build builder)))
(defn PARSE-FN-NAME
  "Parse a PROTO-NAME protobuf message to a map."
  [^JAVA-CLASS proto]
  (cond-> {}
    REGULAR-FIELDS
    ONEOF-PAYLOAD))
(defn BUILD-FN-NAME
  "Build a PROTO-NAME protobuf message from a map."
  [m]
  (let [builder (JAVA-CLASS/newBuilder)]
    REGULAR-FIELDS
    ONEOF-PAYLOAD
    (.build builder)))
(defn PARSE-FN-NAME
  "Parse a PROTO-NAME protobuf message to a map."
  [^JAVA-CLASS proto]
  (cond-> {}
    REGULAR-FIELDS
    ONEOF-PAYLOAD))
(defn BUILD-FN-NAME
  "Build a PROTO-NAME protobuf message from a map."
  [m]
  (let [builder (JAVA-CLASS/newBuilder)]
    REGULAR-FIELDS
    ONEOF-PAYLOAD
    (.build builder)))

(defn BUILD-FN-NAME-PAYLOAD
  "Set the oneof payload field."
  [builder [field-key field-value]]
  (case field-key
    CASE-CLAUSES
    (throw (ex-info "Unknown payload field" {:field field-key}))))
(defn PARSE-FN-NAME
  "Parse a PROTO-NAME protobuf message to a map."
  [^JAVA-CLASS proto]
  (cond-> {}
    REGULAR-FIELDS
    ONEOF-PAYLOAD))

(defn PARSE-FN-NAME-PAYLOAD
  "Parse the oneof payload from a PROTO-NAME."
  [^JAVA-CLASS proto]
  (case (.GET-ONEOF-CASE proto)
    CASE-CLAUSES
    ;; Default case - no payload set
    {}))
(defn BUILD-FN-NAME
  "Build a PROTO-NAME protobuf message from a map."
  [m]
  (let [builder (JAVA-CLASS/newBuilder)]
    REGULAR-FIELDS
    ONEOF-PAYLOAD
    (.build builder)))
(defn PARSE-FN-NAME
  "Parse a PROTO-NAME protobuf message to a map."
  [^JAVA-CLASS proto]
  (cond-> {}
    REGULAR-FIELDS
    ONEOF-PAYLOAD))
(defn BUILD-FN-NAME
  "Build a PROTO-NAME protobuf message from a map."
  [m]
  (let [builder (JAVA-CLASS/newBuilder)]
    REGULAR-FIELDS
    ONEOF-PAYLOAD
    (.build builder)))

(defn BUILD-FN-NAME-PAYLOAD
  "Set the oneof payload field."
  [builder [field-key field-value]]
  (case field-key
    CASE-CLAUSES
    (throw (ex-info "Unknown payload field" {:field field-key}))))
(defn PARSE-FN-NAME
  "Parse a PROTO-NAME protobuf message to a map."
  [^JAVA-CLASS proto]
  (cond-> {}
    REGULAR-FIELDS
    ONEOF-PAYLOAD))

(defn PARSE-FN-NAME-PAYLOAD
  "Parse the oneof payload from a PROTO-NAME."
  [^JAVA-CLASS proto]
  (case (.GET-ONEOF-CASE proto)
    CASE-CLAUSES
    ;; Default case - no payload set
    {}))
(defn BUILD-FN-NAME
  "Build a PROTO-NAME protobuf message from a map."
  [m]
  (let [builder (JAVA-CLASS/newBuilder)]
    REGULAR-FIELDS
    ONEOF-PAYLOAD
    (.build builder)))

(defn BUILD-FN-NAME-PAYLOAD
  "Set the oneof payload field."
  [builder [field-key field-value]]
  (case field-key
    CASE-CLAUSES
    (throw (ex-info "Unknown payload field" {:field field-key}))))
(defn PARSE-FN-NAME
  "Parse a PROTO-NAME protobuf message to a map."
  [^JAVA-CLASS proto]
  (cond-> {}
    REGULAR-FIELDS
    ONEOF-PAYLOAD))

(defn PARSE-FN-NAME-PAYLOAD
  "Parse the oneof payload from a PROTO-NAME."
  [^JAVA-CLASS proto]
  (case (.GET-ONEOF-CASE proto)
    CASE-CLAUSES
    ;; Default case - no payload set
    {}))
(defn BUILD-FN-NAME
  "Build a PROTO-NAME protobuf message from a map."
  [m]
  (let [builder (JAVA-CLASS/newBuilder)]
    REGULAR-FIELDS
    ONEOF-PAYLOAD
    (.build builder)))

(defn BUILD-FN-NAME-PAYLOAD
  "Set the oneof payload field."
  [builder [field-key field-value]]
  (case field-key
    CASE-CLAUSES
    (throw (ex-info "Unknown payload field" {:field field-key}))))
(defn PARSE-FN-NAME
  "Parse a PROTO-NAME protobuf message to a map."
  [^JAVA-CLASS proto]
  (cond-> {}
    REGULAR-FIELDS
    ONEOF-PAYLOAD))

(defn PARSE-FN-NAME-PAYLOAD
  "Parse the oneof payload from a PROTO-NAME."
  [^JAVA-CLASS proto]
  (case (.GET-ONEOF-CASE proto)
    CASE-CLAUSES
    ;; Default case - no payload set
    {}))
(defn BUILD-FN-NAME
  "Build a PROTO-NAME protobuf message from a map."
  [m]
  (let [builder (JAVA-CLASS/newBuilder)]
    REGULAR-FIELDS
    ONEOF-PAYLOAD
    (.build builder)))

(defn BUILD-FN-NAME-PAYLOAD
  "Set the oneof payload field."
  [builder [field-key field-value]]
  (case field-key
    CASE-CLAUSES
    (throw (ex-info "Unknown payload field" {:field field-key}))))
(defn PARSE-FN-NAME
  "Parse a PROTO-NAME protobuf message to a map."
  [^JAVA-CLASS proto]
  (cond-> {}
    REGULAR-FIELDS
    ONEOF-PAYLOAD))

(defn PARSE-FN-NAME-PAYLOAD
  "Parse the oneof payload from a PROTO-NAME."
  [^JAVA-CLASS proto]
  (case (.GET-ONEOF-CASE proto)
    CASE-CLAUSES
    ;; Default case - no payload set
    {}))
(defn BUILD-FN-NAME
  "Build a PROTO-NAME protobuf message from a map."
  [m]
  (let [builder (JAVA-CLASS/newBuilder)]
    REGULAR-FIELDS
    ONEOF-PAYLOAD
    (.build builder)))

(defn BUILD-FN-NAME-PAYLOAD
  "Set the oneof payload field."
  [builder [field-key field-value]]
  (case field-key
    CASE-CLAUSES
    (throw (ex-info "Unknown payload field" {:field field-key}))))
(defn PARSE-FN-NAME
  "Parse a PROTO-NAME protobuf message to a map."
  [^JAVA-CLASS proto]
  (cond-> {}
    REGULAR-FIELDS
    ONEOF-PAYLOAD))

(defn PARSE-FN-NAME-PAYLOAD
  "Parse the oneof payload from a PROTO-NAME."
  [^JAVA-CLASS proto]
  (case (.GET-ONEOF-CASE proto)
    CASE-CLAUSES
    ;; Default case - no payload set
    {}))
(defn BUILD-FN-NAME
  "Build a PROTO-NAME protobuf message from a map."
  [m]
  (let [builder (JAVA-CLASS/newBuilder)]
    REGULAR-FIELDS
    ONEOF-PAYLOAD
    (.build builder)))

(defn BUILD-FN-NAME-PAYLOAD
  "Set the oneof payload field."
  [builder [field-key field-value]]
  (case field-key
    CASE-CLAUSES
    (throw (ex-info "Unknown payload field" {:field field-key}))))
(defn PARSE-FN-NAME
  "Parse a PROTO-NAME protobuf message to a map."
  [^JAVA-CLASS proto]
  (cond-> {}
    REGULAR-FIELDS
    ONEOF-PAYLOAD))

(defn PARSE-FN-NAME-PAYLOAD
  "Parse the oneof payload from a PROTO-NAME."
  [^JAVA-CLASS proto]
  (case (.GET-ONEOF-CASE proto)
    CASE-CLAUSES
    ;; Default case - no payload set
    {}))
(defn BUILD-FN-NAME
  "Build a PROTO-NAME protobuf message from a map."
  [m]
  (let [builder (JAVA-CLASS/newBuilder)]
    REGULAR-FIELDS
    ONEOF-PAYLOAD
    (.build builder)))

(defn BUILD-FN-NAME-PAYLOAD
  "Set the oneof payload field."
  [builder [field-key field-value]]
  (case field-key
    CASE-CLAUSES
    (throw (ex-info "Unknown payload field" {:field field-key}))))
(defn PARSE-FN-NAME
  "Parse a PROTO-NAME protobuf message to a map."
  [^JAVA-CLASS proto]
  (cond-> {}
    REGULAR-FIELDS
    ONEOF-PAYLOAD))

(defn PARSE-FN-NAME-PAYLOAD
  "Parse the oneof payload from a PROTO-NAME."
  [^JAVA-CLASS proto]
  (case (.GET-ONEOF-CASE proto)
    CASE-CLAUSES
    ;; Default case - no payload set
    {}))
(defn BUILD-FN-NAME
  "Build a PROTO-NAME protobuf message from a map."
  [m]
  (let [builder (JAVA-CLASS/newBuilder)]
    REGULAR-FIELDS
    ONEOF-PAYLOAD
    (.build builder)))

(defn BUILD-FN-NAME-PAYLOAD
  "Set the oneof payload field."
  [builder [field-key field-value]]
  (case field-key
    CASE-CLAUSES
    (throw (ex-info "Unknown payload field" {:field field-key}))))
(defn PARSE-FN-NAME
  "Parse a PROTO-NAME protobuf message to a map."
  [^JAVA-CLASS proto]
  (cond-> {}
    REGULAR-FIELDS
    ONEOF-PAYLOAD))

(defn PARSE-FN-NAME-PAYLOAD
  "Parse the oneof payload from a PROTO-NAME."
  [^JAVA-CLASS proto]
  (case (.GET-ONEOF-CASE proto)
    CASE-CLAUSES
    ;; Default case - no payload set
    {}))
(defn BUILD-FN-NAME
  "Build a PROTO-NAME protobuf message from a map."
  [m]
  (let [builder (JAVA-CLASS/newBuilder)]
    REGULAR-FIELDS
    ONEOF-PAYLOAD
    (.build builder)))

(defn BUILD-FN-NAME-PAYLOAD
  "Set the oneof payload field."
  [builder [field-key field-value]]
  (case field-key
    CASE-CLAUSES
    (throw (ex-info "Unknown payload field" {:field field-key}))))
(defn PARSE-FN-NAME
  "Parse a PROTO-NAME protobuf message to a map."
  [^JAVA-CLASS proto]
  (cond-> {}
    REGULAR-FIELDS
    ONEOF-PAYLOAD))

(defn PARSE-FN-NAME-PAYLOAD
  "Parse the oneof payload from a PROTO-NAME."
  [^JAVA-CLASS proto]
  (case (.GET-ONEOF-CASE proto)
    CASE-CLAUSES
    ;; Default case - no payload set
    {}))
(defn BUILD-FN-NAME
  "Build a PROTO-NAME protobuf message from a map."
  [m]
  (let [builder (JAVA-CLASS/newBuilder)]
    REGULAR-FIELDS
    ONEOF-PAYLOAD
    (.build builder)))

(defn BUILD-FN-NAME-PAYLOAD
  "Set the oneof payload field."
  [builder [field-key field-value]]
  (case field-key
    CASE-CLAUSES
    (throw (ex-info "Unknown payload field" {:field field-key}))))
(defn PARSE-FN-NAME
  "Parse a PROTO-NAME protobuf message to a map."
  [^JAVA-CLASS proto]
  (cond-> {}
    REGULAR-FIELDS
    ONEOF-PAYLOAD))

(defn PARSE-FN-NAME-PAYLOAD
  "Parse the oneof payload from a PROTO-NAME."
  [^JAVA-CLASS proto]
  (case (.GET-ONEOF-CASE proto)
    CASE-CLAUSES
    ;; Default case - no payload set
    {}))
(defn BUILD-FN-NAME
  "Build a PROTO-NAME protobuf message from a map."
  [m]
  (let [builder (JAVA-CLASS/newBuilder)]
    REGULAR-FIELDS
    ONEOF-PAYLOAD
    (.build builder)))

(defn BUILD-FN-NAME-PAYLOAD
  "Set the oneof payload field."
  [builder [field-key field-value]]
  (case field-key
    CASE-CLAUSES
    (throw (ex-info "Unknown payload field" {:field field-key}))))
(defn PARSE-FN-NAME
  "Parse a PROTO-NAME protobuf message to a map."
  [^JAVA-CLASS proto]
  (cond-> {}
    REGULAR-FIELDS
    ONEOF-PAYLOAD))

(defn PARSE-FN-NAME-PAYLOAD
  "Parse the oneof payload from a PROTO-NAME."
  [^JAVA-CLASS proto]
  (case (.GET-ONEOF-CASE proto)
    CASE-CLAUSES
    ;; Default case - no payload set
    {}))
(defn BUILD-FN-NAME
  "Build a PROTO-NAME protobuf message from a map."
  [m]
  (let [builder (JAVA-CLASS/newBuilder)]
    REGULAR-FIELDS
    ONEOF-PAYLOAD
    (.build builder)))

(defn BUILD-FN-NAME-PAYLOAD
  "Set the oneof payload field."
  [builder [field-key field-value]]
  (case field-key
    CASE-CLAUSES
    (throw (ex-info "Unknown payload field" {:field field-key}))))
(defn PARSE-FN-NAME
  "Parse a PROTO-NAME protobuf message to a map."
  [^JAVA-CLASS proto]
  (cond-> {}
    REGULAR-FIELDS
    ONEOF-PAYLOAD))

(defn PARSE-FN-NAME-PAYLOAD
  "Parse the oneof payload from a PROTO-NAME."
  [^JAVA-CLASS proto]
  (case (.GET-ONEOF-CASE proto)
    CASE-CLAUSES
    ;; Default case - no payload set
    {}))
(defn BUILD-FN-NAME
  "Build a PROTO-NAME protobuf message from a map."
  [m]
  (let [builder (JAVA-CLASS/newBuilder)]
    REGULAR-FIELDS
    ONEOF-PAYLOAD
    (.build builder)))
(defn PARSE-FN-NAME
  "Parse a PROTO-NAME protobuf message to a map."
  [^JAVA-CLASS proto]
  (cond-> {}
    REGULAR-FIELDS
    ONEOF-PAYLOAD))
(defn BUILD-FN-NAME
  "Build a PROTO-NAME protobuf message from a map."
  [m]
  (let [builder (JAVA-CLASS/newBuilder)]
    REGULAR-FIELDS
    ONEOF-PAYLOAD
    (.build builder)))

(defn BUILD-FN-NAME-PAYLOAD
  "Set the oneof payload field."
  [builder [field-key field-value]]
  (case field-key
    CASE-CLAUSES
    (throw (ex-info "Unknown payload field" {:field field-key}))))
(defn PARSE-FN-NAME
  "Parse a PROTO-NAME protobuf message to a map."
  [^JAVA-CLASS proto]
  (cond-> {}
    REGULAR-FIELDS
    ONEOF-PAYLOAD))

(defn PARSE-FN-NAME-PAYLOAD
  "Parse the oneof payload from a PROTO-NAME."
  [^JAVA-CLASS proto]
  (case (.GET-ONEOF-CASE proto)
    CASE-CLAUSES
    ;; Default case - no payload set
    {}))
(defn BUILD-FN-NAME
  "Build a PROTO-NAME protobuf message from a map."
  [m]
  (let [builder (JAVA-CLASS/newBuilder)]
    REGULAR-FIELDS
    ONEOF-PAYLOAD
    (.build builder)))

(defn BUILD-FN-NAME-PAYLOAD
  "Set the oneof payload field."
  [builder [field-key field-value]]
  (case field-key
    CASE-CLAUSES
    (throw (ex-info "Unknown payload field" {:field field-key}))))
(defn PARSE-FN-NAME
  "Parse a PROTO-NAME protobuf message to a map."
  [^JAVA-CLASS proto]
  (cond-> {}
    REGULAR-FIELDS
    ONEOF-PAYLOAD))

(defn PARSE-FN-NAME-PAYLOAD
  "Parse the oneof payload from a PROTO-NAME."
  [^JAVA-CLASS proto]
  (case (.GET-ONEOF-CASE proto)
    CASE-CLAUSES
    ;; Default case - no payload set
    {}))
(defn BUILD-FN-NAME
  "Build a PROTO-NAME protobuf message from a map."
  [m]
  (let [builder (JAVA-CLASS/newBuilder)]
    REGULAR-FIELDS
    ONEOF-PAYLOAD
    (.build builder)))
(defn PARSE-FN-NAME
  "Parse a PROTO-NAME protobuf message to a map."
  [^JAVA-CLASS proto]
  (cond-> {}
    REGULAR-FIELDS
    ONEOF-PAYLOAD))
(defn BUILD-FN-NAME
  "Build a PROTO-NAME protobuf message from a map."
  [m]
  (let [builder (JAVA-CLASS/newBuilder)]
    REGULAR-FIELDS
    ONEOF-PAYLOAD
    (.build builder)))
(defn PARSE-FN-NAME
  "Parse a PROTO-NAME protobuf message to a map."
  [^JAVA-CLASS proto]
  (cond-> {}
    REGULAR-FIELDS
    ONEOF-PAYLOAD))
(defn BUILD-FN-NAME
  "Build a PROTO-NAME protobuf message from a map."
  [m]
  (let [builder (JAVA-CLASS/newBuilder)]
    REGULAR-FIELDS
    ONEOF-PAYLOAD
    (.build builder)))
(defn PARSE-FN-NAME
  "Parse a PROTO-NAME protobuf message to a map."
  [^JAVA-CLASS proto]
  (cond-> {}
    REGULAR-FIELDS
    ONEOF-PAYLOAD))
(defn BUILD-FN-NAME
  "Build a PROTO-NAME protobuf message from a map."
  [m]
  (let [builder (JAVA-CLASS/newBuilder)]
    REGULAR-FIELDS
    ONEOF-PAYLOAD
    (.build builder)))
(defn PARSE-FN-NAME
  "Parse a PROTO-NAME protobuf message to a map."
  [^JAVA-CLASS proto]
  (cond-> {}
    REGULAR-FIELDS
    ONEOF-PAYLOAD))
(defn BUILD-FN-NAME
  "Build a PROTO-NAME protobuf message from a map."
  [m]
  (let [builder (JAVA-CLASS/newBuilder)]
    REGULAR-FIELDS
    ONEOF-PAYLOAD
    (.build builder)))

(defn BUILD-FN-NAME-PAYLOAD
  "Set the oneof payload field."
  [builder [field-key field-value]]
  (case field-key
    CASE-CLAUSES
    (throw (ex-info "Unknown payload field" {:field field-key}))))
(defn PARSE-FN-NAME
  "Parse a PROTO-NAME protobuf message to a map."
  [^JAVA-CLASS proto]
  (cond-> {}
    REGULAR-FIELDS
    ONEOF-PAYLOAD))

(defn PARSE-FN-NAME-PAYLOAD
  "Parse the oneof payload from a PROTO-NAME."
  [^JAVA-CLASS proto]
  (case (.GET-ONEOF-CASE proto)
    CASE-CLAUSES
    ;; Default case - no payload set
    {}))
(defn BUILD-FN-NAME
  "Build a PROTO-NAME protobuf message from a map."
  [m]
  (let [builder (JAVA-CLASS/newBuilder)]
    REGULAR-FIELDS
    ONEOF-PAYLOAD
    (.build builder)))

(defn BUILD-FN-NAME-PAYLOAD
  "Set the oneof payload field."
  [builder [field-key field-value]]
  (case field-key
    CASE-CLAUSES
    (throw (ex-info "Unknown payload field" {:field field-key}))))
(defn PARSE-FN-NAME
  "Parse a PROTO-NAME protobuf message to a map."
  [^JAVA-CLASS proto]
  (cond-> {}
    REGULAR-FIELDS
    ONEOF-PAYLOAD))

(defn PARSE-FN-NAME-PAYLOAD
  "Parse the oneof payload from a PROTO-NAME."
  [^JAVA-CLASS proto]
  (case (.GET-ONEOF-CASE proto)
    CASE-CLAUSES
    ;; Default case - no payload set
    {}))
(defn BUILD-FN-NAME
  "Build a PROTO-NAME protobuf message from a map."
  [m]
  (let [builder (JAVA-CLASS/newBuilder)]
    REGULAR-FIELDS
    ONEOF-PAYLOAD
    (.build builder)))
(defn PARSE-FN-NAME
  "Parse a PROTO-NAME protobuf message to a map."
  [^JAVA-CLASS proto]
  (cond-> {}
    REGULAR-FIELDS
    ONEOF-PAYLOAD))
(defn BUILD-FN-NAME
  "Build a PROTO-NAME protobuf message from a map."
  [m]
  (let [builder (JAVA-CLASS/newBuilder)]
    REGULAR-FIELDS
    ONEOF-PAYLOAD
    (.build builder)))
(defn PARSE-FN-NAME
  "Parse a PROTO-NAME protobuf message to a map."
  [^JAVA-CLASS proto]
  (cond-> {}
    REGULAR-FIELDS
    ONEOF-PAYLOAD))
(defn BUILD-FN-NAME
  "Build a PROTO-NAME protobuf message from a map."
  [m]
  (let [builder (JAVA-CLASS/newBuilder)]
    REGULAR-FIELDS
    ONEOF-PAYLOAD
    (.build builder)))
(defn PARSE-FN-NAME
  "Parse a PROTO-NAME protobuf message to a map."
  [^JAVA-CLASS proto]
  (cond-> {}
    REGULAR-FIELDS
    ONEOF-PAYLOAD))
(defn BUILD-FN-NAME
  "Build a PROTO-NAME protobuf message from a map."
  [m]
  (let [builder (JAVA-CLASS/newBuilder)]
    REGULAR-FIELDS
    ONEOF-PAYLOAD
    (.build builder)))

(defn BUILD-FN-NAME-PAYLOAD
  "Set the oneof payload field."
  [builder [field-key field-value]]
  (case field-key
    CASE-CLAUSES
    (throw (ex-info "Unknown payload field" {:field field-key}))))
(defn PARSE-FN-NAME
  "Parse a PROTO-NAME protobuf message to a map."
  [^JAVA-CLASS proto]
  (cond-> {}
    REGULAR-FIELDS
    ONEOF-PAYLOAD))

(defn PARSE-FN-NAME-PAYLOAD
  "Parse the oneof payload from a PROTO-NAME."
  [^JAVA-CLASS proto]
  (case (.GET-ONEOF-CASE proto)
    CASE-CLAUSES
    ;; Default case - no payload set
    {}))
(defn BUILD-FN-NAME
  "Build a PROTO-NAME protobuf message from a map."
  [m]
  (let [builder (JAVA-CLASS/newBuilder)]
    REGULAR-FIELDS
    ONEOF-PAYLOAD
    (.build builder)))

(defn BUILD-FN-NAME-PAYLOAD
  "Set the oneof payload field."
  [builder [field-key field-value]]
  (case field-key
    CASE-CLAUSES
    (throw (ex-info "Unknown payload field" {:field field-key}))))
(defn PARSE-FN-NAME
  "Parse a PROTO-NAME protobuf message to a map."
  [^JAVA-CLASS proto]
  (cond-> {}
    REGULAR-FIELDS
    ONEOF-PAYLOAD))

(defn PARSE-FN-NAME-PAYLOAD
  "Parse the oneof payload from a PROTO-NAME."
  [^JAVA-CLASS proto]
  (case (.GET-ONEOF-CASE proto)
    CASE-CLAUSES
    ;; Default case - no payload set
    {}))
(defn BUILD-FN-NAME
  "Build a PROTO-NAME protobuf message from a map."
  [m]
  (let [builder (JAVA-CLASS/newBuilder)]
    REGULAR-FIELDS
    ONEOF-PAYLOAD
    (.build builder)))
(defn PARSE-FN-NAME
  "Parse a PROTO-NAME protobuf message to a map."
  [^JAVA-CLASS proto]
  (cond-> {}
    REGULAR-FIELDS
    ONEOF-PAYLOAD))
(defn BUILD-FN-NAME
  "Build a PROTO-NAME protobuf message from a map."
  [m]
  (let [builder (JAVA-CLASS/newBuilder)]
    REGULAR-FIELDS
    ONEOF-PAYLOAD
    (.build builder)))
(defn PARSE-FN-NAME
  "Parse a PROTO-NAME protobuf message to a map."
  [^JAVA-CLASS proto]
  (cond-> {}
    REGULAR-FIELDS
    ONEOF-PAYLOAD))
(defn BUILD-FN-NAME
  "Build a PROTO-NAME protobuf message from a map."
  [m]
  (let [builder (JAVA-CLASS/newBuilder)]
    REGULAR-FIELDS
    ONEOF-PAYLOAD
    (.build builder)))
(defn PARSE-FN-NAME
  "Parse a PROTO-NAME protobuf message to a map."
  [^JAVA-CLASS proto]
  (cond-> {}
    REGULAR-FIELDS
    ONEOF-PAYLOAD))
(defn BUILD-FN-NAME
  "Build a PROTO-NAME protobuf message from a map."
  [m]
  (let [builder (JAVA-CLASS/newBuilder)]
    REGULAR-FIELDS
    ONEOF-PAYLOAD
    (.build builder)))
(defn PARSE-FN-NAME
  "Parse a PROTO-NAME protobuf message to a map."
  [^JAVA-CLASS proto]
  (cond-> {}
    REGULAR-FIELDS
    ONEOF-PAYLOAD))
(defn BUILD-FN-NAME
  "Build a PROTO-NAME protobuf message from a map."
  [m]
  (let [builder (JAVA-CLASS/newBuilder)]
    REGULAR-FIELDS
    ONEOF-PAYLOAD
    (.build builder)))
(defn PARSE-FN-NAME
  "Parse a PROTO-NAME protobuf message to a map."
  [^JAVA-CLASS proto]
  (cond-> {}
    REGULAR-FIELDS
    ONEOF-PAYLOAD))
(defn BUILD-FN-NAME
  "Build a PROTO-NAME protobuf message from a map."
  [m]
  (let [builder (JAVA-CLASS/newBuilder)]
    REGULAR-FIELDS
    ONEOF-PAYLOAD
    (.build builder)))
(defn PARSE-FN-NAME
  "Parse a PROTO-NAME protobuf message to a map."
  [^JAVA-CLASS proto]
  (cond-> {}
    REGULAR-FIELDS
    ONEOF-PAYLOAD))
(defn BUILD-FN-NAME
  "Build a PROTO-NAME protobuf message from a map."
  [m]
  (let [builder (JAVA-CLASS/newBuilder)]
    REGULAR-FIELDS
    ONEOF-PAYLOAD
    (.build builder)))
(defn PARSE-FN-NAME
  "Parse a PROTO-NAME protobuf message to a map."
  [^JAVA-CLASS proto]
  (cond-> {}
    REGULAR-FIELDS
    ONEOF-PAYLOAD))
(defn BUILD-FN-NAME
  "Build a PROTO-NAME protobuf message from a map."
  [m]
  (let [builder (JAVA-CLASS/newBuilder)]
    REGULAR-FIELDS
    ONEOF-PAYLOAD
    (.build builder)))
(defn PARSE-FN-NAME
  "Parse a PROTO-NAME protobuf message to a map."
  [^JAVA-CLASS proto]
  (cond-> {}
    REGULAR-FIELDS
    ONEOF-PAYLOAD))
(defn BUILD-FN-NAME
  "Build a PROTO-NAME protobuf message from a map."
  [m]
  (let [builder (JAVA-CLASS/newBuilder)]
    REGULAR-FIELDS
    ONEOF-PAYLOAD
    (.build builder)))
(defn PARSE-FN-NAME
  "Parse a PROTO-NAME protobuf message to a map."
  [^JAVA-CLASS proto]
  (cond-> {}
    REGULAR-FIELDS
    ONEOF-PAYLOAD))
(defn BUILD-FN-NAME
  "Build a PROTO-NAME protobuf message from a map."
  [m]
  (let [builder (JAVA-CLASS/newBuilder)]
    REGULAR-FIELDS
    ONEOF-PAYLOAD
    (.build builder)))
(defn PARSE-FN-NAME
  "Parse a PROTO-NAME protobuf message to a map."
  [^JAVA-CLASS proto]
  (cond-> {}
    REGULAR-FIELDS
    ONEOF-PAYLOAD))
(defn BUILD-FN-NAME
  "Build a PROTO-NAME protobuf message from a map."
  [m]
  (let [builder (JAVA-CLASS/newBuilder)]
    REGULAR-FIELDS
    ONEOF-PAYLOAD
    (.build builder)))
(defn PARSE-FN-NAME
  "Parse a PROTO-NAME protobuf message to a map."
  [^JAVA-CLASS proto]
  (cond-> {}
    REGULAR-FIELDS
    ONEOF-PAYLOAD))
(defn BUILD-FN-NAME
  "Build a PROTO-NAME protobuf message from a map."
  [m]
  (let [builder (JAVA-CLASS/newBuilder)]
    REGULAR-FIELDS
    ONEOF-PAYLOAD
    (.build builder)))
(defn PARSE-FN-NAME
  "Parse a PROTO-NAME protobuf message to a map."
  [^JAVA-CLASS proto]
  (cond-> {}
    REGULAR-FIELDS
    ONEOF-PAYLOAD))
(defn BUILD-FN-NAME
  "Build a PROTO-NAME protobuf message from a map."
  [m]
  (let [builder (JAVA-CLASS/newBuilder)]
    REGULAR-FIELDS
    ONEOF-PAYLOAD
    (.build builder)))

(defn BUILD-FN-NAME-PAYLOAD
  "Set the oneof payload field."
  [builder [field-key field-value]]
  (case field-key
    CASE-CLAUSES
    (throw (ex-info "Unknown payload field" {:field field-key}))))
(defn PARSE-FN-NAME
  "Parse a PROTO-NAME protobuf message to a map."
  [^JAVA-CLASS proto]
  (cond-> {}
    REGULAR-FIELDS
    ONEOF-PAYLOAD))

(defn PARSE-FN-NAME-PAYLOAD
  "Parse the oneof payload from a PROTO-NAME."
  [^JAVA-CLASS proto]
  (case (.GET-ONEOF-CASE proto)
    CASE-CLAUSES
    ;; Default case - no payload set
    {}))
(defn BUILD-FN-NAME
  "Build a PROTO-NAME protobuf message from a map."
  [m]
  (let [builder (JAVA-CLASS/newBuilder)]
    REGULAR-FIELDS
    ONEOF-PAYLOAD
    (.build builder)))
(defn PARSE-FN-NAME
  "Parse a PROTO-NAME protobuf message to a map."
  [^JAVA-CLASS proto]
  (cond-> {}
    REGULAR-FIELDS
    ONEOF-PAYLOAD))
(defn BUILD-FN-NAME
  "Build a PROTO-NAME protobuf message from a map."
  [m]
  (let [builder (JAVA-CLASS/newBuilder)]
    REGULAR-FIELDS
    ONEOF-PAYLOAD
    (.build builder)))
(defn PARSE-FN-NAME
  "Parse a PROTO-NAME protobuf message to a map."
  [^JAVA-CLASS proto]
  (cond-> {}
    REGULAR-FIELDS
    ONEOF-PAYLOAD))
(defn BUILD-FN-NAME
  "Build a PROTO-NAME protobuf message from a map."
  [m]
  (let [builder (JAVA-CLASS/newBuilder)]
    REGULAR-FIELDS
    ONEOF-PAYLOAD
    (.build builder)))
(defn PARSE-FN-NAME
  "Parse a PROTO-NAME protobuf message to a map."
  [^JAVA-CLASS proto]
  (cond-> {}
    REGULAR-FIELDS
    ONEOF-PAYLOAD))
(defn BUILD-FN-NAME
  "Build a PROTO-NAME protobuf message from a map."
  [m]
  (let [builder (JAVA-CLASS/newBuilder)]
    REGULAR-FIELDS
    ONEOF-PAYLOAD
    (.build builder)))
(defn PARSE-FN-NAME
  "Parse a PROTO-NAME protobuf message to a map."
  [^JAVA-CLASS proto]
  (cond-> {}
    REGULAR-FIELDS
    ONEOF-PAYLOAD))
(defn BUILD-FN-NAME
  "Build a PROTO-NAME protobuf message from a map."
  [m]
  (let [builder (JAVA-CLASS/newBuilder)]
    REGULAR-FIELDS
    ONEOF-PAYLOAD
    (.build builder)))
(defn PARSE-FN-NAME
  "Parse a PROTO-NAME protobuf message to a map."
  [^JAVA-CLASS proto]
  (cond-> {}
    REGULAR-FIELDS
    ONEOF-PAYLOAD))
(defn BUILD-FN-NAME
  "Build a PROTO-NAME protobuf message from a map."
  [m]
  (let [builder (JAVA-CLASS/newBuilder)]
    REGULAR-FIELDS
    ONEOF-PAYLOAD
    (.build builder)))
(defn PARSE-FN-NAME
  "Parse a PROTO-NAME protobuf message to a map."
  [^JAVA-CLASS proto]
  (cond-> {}
    REGULAR-FIELDS
    ONEOF-PAYLOAD))
(defn BUILD-FN-NAME
  "Build a PROTO-NAME protobuf message from a map."
  [m]
  (let [builder (JAVA-CLASS/newBuilder)]
    REGULAR-FIELDS
    ONEOF-PAYLOAD
    (.build builder)))

(defn BUILD-FN-NAME-PAYLOAD
  "Set the oneof payload field."
  [builder [field-key field-value]]
  (case field-key
    CASE-CLAUSES
    (throw (ex-info "Unknown payload field" {:field field-key}))))
(defn PARSE-FN-NAME
  "Parse a PROTO-NAME protobuf message to a map."
  [^JAVA-CLASS proto]
  (cond-> {}
    REGULAR-FIELDS
    ONEOF-PAYLOAD))

(defn PARSE-FN-NAME-PAYLOAD
  "Parse the oneof payload from a PROTO-NAME."
  [^JAVA-CLASS proto]
  (case (.GET-ONEOF-CASE proto)
    CASE-CLAUSES
    ;; Default case - no payload set
    {}))
(defn BUILD-FN-NAME
  "Build a PROTO-NAME protobuf message from a map."
  [m]
  (let [builder (JAVA-CLASS/newBuilder)]
    REGULAR-FIELDS
    ONEOF-PAYLOAD
    (.build builder)))
(defn PARSE-FN-NAME
  "Parse a PROTO-NAME protobuf message to a map."
  [^JAVA-CLASS proto]
  (cond-> {}
    REGULAR-FIELDS
    ONEOF-PAYLOAD))
(defn BUILD-FN-NAME
  "Build a PROTO-NAME protobuf message from a map."
  [m]
  (let [builder (JAVA-CLASS/newBuilder)]
    REGULAR-FIELDS
    ONEOF-PAYLOAD
    (.build builder)))
(defn PARSE-FN-NAME
  "Parse a PROTO-NAME protobuf message to a map."
  [^JAVA-CLASS proto]
  (cond-> {}
    REGULAR-FIELDS
    ONEOF-PAYLOAD))
(defn BUILD-FN-NAME
  "Build a PROTO-NAME protobuf message from a map."
  [m]
  (let [builder (JAVA-CLASS/newBuilder)]
    REGULAR-FIELDS
    ONEOF-PAYLOAD
    (.build builder)))
(defn PARSE-FN-NAME
  "Parse a PROTO-NAME protobuf message to a map."
  [^JAVA-CLASS proto]
  (cond-> {}
    REGULAR-FIELDS
    ONEOF-PAYLOAD))
(defn BUILD-FN-NAME
  "Build a PROTO-NAME protobuf message from a map."
  [m]
  (let [builder (JAVA-CLASS/newBuilder)]
    REGULAR-FIELDS
    ONEOF-PAYLOAD
    (.build builder)))
(defn PARSE-FN-NAME
  "Parse a PROTO-NAME protobuf message to a map."
  [^JAVA-CLASS proto]
  (cond-> {}
    REGULAR-FIELDS
    ONEOF-PAYLOAD))
(defn BUILD-FN-NAME
  "Build a PROTO-NAME protobuf message from a map."
  [m]
  (let [builder (JAVA-CLASS/newBuilder)]
    REGULAR-FIELDS
    ONEOF-PAYLOAD
    (.build builder)))
(defn PARSE-FN-NAME
  "Parse a PROTO-NAME protobuf message to a map."
  [^JAVA-CLASS proto]
  (cond-> {}
    REGULAR-FIELDS
    ONEOF-PAYLOAD))
(defn BUILD-FN-NAME
  "Build a PROTO-NAME protobuf message from a map."
  [m]
  (let [builder (JAVA-CLASS/newBuilder)]
    REGULAR-FIELDS
    ONEOF-PAYLOAD
    (.build builder)))
(defn PARSE-FN-NAME
  "Parse a PROTO-NAME protobuf message to a map."
  [^JAVA-CLASS proto]
  (cond-> {}
    REGULAR-FIELDS
    ONEOF-PAYLOAD))
(defn BUILD-FN-NAME
  "Build a PROTO-NAME protobuf message from a map."
  [m]
  (let [builder (JAVA-CLASS/newBuilder)]
    REGULAR-FIELDS
    ONEOF-PAYLOAD
    (.build builder)))
(defn PARSE-FN-NAME
  "Parse a PROTO-NAME protobuf message to a map."
  [^JAVA-CLASS proto]
  (cond-> {}
    REGULAR-FIELDS
    ONEOF-PAYLOAD))
(defn BUILD-FN-NAME
  "Build a PROTO-NAME protobuf message from a map."
  [m]
  (let [builder (JAVA-CLASS/newBuilder)]
    REGULAR-FIELDS
    ONEOF-PAYLOAD
    (.build builder)))
(defn PARSE-FN-NAME
  "Parse a PROTO-NAME protobuf message to a map."
  [^JAVA-CLASS proto]
  (cond-> {}
    REGULAR-FIELDS
    ONEOF-PAYLOAD))
(defn BUILD-FN-NAME
  "Build a PROTO-NAME protobuf message from a map."
  [m]
  (let [builder (JAVA-CLASS/newBuilder)]
    REGULAR-FIELDS
    ONEOF-PAYLOAD
    (.build builder)))
(defn PARSE-FN-NAME
  "Parse a PROTO-NAME protobuf message to a map."
  [^JAVA-CLASS proto]
  (cond-> {}
    REGULAR-FIELDS
    ONEOF-PAYLOAD))
(defn BUILD-FN-NAME
  "Build a PROTO-NAME protobuf message from a map."
  [m]
  (let [builder (JAVA-CLASS/newBuilder)]
    REGULAR-FIELDS
    ONEOF-PAYLOAD
    (.build builder)))
(defn PARSE-FN-NAME
  "Parse a PROTO-NAME protobuf message to a map."
  [^JAVA-CLASS proto]
  (cond-> {}
    REGULAR-FIELDS
    ONEOF-PAYLOAD))
(defn BUILD-FN-NAME
  "Build a PROTO-NAME protobuf message from a map."
  [m]
  (let [builder (JAVA-CLASS/newBuilder)]
    REGULAR-FIELDS
    ONEOF-PAYLOAD
    (.build builder)))
(defn PARSE-FN-NAME
  "Parse a PROTO-NAME protobuf message to a map."
  [^JAVA-CLASS proto]
  (cond-> {}
    REGULAR-FIELDS
    ONEOF-PAYLOAD))
(defn BUILD-FN-NAME
  "Build a PROTO-NAME protobuf message from a map."
  [m]
  (let [builder (JAVA-CLASS/newBuilder)]
    REGULAR-FIELDS
    ONEOF-PAYLOAD
    (.build builder)))
(defn PARSE-FN-NAME
  "Parse a PROTO-NAME protobuf message to a map."
  [^JAVA-CLASS proto]
  (cond-> {}
    REGULAR-FIELDS
    ONEOF-PAYLOAD))
(defn BUILD-FN-NAME
  "Build a PROTO-NAME protobuf message from a map."
  [m]
  (let [builder (JAVA-CLASS/newBuilder)]
    REGULAR-FIELDS
    ONEOF-PAYLOAD
    (.build builder)))
(defn PARSE-FN-NAME
  "Parse a PROTO-NAME protobuf message to a map."
  [^JAVA-CLASS proto]
  (cond-> {}
    REGULAR-FIELDS
    ONEOF-PAYLOAD))
(defn BUILD-FN-NAME
  "Build a PROTO-NAME protobuf message from a map."
  [m]
  (let [builder (JAVA-CLASS/newBuilder)]
    REGULAR-FIELDS
    ONEOF-PAYLOAD
    (.build builder)))
(defn PARSE-FN-NAME
  "Parse a PROTO-NAME protobuf message to a map."
  [^JAVA-CLASS proto]
  (cond-> {}
    REGULAR-FIELDS
    ONEOF-PAYLOAD))
(defn BUILD-FN-NAME
  "Build a PROTO-NAME protobuf message from a map."
  [m]
  (let [builder (JAVA-CLASS/newBuilder)]
    REGULAR-FIELDS
    ONEOF-PAYLOAD
    (.build builder)))
(defn PARSE-FN-NAME
  "Parse a PROTO-NAME protobuf message to a map."
  [^JAVA-CLASS proto]
  (cond-> {}
    REGULAR-FIELDS
    ONEOF-PAYLOAD))
(defn BUILD-FN-NAME
  "Build a PROTO-NAME protobuf message from a map."
  [m]
  (let [builder (JAVA-CLASS/newBuilder)]
    REGULAR-FIELDS
    ONEOF-PAYLOAD
    (.build builder)))
(defn PARSE-FN-NAME
  "Parse a PROTO-NAME protobuf message to a map."
  [^JAVA-CLASS proto]
  (cond-> {}
    REGULAR-FIELDS
    ONEOF-PAYLOAD))
(defn BUILD-FN-NAME
  "Build a PROTO-NAME protobuf message from a map."
  [m]
  (let [builder (JAVA-CLASS/newBuilder)]
    REGULAR-FIELDS
    ONEOF-PAYLOAD
    (.build builder)))
(defn PARSE-FN-NAME
  "Parse a PROTO-NAME protobuf message to a map."
  [^JAVA-CLASS proto]
  (cond-> {}
    REGULAR-FIELDS
    ONEOF-PAYLOAD))
(defn BUILD-FN-NAME
  "Build a PROTO-NAME protobuf message from a map."
  [m]
  (let [builder (JAVA-CLASS/newBuilder)]
    REGULAR-FIELDS
    ONEOF-PAYLOAD
    (.build builder)))
(defn PARSE-FN-NAME
  "Parse a PROTO-NAME protobuf message to a map."
  [^JAVA-CLASS proto]
  (cond-> {}
    REGULAR-FIELDS
    ONEOF-PAYLOAD))
(defn BUILD-FN-NAME
  "Build a PROTO-NAME protobuf message from a map."
  [m]
  (let [builder (JAVA-CLASS/newBuilder)]
    REGULAR-FIELDS
    ONEOF-PAYLOAD
    (.build builder)))
(defn PARSE-FN-NAME
  "Parse a PROTO-NAME protobuf message to a map."
  [^JAVA-CLASS proto]
  (cond-> {}
    REGULAR-FIELDS
    ONEOF-PAYLOAD))
(defn BUILD-FN-NAME
  "Build a PROTO-NAME protobuf message from a map."
  [m]
  (let [builder (JAVA-CLASS/newBuilder)]
    REGULAR-FIELDS
    ONEOF-PAYLOAD
    (.build builder)))
(defn PARSE-FN-NAME
  "Parse a PROTO-NAME protobuf message to a map."
  [^JAVA-CLASS proto]
  (cond-> {}
    REGULAR-FIELDS
    ONEOF-PAYLOAD))
(defn BUILD-FN-NAME
  "Build a PROTO-NAME protobuf message from a map."
  [m]
  (let [builder (JAVA-CLASS/newBuilder)]
    REGULAR-FIELDS
    ONEOF-PAYLOAD
    (.build builder)))

(defn BUILD-FN-NAME-PAYLOAD
  "Set the oneof payload field."
  [builder [field-key field-value]]
  (case field-key
    CASE-CLAUSES
    (throw (ex-info "Unknown payload field" {:field field-key}))))
(defn PARSE-FN-NAME
  "Parse a PROTO-NAME protobuf message to a map."
  [^JAVA-CLASS proto]
  (cond-> {}
    REGULAR-FIELDS
    ONEOF-PAYLOAD))

(defn PARSE-FN-NAME-PAYLOAD
  "Parse the oneof payload from a PROTO-NAME."
  [^JAVA-CLASS proto]
  (case (.GET-ONEOF-CASE proto)
    CASE-CLAUSES
    ;; Default case - no payload set
    {}))
(defn BUILD-FN-NAME
  "Build a PROTO-NAME protobuf message from a map."
  [m]
  (let [builder (JAVA-CLASS/newBuilder)]
    REGULAR-FIELDS
    ONEOF-PAYLOAD
    (.build builder)))
(defn PARSE-FN-NAME
  "Parse a PROTO-NAME protobuf message to a map."
  [^JAVA-CLASS proto]
  (cond-> {}
    REGULAR-FIELDS
    ONEOF-PAYLOAD))
(defn BUILD-FN-NAME
  "Build a PROTO-NAME protobuf message from a map."
  [m]
  (let [builder (JAVA-CLASS/newBuilder)]
    REGULAR-FIELDS
    ONEOF-PAYLOAD
    (.build builder)))
(defn PARSE-FN-NAME
  "Parse a PROTO-NAME protobuf message to a map."
  [^JAVA-CLASS proto]
  (cond-> {}
    REGULAR-FIELDS
    ONEOF-PAYLOAD))
(defn BUILD-FN-NAME
  "Build a PROTO-NAME protobuf message from a map."
  [m]
  (let [builder (JAVA-CLASS/newBuilder)]
    REGULAR-FIELDS
    ONEOF-PAYLOAD
    (.build builder)))
(defn PARSE-FN-NAME
  "Parse a PROTO-NAME protobuf message to a map."
  [^JAVA-CLASS proto]
  (cond-> {}
    REGULAR-FIELDS
    ONEOF-PAYLOAD))
(defn BUILD-FN-NAME
  "Build a PROTO-NAME protobuf message from a map."
  [m]
  (let [builder (JAVA-CLASS/newBuilder)]
    REGULAR-FIELDS
    ONEOF-PAYLOAD
    (.build builder)))
(defn PARSE-FN-NAME
  "Parse a PROTO-NAME protobuf message to a map."
  [^JAVA-CLASS proto]
  (cond-> {}
    REGULAR-FIELDS
    ONEOF-PAYLOAD))
(defn BUILD-FN-NAME
  "Build a PROTO-NAME protobuf message from a map."
  [m]
  (let [builder (JAVA-CLASS/newBuilder)]
    REGULAR-FIELDS
    ONEOF-PAYLOAD
    (.build builder)))
(defn PARSE-FN-NAME
  "Parse a PROTO-NAME protobuf message to a map."
  [^JAVA-CLASS proto]
  (cond-> {}
    REGULAR-FIELDS
    ONEOF-PAYLOAD))
(defn BUILD-FN-NAME
  "Build a PROTO-NAME protobuf message from a map."
  [m]
  (let [builder (JAVA-CLASS/newBuilder)]
    REGULAR-FIELDS
    ONEOF-PAYLOAD
    (.build builder)))
(defn PARSE-FN-NAME
  "Parse a PROTO-NAME protobuf message to a map."
  [^JAVA-CLASS proto]
  (cond-> {}
    REGULAR-FIELDS
    ONEOF-PAYLOAD))
(defn BUILD-FN-NAME
  "Build a PROTO-NAME protobuf message from a map."
  [m]
  (let [builder (JAVA-CLASS/newBuilder)]
    REGULAR-FIELDS
    ONEOF-PAYLOAD
    (.build builder)))
(defn PARSE-FN-NAME
  "Parse a PROTO-NAME protobuf message to a map."
  [^JAVA-CLASS proto]
  (cond-> {}
    REGULAR-FIELDS
    ONEOF-PAYLOAD))
(defn BUILD-FN-NAME
  "Build a PROTO-NAME protobuf message from a map."
  [m]
  (let [builder (JAVA-CLASS/newBuilder)]
    REGULAR-FIELDS
    ONEOF-PAYLOAD
    (.build builder)))

(defn BUILD-FN-NAME-PAYLOAD
  "Set the oneof payload field."
  [builder [field-key field-value]]
  (case field-key
    CASE-CLAUSES
    (throw (ex-info "Unknown payload field" {:field field-key}))))
(defn PARSE-FN-NAME
  "Parse a PROTO-NAME protobuf message to a map."
  [^JAVA-CLASS proto]
  (cond-> {}
    REGULAR-FIELDS
    ONEOF-PAYLOAD))

(defn PARSE-FN-NAME-PAYLOAD
  "Parse the oneof payload from a PROTO-NAME."
  [^JAVA-CLASS proto]
  (case (.GET-ONEOF-CASE proto)
    CASE-CLAUSES
    ;; Default case - no payload set
    {}))
(defn BUILD-FN-NAME
  "Build a PROTO-NAME protobuf message from a map."
  [m]
  (let [builder (JAVA-CLASS/newBuilder)]
    REGULAR-FIELDS
    ONEOF-PAYLOAD
    (.build builder)))

(defn BUILD-FN-NAME-PAYLOAD
  "Set the oneof payload field."
  [builder [field-key field-value]]
  (case field-key
    CASE-CLAUSES
    (throw (ex-info "Unknown payload field" {:field field-key}))))
(defn PARSE-FN-NAME
  "Parse a PROTO-NAME protobuf message to a map."
  [^JAVA-CLASS proto]
  (cond-> {}
    REGULAR-FIELDS
    ONEOF-PAYLOAD))

(defn PARSE-FN-NAME-PAYLOAD
  "Parse the oneof payload from a PROTO-NAME."
  [^JAVA-CLASS proto]
  (case (.GET-ONEOF-CASE proto)
    CASE-CLAUSES
    ;; Default case - no payload set
    {}))
(defn BUILD-FN-NAME
  "Build a PROTO-NAME protobuf message from a map."
  [m]
  (let [builder (JAVA-CLASS/newBuilder)]
    REGULAR-FIELDS
    ONEOF-PAYLOAD
    (.build builder)))
(defn PARSE-FN-NAME
  "Parse a PROTO-NAME protobuf message to a map."
  [^JAVA-CLASS proto]
  (cond-> {}
    REGULAR-FIELDS
    ONEOF-PAYLOAD))
(defn BUILD-FN-NAME
  "Build a PROTO-NAME protobuf message from a map."
  [m]
  (let [builder (JAVA-CLASS/newBuilder)]
    REGULAR-FIELDS
    ONEOF-PAYLOAD
    (.build builder)))
(defn PARSE-FN-NAME
  "Parse a PROTO-NAME protobuf message to a map."
  [^JAVA-CLASS proto]
  (cond-> {}
    REGULAR-FIELDS
    ONEOF-PAYLOAD))
(defn BUILD-FN-NAME
  "Build a PROTO-NAME protobuf message from a map."
  [m]
  (let [builder (JAVA-CLASS/newBuilder)]
    REGULAR-FIELDS
    ONEOF-PAYLOAD
    (.build builder)))
(defn PARSE-FN-NAME
  "Parse a PROTO-NAME protobuf message to a map."
  [^JAVA-CLASS proto]
  (cond-> {}
    REGULAR-FIELDS
    ONEOF-PAYLOAD))
(defn BUILD-FN-NAME
  "Build a PROTO-NAME protobuf message from a map."
  [m]
  (let [builder (JAVA-CLASS/newBuilder)]
    REGULAR-FIELDS
    ONEOF-PAYLOAD
    (.build builder)))
(defn PARSE-FN-NAME
  "Parse a PROTO-NAME protobuf message to a map."
  [^JAVA-CLASS proto]
  (cond-> {}
    REGULAR-FIELDS
    ONEOF-PAYLOAD))
(defn BUILD-FN-NAME
  "Build a PROTO-NAME protobuf message from a map."
  [m]
  (let [builder (JAVA-CLASS/newBuilder)]
    REGULAR-FIELDS
    ONEOF-PAYLOAD
    (.build builder)))
(defn PARSE-FN-NAME
  "Parse a PROTO-NAME protobuf message to a map."
  [^JAVA-CLASS proto]
  (cond-> {}
    REGULAR-FIELDS
    ONEOF-PAYLOAD))
(defn BUILD-FN-NAME
  "Build a PROTO-NAME protobuf message from a map."
  [m]
  (let [builder (JAVA-CLASS/newBuilder)]
    REGULAR-FIELDS
    ONEOF-PAYLOAD
    (.build builder)))
(defn PARSE-FN-NAME
  "Parse a PROTO-NAME protobuf message to a map."
  [^JAVA-CLASS proto]
  (cond-> {}
    REGULAR-FIELDS
    ONEOF-PAYLOAD))
(defn BUILD-FN-NAME
  "Build a PROTO-NAME protobuf message from a map."
  [m]
  (let [builder (JAVA-CLASS/newBuilder)]
    REGULAR-FIELDS
    ONEOF-PAYLOAD
    (.build builder)))
(defn PARSE-FN-NAME
  "Parse a PROTO-NAME protobuf message to a map."
  [^JAVA-CLASS proto]
  (cond-> {}
    REGULAR-FIELDS
    ONEOF-PAYLOAD))
(defn BUILD-FN-NAME
  "Build a PROTO-NAME protobuf message from a map."
  [m]
  (let [builder (JAVA-CLASS/newBuilder)]
    REGULAR-FIELDS
    ONEOF-PAYLOAD
    (.build builder)))
(defn PARSE-FN-NAME
  "Parse a PROTO-NAME protobuf message to a map."
  [^JAVA-CLASS proto]
  (cond-> {}
    REGULAR-FIELDS
    ONEOF-PAYLOAD))
(defn BUILD-FN-NAME
  "Build a PROTO-NAME protobuf message from a map."
  [m]
  (let [builder (JAVA-CLASS/newBuilder)]
    REGULAR-FIELDS
    ONEOF-PAYLOAD
    (.build builder)))
(defn PARSE-FN-NAME
  "Parse a PROTO-NAME protobuf message to a map."
  [^JAVA-CLASS proto]
  (cond-> {}
    REGULAR-FIELDS
    ONEOF-PAYLOAD))
(defn BUILD-FN-NAME
  "Build a PROTO-NAME protobuf message from a map."
  [m]
  (let [builder (JAVA-CLASS/newBuilder)]
    REGULAR-FIELDS
    ONEOF-PAYLOAD
    (.build builder)))
(defn PARSE-FN-NAME
  "Parse a PROTO-NAME protobuf message to a map."
  [^JAVA-CLASS proto]
  (cond-> {}
    REGULAR-FIELDS
    ONEOF-PAYLOAD))
(defn BUILD-FN-NAME
  "Build a PROTO-NAME protobuf message from a map."
  [m]
  (let [builder (JAVA-CLASS/newBuilder)]
    REGULAR-FIELDS
    ONEOF-PAYLOAD
    (.build builder)))
(defn PARSE-FN-NAME
  "Parse a PROTO-NAME protobuf message to a map."
  [^JAVA-CLASS proto]
  (cond-> {}
    REGULAR-FIELDS
    ONEOF-PAYLOAD))
(defn BUILD-FN-NAME
  "Build a PROTO-NAME protobuf message from a map."
  [m]
  (let [builder (JAVA-CLASS/newBuilder)]
    REGULAR-FIELDS
    ONEOF-PAYLOAD
    (.build builder)))
(defn PARSE-FN-NAME
  "Parse a PROTO-NAME protobuf message to a map."
  [^JAVA-CLASS proto]
  (cond-> {}
    REGULAR-FIELDS
    ONEOF-PAYLOAD))
(defn BUILD-FN-NAME
  "Build a PROTO-NAME protobuf message from a map."
  [m]
  (let [builder (JAVA-CLASS/newBuilder)]
    REGULAR-FIELDS
    ONEOF-PAYLOAD
    (.build builder)))
(defn PARSE-FN-NAME
  "Parse a PROTO-NAME protobuf message to a map."
  [^JAVA-CLASS proto]
  (cond-> {}
    REGULAR-FIELDS
    ONEOF-PAYLOAD))
(defn BUILD-FN-NAME
  "Build a PROTO-NAME protobuf message from a map."
  [m]
  (let [builder (JAVA-CLASS/newBuilder)]
    REGULAR-FIELDS
    ONEOF-PAYLOAD
    (.build builder)))
(defn PARSE-FN-NAME
  "Parse a PROTO-NAME protobuf message to a map."
  [^JAVA-CLASS proto]
  (cond-> {}
    REGULAR-FIELDS
    ONEOF-PAYLOAD))
(defn BUILD-FN-NAME
  "Build a PROTO-NAME protobuf message from a map."
  [m]
  (let [builder (JAVA-CLASS/newBuilder)]
    REGULAR-FIELDS
    ONEOF-PAYLOAD
    (.build builder)))
(defn PARSE-FN-NAME
  "Parse a PROTO-NAME protobuf message to a map."
  [^JAVA-CLASS proto]
  (cond-> {}
    REGULAR-FIELDS
    ONEOF-PAYLOAD))
(defn BUILD-FN-NAME
  "Build a PROTO-NAME protobuf message from a map."
  [m]
  (let [builder (JAVA-CLASS/newBuilder)]
    REGULAR-FIELDS
    ONEOF-PAYLOAD
    (.build builder)))

(defn BUILD-FN-NAME-PAYLOAD
  "Set the oneof payload field."
  [builder [field-key field-value]]
  (case field-key
    CASE-CLAUSES
    (throw (ex-info "Unknown payload field" {:field field-key}))))
(defn PARSE-FN-NAME
  "Parse a PROTO-NAME protobuf message to a map."
  [^JAVA-CLASS proto]
  (cond-> {}
    REGULAR-FIELDS
    ONEOF-PAYLOAD))

(defn PARSE-FN-NAME-PAYLOAD
  "Parse the oneof payload from a PROTO-NAME."
  [^JAVA-CLASS proto]
  (case (.GET-ONEOF-CASE proto)
    CASE-CLAUSES
    ;; Default case - no payload set
    {}))
(defn BUILD-FN-NAME
  "Build a PROTO-NAME protobuf message from a map."
  [m]
  (let [builder (JAVA-CLASS/newBuilder)]
    REGULAR-FIELDS
    ONEOF-PAYLOAD
    (.build builder)))
(defn PARSE-FN-NAME
  "Parse a PROTO-NAME protobuf message to a map."
  [^JAVA-CLASS proto]
  (cond-> {}
    REGULAR-FIELDS
    ONEOF-PAYLOAD))
(defn BUILD-FN-NAME
  "Build a PROTO-NAME protobuf message from a map."
  [m]
  (let [builder (JAVA-CLASS/newBuilder)]
    REGULAR-FIELDS
    ONEOF-PAYLOAD
    (.build builder)))
(defn PARSE-FN-NAME
  "Parse a PROTO-NAME protobuf message to a map."
  [^JAVA-CLASS proto]
  (cond-> {}
    REGULAR-FIELDS
    ONEOF-PAYLOAD))
(defn BUILD-FN-NAME
  "Build a PROTO-NAME protobuf message from a map."
  [m]
  (let [builder (JAVA-CLASS/newBuilder)]
    REGULAR-FIELDS
    ONEOF-PAYLOAD
    (.build builder)))
(defn PARSE-FN-NAME
  "Parse a PROTO-NAME protobuf message to a map."
  [^JAVA-CLASS proto]
  (cond-> {}
    REGULAR-FIELDS
    ONEOF-PAYLOAD))
(defn BUILD-FN-NAME
  "Build a PROTO-NAME protobuf message from a map."
  [m]
  (let [builder (JAVA-CLASS/newBuilder)]
    REGULAR-FIELDS
    ONEOF-PAYLOAD
    (.build builder)))
(defn PARSE-FN-NAME
  "Parse a PROTO-NAME protobuf message to a map."
  [^JAVA-CLASS proto]
  (cond-> {}
    REGULAR-FIELDS
    ONEOF-PAYLOAD))
(defn BUILD-FN-NAME
  "Build a PROTO-NAME protobuf message from a map."
  [m]
  (let [builder (JAVA-CLASS/newBuilder)]
    REGULAR-FIELDS
    ONEOF-PAYLOAD
    (.build builder)))
(defn PARSE-FN-NAME
  "Parse a PROTO-NAME protobuf message to a map."
  [^JAVA-CLASS proto]
  (cond-> {}
    REGULAR-FIELDS
    ONEOF-PAYLOAD))
(defn BUILD-FN-NAME
  "Build a PROTO-NAME protobuf message from a map."
  [m]
  (let [builder (JAVA-CLASS/newBuilder)]
    REGULAR-FIELDS
    ONEOF-PAYLOAD
    (.build builder)))
(defn PARSE-FN-NAME
  "Parse a PROTO-NAME protobuf message to a map."
  [^JAVA-CLASS proto]
  (cond-> {}
    REGULAR-FIELDS
    ONEOF-PAYLOAD))
(defn BUILD-FN-NAME
  "Build a PROTO-NAME protobuf message from a map."
  [m]
  (let [builder (JAVA-CLASS/newBuilder)]
    REGULAR-FIELDS
    ONEOF-PAYLOAD
    (.build builder)))
(defn PARSE-FN-NAME
  "Parse a PROTO-NAME protobuf message to a map."
  [^JAVA-CLASS proto]
  (cond-> {}
    REGULAR-FIELDS
    ONEOF-PAYLOAD))
(defn BUILD-FN-NAME
  "Build a PROTO-NAME protobuf message from a map."
  [m]
  (let [builder (JAVA-CLASS/newBuilder)]
    REGULAR-FIELDS
    ONEOF-PAYLOAD
    (.build builder)))
(defn PARSE-FN-NAME
  "Parse a PROTO-NAME protobuf message to a map."
  [^JAVA-CLASS proto]
  (cond-> {}
    REGULAR-FIELDS
    ONEOF-PAYLOAD))
(defn BUILD-FN-NAME
  "Build a PROTO-NAME protobuf message from a map."
  [m]
  (let [builder (JAVA-CLASS/newBuilder)]
    REGULAR-FIELDS
    ONEOF-PAYLOAD
    (.build builder)))
(defn PARSE-FN-NAME
  "Parse a PROTO-NAME protobuf message to a map."
  [^JAVA-CLASS proto]
  (cond-> {}
    REGULAR-FIELDS
    ONEOF-PAYLOAD))
(defn BUILD-FN-NAME
  "Build a PROTO-NAME protobuf message from a map."
  [m]
  (let [builder (JAVA-CLASS/newBuilder)]
    REGULAR-FIELDS
    ONEOF-PAYLOAD
    (.build builder)))
(defn PARSE-FN-NAME
  "Parse a PROTO-NAME protobuf message to a map."
  [^JAVA-CLASS proto]
  (cond-> {}
    REGULAR-FIELDS
    ONEOF-PAYLOAD))
(defn BUILD-FN-NAME
  "Build a PROTO-NAME protobuf message from a map."
  [m]
  (let [builder (JAVA-CLASS/newBuilder)]
    REGULAR-FIELDS
    ONEOF-PAYLOAD
    (.build builder)))
(defn PARSE-FN-NAME
  "Parse a PROTO-NAME protobuf message to a map."
  [^JAVA-CLASS proto]
  (cond-> {}
    REGULAR-FIELDS
    ONEOF-PAYLOAD))
(defn BUILD-FN-NAME
  "Build a PROTO-NAME protobuf message from a map."
  [m]
  (let [builder (JAVA-CLASS/newBuilder)]
    REGULAR-FIELDS
    ONEOF-PAYLOAD
    (.build builder)))
(defn PARSE-FN-NAME
  "Parse a PROTO-NAME protobuf message to a map."
  [^JAVA-CLASS proto]
  (cond-> {}
    REGULAR-FIELDS
    ONEOF-PAYLOAD))
(defn BUILD-FN-NAME
  "Build a PROTO-NAME protobuf message from a map."
  [m]
  (let [builder (JAVA-CLASS/newBuilder)]
    REGULAR-FIELDS
    ONEOF-PAYLOAD
    (.build builder)))
(defn PARSE-FN-NAME
  "Parse a PROTO-NAME protobuf message to a map."
  [^JAVA-CLASS proto]
  (cond-> {}
    REGULAR-FIELDS
    ONEOF-PAYLOAD))
(defn BUILD-FN-NAME
  "Build a PROTO-NAME protobuf message from a map."
  [m]
  (let [builder (JAVA-CLASS/newBuilder)]
    REGULAR-FIELDS
    ONEOF-PAYLOAD
    (.build builder)))
(defn PARSE-FN-NAME
  "Parse a PROTO-NAME protobuf message to a map."
  [^JAVA-CLASS proto]
  (cond-> {}
    REGULAR-FIELDS
    ONEOF-PAYLOAD))
(defn BUILD-FN-NAME
  "Build a PROTO-NAME protobuf message from a map."
  [m]
  (let [builder (JAVA-CLASS/newBuilder)]
    REGULAR-FIELDS
    ONEOF-PAYLOAD
    (.build builder)))
(defn PARSE-FN-NAME
  "Parse a PROTO-NAME protobuf message to a map."
  [^JAVA-CLASS proto]
  (cond-> {}
    REGULAR-FIELDS
    ONEOF-PAYLOAD))
(defn BUILD-FN-NAME
  "Build a PROTO-NAME protobuf message from a map."
  [m]
  (let [builder (JAVA-CLASS/newBuilder)]
    REGULAR-FIELDS
    ONEOF-PAYLOAD
    (.build builder)))
(defn PARSE-FN-NAME
  "Parse a PROTO-NAME protobuf message to a map."
  [^JAVA-CLASS proto]
  (cond-> {}
    REGULAR-FIELDS
    ONEOF-PAYLOAD))
(defn BUILD-FN-NAME
  "Build a PROTO-NAME protobuf message from a map."
  [m]
  (let [builder (JAVA-CLASS/newBuilder)]
    REGULAR-FIELDS
    ONEOF-PAYLOAD
    (.build builder)))
(defn PARSE-FN-NAME
  "Parse a PROTO-NAME protobuf message to a map."
  [^JAVA-CLASS proto]
  (cond-> {}
    REGULAR-FIELDS
    ONEOF-PAYLOAD))
(defn BUILD-FN-NAME
  "Build a PROTO-NAME protobuf message from a map."
  [m]
  (let [builder (JAVA-CLASS/newBuilder)]
    REGULAR-FIELDS
    ONEOF-PAYLOAD
    (.build builder)))
(defn PARSE-FN-NAME
  "Parse a PROTO-NAME protobuf message to a map."
  [^JAVA-CLASS proto]
  (cond-> {}
    REGULAR-FIELDS
    ONEOF-PAYLOAD))
(defn BUILD-FN-NAME
  "Build a PROTO-NAME protobuf message from a map."
  [m]
  (let [builder (JAVA-CLASS/newBuilder)]
    REGULAR-FIELDS
    ONEOF-PAYLOAD
    (.build builder)))
(defn PARSE-FN-NAME
  "Parse a PROTO-NAME protobuf message to a map."
  [^JAVA-CLASS proto]
  (cond-> {}
    REGULAR-FIELDS
    ONEOF-PAYLOAD))
(defn BUILD-FN-NAME
  "Build a PROTO-NAME protobuf message from a map."
  [m]
  (let [builder (JAVA-CLASS/newBuilder)]
    REGULAR-FIELDS
    ONEOF-PAYLOAD
    (.build builder)))
(defn PARSE-FN-NAME
  "Parse a PROTO-NAME protobuf message to a map."
  [^JAVA-CLASS proto]
  (cond-> {}
    REGULAR-FIELDS
    ONEOF-PAYLOAD))
(defn BUILD-FN-NAME
  "Build a PROTO-NAME protobuf message from a map."
  [m]
  (let [builder (JAVA-CLASS/newBuilder)]
    REGULAR-FIELDS
    ONEOF-PAYLOAD
    (.build builder)))
(defn PARSE-FN-NAME
  "Parse a PROTO-NAME protobuf message to a map."
  [^JAVA-CLASS proto]
  (cond-> {}
    REGULAR-FIELDS
    ONEOF-PAYLOAD))
(defn BUILD-FN-NAME
  "Build a PROTO-NAME protobuf message from a map."
  [m]
  (let [builder (JAVA-CLASS/newBuilder)]
    REGULAR-FIELDS
    ONEOF-PAYLOAD
    (.build builder)))

(defn BUILD-FN-NAME-PAYLOAD
  "Set the oneof payload field."
  [builder [field-key field-value]]
  (case field-key
    CASE-CLAUSES
    (throw (ex-info "Unknown payload field" {:field field-key}))))
(defn PARSE-FN-NAME
  "Parse a PROTO-NAME protobuf message to a map."
  [^JAVA-CLASS proto]
  (cond-> {}
    REGULAR-FIELDS
    ONEOF-PAYLOAD))

(defn PARSE-FN-NAME-PAYLOAD
  "Parse the oneof payload from a PROTO-NAME."
  [^JAVA-CLASS proto]
  (case (.GET-ONEOF-CASE proto)
    CASE-CLAUSES
    ;; Default case - no payload set
    {}))
(defn BUILD-FN-NAME
  "Build a PROTO-NAME protobuf message from a map."
  [m]
  (let [builder (JAVA-CLASS/newBuilder)]
    REGULAR-FIELDS
    ONEOF-PAYLOAD
    (.build builder)))
(defn PARSE-FN-NAME
  "Parse a PROTO-NAME protobuf message to a map."
  [^JAVA-CLASS proto]
  (cond-> {}
    REGULAR-FIELDS
    ONEOF-PAYLOAD))
(defn BUILD-FN-NAME
  "Build a PROTO-NAME protobuf message from a map."
  [m]
  (let [builder (JAVA-CLASS/newBuilder)]
    REGULAR-FIELDS
    ONEOF-PAYLOAD
    (.build builder)))
(defn PARSE-FN-NAME
  "Parse a PROTO-NAME protobuf message to a map."
  [^JAVA-CLASS proto]
  (cond-> {}
    REGULAR-FIELDS
    ONEOF-PAYLOAD))
(defn BUILD-FN-NAME
  "Build a PROTO-NAME protobuf message from a map."
  [m]
  (let [builder (JAVA-CLASS/newBuilder)]
    REGULAR-FIELDS
    ONEOF-PAYLOAD
    (.build builder)))
(defn PARSE-FN-NAME
  "Parse a PROTO-NAME protobuf message to a map."
  [^JAVA-CLASS proto]
  (cond-> {}
    REGULAR-FIELDS
    ONEOF-PAYLOAD))
(defn BUILD-FN-NAME
  "Build a PROTO-NAME protobuf message from a map."
  [m]
  (let [builder (JAVA-CLASS/newBuilder)]
    REGULAR-FIELDS
    ONEOF-PAYLOAD
    (.build builder)))
(defn PARSE-FN-NAME
  "Parse a PROTO-NAME protobuf message to a map."
  [^JAVA-CLASS proto]
  (cond-> {}
    REGULAR-FIELDS
    ONEOF-PAYLOAD))
(defn BUILD-FN-NAME
  "Build a PROTO-NAME protobuf message from a map."
  [m]
  (let [builder (JAVA-CLASS/newBuilder)]
    REGULAR-FIELDS
    ONEOF-PAYLOAD
    (.build builder)))
(defn PARSE-FN-NAME
  "Parse a PROTO-NAME protobuf message to a map."
  [^JAVA-CLASS proto]
  (cond-> {}
    REGULAR-FIELDS
    ONEOF-PAYLOAD))
(defn BUILD-FN-NAME
  "Build a PROTO-NAME protobuf message from a map."
  [m]
  (let [builder (JAVA-CLASS/newBuilder)]
    REGULAR-FIELDS
    ONEOF-PAYLOAD
    (.build builder)))
(defn PARSE-FN-NAME
  "Parse a PROTO-NAME protobuf message to a map."
  [^JAVA-CLASS proto]
  (cond-> {}
    REGULAR-FIELDS
    ONEOF-PAYLOAD))
(defn BUILD-FN-NAME
  "Build a PROTO-NAME protobuf message from a map."
  [m]
  (let [builder (JAVA-CLASS/newBuilder)]
    REGULAR-FIELDS
    ONEOF-PAYLOAD
    (.build builder)))
(defn PARSE-FN-NAME
  "Parse a PROTO-NAME protobuf message to a map."
  [^JAVA-CLASS proto]
  (cond-> {}
    REGULAR-FIELDS
    ONEOF-PAYLOAD))
(defn BUILD-FN-NAME
  "Build a PROTO-NAME protobuf message from a map."
  [m]
  (let [builder (JAVA-CLASS/newBuilder)]
    REGULAR-FIELDS
    ONEOF-PAYLOAD
    (.build builder)))
(defn PARSE-FN-NAME
  "Parse a PROTO-NAME protobuf message to a map."
  [^JAVA-CLASS proto]
  (cond-> {}
    REGULAR-FIELDS
    ONEOF-PAYLOAD))
(defn BUILD-FN-NAME
  "Build a PROTO-NAME protobuf message from a map."
  [m]
  (let [builder (JAVA-CLASS/newBuilder)]
    REGULAR-FIELDS
    ONEOF-PAYLOAD
    (.build builder)))
(defn PARSE-FN-NAME
  "Parse a PROTO-NAME protobuf message to a map."
  [^JAVA-CLASS proto]
  (cond-> {}
    REGULAR-FIELDS
    ONEOF-PAYLOAD))
(defn BUILD-FN-NAME
  "Build a PROTO-NAME protobuf message from a map."
  [m]
  (let [builder (JAVA-CLASS/newBuilder)]
    REGULAR-FIELDS
    ONEOF-PAYLOAD
    (.build builder)))
(defn PARSE-FN-NAME
  "Parse a PROTO-NAME protobuf message to a map."
  [^JAVA-CLASS proto]
  (cond-> {}
    REGULAR-FIELDS
    ONEOF-PAYLOAD))
(defn BUILD-FN-NAME
  "Build a PROTO-NAME protobuf message from a map."
  [m]
  (let [builder (JAVA-CLASS/newBuilder)]
    REGULAR-FIELDS
    ONEOF-PAYLOAD
    (.build builder)))
(defn PARSE-FN-NAME
  "Parse a PROTO-NAME protobuf message to a map."
  [^JAVA-CLASS proto]
  (cond-> {}
    REGULAR-FIELDS
    ONEOF-PAYLOAD))
(defn BUILD-FN-NAME
  "Build a PROTO-NAME protobuf message from a map."
  [m]
  (let [builder (JAVA-CLASS/newBuilder)]
    REGULAR-FIELDS
    ONEOF-PAYLOAD
    (.build builder)))
(defn PARSE-FN-NAME
  "Parse a PROTO-NAME protobuf message to a map."
  [^JAVA-CLASS proto]
  (cond-> {}
    REGULAR-FIELDS
    ONEOF-PAYLOAD))
(defn BUILD-FN-NAME
  "Build a PROTO-NAME protobuf message from a map."
  [m]
  (let [builder (JAVA-CLASS/newBuilder)]
    REGULAR-FIELDS
    ONEOF-PAYLOAD
    (.build builder)))
(defn PARSE-FN-NAME
  "Parse a PROTO-NAME protobuf message to a map."
  [^JAVA-CLASS proto]
  (cond-> {}
    REGULAR-FIELDS
    ONEOF-PAYLOAD))
(defn BUILD-FN-NAME
  "Build a PROTO-NAME protobuf message from a map."
  [m]
  (let [builder (JAVA-CLASS/newBuilder)]
    REGULAR-FIELDS
    ONEOF-PAYLOAD
    (.build builder)))
(defn PARSE-FN-NAME
  "Parse a PROTO-NAME protobuf message to a map."
  [^JAVA-CLASS proto]
  (cond-> {}
    REGULAR-FIELDS
    ONEOF-PAYLOAD))
(defn BUILD-FN-NAME
  "Build a PROTO-NAME protobuf message from a map."
  [m]
  (let [builder (JAVA-CLASS/newBuilder)]
    REGULAR-FIELDS
    ONEOF-PAYLOAD
    (.build builder)))

(defn BUILD-FN-NAME-PAYLOAD
  "Set the oneof payload field."
  [builder [field-key field-value]]
  (case field-key
    CASE-CLAUSES
    (throw (ex-info "Unknown payload field" {:field field-key}))))
(defn PARSE-FN-NAME
  "Parse a PROTO-NAME protobuf message to a map."
  [^JAVA-CLASS proto]
  (cond-> {}
    REGULAR-FIELDS
    ONEOF-PAYLOAD))

(defn PARSE-FN-NAME-PAYLOAD
  "Parse the oneof payload from a PROTO-NAME."
  [^JAVA-CLASS proto]
  (case (.GET-ONEOF-CASE proto)
    CASE-CLAUSES
    ;; Default case - no payload set
    {}))
(defn BUILD-FN-NAME
  "Build a PROTO-NAME protobuf message from a map."
  [m]
  (let [builder (JAVA-CLASS/newBuilder)]
    REGULAR-FIELDS
    ONEOF-PAYLOAD
    (.build builder)))
(defn PARSE-FN-NAME
  "Parse a PROTO-NAME protobuf message to a map."
  [^JAVA-CLASS proto]
  (cond-> {}
    REGULAR-FIELDS
    ONEOF-PAYLOAD))
(defn BUILD-FN-NAME
  "Build a PROTO-NAME protobuf message from a map."
  [m]
  (let [builder (JAVA-CLASS/newBuilder)]
    REGULAR-FIELDS
    ONEOF-PAYLOAD
    (.build builder)))
(defn PARSE-FN-NAME
  "Parse a PROTO-NAME protobuf message to a map."
  [^JAVA-CLASS proto]
  (cond-> {}
    REGULAR-FIELDS
    ONEOF-PAYLOAD))
(defn BUILD-FN-NAME
  "Build a PROTO-NAME protobuf message from a map."
  [m]
  (let [builder (JAVA-CLASS/newBuilder)]
    REGULAR-FIELDS
    ONEOF-PAYLOAD
    (.build builder)))
(defn PARSE-FN-NAME
  "Parse a PROTO-NAME protobuf message to a map."
  [^JAVA-CLASS proto]
  (cond-> {}
    REGULAR-FIELDS
    ONEOF-PAYLOAD))
(defn BUILD-FN-NAME
  "Build a PROTO-NAME protobuf message from a map."
  [m]
  (let [builder (JAVA-CLASS/newBuilder)]
    REGULAR-FIELDS
    ONEOF-PAYLOAD
    (.build builder)))
(defn PARSE-FN-NAME
  "Parse a PROTO-NAME protobuf message to a map."
  [^JAVA-CLASS proto]
  (cond-> {}
    REGULAR-FIELDS
    ONEOF-PAYLOAD))
(defn BUILD-FN-NAME
  "Build a PROTO-NAME protobuf message from a map."
  [m]
  (let [builder (JAVA-CLASS/newBuilder)]
    REGULAR-FIELDS
    ONEOF-PAYLOAD
    (.build builder)))
(defn PARSE-FN-NAME
  "Parse a PROTO-NAME protobuf message to a map."
  [^JAVA-CLASS proto]
  (cond-> {}
    REGULAR-FIELDS
    ONEOF-PAYLOAD))
(defn BUILD-FN-NAME
  "Build a PROTO-NAME protobuf message from a map."
  [m]
  (let [builder (JAVA-CLASS/newBuilder)]
    REGULAR-FIELDS
    ONEOF-PAYLOAD
    (.build builder)))
(defn PARSE-FN-NAME
  "Parse a PROTO-NAME protobuf message to a map."
  [^JAVA-CLASS proto]
  (cond-> {}
    REGULAR-FIELDS
    ONEOF-PAYLOAD))
(defn BUILD-FN-NAME
  "Build a PROTO-NAME protobuf message from a map."
  [m]
  (let [builder (JAVA-CLASS/newBuilder)]
    REGULAR-FIELDS
    ONEOF-PAYLOAD
    (.build builder)))
(defn PARSE-FN-NAME
  "Parse a PROTO-NAME protobuf message to a map."
  [^JAVA-CLASS proto]
  (cond-> {}
    REGULAR-FIELDS
    ONEOF-PAYLOAD))
(defn BUILD-FN-NAME
  "Build a PROTO-NAME protobuf message from a map."
  [m]
  (let [builder (JAVA-CLASS/newBuilder)]
    REGULAR-FIELDS
    ONEOF-PAYLOAD
    (.build builder)))
(defn PARSE-FN-NAME
  "Parse a PROTO-NAME protobuf message to a map."
  [^JAVA-CLASS proto]
  (cond-> {}
    REGULAR-FIELDS
    ONEOF-PAYLOAD))
(defn BUILD-FN-NAME
  "Build a PROTO-NAME protobuf message from a map."
  [m]
  (let [builder (JAVA-CLASS/newBuilder)]
    REGULAR-FIELDS
    ONEOF-PAYLOAD
    (.build builder)))
(defn PARSE-FN-NAME
  "Parse a PROTO-NAME protobuf message to a map."
  [^JAVA-CLASS proto]
  (cond-> {}
    REGULAR-FIELDS
    ONEOF-PAYLOAD))
(defn BUILD-FN-NAME
  "Build a PROTO-NAME protobuf message from a map."
  [m]
  (let [builder (JAVA-CLASS/newBuilder)]
    REGULAR-FIELDS
    ONEOF-PAYLOAD
    (.build builder)))
(defn PARSE-FN-NAME
  "Parse a PROTO-NAME protobuf message to a map."
  [^JAVA-CLASS proto]
  (cond-> {}
    REGULAR-FIELDS
    ONEOF-PAYLOAD))
(defn BUILD-FN-NAME
  "Build a PROTO-NAME protobuf message from a map."
  [m]
  (let [builder (JAVA-CLASS/newBuilder)]
    REGULAR-FIELDS
    ONEOF-PAYLOAD
    (.build builder)))
(defn PARSE-FN-NAME
  "Parse a PROTO-NAME protobuf message to a map."
  [^JAVA-CLASS proto]
  (cond-> {}
    REGULAR-FIELDS
    ONEOF-PAYLOAD))
(defn BUILD-FN-NAME
  "Build a PROTO-NAME protobuf message from a map."
  [m]
  (let [builder (JAVA-CLASS/newBuilder)]
    REGULAR-FIELDS
    ONEOF-PAYLOAD
    (.build builder)))
(defn PARSE-FN-NAME
  "Parse a PROTO-NAME protobuf message to a map."
  [^JAVA-CLASS proto]
  (cond-> {}
    REGULAR-FIELDS
    ONEOF-PAYLOAD))
(defn BUILD-FN-NAME
  "Build a PROTO-NAME protobuf message from a map."
  [m]
  (let [builder (JAVA-CLASS/newBuilder)]
    REGULAR-FIELDS
    ONEOF-PAYLOAD
    (.build builder)))
(defn PARSE-FN-NAME
  "Parse a PROTO-NAME protobuf message to a map."
  [^JAVA-CLASS proto]
  (cond-> {}
    REGULAR-FIELDS
    ONEOF-PAYLOAD))
(defn BUILD-FN-NAME
  "Build a PROTO-NAME protobuf message from a map."
  [m]
  (let [builder (JAVA-CLASS/newBuilder)]
    REGULAR-FIELDS
    ONEOF-PAYLOAD
    (.build builder)))
(defn PARSE-FN-NAME
  "Parse a PROTO-NAME protobuf message to a map."
  [^JAVA-CLASS proto]
  (cond-> {}
    REGULAR-FIELDS
    ONEOF-PAYLOAD))
(defn BUILD-FN-NAME
  "Build a PROTO-NAME protobuf message from a map."
  [m]
  (let [builder (JAVA-CLASS/newBuilder)]
    REGULAR-FIELDS
    ONEOF-PAYLOAD
    (.build builder)))
(defn PARSE-FN-NAME
  "Parse a PROTO-NAME protobuf message to a map."
  [^JAVA-CLASS proto]
  (cond-> {}
    REGULAR-FIELDS
    ONEOF-PAYLOAD))
(defn BUILD-FN-NAME
  "Build a PROTO-NAME protobuf message from a map."
  [m]
  (let [builder (JAVA-CLASS/newBuilder)]
    REGULAR-FIELDS
    ONEOF-PAYLOAD
    (.build builder)))
(defn PARSE-FN-NAME
  "Parse a PROTO-NAME protobuf message to a map."
  [^JAVA-CLASS proto]
  (cond-> {}
    REGULAR-FIELDS
    ONEOF-PAYLOAD))
(defn BUILD-FN-NAME
  "Build a PROTO-NAME protobuf message from a map."
  [m]
  (let [builder (JAVA-CLASS/newBuilder)]
    REGULAR-FIELDS
    ONEOF-PAYLOAD
    (.build builder)))

(defn BUILD-FN-NAME-PAYLOAD
  "Set the oneof payload field."
  [builder [field-key field-value]]
  (case field-key
    CASE-CLAUSES
    (throw (ex-info "Unknown payload field" {:field field-key}))))
(defn PARSE-FN-NAME
  "Parse a PROTO-NAME protobuf message to a map."
  [^JAVA-CLASS proto]
  (cond-> {}
    REGULAR-FIELDS
    ONEOF-PAYLOAD))

(defn PARSE-FN-NAME-PAYLOAD
  "Parse the oneof payload from a PROTO-NAME."
  [^JAVA-CLASS proto]
  (case (.GET-ONEOF-CASE proto)
    CASE-CLAUSES
    ;; Default case - no payload set
    {}))
(defn BUILD-FN-NAME
  "Build a PROTO-NAME protobuf message from a map."
  [m]
  (let [builder (JAVA-CLASS/newBuilder)]
    REGULAR-FIELDS
    ONEOF-PAYLOAD
    (.build builder)))
(defn PARSE-FN-NAME
  "Parse a PROTO-NAME protobuf message to a map."
  [^JAVA-CLASS proto]
  (cond-> {}
    REGULAR-FIELDS
    ONEOF-PAYLOAD))
(defn BUILD-FN-NAME
  "Build a PROTO-NAME protobuf message from a map."
  [m]
  (let [builder (JAVA-CLASS/newBuilder)]
    REGULAR-FIELDS
    ONEOF-PAYLOAD
    (.build builder)))
(defn PARSE-FN-NAME
  "Parse a PROTO-NAME protobuf message to a map."
  [^JAVA-CLASS proto]
  (cond-> {}
    REGULAR-FIELDS
    ONEOF-PAYLOAD))
(defn BUILD-FN-NAME
  "Build a PROTO-NAME protobuf message from a map."
  [m]
  (let [builder (JAVA-CLASS/newBuilder)]
    REGULAR-FIELDS
    ONEOF-PAYLOAD
    (.build builder)))
(defn PARSE-FN-NAME
  "Parse a PROTO-NAME protobuf message to a map."
  [^JAVA-CLASS proto]
  (cond-> {}
    REGULAR-FIELDS
    ONEOF-PAYLOAD))
(defn BUILD-FN-NAME
  "Build a PROTO-NAME protobuf message from a map."
  [m]
  (let [builder (JAVA-CLASS/newBuilder)]
    REGULAR-FIELDS
    ONEOF-PAYLOAD
    (.build builder)))
(defn PARSE-FN-NAME
  "Parse a PROTO-NAME protobuf message to a map."
  [^JAVA-CLASS proto]
  (cond-> {}
    REGULAR-FIELDS
    ONEOF-PAYLOAD))
(defn BUILD-FN-NAME
  "Build a PROTO-NAME protobuf message from a map."
  [m]
  (let [builder (JAVA-CLASS/newBuilder)]
    REGULAR-FIELDS
    ONEOF-PAYLOAD
    (.build builder)))
(defn PARSE-FN-NAME
  "Parse a PROTO-NAME protobuf message to a map."
  [^JAVA-CLASS proto]
  (cond-> {}
    REGULAR-FIELDS
    ONEOF-PAYLOAD))
(defn BUILD-FN-NAME
  "Build a PROTO-NAME protobuf message from a map."
  [m]
  (let [builder (JAVA-CLASS/newBuilder)]
    REGULAR-FIELDS
    ONEOF-PAYLOAD
    (.build builder)))
(defn PARSE-FN-NAME
  "Parse a PROTO-NAME protobuf message to a map."
  [^JAVA-CLASS proto]
  (cond-> {}
    REGULAR-FIELDS
    ONEOF-PAYLOAD))
(defn BUILD-FN-NAME
  "Build a PROTO-NAME protobuf message from a map."
  [m]
  (let [builder (JAVA-CLASS/newBuilder)]
    REGULAR-FIELDS
    ONEOF-PAYLOAD
    (.build builder)))
(defn PARSE-FN-NAME
  "Parse a PROTO-NAME protobuf message to a map."
  [^JAVA-CLASS proto]
  (cond-> {}
    REGULAR-FIELDS
    ONEOF-PAYLOAD))
(defn BUILD-FN-NAME
  "Build a PROTO-NAME protobuf message from a map."
  [m]
  (let [builder (JAVA-CLASS/newBuilder)]
    REGULAR-FIELDS
    ONEOF-PAYLOAD
    (.build builder)))
(defn PARSE-FN-NAME
  "Parse a PROTO-NAME protobuf message to a map."
  [^JAVA-CLASS proto]
  (cond-> {}
    REGULAR-FIELDS
    ONEOF-PAYLOAD))
(defn BUILD-FN-NAME
  "Build a PROTO-NAME protobuf message from a map."
  [m]
  (let [builder (JAVA-CLASS/newBuilder)]
    REGULAR-FIELDS
    ONEOF-PAYLOAD
    (.build builder)))
(defn PARSE-FN-NAME
  "Parse a PROTO-NAME protobuf message to a map."
  [^JAVA-CLASS proto]
  (cond-> {}
    REGULAR-FIELDS
    ONEOF-PAYLOAD))
(defn BUILD-FN-NAME
  "Build a PROTO-NAME protobuf message from a map."
  [m]
  (let [builder (JAVA-CLASS/newBuilder)]
    REGULAR-FIELDS
    ONEOF-PAYLOAD
    (.build builder)))
(defn PARSE-FN-NAME
  "Parse a PROTO-NAME protobuf message to a map."
  [^JAVA-CLASS proto]
  (cond-> {}
    REGULAR-FIELDS
    ONEOF-PAYLOAD))
(defn BUILD-FN-NAME
  "Build a PROTO-NAME protobuf message from a map."
  [m]
  (let [builder (JAVA-CLASS/newBuilder)]
    REGULAR-FIELDS
    ONEOF-PAYLOAD
    (.build builder)))
(defn PARSE-FN-NAME
  "Parse a PROTO-NAME protobuf message to a map."
  [^JAVA-CLASS proto]
  (cond-> {}
    REGULAR-FIELDS
    ONEOF-PAYLOAD))
(defn BUILD-FN-NAME
  "Build a PROTO-NAME protobuf message from a map."
  [m]
  (let [builder (JAVA-CLASS/newBuilder)]
    REGULAR-FIELDS
    ONEOF-PAYLOAD
    (.build builder)))
(defn PARSE-FN-NAME
  "Parse a PROTO-NAME protobuf message to a map."
  [^JAVA-CLASS proto]
  (cond-> {}
    REGULAR-FIELDS
    ONEOF-PAYLOAD))
(defn BUILD-FN-NAME
  "Build a PROTO-NAME protobuf message from a map."
  [m]
  (let [builder (JAVA-CLASS/newBuilder)]
    REGULAR-FIELDS
    ONEOF-PAYLOAD
    (.build builder)))
(defn PARSE-FN-NAME
  "Parse a PROTO-NAME protobuf message to a map."
  [^JAVA-CLASS proto]
  (cond-> {}
    REGULAR-FIELDS
    ONEOF-PAYLOAD))
(defn BUILD-FN-NAME
  "Build a PROTO-NAME protobuf message from a map."
  [m]
  (let [builder (JAVA-CLASS/newBuilder)]
    REGULAR-FIELDS
    ONEOF-PAYLOAD
    (.build builder)))
(defn PARSE-FN-NAME
  "Parse a PROTO-NAME protobuf message to a map."
  [^JAVA-CLASS proto]
  (cond-> {}
    REGULAR-FIELDS
    ONEOF-PAYLOAD))
(defn BUILD-FN-NAME
  "Build a PROTO-NAME protobuf message from a map."
  [m]
  (let [builder (JAVA-CLASS/newBuilder)]
    REGULAR-FIELDS
    ONEOF-PAYLOAD
    (.build builder)))
(defn PARSE-FN-NAME
  "Parse a PROTO-NAME protobuf message to a map."
  [^JAVA-CLASS proto]
  (cond-> {}
    REGULAR-FIELDS
    ONEOF-PAYLOAD))
(defn BUILD-FN-NAME
  "Build a PROTO-NAME protobuf message from a map."
  [m]
  (let [builder (JAVA-CLASS/newBuilder)]
    REGULAR-FIELDS
    ONEOF-PAYLOAD
    (.build builder)))
(defn PARSE-FN-NAME
  "Parse a PROTO-NAME protobuf message to a map."
  [^JAVA-CLASS proto]
  (cond-> {}
    REGULAR-FIELDS
    ONEOF-PAYLOAD))
(defn BUILD-FN-NAME
  "Build a PROTO-NAME protobuf message from a map."
  [m]
  (let [builder (JAVA-CLASS/newBuilder)]
    REGULAR-FIELDS
    ONEOF-PAYLOAD
    (.build builder)))

(defn BUILD-FN-NAME-PAYLOAD
  "Set the oneof payload field."
  [builder [field-key field-value]]
  (case field-key
    CASE-CLAUSES
    (throw (ex-info "Unknown payload field" {:field field-key}))))
(defn PARSE-FN-NAME
  "Parse a PROTO-NAME protobuf message to a map."
  [^JAVA-CLASS proto]
  (cond-> {}
    REGULAR-FIELDS
    ONEOF-PAYLOAD))

(defn PARSE-FN-NAME-PAYLOAD
  "Parse the oneof payload from a PROTO-NAME."
  [^JAVA-CLASS proto]
  (case (.GET-ONEOF-CASE proto)
    CASE-CLAUSES
    ;; Default case - no payload set
    {}))
(defn BUILD-FN-NAME
  "Build a PROTO-NAME protobuf message from a map."
  [m]
  (let [builder (JAVA-CLASS/newBuilder)]
    REGULAR-FIELDS
    ONEOF-PAYLOAD
    (.build builder)))
(defn PARSE-FN-NAME
  "Parse a PROTO-NAME protobuf message to a map."
  [^JAVA-CLASS proto]
  (cond-> {}
    REGULAR-FIELDS
    ONEOF-PAYLOAD))
(defn BUILD-FN-NAME
  "Build a PROTO-NAME protobuf message from a map."
  [m]
  (let [builder (JAVA-CLASS/newBuilder)]
    REGULAR-FIELDS
    ONEOF-PAYLOAD
    (.build builder)))
(defn PARSE-FN-NAME
  "Parse a PROTO-NAME protobuf message to a map."
  [^JAVA-CLASS proto]
  (cond-> {}
    REGULAR-FIELDS
    ONEOF-PAYLOAD))
(defn BUILD-FN-NAME
  "Build a PROTO-NAME protobuf message from a map."
  [m]
  (let [builder (JAVA-CLASS/newBuilder)]
    REGULAR-FIELDS
    ONEOF-PAYLOAD
    (.build builder)))
(defn PARSE-FN-NAME
  "Parse a PROTO-NAME protobuf message to a map."
  [^JAVA-CLASS proto]
  (cond-> {}
    REGULAR-FIELDS
    ONEOF-PAYLOAD))
(defn BUILD-FN-NAME
  "Build a PROTO-NAME protobuf message from a map."
  [m]
  (let [builder (JAVA-CLASS/newBuilder)]
    REGULAR-FIELDS
    ONEOF-PAYLOAD
    (.build builder)))
(defn PARSE-FN-NAME
  "Parse a PROTO-NAME protobuf message to a map."
  [^JAVA-CLASS proto]
  (cond-> {}
    REGULAR-FIELDS
    ONEOF-PAYLOAD))
(defn BUILD-FN-NAME
  "Build a PROTO-NAME protobuf message from a map."
  [m]
  (let [builder (JAVA-CLASS/newBuilder)]
    REGULAR-FIELDS
    ONEOF-PAYLOAD
    (.build builder)))

(defn BUILD-FN-NAME-PAYLOAD
  "Set the oneof payload field."
  [builder [field-key field-value]]
  (case field-key
    CASE-CLAUSES
    (throw (ex-info "Unknown payload field" {:field field-key}))))
(defn PARSE-FN-NAME
  "Parse a PROTO-NAME protobuf message to a map."
  [^JAVA-CLASS proto]
  (cond-> {}
    REGULAR-FIELDS
    ONEOF-PAYLOAD))

(defn PARSE-FN-NAME-PAYLOAD
  "Parse the oneof payload from a PROTO-NAME."
  [^JAVA-CLASS proto]
  (case (.GET-ONEOF-CASE proto)
    CASE-CLAUSES
    ;; Default case - no payload set
    {}))
(defn BUILD-FN-NAME
  "Build a PROTO-NAME protobuf message from a map."
  [m]
  (let [builder (JAVA-CLASS/newBuilder)]
    REGULAR-FIELDS
    ONEOF-PAYLOAD
    (.build builder)))
(defn PARSE-FN-NAME
  "Parse a PROTO-NAME protobuf message to a map."
  [^JAVA-CLASS proto]
  (cond-> {}
    REGULAR-FIELDS
    ONEOF-PAYLOAD))
(defn BUILD-FN-NAME
  "Build a PROTO-NAME protobuf message from a map."
  [m]
  (let [builder (JAVA-CLASS/newBuilder)]
    REGULAR-FIELDS
    ONEOF-PAYLOAD
    (.build builder)))
(defn PARSE-FN-NAME
  "Parse a PROTO-NAME protobuf message to a map."
  [^JAVA-CLASS proto]
  (cond-> {}
    REGULAR-FIELDS
    ONEOF-PAYLOAD))
(defn BUILD-FN-NAME
  "Build a PROTO-NAME protobuf message from a map."
  [m]
  (let [builder (JAVA-CLASS/newBuilder)]
    REGULAR-FIELDS
    ONEOF-PAYLOAD
    (.build builder)))
(defn PARSE-FN-NAME
  "Parse a PROTO-NAME protobuf message to a map."
  [^JAVA-CLASS proto]
  (cond-> {}
    REGULAR-FIELDS
    ONEOF-PAYLOAD))
(defn BUILD-FN-NAME
  "Build a PROTO-NAME protobuf message from a map."
  [m]
  (let [builder (JAVA-CLASS/newBuilder)]
    REGULAR-FIELDS
    ONEOF-PAYLOAD
    (.build builder)))
(defn PARSE-FN-NAME
  "Parse a PROTO-NAME protobuf message to a map."
  [^JAVA-CLASS proto]
  (cond-> {}
    REGULAR-FIELDS
    ONEOF-PAYLOAD))
(defn BUILD-FN-NAME
  "Build a PROTO-NAME protobuf message from a map."
  [m]
  (let [builder (JAVA-CLASS/newBuilder)]
    REGULAR-FIELDS
    ONEOF-PAYLOAD
    (.build builder)))
(defn PARSE-FN-NAME
  "Parse a PROTO-NAME protobuf message to a map."
  [^JAVA-CLASS proto]
  (cond-> {}
    REGULAR-FIELDS
    ONEOF-PAYLOAD))
(defn BUILD-FN-NAME
  "Build a PROTO-NAME protobuf message from a map."
  [m]
  (let [builder (JAVA-CLASS/newBuilder)]
    REGULAR-FIELDS
    ONEOF-PAYLOAD
    (.build builder)))
(defn PARSE-FN-NAME
  "Parse a PROTO-NAME protobuf message to a map."
  [^JAVA-CLASS proto]
  (cond-> {}
    REGULAR-FIELDS
    ONEOF-PAYLOAD))
(defn BUILD-FN-NAME
  "Build a PROTO-NAME protobuf message from a map."
  [m]
  (let [builder (JAVA-CLASS/newBuilder)]
    REGULAR-FIELDS
    ONEOF-PAYLOAD
    (.build builder)))
(defn PARSE-FN-NAME
  "Parse a PROTO-NAME protobuf message to a map."
  [^JAVA-CLASS proto]
  (cond-> {}
    REGULAR-FIELDS
    ONEOF-PAYLOAD))
(defn BUILD-FN-NAME
  "Build a PROTO-NAME protobuf message from a map."
  [m]
  (let [builder (JAVA-CLASS/newBuilder)]
    REGULAR-FIELDS
    ONEOF-PAYLOAD
    (.build builder)))
(defn PARSE-FN-NAME
  "Parse a PROTO-NAME protobuf message to a map."
  [^JAVA-CLASS proto]
  (cond-> {}
    REGULAR-FIELDS
    ONEOF-PAYLOAD))
(defn BUILD-FN-NAME
  "Build a PROTO-NAME protobuf message from a map."
  [m]
  (let [builder (JAVA-CLASS/newBuilder)]
    REGULAR-FIELDS
    ONEOF-PAYLOAD
    (.build builder)))

(defn BUILD-FN-NAME-PAYLOAD
  "Set the oneof payload field."
  [builder [field-key field-value]]
  (case field-key
    CASE-CLAUSES
    (throw (ex-info "Unknown payload field" {:field field-key}))))
(defn PARSE-FN-NAME
  "Parse a PROTO-NAME protobuf message to a map."
  [^JAVA-CLASS proto]
  (cond-> {}
    REGULAR-FIELDS
    ONEOF-PAYLOAD))

(defn PARSE-FN-NAME-PAYLOAD
  "Parse the oneof payload from a PROTO-NAME."
  [^JAVA-CLASS proto]
  (case (.GET-ONEOF-CASE proto)
    CASE-CLAUSES
    ;; Default case - no payload set
    {}))
(defn BUILD-FN-NAME
  "Build a PROTO-NAME protobuf message from a map."
  [m]
  (let [builder (JAVA-CLASS/newBuilder)]
    REGULAR-FIELDS
    ONEOF-PAYLOAD
    (.build builder)))

(defn BUILD-FN-NAME-PAYLOAD
  "Set the oneof payload field."
  [builder [field-key field-value]]
  (case field-key
    CASE-CLAUSES
    (throw (ex-info "Unknown payload field" {:field field-key}))))
(defn PARSE-FN-NAME
  "Parse a PROTO-NAME protobuf message to a map."
  [^JAVA-CLASS proto]
  (cond-> {}
    REGULAR-FIELDS
    ONEOF-PAYLOAD))

(defn PARSE-FN-NAME-PAYLOAD
  "Parse the oneof payload from a PROTO-NAME."
  [^JAVA-CLASS proto]
  (case (.GET-ONEOF-CASE proto)
    CASE-CLAUSES
    ;; Default case - no payload set
    {}))
(defn BUILD-FN-NAME
  "Build a PROTO-NAME protobuf message from a map."
  [m]
  (let [builder (JAVA-CLASS/newBuilder)]
    REGULAR-FIELDS
    ONEOF-PAYLOAD
    (.build builder)))
(defn PARSE-FN-NAME
  "Parse a PROTO-NAME protobuf message to a map."
  [^JAVA-CLASS proto]
  (cond-> {}
    REGULAR-FIELDS
    ONEOF-PAYLOAD))
(defn BUILD-FN-NAME
  "Build a PROTO-NAME protobuf message from a map."
  [m]
  (let [builder (JAVA-CLASS/newBuilder)]
    REGULAR-FIELDS
    ONEOF-PAYLOAD
    (.build builder)))
(defn PARSE-FN-NAME
  "Parse a PROTO-NAME protobuf message to a map."
  [^JAVA-CLASS proto]
  (cond-> {}
    REGULAR-FIELDS
    ONEOF-PAYLOAD))
(defn BUILD-FN-NAME
  "Build a PROTO-NAME protobuf message from a map."
  [m]
  (let [builder (JAVA-CLASS/newBuilder)]
    REGULAR-FIELDS
    ONEOF-PAYLOAD
    (.build builder)))
(defn PARSE-FN-NAME
  "Parse a PROTO-NAME protobuf message to a map."
  [^JAVA-CLASS proto]
  (cond-> {}
    REGULAR-FIELDS
    ONEOF-PAYLOAD))
(defn BUILD-FN-NAME
  "Build a PROTO-NAME protobuf message from a map."
  [m]
  (let [builder (JAVA-CLASS/newBuilder)]
    REGULAR-FIELDS
    ONEOF-PAYLOAD
    (.build builder)))
(defn PARSE-FN-NAME
  "Parse a PROTO-NAME protobuf message to a map."
  [^JAVA-CLASS proto]
  (cond-> {}
    REGULAR-FIELDS
    ONEOF-PAYLOAD))
(defn BUILD-FN-NAME
  "Build a PROTO-NAME protobuf message from a map."
  [m]
  (let [builder (JAVA-CLASS/newBuilder)]
    REGULAR-FIELDS
    ONEOF-PAYLOAD
    (.build builder)))

(defn BUILD-FN-NAME-PAYLOAD
  "Set the oneof payload field."
  [builder [field-key field-value]]
  (case field-key
    CASE-CLAUSES
    (throw (ex-info "Unknown payload field" {:field field-key}))))
(defn PARSE-FN-NAME
  "Parse a PROTO-NAME protobuf message to a map."
  [^JAVA-CLASS proto]
  (cond-> {}
    REGULAR-FIELDS
    ONEOF-PAYLOAD))

(defn PARSE-FN-NAME-PAYLOAD
  "Parse the oneof payload from a PROTO-NAME."
  [^JAVA-CLASS proto]
  (case (.GET-ONEOF-CASE proto)
    CASE-CLAUSES
    ;; Default case - no payload set
    {}))
(defn BUILD-FN-NAME
  "Build a PROTO-NAME protobuf message from a map."
  [m]
  (let [builder (JAVA-CLASS/newBuilder)]
    REGULAR-FIELDS
    ONEOF-PAYLOAD
    (.build builder)))
(defn PARSE-FN-NAME
  "Parse a PROTO-NAME protobuf message to a map."
  [^JAVA-CLASS proto]
  (cond-> {}
    REGULAR-FIELDS
    ONEOF-PAYLOAD))
(defn BUILD-FN-NAME
  "Build a PROTO-NAME protobuf message from a map."
  [m]
  (let [builder (JAVA-CLASS/newBuilder)]
    REGULAR-FIELDS
    ONEOF-PAYLOAD
    (.build builder)))
(defn PARSE-FN-NAME
  "Parse a PROTO-NAME protobuf message to a map."
  [^JAVA-CLASS proto]
  (cond-> {}
    REGULAR-FIELDS
    ONEOF-PAYLOAD))
(defn BUILD-FN-NAME
  "Build a PROTO-NAME protobuf message from a map."
  [m]
  (let [builder (JAVA-CLASS/newBuilder)]
    REGULAR-FIELDS
    ONEOF-PAYLOAD
    (.build builder)))
(defn PARSE-FN-NAME
  "Parse a PROTO-NAME protobuf message to a map."
  [^JAVA-CLASS proto]
  (cond-> {}
    REGULAR-FIELDS
    ONEOF-PAYLOAD))
(defn BUILD-FN-NAME
  "Build a PROTO-NAME protobuf message from a map."
  [m]
  (let [builder (JAVA-CLASS/newBuilder)]
    REGULAR-FIELDS
    ONEOF-PAYLOAD
    (.build builder)))
(defn PARSE-FN-NAME
  "Parse a PROTO-NAME protobuf message to a map."
  [^JAVA-CLASS proto]
  (cond-> {}
    REGULAR-FIELDS
    ONEOF-PAYLOAD))
(defn BUILD-FN-NAME
  "Build a PROTO-NAME protobuf message from a map."
  [m]
  (let [builder (JAVA-CLASS/newBuilder)]
    REGULAR-FIELDS
    ONEOF-PAYLOAD
    (.build builder)))
(defn PARSE-FN-NAME
  "Parse a PROTO-NAME protobuf message to a map."
  [^JAVA-CLASS proto]
  (cond-> {}
    REGULAR-FIELDS
    ONEOF-PAYLOAD))
(defn BUILD-FN-NAME
  "Build a PROTO-NAME protobuf message from a map."
  [m]
  (let [builder (JAVA-CLASS/newBuilder)]
    REGULAR-FIELDS
    ONEOF-PAYLOAD
    (.build builder)))
(defn PARSE-FN-NAME
  "Parse a PROTO-NAME protobuf message to a map."
  [^JAVA-CLASS proto]
  (cond-> {}
    REGULAR-FIELDS
    ONEOF-PAYLOAD))
(defn BUILD-FN-NAME
  "Build a PROTO-NAME protobuf message from a map."
  [m]
  (let [builder (JAVA-CLASS/newBuilder)]
    REGULAR-FIELDS
    ONEOF-PAYLOAD
    (.build builder)))
(defn PARSE-FN-NAME
  "Parse a PROTO-NAME protobuf message to a map."
  [^JAVA-CLASS proto]
  (cond-> {}
    REGULAR-FIELDS
    ONEOF-PAYLOAD))
(defn BUILD-FN-NAME
  "Build a PROTO-NAME protobuf message from a map."
  [m]
  (let [builder (JAVA-CLASS/newBuilder)]
    REGULAR-FIELDS
    ONEOF-PAYLOAD
    (.build builder)))
(defn PARSE-FN-NAME
  "Parse a PROTO-NAME protobuf message to a map."
  [^JAVA-CLASS proto]
  (cond-> {}
    REGULAR-FIELDS
    ONEOF-PAYLOAD))
(defn BUILD-FN-NAME
  "Build a PROTO-NAME protobuf message from a map."
  [m]
  (let [builder (JAVA-CLASS/newBuilder)]
    REGULAR-FIELDS
    ONEOF-PAYLOAD
    (.build builder)))
(defn PARSE-FN-NAME
  "Parse a PROTO-NAME protobuf message to a map."
  [^JAVA-CLASS proto]
  (cond-> {}
    REGULAR-FIELDS
    ONEOF-PAYLOAD))
(defn BUILD-FN-NAME
  "Build a PROTO-NAME protobuf message from a map."
  [m]
  (let [builder (JAVA-CLASS/newBuilder)]
    REGULAR-FIELDS
    ONEOF-PAYLOAD
    (.build builder)))
(defn PARSE-FN-NAME
  "Parse a PROTO-NAME protobuf message to a map."
  [^JAVA-CLASS proto]
  (cond-> {}
    REGULAR-FIELDS
    ONEOF-PAYLOAD))
(defn BUILD-FN-NAME
  "Build a PROTO-NAME protobuf message from a map."
  [m]
  (let [builder (JAVA-CLASS/newBuilder)]
    REGULAR-FIELDS
    ONEOF-PAYLOAD
    (.build builder)))
(defn PARSE-FN-NAME
  "Parse a PROTO-NAME protobuf message to a map."
  [^JAVA-CLASS proto]
  (cond-> {}
    REGULAR-FIELDS
    ONEOF-PAYLOAD))
(defn BUILD-FN-NAME
  "Build a PROTO-NAME protobuf message from a map."
  [m]
  (let [builder (JAVA-CLASS/newBuilder)]
    REGULAR-FIELDS
    ONEOF-PAYLOAD
    (.build builder)))
(defn PARSE-FN-NAME
  "Parse a PROTO-NAME protobuf message to a map."
  [^JAVA-CLASS proto]
  (cond-> {}
    REGULAR-FIELDS
    ONEOF-PAYLOAD))
(defn BUILD-FN-NAME
  "Build a PROTO-NAME protobuf message from a map."
  [m]
  (let [builder (JAVA-CLASS/newBuilder)]
    REGULAR-FIELDS
    ONEOF-PAYLOAD
    (.build builder)))
(defn PARSE-FN-NAME
  "Parse a PROTO-NAME protobuf message to a map."
  [^JAVA-CLASS proto]
  (cond-> {}
    REGULAR-FIELDS
    ONEOF-PAYLOAD))
(defn BUILD-FN-NAME
  "Build a PROTO-NAME protobuf message from a map."
  [m]
  (let [builder (JAVA-CLASS/newBuilder)]
    REGULAR-FIELDS
    ONEOF-PAYLOAD
    (.build builder)))

(defn BUILD-FN-NAME-PAYLOAD
  "Set the oneof payload field."
  [builder [field-key field-value]]
  (case field-key
    CASE-CLAUSES
    (throw (ex-info "Unknown payload field" {:field field-key}))))
(defn PARSE-FN-NAME
  "Parse a PROTO-NAME protobuf message to a map."
  [^JAVA-CLASS proto]
  (cond-> {}
    REGULAR-FIELDS
    ONEOF-PAYLOAD))

(defn PARSE-FN-NAME-PAYLOAD
  "Parse the oneof payload from a PROTO-NAME."
  [^JAVA-CLASS proto]
  (case (.GET-ONEOF-CASE proto)
    CASE-CLAUSES
    ;; Default case - no payload set
    {}))
(defn BUILD-FN-NAME
  "Build a PROTO-NAME protobuf message from a map."
  [m]
  (let [builder (JAVA-CLASS/newBuilder)]
    REGULAR-FIELDS
    ONEOF-PAYLOAD
    (.build builder)))
(defn PARSE-FN-NAME
  "Parse a PROTO-NAME protobuf message to a map."
  [^JAVA-CLASS proto]
  (cond-> {}
    REGULAR-FIELDS
    ONEOF-PAYLOAD))
(defn BUILD-FN-NAME
  "Build a PROTO-NAME protobuf message from a map."
  [m]
  (let [builder (JAVA-CLASS/newBuilder)]
    REGULAR-FIELDS
    ONEOF-PAYLOAD
    (.build builder)))
(defn PARSE-FN-NAME
  "Parse a PROTO-NAME protobuf message to a map."
  [^JAVA-CLASS proto]
  (cond-> {}
    REGULAR-FIELDS
    ONEOF-PAYLOAD))
(defn BUILD-FN-NAME
  "Build a PROTO-NAME protobuf message from a map."
  [m]
  (let [builder (JAVA-CLASS/newBuilder)]
    REGULAR-FIELDS
    ONEOF-PAYLOAD
    (.build builder)))
(defn PARSE-FN-NAME
  "Parse a PROTO-NAME protobuf message to a map."
  [^JAVA-CLASS proto]
  (cond-> {}
    REGULAR-FIELDS
    ONEOF-PAYLOAD))
(defn BUILD-FN-NAME
  "Build a PROTO-NAME protobuf message from a map."
  [m]
  (let [builder (JAVA-CLASS/newBuilder)]
    REGULAR-FIELDS
    ONEOF-PAYLOAD
    (.build builder)))
(defn PARSE-FN-NAME
  "Parse a PROTO-NAME protobuf message to a map."
  [^JAVA-CLASS proto]
  (cond-> {}
    REGULAR-FIELDS
    ONEOF-PAYLOAD))
(defn BUILD-FN-NAME
  "Build a PROTO-NAME protobuf message from a map."
  [m]
  (let [builder (JAVA-CLASS/newBuilder)]
    REGULAR-FIELDS
    ONEOF-PAYLOAD
    (.build builder)))
(defn PARSE-FN-NAME
  "Parse a PROTO-NAME protobuf message to a map."
  [^JAVA-CLASS proto]
  (cond-> {}
    REGULAR-FIELDS
    ONEOF-PAYLOAD))
(defn BUILD-FN-NAME
  "Build a PROTO-NAME protobuf message from a map."
  [m]
  (let [builder (JAVA-CLASS/newBuilder)]
    REGULAR-FIELDS
    ONEOF-PAYLOAD
    (.build builder)))
(defn PARSE-FN-NAME
  "Parse a PROTO-NAME protobuf message to a map."
  [^JAVA-CLASS proto]
  (cond-> {}
    REGULAR-FIELDS
    ONEOF-PAYLOAD))
(defn BUILD-FN-NAME
  "Build a PROTO-NAME protobuf message from a map."
  [m]
  (let [builder (JAVA-CLASS/newBuilder)]
    REGULAR-FIELDS
    ONEOF-PAYLOAD
    (.build builder)))
(defn PARSE-FN-NAME
  "Parse a PROTO-NAME protobuf message to a map."
  [^JAVA-CLASS proto]
  (cond-> {}
    REGULAR-FIELDS
    ONEOF-PAYLOAD))
(defn BUILD-FN-NAME
  "Build a PROTO-NAME protobuf message from a map."
  [m]
  (let [builder (JAVA-CLASS/newBuilder)]
    REGULAR-FIELDS
    ONEOF-PAYLOAD
    (.build builder)))
(defn PARSE-FN-NAME
  "Parse a PROTO-NAME protobuf message to a map."
  [^JAVA-CLASS proto]
  (cond-> {}
    REGULAR-FIELDS
    ONEOF-PAYLOAD))
(defn BUILD-FN-NAME
  "Build a PROTO-NAME protobuf message from a map."
  [m]
  (let [builder (JAVA-CLASS/newBuilder)]
    REGULAR-FIELDS
    ONEOF-PAYLOAD
    (.build builder)))
(defn PARSE-FN-NAME
  "Parse a PROTO-NAME protobuf message to a map."
  [^JAVA-CLASS proto]
  (cond-> {}
    REGULAR-FIELDS
    ONEOF-PAYLOAD))
(defn BUILD-FN-NAME
  "Build a PROTO-NAME protobuf message from a map."
  [m]
  (let [builder (JAVA-CLASS/newBuilder)]
    REGULAR-FIELDS
    ONEOF-PAYLOAD
    (.build builder)))

(defn BUILD-FN-NAME-PAYLOAD
  "Set the oneof payload field."
  [builder [field-key field-value]]
  (case field-key
    CASE-CLAUSES
    (throw (ex-info "Unknown payload field" {:field field-key}))))
(defn PARSE-FN-NAME
  "Parse a PROTO-NAME protobuf message to a map."
  [^JAVA-CLASS proto]
  (cond-> {}
    REGULAR-FIELDS
    ONEOF-PAYLOAD))

(defn PARSE-FN-NAME-PAYLOAD
  "Parse the oneof payload from a PROTO-NAME."
  [^JAVA-CLASS proto]
  (case (.GET-ONEOF-CASE proto)
    CASE-CLAUSES
    ;; Default case - no payload set
    {}))
(defn BUILD-FN-NAME
  "Build a PROTO-NAME protobuf message from a map."
  [m]
  (let [builder (JAVA-CLASS/newBuilder)]
    REGULAR-FIELDS
    ONEOF-PAYLOAD
    (.build builder)))
(defn PARSE-FN-NAME
  "Parse a PROTO-NAME protobuf message to a map."
  [^JAVA-CLASS proto]
  (cond-> {}
    REGULAR-FIELDS
    ONEOF-PAYLOAD))
(defn BUILD-FN-NAME
  "Build a PROTO-NAME protobuf message from a map."
  [m]
  (let [builder (JAVA-CLASS/newBuilder)]
    REGULAR-FIELDS
    ONEOF-PAYLOAD
    (.build builder)))
(defn PARSE-FN-NAME
  "Parse a PROTO-NAME protobuf message to a map."
  [^JAVA-CLASS proto]
  (cond-> {}
    REGULAR-FIELDS
    ONEOF-PAYLOAD))
(defn BUILD-FN-NAME
  "Build a PROTO-NAME protobuf message from a map."
  [m]
  (let [builder (JAVA-CLASS/newBuilder)]
    REGULAR-FIELDS
    ONEOF-PAYLOAD
    (.build builder)))
(defn PARSE-FN-NAME
  "Parse a PROTO-NAME protobuf message to a map."
  [^JAVA-CLASS proto]
  (cond-> {}
    REGULAR-FIELDS
    ONEOF-PAYLOAD))
(defn BUILD-FN-NAME
  "Build a PROTO-NAME protobuf message from a map."
  [m]
  (let [builder (JAVA-CLASS/newBuilder)]
    REGULAR-FIELDS
    ONEOF-PAYLOAD
    (.build builder)))
(defn PARSE-FN-NAME
  "Parse a PROTO-NAME protobuf message to a map."
  [^JAVA-CLASS proto]
  (cond-> {}
    REGULAR-FIELDS
    ONEOF-PAYLOAD))
(defn BUILD-FN-NAME
  "Build a PROTO-NAME protobuf message from a map."
  [m]
  (let [builder (JAVA-CLASS/newBuilder)]
    REGULAR-FIELDS
    ONEOF-PAYLOAD
    (.build builder)))
(defn PARSE-FN-NAME
  "Parse a PROTO-NAME protobuf message to a map."
  [^JAVA-CLASS proto]
  (cond-> {}
    REGULAR-FIELDS
    ONEOF-PAYLOAD))
(defn BUILD-FN-NAME
  "Build a PROTO-NAME protobuf message from a map."
  [m]
  (let [builder (JAVA-CLASS/newBuilder)]
    REGULAR-FIELDS
    ONEOF-PAYLOAD
    (.build builder)))

(defn BUILD-FN-NAME-PAYLOAD
  "Set the oneof payload field."
  [builder [field-key field-value]]
  (case field-key
    CASE-CLAUSES
    (throw (ex-info "Unknown payload field" {:field field-key}))))
(defn PARSE-FN-NAME
  "Parse a PROTO-NAME protobuf message to a map."
  [^JAVA-CLASS proto]
  (cond-> {}
    REGULAR-FIELDS
    ONEOF-PAYLOAD))

(defn PARSE-FN-NAME-PAYLOAD
  "Parse the oneof payload from a PROTO-NAME."
  [^JAVA-CLASS proto]
  (case (.GET-ONEOF-CASE proto)
    CASE-CLAUSES
    ;; Default case - no payload set
    {}))
(defn BUILD-FN-NAME
  "Build a PROTO-NAME protobuf message from a map."
  [m]
  (let [builder (JAVA-CLASS/newBuilder)]
    REGULAR-FIELDS
    ONEOF-PAYLOAD
    (.build builder)))
(defn PARSE-FN-NAME
  "Parse a PROTO-NAME protobuf message to a map."
  [^JAVA-CLASS proto]
  (cond-> {}
    REGULAR-FIELDS
    ONEOF-PAYLOAD))
(defn BUILD-FN-NAME
  "Build a PROTO-NAME protobuf message from a map."
  [m]
  (let [builder (JAVA-CLASS/newBuilder)]
    REGULAR-FIELDS
    ONEOF-PAYLOAD
    (.build builder)))
(defn PARSE-FN-NAME
  "Parse a PROTO-NAME protobuf message to a map."
  [^JAVA-CLASS proto]
  (cond-> {}
    REGULAR-FIELDS
    ONEOF-PAYLOAD))
(defn BUILD-FN-NAME
  "Build a PROTO-NAME protobuf message from a map."
  [m]
  (let [builder (JAVA-CLASS/newBuilder)]
    REGULAR-FIELDS
    ONEOF-PAYLOAD
    (.build builder)))

(defn BUILD-FN-NAME-PAYLOAD
  "Set the oneof payload field."
  [builder [field-key field-value]]
  (case field-key
    CASE-CLAUSES
    (throw (ex-info "Unknown payload field" {:field field-key}))))
(defn PARSE-FN-NAME
  "Parse a PROTO-NAME protobuf message to a map."
  [^JAVA-CLASS proto]
  (cond-> {}
    REGULAR-FIELDS
    ONEOF-PAYLOAD))

(defn PARSE-FN-NAME-PAYLOAD
  "Parse the oneof payload from a PROTO-NAME."
  [^JAVA-CLASS proto]
  (case (.GET-ONEOF-CASE proto)
    CASE-CLAUSES
    ;; Default case - no payload set
    {}))
(defn BUILD-FN-NAME
  "Build a PROTO-NAME protobuf message from a map."
  [m]
  (let [builder (JAVA-CLASS/newBuilder)]
    REGULAR-FIELDS
    ONEOF-PAYLOAD
    (.build builder)))
(defn PARSE-FN-NAME
  "Parse a PROTO-NAME protobuf message to a map."
  [^JAVA-CLASS proto]
  (cond-> {}
    REGULAR-FIELDS
    ONEOF-PAYLOAD))
(defn BUILD-FN-NAME
  "Build a PROTO-NAME protobuf message from a map."
  [m]
  (let [builder (JAVA-CLASS/newBuilder)]
    REGULAR-FIELDS
    ONEOF-PAYLOAD
    (.build builder)))
(defn PARSE-FN-NAME
  "Parse a PROTO-NAME protobuf message to a map."
  [^JAVA-CLASS proto]
  (cond-> {}
    REGULAR-FIELDS
    ONEOF-PAYLOAD))
(defn BUILD-FN-NAME
  "Build a PROTO-NAME protobuf message from a map."
  [m]
  (let [builder (JAVA-CLASS/newBuilder)]
    REGULAR-FIELDS
    ONEOF-PAYLOAD
    (.build builder)))
(defn PARSE-FN-NAME
  "Parse a PROTO-NAME protobuf message to a map."
  [^JAVA-CLASS proto]
  (cond-> {}
    REGULAR-FIELDS
    ONEOF-PAYLOAD))