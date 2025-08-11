(ns validate.debug-test
  "Debug test to understand validation failures"
  (:require
   [clojure.test :refer [deftest testing is]]
   [pronto.core :as p]
   [validate.test-harness :as h]
   [validate.validator :as v]
   [clojure.pprint :as pp]))

(deftest test-debug-gps-validation
  (testing "Debug GPS validation"
    (let [;; Create a simple GPS proto-map
          proto-map (p/proto-map h/state-mapper ser.JonSharedDataGps$JonGuiDataGps
                                :latitude 45.5
                                :longitude -122.6
                                :altitude 100.0
                                :fix_type :JON_GUI_DATA_GPS_FIX_TYPE_3D
                                :manual_latitude 0.0
                                :manual_longitude 0.0)
          
          ;; Convert to bytes
          binary (p/proto-map->bytes proto-map)
          
          ;; Validate
          result (v/validate-binary binary)]
      
      (println "\n=== GPS Validation Debug ===")
      (println "Proto-map fields:" (keys proto-map))
      (println "Binary size:" (count binary))
      (println "Valid?:" (:valid? result))
      (println "Message type detected:" (:message-type result))
      (println "Violations:" (:violations result))
      
      (is (:valid? result) 
          (str "GPS should validate. Violations: " (:violations result))))))

(deftest test-debug-state-detection
  (testing "Debug message type detection"
    (let [;; Create state and command messages
          gps-proto (p/proto-map h/state-mapper ser.JonSharedDataGps$JonGuiDataGps
                                :latitude 45.5
                                :longitude -122.6
                                :altitude 100.0
                                :fix_type :JON_GUI_DATA_GPS_FIX_TYPE_3D
                                :manual_latitude 0.0
                                :manual_longitude 0.0)
          
          ping-proto (p/proto-map h/cmd-mapper cmd.JonSharedCmd$Root
                                 :protocol_version 1
                                 :client_type :JON_GUI_DATA_CLIENT_TYPE_LOCAL_NETWORK
                                 :ping (p/proto-map h/cmd-mapper cmd.JonSharedCmd$Ping))
          
          gps-binary (p/proto-map->bytes gps-proto)
          ping-binary (p/proto-map->bytes ping-proto)]
      
      (println "\n=== Type Detection Debug ===")
      
      ;; Test GPS
      (let [result (v/validate-binary gps-binary)]
        (println "GPS - Detected type:" (:message-type result))
        (println "GPS - Valid?:" (:valid? result))
        (when-not (:valid? result)
          (println "GPS - Violations:" (:violations result))))
      
      ;; Test Ping
      (let [result (v/validate-binary ping-binary :type :cmd)]
        (println "Ping - Detected type:" (:message-type result))
        (println "Ping - Valid?:" (:valid? result))
        (when-not (:valid? result)
          (println "Ping - Violations:" (:violations result)))))))