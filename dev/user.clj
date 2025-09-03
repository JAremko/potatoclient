(ns user
  "User namespace for NREPL development.
   Initializes the development environment with Malli instrumentation."
  (:require
    ;; Initialize Malli registry first - required for m/=> declarations
    [potatoclient.malli.init]
    [malli.dev :as dev]
    [malli.dev.pretty :as pretty]
    [malli.instrument :as mi]
    [potatoclient.logging :as logging]))

;; Initialize development environment first
(println "\n=== Loading NREPL Development Environment ===")
(require '[init-dev])
(def printer-opts (init-dev/initialize!))

(defn instrumented-count
  "Show how many functions are currently instrumented."
  []
  (require 'malli.core)
  (->> (malli.core/function-schemas)
       vals
       (mapcat vals)
       count))

(defn help
  "Show available REPL commands."
  []
  (println "\n=== Available REPL Functions ===")
  (println "  (reinstrument!)     - Re-collect and instrument all function schemas")
  (println "  (restart-logging!)  - Restart logging")
  (println "  (check-functions!)  - Check all function schemas with generative testing")
  (println "  (set-throw-mode!)   - Throw on validation errors")
  (println "  (set-print-mode!)   - Print validation errors (default)")
  (println "  (instrumented-count) - Count instrumented functions")
  (println "\n=== Current Status ===")
  (println (format "  %d functions instrumented" (instrumented-count)))
  (println "=======================================\n"))

(defn reinstrument!
  "Re-collect schemas from all namespaces and trigger re-instrumentation.
   The dev/start! watcher will automatically re-instrument changed functions."
  []
  (println "Re-collecting function schemas...")
  (mi/collect! {:ns (all-ns)})
  (println "✓ Schemas collected, watcher will auto-reinstrument changed functions"))

(defn restart-logging!
  "Restart the logging system."
  []
  (logging/shutdown!)
  (logging/init!)
  (println "✓ Logging restarted"))

(defn check-functions!
  "Check all functions against their schemas using generative testing."
  []
  (println "Checking function schemas...")
  (let [results (mi/check)]
    (if (empty? results)
      (println "✓ All function schemas are valid!")
      (do
        (println (str "✗ Found " (count results) " violations:"))
        (doseq [[fn-var _] results]
          (println "  -" fn-var))))
    results))

(defn set-throw-mode!
  "Switch to throwing exceptions on validation errors."
  []
  (dev/stop!)
  (dev/start! {:report (pretty/thrower printer-opts)})
  (println "✓ Switched to THROWING mode - validation errors will throw exceptions"))

(defn set-print-mode!
  "Switch to printing validation errors."
  []
  (dev/stop!)
  (dev/start! {:report (pretty/reporter printer-opts)})
  (println "✓ Switched to PRINTING mode - validation errors will be printed"))

;; Show help on startup
(println "\nWelcome to PotatoClient NREPL!")
(println "Development environment is fully initialized.")
(println (format "%d functions instrumented." (instrumented-count)))
(println "Type (help) to see available commands.")