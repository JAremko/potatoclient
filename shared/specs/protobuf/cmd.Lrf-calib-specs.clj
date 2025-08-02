(ns potatoclient.specs.cmd.Lrf-calib "Generated Malli specs from protobuf descriptors" (:require [malli.core :as proto-explorer.spec-generator/m] [malli.generator :as proto-explorer.spec-generator/mg]))

(def Root "Schema for Root" [:map [:channel [:oneof {:day [:map [:day [:maybe :cmd/Offsets]]], :heat [:map [:heat [:maybe :cmd/Offsets]]]}]]])

(def Offsets "Schema for Offsets" [:map [:cmd [:oneof {:set [:map [:set [:maybe :cmd/SetOffsets]]], :save [:map [:save [:maybe :cmd/SaveOffsets]]], :reset [:map [:reset [:maybe :cmd/ResetOffsets]]], :shift [:map [:shift [:maybe :cmd/ShiftOffsetsBy]]]}]]])

(def SetOffsets "Schema for SetOffsets" [:map [:x [:and [:maybe :int] [:>= -1920] [:<= 1920]]] [:y [:and [:maybe :int] [:>= -1080] [:<= 1080]]]])

(def ShiftOffsetsBy "Schema for ShiftOffsetsBy" [:map [:x [:and [:maybe :int] [:>= -1920] [:<= 1920]]] [:y [:and [:maybe :int] [:>= -1080] [:<= 1080]]]])

(def ResetOffsets "Schema for ResetOffsets" [:map])

(def SaveOffsets "Schema for SaveOffsets" [:map])