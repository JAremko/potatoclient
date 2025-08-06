(ns test.deps.ser.camera-heat
  "Generated protobuf functions."
  (:require [com.fulcrologic.guardrails.malli.core :refer [=> >defn >defn- ?]]
            [malli.core :as m]
            [test.deps.ser.types :as ser])
  (:import ser.JonSharedDataCameraHeat$JonGuiDataCameraHeat))

;; =============================================================================
;; Enums
;; =============================================================================

;; No enums

;; =============================================================================
;; Malli Specs
;; =============================================================================

(def jon-gui-data-camera-heat-spec
  "Malli spec for jon-gui-data-camera-heat message"
  [:map [:zoom-pos [:maybe :float]]
   [:agc-mode [:maybe :ser/jon-gui-data-video-channel-heat-agc-modes]]
   [:filter [:maybe :ser/jon-gui-data-video-channel-heat-filters]]
   [:auto-focus [:maybe :boolean]] [:zoom-table-pos [:maybe :int]]
   [:zoom-table-pos-max [:maybe :int]] [:dde-level [:maybe :int]]
   [:dde-enabled [:maybe :boolean]]
   [:fx-mode [:maybe :ser/jon-gui-data-fx-mode-heat]]
   [:digital-zoom-level [:maybe :float]] [:clahe-level [:maybe :float]]])

;; =============================================================================
;; Builders and Parsers
;; =============================================================================

;; Forward declarations
(declare build-jon-gui-data-camera-heat)
(declare parse-jon-gui-data-camera-heat)

(>defn
  build-jon-gui-data-camera-heat
  "Build a JonGuiDataCameraHeat protobuf message from a map."
  [m]
  [jon-gui-data-camera-heat-spec =>
   #(instance? ser.JonSharedDataCameraHeat$JonGuiDataCameraHeat %)]
  (let [builder (ser.JonSharedDataCameraHeat$JonGuiDataCameraHeat/newBuilder)]
    ;; Set regular fields
    (when (contains? m :zoom-pos) (.setZoomPos builder (get m :zoom-pos)))
    (when (contains? m :agc-mode)
      (.setAgcMode builder
                   (get jon-gui-data-video-channel-heat-agc-modes-values
                        (get m :agc-mode))))
    (when (contains? m :filter)
      (.setFilter builder
                  (get jon-gui-data-video-channel-heat-filters-values
                       (get m :filter))))
    (when (contains? m :auto-focus) (.setAutoFocus builder (get m :auto-focus)))
    (when (contains? m :zoom-table-pos)
      (.setZoomTablePos builder (get m :zoom-table-pos)))
    (when (contains? m :zoom-table-pos-max)
      (.setZoomTablePosMax builder (get m :zoom-table-pos-max)))
    (when (contains? m :dde-level) (.setDdeLevel builder (get m :dde-level)))
    (when (contains? m :dde-enabled)
      (.setDdeEnabled builder (get m :dde-enabled)))
    (when (contains? m :fx-mode)
      (.setFxMode builder
                  (get jon-gui-data-fx-mode-heat-values (get m :fx-mode))))
    (when (contains? m :digital-zoom-level)
      (.setDigitalZoomLevel builder (get m :digital-zoom-level)))
    (when (contains? m :clahe-level)
      (.setClaheLevel builder (get m :clahe-level)))
    (.build builder)))

(>defn parse-jon-gui-data-camera-heat
       "Parse a JonGuiDataCameraHeat protobuf message to a map."
       [^ser.JonSharedDataCameraHeat$JonGuiDataCameraHeat proto]
       [#(instance? ser.JonSharedDataCameraHeat$JonGuiDataCameraHeat %) =>
        jon-gui-data-camera-heat-spec]
       (cond-> {}
         ;; Regular fields
         true (assoc :zoom-pos (.getZoomPos proto))
         true (assoc :agc-mode
                (get jon-gui-data-video-channel-heat-agc-modes-keywords
                     (.getAgcMode proto)))
         true (assoc :filter
                (get jon-gui-data-video-channel-heat-filters-keywords
                     (.getFilter proto)))
         true (assoc :auto-focus (.getAutoFocus proto))
         true (assoc :zoom-table-pos (.getZoomTablePos proto))
         true (assoc :zoom-table-pos-max (.getZoomTablePosMax proto))
         true (assoc :dde-level (.getDdeLevel proto))
         true (assoc :dde-enabled (.getDdeEnabled proto))
         true (assoc :fx-mode
                (get jon-gui-data-fx-mode-heat-keywords (.getFxMode proto)))
         true (assoc :digital-zoom-level (.getDigitalZoomLevel proto))
         true (assoc :clahe-level (.getClaheLevel proto))))