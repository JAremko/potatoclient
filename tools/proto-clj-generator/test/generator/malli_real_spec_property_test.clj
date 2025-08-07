(ns generator.malli_real_spec_property_test
  "Property tests using real production specs with custom generators"
  (:require [clojure.test :refer [deftest is testing]]
            [clojure.test.check.clojure-test :refer [defspec]]
            [clojure.test.check.properties :as prop]
            [malli.core :as m]
            [malli.generator :as mg]
            [generator.deps :as deps]
            [generator.specs :as specs]
            [generator.spec-generators :as spec-gen]))

;; =============================================================================
;; Property Tests Using Real Specs
;; =============================================================================

(defspec enrichment-with-real-specs
  30
  (prop/for-all [descriptor spec-gen/descriptor-set-gen]
    (try
      (let [enriched (deps/enrich-descriptor-set descriptor)]
        (and 
         ;; Basic structure preserved
         (= :combined (:type enriched))
         (map? (:symbol-registry enriched))
         (vector? (:sorted-files enriched))
         
         ;; File count preserved
         (= (count (:files descriptor))
            (count (:files enriched)))
         
         ;; Enriched output validates against spec
         (m/validate specs/EnrichedDescriptorSet enriched {:registry specs/registry})))
      (catch Exception e
        ;; Circular dependencies are valid failures
        (boolean (re-find #"[Cc]ircular" (.getMessage e)))))))

(defspec symbol-registry-correctness
  30
  (prop/for-all [descriptor spec-gen/descriptor-set-gen]
    (try
      (let [enriched (deps/enrich-descriptor-set descriptor)
            registry (:symbol-registry enriched)]
        (and
         ;; All FQNs start with dot
         (every? #(clojure.string/starts-with? % ".") (keys registry))
         
         ;; All symbols have correct structure
         (every? (fn [[fqn sym]]
                   (and (= fqn (:fqn sym))
                        (#{:enum :message} (:type sym))
                        (map? (:definition sym))))
                 registry)))
      (catch Exception e
        (boolean (re-find #"[Cc]ircular" (.getMessage e)))))))

(defspec dependency-ordering
  30  
  (prop/for-all [descriptor spec-gen/descriptor-set-gen]
    (try
      (let [enriched (deps/enrich-descriptor-set descriptor)
            sorted-files (:sorted-files enriched)
            dep-graph (:dependency-graph enriched)
            edges (:edges dep-graph)]
        ;; For each file, all its dependencies come before it in sorted order
        (every? (fn [file]
                  (let [file-idx (.indexOf sorted-files file)
                        deps (get edges file #{})]
                    (every? #(< (.indexOf sorted-files %) file-idx) deps)))
                sorted-files))
      (catch Exception e
        (boolean (re-find #"[Cc]ircular" (.getMessage e)))))))

(defspec field-type-enrichment
  20
  (prop/for-all [descriptor spec-gen/descriptor-set-gen]
    (try
      (let [enriched (deps/enrich-descriptor-set descriptor)]
        ;; Check that non-scalar field types get enriched
        (every? (fn [file]
                  (every? (fn [msg]
                            (every? (fn [field]
                                      (let [field-type (:type field)]
                                        (cond
                                          ;; Scalars remain unchanged
                                          (:scalar field-type) true
                                          
                                          ;; Message/enum refs might get resolved
                                          (or (:message field-type) (:enum field-type))
                                          (let [ref (or (get-in field-type [:message :type-ref])
                                                        (get-in field-type [:enum :type-ref]))]
                                            ;; Either has original ref or enriched data
                                            (and (string? ref)
                                                 (clojure.string/starts-with? ref ".")))
                                          
                                          ;; Unknown types remain unchanged
                                          (:unknown field-type) true
                                          
                                          :else false)))
                                    (:fields msg)))
                          (:messages file)))
                (:files enriched)))
      (catch Exception e
        (boolean (re-find #"[Cc]ircular" (.getMessage e)))))))

;; =============================================================================
;; Specific Edge Case Tests
;; =============================================================================

(deftest test-empty-descriptor-set
  (testing "Empty descriptor set enrichment"
    (let [empty-desc {:type :descriptor-set :files []}
          enriched (deps/enrich-descriptor-set empty-desc)]
      (is (= :combined (:type enriched)))
      (is (empty? (:files enriched)))
      (is (empty? (:symbol-registry enriched)))
      (is (empty? (:sorted-files enriched))))))

(deftest test-single-file-no-deps
  (testing "Single file with no dependencies"
    (let [descriptor {:type :descriptor-set
                      :files [{:type :file
                               :name "test.proto"
                               :package "com.test"
                               :dependencies []
                               :messages [{:type :message
                                           :name :request
                                           :proto-name "Request"
                                           :package "com.test"
                                           :fields [{:name :id
                                                     :proto-name "id"
                                                     :type {:scalar :string}
                                                     :label :label-optional
                                                     :number 1}]}]
                               :enums []}]}
          enriched (deps/enrich-descriptor-set descriptor)]
      (is (= 1 (count (:files enriched))))
      (is (= ["test.proto"] (:sorted-files enriched)))
      (is (contains? (:symbol-registry enriched) ".com.test.Request")))))

(deftest test-cross-file-references
  (testing "Cross-file type references"
    (let [descriptor {:type :descriptor-set
                      :files [{:type :file
                               :name "types.proto"
                               :package "com.types"
                               :dependencies []
                               :messages [{:type :message
                                           :name :status
                                           :proto-name "Status"
                                           :package "com.types"
                                           :fields [{:name :code
                                                     :proto-name "code"
                                                     :type {:scalar :int32}
                                                     :label :label-optional
                                                     :number 1}]}]
                               :enums []}
                              {:type :file
                               :name "api.proto"
                               :package "com.api"
                               :dependencies ["types.proto"]
                               :messages [{:type :message
                                           :name :response
                                           :proto-name "Response"
                                           :package "com.api"
                                           :fields [{:name :status
                                                     :proto-name "status"
                                                     :type {:message {:type-ref ".com.types.Status"}}
                                                     :label :label-optional
                                                     :number 1}]}]
                               :enums []}]}
          enriched (deps/enrich-descriptor-set descriptor)]
      (is (= 2 (count (:files enriched))))
      (is (= ["types.proto" "api.proto"] (:sorted-files enriched)))
      (is (contains? (:symbol-registry enriched) ".com.types.Status"))
      (is (contains? (:symbol-registry enriched) ".com.api.Response")))))