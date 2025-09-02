(ns potatoclient.init
  "Centralized initialization for the PotatoClient application.
   
   This namespace ensures all core systems are properly initialized
   exactly once, regardless of the entry point (main, dev, nrepl, tests)."
  (:require
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
  {:malli/schema [:=> [:cat] :boolean]}
  []
  @registry-initialized?)

;; ============================================================================
;; Development Mode Detection
;; ============================================================================

(defn development-mode?
  "Check if running in development mode."
  {:malli/schema [:=> [:cat] :boolean]}
  []
  (or (System/getProperty "potatoclient.dev")
      (System/getenv "POTATOCLIENT_DEV")
      (= "dev" (System/getProperty "potatoclient.env"))))

(defn nrepl-mode?
  "Check if running in nREPL mode."
  {:malli/schema [:=> [:cat] :boolean]}
  []
  (or (System/getProperty "potatoclient.nrepl")
      (some? (resolve 'nrepl.server/start-server))))

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
  {:malli/schema [:=> [:cat] :nil]}
  []
  (when-not @initialized?
    (t/log! {:level :info :msg "Starting PotatoClient initialization..."})
    
    ;; 1. Initialize Malli registry first - required by everything else
    (ensure-registry!)
    
    ;; 2. Set up development instrumentation if in dev mode
    (when (development-mode?)
      (t/log! {:level :info :msg "Development mode detected, loading dev tools..."})
      (try
        (require 'potatoclient.dev-instrumentation)
        (when-let [start-fn (resolve 'potatoclient.dev-instrumentation/start!)]
          (start-fn {:report :print}))
        (catch Exception e
          (t/log! {:level :warn :msg (str "Could not load dev instrumentation: " (.getMessage e))}))))
    
    ;; 3. Mark as initialized
    (reset! initialized? true)
    (t/log! {:level :info :msg "PotatoClient initialization complete"}))
  nil)

;; ============================================================================
;; Test-specific Initialization
;; ============================================================================

(defn initialize-for-tests!
  "Initialize the system specifically for testing.
   
   This includes all normal initialization plus test-specific setup."
  {:malli/schema [:=> [:cat] :nil]}
  []
  (initialize!)
  ;; Additional test-specific initialization can go here
  (t/log! {:level :info :msg "Test environment initialized"})
  nil)

;; ============================================================================
;; NREPL-specific Initialization
;; ============================================================================

(defn initialize-for-nrepl!
  "Initialize the system specifically for NREPL.
   
   This includes all normal initialization plus NREPL-specific setup."
  {:malli/schema [:=> [:cat] :nil]}
  []
  (initialize!)
  ;; Set up NREPL-specific features
  (when (nrepl-mode?)
    (t/log! {:level :info :msg "NREPL mode detected, setting up REPL tools..."})
    ;; Load development tools for REPL
    (try
      (require 'potatoclient.dev)
      (catch Exception e
        (t/log! {:level :warn :msg (str "Could not load dev namespace: " (.getMessage e))}))))
  nil)