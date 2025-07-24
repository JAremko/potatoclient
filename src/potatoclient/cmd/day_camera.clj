(ns potatoclient.cmd.day-camera
  "Day camera command functions for PotatoClient"
  (:require [com.fulcrologic.guardrails.malli.core :as gr :refer [>defn ? =>]]
            [potatoclient.cmd.core :as cmd-core]
            [potatoclient.specs :as specs])
  (:import [cmd.DayCamera
            JonSharedCmdDayCamera$Root
            JonSharedCmdDayCamera$Start JonSharedCmdDayCamera$Stop
            JonSharedCmdDayCamera$Photo JonSharedCmdDayCamera$Halt
            JonSharedCmdDayCamera$SetInfraRedFilter JonSharedCmdDayCamera$SetIris
            JonSharedCmdDayCamera$SetAutoIris JonSharedCmdDayCamera$GetMeteo
            JonSharedCmdDayCamera$Focus JonSharedCmdDayCamera$Zoom
            JonSharedCmdDayCamera$SetValue JonSharedCmdDayCamera$Move
            JonSharedCmdDayCamera$Offset JonSharedCmdDayCamera$HaltAll
            JonSharedCmdDayCamera$ResetFocus JonSharedCmdDayCamera$SaveToTableFocus
            JonSharedCmdDayCamera$ResetZoom JonSharedCmdDayCamera$SaveToTable
            JonSharedCmdDayCamera$SetZoomTableValue
            JonSharedCmdDayCamera$NextZoomTablePos JonSharedCmdDayCamera$PrevZoomTablePos
            JonSharedCmdDayCamera$SetDigitalZoomLevel
            JonSharedCmdDayCamera$SetFxMode JonSharedCmdDayCamera$NextFxMode JonSharedCmdDayCamera$PrevFxMode
            JonSharedCmdDayCamera$SetClaheLevel JonSharedCmdDayCamera$ShiftClaheLevel]
           [data
            JonSharedDataTypes$JonGuiDataFxModeDay]))

;; ============================================================================
;; Basic Commands
;; ============================================================================

(>defn start
  "Start the day camera"
  []
  [=> nil?]
  (let [root-msg (cmd-core/create-root-message)
        day-camera-root (-> (JonSharedCmdDayCamera$Root/newBuilder)
                            (.setStart (JonSharedCmdDayCamera$Start/newBuilder)))]
    (.setDayCamera root-msg day-camera-root)
    (cmd-core/send-cmd-message root-msg))
  nil)

(>defn stop
  "Stop the day camera"
  []
  [=> nil?]
  (let [root-msg (cmd-core/create-root-message)
        day-camera-root (-> (JonSharedCmdDayCamera$Root/newBuilder)
                            (.setStop (JonSharedCmdDayCamera$Stop/newBuilder)))]
    (.setDayCamera root-msg day-camera-root)
    (cmd-core/send-cmd-message root-msg))
  nil)

(>defn take-photo
  "Take a photo with the day camera"
  []
  [=> nil?]
  (let [root-msg (cmd-core/create-root-message)
        day-camera-root (-> (JonSharedCmdDayCamera$Root/newBuilder)
                            (.setPhoto (JonSharedCmdDayCamera$Photo/newBuilder)))]
    (.setDayCamera root-msg day-camera-root)
    (cmd-core/send-cmd-message root-msg))
  nil)

(>defn halt-all
  "Halt all day camera operations"
  []
  [=> nil?]
  (let [root-msg (cmd-core/create-root-message)
        day-camera-root (-> (JonSharedCmdDayCamera$Root/newBuilder)
                            (.setHaltAll (JonSharedCmdDayCamera$HaltAll/newBuilder)))]
    (.setDayCamera root-msg day-camera-root)
    (cmd-core/send-cmd-message root-msg))
  nil)

;; ============================================================================
;; Camera Settings
;; ============================================================================

(>defn set-infrared-filter
  "Set infrared filter on/off"
  [value]
  [boolean? => nil?]
  (let [root-msg (cmd-core/create-root-message)
        ir-filter (-> (JonSharedCmdDayCamera$SetInfraRedFilter/newBuilder)
                      (.setValue value))
        day-camera-root (-> (JonSharedCmdDayCamera$Root/newBuilder)
                            (.setSetInfraRedFilter ir-filter))]
    (.setDayCamera root-msg day-camera-root)
    (cmd-core/send-cmd-message root-msg))
  nil)

(>defn set-iris
  "Set iris value (0.0 to 1.0)"
  [value]
  [::specs/normalized-value => nil?]
  (let [root-msg (cmd-core/create-root-message)
        iris (-> (JonSharedCmdDayCamera$SetIris/newBuilder)
                 (.setValue (float value)))
        day-camera-root (-> (JonSharedCmdDayCamera$Root/newBuilder)
                            (.setSetIris iris))]
    (.setDayCamera root-msg day-camera-root)
    (cmd-core/send-cmd-message root-msg))
  nil)

(>defn set-auto-iris
  "Enable/disable auto iris"
  [value]
  [boolean? => nil?]
  (let [root-msg (cmd-core/create-root-message)
        auto-iris (-> (JonSharedCmdDayCamera$SetAutoIris/newBuilder)
                      (.setValue value))
        day-camera-root (-> (JonSharedCmdDayCamera$Root/newBuilder)
                            (.setSetAutoIris auto-iris))]
    (.setDayCamera root-msg day-camera-root)
    (cmd-core/send-cmd-message root-msg))
  nil)

;; ============================================================================
;; Focus Commands
;; ============================================================================

(>defn focus-set-value
  "Set focus to specific value (0.0 to 1.0)"
  [value]
  [::specs/normalized-value => nil?]
  (let [root-msg (cmd-core/create-root-message)
        set-value (-> (JonSharedCmdDayCamera$SetValue/newBuilder)
                      (.setValue (float value)))
        focus (-> (JonSharedCmdDayCamera$Focus/newBuilder)
                  (.setSetValue set-value))
        day-camera-root (-> (JonSharedCmdDayCamera$Root/newBuilder)
                            (.setFocus focus))]
    (.setDayCamera root-msg day-camera-root)
    (cmd-core/send-cmd-message root-msg))
  nil)

(>defn focus-move
  "Move focus to target value at specified speed"
  [target-value speed]
  [::specs/normalized-value ::specs/normalized-value => nil?]
  (let [root-msg (cmd-core/create-root-message)
        move (-> (JonSharedCmdDayCamera$Move/newBuilder)
                 (.setTargetValue (float target-value))
                 (.setSpeed (float speed)))
        focus (-> (JonSharedCmdDayCamera$Focus/newBuilder)
                  (.setMove move))
        day-camera-root (-> (JonSharedCmdDayCamera$Root/newBuilder)
                            (.setFocus focus))]
    (.setDayCamera root-msg day-camera-root)
    (cmd-core/send-cmd-message root-msg))
  nil)

(>defn focus-halt
  "Stop focus movement"
  []
  [=> nil?]
  (let [root-msg (cmd-core/create-root-message)
        halt (JonSharedCmdDayCamera$Halt/newBuilder)
        focus (-> (JonSharedCmdDayCamera$Focus/newBuilder)
                  (.setHalt halt))
        day-camera-root (-> (JonSharedCmdDayCamera$Root/newBuilder)
                            (.setFocus focus))]
    (.setDayCamera root-msg day-camera-root)
    (cmd-core/send-cmd-message root-msg))
  nil)

(>defn focus-offset
  "Offset focus by specified value (-1.0 to 1.0)"
  [offset-value]
  [::specs/offset-value => nil?]
  (let [root-msg (cmd-core/create-root-message)
        offset (-> (JonSharedCmdDayCamera$Offset/newBuilder)
                   (.setOffsetValue (float offset-value)))
        focus (-> (JonSharedCmdDayCamera$Focus/newBuilder)
                  (.setOffset offset))
        day-camera-root (-> (JonSharedCmdDayCamera$Root/newBuilder)
                            (.setFocus focus))]
    (.setDayCamera root-msg day-camera-root)
    (cmd-core/send-cmd-message root-msg))
  nil)

(>defn focus-reset
  "Reset focus to default"
  []
  [=> nil?]
  (let [root-msg (cmd-core/create-root-message)
        reset (JonSharedCmdDayCamera$ResetFocus/newBuilder)
        focus (-> (JonSharedCmdDayCamera$Focus/newBuilder)
                  (.setResetFocus reset))
        day-camera-root (-> (JonSharedCmdDayCamera$Root/newBuilder)
                            (.setFocus focus))]
    (.setDayCamera root-msg day-camera-root)
    (cmd-core/send-cmd-message root-msg))
  nil)

(>defn focus-save-to-table
  "Save current focus position to table"
  []
  [=> nil?]
  (let [root-msg (cmd-core/create-root-message)
        save (JonSharedCmdDayCamera$SaveToTableFocus/newBuilder)
        focus (-> (JonSharedCmdDayCamera$Focus/newBuilder)
                  (.setSaveToTableFocus save))
        day-camera-root (-> (JonSharedCmdDayCamera$Root/newBuilder)
                            (.setFocus focus))]
    (.setDayCamera root-msg day-camera-root)
    (cmd-core/send-cmd-message root-msg))
  nil)

;; ============================================================================
;; Zoom Commands
;; ============================================================================

(>defn zoom-set-value
  "Set zoom to specific value (0.0 to 1.0)"
  [value]
  [::specs/normalized-value => nil?]
  (let [root-msg (cmd-core/create-root-message)
        set-value (-> (JonSharedCmdDayCamera$SetValue/newBuilder)
                      (.setValue (float value)))
        zoom (-> (JonSharedCmdDayCamera$Zoom/newBuilder)
                 (.setSetValue set-value))
        day-camera-root (-> (JonSharedCmdDayCamera$Root/newBuilder)
                            (.setZoom zoom))]
    (.setDayCamera root-msg day-camera-root)
    (cmd-core/send-cmd-message root-msg))
  nil)

(>defn zoom-move
  "Move zoom to target value at specified speed"
  [target-value speed]
  [::specs/normalized-value ::specs/normalized-value => nil?]
  (let [root-msg (cmd-core/create-root-message)
        move (-> (JonSharedCmdDayCamera$Move/newBuilder)
                 (.setTargetValue (float target-value))
                 (.setSpeed (float speed)))
        zoom (-> (JonSharedCmdDayCamera$Zoom/newBuilder)
                 (.setMove move))
        day-camera-root (-> (JonSharedCmdDayCamera$Root/newBuilder)
                            (.setZoom zoom))]
    (.setDayCamera root-msg day-camera-root)
    (cmd-core/send-cmd-message root-msg))
  nil)

(>defn zoom-halt
  "Stop zoom movement"
  []
  [=> nil?]
  (let [root-msg (cmd-core/create-root-message)
        halt (JonSharedCmdDayCamera$Halt/newBuilder)
        zoom (-> (JonSharedCmdDayCamera$Zoom/newBuilder)
                 (.setHalt halt))
        day-camera-root (-> (JonSharedCmdDayCamera$Root/newBuilder)
                            (.setZoom zoom))]
    (.setDayCamera root-msg day-camera-root)
    (cmd-core/send-cmd-message root-msg))
  nil)

(>defn zoom-offset
  "Offset zoom by specified value (-1.0 to 1.0)"
  [offset-value]
  [::specs/offset-value => nil?]
  (let [root-msg (cmd-core/create-root-message)
        offset (-> (JonSharedCmdDayCamera$Offset/newBuilder)
                   (.setOffsetValue (float offset-value)))
        zoom (-> (JonSharedCmdDayCamera$Zoom/newBuilder)
                 (.setOffset offset))
        day-camera-root (-> (JonSharedCmdDayCamera$Root/newBuilder)
                            (.setZoom zoom))]
    (.setDayCamera root-msg day-camera-root)
    (cmd-core/send-cmd-message root-msg))
  nil)

(>defn zoom-reset
  "Reset zoom to default"
  []
  [=> nil?]
  (let [root-msg (cmd-core/create-root-message)
        reset (JonSharedCmdDayCamera$ResetZoom/newBuilder)
        zoom (-> (JonSharedCmdDayCamera$Zoom/newBuilder)
                 (.setResetZoom reset))
        day-camera-root (-> (JonSharedCmdDayCamera$Root/newBuilder)
                            (.setZoom zoom))]
    (.setDayCamera root-msg day-camera-root)
    (cmd-core/send-cmd-message root-msg))
  nil)

(>defn zoom-save-to-table
  "Save current zoom position to table"
  []
  [=> nil?]
  (let [root-msg (cmd-core/create-root-message)
        save (JonSharedCmdDayCamera$SaveToTable/newBuilder)
        zoom (-> (JonSharedCmdDayCamera$Zoom/newBuilder)
                 (.setSaveToTable save))
        day-camera-root (-> (JonSharedCmdDayCamera$Root/newBuilder)
                            (.setZoom zoom))]
    (.setDayCamera root-msg day-camera-root)
    (cmd-core/send-cmd-message root-msg))
  nil)

(>defn zoom-set-table-value
  "Set zoom table position"
  [value]
  [nat-int? => nil?]
  (let [root-msg (cmd-core/create-root-message)
        table-value (-> (JonSharedCmdDayCamera$SetZoomTableValue/newBuilder)
                        (.setValue (int value)))
        zoom (-> (JonSharedCmdDayCamera$Zoom/newBuilder)
                 (.setSetZoomTableValue table-value))
        day-camera-root (-> (JonSharedCmdDayCamera$Root/newBuilder)
                            (.setZoom zoom))]
    (.setDayCamera root-msg day-camera-root)
    (cmd-core/send-cmd-message root-msg))
  nil)

(>defn zoom-next-table-position
  "Move to next zoom table position"
  []
  [=> nil?]
  (let [root-msg (cmd-core/create-root-message)
        next-pos (JonSharedCmdDayCamera$NextZoomTablePos/newBuilder)
        zoom (-> (JonSharedCmdDayCamera$Zoom/newBuilder)
                 (.setNextZoomTablePos next-pos))
        day-camera-root (-> (JonSharedCmdDayCamera$Root/newBuilder)
                            (.setZoom zoom))]
    (.setDayCamera root-msg day-camera-root)
    (cmd-core/send-cmd-message root-msg))
  nil)

(>defn zoom-prev-table-position
  "Move to previous zoom table position"
  []
  [=> nil?]
  (let [root-msg (cmd-core/create-root-message)
        prev-pos (JonSharedCmdDayCamera$PrevZoomTablePos/newBuilder)
        zoom (-> (JonSharedCmdDayCamera$Zoom/newBuilder)
                 (.setPrevZoomTablePos prev-pos))
        day-camera-root (-> (JonSharedCmdDayCamera$Root/newBuilder)
                            (.setZoom zoom))]
    (.setDayCamera root-msg day-camera-root)
    (cmd-core/send-cmd-message root-msg))
  nil)

(>defn set-digital-zoom-level
  "Set digital zoom level (1.0 or greater)"
  [value]
  [::specs/digital-zoom-level => nil?]
  (let [root-msg (cmd-core/create-root-message)
        digital-zoom (-> (JonSharedCmdDayCamera$SetDigitalZoomLevel/newBuilder)
                         (.setValue (float value)))
        day-camera-root (-> (JonSharedCmdDayCamera$Root/newBuilder)
                            (.setSetDigitalZoomLevel digital-zoom))]
    (.setDayCamera root-msg day-camera-root)
    (cmd-core/send-cmd-message root-msg))
  nil)

;; ============================================================================
;; FX Mode Commands
;; ============================================================================

(>defn set-fx-mode
  "Set FX mode"
  [mode]
  [::specs/day-fx-mode => nil?]
  (let [root-msg (cmd-core/create-root-message)
        fx-mode (-> (JonSharedCmdDayCamera$SetFxMode/newBuilder)
                    (.setMode mode))
        day-camera-root (-> (JonSharedCmdDayCamera$Root/newBuilder)
                            (.setSetFxMode fx-mode))]
    (.setDayCamera root-msg day-camera-root)
    (cmd-core/send-cmd-message root-msg))
  nil)

(>defn next-fx-mode
  "Switch to next FX mode"
  []
  [=> nil?]
  (let [root-msg (cmd-core/create-root-message)
        day-camera-root (-> (JonSharedCmdDayCamera$Root/newBuilder)
                            (.setNextFxMode (JonSharedCmdDayCamera$NextFxMode/newBuilder)))]
    (.setDayCamera root-msg day-camera-root)
    (cmd-core/send-cmd-message root-msg))
  nil)

(>defn prev-fx-mode
  "Switch to previous FX mode"
  []
  [=> nil?]
  (let [root-msg (cmd-core/create-root-message)
        day-camera-root (-> (JonSharedCmdDayCamera$Root/newBuilder)
                            (.setPrevFxMode (JonSharedCmdDayCamera$PrevFxMode/newBuilder)))]
    (.setDayCamera root-msg day-camera-root)
    (cmd-core/send-cmd-message root-msg))
  nil)

;; ============================================================================
;; CLAHE Commands
;; ============================================================================

(>defn set-clahe-level
  "Set CLAHE enhancement level (0.0 to 1.0)"
  [value]
  [::specs/normalized-value => nil?]
  (let [root-msg (cmd-core/create-root-message)
        clahe (-> (JonSharedCmdDayCamera$SetClaheLevel/newBuilder)
                  (.setValue (float value)))
        day-camera-root (-> (JonSharedCmdDayCamera$Root/newBuilder)
                            (.setSetClaheLevel clahe))]
    (.setDayCamera root-msg day-camera-root)
    (cmd-core/send-cmd-message root-msg))
  nil)

(>defn shift-clahe-level
  "Shift CLAHE level by specified amount (-1.0 to 1.0)"
  [value]
  [::specs/offset-value => nil?]
  (let [root-msg (cmd-core/create-root-message)
        shift (-> (JonSharedCmdDayCamera$ShiftClaheLevel/newBuilder)
                  (.setValue (float value)))
        day-camera-root (-> (JonSharedCmdDayCamera$Root/newBuilder)
                            (.setShiftClaheLevel shift))]
    (.setDayCamera root-msg day-camera-root)
    (cmd-core/send-cmd-message root-msg))
  nil)

;; ============================================================================
;; Meteo Data
;; ============================================================================

(>defn get-meteo
  "Request day camera meteorological data (read-only safe)"
  []
  [=> nil?]
  (let [root-msg (cmd-core/create-root-message)
        day-camera-root (-> (JonSharedCmdDayCamera$Root/newBuilder)
                            (.setGetMeteo (JonSharedCmdDayCamera$GetMeteo/newBuilder)))]
    (.setDayCamera root-msg day-camera-root)
    (cmd-core/send-cmd-message root-msg))
  nil)

;; ============================================================================
;; Utility Functions
;; ============================================================================

(>defn string->fx-mode
  "Convert string to FX mode enum value"
  [value]
  [string? => (? ::specs/day-fx-mode)]
  (case value
    "default" JonSharedDataTypes$JonGuiDataFxModeDay/JON_GUI_DATA_FX_MODE_DAY_DEFAULT
    "a" JonSharedDataTypes$JonGuiDataFxModeDay/JON_GUI_DATA_FX_MODE_DAY_A
    "b" JonSharedDataTypes$JonGuiDataFxModeDay/JON_GUI_DATA_FX_MODE_DAY_B
    "c" JonSharedDataTypes$JonGuiDataFxModeDay/JON_GUI_DATA_FX_MODE_DAY_C
    "d" JonSharedDataTypes$JonGuiDataFxModeDay/JON_GUI_DATA_FX_MODE_DAY_D
    "e" JonSharedDataTypes$JonGuiDataFxModeDay/JON_GUI_DATA_FX_MODE_DAY_E
    "f" JonSharedDataTypes$JonGuiDataFxModeDay/JON_GUI_DATA_FX_MODE_DAY_F
    nil))

;; Convenience aliases for commonly used functions
(>defn focus-in
  "Focus near (continuous movement)"
  []
  [=> nil?]
  (focus-move 0.0 0.5))

(>defn focus-out
  "Focus far (continuous movement)"
  []
  [=> nil?]
  (focus-move 1.0 0.5))

(>defn focus-step-in
  "Single step focus near"
  []
  [=> nil?]
  (focus-offset -0.1))

(>defn focus-step-out
  "Single step focus far"
  []
  [=> nil?]
  (focus-offset 0.1))

(>defn zoom-in
  "Zoom in (continuous movement)"
  []
  [=> nil?]
  (zoom-move 1.0 0.5))

(>defn zoom-out
  "Zoom out (continuous movement)"
  []
  [=> nil?]
  (zoom-move 0.0 0.5))

(>defn focus-auto
  "Enable auto focus (compatibility alias)"
  []
  [=> nil?]
  (focus-reset))

(>defn focus-manual
  "Enable manual focus (compatibility alias)"
  []
  [=> nil?]
  (focus-halt))