(ns potatoclient.instrumentation
  "Function schema instrumentation for development builds.
   
   This namespace is not AOT compiled and should be loaded manually in development:
   
   ;; In REPL:
   (require 'potatoclient.instrumentation)
   (potatoclient.instrumentation/start!)
   
   This avoids circular dependency issues during startup."
  (:require [malli.core :as m]
            [malli.dev :as dev]
            [malli.dev.pretty :as pretty]
            ;; Core namespaces
            [potatoclient.theme :as theme]
            [potatoclient.i18n :as i18n]
            [potatoclient.config :as config]
            [potatoclient.state :as state]
            [potatoclient.state.streams :as state-streams]
            [potatoclient.state.config :as state-config]
            [potatoclient.state.ui :as state-ui]
            [potatoclient.process :as process]
            [potatoclient.proto :as proto]
            [potatoclient.ipc :as ipc]
            [potatoclient.logging :as logging]
            ;; Event namespaces
            [potatoclient.events.stream :as stream-events]
            ;; UI namespaces
            [potatoclient.ui.control-panel :as control-panel]
            [potatoclient.ui.main-frame :as main-frame]
            ;; Main namespaces
            [potatoclient.core :as core]
            ;; Note: main namespace excluded to avoid circular dependency
            ;; Specs
            [potatoclient.specs :as specs]))

;; -----------------------------------------------------------------------------
;; Theme namespace schemas
;; -----------------------------------------------------------------------------

(m/=> theme/get-current-theme [:=> [:cat] ::specs/theme-key])
(m/=> theme/get-theme [:=> [:cat ::specs/theme-key] [:map-of keyword? any?]])
(m/=> theme/get-theme-color [:=> [:cat ::specs/theme-key keyword?] ::specs/color])
(m/=> theme/apply-theme! [:=> [:cat ::specs/jframe ::specs/theme-key] any?])
(m/=> theme/get-available-themes [:=> [:cat] [:sequential ::specs/theme-key]])
(m/=> theme/get-theme-name [:=> [:cat ::specs/theme-key] string?])
(m/=> theme/apply-nimbus-theme! [:=> [:cat ::specs/theme-key] any?])
(m/=> theme/apply-darklaf-theme! [:=> [:cat ::specs/theme-key] any?])

;; -----------------------------------------------------------------------------
;; I18n namespace schemas
;; -----------------------------------------------------------------------------

(m/=> i18n/get-current-locale [:=> [:cat] ::specs/locale])
(m/=> i18n/tr [:=> [:cat ::specs/translation-key [:* any?]] string?])
(m/=> i18n/get-available-locales [:=> [:cat] [:sequential ::specs/locale]])
(m/=> i18n/set-locale! [:=> [:cat ::specs/locale] any?])
(m/=> i18n/reload-translations! [:=> [:cat] any?])

;; -----------------------------------------------------------------------------
;; Config namespace schemas
;; -----------------------------------------------------------------------------

(m/=> config/get-config-dir [:=> [:cat] ::specs/file])
(m/=> config/get-config-file [:=> [:cat] ::specs/file])
(m/=> config/ensure-config-dir! [:=> [:cat] any?])
(m/=> config/default-config [:=> [:cat] ::specs/config])
(m/=> config/load-config [:=> [:cat] ::specs/config])
(m/=> config/save-config! [:=> [:cat ::specs/config] any?])
(m/=> config/get-config [:=> [:cat ::specs/config-key] any?])
(m/=> config/update-config! [:=> [:cat ::specs/config-key any?] any?])
(m/=> config/get-domain [:=> [:cat] ::specs/domain])
(m/=> config/get-theme [:=> [:cat] ::specs/theme-key])
(m/=> config/get-locale [:=> [:cat] ::specs/locale])

;; -----------------------------------------------------------------------------
;; State namespace schemas
;; -----------------------------------------------------------------------------

(m/=> state/get-stream [:=> [:cat ::specs/stream-key] [:maybe ::specs/stream-process-map]])
(m/=> state/all-streams [:=> [:cat] [:map-of ::specs/stream-key [:maybe ::specs/stream-process-map]]])
(m/=> state/set-stream! [:=> [:cat ::specs/stream-key ::specs/stream-process-map] any?])
(m/=> state/clear-stream! [:=> [:cat ::specs/stream-key] any?])
(m/=> state/get-ui-element [:=> [:cat keyword?] any?])
(m/=> state/register-ui-element! [:=> [:cat keyword? any?] any?])
(m/=> state/get-locale [:=> [:cat] ::specs/locale])
(m/=> state/set-locale! [:=> [:cat ::specs/locale] any?])
(m/=> state/get-domain [:=> [:cat] ::specs/domain])
(m/=> state/set-domain! [:=> [:cat string?] any?])
(m/=> state/current-state [:=> [:cat] map?])

;; -----------------------------------------------------------------------------
;; State.streams namespace schemas
;; -----------------------------------------------------------------------------

(m/=> state-streams/get-stream [:=> [:cat ::specs/stream-key] [:maybe ::specs/stream-process-map]])
(m/=> state-streams/set-stream! [:=> [:cat ::specs/stream-key ::specs/stream-process-map] any?])
(m/=> state-streams/clear-stream! [:=> [:cat ::specs/stream-key] any?])
(m/=> state-streams/all-streams [:=> [:cat] [:map-of ::specs/stream-key [:maybe ::specs/stream-process-map]]])

;; -----------------------------------------------------------------------------
;; State.config namespace schemas
;; -----------------------------------------------------------------------------

(m/=> state-config/get-locale [:=> [:cat] ::specs/locale])
(m/=> state-config/set-locale! [:=> [:cat ::specs/locale] any?])
(m/=> state-config/get-domain [:=> [:cat] ::specs/domain])
(m/=> state-config/set-domain! [:=> [:cat string?] any?])
(m/=> state-config/get-config [:=> [:cat] map?])

;; -----------------------------------------------------------------------------
;; State.ui namespace schemas
;; -----------------------------------------------------------------------------

(m/=> state-ui/register-ui-element! [:=> [:cat keyword? any?] any?])
(m/=> state-ui/get-ui-element [:=> [:cat keyword?] any?])
(m/=> state-ui/all-ui-elements [:=> [:cat] [:sequential keyword?]])

;; -----------------------------------------------------------------------------
;; Events.stream namespace schemas
;; -----------------------------------------------------------------------------

(m/=> stream-events/format-window-event [:=> [:cat ::specs/window-event] string?])
(m/=> stream-events/format-navigation-event [:=> [:cat ::specs/navigation-event] string?])
(m/=> stream-events/handle-response-event [:=> [:cat keyword? map?] nil?])
(m/=> stream-events/handle-navigation-event [:=> [:cat [:map [:event map?]]] nil?])
(m/=> stream-events/handle-window-event [:=> [:cat [:map [:event map?]]] nil?])
(m/=> stream-events/stream-connected? [:=> [:cat ::specs/stream-key] boolean?])
(m/=> stream-events/all-streams-connected? [:=> [:cat] boolean?])

;; -----------------------------------------------------------------------------
;; Proto namespace schemas
;; -----------------------------------------------------------------------------

(m/=> proto/create-builder [:=> [:cat keyword?] any?])
(m/=> proto/set-field! [:=> [:cat any? keyword? any?] any?])
(m/=> proto/build-message [:=> [:cat any?] any?])
(m/=> proto/create-message [:=> [:cat keyword? map?] any?])
(m/=> proto/message->bytes [:=> [:cat any?] bytes?])
(m/=> proto/write-delimited [:=> [:cat any? any?] any?])
(m/=> proto/parse-command [:=> [:cat bytes?] [:maybe ::specs/command]])
(m/=> proto/create-gimbal-angle-command [:=> [:cat number? number?] ::specs/command])
(m/=> proto/create-lrf-request-command [:=> [:cat] ::specs/command])
(m/=> proto/create-lrf-single-pulse-command [:=> [:cat] ::specs/command])
(m/=> proto/create-window-event [:=> [:cat map?] bytes?])
(m/=> proto/create-navigation-event [:=> [:cat map?] bytes?])
(m/=> proto/handle-window-event [:=> [:cat ::specs/stream-process-map map?] any?])
(m/=> proto/handle-navigation-event [:=> [:cat ::specs/stream-process-map map?] any?])
(m/=> proto/handle-gimbal-command [:=> [:cat number? number?] any?])
(m/=> proto/handle-lrf-command [:=> [:cat keyword?] any?])

;; -----------------------------------------------------------------------------
;; Logging namespace schemas
;; -----------------------------------------------------------------------------

(m/=> logging/init! [:=> [:cat] any?])
(m/=> logging/shutdown! [:=> [:cat] any?])

;; -----------------------------------------------------------------------------
;; IPC namespace schemas
;; -----------------------------------------------------------------------------

(m/=> ipc/parse-message [:=> [:cat string?] [:maybe ::specs/message]])
(m/=> ipc/format-error [:=> [:cat string? ::specs/exception] string?])
(m/=> ipc/handle-message [:=> [:cat ::specs/stream-key string?] any?])
(m/=> ipc/process-stream-output [:=> [:cat ::specs/stream-key any?] any?])
(m/=> ipc/start-output-processor [:=> [:cat ::specs/stream-key ::specs/stream-process-map] any?])

;; -----------------------------------------------------------------------------
;; Process namespace schemas
;; -----------------------------------------------------------------------------

(m/=> process/create-process-builder [:=> [:cat string? [:sequential string?]] any?])
(m/=> process/start-stream-process [:=> [:cat ::specs/stream-key] [:maybe ::specs/stream-process-map]])
(m/=> process/stop-stream [:=> [:cat ::specs/stream-process-map] any?])
(m/=> process/send-command [:=> [:cat ::specs/stream-process-map ::specs/process-command] any?])
(m/=> process/get-stream-state [:=> [:cat ::specs/stream-process-map] ::specs/process-state])

;; -----------------------------------------------------------------------------
;; UI.control-panel namespace schemas
;; -----------------------------------------------------------------------------

(m/=> control-panel/create-stream-button [:=> [:cat ::specs/stream-key string?] ::specs/jtoggle-button])
(m/=> control-panel/toggle-stream [:=> [:cat ::specs/stream-key ::specs/jtoggle-button] any?])
(m/=> control-panel/create-control-button [:=> [:cat keyword? string? any?] ::specs/jbutton])
(m/=> control-panel/create-gimbal-controls [:=> [:cat] ::specs/jpanel])
(m/=> control-panel/create-control-panel [:=> [:cat] ::specs/jpanel])

;; -----------------------------------------------------------------------------
;; UI.main-frame namespace schemas
;; -----------------------------------------------------------------------------

(m/=> main-frame/save-window-state [:=> [:cat ::specs/jframe] any?])
(m/=> main-frame/load-window-state [:=> [:cat] [:maybe ::specs/window-state]])
(m/=> main-frame/restore-window-state [:=> [:cat ::specs/jframe] any?])
(m/=> main-frame/make-centered [:=> [:cat ::specs/jframe] any?])
(m/=> main-frame/setup-window-persistence [:=> [:cat ::specs/jframe] any?])
(m/=> main-frame/create-language-menu [:=> [:cat] ::specs/jmenu])
(m/=> main-frame/create-theme-menu [:=> [:cat ::specs/jframe] ::specs/jmenu])
(m/=> main-frame/create-view-menu [:=> [:cat ::specs/jframe] ::specs/jmenu])
(m/=> main-frame/create-help-menu [:=> [:cat] ::specs/jmenu])
(m/=> main-frame/create-menu-bar [:=> [:cat ::specs/jframe] ::specs/jmenu-bar])
(m/=> main-frame/create-main-frame [:=> [:cat map?] ::specs/jframe])

;; -----------------------------------------------------------------------------
;; Core namespace schemas
;; -----------------------------------------------------------------------------

(m/=> core/start-application [:=> [:cat] any?])
(m/=> core/shutdown-application [:=> [:cat] any?])

;; -----------------------------------------------------------------------------
;; Instrumentation control
;; -----------------------------------------------------------------------------

(defonce ^:private instrumentation-started? (atom false))

(defn start!
  "Start function instrumentation.
   
   This should only be called in development environments after
   the application has been initialized."
  []
  (if @instrumentation-started?
    (println "Instrumentation already started")
    (do
      (println "Starting Malli function instrumentation...")
      (dev/start! {:report (pretty/reporter)})
      (reset! instrumentation-started? true)
      (println "Instrumentation started successfully"))))

(defn stop!
  "Stop function instrumentation."
  []
  (if @instrumentation-started?
    (do
      (println "Stopping Malli function instrumentation...")
      (dev/stop!)
      (reset! instrumentation-started? false)
      (println "Instrumentation stopped"))
    (println "Instrumentation not running")))