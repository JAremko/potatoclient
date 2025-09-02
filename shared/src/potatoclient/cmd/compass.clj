(ns potatoclient.cmd.compass
  "Compass command functions.
   Based on the Compass message structure in jon_shared_cmd_compass.proto."
  (:require
    [potatoclient.cmd.core :as core]))

;; ============================================================================
;; Control Commands (under :compass key in cmd root)
;; ============================================================================

(defn start
  "Start the compass subsystem.
   Returns a fully formed cmd root ready to send."
  {:malli/schema [:=> [:cat] :cmd/root]}
  []
  (core/create-command {:compass {:start {}}}))

(defn stop
  "Stop the compass subsystem.
   Returns a fully formed cmd root ready to send."
  {:malli/schema [:=> [:cat] :cmd/root]}
  []
  (core/create-command {:compass {:stop {}}}))

;; ============================================================================
;; Data Request Commands
;; ============================================================================

(defn get-meteo
  "Request compass meteorological data.
   Returns a fully formed cmd root ready to send."
  {:malli/schema [:=> [:cat] :cmd/root]}
  []
  (core/create-command {:compass {:get_meteo {}}}))

;; ============================================================================
;; Configuration Commands
;; ============================================================================

(defn set-magnetic-declination
  "Set the magnetic declination angle in mils.
   Returns a fully formed cmd root ready to send."
  {:malli/schema [:=> [:cat :angle/magnetic-declination] :cmd/root]}
  [value]
  (core/create-command {:compass {:set_magnetic_declination {:value value}}}))

(defn set-offset-angle-azimuth
  "Set the offset angle for azimuth in mils.
   Returns a fully formed cmd root ready to send."
  {:malli/schema [:=> [:cat :angle/offset-azimuth] :cmd/root]}
  [value]
  (core/create-command {:compass {:set_offset_angle_azimuth {:value value}}}))

(defn set-offset-angle-elevation
  "Set the offset angle for elevation in mils.
   Returns a fully formed cmd root ready to send."
  {:malli/schema [:=> [:cat :angle/offset-elevation] :cmd/root]}
  [value]
  (core/create-command {:compass {:set_offset_angle_elevation {:value value}}}))

(defn set-use-rotary-position
  "Configure whether to use rotary position data.
   Returns a fully formed cmd root ready to send."
  {:malli/schema [:=> [:cat :boolean] :cmd/root]}
  [use-rotary?]
  (core/create-command {:compass {:set_use_rotary_position {:flag use-rotary?}}}))

;; ============================================================================
;; Calibration Commands
;; ============================================================================

(defn calibrate-long-start
  "Start long calibration process for the compass.
   Returns a fully formed cmd root ready to send."
  {:malli/schema [:=> [:cat] :cmd/root]}
  []
  (core/create-command {:compass {:start_calibrate_long {}}}))

(defn calibrate-short-start
  "Start short calibration process for the compass.
   Returns a fully formed cmd root ready to send."
  {:malli/schema [:=> [:cat] :cmd/root]}
  []
  (core/create-command {:compass {:start_calibrate_short {}}}))

(defn calibrate-next
  "Move to the next step in the calibration process.
   Returns a fully formed cmd root ready to send."
  {:malli/schema [:=> [:cat] :cmd/root]}
  []
  (core/create-command {:compass {:calibrate_next {}}}))

(defn calibrate-cancel
  "Cancel the ongoing calibration process.
   Note: Proto has typo 'cencel' which we preserve for compatibility.
   Returns a fully formed cmd root ready to send."
  {:malli/schema [:=> [:cat] :cmd/root]}
  []
  (core/create-command {:compass {:calibrate_cencel {}}}))