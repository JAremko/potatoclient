(ns potatoclient.streams.state
  "Stream state management using main app state."
  (:require
            [malli.core :as m]
    [potatoclient.logging :as logging]
    [potatoclient.state :as app-state]
    [potatoclient.streams.config :as config]
    [potatoclient.ui.status-bar.messages :as status-msg]))

;; ============================================================================
;; State Queries
;; ============================================================================

(defn get-stream-state
  "Get complete state for a stream from app state"
  [stream-type]
  (let [process-key (config/get-process-key stream-type)
        process-state (app-state/get-process-state process-key)
        stream-info (app-state/get-stream-process stream-type)]
    (merge process-state stream-info))) 
 (m/=> get-stream-state [:=> [:cat :keyword] [:maybe :map]])

(defn get-stream-status
  "Get status for a specific stream"
  [stream-type]
  (let [process-key (config/get-process-key stream-type)]
    (get-in @app-state/app-state [:processes process-key :status]))) 
 (m/=> get-stream-status [:=> [:cat :keyword] [:maybe :keyword]])

(defn stream-running?
  "Check if stream is running"
  [stream-type]
  (= :running (get-stream-status stream-type))) 
 (m/=> stream-running? [:=> [:cat :keyword] :boolean])

(defn stream-stopped?
  "Check if stream is stopped"
  [stream-type]
  (= :stopped (get-stream-status stream-type))) 
 (m/=> stream-stopped? [:=> [:cat :keyword] :boolean])

(defn any-stream-running?
  "Check if any stream is running"
  []
  (or (stream-running? :heat)
      (stream-running? :day))) 
 (m/=> any-stream-running? [:=> [:cat] :boolean])

(defn all-streams-running?
  "Check if all streams are running"
  []
  (and (stream-running? :heat)
       (stream-running? :day))) 
 (m/=> all-streams-running? [:=> [:cat] :boolean])

;; ============================================================================
;; State Updates
;; ============================================================================

(defn set-stream-status!
  "Set status for a stream and update UI"
  [stream-type status pid]
  (let [process-key (config/get-process-key stream-type)]
    ;; Update app state
    (app-state/update-process-status! process-key pid status)

    ;; Update status bar
    (case status
      :starting (let [name (get-in config/stream-config [stream-type :name])]
                  (status-msg/set-info! (str "Starting " name "...")))
      :running (status-msg/set-stream-started! stream-type)
      :stopping (let [name (get-in config/stream-config [stream-type :name])]
                  (status-msg/set-info! (str "Stopping " name "...")))
      :stopped (status-msg/set-stream-stopped! stream-type)
      :error (let [name (get-in config/stream-config [stream-type :name])
                   error (get-in (app-state/get-stream-process stream-type) [:error])]
               (status-msg/set-error! (str name " failed: " (or error "Unknown error"))))
      nil)

    ;; Log state change
    (logging/log-info {:id :stream/status-change
                       :stream stream-type
                       :status status
                       :pid pid}))
  nil) 
 (m/=> set-stream-status! [:=> [:cat :keyword :keyword [:maybe :int]] :nil])

(defn set-stream-process-info!
  "Store additional process info for a stream"
  [stream-type info]
  (app-state/add-stream-process! stream-type info)
  nil) 
 (m/=> set-stream-process-info! [:=> [:cat :keyword :map] :nil])

(defn update-stream-process-info!
  "Update existing stream process info"
  [stream-type updates]
  (let [current (app-state/get-stream-process stream-type)]
    (app-state/add-stream-process! stream-type (merge current updates)))
  nil) 
 (m/=> update-stream-process-info! [:=> [:cat :keyword :map] :nil])

(defn set-stream-error!
  "Set error for a stream"
  [stream-type error]
  (update-stream-process-info! stream-type {:error error})
  (set-stream-status! stream-type :error nil)
  nil) 
 (m/=> set-stream-error! [:=> [:cat :keyword :string] :nil])

(defn clear-stream-error!
  "Clear error for a stream"
  [stream-type]
  (update-stream-process-info! stream-type {:error nil})
  nil) 
 (m/=> clear-stream-error! [:=> [:cat :keyword] :nil])

(defn set-stream-ipc!
  "Store IPC server reference for a stream"
  [stream-type ipc-server]
  (update-stream-process-info! stream-type {:ipc-server ipc-server})
  nil) 
 (m/=> set-stream-ipc! [:=> [:cat :keyword :any] :nil])

(defn get-stream-ipc
  "Get IPC server for a stream"
  [stream-type]
  (get-in (app-state/get-stream-process stream-type) [:ipc-server])) 
 (m/=> get-stream-ipc [:=> [:cat :keyword] [:maybe :any]])

(defn set-stream-process!
  "Store Java Process object for a stream"
  [stream-type process]
  (update-stream-process-info! stream-type {:process process})
  nil) 
 (m/=> set-stream-process! [:=> [:cat :keyword :any] :nil])

(defn get-stream-process
  "Get Java Process object for a stream"
  [stream-type]
  (get-in (app-state/get-stream-process stream-type) [:process])) 
 (m/=> get-stream-process [:=> [:cat :keyword] [:maybe :any]])

(defn set-stream-window-info!
  "Store stream window position and size"
  [stream-type window-info]
  (update-stream-process-info! stream-type {:window window-info})
  nil) 
 (m/=> set-stream-window-info! [:=> [:cat :keyword :map] :nil])

(defn set-stream-window-position!
  "Set window position for a stream"
  [stream-type x y]
  (swap! app-state/app-state assoc-in [:streams stream-type :window :x] x)
  (swap! app-state/app-state assoc-in [:streams stream-type :window :y] y)
  nil) 
 (m/=> set-stream-window-position! [:=> [:cat :keyword :int :int] :nil])

(defn set-stream-window-size!
  "Set window size for a stream"
  [stream-type width height]
  (swap! app-state/app-state assoc-in [:streams stream-type :window :width] width)
  (swap! app-state/app-state assoc-in [:streams stream-type :window :height] height)
  nil) 
 (m/=> set-stream-window-size! [:=> [:cat :keyword :int :int] :nil])

(defn get-stream-window-info
  "Get stream window position and size"
  [stream-type]
  (get-in (app-state/get-stream-process stream-type) [:window])) 
 (m/=> get-stream-window-info [:=> [:cat :keyword] [:maybe :map]])

;; ============================================================================
;; State Reset
;; ============================================================================

(defn reset-stream-state!
  "Reset state for a stream"
  [stream-type]
  (let [process-key (config/get-process-key stream-type)]
    (app-state/update-process-status! process-key nil :stopped)
    (app-state/remove-stream-process! stream-type))
  nil) 
 (m/=> reset-stream-state! [:=> [:cat :keyword] :nil])

(defn reset-all-states!
  "Reset all stream states"
  []
  (reset-stream-state! :heat)
  (reset-stream-state! :day)
  nil) 
 (m/=> reset-all-states! [:=> [:cat] :nil])