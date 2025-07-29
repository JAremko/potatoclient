(ns potatoclient.state.proto-test-helper
  "Test helper for creating protobuf messages from EDN data.
  This is only for testing - production code doesn't need EDN->proto conversion."
  (:require [clojure.string :as str]
            [com.fulcrologic.guardrails.malli.core :refer [=> >defn >defn- ?]]
            [potatoclient.proto :as proto])
  (:import (ser JonSharedData$JonGUIState
                JonSharedDataSystem$JonGuiDataSystem
                JonSharedDataTime$JonGuiDataTime
                JonSharedDataGps$JonGuiDataGps
                JonSharedDataCompass$JonGuiDataCompass
                JonSharedDataRotary$JonGuiDataRotary
                JonSharedDataLrf$JonGuiDataLrf
                JonSharedDataCameraDay$JonGuiDataCameraDay
                JonSharedDataCameraHeat$JonGuiDataCameraHeat
                JonSharedDataCompassCalibration$JonGuiDataCompassCalibration
                JonSharedDataRecOsd$JonGuiDataRecOsd
                JonSharedDataDayCamGlassHeater$JonGuiDataDayCamGlassHeater
                JonSharedDataActualSpaceTime$JonGuiDataActualSpaceTime)))

(>defn- set-field-if-present
  "Set a field on a builder if the value is present in the map"
  [builder field-name value]
  [any? string? any? => nil?]
  (when (some? value)
    (let [setter-name (str "set" (str/capitalize field-name))
          method (.getMethod (.getClass builder) setter-name (into-array Class [(type value)]))]
      (.invoke method builder (into-array Object [value]))))
  nil)

(>defn edn->binary-for-test
  "Convert an EDN state map to binary protobuf for testing.
  This is a simplified implementation that only handles the fields we need for tests."
  [state-map]
  [map? => bytes?]
  (let [builder (JonSharedData$JonGUIState/newBuilder)]
    ;; Set protocol version
    (when-let [pv (:protocol-version state-map)]
      (.setProtocolVersion builder pv))
    
    ;; Handle system subsystem
    (when-let [system (:system state-map)]
      (let [system-builder (.getSystemBuilder builder)]
        (when-let [v (:cpu-temperature system)] (.setCpuTemperature system-builder v))
        (when-let [v (:gpu-temperature system)] (.setGpuTemperature system-builder v))
        (when-let [v (:cpu-load system)] (.setCpuLoad system-builder v))
        (when-let [v (:gpu-load system)] (.setGpuLoad system-builder v))
        (when-let [v (:power-consumption system)] (.setPowerConsumption system-builder v))
        (when-let [v (:disk-space system)] (.setDiskSpace system-builder v))
        (when-let [v (:rec-enabled system)] (.setRecEnabled system-builder v))
        (when-let [v (:important-rec-enabled system)] (.setImportantRecEnabled system-builder v))
        (when-let [v (:low-disk-space system)] (.setLowDiskSpace system-builder v))
        (when-let [v (:no-disk-space system)] (.setNoDiskSpace system-builder v))
        (when-let [v (:tracking system)] (.setTracking system-builder v))
        (when-let [v (:vampire-mode system)] (.setVampireMode system-builder v))
        (when-let [v (:stabilization-mode system)] (.setStabilizationMode system-builder v))
        (when-let [v (:geodesic-mode system)] (.setGeodesicMode system-builder v))
        (when-let [v (:cv-dumping system)] (.setCvDumping system-builder v))
        (when-let [v (:cur-video-rec-dir-year system)] (.setCurVideoRecDirYear system-builder v))
        (when-let [v (:cur-video-rec-dir-month system)] (.setCurVideoRecDirMonth system-builder v))
        (when-let [v (:cur-video-rec-dir-day system)] (.setCurVideoRecDirDay system-builder v))
        (when-let [v (:cur-video-rec-dir-hour system)] (.setCurVideoRecDirHour system-builder v))
        (when-let [v (:cur-video-rec-dir-minute system)] (.setCurVideoRecDirMinute system-builder v))
        (when-let [v (:cur-video-rec-dir-second system)] (.setCurVideoRecDirSecond system-builder v))))
    
    ;; Handle time subsystem
    (when-let [time (:time state-map)]
      (let [time-builder (.getTimeBuilder builder)]
        (when-let [v (:timestamp time)] (.setTimestamp time-builder v))
        (when-let [v (:manual-timestamp time)] (.setManualTimestamp time-builder v))
        (when-let [v (:zone-id time)] (.setZoneId time-builder v))
        (when-let [v (:use-manual-time time)] (.setUseManualTime time-builder v))))
    
    ;; Handle GPS subsystem
    (when-let [gps (:gps state-map)]
      (let [gps-builder (.getGpsBuilder builder)]
        (when-let [v (:longitude gps)] (.setLongitude gps-builder v))
        (when-let [v (:latitude gps)] (.setLatitude gps-builder v))
        (when-let [v (:altitude gps)] (.setAltitude gps-builder v))
        (when-let [v (:manual-longitude gps)] (.setManualLongitude gps-builder v))
        (when-let [v (:manual-latitude gps)] (.setManualLatitude gps-builder v))
        (when-let [v (:manual-altitude gps)] (.setManualAltitude gps-builder v))
        (when-let [v (:use-manual gps)] (.setUseManual gps-builder v))))
    
    ;; Handle compass subsystem
    (when-let [compass (:compass state-map)]
      (let [compass-builder (.getCompassBuilder builder)]
        (when-let [v (:azimuth compass)] (.setAzimuth compass-builder v))
        (when-let [v (:elevation compass)] (.setElevation compass-builder v))
        (when-let [v (:bank compass)] (.setBank compass-builder v))
        (when-let [v (:offset-azimuth compass)] (.setOffsetAzimuth compass-builder v))
        (when-let [v (:offset-elevation compass)] (.setOffsetElevation compass-builder v))
        (when-let [v (:magnetic-declination compass)] (.setMagneticDeclination compass-builder v))
        (when-let [v (:calibrating compass)] (.setCalibrating compass-builder v))))
    
    (.toByteArray (.build builder))))