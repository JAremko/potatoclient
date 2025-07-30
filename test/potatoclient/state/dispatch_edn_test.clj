(ns potatoclient.state.dispatch-edn-test
  "Tests for the EDN-based state dispatch system"
  (:require [clojure.test :refer [deftest testing is use-fixtures]]
            [clojure.core.async :as async]
            [potatoclient.state.dispatch :as dispatch]
            [potatoclient.state.device :as device]
            [potatoclient.state.proto-bridge :as bridge]
            [potatoclient.state.proto-test-helper :as proto-helper]
            [potatoclient.test-utils.edn :as edn-utils]))

(defn reset-fixture [f]
  ;; Reset all state before each test
  (device/reset-all-states!)
  (dispatch/dispose!)
  (f)
  ;; Clean up after
  (dispatch/dispose!))

(use-fixtures :each reset-fixture)

(deftest test-edn-state-dispatch
  (testing "Basic state dispatch with EDN"
    (let [test-state (edn-utils/create-test-gui-state :system :time)
          binary-data (proto-helper/edn->binary-for-test test-state)
          state-channel (dispatch/get-state-channel)]

      ;; Handle the binary state
      (dispatch/handle-binary-state binary-data)

      ;; Check that subsystem atoms were updated
      (Thread/sleep 50) ; Give async operations time to complete

      (is (some? (device/get-system)))
      (is (some? (device/get-time)))
      (is (= 45.0 (:cpu-temperature (device/get-system))))
      (is (= 1705337400 (:timestamp (device/get-time))))

      ;; Check that state was sent to channel
      (let [timeout-chan (async/timeout 100)
            [val port] (async/alts!! [state-channel timeout-chan])]
        (is (not= port timeout-chan) "Should receive state on channel")
        (when (not= port timeout-chan)
          (is (map? val))
          (is (= 1 (:protocol-version val)))
          (is (contains? val :system))
          (is (contains? val :time)))))))

(deftest test-change-detection
  (testing "Only changed subsystems are updated"
    (let [;; Initial state
          initial-state (edn-utils/create-test-gui-state :system :gps)
          _ (dispatch/handle-binary-state (proto-helper/edn->binary-for-test initial-state))
          _ (Thread/sleep 50)

          ;; Capture initial values
          initial-system (device/get-system)
          initial-gps (device/get-gps)

          ;; Update only GPS
          updated-state (edn-utils/update-subsystem initial-state :gps {:latitude 51.0})
          _ (dispatch/handle-binary-state (proto-helper/edn->binary-for-test updated-state))
          _ (Thread/sleep 50)]

      ;; System should be unchanged (same reference)
      (is (identical? initial-system (device/get-system)))

      ;; GPS should be updated
      (is (not (identical? initial-gps (device/get-gps))))
      (is (= 51.0 (:latitude (device/get-gps))))
      (is (= 30.5234 (:longitude (device/get-gps)))))))

(deftest test-subsystem-removal
  (testing "Subsystems can be removed from state"
    (let [;; Initial state with multiple subsystems
          initial-state (edn-utils/create-test-gui-state :system :gps :compass)
          _ (dispatch/handle-binary-state (proto-helper/edn->binary-for-test initial-state))
          _ (Thread/sleep 50)

          ;; Verify all subsystems present
          _ (is (some? (device/get-system)))
          _ (is (some? (device/get-gps)))
          _ (is (some? (device/get-compass)))

          ;; Remove GPS subsystem
          updated-state (-> initial-state
                            (dissoc :gps)
                            (assoc :system (edn-utils/create-test-system-state :cpu-temperature 60.0)))
          _ (dispatch/handle-binary-state (proto-helper/edn->binary-for-test updated-state))
          _ (Thread/sleep 50)]

      ;; System should be updated
      (is (= 60.0 (:cpu-temperature (device/get-system))))

      ;; GPS should still have its last value (not cleared)
      (is (some? (device/get-gps)))

      ;; Compass should be unchanged
      (is (some? (device/get-compass))))))

(deftest test-validation
  (testing "Invalid subsystem data is rejected"
    (dispatch/enable-validation! true)
    (let [;; Create state with invalid temperature (outside valid range)
          invalid-state {:protocol-version 1
                         :system {:cpu-temperature -500.0 ; Below absolute zero
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
          binary-data (proto-helper/edn->binary-for-test invalid-state)]

      (dispatch/handle-binary-state binary-data)
      (Thread/sleep 50)

      ;; System state should not be updated due to validation failure
      (is (nil? (device/get-system))))

    (dispatch/enable-validation! false)))

(deftest test-concurrent-updates
  (testing "Concurrent state updates are handled correctly"
    (let [num-updates 20
          update-futures (doall
                           (for [i (range num-updates)]
                             (future
                               (let [state (edn-utils/create-test-gui-state
                                             {:system (edn-utils/create-test-system-state
                                                        :cpu-temperature (double i))})]
                                 (dispatch/handle-binary-state
                                   (proto-helper/edn->binary-for-test state))))))]

      ;; Wait for all updates to complete
      (doseq [f update-futures]
        @f)

      (Thread/sleep 100)

      ;; Should have some final state
      (is (some? (device/get-system)))
      (is (number? (:cpu-temperature (device/get-system)))))))

(deftest test-debug-mode
  (testing "Debug mode logs state changes"
    (dispatch/enable-debug! true)

    (let [test-state (edn-utils/create-test-gui-state :system)]
      (dispatch/handle-binary-state (proto-helper/edn->binary-for-test test-state))
      (Thread/sleep 50)

      ;; Just verify it doesn't throw
      (is (some? (device/get-system))))

    (dispatch/enable-debug! false)))