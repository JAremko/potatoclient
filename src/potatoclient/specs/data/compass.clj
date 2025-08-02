(ns potatoclient.specs.data.compass
  "Malli specs for compass data matching jon_shared_data_compass.proto validation constraints")

;; From jon_shared_data_compass.proto validation constraints

(def azimuth
  "Compass azimuth/heading in degrees (0-360)"
  [:double {:min 0 :max 360}])

(def elevation
  "Compass elevation/pitch in degrees (-90 to 90)"
  [:double {:min -90 :max 90}])

(def bank
  "Compass bank/roll in degrees (-180 to 180)"
  [:double {:min -180 :max 180}])

(def offset-azimuth
  "Azimuth offset for calibration (-180 to 180)"
  [:double {:min -180 :max 180}])

(def offset-elevation
  "Elevation offset for calibration (-90 to 90)"
  [:double {:min -90 :max 90}])

(def magnetic-declination
  "Magnetic declination in degrees (-180 to 180)"
  [:double {:min -180 :max 180}])

(def compass-data
  "Complete compass data structure"
  [:map
   [:azimuth {:optional true} azimuth]
   [:elevation {:optional true} elevation]
   [:bank {:optional true} bank]
   [:offset-azimuth {:optional true} offset-azimuth]
   [:offset-elevation {:optional true} offset-elevation]
   [:magnetic-declination {:optional true} magnetic-declination]
   [:calibrating {:optional true} boolean?]])