(ns potatoclient.specs.cmd.system
  "System command specs matching buf.validate constraints.
   Based on jon_shared_cmd_system.proto.
   All maps use {:closed true} to catch typos and invalid keys."
  (:require
   [potatoclient.malli.registry :as registry]
   [potatoclient.specs.oneof-edn :as oneof-edn]))

;; System command specs - simplified placeholders
;; This is a oneof structure with multiple command types

(def reboot-spec [:map {:closed true}])
(def shutdown-spec [:map {:closed true}])
(def set-localization-spec
  [:map {:closed true}
   [:locale [:string]]])

;; Main System command spec using oneof
(def system-command-spec
  [:oneof_edn
   [:reboot reboot-spec]
   [:shutdown shutdown-spec]
   [:set_localization set-localization-spec]
   ;; Add more commands as needed
   ])

(registry/register! :cmd/system system-command-spec)