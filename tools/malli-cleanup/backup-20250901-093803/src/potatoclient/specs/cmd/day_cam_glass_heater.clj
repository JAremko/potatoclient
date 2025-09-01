(ns potatoclient.specs.cmd.day-cam-glass-heater
  "Day Camera Glass Heater command specs matching buf.validate constraints.
   Based on jon_shared_cmd_day_cam_glass_heater.proto.
   All maps use {:closed true} to catch typos and invalid keys."
  (:require
   [potatoclient.malli.registry :as registry]))

;; Day cam glass heater command specs - based on proto-explorer findings
;; This is a oneof structure with 5 command types

;; Main Day Cam Glass Heater command spec using oneof
(def day-cam-glass-heater-command-spec
  [:oneof
   [:start :cmd/empty]
   [:stop :cmd/empty]
   [:turn_on :cmd/empty]
   [:turn_off :cmd/empty]
   [:get_meteo :cmd/empty]])

(registry/register! :cmd/day-cam-glass-heater day-cam-glass-heater-command-spec)