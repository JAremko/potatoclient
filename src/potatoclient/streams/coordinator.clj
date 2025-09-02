(ns potatoclient.streams.coordinator
  "Coordinates IPC servers and stream processes."
  (:require
    [potatoclient.ipc.core :as ipc]
    [potatoclient.logging :as logging]
    [potatoclient.streams.config :as config]
    [potatoclient.streams.events :as events]
    [potatoclient.streams.process :as process]
    [potatoclient.streams.state :as state]
    [potatoclient.ipc.transit :as transit]))

;; ============================================================================
;; Constants
;; ============================================================================

(def ^:private ipc-startup-delay-ms
  "Delay in milliseconds to ensure IPC server is ready before starting process."
  100)

(def ^:private process-stop-delay-ms
  "Delay in milliseconds to allow process to disconnect before stopping IPC."
  100)

(def ^:private stream-restart-delay-ms
  "Delay in milliseconds between stopping and restarting a stream."
  500)

(def ^:private batch-operation-delay-ms
  "Delay in milliseconds between stream operations in batch mode."
  500)

;; ============================================================================
;; IPC Management
;; ============================================================================

(defn- create-message-handler
  "Create message handler for stream IPC"
  {:malli/schema [:=> [:cat :keyword] :fn]}
  [stream-type]
  (fn [message]
    (events/handle-message stream-type message)))

(defn- start-ipc-server
  "Start IPC server for a stream"
  {:malli/schema [:=> [:cat :keyword] [:maybe :any]]}
  [stream-type]
  (try
    (logging/log-info {:id :stream/starting-ipc
                       :stream stream-type})
    
    (let [handler (create-message-handler stream-type)
          server (ipc/create-and-register-server stream-type
                                                :on-message handler
                                                :await-binding? true)]
      (state/set-stream-ipc! stream-type server)
      server)
    
    (catch Exception e
      (logging/log-error {:id :stream/ipc-failed
                         :stream stream-type
                         :error (.getMessage e)})
      (state/set-stream-error! stream-type (.getMessage e))
      nil)))

(defn- stop-ipc-server
  "Stop IPC server for a stream"
  {:malli/schema [:=> [:cat :keyword] :nil]}
  [stream-type]
  (when-let [server (state/get-stream-ipc stream-type)]
    (try
      (ipc/stop-server server)
      (state/set-stream-ipc! stream-type nil)
      (catch Exception e
        (logging/log-error {:id :stream/ipc-stop-failed
                           :stream stream-type
                           :error (.getMessage e)}))))
  nil)

;; ============================================================================
;; Stream Lifecycle
;; ============================================================================

(defn start-stream
  "Start a stream (IPC + process)"
  {:malli/schema [:=> [:cat :keyword] :boolean]}
  [stream-type]
  (if (state/stream-running? stream-type)
    (do
      (logging/log-warn {:id :stream/already-running
                        :stream stream-type})
      true)
    (do
      ;; Clear any previous errors
      (state/clear-stream-error! stream-type)
      
      ;; Start IPC server first
      (if-let [ipc-server (start-ipc-server stream-type)]
        (do
          ;; Small delay to ensure IPC is ready
          (Thread/sleep ipc-startup-delay-ms)
          
          ;; Start the process
          (let [host (config/get-stream-host)
                parent-pid (process/get-current-pid)]
            (if (process/spawn-process stream-type host parent-pid)
              true
              (do
                ;; Clean up IPC if process fails
                (stop-ipc-server stream-type)
                false))))
        false))))

(defn stop-stream
  "Stop a stream (process + IPC)"
  {:malli/schema [:=> [:cat :keyword] :nil]}
  [stream-type]
  (when-not (state/stream-stopped? stream-type)
    (logging/log-info {:id :stream/stopping
                       :stream stream-type})
    
    ;; Stop process first
    (process/stop-process stream-type)
    
    ;; Give process time to disconnect
    (Thread/sleep process-stop-delay-ms)
    
    ;; Stop IPC server
    (stop-ipc-server stream-type)
    
    ;; Reset state
    (state/reset-stream-state! stream-type))
  nil)

(defn restart-stream
  "Restart a stream"
  {:malli/schema [:=> [:cat :keyword] :boolean]}
  [stream-type]
  (stop-stream stream-type)
  (Thread/sleep stream-restart-delay-ms)
  (start-stream stream-type))

;; ============================================================================
;; Batch Operations
;; ============================================================================

(defn start-all-streams
  "Start both heat and day streams"
  {:malli/schema [:=> [:cat] :map]}
  []
  {:heat (start-stream :heat)
   :day  (start-stream :day)})

(defn stop-all-streams
  "Stop all streams"
  {:malli/schema [:=> [:cat] :nil]}
  []
  (stop-stream :heat)
  (stop-stream :day)
  nil)

(defn restart-all-streams
  "Restart all streams"
  {:malli/schema [:=> [:cat] :map]}
  []
  (stop-all-streams)
  (Thread/sleep batch-operation-delay-ms)
  (start-all-streams))

;; ============================================================================
;; Control Messages
;; ============================================================================

(defn send-command
  "Send command to a stream"
  {:malli/schema [:=> [:cat :keyword :keyword :map] :boolean]}
  [stream-type action data]
  (if-let [server (state/get-stream-ipc stream-type)]
    (let [message (transit/create-command action data)]
      (ipc/send-message server message))
    (do
      (logging/log-warn {:id :stream/no-ipc-server
                        :stream stream-type
                        :action action})
      false)))

(defn send-close-request
  "Send close request to a stream"
  {:malli/schema [:=> [:cat :keyword] :boolean]}
  [stream-type]
  (send-command stream-type :close-request {:stream-type stream-type}))

;; ============================================================================
;; Initialization
;; ============================================================================

(defn initialize
  "Initialize stream coordinator"
  {:malli/schema [:=> [:cat] :nil]}
  []
  (logging/log-info {:id :stream/coordinator-init})
  (state/reset-all-states!)
  nil)

(defn shutdown
  "Shutdown all streams and cleanup"
  {:malli/schema [:=> [:cat] :nil]}
  []
  (logging/log-info {:id :stream/coordinator-shutdown})
  (stop-all-streams)
  nil)