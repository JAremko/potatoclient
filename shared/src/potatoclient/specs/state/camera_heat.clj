(ns potatoclient.specs.state.camera-heat
  "Camera Heat message spec matching buf.validate constraints and EDN output format.
   Based on jon_shared_data_camera_heat.proto.
   All maps use {:closed true} to catch typos and invalid keys."
  (:require
   [potatoclient.malli.registry :as registry]))

;; AGC Mode enum for heat camera
(def heat-agc-mode-spec
  [:enum
   :JON_GUI_DATA_VIDEO_CHANNEL_HEAT_AGC_MODE_1
   :JON_GUI_DATA_VIDEO_CHANNEL_HEAT_AGC_MODE_2
   :JON_GUI_DATA_VIDEO_CHANNEL_HEAT_AGC_MODE_3])

(registry/register! :enum/heat-agc-mode heat-agc-mode-spec)

;; Heat filter enum
(def heat-filter-spec
  [:enum
   :JON_GUI_DATA_VIDEO_CHANNEL_HEAT_FILTER_hot_WHITE
   :JON_GUI_DATA_VIDEO_CHANNEL_HEAT_FILTER_hot_BLACK
   :JON_GUI_DATA_VIDEO_CHANNEL_HEAT_FILTER_rainbow
   :JON_GUI_DATA_VIDEO_CHANNEL_HEAT_FILTER_iron])

(registry/register! :enum/heat-filter heat-filter-spec)

;; JonGuiDataCameraHeat message spec based on EDN output:
;; {:agc_mode :JON_GUI_DATA_VIDEO_CHANNEL_HEAT_AGC_MODE_2
;;  :clahe_level 0.5
;;  :digital_zoom_level 1.0
;;  :filter :JON_GUI_DATA_VIDEO_CHANNEL_HEAT_FILTER_hot_WHITE
;;  :fx_mode :JON_GUI_DATA_FX_MODE_HEAT_A
;;  :zoom_table_pos 3
;;  :zoom_table_pos_max 4}

(def camera-heat-message-spec
  [:map {:closed true}
   [:agc_mode :enum/heat-agc-mode]
   [:clahe_level :range/normalized]           ; float [0.0, 1.0]
   [:digital_zoom_level [:double {:min 1.0}]] ; float >= 1.0
   [:filter :enum/heat-filter]
   [:fx_mode :enum/fx-mode-heat]              ; enum defined_only
   [:zoom_table_pos [:int {:min 0}]]          ; int32 >= 0
   [:zoom_table_pos_max [:int {:min 0}]]])    ; int32 >= 0

(registry/register! :state/camera-heat camera-heat-message-spec)