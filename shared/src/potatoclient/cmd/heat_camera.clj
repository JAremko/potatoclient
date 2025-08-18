(ns potatoclient.cmd.heat-camera
  "Heat Camera (thermal imaging) command functions.
   Based on the HeatCamera message structure in jon_shared_cmd_heat_camera.proto."
  (:require
   [com.fulcrologic.guardrails.malli.core :refer [>defn >defn- => | ?]]
   [potatoclient.cmd.core :as core]))

;; ============================================================================
;; Photo Control
;; ============================================================================

(>defn take-photo
  "Take a photo with the heat camera.
   Returns a fully formed cmd root ready to send."
  []
  [=> :cmd/root]
  (core/create-command {:heat_camera {:photo {}}}))

;; ============================================================================
;; AGC (Automatic Gain Control) and Filter Settings
;; ============================================================================

(>defn set-agc
  "Set the AGC (Automatic Gain Control) mode.
   Mode must be one of the JonGuiDataVideoChannelHeatAGCModes enum values.
   Returns a fully formed cmd root ready to send."
  [mode]
  [:enum/heat-agc-mode => :cmd/root]
  (core/create-command 
    {:heat_camera {:set_agc {:value mode}}}))

(>defn set-filter
  "Set the heat camera filter (thermal visualization mode).
   Filter must be one of the JonGuiDataVideoChannelHeatFilters enum values.
   Returns a fully formed cmd root ready to send."
  [filter]
  [:enum/heat-filter => :cmd/root]
  (core/create-command 
    {:heat_camera {:set_filter {:value filter}}}))

;; ============================================================================
;; Camera Control
;; ============================================================================

(>defn calibrate
  "Calibrate the heat camera.
   Returns a fully formed cmd root ready to send."
  []
  [=> :cmd/root]
  (core/create-command {:heat_camera {:calibrate {}}}))

(>defn start
  "Start the heat camera.
   Returns a fully formed cmd root ready to send."
  []
  [=> :cmd/root]
  (core/create-command {:heat_camera {:start {}}}))

(>defn stop
  "Stop the heat camera.
   Returns a fully formed cmd root ready to send."
  []
  [=> :cmd/root]
  (core/create-command {:heat_camera {:stop {}}}))

(>defn set-calib-mode
  "Set calibration mode for the heat camera.
   Returns a fully formed cmd root ready to send."
  []
  [=> :cmd/root]
  (core/create-command {:heat_camera {:set_calib_mode {}}}))

;; ============================================================================
;; Zoom Control - Simple Commands
;; ============================================================================

(>defn zoom-in
  "Start zooming in.
   Returns a fully formed cmd root ready to send."
  []
  [=> :cmd/root]
  (core/create-command {:heat_camera {:zoom_in {}}}))

(>defn zoom-out
  "Start zooming out.
   Returns a fully formed cmd root ready to send."
  []
  [=> :cmd/root]
  (core/create-command {:heat_camera {:zoom_out {}}}))

(>defn zoom-stop
  "Stop zoom movement.
   Returns a fully formed cmd root ready to send."
  []
  [=> :cmd/root]
  (core/create-command {:heat_camera {:zoom_stop {}}}))

(>defn reset-zoom
  "Reset zoom to default position.
   Returns a fully formed cmd root ready to send."
  []
  [=> :cmd/root]
  (core/create-command {:heat_camera {:reset_zoom {}}}))

(>defn save-zoom-to-table
  "Save current zoom position to table.
   Returns a fully formed cmd root ready to send."
  []
  [=> :cmd/root]
  (core/create-command {:heat_camera {:save_to_table {}}}))

;; ============================================================================
;; Zoom Control - Table Operations (nested Zoom message)
;; ============================================================================

(>defn set-zoom-table-value
  "Set the zoom table value (positive integer).
   Returns a fully formed cmd root ready to send."
  [value]
  [:proto/int32-positive => :cmd/root]
  (core/create-command 
    {:heat_camera {:zoom {:set_zoom_table_value {:value value}}}}))

(>defn next-zoom-table-pos
  "Move to next zoom table position.
   Returns a fully formed cmd root ready to send."
  []
  [=> :cmd/root]
  (core/create-command 
    {:heat_camera {:zoom {:next_zoom_table_pos {}}}}))

(>defn prev-zoom-table-pos
  "Move to previous zoom table position.
   Returns a fully formed cmd root ready to send."
  []
  [=> :cmd/root]
  (core/create-command 
    {:heat_camera {:zoom {:prev_zoom_table_pos {}}}}))

;; ============================================================================
;; Digital Zoom
;; ============================================================================

(>defn set-digital-zoom-level
  "Set the digital zoom level (must be >= 1.0).
   Returns a fully formed cmd root ready to send."
  [value]
  [:range/digital-zoom => :cmd/root]
  (core/create-command 
    {:heat_camera {:set_digital_zoom_level {:value value}}}))

;; ============================================================================
;; Focus Control
;; ============================================================================

(>defn set-auto-focus
  "Enable or disable auto focus.
   Returns a fully formed cmd root ready to send."
  [enabled?]
  [:boolean => :cmd/root]
  (core/create-command 
    {:heat_camera {:set_auto_focus {:value enabled?}}}))

(>defn focus-stop
  "Stop focus movement.
   Returns a fully formed cmd root ready to send."
  []
  [=> :cmd/root]
  (core/create-command {:heat_camera {:focus_stop {}}}))

(>defn focus-in
  "Start focusing in (closer).
   Returns a fully formed cmd root ready to send."
  []
  [=> :cmd/root]
  (core/create-command {:heat_camera {:focus_in {}}}))

(>defn focus-out
  "Start focusing out (farther).
   Returns a fully formed cmd root ready to send."
  []
  [=> :cmd/root]
  (core/create-command {:heat_camera {:focus_out {}}}))

(>defn focus-step-plus
  "Step focus forward by one increment.
   Returns a fully formed cmd root ready to send."
  []
  [=> :cmd/root]
  (core/create-command {:heat_camera {:focus_step_plus {}}}))

(>defn focus-step-minus
  "Step focus backward by one increment.
   Returns a fully formed cmd root ready to send."
  []
  [=> :cmd/root]
  (core/create-command {:heat_camera {:focus_step_minus {}}}))

;; ============================================================================
;; Meteo Data
;; ============================================================================

(>defn get-meteo
  "Request meteorological data from heat camera.
   Returns a fully formed cmd root ready to send."
  []
  [=> :cmd/root]
  (core/create-command {:heat_camera {:get_meteo {}}}))

;; ============================================================================
;; DDE (Digital Detail Enhancement)
;; ============================================================================

(>defn enable-dde
  "Enable Digital Detail Enhancement.
   Returns a fully formed cmd root ready to send."
  []
  [=> :cmd/root]
  (core/create-command {:heat_camera {:enable_dde {}}}))

(>defn disable-dde
  "Disable Digital Detail Enhancement.
   Returns a fully formed cmd root ready to send."
  []
  [=> :cmd/root]
  (core/create-command {:heat_camera {:disable_dde {}}}))

(>defn set-dde-level
  "Set the DDE level (0 to 100).
   Returns a fully formed cmd root ready to send."
  [level]
  [[:int {:min 0 :max 100}] => :cmd/root]
  (core/create-command 
    {:heat_camera {:set_dde_level {:value level}}}))

(>defn shift-dde
  "Shift the DDE level by offset (-100 to 100).
   Returns a fully formed cmd root ready to send."
  [shift-value]
  [[:int {:min -100 :max 100}] => :cmd/root]
  (core/create-command 
    {:heat_camera {:shift_dde {:value shift-value}}}))

;; ============================================================================
;; FX Mode Control
;; ============================================================================

(>defn set-fx-mode
  "Set the FX mode for the heat camera.
   Mode must be one of the JonGuiDataFxModeHeat enum values.
   Returns a fully formed cmd root ready to send."
  [mode]
  [:enum/fx-mode-heat => :cmd/root]
  (core/create-command 
    {:heat_camera {:set_fx_mode {:mode mode}}}))

(>defn next-fx-mode
  "Switch to next FX mode.
   Returns a fully formed cmd root ready to send."
  []
  [=> :cmd/root]
  (core/create-command {:heat_camera {:next_fx_mode {}}}))

(>defn prev-fx-mode
  "Switch to previous FX mode.
   Returns a fully formed cmd root ready to send."
  []
  [=> :cmd/root]
  (core/create-command {:heat_camera {:prev_fx_mode {}}}))

(>defn refresh-fx-mode
  "Refresh current FX mode.
   Returns a fully formed cmd root ready to send."
  []
  [=> :cmd/root]
  (core/create-command {:heat_camera {:refresh_fx_mode {}}}))

;; ============================================================================
;; CLAHE (Contrast Limited Adaptive Histogram Equalization)
;; ============================================================================

(>defn set-clahe-level
  "Set the CLAHE level (0.0 to 1.0).
   Returns a fully formed cmd root ready to send."
  [value]
  [:range/normalized => :cmd/root]
  (core/create-command 
    {:heat_camera {:set_clahe_level {:value value}}}))

(>defn shift-clahe-level
  "Shift the CLAHE level by offset (-1.0 to 1.0).
   Returns a fully formed cmd root ready to send."
  [shift-value]
  [:range/normalized-offset => :cmd/root]
  (core/create-command 
    {:heat_camera {:shift_clahe_level {:value shift-value}}}))