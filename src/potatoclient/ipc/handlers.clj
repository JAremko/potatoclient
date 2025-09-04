(ns potatoclient.ipc.handlers
  "Common IPC message handler patterns and utilities.

   Provides reusable patterns for creating message handlers,
   processing queues, and managing IPC communication."
  (:require
            [malli.core :as m]
    [taoensso.telemere :as t]
    [potatoclient.ipc.transit :as transit])
  (:import
    (clojure.lang Atom)
    (java.util.concurrent LinkedBlockingQueue TimeUnit)
    (java.lang Thread)))

;; ============================================================================
;; Constants
;; ============================================================================

(def ^:private default-error-retry-delay-ms
  "Default delay in milliseconds before retrying after an error."
  100)

(def ^:private default-message-poll-timeout-ms
  "Default timeout in milliseconds for polling messages from queue."
  100)

;; ============================================================================
;; Handler Protocol
;; ============================================================================

(defprotocol IMessageHandler
  "Protocol for message handlers."
  (handle-message [this message]
    "Handle a single message. Return value is handler-specific.")
  (on-error [this error message]
    "Handle an error during message processing.")
  (should-continue? [this]
    "Check if the handler should continue processing."))

;; ============================================================================
;; Base Handler Implementation
;; ============================================================================

(defrecord BaseMessageHandler [name handler-fn error-fn running?]
  IMessageHandler
  (handle-message [_ message]
    (when handler-fn
      (handler-fn message)))

  (on-error [_ error message]
    (if error-fn
      (error-fn error message)
      (t/log! :error (str "[" name "] Error processing message: " (.getMessage error)))))

  (should-continue? [_]
    @running?))

(defn create-handler
  "Create a message handler with the given configuration.

   Options:
   - :name - Handler name for logging
   - :handler-fn - Function to process messages (required)
   - :error-fn - Function to handle errors (optional)
   - :running? - Atom tracking if handler should continue (required)"
  [{:keys [name handler-fn error-fn running?]}]
  (->BaseMessageHandler name handler-fn error-fn running?))
 (m/=> create-handler [:=> [:cat [:map [:name :string] [:handler-fn [:=> [:cat :any] :any]] [:error-fn {:optional true} [:=> [:cat :any :any] :any]] [:running? [:fn (fn* [p1__4304#] (instance? Atom p1__4304#))]]]] [:fn (fn* [p1__4306#] (satisfies? IMessageHandler p1__4306#))]])

;; ============================================================================
;; Queue Processing
;; ============================================================================

(defn process-queue
  "Process messages from a queue using the provided handler.

   This function will:
   1. Poll messages from the queue
   2. Process them with the handler
   3. Handle errors gracefully
   4. Continue until handler says to stop

   Options:
   - :queue - The message queue (required)
   - :handler - IMessageHandler implementation (required)
   - :poll-timeout-ms - Timeout for polling (default 100ms)
   - :error-delay-ms - Delay after errors (default 100ms)"
  [{:keys [queue handler poll-timeout-ms error-delay-ms]
    :or {poll-timeout-ms default-message-poll-timeout-ms
         error-delay-ms default-error-retry-delay-ms}}]
  (while (should-continue? handler)
    (try
      (when-let [message (.poll queue poll-timeout-ms TimeUnit/MILLISECONDS)]
        (handle-message handler message))
      (catch InterruptedException _
        (.interrupt (Thread/currentThread))
        ;; Exit the loop on interruption
        (reduced nil))
      (catch Exception e
        (when (should-continue? handler)
          (on-error handler e nil)
          (Thread/sleep error-delay-ms)))))
  nil)
 (m/=> process-queue [:=> [:cat [:map [:queue [:fn (fn* [p1__4316#] (instance? LinkedBlockingQueue p1__4316#))]] [:handler [:fn (fn* [p1__4318#] (satisfies? IMessageHandler p1__4318#))]] [:poll-timeout-ms {:optional true} pos-int?] [:error-delay-ms {:optional true} pos-int?]]] :nil])

;; ============================================================================
;; Thread Management
;; ============================================================================

(defn create-processor-thread
  "Create a thread that processes messages from a queue.

   Options:
   - :name - Thread name
   - :queue - Message queue
   - :handler - Message handler
   - :daemon? - Whether thread should be daemon (default true)"
  [{:keys [name queue handler daemon?]
    :or {daemon? true}}]
  (let [thread (Thread.
                 (fn []
                   (t/log! :info (str "[" name "] Processor thread started"))
                   (process-queue {:queue queue
                                   :handler handler})
                   (t/log! :info (str "[" name "] Processor thread stopped")))
                 name)]
    (when daemon?
      (.setDaemon thread true))
    thread))
 (m/=> create-processor-thread [:=> [:cat [:map [:name :string] [:queue [:fn (fn* [p1__4332#] (instance? LinkedBlockingQueue p1__4332#))]] [:handler [:fn (fn* [p1__4334#] (satisfies? IMessageHandler p1__4334#))]] [:daemon? {:optional true} :boolean]]] [:fn (fn* [p1__4336#] (instance? Thread p1__4336#))]])

;; ============================================================================
;; Composite Handlers
;; ============================================================================

(defrecord CompositeHandler [handlers running?]
  IMessageHandler
  (handle-message [_ message]
    (doseq [handler handlers]
      (try
        (handle-message handler message)
        (catch Exception e
          (on-error handler e message)))))

  (on-error [_ _ _]
    ;; Composite handler doesn't handle errors directly
    ;; Individual handlers handle their own errors
    nil)

  (should-continue? [_]
    @running?))

(defn create-composite-handler
  "Create a handler that delegates to multiple handlers.

   All handlers will receive each message.
   Each handler processes messages independently."
  [handlers running?]
  (->CompositeHandler handlers running?))
 (m/=> create-composite-handler [:=> [:cat [:vector [:fn (fn* [p1__4350#] (satisfies? IMessageHandler p1__4350#))]] [:fn (fn* [p1__4352#] (instance? Atom p1__4352#))]] [:fn (fn* [p1__4354#] (satisfies? IMessageHandler p1__4354#))]])

;; ============================================================================
;; Filtering Handlers
;; ============================================================================

(defrecord FilteringHandler [base-handler filter-fn running?]
  IMessageHandler
  (handle-message [_ message]
    (when (filter-fn message)
      (handle-message base-handler message)))

  (on-error [_ error message]
    (on-error base-handler error message))

  (should-continue? [_]
    (and @running?
         (should-continue? base-handler))))

(defn create-filtering-handler
  "Create a handler that filters messages before processing.

   Only messages that pass the filter function will be processed."
  [base-handler filter-fn running?]
  (->FilteringHandler base-handler filter-fn running?))
 (m/=> create-filtering-handler [:=> [:cat [:fn (fn* [p1__4368#] (satisfies? IMessageHandler p1__4368#))] [:=> [:cat :any] :boolean] [:fn (fn* [p1__4370#] (instance? Atom p1__4370#))]] [:fn (fn* [p1__4372#] (satisfies? IMessageHandler p1__4372#))]])

;; ============================================================================
;; Transforming Handlers
;; ============================================================================

(defrecord TransformingHandler [base-handler transform-fn running?]
  IMessageHandler
  (handle-message [_ message]
    (try
      (let [transformed (transform-fn message)]
        (handle-message base-handler transformed))
      (catch Exception e
        (on-error base-handler e message))))

  (on-error [_ error message]
    (on-error base-handler error message))

  (should-continue? [_]
    (and @running?
         (should-continue? base-handler))))

(defn create-transforming-handler
  "Create a handler that transforms messages before processing.

   The transform function is applied to each message before
   passing it to the base handler."
  [base-handler transform-fn running?]
  (->TransformingHandler base-handler transform-fn running?))
 (m/=> create-transforming-handler [:=> [:cat [:fn (fn* [p1__4386#] (satisfies? IMessageHandler p1__4386#))] [:=> [:cat :any] :any] [:fn (fn* [p1__4388#] (instance? Atom p1__4388#))]] [:fn (fn* [p1__4390#] (satisfies? IMessageHandler p1__4390#))]])

;; ============================================================================
;; Logging Handler
;; ============================================================================

(defrecord LoggingHandler [base-handler name level running?]
  IMessageHandler
  (handle-message [_ message]
    (t/log! level (str "[" name "] Processing message: " (pr-str message)))
    (let [result (handle-message base-handler message)]
      (t/log! level (str "[" name "] Message processed"))
      result))

  (on-error [_ error message]
    (t/log! :error (str "[" name "] Error: " (.getMessage error)))
    (on-error base-handler error message))

  (should-continue? [_]
    (and @running?
         (should-continue? base-handler))))

(defn create-logging-handler
  "Create a handler that logs messages before/after processing.

   Useful for debugging and monitoring."
  [base-handler name level running?]
  (->LoggingHandler base-handler name level running?))
 (m/=> create-logging-handler [:=> [:cat [:fn (fn* [p1__4404#] (satisfies? IMessageHandler p1__4404#))] :string [:enum :trace :debug :info :warn :error] [:fn (fn* [p1__4406#] (instance? Atom p1__4406#))]] [:fn (fn* [p1__4408#] (satisfies? IMessageHandler p1__4408#))]])
