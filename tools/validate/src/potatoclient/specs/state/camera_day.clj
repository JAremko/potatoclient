(ns potatoclient.specs.state.camera-day
  "Camera Day message spec matching buf.validate constraints and EDN output format.
   Based on jon_shared_data_camera_day.proto.
   All maps use {:closed true} to catch typos and invalid keys."
  (:require
   [potatoclient.specs.common]
   [potatoclient.malli.registry :as registry]))

;; JonGuiDataCameraDay message spec with all 11 fields from proto
(def camera-day-message-spec
  [:map {:closed true}
   [:auto_focus {:optional true} :boolean]
   [:auto_iris {:optional true} :boolean]
   [:clahe_level [:double {:min 0.0 :max 1.0}]]
   [:digital_zoom_level [:double {:min 1.0}]]
   [:focus_pos [:double {:min 0.0 :max 1.0}]]
   [:fx_mode :enum/fx-mode-day]
   [:infrared_filter {:optional true} :boolean]
   [:iris_pos [:double {:min 0.0 :max 1.0}]]
   [:zoom_pos [:double {:min 0.0 :max 1.0}]]
   [:zoom_table_pos [:int {:min 0}]]
   [:zoom_table_pos_max [:int {:min 0}]]])

(registry/register! :state/camera-day camera-day-message-spec)