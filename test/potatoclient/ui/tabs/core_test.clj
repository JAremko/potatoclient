(ns potatoclient.ui.tabs.core-test
  "Tests for tab panel functionality."
  (:require [clojure.test :refer [deftest is testing]]
            [potatoclient.ui.tabs :as tabs]
            [potatoclient.i18n :as i18n])
  (:import (javax.swing JTabbedPane JPanel JFrame)))

(deftest test-tabs-creation
  (testing "Tab panel creation"
    (i18n/load-translations!)
    
    (let [parent-frame (JFrame.)
          tabbed-pane (tabs/create-default-tabs parent-frame)]
      (is (instance? JTabbedPane tabbed-pane)
          "Should create a JTabbedPane")
      
      (is (> (.getTabCount tabbed-pane) 0)
          "Should have at least one tab")
      
      (is (= JTabbedPane/TOP (.getTabPlacement tabbed-pane))
          "Tabs should be placed at the top"))))

; TODO: Re-enable when overview namespace is implemented
#_(deftest test-overview-tab
  (testing "Overview tab creation"
    (i18n/load-translations!)
    
    (let [overview-panel (overview/create)]
      (is (instance? JPanel overview-panel)
          "Overview should create a JPanel"))
    
    (let [tab-info (overview/get-tab-info)]
      (is (map? tab-info) "get-tab-info should return a map")
      (is (contains? tab-info :title) "Tab info should have title")
      (is (contains? tab-info :tip) "Tab info should have tooltip")
      (is (contains? tab-info :content) "Tab info should have content")
      (is (string? (:title tab-info)) "Title should be a string")
      (is (string? (:tip tab-info)) "Tooltip should be a string"))))