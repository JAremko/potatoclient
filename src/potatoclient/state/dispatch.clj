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
            [potatoclient.state.schemas :as schemas])
  (:import (clojure.core.async.impl.channels ManyToManyChannel)
           (ser JonSharedData$JonGUIState)))

;; ============================================================================
;; Singleton State
;; ============================================================================

(defonce ^:private instance (atom nil))

;; Channel for raw binary state messages
(defonce ^:private state-channel (atom (chan 100)))

;; Shadow state - mutable protobuf builder for type-safe comparison
;; This maintains the last known state in protobuf form
(defonce ^:private shadow-state-builder
  (atom (JonSharedData$JonGUIState/newBuilder)))

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

(>defn- protobuf-changed?
  "Check if protobuf message has changed using Java equals().
  Protobuf messages implement deep equality comparison."
  [old-proto new-proto]
  [(? any?) (? any?) => boolean?]
  (if (and old-proto new-proto)
    (not (.equals old-proto new-proto))
    (not= (nil? old-proto) (nil? new-proto))))

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

(>defn- convert-proto-to-edn
  "Convert protobuf message to EDN if not nil"
  [proto-msg]
  [(? any?) => (? any?)]
  (when proto-msg
    (proto/proto-map->clj-map proto-msg)))

(>defn- get-shadow-subsystem
  "Get the current subsystem value from shadow state builder"
  [builder subsystem-key]
  [any? keyword? => (? any?)]
  (case subsystem-key
    :system (when (.hasSystem builder) (.getSystem builder))
    :lrf (when (.hasLrf builder) (.getLrf builder))
    :time (when (.hasTime builder) (.getTime builder))
    :gps (when (.hasGps builder) (.getGps builder))
    :compass (when (.hasCompass builder) (.getCompass builder))
    :rotary (when (.hasRotary builder) (.getRotary builder))
    :camera-day (when (.hasCameraDay builder) (.getCameraDay builder))
    :camera-heat (when (.hasCameraHeat builder) (.getCameraHeat builder))
    :compass-calibration (when (.hasCompassCalibration builder) (.getCompassCalibration builder))
    :rec-osd (when (.hasRecOsd builder) (.getRecOsd builder))
    :day-cam-glass-heater (when (.hasDayCamGlassHeater builder) (.getDayCamGlassHeater builder))
    :actual-space-time (when (.hasActualSpaceTime builder) (.getActualSpaceTime builder))
    :meteo-internal (when (.hasMeteoInternal builder) (.getMeteoInternal builder))
    nil))

(>defn- update-shadow-subsystem!
  "Update a subsystem in the shadow state builder"
  [builder subsystem-key proto-msg]
  [any? keyword? (? any?) => nil?]
  (when proto-msg
    (case subsystem-key
      :system (.setSystem builder proto-msg)
      :lrf (.setLrf builder proto-msg)
      :time (.setTime builder proto-msg)
      :gps (.setGps builder proto-msg)
      :compass (.setCompass builder proto-msg)
      :rotary (.setRotary builder proto-msg)
      :camera-day (.setCameraDay builder proto-msg)
      :camera-heat (.setCameraHeat builder proto-msg)
      :compass-calibration (.setCompassCalibration builder proto-msg)
      :rec-osd (.setRecOsd builder proto-msg)
      :day-cam-glass-heater (.setDayCamGlassHeater builder proto-msg)
      :actual-space-time (.setActualSpaceTime builder proto-msg)
      :meteo-internal (.setMeteoInternal builder proto-msg)))
  nil)

(>defn- compare-and-update-with-shadow!
  "Compare protobuf objects using shadow state builder, update atom only if changed.
  Uses the type-safe builder pattern for maintaining shadow state.
  Only converts to EDN when actually updating the atom.
  Returns true if state was updated."
  [state-atom proto-msg subsystem-key]
  [any? (? any?) keyword? => boolean?]
  (let [builder @shadow-state-builder
        shadow-proto (get-shadow-subsystem builder subsystem-key)]
    (if (protobuf-changed? shadow-proto proto-msg)
      (let [edn-value (convert-proto-to-edn proto-msg)
            validated-value (validate-subsystem-data subsystem-key edn-value)]
        ;; Update shadow state builder (type-safe)
        (update-shadow-subsystem! builder subsystem-key proto-msg)
        ;; Update atom with EDN value
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
  "Update all subsystem atoms with protobuf message data.
  The proto-msg should be a JonGUIState protobuf message object."
  [proto-msg]
  [any? => nil?]
  ;; Use protobuf getter methods to access subsystem messages
  (when (.hasSystem proto-msg)
    (compare-and-update-with-shadow! device-state/system-state (.getSystem proto-msg) :system))
  (when (.hasLrf proto-msg)
    (compare-and-update-with-shadow! device-state/lrf-state (.getLrf proto-msg) :lrf))
  (when (.hasTime proto-msg)
    (compare-and-update-with-shadow! device-state/time-state (.getTime proto-msg) :time))
  (when (.hasGps proto-msg)
    (compare-and-update-with-shadow! device-state/gps-state (.getGps proto-msg) :gps))
  (when (.hasCompass proto-msg)
    (compare-and-update-with-shadow! device-state/compass-state (.getCompass proto-msg) :compass))
  (when (.hasRotary proto-msg)
    (compare-and-update-with-shadow! device-state/rotary-state (.getRotary proto-msg) :rotary))
  (when (.hasCameraDay proto-msg)
    (compare-and-update-with-shadow! device-state/camera-day-state (.getCameraDay proto-msg) :camera-day))
  (when (.hasCameraHeat proto-msg)
    (compare-and-update-with-shadow! device-state/camera-heat-state (.getCameraHeat proto-msg) :camera-heat))
  (when (.hasCompassCalibration proto-msg)
    (compare-and-update-with-shadow! device-state/compass-calibration-state (.getCompassCalibration proto-msg) :compass-calibration))
  (when (.hasRecOsd proto-msg)
    (compare-and-update-with-shadow! device-state/rec-osd-state (.getRecOsd proto-msg) :rec-osd))
  (when (.hasDayCamGlassHeater proto-msg)
    (compare-and-update-with-shadow! device-state/day-cam-glass-heater-state (.getDayCamGlassHeater proto-msg) :day-cam-glass-heater))
  (when (.hasActualSpaceTime proto-msg)
    (compare-and-update-with-shadow! device-state/actual-space-time-state (.getActualSpaceTime proto-msg) :actual-space-time))
  ;; Note: meteo-internal might need special handling if it exists
  (when (try (.hasMeteoInternal proto-msg) (catch Exception _ false))
    (compare-and-update-with-shadow! device-state/meteo-internal-state (.getMeteoInternal proto-msg) :meteo-internal))
  nil)

;; ============================================================================
;; State Dispatch Implementation
;; ============================================================================

(defrecord StateDispatch [running?]
  IStateDispatch

  (handle-state-message [_ binary-data]
    (try
      ;; Parse protobuf message directly
      (let [proto-msg (JonSharedData$JonGUIState/parseFrom ^bytes binary-data)]
        ;; Update using protobuf objects for comparison
        (update-all-subsystems! proto-msg)
        ;; Convert to EDN for channel subscribers
        (let [state-map (proto/proto-map->clj-map proto-msg)]
          (put! @state-channel state-map)))
      (catch Exception e
        (logging/log-error {:msg "Failed to deserialize state message"
                            :error (.getMessage e)
                            :bytes-length (count binary-data)}))))

  (dispose [_]
    (reset! running? false)
    (close! @state-channel)
    ;; Reset the builder to a fresh instance
    (reset! shadow-state-builder (JonSharedData$JonGUIState/newBuilder))
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
