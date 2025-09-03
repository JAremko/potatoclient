(ns user
  "NREPL initialization namespace.
   
   This namespace is automatically loaded when starting an NREPL session.
   It ensures the system is properly initialized for interactive development."
  (:require
    [potatoclient.init :as init]
    [potatoclient.logging :as logging]))

;; Initialize the system for NREPL
(println "\n=== Initializing NREPL Environment ===")
(init/initialize-for-nrepl!)
(println "NREPL initialization complete!")
(println "You can now require any namespace and start developing.")
(println "=======================================\n")

;; Useful development functions
(defn restart-logging!
  "Restart the logging system."
  []
  (logging/shutdown!)
  (logging/init!)
  (println "Logging system restarted"))

(defn reload-registry!
  "Force reload of the Malli registry.
   Useful if you've changed schema definitions."
  []
  (require '[potatoclient.malli.registry :as registry] :reload)
  (require '[potatoclient.specs.cmd.root] :reload)
  (require '[potatoclient.specs.state.root] :reload)
  (init/ensure-registry!)
  (println "Registry reloaded"))

(defn start-instrumentation!
  "Start Malli function instrumentation.
   Options:
   - :report - :print (logs errors) or :throw (throws on validation error) 
   - :width - Pretty printer width (default 120)
   - :print-length - Max collection items to print (default 50)"
  ([] (start-instrumentation! {}))
  ([opts]
   (require '[potatoclient.dev-instrumentation])
   (let [start-fn (resolve 'potatoclient.dev-instrumentation/start!)
         default-opts {:report :throw
                       :width 120
                       :print-length 50
                       :print-level 4}]
     (if start-fn
       (do
         (start-fn (merge default-opts opts))
         (println "Malli instrumentation started!"))
       (println "Error: Could not load instrumentation")))))

(defn stop-instrumentation!
  "Stop Malli function instrumentation."
  []
  (require '[potatoclient.dev-instrumentation])
  (when-let [stop-fn (resolve 'potatoclient.dev-instrumentation/stop!)]
    (stop-fn)
    (println "Malli instrumentation stopped!")))

(defn check-functions!
  "Check all functions against their schemas.
   Returns map of function -> errors for any violations."
  []
  (require '[potatoclient.dev-instrumentation])
  (when-let [check-fn (resolve 'potatoclient.dev-instrumentation/check-all)]
    (let [results (check-fn)]
      (if (empty? results)
        (println "✓ All function schemas are valid!")
        (do
          (println (str "✗ Found " (count results) " function schema violations:"))
          (doseq [[fn-var errors] results]
            (println "  -" fn-var))))
      results)))

(defn instrument-ns!
  "Instrument all functions in a specific namespace."
  [ns-sym]
  (require '[potatoclient.dev-instrumentation])
  (when-let [inst-fn (resolve 'potatoclient.dev-instrumentation/instrument-ns!)]
    (inst-fn ns-sym)
    (println (str "Instrumented namespace: " ns-sym))))

;; Note: Instrumentation is automatically started by init/initialize-for-nrepl!
;; when in development mode, so we don't need to start it again here.
;; The function start-instrumentation! is still available for manual control.

;; Print helpful message
(println "\nUseful REPL functions available:")
(println "  (restart-logging!)       - Restart logging system")
(println "  (reload-registry!)       - Reload Malli schemas")
(println "  (start-instrumentation!) - Start function validation")
(println "  (stop-instrumentation!)  - Stop function validation")
(println "  (check-functions!)       - Check all function schemas")
(println "  (instrument-ns! 'ns)     - Instrument specific namespace")
(println "")