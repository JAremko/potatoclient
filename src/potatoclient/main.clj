(ns potatoclient.main
  "Main entry point for PotatoClient."
  (:require
            ;; Initialize Malli registry first - required for m/=> declarations
            [potatoclient.malli.init]
            [malli.core :as m] 
            [clojure.java.io]
            [clojure.string]
            [potatoclient.config :as config]
            [potatoclient.i18n :as i18n]
            [potatoclient.init :as init]
            [potatoclient.logging :as logging]
            [potatoclient.runtime :as runtime]
            [potatoclient.state :as state]
            [potatoclient.state.server.core :as state-server]
            [potatoclient.theme :as theme]
            [potatoclient.ui.frames.initial.core :as initial-frame]
            [potatoclient.ui.frames.connection.core :as connection-frame]
            [potatoclient.ui.main-frame :as main-frame]
            [potatoclient.ui.status-bar.messages :as status-bar]
            [seesaw.core :as seesaw])
  (:gen-class))

;; ============================================================================
;; Constants
;; ============================================================================

(def ^:private state-throttle-ms
  "Throttle interval for state updates in milliseconds."
  100)

(def ^:private state-timeout-ms
  "Timeout for state connections in milliseconds."
  2000)

(def ^:private initial-state-delay-ms
  "Delay in milliseconds to wait for initial state to arrive."
  500)

(defn- get-version
  "Get application version from VERSION file."
  []
  (try
    (clojure.string/trim (slurp (clojure.java.io/resource "VERSION")))
    (catch Exception _ "dev"))) 
 (m/=> get-version [:=> [:cat] :string])

(defn- get-build-type
  "Get build type (RELEASE or DEVELOPMENT)."
  []
  (if (runtime/release-build?)
    "RELEASE"
    "DEVELOPMENT")) 
 (m/=> get-build-type [:=> [:cat] :string])

(defn- setup-shutdown-hook!
  "Setup JVM shutdown hook."
  []
  (.addShutdownHook
    (Runtime/getRuntime)
    (Thread.
      (fn []
        (try
          (logging/log-info {:msg "Shutting down PotatoClient..."})
          ;; Shutdown state ingress
          (when (state-server/initialized?)
            (state-server/shutdown!))
          ;; Shutdown logging
          (logging/shutdown!)
          (catch Exception e
            (println "Error during shutdown:" (.getMessage e)))))))) 
 (m/=> setup-shutdown-hook! [:=> [:cat] :nil])

(defn- initialize-application!
  "Initialize all application subsystems."
  []
  ;; Initialize core systems first (Malli registry, etc.)
  (init/initialize!)
  (config/initialize!)
  (i18n/init!)
  (setup-shutdown-hook!)) 
 (m/=> initialize-application! [:=> [:cat] :nil])

(defn- log-startup!
  "Log application startup."
  []
  (logging/log-info
    {:id ::startup
     :data {:version (get-version)
            :build-type (get-build-type)}
     :msg (format "Control Center started (v%s %s build)"
                  (get-version)
                  (get-build-type))})
  true) 
 (m/=> log-startup! [:=> [:cat] :boolean])

(declare show-initial-frame-recursive)
(declare show-connection-frame)

(defn- show-connection-frame
  "Show connection frame with ping monitoring."
  []
  ;; Clean up any existing seesaw bindings before showing new frame
  (state/cleanup-seesaw-bindings!)
  (connection-frame/show
    nil
    (fn [result]
      (case result
        :connected
        ;; Connection successful, initialize state ingress and proceed with main frame
        (let [domain (config/get-domain)
              ;; Initialize and start state ingress
              _ (logging/log-info {:msg (str "Initializing state ingress for domain: " domain)})
              _ (state-server/initialize! {:domain domain
                                           :throttle-ms state-throttle-ms
                                           :timeout-ms state-timeout-ms})
              _ (state-server/start!)
              ;; Wait briefly for initial state to arrive
              _ (Thread/sleep initial-state-delay-ms)
              ;; Clean up BEFORE creating the main frame to preserve new bindings
              _ (state/cleanup-seesaw-bindings!)
              params {:version (get-version)
                      :build-type (get-build-type)}
              frame (main-frame/create-main-frame params)]
          (seesaw/show! frame)
          (log-startup!)
          ;; Set initial UI state from config
          (when-let [saved-theme (:theme (config/load-config))]
            (state/set-theme! saved-theme))
          (when-let [saved-locale (:locale (config/load-config))]
            (state/set-locale! saved-locale))
          (logging/log-info {:msg "Application initialized with state ingress"
                             :domain domain}))

        :cancel
        ;; User cancelled connection, go back to initial frame
        (do
          ;; Clean up before going back
          (state/cleanup-seesaw-bindings!)
          (show-initial-frame-recursive))

        :reload
        ;; Theme or language changed, show frame again
        (do
          ;; Clean up before reloading
          (state/cleanup-seesaw-bindings!)
          (theme/preload-theme-icons!)
          (show-connection-frame)))))) 
 (m/=> show-connection-frame [:=> [:cat] :nil])

(defn- show-initial-frame-recursive
  "Show initial frame with recursive reload support."
  []
  ;; Clean up any existing seesaw bindings before showing new frame
  (state/cleanup-seesaw-bindings!)
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
          ;; Clean up before reloading
          (state/cleanup-seesaw-bindings!)
          (theme/preload-theme-icons!)
          (show-initial-frame-recursive)))))) 
 (m/=> show-initial-frame-recursive [:=> [:cat] :nil])

(defn- enable-dev-mode!
  "Enable additional development mode settings.
   Note: When running via 'make dev', instrumentation is already
   set up by dev/dev.clj before main is called."
  []
  (when (or (System/getProperty "potatoclient.dev")
            (System/getenv "POTATOCLIENT_DEV"))
    ;; Dev mode is already initialized by dev/dev.clj when using 'make dev'
    ;; This is here for other entry points that might set the property
    (logging/log-info {:msg "Running in development mode"}))) 
 (m/=> enable-dev-mode! [:=> [:cat] :nil])

(defn- generate-unspecced-report!
  "Generate unspecced functions report and exit."
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
 (m/=> generate-unspecced-report! [:=> [:cat] :nil])

(defn -main
  "Application entry point for PotatoClient."
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
 (m/=> -main [:=> [:cat [:* :any]] :nil])

;(-main)