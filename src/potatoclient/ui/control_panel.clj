(ns potatoclient.ui.control-panel
  "Control panel UI component for managing video streams.
  
  Provides the main control interface for connecting/disconnecting
  video streams and managing application settings."
  (:require [com.fulcrologic.guardrails.malli.core :refer [>defn >defn- => ?]]
            [potatoclient.state :as state]
            [potatoclient.i18n :as i18n]
            [potatoclient.theme :as theme]
            [potatoclient.config :as config]
            [potatoclient.specs :as specs]
            [potatoclient.ipc :as ipc]
            [seesaw.core :as seesaw]
            [seesaw.action :as action]
            [seesaw.border :as border]
            [seesaw.mig :as mig]
            [seesaw.bind :as bind]
            [malli.core :as m]))

;; UI styling constants
(def ^:private panel-border-width 10)
(def ^:private section-gap 15)
(def ^:private header-font {:name "Arial" :style :bold :size 16})
(def ^:private label-font {:name "Arial" :size 12})
(def ^:private status-font {:name "Arial" :style :italic :size 11})

(>defn- create-stream-status-panel
  "Create a status panel for a single stream."
  [stream-key]
  [:potatoclient.specs/stream-key => [:fn {:error/message "must be a Swing panel"}
                                      #(instance? javax.swing.JPanel %)]]
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

    ;; Update status when stream state changes
    (add-watch state/app-state
               (keyword (str "control-panel-" (name stream-key)))
               (fn [_ _ old-state new-state]
                 (let [stream (get new-state stream-key)]
                   (seesaw/invoke-later
                     (if stream
                       (do
                         (seesaw/config! status-label
                                         :text (i18n/tr :status-connected)
                                         :foreground :green)
                         (seesaw/config! info-label
                                         :text (str "PID: " (:pid stream))))
                       (do
                         (seesaw/config! status-label
                                         :text (i18n/tr :status-disconnected)
                                         :foreground :red)
                         (seesaw/config! info-label :text "")))))))

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
       #(instance? javax.swing.JPanel %)]]
  (let [domain-label (seesaw/label :id :domain-info
                                   :text ""
                                   :font label-font)
        update-domain! (fn []
                         (let [domain (state/get-domain)]
                           (seesaw/config! domain-label
                                           :text (if domain
                                                   (str (i18n/tr :server-domain) ": " domain)
                                                   (i18n/tr :no-server-configured)))))]

    ;; Initial update
    (update-domain!)

    ;; Watch for domain changes
    (add-watch state/app-state
               :control-panel-domain
               (fn [_ _ old-state new-state]
                 (when (not= (:domain old-state) (:domain new-state))
                   (seesaw/invoke-later (update-domain!)))))

    (seesaw/border-panel
      :border (i18n/tr :connection-info)
      :center domain-label)))

(>defn- create-stream-controls-panel
  "Create panel with stream control buttons."
  []
  [=> [:fn {:error/message "must be a Swing panel"}
       #(instance? javax.swing.JPanel %)]]
  (let [heat-toggle (seesaw/toggle :id :heat-toggle
                                   :text (i18n/tr :stream-thermal)
                                   :icon (theme/key->icon :heat))
        day-toggle (seesaw/toggle :id :day-toggle
                                  :text (i18n/tr :stream-day)
                                  :icon (theme/key->icon :day))

        ;; Helper to update toggle state
        update-toggle! (fn [toggle stream-key]
                         (let [connected? (some? (state/get-stream stream-key))]
                           (seesaw/config! toggle :selected? connected?)))

        ;; Wire up actions
        setup-toggle! (fn [toggle stream-key endpoint]
                        (seesaw/listen toggle :action
                                       (fn [e]
                                         (if (seesaw/config toggle :selected?)
                                           (ipc/start-stream stream-key endpoint)
                                           (ipc/stop-stream stream-key))))
                        ;; Watch for external state changes
                        (add-watch state/app-state
                                   (keyword (str "toggle-" (name stream-key)))
                                   (fn [_ _ _ _]
                                     (seesaw/invoke-later
                                       (update-toggle! toggle stream-key)))))]

    ;; Set up toggles
    (setup-toggle! heat-toggle :heat "/ws/ws_rec_video_heat")
    (setup-toggle! day-toggle :day "/ws/ws_rec_video_day")

    ;; Initial state
    (update-toggle! heat-toggle :heat)
    (update-toggle! day-toggle :day)

    (seesaw/border-panel
      :border (i18n/tr :stream-controls)
      :center (seesaw/flow-panel
                :align :center
                :hgap 10
                :items [heat-toggle day-toggle]))))

(>defn- create-statistics-panel
  "Create panel showing stream statistics."
  []
  [=> [:fn {:error/message "must be a Swing panel"}
       #(instance? javax.swing.JPanel %)]]
  (let [stats-label (seesaw/text :id :stats-info
                                 :text (i18n/tr :no-statistics)
                                 :font status-font
                                 :multi-line? true
                                 :editable? false
                                 :background nil)]

    ;; This would be updated with real statistics from the streams
    ;; For now, just a placeholder
    (seesaw/border-panel
      :border (i18n/tr :statistics)
      :center (seesaw/scrollable stats-label
                                 :border 0))))

(>defn create
  "Create the control panel UI component.
  
  Returns a panel containing all control elements for managing
  video streams and application settings."
  []
  [=> [:fn {:error/message "must be a Swing panel"}
       #(instance? javax.swing.JPanel %)]]
  (let [header (seesaw/label :text (i18n/tr :control-panel-title)
                             :font header-font
                             :halign :center)
        connection-panel (create-connection-info-panel)
        controls-panel (create-stream-controls-panel)
        heat-status (create-stream-status-panel :heat)
        day-status (create-stream-status-panel :day)
        stats-panel (create-statistics-panel)]

    (seesaw/border-panel
      :border panel-border-width
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

