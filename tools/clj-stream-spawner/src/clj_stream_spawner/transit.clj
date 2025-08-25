(ns clj-stream-spawner.transit
  "Transit serialization/deserialization for IPC messages.
   Uses msgpack format for efficient binary encoding."
  (:require
    [cognitect.transit :as transit]
    [com.fulcrologic.guardrails.malli.core :refer [=> >defn]])
  (:import
    (com.cognitect.transit Keyword TransitFactory)
    (java.io ByteArrayInputStream ByteArrayOutputStream)))

;; ============================================================================
;; Specs
;; ============================================================================

(def TransitMessage
  [:map-of :keyword :any])

(def ByteArray
  [:fn #(bytes? %)])

;; ============================================================================
;; Transit Serialization
;; ============================================================================

(>defn write-message
  "Serialize a Clojure map to Transit msgpack bytes."
  [message]
  [TransitMessage => ByteArray]
  (let [baos (ByteArrayOutputStream.)
        writer (transit/writer baos :msgpack)]
    (transit/write writer message)
    (.toByteArray baos)))

(>defn read-message
  "Deserialize Transit msgpack bytes to a Clojure map.
   Transit Keywords are automatically converted to Clojure keywords."
  [bytes]
  [ByteArray => TransitMessage]
  (let [bais (ByteArrayInputStream. bytes)
        reader (transit/reader bais :msgpack)]
    (transit/read reader)))

;; ============================================================================
;; Transit Keyword Helpers
;; ============================================================================

(>defn keyword->transit
  "Convert a Clojure keyword to a Transit Keyword.
   Used when we need to create messages compatible with Kotlin/Java."
  [kw]
  [:keyword => [:fn #(instance? Keyword %)]]
  (TransitFactory/keyword (name kw)))

(>defn transit->keyword
  "Convert a Transit Keyword to a Clojure keyword.
   Note: Transit reader usually handles this automatically."
  [^Keyword transit-kw]
  [[:fn #(instance? Keyword %)] => :keyword]
  (let [s (.toString transit-kw)]
    (keyword (if (.startsWith s ":")
               (subs s 1)
               s))))

;; ============================================================================
;; Message Construction Helpers
;; ============================================================================

(>defn create-message
  "Create a message with standard envelope fields."
  [msg-type payload]
  [:keyword [:map-of :keyword :any] => TransitMessage]
  (merge {:msg-type msg-type
          :timestamp (System/currentTimeMillis)}
         payload))

(>defn create-event
  "Create an event message."
  [event-type data]
  [:keyword [:map-of :keyword :any] => TransitMessage]
  (create-message :event
                  (merge {:type event-type} data)))

(>defn create-log
  "Create a log message."
  [level message & [data]]
  [:keyword :string [:? [:map-of :keyword :any]] => TransitMessage]
  (create-message :log
                  (cond-> {:level level
                           :message message}
                    data (assoc :data data))))

(>defn create-command
  "Create a command message."
  [action data]
  [:keyword [:map-of :keyword :any] => TransitMessage]
  (create-message :command
                  (merge {:action action} data)))

(>defn create-metric
  "Create a metric message."
  [metric-data]
  [[:map-of :keyword :any] => TransitMessage]
  (create-message :metric metric-data))
