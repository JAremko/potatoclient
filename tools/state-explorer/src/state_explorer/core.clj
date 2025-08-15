(ns state-explorer.core
  "Main entry point for state-explorer tool"
  (:require [state-explorer.websocket-simple :as ws]
            [state-explorer.capture :as capture]
            [state-explorer.pronto-handler :as proto]
            [state-explorer.cli :as cli]
            [clojure.tools.logging :as log]
            [clojure.string :as str])
  (:gen-class))

(defn configure-logging
  "Configure logging based on verbosity"
  [verbose?]
  (if verbose?
    (System/setProperty "org.slf4j.simpleLogger.defaultLogLevel" "debug")
    (System/setProperty "org.slf4j.simpleLogger.defaultLogLevel" "info")))

(defn run-capture
  "Run the capture session"
  [{:keys [domain count output-dir verbose] :as options}]
  (configure-logging verbose)
  
  (let [url (cli/build-ws-url options)
        complete-promise (promise)
        captures (atom [])
        counter (atom 0)]
    
    (println (str "State-Explorer v1.0.0"))
    (println (str "=" (apply str (repeat 50 "="))))
    (println (format "Connecting to: %s" url))
    (println (format "Capture count: %d" count))
    (println (format "Output directory: %s" output-dir))
    (println (str "=" (apply str (repeat 50 "="))))
    (println)
    
    ;; Create capture handler with EDN conversion
    (let [handler (proto/create-converting-handler
                   {:output-dir output-dir
                    :max-count count
                    :on-payload (fn [{:keys [timestamp files]}]
                                 (let [current (swap! counter inc)]
                                   (cli/print-progress current count)
                                   (when (= current count)
                                     (println)))) ; New line after progress bar
                    :on-complete (fn [capture-list]
                                  (reset! captures capture-list)
                                  (deliver complete-promise :done))})]
      
      (try
        ;; Connect to WebSocket
        (let [connection (ws/connect
                         {:url url
                          :on-message handler
                          :on-connect (fn []
                                       (println "Connected! Waiting for messages...")
                                       (println))
                          :on-close (fn [status reason]
                                     (println (format "\nConnection closed: %s %s" status reason))
                                     (deliver complete-promise :closed))
                          :on-error (fn [error]
                                     (log/error error "WebSocket error")
                                     (deliver complete-promise :error))})]
          
          ;; Wait for completion
          (let [result (deref complete-promise (* 300 1000) :timeout)] ; 5 minute timeout
            (println)
            
            ;; Close connection
            (ws/close connection)
            
            ;; Print summary
            (when (seq @captures)
              (let [stats (capture/get-capture-stats @captures)]
                (println)
                (println (cli/format-capture-summary @captures stats))))
            
            ;; Handle different completion reasons
            (case result
              :done (do (println "\nCapture completed successfully!")
                       (System/exit 0))
              :timeout (do (println "\nCapture timed out!")
                          (System/exit 1))
              :error (do (println "\nCapture failed due to error!")
                        (System/exit 1))
              :closed (do (println "\nConnection closed before capture completed!")
                         (System/exit 1)))))
        
        (catch Exception e
          (println (format "\nFatal error: %s" (.getMessage e)))
          (when verbose
            (.printStackTrace e))
          (System/exit 1))))))

(defn -main
  "Main entry point"
  [& args]
  (let [{:keys [options exit-message ok?]} (cli/validate-args args)]
    (if exit-message
      (do
        (println exit-message)
        (System/exit (if ok? 0 1)))
      (run-capture options))))