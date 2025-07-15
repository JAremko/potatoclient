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
            [orchestra.core :refer [defn-spec]]
            [clojure.spec.alpha :as s])
  (:gen-class))

(defn-spec ^:private get-version string?
  "Get application version from VERSION file."
  []
  (try
    (clojure.string/trim (slurp (clojure.java.io/resource "VERSION")))
    (catch Exception _ "dev")))

(defn-spec ^:private get-build-type string?
  "Get build type (RELEASE or DEVELOPMENT)."
  []
  (if (or (System/getProperty "potatoclient.release")
          (System/getenv "POTATOCLIENT_RELEASE"))
    "RELEASE"
    "DEVELOPMENT"))

(defn-spec ^:private setup-shutdown-hook! any?
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

(defn-spec ^:private initialize-application! any?
  "Initialize all application subsystems."
  []
  (config/initialize!)
  (i18n/init!)
  (log-writer/start-logging!)
  (setup-shutdown-hook!))

(defn-spec ^:private log-startup! any?
  "Log application startup."
  []
  (log/add-log-entry!
   {:time (System/currentTimeMillis)
    :stream "SYSTEM"
    :type "INFO"
    :message (format "Control Center started (v%s %s build)"
                     (get-version)
                     (get-build-type))}))

(defn-spec -main any?
  "Application entry point."
  [& args (s/* string?)]
  (initialize-application!)
  (seesaw/invoke-later
   (let [params {:version (get-version)
                 :build-type (get-build-type)}
         frame (main-frame/create-main-frame params)]
     ;; Show frame on next EDT cycle to ensure everything is initialized
     (seesaw/invoke-later
      (seesaw/show! frame))))
  (log-startup!))