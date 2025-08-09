(ns potatoclient.specs.cmd.heat-camera
  "Heat Camera command specs matching buf.validate constraints.
   Based on jon_shared_cmd_heat_camera.proto.
   All maps use {:closed true} to catch typos and invalid keys."
  (:require
   [potatoclient.malli.registry :as registry]))

;; Heat camera command specs - placeholder for now
;; Will need to check proto file for exact structure
;; Likely includes: AGC mode, filter, zoom controls

(def heat-camera-command-spec
  [:map {:closed true}
   ;; TODO: Add fields based on proto definition
   ])

(registry/register! :cmd/heat-camera heat-camera-command-spec)