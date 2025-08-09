(ns validate.specs.cmd.system
  "System command specs matching buf.validate constraints.
   Based on jon_shared_cmd_system.proto.
   All maps use {:closed true} to catch typos and invalid keys."
  (:require
   [potatoclient.malli.registry :as registry]))

;; System command specs - placeholder for now
;; Will need to check proto file for exact structure
;; Likely includes: set localization, recording control

(def system-command-spec
  [:map {:closed true}
   ;; TODO: Add fields based on proto definition
   ])

(registry/register! :cmd/system system-command-spec)