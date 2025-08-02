(ns potatoclient.specs.data.gps
  "Malli specs for GPS data matching jon_shared_data_gps.proto validation constraints"
  (:require [potatoclient.specs.data.types :as types]))

;; From jon_shared_data_gps.proto validation constraints

(def longitude
  "Longitude in degrees (-180 to 180)"
  [:double {:min -180.0 :max 180.0}])

(def latitude
  "Latitude in degrees (-90 to 90)"
  [:double {:min -90.0 :max 90.0}])

(def altitude
  "Altitude in meters (Dead Sea shore -433m to Mount Everest 8848.86m)"
  [:double {:min -433.0 :max 8848.86}])

(def manual-longitude
  "Manual longitude override"
  longitude)

(def manual-latitude
  "Manual latitude override"
  latitude)

(def manual-altitude
  "Manual altitude override"
  altitude)

(def fix-type
  "GPS fix type enum (keyword in Transit)"
  [:enum :no-fix :2d :3d :dgps :rtk])

(def use-manual
  "Whether to use manual GPS coordinates"
  boolean?)

(def gps-data
  "Complete GPS data structure"
  [:map
   [:longitude {:optional true} longitude]
   [:latitude {:optional true} latitude]
   [:altitude {:optional true} altitude]
   [:manual-longitude {:optional true} manual-longitude]
   [:manual-latitude {:optional true} manual-latitude]
   [:manual-altitude {:optional true} manual-altitude]
   [:fix-type {:optional true} fix-type]
   [:use-manual {:optional true} use-manual]])