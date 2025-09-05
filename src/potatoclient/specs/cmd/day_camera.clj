(ns potatoclient.specs.cmd.day-camera
  "Day Camera command specs matching buf.validate constraints.
   Based on jon_shared_cmd_day_camera.proto.
   All maps use {:closed true} to catch typos and invalid keys."
  (:require
    [potatoclient.malli.registry :as registry]))

;; Common message types used across multiple commands

(def set-value-spec
  "SetValue message with double value [0.0, 1.0]"
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

(def halt-spec :cmd/empty)

;; CLAHE-specific messages

(def set-clahe-level-spec
  "SetClaheLevel with value [0.0, 1.0]"
  [:map {:closed true}
   [:value :range/normalized]])

(def shift-clahe-level-spec
  "ShiftClaheLevel with value [-1.0, 1.0]"
  [:map {:closed true}
   [:value :range/normalized-offset]])

;; Focus-specific messages

(def focus-spec
  "Focus command with nested oneof"
  [:oneof
   [:set_value set-value-spec]
   [:move move-spec]
   [:halt halt-spec]
   [:offset offset-spec]
   [:reset_focus :cmd/empty]
   [:save_to_table_focus :cmd/empty]])

;; Zoom-specific messages

(def set-zoom-table-value-spec
  [:map {:closed true}
   [:value :proto/int32-positive]])

(def zoom-spec
  "Zoom command with nested oneof"
  [:oneof
   [:set_value set-value-spec]
   [:move move-spec]
   [:halt halt-spec]
   [:set_zoom_table_value set-zoom-table-value-spec]
   [:next_zoom_table_pos :cmd/empty]
   [:prev_zoom_table_pos :cmd/empty]
   [:offset offset-spec]
   [:reset_zoom :cmd/empty]
   [:save_to_table :cmd/empty]])

;; Simple command messages

(def set-iris-spec
  [:map {:closed true}
   [:value :range/normalized]])

(def set-infra-red-filter-spec
  [:map {:closed true}
   [:value [:boolean]]])

(def set-auto-iris-spec
  [:map {:closed true}
   [:value [:boolean]]])

(def set-fx-mode-spec
  [:map {:closed true}
   [:mode :enum/fx-mode-day]])

(def set-digital-zoom-level-spec
  [:map {:closed true}
   [:value :range/digital-zoom]])

;; ROI (Region of Interest) control messages

(def focus-roi-spec
  "FocusROI with rectangle coordinates and frame_time (protobuf expects full rectangle)"
  :composite/ndc-roi-with-timestamp)

(def track-roi-spec
  "TrackROI with rectangle coordinates and frame_time"
  :composite/ndc-roi-with-timestamp)

(def zoom-roi-spec
  "ZoomROI with rectangle coordinates and frame_time"
  :composite/ndc-roi-with-timestamp)

;; Main Day Camera Root command spec using oneof - all 20 commands (17 + 3 ROI)
(def day-camera-command-spec
  [:oneof
   [:focus focus-spec]
   [:zoom zoom-spec]
   [:set_iris set-iris-spec]
   [:set_infra_red_filter set-infra-red-filter-spec]
   [:start :cmd/empty]
   [:stop :cmd/empty]
   [:photo :cmd/empty]
   [:set_auto_iris set-auto-iris-spec]
   [:halt_all :cmd/empty]
   [:set_fx_mode set-fx-mode-spec]
   [:next_fx_mode :cmd/empty]
   [:prev_fx_mode :cmd/empty]
   [:get_meteo :cmd/empty]
   [:refresh_fx_mode :cmd/empty]
   [:set_digital_zoom_level set-digital-zoom-level-spec]
   [:set_clahe_level set-clahe-level-spec]
   [:shift_clahe_level shift-clahe-level-spec]
   [:focus_roi focus-roi-spec]
   [:track_roi track-roi-spec]
   [:zoom_roi zoom-roi-spec]])

(registry/register-spec! :cmd/day-camera day-camera-command-spec)
