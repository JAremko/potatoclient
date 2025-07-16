(ns potatoclient.core
  "Core application logic for PotatoClient.
  Handles application initialization and lifecycle management."
  (:require [seesaw.core :as seesaw]
            [potatoclient.state :as state]
            [potatoclient.process :as process]
            [potatoclient.ui.main-frame :as main-frame]
            [potatoclient.events.log :as log]
            [potatoclient.log-writer :as log-writer]
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
        (log-writer/stop-logging!)
        (catch Exception e
          nil))))))


(defn- initialize-application!
  "Initialize all application subsystems."
  []
  (config/initialize!)
  (i18n/init!)
  (log-writer/start-logging!)
  (setup-shutdown-hook!))


(defn- log-startup!
  "Log application startup."
  []
  (log/add-log-entry!
   {:time (System/currentTimeMillis)
    :stream "SYSTEM"
    :type "INFO"
    :message (format "Control Center started (v%s %s build)"
                     (get-version)
                     (get-build-type))}))


(defn -main
  "Application entry point."
  [& args]
  (initialize-application!)
  (seesaw/invoke-later
   (let [params {:version (get-version)
                 :build-type (get-build-type)}
         frame (main-frame/create-main-frame params)]
     ;; Show frame on next EDT cycle to ensure everything is initialized
     (seesaw/invoke-later
      (seesaw/show! frame))))
  (log-startup!))

