(ns potatoclient.app-db-state-test
  "Tests for app-db state update integration"
  (:require [clojure.test :refer [deftest testing is use-fixtures]]
            [potatoclient.transit.app-db :as app-db]
            [potatoclient.logging :as logging]))

;; Reset app-db between tests
(use-fixtures :each (fn [f]
                      (app-db/reset-to-initial-state!)
                      (f)))

(deftest test-handle-state-update-gps
  (testing "GPS state updates are properly integrated into app-db"
    ;; Create a state update message as it comes from subprocess
    (let [msg {"msg-type" "state-update"
               "msg-id" "test-123"
               "timestamp" 1234567890
               "payload" {"state" {"gps" {"latitude" 37.7749
                                         "longitude" -122.4194
                                         "altitude" 52.0
                                         "fix-type" "3d"
                                         "use-manual" false}}}}]
      
      ;; Handle the state update
      (app-db/handle-state-update msg)
      
      ;; Verify GPS state was updated
      (let [gps-state (app-db/get-subsystem-state :gps)]
        (is (= 37.7749 (:latitude gps-state)))
        (is (= -122.4194 (:longitude gps-state)))
        (is (= 52.0 (:altitude gps-state)))
        (is (= "3d" (:fix-type gps-state)))
        (is (= false (:use-manual gps-state)))))))

(deftest test-handle-state-update-rotary
  (testing "Rotary platform state updates"
    (let [msg {"msg-type" "state-update"
               "msg-id" "test-456"
               "timestamp" 1234567890
               "payload" {"state" {"rotary" {"azimuth" 45.0
                                            "elevation" -10.0
                                            "azimuth-speed" 2.5
                                            "elevation-speed" 0.0
                                            "is-moving" true
                                            "mode" "auto"}}}}]
      
      (app-db/handle-state-update msg)
      
      (let [rotary-state (app-db/get-subsystem-state :rotary)]
        (is (= 45.0 (:azimuth rotary-state)))
        (is (= -10.0 (:elevation rotary-state)))
        (is (= 2.5 (:azimuth-velocity rotary-state)))
        (is (= 0.0 (:elevation-velocity rotary-state)))
        (is (= true (:moving rotary-state)))
        (is (= :auto (:mode rotary-state)))))))

(deftest test-handle-state-update-cameras
  (testing "Camera state updates"
    (let [msg {"msg-type" "state-update"
               "msg-id" "test-789"
               "timestamp" 1234567890
               "payload" {"state" {"camera-day" {"zoom-pos" 4.5
                                                "auto-focus" true}
                                  "camera-heat" {"zoom-pos" 2.0
                                                "auto-focus" false}}}}]
      
      (app-db/handle-state-update msg)
      
      ;; Check day camera
      (let [day-cam (app-db/get-subsystem-state :camera-day)]
        (is (= 4.5 (:zoom day-cam)))
        (is (= :auto (:focus-mode day-cam))))
      
      ;; Check heat camera
      (let [heat-cam (app-db/get-subsystem-state :camera-heat)]
        (is (= 2.0 (:zoom heat-cam)))
        (is (= :white-hot (:palette heat-cam)))))))

(deftest test-handle-state-update-system
  (testing "System state updates"
    (let [msg {"msg-type" "state-update"
               "msg-id" "test-999"
               "timestamp" 1234567890
               "payload" {"state" {"system" {"rec-enabled" true
                                            "tracking" true
                                            "cpu-temperature" 65.5
                                            "loc" "uk"}}}}]
      
      (app-db/handle-state-update msg)
      
      (let [sys-state (app-db/get-subsystem-state :system)]
        (is (= true (:recording sys-state)))
        (is (= true (:tracking sys-state)))
        (is (= 65.5 (:temperature-c sys-state)))
        (is (= "uk" (:localization sys-state)))))))

(deftest test-handle-state-update-multiple-subsystems
  (testing "Multiple subsystem updates in one message"
    (let [msg {"msg-type" "state-update"
               "msg-id" "test-multi"
               "timestamp" 1234567890
               "payload" {"state" {"gps" {"latitude" 40.7128
                                         "longitude" -74.0060}
                                  "compass" {"azimuth" 180.0
                                            "elevation" 0.0
                                            "bank" 0.0
                                            "calibrating" false}
                                  "lrf" {"target" {"distance-3b" 1234.5}}}}}]
      
      (app-db/handle-state-update msg)
      
      ;; Verify all subsystems were updated
      (let [gps (app-db/get-subsystem-state :gps)
            compass (app-db/get-subsystem-state :compass)
            lrf (app-db/get-subsystem-state :lrf)]
        (is (= 40.7128 (:latitude gps)))
        (is (= -74.0060 (:longitude gps)))
        (is (= 180.0 (:heading compass)))
        (is (= true (:calibrated compass)))
        (is (= 1234.5 (:distance lrf)))
        (is (= true (:target-locked lrf)))))))

(deftest test-handle-state-update-partial
  (testing "Partial state updates don't overwrite unrelated subsystems"
    ;; First set some initial state
    (app-db/update-subsystem! :gps {:latitude 10.0 :longitude 20.0})
    (app-db/update-subsystem! :compass {:heading 90.0})
    
    ;; Now update only GPS
    (let [msg {"msg-type" "state-update"
               "msg-id" "test-partial"
               "timestamp" 1234567890
               "payload" {"state" {"gps" {"latitude" 15.0}}}}]
      
      (app-db/handle-state-update msg)
      
      ;; GPS should be updated
      (is (= 15.0 (get-in @app-db/app-db [:server-state :gps :latitude])))
      ;; But compass should remain unchanged
      (is (= 90.0 (get-in @app-db/app-db [:server-state :compass :heading]))))))

(deftest test-handle-state-update-empty
  (testing "Empty state update doesn't crash"
    (let [msg {"msg-type" "state-update"
               "msg-id" "test-empty"
               "timestamp" 1234567890
               "payload" {"state" {}}}]
      
      ;; Should not throw
      (is (nil? (app-db/handle-state-update msg))))))

(deftest test-handle-state-update-missing-payload
  (testing "Missing payload doesn't crash"
    (let [msg {"msg-type" "state-update"
               "msg-id" "test-missing"
               "timestamp" 1234567890
               "payload" {}}]
      
      ;; Should not throw
      (is (nil? (app-db/handle-state-update msg))))))

;; Test fixture to ensure clean subprocess lifecycle
(defn- state-test-fixture [f]
  (logging/log-info "Starting app-db state tests")
  (f)
  (logging/log-info "Completed app-db state tests"))

(use-fixtures :once state-test-fixture)