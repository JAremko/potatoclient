(ns potatoclient.cmd.heat-camera
  "Heat camera command functions for PotatoClient"
  (:require [com.fulcrologic.guardrails.malli.core :as gr :refer [>defn ? =>]]
            [potatoclient.cmd.core :as cmd-core]
            [potatoclient.specs :as specs])
  (:import [cmd.HeatCamera
            JonSharedCmdHeatCamera$Root
            JonSharedCmdHeatCamera$Start JonSharedCmdHeatCamera$Stop
            JonSharedCmdHeatCamera$Photo JonSharedCmdHeatCamera$Calibrate
            JonSharedCmdHeatCamera$SetAGC JonSharedCmdHeatCamera$SetFilters
            JonSharedCmdHeatCamera$SetAutoFocus JonSharedCmdHeatCamera$GetMeteo
            JonSharedCmdHeatCamera$Zoom JonSharedCmdHeatCamera$SetZoomTableValue
            JonSharedCmdHeatCamera$NextZoomTablePos JonSharedCmdHeatCamera$PrevZoomTablePos
            JonSharedCmdHeatCamera$SetDigitalZoomLevel
            JonSharedCmdHeatCamera$ZoomIn JonSharedCmdHeatCamera$ZoomOut JonSharedCmdHeatCamera$ZoomStop
            JonSharedCmdHeatCamera$FocusIn JonSharedCmdHeatCamera$FocusOut JonSharedCmdHeatCamera$FocusStop
            JonSharedCmdHeatCamera$FocusStepPlus JonSharedCmdHeatCamera$FocusStepMinus
            JonSharedCmdHeatCamera$EnableDDE JonSharedCmdHeatCamera$DisableDDE
            JonSharedCmdHeatCamera$SetDDELevel JonSharedCmdHeatCamera$ShiftDDE
            JonSharedCmdHeatCamera$SetFxMode JonSharedCmdHeatCamera$NextFxMode JonSharedCmdHeatCamera$PrevFxMode
            JonSharedCmdHeatCamera$SetClaheLevel JonSharedCmdHeatCamera$ShiftClaheLevel
            JonSharedCmdHeatCamera$ResetZoom JonSharedCmdHeatCamera$SaveToTable
            JonSharedCmdHeatCamera$SetCalibMode]
           [data
            JonSharedDataTypes$JonGuiDataVideoChannelHeatAGCModes
            JonSharedDataTypes$JonGuiDataVideoChannelHeatFilters
            JonSharedDataTypes$JonGuiDataFxModeHeat]))

;; ============================================================================
;; Basic Commands
;; ============================================================================

(>defn start
  "Start the heat camera"
  []
  [=> nil?]
  (let [root-msg (cmd-core/create-root-message)
        heat-camera-root (-> (JonSharedCmdHeatCamera$Root/newBuilder)
                             (.setStart (JonSharedCmdHeatCamera$Start/newBuilder)))]
    (.setHeatCamera root-msg heat-camera-root)
    (cmd-core/send-cmd-message root-msg))
  nil)

(>defn stop
  "Stop the heat camera"
  []
  [=> nil?]
  (let [root-msg (cmd-core/create-root-message)
        heat-camera-root (-> (JonSharedCmdHeatCamera$Root/newBuilder)
                             (.setStop (JonSharedCmdHeatCamera$Stop/newBuilder)))]
    (.setHeatCamera root-msg heat-camera-root)
    (cmd-core/send-cmd-message root-msg))
  nil)

(>defn take-photo
  "Take a photo with the heat camera"
  []
  [=> nil?]
  (let [root-msg (cmd-core/create-root-message)
        heat-camera-root (-> (JonSharedCmdHeatCamera$Root/newBuilder)
                             (.setPhoto (JonSharedCmdHeatCamera$Photo/newBuilder)))]
    (.setHeatCamera root-msg heat-camera-root)
    (cmd-core/send-cmd-message root-msg))
  nil)

(>defn calibrate
  "Calibrate the heat camera (shutter)"
  []
  [=> nil?]
  (let [root-msg (cmd-core/create-root-message)
        heat-camera-root (-> (JonSharedCmdHeatCamera$Root/newBuilder)
                             (.setCalibrate (JonSharedCmdHeatCamera$Calibrate/newBuilder)))]
    (.setHeatCamera root-msg heat-camera-root)
    (cmd-core/send-cmd-message root-msg))
  nil)

;; ============================================================================
;; Camera Settings
;; ============================================================================

(>defn set-agc-mode
  "Set AGC (Automatic Gain Control) mode"
  [mode]
  [::specs/heat-agc-mode => nil?]
  (let [root-msg (cmd-core/create-root-message)
        agc (-> (JonSharedCmdHeatCamera$SetAGC/newBuilder)
                (.setValue mode))
        heat-camera-root (-> (JonSharedCmdHeatCamera$Root/newBuilder)
                             (.setSetAgc agc))]
    (.setHeatCamera root-msg heat-camera-root)
    (cmd-core/send-cmd-message root-msg))
  nil)

(>defn set-filter
  "Set heat camera filter"
  [filter]
  [::specs/heat-filter => nil?]
  (let [root-msg (cmd-core/create-root-message)
        filter-msg (-> (JonSharedCmdHeatCamera$SetFilters/newBuilder)
                       (.setValue filter))
        heat-camera-root (-> (JonSharedCmdHeatCamera$Root/newBuilder)
                             (.setSetFilter filter-msg))]
    (.setHeatCamera root-msg heat-camera-root)
    (cmd-core/send-cmd-message root-msg))
  nil)

(>defn set-auto-focus
  "Enable/disable auto focus"
  [value]
  [boolean? => nil?]
  (let [root-msg (cmd-core/create-root-message)
        auto-focus (-> (JonSharedCmdHeatCamera$SetAutoFocus/newBuilder)
                       (.setValue value))
        heat-camera-root (-> (JonSharedCmdHeatCamera$Root/newBuilder)
                             (.setSetAutoFocus auto-focus))]
    (.setHeatCamera root-msg heat-camera-root)
    (cmd-core/send-cmd-message root-msg))
  nil)

(>defn set-calib-mode
  "Set calibration mode"
  []
  [=> nil?]
  (let [root-msg (cmd-core/create-root-message)
        heat-camera-root (-> (JonSharedCmdHeatCamera$Root/newBuilder)
                             (.setSetCalibMode (JonSharedCmdHeatCamera$SetCalibMode/newBuilder)))]
    (.setHeatCamera root-msg heat-camera-root)
    (cmd-core/send-cmd-message root-msg))
  nil)

;; ============================================================================
;; Zoom Commands
;; ============================================================================

(>defn zoom-in
  "Zoom in (continuous)"
  []
  [=> nil?]
  (let [root-msg (cmd-core/create-root-message)
        heat-camera-root (-> (JonSharedCmdHeatCamera$Root/newBuilder)
                             (.setZoomIn (JonSharedCmdHeatCamera$ZoomIn/newBuilder)))]
    (.setHeatCamera root-msg heat-camera-root)
    (cmd-core/send-cmd-message root-msg))
  nil)

(>defn zoom-out
  "Zoom out (continuous)"
  []
  [=> nil?]
  (let [root-msg (cmd-core/create-root-message)
        heat-camera-root (-> (JonSharedCmdHeatCamera$Root/newBuilder)
                             (.setZoomOut (JonSharedCmdHeatCamera$ZoomOut/newBuilder)))]
    (.setHeatCamera root-msg heat-camera-root)
    (cmd-core/send-cmd-message root-msg))
  nil)

(>defn zoom-stop
  "Stop zoom movement"
  []
  [=> nil?]
  (let [root-msg (cmd-core/create-root-message)
        heat-camera-root (-> (JonSharedCmdHeatCamera$Root/newBuilder)
                             (.setZoomStop (JonSharedCmdHeatCamera$ZoomStop/newBuilder)))]
    (.setHeatCamera root-msg heat-camera-root)
    (cmd-core/send-cmd-message root-msg))
  nil)

(>defn zoom-set-table-value
  "Set zoom table position"
  [value]
  [nat-int? => nil?]
  (let [root-msg (cmd-core/create-root-message)
        table-value (-> (JonSharedCmdHeatCamera$SetZoomTableValue/newBuilder)
                        (.setValue (int value)))
        zoom (-> (JonSharedCmdHeatCamera$Zoom/newBuilder)
                 (.setSetZoomTableValue table-value))
        heat-camera-root (-> (JonSharedCmdHeatCamera$Root/newBuilder)
                             (.setZoom zoom))]
    (.setHeatCamera root-msg heat-camera-root)
    (cmd-core/send-cmd-message root-msg))
  nil)

(>defn zoom-next-table-position
  "Move to next zoom table position"
  []
  [=> nil?]
  (let [root-msg (cmd-core/create-root-message)
        next-pos (JonSharedCmdHeatCamera$NextZoomTablePos/newBuilder)
        zoom (-> (JonSharedCmdHeatCamera$Zoom/newBuilder)
                 (.setNextZoomTablePos next-pos))
        heat-camera-root (-> (JonSharedCmdHeatCamera$Root/newBuilder)
                             (.setZoom zoom))]
    (.setHeatCamera root-msg heat-camera-root)
    (cmd-core/send-cmd-message root-msg))
  nil)

(>defn zoom-prev-table-position
  "Move to previous zoom table position"
  []
  [=> nil?]
  (let [root-msg (cmd-core/create-root-message)
        prev-pos (JonSharedCmdHeatCamera$PrevZoomTablePos/newBuilder)
        zoom (-> (JonSharedCmdHeatCamera$Zoom/newBuilder)
                 (.setPrevZoomTablePos prev-pos))
        heat-camera-root (-> (JonSharedCmdHeatCamera$Root/newBuilder)
                             (.setZoom zoom))]
    (.setHeatCamera root-msg heat-camera-root)
    (cmd-core/send-cmd-message root-msg))
  nil)

(>defn set-digital-zoom-level
  "Set digital zoom level (1.0 or greater)"
  [value]
  [::specs/digital-zoom-level => nil?]
  (let [root-msg (cmd-core/create-root-message)
        digital-zoom (-> (JonSharedCmdHeatCamera$SetDigitalZoomLevel/newBuilder)
                         (.setValue (float value)))
        heat-camera-root (-> (JonSharedCmdHeatCamera$Root/newBuilder)
                             (.setSetDigitalZoomLevel digital-zoom))]
    (.setHeatCamera root-msg heat-camera-root)
    (cmd-core/send-cmd-message root-msg))
  nil)

(>defn zoom-reset
  "Reset zoom to default"
  []
  [=> nil?]
  (let [root-msg (cmd-core/create-root-message)
        heat-camera-root (-> (JonSharedCmdHeatCamera$Root/newBuilder)
                             (.setResetZoom (JonSharedCmdHeatCamera$ResetZoom/newBuilder)))]
    (.setHeatCamera root-msg heat-camera-root)
    (cmd-core/send-cmd-message root-msg))
  nil)

(>defn zoom-save-to-table
  "Save current zoom position to table"
  []
  [=> nil?]
  (let [root-msg (cmd-core/create-root-message)
        heat-camera-root (-> (JonSharedCmdHeatCamera$Root/newBuilder)
                             (.setSaveToTable (JonSharedCmdHeatCamera$SaveToTable/newBuilder)))]
    (.setHeatCamera root-msg heat-camera-root)
    (cmd-core/send-cmd-message root-msg))
  nil)

;; ============================================================================
;; Focus Commands
;; ============================================================================

(>defn focus-in
  "Focus near (continuous)"
  []
  [=> nil?]
  (let [root-msg (cmd-core/create-root-message)
        heat-camera-root (-> (JonSharedCmdHeatCamera$Root/newBuilder)
                             (.setFocusIn (JonSharedCmdHeatCamera$FocusIn/newBuilder)))]
    (.setHeatCamera root-msg heat-camera-root)
    (cmd-core/send-cmd-message root-msg))
  nil)

(>defn focus-out
  "Focus far (continuous)"
  []
  [=> nil?]
  (let [root-msg (cmd-core/create-root-message)
        heat-camera-root (-> (JonSharedCmdHeatCamera$Root/newBuilder)
                             (.setFocusOut (JonSharedCmdHeatCamera$FocusOut/newBuilder)))]
    (.setHeatCamera root-msg heat-camera-root)
    (cmd-core/send-cmd-message root-msg))
  nil)

(>defn focus-stop
  "Stop focus movement"
  []
  [=> nil?]
  (let [root-msg (cmd-core/create-root-message)
        heat-camera-root (-> (JonSharedCmdHeatCamera$Root/newBuilder)
                             (.setFocusStop (JonSharedCmdHeatCamera$FocusStop/newBuilder)))]
    (.setHeatCamera root-msg heat-camera-root)
    (cmd-core/send-cmd-message root-msg))
  nil)

(>defn focus-step-plus
  "Single step focus plus"
  []
  [=> nil?]
  (let [root-msg (cmd-core/create-root-message)
        heat-camera-root (-> (JonSharedCmdHeatCamera$Root/newBuilder)
                             (.setFocusStepPlus (JonSharedCmdHeatCamera$FocusStepPlus/newBuilder)))]
    (.setHeatCamera root-msg heat-camera-root)
    (cmd-core/send-cmd-message root-msg))
  nil)

(>defn focus-step-minus
  "Single step focus minus"
  []
  [=> nil?]
  (let [root-msg (cmd-core/create-root-message)
        heat-camera-root (-> (JonSharedCmdHeatCamera$Root/newBuilder)
                             (.setFocusStepMinus (JonSharedCmdHeatCamera$FocusStepMinus/newBuilder)))]
    (.setHeatCamera root-msg heat-camera-root)
    (cmd-core/send-cmd-message root-msg))
  nil)

;; ============================================================================
;; DDE (Digital Detail Enhancement) Commands
;; ============================================================================

(>defn enable-dde
  "Enable Digital Detail Enhancement"
  []
  [=> nil?]
  (let [root-msg (cmd-core/create-root-message)
        heat-camera-root (-> (JonSharedCmdHeatCamera$Root/newBuilder)
                             (.setEnableDde (JonSharedCmdHeatCamera$EnableDDE/newBuilder)))]
    (.setHeatCamera root-msg heat-camera-root)
    (cmd-core/send-cmd-message root-msg))
  nil)

(>defn disable-dde
  "Disable Digital Detail Enhancement"
  []
  [=> nil?]
  (let [root-msg (cmd-core/create-root-message)
        heat-camera-root (-> (JonSharedCmdHeatCamera$Root/newBuilder)
                             (.setDisableDde (JonSharedCmdHeatCamera$DisableDDE/newBuilder)))]
    (.setHeatCamera root-msg heat-camera-root)
    (cmd-core/send-cmd-message root-msg))
  nil)

(>defn set-dde-level
  "Set DDE level (0 to 100)"
  [value]
  [::specs/dde-level => nil?]
  (let [root-msg (cmd-core/create-root-message)
        dde-level (-> (JonSharedCmdHeatCamera$SetDDELevel/newBuilder)
                      (.setValue (int value)))
        heat-camera-root (-> (JonSharedCmdHeatCamera$Root/newBuilder)
                             (.setSetDdeLevel dde-level))]
    (.setHeatCamera root-msg heat-camera-root)
    (cmd-core/send-cmd-message root-msg))
  nil)

(>defn shift-dde-level
  "Shift DDE level by specified amount (-100 to 100)"
  [value]
  [::specs/dde-shift => nil?]
  (let [root-msg (cmd-core/create-root-message)
        shift (-> (JonSharedCmdHeatCamera$ShiftDDE/newBuilder)
                  (.setValue (int value)))
        heat-camera-root (-> (JonSharedCmdHeatCamera$Root/newBuilder)
                             (.setShiftDde shift))]
    (.setHeatCamera root-msg heat-camera-root)
    (cmd-core/send-cmd-message root-msg))
  nil)

;; ============================================================================
;; FX Mode Commands
;; ============================================================================

(>defn set-fx-mode
  "Set FX mode"
  [mode]
  [::specs/heat-fx-mode => nil?]
  (let [root-msg (cmd-core/create-root-message)
        fx-mode (-> (JonSharedCmdHeatCamera$SetFxMode/newBuilder)
                    (.setMode mode))
        heat-camera-root (-> (JonSharedCmdHeatCamera$Root/newBuilder)
                             (.setSetFxMode fx-mode))]
    (.setHeatCamera root-msg heat-camera-root)
    (cmd-core/send-cmd-message root-msg))
  nil)

(>defn next-fx-mode
  "Switch to next FX mode"
  []
  [=> nil?]
  (let [root-msg (cmd-core/create-root-message)
        heat-camera-root (-> (JonSharedCmdHeatCamera$Root/newBuilder)
                             (.setNextFxMode (JonSharedCmdHeatCamera$NextFxMode/newBuilder)))]
    (.setHeatCamera root-msg heat-camera-root)
    (cmd-core/send-cmd-message root-msg))
  nil)

(>defn prev-fx-mode
  "Switch to previous FX mode"
  []
  [=> nil?]
  (let [root-msg (cmd-core/create-root-message)
        heat-camera-root (-> (JonSharedCmdHeatCamera$Root/newBuilder)
                             (.setPrevFxMode (JonSharedCmdHeatCamera$PrevFxMode/newBuilder)))]
    (.setHeatCamera root-msg heat-camera-root)
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
        clahe (-> (JonSharedCmdHeatCamera$SetClaheLevel/newBuilder)
                  (.setValue (float value)))
        heat-camera-root (-> (JonSharedCmdHeatCamera$Root/newBuilder)
                             (.setSetClaheLevel clahe))]
    (.setHeatCamera root-msg heat-camera-root)
    (cmd-core/send-cmd-message root-msg))
  nil)

(>defn shift-clahe-level
  "Shift CLAHE level by specified amount (-1.0 to 1.0)"
  [value]
  [::specs/offset-value => nil?]
  (let [root-msg (cmd-core/create-root-message)
        shift (-> (JonSharedCmdHeatCamera$ShiftClaheLevel/newBuilder)
                  (.setValue (float value)))
        heat-camera-root (-> (JonSharedCmdHeatCamera$Root/newBuilder)
                             (.setShiftClaheLevel shift))]
    (.setHeatCamera root-msg heat-camera-root)
    (cmd-core/send-cmd-message root-msg))
  nil)

;; ============================================================================
;; Meteo Data
;; ============================================================================

(>defn get-meteo
  "Request heat camera meteorological data (read-only safe)"
  []
  [=> nil?]
  (let [root-msg (cmd-core/create-root-message)
        heat-camera-root (-> (JonSharedCmdHeatCamera$Root/newBuilder)
                             (.setGetMeteo (JonSharedCmdHeatCamera$GetMeteo/newBuilder)))]
    (.setHeatCamera root-msg heat-camera-root)
    (cmd-core/send-cmd-message root-msg))
  nil)

;; ============================================================================
;; Utility Functions
;; ============================================================================

(>defn string->agc-mode
  "Convert string to AGC mode enum value"
  [value]
  [string? => (? ::specs/heat-agc-mode)]
  (case value
    "mode_1" JonSharedDataTypes$JonGuiDataVideoChannelHeatAGCModes/JON_GUI_DATA_VIDEO_CHANNEL_HEAT_AGC_MODE_1
    "mode_2" JonSharedDataTypes$JonGuiDataVideoChannelHeatAGCModes/JON_GUI_DATA_VIDEO_CHANNEL_HEAT_AGC_MODE_2
    "mode_3" JonSharedDataTypes$JonGuiDataVideoChannelHeatAGCModes/JON_GUI_DATA_VIDEO_CHANNEL_HEAT_AGC_MODE_3
    nil))

(>defn string->filter
  "Convert string to heat filter enum value"
  [value]
  [string? => (? ::specs/heat-filter)]
  (case value
    "hot_white" JonSharedDataTypes$JonGuiDataVideoChannelHeatFilters/JON_GUI_DATA_VIDEO_CHANNEL_HEAT_FILTER_HOT_WHITE
    "hot_black" JonSharedDataTypes$JonGuiDataVideoChannelHeatFilters/JON_GUI_DATA_VIDEO_CHANNEL_HEAT_FILTER_HOT_BLACK
    "sepia" JonSharedDataTypes$JonGuiDataVideoChannelHeatFilters/JON_GUI_DATA_VIDEO_CHANNEL_HEAT_FILTER_SEPIA
    "sepia_inverse" JonSharedDataTypes$JonGuiDataVideoChannelHeatFilters/JON_GUI_DATA_VIDEO_CHANNEL_HEAT_FILTER_SEPIA_INVERSE
    nil))

(>defn string->fx-mode
  "Convert string to FX mode enum value"
  [value]
  [string? => (? ::specs/heat-fx-mode)]
  (case value
    "default" JonSharedDataTypes$JonGuiDataFxModeHeat/JON_GUI_DATA_FX_MODE_HEAT_DEFAULT
    "a" JonSharedDataTypes$JonGuiDataFxModeHeat/JON_GUI_DATA_FX_MODE_HEAT_A
    "b" JonSharedDataTypes$JonGuiDataFxModeHeat/JON_GUI_DATA_FX_MODE_HEAT_B
    "c" JonSharedDataTypes$JonGuiDataFxModeHeat/JON_GUI_DATA_FX_MODE_HEAT_C
    "d" JonSharedDataTypes$JonGuiDataFxModeHeat/JON_GUI_DATA_FX_MODE_HEAT_D
    "e" JonSharedDataTypes$JonGuiDataFxModeHeat/JON_GUI_DATA_FX_MODE_HEAT_E
    "f" JonSharedDataTypes$JonGuiDataFxModeHeat/JON_GUI_DATA_FX_MODE_HEAT_F
    nil))

;; Convenience aliases
(>defn focus-auto
  "Enable auto focus (convenience alias)"
  []
  [=> nil?]
  (set-auto-focus true))

(>defn focus-manual
  "Disable auto focus for manual control"
  []
  [=> nil?]
  (set-auto-focus false))