(ns potatoclient.ui.menu-bar
  "Reusable menu bar creation for application frames."
  (:require [clojure.java.io :as io]
            [clojure.string :as str]
            [potatoclient.config :as config]
            [potatoclient.i18n :as i18n]
            [potatoclient.logging :as logging]
            [potatoclient.state :as state]
            [potatoclient.theme :as theme]
            [potatoclient.ui.help.menu :as help-menu]
            [potatoclient.ui.status-bar.messages :as status-msg]
            [potatoclient.ui.utils :as utils]
            [seesaw.action :as action]
            [seesaw.bind :as bind]
            [seesaw.core :as seesaw])
  (:import (java.awt Rectangle)
           (javax.swing Box JFrame)))

(defn preserve-window-state
  "Extract window state for restoration."
  {:malli/schema [:=> [:cat [:fn {:error/message "must be a JFrame"} (partial instance? JFrame)]] :potatoclient.ui-specs/window-state]}
  [frame]
  (let [^Rectangle bounds (.getBounds frame)]
    {:bounds {:x (.x bounds)
              :y (.y bounds)
              :width (.width bounds)
              :height (.height bounds)}
     :extended-state (.getExtendedState frame)}))

(defn restore-window-state!
  "Restore window state to a frame."
  {:malli/schema [:=> [:cat [:fn {:error/message "must be a JFrame"} (partial instance? JFrame)] :potatoclient.ui-specs/window-state] [:fn {:error/message "must be a JFrame"} (partial instance? JFrame)]]}
  [frame state]
  (doto frame
    (.setBounds (when-let [{:keys [x y width height]} (:bounds state)]
                  (Rectangle. x y width height)))
    (.setExtendedState (:extended-state state))))

(defn- reload-frame!
  "Reload the frame following the ArcherBC2 pattern."
  {:malli/schema [:=> [:cat [:fn {:error/message "must be a JFrame"} (partial instance? JFrame)] :ifn] :nil]}
  [frame frame-cons]
  (seesaw/invoke-later
    (let [window-state (preserve-window-state frame)]
      ;; Clean up all seesaw bindings before disposing frame
      (state/cleanup-seesaw-bindings!)
      (seesaw/config! frame :on-close :nothing)
      (seesaw/dispose! frame)
      (let [new-frame (frame-cons window-state)]
        (seesaw/show! new-frame)))))

(defn- create-language-action
  "Create a language selection action."
  {:malli/schema [:=> [:cat :potatoclient.ui-specs/locale :string :ifn] :any]}
  [lang-key display-name reload-fn]
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
                   (status-msg/set-language-changed! lang-key)
                   (reload-frame! (seesaw/to-root e) reload-fn))))))

(defn- create-theme-action
  "Create a theme selection action."
  {:malli/schema [:=> [:cat :potatoclient.ui-specs/theme-key :ifn] :any]}
  [theme-key reload-fn]
  (let [theme-i18n-key (theme/get-theme-i18n-key theme-key)
        theme-name (i18n/tr theme-i18n-key)]
    (action/action
      :name (str theme-name "    ")
      :icon (theme/key->icon theme-key)
      :handler (fn [e]
                 (when-not (= (theme/get-current-theme) theme-key)
                   (when (theme/set-theme! theme-key)
                     (config/save-theme! theme-key)
                     (status-msg/set-theme-changed! theme-key)
                     (reload-frame! (seesaw/to-root e) reload-fn)))))))

(defn- create-theme-menu
  "Create the Theme menu."
  {:malli/schema [:=> [:cat :ifn] :any]}
  [reload-fn]
  (seesaw/menu
    :text (i18n/tr :menu-view-theme)
    :icon (theme/key->icon :actions-group-theme)
    :items (map #(create-theme-action % reload-fn)
                (theme/get-available-themes))))

(defn- create-language-menu
  "Create the Language menu."
  {:malli/schema [:=> [:cat :ifn] :any]}
  [reload-fn]
  (seesaw/menu
    :text (i18n/tr :menu-view-language)
    :icon (theme/key->icon :icon-languages)
    :items [(create-language-action :english "English" reload-fn)
            (create-language-action :ukrainian "Українська" reload-fn)]))


(defn- create-stream-toggle-button
  "Create a stream toggle button for the menu bar."
  {:malli/schema [:=> [:cat :potatoclient.ui-specs/stream-key] :any]}
  [stream-key]
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
                                   (let [selected? (seesaw/config (seesaw/to-widget e) :selected?)]
                                     ;; Log for debugging
                                     (logging/log-debug
                                       {:msg (str "Stream toggle clicked: " stream-key)
                                        :stream stream-key
                                        :selected? selected?})

                                     ;; Actually start/stop the stream
                                     ;; The status bar will be updated by the stream system
                                     (try
                                       (require 'potatoclient.streams.core)
                                       (if selected?
                                         ((resolve 'potatoclient.streams.core/start-stream) stream-key)
                                         ((resolve 'potatoclient.streams.core/stop-stream) stream-key))
                                       (catch Exception ex
                                         (logging/log-error {:msg (str "Failed to toggle stream: " (.getMessage ex))
                                                             :stream stream-key})
                                         ;; Reset button state on error
                                         (seesaw/config! (seesaw/to-widget e) :selected? (not selected?)))))))
        button (seesaw/toggle :action toggle-action)
        process-key (case stream-key
                      :heat :heat-video
                      :day :day-video)]

    ;; Use seesaw's bind with debouncing to sync button state with app-state
    ;; This prevents rapid intermediate state changes from causing UI flicker
    ;; Cleanup happens automatically via state/cleanup-seesaw-bindings!
    (bind/bind
      state/app-state
      (bind/some
        (utils/mk-debounced-transform
          (fn [state]
            (= :running (get-in state [:processes process-key :status])))))
      (bind/property button :selected?))

    button))

(defn create-menubar
  "Create a menu bar with the specified options.

  Options map can contain:
  - :reload-fn - Function to reload the frame with new settings
  - :parent - Parent frame (for dialogs)
  - :include-stream-buttons? - Whether to include stream toggle buttons (default: false)
  - :include-help? - Whether to include the Help menu (default: true)
  - :include-theme? - Whether to include the Theme menu (default: true)
  - :include-language? - Whether to include the Language menu (default: true)"
  {:malli/schema [:=> [:cat :map] :any]}
  [opts]
  (let [{:keys [reload-fn parent include-stream-buttons? include-help?
                include-theme? include-language?]
         :or {include-help? true
              include-theme? true
              include-language? true
              include-stream-buttons? false}} opts
        items (cond-> []
                include-theme? (conj (create-theme-menu reload-fn))
                include-language? (conj (create-language-menu reload-fn))
                (and include-help? parent) (conj (help-menu/create-help-menu {:parent parent
                                                                                :include-logs? true})))
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

(defn cleanup-menubar!
  "Clean up all bindings associated with a menu bar.
  This is now handled automatically by state/cleanup-seesaw-bindings!
  Kept for backward compatibility."
  {:malli/schema [:=> [:cat :any] :nil]}
  [_]
  (state/cleanup-seesaw-bindings!))
