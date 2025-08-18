(ns potatoclient.cmd.system
  "System command functions matching TypeScript cmdSystem.ts.
   These commands are under the :system key in the cmd root proto."
  (:require
   [com.fulcrologic.guardrails.malli.core :refer [>defn >defn- => | ?]]
   [potatoclient.cmd.core :as core]))

;; ============================================================================
;; System Commands (under :system key in payload oneof)
;; ============================================================================

(>defn reboot
  "Create a reboot system command.
   Returns a fully formed cmd root ready to send."
  []
  [=> :cmd/root]
  (core/create-command {:system {:reboot {}}}))

(>defn power-off
  "Create a power off system command.
   Returns a fully formed cmd root ready to send."
  []
  [=> :cmd/root]
  (core/create-command {:system {:power_off {}}}))

(>defn reset-configs
  "Create a reset configs command.
   Returns a fully formed cmd root ready to send."
  []
  [=> :cmd/root]
  (core/create-command {:system {:reset_configs {}}}))

(>defn start-all
  "Create a start all subsystems command.
   Returns a fully formed cmd root ready to send."
  []
  [=> :cmd/root]
  (core/create-command {:system {:start_all {}}}))

(>defn stop-all
  "Create a stop all subsystems command.
   Returns a fully formed cmd root ready to send."
  []
  [=> :cmd/root]
  (core/create-command {:system {:stop_all {}}}))

(>defn mark-rec-important
  "Create a mark recording as important command.
   Returns a fully formed cmd root ready to send."
  []
  [=> :cmd/root]
  (core/create-command {:system {:mark_rec_important {}}}))

(>defn unmark-rec-important
  "Create an unmark recording as important command.
   Returns a fully formed cmd root ready to send."
  []
  [=> :cmd/root]
  (core/create-command {:system {:unmark_rec_important {}}}))

(>defn set-localization
  "Create a set localization command.
   Localization must be one of the supported enum values:
   - :JON_GUI_DATA_SYSTEM_LOCALIZATION_EN
   - :JON_GUI_DATA_SYSTEM_LOCALIZATION_UA
   - :JON_GUI_DATA_SYSTEM_LOCALIZATION_AR
   - :JON_GUI_DATA_SYSTEM_LOCALIZATION_CS
   Returns a fully formed cmd root ready to send."
  [localization]
  [:keyword => :cmd/root]
  (core/create-command {:system {:localization {:loc localization}}}))

(>defn enter-transport
  "Create an enter transport mode command.
   Returns a fully formed cmd root ready to send."
  []
  [=> :cmd/root]
  (core/create-command {:system {:enter_transport {}}}))

(>defn enable-geodesic-mode
  "Create an enable geodesic mode command.
   Returns a fully formed cmd root ready to send."
  []
  [=> :cmd/root]
  (core/create-command {:system {:geodesic_mode_enable {}}}))

(>defn disable-geodesic-mode
  "Create a disable geodesic mode command.
   Returns a fully formed cmd root ready to send."
  []
  [=> :cmd/root]
  (core/create-command {:system {:geodesic_mode_disable {}}}))