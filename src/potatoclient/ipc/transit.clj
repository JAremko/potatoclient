(ns potatoclient.ipc.transit
  "Transit serialization/deserialization for IPC messages.
   Uses msgpack format for efficient binary encoding."
  (:require
    [malli.core :as m]
    [cognitect.transit :as transit]
    [potatoclient.malli.registry :as registry])
  (:import
    (com.cognitect.transit Keyword TransitFactory)
    (java.io ByteArrayInputStream ByteArrayOutputStream)))

;; ============================================================================
;; Specs
;; ============================================================================

(def TransitMessage
  "A Clojure map representing a Transit message."
  [:map-of :keyword :any])

(def ByteArray
  "A byte array."
  [:fn bytes?])

;; Register specs with shared registry
(registry/register-spec! :potatoclient.ipc/transit-message TransitMessage)
(registry/register-spec! :potatoclient.ipc/byte-array ByteArray)

;; ============================================================================
;; Transit Serialization
;; ============================================================================

(defn write-message
  "Serialize a Clojure map to Transit msgpack bytes."
  [message]
  (let [baos (ByteArrayOutputStream.)
        writer (transit/writer baos :msgpack)]
    (transit/write writer message)
    (.toByteArray baos)))
(m/=> write-message [:=> [:cat TransitMessage] ByteArray])

(defn read-message
  "Deserialize Transit msgpack bytes to a Clojure map.
   Transit Keywords are automatically converted to Clojure keywords."
  [bytes]
  (let [bais (ByteArrayInputStream. bytes)
        reader (transit/reader bais :msgpack)]
    (transit/read reader)))
(m/=> read-message [:=> [:cat ByteArray] TransitMessage])

;; ============================================================================
;; Transit Keyword Helpers
;; ============================================================================

(defn keyword->transit
  "Convert a Clojure keyword to a Transit Keyword.
   Used when we need to create messages compatible with Kotlin/Java."
  [kw]
  (TransitFactory/keyword (name kw)))
(m/=> keyword->transit [:=> [:cat :keyword] [:fn (partial instance? Keyword)]])

(defn transit->keyword
  "Convert a Transit Keyword to a Clojure keyword.
   Note: Transit reader usually handles this automatically."
  [^Keyword transit-kw]
  (let [s (.toString transit-kw)]
    (keyword (if (.startsWith s ":")
               (subs s 1)
               s))))
(m/=> transit->keyword [:=> [:cat [:fn (partial instance? Keyword)]] :keyword])

;; ============================================================================
;; Message Construction Helpers
;; ============================================================================

(defn create-message
  "Create a message with standard envelope fields."
  [msg-type payload]
  (merge {:msg-type msg-type
          :timestamp (System/currentTimeMillis)}
         payload))
(m/=> create-message [:=> [:cat :keyword [:map-of :keyword :any]] TransitMessage])

(defn create-event
  "Create an event message."
  [event-type data]
  (create-message :event
                  (merge {:type event-type} data)))
(m/=> create-event [:=> [:cat :keyword [:map-of :keyword :any]] TransitMessage])

(defn create-log
  "Create a log message."
  [level message & [data]]
  (create-message :log
                  (cond-> {:level level
                           :message message}
                          data (assoc :data data))))
(m/=> create-log [:=> [:cat :keyword :string [:? [:map-of :keyword :any]]] TransitMessage])

(defn create-command
  "Create a command message."
  [action data]
  (create-message :command
                  (merge {:action action} data)))
(m/=> create-command [:=> [:cat :keyword [:map-of :keyword :any]] TransitMessage])

(defn create-metric
  "Create a metric message."
  [metric-data]
  (create-message :metric metric-data))
(m/=> create-metric [:=> [:cat [:map-of :keyword :any]] TransitMessage])
