(ns potatoclient.specs.cmd.osd
  "OSD (On-Screen Display) command specs matching buf.validate constraints.
   Based on jon_shared_cmd_osd.proto.
   All maps use {:closed true} to catch typos and invalid keys."
  (:require
    [potatoclient.malli.registry :as registry]))

;; OSD command specs
;; This is a oneof structure with 8 command types

;; Main OSD command spec using oneof - all 8 commands
(def osd-command-spec
  [:oneof
   [:show_default_screen :cmd/empty]
   [:show_lrf_measure_screen :cmd/empty]
   [:show_lrf_result_screen :cmd/empty]
   [:show_lrf_result_simplified_screen :cmd/empty]
   [:enable_heat_osd :cmd/empty]
   [:disable_heat_osd :cmd/empty]
   [:enable_day_osd :cmd/empty]
   [:disable_day_osd :cmd/empty]])

(registry/register! :cmd/osd osd-command-spec)