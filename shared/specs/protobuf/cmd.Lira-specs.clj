(ns potatoclient.specs.cmd.Lira "Generated Malli specs from protobuf descriptors" (:require [malli.core :as proto-explorer.spec-generator/m] [malli.generator :as proto-explorer.spec-generator/mg]))

;; Note: FUNCTION-PLACEHOLDER markers indicate where runtime functions are needed
;; These will be replaced with actual implementations when loaded

(def root "Schema for root" [:map [:cmd [:oneof {:refine-target [:map [:refine-target :cmd/refine_target]]}]]])

(def refine_target "Schema for refine_target" [:map [:target [:maybe :cmd/jon-gui-data-lira-target]]])

(def jon-gui-data-lira-target "Schema for jon-gui-data-lira-target" [:map [:timestamp [:and [:maybe :int] [:>= 0.0]]] [:target-longitude [:maybe :double]] [:target-latitude [:maybe :double]] [:target-altitude [:maybe :double]] [:target-azimuth [:maybe :double]] [:target-elevation [:maybe :double]] [:distance [:maybe :double]] [:uuid-part1 [:maybe :int]] [:uuid-part2 [:maybe :int]] [:uuid-part3 [:maybe :int]] [:uuid-part4 [:maybe :int]]])