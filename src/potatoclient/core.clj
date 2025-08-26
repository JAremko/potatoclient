(ns potatoclient.core
  "Core application logic for PotatoClient.
  Handles application initialization and lifecycle management."
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

(>defn -main
  "Application entry point."
  [& _]
  [[:* any?] => nil?]
  (initialize-application!)
  (seesaw/invoke-later
    ;; Preload theme icons before showing any UI
    (theme/preload-theme-icons!)
    ;; Show the initial dialog
    (show-startup-dialog-recursive)))