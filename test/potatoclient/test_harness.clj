(ns potatoclient.test-harness
  "Test harness to ensure proto files are compiled and system is initialized.
   Automatically compiles proto classes if they're not available.
   Uses Clojure 1.12's new process features for better control."
  (:require
    [clojure.java.io :as io]
    [clojure.string :as str]
    [clojure.repl.deps :as deps]
    [potatoclient.init :as init])
  (:import
    [java.io File]))

(defn proto-classes-available?
  "Check if proto classes are compiled and on classpath."
  []
  (try
    ;; Try to load core proto classes
    (Class/forName "ser.JonSharedData")
    (Class/forName "cmd.JonSharedCmd")
    true
    (catch ClassNotFoundException _
      false)))

(defn pronto-classes-available?
  "Check if Pronto classes are available."
  []
  (try
    (Class/forName "pronto.ProtoMap")
    true
    (catch ClassNotFoundException _
      false)))

(defn ensure-proto-classes!
  "Ensure proto classes are compiled and available."
  []
  (if (proto-classes-available?)
    (do
      (println "Proto classes available ✓")
      true)
    (do
      (println "")
      (println "================================================")
      (println "ERROR: Proto classes not found!")
      (println "")
      (println "The proto Java classes are required but not compiled.")
      (println "")
      (println "To fix this, run:")
      (println "  cd shared && make proto")
      (println "")
      (println "This will:")
      (println "  1. Generate Java sources from .proto files")
      (println "  2. Compile them to bytecode")
      (println "  3. Make them available for all modules")
      (println "")
      (println "Note: Proto generation only needs to be done once,")
      (println "      or when .proto files change.")
      (println "================================================")
      (throw (ex-info "Proto classes not compiled!"
                      {:missing-classes ["ser.JonSharedData" "cmd.JonSharedCmd"]
                       :solution "Run 'make proto' in the shared directory"})))))

(defn ensure-pronto!
  "Ensure Pronto is available on classpath.
   Uses Clojure 1.12's add-lib if needed."
  []
  (when-not (pronto-classes-available?)
    (println "Adding Pronto to classpath...")
    (try
      ;; In Clojure 1.12, we can add libs dynamically
      (deps/add-lib 'com.appsflyer/pronto
                    {:git/url "https://github.com/JAremko/pronto.git"
                     :git/sha "0fb034bc9c943d6a04177b23eb97436f9ca817f7"})
      (println "Pronto added successfully")
      (catch Exception e
        (println "Note: Could not add Pronto dynamically. Ensure it's in deps.edn")
        (println (.getMessage e))))))

(defn initialize!
  "Initialize the test environment.
   Ensures all prerequisites are met before running tests."
  []
  (println "\n=== Initializing Test Harness ===")

  ;; Ensure Pronto is available
  (ensure-pronto!)

  ;; Ensure proto classes are compiled
  (ensure-proto-classes!)

  ;; Initialize core systems (Malli registry, etc.)
  (init/initialize-for-tests!)

  ;; Verify everything is ready
  (let [proto-ok? (proto-classes-available?)
        pronto-ok? (pronto-classes-available?)]

    (println "\nTest Harness Status:")
    (println (format "  Proto classes: %s" (if proto-ok? "✓" "✗")))
    (println (format "  Pronto library: %s" (if pronto-ok? "✓" "✗")))
    (println (format "  Malli registry: ✓"))

    (when-not (and proto-ok? pronto-ok?)
      (throw (ex-info "Test harness initialization failed"
                      {:proto proto-ok?
                       :pronto pronto-ok?})))

    (println "  Ready for testing!")
    (println "=================================\n")
    true))

;; Auto-initialize when namespace is loaded
;; This ensures all test namespaces that require this will have the system ready
(defonce initialized?
  (try
    (initialize!)
    (catch Exception e
      (println "Warning: Test harness initialization failed")
      (println (.getMessage e))
      false)))