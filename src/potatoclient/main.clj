(ns potatoclient.main
  "Main entry point for PotatoClient - a multi-process video streaming client."
  (:require [potatoclient.core :as core]
            [orchestra.spec.test :as st]
            [orchestra.core :refer [defn-spec]]
            [clojure.spec.alpha :as s])
  (:gen-class))

(defn-spec ^:private release-build? boolean?
  "Check if this is a release build."
  []
  (boolean
   (or (System/getProperty "potatoclient.release")
       (System/getenv "POTATOCLIENT_RELEASE"))))

(defn-spec ^:private enable-instrumentation! any?
  "Enable Orchestra and Malli instrumentation for non-release builds."
  []
  (if (release-build?)
    (println "Running RELEASE build - instrumentation disabled for optimal performance")
    (do
      (println "Running DEVELOPMENT build - enabling instrumentation...")
      ;; Orchestra instrumentation (for legacy code during migration)
      (st/instrument)
      ;; Malli instrumentation
      (try
        (require '[malli.dev :as dev])
        (require '[malli.dev.pretty :as pretty])
        ((resolve 'dev/start!) {:report ((resolve 'pretty/thrower))})
        (println "Orchestra and Malli instrumentation enabled.")
        (catch Exception e
          (println "Warning: Could not enable Malli instrumentation:" (.getMessage e))
          (println "Orchestra instrumentation enabled."))))))

(defn-spec ^:private enable-dev-mode! any?
  "Enable additional development mode settings by loading the dev namespace."
  []
  (when (or (System/getProperty "potatoclient.dev")
            (System/getenv "POTATOCLIENT_DEV"))
    (require 'potatoclient.dev)))

(defn-spec -main any?
  "Application entry point. Delegates to core namespace for actual initialization."
  [& args (s/* string?)]
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