(ns test.deps.ser.rec-osd
  "Generated protobuf functions."
  (:require [com.fulcrologic.guardrails.malli.core :refer [=> >defn >defn- ?]]
            [malli.core :as m]
            [test.deps.ser.types :as ser])
  (:import ser.JonSharedDataRecOsd$JonGuiDataRecOsd))

;; =============================================================================
;; Enums
;; =============================================================================

;; No enums

;; =============================================================================
;; Malli Specs
;; =============================================================================

(def jon-gui-data-rec-osd-spec
  "Malli spec for jon-gui-data-rec-osd message"
  [:map [:screen [:maybe :ser/jon-gui-data-rec-osd-screen]]
   [:heat-osd-enabled [:maybe :boolean]] [:day-osd-enabled [:maybe :boolean]]
   [:heat-crosshair-offset-horizontal [:maybe :int]]
   [:heat-crosshair-offset-vertical [:maybe :int]]
   [:day-crosshair-offset-horizontal [:maybe :int]]
   [:day-crosshair-offset-vertical [:maybe :int]]])

;; =============================================================================
;; Builders and Parsers
;; =============================================================================

;; Forward declarations
(declare build-jon-gui-data-rec-osd)
(declare parse-jon-gui-data-rec-osd)

(>defn
  build-jon-gui-data-rec-osd
  "Build a JonGuiDataRecOsd protobuf message from a map."
  [m]
  [jon-gui-data-rec-osd-spec =>
   #(instance? ser.JonSharedDataRecOsd$JonGuiDataRecOsd %)]
  (let [builder (ser.JonSharedDataRecOsd$JonGuiDataRecOsd/newBuilder)]
    ;; Set regular fields
    (when (contains? m :screen)
      (.setScreen builder
                  (get jon-gui-data-rec-osd-screen-values (get m :screen))))
    (when (contains? m :heat-osd-enabled)
      (.setHeatOsdEnabled builder (get m :heat-osd-enabled)))
    (when (contains? m :day-osd-enabled)
      (.setDayOsdEnabled builder (get m :day-osd-enabled)))
    (when (contains? m :heat-crosshair-offset-horizontal)
      (.setHeatCrosshairOffsetHorizontal
        builder
        (get m :heat-crosshair-offset-horizontal)))
    (when (contains? m :heat-crosshair-offset-vertical)
      (.setHeatCrosshairOffsetVertical builder
                                       (get m :heat-crosshair-offset-vertical)))
    (when (contains? m :day-crosshair-offset-horizontal)
      (.setDayCrosshairOffsetHorizontal builder
                                        (get m
                                             :day-crosshair-offset-horizontal)))
    (when (contains? m :day-crosshair-offset-vertical)
      (.setDayCrosshairOffsetVertical builder
                                      (get m :day-crosshair-offset-vertical)))
    (.build builder)))

(>defn parse-jon-gui-data-rec-osd
       "Parse a JonGuiDataRecOsd protobuf message to a map."
       [^ser.JonSharedDataRecOsd$JonGuiDataRecOsd proto]
       [#(instance? ser.JonSharedDataRecOsd$JonGuiDataRecOsd %) =>
        jon-gui-data-rec-osd-spec]
       (cond-> {}
         ;; Regular fields
         true (assoc :screen
                (get jon-gui-data-rec-osd-screen-keywords (.getScreen proto)))
         true (assoc :heat-osd-enabled (.getHeatOsdEnabled proto))
         true (assoc :day-osd-enabled (.getDayOsdEnabled proto))
         true (assoc :heat-crosshair-offset-horizontal
                (.getHeatCrosshairOffsetHorizontal proto))
         true (assoc :heat-crosshair-offset-vertical
                (.getHeatCrosshairOffsetVertical proto))
         true (assoc :day-crosshair-offset-horizontal
                (.getDayCrosshairOffsetHorizontal proto))
         true (assoc :day-crosshair-offset-vertical
                (.getDayCrosshairOffsetVertical proto))))