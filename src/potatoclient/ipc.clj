(ns potatoclient.ipc
  "Inter-process communication handling for video streams.
  
  Manages communication between the main process and video stream subprocesses,
  including message routing and stream lifecycle management."
  (:require [com.fulcrologic.guardrails.malli.core :refer [=> >defn >defn-]]
            [potatoclient.config :as config]
            [potatoclient.events.stream :as stream-events]
            [potatoclient.gestures.handler]
            [potatoclient.logging :as logging]
            [potatoclient.process :as process]
            [potatoclient.state :as state]
            [potatoclient.transit.app-db :as app-db]
            [potatoclient.transit.debug :as transit-debug]))

;; Constants
(def ^:private stream-init-delay-ms 200)

;; Multimethod for message handling based on message type
(defmulti handle-message
  "Handle messages from subprocesses based on their type."
  (fn [msg-type _stream-key _payload] msg-type))

;; Default handler for unknown message types
(defmethod handle-message :default
  [msg-type stream-key payload]
  (transit-debug/log-unknown-message
    (str "IPC handler for stream " stream-key)
    {:msg-type msg-type :payload payload}
    (str "Unknown message type: " msg-type)))

;; Response messages
(defmethod handle-message :response
  [_ stream-key payload]
  (stream-events/handle-response-event stream-key payload))

;; Log messages
(defmethod handle-message :log
  [_ stream-key payload]
  (logging/log-event ::stream-log
                     {:stream (or (:streamId payload)
                                  (:process payload)
                                  stream-key)
                      :level (:level payload)
                      :message (:message payload)}))

;; Error messages
(defmethod handle-message :error
  [_ stream-key payload]
  (logging/log-error
    {:id ::stream-error
     :data {:stream (or (:process payload) stream-key)
            :context (:context payload)
            :error (:error payload)
            :stackTrace (:stackTrace payload)}
     :msg (str "Error from " (or (:process payload) stream-key) ": " (:context payload))}))

;; Metric messages
(defmethod handle-message :metric
  [_ stream-key payload]
  (logging/log-debug
    {:id ::stream-metric
     :data {:stream (or (:process payload) stream-key)
            :name (:name payload)
            :value (:value payload)}
     :msg (str "Metric from " (or (:process payload) stream-key) ": " (:name payload) " = " (:value payload))}))

;; Status messages
(defmethod handle-message :status
  [_ stream-key payload]
  (logging/log-info
    {:id ::stream-status
     :data {:stream (or (:process payload) stream-key)
            :status (:status payload)}
     :msg (str "Status from " (or (:process payload) stream-key) ": " (:status payload))}))

;; Event messages (contains sub-types)
(defmethod handle-message :event
  [_ stream-key payload]
  ;; With automatic keyword conversion, payload keys should already be keywords
  (case (:type payload)
    :navigation (stream-events/handle-navigation-event payload)
    :window (stream-events/handle-window-event payload)
    :gesture (potatoclient.gestures.handler/handle-gesture-event payload)
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
              :event-type (:type payload)
              :payload-keys (keys payload)}
       :msg (str "Unknown event type: " (:type payload))})))

;; Request messages (from video streams that need something)
(defmethod handle-message :request
  [_ stream-key payload]
  (let [action (:action payload)]
    ;; Handle different request types
    (cond
      ;; Special case for forward-command - extract the nested command
      (= action "forward-command")
      (let [nested-command (:command payload)
            subprocess-launcher (requiring-resolve 'potatoclient.transit.subprocess-launcher/send-message)
            transit-core (requiring-resolve 'potatoclient.transit.core/create-message)]
        (when (and subprocess-launcher transit-core nested-command)
          (let [command-msg (@transit-core :command nested-command)]
            (@subprocess-launcher :command command-msg)
            (logging/log-debug
              {:id ::forwarded-command
               :data {:stream stream-key
                      :action (:action nested-command)
                      :original-action action
                      :command command-msg}
               :msg (str "Forwarded nested command " (:action nested-command) " from " stream-key)}))))

      ;; Direct gesture-based commands
      (contains? #{"rotary-set-velocity" "rotary-halt" "rotary-goto-ndc"
                   "cv-start-track-ndc"
                   "heat-camera-next-zoom-table-pos" "heat-camera-prev-zoom-table-pos"
                   "day-camera-next-zoom-table-pos" "day-camera-prev-zoom-table-pos"} action)
      (let [subprocess-launcher (requiring-resolve 'potatoclient.transit.subprocess-launcher/send-message)
            transit-core (requiring-resolve 'potatoclient.transit.core/create-message)]
        (when (and subprocess-launcher transit-core)
          ;; Create a command message with the action and data from the request
          (let [command-msg (@transit-core :command
                                           (merge {:action action}
                                                  (dissoc payload :action :process)))]
            (@subprocess-launcher :command command-msg)
            (logging/log-debug
              {:id ::forwarded-command
               :data {:stream stream-key
                      :action action
                      :command command-msg}
               :msg (str "Forwarded " action " command from " stream-key)}))))

      ;; Unknown request types
      :else
      (logging/log-warn
        {:id ::unknown-request-type
         :data {:stream stream-key
                :request-type action
                :payload payload}
         :msg (str "Unknown request type: " action)}))))

;; No need to define specs here as they're available in potatoclient.specs

(>defn dispatch-message
  "Dispatch a message to the appropriate handler using multimethod.
  This is called directly from the subprocess reader thread."
  [stream-key msg]
  [:potatoclient.ui-specs/stream-key map? => [:maybe boolean?]]
  ;; Messages should have keyword keys thanks to automatic Transit conversion
  (let [msg-type (:msg-type msg)
        payload (:payload msg)]
    ;; Log error if message type is missing
    (when-not msg-type
      (transit-debug/log-unknown-message
        (str "dispatch-message for " stream-key)
        msg
        "Message missing required msg-type field"))
    ;; Log response dispatching for debugging
    (when (= msg-type :response)
      (logging/log-info
        {:id ::dispatch-response
         :data {:stream-key stream-key
                :action (:action payload)
                :payload payload}
         :msg (str "DISPATCH: Handling response for " stream-key
                   " with action: " (:action payload))}))
    (try
      (handle-message msg-type stream-key payload)
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
  [:potatoclient.ui-specs/stream-process-map => boolean?]
  (Thread/sleep ^long stream-init-delay-ms)
  (process/send-command stream {:action "show"}))

(>defn start-stream
  "Start a stream and set up its message processing.
  
  Creates the subprocess, registers it in state, and begins
  processing its output messages."
  [stream-key endpoint]
  [:potatoclient.ui-specs/stream-key string? => :potatoclient.ui-specs/future-instance]
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
  [:potatoclient.ui-specs/stream-key => :potatoclient.ui-specs/future-instance]
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