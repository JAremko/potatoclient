(ns potatoclient.dev
  "Development utilities and settings.

  This namespace is automatically loaded when running in development mode
  to enable various debugging and development features."
  (:require [potatoclient.dev-instrumentation :as dev-inst]
            [potatoclient.init :as init]
            [potatoclient.logging :as logging]))

(defn enable-verbose-logging!
  "Enable verbose logging for development."
  {:malli/schema [:=> [:cat] :nil]}
  []
  (logging/log-info {:msg "Enabling verbose logging for development..."})
  ;; TODO: Add verbose logging configuration here
  nil)

(defn enable-assertions!
  "Enable assertions for development."
  {:malli/schema [:=> [:cat] :nil]}
  []
  (logging/log-info {:msg "Enabling assertions for development..."})
  ;; TODO: Add assertion configuration here
  nil)

(defn enable-all-dev-settings!
  "Enable all development-specific settings.
  Note: Instrumentation and reflection warnings are now enabled
  for all non-release builds in main.clj"
  {:malli/schema [:=> [:cat] :nil]}
  []
  ;; Ensure core initialization is done first
  (init/initialize!)
  (enable-verbose-logging!)
  (enable-assertions!)
  ;; Instrumentation is already started by init.clj in dev mode
  ;; No need to start it again here
  ;; Add more development-specific settings here as needed
  (logging/log-info {:msg "Additional development settings enabled."}))

;; Automatically enable dev settings when this namespace is loaded
(when (or (System/getProperty "potatoclient.dev")
          (System/getenv "POTATOCLIENT_DEV"))
  (enable-all-dev-settings!))
