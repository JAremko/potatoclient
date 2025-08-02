(ns potatoclient.specs.data.camera
  "Malli specs for camera data (both heat and day cameras) from jon_shared_data_camera_*.proto"
  (:require [potatoclient.specs.data.types :as types]))

;; Common camera data structures

(def fov-range
  "Field of view range structure"
  [:map
   [:horizontal {:optional true} [:float {:min 0.0 :max 180.0}]]
   [:vertical {:optional true} [:float {:min 0.0 :max 180.0}]]])

(def zoom-state
  "Camera zoom state"
  [:map
   [:current-index {:optional true} [:int {:min 0}]]
   [:total-positions {:optional true} [:int {:min 1}]]
   [:current-fov {:optional true} fov-range]
   [:optical-zoom {:optional true} [:float {:min 1.0}]]
   [:digital-zoom {:optional true} [:float {:min 1.0}]]])

;; Heat camera specific

(def heat-camera-settings
  "Heat camera settings"
  [:map
   [:palette {:optional true} types/thermal-palette]
   [:agc-enabled {:optional true} boolean?]
   [:dde-enabled {:optional true} boolean?]
   [:nuc-enabled {:optional true} boolean?]
   [:brightness {:optional true} [:int {:min 0 :max 100}]]
   [:contrast {:optional true} [:int {:min 0 :max 100}]]
   [:gain {:optional true} [:int {:min 0 :max 100}]]])

(def heat-camera-status
  "Heat camera status"
  [:map
   [:temperature {:optional true} [:float {:min -40.0 :max 85.0}]]  ; Celsius
   [:shutter-position {:optional true} [:enum :open :closed]]
   [:calibration-status {:optional true} [:enum :idle :in-progress :complete :failed]]
   [:scene-mode {:optional true} [:enum :normal :maritime :desert :arctic :jungle]]])

(def heat-camera-data
  "Complete heat camera data structure"
  [:map
   [:enabled {:optional true} boolean?]
   [:zoom {:optional true} zoom-state]
   [:settings {:optional true} heat-camera-settings]
   [:status {:optional true} heat-camera-status]
   [:frame-rate {:optional true} [:int {:min 1 :max 60}]]
   [:resolution-width {:optional true} [:int {:min 160 :max 1920}]]
   [:resolution-height {:optional true} [:int {:min 120 :max 1080}]]])

;; Day camera specific

(def day-camera-settings
  "Day camera settings"
  [:map
   [:exposure-mode {:optional true} [:enum :auto :manual :shutter-priority :aperture-priority]]
   [:white-balance-mode {:optional true} [:enum :auto :daylight :cloudy :tungsten :fluorescent :manual]]
   [:iso {:optional true} [:int {:min 50 :max 12800}]]
   [:shutter-speed {:optional true} [:int {:min 1 :max 8000}]]  ; 1/x seconds
   [:aperture {:optional true} [:float {:min 1.0 :max 22.0}]]  ; f-stop
   [:brightness {:optional true} [:int {:min -100 :max 100}]]
   [:contrast {:optional true} [:int {:min -100 :max 100}]]
   [:saturation {:optional true} [:int {:min -100 :max 100}]]
   [:sharpness {:optional true} [:int {:min -100 :max 100}]]])

(def day-camera-status
  "Day camera status"
  [:map
   [:iris-position {:optional true} [:float {:min 0.0 :max 100.0}]]  ; percentage
   [:focus-position {:optional true} [:float {:min 0.0 :max 100.0}]]  ; percentage
   [:focus-mode {:optional true} [:enum :auto :manual :infinity]]
   [:stabilization-enabled {:optional true} boolean?]
   [:defog-enabled {:optional true} boolean?]
   [:glass-heater-status {:optional true} types/glass-heater-mode]])

(def day-camera-data
  "Complete day camera data structure"
  [:map
   [:enabled {:optional true} boolean?]
   [:zoom {:optional true} zoom-state]
   [:settings {:optional true} day-camera-settings]
   [:status {:optional true} day-camera-status]
   [:frame-rate {:optional true} [:int {:min 1 :max 60}]]
   [:resolution-width {:optional true} [:int {:min 640 :max 3840}]]
   [:resolution-height {:optional true} [:int {:min 480 :max 2160}]]])