(ns potatoclient.cmd.cv
  "Computer Vision (CV) command functions.
   Based on the CV message structure in jon_shared_cmd_cv.proto."
  (:require
            [malli.core :as m]
    [potatoclient.cmd.core :as core]))

;; ============================================================================
;; Tracking Commands
;; ============================================================================

(defn start-track-ndc
  "Start tracking at normalized device coordinates.
   NDC coordinates range from -1.0 to 1.0.
   Frame time should be the timestamp of the frame being tracked.
   Returns a fully formed cmd root ready to send."
  [channel x y frame-time]
  (core/create-command
    {:cv {:start_track_ndc {:channel channel
                            :x x
                            :y y
                            :frame_time frame-time}}})) 
 (m/=> start-track-ndc [:=> [:cat :enum/video-channel :screen/ndc-x :screen/ndc-y :time/frame-time] :cmd/root])

(defn stop-track
  "Stop current tracking.
   Returns a fully formed cmd root ready to send."
  []
  (core/create-command {:cv {:stop_track {}}})) 
 (m/=> stop-track [:=> [:cat] :cmd/root])

;; ============================================================================
;; Focus Control
;; ============================================================================

(defn set-auto-focus
  "Set auto focus mode for specified channel.
   Channel must be either :JON_GUI_DATA_VIDEO_CHANNEL_DAY or :JON_GUI_DATA_VIDEO_CHANNEL_HEAT.
   Returns a fully formed cmd root ready to send."
  [channel enabled?]
  (core/create-command
    {:cv {:set_auto_focus {:channel channel
                           :value enabled?}}})) 
 (m/=> set-auto-focus [:=> [:cat :enum/video-channel :boolean] :cmd/root])

;; ============================================================================
;; Vampire Mode (Enhanced Night Vision)
;; ============================================================================

(defn enable-vampire-mode
  "Enable vampire mode (enhanced night vision).
   Returns a fully formed cmd root ready to send."
  []
  (core/create-command {:cv {:vampire_mode_enable {}}})) 
 (m/=> enable-vampire-mode [:=> [:cat] :cmd/root])

(defn disable-vampire-mode
  "Disable vampire mode.
   Returns a fully formed cmd root ready to send."
  []
  (core/create-command {:cv {:vampire_mode_disable {}}})) 
 (m/=> disable-vampire-mode [:=> [:cat] :cmd/root])

;; ============================================================================
;; Stabilization Mode
;; ============================================================================

(defn enable-stabilization-mode
  "Enable image stabilization mode.
   Returns a fully formed cmd root ready to send."
  []
  (core/create-command {:cv {:stabilization_mode_enable {}}})) 
 (m/=> enable-stabilization-mode [:=> [:cat] :cmd/root])

(defn disable-stabilization-mode
  "Disable image stabilization mode.
   Returns a fully formed cmd root ready to send."
  []
  (core/create-command {:cv {:stabilization_mode_disable {}}})) 
 (m/=> disable-stabilization-mode [:=> [:cat] :cmd/root])

;; ============================================================================
;; Data Dump Commands
;; ============================================================================

(defn start-dump
  "Start data dump/recording.
   Returns a fully formed cmd root ready to send."
  []
  (core/create-command {:cv {:dump_start {}}})) 
 (m/=> start-dump [:=> [:cat] :cmd/root])

(defn stop-dump
  "Stop data dump/recording.
   Returns a fully formed cmd root ready to send."
  []
  (core/create-command {:cv {:dump_stop {}}})) 
 (m/=> stop-dump [:=> [:cat] :cmd/root])

;; ============================================================================
;; Recognition Mode
;; ============================================================================

(defn enable-recognition-mode
  "Enable object recognition mode.
   Returns a fully formed cmd root ready to send."
  []
  (core/create-command {:cv {:recognition_mode_enable {}}})) 
 (m/=> enable-recognition-mode [:=> [:cat] :cmd/root])

(defn disable-recognition-mode
  "Disable object recognition mode.
   Returns a fully formed cmd root ready to send."
  []
  (core/create-command {:cv {:recognition_mode_disable {}}})) 
 (m/=> disable-recognition-mode [:=> [:cat] :cmd/root])