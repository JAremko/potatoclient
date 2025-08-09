(ns buff-validate.test-harness
  "Test harness with real state EDN data and Pronto best practices.
   This provides idiomatic test data creation using real captured states."
  (:require [pronto.core :as p]
            [clojure.edn :as edn])
  (:import [cmd JonSharedCmd$Root JonSharedCmd$Ping JonSharedCmd$Noop JonSharedCmd$Frozen]
           [cmd.RotaryPlatform JonSharedCmdRotary$Root JonSharedCmdRotary$RotateAzimuthTo 
                                JonSharedCmdRotary$SetMode JonSharedCmdRotary$Stop]
           [ser JonSharedData$JonGUIState]))

;; ============================================================================
;; PERFORMANCE-OPTIMIZED MAPPER DEFINITIONS
;; Define once, reuse everywhere
;; ============================================================================

(p/defmapper cmd-mapper [cmd.JonSharedCmd$Root])
(p/defmapper rotary-cmd-mapper [cmd.RotaryPlatform.JonSharedCmdRotary$Root])
(p/defmapper state-mapper [ser.JonSharedData$JonGUIState])

;; ============================================================================
;; REAL CAPTURED STATE DATA
;; This is actual state data from a running system
;; ============================================================================

(def real-state-edn
  "Real state captured from state-explorer at timestamp 1754664759800"
  {:actual-space-time {:altitude 0.289371
                       :azimuth 256.62
                       :elevation 7.04
                       :latitude 50.023632
                       :longitude 15.81521
                       :timestamp 1754664759}
   :camera-day {:clahe-level 0.16
                :digital-zoom-level 1.0
                :focus-pos 1.0
                :fx-mode :jon-gui-data-fx-mode-day-a
                :infrared-filter true
                :iris-pos 0.03
                :zoom-pos 0.59938735
                :zoom-table-pos 3
                :zoom-table-pos-max 4}
   :camera-heat {:agc-mode :jon-gui-data-video-channel-heat-agc-mode-2
                 :clahe-level 0.5
                 :digital-zoom-level 1.0
                 :filter :jon-gui-data-video-channel-heat-filter-hot-white
                 :fx-mode :jon-gui-data-fx-mode-heat-a
                 :zoom-table-pos 3
                 :zoom-table-pos-max 4}
   :compass {:azimuth 333.50625
             :bank 0.84375
             :elevation 3.54375}
   :compass-calibration {:final-stage 12
                         :status :jon-gui-data-compass-calibrate-status-not-calibrating
                         :target-azimuth 56.25
                         :target-bank -5.625
                         :target-elevation 6.75}
   :day-cam-glass-heater {}
   :gps {:altitude 0.289371
         :fix-type :jon-gui-data-gps-fix-type-3d
         :latitude 50.023632
         :longitude 15.815209999999999
         :manual-latitude 50.023604
         :manual-longitude 15.815316}
   :lrf {:measure-id 52
         :pointer-mode :jon-gui-data-lrf-laser-pointer-mode-off
         :target {:observer-azimuth 356.40000000000003
                  :observer-elevation -0.675
                  :observer-fix-type :jon-gui-data-gps-fix-type-2d
                  :observer-latitude 8.0
                  :observer-longitude 7.0
                  :target-id 52
                  :target-latitude 50.023638999999996
                  :target-longitude 15.815211999999999
                  :timestamp 1754576916
                  :uuid-part1 -494581931
                  :uuid-part2 -224575107}}
   :meteo-internal {}
   :protocol-version 1
   :rec-osd {:day-osd-enabled true
             :heat-osd-enabled true
             :screen :jon-gui-data-rec-osd-screen-main}
   :rotary {:azimuth 333.50626
            :elevation 7.04
            :mode :jon-gui-data-rotary-mode-position}
   :system {:cpu-load 12.94
            :cpu-temperature 53.33
            :cur-video-rec-dir-day 7
            :cur-video-rec-dir-hour 17
            :cur-video-rec-dir-minute 28
            :cur-video-rec-dir-month 1
            :cur-video-rec-dir-second 47
            :cur-video-rec-dir-year 2025
            :disk-space 34
            :gpu-load 3.0
            :gpu-temperature 48.0
            :important-rec-enabled false
            :loc :jon-gui-data-system-localization-en
            :low-disk-space false
            :no-disk-space false
            :power-consumption 0.0
            :rec-enabled true
            :recognition-mode false}
   :time {:manual-timestamp 1754664759
          :timestamp 1754664759
          :use-manual-time false
          :zone-id 3600}})

;; ============================================================================
;; COMPLEX COMMAND CREATION - Demonstrating nested command structures
;; ============================================================================

(defn valid-rotary-azimuth-cmd
  "Create a complex rotary command to rotate azimuth to specific position."
  []
  (p/proto-map cmd-mapper cmd.JonSharedCmd$Root
               :protocol_version 1
               :session_id 5000
               :client_type :JON_GUI_DATA_CLIENT_TYPE_LOCAL_NETWORK
               :rotary (p/proto-map rotary-cmd-mapper cmd.RotaryPlatform.JonSharedCmdRotary$Root
                                   :rotate_azimuth_to 
                                   (p/proto-map rotary-cmd-mapper 
                                               cmd.RotaryPlatform.JonSharedCmdRotary$RotateAzimuthTo
                                               :target_value 180.0
                                               :speed 10.0
                                               :direction :JON_GUI_DATA_ROTARY_DIRECTION_CLOCKWISE))))

(defn valid-rotary-stop-cmd
  "Create a rotary stop command."
  []
  (p/proto-map cmd-mapper cmd.JonSharedCmd$Root
               :protocol_version 1
               :session_id 6000
               :client_type :JON_GUI_DATA_CLIENT_TYPE_INTERNAL_CV
               :rotary (p/proto-map rotary-cmd-mapper cmd.RotaryPlatform.JonSharedCmdRotary$Root
                                   :stop (p/proto-map rotary-cmd-mapper 
                                                      cmd.RotaryPlatform.JonSharedCmdRotary$Stop))))

;; ============================================================================
;; VALID DATA CREATION - Idiomatic Pronto approach
;; ============================================================================

(defn valid-state
  "Create a valid state proto-map from real EDN data.
   This is the foundation for all state-based tests."
  []
  (p/clj-map->proto-map state-mapper ser.JonSharedData$JonGUIState real-state-edn))

(defn valid-ping-cmd
  "Create a valid ping command using direct proto-map creation."
  []
  (p/proto-map cmd-mapper cmd.JonSharedCmd$Root
               :protocol_version 1
               :session_id 1000
               :client_type :JON_GUI_DATA_CLIENT_TYPE_LOCAL_NETWORK
               :ping (p/proto-map cmd-mapper cmd.JonSharedCmd$Ping)))

(defn valid-noop-cmd
  "Create a valid noop command."
  []
  (p/proto-map cmd-mapper cmd.JonSharedCmd$Root
               :protocol_version 1
               :session_id 2000
               :client_type :JON_GUI_DATA_CLIENT_TYPE_INTERNAL_CV
               :noop (p/proto-map cmd-mapper cmd.JonSharedCmd$Noop)))

(defn valid-frozen-cmd
  "Create a valid frozen command."
  []
  (p/proto-map cmd-mapper cmd.JonSharedCmd$Root
               :protocol_version 1
               :session_id 3000
               :client_type :JON_GUI_DATA_CLIENT_TYPE_LOCAL_NETWORK
               :frozen (p/proto-map cmd-mapper cmd.JonSharedCmd$Frozen)))

;; ============================================================================
;; INVALID DATA CREATION - Performant modification of valid data
;; ============================================================================

(defn invalid-gps-state
  "Create state with out-of-range GPS coordinates.
   Demonstrates performant p/p-> usage with hints."
  []
  (let [state (valid-state)]
    (p/with-hints [(p/hint state ser.JonSharedData$JonGUIState state-mapper)]
      (p/p-> state
             (assoc-in [:gps :latitude] 200.0)      ; Invalid: > 90
             (assoc-in [:gps :longitude] -400.0))))) ; Invalid: < -180

(defn invalid-protocol-state
  "State with invalid protocol version."
  []
  (let [state (valid-state)]
    (p/with-hints [(p/hint state ser.JonSharedData$JonGUIState state-mapper)]
      (p/p-> state
             (assoc :protocol_version 0))))) ; Must be > 0

(defn invalid-client-type-cmd
  "Command with invalid client type (UNSPECIFIED)."
  []
  (p/proto-map cmd-mapper cmd.JonSharedCmd$Root
               :protocol_version 1
               :session_id 4000
               :client_type :JON_GUI_DATA_CLIENT_TYPE_UNSPECIFIED ; Not allowed
               :ping (p/proto-map cmd-mapper cmd.JonSharedCmd$Ping)))

(defn invalid-protocol-cmd
  "Command with invalid protocol version."
  []
  (p/proto-map cmd-mapper cmd.JonSharedCmd$Root
               :protocol_version 0  ; Must be > 0
               :session_id 5000
               :client_type :JON_GUI_DATA_CLIENT_TYPE_LOCAL_NETWORK
               :noop (p/proto-map cmd-mapper cmd.JonSharedCmd$Noop)))

(defn state-missing-required-fields
  "State missing all required nested messages.
   Tests validation of required fields."
  []
  (p/proto-map state-mapper ser.JonSharedData$JonGUIState
               :protocol_version 1)) ; Missing time, system, gps, etc.

;; ============================================================================
;; BOUNDARY TESTING - Valid but extreme values
;; ============================================================================

(defn state-with-boundary-values
  "State with valid but extreme values for boundary testing."
  []
  (let [state (valid-state)]
    (p/with-hints [(p/hint state ser.JonSharedData$JonGUIState state-mapper)]
      (p/p-> state
             (assoc-in [:gps :latitude] 90.0)        ; North Pole
             (assoc-in [:gps :longitude] 180.0)      ; International Date Line
             (assoc-in [:gps :altitude] 8848.86)     ; Mt. Everest
             (assoc-in [:system :cpu_temperature] 100.0)
             (assoc-in [:system :cpu_load] 100.0)
             (assoc-in [:compass :azimuth] 359.999)
             (assoc-in [:rotary :elevation] 90.0)))))

;; ============================================================================
;; UTILITY FUNCTIONS
;; ============================================================================

(defn ->bytes
  "Convert proto-map to byte array for validation."
  [proto-map]
  (p/proto-map->bytes proto-map))

(defn bytes->
  "Convert byte array back to proto-map."
  [mapper clazz byte-array]
  (p/bytes->proto-map mapper clazz byte-array))

(defn modify-state
  "Performantly modify a state with a function.
   The function receives the state and should return p/p-> operations."
  [state-proto-map modifications-fn]
  (p/with-hints [(p/hint state-proto-map ser.JonSharedData$JonGUIState state-mapper)]
    (modifications-fn state-proto-map)))

;; ============================================================================
;; TEST DATA ACCESSORS - Convenience functions for tests
;; ============================================================================

(def valid-state-bytes (memoize #(->bytes (valid-state))))
(def valid-ping-bytes (memoize #(->bytes (valid-ping-cmd))))
(def valid-noop-bytes (memoize #(->bytes (valid-noop-cmd))))
(def valid-frozen-bytes (memoize #(->bytes (valid-frozen-cmd))))
(def valid-rotary-azimuth-bytes (memoize #(->bytes (valid-rotary-azimuth-cmd))))
(def valid-rotary-stop-bytes (memoize #(->bytes (valid-rotary-stop-cmd))))

(def invalid-gps-state-bytes (memoize #(->bytes (invalid-gps-state))))
(def invalid-protocol-state-bytes (memoize #(->bytes (invalid-protocol-state))))
(def invalid-client-cmd-bytes (memoize #(->bytes (invalid-client-type-cmd))))
(def invalid-protocol-cmd-bytes (memoize #(->bytes (invalid-protocol-cmd))))
(def missing-fields-state-bytes (memoize #(->bytes (state-missing-required-fields))))
(def boundary-state-bytes (memoize #(->bytes (state-with-boundary-values))))

;; ============================================================================
;; PERFORMANCE NOTES
;; ============================================================================

(comment
  ;; This harness demonstrates Pronto best practices:
  
  ;; 1. Real data as foundation - using actual captured state
  ;; 2. Mappers defined once and reused
  ;; 3. Direct proto-map creation for new messages
  ;; 4. p/p-> with hints for modifications
  ;; 5. Memoized byte conversions for repeated test use
  ;; 6. No Java builder patterns
  ;; 7. No unnecessary EDN conversions
  ;; 8. Batch operations in single p/p-> calls
  
  ;; Usage in tests:
  ;; (require '[buff-validate.test-harness :as h])
  ;; (validator/validate-binary (h/valid-state-bytes) :type :state)
  )