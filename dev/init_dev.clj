(ns init-dev
  "Shared development initialization logic.
   Used by both user.clj (NREPL) and dev.clj (make dev)."
  (:require
    [malli.dev :as dev]
    [malli.dev.pretty :as pretty]
    [malli.instrument :as mi]
    [potatoclient.malli.registry :as registry]
    [potatoclient.logging :as logging]))

(defn initialize!
  "Initialize development environment with instrumentation.
   Returns the pretty printer options for reuse."
  []
  (println "\n=== Initializing Development Environment ===")
  
  ;; Set font antialiasing to suppress Darklaf warnings
  (when-not (System/getProperty "awt.useSystemAAFontSettings")
    (System/setProperty "awt.useSystemAAFontSettings" "lcd_hrgb"))

  ;; 1. Set up Malli registry
  (println "Setting up Malli registry...")
  (registry/setup-global-registry!)
  (require 'potatoclient.specs.cmd.root)
  (require 'potatoclient.specs.state.root)
  (require 'potatoclient.ui-specs)
  (println "✓ Registry initialized")

  ;; 2. Initialize logging
  (println "Initializing logging...")
  (logging/init!)
  (println "✓ Logging initialized")

  ;; 3. Collect schemas from loaded namespaces
  (println "\nCollecting function schemas...")
  (mi/collect! {:ns (all-ns)})
  (println (format "✓ Collected schemas from %d namespaces" (count (all-ns))))

  ;; 4. Start Malli dev mode with instrumentation
  (println "\nStarting Malli dev mode...")
  (let [printer-opts (pretty/-printer {:width 120
                                       :print-length 50
                                       :print-level 4
                                       :print-meta false})]
    ;; dev/start! handles:
    ;; - Instrumenting functions
    ;; - Watching registry for changes
    ;; - Auto re-instruments on schema changes  
    ;; - Emitting clj-kondo configs
    (dev/start! {:report (pretty/reporter printer-opts)})

    (println "✓ Functions instrumented and watching for changes")
    (println "✓ Validation errors will be printed to console")
    (println "✓ CLJ-Kondo configs generated")
    (println "\nTo re-instrument after code changes:")
    (println "  (malli.instrument/collect! {:ns (all-ns)})")
    (println "  The watcher will auto-reinstrument")
    (println "=======================================\n")

    ;; Return printer opts for reuse
    printer-opts))