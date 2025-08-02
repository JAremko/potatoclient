(ns potatoclient.specs.cmd.Lira "Generated Malli specs from protobuf descriptors" (:require [malli.core :as proto-explorer.spec-generator/m] [malli.generator :as proto-explorer.spec-generator/mg]))

(def Root "Schema for Root" [:map [:cmd [:oneof {:refine-target [:map [:refine-target [:maybe :cmd/Refine-target]]]}]]])

(def Refine-target "Schema for Refine-target" [:map [:target [:maybe :cmd/JonGuiDataLiraTarget]]])

(def JonGuiDataLiraTarget "Schema for JonGuiDataLiraTarget" [:map [:timestamp [:and [:maybe :int] [:>= "0"]]] [:target-longitude [:maybe :double]] [:target-latitude [:maybe :double]] [:target-altitude [:maybe :double]] [:target-azimuth [:maybe :double]] [:target-elevation [:maybe :double]] [:distance [:maybe :double]] [:uuid-part1 [:maybe :int]] [:uuid-part2 [:maybe :int]] [:uuid-part3 [:maybe :int]] [:uuid-part4 [:maybe :int]]])