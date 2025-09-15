(ns potatoclient.state
  "Application state management for PotatoClient UI.

  This namespace provides the core UI state atom and basic accessors
  for managing application UI state."
  (:require [clojure.string :as str]
            [malli.core :as m]
            [potatoclient.malli.registry :as registry]
            [potatoclient.runtime :as runtime]
            [potatoclient.ui-specs :as ui-specs]
            [potatoclient.specs.state.root])
  (:import (java.net URI)
           (java.util Locale)))

;; ============================================================================
;; State Specifications
;; ============================================================================

(def process-status
  "Process status"
  [:enum :starting :running :stopping :stopped :error])

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
   [:latency-ms [:maybe nat-int?]]
   [:reconnect-count nat-int?]])

(def ui-state
  "UI-related state spec.
  
  Includes:
  - theme: Current theme (:sol-light or :sol-dark)
  - locale: Current locale (:english or :ukrainian)
  - fullscreen: Whether the app is in fullscreen mode
  - show-overlay: Whether to show the HUD overlay
  - read-only-mode: Whether UI is in read-only mode
  - active-tab: Currently selected tab {:tag <tab-key>}
  - tab-properties: Map of tab properties {<tab-key> {:has-window boolean
                                                       :window-bounds {:x int :y int :width int :height int}}}"
  [:map {:closed true}
   [:theme :potatoclient.ui-specs/theme]
   [:locale [:enum :english :ukrainian]]
   [:fullscreen :boolean]
   [:show-overlay :boolean]
   [:read-only-mode :boolean]
   [:status [:map {:closed true}
             [:message :string]
             [:type [:enum :info :warning :error]]]]
   [:active-tab [:map {:closed true}
                 [:tag :keyword]]]
   [:tab-properties [:map-of :keyword [:map {:closed true}
                                       [:has-window :boolean]
                                       [:window-bounds {:optional true}
                                        [:map {:closed true}
                                         [:x :int]
                                         [:y :int]
                                         [:width :int]
                                         [:height :int]]]]]]])

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

(def app-state-spec
  "Complete application state specification"
  [:map {:closed true}
   [:connection connection-state]
   [:ui ui-state]
   [:processes processes-state]
   [:stream-processes stream-processes-state]
   [:session session-state]
   [:server-state [:maybe :state/root]]])

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

  ;; Complete app state
  (registry/register-spec! ::app-state app-state-spec))

(m/=> register-state-specs! [:=> [:cat] :nil])

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

(m/=> ensure-specs-registered! [:=> [:cat] :nil])

;; ============================================================================
;; State Atom
;; ============================================================================

(def initial-state
  "Initial application state"
  {:connection {:url ""
                :connected? false
                :latency-ms nil
                :reconnect-count 0}
   :ui {:theme :sol-dark
        :locale :english
        :fullscreen false
        :show-overlay false
        :read-only-mode false
        :status {:message "" :type :info}
        :active-tab {:tag :controls}
        :tab-properties {:controls {:has-window false}
                         :day-camera {:has-window false}
                         :thermal-camera {:has-window false}
                         :modes {:has-window false}
                         :media {:has-window false}}}
   :processes {:state-proc {:status :stopped :pid nil}
               :cmd-proc {:status :stopped :pid nil}
               :heat-video {:status :stopped :pid nil}
               :day-video {:status :stopped :pid nil}}
   :stream-processes {}
   :session {:user nil
             :started-at nil}
   :server-state nil})

(defonce ^{:doc "Main app state"} app-state
  (atom initial-state))

;; ============================================================================
;; Connection Management
;; ============================================================================

(defn get-connection-url
  "Get current connection URL."
  []
  (get-in @app-state [:connection :url]))
(m/=> get-connection-url [:=> [:cat] [:maybe :string]])

(defn set-connection-url!
  "Set connection URL."
  [url]
  (swap! app-state assoc-in [:connection :url] url))
(m/=> set-connection-url! [:=> [:cat :string] :map])

(defn connected?
  "Check if connected to server."
  []
  (get-in @app-state [:connection :connected?] false))
(m/=> connected? [:=> [:cat] :boolean])

(defn set-connected!
  "Set connection status."
  [connected?]
  (swap! app-state assoc-in [:connection :connected?] connected?))
(m/=> set-connected! [:=> [:cat :boolean] :map])

(defn get-domain
  "Get current domain from connection URL."
  []
  (if-let [url (get-connection-url)]
    (try
      (let [uri (URI. url)
            host (.getHost uri)]
        (or host "localhost"))
      (catch Exception _
        "localhost"))
    "localhost"))
(m/=> get-domain [:=> [:cat] :string])

;; ============================================================================
;; UI Configuration
;; ============================================================================

(defn get-locale
  "Get current locale."
  []
  (get-in @app-state [:ui :locale] :english))
(m/=> get-locale [:=> [:cat] [:enum :english :ukrainian]])

(defn set-locale!
  "Set current locale and update Java default."
  [locale]
  (let [locale-map {:english ["en" "US"]
                    :ukrainian ["uk" "UA"]}
        [lang country] (get locale-map locale ["en" "US"])]
    (Locale/setDefault
      (Locale/forLanguageTag (str lang "-" country)))
    (swap! app-state assoc-in [:ui :locale] locale)))
(m/=> set-locale! [:=> [:cat [:enum :english :ukrainian]] :map])

(defn get-theme
  "Get current theme."
  []
  (get-in @app-state [:ui :theme] :sol-dark))
(m/=> get-theme [:=> [:cat] :keyword])

(defn set-theme!
  "Set UI theme."
  [theme]
  (swap! app-state assoc-in [:ui :theme] theme))
(m/=> set-theme! [:=> [:cat :keyword] :map])

(defn fullscreen?
  "Check if in fullscreen mode."
  []
  (get-in @app-state [:ui :fullscreen] false))
(m/=> fullscreen? [:=> [:cat] :boolean])

(defn set-fullscreen!
  "Set fullscreen mode."
  [fullscreen?]
  (swap! app-state assoc-in [:ui :fullscreen] fullscreen?))
(m/=> set-fullscreen! [:=> [:cat :boolean] :map])

;; ============================================================================
;; Session Management
;; ============================================================================

(defn get-session
  "Get current session info."
  []
  (get @app-state :session {}))
(m/=> get-session [:=> [:cat] :map])

(defn start-session!
  "Start a new session."
  [user]
  (swap! app-state assoc :session {:user user
                                   :started-at (System/currentTimeMillis)}))
(m/=> start-session! [:=> [:cat [:maybe :string]] :map])

(defn end-session!
  "End current session."
  []
  (swap! app-state assoc :session {:user nil
                                   :started-at nil}))
(m/=> end-session! [:=> [:cat] :map])

;; ============================================================================
;; Process Management
;; ============================================================================

(defn get-process-state
  "Get state of a specific process."
  [process-key]
  (get-in @app-state [:processes process-key]))
(m/=> get-process-state [:=> [:cat [:enum :state-proc :cmd-proc :heat-video :day-video]] [:maybe :map]])

(defn process-running?
  "Check if a process is running."
  [process-key]
  (= :running (get-in @app-state [:processes process-key :status])))
(m/=> process-running? [:=> [:cat [:enum :state-proc :cmd-proc :heat-video :day-video]] :boolean])

(defn update-process-status!
  "Update process status."
  [process-key pid status]
  (swap! app-state assoc-in [:processes process-key]
         {:pid pid :status status}))
(m/=> update-process-status! [:=> [:cat [:enum :state-proc :cmd-proc :heat-video :day-video] [:maybe :pos-int] [:enum :starting :running :stopping :stopped :error]] :map])

;; ============================================================================
;; Stream Process Management
;; ============================================================================

(defn add-stream-process!
  "Add stream process info."
  [stream-type process-map]
  (let [process-key (case stream-type
                      :heat :heat-video
                      :day :day-video)]
    (swap! app-state assoc-in [:stream-processes process-key] process-map)))
(m/=> add-stream-process! [:=> [:cat [:enum :heat :day] :map] :map])

(defn get-stream-process
  "Get stream process info."
  [stream-type]
  (let [process-key (case stream-type
                      :heat :heat-video
                      :day :day-video)]
    (get-in @app-state [:stream-processes process-key])))
(m/=> get-stream-process [:=> [:cat [:enum :heat :day]] [:maybe :map]])

(defn remove-stream-process!
  "Remove stream process."
  [stream-type]
  (let [process-key (case stream-type
                      :heat :heat-video
                      :day :day-video)]
    (swap! app-state update :stream-processes dissoc process-key)))
(m/=> remove-stream-process! [:=> [:cat [:enum :heat :day]] :map])

(defn get-all-stream-processes
  "Get all stream processes."
  []
  (get @app-state :stream-processes {}))
(m/=> get-all-stream-processes [:=> [:cat] [:map-of :keyword :map]])

;; ============================================================================
;; Server State Management
;; ============================================================================

(defn get-server-state
  "Get the current server state."
  []
  (:server-state @app-state))
(m/=> get-server-state [:=> [:cat] :map])

(defn update-server-state!
  "Update server state."
  [updates]
  (swap! app-state update :server-state merge updates))
(m/=> update-server-state! [:=> [:cat :map] :map])

(defn get-subsystem-state
  "Get state for a specific subsystem."
  [subsystem]
  (get-in @app-state [:server-state subsystem]))
(m/=> get-subsystem-state [:=> [:cat :keyword] [:maybe :map]])

;; ============================================================================
;; Extended UI Configuration
;; ============================================================================

(defn read-only-mode?
  "Check if in read-only mode."
  []
  (get-in @app-state [:ui :read-only-mode] false))
(m/=> read-only-mode? [:=> [:cat] :boolean])

(defn set-read-only-mode!
  "Set read-only mode."
  [enabled?]
  (swap! app-state assoc-in [:ui :read-only-mode] enabled?))
(m/=> set-read-only-mode! [:=> [:cat :boolean] :map])

(defn show-overlay?
  "Check if overlay should be shown."
  []
  (get-in @app-state [:ui :show-overlay] true))
(m/=> show-overlay? [:=> [:cat] :boolean])

(defn set-show-overlay!
  "Set overlay visibility."
  [show?]
  (swap! app-state assoc-in [:ui :show-overlay] show?))
(m/=> set-show-overlay! [:=> [:cat :boolean] :map])

;; ============================================================================
;; Tab Management
;; ============================================================================

(defn get-active-tab
  "Get the currently active tab."
  []
  (get-in @app-state [:ui :active-tab :tag] :controls))
(m/=> get-active-tab [:=> [:cat] :keyword])

(defn set-active-tab!
  "Set the active tab."
  [tab-key]
  (swap! app-state assoc-in [:ui :active-tab :tag] tab-key))
(m/=> set-active-tab! [:=> [:cat :keyword] :map])

(defn get-tab-property
  "Get a property for a specific tab."
  [tab-key property]
  (get-in @app-state [:ui :tab-properties tab-key property]))
(m/=> get-tab-property [:=> [:cat :keyword :keyword] :any])

(defn set-tab-property!
  "Set a property for a specific tab."
  [tab-key property value]
  (swap! app-state assoc-in [:ui :tab-properties tab-key property] value))
(m/=> set-tab-property! [:=> [:cat :keyword :keyword :any] :map])

(defn tab-has-window?
  "Check if a tab has its window open."
  [tab-key]
  (get-tab-property tab-key :has-window))
(m/=> tab-has-window? [:=> [:cat :keyword] :boolean])

(defn set-tab-window!
  "Set whether a tab has its window open."
  [tab-key has-window?]
  (set-tab-property! tab-key :has-window has-window?))
(m/=> set-tab-window! [:=> [:cat :keyword :boolean] :map])

(defn get-tab-window-bounds
  "Get the saved window bounds for a tab."
  [tab-key]
  (get-tab-property tab-key :window-bounds))
(m/=> get-tab-window-bounds [:=> [:cat :keyword] [:maybe [:map [:x :int] [:y :int] [:width :int] [:height :int]]]])

(defn set-tab-window-bounds!
  "Save window bounds for a tab."
  [tab-key x y width height]
  (set-tab-property! tab-key :window-bounds {:x x :y y :width width :height height}))
(m/=> set-tab-window-bounds! [:=> [:cat :keyword :int :int :int :int] :map])

;; ============================================================================
;; Extended Connection Management
;; ============================================================================

(defn get-connection-latency
  "Get connection latency in milliseconds."
  []
  (get-in @app-state [:connection :latency-ms]))
(m/=> get-connection-latency [:=> [:cat] [:maybe :pos-int]])

(defn set-connection-latency!
  "Set connection latency in milliseconds."
  [latency-ms]
  (swap! app-state assoc-in [:connection :latency-ms] latency-ms))
(m/=> set-connection-latency! [:=> [:cat [:maybe :nat-int]] :map])

(defn get-reconnect-count
  "Get reconnection attempt count."
  []
  (get-in @app-state [:connection :reconnect-count] 0))
(m/=> get-reconnect-count [:=> [:cat] :int])

(defn set-connection-state!
  "Set complete connection state."
  [connected? url latency-ms]
  (swap! app-state update :connection
         (fn [conn]
           (cond-> (assoc conn :connected? connected?)
             url (assoc :url url)
             latency-ms (assoc :latency-ms latency-ms)))))
(m/=> set-connection-state! [:=> [:cat :boolean [:maybe :string] [:maybe :pos-int]] :map])

(defn increment-reconnect-count!
  "Increment reconnection attempt counter."
  []
  (swap! app-state update-in [:connection :reconnect-count] (fnil inc 0)))
(m/=> increment-reconnect-count! [:=> [:cat] :map])

(defn reset-reconnect-count!
  "Reset reconnection attempt counter."
  []
  (swap! app-state assoc-in [:connection :reconnect-count] 0))
(m/=> reset-reconnect-count! [:=> [:cat] :map])

;; ============================================================================
;; State Observation
;; ============================================================================

(defn add-watch-handler
  "Add a watch handler to app-state."
  [key handler-fn]
  (add-watch app-state key handler-fn)
  nil)
(m/=> add-watch-handler [:=> [:cat :keyword fn?] :nil])

(defn remove-watch-handler
  "Remove a watch handler from app-state."
  [key]
  (remove-watch app-state key)
  nil)
(m/=> remove-watch-handler [:=> [:cat :keyword] :nil])

(defn cleanup-seesaw-bindings!
  "Remove all watchers added by seesaw.bind from app-state.
  Seesaw bind uses gensym keys like :bindable-atom-watcherXXXXX
  This scans and removes all such watchers."
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
(m/=> cleanup-seesaw-bindings! [:=> [:cat] :nil])

(defn current-state
  "Get a snapshot of entire app state (for debugging)."
  []
  @app-state)
(m/=> current-state [:=> [:cat] :map])

(defn reset-state!
  "Reset entire app state to initial values."
  []
  (reset! app-state initial-state))
(m/=> reset-state! [:=> [:cat] :map])

;; ============================================================================
;; State Validation
;; ============================================================================

(defn validate-state
  "Validate the current app state against the schema.
   Returns nil if valid, or validation errors if invalid."
  []
  (ensure-specs-registered!)
  (when-not (m/validate app-state-spec @app-state)
    (m/explain app-state-spec @app-state)))
(m/=> validate-state [:=> [:cat] [:maybe :map]])

(defn validate-state!
  "Validate state and throw exception if invalid.
   Useful for development assertions."
  []
  (when-let [errors (validate-state)]
    (throw (ex-info "Invalid app state" {:errors errors})))
  nil)
(m/=> validate-state! [:=> [:cat] :nil])

(defn valid-state?
  "Check if current state is valid."
  []
  (ensure-specs-registered!)
  (m/validate app-state-spec @app-state))
(m/=> valid-state? [:=> [:cat] :boolean])

(defn validate-partial
  "Validate a partial state update against a specific schema."
  [schema-key value]
  (let [registry (registry/get-registry)
        schema (get registry schema-key)]
    (when (and schema (not (m/validate schema value)))
      (m/explain schema value))))
(m/=> validate-partial [:=> [:cat :keyword :any] [:maybe :map]])

(defn safe-swap!
  "Swap app-state with validation in development mode.
   In production, behaves like normal swap!"
  [f & args]
  (let [result (apply swap! app-state f args)]
    ;; Only validate in development
    (when (and (not (runtime/release-build?))
               (not (valid-state?)))
      (let [errors (validate-state)]
        (println "WARNING: State validation failed after swap!")
        (println "Errors:" errors)))
    result))
(m/=> safe-swap! [:=> [:cat fn? [:* :any]] :map])

(defn safe-reset!
  "Reset app-state with validation in development mode."
  [new-state]
  (ensure-specs-registered!)
  (when (and (not (runtime/release-build?))
             (not (m/validate app-state-spec new-state)))
    (let [errors (m/explain app-state-spec new-state)]
      (throw (ex-info "Cannot reset to invalid state" {:errors errors}))))
  (reset! app-state new-state))
(m/=> safe-reset! [:=> [:cat :map] :map])
