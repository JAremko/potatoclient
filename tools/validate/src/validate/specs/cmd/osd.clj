(ns validate.specs.cmd.osd
  "OSD (On-Screen Display) command specs matching buf.validate constraints.
   Based on jon_shared_cmd_osd.proto.
   All maps use {:closed true} to catch typos and invalid keys."
  (:require
   [potatoclient.malli.registry :as registry]))

;; OSD command specs - placeholder for now
;; Will need to check proto file for exact structure
;; Likely includes: enable/disable OSD, set display options

(def osd-command-spec
  [:map {:closed true}
   ;; TODO: Add fields based on proto definition
   ])

(registry/register! :cmd/osd osd-command-spec)