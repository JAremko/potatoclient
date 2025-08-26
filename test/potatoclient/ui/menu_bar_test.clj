(ns potatoclient.ui.menu-bar-test
  "Tests for menu bar binding cleanup functionality."
  (:require [clojure.test :refer [deftest is testing]]
            [potatoclient.state :as state]
            [potatoclient.ui.menu-bar :as menu-bar]
            [seesaw.core :as seesaw])
  (:import (javax.swing JFrame)))

(deftest test-binding-cleanup
  (testing "Menu bar bindings are properly cleaned up"
    (let [;; Create a test frame with menu bar
          frame (seesaw/frame :title "Test Frame")
          menubar (menu-bar/create-menubar
                    {:parent frame
                     :include-stream-buttons? true
                     :include-theme? false
                     :include-language? false
                     :include-help? false})
          ;; Get the stream buttons
          components (.getClientProperty menubar ::menu-bar/bound-components)]
      
      ;; Verify buttons have unbind functions attached
      (is (seq components) "Should have bound components")
      
      (doseq [component components]
        (is (.getClientProperty component ::menu-bar/unbind-fn)
            "Each bound component should have an unbind function"))
      
      ;; Get initial watcher count on app-state
      (let [initial-watchers (count (.getWatches state/app-state))]
        
        ;; Clean up the menu bar
        (menu-bar/cleanup-menubar! menubar)
        
        ;; Verify watchers were removed
        (let [final-watchers (count (.getWatches state/app-state))]
          (is (< final-watchers initial-watchers)
              "Watchers should be removed after cleanup")))
      
      ;; Clean up
      (.dispose frame))))

(deftest test-frame-reload-cleanup
  (testing "Frame reload properly cleans up bindings"
    (let [cleanup-called? (atom false)
          ;; Mock the cleanup function to track if it's called
          original-cleanup menu-bar/cleanup-menubar!]
      (with-redefs [menu-bar/cleanup-menubar! 
                    (fn [menubar]
                      (reset! cleanup-called? true)
                      (original-cleanup menubar))]
        
        (let [frame (seesaw/frame :title "Test Frame")
              menubar (menu-bar/create-menubar
                        {:parent frame
                         :include-stream-buttons? true
                         :reload-fn (fn [_] frame)})
              frame-cons (fn [_] frame)]
          
          (.setJMenuBar frame menubar)
          
          ;; Test that reload-frame! calls cleanup
          ;; We need to redefine invoke-later to run synchronously for testing
          (with-redefs [seesaw/dispose! (fn [_] nil)
                        seesaw/show! (fn [_] nil)
                        seesaw/invoke-later (fn [f] 
                                              ;; Run the function synchronously for testing
                                              (f))]
            ;; Call the private function directly
            (@#'menu-bar/reload-frame! frame frame-cons))
          
          (is @cleanup-called? "Cleanup should be called during frame reload"))))))