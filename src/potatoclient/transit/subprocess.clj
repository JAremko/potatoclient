(ns potatoclient.transit.subprocess
  "Subprocess management for Transit-based state and command processors"
  (:require [potatoclient.transit.core :as transit]
            [potatoclient.runtime :as runtime]
            [potatoclient.specs :as specs]
            [com.fulcrologic.guardrails.malli.core :refer [=> >defn >defn- | ?]]
            [clojure.core.async :as async :refer [<! >! go go-loop chan close!]]
            [clojure.java.io :as io]
            [potatoclient.logging :as log])
  (:import [java.io InputStream OutputStream BufferedReader InputStreamReader File]
           [java.lang ProcessBuilder Process]
           [java.util.concurrent TimeUnit]))

;; Forward declarations
(declare start-subprocess-io-handlers!)

;; Channel types for type safety
(>defn- make-channel
  "Create a typed core.async channel"
  [buffer-size]
  [pos-int? => [:fn {:error/message "must be a core.async channel"}
                #(instance? clojure.core.async.impl.channels.ManyToManyChannel %)]]
  (chan buffer-size))

;; Subprocess record for type safety
(defrecord TransitSubprocess [process name in-chan out-chan control-chan])

;; Create subprocess channels
(>defn- create-subprocess-channels
  "Create channels for subprocess communication"
  []
  [=> [:map
       [:in [:fn {:error/message "must be a channel"}
             #(instance? clojure.core.async.impl.channels.ManyToManyChannel %)]]
       [:out [:fn {:error/message "must be a channel"}
              #(instance? clojure.core.async.impl.channels.ManyToManyChannel %)]]
       [:control [:fn {:error/message "must be a channel"}
                  #(instance? clojure.core.async.impl.channels.ManyToManyChannel %)]]]]
  {:in (make-channel 100)
   :out (make-channel 100)
   :control (make-channel 10)})

;; Launch subprocess
(>defn- create-process-builder
  "Create a ProcessBuilder for the Transit subprocess"
  [kotlin-class args]
  [string? [:sequential string?] => [:fn #(instance? ProcessBuilder %)]]
  (let [java-exe (or (when-let [java-home (System/getProperty "java.home")]
                       (let [sep (System/getProperty "file.separator")
                             java-path (str java-home sep "bin" sep "java")]
                         (when (.exists (io/file java-path))
                           java-path)))
                     "java")
        classpath (System/getProperty "java.class.path")
        jvm-args ["-Dfile.encoding=UTF-8"
                  "-Djdk.attach.allowAttachSelf"
                  "--enable-native-access=ALL-UNNAMED"]
        cmd (vec (concat [java-exe "-cp" classpath]
                         jvm-args
                         [kotlin-class]
                         args))]
    (ProcessBuilder. ^java.util.List cmd)))

(>defn launch-transit-subprocess
  "Launch a Kotlin subprocess for Transit communication"
  [subprocess-type ws-url]
  [[:enum :state-proc :cmd-proc] ::specs/url => [:fn {:error/message "must be a TransitSubprocess"}
                                                 #(instance? TransitSubprocess %)]]
  (let [kotlin-class (case subprocess-type
                       :state-proc "potatoclient.transit.StateSubprocess"
                       :cmd-proc "potatoclient.transit.CommandSubprocess")
        process-name (name subprocess-type)
        channels (create-subprocess-channels)
        pb (create-process-builder kotlin-class [ws-url])
        process (.start pb)]

    (log/log-info {:msg "Launching Transit subprocess"
                   :type subprocess-type
                   :class kotlin-class
                   :url ws-url})

    ;; Start I/O handlers
    (start-subprocess-io-handlers! process channels process-name)

    (->TransitSubprocess process process-name
                         (:in channels) (:out channels) (:control channels))))

;; Shutdown subprocess
(>defn- shutdown-subprocess!
  "Gracefully shutdown a subprocess"
  [^Process process channels process-name]
  [[:fn {:error/message "must be a Process"} #(instance? Process %)]
   [:map [:in any?] [:out any?] [:control any?]]
   string? => nil?]
  (log/log-info {:msg "Shutting down subprocess" :process process-name})

  ;; Close channels
  (close! (:in channels))
  (close! (:out channels))
  (close! (:control channels))

  ;; Terminate process
  (when (.isAlive process)
    (.destroy process)
    (when-not (.waitFor process 5 TimeUnit/SECONDS)
      (.destroyForcibly process)))

  nil)

;; Start I/O handlers
(>defn- start-subprocess-io-handlers!
  "Start async handlers for subprocess I/O"
  [^Process process channels process-name]
  [[:fn {:error/message "must be a Process"} #(instance? Process %)]
   [:map [:in any?] [:out any?] [:control any?]]
   string? => nil?]

  ;; Handle stdout (subprocess -> main)
  (go-loop []
    (let [reader (transit/make-reader (.getInputStream process))]
      (loop []
        (when (.isAlive process)
          (try
            (when-let [msg (transit/read-message reader)]
              (>! (:out channels) msg))
            (catch Exception e
              (when (.isAlive process)
                (log/log-error {:msg "Error reading from subprocess"
                                :process process-name
                                :error e}))))
          (recur))))
    (log/log-info {:msg "Subprocess stdout handler stopped" :process process-name}))

  ;; Handle stdin (main -> subprocess)
  (go-loop []
    (let [out-stream (.getOutputStream process)
          writer (transit/make-writer out-stream)]
      (loop []
        (when-let [msg (<! (:in channels))]
          (try
            (transit/write-message! writer msg out-stream)
            (catch Exception e
              (log/log-error {:msg "Error writing to subprocess"
                              :process process-name
                              :error e})))
          (recur))))
    (log/log-info {:msg "Subprocess stdin handler stopped" :process process-name}))

  ;; Handle control messages
  (go-loop []
    (when-let [ctrl-msg (<! (:control channels))]
      (case (:action ctrl-msg)
        :shutdown (shutdown-subprocess! process channels process-name)
        (log/log-warn {:msg "Unknown control action" :action (:action ctrl-msg)}))
      (recur)))

  nil)

;; Send message to subprocess
(>defn send-to-subprocess!
  "Send a message to a subprocess"
  [subprocess msg]
  [[:fn {:error/message "must be a TransitSubprocess"} #(instance? TransitSubprocess %)]
   map? => boolean?]
  (async/put! (:in-chan subprocess) msg))

;; Receive message from subprocess
(>defn receive-from-subprocess
  "Receive a message from a subprocess (blocking)"
  [subprocess timeout-ms]
  [[:fn {:error/message "must be a TransitSubprocess"} #(instance? TransitSubprocess %)]
   pos-int? => any?]
  (async/alt!!
    (:out-chan subprocess) ([msg] msg)
    (async/timeout timeout-ms) nil))

;; Public shutdown function
(>defn shutdown-transit-subprocess!
  "Shutdown a Transit subprocess"
  [subprocess]
  [[:fn {:error/message "must be a TransitSubprocess"} #(instance? TransitSubprocess %)]
   => nil?]
  (async/put! (:control-chan subprocess) {:action :shutdown})
  nil)

;; Check if subprocess is alive
(>defn subprocess-alive?
  "Check if a subprocess is still running"
  [subprocess]
  [[:fn {:error/message "must be a TransitSubprocess"} #(instance? TransitSubprocess %)]
   => boolean?]
  (.isAlive ^Process (:process subprocess)))

;; Control message helpers
(>defn send-control-message!
  "Send a control message to subprocess"
  [subprocess action data]
  [[:fn {:error/message "must be a TransitSubprocess"} #(instance? TransitSubprocess %)]
   keyword? map? => boolean?]
  (send-to-subprocess! subprocess
                       (transit/create-message :control
                                               (assoc data :action action))))

;; Common control operations
(>defn set-rate-limit!
  "Set rate limit for state subprocess"
  [subprocess max-hz]
  [[:fn {:error/message "must be a TransitSubprocess"} #(instance? TransitSubprocess %)]
   [:int {:min 1 :max 120}] => boolean?]
  (send-control-message! subprocess :set-rate-limit {:max-hz max-hz}))

(>defn enable-validation!
  "Enable/disable validation in subprocess"
  [subprocess enabled?]
  [[:fn {:error/message "must be a TransitSubprocess"} #(instance? TransitSubprocess %)]
   boolean? => boolean?]
  (send-control-message! subprocess :set-validation {:enabled enabled?}))

(>defn get-subprocess-logs
  "Get recent logs from subprocess"
  [subprocess lines]
  [[:fn {:error/message "must be a TransitSubprocess"} #(instance? TransitSubprocess %)]
   [:int {:min 1 :max 10000}] => boolean?]
  (send-control-message! subprocess :get-logs {:lines lines}))