(ns generator.ir-transformation-malli-property-test
  "Property-based tests using Malli's spec-based generators"
  (:require [clojure.test :refer [deftest is testing]]
            [clojure.test.check.clojure-test :refer [defspec]]
            [clojure.test.check.properties :as prop]
            [malli.core :as m]
            [malli.generator :as mg]
            [generator.deps :as deps]
            [generator.specs :as specs]
            [com.stuartsierra.dependency :as dep]))

;; =============================================================================
;; Property Tests Using Malli Generators
;; =============================================================================

(defspec dependency-graph-properties-malli
  50
  (prop/for-all [descriptor (mg/generator specs/DescriptorSet {:registry specs/registry})]
    (try
      (let [{:keys [graph file-info]} (deps/build-dependency-graph descriptor)]
        (and
         ;; All files should be in the graph
         (every? #(contains? file-info %) (map :name (:files descriptor)))
         
         ;; Graph should be a valid dependency graph
         (instance? com.stuartsierra.dependency.MapDependencyGraph graph)
         
         ;; No file should depend on itself
         (every? (fn [file]
                   (not (contains? (dep/immediate-dependencies graph (:name file))
                                   (:name file))))
                 (:files descriptor))))
      (catch Exception e
        ;; If circular dependency detected, that's valid behavior
        (re-find #"[Cc]ircular" (.getMessage e))))))

(defspec topological-sort-properties-malli
  50
  (prop/for-all [descriptor (mg/generator specs/DescriptorSet {:registry specs/registry})]
    (try
      (let [{:keys [graph]} (deps/build-dependency-graph descriptor)
            sorted (deps/topological-sort graph)]
        (and
         ;; All nodes should be in the sorted list
         (= (set sorted) (set (dep/nodes graph)))
         
         ;; For each node, all its dependencies should come before it
         (every? (fn [node]
                   (let [node-idx (.indexOf sorted node)
                         deps (dep/immediate-dependencies graph node)]
                     (every? #(< (.indexOf sorted %) node-idx) deps)))
                 sorted)))
      (catch Exception e
        ;; If circular dependency detected, that's valid behavior
        (re-find #"[Cc]ircular" (.getMessage e))))))

(defspec symbol-collection-properties-malli
  50
  (prop/for-all [file (mg/generator specs/FileDef {:registry specs/registry})]
    (let [symbols (deps/collect-file-symbols file)]
      (and
       ;; Should be a vector of symbol definitions
       (vector? symbols)
       
       ;; All enums should be collected
       (every? (fn [enum]
                 (let [fqn (str "." (:package file) "." (:proto-name enum))]
                   (some #(and (= (:fqn %) fqn)
                               (= (:type %) :enum)
                               (= (:definition %) enum))
                         symbols)))
               (:enums file))
       
       ;; All messages should be collected
       (every? (fn [msg]
                 (let [fqn (str "." (:package file) "." (:proto-name msg))]
                   (some #(and (= (:fqn %) fqn)
                               (= (:type %) :message)
                               (= (:definition %) msg))
                         symbols)))
               (:messages file))))))

(defspec enrichment-preserves-structure-malli
  50
  (prop/for-all [descriptor (mg/generator specs/DescriptorSet {:registry specs/registry})]
    (try
      (let [enriched (deps/enrich-descriptor-set descriptor)]
        (and
         ;; Should have same number of files
         (= (count (:files descriptor)) (count (:files enriched)))
         
         ;; All original fields should still exist
         (= :combined (:type enriched))
         (map? (:symbol-registry enriched))
         (vector? (:sorted-files enriched))))
      (catch Exception e
        ;; Circular dependencies are valid failures
        (re-find #"[Cc]ircular" (.getMessage e))))))

(defspec enrichment-adds-metadata-malli
  25
  (prop/for-all [descriptor (mg/generator specs/DescriptorSet {:registry specs/registry})]
    (try
      (let [enriched (deps/enrich-descriptor-set descriptor)]
        ;; For each file, check that fields might have enriched type info
        (every? (fn [file]
                  (every? (fn [msg]
                            (every? (fn [field]
                                      ;; Field structure should be preserved
                                      (and (contains? field :name)
                                           (contains? field :type)))
                                    (:fields msg)))
                          (:messages file)))
                (:files enriched)))
      (catch Exception e
        ;; Circular dependencies are valid failures
        (re-find #"[Cc]ircular" (.getMessage e))))))

(defspec enrichment-output-valid-malli
  50
  (prop/for-all [descriptor (mg/generator specs/DescriptorSet {:registry specs/registry})]
    (try
      (let [enriched (deps/enrich-descriptor-set descriptor)]
        ;; The enriched output should match the EnrichedDescriptorSet spec
        (m/validate specs/EnrichedDescriptorSet enriched {:registry specs/registry}))
      (catch Exception e
        ;; Circular dependencies are valid failures
        (re-find #"[Cc]ircular" (.getMessage e))))))

;; =============================================================================
;; Test Specific Patterns
;; =============================================================================

(deftest test-malli-generates-valid-descriptors
  (testing "Malli generates valid descriptor sets"
    (let [samples (mg/sample specs/DescriptorSet {:size 10 :registry specs/registry})]
      (is (= 10 (count samples)))
      (is (every? #(m/validate specs/DescriptorSet % {:registry specs/registry}) 
                  samples)))))

(deftest test-malli-generates-cross-namespace-refs
  (testing "Malli can generate cross-namespace references"
    ;; Create a custom spec for testing cross-namespace scenarios
    (let [cross-ns-file-spec [:map
                              [:type [:= :file]]
                              [:name :string]
                              [:package :string]
                              [:dependencies [:vector :string]]
                              [:enums [:vector specs/EnumDef]]
                              [:messages [:vector [:map
                                                   [:type [:= :message]]
                                                   [:name :keyword]
                                                   [:proto-name :string]
                                                   [:package :string]
                                                   [:fields [:vector [:map
                                                                      [:name :keyword]
                                                                      [:proto-name :string]
                                                                      [:number :int]
                                                                      [:label specs/proto-labels]
                                                                      [:type [:or
                                                                              specs/ScalarType
                                                                              [:map [:enum [:map [:type-ref [:re #"^\.[a-zA-Z].*"]]]]]
                                                                              [:map [:message [:map [:type-ref [:re #"^\.[a-zA-Z].*"]]]]]]]]]]]]]]
          samples (mg/sample cross-ns-file-spec {:size 5 :registry specs/registry})]
      
      ;; Check that we can generate files with type references
      (is (pos? (count samples)))
      
      ;; Look for cross-namespace patterns
      (let [has-refs? (some (fn [file]
                              (some (fn [msg]
                                      (some (fn [field]
                                              (or (get-in field [:type :enum :type-ref])
                                                  (get-in field [:type :message :type-ref])))
                                            (:fields msg)))
                                    (:messages file)))
                            samples)]
        (is has-refs? "Should generate some type references")))))

(deftest test-enriched-spec-validates-actual-enrichment
  (testing "EnrichedDescriptorSet spec validates real enrichment output"
    (let [simple-descriptor {:type :descriptor-set
                             :files [{:type :file
                                      :name "test.proto"
                                      :package "com.test"
                                      :dependencies []
                                      :enums [{:type :enum
                                               :name :status
                                               :proto-name "Status"
                                               :package "com.test"
                                               :values [{:name :ok :proto-name "OK" :number 0}]}]
                                      :messages [{:type :message
                                                  :name :request
                                                  :proto-name "Request"
                                                  :package "com.test"
                                                  :fields [{:name :id
                                                            :proto-name "id"
                                                            :type {:scalar :string}
                                                            :label :label-optional
                                                            :number 1}]}]}]}]
      (is (m/validate specs/DescriptorSet simple-descriptor {:registry specs/registry}))
      
      (let [enriched (deps/enrich-descriptor-set simple-descriptor)]
        (is (m/validate specs/EnrichedDescriptorSet enriched {:registry specs/registry})
            "Enriched output should validate against EnrichedDescriptorSet spec")))))