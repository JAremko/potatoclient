(ns test.deps.ser.compass
  "Generated protobuf functions."
  (:require [com.fulcrologic.guardrails.malli.core :refer [=> >defn >defn- ?]]
            [malli.core :as m])
  (:import ser.JonSharedDataCompass$JonGuiDataCompass))

;; =============================================================================
;; Enums
;; =============================================================================

;; No enums

;; =============================================================================
;; Malli Specs
;; =============================================================================

(def jon-gui-data-compass-spec
  "Malli spec for jon-gui-data-compass message"
  [:map [:azimuth [:maybe :double]] [:elevation [:maybe :double]]
   [:bank [:maybe :double]] [:offset-azimuth [:maybe :double]]
   [:offset-elevation [:maybe :double]] [:magnetic-declination [:maybe :double]]
   [:calibrating [:maybe :boolean]]])

;; =============================================================================
;; Builders and Parsers
;; =============================================================================

;; Forward declarations
(declare build-jon-gui-data-compass)
(declare parse-jon-gui-data-compass)

(>defn build-jon-gui-data-compass
       "Build a JonGuiDataCompass protobuf message from a map."
       [m]
       [jon-gui-data-compass-spec =>
        #(instance? ser.JonSharedDataCompass$JonGuiDataCompass %)]
       (let [builder (ser.JonSharedDataCompass$JonGuiDataCompass/newBuilder)]
         ;; Set regular fields
         (when (contains? m :azimuth) (.setAzimuth builder (get m :azimuth)))
         (when (contains? m :elevation)
           (.setElevation builder (get m :elevation)))
         (when (contains? m :bank) (.setBank builder (get m :bank)))
         (when (contains? m :offset-azimuth)
           (.setOffsetAzimuth builder (get m :offset-azimuth)))
         (when (contains? m :offset-elevation)
           (.setOffsetElevation builder (get m :offset-elevation)))
         (when (contains? m :magnetic-declination)
           (.setMagneticDeclination builder (get m :magnetic-declination)))
         (when (contains? m :calibrating)
           (.setCalibrating builder (get m :calibrating)))
         (.build builder)))

(>defn parse-jon-gui-data-compass
       "Parse a JonGuiDataCompass protobuf message to a map."
       [^ser.JonSharedDataCompass$JonGuiDataCompass proto]
       [#(instance? ser.JonSharedDataCompass$JonGuiDataCompass %) =>
        jon-gui-data-compass-spec]
       (cond-> {}
         ;; Regular fields
         true (assoc :azimuth (.getAzimuth proto))
         true (assoc :elevation (.getElevation proto))
         true (assoc :bank (.getBank proto))
         true (assoc :offset-azimuth (.getOffsetAzimuth proto))
         true (assoc :offset-elevation (.getOffsetElevation proto))
         true (assoc :magnetic-declination (.getMagneticDeclination proto))
         true (assoc :calibrating (.getCalibrating proto))))