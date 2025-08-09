(ns potatoclient.specs.cmd.day-cam-glass-heater
  "Day Camera Glass Heater command specs matching buf.validate constraints.
   Based on jon_shared_cmd_day_cam_glass_heater.proto.
   All maps use {:closed true} to catch typos and invalid keys."
  (:require
   [potatoclient.malli.registry :as registry]))

;; Day cam glass heater command specs - placeholder for now
;; Will need to check proto file for exact structure
;; Likely includes: enable/disable heater, set temperature

(def day-cam-glass-heater-command-spec
  [:map {:closed true}
   ;; TODO: Add fields based on proto definition
   ])

(registry/register! :cmd/day-cam-glass-heater day-cam-glass-heater-command-spec)