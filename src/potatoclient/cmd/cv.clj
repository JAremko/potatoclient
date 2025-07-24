(ns potatoclient.cmd.cv
  "Computer Vision command functions for PotatoClient"
  (:require [com.fulcrologic.guardrails.malli.core :as gr :refer [>defn ? =>]]
            [potatoclient.cmd.core :as cmd-core]
            [potatoclient.specs :as specs])
  (:import [cmd.CV
            JonSharedCmdCv$Root
            JonSharedCmdCv$StartTracking JonSharedCmdCv$StopTracking
            JonSharedCmdCv$SetAutoFocus JonSharedCmdCv$SetVampireMode
            JonSharedCmdCv$SetStabilizationMode JonSharedCmdCv$StartVideoDump
            JonSharedCmdCv$StopVideoDump]
           [data JonSharedDataTypes$JonGuiDataChannels]))

;; ============================================================================
;; Tracking Commands
;; ============================================================================

(>defn start-tracking
  "Start tracking at normalized device coordinates"
  [x y]
  [::specs/normalized-value ::specs/normalized-value => nil?]
  (let [root-msg (cmd-core/create-root-message)
        tracking-msg (-> (JonSharedCmdCv$StartTracking/newBuilder)
                         (.setX x)
                         (.setY y))
        cv-root (-> (JonSharedCmdCv$Root/newBuilder)
                    (.setStartTracking tracking-msg))]
    (.setCv root-msg cv-root)
    (cmd-core/send-cmd-message root-msg))
  nil)

(>defn stop-tracking
  "Stop current tracking"
  []
  [=> nil?]
  (let [root-msg (cmd-core/create-root-message)
        cv-root (-> (JonSharedCmdCv$Root/newBuilder)
                    (.setStopTracking (JonSharedCmdCv$StopTracking/newBuilder)))]
    (.setCv root-msg cv-root)
    (cmd-core/send-cmd-message root-msg))
  nil)

;; ============================================================================
;; Auto Focus Commands
;; ============================================================================

(>defn set-auto-focus
  "Enable or disable auto focus for specified channel"
  [channel enabled]
  [[:fn {:error/message "must be a JonGuiDataChannels enum"}
    #(instance? JonSharedDataTypes$JonGuiDataChannels %)] boolean? => nil?]
  (let [root-msg (cmd-core/create-root-message)
        af-msg (-> (JonSharedCmdCv$SetAutoFocus/newBuilder)
                   (.setChannel channel)
                   (.setAutoFocus enabled))
        cv-root (-> (JonSharedCmdCv$Root/newBuilder)
                    (.setAutoFocus af-msg))]
    (.setCv root-msg cv-root)
    (cmd-core/send-cmd-message root-msg))
  nil)

;; ============================================================================
;; Mode Commands
;; ============================================================================

(>defn set-vampire-mode
  "Enable or disable vampire mode"
  [enabled]
  [boolean? => nil?]
  (let [root-msg (cmd-core/create-root-message)
        vampire-msg (-> (JonSharedCmdCv$SetVampireMode/newBuilder)
                        (.setVampireMode enabled))
        cv-root (-> (JonSharedCmdCv$Root/newBuilder)
                    (.setVampireMode vampire-msg))]
    (.setCv root-msg cv-root)
    (cmd-core/send-cmd-message root-msg))
  nil)

(>defn set-stabilization-mode
  "Enable or disable stabilization mode"
  [enabled]
  [boolean? => nil?]
  (let [root-msg (cmd-core/create-root-message)
        stab-msg (-> (JonSharedCmdCv$SetStabilizationMode/newBuilder)
                     (.setStabiliztionMode enabled))
        cv-root (-> (JonSharedCmdCv$Root/newBuilder)
                    (.setStabilizationMode stab-msg))]
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
                    (.setStartVideoDump (JonSharedCmdCv$StartVideoDump/newBuilder)))]
    (.setCv root-msg cv-root)
    (cmd-core/send-cmd-message root-msg))
  nil)

(>defn stop-video-dump
  "Stop video recording/dump"
  []
  [=> nil?]
  (let [root-msg (cmd-core/create-root-message)
        cv-root (-> (JonSharedCmdCv$Root/newBuilder)
                    (.setStopVideoDump (JonSharedCmdCv$StopVideoDump/newBuilder)))]
    (.setCv root-msg cv-root)
    (cmd-core/send-cmd-message root-msg))
  nil)

;; ============================================================================
;; Utility Functions
;; ============================================================================

(>defn string->channel
  "Convert string to channel enum"
  [value]
  [string? => (? [:fn {:error/message "must be a JonGuiDataChannels enum"}
                  #(instance? JonSharedDataTypes$JonGuiDataChannels %)])]
  (case value
    "day" JonSharedDataTypes$JonGuiDataChannels/JON_GUI_DATA_CHANNELS_DAY_CAMERA
    "heat" JonSharedDataTypes$JonGuiDataChannels/JON_GUI_DATA_CHANNELS_HEAT_CAMERA
    nil))