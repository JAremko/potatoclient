(ns potatoclient.cmd.cam-day-glass-heater
  "Day Camera Glass Heater command functions for controlling lens defogging/deicing.
   Based on the DayCamGlassHeater message structure in jon_shared_cmd_day_cam_glass_heater.proto."
  (:require
            [malli.core :as m]
    [potatoclient.cmd.core :as core]))

;; ============================================================================
;; Device Control
;; ============================================================================

(defn start
  "Start the day camera glass heater subsystem.
   Returns a fully formed cmd root ready to send."
  []
  (core/create-command {:day_cam_glass_heater {:start {}}})) 
 (m/=> start [:=> [:cat] :cmd/root])

(defn stop
  "Stop the day camera glass heater subsystem.
   Returns a fully formed cmd root ready to send."
  []
  (core/create-command {:day_cam_glass_heater {:stop {}}})) 
 (m/=> stop [:=> [:cat] :cmd/root])

;; ============================================================================
;; Heater Control
;; ============================================================================

(defn turn-on
  "Turn on the day camera glass heater element.
   Returns a fully formed cmd root ready to send."
  []
  (core/create-command {:day_cam_glass_heater {:turn_on {}}})) 
 (m/=> turn-on [:=> [:cat] :cmd/root])

(defn turn-off
  "Turn off the day camera glass heater element.
   Returns a fully formed cmd root ready to send."
  []
  (core/create-command {:day_cam_glass_heater {:turn_off {}}})) 
 (m/=> turn-off [:=> [:cat] :cmd/root])

;; ============================================================================
;; Meteo Data
;; ============================================================================

(defn get-meteo
  "Request meteorological data from the day camera glass heater subsystem.
   Returns a fully formed cmd root ready to send."
  []
  (core/create-command {:day_cam_glass_heater {:get_meteo {}}})) 
 (m/=> get-meteo [:=> [:cat] :cmd/root])

;; ============================================================================
;; Removed Convenience Functions
;; ============================================================================
;; Removed functions that return vectors of commands
;; Each cmd constructor should return a single valid cmd/root