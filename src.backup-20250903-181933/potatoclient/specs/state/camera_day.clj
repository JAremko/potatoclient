(ns potatoclient.specs.state.camera-day
  "Camera Day message spec matching buf.validate constraints and EDN output format.
   Based on jon_shared_data_camera_day.proto.
   All maps use {:closed true} to catch typos and invalid keys."
  (:require
    [potatoclient.specs.common]
    [potatoclient.malli.registry :as registry]))

;; JonGuiDataCameraDay message spec
;; All 11 fields from proto definition

(def camera-day-message-spec
  [:map {:closed true}
   [:focus_pos :range/focus]  ; double in proto (0-1)
   [:zoom_pos :range/zoom]  ; double in proto (0-1)  
   [:iris_pos :range/normalized]  ; double in proto (0-1)
   [:infrared_filter :boolean]
   [:zoom_table_pos :proto/int32-positive]
   [:zoom_table_pos_max :proto/int32-positive]
   [:fx_mode :enum/fx-mode-day]
   [:auto_focus :boolean]
   [:auto_iris :boolean]
   [:digital_zoom_level [:double {:min 1.0 :max 100000.0}]]  ; double in proto
   [:clahe_level :range/normalized]])  ; double in proto (0-1)

(registry/register-spec! :state/camera-day camera-day-message-spec)