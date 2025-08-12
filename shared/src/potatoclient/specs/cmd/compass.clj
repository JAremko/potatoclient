(ns potatoclient.specs.cmd.compass
  "Compass command specs matching buf.validate constraints.
   Based on jon_shared_cmd_compass.proto.
   All maps use {:closed true} to catch typos and invalid keys."
  (:require
   [potatoclient.malli.registry :as registry]
   [potatoclient.specs.oneof-edn :as oneof-edn]))

;; Compass command specs - based on proto-explorer findings
;; This is a oneof structure with 11 command types

;; Basic control
(def start-spec [:map {:closed true}])
(def stop-spec [:map {:closed true}])

;; Configuration
(def set-magnetic-declination-spec
  [:map {:closed true}
   [:value [:float {:min -180.0 :max 180.0}]]])

(def set-offset-angle-azimuth-spec
  [:map {:closed true}
   [:value [:float {:min -360.0 :max 360.0}]]])

(def set-offset-angle-elevation-spec
  [:map {:closed true}
   [:value [:float {:min -90.0 :max 90.0}]]])

(def set-use-rotary-position-spec
  [:map {:closed true}
   [:value [:boolean]]])

;; Calibration
(def start-calibrate-long-spec [:map {:closed true}])
(def start-calibrate-short-spec [:map {:closed true}])
(def calibrate-next-spec [:map {:closed true}])
(def calibrate-cencel-spec [:map {:closed true}]) ; Note: typo in proto (cencel)

;; Meteo
(def get-meteo-spec [:map {:closed true}])

;; Main compass command spec using oneof - all 11 commands
(def compass-command-spec
  [:oneof_edn
   [:start start-spec]
   [:stop stop-spec]
   [:set_magnetic_declination set-magnetic-declination-spec]
   [:set_offset_angle_azimuth set-offset-angle-azimuth-spec]
   [:set_offset_angle_elevation set-offset-angle-elevation-spec]
   [:set_use_rotary_position set-use-rotary-position-spec]
   [:start_calibrate_long start-calibrate-long-spec]
   [:start_calibrate_short start-calibrate-short-spec]
   [:calibrate_next calibrate-next-spec]
   [:calibrate_cencel calibrate-cencel-spec] ; Keep proto typo
   [:get_meteo get-meteo-spec]])

(registry/register! :cmd/compass compass-command-spec)