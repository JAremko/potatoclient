(ns potatoclient.state.proto-bridge
  "Isolated protobuf conversion layer.
  
  This namespace handles all conversions between protobuf binary messages
  and EDN data structures. All protobuf dependencies are isolated here."
  (:require [com.fulcrologic.guardrails.malli.core :refer [=> >defn >defn- ?]]
            [potatoclient.logging :as logging]
            [potatoclient.proto :as proto]
            [potatoclient.state.edn :as edn])
  (:import (ser JonSharedData$JonGUIState)))

;; ============================================================================
;; Binary <-> EDN Conversion
;; ============================================================================

(>defn binary->edn-state
  "Convert binary protobuf message to EDN state map.
  Returns nil if parsing fails."
  [binary-data]
  [bytes? => (? map?)]
  (try
    (when (pos? (count binary-data))
      (let [proto-msg (JonSharedData$JonGUIState/parseFrom ^bytes binary-data)]
        (proto/proto-map->clj-map proto-msg)))
    (catch Exception e
      (logging/log-error {:msg "Failed to parse protobuf binary data"
                          :error (.getMessage e)
                          :bytes-length (count binary-data)})
      nil)))

(>defn edn-state->binary
  "Convert EDN state map to binary protobuf message.
  NOTE: This is a placeholder implementation for testing.
  In production, state only flows from proto -> EDN, not the reverse."
  [state-map]
  [map? => (? bytes?)]
  ;; For now, just return nil - we don't need EDN->proto conversion
  ;; in production since state flows from server to client only
  (do
    (logging/log-warn {:msg "EDN to protobuf conversion not implemented"
                       :state-keys (keys state-map)})
    nil))

;; ============================================================================
;; Protobuf Object <-> EDN Conversion
;; ============================================================================

(>defn proto-msg->edn-state
  "Convert a protobuf JonGUIState object to EDN state map.
  This is used when we already have the parsed protobuf object."
  [proto-msg]
  [[:fn {:error/message "must be a JonGUIState protobuf"}
    #(instance? JonSharedData$JonGUIState %)] => map?]
  (proto/proto-map->clj-map proto-msg))

(>defn extract-subsystem-edn
  "Extract a specific subsystem from a protobuf message as EDN"
  [proto-msg subsystem-k]
  [[:fn {:error/message "must be a JonGUIState protobuf"}
    #(instance? JonSharedData$JonGUIState %)] 
   edn/subsystem-key => (? map?)]
  (case subsystem-k
    :system (when (.hasSystem proto-msg)
              (proto/proto-map->clj-map (.getSystem proto-msg)))
    :lrf (when (.hasLrf proto-msg)
           (proto/proto-map->clj-map (.getLrf proto-msg)))
    :time (when (.hasTime proto-msg)
            (proto/proto-map->clj-map (.getTime proto-msg)))
    :gps (when (.hasGps proto-msg)
           (proto/proto-map->clj-map (.getGps proto-msg)))
    :compass (when (.hasCompass proto-msg)
               (proto/proto-map->clj-map (.getCompass proto-msg)))
    :rotary (when (.hasRotary proto-msg)
              (proto/proto-map->clj-map (.getRotary proto-msg)))
    :camera-day (when (.hasCameraDay proto-msg)
                  (proto/proto-map->clj-map (.getCameraDay proto-msg)))
    :camera-heat (when (.hasCameraHeat proto-msg)
                   (proto/proto-map->clj-map (.getCameraHeat proto-msg)))
    :compass-calibration (when (.hasCompassCalibration proto-msg)
                           (proto/proto-map->clj-map (.getCompassCalibration proto-msg)))
    :rec-osd (when (.hasRecOsd proto-msg)
               (proto/proto-map->clj-map (.getRecOsd proto-msg)))
    :day-cam-glass-heater (when (.hasDayCamGlassHeater proto-msg)
                            (proto/proto-map->clj-map (.getDayCamGlassHeater proto-msg)))
    :actual-space-time (when (.hasActualSpaceTime proto-msg)
                         (proto/proto-map->clj-map (.getActualSpaceTime proto-msg)))
    :meteo-internal (when (try (.hasMeteoInternal proto-msg) (catch Exception _ false))
                      (proto/proto-map->clj-map (.getMeteoInternal proto-msg)))
    nil))

(>defn has-subsystem?
  "Check if a protobuf message has a specific subsystem"
  [proto-msg subsystem-k]
  [[:fn {:error/message "must be a JonGUIState protobuf"}
    #(instance? JonSharedData$JonGUIState %)] 
   edn/subsystem-key => boolean?]
  (case subsystem-k
    :system (.hasSystem proto-msg)
    :lrf (.hasLrf proto-msg)
    :time (.hasTime proto-msg)
    :gps (.hasGps proto-msg)
    :compass (.hasCompass proto-msg)
    :rotary (.hasRotary proto-msg)
    :camera-day (.hasCameraDay proto-msg)
    :camera-heat (.hasCameraHeat proto-msg)
    :compass-calibration (.hasCompassCalibration proto-msg)
    :rec-osd (.hasRecOsd proto-msg)
    :day-cam-glass-heater (.hasDayCamGlassHeater proto-msg)
    :actual-space-time (.hasActualSpaceTime proto-msg)
    :meteo-internal (try (.hasMeteoInternal proto-msg) (catch Exception _ false))
    false))

;; ============================================================================
;; State Comparison
;; ============================================================================

(>defn changed?
  "Check if two EDN states are different.
  This is a simple equality check on the EDN data."
  [old-state new-state]
  [(? map?) (? map?) => boolean?]
  (not= old-state new-state))

(>defn subsystem-changed?
  "Check if a specific subsystem has changed between states"
  [old-state new-state subsystem-k]
  [(? map?) (? map?) edn/subsystem-key => boolean?]
  (not= (get old-state subsystem-k)
        (get new-state subsystem-k)))

;; ============================================================================
;; Protobuf Parsing
;; ============================================================================

(>defn parse-gui-state
  "Parse a JonGUIState from binary data.
  Returns the protobuf object or nil on failure."
  [binary-data]
  [bytes? => (? [:fn {:error/message "must be a JonGUIState protobuf"}
                 #(instance? JonSharedData$JonGUIState %)])]
  (try
    (JonSharedData$JonGUIState/parseFrom ^bytes binary-data)
    (catch Exception e
      (logging/log-error {:msg "Failed to parse JonGUIState"
                          :error (.getMessage e)
                          :bytes-length (count binary-data)})
      nil)))

;; ============================================================================
;; Subsystem List
;; ============================================================================

(def subsystem-keys
  "List of all subsystem keys in the state"
  [:system :lrf :time :gps :compass :rotary :camera-day :camera-heat
   :compass-calibration :rec-osd :day-cam-glass-heater :actual-space-time
   :meteo-internal])

(>defn extract-all-subsystems
  "Extract all subsystems from a protobuf message to EDN"
  [proto-msg]
  [[:fn {:error/message "must be a JonGUIState protobuf"}
    #(instance? JonSharedData$JonGUIState %)] => map?]
  (reduce (fn [acc subsystem-key]
            (if-let [subsystem-data (extract-subsystem-edn proto-msg subsystem-key)]
              (assoc acc subsystem-key subsystem-data)
              acc))
          {:protocol-version (.getProtocolVersion proto-msg)}
          subsystem-keys))