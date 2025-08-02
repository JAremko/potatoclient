(ns potatoclient.specs.data.rotary
  "Malli specs for rotary data matching jon_shared_data_rotary.proto validation constraints"
  (:require [potatoclient.specs.data.types :as types]))

;; From jon_shared_data_rotary.proto validation constraints

(def azimuth
  "Azimuth angle in degrees (0-360)"
  [:float {:min 0 :max 360}]) ; Using max instead of lt for inclusive upper bound

(def azimuth-speed
  "Azimuth rotation speed (-1.0 to 1.0)"
  [:float {:min -1.0 :max 1.0}])

(def elevation
  "Elevation angle in degrees (-90 to 90)"
  [:float {:min -90 :max 90}])

(def elevation-speed
  "Elevation rotation speed (-1.0 to 1.0)"
  [:float {:min -1.0 :max 1.0}])

(def platform-azimuth
  "Platform azimuth angle in degrees (0-360)"
  [:float {:min 0 :max 360}])

(def platform-elevation
  "Platform elevation angle in degrees (-90 to 90)"
  [:float {:min -90 :max 90}])

(def platform-bank
  "Platform bank angle in degrees (-180 to 180)"
  [:float {:min -180 :max 180}])

(def scan-target
  "Current scan target index"
  [:int {:min 0}])

(def scan-target-max
  "Maximum scan target index"
  [:int {:min 0}])

(def sun-azimuth
  "Sun azimuth angle in degrees (0-360)"
  [:float {:min 0 :max 360}])

(def sun-elevation
  "Sun elevation angle in degrees (0-360)"
  [:float {:min 0 :max 360}])

(def scan-node
  "Scan node configuration"
  [:map
   [:index {:optional true} [:int {:min 0}]]
   [:day-zoom-table-value {:optional true} [:int {:min 0}]]
   [:heat-zoom-table-value {:optional true} [:int {:min 0}]]
   [:azimuth {:optional true} [:double {:min 0 :max 360}]]
   [:elevation {:optional true} [:double {:min -90 :max 90}]]
   [:linger {:optional true} [:double {:min 0}]]
   [:speed {:optional true} [:double {:min 0.0 :max 1.0}]]])

(def rotary-data
  "Complete rotary data structure"
  [:map
   [:azimuth {:optional true} azimuth]
   [:azimuth-speed {:optional true} azimuth-speed]
   [:elevation {:optional true} elevation]
   [:elevation-speed {:optional true} elevation-speed]
   [:platform-azimuth {:optional true} platform-azimuth]
   [:platform-elevation {:optional true} platform-elevation]
   [:platform-bank {:optional true} platform-bank]
   [:is-moving {:optional true} boolean?]
   [:mode {:optional true} types/rotary-mode]
   [:is-scanning {:optional true} boolean?]
   [:is-scanning-paused {:optional true} boolean?]
   [:use-rotary-as-compass {:optional true} boolean?]
   [:scan-target {:optional true} scan-target]
   [:scan-target-max {:optional true} scan-target-max]
   [:sun-azimuth {:optional true} sun-azimuth]
   [:sun-elevation {:optional true} sun-elevation]
   [:current-scan-node {:optional false} scan-node]]) ; Required field