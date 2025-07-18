(ns potatoclient.instrumentation
  "Function schema instrumentation for development builds.
   
   This namespace is not AOT compiled and should be loaded manually in development:
   
   ;; In REPL:
   (require 'potatoclient.instrumentation)
   (potatoclient.instrumentation/start!)
   
   This avoids circular dependency issues during startup."
  (:require [malli.core :as m]
            [malli.registry :as mr]
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
            [potatoclient.ui.log-viewer :as log-viewer]
            [potatoclient.ui.startup-dialog :as startup-dialog]
            [potatoclient.ui.utils :as ui-utils]
            ;; Main namespaces
            [potatoclient.core :as core]
            [potatoclient.main :as main]
            ;; Specs
            [potatoclient.specs :as specs]))

;; -----------------------------------------------------------------------------
;; Theme namespace schemas
;; -----------------------------------------------------------------------------

(m/=> potatoclient.theme/get-current-theme [:=> [:cat] :potatoclient.specs/theme-key])
(m/=> potatoclient.theme/get-theme [:=> [:cat :potatoclient.specs/theme-key] [:map-of keyword? [:or string? :potatoclient.specs/color]]])
(m/=> potatoclient.theme/get-theme-color [:=> [:cat :potatoclient.specs/theme-key keyword?] :potatoclient.specs/color])
(m/=> potatoclient.theme/apply-theme! [:=> [:cat :potatoclient.specs/jframe :potatoclient.specs/theme-key] nil?])
(m/=> potatoclient.theme/get-available-themes [:=> [:cat] [:sequential :potatoclient.specs/theme-key]])
(m/=> potatoclient.theme/get-theme-name [:=> [:cat :potatoclient.specs/theme-key] string?])
(m/=> potatoclient.theme/apply-nimbus-theme! [:=> [:cat :potatoclient.specs/theme-key] nil?])
(m/=> potatoclient.theme/apply-darklaf-theme! [:=> [:cat :potatoclient.specs/theme-key] nil?])
(m/=> potatoclient.theme/initialize-theme! [:=> [:cat :potatoclient.specs/theme-key] nil?])
(m/=> potatoclient.theme/set-theme! [:=> [:cat :potatoclient.specs/theme-key] boolean?])
(m/=> potatoclient.theme/get-theme-i18n-key [:=> [:cat :potatoclient.specs/theme-key] keyword?])
(m/=> potatoclient.theme/preload-theme-icons! [:=> [:cat] nil?])
(m/=> potatoclient.theme/key->icon [:=> [:cat keyword? :potatoclient.specs/theme-key] :potatoclient.specs/icon])

;; -----------------------------------------------------------------------------
;; I18n namespace schemas
;; -----------------------------------------------------------------------------

(m/=> potatoclient.i18n/init! [:=> [:cat] nil?])
(m/=> potatoclient.i18n/load-translation-file [:=> [:cat :potatoclient.specs/locale-code] [:maybe [:map-of keyword? string?]]])
(m/=> potatoclient.i18n/load-translations! [:=> [:cat] :potatoclient.specs/translations-map])
(m/=> potatoclient.i18n/get-current-locale [:=> [:cat] :potatoclient.specs/locale])
(m/=> potatoclient.i18n/tr [:=> [:cat :potatoclient.specs/translation-key :potatoclient.specs/translation-args] string?])
(m/=> potatoclient.i18n/get-available-locales [:=> [:cat] [:sequential :potatoclient.specs/locale]])
(m/=> potatoclient.i18n/set-locale! [:=> [:cat :potatoclient.specs/locale] nil?])
(m/=> potatoclient.i18n/reload-translations! [:=> [:cat] :potatoclient.specs/translations-map])

;; -----------------------------------------------------------------------------
;; Config namespace schemas
;; -----------------------------------------------------------------------------

(m/=> potatoclient.config/get-config-dir [:=> [:cat] :potatoclient.specs/file])
(m/=> potatoclient.config/get-config-file [:=> [:cat] :potatoclient.specs/file])
(m/=> potatoclient.config/ensure-config-dir! [:=> [:cat] nil?])
(m/=> potatoclient.config/load-config [:=> [:cat] :potatoclient.specs/config])
(m/=> potatoclient.config/save-config! [:=> [:cat :potatoclient.specs/config] boolean?])
(m/=> potatoclient.config/get-theme [:=> [:cat] :potatoclient.specs/theme-key])
(m/=> potatoclient.config/save-theme! [:=> [:cat :potatoclient.specs/theme-key] boolean?])
(m/=> potatoclient.config/extract-domain [:=> [:cat string?] :potatoclient.specs/domain])
(m/=> potatoclient.config/get-most-recent-url [:=> [:cat] [:maybe string?]])
(m/=> potatoclient.config/get-domain [:=> [:cat] :potatoclient.specs/domain])
(m/=> potatoclient.config/get-locale [:=> [:cat] :potatoclient.specs/locale])
(m/=> potatoclient.config/save-locale! [:=> [:cat :potatoclient.specs/locale] boolean?])
(m/=> potatoclient.config/update-config! [:=> [:cat :potatoclient.specs/config-key [:or :potatoclient.specs/theme-key :potatoclient.specs/domain :potatoclient.specs/locale :potatoclient.specs/url-history]] boolean?])
(m/=> potatoclient.config/get-config-location [:=> [:cat] string?])
(m/=> potatoclient.config/initialize! [:=> [:cat] :potatoclient.specs/config])
(m/=> potatoclient.config/get-url-history [:=> [:cat] :potatoclient.specs/url-history])
(m/=> potatoclient.config/add-url-to-history [:=> [:cat string?] nil?])
(m/=> potatoclient.config/get-recent-urls [:=> [:cat] [:vector string?]])

;; -----------------------------------------------------------------------------
;; State namespace schemas
;; -----------------------------------------------------------------------------

(m/=> potatoclient.state/get-stream [:=> [:cat :potatoclient.specs/stream-key] [:maybe :potatoclient.specs/stream-process-map]])
(m/=> potatoclient.state/all-streams [:=> [:cat] [:map-of :potatoclient.specs/stream-key [:maybe :potatoclient.specs/stream-process-map]]])
(m/=> potatoclient.state/set-stream! [:=> [:cat :potatoclient.specs/stream-key :potatoclient.specs/stream-process-map] nil?])
(m/=> potatoclient.state/clear-stream! [:=> [:cat :potatoclient.specs/stream-key] nil?])
(m/=> potatoclient.state/get-ui-element [:=> [:cat keyword?] [:maybe [:or :potatoclient.specs/jbutton :potatoclient.specs/jtoggle-button :potatoclient.specs/jframe :potatoclient.specs/jpanel]]])
(m/=> potatoclient.state/register-ui-element! [:=> [:cat keyword? [:or :potatoclient.specs/jbutton :potatoclient.specs/jtoggle-button :potatoclient.specs/jframe :potatoclient.specs/jpanel]] nil?])
(m/=> potatoclient.state/get-locale [:=> [:cat] :potatoclient.specs/locale])
(m/=> potatoclient.state/set-locale! [:=> [:cat :potatoclient.specs/locale] nil?])
(m/=> potatoclient.state/get-domain [:=> [:cat] :potatoclient.specs/domain])
(m/=> potatoclient.state/set-domain! [:=> [:cat :potatoclient.specs/domain] nil?])
(m/=> potatoclient.state/current-state [:=> [:cat] [:map
                                                    [:streams [:map-of :potatoclient.specs/stream-key [:maybe :potatoclient.specs/stream-process-map]]]
                                                    [:config [:map
                                                              [:locale :potatoclient.specs/locale]
                                                              [:domain :potatoclient.specs/domain]]]]])

;; -----------------------------------------------------------------------------
;; State.streams namespace schemas
;; -----------------------------------------------------------------------------

(m/=> potatoclient.state.streams/get-stream [:=> [:cat :potatoclient.specs/stream-key] [:maybe :potatoclient.specs/stream-process-map]])
(m/=> potatoclient.state.streams/set-stream! [:=> [:cat :potatoclient.specs/stream-key :potatoclient.specs/stream-process-map] nil?])
(m/=> potatoclient.state.streams/clear-stream! [:=> [:cat :potatoclient.specs/stream-key] nil?])
(m/=> potatoclient.state.streams/all-streams [:=> [:cat] [:map-of :potatoclient.specs/stream-key [:maybe :potatoclient.specs/stream-process-map]]])

;; -----------------------------------------------------------------------------
;; State.config namespace schemas
;; -----------------------------------------------------------------------------

(m/=> potatoclient.state.config/get-locale [:=> [:cat] :potatoclient.specs/locale])
(m/=> potatoclient.state.config/set-locale! [:=> [:cat :potatoclient.specs/locale] nil?])
(m/=> potatoclient.state.config/get-domain [:=> [:cat] :potatoclient.specs/domain])
(m/=> potatoclient.state.config/set-domain! [:=> [:cat :potatoclient.specs/domain] nil?])
(m/=> potatoclient.state.config/get-config [:=> [:cat] [:map
                                                        [:locale :potatoclient.specs/locale]
                                                        [:domain :potatoclient.specs/domain]]])

;; -----------------------------------------------------------------------------
;; State.ui namespace schemas
;; -----------------------------------------------------------------------------

(m/=> potatoclient.state.ui/register-ui-element! [:=> [:cat keyword? [:or :potatoclient.specs/jbutton :potatoclient.specs/jtoggle-button :potatoclient.specs/jframe :potatoclient.specs/jpanel]] nil?])
(m/=> potatoclient.state.ui/get-ui-element [:=> [:cat keyword?] [:maybe [:or :potatoclient.specs/jbutton :potatoclient.specs/jtoggle-button :potatoclient.specs/jframe :potatoclient.specs/jpanel]]])
(m/=> potatoclient.state.ui/all-ui-elements [:=> [:cat] [:sequential keyword?]])

;; -----------------------------------------------------------------------------
;; Events.stream namespace schemas
;; -----------------------------------------------------------------------------

(m/=> potatoclient.events.stream/format-window-event [:=> [:cat :potatoclient.specs/window-event] string?])
(m/=> potatoclient.events.stream/format-navigation-event [:=> [:cat :potatoclient.specs/navigation-event] string?])
(m/=> potatoclient.events.stream/handle-response-event [:=> [:cat keyword? map?] nil?])
(m/=> potatoclient.events.stream/handle-navigation-event [:=> [:cat [:map [:event map?]]] nil?])
(m/=> potatoclient.events.stream/handle-window-event [:=> [:cat [:map [:event map?]]] nil?])
(m/=> potatoclient.events.stream/stream-connected? [:=> [:cat :potatoclient.specs/stream-key] boolean?])
(m/=> potatoclient.events.stream/all-streams-connected? [:=> [:cat] boolean?])

;; -----------------------------------------------------------------------------
;; Proto namespace schemas
;; -----------------------------------------------------------------------------

;; Proto mapper constructors (generated by pronto/defmapper)
;; These are auto-generated by pronto and have dynamic types
(m/=> potatoclient.proto/->ProtoMapper_potatoclient_proto_proto-mapper [:=> [:cat any? any?] any?])
(m/=> potatoclient.proto/map->ProtoMapper_potatoclient_proto_proto-mapper [:=> [:cat map?] any?])

;; Command serialization/deserialization
(m/=> potatoclient.proto/serialize-cmd [:=> [:cat :potatoclient.specs/command] bytes?])
(m/=> potatoclient.proto/deserialize-state [:=> [:cat bytes?] map?])

;; Command factory functions
(m/=> potatoclient.proto/cmd-ping [:=> [:cat pos-int? [:enum :internal-cv :local :certificate :lira]] :potatoclient.specs/command])
(m/=> potatoclient.proto/cmd-noop [:=> [:cat pos-int? [:enum :internal-cv :local :certificate :lira]] :potatoclient.specs/command])
(m/=> potatoclient.proto/cmd-frozen [:=> [:cat pos-int? [:enum :internal-cv :local :certificate :lira]] :potatoclient.specs/command])

;; State accessor functions
(m/=> potatoclient.proto/get-system-info [:=> [:cat map?] [:maybe map?]])
(m/=> potatoclient.proto/get-camera-day [:=> [:cat map?] [:maybe map?]])
(m/=> potatoclient.proto/get-camera-heat [:=> [:cat map?] [:maybe map?]])
(m/=> potatoclient.proto/get-gps-info [:=> [:cat map?] [:maybe map?]])
(m/=> potatoclient.proto/get-compass-info [:=> [:cat map?] [:maybe map?]])
(m/=> potatoclient.proto/get-lrf-info [:=> [:cat map?] [:maybe map?]])
(m/=> potatoclient.proto/get-time-info [:=> [:cat map?] [:maybe map?]])
(m/=> potatoclient.proto/get-location [:=> [:cat map?] [:maybe [:map
                                                                [:latitude number?]
                                                                [:longitude number?]
                                                                [:altitude number?]
                                                                [:heading {:optional true} number?]
                                                                [:timestamp {:optional true} [:or string? inst?]]]]])

;; State query functions
(m/=> potatoclient.proto/cameras-available? [:=> [:cat map?] boolean?])

;; Validation functions
(m/=> potatoclient.proto/valid-command? [:=> [:cat map?] boolean?])
(m/=> potatoclient.proto/explain-invalid-command [:=> [:cat map?] string?])
(m/=> potatoclient.proto/explain-invalid-command? [:=> [:cat map?] string?])

;; Legacy proto functions (if they still exist)
;; These work with Java protobuf builders/messages which have dynamic types
(m/=> potatoclient.proto/create-builder [:=> [:cat keyword?] any?])
(m/=> potatoclient.proto/set-field! [:=> [:cat any? keyword? any?] nil?])
(m/=> potatoclient.proto/build-message [:=> [:cat any?] any?])
(m/=> potatoclient.proto/create-message [:=> [:cat keyword? map?] any?])
(m/=> potatoclient.proto/message->bytes [:=> [:cat any?] bytes?])
(m/=> potatoclient.proto/write-delimited [:=> [:cat any? any?] nil?])
(m/=> potatoclient.proto/parse-command [:=> [:cat bytes?] [:maybe :potatoclient.specs/command]])
(m/=> potatoclient.proto/create-gimbal-angle-command [:=> [:cat number? number?] :potatoclient.specs/command])
(m/=> potatoclient.proto/create-lrf-request-command [:=> [:cat] :potatoclient.specs/command])
(m/=> potatoclient.proto/create-lrf-single-pulse-command [:=> [:cat] :potatoclient.specs/command])
(m/=> potatoclient.proto/create-window-event [:=> [:cat :potatoclient.specs/window-event] bytes?])
(m/=> potatoclient.proto/create-navigation-event [:=> [:cat :potatoclient.specs/navigation-event] bytes?])
(m/=> potatoclient.proto/handle-window-event [:=> [:cat :potatoclient.specs/stream-process-map :potatoclient.specs/window-event] nil?])
(m/=> potatoclient.proto/handle-navigation-event [:=> [:cat :potatoclient.specs/stream-process-map :potatoclient.specs/navigation-event] nil?])
(m/=> potatoclient.proto/handle-gimbal-command [:=> [:cat number? number?] nil?])
(m/=> potatoclient.proto/handle-lrf-command [:=> [:cat :potatoclient.specs/payload-type] nil?])

;; -----------------------------------------------------------------------------
;; Logging namespace schemas
;; -----------------------------------------------------------------------------

(m/=> potatoclient.logging/init! [:=> [:cat] nil?])
(m/=> potatoclient.logging/shutdown! [:=> [:cat] nil?])
(m/=> potatoclient.logging/get-logs-directory [:=> [:cat] :potatoclient.specs/file])

;; -----------------------------------------------------------------------------
;; IPC namespace schemas
;; -----------------------------------------------------------------------------

(m/=> potatoclient.ipc/parse-message [:=> [:cat string?] [:maybe :potatoclient.specs/message]])
(m/=> potatoclient.ipc/format-error [:=> [:cat string? :potatoclient.specs/exception] string?])
(m/=> potatoclient.ipc/handle-message [:=> [:cat :potatoclient.specs/stream-key string?] nil?])
(m/=> potatoclient.ipc/process-stream-output [:=> [:cat :potatoclient.specs/stream-key [:fn {:error/message "must be a core.async channel"}
                                                                                        #(instance? clojure.core.async.impl.channels.ManyToManyChannel %)]] nil?])
(m/=> potatoclient.ipc/start-output-processor [:=> [:cat :potatoclient.specs/stream-key :potatoclient.specs/stream-process-map] nil?])
(m/=> potatoclient.ipc/start-stream [:=> [:cat :potatoclient.specs/stream-key string?] nil?])
(m/=> potatoclient.ipc/stop-stream [:=> [:cat :potatoclient.specs/stream-key] nil?])
(m/=> potatoclient.ipc/restart-stream [:=> [:cat :potatoclient.specs/stream-key string?] nil?])
(m/=> potatoclient.ipc/send-command-to-stream [:=> [:cat :potatoclient.specs/stream-key :potatoclient.specs/process-command] boolean?])
(m/=> potatoclient.ipc/broadcast-command [:=> [:cat :potatoclient.specs/process-command] nil?])

;; -----------------------------------------------------------------------------
;; Process namespace schemas
;; -----------------------------------------------------------------------------

(m/=> potatoclient.process/start-stream-process [:=> [:cat string? string? :potatoclient.specs/domain] :potatoclient.specs/stream-process-map])
(m/=> potatoclient.process/send-command [:=> [:cat :potatoclient.specs/stream-process-map :potatoclient.specs/process-command] boolean?])
(m/=> potatoclient.process/stop-stream [:=> [:cat :potatoclient.specs/stream-process-map] nil?])
(m/=> potatoclient.process/cleanup-all-processes
      [:function
       [:=> [:cat] nil?]
       [:=> [:cat [:map-of :potatoclient.specs/stream-key [:maybe :potatoclient.specs/stream-process-map]]] nil?]])
(m/=> potatoclient.process/process-alive? [:=> [:cat :potatoclient.specs/stream-process-map] boolean?])

;; -----------------------------------------------------------------------------
;; UI.control-panel namespace schemas
;; -----------------------------------------------------------------------------

;; New control panel functions after refactoring
(m/=> potatoclient.ui.control-panel/create-stream-status-panel [:=> [:cat :potatoclient.specs/stream-key] :potatoclient.specs/jpanel])
(m/=> potatoclient.ui.control-panel/create-connection-info-panel [:=> [:cat] :potatoclient.specs/jpanel])
(m/=> potatoclient.ui.control-panel/create-stream-controls-panel [:=> [:cat] :potatoclient.specs/jpanel])
(m/=> potatoclient.ui.control-panel/create-statistics-panel [:=> [:cat] :potatoclient.specs/jpanel])
(m/=> potatoclient.ui.control-panel/create [:=> [:cat] :potatoclient.specs/jpanel])

;; -----------------------------------------------------------------------------
;; UI.main-frame namespace schemas
;; -----------------------------------------------------------------------------

(m/=> potatoclient.ui.main-frame/preserve-window-state [:=> [:cat :potatoclient.specs/jframe] :potatoclient.specs/window-state])
(m/=> potatoclient.ui.main-frame/restore-window-state! [:=> [:cat :potatoclient.specs/jframe :potatoclient.specs/window-state] :potatoclient.specs/jframe])
(m/=> potatoclient.ui.main-frame/reload-frame! [:=> [:cat :potatoclient.specs/jframe fn?] nil?])
(m/=> potatoclient.ui.main-frame/create-language-action [:=> [:cat :potatoclient.specs/locale string? fn?] :potatoclient.specs/action])
(m/=> potatoclient.ui.main-frame/create-theme-action [:=> [:cat :potatoclient.specs/theme-key fn?] :potatoclient.specs/action])
(m/=> potatoclient.ui.main-frame/create-theme-menu [:=> [:cat fn?] :potatoclient.specs/jmenu])
(m/=> potatoclient.ui.main-frame/create-language-menu [:=> [:cat fn?] :potatoclient.specs/jmenu])
(m/=> potatoclient.ui.main-frame/show-about-dialog [:=> [:cat [:or :potatoclient.specs/jframe nil?]] nil?])
(m/=> potatoclient.ui.main-frame/open-logs-viewer [:=> [:cat] nil?])
(m/=> potatoclient.ui.main-frame/create-help-menu [:=> [:cat [:or :potatoclient.specs/jframe nil?]] :potatoclient.specs/jmenu])
(m/=> potatoclient.ui.main-frame/create-stream-toggle-button [:=> [:cat :potatoclient.specs/stream-key] :potatoclient.specs/jtoggle-button])
(m/=> potatoclient.ui.main-frame/create-menu-bar [:=> [:cat fn? [:or :potatoclient.specs/jframe nil?]] :potatoclient.specs/jmenu-bar])
(m/=> potatoclient.ui.main-frame/create-main-content [:=> [:cat] :potatoclient.specs/jpanel])
(m/=> potatoclient.ui.main-frame/add-window-close-handler! [:=> [:cat :potatoclient.specs/jframe] nil?])
(m/=> potatoclient.ui.main-frame/ensure-on-edt [:=> [:cat fn?] fn?])
(m/=> potatoclient.ui.main-frame/ensure-on-edt-later [:=> [:cat fn?] fn?])
(m/=> potatoclient.ui.main-frame/create-main-frame [:=> [:cat :potatoclient.specs/frame-params] :potatoclient.specs/jframe])

;; -----------------------------------------------------------------------------
;; Log Viewer namespace schemas
;; -----------------------------------------------------------------------------

(m/=> potatoclient.ui.log-viewer/get-log-directory [:=> [:cat] :potatoclient.specs/file])
(m/=> potatoclient.ui.log-viewer/parse-log-filename [:=> [:cat string?] [:maybe [:map
                                                                                 [:version string?]
                                                                                 [:type string?]
                                                                                 [:timestamp string?]]]])
(m/=> potatoclient.ui.log-viewer/format-timestamp [:=> [:cat string?] string?])
(m/=> potatoclient.ui.log-viewer/get-log-files [:=> [:cat] [:sequential [:map
                                                                         [:file :potatoclient.specs/file]
                                                                         [:name string?]
                                                                         [:timestamp string?]
                                                                         [:version string?]
                                                                         [:size int?]]]])
(m/=> potatoclient.ui.log-viewer/format-file-size [:=> [:cat int?] string?])
(m/=> potatoclient.ui.log-viewer/copy-to-clipboard [:=> [:cat string?] nil?])
(m/=> potatoclient.ui.log-viewer/format-log-content [:=> [:cat string?] string?])
(m/=> potatoclient.ui.log-viewer/create-file-viewer [:=> [:cat :potatoclient.specs/file :potatoclient.specs/jframe] :potatoclient.specs/jframe])
(m/=> potatoclient.ui.log-viewer/pack-table-columns! [:=> [:cat [:fn #(instance? javax.swing.JTable %)]] nil?])
(m/=> potatoclient.ui.log-viewer/create-log-table [:=> [:cat] [:fn #(instance? javax.swing.JTable %)]])
(m/=> potatoclient.ui.log-viewer/update-log-list! [:=> [:cat [:fn #(instance? javax.swing.JTable %)]] nil?])
(m/=> potatoclient.ui.log-viewer/create-log-viewer-frame [:=> [:cat] :potatoclient.specs/jframe])
(m/=> potatoclient.ui.log-viewer/show-log-viewer [:=> [:cat] nil?])

;; -----------------------------------------------------------------------------
;; UI startup-dialog namespace schemas
;; -----------------------------------------------------------------------------

(m/=> potatoclient.ui.startup-dialog/reload-dialog! [:=> [:cat :potatoclient.specs/jframe fn?] nil?])
(m/=> potatoclient.ui.startup-dialog/create-language-action [:=> [:cat :potatoclient.specs/locale string? :potatoclient.specs/jframe fn?] :potatoclient.specs/action])
(m/=> potatoclient.ui.startup-dialog/create-theme-action [:=> [:cat :potatoclient.specs/theme-key :potatoclient.specs/jframe fn?] :potatoclient.specs/action])
(m/=> potatoclient.ui.startup-dialog/create-menu-bar [:=> [:cat :potatoclient.specs/jframe fn?] :potatoclient.specs/jmenu-bar])
(m/=> potatoclient.ui.startup-dialog/extract-domain [:=> [:cat string?] :potatoclient.specs/domain])
(m/=> potatoclient.ui.startup-dialog/validate-domain [:=> [:cat string?] boolean?])
(m/=> potatoclient.ui.startup-dialog/create-content-panel [:=> [:cat string?] :potatoclient.specs/jpanel])
(m/=> potatoclient.ui.startup-dialog/show-startup-dialog [:=> [:cat [:maybe :potatoclient.specs/jframe] fn?] [:maybe string?]])

;; -----------------------------------------------------------------------------
;; UI Utils namespace schemas
;; -----------------------------------------------------------------------------

(m/=> potatoclient.ui.utils/mk-debounced-transform [:=> [:cat fn?] fn?])
(m/=> potatoclient.ui.utils/debounce [:=> [:cat fn? pos-int?] fn?])
(m/=> potatoclient.ui.utils/throttle [:=> [:cat fn? pos-int?] fn?])
(m/=> potatoclient.ui.utils/batch-updates [:=> [:cat [:sequential fn?]] nil?])
;; These return whatever the passed function returns
(m/=> potatoclient.ui.utils/with-busy-cursor [:=> [:cat [:fn #(instance? java.awt.Component %)] fn?] any?])
(m/=> potatoclient.ui.utils/preserve-selection [:=> [:cat [:fn #(instance? javax.swing.text.JTextComponent %)] fn?] any?])

;; -----------------------------------------------------------------------------
;; Runtime namespace schemas
;; -----------------------------------------------------------------------------

(m/=> potatoclient.runtime/release-build? [:=> [:cat] boolean?])

;; -----------------------------------------------------------------------------
;; Core namespace schemas
;; -----------------------------------------------------------------------------

(m/=> potatoclient.core/-main [:=> [:cat [:* string?]] nil?])
(m/=> potatoclient.core/start-application [:=> [:cat] nil?])
(m/=> potatoclient.core/shutdown-application [:=> [:cat] nil?])

;; -----------------------------------------------------------------------------
;; Main namespace schemas
;; -----------------------------------------------------------------------------

(m/=> potatoclient.main/-main [:=> [:cat [:* string?]] nil?])

;; -----------------------------------------------------------------------------
;; Instrumentation control
;; -----------------------------------------------------------------------------

(defonce ^:private instrumentation-started? (atom false))

(declare report-unspecced-functions)

(defn- ensure-schemas-loaded!
  "Ensure all m/=> declarations in this namespace are evaluated.
   This is necessary because m/=> forms need to be evaluated after
   the target functions are defined."
  []
  ;; Re-evaluate all m/=> forms by loading this file
  ;; This ensures schemas are registered even if namespaces were loaded
  ;; in a different order
  (load-file "src/potatoclient/instrumentation.clj"))

;; -----------------------------------------------------------------------------
;; Instrumentation namespace schemas
;; -----------------------------------------------------------------------------

(m/=> find-unspecced-functions [:=> [:cat] [:map-of symbol? [:sequential symbol?]]])
(m/=> report-unspecced-functions [:=> [:cat] [:map
                                              [:status keyword?]
                                              [:message string?]
                                              [:data {:optional true} [:map-of symbol? [:sequential symbol?]]]
                                              [:total {:optional true} nat-int?]]])

(defn start!
  "Start function instrumentation.
   
   This should only be called in development environments after
   the application has been initialized."
  []
  (if @instrumentation-started?
    (println "Instrumentation already started")
    (do
      (println "Starting Malli function instrumentation...")
      ;; Set the global registry to include our custom schemas
      (mr/set-default-registry! specs/registry)
      ;; Ensure all schemas are loaded before starting
      (ensure-schemas-loaded!)
      (dev/start! {:report (pretty/reporter)})
      (reset! instrumentation-started? true)
      (println "Instrumentation started successfully")
      ;; Report unspecced functions after instrumentation starts
      (let [report (report-unspecced-functions)]
        (when (= :warning (:status report))
          (println (str "\n⚠ Found " (:total report) " functions without Malli specs"))
          ;; Automatically generate the report
          (try
            (require 'potatoclient.reports)
            (let [generate-report! (resolve 'potatoclient.reports/generate-unspecced-functions-report!)
                  report-path (generate-report! report)]
              (when report-path
                (println (str "📄 Report generated: " report-path))))
            (catch Exception e
              (println "Failed to generate report:" (.getMessage e)))))
        (when (= :success (:status report))
          (println "\n✅ All functions have Malli specs!"))))))

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

(defn refresh-schemas!
  "Manually refresh all function schemas.
   Useful when schemas might be out of sync."
  []
  (println "Refreshing function schemas...")
  (ensure-schemas-loaded!)
  (println "Schemas refreshed successfully"))

(defn- get-public-fns
  "Get all public function vars from a namespace."
  [ns-sym]
  (->> (ns-publics ns-sym)
       (filter (fn [[_ v]]
                 (and (var? v)
                      (fn? @v)
                      (not (:macro (meta v)))
                      ;; Check if it's a function defined with defn/defn-
                      ;; This excludes def'd values that happen to be functions
                      (or (contains? (meta v) :arglists)
                          ;; Protocol methods don't have arglists but have :method-builder
                          (contains? (meta v) :method-builder)))))
       (map first)))

(defn- has-malli-spec?
  "Check if a function has a Malli spec registered."
  [ns-sym fn-name]
  (let [all-schemas (m/function-schemas)
        ns-schemas (get all-schemas ns-sym)]
    (boolean (get ns-schemas fn-name))))

(defn find-unspecced-functions
  "Find all functions in potatoclient namespaces that don't have Malli specs.
   Returns a map of namespace to unspecced function names."
  []
  ;; Ensure we have the latest function schemas
  ;; This is important because schemas might be defined after namespace loading
  (let [current-schemas (m/function-schemas)
        potatoclient-nses (->> (all-ns)
                               (map ns-name)
                               (filter #(re-matches #"^potatoclient\..*" (str %)))
                               ;; Exclude instrumentation namespace itself
                               (remove #(= % 'potatoclient.instrumentation))
                               sort)]
    (into {}
          (for [ns-sym potatoclient-nses
                :let [;; Ensure namespace is loaded
                      _ (try (require ns-sym) (catch Exception _ nil))
                      public-fns (get-public-fns ns-sym)
                      unspecced (remove #(has-malli-spec? ns-sym %) public-fns)]
                :when (seq unspecced)]
            [ns-sym (vec unspecced)]))))

(defn report-unspecced-functions
  "Report functions without Malli specs in development mode.
   Returns a map with :status, :data, and :total keys."
  []
  (let [unspecced (find-unspecced-functions)
        total (reduce + (map count (vals unspecced)))]
    (if (empty? unspecced)
      {:status :success
       :message "All functions have Malli specs!"
       :data {}
       :total 0}
      {:status :warning
       :message (str total " functions without specs")
       :data unspecced
       :total total})))