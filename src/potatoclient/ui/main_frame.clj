(ns potatoclient.ui.main-frame
  "Main application frame construction and management.
  
  Provides a clean-slate constructor for the main window that ensures
  proper initialization of all UI elements including theme-aware icons."
  (:require [seesaw.core :as seesaw]
            [seesaw.action :as action]
            [potatoclient.state :as state]
            [potatoclient.process :as process]
            [potatoclient.ui.control-panel :as control-panel]
            [potatoclient.ui.log-viewer :as log-viewer]
            [potatoclient.config :as config]
            [potatoclient.i18n :as i18n]
            [potatoclient.theme :as theme]
            [potatoclient.runtime :as runtime]
            [potatoclient.logging :as logging]
            [potatoclient.ipc :as ipc]
            [malli.core :as m]
            [potatoclient.specs :as specs])
  (:import [javax.swing JFrame]
           [java.awt.event WindowAdapter]))

;; Additional schemas not in specs
(def display-name :string)
(def version :string)
(def build-type :string)
(def frame-params
  [:map
   [:version version]
   [:build-type build-type]
   [:window-state {:optional true} specs/window-state]])

(defn preserve-window-state
  "Extract window state for restoration."
  [frame]
  {:bounds (.getBounds ^javax.swing.JFrame frame)
   :extended-state (.getExtendedState ^javax.swing.JFrame frame)})

(defn restore-window-state!
  "Restore window state to a frame."
  [frame state]
  (doto ^javax.swing.JFrame frame
    (.setBounds ^java.awt.Rectangle (:bounds state))
    (.setExtendedState ^Integer (:extended-state state))))

(defn- reload-frame!
  "Reload the frame following the ArcherBC2 pattern."
  [frame frame-cons]
  (seesaw/invoke-later
   (let [state (preserve-window-state frame)]
     (seesaw/config! frame :on-close :nothing)
     (.dispose ^javax.swing.JFrame frame)
     (let [new-frame (frame-cons state)]
       (seesaw/show! new-frame)))))

(defn- create-language-action
  "Create a language selection action."
  [lang-key display-name reload-fn]
  (let [flag-icon (case lang-key
                    :english (seesaw/icon (clojure.java.io/resource "flags/en.png"))
                    :ukrainian (seesaw/icon (clojure.java.io/resource "flags/ua.png"))
                    nil)]
    (action/action
     :name (str display-name "    ")
     :icon flag-icon
     :handler (fn [e]
                ;; Only proceed if selecting a different language
                (when-not (= (state/get-locale) lang-key)
                  (state/set-locale! lang-key)
                  (config/update-config! :locale lang-key)
                  (reload-frame! (seesaw/to-root e) reload-fn))))))

(defn- create-theme-action
  "Create a theme selection action."
  [theme-key reload-fn]
  (let [theme-i18n-key (theme/get-theme-i18n-key theme-key)
        theme-name (i18n/tr theme-i18n-key)]
    (action/action
     :name (str theme-name "    ")
     :icon (theme/key->icon theme-key)
     :handler (fn [e]
                ;; Only proceed if selecting a different theme
                (when-not (= (theme/get-current-theme) theme-key)
                  (when (theme/set-theme! theme-key)
                    (config/save-theme! theme-key)
                    (reload-frame! (seesaw/to-root e) reload-fn)))))))

(defn- create-theme-menu
  "Create the Theme menu."
  [reload-fn]
  (seesaw/menu
   :text (i18n/tr :menu-view-theme)
   :icon (theme/key->icon :actions-group-theme)
   :items (map #(create-theme-action % reload-fn)
               (theme/get-available-themes))))

(defn- create-language-menu
  "Create the Language menu."
  [reload-fn]
  (seesaw/menu
   :text (i18n/tr :menu-view-language)
   :icon (theme/key->icon :icon-languages)
   :items [(create-language-action :english "English" reload-fn)
           (create-language-action :ukrainian "Українська" reload-fn)]))

(defn- show-about-dialog
  "Show the About dialog."
  [parent]
  (let [version (try
                  (clojure.string/trim (slurp (clojure.java.io/resource "VERSION")))
                  (catch Exception _ "dev"))
        build-type (if (runtime/release-build?) "RELEASE" "DEVELOPMENT")]
    (javax.swing.JOptionPane/showMessageDialog
     parent
     (str (i18n/tr :about-text) "\n\n"
          (i18n/tr :app-version) ": " version " [" build-type "]")
     (i18n/tr :about-title)
     javax.swing.JOptionPane/INFORMATION_MESSAGE)))

(defn- open-logs-viewer
  "Open the log viewer window."
  []
  (log-viewer/show-log-viewer))

(defn- create-help-menu
  "Create the Help menu."
  [parent]
  (let [menu-items [(action/action
                     :name (i18n/tr :menu-help-about)
                     :icon (theme/key->icon :tab-icon-description)
                     :handler (fn [_] (show-about-dialog parent)))]
        menu-items (conj menu-items
                         (action/action
                          :name (i18n/tr :menu-help-view-logs)
                          :icon (theme/key->icon :file-open)
                          :handler (fn [_] (open-logs-viewer))))]
    (seesaw/menu
     :text (i18n/tr :menu-help)
     :icon (theme/key->icon :actions-group-menu)
     :items menu-items)))

(defn- create-stream-toggle-button
  "Create a stream toggle button for the menu bar."
  [stream-key]
  (let [stream-config {:heat {:endpoint "/ws/ws_rec_video_heat"
                              :tooltip "Heat Camera (900x720)"
                              :label-key :stream-thermal}
                       :day {:endpoint "/ws/ws_rec_video_day"
                             :tooltip "Day Camera (1920x1080)"
                             :label-key :stream-day}}
        {:keys [endpoint tooltip label-key]} (get stream-config stream-key)
        is-connected (atom (some? (state/get-stream stream-key)))
        button (seesaw/toggle
                :text (i18n/tr label-key)
                :icon (theme/key->icon stream-key)
                :selected? @is-connected
                :tip tooltip)]
    ;; Add action handler
    (seesaw/listen button :action
                   (fn [e]
                     (let [current-state @is-connected]
                       (if current-state
                         (do
                           (ipc/stop-stream stream-key)
                           (reset! is-connected false))
                         (do
                           (ipc/start-stream stream-key endpoint)
                           (reset! is-connected true))))))

    ;; Update button state when stream state changes
    (add-watch state/app-state
               (keyword (str "stream-button-" (name stream-key)))
               (fn [_ _ old-state new-state]
                 (let [old-stream (get old-state stream-key)
                       new-stream (get new-state stream-key)
                       connected? (some? new-stream)]
                   (when (not= (some? old-stream) connected?)
                     (reset! is-connected connected?)
                     (seesaw/invoke-later
                      (seesaw/config! button :selected? connected?))))))
    button))

(defn- create-menu-bar
  "Create the application menu bar."
  [reload-fn parent]
  (let [heat-button (create-stream-toggle-button :heat)
        day-button (create-stream-toggle-button :day)]
    (seesaw/menubar
     :items [(create-theme-menu reload-fn)
             (create-language-menu reload-fn)
             (create-help-menu parent)
             (seesaw/separator :orientation :vertical)
             ;; Remove text from toggle buttons to show only icons
             (seesaw/config! heat-button :text "")
             (seesaw/config! day-button :text "")])))

(defn- create-main-content
  "Create the main content panel."
  []
  ;; Since we removed the log view, just show the control panel
  (control-panel/create))

(defn- add-window-close-handler!
  "Add window close handler for proper cleanup."
  [frame]
  (.addWindowListener ^javax.swing.JFrame frame
                      (proxy [WindowAdapter] []
                        (windowClosing [_]
                          (println "Shutting down PotatoClient...")
                          (try
                            (process/cleanup-all-processes)
                            (logging/shutdown!)
                            (catch Exception e
                              (println "Error during shutdown:" (.getMessage e))))
                          (System/exit 0)))))

(defn- ensure-on-edt
  "Ensure a function runs on the Event Dispatch Thread."
  [f]
  (fn [& args]
    (if (javax.swing.SwingUtilities/isEventDispatchThread)
      (apply f args)
      (seesaw/invoke-now (apply f args)))))

(defn- ensure-on-edt-later
  "Ensure a function runs on the Event Dispatch Thread using invoke-later."
  [f]
  (fn [& args]
    (if (javax.swing.SwingUtilities/isEventDispatchThread)
      (apply f args)
      (seesaw/invoke-later (apply f args)))))

(defn create-main-frame
  "Create a new main application frame with clean state.
  
  This function creates a fresh frame instance ensuring all components
  are properly initialized, including theme-aware icons. It accepts
  parameters for version info and optional window state for restoration.
  
  IMPORTANT: This should be called on the Event Dispatch Thread."
  [params]
  (let [{:keys [version build-type window-state]} params
        ;; Preload icons before creating UI components
        _ (theme/preload-theme-icons!)
        ;; Create frame constructor that preserves window state
        frame-cons (fn [state]
                     (create-main-frame (assoc params :window-state state)))
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

    ;; Set up menu bar with frame constructor
    (.setJMenuBar ^javax.swing.JFrame frame (create-menu-bar frame-cons frame))

    ;; Add window close handler
    (add-window-close-handler! frame)

    ;; Restore window state if provided
    (when window-state
      (restore-window-state! frame window-state))

    ;; Log frame creation completion in dev mode
    (when-not (runtime/release-build?)
      (logging/log-info
       {:id ::frame-created
        :msg "Main frame created and configured"}))

    frame))

