(ns potatoclient.ipc.transit
  "Transit serialization/deserialization for IPC messages.
   Uses msgpack format for efficient binary encoding."
  (:require
    [cognitect.transit :as transit]
    [potatoclient.malli.registry :as registry])
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

;; Register specs with shared registry
(registry/register-spec! :potatoclient.ipc/transit-message TransitMessage)
(registry/register-spec! :potatoclient.ipc/byte-array ByteArray)

;; ============================================================================
;; Transit Serialization
;; ============================================================================

(defn write-message
  "Serialize a Clojure map to Transit msgpack bytes." {:malli/schema [:=> [:cat TransitMessage] ByteArray]}
  [message]
  (let [baos (ByteArrayOutputStream.)
        writer (transit/writer baos :msgpack)]
    (transit/write writer message)
    (.toByteArray baos)))

(defn read-message
  "Deserialize Transit msgpack bytes to a Clojure map.
   Transit Keywords are automatically converted to Clojure keywords." {:malli/schema [:=> [:cat ByteArray] TransitMessage]}
  [bytes]
  (let [bais (ByteArrayInputStream. bytes)
        reader (transit/reader bais :msgpack)]
    (transit/read reader)))

;; ============================================================================
;; Transit Keyword Helpers
;; ============================================================================

(defn keyword->transit
  "Convert a Clojure keyword to a Transit Keyword.
   Used when we need to create messages compatible with Kotlin/Java." {:malli/schema [:=> [:cat :keyword] [:fn (fn* [p1__3792#] (instance? Keyword p1__3792#))]]}
  [kw]
  (TransitFactory/keyword (name kw)))

(defn transit->keyword
  "Convert a Transit Keyword to a Clojure keyword.
   Note: Transit reader usually handles this automatically." {:malli/schema [:=> [:cat [:fn (fn* [p1__3794#] (instance? Keyword p1__3794#))]] :keyword]}
  [^Keyword transit-kw]
  (let [s (.toString transit-kw)]
    (keyword (if (.startsWith s ":")
               (subs s 1)
               s))))

;; ============================================================================
;; Message Construction Helpers
;; ============================================================================

(defn create-message
  "Create a message with standard envelope fields." {:malli/schema [:=> [:cat :keyword [:map-of :keyword :any]] TransitMessage]}
  [msg-type payload]
  (merge {:msg-type msg-type
          :timestamp (System/currentTimeMillis)}
         payload))

(defn create-event
  "Create an event message." {:malli/schema [:=> [:cat :keyword [:map-of :keyword :any]] TransitMessage]}
  [event-type data]
  (create-message :event
                  (merge {:type event-type} data)))

(defn create-log
  "Create a log message." {:malli/schema [:=> [:cat :keyword :string [:? [:map-of :keyword :any]]] TransitMessage]}
  [level message & [data]]
  (create-message :log
                  (cond-> {:level level
                           :message message}
                    data (assoc :data data))))

(defn create-command
  "Create a command message." {:malli/schema [:=> [:cat :keyword [:map-of :keyword :any]] TransitMessage]}
  [action data]
  (create-message :command
                  (merge {:action action} data)))

(defn create-metric
  "Create a metric message." {:malli/schema [:=> [:cat [:map-of :keyword :any]] TransitMessage]}
  [metric-data]
  (create-message :metric metric-data))