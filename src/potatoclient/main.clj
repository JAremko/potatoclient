(ns potatoclient.main
  "Main entry point for PotatoClient."
  (:require [com.fulcrologic.guardrails.malli.core :refer [>defn >defn- =>]]
            [potatoclient.core :as core]
            [potatoclient.logging :as logging]
            [potatoclient.runtime :as runtime])
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

(>defn- install-shutdown-hook!
  "Install JVM shutdown hook for clean termination"
  []
  [=> nil?]
  (.addShutdownHook (Runtime/getRuntime)
                    (Thread. (fn []
                               (try
                                 (println "Shutdown hook triggered - cleaning up...")
                                 ;; Stop logging
                                 (logging/shutdown!)
                                 (Thread/sleep 100)
                                 (catch Exception e
                                   (println "Error in shutdown hook:" (.getMessage e))))))))

(>defn -main
  "Application entry point. Delegates to core namespace for actual initialization."
  [& args]
  [[:* any?] => nil?]
  ;; Note: System properties for UI behavior should be set via JVM flags
  ;; at startup time, not programmatically here. See Makefile and Launch4j
  ;; configuration for proper flag setup.

  (enable-guardrails!)
  (enable-dev-mode!)
  (install-shutdown-hook!)

  (try
    (logging/init!)
    ;; Check for special flags
    (cond
      (some #{"--report-unspecced"} args)
      (generate-unspecced-report!)

      (some #{"--test-guardrails"} args)
      (do
        (println "Running Guardrails validation tests...")
        (require 'potatoclient.guardrails-test)
        (let [test-fn (resolve 'potatoclient.guardrails-test/test-guardrails!)]
          (if test-fn
            (do
              (test-fn)
              (System/exit 0))
            (do
              (println "Error: Could not find test function")
              (System/exit 1)))))

      :else
      (apply core/-main args))
    (catch Exception e
      (binding [*out* *err*]
        (logging/log-error {:msg (str "Fatal error during application startup: " (.getMessage e))})
        (.printStackTrace e))
      (System/exit 1))))