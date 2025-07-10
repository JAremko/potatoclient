(ns potatoclient.ui.control-panel
  "Control panel UI component for managing video streams"
  (:require [seesaw.core :as seesaw]
            [potatoclient.state :as state]
            [potatoclient.ipc :as ipc]))

(defn- toggle-stream
  "Toggle a stream on/off and update button text"
  [stream-key endpoint button]
  (if (state/get-stream stream-key)
    ;; Stop the stream
    (do
      (ipc/stop-stream stream-key)
      (seesaw/text! button (str (name stream-key) " Stream OFF"))
      false)
    ;; Start the stream
    (do
      (ipc/start-stream stream-key endpoint)
      (seesaw/text! button (str (name stream-key) " Stream ON"))
      true)))

(defn create
  "Create the control panel UI component"
  []
  (let [domain-field (seesaw/text :columns 20 :text (state/get-domain))
        heat-btn (seesaw/toggle :text "Heat Stream OFF")
        day-btn (seesaw/toggle :text "Day Stream OFF")
        clear-log-btn (seesaw/button :text "Clear Log")
        save-log-btn (seesaw/button :text "Save Logs")]
    
    ;; Register UI elements for updates
    (state/register-ui-element! :heat heat-btn)
    (state/register-ui-element! :day day-btn)
    
    ;; Update domain atom when field changes
    (seesaw/listen domain-field :document
      (fn [e]
        (state/set-domain! (seesaw/text domain-field))))
    
    ;; Stream toggle handlers
    (seesaw/listen heat-btn :action
      (fn [e]
        (toggle-stream :heat "/ws/ws_rec_video_heat" heat-btn)))
    
    (seesaw/listen day-btn :action
      (fn [e]
        (toggle-stream :day "/ws/ws_rec_video_day" day-btn)))
    
    ;; Log control handlers
    (seesaw/listen clear-log-btn :action
      (fn [e]
        (state/clear-logs!)))
    
    (seesaw/listen save-log-btn :action
      (fn [e]
        (require '[potatoclient.ui.log-export :as log-export])
        ((resolve 'potatoclient.ui.log-export/save-logs-dialog))))
    
    ;; Build the panel
    (seesaw/vertical-panel
      :border 5
      :items [(seesaw/label :text "Video Stream Control Center" :font "ARIAL-BOLD-16")
              (seesaw/horizontal-panel :items ["Domain: " domain-field])
              (seesaw/label :text "Heat: 900x720 @ 30fps | Day: 1920x1080 @ 30fps" :font "ARIAL-10")
              (seesaw/flow-panel :items [heat-btn day-btn])
              (seesaw/horizontal-panel :items [clear-log-btn save-log-btn])])))