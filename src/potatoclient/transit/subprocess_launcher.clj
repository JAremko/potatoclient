(ns potatoclient.transit.subprocess-launcher
  "Process lifecycle management for Transit subprocesses (Command and State).
  
  Extends the existing process management infrastructure to support
  Transit-based communication with protobuf isolation in Kotlin subprocesses."
  (:require [clojure.java.io :as io]
            [clojure.string :as str]
            [com.fulcrologic.guardrails.malli.core :refer [=> >defn >defn- ?]]
            [potatoclient.logging :as logging]
            [potatoclient.specs :as specs]
            [potatoclient.transit.app-db :as app-db]
            [potatoclient.transit.core :as transit-core]
            [potatoclient.transit.framed-io :as framed-io])
  (:import (java.io InputStream OutputStream)
           (java.lang Process ProcessBuilder)
           (java.util List Map)))

;; Configuration
(def ^:private shutdown-grace-period-ms 100)

;; Global subprocess tracking
(defonce ^:private subprocess-registry (atom {}))

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
  [subprocess-type url domain]
  [keyword? string? string? => [:fn #(instance? ProcessBuilder %)]]
  (let [java-exe (get-java-executable)
        classpath (System/getProperty "java.class.path")
        app-env (get-appimage-environment)
        jvm-args (build-jvm-args app-env)
        main-class (case subprocess-type
                     :command "potatoclient.kotlin.transit.CommandSubprocessKt"
                     :state "potatoclient.kotlin.transit.StateSubprocessKt")
        cmd (vec (concat [java-exe "-cp" classpath]
                         jvm-args
                         [main-class url domain]))]
    (doto (ProcessBuilder. ^List cmd)
      (-> .environment (.putAll ^Map (build-process-environment app-env))))))

(>defn- create-reader-thread
  "Create a reader thread that reads Transit messages and calls the handler.
  Returns a function that can be called to start the thread."
  [^InputStream input-stream subprocess-type message-handler]
  [[:fn #(instance? InputStream %)]
   keyword?
   fn?
   => fn?]
  (fn []
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
                                 (when-not (.contains (.getMessage e) "Stream closed")
                                   (logging/log-error
                                     {:id ::transit-read-error
                                      :data {:subprocess subprocess-type
                                             :error (.getMessage e)}
                                      :msg (str "Error reading Transit message from " (name subprocess-type))}))
                                 nil))]
                (when (map? msg)
                  ;; Call the message handler directly
                  (message-handler msg))
                (recur))))
          (catch Exception e
            (logging/log-error
              {:id ::reader-thread-error
               :data {:subprocess subprocess-type
                      :error (.getMessage e)}
               :msg "Error in reader thread"})))))))

(>defn- create-subprocess
  "Create the subprocess and I/O resources."
  [subprocess-type url domain message-handler]
  [keyword? string? string? fn? => ::specs/transit-subprocess]
  (let [^ProcessBuilder pb (create-process-builder subprocess-type url domain)
        ^Process process (.start pb)
        input-stream (.getInputStream process)
        output-stream (.getOutputStream process)
        error-stream (.getErrorStream process)
        framed-output (framed-io/make-framed-output-stream output-stream)
        writer (transit-core/make-writer framed-output)
        write-fn (fn [msg]
                   (transit-core/write-message! writer msg framed-output))
        state (atom :starting)]
    {:process process
     :subprocess-type subprocess-type
     :url url
     :input-stream input-stream
     :output-stream output-stream
     :error-stream error-stream
     :write-fn write-fn
     :message-handler message-handler
     :state state}))

(>defn- monitor-error-stream
  "Monitor subprocess error stream and log output."
  [subprocess-type ^InputStream error-stream]
  [keyword? [:fn #(instance? InputStream %)] => fn?]
  (fn []
    (Thread.
      (fn []
        (try
          (with-open [reader (io/reader error-stream)]
            (loop []
              (when-let [line (.readLine ^java.io.BufferedReader reader)]
                (logging/log-error
                  {:id ::subprocess-stderr
                   :data {:subprocess subprocess-type
                          :line line}
                   :msg (str (name subprocess-type) " subprocess stderr: " line)})
                (recur))))
          (catch Exception e
            (when-not (.contains (.getMessage e) "Stream closed")
              (logging/log-error
                {:id ::stderr-monitor-error
                 :data {:subprocess subprocess-type
                        :error (.getMessage e)}
                 :msg "Error monitoring stderr"}))))))))

(>defn start-subprocess
  "Start a Transit subprocess with a message handler.
  The handler will be called directly from the reader thread."
  [subprocess-type url domain message-handler]
  [keyword? string? string? fn? => ::specs/transit-subprocess]
  (let [subprocess (create-subprocess subprocess-type url domain message-handler)
        reader-thread ((create-reader-thread (:input-stream subprocess)
                                             subprocess-type
                                             message-handler))
        error-thread ((monitor-error-stream subprocess-type
                                            (:error-stream subprocess)))]
    ;; Start threads
    (.start reader-thread)
    (.start error-thread)

    ;; Update state
    (reset! (:state subprocess) :running)

    ;; Register subprocess
    (swap! subprocess-registry assoc subprocess-type subprocess)

    (logging/log-info
      {:id ::subprocess-started
       :data {:subprocess-type subprocess-type
              :url url}
       :msg (str "Started " (name subprocess-type) " subprocess")})

    subprocess))

(>defn send-message
  "Send a Transit message to a subprocess."
  [subprocess-type message]
  [keyword? map? => boolean?]
  (if-let [subprocess (get @subprocess-registry subprocess-type)]
    (let [current-state @(:state subprocess)]
      (if (= current-state :running)
        (try
          ((:write-fn subprocess) message)
          (logging/log-debug
            {:id ::message-sent
             :data {:subprocess subprocess-type
                    :msg-type (:msg-type message)}
             :msg (str "Sent " (:msg-type message) " to " (name subprocess-type))})
          true
          (catch Exception e
            (logging/log-error
              {:id ::send-error
               :data {:subprocess subprocess-type
                      :error (.getMessage e)}
               :msg "Failed to send message"})
            false))
        (do
          (logging/log-warn
            {:id ::subprocess-not-running
             :data {:subprocess subprocess-type
                    :state current-state}
             :msg (str "Cannot send to " (name subprocess-type) " in state " current-state)})
          false)))
    (do
      (logging/log-error
        {:id ::subprocess-not-found
         :data {:subprocess subprocess-type}
         :msg (str "Subprocess " (name subprocess-type) " not found")})
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

(>defn stop-subprocess
  "Stop a subprocess gracefully."
  [subprocess-type]
  [keyword? => nil?]
  (when-let [subprocess (get @subprocess-registry subprocess-type)]
    (logging/log-info
      {:id ::stopping-subprocess
       :data {:subprocess-type subprocess-type}
       :msg (str "Stopping " (name subprocess-type) " subprocess")})

    ;; Update state
    (reset! (:state subprocess) :stopping)

    ;; Send shutdown message if possible
    (send-message subprocess-type
                  {:msg-type "command"
                   :msg-id (str (java.util.UUID/randomUUID))
                   :timestamp (System/currentTimeMillis)
                   :payload {:action "shutdown"}})

    ;; Wait briefly for graceful shutdown
    (Thread/sleep shutdown-grace-period-ms)

    ;; Force kill if still alive
    (let [^Process process (:process subprocess)]
      (when (.isAlive process)
        (.destroyForcibly process)))

    ;; Close resources
    (close-resource (:input-stream subprocess) #(.close ^InputStream %))
    (close-resource (:output-stream subprocess) #(.close ^OutputStream %))
    (close-resource (:error-stream subprocess) #(.close ^InputStream %))

    ;; Update state and remove from registry
    (reset! (:state subprocess) :stopped)
    (swap! subprocess-registry dissoc subprocess-type)

    (logging/log-info
      {:id ::subprocess-stopped
       :data {:subprocess-type subprocess-type}
       :msg (str "Stopped " (name subprocess-type) " subprocess")}))
  nil)

(>defn start-command-subprocess
  "Start the command subprocess that converts Transit commands to protobuf."
  [url domain]
  [string? string? => ::specs/transit-subprocess]
  (start-subprocess :command url domain app-db/handle-command-response))

(>defn start-state-subprocess
  "Start the state subprocess that converts protobuf state to Transit."
  [url domain]
  [string? string? => ::specs/transit-subprocess]
  (start-subprocess :state url domain app-db/handle-state-update))

(>defn stop-all-subprocesses
  "Stop all running subprocesses."
  []
  [=> nil?]
  (doseq [subprocess-type (keys @subprocess-registry)]
    (stop-subprocess subprocess-type))
  nil)