(ns potatoclient.core
  "Core application logic for PotatoClient.
  Handles application initialization and lifecycle management."
  (:require [seesaw.core :as seesaw]
            [potatoclient.state :as state]
            [potatoclient.process :as process]
            [potatoclient.ui.main-frame :as main-frame]
            [potatoclient.ui.startup-dialog :as startup-dialog]
            [potatoclient.logging :as logging]
            [potatoclient.config :as config]
            [potatoclient.i18n :as i18n]
            [potatoclient.theme :as theme]
            [potatoclient.runtime :as runtime]
            [malli.core :as m]
            [potatoclient.specs :as specs])
  (:gen-class))

(defn- get-version
  "Get application version from VERSION file."
  []
  (try
    (clojure.string/trim (slurp (clojure.java.io/resource "VERSION")))
    (catch Exception _ "dev")))


(defn- get-build-type
  "Get build type (RELEASE or DEVELOPMENT)."
  []
  (if (runtime/release-build?)
    "RELEASE"
    "DEVELOPMENT"))


(defn- setup-shutdown-hook!
  "Setup JVM shutdown hook to clean up processes."
  []
  (.addShutdownHook
   (Runtime/getRuntime)
   (Thread.
    (fn []
      (try
        (process/cleanup-all-processes)
        (logging/shutdown!)
        (catch Exception e
          nil))))))


(defn- initialize-application!
  "Initialize all application subsystems."
  []
  (config/initialize!)
  (i18n/init!)
  (setup-shutdown-hook!))


(defn- log-startup!
  "Log application startup."
  []
  (logging/log-info
   {:id ::startup
    :data {:version (get-version)
           :build-type (get-build-type)}
    :msg (format "Control Center started (v%s %s build)"
                 (get-version)
                 (get-build-type))}))


(defn -main
  "Application entry point."
  [& args]
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
                     frame (main-frame/create-main-frame params)]
                 (seesaw/show! frame)
                 (log-startup!))
               
               :cancel
               ;; User clicked Cancel, exit application
               (do
                 (logging/log-info "User cancelled startup dialog, exiting...")
                 (System/exit 0))
               
               :reload
               ;; Theme or language changed, show dialog again
               (do
                 (theme/preload-theme-icons!)
                 (startup-dialog/show-startup-dialog nil handle-dialog-result))))]
     ;; Show the initial dialog
     (startup-dialog/show-startup-dialog nil handle-dialog-result))))

