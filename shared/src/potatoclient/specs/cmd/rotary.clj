(ns potatoclient.specs.cmd.rotary
  "Rotary Platform command specs matching buf.validate constraints.
   Based on jon_shared_cmd_rotary.proto.
   All maps use {:closed true} to catch typos and invalid keys."
  (:require
   [potatoclient.malli.registry :as registry]))

;; ====================================================================
;; Azimuth command specs (nested within Axis)
;; ====================================================================

(def set-azimuth-value-spec
  [:map {:closed true}
   [:value :angle/azimuth]
   [:direction :enum/rotary-direction]])

(def rotate-azimuth-to-spec
  [:map {:closed true}
   [:target_value :angle/azimuth]
   [:speed :range/normalized]
   [:direction :enum/rotary-direction]])

(def rotate-azimuth-spec
  [:map {:closed true}
   [:speed :range/normalized]
   [:direction :enum/rotary-direction]])

(def rotate-azimuth-relative-spec
  [:map {:closed true}
   [:value :angle/relative-azimuth]
   [:speed :range/normalized]
   [:direction :enum/rotary-direction]])

(def rotate-azimuth-relative-set-spec
  [:map {:closed true}
   [:value :angle/relative-azimuth]
   [:direction :enum/rotary-direction]])

;; Azimuth oneof structure
(def azimuth-spec
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
  [:map {:closed true}
   [:value :angle/elevation]])

(def rotate-elevation-to-spec
  [:map {:closed true}
   [:target_value :angle/elevation]
   [:speed :range/normalized]])

(def rotate-elevation-spec
  [:map {:closed true}
   [:speed :range/normalized]
   [:direction :enum/rotary-direction]])

(def rotate-elevation-relative-spec
  [:map {:closed true}
   [:value :angle/relative-elevation]
   [:speed :range/normalized]
   [:direction :enum/rotary-direction]])

(def rotate-elevation-relative-set-spec
  [:map {:closed true}
   [:value :angle/relative-elevation]
   [:direction :enum/rotary-direction]])

;; Elevation oneof structure
(def elevation-spec
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
  [:map {:closed true}
   [:azimuth azimuth-spec]
   [:elevation elevation-spec]])

;; ====================================================================
;; Platform control commands
;; ====================================================================

(def set-platform-azimuth-spec
  [:map {:closed true}
   [:value [:and [:double {:min -360.0 :max 360.0}]
            [:> -360.0]
            [:< 360.0]]]])

(def set-platform-elevation-spec
  [:map {:closed true}
   [:value :angle/elevation]])

(def set-platform-bank-spec
  [:map {:closed true}
   [:value :angle/bank]])

;; ====================================================================
;; Mode and configuration
;; ====================================================================

(def set-mode-spec
  [:map {:closed true}
   [:mode :enum/rotary-mode]])

(def set-use-rotary-as-compass-spec
  [:map {:closed true}
   [:flag [:boolean]]])

;; ====================================================================
;; GPS integration
;; ====================================================================

(def rotate-to-gps-spec
  [:map {:closed true}
   [:latitude :position/latitude]
   [:longitude :position/longitude]
   [:altitude :position/altitude]])

(def set-origin-gps-spec
  [:map {:closed true}
   [:latitude :position/latitude]
   [:longitude :position/longitude]
   [:altitude :position/altitude]])

;; ====================================================================
;; NDC rotation
;; ====================================================================

(def rotate-to-ndc-spec
  [:map {:closed true}
   [:channel :enum/video-channel]
   [:x :screen/ndc-x]
   [:y :screen/ndc-y]])

;; ====================================================================
;; Scan node operations
;; ====================================================================

(def scan-select-node-spec
  [:map {:closed true}
   [:index :proto/int32-positive]])

(def scan-delete-node-spec
  [:map {:closed true}
   [:index :proto/int32-positive]])

(def scan-update-node-spec
  [:map {:closed true}
   [:index :proto/int32-positive]
   [:DayZoomTableValue :proto/int32-positive]
   [:HeatZoomTableValue :proto/int32-positive]
   [:azimuth :angle/azimuth]
   [:elevation :angle/elevation]
   [:linger [:double {:min 0.0}]]
   [:speed [:and [:double {:min 0.0 :max 1.0}]
            [:> 0.0]]]])

(def scan-add-node-spec
  [:map {:closed true}
   [:index :proto/int32-positive]
   [:DayZoomTableValue :proto/int32-positive]
   [:HeatZoomTableValue :proto/int32-positive]
   [:azimuth :angle/azimuth]
   [:elevation :angle/elevation]
   [:linger [:double {:min 0.0}]]
   [:speed [:and [:double {:min 0.0 :max 1.0}]
            [:> 0.0]]]])

;; ====================================================================
;; Main rotary command spec using oneof - all 24 commands
;; ====================================================================

(def rotary-command-spec
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
   [:scan_add_node scan-add-node-spec]])

(registry/register! :cmd/rotary rotary-command-spec)