(ns potatoclient.specs.cmd.Lrf
  "Generated Malli specs from protobuf descriptors"
  (:require [malli.core :as m]
            [malli.generator :as mg]))

;; Note: FUNCTION-PLACEHOLDER markers indicate where runtime functions are needed
;; These will be replaced with actual implementations when loaded

(def target-designator-off "Schema for target-designator-off" [:map])


(def target-designator-on-mode-b "Schema for target-designator-on-mode-b" [:map])


(def disable-fog-mode "Schema for disable-fog-mode" [:map])


(def set-scan-mode "Schema for set-scan-mode" [:map [:mode [:not [:enum [0]]]]])


(def refine-off "Schema for refine-off" [:map])


(def scan-off "Schema for scan-off" [:map])


(def refine-on "Schema for refine-on" [:map])


(def start "Schema for start" [:map])


(def measure "Schema for measure" [:map])


(def scan-on "Schema for scan-on" [:map])


(def stop "Schema for stop" [:map])


(def new-session "Schema for new-session" [:map])


(def root
  "Schema for root"
  [:map
   [:cmd
    [:oneof
     {:target-designator-off [:map [:target-designator-off :cmd/target-designator-off]],
      :target-designator-on-mode-b
        [:map [:target-designator-on-mode-b :cmd/target-designator-on-mode-b]],
      :disable-fog-mode [:map [:disable-fog-mode :cmd/disable-fog-mode]],
      :set-scan-mode [:map [:set-scan-mode :cmd/set-scan-mode]],
      :refine-off [:map [:refine-off :cmd/refine-off]],
      :scan-off [:map [:scan-off :cmd/scan-off]],
      :refine-on [:map [:refine-on :cmd/refine-on]],
      :start [:map [:start :cmd/start]],
      :measure [:map [:measure :cmd/measure]],
      :scan-on [:map [:scan-on :cmd/scan-on]],
      :stop [:map [:stop :cmd/stop]],
      :new-session [:map [:new-session :cmd/new-session]],
      :get-meteo [:map [:get-meteo :cmd/get-meteo]],
      :enable-fog-mode [:map [:enable-fog-mode :cmd/enable-fog-mode]],
      :target-designator-on-mode-a
        [:map [:target-designator-on-mode-a :cmd/target-designator-on-mode-a]]}]]])


(def get-meteo "Schema for get-meteo" [:map])


(def enable-fog-mode "Schema for enable-fog-mode" [:map])


(def target-designator-on-mode-a "Schema for target-designator-on-mode-a" [:map])
