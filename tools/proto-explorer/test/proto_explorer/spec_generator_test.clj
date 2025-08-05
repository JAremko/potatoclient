(ns proto-explorer.spec-generator-test
  (:require [clojure.test :refer :all]
            [proto-explorer.spec-generator :as spec-gen]
            [proto-explorer.json-to-edn :as json-edn]))

(deftest test-type-mapping
  (testing "Proto type to Malli mapping"
    (is (= :string (spec-gen/proto-type->malli :type-string)))
    (is (= :int (spec-gen/proto-type->malli :type-int32)))
    (is (= :boolean (spec-gen/proto-type->malli :type-bool)))
    (is (= :double (spec-gen/proto-type->malli :type-double)))
    (is (= :double (spec-gen/proto-type->malli :type-float)))))  ; Malli uses :double for all floats

(deftest test-resolve-type-name
  (testing "Type name resolution"
    (is (= :cmd/root (spec-gen/resolve-type-name ".cmd.Root")))
    (is (= :ser/jon-gui-data (spec-gen/resolve-type-name ".ser.JonGuiData")))
    (is (= :test/test-message (spec-gen/resolve-type-name ".test.TestMessage")))
    (is (nil? (spec-gen/resolve-type-name nil)))
    (is (nil? (spec-gen/resolve-type-name "")))
    (is (nil? (spec-gen/resolve-type-name "InvalidType")))))

(deftest test-process-field
  (testing "Primitive field processing"
    (is (= :string (spec-gen/process-field {:type :type-string} {})))
    (is (= :int (spec-gen/process-field {:type :type-int32} {})))
    (is (= :boolean (spec-gen/process-field {:type :type-bool} {}))))
  
  (testing "Message field processing"
    (is (= :cmd/ping 
           (spec-gen/process-field {:type :type-message
                                   :type-name ".cmd.Ping"} {})))
    (is (= :map 
           (spec-gen/process-field {:type :type-message
                                   :type-name nil} {}))))
  
  (testing "Enum field processing"
    (is (= :test/status
           (spec-gen/process-field {:type :type-enum
                                   :type-name ".test.Status"} {})))))

(deftest test-apply-field-label
  (testing "Field label application"
    (is (= [:vector :string]
           (spec-gen/apply-field-label :string {:label :label-repeated})))
    (is (= [:maybe :int]
           (spec-gen/apply-field-label :int {:label :label-optional})))
    (is (= :boolean
           (spec-gen/apply-field-label :boolean {})))))

(deftest test-process-enum
  (testing "Enum processing"
    (let [enum {:name "Status"
                :value [{:name "STATUS_UNKNOWN" :number 0}
                        {:name "STATUS_ACTIVE" :number 1}
                        {:name "STATUS_INACTIVE" :number 2}]}
          result (spec-gen/process-enum enum {})]
      (is (= {:status [:enum :status-unknown :status-active :status-inactive]}
             result)))))

(deftest test-simple-message-processing
  (testing "Simple message with basic fields"
    (let [message {:name "SimpleMessage"
                   :field [{:name "id" :type :type-string :number 1}
                          {:name "count" :type :type-int32 :number 2}
                          {:name "active" :type :type-bool :number 3}]}
          result (spec-gen/process-message message {})]
      (is (= {:simple-message 
              [:map 
               [:id :string]
               [:count :int]
               [:active :boolean]]}
             result)))))

(deftest test-message-with-repeated-fields
  (testing "Message with repeated fields"
    (let [message {:name "ListMessage"
                   :field [{:name "items" 
                           :type :type-string 
                           :number 1
                           :label :label-repeated}]}
          result (spec-gen/process-message message {})]
      (is (= {:list-message 
              [:map 
               [:items [:vector :string]]]}
             result)))))

(deftest test-message-with-optional-fields
  (testing "Message with optional fields"
    (let [message {:name "OptionalMessage"
                   :field [{:name "required_field" 
                           :type :type-string 
                           :number 1}
                          {:name "optional_field" 
                           :type :type-string 
                           :number 2
                           :label :label-optional}]}
          result (spec-gen/process-message message {})]
      (is (= {:optional-message 
              [:map 
               [:required-field :string]
               [:optional-field [:maybe :string]]]}
             result)))))

(deftest test-message-with-oneof
  (testing "Message with oneof field"
    (let [message {:name "Command"
                   :field [{:name "version" :type :type-int32 :number 1}
                          {:name "create" :type :type-message 
                           :type-name ".test.Create" :number 2
                           :oneof-index 0}
                          {:name "update" :type :type-message
                           :type-name ".test.Update" :number 3
                           :oneof-index 0}]
                   :oneof-decl [{:name "cmd"}]}
          context {:current-message message}
          result (spec-gen/process-message message context)]
      ;; The result should have a oneof schema
      (is (contains? result :command))
      (let [schema (get result :command)]
        (is (vector? schema))
        (is (= :map (first schema)))
        ;; Check the schema structure - should be [:map [:version :int] [:cmd oneof-spec]]
        (let [fields (rest schema)
              version-field (first fields)
              cmd-field (second fields)]
          (is (= [:version :int] version-field))
          (is (= :cmd (first cmd-field)))
          (is (= :oneof (first (second cmd-field)))))))))

(deftest test-namespace-generation
  (testing "Namespace declaration generation"
    (let [ns-decl (spec-gen/generate-namespace-declaration "cmd")]
      (is (seq? ns-decl))
      (is (= 'ns (first ns-decl)))
      (is (symbol? (second ns-decl)))
      (is (= "potatoclient.specs.cmd" (str (second ns-decl)))))))

(deftest test-realistic-descriptor
  (testing "Processing a realistic descriptor structure"
    (let [descriptor {:file [{:name "test.proto"
                             :package "test"
                             :message-type [{:name "TestMessage"
                                           :field [{:name "id" 
                                                   :type :type-string 
                                                   :number 1}
                                                  {:name "data" 
                                                   :type :type-message
                                                   :type-name ".test.Data"
                                                   :number 2}]}
                                          {:name "Data"
                                           :field [{:name "value" 
                                                   :type :type-int32 
                                                   :number 1}]}]
                             :enum-type [{:name "Status"
                                        :value [{:name "ACTIVE" :number 1}
                                               {:name "INACTIVE" :number 2}]}]}]}
          specs (spec-gen/generate-specs descriptor)]
      
      ;; Check that we got specs for the test package
      (is (contains? specs "test"))
      
      (let [test-specs (get specs "test")]
        ;; Should have both messages and the enum
        (is (contains? test-specs :test-message))
        (is (contains? test-specs :data))
        (is (contains? test-specs :status))
        
        ;; Check message structure
        (let [test-msg (get test-specs :test-message)]
          (is (= :map (first test-msg)))
          (is (= [:id :string] (second test-msg)))
          (is (= [:data :test/data] (nth test-msg 2))))
        
        ;; Check enum structure
        (let [status-enum (get test-specs :status)]
          (is (= :enum (first status-enum)))
          (is (contains? (set (rest status-enum)) :active))
          (is (contains? (set (rest status-enum)) :inactive)))))))

(deftest test-spec-file-generation
  (testing "Generate spec file content"
    (let [schemas {:root [:map [:version :int]]
                   :ping [:map]}
          content (spec-gen/generate-spec-file "cmd" schemas)]
      (is (string? content))
      (is (re-find #"ns potatoclient.specs.cmd" content))
      (is (re-find #"def root" content))
      (is (re-find #"def ping" content))
      (is (re-find #":require \[malli.core" content)))))