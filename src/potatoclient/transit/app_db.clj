(ns potatoclient.transit.app-db
  "Single source of truth atom following re-frame pattern with Guardrails"
  (:require [com.fulcrologic.guardrails.malli.core :refer [=> >defn >defn- | ?]]
            [potatoclient.logging :as log]
            [potatoclient.runtime :as runtime]
            [potatoclient.ui-specs :as specs]))

;; Default initial state
(def ^{:doc "Default initial application state"}
  initial-state
  {:server-state {:system {:battery-level 0
                           :localization "en"
                           :recording false
                           :mode :day
                           :temperature-c 20.0}
                  :lrf {:distance 0.0
                        :scan-mode "single"
                        :target-locked false}
                  :gps {:latitude 0.0
                        :longitude 0.0
                        :altitude 0.0
                        :fix-type "none"
                        :satellites 0
                        :hdop 99.9
                        :use-manual false}
                  :compass {:heading 0.0
                            :pitch 0.0
                            :roll 0.0
                            :unit "degrees"
                            :calibrated false}
                  :rotary {:azimuth 0.0
                           :elevation 0.0
                           :azimuth-velocity 0.0
                           :elevation-velocity 0.0
                           :moving false
                           :mode :manual}
                  :camera-day {:zoom 1.0
                               :focus-mode :auto
                               :exposure-mode :auto
                               :brightness 50
                               :contrast 50
                               :recording false}
                  :camera-heat {:zoom 1.0
                                :palette :white-hot
                                :brightness 50
                                :contrast 50
                                :nuc-status :idle
                                :recording false}
                  :glass-heater {:enabled false
                                 :temperature-c 20.0
                                 :target-temp-c 25.0
                                 :power-percent 0}}
   :app-state {:connection {:url ""
                            :connected? false
                            :latency-ms nil
                            :reconnect-count 0}
               :ui {:theme :sol-dark
                    :locale :english
                    :read-only-mode? false
                    :show-overlay? true
                    :fullscreen? false}
               :processes {:state-proc {:pid nil
                                        :status :stopped}
                           :cmd-proc {:pid nil
                                      :status :stopped}
                           :heat-video {:pid nil
                                        :status :stopped}
                           :day-video {:pid nil
                                       :status :stopped}}}
   :validation {:enabled? (not (runtime/release-build?))
                :errors []
                :stats {:total-validations 0
                        :failed-validations 0
                        :last-validation-time nil}}
   :rate-limits {:max-rate-hz 30
                 :current-rate 0.0
                 :dropped-updates 0
                 :last-update-time nil}})

;; The single source of truth
;; Global application state atom following re-frame pattern.
(defonce ^{:doc "Global application state atom following re-frame pattern"}
  app-db (atom initial-state))

;; Basic accessors with Guardrails
(>defn get-server-state
  "Get the current server state"
  []
  [=> map?]
  (:server-state @app-db))

(>defn get-app-state
  "Get the current app state"
  []
  [=> map?]
  (:app-state @app-db))

(>defn get-validation-state
  "Get the current validation state"
  []
  [=> map?]
  (:validation @app-db))

(>defn get-rate-limits
  "Get the current rate limits"
  []
  [=> map?]
  (:rate-limits @app-db))

;; Subsystem accessors
(>defn get-subsystem-state
  "Get state for a specific subsystem"
  [subsystem]
  [keyword? => (? map?)]
  (get-in @app-db [:server-state subsystem]))

;; Alias for tests
(def ^{:doc "Alias for get-subsystem-state for backward compatibility in tests"}
  get-subsystem get-subsystem-state)

;; Connection state
(>defn connected?
  "Check if connected to server"
  []
  [=> boolean?]
  (get-in @app-db [:app-state :connection :connected?]))

(>defn get-connection-url
  "Get current connection URL"
  []
  [=> (? string?)]
  (get-in @app-db [:app-state :connection :url]))

;; Process state
(>defn get-process-state
  "Get state of a specific process"
  [process-key]
  [[:enum :state-proc :cmd-proc :heat-video :day-video] => (? map?)]
  (get-in @app-db [:app-state :processes process-key]))

(>defn process-running?
  "Check if a process is running"
  [process-key]
  [[:enum :state-proc :cmd-proc :heat-video :day-video] => boolean?]
  (= :running (get-in @app-db [:app-state :processes process-key :status])))

;; UI state
(>defn get-theme
  "Get current theme"
  []
  [=> ::ui-specs/theme-key]
  (get-in @app-db [:app-state :ui :theme]))

(>defn get-locale
  "Get current locale"
  []
  [=> ::ui-specs/locale]
  (get-in @app-db [:app-state :ui :locale]))

(>defn read-only-mode?
  "Check if in read-only mode"
  []
  [=> boolean?]
  (get-in @app-db [:app-state :ui :read-only-mode?]))

;; Update helpers with validation
(>defn update-server-state!
  "Update server state from Transit message"
  [state-update]
  [map? => map?]
  (swap! app-db update :server-state merge state-update))

(>defn update-subsystem!
  "Update a specific subsystem state"
  [subsystem state-update]
  [keyword? map? => map?]
  (swap! app-db update-in [:server-state subsystem] merge state-update))

(>defn update-in-app-db!
  "Generic update-in for app-db with path and value"
  [path value]
  [vector? any? => map?]
  (swap! app-db assoc-in path value))

(>defn get-in-app-db
  "Generic get-in for app-db with path and optional default"
  ([path]
   [vector? => any?]
   (get-in @app-db path))
  ([path default]
   [vector? any? => any?]
   (get-in @app-db path default)))

(>defn set-connection-state!
  "Update connection state"
  [connected? url latency-ms]
  [boolean? (? string?) (? pos-int?) => map?]
  (swap! app-db update-in [:app-state :connection]
         (fn [conn]
           (cond-> (assoc conn :connected? connected?)
             url (assoc :url url)
             latency-ms (assoc :latency-ms latency-ms)
             (not connected?) (update :reconnect-count inc)))))

(>defn set-process-state!
  "Update process state"
  [process-key pid status]
  [[:enum :state-proc :cmd-proc :heat-video :day-video]
   (? pos-int?)
   [:enum :running :stopped :error]
   => map?]
  (swap! app-db assoc-in [:app-state :processes process-key]
         {:pid pid :status status}))

(>defn set-stream-process!
  "Store full stream process map for video streams"
  [stream-key process-map]
  [[:enum :heat :day] map? => map?]
  (let [process-key (case stream-key
                      :heat :heat-video
                      :day :day-video)]
    (swap! app-db assoc-in [:app-state :stream-processes process-key] process-map)))

(>defn get-stream-process
  "Get full stream process map"
  [stream-key]
  [[:enum :heat :day] => (? map?)]
  (let [process-key (case stream-key
                      :heat :heat-video
                      :day :day-video)]
    (get-in @app-db [:app-state :stream-processes process-key])))

(>defn remove-stream-process!
  "Remove stream process map"
  [stream-key]
  [[:enum :heat :day] => map?]
  (let [process-key (case stream-key
                      :heat :heat-video
                      :day :day-video)]
    (swap! app-db update-in [:app-state :stream-processes] dissoc process-key)))

(>defn get-all-stream-processes
  "Get all active stream processes"
  []
  [=> [:map-of keyword? map?]]
  (get-in @app-db [:app-state :stream-processes] {}))

(>defn add-validation-error!
  "Add a validation error"
  [source subsystem errors]
  [[:enum :buf-validate :malli] (? keyword?) [:sequential map?] => map?]
  (swap! app-db
         (fn [state]
           (-> state
               (update-in [:validation :errors] conj
                          {:timestamp (System/currentTimeMillis)
                           :source source
                           :subsystem subsystem
                           :errors errors})
               (update-in [:validation :stats :failed-validations] inc)
               (assoc-in [:validation :stats :last-validation-time]
                         (System/currentTimeMillis))))))

(>defn update-rate-metrics!
  "Update rate limiting metrics"
  [current-rate dropped?]
  [number? boolean? => map?]
  (swap! app-db
         (fn [state]
           (-> state
               (assoc-in [:rate-limits :current-rate] current-rate)
               (assoc-in [:rate-limits :last-update-time] (System/currentTimeMillis))
               (cond-> dropped? (update-in [:rate-limits :dropped-updates] inc))))))

;; Theme and locale updates
(>defn set-theme!
  "Update UI theme"
  [theme]
  [::ui-specs/theme-key => map?]
  (swap! app-db assoc-in [:app-state :ui :theme] theme))

(>defn set-locale!
  "Update UI locale"
  [locale]
  [::ui-specs/locale => map?]
  (swap! app-db assoc-in [:app-state :ui :locale] locale))

;; Domain and connection helpers for tests
(>defn set-domain!
  "Set the server domain"
  [domain]
  [string? => map?]
  (swap! app-db assoc-in [:app-state :connection :url] domain))

(>defn get-domain
  "Get the server domain"
  []
  [=> (? string?)]
  (get-in @app-db [:app-state :connection :url]))

(>defn set-connected!
  "Set connection status"
  [connected?]
  [boolean? => map?]
  (swap! app-db assoc-in [:app-state :connection :connected?] connected?))

;; Read-only mode
(>defn set-read-only-mode!
  "Set read-only mode"
  [enabled?]
  [boolean? => map?]
  (swap! app-db assoc-in [:app-state :ui :read-only-mode?] enabled?))

;; Validation control
(>defn set-validation-enabled!
  "Enable/disable validation"
  [enabled?]
  [boolean? => map?]
  (swap! app-db assoc-in [:validation :enabled?] enabled?))

;; Rate limit control
(>defn set-max-rate-hz!
  "Set maximum update rate in Hz"
  [hz]
  [[:int {:min 1 :max 120}] => map?]
  (swap! app-db assoc-in [:rate-limits :max-rate-hz] hz))

;; Reset functions
(>defn reset-validation-errors!
  "Clear all validation errors"
  []
  [=> map?]
  (swap! app-db assoc-in [:validation :errors] []))

(>defn reset-to-initial-state!
  "Reset entire app-db to initial state"
  []
  [=> map?]
  (reset! app-db initial-state))

;; Watch for state changes (for debugging)
(>defn add-watch-handler
  "Add a watch handler to app-db"
  [key handler-fn]
  [keyword? fn? => nil?]
  (add-watch app-db key handler-fn)
  nil)

(>defn remove-watch-handler
  "Remove a watch handler from app-db"
  [key]
  [keyword? => nil?]
  (remove-watch app-db key)
  nil)

;; Message handlers for subprocess launcher
(>defn handle-command-response
  "Handle response messages from command subprocess"
  [msg]
  [map? => nil?]
  (log/log-debug
    {:id ::command-response
     :data {:msg-type (:msg-type msg)
            :payload (:payload msg)}
     :msg "Received command response from subprocess"})
  ;; TODO: Implement actual command response handling
  nil)

(>defn handle-state-update
  "Handle state update messages from state subprocess"
  [msg]
  [map? => nil?]
  (log/log-debug
    {:id ::state-update
     :data {:msg-type (:msg-type msg)
            :payload (:payload msg)}
     :msg "Received state update from subprocess"})
  ;; TODO: Implement actual state update handling
  nil)