(ns potatoclient.dev
  "Development utilities and settings.
  
  This namespace is automatically loaded when running in development mode
  to enable various debugging and development features."
  (:require [orchestra.spec.test :as st]))

(defn enable-verbose-logging!
  "Enable verbose logging for development."
  []
  (println "Enabling verbose logging for development...")
  ;; Add verbose logging configuration here
  )

(defn enable-assertions!
  "Enable assertions for development."
  []
  (println "Enabling assertions for development...")
  ;; Add assertion configuration here
  )

(defn enable-all-dev-settings!
  "Enable all development-specific settings.
  Note: Instrumentation and reflection warnings are now enabled 
  for all non-release builds in main.clj"
  []
  (enable-verbose-logging!)
  (enable-assertions!)
  ;; Add more development-specific settings here as needed
  (println "Additional development settings enabled."))

;; Automatically enable dev settings when this namespace is loaded
(when (or (System/getProperty "potatoclient.dev")
          (System/getenv "POTATOCLIENT_DEV"))
  (enable-all-dev-settings!))