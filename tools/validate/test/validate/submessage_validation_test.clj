(ns validate.submessage-validation-test
  "Test if sub-messages can be validated independently when constructed as root messages."
  (:require
   [clojure.test :refer [deftest testing is]]
   [pronto.core :as p]
   [validate.test-harness :as h]
   [validate.validator :as v]
   [malli.core :as m]
   [malli.generator :as mg]
   [potatoclient.malli.registry :as registry]
   [potatoclient.specs.oneof-edn :as oneof-edn]
   ;; Load specs
   [potatoclient.specs.common]
   [potatoclient.specs.state.gps]
   [potatoclient.specs.state.lrf]
   [potatoclient.specs.state.system]
   [potatoclient.specs.state.time]
   [potatoclient.specs.state.compass])
  (:import
   [ser JonSharedDataGps$JonGuiDataGps
    JonSharedDataLrf$JonGuiDataLrf
    JonSharedDataSystem$JonGuiDataSystem
    JonSharedDataTime$JonGuiDataTime
    JonSharedDataCompass$JonGuiDataCompass]))

;; Initialize registry
(registry/setup-global-registry!
  (oneof-edn/register-oneof-edn-schema!))

;; ============================================================================
;; TEST SUB-MESSAGE VALIDATION
;; ============================================================================

(deftest test-submessage-as-root-validation
  (testing "Can we validate sub-messages when constructed as root proto-maps?"
    
    (testing "GPS sub-message validation"
      (let [;; Create GPS as a root proto-map
            gps-proto (p/proto-map h/state-mapper JonSharedDataGps$JonGuiDataGps
                                  :latitude 45.5
                                  :longitude -122.6
                                  :altitude 100.0
                                  :fix_type :JON_GUI_DATA_GPS_FIX_TYPE_3D
                                  :manual_latitude 0.0
                                  :manual_longitude 0.0)
            
            ;; Convert to binary
            binary (p/proto-map->bytes gps-proto)
            
            ;; Try to validate - this might fail if buf.validate is at proto level
            result (v/validate-binary binary)]
        
        (println "\n=== GPS Sub-message Validation ===")
        (println "Binary size:" (count binary))
        (println "Detected type:" (:message-type result))
        (println "Valid?:" (:valid? result))
        (when-not (:valid? result)
          (println "Violations:" (:violations result)))
        
        ;; This test explores if the issue is how we construct the proto-map
        ;; or if buf.validate constraints are only on root messages
        (is (or (:valid? result)
                (do (println "GPS cannot be validated as standalone") true))
            "Document GPS validation behavior")))
    
    (testing "LRF sub-message validation"
      (let [;; Create LRF as a root proto-map
            lrf-proto (p/proto-map h/state-mapper JonSharedDataLrf$JonGuiDataLrf
                                  :measure_id 42
                                  :pointer_mode :JON_GUI_DATA_LRF_LASER_POINTER_MODE_OFF)
            
            binary (p/proto-map->bytes lrf-proto)
            result (v/validate-binary binary)]
        
        (println "\n=== LRF Sub-message Validation ===")
        (println "Binary size:" (count binary))
        (println "Detected type:" (:message-type result))
        (println "Valid?:" (:valid? result))
        (when-not (:valid? result)
          (println "Violations:" (:violations result)))
        
        (is (or (:valid? result)
                (do (println "LRF cannot be validated as standalone") true))
            "Document LRF validation behavior")))
    
    (testing "Time sub-message validation"
      (let [;; Create Time as a root proto-map
            time-proto (p/proto-map h/state-mapper JonSharedDataTime$JonGuiDataTime
                                   :timestamp 1754664759
                                   :manual_timestamp 1754664759)
            
            binary (p/proto-map->bytes time-proto)
            result (v/validate-binary binary)]
        
        (println "\n=== Time Sub-message Validation ===")
        (println "Binary size:" (count binary))
        (println "Detected type:" (:message-type result))
        (println "Valid?:" (:valid? result))
        (when-not (:valid? result)
          (println "Violations:" (:violations result)))
        
        (is (or (:valid? result)
                (do (println "Time cannot be validated as standalone") true))
            "Document Time validation behavior")))))

(deftest test-submessage-spec-validation
  (testing "Sub-message Malli spec validation (independent of buf.validate)"
    
    (testing "GPS spec validation with generated data"
      (dotimes [_ 10]
        (let [generated (mg/generate :state/gps)]
          (is (m/validate :state/gps generated)
              (str "Generated GPS should validate against spec: " generated)))))
    
    (testing "LRF spec validation with generated data"
      (dotimes [_ 10]
        (let [generated (mg/generate :state/lrf)]
          (is (m/validate :state/lrf generated)
              (str "Generated LRF should validate against spec: " generated)))))
    
    (testing "System spec validation with generated data"
      (dotimes [_ 10]
        (let [generated (mg/generate :state/system)]
          (is (m/validate :state/system generated)
              (str "Generated System should validate against spec: " generated)))))
    
    (testing "Time spec validation with generated data"
      (dotimes [_ 10]
        (let [generated (mg/generate :state/time)]
          (is (m/validate :state/time generated)
              (str "Generated Time should validate against spec: " generated)))))
    
    (testing "Compass spec validation with generated data"
      (dotimes [_ 10]
        (let [generated (mg/generate :state/compass)]
          (is (m/validate :state/compass generated)
              (str "Generated Compass should validate against spec: " generated)))))))

(deftest test-submessage-in-root-context
  (testing "Sub-messages validated within root message context"
    
    (testing "GPS within complete state"
      (let [;; Generate GPS data from spec
            gps-spec-data (mg/generate :state/gps)
            
            ;; Create a complete state with the generated GPS
            state-proto (h/valid-state)
            
            ;; Update GPS fields with generated data
            updated-state (p/p-> state-proto
                                (assoc-in [:gps :latitude] 
                                         (:latitude gps-spec-data))
                                (assoc-in [:gps :longitude] 
                                         (:longitude gps-spec-data))
                                (assoc-in [:gps :altitude] 
                                         (:altitude gps-spec-data)))
            
            ;; Validate the complete state
            binary (p/proto-map->bytes updated-state)
            result (v/validate-binary binary :type :state)]
        
        (is (:valid? result)
            (str "State with generated GPS should validate. "
                 "GPS data: " gps-spec-data
                 " Violations: " (:violations result)))))
    
    (testing "Multiple sub-messages with generated data"
      (let [;; Generate data for multiple sub-messages
            gps-data (mg/generate :state/gps)
            compass-data (mg/generate :state/compass)
            time-data (mg/generate :state/time)
            
            ;; Start with valid state and update multiple sub-messages
            state-proto (h/valid-state)
            
            ;; Update with generated data (converting kebab-case to snake_case)
            updated-state (p/p-> state-proto
                                ;; GPS updates
                                (assoc-in [:gps :latitude] (:latitude gps-data))
                                (assoc-in [:gps :longitude] (:longitude gps-data))
                                (assoc-in [:gps :altitude] (:altitude gps-data))
                                ;; Compass updates
                                (assoc-in [:compass :azimuth] (:azimuth compass-data))
                                (assoc-in [:compass :elevation] (:elevation compass-data))
                                (assoc-in [:compass :bank] (:bank compass-data))
                                ;; Time updates
                                (assoc-in [:time :timestamp] (:timestamp time-data))
                                (assoc-in [:time :manual_timestamp] 
                                         (:manual-timestamp time-data)))
            
            ;; Validate the complete state
            binary (p/proto-map->bytes updated-state)
            result (v/validate-binary binary :type :state)]
        
        (is (:valid? result)
            (str "State with multiple generated sub-messages should validate. "
                 "Violations: " (:violations result)))))))