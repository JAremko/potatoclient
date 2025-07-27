(ns potatoclient.state.device
  "Reactive atoms for device state management.
  
  This namespace provides centralized state atoms for all device subsystems,
  mirroring the signal-based approach from the TypeScript implementation.
  Each atom holds the deserialized EDN data for its respective subsystem."
  (:require [com.fulcrologic.guardrails.malli.core :as gr :refer [>defn ? =>]]
            [malli.core :as m]
            [potatoclient.state.schemas :as schemas]))

;; ============================================================================
;; Subsystem State Atoms
;; ============================================================================

;; System information including localization, battery status, etc.
(defonce system-state (atom nil))

;; Laser range finder data including distance measurements and scan modes
(defonce lrf-state (atom nil))

;; Time information including UTC time and format settings
(defonce time-state (atom nil))

;; GPS coordinates with manual override support and fix type
(defonce gps-state (atom nil))

;; Compass heading data with unit preferences
(defonce compass-state (atom nil))

;; Rotary platform position and movement data
(defonce rotary-state (atom nil))

;; Day camera settings and status
(defonce camera-day-state (atom nil))

;; Heat camera settings, filters, and AGC modes
(defonce camera-heat-state (atom nil))

;; Compass calibration status and progress
(defonce compass-calibration-state (atom nil))

;; Recording and OSD (On-Screen Display) settings
(defonce rec-osd-state (atom nil))

;; Day camera glass heater status for defogging
(defonce day-cam-glass-heater-state (atom nil))

;; Actual space-time data for advanced calculations
(defonce actual-space-time-state (atom nil))

;; Internal meteorological data (temperature, humidity, pressure)
;; Note: meteo-internal is in the proto but not in the TypeScript implementation
(defonce meteo-internal-state (atom nil))

;; ============================================================================
;; State Reset Functions
;; ============================================================================

(>defn reset-all-states!
  "Reset all state atoms to nil. Useful for testing or disconnection."
  []
  [=> nil?]
  (reset! system-state nil)
  (reset! lrf-state nil)
  (reset! time-state nil)
  (reset! gps-state nil)
  (reset! compass-state nil)
  (reset! rotary-state nil)
  (reset! camera-day-state nil)
  (reset! camera-heat-state nil)
  (reset! compass-calibration-state nil)
  (reset! rec-osd-state nil)
  (reset! day-cam-glass-heater-state nil)
  (reset! actual-space-time-state nil)
  (reset! meteo-internal-state nil)
  nil)

;; ============================================================================
;; State Accessor Functions
;; ============================================================================

(>defn get-system
  "Get current system state"
  []
  [=> (? map?)]
  @system-state)

(>defn get-lrf
  "Get current laser range finder state"
  []
  [=> (? map?)]
  @lrf-state)

(>defn get-time
  "Get current time state"
  []
  [=> (? map?)]
  @time-state)

(>defn get-gps
  "Get current GPS state"
  []
  [=> (? map?)]
  @gps-state)

(>defn get-compass
  "Get current compass state"
  []
  [=> (? map?)]
  @compass-state)

(>defn get-rotary
  "Get current rotary platform state"
  []
  [=> (? map?)]
  @rotary-state)

(>defn get-camera-day
  "Get current day camera state"
  []
  [=> (? map?)]
  @camera-day-state)

(>defn get-camera-heat
  "Get current heat camera state"
  []
  [=> (? map?)]
  @camera-heat-state)

(>defn get-compass-calibration
  "Get current compass calibration state"
  []
  [=> (? map?)]
  @compass-calibration-state)

(>defn get-rec-osd
  "Get current recording/OSD state"
  []
  [=> (? map?)]
  @rec-osd-state)

(>defn get-day-cam-glass-heater
  "Get current day camera glass heater state"
  []
  [=> (? map?)]
  @day-cam-glass-heater-state)

(>defn get-actual-space-time
  "Get current actual space-time state"
  []
  [=> (? map?)]
  @actual-space-time-state)

(>defn get-meteo-internal
  "Get current internal meteorological state"
  []
  [=> (? map?)]
  @meteo-internal-state)

;; ============================================================================
;; Convenience Functions
;; ============================================================================

(>defn get-current-position
  "Get current position from GPS state with nil safety.
  Returns map with :latitude, :longitude, :altitude or nil."
  []
  [=> (? [:map
          [:latitude number?]
          [:longitude number?]
          [:altitude number?]])]
  (when-let [gps @gps-state]
    (let [use-manual (:use-manual gps false)]
      (if use-manual
        {:latitude (:manual-latitude gps)
         :longitude (:manual-longitude gps)
         :altitude (:manual-altitude gps)}
        {:latitude (:latitude gps)
         :longitude (:longitude gps)
         :altitude (:altitude gps)}))))

(>defn get-heading
  "Get current heading from compass state"
  []
  [=> (? number?)]
  (when-let [compass @compass-state]
    (:heading compass)))

(>defn get-distance
  "Get current distance measurement from LRF"
  []
  [=> (? number?)]
  (when-let [lrf @lrf-state]
    (:distance lrf)))

(>defn get-battery-level
  "Get current battery level from system state"
  []
  [=> (? number?)]
  (when-let [system @system-state]
    (:battery-level system)))

(>defn cameras-ready?
  "Check if both cameras have reported state"
  []
  [=> boolean?]
  (and (some? @camera-day-state)
       (some? @camera-heat-state)))

(>defn gps-has-fix?
  "Check if GPS has a valid fix (not NONE or UNSPECIFIED)"
  []
  [=> boolean?]
  (when-let [gps @gps-state]
    (let [fix-type (:fix-type gps)]
      (and (some? fix-type)
           (not= fix-type "JON_GUI_DATA_GPS_FIX_TYPE_UNSPECIFIED")
           (not= fix-type "JON_GUI_DATA_GPS_FIX_TYPE_NONE")))))

(>defn compass-calibrated?
  "Check if compass is calibrated"
  []
  [=> boolean?]
  (when-let [cal @compass-calibration-state]
    (= (:status cal) "JON_GUI_DATA_COMPASS_CALIBRATE_STATUS_FINISHED")))

;; ============================================================================
;; State Validation
;; ============================================================================

(>defn validate-and-set!
  "Validate data against subsystem schema before setting atom.
  Returns true if data was valid and set, false otherwise."
  [subsystem-key data]
  [keyword? (? any?) => boolean?]
  (if (nil? data)
    ;; nil is always valid (represents no data)
    true
    (let [valid? (schemas/validate-subsystem subsystem-key data)]
      (when-not valid?
        (let [errors (m/explain (get schemas/all-schemas subsystem-key) data)]
          (println "Validation failed for" subsystem-key ":" errors)))
      valid?)))

;; ============================================================================
;; State Watch Support
;; ============================================================================

(>defn watch-state
  "Add a watch function to a specific subsystem atom.
  Returns a unique key that can be used to remove the watch."
  [subsystem-key watch-fn]
  [keyword? fn? => keyword?]
  (let [state-atom (case subsystem-key
                     :system system-state
                     :lrf lrf-state
                     :time time-state
                     :gps gps-state
                     :compass compass-state
                     :rotary rotary-state
                     :camera-day camera-day-state
                     :camera-heat camera-heat-state
                     :compass-calibration compass-calibration-state
                     :rec-osd rec-osd-state
                     :day-cam-glass-heater day-cam-glass-heater-state
                     :actual-space-time actual-space-time-state
                     :meteo-internal meteo-internal-state
                     (throw (ex-info "Unknown subsystem" {:subsystem subsystem-key})))
        watch-key (keyword (gensym (str "watch-" (name subsystem-key) "-")))]
    (add-watch state-atom watch-key
               (fn [_ _ old-state new-state]
                 (when (not= old-state new-state)
                   (watch-fn old-state new-state))))
    watch-key))

(>defn unwatch-state
  "Remove a watch from a subsystem atom"
  [subsystem-key watch-key]
  [keyword? keyword? => nil?]
  (let [state-atom (case subsystem-key
                     :system system-state
                     :lrf lrf-state
                     :time time-state
                     :gps gps-state
                     :compass compass-state
                     :rotary rotary-state
                     :camera-day camera-day-state
                     :camera-heat camera-heat-state
                     :compass-calibration compass-calibration-state
                     :rec-osd rec-osd-state
                     :day-cam-glass-heater day-cam-glass-heater-state
                     :actual-space-time actual-space-time-state
                     :meteo-internal meteo-internal-state)]
    (remove-watch state-atom watch-key))
  nil)