(ns potatoclient.specs.data.system
  "Malli specs for system data matching jon_shared_data_system.proto validation constraints"
  (:require [potatoclient.specs.data.types :as types]))

;; From jon_shared_data_system.proto validation constraints

(def cpu-temperature
  "CPU temperature in Celsius"
  [:float {:min -273.15 :max 150}])

(def gpu-temperature
  "GPU temperature in Celsius"
  [:float {:min -273.15 :max 150}])

(def gpu-load
  "GPU load percentage (0-100)"
  [:float {:min 0 :max 100}])

(def cpu-load
  "CPU load percentage (0-100)"
  [:float {:min 0 :max 100}])

(def power-consumption
  "Power consumption in watts"
  [:float {:min 0 :max 1000}])

(def localization
  "System localization/language"
  types/system-localizations)

(def timestamp-part
  "Part of recording directory timestamp (non-negative)"
  [:int {:min 0}])

(def disk-space
  "Available disk space percentage (0-100)"
  [:int {:min 0 :max 100}])

(def system-data
  "Complete system data structure"
  [:map
   [:cpu-temperature {:optional true} cpu-temperature]
   [:gpu-temperature {:optional true} gpu-temperature]
   [:gpu-load {:optional true} gpu-load]
   [:cpu-load {:optional true} cpu-load]
   [:power-consumption {:optional true} power-consumption]
   [:localization {:optional true} localization]
   [:rec-dir-timestamp {:optional true}
    [:map
     [:year {:optional true} timestamp-part]
     [:month {:optional true} timestamp-part]
     [:day {:optional true} timestamp-part]
     [:hour {:optional true} timestamp-part]
     [:minute {:optional true} timestamp-part]
     [:second {:optional true} timestamp-part]]]
   [:rec-enabled {:optional true} boolean?]
   [:important-rec-enabled {:optional true} boolean?]
   [:low-disk-space {:optional true} boolean?]
   [:no-disk-space {:optional true} boolean?]
   [:disk-space {:optional true} disk-space]
   [:tracking {:optional true} boolean?]
   [:vampire-mode {:optional true} boolean?]
   [:stabilization-mode {:optional true} boolean?]
   [:geodesic-mode {:optional true} boolean?]
   [:cv-dumping {:optional true} boolean?]])