(ns potatoclient.specs.cmd.DayCamera "Generated Malli specs from protobuf descriptors" (:require [malli.core :as proto-explorer.spec-generator/m] [malli.generator :as proto-explorer.spec-generator/mg]))

(def PrevFxMode "Schema for PrevFxMode" [:map])

(def Stop "Schema for Stop" [:map])

(def SetZoomTableValue "Schema for SetZoomTableValue" [:map [:value [:and [:maybe :int] [:>= 0]]]])

(def Offset "Schema for Offset" [:map [:offset-value [:and [:maybe :float] [:>= -1] [:<= 1]]]])

(def Zoom "Schema for Zoom" [:map [:cmd [:oneof {:prev-zoom-table-pos [:map [:prev-zoom-table-pos [:maybe :cmd/PrevZoomTablePos]]], :offset [:map [:offset [:maybe :cmd/Offset]]], :move [:map [:move [:maybe :cmd/Move]]], :reset-zoom [:map [:reset-zoom [:maybe :cmd/ResetZoom]]], :next-zoom-table-pos [:map [:next-zoom-table-pos [:maybe :cmd/NextZoomTablePos]]], :set-value [:map [:set-value [:maybe :cmd/SetValue]]], :set-zoom-table-value [:map [:set-zoom-table-value [:maybe :cmd/SetZoomTableValue]]], :halt [:map [:halt [:maybe :cmd/Halt]]], :save-to-table [:map [:save-to-table [:maybe :cmd/SaveToTable]]]}]]])

(def NextZoomTablePos "Schema for NextZoomTablePos" [:map])

(def SaveToTableFocus "Schema for SaveToTableFocus" [:map])

(def SetClaheLevel "Schema for SetClaheLevel" [:map [:value [:and [:maybe :float] [:>= 0] [:<= 1]]]])

(def SetValue "Schema for SetValue" [:map [:value [:and [:maybe :float] [:>= 0] [:<= 1]]]])

(def PrevZoomTablePos "Schema for PrevZoomTablePos" [:map])

(def SetDigitalZoomLevel "Schema for SetDigitalZoomLevel" [:map [:value [:and [:maybe :float] [:>= 1]]]])

(def SetFxMode "Schema for SetFxMode" [:map [:mode [:maybe :ser/JonGuiDataFxModeDay]]])

(def SaveToTable "Schema for SaveToTable" [:map])

(def Halt "Schema for Halt" [:map])

(def RefreshFxMode "Schema for RefreshFxMode" [:map])

(def Move "Schema for Move" [:map [:target-value [:and [:maybe :float] [:>= 0] [:<= 1]]] [:speed [:and [:maybe :float] [:>= 0] [:<= 1]]]])

(def SetInfraRedFilter "Schema for SetInfraRedFilter" [:map [:value [:maybe :boolean]]])

(def SetAutoIris "Schema for SetAutoIris" [:map [:value [:maybe :boolean]]])

(def Photo "Schema for Photo" [:map])

(def ResetZoom "Schema for ResetZoom" [:map])

(def ShiftClaheLevel "Schema for ShiftClaheLevel" [:map [:value [:and [:maybe :float] [:>= -1] [:<= 1]]]])

(def GetPos "Schema for GetPos" [:map])

(def GetMeteo "Schema for GetMeteo" [:map])

(def Root "Schema for Root" [:map [:cmd [:oneof {:zoom [:map [:zoom [:maybe :cmd/Zoom]]], :set-infra-red-filter [:map [:set-infra-red-filter [:maybe :cmd/SetInfraRedFilter]]], :set-clahe-level [:map [:set-clahe-level [:maybe :cmd/SetClaheLevel]]], :prev-fx-mode [:map [:prev-fx-mode [:maybe :cmd/PrevFxMode]]], :start [:map [:start [:maybe :cmd/Start]]], :halt-all [:map [:halt-all [:maybe :cmd/HaltAll]]], :set-digital-zoom-level [:map [:set-digital-zoom-level [:maybe :cmd/SetDigitalZoomLevel]]], :stop [:map [:stop [:maybe :cmd/Stop]]], :photo [:map [:photo [:maybe :cmd/Photo]]], :get-meteo [:map [:get-meteo [:maybe :cmd/GetMeteo]]], :focus [:map [:focus [:maybe :cmd/Focus]]], :set-fx-mode [:map [:set-fx-mode [:maybe :cmd/SetFxMode]]], :set-iris [:map [:set-iris [:maybe :cmd/SetIris]]], :refresh-fx-mode [:map [:refresh-fx-mode [:maybe :cmd/RefreshFxMode]]], :set-auto-iris [:map [:set-auto-iris [:maybe :cmd/SetAutoIris]]], :next-fx-mode [:map [:next-fx-mode [:maybe :cmd/NextFxMode]]], :shift-clahe-level [:map [:shift-clahe-level [:maybe :cmd/ShiftClaheLevel]]]}]]])

(def NextFxMode "Schema for NextFxMode" [:map])

(def ResetFocus "Schema for ResetFocus" [:map])

(def Start "Schema for Start" [:map])

(def HaltAll "Schema for HaltAll" [:map])

(def Focus "Schema for Focus" [:map [:cmd [:oneof {:set-value [:map [:set-value [:maybe :cmd/SetValue]]], :move [:map [:move [:maybe :cmd/Move]]], :halt [:map [:halt [:maybe :cmd/Halt]]], :offset [:map [:offset [:maybe :cmd/Offset]]], :reset-focus [:map [:reset-focus [:maybe :cmd/ResetFocus]]], :save-to-table-focus [:map [:save-to-table-focus [:maybe :cmd/SaveToTableFocus]]]}]]])

(def SetIris "Schema for SetIris" [:map [:value [:and [:maybe :float] [:>= 0] [:<= 1]]]])