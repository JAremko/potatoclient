(ns potatoclient.specs.cmd.DayCamera "Generated Malli specs from protobuf descriptors" (:require [malli.core :as proto-explorer.spec-generator/m] [malli.generator :as proto-explorer.spec-generator/mg]))

;; Note: FUNCTION-PLACEHOLDER markers indicate where runtime functions are needed
;; These will be replaced with actual implementations when loaded

(def prev-zoom-table-pos "Schema for prev-zoom-table-pos" [:map])

(def zoom "Schema for zoom" [:map [:cmd [:oneof {:prev-zoom-table-pos [:map [:prev-zoom-table-pos :cmd/prev-zoom-table-pos]], :offset [:map [:offset :cmd/offset]], :move [:map [:move :cmd/move]], :reset-zoom [:map [:reset-zoom :cmd/reset-zoom]], :next-zoom-table-pos [:map [:next-zoom-table-pos :cmd/next-zoom-table-pos]], :set-value [:map [:set-value :cmd/set-value]], :set-zoom-table-value [:map [:set-zoom-table-value :cmd/set-zoom-table-value]], :halt [:map [:halt :cmd/halt]], :save-to-table [:map [:save-to-table :cmd/save-to-table]]}]]])

(def set-infra-red-filter "Schema for set-infra-red-filter" [:map [:value [:maybe :boolean]]])

(def offset "Schema for offset" [:map [:offset-value [:and [:maybe :double] [:>= -1] [:<= 1]]]])

(def set-clahe-level "Schema for set-clahe-level" [:map [:value [:and [:maybe :double] [:>= 0] [:<= 1]]]])

(def prev-fx-mode "Schema for prev-fx-mode" [:map])

(def start "Schema for start" [:map])

(def move "Schema for move" [:map [:target-value [:and [:maybe :double] [:>= 0] [:<= 1]]] [:speed [:and [:maybe :double] [:>= 0] [:<= 1]]]])

(def reset-focus "Schema for reset-focus" [:map])

(def halt-all "Schema for halt-all" [:map])

(def get-pos "Schema for get-pos" [:map])

(def set-digital-zoom-level "Schema for set-digital-zoom-level" [:map [:value [:>= 1]]])

(def stop "Schema for stop" [:map])

(def reset-zoom "Schema for reset-zoom" [:map])

(def save-to-table-focus "Schema for save-to-table-focus" [:map])

(def root "Schema for root" [:map [:cmd [:oneof {:zoom [:map [:zoom :cmd/zoom]], :set-infra-red-filter [:map [:set-infra-red-filter :cmd/set-infra-red-filter]], :set-clahe-level [:map [:set-clahe-level :cmd/set-clahe-level]], :prev-fx-mode [:map [:prev-fx-mode :cmd/prev-fx-mode]], :start [:map [:start :cmd/start]], :halt-all [:map [:halt-all :cmd/halt-all]], :set-digital-zoom-level [:map [:set-digital-zoom-level :cmd/set-digital-zoom-level]], :stop [:map [:stop :cmd/stop]], :photo [:map [:photo :cmd/photo]], :get-meteo [:map [:get-meteo :cmd/get-meteo]], :focus [:map [:focus :cmd/focus]], :set-fx-mode [:map [:set-fx-mode :cmd/set-fx-mode]], :set-iris [:map [:set-iris :cmd/set-iris]], :refresh-fx-mode [:map [:refresh-fx-mode :cmd/refresh-fx-mode]], :set-auto-iris [:map [:set-auto-iris :cmd/set-auto-iris]], :next-fx-mode [:map [:next-fx-mode :cmd/next-fx-mode]], :shift-clahe-level [:map [:shift-clahe-level :cmd/shift-clahe-level]]}]]])

(def photo "Schema for photo" [:map])

(def get-meteo "Schema for get-meteo" [:map])

(def next-zoom-table-pos "Schema for next-zoom-table-pos" [:map])

(def focus "Schema for focus" [:map [:cmd [:oneof {:set-value [:map [:set-value :cmd/set-value]], :move [:map [:move :cmd/move]], :halt [:map [:halt :cmd/halt]], :offset [:map [:offset :cmd/offset]], :reset-focus [:map [:reset-focus :cmd/reset-focus]], :save-to-table-focus [:map [:save-to-table-focus :cmd/save-to-table-focus]]}]]])

(def set-value "Schema for set-value" [:map [:value [:and [:maybe :double] [:>= 0] [:<= 1]]]])

(def set-zoom-table-value "Schema for set-zoom-table-value" [:map [:value [:>= 0]]])

(def set-fx-mode "Schema for set-fx-mode" [:map [:mode [:not [:enum [0]]]]])

(def set-iris "Schema for set-iris" [:map [:value [:and [:maybe :double] [:>= 0] [:<= 1]]]])

(def refresh-fx-mode "Schema for refresh-fx-mode" [:map])

(def halt "Schema for halt" [:map])

(def set-auto-iris "Schema for set-auto-iris" [:map [:value [:maybe :boolean]]])

(def save-to-table "Schema for save-to-table" [:map])

(def next-fx-mode "Schema for next-fx-mode" [:map])

(def shift-clahe-level "Schema for shift-clahe-level" [:map [:value [:and [:maybe :double] [:>= -1] [:<= 1]]]])