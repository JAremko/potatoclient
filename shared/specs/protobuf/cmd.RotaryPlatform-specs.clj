(ns potatoclient.specs.cmd.RotaryPlatform "Generated Malli specs from protobuf descriptors" (:require [malli.core :as proto-explorer.spec-generator/m] [malli.generator :as proto-explorer.spec-generator/mg]))

(def setUseRotaryAsCompass "Schema for setUseRotaryAsCompass" [:map [:flag [:maybe :boolean]]])

(def SetMode "Schema for SetMode" [:map [:mode [:maybe :ser/JonGuiDataRotaryMode]]])

(def HaltAzimuth "Schema for HaltAzimuth" [:map])

(def Stop "Schema for Stop" [:map])

(def Axis "Schema for Axis" [:map [:azimuth [:maybe :cmd/Azimuth]] [:elevation [:maybe :cmd/Elevation]]])

(def RotateAzimuthRelative "Schema for RotateAzimuthRelative" [:map [:value [:maybe :float]] [:speed [:and [:maybe :float] [:>= 0] [:<= 1]]] [:direction [:maybe :ser/JonGuiDataRotaryDirection]]])

(def RotateElevationTo "Schema for RotateElevationTo" [:map [:target-value [:and [:maybe :float] [:>= -90] [:<= 90]]] [:speed [:and [:maybe :float] [:>= 0] [:<= 1]]]])

(def Azimuth "Schema for Azimuth" [:map [:cmd [:oneof {:set-value [:map [:set-value [:maybe :cmd/SetAzimuthValue]]], :rotate-to [:map [:rotate-to [:maybe :cmd/RotateAzimuthTo]]], :rotate [:map [:rotate [:maybe :cmd/RotateAzimuth]]], :relative [:map [:relative [:maybe :cmd/RotateAzimuthRelative]]], :relative-set [:map [:relative-set [:maybe :cmd/RotateAzimuthRelativeSet]]], :halt [:map [:halt [:maybe :cmd/HaltAzimuth]]]}]]])

(def RotateAzimuthRelativeSet "Schema for RotateAzimuthRelativeSet" [:map [:value [:maybe :float]] [:direction [:maybe :ser/JonGuiDataRotaryDirection]]])

(def SetPlatformElevation "Schema for SetPlatformElevation" [:map [:value [:and [:maybe :float] [:>= -90] [:<= 90]]]])

(def ScanAddNode "Schema for ScanAddNode" [:map [:index [:and [:maybe :int] [:>= 0]]] [:DayZoomTableValue [:and [:maybe :int] [:>= 0]]] [:HeatZoomTableValue [:and [:maybe :int] [:>= 0]]] [:azimuth [:maybe :double]] [:elevation [:maybe :double]] [:linger [:maybe :double]] [:speed [:maybe :double]]])

(def HaltElevation "Schema for HaltElevation" [:map])

(def SetPlatformAzimuth "Schema for SetPlatformAzimuth" [:map [:value [:and [:maybe :float] [:> -360] [:< 360]]]])

(def ScanStart "Schema for ScanStart" [:map])

(def RotateElevationRelative "Schema for RotateElevationRelative" [:map [:value [:maybe :float]] [:speed [:and [:maybe :float] [:>= 0] [:<= 1]]] [:direction [:maybe :ser/JonGuiDataRotaryDirection]]])

(def SetAzimuthValue "Schema for SetAzimuthValue" [:map [:value [:and [:maybe :float] [:>= 0] [:< 360]]] [:direction [:maybe :ser/JonGuiDataRotaryDirection]]])

(def SetElevationValue "Schema for SetElevationValue" [:map [:value [:and [:maybe :float] [:>= -90] [:<= 90]]]])

(def RotateElevation "Schema for RotateElevation" [:map [:speed [:and [:maybe :float] [:>= 0] [:<= 1]]] [:direction [:maybe :ser/JonGuiDataRotaryDirection]]])

(def RotateElevationRelativeSet "Schema for RotateElevationRelativeSet" [:map [:value [:maybe :float]] [:direction [:maybe :ser/JonGuiDataRotaryDirection]]])

(def ScanStop "Schema for ScanStop" [:map])

(def Halt "Schema for Halt" [:map])

(def ScanNext "Schema for ScanNext" [:map])

(def SetPlatformBank "Schema for SetPlatformBank" [:map [:value [:and [:maybe :float] [:>= -180] [:< 180]]]])

(def ScanUnpause "Schema for ScanUnpause" [:map])

(def ScanDeleteNode "Schema for ScanDeleteNode" [:map [:index [:and [:maybe :int] [:>= 0]]]])

(def ScanRefreshNodeList "Schema for ScanRefreshNodeList" [:map])

(def ScanSelectNode "Schema for ScanSelectNode" [:map [:index [:and [:maybe :int] [:>= 0]]]])

(def RotateAzimuth "Schema for RotateAzimuth" [:map [:speed [:and [:maybe :float] [:>= 0] [:<= 1]]] [:direction [:maybe :ser/JonGuiDataRotaryDirection]]])

(def GetMeteo "Schema for GetMeteo" [:map])

(def Root "Schema for Root" [:map [:cmd [:oneof {:rotate-to-gps [:map [:rotate-to-gps [:maybe :cmd/RotateToGPS]]], :scan-pause [:map [:scan-pause [:maybe :cmd/ScanPause]]], :rotate-to-ndc [:map [:rotate-to-ndc [:maybe :cmd/RotateToNDC]]], :scan-start [:map [:scan-start [:maybe :cmd/ScanStart]]], :set-platform-azimuth [:map [:set-platform-azimuth [:maybe :cmd/SetPlatformAzimuth]]], :scan-stop [:map [:scan-stop [:maybe :cmd/ScanStop]]], :start [:map [:start [:maybe :cmd/Start]]], :stop [:map [:stop [:maybe :cmd/Stop]]], :set-origin-gps [:map [:set-origin-gps [:maybe :cmd/SetOriginGPS]]], :scan-next [:map [:scan-next [:maybe :cmd/ScanNext]]], :set-platform-bank [:map [:set-platform-bank [:maybe :cmd/SetPlatformBank]]], :get-meteo [:map [:get-meteo [:maybe :cmd/GetMeteo]]], :set-use-rotary-as-compass [:map [:set-use-rotary-as-compass [:maybe :cmd/setUseRotaryAsCompass]]], :scan-prev [:map [:scan-prev [:maybe :cmd/ScanPrev]]], :scan-add-node [:map [:scan-add-node [:maybe :cmd/ScanAddNode]]], :set-platform-elevation [:map [:set-platform-elevation [:maybe :cmd/SetPlatformElevation]]], :scan-select-node [:map [:scan-select-node [:maybe :cmd/ScanSelectNode]]], :halt [:map [:halt [:maybe :cmd/Halt]]], :scan-delete-node [:map [:scan-delete-node [:maybe :cmd/ScanDeleteNode]]], :axis [:map [:axis [:maybe :cmd/Axis]]], :scan-unpause [:map [:scan-unpause [:maybe :cmd/ScanUnpause]]], :set-mode [:map [:set-mode [:maybe :cmd/SetMode]]], :scan-refresh-node-list [:map [:scan-refresh-node-list [:maybe :cmd/ScanRefreshNodeList]]], :scan-update-node [:map [:scan-update-node [:maybe :cmd/ScanUpdateNode]]]}]]])

(def RotateToNDC "Schema for RotateToNDC" [:map [:channel [:maybe :ser/JonGuiDataVideoChannel]] [:x [:and [:maybe :float] [:>= -1] [:<= 1]]] [:y [:and [:maybe :float] [:>= -1] [:<= 1]]]])

(def RotateToGPS "Schema for RotateToGPS" [:map [:latitude [:and [:maybe :float] [:>= -90] [:<= 90]]] [:longitude [:and [:maybe :float] [:>= -180] [:< 180]]] [:altitude [:maybe :float]]])

(def Elevation "Schema for Elevation" [:map [:cmd [:oneof {:set-value [:map [:set-value [:maybe :cmd/SetElevationValue]]], :rotate-to [:map [:rotate-to [:maybe :cmd/RotateElevationTo]]], :rotate [:map [:rotate [:maybe :cmd/RotateElevation]]], :relative [:map [:relative [:maybe :cmd/RotateElevationRelative]]], :relative-set [:map [:relative-set [:maybe :cmd/RotateElevationRelativeSet]]], :halt [:map [:halt [:maybe :cmd/HaltElevation]]]}]]])

(def ScanUpdateNode "Schema for ScanUpdateNode" [:map [:index [:and [:maybe :int] [:>= 0]]] [:DayZoomTableValue [:and [:maybe :int] [:>= 0]]] [:HeatZoomTableValue [:and [:maybe :int] [:>= 0]]] [:azimuth [:maybe :double]] [:elevation [:maybe :double]] [:linger [:maybe :double]] [:speed [:maybe :double]]])

(def ScanPrev "Schema for ScanPrev" [:map])

(def ScanPause "Schema for ScanPause" [:map])

(def RotateAzimuthTo "Schema for RotateAzimuthTo" [:map [:target-value [:and [:maybe :float] [:>= 0] [:< 360]]] [:speed [:and [:maybe :float] [:>= 0] [:<= 1]]] [:direction [:maybe :ser/JonGuiDataRotaryDirection]]])

(def SetOriginGPS "Schema for SetOriginGPS" [:map [:latitude [:and [:maybe :float] [:>= -90] [:<= 90]]] [:longitude [:and [:maybe :float] [:>= -180] [:< 180]]] [:altitude [:maybe :float]]])

(def Start "Schema for Start" [:map])