(ns potatoclient.state.proto-bridge-test
  "Tests for the protobuf isolation bridge"
  (:require [clojure.test :refer [deftest testing is]]
            [potatoclient.state.proto-bridge :as bridge]
            [potatoclient.state.edn :as edn]
            [potatoclient.proto :as proto])
  (:import (ser JonSharedData$JonGUIState)))

#_(deftest test-binary-edn-roundtrip ;; Skip until EDN->proto is implemented
  (testing "Empty state roundtrip"
    (let [empty-state {:protocol-version 1}
          binary (bridge/edn-state->binary empty-state)]
      (is (bytes? binary))
      (when binary
        (let [result (bridge/binary->edn-state binary)]
          (is (= empty-state result))))))
  
  (testing "System state roundtrip"
    (let [state {:protocol-version 1
                 :system {:cpu-temperature 45.5
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
                          :cur-video-rec-dir-second 0}}
          binary (bridge/edn-state->binary state)]
      (is (bytes? binary))
      (when binary
        (let [result (bridge/binary->edn-state binary)]
          (is (= state result))))))
  
  (testing "Multiple subsystems roundtrip"
    (let [state {:protocol-version 1
                 :system {:cpu-temperature 45.5
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
                 :time {:timestamp 1705337400
                        :manual-timestamp 0
                        :zone-id 0
                        :use-manual-time false}
                 :gps {:longitude 30.5234
                       :latitude 50.4501
                       :altitude 150.0
                       :manual-longitude 0.0
                       :manual-latitude 0.0
                       :manual-altitude 0.0
                       :fix-type "JON_GUI_DATA_GPS_FIX_TYPE_3D"
                       :use-manual false}}
          binary (bridge/edn-state->binary state)]
      (is (bytes? binary))
      (when binary
        (let [result (bridge/binary->edn-state binary)]
          (is (= state result)))))))

(deftest test-proto-msg-to-edn
  (testing "Convert protobuf object to EDN"
    (let [proto-msg (-> (JonSharedData$JonGUIState/newBuilder)
                        (.setProtocolVersion 1)
                        (.build))
          edn-state (bridge/proto-msg->edn-state proto-msg)]
      (is (map? edn-state))
      (is (= 1 (:protocol-version edn-state))))))

(deftest test-extract-subsystem
  (testing "Extract individual subsystems"
    (let [builder (JonSharedData$JonGUIState/newBuilder)
          _ (.setProtocolVersion builder 1)
          system-builder (.getSystemBuilder builder)
          _ (doto system-builder
              (.setCpuTemperature 45.0)
              (.setGpuTemperature 50.0))
          proto-msg (.build builder)]
      
      (is (bridge/has-subsystem? proto-msg :system))
      (is (not (bridge/has-subsystem? proto-msg :gps)))
      
      (let [system-edn (bridge/extract-subsystem-edn proto-msg :system)]
        (is (map? system-edn))
        (is (= 45.0 (:cpu-temperature system-edn)))
        (is (= 50.0 (:gpu-temperature system-edn))))
      
      (is (nil? (bridge/extract-subsystem-edn proto-msg :gps))))))

(deftest test-state-comparison
  (testing "EDN state comparison"
    (let [state1 {:protocol-version 1 :system {:cpu-temperature 45.0}}
          state2 {:protocol-version 1 :system {:cpu-temperature 45.0}}
          state3 {:protocol-version 1 :system {:cpu-temperature 50.0}}]
      
      (is (not (bridge/changed? state1 state2)))
      (is (bridge/changed? state1 state3))
      (is (bridge/changed? state1 nil))
      (is (bridge/changed? nil state1))))
  
  (testing "Subsystem comparison"
    (let [state1 {:protocol-version 1 :system {:cpu-temperature 45.0}}
          state2 {:protocol-version 1 :system {:cpu-temperature 50.0}}]
      
      (is (bridge/subsystem-changed? state1 state2 :system))
      (is (not (bridge/subsystem-changed? state1 state2 :gps))))))

#_(deftest test-parse-gui-state ;; Skip until EDN->proto is implemented
  (testing "Parse binary data to protobuf"
    (let [state {:protocol-version 1}
          binary (bridge/edn-state->binary state)]
      (is (bytes? binary))
      (when binary
        (let [proto-msg (bridge/parse-gui-state binary)]
          (is (instance? JonSharedData$JonGUIState proto-msg))
          (is (= 1 (.getProtocolVersion proto-msg)))))))
  
  (testing "Parse invalid binary data"
    (let [invalid-binary (byte-array [1 2 3 4 5])
          result (bridge/parse-gui-state invalid-binary)]
      (is (nil? result)))))

(deftest test-extract-all-subsystems
  (testing "Extract all subsystems from protobuf"
    (let [builder (JonSharedData$JonGUIState/newBuilder)
          _ (.setProtocolVersion builder 1)
          system-builder (.getSystemBuilder builder)
          _ (doto system-builder
              (.setCpuTemperature 45.0)
              (.setGpuTemperature 50.0))
          time-builder (.getTimeBuilder builder)
          _ (doto time-builder
              (.setTimestamp 1705337400)
              (.setUseManualTime false))
          proto-msg (.build builder)
          
          edn-state (bridge/extract-all-subsystems proto-msg)]
      
      (is (map? edn-state))
      (is (= 1 (:protocol-version edn-state)))
      (is (= 45.0 (get-in edn-state [:system :cpu-temperature])))
      (is (= 1705337400 (get-in edn-state [:time :timestamp])))
      (is (not (contains? edn-state :gps))))))

(deftest test-error-handling
  (testing "Binary conversion with invalid data"
    (is (nil? (bridge/binary->edn-state (byte-array [1 2 3]))))
    (is (nil? (bridge/binary->edn-state (byte-array [])))))
  
  (testing "EDN to binary returns nil (not implemented)"
    ;; EDN->proto conversion is not implemented
    (let [state {:protocol-version 1}]
      ;; Function logs a warning and returns nil
      (is (nil? (bridge/edn-state->binary state))))))

(deftest test-subsystem-keys
  (testing "All subsystem keys are defined"
    (is (= 13 (count bridge/subsystem-keys)))
    (is (contains? (set bridge/subsystem-keys) :system))
    (is (contains? (set bridge/subsystem-keys) :meteo-internal))))