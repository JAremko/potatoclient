(ns potatoclient.ui.menu-bar-test
  "Tests for menu bar functionality and binding cleanup."
  (:require [clojure.test :refer [deftest is testing]]
            [clojure.string :as str]
            [potatoclient.state :as state]
            [potatoclient.ui.menu-bar :as menu-bar]
            [seesaw.bind :as bind]
            [seesaw.core :as seesaw])
  (:import (javax.swing JFrame JToggleButton)))

(deftest test-stream-toggle-bindings
  (testing "Stream toggle buttons are properly bound to app state"
    (state/reset-state!)
    
    ;; Get initial watcher count
    (let [initial-watchers (count (.getWatches state/app-state))]
      
      ;; Create menu bar with stream buttons
      (let [frame (seesaw/frame :title "Test Frame")
            menubar (menu-bar/create-menubar
                      {:parent frame
                       :include-stream-buttons? true
                       :include-theme? false
                       :include-language? false
                       :include-help? false})]
        
        ;; Verify watchers were added for stream buttons
        (let [new-watcher-count (count (.getWatches state/app-state))]
          ;; Should have added 2 watchers (one for heat, one for day)
          (is (= (+ initial-watchers 2) new-watcher-count)
              "Should add 2 watchers for stream buttons"))
        
        ;; Find the toggle buttons in the menu bar
        (let [components (atom [])
              find-toggles (fn find-toggles [container]
                            (doseq [comp (.getComponents container)]
                              (cond
                                (instance? JToggleButton comp)
                                (swap! components conj comp)
                                
                                (instance? java.awt.Container comp)
                                (find-toggles comp))))]
          (find-toggles menubar)
          
          (is (= 2 (count @components))
              "Should find 2 toggle buttons")
          
          ;; Test that toggles respond to state changes
          (let [[heat-toggle day-toggle] @components]
            
            ;; Initially both should be unselected (processes stopped)
            (is (not (.isSelected heat-toggle))
                "Heat toggle should initially be unselected")
            (is (not (.isSelected day-toggle))
                "Day toggle should initially be unselected")
            
            ;; Update process state
            (state/update-process-status! :heat-video nil :running)
            (Thread/sleep 10) ; Give binding time to trigger
            
            (is (.isSelected heat-toggle)
                "Heat toggle should be selected when process is running")
            (is (not (.isSelected day-toggle))
                "Day toggle should remain unselected")
            
            ;; Update day process
            (state/update-process-status! :day-video nil :running)
            (Thread/sleep 10)
            
            (is (.isSelected day-toggle)
                "Day toggle should be selected when process is running")
            
            ;; Stop heat process
            (state/update-process-status! :heat-video nil :stopped)
            (Thread/sleep 10)
            
            (is (not (.isSelected heat-toggle))
                "Heat toggle should be unselected when process stops")))
        
        ;; Clean up
        (.dispose frame)))))

(deftest test-menubar-cleanup
  (testing "cleanup-menubar! removes all seesaw bindings"
    (state/reset-state!)
    
    (let [initial-count (count (.getWatches state/app-state))
          frame (seesaw/frame :title "Test Frame")
          frame-cons (fn [_] frame)
          menubar (menu-bar/create-menubar
                    {:parent frame
                     :include-stream-buttons? true
                     :include-theme? true
                     :include-language? true
                     :include-help? true
                     :reload-fn frame-cons})]
      
      ;; Verify watchers were added
      (let [with-menu-count (count (.getWatches state/app-state))]
        (is (> with-menu-count initial-count)
            "Menu bar should add watchers"))
      
      ;; Clean up menu bar
      (menu-bar/cleanup-menubar! menubar)
      
      ;; Verify watchers were removed
      (is (= initial-count (count (.getWatches state/app-state)))
          "All menu bar watchers should be removed")
      
      ;; Verify no seesaw watchers remain
      (let [watcher-keys (keys (.getWatches state/app-state))
            seesaw-watchers (filter #(and (keyword? %)
                                          (when-let [n (name %)]
                                            (str/starts-with? n "bindable-atom-watcher")))
                                   watcher-keys)]
        (is (empty? seesaw-watchers)
            "No seesaw watchers should remain after cleanup"))
      
      ;; Clean up
      (.dispose frame))))

(deftest test-frame-reload-cleanup
  (testing "Frame reload properly cleans up bindings"
    (state/reset-state!)
    
    (let [cleanup-called? (atom false)]
      
      (let [frame (seesaw/frame :title "Test Frame")
            frame-cons (fn [_] frame)]
        
        ;; Create menu bar
        (let [menubar (menu-bar/create-menubar
                        {:parent frame
                         :include-stream-buttons? true
                         :reload-fn frame-cons})]
          (.setJMenuBar frame menubar))
        
        ;; Mock everything needed for frame reload
        (with-redefs [state/cleanup-seesaw-bindings! 
                      (fn []
                        (reset! cleanup-called? true))
                      seesaw/invoke-later (fn [f] (f))
                      seesaw/dispose! (fn [_] nil)
                      seesaw/show! (fn [_] nil)
                      seesaw/config! (fn [_ & _] nil)]
          ;; Call the private reload function
          (#'menu-bar/reload-frame! frame frame-cons))
        
        (is @cleanup-called? 
            "cleanup-seesaw-bindings! should be called during frame reload")))))