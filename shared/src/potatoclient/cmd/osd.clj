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

(defn toggle-osd
  "Toggle OSD on both cameras - if either is on, turn both off; otherwise turn both on.
   Returns a vector of two cmd roots: [day-cmd heat-cmd]." {:malli/schema [:=> [:cat :boolean :boolean] [:vector :cmd/root]]}
  [day-enabled? heat-enabled?]
  (if (or day-enabled? heat-enabled?)
    ;; If either is on, turn both off
    [(disable-day-osd) (disable-heat-osd)]
    ;; If both are off, turn both on
    [(enable-day-osd) (enable-heat-osd)]))

(defn show-lrf-workflow
  "Show the complete LRF workflow screens in sequence.
   Returns a vector of cmd roots: [measure-screen-cmd result-screen-cmd default-screen-cmd].
   This is useful for demonstrating the LRF measurement process." {:malli/schema [:=> [:cat] [:vector :cmd/root]]}
  []
  [(show-lrf-measure-screen)
   (show-lrf-result-screen)
   (show-default-screen)])

(defn disable-all-osd
  "Disable OSD on both day and heat cameras.
   Returns a vector of two cmd roots: [day-disable-cmd heat-disable-cmd]." {:malli/schema [:=> [:cat] [:vector :cmd/root]]}
  []
  [(disable-day-osd)
   (disable-heat-osd)])

(defn enable-all-osd
  "Enable OSD on both day and heat cameras.
   Returns a vector of two cmd roots: [day-enable-cmd heat-enable-cmd]." {:malli/schema [:=> [:cat] [:vector :cmd/root]]}
  []
  [(enable-day-osd)
   (enable-heat-osd)])