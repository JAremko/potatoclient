(ns test.deps.state
  "State index - re-exports all state namespaces"
  (:require [test.deps.ser.lrf :as ser]
            [test.deps.ser.rotary :as ser1]
            [test.deps.ser.compass :as ser2]
            [test.deps.ser.day-cam-glass-heater :as ser3]
            [test.deps.ser.types :as ser4]
            [test.deps.ser.data :as ser5]
            [test.deps.ser.actual-space-time :as ser6]
            [test.deps.ser.compass-calibration :as ser7]
            [test.deps.ser.system :as ser8]
            [test.deps.ser.rec-osd :as ser9]
            [test.deps.ser.time :as ser10]
            [test.deps.ser.gps :as ser11]
            [test.deps.ser.camera-heat :as ser12]
            [test.deps.ser.camera-day :as ser13]))

;; Re-export all public functions from sub-namespaces
;; This supports testing without needing to know the internal namespace
;; structure

(def ser-build-jon-gui-data-lrf ser/build-jon-gui-data-lrf)
(def ser-parse-jon-gui-data-lrf ser/parse-jon-gui-data-lrf)
(def ser-build-jon-gui-data-target ser/build-jon-gui-data-target)
(def ser-parse-jon-gui-data-target ser/parse-jon-gui-data-target)
(def ser-build-rgb-color ser/build-rgb-color)
(def ser-parse-rgb-color ser/parse-rgb-color)
(def ser1-build-jon-gui-data-rotary ser1/build-jon-gui-data-rotary)
(def ser1-parse-jon-gui-data-rotary ser1/parse-jon-gui-data-rotary)
(def ser1-build-scan-node ser1/build-scan-node)
(def ser1-parse-scan-node ser1/parse-scan-node)
(def ser2-build-jon-gui-data-compass ser2/build-jon-gui-data-compass)
(def ser2-parse-jon-gui-data-compass ser2/parse-jon-gui-data-compass)
(def ser3-build-jon-gui-data-day-cam-glass-heater
  ser3/build-jon-gui-data-day-cam-glass-heater)
(def ser3-parse-jon-gui-data-day-cam-glass-heater
  ser3/parse-jon-gui-data-day-cam-glass-heater)
(def ser4-build-jon-gui-data-meteo ser4/build-jon-gui-data-meteo)
(def ser4-parse-jon-gui-data-meteo ser4/parse-jon-gui-data-meteo)
(def ser4-build-jon-gui-data-meteo ser4/build-jon-gui-data-meteo)
(def ser4-parse-jon-gui-data-meteo ser4/parse-jon-gui-data-meteo)
(def ser5-build-jon-gui-state ser5/build-jon-gui-state)
(def ser5-parse-jon-gui-state ser5/parse-jon-gui-state)
(def ser6-build-jon-gui-data-actual-space-time
  ser6/build-jon-gui-data-actual-space-time)
(def ser6-parse-jon-gui-data-actual-space-time
  ser6/parse-jon-gui-data-actual-space-time)
(def ser7-build-jon-gui-data-compass-calibration
  ser7/build-jon-gui-data-compass-calibration)
(def ser7-parse-jon-gui-data-compass-calibration
  ser7/parse-jon-gui-data-compass-calibration)
(def ser8-build-jon-gui-data-system ser8/build-jon-gui-data-system)
(def ser8-parse-jon-gui-data-system ser8/parse-jon-gui-data-system)
(def ser9-build-jon-gui-data-rec-osd ser9/build-jon-gui-data-rec-osd)
(def ser9-parse-jon-gui-data-rec-osd ser9/parse-jon-gui-data-rec-osd)
(def ser10-build-jon-gui-data-time ser10/build-jon-gui-data-time)
(def ser10-parse-jon-gui-data-time ser10/parse-jon-gui-data-time)
(def ser11-build-jon-gui-data-gps ser11/build-jon-gui-data-gps)
(def ser11-parse-jon-gui-data-gps ser11/parse-jon-gui-data-gps)
(def ser12-build-jon-gui-data-camera-heat ser12/build-jon-gui-data-camera-heat)
(def ser12-parse-jon-gui-data-camera-heat ser12/parse-jon-gui-data-camera-heat)
(def ser13-build-jon-gui-data-camera-day ser13/build-jon-gui-data-camera-day)
(def ser13-parse-jon-gui-data-camera-day ser13/parse-jon-gui-data-camera-day)
