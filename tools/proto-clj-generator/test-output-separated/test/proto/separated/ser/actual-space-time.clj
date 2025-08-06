(ns test.proto.separated.ser.actual-space-time
  "Generated protobuf functions."
  (:require [com.fulcrologic.guardrails.malli.core :refer [=> >defn >defn- ?]]
            [malli.core :as m])
  (:import ser.JonSharedDataActualSpaceTime$JonGuiDataActualSpaceTime))

;; =============================================================================
;; Enums
;; =============================================================================

;; No enums

;; =============================================================================
;; Malli Specs
;; =============================================================================

(def jon-gui-data-actual-space-time-spec
  "Malli spec for jon-gui-data-actual-space-time message"
  [:map [:azimuth [:maybe :float]] [:elevation [:maybe :float]]
   [:bank [:maybe :float]] [:latitude [:maybe :float]]
   [:longitude [:maybe :float]] [:altitude [:maybe :double]]
   [:timestamp [:maybe :int]]])

;; =============================================================================
;; Builders and Parsers
;; =============================================================================

;; Forward declarations
(declare build-jon-gui-data-actual-space-time)
(declare parse-jon-gui-data-actual-space-time)

(>defn
  build-jon-gui-data-actual-space-time
  "Build a JonGuiDataActualSpaceTime protobuf message from a map."
  [m]
  [jon-gui-data-actual-space-time-spec =>
   #(instance? ser.JonSharedDataActualSpaceTime$JonGuiDataActualSpaceTime %)]
  (let
    [builder
       (ser.JonSharedDataActualSpaceTime$JonGuiDataActualSpaceTime/newBuilder)]
    ;; Set regular fields
    (when (contains? m :azimuth) (.setAzimuth builder (get m :azimuth)))
    (when (contains? m :elevation) (.setElevation builder (get m :elevation)))
    (when (contains? m :bank) (.setBank builder (get m :bank)))
    (when (contains? m :latitude) (.setLatitude builder (get m :latitude)))
    (when (contains? m :longitude) (.setLongitude builder (get m :longitude)))
    (when (contains? m :altitude) (.setAltitude builder (get m :altitude)))
    (when (contains? m :timestamp) (.setTimestamp builder (get m :timestamp)))
    (.build builder)))

(>defn parse-jon-gui-data-actual-space-time
       "Parse a JonGuiDataActualSpaceTime protobuf message to a map."
       [^ser.JonSharedDataActualSpaceTime$JonGuiDataActualSpaceTime proto]
       [#(instance? ser.JonSharedDataActualSpaceTime$JonGuiDataActualSpaceTime
                    %) => jon-gui-data-actual-space-time-spec]
       (cond-> {}
         ;; Regular fields
         true (assoc :azimuth (.getAzimuth proto))
         true (assoc :elevation (.getElevation proto))
         true (assoc :bank (.getBank proto))
         true (assoc :latitude (.getLatitude proto))
         true (assoc :longitude (.getLongitude proto))
         true (assoc :altitude (.getAltitude proto))
         true (assoc :timestamp (.getTimestamp proto))))