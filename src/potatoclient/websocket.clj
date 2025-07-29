(ns potatoclient.websocket
  "WebSocket client implementation for command and state channels.
  Integrates Java WebSocket wrapper classes with the Clojure codebase."
  (:require [clojure.core.async :as async :refer [go-loop <! >! timeout]]
            [com.fulcrologic.guardrails.malli.core :refer [>defn >defn- =>]]
            [potatoclient.cmd.core :as cmd]
            [potatoclient.logging :as logging]
            [potatoclient.runtime :as runtime]
            [potatoclient.state.dispatch :as dispatch])
  (:import [potatoclient.java.websocket CommandWebSocketClient StateWebSocketClient]
           [cmd JonSharedCmd$Root JonSharedCmd$Ping]
           [ser JonSharedDataTypes$JonGUIState]))

;; ============================================================================
;; WebSocket Client Management
;; ============================================================================

(defonce ^:private clients (atom {}))

;; ============================================================================
;; Error Handling
;; ============================================================================

(>defn- handle-error
  "Handle WebSocket errors"
  [source error]
  [string? string? => nil?]
  (logging/log-error (str "WebSocket error [" source "]: " error))
  nil)

;; ============================================================================
;; State Processing
;; ============================================================================

(>defn- start-state-processor!
  "Start the state processing loop that polls the state queue"
  [^StateWebSocketClient state-client]
  [[:fn #(instance? StateWebSocketClient %)] => any?]
  (go-loop []
    (when-let [{:keys [state]} @clients]
      (try
        ;; Poll with 50ms timeout to prevent busy waiting
        (when-let [state-msg (.poll state-client 50 java.util.concurrent.TimeUnit/MILLISECONDS)]
          ;; Forward to state dispatch system
          (dispatch/handle-binary-state (.toByteArray state-msg)))
        (catch InterruptedException _
          ;; Exit loop on interrupt
          nil)
        (catch Exception e
          (logging/log-error (str "Error processing state: " (.getMessage e)))
          ;; Continue processing after error
          (<! (timeout 100))))
      ;; Continue loop if client still exists
      (when @clients
        (recur)))))

;; ============================================================================
;; Command Channel Integration
;; ============================================================================

(>defn- start-command-forwarder!
  "Forward commands from the cmd/core command channel to WebSocket"
  [^CommandWebSocketClient cmd-client]
  [[:fn #(instance? CommandWebSocketClient %)] => any?]
  (go-loop []
    (when-let [cmd-msg (<! cmd/command-channel)]
      (try
        ;; Validate in development/test only
        (when (runtime/development-build?)
          (try
            (CommandWebSocketClient/validate cmd-msg)
            (catch Exception e
              (logging/log-error (str "Command validation failed: " (.getMessage e)))
              (throw e))))
        
        ;; Send command
        (when-not (.send cmd-client cmd-msg)
          (logging/log-warn "Command queue full, message dropped"))
        
        (catch Exception e
          (logging/log-error (str "Error sending command: " (.getMessage e)))))
      
      ;; Continue forwarding
      (when @clients
        (recur)))))

;; ============================================================================
;; Public API
;; ============================================================================

(>defn start!
  "Start WebSocket connections for command and state channels"
  [domain]
  [string? => map?]
  (logging/log-info (str "Starting WebSocket connections to " domain))
  
  (let [;; Create command client
        cmd-client (CommandWebSocketClient. 
                     domain
                     (partial handle-error "command"))
        
        ;; Create state client
        state-client (StateWebSocketClient.
                       domain
                       (partial handle-error "state"))
        
        ;; Start processors
        state-processor (start-state-processor! state-client)
        cmd-forwarder (start-command-forwarder! cmd-client)]
    
    ;; Start both managers (they handle reconnection)
    (.start cmd-client)
    (.start state-client)
    
    ;; Store everything
    (reset! clients
            {:command cmd-client
             :state state-client
             :state-processor state-processor
             :cmd-forwarder cmd-forwarder})
    
    ;; Return API map
    {:stop #(stop!)
     :connected? #(connected?)
     :command-queue-size #(command-queue-size)
     :state-queue-size #(state-queue-size)}))

(>defn stop!
  "Stop all WebSocket connections and processors"
  []
  [=> nil?]
  (when-let [{:keys [command state state-processor cmd-forwarder]} @clients]
    (logging/log-info "Stopping WebSocket connections")
    
    ;; Stop processors by closing their channels
    (when state-processor
      (async/close! state-processor))
    (when cmd-forwarder
      (async/close! cmd-forwarder))
    
    ;; Stop WebSocket managers
    (.stop command)
    (.stop state)
    
    ;; Clear clients
    (reset! clients {}))
  nil)

(>defn connected?
  "Check if both WebSocket connections are active"
  []
  [=> boolean?]
  (let [{:keys [command state]} @clients]
    (and command state
         (.isConnected command)
         (.isConnected state))))

(>defn command-queue-size
  "Get the number of commands in the queue"
  []
  [=> int?]
  (if-let [client (:command @clients)]
    (.getQueueSize client)
    0))

(>defn state-queue-size
  "Get the number of state messages in the queue"
  []
  [=> int?]
  (if-let [client (:state @clients)]
    (.getQueueSize client)
    0))

;; ============================================================================
;; Periodic Tasks
;; ============================================================================

(>defn- start-ping-task!
  "Start periodic ping task (handled by CommandWebSocketClient internally)"
  []
  [=> nil?]
  ;; The CommandWebSocketClient handles pings internally when connected
  ;; This is just a placeholder if we need additional ping logic
  nil)