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
   [:azimuth :angle/azimuth-float]  ; float in proto
   [:elevation :angle/elevation-float]  ; float in proto
   [:bank :angle/bank-float]  ; float in proto
   [:latitude [:float {:min -90.0 :max 90.0}]]  ; float in proto
   [:longitude [:float {:min -180.0 :max 180.0}]]  ; float in proto
   [:altitude :position/altitude]  ; double in proto
   [:timestamp :time/unix-timestamp]])

(registry/register! :state/actual-space-time actual-space-time-message-spec)