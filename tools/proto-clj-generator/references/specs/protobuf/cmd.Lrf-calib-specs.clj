(ns potatoclient.specs.cmd.Lrf-calib
  "Generated Malli specs from protobuf descriptors"
  (:require [malli.core :as m]
            [malli.generator :as mg]))

;; Note: FUNCTION-PLACEHOLDER markers indicate where runtime functions are needed
;; These will be replaced with actual implementations when loaded

(def root
  "Schema for root"
  [:map [:channel [:oneof {:day [:map [:day :cmd/offsets]], :heat [:map [:heat :cmd/offsets]]}]]])


(def offsets
  "Schema for offsets"
  [:map
   [:cmd
    [:oneof
     {:set [:map [:set :cmd/set-offsets]],
      :save [:map [:save :cmd/save-offsets]],
      :reset [:map [:reset :cmd/reset-offsets]],
      :shift [:map [:shift :cmd/shift-offsets-by]]}]]])


(def set-offsets
  "Schema for set-offsets"
  [:map [:x [:and [:maybe :int] [:>= -1920] [:<= 1920]]]
   [:y [:and [:maybe :int] [:>= -1080] [:<= 1080]]]])


(def shift-offsets-by
  "Schema for shift-offsets-by"
  [:map [:x [:and [:maybe :int] [:>= -1920] [:<= 1920]]]
   [:y [:and [:maybe :int] [:>= -1080] [:<= 1080]]]])


(def reset-offsets "Schema for reset-offsets" [:map])


(def save-offsets "Schema for save-offsets" [:map])
