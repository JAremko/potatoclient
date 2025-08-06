(ns potatoclient.proto.ser.rotary
  "Generated protobuf functions."
  (:require [com.fulcrologic.guardrails.malli.core :refer [=> >defn >defn- ?]]
            [malli.core :as m]
            [potatoclient.proto.ser.types :as types])
  (:import ser.JonSharedDataRotary$JonGuiDataRotary
           ser.JonSharedDataRotary$ScanNode))

;; =============================================================================
;; Enums
;; =============================================================================

;; No enums

;; =============================================================================
;; Malli Specs
;; =============================================================================

(def jon-gui-data-rotary-spec
  "Malli spec for jon-gui-data-rotary message"
  [:map [:azimuth [:maybe :float]] [:azimuth-speed [:maybe :float]]
   [:elevation [:maybe :float]] [:elevation-speed [:maybe :float]]
   [:platform-azimuth [:maybe :float]] [:platform-elevation [:maybe :float]]
   [:platform-bank [:maybe :float]] [:is-moving [:maybe :boolean]]
   [:mode [:maybe :ser/jon-gui-data-rotary-mode]]
   [:is-scanning [:maybe :boolean]] [:is-scanning-paused [:maybe :boolean]]
   [:use-rotary-as-compass [:maybe :boolean]] [:scan-target [:maybe :int]]
   [:scan-target-max [:maybe :int]] [:sun-azimuth [:maybe :float]]
   [:sun-elevation [:maybe :float]]
   [:current-scan-node [:maybe :ser/scan-node]]])

(def scan-node-spec
  "Malli spec for scan-node message"
  [:map [:index [:maybe :int]] [:day-zoom-table-value [:maybe :int]]
   [:heat-zoom-table-value [:maybe :int]] [:azimuth [:maybe :double]]
   [:elevation [:maybe :double]] [:linger [:maybe :double]]
   [:speed [:maybe :double]]])

;; =============================================================================
;; Builders and Parsers
;; =============================================================================

;; Forward declarations
(declare build-jon-gui-data-rotary)
(declare build-scan-node)
(declare parse-jon-gui-data-rotary)
(declare parse-scan-node)

(>defn
  build-jon-gui-data-rotary
  "Build a JonGuiDataRotary protobuf message from a map."
  [m]
  [jon-gui-data-rotary-spec =>
   #(instance? ser.JonSharedDataRotary$JonGuiDataRotary %)]
  (let [builder (ser.JonSharedDataRotary$JonGuiDataRotary/newBuilder)]
    ;; Set regular fields
    (when (contains? m :azimuth) (.setAzimuth builder (get m :azimuth)))
    (when (contains? m :azimuth-speed)
      (.setAzimuthSpeed builder (get m :azimuth-speed)))
    (when (contains? m :elevation) (.setElevation builder (get m :elevation)))
    (when (contains? m :elevation-speed)
      (.setElevationSpeed builder (get m :elevation-speed)))
    (when (contains? m :platform-azimuth)
      (.setPlatformAzimuth builder (get m :platform-azimuth)))
    (when (contains? m :platform-elevation)
      (.setPlatformElevation builder (get m :platform-elevation)))
    (when (contains? m :platform-bank)
      (.setPlatformBank builder (get m :platform-bank)))
    (when (contains? m :is-moving) (.setIsMoving builder (get m :is-moving)))
    (when (contains? m :mode)
      (.setMode builder (get jon-gui-data-rotary-mode-values (get m :mode))))
    (when (contains? m :is-scanning)
      (.setIsScanning builder (get m :is-scanning)))
    (when (contains? m :is-scanning-paused)
      (.setIsScanningPaused builder (get m :is-scanning-paused)))
    (when (contains? m :use-rotary-as-compass)
      (.setUseRotaryAsCompass builder (get m :use-rotary-as-compass)))
    (when (contains? m :scan-target)
      (.setScanTarget builder (get m :scan-target)))
    (when (contains? m :scan-target-max)
      (.setScanTargetMax builder (get m :scan-target-max)))
    (when (contains? m :sun-azimuth)
      (.setSunAzimuth builder (get m :sun-azimuth)))
    (when (contains? m :sun-elevation)
      (.setSunElevation builder (get m :sun-elevation)))
    (when (contains? m :current-scan-node)
      (.setCurrentScanNode builder
                           (build-scan-node (get m :current-scan-node))))
    (.build builder)))

(>defn build-scan-node
       "Build a ScanNode protobuf message from a map."
       [m]
       [scan-node-spec => #(instance? ser.JonSharedDataRotary$ScanNode %)]
       (let [builder (ser.JonSharedDataRotary$ScanNode/newBuilder)]
         ;; Set regular fields
         (when (contains? m :index) (.setIndex builder (get m :index)))
         (when (contains? m :day-zoom-table-value)
           (.setDayZoomTableValue builder (get m :day-zoom-table-value)))
         (when (contains? m :heat-zoom-table-value)
           (.setHeatZoomTableValue builder (get m :heat-zoom-table-value)))
         (when (contains? m :azimuth) (.setAzimuth builder (get m :azimuth)))
         (when (contains? m :elevation)
           (.setElevation builder (get m :elevation)))
         (when (contains? m :linger) (.setLinger builder (get m :linger)))
         (when (contains? m :speed) (.setSpeed builder (get m :speed)))
         (.build builder)))

(>defn
  parse-jon-gui-data-rotary
  "Parse a JonGuiDataRotary protobuf message to a map."
  [^ser.JonSharedDataRotary$JonGuiDataRotary proto]
  [#(instance? ser.JonSharedDataRotary$JonGuiDataRotary %) =>
   jon-gui-data-rotary-spec]
  (cond-> {}
    ;; Regular fields
    true (assoc :azimuth (.getAzimuth proto))
    true (assoc :azimuth-speed (.getAzimuthSpeed proto))
    true (assoc :elevation (.getElevation proto))
    true (assoc :elevation-speed (.getElevationSpeed proto))
    true (assoc :platform-azimuth (.getPlatformAzimuth proto))
    true (assoc :platform-elevation (.getPlatformElevation proto))
    true (assoc :platform-bank (.getPlatformBank proto))
    true (assoc :is-moving (.getIsMoving proto))
    true (assoc :mode (get jon-gui-data-rotary-mode-keywords (.getMode proto)))
    true (assoc :is-scanning (.getIsScanning proto))
    true (assoc :is-scanning-paused (.getIsScanningPaused proto))
    true (assoc :use-rotary-as-compass (.getUseRotaryAsCompass proto))
    true (assoc :scan-target (.getScanTarget proto))
    true (assoc :scan-target-max (.getScanTargetMax proto))
    true (assoc :sun-azimuth (.getSunAzimuth proto))
    true (assoc :sun-elevation (.getSunElevation proto))
    (.hasCurrentScanNode proto)
      (assoc :current-scan-node (parse-scan-node (.getCurrentScanNode proto)))))

(>defn parse-scan-node
       "Parse a ScanNode protobuf message to a map."
       [^ser.JonSharedDataRotary$ScanNode proto]
       [#(instance? ser.JonSharedDataRotary$ScanNode %) => scan-node-spec]
       (cond-> {}
         ;; Regular fields
         true (assoc :index (.getIndex proto))
         true (assoc :day-zoom-table-value (.getDayZoomTableValue proto))
         true (assoc :heat-zoom-table-value (.getHeatZoomTableValue proto))
         true (assoc :azimuth (.getAzimuth proto))
         true (assoc :elevation (.getElevation proto))
         true (assoc :linger (.getLinger proto))
         true (assoc :speed (.getSpeed proto))))