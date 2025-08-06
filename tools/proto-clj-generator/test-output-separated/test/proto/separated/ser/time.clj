(ns test.proto.separated.ser.time
  "Generated protobuf functions."
  (:require [com.fulcrologic.guardrails.malli.core :refer [=> >defn >defn- ?]]
            [malli.core :as m])
  (:import ser.JonSharedDataTime$JonGuiDataTime))

;; =============================================================================
;; Enums
;; =============================================================================

;; No enums

;; =============================================================================
;; Malli Specs
;; =============================================================================

(def jon-gui-data-time-spec
  "Malli spec for jon-gui-data-time message"
  [:map [:timestamp [:maybe :int]] [:manual-timestamp [:maybe :int]]
   [:zone-id [:maybe :int]] [:use-manual-time [:maybe :boolean]]])

;; =============================================================================
;; Builders and Parsers
;; =============================================================================

;; Forward declarations
(declare build-jon-gui-data-time)
(declare parse-jon-gui-data-time)

(>defn build-jon-gui-data-time
       "Build a JonGuiDataTime protobuf message from a map."
       [m]
       [jon-gui-data-time-spec =>
        #(instance? ser.JonSharedDataTime$JonGuiDataTime %)]
       (let [builder (ser.JonSharedDataTime$JonGuiDataTime/newBuilder)]
         ;; Set regular fields
         (when (contains? m :timestamp)
           (.setTimestamp builder (get m :timestamp)))
         (when (contains? m :manual-timestamp)
           (.setManualTimestamp builder (get m :manual-timestamp)))
         (when (contains? m :zone-id) (.setZoneId builder (get m :zone-id)))
         (when (contains? m :use-manual-time)
           (.setUseManualTime builder (get m :use-manual-time)))
         (.build builder)))

(>defn parse-jon-gui-data-time
       "Parse a JonGuiDataTime protobuf message to a map."
       [^ser.JonSharedDataTime$JonGuiDataTime proto]
       [#(instance? ser.JonSharedDataTime$JonGuiDataTime %) =>
        jon-gui-data-time-spec]
       (cond-> {}
         ;; Regular fields
         true (assoc :timestamp (.getTimestamp proto))
         true (assoc :manual-timestamp (.getManualTimestamp proto))
         true (assoc :zone-id (.getZoneId proto))
         true (assoc :use-manual-time (.getUseManualTime proto))))