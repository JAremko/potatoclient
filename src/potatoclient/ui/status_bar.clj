(ns potatoclient.ui.status-bar
  "Status bar component for displaying system messages and errors."
  (:require
    [potatoclient.state :as state]
    [potatoclient.theme :as theme]
    [seesaw.bind :as bind]
    [seesaw.color :as color]
    [seesaw.core :as seesaw])
  (:import (javax.swing JPanel)))

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
    :info (color/default-color "TextField.foreground")))

;; ============================================================================
;; Status Bar Creation
;; ============================================================================

(defn create
  "Create status bar component bound to app state."
  {:malli/schema [:=> [:cat] [:fn {:error/message "must be a Swing panel"} (partial instance? JPanel)]]}
  []
  (let [icon-label (seesaw/label :icon (get-status-icon :info))
        text-field (seesaw/text :editable? false
                               :focusable? false
                               :text ""
                               :foreground (color/default-color "TextField.foreground"))]
    
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
          (bind/value text-field))))
    
    ;; Create the panel
    (seesaw/vertical-panel
      :items [(seesaw/separator :orientation :horizontal)
              (seesaw/horizontal-panel
                :items [icon-label text-field])])))

;; ============================================================================
;; Status Updates
;; ============================================================================

(defn set-status!
  "Set status message with type."
  {:malli/schema [:=> [:cat :string :keyword] :map]}
  [message type]
  (swap! state/app-state assoc-in [:ui :status] {:message message
                                                  :type type}))

(defn set-info!
  "Set info status message."
  {:malli/schema [:=> [:cat :string] :map]}
  [message]
  (set-status! message :info))

(defn set-warning!
  "Set warning status message."
  {:malli/schema [:=> [:cat :string] :map]}
  [message]
  (set-status! message :warning))

(defn set-error!
  "Set error status message."
  {:malli/schema [:=> [:cat :string] :map]}
  [message]
  (set-status! message :error))

(defn clear!
  "Clear status message."
  {:malli/schema [:=> [:cat] :map]}
  []
  (set-status! "" :info))