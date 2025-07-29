(ns potatoclient.core
  "Core application logic for PotatoClient.
  Handles application initialization and lifecycle management."
  (:require [clojure.java.io]
            [clojure.spec.alpha :as s]
            [clojure.string]
            [com.fulcrologic.guardrails.core :refer [>defn >defn- =>]]
            [potatoclient.cmd.core :as cmd]
            [potatoclient.config :as config]
            [potatoclient.i18n :as i18n]
            [potatoclient.logging :as logging]
            [potatoclient.process :as process]
            [potatoclient.runtime :as runtime]
            [potatoclient.state.dispatch :as dispatch]
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
          (cmd/stop-websocket!)
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
                  ;; Initialize WebSocket connections
                  (cmd/init-websocket! 
                    domain
                    (fn [error-msg] 
                      (logging/log-error (str "WebSocket error: " error-msg)))
                    (fn [binary-data]
                      (dispatch/handle-binary-state binary-data)))
                  (logging/log-info (str "WebSocket connections started to " domain)))

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

