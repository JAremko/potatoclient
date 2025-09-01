(ns potatoclient.state
  "Application state management for PotatoClient UI.

  This namespace provides the core UI state atom and basic accessors
  for managing application UI state."
  (:require [clojure.string :as str]
            [malli.core :as m]
            [potatoclient.malli.registry :as registry]
            [potatoclient.runtime :as runtime]
            [potatoclient.ui-specs :as ui-specs])
  (:import (java.net URI)
           (java.util Locale)))

;; ============================================================================
;; State Specifications
;; ============================================================================

(def process-status
  "Process status"
  [:enum :running :stopped :error])

(def process-info
  "Process information"
  [:map {:closed true}
   [:pid [:maybe pos-int?]]
   [:status process-status]])

(def connection-state
  "Connection state specification"
  [:map {:closed true}
   [:url :string]
   [:connected? :boolean]
   [:latency-ms [:maybe pos-int?]]
   [:reconnect-count nat-int?]])

(def ui-state
  "UI state specification"
  [:map {:closed true}
   [:theme :potatoclient.ui-specs/theme-key]
   [:locale :potatoclient.ui-specs/locale]
   [:fullscreen? :boolean]
   [:read-only-mode? :boolean]
   [:show-overlay? :boolean]
   [:status :potatoclient.ui-specs/status-message]])

(def processes-state
  "Processes state specification"
  [:map {:closed true}
   [:state-proc process-info]
   [:cmd-proc process-info]
   [:heat-video process-info]
   [:day-video process-info]])

(def stream-process-info
  "Stream process information"
  [:map {:closed true}
   [:pid [:maybe pos-int?]]
   [:port [:maybe pos-int?]]
   [:status process-status]
   [:type :potatoclient.ui-specs/stream-key]
   [:started-at [:maybe pos-int?]]])

(def stream-processes-state
  "Stream processes state specification"
  [:map-of :potatoclient.ui-specs/stream-key stream-process-info])

(def session-state
  "Session state specification"
  [:map {:closed true}
   [:user [:maybe :string]]
   [:started-at [:maybe pos-int?]]])

(def system-server-state
  "System server state"
  [:map {:closed true}
   [:battery-level [:and int? [:>= 0] [:<= 100]]]
   [:localization :string]
   [:recording :boolean]
   [:mode [:enum :day :night :heat]]
   [:temperature-c :double]])

(def lrf-server-state
  "LRF server state"
  [:map {:closed true}
   [:distance :double]
   [:scan-mode :string]
   [:target-locked :boolean]])

(def gps-server-state
  "GPS server state"
  [:map {:closed true}
   [:latitude :double]
   [:longitude :double]
   [:altitude :double]
   [:fix-type :string]
   [:satellites nat-int?]
   [:hdop :double]
   [:use-manual :boolean]])

(def compass-server-state
  "Compass server state"
  [:map {:closed true}
   [:heading :double]
   [:pitch :double]
   [:roll :double]
   [:unit :string]
   [:calibrated :boolean]])

(def rotary-server-state
  "Rotary server state"
  [:map {:closed true}
   [:azimuth :double]
   [:elevation :double]
   [:azimuth-velocity :double]
   [:elevation-velocity :double]
   [:moving :boolean]
   [:mode [:enum :manual :auto :tracking]]])

(def camera-day-server-state
  "Day camera server state"
  [:map {:closed true}
   [:zoom :double]
   [:focus-mode [:enum :auto :manual]]
   [:exposure-mode [:enum :auto :manual]]
   [:brightness [:and int? [:>= 0] [:<= 100]]]
   [:contrast [:and int? [:>= 0] [:<= 100]]]
   [:recording :boolean]])

(def camera-heat-server-state
  "Heat camera server state"
  [:map {:closed true}
   [:zoom :double]
   [:palette [:enum :white-hot :black-hot :rainbow :ironbow]]
   [:brightness [:and int? [:>= 0] [:<= 100]]]
   [:contrast [:and int? [:>= 0] [:<= 100]]]
   [:nuc-status [:enum :idle :in-progress :scheduled]]
   [:recording :boolean]])

(def glass-heater-server-state
  "Glass heater server state"
  [:map {:closed true}
   [:enabled :boolean]
   [:temperature-c :double]
   [:target-temp-c :double]
   [:power-percent [:and int? [:>= 0] [:<= 100]]]])

(def server-state
  "Complete server state specification"
  [:map {:closed true}
   [:system system-server-state]
   [:lrf lrf-server-state]
   [:gps gps-server-state]
   [:compass compass-server-state]
   [:rotary rotary-server-state]
   [:camera-day camera-day-server-state]
   [:camera-heat camera-heat-server-state]
   [:glass-heater glass-heater-server-state]])

(def app-state-spec
  "Complete application state specification"
  [:map {:closed true}
   [:connection connection-state]
   [:ui ui-state]
   [:processes processes-state]
   [:stream-processes stream-processes-state]
   [:session session-state]
   [:server-state server-state]])

;; ============================================================================
;; Registry Registration
;; ============================================================================

(defn register-state-specs!
  "Register all state specs to the global malli registry"
  []
  ;; Process and connection specs
  (registry/register-spec! ::process-status process-status)
  (registry/register-spec! ::process-info process-info)
  (registry/register-spec! ::connection-state connection-state)
  
  ;; UI and session specs
  (registry/register-spec! ::ui-state ui-state)
  (registry/register-spec! ::session-state session-state)
  
  ;; Processes specs
  (registry/register-spec! ::processes-state processes-state)
  (registry/register-spec! ::stream-process-info stream-process-info)
  (registry/register-spec! ::stream-processes-state stream-processes-state)
  
  ;; Server state specs
  (registry/register-spec! ::system-server-state system-server-state)
  (registry/register-spec! ::lrf-server-state lrf-server-state)
  (registry/register-spec! ::gps-server-state gps-server-state)
  (registry/register-spec! ::compass-server-state compass-server-state)
  (registry/register-spec! ::rotary-server-state rotary-server-state)
  (registry/register-spec! ::camera-day-server-state camera-day-server-state)
  (registry/register-spec! ::camera-heat-server-state camera-heat-server-state)
  (registry/register-spec! ::glass-heater-server-state glass-heater-server-state)
  (registry/register-spec! ::server-state server-state)
  
  ;; Complete app state
  (registry/register-spec! ::app-state app-state-spec))

;; Specs will be registered after ui-specs are loaded
;; This is handled by ensure-specs-registered! below

;; ============================================================================
;; Lazy Initialization
;; ============================================================================

(defonce ^:private specs-registered? (atom false))

(defn ensure-specs-registered!
  "Ensure state specs are registered. Called lazily on first use."
  []
  (when-not @specs-registered?
    (register-state-specs!)
    (reset! specs-registered? true)))

;; ============================================================================
;; State Atom
;; ============================================================================

(def initial-state
  "Initial application UI state"
  {:connection {:url ""
                :connected? false
                :latency-ms nil
                :reconnect-count 0}
   :ui {:theme :sol-dark
        :locale :english
        :fullscreen? false
        :read-only-mode? false
        :show-overlay? true
        :status {:message ""
                 :type :info}}
   :processes {:state-proc {:pid nil
                            :status :stopped}
               :cmd-proc {:pid nil
                          :status :stopped}
               :heat-video {:pid nil
                            :status :stopped}
               :day-video {:pid nil
                           :status :stopped}}
   :stream-processes {}
   :session {:user nil
             :started-at nil}
   :server-state {:system {:battery-level 0
                           :localization "en"
                           :recording false
                           :mode :day
                           :temperature-c 20.0}
                  :lrf {:distance 0.0
                        :scan-mode "single"
                        :target-locked false}
                  :gps {:latitude 0.0
                        :longitude 0.0
                        :altitude 0.0
                        :fix-type "none"
                        :satellites 0
                        :hdop 99.9
                        :use-manual false}
                  :compass {:heading 0.0
                            :pitch 0.0
                            :roll 0.0
                            :unit "degrees"
                            :calibrated false}
                  :rotary {:azimuth 0.0
                           :elevation 0.0
                           :azimuth-velocity 0.0
                           :elevation-velocity 0.0
                           :moving false
                           :mode :manual}
                  :camera-day {:zoom 1.0
                               :focus-mode :auto
                               :exposure-mode :auto
                               :brightness 50
                               :contrast 50
                               :recording false}
                  :camera-heat {:zoom 1.0
                                :palette :white-hot
                                :brightness 50
                                :contrast 50
                                :nuc-status :idle
                                :recording false}
                  :glass-heater {:enabled false
                                 :temperature-c 20.0
                                 :target-temp-c 25.0
                                 :power-percent 0}}})

(defonce ^{:doc "Main app state"} app-state
  (atom initial-state))

;; ============================================================================
;; Connection Management
;; ============================================================================

(defn get-connection-url
  "Get current connection URL."
  {:malli/schema [:=> [:cat] [:maybe :string]]}
  []
  (get-in @app-state [:connection :url]))

(defn set-connection-url!
  "Set connection URL."
  {:malli/schema [:=> [:cat :string] :map]}
  [url]
  (swap! app-state assoc-in [:connection :url] url))

(defn connected?
  "Check if connected to server."
  {:malli/schema [:=> [:cat] :boolean]}
  []
  (get-in @app-state [:connection :connected?] false))

(defn set-connected!
  "Set connection status."
  {:malli/schema [:=> [:cat :boolean] :map]}
  [connected?]
  (swap! app-state assoc-in [:connection :connected?] connected?))

(defn get-domain
  "Get current domain from connection URL."
  {:malli/schema [:=> [:cat] :string]}
  []
  (if-let [url (get-connection-url)]
    (try
      (let [uri (URI. url)
            host (.getHost uri)]
        (or host "localhost"))
      (catch Exception _
        "localhost"))
    "localhost"))

;; ============================================================================
;; UI Configuration
;; ============================================================================

(defn get-locale
  "Get current locale."
  {:malli/schema [:=> [:cat] [:enum :english :ukrainian]]}
  []
  (get-in @app-state [:ui :locale] :english))

(defn set-locale!
  "Set current locale and update Java default."
  {:malli/schema [:=> [:cat [:enum :english :ukrainian]] :map]}
  [locale]
  (let [locale-map {:english ["en" "US"]
                    :ukrainian ["uk" "UA"]}
        [lang country] (get locale-map locale ["en" "US"])]
    (Locale/setDefault
      (Locale/forLanguageTag (str lang "-" country))))
  (swap! app-state assoc-in [:ui :locale] locale))

(defn get-theme
  "Get current theme."
  {:malli/schema [:=> [:cat] :keyword]}
  []
  (get-in @app-state [:ui :theme] :sol-dark))

(defn set-theme!
  "Set UI theme."
  {:malli/schema [:=> [:cat :keyword] :map]}
  [theme]
  (swap! app-state assoc-in [:ui :theme] theme))

(defn fullscreen?
  "Check if in fullscreen mode."
  {:malli/schema [:=> [:cat] :boolean]}
  []
  (get-in @app-state [:ui :fullscreen?] false))

(defn set-fullscreen!
  "Set fullscreen mode."
  {:malli/schema [:=> [:cat :boolean] :map]}
  [fullscreen?]
  (swap! app-state assoc-in [:ui :fullscreen?] fullscreen?))

;; ============================================================================
;; Session Management
;; ============================================================================

(defn get-session
  "Get current session info."
  {:malli/schema [:=> [:cat] :map]}
  []
  (get @app-state :session {}))

(defn start-session!
  "Start a new session."
  {:malli/schema [:=> [:cat [:maybe :string]] :map]}
  [user]
  (swap! app-state assoc :session {:user user
                                   :started-at (System/currentTimeMillis)}))

(defn end-session!
  "End current session."
  {:malli/schema [:=> [:cat] :map]}
  []
  (swap! app-state assoc :session {:user nil
                                   :started-at nil}))

;; ============================================================================
;; Process Management
;; ============================================================================

(defn get-process-state
  "Get state of a specific process."
  {:malli/schema [:=> [:cat [:enum :state-proc :cmd-proc :heat-video :day-video]] [:maybe :map]]}
  [process-key]
  (get-in @app-state [:processes process-key]))

(defn process-running?
  "Check if a process is running."
  {:malli/schema [:=> [:cat [:enum :state-proc :cmd-proc :heat-video :day-video]] :boolean]}
  [process-key]
  (= :running (get-in @app-state [:processes process-key :status])))

(defn update-process-status!
  "Update process status."
  {:malli/schema [:=> [:cat [:enum :state-proc :cmd-proc :heat-video :day-video] [:maybe :pos-int] [:enum :running :stopped :error]] :map]}
  [process-key pid status]
  (swap! app-state assoc-in [:processes process-key]
         {:pid pid :status status}))

;; ============================================================================
;; Stream Process Management
;; ============================================================================

(defn add-stream-process!
  "Add stream process info."
  {:malli/schema [:=> [:cat [:enum :heat :day] :map] :map]}
  [stream-type process-map]
  (let [process-key (case stream-type
                      :heat :heat-video
                      :day :day-video)]
    (swap! app-state assoc-in [:stream-processes process-key] process-map)))

(defn get-stream-process
  "Get stream process info."
  {:malli/schema [:=> [:cat [:enum :heat :day]] [:maybe :map]]}
  [stream-type]
  (let [process-key (case stream-type
                      :heat :heat-video
                      :day :day-video)]
    (get-in @app-state [:stream-processes process-key])))

(defn remove-stream-process!
  "Remove stream process."
  {:malli/schema [:=> [:cat [:enum :heat :day]] :map]}
  [stream-type]
  (let [process-key (case stream-type
                      :heat :heat-video
                      :day :day-video)]
    (swap! app-state update :stream-processes dissoc process-key)))

(defn get-all-stream-processes
  "Get all stream processes."
  {:malli/schema [:=> [:cat] [:map-of :keyword :map]]}
  []
  (get @app-state :stream-processes {}))

;; ============================================================================
;; Server State Management
;; ============================================================================

(defn get-server-state
  "Get the current server state."
  {:malli/schema [:=> [:cat] :map]}
  []
  (:server-state @app-state))

(defn update-server-state!
  "Update server state."
  {:malli/schema [:=> [:cat :map] :map]}
  [updates]
  (swap! app-state update :server-state merge updates))

(defn get-subsystem-state
  "Get state for a specific subsystem."
  {:malli/schema [:=> [:cat :keyword] [:maybe :map]]}
  [subsystem]
  (get-in @app-state [:server-state subsystem]))

;; ============================================================================
;; Extended UI Configuration
;; ============================================================================

(defn read-only-mode?
  "Check if in read-only mode."
  {:malli/schema [:=> [:cat] :boolean]}
  []
  (get-in @app-state [:ui :read-only-mode?] false))

(defn set-read-only-mode!
  "Set read-only mode."
  {:malli/schema [:=> [:cat :boolean] :map]}
  [enabled?]
  (swap! app-state assoc-in [:ui :read-only-mode?] enabled?))

(defn show-overlay?
  "Check if overlay should be shown."
  {:malli/schema [:=> [:cat] :boolean]}
  []
  (get-in @app-state [:ui :show-overlay?] true))

(defn set-show-overlay!
  "Set overlay visibility."
  {:malli/schema [:=> [:cat :boolean] :map]}
  [show?]
  (swap! app-state assoc-in [:ui :show-overlay?] show?))

;; ============================================================================
;; Extended Connection Management
;; ============================================================================

(defn get-connection-latency
  "Get connection latency in milliseconds."
  {:malli/schema [:=> [:cat] [:maybe :pos-int]]}
  []
  (get-in @app-state [:connection :latency-ms]))

(defn set-connection-latency!
  "Set connection latency in milliseconds."
  {:malli/schema [:=> [:cat [:maybe :pos-int]] :map]}
  [latency-ms]
  (swap! app-state assoc-in [:connection :latency-ms] latency-ms))

(defn get-reconnect-count
  "Get reconnection attempt count."
  {:malli/schema [:=> [:cat] :int]}
  []
  (get-in @app-state [:connection :reconnect-count] 0))

(defn set-connection-state!
  "Set complete connection state."
  {:malli/schema [:=> [:cat :boolean [:maybe :string] [:maybe :pos-int]] :map]}
  [connected? url latency-ms]
  (swap! app-state update :connection
         (fn [conn]
           (cond-> (assoc conn :connected? connected?)
             url (assoc :url url)
             latency-ms (assoc :latency-ms latency-ms)))))

(defn increment-reconnect-count!
  "Increment reconnection attempt counter."
  {:malli/schema [:=> [:cat] :map]}
  []
  (swap! app-state update-in [:connection :reconnect-count] (fnil inc 0)))

(defn reset-reconnect-count!
  "Reset reconnection attempt counter."
  {:malli/schema [:=> [:cat] :map]}
  []
  (swap! app-state assoc-in [:connection :reconnect-count] 0))

;; ============================================================================
;; State Observation
;; ============================================================================

(defn add-watch-handler
  "Add a watch handler to app-state."
  {:malli/schema [:=> [:cat :keyword :fn] :nil]}
  [key handler-fn]
  (add-watch app-state key handler-fn)
  nil)

(defn remove-watch-handler
  "Remove a watch handler from app-state."
  {:malli/schema [:=> [:cat :keyword] :nil]}
  [key]
  (remove-watch app-state key)
  nil)

(defn cleanup-seesaw-bindings!
  "Remove all watchers added by seesaw.bind from app-state.
  Seesaw bind uses gensym keys like :bindable-atom-watcherXXXXX
  This scans and removes all such watchers."
  {:malli/schema [:=> [:cat] :nil]}
  []
  (let [watchers (keys (.getWatches app-state))
        ;; Seesaw bind creates keys like :bindable-atom-watcherXXXXX
        seesaw-watchers (filter #(and (keyword? %)
                                      (when-let [name-str (name %)]
                                        (or (str/starts-with? name-str "bindable-atom-watcher")
                                            (str/starts-with? name-str "bindable-agent-watcher")
                                            (str/starts-with? name-str "bindable-ref-watcher"))))
                                watchers)]
    (doseq [watcher-key seesaw-watchers]
      (remove-watch app-state watcher-key))
    nil))

(defn current-state
  "Get a snapshot of entire app state (for debugging)."
  {:malli/schema [:=> [:cat] :map]}
  []
  @app-state)

(defn reset-state!
  "Reset entire app state to initial values."
  {:malli/schema [:=> [:cat] :map]}
  []
  (reset! app-state initial-state))

;; ============================================================================
;; State Validation
;; ============================================================================

(defn validate-state
  "Validate the current app state against the schema.
   Returns nil if valid, or validation errors if invalid."
  {:malli/schema [:=> [:cat] [:maybe :map]]}
  []
  (ensure-specs-registered!)
  (when-not (m/validate app-state-spec @app-state)
    (m/explain app-state-spec @app-state)))

(defn validate-state!
  "Validate state and throw exception if invalid.
   Useful for development assertions."
  {:malli/schema [:=> [:cat] :nil]}
  []
  (when-let [errors (validate-state)]
    (throw (ex-info "Invalid app state" {:errors errors})))
  nil)

(defn valid-state?
  "Check if current state is valid."
  {:malli/schema [:=> [:cat] :boolean]}
  []
  (ensure-specs-registered!)
  (m/validate app-state-spec @app-state))

(defn validate-partial
  "Validate a partial state update against a specific schema."
  {:malli/schema [:=> [:cat :keyword :any] [:maybe :map]]}
  [schema-key value]
  (let [registry (registry/get-registry)
        schema (get registry schema-key)]
    (when (and schema (not (m/validate schema value)))
      (m/explain schema value))))

(defn safe-swap!
  "Swap app-state with validation in development mode.
   In production, behaves like normal swap!"
  {:malli/schema [:=> [:cat :fn [:* :any]] :map]}
  [f & args]
  (let [result (apply swap! app-state f args)]
    ;; Only validate in development
    (when (and (not (runtime/release-build?))
               (not (valid-state?)))
      (let [errors (validate-state)]
        (println "WARNING: State validation failed after swap!")
        (println "Errors:" errors)))
    result))

(defn safe-reset!
  "Reset app-state with validation in development mode."
  {:malli/schema [:=> [:cat :map] :map]}
  [new-state]
  (ensure-specs-registered!)
  (when (and (not (runtime/release-build?))
             (not (m/validate app-state-spec new-state)))
    (let [errors (m/explain app-state-spec new-state)]
      (throw (ex-info "Cannot reset to invalid state" {:errors errors}))))
  (reset! app-state new-state))
