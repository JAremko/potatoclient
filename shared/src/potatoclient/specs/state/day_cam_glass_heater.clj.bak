(ns potatoclient.specs.state.day-cam-glass-heater
  "Day Camera Glass Heater message spec matching buf.validate constraints and EDN output.
   Based on jon_shared_data_day_cam_glass_heater.proto.
   All maps use {:closed true} to catch typos and invalid keys."
  (:require
   [potatoclient.malli.registry :as registry]))

;; JonGuiDataDayCamGlassHeater message spec based on EDN output:
;; {} (empty map in the example)
;;
;; This message appears to be empty in the sample but may have optional fields

(def day-cam-glass-heater-message-spec
  [:map {:closed true}
   ;; Add fields here when they appear in actual data
   ])

(registry/register! :state/day-cam-glass-heater day-cam-glass-heater-message-spec)