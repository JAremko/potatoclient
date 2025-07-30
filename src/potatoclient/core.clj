(ns potatoclient.core
  "Core application logic for PotatoClient.
  Handles application initialization and lifecycle management."
  (:require [clojure.java.io]
            [clojure.spec.alpha :as s]
            [clojure.string]
            [com.fulcrologic.guardrails.core :refer [>defn >defn- =>]]
            [potatoclient.config :as config]
            [potatoclient.i18n :as i18n]
            [potatoclient.logging :as logging]
            [potatoclient.process :as process]
            [potatoclient.runtime :as runtime]
            [potatoclient.theme :as theme]
            [potatoclient.transit.app-db :as app-db]
            [potatoclient.transit.subprocess-launcher :as launcher]
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
  "Setup JVM shutdown hook to clean up processes."
  []
  [=> nil?]
  (.addShutdownHook
    (Runtime/getRuntime)
    (Thread.
      (fn []
        (try
          (logging/log-info {:msg "Shutting down PotatoClient..."})
          ;; Stop Transit subprocesses first
          (launcher/stop-all-subprocesses)
          ;; Stop video stream processes
          (process/cleanup-all-processes)
          ;; Give processes time to terminate
          (Thread/sleep 100)
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
                  (get-build-type))}))

(>defn- setup-state-monitoring!
  "Set up monitoring for state changes through app-db"
  []
  [=> nil?]
  ;; Monitor connection status
  (add-watch app-db/app-db ::connection-monitor
             (fn [_ _ old-state new-state]
               (let [old-conn (get-in old-state [:app-state :connection :connected?])
                     new-conn (get-in new-state [:app-state :connection :connected?])]
                 (when (not= old-conn new-conn)
                   (if new-conn
                     (logging/log-info {:msg "WebSocket connected to server"})
                     (logging/log-warn {:msg "WebSocket disconnected from server"}))))))

  ;; Monitor critical system state
  (add-watch app-db/app-db ::battery-monitor
             (fn [_ _ old-state new-state]
               (let [old-battery (get-in old-state [:server-state :system :battery-level])
                     new-battery (get-in new-state [:server-state :system :battery-level])]
                 (when (and new-battery
                            (not= old-battery new-battery)
                            (< new-battery 20))
                   (logging/log-warn {:msg "Low battery warning" :level new-battery})))))
  nil)

(>defn -main
  "Application entry point."
  [& _]
  [(s/* any?) => nil?]
  (initialize-application!)
  (seesaw/invoke-later
    ;; Preload theme icons before showing any UI
    (theme/preload-theme-icons!)
    ;; Define callback handler for dialog results
    (letfn [(handle-dialog-result [result]
              (case result
                :connect
                ;; User clicked Connect, proceed with main frame
                (let [params {:version (get-version)
                              :build-type (get-build-type)}
                      frame (main-frame/create-main-frame params)
                      domain (config/get-domain)]
                  (seesaw/show! frame)
                  (log-startup!)
                  ;; Initialize Transit subprocess system
                  (let [ws-url (str "wss://" domain "/ws/ws_cmd")
                        state-url (str "wss://" domain "/ws/ws_state")]
                    ;; Start command subprocess
                    (launcher/start-command-subprocess ws-url domain)
                    ;; Start state subprocess  
                    (launcher/start-state-subprocess state-url domain)
                    ;; Set up state monitoring
                    (setup-state-monitoring!)
                    ;; Set initial UI state from config
                    (when-let [saved-theme (:theme (config/load-config))]
                      (app-db/set-theme! saved-theme))
                    (when-let [saved-locale (:locale (config/load-config))]
                      (app-db/set-locale! saved-locale))
                    (logging/log-info {:msg "Transit subprocess system initialized"
                                       :domain domain})))

                :cancel
                ;; User clicked Cancel, exit application
                (do
                  (logging/log-info {:msg "User cancelled startup dialog, exiting..."})
                  (System/exit 0))

                :reload
                ;; Theme or language changed, show dialog again
                (do
                  (theme/preload-theme-icons!)
                  (startup-dialog/show-startup-dialog nil handle-dialog-result))))]
      ;; Show the initial dialog
      (startup-dialog/show-startup-dialog nil handle-dialog-result))))

