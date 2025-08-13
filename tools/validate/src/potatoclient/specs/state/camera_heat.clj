(ns potatoclient.specs.state.camera-heat
  "Camera Heat message spec matching buf.validate constraints and EDN output format.
   Based on jon_shared_data_camera_heat.proto.
   All maps use {:closed true} to catch typos and invalid keys."
  (:require
   [potatoclient.specs.common]
   [potatoclient.malli.registry :as registry]))

;; JonGuiDataCameraHeat message spec with all 11 fields from proto
(def camera-heat-message-spec
  [:map {:closed true}
   [:agc_mode :enum/heat-agc-mode]
   [:auto_focus {:optional true} :boolean]
   [:clahe_level [:double {:min 0.0 :max 1.0}]]
   [:dde_enabled {:optional true} :boolean]
   [:dde_level {:optional true} [:int {:min 0 :max 512}]]
   [:digital_zoom_level [:double {:min 1.0}]]
   [:filter :enum/heat-filter]
   [:fx_mode :enum/fx-mode-heat]
   [:zoom_pos {:optional true} [:double {:min 0.0 :max 1.0}]]
   [:zoom_table_pos [:int {:min 0}]]
   [:zoom_table_pos_max [:int {:min 0}]]])

(registry/register! :state/camera-heat camera-heat-message-spec)