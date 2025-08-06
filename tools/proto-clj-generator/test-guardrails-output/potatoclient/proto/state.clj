(ns potatoclient.proto.state
  "State index - re-exports all state namespaces"
  (:require [potatoclient.proto.ser :as ser]))

;; Re-export all public functions from sub-namespaces
;; This supports testing without needing to know the internal namespace
;; structure

(def ser-build-jon-gui-data-meteo ser/build-jon-gui-data-meteo)
(def ser-parse-jon-gui-data-meteo ser/parse-jon-gui-data-meteo)
(def ser-build-jon-gui-data-meteo ser/build-jon-gui-data-meteo)
(def ser-parse-jon-gui-data-meteo ser/parse-jon-gui-data-meteo)
(def ser-build-jon-gui-data-time ser/build-jon-gui-data-time)
(def ser-parse-jon-gui-data-time ser/parse-jon-gui-data-time)
(def ser-build-jon-gui-data-system ser/build-jon-gui-data-system)
(def ser-parse-jon-gui-data-system ser/parse-jon-gui-data-system)
(def ser-build-jon-gui-data-lrf ser/build-jon-gui-data-lrf)
(def ser-parse-jon-gui-data-lrf ser/parse-jon-gui-data-lrf)
(def ser-build-jon-gui-data-target ser/build-jon-gui-data-target)
(def ser-parse-jon-gui-data-target ser/parse-jon-gui-data-target)
(def ser-build-rgb-color ser/build-rgb-color)
(def ser-parse-rgb-color ser/parse-rgb-color)
(def ser-build-jon-gui-data-gps ser/build-jon-gui-data-gps)
(def ser-parse-jon-gui-data-gps ser/parse-jon-gui-data-gps)
(def ser-build-jon-gui-data-compass ser/build-jon-gui-data-compass)
(def ser-parse-jon-gui-data-compass ser/parse-jon-gui-data-compass)
(def ser-build-jon-gui-data-compass-calibration
  ser/build-jon-gui-data-compass-calibration)
(def ser-parse-jon-gui-data-compass-calibration
  ser/parse-jon-gui-data-compass-calibration)
(def ser-build-jon-gui-data-rotary ser/build-jon-gui-data-rotary)
(def ser-parse-jon-gui-data-rotary ser/parse-jon-gui-data-rotary)
(def ser-build-scan-node ser/build-scan-node)
(def ser-parse-scan-node ser/parse-scan-node)
(def ser-build-jon-gui-data-camera-day ser/build-jon-gui-data-camera-day)
(def ser-parse-jon-gui-data-camera-day ser/parse-jon-gui-data-camera-day)
(def ser-build-jon-gui-data-camera-heat ser/build-jon-gui-data-camera-heat)
(def ser-parse-jon-gui-data-camera-heat ser/parse-jon-gui-data-camera-heat)
(def ser-build-jon-gui-data-rec-osd ser/build-jon-gui-data-rec-osd)
(def ser-parse-jon-gui-data-rec-osd ser/parse-jon-gui-data-rec-osd)
(def ser-build-jon-gui-data-day-cam-glass-heater
  ser/build-jon-gui-data-day-cam-glass-heater)
(def ser-parse-jon-gui-data-day-cam-glass-heater
  ser/parse-jon-gui-data-day-cam-glass-heater)
(def ser-build-jon-gui-data-actual-space-time
  ser/build-jon-gui-data-actual-space-time)
(def ser-parse-jon-gui-data-actual-space-time
  ser/parse-jon-gui-data-actual-space-time)
(def ser-build-jon-gui-state ser/build-jon-gui-state)
(def ser-parse-jon-gui-state ser/parse-jon-gui-state)
