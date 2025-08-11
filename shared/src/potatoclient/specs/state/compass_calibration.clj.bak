(ns potatoclient.specs.state.compass-calibration
  "Compass Calibration message spec matching buf.validate constraints and EDN output.
   All maps use {:closed true} to catch typos and invalid keys."
  (:require
   [potatoclient.malli.registry :as registry]))

;; Compass calibration message spec based on actual EDN output:
;; {:final_stage 12
;;  :status :JON_GUI_DATA_COMPASS_CALIBRATE_STATUS_NOT_CALIBRATING
;;  :target_azimuth 56.25
;;  :target_bank -5.625
;;  :target_elevation 6.75}

(def compass-calibration-message-spec
  [:map {:closed true}
   [:final-stage :int]
   [:status :enum/compass-calibrate-status]
   [:target-azimuth :angle/azimuth]
   [:target-bank :angle/bank]
   [:target-elevation :angle/elevation]])

(registry/register! :state/compass-calibration compass-calibration-message-spec)