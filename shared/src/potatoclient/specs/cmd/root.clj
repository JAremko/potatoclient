(ns potatoclient.specs.cmd.root
  "Root Command message spec (JonSharedCmd.Root) using oneof structure.
   Based on jon_shared_cmd.proto.
   All maps use {:closed true} to catch typos and invalid keys."
  (:require
   [malli.core :as m]
   [malli.generator :as mg]
   [clojure.test.check.generators :as gen]
   [potatoclient.malli.registry :as registry]
   [potatoclient.malli.oneof :as oneof]
   ;; Import all command specs
   [potatoclient.specs.common]
   [potatoclient.specs.cmd.compass]
   [potatoclient.specs.cmd.cv]
   [potatoclient.specs.cmd.day-cam-glass-heater]
   [potatoclient.specs.cmd.day-camera]
   [potatoclient.specs.cmd.gps]
   [potatoclient.specs.cmd.heat-camera]
   [potatoclient.specs.cmd.lira]
   [potatoclient.specs.cmd.lrf]
   [potatoclient.specs.cmd.lrf-align]
   [potatoclient.specs.cmd.osd]
   [potatoclient.specs.cmd.rotary]
   [potatoclient.specs.cmd.system]))

;; The flat spec that matches protobuf structure
(def jon-shared-cmd-root-spec
  [:map {:closed true}
   [:protocol_version [:int {:min 1}]]
   [:session_id [:int {:min 1}]]
   [:important :boolean]
   [:from_cv_subsystem :boolean]
   [:client_type :enum/client-type]
   ;; Payload field using oneof schema type
   [:payload
    [:oneof
     [:day_camera :cmd/day-camera]
     [:heat_camera :cmd/heat-camera]
     [:gps :cmd/gps]
     [:compass :cmd/compass]
     [:lrf :cmd/lrf]
     [:lrf_calib :cmd/lrf-align]
     [:rotary :cmd/rotary]
     [:osd :cmd/osd]
     [:ping :cmd/empty]
     [:noop :cmd/empty]
     [:frozen :cmd/empty]
     [:system :cmd/system]
     [:cv :cmd/cv]
     [:day_cam_glass_heater :cmd/day-cam-glass-heater]
     [:lira :cmd/lira]]]])

(registry/register! :cmd/root jon-shared-cmd-root-spec)

;(registry/setup-global-registry!)
;
;(mg/generate :cmd/root)
