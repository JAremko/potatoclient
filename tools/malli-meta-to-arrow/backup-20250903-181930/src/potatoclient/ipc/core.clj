(ns potatoclient.ipc.core
  "IPC server implementation using Unix Domain Sockets.
   Wraps Java UnixSocketCommunicator with Transit serialization."
  (:require
    [potatoclient.ipc.handlers :as handlers]
    [potatoclient.ipc.transit :as transit]
    [potatoclient.malli.registry :as registry]
    [taoensso.telemere :as t])
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

(def ^:private message-poll-timeout-ms
  "Timeout in milliseconds for polling messages from queue."
  100)

(def ^:private message-queue-capacity
  "Maximum number of messages that can be queued."
  1000)

;; ============================================================================
;; Specs
;; ============================================================================

(def StreamType
  [:enum :heat :day])

(def IpcServer
  [:map
   [:stream-type StreamType]
   [:socket-path [:fn #(instance? Path %)]]
   [:communicator [:fn #(instance? UnixSocketCommunicator %)]]
   [:message-queue [:fn #(instance? LinkedBlockingQueue %)]]
   [:running? [:fn #(instance? Atom %)]]
   [:reader-thread [:fn #(or (nil? %) (instance? Thread %))]]
   [:processor-thread [:fn #(or (nil? %) (instance? Thread %))]]])

;; Register specs with shared registry
(registry/register-spec! :potatoclient.ipc/stream-type StreamType)
(registry/register-spec! :potatoclient.ipc/server IpcServer)

;; ============================================================================
;; IPC Server Implementation
;; ============================================================================

(defn get-current-pid
  "Get the current process PID."
  []
  (.pid (ProcessHandle/current)))

(defn- generate-socket-path
  "Generate a socket path for a stream."
  {:malli/schema [:=> [:cat StreamType] [:fn (partial instance? Path)]]}
  [stream-type]
  (let [socket-name (str "ipc-" (get-current-pid) "-" (name stream-type))]
    (SocketFactory/generateSocketPath socket-name)))

(defn- start-reader-thread
  "Start the thread that reads messages from the socket."
  {:malli/schema [:=> [:cat IpcServer] [:fn (partial instance? Thread)]]}
  [{:keys [stream-type communicator message-queue running?]}]
  (Thread.
    (fn []
      (t/log! :info (str "[" (name stream-type) "-server] Reader thread started"))
      (while @running?
        (try
          (when-let [message-bytes (.receive communicator)]
            (let [message (transit/read-message message-bytes)]
              (when-not (.offer message-queue message)
                (t/log! :warn (str "[" (name stream-type) "-server] Message queue full, dropping message")))))
          (catch InterruptedException _
            (.interrupt (Thread/currentThread))
            (reset! running? false))
          (catch Exception e
            (when @running?
              (t/log! :error (str "[" (name stream-type) "-server] Error reading message: " (.getMessage e)))
              (Thread/sleep error-retry-delay-ms))))))
    (str (name stream-type) "-server-reader")))

(defn- start-processor-thread
  "Start the thread that processes messages from the queue."
  {:malli/schema [:=> [:cat IpcServer [:maybe [:=> [:cat [:map-of :keyword :any]] :any]]] [:fn (partial instance? Thread)]]}
  [{:keys [stream-type message-queue running?]} on-message]
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
        message-queue (LinkedBlockingQueue. message-queue-capacity)
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

      (t/log! :info (str "[" (name stream-type) "-server] IPC server started on " (.toString socket-path)))

      (assoc server
             :reader-thread reader-thread
             :processor-thread processor-thread))))

(defn stop-server
  "Stop an IPC server and clean up resources."
  {:malli/schema [:=> [:cat IpcServer] :nil]}
  [server]
  (let [{:keys [stream-type running? reader-thread processor-thread
                communicator socket-path]} server]
    (when @running?
      (t/log! :info (str "[" (name stream-type) "-server] Stopping IPC server"))
      (reset! running? false)

      ;; Stop threads
      (when reader-thread
        (.interrupt ^Thread reader-thread))
      (when processor-thread
        (.interrupt ^Thread processor-thread))

      ;; Stop socket and remove from SocketFactory
      (when communicator
        (.stop ^UnixSocketCommunicator communicator)
        ;; Important: Remove from SocketFactory's static map
        (SocketFactory/close communicator))

      ;; Clean up socket file
      (try
        (Files/deleteIfExists socket-path)
        (catch Exception _))

      (t/log! :info (str "[" (name stream-type) "-server] IPC server stopped")))
    nil))

(defn send-message
  "Send a message through the IPC server."
  {:malli/schema [:=> [:cat IpcServer [:map-of :keyword :any]] :boolean]}
  [server message]
  (let [{:keys [stream-type communicator running?]} server]
    (if @running?
      (try
        (let [message-bytes (transit/write-message message)]
          (.send communicator message-bytes)
          true)
        (catch Exception e
          (t/log! :error (str "[" (name stream-type) "-server] Failed to send message: " (.getMessage e)))
          false))
      (do
        (t/log! :warn (str "[" (name stream-type) "-server] Cannot send message - server not running"))
        false))))

(defn receive-message
  "Receive a message from the queue (blocking with optional timeout)."
  {:malli/schema [:=> [:cat IpcServer [:* :any]] [:maybe [:map-of :keyword :any]]]}
  [server & {:keys [timeout-ms] :or {timeout-ms 0}}]
  (let [{:keys [message-queue running?]} server]
    (when @running?
      (if (pos? timeout-ms)
        (.poll message-queue timeout-ms TimeUnit/MILLISECONDS)
        (.take message-queue)))))

(defn try-receive-message
  "Try to receive a message without blocking."
  {:malli/schema [:=> [:cat IpcServer] [:maybe [:map-of :keyword :any]]]}
  [server]
  (let [{:keys [message-queue]} server]
    (.poll message-queue)))

(defn server-running?
  "Check if the server is running."
  {:malli/schema [:=> [:cat IpcServer] :boolean]}
  [server]
  (let [{:keys [running? communicator]} server]
    (and @running?
         communicator
         (.isRunning communicator))))

;; ============================================================================
;; Server Pool Management
;; ============================================================================

(def ^:private servers (atom {}))

(defn get-server
  "Get an existing server for a stream type."
  {:malli/schema [:=> [:cat StreamType] [:maybe IpcServer]]}
  [stream-type]
  (get @servers stream-type))

(defn create-and-register-server
  "Create a server and register it in the pool."
  {:malli/schema [:=> [:cat StreamType [:* :any]] IpcServer]}
  [stream-type & opts]
  (when-let [existing (get-server stream-type)]
    (stop-server existing)
    (swap! servers dissoc stream-type))
  (let [server (apply create-server stream-type opts)]
    (swap! servers assoc stream-type server)
    server))

(defn stop-all-servers
  "Stop all registered servers."
  {:malli/schema [:=> [:cat] :nil]}
  []
  (doseq [[stream-type server] @servers]
    (t/log! :info (str "Stopping " (name stream-type) " server"))
    (stop-server server))
  (reset! servers {})
  nil)
