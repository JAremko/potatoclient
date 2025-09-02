(ns potatoclient.state.server.core
  "Core namespace for server state ingress.
   Provides high-level API for managing state synchronization with server."
  (:require [potatoclient.logging :as logging]
            [potatoclient.state.server.ingress :as ingress]))

(defonce ^:private manager (atom nil))

(defn initialized?
  "Check if state ingress is initialized."
  []
  (some? @manager))

(defn running?
  "Check if state ingress is running."
  []
  (and @manager @(:running? @manager)))

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

(defn stop!
  "Stop state ingress."
  []
  (when-let [mgr @manager]
    (logging/log-info {:msg "Stopping state ingress"})
    (ingress/stop mgr)))

(defn restart!
  "Restart state ingress with new configuration."
  [config]
  (if-let [mgr @manager]
    (do
      (logging/log-info {:msg "Restarting state ingress with new config"})
      (ingress/restart mgr config))
    (throw (ex-info "State ingress not initialized" {}))))

(defn shutdown!
  "Shutdown state ingress completely."
  []
  (when-let [mgr @manager]
    (logging/log-info {:msg "Shutting down state ingress"})
    (ingress/stop mgr)
    (reset! manager nil)))