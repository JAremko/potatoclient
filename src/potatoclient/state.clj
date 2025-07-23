(ns potatoclient.state
  "Application state management for PotatoClient.
  
  Provides centralized state management with clear boundaries between
  different state concerns (streams, UI, configuration).
  
  This namespace re-exports all state management functions from the
  sub-namespaces for backward compatibility."
  (:require [com.fulcrologic.guardrails.malli.core :refer [=> >defn]]
            [potatoclient.state.config :as config]
            [potatoclient.state.streams :as streams]
            [potatoclient.state.ui :as ui]))

;; Re-export stream management functions
(def get-stream
  "Get stream process for given key."
  streams/get-stream)
(def set-stream!
  "Set stream process for given key."
  streams/set-stream!)
(def clear-stream!
  "Clear stream process for given key."
  streams/clear-stream!)
(def all-streams
  "Get all stream processes."
  streams/all-streams)

;; Re-export configuration functions
(def get-locale
  "Get current locale."
  config/get-locale)
(def set-locale!
  "Set current locale."
  config/set-locale!)
(def get-domain
  "Get configured domain."
  config/get-domain)
(def set-domain!
  "Set configured domain."
  config/set-domain!)

;; Re-export UI functions
(def register-ui-element!
  "Register UI element reference."
  ui/register-ui-element!)
(def get-ui-element
  "Get UI element reference."
  ui/get-ui-element)

;; State inspection (useful for debugging/REPL)
(>defn current-state
  "Get a snapshot of all application state.
  Useful for debugging - not for normal application use."
  []
  [=> [:map
       [:streams [:map-of :potatoclient.specs/stream-key [:maybe :potatoclient.specs/stream-process-map]]]
       [:config [:map
                 [:locale :potatoclient.specs/locale]
                 [:domain :potatoclient.specs/domain]]]]]
  {:streams (streams/all-streams)
   :config (config/get-config)
   :ui-elements (ui/all-ui-elements)})

;; Legacy compatibility - these are still used in some places
(def app-state
  "Legacy reference to streams atom."
  streams/app-state)
(def ui-elements
  "Legacy reference to UI elements atom."
  ui/ui-elements)