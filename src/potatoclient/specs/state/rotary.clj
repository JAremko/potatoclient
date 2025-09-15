(ns potatoclient.specs.state.rotary
  "Rotary message spec matching buf.validate constraints and EDN output format.
   All maps use {:closed true} to catch typos and invalid keys."
  (:require
    [potatoclient.specs.common]
    [potatoclient.malli.registry :as registry]))

;; ScanNode message spec
;; All 7 fields from proto definition

(def scan-node-spec
  "Scan node spec - represents a single scan position in the rotary scan sequence"
  [:map {:closed true}
   [:index :proto/int32-positive]
   [:DayZoomTableValue :proto/int32-positive]
   [:HeatZoomTableValue :proto/int32-positive]
   [:azimuth :angle/azimuth]
   [:elevation :angle/elevation]
   [:linger [:double {:min 0.0}]]
   [:speed :speed/normalized]])

(registry/register-spec! :rotary/scan-node scan-node-spec)

;; JonGuiDataRotary message spec
;; All 17 fields from proto definition
;; All angle fields now use double in proto

(def rotary-message-spec
  "Rotary state message spec - complete rotary platform state including position and scan status"
  [:map {:closed true}
   [:azimuth :angle/azimuth]  ; double in proto
   [:azimuth_speed [:double {:min -1.0 :max 1.0}]]  ; double in proto
   [:elevation :angle/elevation]  ; double in proto
   [:elevation_speed [:double {:min -1.0 :max 1.0}]]  ; double in proto
   [:platform_azimuth :angle/azimuth]  ; double in proto
   [:platform_elevation :angle/elevation]  ; double in proto
   [:platform_bank :angle/bank]  ; double in proto
   [:is_moving :boolean]
   [:mode :enum/rotary-mode]
   [:is_scanning :boolean]
   [:is_scanning_paused :boolean]
   [:use_rotary_as_compass :boolean]
   [:scan_target :proto/int32-positive]
   [:scan_target_max :proto/int32-positive]
   [:sun_azimuth :angle/azimuth]  ; double in proto
   [:sun_elevation :angle/sun-elevation]  ; double in proto (0-360 due to proto constraint bug)
   [:current_scan_node :rotary/scan-node]])

(registry/register-spec! :state/rotary rotary-message-spec)