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
   ;; Empty map as shown in actual data
   ;; May have optional weather-related fields in some configurations
   ])

(registry/register! :state/meteo-internal meteo-internal-message-spec)