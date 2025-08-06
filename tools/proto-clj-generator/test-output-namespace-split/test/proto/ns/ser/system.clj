(ns test.proto.ns.ser.system
  "Generated protobuf functions."
  (:require [com.fulcrologic.guardrails.malli.core :refer [=> >defn >defn- ?]]
            [malli.core :as m])
  (:import ser.JonSharedDataSystem$JonGuiDataSystem))

;; =============================================================================
;; Enums
;; =============================================================================

;; No enums

;; =============================================================================
;; Malli Specs
;; =============================================================================

(def jon-gui-data-system-spec
  "Malli spec for jon-gui-data-system message"
  [:map [:cpu-temperature [:maybe :float]] [:gpu-temperature [:maybe :float]]
   [:gpu-load [:maybe :float]] [:cpu-load [:maybe :float]]
   [:power-consumption [:maybe :float]]
   [:loc [:maybe :ser/jon-gui-data-system-localizations]]
   [:cur-video-rec-dir-year [:maybe :int]]
   [:cur-video-rec-dir-month [:maybe :int]]
   [:cur-video-rec-dir-day [:maybe :int]]
   [:cur-video-rec-dir-hour [:maybe :int]]
   [:cur-video-rec-dir-minute [:maybe :int]]
   [:cur-video-rec-dir-second [:maybe :int]] [:rec-enabled [:maybe :boolean]]
   [:important-rec-enabled [:maybe :boolean]]
   [:low-disk-space [:maybe :boolean]] [:no-disk-space [:maybe :boolean]]
   [:disk-space [:maybe :int]] [:tracking [:maybe :boolean]]
   [:vampire-mode [:maybe :boolean]] [:stabilization-mode [:maybe :boolean]]
   [:geodesic-mode [:maybe :boolean]] [:cv-dumping [:maybe :boolean]]])

;; =============================================================================
;; Builders and Parsers
;; =============================================================================

;; Forward declarations
(declare build-jon-gui-data-system)
(declare parse-jon-gui-data-system)

(>defn
  build-jon-gui-data-system
  "Build a JonGuiDataSystem protobuf message from a map."
  [m]
  [jon-gui-data-system-spec =>
   #(instance? ser.JonSharedDataSystem$JonGuiDataSystem %)]
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
               (get jon-gui-data-system-localizations-values (get m :loc))))
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

(>defn parse-jon-gui-data-system
       "Parse a JonGuiDataSystem protobuf message to a map."
       [^ser.JonSharedDataSystem$JonGuiDataSystem proto]
       [#(instance? ser.JonSharedDataSystem$JonGuiDataSystem %) =>
        jon-gui-data-system-spec]
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