(ns potatoclient.ui.control-panel
  "Control panel UI component for managing video streams"
  (:require [potatoclient.state :as state]
            [potatoclient.ipc :as ipc]
            [potatoclient.i18n :as i18n])
  (:use [seesaw core border]))

(defn- toggle-stream
  "Toggle a stream on/off and update button text"
  [stream-key endpoint button]
  (if (state/get-stream stream-key)
    ;; Stop the stream
    (do
      (ipc/stop-stream stream-key)
      (text! button (i18n/tr :control-button-connect))
      false)
    ;; Start the stream
    (do
      (ipc/start-stream stream-key endpoint)
      (text! button (i18n/tr :control-button-disconnect))
      true)))

(defn- create-stream-control
  "Create a control panel for a single stream"
  [stream-key stream-label endpoint specs]
  (let [is-connected (state/get-stream stream-key)
        button (toggle :text (if is-connected
                              (i18n/tr :control-button-disconnect)
                              (i18n/tr :control-button-connect))
                      :selected? is-connected)]
    ;; Register UI element for updates
    (state/register-ui-element! stream-key button)
    
    ;; Stream toggle handler
    (listen button :action
      (fn [e]
        (toggle-stream stream-key endpoint button)))
    
    (border-panel
      :border (compound-border
               (line-border :thickness 1 :color "#cccccc")
               5
               stream-label)
      :center (vertical-panel
                :border 5
                :items [(label :text specs :font "ARIAL-10")
                        button]))))

(defn create
  "Create the control panel UI component"
  []
  (let [domain-field (text :columns 20 :text (state/get-domain))
        heat-control (create-stream-control 
                       :heat 
                       (i18n/tr :control-label-heat)
                       "/ws/ws_rec_video_heat"
                       "900x720 @ 30fps")
        day-control (create-stream-control 
                      :day 
                      (i18n/tr :control-label-day)
                      "/ws/ws_rec_video_day"
                      "1920x1080 @ 30fps")
        clear-log-btn (button :text "Clear Log")
        save-log-btn (button :text (i18n/tr :menu-file-export))]
    
    ;; Update domain atom when field changes
    (listen domain-field :document
      (fn [e]
        (state/set-domain! (text domain-field))))
    
    ;; Log control handlers
    (listen clear-log-btn :action
      (fn [e]
        (state/clear-logs!)))
    
    (listen save-log-btn :action
      (fn [e]
        (require '[potatoclient.ui.log-export :as log-export])
        ((resolve 'potatoclient.ui.log-export/save-logs-dialog))))
    
    ;; Build the panel
    (vertical-panel
      :border 5
      :items [(label :text (i18n/tr :control-label-system) :font "ARIAL-BOLD-16")
              (horizontal-panel :items [(str (i18n/tr :config-server-address) ": ") domain-field])
              (separator)
              (horizontal-panel 
                :items [heat-control day-control])
              (separator)
              (flow-panel :items [clear-log-btn save-log-btn])])))