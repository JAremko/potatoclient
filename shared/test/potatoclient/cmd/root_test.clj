(ns potatoclient.cmd.root-test
  "Roundtrip tests for root-level commands (ping, noop, frozen).
   Validates that commands are constructed correctly and survive serialization/deserialization."
  (:require
   [clojure.test :refer [deftest is testing]]
   [matcher-combinators.test] ;; extends clojure.test's `is` macro
   [matcher-combinators.matchers :as matchers]
   [potatoclient.cmd.root :as root]
   [potatoclient.cmd.core :as core]
   [potatoclient.cmd.validation :as validation]
   [potatoclient.malli.registry :as registry]
   [potatoclient.test-harness :as harness]
   [malli.core :as m]))

;; Ensure test harness is initialized
(when-not harness/initialized?
  (throw (ex-info "Test harness failed to initialize!" 
                  {:initialized? harness/initialized?})))

;; Initialize registry
(registry/setup-global-registry!)

;; ============================================================================
;; Test Helpers
;; ============================================================================

(defn validate-cmd
  "Validate a command against the cmd/root spec."
  [cmd]
  (m/validate (m/schema :cmd/root) cmd))

;; ============================================================================
;; Root-level Command Tests
;; ============================================================================

(deftest ping-command-test
  (testing "ping command construction and roundtrip"
    (let [cmd (root/ping)]
      (is (match? {:protocol_version 1
                   :client_type :JON_GUI_DATA_CLIENT_TYPE_LOCAL_NETWORK
                   :session_id 0
                   :important false
                   :from_cv_subsystem false
                   :ping {}}
                  cmd)
          "Command structure should match expected")
      (is (validate-cmd cmd) "Command should be valid against spec")
      
      (testing "roundtrip serialization"
        (let [result (validation/validate-roundtrip-with-report cmd)]
          (is (:valid? result) 
              (str "Command should survive serialization/deserialization"
                   (when-not (:valid? result)
                     (str "\n" (:pretty-diff result))))))))))

(deftest noop-command-test
  (testing "noop command construction and roundtrip"
    (let [cmd (root/noop)]
      (is (match? {:protocol_version 1
                   :client_type :JON_GUI_DATA_CLIENT_TYPE_LOCAL_NETWORK
                   :session_id 0
                   :important false
                   :from_cv_subsystem false
                   :noop {}}
                  cmd)
          "Command structure should match expected")
      (is (validate-cmd cmd) "Command should be valid against spec")
      
      (testing "roundtrip serialization"
        (let [result (validation/validate-roundtrip-with-report cmd)]
          (is (:valid? result) 
              (str "Command should survive serialization/deserialization"
                   (when-not (:valid? result)
                     (str "\n" (:pretty-diff result))))))))))

(deftest frozen-command-test
  (testing "frozen command construction and roundtrip"
    (let [cmd (root/frozen)]
      (is (match? {:protocol_version 1
                   :client_type :JON_GUI_DATA_CLIENT_TYPE_LOCAL_NETWORK
                   :session_id 0
                   :important false
                   :from_cv_subsystem false
                   :frozen {}}
                  cmd)
          "Command structure should match expected")
      (is (validate-cmd cmd) "Command should be valid against spec")
      
      (testing "roundtrip serialization"
        (let [result (validation/validate-roundtrip-with-report cmd)]
          (is (:valid? result) 
              (str "Command should survive serialization/deserialization"
                   (when-not (:valid? result)
                     (str "\n" (:pretty-diff result))))))))))

;; ============================================================================
;; Test Mode Verification
;; ============================================================================

(deftest test-mode-verification
  (testing "Commands should be validated in test mode"
    (is (core/in-test-mode?) "Should be running in test mode")
    
    (testing "Invalid commands should throw"
      (is (thrown? Exception
            (core/send-command! {:invalid_command {}}))
          "Invalid command should throw validation error"))))

;; ============================================================================
;; Core Features Tests
;; ============================================================================

;; Important flag test removed - it's just a default protocol field

(deftest ping-keep-alive-test
  (testing "Ping command for keep-alive"
    (let [ping-cmd (core/create-ping-command)]
      (is (match? {:protocol_version 1
                   :client_type :JON_GUI_DATA_CLIENT_TYPE_LOCAL_NETWORK
                   :session_id 0
                   :important false
                   :from_cv_subsystem false
                   :ping {}}
                  ping-cmd)
          "Should create complete ping command with all protocol fields")
      
      (testing "roundtrip validation"
        (let [validation-result (validation/validate-roundtrip-with-report ping-cmd)]
          (is (:valid? validation-result) 
              (str "Keep-alive ping should survive roundtrip"
                   (when-not (:valid? validation-result)
                     (str "\n" (:pretty-diff validation-result))))))))))