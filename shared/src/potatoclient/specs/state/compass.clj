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
   [:elevation :angle/elevation]
   [:offsetAzimuth {:optional true} :double]
   [:offsetElevation {:optional true} :double]
   [:magneticDeclination {:optional true} :double]
   [:calibrating {:optional true} boolean?]])

(registry/register! :state/compass compass-message-spec)

;; Compass calibration message spec based on actual EDN output:
;; {:final_stage 12
;;  :status :JON_GUI_DATA_COMPASS_CALIBRATE_STATUS_NOT_CALIBRATING
;;  :target_azimuth 56.25
;;  :target_bank -5.625
;;  :target_elevation 6.75}

(def compass-calibration-message-spec
  [:map {:closed true}
   [:stage {:optional true} [:int {:min 0 :max 100}]]
   [:final_stage [:int {:min 0 :max 100}]]
   [:status :enum/compass-calibrate-status]
   [:target_azimuth :angle/azimuth]
   [:target_bank :angle/bank]
   [:target_elevation :angle/elevation]])

(registry/register! :state/compass-calibration compass-calibration-message-spec)