(ns potatoclient.ipc
  "Inter-process communication handling for video streams.
  
  Manages communication between the main process and video stream subprocesses,
  including message routing and stream lifecycle management."
  (:require [clojure.core.async :refer [<! go-loop]]
            [com.fulcrologic.guardrails.malli.core :refer [=> >defn >defn-]]
            [potatoclient.config :as config]
            [potatoclient.events.stream :as stream-events]
            [potatoclient.logging :as logging]
            [potatoclient.process :as process]
            [potatoclient.state :as state])
  (:import (clojure.core.async.impl.channels ManyToManyChannel)))

;; Constants
(def ^:private stream-init-delay-ms 200)

;; Message type dispatch table
(def ^:private message-handlers
  {:response stream-events/handle-response-event
   :log (fn [_ msg] (logging/log-event ::stream-log
                                       {:stream (:streamId msg)
                                        :level (:level msg)
                                        :message (:message msg)}))
   :navigation (fn [_ msg] (stream-events/handle-navigation-event msg))
   :window (fn [_ msg] (stream-events/handle-window-event msg))})

;; No need to define specs here as they're available in potatoclient.specs

(>defn- dispatch-message
  "Dispatch a message to the appropriate handler."
  [stream-key msg]
  [:potatoclient.specs/stream-key map? => [:maybe boolean?]]
  (if-let [handler (get message-handlers (keyword (:type msg)))]
    (try
      (handler stream-key msg)
      (catch Exception e
        (logging/log-error
          {:id ::message-handler-error
           :data {:stream stream-key
                  :msg-type (:type msg)
                  :error (.getMessage e)}
           :msg (str "Error handling " (:type msg) " message")})))
    (logging/log-warn
      {:id ::unknown-message-type
       :data {:stream stream-key
              :msg-type (:type msg)}
       :msg (str "Unknown message type: " (:type msg))})))

(>defn- process-stream-messages
  "Process messages from a stream's output channel."
  [stream-key stream]
  [:potatoclient.specs/stream-key :potatoclient.specs/stream-process-map => [:fn {:error/message "must be a core.async channel"}
                                                                             #(instance? ManyToManyChannel %)]]
  (go-loop []
    (when-let [msg (<! (:output-chan stream))]
      (dispatch-message stream-key msg)
      (recur))))

(>defn- build-stream-url
  "Build the WebSocket URL for a stream."
  [endpoint]
  [string? => string?]
  ;; Get domain from state (which was set from user input)
  (let [domain (state/get-domain)
        ;; Always build URL as wss://domain/endpoint
        stream-url (str "wss://" domain endpoint)]
    (logging/log-debug
      {:id ::build-stream-url
       :data {:domain domain
              :endpoint endpoint
              :stream-url stream-url}
       :msg "Building stream URL"})
    stream-url))

(>defn- initialize-stream
  "Initialize a newly started stream."
  [stream]
  [:potatoclient.specs/stream-process-map => boolean?]
  (Thread/sleep ^long stream-init-delay-ms)
  (process/send-command stream {:action "show"}))

(>defn start-stream
  "Start a stream and set up its message processing.
  
  Creates the subprocess, registers it in state, and begins
  processing its output messages."
  [stream-key endpoint]
  [:potatoclient.specs/stream-key string? => :potatoclient.specs/future-instance]
  (future
    (try
      (let [url (build-stream-url endpoint)
            domain (config/get-domain)
            stream (process/start-stream-process (name stream-key) url domain)]
        (state/set-stream! stream-key stream)
        (process-stream-messages stream-key stream)
        (initialize-stream stream)
        (logging/log-info
          {:id ::stream-started
           :data {:stream stream-key
                  :endpoint endpoint}
           :msg (str "Stream started: " endpoint)}))
      (catch Exception e
        (logging/log-error
          {:id ::stream-start-failed
           :data {:stream stream-key
                  :error (.getMessage e)}
           :msg "Failed to start stream"})
        (state/clear-stream! stream-key)))))

(>defn stop-stream
  "Stop a stream and clean up resources.
  
  Stops the subprocess and removes it from state."
  [stream-key]
  [:potatoclient.specs/stream-key => :potatoclient.specs/future-instance]
  (future
    (try
      (when-let [stream (state/get-stream stream-key)]
        (process/stop-stream stream)
        (state/clear-stream! stream-key)
        (logging/log-info
          {:id ::stream-stopped
           :data {:stream stream-key
                  :source :control-button}
           :msg "Stream stopped by control button"}))
      (catch Exception e
        (logging/log-error
          {:id ::stream-stop-error
           :data {:stream stream-key
                  :error (.getMessage e)}
           :msg "Error stopping stream"})))
    nil))