(ns potatoclient.cmd.osd
  "OSD (On-Screen Display) command functions for controlling display overlays.
   Based on the OSD message structure in jon_shared_cmd_osd.proto."
  (:require
   [potatoclient.cmd.core :as core]))

;; ============================================================================
;; Screen Display Commands
;; ============================================================================

(defn show-default-screen
  "Show the default OSD screen.
   Returns a fully formed cmd root ready to send." {:malli/schema [:=> [:cat] :cmd/root]}
  []
  (core/create-command {:osd {:show_default_screen {}}}))

(defn show-lrf-measure-screen
  "Show the LRF measurement screen with targeting reticle.
   Returns a fully formed cmd root ready to send." {:malli/schema [:=> [:cat] :cmd/root]}
  []
  (core/create-command {:osd {:show_lrf_measure_screen {}}}))

(defn show-lrf-result-screen
  "Show the LRF result screen with full measurement details.
   Returns a fully formed cmd root ready to send." {:malli/schema [:=> [:cat] :cmd/root]}
  []
  (core/create-command {:osd {:show_lrf_result_screen {}}}))

(defn show-lrf-result-simplified-screen
  "Show the simplified LRF result screen with basic measurement info.
   Returns a fully formed cmd root ready to send." {:malli/schema [:=> [:cat] :cmd/root]}
  []
  (core/create-command {:osd {:show_lrf_result_simplified_screen {}}}))

;; ============================================================================
;; Heat Camera OSD Control
;; ============================================================================

(defn enable-heat-osd
  "Enable the OSD overlay on the heat camera feed.
   Returns a fully formed cmd root ready to send." {:malli/schema [:=> [:cat] :cmd/root]}
  []
  (core/create-command {:osd {:enable_heat_osd {}}}))

(defn disable-heat-osd
  "Disable the OSD overlay on the heat camera feed.
   Returns a fully formed cmd root ready to send." {:malli/schema [:=> [:cat] :cmd/root]}
  []
  (core/create-command {:osd {:disable_heat_osd {}}}))

;; ============================================================================
;; Day Camera OSD Control
;; ============================================================================

(defn enable-day-osd
  "Enable the OSD overlay on the day camera feed.
   Returns a fully formed cmd root ready to send." {:malli/schema [:=> [:cat] :cmd/root]}
  []
  (core/create-command {:osd {:enable_day_osd {}}}))

(defn disable-day-osd
  "Disable the OSD overlay on the day camera feed.
   Returns a fully formed cmd root ready to send." {:malli/schema [:=> [:cat] :cmd/root]}
  []
  (core/create-command {:osd {:disable_day_osd {}}}))

;; ============================================================================
;; Convenience Functions
;; ============================================================================

;; Removed functions that return vectors of commands
;; Each cmd constructor should return a single valid cmd/root