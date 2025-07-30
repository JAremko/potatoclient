(ns potatoclient.transit.migration
  "Migration helpers from old multi-atom architecture to new Transit architecture"
  (:require [potatoclient.transit :as transit]
            [potatoclient.transit.app-db :as app-db]
            [com.fulcrologic.guardrails.malli.core :refer [=> >defn >defn-]]))

;; Adapter functions to maintain compatibility with existing code

;; Old: (get @streams-state :heat)
;; New: 
(>defn get-stream-state
  "Get video stream state (adapter for old code)"
  [stream-type]
  [[:enum :heat :day] => (? map?)]
  (get-in @app-db/app-db [:app-state :processes 
                          (keyword (str (name stream-type) "-video"))]))

;; Old: (get @app-config :theme)
;; New:
(>defn get-config
  "Get config value (adapter for old code)"
  [key]
  [keyword? => any?]
  (case key
    :theme (app-db/get-theme)
    :locale (app-db/get-locale)
    :domain (app-db/get-connection-url)
    ;; Default
    (get-in @app-db/app-db [:app-state :ui key])))

;; Old: (swap! app-config assoc :theme new-theme)
;; New:
(>defn set-config!
  "Set config value (adapter for old code)"
  [key value]
  [keyword? any? => map?]
  (case key
    :theme (app-db/set-theme! value)
    :locale (app-db/set-locale! value)
    ;; Default - update UI state
    (swap! app-db/app-db assoc-in [:app-state :ui key] value)))

;; Old: Multiple atoms for device state
;; New: Unified access
(>defn get-device-state
  "Get device subsystem state (adapter for old code)"
  [subsystem]
  [keyword? => (? map?)]
  (app-db/get-subsystem-state subsystem))

;; Command sending adapters
(>defn send-rotary-command!
  "Send rotary command (adapter for old code)"
  [cmd-type & args]
  [keyword? [:* any?] => boolean?]
  (case cmd-type
    :goto (let [[az el] args]
            (transit/rotary-goto! az el))
    :stop (transit/send-command! :rotary-stop {})
    :velocity (let [[az-vel el-vel] args]
                (transit/send-command! :rotary-set-velocity 
                                       {:azimuth-velocity az-vel
                                        :elevation-velocity el-vel}))
    false))

;; Migration function to help transition
(>defn migrate-existing-state!
  "Migrate existing multi-atom state to new app-db"
  [old-states]
  [map? => nil?]
  ;; This would read from old atoms and populate app-db
  ;; Implementation depends on existing state structure
  nil)

;; Watcher migration
(>defn migrate-watchers!
  "Migrate watchers from old atoms to new app-db"
  [old-watchers]
  [map? => nil?]
  (doseq [[key watch-fn] old-watchers]
    (app-db/add-watch-handler key 
                              (fn [k ref old-state new-state]
                                ;; Adapt watcher to new structure
                                (watch-fn k ref 
                                          (:server-state old-state)
                                          (:server-state new-state)))))
  nil)

;; Example usage in existing code:
;; Replace: @device-state
;; With: (get-in @app-db/app-db [:server-state])
;;
;; Replace: (swap! some-state assoc :key value)  
;; With: (swap! app-db/app-db assoc-in [:server-state :key] value)