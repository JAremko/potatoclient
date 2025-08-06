(ns generator.repeated-field-test
  "Integration test for repeated field generation."
  (:require [clojure.test :refer [deftest testing is]]
            [generator.backend :as backend]
            [generator.frontend :as frontend]
            [clojure.string :as str]))

(def test-descriptor
  {:name "test.proto"
   :package "test"
   :options {:javaPackage "com.test"
            :javaOuterClassname "TestProto"}
   :messageType [{:name "RepeatedFieldsMessage"
                  :field [{:name "tags"
                          :number 1
                          :type "TYPE_STRING"
                          :label "LABEL_REPEATED"}
                         {:name "numbers"
                          :number 2
                          :type "TYPE_INT32"
                          :label "LABEL_REPEATED"}
                         {:name "messages"
                          :number 3
                          :type "TYPE_MESSAGE"
                          :typeName ".test.SubMessage"
                          :label "LABEL_REPEATED"}
                         {:name "name"
                          :number 4
                          :type "TYPE_STRING"
                          :label "LABEL_OPTIONAL"}]}
                 {:name "SubMessage"
                  :field [{:name "value"
                          :number 1
                          :type "TYPE_STRING"
                          :label "LABEL_OPTIONAL"}]}]})

(deftest repeated-fields-backend-test
  (testing "Backend correctly marks repeated fields"
    (let [result (backend/file->edn test-descriptor)]
      
      ;; Find the RepeatedFieldsMessage
      (let [msg (first (filter #(= :repeated-fields-message (:name %)) (:messages result)))]
        (is (some? msg))
        
        ;; Check repeated fields
        (let [tags-field (first (filter #(= :tags (:name %)) (:fields msg)))
              numbers-field (first (filter #(= :numbers (:name %)) (:fields msg)))
              messages-field (first (filter #(= :messages (:name %)) (:fields msg)))
              name-field (first (filter #(= :name (:name %)) (:fields msg)))]
          
          (is (:repeated? tags-field))
          (is (= :label-repeated (:label tags-field)))
          
          (is (:repeated? numbers-field))
          (is (= :label-repeated (:label numbers-field)))
          
          (is (:repeated? messages-field))
          (is (= :label-repeated (:label messages-field)))
          
          (is (not (:repeated? name-field)))
          (is (= :label-optional (:label name-field))))))))

(deftest repeated-fields-frontend-test
  (testing "Frontend generates correct code for repeated fields"
    (let [msg {:type :message
               :name :repeated-fields-message
               :proto-name "RepeatedFieldsMessage"
               :java-class "com.test.TestProto$RepeatedFieldsMessage"
               :fields [{:name :tags
                        :proto-name "tags"
                        :number 1
                        :type {:unknown {:proto-type :type-string}}
                        :label :label-repeated
                        :repeated? true}
                       {:name :numbers
                        :proto-name "numbers"
                        :number 2
                        :type {:scalar :type-int32}
                        :label :label-repeated
                        :repeated? true}
                       {:name :name
                        :proto-name "name"
                        :number 4
                        :type {:unknown {:proto-type :type-string}}
                        :label :label-optional}]
               :oneofs []}
          
          builder-code (frontend/generate-builder msg {} "test")
          parser-code (frontend/generate-parser msg {} "test")]
      
      ;; Check builder
      (testing "Builder uses addAll for repeated fields"
        (is (str/includes? builder-code ".addAllTags"))
        (is (str/includes? builder-code ".addAllNumbers"))
        (is (str/includes? builder-code ".setName"))
        (is (not (str/includes? builder-code ".addAllName"))))
      
      ;; Check parser
      (testing "Parser uses getList for repeated fields"
        (is (str/includes? parser-code ".getTagsList"))
        (is (str/includes? parser-code ".getNumbersList"))
        (is (str/includes? parser-code "(vec"))
        ;; We now always include all fields, not using .hasName checks
        (is (str/includes? parser-code "true (assoc :name (.getName proto))"))
        (is (str/includes? parser-code ".getName"))))))

(deftest end-to-end-repeated-fields-test
  (testing "End-to-end generation handles repeated fields correctly"
    (let [result (backend/file->edn test-descriptor)
          ;; Manually construct backend output format  
          backend-output {:command {:files [result]}
                         :state {:files []}
                         :type-lookup (backend/build-type-lookup {:files [result]})}
          frontend-output (frontend/generate-from-backend backend-output "test.proto")
          command-code (:command frontend-output)]
      
      ;; Basic structure check
      (is (str/includes? command-code "build-repeated-fields-message"))
      (is (str/includes? command-code "parse-repeated-fields-message"))
      
      ;; Builder checks
      (is (str/includes? command-code "when (contains? m :tags)"))
      (is (str/includes? command-code ".addAllTags builder (get m :tags)"))
      
      ;; Parser checks  
      (is (str/includes? command-code "true (assoc :tags (vec (.getTagsList proto)))"))
      (is (str/includes? command-code "true (assoc :numbers (vec (.getNumbersList proto)))"))
      ;; We now always include all fields, not conditional on hasName
      (is (str/includes? command-code "true (assoc :name (.getName proto))")))))