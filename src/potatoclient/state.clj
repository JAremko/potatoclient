(ns potatoclient.state
  "Application state management for PotatoClient using Transit app-db.
  
  This namespace provides all state management functions that directly
  interact with the single app-db atom following the re-frame pattern."
  (:require [com.fulcrologic.guardrails.malli.core :refer [=> >defn >defn- | ?]]
            [potatoclient.ui-specs :as specs]
            [potatoclient.transit.app-db :as app-db]))

;; ============================================================================
;; Stream Process Management
;; ============================================================================

(>defn get-stream
  "Get stream process for given key (:heat or :day)."
  [stream-key]
  [::specs/stream-key => (? map?)]
  (case stream-key
    :heat (app-db/get-process-state :heat-video)
    :day (app-db/get-process-state :day-video)
    nil))

(>defn set-stream!
  "Set stream process for given key."
  [stream-key pid status]
  [::specs/stream-key (? pos-int?) [:enum :running :stopped :error] => map?]
  (let [process-key (case stream-key
                      :heat :heat-video
                      :day :day-video)]
    (app-db/set-process-state! process-key pid status)))

(>defn clear-stream!
  "Clear stream process for given key."
  [stream-key]
  [::specs/stream-key => map?]
  (set-stream! stream-key nil :stopped))

(>defn stream-running?
  "Check if a stream is running."
  [stream-key]
  [::specs/stream-key => boolean?]
  (let [process-key (case stream-key
                      :heat :heat-video
                      :day :day-video)]
    (app-db/process-running? process-key)))

(>defn all-streams
  "Get all stream process states."
  []
  [=> map?]
  {:heat (app-db/get-process-state :heat-video)
   :day (app-db/get-process-state :day-video)})

;; ============================================================================
;; Configuration Management
;; ============================================================================

(>defn get-locale
  "Get current locale."
  []
  [=> ::specs/locale]
  (app-db/get-locale))

(>defn set-locale!
  "Set current locale and update Java default."
  [locale]
  [::specs/locale => map?]
  ;; Update app-db
  (app-db/set-locale! locale)
  ;; Also update Java default Locale
  (let [locale-map {:english ["en" "US"]
                    :ukrainian ["uk" "UA"]}
        [lang country] (get locale-map locale ["en" "US"])]
    (java.util.Locale/setDefault
      (java.util.Locale. ^String lang ^String country)))
  (app-db/get-app-state))

(>defn get-theme
  "Get current theme."
  []
  [=> ::specs/theme-key]
  (app-db/get-theme))

(>defn set-theme!
  "Set UI theme."
  [theme]
  [::specs/theme-key => map?]
  (app-db/set-theme! theme))

(>defn get-domain
  "Get current domain from connection URL."
  []
  [=> string?]
  (if-let [url (app-db/get-connection-url)]
    (try
      (let [uri (java.net.URI. url)
            host (.getHost uri)]
        (or host "localhost"))
      (catch Exception _
        "localhost"))
    "localhost"))

(>defn set-domain!
  "Set domain by updating connection URL."
  [domain]
  [::specs/domain => map?]
  (let [url (str "wss://" domain)]
    (app-db/set-connection-state! false url nil)))

;; ============================================================================
;; Connection Management
;; ============================================================================

(>defn connected?
  "Check if connected to server."
  []
  [=> boolean?]
  (app-db/connected?))

(>defn get-connection-url
  "Get current connection URL."
  []
  [=> (? string?)]
  (app-db/get-connection-url))

(>defn set-connection-state!
  "Update connection state."
  [connected? url latency-ms]
  [boolean? (? string?) (? pos-int?) => map?]
  (app-db/set-connection-state! connected? url latency-ms))

;; ============================================================================
;; Device State Access
;; ============================================================================

(>defn get-system-state
  "Get system subsystem state."
  []
  [=> (? map?)]
  (app-db/get-subsystem-state :system))

(>defn get-lrf-state
  "Get LRF subsystem state."
  []
  [=> (? map?)]
  (app-db/get-subsystem-state :lrf))

(>defn get-gps-state
  "Get GPS subsystem state."
  []
  [=> (? map?)]
  (app-db/get-subsystem-state :gps))

(>defn get-compass-state
  "Get compass subsystem state."
  []
  [=> (? map?)]
  (app-db/get-subsystem-state :compass))

(>defn get-rotary-state
  "Get rotary platform subsystem state."
  []
  [=> (? map?)]
  (app-db/get-subsystem-state :rotary))

(>defn get-camera-day-state
  "Get day camera subsystem state."
  []
  [=> (? map?)]
  (app-db/get-subsystem-state :camera-day))

(>defn get-camera-heat-state
  "Get heat camera subsystem state."
  []
  [=> (? map?)]
  (app-db/get-subsystem-state :camera-heat))

(>defn get-glass-heater-state
  "Get glass heater subsystem state."
  []
  [=> (? map?)]
  (app-db/get-subsystem-state :glass-heater))

;; ============================================================================
;; Device State Updates (from Transit messages)
;; ============================================================================

(>defn update-subsystem!
  "Update a specific subsystem state."
  [subsystem state-update]
  [keyword? map? => map?]
  (app-db/update-subsystem! subsystem state-update))

(>defn update-server-state!
  "Update entire server state from Transit message."
  [state-update]
  [map? => map?]
  (app-db/update-server-state! state-update))

;; ============================================================================
;; UI State Management
;; ============================================================================

(>defn read-only-mode?
  "Check if in read-only mode."
  []
  [=> boolean?]
  (app-db/read-only-mode?))

(>defn set-read-only-mode!
  "Set read-only mode."
  [enabled?]
  [boolean? => map?]
  (app-db/set-read-only-mode! enabled?))

;; ============================================================================
;; State Observation
;; ============================================================================

(>defn add-state-watch
  "Add a watch handler to app-db."
  [key handler-fn]
  [keyword? fn? => nil?]
  (app-db/add-watch-handler key handler-fn))

(>defn remove-state-watch
  "Remove a watch handler from app-db."
  [key]
  [keyword? => nil?]
  (app-db/remove-watch-handler key))

(>defn current-state
  "Get a snapshot of entire app state (for debugging)."
  []
  [=> map?]
  @app-db/app-db)

(>defn server-state
  "Get current server state."
  []
  [=> map?]
  (app-db/get-server-state))

(>defn app-state
  "Get current app state."
  []
  [=> map?]
  (app-db/get-app-state))

;; ============================================================================
;; Validation and Rate Limiting
;; ============================================================================

(>defn validation-enabled?
  "Check if validation is enabled."
  []
  [=> boolean?]
  (get-in @app-db/app-db [:validation :enabled?]))

(>defn get-rate-limits
  "Get current rate limit configuration."
  []
  [=> map?]
  (app-db/get-rate-limits))

;; ============================================================================
;; Process Management
;; ============================================================================

(>defn get-all-processes
  "Get all process states."
  []
  [=> map?]
  (get-in @app-db/app-db [:app-state :processes]))

(>defn set-subprocess-state!
  "Set Transit subprocess state."
  [process-key pid status]
  [[:enum :state-proc :cmd-proc] (? pos-int?) [:enum :running :stopped :error] => map?]
  (app-db/set-process-state! process-key pid status))

;; ============================================================================
;; Initialization
;; ============================================================================

(>defn reset-state!
  "Reset entire app state to initial values."
  []
  [=> map?]
  (app-db/reset-to-initial-state!))