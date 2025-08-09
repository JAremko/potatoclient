(ns validate.specs.state.actual-space-time
  "Actual Space Time message spec matching buf.validate constraints and EDN output.
   Based on jon_shared_data_actual_space_time.proto.
   All maps use {:closed true} to catch typos and invalid keys."
  (:require
   [potatoclient.malli.registry :as registry]))

;; JonGuiDataActualSpaceTime message spec based on EDN output:
;; {:altitude 0.291143
;;  :azimuth 256.62
;;  :elevation 7.04
;;  :latitude 50.02363
;;  :longitude 15.815215
;;  :timestamp 1754665407}

(def actual-space-time-message-spec
  [:map {:closed true}
   [:altitude :position/altitude]
   [:azimuth :angle/azimuth]
   [:elevation :angle/elevation]
   [:latitude :position/latitude]
   [:longitude :position/longitude]
   [:timestamp :time/unix-timestamp]])

(registry/register! :state/actual-space-time actual-space-time-message-spec)