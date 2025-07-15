(ns potatoclient.instrumentation
  "Function schema instrumentation for development builds.
   This namespace is not AOT compiled and is loaded conditionally."
  (:require [malli.core :as m]
            [malli.dev :as dev]
            [malli.dev.pretty :as pretty]
            ;; Core namespaces
            [potatoclient.theme :as theme]
            [potatoclient.i18n :as i18n]
            [potatoclient.config :as config]
            [potatoclient.state :as state]
            [potatoclient.process :as process]
            [potatoclient.proto :as proto]
            [potatoclient.ipc :as ipc]
            [potatoclient.log-writer :as log-writer]
            ;; Event namespaces
            [potatoclient.events.stream :as stream-events]
            [potatoclient.events.log :as log-events]
            ;; UI namespaces
            [potatoclient.ui.log-export :as log-export]
            [potatoclient.ui.log-table :as log-table]
            [potatoclient.ui.control-panel :as control-panel]
            [potatoclient.ui.main-frame :as main-frame]
            ;; Main namespaces
            [potatoclient.core :as core]
            [potatoclient.main :as main]
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
(m/=> state/get-all-streams [:=> [:cat] [:map-of ::specs/stream-key ::specs/stream-process-map]])
(m/=> state/set-stream! [:=> [:cat ::specs/stream-key ::specs/stream-process-map] any?])
(m/=> state/clear-stream! [:=> [:cat ::specs/stream-key] any?])
(m/=> state/get-app-config [:=> [:cat ::specs/config-key] any?])
(m/=> state/update-app-config! [:=> [:cat ::specs/config-key any?] any?])
(m/=> state/get-logs [:=> [:cat] [:sequential ::specs/log-entry]])
(m/=> state/get-ui-element [:=> [:cat keyword?] any?])
(m/=> state/set-ui-element! [:=> [:cat keyword? any?] any?])

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
;; Events.log namespace schemas
;; -----------------------------------------------------------------------------

(m/=> log-events/add-log-entry! [:=> [:cat ::specs/log-entry] any?])
(m/=> log-events/get-next-id [:=> [:cat] int?])
(m/=> log-events/trim-logs! [:=> [:cat] any?])
(m/=> log-events/update-table-model! [:=> [:cat] any?])
(m/=> log-events/scroll-to-bottom! [:=> [:cat] any?])
(m/=> log-events/log-error [:=> [:cat string? string? [:* [:alt [:tuple keyword? any?]]]] any?])

;; -----------------------------------------------------------------------------
;; Proto namespace schemas
;; -----------------------------------------------------------------------------

(m/=> proto/create-builder [:=> [:cat keyword?] any?])
(m/=> proto/set-field! [:=> [:cat any? keyword? any?] any?])
(m/=> proto/build-message [:=> [:cat any?] any?])
(m/=> proto/create-message [:=> [:cat keyword? map?] any?])
(m/=> proto/message->bytes [:=> [:cat any?] bytes?])
(m/=> proto/write-delimited [:=> [:cat any? ::specs/buffered-writer] any?])
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
;; Log-writer namespace schemas
;; -----------------------------------------------------------------------------

(m/=> log-writer/ensure-log-directory! [:=> [:cat] any?])
(m/=> log-writer/get-log-file-name [:=> [:cat] string?])
(m/=> log-writer/get-current-log-file [:=> [:cat] ::specs/file])
(m/=> log-writer/rotate-log-if-needed! [:=> [:cat] any?])
(m/=> log-writer/format-log-entry [:=> [:cat ::specs/log-entry] string?])
(m/=> log-writer/write-log-entry! [:=> [:cat ::specs/log-entry] any?])
(m/=> log-writer/init-log-writer! [:=> [:cat] any?])
(m/=> log-writer/shutdown-log-writer! [:=> [:cat] any?])

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
;; UI.log-export namespace schemas
;; -----------------------------------------------------------------------------

(m/=> log-export/export-logs [:=> [:cat ::specs/jframe] any?])

;; -----------------------------------------------------------------------------
;; UI.log-table namespace schemas
;; -----------------------------------------------------------------------------

(m/=> log-table/create-table-model [:=> [:cat] any?])
(m/=> log-table/create-cell-renderer [:=> [:cat] ::specs/table-cell-renderer])
(m/=> log-table/create-log-table [:=> [:cat] any?])
(m/=> log-table/create-log-panel [:=> [:cat] ::specs/jscroll-pane])

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
(m/=> main-frame/set-icon [:=> [:cat ::specs/jframe] any?])
(m/=> main-frame/create-main-frame [:=> [:cat ::specs/frame-params] ::specs/jframe])
(m/=> main-frame/create-theme-menu-item [:=> [:cat ::specs/theme-key ::specs/jframe] any?])
(m/=> main-frame/create-locale-menu-item [:=> [:cat ::specs/locale] any?])
(m/=> main-frame/create-help-action [:=> [:cat ::specs/jframe ::specs/frame-params] ::specs/action])
(m/=> main-frame/create-menu-bar [:=> [:cat ::specs/jframe ::specs/frame-params] ::specs/jmenu-bar])
(m/=> main-frame/show-about-dialog [:=> [:cat ::specs/jframe ::specs/frame-params] any?])

;; -----------------------------------------------------------------------------
;; Core namespace schemas
;; -----------------------------------------------------------------------------

(m/=> core/setup-shutdown-hook [:=> [:cat any?] any?])
(m/=> core/initialize-ui! [:=> [:cat] any?])
(m/=> core/build-type-string [:=> [:cat] string?])
(m/=> core/setup-exception-handler! [:=> [:cat] any?])
(m/=> core/-main [:=> [:cat [:* string?]] any?])
(m/=> core/shutdown! [:=> [:cat] any?])

;; -----------------------------------------------------------------------------
;; Main namespace schemas
;; -----------------------------------------------------------------------------

(m/=> main/release-build? [:=> [:cat] boolean?])
(m/=> main/enable-instrumentation! [:=> [:cat] any?])
(m/=> main/enable-dev-mode! [:=> [:cat] any?])
(m/=> main/-main [:=> [:cat [:* string?]] any?])

;; -----------------------------------------------------------------------------
;; Instrumentation startup
;; -----------------------------------------------------------------------------

(defn start!
  "Start Malli instrumentation with pretty error reporting."
  []
  (println "Starting Malli function instrumentation...")
  (dev/start! {:report (pretty/thrower)})
  (println "Malli function instrumentation enabled."))