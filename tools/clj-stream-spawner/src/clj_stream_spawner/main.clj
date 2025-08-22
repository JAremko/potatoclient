(ns clj-stream-spawner.main
  "Main entry point for the Clojure stream spawner."
  (:require 
   [clj-stream-spawner.coordinator :as coordinator]
   [clojure.string :as str]
   [com.fulcrologic.guardrails.malli.core :refer [>defn =>]]
   [taoensso.telemere :as t])
  (:gen-class))

;; ============================================================================
;; CLI Parsing
;; ============================================================================

(defn- print-usage
  "Print usage information."
  []
  (println "
Clojure Stream Spawner - Launch heat and day video streams

Usage: clj -M:run [options]

Options:
  --host, -h <hostname>  Host to connect to (default: sych.local)
  --debug, -d            Enable debug output
  --help                 Show this help message

Examples:
  clj -M:run                    # Use default host (sych.local)
  clj -M:run --host myhost.com  # Use custom host
  clj -M:run --debug            # Show debug information

The spawner will:
1. Create IPC servers for heat and day streams
2. Launch VideoStreamManager processes for each stream
3. Handle all IPC communication with Transit protocol
4. Display events and logs with proper stream prefixes
5. Gracefully shutdown on Ctrl+C
"))

(defn- parse-args
  "Parse command line arguments."
  [args]
  (loop [args args
         opts {:host "sych.local"
               :debug? false}]
    (if (empty? args)
      opts
      (let [[arg & rest] args]
        (case arg
          ("--host" "-h")
          (if (empty? rest)
            (do (println "Error: --host requires a value")
                (print-usage)
                nil)
            (recur (rest rest) (assoc opts :host (first rest))))
          
          ("--debug" "-d")
          (recur rest (assoc opts :debug? true))
          
          "--help"
          nil
          
          (do (println (str "Error: Unknown argument: " arg))
              (print-usage)
              nil))))))

;; ============================================================================
;; Shutdown Handling
;; ============================================================================

(defn- setup-shutdown-hook
  "Set up JVM shutdown hook for graceful cleanup."
  []
  (.addShutdownHook (Runtime/getRuntime)
                    (Thread. (fn []
                               (println "\n========================================")
                               (println "Shutting down stream spawner...")
                               (println "========================================")
                               (coordinator/shutdown)
                               (println "Shutdown complete"))
                             "shutdown-hook")))

;; ============================================================================
;; Main Entry Point
;; ============================================================================

(defn -main
  "Main entry point for the stream spawner."
  [& args]
  (println "========================================")
  (println "Clojure Stream Spawner")
  (println "========================================")
  
  ;; Parse arguments
  (if-let [{:keys [host debug?]} (parse-args args)]
    (do
      ;; Configure logging
      (when debug?
        (t/set-min-level! :debug))
      
      (println (str "Host: " host))
      (println (str "Debug: " debug?))
      (println "")
      
      ;; Initialize coordinator
      (coordinator/initialize host :debug? debug?)
      
      ;; Set up shutdown hook
      (setup-shutdown-hook)
      
      ;; Start streams
      (println "Starting video streams...")
      (let [{:keys [heat day]} (coordinator/start-all-streams)]
        (if (and heat day)
          (do
            (println "")
            (println "Both streams started successfully!")
            (println "Press Ctrl+C to stop all streams")
            (println "")
            (println "========================================")
            (println "")
            
            ;; Wait for shutdown
            (coordinator/wait-for-shutdown))
          (do
            (println "")
            (println "Failed to start one or more streams")
            (println "Check the logs above for errors")
            (coordinator/shutdown)
            (System/exit 1))))
      
      ;; Normal exit
      (System/exit 0))
    
    ;; Args parsing failed
    (System/exit 1)))