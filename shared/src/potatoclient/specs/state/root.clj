(ns potatoclient.specs.state.root
  "Root State message spec (JonGUIState) with all subsystem states.
   Based on jon_shared_data.proto.
   All maps use {:closed true} to catch typos and invalid keys."
  (:require
   [malli.core :as m]
   [malli.generator :as mg]
   [clojure.test.check.generators :as gen]
   [potatoclient.malli.registry :as registry]
   ;; Import all state specs
   [potatoclient.specs.state.system]
   [potatoclient.specs.state.meteo-internal]
   [potatoclient.specs.state.lrf]
   [potatoclient.specs.state.time]
   [potatoclient.specs.state.gps]
   [potatoclient.specs.state.compass]
   [potatoclient.specs.state.rotary]
   [potatoclient.specs.state.camera-day]
   [potatoclient.specs.state.camera-heat]
   [potatoclient.specs.state.compass-calibration]
   [potatoclient.specs.state.rec-osd]
   [potatoclient.specs.state.day-cam-glass-heater]
   [potatoclient.specs.state.actual-space-time]))

;; Root state spec that matches JonGUIState protobuf structure
;; All 14 fields with protocol_version being uint32 > 0 and all message fields required
(def jon-gui-state-spec
  [:map {:closed true}
   ;; protocol_version: uint32 with constraint gt: 0
   [:protocol_version [:and :int [:> 0]]]
   ;; All subsystem state messages - all are required per buf.validate
   [:system :state/system]
   [:meteo_internal :state/meteo-internal]
   [:lrf :state/lrf]
   [:time :state/time]
   [:gps :state/gps]
   [:compass :state/compass]
   [:rotary :state/rotary]
   [:camera_day :state/camera-day]
   [:camera_heat :state/camera-heat]
   [:compass_calibration :state/compass-calibration]
   [:rec_osd :state/rec-osd]
   [:day_cam_glass_heater :state/day-cam-glass-heater]
   [:actual_space_time :state/actual-space-time]])

;; Register the spec for global use
(registry/register! :state/root jon-gui-state-spec)

;; Helper function to validate state messages
(defn validate-state
  "Validate a state message against the JonGUIState spec.
   Returns validation result with errors if any."
  [state-data]
  (m/explain :state/root state-data))

;; Helper function to generate sample state for testing
(defn generate-sample-state
  "Generate a sample JonGUIState message for testing.
   Uses the spec's generator."
  []
  (mg/generate :state/root))
