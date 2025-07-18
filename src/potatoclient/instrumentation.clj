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
            ;; Main namespaces
            [potatoclient.core :as core]
            [potatoclient.main :as main]
            ;; Specs
            [potatoclient.specs :as specs]))

;; -----------------------------------------------------------------------------
;; Theme namespace schemas
;; -----------------------------------------------------------------------------

(m/=> potatoclient.theme/get-current-theme [:=> [:cat] :potatoclient.specs/theme-key])
(m/=> potatoclient.theme/get-theme [:=> [:cat :potatoclient.specs/theme-key] [:map-of keyword? any?]])
(m/=> potatoclient.theme/get-theme-color [:=> [:cat :potatoclient.specs/theme-key keyword?] :potatoclient.specs/color])
(m/=> potatoclient.theme/apply-theme! [:=> [:cat :potatoclient.specs/jframe :potatoclient.specs/theme-key] any?])
(m/=> potatoclient.theme/get-available-themes [:=> [:cat] [:sequential :potatoclient.specs/theme-key]])
(m/=> potatoclient.theme/get-theme-name [:=> [:cat :potatoclient.specs/theme-key] string?])
(m/=> potatoclient.theme/apply-nimbus-theme! [:=> [:cat :potatoclient.specs/theme-key] any?])
(m/=> potatoclient.theme/apply-darklaf-theme! [:=> [:cat :potatoclient.specs/theme-key] any?])
(m/=> potatoclient.theme/initialize-theme! [:=> [:cat] any?])
(m/=> potatoclient.theme/set-theme! [:=> [:cat :potatoclient.specs/theme-key] any?])
(m/=> potatoclient.theme/get-theme-i18n-key [:=> [:cat :potatoclient.specs/theme-key] keyword?])
(m/=> potatoclient.theme/preload-theme-icons! [:=> [:cat] any?])
(m/=> potatoclient.theme/key->icon [:=> [:cat keyword? :potatoclient.specs/theme-key] :potatoclient.specs/icon])

;; -----------------------------------------------------------------------------
;; I18n namespace schemas
;; -----------------------------------------------------------------------------

(m/=> potatoclient.i18n/init! [:=> [:cat] any?])
(m/=> potatoclient.i18n/load-translation-file [:=> [:cat :potatoclient.specs/locale] [:maybe map?]])
(m/=> potatoclient.i18n/load-translations! [:=> [:cat] map?])
(m/=> potatoclient.i18n/get-current-locale [:=> [:cat] :potatoclient.specs/locale])
(m/=> potatoclient.i18n/tr [:=> [:cat :potatoclient.specs/translation-key [:* any?]] string?])
(m/=> potatoclient.i18n/get-available-locales [:=> [:cat] [:sequential :potatoclient.specs/locale]])
(m/=> potatoclient.i18n/set-locale! [:=> [:cat :potatoclient.specs/locale] any?])
(m/=> potatoclient.i18n/reload-translations! [:=> [:cat] any?])

;; -----------------------------------------------------------------------------
;; Config namespace schemas
;; -----------------------------------------------------------------------------

(m/=> potatoclient.config/get-config-dir [:=> [:cat] :potatoclient.specs/file])
(m/=> potatoclient.config/get-config-file [:=> [:cat] :potatoclient.specs/file])
(m/=> potatoclient.config/ensure-config-dir! [:=> [:cat] any?])
(m/=> potatoclient.config/load-config [:=> [:cat] :potatoclient.specs/config])
(m/=> potatoclient.config/save-config! [:=> [:cat :potatoclient.specs/config] boolean?])
(m/=> potatoclient.config/get-theme [:=> [:cat] :potatoclient.specs/theme-key])
(m/=> potatoclient.config/save-theme! [:=> [:cat :potatoclient.specs/theme-key] boolean?])
(m/=> potatoclient.config/get-domain [:=> [:cat] :potatoclient.specs/domain])
(m/=> potatoclient.config/save-domain! [:=> [:cat string?] boolean?])
(m/=> potatoclient.config/get-locale [:=> [:cat] :potatoclient.specs/locale])
(m/=> potatoclient.config/save-locale! [:=> [:cat :potatoclient.specs/locale] boolean?])
(m/=> potatoclient.config/update-config! [:=> [:cat keyword? any?] boolean?])
(m/=> potatoclient.config/get-config-location [:=> [:cat] string?])
(m/=> potatoclient.config/initialize! [:=> [:cat] :potatoclient.specs/config])

;; -----------------------------------------------------------------------------
;; State namespace schemas
;; -----------------------------------------------------------------------------

(m/=> potatoclient.state/get-stream [:=> [:cat :potatoclient.specs/stream-key] [:maybe :potatoclient.specs/stream-process-map]])
(m/=> potatoclient.state/all-streams [:=> [:cat] [:map-of :potatoclient.specs/stream-key [:maybe :potatoclient.specs/stream-process-map]]])
(m/=> potatoclient.state/set-stream! [:=> [:cat :potatoclient.specs/stream-key :potatoclient.specs/stream-process-map] any?])
(m/=> potatoclient.state/clear-stream! [:=> [:cat :potatoclient.specs/stream-key] any?])
(m/=> potatoclient.state/get-ui-element [:=> [:cat keyword?] any?])
(m/=> potatoclient.state/register-ui-element! [:=> [:cat keyword? any?] any?])
(m/=> potatoclient.state/get-locale [:=> [:cat] :potatoclient.specs/locale])
(m/=> potatoclient.state/set-locale! [:=> [:cat :potatoclient.specs/locale] any?])
(m/=> potatoclient.state/get-domain [:=> [:cat] :potatoclient.specs/domain])
(m/=> potatoclient.state/set-domain! [:=> [:cat string?] any?])
(m/=> potatoclient.state/current-state [:=> [:cat] map?])

;; -----------------------------------------------------------------------------
;; State.streams namespace schemas
;; -----------------------------------------------------------------------------

(m/=> potatoclient.state.streams/get-stream [:=> [:cat :potatoclient.specs/stream-key] [:maybe :potatoclient.specs/stream-process-map]])
(m/=> potatoclient.state.streams/set-stream! [:=> [:cat :potatoclient.specs/stream-key :potatoclient.specs/stream-process-map] any?])
(m/=> potatoclient.state.streams/clear-stream! [:=> [:cat :potatoclient.specs/stream-key] any?])
(m/=> potatoclient.state.streams/all-streams [:=> [:cat] [:map-of :potatoclient.specs/stream-key [:maybe :potatoclient.specs/stream-process-map]]])

;; -----------------------------------------------------------------------------
;; State.config namespace schemas
;; -----------------------------------------------------------------------------

(m/=> potatoclient.state.config/get-locale [:=> [:cat] :potatoclient.specs/locale])
(m/=> potatoclient.state.config/set-locale! [:=> [:cat :potatoclient.specs/locale] any?])
(m/=> potatoclient.state.config/get-domain [:=> [:cat] :potatoclient.specs/domain])
(m/=> potatoclient.state.config/set-domain! [:=> [:cat string?] any?])
(m/=> potatoclient.state.config/get-config [:=> [:cat] map?])

;; -----------------------------------------------------------------------------
;; State.ui namespace schemas
;; -----------------------------------------------------------------------------

(m/=> potatoclient.state.ui/register-ui-element! [:=> [:cat keyword? any?] any?])
(m/=> potatoclient.state.ui/get-ui-element [:=> [:cat keyword?] any?])
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
(m/=> potatoclient.proto/->ProtoMapper_potatoclient_proto_proto-mapper [:=> [:cat any? any?] any?])
(m/=> potatoclient.proto/map->ProtoMapper_potatoclient_proto_proto-mapper [:=> [:cat map?] any?])

;; Command serialization/deserialization
(m/=> potatoclient.proto/serialize-cmd [:=> [:cat map?] bytes?])
(m/=> potatoclient.proto/deserialize-state [:=> [:cat bytes?] map?])

;; Command factory functions
(m/=> potatoclient.proto/cmd-ping [:=> [:cat pos-int? keyword?] map?])
(m/=> potatoclient.proto/cmd-noop [:=> [:cat pos-int? keyword?] map?])
(m/=> potatoclient.proto/cmd-frozen [:=> [:cat pos-int? keyword?] map?])

;; State accessor functions
(m/=> potatoclient.proto/get-system-info [:=> [:cat map?] any?])
(m/=> potatoclient.proto/get-camera-day [:=> [:cat map?] any?])
(m/=> potatoclient.proto/get-camera-heat [:=> [:cat map?] any?])
(m/=> potatoclient.proto/get-gps-info [:=> [:cat map?] any?])
(m/=> potatoclient.proto/get-compass-info [:=> [:cat map?] any?])
(m/=> potatoclient.proto/get-lrf-info [:=> [:cat map?] any?])
(m/=> potatoclient.proto/get-time-info [:=> [:cat map?] any?])
(m/=> potatoclient.proto/get-location [:=> [:cat map?] [:maybe map?]])

;; State query functions
(m/=> potatoclient.proto/cameras-available? [:=> [:cat map?] boolean?])

;; Validation functions
(m/=> potatoclient.proto/valid-command? [:=> [:cat map?] boolean?])
(m/=> potatoclient.proto/explain-invalid-command [:=> [:cat map?] string?])
(m/=> potatoclient.proto/explain-invalid-command? [:=> [:cat map?] string?])

;; Legacy proto functions (if they still exist)
(m/=> potatoclient.proto/create-builder [:=> [:cat keyword?] any?])
(m/=> potatoclient.proto/set-field! [:=> [:cat any? keyword? any?] any?])
(m/=> potatoclient.proto/build-message [:=> [:cat any?] any?])
(m/=> potatoclient.proto/create-message [:=> [:cat keyword? map?] any?])
(m/=> potatoclient.proto/message->bytes [:=> [:cat any?] bytes?])
(m/=> potatoclient.proto/write-delimited [:=> [:cat any? any?] any?])
(m/=> potatoclient.proto/parse-command [:=> [:cat bytes?] [:maybe :potatoclient.specs/command]])
(m/=> potatoclient.proto/create-gimbal-angle-command [:=> [:cat number? number?] :potatoclient.specs/command])
(m/=> potatoclient.proto/create-lrf-request-command [:=> [:cat] :potatoclient.specs/command])
(m/=> potatoclient.proto/create-lrf-single-pulse-command [:=> [:cat] :potatoclient.specs/command])
(m/=> potatoclient.proto/create-window-event [:=> [:cat map?] bytes?])
(m/=> potatoclient.proto/create-navigation-event [:=> [:cat map?] bytes?])
(m/=> potatoclient.proto/handle-window-event [:=> [:cat :potatoclient.specs/stream-process-map map?] any?])
(m/=> potatoclient.proto/handle-navigation-event [:=> [:cat :potatoclient.specs/stream-process-map map?] any?])
(m/=> potatoclient.proto/handle-gimbal-command [:=> [:cat number? number?] any?])
(m/=> potatoclient.proto/handle-lrf-command [:=> [:cat keyword?] any?])

;; -----------------------------------------------------------------------------
;; Logging namespace schemas
;; -----------------------------------------------------------------------------

(m/=> potatoclient.logging/init! [:=> [:cat] any?])
(m/=> potatoclient.logging/shutdown! [:=> [:cat] any?])
(m/=> potatoclient.logging/get-logs-directory [:=> [:cat] any?])

;; -----------------------------------------------------------------------------
;; IPC namespace schemas
;; -----------------------------------------------------------------------------

(m/=> potatoclient.ipc/parse-message [:=> [:cat string?] [:maybe :potatoclient.specs/message]])
(m/=> potatoclient.ipc/format-error [:=> [:cat string? :potatoclient.specs/exception] string?])
(m/=> potatoclient.ipc/handle-message [:=> [:cat :potatoclient.specs/stream-key string?] any?])
(m/=> potatoclient.ipc/process-stream-output [:=> [:cat :potatoclient.specs/stream-key any?] any?])
(m/=> potatoclient.ipc/start-output-processor [:=> [:cat :potatoclient.specs/stream-key :potatoclient.specs/stream-process-map] any?])
(m/=> potatoclient.ipc/start-stream [:=> [:cat :potatoclient.specs/stream-key string?] any?])
(m/=> potatoclient.ipc/stop-stream [:=> [:cat :potatoclient.specs/stream-key] any?])
(m/=> potatoclient.ipc/restart-stream [:=> [:cat :potatoclient.specs/stream-key string?] any?])
(m/=> potatoclient.ipc/send-command-to-stream [:=> [:cat :potatoclient.specs/stream-key map?] boolean?])
(m/=> potatoclient.ipc/broadcast-command [:=> [:cat map?] any?])

;; -----------------------------------------------------------------------------
;; Process namespace schemas
;; -----------------------------------------------------------------------------

(m/=> potatoclient.process/start-stream-process [:=> [:cat string? string?] :potatoclient.specs/stream-process-map])
(m/=> potatoclient.process/send-command [:=> [:cat :potatoclient.specs/stream-process-map map?] boolean?])
(m/=> potatoclient.process/stop-stream [:=> [:cat :potatoclient.specs/stream-process-map] any?])
(m/=> potatoclient.process/cleanup-all-processes
      [:function
       [:=> [:cat] any?]
       [:=> [:cat [:map-of keyword? any?]] any?]])
(m/=> potatoclient.process/process-alive? [:=> [:cat :potatoclient.specs/stream-process-map] boolean?])

;; -----------------------------------------------------------------------------
;; UI.control-panel namespace schemas
;; -----------------------------------------------------------------------------

(m/=> potatoclient.ui.control-panel/create-stream-button [:=> [:cat :potatoclient.specs/stream-key string?] :potatoclient.specs/jtoggle-button])
(m/=> potatoclient.ui.control-panel/toggle-stream [:=> [:cat :potatoclient.specs/stream-key :potatoclient.specs/jtoggle-button] any?])
(m/=> potatoclient.ui.control-panel/create-control-button [:=> [:cat keyword? string? any?] :potatoclient.specs/jbutton])
(m/=> potatoclient.ui.control-panel/create-gimbal-controls [:=> [:cat] :potatoclient.specs/jpanel])
(m/=> potatoclient.ui.control-panel/create-control-panel [:=> [:cat] :potatoclient.specs/jpanel])
(m/=> potatoclient.ui.control-panel/create [:=> [:cat] :potatoclient.specs/jpanel])

;; -----------------------------------------------------------------------------
;; UI.main-frame namespace schemas
;; -----------------------------------------------------------------------------

(m/=> potatoclient.ui.main-frame/preserve-window-state [:=> [:cat :potatoclient.specs/jframe] map?])
(m/=> potatoclient.ui.main-frame/restore-window-state! [:=> [:cat :potatoclient.specs/jframe map?] any?])
(m/=> potatoclient.ui.main-frame/reload-frame! [:=> [:cat :potatoclient.specs/jframe fn?] any?])
(m/=> potatoclient.ui.main-frame/create-language-action [:=> [:cat keyword? string? fn?] any?])
(m/=> potatoclient.ui.main-frame/create-theme-action [:=> [:cat keyword? fn?] any?])
(m/=> potatoclient.ui.main-frame/create-theme-menu [:=> [:cat fn?] :potatoclient.specs/jmenu])
(m/=> potatoclient.ui.main-frame/create-language-menu [:=> [:cat fn?] :potatoclient.specs/jmenu])
(m/=> potatoclient.ui.main-frame/show-about-dialog [:=> [:cat any?] any?])
(m/=> potatoclient.ui.main-frame/open-logs-viewer [:=> [:cat] any?])
(m/=> potatoclient.ui.main-frame/create-help-menu [:=> [:cat any?] :potatoclient.specs/jmenu])
(m/=> potatoclient.ui.main-frame/create-stream-toggle-button [:=> [:cat keyword?] any?])
(m/=> potatoclient.ui.main-frame/create-menu-bar [:=> [:cat fn? any?] :potatoclient.specs/jmenu-bar])
(m/=> potatoclient.ui.main-frame/create-main-content [:=> [:cat] :potatoclient.specs/jpanel])
(m/=> potatoclient.ui.main-frame/add-window-close-handler! [:=> [:cat :potatoclient.specs/jframe] any?])
(m/=> potatoclient.ui.main-frame/ensure-on-edt [:=> [:cat fn?] fn?])
(m/=> potatoclient.ui.main-frame/ensure-on-edt-later [:=> [:cat fn?] fn?])
(m/=> potatoclient.ui.main-frame/create-main-frame [:=> [:cat map?] :potatoclient.specs/jframe])

;; -----------------------------------------------------------------------------
;; Log Viewer namespace schemas
;; -----------------------------------------------------------------------------

(m/=> potatoclient.ui.log-viewer/get-log-directory [:=> [:cat] :potatoclient.specs/file])
(m/=> potatoclient.ui.log-viewer/parse-log-filename [:=> [:cat string?] [:maybe map?]])
(m/=> potatoclient.ui.log-viewer/format-timestamp [:=> [:cat string?] string?])
(m/=> potatoclient.ui.log-viewer/get-log-files [:=> [:cat] [:sequential map?]])
(m/=> potatoclient.ui.log-viewer/format-file-size [:=> [:cat int?] string?])
(m/=> potatoclient.ui.log-viewer/copy-to-clipboard [:=> [:cat string?] any?])
(m/=> potatoclient.ui.log-viewer/format-log-content [:=> [:cat string?] string?])
(m/=> potatoclient.ui.log-viewer/create-file-viewer [:=> [:cat :potatoclient.specs/file :potatoclient.specs/jframe] :potatoclient.specs/jframe])
(m/=> potatoclient.ui.log-viewer/create-log-table [:=> [:cat] any?])
(m/=> potatoclient.ui.log-viewer/update-log-list! [:=> [:cat any?] any?])
(m/=> potatoclient.ui.log-viewer/create-log-viewer-frame [:=> [:cat] :potatoclient.specs/jframe])
(m/=> potatoclient.ui.log-viewer/show-log-viewer [:=> [:cat] any?])

;; -----------------------------------------------------------------------------
;; Runtime namespace schemas
;; -----------------------------------------------------------------------------

(m/=> potatoclient.runtime/release-build? [:=> [:cat] boolean?])

;; -----------------------------------------------------------------------------
;; Core namespace schemas
;; -----------------------------------------------------------------------------

(m/=> potatoclient.core/-main [:=> [:cat [:* any?]] any?])
(m/=> potatoclient.core/start-application [:=> [:cat] any?])
(m/=> potatoclient.core/shutdown-application [:=> [:cat] any?])

;; -----------------------------------------------------------------------------
;; Main namespace schemas
;; -----------------------------------------------------------------------------

(m/=> potatoclient.main/-main [:=> [:cat [:* any?]] any?])

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