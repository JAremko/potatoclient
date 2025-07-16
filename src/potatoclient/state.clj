(ns potatoclient.state
  "Application state management for PotatoClient.
  
  Provides centralized state management with clear boundaries between
  different state concerns (streams, UI, configuration).
  
  This namespace re-exports all state management functions from the
  sub-namespaces for backward compatibility."
  (:require [potatoclient.state.streams :as streams]
            [potatoclient.state.config :as config]
            [potatoclient.state.ui :as ui]))

;; Re-export stream management functions
(def get-stream streams/get-stream)
(def set-stream! streams/set-stream!)
(def clear-stream! streams/clear-stream!)
(def all-streams streams/all-streams)

;; Re-export configuration functions
(def get-locale config/get-locale)
(def set-locale! config/set-locale!)
(def get-domain config/get-domain)
(def set-domain! config/set-domain!)

;; Re-export UI functions
(def register-ui-element! ui/register-ui-element!)
(def get-ui-element ui/get-ui-element)

;; State inspection (useful for debugging/REPL)
(defn current-state
  "Get a snapshot of all application state.
  Useful for debugging - not for normal application use."
  []
  {:streams (streams/all-streams)
   :config (config/get-config)
   :ui-elements (ui/all-ui-elements)})

;; Legacy compatibility - these are still used in some places
(def app-state streams/app-state)
(def ui-elements ui/ui-elements)