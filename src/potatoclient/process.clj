(ns potatoclient.process
  "Process lifecycle management for video stream subprocesses.

  Handles spawning, communication, and cleanup of Java subprocesses
  that manage the actual video streams via WebSocket and GStreamer."
  (:require [clojure.data.json :as json]
            [clojure.java.io :as io]
            [clojure.string :as str]
            [com.fulcrologic.guardrails.malli.core :refer [=> >defn >defn- ?]]
            [potatoclient.logging :as logging]
            [potatoclient.transit.core :as transit-core]
            [potatoclient.transit.framed-io :as framed-io])
  (:import (java.io BufferedReader InputStreamReader InputStream OutputStream)
           (java.lang Process ProcessBuilder)
           (java.util List Map)
           (potatoclient.transit MessageType MessageKeys)))

;; Configuration constants
(def ^:private shutdown-grace-period-ms 50)

(>defn- get-java-executable
  "Find the Java executable, checking java.home first."
  []
  [=> string?]
  (let [java-home (System/getProperty "java.home")
        sep (System/getProperty "file.separator")
        candidates [(str java-home sep "bin" sep "java")
                    (str java-home sep "bin" sep "java.exe")
                    "java"]]
    (or (first (filter #(.exists (io/file %)) candidates))
        "java")))

(>defn- get-appimage-environment
  "Extract AppImage-specific environment variables if running in AppImage."
  []
  [=> (? [:map
          [:appdir string?]
          [:lib-path string?]
          [:plugin-path string?]
          [:scanner-path string?]])]
  (when-let [appdir (System/getenv "APPDIR")]
    (when-not (str/blank? appdir)
      {:appdir appdir
       :lib-path (str appdir "/usr/lib:" appdir "/usr/lib/x86_64-linux-gnu")
       :plugin-path (str appdir "/usr/lib/gstreamer-1.0")
       :scanner-path (str appdir "/usr/lib/gstreamer1.0/gstreamer-1.0/gst-plugin-scanner")})))

(>defn- build-process-environment
  "Build environment variables for the subprocess."
  [app-env]
  [(? [:map
       [:appdir string?]
       [:lib-path string?]
       [:plugin-path string?]
       [:scanner-path string?]]) => [:map-of string? string?]]
  (if app-env
    {"APPDIR" (:appdir app-env)
     "GST_PLUGIN_PATH" (:plugin-path app-env)
     "GST_PLUGIN_PATH_1_0" (:plugin-path app-env)
     "GST_PLUGIN_SYSTEM_PATH_1_0" (:plugin-path app-env)
     "GST_PLUGIN_SCANNER_1_0" (:scanner-path app-env)
     "LD_LIBRARY_PATH" (str (:lib-path app-env) ":" (System/getenv "LD_LIBRARY_PATH"))}
    {}))

(>defn- build-jvm-args
  "Build JVM arguments for the subprocess."
  [app-env]
  [(? [:map
       [:appdir string?]
       [:lib-path string?]
       [:plugin-path string?]
       [:scanner-path string?]]) => [:sequential string?]]
  (let [base-args ["--enable-native-access=ALL-UNNAMED"
                   "-Djna.nosys=false"]
        lib-path (or (:lib-path app-env) "/usr/lib")
        plugin-path (or (:plugin-path app-env) "/usr/lib/gstreamer-1.0")]
    (concat base-args
            [(str "-Djna.library.path=" lib-path)
             (str "-Dgstreamer.library.path=" lib-path)
             (str "-Dgstreamer.plugin.path=" plugin-path)
             (str "-Djava.library.path=" lib-path)]
            (when app-env
              [(str "-DAPPDIR=" (:appdir app-env))]))))

(>defn- create-process-builder
  "Create a configured ProcessBuilder for the video stream subprocess."
  [stream-id url domain]
  [string? string? :potatoclient.specs/domain => [:fn #(instance? ProcessBuilder %)]]
  (let [java-exe (get-java-executable)
        classpath (System/getProperty "java.class.path")
        app-env (get-appimage-environment)
        jvm-args (build-jvm-args app-env)
        main-class "potatoclient.kotlin.VideoStreamManager"
        cmd (vec (concat [java-exe "-cp" classpath]
                         jvm-args
                         [main-class stream-id url domain]))]
    (doto (ProcessBuilder. ^List cmd)
      (-> .environment (.putAll ^Map (build-process-environment app-env))))))

(>defn- parse-json-message
  "Safely parse a JSON message from the subprocess."
  [line stream-id]
  [string? string? => map?]
  (try
    (json/read-str line :key-fn keyword)
    (catch Exception _
      ;; Non-JSON stdout - wrap as Transit log message
      {MessageKeys/MSG_TYPE (.getKey MessageType/LOG)
       MessageKeys/MSG_ID (str (java.util.UUID/randomUUID))
       MessageKeys/TIMESTAMP (System/currentTimeMillis)
       MessageKeys/PAYLOAD {MessageKeys/STREAM_ID stream-id
                            MessageKeys/LEVEL "INFO"
                            MessageKeys/MESSAGE line}})))

(>defn- create-stdout-reader
  "Create a reader thread for process stdout."
  [^BufferedReader stdout-reader message-handler stream-id]
  [[:fn #(instance? BufferedReader %)]
   fn?
   string? => [:fn #(instance? Thread %)]]
  (Thread.
    (fn []
      (try
        (loop []
          (when-let [line (.readLine stdout-reader)]
            (when-not (str/blank? line)
              (when (.startsWith line "{")
                (message-handler (parse-json-message line stream-id))))
            (recur)))
        (catch Exception e
          (logging/log-error
            {:id ::stdout-reader-error
             :data {:stream stream-id
                    :error (.getMessage e)}
             :msg "Error in stdout reader thread"}))))))

(>defn- consolidate-stderr-lines
  "Consolidate multi-line stderr output (stack traces, etc)."
  [lines]
  [[:sequential string?] => [:sequential string?]]
  (let [stack-trace-pattern #"^\s+at\s+"
        caused-by-pattern #"^Caused by:"
        continuation-pattern #"^\s+\.\.\."
        indented-pattern #"^\s+"]
    (loop [remaining lines
           current-msg nil
           messages []]
      (if-let [line (first remaining)]
        (cond
          ;; Continuation patterns
          (or (re-find stack-trace-pattern line)
              (re-find caused-by-pattern line)
              (re-find continuation-pattern line)
              (and current-msg (re-find indented-pattern line)))
          (recur (rest remaining)
                 (if current-msg
                   (str current-msg "\n" line)
                   line)
                 messages)

          ;; New message
          :else
          (recur (rest remaining)
                 line
                 (if current-msg
                   (conj messages current-msg)
                   messages)))

        ;; Return all messages including final one
        (if current-msg
          (conj messages current-msg)
          messages)))))

(>defn- create-stderr-reader
  "Create a reader thread for process stderr with multi-line consolidation."
  [^BufferedReader stderr-reader message-handler stream-id]
  [[:fn #(instance? BufferedReader %)]
   fn?
   string? => [:fn #(instance? Thread %)]]
  (Thread.
    (fn []
      (try
        (loop [buffer []]
          (if-let [line (.readLine stderr-reader)]
            (recur (conj buffer line))
            ;; EOF - process accumulated lines
            (doseq [msg (consolidate-stderr-lines buffer)]
              (message-handler
                {MessageKeys/MSG_TYPE (.getKey MessageType/LOG)
                 MessageKeys/MSG_ID (str (java.util.UUID/randomUUID))
                 MessageKeys/TIMESTAMP (System/currentTimeMillis)
                 MessageKeys/PAYLOAD {MessageKeys/STREAM_ID stream-id
                                      MessageKeys/PROCESS stream-id
                                      MessageKeys/LEVEL "STDERR"
                                      MessageKeys/MESSAGE msg}}))))
        (catch Exception e
          (logging/log-error
            {:id ::stderr-reader-error
             :data {:stream stream-id
                    :error (.getMessage e)}
             :msg "Error in stderr reader thread"}))))))

(>defn- create-transit-reader
  "Create a Transit reader thread that processes messages from subprocess."
  [^InputStream input-stream message-handler stream-id]
  [[:fn #(instance? InputStream %)]
   fn?
   string?
   => [:fn #(instance? Thread %)]]
  (Thread.
    (fn []
      (try
        (let [framed-input (framed-io/make-framed-input-stream input-stream)
              reader (transit-core/make-reader framed-input)
              read-fn (fn [] (transit-core/read-message reader))]
          (loop []
            (when-let [msg (try
                             (read-fn)
                             (catch Exception e
                               (logging/log-error
                                 {:id ::transit-read-error
                                  :data {:stream stream-id
                                         :error (.getMessage e)}
                                  :msg (str "Error reading Transit message from " stream-id)})
                               nil))]
              ;; Only forward map messages to the handler
              (when (map? msg)
                ;; Add debug logging for video stream messages
                (when (and (not (:msg-type msg)) (not (:type msg)))
                  (logging/log-debug
                    {:id ::video-stream-raw-message
                     :data {:stream stream-id
                            :msg-keys (keys msg)
                            :msg msg}
                     :msg "Video stream sent non-standard message"}))
                ;; Log response messages for debugging window-closed issue
                (when (= (:msg-type msg) :response)
                  (logging/log-info
                    {:id ::transit-response-received
                     :data {:stream stream-id
                            :action (get-in msg [:payload :action])
                            :full-msg msg}
                     :msg (str "TRANSIT RESPONSE RECEIVED from " stream-id " - action: " (get-in msg [:payload :action]))}))
                ;; Call the message handler directly
                (message-handler msg))
              (recur))))
        (catch Exception e
          (logging/log-error
            {:id ::transit-reader-error
             :data {:stream stream-id
                    :error (.getMessage e)}
             :msg "Error in transit reader thread"}))))))

(>defn- create-stream-process
  "Create the process and I/O resources."
  [stream-id url domain message-handler]
  [string? string? :potatoclient.specs/domain fn? => :potatoclient.specs/stream-process-map]
  (let [^ProcessBuilder pb (create-process-builder stream-id url domain)
        ^Process process (.start pb)
        input-stream (.getInputStream process)
        output-stream (.getOutputStream process)
        stderr-reader (BufferedReader. (InputStreamReader. (.getErrorStream process)))
        framed-output (framed-io/make-framed-output-stream output-stream)
        writer (transit-core/make-writer framed-output)
        write-fn (fn [msg]
                   (transit-core/write-message! writer msg framed-output))]
    {:process process
     :writer write-fn
     :input-stream input-stream
     :output-stream output-stream
     :stderr-reader stderr-reader
     :message-handler message-handler
     :stream-id stream-id
     :state (atom :starting)}))

(>defn start-stream-process
  "Start a video stream subprocess.
  Returns a map containing process info and communication channels."
  [stream-id url domain message-handler]
  [string? string? :potatoclient.specs/domain fn? => :potatoclient.specs/stream-process-map]
  (let [stream (create-stream-process stream-id url domain message-handler)
        ;; Start reader threads
        transit-reader (create-transit-reader (:input-stream stream) message-handler stream-id)
        stderr-reader (create-stderr-reader (:stderr-reader stream) message-handler stream-id)]
    (.start transit-reader)
    (.start stderr-reader)
    (reset! (:state stream) :running)
    (logging/log-debug {:msg (str "Stream state set to :running for " stream-id)})
    stream))

(>defn send-command
  "Send a command to a stream process."
  [stream cmd]
  [:potatoclient.specs/stream-process-map :potatoclient.specs/process-command => boolean?]
  (if stream
    (let [current-state @(:state stream)]
      (logging/log-debug {:msg (str "send-command: stream-id=" (:stream-id stream)
                                    ", state=" current-state
                                    ", cmd=" cmd)})
      (if (= current-state :running)
        (try
          (let [write-fn (:writer stream)
                message {MessageKeys/MSG_TYPE (.getKey MessageType/COMMAND)
                         MessageKeys/MSG_ID (str (java.util.UUID/randomUUID))
                         MessageKeys/TIMESTAMP (System/currentTimeMillis)
                         MessageKeys/PAYLOAD cmd}]
            (write-fn message)
            true)
          (catch Exception e
            (logging/log-error {:msg "Failed to send command"
                                :error (.getMessage e)
                                :stream-id (:stream-id stream)})
            false))
        (do
          (logging/log-warn {:msg (str "Cannot send command to stream in state " current-state)
                             :stream-id (:stream-id stream)})
          false)))
    (do
      (logging/log-error {:msg "send-command called with nil stream"})
      false)))

(>defn- close-resource
  "Safely close a resource, ignoring exceptions."
  [resource close-fn]
  [any? fn? => nil?]
  (try
    (when resource
      (close-fn resource))
    (catch Exception _))
  nil)

(>defn stop-stream
  "Stop a stream process gracefully, then forcefully if needed."
  [stream]
  [:potatoclient.specs/stream-process-map => :potatoclient.specs/process-state]
  (when stream
    (logging/log-debug {:msg (str "stop-stream called for " (:stream-id stream)
                                  ", current state: " @(:state stream))})

    ;; Try graceful shutdown first BEFORE changing state
    (send-command stream {:action "shutdown"})
    (Thread/sleep ^long shutdown-grace-period-ms)

    ;; Now change state to stopping
    (reset! (:state stream) :stopping)

    ;; Force kill if still alive
    (let [^Process process (:process stream)]
      (when (.isAlive process)
        (.destroyForcibly process)))

    ;; Close all resources
    (close-resource (:input-stream stream) #(.close ^InputStream %))
    (close-resource (:output-stream stream) #(.close ^OutputStream %))
    (close-resource (:stderr-reader stream) #(.close ^BufferedReader %))

    (reset! (:state stream) :stopped)
    :stopped))

(>defn cleanup-all-processes
  "Clean up all processes - useful for shutdown hooks.
  Can be called with no args (no-op) or with a streams-map."
  ([]
   [=> nil?]
   ;; No-arg version - no-op since we don't have process references
   (logging/log-info {:msg "Video stream cleanup called - streams managed by IPC"})
   nil)
  ([streams-map]
   [[:map-of :potatoclient.specs/stream-key (? :potatoclient.specs/stream-process-map)] => nil?]
   ;; Original version that accepts a map
   (doseq [[_ stream-data] streams-map]
     (when (and (map? stream-data)
                (:process stream-data))
       (when-let [stream-id (:stream-id stream-data)]
         (logging/log-info
           {:id ::stream-shutdown
            :data {:stream-id stream-id}
            :msg "Stream stopped by main app shutdown"}))
       (stop-stream stream-data)))
   nil))