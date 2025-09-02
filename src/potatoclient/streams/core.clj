(ns potatoclient.streams.core
  "Public API for stream management."
  (:require
    [potatoclient.logging :as logging]
    [potatoclient.streams.config :as config]
    [potatoclient.streams.coordinator :as coordinator]
    [potatoclient.streams.state :as state]))

;; ============================================================================
;; Stream Control
;; ============================================================================

(defn start-stream
  "Start a video stream
  
  Parameters:
    stream-type - :heat or :day
  
  Returns true if successful, false otherwise."
  {:malli/schema [:=> [:cat :keyword] :boolean]}
  [stream-type]
  (if (config/valid-stream-type? stream-type)
    (coordinator/start-stream stream-type)
    (do
      (logging/log-error {:id :stream/invalid-type
                         :stream stream-type})
      false)))

(defn stop-stream
  "Stop a video stream
  
  Parameters:
    stream-type - :heat or :day"
  {:malli/schema [:=> [:cat :keyword] :nil]}
  [stream-type]
  (when (config/valid-stream-type? stream-type)
    (coordinator/stop-stream stream-type))
  nil)

(defn restart-stream
  "Restart a video stream
  
  Parameters:
    stream-type - :heat or :day
  
  Returns true if successful, false otherwise."
  {:malli/schema [:=> [:cat :keyword] :boolean]}
  [stream-type]
  (if (config/valid-stream-type? stream-type)
    (coordinator/restart-stream stream-type)
    false))

(defn toggle-stream
  "Toggle a stream on/off based on current state
  
  Parameters:
    stream-type - :heat or :day
  
  Returns new state (:running or :stopped)"
  {:malli/schema [:=> [:cat :keyword] [:maybe :keyword]]}
  [stream-type]
  (if (state/stream-running? stream-type)
    (do
      (stop-stream stream-type)
      :stopped)
    (do
      (start-stream stream-type)
      :running)))

;; ============================================================================
;; Batch Operations
;; ============================================================================

(defn start-all-streams
  "Start both heat and day streams
  
  Returns map with results {:heat boolean :day boolean}"
  {:malli/schema [:=> [:cat] :map]}
  []
  (coordinator/start-all-streams))

(defn stop-all-streams
  "Stop all running streams"
  {:malli/schema [:=> [:cat] :nil]}
  []
  (coordinator/stop-all-streams))

(defn restart-all-streams
  "Restart all streams
  
  Returns map with results {:heat boolean :day boolean}"
  {:malli/schema [:=> [:cat] :map]}
  []
  (coordinator/restart-all-streams))

;; ============================================================================
;; Status Queries
;; ============================================================================

(defn get-stream-status
  "Get status of a stream
  
  Parameters:
    stream-type - :heat or :day
  
  Returns :stopped, :starting, :running, :stopping, or :error"
  {:malli/schema [:=> [:cat :keyword] [:maybe :keyword]]}
  [stream-type]
  (state/get-stream-status stream-type))

(defn stream-running?
  "Check if a stream is running
  
  Parameters:
    stream-type - :heat or :day"
  {:malli/schema [:=> [:cat :keyword] :boolean]}
  [stream-type]
  (state/stream-running? stream-type))

(defn any-stream-running?
  "Check if any stream is running"
  {:malli/schema [:=> [:cat] :boolean]}
  []
  (state/any-stream-running?))

(defn all-streams-running?
  "Check if all streams are running"
  {:malli/schema [:=> [:cat] :boolean]}
  []
  (state/all-streams-running?))

(defn get-stream-info
  "Get complete information about a stream
  
  Parameters:
    stream-type - :heat or :day
  
  Returns map with :status, :pid, :window, :error, etc."
  {:malli/schema [:=> [:cat :keyword] [:maybe :map]]}
  [stream-type]
  (state/get-stream-state stream-type))

;; ============================================================================
;; Control Messages
;; ============================================================================

(defn send-command
  "Send command to a running stream
  
  Parameters:
    stream-type - :heat or :day
    action - Command action keyword
    data - Command data map
  
  Returns true if sent, false if stream not running or invalid type."
  {:malli/schema [:=> [:cat :keyword :keyword :map] :boolean]}
  [stream-type action data]
  (if (and (config/valid-stream-type? stream-type)
           (state/stream-running? stream-type))
    (coordinator/send-command stream-type action data)
    false))

(defn request-stream-close
  "Request a stream to close gracefully
  
  Parameters:
    stream-type - :heat or :day"
  {:malli/schema [:=> [:cat :keyword] :boolean]}
  [stream-type]
  (coordinator/send-close-request stream-type))

;; ============================================================================
;; Lifecycle
;; ============================================================================

(defn initialize
  "Initialize the stream system"
  {:malli/schema [:=> [:cat] :nil]}
  []
  (coordinator/initialize))

(defn shutdown
  "Shutdown all streams and cleanup resources"
  {:malli/schema [:=> [:cat] :nil]}
  []
  (coordinator/shutdown))