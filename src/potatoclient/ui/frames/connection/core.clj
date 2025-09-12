(ns potatoclient.ui.frames.connection.core
  "Connection frame that displays ping status and connection progress."
  (:require
            [malli.core :as m]
    [clojure.java.io :as io]
    [potatoclient.config :as config]
    [potatoclient.i18n :as i18n]
    [potatoclient.logging :as logging]
    [potatoclient.state :as state]
    [potatoclient.state.server.core :as state-server]
    [potatoclient.ui.menu-bar :as menu-bar]
    [seesaw.action :as action]
    [seesaw.border :as border]
    [seesaw.core :as seesaw]
    [seesaw.mig :as mig]
    [seesaw.timer :as timer])
  (:import (javax.swing JFrame JLabel JPanel SwingConstants)
           (java.net InetAddress)
           (java.time Duration Instant)
           (java.time.format DateTimeFormatter)
           (java.time ZoneId)))

(defn- format-duration
  "Format duration in seconds to MM:SS format."
  [seconds]
  (let [mins (quot seconds 60)
        secs (mod seconds 60)]
    (format "%02d:%02d" mins secs))) 
 (m/=> format-duration [:=> [:cat :int] :string])

(defn- ping-host
  "Ping a host and return latency in ms or nil if failed."
  [host timeout-ms]
  (try
    (let [address (InetAddress/getByName host)
          start (System/currentTimeMillis)
          reachable? (.isReachable address timeout-ms)
          end (System/currentTimeMillis)]
      (when reachable?
        (- end start)))
    (catch Exception e
      (logging/log-debug {:msg (str "Ping failed: " (.getMessage e))})
      nil))) 
 (m/=> ping-host [:=> [:cat :string :int] [:maybe :int]])

(defn- create-content-panel
  "Create the main content panel for the connection frame."
  [domain]
  (let [title-label (seesaw/label
                      :text (str (i18n/tr :connecting-to) " " domain)
                      :font {:size 18 :style :bold}
                      :halign :center)
        status-label (seesaw/label
                       :id :status-label
                       :text (str "<html><center>"
                                 (i18n/tr :connection-attempting) "<br>"
                                 "<span style='color:gray;font-size:10px;'>"
                                 (i18n/tr :connection-waiting-ping)
                                 "</span></center></html>")
                       :font {:size 14}
                       :halign :center)
        ping-label (seesaw/label
                     :id :ping-label
                     :text ""
                     :font {:size 12}
                     :halign :center
                     :foreground :gray
                     :visible? false)  ; Hidden initially, shown when ping succeeds
        ws-status-label (seesaw/label
                          :id :ws-status-label
                          :text (i18n/tr :connection-ws-not-connected)
                          :font {:size 12}
                          :halign :center
                          :foreground :gray)
        attempts-label (seesaw/label
                         :id :attempts-label
                         :text (str (i18n/tr :connection-attempts) ": 0")
                         :font {:size 12}
                         :halign :center)
        ws-attempts-label (seesaw/label
                            :id :ws-attempts-label
                            :text (i18n/tr :connection-ws-attempts [0])
                            :font {:size 12}
                            :halign :center)
        time-label (seesaw/label
                     :id :time-label
                     :text (str (i18n/tr :connection-time) ": 00:00")
                     :font {:size 12}
                     :halign :center)
        progress-bar (seesaw/progress-bar
                       :id :progress-bar
                       :indeterminate? true
                       :preferred-size [300 :by 20])]
    (mig/mig-panel
      :constraints ["wrap 1, insets 30, gap 15, align center" "[grow, fill, align center]" "[]"]
      :items [[title-label ""]
              [status-label "gaptop 20"]
              [progress-bar "gaptop 15"]
              [ping-label "gaptop 5"]
              [ws-status-label "gaptop 15"]
              [attempts-label "gaptop 10"]
              [ws-attempts-label "gaptop 5"]
              [time-label "gaptop 5"]]))) 
 (m/=> create-content-panel [:=> [:cat :string] [:fn {:error/message "must be a Swing panel"} (partial instance? JPanel)]])

(defn- stop-timers!
  "Stop all timers and clean up resources."
  [ping-timer update-timer]
  (when ping-timer (.stop ping-timer))
  (when update-timer (.stop update-timer))
  nil) 
 (m/=> stop-timers! [:=> [:cat [:maybe any?] [:maybe any?]] :nil])

(defn- update-ping-success-ui!
  "Update UI elements to show successful ping."
  [{:keys [ping-label status-label]} latency]
  (seesaw/invoke-later
    (seesaw/config! ping-label
                    :text (i18n/tr :connection-ping-success [latency])
                    :foreground :green
                    :visible? true)
    (seesaw/config! status-label
                    :text (str "<html><center>"
                              "<span style='color:green;'>"
                              (i18n/tr :connection-ping-success-connecting)
                              "</span><br>"
                              "<span style='color:gray;font-size:10px;'>"
                              (i18n/tr :connection-ws-initializing)
                              "</span></center></html>")))
  nil) 
 (m/=> update-ping-success-ui! [:=> [:cat any? :int] :nil])

(defn- update-ping-failure-ui!
  "Update UI elements to show failed ping."
  [{:keys [ping-label status-label]}]
  (seesaw/invoke-later
    (seesaw/config! ping-label
                    :text ""  ; Hide ping label on failure
                    :visible? false)
    (seesaw/config! status-label
                    :text (str "<html><center>"
                              (i18n/tr :connection-attempting) "<br>"
                              "<span style='color:red;font-size:10px;'>"
                              (i18n/tr :connection-ping-failed)
                              "</span></center></html>")))
  nil) 
 (m/=> update-ping-failure-ui! [:=> [:cat any?] :nil])

(defn- get-ws-status-text
  "Get localized status text based on connection state."
  [status failures]
  (case status
    :connected (i18n/tr :connection-ws-connected)
    :connecting (if (> failures 0)
                  (i18n/tr :connection-ws-connecting-retry [failures])
                  (i18n/tr :connection-ws-connecting))
    :disconnected (i18n/tr :connection-ws-disconnected)
    (i18n/tr :connection-ws-unknown)))

(defn- get-status-color
  "Get color for status display."
  [status]
  (case status
    :connected :green
    :connecting :orange
    :red))

(defn- update-ws-status-ui!
  "Update WebSocket status in UI."
  [ui-refs]
  (when (state-server/get-manager)
    (let [stats (state-server/get-connection-stats)
          status (:status stats :disconnected)
          attempts (:attempts stats 0)
          failures (:consecutive-failures stats 0)
          status-text (get-ws-status-text status failures)
          color (get-status-color status)]
      (seesaw/invoke-later
        (seesaw/config! (:ws-status-label @ui-refs)
                        :text status-text
                        :foreground color)
        (seesaw/config! (:ws-attempts-label @ui-refs)
                        :text (i18n/tr :connection-ws-attempts [attempts]))))))

(defn- handle-connection-success!
  "Handle successful state connection."
  [ui-refs dialog callback]
  (logging/log-info {:msg "State connection established successfully"})
  (seesaw/invoke-later
    (seesaw/config! (:status-label @ui-refs)
                    :text (str "<html><center>"
                              "<span style='color:green;font-weight:bold;'>"
                              (i18n/tr :connection-state-connected)
                              "</span></center></html>"))
    (seesaw/config! (:ws-status-label @ui-refs)
                    :text (i18n/tr :connection-ws-connected)
                    :foreground :green)
    ;; Brief pause to show success
    (Thread/sleep 500)
    (when (.isDisplayable dialog)
      (seesaw/dispose! dialog))
    (callback :connected)))

(defn- log-long-running-attempt
  "Log message for long-running connection attempts."
  [start-time]
  (let [elapsed (- (System/currentTimeMillis) start-time)]
    (when (and (= 0 (mod elapsed 30000))  ; Every 30 seconds
               (> elapsed 0))
      (logging/log-info {:msg (str "Still attempting connection after " 
                                   (/ elapsed 1000) " seconds. Attempts: " 
                                   (:attempts (state-server/get-connection-stats)))}))))

(defn- monitor-state-connection!
  "Monitor state WebSocket connection with UI updates.
   Returns a future that can be cancelled."
  [ui-refs callback dialog]
  (future
    (try
      (let [start-time (System/currentTimeMillis)
            monitoring? (atom true)]
        
        ;; Store monitoring atom for cancellation
        (swap! ui-refs assoc :monitoring? monitoring?)
        
        ;; Start update loop
        (while @monitoring?
          (update-ws-status-ui! ui-refs)
          
          ;; Check if connected
          (when (or (state-server/connected?)
                   (some? (:server-state @state/app-state)))
            (reset! monitoring? false)
            (handle-connection-success! ui-refs dialog callback))
          
          ;; Log long-running connection attempts
          (log-long-running-attempt start-time)
          
          (Thread/sleep 500)))
      (catch InterruptedException e
        (logging/log-debug {:msg "State connection monitoring interrupted"}))
      (catch Exception e
        (logging/log-error {:msg "Error monitoring state connection" :error e})
        (seesaw/invoke-later
          (seesaw/config! (:status-label @ui-refs)
                         :text (i18n/tr :connection-state-error)
                         :foreground :red)
          (Thread/sleep 2000)
          (when (.isDisplayable dialog)
            (seesaw/dispose! dialog))
          (callback :error))))))
 (m/=> wait-for-state-connection! [:=> [:cat :int] :boolean])

(defn- handle-successful-connection!
  "Handle successful connection establishment."
  [domain latency dialog ping-timer update-timer callback ui-refs]
  (stop-timers! ping-timer update-timer)
  (state/set-connection-latency! latency)
  (state/set-connected! true)
  (logging/log-info {:msg (str "Successfully connected to " domain " with latency " latency "ms")})
  
  ;; Update UI to show we're connecting to state socket
  (seesaw/invoke-later
    (seesaw/config! (:status-label @ui-refs)
                    :text (str "<html><center>"
                              "<span style='color:green;'>"
                              (i18n/tr :connection-ping-success-now-connecting)
                              "</span><br>"
                              "<span style='color:gray;font-size:10px;'>"
                              (i18n/tr :connection-ws-initializing)
                              "</span></center></html>"))
    (seesaw/config! (:ws-status-label @ui-refs)
                    :text (i18n/tr :connection-ws-initializing)
                    :foreground :orange))
  
  ;; Initialize and start state ingress
  (logging/log-info {:msg (str "Initializing state ingress for domain: " domain)})
  (state-server/initialize! {:domain domain
                             :throttle-ms 100
                             :timeout-ms 2000})
  (state-server/start!)
  
  ;; Monitor state connection with persistent retry
  (let [monitor-future (monitor-state-connection! ui-refs callback dialog)]
    (swap! ui-refs assoc :monitor-future monitor-future))
  nil) 
 (m/=> handle-successful-connection! [:=> [:cat :string :int any? any? any? :ifn any?] :nil])

(defn- create-ping-monitor
  "Create ping monitoring function with state management."
  [domain state ui-refs timers callback update-ui!]
  (let [ping-in-progress? (atom false)]
    (fn do-ping []
      (when-not @ping-in-progress?
        (reset! ping-in-progress? true)
        (future
          (try
            (let [latency (ping-host domain 2000)] ; 2 second timeout
              (swap! (:attempts state) inc)
              (seesaw/invoke-later (update-ui!))

              (if latency
                (do
                  (swap! (:successful-pings state) inc)
                  (update-ping-success-ui! @ui-refs latency)
                  (handle-successful-connection!
                    domain latency
                    @(:dialog timers)
                    @(:ping-timer timers)
                    @(:update-timer timers)
                    callback
                    ui-refs))
                (do
                  (reset! (:successful-pings state) 0)
                  (update-ping-failure-ui! @ui-refs))))
            (catch Exception e
              (logging/log-error {:msg (str "Error in ping monitor: " (.getMessage e))
                                  :error e})
              (reset! (:successful-pings state) 0)
              (update-ping-failure-ui! ui-refs))
            (finally
              (reset! ping-in-progress? false)))))))) 
 (m/=> create-ping-monitor [:=> [:cat :string [:map [:attempts any?] [:successful-pings any?]] any? [:map [:dialog any?] [:ping-timer any?] [:update-timer any?]] :ifn :ifn] :ifn])

(defn- create-update-ui-fn
  "Create UI update function for elapsed time and attempts."
  [start-time state ui-refs]
  (fn []
    (let [elapsed (.getSeconds (Duration/between start-time (Instant/now)))]
      (seesaw/config! (:attempts-label ui-refs)
                      :text (str (i18n/tr :connection-attempts) ": " @(:attempts state)))
      (seesaw/config! (:time-label ui-refs)
                      :text (str (i18n/tr :connection-time) ": " (format-duration elapsed)))))) 
 (m/=> create-update-ui-fn [:=> [:cat any? [:map [:attempts any?]] [:map [:attempts-label any?] [:time-label any?]]] :ifn])

(defn- create-dialog
  "Create the connection dialog window."
  [parent content callback timers ui-refs]
  (let [cancel-action (action/action
                        :name (i18n/tr :startup-button-cancel)
                        :handler (fn [_]
                                   (stop-timers! @(:ping-timer timers) @(:update-timer timers))
                                   ;; Stop monitoring if active
                                   (when-let [monitoring? (:monitoring? @ui-refs)]
                                     (reset! monitoring? false))
                                   (when-let [monitor-future (:monitor-future @ui-refs)]
                                     (future-cancel monitor-future))
                                   ;; Stop state server if it was started
                                   (when (state-server/running?)
                                     (state-server/stop!))
                                   (seesaw/dispose! @(:dialog timers))
                                   (callback :cancel)))

        cancel-button (seesaw/button
                        :action cancel-action
                        :font {:size 14}
                        :preferred-size [120 :by 70])

        buttons-panel (seesaw/flow-panel
                        :align :center
                        :items [cancel-button])

        main-panel (seesaw/border-panel
                     :center content
                     :south buttons-panel
                     :border 20)

        dialog (seesaw/frame
                 :title (i18n/tr :connection-title)
                 :icon (io/resource "main.png")
                 :resizable? false
                 :content main-panel
                 :on-close :nothing)]

    (seesaw/config! dialog
                    :menubar (menu-bar/create-menubar
                               {:reload-fn (fn [_]
                                             (stop-timers! @(:ping-timer timers) @(:update-timer timers))
                                             (seesaw/dispose! dialog)
                                             (callback :reload))
                                :include-theme? true
                                :include-language? true
                                :include-help? false
                                :include-stream-buttons? false}))

    (seesaw/listen dialog :window-closing
                   (fn [_]
                     (stop-timers! @(:ping-timer timers) @(:update-timer timers))
                     ;; Stop monitoring if active
                     (when-let [monitoring? (:monitoring? @ui-refs)]
                       (reset! monitoring? false))
                     (when-let [monitor-future (:monitor-future @ui-refs)]
                       (future-cancel monitor-future))
                     ;; Stop state server if it was started
                     (when (state-server/running?)
                       (state-server/stop!))
                     (seesaw/dispose! dialog)
                     (callback :cancel)))

    (doto dialog
      seesaw/pack!
      (.setLocationRelativeTo nil)) ; nil centers on screen

    dialog)) 
 (m/=> create-dialog [:=> [:cat [:maybe [:fn {:error/message "must be a JFrame"} (partial instance? JFrame)]] any? :ifn [:map [:ping-timer any?] [:update-timer any?] [:dialog any?]] any?] [:fn {:error/message "must be a JFrame"} (partial instance? JFrame)]])

(defn show
  "Show the connection frame with ping monitoring."
  [parent callback]
  (let [domain (state/get-domain)
        content (create-content-panel domain)

        ; State atoms
        state {:attempts (atom 0)
               :successful-pings (atom 0)}

        ; Timer atoms
        timers {:dialog (atom nil)
                :ping-timer (atom nil)
                :update-timer (atom nil)}

        ; Cache UI element references (using atom for mutable fields)
        ui-refs (atom {:ping-label (seesaw/select content [:#ping-label])
                       :status-label (seesaw/select content [:#status-label])
                       :ws-status-label (seesaw/select content [:#ws-status-label])
                       :attempts-label (seesaw/select content [:#attempts-label])
                       :ws-attempts-label (seesaw/select content [:#ws-attempts-label])
                       :time-label (seesaw/select content [:#time-label])})

        start-time (Instant/now)
        update-ui! (create-update-ui-fn start-time state @ui-refs)
        do-ping! (create-ping-monitor domain state ui-refs timers callback update-ui!)

        dialog (create-dialog parent content callback timers ui-refs)]

    ; Store dialog reference
    (reset! (:dialog timers) dialog)

    ; Create and start timers
    (reset! (:ping-timer timers)
            (timer/timer (fn [_] (do-ping!))
                         :delay 1000
                         :initial-delay 100))

    (reset! (:update-timer timers)
            (timer/timer (fn [_] (update-ui!))
                         :delay 1000))

    (logging/log-info {:id :user/show-connection-frame
                       :data {:domain domain}
                       :msg (str "Showing connection frame for domain: " domain)})

    (seesaw/show! dialog)
    nil)) 
 (m/=> show [:=> [:cat [:maybe [:fn {:error/message "must be a JFrame"} (partial instance? JFrame)]] :ifn] :nil])