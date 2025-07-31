(ns potatoclient.logging
  "Centralized logging configuration using Telemere"
  (:require [clojure.java.io :as io]
            [clojure.string]
            [com.fulcrologic.guardrails.malli.core :refer [=> >defn >defn-]]
            [potatoclient.runtime :as runtime]
            [taoensso.telemere :as tel])
  (:import (java.time LocalDateTime)
           (java.time.format DateTimeFormatter)))

(>defn- get-version
  "Get application version"
  []
  [=> string?]
  (try
    (clojure.string/trim (slurp (clojure.java.io/resource "VERSION")))
    (catch Exception _ "dev")))

(>defn- get-log-dir
  "Get the log directory path using platform-specific conventions"
  []
  [=> :potatoclient.specs/file]
  (let [os-name (.toLowerCase ^String (System/getProperty "os.name"))]
    (cond
      ;; Windows - use LOCALAPPDATA if available, fallback to APPDATA
      (.contains ^String os-name "win")
      (let [local-appdata (System/getenv "LOCALAPPDATA")
            appdata (System/getenv "APPDATA")
            user-home (System/getProperty "user.home")]
        (io/file (or local-appdata
                     appdata
                     (str user-home "/AppData/Local"))
                 "PotatoClient"
                 "logs"))

      ;; macOS - use standard Application Support directory
      (.contains ^String os-name "mac")
      (io/file (System/getProperty "user.home")
               "Library"
               "Application Support"
               "PotatoClient"
               "logs")

      ;; Linux/Unix - follow XDG Base Directory specification
      :else
      (let [xdg-data (System/getenv "XDG_DATA_HOME")
            user-home (System/getProperty "user.home")
            data-base (if (and xdg-data
                               (.startsWith ^String xdg-data "/")
                               (not= xdg-data user-home))
                        xdg-data
                        (io/file user-home ".local" "share"))]
        (io/file data-base "potatoclient" "logs")))))

(>defn- get-log-file-path
  "Get the path for the log file with timestamp and version"
  []
  [=> :potatoclient.specs/file]
  (let [logs-dir (if (runtime/release-build?)
                   (get-log-dir)
                   (io/file "logs"))
        version (get-version)
        timestamp (.format (DateTimeFormatter/ofPattern "yyyyMMdd-HHmmss")
                           (LocalDateTime/now))
        filename (format "potatoclient-%s-%s.log" version timestamp)]
    (.mkdirs logs-dir)
    (io/file logs-dir filename)))

(>defn- create-file-handler
  "Create a file handler for Telemere"
  []
  [=> fn?]
  (tel/handler:file
    {:output-fn (tel/format-signal-fn)
     :path (.getAbsolutePath (get-log-file-path))}))

(>defn- cleanup-old-logs!
  "Keep only the newest N log files, delete older ones"
  [max-files]
  [pos-int? => nil?]
  (let [log-dir (if (runtime/release-build?)
                  (get-log-dir)
                  (io/file "logs"))]
    (when (.exists log-dir)
      (let [log-files (->> (.listFiles log-dir)
                           (filter #(and (.isFile %)
                                         (clojure.string/ends-with? (.getName %) ".log")
                                         (clojure.string/starts-with? (.getName %) "potatoclient-")))
                           (sort-by #(.lastModified %) >))
            files-to-delete (drop max-files log-files)]
        (doseq [file files-to-delete]
          (try
            (.delete file)
                  ;; Can't use logging here as we're in the logging initialization
                  ;; Using println is appropriate for bootstrap logging
            (println (str "Deleted old log file: " (.getName file)))
            (catch Exception e
              (println (str "Failed to delete log file: " (.getName file) " - " (.getMessage e))))))))))

;; Track if logging has been initialized
(def ^:private initialized? (atom false))

(>defn init!
  "Initialize the logging system"
  []
  [=> nil?]
  ;; Prevent double initialization
  (when (compare-and-set! initialized? false true)
    ;; Clean up old logs first (keep newest 50)
    (cleanup-old-logs! 50)

    ;; Remove ALL handlers first to ensure clean state
    ;; Telemere adds a default console handler automatically which causes duplication
    (tel/remove-handler! :default/console)  ; Remove the auto-added default console handler
    (tel/remove-handler! :default)
    (tel/remove-handler! :console) 
    (tel/remove-handler! :file)

    ;; Set minimum level based on build type
    (if (runtime/release-build?)
      (tel/set-min-level! :warn) ; Only warnings and errors in production
      (tel/set-min-level! :trace)) ; All levels in development

    ;; Configure handlers based on build type
    (if (runtime/release-build?)
      ;; Production: critical events to both console and file
      (do
        (tel/add-handler!
          :console
          (tel/handler:console
            {:output-fn (tel/format-signal-fn)})
          {:async {:mode :dropping, :buffer-size 1024}
           :min-level :warn})

        ;; Also log to file in production for critical events
        (tel/add-handler!
          :file
          (create-file-handler)
          {:async {:mode :dropping, :buffer-size 1024}
           :min-level :warn}))
      ;; Development: console + file logging
      (do
        (tel/add-handler!
          :console
          (tel/handler:console
            {:output-fn (tel/format-signal-fn)})
          {:async {:mode :dropping, :buffer-size 1024}})

        (tel/add-handler!
          :file
          (create-file-handler)
          {:async {:mode :dropping, :buffer-size 2048}})))

    ;; Filter out noisy Java packages
    (tel/set-ns-filter! {:disallow ["com.sun.*" "java.awt.*" "javax.swing.*"]})

    ;; Log startup
    (tel/log!
      {:level :info
       :id ::startup
       :data {:build-type (if (runtime/release-build?) "RELEASE" "DEVELOPMENT")}}
      "PotatoClient logging initialized")
    
    ;; Log where init was called from for debugging double-init
    (let [stack-trace (Thread/currentThread)
          stack (.getStackTrace stack-trace)]
      (tel/log! {:level :debug
                 :id ::init-stack
                 :data {:caller (take 10 (map str stack))}}
                "Logging init called from")))

  ;; Ensure we return nil
  nil)

(>defn shutdown!
  "Shutdown the logging system"
  []
  [=> nil?]
  (tel/log! {:level :info :id ::shutdown} "Shutting down logging system")
  (tel/stop-handlers!)
  nil)

;; Convenience logging macros that match our previous API
(defmacro log-info
  "Log an info level message."
  [& args]
  `(tel/log! :info ~@args))

(defmacro log-debug
  "Log a debug level message."
  [& args]
  `(tel/log! :debug ~@args))

(defmacro log-warn
  "Log a warning level message."
  [& args]
  `(tel/log! :warn ~@args))

(defmacro log-error
  "Log an error level message."
  [& args]
  `(tel/log! :error ~@args))

;; Event logging for specific types
(defmacro log-event
  "Log a specific event with id and data."
  [id data & [msg]]
  `(tel/event! ~id {:level :info :data ~data :msg ~msg}))

(defmacro log-stream-event
  "Log a stream-specific event."
  [stream-type event-type data]
  `(tel/event! ::stream-event
               {:level :info
                :data (merge {:stream ~stream-type
                              :event ~event-type}
                             ~data)}))

(>defn get-logs-directory
  "Get the logs directory path. Public function for UI access."
  []
  [=> :potatoclient.specs/file]
  (if (runtime/release-build?)
    (get-log-dir)
    (io/file "logs")))