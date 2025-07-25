(ns potatoclient.state-integration-test
  "Integration test for state dispatch with real protobuf messages"
  (:require [clojure.test :refer [deftest testing is use-fixtures]]
            [potatoclient.proto :as proto]
            [potatoclient.state.dispatch :as dispatch]
            [potatoclient.state.device :as device])
  (:import [ser JonSharedData$JonGUIState
            JonSharedDataSystem$JonGuiDataSystem
            JonSharedDataGps$JonGuiDataGps
            JonSharedDataTypes$JonGuiDataGpsFixType]))

(defn reset-state-fixture
  "Reset all state before and after tests"
  [f]
  (device/reset-all-states!)
  (dispatch/enable-validation! false)
  (dispatch/enable-debug! false)
  (f)
  (device/reset-all-states!)
  (dispatch/dispose!))

(use-fixtures :each reset-state-fixture)

(deftest test-state-dispatch-integration
  (testing "State dispatch with real protobuf messages"
    ;; Create a protobuf state message with some data
    (let [state-builder (JonSharedData$JonGUIState/newBuilder)
          system-builder (JonSharedDataSystem$JonGuiDataSystem/newBuilder)
          gps-builder (JonSharedDataGps$JonGuiDataGps/newBuilder)]

      ;; Build system data
      (.setCpuTemperature system-builder 45.0)
      (.setGpuTemperature system-builder 50.0)
      (.setCpuLoad system-builder 25.0)

      ;; Build GPS data
      (.setLatitude gps-builder 45.5)
      (.setLongitude gps-builder -73.6)
      (.setAltitude gps-builder 100.0)
      (.setFixType gps-builder JonSharedDataTypes$JonGuiDataGpsFixType/JON_GUI_DATA_GPS_FIX_TYPE_3D)

      ;; Set subsystems in main state
      (.setProtocolVersion state-builder 1)
      (.setSystem state-builder system-builder)
      (.setGps state-builder gps-builder)

      ;; Serialize and dispatch
      (let [proto-bytes (.toByteArray (.build state-builder))]
        (dispatch/handle-binary-state proto-bytes)

        ;; Verify state was updated
        (is (= 45.0 (:cpu-temperature @device/system-state)))
        (is (= 50.0 (:gpu-temperature @device/system-state)))
        (is (= 25.0 (:cpu-load @device/system-state)))

        (is (= 45.5 (:latitude @device/gps-state)))
        (is (= -73.6 (:longitude @device/gps-state)))
        (is (= 100.0 (:altitude @device/gps-state)))
        (is (= "JON_GUI_DATA_GPS_FIX_TYPE_3D" (:fix-type @device/gps-state)))))))

(deftest test-state-change-detection
  (testing "State is only updated when data changes"
    ;; Create initial state
    (let [state-builder (JonSharedData$JonGUIState/newBuilder)
          system-builder (JonSharedDataSystem$JonGuiDataSystem/newBuilder)]

      (.setCpuTemperature system-builder 50.0)
      (.setProtocolVersion state-builder 1)
      (.setSystem state-builder system-builder)

      (let [proto-bytes (.toByteArray (.build state-builder))]
        ;; First dispatch
        (dispatch/handle-binary-state proto-bytes)
        (is (= 50.0 (:cpu-temperature @device/system-state)))

        ;; Store the atom reference
        (let [system-atom-before @device/system-state]
          ;; Dispatch same data again
          (dispatch/handle-binary-state proto-bytes)
          ;; Verify atom wasn't changed (same reference)
          (is (identical? system-atom-before @device/system-state)
              "Atom should not be updated when data is unchanged"))

        ;; Now change the data
        (let [new-builder (JonSharedData$JonGUIState/newBuilder)
              new-system (JonSharedDataSystem$JonGuiDataSystem/newBuilder)]
          (.setCpuTemperature new-system 75.0) ; Changed value
          (.setProtocolVersion new-builder 1)
          (.setSystem new-builder new-system)

          (let [new-bytes (.toByteArray (.build new-builder))]
            (dispatch/handle-binary-state new-bytes)
            (is (= 75.0 (:cpu-temperature @device/system-state))
                "State should update when data changes")))))))