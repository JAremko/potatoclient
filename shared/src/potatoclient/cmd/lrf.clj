(ns potatoclient.cmd.lrf
  "LRF (Laser Range Finder) command functions for controlling laser measurement operations.
   Based on the Lrf message structure in jon_shared_cmd_lrf.proto."
  (:require
   [com.fulcrologic.guardrails.malli.core :refer [>defn >defn- => | ?]]
   [potatoclient.cmd.core :as core]))

;; ============================================================================
;; Measurement Operations
;; ============================================================================

(>defn measure
  "Trigger a single distance measurement.
   Returns a fully formed cmd root ready to send."
  []
  [=> :cmd/root]
  (core/create-command {:lrf {:measure {}}}))

;; ============================================================================
;; Scan Operations
;; ============================================================================

(>defn scan-on
  "Enable continuous scanning mode.
   Returns a fully formed cmd root ready to send."
  []
  [=> :cmd/root]
  (core/create-command {:lrf {:scan_on {}}}))

(>defn scan-off
  "Disable continuous scanning mode.
   Returns a fully formed cmd root ready to send."
  []
  [=> :cmd/root]
  (core/create-command {:lrf {:scan_off {}}}))

(>defn set-scan-mode
  "Set the LRF scan mode.
   Mode must be one of the JonGuiDataLrfScanMode enum values:
   - :JON_GUI_DATA_LRF_SCAN_MODE_1_HZ_CONTINUOUS
   - :JON_GUI_DATA_LRF_SCAN_MODE_4_HZ_CONTINUOUS
   - :JON_GUI_DATA_LRF_SCAN_MODE_10_HZ_CONTINUOUS
   - :JON_GUI_DATA_LRF_SCAN_MODE_20_HZ_CONTINUOUS
   - :JON_GUI_DATA_LRF_SCAN_MODE_100_HZ_CONTINUOUS
   - :JON_GUI_DATA_LRF_SCAN_MODE_200_HZ_CONTINUOUS
   Returns a fully formed cmd root ready to send."
  [mode]
  [:enum/lrf-scan-modes => :cmd/root]
  (core/create-command {:lrf {:set_scan_mode {:mode mode}}}))

;; ============================================================================
;; Device Control
;; ============================================================================

(>defn start
  "Start the LRF device.
   Returns a fully formed cmd root ready to send."
  []
  [=> :cmd/root]
  (core/create-command {:lrf {:start {}}}))

(>defn stop
  "Stop the LRF device.
   Returns a fully formed cmd root ready to send."
  []
  [=> :cmd/root]
  (core/create-command {:lrf {:stop {}}}))

;; ============================================================================
;; Target Designator Control
;; ============================================================================

(>defn target-designator-off
  "Turn off the target designator laser.
   Returns a fully formed cmd root ready to send."
  []
  [=> :cmd/root]
  (core/create-command {:lrf {:target_designator_off {}}}))

(>defn target-designator-on-mode-a
  "Turn on the target designator laser in mode A.
   Returns a fully formed cmd root ready to send."
  []
  [=> :cmd/root]
  (core/create-command {:lrf {:target_designator_on_mode_a {}}}))

(>defn target-designator-on-mode-b
  "Turn on the target designator laser in mode B.
   Returns a fully formed cmd root ready to send."
  []
  [=> :cmd/root]
  (core/create-command {:lrf {:target_designator_on_mode_b {}}}))

;; ============================================================================
;; Fog Mode Control
;; ============================================================================

(>defn enable-fog-mode
  "Enable fog mode for improved performance in foggy conditions.
   Returns a fully formed cmd root ready to send."
  []
  [=> :cmd/root]
  (core/create-command {:lrf {:enable_fog_mode {}}}))

(>defn disable-fog-mode
  "Disable fog mode.
   Returns a fully formed cmd root ready to send."
  []
  [=> :cmd/root]
  (core/create-command {:lrf {:disable_fog_mode {}}}))

;; ============================================================================
;; Session Management
;; ============================================================================

(>defn new-session
  "Start a new measurement session.
   Returns a fully formed cmd root ready to send."
  []
  [=> :cmd/root]
  (core/create-command {:lrf {:new_session {}}}))

;; ============================================================================
;; Meteo Data
;; ============================================================================

(>defn get-meteo
  "Request meteorological data from the LRF.
   Returns a fully formed cmd root ready to send."
  []
  [=> :cmd/root]
  (core/create-command {:lrf {:get_meteo {}}}))

;; ============================================================================
;; Refine Mode Control
;; ============================================================================

(>defn refine-on
  "Enable refine mode for improved measurement accuracy.
   Returns a fully formed cmd root ready to send."
  []
  [=> :cmd/root]
  (core/create-command {:lrf {:refine_on {}}}))

(>defn refine-off
  "Disable refine mode.
   Returns a fully formed cmd root ready to send."
  []
  [=> :cmd/root]
  (core/create-command {:lrf {:refine_off {}}}))
