(ns potatoclient.cmd.negative-test
  "Negative tests to ensure our guardrails and validation catch errors.
   These tests deliberately pass invalid arguments to ensure proper error handling."
  (:require
   [clojure.test :refer [deftest is testing]]
   [potatoclient.cmd.root :as root]
   [potatoclient.cmd.system :as sys]
   [potatoclient.cmd.core :as core]
   [potatoclient.malli.registry :as registry]
   [potatoclient.test-harness :as harness]))

;; Ensure test harness is initialized
(when-not harness/initialized?
  (throw (ex-info "Test harness failed to initialize!" 
                  {:initialized? harness/initialized?})))

;; Initialize registry
(registry/setup-global-registry!)

;; ============================================================================
;; Guardrails Function Argument Validation
;; ============================================================================

(deftest guardrails-argument-validation-test
  (testing "Guardrails should catch invalid function arguments"
    
    (testing "system/set-localization with invalid argument types"
      ;; Should take a keyword, not a string
      (is (thrown? Exception
            (sys/set-localization "not-a-keyword"))
          "Should reject string instead of keyword")
      
      ;; Should take a keyword, not a number
      (is (thrown? Exception
            (sys/set-localization 123))
          "Should reject number instead of keyword")
      
      ;; Should take a keyword, not nil
      (is (thrown? Exception
            (sys/set-localization nil))
          "Should reject nil instead of keyword"))
    
    (testing "Functions that take no arguments shouldn't accept any"
      ;; ping takes no arguments
      (is (thrown? Exception
            (root/ping "unexpected-arg"))
          "ping should reject unexpected arguments")
      
      ;; noop takes no arguments
      (is (thrown? Exception
            (root/noop {:some :map}))
          "noop should reject unexpected arguments"))))

;; ============================================================================
;; Validation of Invalid Commands
;; ============================================================================

(deftest invalid-command-validation-test
  (testing "Invalid commands should be rejected by validation"
    
    (testing "Invalid enum values in nested commands"
      (is (thrown? Exception
            (core/send-command! 
              {:system {:localization {:loc :INVALID_LOCALIZATION}}}))
          "Should reject invalid localization enum"))
    
    (testing "Missing required fields in nested messages"
      (is (thrown? Exception
            (core/send-command! 
              {:system {:localization {}}}))  ; Missing required 'loc' field
          "Should reject command with missing required field"))
    
    (testing "Wrong structure for commands"
      (is (thrown? Exception
            (core/send-command! 
              {:system "not-a-map"}))  ; System should be a map
          "Should reject wrong type for nested message"))
    
    (testing "Unknown command types"
      (is (thrown? Exception
            (core/send-command! 
              {:unknown_command {}}))
          "Should reject unknown command types"))
    
    (testing "Malformed root structure"
      (is (thrown? Exception
            (core/send-command! 
              "not-even-a-map"))
          "Should reject non-map as command"))))

;; ============================================================================
;; Boundary Value Testing
;; ============================================================================


;; ============================================================================
;; Sanity Check - Ensure Tests Can Detect Real Errors
;; ============================================================================

(deftest sanity-check-our-tests-work
  (testing "Sanity check - our tests can actually detect errors"
    
    (testing "Valid commands should pass"
      ;; This should NOT throw
      (is (= {:ping {}} (root/ping))
          "Valid ping should work")
      
      (is (= {:system {:reboot {}}} (sys/reboot))
          "Valid reboot should work")
      
      (is (= {:system {:localization {:loc :JON_GUI_DATA_SYSTEM_LOCALIZATION_EN}}}
             (sys/set-localization :JON_GUI_DATA_SYSTEM_LOCALIZATION_EN))
          "Valid set-localization should work"))
    
    (testing "Basic assertions work"
      (is (= 1 1) "Basic equality should pass")
      (is (not= 1 2) "Basic inequality should pass")
      (is (thrown? Exception (throw (Exception. "test")))
          "Exception detection should work"))
    
    (testing "Our validation actually validates"
      ;; Create a deliberately invalid command
      (let [invalid-cmd {:not :valid}]
        (is (thrown? Exception
              (core/send-command! invalid-cmd))
            "Invalid command should be rejected")))))

;; ============================================================================
;; Core Infrastructure Tests
;; ============================================================================

(deftest core-infrastructure-test
  (testing "Core command infrastructure"
    
    (testing "Queue operations in test mode"
      ;; In test mode, commands aren't actually queued
      (let [initial-size (core/queue-size)]
        (root/ping)
        (is (= initial-size (core/queue-size))
            "In test mode, queue shouldn't grow"))
      
      ;; Clear should work regardless
      (core/clear-queue!)
      (is (= 0 (core/queue-size))
          "Clear queue should work"))
    
    (testing "Test mode detection"
      (is (core/in-test-mode?)
          "Should detect we're in test mode"))
    
    (testing "Protocol fields are added correctly"
      (let [ping-cmd (core/create-ping-command)]
        (is (= 1 (:protocol_version ping-cmd))
            "Should have protocol version 1")
        (is (= :JON_GUI_DATA_CLIENT_TYPE_LOCAL_NETWORK (:client_type ping-cmd))
            "Should have correct client type")
        (is (= 0 (:session_id ping-cmd))
            "Should have default session ID")
        (is (false? (:important ping-cmd))
            "Should have important flag false by default")
        (is (false? (:from_cv_subsystem ping-cmd))
            "Should have from_cv_subsystem false by default")))))