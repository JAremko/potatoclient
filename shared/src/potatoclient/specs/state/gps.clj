(ns potatoclient.specs.state.gps
  "GPS message spec matching buf.validate constraints and EDN output format.
   All maps use {:closed true} to catch typos and invalid keys."
  (:require
   [potatoclient.malli.registry :as registry]
   [potatoclient.specs.common]))

;; GPS message spec based on proto definition and actual EDN output:
;; All 8 fields from JonGuiDataGps proto message

(def gps-message-spec
  [:map {:closed true}
   [:altitude :position/altitude]
   [:fix_type :enum/gps-fix-type]
   [:latitude :position/latitude]
   [:longitude :position/longitude]
   [:manual_altitude {:optional true} :position/altitude]
   [:manual_latitude :position/latitude]
   [:manual_longitude :position/longitude]
   [:use_manual {:optional true} :boolean]])

(registry/register! :state/gps gps-message-spec)