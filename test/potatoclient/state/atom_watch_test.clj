(ns potatoclient.state.atom-watch-test
  "Tests to ensure atom watches don't trigger when data hasn't changed"
  (:require [clojure.test :refer [deftest testing is use-fixtures]]
            [potatoclient.state.dispatch :as dispatch]
            [potatoclient.state.device :as device]
            [potatoclient.state.proto-bridge :as bridge]
            [potatoclient.test-utils.edn :as edn-utils])
  (:import (ser JonSharedData$JonGUIState)))

(defn reset-fixture [f]
  ;; Reset all state before each test
  (device/reset-all-states!)
  (dispatch/dispose!)
  (f)
  ;; Clean up after
  (dispatch/dispose!))

(use-fixtures :each reset-fixture)

(deftest test-atom-watch-no-trigger-on-same-data
  (testing "Atom watches don't trigger when the same data is set"
    (let [watch-calls (atom 0)
          test-atom (atom nil)
          watch-key (gensym "test-watch-")]

      ;; Add watch
      (add-watch test-atom watch-key
                 (fn [_ _ old-val new-val]
                   (when (not= old-val new-val)
                     (swap! watch-calls inc))))

      ;; Set initial value
      (reset! test-atom {:value 1})
      (is (= 1 @watch-calls) "Watch should trigger on initial set")

      ;; Set same value again
      (reset! test-atom {:value 1})
      (is (= 1 @watch-calls) "Watch should NOT trigger when setting same value")

      ;; Set different value
      (reset! test-atom {:value 2})
      (is (= 2 @watch-calls) "Watch should trigger when value changes")

      ;; Clean up
      (remove-watch test-atom watch-key))))

(deftest test-dispatch-atom-watches
  (testing "State dispatch doesn't trigger watches for unchanged subsystems"
    (let [system-watch-calls (atom 0)
          gps-watch-calls (atom 0)
          system-watch-key (gensym "system-watch-")
          gps-watch-key (gensym "gps-watch-")]

      ;; Add watches to subsystem atoms
      (add-watch device/system-state system-watch-key
                 (fn [_ _ old-val new-val]
                   (when (not= old-val new-val)
                     (swap! system-watch-calls inc))))

      (add-watch device/gps-state gps-watch-key
                 (fn [_ _ old-val new-val]
                   (when (not= old-val new-val)
                     (swap! gps-watch-calls inc))))

      ;; Create initial protobuf state
      (let [builder (JonSharedData$JonGUIState/newBuilder)
            system-builder (.getSystemBuilder builder)
            _ (doto system-builder
                (.setCpuTemperature 45.0)
                (.setGpuTemperature 50.0))
            gps-builder (.getGpsBuilder builder)
            _ (doto gps-builder
                (.setLatitude 50.0)
                (.setLongitude 30.0)
                (.setAltitude 100.0))
            proto-msg (.build builder)
            binary (.toByteArray proto-msg)]

        ;; First dispatch
        (dispatch/handle-binary-state binary)
        (Thread/sleep 50)

        (is (= 1 @system-watch-calls) "System watch should trigger once")
        (is (= 1 @gps-watch-calls) "GPS watch should trigger once")

        ;; Dispatch same state again
        (dispatch/handle-binary-state binary)
        (Thread/sleep 50)

        (is (= 1 @system-watch-calls) "System watch should NOT trigger again")
        (is (= 1 @gps-watch-calls) "GPS watch should NOT trigger again")

        ;; Now change only GPS
        (let [builder2 (JonSharedData$JonGUIState/newBuilder)
              system-builder2 (.getSystemBuilder builder2)
              _ (doto system-builder2
                  (.setCpuTemperature 45.0)  ; Same as before
                  (.setGpuTemperature 50.0)) ; Same as before
              gps-builder2 (.getGpsBuilder builder2)
              _ (doto gps-builder2
                  (.setLatitude 51.0)    ; Changed!
                  (.setLongitude 30.0)
                  (.setAltitude 100.0))
              proto-msg2 (.build builder2)
              binary2 (.toByteArray proto-msg2)]

          (dispatch/handle-binary-state binary2)
          (Thread/sleep 50)

          (is (= 1 @system-watch-calls) "System watch should still NOT trigger")
          (is (= 2 @gps-watch-calls) "GPS watch should trigger for change")))

      ;; Clean up
      (remove-watch device/system-state system-watch-key)
      (remove-watch device/gps-state gps-watch-key))))

(deftest test-identical-reference-preservation
  (testing "Unchanged subsystem atoms preserve identical references"
    (let [builder (JonSharedData$JonGUIState/newBuilder)
          system-builder (.getSystemBuilder builder)
          _ (.setCpuTemperature system-builder 45.0)
          proto-msg (.build builder)
          binary (.toByteArray proto-msg)]

      ;; First dispatch
      (dispatch/handle-binary-state binary)
      (Thread/sleep 50)

      (let [system-ref-1 @device/system-state]
        ;; Dispatch same state again
        (dispatch/handle-binary-state binary)
        (Thread/sleep 50)

        (let [system-ref-2 @device/system-state]
          ;; The atom should contain the exact same object reference
          (is (identical? system-ref-1 system-ref-2)
              "Unchanged subsystem should preserve identical reference"))))))

(deftest test-nil-subsystem-handling
  (testing "Nil subsystems don't trigger watches unnecessarily"
    (let [watch-calls (atom 0)
          watch-key (gensym "watch-")]

      ;; Add watch
      (add-watch device/lrf-state watch-key
                 (fn [_ _ old-val new-val]
                   (when (not= old-val new-val)
                     (swap! watch-calls inc))))

      ;; Initial state is nil
      (is (nil? @device/lrf-state))

      ;; Create state without LRF
      (let [builder (JonSharedData$JonGUIState/newBuilder)
            system-builder (.getSystemBuilder builder)
            _ (.setCpuTemperature system-builder 45.0)
            proto-msg (.build builder)
            binary (.toByteArray proto-msg)]

        ;; Dispatch
        (dispatch/handle-binary-state binary)
        (Thread/sleep 50)

        ;; LRF should still be nil and watch shouldn't trigger
        (is (nil? @device/lrf-state))
        (is (zero? @watch-calls) "Watch should not trigger for nil->nil"))

      ;; Clean up
      (remove-watch device/lrf-state watch-key))))

(deftest test-device-watch-state-function
  (testing "device/watch-state function properly filters changes"
    (let [watch-calls (atom [])
          watch-key (device/watch-state :system
                                        (fn [old new]
                                          (swap! watch-calls conj {:old old :new new})))]

      ;; Create and dispatch initial state
      (let [builder (JonSharedData$JonGUIState/newBuilder)
            system-builder (.getSystemBuilder builder)
            _ (.setCpuTemperature system-builder 45.0)
            proto-msg (.build builder)
            binary (.toByteArray proto-msg)]

        (dispatch/handle-binary-state binary)
        (Thread/sleep 50)

        (is (= 1 (count @watch-calls)) "Should have one watch call")

        ;; Dispatch same state
        (dispatch/handle-binary-state binary)
        (Thread/sleep 50)

        (is (= 1 (count @watch-calls)) "Should still have only one watch call")

        ;; Change state
        (let [builder2 (JonSharedData$JonGUIState/newBuilder)
              system-builder2 (.getSystemBuilder builder2)
              _ (.setCpuTemperature system-builder2 50.0)
              proto-msg2 (.build builder2)
              binary2 (.toByteArray proto-msg2)]

          (dispatch/handle-binary-state binary2)
          (Thread/sleep 50)

          (is (= 2 (count @watch-calls)) "Should have two watch calls")
          (is (= 45.0 (get-in (first @watch-calls) [:new :cpu-temperature])))
          (is (= 50.0 (get-in (second @watch-calls) [:new :cpu-temperature])))))

      ;; Clean up
      (device/unwatch-state :system watch-key))))