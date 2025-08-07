(ns generator.full-roundtrip-validation-basic-test
  "Basic full roundtrip validation test"
  (:require [clojure.test :refer :all]
            [malli.core :as m]
            [malli.generator :as mg]
            [malli.registry :as mr]
            [potatoclient.specs.malli-oneof :as oneof]
            ;; Pre-generated namespaces
            [test.roundtrip.cmd :as cmd-gen]
            [test.roundtrip.ser :as state-gen])
  (:import [cmd JonSharedCmd JonSharedCmd$Root]
           [ser JonSharedData JonSharedData$JonGUIState]))

;; =============================================================================
;; Test Setup
;; =============================================================================

;; Register the custom :oneof schema globally
(mr/set-default-registry!
  (merge (m/default-schemas)
         (mr/schemas m/default-registry)
         {:oneof oneof/-oneof-schema}))

;; =============================================================================
;; Basic Roundtrip Test
;; =============================================================================

(deftest test-basic-cmd-roundtrip
  (testing "Basic command roundtrip"
    (testing "Simple ping command"
      (let [cmd-data {:protocol-version 1
                     :client-type :jon-gui-data-client-type-local-network
                     :payload {:ping {:ping {}}}}
            proto (cmd-gen/build-root cmd-data)
            binary (.toByteArray proto)
            parsed (JonSharedCmd$Root/parseFrom binary)
            roundtripped (cmd-gen/parse-root parsed)]
        
        (is (instance? JonSharedCmd$Root proto))
        (is (= 1 (.getProtocolVersion proto)))
        (is (.hasPing proto))
        ;; The parsed data includes default values for optional fields
        (is (= 1 (:protocol-version roundtripped)))
        (is (= :jon-gui-data-client-type-local-network (:client-type roundtripped)))
        (is (= {:ping {:ping {}}} (:payload roundtripped)))))
    
    (testing "Protocol version constraint (> 0)"
      (is (thrown? Exception
                  (cmd-gen/build-root {:protocol-version 0
                                      :client-type :jon-gui-data-client-type-local-network
                                      :payload {:ping {:ping {}}}}))
          "Protocol version 0 should fail validation"))))

(deftest test-oneof-validation
  (testing "Oneof validation in commands"
    (testing "Only one payload option allowed"
      ;; This should fail at the Clojure level with our oneof validation
      (is (thrown? Exception
                  (cmd-gen/build-root {:protocol-version 1
                                      :client-type :jon-gui-data-client-type-local-network
                                      :payload {:ping {:ping {}}
                                               :system {:system {:start-rec {:start-rec {}}}}}}))))))

(deftest test-state-roundtrip
  (testing "State message roundtrip"
    (testing "Minimal state"
      (let [state-data {:protocol-version 1}
            proto (state-gen/build-jon-gui-state state-data)
            binary (.toByteArray proto)
            parsed (JonSharedData$JonGUIState/parseFrom binary)
            roundtripped (state-gen/parse-jon-gui-state parsed)]
        
        (is (instance? JonSharedData$JonGUIState proto))
        (is (= 1 (.getProtocolVersion proto)))
        (is (= state-data roundtripped))))))

(deftest test-enum-roundtrip
  (testing "Enum values roundtrip correctly"
    (testing "Client type enum"
      (let [cmd-data {:protocol-version 1
                     :client-type :jon-gui-data-client-type-remote-network
                     :payload {:ping {:ping {}}}}
            proto (cmd-gen/build-root cmd-data)
            roundtripped (cmd-gen/parse-root proto)]
        
        (is (= :jon-gui-data-client-type-remote-network (:client-type roundtripped)))))))

(deftest test-nested-messages
  (testing "Nested message structures"
    ;; Test with a more complex command if available
    (testing "System command with nested message"
      (let [cmd-data {:protocol-version 1
                     :client-type :jon-gui-data-client-type-local-network
                     :payload {:system {:system {:start-rec {:start-rec {}}}}}}
            proto (cmd-gen/build-root cmd-data)
            roundtripped (cmd-gen/parse-root proto)]
        
        ;; Just check that the main payload is preserved
        (is (= (:payload cmd-data) (:payload roundtripped)))))))

(deftest test-constraint-validation
  (testing "Constraints are enforced"
    (testing "Protocol version must be > 0"
      (is (thrown? Exception
                  (cmd-gen/build-root {:protocol-version -1
                                      :client-type :jon-gui-data-client-type-local-network
                                      :payload {:ping {:ping {}}}})))
      
      (is (thrown? Exception
                  (cmd-gen/build-root {:protocol-version 0
                                      :client-type :jon-gui-data-client-type-local-network
                                      :payload {:ping {:ping {}}}})))
      
      ;; Valid case
      (is (some? (cmd-gen/build-root {:protocol-version 1
                                     :client-type :jon-gui-data-client-type-local-network
                                     :payload {:ping {:ping {}}}}))))))