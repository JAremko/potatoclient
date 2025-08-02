(ns potatoclient.specs.cmd.Compass "Generated Malli specs from protobuf descriptors" (:require [malli.core :as proto-explorer.spec-generator/m] [malli.generator :as proto-explorer.spec-generator/mg]))

(def CalibrateCencel "Schema for CalibrateCencel" [:map])

(def SetOffsetAngleAzimuth "Schema for SetOffsetAngleAzimuth" [:map [:value [:and [:maybe :float] [:>= -180] [:< 180]]]])

(def Stop "Schema for Stop" [:map])

(def CalibrateStartShort "Schema for CalibrateStartShort" [:map])

(def SetMagneticDeclination "Schema for SetMagneticDeclination" [:map [:value [:and [:maybe :float] [:>= -180] [:< 180]]]])

(def CalibrateStartLong "Schema for CalibrateStartLong" [:map])

(def CalibrateNext "Schema for CalibrateNext" [:map])

(def Next "Schema for Next" [:map])

(def SetUseRotaryPosition "Schema for SetUseRotaryPosition" [:map [:flag [:maybe :boolean]]])

(def GetMeteo "Schema for GetMeteo" [:map])

(def Root "Schema for Root" [:map [:cmd [:oneof {:calibrate-cencel [:map [:calibrate-cencel [:maybe :cmd/CalibrateCencel]]], :start [:map [:start [:maybe :cmd/Start]]], :set-offset-angle-elevation [:map [:set-offset-angle-elevation [:maybe :cmd/SetOffsetAngleElevation]]], :stop [:map [:stop [:maybe :cmd/Stop]]], :calibrate-next [:map [:calibrate-next [:maybe :cmd/CalibrateNext]]], :get-meteo [:map [:get-meteo [:maybe :cmd/GetMeteo]]], :set-use-rotary-position [:map [:set-use-rotary-position [:maybe :cmd/SetUseRotaryPosition]]], :set-magnetic-declination [:map [:set-magnetic-declination [:maybe :cmd/SetMagneticDeclination]]], :start-calibrate-short [:map [:start-calibrate-short [:maybe :cmd/CalibrateStartShort]]], :start-calibrate-long [:map [:start-calibrate-long [:maybe :cmd/CalibrateStartLong]]], :set-offset-angle-azimuth [:map [:set-offset-angle-azimuth [:maybe :cmd/SetOffsetAngleAzimuth]]]}]]])

(def Start "Schema for Start" [:map])

(def SetOffsetAngleElevation "Schema for SetOffsetAngleElevation" [:map [:value [:and [:maybe :float] [:>= -90] [:<= 90]]]])