(ns potatoclient.cmd.core
  "Core command sending infrastructure for PotatoClient.
   Implements the command pattern from the TypeScript web example."
  (:require [clojure.core.async :as async]
            [clojure.string :as str]
            [potatoclient.logging :as logging]
            [potatoclient.runtime :as runtime]
            [com.fulcrologic.guardrails.malli.core :as gr :refer [>defn >defn- >def | ? =>]])
  (:import [cmd JonSharedCmd$Root
            JonSharedCmd$Ping JonSharedCmd$Frozen JonSharedCmd$Noop]
           [cmd.RotaryPlatform JonSharedCmdRotary$Root JonSharedCmdRotary$Axis]
           [data JonSharedDataTypes$JonGuiDataClientType]
           [com.google.protobuf.util JsonFormat]))

;; ============================================================================
;; Configuration
;; ============================================================================

(def ^:const PROTOCOL_VERSION 1)

;; Channel for outgoing commands
(def command-channel (async/chan 100))

;; Read-only mode atom
(def ^:private read-only-mode? (atom false))

;; ============================================================================
;; Helper Functions
;; ============================================================================

(>defn set-read-only-mode!
  "Set the read-only mode state"
  [value]
  [boolean? => nil?]
  (reset! read-only-mode? value)
  nil)

(>defn- is-read-only-mode?
  "Check if we're in read-only mode"
  []
  [=> boolean?]
  @read-only-mode?)

;; ============================================================================
;; Core Message Creation
;; ============================================================================

(>defn create-root-message
  "Create a new Root command message builder"
  []
  [=> :potatoclient.specs/cmd-root-builder]
  (-> (JonSharedCmd$Root/newBuilder)
      (.setProtocolVersion PROTOCOL_VERSION)
      (.setClientType JonSharedDataTypes$JonGuiDataClientType/JON_GUI_DATA_CLIENT_TYPE_LOCAL_NETWORK)))

(>defn create-axis-message
  "Create a new Axis message builder for rotary platform"
  []
  [=> :potatoclient.specs/rotary-axis-builder]
  (JonSharedCmdRotary$Axis/newBuilder))

;; ============================================================================
;; Message Encoding
;; ============================================================================

(>defn encode-cmd-message
  "Encode a Root command message to bytes"
  [root-msg]
  [:potatoclient.specs/cmd-root-builder => bytes?]
  (.toByteArray (.build root-msg)))

;; ============================================================================
;; Command Sending
;; ============================================================================

(>defn- should-buffer?
  "Determine if a command should be buffered"
  [root-msg]
  [:potatoclient.specs/cmd-root-builder => boolean?]
  (not (.hasPing root-msg)))

(>defn send-cmd-message
  "Send a command message through the command channel"
  [root-msg]
  [:potatoclient.specs/cmd-root-builder => nil?]
  ;; In readonly mode, only allow ping and frozen messages
  (when (or (not (is-read-only-mode?))
            (.hasPing root-msg)
            (.hasFrozen root-msg))
    (let [encoded-message (encode-cmd-message root-msg)
          should-buffer (should-buffer? root-msg)]
      (async/put! command-channel
                  {:pld encoded-message
                   :should-buffer should-buffer})))
  nil)

;; ============================================================================
;; Basic Commands
;; ============================================================================

(>defn send-cmd-ping
  "Send a ping command (allowed in readonly mode)"
  []
  [=> nil?]
  (let [root-msg (create-root-message)]
    (.setPing root-msg (JonSharedCmd$Ping/newBuilder))
    (send-cmd-message root-msg))
  nil)

(>defn send-cmd-frozen
  "Send a frozen command (allowed in readonly mode)"
  []
  [=> nil?]
  (let [root-msg (create-root-message)]
    (.setFrozen root-msg (JonSharedCmd$Frozen/newBuilder))
    (send-cmd-message root-msg))
  nil)

(>defn send-cmd-noop
  "Send a no-op command"
  []
  [=> nil?]
  (let [root-msg (create-root-message)]
    (.setNoop root-msg (JonSharedCmd$Noop/newBuilder))
    (send-cmd-message root-msg))
  nil)

;; ============================================================================
;; Rotary Axis Commands
;; ============================================================================

(>defn send-rotary-axis-command
  "Send a rotary platform axis command with azimuth and/or elevation"
  [{:keys [azimuth elevation]}]
  [:potatoclient.specs/rotary-axis-command-map => nil?]
  (when-not (is-read-only-mode?)
    (let [axis-msg (create-axis-message)]
      (when azimuth
        (.setAzimuth axis-msg azimuth))
      (when elevation
        (.setElevation axis-msg elevation))

      (let [root-msg (create-root-message)
            rotary-root (-> (JonSharedCmdRotary$Root/newBuilder)
                            (.setAxis axis-msg))]
        (.setRotary root-msg rotary-root)
        (send-cmd-message root-msg))))
  nil)

;; ============================================================================
;; Debug Utilities (Development Mode Only)
;; ============================================================================

(>defn- decode-proto-to-json
  "Decode protobuf bytes to JSON string for debugging"
  [proto-bytes]
  [bytes? => (? string?)]
  (try
    (let [root-msg (JonSharedCmd$Root/parseFrom proto-bytes)]
      (-> (JsonFormat/printer)
          (.includingDefaultValueFields)
          (.print root-msg)))
    (catch Exception e
      (logging/log-error {:id ::decode-proto-failed
                          :error (.getMessage e)})
      nil)))

;; ============================================================================
;; Command Channel Reader
;; ============================================================================

(>defn- format-command-as-json
  "Format a command for JSON output"
  [{:keys [pld should-buffer]}]
  [:potatoclient.specs/command-payload-map => string?]
  (let [base64-payload (.encodeToString (java.util.Base64/getEncoder) pld)]
    (str/join "\n"
              ["{"
               (str "  \"payload\": \"" base64-payload "\",")
               (str "  \"shouldBuffer\": " should-buffer ",")
               (str "  \"size\": " (count pld))
               "}"])))

(>defn start-command-reader!
  "Start a go-loop that reads from the command channel and prints as JSON"
  []
  [=> :potatoclient.specs/core-async-channel]
  (async/go-loop []
    (when-let [cmd (async/<! command-channel)]
      ;; In development mode, decode and log the protobuf structure
      (when-not runtime/release-build?
        (when-let [proto-json (decode-proto-to-json (:pld cmd))]
          (logging/log-info {:id ::command-proto-structure
                             :type "command"
                             :json proto-json
                             :size (count (:pld cmd))})))

      ;; Always output the transport format (for future websocket)
      (println "Command sent:")
      (println (format-command-as-json cmd))
      (println "---")
      (recur))))

;; ============================================================================
;; Initialization
;; ============================================================================

(>defn init!
  "Initialize the command system"
  []
  [=> nil?]
  (println "Command system initialized")
  (start-command-reader!)
  nil)