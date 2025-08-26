(ns potatoclient.ui.menu-bar
  "Reusable menu bar creation for application frames."
  (:require [clojure.java.io :as io]
            [clojure.string :as str]
            [com.fulcrologic.guardrails.malli.core :refer [=> >defn >defn-]]
            [potatoclient.config :as config]
            [potatoclient.i18n :as i18n]
            [potatoclient.logging :as logging]
            [potatoclient.runtime :as runtime]
            [potatoclient.state :as state]
            [potatoclient.theme :as theme]
            [potatoclient.ui.log-viewer :as log-viewer]
            [seesaw.action :as action]
            [seesaw.bind :as bind]
            [seesaw.core :as seesaw])
  (:import (java.awt Rectangle)
           (javax.swing Box JFrame)))

;; Namespace for UI binding watcher keys
(def ^:private ui-watcher-prefix "potatoclient.ui.binding/")

;; Forward declaration
(declare cleanup-ui-watchers!)

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
      ;; Clean up all UI watchers before disposing frame
      (cleanup-ui-watchers!)
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
        button (seesaw/toggle :action toggle-action)
        ;; Create a unique watcher key with our UI namespace prefix
        watcher-key (keyword (str ui-watcher-prefix "stream-" (name stream-key) "-" (gensym)))]
    
    ;; Add watcher with our namespaced key for easy cleanup later
    (add-watch state/app-state watcher-key
               (fn [_ _ _ new-state]
                 (let [process-key (case stream-key
                                     :heat :heat-video
                                     :day :day-video)
                       selected? (= :running (get-in new-state [:processes process-key :status]))]
                   (seesaw/config! button :selected? selected?))))
    button))

(>defn create-menubar
  "Create a menu bar with the specified options.
  
  Options map can contain:
  - :reload-fn - Function to reload the frame with new settings
  - :parent - Parent frame (for dialogs)
  - :include-stream-buttons? - Whether to include stream toggle buttons (default: false)
  - :include-help? - Whether to include the Help menu (default: true)
  - :include-theme? - Whether to include the Theme menu (default: true)
  - :include-language? - Whether to include the Language menu (default: true)"
  [opts]
  [map? => any?]
  (let [{:keys [reload-fn parent include-stream-buttons? include-help?
                include-theme? include-language?]
         :or {include-help? true
              include-theme? true
              include-language? true
              include-stream-buttons? false}} opts
        items (cond-> []
                include-theme? (conj (create-theme-menu reload-fn))
                include-language? (conj (create-language-menu reload-fn))
                (and include-help? parent) (conj (create-help-menu parent)))
        menubar (if include-stream-buttons?
                  (let [heat-button (doto (create-stream-toggle-button :heat)
                                      (seesaw/config! :text ""))
                        day-button (doto (create-stream-toggle-button :day)
                                     (seesaw/config! :text ""))]
                    (seesaw/menubar
                      :items (concat items
                                   [(Box/createHorizontalGlue)
                                    heat-button
                                    day-button])))
                  (seesaw/menubar :items items))]
    menubar))

(>defn cleanup-ui-watchers!
  "Clean up all UI-related watchers from app-state.
  This removes any watcher whose key starts with our UI namespace prefix.
  Should be called when frames are being disposed or reloaded."
  []
  [=> nil?]
  (let [watchers (.getWatches state/app-state)
        ui-watcher-keys (filter #(and (keyword? %)
                                      (str/starts-with? (namespace %) 
                                                       ui-watcher-prefix))
                                (keys watchers))]
    (doseq [watcher-key ui-watcher-keys]
      (remove-watch state/app-state watcher-key)
      (logging/log-debug {:msg "Removed UI watcher"
                         :key watcher-key}))
    (when (seq ui-watcher-keys)
      (logging/log-debug {:msg "Cleaned up UI watchers"
                         :count (count ui-watcher-keys)})))
  nil)

(>defn cleanup-menubar!
  "Clean up all bindings associated with a menu bar.
  For backward compatibility, but just calls cleanup-ui-watchers!."
  [menubar]
  [any? => nil?]
  (cleanup-ui-watchers!))