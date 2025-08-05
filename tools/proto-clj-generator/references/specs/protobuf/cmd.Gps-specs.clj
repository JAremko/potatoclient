(ns potatoclient.specs.cmd.Gps
  "Generated Malli specs from protobuf descriptors"
  (:require [malli.core :as m]
            [malli.generator :as mg]))

;; Note: FUNCTION-PLACEHOLDER markers indicate where runtime functions are needed
;; These will be replaced with actual implementations when loaded

(def root
  "Schema for root"
  [:map
   [:cmd
    [:oneof
     {:start [:map [:start :cmd/start]],
      :stop [:map [:stop :cmd/stop]],
      :set-manual-position [:map [:set-manual-position :cmd/set-manual-position]],
      :set-use-manual-position [:map [:set-use-manual-position :cmd/set-use-manual-position]],
      :get-meteo [:map [:get-meteo :cmd/get-meteo]]}]]])


(def start "Schema for start" [:map])


(def stop "Schema for stop" [:map])


(def get-meteo "Schema for get-meteo" [:map])


(def set-use-manual-position "Schema for set-use-manual-position" [:map [:flag [:maybe :boolean]]])


(def set-manual-position
  "Schema for set-manual-position"
  [:map [:latitude [:and [:maybe :double] [:>= -90] [:<= 90]]]
   [:longitude [:and [:maybe :double] [:>= -180] [:< 180]]]
   [:altitude [:and [:maybe :double] [:>= -432] [:<= 8848]]]])
