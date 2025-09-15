(ns potatoclient.specs.cmd.lrf
  "LRF (Laser Range Finder) command specs matching buf.validate constraints.
   Based on jon_shared_cmd_lrf.proto.
   All maps use {:closed true} to catch typos and invalid keys."
  (:require
    [potatoclient.malli.registry :as registry]))

;; LRF command specs
;; This is a oneof structure with 15 command types

;; Scan control with mode setting
(def set-scan-mode-spec
  "SetScanMode spec - configures laser rangefinder scanning mode"
  [:map {:closed true}
   [:mode :enum/lrf-scan-modes]])

;; Main LRF command spec using oneof - all 15 commands
(def lrf-command-spec
  "LRF command root spec - all laser rangefinder control operations"
  [:oneof
   [:measure :cmd/empty]
   [:scan_on :cmd/empty]
   [:scan_off :cmd/empty]
   [:start :cmd/empty]
   [:stop :cmd/empty]
   [:target_designator_off :cmd/empty]
   [:target_designator_on_mode_a :cmd/empty]
   [:target_designator_on_mode_b :cmd/empty]
   [:enable_fog_mode :cmd/empty]
   [:disable_fog_mode :cmd/empty]
   [:set_scan_mode set-scan-mode-spec]
   [:new_session :cmd/empty]
   [:get_meteo :cmd/empty]
   [:refine_on :cmd/empty]
   [:refine_off :cmd/empty]])

(registry/register-spec! :cmd/lrf lrf-command-spec)