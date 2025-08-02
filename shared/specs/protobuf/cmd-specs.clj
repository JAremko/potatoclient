(ns potatoclient.specs.cmd "Generated Malli specs from protobuf descriptors" (:require [malli.core :as proto-explorer.spec-generator/m] [malli.generator :as proto-explorer.spec-generator/mg]))

(def Root "Schema for Root" [:map [:protocol-version [:and [:maybe :int] [:> 0]]] [:session-id [:maybe :int]] [:important [:maybe :boolean]] [:from-cv-subsystem [:maybe :boolean]] [:client-type [:maybe :ser/JonGuiDataClientType]] [:payload [:oneof {:osd [:map [:osd [:maybe :cmd/Root]]], :ping [:map [:ping [:maybe :cmd/Ping]]], :system [:map [:system [:maybe :cmd/Root]]], :noop [:map [:noop [:maybe :cmd/Noop]]], :cv [:map [:cv [:maybe :cmd/Root]]], :gps [:map [:gps [:maybe :cmd/Root]]], :lrf [:map [:lrf [:maybe :cmd/Root]]], :day-cam-glass-heater [:map [:day-cam-glass-heater [:maybe :cmd/Root]]], :day-camera [:map [:day-camera [:maybe :cmd/Root]]], :heat-camera [:map [:heat-camera [:maybe :cmd/Root]]], :lira [:map [:lira [:maybe :cmd/Root]]], :lrf-calib [:map [:lrf-calib [:maybe :cmd/Root]]], :rotary [:map [:rotary [:maybe :cmd/Root]]], :compass [:map [:compass [:maybe :cmd/Root]]], :frozen [:map [:frozen [:maybe :cmd/Frozen]]]}]]])

(def Ping "Schema for Ping" [:map])

(def Noop "Schema for Noop" [:map])

(def Frozen "Schema for Frozen" [:map])