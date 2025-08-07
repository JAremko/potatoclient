(ns generator.deps-integration-test
  "Integration tests for dependency resolution system"
  (:require [clojure.test :refer [deftest is testing]]
            [generator.deps :as deps]
            [generator.specs :as specs]
            [com.rpl.specter :as sp]
            [clojure.core.match :refer [match]]))

(defn create-test-file
  "Create a test file descriptor"
  [{:keys [name package dependencies messages enums]}]
  {:type :file
   :name name
   :package package
   :dependencies (or dependencies [])
   :messages (or messages [])
   :enums (or enums [])})

(defn create-test-enum
  "Create a test enum"
  ([name values] (create-test-enum name values nil))
  ([name values package]
   {:type :enum
    :name (keyword name)
    :proto-name name
    :package (or package "test")
    :values (vec (map-indexed (fn [idx v]
                                {:name (keyword v)
                                 :proto-name v
                                 :number idx})
                              values))}))

(defn create-test-message
  "Create a test message"
  ([name fields] (create-test-message name fields nil))
  ([name fields package]
   {:type :message
    :name (keyword name)
    :proto-name name
    :package (or package "test")
    :fields fields
    :oneofs []
    :nested-types []}))

(defn create-test-field
  "Create a test field"
  [{:keys [name type-ref scalar-type enum-ref number]
    :or {number 1}}]
  {:name (keyword name)
   :proto-name name
   :number number
   :label :label-optional
   :type (cond
           scalar-type {:scalar scalar-type}
           enum-ref {:enum {:type-ref enum-ref}}
           type-ref {:message {:type-ref type-ref}}
           :else {:scalar :string})})

(deftest test-dependency-graph-building
  (testing "Simple dependency chain"
    (let [descriptor-set {:type :descriptor-set
                          :files [(create-test-file {:name "base.proto"
                                                     :package "base"})
                                  (create-test-file {:name "derived.proto"
                                                     :package "derived"
                                                     :dependencies ["base.proto"]})]}
          {:keys [graph file-info]} (deps/build-dependency-graph descriptor-set)]
      (is (= #{"base.proto" "derived.proto"} (set (keys file-info))))
      (is (= [] (get-in file-info ["base.proto" :depends-on])))
      (is (= ["base.proto"] (get-in file-info ["derived.proto" :depends-on])))))
  
  (testing "Multiple dependencies"
    (let [descriptor-set {:type :descriptor-set
                          :files [(create-test-file {:name "a.proto" :package "a"})
                                  (create-test-file {:name "b.proto" :package "b"})
                                  (create-test-file {:name "c.proto" 
                                                     :package "c"
                                                     :dependencies ["a.proto" "b.proto"]})]}
          {:keys [file-info]} (deps/build-dependency-graph descriptor-set)]
      (is (= #{"a.proto" "b.proto"} (set (get-in file-info ["c.proto" :depends-on])))))))

(deftest test-topological-sort
  (testing "Linear dependency chain"
    (let [descriptor-set {:type :descriptor-set
                          :files [(create-test-file {:name "a.proto" :package "a"})
                                  (create-test-file {:name "b.proto" :package "b" :dependencies ["a.proto"]})
                                  (create-test-file {:name "c.proto" :package "c" :dependencies ["b.proto"]})]}
          {:keys [graph]} (deps/build-dependency-graph descriptor-set)
          sorted (deps/topological-sort graph)]
      (is (= ["a.proto" "b.proto" "c.proto"] sorted))))
  
  (testing "Diamond dependency"
    (let [descriptor-set {:type :descriptor-set
                          :files [(create-test-file {:name "base.proto" :package "base"})
                                  (create-test-file {:name "left.proto" :package "left" :dependencies ["base.proto"]})
                                  (create-test-file {:name "right.proto" :package "right" :dependencies ["base.proto"]})
                                  (create-test-file {:name "top.proto" :package "top" :dependencies ["left.proto" "right.proto"]})]}
          {:keys [graph]} (deps/build-dependency-graph descriptor-set)
          sorted (deps/topological-sort graph)]
      (is (= "base.proto" (first sorted)))
      (is (= "top.proto" (last sorted)))
      (is (= 4 (count sorted)))))
  
  (testing "Circular dependency detection"
    (let [descriptor-set {:type :descriptor-set
                          :files [(create-test-file {:name "a.proto" :package "a" :dependencies ["b.proto"]})
                                  (create-test-file {:name "b.proto" :package "b" :dependencies ["a.proto"]})]}]
      ;; The circular dependency is detected during graph building now
      (is (thrown-with-msg? Exception #"[Cc]ircular"
                            (deps/build-dependency-graph descriptor-set))))))

(deftest test-symbol-collection
  (testing "Collect top-level symbols"
    (let [file (create-test-file 
                {:name "test.proto"
                 :package "test.pkg"
                 :enums [(create-test-enum "Status" ["UNKNOWN" "ACTIVE"])]
                 :messages [(create-test-message "Request" [])]})
          symbols (deps/collect-file-symbols file)]
      (is (= 2 (count symbols)))
      (is (= #{".test.pkg.Status" ".test.pkg.Request"}
             (set (map :fqn symbols))))))
  
  (testing "Collect nested enums"
    (let [message (create-test-message 
                   "Outer"
                   [(create-test-field {:name "value" :scalar-type :string})])
          message (assoc message 
                         :nested-types [(create-test-enum "Inner" ["A" "B"])])
          file (create-test-file {:name "test.proto"
                                  :package "test"
                                  :messages [message]})
          symbols (deps/collect-file-symbols file)]
      (is (= 2 (count symbols)))
      (is (contains? (set (map :fqn symbols)) ".test.Outer"))
      (is (contains? (set (map :fqn symbols)) ".test.Outer.Inner")))))

(deftest test-type-enrichment
  (testing "Enrich cross-namespace enum reference through full pipeline"
    (let [;; Create a file with a field that references another namespace
          message (create-test-message "TestMsg" 
                                      [(create-test-field {:name "mode" 
                                                          :enum-ref ".other.FxMode"})])
          file {:type :file
                :name "current.proto"
                :package "current"
                :messages [message]
                :enums []}
          ;; Create registry with the referenced type
          registry {".other.FxMode" {:fqn ".other.FxMode"
                                     :type :enum
                                     :definition {:package "other"
                                                  :name :fx-mode
                                                  :proto-name "FxMode"}}}
          ;; Enrich the file
          enriched-file (deps/enrich-file file registry)]
      ;; Check that the field was enriched with cross-namespace info
      (is (true? (get-in enriched-file [:messages 0 :fields 0 :type :enum :cross-namespace])))
      (is (= "other" (get-in enriched-file [:messages 0 :fields 0 :type :enum :target-package])))))
  
  (testing "Same namespace reference not marked as cross-namespace"
    (let [message (create-test-message "TestMsg"
                                      [(create-test-field {:name "status"
                                                          :enum-ref ".current.Status"})])
          file {:type :file
                :name "current.proto"
                :package "current"
                :messages [message]
                :enums []}
          registry {".current.Status" {:fqn ".current.Status"
                                       :type :enum
                                       :definition {:package "current"
                                                    :name :status
                                                    :proto-name "Status"}}}
          enriched-file (deps/enrich-file file registry)]
      ;; Should not have cross-namespace flag
      (is (nil? (get-in enriched-file [:messages 0 :fields 0 :type :enum :cross-namespace]))))))

(deftest test-specter-core-match-edge-cases
  (testing "Top-level message enrichment with Specter (nested types not currently enriched)"
    (let [;; Create structure with cross-namespace refs in top-level messages
          msg1 (create-test-message 
                "TopMsg1"
                [(create-test-field {:name "ref1"
                                     :enum-ref ".other.OtherEnum"})]
                "current")
          msg2 (create-test-message
               "TopMsg2"
               [(create-test-field {:name "ref2"
                                    :type-ref ".other.OtherMsg"})]
               "current")
          
          file {:type :file
                :name "multi.proto"
                :package "current"
                :messages [msg1 msg2]
                :enums []}
          
          registry {".other.OtherEnum" {:fqn ".other.OtherEnum"
                                        :type :enum
                                        :definition {:package "other" :type :enum}}
                    ".other.OtherMsg" {:fqn ".other.OtherMsg"
                                       :type :message
                                       :definition {:package "other" :type :message}}}
          
          enriched (deps/enrich-file file registry)]
      
      ;; Verify top-level enrichment worked
      (is (true? (get-in enriched [:messages 0 :fields 0 :type :enum :cross-namespace])))
      (is (true? (get-in enriched [:messages 1 :fields 0 :type :message :cross-namespace])))
      ;; Both should have target-package set
      (is (= "other" (get-in enriched [:messages 0 :fields 0 :type :enum :target-package])))
      (is (= "other" (get-in enriched [:messages 1 :fields 0 :type :message :target-package])))))
  
  (testing "Mixed scalar and reference types with core.match"
    (let [msg (create-test-message
               "Mixed"
               [(create-test-field {:name "scalar1" :scalar-type :int32})
                (create-test-field {:name "enum1" :enum-ref ".pkg.SomeEnum"})
                (create-test-field {:name "scalar2" :scalar-type :string})
                (create-test-field {:name "msg1" :type-ref ".pkg.SomeMsg"})
                (create-test-field {:name "scalar3" :scalar-type :bool})]
               "pkg")
          file {:type :file
                :name "mixed.proto"
                :package "pkg"
                :messages [msg]
                :enums []}
          registry {".pkg.SomeEnum" {:fqn ".pkg.SomeEnum"
                                     :type :enum
                                     :definition {:package "pkg"}}
                    ".pkg.SomeMsg" {:fqn ".pkg.SomeMsg"
                                    :type :message
                                    :definition {:package "pkg"}}}
          enriched (deps/enrich-file file registry)]
      
      ;; Verify scalars remain unchanged
      (is (= {:scalar :int32} (get-in enriched [:messages 0 :fields 0 :type])))
      (is (= {:scalar :string} (get-in enriched [:messages 0 :fields 2 :type])))
      (is (= {:scalar :bool} (get-in enriched [:messages 0 :fields 4 :type])))
      
      ;; Verify references are enriched but not cross-namespace
      (is (nil? (get-in enriched [:messages 0 :fields 1 :type :enum :cross-namespace])))
      (is (nil? (get-in enriched [:messages 0 :fields 3 :type :message :cross-namespace])))))
  
  (testing "Empty and nil handling in enrichment"
    (let [empty-file {:type :file
                      :package "empty"
                      :messages []
                      :enums []}
          enriched (deps/enrich-file empty-file {})]
      (is (= empty-file enriched)))
    
    (let [file-no-fields {:type :file
                          :package "test"
                          :messages [{:type :message
                                      :name :empty
                                      :proto-name "Empty"
                                      :package "test"
                                      :fields []
                                      :oneofs []
                                      :nested-types []}]
                          :enums []}
          enriched (deps/enrich-file file-no-fields {})]
      (is (= file-no-fields enriched)))))

(deftest test-full-enrichment-pipeline
  (testing "Complete enrichment with cross-namespace references"
    (let [;; Create a descriptor set with cross-namespace references
          types-file (create-test-file 
                      {:name "types.proto"
                       :package "common"
                       :enums [(create-test-enum "SharedEnum" ["VALUE_A" "VALUE_B"] "common")]})
          
          consumer-msg (create-test-message
                        "Consumer"
                        [(create-test-field {:name "shared_value"
                                             :enum-ref ".common.SharedEnum"})]
                        "app")
          
          consumer-file (create-test-file
                         {:name "consumer.proto"
                          :package "app"
                          :dependencies ["types.proto"]
                          :messages [consumer-msg]})
          
          descriptor-set {:type :descriptor-set
                          :files [types-file consumer-file]}
          
          ;; Run enrichment
          enriched (deps/enrich-descriptor-set descriptor-set)]
      
      ;; Verify structure
      (is (specs/valid? specs/EnrichedDescriptorSet enriched))
      
      ;; Verify dependency graph
      (is (= ["types.proto" "consumer.proto"] (:sorted-files enriched)))
      
      ;; Verify symbol registry
      (is (contains? (:symbol-registry enriched) ".common.SharedEnum"))
      (is (contains? (:symbol-registry enriched) ".app.Consumer"))
      
      ;; Verify enriched field
      (let [enriched-consumer (-> enriched
                                  :files
                                  second
                                  :messages
                                  first)
            shared-field (-> enriched-consumer
                             :fields
                             first)]
        (is (true? (get-in shared-field [:type :enum :cross-namespace])))
        (is (= "common" (get-in shared-field [:type :enum :target-package]))))))

  (testing "Error handling for missing dependencies"
    (let [file-with-bad-ref (create-test-file
                             {:name "bad.proto"
                              :package "bad"
                              :messages [(create-test-message
                                          "BadMsg"
                                          [(create-test-field {:name "missing"
                                                               :enum-ref ".nonexistent.Type"})]
                                          "bad")]})
          descriptor-set {:type :descriptor-set
                          :files [file-with-bad-ref]}
          enriched (deps/enrich-descriptor-set descriptor-set)]
      ;; Should complete without error, unresolved refs just don't get enriched
      (is (= :combined (:type enriched)))
      (is (nil? (get-in enriched [:files 0 :messages 0 :fields 0 :type :enum :resolved]))))))

(deftest test-specter-path-sanity-checks
  (testing "ALL-FIELDS-PATH reaches all fields in complex structure"
    (let [complex-file {:type :file
                        :package "test"
                        :messages [{:type :message
                                    :name :msg1
                                    :fields [{:name :f1 :type {:scalar :int32}}
                                             {:name :f2 :type {:scalar :string}}]}
                                   {:type :message
                                    :name :msg2
                                    :fields [{:name :f3 :type {:enum {:type-ref ".test.E1"}}}]}
                                   {:type :message
                                    :name :msg3
                                    :fields []}]}
          ;; Count fields using Specter
          field-count (count (com.rpl.specter/select 
                              [:messages com.rpl.specter/ALL :fields com.rpl.specter/ALL]
                              complex-file))]
      (is (= 3 field-count))))
  
  (testing "Specter transformation preserves structure"
    (let [original {:type :file
                    :package "test"
                    :messages [{:type :message
                                :fields [{:type {:scalar :int32}}]}]}
          transformed (com.rpl.specter/transform
                       [:messages com.rpl.specter/ALL :fields com.rpl.specter/ALL :type]
                       (fn [t] (assoc t :modified true))
                       original)]
      ;; Structure should be preserved
      (is (= :file (:type transformed)))
      (is (= "test" (:package transformed)))
      (is (= 1 (count (:messages transformed))))
      ;; Modification should be applied
      (is (true? (get-in transformed [:messages 0 :fields 0 :type :modified]))))))

(deftest test-core-match-pattern-coverage
  (testing "All field type patterns are handled"
    (let [test-cases [{:type {:scalar :int32} :expected :scalar}
                      {:type {:message {:type-ref ".pkg.Msg"}} :expected :message}
                      {:type {:enum {:type-ref ".pkg.Enum"}} :expected :enum}
                      {:type {:unknown {:proto-type :weird}} :expected :unknown}
                      {:type {:something "unexpected"} :expected :other}]]
      (doseq [{:keys [type expected]} test-cases]
        (let [result (match type
                       {:scalar _} :scalar
                       {:message _} :message
                       {:enum _} :enum
                       {:unknown _} :unknown
                       :else :other)]
          (is (= expected result))))))

  (testing "Nested pattern matching with core.match"
    (let [nested-type {:enum {:type-ref ".pkg.Status" 
                              :resolved {:fqn ".pkg.Status"
                                         :type :enum}}}
          result (match nested-type
                   {:enum {:type-ref ref :resolved _}} ref
                   :else nil)]
      (is (= ".pkg.Status" result)))))