(ns potatoclient.main
  "Main entry point for PotatoClient - a multi-process video streaming client."
  (:require [potatoclient.core :as core]
            [potatoclient.runtime :as runtime]
            [potatoclient.logging :as logging]
            [clojure.java.io :as io]
            [malli.core :as m]
            [potatoclient.specs :as specs])
  (:gen-class))



(defn- enable-instrumentation!
  "Enable Malli instrumentation for non-release builds."
  []
  (if (runtime/release-build?)
    (println "Running RELEASE build - instrumentation disabled for optimal performance")
    (println "Running DEVELOPMENT build - instrumentation available via (potatoclient.instrumentation/start!)")))


(defn- enable-dev-mode!
  "Enable additional development mode settings by loading the dev namespace."
  []
  (when (or (System/getProperty "potatoclient.dev")
            (System/getenv "POTATOCLIENT_DEV"))
    (require 'potatoclient.dev)))


(defn -main
  "Application entry point. Delegates to core namespace for actual initialization."
  [& args]
  (enable-instrumentation!)
  (enable-dev-mode!)
  (try
    (logging/init!)
    (apply core/-main args)
    (catch Exception e
      (binding [*out* *err*]
        (println "Fatal error during application startup:")
        (println (.getMessage e))
        (.printStackTrace e))
      (System/exit 1))))

