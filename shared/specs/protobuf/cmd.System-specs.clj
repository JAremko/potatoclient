(ns potatoclient.specs.cmd.System "Generated Malli specs from protobuf descriptors" (:require [malli.core :as proto-explorer.spec-generator/m] [malli.generator :as proto-explorer.spec-generator/mg]))

(def EnterTransport "Schema for EnterTransport" [:map])

(def EnableGeodesicMode "Schema for EnableGeodesicMode" [:map])

(def UnmarkRecImportant "Schema for UnmarkRecImportant" [:map])

(def ResetConfigs "Schema for ResetConfigs" [:map])

(def StopRec "Schema for StopRec" [:map])

(def MarkRecImportant "Schema for MarkRecImportant" [:map])

(def StartALl "Schema for StartALl" [:map])

(def Reboot "Schema for Reboot" [:map])

(def StartRec "Schema for StartRec" [:map])

(def StopALl "Schema for StopALl" [:map])

(def Root "Schema for Root" [:map [:cmd [:oneof {:geodesic-mode-disable [:map [:geodesic-mode-disable [:maybe :cmd/DisableGeodesicMode]]], :start-all [:map [:start-all [:maybe :cmd/StartALl]]], :geodesic-mode-enable [:map [:geodesic-mode-enable [:maybe :cmd/EnableGeodesicMode]]], :localization [:map [:localization [:maybe :cmd/SetLocalization]]], :unmark-rec-important [:map [:unmark-rec-important [:maybe :cmd/UnmarkRecImportant]]], :stop-rec [:map [:stop-rec [:maybe :cmd/StopRec]]], :reboot [:map [:reboot [:maybe :cmd/Reboot]]], :start-rec [:map [:start-rec [:maybe :cmd/StartRec]]], :power-off [:map [:power-off [:maybe :cmd/PowerOff]]], :reset-configs [:map [:reset-configs [:maybe :cmd/ResetConfigs]]], :stop-all [:map [:stop-all [:maybe :cmd/StopALl]]], :enter-transport [:map [:enter-transport [:maybe :cmd/EnterTransport]]], :mark-rec-important [:map [:mark-rec-important [:maybe :cmd/MarkRecImportant]]]}]]])

(def SetLocalization "Schema for SetLocalization" [:map [:loc [:maybe :ser/JonGuiDataSystemLocalizations]]])

(def DisableGeodesicMode "Schema for DisableGeodesicMode" [:map])

(def PowerOff "Schema for PowerOff" [:map])