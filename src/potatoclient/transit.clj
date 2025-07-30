(ns potatoclient.transit
  "Main integration module for Transit-based architecture"
  (:require [potatoclient.transit.core :as transit-core]
            [potatoclient.transit.subprocess :as subprocess]
            [potatoclient.transit.handlers :as handlers]
            [potatoclient.transit.control :as control]
            [potatoclient.transit.app-db :as app-db]
            [potatoclient.transit.schemas :as schemas]
            [potatoclient.runtime :as runtime]
            [com.fulcrologic.guardrails.malli.core :refer [=> >defn >defn- | ?]]
            [clojure.core.async :as async]
            [potatoclient.logging :as log]))

;; System state
(defonce system (atom nil))

;; Initialize the Transit system
(>defn init!
  "Initialize the Transit-based architecture"
  [ws-url]
  [::specs/url => boolean?]
  (try
    ;; Initialize handlers
    (handlers/init-handlers!)

    ;; Launch subprocesses
    (let [launch-results (control/launch-all-subprocesses! ws-url)]
      (if (and (:state-proc launch-results)
               (:cmd-proc launch-results))
        (do
          ;; Start message processors
          (when-let [state-proc (control/get-subprocess :state-proc)]
            (handlers/start-message-processor! state-proc))

          ;; Store system state
          (reset! system {:ws-url ws-url
                          :initialized true
                          :start-time (System/currentTimeMillis)})

          (log/log-info {:msg "Transit system initialized"
                         :url ws-url
                         :processes launch-results})
          true)
        (do
          (log/log-error {:msg "Failed to launch subprocesses"
                          :results launch-results})
          false)))
    (catch Exception e
      (log/log-error {:msg "Failed to initialize Transit system"
                      :error e})
      false)))

;; Shutdown the system
(>defn shutdown!
  "Shutdown the Transit system gracefully"
  []
  [=> nil?]
  (log/log-info {:msg "Shutting down Transit system"})

  ;; Shutdown all subprocesses
  (control/shutdown-all-subprocesses!)

  ;; Clear system state
  (reset! system nil)

  ;; Reset app-db
  (app-db/reset-to-initial-state!)

  nil)

;; Send a command
(>defn send-command!
  "Send a command through the Transit system"
  [action params]
  [keyword? map? => boolean?]
  (if-let [cmd-proc (control/get-subprocess :cmd-proc)]
    (let [command-data {:action (name action)
                        :params params}]
      (handlers/send-command! cmd-proc command-data)
      true)
    (do
      (log/log-warn {:msg "Command subprocess not available"
                     :action action})
      false)))

;; System health check
(>defn health-check
  "Check health of the Transit system"
  []
  [=> [:map
       [:initialized? boolean?]
       [:subprocesses [:map-of keyword? [:map [:alive? boolean?] [:status keyword?]]]]
       [:app-db-valid? boolean?]
       [:uptime-ms (? pos-int?)]]]
  (let [sys @system
        subprocess-health (control/check-all-subprocess-health)
        app-db-valid? (schemas/validate-app-db @app-db/app-db)]
    {:initialized? (boolean (:initialized sys))
     :subprocesses subprocess-health
     :app-db-valid? app-db-valid?
     :uptime-ms (when sys
                  (- (System/currentTimeMillis) (:start-time sys)))}))

;; Rate control
(>defn set-state-update-rate!
  "Set the maximum rate for state updates"
  [hz]
  [[:int {:min 1 :max 120}] => boolean?]
  (control/set-state-rate-limit! hz))

;; Validation control
(>defn set-validation-enabled!
  "Enable/disable validation in subprocesses"
  [enabled?]
  [boolean? => nil?]
  (control/enable-validation! :state-proc enabled?)
  (control/enable-validation! :cmd-proc enabled?)
  nil)

;; Get current state
(>defn get-current-state
  "Get the current complete state"
  []
  [=> map?]
  @app-db/app-db)

;; Get server state only
(>defn get-server-state
  "Get just the server state portion"
  []
  [=> map?]
  (app-db/get-server-state))

;; Watch for state changes
(>defn watch-state
  "Add a watch function for state changes"
  [key watch-fn]
  [keyword? fn? => nil?]
  (app-db/add-watch-handler key watch-fn))

;; Remove state watch
(>defn unwatch-state
  "Remove a state watch function"
  [key]
  [keyword? => nil?]
  (app-db/remove-watch-handler key))

;; Metrics
(>defn get-system-metrics
  "Get comprehensive system metrics"
  []
  [=> [:map
       [:state-proc any?]
       [:cmd-proc any?]
       [:rate-limits map?]
       [:validation map?]]]
  {:state-proc (control/get-subprocess-metrics :state-proc)
   :cmd-proc (control/get-subprocess-metrics :cmd-proc)
   :rate-limits (app-db/get-rate-limits)
   :validation (app-db/get-validation-state)})

;; Logging control
(>defn set-log-level!
  "Set log level for all subprocesses"
  [level]
  [[:enum :debug :info :warn :error] => nil?]
  (control/set-subprocess-log-level! :state-proc level)
  (control/set-subprocess-log-level! :cmd-proc level)
  nil)

;; Emergency stop
(>defn emergency-stop!
  "Emergency stop all subprocesses"
  []
  [=> nil?]
  (log/log-warn {:msg "Emergency stop initiated"})
  (control/emergency-stop! :state-proc)
  (control/emergency-stop! :cmd-proc)
  (reset! system nil)
  nil)

;; Connection management
(>defn reconnect!
  "Reconnect to server with new URL"
  [new-url]
  [::specs/url => boolean?]
  (shutdown!)
  (Thread/sleep 1000) ; Brief pause before reconnecting
  (init! new-url))

;; Validation error access
(>defn get-validation-errors
  "Get current validation errors"
  []
  [=> [:sequential map?]]
  (get-in @app-db/app-db [:validation :errors]))

(>defn clear-validation-errors!
  "Clear all validation errors"
  []
  [=> map?]
  (app-db/reset-validation-errors!))

;; Development helpers
(>defn- dev-mode?
  "Check if running in development mode"
  []
  [=> boolean?]
  (not (runtime/release-build?)))

(>defn dump-state
  "Dump current state for debugging (dev only)"
  []
  [=> (? map?)]
  (when (dev-mode?)
    {:system @system
     :app-db @app-db/app-db
     :subprocesses (control/check-all-subprocess-health)}))

;; Command helpers for common operations
(>defn set-recording!
  "Enable/disable recording"
  [enabled?]
  [boolean? => boolean?]
  (send-command! :set-recording {:enabled enabled?}))

(>defn set-localization!
  "Set system localization"
  [locale]
  [string? => boolean?]
  (send-command! :set-localization {:locale locale}))

(>defn rotary-goto!
  "Command rotary platform to go to position"
  [azimuth elevation]
  [number? number? => boolean?]
  (send-command! :rotary-goto {:azimuth azimuth
                               :elevation elevation}))

(>defn lrf-measure!
  "Trigger LRF measurement"
  []
  [=> boolean?]
  (send-command! :lrf-single-measurement {}))

(>defn camera-zoom!
  "Set camera zoom"
  [camera-type zoom]
  [[:enum :day :heat] number? => boolean?]
  (send-command! (keyword (str (name camera-type) "-camera-zoom"))
                 {:zoom zoom}))