(ns potatoclient.specs.cmd.rotary
  "Rotary Platform command specs matching buf.validate constraints.
   Based on jon_shared_cmd_rotary.proto.
   All maps use {:closed true} to catch typos and invalid keys."
  (:require
    [potatoclient.malli.registry :as registry]
    [potatoclient.specs.common]))

;; ====================================================================
;; Azimuth command specs (nested within Axis)
;; ====================================================================

(def set-azimuth-value-spec
  "Set absolute azimuth position with rotation direction."
  [:map {:closed true}
   [:value :angle/azimuth]
   [:direction :enum/rotary-direction]])

(def rotate-azimuth-to-spec
  "Rotate azimuth to target position with speed and direction control."
  [:map {:closed true}
   [:target_value :angle/azimuth]
   [:speed :range/normalized]
   [:direction :enum/rotary-direction]])

(def rotate-azimuth-spec
  "Continuous azimuth rotation with speed and direction."
  [:map {:closed true}
   [:speed :range/normalized]
   [:direction :enum/rotary-direction]])

(def rotate-azimuth-relative-spec
  "Rotate azimuth by relative amount from current position."
  [:map {:closed true}
   [:value :angle/relative-azimuth]
   [:speed :range/normalized]
   [:direction :enum/rotary-direction]])

(def rotate-azimuth-relative-set-spec
  "Set azimuth relative position from current without speed control."
  [:map {:closed true}
   [:value :angle/relative-azimuth]
   [:direction :enum/rotary-direction]])

;; Azimuth oneof structure
(def azimuth-spec
  "Azimuth axis control commands using protobuf oneof pattern."
  [:oneof
   [:set_value set-azimuth-value-spec]
   [:rotate_to rotate-azimuth-to-spec]
   [:rotate rotate-azimuth-spec]
   [:relative rotate-azimuth-relative-spec]
   [:relative_set rotate-azimuth-relative-set-spec]
   [:halt :cmd/empty]])

;; ====================================================================
;; Elevation command specs (nested within Axis)
;; ====================================================================

(def set-elevation-value-spec
  "Set absolute elevation position."
  [:map {:closed true}
   [:value :angle/elevation]])

(def rotate-elevation-to-spec
  "Rotate elevation to target position with speed control."
  [:map {:closed true}
   [:target_value :angle/elevation]
   [:speed :range/normalized]])

(def rotate-elevation-spec
  "Continuous elevation rotation with speed and direction."
  [:map {:closed true}
   [:speed :range/normalized]
   [:direction :enum/rotary-direction]])

(def rotate-elevation-relative-spec
  "Rotate elevation by relative amount from current position."
  [:map {:closed true}
   [:value :angle/relative-elevation]
   [:speed :range/normalized]
   [:direction :enum/rotary-direction]])

(def rotate-elevation-relative-set-spec
  "Set elevation relative position from current without speed control."
  [:map {:closed true}
   [:value :angle/relative-elevation]
   [:direction :enum/rotary-direction]])

;; Elevation oneof structure
(def elevation-spec
  "Elevation axis control commands using protobuf oneof pattern."
  [:oneof
   [:set_value set-elevation-value-spec]
   [:rotate_to rotate-elevation-to-spec]
   [:rotate rotate-elevation-spec]
   [:relative rotate-elevation-relative-spec]
   [:relative_set rotate-elevation-relative-set-spec]
   [:halt :cmd/empty]])

;; ====================================================================
;; Axis command (contains Azimuth and Elevation fields)
;; ====================================================================

(def axis-spec
  "Combined azimuth and elevation axis control container."
  [:map {:closed true}
   [:azimuth {:optional true} [:maybe azimuth-spec]]
   [:elevation {:optional true} [:maybe elevation-spec]]])

;; ====================================================================
;; Platform control commands
;; ====================================================================

(def set-platform-azimuth-spec
  "Set platform azimuth reference angle (-360, 360) degrees."
  [:map {:closed true}
   [:value [:and [:double {:min -360.0 :max 360.0}]
            [:> -360.0]
            [:< 360.0]]]])

(def set-platform-elevation-spec
  "Set platform elevation reference angle [-90, 90] degrees."
  [:map {:closed true}
   [:value :angle/elevation]])

(def set-platform-bank-spec
  "Set platform bank (roll) reference angle [-180, 180) degrees."
  [:map {:closed true}
   [:value :angle/bank]])

;; ====================================================================
;; Mode and configuration
;; ====================================================================

(def set-mode-spec
  "Set rotary operational mode (initialization, speed, position, etc.)."
  [:map {:closed true}
   [:mode :enum/rotary-mode]])

(def set-use-rotary-as-compass-spec
  "Enable/disable using rotary turret as compass reference."
  [:map {:closed true}
   [:flag [:boolean]]])

;; ====================================================================
;; GPS integration
;; ====================================================================

(def rotate-to-gps-spec
  "Rotate turret to point at GPS coordinates."
  [:map {:closed true}
   [:latitude :position/latitude]
   [:longitude :position/longitude]
   [:altitude :position/altitude]])

(def set-origin-gps-spec
  "Set GPS origin point for relative positioning calculations."
  [:map {:closed true}
   [:latitude :position/latitude]
   [:longitude :position/longitude]
   [:altitude :position/altitude]])

;; ====================================================================
;; NDC rotation
;; ====================================================================

(def rotate-to-ndc-spec
  "Rotate turret to point at screen NDC coordinates with frame sync."
  [:map {:closed true}
   [:channel :enum/video-channel]
   [:x :screen/ndc-x]
   [:y :screen/ndc-y]
   [:frame_time :time/frame-time]
   [:state_time [:int {:min 0 :max potatoclient.specs.common/long-max-value}]]])

(def halt-with-ndc-spec
  "Halt turret rotation with NDC position reference and timestamps."
  [:map {:closed true}
   [:channel :enum/video-channel]
   [:x :screen/ndc-x]
   [:y :screen/ndc-y]
   [:frame_time :time/frame-time]
   [:state_time [:int {:min 0 :max potatoclient.specs.common/long-max-value}]]])

;; ====================================================================
;; Scan node operations
;; ====================================================================

(def scan-select-node-spec
  "Select scan pattern node by index."
  [:map {:closed true}
   [:index :proto/int32-positive]])

(def scan-delete-node-spec
  "Delete scan pattern node at index."
  [:map {:closed true}
   [:index :proto/int32-positive]])

(def scan-update-node-spec
  "Update scan pattern node with position, zoom, and timing parameters."
  [:map {:closed true}
   [:index :proto/int32-positive]
   [:DayZoomTableValue :proto/int32-positive]
   [:HeatZoomTableValue :proto/int32-positive]
   [:azimuth :angle/azimuth]
   [:elevation :angle/elevation]
   [:linger [:double {:min 0.0}]]
   [:speed :speed/normalized]])

(def scan-add-node-spec
  "Add new scan pattern node with position, zoom, and timing parameters."
  [:map {:closed true}
   [:index :proto/int32-positive]
   [:DayZoomTableValue :proto/int32-positive]
   [:HeatZoomTableValue :proto/int32-positive]
   [:azimuth :angle/azimuth]
   [:elevation :angle/elevation]
   [:linger [:double {:min 0.0}]]
   [:speed :speed/normalized]])

;; ====================================================================
;; Main rotary command spec using oneof - all 25 commands
;; ====================================================================

(def rotary-command-spec
  "Complete rotary platform command specification with 25 command variants."
  [:oneof
   [:start :cmd/empty]
   [:stop :cmd/empty]
   [:axis axis-spec]
   [:set_platform_azimuth set-platform-azimuth-spec]
   [:set_platform_elevation set-platform-elevation-spec]
   [:set_platform_bank set-platform-bank-spec]
   [:halt :cmd/empty]
   [:set_use_rotary_as_compass set-use-rotary-as-compass-spec]
   [:rotate_to_gps rotate-to-gps-spec]
   [:set_origin_gps set-origin-gps-spec]
   [:set_mode set-mode-spec]
   [:rotate_to_ndc rotate-to-ndc-spec]
   [:scan_start :cmd/empty]
   [:scan_stop :cmd/empty]
   [:scan_pause :cmd/empty]
   [:scan_unpause :cmd/empty]
   [:get_meteo :cmd/empty]
   [:scan_prev :cmd/empty]
   [:scan_next :cmd/empty]
   [:scan_refresh_node_list :cmd/empty]
   [:scan_select_node scan-select-node-spec]
   [:scan_delete_node scan-delete-node-spec]
   [:scan_update_node scan-update-node-spec]
   [:scan_add_node scan-add-node-spec]
   [:halt_with_ndc halt-with-ndc-spec]])

(registry/register-spec! :cmd/rotary rotary-command-spec)