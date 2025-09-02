(ns potatoclient.ui.status-bar.messages
  "Status bar message management."
  (:require
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
  {:malli/schema [:=> [:cat [:maybe :string] :keyword] :map]}
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

(defn with-error-handler
  "Execute function with error handling and status bar notification."
  {:malli/schema [:=> [:cat :fn] :any]}
  [f]
  (try
    (f)
    (catch Exception e
      (helpers/store-error! e)
      (set-error! (i18n/tr :status-error-occurred))
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