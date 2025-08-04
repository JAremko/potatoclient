(ns potatoclient.transit.error-handler
  "Centralized error handling for Transit subprocess messages"
  (:require [com.fulcrologic.guardrails.malli.core :refer [=> >defn >defn- | ?]]
            [potatoclient.logging :as log]
            [potatoclient.transit.app-db :as app-db]))

(>defn handle-subprocess-error
  "Handle error messages from subprocesses"
  [msg]
  [map? => nil?]
  (let [error-type (get-in msg ["payload" "type"])
        message (get-in msg ["payload" "message"])
        stack-trace (get-in msg ["payload" "stackTrace"])
        process-name (get-in msg ["payload" "process"])
        msg-id (get msg "msg-id")
        timestamp (get msg "timestamp")]
    
    (case error-type
      ;; WebSocket connection errors
      "websocket-error"
      (do
        (log/log-error
          {:id ::websocket-error
           :data {:msg-id msg-id
                  :message message
                  :process process-name}
           :msg "WebSocket error in subprocess"})
        ;; Update connection state
        (app-db/set-connection-state! false nil nil)
        ;; Could trigger reconnection logic here
        )
      
      ;; Command building errors
      "command-error"
      (do
        (log/log-error
          {:id ::command-build-error
           :data {:msg-id msg-id
                  :message message
                  :stack-trace stack-trace}
           :msg "Failed to build command from Transit data"})
        ;; Update validation errors
        (app-db/add-validation-error! :malli nil 
          [{:error message
            :timestamp timestamp}]))
      
      ;; State parsing errors
      "state-parse-error"
      (do
        (log/log-error
          {:id ::state-parse-error
           :data {:msg-id msg-id
                  :message message}
           :msg "Failed to parse protobuf state"})
        ;; Track parsing errors
        (swap! app-db/app-db update-in [:validation :stats :state-parse-errors] 
               (fnil inc 0)))
      
      ;; Transit serialization errors
      "transit-error"
      (log/log-error
        {:id ::transit-error
         :data {:msg-id msg-id
                :message message
                :process process-name}
         :msg "Transit serialization error"})
      
      ;; General subprocess errors
      "subprocess-error"
      (do
        (log/log-error
          {:id ::subprocess-error
           :data {:msg-id msg-id
                  :message message
                  :process process-name
                  :stack-trace stack-trace}
           :msg "Subprocess error"})
        ;; Check if subprocess needs restart
        (when (re-find #"fatal|critical" (clojure.string/lower-case (or message "")))
          (log/log-warn
            {:id ::subprocess-restart-needed
             :data {:process process-name}
             :msg "Subprocess may need restart due to fatal error"})))
      
      ;; Unknown error type
      (log/log-warn
        {:id ::unknown-error-type
         :data {:error-type error-type
                :message message
                :payload (get msg "payload")}
         :msg "Unknown error type from subprocess"})))
  nil)

(>defn handle-error-message
  "Main entry point for error message handling"
  [msg]
  [map? => nil?]
  ;; Log all errors for debugging
  (log/log-debug
    {:id ::error-message-received
     :data {:msg-type (get msg "msg-type")
            :payload (get msg "payload")}
     :msg "Received error message from subprocess"})
  
  ;; Delegate to specific handler
  (handle-subprocess-error msg)
  nil)

(>defn handle-validation-error
  "Handle validation error messages"
  [msg]
  [map? => nil?]
  (let [errors (get-in msg ["payload" "errors"])
        source (get-in msg ["payload" "source"])
        subsystem (get-in msg ["payload" "subsystem"])]
    
    (log/log-warn
      {:id ::validation-error
       :data {:source source
              :subsystem subsystem
              :error-count (count errors)}
       :msg "Validation errors from subprocess"})
    
    ;; Store validation errors
    (when (seq errors)
      (app-db/add-validation-error! 
        (keyword (or source "unknown"))
        (keyword (or subsystem "unknown"))
        errors)))
  nil)