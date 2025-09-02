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

;; Print helpful message
(println "Useful REPL functions available:")
(println "  (restart-logging!)  - Restart logging system")
(println "  (reload-registry!)  - Reload Malli schemas")
(println "")