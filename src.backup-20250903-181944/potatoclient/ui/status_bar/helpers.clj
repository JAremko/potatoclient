(ns potatoclient.ui.status-bar.helpers
  "Helper utilities for status bar."
  (:require
            [malli.core :as m]
    [potatoclient.theme :as theme]
    [seesaw.color :as color])
  (:import (java.io StringWriter PrintWriter)))

;; ============================================================================
;; Status Icons
;; ============================================================================

(defn get-status-icon
  "Get icon for status type."
  [status-type]
  (case status-type
    :info (theme/get-icon :status-bar-icon-good)
    :warning (theme/get-icon :status-bar-icon-warn)
    :error (theme/get-icon :status-bar-icon-bad)
    (theme/get-icon :status-bar-icon-good))) 
 (m/=> get-status-icon [:=> [:cat :keyword] [:maybe :any]])

(defn get-status-color
  "Get color for status type."
  [status-type]
  (case status-type
    :error :red
    :warning :orange
    :info (color/default-color "TextField.foreground")
    ;; Default for invalid types
    (color/default-color "TextField.foreground"))) 
 (m/=> get-status-color [:=> [:cat :keyword] :any])

;; ============================================================================
;; Error Handling
;; ============================================================================

(defn get-stack-trace
  "Get stack trace as string."
  [throwable]
  (let [sw (StringWriter.)
        pw (PrintWriter. sw)]
    (.printStackTrace throwable pw)
    (.toString sw))) 
 (m/=> get-stack-trace [:=> [:cat :any] :string])

;; ============================================================================
;; Error Storage
;; ============================================================================

(defonce ^{:doc "Stores the last error for display"} last-error-atom
  (atom nil))

(defn store-error!
  "Store error information for later display."
  [throwable]
  (let [error-info {:message (.getMessage throwable)
                    :stack-trace (get-stack-trace throwable)
                    :timestamp (System/currentTimeMillis)}]
    (reset! last-error-atom error-info)
    error-info)) 
 (m/=> store-error! [:=> [:cat :any] :map])

(defn get-last-error
  "Get the last stored error."
  []
  @last-error-atom) 
 (m/=> get-last-error [:=> [:cat] [:maybe :map]])