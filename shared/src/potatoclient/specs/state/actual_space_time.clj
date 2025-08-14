(ns potatoclient.specs.state.actual-space-time
  "Actual Space Time message spec matching buf.validate constraints and EDN output.
   Based on jon_shared_data_actual_space_time.proto.
   All maps use {:closed true} to catch typos and invalid keys."
  (:require
   [potatoclient.specs.common]
   [potatoclient.malli.registry :as registry]))

;; JonGuiDataActualSpaceTime message spec
;; All 7 fields from proto definition

(def actual-space-time-message-spec
  [:map {:closed true}
   [:azimuth :angle/azimuth]
   [:elevation :angle/elevation]
   [:bank :angle/bank]
   [:latitude :position/latitude]
   [:longitude :position/longitude]
   [:altitude :position/altitude]
   [:timestamp :time/unix-timestamp]])

(registry/register! :state/actual-space-time actual-space-time-message-spec)