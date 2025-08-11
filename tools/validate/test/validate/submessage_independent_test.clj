(ns validate.submessage-independent-test
  "Direct test of sub-message validation using the test_validator.
   This demonstrates that sub-messages CAN be validated independently
   when using the correct validation approach."
  (:require
   [clojure.test :refer [deftest testing is]]
   [validate.test-validator :as tv]
   [validate.test-harness :as h]
   [pronto.core :as p]
   [validate.validator :as v])
  (:import 
   [ser JonSharedDataGps$JonGuiDataGps
    JonSharedDataSystem$JonGuiDataSystem
    JonSharedDataTime$JonGuiDataTime
    JonSharedDataCompass$JonGuiDataCompass]))

;; ============================================================================
;; DIRECT SUB-MESSAGE VALIDATION
;; ============================================================================

(deftest test-submessage-direct-validation
  (testing "Direct validation of sub-messages using test_validator"
    
    (testing "GPS sub-message direct validation"
      (let [;; Create GPS proto-map with valid data
            gps-proto (p/proto-map h/state-mapper JonSharedDataGps$JonGuiDataGps
                                  :latitude 45.5
                                  :longitude -122.6
                                  :altitude 100.0
                                  :fix_type :JON_GUI_DATA_GPS_FIX_TYPE_3D
                                  :manual_latitude 0.0
                                  :manual_longitude 0.0)
            
            ;; Validate directly using test validator
            direct-result (tv/validate-proto-map gps-proto)
            
            ;; Also try with binary (will fail with production validator)
            binary (p/proto-map->bytes gps-proto)
            binary-result (v/validate-binary binary)]
        
        (println "\n=== GPS Sub-message Validation ===")
        (println "Proto-map class:" (-> gps-proto p/proto-map->proto class .getName))
        (println "Binary size:" (count binary) "bytes")
        (println "\nDirect validation (test_validator):")
        (println "  Valid?:" (:valid? direct-result))
        (println "  Violations:" (:violations direct-result))
        (println "\nBinary validation (production validator):")
        (println "  Detected as:" (:message-type binary-result))
        (println "  Valid?:" (:valid? binary-result))
        (println "  Violations:" (take 3 (:violations binary-result)))
        
        (is (:valid? direct-result)
            "GPS should validate when using test_validator directly")
        
        (is (not (:valid? binary-result))
            "GPS binary should NOT validate with production validator (expects root)")))
    
    (testing "System sub-message direct validation"
      (let [sys-proto (p/proto-map h/state-mapper JonSharedDataSystem$JonGuiDataSystem
                                   :cpu_load 50.0
                                   :cpu_temperature 60.0
                                   :gpu_load 30.0
                                   :gpu_temperature 55.0
                                   :disk_space 75
                                   :power_consumption 100.0
                                   :rec_enabled false
                                   :low_disk_space false
                                   :cur_video_rec_dir_year 2025
                                   :cur_video_rec_dir_month 1
                                   :cur_video_rec_dir_day 11
                                   :cur_video_rec_dir_hour 12
                                   :cur_video_rec_dir_minute 30
                                   :cur_video_rec_dir_second 45
                                   :loc :JON_GUI_DATA_SYSTEM_LOCALIZATION_EN)
            
            direct-result (tv/validate-proto-map sys-proto)]
        
        (is (:valid? direct-result)
            "System should validate when using test_validator directly")))
    
    (testing "Time sub-message direct validation"
      (let [time-proto (p/proto-map h/state-mapper JonSharedDataTime$JonGuiDataTime
                                    :timestamp 1754664759
                                    :manual_timestamp 1754664759)
            
            direct-result (tv/validate-proto-map time-proto)]
        
        (is (:valid? direct-result)
            "Time should validate when using test_validator directly")))
    
    (testing "Compass sub-message direct validation"  
      (let [compass-proto (p/proto-map h/state-mapper JonSharedDataCompass$JonGuiDataCompass
                                       :azimuth 180.0
                                       :elevation 45.0
                                       :bank 10.0)
            
            direct-result (tv/validate-proto-map compass-proto)]
        
        (is (:valid? direct-result)
            "Compass should validate when using test_validator directly")))))

;; ============================================================================
;; VALIDATE BINARY AS SPECIFIC TYPE
;; ============================================================================

(deftest test-validate-binary-as-type
  (testing "Validate binary data as specific message type"
    
    (testing "GPS binary parsed as GPS type"
      (let [gps-proto (p/proto-map h/state-mapper JonSharedDataGps$JonGuiDataGps
                                   :latitude 0.0
                                   :longitude 0.0
                                   :altitude 0.0
                                   :fix_type :JON_GUI_DATA_GPS_FIX_TYPE_3D
                                   :manual_latitude 0.0
                                   :manual_longitude 0.0)
            binary (p/proto-map->bytes gps-proto)
            result (tv/validate-binary-as binary JonSharedDataGps$JonGuiDataGps)]
        
        (println "\n=== GPS Binary as GPS Type ===")
        (println "Message class:" (:message-class result))
        (println "Valid?:" (:valid? result))
        (println "Violations:" (:violations result))
        
        (is (:valid? result)
            "GPS binary should validate when parsed as GPS type")))
    
    (testing "System binary parsed as System type"
      (let [sys-proto (p/proto-map h/state-mapper JonSharedDataSystem$JonGuiDataSystem
                                   :cpu_load 0.0
                                   :cpu_temperature 0.0
                                   :gpu_load 0.0
                                   :gpu_temperature 0.0
                                   :disk_space 0
                                   :power_consumption 0.0
                                   :rec_enabled false
                                   :low_disk_space false
                                   :cur_video_rec_dir_year 2025
                                   :cur_video_rec_dir_month 1
                                   :cur_video_rec_dir_day 1
                                   :cur_video_rec_dir_hour 0
                                   :cur_video_rec_dir_minute 0
                                   :cur_video_rec_dir_second 0
                                   :loc :JON_GUI_DATA_SYSTEM_LOCALIZATION_EN)
            binary (p/proto-map->bytes sys-proto)
            result (tv/validate-binary-as binary JonSharedDataSystem$JonGuiDataSystem)]
        
        (is (:valid? result)
            "System binary should validate when parsed as System type")))))

;; ============================================================================
;; HIERARCHY VALIDATION
;; ============================================================================

(deftest test-hierarchy-validation
  (testing "Validate message hierarchy"
    
    (testing "Valid state hierarchy"
      (let [state (h/valid-state)
            hierarchy (tv/validate-hierarchy state [:root])]
        
        (println "\n=== State Hierarchy Validation ===")
        (println "Root valid?:" (:valid? hierarchy))
        (println "Sub-messages:" (keys (:sub-messages hierarchy)))
        (println "All valid?:" (tv/all-valid? hierarchy))
        
        (is (tv/all-valid? hierarchy)
            "Complete state hierarchy should validate")))
    
    (testing "Bottom-up validation"
      (let [state (h/valid-state)
            report (tv/test-bottom-up state)]
        
        (println "\n=== Bottom-Up Test Report ===")
        (println "Summary:" (:summary report))
        (println "All valid?:" (:all-valid? report))
        (doseq [level (:test-order report)]
          (println "  Level" (:level level) "-" 
                   (count (:tests level)) "messages,"
                   (count (filter :valid? (:tests level))) "valid"))
        
        (is (:all-valid? report)
            "Bottom-up validation should pass for valid state")))))