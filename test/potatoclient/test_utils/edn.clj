(ns potatoclient.test-utils.edn
  "Test utilities for working with EDN state structures.
  
  This namespace provides helpers for creating test data without
  requiring protobuf classes, making tests simpler and faster."
  (:require [potatoclient.state.edn :as edn]
            [malli.core :as m]
            [malli.generator :as mg]))

;; ============================================================================
;; Test State Builders
;; ============================================================================

(defn create-test-system-state
  "Create a test system state with optional overrides"
  [& {:as overrides}]
  (merge {:cpu-temperature 45.0
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
          :cur-video-rec-dir-second 0}
         overrides))

(defn create-test-time-state
  "Create a test time state with optional overrides"
  [& {:as overrides}]
  (merge {:timestamp 1705337400
          :manual-timestamp 0
          :zone-id 0
          :use-manual-time false}
         overrides))

(defn create-test-gps-state
  "Create a test GPS state with optional overrides"
  [& {:as overrides}]
  (merge {:longitude 30.5234
          :latitude 50.4501
          :altitude 150.0
          :manual-longitude 0.0
          :manual-latitude 0.0
          :manual-altitude 0.0
          :fix-type "JON_GUI_DATA_GPS_FIX_TYPE_3D"
          :use-manual false}
         overrides))

(defn create-test-compass-state
  "Create a test compass state with optional overrides"
  [& {:as overrides}]
  (merge {:azimuth 45.0
          :elevation 10.0
          :bank 0.0
          :offset-azimuth 0.0
          :offset-elevation 0.0
          :magnetic-declination 3.5
          :calibrating false}
         overrides))

(defn create-test-rotary-state
  "Create a test rotary state with optional overrides"
  [& {:as overrides}]
  (merge {:azimuth 90.0
          :azimuth-speed 0.0
          :elevation 45.0
          :elevation-speed 0.0
          :platform-azimuth 90.0
          :platform-elevation 45.0
          :platform-bank 0.0
          :is-moving false
          :mode "JON_GUI_DATA_ROTARY_MODE_POSITION"
          :is-scanning false
          :is-scanning-paused false
          :use-rotary-as-compass false
          :scan-target 0
          :scan-target-max 0
          :sun-azimuth 180.0
          :sun-elevation 45.0
          :current-scan-node {:index 0
                              :day-zoom-table-value 0
                              :heat-zoom-table-value 0
                              :azimuth 0.0
                              :elevation 0.0
                              :linger 1.0
                              :speed 0.5}}
         overrides))

(defn create-test-camera-day-state
  "Create a test day camera state with optional overrides"
  [& {:as overrides}]
  (merge {:focus-pos 0.5
          :zoom-pos 0.0
          :iris-pos 0.5
          :infrared-filter false
          :zoom-table-pos 0
          :zoom-table-pos-max 10
          :fx-mode "JON_GUI_DATA_FX_MODE_DAY_DEFAULT"
          :auto-focus true
          :auto-iris true
          :digital-zoom-level 1.0
          :clahe-level 0.0}
         overrides))

(defn create-test-camera-heat-state
  "Create a test heat camera state with optional overrides"
  [& {:as overrides}]
  (merge {:zoom-pos 0.0
          :agc-mode "JON_GUI_DATA_VIDEO_CHANNEL_HEAT_AGC_MODE_1"
          :filter "JON_GUI_DATA_VIDEO_CHANNEL_HEAT_FILTER_HOT_WHITE"
          :auto-focus true
          :zoom-table-pos 0
          :zoom-table-pos-max 10
          :dde-level 0
          :dde-enabled false
          :fx-mode "JON_GUI_DATA_FX_MODE_HEAT_DEFAULT"
          :digital-zoom-level 1.0
          :clahe-level 0.0}
         overrides))

(defn create-test-lrf-state
  "Create a test LRF state with optional overrides"
  [& {:as overrides}]
  (merge {:is-scanning false
          :is-measuring false
          :measure-id 0
          :pointer-mode "JON_GUI_DATA_LRF_LASER_POINTER_MODE_OFF"
          :fog-mode-enabled false
          :is-refining false}
         overrides))

(defn create-test-gui-state
  "Create a complete test GUI state with specified subsystems"
  [& subsystem-specs]
  (let [base-state {:protocol-version 1}]
    (reduce (fn [state spec]
              (cond
                (keyword? spec)
                (case spec
                  :system (assoc state :system (create-test-system-state))
                  :time (assoc state :time (create-test-time-state))
                  :gps (assoc state :gps (create-test-gps-state))
                  :compass (assoc state :compass (create-test-compass-state))
                  :rotary (assoc state :rotary (create-test-rotary-state))
                  :camera-day (assoc state :camera-day (create-test-camera-day-state))
                  :camera-heat (assoc state :camera-heat (create-test-camera-heat-state))
                  :lrf (assoc state :lrf (create-test-lrf-state))
                  state)
                
                (map? spec)
                (merge state spec)
                
                :else state))
            base-state
            subsystem-specs)))

;; ============================================================================
;; State Generators (using Malli)
;; ============================================================================

(defn generate-valid-system-state
  "Generate a random valid system state"
  []
  (mg/generate edn/system-state-schema))

(defn generate-valid-gps-state
  "Generate a random valid GPS state"
  []
  (mg/generate edn/gps-state-schema))

(defn generate-valid-gui-state
  "Generate a random valid GUI state"
  []
  (mg/generate edn/gui-state-schema))

;; ============================================================================
;; State Comparison Helpers
;; ============================================================================

(defn states-equal?
  "Compare two states, ignoring protocol-version if not present in both"
  [state1 state2]
  (let [normalize (fn [s]
                    (if (and (contains? s :protocol-version)
                             (not (contains? state2 :protocol-version)))
                      (dissoc s :protocol-version)
                      s))]
    (= (normalize state1) (normalize state2))))

(defn subsystem-present?
  "Check if a subsystem is present in the state"
  [state subsystem-key]
  (contains? state subsystem-key))

(defn subsystems-present
  "Return set of subsystem keys present in state"
  [state]
  (set (filter #(contains? state %) 
               [:system :lrf :time :gps :compass :rotary 
                :camera-day :camera-heat :compass-calibration 
                :rec-osd :day-cam-glass-heater :actual-space-time 
                :meteo-internal])))

;; ============================================================================
;; Validation Helpers
;; ============================================================================

(defn valid-state?
  "Check if a state is valid according to schema"
  [state]
  (edn/validate-state state))

(defn valid-subsystem?
  "Check if a subsystem is valid"
  [subsystem-key data]
  (edn/validate-subsystem subsystem-key data))

(defn validation-errors
  "Get validation errors for a state"
  [state]
  (edn/explain-state state))

;; ============================================================================
;; State Modification Helpers
;; ============================================================================

(defn update-subsystem
  "Update a subsystem in a state"
  [state subsystem-key updates]
  (if (map? updates)
    (update state subsystem-key merge updates)
    (assoc state subsystem-key updates)))

(defn remove-subsystem
  "Remove a subsystem from state"
  [state subsystem-key]
  (dissoc state subsystem-key))

(defn with-subsystems
  "Add multiple subsystems to a state"
  [state & subsystem-pairs]
  (apply assoc state subsystem-pairs))

;; ============================================================================
;; Test Assertions
;; ============================================================================

(defn assert-valid-state
  "Assert that a state is valid, throw with details if not"
  [state]
  (when-not (valid-state? state)
    (throw (ex-info "Invalid state" 
                    {:state state
                     :errors (validation-errors state)}))))

(defn assert-subsystem-present
  "Assert that a subsystem is present in state"
  [state subsystem-key]
  (when-not (subsystem-present? state subsystem-key)
    (throw (ex-info "Subsystem not present" 
                    {:subsystem subsystem-key
                     :present-subsystems (subsystems-present state)}))))

(defn assert-subsystem-valid
  "Assert that a subsystem is valid"
  [subsystem-key data]
  (when-not (valid-subsystem? subsystem-key data)
    (throw (ex-info "Invalid subsystem data" 
                    {:subsystem subsystem-key
                     :data data}))))