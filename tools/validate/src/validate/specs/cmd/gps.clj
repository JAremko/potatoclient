(ns validate.specs.cmd.gps
  "GPS command specs matching buf.validate constraints.
   Based on jon_shared_cmd_gps.proto.
   All maps use {:closed true} to catch typos and invalid keys."
  (:require
   [potatoclient.malli.registry :as registry]))

;; GPS command specs - placeholder for now
;; Will need to check proto file for exact structure
;; Likely includes: set manual position, enable/disable

(def gps-command-spec
  [:map {:closed true}
   ;; TODO: Add fields based on proto definition
   ])

(registry/register! :cmd/gps gps-command-spec)