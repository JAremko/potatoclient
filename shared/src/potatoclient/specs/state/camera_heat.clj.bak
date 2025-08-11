(ns potatoclient.specs.state.camera-heat
  "Camera Heat message spec matching buf.validate constraints and EDN output format.
   Based on jon_shared_data_camera_heat.proto.
   All maps use {:closed true} to catch typos and invalid keys."
  (:require
   [potatoclient.malli.registry :as registry]))

;; AGC Mode enum for heat camera
(def heat-agc-mode-spec
  [:enum
   :jon-gui-data-video-channel-heat-agc-mode-1
   :jon-gui-data-video-channel-heat-agc-mode-2
   :jon-gui-data-video-channel-heat-agc-mode-3])

(registry/register! :enum/heat-agc-mode heat-agc-mode-spec)

;; Heat filter enum
(def heat-filter-spec
  [:enum
   :jon-gui-data-video-channel-heat-filter-hot-white
   :jon-gui-data-video-channel-heat-filter-hot-black
   :jon-gui-data-video-channel-heat-filter-rainbow
   :jon-gui-data-video-channel-heat-filter-iron])

(registry/register! :enum/heat-filter heat-filter-spec)

;; JonGuiDataCameraHeat message spec based on EDN output:
;; {:agc-mode :jon-gui-data-video-channel-heat-agc-mode-2
;;  :clahe-level 0.5
;;  :digital-zoom-level 1.0
;;  :filter :jon-gui-data-video-channel-heat-filter-hot-white
;;  :fx-mode :jon-gui-data-fx-mode-heat-a
;;  :zoom-table-pos 3
;;  :zoom-table-pos-max 4}

(def camera-heat-message-spec
  [:map {:closed true}
   [:agc-mode :enum/heat-agc-mode]
   [:clahe-level :range/normalized]           ; float [0.0, 1.0]
   [:digital-zoom-level [:double {:min 1.0}]] ; float >= 1.0
   [:filter :enum/heat-filter]
   [:fx-mode :enum/fx-mode-heat]              ; enum defined_only
   [:zoom-table-pos [:int {:min 0}]]          ; int32 >= 0
   [:zoom-table-pos-max [:int {:min 0}]]])    ; int32 >= 0

(registry/register! :state/camera-heat camera-heat-message-spec)