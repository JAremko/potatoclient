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
(m/=> parse-state-message [:=> [:cat bytes?] [:maybe :map]])

(defn- proto->edn
  "Convert protobuf map to EDN using Pronto."
  [proto-map]
  (try
    (when proto-map
      (p/proto-map->clj-map proto-map))
    (catch Exception e
      (logging/log-error {:msg "Failed to convert proto to EDN" :error e})
      nil)))
(m/=> proto->edn [:=> [:cat [:maybe :map]] [:maybe :state/root]])

(defrecord ^{:doc "Manages WebSocket state ingress from the server.
   
   Maintains persistent connection, handles reconnection logic,
   validates incoming state messages, and updates app state atom.
   
   Fields:
   - config: Map with :url :connect-timeout-ms :read-timeout-ms :max-reconnect-delay-ms
   - connection: Current WebSocketConnection instance or nil
   - throttler: Throttle instance for rate-limiting updates
   - executor: ScheduledExecutorService for async operations
   - running?: Atom boolean, controls connection lifecycle
   - connected?: Atom boolean, tracks current connection status
   - connection-stats: Atom with connection metrics and health data"}
  StateIngressManager [config connection throttler executor running? connected? connection-stats])

;; Spec for StateIngressManager type
(def state-ingress-manager?
  "Spec for StateIngressManager record instances"
  [:fn {:error/message "must be a StateIngressManager"}
   (partial instance? StateIngressManager)])

(defn- handle-state-update
  "Process and apply a state update to the app atom."
  [manager edn-state]
  ;; Validate state using status bar validation (reports errors automatically)
  (when (validation/validate :state/root edn-state)
    ;; Update app state
    (swap! state/app-state assoc :server-state edn-state)))
(m/=> handle-state-update [:=> [:cat state-ingress-manager? :state/root] :any])

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
(m/=> check-connection-health [:=> [:cat state-ingress-manager?] :any])

(defn- calculate-backoff-delay
  "Calculate exponential backoff delay with jitter.
   Starts at 1s, doubles each attempt up to max 30s, with 10% jitter."
  [attempt]
  (let [base-delay 1000  ; 1 second
        max-delay 30000  ; 30 seconds
        delay (min (* base-delay (bit-shift-left 1 (min attempt 5))) max-delay)
        jitter (* delay 0.1 (- (rand) 0.5))]  ; +/- 5% jitter
    (long (+ delay jitter))))
(m/=> calculate-backoff-delay [:=> [:cat nat-int?] [:int {:min 0}]])

(defn- update-connection-stats!
  "Update connection statistics."
  [manager status & {:keys [error]}]
  (let [stats @(:connection-stats manager)
        now (System/currentTimeMillis)]
    (swap! (:connection-stats manager)
           (fn [s]
             (case status
               :attempting (-> s
                              (update :attempts inc)
                              (assoc :last-attempt now
                                     :status :connecting))
               :connected (-> s
                             (assoc :status :connected
                                    :connected-at now
                                    :consecutive-failures 0
                                    :last-error nil))
               :failed (-> s
                          (update :consecutive-failures inc)
                          (assoc :status :disconnected
                                 :last-error (when error (.getMessage error))
                                 :last-failure now))
               :disconnected (-> s
                               (assoc :status :disconnected
                                      :disconnected-at now))
               s)))))
(m/=> update-connection-stats! [:=> [:cat state-ingress-manager? [:enum :attempting :connected :failed :disconnected]] :any])

(defn- connect-loop
  "Connection loop that maintains WebSocket connection with persistent retry."
  [manager]
  (while @(:running? manager)
    (try
      ;; Check if we need to connect
      (when-not (and @(:connection manager)
                     (ws/connected? @(:connection manager)))

        (let [domain (get-in @(:config manager) [:domain])
              url (str "wss://" domain "/ws/ws_state")
              stats @(:connection-stats manager)
              attempt (:consecutive-failures stats 0)
              first-connect? (nil? @(:connection manager))]

          ;; Update connection stats
          (update-connection-stats! manager :attempting)

          (logging/log-info {:msg (str "Connecting to state endpoint: " url
                                       " (attempt " (inc attempt) ")"
                                       (when (> attempt 0)
                                         (str " after " attempt " failures")))})

          ;; Update status bar for reconnection (not initial connection)
          (when-not first-connect?
            (status-bar/set-warning! 
              (str "Reconnecting to state server (attempt " (inc attempt) ")...")))

          (let [last-message-time (atom (System/currentTimeMillis))
                conn (ws/connect
                       {:url url
                        :on-message (fn [data]
                                      (reset! last-message-time (System/currentTimeMillis))
                                      (handle-message manager data))
                        :on-connect (fn []
                                      (logging/log-info {:msg "State connection established"})
                                      (reset! (:connected? manager) true)
                                      (update-connection-stats! manager :connected)
                                      (when-not first-connect?
                                        (status-bar/set-info! "State connection restored")))
                        :on-close (fn [code reason]
                                    (logging/log-warn {:msg (str "State connection closed: " code " " reason)})
                                    (reset! (:connected? manager) false)
                                    (update-connection-stats! manager :disconnected)
                                    (when @(:running? manager)
                                      (status-bar/set-warning! "State connection lost")))
                        :on-error (fn [error]
                                    (logging/log-error {:msg "State connection error" :error error})
                                    (update-connection-stats! manager :failed :error error))})]

            ;; Store connection and last message time
            (reset! (:connection manager) conn)
            (swap! (:config manager) assoc :last-message-time last-message-time))))

      ;; Calculate backoff delay based on consecutive failures
      (let [stats @(:connection-stats manager)
            delay (if (= :connected (:status stats))
                    2000  ; Normal check interval when connected
                    (calculate-backoff-delay (:consecutive-failures stats 0)))]
        (Thread/sleep delay))

      (catch InterruptedException _
        ;; Thread interrupted, exit loop
        (logging/log-debug {:msg "Connection loop interrupted"}))
      (catch Exception e
        (logging/log-error {:msg "Error in connection loop" :error e})
        (update-connection-stats! manager :failed :error e)
        ;; Calculate backoff delay for retry
        (let [stats @(:connection-stats manager)
              delay (calculate-backoff-delay (:consecutive-failures stats 0))]
          (logging/log-debug {:msg (str "Retrying in " (/ delay 1000.0) " seconds")})
          (Thread/sleep delay)))))
(m/=> connect-loop [:=> [:cat state-ingress-manager?] :any]))

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
       :running? (atom false)
       :connected? (atom false)
       :connection-stats (atom {:status :disconnected
                                :attempts 0
                                :consecutive-failures 0
                                :last-attempt nil
                                :last-error nil
                                :connected-at nil
                                :disconnected-at nil
                                :start-time (System/currentTimeMillis)})})))
(m/=> create-manager [:=> [:cat [:map [:domain :string]]] state-ingress-manager?])

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
(m/=> start [:=> [:cat state-ingress-manager?] state-ingress-manager?])

(defn connected?
  "Check if the state ingress is connected."
  [manager]
  (and @(:running? manager)
       @(:connected? manager)
       @(:connection manager)
       (ws/connected? @(:connection manager))))
(m/=> connected? [:=> [:cat state-ingress-manager?] :boolean])

(defn get-connection-stats
  "Get current connection statistics."
  [manager]
  @(:connection-stats manager))
(m/=> get-connection-stats [:=> [:cat state-ingress-manager?] :map])

(defn stop
  "Stop the state ingress manager."
  [manager]
  (when (compare-and-set! (:running? manager) true false)
    (logging/log-info {:msg "Stopping state ingress manager"})

    ;; Reset connected flag and update stats
    (reset! (:connected? manager) false)
    (update-connection-stats! manager :disconnected)

    ;; Close connection
    (when-let [conn @(:connection manager)]
      (ws/close conn))

    ;; Shutdown throttler
    (throttle/shutdown-throttler (:throttler manager))

    ;; Shutdown executor
    (.shutdown (:executor manager)))

  manager)
(m/=> stop [:=> [:cat state-ingress-manager?] state-ingress-manager?])

(defn restart
  "Restart the state ingress manager with new config."
  [manager new-config]
  (stop manager)
  (reset! (:config manager) new-config)
  (start manager))
(m/=> restart [:=> [:cat state-ingress-manager? [:map [:domain :string]]] state-ingress-manager?])