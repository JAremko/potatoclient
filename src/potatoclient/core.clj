(ns potatoclient.core
  "Main entry point for the Potato Client application"
  (:require [seesaw.core :as seesaw]
            [potatoclient.state :as state]
            [potatoclient.process :as process]
            [potatoclient.ui.control-panel :as control-panel]
            [potatoclient.ui.log-table :as log-table]
            [potatoclient.events.log :as log])
  (:gen-class))

(defn- create-main-window
  "Create and configure the main application window"
  []
  (let [control-panel (control-panel/create)
        log-table (log-table/create)]
    (seesaw/frame
      :title "WebSocket Video Streams - Control Center"
      :icon (clojure.java.io/resource "main.png")
      :on-close :exit
      :size [800 :by 600]
      :content (seesaw/border-panel
                 :north control-panel
                 :center log-table))))

(defn- setup-shutdown-hook
  "Setup JVM shutdown hook to clean up processes"
  []
  (.addShutdownHook (Runtime/getRuntime)
    (Thread. #(process/cleanup-all-processes state/app-state))))

(defn -main
  "Application entry point"
  [& args]
  ;; Enable native look and feel
  (seesaw/native!)
  
  ;; Setup shutdown hook
  (setup-shutdown-hook)
  
  ;; Create and show the main window
  (seesaw/invoke-later
    (-> (create-main-window)
        seesaw/show!))
  
  ;; Log startup
  (log/add-log-entry! {:time (System/currentTimeMillis)
                       :stream "SYSTEM"
                       :type "INFO"
                       :message "Control Center started"}))