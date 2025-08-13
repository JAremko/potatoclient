(ns potatoclient.specs.cmd.osd
  "OSD (On-Screen Display) command specs matching buf.validate constraints.
   Based on jon_shared_cmd_osd.proto.
   All maps use {:closed true} to catch typos and invalid keys."
  (:require
   [potatoclient.malli.registry :as registry]
   [potatoclient.malli.oneof-edn :as oneof-edn]))

;; OSD command specs - realistic implementation
;; This is a oneof structure with multiple command types

(def start-spec [:map {:closed true}])
(def stop-spec [:map {:closed true}])
(def show-spec [:map {:closed true}])
(def hide-spec [:map {:closed true}])
(def set-text-spec
  [:map {:closed true}
   [:text [:string {:max 100}]]])
(def set-position-spec
  [:map {:closed true}
   [:x [:int {:min 0 :max 1920}]]
   [:y [:int {:min 0 :max 1080}]]])

;; Main OSD command spec using oneof
(def osd-command-spec
  [:oneof_edn
   [:start start-spec]
   [:stop stop-spec]
   [:show show-spec]
   [:hide hide-spec]
   [:set_text set-text-spec]
   [:set_position set-position-spec]])

(registry/register! :cmd/osd osd-command-spec)