(ns potatoclient.cmd.lrf-alignment
  "LRF alignment command functions for PotatoClient"
  (:require [com.fulcrologic.guardrails.malli.core :as gr :refer [>defn =>]]
            [potatoclient.cmd.core :as cmd-core]
            [potatoclient.specs :as specs])
  (:import [cmd.Lrf_calib
            JonSharedCmdLrfAlign$Root
            JonSharedCmdLrfAlign$SetOffsetDay JonSharedCmdLrfAlign$ShiftOffsetDay
            JonSharedCmdLrfAlign$SetOffsetHeat JonSharedCmdLrfAlign$ShiftOffsetHeat
            JonSharedCmdLrfAlign$SaveOffsets JonSharedCmdLrfAlign$ResetOffsets]))

;; ============================================================================
;; Day Camera Offset Commands
;; ============================================================================

(>defn set-offset-day
  "Set day camera alignment offset"
  [azimuth elevation]
  [::specs/offset-value ::specs/offset-value => nil?]
  (let [root-msg (cmd-core/create-root-message)
        offset-msg (-> (JonSharedCmdLrfAlign$SetOffsetDay/newBuilder)
                       (.setOffsetAz azimuth)
                       (.setOffsetEl elevation))
        lrf-align-root (-> (JonSharedCmdLrfAlign$Root/newBuilder)
                           (.setSetOffsetDay offset-msg))]
    (.setLrfAlign root-msg lrf-align-root)
    (cmd-core/send-cmd-message root-msg))
  nil)

(>defn shift-offset-day
  "Shift day camera alignment offset by relative amounts"
  [azimuth-shift elevation-shift]
  [::specs/offset-value ::specs/offset-value => nil?]
  (let [root-msg (cmd-core/create-root-message)
        shift-msg (-> (JonSharedCmdLrfAlign$ShiftOffsetDay/newBuilder)
                      (.setShiftOffsetAz azimuth-shift)
                      (.setShiftOffsetEl elevation-shift))
        lrf-align-root (-> (JonSharedCmdLrfAlign$Root/newBuilder)
                           (.setShiftOffsetDay shift-msg))]
    (.setLrfAlign root-msg lrf-align-root)
    (cmd-core/send-cmd-message root-msg))
  nil)

;; ============================================================================
;; Heat Camera Offset Commands
;; ============================================================================

(>defn set-offset-heat
  "Set heat camera alignment offset"
  [azimuth elevation]
  [::specs/offset-value ::specs/offset-value => nil?]
  (let [root-msg (cmd-core/create-root-message)
        offset-msg (-> (JonSharedCmdLrfAlign$SetOffsetHeat/newBuilder)
                       (.setOffsetAz azimuth)
                       (.setOffsetEl elevation))
        lrf-align-root (-> (JonSharedCmdLrfAlign$Root/newBuilder)
                           (.setSetOffsetHeat offset-msg))]
    (.setLrfAlign root-msg lrf-align-root)
    (cmd-core/send-cmd-message root-msg))
  nil)

(>defn shift-offset-heat
  "Shift heat camera alignment offset by relative amounts"
  [azimuth-shift elevation-shift]
  [::specs/offset-value ::specs/offset-value => nil?]
  (let [root-msg (cmd-core/create-root-message)
        shift-msg (-> (JonSharedCmdLrfAlign$ShiftOffsetHeat/newBuilder)
                      (.setShiftOffsetAz azimuth-shift)
                      (.setShiftOffsetEl elevation-shift))
        lrf-align-root (-> (JonSharedCmdLrfAlign$Root/newBuilder)
                           (.setShiftOffsetHeat shift-msg))]
    (.setLrfAlign root-msg lrf-align-root)
    (cmd-core/send-cmd-message root-msg))
  nil)

;; ============================================================================
;; Offset Management Commands
;; ============================================================================

(>defn save-offsets
  "Save current LRF offsets to persistent storage"
  []
  [=> nil?]
  (let [root-msg (cmd-core/create-root-message)
        lrf-align-root (-> (JonSharedCmdLrfAlign$Root/newBuilder)
                           (.setSaveOffsets (JonSharedCmdLrfAlign$SaveOffsets/newBuilder)))]
    (.setLrfAlign root-msg lrf-align-root)
    (cmd-core/send-cmd-message root-msg))
  nil)

(>defn reset-offsets
  "Reset LRF offsets to default values"
  []
  [=> nil?]
  (let [root-msg (cmd-core/create-root-message)
        lrf-align-root (-> (JonSharedCmdLrfAlign$Root/newBuilder)
                           (.setResetOffsets (JonSharedCmdLrfAlign$ResetOffsets/newBuilder)))]
    (.setLrfAlign root-msg lrf-align-root)
    (cmd-core/send-cmd-message root-msg))
  nil)