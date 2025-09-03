(ns repl
  "REPL development utilities.
   
   Loaded by user.clj when running in NREPL mode.
   Provides a clean REPL environment with automatic Malli instrumentation."
  (:require
    [clojure.tools.namespace.repl :as tnr]
    [malli.dev :as dev]
    [malli.dev.pretty :as pretty]
    [malli.instrument :as mi]
    [init-dev]
    [potatoclient.logging :as logging]))

;; Initialize development environment
(def printer-opts (init-dev/initialize!))

;; ============================================================================
;; REPL Utility Functions
;; ============================================================================

(defn restart-logging!
  "Restart the logging system."
  []
  (logging/shutdown!)
  (logging/init!)
  (println "Logging restarted"))

(defn clear-aliases!
  "Clear namespace aliases that might cause refresh issues."
  []
  (doseq [ns-sym (map ns-name (all-ns))]
    (when-let [ns-obj (find-ns ns-sym)]
      (doseq [[alias-sym _] (ns-aliases ns-obj)]
        (ns-unalias ns-sym alias-sym))))
  (println "✓ Cleared all namespace aliases"))

(defn reload!
  "Reload modified namespaces using tools.namespace.
   This properly handles dependencies and unloads old definitions."
  []
  (let [result (tnr/refresh :after 'repl/post-refresh)]
    (if (instance? Throwable result)
      (do
        (println "✗ Refresh failed!")
        (println "Error:" (.getMessage result))
        (println "\nTo see full stacktrace: (clojure.repl/pst)")
        (println "To clear namespace issues: (clear-aliases!)")
        (println "To force reload all: (reload-all!)")
        result)
      result)))

(defn post-refresh
  "Called after successful refresh to re-instrument.
   This runs in the NEW namespace environment after refresh."
  []
  ;; We need to re-require these since we're in a new namespace environment
  (require '[malli.dev :as dev])
  (require '[malli.dev.pretty :as pretty])

  ;; Re-setup pretty printer
  (def printer-opts
    (pretty/-printer {:width 120
                      :print-length 50
                      :print-level 4
                      :print-meta false}))

  ;; dev/start! will automatically:
  ;; - Run mi/collect! on all loaded namespaces
  ;; - Re-instrument all functions
  ;; - Continue watching for changes
  (dev/start! {:report (pretty/reporter printer-opts)})

  (println "✓ Re-instrumented functions after refresh"))

(defn reload-all!
  "Reload ALL namespaces from scratch.
   Use this if reload! fails due to complex dependency changes."
  []
  (println "Reloading ALL namespaces...")
  (let [result (tnr/refresh-all :after 'repl/post-refresh)]
    (if (instance? Throwable result)
      (do
        (println "✗ Refresh-all failed!")
        (println "Error:" (.getMessage result))
        (println "\nTo see full stacktrace: (clojure.repl/pst)")
        result)
      result)))

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
  "Switch to throwing exceptions on validation errors.
   Good for testing - failures are immediate and obvious."
  []
  (dev/stop!)
  (dev/start! {:report (pretty/thrower printer-opts)})
  (println "✓ Switched to THROWING mode - validation errors will throw exceptions"))

(defn set-print-mode!
  "Switch to printing validation errors.
   Good for REPL exploration - see errors without interrupting flow."
  []
  (dev/stop!)
  (dev/start! {:report (pretty/reporter printer-opts)})
  (println "✓ Switched to PRINTING mode - validation errors will be printed"))

(defn instrumented-count
  "Show how many functions are currently instrumented."
  []
  (count (mi/instrumented)))

(defn instrumented?
  "Check if a specific function is instrumented.
   Usage: (instrumented? 'my.ns/my-fn)"
  [fn-sym]
  (contains? (set (mi/instrumented)) fn-sym))

(defn uninstrument!
  "Remove instrumentation from a specific function.
   Useful for debugging instrumentation issues."
  [fn-sym]
  (mi/unstrument! fn-sym)
  (println (format "Unstrumented %s" fn-sym)))

;; ============================================================================
;; Set up namespace refresh dirs
;; ============================================================================

(tnr/set-refresh-dirs "src" "test")

;; ============================================================================
;; Help Message
;; ============================================================================

(println "\n=== Available REPL Functions ===")
(println "  (reload!)           - Reload modified namespaces")
(println "  (reload-all!)       - Reload ALL namespaces from scratch")
(println "  (clear-aliases!)    - Clear namespace aliases (fixes some reload issues)")
(println "  (restart-logging!)  - Restart logging")
(println "  (check-functions!)  - Check all function schemas")
(println "  (set-throw-mode!)   - Throw on validation errors")
(println "  (set-print-mode!)   - Print validation errors")
(println "  (instrumented-count) - Count instrumented functions")
(println "  (instrumented? 'fn) - Check if function is instrumented")
(println "  (uninstrument! 'fn) - Remove instrumentation from function")

(println "\n=== Workflow ===")
(println "  1. Edit your code")
(println "  2. Call (reload!) to refresh changed namespaces")
(println "  3. Functions are auto-instrumented as you define them")
(println "  4. Validation errors are printed (or thrown in throw-mode)")
(println "=======================================\n")