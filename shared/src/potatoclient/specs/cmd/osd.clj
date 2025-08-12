(ns potatoclient.specs.cmd.osd
  "OSD (On-Screen Display) command specs matching buf.validate constraints.
   Based on jon_shared_cmd_osd.proto.
   All maps use {:closed true} to catch typos and invalid keys."
  (:require
   [potatoclient.malli.registry :as registry]
   [potatoclient.specs.oneof-edn :as oneof-edn]))

;; OSD command specs - simplified placeholders
;; This is a oneof structure with multiple command types

(def enable-spec [:map {:closed true}])
(def disable-spec [:map {:closed true}])
(def set-text-spec
  [:map {:closed true}
   [:text [:string]]])

;; Main OSD command spec using oneof
(def osd-command-spec
  [:oneof_edn
   [:enable enable-spec]
   [:disable disable-spec]
   [:set_text set-text-spec]
   ;; Add more commands as needed
   ])

(registry/register! :cmd/osd osd-command-spec)