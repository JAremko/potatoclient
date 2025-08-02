(ns potatoclient.specs.cmd "Generated Malli specs from protobuf descriptors" (:require [malli.core :as proto-explorer.spec-generator/m] [malli.generator :as proto-explorer.spec-generator/mg]))

;; Note: FUNCTION-PLACEHOLDER markers indicate where runtime functions are needed
;; These will be replaced with actual implementations when loaded

(def root "Schema for root" [:map [:protocol-version [:maybe :int]] [:session-id [:maybe :int]] [:important [:maybe :boolean]] [:from-cv-subsystem [:maybe :boolean]] [:client-type [:not [:enum [0]]]] [:payload [:oneof {:osd [:map [:osd [:maybe :cmd/root]]], :ping [:map [:ping [:maybe :cmd/ping]]], :system [:map [:system [:maybe :cmd/root]]], :noop [:map [:noop [:maybe :cmd/noop]]], :cv [:map [:cv [:maybe :cmd/root]]], :gps [:map [:gps [:maybe :cmd/root]]], :lrf [:map [:lrf [:maybe :cmd/root]]], :day-cam-glass-heater [:map [:day-cam-glass-heater [:maybe :cmd/root]]], :day-camera [:map [:day-camera [:maybe :cmd/root]]], :heat-camera [:map [:heat-camera [:maybe :cmd/root]]], :lira [:map [:lira [:maybe :cmd/root]]], :lrf-calib [:map [:lrf-calib [:maybe :cmd/root]]], :rotary [:map [:rotary [:maybe :cmd/root]]], :compass [:map [:compass [:maybe :cmd/root]]], :frozen [:map [:frozen [:maybe :cmd/frozen]]]}]]])

(def ping "Schema for ping" [:map])

(def noop "Schema for noop" [:map])

(def frozen "Schema for frozen" [:map])