(ns potatoclient.specs.cmd.gps
  "GPS command specs matching buf.validate constraints.
   Based on jon_shared_cmd_gps.proto.
   All maps use {:closed true} to catch typos and invalid keys."
  (:require
    [malli.generator :as mg]
    [malli.core :as m]
    [potatoclient.malli.registry :as registry]
    [potatoclient.malli.oneof :as oneof]))

;; GPS command specs - based on proto-explorer findings
;; This is a oneof structure with 5 command types

;; Basic control
(def start-spec [:map {:closed true}])
(def stop-spec [:map {:closed true}])

;; Manual position
(def set-manual-position-spec
  [:map {:closed true}
   [:latitude [:double {:min -90.0 :max 90.0}]]
   [:longitude [:double {:min -180.0 :max 180.0}]]])

(def set-use-manual-position-spec
  [:map {:closed true}
   [:value [:boolean]]])

;; Meteo
(def get-meteo-spec [:map {:closed true}])

;; Main GPS command spec using oneof - all 5 commands
(def gps-command-spec
  [:oneof
   [:start start-spec]
   [:stop stop-spec]
   [:set_manual_position set-manual-position-spec]
   [:set_use_manual_position set-use-manual-position-spec]
   [:get_meteo get-meteo-spec]])

(registry/register! :cmd/gps gps-command-spec)
