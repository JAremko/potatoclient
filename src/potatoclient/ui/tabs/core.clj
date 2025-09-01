(ns potatoclient.ui.tabs.core
  "Tab panel management for main frame."
  (:require
    [potatoclient.ui.tabs.overview :as overview-tab]
    [seesaw.core :as seesaw])
  (:import (javax.swing JTabbedPane)))

(defn create
  "Create tabbed panel with all tabs."
  {:malli/schema [:=> [:cat] [:fn {:error/message "must be a JTabbedPane"} (partial instance? JTabbedPane)]]}
  []
  (let [overview (overview-tab/get-tab-info)]
    (seesaw/tabbed-panel
      :placement :left
      :overflow :scroll
      :tabs [overview])))