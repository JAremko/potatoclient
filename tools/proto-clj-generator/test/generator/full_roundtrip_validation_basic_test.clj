(ns generator.full-roundtrip-validation-basic-test
  "Basic full roundtrip validation test"
  (:require [clojure.test :refer :all]
            [malli.core :as m]
            [malli.generator :as mg]
            [malli.registry :as mr]
            [potatoclient.specs.malli-oneof :as oneof]
            [generator.core :as core]
            [clojure.java.io :as io])
  (:import [cmd JonSharedCmd JonSharedCmd$Root]
           [ser JonSharedData JonSharedData$JonGUIState]))

;; =============================================================================
;; Test Setup
;; =============================================================================

(defonce generated? (atom false))

(defn ensure-generated-code!
  "Generate code once for all tests"
  []
  (when-not @generated?
    (println "\nGenerating code for full roundtrip validation tests...")
    ;; Clear naming cache to avoid collisions
    (require 'generator.naming)
    ((resolve 'generator.naming/clear-conversion-cache!))
    
    ;; Generate code with constraints
    (let [result (core/generate-all {:input-dir "../proto-explorer/output/json-descriptors"
                                     :output-dir "test-roundtrip-output"
                                     :namespace-prefix "test.roundtrip"
                                     :debug? false
                                     :guardrails? true})]
      (when-not (:success result)
        (throw (ex-info "Failed to generate code" result)))
      
      ;; Load generated namespaces
      (println "Loading generated code...")
      ;; Since we're using separated namespace mode, we need the actual namespace
      (require '[test.roundtrip.cmd :as cmd-gen] :reload)
      (require '[test.roundtrip.ser :as state-gen] :reload)
      
      (reset! generated? true))))

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
    (ensure-generated-code!)
    
    (let [cmd-ns (find-ns 'test.roundtrip.cmd)
          build-root (ns-resolve cmd-ns 'build-root)
          parse-root (ns-resolve cmd-ns 'parse-root)]
      
      (testing "Simple ping command"
        (let [cmd-data {:protocol-version 1
                       :client-type :jon-gui-data-client-type-local-network
                       :payload {:ping {:ping {}}}}
              proto (build-root cmd-data)
              binary (.toByteArray proto)
              parsed (JonSharedCmd$Root/parseFrom binary)
              roundtripped (parse-root parsed)]
          
          (is (instance? JonSharedCmd$Root proto))
          (is (= 1 (.getProtocolVersion proto)))
          (is (.hasPing proto))
          ;; The parsed data includes default values for optional fields
          (is (= 1 (:protocol-version roundtripped)))
          (is (= :jon-gui-data-client-type-local-network (:client-type roundtripped)))
          (is (= {:ping {:ping {}}} (:payload roundtripped)))))
      
      (testing "Protocol version constraint (> 0)"
        (is (thrown? Exception
                    (build-root {:protocol-version 0
                                :client-type :jon-gui-data-client-type-local-network
                                :payload {:ping {:ping {}}}}))
            "Protocol version 0 should fail validation")))))

(deftest test-oneof-validation
  (testing "Oneof validation in commands"
    (ensure-generated-code!)
    
    (let [cmd-ns (find-ns 'test.roundtrip.cmd)
          build-root (ns-resolve cmd-ns 'build-root)]
      
      (testing "Only one payload option allowed"
        ;; This should fail at the Clojure level with our oneof validation
        (is (thrown? Exception
                    (build-root {:protocol-version 1
                                :client-type :jon-gui-data-client-type-local-network
                                :payload {:ping {:ping {}}
                                         :system {:system {:start-rec {:start-rec {}}}}}}))))))

(deftest test-state-roundtrip
  (testing "State message roundtrip"
    (ensure-generated-code!)
    
    (let [state-ns (find-ns 'test.roundtrip.ser)
          build-fn (ns-resolve state-ns 'build-jon-gui-state)
          parse-fn (ns-resolve state-ns 'parse-jon-gui-state)]
      
      (testing "Minimal state"
        (let [state-data {:protocol-version 1}
              proto (build-fn state-data)
              binary (.toByteArray proto)
              parsed (JonSharedData$JonGUIState/parseFrom binary)
              roundtripped (parse-fn parsed)]
          
          (is (instance? JonSharedData$JonGUIState proto))
          (is (= 1 (.getProtocolVersion proto)))
          (is (= state-data roundtripped)))))))

(deftest test-enum-roundtrip
  (testing "Enum values roundtrip correctly"
    (ensure-generated-code!)
    
    (let [cmd-ns (find-ns 'test.roundtrip.cmd)
          build-root (ns-resolve cmd-ns 'build-root)
          parse-root (ns-resolve cmd-ns 'parse-root)]
      
      (testing "Client type enum"
        (let [cmd-data {:protocol-version 1
                       :client-type :jon-gui-data-client-type-remote-network
                       :payload {:ping {:ping {}}}}
              proto (build-root cmd-data)
              roundtripped (parse-root proto)]
          
          (is (= :jon-gui-data-client-type-remote-network (:client-type roundtripped))))))))

(deftest test-nested-messages
  (testing "Nested message structures"
    (ensure-generated-code!)
    
    (let [cmd-ns (find-ns 'test.roundtrip.cmd)
          build-root (ns-resolve cmd-ns 'build-root)
          parse-root (ns-resolve cmd-ns 'parse-root)]
      
      ;; Test with a more complex command if available
      (testing "System command with nested message"
        (let [cmd-data {:protocol-version 1
                       :client-type :jon-gui-data-client-type-local-network
                       :payload {:system {:system {:start-rec {:start-rec {}}}}}}
              proto (build-root cmd-data)
              roundtripped (parse-root proto)]
          
          ;; Just check that the main payload is preserved
          (is (= (:payload cmd-data) (:payload roundtripped)))))))))

(deftest test-constraint-validation
  (testing "Constraints are enforced"
    (ensure-generated-code!)
    
    (let [cmd-ns (find-ns 'test.roundtrip.cmd)
          build-root (ns-resolve cmd-ns 'build-root)]
      
      (testing "Protocol version must be > 0"
        (is (thrown? Exception
                    (build-root {:protocol-version -1
                                :client-type :jon-gui-data-client-type-local-network
                                :payload {:ping {:ping {}}}})))
        
        (is (thrown? Exception
                    (build-root {:protocol-version 0
                                :client-type :jon-gui-data-client-type-local-network
                                :payload {:ping {:ping {}}}})))
        
        ;; Valid case
        (is (some? (build-root {:protocol-version 1
                               :client-type :jon-gui-data-client-type-local-network
                               :payload {:ping {:ping {}}}})))))))

(run-tests)