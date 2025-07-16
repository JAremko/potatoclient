(ns potatoclient.state
  "Application state management for PotatoClient.
  
  Provides centralized state management with clear boundaries between
  different state concerns (streams, UI, configuration)."
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

;; UI component references for updates
(defonce ^:private ui-refs
  (atom {}))

;; Stream management
(defn get-stream
  "Get a stream process by key (:heat or :day)."
  [stream-key]
  {:pre [(m/validate specs/stream-key stream-key)]}
  (get @streams-state stream-key))


(defn set-stream!
  "Set a stream process."
  [stream-key stream]
  {:pre [(m/validate specs/stream-key stream-key)
         (map? stream)]}
  (swap! streams-state assoc stream-key stream))


(defn clear-stream!
  "Clear a stream process."
  [stream-key]
  {:pre [(m/validate specs/stream-key stream-key)]}
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


;; UI element management
(defn register-ui-element!
  "Register a UI element for later updates."
  [element-key element]
  {:pre [(keyword? element-key)
         (some? element)]}
  (swap! ui-refs assoc element-key element))


(defn get-ui-element
  "Get a registered UI element."
  [element-key]
  {:pre [(keyword? element-key)]}
  (get @ui-refs element-key))


;; Configuration management
(defn get-locale
  "Get the current locale."
  []
  (:locale @app-config))


(defn set-locale!
  "Set the current locale."
  [locale]
  {:pre [(m/validate specs/locale locale)]}
  (swap! app-config assoc :locale locale)
  ;; Also update default Locale
  (let [locale-map {:english ["en" "US"]
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
   :ui-elements (keys @ui-refs)})


;; Atom access for legacy compatibility
(def app-state streams-state)
(def ui-elements ui-refs)