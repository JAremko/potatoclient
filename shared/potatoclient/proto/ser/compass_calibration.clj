(ns potatoclient.proto.ser.compass-calibration
  "Generated protobuf functions."
  (:require [com.fulcrologic.guardrails.malli.core :refer [=> >defn >defn- ?]]
            [malli.core :as m]
            [potatoclient.proto.ser.types :as types])
  (:import ser.JonSharedDataCompassCalibration$JonGuiDataCompassCalibration))

;; =============================================================================
;; Enums
;; =============================================================================

;; No enums

;; =============================================================================
;; Malli Specs
;; =============================================================================

(def jon-gui-data-compass-calibration-spec
  "Malli spec for jon-gui-data-compass-calibration message"
  [:map [:stage [:maybe :int]] [:final-stage [:maybe :int]]
   [:target-azimuth [:maybe :double]] [:target-elevation [:maybe :double]]
   [:target-bank [:maybe :double]]
   [:status [:maybe :ser/jon-gui-data-compass-calibrate-status]]])

;; =============================================================================
;; Builders and Parsers
;; =============================================================================

;; Forward declarations
(declare build-jon-gui-data-compass-calibration)
(declare parse-jon-gui-data-compass-calibration)

(>defn
  build-jon-gui-data-compass-calibration
  "Build a JonGuiDataCompassCalibration protobuf message from a map."
  [m]
  [jon-gui-data-compass-calibration-spec =>
   #(instance? ser.JonSharedDataCompassCalibration$JonGuiDataCompassCalibration
               %)]
  (let
    [builder
       (ser.JonSharedDataCompassCalibration$JonGuiDataCompassCalibration/newBuilder)]
    ;; Set regular fields
    (when (contains? m :stage) (.setStage builder (get m :stage)))
    (when (contains? m :final-stage)
      (.setFinalStage builder (get m :final-stage)))
    (when (contains? m :target-azimuth)
      (.setTargetAzimuth builder (get m :target-azimuth)))
    (when (contains? m :target-elevation)
      (.setTargetElevation builder (get m :target-elevation)))
    (when (contains? m :target-bank)
      (.setTargetBank builder (get m :target-bank)))
    (when (contains? m :status)
      (.setStatus builder
                  (get jon-gui-data-compass-calibrate-status-values
                       (get m :status))))
    (.build builder)))

(>defn parse-jon-gui-data-compass-calibration
       "Parse a JonGuiDataCompassCalibration protobuf message to a map."
       [^ser.JonSharedDataCompassCalibration$JonGuiDataCompassCalibration proto]
       [#(instance?
           ser.JonSharedDataCompassCalibration$JonGuiDataCompassCalibration
           %) => jon-gui-data-compass-calibration-spec]
       (cond-> {}
         ;; Regular fields
         true (assoc :stage (.getStage proto))
         true (assoc :final-stage (.getFinalStage proto))
         true (assoc :target-azimuth (.getTargetAzimuth proto))
         true (assoc :target-elevation (.getTargetElevation proto))
         true (assoc :target-bank (.getTargetBank proto))
         true (assoc :status
                (get jon-gui-data-compass-calibrate-status-keywords
                     (.getStatus proto)))))