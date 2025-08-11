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
;; {:clahe-level 0.16
;;  :digital-zoom-level 1.0
;;  :focus-pos 1.0
;;  :fx-mode :jon-gui-data-fx-mode-day-a
;;  :infrared-filter true
;;  :iris-pos 0.03
;;  :zoom-pos 0.59938735
;;  :zoom-table-pos 3
;;  :zoom-table-pos-max 4}

(def camera-day-message-spec
  [:map {:closed true}
   [:clahe-level :range/normalized]           ; float [0.0, 1.0]
   [:digital-zoom-level [:double {:min 1.0}]] ; float >= 1.0
   [:focus-pos :range/normalized]             ; float [0.0, 1.0]
   [:fx-mode :enum/fx-mode-day]               ; enum defined_only
   [:infrared-filter :boolean]
   [:iris-pos :range/normalized]              ; float [0.0, 1.0]
   [:zoom-pos :range/normalized]              ; float [0.0, 1.0]
   [:zoom-table-pos [:int {:min 0}]]          ; int32 >= 0
   [:zoom-table-pos-max [:int {:min 0}]]])    ; int32 >= 0

(registry/register! :state/camera-day camera-day-message-spec)