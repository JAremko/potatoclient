(ns potatoclient.cmd.core
  "Core infrastructure for command sending with queue and validation.
   Commands are validated, queued, and consumed by a WebSocket sender.
   The reader blocks with timeout and sends ping to maintain connection.
   
   Uses Pronto efficiently based on performance guidelines:
   - Creates proto-maps with initial values (fastest)
   - Uses p-> for multiple mutations
   - Avoids repeated assoc operations"
  (:require
   [pronto.core :as p]
   [potatoclient.proto.serialize :as serialize]
   [potatoclient.proto.deserialize :as deserialize]
   [potatoclient.cmd.validation :as validation]
   [potatoclient.cmd.builder :as builder]
   [potatoclient.malli.registry :as registry]
   [potatoclient.specs.cmd.root]) ; Load the specs
  (:import
   [java.util.concurrent LinkedBlockingQueue TimeUnit]))

;; Initialize registry to access specs
(registry/setup-global-registry!)

;; ============================================================================
;; Test Mode Detection
;; ============================================================================

(def ^:private test-mode?
  "Check if we're running in test mode based on system property or environment."
  (or (= "test" (System/getProperty "potatoclient.mode"))
      (= "test" (System/getenv "POTATOCLIENT_MODE"))
      ;; Check if test alias is active via classpath
      (boolean (some #(re-find #"test" %) 
                     (seq (.split (System/getProperty "java.class.path") ":"))))))

;; ============================================================================
;; Command Queue
;; ============================================================================

(defonce ^LinkedBlockingQueue command-queue
  (LinkedBlockingQueue.))



;; ============================================================================
;; Test Support - Roundtrip Validation
;; ============================================================================

(defn- validate-roundtrip!
  "Validate that a command survives serialization/deserialization.
   Used in test mode to ensure correctness.
   Uses normalized comparison with proto template."
  {:malli/schema [:=> [:cat :cmd/root] :nil]}
  [full-cmd]
  (validation/assert-roundtrip full-cmd))

;; ============================================================================
;; Core Send Function - Automatically uses test mode when in test alias
;; ============================================================================

(defn send-command!
  "Enqueue a fully formed command for sending.
   Takes a complete cmd root and adds it to the queue.
   In production, returns nil after enqueueing.
   In test mode, skips enqueueing but still validates.
   Throws if validation fails."
  {:malli/schema [:=> [:cat :cmd/root] :nil]}
  [full-cmd]
  ;; Validate with Malli and buf.validate via serialize
  ;; This will throw if validation fails
  (serialize/serialize-cmd-payload full-cmd)
  
  ;; In test mode, also validate roundtrip
  (when test-mode?
    (validate-roundtrip! full-cmd))
  
  ;; If validation passed, add to queue (unless in test mode)
  (when-not test-mode?
    (.offer command-queue full-cmd))
  
  ;; Always return nil
  nil)


(defn create-command
  "Create a fully populated command from a payload.
   Takes a cmd payload map (just the oneof part) and returns the full command.
   This is what command functions should use to build their return values."
  {:malli/schema [:=> [:cat :map] :cmd/root]}
  [cmd-payload]
  ;; Simple map check instead of complex :or spec
  (builder/populate-cmd-fields cmd-payload))

;; ============================================================================
;; Ping Command for Keep-Alive
;; ============================================================================

(defn create-ping-command
  "Create a ping command to keep connection alive.
   Returns the full command with all protocol fields."
  {:malli/schema [:=> [:cat] :cmd/root]}
  []
  (builder/populate-cmd-fields {:ping {}}))

;; ============================================================================
;; Queue Reader Functions
;; ============================================================================

(defn poll-command-with-timeout
  "Poll for the next command with timeout.
   If timeout expires, returns a ping command.
   Timeout is in milliseconds."
  {:malli/schema [:=> [:cat :nat-int] :cmd/root]}
  [timeout-ms]
  (or (.poll command-queue timeout-ms TimeUnit/MILLISECONDS)
      ;; Timeout - send ping to keep connection alive
      (create-ping-command)))

(defn take-next-command
  "Take the next command, blocking up to 1 second.
   Returns ping command if timeout."
  {:malli/schema [:=> [:cat] :cmd/root]}
  []
  (poll-command-with-timeout 1000))

;; ============================================================================
;; Queue Management
;; ============================================================================

(defn clear-queue!
  "Clear all pending commands from the queue."
  {:malli/schema [:=> [:cat] :nil]}
  []
  (.clear command-queue)
  nil)

(defn queue-size
  "Get the current number of commands in the queue."
  {:malli/schema [:=> [:cat] :nat-int]}
  []
  (.size command-queue))

;; ============================================================================
;; WebSocket Consumer (to be called from WebSocket thread)
;; ============================================================================

(defn consume-commands
  "Continuously consume commands from queue and send via WebSocket.
   This should be called from a dedicated thread.
   The send-fn is a function that takes binary data and sends it.
   Returns a function to stop the consumer loop."
  {:malli/schema [:=> [:cat fn?] fn?]}
  [send-fn]
  (let [running (atom true)]
    (future
      (while @running
        (try
          (let [cmd (take-next-command)
                ;; Use fast serialization without validation for sending
                ;; (already validated when queued)
                binary (serialize/serialize-cmd-payload* cmd)]
            (send-fn binary))
          (catch InterruptedException _
            ;; Normal shutdown
            (reset! running false))
          (catch Exception e
            ;; Log error but continue - don't let one failure stop the loop
            (println "Error sending command:" (.getMessage e))))))
    ;; Return stop function
    (fn stop-consumer []
      (reset! running false))))

;; ============================================================================
;; Test Helpers
;; ============================================================================

(defn in-test-mode?
  "Check if running in test mode."
  {:malli/schema [:=> [:cat] :boolean]}
  []
  test-mode?)