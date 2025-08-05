(ns potatoclient.specs.cmd.HeatCamera
  "Generated Malli specs from protobuf descriptors"
  (:require [malli.core :as m]
            [malli.generator :as mg]))

;; Note: FUNCTION-PLACEHOLDER markers indicate where runtime functions are needed
;; These will be replaced with actual implementations when loaded

(def prev-zoom-table-pos "Schema for prev-zoom-table-pos" [:map])


(def set-dde-level
  "Schema for set-dde-level"
  [:map [:value [:and [:maybe :int] [:>= 0] [:<= 100]]]])


(def set-calib-mode "Schema for set-calib-mode" [:map])


(def zoom
  "Schema for zoom"
  [:map
   [:cmd
    [:oneof
     {:set-zoom-table-value [:map [:set-zoom-table-value :cmd/set-zoom-table-value]],
      :next-zoom-table-pos [:map [:next-zoom-table-pos :cmd/next-zoom-table-pos]],
      :prev-zoom-table-pos [:map [:prev-zoom-table-pos :cmd/prev-zoom-table-pos]]}]]])


(def set-agc "Schema for set-agc" [:map [:value [:not [:enum [0]]]]])


(def shift-dde "Schema for shift-dde" [:map [:value [:and [:maybe :int] [:>= -100] [:<= 100]]]])


(def set-clahe-level
  "Schema for set-clahe-level"
  [:map [:value [:and [:maybe :double] [:>= 0] [:<= 1]]]])


(def disable-dde "Schema for disable-dde" [:map])


(def prev-fx-mode "Schema for prev-fx-mode" [:map])


(def start "Schema for start" [:map])


(def focus-step-minus "Schema for focus-step-minus" [:map])


(def set-digital-zoom-level "Schema for set-digital-zoom-level" [:map [:value [:>= 1]]])


(def enable-dde "Schema for enable-dde" [:map])


(def focus-stop "Schema for focus-stop" [:map])


(def stop "Schema for stop" [:map])


(def reset-zoom "Schema for reset-zoom" [:map])


(def zoom-out "Schema for zoom-out" [:map])


(def root
  "Schema for root"
  [:map
   [:cmd
    [:oneof
     {:set-dde-level [:map [:set-dde-level :cmd/set-dde-level]],
      :set-calib-mode [:map [:set-calib-mode :cmd/set-calib-mode]],
      :zoom [:map [:zoom :cmd/zoom]],
      :set-agc [:map [:set-agc :cmd/set-agc]],
      :shift-dde [:map [:shift-dde :cmd/shift-dde]],
      :set-filter [:map [:set-filter :cmd/set-filters]],
      :set-clahe-level [:map [:set-clahe-level :cmd/set-clahe-level]],
      :disable-dde [:map [:disable-dde :cmd/disable-dde]],
      :prev-fx-mode [:map [:prev-fx-mode :cmd/prev-fx-mode]],
      :start [:map [:start :cmd/start]],
      :focus-step-minus [:map [:focus-step-minus :cmd/focus-step-minus]],
      :set-digital-zoom-level [:map [:set-digital-zoom-level :cmd/set-digital-zoom-level]],
      :enable-dde [:map [:enable-dde :cmd/enable-dde]],
      :focus-stop [:map [:focus-stop :cmd/focus-stop]],
      :stop [:map [:stop :cmd/stop]],
      :reset-zoom [:map [:reset-zoom :cmd/reset-zoom]],
      :zoom-out [:map [:zoom-out :cmd/zoom-out]],
      :photo [:map [:photo :cmd/photo]],
      :zoom-in [:map [:zoom-in :cmd/zoom-in]],
      :get-meteo [:map [:get-meteo :cmd/get-meteo]],
      :focus-step-plus [:map [:focus-step-plus :cmd/focus-step-plus]],
      :set-fx-mode [:map [:set-fx-mode :cmd/set-fx-mode]],
      :refresh-fx-mode [:map [:refresh-fx-mode :cmd/refresh-fx-mode]],
      :focus-out [:map [:focus-out :cmd/focus-out]],
      :set-auto-focus [:map [:set-auto-focus :cmd/set-auto-focus]],
      :zoom-stop [:map [:zoom-stop :cmd/zoom-stop]],
      :save-to-table [:map [:save-to-table :cmd/save-to-table]],
      :next-fx-mode [:map [:next-fx-mode :cmd/next-fx-mode]],
      :calibrate [:map [:calibrate :cmd/calibrate]],
      :shift-clahe-level [:map [:shift-clahe-level :cmd/shift-clahe-level]],
      :focus-in [:map [:focus-in :cmd/focus-in]]}]]])


(def photo "Schema for photo" [:map])


(def zoom-in "Schema for zoom-in" [:map])


(def get-meteo "Schema for get-meteo" [:map])


(def next-zoom-table-pos "Schema for next-zoom-table-pos" [:map])


(def focus-step-plus "Schema for focus-step-plus" [:map])


(def set-value "Schema for set-value" [:map [:value [:and [:maybe :double] [:>= 0] [:<= 1]]]])


(def set-zoom-table-value "Schema for set-zoom-table-value" [:map [:value [:>= 0]]])


(def set-filters "Schema for set-filters" [:map [:value [:not [:enum [0]]]]])


(def set-fx-mode "Schema for set-fx-mode" [:map [:mode [:not [:enum [0]]]]])


(def refresh-fx-mode "Schema for refresh-fx-mode" [:map])


(def halt "Schema for halt" [:map])


(def focus-out "Schema for focus-out" [:map])


(def set-auto-focus "Schema for set-auto-focus" [:map [:value [:maybe :boolean]]])


(def zoom-stop "Schema for zoom-stop" [:map])


(def save-to-table "Schema for save-to-table" [:map])


(def next-fx-mode "Schema for next-fx-mode" [:map])


(def calibrate "Schema for calibrate" [:map])


(def shift-clahe-level
  "Schema for shift-clahe-level"
  [:map [:value [:and [:maybe :double] [:>= -1] [:<= 1]]]])


(def focus-in "Schema for focus-in" [:map])
