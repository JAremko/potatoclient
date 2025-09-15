(ns potatoclient.ipc.handlers
  "Common IPC message handler patterns and utilities.

   Provides reusable patterns for creating message handlers,
   processing queues, and managing IPC communication."
  (:require
    [potatoclient.logging :as logging])
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

(defrecord ^{:doc "Base implementation of IMessageHandler protocol.
   
   Fields:
   - name: String identifier for logging and debugging
   - handler-fn: Function (message) -> any, processes each message
   - error-fn: Optional function (error, message) -> any, handles processing errors
   - running?: Atom boolean, controls whether handler continues processing"}
  BaseMessageHandler [name handler-fn error-fn running?]
  IMessageHandler
  (handle-message [_ message]
    (when handler-fn
      (handler-fn message)))

  (on-error [_ error message]
    (if error-fn
      (error-fn error message)
      (logging/log-error (str "[" name "] Error processing message: " (.getMessage error)))))

  (should-continue? [_]
    @running?))

(defn create-handler
  "Create a message handler with the given configuration.

   Options:
   - :name - Handler name for logging
   - :handler-fn - Function to process messages (required)
   - :error-fn - Function to handle errors (optional)
   - :running? - Atom tracking if handler should continue (required)"
  {:malli/schema [:=> [:cat [:map
                             [:name :string]
                             [:handler-fn [:=> [:cat :any] :any]]
                             [:error-fn {:optional true} [:=> [:cat :any :any] :any]]
                             [:running? [:fn (partial instance? Atom)]]]]
                  [:fn #(satisfies? IMessageHandler %)]]}
  [{:keys [name handler-fn error-fn running?]}]
  (->BaseMessageHandler name handler-fn error-fn running?))

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
  {:malli/schema [:=> [:cat [:map
                             [:queue [:fn (partial instance? LinkedBlockingQueue)]]
                             [:handler [:fn #(satisfies? IMessageHandler %)]]
                             [:poll-timeout-ms {:optional true} pos-int?]
                             [:error-delay-ms {:optional true} pos-int?]]]
                  :nil]}
  [{:keys [^LinkedBlockingQueue queue handler poll-timeout-ms error-delay-ms]
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
          (Thread/sleep ^long error-delay-ms)))))
  nil)

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
  {:malli/schema [:=> [:cat [:map
                             [:name :string]
                             [:queue [:fn (partial instance? LinkedBlockingQueue)]]
                             [:handler [:fn #(satisfies? IMessageHandler %)]]
                             [:daemon? {:optional true} :boolean]]]
                  [:fn (partial instance? Thread)]]}
  ^Thread [{:keys [name queue handler daemon?]
            :or {daemon? true}}]
  (let [thread (Thread.
                 ^Runnable (fn []
                             (logging/log-info (str "[" name "] Processor thread started"))
                             (process-queue {:queue queue
                                             :handler handler})
                             (logging/log-info (str "[" name "] Processor thread stopped")))
                 ^String name)]
    (when daemon?
      (.setDaemon thread true))
    thread))

;; ============================================================================
;; Composite Handlers
;; ============================================================================

(defrecord ^{:doc "Handler that delegates message processing to multiple sub-handlers.
   
   Each handler processes messages independently and errors are isolated.
   Useful for fan-out processing where multiple consumers need the same message.
   
   Fields:
   - handlers: Vector of IMessageHandler implementations to delegate to
   - running?: Atom boolean, controls whether composite handler continues"}
  CompositeHandler [handlers running?]
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
  {:malli/schema [:=> [:cat
                       [:vector [:fn #(satisfies? IMessageHandler %)]]
                       [:fn (partial instance? Atom)]]
                  [:fn #(satisfies? IMessageHandler %)]]}
  [handlers running?]
  (->CompositeHandler handlers running?))

;; ============================================================================
;; Filtering Handlers
;; ============================================================================

(defrecord ^{:doc "Handler wrapper that filters messages before delegating to base handler.
   
   Only messages passing the filter predicate are processed.
   Useful for selective message processing based on content or type.
   
   Fields:
   - base-handler: IMessageHandler to delegate filtered messages to
   - filter-fn: Predicate (message) -> boolean, determines which messages to process
   - running?: Atom boolean, controls handler continuation"}
  FilteringHandler [base-handler filter-fn running?]
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
  {:malli/schema [:=> [:cat
                       [:fn #(satisfies? IMessageHandler %)]
                       [:=> [:cat :any] :boolean]
                       [:fn (partial instance? Atom)]]
                  [:fn #(satisfies? IMessageHandler %)]]}
  [base-handler filter-fn running?]
  (->FilteringHandler base-handler filter-fn running?))

;; ============================================================================
;; Transforming Handlers
;; ============================================================================

(defrecord ^{:doc "Handler wrapper that transforms messages before delegating to base handler.
   
   Applies transformation function to each message before processing.
   Useful for message format conversion or enrichment.
   
   Fields:
   - base-handler: IMessageHandler to receive transformed messages
   - transform-fn: Function (message) -> transformed-message
   - running?: Atom boolean, controls handler continuation"}
  TransformingHandler [base-handler transform-fn running?]
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
  {:malli/schema [:=> [:cat
                       [:fn #(satisfies? IMessageHandler %)]
                       [:=> [:cat :any] :any]
                       [:fn (partial instance? Atom)]]
                  [:fn #(satisfies? IMessageHandler %)]]}
  [base-handler transform-fn running?]
  (->TransformingHandler base-handler transform-fn running?))

;; ============================================================================
;; Logging Handler
;; ============================================================================

(defrecord ^{:doc "Handler wrapper that logs messages before and after processing.
   
   Provides visibility into message flow for debugging and monitoring.
   Log level can be configured per handler instance.
   
   Fields:
   - base-handler: IMessageHandler to delegate actual processing to  
   - name: String identifier used in log messages
   - level: Keyword log level (:trace :debug :info :warn :error)
   - running?: Atom boolean, controls handler continuation"}
  LoggingHandler [base-handler name level running?]
  IMessageHandler
  (handle-message [_ message]
    (case level
      :debug (logging/log-debug (str "[" name "] Processing message: " (pr-str message)))
      :info (logging/log-info (str "[" name "] Processing message: " (pr-str message)))
      :warn (logging/log-warn (str "[" name "] Processing message: " (pr-str message)))
      :error (logging/log-error (str "[" name "] Processing message: " (pr-str message)))
      (logging/log-debug (str "[" name "] Processing message: " (pr-str message))))
    (let [result (handle-message base-handler message)]
      (case level
        :debug (logging/log-debug (str "[" name "] Message processed"))
        :info (logging/log-info (str "[" name "] Message processed"))
        :warn (logging/log-warn (str "[" name "] Message processed"))
        :error (logging/log-error (str "[" name "] Message processed"))
        (logging/log-debug (str "[" name "] Message processed")))
      result))

  (on-error [_ error message]
    (logging/log-error (str "[" name "] Error: " (.getMessage error)))
    (on-error base-handler error message))

  (should-continue? [_]
    (and @running?
         (should-continue? base-handler))))

(defn create-logging-handler
  "Create a handler that logs messages before/after processing.

   Useful for debugging and monitoring."
  {:malli/schema [:=> [:cat
                       [:fn #(satisfies? IMessageHandler %)]
                       :string
                       [:enum :trace :debug :info :warn :error]
                       [:fn (partial instance? Atom)]]
                  [:fn #(satisfies? IMessageHandler %)]]}
  [base-handler name level running?]
  (->LoggingHandler base-handler name level running?))
