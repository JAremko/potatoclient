(ns potatoclient.specs.state.day-cam-glass-heater
  "Day Camera Glass Heater message spec matching buf.validate constraints and EDN output.
   Based on jon_shared_data_day_cam_glass_heater.proto.
   All maps use {:closed true} to catch typos and invalid keys."
  (:require
    [potatoclient.specs.common]
    [potatoclient.malli.registry :as registry]))

;; JonGuiDataDayCamGlassHeater message spec
;; All 2 fields from proto definition

(def day-cam-glass-heater-message-spec
  [:map {:closed true}
   [:temperature [:double {:min -273.15 :max 660.32}]]
   [:status :boolean]])

(registry/register-spec! :state/day-cam-glass-heater day-cam-glass-heater-message-spec)