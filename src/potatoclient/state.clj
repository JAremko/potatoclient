(ns potatoclient.state
  "Application state management for PotatoClient.
  
  Provides centralized state management with clear boundaries between
  different state concerns (streams, logs, UI, configuration)."
  (:require [potatoclient.config :as config]
            [malli.core :as m]
            [potatoclient.specs :as specs]
            [clojure.string]))


;; Core application state - separated by concern
;; Stream process references
(defonce ^:private streams-state
  (atom {:heat nil
         :day nil}))

;; Runtime configuration state
(defonce ^:private app-config
  (atom {:locale :english}))

;; Log entries and buffering
(defonce ^:private logs-state
  (atom {:entries []
         :buffer []
         :update-scheduled? false}))

;; UI component references for updates
(defonce ^:private ui-refs
  (atom {}))

;; Constants
(def ^:private max-log-entries 100)
(def ^:private log-buffer-threshold 200)
(def ^:private log-buffer-drop-count 100)

;; Stream management
(defn get-stream
  "Get a stream process by key (:heat or :day)."
  [stream-key]
  {:pre [(m/validate ::specs/stream-key stream-key)]}
  (get @streams-state stream-key))


(defn set-stream!
  "Set a stream process reference."
  [stream-key stream]
  {:pre [(m/validate ::specs/stream-key stream-key)
         (m/validate ::specs/stream-process stream)]}
  (swap! streams-state assoc stream-key stream))


(defn clear-stream!
  "Clear a stream process reference."
  [stream-key]
  {:pre [(m/validate ::specs/stream-key stream-key)]}
  (swap! streams-state assoc stream-key nil))


(defn all-streams
  "Get all stream entries as a map."
  []
  @streams-state)


;; Domain/server configuration
(defn get-domain
  "Get the current domain configuration from persistent config."
  []
  (config/get-domain))


(defn set-domain!
  "Update the domain configuration persistently."
  [domain]
  {:pre [(string? domain)
         (not (clojure.string/blank? domain))]}
  (config/save-domain! domain))


;; Log management
(defn- merge-log-buffers
  "Merge buffer entries with existing entries, maintaining size limit."
  [{:keys [entries buffer]}]
  (let [new-entries (vec (take max-log-entries 
                              (concat buffer entries)))]
    {:entries new-entries
     :buffer []
     :update-scheduled? false}))

(defn add-log-entry!
  "Add a log entry to the buffer."
  [entry]
  {:pre [(m/validate ::specs/log-entry entry)]}
  (swap! logs-state update :buffer conj entry)
  ;; Trim buffer if too large
  (when (> (count (:buffer @logs-state)) log-buffer-threshold)
    (swap! logs-state update :buffer 
           #(vec (drop log-buffer-drop-count %)))))


(defn flush-log-buffer!
  "Flush log buffer to main log entries."
  []
  (swap! logs-state merge-log-buffers))


(defn clear-logs!
  "Clear all log entries."
  []
  (swap! logs-state assoc :entries [] :buffer []))


(defn get-log-entries
  "Get all current log entries (not including buffered)."
  []
  (:entries @logs-state))


(defn get-update-scheduled?
  "Check if a log update is already scheduled."
  []
  (:update-scheduled? @logs-state))


(defn set-update-scheduled!
  "Set the update scheduled flag."
  [scheduled?]
  (swap! logs-state assoc :update-scheduled? scheduled?))


;; UI element management
(defn register-ui-element!
  "Register a UI element for later updates."
  [element-key element]
  {:pre [(keyword? element-key)
         (some? element)]}
  (swap! ui-refs assoc element-key element))


(defn get-ui-element
  "Get a UI element reference by key."
  [element-key]
  {:pre [(keyword? element-key)]}
  (get @ui-refs element-key))


(defn unregister-ui-element!
  "Remove a UI element reference."
  [element-key]
  {:pre [(keyword? element-key)]}
  (swap! ui-refs dissoc element-key))


;; Locale management
(defn get-locale
  "Get the current locale setting."
  []
  (:locale @app-config))


(defn set-locale!
  "Set the locale and update Java locale accordingly."
  [locale]
  {:pre [(m/validate ::specs/locale locale)]}
  (swap! app-config assoc :locale locale)
  ;; Update Java locale for proper i18n
  (let [locale-map {:english   ["en" "US"]
                    :ukrainian ["uk" "UA"]}
        [lang country] (get locale-map locale ["en" "US"])]
    (java.util.Locale/setDefault
     (java.util.Locale. ^String lang ^String country))))


;; State inspection (useful for debugging/REPL)
(defn current-state
  "Get a snapshot of all application state.
  Useful for debugging - not for normal application use."
  []
  {:streams @streams-state
   :config @app-config
   :logs (select-keys @logs-state [:entries])
   :ui-elements (keys @ui-refs)})


;; Atom access for legacy compatibility
;; TODO: Gradually phase these out
(def app-state streams-state)
(def log-entries (atom []))
(def log-buffer (atom []))
(def update-scheduled (atom false))
(def ui-elements ui-refs)

;; Watchers to sync legacy atoms
(add-watch logs-state :legacy-sync
           (fn [_ _ _ new-state]
             (reset! log-entries (:entries new-state))
             (reset! log-buffer (:buffer new-state))
             (reset! update-scheduled (:update-scheduled? new-state))))