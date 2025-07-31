(ns potatoclient.ui.control-panel
  "Control panel UI component for managing video streams.
  
  Provides the main control interface for connecting/disconnecting
  video streams and managing application settings."
  (:require [com.fulcrologic.guardrails.malli.core :refer [=> >defn >defn-]]
            [potatoclient.i18n :as i18n]
            [potatoclient.ipc :as ipc]
            [potatoclient.state :as state]
            [potatoclient.theme :as theme]
            [potatoclient.transit.app-db :as app-db]
            [seesaw.core :as seesaw]
            [seesaw.bind :as bind]
            [seesaw.border :as border]
            [seesaw.mig :as mig])
  (:import (javax.swing JPanel)))

;; UI styling constants
(def ^:private panel-border-width 10)
(def ^:private header-font {:name "Arial" :style :bold :size 16})
(def ^:private label-font {:name "Arial" :size 12})
(def ^:private status-font {:name "Arial" :style :italic :size 11})

(>defn- create-stream-status-panel
  "Create a status panel for a single stream."
  [stream-key]
  [:potatoclient.specs/stream-key => [:fn {:error/message "must be a Swing panel"}
                                      #(instance? JPanel %)]]
  (let [stream-name (case stream-key
                      :heat (i18n/tr :stream-thermal)
                      :day (i18n/tr :stream-day))
        status-label (seesaw/label :id (keyword (str (name stream-key) "-status"))
                                   :text (i18n/tr :status-disconnected)
                                   :font status-font
                                   :foreground :red)
        info-label (seesaw/label :id (keyword (str (name stream-key) "-info"))
                                 :text ""
                                 :font status-font)]

    ;; Bind status labels to app-db
    (bind/bind app-db/app-db
               (bind/transform (fn [db]
                                 (let [process-key (case stream-key
                                                     :heat :heat-video
                                                     :day :day-video)
                                       process (get-in db [:app-state :processes process-key])
                                       running? (= :running (:status process))]
                                   {:connected? running?
                                    :pid (:pid process)})))
               (bind/tee
                 (bind/bind (bind/transform #(:connected? %))
                            (bind/b-do [connected?]
                                       (seesaw/config! status-label
                                                       :text (if connected?
                                                               (i18n/tr :status-connected)
                                                               (i18n/tr :status-disconnected))
                                                       :foreground (if connected?
                                                                     (java.awt.Color/GREEN)
                                                                     (java.awt.Color/RED)))))
                 (bind/bind (bind/transform #(if (:connected? %)
                                               (str "PID: " (:pid %))
                                               ""))
                            (bind/property info-label :text))))

    (mig/mig-panel
      :constraints ["wrap 2, insets 5"
                    "[right]rel[grow,fill]"]
      :items [[(seesaw/label :text stream-name
                             :font label-font)]
              [status-label]
              [""]
              [info-label "span 2"]])))

(>defn- create-connection-info-panel
  "Create panel showing current connection information."
  []
  [=> [:fn {:error/message "must be a Swing panel"}
       #(instance? JPanel %)]]
  (let [domain-label (seesaw/label :id :domain-info
                                   :text ""
                                   :font label-font)]

    ;; Bind domain label to connection URL
    (bind/bind app-db/app-db
               (bind/transform #(or (get-in % [:app-state :connection :url]) ""))
               (bind/transform #(if (empty? %)
                                  (i18n/tr :no-server-configured)
                                  (str (i18n/tr :server-domain) ": " %)))
               (bind/property domain-label :text))

    (seesaw/border-panel
      :border (border/line-border :title (i18n/tr :connection-info))
      :center domain-label)))

(>defn- create-stream-controls-panel
  "Create panel with stream control buttons."
  []
  [=> [:fn {:error/message "must be a Swing panel"}
       #(instance? JPanel %)]]
  (let [heat-toggle (seesaw/toggle :id :heat-toggle
                                   :text (i18n/tr :stream-thermal)
                                   :icon (theme/key->icon :heat))
        day-toggle (seesaw/toggle :id :day-toggle
                                  :text (i18n/tr :stream-day)
                                  :icon (theme/key->icon :day))

        ;; Wire up actions
        setup-toggle! (fn [toggle stream-key endpoint]
                        (seesaw/listen toggle :action
                                       (fn [_]
                                         (if (seesaw/config toggle :selected?)
                                           (ipc/start-stream stream-key endpoint)
                                           (ipc/stop-stream stream-key))))
                        ;; Bind toggle state to app-db
                        (bind/bind app-db/app-db
                                   (bind/transform (fn [db]
                                                     (let [process-key (case stream-key
                                                                         :heat :heat-video
                                                                         :day :day-video)]
                                                       (= :running (get-in db [:app-state :processes process-key :status])))))
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

(>defn- create-statistics-panel
  "Create panel showing stream statistics."
  []
  [=> [:fn {:error/message "must be a Swing panel"}
       #(instance? JPanel %)]]
  (let [stats-label (seesaw/text :id :stats-info
                                 :text (i18n/tr :no-statistics)
                                 :font status-font
                                 :multi-line? true
                                 :editable? false
                                 :background nil)]

    ;; This would be updated with real statistics from the streams
    ;; For now, just a placeholder
    (seesaw/border-panel
      :border (border/line-border :title (i18n/tr :statistics))
      :center (seesaw/scrollable stats-label
                                 :border 0))))

(>defn create
  "Create the control panel UI component.
  
  Returns a panel containing all control elements for managing
  video streams and application settings."
  []
  [=> [:fn {:error/message "must be a Swing panel"}
       #(instance? JPanel %)]]
  (let [header (seesaw/label :text (i18n/tr :control-panel-title)
                             :font header-font
                             :halign :center)
        connection-panel (create-connection-info-panel)
        controls-panel (create-stream-controls-panel)
        heat-status (create-stream-status-panel :heat)
        day-status (create-stream-status-panel :day)
        stats-panel (create-statistics-panel)]

    (seesaw/border-panel
      :border panel-border-width ; Creates empty border with specified width
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

