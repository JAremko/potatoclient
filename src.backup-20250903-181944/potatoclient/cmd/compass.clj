(ns potatoclient.cmd.compass
  "Compass command functions.
   Based on the Compass message structure in jon_shared_cmd_compass.proto."
  (:require
            [malli.core :as m]
    [potatoclient.cmd.core :as core]))

;; ============================================================================
;; Control Commands (under :compass key in cmd root)
;; ============================================================================

(defn start
  "Start the compass subsystem.
   Returns a fully formed cmd root ready to send."
  []
  (core/create-command {:compass {:start {}}})) 
 (m/=> start [:=> [:cat] :cmd/root])

(defn stop
  "Stop the compass subsystem.
   Returns a fully formed cmd root ready to send."
  []
  (core/create-command {:compass {:stop {}}})) 
 (m/=> stop [:=> [:cat] :cmd/root])

;; ============================================================================
;; Data Request Commands
;; ============================================================================

(defn get-meteo
  "Request compass meteorological data.
   Returns a fully formed cmd root ready to send."
  []
  (core/create-command {:compass {:get_meteo {}}})) 
 (m/=> get-meteo [:=> [:cat] :cmd/root])

;; ============================================================================
;; Configuration Commands
;; ============================================================================

(defn set-magnetic-declination
  "Set the magnetic declination angle in mils.
   Returns a fully formed cmd root ready to send."
  [value]
  (core/create-command {:compass {:set_magnetic_declination {:value value}}})) 
 (m/=> set-magnetic-declination [:=> [:cat :angle/magnetic-declination] :cmd/root])

(defn set-offset-angle-azimuth
  "Set the offset angle for azimuth in mils.
   Returns a fully formed cmd root ready to send."
  [value]
  (core/create-command {:compass {:set_offset_angle_azimuth {:value value}}})) 
 (m/=> set-offset-angle-azimuth [:=> [:cat :angle/offset-azimuth] :cmd/root])

(defn set-offset-angle-elevation
  "Set the offset angle for elevation in mils.
   Returns a fully formed cmd root ready to send."
  [value]
  (core/create-command {:compass {:set_offset_angle_elevation {:value value}}})) 
 (m/=> set-offset-angle-elevation [:=> [:cat :angle/offset-elevation] :cmd/root])

(defn set-use-rotary-position
  "Configure whether to use rotary position data.
   Returns a fully formed cmd root ready to send."
  [use-rotary?]
  (core/create-command {:compass {:set_use_rotary_position {:flag use-rotary?}}})) 
 (m/=> set-use-rotary-position [:=> [:cat :boolean] :cmd/root])

;; ============================================================================
;; Calibration Commands
;; ============================================================================

(defn calibrate-long-start
  "Start long calibration process for the compass.
   Returns a fully formed cmd root ready to send."
  []
  (core/create-command {:compass {:start_calibrate_long {}}})) 
 (m/=> calibrate-long-start [:=> [:cat] :cmd/root])

(defn calibrate-short-start
  "Start short calibration process for the compass.
   Returns a fully formed cmd root ready to send."
  []
  (core/create-command {:compass {:start_calibrate_short {}}})) 
 (m/=> calibrate-short-start [:=> [:cat] :cmd/root])

(defn calibrate-next
  "Move to the next step in the calibration process.
   Returns a fully formed cmd root ready to send."
  []
  (core/create-command {:compass {:calibrate_next {}}})) 
 (m/=> calibrate-next [:=> [:cat] :cmd/root])

(defn calibrate-cancel
  "Cancel the ongoing calibration process.
   Note: Proto has typo 'cencel' which we preserve for compatibility.
   Returns a fully formed cmd root ready to send."
  []
  (core/create-command {:compass {:calibrate_cencel {}}})) 
 (m/=> calibrate-cancel [:=> [:cat] :cmd/root])