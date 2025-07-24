(ns potatoclient.cmd.osd
  "OSD (On-Screen Display) command functions for PotatoClient"
  (:require [com.fulcrologic.guardrails.malli.core :as gr :refer [>defn =>]]
            [potatoclient.cmd.core :as cmd-core])
  (:import [cmd.OSD
            JonSharedCmdOsd$Root
            JonSharedCmdOsd$ShowDefaultScreen JonSharedCmdOsd$ShowLRFMeasureScreen
            JonSharedCmdOsd$ShowLRFResultScreen JonSharedCmdOsd$ShowLRFResultSimplifiedScreen
            JonSharedCmdOsd$EnableHeatOSD JonSharedCmdOsd$DisableHeatOSD
            JonSharedCmdOsd$EnableDayOSD JonSharedCmdOsd$DisableDayOSD]))

;; ============================================================================
;; Screen Display Commands
;; ============================================================================

(>defn show-default-screen
  "Show the default OSD screen"
  []
  [=> nil?]
  (let [root-msg (cmd-core/create-root-message)
        osd-root (-> (JonSharedCmdOsd$Root/newBuilder)
                     (.setShowDefaultScreen (JonSharedCmdOsd$ShowDefaultScreen/newBuilder)))]
    (.setOsd root-msg osd-root)
    (cmd-core/send-cmd-message root-msg))
  nil)

(>defn show-lrf-measure-screen
  "Show the LRF measurement screen"
  []
  [=> nil?]
  (let [root-msg (cmd-core/create-root-message)
        osd-root (-> (JonSharedCmdOsd$Root/newBuilder)
                     (.setShowLrfMeasureScreen (JonSharedCmdOsd$ShowLRFMeasureScreen/newBuilder)))]
    (.setOsd root-msg osd-root)
    (cmd-core/send-cmd-message root-msg))
  nil)

(>defn show-lrf-result-screen
  "Show the LRF result screen"
  []
  [=> nil?]
  (let [root-msg (cmd-core/create-root-message)
        osd-root (-> (JonSharedCmdOsd$Root/newBuilder)
                     (.setShowLrfResultScreen (JonSharedCmdOsd$ShowLRFResultScreen/newBuilder)))]
    (.setOsd root-msg osd-root)
    (cmd-core/send-cmd-message root-msg))
  nil)

(>defn show-lrf-result-simplified-screen
  "Show the simplified LRF result screen"
  []
  [=> nil?]
  (let [root-msg (cmd-core/create-root-message)
        osd-root (-> (JonSharedCmdOsd$Root/newBuilder)
                     (.setShowLrfResultSimplifiedScreen (JonSharedCmdOsd$ShowLRFResultSimplifiedScreen/newBuilder)))]
    (.setOsd root-msg osd-root)
    (cmd-core/send-cmd-message root-msg))
  nil)

;; ============================================================================
;; Day Camera OSD Commands
;; ============================================================================

(>defn enable-day-osd
  "Enable OSD overlay on day camera"
  []
  [=> nil?]
  (let [root-msg (cmd-core/create-root-message)
        osd-root (-> (JonSharedCmdOsd$Root/newBuilder)
                     (.setEnableDayOsd (JonSharedCmdOsd$EnableDayOSD/newBuilder)))]
    (.setOsd root-msg osd-root)
    (cmd-core/send-cmd-message root-msg))
  nil)

(>defn disable-day-osd
  "Disable OSD overlay on day camera"
  []
  [=> nil?]
  (let [root-msg (cmd-core/create-root-message)
        osd-root (-> (JonSharedCmdOsd$Root/newBuilder)
                     (.setDisableDayOsd (JonSharedCmdOsd$DisableDayOSD/newBuilder)))]
    (.setOsd root-msg osd-root)
    (cmd-core/send-cmd-message root-msg))
  nil)

;; ============================================================================
;; Heat Camera OSD Commands
;; ============================================================================

(>defn enable-heat-osd
  "Enable OSD overlay on heat camera"
  []
  [=> nil?]
  (let [root-msg (cmd-core/create-root-message)
        osd-root (-> (JonSharedCmdOsd$Root/newBuilder)
                     (.setEnableHeatOsd (JonSharedCmdOsd$EnableHeatOSD/newBuilder)))]
    (.setOsd root-msg osd-root)
    (cmd-core/send-cmd-message root-msg))
  nil)

(>defn disable-heat-osd
  "Disable OSD overlay on heat camera"
  []
  [=> nil?]
  (let [root-msg (cmd-core/create-root-message)
        osd-root (-> (JonSharedCmdOsd$Root/newBuilder)
                     (.setDisableHeatOsd (JonSharedCmdOsd$DisableHeatOSD/newBuilder)))]
    (.setOsd root-msg osd-root)
    (cmd-core/send-cmd-message root-msg))
  nil)