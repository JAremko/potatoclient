(ns potatoclient.specs.cmd.DayCamGlassHeater "Generated Malli specs from protobuf descriptors" (:require [malli.core :as proto-explorer.spec-generator/m] [malli.generator :as proto-explorer.spec-generator/mg]))

(def Root "Schema for Root" [:map [:cmd [:oneof {:start [:map [:start [:maybe :cmd/Start]]], :stop [:map [:stop [:maybe :cmd/Stop]]], :turn-on [:map [:turn-on [:maybe :cmd/TurnOn]]], :turn-off [:map [:turn-off [:maybe :cmd/TurnOff]]], :get-meteo [:map [:get-meteo [:maybe :cmd/GetMeteo]]]}]]])

(def Start "Schema for Start" [:map])

(def Stop "Schema for Stop" [:map])

(def TurnOn "Schema for TurnOn" [:map])

(def TurnOff "Schema for TurnOff" [:map])

(def GetMeteo "Schema for GetMeteo" [:map])