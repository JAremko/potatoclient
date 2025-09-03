(ns potatoclient.streams.state
  "Stream state management using main app state."
  (:require
    [potatoclient.logging :as logging]
    [potatoclient.state :as app-state]
    [potatoclient.streams.config :as config]
    [potatoclient.ui.status-bar.messages :as status-msg]))

;; ============================================================================
;; State Queries
;; ============================================================================

(defn get-stream-state
  "Get complete state for a stream from app state"
  {:malli/schema [:=> [:cat :keyword] [:maybe :map]]}
  [stream-type]
  (let [process-key (config/get-process-key stream-type)
        process-state (app-state/get-process-state process-key)
        stream-info (app-state/get-stream-process stream-type)]
    (merge process-state stream-info)))

(defn get-stream-status
  "Get status for a specific stream"
  {:malli/schema [:=> [:cat :keyword] [:maybe :keyword]]}
  [stream-type]
  (let [process-key (config/get-process-key stream-type)]
    (get-in @app-state/app-state [:processes process-key :status])))

(defn stream-running?
  "Check if stream is running"
  {:malli/schema [:=> [:cat :keyword] :boolean]}
  [stream-type]
  (= :running (get-stream-status stream-type)))

(defn stream-stopped?
  "Check if stream is stopped"
  {:malli/schema [:=> [:cat :keyword] :boolean]}
  [stream-type]
  (= :stopped (get-stream-status stream-type)))

(defn any-stream-running?
  "Check if any stream is running"
  {:malli/schema [:=> [:cat] :boolean]}
  []
  (or (stream-running? :heat)
      (stream-running? :day)))

(defn all-streams-running?
  "Check if all streams are running"
  {:malli/schema [:=> [:cat] :boolean]}
  []
  (and (stream-running? :heat)
       (stream-running? :day)))

;; ============================================================================
;; State Updates
;; ============================================================================

(defn set-stream-status!
  "Set status for a stream and update UI"
  {:malli/schema [:=> [:cat :keyword :keyword [:maybe :int]] :nil]}
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

(defn set-stream-process-info!
  "Store additional process info for a stream"
  {:malli/schema [:=> [:cat :keyword :map] :nil]}
  [stream-type info]
  (app-state/add-stream-process! stream-type info)
  nil)

(defn update-stream-process-info!
  "Update existing stream process info"
  {:malli/schema [:=> [:cat :keyword :map] :nil]}
  [stream-type updates]
  (let [current (app-state/get-stream-process stream-type)]
    (app-state/add-stream-process! stream-type (merge current updates)))
  nil)

(defn set-stream-error!
  "Set error for a stream"
  {:malli/schema [:=> [:cat :keyword :string] :nil]}
  [stream-type error]
  (update-stream-process-info! stream-type {:error error})
  (set-stream-status! stream-type :error nil)
  nil)

(defn clear-stream-error!
  "Clear error for a stream"
  {:malli/schema [:=> [:cat :keyword] :nil]}
  [stream-type]
  (update-stream-process-info! stream-type {:error nil})
  nil)

(defn set-stream-ipc!
  "Store IPC server reference for a stream"
  {:malli/schema [:=> [:cat :keyword :any] :nil]}
  [stream-type ipc-server]
  (update-stream-process-info! stream-type {:ipc-server ipc-server})
  nil)

(defn get-stream-ipc
  "Get IPC server for a stream"
  {:malli/schema [:=> [:cat :keyword] [:maybe :any]]}
  [stream-type]
  (get-in (app-state/get-stream-process stream-type) [:ipc-server]))

(defn set-stream-process!
  "Store Java Process object for a stream"
  {:malli/schema [:=> [:cat :keyword :any] :nil]}
  [stream-type process]
  (update-stream-process-info! stream-type {:process process})
  nil)

(defn get-stream-process
  "Get Java Process object for a stream"
  {:malli/schema [:=> [:cat :keyword] [:maybe :any]]}
  [stream-type]
  (get-in (app-state/get-stream-process stream-type) [:process]))

(defn set-stream-window-info!
  "Store stream window position and size"
  {:malli/schema [:=> [:cat :keyword :map] :nil]}
  [stream-type window-info]
  (update-stream-process-info! stream-type {:window window-info})
  nil)

(defn set-stream-window-position!
  "Set window position for a stream"
  {:malli/schema [:=> [:cat :keyword :int :int] :nil]}
  [stream-type x y]
  (swap! app-state/app-state assoc-in [:streams stream-type :window :x] x)
  (swap! app-state/app-state assoc-in [:streams stream-type :window :y] y)
  nil)

(defn set-stream-window-size!
  "Set window size for a stream"
  {:malli/schema [:=> [:cat :keyword :int :int] :nil]}
  [stream-type width height]
  (swap! app-state/app-state assoc-in [:streams stream-type :window :width] width)
  (swap! app-state/app-state assoc-in [:streams stream-type :window :height] height)
  nil)

(defn get-stream-window-info
  "Get stream window position and size"
  {:malli/schema [:=> [:cat :keyword] [:maybe :map]]}
  [stream-type]
  (get-in (app-state/get-stream-process stream-type) [:window]))

;; ============================================================================
;; State Reset
;; ============================================================================

(defn reset-stream-state!
  "Reset state for a stream"
  {:malli/schema [:=> [:cat :keyword] :nil]}
  [stream-type]
  (let [process-key (config/get-process-key stream-type)]
    (app-state/update-process-status! process-key nil :stopped)
    (app-state/remove-stream-process! stream-type))
  nil)

(defn reset-all-states!
  "Reset all stream states"
  {:malli/schema [:=> [:cat] :nil]}
  []
  (reset-stream-state! :heat)
  (reset-stream-state! :day)
  nil)