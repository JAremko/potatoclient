(ns generator.property_test_summary
  "Summary of property testing approaches for IR transformation.
  
  Key learnings:
  1. Use custom generators for complex specs with constraints
  2. Attach generators to real production specs, not simplified test specs
  3. Handle circular dependencies gracefully in tests
  4. Validate enriched output against the EnrichedDescriptorSet spec"
  (:require [clojure.test :refer [deftest is testing]]
            [clojure.test.check.clojure-test :refer [defspec]]
            [clojure.test.check.properties :as prop]
            [clojure.test.check.generators :as gen]
            [malli.core :as m]
            [generator.deps :as deps]
            [generator.specs :as specs]
            [generator.spec-generators :as spec-gen]))

;; =============================================================================
;; Main Property Test Pattern
;; =============================================================================

(defspec comprehensive-ir-enrichment-test
  50  ; Number of test cases
  (prop/for-all [descriptor spec-gen/descriptor-set-gen]  ; Use custom generator
    (try
      (let [enriched (deps/enrich-descriptor-set descriptor)]
        (and 
         ;; 1. Basic structural invariants
         (= :combined (:type enriched))
         (map? enriched)
         (contains? enriched :files)
         (contains? enriched :symbol-registry)
         (contains? enriched :sorted-files)
         (contains? enriched :dependency-graph)
         
         ;; 2. File preservation
         (= (count (:files descriptor))
            (count (:files enriched)))
         
         ;; 3. All files appear in sorted order
         (= (set (map :name (:files descriptor)))
            (set (:sorted-files enriched)))
         
         ;; 4. Symbol registry is well-formed
         (every? (fn [[fqn sym-def]]
                   (and (string? fqn)
                        (clojure.string/starts-with? fqn ".")
                        (= fqn (:fqn sym-def))
                        (#{:enum :message} (:type sym-def))
                        (map? (:definition sym-def))))
                 (:symbol-registry enriched))
         
         ;; 5. Output validates against the enriched spec
         (m/validate specs/EnrichedDescriptorSet enriched {:registry specs/registry})))
      
      ;; Handle expected failure cases
      (catch Exception e
        ;; Circular dependencies are valid reasons for enrichment to fail
        (boolean (re-find #"[Cc]ircular" (.getMessage e)))))))

;; =============================================================================
;; Edge Case Tests
;; =============================================================================

(deftest test-enrichment-edge-cases
  (testing "Empty descriptor set"
    (let [empty-desc {:type :descriptor-set :files []}
          enriched (deps/enrich-descriptor-set empty-desc)]
      (is (= :combined (:type enriched)))
      (is (empty? (:files enriched)))
      (is (empty? (:symbol-registry enriched)))))
  
  (testing "Single file with self-contained types"
    (let [descriptor {:type :descriptor-set
                      :files [{:type :file
                               :name "standalone.proto"
                               :package "com.standalone"
                               :dependencies []
                               :messages [{:type :message
                                           :name :user
                                           :proto-name "User"
                                           :package "com.standalone"
                                           :fields [{:name :id
                                                     :proto-name "id"
                                                     :type {:scalar :string}
                                                     :label :label-optional
                                                     :number 1}
                                                    {:name :status
                                                     :proto-name "status"
                                                     :type {:enum {:type-ref ".com.standalone.Status"}}
                                                     :label :label-optional
                                                     :number 2}]}]
                               :enums [{:type :enum
                                        :name :status
                                        :proto-name "Status"
                                        :package "com.standalone"
                                        :values [{:name :active :proto-name "ACTIVE" :number 0}
                                                 {:name :inactive :proto-name "INACTIVE" :number 1}]}]}]}
          enriched (deps/enrich-descriptor-set descriptor)]
      (is (= 1 (count (:files enriched))))
      (is (contains? (:symbol-registry enriched) ".com.standalone.User"))
      (is (contains? (:symbol-registry enriched) ".com.standalone.Status"))))
  
  (testing "Circular dependency detection"
    (let [circular-desc {:type :descriptor-set
                         :files [{:type :file
                                  :name "a.proto"
                                  :package "com.a"
                                  :dependencies ["b.proto"]
                                  :messages []
                                  :enums []}
                                 {:type :file
                                  :name "b.proto"
                                  :package "com.b"
                                  :dependencies ["a.proto"]
                                  :messages []
                                  :enums []}]}]
      (is (thrown-with-msg? Exception #"[Cc]ircular"
                            (deps/enrich-descriptor-set circular-desc))))))

;; =============================================================================
;; Generator Quality Tests
;; =============================================================================

(deftest test-custom-generators-produce-valid-data
  (testing "Generated descriptors validate against original specs"
    (doseq [_ (range 10)]
      (let [descriptor (gen/generate spec-gen/descriptor-set-gen)]
        (is (m/validate specs/DescriptorSet descriptor {:registry specs/registry})))))
  
  (testing "Generated files have consistent packages"
    (doseq [_ (range 10)]
      (let [file (gen/generate spec-gen/file-def-gen)]
        (is (every? #(= (:package file) (:package %)) (:messages file)))
        (is (every? #(= (:package file) (:package %)) (:enums file))))))
  
  (testing "No self-dependencies in generated files"
    (doseq [_ (range 10)]
      (let [descriptor (gen/generate spec-gen/descriptor-set-gen)]
        (is (every? (fn [file]
                      (not (contains? (set (:dependencies file)) (:name file))))
                    (:files descriptor)))))))

;; =============================================================================
;; Key Insights for Property Testing IR Transformations
;; =============================================================================

(comment
  "1. Custom Generators are Essential"
  ;; The IR structure has many constraints that Malli's automatic generators
  ;; struggle with (e.g., TypeReference must start with dot, consistent packages).
  ;; Custom generators ensure valid test data.
  
  "2. Handle Expected Failures"
  ;; Circular dependencies are a valid reason for enrichment to fail.
  ;; Property tests should catch these exceptions and consider them passing cases.
  
  "3. Test Invariants, Not Implementation"
  ;; Focus on properties that should always hold:
  ;; - File count preserved
  ;; - All symbols collected
  ;; - Dependency ordering respected
  ;; - Output validates against spec
  
  "4. Use Real Specs"
  ;; Test against the actual production specs with custom generators
  ;; rather than creating simplified test-only specs.
  
  "5. Incremental Testing"
  ;; Start with simple cases (empty, single file) before testing complex scenarios.
  ;; This helps isolate issues in the generators or the code under test.
  )