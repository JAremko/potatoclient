(ns potatoclient.specs.cmd.rotary
  "Malli specs for rotary commands matching jon_shared_cmd_rotary.proto validation constraints"
  (:require [potatoclient.specs.data.types :as types]))

;; Speed specs (0.0 to 1.0)
(def rotation-speed
  "Rotation speed value (0.0 to 1.0)"
  [:float {:min 0.0 :max 1.0}])

;; Angle specs
(def azimuth-angle
  "Azimuth angle in degrees (0 to 360)"
  [:float {:min 0.0 :max 360.0}])

(def elevation-angle
  "Elevation angle in degrees (-90 to 90)"
  [:float {:min -90.0 :max 90.0}])

(def bank-angle
  "Bank angle in degrees (-180 to 180)"
  [:float {:min -180.0 :max 180.0}])

;; Command parameter specs
(def rotate-azimuth-params
  "Parameters for rotating azimuth at speed"
  [:map
   [:speed rotation-speed]
   [:direction types/rotary-direction]])

(def rotate-elevation-params
  "Parameters for rotating elevation at speed"
  [:map
   [:speed rotation-speed]
   [:direction types/rotary-direction]])

(def rotate-to-ndc-params
  "Parameters for rotating to normalized device coordinates"
  [:map
   [:channel types/video-channel]
   [:x [:float {:min -1.0 :max 1.0}]]
   [:y [:float {:min -1.0 :max 1.0}]]])

(def set-mode-params
  "Parameters for setting rotary mode"
  [:map
   [:mode types/rotary-mode]])

;; Command actions that take parameters
(def parameterized-commands
  #{:rotary-set-velocity
    :rotary-goto-ndc
    :rotary-set-mode
    :rotary-goto-azimuth
    :rotary-goto-elevation})

;; Command actions that take no parameters
(def simple-commands
  #{:rotary-halt
    :rotary-start
    :rotary-stop
    :scan-start
    :scan-stop
    :scan-pause
    :scan-unpause
    :scan-prev
    :scan-next})

(def rotary-command
  "Rotary command structure"
  [:map
   [:action [:enum
             :rotary-halt
             :rotary-start
             :rotary-stop
             :rotary-set-velocity
             :rotary-goto-ndc
             :rotary-set-mode
             :rotary-goto-azimuth
             :rotary-goto-elevation
             :scan-start
             :scan-stop
             :scan-pause
             :scan-unpause
             :scan-prev
             :scan-next]]
   [:params {:optional true} [:map]]])