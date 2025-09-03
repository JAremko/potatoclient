(ns user
  "User namespace for NREPL development.
   Initializes the development environment and loads REPL utilities.")

;; Initialize development environment first
(println "\n=== Loading NREPL Development Environment ===")
(require '[init-dev])
(def printer-opts (init-dev/initialize!))

;; Now load REPL utilities
(require '[repl :refer [reload! reload-all! clear-aliases! 
                        restart-logging! check-functions!
                        set-throw-mode! set-print-mode!
                        instrumented-count instrumented? uninstrument!]])

(defn help
  "Show available REPL commands."
  []
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
  (println "\n=== Current Status ===")
  (println (format "  %d functions instrumented" (instrumented-count)))
  (println "=======================================\n"))

;; Show help on startup
(println "\nWelcome to PotatoClient NREPL!")
(println "Development environment is fully initialized.")
(println (format "%d functions instrumented." (instrumented-count)))
(println "Type (help) to see available commands.")