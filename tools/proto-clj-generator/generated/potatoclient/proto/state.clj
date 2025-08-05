(ns potatoclient.proto.state
  "Generated protobuf conversion functions."
  (:require [clojure.string :as str])
  (:import
    [ser JonSharedData$JonGUIState]))

;; Forward declarations
(declare build-jon-gui-state parse-jon-gui-state)

;; Message Converters
(defn build-jon-gui-state
  "Build a JonGUIState protobuf message from a map."
  [m]
  (let [builder (ser.JonSharedData$JonGUIState/newBuilder)]
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
  [^ser.JonSharedData$JonGUIState proto]
  (merge
    {:protocol-version (.getProtocolVersion proto)
     :system (.getSystem proto)
     :meteo-internal (.getMeteoInternal proto)
     :lrf (.getLrf proto)
     :time (.getTime proto)
     :gps (.getGps proto)
     :compass (.getCompass proto)
     :rotary (.getRotary proto)
     :camera-day (.getCameraDay proto)
     :camera-heat (.getCameraHeat proto)
     :compass-calibration (.getCompassCalibration proto)
     :rec-osd (.getRecOsd proto)
     :day-cam-glass-heater (.getDayCamGlassHeater proto)
     :actual-space-time (.getActualSpaceTime proto)}
))
