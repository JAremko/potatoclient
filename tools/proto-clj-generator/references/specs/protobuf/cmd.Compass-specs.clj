(ns potatoclient.specs.cmd.Compass
  "Generated Malli specs from protobuf descriptors"
  (:require [malli.core :as m]
            [malli.generator :as mg]))

;; Note: FUNCTION-PLACEHOLDER markers indicate where runtime functions are needed
;; These will be replaced with actual implementations when loaded

(def calibrate-cencel "Schema for calibrate-cencel" [:map])


(def start "Schema for start" [:map])


(def calibrate-start-short "Schema for calibrate-start-short" [:map])


(def set-offset-angle-elevation
  "Schema for set-offset-angle-elevation"
  [:map [:value [:and [:maybe :double] [:>= -90] [:<= 90]]]])


(def stop "Schema for stop" [:map])


(def calibrate-start-long "Schema for calibrate-start-long" [:map])


(def next "Schema for next" [:map])


(def root
  "Schema for root"
  [:map
   [:cmd
    [:oneof
     {:calibrate-cencel [:map [:calibrate-cencel :cmd/calibrate-cencel]],
      :start [:map [:start :cmd/start]],
      :set-offset-angle-elevation [:map
                                   [:set-offset-angle-elevation :cmd/set-offset-angle-elevation]],
      :stop [:map [:stop :cmd/stop]],
      :calibrate-next [:map [:calibrate-next :cmd/calibrate-next]],
      :get-meteo [:map [:get-meteo :cmd/get-meteo]],
      :set-use-rotary-position [:map [:set-use-rotary-position :cmd/set-use-rotary-position]],
      :set-magnetic-declination [:map [:set-magnetic-declination :cmd/set-magnetic-declination]],
      :start-calibrate-short [:map [:start-calibrate-short :cmd/calibrate-start-short]],
      :start-calibrate-long [:map [:start-calibrate-long :cmd/calibrate-start-long]],
      :set-offset-angle-azimuth [:map
                                 [:set-offset-angle-azimuth :cmd/set-offset-angle-azimuth]]}]]])


(def calibrate-next "Schema for calibrate-next" [:map])


(def get-meteo "Schema for get-meteo" [:map])


(def set-use-rotary-position "Schema for set-use-rotary-position" [:map [:flag [:maybe :boolean]]])


(def set-magnetic-declination
  "Schema for set-magnetic-declination"
  [:map [:value [:and [:maybe :double] [:>= -180] [:< 180]]]])


(def set-offset-angle-azimuth
  "Schema for set-offset-angle-azimuth"
  [:map [:value [:and [:maybe :double] [:>= -180] [:< 180]]]])
