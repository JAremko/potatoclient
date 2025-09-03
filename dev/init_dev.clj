(ns init-dev
  "Shared development initialization logic.
   Used by both user.clj (REPL) and dev.clj (make dev)."
  (:require
    [clojure.tools.namespace.find :as find]
    [clojure.java.io :as io]
    [malli.dev :as dev]
    [malli.dev.pretty :as pretty]
    [potatoclient.malli.registry :as registry]
    [potatoclient.logging :as logging]))

(defn initialize!
  "Initialize development environment with instrumentation.
   Returns the pretty printer options for reuse."
  []
  (println "\n=== Initializing Development Environment ===")

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

  ;; 3. Load all project namespaces using tools.namespace
  (println "\nLoading project namespaces...")
  (let [source-dirs ["src"]
        all-ns (mapcat #(find/find-namespaces-in-dir (io/file %)) source-dirs)
        ;; Filter to only potatoclient namespaces
        project-ns (filter #(re-matches #"^potatoclient\..*" (str %)) all-ns)]

    (println (format "Found %d project namespaces" (count project-ns)))

    ;; Load them all, skipping any that fail
    (doseq [ns-sym project-ns]
      (try
        (require ns-sym)
        (catch Exception e
          ;; Some namespaces might have specific runtime requirements
          ;; Only log if it's not a known issue
          (when-not (re-find #"(main|ui\.frames)" (str ns-sym))
            (println (format "  Warning: Could not load %s: %s"
                             ns-sym
                             (or (.getMessage e) (str e)))))))))

  ;; 4. Start Malli dev mode with instrumentation
  (println "\nStarting Malli dev mode...")
  (let [printer-opts (pretty/-printer {:width 120
                                       :print-length 50
                                       :print-level 4
                                       :print-meta false})]
    ;; dev/start! handles everything:
    ;; - Runs mi/collect! for all loaded namespaces
    ;; - Instruments functions
    ;; - Watches registry for changes
    ;; - Auto re-instruments on schema changes
    (dev/start! {:report (pretty/reporter printer-opts)})

    (println "✓ Functions instrumented and watching for changes")
    (println "✓ Validation errors will be printed to console")
    (println "✓ CLJ-Kondo configs generated")
    (println "=======================================\n")

    ;; Return printer opts for reuse
    printer-opts))