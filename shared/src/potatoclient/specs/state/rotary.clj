(ns potatoclient.specs.state.rotary
  "Rotary message spec matching buf.validate constraints and EDN output format.
   All maps use {:closed true} to catch typos and invalid keys."
  (:require
   [potatoclient.malli.registry :as registry]))

;; Scan node spec based on actual EDN output:
;; {:azimuth 0.001
;;  :dayzoomtablevalue 1
;;  :elevation 0.001
;;  :heatzoomtablevalue 1
;;  :index 1
;;  :linger 0.001
;;  :speed 0.001}

(def scan-node-spec
  [:map {:closed true}
   [:azimuth :rotary/azimuth]
   [:DayZoomTableValue [:int {:min 0}]]
   [:elevation :rotary/elevation]
   [:HeatZoomTableValue [:int {:min 0}]]
   [:index [:int {:min 0}]]
   [:linger [:double {:min 0.0}]]
   [:speed :rotary/speed]])

(registry/register! :rotary/scan-node scan-node-spec)

;; Rotary message spec based on actual EDN output:
;; {:azimuth 335.3625
;;  :current_scan_node {...}
;;  :mode :JON_GUI_DATA_ROTARY_MODE_POSITION
;;  :platform_azimuth 256.62
;;  :platform_elevation 7.04
;;  :scan_target 1
;;  :scan_target_max 1}

(def rotary-message-spec
  [:map {:closed true}
   ;; All fields are optional except current_scan_node
   [:azimuth {:optional true} :angle/azimuth]
   [:azimuth_speed {:optional true} [:double {:min -1.0 :max 1.0}]]
   [:elevation {:optional true} :rotary/elevation]
   [:elevation_speed {:optional true} [:double {:min -1.0 :max 1.0}]]
   [:platform_azimuth {:optional true} :rotary/platform-azimuth]
   [:platform_elevation {:optional true} :rotary/elevation]
   [:platform_bank {:optional true} [:double {:min -180 :max 180}]]
   [:is_moving {:optional true} boolean?]
   [:mode {:optional true} :enum/rotary-mode]
   [:is_scanning {:optional true} boolean?]
   [:is_scanning_paused {:optional true} boolean?]
   [:use_rotary_as_compass {:optional true} boolean?]
   [:scan_target {:optional true} [:int {:min 0}]]
   [:scan_target_max {:optional true} [:int {:min 0}]]
   [:sun_azimuth {:optional true} :angle/azimuth]
   [:sun_elevation {:optional true} :angle/azimuth]
   ;; current_scan_node is required
   [:current_scan_node :rotary/scan-node]])

(registry/register! :state/rotary rotary-message-spec)