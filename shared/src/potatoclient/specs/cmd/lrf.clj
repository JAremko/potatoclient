(ns potatoclient.specs.cmd.lrf
  "LRF (Laser Range Finder) command specs matching buf.validate constraints.
   Based on jon_shared_cmd_lrf.proto.
   All maps use {:closed true} to catch typos and invalid keys."
  (:require
   [potatoclient.malli.registry :as registry]
   [potatoclient.specs.oneof-edn :as oneof-edn]))

;; LRF command specs - based on proto-explorer findings
;; This is a oneof structure with 15 command types

;; Basic operations
(def measure-spec [:map {:closed true}])
(def start-spec [:map {:closed true}])
(def stop-spec [:map {:closed true}])
(def new-session-spec [:map {:closed true}])

;; Scan control
(def scan-on-spec [:map {:closed true}])
(def scan-off-spec [:map {:closed true}])
(def set-scan-mode-spec
  [:map {:closed true}
   [:mode [:enum 
           :JON_GUI_DATA_LRF_SCAN_MODE_LINE
           :JON_GUI_DATA_LRF_SCAN_MODE_RECT
           :JON_GUI_DATA_LRF_SCAN_MODE_3X3
           :JON_GUI_DATA_LRF_SCAN_MODE_5X5]]])

;; Target designator
(def target-designator-off-spec [:map {:closed true}])
(def target-designator-on-mode-a-spec [:map {:closed true}])
(def target-designator-on-mode-b-spec [:map {:closed true}])

;; Fog mode
(def enable-fog-mode-spec [:map {:closed true}])
(def disable-fog-mode-spec [:map {:closed true}])

;; Refine mode
(def refine-on-spec [:map {:closed true}])
(def refine-off-spec [:map {:closed true}])

;; Meteo
(def get-meteo-spec [:map {:closed true}])

;; Main LRF command spec using oneof - all 15 commands
(def lrf-command-spec
  [:oneof_edn
   [:measure measure-spec]
   [:scan_on scan-on-spec]
   [:scan_off scan-off-spec]
   [:start start-spec]
   [:stop stop-spec]
   [:target_designator_off target-designator-off-spec]
   [:target_designator_on_mode_a target-designator-on-mode-a-spec]
   [:target_designator_on_mode_b target-designator-on-mode-b-spec]
   [:enable_fog_mode enable-fog-mode-spec]
   [:disable_fog_mode disable-fog-mode-spec]
   [:set_scan_mode set-scan-mode-spec]
   [:new_session new-session-spec]
   [:get_meteo get-meteo-spec]
   [:refine_on refine-on-spec]
   [:refine_off refine-off-spec]])

(registry/register! :cmd/lrf lrf-command-spec)