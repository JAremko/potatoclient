(ns potatoclient.cmd.heat-camera
  "Heat Camera (thermal imaging) command functions.
   Based on the HeatCamera message structure in jon_shared_cmd_heat_camera.proto."
  (:require
    [malli.core :as m]
    [potatoclient.cmd.core :as core]))

;; ============================================================================
;; Photo Control
;; ============================================================================

(defn take-photo
  "Take a photo with the heat camera.
   Returns a fully formed cmd root ready to send."
  []
  (core/create-command {:heat_camera {:photo {}}}))
(m/=> take-photo [:=> [:cat] :cmd/root])

;; ============================================================================
;; AGC (Automatic Gain Control) and Filter Settings
;; ============================================================================

(defn set-agc
  "Set the AGC (Automatic Gain Control) mode.
   Mode must be one of the JonGuiDataVideoChannelHeatAGCModes enum values.
   Returns a fully formed cmd root ready to send."
  [mode]
  (core/create-command
    {:heat_camera {:set_agc {:value mode}}}))
(m/=> set-agc [:=> [:cat :enum/heat-agc-mode] :cmd/root])

(defn set-filter
  "Set the heat camera filter (thermal visualization mode).
   Filter must be one of the JonGuiDataVideoChannelHeatFilters enum values.
   Returns a fully formed cmd root ready to send."
  [filter]
  (core/create-command
    {:heat_camera {:set_filter {:value filter}}}))
(m/=> set-filter [:=> [:cat :enum/heat-filter] :cmd/root])

;; ============================================================================
;; Camera Control
;; ============================================================================

(defn calibrate
  "Calibrate the heat camera.
   Returns a fully formed cmd root ready to send."
  []
  (core/create-command {:heat_camera {:calibrate {}}}))
(m/=> calibrate [:=> [:cat] :cmd/root])

(defn start
  "Start the heat camera.
   Returns a fully formed cmd root ready to send."
  []
  (core/create-command {:heat_camera {:start {}}}))
(m/=> start [:=> [:cat] :cmd/root])

(defn stop
  "Stop the heat camera.
   Returns a fully formed cmd root ready to send."
  []
  (core/create-command {:heat_camera {:stop {}}}))
(m/=> stop [:=> [:cat] :cmd/root])

(defn set-calib-mode
  "Set calibration mode for the heat camera.
   Returns a fully formed cmd root ready to send."
  []
  (core/create-command {:heat_camera {:set_calib_mode {}}}))
(m/=> set-calib-mode [:=> [:cat] :cmd/root])

;; ============================================================================
;; Zoom Control - Simple Commands
;; ============================================================================

(defn zoom-in
  "Start zooming in.
   Returns a fully formed cmd root ready to send."
  []
  (core/create-command {:heat_camera {:zoom_in {}}}))
(m/=> zoom-in [:=> [:cat] :cmd/root])

(defn zoom-out
  "Start zooming out.
   Returns a fully formed cmd root ready to send."
  []
  (core/create-command {:heat_camera {:zoom_out {}}}))
(m/=> zoom-out [:=> [:cat] :cmd/root])

(defn zoom-stop
  "Stop zoom movement.
   Returns a fully formed cmd root ready to send."
  []
  (core/create-command {:heat_camera {:zoom_stop {}}}))
(m/=> zoom-stop [:=> [:cat] :cmd/root])

(defn reset-zoom
  "Reset zoom to default position.
   Returns a fully formed cmd root ready to send."
  []
  (core/create-command {:heat_camera {:reset_zoom {}}}))
(m/=> reset-zoom [:=> [:cat] :cmd/root])

(defn save-zoom-to-table
  "Save current zoom position to table.
   Returns a fully formed cmd root ready to send."
  []
  (core/create-command {:heat_camera {:save_to_table {}}}))
(m/=> save-zoom-to-table [:=> [:cat] :cmd/root])

;; ============================================================================
;; Zoom Control - Table Operations (nested Zoom message)
;; ============================================================================

(defn set-zoom-table-value
  "Set the zoom table value (positive integer).
   Returns a fully formed cmd root ready to send."
  [value]
  (core/create-command
    {:heat_camera {:zoom {:set_zoom_table_value {:value value}}}}))
(m/=> set-zoom-table-value [:=> [:cat :proto/int32-positive] :cmd/root])

(defn next-zoom-table-pos
  "Move to next zoom table position.
   Returns a fully formed cmd root ready to send."
  []
  (core/create-command
    {:heat_camera {:zoom {:next_zoom_table_pos {}}}}))
(m/=> next-zoom-table-pos [:=> [:cat] :cmd/root])

(defn prev-zoom-table-pos
  "Move to previous zoom table position.
   Returns a fully formed cmd root ready to send."
  []
  (core/create-command
    {:heat_camera {:zoom {:prev_zoom_table_pos {}}}}))
(m/=> prev-zoom-table-pos [:=> [:cat] :cmd/root])

;; ============================================================================
;; Digital Zoom
;; ============================================================================

(defn set-digital-zoom-level
  "Set the digital zoom level (must be >= 1.0).
   Returns a fully formed cmd root ready to send."
  [value]
  (core/create-command
    {:heat_camera {:set_digital_zoom_level {:value value}}}))
(m/=> set-digital-zoom-level [:=> [:cat :range/digital-zoom] :cmd/root])

;; ============================================================================
;; Focus Control
;; ============================================================================

(defn set-auto-focus
  "Enable or disable auto focus.
   Returns a fully formed cmd root ready to send."
  [enabled?]
  (core/create-command
    {:heat_camera {:set_auto_focus {:value enabled?}}}))
(m/=> set-auto-focus [:=> [:cat :boolean] :cmd/root])

(defn focus-stop
  "Stop focus movement.
   Returns a fully formed cmd root ready to send."
  []
  (core/create-command {:heat_camera {:focus_stop {}}}))
(m/=> focus-stop [:=> [:cat] :cmd/root])

(defn focus-in
  "Start focusing in (closer).
   Returns a fully formed cmd root ready to send."
  []
  (core/create-command {:heat_camera {:focus_in {}}}))
(m/=> focus-in [:=> [:cat] :cmd/root])

(defn focus-out
  "Start focusing out (farther).
   Returns a fully formed cmd root ready to send."
  []
  (core/create-command {:heat_camera {:focus_out {}}}))
(m/=> focus-out [:=> [:cat] :cmd/root])

(defn focus-step-plus
  "Step focus forward by one increment.
   Returns a fully formed cmd root ready to send."
  []
  (core/create-command {:heat_camera {:focus_step_plus {}}}))
(m/=> focus-step-plus [:=> [:cat] :cmd/root])

(defn focus-step-minus
  "Step focus backward by one increment.
   Returns a fully formed cmd root ready to send."
  []
  (core/create-command {:heat_camera {:focus_step_minus {}}}))
(m/=> focus-step-minus [:=> [:cat] :cmd/root])

;; ============================================================================
;; Meteo Data
;; ============================================================================

(defn get-meteo
  "Request meteorological data from heat camera.
   Returns a fully formed cmd root ready to send."
  []
  (core/create-command {:heat_camera {:get_meteo {}}}))
(m/=> get-meteo [:=> [:cat] :cmd/root])

;; ============================================================================
;; DDE (Digital Detail Enhancement)
;; ============================================================================

(defn enable-dde
  "Enable Digital Detail Enhancement.
   Returns a fully formed cmd root ready to send."
  []
  (core/create-command {:heat_camera {:enable_dde {}}}))
(m/=> enable-dde [:=> [:cat] :cmd/root])

(defn disable-dde
  "Disable Digital Detail Enhancement.
   Returns a fully formed cmd root ready to send."
  []
  (core/create-command {:heat_camera {:disable_dde {}}}))
(m/=> disable-dde [:=> [:cat] :cmd/root])

(defn set-dde-level
  "Set the DDE level (0 to 100).
   Returns a fully formed cmd root ready to send."
  [level]
  (core/create-command
    {:heat_camera {:set_dde_level {:value level}}}))
(m/=> set-dde-level [:=> [:cat [:int {:min 0, :max 100}]] :cmd/root])

(defn shift-dde
  "Shift the DDE level by offset (-100 to 100).
   Returns a fully formed cmd root ready to send."
  [shift-value]
  (core/create-command
    {:heat_camera {:shift_dde {:value shift-value}}}))
(m/=> shift-dde [:=> [:cat [:int {:min -100, :max 100}]] :cmd/root])

;; ============================================================================
;; FX Mode Control
;; ============================================================================

(defn set-fx-mode
  "Set the FX mode for the heat camera.
   Mode must be one of the JonGuiDataFxModeHeat enum values.
   Returns a fully formed cmd root ready to send."
  [mode]
  (core/create-command
    {:heat_camera {:set_fx_mode {:mode mode}}}))
(m/=> set-fx-mode [:=> [:cat :enum/fx-mode-heat] :cmd/root])

(defn next-fx-mode
  "Switch to next FX mode.
   Returns a fully formed cmd root ready to send."
  []
  (core/create-command {:heat_camera {:next_fx_mode {}}}))
(m/=> next-fx-mode [:=> [:cat] :cmd/root])

(defn prev-fx-mode
  "Switch to previous FX mode.
   Returns a fully formed cmd root ready to send."
  []
  (core/create-command {:heat_camera {:prev_fx_mode {}}}))
(m/=> prev-fx-mode [:=> [:cat] :cmd/root])

(defn refresh-fx-mode
  "Refresh current FX mode.
   Returns a fully formed cmd root ready to send."
  []
  (core/create-command {:heat_camera {:refresh_fx_mode {}}}))
(m/=> refresh-fx-mode [:=> [:cat] :cmd/root])

;; ============================================================================
;; CLAHE (Contrast Limited Adaptive Histogram Equalization)
;; ============================================================================

(defn set-clahe-level
  "Set the CLAHE level (0.0 to 1.0).
   Returns a fully formed cmd root ready to send."
  [value]
  (core/create-command
    {:heat_camera {:set_clahe_level {:value value}}}))
(m/=> set-clahe-level [:=> [:cat :range/normalized] :cmd/root])

(defn shift-clahe-level
  "Shift the CLAHE level by offset (-1.0 to 1.0).
   Returns a fully formed cmd root ready to send."
  [shift-value]
  (core/create-command
    {:heat_camera {:shift_clahe_level {:value shift-value}}}))
(m/=> shift-clahe-level [:=> [:cat :range/normalized-offset] :cmd/root])

;; ============================================================================
;; ROI (Region of Interest) Control
;; ============================================================================

(defn focus-roi
  "Set focus to a region of interest.
   Takes rectangle coordinates (x1, y1, x2, y2) and frame time.
   Note: While the TypeScript client calculates center point, the protobuf
   expects the full rectangle. The server will handle the center calculation.
   Frame time should be the timestamp of the frame being focused.
   Returns a fully formed cmd root ready to send."
  [x1 y1 x2 y2 frame-time]
  (core/create-command
    {:heat_camera {:focus_roi {:x1 x1 :y1 y1 :x2 x2 :y2 y2 :frame_time frame-time}}}))
(m/=> focus-roi [:=> [:cat :proto/double :proto/double :proto/double :proto/double :time/frame-time] :cmd/root])

(defn track-roi
  "Start tracking a region of interest.
   Takes rectangle coordinates (x1, y1, x2, y2) and frame time.
   Frame time should be the timestamp of the frame being tracked.
   Returns a fully formed cmd root ready to send."
  [x1 y1 x2 y2 frame-time]
  (core/create-command
    {:heat_camera {:track_roi {:x1 x1 :y1 y1 :x2 x2 :y2 y2 :frame_time frame-time}}}))
(m/=> track-roi [:=> [:cat :proto/double :proto/double :proto/double :proto/double :time/frame-time] :cmd/root])

(defn zoom-roi
  "Zoom to a region of interest.
   Takes rectangle coordinates (x1, y1, x2, y2) and frame time.
   Frame time should be the timestamp of the frame being zoomed.
   Returns a fully formed cmd root ready to send."
  [x1 y1 x2 y2 frame-time]
  (core/create-command
    {:heat_camera {:zoom_roi {:x1 x1 :y1 y1 :x2 x2 :y2 y2 :frame_time frame-time}}}))
(m/=> zoom-roi [:=> [:cat :proto/double :proto/double :proto/double :proto/double :time/frame-time] :cmd/root])
