(ns potatoclient.specs.cmd.day-camera
  "Day Camera command specs matching buf.validate constraints.
   Based on jon_shared_cmd_day_camera.proto.
   All maps use {:closed true} to catch typos and invalid keys."
  (:require
   [potatoclient.malli.registry :as registry]
   [potatoclient.specs.common :as common]
   [potatoclient.malli.oneof :as oneof]))

;; Common message types used across multiple commands

(def set-value-spec
  "SetValue message with float value [0.0, 1.0]"
  [:map {:closed true}
   [:value [:double {:min 0.0 :max 1.0}]]])

(def move-spec
  "Move message with target_value and speed, both [0.0, 1.0]"
  [:map {:closed true}
   [:target_value :range/normalized]
   [:speed :speed/normalized]])

(def offset-spec
  "Offset message with offset_value [-1.0, 1.0]"
  [:map {:closed true}
   [:offset_value :range/normalized-offset]])

(def halt-spec
  "Halt message - empty"
  [:map {:closed true}])

;; CLAHE-specific messages

(def set-clahe-level-spec
  "SetClaheLevel with value [0.0, 1.0]"
  [:map {:closed true}
   [:value :range/normalized]])

(def shift-clahe-level-spec
  "ShiftClaheLevel with value [-1.0, 1.0]"
  [:map {:closed true}
   [:value :range/normalized]])

;; Focus-specific messages

(def reset-focus-spec [:map {:closed true}])
(def save-to-table-focus-spec [:map {:closed true}])

(def focus-spec
  "Focus command with nested oneof"
  [:oneof
   [:set_value set-value-spec]
   [:move move-spec]
   [:halt halt-spec]
   [:offset offset-spec]
   [:reset_focus reset-focus-spec]
   [:save_to_table_focus save-to-table-focus-spec]])

;; Zoom-specific messages

(def set-zoom-table-value-spec
  [:map {:closed true}
   [:value [:int {:min 0}]]])

(def next-zoom-table-pos-spec [:map {:closed true}])
(def prev-zoom-table-pos-spec [:map {:closed true}])
(def reset-zoom-spec [:map {:closed true}])
(def save-to-table-spec [:map {:closed true}])

(def zoom-spec
  "Zoom command with nested oneof"
  [:oneof
   [:set_value set-value-spec]
   [:move move-spec]
   [:halt halt-spec]
   [:set_zoom_table_value set-zoom-table-value-spec]
   [:next_zoom_table_pos next-zoom-table-pos-spec]
   [:prev_zoom_table_pos prev-zoom-table-pos-spec]
   [:offset offset-spec]
   [:reset_zoom reset-zoom-spec]
   [:save_to_table save-to-table-spec]])

;; Simple command messages

(def set-iris-spec
  [:map {:closed true}
   [:value :range/normalized]])

(def set-infra-red-filter-spec
  [:map {:closed true}
   [:value [:boolean]]])

(def start-spec [:map {:closed true}])
(def stop-spec [:map {:closed true}])
(def photo-spec [:map {:closed true}])

(def set-auto-iris-spec
  [:map {:closed true}
   [:value [:boolean]]])

(def halt-all-spec [:map {:closed true}])

(def set-fx-mode-spec
  [:map {:closed true}
   [:mode :enum/fx-mode-day]])

(def next-fx-mode-spec [:map {:closed true}])
(def prev-fx-mode-spec [:map {:closed true}])
(def get-meteo-spec [:map {:closed true}])
(def refresh-fx-mode-spec [:map {:closed true}])

(def set-digital-zoom-level-spec
  [:map {:closed true}
   [:value [:double {:min 1.0}]]])

;; Main Day Camera Root command spec using oneof - all 17 commands
(def day-camera-command-spec
  [:oneof
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
