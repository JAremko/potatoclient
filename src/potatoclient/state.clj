(ns potatoclient.state
  "Application state management for PotatoClient UI.
  
  This namespace provides the core UI state atom and basic accessors
  for managing application UI state."
  (:require [com.fulcrologic.guardrails.malli.core :refer [=> >defn >defn- | ?]]
            [potatoclient.runtime :as runtime]))

;; ============================================================================
;; State Atom
;; ============================================================================

(def initial-state
  "Initial application UI state"
  {:connection {:url ""
                :connected? false}
   :ui {:theme :sol-dark
        :locale :english
        :fullscreen? false}
   :session {:user nil
             :started-at nil}})

(defonce app-state 
  (atom initial-state))

;; ============================================================================
;; Connection Management
;; ============================================================================

(>defn get-connection-url
  "Get current connection URL."
  []
  [=> (? string?)]
  (get-in @app-state [:connection :url]))

(>defn set-connection-url!
  "Set connection URL."
  [url]
  [string? => map?]
  (swap! app-state assoc-in [:connection :url] url))

(>defn connected?
  "Check if connected to server."
  []
  [=> boolean?]
  (get-in @app-state [:connection :connected?] false))

(>defn set-connected!
  "Set connection status."
  [connected?]
  [boolean? => map?]
  (swap! app-state assoc-in [:connection :connected?] connected?))

(>defn get-domain
  "Get current domain from connection URL."
  []
  [=> string?]
  (if-let [url (get-connection-url)]
    (try
      (let [uri (java.net.URI. url)
            host (.getHost uri)]
        (or host "localhost"))
      (catch Exception _
        "localhost"))
    "localhost"))

;; ============================================================================
;; UI Configuration
;; ============================================================================

(>defn get-locale
  "Get current locale."
  []
  [=> [:enum :english :ukrainian]]
  (get-in @app-state [:ui :locale] :english))

(>defn set-locale!
  "Set current locale and update Java default."
  [locale]
  [[:enum :english :ukrainian] => map?]
  (let [locale-map {:english ["en" "US"]
                    :ukrainian ["uk" "UA"]}
        [lang country] (get locale-map locale ["en" "US"])]
    (java.util.Locale/setDefault
      (java.util.Locale. ^String lang ^String country)))
  (swap! app-state assoc-in [:ui :locale] locale))

(>defn get-theme
  "Get current theme."
  []
  [=> keyword?]
  (get-in @app-state [:ui :theme] :sol-dark))

(>defn set-theme!
  "Set UI theme."
  [theme]
  [keyword? => map?]
  (swap! app-state assoc-in [:ui :theme] theme))

(>defn fullscreen?
  "Check if in fullscreen mode."
  []
  [=> boolean?]
  (get-in @app-state [:ui :fullscreen?] false))

(>defn set-fullscreen!
  "Set fullscreen mode."
  [fullscreen?]
  [boolean? => map?]
  (swap! app-state assoc-in [:ui :fullscreen?] fullscreen?))

;; ============================================================================
;; Session Management
;; ============================================================================

(>defn get-session
  "Get current session info."
  []
  [=> map?]
  (get @app-state :session {}))

(>defn start-session!
  "Start a new session."
  [user]
  [(? string?) => map?]
  (swap! app-state assoc :session {:user user
                                   :started-at (System/currentTimeMillis)}))

(>defn end-session!
  "End current session."
  []
  [=> map?]
  (swap! app-state assoc :session {:user nil
                                   :started-at nil}))

;; ============================================================================
;; State Observation
;; ============================================================================

(>defn add-watch-handler
  "Add a watch handler to app-state."
  [key handler-fn]
  [keyword? fn? => nil?]
  (add-watch app-state key handler-fn)
  nil)

(>defn remove-watch-handler
  "Remove a watch handler from app-state."
  [key]
  [keyword? => nil?]
  (remove-watch app-state key)
  nil)

(>defn current-state
  "Get a snapshot of entire app state (for debugging)."
  []
  [=> map?]
  @app-state)

(>defn reset-state!
  "Reset entire app state to initial values."
  []
  [=> map?]
  (reset! app-state initial-state))