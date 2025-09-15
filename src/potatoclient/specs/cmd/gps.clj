(ns potatoclient.specs.cmd.gps
  "GPS command specs matching buf.validate constraints.
   Based on jon_shared_cmd_gps.proto.
   All maps use {:closed true} to catch typos and invalid keys."
  (:require
    [potatoclient.malli.registry :as registry]))

;; Manual position
(def set-manual-position-spec
  "Specification for setting manual GPS coordinates when GPS signal is unavailable.
   Includes latitude [-90, 90], longitude [-180, 180], and altitude in meters."
  [:map {:closed true}
   [:latitude :position/latitude]
   [:longitude :position/longitude]
   [:altitude :position/altitude]])

(def set-use-manual-position-spec
  "Specification for enabling/disabling use of manual GPS position.
   When enabled, system uses manual coordinates instead of GPS receiver data."
  [:map {:closed true}
   [:flag [:boolean]]])

;; Main GPS command spec using oneof - all 5 commands
(def gps-command-spec
  "Root specification for GPS commands using protobuf oneof pattern.
   Supports start/stop GPS receiver, manual position override, and meteorological data retrieval."
  [:oneof
   [:start :cmd/empty]
   [:stop :cmd/empty]
   [:set_manual_position set-manual-position-spec]
   [:set_use_manual_position set-use-manual-position-spec]
   [:get_meteo :cmd/empty]])

(registry/register-spec! :cmd/gps gps-command-spec)
