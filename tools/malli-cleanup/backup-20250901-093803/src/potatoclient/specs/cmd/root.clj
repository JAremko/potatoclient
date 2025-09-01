(ns potatoclient.specs.cmd.root
  "Root Command message spec (JonSharedCmd.Root) with flattened oneof structure.
   Based on jon_shared_cmd.proto.
   
   Pronto expects oneof fields to be flattened at the root level, not wrapped in :payload.
   The spec ensures exactly one oneof field is present."
  (:require
   [malli.core :as m]
   [malli.generator :as mg]
   [clojure.test.check.generators :as gen]
   [potatoclient.malli.registry :as registry]
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
;; Uses oneof with base fields for the non-oneof fields
(def jon-shared-cmd-root-spec
  [:oneof
   ;; Base fields (always present, not part of oneof constraint)
   [:protocol_version {:base true} :proto/protocol-version]
   [:session_id {:base true} :proto/uint32]  ; Just uint32, no additional constraints in proto
   [:important {:base true} :boolean]
   [:from_cv_subsystem {:base true} :boolean]
   [:client_type {:base true} :enum/client-type]
   ;; Oneof fields (exactly one must be present)
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
   [:lira :cmd/lira]])

;; Register the spec
(registry/register! :cmd/root jon-shared-cmd-root-spec)

;; Spec for just the payload (one of the oneof fields, without protocol fields)
;; This is what command functions accept as input
(def cmd-payload-spec
  [:or
   [:map {:closed true} [:day_camera :cmd/day-camera]]
   [:map {:closed true} [:heat_camera :cmd/heat-camera]]
   [:map {:closed true} [:gps :cmd/gps]]
   [:map {:closed true} [:compass :cmd/compass]]
   [:map {:closed true} [:lrf :cmd/lrf]]
   [:map {:closed true} [:lrf_calib :cmd/lrf-align]]
   [:map {:closed true} [:rotary :cmd/rotary]]
   [:map {:closed true} [:osd :cmd/osd]]
   [:map {:closed true} [:ping :cmd/empty]]
   [:map {:closed true} [:noop :cmd/empty]]
   [:map {:closed true} [:frozen :cmd/empty]]
   [:map {:closed true} [:system :cmd/system]]
   [:map {:closed true} [:cv :cmd/cv]]
   [:map {:closed true} [:day_cam_glass_heater :cmd/day-cam-glass-heater]]
   [:map {:closed true} [:lira :cmd/lira]]])

;; Register the payload spec
(registry/register! :cmd/payload cmd-payload-spec)
