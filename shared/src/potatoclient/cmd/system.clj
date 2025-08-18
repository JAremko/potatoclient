(ns potatoclient.cmd.system
  "System command functions matching TypeScript cmdSystem.ts.
   These commands are under the :system key in the cmd root proto."
  (:require
   [com.fulcrologic.guardrails.core :refer [>defn >defn- => | ?]]
   [clojure.spec.alpha :as s]
   [potatoclient.cmd.core :as core]))

;; ============================================================================
;; Specs
;; ============================================================================

(s/def ::cmd-root (s/and map?
                         #(some? (dissoc % :protocol_version :client_type 
                                        :session_id :important :from_cv_subsystem))))

;; ============================================================================
;; System Commands (under :system key in payload oneof)
;; ============================================================================

(>defn reboot
  "Reboot the system."
  []
  [=> ::cmd-root]
  (core/send-command! {:system {:reboot {}}}))

(>defn power-off
  "Power off the system."
  []
  [=> ::cmd-root]
  (core/send-command! {:system {:power_off {}}}))

(>defn reset-configs
  "Reset system configurations to defaults."
  []
  [=> ::cmd-root]
  (core/send-command! {:system {:reset_configs {}}}))

(>defn start-all
  "Start all system subsystems."
  []
  [=> ::cmd-root]
  (core/send-command! {:system {:start_all {}}}))

(>defn stop-all
  "Stop all system subsystems."
  []
  [=> ::cmd-root]
  (core/send-command! {:system {:stop_all {}}}))

(>defn mark-rec-important
  "Mark current recording as important."
  []
  [=> ::cmd-root]
  (core/send-command! {:system {:mark_rec_important {}}}))

(>defn unmark-rec-important
  "Unmark current recording as important."
  []
  [=> ::cmd-root]
  (core/send-command! {:system {:unmark_rec_important {}}}))

(>defn set-localization
  "Set system localization/language.
   Localization must be one of the supported enum values:
   - :JON_GUI_DATA_SYSTEM_LOCALIZATION_EN
   - :JON_GUI_DATA_SYSTEM_LOCALIZATION_UA
   - :JON_GUI_DATA_SYSTEM_LOCALIZATION_AR
   - :JON_GUI_DATA_SYSTEM_LOCALIZATION_CS"
  [localization]
  [keyword? => ::cmd-root]
  (core/send-command! {:system {:localization {:loc localization}}}))

(>defn enter-transport
  "Enter transport mode - prepares system for transportation."
  []
  [=> ::cmd-root]
  (core/send-command! {:system {:enter_transport {}}}))

(>defn enable-geodesic-mode
  "Enable geodesic mode for accurate long-range calculations."
  []
  [=> ::cmd-root]
  (core/send-command! {:system {:geodesic_mode_enable {}}}))

(>defn disable-geodesic-mode
  "Disable geodesic mode."
  []
  [=> ::cmd-root]
  (core/send-command! {:system {:geodesic_mode_disable {}}}))