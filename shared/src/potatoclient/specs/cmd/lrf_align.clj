(ns potatoclient.specs.cmd.lrf-align
  "LRF Alignment/Calibration command specs matching buf.validate constraints.
   Based on jon_shared_cmd_lrf_align.proto.
   All maps use {:closed true} to catch typos and invalid keys."
  (:require
   [potatoclient.malli.registry :as registry]
   [potatoclient.specs.oneof-edn :as oneof-edn]))

;; LRF alignment command specs - simplified placeholders
;; This is a oneof structure with multiple command types

(def start-calibration-spec [:map {:closed true}])
(def stop-calibration-spec [:map {:closed true}])
(def set-offset-spec
  [:map {:closed true}
   [:x [:float]]
   [:y [:float]]])

;; Main LRF Align command spec using oneof
(def lrf-align-command-spec
  [:oneof_edn
   [:start_calibration start-calibration-spec]
   [:stop_calibration stop-calibration-spec]
   [:set_offset set-offset-spec]
   ;; Add more commands as needed
   ])

(registry/register! :cmd/lrf-align lrf-align-command-spec)
(registry/register! :cmd/lrf_calib lrf-align-command-spec) ; Alternative name