(ns generator.enriched-ir-test
  (:require [clojure.test :refer [deftest is testing]]
            [generator.deps :as deps]
            [generator.specs :as specs]
            [malli.core :as m]))

(deftest test-enrich-descriptor-set
  (testing "Full enrichment pipeline"
    (let [descriptor {:type :descriptor-set
                      :files [{:type :file
                               :name "common.proto"
                               :package "common"
                               :dependencies []
                               :enums [{:type :enum
                                        :proto-name "Status"
                                        :name :status
                                        :values [{:name :ok :proto-name "OK" :number 0}
                                                 {:name :error :proto-name "ERROR" :number 1}]}]
                               :messages []}
                              {:type :file
                               :name "service.proto"
                               :package "service"
                               :dependencies ["common.proto"]
                               :enums []
                               :messages [{:type :message
                                           :proto-name "Request"
                                           :name :request
                                           :fields [{:name :id
                                                     :proto-name "id"
                                                     :number 1
                                                     :label :label-optional
                                                     :type {:scalar :string}}
                                                    {:name :status
                                                     :proto-name "status"
                                                     :number 2
                                                     :label :label-optional
                                                     :type {:enum {:type-ref ".common.Status"}}}]}]}]}
          enriched (deps/enrich-descriptor-set descriptor)]
      
      ;; Check basic structure
      (is (= :combined (:type enriched)))
      (is (= 2 (count (:files enriched))))
      
      ;; Check dependency graph
      (is (contains? enriched :dependency-graph))
      (is (= #{"common.proto" "service.proto"} 
             (-> enriched :dependency-graph :nodes)))
      (is (= {"service.proto" #{"common.proto"}}
             (-> enriched :dependency-graph :edges)))
      
      ;; Check sorted files
      (is (= ["common.proto" "service.proto"] (:sorted-files enriched)))
      
      ;; Check symbol registry
      (is (contains? (:symbol-registry enriched) ".common.Status"))
      (is (contains? (:symbol-registry enriched) ".service.Request"))
      
      ;; Check cross-namespace enrichment
      (let [request-msg (-> enriched
                            :files
                            second  ; service.proto
                            :messages
                            first)
            status-field (-> request-msg
                             :fields
                             second)]
        (is (= :status (:name status-field)))
        (is (get-in status-field [:type :enum :cross-namespace]))
        (is (= "common" (get-in status-field [:type :enum :target-package]))))))

  (testing "Nested message type resolution"
    (let [descriptor {:type :descriptor-set
                      :files [{:type :file
                               :name "nested.proto"
                               :package "test"
                               :dependencies []
                               :enums []
                               :messages [{:type :message
                                           :proto-name "Outer"
                                           :name :outer
                                           :fields []
                                           :nested-types [{:type :message
                                                           :proto-name "Inner"
                                                           :name :inner
                                                           :fields [{:name :value
                                                                     :proto-name "value"
                                                                     :number 1
                                                                     :label :label-optional
                                                                     :type {:scalar :int32}}]}]}
                                          {:type :message
                                           :proto-name "User"
                                           :name :user
                                           :fields [{:name :data
                                                     :proto-name "data"
                                                     :number 1
                                                     :label :label-optional
                                                     :type {:message {:type-ref ".test.Outer.Inner"}}}]}]}]}
          enriched (deps/enrich-descriptor-set descriptor)
          user-msg (-> enriched :files first :messages second)
          data-field (-> user-msg :fields first)]
      
      (is (contains? (:symbol-registry enriched) ".test.Outer.Inner"))
      (is (get-in data-field [:type :message :resolved]))
      (is (= :inner (get-in data-field [:type :message :resolved :definition :name])))))

  (testing "Multiple dependency chain"
    (let [descriptor {:type :descriptor-set
                      :files [{:type :file
                               :name "base.proto"
                               :package "base"
                               :dependencies []
                               :enums [{:type :enum
                                        :proto-name "BaseEnum"
                                        :name :base-enum
                                        :values [{:name :one :proto-name "ONE" :number 1}]}]
                               :messages []}
                              {:type :file
                               :name "middle.proto"
                               :package "middle"
                               :dependencies ["base.proto"]
                               :enums []
                               :messages [{:type :message
                                           :proto-name "MiddleMsg"
                                           :name :middle-msg
                                           :fields [{:name :base-ref
                                                     :proto-name "base_ref"
                                                     :number 1
                                                     :label :label-optional
                                                     :type {:enum {:type-ref ".base.BaseEnum"}}}]}]}
                              {:type :file
                               :name "top.proto"
                               :package "top"
                               :dependencies ["middle.proto"]
                               :enums []
                               :messages [{:type :message
                                           :proto-name "TopMsg"
                                           :name :top-msg
                                           :fields [{:name :middle-ref
                                                     :proto-name "middle_ref"
                                                     :number 1
                                                     :label :label-optional
                                                     :type {:message {:type-ref ".middle.MiddleMsg"}}}]}]}]}
          enriched (deps/enrich-descriptor-set descriptor)]
      
      ;; Check proper ordering
      (is (= ["base.proto" "middle.proto" "top.proto"] (:sorted-files enriched)))
      
      ;; Check all symbols are registered
      (is (= #{".base.BaseEnum" ".middle.MiddleMsg" ".top.TopMsg"}
             (set (keys (:symbol-registry enriched)))))
      
      ;; Validate the enriched structure
      (is (m/validate specs/EnrichedDescriptorSet enriched {:registry specs/registry})))))

(deftest test-validation-at-each-stage
  (testing "Invalid input is caught early"
    (is (thrown? Exception
                 (deps/enrich-descriptor-set
                  {:type :wrong-type  ; Invalid type
                   :files []})))))

(deftest test-edge-cases
  (testing "Empty descriptor set"
    (let [descriptor {:type :descriptor-set :files []}
          enriched (deps/enrich-descriptor-set descriptor)]
      (is (= :combined (:type enriched)))
      (is (empty? (:files enriched)))
      (is (empty? (:sorted-files enriched)))
      (is (empty? (:symbol-registry enriched)))))
  
  (testing "File with no dependencies, messages, or enums"
    (let [descriptor {:type :descriptor-set
                      :files [{:type :file
                               :name "empty.proto"
                               :package "empty"
                               :dependencies []
                               :enums []
                               :messages []}]}
          enriched (deps/enrich-descriptor-set descriptor)]
      (is (= 1 (count (:files enriched))))
      (is (empty? (:symbol-registry enriched))))))

(deftest test-oneof-handling
  (testing "Oneofs are preserved during enrichment"
    (let [descriptor {:type :descriptor-set
                      :files [{:type :file
                               :name "oneof.proto"
                               :package "test"
                               :dependencies []
                               :enums []
                               :messages [{:type :message
                                           :proto-name "Choice"
                                           :name :choice
                                           :fields [{:name :text
                                                     :proto-name "text"
                                                     :number 1
                                                     :label :label-optional
                                                     :type {:scalar :string}
                                                     :oneof-index 0}
                                                    {:name :number
                                                     :proto-name "number"
                                                     :number 2
                                                     :label :label-optional
                                                     :type {:scalar :int32}
                                                     :oneof-index 0}]
                                           :oneofs [{:name :value
                                                     :proto-name "value"
                                                     :index 0
                                                     :fields [{:name :text}
                                                              {:name :number}]}]}]}]}
          enriched (deps/enrich-descriptor-set descriptor)
          choice-msg (-> enriched :files first :messages first)]
      
      (is (= 1 (count (:oneofs choice-msg))))
      (is (= :value (-> choice-msg :oneofs first :name)))
      (is (= 0 (-> choice-msg :fields first :oneof-index))))))