(ns potatoclient.specs.state.gps
  "GPS message spec matching buf.validate constraints and EDN output format.
   All maps use {:closed true} to catch typos and invalid keys."
  (:require
   [potatoclient.malli.registry :as registry]))

;; GPS message spec based on actual EDN output:
;; {:altitude 0.291143
;;  :fix_type :JON_GUI_DATA_GPS_FIX_TYPE_3D
;;  :latitude 50.023629
;;  :longitude 15.815214999999998
;;  :manual_latitude 50.023604
;;  :manual_longitude 15.815316}

(def gps-message-spec
  [:map {:closed true}
   [:altitude :position/altitude]
   [:fix_type :enum/gps-fix-type]
   [:latitude :position/latitude]
   [:longitude :position/longitude]
   [:manual_latitude :position/latitude]
   [:manual_longitude :position/longitude]])

(registry/register! :state/gps gps-message-spec)