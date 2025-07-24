(ns potatoclient.dev
  "Development utilities and settings.
  
  This namespace is automatically loaded when running in development mode
  to enable various debugging and development features."
  (:require [com.fulcrologic.guardrails.core :refer [>defn =>]]
            [potatoclient.logging :as logging]))

(>defn enable-verbose-logging!
  "Enable verbose logging for development."
  []
  [=> nil?]
  (logging/log-info {:msg "Enabling verbose logging for development..."})
  ;; Add verbose logging configuration here
  nil)

(>defn enable-assertions!
  "Enable assertions for development."
  []
  [=> nil?]
  (logging/log-info {:msg "Enabling assertions for development..."})
  ;; Add assertion configuration here
  nil)

(>defn enable-all-dev-settings!
  "Enable all development-specific settings.
  Note: Instrumentation and reflection warnings are now enabled 
  for all non-release builds in main.clj"
  []
  [=> nil?]
  (enable-verbose-logging!)
  (enable-assertions!)
  ;; Add more development-specific settings here as needed
  (logging/log-info {:msg "Additional development settings enabled."}))

;; Automatically enable dev settings when this namespace is loaded
(when (or (System/getProperty "potatoclient.dev")
          (System/getenv "POTATOCLIENT_DEV"))
  (enable-all-dev-settings!))