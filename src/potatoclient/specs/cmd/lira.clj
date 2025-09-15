(ns potatoclient.specs.cmd.lira
  "LIRA command specs matching buf.validate constraints.
   Based on jon_shared_cmd_lira.proto.
   All maps use {:closed true} to catch typos and invalid keys."
  (:require
    [potatoclient.malli.registry :as registry]))

;; LIRA command specs
;; This is a oneof structure with one command type

;; JonGuiDataLiraTarget specification
(def lira-target-spec
  "LIRA target data spec - target information for laser-illuminated ranging and acquisition"
  [:map {:closed true}
   [:timestamp :time/unix-timestamp-int64]
   [:target_longitude [:double {:min -180.0 :max 180.0}]]
   [:target_latitude :position/latitude]
   [:target_altitude :position/altitude]
   [:target_azimuth :angle/azimuth]
   [:target_elevation :angle/elevation]
   [:distance [:double {:min 0.0}]]
   [:uuid_part1 :proto/int32]
   [:uuid_part2 :proto/int32]
   [:uuid_part3 :proto/int32]
   [:uuid_part4 :proto/int32]])

;; Refine target command
(def refine-target-spec
  "RefineTarget spec - command to refine LIRA target acquisition"
  [:map {:closed true}
   [:target lira-target-spec]])

;; Main LIRA command spec using oneof
(def lira-command-spec
  "LIRA command root spec - laser-illuminated ranging acquisition control"
  [:oneof
   [:refine_target refine-target-spec]])

(registry/register-spec! :cmd/lira lira-command-spec)