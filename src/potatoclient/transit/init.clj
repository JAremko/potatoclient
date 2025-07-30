(ns potatoclient.transit.init
  "Initialization module for Transit-based architecture.
  
  This namespace shows how to update the core initialization to use
  Transit instead of direct WebSocket/protobuf communication."
  (:require [com.fulcrologic.guardrails.malli.core :refer [=> >defn >defn- ?]]
            [potatoclient.transit.websocket-manager :as ws-manager]
            [potatoclient.transit.app-db :as app-db]
            [potatoclient.logging :as logging]))

;; ============================================================================
;; Example Updated Initialization
;; ============================================================================

(>defn initialize-transit-system!
  "Initialize the Transit-based communication system.
  
  This replaces the old cmd/init-websocket! call in core.clj"
  [domain]
  [string? => nil?]
  (logging/log-info "Initializing Transit communication system" {:domain domain})

  ;; Initialize WebSocket connections through Transit subprocesses
  (ws-manager/init! domain)

  ;; Set up any initial state watchers if needed
  (add-watch app-db/app-db ::connection-monitor
             (fn [_ _ old-state new-state]
               (let [old-conn (get-in old-state [:app-state :connection :connected?])
                     new-conn (get-in new-state [:app-state :connection :connected?])]
                 (when (not= old-conn new-conn)
                   (if new-conn
                     (logging/log-info "WebSocket connected")
                     (logging/log-warn "WebSocket disconnected"))))))

  ;; Log successful initialization
  (logging/log-info "Transit system initialized successfully")
  nil)

(>defn shutdown-transit-system!
  "Shutdown the Transit-based communication system.
  
  This replaces the old cmd/stop-websocket! call"
  []
  [=> nil?]
  (logging/log-info "Shutting down Transit communication system")

  ;; Remove watchers
  (remove-watch app-db/app-db ::connection-monitor)

  ;; Stop WebSocket connections
  (ws-manager/stop!)

  (logging/log-info "Transit system shutdown complete")
  nil)

;; ============================================================================
;; Updated -main Example
;; ============================================================================

(comment
  ;; This shows how to update the -main function in core.clj:

  ;; OLD CODE:
  #_(cmd/init-websocket!
      domain
      (fn [error-msg]
        (logging/log-error (str "WebSocket error: " error-msg)))
      (fn [binary-data]
        (dispatch/handle-binary-state binary-data)))

  ;; NEW CODE:
  (initialize-transit-system! domain)

  ;; The error handling and state updates are now automatic through app-db
  ;; No need for explicit callbacks
  )

;; ============================================================================
;; Updated Shutdown Hook Example
;; ============================================================================

(comment
  ;; This shows how to update the shutdown hook:

  ;; OLD CODE:
  #_(>defn- setup-shutdown-hook!
      []
      [=> nil?]
      (.addShutdownHook
        (Runtime/getRuntime)
        (Thread.
          (fn []
            (try
              (cmd/stop-websocket!)
              (process/cleanup-all-processes)
              (logging/shutdown!)
              (catch Exception _
                nil))))))

  ;; NEW CODE:
  (>defn- setup-shutdown-hook!
    []
    [=> nil?]
    (.addShutdownHook
      (Runtime/getRuntime)
      (Thread.
        (fn []
          (try
            (shutdown-transit-system!)  ; Replace cmd/stop-websocket!
            (process/cleanup-all-processes)
            (logging/shutdown!)
            (catch Exception _
              nil)))))))