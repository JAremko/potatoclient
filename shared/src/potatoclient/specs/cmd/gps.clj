(ns potatoclient.specs.cmd.gps
  "GPS command specs matching buf.validate constraints.
   Based on jon_shared_cmd_gps.proto.
   All maps use {:closed true} to catch typos and invalid keys."
  (:require
   [potatoclient.malli.registry :as registry]
   [potatoclient.specs.oneof-edn :as oneof-edn]))

;; GPS command specs
;; This is a oneof structure with multiple command types

(def set-fix-manual-spec
  [:map {:closed true}
   [:latitude [:float {:min -90.0 :max 90.0}]]
   [:longitude [:float {:min -180.0 :max 180.0}]]])

(def set-fix-auto-spec [:map {:closed true}])

;; Main GPS command spec using oneof
(def gps-command-spec
  [:oneof_edn
   [:set_fix_manual set-fix-manual-spec]
   [:set_fix_auto set-fix-auto-spec]
   ;; Add more commands as needed
   ])

(registry/register! :cmd/gps gps-command-spec)