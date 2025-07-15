(ns potatoclient.main
  "Main entry point for PotatoClient - a multi-process video streaming client."
  (:require [potatoclient.core :as core]
            [malli.core :as m]
            [potatoclient.specs :as specs])
  (:gen-class))

(defn ^:private release-build?
  "Check if this is a release build."
  []
  (boolean
   (or (System/getProperty "potatoclient.release")
       (System/getenv "POTATOCLIENT_RELEASE"))))


(defn ^:private enable-instrumentation!
  "Enable Malli instrumentation for non-release builds."
  []
  (if (release-build?)
    (println "Running RELEASE build - instrumentation disabled for optimal performance")
    (do
      (println "Running DEVELOPMENT build - enabling instrumentation...")
      ;; Load and start Malli instrumentation
      (try
        (require 'potatoclient.instrumentation)
        ((resolve 'potatoclient.instrumentation/start!))
        (catch Exception e
          (println "Warning: Could not enable Malli instrumentation:" (.getMessage e)))))))


(defn ^:private enable-dev-mode!
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

