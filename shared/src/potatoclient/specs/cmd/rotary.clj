(ns potatoclient.specs.cmd.rotary
  "Rotary Platform command specs matching buf.validate constraints.
   Based on jon_shared_cmd_rotary.proto.
   All maps use {:closed true} to catch typos and invalid keys."
  (:require
   [potatoclient.malli.registry :as registry]
   [potatoclient.specs.oneof-edn :as oneof-edn]))

;; Rotary command specs - based on proto-explorer findings
;; This is a oneof structure with 24 command types

;; Basic control commands
(def start-spec [:map {:closed true}])
(def stop-spec [:map {:closed true}])
(def halt-spec [:map {:closed true}])

;; Axis control
(def axis-spec 
  [:map {:closed true}
   [:azimuth [:float]]
   [:elevation [:float]]])

;; Platform position controls
(def set-platform-azimuth-spec
  [:map {:closed true}
   [:value [:float {:min -360.0 :max 360.0}]]])
   
(def set-platform-elevation-spec  
  [:map {:closed true}
   [:value [:float {:min -90.0 :max 90.0}]]])

(def set-platform-bank-spec
  [:map {:closed true}
   [:value [:float {:min -180.0 :max 180.0}]]])

;; Mode and configuration
(def set-mode-spec
  [:map {:closed true}
   [:mode [:enum 
           :JON_GUI_DATA_ROTARY_MODE_INITIALIZATION
           :JON_GUI_DATA_ROTARY_MODE_SPEED  
           :JON_GUI_DATA_ROTARY_MODE_POSITION
           :JON_GUI_DATA_ROTARY_MODE_STABILIZATION
           :JON_GUI_DATA_ROTARY_MODE_TARGETING
           :JON_GUI_DATA_ROTARY_MODE_VIDEO_TRACKER]]])

(def set-use-rotary-as-compass-spec
  [:map {:closed true}
   [:value [:boolean]]])

;; GPS integration
(def rotate-to-gps-spec
  [:map {:closed true}
   [:latitude [:double]]
   [:longitude [:double]]])

(def set-origin-gps-spec
  [:map {:closed true}
   [:latitude [:double]]
   [:longitude [:double]]])

;; NDC rotation
(def rotate-to-ndc-spec
  [:map {:closed true}
   [:x [:float]]
   [:y [:float]]])

;; Scan operations
(def scan-start-spec [:map {:closed true}])
(def scan-stop-spec [:map {:closed true}])
(def scan-pause-spec [:map {:closed true}])
(def scan-unpause-spec [:map {:closed true}])
(def scan-next-spec [:map {:closed true}])
(def scan-prev-spec [:map {:closed true}])

(def scan-select-node-spec
  [:map {:closed true}
   [:index [:int {:min 0}]]])

(def scan-add-node-spec
  [:map {:closed true}
   [:azimuth [:float]]
   [:elevation [:float]]
   [:linger [:float {:min 0.0}]]
   [:dayzoomtablevalue [:int {:min 0}]]
   [:heatzoomtablevalue [:int {:min 0}]]])

(def scan-delete-node-spec
  [:map {:closed true}
   [:index [:int {:min 0}]]])

(def scan-update-node-spec
  [:map {:closed true}
   [:index [:int {:min 0}]]
   [:azimuth [:float]]
   [:elevation [:float]]
   [:linger [:float {:min 0.0}]]
   [:dayzoomtablevalue [:int {:min 0}]]
   [:heatzoomtablevalue [:int {:min 0}]]])

(def scan-refresh-node-list-spec [:map {:closed true}])

;; Meteo data
(def get-meteo-spec [:map {:closed true}])

;; Main rotary command spec using oneof - all 24 commands
(def rotary-command-spec
  [:oneof_edn
   [:start start-spec]
   [:stop stop-spec]
   [:axis axis-spec]
   [:set_platform_azimuth set-platform-azimuth-spec]
   [:set_platform_elevation set-platform-elevation-spec]
   [:set_platform_bank set-platform-bank-spec]
   [:halt halt-spec]
   [:set_use_rotary_as_compass set-use-rotary-as-compass-spec]
   [:rotate_to_gps rotate-to-gps-spec]
   [:set_origin_gps set-origin-gps-spec]
   [:set_mode set-mode-spec]
   [:rotate_to_ndc rotate-to-ndc-spec]
   [:scan_start scan-start-spec]
   [:scan_stop scan-stop-spec]
   [:scan_pause scan-pause-spec]
   [:scan_unpause scan-unpause-spec]
   [:scan_next scan-next-spec]
   [:scan_prev scan-prev-spec]
   [:scan_select_node scan-select-node-spec]
   [:scan_add_node scan-add-node-spec]
   [:scan_delete_node scan-delete-node-spec]
   [:scan_update_node scan-update-node-spec]
   [:scan_refresh_node_list scan-refresh-node-list-spec]
   [:get_meteo get-meteo-spec]])

(registry/register! :cmd/rotary rotary-command-spec)