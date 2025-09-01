(ns potatoclient.ui.tabs.overview
  "Overview tab showing system status and basic controls."
  (:require
    [potatoclient.i18n :as i18n]
    [seesaw.core :as seesaw])
  (:import (javax.swing JPanel)))

(defn create
  "Create overview tab content."
  {:malli/schema [:=> [:cat] [:fn {:error/message "must be a Swing panel"} (partial instance? JPanel)]]}
  []
  (seesaw/border-panel
    :border 10
    :center (seesaw/label 
              :text "Overview Tab - Placeholder"
              :halign :center
              :font {:name "Arial" :style :bold :size 18})))

(defn get-tab-info
  "Get tab information for tabbed pane."
  {:malli/schema [:=> [:cat] :map]}
  []
  {:title (i18n/tr :tab-overview)
   :tip "System overview and status"
   :icon nil
   :content (seesaw/scrollable (create) :border 0)})