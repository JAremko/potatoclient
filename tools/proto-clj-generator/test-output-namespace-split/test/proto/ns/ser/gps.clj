(ns test.proto.ns.ser.gps
  "Generated protobuf functions."
  (:require [com.fulcrologic.guardrails.malli.core :refer [=> >defn >defn- ?]]
            [malli.core :as m])
  (:import ser.JonSharedDataGps$JonGuiDataGps))

;; =============================================================================
;; Enums
;; =============================================================================

;; No enums

;; =============================================================================
;; Malli Specs
;; =============================================================================

(def jon-gui-data-gps-spec
  "Malli spec for jon-gui-data-gps message"
  [:map [:longitude [:maybe :double]] [:latitude [:maybe :double]]
   [:altitude [:maybe :double]] [:manual-longitude [:maybe :double]]
   [:manual-latitude [:maybe :double]] [:manual-altitude [:maybe :double]]
   [:fix-type [:maybe :ser/jon-gui-data-gps-fix-type]]
   [:use-manual [:maybe :boolean]]])

;; =============================================================================
;; Builders and Parsers
;; =============================================================================

;; Forward declarations
(declare build-jon-gui-data-gps)
(declare parse-jon-gui-data-gps)

(>defn
  build-jon-gui-data-gps
  "Build a JonGuiDataGps protobuf message from a map."
  [m]
  [jon-gui-data-gps-spec => #(instance? ser.JonSharedDataGps$JonGuiDataGps %)]
  (let [builder (ser.JonSharedDataGps$JonGuiDataGps/newBuilder)]
    ;; Set regular fields
    (when (contains? m :longitude) (.setLongitude builder (get m :longitude)))
    (when (contains? m :latitude) (.setLatitude builder (get m :latitude)))
    (when (contains? m :altitude) (.setAltitude builder (get m :altitude)))
    (when (contains? m :manual-longitude)
      (.setManualLongitude builder (get m :manual-longitude)))
    (when (contains? m :manual-latitude)
      (.setManualLatitude builder (get m :manual-latitude)))
    (when (contains? m :manual-altitude)
      (.setManualAltitude builder (get m :manual-altitude)))
    (when (contains? m :fix-type)
      (.setFixType builder
                   (get jon-gui-data-gps-fix-type-values (get m :fix-type))))
    (when (contains? m :use-manual) (.setUseManual builder (get m :use-manual)))
    (.build builder)))

(>defn parse-jon-gui-data-gps
       "Parse a JonGuiDataGps protobuf message to a map."
       [^ser.JonSharedDataGps$JonGuiDataGps proto]
       [#(instance? ser.JonSharedDataGps$JonGuiDataGps %) =>
        jon-gui-data-gps-spec]
       (cond-> {}
         ;; Regular fields
         true (assoc :longitude (.getLongitude proto))
         true (assoc :latitude (.getLatitude proto))
         true (assoc :altitude (.getAltitude proto))
         true (assoc :manual-longitude (.getManualLongitude proto))
         true (assoc :manual-latitude (.getManualLatitude proto))
         true (assoc :manual-altitude (.getManualAltitude proto))
         true (assoc :fix-type
                (get jon-gui-data-gps-fix-type-keywords (.getFixType proto)))
         true (assoc :use-manual (.getUseManual proto))))