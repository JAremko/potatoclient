(ns potatoclient.specs.cmd.Gps "Generated Malli specs from protobuf descriptors" (:require [malli.core :as proto-explorer.spec-generator/m] [malli.generator :as proto-explorer.spec-generator/mg]))

(def Root "Schema for Root" [:map [:cmd [:oneof {:start [:map [:start [:maybe :cmd/Start]]], :stop [:map [:stop [:maybe :cmd/Stop]]], :set-manual-position [:map [:set-manual-position [:maybe :cmd/SetManualPosition]]], :set-use-manual-position [:map [:set-use-manual-position [:maybe :cmd/SetUseManualPosition]]], :get-meteo [:map [:get-meteo [:maybe :cmd/GetMeteo]]]}]]])

(def Start "Schema for Start" [:map])

(def Stop "Schema for Stop" [:map])

(def GetMeteo "Schema for GetMeteo" [:map])

(def SetUseManualPosition "Schema for SetUseManualPosition" [:map [:flag [:maybe :boolean]]])

(def SetManualPosition "Schema for SetManualPosition" [:map [:latitude [:and [:maybe :float] [:>= -90] [:<= 90]]] [:longitude [:and [:maybe :float] [:>= -180] [:< 180]]] [:altitude [:and [:maybe :float] [:>= -432] [:<= 8848]]]])