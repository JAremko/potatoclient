(ns potatoclient.specs.state.camera-heat
  "Camera Heat message spec matching buf.validate constraints and EDN output format.
   Based on jon_shared_data_camera_heat.proto.
   All maps use {:closed true} to catch typos and invalid keys."
  (:require
   [potatoclient.specs.common]
   [potatoclient.malli.registry :as registry]))

;; JonGuiDataCameraHeat message spec
;; All 11 fields from proto definition

(def camera-heat-message-spec
  [:map {:closed true}
   [:zoom_pos :range/zoom]
   [:agc_mode :enum/heat-agc-mode]
   [:filter :enum/heat-filter]
   [:auto_focus :boolean]
   [:zoom_table_pos :proto/int32-positive]
   [:zoom_table_pos_max :proto/int32-positive]
   [:dde_level [:int {:min 0 :max 512}]]
   [:dde_enabled :boolean]
   [:fx_mode :enum/fx-mode-heat]
   [:digital_zoom_level [:double {:min 1.0 :max 100000.0}]]  ; double in proto
   [:clahe_level :range/normalized]])

(registry/register! :state/camera-heat camera-heat-message-spec)