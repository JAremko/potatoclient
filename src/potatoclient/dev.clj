(ns potatoclient.dev
  "Development utilities and settings.
  
  This namespace is automatically loaded when running in development mode
  to enable various debugging and development features.")

(defn enable-reflection-warnings!
  "Enable reflection warnings for all subsequently loaded namespaces."
  []
  (println "Enabling reflection warnings for development...")
  (set! *warn-on-reflection* true))

(defn enable-all-dev-settings!
  "Enable all development settings."
  []
  (enable-reflection-warnings!)
  ;; Add more development settings here as needed
  ;; e.g., verbose logging, assertions, etc.
  (println "Development settings enabled."))

;; Automatically enable dev settings when this namespace is loaded
(when (or (System/getProperty "potatoclient.dev")
          (System/getenv "POTATOCLIENT_DEV"))
  (enable-all-dev-settings!))