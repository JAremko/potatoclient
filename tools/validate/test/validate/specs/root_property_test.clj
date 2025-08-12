(ns validate.specs.root-property-test
  "Property-based tests for root messages only.
   Tests cmd/root and state/root against buf.validate."
  (:require
   [clojure.test :refer [deftest testing is]]
   [validate.spec-validation-harness :as harness]
   [validate.test-harness :as h]
   [malli.core :as m]
   [malli.generator :as mg]
   [potatoclient.malli.registry :as registry]
   [potatoclient.specs.oneof-edn :as oneof-edn]
   [clojure.tools.logging :as log]
   ;; Load all specs
   [potatoclient.specs.common]
   [potatoclient.specs.cmd.root]
   [potatoclient.specs.state.root])
  (:import 
   [cmd JonSharedCmd$Root]
   [ser JonSharedData$JonGUIState]))

(harness/init-registry!)

;; ============================================================================
;; PROPERTY-BASED TESTS FOR ROOT MESSAGES
;; ============================================================================

(deftest test-state-root-property
  (testing "State root message property-based validation (1000 samples)"
    (let [results (harness/run-property-tests 
                   :state/root
                   h/state-mapper
                   JonSharedData$JonGUIState
                   :message-type :root
                   :root-type :state
                   :n 1000)]
      (println "\n=== State Root Property Test Results ===")
      (println (str "Success rate: " (* 100 (:success-rate results)) "%"))
      (println (str "Passed: " (:passed results) "/" (:total results)))
      
      (is (>= (:success-rate results) 0.95)
          (str "State root spec should pass at least 95% of property tests. "
               "Actual: " (* 100 (:success-rate results)) "%"))
      
      (when (pos? (:failed results))
        (let [analysis (harness/analyze-violations results)]
          (log/warn "State root failures:" analysis)
          (println "\nFailure analysis:")
          (println "  Top violations:" (:top-violations analysis))
          (println "  First failure:" (-> results :failures first :error)))))))

(deftest test-cmd-root-property
  (testing "Command root message property-based validation (1000 samples)"
    (let [results (harness/run-property-tests 
                   :cmd/root
                   h/cmd-mapper
                   JonSharedCmd$Root
                   :message-type :root
                   :root-type :cmd
                   :n 1000)]
      (println "\n=== Command Root Property Test Results ===")
      (println (str "Success rate: " (* 100 (:success-rate results)) "%"))
      (println (str "Passed: " (:passed results) "/" (:total results)))
      
      (is (>= (:success-rate results) 0.95)
          (str "Command root spec should pass at least 95% of property tests. "
               "Actual: " (* 100 (:success-rate results)) "%"))
      
      (when (pos? (:failed results))
        (let [analysis (harness/analyze-violations results)]
          (log/warn "Command root failures:" analysis)
          (println "\nFailure analysis:")
          (println "  Top violations:" (:top-violations analysis))
          (println "  First failure:" (-> results :failures first :error)))))))

;; ============================================================================
;; ROUND-TRIP VALIDATION TESTS
;; ============================================================================

(deftest test-state-root-round-trip
  (testing "State root round-trip validation (100 samples)"
    (dotimes [i 100]
      (let [generated (mg/generate :state/root)
            result (harness/round-trip-validate 
                    :state/root
                    h/state-mapper
                    JonSharedData$JonGUIState
                    generated
                    :message-type :root
                    :root-type :state)]
        (when-not (:success? result)
          (is false (str "Round-trip " i " failed: " (:error result))))))))

(deftest test-cmd-root-round-trip
  (testing "Command root round-trip validation (100 samples)"
    (dotimes [i 100]
      (let [generated (mg/generate :cmd/root)
            result (harness/round-trip-validate 
                    :cmd/root
                    h/cmd-mapper
                    JonSharedCmd$Root
                    generated
                    :message-type :root
                    :root-type :cmd)]
        (when-not (:success? result)
          (is false (str "Round-trip " i " failed: " (:error result))))))))

;; ============================================================================
;; SPEC COVERAGE TESTS
;; ============================================================================

(deftest test-root-spec-coverage
  (testing "Root message spec coverage"
    (testing "State root coverage"
      (let [coverage (harness/check-spec-coverage :state/root 500)]
        (println "\nState root spec coverage:")
        (println "  Unique paths:" (:unique-paths coverage))
        (is (>= (:unique-paths coverage) 10)
            "State root should generate diverse messages")))
    
    (testing "Command root coverage"
      (let [coverage (harness/check-spec-coverage :cmd/root 500)]
        (println "\nCommand root spec coverage:")
        (println "  Unique paths:" (:unique-paths coverage))
        (is (>= (:unique-paths coverage) 5)
            "Command root should generate diverse command types")))))

;; ============================================================================
;; MANUAL VALUE TESTS
;; ============================================================================

(def manual-state-example
  {:protocol_version 1
   :actual_space_time {:latitude 45.5
                       :longitude -122.6
                       :altitude 100.0
                       :azimuth 180.0
                       :elevation 0.0
                       :timestamp 1754664759}
   :gps {:latitude 45.5
         :longitude -122.6
         :altitude 100.0
         :fix_type :JON_GUI_DATA_GPS_FIX_TYPE_3D
         :manual_latitude 0.0
         :manual_longitude 0.0}
   :system {:cpu_load 50.0
            :cpu_temperature 60.0
            :gpu_load 30.0
            :gpu_temperature 55.0
            :disk_space 50
            :power_consumption 100.0
            :rec_enabled true
            :low_disk_space false
            :cur_video_rec_dir_year 2025
            :cur_video_rec_dir_month 1
            :cur_video_rec_dir_day 11
            :cur_video_rec_dir_hour 12
            :cur_video_rec_dir_minute 30
            :cur_video_rec_dir_second 45
            :loc :JON_GUI_DATA_SYSTEM_LOCALIZATION_EN}
   :time {:timestamp 1754664759
          :manual_timestamp 1754664759}
   :compass {:azimuth 180.0
             :elevation 0.0
             :bank 0.0}
   :compass_calibration {:status :JON_GUI_DATA_COMPASS_CALIBRATE_STATUS_NOT_CALIBRATING
                         :target_azimuth 0.0
                         :target_elevation 0.0
                         :target_bank 0.0
                         :final_stage 0}
   :day_cam_glass_heater {}
   :lrf {:measure_id 1
         :pointer_mode :JON_GUI_DATA_LRF_LASER_POINTER_MODE_OFF}
   :meteo_internal {}
   :rotary {:azimuth 180.0
            :elevation 0.0
            :mode :JON_GUI_DATA_ROTARY_MODE_POSITION
            :platform_azimuth 180.0
            :platform_elevation 0.0}
   :camera_day {:zoom_table_pos 1
                :zoom_table_pos_max 4}
   :camera_heat {:zoom_table_pos 1
                 :zoom_table_pos_max 4}
   :rec_osd {:screen :JON_GUI_DATA_REC_OSD_SCREEN_MAIN}})

(def manual-cmd-example
  {:protocol_version 1
   :client_type :JON_GUI_DATA_CLIENT_TYPE_LOCAL_NETWORK
   :ping {}})

(deftest test-manual-examples
  (testing "Manual example validation"
    (testing "State example"
      (is (m/validate :state/root manual-state-example)
          "Manual state example should validate")
      (let [result (harness/round-trip-validate 
                    :state/root
                    h/state-mapper
                    JonSharedData$JonGUIState
                    manual-state-example
                    :message-type :root
                    :root-type :state)]
        (is (:success? result)
            (str "Manual state round-trip should succeed. Error: " (:error result)))))
    
    (testing "Command example"
      (is (m/validate :cmd/root manual-cmd-example)
          "Manual command example should validate")
      (let [result (harness/round-trip-validate 
                    :cmd/root
                    h/cmd-mapper
                    JonSharedCmd$Root
                    manual-cmd-example
                    :message-type :root
                    :root-type :cmd)]
        (is (:success? result)
            (str "Manual command round-trip should succeed. Error: " (:error result)))))))