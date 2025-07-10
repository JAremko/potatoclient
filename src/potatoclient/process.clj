(ns potatoclient.process
  "Process lifecycle management for video stream subprocesses."
  (:require [clojure.java.io :as io]
            [clojure.data.json :as json]
            [clojure.core.async :as async])
  (:import [java.lang ProcessBuilder]
           [java.io BufferedReader BufferedWriter InputStreamReader OutputStreamWriter]))

(defn- find-java
  "Find the Java executable, checking java.home first"
  []
  (let [java-home (System/getProperty "java.home")
        sep (System/getProperty "file.separator")
        java-bin (str java-home sep "bin" sep "java")]
    (if (.exists (io/file java-bin))
      java-bin
      (if (.exists (io/file (str java-bin ".exe")))
        (str java-bin ".exe")
        "java"))))

(defn- create-stdout-reader
  "Create a reader thread for process stdout"
  [stdout-reader output-chan stream-id]
  (future
    (try
      (loop []
        (when-let [line (.readLine stdout-reader)]
          (when (and (not (.isEmpty line))
                     (.startsWith line "{"))
            (try
              (let [msg (json/read-str line :key-fn keyword)]
                (async/>!! output-chan msg))
              (catch Exception e
                ;; Non-JSON stdout - log as INFO
                (async/>!! output-chan
                  {:type "log"
                   :streamId stream-id
                   :level "INFO"
                   :message line
                   :timestamp (System/currentTimeMillis)}))))
          (recur)))
      (catch Exception e
        ;; Stream closed, normal during shutdown
        ))))

(defn- create-stderr-reader
  "Create a reader thread for process stderr with multi-line consolidation"
  [stderr-reader output-chan stream-id]
  (future
    (try
      (loop [buffer nil]
        (if-let [line (.readLine stderr-reader)]
          ;; Check if this line starts a new error or is continuation
          (if (or (re-find #"^\s+at\s+" line)  ; Stack trace line
                  (re-find #"^Caused by:" line) ; Caused by line
                  (re-find #"^\s+\.\.\." line)  ; More lines indicator
                  (and buffer (re-find #"^\s+" line))) ; Indented continuation
            ;; Continuation of previous error
            (recur (if buffer (str buffer "\n" line) line))
            ;; New error line or message
            (do
              ;; Send previous buffer if exists
              (when buffer
                (async/>!! output-chan
                  {:type "log"
                   :streamId stream-id
                   :level "STDERR"
                   :message buffer
                   :timestamp (System/currentTimeMillis)}))
              ;; Start new buffer
              (recur line)))
          ;; EOF - send final buffer
          (when buffer
            (async/>!! output-chan
              {:type "log"
               :streamId stream-id
               :level "STDERR"
               :message buffer
               :timestamp (System/currentTimeMillis)}))))
      (catch Exception e
        ;; Stream closed, normal during shutdown
        ))))

(defn start-stream-process
  "Start a video stream subprocess.
  Returns a map containing process info and communication channels."
  [stream-id url]
  (let [java-exe (find-java)
        classpath (System/getProperty "java.class.path")
        ;; Detect AppImage environment and adjust library paths
        appdir (System/getenv "APPDIR")
        is-appimage (and appdir (not (.isEmpty appdir)))
        lib-path (if is-appimage
                   (str appdir "/usr/lib:" appdir "/usr/lib/x86_64-linux-gnu")
                   "/usr/lib")
        plugin-path (if is-appimage
                      (str appdir "/usr/lib/gstreamer-1.0")
                      "/usr/lib/gstreamer-1.0")
        ;; Build command with dynamic paths
        cmd-args (concat [java-exe 
                         "--enable-native-access=ALL-UNNAMED"
                         "-cp" classpath
                         (str "-Djna.library.path=" lib-path)
                         (str "-Dgstreamer.library.path=" lib-path)
                         (str "-Dgstreamer.plugin.path=" plugin-path)
                         (str "-Djava.library.path=" lib-path)
                         "-Djna.nosys=false"]
                        ;; Pass environment to child process
                        (when is-appimage
                          [(str "-DAPPDIR=" appdir)])
                        ["com.sycha.VideoStreamManager"
                         stream-id url])
        pb (ProcessBuilder. (vec (remove nil? cmd-args)))
        ;; Inherit environment variables including GStreamer settings
        _ (when is-appimage
            (let [env (.environment pb)]
              ;; Ensure child process inherits critical GStreamer environment
              (.put env "GST_PLUGIN_PATH" plugin-path)
              (.put env "GST_PLUGIN_PATH_1_0" plugin-path)
              (.put env "GST_PLUGIN_SYSTEM_PATH_1_0" plugin-path)
              (.put env "GST_PLUGIN_SCANNER_1_0" 
                    (str appdir "/usr/lib/gstreamer1.0/gstreamer-1.0/gst-plugin-scanner"))
              (.put env "LD_LIBRARY_PATH"
                    (str lib-path ":" (System/getenv "LD_LIBRARY_PATH")))
              (.put env "APPDIR" appdir)))
        process (.start pb)
        writer (BufferedWriter. (OutputStreamWriter. (.getOutputStream process)))
        stdout-reader (BufferedReader. (InputStreamReader. (.getInputStream process)))
        stderr-reader (BufferedReader. (InputStreamReader. (.getErrorStream process)))
        output-chan (async/chan 100)]
    
    ;; Start reader threads
    (create-stdout-reader stdout-reader output-chan stream-id)
    (create-stderr-reader stderr-reader output-chan stream-id)
    
    {:process process
     :writer writer
     :stdout-reader stdout-reader
     :stderr-reader stderr-reader
     :output-chan output-chan
     :stream-id stream-id}))

(defn send-command
  "Send a command to a stream process"
  [stream cmd]
  (when stream
    (try
      (.write (:writer stream) (json/write-str cmd))
      (.newLine (:writer stream))
      (.flush (:writer stream))
      (catch Exception e
        (println "Send error:" e)))))

(defn stop-stream
  "Stop a stream process gracefully, then forcefully if needed"
  [stream]
  (when stream
    ;; First try graceful shutdown
    (try
      (send-command stream {:action "shutdown"})
      (Thread/sleep 50)
      (catch Exception e))
    ;; Then force kill the process
    (try
      (when (.isAlive (:process stream))
        (.destroyForcibly (:process stream)))
      (catch Exception e))
    ;; Close all streams
    (try (.close (:writer stream)) (catch Exception e))
    (try (.close (:stdout-reader stream)) (catch Exception e))
    (try (.close (:stderr-reader stream)) (catch Exception e))
    ;; Close the channel
    (try (async/close! (:output-chan stream)) (catch Exception e))))

(defn cleanup-all-processes
  "Clean up all processes - useful for shutdown hooks"
  [state-atom]
  (try
    (doseq [[k stream] @state-atom]
      (when stream
        (try
          (when (.isAlive (:process stream))
            (.destroyForcibly (:process stream)))
          (catch Exception e))))
    (catch Exception e)))