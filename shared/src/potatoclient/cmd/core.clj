(ns potatoclient.cmd.core
  "Core infrastructure for command sending with queue and validation.
   Commands are validated, queued, and consumed by a WebSocket sender.
   The reader blocks with timeout and sends ping to maintain connection.
   
   Uses Pronto efficiently based on performance guidelines:
   - Creates proto-maps with initial values (fastest)
   - Uses p-> for multiple mutations
   - Avoids repeated assoc operations"
  (:require
   [com.fulcrologic.guardrails.core :refer [>defn >defn- => | ?]]
   [clojure.spec.alpha :as s]
   [pronto.core :as p]
   [potatoclient.proto.serialize :as serialize]
   [potatoclient.proto.deserialize :as deserialize]
   [potatoclient.cmd.validation :as validation]
   [potatoclient.cmd.builder :as builder])
  (:import
   [java.util.concurrent LinkedBlockingQueue TimeUnit]))

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
;; Specs
;; ============================================================================

(s/def ::cmd-edn (s/and map? 
                        #(contains? % :protocol_version) 
                        #(contains? % :client_type)))

(s/def ::cmd-root (s/and map?
                         #(some? (dissoc % :protocol_version :client_type 
                                        :session_id :important :from_cv_subsystem))))

(s/def ::bytes bytes?)


;; ============================================================================
;; Test Support - Roundtrip Validation
;; ============================================================================

(>defn- validate-roundtrip!
  "Validate that a command survives serialization/deserialization.
   Used in test mode to ensure correctness.
   Uses normalized comparison with proto template."
  [full-cmd]
  [::cmd-edn => nil?]
  (validation/assert-roundtrip full-cmd))

;; ============================================================================
;; Core Send Function - Automatically uses test mode when in test alias
;; ============================================================================

(>defn send-command!
  "Validate and enqueue a command for sending.
   This is the main function that all cmd functions call.
   Takes a cmd payload map (just the oneof part) and returns the full command.
   In test mode, also validates roundtrip serialization.
   Throws if validation fails."
  [cmd-payload]
  [::cmd-root => map?]
  (let [full-cmd (builder/populate-cmd-fields cmd-payload)]
    ;; Validate with Malli and buf.validate via serialize
    ;; This will throw if validation fails
    (serialize/serialize-cmd-payload full-cmd)
    
    ;; In test mode, also validate roundtrip
    (when test-mode?
      (validate-roundtrip! full-cmd))
    
    ;; If validation passed, add to queue (unless in test mode)
    (when-not test-mode?
      (.offer command-queue full-cmd))
    
    ;; Return the full command with all fields
    full-cmd))

(>defn send-command-with-session!
  "Send a command with a specific session ID.
   Used when session tracking is important.
   Returns the full command with all fields."
  [cmd-payload session-id]
  [::cmd-root pos-int? => map?]
  (let [full-cmd (builder/populate-cmd-fields-with-overrides 
                   cmd-payload
                   {:session_id session-id})]
    ;; Validate with Malli and buf.validate via serialize
    (serialize/serialize-cmd-payload full-cmd)
    
    ;; In test mode, also validate roundtrip
    (when test-mode?
      (validate-roundtrip! full-cmd))
    
    ;; If validation passed, add to queue (unless in test mode)
    (when-not test-mode?
      (.offer command-queue full-cmd))
    
    ;; Return the full command
    full-cmd))

(>defn send-important-command!
  "Send a command marked as important.
   Important commands may have different handling on the server.
   Returns the full command with all fields."
  [cmd-payload]
  [::cmd-root => map?]
  (let [full-cmd (builder/populate-cmd-fields-with-overrides 
                   cmd-payload
                   {:important true})]
    ;; Validate with Malli and buf.validate via serialize
    (serialize/serialize-cmd-payload full-cmd)
    
    ;; In test mode, also validate roundtrip
    (when test-mode?
      (validate-roundtrip! full-cmd))
    
    ;; If validation passed, add to queue (unless in test mode)
    (when-not test-mode?
      (.offer command-queue full-cmd))
    
    ;; Return the full command
    full-cmd))

;; ============================================================================
;; Ping Command for Keep-Alive
;; ============================================================================

(>defn create-ping-command
  "Create a ping command to keep connection alive.
   Returns the full command with all protocol fields."
  []
  [=> ::cmd-edn]
  (builder/populate-cmd-fields {:ping {}}))

;; ============================================================================
;; Queue Reader Functions
;; ============================================================================

(>defn poll-command-with-timeout
  "Poll for the next command with timeout.
   If timeout expires, returns a ping command.
   Timeout is in milliseconds."
  [timeout-ms]
  [pos-int? => ::cmd-edn]
  (or (.poll command-queue timeout-ms TimeUnit/MILLISECONDS)
      ;; Timeout - send ping to keep connection alive
      (create-ping-command)))

(>defn take-next-command
  "Take the next command, blocking up to 1 second.
   Returns ping command if timeout."
  []
  [=> ::cmd-edn]
  (poll-command-with-timeout 1000))

;; ============================================================================
;; Queue Management
;; ============================================================================

(>defn clear-queue!
  "Clear all pending commands from the queue."
  []
  [=> nil?]
  (.clear command-queue)
  nil)

(>defn queue-size
  "Get the current number of commands in the queue."
  []
  [=> nat-int?]
  (.size command-queue))

;; ============================================================================
;; WebSocket Consumer (to be called from WebSocket thread)
;; ============================================================================

(>defn consume-commands
  "Continuously consume commands from queue and send via WebSocket.
   This should be called from a dedicated thread.
   The send-fn is a function that takes binary data and sends it.
   Returns a function to stop the consumer loop."
  [send-fn]
  [(s/fspec :args (s/cat :data ::bytes)) => fn?]
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

(>defn in-test-mode?
  "Check if running in test mode."
  []
  [=> boolean?]
  test-mode?)