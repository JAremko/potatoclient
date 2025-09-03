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
   [:azimuth :angle/azimuth]  ; double in proto
   [:elevation :angle/elevation]  ; double in proto
   [:bank :angle/bank]  ; double in proto
   [:latitude [:double {:min -90.0 :max 90.0}]]  ; double in proto
   [:longitude [:double {:min -180.0 :max 180.0}]]  ; double in proto
   [:altitude :position/altitude]  ; double in proto
   [:timestamp :time/unix-timestamp]])

(registry/register-spec! :state/actual-space-time actual-space-time-message-spec)