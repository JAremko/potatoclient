(ns potatoclient.core
  "Core application logic for PotatoClient.
  Handles application initialization and lifecycle management."
  (:require [seesaw.core :as seesaw]
            [potatoclient.state :as state]
            [potatoclient.process :as process]
            [potatoclient.ui.main-frame :as main-frame]
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
   (let [params {:version (get-version)
                 :build-type (get-build-type)}
         frame (main-frame/create-main-frame params)]
     ;; Show frame on next EDT cycle to ensure everything is initialized
     (seesaw/invoke-later
      (seesaw/show! frame))))
  (log-startup!))

