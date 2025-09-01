(ns potatoclient.ui.control-panel
  "Control panel UI component for managing video streams.

  Provides the main control interface for connecting/disconnecting
  video streams and managing application settings."
  (:require [potatoclient.i18n :as i18n]
            [potatoclient.state :as state]
            [potatoclient.theme :as theme]
            [seesaw.bind :as bind]
            [seesaw.border :as border]
            [seesaw.core :as seesaw]
            [seesaw.mig :as mig])
  (:import (java.awt Color)
           (javax.swing JPanel)))

;; UI styling constants

(defn- create-stream-status-panel
  "Create a status panel for a single stream." {:malli/schema [:=> [:cat :potatoclient.ui-specs/stream-key] [:fn {:error/message "must be a Swing panel"} (fn* [p1__3768#] (instance? JPanel p1__3768#))]]}
  [stream-key]
  (let [stream-name (case stream-key
                      :heat (i18n/tr :stream-thermal)
                      :day (i18n/tr :stream-day))
        status-label (seesaw/label :id (keyword (str (name stream-key) "-status"))
                                   :text (i18n/tr :status-disconnected)
                                   :foreground :red)
        info-label (seesaw/label :id (keyword (str (name stream-key) "-info"))
                                 :text "")]

    ;; Bind status labels to app-state
    (bind/bind state/app-state
               (bind/transform (fn [s]
                                 (let [process-key (case stream-key
                                                     :heat :heat-video
                                                     :day :day-video)
                                       process (get-in s [:processes process-key])
                                       running? (= :running (:status process))]
                                   {:connected? running?
                                    :pid (:pid process)})))
               (bind/tee
                 (bind/bind (bind/transform :connected?)
                            (bind/b-do [connected?]
                                       (seesaw/config! status-label
                                                       :text (if connected?
                                                               (i18n/tr :status-connected)
                                                               (i18n/tr :status-disconnected))
                                                       :foreground (if connected?
                                                                     Color/GREEN
                                                                     Color/RED))))
                 (bind/bind (bind/transform #(if (:connected? %)
                                               (str "PID: " (:pid %))
                                               ""))
                            (bind/property info-label :text))))

    (mig/mig-panel
      :constraints ["wrap 2, insets 5"
                    "[right]rel[grow,fill]"]
      :items [[(seesaw/label :text stream-name)]
              [status-label]
              [""]
              [info-label "span 2"]])))

(defn- create-connection-info-panel
  "Create panel showing current connection information." {:malli/schema [:=> [:cat] [:fn {:error/message "must be a Swing panel"} (fn* [p1__3770#] (instance? JPanel p1__3770#))]]}
  []
  (let [domain-label (seesaw/label :id :domain-info
                                   :text "")]

    ;; Bind domain label to connection URL
    (bind/bind state/app-state
               (bind/transform #(or (get-in % [:connection :url]) ""))
               (bind/transform #(if (empty? %)
                                  (i18n/tr :no-server-configured)
                                  (str (i18n/tr :server-domain) ": " %)))
               (bind/property domain-label :text))

    (seesaw/border-panel
      :border (border/line-border :title (i18n/tr :connection-info))
      :center domain-label)))

(defn- create-stream-controls-panel
  "Create panel with stream control buttons." {:malli/schema [:=> [:cat] [:fn {:error/message "must be a Swing panel"} (fn* [p1__3772#] (instance? JPanel p1__3772#))]]}
  []
  (let [heat-toggle (seesaw/toggle :id :heat-toggle
                                   :text (i18n/tr :stream-thermal)
                                   :icon (theme/key->icon :heat))
        day-toggle (seesaw/toggle :id :day-toggle
                                  :text (i18n/tr :stream-day)
                                  :icon (theme/key->icon :day))

        ;; Wire up actions
        setup-toggle! (fn [toggle stream-key _]
                        (seesaw/listen toggle :action
                                       (fn [_]
                                         ;; Stream control is noop for now
                                         nil))
                        ;; Bind toggle state to app-state
                        (bind/bind state/app-state
                                   (bind/transform (fn [s]
                                                     (let [process-key (case stream-key
                                                                         :heat :heat-video
                                                                         :day :day-video)]
                                                       (= :running (get-in s [:processes process-key :status])))))
                                   (bind/property toggle :selected?)))]

    ;; Set up toggles
    (setup-toggle! heat-toggle :heat "/ws/ws_rec_video_heat")
    (setup-toggle! day-toggle :day "/ws/ws_rec_video_day")

    (seesaw/border-panel
      :border (border/line-border :title (i18n/tr :stream-controls))
      :center (seesaw/flow-panel
                :align :center
                :hgap 10
                :items [heat-toggle day-toggle]))))

(defn- create-statistics-panel
  "Create panel showing stream statistics." {:malli/schema [:=> [:cat] [:fn {:error/message "must be a Swing panel"} (fn* [p1__3774#] (instance? JPanel p1__3774#))]]}
  []
  (let [stats-label (seesaw/text :id :stats-info
                                 :text (i18n/tr :no-statistics)
                                 :multi-line? true
                                 :editable? false
                                 :background nil)]

    ;; This would be updated with real statistics from the streams
    ;; For now, just a placeholder
    (seesaw/border-panel
      :border (border/line-border :title (i18n/tr :statistics))
      :center (seesaw/scrollable stats-label
                                 :border 0))))

(defn create
  "Create the control panel UI component.

  Returns a panel containing all control elements for managing
  video streams and application settings." {:malli/schema [:=> [:cat] [:fn {:error/message "must be a Swing panel"} (fn* [p1__3776#] (instance? JPanel p1__3776#))]]}
  []
  (let [header (seesaw/label :text (i18n/tr :control-panel-title)
                             :halign :center)
        connection-panel (create-connection-info-panel)
        controls-panel (create-stream-controls-panel)
        heat-status (create-stream-status-panel :heat)
        day-status (create-stream-status-panel :day)
        stats-panel (create-statistics-panel)]

    (seesaw/border-panel
      :border 10 ; Creates empty border with specified width
      :north header
      :center (mig/mig-panel
                :constraints ["wrap 1, fill, insets 10"
                              "[grow, fill]"
                              "[]"]
                :items [[connection-panel "growx"]
                        [controls-panel "growx, gaptop 10"]
                        [(seesaw/separator) "growx, gaptop 10, gapbottom 10"]
                        [heat-status "growx"]
                        [day-status "growx, gaptop 5"]
                        [(seesaw/separator) "growx, gaptop 10, gapbottom 10"]
                        [stats-panel "grow, push"]]))))

