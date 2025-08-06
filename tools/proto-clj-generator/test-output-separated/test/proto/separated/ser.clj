(ns test.proto.separated.ser.camera-day
  "Generated protobuf functions."
  (:require [com.fulcrologic.guardrails.malli.core :refer [=> >defn >defn- ?]]
            [malli.core :as m])
  (:import ser.JonSharedDataCameraDay$JonGuiDataCameraDay))

;; =============================================================================
;; Enums
;; =============================================================================

;; No enums

;; =============================================================================
;; Malli Specs
;; =============================================================================

(def jon-gui-data-camera-day-spec
  "Malli spec for jon-gui-data-camera-day message"
  [:map [:focus-pos [:maybe :float]] [:zoom-pos [:maybe :float]]
   [:iris-pos [:maybe :float]] [:infrared-filter [:maybe :boolean]]
   [:zoom-table-pos [:maybe :int]] [:zoom-table-pos-max [:maybe :int]]
   [:fx-mode [:maybe :ser/jon-gui-data-fx-mode-day]]
   [:auto-focus [:maybe :boolean]] [:auto-iris [:maybe :boolean]]
   [:digital-zoom-level [:maybe :float]] [:clahe-level [:maybe :float]]])

;; =============================================================================
;; Builders and Parsers
;; =============================================================================

;; Forward declarations
(declare build-jon-gui-data-camera-day)
(declare parse-jon-gui-data-camera-day)

(>defn
  build-jon-gui-data-camera-day
  "Build a JonGuiDataCameraDay protobuf message from a map."
  [m]
  [jon-gui-data-camera-day-spec =>
   #(instance? ser.JonSharedDataCameraDay$JonGuiDataCameraDay %)]
  (let [builder (ser.JonSharedDataCameraDay$JonGuiDataCameraDay/newBuilder)]
    ;; Set regular fields
    (when (contains? m :focus-pos) (.setFocusPos builder (get m :focus-pos)))
    (when (contains? m :zoom-pos) (.setZoomPos builder (get m :zoom-pos)))
    (when (contains? m :iris-pos) (.setIrisPos builder (get m :iris-pos)))
    (when (contains? m :infrared-filter)
      (.setInfraredFilter builder (get m :infrared-filter)))
    (when (contains? m :zoom-table-pos)
      (.setZoomTablePos builder (get m :zoom-table-pos)))
    (when (contains? m :zoom-table-pos-max)
      (.setZoomTablePosMax builder (get m :zoom-table-pos-max)))
    (when (contains? m :fx-mode)
      (.setFxMode builder
                  (get jon-gui-data-fx-mode-day-values (get m :fx-mode))))
    (when (contains? m :auto-focus) (.setAutoFocus builder (get m :auto-focus)))
    (when (contains? m :auto-iris) (.setAutoIris builder (get m :auto-iris)))
    (when (contains? m :digital-zoom-level)
      (.setDigitalZoomLevel builder (get m :digital-zoom-level)))
    (when (contains? m :clahe-level)
      (.setClaheLevel builder (get m :clahe-level)))
    (.build builder)))

(>defn parse-jon-gui-data-camera-day
       "Parse a JonGuiDataCameraDay protobuf message to a map."
       [^ser.JonSharedDataCameraDay$JonGuiDataCameraDay proto]
       [#(instance? ser.JonSharedDataCameraDay$JonGuiDataCameraDay %) =>
        jon-gui-data-camera-day-spec]
       (cond-> {}
         ;; Regular fields
         true (assoc :focus-pos (.getFocusPos proto))
         true (assoc :zoom-pos (.getZoomPos proto))
         true (assoc :iris-pos (.getIrisPos proto))
         true (assoc :infrared-filter (.getInfraredFilter proto))
         true (assoc :zoom-table-pos (.getZoomTablePos proto))
         true (assoc :zoom-table-pos-max (.getZoomTablePosMax proto))
         true (assoc :fx-mode
                (get jon-gui-data-fx-mode-day-keywords (.getFxMode proto)))
         true (assoc :auto-focus (.getAutoFocus proto))
         true (assoc :auto-iris (.getAutoIris proto))
         true (assoc :digital-zoom-level (.getDigitalZoomLevel proto))
         true (assoc :clahe-level (.getClaheLevel proto))))