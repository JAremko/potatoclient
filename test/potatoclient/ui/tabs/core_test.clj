(ns potatoclient.ui.tabs.core-test
  "Tests for tab panel functionality."
  (:require [clojure.test :refer [deftest is testing]]
            [potatoclient.ui.tabs.core :as tabs]
            [potatoclient.ui.tabs.overview :as overview]
            [potatoclient.i18n :as i18n])
  (:import (javax.swing JTabbedPane JPanel)))

(deftest test-tabs-creation
  (testing "Tab panel creation"
    (i18n/load-translations!)
    
    (let [tabbed-pane (tabs/create)]
      (is (instance? JTabbedPane tabbed-pane)
          "Should create a JTabbedPane")
      
      (is (= 1 (.getTabCount tabbed-pane))
          "Should have one tab initially")
      
      (is (= JTabbedPane/LEFT (.getTabPlacement tabbed-pane))
          "Tabs should be placed on the left"))))

(deftest test-overview-tab
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