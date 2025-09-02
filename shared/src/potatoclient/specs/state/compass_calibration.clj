(ns potatoclient.specs.state.compass-calibration
  "Compass Calibration message spec matching buf.validate constraints and EDN output.
   All maps use {:closed true} to catch typos and invalid keys."
  (:require
    [potatoclient.specs.common]
    [potatoclient.malli.registry :as registry]))

;; JonGuiDataCompassCalibration message spec
;; All 6 fields from proto definition

(def compass-calibration-message-spec
  [:map {:closed true}
   [:stage :proto/int32-positive]
   [:final_stage [:int {:min 1 :max 2147483647}]]
   [:target_azimuth :angle/azimuth]
   [:target_elevation :angle/elevation]
   [:target_bank :angle/bank]
   [:status :enum/compass-calibrate-status]])

(registry/register! :state/compass-calibration compass-calibration-message-spec)