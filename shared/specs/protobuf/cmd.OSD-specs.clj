(ns potatoclient.specs.cmd.OSD "Generated Malli specs from protobuf descriptors" (:require [malli.core :as proto-explorer.spec-generator/m] [malli.generator :as proto-explorer.spec-generator/mg]))

(def DisableHeatOSD "Schema for DisableHeatOSD" [:map])

(def ShowDefaultScreen "Schema for ShowDefaultScreen" [:map])

(def DisableDayOSD "Schema for DisableDayOSD" [:map])

(def ShowLRFResultScreen "Schema for ShowLRFResultScreen" [:map])

(def EnableHeatOSD "Schema for EnableHeatOSD" [:map])

(def EnableDayOSD "Schema for EnableDayOSD" [:map])

(def ShowLRFMeasureScreen "Schema for ShowLRFMeasureScreen" [:map])

(def Root "Schema for Root" [:map [:cmd [:oneof {:show-default-screen [:map [:show-default-screen [:maybe :cmd/ShowDefaultScreen]]], :show-lrf-measure-screen [:map [:show-lrf-measure-screen [:maybe :cmd/ShowLRFMeasureScreen]]], :show-lrf-result-screen [:map [:show-lrf-result-screen [:maybe :cmd/ShowLRFResultScreen]]], :show-lrf-result-simplified-screen [:map [:show-lrf-result-simplified-screen [:maybe :cmd/ShowLRFResultSimplifiedScreen]]], :enable-heat-osd [:map [:enable-heat-osd [:maybe :cmd/EnableHeatOSD]]], :disable-heat-osd [:map [:disable-heat-osd [:maybe :cmd/DisableHeatOSD]]], :enable-day-osd [:map [:enable-day-osd [:maybe :cmd/EnableDayOSD]]], :disable-day-osd [:map [:disable-day-osd [:maybe :cmd/DisableDayOSD]]]}]]])

(def ShowLRFResultSimplifiedScreen "Schema for ShowLRFResultSimplifiedScreen" [:map])