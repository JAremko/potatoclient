(ns clj-stream-spawner.process
  "Process management for spawning VideoStreamManager instances."
  (:require
    [clojure.java.io :as io]
    [clojure.string :as str]
    [taoensso.telemere :as t])
  (:import
    (java.io BufferedReader File IOException InputStreamReader)
    (java.lang Process ProcessBuilder ProcessHandle)
    (java.util List)
    (java.util.concurrent TimeUnit)))

;; ============================================================================
;; Specs
;; ============================================================================

(def StreamType
  [:enum :heat :day])

(def ProcessInfo
  [:map
   [:stream-type StreamType]
   [:process [:fn #(instance? Process %)]]
   [:pid :int]
   [:stdout-thread [:fn #(instance? Thread %)]]
   [:stderr-thread [:fn #(instance? Thread %)]]])

;; ============================================================================
;; Configuration
;; ============================================================================

(def ^:private stream-endpoints
  "WebSocket endpoints for each stream type."
  {:heat "/ws/ws_rec_video_heat"
   :day "/ws/ws_rec_video_day"})

(defn- build-stream-url
  "Build the WebSocket URL for a stream." {:malli/schema [:=> [:cat StreamType :string] :string]}
  [stream-type host]
  (str "wss://" host (get stream-endpoints stream-type)))

(defn- get-classpath
  "Get the classpath for running VideoStreamManager.
   Gets the full classpath from the main project." {:malli/schema [:=> [:cat] :string]}
  []
  (let [project-root (-> (io/file ".")
                        (.getCanonicalPath)
                        (str/replace #"/tools/clj-stream-spawner$" ""))]
    ;; Get the full classpath from the main project
    (let [result (-> (ProcessBuilder. ["clojure" "-Spath"])
                     (.directory (io/file project-root))
                     (.start))]
      (.waitFor result)
      (if (zero? (.exitValue result))
        (let [classpath (slurp (.getInputStream result))]
          ;; Add target directories to classpath
          (str (str/trim classpath)
               ":" (str project-root "/target/classes")
               ":" (str project-root "/target/java-classes")))
        ;; Fallback to building classpath manually
        (str/join ":"
                  [(str project-root "/target/classes")
                   (str project-root "/target/java-classes")
                   (str project-root "/lib/*")])))))

(defn- get-project-root
  "Get the project root directory." {:malli/schema [:=> [:cat] [:fn (fn* [p1__3756#] (instance? File p1__3756#))]]}
  []
  (-> (io/file ".")
      (.getCanonicalPath)
      (str/replace #"/tools/clj-stream-spawner$" "")
      io/file))

;; ============================================================================
;; Stream Output Handling
;; ============================================================================

(defn- create-output-gobbler
  "Create a thread that consumes process output." {:malli/schema [:=> [:cat StreamType :string [:fn (fn* [p1__3758#] (instance? BufferedReader p1__3758#))]] [:fn (fn* [p1__3760#] (instance? Thread p1__3760#))]]}
  [stream-type stream-name ^BufferedReader reader]
  (Thread.
   (fn []
     (try
       (loop []
         (when-let [line (.readLine reader)]
           (println (str "[" (name stream-type) "-" stream-name "] " line))
           (recur)))
       (catch Exception e
         (when-not (instance? IOException e)
           (t/log! :error (str "Error reading " stream-name " for " (name stream-type) ": " (.getMessage e)))))))
   (str (name stream-type) "-" stream-name "-gobbler")))

;; ============================================================================
;; Process Spawning
;; ============================================================================

(defn spawn-stream-process
  "Spawn a VideoStreamManager process for a stream.
   Returns a ProcessInfo map with the process and monitoring threads." {:malli/schema [:=> [:cat StreamType :string [:* :any]] ProcessInfo]}
  [stream-type host & {:keys [parent-pid debug?]
                        :or {debug? false}}]
  (let [stream-url (build-stream-url stream-type host)
        classpath (get-classpath)
        project-root (get-project-root)

        ;; Build command
        command ["java"
                 "-cp" classpath
                 "-Djava.awt.headless=false"
                 "-Dgstreamer.plugin.path=/usr/lib/x86_64-linux-gnu/gstreamer-1.0"
                 "--enable-native-access=ALL-UNNAMED"
                 "potatoclient.kotlin.VideoStreamManager"
                 (name stream-type)
                 stream-url
                 host
                 (str (or parent-pid (.pid (ProcessHandle/current))))]

        _ (when debug?
            (t/log! :debug (str "Spawning " (name stream-type) " stream"))
            (t/log! :debug (str "URL: " stream-url))
            (t/log! :debug (str "Command: " (str/join " " command))))

        ;; Create process
        process-builder (doto (ProcessBuilder. ^List command)
                          (.directory project-root)
                          (.redirectErrorStream false))

        process (.start process-builder)
        pid (.pid process)

        ;; Create output gobblers
        stdout-reader (BufferedReader. (InputStreamReader. (.getInputStream process)))
        stderr-reader (BufferedReader. (InputStreamReader. (.getErrorStream process)))

        ^Thread stdout-thread (create-output-gobbler stream-type "stdout" stdout-reader)
        ^Thread stderr-thread (create-output-gobbler stream-type "stderr" stderr-reader)

        ;; Start gobbler threads
        _ (doto stdout-thread (.setDaemon true) (.start))
        _ (doto stderr-thread (.setDaemon true) (.start))]

    (t/log! :info (str "Started " (name stream-type) " stream process - PID: " pid))

    {:stream-type stream-type
     :process process
     :pid pid
     :stdout-thread stdout-thread
     :stderr-thread stderr-thread}))

;; ============================================================================
;; Process Control
;; ============================================================================

(defn process-alive?
  "Check if a process is still alive." {:malli/schema [:=> [:cat ProcessInfo] :boolean]}
  [process-info]
  (.isAlive (:process process-info)))

(defn stop-process
  "Stop a process gracefully, with force kill fallback." {:malli/schema [:=> [:cat ProcessInfo [:* :any]] :nil]}
  [process-info & {:keys [timeout-seconds] :or {timeout-seconds 5}}]
  (let [{:keys [stream-type process pid]} process-info]
    (when (process-alive? process-info)
      (t/log! :info (str "Stopping " (name stream-type) " stream process - PID: " pid))

      ;; Try graceful shutdown
      (.destroy ^Process process)

      ;; Wait for shutdown
      (when-not (.waitFor ^Process process timeout-seconds TimeUnit/SECONDS)
        (t/log! :warn (str "Force killing " (name stream-type) " stream process - PID: " pid))
        (.destroyForcibly ^Process process)
        (.waitFor process 2 TimeUnit/SECONDS))

      (t/log! :info (str (name stream-type) " stream process stopped - PID: " pid))))
  nil)

(defn wait-for-process
  "Wait for a process to terminate." {:malli/schema [:=> [:cat ProcessInfo [:* :any]] [:maybe :int]]}
  [process-info & {:keys [timeout-seconds]}]
  (let [{:keys [process]} process-info]
    (if timeout-seconds
      (when (.waitFor process timeout-seconds TimeUnit/SECONDS)
        (.exitValue process))
      (do
        (.waitFor process)
        (.exitValue process)))))

;; ============================================================================
;; Process Pool Management
;; ============================================================================

(def ^:private processes (atom {}))

(defn spawn-and-register
  "Spawn a process and register it in the pool." {:malli/schema [:=> [:cat StreamType :string [:* :any]] ProcessInfo]}
  [stream-type host & opts]
  (when (get @processes stream-type)
    (throw (ex-info "Process already exists" {:stream-type stream-type})))
  (let [process-info (apply spawn-stream-process stream-type host opts)]
    (swap! processes assoc stream-type process-info)
    process-info))

(defn get-process
  "Get a registered process by stream type." {:malli/schema [:=> [:cat StreamType] [:maybe ProcessInfo]]}
  [stream-type]
  (get @processes stream-type))

(defn stop-all-processes
  "Stop all registered processes." {:malli/schema [:=> [:cat] :nil]}
  []
  (t/log! :info "Stopping all stream processes...")
  (doseq [[_ process-info] @processes]
    (stop-process process-info))
  (reset! processes {})
  nil)
