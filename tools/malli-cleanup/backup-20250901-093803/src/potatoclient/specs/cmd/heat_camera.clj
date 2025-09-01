(ns potatoclient.specs.cmd.heat-camera
  "Heat Camera command specs matching buf.validate constraints.
   Based on jon_shared_cmd_heat_camera.proto.
   All maps use {:closed true} to catch typos and invalid keys."
  (:require
   [potatoclient.malli.registry :as registry]))

;; Zoom nested command (has its own oneof structure)

(def set-zoom-table-value-spec
  "SetZoomTableValue with value >= 0"
  [:map {:closed true}
   [:value :proto/int32-positive]])


(def zoom-spec
  "Zoom command with nested oneof"
  [:oneof
   [:set_zoom_table_value set-zoom-table-value-spec]
   [:next_zoom_table_pos :cmd/empty]
   [:prev_zoom_table_pos :cmd/empty]])

;; AGC and Filter control messages

(def set-agc-spec
  "SetAGC with heat AGC mode enum (cannot be UNSPECIFIED)"
  [:map {:closed true}
   [:value :enum/heat-agc-mode]])

(def set-filters-spec
  "SetFilters with heat filter enum (cannot be UNSPECIFIED)"
  [:map {:closed true}
   [:value :enum/heat-filter]])

;; DDE (Digital Detail Enhancement) messages

(def set-dde-level-spec
  "SetDDELevel with value [0, 100]"
  [:map {:closed true}
   [:value [:int {:min 0 :max 100}]]])

(def shift-dde-spec
  "ShiftDDE with value [-100, 100]"
  [:map {:closed true}
   [:value [:int {:min -100 :max 100}]]])


;; FX (Effects) mode messages

(def set-fx-mode-spec
  "SetFxMode with heat FX mode enum (cannot be UNSPECIFIED)"
  [:map {:closed true}
   [:mode :enum/fx-mode-heat]])


;; CLAHE messages

(def set-clahe-level-spec
  "SetClaheLevel with value [0.0, 1.0]"
  [:map {:closed true}
   [:value :range/normalized]])

(def shift-clahe-level-spec
  "ShiftClaheLevel with value [-1.0, 1.0]"
  [:map {:closed true}
   [:value :range/normalized-offset]])

;; Digital zoom control

(def set-digital-zoom-level-spec
  "SetDigitalZoomLevel with value >= 1.0"
  [:map {:closed true}
   [:value :range/digital-zoom]])

;; Focus control messages


(def set-auto-focus-spec
  "SetAutoFocus with boolean value"
  [:map {:closed true}
   [:value [:boolean]]])

;; Zoom control messages (simple zoom commands, not the nested Zoom message)


;; Basic control messages


;; Main Heat Camera Root command spec using oneof - all 31 commands
(def heat-camera-command-spec
  [:oneof
   [:zoom zoom-spec]
   [:set_agc set-agc-spec]
   [:set_filter set-filters-spec]
   [:start :cmd/empty]
   [:stop :cmd/empty]
   [:photo :cmd/empty]
   [:zoom_in :cmd/empty]
   [:zoom_out :cmd/empty]
   [:zoom_stop :cmd/empty]
   [:focus_in :cmd/empty]
   [:focus_out :cmd/empty]
   [:focus_stop :cmd/empty]
   [:calibrate :cmd/empty]
   [:set_dde_level set-dde-level-spec]
   [:enable_dde :cmd/empty]
   [:disable_dde :cmd/empty]
   [:set_auto_focus set-auto-focus-spec]
   [:focus_step_plus :cmd/empty]
   [:focus_step_minus :cmd/empty]
   [:set_fx_mode set-fx-mode-spec]
   [:next_fx_mode :cmd/empty]
   [:prev_fx_mode :cmd/empty]
   [:get_meteo :cmd/empty]
   [:shift_dde shift-dde-spec]
   [:refresh_fx_mode :cmd/empty]
   [:reset_zoom :cmd/empty]
   [:save_to_table :cmd/empty]
   [:set_calib_mode :cmd/empty]
   [:set_digital_zoom_level set-digital-zoom-level-spec]
   [:set_clahe_level set-clahe-level-spec]
   [:shift_clahe_level shift-clahe-level-spec]])

(registry/register! :cmd/heat-camera heat-camera-command-spec)