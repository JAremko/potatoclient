(ns potatoclient.specs.cmd.lrf-align
  "LRF Alignment/Calibration command specs matching buf.validate constraints.
   Based on jon_shared_cmd_lrf_align.proto.
   All maps use {:closed true} to catch typos and invalid keys."
  (:require
    [potatoclient.malli.registry :as registry]))

;; LRF alignment command specs
;; This has a nested oneof structure: channel -> cmd

;; Offset commands
(def set-offsets-spec
  "Specification for setting LRF alignment offsets with x,y pixel coordinates.
   Used to align laser rangefinder crosshairs with camera view."
  [:map {:closed true}
   [:x :screen/pixel-offset-x]
   [:y :screen/pixel-offset-y]])

(def shift-offsets-by-spec
  "Specification for incrementally shifting LRF alignment by delta x,y pixels.
   Adjusts current alignment position relative to existing offset."
  [:map {:closed true}
   [:x :screen/pixel-offset-x]
   [:y :screen/pixel-offset-y]])

;; Offsets message with cmd oneof
(def offsets-spec
  "Specification for LRF offset commands using protobuf oneof pattern.
   Supports set, save, reset, and shift operations for alignment calibration."
  [:oneof
   [:set set-offsets-spec]
   [:save :cmd/empty]
   [:reset :cmd/empty]
   [:shift shift-offsets-by-spec]])

;; Main LRF Align command spec using channel oneof
(def lrf-align-command-spec
  "Root specification for LRF alignment commands per camera channel.
   Allows independent alignment calibration for day and heat camera channels."
  [:oneof
   [:day offsets-spec]
   [:heat offsets-spec]])

(registry/register-spec! :cmd/lrf-align lrf-align-command-spec)
(registry/register-spec! :cmd/lrf_calib lrf-align-command-spec) ; Alternative name