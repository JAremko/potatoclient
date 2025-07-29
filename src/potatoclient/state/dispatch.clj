(ns potatoclient.state.dispatch
  "Singleton state dispatch system for handling device state updates.

  This namespace provides a centralized dispatch system for JonGUIState messages
  received from the server. It implements change detection and efficient state
  distribution using core.async channels.

  Based on the TypeScript deviceStateDispatch.ts implementation."
  (:require [clojure.core.async :refer [chan close! put!]]
            [com.fulcrologic.guardrails.malli.core :refer [=> >defn >defn- ?]]
            [malli.core :as m]
            [potatoclient.logging :as logging]
            [potatoclient.proto :as proto]
            [potatoclient.state.device :as device-state]
            [potatoclient.state.edn :as edn]
            [potatoclient.state.proto-bridge :as bridge]
            [potatoclient.state.schemas :as schemas])
  (:import (clojure.core.async.impl.channels ManyToManyChannel)))

;; ============================================================================
;; Singleton State
;; ============================================================================

(defonce ^:private instance (atom nil))

;; Channel for raw binary state messages
(defonce ^:private state-channel (atom (chan 100)))

;; Shadow state - EDN map for efficient comparison
;; This maintains the last known state in EDN form
(defonce ^:private shadow-state
  (atom {}))

;; ============================================================================
;; Protocol Definition
;; ============================================================================

(defprotocol IStateDispatch
  "Protocol for state dispatch operations"
  (handle-state-message [this binary-data]
    "Process binary protobuf state message")
  (dispose [this]
    "Clean up resources"))

;; ============================================================================
;; Validation Control
;; ============================================================================

(defonce ^{:doc "Enable/disable state validation. Set to true in dev mode."}
  validate-state? (atom false))

(defonce ^{:doc "Debug mode for logging all state changes."}
  debug-mode? (atom false))

;; ============================================================================
;; Change Detection
;; ============================================================================

(>defn- subsystem-changed?
  "Check if a subsystem has changed using EDN equality."
  [old-data new-data]
  [(? any?) (? any?) => boolean?]
  (not= old-data new-data))

(>defn- get-schema-for-subsystem
  "Get the Malli schema for a given subsystem"
  [subsystem-key]
  [keyword? => (? any?)]
  (case subsystem-key
    :system schemas/system-schema
    :lrf schemas/lrf-schema
    :time schemas/time-schema
    :gps schemas/gps-schema
    :compass schemas/compass-schema
    :rotary schemas/rotary-schema
    :camera-day schemas/camera-day-schema
    :camera-heat schemas/camera-heat-schema
    :compass-calibration schemas/compass-calibration-schema
    :rec-osd schemas/rec-osd-schema
    :day-cam-glass-heater schemas/day-cam-glass-heater-schema
    :actual-space-time schemas/actual-space-time-schema
    :meteo-internal schemas/meteo-schema
    nil))

(>defn- validate-subsystem-data
  "Validate subsystem data against its schema if validation is enabled"
  [subsystem-key data]
  [keyword? (? any?) => (? any?)]
  (if (and @validate-state? data)
    (if-let [schema (get-schema-for-subsystem subsystem-key)]
      (if (m/validate schema data)
        data
        (do
          (logging/log-warn {:msg (str "State validation failed for " (name subsystem-key))
                             :subsystem subsystem-key
                             :errors (m/explain schema data)})
          nil))
      data)
    data))

;; No longer needed - we work with EDN directly

(>defn- get-shadow-subsystem
  "Get the current subsystem value from shadow state"
  [shadow-state subsystem-key]
  [map? keyword? => (? any?)]
  (get shadow-state subsystem-key))

(>defn- update-shadow-subsystem!
  "Update a subsystem in the shadow state"
  [shadow-atom subsystem-key subsystem-data]
  [any? keyword? (? any?) => nil?]
  (if subsystem-data
    (swap! shadow-atom assoc subsystem-key subsystem-data)
    (swap! shadow-atom dissoc subsystem-key))
  nil)

(>defn- compare-and-update-with-shadow!
  "Compare EDN data using shadow state, update atom only if changed.
  Returns true if state was updated."
  [state-atom subsystem-data subsystem-key]
  [any? (? any?) keyword? => boolean?]
  (let [shadow-data (get-shadow-subsystem @shadow-state subsystem-key)]
    (if (subsystem-changed? shadow-data subsystem-data)
      (let [validated-value (validate-subsystem-data subsystem-key subsystem-data)]
        ;; Update shadow state with EDN data
        (update-shadow-subsystem! shadow-state subsystem-key subsystem-data)
        ;; Update atom with validated EDN value
        (reset! state-atom validated-value)
        (when @debug-mode?
          (logging/log-debug {:msg (str "State updated: " (name subsystem-key))
                              :subsystem subsystem-key
                              :has-value (some? validated-value)}))
        true)
      false)))

;; ============================================================================
;; State Updates
;; ============================================================================

(>defn- update-all-subsystems!
  "Update all subsystem atoms with EDN state data."
  [state-map]
  [map? => nil?]
  ;; Update each subsystem present in the state map
  (doseq [subsystem-key bridge/subsystem-keys]
    (when (contains? state-map subsystem-key)
      (let [subsystem-data (get state-map subsystem-key)
            state-atom (case subsystem-key
                         :system device-state/system-state
                         :lrf device-state/lrf-state
                         :time device-state/time-state
                         :gps device-state/gps-state
                         :compass device-state/compass-state
                         :rotary device-state/rotary-state
                         :camera-day device-state/camera-day-state
                         :camera-heat device-state/camera-heat-state
                         :compass-calibration device-state/compass-calibration-state
                         :rec-osd device-state/rec-osd-state
                         :day-cam-glass-heater device-state/day-cam-glass-heater-state
                         :actual-space-time device-state/actual-space-time-state
                         :meteo-internal device-state/meteo-internal-state)]
        (compare-and-update-with-shadow! state-atom subsystem-data subsystem-key))))
  nil)

;; ============================================================================
;; State Dispatch Implementation
;; ============================================================================

(defrecord StateDispatch [running?]
  IStateDispatch

  (handle-state-message [_ binary-data]
    (try
      ;; Convert binary to EDN state using proto-bridge
      (when-let [state-map (bridge/binary->edn-state binary-data)]
        ;; Update all subsystems with EDN data
        (update-all-subsystems! state-map)
        ;; Send to channel subscribers
        (put! @state-channel state-map))
      (catch Exception e
        (logging/log-error {:msg "Failed to process state message"
                            :error (.getMessage e)
                            :bytes-length (count binary-data)}))))

  (dispose [_]
    (reset! running? false)
    (close! @state-channel)
    ;; Reset the shadow state to empty map
    (reset! shadow-state {})
    (reset! instance nil)
    ;; Create a new channel for next use
    (reset! state-channel (chan 100))))

;; ============================================================================
;; Singleton Management
;; ============================================================================

(>defn get-instance
  "Get the singleton StateDispatch instance"
  []
  [=> [:fn {:error/message "must be a StateDispatch instance"}
       #(instance? StateDispatch %)]]
  (when-not @instance
    (let [dispatch (->StateDispatch (atom true))]
      (reset! instance dispatch)
      (logging/log-info {:msg "StateDispatch initialized"})))
  @instance)

(>defn handle-binary-state
  "Public API to handle incoming binary state data"
  [binary-data]
  [bytes? => nil?]
  (let [dispatch (get-instance)]
    (handle-state-message dispatch binary-data))
  nil)

(>defn get-state-channel
  "Get the channel that receives decoded state maps"
  []
  [=> [:fn {:error/message "must be a core.async channel"}
       #(instance? ManyToManyChannel %)]]
  @state-channel)

(>defn dispose!
  "Clean up the singleton instance"
  []
  [=> nil?]
  (when-let [dispatch @instance]
    (dispose dispatch))
  nil)

;; ============================================================================
;; Configuration Functions
;; ============================================================================

(>defn enable-validation!
  "Enable state validation. Useful for development."
  [enabled?]
  [boolean? => nil?]
  (reset! validate-state? enabled?)
  (logging/log-info {:msg (str "State validation " (if enabled? "enabled" "disabled"))})
  nil)

(>defn enable-debug!
  "Enable debug logging for state changes"
  [enabled?]
  [boolean? => nil?]
  (reset! debug-mode? enabled?)
  (logging/log-info {:msg (str "State debug mode " (if enabled? "enabled" "disabled"))})
  nil)
