(ns potatoclient.specs.state.camera-day
  "Camera Day message spec matching buf.validate constraints and EDN output format.
   Based on jon_shared_data_camera_day.proto.
   All maps use {:closed true} to catch typos and invalid keys."
  (:require
   [potatoclient.malli.registry :as registry]))

;; JonGuiDataCameraDay message spec based on proto and EDN output:
;; Proto has fields: focus_pos, zoom_pos, iris_pos, infrared_filter, 
;;                   zoom_table_pos, zoom_table_pos_max, fx_mode,
;;                   auto_focus, auto_iris, digital_zoom_level, clahe_level
;;
;; EDN output shows:
;; {:clahe_level 0.16
;;  :digital_zoom_level 1.0
;;  :focus_pos 1.0
;;  :fx_mode :JON_GUI_DATA_FX_MODE_DAY_A
;;  :infrared_filter true
;;  :iris_pos 0.03
;;  :zoom_pos 0.59938735
;;  :zoom_table_pos 3
;;  :zoom_table_pos_max 4}

(def camera-day-message-spec
  [:map {:closed true}
   [:clahe_level :range/normalized]           ; float [0.0, 1.0]
   [:digital_zoom_level [:double {:min 1.0}]] ; float >= 1.0
   [:focus_pos :range/normalized]             ; float [0.0, 1.0]
   [:fx_mode :enum/fx-mode-day]               ; enum defined_only
   [:infrared_filter boolean?]
   [:iris_pos :range/normalized]              ; float [0.0, 1.0]
   [:zoom_pos :range/normalized]              ; float [0.0, 1.0]
   [:zoom_table_pos [:int {:min 0}]]          ; int32 >= 0
   [:zoom_table_pos_max [:int {:min 0}]]])    ; int32 >= 0

(registry/register! :state/camera-day camera-day-message-spec)