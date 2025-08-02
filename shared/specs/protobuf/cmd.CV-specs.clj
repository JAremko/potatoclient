(ns potatoclient.specs.cmd.CV "Generated Malli specs from protobuf descriptors" (:require [malli.core :as proto-explorer.spec-generator/m] [malli.generator :as proto-explorer.spec-generator/mg]))

(def StartTrackNDC "Schema for StartTrackNDC" [:map [:channel [:maybe :ser/JonGuiDataVideoChannel]] [:x [:and [:maybe :float] [:>= -1] [:<= 1]]] [:y [:and [:maybe :float] [:>= -1] [:<= 1]]] [:frame-time [:maybe :int]]])

(def VampireModeEnable "Schema for VampireModeEnable" [:map])

(def SetAutoFocus "Schema for SetAutoFocus" [:map [:channel [:maybe :ser/JonGuiDataVideoChannel]] [:value [:maybe :boolean]]])

(def VampireModeDisable "Schema for VampireModeDisable" [:map])

(def Root "Schema for Root" [:map [:cmd [:oneof {:vampire-mode-enable [:map [:vampire-mode-enable [:maybe :cmd/VampireModeEnable]]], :vampire-mode-disable [:map [:vampire-mode-disable [:maybe :cmd/VampireModeDisable]]], :dump-stop [:map [:dump-stop [:maybe :cmd/DumpStop]]], :stabilization-mode-disable [:map [:stabilization-mode-disable [:maybe :cmd/StabilizationModeDisable]]], :set-auto-focus [:map [:set-auto-focus [:maybe :cmd/SetAutoFocus]]], :start-track-ndc [:map [:start-track-ndc [:maybe :cmd/StartTrackNDC]]], :dump-start [:map [:dump-start [:maybe :cmd/DumpStart]]], :stop-track [:map [:stop-track [:maybe :cmd/StopTrack]]], :stabilization-mode-enable [:map [:stabilization-mode-enable [:maybe :cmd/StabilizationModeEnable]]]}]]])

(def StabilizationModeEnable "Schema for StabilizationModeEnable" [:map])

(def DumpStop "Schema for DumpStop" [:map])

(def StabilizationModeDisable "Schema for StabilizationModeDisable" [:map])

(def DumpStart "Schema for DumpStart" [:map])

(def StopTrack "Schema for StopTrack" [:map])