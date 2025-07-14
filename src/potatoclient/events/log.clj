(ns potatoclient.events.log
  "Log event handling and formatting.
  
  Manages log entry formatting, buffering, and batch updates
  to prevent UI flooding."
  (:require [potatoclient.state :as state]
            [seesaw.core :as seesaw]
            [clojure.spec.alpha :as s])
  (:import [java.text SimpleDateFormat]
           [java.util Date]))

;; Constants
(def ^:private batch-update-delay-ms 100)
(def ^:private error-types #{"ERROR" "STDERR"})

;; Date formatter - thread-local for thread safety
(def ^:private ^ThreadLocal date-formatter
  (ThreadLocal/withInitial
   #(SimpleDateFormat. "HH:mm:ss.SSS")))

;; Specs
(s/def ::time pos-int?)
(s/def ::stream string?)
(s/def ::type string?)
(s/def ::message string?)
(s/def ::log-entry (s/keys :req-un [::time ::stream ::type ::message]
                          :opt-un [::raw-data]))

(defn- get-timestamp
  "Format a timestamp for display."
  [time-ms]
  (.format ^SimpleDateFormat (.get date-formatter) (Date. time-ms)))

(defn format-log-entry
  "Format a log entry for display."
  [entry]
  {:pre [(s/valid? ::log-entry entry)]}
  (format "[%s] %s %s: %s"
          (get-timestamp (:time entry))
          (:stream entry)
          (:type entry)
          (:message entry)))

(defn- write-to-console
  "Write log message to appropriate console stream."
  [log-msg log-type]
  (if (contains? error-types log-type)
    (binding [*out* *err*]
      (println log-msg))
    (println log-msg)))

(defn- schedule-batch-update!
  "Schedule a batch update if not already scheduled."
  []
  (when (compare-and-set! state/update-scheduled false true)
    (seesaw/timer
     (fn [_]
       (try
         (state/flush-log-buffer!)
         (finally
           (reset! state/update-scheduled false))))
     :delay batch-update-delay-ms
     :repeats? false)))

(defn add-log-entry!
  "Add a log entry with console output and batch buffering."
  [entry]
  {:pre [(s/valid? ::log-entry entry)]}
  ;; Write to console
  (let [log-msg (format-log-entry entry)]
    (write-to-console log-msg (:type entry)))
  
  ;; Add to state buffer
  (state/add-log-entry! entry)
  
  ;; Schedule batch update
  (schedule-batch-update!))

(defn- build-log-entry
  "Build a standardized log entry from various sources."
  [stream-id level message & {:keys [raw-data stack-trace]}]
  (let [full-message (if stack-trace
                      (str message "\n" stack-trace)
                      message)]
    (cond-> {:time (System/currentTimeMillis)
             :stream stream-id
             :type level
             :message full-message}
      raw-data (assoc :raw-data raw-data))))

(defn handle-log-event
  "Handle a log event from a stream process."
  [msg]
  {:pre [(map? msg)
         (string? (:streamId msg))
         (string? (:level msg))]}
  (add-log-entry!
   (build-log-entry (:streamId msg)
                   (:level msg)
                   (:message msg)
                   :raw-data msg
                   :stack-trace (:stackTrace msg))))

;; Utility functions for common log operations
(defn log-info
  "Log an informational message."
  [stream-id message]
  (add-log-entry!
   (build-log-entry stream-id "INFO" message)))

(defn log-error
  "Log an error message."
  [stream-id message & {:keys [exception]}]
  (add-log-entry!
   (build-log-entry stream-id "ERROR" message
                   :stack-trace (when exception
                                 (.toString exception)))))

(defn log-warning
  "Log a warning message."
  [stream-id message]
  (add-log-entry!
   (build-log-entry stream-id "WARN" message)))