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
            [potatoclient.theme :as theme]
            [orchestra.core :refer [defn-spec]]
            [clojure.spec.alpha :as s])
  (:gen-class))

(declare create-main-window)

(defn-spec ^:private get-version string?
  "Get application version from VERSION file."
  []
  (try
    (clojure.string/trim (slurp (clojure.java.io/resource "VERSION")))
    (catch Exception _ "dev")))

(defn-spec ^:private get-build-type string?
  "Get build type (RELEASE or DEVELOPMENT)."
  []
  (if (or (System/getProperty "potatoclient.release")
          (System/getenv "POTATOCLIENT_RELEASE"))
    "RELEASE"
    "DEVELOPMENT"))

;; Specs for core namespace
(s/def ::window-state (s/keys :req-un [::bounds ::extended-state]))
(s/def ::bounds #(instance? java.awt.Rectangle %))
(s/def ::extended-state integer?)
(s/def ::frame #(instance? javax.swing.JFrame %))
(s/def ::lang-key #{:english :ukrainian})
(s/def ::display-name string?)
(s/def ::group #(instance? javax.swing.ButtonGroup %))
(s/def ::theme-key keyword?)

(defn-spec ^:private preserve-window-state ::window-state
  "Extract window state for restoration."
  [frame ::frame]
  {:bounds (.getBounds ^javax.swing.JFrame frame)
   :extended-state (.getExtendedState ^javax.swing.JFrame frame)})

(defn-spec ^:private restore-window-state! ::frame
  "Restore window state to a frame."
  [frame ::frame, state ::window-state]
  (doto ^javax.swing.JFrame frame
    (.setBounds ^java.awt.Rectangle (:bounds state))
    (.setExtendedState ^Integer (:extended-state state))))

(defn-spec ^:private reload-ui! any?
  "Reload the UI with new locale while preserving window state."
  [frame ::frame]
  (let [window-state (preserve-window-state frame)
        new-frame (create-main-window)]
    (.dispose ^javax.swing.JFrame frame)
    (restore-window-state! new-frame window-state)
    (seesaw/show! new-frame)))

(defn-spec ^:private create-language-menu-item #(instance? javax.swing.JRadioButtonMenuItem %)
  "Create a language selection menu item."
  [lang-key ::lang-key, display-name ::display-name, group ::group, current-locale keyword?, reload-fn fn?]
  (seesaw/radio-menu-item
   :text display-name
   :group group
   :selected? (= current-locale lang-key)
   :listen [:action (fn [_]
                     (state/set-locale! lang-key)
                     (config/update-config! :locale lang-key)
                     (reload-fn))]))

(defn-spec ^:private create-theme-menu-item #(instance? javax.swing.JRadioButtonMenuItem %)
  "Create a theme selection menu item."
  [theme-key ::theme-key, theme-group ::group, current-theme ::theme-key, frame-ref #(instance? clojure.lang.IDeref %)]
  (seesaw/radio-menu-item
   :text (theme/get-theme-name theme-key)
   :group theme-group
   :selected? (= theme-key current-theme)
   :listen [:action (fn [_]
                     (when (theme/set-theme! theme-key)
                       (config/save-theme! theme-key)
                       (javax.swing.SwingUtilities/updateComponentTreeUI @frame-ref)))]))

(defn-spec ^:private create-file-menu #(instance? javax.swing.JMenu %)
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

(defn-spec ^:private create-view-menu #(instance? javax.swing.JMenu %)
  "Create the View menu."
  [frame-ref #(instance? clojure.lang.IDeref %), reload-fn fn?]
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

(defn-spec ^:private create-help-menu #(instance? javax.swing.JMenu %)
  "Create the Help menu."
  [frame-ref #(instance? clojure.lang.IDeref %)]
  (seesaw/menu 
   :text (i18n/tr :menu-help)
   :items [(action/action
            :name (i18n/tr :menu-help-about)
            :handler (fn [_]
                      (let [about-text (format "%s\n\nVersion: %s\nBuild: %s\n\nOrchestra instrumentation: %s"
                                               (i18n/tr :about-text)
                                               (get-version)
                                               (get-build-type)
                                               (if (= "RELEASE" (get-build-type))
                                                 "Disabled (optimized)"
                                                 "Enabled (development)"))]
                        (seesaw/alert @frame-ref
                                     about-text
                                     :title (i18n/tr :about-title)
                                     :type :info))))]))

(defn-spec ^:private create-menu-bar #(instance? javax.swing.JMenuBar %)
  "Create the application menu bar."
  [frame-ref #(instance? clojure.lang.IDeref %)]
  (let [reload-fn #(reload-ui! @frame-ref)]
    (seesaw/menubar
     :items [(create-file-menu)
             (create-view-menu frame-ref reload-fn)
             (create-help-menu frame-ref)])))

(defn-spec ^:private create-main-content #(instance? javax.swing.JPanel %)
  "Create the main content panel."
  []
  (seesaw/border-panel
   :north (control-panel/create)
   :center (log-table/create)))

(defn-spec ^:private create-main-window ::frame
  "Create and configure the main application window."
  []
  (let [frame-ref (atom nil)
        title (format "%s v%s [%s]" 
                      (i18n/tr :app-title)
                      (get-version)
                      (get-build-type))
        frame (seesaw/frame
               :title title
               :icon (clojure.java.io/resource "main.png")
               :on-close :exit
               :size [800 :by 600]
               :content (create-main-content))]
    (reset! frame-ref frame)
    (.setJMenuBar ^javax.swing.JFrame frame (create-menu-bar frame-ref))
    frame))

(defn-spec ^:private setup-shutdown-hook! any?
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

(defn-spec ^:private initialize-application! any?
  "Initialize all application subsystems."
  []
  (config/initialize!)
  (i18n/init!)
  (setup-shutdown-hook!))

(defn-spec ^:private log-startup! any?
  "Log application startup."
  []
  (log/add-log-entry!
   {:time (System/currentTimeMillis)
    :stream "SYSTEM"
    :type "INFO"
    :message (format "Control Center started (v%s %s build)"
                     (get-version)
                     (get-build-type))}))

(defn-spec -main any?
  "Application entry point."
  [& args (s/* string?)]
  (initialize-application!)
  (seesaw/invoke-later
   (-> (create-main-window)
       seesaw/show!))
  (log-startup!))