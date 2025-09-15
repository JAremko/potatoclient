(ns potatoclient.ui.connection-monitor
  "Monitor state server connection and handle disconnections in main frame."
  (:require
    [malli.core :as m]
    [potatoclient.logging :as logging]
    [potatoclient.state :as state]
    [potatoclient.state.server.core :as state-server]
    [potatoclient.ui.dialogs.connection-lost :as connection-lost]
    [seesaw.core :as seesaw])
  (:import (javax.swing JFrame)))

(def ^:private monitor-state
  "Atom holding the current monitor state"
  (atom {:monitoring? false
         :monitor-thread nil
         :dialog-shown? false
         :frame nil
         :on-cancel nil}))

(defn- handle-disconnection!
  "Handle state server disconnection by showing modal dialog."
  [frame on-cancel]
  (when-not (:dialog-shown? @monitor-state)
    (swap! monitor-state assoc :dialog-shown? true)
    (logging/log-warn {:msg "State connection lost, showing modal dialog"})

    ;; Show the modal dialog on EDT
    (seesaw/invoke-later
      (connection-lost/show-connection-lost-dialog
        frame
        (fn []
        ;; User clicked cancel - execute callback to return to initial menu
          (swap! monitor-state assoc :dialog-shown? false :monitoring? false)
          (when on-cancel
            (on-cancel)))))))

(m/=> handle-disconnection! [:=> [:cat any? [:maybe fn?]] :nil])

(defn- monitor-loop
  "Main monitoring loop that checks connection status."
  [frame on-cancel]
  (let [last-connected? (atom true)]
    (while (:monitoring? @monitor-state)
      (try
        (let [connected? (or (state-server/connected?)
                             (some? (:server-state @state/app-state)))]

          ;; Check for transition from connected to disconnected
          (when (and @last-connected? (not connected?))
            (logging/log-info {:msg "Detected state server disconnection"})
            (handle-disconnection! frame on-cancel))

          ;; Check for reconnection to close dialog
          (when (and (not @last-connected?) connected? (:dialog-shown? @monitor-state))
            (logging/log-info {:msg "State server reconnected, dialog should auto-close"})
            (swap! monitor-state assoc :dialog-shown? false))

          (reset! last-connected? connected?))

        (Thread/sleep 500)

        (catch InterruptedException _
          (logging/log-debug {:msg "Connection monitor interrupted"}))
        (catch Exception e
          (logging/log-error {:msg "Error in connection monitor" :error e})
          (Thread/sleep 2000))))))

(m/=> monitor-loop [:=> [:cat any? [:maybe fn?]] :nil])

(defn start-monitoring!
  "Start monitoring the state server connection.
   
   Parameters:
   - frame: The main application frame
   - on-cancel: Callback to execute when user cancels (returns to initial menu)
   
   Returns nil."
  [frame on-cancel]
  (when-not (:monitoring? @monitor-state)
    (logging/log-info {:msg "Starting state connection monitor"})

    (swap! monitor-state assoc
           :monitoring? true
           :frame frame
           :on-cancel on-cancel
           :dialog-shown? false)

    (let [thread (Thread.
                   #(monitor-loop frame on-cancel)
                   "state-connection-monitor")]
      (.setDaemon thread true)
      (.start thread)
      (swap! monitor-state assoc :monitor-thread thread)))
  nil)
(m/=> start-monitoring! [:=> [:cat [:fn {:error/message "must be a JFrame"} (partial instance? JFrame)] [:maybe :ifn]] :nil])

(defn stop-monitoring!
  "Stop monitoring the state server connection."
  []
  (when (:monitoring? @monitor-state)
    (logging/log-info {:msg "Stopping state connection monitor"})
    (swap! monitor-state assoc :monitoring? false)

    ;; Interrupt the monitor thread if it exists
    (when-let [thread (:monitor-thread @monitor-state)]
      (when (.isAlive thread)
        (.interrupt thread)))

    (swap! monitor-state assoc :monitor-thread nil))
  nil)
(m/=> stop-monitoring! [:=> [:cat] :nil])