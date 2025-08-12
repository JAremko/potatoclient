(ns potatoclient.specs.cmd.rotary
  "Rotary Platform command specs matching buf.validate constraints.
   Based on jon_shared_cmd_rotary.proto.
   All maps use {:closed true} to catch typos and invalid keys."
  (:require
   [potatoclient.malli.registry :as registry]
   [potatoclient.specs.oneof-edn :as oneof-edn]))

;; Rotary command specs 
;; This is a oneof structure with multiple command types

;; Individual rotary commands (simplified placeholders)
(def stop-spec [:map {:closed true}])
(def set-position-spec 
  [:map {:closed true}
   [:azimuth [:float {:min 0.0 :max 360.0}]]
   [:elevation [:float {:min -90.0 :max 90.0}]]])
   
(def set-platform-azimuth-spec
  [:map {:closed true}
   [:value [:float {:min 0.0 :max 360.0}]]])
   
(def set-platform-elevation-spec  
  [:map {:closed true}
   [:value [:float {:min -90.0 :max 90.0}]]])

(def set-mode-spec
  [:map {:closed true}
   [:mode [:enum 
           :JON_GUI_DATA_ROTARY_MODE_INITIALIZATION
           :JON_GUI_DATA_ROTARY_MODE_SPEED  
           :JON_GUI_DATA_ROTARY_MODE_POSITION
           :JON_GUI_DATA_ROTARY_MODE_STABILIZATION
           :JON_GUI_DATA_ROTARY_MODE_TARGETING
           :JON_GUI_DATA_ROTARY_MODE_VIDEO_TRACKER]]])

(def scan-node-spec
  [:map {:closed true}
   [:speed [:float {:min 0.0 :max 1.0}]]
   [:azimuth [:float {:min 0.0 :max 360.0}]]
   [:elevation [:float {:min -90.0 :max 90.0}]]
   [:linger [:float {:min 0.0}]]
   [:index [:int {:min 0}]]
   [:dayzoomtablevalue [:int {:min 0 :max 10}]]
   [:heatzoomtablevalue [:int {:min 0 :max 10}]]])

;; Main rotary command spec using oneof
(def rotary-command-spec
  [:oneof_edn
   [:stop stop-spec]
   [:set_position set-position-spec]
   [:set_platform_azimuth set-platform-azimuth-spec]
   [:set_platform_elevation set-platform-elevation-spec]
   [:set_mode set-mode-spec]
   [:scan_node scan-node-spec]
   ;; Add more commands as needed
   ])

(registry/register! :cmd/rotary rotary-command-spec)