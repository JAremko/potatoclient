(ns validate.specs.cmd.day-camera
  "Day Camera command specs matching buf.validate constraints.
   Based on jon_shared_cmd_day_camera.proto.
   All maps use {:closed true} to catch typos and invalid keys."
  (:require
   [potatoclient.malli.registry :as registry]))

;; Day camera command specs - placeholder for now
;; Will need to check proto file for exact structure
;; Likely includes: zoom, focus, iris, infrared filter controls

(def day-camera-command-spec
  [:map {:closed true}
   ;; TODO: Add fields based on proto definition
   ])

(registry/register! :cmd/day-camera day-camera-command-spec)