(ns potatoclient.specs.cmd.cv
  "CV (Computer Vision) command specs matching buf.validate constraints.
   Based on jon_shared_cmd_cv.proto.
   All maps use {:closed true} to catch typos and invalid keys."
  (:require
   [potatoclient.malli.registry :as registry]
   [potatoclient.malli.oneof :as oneof]))

;; CV command specs - based on proto-explorer findings
;; This is a oneof structure with 11 command types

;; Auto focus
(def set-auto-focus-spec
  [:map {:closed true}
   [:value [:boolean]]])

;; Tracking
(def start-track-ndc-spec
  [:map {:closed true}
   [:x [:double {:min -1.0 :max 1.0}]]
   [:y [:double {:min -1.0 :max 1.0}]]])

(def stop-track-spec [:map {:closed true}])

;; Vampire mode
(def vampire-mode-enable-spec [:map {:closed true}])
(def vampire-mode-disable-spec [:map {:closed true}])

;; Stabilization mode  
(def stabilization-mode-enable-spec [:map {:closed true}])
(def stabilization-mode-disable-spec [:map {:closed true}])

;; Dump control
(def dump-start-spec [:map {:closed true}])
(def dump-stop-spec [:map {:closed true}])

;; Recognition mode
(def recognition-mode-enable-spec [:map {:closed true}])
(def recognition-mode-disable-spec [:map {:closed true}])

;; Main CV command spec using oneof - all 11 commands
(def cv-command-spec
  [:oneof
   [:set_auto_focus set-auto-focus-spec]
   [:start_track_ndc start-track-ndc-spec]
   [:stop_track stop-track-spec]
   [:vampire_mode_enable vampire-mode-enable-spec]
   [:vampire_mode_disable vampire-mode-disable-spec]
   [:stabilization_mode_enable stabilization-mode-enable-spec]
   [:stabilization_mode_disable stabilization-mode-disable-spec]
   [:dump_start dump-start-spec]
   [:dump_stop dump-stop-spec]
   [:recognition_mode_enable recognition-mode-enable-spec]
   [:recognition_mode_disable recognition-mode-disable-spec]])

(registry/register! :cmd/cv cv-command-spec)