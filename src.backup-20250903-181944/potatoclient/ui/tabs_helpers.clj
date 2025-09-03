(ns potatoclient.ui.tabs-helpers
  "Helper functions for the tab system.
  
  Provides utilities for creating tab headers with detach buttons
  and managing tab state synchronization."
  (:require
            [malli.core :as m] [potatoclient.i18n :as i18n]
            [potatoclient.state :as state]
            [potatoclient.ui.tabs-windows :as windows]
            [seesaw.core :as seesaw])
  (:import [javax.swing JPanel JFrame]
           [java.awt FlowLayout]))

(defn cleanup-tab-watchers
  "Remove all watchers for tabs. Call this when disposing of tabs."
  [tab-keys]
  (doseq [tab-key tab-keys]
    (remove-watch state/app-state (keyword (str "checkbox-updater-" (name tab-key)))))
  nil) 
 (m/=> cleanup-tab-watchers [:=> [:cat [:sequential :keyword]] :nil])

(defn create-tab-header
  "Create a tab header panel with title and window toggle button.
  
  Parameters:
    tab-key - The tab identifier
    title - The tab title text
    parent-frame - The parent frame for detached windows
    content-factory - Function that creates tab content with optional binding group
  
  Returns a JPanel with the tab header."
  [tab-key title parent-frame content-factory]
  (let [panel (JPanel. (FlowLayout. FlowLayout/LEFT 5 0))
        label (seesaw/label :text title)
        ;; Simple checkbox without confusing icon
        checkbox (seesaw/checkbox :selected? (state/tab-has-window? tab-key)
                                  :tip (i18n/tr :detach-window-tip))
        ;; Track if we're updating programmatically to avoid loops
        updating-atom (atom false)]

    ;; Watch state changes and update checkbox directly
    (add-watch state/app-state (keyword (str "checkbox-updater-" (name tab-key)))
               (fn [_ _ old-state new-state]
                 (let [old-val (get-in old-state [:ui :tab-properties tab-key :has-window] false)
                       new-val (get-in new-state [:ui :tab-properties tab-key :has-window] false)]
                   (when (not= old-val new-val)
                     (reset! updating-atom true)
                     (.setSelected checkbox new-val)
                     (reset! updating-atom false)))))

    ;; Handle checkbox changes - update window state based on checkbox state
    (seesaw/listen checkbox :action
                   (fn [e]
                     ;; Ignore if we're updating programmatically
                     (when-not @updating-atom
                       (let [selected (.isSelected checkbox)]
                         ;; Update window state based on checkbox
                         (if selected
                           (when-not (state/tab-has-window? tab-key)
                             (windows/create-detached-window! tab-key content-factory parent-frame))
                           (when (state/tab-has-window? tab-key)
                             (windows/close-detached-window! tab-key)))))))

    ;; Add components to panel
    (.add panel label)
    (.add panel checkbox)
    panel)) 
 (m/=> create-tab-header [:=> [:cat :keyword :string [:fn (fn* [p1__4181#] (instance? JFrame p1__4181#))] [:=> [:cat [:maybe :keyword]] :any]] [:fn (fn* [p1__4183#] (instance? JPanel p1__4183#))]])