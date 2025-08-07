(ns generator.enrichment-detailed-test
  "Detailed tests to understand enrichment behavior"
  (:require [clojure.test :refer [deftest is testing]]
            [generator.deps :as deps]
            [clojure.pprint :as pp]))

;; =============================================================================
;; Test Helpers
;; =============================================================================

(defn- print-enriched-structure
  "Helper to print enriched structure for debugging"
  [enriched]
  (println "\n=== Enriched Structure ===")
  (println "Type:" (:type enriched))
  (println "Keys:" (keys enriched))
  (println "\nFiles count:" (count (:files enriched)))
  (when (seq (:files enriched))
    (println "First file keys:" (keys (first (:files enriched)))))
  (println "\nSorted files:" (:sorted-files enriched))
  (println "Symbol registry size:" (count (:symbol-registry enriched)))
  enriched)

;; =============================================================================
;; Basic Enrichment Tests
;; =============================================================================

(deftest test-minimal-enrichment
  (testing "Enrichment of minimal descriptor set"
    (let [descriptor {:type :descriptor-set
                      :files [{:type :file
                               :name "test.proto"
                               :package "com.test"
                               :imports []
                               :dependencies []
                               :enums []
                               :messages []}]}
          enriched (deps/enrich-descriptor-set descriptor)]
      
      (print-enriched-structure enriched)
      
      ;; Test basic structure
      (is (= :combined (:type enriched)))
      (is (contains? enriched :files))
      (is (contains? enriched :dependency-graph))
      (is (contains? enriched :sorted-files))
      (is (contains? enriched :symbol-registry))
      
      ;; Test file preservation
      (is (= 1 (count (:files enriched))))
      (is (= "test.proto" (-> enriched :files first :name)))
      
      ;; Test sorted files
      (is (= ["test.proto"] (:sorted-files enriched)))
      
      ;; Test empty symbol registry
      (is (empty? (:symbol-registry enriched))))))

(deftest test-enrichment-with-enum
  (testing "Enrichment with single enum"
    (let [descriptor {:type :descriptor-set
                      :files [{:type :file
                               :name "test.proto"
                               :package "com.test"
                               :imports []
                               :dependencies []
                               :enums [{:type :enum
                                        :proto-name "Status"
                                        :values [{:name "UNKNOWN" :number 0}
                                                 {:name "SUCCESS" :number 1}]}]
                               :messages []}]}
          enriched (deps/enrich-descriptor-set descriptor)]
      
      (println "\n=== With Enum ===")
      (println "Symbol registry:" (:symbol-registry enriched))
      
      ;; Check symbol registry
      (is (= 1 (count (:symbol-registry enriched))))
      (is (contains? (:symbol-registry enriched) ".com.test.Status"))
      
      (let [status-sym (get (:symbol-registry enriched) ".com.test.Status")]
        (is (= :enum (:type status-sym)))
        (is (= ".com.test.Status" (:fqn status-sym)))
        (is (= "Status" (-> status-sym :definition :proto-name)))))))

(deftest test-enrichment-with-message
  (testing "Enrichment with single message"
    (let [descriptor {:type :descriptor-set
                      :files [{:type :file
                               :name "test.proto"
                               :package "com.test"
                               :imports []
                               :dependencies []
                               :enums []
                               :messages [{:type :message
                                           :proto-name "Request"
                                           :fields [{:name "id"
                                                     :type {:scalar :string}
                                                     :label :optional
                                                     :number 1}]}]}]}
          enriched (deps/enrich-descriptor-set descriptor)]
      
      (println "\n=== With Message ===")
      (println "Symbol registry:" (:symbol-registry enriched))
      
      ;; Check symbol registry
      (is (= 1 (count (:symbol-registry enriched))))
      (is (contains? (:symbol-registry enriched) ".com.test.Request"))
      
      (let [request-sym (get (:symbol-registry enriched) ".com.test.Request")]
        (is (= :message (:type request-sym)))
        (is (= ".com.test.Request" (:fqn request-sym)))
        (is (= "Request" (-> request-sym :definition :proto-name)))))))

(deftest test-enrichment-preserves-fields
  (testing "Enrichment preserves message fields"
    (let [descriptor {:type :descriptor-set
                      :files [{:type :file
                               :name "test.proto"
                               :package "com.test"
                               :imports []
                               :dependencies []
                               :enums []
                               :messages [{:type :message
                                           :proto-name "Request"
                                           :fields [{:name "id"
                                                     :type {:scalar :string}
                                                     :label :optional
                                                     :number 1}
                                                    {:name "value"
                                                     :type {:scalar :int32}
                                                     :label :optional
                                                     :number 2}]}]}]}
          enriched (deps/enrich-descriptor-set descriptor)
          enriched-msg (-> enriched :files first :messages first)]
      
      (println "\n=== Field Preservation ===")
      (println "Original fields:" (-> descriptor :files first :messages first :fields))
      (println "Enriched fields:" (:fields enriched-msg))
      
      ;; Check fields are preserved
      (is (= 2 (count (:fields enriched-msg))))
      (is (= "id" (-> enriched-msg :fields first :name)))
      (is (= {:scalar :string} (-> enriched-msg :fields first :type)))
      (is (= "value" (-> enriched-msg :fields second :name)))
      (is (= {:scalar :int32} (-> enriched-msg :fields second :type))))))

(deftest test-enrichment-with-dependencies
  (testing "Enrichment with file dependencies"
    (let [descriptor {:type :descriptor-set
                      :files [{:type :file
                               :name "base.proto"
                               :package "com.base"
                               :imports []
                               :dependencies []
                               :enums [{:type :enum
                                        :proto-name "Status"
                                        :values [{:name "OK" :number 0}]}]
                               :messages []}
                              {:type :file
                               :name "derived.proto"
                               :package "com.derived"
                               :imports ["base.proto"]
                               :dependencies ["base.proto"]
                               :enums []
                               :messages [{:type :message
                                           :proto-name "Response"
                                           :fields [{:name "status"
                                                     :type {:enum {:type-ref ".com.base.Status"}}
                                                     :label :optional
                                                     :number 1}]}]}]}
          enriched (deps/enrich-descriptor-set descriptor)]
      
      (println "\n=== With Dependencies ===")
      (println "Sorted files:" (:sorted-files enriched))
      (println "Dependency graph:" (:dependency-graph enriched))
      
      ;; Check dependency ordering
      (is (= ["base.proto" "derived.proto"] (:sorted-files enriched)))
      
      ;; Check both symbols in registry
      (is (= 2 (count (:symbol-registry enriched))))
      (is (contains? (:symbol-registry enriched) ".com.base.Status"))
      (is (contains? (:symbol-registry enriched) ".com.derived.Response")))))

(deftest test-enrichment-cross-namespace-detection
  (testing "Cross-namespace reference enrichment"
    (let [descriptor {:type :descriptor-set
                      :files [{:type :file
                               :name "types.proto"
                               :package "com.types"
                               :imports []
                               :dependencies []
                               :enums [{:type :enum
                                        :proto-name "ErrorCode"
                                        :values [{:name "NONE" :number 0}
                                                 {:name "INVALID" :number 1}]}]
                               :messages []}
                              {:type :file
                               :name "api.proto"
                               :package "com.api"
                               :imports ["types.proto"]
                               :dependencies ["types.proto"]
                               :enums []
                               :messages [{:type :message
                                           :proto-name "Error"
                                           :fields [{:name "code"
                                                     :type {:enum {:type-ref ".com.types.ErrorCode"}}
                                                     :label :optional
                                                     :number 1}]}]}]}
          enriched (deps/enrich-descriptor-set descriptor)
          error-msg (-> enriched :files second :messages first)
          code-field (-> error-msg :fields first)]
      
      (println "\n=== Cross-namespace Detection ===")
      (println "Code field type:" (:type code-field))
      (pp/pprint code-field)
      
      ;; Check if cross-namespace flag is added
      (when (get-in code-field [:type :enum :cross-namespace])
        (is (true? (get-in code-field [:type :enum :cross-namespace])))
        (is (= "com.types" (get-in code-field [:type :enum :target-package])))))))

(deftest test-enrichment-file-structure-changes
  (testing "How enrichment changes file structure"
    (let [original-file {:type :file
                         :name "test.proto"
                         :package "com.test"
                         :imports []
                         :dependencies []
                         :enums [{:type :enum
                                  :proto-name "Status"
                                  :values [{:name "OK" :number 0}]}]
                         :messages [{:type :message
                                     :proto-name "Request"
                                     :fields [{:name "id"
                                               :type {:scalar :string}
                                               :label :optional
                                               :number 1}]}]}
          descriptor {:type :descriptor-set
                      :files [original-file]}
          enriched (deps/enrich-descriptor-set descriptor)
          enriched-file (first (:files enriched))]
      
      (println "\n=== File Structure Comparison ===")
      (println "Original file keys:" (keys original-file))
      (println "Enriched file keys:" (keys enriched-file))
      (println "Keys added:" (clojure.set/difference (set (keys enriched-file)) 
                                                       (set (keys original-file))))
      (println "Keys removed:" (clojure.set/difference (set (keys original-file)) 
                                                         (set (keys enriched-file))))
      
      ;; Check what changed
      (is (= (keys original-file) (keys enriched-file)) 
          "File structure should be preserved"))))

(deftest test-enrichment-with-self-reference
  (testing "Enrichment with self-referencing message"
    (let [descriptor {:type :descriptor-set
                      :files [{:type :file
                               :name "recursive.proto"
                               :package "com.test"
                               :imports []
                               :dependencies []
                               :enums []
                               :messages [{:type :message
                                           :proto-name "Node"
                                           :fields [{:name "value"
                                                     :type {:scalar :string}
                                                     :label :optional
                                                     :number 1}
                                                    {:name "next"
                                                     :type {:message {:type-ref ".com.test.Node"}}
                                                     :label :optional
                                                     :number 2}]}]}]}
          enriched (deps/enrich-descriptor-set descriptor)
          node-msg (-> enriched :files first :messages first)
          next-field (-> node-msg :fields second)]
      
      (println "\n=== Self-reference ===")
      (println "Next field type:" (:type next-field))
      
      ;; Self-reference should not be marked as cross-namespace
      (is (not (get-in next-field [:type :message :cross-namespace]))))))

(deftest test-roundtrip-expectation
  (testing "Understanding roundtrip test expectations"
    (let [descriptor {:type :descriptor-set
                      :files [{:type :file
                               :name "test.proto"
                               :package "com.test"
                               :imports []
                               :dependencies []
                               :enums []
                               :messages [{:type :message
                                           :proto-name "Request"
                                           :fields [{:name "id"
                                                     :type {:scalar :string}
                                                     :label :optional
                                                     :number 1}]}]}]}
          enriched1 (deps/enrich-descriptor-set descriptor)
          ;; Extract just files for re-enrichment
          files-only {:type :descriptor-set :files (:files enriched1)}
          enriched2 (deps/enrich-descriptor-set files-only)]
      
      (println "\n=== Roundtrip Analysis ===")
      (println "First enrichment type:" (:type enriched1))
      (println "Second enrichment type:" (:type enriched2))
      (println "Sorted files match:" (= (:sorted-files enriched1) (:sorted-files enriched2)))
      (println "Registry size match:" (= (count (:symbol-registry enriched1))
                                         (count (:symbol-registry enriched2))))
      
      ;; This is what the property test checks
      (is (= (:sorted-files enriched1) (:sorted-files enriched2)))
      (is (= (count (:symbol-registry enriched1)) 
             (count (:symbol-registry enriched2)))))))