(ns potatoclient.ui.main-frame
  "Main application frame construction and management.

  Provides a clean-slate constructor for the main window that ensures
  proper initialization of all UI elements including theme-aware icons."
  (:require [clojure.java.io :as io]
            [potatoclient.config :as config]
            [potatoclient.i18n :as i18n]
            [potatoclient.logging :as logging]
            [potatoclient.runtime :as runtime]
            [potatoclient.state :as state]
            [potatoclient.theme :as theme]
            [potatoclient.ui.menu-bar :as menu-bar]
            [potatoclient.ui.status-bar.core :as status-bar]
            [potatoclient.ui.tabs :as tabs]
            [potatoclient.ui.tabs-windows :as tabs-windows]
            [seesaw.core :as seesaw])
  (:import (javax.swing JFrame JPanel)
           (java.awt BorderLayout)))

(def version "Version schema for validation." :string)

(defn- create-main-content
  "Create the main content panel for the frame.
  This includes tabs and status bar."
  {:malli/schema [:=> [:cat [:fn #(instance? JFrame %)]] [:fn #(instance? JPanel %)]]}
  [parent-frame]
  (let [tabs-panel (tabs/create-default-tabs parent-frame)
        status-bar (status-bar/create)
        main-panel (JPanel. (BorderLayout.))]
    (.add main-panel tabs-panel BorderLayout/CENTER)
    (.add main-panel status-bar BorderLayout/SOUTH)
    main-panel))

(defn- add-window-close-handler!
  "Add window close handler for proper cleanup."
  {:malli/schema [:=> [:cat [:fn {:error/message "must be a JFrame"} (partial instance? JFrame)]] :nil]}
  [frame]
  (seesaw/listen frame :window-closing
                 (fn [_]
                   (logging/log-info {:msg "Shutting down PotatoClient..."})
                   (future
                     (try
          ;; Close all detached windows first
                       (tabs-windows/close-all-windows!)
                       (logging/log-debug {:msg "Closed all detached windows"})

          ;; Clean up bindings
                       (state/cleanup-seesaw-bindings!)
                       (logging/log-debug {:msg "Cleaned up all seesaw bindings"})

          ;; Save configuration
                       (let [current-config {:theme (theme/get-current-theme)
                                             :locale (state/get-locale)
                                             :url-history (config/get-url-history)}]
                         (config/save-config! current-config))

          ;; Clean up stream processes
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

      ;; Fallback timeout
                   (future
                     (Thread/sleep 3000)
                     (println "Force exiting due to shutdown timeout...")
                     (System/exit 1))))
  nil)

(defn create-main-frame
  "Create a new main application frame with clean state.

  This function creates a fresh frame instance ensuring all components
  are properly initialized, including theme-aware icons. It accepts
  parameters for version info and optional window state for restoration.

  IMPORTANT: This should be called on the Event Dispatch Thread."
  {:malli/schema [:=> [:cat :map] [:fn {:error/message "must be a JFrame"} (partial instance? JFrame)]]}
  [params]
  (let [{:keys [version build-type window-state]} params
        _ (theme/preload-theme-icons!)
        frame-cons (fn [state] (create-main-frame (assoc params :window-state state)))
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
                :size [1200 :by 800])]

    ;; Set content - pass frame as parent for detachable windows
    (seesaw/config! frame :content (create-main-content frame))

    ;; Set menu bar
    (seesaw/config! frame
                    :menubar (menu-bar/create-menubar
                               {:reload-fn frame-cons
                                :parent frame
                                :include-stream-buttons? true
                                :include-help? true
                                :include-theme? true
                                :include-language? true}))

    (add-window-close-handler! frame)

    (when window-state
      (menu-bar/restore-window-state! frame window-state))

    ;; Set initial status
    (require '[potatoclient.ui.status-bar.messages :as status-msg])
    ((resolve 'status-msg/set-ready!))

    (when-not (runtime/release-build?)
      (logging/log-info {:id :user/frame-created
                         :msg "Main frame created and configured"}))
    frame))
