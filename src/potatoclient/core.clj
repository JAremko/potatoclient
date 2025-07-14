(ns potatoclient.core
  "Main entry point for the Potato Client application"
  (:require [seesaw.core :as seesaw]
            [potatoclient.state :as state]
            [potatoclient.process :as process]
            [potatoclient.ui.control-panel :as control-panel]
            [potatoclient.ui.log-table :as log-table]
            [potatoclient.ui.log-export :as log-export]
            [potatoclient.events.log :as log]
            [potatoclient.config :as config]
            [potatoclient.i18n :as i18n]
            [potatoclient.theme :as theme]
            [seesaw.action :as action])
  (:gen-class))

;; Forward declaration
(declare create-main-window)

(defn- reload-ui!
  "Reload the UI with new locale"
  [frame]
  (let [bounds (.getBounds frame)
        new-frame (create-main-window)]
    (.dispose frame)
    (.setBounds new-frame bounds)
    (seesaw/show! new-frame)))

(defn- create-menu-bar
  "Create the application menu bar"
  [frame]
  (let [reload-fn #(reload-ui! @frame)
        lang-group (seesaw/button-group)
        theme-group (seesaw/button-group)
        current-theme (theme/get-current-theme)]
    (seesaw/menubar
      :items [(seesaw/menu :text (i18n/tr :menu-file)
                          :items [(action/action
                                   :name (i18n/tr :menu-file-export)
                                   :handler (fn [_] (log-export/save-logs-dialog)))
                                  :separator
                                  (action/action
                                   :name (i18n/tr :menu-file-exit)
                                   :handler (fn [_] (System/exit 0)))])
              (seesaw/menu :text (i18n/tr :menu-view)
                          :items [(seesaw/menu :text (i18n/tr :menu-view-theme)
                                              :items (for [theme-key (theme/get-available-themes)]
                                                      (seesaw/radio-menu-item
                                                       :text (theme/get-theme-name theme-key)
                                                       :group theme-group
                                                       :selected? (= theme-key current-theme)
                                                       :listen [:action (fn [_]
                                                                         (when (theme/set-theme! theme-key)
                                                                           (config/save-theme! theme-key)
                                                                           (javax.swing.SwingUtilities/updateComponentTreeUI @frame)))])))
                                  :separator
                                  (seesaw/menu :text (i18n/tr :menu-view-language)
                                              :items [(seesaw/radio-menu-item
                                                       :text "English"
                                                       :group lang-group
                                                       :selected? (= (state/get-locale) :english)
                                                       :listen [:action (fn [_]
                                                                         (state/set-locale! :english)
                                                                         (config/update-config! :locale :english)
                                                                         (reload-fn))])
                                                      (seesaw/radio-menu-item
                                                       :text "Українська"
                                                       :group lang-group
                                                       :selected? (= (state/get-locale) :ukrainian)
                                                       :listen [:action (fn [_]
                                                                         (state/set-locale! :ukrainian)
                                                                         (config/update-config! :locale :ukrainian)
                                                                         (reload-fn))])])])
              (seesaw/menu :text (i18n/tr :menu-help)
                          :items [(action/action
                                   :name (i18n/tr :menu-help-about)
                                   :handler (fn [_]
                                             (seesaw/alert @frame
                                                          (i18n/tr :about-text)
                                                          :title (i18n/tr :about-title)
                                                          :type :info)))])])))

(defn- create-main-window
  "Create and configure the main application window"
  []
  (let [frame-ref (atom nil)
        control-panel (control-panel/create)
        log-table (log-table/create)
        frame (seesaw/frame
                :title (i18n/tr :app-title)
                :icon (clojure.java.io/resource "main.png")
                :on-close :exit
                :size [800 :by 600]
                :content (seesaw/border-panel
                           :north control-panel
                           :center log-table))]
    (reset! frame-ref frame)
    (.setJMenuBar frame (create-menu-bar frame-ref))
    frame))

(defn- setup-shutdown-hook
  "Setup JVM shutdown hook to clean up processes"
  []
  (.addShutdownHook (Runtime/getRuntime)
    (Thread. #(process/cleanup-all-processes state/app-state))))

(defn -main
  "Application entry point"
  [& args]
  ;; Initialize configuration and theming
  (config/initialize!)
  
  ;; Initialize localization
  (i18n/init!)
  
  ;; Setup shutdown hook
  (setup-shutdown-hook)
  
  ;; Create and show the main window
  (seesaw/invoke-later
    (-> (create-main-window)
        seesaw/show!))
  
  ;; Log startup
  (log/add-log-entry! {:time (System/currentTimeMillis)
                       :stream "SYSTEM"
                       :type "INFO"
                       :message "Control Center started"}))