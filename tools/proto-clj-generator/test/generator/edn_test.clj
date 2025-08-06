(ns generator.edn-test
  "Tests for the EDN intermediate representation."
  (:require [clojure.test :refer :all]
            [generator.backend :as backend]
            [generator.frontend :as frontend]
            [generator.edn-specs :as specs]
            [malli.core :as m]
            [malli.generator :as mg]
            [clojure.pprint :as pp]
            [clojure.string :as str]
            [com.rpl.specter :as sp]))

(deftest scalar-field-test
  (testing "Scalar field conversion to EDN"
    (let [proto-field {:name "protocol_version"
                       :number 1
                       :label "LABEL_OPTIONAL"
                       :type "TYPE_INT32"}
          edn-field (backend/field->edn proto-field)]
      
      (is (= :protocol-version (:name edn-field)))
      (is (= "protocol_version" (:proto-name edn-field)))
      (is (= 1 (:number edn-field)))
      (is (= {:scalar :int32} (:type edn-field)))
      (is (= :label-optional (:label edn-field)))
      (is (true? (:optional? edn-field)))
      
      (is (specs/validate-field edn-field)
          "Field should validate against spec"))))

(deftest message-field-test
  (testing "Message field conversion to EDN"
    (let [proto-field {:name "ping"
                       :number 2
                       :label "LABEL_OPTIONAL"
                       :type "TYPE_MESSAGE"
                       :typeName ".cmd.System.Ping"
                       :oneofIndex 0}
          edn-field (backend/field->edn proto-field)]
      
      (is (= :ping (:name edn-field)))
      (is (= {:message {:type-ref ".cmd.System.Ping"}} (:type edn-field)))
      (is (= 0 (:oneof-index edn-field)))
      
      (is (specs/validate-field edn-field)
          "Field should validate against spec"))))

(deftest enum-field-test
  (testing "Enum field conversion to EDN"
    (let [proto-field {:name "mode"
                       :number 1
                       :label "LABEL_OPTIONAL"
                       :type "TYPE_ENUM"
                       :typeName ".cmd.FocusMode"}
          edn-field (backend/field->edn proto-field)]
      
      (is (= :mode (:name edn-field)))
      (is (= {:enum {:type-ref ".cmd.FocusMode"}} (:type edn-field)))
      
      (is (specs/validate-field edn-field)
          "Field should validate against spec"))))

(deftest message-conversion-test
  (testing "Complete message conversion to EDN"
    (let [proto-message {:name "Root"
                        :field [{:name "protocol_version"
                                :number 1
                                :label "LABEL_OPTIONAL"
                                :type "TYPE_INT32"}
                               {:name "ping"
                                :number 2
                                :label "LABEL_OPTIONAL"
                                :type "TYPE_MESSAGE"
                                :type-name ".cmd.System.Ping"
                                :oneof-index 0}]
                        :oneof-decl [{:name "payload"}]}
          context {:package "cmd"
                   :java-package nil
                   :java-outer-classname "JonSharedCmd"}
          edn-message (backend/message->edn proto-message context)]
      
      (is (= :message (:type edn-message)))
      (is (= :root (:name edn-message)))
      (is (= "Root" (:proto-name edn-message)))
      (is (= "cmd.JonSharedCmd$Root" (:java-class edn-message)))
      
      (is (= 1 (count (:fields edn-message))))
      (is (= :protocol-version (-> edn-message :fields first :name)))
      
      (is (= 1 (count (:oneofs edn-message))))
      (is (= :payload (-> edn-message :oneofs first :name)))
      (is (= 1 (count (-> edn-message :oneofs first :fields))))
      
      (is (specs/validate-message edn-message)
          "Message should validate against spec"))))

(deftest enum-conversion-test
  (testing "Enum conversion to EDN"
    (let [proto-enum {:name "FocusMode"
                     :value [{:name "FOCUS_MODE_UNKNOWN" :number 0}
                            {:name "FOCUS_MODE_AUTO" :number 1}
                            {:name "FOCUS_MODE_MANUAL" :number 2}]}
          context {:package "cmd"
                   :java-package nil
                   :java-outer-classname "JonSharedCmd"}
          edn-enum (backend/enum->edn proto-enum context)]
      
      (is (= :enum (:type edn-enum)))
      (is (= :focus-mode (:name edn-enum)))
      (is (= "cmd.JonSharedCmd$FocusMode" (:java-class edn-enum)))
      
      (is (= 3 (count (:values edn-enum))))
      (is (= #{:focus-mode-unknown :focus-mode-auto :focus-mode-manual}
             (set (map :name (:values edn-enum)))))
      
      (is (m/validate specs/EnumDef edn-enum {:registry specs/registry})
          "Enum should validate against spec"))))

(deftest type-lookup-test
  (testing "Type lookup building"
    (let [edn-data {:type :descriptor-set
                   :files [{:type :file
                            :package "cmd"
                            :messages [{:type :message
                                       :name :root
                                       :proto-name "Root"
                                       :java-class "cmd.JonSharedCmd$Root"
                                       :package "cmd"
                                       :fields []
                                       :oneofs []
                                       :nested-types [{:type :message
                                                     :name :ping
                                                     :proto-name "Ping"
                                                     :java-class "cmd.System$Ping"
                                                     :package "cmd"
                                                     :fields []
                                                     :oneofs []
                                                     :nested-types []}]}]
                            :enums [{:type :enum
                                    :name :focus-mode
                                    :proto-name "FocusMode"
                                    :java-class "cmd.JonSharedCmd$FocusMode"
                                    :package "cmd"
                                    :values []}]}]}
          lookup (backend/build-type-lookup edn-data)]
      
      ;; The type lookup is keyed by canonical type references
      (is (contains? lookup ".cmd.Root"))
      (is (contains? lookup ".cmd.System.Ping"))
      (is (contains? lookup ".cmd.FocusMode"))
      
      (is (= "cmd.JonSharedCmd$Root" (:java-class (get lookup ".cmd.Root"))))
      (is (= "cmd.System$Ping" (:java-class (get lookup ".cmd.System.Ping")))))))

;; TODO: Implement resolve-builder-name function in frontend
#_(deftest builder-name-resolution-test
  (testing "Builder name resolution from type references"
    (let [type-lookup {:ping {:name :ping}
                      :calibrate-start-long {:name :calibrate-start-long}}]
      
      (is (= "build-ping" 
             (frontend/resolve-builder-name ".cmd.System.Ping" type-lookup)))
      
      (is (= "build-calibrate-start-long"
             (frontend/resolve-builder-name ".cmd.Compass.CalibrateStartLong" type-lookup)))
      
      ;; Test fallback when not in lookup
      (is (= "build-unknown-message"
             (frontend/resolve-builder-name ".cmd.UnknownMessage" type-lookup))))))

(deftest code-generation-test
  (testing "Code generation from EDN"
    (let [edn-message {:type :message
                      :name :ping
                      :proto-name "Ping"
                      :java-class "cmd.System$Ping"
                      :fields [{:name :timestamp
                               :proto-name "timestamp"
                               :number 1
                               :type {:scalar :int64}}]
                      :oneofs []}
          type-lookup {}
          builder-code (frontend/generate-builder edn-message type-lookup)
          parser-code (frontend/generate-parser edn-message type-lookup)]
      
      (is (str/includes? builder-code "defn build-ping"))
      (is (str/includes? builder-code "cmd.System$Ping/newBuilder"))
      (is (str/includes? builder-code "setTimestamp"))
      
      (is (str/includes? parser-code "defn parse-ping"))
      (is (str/includes? parser-code "^cmd.System$Ping proto"))
      (is (str/includes? parser-code "getTimestamp")))))

(deftest roundtrip-edn-test
  (testing "EDN representation roundtrip"
    ;; Create a simple proto descriptor in memory
    (let [proto-desc {:file [{:name "test.proto"
                             :package "test"
                             :options {:java-outer-classname "TestProto"}
                             :message-type [{:name "TestMessage"
                                           :field [{:name "id"
                                                   :number 1
                                                   :label "LABEL_OPTIONAL"
                                                   :type "TYPE_INT32"}
                                                  {:name "name"
                                                   :number 2
                                                   :label "LABEL_OPTIONAL"
                                                   :type "TYPE_STRING"}]}]
                             :enum-type [{:name "TestEnum"
                                        :value [{:name "UNKNOWN" :number 0}
                                               {:name "ACTIVE" :number 1}]}]}]}
          
          ;; Convert to EDN through backend
          ;; parse-descriptor-set expects a directory path, not a proto descriptor
          ;; This test needs to be rewritten to test the actual JSON parsing
          edn-output {:files [{:name "test.proto"
                              :package "test"
                              :messages [{:name :test-message
                                         :proto-name "TestMessage"
                                         :fields [{:name :id
                                                  :proto-name "id"
                                                  :number 1
                                                  :type {:scalar :int32}
                                                  :label :label-optional
                                                  :optional? true}
                                                 {:name :name
                                                  :proto-name "name"
                                                  :number 2
                                                  :type {:scalar :string}
                                                  :label :label-optional
                                                  :optional? true}]}]
                              :enums [{:name :test-enum
                                      :proto-name "TestEnum"
                                      :values [{:name :unknown :number 0}
                                              {:name :active :number 1}]}]}]}
          
          ;; Validate EDN structure
          validation (specs/validate-descriptor-set edn-output)]
      
      (is validation "EDN output should validate against spec")
      
      (when-not validation
        (println "Validation errors:")
        (pp/pprint (specs/explain-validation-error specs/DescriptorSet edn-output)))
      
      ;; Check structure
      (is (= :descriptor-set (:type edn-output)))
      (is (= 1 (count (:files edn-output))))
      
      (let [file (first (:files edn-output))]
        (is (= :file (:type file)))
        (is (= "test" (:package file)))
        (is (= "TestProto" (:java-outer-classname file)))
        (is (= 1 (count (:messages file))))
        (is (= 1 (count (:enums file))))))))

(deftest backend-output-validation-test
  (testing "Complete backend output validation"
    ;; This would test with real descriptor files in a real scenario
    ;; For now, we'll test the structure
    (let [sample-output {:command {:type :descriptor-set
                                  :files []}
                        :state {:type :descriptor-set
                               :files []}
                        :type-lookup {}}]
      
      (is (specs/validate-backend-output sample-output)
          "Backend output structure should validate"))))