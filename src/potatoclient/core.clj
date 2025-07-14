(ns potatoclient.core
  "Core application logic for PotatoClient.
  Handles UI initialization, lifecycle management, and coordination."
  (:require [seesaw.core :as seesaw]
            [seesaw.action :as action]
            [potatoclient.state :as state]
            [potatoclient.process :as process]
            [potatoclient.ui.control-panel :as control-panel]
            [potatoclient.ui.log-table :as log-table]
            [potatoclient.ui.log-export :as log-export]
            [potatoclient.events.log :as log]
            [potatoclient.config :as config]
            [potatoclient.i18n :as i18n]
            [potatoclient.theme :as theme])
  (:gen-class))

(declare create-main-window)

(defn- preserve-window-state
  "Extract window state for restoration."
  [^javax.swing.JFrame frame]
  {:bounds (.getBounds frame)
   :extended-state (.getExtendedState frame)})

(defn- restore-window-state!
  "Restore window state to a frame."
  [^javax.swing.JFrame frame state]
  (doto frame
    (.setBounds ^java.awt.Rectangle (:bounds state))
    (.setExtendedState ^Integer (:extended-state state))))

(defn- reload-ui!
  "Reload the UI with new locale while preserving window state."
  [^javax.swing.JFrame frame]
  (let [window-state (preserve-window-state frame)
        new-frame (create-main-window)]
    (.dispose frame)
    (restore-window-state! new-frame window-state)
    (seesaw/show! new-frame)))

(defn- create-language-menu-item
  "Create a language selection menu item."
  [lang-key display-name group current-locale reload-fn]
  (seesaw/radio-menu-item
   :text display-name
   :group group
   :selected? (= current-locale lang-key)
   :listen [:action (fn [_]
                     (state/set-locale! lang-key)
                     (config/update-config! :locale lang-key)
                     (reload-fn))]))

(defn- create-theme-menu-item
  "Create a theme selection menu item."
  [theme-key theme-group current-theme frame-ref]
  (seesaw/radio-menu-item
   :text (theme/get-theme-name theme-key)
   :group theme-group
   :selected? (= theme-key current-theme)
   :listen [:action (fn [_]
                     (when (theme/set-theme! theme-key)
                       (config/save-theme! theme-key)
                       (javax.swing.SwingUtilities/updateComponentTreeUI @frame-ref)))]))

(defn- create-file-menu
  "Create the File menu."
  []
  (seesaw/menu 
   :text (i18n/tr :menu-file)
   :items [(action/action
            :name (i18n/tr :menu-file-export)
            :handler (fn [_] (log-export/save-logs-dialog)))
           :separator
           (action/action
            :name (i18n/tr :menu-file-exit)
            :handler (fn [_] (System/exit 0)))]))

(defn- create-view-menu
  "Create the View menu."
  [frame-ref reload-fn]
  (let [lang-group (seesaw/button-group)
        theme-group (seesaw/button-group)
        current-theme (theme/get-current-theme)
        current-locale (state/get-locale)]
    (seesaw/menu 
     :text (i18n/tr :menu-view)
     :items [(seesaw/menu 
              :text (i18n/tr :menu-view-theme)
              :items (map #(create-theme-menu-item % theme-group current-theme frame-ref)
                         (theme/get-available-themes)))
             :separator
             (seesaw/menu 
              :text (i18n/tr :menu-view-language)
              :items [(create-language-menu-item :english "English" lang-group current-locale reload-fn)
                      (create-language-menu-item :ukrainian "Українська" lang-group current-locale reload-fn)])])))

(defn- create-help-menu
  "Create the Help menu."
  [frame-ref]
  (seesaw/menu 
   :text (i18n/tr :menu-help)
   :items [(action/action
            :name (i18n/tr :menu-help-about)
            :handler (fn [_]
                      (seesaw/alert @frame-ref
                                   (i18n/tr :about-text)
                                   :title (i18n/tr :about-title)
                                   :type :info)))]))

(defn- create-menu-bar
  "Create the application menu bar."
  [frame-ref]
  (let [reload-fn #(reload-ui! @frame-ref)]
    (seesaw/menubar
     :items [(create-file-menu)
             (create-view-menu frame-ref reload-fn)
             (create-help-menu frame-ref)])))

(defn- create-main-content
  "Create the main content panel."
  []
  (seesaw/border-panel
   :north (control-panel/create)
   :center (log-table/create)))

(defn- create-main-window
  "Create and configure the main application window."
  []
  (let [frame-ref (atom nil)
        frame (seesaw/frame
               :title (i18n/tr :app-title)
               :icon (clojure.java.io/resource "main.png")
               :on-close :exit
               :size [800 :by 600]
               :content (create-main-content))]
    (reset! frame-ref frame)
    (.setJMenuBar ^javax.swing.JFrame frame (create-menu-bar frame-ref))
    frame))

(defn- setup-shutdown-hook!
  "Setup JVM shutdown hook to clean up processes."
  []
  (.addShutdownHook
   (Runtime/getRuntime)
   (Thread.
    (fn []
      (try
        (process/cleanup-all-processes state/app-state)
        (catch Exception e
          nil))))))

(defn- initialize-application!
  "Initialize all application subsystems."
  []
  (config/initialize!)
  (i18n/init!)
  (setup-shutdown-hook!))

(defn- log-startup!
  "Log application startup."
  []
  (log/add-log-entry!
   {:time (System/currentTimeMillis)
    :stream "SYSTEM"
    :type "INFO"
    :message "Control Center started"}))

(defn -main
  "Application entry point."
  [& args]
  (initialize-application!)
  (seesaw/invoke-later
   (-> (create-main-window)
       seesaw/show!))
  (log-startup!))