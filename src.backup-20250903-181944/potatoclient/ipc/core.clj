(ns potatoclient.ipc.core
  "IPC server implementation using Unix Domain Sockets.
   Wraps Java UnixSocketCommunicator with Transit serialization."
  (:require
            [malli.core :as m]
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
  [stream-type]
  (let [socket-name (str "ipc-" (get-current-pid) "-" (name stream-type))]
    (SocketFactory/generateSocketPath socket-name))) 
 (m/=> generate-socket-path [:=> [:cat StreamType] [:fn (partial instance? Path)]])

(defn- start-reader-thread
  "Start the thread that reads messages from the socket."
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
 (m/=> start-reader-thread [:=> [:cat IpcServer] [:fn (partial instance? Thread)]])

(defn- start-processor-thread
  "Start the thread that processes messages from the queue."
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
 (m/=> start-processor-thread [:=> [:cat IpcServer [:maybe [:=> [:cat [:map-of :keyword :any]] :any]]] [:fn (partial instance? Thread)]])

(defn create-server
  "Create and start an IPC server for a stream.
   Returns a server map with control functions."
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
 (m/=> create-server [:=> [:cat StreamType [:* :any]] IpcServer])

(defn stop-server
  "Stop an IPC server and clean up resources."
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
 (m/=> stop-server [:=> [:cat IpcServer] :nil])

(defn send-message
  "Send a message through the IPC server."
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
 (m/=> send-message [:=> [:cat IpcServer [:map-of :keyword :any]] :boolean])

(defn receive-message
  "Receive a message from the queue (blocking with optional timeout)."
  [server & {:keys [timeout-ms] :or {timeout-ms 0}}]
  (let [{:keys [message-queue running?]} server]
    (when @running?
      (if (pos? timeout-ms)
        (.poll message-queue timeout-ms TimeUnit/MILLISECONDS)
        (.take message-queue))))) 
 (m/=> receive-message [:=> [:cat IpcServer [:* :any]] [:maybe [:map-of :keyword :any]]])

(defn try-receive-message
  "Try to receive a message without blocking."
  [server]
  (let [{:keys [message-queue]} server]
    (.poll message-queue))) 
 (m/=> try-receive-message [:=> [:cat IpcServer] [:maybe [:map-of :keyword :any]]])

(defn server-running?
  "Check if the server is running."
  [server]
  (let [{:keys [running? communicator]} server]
    (and @running?
         communicator
         (.isRunning communicator)))) 
 (m/=> server-running? [:=> [:cat IpcServer] :boolean])

;; ============================================================================
;; Server Pool Management
;; ============================================================================

(def ^:private servers (atom {}))

(defn get-server
  "Get an existing server for a stream type."
  [stream-type]
  (get @servers stream-type)) 
 (m/=> get-server [:=> [:cat StreamType] [:maybe IpcServer]])

(defn create-and-register-server
  "Create a server and register it in the pool."
  [stream-type & opts]
  (when-let [existing (get-server stream-type)]
    (stop-server existing)
    (swap! servers dissoc stream-type))
  (let [server (apply create-server stream-type opts)]
    (swap! servers assoc stream-type server)
    server)) 
 (m/=> create-and-register-server [:=> [:cat StreamType [:* :any]] IpcServer])

(defn stop-all-servers
  "Stop all registered servers."
  []
  (doseq [[stream-type server] @servers]
    (t/log! :info (str "Stopping " (name stream-type) " server"))
    (stop-server server))
  (reset! servers {})
  nil) 
 (m/=> stop-all-servers [:=> [:cat] :nil])