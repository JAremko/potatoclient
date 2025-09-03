(ns potatoclient.init
  "Centralized initialization for the PotatoClient application.
   
   This namespace ensures all core systems are properly initialized
   exactly once, regardless of the entry point (main, dev, nrepl, tests)."
  (:require
            [malli.core :as m]
    [potatoclient.malli.registry :as registry]
    [taoensso.telemere :as t]))

;; ============================================================================
;; Registry Initialization
;; ============================================================================

(defonce ^:private ^{:doc "Ensures the Malli registry is initialized exactly once."}
  registry-initialized?
  (delay
    (try
      (t/log! {:level :info :msg "Initializing Malli global registry..."})
      (registry/setup-global-registry!)
      ;; Force loading of all spec namespaces to register their schemas
      ;; This ensures all schemas are available regardless of load order
      (require 'potatoclient.specs.cmd.root)
      (require 'potatoclient.specs.state.root)
      (require 'potatoclient.ui-specs)
      (t/log! {:level :info :msg "Malli global registry initialized successfully"})
      true
      (catch Exception e
        (t/log! {:level :error :msg (str "Failed to initialize Malli registry: " (.getMessage e))})
        (throw e)))))

(defn ensure-registry!
  "Ensure the Malli registry is initialized.
   Safe to call multiple times - initialization only happens once."
  []
  @registry-initialized?)
;; Cannot use m/=> here - registry not initialized yet when this compiles

;; ============================================================================
;; Development Mode Detection
;; ============================================================================

(defn development-mode?
  "Check if running in development mode."
  []
  (or (System/getProperty "potatoclient.dev")
      (System/getenv "POTATOCLIENT_DEV")
      (= "dev" (System/getProperty "potatoclient.env"))))
;; Cannot use m/=> here - registry not initialized yet when this compiles

(defn nrepl-mode?
  "Check if running in nREPL mode."
  []
  (or (System/getProperty "potatoclient.nrepl")
      (some? (resolve 'nrepl.server/start-server))))
;; Cannot use m/=> here - registry not initialized yet when this compiles

;; ============================================================================
;; Complete Initialization
;; ============================================================================

(defonce ^:private ^{:doc "Tracks whether full initialization has been completed."}
  initialized?
  (atom false))

(defn initialize!
  "Initialize all core systems for PotatoClient.
   
   This function is idempotent and can be called from multiple entry points.
   It ensures that all core systems are initialized exactly once.
   
   Entry points that should call this:
   - main/-main (production)
   - dev namespace (development)
   - nREPL startup
   - test harness"
  []
  (when-not @initialized?
    (t/log! {:level :info :msg "Starting PotatoClient initialization..."})

    ;; Initialize Malli registry - required by everything else
    (ensure-registry!)

    ;; Mark as initialized
    (reset! initialized? true)
    (t/log! {:level :info :msg "PotatoClient initialization complete"})

    ;; Note: Development-specific initialization (instrumentation, etc.)
    ;; is now handled in dev/user.clj to avoid circular dependencies
    ;; and provide better REPL workflow
    )
  nil)
;; Cannot use m/=> here - registry not initialized yet when this compiles

;; ============================================================================
;; Test-specific Initialization
;; ============================================================================

(defn initialize-for-tests!
  "Initialize the system specifically for testing.
   
   This includes all normal initialization plus test-specific setup."
  []
  (initialize!)
  ;; Additional test-specific initialization can go here
  (t/log! {:level :info :msg "Test environment initialized"})
  nil)
;; Cannot use m/=> here - registry not initialized yet when this compiles

;; ============================================================================
;; NREPL-specific Initialization
;; ============================================================================

(defn initialize-for-nrepl!
  "Initialize the system specifically for NREPL.
   
   NOTE: This is kept for backward compatibility, but the actual NREPL
   initialization is now handled in dev/user.clj to provide better
   development workflow and avoid circular dependencies."
  []
  (initialize!)
  ;; NREPL-specific setup is now in dev/user.clj
  nil)
;; Cannot use m/=> here - registry not initialized yet when this compiles