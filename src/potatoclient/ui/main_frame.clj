(ns potatoclient.ui.main-frame
  "Main application frame construction and management.

  Provides a clean-slate constructor for the main window that ensures
  proper initialization of all UI elements including theme-aware icons."
  (:require [clojure.java.io :as io]
            [clojure.string :as str]
            [com.fulcrologic.guardrails.malli.core :refer [=> >defn >defn-]]
            [potatoclient.config :as config]
            [potatoclient.i18n :as i18n]
            [potatoclient.logging :as logging]
            [potatoclient.runtime :as runtime]
            [potatoclient.state :as state]
            [potatoclient.theme :as theme]
            [potatoclient.ui.control-panel :as control-panel]
            [potatoclient.ui.log-viewer :as log-viewer]
            [seesaw.action :as action]
            [seesaw.bind :as bind]
            [seesaw.core :as seesaw])
  (:import (java.awt Rectangle)
           (javax.swing Box JFrame JPanel)))

;; Additional schemas not in specs
(def version
  "Version schema for validation."
  :string)

(>defn preserve-window-state
  "Extract window state for restoration."
  [frame]
  [[:fn {:error/message "must be a JFrame"}
    #(instance? JFrame %)] => :potatoclient.ui-specs/window-state]
  (let [^Rectangle bounds (.getBounds frame)]
    {:bounds {:x (.x bounds)
              :y (.y bounds)
              :width (.width bounds)
              :height (.height bounds)}
     :extended-state (.getExtendedState frame)}))

(>defn restore-window-state!
  "Restore window state to a frame."
  [frame state]
  [[:fn {:error/message "must be a JFrame"}
    #(instance? JFrame %)]
   :potatoclient.ui-specs/window-state
   => [:fn {:error/message "must be a JFrame"}
       #(instance? JFrame %)]]
  (doto frame
    (.setBounds (when-let [{:keys [x y width height]} (:bounds state)]
                  (Rectangle. x y width height)))
    (.setExtendedState (:extended-state state))))

(>defn- reload-frame!
  "Reload the frame following the ArcherBC2 pattern."
  [frame frame-cons]
  [[:fn {:error/message "must be a JFrame"}
    #(instance? JFrame %)]
   ifn? => nil?]
  (seesaw/invoke-later
    (let [state (preserve-window-state frame)]
      (seesaw/config! frame :on-close :nothing)
      (seesaw/dispose! frame)
      (let [new-frame (frame-cons state)]
        (seesaw/show! new-frame)))))

(>defn- create-language-action
  "Create a language selection action."
  [lang-key display-name reload-fn]
  [:potatoclient.ui-specs/locale string? ifn? => any?]
  (let [flag-icon (case lang-key
                    :english (seesaw/icon (io/resource "flags/en.png"))
                    :ukrainian (seesaw/icon (io/resource "flags/ua.png"))
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

(>defn- create-theme-action
  "Create a theme selection action."
  [theme-key reload-fn]
  [:potatoclient.ui-specs/theme-key ifn? => any?]
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

(>defn- create-theme-menu
  "Create the Theme menu."
  [reload-fn]
  [ifn? => any?]
  (seesaw/menu
    :text (i18n/tr :menu-view-theme)
    :icon (theme/key->icon :actions-group-theme)
    :items (map #(create-theme-action % reload-fn)
                (theme/get-available-themes))))

(>defn- create-language-menu
  "Create the Language menu."
  [reload-fn]
  [ifn? => any?]
  (seesaw/menu
    :text (i18n/tr :menu-view-language)
    :icon (theme/key->icon :icon-languages)
    :items [(create-language-action :english "English" reload-fn)
            (create-language-action :ukrainian "Українська" reload-fn)]))

(>defn- show-about-dialog
  "Show the About dialog."
  [parent]
  [[:fn {:error/message "must be a JFrame"}
    #(instance? JFrame %)] => nil?]
  (let [version (try
                  (str/trim (slurp (io/resource "VERSION")))
                  (catch Exception _ "dev"))
        build-type (if (runtime/release-build?) "RELEASE" "DEVELOPMENT")]
    (seesaw/alert parent
                  (str (i18n/tr :about-text) "\n\n"
                       (i18n/tr :app-version) ": " version " [" build-type "]")
                  :title (i18n/tr :about-title)
                  :type :info)))

(>defn- open-logs-viewer
  "Open the log viewer window."
  []
  [=> any?]
  (log-viewer/show-log-viewer))

(>defn- create-help-menu
  "Create the Help menu."
  [parent]
  [[:fn {:error/message "must be a JFrame"}
    #(instance? JFrame %)] => any?]
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

(>defn- create-stream-toggle-button
  "Create a stream toggle button for the menu bar."
  [stream-key]
  [:potatoclient.ui-specs/stream-key => any?]
  (let [stream-config {:heat {:endpoint "/ws/ws_rec_video_heat"
                              :tooltip "Heat Camera (900x720)"
                              :label-key :stream-thermal}
                       :day {:endpoint "/ws/ws_rec_video_day"
                             :tooltip "Day Camera (1920x1080)"
                             :label-key :stream-day}}
        {:keys [_ tooltip label-key]} (get stream-config stream-key)
        toggle-action (action/action
                        :name (i18n/tr label-key)
                        :icon (theme/key->icon stream-key)
                        :tip tooltip
                        :handler (fn [e]
                                   (logging/log-debug
                                     {:msg (str "Stream toggle clicked (noop): " stream-key)
                                      :stream stream-key
                                      :selected? (seesaw/config (seesaw/to-widget e) :selected?)})))
        button (seesaw/toggle :action toggle-action)]

    ;; Bind button state to app-state using seesaw.bind
    (bind/bind state/app-state
               (bind/transform (fn [s]
                                 (let [process-key (case stream-key
                                                     :heat :heat-video
                                                     :day :day-video)]
                                   (= :running (get-in s [:processes process-key :status])))))
               (bind/property button :selected?))
    button))

(>defn- create-menu-bar
  "Create the application menu bar."
  [_reload-fn _parent]
  [ifn? [:fn {:error/message "must be a JFrame"}
         #(instance? JFrame %)] => any?]
  (let [heat-button (doto (create-stream-toggle-button :heat)
                      (seesaw/config! :text ""))
        day-button (doto (create-stream-toggle-button :day)
                     (seesaw/config! :text ""))]
    (seesaw/menubar
      :items [;; Left side - menus
              (create-theme-menu _reload-fn)
              (create-language-menu _reload-fn)
              (create-help-menu _parent)
             ;; Use horizontal glue to push buttons to the right
              (Box/createHorizontalGlue)
             ;; Right side - stream buttons
              heat-button
              day-button])))

(>defn- create-main-content
  "Create the main content panel."
  []
  [=> [:fn {:error/message "must be a Swing panel"}
       #(instance? JPanel %)]]
  ;; Since we removed the log view, just show the control panel
  (control-panel/create))

(>defn- add-window-close-handler!
  "Add window close handler for proper cleanup."
  [frame]
  [[:fn {:error/message "must be a JFrame"}
    #(instance? JFrame %)] => nil?]
  (seesaw/listen frame
                 :window-closing
                 (fn [_]
                   (logging/log-info {:msg "Shutting down PotatoClient..."})
                   ;; Run cleanup in a separate thread with timeout
                   (future
                     (try
                       ;; Save current config state
                       (let [current-config {:theme (theme/get-current-theme)
                                             :locale (state/get-locale)
                                             :url-history (config/get-url-history)}]
                         (config/save-config! current-config))
                       ;; Stop video stream processes
                       (let [stream-processes (state/get-all-stream-processes)
                             ;; Transform keys from :heat-video/:day-video to :heat/:day
                             _ (reduce-kv (fn [m k v]
                                                                (let [stream-key (case k
                                                                                   :heat-video :heat
                                                                                   :day-video :day
                                                                                   k)]
                                                                  (assoc m stream-key v)))
                                                              {}
                                                              stream-processes)]
                         ;; Stream cleanup removed - noop for now
                         nil)
                       (logging/shutdown!)
                       (catch Exception e
                         (logging/log-error {:msg (str "Error during shutdown: " (.getMessage e))}))
                       (finally
                         ;; Force exit after cleanup attempt
                         (System/exit 0))))
                   ;; Also schedule a forced exit after 3 seconds if cleanup hangs
                   (future
                     (Thread/sleep 3000)
                     (println "Force exiting due to shutdown timeout...")
                     (System/exit 1))))
  nil)

(>defn create-main-frame
  "Create a new main application frame with clean state.

  This function creates a fresh frame instance ensuring all components
  are properly initialized, including theme-aware icons. It accepts
  parameters for version info and optional window state for restoration.

  IMPORTANT: This should be called on the Event Dispatch Thread."
  [params]
  [map? => [:fn {:error/message "must be a JFrame"}
            #(instance? JFrame %)]]
  (let [{:keys [version build-type window-state]} params
        ;; Preload icons before creating UI components
        _ (theme/preload-theme-icons!)
        ;; Create frame constructor that preserves window state
        frame-cons (fn [state]
                     (create-main-frame (assoc params :window-state state)))
        domain (config/get-domain)
        title (format "%s v%s [%s] - %s"
                      (i18n/tr :app-title)
                      version
                      build-type
                      domain)
        frame (seesaw/frame
                :title title
                :icon (io/resource "main.png")
                :on-close :nothing
                :size [800 :by 600]
                :content (create-main-content))]

    ;; Set up menu bar
    (seesaw/config! frame :menubar (create-menu-bar frame-cons frame))

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
