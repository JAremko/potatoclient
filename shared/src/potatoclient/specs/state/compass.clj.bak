(ns potatoclient.specs.state.compass
  "Compass message specs matching buf.validate constraints and EDN output format.
   All maps use {:closed true} to catch typos and invalid keys."
  (:require
   [potatoclient.malli.registry :as registry]))

;; Compass message spec based on actual EDN output:
;; {:azimuth 335.3625
;;  :bank 0.7312500000000001
;;  :elevation 3.6}

(def compass-message-spec
  [:map {:closed true}
   [:azimuth :angle/azimuth]
   [:bank :angle/bank]
   [:elevation :angle/elevation]])

(registry/register! :state/compass compass-message-spec)

;; Compass calibration message spec based on actual EDN output:
;; {:final-stage 12
;;  :status :jon-gui-data-compass-calibrate-status-not-calibrating
;;  :target-azimuth 56.25
;;  :target-bank -5.625
;;  :target-elevation 6.75}

(def compass-calibration-message-spec
  [:map {:closed true}
   [:final-stage [:int {:min 0 :max 100}]]
   [:status :enum/compass-calibrate-status]
   [:target-azimuth :angle/azimuth]
   [:target-bank :angle/bank]
   [:target-elevation :angle/elevation]])

(registry/register! :state/compass-calibration compass-calibration-message-spec)