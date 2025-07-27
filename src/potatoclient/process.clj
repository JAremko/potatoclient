(ns potatoclient.process
  "Process lifecycle management for video stream subprocesses.
  
  Handles spawning, communication, and cleanup of Java subprocesses
  that manage the actual video streams via WebSocket and GStreamer."
  (:require [clojure.core.async :as async :refer [>!! go-loop]]
            [clojure.data.json :as json]
            [clojure.java.io :as io]
            [clojure.string :as str]
            [com.fulcrologic.guardrails.malli.core :refer [=> >defn >defn- ?]]
            [potatoclient.logging :as logging]
            [potatoclient.state :as state])
  (:import (clojure.core.async.impl.channels ManyToManyChannel)
           (java.io BufferedReader BufferedWriter InputStreamReader OutputStreamWriter)
           (java.lang Process ProcessBuilder)
           (java.util List Map)))

;; Configuration constants
(def ^:private shutdown-grace-period-ms 50)
(def ^:private channel-buffer-size 100)

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
      ;; Non-JSON stdout - wrap as log message
      {:type "log"
       :streamId stream-id
       :level "INFO"
       :message line
       :timestamp (System/currentTimeMillis)})))

(>defn- create-stdout-reader
  "Create a reader thread for process stdout."
  [^BufferedReader stdout-reader output-chan stream-id]
  [[:fn #(instance? BufferedReader %)]
   [:fn {:error/message "must be a core.async channel"}
    #(instance? ManyToManyChannel %)]
   string? => [:fn {:error/message "must be a core.async channel"}
               #(instance? ManyToManyChannel %)]]
  (go-loop []
    (when-let [line (try
                      (.readLine stdout-reader)
                      (catch Exception _ nil))]
      (when-not (str/blank? line)
        (when (.startsWith line "{")
          (>!! output-chan (parse-json-message line stream-id))))
      (recur))))

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
  [^BufferedReader stderr-reader output-chan stream-id]
  [[:fn #(instance? BufferedReader %)]
   [:fn {:error/message "must be a core.async channel"}
    #(instance? ManyToManyChannel %)]
   string? => [:fn {:error/message "must be a core.async channel"}
               #(instance? ManyToManyChannel %)]]
  (go-loop [buffer []]
    (if-let [line (try
                    (.readLine stderr-reader)
                    (catch Exception _ nil))]
      (recur (conj buffer line))
      ;; EOF - process accumulated lines
      (doseq [msg (consolidate-stderr-lines buffer)]
        (>!! output-chan
             {:type "log"
              :streamId stream-id
              :level "STDERR"
              :message msg
              :timestamp (System/currentTimeMillis)})))))

(>defn- create-stream-process
  "Create the process and I/O resources."
  [stream-id url domain]
  [string? string? :potatoclient.specs/domain => :potatoclient.specs/stream-process-map]
  (let [^ProcessBuilder pb (create-process-builder stream-id url domain)
        ^Process process (.start pb)
        writer (BufferedWriter. (OutputStreamWriter. (.getOutputStream process)))
        stdout-reader (BufferedReader. (InputStreamReader. (.getInputStream process)))
        stderr-reader (BufferedReader. (InputStreamReader. (.getErrorStream process)))
        output-chan (async/chan channel-buffer-size)]
    {:process process
     :writer writer
     :stdout-reader stdout-reader
     :stderr-reader stderr-reader
     :output-chan output-chan
     :stream-id stream-id
     :state (atom :starting)}))

(>defn start-stream-process
  "Start a video stream subprocess.
  Returns a map containing process info and communication channels."
  [stream-id url domain]
  [string? string? :potatoclient.specs/domain => :potatoclient.specs/stream-process-map]
  (let [stream (create-stream-process stream-id url domain)]
    ;; Start reader threads
    (create-stdout-reader (:stdout-reader stream) (:output-chan stream) stream-id)
    (create-stderr-reader (:stderr-reader stream) (:output-chan stream) stream-id)
    (reset! (:state stream) :running)
    stream))

(>defn send-command
  "Send a command to a stream process."
  [stream cmd]
  [:potatoclient.specs/stream-process-map :potatoclient.specs/process-command => boolean?]
  (if (and stream (= @(:state stream) :running))
    (try
      (let [^BufferedWriter writer (:writer stream)]
        (.write writer (json/write-str cmd))
        (.newLine writer)
        (.flush writer))
      true
      (catch Exception _
        false))
    false))

(>defn- close-resource
  "Safely close a resource, ignoring exceptions."
  [resource close-fn]
  [any? fn? => nil?]
  (try
    (when resource
      (close-fn resource))
    (catch Exception _)))

(>defn stop-stream
  "Stop a stream process gracefully, then forcefully if needed."
  [stream]
  [:potatoclient.specs/stream-process-map => :potatoclient.specs/process-state]
  (when stream
    (reset! (:state stream) :stopping)

    ;; Try graceful shutdown first
    (send-command stream {:action "shutdown"})
    (Thread/sleep shutdown-grace-period-ms)

    ;; Force kill if still alive
    (let [^Process process (:process stream)]
      (when (.isAlive process)
        (.destroyForcibly process)))

    ;; Close all resources
    (close-resource (:writer stream) #(.close %))
    (close-resource (:stdout-reader stream) #(.close %))
    (close-resource (:stderr-reader stream) #(.close %))
    (close-resource (:output-chan stream) async/close!)

    (reset! (:state stream) :stopped)
    :stopped))

(>defn cleanup-all-processes
  "Clean up all processes - useful for shutdown hooks.
  Can be called with no args (uses state directly) or with a streams-map."
  ([]
   [=> nil?]
   ;; No-arg version that uses state directly
   (let [all-streams (state/all-streams)]
     (cleanup-all-processes all-streams)))
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