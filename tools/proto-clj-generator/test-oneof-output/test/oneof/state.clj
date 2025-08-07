(ns test.oneof.state
  "State index - re-exports all state namespaces"
  (:require [test.oneof.ser :as ser]))

;; Re-export all public functions from sub-namespaces
;; This supports testing without needing to know the internal namespace
;; structure

(def build-jon-gui-data-meteo ser/build-jon-gui-data-meteo)
(def parse-jon-gui-data-meteo ser/parse-jon-gui-data-meteo)
(def build-jon-gui-data-meteo ser/build-jon-gui-data-meteo)
(def parse-jon-gui-data-meteo ser/parse-jon-gui-data-meteo)
(def build-jon-gui-data-time ser/build-jon-gui-data-time)
(def parse-jon-gui-data-time ser/parse-jon-gui-data-time)
(def build-jon-gui-data-system ser/build-jon-gui-data-system)
(def parse-jon-gui-data-system ser/parse-jon-gui-data-system)
(def build-jon-gui-data-lrf ser/build-jon-gui-data-lrf)
(def parse-jon-gui-data-lrf ser/parse-jon-gui-data-lrf)
(def build-jon-gui-data-target ser/build-jon-gui-data-target)
(def parse-jon-gui-data-target ser/parse-jon-gui-data-target)
(def build-rgb-color ser/build-rgb-color)
(def parse-rgb-color ser/parse-rgb-color)
(def build-jon-gui-data-gps ser/build-jon-gui-data-gps)
(def parse-jon-gui-data-gps ser/parse-jon-gui-data-gps)
(def build-jon-gui-data-compass ser/build-jon-gui-data-compass)
(def parse-jon-gui-data-compass ser/parse-jon-gui-data-compass)
(def build-jon-gui-data-compass-calibration
  ser/build-jon-gui-data-compass-calibration)
(def parse-jon-gui-data-compass-calibration
  ser/parse-jon-gui-data-compass-calibration)
(def build-jon-gui-data-rotary ser/build-jon-gui-data-rotary)
(def parse-jon-gui-data-rotary ser/parse-jon-gui-data-rotary)
(def build-scan-node ser/build-scan-node)
(def parse-scan-node ser/parse-scan-node)
(def build-jon-gui-data-camera-day ser/build-jon-gui-data-camera-day)
(def parse-jon-gui-data-camera-day ser/parse-jon-gui-data-camera-day)
(def build-jon-gui-data-camera-heat ser/build-jon-gui-data-camera-heat)
(def parse-jon-gui-data-camera-heat ser/parse-jon-gui-data-camera-heat)
(def build-jon-gui-data-rec-osd ser/build-jon-gui-data-rec-osd)
(def parse-jon-gui-data-rec-osd ser/parse-jon-gui-data-rec-osd)
(def build-jon-gui-data-day-cam-glass-heater
  ser/build-jon-gui-data-day-cam-glass-heater)
(def parse-jon-gui-data-day-cam-glass-heater
  ser/parse-jon-gui-data-day-cam-glass-heater)
(def build-jon-gui-data-actual-space-time
  ser/build-jon-gui-data-actual-space-time)
(def parse-jon-gui-data-actual-space-time
  ser/parse-jon-gui-data-actual-space-time)
(def build-jon-gui-state ser/build-jon-gui-state)
(def parse-jon-gui-state ser/parse-jon-gui-state)
