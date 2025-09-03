(ns dev
  "Development runner - initializes instrumentation and starts the app.
   Used by 'make dev' to run the application with full development features."
  (:require
    ;; Initialize Malli registry first - required for m/=> declarations
    [potatoclient.malli.init]
    [init-dev]
    [potatoclient.main :as main]))

(defn -main
  "Development entry point - initialize and start the app."
  [& args]
  ;; Initialize development environment
  (init-dev/initialize!)

  ;; Start the main application
  (println "Starting PotatoClient in development mode...")
  (apply main/-main args))