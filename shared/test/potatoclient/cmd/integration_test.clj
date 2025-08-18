(ns potatoclient.cmd.integration-test
  "Integration tests to verify the complete command system works end-to-end."
  (:require
   [clojure.test :refer [deftest is testing]]
   [potatoclient.cmd.root :as root]
   [potatoclient.cmd.system :as sys]
   [potatoclient.cmd.core :as core]
   [potatoclient.cmd.validation :as v]
   [potatoclient.cmd.builder :as builder]
   [potatoclient.proto.serialize :as serialize]
   [potatoclient.proto.deserialize :as deserialize]
   [potatoclient.malli.registry :as registry]
   [potatoclient.test-harness :as harness]))

;; Ensure test harness is initialized
(when-not harness/initialized?
  (throw (ex-info "Test harness failed to initialize!" 
                  {:initialized? harness/initialized?})))

;; Initialize registry
(registry/setup-global-registry!)

;; ============================================================================
;; Integration Test - Full Command Flow
;; ============================================================================

(deftest full-command-flow-test
  (testing "Complete command flow from function call to serialization"
    
    (testing "Root commands return full command with all fields"
      (let [ping-cmd (root/ping)]
        (is (map? ping-cmd) "Should return a map")
        (is (= 1 (:protocol_version ping-cmd)) "Should have protocol version")
        (is (= :JON_GUI_DATA_CLIENT_TYPE_LOCAL_NETWORK (:client_type ping-cmd)) "Should have client type")
        (is (= 0 (:session_id ping-cmd)) "Should have session ID")
        (is (false? (:important ping-cmd)) "Should have important flag")
        (is (false? (:from_cv_subsystem ping-cmd)) "Should have from_cv flag")
        (is (= {} (:ping ping-cmd)) "Should have ping payload")))
    
    (testing "Commands can be serialized to binary"
      (let [cmd (root/noop)
            binary (serialize/serialize-cmd-payload cmd)]
        (is (bytes? binary) "Should produce byte array")
        (is (pos? (count binary)) "Should have non-zero length")))
    
    (testing "Commands survive roundtrip serialization"
      (doseq [cmd-fn [root/ping root/noop root/frozen]]
        (let [cmd (cmd-fn)
              result (v/validate-roundtrip-with-report cmd)]
          (is (:valid? result) 
              (str "Command " cmd-fn " should roundtrip successfully")))))
    
    (testing "System commands work correctly"
      (let [reboot-cmd (sys/reboot)]
        (is (= {:reboot {}} (:system reboot-cmd)) "Should have system.reboot")
        (is (:valid? (v/validate-roundtrip-with-report reboot-cmd))
            "System command should roundtrip")))
    
    (testing "Commands with parameters work"
      (let [loc-cmd (sys/set-localization :JON_GUI_DATA_SYSTEM_LOCALIZATION_UA)]
        (is (= {:localization {:loc :JON_GUI_DATA_SYSTEM_LOCALIZATION_UA}} 
               (:system loc-cmd))
            "Should have correct localization")
        (is (:valid? (v/validate-roundtrip-with-report loc-cmd))
            "Parameterized command should roundtrip")))))

(deftest session-and-importance-test
  (testing "Session ID and importance flags work correctly"
    
    (testing "Commands with custom session ID"
      (let [cmd (core/send-command-with-session! {:ping {}} 12345)]
        (is (= 12345 (:session_id cmd)) "Should have custom session ID")
        (is (= {} (:ping cmd)) "Should have ping payload")
        (is (:valid? (v/validate-roundtrip-with-report cmd))
            "Custom session command should roundtrip")))
    
    (testing "Important commands"
      (let [cmd (core/send-important-command! {:frozen {}})]
        (is (true? (:important cmd)) "Should be marked important")
        (is (= {} (:frozen cmd)) "Should have frozen payload")
        (is (:valid? (v/validate-roundtrip-with-report cmd))
            "Important command should roundtrip")))))

(deftest builder-integration-test
  (testing "Builder functions work in the command flow"
    
    (testing "Builder properly populates fields"
      (let [minimal {:ping {}}
            full (builder/populate-cmd-fields minimal)]
        (is (= 1 (:protocol_version full)) "Should add protocol version")
        (is (= 0 (:session_id full)) "Should add default session ID")))
    
    (testing "Builder with overrides"
      (let [cmd {:noop {}}
            overrides {:session_id 999 :important true}
            full (builder/create-full-cmd cmd overrides)]
        (is (= 999 (:session_id full)) "Should use override session ID")
        (is (true? (:important full)) "Should use override important flag")))
    
    (testing "Batch command creation"
      (let [payloads [{:ping {}} {:noop {}} {:frozen {}}]
            overrides {:session_id 777}
            cmds (builder/create-batch-commands payloads overrides)]
        (is (= 3 (count cmds)) "Should create 3 commands")
        (is (every? #(= 777 (:session_id %)) cmds) 
            "All should have same session ID")
        (is (every? #(:valid? (v/validate-roundtrip-with-report %)) cmds)
            "All should be valid")))))

(deftest queue-operations-test
  (testing "Queue operations in test mode"
    (is (core/in-test-mode?) "Should be in test mode")
    
    (testing "Queue is not used in test mode"
      (core/clear-queue!)
      (let [initial-size (core/queue-size)]
        (root/ping)
        (root/noop)
        (root/frozen)
        (is (= initial-size (core/queue-size))
            "Queue should not grow in test mode")))))

(deftest error-handling-test
  (testing "Invalid commands are rejected"
    
    (testing "Invalid payload structure"
      (is (thrown? Exception
            (core/send-command! {:invalid_key {}}))
          "Should reject unknown command type"))
    
    (testing "Invalid enum values"
      (is (thrown? Exception
            (core/send-command! {:system {:localization {:loc :INVALID_ENUM}}}))
          "Should reject invalid enum"))
    
    (testing "Missing required nested fields"
      (is (thrown? Exception
            (core/send-command! {:system {:localization {}}}))
          "Should reject missing required field"))))

(deftest performance-sanity-test
  (testing "Commands are created efficiently"
    ;; This is a basic sanity check, not a real performance test
    (let [start (System/nanoTime)
          _ (dotimes [_ 1000] (root/ping))
          elapsed (/ (- (System/nanoTime) start) 1000000.0)]
      (is (< elapsed 15000) 
          (str "Should create 1000 commands in under 15 seconds, took " elapsed "ms")))))