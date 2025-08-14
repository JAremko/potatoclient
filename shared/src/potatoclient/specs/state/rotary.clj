(ns potatoclient.specs.state.rotary
  "Rotary message spec matching buf.validate constraints and EDN output format.
   All maps use {:closed true} to catch typos and invalid keys."
  (:require
   [potatoclient.specs.common]
   [potatoclient.malli.registry :as registry]))

;; ScanNode message spec
;; All 7 fields from proto definition

(def scan-node-spec
  [:map {:closed true}
   [:index [:int {:min 0}]]
   [:DayZoomTableValue [:int {:min 0}]]
   [:HeatZoomTableValue [:int {:min 0}]]
   [:azimuth :angle/azimuth]
   [:elevation :angle/elevation]
   [:linger [:double {:min 0.0}]]
   [:speed :speed/normalized]])

(registry/register! :rotary/scan-node scan-node-spec)

;; JonGuiDataRotary message spec
;; All 17 fields from proto definition

(def rotary-message-spec
  [:map {:closed true}
   [:azimuth :angle/azimuth]
   [:azimuth_speed [:double {:min -1.0 :max 1.0}]]
   [:elevation :angle/elevation]
   [:elevation_speed [:double {:min -1.0 :max 1.0}]]
   [:platform_azimuth :angle/azimuth]
   [:platform_elevation :angle/elevation]
   [:platform_bank :angle/bank]
   [:is_moving :boolean]
   [:mode :enum/rotary-mode]
   [:is_scanning :boolean]
   [:is_scanning_paused :boolean]
   [:use_rotary_as_compass :boolean]
   [:scan_target [:int {:min 0}]]
   [:scan_target_max [:int {:min 0}]]
   [:sun_azimuth :angle/azimuth]
   [:sun_elevation :angle/azimuth]
   [:current_scan_node :rotary/scan-node]])

(registry/register! :state/rotary rotary-message-spec)