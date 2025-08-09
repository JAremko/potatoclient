(ns potatoclient.specs.state.root
  "Root State message spec (JonGUIState) assembling all sub-messages.
   Based on jon_shared_data.proto.
   All maps use {:closed true} to catch typos and invalid keys."
  (:require
   [potatoclient.malli.registry :as registry]
   ;; Import all sub-message specs
   [validate.specs.state.actual-space-time]
   [validate.specs.state.camera-day]
   [validate.specs.state.camera-heat]
   [validate.specs.state.compass]
   [validate.specs.state.day-cam-glass-heater]
   [validate.specs.state.gps]
   [validate.specs.state.lrf]
   [validate.specs.state.meteo-internal]
   [validate.specs.state.rec-osd]
   [validate.specs.state.rotary]
   [validate.specs.state.system]
   [validate.specs.state.time]))

;; JonGUIState root message spec based on proto and EDN output
;; All 14 sub-messages are required per buf.validate
;; protocol_version must be > 0

(def jon-gui-state-spec
  [:map {:closed true}
   ;; protocol_version > 0 (required)
   [:protocol-version :proto/protocol-version]
   
   ;; All 14 required sub-messages (in EDN format with kebab-case)
   [:actual-space-time :state/actual-space-time]
   [:camera-day :state/camera-day]
   [:camera-heat :state/camera-heat]
   [:compass :state/compass]
   [:compass-calibration :state/compass-calibration]
   [:day-cam-glass-heater :state/day-cam-glass-heater]
   [:gps :state/gps]
   [:lrf :state/lrf]
   [:meteo-internal :state/meteo-internal]
   [:rec-osd :state/rec-osd]
   [:rotary :state/rotary]
   [:system :state/system]
   [:time :state/time]])

(registry/register! :state/root jon-gui-state-spec)
(registry/register! :jon-gui-state jon-gui-state-spec) ; Alternative name