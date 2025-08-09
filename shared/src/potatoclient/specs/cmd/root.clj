(ns potatoclient.specs.cmd.root
  "Root Command message spec (JonSharedCmd.Root) using oneof structure.
   Based on jon_shared_cmd.proto.
   All maps use {:closed true} to catch typos and invalid keys."
  (:require
   [potatoclient.malli.registry :as registry]
   [potatoclient.specs.oneof-edn :as oneof-edn]
   ;; Import all command specs
   [validate.specs.cmd.common]
   [validate.specs.cmd.compass]
   [validate.specs.cmd.cv]
   [validate.specs.cmd.day-cam-glass-heater]
   [validate.specs.cmd.day-camera]
   [validate.specs.cmd.gps]
   [validate.specs.cmd.heat-camera]
   [validate.specs.cmd.lira]
   [validate.specs.cmd.lrf]
   [validate.specs.cmd.lrf-align]
   [validate.specs.cmd.osd]
   [validate.specs.cmd.rotary]
   [validate.specs.cmd.system]))

;; JonSharedCmd.Root message spec
;; From proto discovery, has:
;; - protocol_version: uint32 > 0
;; - client_type: enum, cannot be UNSPECIFIED
;; - 15 command payloads in oneof structure

;; This requires the oneof-pronto schema to be registered
;; The root will validate that exactly one command field is present

(def jon-shared-cmd-root-spec
  [:map {:closed true}
   ;; Required fields
   [:protocol-version :proto/protocol-version]
   [:client-type :proto/client-type]
   
   ;; Oneof command payloads (exactly one must be present)
   ;; Using oneof-edn schema for EDN validation
   [:cmd [:oneof-edn
          [:cv :cmd/cv]
          [:day-camera :cmd/day-camera]
          [:heat-camera :cmd/heat-camera]
          [:gps :cmd/gps]
          [:compass :cmd/compass]
          [:lrf :cmd/lrf]
          [:lrf-calib :cmd/lrf-align]
          [:rotary :cmd/rotary]
          [:osd :cmd/osd]
          [:ping :cmd/ping]
          [:noop :cmd/noop]
          [:frozen :cmd/frozen]
          [:system :cmd/system]
          [:day-cam-glass-heater :cmd/day-cam-glass-heater]
          [:lira :cmd/lira]]]])

(registry/register! :cmd/root jon-shared-cmd-root-spec)
(registry/register! :jon-shared-cmd-root jon-shared-cmd-root-spec) ; Alternative name