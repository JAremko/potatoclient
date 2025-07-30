(ns potatoclient.transit.subprocess-launcher
  "Process lifecycle management for Transit subprocesses (Command and State).
  
  Extends the existing process management infrastructure to support
  Transit-based communication with protobuf isolation in Kotlin subprocesses."
  (:require [clojure.core.async :as async :refer [>!! <!! go-loop]]
            [clojure.java.io :as io]
            [clojure.string :as str]
            [com.fulcrologic.guardrails.malli.core :refer [=> >defn >defn- ?]]
            [potatoclient.logging :as logging]
            [potatoclient.transit.core :as transit-core]
            [potatoclient.transit.app-db :as app-db]
            [potatoclient.transit.handlers :as handlers])
  (:import (clojure.core.async.impl.channels ManyToManyChannel)
           (java.io InputStream OutputStream)
           (java.lang Process ProcessBuilder)
           (java.util List Map)))

;; Configuration
(def ^:private shutdown-grace-period-ms 100)
(def ^:private channel-buffer-size 1000)

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
  [(? [:map [:appdir string?]]) => [:map-of string? string?]]
  (let [base-env {"GST_DEBUG" "2"
                  "GST_DEBUG_NO_COLOR" "1"}]
    (if app-env
      (merge base-env
             {"APPDIR" (:appdir app-env)
              "LD_LIBRARY_PATH" (:lib-path app-env)
              "GST_PLUGIN_PATH" (:plugin-path app-env)
              "GST_PLUGIN_SCANNER" (:scanner-path app-env)})
      base-env)))

(>defn- build-jvm-args
  "Build JVM arguments including AppImage-specific paths."
  [app-env]
  [(? [:map [:appdir string?]]) => [:sequential string?]]
  (let [base-args ["-Xms256m" "-Xmx512m" "-XX:+UseG1GC"]
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
  "Create a configured ProcessBuilder for Transit subprocess."
  [subprocess-type ws-url]
  [[:enum :command :state] string? => [:fn #(instance? ProcessBuilder %)]]
  (let [java-exe (get-java-executable)
        classpath (System/getProperty "java.class.path")
        app-env (get-appimage-environment)
        jvm-args (build-jvm-args app-env)
        main-class (case subprocess-type
                     :command "potatoclient.kotlin.transit.CommandSubprocess"
                     :state "potatoclient.kotlin.transit.StateSubprocess")
        cmd (vec (concat [java-exe "-cp" classpath]
                         jvm-args
                         [main-class ws-url]))]
    (doto (ProcessBuilder. ^List cmd)
      (-> .environment (.putAll ^Map (build-process-environment app-env))))))

(>defn- create-transit-reader
  "Create a Transit reader thread that processes messages from subprocess."
  [^InputStream input-stream output-chan subprocess-type]
  [[:fn #(instance? InputStream %)]
   [:fn {:error/message "must be a core.async channel"}
    #(instance? ManyToManyChannel %)]
   [:enum :command :state]
   => [:fn {:error/message "must be a core.async channel"}
       #(instance? ManyToManyChannel %)]]
  (let [reader (transit-core/make-reader input-stream)
        read-fn (fn [] (transit-core/read-message reader))]
    (go-loop []
      (when-let [msg (try
                       (read-fn)
                       (catch Exception e
                         (logging/log-error {:msg "Error reading Transit message"
                                             :error e
                                             :subprocess subprocess-type})
                         nil))]
        (>!! output-chan msg)
        (recur)))))

(>defn- create-transit-writer
  "Create a function to write Transit messages to subprocess."
  [^OutputStream output-stream]
  [[:fn #(instance? OutputStream %)]
   => fn?]
  (let [writer (transit-core/make-writer output-stream)]
    (fn [message]
      (try
        (transit-core/write-message! writer message output-stream)
        true
        (catch Exception e
          (logging/log-error {:msg "Error writing Transit message" :error e})
          false)))))

(>defn- create-subprocess
  "Create and start a Transit subprocess."
  [subprocess-type ws-url]
  [[:enum :command :state] string? => :potatoclient.specs.transit/subprocess]
  (let [^ProcessBuilder pb (create-process-builder subprocess-type ws-url)
        ^Process process (.start pb)
        input-stream (.getInputStream process)
        output-stream (.getOutputStream process)
        error-stream (.getErrorStream process)
        output-chan (async/chan channel-buffer-size)
        write-fn (create-transit-writer output-stream)]

    ;; Start reader thread
    (create-transit-reader input-stream output-chan subprocess-type)

    ;; Start error stream reader
    (go-loop []
      (let [error-reader (io/reader error-stream)]
        (when-let [line (.readLine error-reader)]
          (logging/log-error {:msg "Subprocess stderr"
                              :subprocess subprocess-type
                              :message line})
          (recur))))

    {:subprocess-type subprocess-type
     :process process
     :input-stream input-stream
     :output-stream output-stream
     :error-stream error-stream
     :output-chan output-chan
     :write-fn write-fn
     :state (atom :starting)}))

(>defn start-command-subprocess
  "Start the command subprocess that sends commands to WebSocket."
  [ws-url]
  [string? => :potatoclient.specs.transit/subprocess]
  (let [subprocess (create-subprocess :command ws-url)]
    ;; Update app-db
    (app-db/set-process-state! :cmd-proc (.pid ^Process (:process subprocess)) :running)
    (logging/log-info {:msg "Started command subprocess" :url ws-url})
    subprocess))

(>defn start-state-subprocess
  "Start the state subprocess that receives state from WebSocket."
  [ws-url]
  [string? => :potatoclient.specs.transit/subprocess]
  (let [subprocess (create-subprocess :state ws-url)]
    ;; Update app-db
    (app-db/set-process-state! :state-proc (.pid ^Process (:process subprocess)) :running)
    (logging/log-info {:msg "Started state subprocess" :url ws-url})
    subprocess))

(>defn send-message!
  "Send a message to a subprocess."
  [subprocess message]
  [:potatoclient.specs.transit/subprocess map? => boolean?]
  ((:write-fn subprocess) message))

(>defn read-message
  "Read a message from subprocess output channel."
  [subprocess timeout-ms]
  [:potatoclient.specs.transit/subprocess pos-int? => (? map?)]
  (async/alt!!
    (:output-chan subprocess) ([msg] msg)
    (async/timeout timeout-ms) nil))

(>defn stop-subprocess!
  "Stop a subprocess gracefully."
  [subprocess]
  [:potatoclient.specs.transit/subprocess => nil?]
  (let [process ^Process (:process subprocess)
        subprocess-type (:subprocess-type subprocess)]
    (try
      ;; Send shutdown command
      (send-message! subprocess {:msg-type "control"
                                 :msg-id (str (java.util.UUID/randomUUID))
                                 :timestamp (System/currentTimeMillis)
                                 :payload {:action "shutdown"}})

      ;; Give it time to shutdown gracefully
      (Thread/sleep shutdown-grace-period-ms)

      ;; Force destroy if still alive
      (when (.isAlive process)
        (.destroyForcibly process))

      ;; Close streams
      (.close ^InputStream (:input-stream subprocess))
      (.close ^OutputStream (:output-stream subprocess))
      (.close ^InputStream (:error-stream subprocess))

      ;; Close channel
      (async/close! (:output-chan subprocess))

      ;; Update app-db
      (app-db/set-process-state! (case subprocess-type
                                   :command :cmd-proc
                                   :state :state-proc)
                                 nil :stopped)

      (logging/log-info {:msg "Stopped subprocess" :type subprocess-type})
      nil

      (catch Exception e
        (logging/log-error {:msg "Error stopping subprocess"
                            :error e
                            :type subprocess-type})
        nil))))

(>defn subprocess-alive?
  "Check if a subprocess is still running."
  [subprocess]
  [:potatoclient.specs.transit/subprocess => boolean?]
  (.isAlive ^Process (:process subprocess)))

(>defn restart-subprocess!
  "Restart a subprocess."
  [subprocess ws-url]
  [:potatoclient.specs.transit/subprocess string? => :potatoclient.specs.transit/subprocess]
  (let [subprocess-type (:subprocess-type subprocess)]
    (stop-subprocess! subprocess)
    (case subprocess-type
      :command (start-command-subprocess ws-url)
      :state (start-state-subprocess ws-url))))