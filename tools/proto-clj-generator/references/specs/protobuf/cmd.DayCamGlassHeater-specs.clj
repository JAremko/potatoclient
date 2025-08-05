(ns potatoclient.specs.cmd.DayCamGlassHeater
  "Generated Malli specs from protobuf descriptors"
  (:require [malli.core :as m]
            [malli.generator :as mg]))

;; Note: FUNCTION-PLACEHOLDER markers indicate where runtime functions are needed
;; These will be replaced with actual implementations when loaded

(def root
  "Schema for root"
  [:map
   [:cmd
    [:oneof
     {:start [:map [:start :cmd/start]],
      :stop [:map [:stop :cmd/stop]],
      :turn-on [:map [:turn-on :cmd/turn-on]],
      :turn-off [:map [:turn-off :cmd/turn-off]],
      :get-meteo [:map [:get-meteo :cmd/get-meteo]]}]]])


(def start "Schema for start" [:map])


(def stop "Schema for stop" [:map])


(def turn-on "Schema for turn-on" [:map])


(def turn-off "Schema for turn-off" [:map])


(def get-meteo "Schema for get-meteo" [:map])
