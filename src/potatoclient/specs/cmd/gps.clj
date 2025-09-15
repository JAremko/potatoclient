(ns potatoclient.specs.cmd.gps
  "GPS command specs matching buf.validate constraints.
   Based on jon_shared_cmd_gps.proto.
   All maps use {:closed true} to catch typos and invalid keys."
  (:require
    [potatoclient.malli.registry :as registry]))

;; Manual position
(def set-manual-position-spec
  "SetManualPosition spec - manually specified GPS coordinates for override"
  [:map {:closed true}
   [:latitude :position/latitude]
   [:longitude :position/longitude]
   [:altitude :position/altitude]])

(def set-use-manual-position-spec
  "SetUseManualPosition spec - enables/disables use of manually set GPS position"
  [:map {:closed true}
   [:flag [:boolean]]])

;; Main GPS command spec using oneof - all 5 commands
(def gps-command-spec
  "GPS command root spec - all GPS control operations including manual position override"
  [:oneof
   [:start :cmd/empty]
   [:stop :cmd/empty]
   [:set_manual_position set-manual-position-spec]
   [:set_use_manual_position set-use-manual-position-spec]
   [:get_meteo :cmd/empty]])

(registry/register-spec! :cmd/gps gps-command-spec)
