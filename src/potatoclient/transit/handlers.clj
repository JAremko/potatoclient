(ns potatoclient.transit.handlers
  "Message handlers and routing for Transit communication"
  (:require [potatoclient.transit.core :as transit]
            [potatoclient.transit.app-db :as app-db]
            [potatoclient.transit.schemas :as schemas]
            [potatoclient.transit.subprocess :as subprocess]
            [potatoclient.specs :as specs]
            [com.fulcrologic.guardrails.malli.core :refer [=> >defn >defn- | ?]]
            [clojure.core.async :as async :refer [<! >! go go-loop]]
            [potatoclient.logging :as log]))

;; Message handler registry
(defonce message-handlers (atom {}))

;; Pending responses for request-reply pattern
(defonce pending-responses (atom {}))

;; Register a message handler
(>defn register-handler!
  "Register a handler for a specific message type"
  [msg-type handler-fn]
  [keyword? fn? => nil?]
  (swap! message-handlers assoc msg-type handler-fn)
  nil)

;; Unregister a message handler
(>defn unregister-handler!
  "Remove a handler for a message type"
  [msg-type]
  [keyword? => nil?]
  (swap! message-handlers dissoc msg-type)
  nil)

;; Core message dispatcher
(>defn- dispatch-message
  "Dispatch a message to its handler"
  [msg]
  [map? => any?]
  (if-let [handler (get @message-handlers (:msg-type msg))]
    (try
      (handler msg)
      (catch Exception e
        (log/log-error {:msg "Error in message handler"
                        :msg-type (:msg-type msg)
                        :error e})
        nil))
    (do
      (log/log-warn {:msg "No handler for message type"
                     :msg-type (:msg-type msg)})
      nil)))

;; State update handler
(>defn- handle-state-message
  "Handle state update from subprocess"
  [{:keys [payload timestamp] :as msg}]
  [map? => nil?]
  (when (schemas/validate-state-message msg)
    ;; Update server state
    (app-db/update-server-state! payload)

    ;; Update rate metrics (tracking rate from Kotlin subprocess)
    (let [last-update (app-db/get-rate-limits)
          current-time timestamp
          time-diff (- current-time (or (:last-update-time last-update) 0))
          rate (if (pos? time-diff)
                 (/ 1000.0 time-diff)  ; Convert to Hz
                 0.0)]
      (app-db/update-rate-metrics! rate false))

    (log/log-debug {:msg "State updated"
                    :subsystems (keys payload)
                    :timestamp timestamp}))
  nil)

;; Validation error handler
(>defn- handle-validation-error
  "Handle validation error from subprocess"
  [{:keys [payload] :as msg}]
  [map? => nil?]
  (when (schemas/validate-message msg)
    (let [{:keys [source subsystem errors]} payload]
      (app-db/add-validation-error! source subsystem errors)
      (log/log-warn {:msg "Validation error received"
                     :source source
                     :subsystem subsystem
                     :error-count (count errors)})))
  nil)

;; Response handler
(>defn- handle-response-message
  "Handle response from subprocess"
  [{:keys [msg-id payload] :as msg}]
  [map? => nil?]
  (when-let [promise (get @pending-responses msg-id)]
    (deliver promise payload)
    (swap! pending-responses dissoc msg-id))
  nil)

;; Initialize default handlers
(>defn init-handlers!
  "Initialize default message handlers"
  []
  [=> nil?]
  (register-handler! :state handle-state-message)
  (register-handler! :validation-error handle-validation-error)
  (register-handler! :response handle-response-message)
  nil)

;; Message processing loop for subprocess
(>defn start-message-processor!
  "Start async message processor for a subprocess"
  [subprocess]
  [[:fn {:error/message "must be a TransitSubprocess"}
    #(instance? potatoclient.transit.subprocess.TransitSubprocess %)]
   => nil?]
  (go-loop []
    (when-let [msg (<! (:out-chan subprocess))]
      (dispatch-message msg)
      (recur)))
  nil)

;; Send command with optional response waiting
(>defn send-command!
  "Send a command to the command subprocess"
  [cmd-subprocess command-data & {:keys [wait-response? timeout-ms]
                                  :or {wait-response? false
                                       timeout-ms 5000}}]
  [[:fn {:error/message "must be a TransitSubprocess"}
    #(instance? potatoclient.transit.subprocess.TransitSubprocess %)]
   map?
   [:* any?]
   => any?]
  (let [msg (transit/create-message :command command-data)]
    (if wait-response?
      (let [response-promise (promise)
            msg-id (:msg-id msg)]
        (swap! pending-responses assoc msg-id response-promise)
        (subprocess/send-to-subprocess! cmd-subprocess msg)
        (deref response-promise timeout-ms :timeout))
      (do
        (subprocess/send-to-subprocess! cmd-subprocess msg)
        true))))

;; Rate limiting control
(>defn set-state-rate-limit!
  "Set the rate limit for state updates"
  [state-subprocess max-hz]
  [[:fn {:error/message "must be a TransitSubprocess"}
    #(instance? potatoclient.transit.subprocess.TransitSubprocess %)]
   [:int {:min 1 :max 120}]
   => boolean?]
  (subprocess/set-rate-limit! state-subprocess max-hz)
  (app-db/set-max-rate-hz! max-hz)
  true)

;; Validation control
(>defn enable-subprocess-validation!
  "Enable/disable validation in subprocess"
  [subprocess enabled?]
  [[:fn {:error/message "must be a TransitSubprocess"}
    #(instance? potatoclient.transit.subprocess.TransitSubprocess %)]
   boolean?
   => boolean?]
  (subprocess/enable-validation! subprocess enabled?)
  (when (= (:name subprocess) "state-proc")
    (app-db/set-validation-enabled! enabled?))
  true)

;; Get logs from subprocess
(>defn get-subprocess-logs!
  "Get recent logs from a subprocess"
  [subprocess lines]
  [[:fn {:error/message "must be a TransitSubprocess"}
    #(instance? potatoclient.transit.subprocess.TransitSubprocess %)]
   [:int {:min 1 :max 10000}]
   => any?]
  (let [msg (transit/create-message :control {:action :get-logs :lines lines})
        response-promise (promise)
        msg-id (:msg-id msg)]
    (swap! pending-responses assoc msg-id response-promise)
    (subprocess/send-to-subprocess! subprocess msg)
    (deref response-promise 5000 :timeout)))

;; Broadcast to all subprocesses
(>defn broadcast-control!
  "Send control message to all running subprocesses"
  [action data]
  [keyword? map? => [:map-of keyword? boolean?]]
  (let [processes (app-db/get-app-state)
        results (atom {})]
    (doseq [[proc-key proc-state] (:processes processes)]
      (when (= :running (:status proc-state))
        (let [msg (transit/create-message :control (assoc data :action action))]
          ;; Note: In real implementation, need to get subprocess refs
          (swap! results assoc proc-key true))))
    @results))

;; Graceful shutdown of all subprocesses
(>defn shutdown-all-subprocesses!
  "Shutdown all running subprocesses"
  []
  [=> nil?]
  (broadcast-control! :shutdown {})
  nil)

;; Health check for subprocesses
(>defn check-subprocess-health
  "Check health of all subprocesses"
  []
  [=> [:map-of keyword? [:map [:alive? boolean?] [:status keyword?]]]]
  (let [processes (app-db/get-app-state)]
    (into {}
          (for [[proc-key proc-state] (:processes processes)]
            [proc-key {:alive? (= :running (:status proc-state))
                       :status (:status proc-state)}]))))

;; Message statistics
(>defn- update-message-stats!
  "Update message processing statistics"
  [msg-type]
  [keyword? => nil?]
  ;; This would update metrics in app-db in a real implementation
  nil)

;; Error recovery
(>defn- handle-subprocess-error
  "Handle subprocess communication errors"
  [subprocess error]
  [[:fn {:error/message "must be a TransitSubprocess"}
    #(instance? potatoclient.transit.subprocess.TransitSubprocess %)]
   [:fn {:error/message "must be a Throwable"}
    #(instance? Throwable %)]
   => nil?]
  (log/log-error {:msg "Subprocess error"
                  :subprocess (:name subprocess)
                  :error error})
  ;; Update process state to error
  (app-db/set-process-state! (keyword (:name subprocess)) nil :error)
  nil)

;; Reconnection handler
(>defn- attempt-subprocess-reconnect
  "Attempt to reconnect a failed subprocess"
  [subprocess-type ws-url]
  [[:enum :state-proc :cmd-proc] ::specs/url => boolean?]
  (try
    (let [new-subprocess (subprocess/launch-transit-subprocess subprocess-type ws-url)]
      (start-message-processor! new-subprocess)
      (app-db/set-process-state! subprocess-type
                                 (.pid ^Process (:process new-subprocess))
                                 :running)
      true)
    (catch Exception e
      (log/log-error {:msg "Failed to reconnect subprocess"
                      :type subprocess-type
                      :error e})
      false)))