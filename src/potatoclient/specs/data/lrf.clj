(ns potatoclient.specs.data.lrf
  "Malli specs for LRF (Laser Range Finder) data matching jon_shared_data_lrf.proto validation constraints"
  (:require [potatoclient.specs.data.types :as types]))

;; From jon_shared_data_lrf.proto validation constraints

(def measure-id
  "Measurement ID (non-negative)"
  [:int {:min 0}])

(def timestamp
  "Unix timestamp in milliseconds (non-negative)"
  [:int {:min 0}])

(def longitude
  "Longitude in degrees (-180 to 180)"
  [:double {:min -180.0 :max 180.0}])

(def latitude
  "Latitude in degrees (-90 to 90)"
  [:double {:min -90.0 :max 90.0}])

(def azimuth
  "Azimuth angle in degrees (0-360)"
  [:double {:min 0 :max 360}])

(def elevation
  "Elevation angle in degrees (-90 to 90)"
  [:double {:min -90 :max 90}])

(def bank
  "Bank angle in degrees (-180 to 180)"
  [:double {:min -180 :max 180}])

(def distance
  "Distance measurement in decimeters (0 to 50km)"
  [:double {:min 0 :max 500000}])

(def color-component
  "RGB color component (0-255)"
  [:int {:min 0 :max 255}])

(def rgb-color
  "RGB color specification"
  [:map
   [:red {:optional true} color-component]
   [:green {:optional true} color-component]
   [:blue {:optional true} color-component]])

(def target-data
  "LRF target measurement data"
  [:map
   [:timestamp {:optional true} timestamp]
   [:target-longitude {:optional true} longitude]
   [:target-latitude {:optional true} latitude]
   [:target-altitude {:optional true} number?]
   [:observer-longitude {:optional true} longitude]
   [:observer-latitude {:optional true} latitude]
   [:observer-altitude {:optional true} number?]
   [:observer-azimuth {:optional true} azimuth]
   [:observer-elevation {:optional true} elevation]
   [:observer-bank {:optional true} bank]
   [:distance-2d {:optional true} distance]
   [:distance-3d {:optional true} distance]
   [:observer-fix-type {:optional true} types/fix-type]
   [:session-id {:optional true} [:int {:min 0}]]
   [:target-id {:optional true} [:int {:min 0}]]
   [:target-color {:optional true} rgb-color]
   [:type {:optional true} [:int {:min 0}]]
   [:uuid-part1 {:optional true} int?]
   [:uuid-part2 {:optional true} int?]
   [:uuid-part3 {:optional true} int?]
   [:uuid-part4 {:optional true} int?]])

(def lrf-data
  "Complete LRF data structure"
  [:map
   [:is-scanning {:optional true} boolean?]
   [:is-measuring {:optional true} boolean?]
   [:measure-id {:optional true} measure-id]
   [:target {:optional true} target-data]
   [:pointer-mode {:optional true} keyword?] ; Enum type to be determined
   [:fog-mode-enabled {:optional true} boolean?]
   [:is-refining {:optional true} boolean?]])