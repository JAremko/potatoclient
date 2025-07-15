(ns potatoclient.ui.main-frame
  "Main application frame construction and management.
  
  Provides a clean-slate constructor for the main window that ensures
  proper initialization of all UI elements including theme-aware icons."
  (:require [seesaw.core :as seesaw]
            [seesaw.action :as action]
            [potatoclient.state :as state]
            [potatoclient.process :as process]
            [potatoclient.ui.control-panel :as control-panel]
            [potatoclient.ui.log-table :as log-table]
            [potatoclient.config :as config]
            [potatoclient.i18n :as i18n]
            [potatoclient.theme :as theme]
            [potatoclient.log-writer :as log-writer]
            [potatoclient.events.log :as log]
            [orchestra.core :refer [defn-spec]]
            [clojure.spec.alpha :as s])
  (:import [javax.swing JFrame JMenuBar JMenu]
           [java.awt.event WindowAdapter]))

;; Specs
(s/def ::window-state (s/keys :req-un [::bounds ::extended-state]))
(s/def ::bounds #(instance? java.awt.Rectangle %))
(s/def ::extended-state integer?)
(s/def ::frame #(instance? javax.swing.JFrame %))
(s/def ::lang-key #{:english :ukrainian})
(s/def ::display-name string?)
(s/def ::theme-key keyword?)
(s/def ::version string?)
(s/def ::build-type string?)
(s/def ::frame-params (s/keys :req-un [::version ::build-type]
                              :opt-un [::window-state]))

(defn-spec ^:private create-language-action #(instance? javax.swing.Action %)
  "Create a language selection action."
  [lang-key ::lang-key, display-name ::display-name, reload-fn fn?]
  (let [flag-icon (case lang-key
                    :english (seesaw/icon (clojure.java.io/resource "flags/en.png"))
                    :ukrainian (seesaw/icon (clojure.java.io/resource "flags/ua.png"))
                    nil)
        ;; Add padding to display name to ensure menu width
        padded-name (format "%-20s" display-name)]
    (action/action
     :name padded-name
     :icon flag-icon
     :handler (fn [_]
                ;; Only proceed if selecting a different language
                (when-not (= (state/get-locale) lang-key)
                  (state/set-locale! lang-key)
                  (config/update-config! :locale lang-key)
                  (reload-fn))))))

(defn-spec ^:private create-theme-action #(instance? javax.swing.Action %)
  "Create a theme selection action."
  [theme-key ::theme-key, reload-fn fn?]
  (let [theme-name (theme/get-theme-name theme-key)
        ;; Add padding to theme name to ensure menu width
        padded-name (format "%-20s" theme-name)]
    (action/action
     :name padded-name
     :icon (theme/key->icon theme-key)
     :handler (fn [_]
                ;; Only proceed if selecting a different theme
                (when-not (= (theme/get-current-theme) theme-key)
                  (when (theme/set-theme! theme-key)
                    (config/save-theme! theme-key)
                    ;; Recreate frame to ensure all theme changes apply properly
                    (reload-fn)))))))


(defn-spec ^:private create-theme-menu #(instance? javax.swing.JMenu %)
  "Create the Theme menu."
  [reload-fn fn?]
  (seesaw/menu 
   :text (i18n/tr :menu-view-theme)
   :icon (theme/key->icon :actions-group-theme)
   :items (map #(create-theme-action % reload-fn)
              (theme/get-available-themes))))

(defn-spec ^:private create-language-menu #(instance? javax.swing.JMenu %)
  "Create the Language menu."
  [reload-fn fn?]
  (seesaw/menu 
   :text (i18n/tr :menu-view-language)
   :icon (theme/key->icon :icon-languages)
   :items [(create-language-action :english "English" reload-fn)
           (create-language-action :ukrainian "Українська" reload-fn)]))

(defn-spec ^:private create-menu-bar #(instance? javax.swing.JMenuBar %)
  "Create the application menu bar."
  [reload-fn fn?]
  (seesaw/menubar
   :items [(create-theme-menu reload-fn)
           (create-language-menu reload-fn)]))

(defn-spec ^:private create-main-content #(instance? javax.swing.JPanel %)
  "Create the main content panel."
  []
  (seesaw/border-panel
   :north (control-panel/create)
   :center (log-table/create)))

(defn-spec ^:private add-window-close-handler! any?
  "Add window close handler for proper cleanup."
  [frame ::frame]
  (.addWindowListener ^javax.swing.JFrame frame
    (proxy [WindowAdapter] []
      (windowClosing [_]
        (println "Shutting down PotatoClient...")
        (try
          (process/cleanup-all-processes)
          (log-writer/stop-logging!)
          (catch Exception e
            (println "Error during shutdown:" (.getMessage e))))
        (System/exit 0)))))

(defn-spec preserve-window-state ::window-state
  "Extract window state for restoration."
  [frame ::frame]
  {:bounds (.getBounds ^javax.swing.JFrame frame)
   :extended-state (.getExtendedState ^javax.swing.JFrame frame)})

(defn-spec restore-window-state! ::frame
  "Restore window state to a frame."
  [frame ::frame, state ::window-state]
  (doto ^javax.swing.JFrame frame
    (.setBounds ^java.awt.Rectangle (:bounds state))
    (.setExtendedState ^Integer (:extended-state state))))

(defn-spec ^:private ensure-on-edt fn?
  "Ensure a function runs on the Event Dispatch Thread."
  [f fn?]
  (fn [& args]
    (if (javax.swing.SwingUtilities/isEventDispatchThread)
      (apply f args)
      (seesaw/invoke-now (apply f args)))))

(defn-spec create-main-frame ::frame
  "Create a new main application frame with clean state.
  
  This function creates a fresh frame instance ensuring all components
  are properly initialized, including theme-aware icons. It accepts
  parameters for version info and optional window state for restoration.
  
  IMPORTANT: This should be called on the Event Dispatch Thread."
  [params ::frame-params]
  (let [{:keys [version build-type window-state]} params
        ;; Preload icons before creating UI components
        _ (theme/preload-theme-icons!)
        frame-ref (atom nil)
        reload-fn (ensure-on-edt
                   (fn [] 
                     (when-let [old-frame @frame-ref]
                       (let [state (preserve-window-state old-frame)
                             new-params (assoc params :window-state state)]
                         (.dispose ^javax.swing.JFrame old-frame)
                         ;; Create new frame on EDT
                         (seesaw/invoke-later
                          (let [new-frame (create-main-frame new-params)]
                            (reset! frame-ref new-frame)
                            (seesaw/show! new-frame)))))))
        title (format "%s v%s [%s]" 
                     (i18n/tr :app-title)
                     version
                     build-type)
        frame (seesaw/frame
               :title title
               :icon (clojure.java.io/resource "main.png")
               :on-close :nothing
               :size [800 :by 600]
               :content (create-main-content))]
    
    ;; Store frame reference for reload function
    (reset! frame-ref frame)
    
    ;; Set up menu bar (icons should already be loaded)
    (.setJMenuBar ^javax.swing.JFrame frame (create-menu-bar reload-fn))
    
    ;; Add window close handler
    (add-window-close-handler! frame)
    
    ;; Restore window state if provided
    (when window-state
      (restore-window-state! frame window-state))
    
    ;; Log frame creation completion in dev mode
    (when-not (or (System/getProperty "potatoclient.release")
                  (System/getenv "POTATOCLIENT_RELEASE"))
      (log/log-info "UI" "Main frame created and configured"))
    
    frame))