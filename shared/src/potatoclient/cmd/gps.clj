(ns potatoclient.cmd.gps
  "GPS command functions.
   Based on the GPS message structure in jon_shared_cmd_gps.proto."
  (:require
    [potatoclient.cmd.core :as core]))

;; ============================================================================
;; GPS Control
;; ============================================================================

(defn start
  "Start GPS operations.
   Returns a fully formed cmd root ready to send."
  {:malli/schema [:=> [:cat] :cmd/root]}
  []
  (core/create-command {:gps {:start {}}}))

(defn stop
  "Stop GPS operations.
   Returns a fully formed cmd root ready to send."
  {:malli/schema [:=> [:cat] :cmd/root]}
  []
  (core/create-command {:gps {:stop {}}}))

;; ============================================================================
;; Manual Position Control
;; ============================================================================

(defn set-manual-position
  "Set manual GPS position.
   Latitude: -90 to 90 degrees
   Longitude: -180 to 180 degrees  
   Altitude: -430 to 100000 meters (Dead Sea to edge of space)
   Returns a fully formed cmd root ready to send."
  {:malli/schema [:=> [:cat :position/latitude :position/longitude :position/altitude] :cmd/root]}
  [latitude longitude altitude]
  (core/create-command
    {:gps {:set_manual_position {:latitude latitude
                                 :longitude longitude
                                 :altitude altitude}}}))

(defn set-use-manual-position
  "Enable or disable use of manual GPS position.
   When enabled, the system uses the manually set position instead of GPS receiver.
   Returns a fully formed cmd root ready to send."
  {:malli/schema [:=> [:cat :boolean] :cmd/root]}
  [use-manual?]
  (core/create-command
    {:gps {:set_use_manual_position {:flag use-manual?}}}))

;; ============================================================================
;; Meteo Data
;; ============================================================================

(defn get-meteo
  "Request meteorological data from GPS module.
   Returns a fully formed cmd root ready to send."
  {:malli/schema [:=> [:cat] :cmd/root]}
  []
  (core/create-command {:gps {:get_meteo {}}}))