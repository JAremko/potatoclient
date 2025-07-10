(ns potatoclient.ipc
  "Inter-process communication handling for video streams.
  Manages communication between the main process and video stream subprocesses."
  (:require [clojure.core.async :as async]
            [potatoclient.state :as state]
            [potatoclient.process :as process]
            [potatoclient.events.log :as log]
            [potatoclient.events.stream :as stream-events]))

(defn process-stream-output
  "Process messages from a stream's output channel"
  [stream-key stream]
  (async/go-loop []
    (when-let [msg (async/<! (:output-chan stream))]
      (case (:type msg)
        "response" 
        (stream-events/handle-response-event stream-key msg)
        
        "log" 
        (log/handle-log-event msg)
        
        "navigation" 
        (stream-events/handle-navigation-event msg)
        
        "window" 
        (stream-events/handle-window-event msg)
        
        ;; Unknown message type
        nil)
      (recur))))

(defn start-stream
  "Start a stream and set up its message processing"
  [stream-key endpoint]
  (future
    (let [url (str "wss://" (state/get-domain) endpoint)
          stream (process/start-stream-process (name stream-key) url)]
      (state/set-stream! stream-key stream)
      (process-stream-output stream-key stream)
      ;; Give process time to initialize
      (Thread/sleep 200)
      (process/send-command stream {:action "show"}))))

(defn stop-stream
  "Stop a stream and clean up"
  [stream-key]
  (future
    (when-let [stream (state/get-stream stream-key)]
      (process/stop-stream stream)
      (state/clear-stream! stream-key))))