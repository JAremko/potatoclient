(ns potatoclient.transit.websocket-manager
  "Transit-based WebSocket management that replaces direct protobuf usage.
  
  This namespace provides a clean API for command sending and state receiving
  through Transit subprocesses, completely isolating protobuf handling."
  (:require [clojure.core.async :as async :refer [go-loop <! >! timeout]]
            [com.fulcrologic.guardrails.malli.core :refer [=> >defn >defn- ?]]
            [potatoclient.logging :as logging]
            [potatoclient.transit.subprocess-launcher :as launcher]
            [potatoclient.transit.app-db :as app-db]
            [potatoclient.transit.handlers :as handlers]
            [potatoclient.specs :as specs]))

;; Subprocess references
(def ^:private subprocesses (atom {:command nil
                                    :state nil}))

;; Configuration
(def ^:private reconnect-delay-ms 5000)
(def ^:private health-check-interval-ms 10000)

;; Read-only mode
(def ^:private read-only-mode? (atom false))

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

(>defn- build-ws-urls
  "Build WebSocket URLs for command and state channels"
  [domain]
  [::specs/domain => [:map [:command string?] [:state string?]]]
  {:command (str "wss://" domain "/protojson/cmd")
   :state (str "wss://" domain "/protojson/state")})

(>defn- start-state-handler!
  "Start the state message handler goroutine"
  [subprocess]
  [:potatoclient.specs.transit/subprocess => nil?]
  (go-loop []
    (when-let [msg (<! (:output-chan subprocess))]
      (try
        (handlers/handle-message msg)
        (catch Exception e
          (logging/log-error "Error handling state message" {:error e})))
      (recur)))
  nil)

(>defn- start-command-handler!
  "Start the command response handler goroutine"
  [subprocess]
  [:potatoclient.specs.transit/subprocess => nil?]
  (go-loop []
    (when-let [msg (<! (:output-chan subprocess))]
      (try
        (when (= (:msg-type msg) "response")
          (logging/log-debug "Command response" {:response msg}))
        (when (= (:msg-type msg) "validation-error")
          (logging/log-error "Command validation error" {:error msg}))
        (catch Exception e
          (logging/log-error "Error handling command response" {:error e})))
      (recur)))
  nil)

(>defn- monitor-subprocess-health!
  "Monitor subprocess health and restart if needed"
  [subprocess-type ws-url]
  [[:enum :command :state] string? => nil?]
  (go-loop []
    (<! (timeout health-check-interval-ms))
    (when-let [subprocess (get @subprocesses subprocess-type)]
      (if (launcher/subprocess-alive? subprocess)
        (recur)
        (do
          (logging/log-warn "Subprocess died, restarting" {:type subprocess-type})
          (try
            (let [new-subprocess (case subprocess-type
                                   :command (launcher/start-command-subprocess ws-url)
                                   :state (launcher/start-state-subprocess ws-url))]
              (swap! subprocesses assoc subprocess-type new-subprocess)
              (case subprocess-type
                :command (start-command-handler! new-subprocess)
                :state (start-state-handler! new-subprocess)))
            (catch Exception e
              (logging/log-error "Failed to restart subprocess" {:type subprocess-type
                                                                  :error e})))
          (recur)))))
  nil)

(>defn init!
  "Initialize Transit WebSocket connections"
  [domain]
  [::specs/domain => nil?]
  (logging/log-info "Initializing Transit WebSocket manager" {:domain domain})
  
  ;; Stop existing subprocesses
  (stop!)
  
  ;; Build URLs
  (let [{:keys [command state]} (build-ws-urls domain)]
    (try
      ;; Start subprocesses
      (let [cmd-subprocess (launcher/start-command-subprocess command)
            state-subprocess (launcher/start-state-subprocess state)]
        
        ;; Store references
        (reset! subprocesses {:command cmd-subprocess
                              :state state-subprocess})
        
        ;; Start handlers
        (start-command-handler! cmd-subprocess)
        (start-state-handler! state-subprocess)
        
        ;; Start health monitoring
        (monitor-subprocess-health! :command command)
        (monitor-subprocess-health! :state state)
        
        ;; Update app-db connection status
        (app-db/update-connection! {:url domain
                                    :connected? true
                                    :latency-ms nil
                                    :reconnect-count 0})
        
        (logging/log-info "Transit WebSocket manager initialized successfully"))
      
      (catch Exception e
        (logging/log-error "Failed to initialize Transit WebSocket manager" {:error e})
        (app-db/update-connection! {:url domain
                                    :connected? false
                                    :latency-ms nil
                                    :reconnect-count 0}))))
  nil)

(>defn stop!
  "Stop all WebSocket connections"
  []
  [=> nil?]
  (logging/log-info "Stopping Transit WebSocket manager")
  
  ;; Stop subprocesses
  (doseq [[type subprocess] @subprocesses]
    (when subprocess
      (try
        (launcher/stop-subprocess! subprocess)
        (catch Exception e
          (logging/log-error "Error stopping subprocess" {:type type
                                                           :error e})))))
  
  ;; Clear references
  (reset! subprocesses {:command nil
                        :state nil})
  
  ;; Update app-db
  (app-db/update-connection! {:connected? false})
  nil)

(>defn send-command!
  "Send a command through the Transit command subprocess"
  [command-map]
  [map? => boolean?]
  ;; Check read-only mode
  (when (and (is-read-only-mode?)
             (not (#{:ping :frozen} (:action command-map))))
    (logging/log-warn "Command blocked in read-only mode" {:command (:action command-map)})
    false)
  
  ;; Send command
  (if-let [cmd-subprocess (:command @subprocesses)]
    (try
      (launcher/send-message! cmd-subprocess
                              {:msg-type "command"
                               :msg-id (str (java.util.UUID/randomUUID))
                               :timestamp (System/currentTimeMillis)
                               :payload command-map})
      true
      (catch Exception e
        (logging/log-error "Failed to send command" {:error e
                                                      :command command-map})
        false))
    (do
      (logging/log-error "No command subprocess available")
      false)))

(>defn set-state-rate-limit!
  "Set the rate limit for state updates"
  [rate-hz]
  [pos-int? => nil?]
  (when-let [state-subprocess (:state @subprocesses)]
    (launcher/send-message! state-subprocess
                            {:msg-type "control"
                             :msg-id (str (java.util.UUID/randomUUID))
                             :timestamp (System/currentTimeMillis)
                             :payload {:action "set-rate-limit"
                                       :max-hz rate-hz}}))
  nil)

(>defn get-subprocess-metrics
  "Get metrics from both subprocesses"
  []
  [=> [:map [:command (? map?)] [:state (? map?)]]]
  (let [get-metrics (fn [subprocess]
                      (when subprocess
                        (try
                          (launcher/send-message! subprocess
                                                  {:msg-type "control"
                                                   :msg-id (str (java.util.UUID/randomUUID))
                                                   :timestamp (System/currentTimeMillis)
                                                   :payload {:action "get-metrics"}})
                          (launcher/read-message subprocess 1000)
                          (catch Exception e
                            (logging/log-error "Failed to get metrics" {:error e})
                            nil))))]
    {:command (get-metrics (:command @subprocesses))
     :state (get-metrics (:state @subprocesses))}))