(ns potatoclient.state.streams
  "Stream process state management."
  (:require [com.fulcrologic.guardrails.malli.core :refer [=> >defn ?]]))

;; Stream process references
(defonce ^:private streams-state
  (atom {:heat nil
         :day nil}))

(>defn get-stream
  "Get a stream process by key (:heat or :day)."
  [stream-key]
  [:potatoclient.specs/stream-key => (? :potatoclient.specs/stream-process-map)]
  (get @streams-state stream-key))

(>defn set-stream!
  "Set a stream process."
  [stream-key stream]
  [:potatoclient.specs/stream-key :potatoclient.specs/stream-process-map => [:map-of :potatoclient.specs/stream-key [:maybe :potatoclient.specs/stream-process-map]]]
  (swap! streams-state assoc stream-key stream))

(>defn clear-stream!
  "Clear a stream process."
  [stream-key]
  [:potatoclient.specs/stream-key => [:map-of :potatoclient.specs/stream-key [:maybe :potatoclient.specs/stream-process-map]]]
  (swap! streams-state assoc stream-key nil))

(>defn all-streams
  "Get all stream entries as a map."
  []
  [=> [:map-of :potatoclient.specs/stream-key (? :potatoclient.specs/stream-process-map)]]
  @streams-state)

;; Legacy compatibility
(def app-state streams-state)