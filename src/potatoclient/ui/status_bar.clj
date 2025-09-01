(ns potatoclient.ui.status-bar
  "Status bar component for displaying system messages and errors."
  (:require
    [clojure.string :as str]
    [potatoclient.i18n :as i18n]
    [potatoclient.state :as state]
    [potatoclient.theme :as theme]
    [seesaw.bind :as bind]
    [seesaw.color :as color]
    [seesaw.core :as seesaw])
  (:import (javax.swing JPanel)
           (java.awt Toolkit)
           (java.awt.datatransfer StringSelection)
           (java.io StringWriter PrintWriter)))

;; ============================================================================
;; Status Icons
;; ============================================================================

(defn- get-status-icon
  "Get icon for status type."
  {:malli/schema [:=> [:cat :keyword] [:maybe :any]]}
  [status-type]
  (case status-type
    :info (theme/get-icon :status-bar-icon-good)
    :warning (theme/get-icon :status-bar-icon-warn)
    :error (theme/get-icon :status-bar-icon-bad)
    (theme/get-icon :status-bar-icon-good)))

(defn- get-status-color
  "Get color for status type."
  {:malli/schema [:=> [:cat :keyword] :any]}
  [status-type]
  (case status-type
    :error :red
    :warning :orange
    :info (color/default-color "TextField.foreground")
    ;; Default for invalid types
    (color/default-color "TextField.foreground")))

;; ============================================================================
;; Error Storage
;; ============================================================================

(defonce ^{:doc "Stores the last error for display"} last-error-atom
  (atom nil))

;; ============================================================================
;; Forward declarations
;; ============================================================================

(declare set-info!)

;; ============================================================================
;; Status Bar Creation
;; ============================================================================

(defn- show-error-dialog
  "Show dialog with error details."
  {:malli/schema [:=> [:cat :any] :nil]}
  [parent]
  (when-let [error @last-error-atom]
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
                                          (set-info! (i18n/tr :error-dialog-copied))))])
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

(defn create
  "Create status bar component bound to app state."
  {:malli/schema [:=> [:cat] [:fn {:error/message "must be a Swing panel"} (partial instance? JPanel)]]}
  []
  (let [icon-label (seesaw/label :icon (get-status-icon :info))
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
          (bind/transform #(get-status-icon (:type % :info)))
          (bind/property icon-label :icon))
        ;; Update text color
        (bind/bind
          (bind/transform #(get-status-color (:type % :info)))
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

;; ============================================================================
;; Status Updates
;; ============================================================================

(defn set-status!
  "Set status message with type."
  {:malli/schema [:=> [:cat [:maybe :string] :keyword] :map]}
  [message type]
  (swap! state/app-state assoc-in [:ui :status] {:message (or message "")
                                                  :type type}))

(defn set-info!
  "Set info status message."
  {:malli/schema [:=> [:cat [:maybe :string]] :map]}
  [message]
  (set-status! message :info))

(defn set-warning!
  "Set warning status message."
  {:malli/schema [:=> [:cat [:maybe :string]] :map]}
  [message]
  (set-status! message :warning))

(defn set-error!
  "Set error status message."
  {:malli/schema [:=> [:cat [:maybe :string]] :map]}
  [message]
  (set-status! message :error))

(defn clear!
  "Clear status message."
  {:malli/schema [:=> [:cat] :map]}
  []
  (set-status! "" :info))

;; ============================================================================
;; Action Status Helpers
;; ============================================================================

(defn set-theme-changed!
  "Set status for theme change."
  {:malli/schema [:=> [:cat :keyword] :map]}
  [theme-key]
  (let [theme-name (i18n/tr (keyword (str "theme-" (name theme-key))))]
    (set-info! (i18n/tr :status-theme-changed [theme-name]))))

(defn set-language-changed!
  "Set status for language change."
  {:malli/schema [:=> [:cat :keyword] :map]}
  [locale]
  (let [language-name (case locale
                        :english (i18n/tr :language-english)
                        :ukrainian (i18n/tr :language-ukrainian)
                        (name locale))]
    (set-info! (i18n/tr :status-language-changed [language-name]))))

(defn set-connecting!
  "Set status for connection attempt."
  {:malli/schema [:=> [:cat :string] :map]}
  [server]
  (set-info! (i18n/tr :status-connecting-server [server])))

(defn set-connected!
  "Set status for successful connection."
  {:malli/schema [:=> [:cat :string] :map]}
  [server]
  (set-info! (i18n/tr :status-connected-server [server])))

(defn set-disconnected!
  "Set status for disconnection."
  {:malli/schema [:=> [:cat :string] :map]}
  [server]
  (set-warning! (i18n/tr :status-disconnected-server [server])))

(defn set-connection-failed!
  "Set status for connection failure."
  {:malli/schema [:=> [:cat :string] :map]}
  [error-msg]
  (set-error! (i18n/tr :status-connection-failed [error-msg])))

(defn set-stream-started!
  "Set status for stream start."
  {:malli/schema [:=> [:cat :keyword] :map]}
  [stream-type]
  (let [stream-name (case stream-type
                      :heat (i18n/tr :stream-thermal)
                      :day (i18n/tr :stream-day)
                      (name stream-type))]
    (set-info! (i18n/tr :status-stream-started [stream-name]))))

(defn set-stream-stopped!
  "Set status for stream stop."
  {:malli/schema [:=> [:cat :keyword] :map]}
  [stream-type]
  (let [stream-name (case stream-type
                      :heat (i18n/tr :stream-thermal)
                      :day (i18n/tr :stream-day)
                      (name stream-type))]
    (set-info! (i18n/tr :status-stream-stopped [stream-name]))))

(defn set-config-saved!
  "Set status for config save."
  {:malli/schema [:=> [:cat] :map]}
  []
  (set-info! (i18n/tr :status-config-saved)))

(defn set-logs-exported!
  "Set status for log export."
  {:malli/schema [:=> [:cat :string] :map]}
  [path]
  (set-info! (i18n/tr :status-logs-exported [path])))

(defn set-ready!
  "Set ready status."
  {:malli/schema [:=> [:cat] :map]}
  []
  (set-info! (i18n/tr :status-ready)))

;; ============================================================================
;; Error Handling
;; ============================================================================

(defn- get-stack-trace
  "Get stack trace as string."
  {:malli/schema [:=> [:cat :any] :string]}
  [throwable]
  (let [sw (StringWriter.)
        pw (PrintWriter. sw)]
    (.printStackTrace throwable pw)
    (.toString sw)))

(defn with-error-handler
  "Execute function with error handling and status bar notification."
  {:malli/schema [:=> [:cat :fn] :any]}
  [f]
  (try
    (f)
    (catch Exception e
      (let [error-info {:message (.getMessage e)
                       :stack-trace (get-stack-trace e)
                       :timestamp (System/currentTimeMillis)}]
        (reset! last-error-atom error-info)
        (set-error! (i18n/tr :status-error-occurred)))
      ;; Re-throw to not swallow the error
      (throw e))))

(defmacro with-status
  "Execute body with status message."
  [status-msg & body]
  `(do
     (set-info! ~status-msg)
     (try
       ~@body
       (finally
         (set-ready!)))))