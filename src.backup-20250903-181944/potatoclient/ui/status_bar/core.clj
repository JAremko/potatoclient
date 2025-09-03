(ns potatoclient.ui.status-bar.core
  "Core status bar component."
  (:require
            [malli.core :as m]
    [clojure.string :as str]
    [potatoclient.i18n :as i18n]
    [potatoclient.state :as state]
    [potatoclient.ui.debounce :as debounce]
    [potatoclient.ui.status-bar.helpers :as helpers]
    [potatoclient.ui.status-bar.messages :as msg]
    [seesaw.bind :as bind]
    [seesaw.color :as color]
    [seesaw.core :as seesaw])
  (:import (javax.swing JPanel)
           (java.awt Toolkit)
           (java.awt.datatransfer StringSelection)))

;; ============================================================================
;; Constants
;; ============================================================================

(def ^:private status-update-delay-ms
  "Delay in milliseconds for debouncing status bar updates."
  100)

(def ^:private initial-status-delay-ms
  "Delay in milliseconds before setting initial status to ready."
  100)

;; ============================================================================
;; Error Dialog
;; ============================================================================

(defn- show-error-dialog
  "Show dialog with error details."
  [parent]
  (when-let [error (helpers/get-last-error)]
    (let [error-text (str "Error: " (:message error) "\n\n"
                          "Stack Trace:\n" (:stack-trace error))
          text-area (seesaw/text :multi-line? true
                                 :editable? false
                                 :text error-text
                                 :font {:name "Monospaced" :size 12})
          scroll-pane (seesaw/scrollable text-area
                                         :preferred-size [600 :by 400])
          copy-button (seesaw/button
                        :text (i18n/tr :error-dialog-copy)
                        :listen [:action (fn [_]
                                           (let [clipboard (.getSystemClipboard (Toolkit/getDefaultToolkit))
                                                 selection (StringSelection. error-text)]
                                             (.setContents clipboard selection nil)
                                             (msg/set-info! (i18n/tr :error-dialog-copied))))])
          close-button (seesaw/button
                         :text (i18n/tr :error-dialog-close)
                         :listen [:action (fn [e]
                                            (seesaw/dispose! (seesaw/to-root e)))])
          button-panel (seesaw/horizontal-panel
                         :items [copy-button close-button])]
      (seesaw/dialog
        :parent parent
        :title (i18n/tr :error-dialog-title)
        :content (seesaw/border-panel
                   :center scroll-pane
                   :south button-panel
                   :vgap 5
                   :border 5)
        :on-close :dispose
        :modal? false
        :size [650 :by 450])))
  nil) 
 (m/=> show-error-dialog [:=> [:cat :any] :nil])

;; ============================================================================
;; Status Bar Creation
;; ============================================================================

(defn create
  "Create status bar component bound to app state."
  []
  (let [;; Create UI components with default values first
        icon-label (seesaw/label :icon (helpers/get-status-icon :info))
        text-field (seesaw/label :text (i18n/tr :status-ready)
                                 :foreground (helpers/get-status-color :info))
        ;; Use flow-panel with align :left for left alignment
        status-panel (seesaw/flow-panel
                       :align :left
                       :hgap 5
                       :items [icon-label text-field])
        ;; Create a debounced atom for status updates
        ;; This prevents excessive updates from rapid state changes
        status-debounced (debounce/debounce-atom state/app-state status-update-delay-ms)]

    ;; Bind to debounced status for UI updates
    (bind/bind
      status-debounced
      (bind/transform #(get-in % [:ui :status]))
      (bind/tee
        ;; Update icon
        (bind/bind
          (bind/transform #(helpers/get-status-icon (:type % :info)))
          (bind/property icon-label :icon))
        ;; Update text color
        (bind/bind
          (bind/transform #(helpers/get-status-color (:type % :info)))
          (bind/property text-field :foreground))
        ;; Update text - show "Ready" if empty
        (bind/bind
          (bind/transform #(let [msg (:message % "")]
                             (if (str/blank? msg)
                               (i18n/tr :status-ready)
                               msg)))
          (bind/property text-field :text))
        ;; Make clickable if error
        (bind/bind
          (bind/transform #(= (:type % :info) :error))
          (bind/b-do [clickable?]
                     (.setCursor status-panel
                                 (if clickable?
                                   (java.awt.Cursor/getPredefinedCursor java.awt.Cursor/HAND_CURSOR)
                                   (java.awt.Cursor/getDefaultCursor)))))))

    ;; Add click listener for error details
    (seesaw/listen status-panel :mouse-clicked
                   (fn [e]
                     (when (= (:type (get-in @state/app-state [:ui :status]) {:type :info}) :error)
                       (-> (show-error-dialog (seesaw/to-root e))
                           seesaw/pack!
                           seesaw/show!))))

    ;; Set initial status after a small delay to ensure bindings are ready
    (let [current-status (get-in @state/app-state [:ui :status] {:message "" :type :info})]
      (when (str/blank? (:message current-status))
        (let [timer (javax.swing.Timer. initial-status-delay-ms
                                        (reify java.awt.event.ActionListener
                                          (actionPerformed [_ _]
                                            (msg/set-ready!))))]
          (.setRepeats timer false)
          (.start timer))))

    ;; Create the panel with left-aligned content
    (seesaw/vertical-panel
      :items [(seesaw/separator :orientation :horizontal)
              status-panel]))) 
 (m/=> create [:=> [:cat] [:fn {:error/message "must be a Swing panel"} (partial instance? JPanel)]])