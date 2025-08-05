(ns proto-explorer.json-to-edn-test
  (:require [clojure.test :refer :all]
            [proto-explorer.json-to-edn :as json-edn]
            [clojure.java.io :as io]))

(deftest test-snake->kebab
  (testing "Basic snake_case to kebab-case conversion"
    (is (= "message-type" (json-edn/snake->kebab "message_type")))
    (is (= "TYPE-STRING" (json-edn/snake->kebab "TYPE_STRING")))
    (is (= "LABEL-OPTIONAL" (json-edn/snake->kebab "LABEL_OPTIONAL"))))
  
  (testing "Edge cases"
    (is (= "test" (json-edn/snake->kebab "_test_")))
    (is (= "test-case" (json-edn/snake->kebab "__test__case__")))
    (is (= "" (json-edn/snake->kebab "___")))
    (is (= "a-b-c" (json-edn/snake->kebab "a_b_c")))))

(deftest test-camel->kebab
  (testing "camelCase to kebab-case"
    (is (= "message-type" (json-edn/camel->kebab "messageType")))
    (is (= "my-long-variable-name" (json-edn/camel->kebab "myLongVariableName"))))
  
  (testing "PascalCase to kebab-case"
    (is (= "message-type" (json-edn/camel->kebab "MessageType")))
    (is (= "jon-gui-data" (json-edn/camel->kebab "JonGuiData"))))
  
  (testing "Mixed case with acronyms"
    (is (= "xml-http-request" (json-edn/camel->kebab "XMLHttpRequest")))
    (is (= "http-api" (json-edn/camel->kebab "HTTPApi")))
    (is (= "io-exception" (json-edn/camel->kebab "IOException")))))

(deftest test-normalize-key
  (testing "Various key formats"
    (is (= :message-type (json-edn/normalize-key "message_type")))
    (is (= :message-type (json-edn/normalize-key "messageType")))
    (is (= :message-type (json-edn/normalize-key "MessageType")))
    (is (= :type-string (json-edn/normalize-key "TYPE_STRING")))))

(deftest test-json-parsing
  (testing "Simple JSON with keywordization"
    (let [json "{\"message_type\":\"Root\",\"field_count\":5}"
          result (json-edn/json->edn json {:convert-values? false})]
      (is (= {:message-type "Root" :field-count 5} result))))
  
  (testing "Nested JSON structures"
    (let [json "{\"message_type\":{\"nested_field\":\"value\",\"otherField\":123}}"
          result (json-edn/json->edn json {:convert-values? false})]
      (is (= {:message-type {:nested-field "value" :other-field 123}} result))))
  
  (testing "Arrays in JSON"
    (let [json "{\"field_list\":[\"field_one\",\"field_two\"]}"
          result (json-edn/json->edn json {:convert-values? false})]
      (is (= {:field-list ["field_one" "field_two"]} result)))))

(deftest test-proto-type-detection
  (testing "Proto type constants"
    (is (json-edn/proto-type-string? "TYPE_STRING"))
    (is (json-edn/proto-type-string? "TYPE_INT32"))
    (is (json-edn/proto-type-string? "TYPE_MESSAGE"))
    (is (not (json-edn/proto-type-string? "type_string")))
    (is (not (json-edn/proto-type-string? "STRING_TYPE")))))
  
  (testing "Proto label constants"
    (is (json-edn/proto-label-string? "LABEL_OPTIONAL"))
    (is (json-edn/proto-label-string? "LABEL_REPEATED"))
    (is (not (json-edn/proto-label-string? "label_optional")))
    (is (not (json-edn/proto-label-string? "OPTIONAL_LABEL"))))

(deftest test-selective-value-conversion
  (testing "Complete JSON descriptor processing"
    (let [json-string "{\"file\":[{
                  \"name\":\"test.proto\",
                  \"package\":\"test_package\",
                  \"message_type\":[{
                    \"name\":\"TestMessage\",
                    \"field\":[{
                      \"name\":\"test_field\",
                      \"number\":1,
                      \"type\":\"TYPE_STRING\",
                      \"label\":\"LABEL_OPTIONAL\"
                    }]
                  }]
                }]}"
          result (json-edn/json->edn json-string)]
      ;; Check structure is keywordized
      (is (map? result))
      (is (contains? result :file))
      (is (vector? (:file result)))
      
      ;; Check nested keys are converted
      (let [file (first (:file result))]
        (is (= :name (first (keys file))))
        (is (contains? file :message-type))
        
        ;; Check field conversion
        (let [msg (first (:message-type file))
              field (first (:field msg))]
          (is (= "test_field" (:name field)))
          (is (= :type-string (:type field)))
          (is (= :label-optional (:label field))))))))

(deftest test-find-utilities
  (let [test-data {:file [{:name "test.proto"
                          :package "test"
                          :message-type [{:name "Root"}]}
                         {:name "cmd.proto"
                          :package "cmd"
                          :message-type [{:name "Ping"}]}]}]
    
    (testing "find-file-descriptor"
      (let [found (json-edn/find-file-descriptor test-data #"cmd")]
        (is (= 1 (count found)))
        (is (= "cmd.proto" (:name (first found))))))
    
    (testing "find-message"
      (let [found (json-edn/find-message test-data "Ping")]
        (is (= 1 (count found)))
        (is (= "cmd" (:package (first found))))
        (is (= "cmd.Ping" (:full-name (first found))))))))

;; Integration test with a more realistic protobuf descriptor structure
(deftest test-realistic-descriptor
  (let [json "{
    \"file\": [{
      \"name\": \"jon_shared_cmd.proto\",
      \"package\": \"cmd\",
      \"dependency\": [\"buf/validate/validate.proto\"],
      \"message_type\": [{
        \"name\": \"Root\",
        \"field\": [{
          \"name\": \"protocol_version\",
          \"number\": 1,
          \"label\": \"LABEL_OPTIONAL\",
          \"type\": \"TYPE_INT32\",
          \"json_name\": \"protocolVersion\"
        }, {
          \"name\": \"ping\",
          \"number\": 2,
          \"label\": \"LABEL_OPTIONAL\",
          \"type\": \"TYPE_MESSAGE\",
          \"type_name\": \".cmd.Ping\",
          \"oneof_index\": 0,
          \"json_name\": \"ping\"
        }],
        \"oneof_decl\": [{
          \"name\": \"cmd\"
        }]
      }, {
        \"name\": \"Ping\",
        \"field\": []
      }]
    }]
  }"
        result (json-edn/json->edn json)]
    
    ;; Verify overall structure
    (is (map? result))
    (is (vector? (:file result)))
    
    ;; Check file-level conversion
    (let [file (first (:file result))]
      (is (= "jon_shared_cmd.proto" (:name file)))
      (is (= "cmd" (:package file)))
      (is (vector? (:dependency file)))
      
      ;; Check message conversion
      (let [root-msg (first (:message-type file))]
        (is (= "Root" (:name root-msg)))
        (is (= 2 (count (:field root-msg))))
        
        ;; Check field details
        (let [version-field (first (:field root-msg))
              ping-field (second (:field root-msg))]
          ;; Protocol version field
          (is (= "protocol_version" (:name version-field)))
          (is (= :type-int32 (:type version-field)))
          (is (= :label-optional (:label version-field)))
          (is (= "protocolVersion" (:json-name version-field)))
          
          ;; Ping field (part of oneof)
          (is (= "ping" (:name ping-field)))
          (is (= :type-message (:type ping-field)))
          (is (= ".cmd.Ping" (:type-name ping-field)))
          (is (= 0 (:oneof-index ping-field)))
          
          ;; Oneof declaration
          (let [oneof (first (:oneof-decl root-msg))]
            (is (= "cmd" (:name oneof)))))))))

(deftest test-save-edn
  (testing "Save EDN to file"
    (let [temp-file (java.io.File/createTempFile "test" ".edn")
          test-data {:test "data" :nested {:key "value"}}]
      (try
        (json-edn/save-edn test-data (.getAbsolutePath temp-file))
        (let [loaded (read-string (slurp temp-file))]
          (is (= test-data loaded)))
        (finally
          (.delete temp-file))))))

(deftest test-load-json-descriptor
  (testing "Load JSON from file"
    (let [json-content "{\"file\":[{\"name\":\"test.proto\"}]}"
          temp-file (java.io.File/createTempFile "test" ".json")]
      (try
        (spit temp-file json-content)
        (let [result (json-edn/load-json-descriptor (.getPath temp-file))]
          (is (map? result))
          (is (contains? result :file))
          (is (= "test.proto" (get-in result [:file 0 :name]))))
        (finally
          (.delete temp-file))))))

(deftest test-json->edn-validates-input
  (testing "json->edn rejects non-string input"
    (is (thrown? IllegalArgumentException
                 (json-edn/json->edn {:not "a string"})))
    (is (thrown? IllegalArgumentException
                 (json-edn/json->edn 123)))
    (is (thrown? IllegalArgumentException
                 (json-edn/json->edn nil)))))