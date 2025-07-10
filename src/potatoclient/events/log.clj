(ns potatoclient.events.log
  "Log event handling and formatting"
  (:require [potatoclient.state :as state]
            [seesaw.core :as seesaw]))

(defn format-log-entry
  "Format a log entry for display"
  [entry]
  (let [fmt (java.text.SimpleDateFormat. "HH:mm:ss.SSS")
        timestamp (.format fmt (java.util.Date. (:time entry)))]
    (format "[%s] %s %s: %s"
            timestamp
            (:stream entry)
            (:type entry)
            (:message entry))))

(defn add-log-entry!
  "Add a log entry and print to appropriate output stream"
  [entry]
  ;; Write to stdout/stderr for logging
  (let [log-msg (format-log-entry entry)]
    (if (contains? #{"ERROR" "STDERR"} (:type entry))
      (binding [*out* *err*]
        (println log-msg))
      (println log-msg)))
  
  ;; Add to buffer
  (state/add-log-entry! entry)
  
  ;; Schedule batch update if not already scheduled
  (when (compare-and-set! state/update-scheduled false true)
    (seesaw/timer
      (fn [_]
        (state/flush-log-buffer!)
        (reset! state/update-scheduled false))
      :delay 100
      :repeats? false)))

(defn handle-log-event
  "Handle a log event from a stream process"
  [msg]
  (let [stack-trace (:stackTrace msg)
        full-message (if stack-trace
                       (str (:message msg) "\n" stack-trace)
                       (:message msg))]
    (add-log-entry! {:time (System/currentTimeMillis)
                     :stream (:streamId msg)
                     :type (:level msg)
                     :message full-message
                     :raw-data msg})))