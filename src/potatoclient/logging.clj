(ns potatoclient.logging
  "Centralized logging configuration using Telemere"
  (:require [taoensso.telemere :as tel]
            [potatoclient.runtime :as runtime]
            [potatoclient.config :as config]
            [clojure.java.io :as io]
            [clojure.string])
  (:import [java.time LocalDateTime]
           [java.time.format DateTimeFormatter]))

(defn- get-version
  "Get application version"
  []
  (try
    (clojure.string/trim (slurp (clojure.java.io/resource "VERSION")))
    (catch Exception _ "dev")))

(defn- get-log-dir
  "Get the log directory path using platform-specific conventions"
  []
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

(defn- get-log-file-path
  "Get the path for the log file with timestamp and version"
  []
  (let [logs-dir (if (runtime/release-build?)
                   (get-log-dir)
                   (io/file "logs"))
        version (get-version)
        timestamp (.format (DateTimeFormatter/ofPattern "yyyyMMdd-HHmmss")
                          (LocalDateTime/now))
        filename (format "potatoclient-%s-%s.log" version timestamp)]
    (.mkdirs logs-dir)
    (io/file logs-dir filename)))

(defn- create-file-handler
  "Create a file handler for Telemere"
  []
  (let [log-file (get-log-file-path)
        formatter (DateTimeFormatter/ofPattern "yyyy-MM-dd HH:mm:ss.SSS")]
    (tel/handler:console
      {:output-fn
       (tel/format-signal-fn
         {:format-inst-fn
          (fn [inst]
            (.format formatter (LocalDateTime/ofInstant inst (java.time.ZoneId/systemDefault))))})
       :stream (io/writer log-file :append true)})))

(defn- cleanup-old-logs!
  "Keep only the newest N log files, delete older ones"
  [max-files]
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
            (println (str "Deleted old log file: " (.getName file)))
            (catch Exception e
              (println (str "Failed to delete log file: " (.getName file) " - " (.getMessage e))))))))))

(defn init!
  "Initialize the logging system"
  []
  ;; Clean up old logs first (keep newest 50)
  (cleanup-old-logs! 50)
  
  ;; Remove default console handler
  (tel/remove-handler! :default/console)
  
  ;; Set minimum level based on build type
  (if (runtime/release-build?)
    (tel/set-min-level! :warn)  ; Only warnings and errors in production
    (tel/set-min-level! :debug))
  
  ;; Configure handlers based on build type
  (if (runtime/release-build?)
    ;; Production: critical events to both console and file
    (do
      (tel/add-handler! :console
        (tel/handler:console
          {:output-fn (tel/format-signal-fn {})})
        {:async {:mode :dropping, :buffer-size 1024}
         :min-level :warn})
      
      ;; Also log to file in production for critical events
      (tel/add-handler! :file
        (create-file-handler)
        {:async {:mode :dropping, :buffer-size 1024}
         :min-level :warn}))
    ;; Development: console + file logging
    (do
      (tel/add-handler! :console
        (tel/handler:console
          {:output-fn (tel/format-signal-fn {})})
        {:async {:mode :dropping, :buffer-size 1024}})
      
      (tel/add-handler! :file
        (create-file-handler)
        {:async {:mode :dropping, :buffer-size 2048}})))
  
  ;; Filter out noisy Java packages
  (tel/set-ns-filter! {:disallow ["com.sun.*" "java.awt.*" "javax.swing.*"]})
  
  ;; Log startup
  (tel/log! 
    {:level :info
     :id ::startup
     :data {:build-type (if (runtime/release-build?) "RELEASE" "DEVELOPMENT")}}
    "PotatoClient logging initialized"))

(defn shutdown!
  "Shutdown the logging system"
  []
  (tel/log! {:level :info :id ::shutdown} "Shutting down logging system")
  (tel/stop-handlers!))

;; Convenience logging macros that match our previous API
(defmacro log-info [& args]
  `(tel/log! :info ~@args))

(defmacro log-debug [& args]
  `(tel/log! :debug ~@args))

(defmacro log-warn [& args]
  `(tel/log! :warn ~@args))

(defmacro log-error [& args]
  `(tel/log! :error ~@args))

;; Event logging for specific types
(defmacro log-event [id data & [msg]]
  `(tel/event! ~id {:level :info :data ~data :msg ~msg}))

(defmacro log-stream-event [stream-type event-type data]
  `(tel/event! ::stream-event
     {:level :info
      :data (merge {:stream ~stream-type
                    :event ~event-type}
                   ~data)}))

(defn get-logs-directory
  "Get the logs directory path. Public function for UI access."
  []
  (if (runtime/release-build?)
    (get-log-dir)
    (io/file "logs")))