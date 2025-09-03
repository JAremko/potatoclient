(ns potatoclient.cmd.system
  "System command functions matching TypeScript cmdSystem.ts.
   These commands are under the :system key in the cmd root proto."
  (:require
            [malli.core :as m]
    [potatoclient.cmd.core :as core]
    [potatoclient.malli.registry :as registry]
    [potatoclient.specs.common])) ; Load enum specs

;; Initialize registry to access specs
(registry/setup-global-registry!)

;; ============================================================================
;; System Commands (under :system key in payload oneof)
;; ============================================================================

(defn reboot
  "Create a reboot system command.
   Returns a fully formed cmd root ready to send."
  []
  (core/create-command {:system {:reboot {}}})) 
 (m/=> reboot [:=> [:cat] :cmd/root])

(defn power-off
  "Create a power off system command.
   Returns a fully formed cmd root ready to send."
  []
  (core/create-command {:system {:power_off {}}})) 
 (m/=> power-off [:=> [:cat] :cmd/root])

(defn reset-configs
  "Create a reset configs command.
   Returns a fully formed cmd root ready to send."
  []
  (core/create-command {:system {:reset_configs {}}})) 
 (m/=> reset-configs [:=> [:cat] :cmd/root])

(defn start-all
  "Create a start all subsystems command.
   Returns a fully formed cmd root ready to send."
  []
  (core/create-command {:system {:start_all {}}})) 
 (m/=> start-all [:=> [:cat] :cmd/root])

(defn stop-all
  "Create a stop all subsystems command.
   Returns a fully formed cmd root ready to send."
  []
  (core/create-command {:system {:stop_all {}}})) 
 (m/=> stop-all [:=> [:cat] :cmd/root])

(defn mark-rec-important
  "Create a mark recording as important command.
   Returns a fully formed cmd root ready to send."
  []
  (core/create-command {:system {:mark_rec_important {}}})) 
 (m/=> mark-rec-important [:=> [:cat] :cmd/root])

(defn unmark-rec-important
  "Create an unmark recording as important command.
   Returns a fully formed cmd root ready to send."
  []
  (core/create-command {:system {:unmark_rec_important {}}})) 
 (m/=> unmark-rec-important [:=> [:cat] :cmd/root])

(defn set-localization
  "Create a set localization command.
   Localization must be one of the supported enum values:
   - :JON_GUI_DATA_SYSTEM_LOCALIZATION_EN
   - :JON_GUI_DATA_SYSTEM_LOCALIZATION_UA
   - :JON_GUI_DATA_SYSTEM_LOCALIZATION_AR
   - :JON_GUI_DATA_SYSTEM_LOCALIZATION_CS
   Returns a fully formed cmd root ready to send."
  [localization]
  (core/create-command {:system {:localization {:loc localization}}})) 
 (m/=> set-localization [:=> [:cat :enum/system-localizations] :cmd/root])

(defn enter-transport
  "Create an enter transport mode command.
   Returns a fully formed cmd root ready to send."
  []
  (core/create-command {:system {:enter_transport {}}})) 
 (m/=> enter-transport [:=> [:cat] :cmd/root])

(defn enable-geodesic-mode
  "Create an enable geodesic mode command.
   Returns a fully formed cmd root ready to send."
  []
  (core/create-command {:system {:geodesic_mode_enable {}}})) 
 (m/=> enable-geodesic-mode [:=> [:cat] :cmd/root])

(defn disable-geodesic-mode
  "Create a disable geodesic mode command.
   Returns a fully formed cmd root ready to send."
  []
  (core/create-command {:system {:geodesic_mode_disable {}}})) 
 (m/=> disable-geodesic-mode [:=> [:cat] :cmd/root])