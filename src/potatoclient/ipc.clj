(ns potatoclient.ipc
  "Inter-process communication handling for video streams.
  
  Manages communication between the main process and video stream subprocesses,
  including message routing and stream lifecycle management."
  (:require [com.fulcrologic.guardrails.malli.core :refer [=> >defn >defn-]]
            [potatoclient.config :as config]
            [potatoclient.events.stream :as stream-events]
            [potatoclient.logging :as logging]
            [potatoclient.process :as process]
            [potatoclient.state :as state]
            [potatoclient.transit.app-db :as app-db])
  (:import (potatoclient.transit MessageType MessageKeys)))

;; Constants
(def ^:private stream-init-delay-ms 200)

;; Multimethod for message handling based on message type
(defmulti handle-message
  "Handle messages from subprocesses based on their type."
  (fn [msg-type stream-key payload] msg-type))

;; Default handler for unknown message types
(defmethod handle-message :default
  [msg-type stream-key payload]
  (logging/log-warn
    {:id ::unknown-message-type
     :data {:stream stream-key
            :msg-type msg-type}
     :msg (str "Unknown message type: " msg-type)}))

;; Response messages
(defmethod handle-message :response
  [_ stream-key payload]
  (stream-events/handle-response-event stream-key payload))

;; Log messages
(defmethod handle-message :log
  [_ stream-key payload]
  (logging/log-event ::stream-log
                     {:stream (or (get payload MessageKeys/STREAM_ID)
                                  (get payload MessageKeys/PROCESS)
                                  stream-key)
                      :level (get payload MessageKeys/LEVEL)
                      :message (get payload MessageKeys/MESSAGE)}))

;; Error messages
(defmethod handle-message :error
  [_ stream-key payload]
  (logging/log-error
    {:id ::stream-error
     :data {:stream (or (get payload MessageKeys/PROCESS) stream-key)
            :context (get payload MessageKeys/CONTEXT)
            :error (get payload MessageKeys/ERROR)
            :stackTrace (get payload MessageKeys/STACK_TRACE)}
     :msg (str "Error from " (or (get payload MessageKeys/PROCESS) stream-key) ": " (get payload MessageKeys/CONTEXT))}))

;; Metric messages
(defmethod handle-message :metric
  [_ stream-key payload]
  (logging/log-debug
    {:id ::stream-metric
     :data {:stream (or (get payload MessageKeys/PROCESS) stream-key)
            :name (get payload MessageKeys/NAME)
            :value (get payload MessageKeys/VALUE)}
     :msg (str "Metric from " (or (get payload MessageKeys/PROCESS) stream-key) ": " (get payload MessageKeys/NAME) " = " (get payload MessageKeys/VALUE))}))

;; Status messages
(defmethod handle-message :status
  [_ stream-key payload]
  (logging/log-info
    {:id ::stream-status
     :data {:stream (or (get payload MessageKeys/PROCESS) stream-key)
            :status (get payload MessageKeys/STATUS)}
     :msg (str "Status from " (or (get payload MessageKeys/PROCESS) stream-key) ": " (get payload MessageKeys/STATUS))}))

;; Event messages (contains sub-types)
(defmethod handle-message :event
  [_ stream-key payload]
  (case (keyword (get payload MessageKeys/TYPE))
    :navigation (stream-events/handle-navigation-event payload)
    :window (stream-events/handle-window-event payload)
    :frame (logging/log-debug
             {:id ::frame-event
              :data {:stream stream-key
                     :payload payload}
              :msg "Frame event received"})
    :error (logging/log-error
             {:id ::video-stream-error
              :data {:stream stream-key
                     :payload payload}
              :msg "Video stream error event"})
    ;; Log unknown event types
    (logging/log-warn
      {:id ::unknown-event-type
       :data {:stream stream-key
              :event-type (get payload MessageKeys/TYPE)}
       :msg (str "Unknown event type: " (get payload MessageKeys/TYPE))})))

;; No need to define specs here as they're available in potatoclient.specs

(>defn dispatch-message
  "Dispatch a message to the appropriate handler using multimethod.
  This is called directly from the subprocess reader thread."
  [stream-key msg]
  [:potatoclient.specs/stream-key map? => [:maybe boolean?]]
  ;; Extract message type and payload from Transit message structure
  (let [msg-type (get msg MessageKeys/MSG_TYPE)
        payload (get msg MessageKeys/PAYLOAD)]
    ;; Log error if message type is missing
    (when-not msg-type
      (logging/log-error
        {:id ::missing-msg-type
         :data {:stream stream-key
                :msg msg}
         :msg "Message missing required msg-type field"}))
    ;; Log response dispatching for debugging
    (when (= msg-type "response")
      (logging/log-info
        {:id ::dispatch-response
         :data {:stream-key stream-key
                :action (get payload "action")
                :payload payload}
         :msg (str "DISPATCH: Handling response for " stream-key 
                  " with action: " (get payload "action"))}))
    (try
      (handle-message (keyword msg-type) stream-key payload)
      true
      (catch Exception e
        (logging/log-error
          {:id ::message-handler-error
           :data {:stream stream-key
                  :msg-type msg-type
                  :error (.getMessage e)}
           :msg (str "Error handling " msg-type " message")})
        false))))

;; Message handler is now set directly in process.clj
;; No more go-loops or channels needed here!)

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
            ;; Pass our dispatch function to the process so it can call us directly
            stream (process/start-stream-process (name stream-key) url domain
                                                 (fn [msg] (dispatch-message stream-key msg)))]
        ;; Store the full stream process map in app-db
        (app-db/set-stream-process! stream-key stream)
        (state/set-stream! stream-key (.pid ^Process (:process stream)) :running)
        (logging/log-info
          {:id ::starting-message-processor
           :data {:stream-key stream-key}
           :msg (str "Message handler registered for " stream-key)})
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
      ;; Get the actual stream process map from app-db
      (when-let [stream (app-db/get-stream-process stream-key)]
        (process/stop-stream stream)
        (state/clear-stream! stream-key)
        (app-db/remove-stream-process! stream-key)
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