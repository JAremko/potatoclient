(ns potatoclient.specs.cmd.Lrf "Generated Malli specs from protobuf descriptors" (:require [malli.core :as proto-explorer.spec-generator/m] [malli.generator :as proto-explorer.spec-generator/mg]))

(def Stop "Schema for Stop" [:map])

(def TargetDesignatorOnModeB "Schema for TargetDesignatorOnModeB" [:map])

(def DisableFogMode "Schema for DisableFogMode" [:map])

(def SetScanMode "Schema for SetScanMode" [:map [:mode [:maybe :ser/JonGuiDataLrfScanModes]]])

(def RefineOff "Schema for RefineOff" [:map])

(def NewSession "Schema for NewSession" [:map])

(def RefineOn "Schema for RefineOn" [:map])

(def ScanOn "Schema for ScanOn" [:map])

(def ScanOff "Schema for ScanOff" [:map])

(def Measure "Schema for Measure" [:map])

(def TargetDesignatorOff "Schema for TargetDesignatorOff" [:map])

(def GetMeteo "Schema for GetMeteo" [:map])

(def Root "Schema for Root" [:map [:cmd [:oneof {:target-designator-off [:map [:target-designator-off [:maybe :cmd/TargetDesignatorOff]]], :target-designator-on-mode-b [:map [:target-designator-on-mode-b [:maybe :cmd/TargetDesignatorOnModeB]]], :disable-fog-mode [:map [:disable-fog-mode [:maybe :cmd/DisableFogMode]]], :set-scan-mode [:map [:set-scan-mode [:maybe :cmd/SetScanMode]]], :refine-off [:map [:refine-off [:maybe :cmd/RefineOff]]], :scan-off [:map [:scan-off [:maybe :cmd/ScanOff]]], :refine-on [:map [:refine-on [:maybe :cmd/RefineOn]]], :start [:map [:start [:maybe :cmd/Start]]], :measure [:map [:measure [:maybe :cmd/Measure]]], :scan-on [:map [:scan-on [:maybe :cmd/ScanOn]]], :stop [:map [:stop [:maybe :cmd/Stop]]], :new-session [:map [:new-session [:maybe :cmd/NewSession]]], :get-meteo [:map [:get-meteo [:maybe :cmd/GetMeteo]]], :enable-fog-mode [:map [:enable-fog-mode [:maybe :cmd/EnableFogMode]]], :target-designator-on-mode-a [:map [:target-designator-on-mode-a [:maybe :cmd/TargetDesignatorOnModeA]]]}]]])

(def TargetDesignatorOnModeA "Schema for TargetDesignatorOnModeA" [:map])

(def EnableFogMode "Schema for EnableFogMode" [:map])

(def Start "Schema for Start" [:map])