(ns potatoclient.specs.cmd.rotary
  "Rotary Platform command specs matching buf.validate constraints.
   Based on jon_shared_cmd_rotary.proto.
   All maps use {:closed true} to catch typos and invalid keys."
  (:require
   [potatoclient.malli.registry :as registry]))

;; Rotary command specs - placeholder for now
;; From proto file we saw earlier, includes:
;; - Start/Stop/Halt commands
;; - Azimuth/Elevation control
;; - Platform positioning
;; - Scan operations
;; - GPS rotation commands

(def rotary-command-spec
  [:map {:closed true}
   ;; TODO: Add fields based on proto definition
   ;; This will use oneof structure for command type
   ])

(registry/register! :cmd/rotary rotary-command-spec)