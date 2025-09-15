(ns potatoclient.specs.state.gps
  "GPS message spec matching buf.validate constraints and EDN output format.
   All maps use {:closed true} to catch typos and invalid keys."
  (:require
    [potatoclient.specs.common]
    [potatoclient.malli.registry :as registry]))

;; JonGuiDataGps message spec
;; All 8 fields from proto definition

(def gps-message-spec
  "GPS state spec - current GPS position, fix status, and satellite information"
  [:map {:closed true}
   [:longitude :position/longitude]
   [:latitude :position/latitude]
   [:altitude :position/altitude]
   [:manual_longitude :position/longitude]
   [:manual_latitude :position/latitude]
   [:manual_altitude :position/altitude]
   [:fix_type :enum/gps-fix-type]
   [:use_manual :boolean]])

(registry/register-spec! :state/gps gps-message-spec)