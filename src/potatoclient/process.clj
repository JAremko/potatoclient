(ns potatoclient.process
  "Process lifecycle management for video stream subprocesses.
  
  Handles spawning, communication, and cleanup of Java subprocesses
  that manage the actual video streams via WebSocket and GStreamer."
  (:require [clojure.java.io :as io]
            [clojure.data.json :as json]
            [clojure.core.async :as async :refer [>!! <!! go-loop]]
            [clojure.string :as str]
            [orchestra.core :refer [defn-spec]]
            [orchestra.spec.test :as st])
  (:import [java.lang ProcessBuilder Process]
           [java.io BufferedReader BufferedWriter InputStreamReader OutputStreamWriter]
           [java.util.concurrent TimeUnit]))

;; Configuration constants
(def ^:private read-timeout-ms 100)
(def ^:private shutdown-grace-period-ms 50)
(def ^:private channel-buffer-size 100)

;; Process lifecycle states
(def ^:private process-states #{:starting :running :stopping :stopped :failed})

(defn- get-java-executable
  "Find the Java executable, checking java.home first."
  []
  (let [java-home (System/getProperty "java.home")
        sep (System/getProperty "file.separator")
        candidates [(str java-home sep "bin" sep "java")
                    (str java-home sep "bin" sep "java.exe")
                    "java"]]
    (or (first (filter #(.exists (io/file %)) candidates))
        "java")))

(defn- get-appimage-environment
  "Extract AppImage-specific environment variables if running in AppImage."
  []
  (when-let [appdir (System/getenv "APPDIR")]
    (when-not (str/blank? appdir)
      {:appdir appdir
       :lib-path (str appdir "/usr/lib:" appdir "/usr/lib/x86_64-linux-gnu")
       :plugin-path (str appdir "/usr/lib/gstreamer-1.0")
       :scanner-path (str appdir "/usr/lib/gstreamer1.0/gstreamer-1.0/gst-plugin-scanner")})))

(defn- build-process-environment
  "Build environment variables for the subprocess."
  [app-env]
  (if app-env
    {"APPDIR" (:appdir app-env)
     "GST_PLUGIN_PATH" (:plugin-path app-env)
     "GST_PLUGIN_PATH_1_0" (:plugin-path app-env)
     "GST_PLUGIN_SYSTEM_PATH_1_0" (:plugin-path app-env)
     "GST_PLUGIN_SCANNER_1_0" (:scanner-path app-env)
     "LD_LIBRARY_PATH" (str (:lib-path app-env) ":" (System/getenv "LD_LIBRARY_PATH"))}
    {}))

(defn- build-jvm-args
  "Build JVM arguments for the subprocess."
  [app-env]
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

(defn- create-process-builder
  "Create a configured ProcessBuilder for the video stream subprocess."
  [stream-id url]
  (let [java-exe (get-java-executable)
        classpath (System/getProperty "java.class.path")
        app-env (get-appimage-environment)
        jvm-args (build-jvm-args app-env)
        main-class "com.sycha.VideoStreamManager"
        cmd (vec (concat [java-exe "-cp" classpath]
                        jvm-args
                        [main-class stream-id url]))]
    (doto (ProcessBuilder. cmd)
      (-> .environment (.putAll ^java.util.Map (build-process-environment app-env))))))

(defn- parse-json-message
  "Safely parse a JSON message from the subprocess."
  [line stream-id]
  (try
    (json/read-str line :key-fn keyword)
    (catch Exception e
      ;; Non-JSON stdout - wrap as log message
      {:type "log"
       :streamId stream-id
       :level "INFO"
       :message line
       :timestamp (System/currentTimeMillis)})))

(defn- create-stdout-reader
  "Create a reader thread for process stdout."
  [^BufferedReader stdout-reader output-chan stream-id]
  (go-loop []
    (when-let [line (try
                      (.readLine stdout-reader)
                      (catch Exception _ nil))]
      (when-not (str/blank? line)
        (when (.startsWith line "{")
          (>!! output-chan (parse-json-message line stream-id))))
      (recur))))

(defn- consolidate-stderr-lines
  "Consolidate multi-line stderr output (stack traces, etc)."
  [lines]
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

(defn- create-stderr-reader
  "Create a reader thread for process stderr with multi-line consolidation."
  [^BufferedReader stderr-reader output-chan stream-id]
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

(defn- create-stream-process
  "Create the process and I/O resources."
  [stream-id url]
  (let [^ProcessBuilder pb (create-process-builder stream-id url)
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

(defn-spec start-stream-process map?
  "Start a video stream subprocess.
  Returns a map containing process info and communication channels."
  [stream-id string?
   url string?]
  (let [stream (create-stream-process stream-id url)]
    ;; Start reader threads
    (create-stdout-reader (:stdout-reader stream) (:output-chan stream) stream-id)
    (create-stderr-reader (:stderr-reader stream) (:output-chan stream) stream-id)
    (reset! (:state stream) :running)
    stream))

(defn-spec send-command boolean?
  "Send a command to a stream process."
  [stream map?
   cmd map?]
  (if (and stream (= @(:state stream) :running))
    (try
      (let [^BufferedWriter writer (:writer stream)]
        (.write writer (json/write-str cmd))
        (.newLine writer)
        (.flush writer))
      true
      (catch Exception e
        false))
    false))

(defn- close-resource
  "Safely close a resource, ignoring exceptions."
  [resource close-fn]
  (try
    (when resource
      (close-fn resource))
    (catch Exception _)))

(defn-spec stop-stream any?
  "Stop a stream process gracefully, then forcefully if needed."
  [stream map?]
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
    
    (reset! (:state stream) :stopped)))

(defn-spec cleanup-all-processes any?
  "Clean up all processes - useful for shutdown hooks."
  [state-atom any?]
  (doseq [[stream-key stream-data] @state-atom]
    (when (and (map? stream-data)
               (:process stream-data))
      (let [^Process process (:process stream-data)]
        (when (.isAlive process)
          (try
            (.destroyForcibly process)
            (catch Exception _)))))))

(defn-spec process-alive? boolean?
  "Check if a stream process is still alive."
  [stream map?]
  (and stream
       (:process stream)
       (let [^Process process (:process stream)]
         (.isAlive process))
       (= @(:state stream) :running)))