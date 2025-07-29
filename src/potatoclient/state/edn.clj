(ns potatoclient.state.edn
  "Pure EDN schemas for device state data.
  
  These schemas define the internal representation of state data independent
  of the protobuf wire format. They mirror the protobuf structure but use
  idiomatic Clojure data structures."
  (:require [clojure.spec.alpha :as s]
            [com.fulcrologic.guardrails.malli.core :refer [=> >defn >defn- ?]]
            [malli.core :as m]
            [potatoclient.state.schemas :as schemas]))

;; ============================================================================
;; State Schemas
;; ============================================================================

;; Import schemas from the existing definitions
(def gui-state-schema schemas/jon-gui-state-schema)
(def system-state-schema schemas/system-schema)
(def lrf-state-schema schemas/lrf-schema)
(def time-state-schema schemas/time-schema)
(def gps-state-schema schemas/gps-schema)
(def compass-state-schema schemas/compass-schema)
(def rotary-state-schema schemas/rotary-schema)
(def camera-day-state-schema schemas/camera-day-schema)
(def camera-heat-state-schema schemas/camera-heat-schema)
(def compass-calibration-state-schema schemas/compass-calibration-schema)
(def rec-osd-state-schema schemas/rec-osd-schema)
(def day-cam-glass-heater-state-schema schemas/day-cam-glass-heater-schema)
(def actual-space-time-state-schema schemas/actual-space-time-schema)
(def meteo-state-schema schemas/meteo-schema)

;; Subsystem key schema
(def subsystem-key
  "Valid subsystem keys"
  [:enum :system :lrf :time :gps :compass :rotary :camera-day :camera-heat
   :compass-calibration :rec-osd :day-cam-glass-heater :actual-space-time
   :meteo-internal])

;; ============================================================================
;; State Creation Helpers
;; ============================================================================

(>defn create-empty-state
  "Create an empty GUI state map with minimal required fields"
  []
  [=> gui-state-schema]
  {:protocol-version 1})

(>defn create-subsystem-state
  "Create a subsystem state with defaults"
  [subsystem-key data]
  [subsystem-key map? => gui-state-schema]
  (assoc (create-empty-state) subsystem-key data))

;; ============================================================================
;; State Validation
;; ============================================================================

(>defn validate-state
  "Validate a complete state map against the schema"
  [state-map]
  [any? => boolean?]
  (m/validate gui-state-schema state-map))

(>defn explain-state
  "Get validation errors for a state map"
  [state-map]
  [any? => any?]
  (m/explain gui-state-schema state-map))

(>defn validate-subsystem
  "Validate a specific subsystem's data"
  [subsystem-k data]
  [subsystem-key any? => boolean?]
  (schemas/validate-subsystem subsystem-k data))

;; ============================================================================
;; State Diffing
;; ============================================================================

(>defn subsystem-changed?
  "Check if a subsystem has changed between two states"
  [old-state new-state subsystem-k]
  [(? map?) (? map?) subsystem-key => boolean?]
  (not= (get old-state subsystem-k)
        (get new-state subsystem-k)))

(>defn changed-subsystems
  "Return a set of subsystem keys that have changed"
  [old-state new-state]
  [(? map?) (? map?) => [:set subsystem-key]]
  (let [all-keys (into #{} (concat (keys old-state) (keys new-state)))]
    (into #{}
          (filter #(subsystem-changed? old-state new-state %))
          all-keys)))

;; ============================================================================
;; State Merging
;; ============================================================================

(>defn merge-state
  "Merge partial state updates into existing state"
  [current-state updates]
  [map? map? => map?]
  (merge current-state updates))

(>defn update-subsystem
  "Update a specific subsystem in the state"
  [state subsystem-k subsystem-data]
  [map? subsystem-key any? => map?]
  (if (nil? subsystem-data)
    (dissoc state subsystem-k)
    (assoc state subsystem-k subsystem-data)))

;; ============================================================================
;; State Extraction
;; ============================================================================

(>defn extract-subsystems
  "Extract all subsystem data from a state map"
  [state]
  [map? => map?]
  (dissoc state :protocol-version))

(>defn get-subsystem
  "Get a specific subsystem from state"
  [state subsystem-k]
  [map? subsystem-key => any?]
  (get state subsystem-k))

;; ============================================================================
;; Test Data Generators
;; ============================================================================

(>defn generate-test-state
  "Generate a test state with optional subsystems"
  [& subsystems]
  [(? (s/* keyword?)) => map?]
  (let [base-state (create-empty-state)]
    (reduce (fn [state subsystem-key]
              (case subsystem-key
                :system (assoc state :system {:cpu-temperature 45.0
                                              :gpu-temperature 50.0
                                              :cpu-load 25.0
                                              :gpu-load 30.0
                                              :power-consumption 15.5
                                              :loc "JON_GUI_DATA_SYSTEM_LOCALIZATION_EN"
                                              :rec-enabled false
                                              :important-rec-enabled false
                                              :low-disk-space false
                                              :no-disk-space false
                                              :disk-space 75
                                              :tracking false
                                              :vampire-mode false
                                              :stabilization-mode false
                                              :geodesic-mode false
                                              :cv-dumping false
                                              :cur-video-rec-dir-year 2024
                                              :cur-video-rec-dir-month 1
                                              :cur-video-rec-dir-day 15
                                              :cur-video-rec-dir-hour 14
                                              :cur-video-rec-dir-minute 30
                                              :cur-video-rec-dir-second 0})
                :time (assoc state :time {:timestamp 1705337400
                                          :manual-timestamp 0
                                          :zone-id 0
                                          :use-manual-time false})
                :gps (assoc state :gps {:longitude 30.5234
                                        :latitude 50.4501
                                        :altitude 150.0
                                        :manual-longitude 0.0
                                        :manual-latitude 0.0
                                        :manual-altitude 0.0
                                        :fix-type "JON_GUI_DATA_GPS_FIX_TYPE_3D"
                                        :use-manual false})
                :compass (assoc state :compass {:azimuth 45.0
                                                :elevation 10.0
                                                :bank 0.0
                                                :offset-azimuth 0.0
                                                :offset-elevation 0.0
                                                :magnetic-declination 3.5
                                                :calibrating false})
                state))
            base-state
            subsystems)))