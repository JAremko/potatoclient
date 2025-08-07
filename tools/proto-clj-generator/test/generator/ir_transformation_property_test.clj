(ns generator.ir-transformation-property-test
  "Property-based tests for IR transformations using Malli generators"
  (:require [clojure.test :refer [deftest is testing]]
            [clojure.test.check.clojure-test :refer [defspec]]
            [clojure.test.check.properties :as prop]
            [clojure.test.check.generators :as gen]
            [malli.core :as m]
            [malli.generator :as mg]
            [generator.deps :as deps]
            [generator.backend :as backend]
            [generator.specs :as specs]
            [com.stuartsierra.dependency :as dep]))

;; =============================================================================
;; Generators for IR Data Structures
;; =============================================================================

;; Simple generators for basic types
(def package-name-gen
  (gen/fmap (fn [parts]
              (clojure.string/join "." parts))
            (gen/vector (gen/elements ["com" "org" "io" "pkg" "test" "proto"]) 1 4)))

(def file-name-gen
  (gen/fmap (fn [name] (str name ".proto"))
            (gen/elements ["common" "types" "service" "messages" "enums" "config"])))

(def type-name-gen
  (gen/elements ["Request" "Response" "Status" "Config" "Data" "Info" "Error"]))

(def enum-value-gen
  (gen/elements ["UNKNOWN" "SUCCESS" "FAILURE" "PENDING" "ACTIVE" "INACTIVE"]))

;; Generator for enum definitions
(def enum-gen
  (gen/fmap (fn [[name values]]
              {:type :enum
               :proto-name name
               :values (mapv (fn [v] {:name v :number (rand-int 100)}) values)})
            (gen/tuple type-name-gen
                       (gen/vector enum-value-gen 2 5))))

;; Generator for scalar field types
(def scalar-type-gen
  (gen/elements [:string :int32 :int64 :uint32 :uint64 :double :float :bool :bytes]))

;; Generator for field types
(def field-type-gen
  (gen/frequency [[6 (gen/fmap (fn [t] {:scalar t}) scalar-type-gen)]
                  [2 (gen/fmap (fn [ref] {:enum {:type-ref ref}})
                               (gen/fmap (fn [name] (str "." name)) type-name-gen))]
                  [2 (gen/fmap (fn [ref] {:message {:type-ref ref}})
                               (gen/fmap (fn [name] (str "." name)) type-name-gen))]]))

;; Generator for fields
(def field-gen
  (gen/fmap (fn [[name type]]
              {:name name
               :type type
               :label :optional
               :number (rand-int 1000)})
            (gen/tuple (gen/elements ["id" "name" "value" "status" "data" "info"])
                       field-type-gen)))

;; Generator for messages
(def message-gen
  (gen/fmap (fn [[name fields]]
              {:type :message
               :proto-name name
               :fields fields})
            (gen/tuple type-name-gen
                       (gen/vector field-gen 1 5))))

;; Generator for files with dependencies
(def file-with-deps-gen
  (gen/let [filename file-name-gen
            package package-name-gen
            ;; Generate imports that don't include the current file
            other-files (gen/vector file-name-gen 0 5)
            enums (gen/vector enum-gen 0 3)
            messages (gen/vector message-gen 0 5)]
    (let [imports (vec (distinct (remove #(= % filename) other-files)))]
      {:type :file
       :name filename
       :package package
       :imports imports
       :dependencies imports
       :enums enums
       :messages messages})))

;; Generator for descriptor sets
(def descriptor-set-gen
  (gen/fmap (fn [files]
              ;; Ensure unique filenames by deduping
              (let [unique-files (reduce (fn [acc file]
                                           (if (some #(= (:name %) (:name file)) acc)
                                             acc
                                             (conj acc file)))
                                         []
                                         files)
                    filenames (set (map :name unique-files))
                    ;; Filter dependencies to only reference existing files
                    filtered-files (mapv (fn [file]
                                           (-> file
                                               (update :dependencies
                                                       (fn [deps]
                                                         (vec (filter filenames deps))))
                                               (update :imports
                                                       (fn [deps]
                                                         (vec (filter filenames deps))))))
                                         unique-files)]
                {:type :descriptor-set
                 :files filtered-files}))
            (gen/vector file-with-deps-gen 1 10)))

;; =============================================================================
;; Property Tests for Dependency Graph
;; =============================================================================

(defspec dependency-graph-properties
  50
  (prop/for-all [descriptor descriptor-set-gen]
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

(defspec topological-sort-properties
  50
  (prop/for-all [descriptor descriptor-set-gen]
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

;; =============================================================================
;; Property Tests for Symbol Collection
;; =============================================================================

(defspec symbol-collection-properties
  50
  (prop/for-all [file file-with-deps-gen]
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

;; =============================================================================
;; Property Tests for Type Enrichment
;; =============================================================================

(defspec enrichment-preserves-structure
  50
  (prop/for-all [descriptor descriptor-set-gen]
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

(defspec enrichment-adds-metadata
  25
  (prop/for-all [descriptor descriptor-set-gen]
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

;; =============================================================================
;; Property Tests for Circular Dependency Detection
;; =============================================================================

(def circular-deps-gen
  "Generator that creates descriptor sets with circular dependencies"
  (gen/fmap (fn [[file1 file2 file3]]
              {:type :descriptor-set
               :files [{:type :file
                        :name file1
                        :package "pkg1"
                        :dependencies [file2]
                        :imports [file2]
                        :enums []
                        :messages []}
                       {:type :file
                        :name file2
                        :package "pkg2"
                        :dependencies [file3]
                        :imports [file3]
                        :enums []
                        :messages []}
                       {:type :file
                        :name file3
                        :package "pkg3"
                        :dependencies [file1]
                        :imports [file1]
                        :enums []
                        :messages []}]})
            (gen/tuple file-name-gen file-name-gen file-name-gen)))

(defspec circular-dependency-detection
  20
  (prop/for-all [descriptor circular-deps-gen]
    (try
      (let [{:keys [graph]} (deps/build-dependency-graph descriptor)]
        ;; Should throw on topological sort
        (deps/topological-sort graph)
        false) ; If we get here, test failed
      (catch Exception e
        ;; Should get circular dependency error
        (re-find #"[Cc]ircular" (.getMessage e))))))

;; =============================================================================
;; Property Tests for Empty/Edge Cases
;; =============================================================================

(defspec handles-empty-files
  50
  (prop/for-all [n (gen/choose 1 10)]
    (let [empty-files (mapv (fn [i]
                               {:type :file
                                :name (str "empty" i ".proto")
                                :package (str "pkg" i)
                                :dependencies []
                                :imports []
                                :enums []
                                :messages []})
                             (range n))
          descriptor {:type :descriptor-set :files empty-files}
          enriched (deps/enrich-descriptor-set descriptor)]
      (and
       ;; Should handle empty files
       (= n (count (:files enriched)))
       ;; Symbol registry should be empty
       (empty? (:symbol-registry enriched))))))

;; =============================================================================
;; Performance Sanity Tests
;; =============================================================================

(deftest performance-with-generated-data
  (testing "IR enrichment performs acceptably on generated data"
    (let [large-descriptor (gen/generate (gen/resize 50 descriptor-set-gen))
          start (System/nanoTime)
          enriched (deps/enrich-descriptor-set large-descriptor)
          elapsed (/ (- (System/nanoTime) start) 1e9)]
      (is enriched)
      (is (< elapsed 5.0) 
          (str "Enrichment of " (count (:files large-descriptor)) 
               " files took " elapsed " seconds")))))

;; =============================================================================
;; Invariant Tests
;; =============================================================================

(defspec enrichment-invariants
  50
  (prop/for-all [descriptor descriptor-set-gen]
    (try
      (let [enriched (deps/enrich-descriptor-set descriptor)]
        (and
         ;; 1. File count preserved
         (= (count (:files descriptor))
            (count (:files enriched)))
         
         ;; 2. All files in sorted order are from original
         (every? (fn [filename]
                   (some #(= filename (:name %)) (:files descriptor)))
                 (:sorted-files enriched))
         
         ;; 3. Symbol registry contains only valid FQNs
         (every? (fn [[fqn _]]
                   (re-matches #"^[a-zA-Z][a-zA-Z0-9_.]*$" fqn))
                 (:symbol-registry enriched))
         
         ;; 4. No nil values in enriched structure
         (nil? (some nil? (tree-seq coll? seq enriched)))))
      (catch Exception e
        (re-find #"[Cc]ircular" (.getMessage e))))))

;; =============================================================================
;; Roundtrip Properties
;; =============================================================================

(defspec ir-roundtrip-property
  25
  (prop/for-all [descriptor descriptor-set-gen]
    (try
      ;; Test that enriching twice gives the same result
      (let [enriched1 (deps/enrich-descriptor-set descriptor)
            ;; Extract just the file data for re-enrichment
            files-only {:type :descriptor-set :files (:files enriched1)}
            enriched2 (deps/enrich-descriptor-set files-only)]
        ;; Key properties should be preserved
        (and (= (:sorted-files enriched1) (:sorted-files enriched2))
             (= (count (:symbol-registry enriched1)) 
                (count (:symbol-registry enriched2)))))
      (catch Exception e
        (re-find #"[Cc]ircular" (.getMessage e))))))