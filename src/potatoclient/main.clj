(ns potatoclient.main
  "Main entry point for PotatoClient - a multi-process video streaming client."
  (:require [potatoclient.core :as core])
  (:gen-class))

(defn- enable-dev-mode!
  "Enable development mode settings by loading the dev namespace."
  []
  (when (or (System/getProperty "potatoclient.dev")
            (System/getenv "POTATOCLIENT_DEV"))
    (require 'potatoclient.dev)))

(defn -main
  "Application entry point. Delegates to core namespace for actual initialization."
  [& args]
  (enable-dev-mode!)
  (try
    (apply core/-main args)
    (catch Exception e
      (binding [*out* *err*]
        (println "Fatal error during application startup:")
        (println (.getMessage e))
        (.printStackTrace e))
      (System/exit 1))))