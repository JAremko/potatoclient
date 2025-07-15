(ns potatoclient.log-writer
  "Automatic file logging for non-release builds"
  (:require [clojure.java.io :as io]
            [clojure.spec.alpha :as s]
            [orchestra.core :refer [defn-spec]])
  (:import [java.io BufferedWriter FileWriter]
           [java.text SimpleDateFormat]
           [java.util Date]))

;; Log writer state
(def ^:private log-writer (atom nil))
(def ^:private logging-enabled (atom false))

;; Date formatter for log filenames
(def ^:private filename-formatter
  (SimpleDateFormat. "yyyy-MM-dd_HH-mm-ss"))

;; Date formatter for log entries
(def ^:private ^ThreadLocal log-formatter
  (ThreadLocal/withInitial
   #(SimpleDateFormat. "HH:mm:ss.SSS")))

;; Specs
(s/def ::writer #(instance? BufferedWriter %))
(s/def ::time pos-int?)
(s/def ::stream string?)
(s/def ::type string?)
(s/def ::message string?)
(s/def ::log-entry (s/keys :req-un [::time ::stream ::type ::message]))

(defn-spec ^:private get-log-filename string?
  "Generate a log filename with current timestamp"
  []
  (let [timestamp (.format filename-formatter (Date.))]
    (str "logs/potatoclient_" timestamp ".log")))

(defn-spec ^:private ensure-logs-directory! any?
  "Ensure the logs directory exists"
  []
  (let [logs-dir (io/file "logs")]
    (when-not (.exists logs-dir)
      (.mkdirs logs-dir))))

(defn-spec ^:private create-log-writer! ::writer
  "Create a new buffered writer for the log file"
  [filename string?]
  (BufferedWriter. (FileWriter. filename true)))

(defn-spec ^:private format-log-entry string?
  "Format a log entry for file output"
  [entry ::log-entry]
  (let [timestamp (.format ^SimpleDateFormat (.get log-formatter) (Date. (:time entry)))]
    (format "[%s] %s %s: %s"
            timestamp
            (:stream entry)
            (:type entry)
            (:message entry))))

(defn-spec write-log-entry! any?
  "Write a log entry to the file if logging is enabled"
  [entry ::log-entry]
  (when (and @logging-enabled @log-writer)
    (try
      (let [formatted (format-log-entry entry)]
        (.write ^BufferedWriter @log-writer formatted)
        (.newLine ^BufferedWriter @log-writer)
        (.flush ^BufferedWriter @log-writer))
      (catch Exception e
        (println "Error writing to log file:" (.getMessage e))))))

(defn-spec start-logging! boolean?
  "Start automatic file logging if not in release mode"
  []
  (when-not (or (System/getProperty "potatoclient.release")
                (System/getenv "POTATOCLIENT_RELEASE"))
    (try
      (ensure-logs-directory!)
      (let [filename (get-log-filename)
            writer (create-log-writer! filename)]
        (reset! log-writer writer)
        (reset! logging-enabled true)
        (println (str "Logging to file: " filename))
        true)
      (catch Exception e
        (println "Failed to start file logging:" (.getMessage e))
        false))))

(defn-spec stop-logging! any?
  "Stop file logging and close the writer"
  []
  (when @log-writer
    (try
      (.close ^BufferedWriter @log-writer)
      (catch Exception e
        (println "Error closing log file:" (.getMessage e))))
    (reset! log-writer nil)
    (reset! logging-enabled false)))

(defn-spec is-logging-enabled? boolean?
  "Check if file logging is enabled"
  []
  @logging-enabled)