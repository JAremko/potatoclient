(ns potatoclient.specs.cmd.root-test
  "Hardcoded tests for cmd.Root spec validation.
   Tests specific edge cases and negative scenarios."
  (:require
   [clojure.test :refer [deftest is testing]]
   [malli.core :as m]
   [malli.generator :as mg]
   [potatoclient.malli.registry :as registry]))

;; Initialize registry with all specs
(registry/setup-global-registry!)

(deftest cmd-root-negative-tests
  (testing "Hand-crafted invalid cmd messages"
    (let [cmd-spec (m/schema :cmd/root)
          ;; Generate a valid base sample
          base-sample (mg/generate cmd-spec)]
      
      (testing "Invalid protocol_version (0 - must be > 0)"
        (let [invalid-sample (assoc base-sample :protocol_version 0)]
          (is (not (m/validate cmd-spec invalid-sample))
              "Message with protocol_version=0 should fail Malli validation")))
      
      (testing "Invalid GPS coordinates in GPS command"
        (when (:gps base-sample)
          (let [invalid-sample (assoc base-sample
                                      :gps {:set_manual_position
                                            {:latitude 91.0     ; Invalid: > 90
                                             :longitude 181.0   ; Invalid: > 180  
                                             :altitude -500.0}})] ; Invalid: < -430
            (is (not (m/validate cmd-spec invalid-sample))
                "Message with invalid GPS coordinates should fail"))))
      
      (testing "Invalid compass angles"
        (when (:compass base-sample)
          (let [invalid-sample (assoc base-sample
                                      :compass {:set_offset_angle_azimuth
                                               {:value 180.0}})]  ; Invalid: must be < 180
            (is (not (m/validate cmd-spec invalid-sample))
                "Message with invalid compass offset should fail"))))
      
      (testing "Invalid rotary relative angles"
        (when (:rotary base-sample)
          (let [invalid-sample (assoc base-sample
                                      :rotary {:axis {:azimuth {:relative
                                                                {:value 181.0  ; Invalid: > 180
                                                                 :speed 0.5
                                                                 :direction :JON_GUI_DATA_ROTARY_DIRECTION_CLOCKWISE}}
                                                     :elevation {:relative
                                                                {:value 91.0  ; Invalid: > 90
                                                                 :speed 0.5
                                                                 :direction :JON_GUI_DATA_ROTARY_DIRECTION_CLOCKWISE}}}})]
            (is (not (m/validate cmd-spec invalid-sample))
                "Message with invalid rotary relative angles should fail"))))
      
      (testing "Multiple oneof fields present (invalid)"
        (let [invalid-sample (-> base-sample
                                 (assoc :ping {})
                                 (assoc :noop {}))]
          (is (not (m/validate cmd-spec invalid-sample))
              "Message with multiple oneof fields should fail")))
      
      (testing "No oneof fields present (invalid)"  
        (let [invalid-sample (-> base-sample
                                 (dissoc :ping :noop :system :gps :compass :lrf 
                                        :lrf_calib :rotary :osd :cv :lira :frozen
                                        :day_camera :heat_camera :day_cam_glass_heater))]
          (is (not (m/validate cmd-spec invalid-sample))
              "Message with no oneof fields should fail"))))))

(deftest cmd-root-sanity-checks
  (testing "Sanity checks for valid cmd messages"
    (let [cmd-spec (m/schema :cmd/root)]
      
      (testing "Valid ping command"
        (let [valid-sample {:protocol_version 1
                            :session_id 123
                            :important false
                            :from_cv_subsystem false
                            :client_type :JON_GUI_DATA_CLIENT_TYPE_LOCAL_NETWORK
                            :ping {}}]
          (is (m/validate cmd-spec valid-sample)
              "Valid ping command should pass")))
      
      (testing "Valid system command"
        (let [valid-sample {:protocol_version 1
                            :session_id 456
                            :important true
                            :from_cv_subsystem false
                            :client_type :JON_GUI_DATA_CLIENT_TYPE_INTERNAL_CV
                            :system {:localization {:loc :JON_GUI_DATA_SYSTEM_LOCALIZATION_EN}}}]
          (is (m/validate cmd-spec valid-sample)
              "Valid system command should pass")))))
  
  (testing "Edge cases"
    (let [cmd-spec (m/schema :cmd/root)]
      
      (testing "Maximum valid protocol_version"
        (let [valid-sample {:protocol_version 2147483647
                            :session_id 0
                            :important false
                            :from_cv_subsystem false
                            :client_type :JON_GUI_DATA_CLIENT_TYPE_LIRA
                            :noop {}}]
          (is (m/validate cmd-spec valid-sample)
              "Max protocol_version should be valid")))
      
      (testing "Minimum valid session_id (0 is valid)"
        (let [valid-sample {:protocol_version 1
                            :session_id 0
                            :important false
                            :from_cv_subsystem false
                            :client_type :JON_GUI_DATA_CLIENT_TYPE_LIRA
                            :frozen {}}]
          (is (m/validate cmd-spec valid-sample)
              "session_id=0 should be valid"))))))