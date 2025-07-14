(ns potatoclient.ui.control-panel
  "Control panel UI component for managing video streams.
  
  Provides the main control interface for connecting/disconnecting
  video streams and managing application settings."
  (:require [potatoclient.state :as state]
            [potatoclient.ipc :as ipc]
            [potatoclient.i18n :as i18n]
            [seesaw.core :as seesaw]
            [seesaw.border :as border]
            [orchestra.core :refer [defn-spec]]
            [clojure.spec.alpha :as s]))

;; Stream configuration
(def ^:private stream-config
  {:heat {:endpoint "/ws/ws_rec_video_heat"
          :specs "900x720 @ 30fps"}
   :day  {:endpoint "/ws/ws_rec_video_day"
          :specs "1920x1080 @ 30fps"}})

;; UI styling constants
(def ^:private panel-border-width 5)
(def ^:private domain-field-columns 20)
(def ^:private label-font "ARIAL-BOLD-16")
(def ^:private spec-font "ARIAL-10")

;; Specs for control-panel namespace
(s/def ::stream-key #{:heat :day})
(s/def ::button #(instance? javax.swing.AbstractButton %))
(s/def ::label-text string?)
(s/def ::panel #(instance? javax.swing.JPanel %))

(defn-spec ^:private toggle-stream! boolean?
  "Toggle a stream on/off and update button text."
  [stream-key ::stream-key, button ::button]
  (let [{:keys [endpoint]} (get stream-config stream-key)]
    (if (state/get-stream stream-key)
      ;; Stop the stream
      (do
        (ipc/stop-stream stream-key)
        (seesaw/text! button (i18n/tr :control-button-connect))
        false)
      ;; Start the stream
      (do
        (ipc/start-stream stream-key endpoint)
        (seesaw/text! button (i18n/tr :control-button-disconnect))
        true))))

(defn-spec ^:private create-stream-button ::button
  "Create a toggle button for stream control."
  [stream-key ::stream-key, initial-state boolean?]
  (let [button (seesaw/toggle
                :text (if initial-state
                        (i18n/tr :control-button-disconnect)
                        (i18n/tr :control-button-connect))
                :selected? initial-state)]
    ;; Register for state updates
    (state/register-ui-element! stream-key button)
    
    ;; Add action handler
    (seesaw/listen button :action
                  (fn [_] (toggle-stream! stream-key button)))
    button))

(defn-spec ^:private create-stream-panel ::panel
  "Create a control panel for a single stream."
  [stream-key ::stream-key, label-text ::label-text]
  (let [{:keys [specs]} (get stream-config stream-key)
        is-connected (some? (state/get-stream stream-key))
        button (create-stream-button stream-key is-connected)]
    (seesaw/border-panel
     :border (border/compound-border
              (border/line-border :thickness 1 :color "#cccccc")
              panel-border-width
              label-text)
     :center (seesaw/vertical-panel
              :border panel-border-width
              :items [(seesaw/label :text specs :font spec-font)
                      button]))))

(defn-spec ^:private create-domain-field #(instance? javax.swing.JTextField %)
  "Create the domain configuration field."
  []
  (let [field (seesaw/text :columns domain-field-columns
                          :text (state/get-domain))]
    ;; Update state when field changes
    (seesaw/listen field :document
                  (fn [_]
                    (state/set-domain! (seesaw/text field))))
    field))

(defn-spec ^:private create-log-controls (s/coll-of ::button :count 2)
  "Create log management buttons."
  []
  (let [clear-btn (seesaw/button :text "Clear Log")
        export-btn (seesaw/button :text (i18n/tr :menu-file-export))]
    
    ;; Clear button handler
    (seesaw/listen clear-btn :action
                  (fn [_] (state/clear-logs!)))
    
    ;; Export button handler - lazy load export dialog
    (seesaw/listen export-btn :action
                  (fn [_]
                    (require '[potatoclient.ui.log-export :as log-export])
                    ((resolve 'potatoclient.ui.log-export/save-logs-dialog))))
    
    [clear-btn export-btn]))

(defn-spec ^:private create-header-section ::panel
  "Create the header section with domain configuration."
  []
  (seesaw/vertical-panel
   :items [(seesaw/label :text (i18n/tr :control-label-system)
                        :font label-font)
           (seesaw/horizontal-panel
            :items [(str (i18n/tr :config-server-address) ": ")
                    (create-domain-field)])
           (seesaw/separator)]))

(defn-spec ^:private create-streams-section ::panel
  "Create the streams control section."
  []
  (seesaw/horizontal-panel
   :items [(create-stream-panel :heat (i18n/tr :control-label-heat))
           (create-stream-panel :day (i18n/tr :control-label-day))]))

(defn-spec ^:private create-log-section ::panel
  "Create the log controls section."
  []
  (let [[clear-btn export-btn] (create-log-controls)]
    (seesaw/vertical-panel
     :items [(seesaw/separator)
             (seesaw/flow-panel :items [clear-btn export-btn])])))

(defn-spec create ::panel
  "Create the control panel UI component.
  
  Returns a panel containing all control elements for managing
  video streams and application settings."
  []
  (seesaw/vertical-panel
   :border panel-border-width
   :items [(create-header-section)
           (create-streams-section)
           (create-log-section)]))