(ns potatoclient.specs.state.root
  "Root State message spec (JonGUIState) assembling all sub-messages.
   Based on jon_shared_data.proto.
   All maps use {:closed true} to catch typos and invalid keys."
  (:require
   [potatoclient.malli.registry :as registry]
   ;; Import common specs for protocol-version and other shared types
   [potatoclient.specs.common]
   ;; Import all sub-message specs
   [potatoclient.specs.state.actual-space-time]
   [potatoclient.specs.state.camera-day]
   [potatoclient.specs.state.camera-heat]
   [potatoclient.specs.state.compass]
   [potatoclient.specs.state.compass-calibration]
   [potatoclient.specs.state.day-cam-glass-heater]
   [potatoclient.specs.state.gps]
   [potatoclient.specs.state.lrf]
   [potatoclient.specs.state.meteo-internal]
   [potatoclient.specs.state.rec-osd]
   [potatoclient.specs.state.rotary]
   [potatoclient.specs.state.system]
   [potatoclient.specs.state.time]))

;; JonGUIState root message spec based on proto and EDN output
;; All 13 sub-messages are required per buf.validate
;; protocol_version must be > 0

(def jon-gui-state-spec
  [:map {:closed true}
   ;; protocol_version > 0 (required)
   [:protocol_version :proto/protocol-version]
   
   ;; All 13 required sub-messages (snake_case matching Pronto's output)
   [:actual_space_time :state/actual-space-time]
   [:camera_day :state/camera-day]
   [:camera_heat :state/camera-heat]
   [:compass :state/compass]
   [:compass_calibration :state/compass-calibration]
   [:day_cam_glass_heater :state/day-cam-glass-heater]
   [:gps :state/gps]
   [:lrf :state/lrf]
   [:meteo_internal :state/meteo-internal]
   [:rec_osd :state/rec-osd]
   [:rotary :state/rotary]
   [:system :state/system]
   [:time :state/time]])

(registry/register! :state/root jon-gui-state-spec)
(registry/register! :jon_gui_state jon-gui-state-spec) ; Alternative name