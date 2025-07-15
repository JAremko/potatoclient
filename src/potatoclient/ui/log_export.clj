(ns potatoclient.ui.log-export
  "Log export functionality"
  (:require [clojure.java.io :as io]
            [potatoclient.state :as state]
            [potatoclient.i18n :as i18n]
            [malli.core :as m]
            [potatoclient.specs :as specs])
  (:use [seesaw core chooser]))

(defn save-logs-dialog
  "Show a file dialog and save logs to the selected file"
  []
  (when-let [file (choose-file
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
      (alert (i18n/tr :export-success))
      (catch Exception e
        (alert (str (i18n/tr :export-error) ": " (.getMessage e)))))))

