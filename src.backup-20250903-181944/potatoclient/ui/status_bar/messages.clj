(ns potatoclient.ui.status-bar.messages
  "Status bar message management."
  (:require
            [malli.core :as m]
    [potatoclient.i18n :as i18n]
    [potatoclient.logging :as logging]
    [potatoclient.runtime :as runtime]
    [potatoclient.state :as state]
    [potatoclient.ui.status-bar.helpers :as helpers]
    [seesaw.invoke :as invoke]))

;; ============================================================================
;; Core Status Updates
;; ============================================================================

(defn set-status!
  "Set status message with type."
  [message type]
  ;; Log in dev mode
  (when-not (runtime/release-build?)
    (logging/log-info {:id :status-bar/update
                       :msg (str "Status bar update: " (or message "(empty)"))
                       :type type}))
  ;; Update state synchronously first (for tests)
  (let [status-map {:message (or message "") :type type}]
    (swap! state/app-state assoc-in [:ui :status] status-map)
    status-map)) 
 (m/=> set-status! [:=> [:cat [:maybe :string] :keyword] :map])

(defn set-info!
  "Set info status message."
  [message]
  (set-status! message :info)) 
 (m/=> set-info! [:=> [:cat [:maybe :string]] :map])

(defn set-warning!
  "Set warning status message."
  [message]
  (set-status! message :warning)) 
 (m/=> set-warning! [:=> [:cat [:maybe :string]] :map])

(defn set-error!
  "Set error status message."
  [message]
  (set-status! message :error)) 
 (m/=> set-error! [:=> [:cat [:maybe :string]] :map])

(defn clear!
  "Clear status message."
  []
  (set-status! "" :info)) 
 (m/=> clear! [:=> [:cat] :map])

;; ============================================================================
;; Action Status Helpers
;; ============================================================================

(defn set-theme-changed!
  "Set status for theme change."
  [theme-key]
  (let [theme-name (i18n/tr (keyword (str "theme-" (name theme-key))))]
    (set-info! (i18n/tr :status-theme-changed [theme-name])))) 
 (m/=> set-theme-changed! [:=> [:cat :keyword] :map])

(defn set-language-changed!
  "Set status for language change."
  [locale]
  (let [language-name (case locale
                        :english (i18n/tr :language-english)
                        :ukrainian (i18n/tr :language-ukrainian)
                        (name locale))]
    (set-info! (i18n/tr :status-language-changed [language-name])))) 
 (m/=> set-language-changed! [:=> [:cat :keyword] :map])

(defn set-connecting!
  "Set status for connection attempt."
  [server]
  (set-info! (i18n/tr :status-connecting-server [server]))) 
 (m/=> set-connecting! [:=> [:cat :string] :map])

(defn set-connected!
  "Set status for successful connection."
  [server]
  (set-info! (i18n/tr :status-connected-server [server]))) 
 (m/=> set-connected! [:=> [:cat :string] :map])

(defn set-disconnected!
  "Set status for disconnection."
  [server]
  (set-warning! (i18n/tr :status-disconnected-server [server]))) 
 (m/=> set-disconnected! [:=> [:cat :string] :map])

(defn set-connection-failed!
  "Set status for connection failure."
  [error-msg]
  (set-error! (i18n/tr :status-connection-failed [error-msg]))) 
 (m/=> set-connection-failed! [:=> [:cat :string] :map])

(defn set-stream-started!
  "Set status for stream start."
  [stream-type]
  (let [stream-name (case stream-type
                      :heat (i18n/tr :stream-thermal)
                      :day (i18n/tr :stream-day)
                      (name stream-type))]
    (set-info! (i18n/tr :status-stream-started [stream-name])))) 
 (m/=> set-stream-started! [:=> [:cat :keyword] :map])

(defn set-stream-stopped!
  "Set status for stream stop."
  [stream-type]
  (let [stream-name (case stream-type
                      :heat (i18n/tr :stream-thermal)
                      :day (i18n/tr :stream-day)
                      (name stream-type))]
    (set-info! (i18n/tr :status-stream-stopped [stream-name])))) 
 (m/=> set-stream-stopped! [:=> [:cat :keyword] :map])

(defn set-config-saved!
  "Set status for config save."
  []
  (set-info! (i18n/tr :status-config-saved))) 
 (m/=> set-config-saved! [:=> [:cat] :map])

(defn set-logs-exported!
  "Set status for log export."
  [path]
  (set-info! (i18n/tr :status-logs-exported [path]))) 
 (m/=> set-logs-exported! [:=> [:cat :string] :map])

(defn set-ready!
  "Set ready status."
  []
  (set-info! (i18n/tr :status-ready))) 
 (m/=> set-ready! [:=> [:cat] :map])

;; ============================================================================
;; Error Handling
;; ============================================================================

(defn with-error-handler
  "Execute function with error handling and status bar notification."
  [f]
  (try
    (f)
    (catch Exception e
      (helpers/store-error! e)
      (set-error! (i18n/tr :status-error-occurred))
      ;; Re-throw to not swallow the error
      (throw e)))) 
 (m/=> with-error-handler [:=> [:cat :ifn] :any])

(defmacro with-status
  "Execute body with status message."
  [status-msg & body]
  `(do
     (set-info! ~status-msg)
     (try
       ~@body
       (finally
         (set-ready!)))))