(ns potatoclient.specs.cmd.lrf-align
  "LRF Alignment/Calibration command specs matching buf.validate constraints.
   Based on jon_shared_cmd_lrf_align.proto.
   All maps use {:closed true} to catch typos and invalid keys."
  (:require
   [potatoclient.malli.registry :as registry]))

;; LRF alignment command specs - placeholder for now
;; Will need to check proto file for exact structure

(def lrf-align-command-spec
  [:map {:closed true}
   ;; TODO: Add fields based on proto definition
   ])

(registry/register! :cmd/lrf-align lrf-align-command-spec)