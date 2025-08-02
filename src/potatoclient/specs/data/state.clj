(ns potatoclient.specs.data.state
  "Top-level Malli spec for complete GUI state matching jon_shared_data.proto"
  (:require [potatoclient.specs.data.system :as system]
            [potatoclient.specs.data.rotary :as rotary]
            [potatoclient.specs.data.gps :as gps]
            [potatoclient.specs.data.compass :as compass]
            [potatoclient.specs.data.lrf :as lrf]
            [potatoclient.specs.data.time :as time]
            [potatoclient.specs.data.camera :as camera]))

(def jon-gui-state
  "Complete JonGUIState structure as received from protobuf"
  [:map
   [:timestamp {:optional true} [:int {:min 0}]]
   [:protocol-version {:optional true} [:int {:min 0}]]
   [:system {:optional true} system/system-data]
   [:rotary {:optional true} rotary/rotary-data]
   [:gps {:optional true} gps/gps-data]
   [:compass {:optional true} compass/compass-data]
   [:lrf {:optional true} lrf/lrf-data]
   [:time {:optional true} time/time-data]
   [:camera-day {:optional true} camera/day-camera-data]
   [:camera-heat {:optional true} camera/heat-camera-data]])