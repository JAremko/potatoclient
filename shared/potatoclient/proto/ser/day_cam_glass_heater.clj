(ns potatoclient.proto.ser.day-cam-glass-heater
  "Generated protobuf functions."
  (:require [com.fulcrologic.guardrails.malli.core :refer [=> >defn >defn- ?]]
            [malli.core :as m])
  (:import ser.JonSharedDataDayCamGlassHeater$JonGuiDataDayCamGlassHeater))

;; =============================================================================
;; Enums
;; =============================================================================

;; No enums

;; =============================================================================
;; Malli Specs
;; =============================================================================

(def jon-gui-data-day-cam-glass-heater-spec
  "Malli spec for jon-gui-data-day-cam-glass-heater message"
  [:map [:temperature [:maybe :double]] [:status [:maybe :boolean]]])

;; =============================================================================
;; Builders and Parsers
;; =============================================================================

;; Forward declarations
(declare build-jon-gui-data-day-cam-glass-heater)
(declare parse-jon-gui-data-day-cam-glass-heater)

(>defn
  build-jon-gui-data-day-cam-glass-heater
  "Build a JonGuiDataDayCamGlassHeater protobuf message from a map."
  [m]
  [jon-gui-data-day-cam-glass-heater-spec =>
   #(instance? ser.JonSharedDataDayCamGlassHeater$JonGuiDataDayCamGlassHeater
               %)]
  (let
    [builder
       (ser.JonSharedDataDayCamGlassHeater$JonGuiDataDayCamGlassHeater/newBuilder)]
    ;; Set regular fields
    (when (contains? m :temperature)
      (.setTemperature builder (get m :temperature)))
    (when (contains? m :status) (.setStatus builder (get m :status)))
    (.build builder)))

(>defn parse-jon-gui-data-day-cam-glass-heater
       "Parse a JonGuiDataDayCamGlassHeater protobuf message to a map."
       [^ser.JonSharedDataDayCamGlassHeater$JonGuiDataDayCamGlassHeater proto]
       [#(instance?
           ser.JonSharedDataDayCamGlassHeater$JonGuiDataDayCamGlassHeater
           %) => jon-gui-data-day-cam-glass-heater-spec]
       (cond-> {}
         ;; Regular fields
         true (assoc :temperature (.getTemperature proto))
         true (assoc :status (.getStatus proto))))