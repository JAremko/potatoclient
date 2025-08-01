(ns potatoclient.transit.debug
  "Debug utilities for Transit messages"
  (:require [clojure.data.json :as json]
            [clojure.pprint :as pprint]
            [cognitect.transit :as transit]
            [com.fulcrologic.guardrails.malli.core :refer [>defn =>]]
            [potatoclient.logging :as logging])
  (:import [java.io ByteArrayOutputStream ByteArrayInputStream]))

(>defn transit->json
  "Convert a Transit message to JSON string for debugging.
  This helps visualize Transit messages in a readable format."
  [transit-data]
  [any? => string?]
  (try
    ;; First encode to Transit
    (let [out (ByteArrayOutputStream.)
          writer (transit/writer out :msgpack)
          _ (transit/write writer transit-data)
          transit-bytes (.toByteArray out)

          ;; Then decode back to get a clean data structure
          in (ByteArrayInputStream. transit-bytes)
          reader (transit/reader in :msgpack)
          decoded (transit/read reader)]
      ;; Convert to JSON for readability
      (json/write-str decoded :indent true))
    (catch Exception e
      (str "Error converting to JSON: " (.getMessage e)))))

(>defn transit->edn
  "Convert a Transit message to EDN string for debugging."
  [transit-data]
  [any? => string?]
  (try
    (with-out-str (pprint/pprint transit-data))
    (catch Exception e
      (str "Error converting to EDN: " (.getMessage e)))))

(>defn log-transit-message
  "Log a Transit message in a readable format."
  [label message]
  [string? any? => nil?]
  (logging/log-debug
    (str label
         "\nEDN:\n" (transit->edn message)
         "\nJSON:\n" (transit->json message)))
  nil)

(>defn log-unknown-message
  "Log an unknown or malformed message with full details."
  [context message error-msg]
  [string? any? string? => nil?]
  (logging/log-warn
    {:id :transit-debug/unknown-message
     :data {:context context
            :error error-msg
            :message-type (type message)
            :message-keys (when (map? message) (keys message))
            :message-str (pr-str message)}
     :msg (str "Unknown/malformed message in " context ": " error-msg
               "\nMessage: " (transit->edn message))})
  nil)

(>defn validate-transit-envelope
  "Validate a Transit message has the required envelope structure.
  Returns [valid? error-msg]"
  [message]
  [any? => [:tuple boolean? [:maybe string?]]]
  (cond
    (not (map? message))
    [false "Message is not a map"]

    (not (:msg-type message))
    [false "Missing required :msg-type field"]

    (not (:msg-id message))
    [false "Missing required :msg-id field"]

    (not (:timestamp message))
    [false "Missing required :timestamp field"]

    (not (:payload message))
    [false "Missing required :payload field"]

    :else
    [true nil]))

;; For Kotlin side debugging
(>defn create-debug-response
  "Create a debug response message for testing."
  [original-msg error-details]
  [map? string? => map?]
  {:msg-type :debug
   :msg-id (str (java.util.UUID/randomUUID))
   :timestamp (System/currentTimeMillis)
   :payload {:original-msg-id (:msg-id original-msg)
             :original-msg-type (:msg-type original-msg)
             :error error-details
             :original-payload (:payload original-msg)}})