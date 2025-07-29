(ns potatoclient.cmd.core
  "Core command sending infrastructure for PotatoClient.
   Implements the command pattern from the TypeScript web example."
  (:require [clojure.string :as str]
            [com.fulcrologic.guardrails.malli.core :refer [=> >defn >defn- ?]]
            [potatoclient.logging :as logging]
            [potatoclient.runtime :as runtime])
  (:import (cmd JonSharedCmd$Frozen
                JonSharedCmd$Noop JonSharedCmd$Ping JonSharedCmd$Root)
           (cmd.RotaryPlatform JonSharedCmdRotary$Axis JonSharedCmdRotary$Root)
           (ser JonSharedDataTypes$JonGuiDataClientType)))

;; ============================================================================
;; Configuration
;; ============================================================================

(def ^:const PROTOCOL_VERSION
  "Protocol version for command communication"
  1)

;; WebSocket manager reference
(defonce ^:private websocket-manager (atom nil))

;; Read-only mode atom
(def ^:private read-only-mode? (atom false))

;; ============================================================================
;; WebSocket Management
;; ============================================================================

(>defn init-websocket!
  "Initialize WebSocket manager - called from core.clj"
  [domain error-callback state-callback]
  [string? ifn? ifn? => nil?]
  (when-let [old-manager @websocket-manager]
    (.stop old-manager))
  (let [manager (potatoclient.java.websocket.WebSocketManager. 
                  domain 
                  (reify java.util.function.Consumer
                    (accept [_ msg] (error-callback msg)))
                  (reify java.util.function.Consumer
                    (accept [_ data] (state-callback data))))]
    (.start manager)
    (reset! websocket-manager manager))
  nil)

(>defn stop-websocket!
  "Stop WebSocket connections"
  []
  [=> nil?]
  (when-let [manager @websocket-manager]
    (.stop manager)
    (reset! websocket-manager nil))
  nil)

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
;; Command Sending
;; ============================================================================

(>defn send-cmd-message
  "Send a command message through WebSocket"
  [root-msg]
  [:potatoclient.specs/cmd-root-builder => nil?]
  ;; In readonly mode, only allow ping and frozen messages
  (when (or (not (is-read-only-mode?))
            (.hasPing root-msg)
            (.hasFrozen root-msg))
    (when-let [manager @websocket-manager]
      (let [command (.build root-msg)]
        (.sendCommand manager command))))
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
                            (.setAxis ^JonSharedCmdRotary$Axis (.build axis-msg)))]
        (.setRotary root-msg rotary-root)
        (send-cmd-message root-msg))))
  nil)


;; ============================================================================
;; Initialization
;; ============================================================================

