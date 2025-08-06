(ns test.proto.separated.ser.data
  "Generated protobuf functions."
  (:require [com.fulcrologic.guardrails.malli.core :refer [=> >defn >defn- ?]]
            [malli.core :as m])
  (:import ser.JonSharedData$JonGUIState))

;; =============================================================================
;; Enums
;; =============================================================================

;; No enums

;; =============================================================================
;; Malli Specs
;; =============================================================================

(def jon-gui-state-spec
  "Malli spec for jon-gui-state message"
  [:map [:protocol-version [:maybe :int]]
   [:system [:maybe :ser/jon-gui-data-system]]
   [:meteo-internal [:maybe :ser/jon-gui-data-meteo]]
   [:lrf [:maybe :ser/jon-gui-data-lrf]] [:time [:maybe :ser/jon-gui-data-time]]
   [:gps [:maybe :ser/jon-gui-data-gps]]
   [:compass [:maybe :ser/jon-gui-data-compass]]
   [:rotary [:maybe :ser/jon-gui-data-rotary]]
   [:camera-day [:maybe :ser/jon-gui-data-camera-day]]
   [:camera-heat [:maybe :ser/jon-gui-data-camera-heat]]
   [:compass-calibration [:maybe :ser/jon-gui-data-compass-calibration]]
   [:rec-osd [:maybe :ser/jon-gui-data-rec-osd]]
   [:day-cam-glass-heater [:maybe :ser/jon-gui-data-day-cam-glass-heater]]
   [:actual-space-time [:maybe :ser/jon-gui-data-actual-space-time]]])

;; =============================================================================
;; Builders and Parsers
;; =============================================================================

;; Forward declarations
(declare build-jon-gui-state)
(declare parse-jon-gui-state)

(>defn
  build-jon-gui-state
  "Build a JonGUIState protobuf message from a map."
  [m]
  [jon-gui-state-spec => #(instance? ser.JonSharedData$JonGUIState %)]
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

(>defn
  parse-jon-gui-state
  "Parse a JonGUIState protobuf message to a map."
  [^ser.JonSharedData$JonGUIState proto]
  [#(instance? ser.JonSharedData$JonGUIState %) => jon-gui-state-spec]
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