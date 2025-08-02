(ns potatoclient.specs.cmd.Compass "Generated Malli specs from protobuf descriptors" (:require [malli.core :as proto-explorer.spec-generator/m] [malli.generator :as proto-explorer.spec-generator/mg]))

;; Note: FUNCTION-PLACEHOLDER markers indicate where runtime functions are needed
;; These will be replaced with actual implementations when loaded

(def calibrate-cencel "Schema for calibrate-cencel" [:map])

(def start "Schema for start" [:map])

(def calibrate-start-short "Schema for calibrate-start-short" [:map])

(def set-offset-angle-elevation "Schema for set-offset-angle-elevation" [:map [:value [:and [:maybe :double] [:>= -90] [:<= 90]]]])

(def stop "Schema for stop" [:map])

(def calibrate-start-long "Schema for calibrate-start-long" [:map])

(def next "Schema for next" [:map])

(def root "Schema for root" [:map [:cmd [:oneof {:calibrate-cencel [:map [:calibrate-cencel [:maybe :cmd/calibrate-cencel]]], :start [:map [:start [:maybe :cmd/start]]], :set-offset-angle-elevation [:map [:set-offset-angle-elevation [:maybe :cmd/set-offset-angle-elevation]]], :stop [:map [:stop [:maybe :cmd/stop]]], :calibrate-next [:map [:calibrate-next [:maybe :cmd/calibrate-next]]], :get-meteo [:map [:get-meteo [:maybe :cmd/get-meteo]]], :set-use-rotary-position [:map [:set-use-rotary-position [:maybe :cmd/set-use-rotary-position]]], :set-magnetic-declination [:map [:set-magnetic-declination [:maybe :cmd/set-magnetic-declination]]], :start-calibrate-short [:map [:start-calibrate-short [:maybe :cmd/calibrate-start-short]]], :start-calibrate-long [:map [:start-calibrate-long [:maybe :cmd/calibrate-start-long]]], :set-offset-angle-azimuth [:map [:set-offset-angle-azimuth [:maybe :cmd/set-offset-angle-azimuth]]]}]]])

(def calibrate-next "Schema for calibrate-next" [:map])

(def get-meteo "Schema for get-meteo" [:map])

(def set-use-rotary-position "Schema for set-use-rotary-position" [:map [:flag [:maybe :boolean]]])

(def set-magnetic-declination "Schema for set-magnetic-declination" [:map [:value [:and [:maybe :double] [:>= -180] [:< 180]]]])

(def set-offset-angle-azimuth "Schema for set-offset-angle-azimuth" [:map [:value [:and [:maybe :double] [:>= -180] [:< 180]]]])