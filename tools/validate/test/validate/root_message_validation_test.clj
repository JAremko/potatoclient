(ns validate.root-message-validation-test
  "Tests for validating complete root messages with buf.validate.
   buf.validate only works on root messages, not sub-messages."
  (:require
   [clojure.test :refer [deftest testing is]]
   [malli.core :as m]
   [malli.generator :as mg]
   [pronto.core :as p]
   [validate.test-harness :as h]
   [validate.validator :as v]
   [potatoclient.malli.registry :as registry]
   [potatoclient.specs.oneof-edn :as oneof-edn]
   ;; Load all specs
   [potatoclient.specs.common]
   [potatoclient.specs.state.root]
   [potatoclient.specs.cmd.root]
   [clojure.tools.logging :as log]))

;; Initialize registry
(registry/setup-global-registry!
  (oneof-edn/register_ONeof-edn-schema!))

;; ============================================================================
;; ROOT STATE MESSAGE VALIDATION
;; ============================================================================

(deftest test-complete-state-validation
  (testing "Complete state message validation with buf.validate"
    (testing "Valid state from test harness"
      (let [state-proto (h/valid-state)
            binary (p/proto-map->bytes state-proto)
            result (v/validate-binary binary :type :state)]
        (is (:valid? result)
            (str "Valid state should pass buf.validate. "
                 "Violations: " (:violations result)))))
    
    (testing "State with boundary GPS values"
      (let [state-proto (h/state-with-boundary-values)
            binary (p/proto-map->bytes state-proto)
            result (v/validate-binary binary :type :state)]
        (is (:valid? result)
            "State with boundary values should validate")))
    
    (testing "Invalid state - bad GPS coordinates"
      (let [state-proto (h/invalid-gps-state)
            binary (p/proto-map->bytes state-proto)
            result (v/validate-binary binary :type :state)]
        (is (not (:valid? result))
            "State with invalid GPS should fail validation")
        (is (some #(re-find #"latitude" (:field %)) (:violations result))
            "Should have latitude violation")
        (is (some #(re-find #"longitude" (:field %)) (:violations result))
            "Should have longitude violation")))
    
    (testing "Invalid state - zero protocol version"
      (let [state-proto (h/invalid-protocol-state)
            binary (p/proto-map->bytes state-proto)
            result (v/validate-binary binary :type :state)]
        (is (not (:valid? result))
            "State with zero protocol version should fail")
        (is (some #(= "protocol_version" (:field %)) (:violations result))
            "Should have protocol_version violation")))))

;; ============================================================================
;; ROOT COMMAND MESSAGE VALIDATION
;; ============================================================================

(deftest test-command-validation
  (testing "Command message validation with buf.validate"
    (testing "Valid ping command"
      (let [cmd-proto (h/valid-ping-cmd)
            binary (p/proto-map->bytes cmd-proto)
            result (v/validate-binary binary :type :cmd)]
        (is (:valid? result)
            "Valid ping command should pass validation")))
    
    (testing "Valid noop command"
      (let [cmd-proto (h/valid-noop-cmd)
            binary (p/proto-map->bytes cmd-proto)
            result (v/validate-binary binary :type :cmd)]
        (is (:valid? result)
            "Valid noop command should pass validation")))
    
    (testing "Valid rotary command"
      (let [cmd-proto (h/valid-rotary-azimuth-cmd)
            binary (p/proto-map->bytes cmd-proto)
            result (v/validate-binary binary :type :cmd)]
        (is (:valid? result)
            "Valid rotary command should pass validation")))
    
    (testing "Invalid command - UNSPECIFIED client type"
      (let [cmd-proto (h/invalid-client-type-cmd)
            binary (p/proto-map->bytes cmd-proto)
            result (v/validate-binary binary :type :cmd)]
        (is (not (:valid? result))
            "Command with UNSPECIFIED client type should fail")
        (is (some #(= "client_type" (:field %)) (:violations result))
            "Should have client_type violation")))
    
    (testing "Invalid command - zero protocol version"
      (let [cmd-proto (h/invalid-protocol-cmd)
            binary (p/proto-map->bytes cmd-proto)
            result (v/validate-binary binary :type :cmd)]
        (is (not (:valid? result))
            "Command with zero protocol version should fail")
        (is (some #(= "protocol_version" (:field %)) (:violations result))
            "Should have protocol_version violation")))))

;; ============================================================================
;; ROUND-TRIP VALIDATION (ROOT MESSAGES ONLY)
;; ============================================================================

(deftest test-root-message-round-trips
  (testing "Round-trip validation for root messages"
    (testing "State message round-trip"
      (let [;; Start with valid state
            state-1 (h/valid-state)
            
            ;; Convert to binary
            binary (p/proto-map->bytes state-1)
            
            ;; Validate
            validation (v/validate-binary binary :type :state)
            
            ;; Parse back
            state-2 (p/bytes->proto-map h/state-mapper 
                                        ser.JonSharedData$JonGUIState 
                                        binary)
            
            ;; Convert both to EDN for comparison
            edn-1 (p/proto-map->clj-map state-1)
            edn-2 (p/proto-map->clj-map state-2)]
        
        (is (:valid? validation)
            "State should validate during round-trip")
        (is (= edn-1 edn-2)
            "State data should be preserved through round-trip")))
    
    (testing "Command message round-trip"
      (let [;; Start with valid command
            cmd-1 (h/valid-ping-cmd)
            
            ;; Convert to binary
            binary (p/proto-map->bytes cmd-1)
            
            ;; Validate
            validation (v/validate-binary binary :type :cmd)
            
            ;; Parse back
            cmd-2 (p/bytes->proto-map h/cmd-mapper 
                                      cmd.JonSharedCmd$Root 
                                      binary)
            
            ;; Convert both to EDN for comparison
            edn-1 (p/proto-map->clj-map cmd-1)
            edn-2 (p/proto-map->clj-map cmd-2)]
        
        (is (:valid? validation)
            "Command should validate during round-trip")
        (is (= edn-1 edn-2)
            "Command data should be preserved through round-trip")))))

;; ============================================================================
;; MALLI SPEC TO ROOT MESSAGE VALIDATION
;; ============================================================================

(deftest test-spec-generated-root-messages
  (testing "Malli spec generated root messages"
    (testing "Generate and validate state messages"
      (let [;; We can't easily generate valid complete states yet
            ;; because the specs need proper generators for all nested fields
            ;; For now, test with modified valid state
            base-state (h/valid-state)
            
            ;; Modify with generated GPS values
            gps-spec-data (mg/generate :state/gps)
            modified-state (p/p-> base-state
                                 (assoc-in [:gps :latitude] 
                                          (:latitude gps-spec-data))
                                 (assoc-in [:gps :longitude] 
                                          (:longitude gps-spec-data))
                                 (assoc-in [:gps :altitude] 
                                          (:altitude gps-spec-data)))
            
            binary (p/proto-map->bytes modified-state)
            validation (v/validate-binary binary :type :state)]
        
        (is (:valid? validation)
            (str "State with generated GPS should validate. "
                 "GPS data: " gps-spec-data))))
    
    (testing "Validate command with generated protocol version"
      (let [;; Generate protocol version from spec
            pv (mg/generate :proto/protocol-version)
            
            ;; Create command with generated protocol version
            cmd (p/proto-map h/cmd-mapper cmd.JonSharedCmd$Root
                            :protocol_version pv
                            :client_type :JON_GUI_DATA_CLIENT_TYPE_LOCAL_NETWORK
                            :ping (p/proto-map h/cmd-mapper cmd.JonSharedCmd$Ping))
            
            binary (p/proto-map->bytes cmd)
            validation (v/validate-binary binary :type :cmd)]
        
        (is (:valid? validation)
            (str "Command with generated protocol version " pv 
                 " should validate"))))))

;; ============================================================================
;; PERFORMANCE TEST
;; ============================================================================

(deftest test-validation-performance
  (testing "Validation performance"
    (let [state-binary (p/proto-map->bytes (h/valid-state))
          cmd-binary (p/proto-map->bytes (h/valid-ping-cmd))
          
          ;; Warm up the validator
          _ (v/validate-binary state-binary :type :state)
          _ (v/validate-binary cmd-binary :type :cmd)
          
          ;; Time 100 validations
          state-start (System/currentTimeMillis)
          _ (dotimes [_ 100]
              (v/validate-binary state-binary :type :state))
          state-time (- (System/currentTimeMillis) state-start)
          
          cmd-start (System/currentTimeMillis)
          _ (dotimes [_ 100]
              (v/validate-binary cmd-binary :type :cmd))
          cmd-time (- (System/currentTimeMillis) cmd-start)]
      
      (println (str "\nPerformance: 100 state validations in " state-time "ms"
                   " (" (/ state-time 100.0) "ms per validation)"))
      (println (str "Performance: 100 command validations in " cmd-time "ms"
                   " (" (/ cmd-time 100.0) "ms per validation)"))
      
      (is (< (/ state-time 100.0) 20)
          "State validation should be under 20ms per message")
      (is (< (/ cmd-time 100.0) 20)
          "Command validation should be under 20ms per message"))))