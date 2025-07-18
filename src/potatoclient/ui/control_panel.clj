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
(def ^:private label-font "ARIAL-BOLD-16")


(defn create
  "Create the control panel UI component.
  
  Returns a panel containing all control elements for managing
  video streams and application settings."
  []
  (seesaw/vertical-panel
   :border panel-border-width
   :items [(seesaw/label :text "Control Panel" :font label-font)]))

