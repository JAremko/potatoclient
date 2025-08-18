(ns potatoclient.cmd.cam-day-glass-heater
  "Day Camera Glass Heater command functions for controlling lens defogging/deicing.
   Based on the DayCamGlassHeater message structure in jon_shared_cmd_day_cam_glass_heater.proto."
  (:require
   [com.fulcrologic.guardrails.malli.core :refer [>defn >defn- => | ?]]
   [potatoclient.cmd.core :as core]))

;; ============================================================================
;; Device Control
;; ============================================================================

(>defn start
  "Start the day camera glass heater subsystem.
   Returns a fully formed cmd root ready to send."
  []
  [=> :cmd/root]
  (core/create-command {:day_cam_glass_heater {:start {}}}))

(>defn stop
  "Stop the day camera glass heater subsystem.
   Returns a fully formed cmd root ready to send."
  []
  [=> :cmd/root]
  (core/create-command {:day_cam_glass_heater {:stop {}}}))

;; ============================================================================
;; Heater Control
;; ============================================================================

(>defn turn-on
  "Turn on the day camera glass heater element.
   Returns a fully formed cmd root ready to send."
  []
  [=> :cmd/root]
  (core/create-command {:day_cam_glass_heater {:turn_on {}}}))

(>defn turn-off
  "Turn off the day camera glass heater element.
   Returns a fully formed cmd root ready to send."
  []
  [=> :cmd/root]
  (core/create-command {:day_cam_glass_heater {:turn_off {}}}))

;; ============================================================================
;; Meteo Data
;; ============================================================================

(>defn get-meteo
  "Request meteorological data from the day camera glass heater subsystem.
   Returns a fully formed cmd root ready to send."
  []
  [=> :cmd/root]
  (core/create-command {:day_cam_glass_heater {:get_meteo {}}}))

;; ============================================================================
;; Convenience Functions
;; ============================================================================

(>defn enable-heater
  "Convenience function to start the subsystem and turn on the heater.
   Returns a vector of two cmd roots: [start-cmd turn-on-cmd]."
  []
  [=> [:vector :cmd/root]]
  [(start)
   (turn-on)])

(>defn disable-heater
  "Convenience function to turn off the heater and stop the subsystem.
   Returns a vector of two cmd roots: [turn-off-cmd stop-cmd]."
  []
  [=> [:vector :cmd/root]]
  [(turn-off)
   (stop)])

(>defn cycle-heater
  "Cycle the heater - turn it off then back on.
   Useful for resetting the heater or clearing condensation patterns.
   Returns a vector of two cmd roots: [turn-off-cmd turn-on-cmd]."
  []
  [=> [:vector :cmd/root]]
  [(turn-off)
   (turn-on)])
