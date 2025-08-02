(ns potatoclient.specs.cmd.CV "Generated Malli specs from protobuf descriptors" (:require [malli.core :as proto-explorer.spec-generator/m] [malli.generator :as proto-explorer.spec-generator/mg]))

;; Note: FUNCTION-PLACEHOLDER markers indicate where runtime functions are needed
;; These will be replaced with actual implementations when loaded

(def vampire-mode-enable "Schema for vampire-mode-enable" [:map])

(def vampire-mode-disable "Schema for vampire-mode-disable" [:map])

(def root "Schema for root" [:map [:cmd [:oneof {:vampire-mode-enable [:map [:vampire-mode-enable [:maybe :cmd/vampire-mode-enable]]], :vampire-mode-disable [:map [:vampire-mode-disable [:maybe :cmd/vampire-mode-disable]]], :dump-stop [:map [:dump-stop [:maybe :cmd/dump-stop]]], :stabilization-mode-disable [:map [:stabilization-mode-disable [:maybe :cmd/stabilization-mode-disable]]], :set-auto-focus [:map [:set-auto-focus [:maybe :cmd/set-auto-focus]]], :start-track-ndc [:map [:start-track-ndc [:maybe :cmd/start-track-ndc]]], :dump-start [:map [:dump-start [:maybe :cmd/dump-start]]], :stop-track [:map [:stop-track [:maybe :cmd/stop-track]]], :stabilization-mode-enable [:map [:stabilization-mode-enable [:maybe :cmd/stabilization-mode-enable]]]}]]])

(def dump-stop "Schema for dump-stop" [:map])

(def stabilization-mode-disable "Schema for stabilization-mode-disable" [:map])

(def set-auto-focus "Schema for set-auto-focus" [:map [:channel [:not [:enum [0]]]] [:value [:maybe :boolean]]])

(def start-track-ndc "Schema for start-track-ndc" [:map [:channel [:not [:enum [0]]]] [:x [:and [:maybe :double] [:>= -1] [:<= 1]]] [:y [:and [:maybe :double] [:>= -1] [:<= 1]]] [:frame-time [:maybe :int]]])

(def dump-start "Schema for dump-start" [:map])

(def stop-track "Schema for stop-track" [:map])

(def stabilization-mode-enable "Schema for stabilization-mode-enable" [:map])