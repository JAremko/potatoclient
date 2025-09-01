(ns potatoclient.ui.tabs-windows
  "Detached window management for tabs.
  
  Provides functionality to create separate windows for tab content
  that can be opened and closed independently of the main window."
  (:require [potatoclient.i18n :as i18n]
            [potatoclient.state :as state]
            [potatoclient.ui.bind-group :as bg]
            [potatoclient.ui.status-bar.messages :as status-bar]
            [seesaw.core :as seesaw])
  (:import [javax.swing JFrame WindowConstants]
           [java.awt.event WindowAdapter ComponentAdapter]))

(def ^:private detached-windows
  "Map of tab-key to detached window frame."
  (atom {}))

(defn- make-window-title
  "Create window title for detached tab."
  {:malli/schema [:=> [:cat :keyword] :string]}
  [tab-key]
  (str (i18n/tr :app-title) " - "
       (case tab-key
         :controls (i18n/tr :tab-controls)
         :day-camera (i18n/tr :tab-day-camera)
         :thermal-camera (i18n/tr :tab-thermal-camera)
         :modes (i18n/tr :tab-modes)
         :media (i18n/tr :tab-media)
         (name tab-key))))

(defn- get-binding-group-key
  "Get the binding group key for a detached window."
  {:malli/schema [:=> [:cat :keyword] :keyword]}
  [tab-key]
  (keyword (str "detached-" (name tab-key))))

(defn- save-window-bounds!
  "Save the current window bounds to state."
  {:malli/schema [:=> [:cat :keyword [:fn #(instance? JFrame %)]] :nil]}
  [tab-key window]
  (let [bounds (.getBounds window)]
    (state/set-tab-window-bounds! tab-key
                                  (.x bounds)
                                  (.y bounds)
                                  (.width bounds)
                                  (.height bounds)))
  nil)

(defn close-detached-window!
  "Close the detached window for a tab."
  {:malli/schema [:=> [:cat :keyword] :nil]}
  [tab-key]
  (when-let [window (get @detached-windows tab-key)]
    ;; Save final window position
    (save-window-bounds! tab-key window)

    ;; Clean up bindings for this window's CONTENT only (not the tab header)
    (let [binding-group (get-binding-group-key tab-key)]
      (bg/clean-group binding-group state/app-state))

    ;; Close and dispose window
    (.setVisible window false)
    (.dispose window)

    ;; Remove from tracking
    (swap! detached-windows dissoc tab-key)

    ;; Update state to reflect window is closed
    (state/set-tab-window! tab-key false)

    ;; Force a state change notification
    (swap! state/app-state identity)

    ;; Update status
    (status-bar/set-info! (str "Closed window for " (make-window-title tab-key))))
  nil)

(defn create-detached-window!
  "Create a detached window for tab content.
  
  Parameters:
    tab-key - The tab identifier
    content-factory - Function that takes a binding-group key and creates the content panel
    parent-frame - The parent frame (for positioning)
  
  Returns the created JFrame."
  {:malli/schema [:=> [:cat :keyword [:=> [:cat [:maybe :keyword]] :any] [:fn #(instance? JFrame %)]]
                  [:fn #(instance? JFrame %)]]}
  [tab-key content-factory parent-frame]
  ;; Close existing window if any
  (close-detached-window! tab-key)

  (let [;; Get binding group for this window
        binding-group (get-binding-group-key tab-key)

        ;; Create content using factory with binding group
        content (content-factory binding-group)

        ;; Get saved bounds or calculate default
        saved-bounds (state/get-tab-window-bounds tab-key)
        default-bounds (when parent-frame
                         (let [parent-bounds (.getBounds parent-frame)]
                           {:x (+ (.x parent-bounds) 50)
                            :y (+ (.y parent-bounds) 50)
                            :width 800
                            :height 600}))
        bounds (or saved-bounds default-bounds {:x 100 :y 100 :width 800 :height 600})

        ;; Create the window
        window (seesaw/frame
                 :title (make-window-title tab-key)
                 :content content
                 :size [(:width bounds) :by (:height bounds)]
                 :on-close :nothing)]

    ;; Configure window
    (.setDefaultCloseOperation window WindowConstants/DO_NOTHING_ON_CLOSE)

    ;; Set position
    (.setLocation window (:x bounds) (:y bounds))

    ;; Add window listener to handle close and save bounds
    (.addWindowListener window
                        (proxy [WindowAdapter] []
                          (windowClosing [_]
                            (close-detached-window! tab-key))))

    ;; Add component listener to save bounds on resize/move
    (.addComponentListener window
                           (proxy [ComponentAdapter] []
                             (componentResized [_]
                               (save-window-bounds! tab-key window))
                             (componentMoved [_]
                               (save-window-bounds! tab-key window))))

    ;; Store window reference
    (swap! detached-windows assoc tab-key window)

    ;; Update state
    (state/set-tab-window! tab-key true)

    ;; Show window
    (.setVisible window true)

    ;; Update status
    (status-bar/set-info! (str "Opened window for " (make-window-title tab-key)))

    window))

(defn toggle-detached-window!
  "Toggle the detached window for a tab."
  {:malli/schema [:=> [:cat :keyword [:=> [:cat [:maybe :keyword]] :any] [:fn #(instance? JFrame %)]] :nil]}
  [tab-key content-factory parent-frame]
  (if (state/tab-has-window? tab-key)
    (close-detached-window! tab-key)
    (create-detached-window! tab-key content-factory parent-frame))
  nil)

(defn get-detached-window
  "Get the detached window for a tab if it exists."
  {:malli/schema [:=> [:cat :keyword] [:maybe [:fn #(instance? JFrame %)]]]}
  [tab-key]
  (get @detached-windows tab-key))

(defn close-all-windows!
  "Close all detached windows."
  {:malli/schema [:=> [:cat] :nil]}
  []
  (doseq [tab-key (keys @detached-windows)]
    (close-detached-window! tab-key))
  nil)

(defn reload-all-windows!
  "Reload all detached windows (for theme/locale changes).
  
  Parameters:
    parent-frame - The parent frame
    tabs-config - The tabs configuration with content factories"
  {:malli/schema [:=> [:cat [:fn #(instance? JFrame %)] [:sequential :map]] :nil]}
  [parent-frame tabs-config]
  ;; Get list of currently open windows
  (let [open-windows (set (keys @detached-windows))]
    ;; Close all windows (this cleans up bindings)
    (close-all-windows!)

    ;; Recreate previously open windows
    (doseq [tab-config tabs-config]
      (let [tab-key (:key tab-config)]
        (when (contains? open-windows tab-key)
          (create-detached-window! tab-key (:content-factory tab-config) parent-frame)))))
  nil)