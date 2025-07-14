(ns potatoclient.main
  "Main entry point for PotatoClient - a multi-process video streaming client."
  (:require [potatoclient.core :as core]
            [orchestra.spec.test :as st])
  (:gen-class))

(defn- release-build?
  "Check if this is a release build."
  []
  (or (System/getProperty "potatoclient.release")
      (System/getenv "POTATOCLIENT_RELEASE")))

(defn- enable-instrumentation!
  "Enable Orchestra instrumentation and reflection warnings for non-release builds."
  []
  (when-not (release-build?)
    (println "Enabling instrumentation and reflection warnings...")
    (set! *warn-on-reflection* true)
    (st/instrument)
    (println "Instrumentation enabled.")))

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
    (apply core/-main args)
    (catch Exception e
      (binding [*out* *err*]
        (println "Fatal error during application startup:")
        (println (.getMessage e))
        (.printStackTrace e))
      (System/exit 1))))