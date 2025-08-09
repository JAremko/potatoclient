(ns potatoclient.specs.cmd.cv
  "CV (Computer Vision) command specs matching buf.validate constraints.
   Based on jon_shared_cmd_cv.proto.
   All maps use {:closed true} to catch typos and invalid keys."
  (:require
   [potatoclient.malli.registry :as registry]))

;; CV command specs - placeholder for now
;; Will need to check proto file for exact structure

(def cv-command-spec
  [:map {:closed true}
   ;; TODO: Add fields based on proto definition
   ])

(registry/register! :cmd/cv cv-command-spec)