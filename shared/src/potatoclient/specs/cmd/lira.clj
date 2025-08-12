(ns potatoclient.specs.cmd.lira
  "LIRA command specs matching buf.validate constraints.
   Based on jon_shared_cmd_lira.proto.
   All maps use {:closed true} to catch typos and invalid keys."
  (:require
   [potatoclient.malli.registry :as registry]
   [potatoclient.specs.oneof-edn :as oneof-edn]))

;; LIRA command specs - simplified placeholders
;; This is a oneof structure with multiple command types

(def activate-spec [:map {:closed true}])
(def deactivate-spec [:map {:closed true}])
(def set-mode-spec
  [:map {:closed true}
   [:mode [:enum :NORMAL :MAINTENANCE]]])

;; Main LIRA command spec using oneof
(def lira-command-spec
  [:oneof_edn
   [:activate activate-spec]
   [:deactivate deactivate-spec]
   [:set_mode set-mode-spec]
   ;; Add more commands as needed
   ])

(registry/register! :cmd/lira lira-command-spec)