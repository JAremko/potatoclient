(ns potatoclient.cmd.cv
  "Computer Vision command functions for PotatoClient"
  (:require [com.fulcrologic.guardrails.malli.core :as gr :refer [>defn ? =>]]
            [potatoclient.cmd.core :as cmd-core]
            [potatoclient.specs :as specs])
  (:import [cmd.CV
            JonSharedCmdCv$Root
            JonSharedCmdCv$StartTrackNDC JonSharedCmdCv$StopTrack
            JonSharedCmdCv$SetAutoFocus
            JonSharedCmdCv$VampireModeEnable JonSharedCmdCv$VampireModeDisable
            JonSharedCmdCv$StabilizationModeEnable JonSharedCmdCv$StabilizationModeDisable
            JonSharedCmdCv$DumpStart JonSharedCmdCv$DumpStop]
           [ser JonSharedDataTypes$JonGuiDataVideoChannel]))

;; ============================================================================
;; Tracking Commands
;; ============================================================================

(>defn start-tracking
  "Start tracking at normalized device coordinates with frame time and channel"
  [channel x y frame-time]
  [::specs/video-channel ::specs/ndc-coordinate ::specs/ndc-coordinate pos-int? => nil?]
  (let [root-msg (cmd-core/create-root-message)
        tracking-msg (-> (JonSharedCmdCv$StartTrackNDC/newBuilder)
                         (.setChannel channel)
                         (.setX x)
                         (.setY y)
                         (.setFrameTime frame-time)
                         (.build))
        cv-root (-> (JonSharedCmdCv$Root/newBuilder)
                    (.setStartTrackNdc tracking-msg))]
    (.setCv root-msg cv-root)
    (cmd-core/send-cmd-message root-msg)
    nil))

(>defn stop-tracking
  "Stop current tracking"
  []
  [=> nil?]
  (let [root-msg (cmd-core/create-root-message)
        cv-root (-> (JonSharedCmdCv$Root/newBuilder)
                    (.setStopTrack (JonSharedCmdCv$StopTrack/newBuilder)))]
    (.setCv root-msg cv-root)
    (cmd-core/send-cmd-message root-msg))
  nil)

;; ============================================================================
;; Auto Focus Commands
;; ============================================================================

(>defn set-auto-focus
  "Enable or disable auto focus for specified channel"
  [channel enabled]
  [::specs/video-channel boolean? => nil?]
  (let [root-msg (cmd-core/create-root-message)
        af-msg (-> (JonSharedCmdCv$SetAutoFocus/newBuilder)
                   (.setChannel channel)
                   (.setValue enabled)
                   (.build))
        cv-root (-> (JonSharedCmdCv$Root/newBuilder)
                    (.setSetAutoFocus af-msg))]
    (.setCv root-msg cv-root)
    (cmd-core/send-cmd-message root-msg))
  nil)

;; ============================================================================
;; Mode Commands
;; ============================================================================

(>defn enable-vampire-mode
  "Enable vampire mode"
  []
  [=> nil?]
  (let [root-msg (cmd-core/create-root-message)
        cv-root (-> (JonSharedCmdCv$Root/newBuilder)
                    (.setVampireModeEnable (JonSharedCmdCv$VampireModeEnable/newBuilder)))]
    (.setCv root-msg cv-root)
    (cmd-core/send-cmd-message root-msg))
  nil)

(>defn disable-vampire-mode
  "Disable vampire mode"
  []
  [=> nil?]
  (let [root-msg (cmd-core/create-root-message)
        cv-root (-> (JonSharedCmdCv$Root/newBuilder)
                    (.setVampireModeDisable (JonSharedCmdCv$VampireModeDisable/newBuilder)))]
    (.setCv root-msg cv-root)
    (cmd-core/send-cmd-message root-msg))
  nil)

(>defn enable-stabilization-mode
  "Enable stabilization mode"
  []
  [=> nil?]
  (let [root-msg (cmd-core/create-root-message)
        cv-root (-> (JonSharedCmdCv$Root/newBuilder)
                    (.setStabilizationModeEnable (JonSharedCmdCv$StabilizationModeEnable/newBuilder)))]
    (.setCv root-msg cv-root)
    (cmd-core/send-cmd-message root-msg))
  nil)

(>defn disable-stabilization-mode
  "Disable stabilization mode"
  []
  [=> nil?]
  (let [root-msg (cmd-core/create-root-message)
        cv-root (-> (JonSharedCmdCv$Root/newBuilder)
                    (.setStabilizationModeDisable (JonSharedCmdCv$StabilizationModeDisable/newBuilder)))]
    (.setCv root-msg cv-root)
    (cmd-core/send-cmd-message root-msg))
  nil)

;; ============================================================================
;; Video Recording Commands
;; ============================================================================

(>defn start-video-dump
  "Start video recording/dump"
  []
  [=> nil?]
  (let [root-msg (cmd-core/create-root-message)
        cv-root (-> (JonSharedCmdCv$Root/newBuilder)
                    (.setDumpStart (JonSharedCmdCv$DumpStart/newBuilder)))]
    (.setCv root-msg cv-root)
    (cmd-core/send-cmd-message root-msg))
  nil)

(>defn stop-video-dump
  "Stop video recording/dump"
  []
  [=> nil?]
  (let [root-msg (cmd-core/create-root-message)
        cv-root (-> (JonSharedCmdCv$Root/newBuilder)
                    (.setDumpStop (JonSharedCmdCv$DumpStop/newBuilder)))]
    (.setCv root-msg cv-root)
    (cmd-core/send-cmd-message root-msg))
  nil)

;; ============================================================================
;; Utility Functions
;; ============================================================================

(>defn string->channel
  "Convert string to channel enum"
  [value]
  [string? => (? ::specs/video-channel)]
  (case value
    "day" JonSharedDataTypes$JonGuiDataVideoChannel/JON_GUI_DATA_VIDEO_CHANNEL_DAY
    "heat" JonSharedDataTypes$JonGuiDataVideoChannel/JON_GUI_DATA_VIDEO_CHANNEL_HEAT
    nil))