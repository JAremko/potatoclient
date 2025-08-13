(ns potatoclient.specs.cmd.day-cam-glass-heater
  "Day Camera Glass Heater command specs matching buf.validate constraints.
   Based on jon_shared_cmd_day_cam_glass_heater.proto.
   All maps use {:closed true} to catch typos and invalid keys."
  (:require
   [potatoclient.malli.registry :as registry]
   [potatoclient.specs.oneof-edn :as oneof-edn]))

;; Day cam glass heater command specs - based on proto-explorer findings
;; This is a oneof structure with 5 command types

(def start-spec [:map {:closed true}])
(def stop-spec [:map {:closed true}])
(def turn-on-spec [:map {:closed true}])
(def turn-off-spec [:map {:closed true}])
(def get-meteo-spec [:map {:closed true}])

;; Main Day Cam Glass Heater command spec using oneof
(def day-cam-glass-heater-command-spec
  [:oneof_edn
   [:start start-spec]
   [:stop stop-spec]
   [:turn_on turn-on-spec]
   [:turn_off turn-off-spec]
   [:get_meteo get-meteo-spec]])

(registry/register! :cmd/day-cam-glass-heater day-cam-glass-heater-command-spec)