(ns potatoclient.ui.log-export
  "Log export functionality"
  (:require [seesaw.core :as seesaw]
            [seesaw.chooser :as chooser]
            [clojure.java.io :as io]
            [potatoclient.state :as state]))

(defn save-logs-dialog
  "Show a file dialog and save logs to the selected file"
  []
  (when-let [file (chooser/choose-file
                    :type :save
                    :filters [["Log files" ["log" "txt"]]
                              ["All files" ["*"]]])]
    (try
      (with-open [w (io/writer file)]
        (let [fmt (java.text.SimpleDateFormat. "yyyy-MM-dd HH:mm:ss.SSS")]
          (.write w "Potato Client Log Export\n")
          (.write w (str "Exported at: " (.format fmt (java.util.Date.)) "\n"))
          (.write w "=====================================\n\n")
          (doseq [entry @state/log-entries]
            (.write w (format "[%s] %s %s: %s\n"
                            (.format fmt (java.util.Date. (:time entry)))
                            (:stream entry)
                            (:type entry)
                            (:message entry)))
            (when (contains? #{"ERROR" "STDERR"} (:type entry))
              (.write w "\n")))))
      (seesaw/alert "Logs saved successfully!")
      (catch Exception e
        (seesaw/alert (str "Error saving logs: " (.getMessage e)))))))