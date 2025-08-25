(ns clj-stream-spawner.coordinator
  "Coordinates heat and day camera streams with IPC and process management."
  (:require
    [clj-stream-spawner.events :as events]
    [clj-stream-spawner.ipc :as ipc]
    [clj-stream-spawner.process :as process]
    [clj-stream-spawner.transit :as transit]
    [com.fulcrologic.guardrails.malli.core :refer [=> >defn >defn-]]
    [taoensso.telemere :as t])
  (:import
    (java.util.concurrent CountDownLatch TimeUnit)))

;; ============================================================================
;; Specs
;; ============================================================================

(def StreamType
  [:enum :heat :day])

(def StreamState
  [:map
   [:status [:enum :starting :running :stopping :stopped :failed]]
   [:ipc-server [:maybe ipc/IpcServer]]
   [:process [:maybe process/ProcessInfo]]
   [:error [:maybe :string]]])

(def CoordinatorState
  [:map
   [:host :string]
   [:debug? :boolean]
   [:streams [:map
              [:heat StreamState]
              [:day StreamState]]]
   [:shutdown-latch [:fn #(instance? CountDownLatch %)]]])

;; ============================================================================
;; State Management
;; ============================================================================

(def ^:private state (atom nil))

(>defn- init-state
  "Initialize coordinator state."
  [host debug?]
  [:string :boolean => CoordinatorState]
  {:host host
   :debug? debug?
   :streams {:heat {:status :stopped
                    :ipc-server nil
                    :process nil
                    :error nil}
             :day {:status :stopped
                   :ipc-server nil
                   :process nil
                   :error nil}}
   :shutdown-latch (CountDownLatch. 1)})

(>defn- update-stream-state
  "Update the state of a specific stream."
  [stream-type updates]
  [StreamType [:map-of :keyword :any] => :any]
  (swap! state update-in [:streams stream-type] merge updates))

(>defn- get-stream-state
  "Get the current state of a stream."
  [stream-type]
  [StreamType => StreamState]
  (get-in @state [:streams stream-type]))

;; ============================================================================
;; IPC Message Handlers
;; ============================================================================

(>defn- create-message-handler
  "Create a message handler for a stream."
  [stream-type]
  [StreamType => [:=> [:cat [:map-of :keyword :any]] :any]]
  (fn [message]
    (let [msg-type (:msg-type message)]
      (case msg-type
        :event (events/handle-event stream-type message)
        :log (events/handle-log stream-type message)
        :metric (events/handle-metric stream-type message)
        :command (events/handle-command stream-type message)
        (t/log! :warn (str "[" (name stream-type) "] Unknown message type: " msg-type))))))

;; ============================================================================
;; Stream Lifecycle
;; ============================================================================

(>defn- start-stream-ipc
  "Start IPC server for a stream."
  [stream-type]
  [StreamType => [:maybe ipc/IpcServer]]
  (try
    (t/log! :info (str "Starting IPC server for " (name stream-type) " stream"))
    (update-stream-state stream-type {:status :starting})

    (let [handler (create-message-handler stream-type)
          server (ipc/create-and-register-server stream-type
                                                  :on-message handler
                                                  :await-binding? true)]
      (update-stream-state stream-type {:ipc-server server})
      server)
    (catch Exception e
      (t/log! :error (str "Failed to start IPC server for " (name stream-type) ": " (.getMessage e)))
      (update-stream-state stream-type {:status :failed
                                        :error (.getMessage e)})
      nil)))

(>defn- start-stream-process
  "Start VideoStreamManager process for a stream."
  [stream-type]
  [StreamType => [:maybe process/ProcessInfo]]
  (let [{:keys [host debug?]} @state]
    (try
      (t/log! :info (str "Starting process for " (name stream-type) " stream"))

      ;; Small delay to ensure IPC server is ready
      (Thread/sleep 100)

      (let [process-info (process/spawn-and-register stream-type host
                                                      :debug? debug?
                                                      :parent-pid (ipc/get-current-pid))]
        (update-stream-state stream-type {:process process-info
                                          :status :running})
        process-info)
      (catch Exception e
        (t/log! :error (str "Failed to start process for " (name stream-type) ": " (.getMessage e)))
        (update-stream-state stream-type {:status :failed
                                          :error (.getMessage e)})
        nil))))

(>defn start-stream
  "Start a complete stream (IPC + process)."
  [stream-type]
  [StreamType => :boolean]
  (let [current-state (get-stream-state stream-type)]
    (if (= (:status current-state) :running)
      (do
        (t/log! :warn (str (name stream-type) " stream is already running"))
        true)
      (do
        ;; Start IPC server first
        (when-let [ipc-server (start-stream-ipc stream-type)]
          ;; Then start the process
          (if (start-stream-process stream-type)
            true
            (do
              ;; Clean up IPC if process fails
              (ipc/stop-server ipc-server)
              (update-stream-state stream-type {:status :failed})
              false)))))))

(>defn stop-stream
  "Stop a stream (process + IPC)."
  [stream-type]
  [StreamType => :nil]
  (let [{:keys [status ipc-server process]} (get-stream-state stream-type)]
    (when (#{:starting :running} status)
      (t/log! :info (str "Stopping " (name stream-type) " stream"))
      (update-stream-state stream-type {:status :stopping})

      ;; Stop process first
      (when process
        (process/stop-process process))

      ;; Then stop IPC server
      (when ipc-server
        (Thread/sleep 100) ; Give process time to disconnect
        (ipc/stop-server ipc-server))

      (update-stream-state stream-type {:status :stopped
                                        :ipc-server nil
                                        :process nil})))
  nil)

;; ============================================================================
;; Coordinator Control
;; ============================================================================

(>defn start-all-streams
  "Start both heat and day streams."
  []
  [=> [:map [:heat :boolean] [:day :boolean]]]
  {:heat (start-stream :heat)
   :day (start-stream :day)})

(>defn stop-all-streams
  "Stop both heat and day streams."
  []
  [=> :nil]
  (t/log! :info "Stopping all streams...")
  (stop-stream :heat)
  (stop-stream :day)
  nil)

(>defn initialize
  "Initialize the coordinator with configuration."
  [host & {:keys [debug?] :or {debug? false}}]
  [:string [:* :any] => CoordinatorState]
  (let [new-state (init-state host debug?)]
    (reset! state new-state)
    (t/log! :info (str "Coordinator initialized - Host: " host " Debug: " debug?))
    new-state))

(>defn shutdown
  "Shutdown the coordinator and all streams."
  []
  [=> :nil]
  (t/log! :info "Shutting down coordinator...")
  (stop-all-streams)

  ;; Signal shutdown
  (when-let [latch (:shutdown-latch @state)]
    (.countDown latch))

  (reset! state nil)
  (t/log! :info "Coordinator shutdown complete")
  nil)

(>defn wait-for-shutdown
  "Wait for the coordinator to shutdown."
  [& {:keys [timeout-seconds]}]
  [[:* :any] => :boolean]
  (if-let [latch (:shutdown-latch @state)]
    (if timeout-seconds
      (.await latch timeout-seconds TimeUnit/SECONDS)
      (do (.await latch) true))
    false))

;; ============================================================================
;; Status & Monitoring
;; ============================================================================

(>defn get-status
  "Get the current status of all streams."
  []
  [=> [:map [:heat StreamState] [:day StreamState]]]
  (:streams @state))

(>defn stream-running?
  "Check if a stream is running."
  [stream-type]
  [StreamType => :boolean]
  (= :running (:status (get-stream-state stream-type))))

(>defn all-streams-running?
  "Check if both streams are running."
  []
  [=> :boolean]
  (and (stream-running? :heat)
       (stream-running? :day)))

;; ============================================================================
;; Control Messages
;; ============================================================================

(>defn send-close-request
  "Send a close request to a stream."
  [stream-type]
  [StreamType => :boolean]
  (if-let [server (:ipc-server (get-stream-state stream-type))]
    (let [message (transit/create-command :close-request
                                          {:stream-type stream-type})]
      (ipc/send-message server message))
    false))
