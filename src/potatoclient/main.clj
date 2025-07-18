(ns potatoclient.main
  "Main entry point for PotatoClient - a multi-process video streaming client."
  (:require [com.fulcrologic.guardrails.malli.core :refer [>defn >defn- => ?]]
            [potatoclient.core :as core]
            [potatoclient.runtime :as runtime]
            [potatoclient.logging :as logging]
            [clojure.java.io :as io]
            [malli.core :as m]
            [potatoclient.specs :as specs])
  (:gen-class))

(>defn- enable-guardrails!
        "Enable Guardrails validation for non-release builds."
        []
        [=> nil?]
        (if (runtime/release-build?)
          (println "Running RELEASE build - Guardrails validation disabled for optimal performance")
          (println "Running DEVELOPMENT build - Guardrails validation enabled")))

(>defn- enable-dev-mode!
        "Enable additional development mode settings by loading the dev namespace."
        []
        [=> nil?]
        (when (or (System/getProperty "potatoclient.dev")
                  (System/getenv "POTATOCLIENT_DEV"))
          (require 'potatoclient.dev)))

(>defn- generate-unspecced-report!
        "Generate unspecced functions report and exit."
        []
        [=> nil?]
        (println "Generating unspecced functions report...")
        (require 'potatoclient.reports)
        (let [generate-fn (resolve 'potatoclient.reports/generate-unspecced-functions-report!)]
          (if generate-fn
            (do
              (generate-fn)
              (println "Report generated successfully!")
              (System/exit 0))
            (do
              (println "Error: Could not find report generation function")
              (System/exit 1)))))

(defn -main
  "Application entry point. Delegates to core namespace for actual initialization."
  [& args]
  ;; Note: System properties for UI behavior should be set via JVM flags
  ;; at startup time, not programmatically here. See Makefile and Launch4j
  ;; configuration for proper flag setup.

       (enable-guardrails!)
       (enable-dev-mode!)
       (try
         (logging/init!)
    ;; Check for report generation flag
         (if (some #{"--report-unspecced"} args)
           (generate-unspecced-report!)
           (apply core/-main args))
         (catch Exception e
           (binding [*out* *err*]
             (logging/log-error (str "Fatal error during application startup: " (.getMessage e)))
             (.printStackTrace e))
           (System/exit 1))))

