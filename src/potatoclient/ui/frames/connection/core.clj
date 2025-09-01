(ns potatoclient.ui.frames.connection.core
  "Connection frame that displays ping status and connection progress."
  (:require
    [clojure.java.io :as io]
    [potatoclient.config :as config]
    [potatoclient.i18n :as i18n]
    [potatoclient.logging :as logging]
    [potatoclient.state :as state]
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
  {:malli/schema [:=> [:cat :int] :string]}
  [seconds]
  (let [mins (quot seconds 60)
        secs (mod seconds 60)]
    (format "%02d:%02d" mins secs)))

(defn- ping-host
  "Ping a host and return latency in ms or nil if failed."
  {:malli/schema [:=> [:cat :string :int] [:maybe :int]]}
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

(defn- create-content-panel
  "Create the main content panel for the connection frame."
  {:malli/schema [:=> [:cat :string] [:fn {:error/message "must be a Swing panel"} (partial instance? JPanel)]]}
  [domain]
  (let [title-label (seesaw/label 
                      :text (str (i18n/tr :connecting-to) " " domain)
                      :font {:size 18 :style :bold}
                      :halign :center)
        status-label (seesaw/label
                       :id :status-label
                       :text (i18n/tr :connection-attempting)
                       :font {:size 14}
                       :halign :center)
        ping-label (seesaw/label
                     :id :ping-label
                     :text (i18n/tr :connection-waiting-ping)
                     :font {:size 12}
                     :halign :center
                     :foreground :gray)
        attempts-label (seesaw/label
                         :id :attempts-label
                         :text (str (i18n/tr :connection-attempts) ": 0")
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
              [progress-bar "gaptop 10"]
              [ping-label "gaptop 20"]
              [attempts-label "gaptop 10"]
              [time-label "gaptop 5"]])))

(defn- stop-timers!
  "Stop all timers and clean up resources."
  {:malli/schema [:=> [:cat [:maybe any?] [:maybe any?]] :nil]}
  [ping-timer update-timer]
  (when ping-timer (.stop ping-timer))
  (when update-timer (.stop update-timer))
  nil)

(defn- update-ping-success-ui!
  "Update UI elements to show successful ping."
  {:malli/schema [:=> [:cat [:map [:ping-label any?] [:status-label any?]] :int] :nil]}
  [{:keys [ping-label status-label]} latency]
  (seesaw/invoke-later
    (seesaw/config! ping-label 
                   :text (str (i18n/tr :connection-ping) ": " latency "ms")
                   :foreground :green)
    (seesaw/config! status-label 
                   :text (i18n/tr :connection-successful)))
  nil)

(defn- update-ping-failure-ui!
  "Update UI elements to show failed ping."
  {:malli/schema [:=> [:cat [:map [:ping-label any?] [:status-label any?]]] :nil]}
  [{:keys [ping-label status-label]}]
  (seesaw/invoke-later
    (seesaw/config! ping-label 
                   :text (i18n/tr :connection-ping-failed)
                   :foreground :red)
    (seesaw/config! status-label 
                   :text (i18n/tr :connection-attempting)))
  nil)

(defn- handle-successful-connection!
  "Handle successful connection establishment."
  {:malli/schema [:=> [:cat :string :int any? any? any? :ifn] :nil]}
  [domain latency dialog ping-timer update-timer callback]
  (stop-timers! ping-timer update-timer)
  (state/set-connection-latency! latency)
  (state/set-connected! true)
  (logging/log-info {:msg (str "Successfully connected to " domain " with latency " latency "ms")})
  (Thread/sleep 500) ; Brief pause to show success
  (seesaw/invoke-later
    (seesaw/dispose! dialog)
    (callback :connected))
  nil)

(defn- create-ping-monitor
  "Create ping monitoring function with state management."
  {:malli/schema [:=> [:cat :string [:map [:attempts any?] [:successful-pings any?]] 
                       [:map [:ping-label any?] [:status-label any?] [:attempts-label any?] [:time-label any?]]
                       [:map [:dialog any?] [:ping-timer any?] [:update-timer any?]]
                       :ifn :ifn] :ifn]}
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
                  (update-ping-success-ui! ui-refs latency)
                  (handle-successful-connection! 
                    domain latency 
                    @(:dialog timers)
                    @(:ping-timer timers)
                    @(:update-timer timers)
                    callback))
                (do
                  (reset! (:successful-pings state) 0)
                  (update-ping-failure-ui! ui-refs))))
            (finally
              (reset! ping-in-progress? false))))))))

(defn- create-update-ui-fn
  "Create UI update function for elapsed time and attempts."
  {:malli/schema [:=> [:cat any? [:map [:attempts any?]] 
                       [:map [:attempts-label any?] [:time-label any?]]] :ifn]}
  [start-time state ui-refs]
  (fn []
    (let [elapsed (.getSeconds (Duration/between start-time (Instant/now)))]
      (seesaw/config! (:attempts-label ui-refs) 
                     :text (str (i18n/tr :connection-attempts) ": " @(:attempts state)))
      (seesaw/config! (:time-label ui-refs) 
                     :text (str (i18n/tr :connection-time) ": " (format-duration elapsed))))))

(defn- create-dialog
  "Create the connection dialog window."
  {:malli/schema [:=> [:cat [:maybe [:fn {:error/message "must be a JFrame"} (partial instance? JFrame)]]
                       any? :ifn [:map [:ping-timer any?] [:update-timer any?] [:dialog any?]]]
                  [:fn {:error/message "must be a JFrame"} (partial instance? JFrame)]]}
  [parent content callback timers]
  (let [cancel-action (action/action
                        :name (i18n/tr :startup-button-cancel)
                        :handler (fn [_]
                                  (stop-timers! @(:ping-timer timers) @(:update-timer timers))
                                  (seesaw/dispose! @(:dialog timers))
                                  (callback :cancel)))
        
        cancel-button (seesaw/button
                        :action cancel-action
                        :font {:size 14}
                        :preferred-size [120 :by 40])
        
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
                     (seesaw/dispose! dialog)
                     (callback :cancel)))
    
    (doto dialog
      seesaw/pack!
      (.setLocationRelativeTo nil))  ; nil centers on screen
    
    dialog))

(defn show
  "Show the connection frame with ping monitoring."
  {:malli/schema [:=> [:cat [:maybe [:fn {:error/message "must be a JFrame"} (partial instance? JFrame)]] :ifn] :nil]}
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
        
        ; Cache UI element references
        ui-refs {:ping-label (seesaw/select content [:#ping-label])
                 :status-label (seesaw/select content [:#status-label])
                 :attempts-label (seesaw/select content [:#attempts-label])
                 :time-label (seesaw/select content [:#time-label])}
        
        start-time (Instant/now)
        update-ui! (create-update-ui-fn start-time state ui-refs)
        do-ping! (create-ping-monitor domain state ui-refs timers callback update-ui!)
        
        dialog (create-dialog parent content callback timers)]
    
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