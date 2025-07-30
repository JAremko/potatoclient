(ns potatoclient.transit.control
  "Bidirectional control interface for subprocess management"
  (:require [potatoclient.transit.core :as transit]
            [potatoclient.transit.subprocess :as subprocess]
            [potatoclient.transit.app-db :as app-db]
            [potatoclient.specs :as specs]
            [com.fulcrologic.guardrails.malli.core :refer [=> >defn >defn- | ?]]
            [clojure.core.async :as async :refer [<! >! go chan timeout]]
            [potatoclient.logging :as log]))

;; Active subprocess registry
(defonce subprocesses (atom {}))

;; Register a subprocess
(>defn register-subprocess!
  "Register a subprocess for management"
  [key subprocess]
  [keyword? 
   [:fn {:error/message "must be a TransitSubprocess"} 
    #(instance? potatoclient.transit.subprocess.TransitSubprocess %)]
   => nil?]
  (swap! subprocesses assoc key subprocess)
  nil)

;; Unregister a subprocess
(>defn unregister-subprocess!
  "Remove a subprocess from management"
  [key]
  [keyword? => nil?]
  (swap! subprocesses dissoc key)
  nil)

;; Get subprocess by key
(>defn get-subprocess
  "Get a registered subprocess"
  [key]
  [keyword? => (? [:fn {:error/message "must be a TransitSubprocess"}
                   #(instance? potatoclient.transit.subprocess.TransitSubprocess %)])]
  (get @subprocesses key))

;; Send control message to specific subprocess
(>defn send-control-message!
  "Send a control message to a subprocess"
  [process-key action data]
  [[:enum :state-proc :cmd-proc :heat-video :day-video]
   keyword? map? => boolean?]
  (if-let [subprocess (get-subprocess process-key)]
    (subprocess/send-control-message! subprocess action data)
    (do
      (log/log-warn {:msg "Subprocess not found" :key process-key})
      false)))

;; Send query and wait for response
(>defn send-query-message!
  "Send a query message and wait for response"
  [process-key action data & {:keys [timeout-ms] :or {timeout-ms 5000}}]
  [[:enum :state-proc :cmd-proc :heat-video :day-video]
   keyword? map? [:* any?] => any?]
  (if-let [subprocess (get-subprocess process-key)]
    (let [msg-id (str (java.util.UUID/randomUUID))
          msg (transit/create-message :control (assoc data :action action))
          response-chan (chan 1)]
      
      ;; Register response handler
      (swap! subprocess/pending-responses assoc msg-id response-chan)
      
      ;; Send message
      (subprocess/send-to-subprocess! subprocess msg)
      
      ;; Wait for response with timeout
      (let [result (async/alt!!
                     response-chan ([v] v)
                     (timeout timeout-ms) :timeout)]
        (swap! subprocess/pending-responses dissoc msg-id)
        result))
    (do
      (log/log-warn {:msg "Subprocess not found" :key process-key})
      nil)))

;; Rate limit control for state subprocess
(>defn set-state-rate-limit!
  "Set maximum state update rate in Hz"
  [hz]
  [[:int {:min 1 :max 120}] => boolean?]
  (send-control-message! :state-proc :set-rate-limit {:max-hz hz}))

;; Get subprocess logs
(>defn get-subprocess-logs
  "Get recent logs from subprocess"
  [process-key lines]
  [[:enum :state-proc :cmd-proc :heat-video :day-video] 
   [:int {:min 1 :max 10000}] 
   => any?]
  (send-query-message! process-key :get-logs {:lines lines}))

;; Enable/disable validation
(>defn enable-validation!
  "Enable/disable validation in subprocess"
  [process-key enabled?]
  [[:enum :state-proc :cmd-proc] boolean? => boolean?]
  (send-control-message! process-key :set-validation {:enabled enabled?}))

;; Broadcast control to all subprocesses
(>defn broadcast-control!
  "Send control message to all registered subprocesses"
  [action data]
  [keyword? map? => [:map-of keyword? boolean?]]
  (into {}
        (for [[key subprocess] @subprocesses]
          [key (subprocess/send-control-message! subprocess action data)])))

;; Health check
(>defn check-subprocess-health
  "Check health of a specific subprocess"
  [process-key]
  [[:enum :state-proc :cmd-proc :heat-video :day-video]
   => [:map [:alive? boolean?] [:status keyword?]]]
  (if-let [subprocess (get-subprocess process-key)]
    {:alive? (subprocess/subprocess-alive? subprocess)
     :status (if (subprocess/subprocess-alive? subprocess) :running :stopped)}
    {:alive? false
     :status :not-found}))

;; Check all subprocess health
(>defn check-all-subprocess-health
  "Check health of all registered subprocesses"
  []
  [=> [:map-of keyword? [:map [:alive? boolean?] [:status keyword?]]]]
  (into {}
        (for [[key subprocess] @subprocesses]
          [key {:alive? (subprocess/subprocess-alive? subprocess)
                :status (if (subprocess/subprocess-alive? subprocess) :running :stopped)}])))

;; Restart subprocess
(>defn restart-subprocess!
  "Restart a subprocess"
  [process-key ws-url]
  [[:enum :state-proc :cmd-proc] ::specs/url => boolean?]
  (try
    ;; Shutdown existing if running
    (when-let [old-subprocess (get-subprocess process-key)]
      (subprocess/shutdown-transit-subprocess! old-subprocess)
      (unregister-subprocess! process-key))
    
    ;; Launch new subprocess
    (let [new-subprocess (subprocess/launch-transit-subprocess process-key ws-url)]
      (register-subprocess! process-key new-subprocess)
      (app-db/set-process-state! process-key 
                                 (.pid ^Process (:process new-subprocess))
                                 :running)
      true)
    (catch Exception e
      (log/log-error {:msg "Failed to restart subprocess"
                   :process process-key
                   :error e})
      false)))

;; Graceful shutdown all
(>defn shutdown-all-subprocesses!
  "Gracefully shutdown all subprocesses"
  []
  [=> nil?]
  (doseq [[key subprocess] @subprocesses]
    (try
      (subprocess/shutdown-transit-subprocess! subprocess)
      (app-db/set-process-state! key nil :stopped)
      (catch Exception e
        (log/log-error {:msg "Error shutting down subprocess"
                     :process key
                     :error e}))))
  (reset! subprocesses {})
  nil)

;; Monitor subprocess metrics
(>defn get-subprocess-metrics
  "Get metrics from a subprocess"
  [process-key]
  [[:enum :state-proc :cmd-proc] => any?]
  (send-query-message! process-key :get-metrics {}))

;; Configure subprocess logging
(>defn set-subprocess-log-level!
  "Set log level for subprocess"
  [process-key level]
  [[:enum :state-proc :cmd-proc :heat-video :day-video]
   [:enum :debug :info :warn :error]
   => boolean?]
  (send-control-message! process-key :set-log-level {:level level}))

;; Request subprocess status
(>defn get-subprocess-status
  "Get detailed status from subprocess"
  [process-key]
  [[:enum :state-proc :cmd-proc :heat-video :day-video] => any?]
  (send-query-message! process-key :get-status {}))

;; Emergency stop
(>defn emergency-stop!
  "Emergency stop a subprocess (force kill)"
  [process-key]
  [[:enum :state-proc :cmd-proc :heat-video :day-video] => boolean?]
  (if-let [subprocess (get-subprocess process-key)]
    (try
      (.destroyForcibly ^Process (:process subprocess))
      (app-db/set-process-state! process-key nil :stopped)
      (unregister-subprocess! process-key)
      true
      (catch Exception e
        (log/log-error {:msg "Failed to emergency stop subprocess"
                     :process process-key
                     :error e})
        false))
    false))

;; Launch all required subprocesses
(>defn launch-all-subprocesses!
  "Launch state and command subprocesses"
  [ws-url]
  [::specs/url => [:map [:state-proc boolean?] [:cmd-proc boolean?]]]
  (let [state-result (try
                       (let [subprocess (subprocess/launch-transit-subprocess :state-proc ws-url)]
                         (register-subprocess! :state-proc subprocess)
                         (app-db/set-process-state! :state-proc 
                                                    (.pid ^Process (:process subprocess))
                                                    :running)
                         true)
                       (catch Exception e
                         (log/log-error {:msg "Failed to launch state subprocess" :error e})
                         false))
        cmd-result (try
                     (let [subprocess (subprocess/launch-transit-subprocess :cmd-proc ws-url)]
                       (register-subprocess! :cmd-proc subprocess)
                       (app-db/set-process-state! :cmd-proc 
                                                  (.pid ^Process (:process subprocess))
                                                  :running)
                       true)
                     (catch Exception e
                       (log/log-error {:msg "Failed to launch command subprocess" :error e})
                       false))]
    {:state-proc state-result
     :cmd-proc cmd-result}))