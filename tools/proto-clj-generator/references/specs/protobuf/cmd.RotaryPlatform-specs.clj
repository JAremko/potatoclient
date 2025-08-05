(ns potatoclient.specs.cmd.RotaryPlatform
  "Generated Malli specs from protobuf descriptors"
  (:require [malli.core :as m]
            [malli.generator :as mg]))

;; Note: FUNCTION-PLACEHOLDER markers indicate where runtime functions are needed
;; These will be replaced with actual implementations when loaded

(def rotate-to-gps
  "Schema for rotate-to-gps"
  [:map [:latitude [:and [:maybe :double] [:>= -90] [:<= 90]]]
   [:longitude [:and [:maybe :double] [:>= -180] [:< 180]]] [:altitude [:maybe :double]]])


(def rotate-azimuth
  "Schema for rotate-azimuth"
  [:map [:speed [:and [:maybe :double] [:>= 0] [:<= 1]]] [:direction [:not [:enum [0]]]]])


(def rotate-elevation-to
  "Schema for rotate-elevation-to"
  [:map [:target-value [:and [:maybe :double] [:>= -90] [:<= 90]]]
   [:speed [:and [:maybe :double] [:>= 0] [:<= 1]]]])


(def scan-pause "Schema for scan-pause" [:map])


(def rotate-to-ndc
  "Schema for rotate-to-ndc"
  [:map [:channel [:not [:enum [0]]]] [:x [:and [:maybe :double] [:>= -1] [:<= 1]]]
   [:y [:and [:maybe :double] [:>= -1] [:<= 1]]]])


(def halt-elevation "Schema for halt-elevation" [:map])


(def scan-start "Schema for scan-start" [:map])


(def set-elevation-value
  "Schema for set-elevation-value"
  [:map [:value [:and [:maybe :double] [:>= -90] [:<= 90]]]])


(def elevation
  "Schema for elevation"
  [:map
   [:cmd
    [:oneof
     {:set-value [:map [:set-value :cmd/set-elevation-value]],
      :rotate-to [:map [:rotate-to :cmd/rotate-elevation-to]],
      :rotate [:map [:rotate :cmd/rotate-elevation]],
      :relative [:map [:relative :cmd/rotate-elevation-relative]],
      :relative-set [:map [:relative-set :cmd/rotate-elevation-relative-set]],
      :halt [:map [:halt :cmd/halt-elevation]]}]]])


(def rotate-elevation-relative
  "Schema for rotate-elevation-relative"
  [:map [:value [:maybe :double]] [:speed [:and [:maybe :double] [:>= 0] [:<= 1]]]
   [:direction [:not [:enum [0]]]]])


(def set-platform-azimuth
  "Schema for set-platform-azimuth"
  [:map [:value [:and [:maybe :double] [:> -360] [:< 360]]]])


(def scan-stop "Schema for scan-stop" [:map])


(def halt-azimuth "Schema for halt-azimuth" [:map])


(def start "Schema for start" [:map])


(def azimuth
  "Schema for azimuth"
  [:map
   [:cmd
    [:oneof
     {:set-value [:map [:set-value :cmd/set-azimuth-value]],
      :rotate-to [:map [:rotate-to :cmd/rotate-azimuth-to]],
      :rotate [:map [:rotate :cmd/rotate-azimuth]],
      :relative [:map [:relative :cmd/rotate-azimuth-relative]],
      :relative-set [:map [:relative-set :cmd/rotate-azimuth-relative-set]],
      :halt [:map [:halt :cmd/halt-azimuth]]}]]])


(def stop "Schema for stop" [:map])


(def rotate-azimuth-relative
  "Schema for rotate-azimuth-relative"
  [:map [:value [:maybe :double]] [:speed [:and [:maybe :double] [:>= 0] [:<= 1]]]
   [:direction [:not [:enum [0]]]]])


(def set-origin-gps
  "Schema for set-origin-gps"
  [:map [:latitude [:and [:maybe :double] [:>= -90] [:<= 90]]]
   [:longitude [:and [:maybe :double] [:>= -180] [:< 180]]] [:altitude [:maybe :double]]])


(def root
  "Schema for root"
  [:map
   [:cmd
    [:oneof
     {:rotate-to-gps [:map [:rotate-to-gps :cmd/rotate-to-gps]],
      :scan-pause [:map [:scan-pause :cmd/scan-pause]],
      :rotate-to-ndc [:map [:rotate-to-ndc :cmd/rotate-to-ndc]],
      :scan-start [:map [:scan-start :cmd/scan-start]],
      :set-platform-azimuth [:map [:set-platform-azimuth :cmd/set-platform-azimuth]],
      :scan-stop [:map [:scan-stop :cmd/scan-stop]],
      :start [:map [:start :cmd/start]],
      :stop [:map [:stop :cmd/stop]],
      :set-origin-gps [:map [:set-origin-gps :cmd/set-origin-gps]],
      :scan-next [:map [:scan-next :cmd/scan-next]],
      :set-platform-bank [:map [:set-platform-bank :cmd/set-platform-bank]],
      :get-meteo [:map [:get-meteo :cmd/get-meteo]],
      :set-use-rotary-as-compass [:map [:set-use-rotary-as-compass :cmd/set-use-rotary-as-compass]],
      :scan-prev [:map [:scan-prev :cmd/scan-prev]],
      :scan-add-node [:map [:scan-add-node :cmd/scan-add-node]],
      :set-platform-elevation [:map [:set-platform-elevation :cmd/set-platform-elevation]],
      :scan-select-node [:map [:scan-select-node :cmd/scan-select-node]],
      :halt [:map [:halt :cmd/halt]],
      :scan-delete-node [:map [:scan-delete-node :cmd/scan-delete-node]],
      :axis [:map [:axis :cmd/axis]],
      :scan-unpause [:map [:scan-unpause :cmd/scan-unpause]],
      :set-mode [:map [:set-mode :cmd/set-mode]],
      :scan-refresh-node-list [:map [:scan-refresh-node-list :cmd/scan-refresh-node-list]],
      :scan-update-node [:map [:scan-update-node :cmd/scan-update-node]]}]]])


(def scan-next "Schema for scan-next" [:map])


(def set-platform-bank
  "Schema for set-platform-bank"
  [:map [:value [:and [:maybe :double] [:>= -180] [:< 180]]]])


(def get-meteo "Schema for get-meteo" [:map])


(def set-use-rotary-as-compass
  "Schema for set-use-rotary-as-compass"
  [:map [:flag [:maybe :boolean]]])


(def rotate-azimuth-relative-set
  "Schema for rotate-azimuth-relative-set"
  [:map [:value [:maybe :double]] [:direction [:not [:enum [0]]]]])


(def scan-prev "Schema for scan-prev" [:map])


(def scan-add-node
  "Schema for scan-add-node"
  [:map [:index [:>= 0]] [:DayZoomTableValue [:>= 0]] [:HeatZoomTableValue [:>= 0]]
   [:azimuth [:maybe :double]] [:elevation [:maybe :double]] [:linger [:maybe :double]]
   [:speed [:maybe :double]]])


(def set-platform-elevation
  "Schema for set-platform-elevation"
  [:map [:value [:and [:maybe :double] [:>= -90] [:<= 90]]]])


(def rotate-elevation-relative-set
  "Schema for rotate-elevation-relative-set"
  [:map [:value [:maybe :double]] [:direction [:not [:enum [0]]]]])


(def scan-select-node "Schema for scan-select-node" [:map [:index [:>= 0]]])


(def halt "Schema for halt" [:map])


(def scan-delete-node "Schema for scan-delete-node" [:map [:index [:>= 0]]])


(def axis
  "Schema for axis"
  [:map [:azimuth [:maybe :cmd/azimuth]] [:elevation [:maybe :cmd/elevation]]])


(def scan-unpause "Schema for scan-unpause" [:map])


(def rotate-elevation
  "Schema for rotate-elevation"
  [:map [:speed [:and [:maybe :double] [:>= 0] [:<= 1]]] [:direction [:not [:enum [0]]]]])


(def rotate-azimuth-to
  "Schema for rotate-azimuth-to"
  [:map [:target-value [:and [:maybe :double] [:>= 0] [:< 360]]]
   [:speed [:and [:maybe :double] [:>= 0] [:<= 1]]] [:direction [:not [:enum [0]]]]])


(def set-mode "Schema for set-mode" [:map [:mode [:not [:enum [0]]]]])


(def set-azimuth-value
  "Schema for set-azimuth-value"
  [:map [:value [:and [:maybe :double] [:>= 0] [:< 360]]] [:direction [:not [:enum [0]]]]])


(def scan-refresh-node-list "Schema for scan-refresh-node-list" [:map])


(def scan-update-node
  "Schema for scan-update-node"
  [:map [:index [:>= 0]] [:DayZoomTableValue [:>= 0]] [:HeatZoomTableValue [:>= 0]]
   [:azimuth [:maybe :double]] [:elevation [:maybe :double]] [:linger [:maybe :double]]
   [:speed [:maybe :double]]])
