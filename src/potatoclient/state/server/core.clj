(ns potatoclient.state.server.core
  "Core namespace for server state ingress.
   Provides high-level API for managing state synchronization with server."
  (:require [potatoclient.logging :as logging]
            [potatoclient.state.server.ingress :as ingress]
            [malli.core :as m]))

(defonce ^:private manager (atom nil))

(defn initialized?
  "Check if state ingress is initialized."
  []
  (some? @manager))
(m/=> initialized? [:=> [:cat] :boolean])

(defn running?
  "Check if state ingress is running."
  []
  (and @manager @(:running? @manager)))
(m/=> running? [:=> [:cat] :boolean])

(defn connected?
  "Check if state ingress is connected to the WebSocket."
  []
  (and @manager (ingress/connected? @manager)))

(defn initialize!
  "Initialize state ingress with domain.
   
   Options:
   - :domain - Server domain (required)
   - :throttle-ms - Update throttle interval (default 100ms)
   - :timeout-ms - Connection timeout (default 2000ms)
   
   This should be called during startup before showing main frame."
  [{:keys [domain throttle-ms timeout-ms]
    :or {throttle-ms 100
         timeout-ms 2000}
    :as config}]

  (when-not domain
    (throw (ex-info "Domain is required for state ingress" {:config config})))

  (when @manager
    (logging/log-warn {:msg "State ingress already initialized, stopping existing manager"})
    (ingress/stop @manager))

  (logging/log-info {:msg (str "Initializing state ingress for domain: " domain)})

  (let [mgr (ingress/create-manager config)]
    (reset! manager mgr)
    mgr))

(defn start!
  "Start state ingress.
   Must be initialized first."
  []
  (if-let [mgr @manager]
    (do
      (logging/log-info {:msg "Starting state ingress"})
      (ingress/start mgr))
    (throw (ex-info "State ingress not initialized" {}))))
(m/=> start! [:=> [:cat] :any])

(defn stop!
  "Stop state ingress."
  []
  (when-let [mgr @manager]
    (logging/log-info {:msg "Stopping state ingress"})
    (ingress/stop mgr)))
(m/=> stop! [:=> [:cat] :any])

(defn restart!
  "Restart state ingress with new configuration."
  [config]
  (if-let [mgr @manager]
    (do
      (logging/log-info {:msg "Restarting state ingress with new config"})
      (ingress/restart mgr config))
    (throw (ex-info "State ingress not initialized" {}))))
(m/=> restart! [:=> [:cat :map] :any])

(defn shutdown!
  "Shutdown state ingress completely."
  []
  (when-let [mgr @manager]
    (logging/log-info {:msg "Shutting down state ingress"})
    (ingress/stop mgr)
    (reset! manager nil)))
(m/=> shutdown! [:=> [:cat] :nil])

(defn get-manager
  "Get the current state ingress manager.
   Used for accessing internal stats and state."
  []
  @manager)
(m/=> get-manager [:=> [:cat] [:maybe :map]])

(defn get-connection-stats
  "Get connection statistics from the state ingress.
   Returns nil if not initialized."
  []
  (when-let [mgr @manager]
    (ingress/get-connection-stats mgr)))