(ns potatoclient.streams.core
  "Public API for stream management."
  (:require
            [malli.core :as m]
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
  [stream-type]
  (if (config/valid-stream-type? stream-type)
    (coordinator/start-stream stream-type)
    (do
      (logging/log-error {:id :stream/invalid-type
                         :stream stream-type})
      false))) 
 (m/=> start-stream [:=> [:cat :keyword] :boolean])

(defn stop-stream
  "Stop a video stream
  
  Parameters:
    stream-type - :heat or :day"
  [stream-type]
  (when (config/valid-stream-type? stream-type)
    (coordinator/stop-stream stream-type))
  nil) 
 (m/=> stop-stream [:=> [:cat :keyword] :nil])

(defn restart-stream
  "Restart a video stream
  
  Parameters:
    stream-type - :heat or :day
  
  Returns true if successful, false otherwise."
  [stream-type]
  (if (config/valid-stream-type? stream-type)
    (coordinator/restart-stream stream-type)
    false)) 
 (m/=> restart-stream [:=> [:cat :keyword] :boolean])

(defn toggle-stream
  "Toggle a stream on/off based on current state
  
  Parameters:
    stream-type - :heat or :day
  
  Returns new state (:running or :stopped)"
  [stream-type]
  (if (state/stream-running? stream-type)
    (do
      (stop-stream stream-type)
      :stopped)
    (do
      (start-stream stream-type)
      :running))) 
 (m/=> toggle-stream [:=> [:cat :keyword] [:maybe :keyword]])

;; ============================================================================
;; Batch Operations
;; ============================================================================

(defn start-all-streams
  "Start both heat and day streams
  
  Returns map with results {:heat boolean :day boolean}"
  []
  (coordinator/start-all-streams)) 
 (m/=> start-all-streams [:=> [:cat] :map])

(defn stop-all-streams
  "Stop all running streams"
  []
  (coordinator/stop-all-streams)) 
 (m/=> stop-all-streams [:=> [:cat] :nil])

(defn restart-all-streams
  "Restart all streams
  
  Returns map with results {:heat boolean :day boolean}"
  []
  (coordinator/restart-all-streams)) 
 (m/=> restart-all-streams [:=> [:cat] :map])

;; ============================================================================
;; Status Queries
;; ============================================================================

(defn get-stream-status
  "Get status of a stream
  
  Parameters:
    stream-type - :heat or :day
  
  Returns :stopped, :starting, :running, :stopping, or :error"
  [stream-type]
  (state/get-stream-status stream-type)) 
 (m/=> get-stream-status [:=> [:cat :keyword] [:maybe :keyword]])

(defn stream-running?
  "Check if a stream is running
  
  Parameters:
    stream-type - :heat or :day"
  [stream-type]
  (state/stream-running? stream-type)) 
 (m/=> stream-running? [:=> [:cat :keyword] :boolean])

(defn any-stream-running?
  "Check if any stream is running"
  []
  (state/any-stream-running?)) 
 (m/=> any-stream-running? [:=> [:cat] :boolean])

(defn all-streams-running?
  "Check if all streams are running"
  []
  (state/all-streams-running?)) 
 (m/=> all-streams-running? [:=> [:cat] :boolean])

(defn get-stream-info
  "Get complete information about a stream
  
  Parameters:
    stream-type - :heat or :day
  
  Returns map with :status, :pid, :window, :error, etc."
  [stream-type]
  (state/get-stream-state stream-type)) 
 (m/=> get-stream-info [:=> [:cat :keyword] [:maybe :map]])

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
  [stream-type action data]
  (if (and (config/valid-stream-type? stream-type)
           (state/stream-running? stream-type))
    (coordinator/send-command stream-type action data)
    false)) 
 (m/=> send-command [:=> [:cat :keyword :keyword :map] :boolean])

(defn request-stream-close
  "Request a stream to close gracefully
  
  Parameters:
    stream-type - :heat or :day"
  [stream-type]
  (coordinator/send-close-request stream-type)) 
 (m/=> request-stream-close [:=> [:cat :keyword] :boolean])

;; ============================================================================
;; Lifecycle
;; ============================================================================

(defn initialize
  "Initialize the stream system"
  []
  (coordinator/initialize)) 
 (m/=> initialize [:=> [:cat] :nil])

(defn shutdown
  "Shutdown all streams and cleanup resources"
  []
  (coordinator/shutdown)) 
 (m/=> shutdown [:=> [:cat] :nil])