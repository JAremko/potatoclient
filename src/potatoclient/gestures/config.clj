(ns potatoclient.gestures.config
  (:require [com.fulcrologic.guardrails.malli.core :refer [>defn >defn- => ?]]
            [clojure.edn :as edn]
            [clojure.java.io :as io]
            [potatoclient.specs :as specs]
            [potatoclient.logging :as logging]))

(def ^:private gesture-config-atom (atom nil))

(>defn load-gesture-config!
  "Load gesture configuration from resources"
  []
  [=> nil?]
  (try
    (if-let [resource (io/resource "config/gestures.edn")]
      (let [config (edn/read-string (slurp resource))]
        (reset! gesture-config-atom config)
        (logging/log-info {:msg "Loaded gesture configuration"}))
      (logging/log-warn {:msg "Gesture config not found, using defaults"}))
    (catch Exception e
      (logging/log-error {:msg "Failed to load gesture config" :error e})))
  nil)

(>defn get-gesture-config
  "Get current gesture configuration"
  []
  [=> (? map?)]
  (or @gesture-config-atom
      ;; Default configuration
      {:gesture-config
       {:move-threshold 20
        :tap-long-press-threshold 300
        :double-tap-threshold 300
        :swipe-threshold 100
        :pan-update-interval 120
        :double-tap-tolerance 10}
       :zoom-speed-config {}}))

(>defn zoom-value-to-table-index
  "Convert zoom value (1.0x, 2.0x, etc) to table index (0-4)"
  [zoom-value]
  [number? => int?]
  ;; Simple mapping: 1.0x = index 0, 2.0x = index 1, etc.
  ;; Clamp to 0-4 range
  (-> zoom-value
      (- 1.0)
      (max 0.0)
      (min 4.0)
      int))

(>defn get-speed-config-for-zoom
  "Get speed configuration for a given stream type and zoom table index"
  [stream-type zoom-table-index]
  [string? int? => ::specs/speed-config]
  (let [gesture-config (get-gesture-config)
        configs (get-in gesture-config [:zoom-speed-config (keyword stream-type)])]
    (or (first (filter #(= (or (:zoom-table-index %) (:index %)) zoom-table-index) configs))
        ;; Default to zoom table index 0 if not found
        (first (filter #(= (or (:zoom-table-index %) (:index %)) 0) configs))
        ;; Fallback config
        {:max-rotation-speed 1.0
         :min-rotation-speed 0.0001
         :ndc-threshold 0.5
         :dead-zone-radius 0.05
         :curve-steepness 4.0})))

(>defn get-speed-config-for-zoom-value
  "Get speed configuration for a given stream type and zoom value (e.g. 1.0, 2.5)"
  [stream-type zoom-value]
  [string? number? => ::specs/speed-config]
  (let [zoom-index (zoom-value-to-table-index zoom-value)]
    (get-speed-config-for-zoom stream-type zoom-index)))

(>defn calculate-rotation-speeds
  "Calculate azimuth and elevation rotation speeds based on NDC deltas and config"
  [ndc-delta-x ndc-delta-y config]
  [number? number? ::specs/speed-config => [:tuple number? number?]]
  (let [{:keys [max-rotation-speed min-rotation-speed ndc-threshold 
                dead-zone-radius curve-steepness]} config
        ;; Calculate magnitude
        magnitude (Math/sqrt (+ (* ndc-delta-x ndc-delta-x) 
                               (* ndc-delta-y ndc-delta-y)))
        ;; Apply dead zone
        magnitude-adjusted (max 0 (- magnitude dead-zone-radius))
        ;; Apply curve (exponential mapping)
        normalized-speed (if (> magnitude-adjusted 0)
                          (Math/pow (/ magnitude-adjusted ndc-threshold) curve-steepness)
                          0)
        ;; Clamp to speed range
        speed (if (> magnitude-adjusted 0)
                (-> normalized-speed
                    (min 1.0)
                    (* (- max-rotation-speed min-rotation-speed))
                    (+ min-rotation-speed))
                0.0)
        ;; Calculate individual axis speeds based on contribution to movement
        az-ratio (if (> magnitude 0) (Math/abs (/ ndc-delta-x magnitude)) 0)
        el-ratio (if (> magnitude 0) (Math/abs (/ ndc-delta-y magnitude)) 0)]
    [(* speed az-ratio) (* speed el-ratio)]))