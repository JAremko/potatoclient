(ns mock-video-stream.process
  "Mock video stream subprocess that communicates via Transit over stdin/stdout.
  Simulates a real video stream subprocess for testing."
  (:require [mock-video-stream.core :as core]
            [mock-video-stream.gesture-sim :as gesture-sim]
            [potatoclient.transit.core :as transit-core]
            [cognitect.transit :as transit]
            [taoensso.telemere :as log]
            [clojure.java.io :as io])
  (:import [java.io PushbackInputStream BufferedOutputStream]
           [java.util UUID]))

;; ============================================================================
;; Transit Communication
;; ============================================================================

(defn create-transit-reader
  "Create a Transit reader for stdin"
  []
  (transit/reader (PushbackInputStream. System/in) :msgpack))

(defn create-transit-writer
  "Create a Transit writer for stdout"
  []
  (transit/writer (BufferedOutputStream. System/out) :msgpack))

(defn send-message
  "Send a Transit message to stdout"
  [writer msg-type payload]
  (let [message {:msg-type msg-type
                 :msg-id (str (UUID/randomUUID))
                 :timestamp (System/currentTimeMillis)
                 :payload payload}]
    (transit/write writer message)
    (.flush System/out)))

(defn send-command
  "Send a command message"
  [writer command]
  (send-message writer :command command))

(defn send-event
  "Send an event message"
  [writer event-type event-data]
  (send-message writer :event {:type event-type
                               :data event-data}))

(defn send-log
  "Send a log message"
  [writer level message data]
  (send-message writer :log {:level level
                            :message message
                            :data data
                            :process "mock-video-stream"}))

;; ============================================================================
;; Message Handlers
;; ============================================================================

(defmulti handle-message 
  "Handle incoming messages based on type"
  (fn [msg _ _] (:msg-type msg)))

(defmethod handle-message :control
  [msg state writer]
  (let [response (core/handle-message msg)]
    (send-message writer :response (merge {:request-id (:msg-id msg)}
                                          response))))

(defmethod handle-message :request
  [msg state writer]
  (case (get-in msg [:payload :type])
    :inject-mouse-event
    (let [event (get-in msg [:payload :event])
          result (gesture-sim/process-mouse-event event state)]
      (when-let [command (:command result)]
        (send-command writer command))
      (when-let [gesture-event (:gesture-event result)]
        (send-event writer :gesture gesture-event))
      (send-message writer :response {:request-id (:msg-id msg)
                                     :status :ok}))
    
    ;; Default
    (send-message writer :response {:request-id (:msg-id msg)
                                   :status :error
                                   :message "Unknown request type"})))

(defmethod handle-message :default
  [msg _ writer]
  (send-log writer :warn "Unknown message type" {:msg-type (:msg-type msg)}))

;; ============================================================================
;; Main Process Loop
;; ============================================================================

(defn start-mock-process
  "Start the mock video stream subprocess"
  [{:keys [stream-type canvas-width canvas-height]}]
  (let [reader (create-transit-reader)
        writer (create-transit-writer)]
    
    ;; Initialize state
    (reset! core/state {:stream-type (keyword stream-type)
                       :canvas {:width (or canvas-width 800)
                               :height (or canvas-height 600)}
                       :zoom-level 0
                       :frame-data {:timestamp 0 :duration 33}})
    
    ;; Send startup log
    (send-log writer :info "Mock video stream started" @core/state)
    
    ;; Send initial window event
    (send-event writer :window {:type :open
                               :stream-id (str "mock-" (name stream-type))
                               :width (:width (:canvas @core/state))
                               :height (:height (:canvas @core/state))})
    
    ;; Main message loop
    (try
      (loop []
        (when-let [msg (transit/read reader)]
          (try
            (handle-message msg core/state writer)
            (catch Exception e
              (log/error! e "Error handling message" {:msg msg})
              (send-log writer :error "Message handling error" 
                       {:error (.getMessage e)
                        :msg-type (:msg-type msg)})))
          (recur)))
      (catch Exception e
        (when-not (instance? java.io.EOFException e)
          (log/error! e "Process error"))
        (send-log writer :info "Mock video stream shutting down" {})))
    
    ;; Send close event
    (send-event writer :window {:type :close
                               :stream-id (str "mock-" (name stream-type))})))

;; ============================================================================
;; Entry Point
;; ============================================================================

(defn -main
  "Main entry point for subprocess mode"
  [& args]
  (let [args-map (apply hash-map args)
        stream-type (get args-map "--stream-type" "heat")]
    (start-mock-process {:stream-type stream-type})))