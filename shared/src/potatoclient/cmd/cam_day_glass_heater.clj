(ns potatoclient.cmd.cam-day-glass-heater
  "Day Camera Glass Heater command functions for controlling lens defogging/deicing.
   Based on the DayCamGlassHeater message structure in jon_shared_cmd_day_cam_glass_heater.proto."
  (:require
   [potatoclient.cmd.core :as core]))

;; ============================================================================
;; Device Control
;; ============================================================================

(defn start
  "Start the day camera glass heater subsystem.
   Returns a fully formed cmd root ready to send." {:malli/schema [:=> [:cat] :cmd/root]}
  []
  (core/create-command {:day_cam_glass_heater {:start {}}}))

(defn stop
  "Stop the day camera glass heater subsystem.
   Returns a fully formed cmd root ready to send." {:malli/schema [:=> [:cat] :cmd/root]}
  []
  (core/create-command {:day_cam_glass_heater {:stop {}}}))

;; ============================================================================
;; Heater Control
;; ============================================================================

(defn turn-on
  "Turn on the day camera glass heater element.
   Returns a fully formed cmd root ready to send." {:malli/schema [:=> [:cat] :cmd/root]}
  []
  (core/create-command {:day_cam_glass_heater {:turn_on {}}}))

(defn turn-off
  "Turn off the day camera glass heater element.
   Returns a fully formed cmd root ready to send." {:malli/schema [:=> [:cat] :cmd/root]}
  []
  (core/create-command {:day_cam_glass_heater {:turn_off {}}}))

;; ============================================================================
;; Meteo Data
;; ============================================================================

(defn get-meteo
  "Request meteorological data from the day camera glass heater subsystem.
   Returns a fully formed cmd root ready to send." {:malli/schema [:=> [:cat] :cmd/root]}
  []
  (core/create-command {:day_cam_glass_heater {:get_meteo {}}}))

;; ============================================================================
;; Removed Convenience Functions
;; ============================================================================
;; Removed functions that return vectors of commands
;; Each cmd constructor should return a single valid cmd/root
