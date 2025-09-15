(ns potatoclient.specs.cmd.cv
  "CV (Computer Vision) command specs matching buf.validate constraints.
   Based on jon_shared_cmd_cv.proto.
   All maps use {:closed true} to catch typos and invalid keys."
  (:require
    [potatoclient.malli.registry :as registry]
    [potatoclient.specs.common]))

;; CV command specs
;; This is a oneof structure with 11 command types

;; Auto focus
(def set-auto-focus-spec
  "Spec for enabling/disabling auto-focus on a video channel.
   Channel must be valid video channel enum, value is boolean toggle."
  [:map {:closed true}
   [:channel :enum/video-channel]
   [:value [:boolean]]])

;; Tracking
(def start-track-ndc-spec
  "Spec for starting tracking at normalized device coordinates.
   Includes channel, NDC position, and timing information for synchronization."
  [:map {:closed true}
   [:channel :enum/video-channel]
   [:x :screen/ndc-x]
   [:y :screen/ndc-y]
   [:frame_time :time/frame-time]
   [:state_time [:int {:min 0 :max potatoclient.specs.common/long-max-value}]]])

;; Main CV command spec using oneof - all 11 commands
(def cv-command-spec
  "Main computer vision command spec with all 11 command types.
   Includes tracking, auto-focus, vampire mode, stabilization, dump, and recognition.
   Uses oneof to ensure exactly one command is specified."
  [:oneof
   [:set_auto_focus set-auto-focus-spec]
   [:start_track_ndc start-track-ndc-spec]
   [:stop_track :cmd/empty]
   [:vampire_mode_enable :cmd/empty]
   [:vampire_mode_disable :cmd/empty]
   [:stabilization_mode_enable :cmd/empty]
   [:stabilization_mode_disable :cmd/empty]
   [:dump_start :cmd/empty]
   [:dump_stop :cmd/empty]
   [:recognition_mode_enable :cmd/empty]
   [:recognition_mode_disable :cmd/empty]])

(registry/register-spec! :cmd/cv cv-command-spec)