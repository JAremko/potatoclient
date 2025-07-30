(ns potatoclient.transit.schemas
  "Comprehensive Malli schemas for app-db validation"
  (:require [malli.core :as m]
            [malli.error :as me]
            [malli.util :as mu]
            [potatoclient.specs :as specs]
            [com.fulcrologic.guardrails.malli.core :refer [=> >defn >defn-]]))

;; GPS schema
(def gps-schema
  [:map
   [:latitude [:double {:min -90.0 :max 90.0}]]
   [:longitude [:double {:min -180.0 :max 180.0}]]
   [:altitude [:double {:min -1000.0 :max 10000.0}]]
   [:fix-type [:enum "none" "2D" "3D" "DGPS" "RTK"]]
   [:satellites [:int {:min 0 :max 50}]]
   [:hdop [:double {:min 0.0 :max 99.9}]]
   [:use-manual boolean?]])

;; System schema
(def system-schema
  [:map
   [:battery-level [:int {:min 0 :max 100}]]
   [:localization [:enum "en" "uk"]]
   [:recording boolean?]
   [:mode [:enum :day :heat :fusion]]
   [:temperature-c [:double {:min -50.0 :max 80.0}]]])

;; LRF schema
(def lrf-schema
  [:map
   [:distance [:double {:min 0.0 :max 50000.0}]]
   [:scan-mode [:enum "single" "continuous" "gated"]]
   [:target-locked boolean?]])

;; Compass schema
(def compass-schema
  [:map
   [:heading [:double {:min 0.0 :max 360.0}]]
   [:pitch [:double {:min -90.0 :max 90.0}]]
   [:roll [:double {:min -180.0 :max 180.0}]]
   [:unit [:enum "degrees" "mils"]]
   [:calibrated boolean?]])

;; Rotary platform schema
(def rotary-schema
  [:map
   [:azimuth [:double {:min 0.0 :max 360.0}]]
   [:elevation [:double {:min -30.0 :max 90.0}]]
   [:azimuth-velocity [:double {:min -180.0 :max 180.0}]]
   [:elevation-velocity [:double {:min -90.0 :max 90.0}]]
   [:moving boolean?]
   [:mode [:enum :manual :tracking :scanning]]])

;; Camera schemas
(def camera-day-schema
  [:map
   [:zoom [:double {:min 1.0 :max 50.0}]]
   [:focus-mode [:enum :auto :manual :infinity]]
   [:exposure-mode [:enum :auto :manual :shutter-priority :aperture-priority]]
   [:brightness [:int {:min 0 :max 100}]]
   [:contrast [:int {:min 0 :max 100}]]
   [:recording boolean?]])

(def camera-heat-schema
  [:map
   [:zoom [:double {:min 1.0 :max 8.0}]]
   [:palette [:enum :white-hot :black-hot :rainbow :ironbow :lava :arctic]]
   [:brightness [:int {:min 0 :max 100}]]
   [:contrast [:int {:min 0 :max 100}]]
   [:nuc-status [:enum :idle :in-progress :scheduled]]
   [:recording boolean?]])

;; Glass heater schema
(def glass-heater-schema
  [:map
   [:enabled boolean?]
   [:temperature-c [:double {:min -50.0 :max 80.0}]]
   [:target-temp-c [:double {:min -50.0 :max 80.0}]]
   [:power-percent [:int {:min 0 :max 100}]]])

;; Connection schema
(def connection-schema
  [:map
   [:url string?]
   [:connected? boolean?]
   [:latency-ms [:maybe pos-int?]]
   [:reconnect-count [:int {:min 0}]]])

;; UI schema
(def ui-schema
  [:map
   [:theme ::specs/theme-key]
   [:locale ::specs/locale]
   [:read-only-mode? boolean?]
   [:show-overlay? boolean?]
   [:fullscreen? boolean?]])

;; Process schema
(def process-schema
  [:map
   [:pid [:maybe pos-int?]]
   [:status [:enum :running :stopped :error]]])

;; Processes schema
(def processes-schema
  [:map
   [:state-proc process-schema]
   [:cmd-proc process-schema]
   [:heat-video process-schema]
   [:day-video process-schema]])

;; Validation error schema
(def validation-error-schema
  [:map
   [:timestamp pos-int?]
   [:source [:enum :buf-validate :malli]]
   [:subsystem [:maybe keyword?]]
   [:errors [:sequential map?]]])

;; Validation schema
(def validation-schema
  [:map
   [:enabled? boolean?]
   [:errors [:vector validation-error-schema]]
   [:stats [:map
            [:total-validations [:int {:min 0}]]
            [:failed-validations [:int {:min 0}]]
            [:last-validation-time [:maybe pos-int?]]]]])

;; Rate limits schema
(def rate-limits-schema
  [:map
   [:max-rate-hz [:int {:min 1 :max 120}]]
   [:current-rate [:double {:min 0.0}]]
   [:dropped-updates [:int {:min 0}]]
   [:last-update-time [:maybe pos-int?]]])

;; Server state schema (all subsystems)
(def server-state-schema
  [:map
   [:system {:optional true} system-schema]
   [:lrf {:optional true} lrf-schema]
   [:gps {:optional true} gps-schema]
   [:compass {:optional true} compass-schema]
   [:rotary {:optional true} rotary-schema]
   [:camera-day {:optional true} camera-day-schema]
   [:camera-heat {:optional true} camera-heat-schema]
   [:glass-heater {:optional true} glass-heater-schema]])

;; App state schema
(def app-state-schema
  [:map
   [:connection connection-schema]
   [:ui ui-schema]
   [:processes processes-schema]])

;; Complete app-db schema
(def app-db-schema
  [:map
   [:server-state server-state-schema]
   [:app-state app-state-schema]
   [:validation validation-schema]
   [:rate-limits rate-limits-schema]])

;; Validation functions with Guardrails
(>defn validate-app-db
  "Validate the entire app-db structure"
  [db]
  [any? => boolean?]
  (m/validate app-db-schema db))

(>defn validate-server-state
  "Validate just the server state portion"
  [state]
  [any? => boolean?]
  (m/validate server-state-schema state))

(>defn validate-subsystem
  "Validate a specific subsystem"
  [subsystem state]
  [keyword? any? => boolean?]
  (case subsystem
    :system (m/validate system-schema state)
    :lrf (m/validate lrf-schema state)
    :gps (m/validate gps-schema state)
    :compass (m/validate compass-schema state)
    :rotary (m/validate rotary-schema state)
    :camera-day (m/validate camera-day-schema state)
    :camera-heat (m/validate camera-heat-schema state)
    :glass-heater (m/validate glass-heater-schema state)
    false))

(>defn explain-validation-errors
  "Get human-readable validation errors"
  [schema data]
  [any? any? => [:sequential string?]]
  (when-not (m/validate schema data)
    (-> (m/explain schema data)
        (me/humanize)
        (flatten)
        (vec))))

(>defn validate-with-explanation
  "Validate and return both result and errors"
  [schema data]
  [any? any? => [:map [:valid? boolean?] [:errors [:sequential string?]]]]
  (if (m/validate schema data)
    {:valid? true :errors []}
    {:valid? false :errors (explain-validation-errors schema data)}))

;; Message schemas for Transit communication
(def message-envelope-schema
  [:map
   [:msg-type keyword?]
   [:msg-id string?]
   [:timestamp pos-int?]
   [:payload any?]])

(def state-message-schema
  [:and
   message-envelope-schema
   [:map [:msg-type [:= :state]]]])

(def command-message-schema
  [:and
   message-envelope-schema
   [:map [:msg-type [:= :command]]]])

(def control-message-schema
  [:and
   message-envelope-schema
   [:map [:msg-type [:= :control]]
         [:payload [:map [:action keyword?]]]]])

(def response-message-schema
  [:and
   message-envelope-schema
   [:map [:msg-type [:= :response]]]])

(def validation-error-message-schema
  [:and
   message-envelope-schema
   [:map [:msg-type [:= :validation-error]]
         [:payload [:map
                    [:source [:enum :buf-validate :malli]]
                    [:errors [:sequential any?]]]]]])

;; Message validation helpers
(>defn validate-message
  "Validate a Transit message envelope"
  [msg]
  [any? => boolean?]
  (m/validate message-envelope-schema msg))

(>defn validate-state-message
  "Validate a state update message"
  [msg]
  [any? => boolean?]
  (m/validate state-message-schema msg))

(>defn validate-command-message
  "Validate a command message"
  [msg]
  [any? => boolean?]
  (m/validate command-message-schema msg))

;; Schema registry for dynamic lookups
(def schema-registry
  {:app-db app-db-schema
   :server-state server-state-schema
   :app-state app-state-schema
   :system system-schema
   :lrf lrf-schema
   :gps gps-schema
   :compass compass-schema
   :rotary rotary-schema
   :camera-day camera-day-schema
   :camera-heat camera-heat-schema
   :glass-heater glass-heater-schema
   :connection connection-schema
   :ui ui-schema
   :processes processes-schema
   :validation validation-schema
   :rate-limits rate-limits-schema})

(>defn get-schema
  "Get a schema by key"
  [schema-key]
  [keyword? => any?]
  (get schema-registry schema-key))