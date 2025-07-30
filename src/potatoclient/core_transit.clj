(ns potatoclient.core-transit
  "Updated core application logic using Transit architecture.
  
  This namespace demonstrates how to migrate core.clj to use the
  Transit-based WebSocket system instead of direct protobuf."
  (:require [clojure.java.io]
            [clojure.spec.alpha :as s]
            [clojure.string]
            [com.fulcrologic.guardrails.core :refer [>defn >defn- =>]]
            ;; Transit replacements for cmd.core
            [potatoclient.transit.websocket-manager :as ws-manager]
            [potatoclient.transit.commands :as commands]
            [potatoclient.transit.app-db :as app-db]
            ;; Existing dependencies
            [potatoclient.config :as config]
            [potatoclient.i18n :as i18n]
            [potatoclient.logging :as logging]
            [potatoclient.process :as process]
            [potatoclient.runtime :as runtime]
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
  "Setup JVM shutdown hook to clean up processes."
  []
  [=> nil?]
  (.addShutdownHook
    (Runtime/getRuntime)
    (Thread.
      (fn []
        (try
          ;; Use Transit WebSocket manager instead of cmd
          (ws-manager/stop!)
          (process/cleanup-all-processes)
          (logging/shutdown!)
          (catch Exception _
            nil))))))

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
                     (logging/log-info "WebSocket connected to server")
                     (logging/log-warn "WebSocket disconnected from server"))))))

  ;; Monitor critical system state
  (add-watch app-db/app-db ::battery-monitor
             (fn [_ _ old-state new-state]
               (let [old-battery (get-in old-state [:server-state :system :battery-level])
                     new-battery (get-in new-state [:server-state :system :battery-level])]
                 (when (and new-battery
                            (not= old-battery new-battery)
                            (< new-battery 20))
                   (logging/log-warn "Low battery warning" {:level new-battery})))))

  nil)

(>defn -main
  "Application entry point with Transit architecture."
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

                  ;; NEW: Initialize Transit WebSocket system
                  ;; No callbacks needed - state flows through app-db
                  (ws-manager/init! domain)
                  (setup-state-monitoring!)

                  ;; Set initial UI state from config
                  (app-db/set-theme! (or (config/get-theme) :sol-dark))
                  (app-db/set-locale! (or (config/get-locale) :english))

                  (logging/log-info (str "Transit WebSocket system initialized for " domain)))

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

;; Example of how to use the new command API
(comment
  ;; Send commands using the clean API
  (commands/ping)
  (commands/set-recording true)
  (commands/rotary-goto {:azimuth 45.0 :elevation 30.0})

  ;; Access state from app-db
  (app-db/get-subsystem :gps)
  (app-db/get-server-state)

  ;; Monitor specific subsystems
  (add-watch app-db/app-db ::gps-watcher
             (fn [_ _ old new]
               (when (not= (get-in old [:server-state :gps])
                           (get-in new [:server-state :gps]))
                 (println "GPS updated:" (get-in new [:server-state :gps]))))))