(ns potatoclient.specs.cmd.lira
  "LIRA command specs matching buf.validate constraints.
   Based on jon_shared_cmd_lira.proto.
   All maps use {:closed true} to catch typos and invalid keys."
  (:require
   [potatoclient.malli.registry :as registry]))

;; LIRA command specs - based on proto-explorer findings
;; This is a oneof structure with one command type

;; JonGuiDataLiraTarget specification
(def lira-target-spec
  [:map {:closed true}
   [:timestamp [:int {:min 0}]]
   [:target_longitude [:double {:min -180.0 :max 180.0}]]
   [:target_latitude :position/latitude]
   [:target_altitude :position/altitude]
   [:target_azimuth :angle/azimuth]
   [:target_elevation :angle/elevation]
   [:distance [:double {:min 0.0}]]
   [:uuid_part1 [:int]]
   [:uuid_part2 [:int]]
   [:uuid_part3 [:int]]
   [:uuid_part4 [:int]]])

;; Refine target command
(def refine-target-spec
  [:map {:closed true}
   [:target lira-target-spec]])

;; Main LIRA command spec using oneof
(def lira-command-spec
  [:oneof
   [:refine_target refine-target-spec]])

(registry/register! :cmd/lira lira-command-spec)