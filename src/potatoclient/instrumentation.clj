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

(m/=> potatoclient.theme/get-current-theme [:=> [:cat] ::specs/theme-key])
(m/=> potatoclient.theme/get-theme [:=> [:cat ::specs/theme-key] [:map-of keyword? any?]])
(m/=> potatoclient.theme/get-theme-color [:=> [:cat ::specs/theme-key keyword?] ::specs/color])
(m/=> potatoclient.theme/apply-theme! [:=> [:cat ::specs/jframe ::specs/theme-key] any?])
(m/=> potatoclient.theme/get-available-themes [:=> [:cat] [:sequential ::specs/theme-key]])
(m/=> potatoclient.theme/get-theme-name [:=> [:cat ::specs/theme-key] string?])
(m/=> potatoclient.theme/apply-nimbus-theme! [:=> [:cat ::specs/theme-key] any?])
(m/=> potatoclient.theme/apply-darklaf-theme! [:=> [:cat ::specs/theme-key] any?])

;; -----------------------------------------------------------------------------
;; I18n namespace schemas
;; -----------------------------------------------------------------------------

(m/=> potatoclient.i18n/get-current-locale [:=> [:cat] ::specs/locale])
(m/=> potatoclient.i18n/tr [:=> [:cat ::specs/translation-key [:* any?]] string?])
(m/=> potatoclient.i18n/get-available-locales [:=> [:cat] [:sequential ::specs/locale]])
(m/=> potatoclient.i18n/set-locale! [:=> [:cat ::specs/locale] any?])
(m/=> potatoclient.i18n/reload-translations! [:=> [:cat] any?])

;; -----------------------------------------------------------------------------
;; Config namespace schemas
;; -----------------------------------------------------------------------------

(m/=> potatoclient.config/get-config-dir [:=> [:cat] ::specs/file])
(m/=> potatoclient.config/get-config-file [:=> [:cat] ::specs/file])
(m/=> potatoclient.config/ensure-config-dir! [:=> [:cat] any?])
(m/=> potatoclient.config/default-config [:=> [:cat] ::specs/config])
(m/=> potatoclient.config/load-config [:=> [:cat] ::specs/config])
(m/=> potatoclient.config/save-config! [:=> [:cat ::specs/config] any?])
(m/=> potatoclient.config/get-config [:=> [:cat ::specs/config-key] any?])
(m/=> potatoclient.config/update-config! [:=> [:cat ::specs/config-key any?] any?])
(m/=> potatoclient.config/get-domain [:=> [:cat] ::specs/domain])
(m/=> potatoclient.config/get-theme [:=> [:cat] ::specs/theme-key])
(m/=> potatoclient.config/get-locale [:=> [:cat] ::specs/locale])

;; -----------------------------------------------------------------------------
;; State namespace schemas
;; -----------------------------------------------------------------------------

(m/=> potatoclient.state/get-stream [:=> [:cat ::specs/stream-key] [:maybe ::specs/stream-process-map]])
(m/=> potatoclient.state/all-streams [:=> [:cat] [:map-of ::specs/stream-key [:maybe ::specs/stream-process-map]]])
(m/=> potatoclient.state/set-stream! [:=> [:cat ::specs/stream-key ::specs/stream-process-map] any?])
(m/=> potatoclient.state/clear-stream! [:=> [:cat ::specs/stream-key] any?])
(m/=> potatoclient.state/get-ui-element [:=> [:cat keyword?] any?])
(m/=> potatoclient.state/register-ui-element! [:=> [:cat keyword? any?] any?])
(m/=> potatoclient.state/get-locale [:=> [:cat] ::specs/locale])
(m/=> potatoclient.state/set-locale! [:=> [:cat ::specs/locale] any?])
(m/=> potatoclient.state/get-domain [:=> [:cat] ::specs/domain])
(m/=> potatoclient.state/set-domain! [:=> [:cat string?] any?])
(m/=> potatoclient.state/current-state [:=> [:cat] map?])

;; -----------------------------------------------------------------------------
;; State.streams namespace schemas
;; -----------------------------------------------------------------------------

(m/=> potatoclient.state.streams/get-stream [:=> [:cat ::specs/stream-key] [:maybe ::specs/stream-process-map]])
(m/=> potatoclient.state.streams/set-stream! [:=> [:cat ::specs/stream-key ::specs/stream-process-map] any?])
(m/=> potatoclient.state.streams/clear-stream! [:=> [:cat ::specs/stream-key] any?])
(m/=> potatoclient.state.streams/all-streams [:=> [:cat] [:map-of ::specs/stream-key [:maybe ::specs/stream-process-map]]])

;; -----------------------------------------------------------------------------
;; State.config namespace schemas
;; -----------------------------------------------------------------------------

(m/=> potatoclient.state.config/get-locale [:=> [:cat] ::specs/locale])
(m/=> potatoclient.state.config/set-locale! [:=> [:cat ::specs/locale] any?])
(m/=> potatoclient.state.config/get-domain [:=> [:cat] ::specs/domain])
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

(m/=> potatoclient.events.stream/format-window-event [:=> [:cat ::specs/window-event] string?])
(m/=> potatoclient.events.stream/format-navigation-event [:=> [:cat ::specs/navigation-event] string?])
(m/=> potatoclient.events.stream/handle-response-event [:=> [:cat keyword? map?] nil?])
(m/=> potatoclient.events.stream/handle-navigation-event [:=> [:cat [:map [:event map?]]] nil?])
(m/=> potatoclient.events.stream/handle-window-event [:=> [:cat [:map [:event map?]]] nil?])
(m/=> potatoclient.events.stream/stream-connected? [:=> [:cat ::specs/stream-key] boolean?])
(m/=> potatoclient.events.stream/all-streams-connected? [:=> [:cat] boolean?])

;; -----------------------------------------------------------------------------
;; Proto namespace schemas
;; -----------------------------------------------------------------------------

(m/=> potatoclient.proto/create-builder [:=> [:cat keyword?] any?])
(m/=> potatoclient.proto/set-field! [:=> [:cat any? keyword? any?] any?])
(m/=> potatoclient.proto/build-message [:=> [:cat any?] any?])
(m/=> potatoclient.proto/create-message [:=> [:cat keyword? map?] any?])
(m/=> potatoclient.proto/message->bytes [:=> [:cat any?] bytes?])
(m/=> potatoclient.proto/write-delimited [:=> [:cat any? any?] any?])
(m/=> potatoclient.proto/parse-command [:=> [:cat bytes?] [:maybe ::specs/command]])
(m/=> potatoclient.proto/create-gimbal-angle-command [:=> [:cat number? number?] ::specs/command])
(m/=> potatoclient.proto/create-lrf-request-command [:=> [:cat] ::specs/command])
(m/=> potatoclient.proto/create-lrf-single-pulse-command [:=> [:cat] ::specs/command])
(m/=> potatoclient.proto/create-window-event [:=> [:cat map?] bytes?])
(m/=> potatoclient.proto/create-navigation-event [:=> [:cat map?] bytes?])
(m/=> potatoclient.proto/handle-window-event [:=> [:cat ::specs/stream-process-map map?] any?])
(m/=> potatoclient.proto/handle-navigation-event [:=> [:cat ::specs/stream-process-map map?] any?])
(m/=> potatoclient.proto/handle-gimbal-command [:=> [:cat number? number?] any?])
(m/=> potatoclient.proto/handle-lrf-command [:=> [:cat keyword?] any?])

;; -----------------------------------------------------------------------------
;; Logging namespace schemas
;; -----------------------------------------------------------------------------

(m/=> potatoclient.logging/init! [:=> [:cat] any?])
(m/=> potatoclient.logging/shutdown! [:=> [:cat] any?])

;; -----------------------------------------------------------------------------
;; IPC namespace schemas
;; -----------------------------------------------------------------------------

(m/=> potatoclient.ipc/parse-message [:=> [:cat string?] [:maybe ::specs/message]])
(m/=> potatoclient.ipc/format-error [:=> [:cat string? ::specs/exception] string?])
(m/=> potatoclient.ipc/handle-message [:=> [:cat ::specs/stream-key string?] any?])
(m/=> potatoclient.ipc/process-stream-output [:=> [:cat ::specs/stream-key any?] any?])
(m/=> potatoclient.ipc/start-output-processor [:=> [:cat ::specs/stream-key ::specs/stream-process-map] any?])

;; -----------------------------------------------------------------------------
;; Process namespace schemas
;; -----------------------------------------------------------------------------

(m/=> potatoclient.process/create-process-builder [:=> [:cat string? [:sequential string?]] any?])
(m/=> potatoclient.process/start-stream-process [:=> [:cat ::specs/stream-key] [:maybe ::specs/stream-process-map]])
(m/=> potatoclient.process/stop-stream [:=> [:cat ::specs/stream-process-map] any?])
(m/=> potatoclient.process/send-command [:=> [:cat ::specs/stream-process-map ::specs/process-command] any?])
(m/=> potatoclient.process/get-stream-state [:=> [:cat ::specs/stream-process-map] ::specs/process-state])

;; -----------------------------------------------------------------------------
;; UI.control-panel namespace schemas
;; -----------------------------------------------------------------------------

(m/=> potatoclient.ui.control-panel/create-stream-button [:=> [:cat ::specs/stream-key string?] ::specs/jtoggle-button])
(m/=> potatoclient.ui.control-panel/toggle-stream [:=> [:cat ::specs/stream-key ::specs/jtoggle-button] any?])
(m/=> potatoclient.ui.control-panel/create-control-button [:=> [:cat keyword? string? any?] ::specs/jbutton])
(m/=> potatoclient.ui.control-panel/create-gimbal-controls [:=> [:cat] ::specs/jpanel])
(m/=> potatoclient.ui.control-panel/create-control-panel [:=> [:cat] ::specs/jpanel])

;; -----------------------------------------------------------------------------
;; UI.main-frame namespace schemas
;; -----------------------------------------------------------------------------

(m/=> potatoclient.ui.main-frame/save-window-state [:=> [:cat ::specs/jframe] any?])
(m/=> potatoclient.ui.main-frame/load-window-state [:=> [:cat] [:maybe ::specs/window-state]])
(m/=> potatoclient.ui.main-frame/restore-window-state [:=> [:cat ::specs/jframe] any?])
(m/=> potatoclient.ui.main-frame/make-centered [:=> [:cat ::specs/jframe] any?])
(m/=> potatoclient.ui.main-frame/setup-window-persistence [:=> [:cat ::specs/jframe] any?])
(m/=> potatoclient.ui.main-frame/create-language-menu [:=> [:cat] ::specs/jmenu])
(m/=> potatoclient.ui.main-frame/create-theme-menu [:=> [:cat ::specs/jframe] ::specs/jmenu])
(m/=> potatoclient.ui.main-frame/create-view-menu [:=> [:cat ::specs/jframe] ::specs/jmenu])
(m/=> potatoclient.ui.main-frame/create-help-menu [:=> [:cat] ::specs/jmenu])
(m/=> potatoclient.ui.main-frame/create-menu-bar [:=> [:cat ::specs/jframe] ::specs/jmenu-bar])
(m/=> potatoclient.ui.main-frame/create-main-frame [:=> [:cat map?] ::specs/jframe])

;; -----------------------------------------------------------------------------
;; Core namespace schemas
;; -----------------------------------------------------------------------------

(m/=> potatoclient.core/start-application [:=> [:cat] any?])
(m/=> potatoclient.core/shutdown-application [:=> [:cat] any?])

;; -----------------------------------------------------------------------------
;; Instrumentation control
;; -----------------------------------------------------------------------------

(defonce ^:private instrumentation-started? (atom false))

(declare report-unspecced-functions)

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
              (println "Failed to generate report:" (.getMessage e)))))))))

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

(defn- get-public-fns
  "Get all public function vars from a namespace."
  [ns-sym]
  (->> (ns-publics ns-sym)
       (filter (fn [[_ v]] 
                 (and (var? v)
                      (fn? @v)
                      (not (:macro (meta v))))))
       (map first)))

(defn- has-malli-spec?
  "Check if a function has a Malli spec registered."
  [ns-sym fn-name]
  (let [fn-sym (symbol (str ns-sym) (str fn-name))]
    (try
      (boolean (m/function-schema fn-sym))
      (catch Exception _
        ;; If schema lookup fails, the function doesn't have a spec
        false))))

(defn find-unspecced-functions
  "Find all functions in potatoclient namespaces that don't have Malli specs.
   Returns a map of namespace to unspecced function names."
  []
  (let [potatoclient-nses (->> (all-ns)
                               (map ns-name)
                               (filter #(re-matches #"^potatoclient\..*" (str %)))
                               ;; Exclude instrumentation namespace itself
                               (remove #(= % 'potatoclient.instrumentation))
                               sort)]
    (into {}
          (for [ns-sym potatoclient-nses
                :let [public-fns (get-public-fns ns-sym)
                      unspecced (remove #(has-malli-spec? ns-sym %) public-fns)]
                :when (seq unspecced)]
            [ns-sym (vec unspecced)]))))

(defn report-unspecced-functions
  "Report functions without Malli specs in development mode.
   Should be called after start! to ensure specs are registered.
   Returns a map with :status, :data, and :total keys."
  []
  (if-not @instrumentation-started?
    {:status :error
     :message "Instrumentation not started. Call (start!) first."}
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
         :total total}))))