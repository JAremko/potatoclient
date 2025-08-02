(ns potatoclient.specs.cmd.HeatCamera "Generated Malli specs from protobuf descriptors" (:require [malli.core :as proto-explorer.spec-generator/m] [malli.generator :as proto-explorer.spec-generator/mg]))

(def Calibrate "Schema for Calibrate" [:map])

(def FocusStepMinus "Schema for FocusStepMinus" [:map])

(def FocusOut "Schema for FocusOut" [:map])

(def PrevFxMode "Schema for PrevFxMode" [:map])

(def Stop "Schema for Stop" [:map])

(def ZoomIn "Schema for ZoomIn" [:map])

(def SetCalibMode "Schema for SetCalibMode" [:map])

(def SetZoomTableValue "Schema for SetZoomTableValue" [:map [:value [:and [:maybe :int] [:>= 0]]]])

(def SetDDELevel "Schema for SetDDELevel" [:map [:value [:and [:maybe :int] [:>= 0] [:<= 100]]]])

(def Zoom "Schema for Zoom" [:map [:cmd [:oneof {:set-zoom-table-value [:map [:set-zoom-table-value [:maybe :cmd/SetZoomTableValue]]], :next-zoom-table-pos [:map [:next-zoom-table-pos [:maybe :cmd/NextZoomTablePos]]], :prev-zoom-table-pos [:map [:prev-zoom-table-pos [:maybe :cmd/PrevZoomTablePos]]]}]]])

(def NextZoomTablePos "Schema for NextZoomTablePos" [:map])

(def SetAGC "Schema for SetAGC" [:map [:value [:maybe :ser/JonGuiDataVideoChannelHeatAGCModes]]])

(def SetClaheLevel "Schema for SetClaheLevel" [:map [:value [:and [:maybe :float] [:>= 0] [:<= 1]]]])

(def SetValue "Schema for SetValue" [:map [:value [:and [:maybe :float] [:>= 0] [:<= 1]]]])

(def PrevZoomTablePos "Schema for PrevZoomTablePos" [:map])

(def SetDigitalZoomLevel "Schema for SetDigitalZoomLevel" [:map [:value [:and [:maybe :float] [:>= 1]]]])

(def SetFxMode "Schema for SetFxMode" [:map [:mode [:maybe :ser/JonGuiDataFxModeHeat]]])

(def SaveToTable "Schema for SaveToTable" [:map])

(def Halt "Schema for Halt" [:map])

(def FocusIn "Schema for FocusIn" [:map])

(def RefreshFxMode "Schema for RefreshFxMode" [:map])

(def ShiftDDE "Schema for ShiftDDE" [:map [:value [:and [:maybe :int] [:>= -100] [:<= 100]]]])

(def Photo "Schema for Photo" [:map])

(def SetAutoFocus "Schema for SetAutoFocus" [:map [:value [:maybe :boolean]]])

(def SetFilters "Schema for SetFilters" [:map [:value [:maybe :ser/JonGuiDataVideoChannelHeatFilters]]])

(def FocusStepPlus "Schema for FocusStepPlus" [:map])

(def ResetZoom "Schema for ResetZoom" [:map])

(def ShiftClaheLevel "Schema for ShiftClaheLevel" [:map [:value [:and [:maybe :float] [:>= -1] [:<= 1]]]])

(def GetMeteo "Schema for GetMeteo" [:map])

(def EnableDDE "Schema for EnableDDE" [:map])

(def Root "Schema for Root" [:map [:cmd [:oneof {:set-dde-level [:map [:set-dde-level [:maybe :cmd/SetDDELevel]]], :set-calib-mode [:map [:set-calib-mode [:maybe :cmd/SetCalibMode]]], :zoom [:map [:zoom [:maybe :cmd/Zoom]]], :set-agc [:map [:set-agc [:maybe :cmd/SetAGC]]], :shift-dde [:map [:shift-dde [:maybe :cmd/ShiftDDE]]], :set-filter [:map [:set-filter [:maybe :cmd/SetFilters]]], :set-clahe-level [:map [:set-clahe-level [:maybe :cmd/SetClaheLevel]]], :disable-dde [:map [:disable-dde [:maybe :cmd/DisableDDE]]], :prev-fx-mode [:map [:prev-fx-mode [:maybe :cmd/PrevFxMode]]], :start [:map [:start [:maybe :cmd/Start]]], :focus-step-minus [:map [:focus-step-minus [:maybe :cmd/FocusStepMinus]]], :set-digital-zoom-level [:map [:set-digital-zoom-level [:maybe :cmd/SetDigitalZoomLevel]]], :enable-dde [:map [:enable-dde [:maybe :cmd/EnableDDE]]], :focus-stop [:map [:focus-stop [:maybe :cmd/FocusStop]]], :stop [:map [:stop [:maybe :cmd/Stop]]], :reset-zoom [:map [:reset-zoom [:maybe :cmd/ResetZoom]]], :zoom-out [:map [:zoom-out [:maybe :cmd/ZoomOut]]], :photo [:map [:photo [:maybe :cmd/Photo]]], :zoom-in [:map [:zoom-in [:maybe :cmd/ZoomIn]]], :get-meteo [:map [:get-meteo [:maybe :cmd/GetMeteo]]], :focus-step-plus [:map [:focus-step-plus [:maybe :cmd/FocusStepPlus]]], :set-fx-mode [:map [:set-fx-mode [:maybe :cmd/SetFxMode]]], :refresh-fx-mode [:map [:refresh-fx-mode [:maybe :cmd/RefreshFxMode]]], :focus-out [:map [:focus-out [:maybe :cmd/FocusOut]]], :set-auto-focus [:map [:set-auto-focus [:maybe :cmd/SetAutoFocus]]], :zoom-stop [:map [:zoom-stop [:maybe :cmd/ZoomStop]]], :save-to-table [:map [:save-to-table [:maybe :cmd/SaveToTable]]], :next-fx-mode [:map [:next-fx-mode [:maybe :cmd/NextFxMode]]], :calibrate [:map [:calibrate [:maybe :cmd/Calibrate]]], :shift-clahe-level [:map [:shift-clahe-level [:maybe :cmd/ShiftClaheLevel]]], :focus-in [:map [:focus-in [:maybe :cmd/FocusIn]]]}]]])

(def NextFxMode "Schema for NextFxMode" [:map])

(def FocusStop "Schema for FocusStop" [:map])

(def Start "Schema for Start" [:map])

(def DisableDDE "Schema for DisableDDE" [:map])

(def ZoomStop "Schema for ZoomStop" [:map])

(def ZoomOut "Schema for ZoomOut" [:map])