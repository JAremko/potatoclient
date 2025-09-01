(ns potatoclient.ui.status-bar.helpers
  "Helper utilities for status bar."
  (:require
    [potatoclient.theme :as theme]
    [seesaw.color :as color])
  (:import (java.io StringWriter PrintWriter)))

;; ============================================================================
;; Status Icons
;; ============================================================================

(defn get-status-icon
  "Get icon for status type."
  {:malli/schema [:=> [:cat :keyword] [:maybe :any]]}
  [status-type]
  (case status-type
    :info (theme/get-icon :status-bar-icon-good)
    :warning (theme/get-icon :status-bar-icon-warn)
    :error (theme/get-icon :status-bar-icon-bad)
    (theme/get-icon :status-bar-icon-good)))

(defn get-status-color
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
;; Error Handling
;; ============================================================================

(defn get-stack-trace
  "Get stack trace as string."
  {:malli/schema [:=> [:cat :any] :string]}
  [throwable]
  (let [sw (StringWriter.)
        pw (PrintWriter. sw)]
    (.printStackTrace throwable pw)
    (.toString sw)))

;; ============================================================================
;; Error Storage
;; ============================================================================

(defonce ^{:doc "Stores the last error for display"} last-error-atom
  (atom nil))

(defn store-error!
  "Store error information for later display."
  {:malli/schema [:=> [:cat :any] :map]}
  [throwable]
  (let [error-info {:message (.getMessage throwable)
                    :stack-trace (get-stack-trace throwable)
                    :timestamp (System/currentTimeMillis)}]
    (reset! last-error-atom error-info)
    error-info))

(defn get-last-error
  "Get the last stored error."
  {:malli/schema [:=> [:cat] [:maybe :map]]}
  []
  @last-error-atom)