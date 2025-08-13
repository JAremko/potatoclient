(ns potatoclient.specs.cmd.system
  "System command specs matching buf.validate constraints.
   Based on jon_shared_cmd_system.proto.
   All maps use {:closed true} to catch typos and invalid keys."
  (:require
   [potatoclient.malli.registry :as registry]
   [potatoclient.malli.oneof :as oneof]))

;; System command specs - based on proto-explorer findings
;; This is a oneof structure with 13 command types

;; Basic control
(def start-all-spec [:map {:closed true}])
(def stop-all-spec [:map {:closed true}])
(def reboot-spec [:map {:closed true}])
(def power-off-spec [:map {:closed true}])
(def reset-configs-spec [:map {:closed true}])
(def enter-transport-spec [:map {:closed true}])

;; Localization
(def localization-spec
  [:map {:closed true}
   [:locale [:enum
             :JON_GUI_DATA_LOCALE_UNSPECIFIED
             :JON_GUI_DATA_LOCALE_EN
             :JON_GUI_DATA_LOCALE_HE
             :JON_GUI_DATA_LOCALE_RU]]])

;; Recording control
(def start-rec-spec [:map {:closed true}])
(def stop-rec-spec [:map {:closed true}])
(def mark-rec-important-spec [:map {:closed true}])
(def unmark-rec-important-spec [:map {:closed true}])

;; Geodesic mode
(def geodesic-mode-enable-spec [:map {:closed true}])
(def geodesic-mode-disable-spec [:map {:closed true}])

;; Main System command spec using oneof - all 13 commands
(def system-command-spec
  [:oneof
   [:start_all start-all-spec]
   [:stop_all stop-all-spec]
   [:reboot reboot-spec]
   [:power_off power-off-spec]
   [:localization localization-spec]
   [:reset_configs reset-configs-spec]
   [:start_rec start-rec-spec]
   [:stop_rec stop-rec-spec]
   [:mark_rec_important mark-rec-important-spec]
   [:unmark_rec_important unmark-rec-important-spec]
   [:enter_transport enter-transport-spec]
   [:geodesic_mode_enable geodesic-mode-enable-spec]
   [:geodesic_mode_disable geodesic-mode-disable-spec]])

(registry/register! :cmd/system system-command-spec)