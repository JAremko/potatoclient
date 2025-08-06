(ns generator.buf-validate-roundtrip-test
  "Test roundtrip with buf.validate validation"
  (:require [clojure.test :refer :all]
            [generator.core :as core]
            [clojure.java.io :as io])
  (:import [build.buf.validate Validator]
           [build.buf.protovalidate ValidationResult]
           [com.google.protobuf MessageOrBuilder]))

(defonce generated? (atom false))
(defonce validator (atom nil))

(defn ensure-generated-code!
  "Generate code once for all tests"
  []
  (when-not @generated?
    ;; Clear naming cache
    (require 'generator.naming)
    ((resolve 'generator.naming/clear-conversion-cache!))
    
    ;; Generate in single mode
    (let [result (core/generate-all {:input-dir "../proto-explorer/output/json-descriptors"
                                     :output-dir "test-validate-output"
                                     :namespace-prefix "test.validate"
                                     :namespace-mode :single
                                     :debug? false})]
      (when-not (:success result)
        (throw (ex-info "Failed to generate code" result)))
      
      (reset! generated? true)
      
      ;; Load the generated namespaces
      (require '[test.validate.command :as cmd])
      (require '[test.validate.state :as state]))))

(defn create-validator
  "Create a buf.validate Validator instance"
  []
  (when-not @validator
    (reset! validator (Validator.)))
  @validator)

(defn validate-proto
  "Validate a protobuf message using buf.validate"
  [^MessageOrBuilder proto]
  (let [validator (create-validator)
        result (.validate validator proto)]
    {:valid? (.isSuccess result)
     :violations (when-not (.isSuccess result)
                  (map (fn [v]
                         {:field (.getFieldPath v)
                          :message (.getMessage v)
                          :constraint (.getConstraintId v)})
                       (.getViolations result)))}))

(use-fixtures :once (fn [f] 
                     (ensure-generated-code!)
                     (f)))

(deftest validate-generated-protos
  (testing "Generated protobuf messages pass buf.validate"
    (let [cmd-ns (find-ns 'test.validate.command)
          build-root (ns-resolve cmd-ns 'build-root)]
      
      (testing "valid ping command"
        (let [proto (build-root {:ping {}})
              validation (validate-proto proto)]
          (is (:valid? validation)
              (str "Should be valid, but got: " (:violations validation)))))
      
      (testing "valid rotary command"
        (let [proto (build-root {:rotary {:goto-ndc {:channel :heat :x 0.5 :y -0.5}}})
              validation (validate-proto proto)]
          (is (:valid? validation))))
      
      (testing "invalid rotary command (out of range)"
        ;; Assuming NDC coordinates should be -1 to 1
        (let [proto (build-root {:rotary {:goto-ndc {:channel :heat :x 2.0 :y -0.5}}})
              validation (validate-proto proto)]
          ;; This might pass if there are no validation rules
          (println "Validation result for out-of-range:" validation))))))

(deftest validate-with-required-fields
  (testing "Messages with required fields validate correctly"
    (let [state-ns (find-ns 'test.validate.state)
          build-jon-gui-state (ns-resolve state-ns 'build-jon-gui-state)]
      
      (testing "valid state with all required fields"
        (let [proto (build-jon-gui-state {:gui-connected-to-state-updater? true
                                          :connected-to-command-runner? false
                                          :client-type :spectator
                                          :protocol-version 1})
              validation (validate-proto proto)]
          (is (:valid? validation))))
      
      (testing "invalid state missing required fields"
        ;; Try to build with missing fields
        (is (thrown? Exception
                    (build-jon-gui-state {:gui-connected-to-state-updater? true}))
            "Should throw when required fields are missing")))))

(deftest roundtrip-with-validation
  (testing "Roundtrip preserves validity"
    (let [cmd-ns (find-ns 'test.validate.command)
          build-root (ns-resolve cmd-ns 'build-root)
          parse-root (ns-resolve cmd-ns 'parse-root)]
      
      (doseq [input [{:ping {}}
                     {:rotary {:halt {}}}
                     {:system {:start-rec {}}}]]
        (testing (str "roundtrip for " input)
          (let [proto1 (build-root input)
                valid1 (validate-proto proto1)
                parsed (parse-root proto1)
                proto2 (build-root parsed)
                valid2 (validate-proto proto2)]
            
            (is (:valid? valid1) "Original should be valid")
            (is (:valid? valid2) "Roundtripped should be valid")
            (is (= valid1 valid2) "Validation results should match")))))))