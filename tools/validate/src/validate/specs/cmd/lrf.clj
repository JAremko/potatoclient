(ns validate.specs.cmd.lrf
  "LRF (Laser Range Finder) command specs matching buf.validate constraints.
   Based on jon_shared_cmd_lrf.proto.
   All maps use {:closed true} to catch typos and invalid keys."
  (:require
   [potatoclient.malli.registry :as registry]))

;; LRF command specs - placeholder for now
;; Will need to check proto file for exact structure
;; Likely includes: fire laser, set mode, pointer control

(def lrf-command-spec
  [:map {:closed true}
   ;; TODO: Add fields based on proto definition
   ])

(registry/register! :cmd/lrf lrf-command-spec)