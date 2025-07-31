(ns potatoclient.transit.core
  "Transit MessagePack communication infrastructure with Guardrails specs"
  (:require [cognitect.transit :as transit]
            [com.fulcrologic.guardrails.malli.core :refer [=> >defn >defn- | ?]]
            [potatoclient.logging :as log])
  (:import [java.io OutputStream InputStream ByteArrayOutputStream ByteArrayInputStream]
           [cognitect.transit Writer Reader]
           [clojure.lang Keyword]))

;; Custom write handlers for domain types
(def ^{:doc "Custom Transit write handlers for domain types"}
  write-handlers
  {Keyword (transit/write-handler
             "keyword"
             (fn [^Keyword k] (subs (str k) 1))  ; Remove leading colon
             (fn [^Keyword k] (subs (str k) 1)))})

;; Custom read handlers for domain types  
(def ^{:doc "Custom Transit read handlers for domain types"}
  read-handlers
  {"keyword" (transit/read-handler keyword)})

;; Writer creation with Guardrails
(>defn make-writer
  "Create a Transit MessagePack writer with custom handlers"
  [out]
  [[:fn {:error/message "must be an OutputStream"}
    #(instance? OutputStream %)]
   => [:fn {:error/message "must be a Transit Writer"}
       #(instance? Writer %)]]
  (transit/writer out :msgpack {:handlers write-handlers}))

;; Reader creation with Guardrails
(>defn make-reader
  "Create a Transit MessagePack reader with custom handlers"
  [in]
  [[:fn {:error/message "must be an InputStream"}
    #(instance? InputStream %)]
   => [:fn {:error/message "must be a Transit Reader"}
       #(instance? Reader %)]]
  (transit/reader in :msgpack {:handlers read-handlers}))

;; Write operations
(>defn write-message!
  "Write a message to Transit writer and flush the stream"
  [writer msg out-stream]
  [[:fn {:error/message "must be a Transit Writer"}
    #(instance? Writer %)]
   any?
   [:fn {:error/message "must be an OutputStream"}
    #(instance? OutputStream %)]
   => nil?]
  (transit/write writer msg)
  (.flush ^OutputStream out-stream)
  nil)

;; Read operations
(>defn read-message
  "Read a single message from Transit reader"
  [reader]
  [[:fn {:error/message "must be a Transit Reader"}
    #(instance? Reader %)]
   => any?]
  (transit/read reader))

;; Message envelope validation
(>defn validate-message-envelope
  "Validate message has required envelope fields"
  [msg]
  [map? => boolean?]
  (and (contains? msg :msg-type)
       (contains? msg :msg-id)
       (contains? msg :timestamp)
       (contains? msg :payload)))

;; Create message envelope
(>defn create-message
  "Create a properly formatted message envelope"
  [msg-type payload]
  [keyword? any? => [:map
                     [:msg-type keyword?]
                     [:msg-id string?]
                     [:timestamp pos-int?]
                     [:payload any?]]]
  {:msg-type msg-type
   :msg-id (str (java.util.UUID/randomUUID))
   :timestamp (System/currentTimeMillis)
   :payload payload})

;; Encode to bytes
(>defn encode-to-bytes
  "Encode a message to Transit MessagePack bytes"
  [msg]
  [any? => bytes?]
  (let [baos (ByteArrayOutputStream.)]
    (write-message! (make-writer baos) msg baos)
    (.toByteArray baos)))

;; Decode from bytes
(>defn decode-from-bytes
  "Decode Transit MessagePack bytes to a message"
  [^bytes data]
  [bytes? => any?]
  (read-message (make-reader (ByteArrayInputStream. data))))

;; Message type predicates
(>defn state-message?
  "Check if message is a state update"
  [msg]
  [map? => boolean?]
  (and (validate-message-envelope msg)
       (= :state (:msg-type msg))))

(>defn command-message?
  "Check if message is a command"
  [msg]
  [map? => boolean?]
  (and (validate-message-envelope msg)
       (= :command (:msg-type msg))))

(>defn control-message?
  "Check if message is a control message"
  [msg]
  [map? => boolean?]
  (and (validate-message-envelope msg)
       (= :control (:msg-type msg))))

(>defn response-message?
  "Check if message is a response"
  [msg]
  [map? => boolean?]
  (and (validate-message-envelope msg)
       (= :response (:msg-type msg))))

(>defn validation-error-message?
  "Check if message is a validation error"
  [msg]
  [map? => boolean?]
  (and (validate-message-envelope msg)
       (= :validation-error (:msg-type msg))))

;; Logging helpers
(>defn- log-transit-error
  "Log Transit communication errors"
  [context error data]
  [string?
   [:fn {:error/message "must be a Throwable"}
    #(instance? Throwable %)]
   any? => nil?]
  (log/log-error {:msg "Transit communication error"
                  :context context
                  :error error
                  :data data})
  nil)

;; Safe encoding/decoding with error handling
(>defn safe-encode
  "Safely encode message, returning nil on error"
  [msg]
  [any? => (? bytes?)]
  (try
    (encode-to-bytes msg)
    (catch Exception e
      (log-transit-error "encoding" e msg)
      nil)))

(>defn safe-decode
  "Safely decode bytes, returning nil on error"
  [data]
  [bytes? => any?]
  (try
    (decode-from-bytes data)
    (catch Exception e
      (log-transit-error "decoding" e data)
      nil)))