(ns potatoclient.specs.cmd.OSD
  "Generated Malli specs from protobuf descriptors"
  (:require [malli.core :as m]
            [malli.generator :as mg]))

;; Note: FUNCTION-PLACEHOLDER markers indicate where runtime functions are needed
;; These will be replaced with actual implementations when loaded

(def show-lrf-result-simplified-screen "Schema for show-lrf-result-simplified-screen" [:map])


(def show-lrf-measure-screen "Schema for show-lrf-measure-screen" [:map])


(def disable-heat-osd "Schema for disable-heat-osd" [:map])


(def disable-day-osd "Schema for disable-day-osd" [:map])


(def show-lrf-result-screen "Schema for show-lrf-result-screen" [:map])


(def root
  "Schema for root"
  [:map
   [:cmd
    [:oneof
     {:show-default-screen [:map [:show-default-screen :cmd/show-default-screen]],
      :show-lrf-measure-screen [:map [:show-lrf-measure-screen :cmd/show-lrf-measure-screen]],
      :show-lrf-result-screen [:map [:show-lrf-result-screen :cmd/show-lrf-result-screen]],
      :show-lrf-result-simplified-screen
        [:map [:show-lrf-result-simplified-screen :cmd/show-lrf-result-simplified-screen]],
      :enable-heat-osd [:map [:enable-heat-osd :cmd/enable-heat-osd]],
      :disable-heat-osd [:map [:disable-heat-osd :cmd/disable-heat-osd]],
      :enable-day-osd [:map [:enable-day-osd :cmd/enable-day-osd]],
      :disable-day-osd [:map [:disable-day-osd :cmd/disable-day-osd]]}]]])


(def enable-heat-osd "Schema for enable-heat-osd" [:map])


(def show-default-screen "Schema for show-default-screen" [:map])


(def enable-day-osd "Schema for enable-day-osd" [:map])
