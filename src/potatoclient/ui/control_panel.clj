(ns potatoclient.ui.control-panel
  "Control panel UI component for managing video streams.
  
  Provides the main control interface for connecting/disconnecting
  video streams and managing application settings."
  (:require [potatoclient.state :as state]
            [potatoclient.i18n :as i18n]
            [potatoclient.specs :as specs]
            [seesaw.core :as seesaw]
            [seesaw.border :as border]
            [malli.core :as m]))


;; UI styling constants
(def ^:private panel-border-width 5)
(def ^:private domain-field-columns 20)
(def ^:private label-font "ARIAL-BOLD-16")


(defn ^:private create-domain-field
  "Create the domain configuration field."
  []
  (let [field (seesaw/text :columns domain-field-columns
                          :text (state/get-domain))]
    ;; Update state when field changes
    (seesaw/listen field :document
                  (fn [_]
                    (state/set-domain! (seesaw/text field))))
    field))


(defn ^:private create-log-controls
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


(defn ^:private create-header-section
  "Create the header section with domain configuration."
  []
  (seesaw/vertical-panel
   :items [(seesaw/label :text (i18n/tr :control-label-system)
                        :font label-font)
           (seesaw/horizontal-panel
            :items [(str (i18n/tr :config-server-address) ": ")
                    (create-domain-field)])
           (seesaw/separator)]))



(defn ^:private create-log-section
  "Create the log controls section."
  []
  (let [[clear-btn export-btn] (create-log-controls)]
    (seesaw/vertical-panel
     :items [(seesaw/separator)
             (seesaw/flow-panel :items [clear-btn export-btn])])))


(defn create
  "Create the control panel UI component.
  
  Returns a panel containing all control elements for managing
  video streams and application settings."
  []
  (seesaw/vertical-panel
   :border panel-border-width
   :items [(create-header-section)
           (create-log-section)]))

