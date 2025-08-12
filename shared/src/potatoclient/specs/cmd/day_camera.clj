(ns potatoclient.specs.cmd.day-camera
  "Day Camera command specs matching buf.validate constraints.
   Based on jon_shared_cmd_day_camera.proto.
   All maps use {:closed true} to catch typos and invalid keys."
  (:require
   [potatoclient.malli.registry :as registry]
   [potatoclient.specs.oneof-edn :as oneof-edn]))

;; Day camera command specs - based on proto-explorer findings
;; This is a oneof structure with 17 command types

;; Basic control
(def start-spec [:map {:closed true}])
(def stop-spec [:map {:closed true}])
(def halt-all-spec [:map {:closed true}])
(def photo-spec [:map {:closed true}])

;; Focus and zoom
(def focus-spec
  [:map {:closed true}
   [:value [:float]]])

(def zoom-spec
  [:map {:closed true}
   [:value [:float]]])

(def set-digital-zoom-level-spec
  [:map {:closed true}
   [:level [:int {:min 0 :max 10}]]])

;; Iris control
(def set-iris-spec
  [:map {:closed true}
   [:value [:float {:min 0.0 :max 1.0}]]])

(def set-auto-iris-spec
  [:map {:closed true}
   [:value [:boolean]]])

;; Filter control
(def set-infra-red-filter-spec
  [:map {:closed true}
   [:value [:boolean]]])

;; FX mode
(def set-fx-mode-spec
  [:map {:closed true}
   [:mode [:enum
           :JON_GUI_DATA_DAY_CAMERA_FX_MODE_OFF
           :JON_GUI_DATA_DAY_CAMERA_FX_MODE_BW
           :JON_GUI_DATA_DAY_CAMERA_FX_MODE_SILHOUETTE]]])

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

;; Meteo
(def get-meteo-spec [:map {:closed true}])

;; Main Day Camera command spec using oneof - all 17 commands
(def day-camera-command-spec
  [:oneof_edn
   [:focus focus-spec]
   [:zoom zoom-spec]
   [:set_iris set-iris-spec]
   [:set_infra_red_filter set-infra-red-filter-spec]
   [:start start-spec]
   [:stop stop-spec]
   [:photo photo-spec]
   [:set_auto_iris set-auto-iris-spec]
   [:halt_all halt-all-spec]
   [:set_fx_mode set-fx-mode-spec]
   [:next_fx_mode next-fx-mode-spec]
   [:prev_fx_mode prev-fx-mode-spec]
   [:get_meteo get-meteo-spec]
   [:refresh_fx_mode refresh-fx-mode-spec]
   [:set_digital_zoom_level set-digital-zoom-level-spec]
   [:set_clahe_level set-clahe-level-spec]
   [:shift_clahe_level shift-clahe-level-spec]])

(registry/register! :cmd/day-camera day-camera-command-spec)