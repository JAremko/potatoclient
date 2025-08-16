(ns cmd-explorer.test-harness
  "Test harness to ensure proto classes from shared module are available.
   Proto classes are provided by the potatoclient/shared dependency."
  (:require
   [cmd-explorer.registry :as registry]
   [cmd-explorer.specs.oneof-payload :as oneof]
   [clojure.java.io :as io]))

(defn proto-classes-available?
  "Check if protobuf classes are available from shared module."
  []
  (try
    ;; Try to load core proto classes from shared module
    (Class/forName "ser.JonSharedData")
    (Class/forName "cmd.JonSharedCmd")
    true
    (catch ClassNotFoundException _
      false)))

(defn pronto-classes-available?
  "Check if Pronto Java classes are available."
  []
  (try
    ;; Try to load a core Pronto class
    (Class/forName "pronto.ProtoMap")
    true
    (catch ClassNotFoundException _
      false)))

(defn initialize!
  "Initialize the test harness.
   Ensures proto classes from shared module are available."
  []
  ;; Check if proto classes are available
  (if-not (proto-classes-available?)
    (do
      (println "")
      (println "================================================")
      (println "ERROR: Proto classes not found!")
      (println "")
      (println "The proto classes from shared module are required.")
      (println "")
      (println "To fix this, run in the shared directory:")
      (println "  cd ../../shared && make proto")
      (println "")
      (println "This will generate and compile proto classes")
      (println "that are shared across all tools.")
      (println "================================================")
      (throw (ex-info "Proto classes not available from shared module" 
                      {:hint "Run 'make proto' in shared directory"})))
    (println "  ✓ Proto classes available from shared module"))
  
  ;; Check if Pronto is available
  (if-not (pronto-classes-available?)
    (println "  ⚠ Pronto classes not available (may be loaded dynamically)")
    (println "  ✓ Pronto classes available"))
  
  ;; Initialize registry
  (println "Initializing registry...")
  (registry/setup-global-registry!)
  
  ;; Success message
  (println "Cmd-Explorer test harness initialized:")
  (println "  ✓ Ready for testing"))

;; Auto-initialize when namespace is loaded
;; This ensures all test namespaces that require this will have the system ready
(defonce ^:private initialized? 
  (do 
    (try
      (initialize!)
      true
      (catch Exception e
        (println "Warning: Test harness initialization failed")
        (println (.getMessage e))
        false))))