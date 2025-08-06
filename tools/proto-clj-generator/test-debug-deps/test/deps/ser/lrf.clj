(ns test.deps.ser.lrf
  "Generated protobuf functions."
  (:require [com.fulcrologic.guardrails.malli.core :refer [=> >defn >defn- ?]]
            [malli.core :as m]
            [test.deps.ser.types :as ser])
  (:import ser.JonSharedDataLrf$JonGuiDataLrf
           ser.JonSharedDataLrf$JonGuiDataTarget
           ser.JonSharedDataLrf$RgbColor))

;; =============================================================================
;; Enums
;; =============================================================================

;; No enums

;; =============================================================================
;; Malli Specs
;; =============================================================================

(def jon-gui-data-lrf-spec
  "Malli spec for jon-gui-data-lrf message"
  [:map [:is-scanning [:maybe :boolean]] [:is-measuring [:maybe :boolean]]
   [:measure-id [:maybe :int]] [:target [:maybe :ser/jon-gui-data-target]]
   [:pointer-mode [:maybe :ser/jon-gui-datat-lrf-laser-pointer-modes]]
   [:fog-mode-enabled [:maybe :boolean]] [:is-refining [:maybe :boolean]]])

(def jon-gui-data-target-spec
  "Malli spec for jon-gui-data-target message"
  [:map [:timestamp [:maybe :int]] [:target-longitude [:maybe :double]]
   [:target-latitude [:maybe :double]] [:target-altitude [:maybe :double]]
   [:observer-longitude [:maybe :double]] [:observer-latitude [:maybe :double]]
   [:observer-altitude [:maybe :double]] [:observer-azimuth [:maybe :double]]
   [:observer-elevation [:maybe :double]] [:observer-bank [:maybe :double]]
   [:distance-2d [:maybe :double]] [:distance-3b [:maybe :double]]
   [:observer-fix-type [:maybe :ser/jon-gui-data-gps-fix-type]]
   [:session-id [:maybe :int]] [:target-id [:maybe :int]]
   [:target-color [:maybe :ser/rgb-color]] [:type [:maybe :int]]
   [:uuid-part-1 [:maybe :int]] [:uuid-part-2 [:maybe :int]]
   [:uuid-part-3 [:maybe :int]] [:uuid-part-4 [:maybe :int]]])

(def rgb-color-spec
  "Malli spec for rgb-color message"
  [:map [:red [:maybe :int]] [:green [:maybe :int]] [:blue [:maybe :int]]])

;; =============================================================================
;; Builders and Parsers
;; =============================================================================

;; Forward declarations
(declare build-jon-gui-data-lrf)
(declare build-jon-gui-data-target)
(declare build-rgb-color)
(declare parse-jon-gui-data-lrf)
(declare parse-jon-gui-data-target)
(declare parse-rgb-color)

(>defn
  build-jon-gui-data-lrf
  "Build a JonGuiDataLrf protobuf message from a map."
  [m]
  [jon-gui-data-lrf-spec => #(instance? ser.JonSharedDataLrf$JonGuiDataLrf %)]
  (let [builder (ser.JonSharedDataLrf$JonGuiDataLrf/newBuilder)]
    ;; Set regular fields
    (when (contains? m :is-scanning)
      (.setIsScanning builder (get m :is-scanning)))
    (when (contains? m :is-measuring)
      (.setIsMeasuring builder (get m :is-measuring)))
    (when (contains? m :measure-id) (.setMeasureId builder (get m :measure-id)))
    (when (contains? m :target)
      (.setTarget builder (build-jon-gui-data-target (get m :target))))
    (when (contains? m :pointer-mode)
      (.setPointerMode builder
                       (get jon-gui-datat-lrf-laser-pointer-modes-values
                            (get m :pointer-mode))))
    (when (contains? m :fog-mode-enabled)
      (.setFogModeEnabled builder (get m :fog-mode-enabled)))
    (when (contains? m :is-refining)
      (.setIsRefining builder (get m :is-refining)))
    (.build builder)))

(>defn
  build-jon-gui-data-target
  "Build a JonGuiDataTarget protobuf message from a map."
  [m]
  [jon-gui-data-target-spec =>
   #(instance? ser.JonSharedDataLrf$JonGuiDataTarget %)]
  (let [builder (ser.JonSharedDataLrf$JonGuiDataTarget/newBuilder)]
    ;; Set regular fields
    (when (contains? m :timestamp) (.setTimestamp builder (get m :timestamp)))
    (when (contains? m :target-longitude)
      (.setTargetLongitude builder (get m :target-longitude)))
    (when (contains? m :target-latitude)
      (.setTargetLatitude builder (get m :target-latitude)))
    (when (contains? m :target-altitude)
      (.setTargetAltitude builder (get m :target-altitude)))
    (when (contains? m :observer-longitude)
      (.setObserverLongitude builder (get m :observer-longitude)))
    (when (contains? m :observer-latitude)
      (.setObserverLatitude builder (get m :observer-latitude)))
    (when (contains? m :observer-altitude)
      (.setObserverAltitude builder (get m :observer-altitude)))
    (when (contains? m :observer-azimuth)
      (.setObserverAzimuth builder (get m :observer-azimuth)))
    (when (contains? m :observer-elevation)
      (.setObserverElevation builder (get m :observer-elevation)))
    (when (contains? m :observer-bank)
      (.setObserverBank builder (get m :observer-bank)))
    (when (contains? m :distance-2d)
      (.setDistance2d builder (get m :distance-2d)))
    (when (contains? m :distance-3b)
      (.setDistance3b builder (get m :distance-3b)))
    (when (contains? m :observer-fix-type)
      (.setObserverFixType builder
                           (get jon-gui-data-gps-fix-type-values
                                (get m :observer-fix-type))))
    (when (contains? m :session-id) (.setSessionId builder (get m :session-id)))
    (when (contains? m :target-id) (.setTargetId builder (get m :target-id)))
    (when (contains? m :target-color)
      (.setTargetColor builder (build-rgb-color (get m :target-color))))
    (when (contains? m :type) (.setType builder (get m :type)))
    (when (contains? m :uuid-part-1)
      (.setUuidPart1 builder (get m :uuid-part-1)))
    (when (contains? m :uuid-part-2)
      (.setUuidPart2 builder (get m :uuid-part-2)))
    (when (contains? m :uuid-part-3)
      (.setUuidPart3 builder (get m :uuid-part-3)))
    (when (contains? m :uuid-part-4)
      (.setUuidPart4 builder (get m :uuid-part-4)))
    (.build builder)))

(>defn build-rgb-color
       "Build a RgbColor protobuf message from a map."
       [m]
       [rgb-color-spec => #(instance? ser.JonSharedDataLrf$RgbColor %)]
       (let [builder (ser.JonSharedDataLrf$RgbColor/newBuilder)]
         ;; Set regular fields
         (when (contains? m :red) (.setRed builder (get m :red)))
         (when (contains? m :green) (.setGreen builder (get m :green)))
         (when (contains? m :blue) (.setBlue builder (get m :blue)))
         (.build builder)))

(>defn parse-jon-gui-data-lrf
       "Parse a JonGuiDataLrf protobuf message to a map."
       [^ser.JonSharedDataLrf$JonGuiDataLrf proto]
       [#(instance? ser.JonSharedDataLrf$JonGuiDataLrf %) =>
        jon-gui-data-lrf-spec]
       (cond-> {}
         ;; Regular fields
         true (assoc :is-scanning (.getIsScanning proto))
         true (assoc :is-measuring (.getIsMeasuring proto))
         true (assoc :measure-id (.getMeasureId proto))
         (.hasTarget proto) (assoc :target
                              (parse-jon-gui-data-target (.getTarget proto)))
         true (assoc :pointer-mode
                (get jon-gui-datat-lrf-laser-pointer-modes-keywords
                     (.getPointerMode proto)))
         true (assoc :fog-mode-enabled (.getFogModeEnabled proto))
         true (assoc :is-refining (.getIsRefining proto))))

(>defn parse-jon-gui-data-target
       "Parse a JonGuiDataTarget protobuf message to a map."
       [^ser.JonSharedDataLrf$JonGuiDataTarget proto]
       [#(instance? ser.JonSharedDataLrf$JonGuiDataTarget %) =>
        jon-gui-data-target-spec]
       (cond-> {}
         ;; Regular fields
         true (assoc :timestamp (.getTimestamp proto))
         true (assoc :target-longitude (.getTargetLongitude proto))
         true (assoc :target-latitude (.getTargetLatitude proto))
         true (assoc :target-altitude (.getTargetAltitude proto))
         true (assoc :observer-longitude (.getObserverLongitude proto))
         true (assoc :observer-latitude (.getObserverLatitude proto))
         true (assoc :observer-altitude (.getObserverAltitude proto))
         true (assoc :observer-azimuth (.getObserverAzimuth proto))
         true (assoc :observer-elevation (.getObserverElevation proto))
         true (assoc :observer-bank (.getObserverBank proto))
         true (assoc :distance-2d (.getDistance2d proto))
         true (assoc :distance-3b (.getDistance3b proto))
         true (assoc :observer-fix-type
                (get jon-gui-data-gps-fix-type-keywords
                     (.getObserverFixType proto)))
         true (assoc :session-id (.getSessionId proto))
         true (assoc :target-id (.getTargetId proto))
         (.hasTargetColor proto) (assoc :target-color
                                   (parse-rgb-color (.getTargetColor proto)))
         true (assoc :type (.getType proto))
         true (assoc :uuid-part-1 (.getUuidPart1 proto))
         true (assoc :uuid-part-2 (.getUuidPart2 proto))
         true (assoc :uuid-part-3 (.getUuidPart3 proto))
         true (assoc :uuid-part-4 (.getUuidPart4 proto))))

(>defn parse-rgb-color
       "Parse a RgbColor protobuf message to a map."
       [^ser.JonSharedDataLrf$RgbColor proto]
       [#(instance? ser.JonSharedDataLrf$RgbColor %) => rgb-color-spec]
       (cond-> {}
         ;; Regular fields
         true (assoc :red (.getRed proto))
         true (assoc :green (.getGreen proto))
         true (assoc :blue (.getBlue proto))))