(ns potatoclient.specs.state.compass
  "Compass message specs matching buf.validate constraints and EDN output format.
   All maps use {:closed true} to catch typos and invalid keys."
  (:require
    [potatoclient.specs.common]
    [potatoclient.malli.registry :as registry]))

;; JonGuiDataCompass message spec
;; All 7 fields from proto definition

(def compass-message-spec
  [:map {:closed true}
   [:azimuth :angle/azimuth]
   [:elevation :angle/elevation]
   [:bank :angle/bank]
   [:offsetAzimuth :angle/offset-azimuth]
   [:offsetElevation :angle/offset-elevation]
   [:magneticDeclination :angle/magnetic-declination]
   [:calibrating :boolean]])

(registry/register-spec! :state/compass compass-message-spec)