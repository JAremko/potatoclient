(ns potatoclient.specs.cmd.root
  "Root Command message spec (JonSharedCmd.Root) using oneof structure.
   Based on jon_shared_cmd.proto.
   All maps use {:closed true} to catch typos and invalid keys."
  (:require
   [malli.core :as m]
   [malli.generator :as mg]
   [clojure.test.check.generators :as gen]
   [potatoclient.malli.registry :as registry]
   [potatoclient.specs.oneof-edn :as oneof-edn]
   ;; Import all command specs
   [potatoclient.specs.cmd.common]
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

;; The oneof spec for commands
(def command-oneof-spec
  [:oneof_edn
   [:cv :cmd/cv]
   [:day_camera :cmd/day-camera]
   [:heat_camera :cmd/heat-camera]
   [:gps :cmd/gps]
   [:compass :cmd/compass]
   [:lrf :cmd/lrf]
   [:lrf_calib :cmd/lrf-align]
   [:rotary :cmd/rotary]
   [:osd :cmd/osd]
   [:ping :cmd/ping]
   [:noop :cmd/noop]
   [:frozen :cmd/frozen]
   [:system :cmd/system]
   [:day_cam_glass_heater :cmd/day-cam-glass-heater]
   [:lira :cmd/lira]])

;; Main spec with required fields and oneof command
(def jon-shared-cmd-root-spec
  [:merge
   ;; Required and optional metadata fields
   [:map {:closed true}
    ;; Required fields
    [:protocol_version :proto/protocol-version]
    [:client_type :proto/client-type]
    ;; Optional metadata fields  
    [:session_id {:optional true} :int]
    [:important {:optional true} :boolean]
    [:from_cv_subsystem {:optional true} :boolean]]
   ;; Oneof command fields
   command-oneof-spec])

(registry/register! :cmd/root jon-shared-cmd-root-spec)
(registry/register! :jon_shared_cmd_root jon-shared-cmd-root-spec)