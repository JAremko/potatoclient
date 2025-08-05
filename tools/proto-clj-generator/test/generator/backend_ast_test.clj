(ns generator.backend-ast-test
  "Comprehensive AST tests for backend to catch enum and other structural problems."
  (:require [clojure.test :refer :all]
            [generator.backend :as backend]
            [com.rpl.specter :as sp]
            [cheshire.core :as json]))

;; =============================================================================
;; Test Data
;; =============================================================================

(def sample-enum-json
  {:name "Edition"
   :value [{:name "EDITION_UNKNOWN" :number 0}
           {:name "EDITION_LEGACY" :number 998}
           {:name "EDITION_PROTO2" :number 999}]})

(def sample-message-with-enum-json
  {:name "FeatureSet"
   :field [{:name "field_presence"
           :number 1
           :label "LABEL_OPTIONAL"
           :type "TYPE_ENUM"
           :typeName ".google.protobuf.FeatureSet.FieldPresence"
           :oneofIndex 0}
           {:name "enum_type"
           :number 2
           :label "LABEL_OPTIONAL"
           :type "TYPE_ENUM"
           :typeName ".google.protobuf.EnumType"}]
   :enumType [{:name "FieldPresence"
              :value [{:name "FIELD_PRESENCE_UNKNOWN" :number 0}
                     {:name "EXPLICIT" :number 1}
                     {:name "IMPLICIT" :number 2}
                     {:name "LEGACY_REQUIRED" :number 3}]}]
   :oneofDecl [{:name "presence"}]})

(def sample-file-json
  {:name "test.proto"
   :package "test"
   :messageType [sample-message-with-enum-json]
   :enumType [sample-enum-json]
   :options {:javaPackage "com.test"
            :javaOuterClassname "TestProto"}})

;; =============================================================================
;; AST Structure Tests
;; =============================================================================

(deftest enum-ast-structure-test
  (testing "Enum AST structure is correct"
    (let [context {:package "test"
                   :java-package "com.test"
                   :java-outer-classname "TestProto"}
          result (backend/enum->edn sample-enum-json context [])]
      
      (testing "Basic enum structure"
        (is (= :enum (:type result)))
        (is (= :edition (:name result)))
        (is (= "Edition" (:proto-name result)))
        (is (= "com.test.TestProto$Edition" (:java-class result))))
      
      (testing "Enum values structure"
        (is (= 3 (count (:values result))))
        (let [values (:values result)]
          (doseq [value values]
            (is (keyword? (:name value)) "Enum value name should be keyword")
            (is (string? (:proto-name value)) "Proto name should be string")
            (is (number? (:number value)) "Number should be numeric"))))
      
      (testing "Enum value name conversion"
        (let [value-names (map :name (:values result))]
          (is (= #{:edition-unknown :edition-legacy :edition-proto2}
                 (set value-names)))
          ;; Proto names should remain unchanged
          (let [proto-names (map :proto-name (:values result))]
            (is (= #{"EDITION_UNKNOWN" "EDITION_LEGACY" "EDITION_PROTO2"}
                   (set proto-names)))))))))

(deftest nested-enum-ast-structure-test
  (testing "Nested enum AST structure is correct"
    (let [context {:package "test"
                   :java-package "com.test"
                   :java-outer-classname "TestProto"}
          message-result (backend/message->edn sample-message-with-enum-json context [])]
      
      (testing "Message contains nested enum"
        (is (= 1 (count (:nested-types message-result))))
        (let [nested-enum (first (:nested-types message-result))]
          (is (= :enum (:type nested-enum)))
          (is (= :field-presence (:name nested-enum)))
          (is (= "FieldPresence" (:proto-name nested-enum)))
          (is (= "com.test.TestProto$FeatureSet$FieldPresence" 
                 (:java-class nested-enum)))))
      
      (testing "Nested enum values"
        (let [nested-enum (first (:nested-types message-result))
              values (:values nested-enum)]
          (is (= 4 (count values)))
          (is (= #{:field-presence-unknown :explicit :implicit :legacy-required}
                 (set (map :name values)))))))))

(deftest enum-field-reference-test
  (testing "Enum field references are correctly processed"
    (let [context {:package "test"
                   :java-package "com.test"
                   :java-outer-classname "TestProto"}
          message-result (backend/message->edn sample-message-with-enum-json context [])]
      
      (testing "Enum field type references"
        (let [fields (:fields message-result)]
          (is (= 1 (count fields)) "Should only have non-oneof fields")
          (let [enum-field (first fields)]
            (is (= :enum-type (:name enum-field)))
            (is (= {:enum {:type-ref ".google.protobuf.EnumType"}} 
                   (:type enum-field)))))
        
        (let [oneof-fields (-> message-result :oneofs first :fields)]
          (is (= 1 (count oneof-fields)))
          (let [oneof-enum-field (first oneof-fields)]
            (is (= :field-presence (:name oneof-enum-field)))
            (is (= {:enum {:type-ref ".google.protobuf.FeatureSet.FieldPresence"}}
                   (:type oneof-enum-field)))))))))

(deftest enum-java-class-generation-test
  (testing "Java class names for enums are correctly generated"
    (testing "Top-level enum"
      (let [context {:package "cmd"
                     :java-package nil
                     :java-outer-classname "CmdProto"}]
        (is (= "cmd.CmdProto$MyEnum"
               (backend/generate-java-class-name context "MyEnum" [])))))
    
    (testing "Nested enum in message"
      (let [context {:package "cmd"
                     :java-package "com.example.cmd"
                     :java-outer-classname "CmdProto"}]
        (is (= "com.example.cmd.CmdProto$Message$Status"
               (backend/generate-java-class-name context "Status" ["Message"])))))
    
    (testing "Deeply nested enum"
      (let [context {:package "test"
                     :java-package "org.test"
                     :java-outer-classname "Test"}]
        (is (= "org.test.Test$Outer$Inner$DeepEnum"
               (backend/generate-java-class-name context "DeepEnum" ["Outer" "Inner"])))))))

(deftest enum-canonical-reference-test
  (testing "Canonical references for enums are correct"
    (let [edn-data {:type :descriptor-set
                    :files [{:package "test"
                            :messages [{:name :message
                                       :proto-name "Message"
                                       :nested-types [{:type :enum
                                                      :name :status
                                                      :proto-name "Status"}]}]
                            :enums [{:type :enum
                                    :name :global-enum
                                    :proto-name "GlobalEnum"}]}]}
          type-pairs (backend/collect-all-types edn-data)
          type-lookup (into {} type-pairs)]
      
      (testing "Global enum reference"
        (is (contains? type-lookup "test.GlobalEnum"))
        (is (= :global-enum (:name (get type-lookup "test.GlobalEnum")))))
      
      (testing "Nested enum reference"
        (is (contains? type-lookup "test.Message.Status"))
        (is (= :status (:name (get type-lookup "test.Message.Status"))))))))

(deftest enum-value-keyword-conversion-test
  (testing "Enum value names are correctly converted to keywords"
    (let [test-cases [["UNKNOWN" :unknown]
                      ["FIELD_PRESENCE_UNKNOWN" :field-presence-unknown]
                      ["EDITION_2023" :edition-2023]
                      ["LABEL_OPTIONAL" :label-optional]
                      ["TYPE_MESSAGE" :type-message]]]
      (doseq [[input expected] test-cases]
        (is (= expected (backend/keywordize-value input))
            (str "Failed to convert " input " to " expected))))))

(deftest comprehensive-ast-validation-test
  (testing "Complete AST validation for a complex file"
    (let [complex-file {:name "complex.proto"
                       :package "complex"
                       :messageType [{:name "Request"
                                     :field [{:name "method"
                                             :number 1
                                             :type "TYPE_ENUM"
                                             :typeName ".complex.Method"}
                                            {:name "status"
                                             :number 2
                                             :type "TYPE_ENUM"
                                             :typeName ".complex.Request.Status"
                                             :oneofIndex 0}]
                                     :enumType [{:name "Status"
                                               :value [{:name "STATUS_OK" :number 0}
                                                      {:name "STATUS_ERROR" :number 1}]}]
                                     :oneofDecl [{:name "result"}]}]
                       :enumType [{:name "Method"
                                  :value [{:name "GET" :number 0}
                                         {:name "POST" :number 1}
                                         {:name "PUT" :number 2}
                                         {:name "DELETE" :number 3}]}]}
          result (backend/file->edn complex-file)]
      
      (testing "File structure"
        (is (= :file (:type result)))
        (is (= 1 (count (:messages result))))
        (is (= 1 (count (:enums result)))))
      
      (testing "Top-level enum"
        (let [method-enum (first (:enums result))]
          (is (= :method (:name method-enum)))
          (is (= 4 (count (:values method-enum))))
          (is (= #{:get :post :put :delete}
                 (set (map :name (:values method-enum)))))))
      
      (testing "Nested enum in message"
        (let [message (first (:messages result))
              nested-enum (first (:nested-types message))]
          (is (= :status (:name nested-enum)))
          (is (= 2 (count (:values nested-enum))))
          (is (= #{:status-ok :status-error}
                 (set (map :name (:values nested-enum)))))))
      
      (testing "Field references to enums"
        (let [message (first (:messages result))
              method-field (first (:fields message))]
          (is (= :method (:name method-field)))
          (is (= {:enum {:type-ref ".complex.Method"}} (:type method-field))))
        
        (let [message (first (:messages result))
              status-field (-> message :oneofs first :fields first)]
          (is (= :status (:name status-field)))
          (is (= {:enum {:type-ref ".complex.Request.Status"}} 
                 (:type status-field))))))))

(deftest specter-path-validation-test
  (testing "Specter paths correctly navigate enum structures"
    (let [edn-data {:type :descriptor-set
                    :files [{:type :file
                            :enums [{:type :enum
                                    :name :test-enum
                                    :values [{:name :value1 :number 0}
                                            {:name :value2 :number 1}]}]
                            :messages [{:type :message
                                       :name :test-message
                                       :nested-types [{:type :enum
                                                      :name :nested-enum
                                                      :values [{:name :nested1 :number 0}]}]}]}]}]
      
      (testing "Select all enums (top-level and nested)"
        (let [all-enums (sp/select [:files sp/ALL 
                                   (sp/multi-path 
                                    [:enums sp/ALL]
                                    [:messages sp/ALL :nested-types sp/ALL #(= :enum (:type %))])]
                                  edn-data)]
          (is (= 2 (count all-enums)))
          (is (= #{:test-enum :nested-enum}
                 (set (map :name all-enums))))))
      
      (testing "Select all enum values"
        (let [all-values (sp/select [:files sp/ALL 
                                   (sp/multi-path :enums :messages)
                                   sp/ALL
                                   (sp/if-path [:type #(= :enum %)]
                                             [:values sp/ALL]
                                             [:nested-types sp/ALL #(= :enum (:type %)) :values sp/ALL])]
                                  edn-data)]
          (is (= 3 (count all-values)))
          (is (= #{:value1 :value2 :nested1}
                 (set (map :name all-values)))))))))

(deftest json-parsing-enum-test
  (testing "JSON parsing correctly handles enum fields"
    (let [json-str (json/generate-string sample-file-json)
          parsed (json/parse-string json-str true)
          result (backend/file->edn parsed)]
      
      (testing "Enums parsed from JSON"
        (is (= 1 (count (:enums result))))
        (let [enum (first (:enums result))]
          (is (= :edition (:name enum)))
          (is (= 3 (count (:values enum))))))
      
      (testing "Nested enums parsed from JSON"
        (is (= 1 (count (:messages result))))
        (let [message (first (:messages result))
              nested-types (:nested-types message)]
          (is (= 1 (count nested-types)))
          (is (= :enum (:type (first nested-types)))))))))

(deftest enum-in-type-lookup-test
  (testing "Type lookup correctly includes all enums"
    (let [edn-data {:type :descriptor-set
                    :files [(backend/file->edn sample-file-json)]}
          type-pairs (backend/collect-all-types edn-data)
          type-lookup (into {} type-pairs)]
      
      (testing "Top-level enum in lookup"
        (is (contains? type-lookup "test.Edition"))
        (let [enum (get type-lookup "test.Edition")]
          (is (= :enum (:type enum)))
          (is (= :edition (:name enum)))))
      
      (testing "Nested enum in lookup"
        (is (contains? type-lookup "test.FeatureSet.FieldPresence"))
        (let [enum (get type-lookup "test.FeatureSet.FieldPresence")]
          (is (= :enum (:type enum)))
          (is (= :field-presence (:name enum)))))
      
      (testing "Message also in lookup"
        (is (contains? type-lookup "test.FeatureSet"))
        (let [msg (get type-lookup "test.FeatureSet")]
          (is (= :message (:type msg)))
          (is (= :feature-set (:name msg))))))))