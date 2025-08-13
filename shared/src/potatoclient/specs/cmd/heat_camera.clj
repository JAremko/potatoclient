(ns potatoclient.specs.cmd.heat-camera
  "Heat Camera command specs matching buf.validate constraints.
   Based on jon_shared_cmd_heat_camera.proto.
   All maps use {:closed true} to catch typos and invalid keys."
  (:require
   [potatoclient.malli.registry :as registry]
   [potatoclient.specs.common :as common]
   [potatoclient.malli.oneof :as oneof]))

;; Zoom nested command (has its own oneof structure)

(def set-zoom-table-value-spec
  "SetZoomTableValue with value >= 0"
  [:map {:closed true}
   [:value [:int {:min 0}]]])

(def next-zoom-table-pos-spec [:map {:closed true}])
(def prev-zoom-table-pos-spec [:map {:closed true}])

(def zoom-spec
  "Zoom command with nested oneof"
  [:oneof
   [:set_zoom_table_value set-zoom-table-value-spec]
   [:next_zoom_table_pos next-zoom-table-pos-spec]
   [:prev_zoom_table_pos prev-zoom-table-pos-spec]])

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

(def enable-dde-spec [:map {:closed true}])
(def disable-dde-spec [:map {:closed true}])

;; FX (Effects) mode messages

(def set-fx-mode-spec
  "SetFxMode with heat FX mode enum (cannot be UNSPECIFIED)"
  [:map {:closed true}
   [:mode :enum/fx-mode-heat]])

(def next-fx-mode-spec [:map {:closed true}])
(def prev-fx-mode-spec [:map {:closed true}])
(def refresh-fx-mode-spec [:map {:closed true}])

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
   [:value [:double {:min 1.0}]]])

;; Focus control messages

(def focus-in-spec [:map {:closed true}])
(def focus-out-spec [:map {:closed true}])
(def focus-stop-spec [:map {:closed true}])
(def focus-step-plus-spec [:map {:closed true}])
(def focus-step-minus-spec [:map {:closed true}])

(def set-auto-focus-spec
  "SetAutoFocus with boolean value"
  [:map {:closed true}
   [:value [:boolean]]])

;; Zoom control messages (simple zoom commands, not the nested Zoom message)

(def zoom-in-spec [:map {:closed true}])
(def zoom-out-spec [:map {:closed true}])
(def zoom-stop-spec [:map {:closed true}])
(def reset-zoom-spec [:map {:closed true}])

;; Basic control messages

(def start-spec [:map {:closed true}])
(def stop-spec [:map {:closed true}])
(def photo-spec [:map {:closed true}])
(def calibrate-spec [:map {:closed true}])
(def get-meteo-spec [:map {:closed true}])
(def save-to-table-spec [:map {:closed true}])
(def set-calib-mode-spec [:map {:closed true}])

;; Main Heat Camera Root command spec using oneof - all 31 commands
(def heat-camera-command-spec
  [:oneof
   [:zoom zoom-spec]
   [:set_agc set-agc-spec]
   [:set_filters set-filters-spec]
   [:start start-spec]
   [:stop stop-spec]
   [:photo photo-spec]
   [:zoom_in zoom-in-spec]
   [:zoom_out zoom-out-spec]
   [:zoom_stop zoom-stop-spec]
   [:focus_in focus-in-spec]
   [:focus_out focus-out-spec]
   [:focus_stop focus-stop-spec]
   [:calibrate calibrate-spec]
   [:set_dde_level set-dde-level-spec]
   [:enable_dde enable-dde-spec]
   [:disable_dde disable-dde-spec]
   [:set_auto_focus set-auto-focus-spec]
   [:focus_step_plus focus-step-plus-spec]
   [:focus_step_minus focus-step-minus-spec]
   [:set_fx_mode set-fx-mode-spec]
   [:next_fx_mode next-fx-mode-spec]
   [:prev_fx_mode prev-fx-mode-spec]
   [:get_meteo get-meteo-spec]
   [:shift_dde shift-dde-spec]
   [:refresh_fx_mode refresh-fx-mode-spec]
   [:reset_zoom reset-zoom-spec]
   [:save_to_table save-to-table-spec]
   [:set_calib_mode set-calib-mode-spec]
   [:set_digital_zoom_level set-digital-zoom-level-spec]
   [:set_clahe_level set-clahe-level-spec]
   [:shift_clahe_level shift-clahe-level-spec]])

(registry/register! :cmd/heat-camera heat-camera-command-spec)