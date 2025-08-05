(ns potatoclient.proto.command
  "Generated protobuf conversion functions."
  (:require [clojure.string :as str])
  (:import
    [cmd.Lira JonSharedCmdLira$Root JonSharedCmdLira$Refine_target JonSharedCmdLira$JonGuiDataLiraTarget]
    [cmd.RotaryPlatform JonSharedCmdRotary$Root JonSharedCmdRotary$Axis JonSharedCmdRotary$SetMode JonSharedCmdRotary$SetAzimuthValue JonSharedCmdRotary$RotateAzimuthTo JonSharedCmdRotary$RotateAzimuth JonSharedCmdRotary$RotateElevation JonSharedCmdRotary$SetElevationValue JonSharedCmdRotary$RotateElevationTo JonSharedCmdRotary$RotateElevationRelative JonSharedCmdRotary$RotateElevationRelativeSet JonSharedCmdRotary$RotateAzimuthRelative JonSharedCmdRotary$RotateAzimuthRelativeSet JonSharedCmdRotary$SetPlatformAzimuth JonSharedCmdRotary$SetPlatformElevation JonSharedCmdRotary$SetPlatformBank JonSharedCmdRotary$GetMeteo JonSharedCmdRotary$Azimuth JonSharedCmdRotary$Start JonSharedCmdRotary$Stop JonSharedCmdRotary$Halt JonSharedCmdRotary$ScanStart JonSharedCmdRotary$ScanStop JonSharedCmdRotary$ScanPause JonSharedCmdRotary$ScanUnpause JonSharedCmdRotary$HaltAzimuth JonSharedCmdRotary$HaltElevation JonSharedCmdRotary$ScanPrev JonSharedCmdRotary$ScanNext JonSharedCmdRotary$ScanRefreshNodeList JonSharedCmdRotary$ScanSelectNode JonSharedCmdRotary$ScanDeleteNode JonSharedCmdRotary$ScanUpdateNode JonSharedCmdRotary$ScanAddNode JonSharedCmdRotary$Elevation JonSharedCmdRotary$setUseRotaryAsCompass JonSharedCmdRotary$RotateToGPS JonSharedCmdRotary$SetOriginGPS JonSharedCmdRotary$RotateToNDC]
    [cmd.System JonSharedCmdSystem$Root JonSharedCmdSystem$StartALl JonSharedCmdSystem$StopALl JonSharedCmdSystem$Reboot JonSharedCmdSystem$PowerOff JonSharedCmdSystem$ResetConfigs JonSharedCmdSystem$StartRec JonSharedCmdSystem$StopRec JonSharedCmdSystem$MarkRecImportant JonSharedCmdSystem$UnmarkRecImportant JonSharedCmdSystem$EnterTransport JonSharedCmdSystem$EnableGeodesicMode JonSharedCmdSystem$DisableGeodesicMode JonSharedCmdSystem$SetLocalization]
    [cmd.Lrf_calib JonSharedCmdLrfAlign$Root JonSharedCmdLrfAlign$Offsets JonSharedCmdLrfAlign$SetOffsets JonSharedCmdLrfAlign$ShiftOffsetsBy JonSharedCmdLrfAlign$ResetOffsets JonSharedCmdLrfAlign$SaveOffsets]
    [cmd.Gps JonSharedCmdGps$Root JonSharedCmdGps$Start JonSharedCmdGps$Stop JonSharedCmdGps$GetMeteo JonSharedCmdGps$SetUseManualPosition JonSharedCmdGps$SetManualPosition]
    [cmd.HeatCamera JonSharedCmdHeatCamera$Root JonSharedCmdHeatCamera$SetFxMode JonSharedCmdHeatCamera$SetClaheLevel JonSharedCmdHeatCamera$ShiftClaheLevel JonSharedCmdHeatCamera$NextFxMode JonSharedCmdHeatCamera$PrevFxMode JonSharedCmdHeatCamera$RefreshFxMode JonSharedCmdHeatCamera$EnableDDE JonSharedCmdHeatCamera$DisableDDE JonSharedCmdHeatCamera$SetValue JonSharedCmdHeatCamera$SetDDELevel JonSharedCmdHeatCamera$SetDigitalZoomLevel JonSharedCmdHeatCamera$ShiftDDE JonSharedCmdHeatCamera$ZoomIn JonSharedCmdHeatCamera$ZoomOut JonSharedCmdHeatCamera$ZoomStop JonSharedCmdHeatCamera$FocusIn JonSharedCmdHeatCamera$FocusOut JonSharedCmdHeatCamera$FocusStop JonSharedCmdHeatCamera$FocusStepPlus JonSharedCmdHeatCamera$FocusStepMinus JonSharedCmdHeatCamera$Calibrate JonSharedCmdHeatCamera$Zoom JonSharedCmdHeatCamera$NextZoomTablePos JonSharedCmdHeatCamera$PrevZoomTablePos JonSharedCmdHeatCamera$SetCalibMode JonSharedCmdHeatCamera$SetZoomTableValue JonSharedCmdHeatCamera$SetAGC JonSharedCmdHeatCamera$SetFilters JonSharedCmdHeatCamera$Start JonSharedCmdHeatCamera$Stop JonSharedCmdHeatCamera$Halt JonSharedCmdHeatCamera$Photo JonSharedCmdHeatCamera$GetMeteo JonSharedCmdHeatCamera$SetAutoFocus JonSharedCmdHeatCamera$ResetZoom JonSharedCmdHeatCamera$SaveToTable]
    [cmd.CV JonSharedCmdCv$Root JonSharedCmdCv$VampireModeEnable JonSharedCmdCv$DumpStart JonSharedCmdCv$DumpStop JonSharedCmdCv$VampireModeDisable JonSharedCmdCv$StabilizationModeEnable JonSharedCmdCv$StabilizationModeDisable JonSharedCmdCv$SetAutoFocus JonSharedCmdCv$StartTrackNDC JonSharedCmdCv$StopTrack]
    [cmd.DayCamera JonSharedCmdDayCamera$SetValue JonSharedCmdDayCamera$Move JonSharedCmdDayCamera$Offset JonSharedCmdDayCamera$SetClaheLevel JonSharedCmdDayCamera$ShiftClaheLevel JonSharedCmdDayCamera$Root JonSharedCmdDayCamera$GetPos JonSharedCmdDayCamera$NextFxMode JonSharedCmdDayCamera$PrevFxMode JonSharedCmdDayCamera$RefreshFxMode JonSharedCmdDayCamera$HaltAll JonSharedCmdDayCamera$SetFxMode JonSharedCmdDayCamera$SetDigitalZoomLevel JonSharedCmdDayCamera$Focus JonSharedCmdDayCamera$Zoom JonSharedCmdDayCamera$NextZoomTablePos JonSharedCmdDayCamera$PrevZoomTablePos JonSharedCmdDayCamera$SetIris JonSharedCmdDayCamera$SetInfraRedFilter JonSharedCmdDayCamera$SetAutoIris JonSharedCmdDayCamera$SetZoomTableValue JonSharedCmdDayCamera$Stop JonSharedCmdDayCamera$Start JonSharedCmdDayCamera$Photo JonSharedCmdDayCamera$Halt JonSharedCmdDayCamera$GetMeteo JonSharedCmdDayCamera$ResetZoom JonSharedCmdDayCamera$ResetFocus JonSharedCmdDayCamera$SaveToTable JonSharedCmdDayCamera$SaveToTableFocus]
    [cmd.DayCamGlassHeater JonSharedCmdDayCamGlassHeater$Root JonSharedCmdDayCamGlassHeater$Start JonSharedCmdDayCamGlassHeater$Stop JonSharedCmdDayCamGlassHeater$TurnOn JonSharedCmdDayCamGlassHeater$TurnOff JonSharedCmdDayCamGlassHeater$GetMeteo]
    [cmd JonSharedCmd$Root JonSharedCmd$Ping JonSharedCmd$Noop JonSharedCmd$Frozen]
    [cmd.Lrf JonSharedCmdLrf$Root JonSharedCmdLrf$GetMeteo JonSharedCmdLrf$Start JonSharedCmdLrf$Stop JonSharedCmdLrf$Measure JonSharedCmdLrf$ScanOn JonSharedCmdLrf$ScanOff JonSharedCmdLrf$RefineOff JonSharedCmdLrf$RefineOn JonSharedCmdLrf$TargetDesignatorOff JonSharedCmdLrf$TargetDesignatorOnModeA JonSharedCmdLrf$TargetDesignatorOnModeB JonSharedCmdLrf$EnableFogMode JonSharedCmdLrf$DisableFogMode JonSharedCmdLrf$SetScanMode JonSharedCmdLrf$NewSession]
    [cmd.Compass JonSharedCmdCompass$Root JonSharedCmdCompass$Start JonSharedCmdCompass$Stop JonSharedCmdCompass$Next JonSharedCmdCompass$CalibrateStartLong JonSharedCmdCompass$CalibrateStartShort JonSharedCmdCompass$CalibrateNext JonSharedCmdCompass$CalibrateCencel JonSharedCmdCompass$GetMeteo JonSharedCmdCompass$SetMagneticDeclination JonSharedCmdCompass$SetOffsetAngleAzimuth JonSharedCmdCompass$SetOffsetAngleElevation JonSharedCmdCompass$SetUseRotaryPosition]
    [ser JonSharedDataTypes$JonGuiDataMeteo JonSharedDataTypes$JonGuiDataVideoChannelHeatFilters JonSharedDataTypes$JonGuiDataVideoChannelHeatAGCModes JonSharedDataTypes$JonGuiDataGpsUnits JonSharedDataTypes$JonGuiDataGpsFixType JonSharedDataTypes$JonGuiDataCompassUnits JonSharedDataTypes$JonGuiDataAccumulatorStateIdx JonSharedDataTypes$JonGuiDataTimeFormats JonSharedDataTypes$JonGuiDataRotaryDirection JonSharedDataTypes$JonGuiDataLrfScanModes JonSharedDataTypes$JonGuiDatatLrfLaserPointerModes JonSharedDataTypes$JonGuiDataCompassCalibrateStatus JonSharedDataTypes$JonGuiDataRotaryMode JonSharedDataTypes$JonGuiDataVideoChannel JonSharedDataTypes$JonGuiDataRecOsdScreen JonSharedDataTypes$JonGuiDataFxModeDay JonSharedDataTypes$JonGuiDataFxModeHeat JonSharedDataTypes$JonGuiDataSystemLocalizations JonSharedDataTypes$JonGuiDataClientType]
    [cmd.OSD JonSharedCmdOsd$Root JonSharedCmdOsd$ShowDefaultScreen JonSharedCmdOsd$ShowLRFMeasureScreen JonSharedCmdOsd$ShowLRFResultScreen JonSharedCmdOsd$ShowLRFResultSimplifiedScreen JonSharedCmdOsd$EnableHeatOSD JonSharedCmdOsd$DisableHeatOSD JonSharedCmdOsd$EnableDayOSD JonSharedCmdOsd$DisableDayOSD]))

;; Forward declarations
(declare build-root parse-root build-root-payload parse-root-payload build-start parse-start build-stop parse-stop build-next parse-next build-calibrate-start-long parse-calibrate-start-long build-calibrate-start-short parse-calibrate-start-short build-calibrate-next parse-calibrate-next build-calibrate-cencel parse-calibrate-cencel build-get-meteo parse-get-meteo build-set-magnetic-declination parse-set-magnetic-declination build-set-offset-angle-azimuth parse-set-offset-angle-azimuth build-set-offset-angle-elevation parse-set-offset-angle-elevation build-set-use-rotary-position parse-set-use-rotary-position build-root parse-root build-root-payload parse-root-payload build-start parse-start build-stop parse-stop build-get-meteo parse-get-meteo build-set-use-manual-position parse-set-use-manual-position build-set-manual-position parse-set-manual-position build-jon-gui-data-meteo parse-jon-gui-data-meteo build-root parse-root build-root-payload parse-root-payload build-get-meteo parse-get-meteo build-start parse-start build-stop parse-stop build-measure parse-measure build-scan-on parse-scan-on build-scan-off parse-scan-off build-refine-off parse-refine-off build-refine-on parse-refine-on build-target-designator-off parse-target-designator-off build-target-designator-on-mode-a parse-target-designator-on-mode-a build-target-designator-on-mode-b parse-target-designator-on-mode-b build-enable-fog-mode parse-enable-fog-mode build-disable-fog-mode parse-disable-fog-mode build-set-scan-mode parse-set-scan-mode build-new-session parse-new-session build-set-value parse-set-value build-move parse-move build-offset parse-offset build-set-clahe-level parse-set-clahe-level build-shift-clahe-level parse-shift-clahe-level build-root parse-root build-root-payload parse-root-payload build-get-pos parse-get-pos build-next-fx-mode parse-next-fx-mode build-prev-fx-mode parse-prev-fx-mode build-refresh-fx-mode parse-refresh-fx-mode build-halt-all parse-halt-all build-set-fx-mode parse-set-fx-mode build-set-digital-zoom-level parse-set-digital-zoom-level build-focus parse-focus build-focus-payload parse-focus-payload build-zoom parse-zoom build-zoom-payload parse-zoom-payload build-next-zoom-table-pos parse-next-zoom-table-pos build-prev-zoom-table-pos parse-prev-zoom-table-pos build-set-iris parse-set-iris build-set-infra-red-filter parse-set-infra-red-filter build-set-auto-iris parse-set-auto-iris build-set-zoom-table-value parse-set-zoom-table-value build-stop parse-stop build-start parse-start build-photo parse-photo build-halt parse-halt build-get-meteo parse-get-meteo build-reset-zoom parse-reset-zoom build-reset-focus parse-reset-focus build-save-to-table parse-save-to-table build-save-to-table-focus parse-save-to-table-focus build-root parse-root build-root-payload parse-root-payload build-set-fx-mode parse-set-fx-mode build-set-clahe-level parse-set-clahe-level build-shift-clahe-level parse-shift-clahe-level build-next-fx-mode parse-next-fx-mode build-prev-fx-mode parse-prev-fx-mode build-refresh-fx-mode parse-refresh-fx-mode build-enable-dde parse-enable-dde build-disable-dde parse-disable-dde build-set-value parse-set-value build-set-dde-level parse-set-dde-level build-set-digital-zoom-level parse-set-digital-zoom-level build-shift-dde parse-shift-dde build-zoom-in parse-zoom-in build-zoom-out parse-zoom-out build-zoom-stop parse-zoom-stop build-focus-in parse-focus-in build-focus-out parse-focus-out build-focus-stop parse-focus-stop build-focus-step-plus parse-focus-step-plus build-focus-step-minus parse-focus-step-minus build-calibrate parse-calibrate build-zoom parse-zoom build-zoom-payload parse-zoom-payload build-next-zoom-table-pos parse-next-zoom-table-pos build-prev-zoom-table-pos parse-prev-zoom-table-pos build-set-calib-mode parse-set-calib-mode build-set-zoom-table-value parse-set-zoom-table-value build-set-agc parse-set-agc build-set-filters parse-set-filters build-start parse-start build-stop parse-stop build-halt parse-halt build-photo parse-photo build-get-meteo parse-get-meteo build-set-auto-focus parse-set-auto-focus build-reset-zoom parse-reset-zoom build-save-to-table parse-save-to-table build-root parse-root build-root-payload parse-root-payload build-axis parse-axis build-set-mode parse-set-mode build-set-azimuth-value parse-set-azimuth-value build-rotate-azimuth-to parse-rotate-azimuth-to build-rotate-azimuth parse-rotate-azimuth build-rotate-elevation parse-rotate-elevation build-set-elevation-value parse-set-elevation-value build-rotate-elevation-to parse-rotate-elevation-to build-rotate-elevation-relative parse-rotate-elevation-relative build-rotate-elevation-relative-set parse-rotate-elevation-relative-set build-rotate-azimuth-relative parse-rotate-azimuth-relative build-rotate-azimuth-relative-set parse-rotate-azimuth-relative-set build-set-platform-azimuth parse-set-platform-azimuth build-set-platform-elevation parse-set-platform-elevation build-set-platform-bank parse-set-platform-bank build-get-meteo parse-get-meteo build-azimuth parse-azimuth build-azimuth-payload parse-azimuth-payload build-start parse-start build-stop parse-stop build-halt parse-halt build-scan-start parse-scan-start build-scan-stop parse-scan-stop build-scan-pause parse-scan-pause build-scan-unpause parse-scan-unpause build-halt-azimuth parse-halt-azimuth build-halt-elevation parse-halt-elevation build-scan-prev parse-scan-prev build-scan-next parse-scan-next build-scan-refresh-node-list parse-scan-refresh-node-list build-scan-select-node parse-scan-select-node build-scan-delete-node parse-scan-delete-node build-scan-update-node parse-scan-update-node build-scan-add-node parse-scan-add-node build-elevation parse-elevation build-elevation-payload parse-elevation-payload build-set-use-rotary-as-compass parse-set-use-rotary-as-compass build-rotate-to-gps parse-rotate-to-gps build-set-origin-gps parse-set-origin-gps build-rotate-to-ndc parse-rotate-to-ndc build-root parse-root build-root-payload parse-root-payload build-show-default-screen parse-show-default-screen build-show-lrf-measure-screen parse-show-lrf-measure-screen build-show-lrf-result-screen parse-show-lrf-result-screen build-show-lrf-result-simplified-screen parse-show-lrf-result-simplified-screen build-enable-heat-osd parse-enable-heat-osd build-disable-heat-osd parse-disable-heat-osd build-enable-day-osd parse-enable-day-osd build-disable-day-osd parse-disable-day-osd build-root parse-root build-root-payload parse-root-payload build-offsets parse-offsets build-offsets-payload parse-offsets-payload build-set-offsets parse-set-offsets build-shift-offsets-by parse-shift-offsets-by build-reset-offsets parse-reset-offsets build-save-offsets parse-save-offsets build-root parse-root build-root-payload parse-root-payload build-start-a-ll parse-start-a-ll build-stop-a-ll parse-stop-a-ll build-reboot parse-reboot build-power-off parse-power-off build-reset-configs parse-reset-configs build-start-rec parse-start-rec build-stop-rec parse-stop-rec build-mark-rec-important parse-mark-rec-important build-unmark-rec-important parse-unmark-rec-important build-enter-transport parse-enter-transport build-enable-geodesic-mode parse-enable-geodesic-mode build-disable-geodesic-mode parse-disable-geodesic-mode build-set-localization parse-set-localization build-root parse-root build-root-payload parse-root-payload build-vampire-mode-enable parse-vampire-mode-enable build-dump-start parse-dump-start build-dump-stop parse-dump-stop build-vampire-mode-disable parse-vampire-mode-disable build-stabilization-mode-enable parse-stabilization-mode-enable build-stabilization-mode-disable parse-stabilization-mode-disable build-set-auto-focus parse-set-auto-focus build-start-track-ndc parse-start-track-ndc build-stop-track parse-stop-track build-root parse-root build-root-payload parse-root-payload build-start parse-start build-stop parse-stop build-turn-on parse-turn-on build-turn-off parse-turn-off build-get-meteo parse-get-meteo build-root parse-root build-root-payload parse-root-payload build-refine-target parse-refine-target build-jon-gui-data-lira-target parse-jon-gui-data-lira-target build-root parse-root build-root-payload parse-root-payload build-ping parse-ping build-noop parse-noop build-frozen parse-frozen)

;; Message Converters
(defn build-root
  "Build a Root protobuf message from a map."
  [m]
  (let [builder (cmd.Compass.JonSharedCmdCompass$Root/newBuilder)]
    ;; Set oneof payload
    (when-let [payload (first (filter (fn [[k v]] (#{:start :stop :set-magnetic-declination :set-offset-angle-azimuth :set-offset-angle-elevation :set-use-rotary-position :start-calibrate-long :start-calibrate-short :calibrate-next :calibrate-cencel :get-meteo} k)) m))]
      (build-root-payload builder payload))
    (.build builder)))


(defn build-root-payload
  "Set the oneof payload field."
  [builder [field-key field-value]]
  (case field-key
    :start (.setStart builder (build-start field-value))
    :stop (.setStop builder (build-stop field-value))
    :set-magnetic-declination (.setSetMagneticDeclination builder (build-set-magnetic-declination field-value))
    :set-offset-angle-azimuth (.setSetOffsetAngleAzimuth builder (build-set-offset-angle-azimuth field-value))
    :set-offset-angle-elevation (.setSetOffsetAngleElevation builder (build-set-offset-angle-elevation field-value))
    :set-use-rotary-position (.setSetUseRotaryPosition builder (build-set-use-rotary-position field-value))
    :start-calibrate-long (.setStartCalibrateLong builder (build-calibrate-start-long field-value))
    :start-calibrate-short (.setStartCalibrateShort builder (build-calibrate-start-short field-value))
    :calibrate-next (.setCalibrateNext builder (build-calibrate-next field-value))
    :calibrate-cencel (.setCalibrateCencel builder (build-calibrate-cencel field-value))
    :get-meteo (.setGetMeteo builder (build-get-meteo field-value))
    (throw (ex-info "Unknown payload field" {:field field-key}))))

(defn parse-root
  "Parse a Root protobuf message to a map."
  [^cmd.Compass.JonSharedCmdCompass$Root proto]
  (merge
    {}
    (parse-root-payload proto)))


(defn parse-root-payload
  "Parse the oneof payload."
  [proto]
  (case (.getPayloadCase proto)
    START {:start (parse-start (.getStart proto))}
    STOP {:stop (parse-stop (.getStop proto))}
    SET_MAGNETIC_DECLINATION {:set-magnetic-declination (parse-set-magnetic-declination (.getSetMagneticDeclination proto))}
    SET_OFFSET_ANGLE_AZIMUTH {:set-offset-angle-azimuth (parse-set-offset-angle-azimuth (.getSetOffsetAngleAzimuth proto))}
    SET_OFFSET_ANGLE_ELEVATION {:set-offset-angle-elevation (parse-set-offset-angle-elevation (.getSetOffsetAngleElevation proto))}
    SET_USE_ROTARY_POSITION {:set-use-rotary-position (parse-set-use-rotary-position (.getSetUseRotaryPosition proto))}
    START_CALIBRATE_LONG {:start-calibrate-long (parse-calibrate-start-long (.getStartCalibrateLong proto))}
    START_CALIBRATE_SHORT {:start-calibrate-short (parse-calibrate-start-short (.getStartCalibrateShort proto))}
    CALIBRATE_NEXT {:calibrate-next (parse-calibrate-next (.getCalibrateNext proto))}
    CALIBRATE_CENCEL {:calibrate-cencel (parse-calibrate-cencel (.getCalibrateCencel proto))}
    GET_METEO {:get-meteo (parse-get-meteo (.getGetMeteo proto))}
    {}))

(defn build-start
  "Build a Start protobuf message from a map."
  [m]
  (let [builder (cmd.Compass.JonSharedCmdCompass$Start/newBuilder)]
    (.build builder)))

(defn parse-start
  "Parse a Start protobuf message to a map."
  [^cmd.Compass.JonSharedCmdCompass$Start proto]
  (merge
    {}
))

(defn build-stop
  "Build a Stop protobuf message from a map."
  [m]
  (let [builder (cmd.Compass.JonSharedCmdCompass$Stop/newBuilder)]
    (.build builder)))

(defn parse-stop
  "Parse a Stop protobuf message to a map."
  [^cmd.Compass.JonSharedCmdCompass$Stop proto]
  (merge
    {}
))

(defn build-next
  "Build a Next protobuf message from a map."
  [m]
  (let [builder (cmd.Compass.JonSharedCmdCompass$Next/newBuilder)]
    (.build builder)))

(defn parse-next
  "Parse a Next protobuf message to a map."
  [^cmd.Compass.JonSharedCmdCompass$Next proto]
  (merge
    {}
))

(defn build-calibrate-start-long
  "Build a CalibrateStartLong protobuf message from a map."
  [m]
  (let [builder (cmd.Compass.JonSharedCmdCompass$CalibrateStartLong/newBuilder)]
    (.build builder)))

(defn parse-calibrate-start-long
  "Parse a CalibrateStartLong protobuf message to a map."
  [^cmd.Compass.JonSharedCmdCompass$CalibrateStartLong proto]
  (merge
    {}
))

(defn build-calibrate-start-short
  "Build a CalibrateStartShort protobuf message from a map."
  [m]
  (let [builder (cmd.Compass.JonSharedCmdCompass$CalibrateStartShort/newBuilder)]
    (.build builder)))

(defn parse-calibrate-start-short
  "Parse a CalibrateStartShort protobuf message to a map."
  [^cmd.Compass.JonSharedCmdCompass$CalibrateStartShort proto]
  (merge
    {}
))

(defn build-calibrate-next
  "Build a CalibrateNext protobuf message from a map."
  [m]
  (let [builder (cmd.Compass.JonSharedCmdCompass$CalibrateNext/newBuilder)]
    (.build builder)))

(defn parse-calibrate-next
  "Parse a CalibrateNext protobuf message to a map."
  [^cmd.Compass.JonSharedCmdCompass$CalibrateNext proto]
  (merge
    {}
))

(defn build-calibrate-cencel
  "Build a CalibrateCencel protobuf message from a map."
  [m]
  (let [builder (cmd.Compass.JonSharedCmdCompass$CalibrateCencel/newBuilder)]
    (.build builder)))

(defn parse-calibrate-cencel
  "Parse a CalibrateCencel protobuf message to a map."
  [^cmd.Compass.JonSharedCmdCompass$CalibrateCencel proto]
  (merge
    {}
))

(defn build-get-meteo
  "Build a GetMeteo protobuf message from a map."
  [m]
  (let [builder (cmd.Compass.JonSharedCmdCompass$GetMeteo/newBuilder)]
    (.build builder)))

(defn parse-get-meteo
  "Parse a GetMeteo protobuf message to a map."
  [^cmd.Compass.JonSharedCmdCompass$GetMeteo proto]
  (merge
    {}
))

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
  (merge
    {:value (.getValue proto)}
))

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
  (merge
    {:value (.getValue proto)}
))

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
  (merge
    {:value (.getValue proto)}
))

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
  (merge
    {:flag (.getFlag proto)}
))

(defn build-root
  "Build a Root protobuf message from a map."
  [m]
  (let [builder (cmd.Gps.JonSharedCmdGps$Root/newBuilder)]
    ;; Set oneof payload
    (when-let [payload (first (filter (fn [[k v]] (#{:start :stop :set-manual-position :set-use-manual-position :get-meteo} k)) m))]
      (build-root-payload builder payload))
    (.build builder)))


(defn build-root-payload
  "Set the oneof payload field."
  [builder [field-key field-value]]
  (case field-key
    :start (.setStart builder (build-start field-value))
    :stop (.setStop builder (build-stop field-value))
    :set-manual-position (.setSetManualPosition builder (build-set-manual-position field-value))
    :set-use-manual-position (.setSetUseManualPosition builder (build-set-use-manual-position field-value))
    :get-meteo (.setGetMeteo builder (build-get-meteo field-value))
    (throw (ex-info "Unknown payload field" {:field field-key}))))

(defn parse-root
  "Parse a Root protobuf message to a map."
  [^cmd.Gps.JonSharedCmdGps$Root proto]
  (merge
    {}
    (parse-root-payload proto)))


(defn parse-root-payload
  "Parse the oneof payload."
  [proto]
  (case (.getPayloadCase proto)
    START {:start (parse-start (.getStart proto))}
    STOP {:stop (parse-stop (.getStop proto))}
    SET_MANUAL_POSITION {:set-manual-position (parse-set-manual-position (.getSetManualPosition proto))}
    SET_USE_MANUAL_POSITION {:set-use-manual-position (parse-set-use-manual-position (.getSetUseManualPosition proto))}
    GET_METEO {:get-meteo (parse-get-meteo (.getGetMeteo proto))}
    {}))

(defn build-start
  "Build a Start protobuf message from a map."
  [m]
  (let [builder (cmd.Gps.JonSharedCmdGps$Start/newBuilder)]
    (.build builder)))

(defn parse-start
  "Parse a Start protobuf message to a map."
  [^cmd.Gps.JonSharedCmdGps$Start proto]
  (merge
    {}
))

(defn build-stop
  "Build a Stop protobuf message from a map."
  [m]
  (let [builder (cmd.Gps.JonSharedCmdGps$Stop/newBuilder)]
    (.build builder)))

(defn parse-stop
  "Parse a Stop protobuf message to a map."
  [^cmd.Gps.JonSharedCmdGps$Stop proto]
  (merge
    {}
))

(defn build-get-meteo
  "Build a GetMeteo protobuf message from a map."
  [m]
  (let [builder (cmd.Gps.JonSharedCmdGps$GetMeteo/newBuilder)]
    (.build builder)))

(defn parse-get-meteo
  "Parse a GetMeteo protobuf message to a map."
  [^cmd.Gps.JonSharedCmdGps$GetMeteo proto]
  (merge
    {}
))

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
  (merge
    {:flag (.getFlag proto)}
))

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
  (merge
    {:latitude (.getLatitude proto)
     :longitude (.getLongitude proto)
     :altitude (.getAltitude proto)}
))

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
  (merge
    {:temperature (.getTemperature proto)
     :humidity (.getHumidity proto)
     :pressure (.getPressure proto)}
))

(defn build-root
  "Build a Root protobuf message from a map."
  [m]
  (let [builder (cmd.Lrf.JonSharedCmdLrf$Root/newBuilder)]
    ;; Set oneof payload
    (when-let [payload (first (filter (fn [[k v]] (#{:measure :scan-on :scan-off :start :stop :target-designator-off :target-designator-on-mode-a :target-designator-on-mode-b :enable-fog-mode :disable-fog-mode :set-scan-mode :new-session :get-meteo :refine-on :refine-off} k)) m))]
      (build-root-payload builder payload))
    (.build builder)))


(defn build-root-payload
  "Set the oneof payload field."
  [builder [field-key field-value]]
  (case field-key
    :measure (.setMeasure builder (build-measure field-value))
    :scan-on (.setScanOn builder (build-scan-on field-value))
    :scan-off (.setScanOff builder (build-scan-off field-value))
    :start (.setStart builder (build-start field-value))
    :stop (.setStop builder (build-stop field-value))
    :target-designator-off (.setTargetDesignatorOff builder (build-target-designator-off field-value))
    :target-designator-on-mode-a (.setTargetDesignatorOnModeA builder (build-target-designator-on-mode-a field-value))
    :target-designator-on-mode-b (.setTargetDesignatorOnModeB builder (build-target-designator-on-mode-b field-value))
    :enable-fog-mode (.setEnableFogMode builder (build-enable-fog-mode field-value))
    :disable-fog-mode (.setDisableFogMode builder (build-disable-fog-mode field-value))
    :set-scan-mode (.setSetScanMode builder (build-set-scan-mode field-value))
    :new-session (.setNewSession builder (build-new-session field-value))
    :get-meteo (.setGetMeteo builder (build-get-meteo field-value))
    :refine-on (.setRefineOn builder (build-refine-on field-value))
    :refine-off (.setRefineOff builder (build-refine-off field-value))
    (throw (ex-info "Unknown payload field" {:field field-key}))))

(defn parse-root
  "Parse a Root protobuf message to a map."
  [^cmd.Lrf.JonSharedCmdLrf$Root proto]
  (merge
    {}
    (parse-root-payload proto)))


(defn parse-root-payload
  "Parse the oneof payload."
  [proto]
  (case (.getPayloadCase proto)
    MEASURE {:measure (parse-measure (.getMeasure proto))}
    SCAN_ON {:scan-on (parse-scan-on (.getScanOn proto))}
    SCAN_OFF {:scan-off (parse-scan-off (.getScanOff proto))}
    START {:start (parse-start (.getStart proto))}
    STOP {:stop (parse-stop (.getStop proto))}
    TARGET_DESIGNATOR_OFF {:target-designator-off (parse-target-designator-off (.getTargetDesignatorOff proto))}
    TARGET_DESIGNATOR_ON_MODE_A {:target-designator-on-mode-a (parse-target-designator-on-mode-a (.getTargetDesignatorOnModeA proto))}
    TARGET_DESIGNATOR_ON_MODE_B {:target-designator-on-mode-b (parse-target-designator-on-mode-b (.getTargetDesignatorOnModeB proto))}
    ENABLE_FOG_MODE {:enable-fog-mode (parse-enable-fog-mode (.getEnableFogMode proto))}
    DISABLE_FOG_MODE {:disable-fog-mode (parse-disable-fog-mode (.getDisableFogMode proto))}
    SET_SCAN_MODE {:set-scan-mode (parse-set-scan-mode (.getSetScanMode proto))}
    NEW_SESSION {:new-session (parse-new-session (.getNewSession proto))}
    GET_METEO {:get-meteo (parse-get-meteo (.getGetMeteo proto))}
    REFINE_ON {:refine-on (parse-refine-on (.getRefineOn proto))}
    REFINE_OFF {:refine-off (parse-refine-off (.getRefineOff proto))}
    {}))

(defn build-get-meteo
  "Build a GetMeteo protobuf message from a map."
  [m]
  (let [builder (cmd.Lrf.JonSharedCmdLrf$GetMeteo/newBuilder)]
    (.build builder)))

(defn parse-get-meteo
  "Parse a GetMeteo protobuf message to a map."
  [^cmd.Lrf.JonSharedCmdLrf$GetMeteo proto]
  (merge
    {}
))

(defn build-start
  "Build a Start protobuf message from a map."
  [m]
  (let [builder (cmd.Lrf.JonSharedCmdLrf$Start/newBuilder)]
    (.build builder)))

(defn parse-start
  "Parse a Start protobuf message to a map."
  [^cmd.Lrf.JonSharedCmdLrf$Start proto]
  (merge
    {}
))

(defn build-stop
  "Build a Stop protobuf message from a map."
  [m]
  (let [builder (cmd.Lrf.JonSharedCmdLrf$Stop/newBuilder)]
    (.build builder)))

(defn parse-stop
  "Parse a Stop protobuf message to a map."
  [^cmd.Lrf.JonSharedCmdLrf$Stop proto]
  (merge
    {}
))

(defn build-measure
  "Build a Measure protobuf message from a map."
  [m]
  (let [builder (cmd.Lrf.JonSharedCmdLrf$Measure/newBuilder)]
    (.build builder)))

(defn parse-measure
  "Parse a Measure protobuf message to a map."
  [^cmd.Lrf.JonSharedCmdLrf$Measure proto]
  (merge
    {}
))

(defn build-scan-on
  "Build a ScanOn protobuf message from a map."
  [m]
  (let [builder (cmd.Lrf.JonSharedCmdLrf$ScanOn/newBuilder)]
    (.build builder)))

(defn parse-scan-on
  "Parse a ScanOn protobuf message to a map."
  [^cmd.Lrf.JonSharedCmdLrf$ScanOn proto]
  (merge
    {}
))

(defn build-scan-off
  "Build a ScanOff protobuf message from a map."
  [m]
  (let [builder (cmd.Lrf.JonSharedCmdLrf$ScanOff/newBuilder)]
    (.build builder)))

(defn parse-scan-off
  "Parse a ScanOff protobuf message to a map."
  [^cmd.Lrf.JonSharedCmdLrf$ScanOff proto]
  (merge
    {}
))

(defn build-refine-off
  "Build a RefineOff protobuf message from a map."
  [m]
  (let [builder (cmd.Lrf.JonSharedCmdLrf$RefineOff/newBuilder)]
    (.build builder)))

(defn parse-refine-off
  "Parse a RefineOff protobuf message to a map."
  [^cmd.Lrf.JonSharedCmdLrf$RefineOff proto]
  (merge
    {}
))

(defn build-refine-on
  "Build a RefineOn protobuf message from a map."
  [m]
  (let [builder (cmd.Lrf.JonSharedCmdLrf$RefineOn/newBuilder)]
    (.build builder)))

(defn parse-refine-on
  "Parse a RefineOn protobuf message to a map."
  [^cmd.Lrf.JonSharedCmdLrf$RefineOn proto]
  (merge
    {}
))

(defn build-target-designator-off
  "Build a TargetDesignatorOff protobuf message from a map."
  [m]
  (let [builder (cmd.Lrf.JonSharedCmdLrf$TargetDesignatorOff/newBuilder)]
    (.build builder)))

(defn parse-target-designator-off
  "Parse a TargetDesignatorOff protobuf message to a map."
  [^cmd.Lrf.JonSharedCmdLrf$TargetDesignatorOff proto]
  (merge
    {}
))

(defn build-target-designator-on-mode-a
  "Build a TargetDesignatorOnModeA protobuf message from a map."
  [m]
  (let [builder (cmd.Lrf.JonSharedCmdLrf$TargetDesignatorOnModeA/newBuilder)]
    (.build builder)))

(defn parse-target-designator-on-mode-a
  "Parse a TargetDesignatorOnModeA protobuf message to a map."
  [^cmd.Lrf.JonSharedCmdLrf$TargetDesignatorOnModeA proto]
  (merge
    {}
))

(defn build-target-designator-on-mode-b
  "Build a TargetDesignatorOnModeB protobuf message from a map."
  [m]
  (let [builder (cmd.Lrf.JonSharedCmdLrf$TargetDesignatorOnModeB/newBuilder)]
    (.build builder)))

(defn parse-target-designator-on-mode-b
  "Parse a TargetDesignatorOnModeB protobuf message to a map."
  [^cmd.Lrf.JonSharedCmdLrf$TargetDesignatorOnModeB proto]
  (merge
    {}
))

(defn build-enable-fog-mode
  "Build a EnableFogMode protobuf message from a map."
  [m]
  (let [builder (cmd.Lrf.JonSharedCmdLrf$EnableFogMode/newBuilder)]
    (.build builder)))

(defn parse-enable-fog-mode
  "Parse a EnableFogMode protobuf message to a map."
  [^cmd.Lrf.JonSharedCmdLrf$EnableFogMode proto]
  (merge
    {}
))

(defn build-disable-fog-mode
  "Build a DisableFogMode protobuf message from a map."
  [m]
  (let [builder (cmd.Lrf.JonSharedCmdLrf$DisableFogMode/newBuilder)]
    (.build builder)))

(defn parse-disable-fog-mode
  "Parse a DisableFogMode protobuf message to a map."
  [^cmd.Lrf.JonSharedCmdLrf$DisableFogMode proto]
  (merge
    {}
))

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
  (merge
    {:mode (.getMode proto)}
))

(defn build-new-session
  "Build a NewSession protobuf message from a map."
  [m]
  (let [builder (cmd.Lrf.JonSharedCmdLrf$NewSession/newBuilder)]
    (.build builder)))

(defn parse-new-session
  "Parse a NewSession protobuf message to a map."
  [^cmd.Lrf.JonSharedCmdLrf$NewSession proto]
  (merge
    {}
))

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
  (merge
    {:value (.getValue proto)}
))

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
  (merge
    {:target-value (.getTargetValue proto)
     :speed (.getSpeed proto)}
))

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
  (merge
    {:offset-value (.getOffsetValue proto)}
))

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
  (merge
    {:value (.getValue proto)}
))

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
  (merge
    {:value (.getValue proto)}
))

(defn build-root
  "Build a Root protobuf message from a map."
  [m]
  (let [builder (cmd.DayCamera.JonSharedCmdDayCamera$Root/newBuilder)]
    ;; Set oneof payload
    (when-let [payload (first (filter (fn [[k v]] (#{:focus :zoom :set-iris :set-infra-red-filter :start :stop :photo :set-auto-iris :halt-all :set-fx-mode :next-fx-mode :prev-fx-mode :get-meteo :refresh-fx-mode :set-digital-zoom-level :set-clahe-level :shift-clahe-level} k)) m))]
      (build-root-payload builder payload))
    (.build builder)))


(defn build-root-payload
  "Set the oneof payload field."
  [builder [field-key field-value]]
  (case field-key
    :focus (.setFocus builder (build-focus field-value))
    :zoom (.setZoom builder (build-zoom field-value))
    :set-iris (.setSetIris builder (build-set-iris field-value))
    :set-infra-red-filter (.setSetInfraRedFilter builder (build-set-infra-red-filter field-value))
    :start (.setStart builder (build-start field-value))
    :stop (.setStop builder (build-stop field-value))
    :photo (.setPhoto builder (build-photo field-value))
    :set-auto-iris (.setSetAutoIris builder (build-set-auto-iris field-value))
    :halt-all (.setHaltAll builder (build-halt-all field-value))
    :set-fx-mode (.setSetFxMode builder (build-set-fx-mode field-value))
    :next-fx-mode (.setNextFxMode builder (build-next-fx-mode field-value))
    :prev-fx-mode (.setPrevFxMode builder (build-prev-fx-mode field-value))
    :get-meteo (.setGetMeteo builder (build-get-meteo field-value))
    :refresh-fx-mode (.setRefreshFxMode builder (build-refresh-fx-mode field-value))
    :set-digital-zoom-level (.setSetDigitalZoomLevel builder (build-set-digital-zoom-level field-value))
    :set-clahe-level (.setSetClaheLevel builder (build-set-clahe-level field-value))
    :shift-clahe-level (.setShiftClaheLevel builder (build-shift-clahe-level field-value))
    (throw (ex-info "Unknown payload field" {:field field-key}))))

(defn parse-root
  "Parse a Root protobuf message to a map."
  [^cmd.DayCamera.JonSharedCmdDayCamera$Root proto]
  (merge
    {}
    (parse-root-payload proto)))


(defn parse-root-payload
  "Parse the oneof payload."
  [proto]
  (case (.getPayloadCase proto)
    FOCUS {:focus (parse-focus (.getFocus proto))}
    ZOOM {:zoom (parse-zoom (.getZoom proto))}
    SET_IRIS {:set-iris (parse-set-iris (.getSetIris proto))}
    SET_INFRA_RED_FILTER {:set-infra-red-filter (parse-set-infra-red-filter (.getSetInfraRedFilter proto))}
    START {:start (parse-start (.getStart proto))}
    STOP {:stop (parse-stop (.getStop proto))}
    PHOTO {:photo (parse-photo (.getPhoto proto))}
    SET_AUTO_IRIS {:set-auto-iris (parse-set-auto-iris (.getSetAutoIris proto))}
    HALT_ALL {:halt-all (parse-halt-all (.getHaltAll proto))}
    SET_FX_MODE {:set-fx-mode (parse-set-fx-mode (.getSetFxMode proto))}
    NEXT_FX_MODE {:next-fx-mode (parse-next-fx-mode (.getNextFxMode proto))}
    PREV_FX_MODE {:prev-fx-mode (parse-prev-fx-mode (.getPrevFxMode proto))}
    GET_METEO {:get-meteo (parse-get-meteo (.getGetMeteo proto))}
    REFRESH_FX_MODE {:refresh-fx-mode (parse-refresh-fx-mode (.getRefreshFxMode proto))}
    SET_DIGITAL_ZOOM_LEVEL {:set-digital-zoom-level (parse-set-digital-zoom-level (.getSetDigitalZoomLevel proto))}
    SET_CLAHE_LEVEL {:set-clahe-level (parse-set-clahe-level (.getSetClaheLevel proto))}
    SHIFT_CLAHE_LEVEL {:shift-clahe-level (parse-shift-clahe-level (.getShiftClaheLevel proto))}
    {}))

(defn build-get-pos
  "Build a GetPos protobuf message from a map."
  [m]
  (let [builder (cmd.DayCamera.JonSharedCmdDayCamera$GetPos/newBuilder)]
    (.build builder)))

(defn parse-get-pos
  "Parse a GetPos protobuf message to a map."
  [^cmd.DayCamera.JonSharedCmdDayCamera$GetPos proto]
  (merge
    {}
))

(defn build-next-fx-mode
  "Build a NextFxMode protobuf message from a map."
  [m]
  (let [builder (cmd.DayCamera.JonSharedCmdDayCamera$NextFxMode/newBuilder)]
    (.build builder)))

(defn parse-next-fx-mode
  "Parse a NextFxMode protobuf message to a map."
  [^cmd.DayCamera.JonSharedCmdDayCamera$NextFxMode proto]
  (merge
    {}
))

(defn build-prev-fx-mode
  "Build a PrevFxMode protobuf message from a map."
  [m]
  (let [builder (cmd.DayCamera.JonSharedCmdDayCamera$PrevFxMode/newBuilder)]
    (.build builder)))

(defn parse-prev-fx-mode
  "Parse a PrevFxMode protobuf message to a map."
  [^cmd.DayCamera.JonSharedCmdDayCamera$PrevFxMode proto]
  (merge
    {}
))

(defn build-refresh-fx-mode
  "Build a RefreshFxMode protobuf message from a map."
  [m]
  (let [builder (cmd.DayCamera.JonSharedCmdDayCamera$RefreshFxMode/newBuilder)]
    (.build builder)))

(defn parse-refresh-fx-mode
  "Parse a RefreshFxMode protobuf message to a map."
  [^cmd.DayCamera.JonSharedCmdDayCamera$RefreshFxMode proto]
  (merge
    {}
))

(defn build-halt-all
  "Build a HaltAll protobuf message from a map."
  [m]
  (let [builder (cmd.DayCamera.JonSharedCmdDayCamera$HaltAll/newBuilder)]
    (.build builder)))

(defn parse-halt-all
  "Parse a HaltAll protobuf message to a map."
  [^cmd.DayCamera.JonSharedCmdDayCamera$HaltAll proto]
  (merge
    {}
))

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
  (merge
    {:mode (.getMode proto)}
))

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
  (merge
    {:value (.getValue proto)}
))

(defn build-focus
  "Build a Focus protobuf message from a map."
  [m]
  (let [builder (cmd.DayCamera.JonSharedCmdDayCamera$Focus/newBuilder)]
    ;; Set oneof payload
    (when-let [payload (first (filter (fn [[k v]] (#{:set-value :move :halt :offset :reset-focus :save-to-table-focus} k)) m))]
      (build-focus-payload builder payload))
    (.build builder)))


(defn build-focus-payload
  "Set the oneof payload field."
  [builder [field-key field-value]]
  (case field-key
    :set-value (.setSetValue builder (build-set-value field-value))
    :move (.setMove builder (build-move field-value))
    :halt (.setHalt builder (build-halt field-value))
    :offset (.setOffset builder (build-offset field-value))
    :reset-focus (.setResetFocus builder (build-reset-focus field-value))
    :save-to-table-focus (.setSaveToTableFocus builder (build-save-to-table-focus field-value))
    (throw (ex-info "Unknown payload field" {:field field-key}))))

(defn parse-focus
  "Parse a Focus protobuf message to a map."
  [^cmd.DayCamera.JonSharedCmdDayCamera$Focus proto]
  (merge
    {}
    (parse-focus-payload proto)))


(defn parse-focus-payload
  "Parse the oneof payload."
  [proto]
  (case (.getPayloadCase proto)
    SET_VALUE {:set-value (parse-set-value (.getSetValue proto))}
    MOVE {:move (parse-move (.getMove proto))}
    HALT {:halt (parse-halt (.getHalt proto))}
    OFFSET {:offset (parse-offset (.getOffset proto))}
    RESET_FOCUS {:reset-focus (parse-reset-focus (.getResetFocus proto))}
    SAVE_TO_TABLE_FOCUS {:save-to-table-focus (parse-save-to-table-focus (.getSaveToTableFocus proto))}
    {}))

(defn build-zoom
  "Build a Zoom protobuf message from a map."
  [m]
  (let [builder (cmd.DayCamera.JonSharedCmdDayCamera$Zoom/newBuilder)]
    ;; Set oneof payload
    (when-let [payload (first (filter (fn [[k v]] (#{:set-value :move :halt :set-zoom-table-value :next-zoom-table-pos :prev-zoom-table-pos :offset :reset-zoom :save-to-table} k)) m))]
      (build-zoom-payload builder payload))
    (.build builder)))


(defn build-zoom-payload
  "Set the oneof payload field."
  [builder [field-key field-value]]
  (case field-key
    :set-value (.setSetValue builder (build-set-value field-value))
    :move (.setMove builder (build-move field-value))
    :halt (.setHalt builder (build-halt field-value))
    :set-zoom-table-value (.setSetZoomTableValue builder (build-set-zoom-table-value field-value))
    :next-zoom-table-pos (.setNextZoomTablePos builder (build-next-zoom-table-pos field-value))
    :prev-zoom-table-pos (.setPrevZoomTablePos builder (build-prev-zoom-table-pos field-value))
    :offset (.setOffset builder (build-offset field-value))
    :reset-zoom (.setResetZoom builder (build-reset-zoom field-value))
    :save-to-table (.setSaveToTable builder (build-save-to-table field-value))
    (throw (ex-info "Unknown payload field" {:field field-key}))))

(defn parse-zoom
  "Parse a Zoom protobuf message to a map."
  [^cmd.DayCamera.JonSharedCmdDayCamera$Zoom proto]
  (merge
    {}
    (parse-zoom-payload proto)))


(defn parse-zoom-payload
  "Parse the oneof payload."
  [proto]
  (case (.getPayloadCase proto)
    SET_VALUE {:set-value (parse-set-value (.getSetValue proto))}
    MOVE {:move (parse-move (.getMove proto))}
    HALT {:halt (parse-halt (.getHalt proto))}
    SET_ZOOM_TABLE_VALUE {:set-zoom-table-value (parse-set-zoom-table-value (.getSetZoomTableValue proto))}
    NEXT_ZOOM_TABLE_POS {:next-zoom-table-pos (parse-next-zoom-table-pos (.getNextZoomTablePos proto))}
    PREV_ZOOM_TABLE_POS {:prev-zoom-table-pos (parse-prev-zoom-table-pos (.getPrevZoomTablePos proto))}
    OFFSET {:offset (parse-offset (.getOffset proto))}
    RESET_ZOOM {:reset-zoom (parse-reset-zoom (.getResetZoom proto))}
    SAVE_TO_TABLE {:save-to-table (parse-save-to-table (.getSaveToTable proto))}
    {}))

(defn build-next-zoom-table-pos
  "Build a NextZoomTablePos protobuf message from a map."
  [m]
  (let [builder (cmd.DayCamera.JonSharedCmdDayCamera$NextZoomTablePos/newBuilder)]
    (.build builder)))

(defn parse-next-zoom-table-pos
  "Parse a NextZoomTablePos protobuf message to a map."
  [^cmd.DayCamera.JonSharedCmdDayCamera$NextZoomTablePos proto]
  (merge
    {}
))

(defn build-prev-zoom-table-pos
  "Build a PrevZoomTablePos protobuf message from a map."
  [m]
  (let [builder (cmd.DayCamera.JonSharedCmdDayCamera$PrevZoomTablePos/newBuilder)]
    (.build builder)))

(defn parse-prev-zoom-table-pos
  "Parse a PrevZoomTablePos protobuf message to a map."
  [^cmd.DayCamera.JonSharedCmdDayCamera$PrevZoomTablePos proto]
  (merge
    {}
))

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
  (merge
    {:value (.getValue proto)}
))

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
  (merge
    {:value (.getValue proto)}
))

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
  (merge
    {:value (.getValue proto)}
))

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
  (merge
    {:value (.getValue proto)}
))

(defn build-stop
  "Build a Stop protobuf message from a map."
  [m]
  (let [builder (cmd.DayCamera.JonSharedCmdDayCamera$Stop/newBuilder)]
    (.build builder)))

(defn parse-stop
  "Parse a Stop protobuf message to a map."
  [^cmd.DayCamera.JonSharedCmdDayCamera$Stop proto]
  (merge
    {}
))

(defn build-start
  "Build a Start protobuf message from a map."
  [m]
  (let [builder (cmd.DayCamera.JonSharedCmdDayCamera$Start/newBuilder)]
    (.build builder)))

(defn parse-start
  "Parse a Start protobuf message to a map."
  [^cmd.DayCamera.JonSharedCmdDayCamera$Start proto]
  (merge
    {}
))

(defn build-photo
  "Build a Photo protobuf message from a map."
  [m]
  (let [builder (cmd.DayCamera.JonSharedCmdDayCamera$Photo/newBuilder)]
    (.build builder)))

(defn parse-photo
  "Parse a Photo protobuf message to a map."
  [^cmd.DayCamera.JonSharedCmdDayCamera$Photo proto]
  (merge
    {}
))

(defn build-halt
  "Build a Halt protobuf message from a map."
  [m]
  (let [builder (cmd.DayCamera.JonSharedCmdDayCamera$Halt/newBuilder)]
    (.build builder)))

(defn parse-halt
  "Parse a Halt protobuf message to a map."
  [^cmd.DayCamera.JonSharedCmdDayCamera$Halt proto]
  (merge
    {}
))

(defn build-get-meteo
  "Build a GetMeteo protobuf message from a map."
  [m]
  (let [builder (cmd.DayCamera.JonSharedCmdDayCamera$GetMeteo/newBuilder)]
    (.build builder)))

(defn parse-get-meteo
  "Parse a GetMeteo protobuf message to a map."
  [^cmd.DayCamera.JonSharedCmdDayCamera$GetMeteo proto]
  (merge
    {}
))

(defn build-reset-zoom
  "Build a ResetZoom protobuf message from a map."
  [m]
  (let [builder (cmd.DayCamera.JonSharedCmdDayCamera$ResetZoom/newBuilder)]
    (.build builder)))

(defn parse-reset-zoom
  "Parse a ResetZoom protobuf message to a map."
  [^cmd.DayCamera.JonSharedCmdDayCamera$ResetZoom proto]
  (merge
    {}
))

(defn build-reset-focus
  "Build a ResetFocus protobuf message from a map."
  [m]
  (let [builder (cmd.DayCamera.JonSharedCmdDayCamera$ResetFocus/newBuilder)]
    (.build builder)))

(defn parse-reset-focus
  "Parse a ResetFocus protobuf message to a map."
  [^cmd.DayCamera.JonSharedCmdDayCamera$ResetFocus proto]
  (merge
    {}
))

(defn build-save-to-table
  "Build a SaveToTable protobuf message from a map."
  [m]
  (let [builder (cmd.DayCamera.JonSharedCmdDayCamera$SaveToTable/newBuilder)]
    (.build builder)))

(defn parse-save-to-table
  "Parse a SaveToTable protobuf message to a map."
  [^cmd.DayCamera.JonSharedCmdDayCamera$SaveToTable proto]
  (merge
    {}
))

(defn build-save-to-table-focus
  "Build a SaveToTableFocus protobuf message from a map."
  [m]
  (let [builder (cmd.DayCamera.JonSharedCmdDayCamera$SaveToTableFocus/newBuilder)]
    (.build builder)))

(defn parse-save-to-table-focus
  "Parse a SaveToTableFocus protobuf message to a map."
  [^cmd.DayCamera.JonSharedCmdDayCamera$SaveToTableFocus proto]
  (merge
    {}
))

(defn build-root
  "Build a Root protobuf message from a map."
  [m]
  (let [builder (cmd.HeatCamera.JonSharedCmdHeatCamera$Root/newBuilder)]
    ;; Set oneof payload
    (when-let [payload (first (filter (fn [[k v]] (#{:zoom :set-agc :set-filter :start :stop :photo :zoom-in :zoom-out :zoom-stop :focus-in :focus-out :focus-stop :calibrate :set-dde-level :enable-dde :disable-dde :set-auto-focus :focus-step-plus :focus-step-minus :set-fx-mode :next-fx-mode :prev-fx-mode :get-meteo :shift-dde :refresh-fx-mode :reset-zoom :save-to-table :set-calib-mode :set-digital-zoom-level :set-clahe-level :shift-clahe-level} k)) m))]
      (build-root-payload builder payload))
    (.build builder)))


(defn build-root-payload
  "Set the oneof payload field."
  [builder [field-key field-value]]
  (case field-key
    :zoom (.setZoom builder (build-zoom field-value))
    :set-agc (.setSetAgc builder (build-set-agc field-value))
    :set-filter (.setSetFilter builder (build-set-filters field-value))
    :start (.setStart builder (build-start field-value))
    :stop (.setStop builder (build-stop field-value))
    :photo (.setPhoto builder (build-photo field-value))
    :zoom-in (.setZoomIn builder (build-zoom-in field-value))
    :zoom-out (.setZoomOut builder (build-zoom-out field-value))
    :zoom-stop (.setZoomStop builder (build-zoom-stop field-value))
    :focus-in (.setFocusIn builder (build-focus-in field-value))
    :focus-out (.setFocusOut builder (build-focus-out field-value))
    :focus-stop (.setFocusStop builder (build-focus-stop field-value))
    :calibrate (.setCalibrate builder (build-calibrate field-value))
    :set-dde-level (.setSetDdeLevel builder (build-set-dde-level field-value))
    :enable-dde (.setEnableDde builder (build-enable-dde field-value))
    :disable-dde (.setDisableDde builder (build-disable-dde field-value))
    :set-auto-focus (.setSetAutoFocus builder (build-set-auto-focus field-value))
    :focus-step-plus (.setFocusStepPlus builder (build-focus-step-plus field-value))
    :focus-step-minus (.setFocusStepMinus builder (build-focus-step-minus field-value))
    :set-fx-mode (.setSetFxMode builder (build-set-fx-mode field-value))
    :next-fx-mode (.setNextFxMode builder (build-next-fx-mode field-value))
    :prev-fx-mode (.setPrevFxMode builder (build-prev-fx-mode field-value))
    :get-meteo (.setGetMeteo builder (build-get-meteo field-value))
    :shift-dde (.setShiftDde builder (build-shift-dde field-value))
    :refresh-fx-mode (.setRefreshFxMode builder (build-refresh-fx-mode field-value))
    :reset-zoom (.setResetZoom builder (build-reset-zoom field-value))
    :save-to-table (.setSaveToTable builder (build-save-to-table field-value))
    :set-calib-mode (.setSetCalibMode builder (build-set-calib-mode field-value))
    :set-digital-zoom-level (.setSetDigitalZoomLevel builder (build-set-digital-zoom-level field-value))
    :set-clahe-level (.setSetClaheLevel builder (build-set-clahe-level field-value))
    :shift-clahe-level (.setShiftClaheLevel builder (build-shift-clahe-level field-value))
    (throw (ex-info "Unknown payload field" {:field field-key}))))

(defn parse-root
  "Parse a Root protobuf message to a map."
  [^cmd.HeatCamera.JonSharedCmdHeatCamera$Root proto]
  (merge
    {}
    (parse-root-payload proto)))


(defn parse-root-payload
  "Parse the oneof payload."
  [proto]
  (case (.getPayloadCase proto)
    ZOOM {:zoom (parse-zoom (.getZoom proto))}
    SET_AGC {:set-agc (parse-set-agc (.getSetAgc proto))}
    SET_FILTER {:set-filter (parse-set-filters (.getSetFilter proto))}
    START {:start (parse-start (.getStart proto))}
    STOP {:stop (parse-stop (.getStop proto))}
    PHOTO {:photo (parse-photo (.getPhoto proto))}
    ZOOM_IN {:zoom-in (parse-zoom-in (.getZoomIn proto))}
    ZOOM_OUT {:zoom-out (parse-zoom-out (.getZoomOut proto))}
    ZOOM_STOP {:zoom-stop (parse-zoom-stop (.getZoomStop proto))}
    FOCUS_IN {:focus-in (parse-focus-in (.getFocusIn proto))}
    FOCUS_OUT {:focus-out (parse-focus-out (.getFocusOut proto))}
    FOCUS_STOP {:focus-stop (parse-focus-stop (.getFocusStop proto))}
    CALIBRATE {:calibrate (parse-calibrate (.getCalibrate proto))}
    SET_DDE_LEVEL {:set-dde-level (parse-set-dde-level (.getSetDdeLevel proto))}
    ENABLE_DDE {:enable-dde (parse-enable-dde (.getEnableDde proto))}
    DISABLE_DDE {:disable-dde (parse-disable-dde (.getDisableDde proto))}
    SET_AUTO_FOCUS {:set-auto-focus (parse-set-auto-focus (.getSetAutoFocus proto))}
    FOCUS_STEP_PLUS {:focus-step-plus (parse-focus-step-plus (.getFocusStepPlus proto))}
    FOCUS_STEP_MINUS {:focus-step-minus (parse-focus-step-minus (.getFocusStepMinus proto))}
    SET_FX_MODE {:set-fx-mode (parse-set-fx-mode (.getSetFxMode proto))}
    NEXT_FX_MODE {:next-fx-mode (parse-next-fx-mode (.getNextFxMode proto))}
    PREV_FX_MODE {:prev-fx-mode (parse-prev-fx-mode (.getPrevFxMode proto))}
    GET_METEO {:get-meteo (parse-get-meteo (.getGetMeteo proto))}
    SHIFT_DDE {:shift-dde (parse-shift-dde (.getShiftDde proto))}
    REFRESH_FX_MODE {:refresh-fx-mode (parse-refresh-fx-mode (.getRefreshFxMode proto))}
    RESET_ZOOM {:reset-zoom (parse-reset-zoom (.getResetZoom proto))}
    SAVE_TO_TABLE {:save-to-table (parse-save-to-table (.getSaveToTable proto))}
    SET_CALIB_MODE {:set-calib-mode (parse-set-calib-mode (.getSetCalibMode proto))}
    SET_DIGITAL_ZOOM_LEVEL {:set-digital-zoom-level (parse-set-digital-zoom-level (.getSetDigitalZoomLevel proto))}
    SET_CLAHE_LEVEL {:set-clahe-level (parse-set-clahe-level (.getSetClaheLevel proto))}
    SHIFT_CLAHE_LEVEL {:shift-clahe-level (parse-shift-clahe-level (.getShiftClaheLevel proto))}
    {}))

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
  (merge
    {:mode (.getMode proto)}
))

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
  (merge
    {:value (.getValue proto)}
))

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
  (merge
    {:value (.getValue proto)}
))

(defn build-next-fx-mode
  "Build a NextFxMode protobuf message from a map."
  [m]
  (let [builder (cmd.HeatCamera.JonSharedCmdHeatCamera$NextFxMode/newBuilder)]
    (.build builder)))

(defn parse-next-fx-mode
  "Parse a NextFxMode protobuf message to a map."
  [^cmd.HeatCamera.JonSharedCmdHeatCamera$NextFxMode proto]
  (merge
    {}
))

(defn build-prev-fx-mode
  "Build a PrevFxMode protobuf message from a map."
  [m]
  (let [builder (cmd.HeatCamera.JonSharedCmdHeatCamera$PrevFxMode/newBuilder)]
    (.build builder)))

(defn parse-prev-fx-mode
  "Parse a PrevFxMode protobuf message to a map."
  [^cmd.HeatCamera.JonSharedCmdHeatCamera$PrevFxMode proto]
  (merge
    {}
))

(defn build-refresh-fx-mode
  "Build a RefreshFxMode protobuf message from a map."
  [m]
  (let [builder (cmd.HeatCamera.JonSharedCmdHeatCamera$RefreshFxMode/newBuilder)]
    (.build builder)))

(defn parse-refresh-fx-mode
  "Parse a RefreshFxMode protobuf message to a map."
  [^cmd.HeatCamera.JonSharedCmdHeatCamera$RefreshFxMode proto]
  (merge
    {}
))

(defn build-enable-dde
  "Build a EnableDDE protobuf message from a map."
  [m]
  (let [builder (cmd.HeatCamera.JonSharedCmdHeatCamera$EnableDDE/newBuilder)]
    (.build builder)))

(defn parse-enable-dde
  "Parse a EnableDDE protobuf message to a map."
  [^cmd.HeatCamera.JonSharedCmdHeatCamera$EnableDDE proto]
  (merge
    {}
))

(defn build-disable-dde
  "Build a DisableDDE protobuf message from a map."
  [m]
  (let [builder (cmd.HeatCamera.JonSharedCmdHeatCamera$DisableDDE/newBuilder)]
    (.build builder)))

(defn parse-disable-dde
  "Parse a DisableDDE protobuf message to a map."
  [^cmd.HeatCamera.JonSharedCmdHeatCamera$DisableDDE proto]
  (merge
    {}
))

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
  (merge
    {:value (.getValue proto)}
))

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
  (merge
    {:value (.getValue proto)}
))

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
  (merge
    {:value (.getValue proto)}
))

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
  (merge
    {:value (.getValue proto)}
))

(defn build-zoom-in
  "Build a ZoomIn protobuf message from a map."
  [m]
  (let [builder (cmd.HeatCamera.JonSharedCmdHeatCamera$ZoomIn/newBuilder)]
    (.build builder)))

(defn parse-zoom-in
  "Parse a ZoomIn protobuf message to a map."
  [^cmd.HeatCamera.JonSharedCmdHeatCamera$ZoomIn proto]
  (merge
    {}
))

(defn build-zoom-out
  "Build a ZoomOut protobuf message from a map."
  [m]
  (let [builder (cmd.HeatCamera.JonSharedCmdHeatCamera$ZoomOut/newBuilder)]
    (.build builder)))

(defn parse-zoom-out
  "Parse a ZoomOut protobuf message to a map."
  [^cmd.HeatCamera.JonSharedCmdHeatCamera$ZoomOut proto]
  (merge
    {}
))

(defn build-zoom-stop
  "Build a ZoomStop protobuf message from a map."
  [m]
  (let [builder (cmd.HeatCamera.JonSharedCmdHeatCamera$ZoomStop/newBuilder)]
    (.build builder)))

(defn parse-zoom-stop
  "Parse a ZoomStop protobuf message to a map."
  [^cmd.HeatCamera.JonSharedCmdHeatCamera$ZoomStop proto]
  (merge
    {}
))

(defn build-focus-in
  "Build a FocusIn protobuf message from a map."
  [m]
  (let [builder (cmd.HeatCamera.JonSharedCmdHeatCamera$FocusIn/newBuilder)]
    (.build builder)))

(defn parse-focus-in
  "Parse a FocusIn protobuf message to a map."
  [^cmd.HeatCamera.JonSharedCmdHeatCamera$FocusIn proto]
  (merge
    {}
))

(defn build-focus-out
  "Build a FocusOut protobuf message from a map."
  [m]
  (let [builder (cmd.HeatCamera.JonSharedCmdHeatCamera$FocusOut/newBuilder)]
    (.build builder)))

(defn parse-focus-out
  "Parse a FocusOut protobuf message to a map."
  [^cmd.HeatCamera.JonSharedCmdHeatCamera$FocusOut proto]
  (merge
    {}
))

(defn build-focus-stop
  "Build a FocusStop protobuf message from a map."
  [m]
  (let [builder (cmd.HeatCamera.JonSharedCmdHeatCamera$FocusStop/newBuilder)]
    (.build builder)))

(defn parse-focus-stop
  "Parse a FocusStop protobuf message to a map."
  [^cmd.HeatCamera.JonSharedCmdHeatCamera$FocusStop proto]
  (merge
    {}
))

(defn build-focus-step-plus
  "Build a FocusStepPlus protobuf message from a map."
  [m]
  (let [builder (cmd.HeatCamera.JonSharedCmdHeatCamera$FocusStepPlus/newBuilder)]
    (.build builder)))

(defn parse-focus-step-plus
  "Parse a FocusStepPlus protobuf message to a map."
  [^cmd.HeatCamera.JonSharedCmdHeatCamera$FocusStepPlus proto]
  (merge
    {}
))

(defn build-focus-step-minus
  "Build a FocusStepMinus protobuf message from a map."
  [m]
  (let [builder (cmd.HeatCamera.JonSharedCmdHeatCamera$FocusStepMinus/newBuilder)]
    (.build builder)))

(defn parse-focus-step-minus
  "Parse a FocusStepMinus protobuf message to a map."
  [^cmd.HeatCamera.JonSharedCmdHeatCamera$FocusStepMinus proto]
  (merge
    {}
))

(defn build-calibrate
  "Build a Calibrate protobuf message from a map."
  [m]
  (let [builder (cmd.HeatCamera.JonSharedCmdHeatCamera$Calibrate/newBuilder)]
    (.build builder)))

(defn parse-calibrate
  "Parse a Calibrate protobuf message to a map."
  [^cmd.HeatCamera.JonSharedCmdHeatCamera$Calibrate proto]
  (merge
    {}
))

(defn build-zoom
  "Build a Zoom protobuf message from a map."
  [m]
  (let [builder (cmd.HeatCamera.JonSharedCmdHeatCamera$Zoom/newBuilder)]
    ;; Set oneof payload
    (when-let [payload (first (filter (fn [[k v]] (#{:set-zoom-table-value :next-zoom-table-pos :prev-zoom-table-pos} k)) m))]
      (build-zoom-payload builder payload))
    (.build builder)))


(defn build-zoom-payload
  "Set the oneof payload field."
  [builder [field-key field-value]]
  (case field-key
    :set-zoom-table-value (.setSetZoomTableValue builder (build-set-zoom-table-value field-value))
    :next-zoom-table-pos (.setNextZoomTablePos builder (build-next-zoom-table-pos field-value))
    :prev-zoom-table-pos (.setPrevZoomTablePos builder (build-prev-zoom-table-pos field-value))
    (throw (ex-info "Unknown payload field" {:field field-key}))))

(defn parse-zoom
  "Parse a Zoom protobuf message to a map."
  [^cmd.HeatCamera.JonSharedCmdHeatCamera$Zoom proto]
  (merge
    {}
    (parse-zoom-payload proto)))


(defn parse-zoom-payload
  "Parse the oneof payload."
  [proto]
  (case (.getPayloadCase proto)
    SET_ZOOM_TABLE_VALUE {:set-zoom-table-value (parse-set-zoom-table-value (.getSetZoomTableValue proto))}
    NEXT_ZOOM_TABLE_POS {:next-zoom-table-pos (parse-next-zoom-table-pos (.getNextZoomTablePos proto))}
    PREV_ZOOM_TABLE_POS {:prev-zoom-table-pos (parse-prev-zoom-table-pos (.getPrevZoomTablePos proto))}
    {}))

(defn build-next-zoom-table-pos
  "Build a NextZoomTablePos protobuf message from a map."
  [m]
  (let [builder (cmd.HeatCamera.JonSharedCmdHeatCamera$NextZoomTablePos/newBuilder)]
    (.build builder)))

(defn parse-next-zoom-table-pos
  "Parse a NextZoomTablePos protobuf message to a map."
  [^cmd.HeatCamera.JonSharedCmdHeatCamera$NextZoomTablePos proto]
  (merge
    {}
))

(defn build-prev-zoom-table-pos
  "Build a PrevZoomTablePos protobuf message from a map."
  [m]
  (let [builder (cmd.HeatCamera.JonSharedCmdHeatCamera$PrevZoomTablePos/newBuilder)]
    (.build builder)))

(defn parse-prev-zoom-table-pos
  "Parse a PrevZoomTablePos protobuf message to a map."
  [^cmd.HeatCamera.JonSharedCmdHeatCamera$PrevZoomTablePos proto]
  (merge
    {}
))

(defn build-set-calib-mode
  "Build a SetCalibMode protobuf message from a map."
  [m]
  (let [builder (cmd.HeatCamera.JonSharedCmdHeatCamera$SetCalibMode/newBuilder)]
    (.build builder)))

(defn parse-set-calib-mode
  "Parse a SetCalibMode protobuf message to a map."
  [^cmd.HeatCamera.JonSharedCmdHeatCamera$SetCalibMode proto]
  (merge
    {}
))

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
  (merge
    {:value (.getValue proto)}
))

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
  (merge
    {:value (.getValue proto)}
))

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
  (merge
    {:value (.getValue proto)}
))

(defn build-start
  "Build a Start protobuf message from a map."
  [m]
  (let [builder (cmd.HeatCamera.JonSharedCmdHeatCamera$Start/newBuilder)]
    (.build builder)))

(defn parse-start
  "Parse a Start protobuf message to a map."
  [^cmd.HeatCamera.JonSharedCmdHeatCamera$Start proto]
  (merge
    {}
))

(defn build-stop
  "Build a Stop protobuf message from a map."
  [m]
  (let [builder (cmd.HeatCamera.JonSharedCmdHeatCamera$Stop/newBuilder)]
    (.build builder)))

(defn parse-stop
  "Parse a Stop protobuf message to a map."
  [^cmd.HeatCamera.JonSharedCmdHeatCamera$Stop proto]
  (merge
    {}
))

(defn build-halt
  "Build a Halt protobuf message from a map."
  [m]
  (let [builder (cmd.HeatCamera.JonSharedCmdHeatCamera$Halt/newBuilder)]
    (.build builder)))

(defn parse-halt
  "Parse a Halt protobuf message to a map."
  [^cmd.HeatCamera.JonSharedCmdHeatCamera$Halt proto]
  (merge
    {}
))

(defn build-photo
  "Build a Photo protobuf message from a map."
  [m]
  (let [builder (cmd.HeatCamera.JonSharedCmdHeatCamera$Photo/newBuilder)]
    (.build builder)))

(defn parse-photo
  "Parse a Photo protobuf message to a map."
  [^cmd.HeatCamera.JonSharedCmdHeatCamera$Photo proto]
  (merge
    {}
))

(defn build-get-meteo
  "Build a GetMeteo protobuf message from a map."
  [m]
  (let [builder (cmd.HeatCamera.JonSharedCmdHeatCamera$GetMeteo/newBuilder)]
    (.build builder)))

(defn parse-get-meteo
  "Parse a GetMeteo protobuf message to a map."
  [^cmd.HeatCamera.JonSharedCmdHeatCamera$GetMeteo proto]
  (merge
    {}
))

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
  (merge
    {:value (.getValue proto)}
))

(defn build-reset-zoom
  "Build a ResetZoom protobuf message from a map."
  [m]
  (let [builder (cmd.HeatCamera.JonSharedCmdHeatCamera$ResetZoom/newBuilder)]
    (.build builder)))

(defn parse-reset-zoom
  "Parse a ResetZoom protobuf message to a map."
  [^cmd.HeatCamera.JonSharedCmdHeatCamera$ResetZoom proto]
  (merge
    {}
))

(defn build-save-to-table
  "Build a SaveToTable protobuf message from a map."
  [m]
  (let [builder (cmd.HeatCamera.JonSharedCmdHeatCamera$SaveToTable/newBuilder)]
    (.build builder)))

(defn parse-save-to-table
  "Parse a SaveToTable protobuf message to a map."
  [^cmd.HeatCamera.JonSharedCmdHeatCamera$SaveToTable proto]
  (merge
    {}
))

(defn build-root
  "Build a Root protobuf message from a map."
  [m]
  (let [builder (cmd.RotaryPlatform.JonSharedCmdRotary$Root/newBuilder)]
    ;; Set oneof payload
    (when-let [payload (first (filter (fn [[k v]] (#{:start :stop :axis :set-platform-azimuth :set-platform-elevation :set-platform-bank :halt :set-use-rotary-as-compass :rotate-to-gps :set-origin-gps :set-mode :rotate-to-ndc :scan-start :scan-stop :scan-pause :scan-unpause :get-meteo :scan-prev :scan-next :scan-refresh-node-list :scan-select-node :scan-delete-node :scan-update-node :scan-add-node} k)) m))]
      (build-root-payload builder payload))
    (.build builder)))


(defn build-root-payload
  "Set the oneof payload field."
  [builder [field-key field-value]]
  (case field-key
    :start (.setStart builder (build-start field-value))
    :stop (.setStop builder (build-stop field-value))
    :axis (.setAxis builder (build-axis field-value))
    :set-platform-azimuth (.setSetPlatformAzimuth builder (build-set-platform-azimuth field-value))
    :set-platform-elevation (.setSetPlatformElevation builder (build-set-platform-elevation field-value))
    :set-platform-bank (.setSetPlatformBank builder (build-set-platform-bank field-value))
    :halt (.setHalt builder (build-halt field-value))
    :set-use-rotary-as-compass (.setSetUseRotaryAsCompass builder (build-set-use-rotary-as-compass field-value))
    :rotate-to-gps (.setRotateToGps builder (build-rotate-to-gps field-value))
    :set-origin-gps (.setSetOriginGps builder (build-set-origin-gps field-value))
    :set-mode (.setSetMode builder (build-set-mode field-value))
    :rotate-to-ndc (.setRotateToNdc builder (build-rotate-to-ndc field-value))
    :scan-start (.setScanStart builder (build-scan-start field-value))
    :scan-stop (.setScanStop builder (build-scan-stop field-value))
    :scan-pause (.setScanPause builder (build-scan-pause field-value))
    :scan-unpause (.setScanUnpause builder (build-scan-unpause field-value))
    :get-meteo (.setGetMeteo builder (build-get-meteo field-value))
    :scan-prev (.setScanPrev builder (build-scan-prev field-value))
    :scan-next (.setScanNext builder (build-scan-next field-value))
    :scan-refresh-node-list (.setScanRefreshNodeList builder (build-scan-refresh-node-list field-value))
    :scan-select-node (.setScanSelectNode builder (build-scan-select-node field-value))
    :scan-delete-node (.setScanDeleteNode builder (build-scan-delete-node field-value))
    :scan-update-node (.setScanUpdateNode builder (build-scan-update-node field-value))
    :scan-add-node (.setScanAddNode builder (build-scan-add-node field-value))
    (throw (ex-info "Unknown payload field" {:field field-key}))))

(defn parse-root
  "Parse a Root protobuf message to a map."
  [^cmd.RotaryPlatform.JonSharedCmdRotary$Root proto]
  (merge
    {}
    (parse-root-payload proto)))


(defn parse-root-payload
  "Parse the oneof payload."
  [proto]
  (case (.getPayloadCase proto)
    START {:start (parse-start (.getStart proto))}
    STOP {:stop (parse-stop (.getStop proto))}
    AXIS {:axis (parse-axis (.getAxis proto))}
    SET_PLATFORM_AZIMUTH {:set-platform-azimuth (parse-set-platform-azimuth (.getSetPlatformAzimuth proto))}
    SET_PLATFORM_ELEVATION {:set-platform-elevation (parse-set-platform-elevation (.getSetPlatformElevation proto))}
    SET_PLATFORM_BANK {:set-platform-bank (parse-set-platform-bank (.getSetPlatformBank proto))}
    HALT {:halt (parse-halt (.getHalt proto))}
    SET_USE_ROTARY_AS_COMPASS {:set-use-rotary-as-compass (parse-set-use-rotary-as-compass (.getSetUseRotaryAsCompass proto))}
    ROTATE_TO_GPS {:rotate-to-gps (parse-rotate-to-gps (.getRotateToGps proto))}
    SET_ORIGIN_GPS {:set-origin-gps (parse-set-origin-gps (.getSetOriginGps proto))}
    SET_MODE {:set-mode (parse-set-mode (.getSetMode proto))}
    ROTATE_TO_NDC {:rotate-to-ndc (parse-rotate-to-ndc (.getRotateToNdc proto))}
    SCAN_START {:scan-start (parse-scan-start (.getScanStart proto))}
    SCAN_STOP {:scan-stop (parse-scan-stop (.getScanStop proto))}
    SCAN_PAUSE {:scan-pause (parse-scan-pause (.getScanPause proto))}
    SCAN_UNPAUSE {:scan-unpause (parse-scan-unpause (.getScanUnpause proto))}
    GET_METEO {:get-meteo (parse-get-meteo (.getGetMeteo proto))}
    SCAN_PREV {:scan-prev (parse-scan-prev (.getScanPrev proto))}
    SCAN_NEXT {:scan-next (parse-scan-next (.getScanNext proto))}
    SCAN_REFRESH_NODE_LIST {:scan-refresh-node-list (parse-scan-refresh-node-list (.getScanRefreshNodeList proto))}
    SCAN_SELECT_NODE {:scan-select-node (parse-scan-select-node (.getScanSelectNode proto))}
    SCAN_DELETE_NODE {:scan-delete-node (parse-scan-delete-node (.getScanDeleteNode proto))}
    SCAN_UPDATE_NODE {:scan-update-node (parse-scan-update-node (.getScanUpdateNode proto))}
    SCAN_ADD_NODE {:scan-add-node (parse-scan-add-node (.getScanAddNode proto))}
    {}))

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
  (merge
    {:azimuth (.getAzimuth proto)
     :elevation (.getElevation proto)}
))

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
  (merge
    {:mode (.getMode proto)}
))

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
  (merge
    {:value (.getValue proto)
     :direction (.getDirection proto)}
))

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
  (merge
    {:target-value (.getTargetValue proto)
     :speed (.getSpeed proto)
     :direction (.getDirection proto)}
))

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
  (merge
    {:speed (.getSpeed proto)
     :direction (.getDirection proto)}
))

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
  (merge
    {:speed (.getSpeed proto)
     :direction (.getDirection proto)}
))

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
  (merge
    {:value (.getValue proto)}
))

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
  (merge
    {:target-value (.getTargetValue proto)
     :speed (.getSpeed proto)}
))

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
  (merge
    {:value (.getValue proto)
     :speed (.getSpeed proto)
     :direction (.getDirection proto)}
))

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
  (merge
    {:value (.getValue proto)
     :direction (.getDirection proto)}
))

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
  (merge
    {:value (.getValue proto)
     :speed (.getSpeed proto)
     :direction (.getDirection proto)}
))

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
  (merge
    {:value (.getValue proto)
     :direction (.getDirection proto)}
))

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
  (merge
    {:value (.getValue proto)}
))

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
  (merge
    {:value (.getValue proto)}
))

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
  (merge
    {:value (.getValue proto)}
))

(defn build-get-meteo
  "Build a GetMeteo protobuf message from a map."
  [m]
  (let [builder (cmd.RotaryPlatform.JonSharedCmdRotary$GetMeteo/newBuilder)]
    (.build builder)))

(defn parse-get-meteo
  "Parse a GetMeteo protobuf message to a map."
  [^cmd.RotaryPlatform.JonSharedCmdRotary$GetMeteo proto]
  (merge
    {}
))

(defn build-azimuth
  "Build a Azimuth protobuf message from a map."
  [m]
  (let [builder (cmd.RotaryPlatform.JonSharedCmdRotary$Azimuth/newBuilder)]
    ;; Set oneof payload
    (when-let [payload (first (filter (fn [[k v]] (#{:set-value :rotate-to :rotate :relative :relative-set :halt} k)) m))]
      (build-azimuth-payload builder payload))
    (.build builder)))


(defn build-azimuth-payload
  "Set the oneof payload field."
  [builder [field-key field-value]]
  (case field-key
    :set-value (.setSetValue builder (build-set-azimuth-value field-value))
    :rotate-to (.setRotateTo builder (build-rotate-azimuth-to field-value))
    :rotate (.setRotate builder (build-rotate-azimuth field-value))
    :relative (.setRelative builder (build-rotate-azimuth-relative field-value))
    :relative-set (.setRelativeSet builder (build-rotate-azimuth-relative-set field-value))
    :halt (.setHalt builder (build-halt-azimuth field-value))
    (throw (ex-info "Unknown payload field" {:field field-key}))))

(defn parse-azimuth
  "Parse a Azimuth protobuf message to a map."
  [^cmd.RotaryPlatform.JonSharedCmdRotary$Azimuth proto]
  (merge
    {}
    (parse-azimuth-payload proto)))


(defn parse-azimuth-payload
  "Parse the oneof payload."
  [proto]
  (case (.getPayloadCase proto)
    SET_VALUE {:set-value (parse-set-azimuth-value (.getSetValue proto))}
    ROTATE_TO {:rotate-to (parse-rotate-azimuth-to (.getRotateTo proto))}
    ROTATE {:rotate (parse-rotate-azimuth (.getRotate proto))}
    RELATIVE {:relative (parse-rotate-azimuth-relative (.getRelative proto))}
    RELATIVE_SET {:relative-set (parse-rotate-azimuth-relative-set (.getRelativeSet proto))}
    HALT {:halt (parse-halt-azimuth (.getHalt proto))}
    {}))

(defn build-start
  "Build a Start protobuf message from a map."
  [m]
  (let [builder (cmd.RotaryPlatform.JonSharedCmdRotary$Start/newBuilder)]
    (.build builder)))

(defn parse-start
  "Parse a Start protobuf message to a map."
  [^cmd.RotaryPlatform.JonSharedCmdRotary$Start proto]
  (merge
    {}
))

(defn build-stop
  "Build a Stop protobuf message from a map."
  [m]
  (let [builder (cmd.RotaryPlatform.JonSharedCmdRotary$Stop/newBuilder)]
    (.build builder)))

(defn parse-stop
  "Parse a Stop protobuf message to a map."
  [^cmd.RotaryPlatform.JonSharedCmdRotary$Stop proto]
  (merge
    {}
))

(defn build-halt
  "Build a Halt protobuf message from a map."
  [m]
  (let [builder (cmd.RotaryPlatform.JonSharedCmdRotary$Halt/newBuilder)]
    (.build builder)))

(defn parse-halt
  "Parse a Halt protobuf message to a map."
  [^cmd.RotaryPlatform.JonSharedCmdRotary$Halt proto]
  (merge
    {}
))

(defn build-scan-start
  "Build a ScanStart protobuf message from a map."
  [m]
  (let [builder (cmd.RotaryPlatform.JonSharedCmdRotary$ScanStart/newBuilder)]
    (.build builder)))

(defn parse-scan-start
  "Parse a ScanStart protobuf message to a map."
  [^cmd.RotaryPlatform.JonSharedCmdRotary$ScanStart proto]
  (merge
    {}
))

(defn build-scan-stop
  "Build a ScanStop protobuf message from a map."
  [m]
  (let [builder (cmd.RotaryPlatform.JonSharedCmdRotary$ScanStop/newBuilder)]
    (.build builder)))

(defn parse-scan-stop
  "Parse a ScanStop protobuf message to a map."
  [^cmd.RotaryPlatform.JonSharedCmdRotary$ScanStop proto]
  (merge
    {}
))

(defn build-scan-pause
  "Build a ScanPause protobuf message from a map."
  [m]
  (let [builder (cmd.RotaryPlatform.JonSharedCmdRotary$ScanPause/newBuilder)]
    (.build builder)))

(defn parse-scan-pause
  "Parse a ScanPause protobuf message to a map."
  [^cmd.RotaryPlatform.JonSharedCmdRotary$ScanPause proto]
  (merge
    {}
))

(defn build-scan-unpause
  "Build a ScanUnpause protobuf message from a map."
  [m]
  (let [builder (cmd.RotaryPlatform.JonSharedCmdRotary$ScanUnpause/newBuilder)]
    (.build builder)))

(defn parse-scan-unpause
  "Parse a ScanUnpause protobuf message to a map."
  [^cmd.RotaryPlatform.JonSharedCmdRotary$ScanUnpause proto]
  (merge
    {}
))

(defn build-halt-azimuth
  "Build a HaltAzimuth protobuf message from a map."
  [m]
  (let [builder (cmd.RotaryPlatform.JonSharedCmdRotary$HaltAzimuth/newBuilder)]
    (.build builder)))

(defn parse-halt-azimuth
  "Parse a HaltAzimuth protobuf message to a map."
  [^cmd.RotaryPlatform.JonSharedCmdRotary$HaltAzimuth proto]
  (merge
    {}
))

(defn build-halt-elevation
  "Build a HaltElevation protobuf message from a map."
  [m]
  (let [builder (cmd.RotaryPlatform.JonSharedCmdRotary$HaltElevation/newBuilder)]
    (.build builder)))

(defn parse-halt-elevation
  "Parse a HaltElevation protobuf message to a map."
  [^cmd.RotaryPlatform.JonSharedCmdRotary$HaltElevation proto]
  (merge
    {}
))

(defn build-scan-prev
  "Build a ScanPrev protobuf message from a map."
  [m]
  (let [builder (cmd.RotaryPlatform.JonSharedCmdRotary$ScanPrev/newBuilder)]
    (.build builder)))

(defn parse-scan-prev
  "Parse a ScanPrev protobuf message to a map."
  [^cmd.RotaryPlatform.JonSharedCmdRotary$ScanPrev proto]
  (merge
    {}
))

(defn build-scan-next
  "Build a ScanNext protobuf message from a map."
  [m]
  (let [builder (cmd.RotaryPlatform.JonSharedCmdRotary$ScanNext/newBuilder)]
    (.build builder)))

(defn parse-scan-next
  "Parse a ScanNext protobuf message to a map."
  [^cmd.RotaryPlatform.JonSharedCmdRotary$ScanNext proto]
  (merge
    {}
))

(defn build-scan-refresh-node-list
  "Build a ScanRefreshNodeList protobuf message from a map."
  [m]
  (let [builder (cmd.RotaryPlatform.JonSharedCmdRotary$ScanRefreshNodeList/newBuilder)]
    (.build builder)))

(defn parse-scan-refresh-node-list
  "Parse a ScanRefreshNodeList protobuf message to a map."
  [^cmd.RotaryPlatform.JonSharedCmdRotary$ScanRefreshNodeList proto]
  (merge
    {}
))

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
  (merge
    {:index (.getIndex proto)}
))

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
  (merge
    {:index (.getIndex proto)}
))

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
  (merge
    {:index (.getIndex proto)
     :day-zoom-table-value (.getDayZoomTableValue proto)
     :heat-zoom-table-value (.getHeatZoomTableValue proto)
     :azimuth (.getAzimuth proto)
     :elevation (.getElevation proto)
     :linger (.getLinger proto)
     :speed (.getSpeed proto)}
))

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
  (merge
    {:index (.getIndex proto)
     :day-zoom-table-value (.getDayZoomTableValue proto)
     :heat-zoom-table-value (.getHeatZoomTableValue proto)
     :azimuth (.getAzimuth proto)
     :elevation (.getElevation proto)
     :linger (.getLinger proto)
     :speed (.getSpeed proto)}
))

(defn build-elevation
  "Build a Elevation protobuf message from a map."
  [m]
  (let [builder (cmd.RotaryPlatform.JonSharedCmdRotary$Elevation/newBuilder)]
    ;; Set oneof payload
    (when-let [payload (first (filter (fn [[k v]] (#{:set-value :rotate-to :rotate :relative :relative-set :halt} k)) m))]
      (build-elevation-payload builder payload))
    (.build builder)))


(defn build-elevation-payload
  "Set the oneof payload field."
  [builder [field-key field-value]]
  (case field-key
    :set-value (.setSetValue builder (build-set-elevation-value field-value))
    :rotate-to (.setRotateTo builder (build-rotate-elevation-to field-value))
    :rotate (.setRotate builder (build-rotate-elevation field-value))
    :relative (.setRelative builder (build-rotate-elevation-relative field-value))
    :relative-set (.setRelativeSet builder (build-rotate-elevation-relative-set field-value))
    :halt (.setHalt builder (build-halt-elevation field-value))
    (throw (ex-info "Unknown payload field" {:field field-key}))))

(defn parse-elevation
  "Parse a Elevation protobuf message to a map."
  [^cmd.RotaryPlatform.JonSharedCmdRotary$Elevation proto]
  (merge
    {}
    (parse-elevation-payload proto)))


(defn parse-elevation-payload
  "Parse the oneof payload."
  [proto]
  (case (.getPayloadCase proto)
    SET_VALUE {:set-value (parse-set-elevation-value (.getSetValue proto))}
    ROTATE_TO {:rotate-to (parse-rotate-elevation-to (.getRotateTo proto))}
    ROTATE {:rotate (parse-rotate-elevation (.getRotate proto))}
    RELATIVE {:relative (parse-rotate-elevation-relative (.getRelative proto))}
    RELATIVE_SET {:relative-set (parse-rotate-elevation-relative-set (.getRelativeSet proto))}
    HALT {:halt (parse-halt-elevation (.getHalt proto))}
    {}))

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
  (merge
    {:flag (.getFlag proto)}
))

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
  (merge
    {:latitude (.getLatitude proto)
     :longitude (.getLongitude proto)
     :altitude (.getAltitude proto)}
))

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
  (merge
    {:latitude (.getLatitude proto)
     :longitude (.getLongitude proto)
     :altitude (.getAltitude proto)}
))

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
  (merge
    {:channel (.getChannel proto)
     :x (.getX proto)
     :y (.getY proto)}
))

(defn build-root
  "Build a Root protobuf message from a map."
  [m]
  (let [builder (cmd.OSD.JonSharedCmdOsd$Root/newBuilder)]
    ;; Set oneof payload
    (when-let [payload (first (filter (fn [[k v]] (#{:show-default-screen :show-lrf-measure-screen :show-lrf-result-screen :show-lrf-result-simplified-screen :enable-heat-osd :disable-heat-osd :enable-day-osd :disable-day-osd} k)) m))]
      (build-root-payload builder payload))
    (.build builder)))


(defn build-root-payload
  "Set the oneof payload field."
  [builder [field-key field-value]]
  (case field-key
    :show-default-screen (.setShowDefaultScreen builder (build-show-default-screen field-value))
    :show-lrf-measure-screen (.setShowLrfMeasureScreen builder (build-show-lrf-measure-screen field-value))
    :show-lrf-result-screen (.setShowLrfResultScreen builder (build-show-lrf-result-screen field-value))
    :show-lrf-result-simplified-screen (.setShowLrfResultSimplifiedScreen builder (build-show-lrf-result-simplified-screen field-value))
    :enable-heat-osd (.setEnableHeatOsd builder (build-enable-heat-osd field-value))
    :disable-heat-osd (.setDisableHeatOsd builder (build-disable-heat-osd field-value))
    :enable-day-osd (.setEnableDayOsd builder (build-enable-day-osd field-value))
    :disable-day-osd (.setDisableDayOsd builder (build-disable-day-osd field-value))
    (throw (ex-info "Unknown payload field" {:field field-key}))))

(defn parse-root
  "Parse a Root protobuf message to a map."
  [^cmd.OSD.JonSharedCmdOsd$Root proto]
  (merge
    {}
    (parse-root-payload proto)))


(defn parse-root-payload
  "Parse the oneof payload."
  [proto]
  (case (.getPayloadCase proto)
    SHOW_DEFAULT_SCREEN {:show-default-screen (parse-show-default-screen (.getShowDefaultScreen proto))}
    SHOW_LRF_MEASURE_SCREEN {:show-lrf-measure-screen (parse-show-lrf-measure-screen (.getShowLrfMeasureScreen proto))}
    SHOW_LRF_RESULT_SCREEN {:show-lrf-result-screen (parse-show-lrf-result-screen (.getShowLrfResultScreen proto))}
    SHOW_LRF_RESULT_SIMPLIFIED_SCREEN {:show-lrf-result-simplified-screen (parse-show-lrf-result-simplified-screen (.getShowLrfResultSimplifiedScreen proto))}
    ENABLE_HEAT_OSD {:enable-heat-osd (parse-enable-heat-osd (.getEnableHeatOsd proto))}
    DISABLE_HEAT_OSD {:disable-heat-osd (parse-disable-heat-osd (.getDisableHeatOsd proto))}
    ENABLE_DAY_OSD {:enable-day-osd (parse-enable-day-osd (.getEnableDayOsd proto))}
    DISABLE_DAY_OSD {:disable-day-osd (parse-disable-day-osd (.getDisableDayOsd proto))}
    {}))

(defn build-show-default-screen
  "Build a ShowDefaultScreen protobuf message from a map."
  [m]
  (let [builder (cmd.OSD.JonSharedCmdOsd$ShowDefaultScreen/newBuilder)]
    (.build builder)))

(defn parse-show-default-screen
  "Parse a ShowDefaultScreen protobuf message to a map."
  [^cmd.OSD.JonSharedCmdOsd$ShowDefaultScreen proto]
  (merge
    {}
))

(defn build-show-lrf-measure-screen
  "Build a ShowLRFMeasureScreen protobuf message from a map."
  [m]
  (let [builder (cmd.OSD.JonSharedCmdOsd$ShowLRFMeasureScreen/newBuilder)]
    (.build builder)))

(defn parse-show-lrf-measure-screen
  "Parse a ShowLRFMeasureScreen protobuf message to a map."
  [^cmd.OSD.JonSharedCmdOsd$ShowLRFMeasureScreen proto]
  (merge
    {}
))

(defn build-show-lrf-result-screen
  "Build a ShowLRFResultScreen protobuf message from a map."
  [m]
  (let [builder (cmd.OSD.JonSharedCmdOsd$ShowLRFResultScreen/newBuilder)]
    (.build builder)))

(defn parse-show-lrf-result-screen
  "Parse a ShowLRFResultScreen protobuf message to a map."
  [^cmd.OSD.JonSharedCmdOsd$ShowLRFResultScreen proto]
  (merge
    {}
))

(defn build-show-lrf-result-simplified-screen
  "Build a ShowLRFResultSimplifiedScreen protobuf message from a map."
  [m]
  (let [builder (cmd.OSD.JonSharedCmdOsd$ShowLRFResultSimplifiedScreen/newBuilder)]
    (.build builder)))

(defn parse-show-lrf-result-simplified-screen
  "Parse a ShowLRFResultSimplifiedScreen protobuf message to a map."
  [^cmd.OSD.JonSharedCmdOsd$ShowLRFResultSimplifiedScreen proto]
  (merge
    {}
))

(defn build-enable-heat-osd
  "Build a EnableHeatOSD protobuf message from a map."
  [m]
  (let [builder (cmd.OSD.JonSharedCmdOsd$EnableHeatOSD/newBuilder)]
    (.build builder)))

(defn parse-enable-heat-osd
  "Parse a EnableHeatOSD protobuf message to a map."
  [^cmd.OSD.JonSharedCmdOsd$EnableHeatOSD proto]
  (merge
    {}
))

(defn build-disable-heat-osd
  "Build a DisableHeatOSD protobuf message from a map."
  [m]
  (let [builder (cmd.OSD.JonSharedCmdOsd$DisableHeatOSD/newBuilder)]
    (.build builder)))

(defn parse-disable-heat-osd
  "Parse a DisableHeatOSD protobuf message to a map."
  [^cmd.OSD.JonSharedCmdOsd$DisableHeatOSD proto]
  (merge
    {}
))

(defn build-enable-day-osd
  "Build a EnableDayOSD protobuf message from a map."
  [m]
  (let [builder (cmd.OSD.JonSharedCmdOsd$EnableDayOSD/newBuilder)]
    (.build builder)))

(defn parse-enable-day-osd
  "Parse a EnableDayOSD protobuf message to a map."
  [^cmd.OSD.JonSharedCmdOsd$EnableDayOSD proto]
  (merge
    {}
))

(defn build-disable-day-osd
  "Build a DisableDayOSD protobuf message from a map."
  [m]
  (let [builder (cmd.OSD.JonSharedCmdOsd$DisableDayOSD/newBuilder)]
    (.build builder)))

(defn parse-disable-day-osd
  "Parse a DisableDayOSD protobuf message to a map."
  [^cmd.OSD.JonSharedCmdOsd$DisableDayOSD proto]
  (merge
    {}
))

(defn build-root
  "Build a Root protobuf message from a map."
  [m]
  (let [builder (cmd.Lrf_calib.JonSharedCmdLrfAlign$Root/newBuilder)]
    ;; Set oneof payload
    (when-let [payload (first (filter (fn [[k v]] (#{:day :heat} k)) m))]
      (build-root-payload builder payload))
    (.build builder)))


(defn build-root-payload
  "Set the oneof payload field."
  [builder [field-key field-value]]
  (case field-key
    :day (.setDay builder (build-offsets field-value))
    :heat (.setHeat builder (build-offsets field-value))
    (throw (ex-info "Unknown payload field" {:field field-key}))))

(defn parse-root
  "Parse a Root protobuf message to a map."
  [^cmd.Lrf_calib.JonSharedCmdLrfAlign$Root proto]
  (merge
    {}
    (parse-root-payload proto)))


(defn parse-root-payload
  "Parse the oneof payload."
  [proto]
  (case (.getPayloadCase proto)
    DAY {:day (parse-offsets (.getDay proto))}
    HEAT {:heat (parse-offsets (.getHeat proto))}
    {}))

(defn build-offsets
  "Build a Offsets protobuf message from a map."
  [m]
  (let [builder (cmd.Lrf_calib.JonSharedCmdLrfAlign$Offsets/newBuilder)]
    ;; Set oneof payload
    (when-let [payload (first (filter (fn [[k v]] (#{:set :save :reset :shift} k)) m))]
      (build-offsets-payload builder payload))
    (.build builder)))


(defn build-offsets-payload
  "Set the oneof payload field."
  [builder [field-key field-value]]
  (case field-key
    :set (.setSet builder (build-set-offsets field-value))
    :save (.setSave builder (build-save-offsets field-value))
    :reset (.setReset builder (build-reset-offsets field-value))
    :shift (.setShift builder (build-shift-offsets-by field-value))
    (throw (ex-info "Unknown payload field" {:field field-key}))))

(defn parse-offsets
  "Parse a Offsets protobuf message to a map."
  [^cmd.Lrf_calib.JonSharedCmdLrfAlign$Offsets proto]
  (merge
    {}
    (parse-offsets-payload proto)))


(defn parse-offsets-payload
  "Parse the oneof payload."
  [proto]
  (case (.getPayloadCase proto)
    SET {:set (parse-set-offsets (.getSet proto))}
    SAVE {:save (parse-save-offsets (.getSave proto))}
    RESET {:reset (parse-reset-offsets (.getReset proto))}
    SHIFT {:shift (parse-shift-offsets-by (.getShift proto))}
    {}))

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
  (merge
    {:x (.getX proto)
     :y (.getY proto)}
))

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
  (merge
    {:x (.getX proto)
     :y (.getY proto)}
))

(defn build-reset-offsets
  "Build a ResetOffsets protobuf message from a map."
  [m]
  (let [builder (cmd.Lrf_calib.JonSharedCmdLrfAlign$ResetOffsets/newBuilder)]
    (.build builder)))

(defn parse-reset-offsets
  "Parse a ResetOffsets protobuf message to a map."
  [^cmd.Lrf_calib.JonSharedCmdLrfAlign$ResetOffsets proto]
  (merge
    {}
))

(defn build-save-offsets
  "Build a SaveOffsets protobuf message from a map."
  [m]
  (let [builder (cmd.Lrf_calib.JonSharedCmdLrfAlign$SaveOffsets/newBuilder)]
    (.build builder)))

(defn parse-save-offsets
  "Parse a SaveOffsets protobuf message to a map."
  [^cmd.Lrf_calib.JonSharedCmdLrfAlign$SaveOffsets proto]
  (merge
    {}
))

(defn build-root
  "Build a Root protobuf message from a map."
  [m]
  (let [builder (cmd.System.JonSharedCmdSystem$Root/newBuilder)]
    ;; Set oneof payload
    (when-let [payload (first (filter (fn [[k v]] (#{:start-all :stop-all :reboot :power-off :localization :reset-configs :start-rec :stop-rec :mark-rec-important :unmark-rec-important :enter-transport :geodesic-mode-enable :geodesic-mode-disable} k)) m))]
      (build-root-payload builder payload))
    (.build builder)))


(defn build-root-payload
  "Set the oneof payload field."
  [builder [field-key field-value]]
  (case field-key
    :start-all (.setStartAll builder (build-start-a-ll field-value))
    :stop-all (.setStopAll builder (build-stop-a-ll field-value))
    :reboot (.setReboot builder (build-reboot field-value))
    :power-off (.setPowerOff builder (build-power-off field-value))
    :localization (.setLocalization builder (build-set-localization field-value))
    :reset-configs (.setResetConfigs builder (build-reset-configs field-value))
    :start-rec (.setStartRec builder (build-start-rec field-value))
    :stop-rec (.setStopRec builder (build-stop-rec field-value))
    :mark-rec-important (.setMarkRecImportant builder (build-mark-rec-important field-value))
    :unmark-rec-important (.setUnmarkRecImportant builder (build-unmark-rec-important field-value))
    :enter-transport (.setEnterTransport builder (build-enter-transport field-value))
    :geodesic-mode-enable (.setGeodesicModeEnable builder (build-enable-geodesic-mode field-value))
    :geodesic-mode-disable (.setGeodesicModeDisable builder (build-disable-geodesic-mode field-value))
    (throw (ex-info "Unknown payload field" {:field field-key}))))

(defn parse-root
  "Parse a Root protobuf message to a map."
  [^cmd.System.JonSharedCmdSystem$Root proto]
  (merge
    {}
    (parse-root-payload proto)))


(defn parse-root-payload
  "Parse the oneof payload."
  [proto]
  (case (.getPayloadCase proto)
    START_ALL {:start-all (parse-start-a-ll (.getStartAll proto))}
    STOP_ALL {:stop-all (parse-stop-a-ll (.getStopAll proto))}
    REBOOT {:reboot (parse-reboot (.getReboot proto))}
    POWER_OFF {:power-off (parse-power-off (.getPowerOff proto))}
    LOCALIZATION {:localization (parse-set-localization (.getLocalization proto))}
    RESET_CONFIGS {:reset-configs (parse-reset-configs (.getResetConfigs proto))}
    START_REC {:start-rec (parse-start-rec (.getStartRec proto))}
    STOP_REC {:stop-rec (parse-stop-rec (.getStopRec proto))}
    MARK_REC_IMPORTANT {:mark-rec-important (parse-mark-rec-important (.getMarkRecImportant proto))}
    UNMARK_REC_IMPORTANT {:unmark-rec-important (parse-unmark-rec-important (.getUnmarkRecImportant proto))}
    ENTER_TRANSPORT {:enter-transport (parse-enter-transport (.getEnterTransport proto))}
    GEODESIC_MODE_ENABLE {:geodesic-mode-enable (parse-enable-geodesic-mode (.getGeodesicModeEnable proto))}
    GEODESIC_MODE_DISABLE {:geodesic-mode-disable (parse-disable-geodesic-mode (.getGeodesicModeDisable proto))}
    {}))

(defn build-start-a-ll
  "Build a StartALl protobuf message from a map."
  [m]
  (let [builder (cmd.System.JonSharedCmdSystem$StartALl/newBuilder)]
    (.build builder)))

(defn parse-start-a-ll
  "Parse a StartALl protobuf message to a map."
  [^cmd.System.JonSharedCmdSystem$StartALl proto]
  (merge
    {}
))

(defn build-stop-a-ll
  "Build a StopALl protobuf message from a map."
  [m]
  (let [builder (cmd.System.JonSharedCmdSystem$StopALl/newBuilder)]
    (.build builder)))

(defn parse-stop-a-ll
  "Parse a StopALl protobuf message to a map."
  [^cmd.System.JonSharedCmdSystem$StopALl proto]
  (merge
    {}
))

(defn build-reboot
  "Build a Reboot protobuf message from a map."
  [m]
  (let [builder (cmd.System.JonSharedCmdSystem$Reboot/newBuilder)]
    (.build builder)))

(defn parse-reboot
  "Parse a Reboot protobuf message to a map."
  [^cmd.System.JonSharedCmdSystem$Reboot proto]
  (merge
    {}
))

(defn build-power-off
  "Build a PowerOff protobuf message from a map."
  [m]
  (let [builder (cmd.System.JonSharedCmdSystem$PowerOff/newBuilder)]
    (.build builder)))

(defn parse-power-off
  "Parse a PowerOff protobuf message to a map."
  [^cmd.System.JonSharedCmdSystem$PowerOff proto]
  (merge
    {}
))

(defn build-reset-configs
  "Build a ResetConfigs protobuf message from a map."
  [m]
  (let [builder (cmd.System.JonSharedCmdSystem$ResetConfigs/newBuilder)]
    (.build builder)))

(defn parse-reset-configs
  "Parse a ResetConfigs protobuf message to a map."
  [^cmd.System.JonSharedCmdSystem$ResetConfigs proto]
  (merge
    {}
))

(defn build-start-rec
  "Build a StartRec protobuf message from a map."
  [m]
  (let [builder (cmd.System.JonSharedCmdSystem$StartRec/newBuilder)]
    (.build builder)))

(defn parse-start-rec
  "Parse a StartRec protobuf message to a map."
  [^cmd.System.JonSharedCmdSystem$StartRec proto]
  (merge
    {}
))

(defn build-stop-rec
  "Build a StopRec protobuf message from a map."
  [m]
  (let [builder (cmd.System.JonSharedCmdSystem$StopRec/newBuilder)]
    (.build builder)))

(defn parse-stop-rec
  "Parse a StopRec protobuf message to a map."
  [^cmd.System.JonSharedCmdSystem$StopRec proto]
  (merge
    {}
))

(defn build-mark-rec-important
  "Build a MarkRecImportant protobuf message from a map."
  [m]
  (let [builder (cmd.System.JonSharedCmdSystem$MarkRecImportant/newBuilder)]
    (.build builder)))

(defn parse-mark-rec-important
  "Parse a MarkRecImportant protobuf message to a map."
  [^cmd.System.JonSharedCmdSystem$MarkRecImportant proto]
  (merge
    {}
))

(defn build-unmark-rec-important
  "Build a UnmarkRecImportant protobuf message from a map."
  [m]
  (let [builder (cmd.System.JonSharedCmdSystem$UnmarkRecImportant/newBuilder)]
    (.build builder)))

(defn parse-unmark-rec-important
  "Parse a UnmarkRecImportant protobuf message to a map."
  [^cmd.System.JonSharedCmdSystem$UnmarkRecImportant proto]
  (merge
    {}
))

(defn build-enter-transport
  "Build a EnterTransport protobuf message from a map."
  [m]
  (let [builder (cmd.System.JonSharedCmdSystem$EnterTransport/newBuilder)]
    (.build builder)))

(defn parse-enter-transport
  "Parse a EnterTransport protobuf message to a map."
  [^cmd.System.JonSharedCmdSystem$EnterTransport proto]
  (merge
    {}
))

(defn build-enable-geodesic-mode
  "Build a EnableGeodesicMode protobuf message from a map."
  [m]
  (let [builder (cmd.System.JonSharedCmdSystem$EnableGeodesicMode/newBuilder)]
    (.build builder)))

(defn parse-enable-geodesic-mode
  "Parse a EnableGeodesicMode protobuf message to a map."
  [^cmd.System.JonSharedCmdSystem$EnableGeodesicMode proto]
  (merge
    {}
))

(defn build-disable-geodesic-mode
  "Build a DisableGeodesicMode protobuf message from a map."
  [m]
  (let [builder (cmd.System.JonSharedCmdSystem$DisableGeodesicMode/newBuilder)]
    (.build builder)))

(defn parse-disable-geodesic-mode
  "Parse a DisableGeodesicMode protobuf message to a map."
  [^cmd.System.JonSharedCmdSystem$DisableGeodesicMode proto]
  (merge
    {}
))

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
  (merge
    {:loc (.getLoc proto)}
))

(defn build-root
  "Build a Root protobuf message from a map."
  [m]
  (let [builder (cmd.CV.JonSharedCmdCv$Root/newBuilder)]
    ;; Set oneof payload
    (when-let [payload (first (filter (fn [[k v]] (#{:set-auto-focus :start-track-ndc :stop-track :vampire-mode-enable :vampire-mode-disable :stabilization-mode-enable :stabilization-mode-disable :dump-start :dump-stop} k)) m))]
      (build-root-payload builder payload))
    (.build builder)))


(defn build-root-payload
  "Set the oneof payload field."
  [builder [field-key field-value]]
  (case field-key
    :set-auto-focus (.setSetAutoFocus builder (build-set-auto-focus field-value))
    :start-track-ndc (.setStartTrackNdc builder (build-start-track-ndc field-value))
    :stop-track (.setStopTrack builder (build-stop-track field-value))
    :vampire-mode-enable (.setVampireModeEnable builder (build-vampire-mode-enable field-value))
    :vampire-mode-disable (.setVampireModeDisable builder (build-vampire-mode-disable field-value))
    :stabilization-mode-enable (.setStabilizationModeEnable builder (build-stabilization-mode-enable field-value))
    :stabilization-mode-disable (.setStabilizationModeDisable builder (build-stabilization-mode-disable field-value))
    :dump-start (.setDumpStart builder (build-dump-start field-value))
    :dump-stop (.setDumpStop builder (build-dump-stop field-value))
    (throw (ex-info "Unknown payload field" {:field field-key}))))

(defn parse-root
  "Parse a Root protobuf message to a map."
  [^cmd.CV.JonSharedCmdCv$Root proto]
  (merge
    {}
    (parse-root-payload proto)))


(defn parse-root-payload
  "Parse the oneof payload."
  [proto]
  (case (.getPayloadCase proto)
    SET_AUTO_FOCUS {:set-auto-focus (parse-set-auto-focus (.getSetAutoFocus proto))}
    START_TRACK_NDC {:start-track-ndc (parse-start-track-ndc (.getStartTrackNdc proto))}
    STOP_TRACK {:stop-track (parse-stop-track (.getStopTrack proto))}
    VAMPIRE_MODE_ENABLE {:vampire-mode-enable (parse-vampire-mode-enable (.getVampireModeEnable proto))}
    VAMPIRE_MODE_DISABLE {:vampire-mode-disable (parse-vampire-mode-disable (.getVampireModeDisable proto))}
    STABILIZATION_MODE_ENABLE {:stabilization-mode-enable (parse-stabilization-mode-enable (.getStabilizationModeEnable proto))}
    STABILIZATION_MODE_DISABLE {:stabilization-mode-disable (parse-stabilization-mode-disable (.getStabilizationModeDisable proto))}
    DUMP_START {:dump-start (parse-dump-start (.getDumpStart proto))}
    DUMP_STOP {:dump-stop (parse-dump-stop (.getDumpStop proto))}
    {}))

(defn build-vampire-mode-enable
  "Build a VampireModeEnable protobuf message from a map."
  [m]
  (let [builder (cmd.CV.JonSharedCmdCv$VampireModeEnable/newBuilder)]
    (.build builder)))

(defn parse-vampire-mode-enable
  "Parse a VampireModeEnable protobuf message to a map."
  [^cmd.CV.JonSharedCmdCv$VampireModeEnable proto]
  (merge
    {}
))

(defn build-dump-start
  "Build a DumpStart protobuf message from a map."
  [m]
  (let [builder (cmd.CV.JonSharedCmdCv$DumpStart/newBuilder)]
    (.build builder)))

(defn parse-dump-start
  "Parse a DumpStart protobuf message to a map."
  [^cmd.CV.JonSharedCmdCv$DumpStart proto]
  (merge
    {}
))

(defn build-dump-stop
  "Build a DumpStop protobuf message from a map."
  [m]
  (let [builder (cmd.CV.JonSharedCmdCv$DumpStop/newBuilder)]
    (.build builder)))

(defn parse-dump-stop
  "Parse a DumpStop protobuf message to a map."
  [^cmd.CV.JonSharedCmdCv$DumpStop proto]
  (merge
    {}
))

(defn build-vampire-mode-disable
  "Build a VampireModeDisable protobuf message from a map."
  [m]
  (let [builder (cmd.CV.JonSharedCmdCv$VampireModeDisable/newBuilder)]
    (.build builder)))

(defn parse-vampire-mode-disable
  "Parse a VampireModeDisable protobuf message to a map."
  [^cmd.CV.JonSharedCmdCv$VampireModeDisable proto]
  (merge
    {}
))

(defn build-stabilization-mode-enable
  "Build a StabilizationModeEnable protobuf message from a map."
  [m]
  (let [builder (cmd.CV.JonSharedCmdCv$StabilizationModeEnable/newBuilder)]
    (.build builder)))

(defn parse-stabilization-mode-enable
  "Parse a StabilizationModeEnable protobuf message to a map."
  [^cmd.CV.JonSharedCmdCv$StabilizationModeEnable proto]
  (merge
    {}
))

(defn build-stabilization-mode-disable
  "Build a StabilizationModeDisable protobuf message from a map."
  [m]
  (let [builder (cmd.CV.JonSharedCmdCv$StabilizationModeDisable/newBuilder)]
    (.build builder)))

(defn parse-stabilization-mode-disable
  "Parse a StabilizationModeDisable protobuf message to a map."
  [^cmd.CV.JonSharedCmdCv$StabilizationModeDisable proto]
  (merge
    {}
))

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
  (merge
    {:channel (.getChannel proto)
     :value (.getValue proto)}
))

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
  (merge
    {:channel (.getChannel proto)
     :x (.getX proto)
     :y (.getY proto)
     :frame-time (.getFrameTime proto)}
))

(defn build-stop-track
  "Build a StopTrack protobuf message from a map."
  [m]
  (let [builder (cmd.CV.JonSharedCmdCv$StopTrack/newBuilder)]
    (.build builder)))

(defn parse-stop-track
  "Parse a StopTrack protobuf message to a map."
  [^cmd.CV.JonSharedCmdCv$StopTrack proto]
  (merge
    {}
))

(defn build-root
  "Build a Root protobuf message from a map."
  [m]
  (let [builder (cmd.DayCamGlassHeater.JonSharedCmdDayCamGlassHeater$Root/newBuilder)]
    ;; Set oneof payload
    (when-let [payload (first (filter (fn [[k v]] (#{:start :stop :turn-on :turn-off :get-meteo} k)) m))]
      (build-root-payload builder payload))
    (.build builder)))


(defn build-root-payload
  "Set the oneof payload field."
  [builder [field-key field-value]]
  (case field-key
    :start (.setStart builder (build-start field-value))
    :stop (.setStop builder (build-stop field-value))
    :turn-on (.setTurnOn builder (build-turn-on field-value))
    :turn-off (.setTurnOff builder (build-turn-off field-value))
    :get-meteo (.setGetMeteo builder (build-get-meteo field-value))
    (throw (ex-info "Unknown payload field" {:field field-key}))))

(defn parse-root
  "Parse a Root protobuf message to a map."
  [^cmd.DayCamGlassHeater.JonSharedCmdDayCamGlassHeater$Root proto]
  (merge
    {}
    (parse-root-payload proto)))


(defn parse-root-payload
  "Parse the oneof payload."
  [proto]
  (case (.getPayloadCase proto)
    START {:start (parse-start (.getStart proto))}
    STOP {:stop (parse-stop (.getStop proto))}
    TURN_ON {:turn-on (parse-turn-on (.getTurnOn proto))}
    TURN_OFF {:turn-off (parse-turn-off (.getTurnOff proto))}
    GET_METEO {:get-meteo (parse-get-meteo (.getGetMeteo proto))}
    {}))

(defn build-start
  "Build a Start protobuf message from a map."
  [m]
  (let [builder (cmd.DayCamGlassHeater.JonSharedCmdDayCamGlassHeater$Start/newBuilder)]
    (.build builder)))

(defn parse-start
  "Parse a Start protobuf message to a map."
  [^cmd.DayCamGlassHeater.JonSharedCmdDayCamGlassHeater$Start proto]
  (merge
    {}
))

(defn build-stop
  "Build a Stop protobuf message from a map."
  [m]
  (let [builder (cmd.DayCamGlassHeater.JonSharedCmdDayCamGlassHeater$Stop/newBuilder)]
    (.build builder)))

(defn parse-stop
  "Parse a Stop protobuf message to a map."
  [^cmd.DayCamGlassHeater.JonSharedCmdDayCamGlassHeater$Stop proto]
  (merge
    {}
))

(defn build-turn-on
  "Build a TurnOn protobuf message from a map."
  [m]
  (let [builder (cmd.DayCamGlassHeater.JonSharedCmdDayCamGlassHeater$TurnOn/newBuilder)]
    (.build builder)))

(defn parse-turn-on
  "Parse a TurnOn protobuf message to a map."
  [^cmd.DayCamGlassHeater.JonSharedCmdDayCamGlassHeater$TurnOn proto]
  (merge
    {}
))

(defn build-turn-off
  "Build a TurnOff protobuf message from a map."
  [m]
  (let [builder (cmd.DayCamGlassHeater.JonSharedCmdDayCamGlassHeater$TurnOff/newBuilder)]
    (.build builder)))

(defn parse-turn-off
  "Parse a TurnOff protobuf message to a map."
  [^cmd.DayCamGlassHeater.JonSharedCmdDayCamGlassHeater$TurnOff proto]
  (merge
    {}
))

(defn build-get-meteo
  "Build a GetMeteo protobuf message from a map."
  [m]
  (let [builder (cmd.DayCamGlassHeater.JonSharedCmdDayCamGlassHeater$GetMeteo/newBuilder)]
    (.build builder)))

(defn parse-get-meteo
  "Parse a GetMeteo protobuf message to a map."
  [^cmd.DayCamGlassHeater.JonSharedCmdDayCamGlassHeater$GetMeteo proto]
  (merge
    {}
))

(defn build-root
  "Build a Root protobuf message from a map."
  [m]
  (let [builder (cmd.Lira.JonSharedCmdLira$Root/newBuilder)]
    ;; Set oneof payload
    (when-let [payload (first (filter (fn [[k v]] (#{:refine-target} k)) m))]
      (build-root-payload builder payload))
    (.build builder)))


(defn build-root-payload
  "Set the oneof payload field."
  [builder [field-key field-value]]
  (case field-key
    :refine-target (.setRefineTarget builder (build-refine-target field-value))
    (throw (ex-info "Unknown payload field" {:field field-key}))))

(defn parse-root
  "Parse a Root protobuf message to a map."
  [^cmd.Lira.JonSharedCmdLira$Root proto]
  (merge
    {}
    (parse-root-payload proto)))


(defn parse-root-payload
  "Parse the oneof payload."
  [proto]
  (case (.getPayloadCase proto)
    REFINE_TARGET {:refine-target (parse-refine-target (.getRefineTarget proto))}
    {}))

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
  (merge
    {:target (.getTarget proto)}
))

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
  (merge
    {:timestamp (.getTimestamp proto)
     :target-longitude (.getTargetLongitude proto)
     :target-latitude (.getTargetLatitude proto)
     :target-altitude (.getTargetAltitude proto)
     :target-azimuth (.getTargetAzimuth proto)
     :target-elevation (.getTargetElevation proto)
     :distance (.getDistance proto)
     :uuid-part-1 (.getUuidPart1 proto)
     :uuid-part-2 (.getUuidPart2 proto)
     :uuid-part-3 (.getUuidPart3 proto)
     :uuid-part-4 (.getUuidPart4 proto)}
))

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
    ;; Set oneof payload
    (when-let [payload (first (filter (fn [[k v]] (#{:day-camera :heat-camera :gps :compass :lrf :lrf-calib :rotary :osd :ping :noop :frozen :system :cv :day-cam-glass-heater :lira} k)) m))]
      (build-root-payload builder payload))
    (.build builder)))


(defn build-root-payload
  "Set the oneof payload field."
  [builder [field-key field-value]]
  (case field-key
    :day-camera (.setDayCamera builder (build-root field-value))
    :heat-camera (.setHeatCamera builder (build-root field-value))
    :gps (.setGps builder (build-root field-value))
    :compass (.setCompass builder (build-root field-value))
    :lrf (.setLrf builder (build-root field-value))
    :lrf-calib (.setLrfCalib builder (build-root field-value))
    :rotary (.setRotary builder (build-root field-value))
    :osd (.setOsd builder (build-root field-value))
    :ping (.setPing builder (build-ping field-value))
    :noop (.setNoop builder (build-noop field-value))
    :frozen (.setFrozen builder (build-frozen field-value))
    :system (.setSystem builder (build-root field-value))
    :cv (.setCv builder (build-root field-value))
    :day-cam-glass-heater (.setDayCamGlassHeater builder (build-root field-value))
    :lira (.setLira builder (build-root field-value))
    (throw (ex-info "Unknown payload field" {:field field-key}))))

(defn parse-root
  "Parse a Root protobuf message to a map."
  [^cmd.JonSharedCmd$Root proto]
  (merge
    {:protocol-version (.getProtocolVersion proto)
     :session-id (.getSessionId proto)
     :important (.getImportant proto)
     :from-cv-subsystem (.getFromCvSubsystem proto)
     :client-type (.getClientType proto)}
    (parse-root-payload proto)))


(defn parse-root-payload
  "Parse the oneof payload."
  [proto]
  (case (.getPayloadCase proto)
    DAY_CAMERA {:day-camera (parse-root (.getDayCamera proto))}
    HEAT_CAMERA {:heat-camera (parse-root (.getHeatCamera proto))}
    GPS {:gps (parse-root (.getGps proto))}
    COMPASS {:compass (parse-root (.getCompass proto))}
    LRF {:lrf (parse-root (.getLrf proto))}
    LRF_CALIB {:lrf-calib (parse-root (.getLrfCalib proto))}
    ROTARY {:rotary (parse-root (.getRotary proto))}
    OSD {:osd (parse-root (.getOsd proto))}
    PING {:ping (parse-ping (.getPing proto))}
    NOOP {:noop (parse-noop (.getNoop proto))}
    FROZEN {:frozen (parse-frozen (.getFrozen proto))}
    SYSTEM {:system (parse-root (.getSystem proto))}
    CV {:cv (parse-root (.getCv proto))}
    DAY_CAM_GLASS_HEATER {:day-cam-glass-heater (parse-root (.getDayCamGlassHeater proto))}
    LIRA {:lira (parse-root (.getLira proto))}
    {}))

(defn build-ping
  "Build a Ping protobuf message from a map."
  [m]
  (let [builder (cmd.JonSharedCmd$Ping/newBuilder)]
    (.build builder)))

(defn parse-ping
  "Parse a Ping protobuf message to a map."
  [^cmd.JonSharedCmd$Ping proto]
  (merge
    {}
))

(defn build-noop
  "Build a Noop protobuf message from a map."
  [m]
  (let [builder (cmd.JonSharedCmd$Noop/newBuilder)]
    (.build builder)))

(defn parse-noop
  "Parse a Noop protobuf message to a map."
  [^cmd.JonSharedCmd$Noop proto]
  (merge
    {}
))

(defn build-frozen
  "Build a Frozen protobuf message from a map."
  [m]
  (let [builder (cmd.JonSharedCmd$Frozen/newBuilder)]
    (.build builder)))

(defn parse-frozen
  "Parse a Frozen protobuf message to a map."
  [^cmd.JonSharedCmd$Frozen proto]
  (merge
    {}
))
