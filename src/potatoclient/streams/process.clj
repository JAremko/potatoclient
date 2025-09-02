(ns potatoclient.streams.process
  "Process management for video streams."
  (:require
    [clojure.java.io :as io]
    [clojure.string :as str]
    [potatoclient.logging :as logging]
    [potatoclient.streams.config :as config]
    [potatoclient.streams.state :as state])
  (:import
    (java.io BufferedReader InputStreamReader)
    (java.lang Process ProcessBuilder)
    (java.util.concurrent TimeUnit)))

;; ============================================================================
;; Process Building
;; ============================================================================

(defn- build-command
  "Build command to spawn VideoStreamManager"
  {:malli/schema [:=> [:cat :keyword :string :int] [:sequential :string]]}
  [stream-type host parent-pid]
  (let [java-cmd (config/get-java-command)
        classpath (config/get-classpath)
        main-class (config/get-main-class)
        stream-url (config/build-stream-url stream-type)]
    (cond-> [java-cmd
             "-cp" classpath
             "-Djava.awt.headless=false"
             "-Dgstreamer.plugin.path=/usr/lib/x86_64-linux-gnu/gstreamer-1.0"
             "--enable-native-access=ALL-UNNAMED"]
      (config/get-debug-flag) (concat ["-Dpotatoclient.debug=true"])
      true (concat [main-class
                    (name stream-type)
                    stream-url
                    host
                    (str parent-pid)]))))

;; ============================================================================
;; Output Handling
;; ============================================================================

(defn- create-output-handler
  "Create thread to handle process output"
  {:malli/schema [:=> [:cat :keyword :any :keyword] :fn]}
  [stream-type stream output-type]
  (fn []
    (try
      (let [reader (BufferedReader. (InputStreamReader. stream))]
        (loop []
          (when-let [line (.readLine reader)]
            (case output-type
              :stdout (logging/log-debug {:id :stream/stdout
                                          :stream stream-type
                                          :output line})
              :stderr (logging/log-warn {:id :stream/stderr
                                         :stream stream-type
                                         :error line}))
            (recur))))
      (catch Exception e
        (when-not (instance? java.io.IOException e)
          (logging/log-error {:id :stream/output-error
                              :stream stream-type
                              :error (.getMessage e)}))))))

(defn- start-output-handlers
  "Start threads to handle process output"
  {:malli/schema [:=> [:cat :keyword :any] :nil]}
  [stream-type process]
  (let [stdout-handler (create-output-handler stream-type
                                              (.getInputStream process)
                                              :stdout)
        stderr-handler (create-output-handler stream-type
                                              (.getErrorStream process)
                                              :stderr)]
    (.start (Thread. stdout-handler (str (name stream-type) "-stdout")))
    (.start (Thread. stderr-handler (str (name stream-type) "-stderr"))))
  nil)

;; ============================================================================
;; Process Lifecycle
;; ============================================================================

(defn spawn-process
  "Spawn a VideoStreamManager process"
  {:malli/schema [:=> [:cat :keyword :string :int] [:maybe :any]]}
  [stream-type host parent-pid]
  (try
    (logging/log-info {:id :stream/spawning-process
                       :stream stream-type
                       :host host})

    (let [command (build-command stream-type host parent-pid)
          project-root (io/file (System/getProperty "user.dir"))
          _ (logging/log-debug {:id :stream/command
                                :stream stream-type
                                :command (str/join " " command)
                                :working-dir (.getAbsolutePath project-root)})
          pb (doto (ProcessBuilder. ^java.util.List command)
               (.directory project-root))
          process (.start pb)
          pid (.pid (.toHandle process))]

      ;; Start output handlers
      (start-output-handlers stream-type process)

      ;; Store process info in app state
      (state/set-stream-status! stream-type :starting pid)
      (state/set-stream-process! stream-type process)

      (logging/log-info {:id :stream/process-spawned
                         :stream stream-type
                         :pid pid})

      process)

    (catch Exception e
      (logging/log-error {:id :stream/spawn-failed
                          :stream stream-type
                          :error (.getMessage e)})
      (state/set-stream-error! stream-type (.getMessage e))
      nil)))

(defn process-alive?
  "Check if process is alive"
  {:malli/schema [:=> [:cat [:maybe :any]] :boolean]}
  [process]
  (and process
       (instance? Process process)
       (.isAlive ^Process process)))

(defn stop-process
  "Stop a process gracefully"
  {:malli/schema [:=> [:cat :keyword] :nil]}
  [stream-type]
  (when-let [process (state/get-stream-process stream-type)]
    (when (process-alive? process)
      (let [pid (get-in (state/get-stream-state stream-type) [:pid])]
        (logging/log-info {:id :stream/stopping-process
                           :stream stream-type
                           :pid pid})

        ;; Update status
        (state/set-stream-status! stream-type :stopping pid)

        ;; Try graceful shutdown
        (.destroy ^Process process)

        ;; Wait up to 5 seconds
        (when-not (.waitFor ^Process process 5 TimeUnit/SECONDS)
          (logging/log-warn {:id :stream/force-killing
                             :stream stream-type
                             :pid pid})
          (.destroyForcibly ^Process process)
          (.waitFor ^Process process 2 TimeUnit/SECONDS))

        (logging/log-info {:id :stream/process-stopped
                           :stream stream-type
                           :pid pid})

        ;; Update status
        (state/set-stream-status! stream-type :stopped nil))))
  nil)

(defn get-current-pid
  "Get current process PID"
  {:malli/schema [:=> [:cat] :int]}
  []
  (.pid (java.lang.ProcessHandle/current)))