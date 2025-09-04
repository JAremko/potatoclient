(ns potatoclient.ipc.core
  "IPC server implementation using Unix Domain Sockets.
   Wraps Java UnixSocketCommunicator with Transit serialization."
  (:require
    [potatoclient.ipc.handlers :as handlers]
    [potatoclient.ipc.transit :as transit]
    [potatoclient.logging :as logging]
    [potatoclient.malli.registry :as registry])
  (:import
    (clojure.lang Atom)
    (java.lang ProcessHandle)
    (java.nio.file Files LinkOption Path)
    (java.util.concurrent LinkedBlockingQueue TimeUnit)
    (potatoclient.java.ipc SocketFactory UnixSocketCommunicator)))

;; ============================================================================
;; Constants
;; ============================================================================

(def ^:private error-retry-delay-ms
  "Delay in milliseconds before retrying after an error."
  100)

(def ^:private message-queue-capacity
  "Maximum number of messages that can be queued."
  1000)

;; ============================================================================
;; Specs
;; ============================================================================

(def StreamType
  "Valid stream types for IPC communication."
  [:enum :heat :day])

(def IpcServer
  "Schema for an IPC server instance."
  [:map
   [:stream-type StreamType]
   [:socket-path [:fn (partial instance? Path)]]
   [:communicator [:fn (partial instance? UnixSocketCommunicator)]]
   [:message-queue [:fn (partial instance? LinkedBlockingQueue)]]
   [:running? [:fn (partial instance? Atom)]]
   [:reader-thread [:fn #(or (nil? %) (instance? Thread %))]]
   [:processor-thread [:fn #(or (nil? %) (instance? Thread %))]]])

;; Register specs with shared registry
(registry/register-spec! :potatoclient.ipc/stream-type StreamType)
(registry/register-spec! :potatoclient.ipc/server IpcServer)

;; ============================================================================
;; IPC Server Implementation
;; ============================================================================

(defn get-current-pid
  "Get the current process PID.
  Returns the PID as a long value."
  ^Long []
  (.pid (ProcessHandle/current)))

(defn- generate-socket-path
  "Generate a socket path for a stream.
  Creates a unique socket path based on PID and stream type.

  Args:
    stream-type - One of :heat or :day

  Returns:
    Path - The generated socket path"
  {:malli/schema [:=> [:cat StreamType] [:fn (partial instance? Path)]]}
  ^Path [stream-type]
  (let [socket-name (str "ipc-" (get-current-pid) "-" (name stream-type))]
    (SocketFactory/generateSocketPath socket-name)))

(defn- start-reader-thread
  "Start the thread that reads messages from the socket.

  Args:
    server - Server map containing stream-type, communicator, message-queue, and running?

  Returns:
    Thread - The started reader thread"
  {:malli/schema [:=> [:cat IpcServer] [:fn (partial instance? Thread)]]}
  ^Thread [{:keys [stream-type ^UnixSocketCommunicator communicator ^LinkedBlockingQueue message-queue running?]}]
  (Thread.
    (fn []
      (logging/log-info (str "[" (name stream-type) "-server] Reader thread started"))
      (while @running?
        (try
          (when-let [message-bytes (.receive communicator)]
            (let [message (transit/read-message message-bytes)]
              (when-not (.offer message-queue message)
                (logging/log-warn (str "[" (name stream-type) "-server] Message queue full, dropping message")))))
          (catch InterruptedException _
            (.interrupt (Thread/currentThread))
            (reset! running? false))
          (catch Exception ex
            (when @running?
              (logging/log-error (str "[" (name stream-type) "-server] Error reading message: " (.getMessage ex)))
              (Thread/sleep ^long error-retry-delay-ms))))))
    (str (name stream-type) "-server-reader")))

(defn- start-processor-thread
  "Start the thread that processes messages from the queue.

  Args:
    server - Server map containing stream-type, message-queue, and running?
    on-message - Optional message handler function

  Returns:
    Thread - The started processor thread"
  {:malli/schema [:=> [:cat IpcServer [:maybe [:=> [:cat [:map-of :keyword :any]] :any]]] [:fn (partial instance? Thread)]]}
  ^Thread [{:keys [stream-type ^LinkedBlockingQueue message-queue running?]} on-message]
  (let [handler (handlers/create-handler
                  {:name (str (name stream-type) "-server")
                   :handler-fn on-message
                   :running? running?})]
    (handlers/create-processor-thread
      {:name (str (name stream-type) "-server-processor")
       :queue message-queue
       :handler handler
       :daemon? true})))

(defn create-server
  "Create and start an IPC server for a stream.
   Returns a server map with control functions."
  {:malli/schema [:=> [:cat StreamType [:* :any]] IpcServer]}
  [stream-type & {:keys [on-message await-binding?]
                  :or {await-binding? true}}]
  (let [socket-path (generate-socket-path stream-type)
        _ (Files/deleteIfExists socket-path)
        communicator (SocketFactory/createServer ^Path socket-path)
        message-queue (LinkedBlockingQueue. ^int message-queue-capacity)
        running? (atom false)
        server {:stream-type stream-type
                :socket-path socket-path
                :communicator communicator
                :message-queue message-queue
                :running? running?
                :reader-thread nil
                :processor-thread nil}]

    ;; Start the socket
    (.start communicator)

    ;; Wait for socket to be bound if requested
    (when await-binding?
      (let [retries (atom 10)]
        (while (and (not (Files/exists socket-path (make-array LinkOption 0)))
                    (pos? @retries))
          (Thread/sleep 10)
          (swap! retries dec))
        (when-not (Files/exists socket-path (make-array LinkOption 0))
          (throw (ex-info "Socket file not created" {:path (.toString socket-path)})))))

    (reset! running? true)

    ;; Start threads
    (let [^Thread reader-thread (start-reader-thread server)
          ^Thread processor-thread (start-processor-thread server on-message)]
      (.setDaemon reader-thread true)
      (.start reader-thread)
      (.start processor-thread)

      (logging/log-info (str "[" (name stream-type) "-server] IPC server started on " (.toString socket-path)))

      (assoc server
        :reader-thread reader-thread
        :processor-thread processor-thread))))

(defn stop-server
  "Stop an IPC server and clean up resources.
  Stops reader and processor threads, closes the socket, and removes the socket file.

  Args:
    server - The IPC server to stop

  Returns:
    nil"
  {:malli/schema [:=> [:cat IpcServer] :nil]}
  [server]
  (let [{:keys [stream-type running? ^Thread reader-thread ^Thread processor-thread
                ^UnixSocketCommunicator communicator ^Path socket-path]} server]
    (when @running?
      (logging/log-info (str "[" (name stream-type) "-server] Stopping IPC server"))
      (reset! running? false)

      ;; Stop threads
      (when reader-thread
        (.interrupt reader-thread))
      (when processor-thread
        (.interrupt processor-thread))

      ;; Stop socket and remove from SocketFactory
      (when communicator
        (.stop communicator)
        ;; Important: Remove from SocketFactory's static map
        (SocketFactory/close communicator))

      ;; Clean up socket file
      (try
        (Files/deleteIfExists socket-path)
        (catch Exception _))

      (logging/log-info (str "[" (name stream-type) "-server] IPC server stopped")))
    nil))

(defn send-message
  "Send a message through the IPC server.
  Serializes the message using Transit and sends it through the Unix socket.

  Args:
    server - The IPC server to send through
    message - The message map to send

  Returns:
    boolean - true if successful, false otherwise"
  {:malli/schema [:=> [:cat IpcServer [:map-of :keyword :any]] :boolean]}
  ^Boolean [server message]
  (let [{:keys [stream-type ^UnixSocketCommunicator communicator running?]} server]
    (if @running?
      (try
        (let [message-bytes (transit/write-message message)]
          (.send communicator message-bytes)
          true)
        (catch Exception ex
          (logging/log-error (str "[" (name stream-type) "-server] Failed to send message: " (.getMessage ex)))
          false))
      (do
        (logging/log-warn (str "[" (name stream-type) "-server] Cannot send message - server not running"))
        false))))

(defn receive-message
  "Receive a message from the queue (blocking with optional timeout).
  Blocks until a message is available or timeout expires.

  Args:
    server - The IPC server to receive from
    :timeout-ms - Optional timeout in milliseconds (default 0 = infinite)

  Returns:
    The message map if available, nil otherwise"
  {:malli/schema [:=> [:cat IpcServer [:* :any]] [:maybe [:map-of :keyword :any]]]}
  [server & {:keys [timeout-ms] :or {timeout-ms 0}}]
  (let [{:keys [^LinkedBlockingQueue message-queue running?]} server]
    (when @running?
      (if (pos? timeout-ms)
        (.poll message-queue timeout-ms TimeUnit/MILLISECONDS)
        (.take message-queue)))))

(defn try-receive-message
  "Try to receive a message without blocking.
  Returns immediately with a message if available, nil otherwise.

  Args:
    server - The IPC server to receive from

  Returns:
    The message map if available, nil otherwise"
  {:malli/schema [:=> [:cat IpcServer] [:maybe [:map-of :keyword :any]]]}
  [server]
  (let [{:keys [^LinkedBlockingQueue message-queue]} server]
    (.poll message-queue)))

(defn server-running?
  "Check if the server is running.
  Returns true if the server is active and the socket is connected.

  Args:
    server - The IPC server to check

  Returns:
    boolean - true if running, false otherwise"
  {:malli/schema [:=> [:cat IpcServer] :boolean]}
  ^Boolean [server]
  (let [{:keys [running? ^UnixSocketCommunicator communicator]} server]
    (and @running?
         communicator
         (.isRunning communicator))))

;; ============================================================================
;; Server Pool Management
;; ============================================================================

(def ^:private servers
  "Registry of active IPC servers indexed by stream type."
  (atom {}))

(defn get-server
  "Get an existing server for a stream type.

  Args:
    stream-type - One of :heat or :day

  Returns:
    The server instance if exists, nil otherwise"
  {:malli/schema [:=> [:cat StreamType] [:maybe IpcServer]]}
  [stream-type]
  (get @servers stream-type))

(defn create-and-register-server
  "Create a server and register it in the pool.
  Stops any existing server for the same stream type before creating a new one.

  Args:
    stream-type - One of :heat or :day
    opts - Optional keyword arguments passed to create-server

  Returns:
    The newly created server instance"
  {:malli/schema [:=> [:cat StreamType [:* :any]] IpcServer]}
  [stream-type & opts]
  (when-let [existing (get-server stream-type)]
    (stop-server existing)
    (swap! servers dissoc stream-type))
  (let [server (apply create-server stream-type opts)]
    (swap! servers assoc stream-type server)
    server))

(defn stop-all-servers
  "Stop all registered servers.
  Cleans up all active IPC servers and empties the registry.

  Returns:
    nil"
  {:malli/schema [:=> [:cat] :nil]}
  []
  (doseq [[_stream-type server] @servers]
    (logging/log-info (str "Stopping " (name (:stream-type server)) " server"))
    (stop-server server))
  (reset! servers {})
  nil)
