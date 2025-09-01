(ns potatoclient.ui.status-bar.core
  "Core status bar component."
  (:require
    [potatoclient.i18n :as i18n]
    [potatoclient.state :as state]
    [potatoclient.ui.status-bar.helpers :as helpers]
    [potatoclient.ui.status-bar.messages :as msg]
    [seesaw.bind :as bind]
    [seesaw.color :as color]
    [seesaw.core :as seesaw])
  (:import (javax.swing JPanel)
           (java.awt Toolkit)
           (java.awt.datatransfer StringSelection)))

;; ============================================================================
;; Error Dialog
;; ============================================================================

(defn- show-error-dialog
  "Show dialog with error details."
  {:malli/schema [:=> [:cat :any] :nil]}
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

;; ============================================================================
;; Status Bar Creation
;; ============================================================================

(defn create
  "Create status bar component bound to app state."
  {:malli/schema [:=> [:cat] [:fn {:error/message "must be a Swing panel"} (partial instance? JPanel)]]}
  []
  (let [icon-label (seesaw/label :icon (helpers/get-status-icon :info))
        text-field (seesaw/text :editable? false
                                :focusable? false
                                :text ""
                                :foreground (color/default-color "TextField.foreground"))
        status-panel (seesaw/horizontal-panel
                       :items [icon-label text-field])]

    ;; Bind to status in app state
    (bind/bind
      state/app-state
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
        ;; Update text
        (bind/bind
          (bind/transform #(:message % ""))
          (bind/value text-field))
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

    ;; Create the panel
    (seesaw/vertical-panel
      :items [(seesaw/separator :orientation :horizontal)
              status-panel])))