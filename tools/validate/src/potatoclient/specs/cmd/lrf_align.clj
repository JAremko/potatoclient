(ns potatoclient.specs.cmd.lrf-align
  "LRF Alignment/Calibration command specs matching buf.validate constraints.
   Based on jon_shared_cmd_lrf_align.proto.
   All maps use {:closed true} to catch typos and invalid keys."
  (:require
   [potatoclient.malli.registry :as registry]
   [potatoclient.specs.oneof-edn :as oneof-edn]))

;; LRF alignment command specs - based on proto-explorer findings
;; This has a nested oneof structure: channel -> cmd

;; Offset commands
(def set-offsets-spec
  [:map {:closed true}
   [:x [:double]]
   [:y [:double]]])

(def save-offsets-spec [:map {:closed true}])
(def reset-offsets-spec [:map {:closed true}])

(def shift-offsets-by-spec
  [:map {:closed true}
   [:x [:double]]
   [:y [:double]]])

;; Offsets message with cmd oneof
(def offsets-spec
  [:oneof_edn
   [:set set-offsets-spec]
   [:save save-offsets-spec]
   [:reset reset-offsets-spec]
   [:shift shift-offsets-by-spec]])

;; Main LRF Align command spec using channel oneof
(def lrf-align-command-spec
  [:oneof_edn
   [:day offsets-spec]
   [:heat offsets-spec]])

(registry/register! :cmd/lrf-align lrf-align-command-spec)
(registry/register! :cmd/lrf_calib lrf-align-command-spec) ; Alternative name