(ns nrepl-server
  "NREPL server initialization with full development environment setup.
   This namespace handles all initialization before starting the NREPL server."
  (:require
    [nrepl.server :as nrepl]
    [cider.nrepl :refer [cider-nrepl-handler]]
    [init-dev]
    [clojure.java.io :as io]))

(defn -main
  "Initialize development environment and start NREPL server."
  [& args]
  ;; First, do all the initialization
  (println "\n=== Initializing NREPL Development Environment ===")
  (init-dev/initialize!)
  
  ;; Now start the NREPL server
  (println "\nStarting NREPL server...")
  (let [port 7888
        server (nrepl/start-server :port port
                                   :bind "localhost"
                                   :handler cider-nrepl-handler)]
    (println (format "nREPL server started on port %d on host localhost - nrepl://localhost:%d" port port))
    (println "\n✓ Development environment fully initialized")
    (println "✓ Connect your editor to localhost:7888")
    (println "✓ All functions are already instrumented!")
    
    ;; Keep the server running
    @(promise)))