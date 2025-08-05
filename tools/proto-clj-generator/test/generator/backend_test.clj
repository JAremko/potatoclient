(ns generator.backend-test
  (:require [clojure.test :refer :all]
            [generator.backend :as backend]
            [com.rpl.specter :as sp]))

(deftest keywordize-value-test
  (testing "Convert protobuf constants to keywords"
    (is (= :label-optional (backend/keywordize-value "LABEL_OPTIONAL")))
    (is (= :type-message (backend/keywordize-value "TYPE_MESSAGE")))
    (is (= :foo (backend/keywordize-value :foo)))
    (is (= "lowercase" (backend/keywordize-value "lowercase")))
    (is (= 123 (backend/keywordize-value 123)))))

(deftest field->edn-test
  (testing "Convert protobuf field to EDN"
    (testing "Scalar field"
      (let [field {:name "protocol_version"
                   :number 1
                   :label "LABEL_OPTIONAL"
                   :type :int32}
            result (backend/field->edn field)]
        (is (= :protocol-version (:name result)))
        (is (= "protocol_version" (:proto-name result)))
        (is (= 1 (:number result)))
        (is (= {:scalar :int32} (:type result)))
        (is (= :label-optional (:label result)))
        (is (true? (:optional? result)))))
    
    (testing "Message field with oneof"
      (let [field {:name "ping"
                   :number 2
                   :label "LABEL_OPTIONAL"
                   :type :message
                   :type-name ".cmd.System.Ping"
                   :oneof-index 0}
            result (backend/field->edn field)]
        (is (= :ping (:name result)))
        (is (= {:message {:type-ref ".cmd.System.Ping"}} (:type result)))
        (is (= 0 (:oneof-index result)))))
    
    (testing "Enum field"
      (let [field {:name "mode"
                   :number 3
                   :label "LABEL_OPTIONAL"
                   :type :enum
                   :type-name ".cmd.FocusMode"}
            result (backend/field->edn field)]
        (is (= :mode (:name result)))
        (is (= {:enum {:type-ref ".cmd.FocusMode"}} (:type result)))))))

(deftest generate-java-class-name-test
  (testing "Generate Java class names"
    (let [context {:package "cmd"
                   :java-package nil
                   :java-outer-classname "JonSharedCmd"}]
      (is (= "cmd.JonSharedCmd$Root" 
             (backend/generate-java-class-name context "Root" [])))
      (is (= "cmd.JonSharedCmd$System$Ping" 
             (backend/generate-java-class-name context "Ping" ["System"]))))
    
    (testing "With java-package override"
      (let [context {:package "foo"
                     :java-package "com.example"
                     :java-outer-classname "FooProto"}]
        (is (= "com.example.FooProto$Message"
               (backend/generate-java-class-name context "Message" [])))))))

(deftest message->edn-test
  (testing "Convert message to EDN"
    (let [message {:name "Root"
                   :field [{:name "protocol_version"
                           :number 1
                           :label "LABEL_OPTIONAL"
                           :type :int32}
                          {:name "ping"
                           :number 2
                           :label "LABEL_OPTIONAL"
                           :type :message
                           :type-name ".cmd.System.Ping"
                           :oneof-index 0}]
                   :oneof-decl [{:name "payload"}]
                   :nested-type [{:name "Ping"
                                 :field [{:name "timestamp"
                                         :number 1
                                         :type :int64}]}]}
          context {:package "cmd"
                   :java-package nil
                   :java-outer-classname "JonSharedCmd"}
          result (backend/message->edn message context [])]
      
      (is (= :message (:type result)))
      (is (= :root (:name result)))
      (is (= "Root" (:proto-name result)))
      (is (= "cmd.JonSharedCmd$Root" (:java-class result)))
      
      ;; Check fields
      (is (= 1 (count (:fields result))))
      (is (= :protocol-version (-> result :fields first :name)))
      
      ;; Check oneofs
      (is (= 1 (count (:oneofs result))))
      (is (= :payload (-> result :oneofs first :name)))
      (is (= 1 (count (-> result :oneofs first :fields))))
      
      ;; Check nested types
      (is (= 1 (count (:nested-types result))))
      (is (= :ping (-> result :nested-types first :name)))
      (is (= "cmd.JonSharedCmd$Root$Ping" 
             (-> result :nested-types first :java-class))))))

(deftest enum->edn-test
  (testing "Convert enum to EDN"
    (let [enum {:name "FocusMode"
                :value [{:name "FOCUS_MODE_UNKNOWN" :number 0}
                       {:name "FOCUS_MODE_AUTO" :number 1}
                       {:name "FOCUS_MODE_MANUAL" :number 2}]}
          context {:package "cmd"
                   :java-package nil
                   :java-outer-classname "JonSharedCmd"}
          result (backend/enum->edn enum context [])]
      
      (is (= :enum (:type result)))
      (is (= :focus-mode (:name result)))
      (is (= "FocusMode" (:proto-name result)))
      (is (= "cmd.JonSharedCmd$FocusMode" (:java-class result)))
      
      (is (= 3 (count (:values result))))
      (is (= #{:focus-mode-unknown :focus-mode-auto :focus-mode-manual}
             (set (map :name (:values result))))))))

(deftest get-canonical-type-ref-test
  (testing "Get canonical type reference"
    (let [type-def {:proto-name "Ping"}
          file-context {:package "cmd"}]
      (is (= "cmd.Ping" 
             (backend/get-canonical-type-ref type-def file-context [])))
      (is (= "cmd.System.Ping"
             (backend/get-canonical-type-ref type-def file-context ["System"])))
      (is (= "cmd.Root.System.Ping"
             (backend/get-canonical-type-ref type-def file-context ["Root" "System"]))))))

(deftest collect-all-types-test
  (testing "Collect all types with canonical references"
    (let [edn-data {:type :descriptor-set
                    :files [{:package "cmd"
                            :messages [{:name :root
                                       :proto-name "Root"
                                       :nested-types [{:name :ping
                                                      :proto-name "Ping"
                                                      :nested-types []}]}]
                            :enums [{:name :focus-mode
                                    :proto-name "FocusMode"}]}]}
          result (backend/collect-all-types edn-data)]
      
      ;; Should have [canonical-ref type-def] pairs
      (is (seq? result))
      (is (every? #(= 2 (count %)) result))
      
      ;; Check we have the right types
      (let [lookup (into {} result)]
        (is (contains? lookup "cmd.Root"))
        (is (contains? lookup "cmd.Root.Ping"))
        (is (contains? lookup "cmd.FocusMode"))
        (is (= :root (:name (get lookup "cmd.Root"))))
        (is (= :ping (:name (get lookup "cmd.Root.Ping"))))
        (is (= :focus-mode (:name (get lookup "cmd.FocusMode"))))))))

(deftest process-json-value-test
  (testing "Process JSON values converting constants to keywords"
    (is (= {:label :label-optional
            :type :type-message}
           (backend/process-json-value {:label "LABEL_OPTIONAL"
                                       :type "TYPE_MESSAGE"})))
    
    (is (= {:fields [{:label :repeated}]
            :name "test"}
           (backend/process-json-value {:fields [{:label "REPEATED"}]
                                       :name "test"})))
    
    (is (= {:nested {:deeply {:label :optional}}}
           (backend/process-json-value {:nested {:deeply {:label "OPTIONAL"}}})))))

(deftest file->edn-test
  (testing "Convert file descriptor to EDN"
    (let [file {:name "test.proto"
                :package "test"
                :options {:java-outer-classname "TestProto"
                         :java-package "com.example.test"}
                :message-type [{:name "Message"
                               :field [{:name "id" :number 1 :type :int32}]}]
                :enum-type [{:name "Status"
                           :value [{:name "UNKNOWN" :number 0}]}]}
          result (backend/file->edn file)]
      
      (is (= :file (:type result)))
      (is (= "test.proto" (:name result)))
      (is (= "test" (:package result)))
      (is (= "com.example.test" (:java-package result)))
      (is (= "TestProto" (:java-outer-classname result)))
      
      (is (= 1 (count (:messages result))))
      (is (= :message (-> result :messages first :name)))
      
      (is (= 1 (count (:enums result))))
      (is (= :status (-> result :enums first :name))))))