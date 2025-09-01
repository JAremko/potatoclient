(ns potatoclient.main
  "Main entry point for PotatoClient."
  (:require [clojure.java.io]
            [clojure.string]
            [com.fulcrologic.guardrails.malli.core :refer [>defn >defn- =>]]
            [potatoclient.config :as config]
            [potatoclient.i18n :as i18n]
            [potatoclient.logging :as logging]
            [potatoclient.runtime :as runtime]
            [potatoclient.state :as state]
            [potatoclient.theme :as theme]
            [potatoclient.ui.main-frame :as main-frame]
            [potatoclient.ui.startup-dialog :as startup-dialog]
            [seesaw.core :as seesaw])
  (:gen-class))

(>defn- get-version
  "Get application version from VERSION file."
  []
  [=> string?]
  (try
    (clojure.string/trim (slurp (clojure.java.io/resource "VERSION")))
    (catch Exception _ "dev")))

(>defn- get-build-type
  "Get build type (RELEASE or DEVELOPMENT)."
  []
  [=> string?]
  (if (runtime/release-build?)
    "RELEASE"
    "DEVELOPMENT"))

(>defn- setup-shutdown-hook!
  "Setup JVM shutdown hook."
  []
  [=> nil?]
  (.addShutdownHook
    (Runtime/getRuntime)
    (Thread.
      (fn []
        (try
          (logging/log-info {:msg "Shutting down PotatoClient..."})
          ;; Shutdown logging
          (logging/shutdown!)
          (catch Exception e
            (println "Error during shutdown:" (.getMessage e))))))))

(>defn- initialize-application!
  "Initialize all application subsystems."
  []
  [=> nil?]
  (config/initialize!)
  (i18n/init!)
  (setup-shutdown-hook!))

(>defn- log-startup!
  "Log application startup."
  []
  [=> boolean?]
  (logging/log-info
    {:id ::startup
     :data {:version (get-version)
            :build-type (get-build-type)}
     :msg (format "Control Center started (v%s %s build)"
                  (get-version)
                  (get-build-type))})
  true)

(>defn- show-startup-dialog-recursive
  "Show startup dialog with recursive reload support."
  []
  [=> nil?]
  (startup-dialog/show-startup-dialog
    nil
    (fn [result]
      (case result
        :connect
        ;; User clicked Connect, proceed with main frame
        (let [params {:version (get-version)
                      :build-type (get-build-type)}
              frame (main-frame/create-main-frame params)
              domain (config/get-domain)]
          (seesaw/show! frame)
          (log-startup!)
          ;; Save connection URL to state
          (state/set-connection-url! (str "wss://" domain))
          (state/set-connected! true)
          ;; Set initial UI state from config
          (when-let [saved-theme (:theme (config/load-config))]
            (state/set-theme! saved-theme))
          (when-let [saved-locale (:locale (config/load-config))]
            (state/set-locale! saved-locale))
          (logging/log-info {:msg "Application initialized"
                             :domain domain}))

        :cancel
        ;; User clicked Cancel, exit application
        (do
          (logging/log-info {:msg "User cancelled startup dialog, exiting..."})
          (System/exit 0))

        :reload
        ;; Theme or language changed, show dialog again
        (do
          (theme/preload-theme-icons!)
          (show-startup-dialog-recursive))))))

(>defn- enable-guardrails!
  "Enable Guardrails validation for non-release builds."
  []
  [=> nil?]
  (if (runtime/release-build?)
    (println "Running RELEASE build - Guardrails validation disabled for optimal performance")
    (println "Running DEVELOPMENT build - Guardrails validation enabled")))

(>defn- enable-dev-mode!
  "Enable additional development mode settings by loading the dev namespace."
  []
  [=> nil?]
  (when (or (System/getProperty "potatoclient.dev")
            (System/getenv "POTATOCLIENT_DEV"))
    (require 'potatoclient.dev)))

(>defn- generate-unspecced-report!
  "Generate unspecced functions report and exit."
  []
  [=> nil?]
  (println "Generating unspecced functions report...")
  (require 'potatoclient.reports)
  (let [generate-fn (resolve 'potatoclient.reports/generate-unspecced-functions-report!)]
    (if generate-fn
      (do
        (generate-fn)
        (println "Report generated successfully!")
        (System/exit 0))
      (do
        (println "Error: Could not find report generation function")
        (System/exit 1)))))


(>defn -main
  "Application entry point for PotatoClient."
  [& args]
  [[:* any?] => nil?]
  ;; Note: System properties for UI behavior should be set via JVM flags
  ;; at startup time, not programmatically here. See Makefile and Launch4j
  ;; configuration for proper flag setup.

  (enable-guardrails!)
  (enable-dev-mode!)

  (try
    (logging/init!)

    ;; Check for special flags
    (cond
      (some #{"--report-unspecced"} args)
      (generate-unspecced-report!)

      (some #{"--test-guardrails"} args)
      (do
        (println "Running Guardrails validation tests...")
        (require 'potatoclient.guardrails-test)
        (let [test-fn (resolve 'potatoclient.guardrails-test/test-guardrails!)]
          (if test-fn
            (do
              (test-fn)
              (System/exit 0))
            (do
              (println "Error: Could not find test function")
              (System/exit 1)))))

      :else
      ;; Normal application startup
      (do
        (initialize-application!)
        (seesaw/invoke-later
          ;; Preload theme icons before showing any UI
          (theme/preload-theme-icons!)
          ;; Show the initial dialog
          (show-startup-dialog-recursive))))

    (catch Exception e
      (binding [*out* *err*]
        (logging/log-error {:msg (str "Fatal error during application startup: " (.getMessage e))})
        (.printStackTrace e))
      (System/exit 1))))

;(-main)
