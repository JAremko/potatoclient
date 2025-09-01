(ns potatoclient.ui.main-frame
  "Main application frame construction and management.

  Provides a clean-slate constructor for the main window that ensures
  proper initialization of all UI elements including theme-aware icons."
  (:require [clojure.java.io :as io]
            [com.fulcrologic.guardrails.malli.core :refer [=> >defn >defn-]]
            [potatoclient.config :as config]
            [potatoclient.i18n :as i18n]
            [potatoclient.logging :as logging]
            [potatoclient.runtime :as runtime]
            [potatoclient.state :as state]
            [potatoclient.theme :as theme]
            [potatoclient.ui.control-panel :as control-panel]
            [potatoclient.ui.menu-bar :as menu-bar]
            [seesaw.core :as seesaw])
  (:import (javax.swing JFrame JPanel)))

;; Additional schemas not in specs
(def version
  "Version schema for validation."
  :string)

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
                       ;; Clean up all seesaw bindings (menu bar and control panel)
                       (state/cleanup-seesaw-bindings!)
                       (logging/log-debug {:msg "Cleaned up all seesaw bindings"})
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

    ;; Set up menu bar using the new menu-bar namespace
    (seesaw/config! frame :menubar (menu-bar/create-menubar
                                      {:reload-fn frame-cons
                                       :parent frame
                                       :include-stream-buttons? true
                                       :include-help? true
                                       :include-theme? true
                                       :include-language? true}))

    ;; Add window close handler
    (add-window-close-handler! frame)

    ;; Restore window state if provided
    (when window-state
      (menu-bar/restore-window-state! frame window-state))

    ;; Log frame creation completion in dev mode
    (when-not (runtime/release-build?)
      (logging/log-info
        {:id ::frame-created
         :msg "Main frame created and configured"}))

    frame))
