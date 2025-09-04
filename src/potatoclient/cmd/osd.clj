(ns potatoclient.cmd.osd
  "OSD (On-Screen Display) command functions for controlling display overlays.
   Based on the OSD message structure in jon_shared_cmd_osd.proto."
  (:require
    [malli.core :as m]
    [potatoclient.cmd.core :as core]))

;; ============================================================================
;; Screen Display Commands
;; ============================================================================

(defn show-default-screen
  "Show the default OSD screen.
   Returns a fully formed cmd root ready to send."
  []
  (core/create-command {:osd {:show_default_screen {}}}))
(m/=> show-default-screen [:=> [:cat] :cmd/root])

(defn show-lrf-measure-screen
  "Show the LRF measurement screen with targeting reticle.
   Returns a fully formed cmd root ready to send."
  []
  (core/create-command {:osd {:show_lrf_measure_screen {}}}))
(m/=> show-lrf-measure-screen [:=> [:cat] :cmd/root])

(defn show-lrf-result-screen
  "Show the LRF result screen with full measurement details.
   Returns a fully formed cmd root ready to send."
  []
  (core/create-command {:osd {:show_lrf_result_screen {}}}))
(m/=> show-lrf-result-screen [:=> [:cat] :cmd/root])

(defn show-lrf-result-simplified-screen
  "Show the simplified LRF result screen with basic measurement info.
   Returns a fully formed cmd root ready to send."
  []
  (core/create-command {:osd {:show_lrf_result_simplified_screen {}}}))
(m/=> show-lrf-result-simplified-screen [:=> [:cat] :cmd/root])

;; ============================================================================
;; Heat Camera OSD Control
;; ============================================================================

(defn enable-heat-osd
  "Enable the OSD overlay on the heat camera feed.
   Returns a fully formed cmd root ready to send."
  []
  (core/create-command {:osd {:enable_heat_osd {}}}))
(m/=> enable-heat-osd [:=> [:cat] :cmd/root])

(defn disable-heat-osd
  "Disable the OSD overlay on the heat camera feed.
   Returns a fully formed cmd root ready to send."
  []
  (core/create-command {:osd {:disable_heat_osd {}}}))
(m/=> disable-heat-osd [:=> [:cat] :cmd/root])

;; ============================================================================
;; Day Camera OSD Control
;; ============================================================================

(defn enable-day-osd
  "Enable the OSD overlay on the day camera feed.
   Returns a fully formed cmd root ready to send."
  []
  (core/create-command {:osd {:enable_day_osd {}}}))
(m/=> enable-day-osd [:=> [:cat] :cmd/root])

(defn disable-day-osd
  "Disable the OSD overlay on the day camera feed.
   Returns a fully formed cmd root ready to send."
  []
  (core/create-command {:osd {:disable_day_osd {}}}))
(m/=> disable-day-osd [:=> [:cat] :cmd/root])
