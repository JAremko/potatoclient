(ns potatoclient.specs.state.meteo-internal
  "Meteo Internal message spec matching buf.validate constraints and EDN output.
   Based on proto structure.
   All maps use {:closed true} to catch typos and invalid keys."
  (:require
   [potatoclient.malli.registry :as registry]))

;; JonGuiDataMeteo message spec based on EDN output:
;; {} (empty map in the example)
;;
;; This message appears to be empty in the sample but may have weather-related fields

(def meteo-internal-message-spec
  [:map {:closed true}
   ;; Add fields here when they appear in actual data
   ;; Likely fields: temperature, humidity, pressure, wind speed, etc.
   ])

(registry/register! :state/meteo-internal meteo-internal-message-spec)