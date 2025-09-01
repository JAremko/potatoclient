(ns potatoclient.main
  "Main entry point for PotatoClient."
  (:require [clojure.java.io]
            [clojure.string]
            [potatoclient.config :as config]
            [potatoclient.i18n :as i18n]
            [potatoclient.logging :as logging]
            [potatoclient.runtime :as runtime]
            [potatoclient.state :as state]
            [potatoclient.theme :as theme]
            [potatoclient.ui.frames.initial.core :as initial-frame]
            [potatoclient.ui.frames.connection.core :as connection-frame]
            [potatoclient.ui.frames.main.core :as main-frame]
            [seesaw.core :as seesaw])
  (:gen-class))

(defn- get-version
  "Get application version from VERSION file."
  {:malli/schema [:=> [:cat] :string]}
  []
  (try
    (clojure.string/trim (slurp (clojure.java.io/resource "VERSION")))
    (catch Exception _ "dev")))

(defn- get-build-type
  "Get build type (RELEASE or DEVELOPMENT)."
  {:malli/schema [:=> [:cat] :string]}
  []
  (if (runtime/release-build?)
    "RELEASE"
    "DEVELOPMENT"))

(defn- setup-shutdown-hook!
  "Setup JVM shutdown hook."
  {:malli/schema [:=> [:cat] :nil]}
  []
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

(defn- initialize-application!
  "Initialize all application subsystems."
  {:malli/schema [:=> [:cat] :nil]}
  []
  (config/initialize!)
  (i18n/init!)
  (setup-shutdown-hook!))

(defn- log-startup!
  "Log application startup."
  {:malli/schema [:=> [:cat] :boolean]}
  []
  (logging/log-info
    {:id ::startup
     :data {:version (get-version)
            :build-type (get-build-type)}
     :msg (format "Control Center started (v%s %s build)"
                  (get-version)
                  (get-build-type))})
  true)

(declare show-initial-frame-recursive)
(declare show-connection-frame)

(defn- show-connection-frame
  "Show connection frame with ping monitoring."
  {:malli/schema [:=> [:cat] :nil]}
  []
  (connection-frame/show
    nil
    (fn [result]
      (case result
        :connected
        ;; Connection successful, proceed with main frame
        (let [params {:version (get-version)
                      :build-type (get-build-type)}
              frame (main-frame/create params)
              domain (config/get-domain)]
          (seesaw/show! frame)
          (log-startup!)
          ;; Set initial UI state from config
          (when-let [saved-theme (:theme (config/load-config))]
            (state/set-theme! saved-theme))
          (when-let [saved-locale (:locale (config/load-config))]
            (state/set-locale! saved-locale))
          (logging/log-info {:msg "Application initialized"
                             :domain domain}))
        
        :cancel
        ;; User cancelled connection, go back to initial frame
        (show-initial-frame-recursive)
        
        :reload
        ;; Theme or language changed, show frame again
        (do
          (theme/preload-theme-icons!)
          (show-connection-frame))))))

(defn- show-initial-frame-recursive
  "Show initial frame with recursive reload support."
  {:malli/schema [:=> [:cat] :nil]}
  []
  (initial-frame/show
    nil
    (fn [result]
      (case result
        :connect
        ;; User clicked Connect, proceed to connection frame
        (show-connection-frame)

        :cancel
        ;; User clicked Cancel, exit application
        (do
          (logging/log-info {:msg "User cancelled initial frame, exiting..."})
          (System/exit 0))

        :reload
        ;; Theme or language changed, show dialog again
        (do
          (theme/preload-theme-icons!)
          (show-initial-frame-recursive))))))

(defn- enable-dev-mode!
  "Enable additional development mode settings by loading the dev namespace."
  {:malli/schema [:=> [:cat] :nil]}
  []
  (when (or (System/getProperty "potatoclient.dev")
            (System/getenv "POTATOCLIENT_DEV"))
    (require 'potatoclient.dev)))

(defn- generate-unspecced-report!
  "Generate unspecced functions report and exit."
  {:malli/schema [:=> [:cat] :nil]}
  []
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

(defn -main
  "Application entry point for PotatoClient."
  {:malli/schema [:=> [:cat [:* :any]] :nil]}
  [& args]
  ;; Note: System properties for UI behavior should be set via JVM flags
  ;; at startup time, not programmatically here. See Makefile and Launch4j
  ;; configuration for proper flag setup.

  (enable-dev-mode!)

  (try
    (logging/init!)

    ;; Check for special flags
    (cond

      :else
      ;; Normal application startup
      (do
        (initialize-application!)
        (seesaw/invoke-later
          ;; Preload theme icons before showing any UI
          (theme/preload-theme-icons!)
          ;; Show the initial frame
          (show-initial-frame-recursive))))

    (catch Exception e
      (binding [*out* *err*]
        (logging/log-error {:msg (str "Fatal error during application startup: " (.getMessage e))})
        (.printStackTrace e))
      (System/exit 1))))

;(-main)
