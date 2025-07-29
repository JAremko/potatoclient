(ns potatoclient.frame-timing-test
  "Test frame timing extraction and CV start-tracking"
  (:require [clojure.test :refer [deftest testing is]]
            [potatoclient.cmd.cv :as cv]
            [potatoclient.cmd.core :as cmd-core]
            [clojure.core.async :as async :refer [<!! timeout]])
  (:import [ser JonSharedDataTypes$JonGuiDataVideoChannel]))

(deftest test-cv-start-tracking-with-frame-time
  (testing "CV start-tracking requires frame time"
    ;; Initialize mock websocket for testing
    (cmd-core/init-websocket! "test-domain" 
                              (fn [msg] (println "Error:" msg))
                              (fn [data] nil))

    ;; Test that start-tracking accepts channel and frame time parameters without throwing
    (let [channel ser.JonSharedDataTypes$JonGuiDataVideoChannel/JON_GUI_DATA_VIDEO_CHANNEL_HEAT]
      (try
        (cv/start-tracking channel 0.5 0.5 123456789)
        (is true "Successfully called start-tracking with channel and frame time")
        (catch Exception e
          (is false (str "Failed to call start-tracking: " (.getMessage e))))))

    ;; Give time for async operations
    (<!! (timeout 100))))