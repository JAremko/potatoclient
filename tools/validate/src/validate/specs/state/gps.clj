(ns validate.specs.state.gps
  "GPS message spec matching buf.validate constraints and EDN output format.
   All maps use {:closed true} to catch typos and invalid keys."
  (:require
   [potatoclient.malli.registry :as registry]))

;; GPS message spec based on actual EDN output:
;; {:altitude 0.291143
;;  :fix-type :jon-gui-data-gps-fix-type-3d
;;  :latitude 50.023629
;;  :longitude 15.815214999999998
;;  :manual-latitude 50.023604
;;  :manual-longitude 15.815316}

(def gps-message-spec
  [:map {:closed true}
   [:altitude :position/altitude]
   [:fix-type :enum/gps-fix-type]
   [:latitude :position/latitude]
   [:longitude :position/longitude]
   [:manual-latitude :position/latitude]
   [:manual-longitude :position/longitude]])

(registry/register! :state/gps gps-message-spec)