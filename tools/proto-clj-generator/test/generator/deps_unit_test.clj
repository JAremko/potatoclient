(ns generator.deps-unit-test
  "Granular unit tests for dependency resolution functions"
  (:require [clojure.test :refer [deftest is testing]]
            [generator.deps :as deps]
            [generator.specs :as specs]
            [com.stuartsierra.dependency :as dep]))

;; =============================================================================
;; Individual Function Tests
;; =============================================================================

(deftest test-extract-file-dependencies
  (testing "Extract dependencies from file with dependencies field"
    (let [file {:type :file
                :name "test.proto"
                :package "test.pkg"
                :dependencies ["dep1.proto" "dep2.proto"]}
          result (deps/extract-file-dependencies file)]
      (is (= {:name "test.proto"
              :package "test.pkg"
              :depends-on ["dep1.proto" "dep2.proto"]}
             result))))
  
  (testing "Extract dependencies from file with dependency field (JSON style)"
    (let [file {:type :file
                :name "test.proto"
                :package "test.pkg"
                :dependency ["dep1.proto" "dep2.proto"]}
          result (deps/extract-file-dependencies file)]
      (is (= {:name "test.proto"
              :package "test.pkg"
              :depends-on ["dep1.proto" "dep2.proto"]}
             result))))
  
  (testing "Extract dependencies with filtered system deps"
    (let [file {:type :file
                :name "test.proto"
                :package "test.pkg"
                :dependencies ["dep1.proto" 
                                "google/protobuf/descriptor.proto"
                                "buf/validate/validate.proto"]}
          result (deps/extract-file-dependencies file)]
      (is (= ["dep1.proto"] (:depends-on result))))))

(deftest test-build-dependency-graph
  (testing "Build simple dependency graph"
    (let [descriptor {:type :descriptor-set
                      :files [{:type :file :name "a.proto" :package "a" :dependencies []}
                              {:type :file :name "b.proto" :package "b" :dependencies ["a.proto"]}]}
          {:keys [graph file-info]} (deps/build-dependency-graph descriptor)]
      (is (instance? com.stuartsierra.dependency.MapDependencyGraph graph))
      (is (= {"a.proto" {:name "a.proto" :package "a" :depends-on []}
              "b.proto" {:name "b.proto" :package "b" :depends-on ["a.proto"]}}
             file-info))
      (is (dep/depends? graph "b.proto" "a.proto"))))
  
  (testing "Build empty dependency graph"
    (let [descriptor {:type :descriptor-set :files []}
          {:keys [graph file-info]} (deps/build-dependency-graph descriptor)]
      (is (empty? file-info))
      (is (empty? (dep/nodes graph))))))

(deftest test-topological-sort
  (testing "Sort simple dependency chain"
    (let [graph (-> (dep/graph)
                    (dep/depend "c" "b")
                    (dep/depend "b" "a"))
          sorted (deps/topological-sort graph)]
      (is (= ["a" "b" "c"] sorted))))
  
  (testing "Sort with multiple roots"
    (let [graph (-> (dep/graph)
                    (dep/depend "c" "a")
                    (dep/depend "c" "b"))
          sorted (deps/topological-sort graph)]
      (is (contains? #{["a" "b" "c"] ["b" "a" "c"]} sorted))
      (is (= "c" (last sorted))))))

(deftest test-collect-file-symbols
  (testing "Collect top-level enum"
    (let [file {:type :file
                :package "test"
                :enums [{:type :enum :proto-name "Status" :name :status}]
                :messages []}
          symbols (deps/collect-file-symbols file)]
      (is (= 1 (count symbols)))
      (is (= ".test.Status" (:fqn (first symbols))))
      (is (= :enum (:type (first symbols))))))
  
  (testing "Collect top-level message"
    (let [file {:type :file
                :package "test"
                :enums []
                :messages [{:type :message :proto-name "User" :name :user}]}
          symbols (deps/collect-file-symbols file)]
      (is (= 1 (count symbols)))
      (is (= ".test.User" (:fqn (first symbols))))
      (is (= :message (:type (first symbols))))))
  
  (testing "Collect nested message"
    (let [file {:type :file
                :package "test"
                :enums []
                :messages [{:type :message 
                            :proto-name "Outer" 
                            :name :outer
                            :nested-types [{:type :message 
                                            :proto-name "Inner" 
                                            :name :inner}]}]}
          symbols (deps/collect-file-symbols file)]
      (is (= 2 (count symbols)))
      (is (= #{".test.Outer" ".test.Outer.Inner"}
             (set (map :fqn symbols))))))
  
  (testing "Collect nested enum in message"
    (let [file {:type :file
                :package "test"
                :enums []
                :messages [{:type :message 
                            :proto-name "Msg" 
                            :name :msg
                            :nested-types [{:type :enum 
                                            :proto-name "Type" 
                                            :name :type}]}]}
          symbols (deps/collect-file-symbols file)]
      (is (= 2 (count symbols)))
      (is (= #{".test.Msg" ".test.Msg.Type"}
             (set (map :fqn symbols)))))))

(deftest test-build-symbol-registry
  (testing "Build registry from single file"
    (let [descriptor {:type :descriptor-set
                      :files [{:type :file
                               :name "test.proto"
                               :package "test"
                               :enums [{:type :enum :proto-name "E1" :name :e1}]
                               :messages [{:type :message :proto-name "M1" :name :m1}]}]}
          sorted-files ["test.proto"]
          registry (deps/build-symbol-registry descriptor sorted-files)]
      (is (= 2 (count registry)))
      (is (contains? registry ".test.E1"))
      (is (contains? registry ".test.M1"))
      (is (= :enum (get-in registry [".test.E1" :type])))
      (is (= :message (get-in registry [".test.M1" :type])))))
  
  (testing "Build registry with multiple files"
    (let [descriptor {:type :descriptor-set
                      :files [{:type :file
                               :name "a.proto"
                               :package "pkg.a"
                               :enums [{:type :enum :proto-name "AEnum" :name :a-enum}]
                               :messages []}
                              {:type :file
                               :name "b.proto"
                               :package "pkg.b"
                               :enums []
                               :messages [{:type :message :proto-name "BMsg" :name :b-msg}]}]}
          sorted-files ["a.proto" "b.proto"]
          registry (deps/build-symbol-registry descriptor sorted-files)]
      (is (= 2 (count registry)))
      (is (contains? registry ".pkg.a.AEnum"))
      (is (contains? registry ".pkg.b.BMsg")))))

(deftest test-enrich-type-reference
  (testing "Enrich type reference with cross-namespace detection"
    (let [type-ref ".other.Type"
          registry {".other.Type" {:fqn ".other.Type"
                                   :type :enum
                                   :definition {:package "other" :name :type}}}
          current-package "current"
          result (#'deps/enrich-type-reference type-ref registry current-package)]
      (is (= type-ref (:type-ref result)))
      (is (:cross-namespace result))
      (is (= "other" (:target-package result)))
      (is (= (get registry type-ref) (:resolved result)))))
  
  (testing "Enrich type reference within same namespace"
    (let [type-ref ".same.Type"
          registry {".same.Type" {:fqn ".same.Type"
                                  :type :enum
                                  :definition {:package "same" :name :type}}}
          current-package "same"
          result (#'deps/enrich-type-reference type-ref registry current-package)]
      (is (= type-ref (:type-ref result)))
      (is (not (:cross-namespace result)))
      (is (not (contains? result :target-package)))))
  
  (testing "Enrich unknown type reference"
    (let [type-ref ".unknown.Type"
          registry {}
          current-package "current"
          result (#'deps/enrich-type-reference type-ref registry current-package)]
      (is (= type-ref (:type-ref result)))
      (is (not (:resolved result)))
      (is (not (:cross-namespace result))))))

(deftest test-enrich-field-type
  (testing "Enrich scalar field type (no change)"
    (let [field-type {:scalar :string}
          registry {}
          result (#'deps/enrich-field-type field-type registry "pkg")]
      (is (= field-type result))))
  
  (testing "Enrich message field type"
    (let [field-type {:message {:type-ref ".other.Msg"}}
          registry {".other.Msg" {:fqn ".other.Msg"
                                  :type :message
                                  :definition {:package "other" :name :msg}}}
          result (#'deps/enrich-field-type field-type registry "current")]
      (is (get-in result [:message :type-ref]))
      (is (get-in result [:message :resolved]))
      (is (get-in result [:message :cross-namespace]))))
  
  (testing "Enrich enum field type"
    (let [field-type {:enum {:type-ref ".pkg.Status"}}
          registry {".pkg.Status" {:fqn ".pkg.Status"
                                   :type :enum
                                   :definition {:package "pkg" :name :status}}}
          result (#'deps/enrich-field-type field-type registry "pkg")]
      (is (get-in result [:enum :type-ref]))
      (is (get-in result [:enum :resolved]))
      (is (not (get-in result [:enum :cross-namespace]))))))

(deftest test-enrich-file
  (testing "Enrich file with no messages"
    (let [file {:type :file :package "test" :messages []}
          registry {}
          result (deps/enrich-file file registry)]
      (is (= file result))))
  
  (testing "Enrich file with scalar fields only"
    (let [file {:type :file 
                :package "test"
                :messages [{:type :message
                            :proto-name "Simple"
                            :name :simple
                            :fields [{:name :id :type {:scalar :string}}
                                      {:name :count :type {:scalar :int32}}]}]}
          registry {}
          result (deps/enrich-file file registry)]
      (is (= file result))))
  
  (testing "Enrich file with type references"
    (let [file {:type :file 
                :package "test"
                :messages [{:type :message
                            :proto-name "Complex"
                            :name :complex
                            :fields [{:name :status 
                                      :type {:enum {:type-ref ".other.Status"}}}]}]}
          registry {".other.Status" {:fqn ".other.Status"
                                     :type :enum
                                     :definition {:package "other" :name :status}}}
          result (deps/enrich-file file registry)]
      (is (get-in result [:messages 0 :fields 0 :type :enum :resolved]))
      (is (get-in result [:messages 0 :fields 0 :type :enum :cross-namespace])))))

;; =============================================================================
;; Sanity and Negative Tests
;; =============================================================================

(deftest test-edge-cases-extract-file-dependencies
  (testing "File with no dependencies"
    (let [file {:type :file :name "solo.proto" :package "solo" :dependencies []}
          result (deps/extract-file-dependencies file)]
      (is (= [] (:depends-on result)))))
  
  (testing "File with nil dependencies"
    (let [file {:type :file :name "nil.proto" :package "nil"}
          result (deps/extract-file-dependencies file)]
      (is (= [] (:depends-on result)))))
  
  (testing "File with only system dependencies (all filtered)"
    (let [file {:type :file 
                :name "system.proto" 
                :package "sys"
                :dependencies ["google/protobuf/descriptor.proto"
                                "google/protobuf/duration.proto"
                                "google/protobuf/timestamp.proto"
                                "buf/validate/validate.proto"]}
          result (deps/extract-file-dependencies file)]
      (is (empty? (:depends-on result))))))

(deftest test-circular-dependency-detection
  (testing "Direct circular dependency A -> B -> A"
    (let [descriptor {:type :descriptor-set
                      :files [{:type :file :name "a.proto" :package "a" :dependencies ["b.proto"]}
                              {:type :file :name "b.proto" :package "b" :dependencies ["a.proto"]}]}]
      (is (thrown-with-msg? Exception #"[Cc]ircular"
                            (deps/build-dependency-graph descriptor)))))
  
  (testing "Indirect circular dependency A -> B -> C -> A"
    (let [descriptor {:type :descriptor-set
                      :files [{:type :file :name "a.proto" :package "a" :dependencies ["b.proto"]}
                              {:type :file :name "b.proto" :package "b" :dependencies ["c.proto"]}
                              {:type :file :name "c.proto" :package "c" :dependencies ["a.proto"]}]}]
      (is (thrown-with-msg? Exception #"[Cc]ircular"
                            (deps/build-dependency-graph descriptor))))))

(deftest test-missing-dependency-handling
  (testing "Reference to non-existent file"
    (let [descriptor {:type :descriptor-set
                      :files [{:type :file :name "a.proto" :package "a" :dependencies ["missing.proto"]}]}
          {:keys [graph]} (deps/build-dependency-graph descriptor)
          ;; The graph is built but missing.proto won't be in sorted files
          sorted (deps/topological-sort graph)]
      (is (= ["missing.proto" "a.proto"] sorted)))))

(deftest test-empty-and-nil-handling
  (testing "Empty descriptor set"
    (let [descriptor {:type :descriptor-set :files []}
          {:keys [graph file-info]} (deps/build-dependency-graph descriptor)
          sorted (deps/topological-sort graph)
          registry (deps/build-symbol-registry descriptor sorted)]
      (is (empty? file-info))
      (is (empty? sorted))
      (is (empty? registry))))
  
  (testing "File with empty messages and enums"
    (let [file {:type :file :package "empty" :messages [] :enums []}
          symbols (deps/collect-file-symbols file)]
      (is (empty? symbols))))
  
  (testing "Message with empty fields"
    (let [file {:type :file 
                :package "test"
                :messages [{:type :message :proto-name "Empty" :name :empty :fields []}]}
          registry {}
          enriched (deps/enrich-file file registry)]
      (is (= file enriched)))))

(deftest test-malformed-type-references
  (testing "Type reference without leading dot (should still work)"
    (let [type-ref "InvalidRef"
          registry {}
          current-package "test"
          result (#'deps/enrich-type-reference type-ref registry current-package)]
      (is (= type-ref (:type-ref result)))
      (is (not (:resolved result)))))
  
  (testing "Field with unknown type structure"
    (let [field-type {:unknown {:something "weird"}}
          registry {}
          result (#'deps/enrich-field-type field-type registry "pkg")]
      (is (= field-type result)))))

(deftest test-package-edge-cases
  (testing "Empty package name"
    (let [file {:type :file
                :package ""
                :enums [{:type :enum :proto-name "E" :name :e}]
                :messages []}
          symbols (deps/collect-file-symbols file)]
      (is (= "..E" (:fqn (first symbols))))))
  
  (testing "Package with dots"
    (let [file {:type :file
                :package "com.example.deep.nested"
                :enums [{:type :enum :proto-name "Status" :name :status}]
                :messages []}
          symbols (deps/collect-file-symbols file)]
      (is (= ".com.example.deep.nested.Status" (:fqn (first symbols)))))))

(deftest test-deeply-nested-types
  (testing "Three levels of nesting"
    (let [file {:type :file
                :package "test"
                :messages [{:type :message
                            :proto-name "Level1"
                            :name :level1
                            :nested-types [{:type :message
                                            :proto-name "Level2"
                                            :name :level2
                                            :nested-types [{:type :message
                                                            :proto-name "Level3"
                                                            :name :level3}]}]}]}
          symbols (deps/collect-file-symbols file)]
      (is (= 3 (count symbols)))
      (is (contains? (set (map :fqn symbols)) ".test.Level1.Level2.Level3")))))

(deftest test-duplicate-handling
  (testing "Multiple files with same name (last wins)"
    (let [descriptor {:type :descriptor-set
                      :files [{:type :file :name "dup.proto" :package "first" :dependencies []}
                              {:type :file :name "dup.proto" :package "second" :dependencies []}]}
          {:keys [graph file-info]} (deps/build-dependency-graph descriptor)]
      ;; Only one entry for dup.proto
      (is (= 1 (count file-info)))
      (is (= "second" (get-in file-info ["dup.proto" :package])))))
  
  (testing "Registry with duplicate FQNs (last wins)"
    (let [descriptor {:type :descriptor-set
                      :files [{:type :file
                               :name "a.proto"
                               :package "pkg"
                               :enums [{:type :enum :proto-name "Dup" :name :dup-a}]
                               :messages []}
                              {:type :file
                               :name "b.proto"
                               :package "pkg"
                               :enums [{:type :enum :proto-name "Dup" :name :dup-b}]
                               :messages []}]}
          sorted-files ["a.proto" "b.proto"]
          registry (deps/build-symbol-registry descriptor sorted-files)]
      (is (= 1 (count registry)))
      (is (= :dup-b (get-in registry [".pkg.Dup" :definition :name]))))))

(deftest test-performance-sanity
  (testing "Large number of independent files"
    (let [files (mapv (fn [i]
                        {:type :file
                         :name (str "file" i ".proto")
                         :package (str "pkg" i)
                         :dependencies []})
                      (range 100))
          descriptor {:type :descriptor-set :files files}
          {:keys [graph]} (deps/build-dependency-graph descriptor)
          sorted (deps/topological-sort graph)]
      (is (= 100 (count sorted)))))
  
  (testing "Deep dependency chain"
    (let [files (mapv (fn [i]
                        {:type :file
                         :name (str "file" i ".proto")
                         :package (str "pkg" i)
                         :dependencies (if (> i 0)
                                         [(str "file" (dec i) ".proto")]
                                         [])})
                      (range 20))
          descriptor {:type :descriptor-set :files files}
          {:keys [graph]} (deps/build-dependency-graph descriptor)
          sorted (deps/topological-sort graph)]
      (is (= 20 (count sorted)))
      (is (= "file0.proto" (first sorted)))
      (is (= "file19.proto" (last sorted))))))

(deftest test-guardrails-validation
  (testing "Valid inputs work correctly"
    ;; Test that valid inputs don't throw
    (let [valid-file {:type :file :name "test.proto" :package "test" :dependencies []}]
      (is (map? (deps/extract-file-dependencies valid-file))))
    
    (let [valid-descriptor {:type :descriptor-set :files []}]
      (is (map? (deps/build-dependency-graph valid-descriptor))))
    
    (let [valid-file {:type :file :package "test" :messages []}]
      (is (map? (deps/enrich-file valid-file {}))))))