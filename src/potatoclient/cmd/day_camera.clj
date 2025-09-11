(ns potatoclient.cmd.day-camera
  "Day Camera command functions.
   Based on the DayCamera message structure in jon_shared_cmd_day_camera.proto."
  (:require
    [malli.core :as m]
    [potatoclient.cmd.core :as core]
    [potatoclient.state :as state]))

;; ============================================================================
;; Infra-Red Filter Control
;; ============================================================================

(defn set-infra-red-filter
  "Set the infra-red filter on/off.
   Returns a fully formed cmd root ready to send."
  [enabled?]
  (core/create-command
    {:day_camera {:set_infra_red_filter {:value enabled?}}}))
(m/=> set-infra-red-filter [:=> [:cat :boolean] :cmd/root])

;; ============================================================================
;; Iris Control
;; ============================================================================

(defn set-iris
  "Set the iris value (0.0 to 1.0).
   Returns a fully formed cmd root ready to send."
  [value]
  (core/create-command
    {:day_camera {:set_iris {:value value}}}))
(m/=> set-iris [:=> [:cat :range/normalized] :cmd/root])

(defn set-auto-iris
  "Enable or disable auto iris.
   Returns a fully formed cmd root ready to send."
  [enabled?]
  (core/create-command
    {:day_camera {:set_auto_iris {:value enabled?}}}))
(m/=> set-auto-iris [:=> [:cat :boolean] :cmd/root])

;; ============================================================================
;; Photo Control
;; ============================================================================

(defn take-photo
  "Take a photo with the day camera.
   Returns a fully formed cmd root ready to send."
  []
  (core/create-command {:day_camera {:photo {}}}))
(m/=> take-photo [:=> [:cat] :cmd/root])

;; ============================================================================
;; Camera Control
;; ============================================================================

(defn start
  "Start the day camera.
   Returns a fully formed cmd root ready to send."
  []
  (core/create-command {:day_camera {:start {}}}))
(m/=> start [:=> [:cat] :cmd/root])

(defn stop
  "Stop the day camera.
   Returns a fully formed cmd root ready to send."
  []
  (core/create-command {:day_camera {:stop {}}}))
(m/=> stop [:=> [:cat] :cmd/root])

(defn halt-all
  "Halt all day camera operations.
   Returns a fully formed cmd root ready to send."
  []
  (core/create-command {:day_camera {:halt_all {}}}))
(m/=> halt-all [:=> [:cat] :cmd/root])

;; ============================================================================
;; Focus Control
;; ============================================================================

(defn set-focus
  "Set the focus value (0.0 to 1.0).
   Returns a fully formed cmd root ready to send."
  [value]
  (core/create-command
    {:day_camera {:focus {:set_value {:value value}}}}))
(m/=> set-focus [:=> [:cat :range/focus] :cmd/root])

(defn move-focus
  "Move focus to target value at specified speed.
   Both target-value and speed are 0.0 to 1.0.
   Returns a fully formed cmd root ready to send."
  [target-value speed]
  (core/create-command
    {:day_camera {:focus {:move {:target_value target-value
                                 :speed speed}}}}))
(m/=> move-focus [:=> [:cat :range/normalized :speed/normalized] :cmd/root])

(defn halt-focus
  "Halt focus movement.
   Returns a fully formed cmd root ready to send."
  []
  (core/create-command
    {:day_camera {:focus {:halt {}}}}))
(m/=> halt-focus [:=> [:cat] :cmd/root])

(defn offset-focus
  "Offset focus by a value (-1.0 to 1.0).
   Returns a fully formed cmd root ready to send."
  [offset-value]
  (core/create-command
    {:day_camera {:focus {:offset {:offset_value offset-value}}}}))
(m/=> offset-focus [:=> [:cat :range/normalized-offset] :cmd/root])

(defn reset-focus
  "Reset focus to default position.
   Returns a fully formed cmd root ready to send."
  []
  (core/create-command
    {:day_camera {:focus {:reset_focus {}}}}))
(m/=> reset-focus [:=> [:cat] :cmd/root])

(defn save-focus-to-table
  "Save current focus position to table.
   Returns a fully formed cmd root ready to send."
  []
  (core/create-command
    {:day_camera {:focus {:save_to_table_focus {}}}}))
(m/=> save-focus-to-table [:=> [:cat] :cmd/root])

;; ============================================================================
;; Zoom Control
;; ============================================================================

(defn set-zoom
  "Set the zoom value (0.0 to 1.0).
   Returns a fully formed cmd root ready to send."
  [value]
  (core/create-command
    {:day_camera {:zoom {:set_value {:value value}}}}))
(m/=> set-zoom [:=> [:cat :range/zoom] :cmd/root])

(defn move-zoom
  "Move zoom to target value at specified speed.
   Both target-value and speed are 0.0 to 1.0.
   Returns a fully formed cmd root ready to send."
  [target-value speed]
  (core/create-command
    {:day_camera {:zoom {:move {:target_value target-value
                                :speed speed}}}}))
(m/=> move-zoom [:=> [:cat :range/normalized :speed/normalized] :cmd/root])

(defn halt-zoom
  "Halt zoom movement.
   Returns a fully formed cmd root ready to send."
  []
  (core/create-command
    {:day_camera {:zoom {:halt {}}}}))
(m/=> halt-zoom [:=> [:cat] :cmd/root])

(defn offset-zoom
  "Offset zoom by a value (-1.0 to 1.0).
   Returns a fully formed cmd root ready to send."
  [offset-value]
  (core/create-command
    {:day_camera {:zoom {:offset {:offset_value offset-value}}}}))
(m/=> offset-zoom [:=> [:cat :range/normalized-offset] :cmd/root])

(defn reset-zoom
  "Reset zoom to default position.
   Returns a fully formed cmd root ready to send."
  []
  (core/create-command
    {:day_camera {:zoom {:reset_zoom {}}}}))
(m/=> reset-zoom [:=> [:cat] :cmd/root])

(defn save-zoom-to-table
  "Save current zoom position to table.
   Returns a fully formed cmd root ready to send."
  []
  (core/create-command
    {:day_camera {:zoom {:save_to_table {}}}}))
(m/=> save-zoom-to-table [:=> [:cat] :cmd/root])

(defn set-zoom-table-value
  "Set the zoom table value (positive integer).
   Returns a fully formed cmd root ready to send."
  [value]
  (core/create-command
    {:day_camera {:zoom {:set_zoom_table_value {:value value}}}}))
(m/=> set-zoom-table-value [:=> [:cat :proto/int32-positive] :cmd/root])

(defn next-zoom-table-pos
  "Move to next zoom table position.
   Returns a fully formed cmd root ready to send."
  []
  (core/create-command
    {:day_camera {:zoom {:next_zoom_table_pos {}}}}))
(m/=> next-zoom-table-pos [:=> [:cat] :cmd/root])

(defn prev-zoom-table-pos
  "Move to previous zoom table position.
   Returns a fully formed cmd root ready to send."
  []
  (core/create-command
    {:day_camera {:zoom {:prev_zoom_table_pos {}}}}))
(m/=> prev-zoom-table-pos [:=> [:cat] :cmd/root])

;; ============================================================================
;; Digital Zoom
;; ============================================================================

(defn set-digital-zoom-level
  "Set the digital zoom level (must be >= 1.0).
   Returns a fully formed cmd root ready to send."
  [value]
  (core/create-command
    {:day_camera {:set_digital_zoom_level {:value value}}}))
(m/=> set-digital-zoom-level [:=> [:cat :range/digital-zoom] :cmd/root])

;; ============================================================================
;; Meteo Data
;; ============================================================================

(defn get-meteo
  "Request meteorological data from day camera.
   Returns a fully formed cmd root ready to send."
  []
  (core/create-command {:day_camera {:get_meteo {}}}))
(m/=> get-meteo [:=> [:cat] :cmd/root])

;; ============================================================================
;; FX Mode Control
;; ============================================================================

(defn set-fx-mode
  "Set the FX mode for the day camera.
   Mode must be one of the JonGuiDataFxModeDay enum values.
   Returns a fully formed cmd root ready to send."
  [mode]
  (core/create-command
    {:day_camera {:set_fx_mode {:mode mode}}}))
(m/=> set-fx-mode [:=> [:cat :enum/fx-mode-day] :cmd/root])

(defn next-fx-mode
  "Switch to next FX mode.
   Returns a fully formed cmd root ready to send."
  []
  (core/create-command {:day_camera {:next_fx_mode {}}}))
(m/=> next-fx-mode [:=> [:cat] :cmd/root])

(defn prev-fx-mode
  "Switch to previous FX mode.
   Returns a fully formed cmd root ready to send."
  []
  (core/create-command {:day_camera {:prev_fx_mode {}}}))
(m/=> prev-fx-mode [:=> [:cat] :cmd/root])

(defn refresh-fx-mode
  "Refresh current FX mode.
   Returns a fully formed cmd root ready to send."
  []
  (core/create-command {:day_camera {:refresh_fx_mode {}}}))
(m/=> refresh-fx-mode [:=> [:cat] :cmd/root])

;; ============================================================================
;; CLAHE (Contrast Limited Adaptive Histogram Equalization)
;; ============================================================================

(defn set-clahe-level
  "Set the CLAHE level (0.0 to 1.0).
   Returns a fully formed cmd root ready to send."
  [value]
  (core/create-command
    {:day_camera {:set_clahe_level {:value value}}}))
(m/=> set-clahe-level [:=> [:cat :range/normalized] :cmd/root])

(defn shift-clahe-level
  "Shift the CLAHE level by offset (-1.0 to 1.0).
   Returns a fully formed cmd root ready to send."
  [shift-value]
  (core/create-command
    {:day_camera {:shift_clahe_level {:value shift-value}}}))
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
   State time is automatically obtained from the current server state.
   Returns a fully formed cmd root ready to send."
  [x1 y1 x2 y2 frame-time]
  (let [state-time (or (get-in @state/app-state [:server-state :system_monotonic_time_us]) 0)]
    (core/create-command
      {:day_camera {:focus_roi {:x1 x1 :y1 y1 :x2 x2 :y2 y2 :frame_time frame-time :state_time state-time}}})))
(m/=> focus-roi [:=> [:cat :proto/double :proto/double :proto/double :proto/double :time/frame-time] :cmd/root])

(defn track-roi
  "Start tracking a region of interest.
   Takes rectangle coordinates (x1, y1, x2, y2) and frame time.
   Frame time should be the timestamp of the frame being tracked.
   State time is automatically obtained from the current server state.
   Returns a fully formed cmd root ready to send."
  [x1 y1 x2 y2 frame-time]
  (let [state-time (or (get-in @state/app-state [:server-state :system_monotonic_time_us]) 0)]
    (core/create-command
      {:day_camera {:track_roi {:x1 x1 :y1 y1 :x2 x2 :y2 y2 :frame_time frame-time :state_time state-time}}})))
(m/=> track-roi [:=> [:cat :proto/double :proto/double :proto/double :proto/double :time/frame-time] :cmd/root])

(defn zoom-roi
  "Zoom to a region of interest.
   Takes rectangle coordinates (x1, y1, x2, y2) and frame time.
   Frame time should be the timestamp of the frame being zoomed.
   State time is automatically obtained from the current server state.
   Returns a fully formed cmd root ready to send."
  [x1 y1 x2 y2 frame-time]
  (let [state-time (or (get-in @state/app-state [:server-state :system_monotonic_time_us]) 0)]
    (core/create-command
      {:day_camera {:zoom_roi {:x1 x1 :y1 y1 :x2 x2 :y2 y2 :frame_time frame-time :state_time state-time}}})))
(m/=> zoom-roi [:=> [:cat :proto/double :proto/double :proto/double :proto/double :time/frame-time] :cmd/root])
