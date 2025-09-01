(ns potatoclient.specs.cmd.lrf-align
  "LRF Alignment/Calibration command specs matching buf.validate constraints.
   Based on jon_shared_cmd_lrf_align.proto.
   All maps use {:closed true} to catch typos and invalid keys."
  (:require
   [potatoclient.malli.registry :as registry]))

;; LRF alignment command specs - based on proto-explorer findings
;; This has a nested oneof structure: channel -> cmd

;; Offset commands
(def set-offsets-spec
  [:map {:closed true}
   [:x :screen/pixel-offset-x]
   [:y :screen/pixel-offset-y]])

(def shift-offsets-by-spec
  [:map {:closed true}
   [:x :screen/pixel-offset-x]
   [:y :screen/pixel-offset-y]])

;; Offsets message with cmd oneof
(def offsets-spec
  [:oneof
   [:set set-offsets-spec]
   [:save :cmd/empty]
   [:reset :cmd/empty]
   [:shift shift-offsets-by-spec]])

;; Main LRF Align command spec using channel oneof
(def lrf-align-command-spec
  [:oneof
   [:day offsets-spec]
   [:heat offsets-spec]])

(registry/register! :cmd/lrf-align lrf-align-command-spec)
(registry/register! :cmd/lrf_calib lrf-align-command-spec) ; Alternative name