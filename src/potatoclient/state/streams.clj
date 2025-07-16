(ns potatoclient.state.streams
  "Stream process state management."
  (:require [malli.core :as m]
            [potatoclient.specs :as specs]))

;; Stream process references
(defonce ^:private streams-state
  (atom {:heat nil
         :day nil}))

(defn get-stream
  "Get a stream process by key (:heat or :day)."
  [stream-key]
  {:pre [(m/validate specs/stream-key stream-key)]}
  (get @streams-state stream-key))

(defn set-stream!
  "Set a stream process."
  [stream-key stream]
  {:pre [(m/validate specs/stream-key stream-key)
         (map? stream)]}
  (swap! streams-state assoc stream-key stream))

(defn clear-stream!
  "Clear a stream process."
  [stream-key]
  {:pre [(m/validate specs/stream-key stream-key)]}
  (swap! streams-state assoc stream-key nil))

(defn all-streams
  "Get all stream entries as a map."
  []
  @streams-state)

;; Legacy compatibility
(def app-state streams-state)