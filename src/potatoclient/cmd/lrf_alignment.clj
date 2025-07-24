(ns potatoclient.cmd.lrf-alignment
  "LRF alignment command functions for PotatoClient"
  (:require [com.fulcrologic.guardrails.malli.core :as gr :refer [>defn =>]]
            [potatoclient.cmd.core :as cmd-core]
            [potatoclient.specs :as specs])
  (:import [cmd.Lrf_calib
            JonSharedCmdLrfAlign$Root
            JonSharedCmdLrfAlign$Offsets
            JonSharedCmdLrfAlign$SetOffsets
            JonSharedCmdLrfAlign$ShiftOffsetsBy
            JonSharedCmdLrfAlign$SaveOffsets
            JonSharedCmdLrfAlign$ResetOffsets]))

;; ============================================================================
;; Day Camera Offset Commands
;; ============================================================================

(>defn set-offset-day
  "Set day camera alignment offset"
  [x y]
  [::specs/offset-value ::specs/offset-value => nil?]
  (let [root-msg (cmd-core/create-root-message)
        lrf-align-root (JonSharedCmdLrfAlign$Root/newBuilder)
        offsets (JonSharedCmdLrfAlign$Offsets/newBuilder)
        set-offsets (-> (JonSharedCmdLrfAlign$SetOffsets/newBuilder)
                        (.setX x)
                        (.setY y))]
    (.setSet offsets set-offsets)
    (.setDay lrf-align-root offsets)
    (.setLrfAlignment root-msg lrf-align-root)
    (cmd-core/send-cmd-message root-msg))
  nil)

(>defn shift-offset-day
  "Shift day camera alignment offset by x,y pixels"
  [x y]
  [::specs/offset-shift ::specs/offset-shift => nil?]
  (let [root-msg (cmd-core/create-root-message)
        lrf-align-root (JonSharedCmdLrfAlign$Root/newBuilder)
        offsets (JonSharedCmdLrfAlign$Offsets/newBuilder)
        shift-offsets (-> (JonSharedCmdLrfAlign$ShiftOffsetsBy/newBuilder)
                          (.setX x)
                          (.setY y))]
    (.setShift offsets shift-offsets)
    (.setDay lrf-align-root offsets)
    (.setLrfAlignment root-msg lrf-align-root)
    (cmd-core/send-cmd-message root-msg))
  nil)

;; ============================================================================
;; Heat Camera Offset Commands
;; ============================================================================

(>defn set-offset-heat
  "Set heat camera alignment offset"
  [x y]
  [::specs/offset-value ::specs/offset-value => nil?]
  (let [root-msg (cmd-core/create-root-message)
        lrf-align-root (JonSharedCmdLrfAlign$Root/newBuilder)
        offsets (JonSharedCmdLrfAlign$Offsets/newBuilder)
        set-offsets (-> (JonSharedCmdLrfAlign$SetOffsets/newBuilder)
                        (.setX x)
                        (.setY y))]
    (.setSet offsets set-offsets)
    (.setHeat lrf-align-root offsets)
    (.setLrfAlignment root-msg lrf-align-root)
    (cmd-core/send-cmd-message root-msg))
  nil)

(>defn shift-offset-heat
  "Shift heat camera alignment offset by x,y pixels"
  [x y]
  [::specs/offset-shift ::specs/offset-shift => nil?]
  (let [root-msg (cmd-core/create-root-message)
        lrf-align-root (JonSharedCmdLrfAlign$Root/newBuilder)
        offsets (JonSharedCmdLrfAlign$Offsets/newBuilder)
        shift-offsets (-> (JonSharedCmdLrfAlign$ShiftOffsetsBy/newBuilder)
                          (.setX x)
                          (.setY y))]
    (.setShift offsets shift-offsets)
    (.setHeat lrf-align-root offsets)
    (.setLrfAlignment root-msg lrf-align-root)
    (cmd-core/send-cmd-message root-msg))
  nil)

;; ============================================================================
;; Common Commands
;; ============================================================================

(>defn save-offsets
  "Save current offsets to persistent storage"
  []
  [=> nil?]
  (let [root-msg (cmd-core/create-root-message)
        lrf-align-root (JonSharedCmdLrfAlign$Root/newBuilder)
        offsets (JonSharedCmdLrfAlign$Offsets/newBuilder)
        save (JonSharedCmdLrfAlign$SaveOffsets/newBuilder)]
    (.setSave offsets save)
    (.setDay lrf-align-root offsets) ; Need to set a channel, using day
    (.setLrfAlignment root-msg lrf-align-root)
    (cmd-core/send-cmd-message root-msg))
  nil)

(>defn reset-offsets
  "Reset all offsets to zero"
  []
  [=> nil?]
  (let [root-msg (cmd-core/create-root-message)
        lrf-align-root (JonSharedCmdLrfAlign$Root/newBuilder)
        offsets (JonSharedCmdLrfAlign$Offsets/newBuilder)
        reset (JonSharedCmdLrfAlign$ResetOffsets/newBuilder)]
    (.setReset offsets reset)
    (.setDay lrf-align-root offsets) ; Need to set a channel, using day
    (.setLrfAlignment root-msg lrf-align-root)
    (cmd-core/send-cmd-message root-msg))
  nil)