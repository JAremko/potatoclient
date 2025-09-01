(ns potatoclient.ui.status-bar
  "Status bar component for displaying system messages and errors.
   This namespace re-exports functions from the split namespaces for backward compatibility."
  (:require
    [potatoclient.ui.status-bar.core :as core]
    [potatoclient.ui.status-bar.helpers :as helpers]
    [potatoclient.ui.status-bar.messages :as msg]
    [potatoclient.ui.status-bar.validation :as validation]))

;; ============================================================================
;; Re-exports from core
;; ============================================================================

(def create core/create)

;; ============================================================================
;; Re-exports from messages
;; ============================================================================

(def set-status! msg/set-status!)
(def set-info! msg/set-info!)
(def set-warning! msg/set-warning!)
(def set-error! msg/set-error!)
(def clear! msg/clear!)
(def set-theme-changed! msg/set-theme-changed!)
(def set-language-changed! msg/set-language-changed!)
(def set-connecting! msg/set-connecting!)
(def set-connected! msg/set-connected!)
(def set-disconnected! msg/set-disconnected!)
(def set-connection-failed! msg/set-connection-failed!)
(def set-stream-started! msg/set-stream-started!)
(def set-stream-stopped! msg/set-stream-stopped!)
(def set-config-saved! msg/set-config-saved!)
(def set-logs-exported! msg/set-logs-exported!)
(def set-ready! msg/set-ready!)
(def with-error-handler msg/with-error-handler)
(defmacro with-status [status-msg & body]
  `(msg/with-status ~status-msg ~@body))

;; ============================================================================
;; Re-exports from validation
;; ============================================================================

(def validate validation/validate)
(def validate-with-details validation/validate-with-details)
(def valid? validation/valid?)
(def explain-validation validation/explain-validation)

;; ============================================================================
;; Re-exports from helpers (for internal use)
;; ============================================================================

(def ^:private get-status-icon helpers/get-status-icon)
(def ^:private get-status-color helpers/get-status-color)
(def ^:private get-stack-trace helpers/get-stack-trace)
(def ^:private last-error-atom helpers/last-error-atom)