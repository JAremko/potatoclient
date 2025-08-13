(ns potatoclient.specs.state.meteo-internal
  "Meteo Internal message spec matching buf.validate constraints and EDN output.
   Based on proto structure.
   All maps use {:closed true} to catch typos and invalid keys."
  (:require
   [potatoclient.specs.common]
   [potatoclient.malli.registry :as registry]))

;; JonGuiDataMeteo message spec based on proto definition:
;; Has 3 fields: temperature, humidity, pressure (all floats with no constraints)

(def meteo-internal-message-spec
  [:map {:closed true}
   [:temperature :float]
   [:humidity :float]
   [:pressure :float]])

(registry/register! :state/meteo-internal meteo-internal-message-spec)