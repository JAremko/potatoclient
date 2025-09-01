(ns potatoclient.ui.frames.main.core
  "Main application frame construction and management."
  (:require
    [clojure.java.io :as io]
    [potatoclient.config :as config]
    [potatoclient.i18n :as i18n]
    [potatoclient.logging :as logging]
    [potatoclient.runtime :as runtime]
    [potatoclient.state :as state]
    [potatoclient.theme :as theme]
    [potatoclient.ui.control-panel :as control-panel]
    [potatoclient.ui.menu-bar :as menu-bar]
    [potatoclient.ui.status-bar.core :as status-bar]
    [potatoclient.ui.tabs.core :as tabs]
    [seesaw.core :as seesaw])
  (:import (javax.swing JFrame JPanel)))

(def version "Version schema for validation." :string)

(defn- create-main-content
  "Create the main content panel with tabs and status bar."
  {:malli/schema [:=> [:cat] [:fn {:error/message "must be a Swing panel"} (partial instance? JPanel)]]}
  []
  (seesaw/border-panel
    :center (tabs/create)
    :south (status-bar/create)))

(defn- add-window-close-handler!
  "Add window close handler for proper cleanup."
  {:malli/schema [:=> [:cat [:fn {:error/message "must be a JFrame"} (partial instance? JFrame)]] :nil]}
  [frame]
  (seesaw/listen frame :window-closing
                 (fn [_]
                   (logging/log-info {:msg "Shutting down PotatoClient..."})
                   (future
                     (try
                       (state/cleanup-seesaw-bindings!)
                       (logging/log-debug {:msg "Cleaned up all seesaw bindings"})
                       (let [current-config {:theme (theme/get-current-theme)
                                             :locale (state/get-locale)
                                             :url-history (config/get-url-history)}]
                         (config/save-config! current-config))
                       (let [stream-processes (state/get-all-stream-processes)
                             _ (reduce-kv (fn [m k v]
                                            (let [stream-key (case k
                                                               :heat-video :heat
                                                               :day-video :day
                                                               k)]
                                              (assoc m stream-key v)))
                                          {}
                                          stream-processes)]
                         nil)
                       (logging/shutdown!)
                       (catch Exception e
                         (logging/log-error {:msg (str "Error during shutdown: " (.getMessage e))}))
                       (finally
                         (System/exit 0))))
                   (future
                     (Thread/sleep 3000)
                     (println "Force exiting due to shutdown timeout...")
                     (System/exit 1))))
  nil)

(defn create
  "Create a new main application frame with clean state."
  {:malli/schema [:=> [:cat :map] [:fn {:error/message "must be a JFrame"} (partial instance? JFrame)]]}
  [params]
  (let [{:keys [version build-type window-state]} params
        _ (theme/preload-theme-icons!)
        frame-cons (fn [state] (create (assoc params :window-state state)))
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
    (seesaw/config! frame
                    :menubar (menu-bar/create-menubar
                               {:reload-fn frame-cons
                                :parent frame
                                :include-stream-buttons? true
                                :include-help? true
                                :include-theme? true
                                :include-language? true}))
    (add-window-close-handler! frame)
    (if window-state
      ;; Restore previous position/size on reload
      (menu-bar/restore-window-state! frame window-state)
      ;; Center on screen for initial display
      (.setLocationRelativeTo frame nil))
    (when-not (runtime/release-build?)
      (logging/log-info {:id :user/frame-created
                         :msg "Main frame created and configured"}))
    frame))