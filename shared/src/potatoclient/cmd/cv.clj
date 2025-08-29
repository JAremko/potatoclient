(ns potatoclient.cmd.cv
  "Computer Vision (CV) command functions.
   Based on the CV message structure in jon_shared_cmd_cv.proto."
  (:require
   [potatoclient.cmd.core :as core]))

;; ============================================================================
;; Tracking Commands
;; ============================================================================

(defn start-track-ndc
  "Start tracking at normalized device coordinates.
   NDC coordinates range from -1.0 to 1.0.
   Frame time should be the timestamp of the frame being tracked.
   Returns a fully formed cmd root ready to send." {:malli/schema [:=> [:cat :enum/video-channel :screen/ndc-x :screen/ndc-y :time/frame-time] :cmd/root]}
  [channel x y frame-time]
  (core/create-command 
    {:cv {:start_track_ndc {:channel channel
                            :x x
                            :y y
                            :frame_time frame-time}}}))

(defn stop-track
  "Stop current tracking.
   Returns a fully formed cmd root ready to send." {:malli/schema [:=> [:cat] :cmd/root]}
  []
  (core/create-command {:cv {:stop_track {}}}))

;; ============================================================================
;; Focus Control
;; ============================================================================

(defn set-auto-focus
  "Set auto focus mode for specified channel.
   Channel must be either :JON_GUI_DATA_VIDEO_CHANNEL_DAY or :JON_GUI_DATA_VIDEO_CHANNEL_HEAT.
   Returns a fully formed cmd root ready to send." {:malli/schema [:=> [:cat :enum/video-channel :boolean] :cmd/root]}
  [channel enabled?]
  (core/create-command 
    {:cv {:set_auto_focus {:channel channel
                          :value enabled?}}}))

;; ============================================================================
;; Vampire Mode (Enhanced Night Vision)
;; ============================================================================

(defn enable-vampire-mode
  "Enable vampire mode (enhanced night vision).
   Returns a fully formed cmd root ready to send." {:malli/schema [:=> [:cat] :cmd/root]}
  []
  (core/create-command {:cv {:vampire_mode_enable {}}}))

(defn disable-vampire-mode
  "Disable vampire mode.
   Returns a fully formed cmd root ready to send." {:malli/schema [:=> [:cat] :cmd/root]}
  []
  (core/create-command {:cv {:vampire_mode_disable {}}}))

;; ============================================================================
;; Stabilization Mode
;; ============================================================================

(defn enable-stabilization-mode
  "Enable image stabilization mode.
   Returns a fully formed cmd root ready to send." {:malli/schema [:=> [:cat] :cmd/root]}
  []
  (core/create-command {:cv {:stabilization_mode_enable {}}}))

(defn disable-stabilization-mode
  "Disable image stabilization mode.
   Returns a fully formed cmd root ready to send." {:malli/schema [:=> [:cat] :cmd/root]}
  []
  (core/create-command {:cv {:stabilization_mode_disable {}}}))

;; ============================================================================
;; Data Dump Commands
;; ============================================================================

(defn start-dump
  "Start data dump/recording.
   Returns a fully formed cmd root ready to send." {:malli/schema [:=> [:cat] :cmd/root]}
  []
  (core/create-command {:cv {:dump_start {}}}))

(defn stop-dump
  "Stop data dump/recording.
   Returns a fully formed cmd root ready to send." {:malli/schema [:=> [:cat] :cmd/root]}
  []
  (core/create-command {:cv {:dump_stop {}}}))

;; ============================================================================
;; Recognition Mode
;; ============================================================================

(defn enable-recognition-mode
  "Enable object recognition mode.
   Returns a fully formed cmd root ready to send." {:malli/schema [:=> [:cat] :cmd/root]}
  []
  (core/create-command {:cv {:recognition_mode_enable {}}}))

(defn disable-recognition-mode
  "Disable object recognition mode.
   Returns a fully formed cmd root ready to send." {:malli/schema [:=> [:cat] :cmd/root]}
  []
  (core/create-command {:cv {:recognition_mode_disable {}}}))