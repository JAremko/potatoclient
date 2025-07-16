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
    (do
      (println "Running DEVELOPMENT build - starting instrumentation...")
      (require 'potatoclient.instrumentation)
      ((resolve 'potatoclient.instrumentation/start!)))))


(defn- enable-dev-mode!
  "Enable additional development mode settings by loading the dev namespace."
  []
  (when (or (System/getProperty "potatoclient.dev")
            (System/getenv "POTATOCLIENT_DEV"))
    (require 'potatoclient.dev)))


(defn- generate-unspecced-report!
  "Generate unspecced functions report and exit."
  []
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
  (enable-instrumentation!)
  (enable-dev-mode!)
  (try
    (logging/init!)
    ;; Check for report generation flag
    (if (some #{"--report-unspecced"} args)
      (generate-unspecced-report!)
      (apply core/-main args))
    (catch Exception e
      (binding [*out* *err*]
        (println "Fatal error during application startup:")
        (println (.getMessage e))
        (.printStackTrace e))
      (System/exit 1))))

