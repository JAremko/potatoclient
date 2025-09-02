(ns potatoclient.cmd.day-camera
  "Day Camera command functions.
   Based on the DayCamera message structure in jon_shared_cmd_day_camera.proto."
  (:require
    [potatoclient.cmd.core :as core]))

;; ============================================================================
;; Infra-Red Filter Control
;; ============================================================================

(defn set-infra-red-filter
  "Set the infra-red filter on/off.
   Returns a fully formed cmd root ready to send."
  {:malli/schema [:=> [:cat :boolean] :cmd/root]}
  [enabled?]
  (core/create-command
    {:day_camera {:set_infra_red_filter {:value enabled?}}}))

;; ============================================================================
;; Iris Control
;; ============================================================================

(defn set-iris
  "Set the iris value (0.0 to 1.0).
   Returns a fully formed cmd root ready to send."
  {:malli/schema [:=> [:cat :range/normalized] :cmd/root]}
  [value]
  (core/create-command
    {:day_camera {:set_iris {:value value}}}))

(defn set-auto-iris
  "Enable or disable auto iris.
   Returns a fully formed cmd root ready to send."
  {:malli/schema [:=> [:cat :boolean] :cmd/root]}
  [enabled?]
  (core/create-command
    {:day_camera {:set_auto_iris {:value enabled?}}}))

;; ============================================================================
;; Photo Control
;; ============================================================================

(defn take-photo
  "Take a photo with the day camera.
   Returns a fully formed cmd root ready to send."
  {:malli/schema [:=> [:cat] :cmd/root]}
  []
  (core/create-command {:day_camera {:photo {}}}))

;; ============================================================================
;; Camera Control
;; ============================================================================

(defn start
  "Start the day camera.
   Returns a fully formed cmd root ready to send."
  {:malli/schema [:=> [:cat] :cmd/root]}
  []
  (core/create-command {:day_camera {:start {}}}))

(defn stop
  "Stop the day camera.
   Returns a fully formed cmd root ready to send."
  {:malli/schema [:=> [:cat] :cmd/root]}
  []
  (core/create-command {:day_camera {:stop {}}}))

(defn halt-all
  "Halt all day camera operations.
   Returns a fully formed cmd root ready to send."
  {:malli/schema [:=> [:cat] :cmd/root]}
  []
  (core/create-command {:day_camera {:halt_all {}}}))

;; ============================================================================
;; Focus Control
;; ============================================================================

(defn set-focus
  "Set the focus value (0.0 to 1.0).
   Returns a fully formed cmd root ready to send."
  {:malli/schema [:=> [:cat :range/focus] :cmd/root]}
  [value]
  (core/create-command
    {:day_camera {:focus {:set_value {:value value}}}}))

(defn move-focus
  "Move focus to target value at specified speed.
   Both target-value and speed are 0.0 to 1.0.
   Returns a fully formed cmd root ready to send."
  {:malli/schema [:=> [:cat :range/normalized :speed/normalized] :cmd/root]}
  [target-value speed]
  (core/create-command
    {:day_camera {:focus {:move {:target_value target-value
                                 :speed speed}}}}))

(defn halt-focus
  "Halt focus movement.
   Returns a fully formed cmd root ready to send."
  {:malli/schema [:=> [:cat] :cmd/root]}
  []
  (core/create-command
    {:day_camera {:focus {:halt {}}}}))

(defn offset-focus
  "Offset focus by a value (-1.0 to 1.0).
   Returns a fully formed cmd root ready to send."
  {:malli/schema [:=> [:cat :range/normalized-offset] :cmd/root]}
  [offset-value]
  (core/create-command
    {:day_camera {:focus {:offset {:offset_value offset-value}}}}))

(defn reset-focus
  "Reset focus to default position.
   Returns a fully formed cmd root ready to send."
  {:malli/schema [:=> [:cat] :cmd/root]}
  []
  (core/create-command
    {:day_camera {:focus {:reset_focus {}}}}))

(defn save-focus-to-table
  "Save current focus position to table.
   Returns a fully formed cmd root ready to send."
  {:malli/schema [:=> [:cat] :cmd/root]}
  []
  (core/create-command
    {:day_camera {:focus {:save_to_table_focus {}}}}))

;; ============================================================================
;; Zoom Control
;; ============================================================================

(defn set-zoom
  "Set the zoom value (0.0 to 1.0).
   Returns a fully formed cmd root ready to send."
  {:malli/schema [:=> [:cat :range/zoom] :cmd/root]}
  [value]
  (core/create-command
    {:day_camera {:zoom {:set_value {:value value}}}}))

(defn move-zoom
  "Move zoom to target value at specified speed.
   Both target-value and speed are 0.0 to 1.0.
   Returns a fully formed cmd root ready to send."
  {:malli/schema [:=> [:cat :range/normalized :speed/normalized] :cmd/root]}
  [target-value speed]
  (core/create-command
    {:day_camera {:zoom {:move {:target_value target-value
                                :speed speed}}}}))

(defn halt-zoom
  "Halt zoom movement.
   Returns a fully formed cmd root ready to send."
  {:malli/schema [:=> [:cat] :cmd/root]}
  []
  (core/create-command
    {:day_camera {:zoom {:halt {}}}}))

(defn offset-zoom
  "Offset zoom by a value (-1.0 to 1.0).
   Returns a fully formed cmd root ready to send."
  {:malli/schema [:=> [:cat :range/normalized-offset] :cmd/root]}
  [offset-value]
  (core/create-command
    {:day_camera {:zoom {:offset {:offset_value offset-value}}}}))

(defn reset-zoom
  "Reset zoom to default position.
   Returns a fully formed cmd root ready to send."
  {:malli/schema [:=> [:cat] :cmd/root]}
  []
  (core/create-command
    {:day_camera {:zoom {:reset_zoom {}}}}))

(defn save-zoom-to-table
  "Save current zoom position to table.
   Returns a fully formed cmd root ready to send."
  {:malli/schema [:=> [:cat] :cmd/root]}
  []
  (core/create-command
    {:day_camera {:zoom {:save_to_table {}}}}))

(defn set-zoom-table-value
  "Set the zoom table value (positive integer).
   Returns a fully formed cmd root ready to send."
  {:malli/schema [:=> [:cat :proto/int32-positive] :cmd/root]}
  [value]
  (core/create-command
    {:day_camera {:zoom {:set_zoom_table_value {:value value}}}}))

(defn next-zoom-table-pos
  "Move to next zoom table position.
   Returns a fully formed cmd root ready to send."
  {:malli/schema [:=> [:cat] :cmd/root]}
  []
  (core/create-command
    {:day_camera {:zoom {:next_zoom_table_pos {}}}}))

(defn prev-zoom-table-pos
  "Move to previous zoom table position.
   Returns a fully formed cmd root ready to send."
  {:malli/schema [:=> [:cat] :cmd/root]}
  []
  (core/create-command
    {:day_camera {:zoom {:prev_zoom_table_pos {}}}}))

;; ============================================================================
;; Digital Zoom
;; ============================================================================

(defn set-digital-zoom-level
  "Set the digital zoom level (must be >= 1.0).
   Returns a fully formed cmd root ready to send."
  {:malli/schema [:=> [:cat :range/digital-zoom] :cmd/root]}
  [value]
  (core/create-command
    {:day_camera {:set_digital_zoom_level {:value value}}}))

;; ============================================================================
;; Meteo Data
;; ============================================================================

(defn get-meteo
  "Request meteorological data from day camera.
   Returns a fully formed cmd root ready to send."
  {:malli/schema [:=> [:cat] :cmd/root]}
  []
  (core/create-command {:day_camera {:get_meteo {}}}))

;; ============================================================================
;; FX Mode Control
;; ============================================================================

(defn set-fx-mode
  "Set the FX mode for the day camera.
   Mode must be one of the JonGuiDataFxModeDay enum values.
   Returns a fully formed cmd root ready to send."
  {:malli/schema [:=> [:cat :enum/fx-mode-day] :cmd/root]}
  [mode]
  (core/create-command
    {:day_camera {:set_fx_mode {:mode mode}}}))

(defn next-fx-mode
  "Switch to next FX mode.
   Returns a fully formed cmd root ready to send."
  {:malli/schema [:=> [:cat] :cmd/root]}
  []
  (core/create-command {:day_camera {:next_fx_mode {}}}))

(defn prev-fx-mode
  "Switch to previous FX mode.
   Returns a fully formed cmd root ready to send."
  {:malli/schema [:=> [:cat] :cmd/root]}
  []
  (core/create-command {:day_camera {:prev_fx_mode {}}}))

(defn refresh-fx-mode
  "Refresh current FX mode.
   Returns a fully formed cmd root ready to send."
  {:malli/schema [:=> [:cat] :cmd/root]}
  []
  (core/create-command {:day_camera {:refresh_fx_mode {}}}))

;; ============================================================================
;; CLAHE (Contrast Limited Adaptive Histogram Equalization)
;; ============================================================================

(defn set-clahe-level
  "Set the CLAHE level (0.0 to 1.0).
   Returns a fully formed cmd root ready to send."
  {:malli/schema [:=> [:cat :range/normalized] :cmd/root]}
  [value]
  (core/create-command
    {:day_camera {:set_clahe_level {:value value}}}))

(defn shift-clahe-level
  "Shift the CLAHE level by offset (-1.0 to 1.0).
   Returns a fully formed cmd root ready to send."
  {:malli/schema [:=> [:cat :range/normalized-offset] :cmd/root]}
  [shift-value]
  (core/create-command
    {:day_camera {:shift_clahe_level {:value shift-value}}}))