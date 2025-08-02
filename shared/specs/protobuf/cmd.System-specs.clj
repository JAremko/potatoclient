(ns potatoclient.specs.cmd.System "Generated Malli specs from protobuf descriptors" (:require [malli.core :as proto-explorer.spec-generator/m] [malli.generator :as proto-explorer.spec-generator/mg]))

;; Note: FUNCTION-PLACEHOLDER markers indicate where runtime functions are needed
;; These will be replaced with actual implementations when loaded

(def enable-geodesic-mode "Schema for enable-geodesic-mode" [:map])

(def unmark-rec-important "Schema for unmark-rec-important" [:map])

(def stop-rec "Schema for stop-rec" [:map])

(def stop-a-ll "Schema for stop-a-ll" [:map])

(def reboot "Schema for reboot" [:map])

(def root "Schema for root" [:map [:cmd [:oneof {:geodesic-mode-disable [:map [:geodesic-mode-disable [:maybe :cmd/disable-geodesic-mode]]], :start-all [:map [:start-all [:maybe :cmd/start-a-ll]]], :geodesic-mode-enable [:map [:geodesic-mode-enable [:maybe :cmd/enable-geodesic-mode]]], :localization [:map [:localization [:maybe :cmd/set-localization]]], :unmark-rec-important [:map [:unmark-rec-important [:maybe :cmd/unmark-rec-important]]], :stop-rec [:map [:stop-rec [:maybe :cmd/stop-rec]]], :reboot [:map [:reboot [:maybe :cmd/reboot]]], :start-rec [:map [:start-rec [:maybe :cmd/start-rec]]], :power-off [:map [:power-off [:maybe :cmd/power-off]]], :reset-configs [:map [:reset-configs [:maybe :cmd/reset-configs]]], :stop-all [:map [:stop-all [:maybe :cmd/stop-a-ll]]], :enter-transport [:map [:enter-transport [:maybe :cmd/enter-transport]]], :mark-rec-important [:map [:mark-rec-important [:maybe :cmd/mark-rec-important]]]}]]])

(def start-rec "Schema for start-rec" [:map])

(def power-off "Schema for power-off" [:map])

(def set-localization "Schema for set-localization" [:map [:loc [:not [:enum [0]]]]])

(def reset-configs "Schema for reset-configs" [:map])

(def disable-geodesic-mode "Schema for disable-geodesic-mode" [:map])

(def enter-transport "Schema for enter-transport" [:map])

(def mark-rec-important "Schema for mark-rec-important" [:map])

(def start-a-ll "Schema for start-a-ll" [:map])