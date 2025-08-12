(ns potatoclient.specs.cmd.heat-camera
  "Heat Camera command specs matching buf.validate constraints.
   Based on jon_shared_cmd_heat_camera.proto.
   All maps use {:closed true} to catch typos and invalid keys."
  (:require
   [potatoclient.malli.registry :as registry]
   [potatoclient.specs.oneof-edn :as oneof-edn]))

;; Heat camera command specs - based on proto-explorer findings
;; This is a oneof structure with 31 command types

;; Basic control
(def start-spec [:map {:closed true}])
(def stop-spec [:map {:closed true}])
(def photo-spec [:map {:closed true}])
(def calibrate-spec [:map {:closed true}])

;; Zoom control
(def zoom-spec
  [:map {:closed true}
   [:value [:float]]])

(def zoom-in-spec [:map {:closed true}])
(def zoom-out-spec [:map {:closed true}])
(def zoom-stop-spec [:map {:closed true}])
(def reset-zoom-spec [:map {:closed true}])

(def set-digital-zoom-level-spec
  [:map {:closed true}
   [:level [:int {:min 0 :max 10}]]])

;; Focus control
(def focus-in-spec [:map {:closed true}])
(def focus-out-spec [:map {:closed true}])
(def focus-stop-spec [:map {:closed true}])
(def focus-step-plus-spec [:map {:closed true}])
(def focus-step-minus-spec [:map {:closed true}])

(def set-auto-focus-spec
  [:map {:closed true}
   [:value [:boolean]]])

;; AGC control
(def set-agc-spec
  [:map {:closed true}
   [:mode [:enum
           :JON_GUI_DATA_HEAT_CAMERA_AGC_MODE_LINEAR
           :JON_GUI_DATA_HEAT_CAMERA_AGC_MODE_EQUAL
           :JON_GUI_DATA_HEAT_CAMERA_AGC_MODE_EXPONENTIAL]]])

;; Filter control
(def set-filter-spec
  [:map {:closed true}
   [:palette [:enum
              :JON_GUI_DATA_HEAT_CAMERA_PALETTE_WHITE_HOT
              :JON_GUI_DATA_HEAT_CAMERA_PALETTE_BLACK_HOT
              :JON_GUI_DATA_HEAT_CAMERA_PALETTE_RAINBOW
              :JON_GUI_DATA_HEAT_CAMERA_PALETTE_IRONBOW]]])

;; DDE control
(def set-dde-level-spec
  [:map {:closed true}
   [:level [:int {:min 0 :max 10}]]])

(def enable-dde-spec [:map {:closed true}])
(def disable-dde-spec [:map {:closed true}])
(def shift-dde-spec
  [:map {:closed true}
   [:shift [:int {:min -10 :max 10}]]])

;; FX mode
(def set-fx-mode-spec
  [:map {:closed true}
   [:mode [:enum
           :JON_GUI_DATA_HEAT_CAMERA_FX_MODE_OFF
           :JON_GUI_DATA_HEAT_CAMERA_FX_MODE_EDGE
           :JON_GUI_DATA_HEAT_CAMERA_FX_MODE_SILHOUETTE]]])

(def next-fx-mode-spec [:map {:closed true}])
(def prev-fx-mode-spec [:map {:closed true}])
(def refresh-fx-mode-spec [:map {:closed true}])

;; CLAHE
(def set-clahe-level-spec
  [:map {:closed true}
   [:level [:int {:min 0 :max 10}]]])

(def shift-clahe-level-spec
  [:map {:closed true}
   [:shift [:int {:min -10 :max 10}]]])

;; Calibration mode
(def set-calib-mode-spec
  [:map {:closed true}
   [:mode [:enum
           :JON_GUI_DATA_HEAT_CAMERA_CALIB_MODE_AUTO
           :JON_GUI_DATA_HEAT_CAMERA_CALIB_MODE_MANUAL]]])

;; Table save
(def save-to-table-spec
  [:map {:closed true}
   [:index [:int {:min 0 :max 10}]]])

;; Meteo
(def get-meteo-spec [:map {:closed true}])

;; Main Heat Camera command spec using oneof - all 31 commands
(def heat-camera-command-spec
  [:oneof_edn
   [:zoom zoom-spec]
   [:set_agc set-agc-spec]
   [:set_filter set-filter-spec]
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