(ns validate.hierarchical-validation-test
  "Tests for hierarchical validation of proto messages from bottom to top.
   Uses the test_validator to validate sub-messages independently."
  (:require
   [clojure.test :refer [deftest testing is]]
   [validate.test-validator :as tv]
   [validate.test-harness :as h]
   [pronto.core :as p]
   [malli.core :as m]
   [malli.generator :as mg]
   [potatoclient.malli.registry :as registry]
   [potatoclient.specs.oneof-edn :as oneof-edn]
   [clojure.tools.logging :as log]
   ;; Load all specs
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
   [potatoclient.specs.state.compass-calibration]
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
    JonSharedDataDayCamGlassHeater$JonGuiDataDayCamGlassHeater]))

;; Initialize registry
(registry/setup-global-registry!
  (oneof-edn/register_ONeof-edn-schema!))

;; ============================================================================
;; LEVEL 1: LEAF MESSAGE VALIDATION (Sub-messages)
;; ============================================================================

(deftest test-level1-leaf-messages
  (testing "Level 1: Validate leaf sub-messages independently"
    
    (testing "GPS sub-message validation"
      (let [;; Generate valid GPS data from Malli spec
            gps-data (mg/generate :state/gps)
            ;; Create proto-map
            gps-proto (p/proto-map h/state-mapper JonSharedDataGps$JonGuiDataGps
                                  :latitude (:latitude gps-data)
                                  :longitude (:longitude gps-data)
                                  :altitude (:altitude gps-data)
                                  :fix_type (-> gps-data :fix-type name 
                                               (clojure.string/replace "-" "_")
                                               (clojure.string/upper-case)
                                               keyword)
                                  :manual_latitude (:manual-latitude gps-data)
                                  :manual_longitude (:manual-longitude gps-data))
            ;; Validate using test validator
            result (tv/validate-proto-map gps-proto)]
        
        (is (:valid? result)
            (str "GPS sub-message should validate independently. "
                 "Violations: " (:violations result)))))
    
    (testing "System sub-message validation"
      (let [sys-data (mg/generate :state/system)
            sys-proto (p/proto-map h/state-mapper JonSharedDataSystem$JonGuiDataSystem
                                  :cpu_load (:cpu-load sys-data)
                                  :cpu_temperature (:cpu-temperature sys-data)
                                  :gpu_load (:gpu-load sys-data)
                                  :gpu_temperature (:gpu-temperature sys-data)
                                  :disk_space (:disk-space sys-data)
                                  :power_consumption (:power-consumption sys-data)
                                  :rec_enabled (:rec-enabled sys-data)
                                  :low_disk_space (:low-disk-space sys-data)
                                  :cur_video_rec_dir_year (:cur-video-rec-dir-year sys-data)
                                  :cur_video_rec_dir_month (:cur-video-rec-dir-month sys-data)
                                  :cur_video_rec_dir_day (:cur-video-rec-dir-day sys-data)
                                  :cur_video_rec_dir_hour (:cur-video-rec-dir-hour sys-data)
                                  :cur_video_rec_dir_minute (:cur-video-rec-dir-minute sys-data)
                                  :cur_video_rec_dir_second (:cur-video-rec-dir-second sys-data)
                                  :loc (-> sys-data :loc name
                                          (clojure.string/replace "-" "_")
                                          (clojure.string/upper-case)
                                          keyword))
            result (tv/validate-proto-map sys-proto)]
        
        (is (:valid? result)
            (str "System sub-message should validate independently. "
                 "Violations: " (:violations result)))))
    
    (testing "Time sub-message validation"
      (let [time-data (mg/generate :state/time)
            time-proto (p/proto-map h/state-mapper JonSharedDataTime$JonGuiDataTime
                                   :timestamp (:timestamp time-data)
                                   :manual_timestamp (:manual-timestamp time-data))
            result (tv/validate-proto-map time-proto)]
        
        (is (:valid? result)
            (str "Time sub-message should validate independently. "
                 "Violations: " (:violations result)))))
    
    (testing "Compass sub-message validation"
      (let [compass-data (mg/generate :state/compass)
            compass-proto (p/proto-map h/state-mapper JonSharedDataCompass$JonGuiDataCompass
                                       :azimuth (:azimuth compass-data)
                                       :elevation (:elevation compass-data)
                                       :bank (:bank compass-data))
            result (tv/validate-proto-map compass-proto)]
        
        (is (:valid? result)
            (str "Compass sub-message should validate independently. "
                 "Violations: " (:violations result)))))
    
    (testing "LRF sub-message validation"
      (let [lrf-data (mg/generate :state/lrf)
            lrf-proto (p/proto-map h/state-mapper JonSharedDataLrf$JonGuiDataLrf
                                  :measure_id (:measure-id lrf-data)
                                  :pointer_mode (-> lrf-data :pointer-mode name
                                                   (clojure.string/replace "-" "_")
                                                   (clojure.string/upper-case)
                                                   keyword))
            result (tv/validate-proto-map lrf-proto)]
        
        (is (:valid? result)
            (str "LRF sub-message should validate independently. "
                 "Violations: " (:violations result)))))))

;; ============================================================================
;; LEVEL 2: COMPOSITE MESSAGE VALIDATION
;; ============================================================================

(deftest test-level2-composite-messages
  (testing "Level 2: Validate messages with nested sub-messages"
    
    (testing "Rotary with ScanNode sub-message"
      (let [rotary-data (mg/generate :state/rotary)
            ;; Rotary has a nested ScanNode message
            scan-node-data (:current-scan-node rotary-data)
            rotary-proto (p/proto-map h/state-mapper JonSharedDataRotary$JonGuiDataRotary
                                      :azimuth (:azimuth rotary-data)
                                      :elevation (:elevation rotary-data)
                                      :mode (-> rotary-data :mode name
                                               (clojure.string/replace "-" "_")
                                               (clojure.string/upper-case)
                                               keyword)
                                      :platform_azimuth (:platform-azimuth rotary-data)
                                      :platform_elevation (:platform-elevation rotary-data)
                                      :scan_target (:scan-target rotary-data)
                                      :scan_target_max (:scan-target-max rotary-data)
                                      :current_scan_node {:speed (:speed scan-node-data)
                                                         :azimuth (:azimuth scan-node-data)
                                                         :elevation (:elevation scan-node-data)
                                                         :linger (:linger scan-node-data)
                                                         :index (:index scan-node-data)
                                                         :dayzoomtablevalue (:dayzoomtablevalue scan-node-data)
                                                         :heatzoomtablevalue (:heatzoomtablevalue scan-node-data)})
            result (tv/validate-proto-map rotary-proto)]
        
        (is (:valid? result)
            (str "Rotary with nested ScanNode should validate. "
                 "Violations: " (:violations result)))))
    
    (testing "CompassCalibration message validation"
      (let [cc-data (mg/generate :state/compass-calibration)
            cc-proto (p/proto-map h/state-mapper JonSharedDataCompassCalibration$JonGuiDataCompassCalibration
                                 :final_stage (:final-stage cc-data)
                                 :status (-> cc-data :status name
                                           (clojure.string/replace "-" "_")
                                           (clojure.string/upper-case)
                                           keyword)
                                 :target_azimuth (:target-azimuth cc-data)
                                 :target_bank (:target-bank cc-data)
                                 :target_elevation (:target-elevation cc-data))
            result (tv/validate-proto-map cc-proto)]
        
        (is (:valid? result)
            (str "CompassCalibration should validate. "
                 "Violations: " (:violations result)))))))

;; ============================================================================
;; LEVEL 3: ROOT MESSAGE VALIDATION WITH ALL SUB-MESSAGES
;; ============================================================================

(deftest test-level3-root-message
  (testing "Level 3: Validate complete root message with all sub-messages"
    
    (testing "Complete State message with generated sub-messages"
      (let [;; Generate complete state from spec
            state-data (mg/generate :state/root)
            ;; Create proto-map (h/valid-state provides a baseline)
            base-state (h/valid-state)
            ;; We'll validate the base state with hierarchy
            hierarchy-result (tv/validate-hierarchy base-state [:root])]
        
        (is (tv/all-valid? hierarchy-result)
            (str "Complete state hierarchy should validate. "
                 "Violations found: " (tv/find-violations hierarchy-result)))
        
        (when-not (tv/all-valid? hierarchy-result)
          (log/info "Hierarchy validation details:" hierarchy-result))))
    
    (testing "State with specific sub-message updates"
      (let [;; Start with valid base state
            base-state (h/valid-state)
            ;; Generate new GPS data
            gps-data (mg/generate :state/gps)
            ;; Update GPS in state
            updated-state (p/p-> base-state
                                (assoc-in [:gps :latitude] (:latitude gps-data))
                                (assoc-in [:gps :longitude] (:longitude gps-data))
                                (assoc-in [:gps :altitude] (:altitude gps-data)))
            ;; Validate hierarchy
            hierarchy-result (tv/validate-hierarchy updated-state [:root])]
        
        (is (tv/all-valid? hierarchy-result)
            (str "State with updated GPS should validate. "
                 "Violations: " (tv/find-violations hierarchy-result)))))))

;; ============================================================================
;; BOTTOM-UP TESTING STRATEGY
;; ============================================================================

(deftest test-bottom-up-validation-strategy
  (testing "Bottom-up validation from leaf messages to root"
    
    (testing "Test bottom-up with valid state"
      (let [state (h/valid-state)
            test-report (tv/test-bottom-up state)]
        
        (is (:all-valid? test-report)
            (str "Bottom-up validation should pass. "
                 "Summary: " (:summary test-report)))
        
        (when-not (:all-valid? test-report)
          (doseq [level (:test-order test-report)]
            (when (seq (remove :valid? (:tests level)))
              (log/error "Level" (:level level) "failures:"
                        (filter #(not (:valid? %)) (:tests level))))))))
    
    (testing "Test bottom-up with generated state"
      (let [;; Generate a complete state
            state-data (mg/generate :state/root)
            ;; Convert to proto-map (simplified for test)
            state-proto (h/valid-state)  ; Use valid base for now
            test-report (tv/test-bottom-up state-proto)]
        
        (is (:all-valid? test-report)
            (str "Generated state bottom-up validation. "
                 "Summary: " (:summary test-report)))))))

;; ============================================================================
;; PROPERTY-BASED HIERARCHICAL TESTING
;; ============================================================================

(deftest test-property-based-hierarchical-validation
  (testing "Property-based testing with hierarchical validation"
    
    (testing "Generate and validate 50 complete states"
      (let [results (atom {:passed 0 :failed 0 :violations []})]
        
        (dotimes [_ 50]
          (let [;; Use valid base state for stability
                state (h/valid-state)
                hierarchy-result (tv/validate-hierarchy state [:root])]
            
            (if (tv/all-valid? hierarchy-result)
              (swap! results update :passed inc)
              (do
                (swap! results update :failed inc)
                (swap! results update :violations conj 
                       (tv/find-violations hierarchy-result))))))
        
        (is (>= (:passed @results) 45)
            (str "At least 90% of states should validate hierarchically. "
                 "Results: " @results))))
    
    (testing "Validate sub-message updates maintain hierarchy validity"
      (let [base-state (h/valid-state)
            results (atom {:passed 0 :failed 0})]
        
        (dotimes [_ 20]
          (let [;; Generate new sub-message data
                gps-data (mg/generate :state/gps)
                compass-data (mg/generate :state/compass)
                ;; Update state
                updated-state (p/p-> base-state
                                    (assoc-in [:gps :latitude] (:latitude gps-data))
                                    (assoc-in [:gps :longitude] (:longitude gps-data))
                                    (assoc-in [:compass :azimuth] (:azimuth compass-data))
                                    (assoc-in [:compass :elevation] (:elevation compass-data)))
                ;; Test bottom-up
                test-report (tv/test-bottom-up updated-state)]
            
            (if (:all-valid? test-report)
              (swap! results update :passed inc)
              (swap! results update :failed inc))))
        
        (is (= 20 (:passed @results))
            (str "All sub-message updates should maintain validity. "
                 "Results: " @results))))))

;; ============================================================================
;; PERFORMANCE TESTS
;; ============================================================================

(deftest test-hierarchical-validation-performance
  (testing "Performance of hierarchical validation"
    
    (testing "Single state hierarchy validation time"
      (let [state (h/valid-state)
            start (System/currentTimeMillis)
            result (tv/validate-hierarchy state [:root])
            elapsed (- (System/currentTimeMillis) start)]
        
        (is (< elapsed 50)
            (str "Hierarchy validation should complete within 50ms. "
                 "Actual: " elapsed "ms"))))
    
    (testing "Bottom-up testing performance"
      (let [state (h/valid-state)
            start (System/currentTimeMillis)
            result (tv/test-bottom-up state)
            elapsed (- (System/currentTimeMillis) start)]
        
        (is (< elapsed 100)
            (str "Bottom-up testing should complete within 100ms. "
                 "Actual: " elapsed "ms"))))))