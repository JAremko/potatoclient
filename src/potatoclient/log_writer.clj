(ns potatoclient.log-writer
  "Automatic file logging for non-release builds"
  (:require [clojure.java.io :as io]
            [malli.core :as m]
            [potatoclient.specs :as specs])
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

;; Note: Specs are imported from potatoclient.specs
;; Using specs/buffered-writer and specs/log-entry

(defn ^:private get-log-filename
  "Generate a log filename with current timestamp"
  []
  (let [timestamp (.format filename-formatter (Date.))]
    (str "logs/potatoclient_" timestamp ".log")))


(defn ^:private ensure-logs-directory!
  "Ensure the logs directory exists"
  []
  (let [logs-dir (io/file "logs")]
    (when-not (.exists logs-dir)
      (.mkdirs logs-dir))))


(defn ^:private create-log-writer!
  "Create a new buffered writer for the log file"
  [filename]
  (BufferedWriter. (FileWriter. filename true)))


(defn ^:private format-log-entry
  "Format a log entry for file output"
  [entry]
  (let [timestamp (.format ^SimpleDateFormat (.get log-formatter) (Date. (:time entry)))]
    (format "[%s] %s %s: %s"
            timestamp
            (:stream entry)
            (:type entry)
            (:message entry))))


(defn write-log-entry!
  "Write a log entry to the file if logging is enabled"
  [entry]
  (when (and @logging-enabled @log-writer)
    (try
      (let [formatted (format-log-entry entry)]
        (.write ^BufferedWriter @log-writer formatted)
        (.newLine ^BufferedWriter @log-writer)
        (.flush ^BufferedWriter @log-writer))
      (catch Exception e
        (println "Error writing to log file:" (.getMessage e))))))


(defn start-logging!
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


(defn stop-logging!
  "Stop file logging and close the writer"
  []
  (when @log-writer
    (try
      (.close ^BufferedWriter @log-writer)
      (catch Exception e
        (println "Error closing log file:" (.getMessage e))))
    (reset! log-writer nil)
    (reset! logging-enabled false)))


(defn is-logging-enabled?
  "Check if file logging is enabled"
  []
  @logging-enabled)

