(ns potatoclient.specs.cmd.compass
  "Compass command specs matching buf.validate constraints.
   Based on jon_shared_cmd_compass.proto.
   All maps use {:closed true} to catch typos and invalid keys."
  (:require
    [potatoclient.malli.registry :as registry]))

;; Compass command specs
;; This is a oneof structure with 11 command types

;; Configuration
(def set-magnetic-declination-spec
  "Spec for setting magnetic declination angle.
   Validates value is within magnetic declination range."
  [:map {:closed true}
   [:value :angle/magnetic-declination]])

(def set-offset-angle-azimuth-spec
  "Spec for setting azimuth offset angle.
   Validates value is within offset azimuth range."
  [:map {:closed true}
   [:value :angle/offset-azimuth]])

(def set-offset-angle-elevation-spec
  "Spec for setting elevation offset angle.
   Validates value is within offset elevation range."
  [:map {:closed true}
   [:value :angle/offset-elevation]])

(def set-use-rotary-position-spec
  "Spec for toggling rotary position usage.
   Boolean flag to enable/disable rotary position."
  [:map {:closed true}
   [:flag [:boolean]]])

;; Main compass command spec using oneof - all 11 commands
(def compass-command-spec
  "Main compass command spec with all 11 command types.
   Uses oneof to ensure exactly one command is specified.
   Includes start/stop, configuration, calibration, and meteo commands."
  [:oneof
   [:start :cmd/empty]
   [:stop :cmd/empty]
   [:set_magnetic_declination set-magnetic-declination-spec]
   [:set_offset_angle_azimuth set-offset-angle-azimuth-spec]
   [:set_offset_angle_elevation set-offset-angle-elevation-spec]
   [:set_use_rotary_position set-use-rotary-position-spec]
   [:start_calibrate_long :cmd/empty]
   [:start_calibrate_short :cmd/empty]
   [:calibrate_next :cmd/empty]
   [:calibrate_cencel :cmd/empty] ; Keep proto typo
   [:get_meteo :cmd/empty]])

(registry/register-spec! :cmd/compass compass-command-spec)