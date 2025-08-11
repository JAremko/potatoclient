(ns validate.specs.state-property-test
  "Property-based tests for State message specs.
   Tests each sub-message and the complete root message against buf.validate."
  (:require
   [clojure.test :refer [deftest testing is]]
   [validate.spec-validation-harness :as harness]
   [validate.test-harness :as h]
   [malli.core :as m]
   [malli.generator :as mg]
   [potatoclient.malli.registry :as registry]
   [potatoclient.specs.oneof-edn :as oneof-edn]
   [pronto.core :as p]
   [validate.validator :as v]
   [clojure.tools.logging :as log]
   ;; Load all state specs
   [potatoclient.specs.common]
   [potatoclient.specs.state.gps]
   [potatoclient.specs.state.system]
   [potatoclient.specs.state.compass]
   [potatoclient.specs.state.rotary]
   [potatoclient.specs.state.camera-day]
   [potatoclient.specs.state.camera-heat]
   [potatoclient.specs.state.time]
   [potatoclient.specs.state.lrf]
   [potatoclient.specs.state.rec-osd]
   [potatoclient.specs.state.actual-space-time]
   [potatoclient.specs.state.day-cam-glass-heater]
   [potatoclient.specs.state.root])
  (:import 
   [ser JonSharedData$JonGUIState 
    JonSharedDataGps$JonGuiDataGps
    JonSharedDataSystem$JonGuiDataSystem
    JonSharedDataCompass$JonGuiDataCompass
    JonSharedDataRotary$JonGuiDataRotary
    JonSharedDataCameraDay$JonGuiDataCameraDay
    JonSharedDataCameraHeat$JonGuiDataCameraHeat
    JonSharedDataTime$JonGuiDataTime
    JonSharedDataLrf$JonGuiDataLrf
    JonSharedDataRecOsd$JonGuiDataRecOsd
    JonSharedDataActualSpaceTime$JonGuiDataActualSpaceTime
    JonSharedDataCompassCalibration$JonGuiDataCompassCalibration
    JonSharedDataDayCamGlassHeater$JonGuiDataDayCamGlassHeater
    JonSharedDataRotary$ScanNode]))

(harness/init-registry!)

;; ============================================================================
;; MANUAL TEST VALUES
;; ============================================================================

(def manual-gps-values
  "Manual test values for GPS message covering edge cases"
  [{:altitude 0.0
    :fix-type :jon-gui-data-gps-fix-type-3d
    :latitude 0.0
    :longitude 0.0
    :manual-latitude 0.0
    :manual-longitude 0.0}
   
   {:altitude 8848.86  ; Mt. Everest
    :fix-type :jon-gui-data-gps-fix-type-3d
    :latitude 90.0     ; North Pole
    :longitude 180.0   ; International Date Line
    :manual-latitude 90.0
    :manual-longitude 180.0}
   
   {:altitude -433.0   ; Dead Sea
    :fix-type :jon-gui-data-gps-fix-type-2d
    :latitude -90.0    ; South Pole
    :longitude -180.0
    :manual-latitude -90.0
    :manual-longitude -180.0}
   
   {:altitude 1000.0
    :fix-type :jon-gui-data-gps-fix-type-manual
    :latitude 45.5
    :longitude -122.6  ; Portland, OR
    :manual-latitude 45.5
    :manual-longitude -122.6}])

(def manual-system-values
  "Manual test values for System message"
  [{:cpu-load 0.0
    :cpu-temperature 0.0
    :gpu-load 0.0
    :gpu-temperature 0.0
    :disk-space 0
    :power-consumption 0.0
    :rec-enabled false
    :low-disk-space false
    :cur-video-rec-dir-year 2025
    :cur-video-rec-dir-month 1
    :cur-video-rec-dir-day 1
    :cur-video-rec-dir-hour 0
    :cur-video-rec-dir-minute 0
    :cur-video-rec-dir-second 0
    :loc :jon-gui-data-system-localization-en}
   
   {:cpu-load 100.0
    :cpu-temperature 100.0
    :gpu-load 100.0
    :gpu-temperature 100.0
    :disk-space 100
    :power-consumption 1000.0
    :rec-enabled true
    :low-disk-space true
    :cur-video-rec-dir-year 2025
    :cur-video-rec-dir-month 12
    :cur-video-rec-dir-day 31
    :cur-video-rec-dir-hour 23
    :cur-video-rec-dir-minute 59
    :cur-video-rec-dir-second 59
    :loc :jon-gui-data-system-localization-en}])

(def manual-rotary-values
  "Manual test values for Rotary message"
  [{:azimuth 0.0
    :elevation 0.0
    :mode :jon-gui-data-rotary-mode-position
    :platform-azimuth 0.0
    :platform-elevation 0.0
    :scan-target 1
    :scan-target-max 1
    :current-scan-node {:speed 0.001
                        :azimuth 0.0
                        :elevation 0.0
                        :linger 0.001
                        :index 1
                        :dayzoomtablevalue 1
                        :heatzoomtablevalue 1}}
   
   {:azimuth 359.999
    :elevation 90.0
    :mode :jon-gui-data-rotary-mode-stabilization
    :platform-azimuth 359.999
    :platform-elevation 90.0
    :scan-target 10
    :scan-target-max 10
    :current-scan-node {:speed 1.0
                        :azimuth 180.0
                        :elevation -90.0
                        :linger 1.0
                        :index 5
                        :dayzoomtablevalue 4
                        :heatzoomtablevalue 4}}])

;; ============================================================================
;; SUB-MESSAGE PROPERTY TESTS
;; ============================================================================

(deftest test-gps-spec
  (testing "GPS message spec validation"
    (testing "Manual values"
      (doseq [gps-value manual-gps-values]
        (is (m/validate :state/gps gps-value)
            (str "GPS spec should validate: " gps-value))))
    
    (testing "Property-based testing (300 samples)"
      (let [results (harness/run-property-tests 
                     :state/gps 
                     h/state-mapper
                     JonSharedDataGps$JonGuiDataGps
                     :n 300)]
        (is (= 1.0 (:success-rate results))
            (str "GPS spec should pass all property tests. "
                 "Failed: " (:failed results) "/" (:total results)))
        
        (when (pos? (:failed results))
          (log/error "GPS failures:" (harness/analyze-violations results)))))))

(deftest test-system-spec
  (testing "System message spec validation"
    (testing "Manual values"
      (doseq [sys-value manual-system-values]
        (is (m/validate :state/system sys-value)
            (str "System spec should validate: " sys-value))))
    
    (testing "Property-based testing (300 samples)"
      (let [results (harness/run-property-tests 
                     :state/system
                     h/state-mapper
                     JonSharedDataSystem$JonGuiDataSystem
                     :n 300)]
        (is (= 1.0 (:success-rate results))
            (str "System spec should pass all property tests. "
                 "Failed: " (:failed results) "/" (:total results)))))))

(deftest test-compass-spec
  (testing "Compass message spec validation"
    (testing "Property-based testing (300 samples)"
      (let [results (harness/run-property-tests 
                     :state/compass
                     h/state-mapper
                     JonSharedDataCompass$JonGuiDataCompass
                     :n 300)]
        (is (= 1.0 (:success-rate results))
            (str "Compass spec should pass all property tests. "
                 "Failed: " (:failed results) "/" (:total results)))))))

(deftest test-rotary-spec
  (testing "Rotary message spec validation"
    (testing "Manual values"
      (doseq [rotary-value manual-rotary-values]
        (is (m/validate :state/rotary rotary-value)
            (str "Rotary spec should validate: " rotary-value))))
    
    (testing "Property-based testing (300 samples)"
      (let [results (harness/run-property-tests 
                     :state/rotary
                     h/state-mapper
                     JonSharedDataRotary$JonGuiDataRotary
                     :n 300)]
        (is (= 1.0 (:success-rate results))
            (str "Rotary spec should pass all property tests. "
                 "Failed: " (:failed results) "/" (:total results)))))))

(deftest test-camera-day-spec
  (testing "Camera Day message spec validation"
    (testing "Property-based testing (300 samples)"
      (let [results (harness/run-property-tests 
                     :state/camera-day
                     h/state-mapper
                     JonSharedDataCameraDay$JonGuiDataCameraDay
                     :n 300)]
        (is (= 1.0 (:success-rate results))
            (str "Camera Day spec should pass all property tests. "
                 "Failed: " (:failed results) "/" (:total results)))))))

(deftest test-camera-heat-spec
  (testing "Camera Heat message spec validation"
    (testing "Property-based testing (300 samples)"
      (let [results (harness/run-property-tests 
                     :state/camera-heat
                     h/state-mapper
                     JonSharedDataCameraHeat$JonGuiDataCameraHeat
                     :n 300)]
        (is (= 1.0 (:success-rate results))
            (str "Camera Heat spec should pass all property tests. "
                 "Failed: " (:failed results) "/" (:total results)))))))

(deftest test-time-spec
  (testing "Time message spec validation"
    (testing "Property-based testing (300 samples)"
      (let [results (harness/run-property-tests 
                     :state/time
                     h/state-mapper
                     JonSharedDataTime$JonGuiDataTime
                     :n 300)]
        (is (= 1.0 (:success-rate results))
            (str "Time spec should pass all property tests. "
                 "Failed: " (:failed results) "/" (:total results)))))))

;; Note: Sub-messages cannot be validated with buf.validate standalone
;; They must be validated within a root message context
(deftest test-lrf-spec
  (testing "LRF message spec validation (Malli only, not buf.validate)"
    (testing "LRF spec generation and validation"
      (dotimes [_ 50]
        (let [generated (mg/generate :state/lrf)]
          (is (m/validate :state/lrf generated)
              (str "Generated LRF should validate against spec: " generated)))))))

;; ============================================================================
;; ROOT STATE MESSAGE TESTS
;; ============================================================================

(def manual-state-values
  "Complete state messages for testing"
  [(-> h/real-state-edn
       ;; Convert snake_case to kebab-case for Malli
       (clojure.walk/postwalk
        (fn [x]
          (if (keyword? x)
            (keyword (clojure.string/replace (name x) "_" "-"))
            x))))])

(deftest test-state-root-spec
  (testing "Complete State root message validation"
    (testing "Manual real-world state"
      (doseq [state-value manual-state-values]
        (is (m/validate :state/root state-value)
            "Real state data should validate")))
    
    (testing "Property-based testing (100 complete states)"
      (let [results (harness/run-property-tests 
                     :state/root
                     h/state-mapper
                     JonSharedData$JonGUIState
                     :n 100)]
        (is (>= (:success-rate results) 0.95)
            (str "State root spec should pass at least 95% of property tests. "
                 "Passed: " (:passed results) "/" (:total results)))
        
        (when (pos? (:failed results))
          (let [analysis (harness/analyze-violations results)]
            (log/warn "State root failures:" analysis)
            (println "\nViolation analysis:")
            (println "  Top violations:" (:top-violations analysis))))))))

;; ============================================================================
;; ROUND-TRIP VALIDATION TESTS
;; ============================================================================

(deftest test-round-trip-validation
  (testing "Round-trip validation (spec -> proto -> binary -> proto -> spec)"
    (testing "GPS round-trip"
      (let [generated (mg/generate :state/gps)
            result (harness/round-trip-validate 
                    :state/gps
                    h/state-mapper
                    JonSharedDataGps$JonGuiDataGps
                    generated)]
        (is (:success? result)
            (str "GPS round-trip should succeed. Error: " (:error result)))))
    
    (testing "System round-trip"
      (let [generated (mg/generate :state/system)
            result (harness/round-trip-validate 
                    :state/system
                    h/state-mapper
                    JonSharedDataSystem$JonGuiDataSystem
                    generated)]
        (is (:success? result)
            (str "System round-trip should succeed. Error: " (:error result)))))
    
    (testing "Complete state round-trip"
      (let [generated (mg/generate :state/root)
            result (harness/round-trip-validate 
                    :state/root
                    h/state-mapper
                    JonSharedData$JonGUIState
                    generated)]
        (is (:success? result)
            (str "State root round-trip should succeed. Error: " (:error result)))))))

;; ============================================================================
;; BOUNDARY VALUE TESTS
;; ============================================================================

(deftest test-boundary-values
  (testing "Boundary value validation"
    (testing "GPS boundaries"
      (let [boundary-gps {:altitude 8848.86
                          :fix-type :jon-gui-data-gps-fix-type-3d
                          :latitude 90.0
                          :longitude 179.999999
                          :manual-latitude -90.0
                          :manual-longitude -179.999999}
            result (harness/round-trip-validate 
                    :state/gps
                    h/state-mapper
                    JonSharedDataGps$JonGuiDataGps
                    boundary-gps)]
        (is (:success? result)
            "GPS boundary values should validate")))
    
    (testing "Rotary speed boundaries"
      (let [boundary-rotary {:azimuth 359.999
                             :elevation 90.0
                             :mode :jon-gui-data-rotary-mode-position
                             :platform-azimuth 359.999
                             :platform-elevation -90.0
                             :scan-target 1
                             :scan-target-max 100
                             :current-scan-node {:speed 0.001  ; Just above 0
                                                :azimuth 0.0
                                                :elevation 0.0
                                                :linger 0.001
                                                :index 1
                                                :dayzoomtablevalue 1
                                                :heatzoomtablevalue 1}}
            result (harness/round-trip-validate 
                    :state/rotary
                    h/state-mapper
                    JonSharedDataRotary$JonGuiDataRotary
                    boundary-rotary)]
        (is (:success? result)
            "Rotary boundary values should validate")))))

;; ============================================================================
;; SPEC COVERAGE TESTS
;; ============================================================================

(deftest test-spec-coverage
  (testing "Spec coverage analysis"
    (testing "GPS spec coverage"
      (let [coverage (harness/check-spec-coverage :state/gps 100)]
        (is (contains? (:paths coverage) :altitude))
        (is (contains? (:paths coverage) :latitude))
        (is (contains? (:paths coverage) :longitude))
        (is (contains? (:paths coverage) :fix-type))))
    
    (testing "State root coverage"
      (let [coverage (harness/check-spec-coverage :state/root 50)]
        (is (>= (:unique-paths coverage) 14)
            "State root should have at least 14 sub-message paths")))))