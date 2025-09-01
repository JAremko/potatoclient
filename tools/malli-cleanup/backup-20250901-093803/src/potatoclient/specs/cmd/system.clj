(ns potatoclient.specs.cmd.system
  "System command specs matching buf.validate constraints.
   Based on jon_shared_cmd_system.proto.
   All maps use {:closed true} to catch typos and invalid keys."
  (:require
   [potatoclient.malli.registry :as registry]))

;; System command specs - based on proto-explorer findings
;; This is a oneof structure with 13 command types

;; Localization
(def set-localization-spec
  [:map {:closed true}
   [:loc :enum/system-localizations]])

;; Main System command spec using oneof - all 13 commands
(def system-command-spec
  [:oneof
   [:start_all :cmd/empty]
   [:stop_all :cmd/empty]
   [:reboot :cmd/empty]
   [:power_off :cmd/empty]
   [:localization set-localization-spec]
   [:reset_configs :cmd/empty]
   [:start_rec :cmd/empty]
   [:stop_rec :cmd/empty]
   [:mark_rec_important :cmd/empty]
   [:unmark_rec_important :cmd/empty]
   [:enter_transport :cmd/empty]
   [:geodesic_mode_enable :cmd/empty]
   [:geodesic_mode_disable :cmd/empty]])

(registry/register! :cmd/system system-command-spec)