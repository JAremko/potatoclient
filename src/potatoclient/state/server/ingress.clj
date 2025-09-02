(ns potatoclient.state.server.ingress
  "State ingress manager that maintains WebSocket connection to state endpoint,
   validates incoming state, and updates the app state atom."
  (:require [potatoclient.logging :as logging]
            [potatoclient.state :as state]
            [potatoclient.state.server.websocket :as ws]
            [potatoclient.state.server.throttle :as throttle]
            [potatoclient.ui.status-bar.messages :as status-bar]
            [potatoclient.ui.status-bar.validation :as validation]
            [pronto.core :as p]
            [malli.core :as m]
            ;; Import state specs to register :state/root in registry
            [potatoclient.specs.state.root])
  (:import [ser JonSharedData$JonGUIState]
           [java.util.concurrent Executors TimeUnit]))

;; Pronto mapper for state messages
(p/defmapper state-mapper [ser.JonSharedData$JonGUIState])

(defn- parse-state-message
  "Parse binary protobuf state message to proto map."
  [binary-data]
  (try
    (p/bytes->proto-map state-mapper ser.JonSharedData$JonGUIState binary-data)
    (catch Exception e
      (logging/log-error {:msg "Failed to parse state message" :error e})
      nil)))

(defn- proto->edn
  "Convert protobuf map to EDN using Pronto."
  [proto-map]
  (try
    (when proto-map
      (p/proto-map->clj-map proto-map))
    (catch Exception e
      (logging/log-error {:msg "Failed to convert proto to EDN" :error e})
      nil)))

(defrecord StateIngressManager [config connection throttler executor running?])

(defn- handle-state-update
  "Process and apply a state update to the app atom."
  [manager edn-state]
  ;; Validate state using status bar validation (reports errors automatically)
  (when (validation/validate :state/root edn-state)
    ;; Update app state
    (swap! state/app-state assoc :server-state edn-state)))

(defn- handle-message
  "Handle incoming WebSocket message."
  [manager binary-data]
  (when-let [proto-map (parse-state-message binary-data)]
    (when-let [edn-state (proto->edn proto-map)]
      ;; Submit to throttler
      ((:submit (:throttler manager))
       (partial handle-state-update manager)
       edn-state))))

(defn- check-connection-health
  "Check if connection is alive based on last message time."
  [manager]
  (let [last-message (:last-message-time (:config manager))
        now (System/currentTimeMillis)
        timeout-ms (:timeout-ms (:config manager) 2000)]

    (when (and @last-message
               (> (- now @last-message) timeout-ms))
      (logging/log-warn {:msg (str "Connection timeout - no messages for " timeout-ms "ms")})
      ;; Trigger reconnection
      (when-let [conn @(:connection manager)]
        (ws/close conn))
      (reset! (:connection manager) nil))))

(defn- connect-loop
  "Connection loop that maintains WebSocket connection with reconnection."
  [manager]
  (while @(:running? manager)
    (try
      ;; Check if we need to connect
      (when-not (and @(:connection manager)
                     (ws/connected? @(:connection manager)))

        (let [domain (get-in @(:config manager) [:domain])
              url (str "wss://" domain "/ws/ws_state")
              first-connect? (nil? @(:connection manager))]

          (logging/log-info {:msg (str "Connecting to state endpoint: " url)})

          ;; Update status bar for reconnection (not initial connection)
          (when-not first-connect?
            (status-bar/set-warning! "Reconnecting to state server..."))

          (let [last-message-time (atom (System/currentTimeMillis))
                conn (ws/connect
                       {:url url
                        :on-message (fn [data]
                                      (reset! last-message-time (System/currentTimeMillis))
                                      (handle-message manager data))
                        :on-connect (fn []
                                      (logging/log-info {:msg "State connection established"})
                                      (when-not first-connect?
                                        (status-bar/set-info! "State connection restored")))
                        :on-close (fn [code reason]
                                    (logging/log-warn {:msg (str "State connection closed: " code " " reason)})
                                    (when @(:running? manager)
                                      (status-bar/set-warning! "State connection lost")))
                        :on-error (fn [error]
                                    (logging/log-error {:msg "State connection error" :error error}))})]

            ;; Store connection and last message time
            (reset! (:connection manager) conn)
            (swap! (:config manager) assoc :last-message-time last-message-time))))

      ;; Sleep before next check
      (Thread/sleep 2000)

      (catch InterruptedException _
        ;; Thread interrupted, exit loop
        (logging/log-debug {:msg "Connection loop interrupted"}))
      (catch Exception e
        (logging/log-error {:msg "Error in connection loop" :error e})
        ;; Sleep before retry
        (Thread/sleep 2000)))))

(defn create-manager
  "Create a state ingress manager.
   
   Config options:
   - :domain - Server domain (required)
   - :throttle-ms - Throttling interval in milliseconds (default 100)
   - :timeout-ms - Connection timeout in milliseconds (default 2000)
   
   Returns a StateIngressManager that can be started/stopped."
  [config]
  (let [throttler (throttle/create-throttler
                    {:interval-ms (:throttle-ms config 100)
                     :on-drop (fn [_] (logging/log-debug {:msg "State update dropped due to throttling"}))})
        executor (Executors/newSingleThreadScheduledExecutor)]

    (map->StateIngressManager
      {:config (atom config)
       :connection (atom nil)
       :throttler throttler
       :executor executor
       :running? (atom false)})))

(defn start
  "Start the state ingress manager."
  [manager]
  (when (compare-and-set! (:running? manager) false true)
    (logging/log-info {:msg "Starting state ingress manager"})

    ;; Start connection thread
    (let [thread (Thread. #(connect-loop manager) "state-ingress-connection")]
      (.setDaemon thread true)
      (.start thread))

    ;; Schedule health checks
    (.scheduleAtFixedRate (:executor manager)
                          #(check-connection-health manager)
                          2 2 TimeUnit/SECONDS)

    manager))

(defn stop
  "Stop the state ingress manager."
  [manager]
  (when (compare-and-set! (:running? manager) true false)
    (logging/log-info {:msg "Stopping state ingress manager"})

    ;; Close connection
    (when-let [conn @(:connection manager)]
      (ws/close conn))

    ;; Shutdown throttler
    (throttle/shutdown-throttler (:throttler manager))

    ;; Shutdown executor
    (.shutdown (:executor manager))

    manager))

(defn restart
  "Restart the state ingress manager with new config."
  [manager new-config]
  (stop manager)
  (reset! (:config manager) new-config)
  (start manager))