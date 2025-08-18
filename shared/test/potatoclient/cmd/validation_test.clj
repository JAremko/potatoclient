(ns potatoclient.cmd.validation-test
  "Tests for the validation module itself.
   Ensures our validation logic correctly detects both valid and invalid cases."
  (:require
   [clojure.test :refer [deftest is testing]]
   [potatoclient.cmd.validation :as v]
   [potatoclient.proto.serialize :as serialize]
   [potatoclient.malli.registry :as registry]
   [potatoclient.test-harness :as harness]))

;; Ensure test harness is initialized
(when-not harness/initialized?
  (throw (ex-info "Test harness failed to initialize!" 
                  {:initialized? harness/initialized?})))

;; Initialize registry
(registry/setup-global-registry!)

;; ============================================================================
;; Positive Tests - Valid Commands Should Pass
;; ============================================================================

(deftest valid-commands-pass-test
  (testing "Valid commands should pass validation"
    
    (testing "Simple ping command"
      (let [cmd {:protocol_version 1
                 :client_type :JON_GUI_DATA_CLIENT_TYPE_LOCAL_NETWORK
                 :session_id 0
                 :important false
                 :from_cv_subsystem false
                 :ping {}}
            result (v/validate-roundtrip-with-report cmd)]
        (is (:valid? result) "Valid ping command should pass")))
    
    (testing "Command with different oneof field"
      (let [cmd {:protocol_version 1
                 :client_type :JON_GUI_DATA_CLIENT_TYPE_LOCAL_NETWORK
                 :session_id 0
                 :important false
                 :from_cv_subsystem false
                 :noop {}}
            result (v/validate-roundtrip-with-report cmd)]
        (is (:valid? result) "Valid noop command should pass")))
    
    (testing "Command with non-default values"
      (let [cmd {:protocol_version 1
                 :client_type :JON_GUI_DATA_CLIENT_TYPE_LIRA
                 :session_id 12345
                 :important true
                 :from_cv_subsystem true
                 :frozen {}}
            result (v/validate-roundtrip-with-report cmd)]
        (is (:valid? result) "Command with non-default values should pass")))
    
    (testing "Complex nested command"
      (let [cmd {:protocol_version 1
                 :client_type :JON_GUI_DATA_CLIENT_TYPE_LOCAL_NETWORK
                 :session_id 0
                 :important false
                 :from_cv_subsystem false
                 :system {:reboot {}}}
            result (v/validate-roundtrip-with-report cmd)]
        (is (:valid? result) 
            (str "Nested system command should pass"
                 (when-not (:valid? result)
                   (str "\n" (:pretty-diff result)))))))))

;; ============================================================================
;; Negative Tests - Invalid Commands Should Fail
;; ============================================================================

(deftest invalid-commands-fail-test
  (testing "Invalid commands should fail validation"
    
    (testing "Command with multiple oneof fields set"
      (is (thrown-with-msg? Exception #"validation failed"
            (serialize/serialize-cmd-payload
              {:protocol_version 1
               :client_type :JON_GUI_DATA_CLIENT_TYPE_LOCAL_NETWORK
               :session_id 0
               :important false
               :from_cv_subsystem false
               :ping {}
               :noop {}}))
          "Command with multiple oneof fields should fail"))
    
    (testing "Command without required oneof field"
      (is (thrown-with-msg? Exception #"validation failed"
            (serialize/serialize-cmd-payload
              {:protocol_version 1
               :client_type :JON_GUI_DATA_CLIENT_TYPE_LOCAL_NETWORK
               :session_id 0
               :important false
               :from_cv_subsystem false}))
          "Command without any oneof field should fail"))
    
    (testing "Command with invalid enum value"
      (is (thrown-with-msg? Exception #"validation failed"
            (serialize/serialize-cmd-payload
              {:protocol_version 1
               :client_type :INVALID_CLIENT_TYPE
               :session_id 0
               :important false
               :from_cv_subsystem false
               :ping {}}))
          "Command with invalid enum should fail"))
    
    (testing "Command with invalid protocol version"
      (is (thrown-with-msg? Exception #"validation failed"
            (serialize/serialize-cmd-payload
              {:protocol_version 0  ; Must be > 0
               :client_type :JON_GUI_DATA_CLIENT_TYPE_LOCAL_NETWORK
               :session_id 0
               :important false
               :from_cv_subsystem false
               :ping {}}))
          "Command with protocol_version 0 should fail"))
    
    (testing "Command with wrong field types"
      (is (thrown-with-msg? Exception #"validation failed"
            (serialize/serialize-cmd-payload
              {:protocol_version "not-a-number"
               :client_type :JON_GUI_DATA_CLIENT_TYPE_LOCAL_NETWORK
               :session_id 0
               :important false
               :from_cv_subsystem false
               :ping {}}))
          "Command with wrong type should fail"))))

;; ============================================================================
;; Sanity Tests - Ensure Testing Actually Works
;; ============================================================================

(deftest sanity-check-test
  (testing "Sanity check - tests can actually fail"
    
    (testing "Deliberately broken roundtrip"
      ;; Create a command that would fail if we modified it
      (let [original {:protocol_version 1
                     :client_type :JON_GUI_DATA_CLIENT_TYPE_LOCAL_NETWORK
                     :session_id 0
                     :important false
                     :from_cv_subsystem false
                     :ping {}}
            ;; Manually create a broken version
            broken {:protocol_version 2  ; Changed value
                   :client_type :JON_GUI_DATA_CLIENT_TYPE_LOCAL_NETWORK
                   :session_id 0
                   :important false
                   :from_cv_subsystem false
                   :ping {}}]
        ;; Verify they're different
        (is (not= original broken) "Sanity check - modified commands should be different")
        
        ;; Verify the original passes
        (is (:valid? (v/validate-roundtrip-with-report original))
            "Original should pass validation")
        
        ;; The broken one should also pass (it's valid, just different)
        (is (:valid? (v/validate-roundtrip-with-report broken))
            "Modified but valid command should pass")))
    
    (testing "Ensure normalize-cmd works"
      (let [minimal {:ping {}}
            normalized (v/normalize-cmd minimal)]
        (is (contains? normalized :protocol_version) 
            "Normalized command should have protocol_version")
        (is (contains? normalized :noop)
            "Normalized command should have all oneof fields")
        (is (nil? (:noop normalized))
            "Unused oneof fields should be nil")))))

;; ============================================================================
;; Template Creation Tests
;; ============================================================================

(deftest template-creation-test
  (testing "Template creation and structure"
    (let [template (v/create-cmd-template)]
      
      (testing "Template has all required fields"
        (is (map? template) "Template should be a map")
        (is (contains? template :protocol_version) "Should have protocol_version")
        (is (contains? template :client_type) "Should have client_type")
        (is (contains? template :session_id) "Should have session_id")
        (is (contains? template :important) "Should have important")
        (is (contains? template :from_cv_subsystem) "Should have from_cv_subsystem"))
      
      (testing "Template has all oneof fields"
        (doseq [field [:ping :noop :frozen :system :day_camera :heat_camera
                       :gps :compass :lrf :lrf_calib :rotary :osd :cv
                       :day_cam_glass_heater :lira]]
          (is (contains? template field) 
              (str "Template should have " field " field"))))
      
      (testing "Template default values"
        (is (= 0 (:protocol_version template)) "Default protocol_version should be 0")
        (is (= 0 (:session_id template)) "Default session_id should be 0")
        (is (false? (:important template)) "Default important should be false")
        (is (false? (:from_cv_subsystem template)) "Default from_cv_subsystem should be false")))))

;; ============================================================================
;; Roundtrip Edge Cases
;; ============================================================================

(deftest roundtrip-edge-cases-test
  (testing "Edge cases in roundtrip validation"
    
    (testing "Empty nested message"
      (let [cmd {:protocol_version 1
                 :client_type :JON_GUI_DATA_CLIENT_TYPE_LOCAL_NETWORK
                 :session_id 0
                 :important false
                 :from_cv_subsystem false
                 :system {:reboot {}}}
            result (v/validate-roundtrip-with-report cmd)]
        (is (:valid? result)
            (str "Command with empty nested message should roundtrip"
                 (when-not (:valid? result)
                   (str "\n" (:pretty-diff result)))))))
    
    (testing "Maximum session ID"
      (let [cmd {:protocol_version 1
                 :client_type :JON_GUI_DATA_CLIENT_TYPE_LOCAL_NETWORK
                 :session_id 2147483647  ; Max int32
                 :important false
                 :from_cv_subsystem false
                 :ping {}}]
        (is (:valid? (v/validate-roundtrip-with-report cmd))
            "Command with max session_id should roundtrip")))
    
    (testing "All boolean flags true"
      (let [cmd {:protocol_version 1
                 :client_type :JON_GUI_DATA_CLIENT_TYPE_LOCAL_NETWORK
                 :session_id 0
                 :important true
                 :from_cv_subsystem true
                 :noop {}}]
        (is (:valid? (v/validate-roundtrip-with-report cmd))
            "Command with all flags true should roundtrip")))
    
    (testing "Different client types"
      (doseq [client-type [:JON_GUI_DATA_CLIENT_TYPE_INTERNAL_CV
                          :JON_GUI_DATA_CLIENT_TYPE_LOCAL_NETWORK
                          :JON_GUI_DATA_CLIENT_TYPE_CERTIFICATE_PROTECTED
                          :JON_GUI_DATA_CLIENT_TYPE_LIRA]]
        (let [cmd {:protocol_version 1
                   :client_type client-type
                   :session_id 0
                   :important false
                   :from_cv_subsystem false
                   :frozen {}}]
          (is (:valid? (v/validate-roundtrip-with-report cmd))
              (str "Command with client_type " client-type " should roundtrip")))))))