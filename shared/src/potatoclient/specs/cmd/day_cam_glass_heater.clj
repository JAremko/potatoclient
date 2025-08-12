(ns potatoclient.specs.cmd.day-cam-glass-heater
  "Day Camera Glass Heater command specs matching buf.validate constraints.
   Based on jon_shared_cmd_day_cam_glass_heater.proto.
   All maps use {:closed true} to catch typos and invalid keys."
  (:require
   [potatoclient.malli.registry :as registry]
   [potatoclient.specs.oneof-edn :as oneof-edn]))

;; Day cam glass heater command specs - simplified placeholders
;; This is a oneof structure with multiple command types

(def enable-spec [:map {:closed true}])
(def disable-spec [:map {:closed true}])
(def set-temperature-spec
  [:map {:closed true}
   [:temperature [:float {:min -40.0 :max 80.0}]]])

;; Main Day Cam Glass Heater command spec using oneof
(def day-cam-glass-heater-command-spec
  [:oneof_edn
   [:enable enable-spec]
   [:disable disable-spec]
   [:set_temperature set-temperature-spec]
   ;; Add more commands as needed
   ])

(registry/register! :cmd/day-cam-glass-heater day-cam-glass-heater-command-spec)