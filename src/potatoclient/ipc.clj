(ns potatoclient.ipc
  "Inter-process communication handling for video streams.
  
  Manages communication between the main process and video stream subprocesses,
  including message routing and stream lifecycle management."
  (:require [clojure.core.async :as async :refer [go-loop <!]]
            [potatoclient.state :as state]
            [potatoclient.process :as process]
            [potatoclient.events.log :as log]
            [potatoclient.events.stream :as stream-events]
            [malli.core :as m]
            [malli.dev :as dev]
            [potatoclient.specs :as specs]))

;; Constants
(def ^:private stream-init-delay-ms 200)

;; Message type dispatch table
(def ^:private message-handlers
  {:response stream-events/handle-response-event
   :log      (fn [_ msg] (log/handle-log-event msg))
   :navigation (fn [_ msg] (stream-events/handle-navigation-event msg))
   :window   (fn [_ msg] (stream-events/handle-window-event msg))})

;; No need to define specs here as they're available in potatoclient.specs

(defn- dispatch-message
  "Dispatch a message to the appropriate handler."
  [stream-key msg]
  (assert (m/validate specs/stream-key stream-key) 
          (str "Invalid stream-key: " stream-key))
  ;; Only validate that message has a type field
  (assert (contains? msg :type)
          (str "Message missing :type field: " msg))
  (if-let [handler (get message-handlers (keyword (:type msg)))]
    (try
      (handler stream-key msg)
      (catch Exception e
        (log/log-error (name stream-key)
                      (str "Error handling " (:type msg) " message")
                      :exception e)))
    (log/log-warning (name stream-key)
                    (str "Unknown message type: " (:type msg)))))


(defn- process-stream-messages
  "Process messages from a stream's output channel."
  [stream-key stream]
  (go-loop []
    (when-let [msg (<! (:output-chan stream))]
      (dispatch-message stream-key msg)
      (recur))))


(defn- build-stream-url
  "Build the WebSocket URL for a stream."
  [endpoint]
  (str "wss://" (state/get-domain) endpoint))


(defn- initialize-stream
  "Initialize a newly started stream."
  [stream]
  (Thread/sleep stream-init-delay-ms)
  (process/send-command stream {:action "show"}))


(defn start-stream
  "Start a stream and set up its message processing.
  
  Creates the subprocess, registers it in state, and begins
  processing its output messages."
  [stream-key endpoint]
  (assert (m/validate specs/stream-key stream-key)
          (str "Invalid stream-key: " stream-key))
  (assert (and (string? endpoint) (not (clojure.string/blank? endpoint)))
          (str "Invalid endpoint: " endpoint))
  (future
    (try
      (let [url (build-stream-url endpoint)
            stream (process/start-stream-process (name stream-key) url)]
        (state/set-stream! stream-key stream)
        (process-stream-messages stream-key stream)
        (initialize-stream stream)
        (log/log-info (name stream-key)
                     (str "Stream started: " endpoint)))
      (catch Exception e
        (log/log-error (name stream-key)
                      "Failed to start stream"
                      :exception e)
        (state/clear-stream! stream-key)))))


(defn stop-stream
  "Stop a stream and clean up resources.
  
  Stops the subprocess and removes it from state."
  [stream-key]
  (assert (m/validate specs/stream-key stream-key)
          (str "Invalid stream-key: " stream-key))
  (future
    (try
      (when-let [stream (state/get-stream stream-key)]
        (process/stop-stream stream)
        (state/clear-stream! stream-key)
        (log/log-info (name stream-key) "Stream stopped by control button"))
      (catch Exception e
        (log/log-error (name stream-key)
                      "Error stopping stream"
                      :exception e)))))


(defn restart-stream
  "Restart a stream by stopping and starting it again."
  [stream-key endpoint]
  (assert (m/validate specs/stream-key stream-key)
          (str "Invalid stream-key: " stream-key))
  (future
    (when (state/get-stream stream-key)
      (stop-stream stream-key)
      (Thread/sleep (* 2 stream-init-delay-ms)))
    (start-stream stream-key endpoint)))


(defn send-command-to-stream
  "Send a command to a specific stream."
  [stream-key command]
  (assert (m/validate specs/stream-key stream-key)
          (str "Invalid stream-key: " stream-key))
  (assert (map? command)
          (str "Invalid command - must be a map: " command))
  (if-let [stream (state/get-stream stream-key)]
    (process/send-command stream command)
    (do
      (log/log-warning (name stream-key)
                      "Cannot send command - stream not connected")
      false)))


(defn broadcast-command
  "Send a command to all active streams."
  [command]
  (assert (map? command)
          (str "Invalid command - must be a map: " command))
  (doseq [[stream-key stream] (state/all-streams)
          :when stream]
    (send-command-to-stream stream-key command)))

