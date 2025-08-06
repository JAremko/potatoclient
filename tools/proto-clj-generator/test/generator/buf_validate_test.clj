(ns generator.buf-validate-test
  "Test buf.validate constraints are enforced"
  (:require [clojure.test :refer :all]
            [potatoclient.proto.cmd :as cmd-gen]
            [potatoclient.proto.ser :as state-gen])
  (:import [cmd JonSharedCmd JonSharedCmd$Root]
           [ser JonSharedData JonSharedData$JonGUIState JonSharedDataTypes$JonGuiDataClientType]
           [build.buf.protovalidate Validator ValidatorFactory ValidationResult]
           [build.buf.validate Violation]
           [build.buf.protovalidate.exceptions ValidationException]))

(defn- violation->proto 
  "Convert new API Violation to protobuf Violation for consistent access"
  [violation]
  (.toProto violation))

(defn- field-path->string
  "Convert FieldPath to string representation"
  [field-path]
  (let [elements (.getElementsList field-path)]
    (if (empty? elements)
      ""
      (let [element (first elements)
            element-str (str element)]
        ;; Extract field_name from the element string representation
        ;; Format is like: field_number: 1\nfield_name: "protocol_version"\nfield_type: TYPE_UINT32
        (if-let [match (re-find #"field_name:\s*\"([^\"]+)\"" element-str)]
          (second match)
          element-str)))))

(def validator 
  (try 
    (.build (ValidatorFactory/newBuilder))
    (catch Exception e
      (println "Failed to create Validator:" (.getMessage e))
      (println "Stack trace:" (.printStackTrace e))
      nil)))

(deftest command-validation-rules
  (when validator
    (testing "Command protocol_version validation"
      (testing "protocol_version must be > 0"
        ;; Valid case
        (let [valid-cmd {:protocol-version 1 
                        :client-type :jon-gui-data-client-type-local-network
                        :ping {}}
              proto (cmd-gen/build-root valid-cmd)]
          (is (instance? JonSharedCmd$Root proto))
          ;; Validate with buf.validate
          (let [result (.validate validator proto)]
            (is (.isSuccess result) 
                (str "Valid command should pass validation. Violations: " 
                     (when-not (.isSuccess result) (.getViolations result)))))))
        
        ;; Invalid case - protocol_version = 0
        (let [invalid-cmd {:protocol-version 0 :ping {}}
              proto (cmd-gen/build-root invalid-cmd)]
          (is (instance? JonSharedCmd$Root proto))
          ;; Validate with buf.validate - should fail
          (let [result (.validate validator proto)]
            (is (not (.isSuccess result))
                "Protocol version 0 should fail validation")
            (when-not (.isSuccess result)
              (doseq [violation (.getViolations result)]
                (let [proto-violation (violation->proto violation)]
                  (println "Violation:" (.getMessage proto-violation) "Field:" (field-path->string (.getField proto-violation))))))))))
    
    (testing "Command requires payload (oneof validation)"
      ;; Invalid case - no payload
      (let [invalid-cmd {:protocol-version 1}
            proto (cmd-gen/build-root invalid-cmd)]
        (is (instance? JonSharedCmd$Root proto))
        ;; Should fail validation due to missing required oneof
        (let [result (.validate validator proto)]
          (is (not (.isSuccess result))
              "Command without payload should fail validation")
          (when-not (.isSuccess result)
            (println "Missing payload violations:")
            (doseq [violation (.getViolations result)]
              (let [proto-violation (violation->proto violation)]
                (println "  -" (.getMessage proto-violation)))))))))

(deftest state-validation-rules
  (when validator
    (testing "State protocol_version validation"
      (testing "protocol_version must be > 0"
        ;; Valid case
        (let [valid-state {:protocol-version 1}
              proto (state-gen/build-jon-gui-state valid-state)]
          (is (instance? JonSharedData$JonGUIState proto))
          ;; Since other fields are required, this will still fail
          ;; but let's check the specific error
          (let [result (.validate validator proto)]
            (is (not (.isSuccess result))
                "Should fail due to missing required fields")
            (when-not (.isSuccess result)
              (let [violations (.getViolations result)
                    proto-violations (map violation->proto violations)
                    messages (map #(.getMessage %) proto-violations)]
                (is (some #(re-find #"required" %) messages)
                    "Should mention required fields")))))
        
        ;; Invalid case - protocol_version = 0
        (let [invalid-state {:protocol-version 0}
              proto (state-gen/build-jon-gui-state invalid-state)]
          (is (instance? JonSharedData$JonGUIState proto))
          (let [result (.validate validator proto)]
            (is (not (.isSuccess result))
                "Protocol version 0 should fail validation")
            (when-not (.isSuccess result)
              (let [violations (.getViolations result)
                    proto-violations (map violation->proto violations)
                    protocol-violations (filter #(re-find #"protocol_version" (field-path->string (.getField %))) proto-violations)]
                (is (seq protocol-violations)
                    "Should have protocol_version violations")))))))))

(deftest validation-error-messages
  (when validator
    (testing "Validation provides meaningful error messages"
      (let [invalid-cmd {:protocol-version 0 :ping {}}
            proto (cmd-gen/build-root invalid-cmd)
            result (.validate validator proto)]
        (is (not (.isSuccess result))
            "Validation should fail")
        (when-not (.isSuccess result)
          (let [violations (.getViolations result)
                proto-violations (map violation->proto violations)
                messages (map #(.getMessage %) proto-violations)
                field-paths (map #(field-path->string (.getField %)) proto-violations)]
            (is (some #(re-find #"protocol_version" %) field-paths)
                "Error should mention protocol_version field")
            (is (some #(or (re-find #"greater than" %)
                          (re-find #"gt" %)
                          (re-find #">" %)) messages)
                "Error should mention the constraint")))))))

;; Test without validator - just protobuf constraints
(deftest protobuf-built-in-validation
  (testing "Protobuf allows setting negative values on uint32 (wraps around)"
    ;; Java protobuf doesn't enforce uint32 non-negative at build time
    ;; Negative values wrap around to large positive values
    (let [cmd (cmd-gen/build-root {:protocol-version -1 
                                   :client-type :jon-gui-data-client-type-local-network
                                   :ping {}})
          protocol-version (.getProtocolVersion cmd)]
      (is (instance? JonSharedCmd$Root cmd))
      ;; Java protobuf returns the value as-is (as a signed int)
      ;; The wrapping to unsigned happens at serialization time
      (is (= -1 protocol-version) 
          "Java API returns negative value as-is"))))

(deftest specific-validation-constraints
  (when validator
    (testing "Field-specific buf.validate constraints"
      (testing "protocol_version must be > 0 (edge cases)"
        ;; Test exact boundary (0)
        (let [zero-cmd {:protocol-version 0
                       :client-type :jon-gui-data-client-type-local-network
                       :ping {}}
              proto (cmd-gen/build-root zero-cmd)
              result (.validate validator proto)]
          (is (not (.isSuccess result))
              "protocol_version = 0 should fail validation")
          (let [violations (.getViolations result)
                proto-violations (map violation->proto violations)
                protocol-violations (filter #(re-find #"protocol_version" (field-path->string (.getField %))) proto-violations)]
            (is (seq protocol-violations) "Should have protocol_version violation")
            (is (some #(re-find #"greater than 0" (.getMessage %)) protocol-violations)
                "Should mention 'greater than 0' constraint")))
        
        ;; Test valid boundary (1)
        (let [one-cmd {:protocol-version 1
                      :client-type :jon-gui-data-client-type-local-network
                      :ping {}}
              proto (cmd-gen/build-root one-cmd)
              result (.validate validator proto)]
          (is (.isSuccess result)
              "protocol_version = 1 should pass validation"))
        
        ;; Test well above boundary
        (let [large-cmd {:protocol-version 1000000
                        :client-type :jon-gui-data-client-type-local-network
                        :ping {}}
              proto (cmd-gen/build-root large-cmd)
              result (.validate validator proto)]
          (is (.isSuccess result)
              "Large protocol_version should pass validation")))
      
      (testing "client_type enum constraints"
        ;; Test UNSPECIFIED (0) is rejected
        (let [unspec-cmd {:protocol-version 1
                         :client-type :jon-gui-data-client-type-unspecified
                         :ping {}}
              proto (cmd-gen/build-root unspec-cmd)
              result (.validate validator proto)]
          (is (not (.isSuccess result))
              "UNSPECIFIED client_type should fail validation")
          (let [violations (.getViolations result)
                proto-violations (map violation->proto violations)
                client-violations (filter #(re-find #"client_type" (field-path->string (.getField %))) proto-violations)]
            (is (seq client-violations) "Should have client_type violation")
            (is (some #(re-find #"not.*in.*\[0\]" (.getMessage %)) client-violations)
                "Should mention 'not in [0]' constraint")))
        
        ;; Test valid enum values
        (doseq [[name value] [["INTERNAL_CV" :jon-gui-data-client-type-internal-cv]
                             ["LOCAL_NETWORK" :jon-gui-data-client-type-local-network]
                             ["CERTIFICATE_PROTECTED" :jon-gui-data-client-type-certificate-protected]
                             ["LIRA" :jon-gui-data-client-type-lira]]]
          (let [cmd {:protocol-version 1
                    :client-type value
                    :ping {}}
                proto (cmd-gen/build-root cmd)
                result (.validate validator proto)]
            (is (.isSuccess result)
                (str "client_type " name " should pass validation"))))))))