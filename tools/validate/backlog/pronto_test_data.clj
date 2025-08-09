(ns validate.pronto-test-data
  "Idiomatic and performant Pronto-based test data generation.
   This serves as a reference implementation for using Pronto throughout the application."
  (:require [pronto.core :as p]
            [clojure.edn :as edn]
            [clojure.java.io :as io])
  (:import [cmd JonSharedCmd$Root JonSharedCmd$Ping JonSharedCmd$Noop JonSharedCmd$Frozen]
           [ser JonSharedData$JonGUIState]))

;; ============================================================================
;; MAPPER DEFINITIONS
;; Define mappers once and reuse them for performance
;; ============================================================================

(p/defmapper cmd-mapper [cmd.JonSharedCmd$Root])
(p/defmapper state-mapper [ser.JonSharedData$JonGUIState])

;; ============================================================================
;; LOAD REAL STATE DATA FROM STATE-EXPLORER
;; This is the idiomatic way - use real data as the base
;; ============================================================================

(def real-state-edn-path 
  "../state-explorer/output/1754664759800.edn")

(defn load-real-state-edn
  "Load a real state EDN from state-explorer output.
   Returns a Clojure map that can be converted to proto-map."
  []
  (if (.exists (io/file real-state-edn-path))
    (edn/read-string (slurp real-state-edn-path))
    ;; Fallback to embedded minimal valid state if file not found
    {:protocol-version 1
     :time {:timestamp 1754664759
            :manual-timestamp 0
            :zone-id 0
            :use-manual-time false}
     :system {:cpu-temperature 42.0
              :cpu-load 25.0
              :gpu-temperature 40.0
              :gpu-load 20.0
              :disk-space 50
              :power-consumption 100.0
              :rec-enabled false
              :low-disk-space false
              :loc :jon-gui-data-system-localization-en}
     :gps {:latitude 50.023632
           :longitude 15.815209999999999
           :altitude 0.289371
           :fix-type :jon-gui-data-gps-fix-type-3d}
     :compass {:azimuth 333.50625
               :bank 0.84375
               :elevation 3.54375}
     :rotary {:azimuth 333.50626
              :elevation 7.04
              :mode :jon-gui-data-rotary-mode-position}
     :camera-day {:zoom-pos 0.59938735
                  :focus-pos 1.0
                  :iris-pos 0.03
                  :digital-zoom-level 1.0
                  :zoom-table-pos 3
                  :zoom-table-pos-max 4
                  :fx-mode :jon-gui-data-fx-mode-day-a
                  :infrared-filter true
                  :clahe-level 0.16}
     :camera-heat {:zoom-table-pos 3
                   :zoom-table-pos-max 4
                   :digital-zoom-level 1.0
                   :fx-mode :jon-gui-data-fx-mode-heat-a
                   :filter :jon-gui-data-video-channel-heat-filter-hot-white
                   :agc-mode :jon-gui-data-video-channel-heat-agc-mode-2
                   :clahe-level 0.5}
     :day-cam-glass-heater {}
     :meteo-internal {}
     :lrf {:measure-id 52
           :pointer-mode :jon-gui-data-lrf-laser-pointer-mode-off}
     :compass-calibration {:status :jon-gui-data-compass-calibrate-status-not-calibrating
                           :final-stage 12}
     :rec-osd {:day-osd-enabled true
               :heat-osd-enabled true
               :screen :jon-gui-data-rec-osd-screen-main}
     :actual-space-time {:timestamp 1754664759
                         :latitude 50.023632
                         :longitude 15.81521
                         :altitude 0.289371
                         :azimuth 256.62
                         :elevation 7.04}}))

(defn create-valid-state
  "Create a valid state proto-map from real EDN data.
   This is the performant, idiomatic way using Pronto."
  []
  (let [state-edn (load-real-state-edn)]
    (p/clj-map->proto state-mapper 
                      {:ser.JonSharedData$JonGUIState state-edn})))

;; ============================================================================
;; COMMAND MESSAGE CREATION - Idiomatic Pronto Style
;; ============================================================================

(defn create-ping-cmd
  "Create a valid Ping command using performant Pronto proto-map creation."
  []
  (p/proto-map cmd-mapper cmd.JonSharedCmd$Root
               :protocol_version 1
               :session_id 1000
               :client_type :JON_GUI_DATA_CLIENT_TYPE_LOCAL_NETWORK
               :ping (p/proto-map cmd-mapper cmd.JonSharedCmd$Ping)))

(defn create-noop-cmd
  "Create a valid Noop command."
  []
  (p/proto-map cmd-mapper cmd.JonSharedCmd$Root
               :protocol_version 1
               :session_id 2000
               :client_type :JON_GUI_DATA_CLIENT_TYPE_LOCAL_NETWORK
               :noop (p/proto-map cmd-mapper cmd.JonSharedCmd$Noop)))

(defn create-frozen-cmd
  "Create a valid Frozen command."
  []
  (p/proto-map cmd-mapper cmd.JonSharedCmd$Root
               :protocol_version 1
               :session_id 3000
               :client_type :JON_GUI_DATA_CLIENT_TYPE_LOCAL_NETWORK
               :frozen (p/proto-map cmd-mapper cmd.JonSharedCmd$Frozen)))

;; ============================================================================
;; INVALID DATA CREATION - Modify valid data for negative testing
;; This is the idiomatic way: start with valid data, then break it
;; ============================================================================

(defn create-invalid-client-type-cmd
  "Create a command with invalid client type (0) - should fail validation."
  []
  (p/proto-map cmd-mapper cmd.JonSharedCmd$Root
               :protocol_version 1
               :session_id 4000
               :client_type :JON_GUI_DATA_CLIENT_TYPE_UNSPECIFIED  ; Value 0 - not allowed
               :ping (p/proto-map cmd-mapper cmd.JonSharedCmd$Ping)))

(defn create-invalid-protocol-cmd
  "Create a command with invalid protocol version."
  []
  (p/proto-map cmd-mapper cmd.JonSharedCmd$Root
               :protocol_version 0  ; Invalid: should be > 0
               :session_id 5000
               :client_type :JON_GUI_DATA_CLIENT_TYPE_LOCAL_NETWORK
               :ping (p/proto-map cmd-mapper cmd.JonSharedCmd$Ping)))

(defn create-invalid-gps-state
  "Create state with invalid GPS coordinates using Pronto's performant p/p-> macro."
  []
  (let [valid-state (create-valid-state)]
    (p/with-hints [(p/hint valid-state ser.JonSharedData$JonGUIState state-mapper)]
      (p/p-> valid-state
             (assoc-in [:gps :latitude] 200.0)     ; Invalid: > 90
             (assoc-in [:gps :longitude] 400.0))))) ; Invalid: > 180

(defn create-invalid-protocol-state
  "Create a state with invalid protocol version, demonstrating field update."
  []
  (let [valid-state (create-valid-state)]
    (p/with-hints [(p/hint valid-state ser.JonSharedData$JonGUIState state-mapper)]
      (p/p-> valid-state
             (assoc :protocol_version 0))))) ; Invalid: should be > 0

(defn create-state-missing-required-fields
  "Create an invalid state by removing required nested messages."
  []
  (p/proto-map state-mapper ser.JonSharedData$JonGUIState
               :protocol_version 1))  ; Missing all required nested messages

;; ============================================================================
;; PERFORMANT UPDATE OPERATIONS using p/p->
;; Demonstrating best practices for test modifications
;; ============================================================================

(defn modify-state-for-test
  "Example of performant state modification for test scenarios.
   This shows the idiomatic way to create test variations."
  [base-state modifications]
  (p/with-hints [(p/hint base-state ser.JonSharedData$JonGUIState state-mapper)]
    (p/p-> base-state
           modifications)))

(defn create-state-with-extreme-values
  "Create state with extreme but valid values for boundary testing."
  []
  (let [valid-state (create-valid-state)]
    (p/with-hints [(p/hint valid-state ser.JonSharedData$JonGUIState state-mapper)]
      (p/p-> valid-state
             (assoc-in [:gps :latitude] 89.999999)
             (assoc-in [:gps :longitude] 179.999999)
             (assoc-in [:gps :altitude] 8848.86)  ; Mt. Everest
             (assoc-in [:system :cpu_temperature] 99.9)
             (assoc-in [:system :cpu_load] 100.0)))))

;; ============================================================================
;; CONVERSION UTILITIES
;; ============================================================================

(defn proto-map->bytes
  "Convert a proto-map to byte array."
  [proto-map]
  (p/proto-map->bytes proto-map))

(defn bytes->proto-map
  "Convert byte array to proto-map."
  [mapper clazz byte-array]
  (p/bytes->proto-map mapper clazz byte-array))

;; ============================================================================
;; TEST DATA GETTERS - Convenience functions for tests
;; ============================================================================

(defn get-valid-state-bytes []
  (proto-map->bytes (create-valid-state)))

(defn get-ping-cmd-bytes []
  (proto-map->bytes (create-ping-cmd)))

(defn get-noop-cmd-bytes []
  (proto-map->bytes (create-noop-cmd)))

(defn get-frozen-cmd-bytes []
  (proto-map->bytes (create-frozen-cmd)))

(defn get-invalid-client-type-cmd-bytes []
  (proto-map->bytes (create-invalid-client-type-cmd)))

(defn get-invalid-protocol-cmd-bytes []
  (proto-map->bytes (create-invalid-protocol-cmd)))

(defn get-invalid-gps-state-bytes []
  (proto-map->bytes (create-invalid-gps-state)))

(defn get-invalid-protocol-state-bytes []
  (proto-map->bytes (create-invalid-protocol-state)))

(defn get-state-missing-fields-bytes []
  (proto-map->bytes (create-state-missing-required-fields)))

;; ============================================================================
;; PERFORMANCE NOTES
;; ============================================================================

(comment
  ;; This file demonstrates Pronto best practices:
  
  ;; 1. Load real data from EDN files (state-explorer output)
  ;; 2. Use p/clj-map->proto for converting EDN to proto-maps
  ;; 3. Create invalid data by modifying valid proto-maps with p/p->
  ;; 4. Always use hints with p/p-> for performance
  ;; 5. Batch multiple field updates in single p/p-> call
  ;; 6. Define mappers once and reuse them
  
  ;; What NOT to do:
  ;; - Don't use Java builders directly
  ;; - Don't create invalid data from scratch
  ;; - Don't chain individual assocs without p/p->
  ;; - Don't convert to/from EDN unnecessarily
  )