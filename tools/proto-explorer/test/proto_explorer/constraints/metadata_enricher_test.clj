(ns proto-explorer.constraints.metadata-enricher-test
  "Tests for the metadata enricher."
  (:require [clojure.test :refer :all]
            [proto-explorer.constraints.metadata-enricher :as enricher]))

;; =============================================================================
;; Constraint Extraction
;; =============================================================================

(deftest test-extract-buf-validate-constraints
  (testing "Extract buf.validate constraints from options"
    (let [options {(keyword "[buf.validate.field]") {:float {:gte 0 :lt 360}}}
          result (enricher/extract-buf-validate-constraints options)]
      (is (= {:buf.validate {:float {:gte 0 :lt 360}}} result))))
  
  (testing "No constraints returns nil"
    (let [options {:some-other-option "value"}
          result (enricher/extract-buf-validate-constraints options)]
      (is (nil? result))))
  
  (testing "Empty options returns nil"
    (is (nil? (enricher/extract-buf-validate-constraints {})))))

(deftest test-extract-oneof-constraints
  (testing "Extract oneof constraints"
    (let [options {(keyword "[buf.validate.oneof]") {:required true}}
          result (enricher/extract-oneof-constraints options)]
      (is (= {:buf.validate {:required true}} result)))))

;; =============================================================================
;; Field Enrichment
;; =============================================================================

(deftest test-enrich-field
  (testing "Enrich field with constraints"
    (let [field {:name "value"
                 :type "TYPE_FLOAT"
                 :options {(keyword "[buf.validate.field]") {:float {:gte 0 :lt 360}}}}
          enriched (enricher/enrich-field field)]
      ;; Field should have constraints in metadata
      (is (= {:constraints {:buf.validate {:float {:gte 0 :lt 360}}}
              :original-options (:options field)
              :field-type :field}
             (meta enriched)))
      ;; Options should be removed from structure
      (is (nil? (:options enriched)))
      ;; Other fields preserved
      (is (= "value" (:name enriched)))
      (is (= "TYPE_FLOAT" (:type enriched)))))
  
  (testing "Field without constraints unchanged"
    (let [field {:name "id" :type "TYPE_INT32"}
          enriched (enricher/enrich-field field)]
      (is (= field enriched))
      (is (nil? (meta enriched))))))

;; =============================================================================
;; Message Enrichment
;; =============================================================================

(deftest test-enrich-message
  (testing "Enrich message with field constraints"
    (let [message {:name "SetAzimuthValue"
                   :field [{:name "value"
                           :type "TYPE_FLOAT"
                           :options {(keyword "[buf.validate.field]") {:float {:gte 0 :lt 360}}}}
                          {:name "direction"
                           :type "TYPE_ENUM"}]}
          enriched (enricher/enrich-message message)]
      ;; First field should have metadata
      (let [value-field (first (:field enriched))]
        (is (enricher/has-constraints? value-field))
        (is (= {:buf.validate {:float {:gte 0 :lt 360}}}
               (enricher/get-constraints value-field))))
      ;; Second field should not have metadata
      (let [direction-field (second (:field enriched))]
        (is (not (enricher/has-constraints? direction-field))))))
  
  (testing "Enrich message with nested messages"
    (let [message {:name "Parent"
                   :nested-type [{:name "Child"
                                 :field [{:name "value"
                                         :type "TYPE_INT32"
                                         :options {(keyword "[buf.validate.field]") 
                                                  {:int32 {:gte 0 :lte 100}}}}]}]}
          enriched (enricher/enrich-message message)
          child (first (:nested-type enriched))
          child-field (first (:field child))]
      (is (enricher/has-constraints? child-field))
      (is (= {:buf.validate {:int32 {:gte 0 :lte 100}}}
             (enricher/get-constraints child-field))))))

;; =============================================================================
;; Full Descriptor Enrichment
;; =============================================================================

(deftest test-enrich-descriptor
  (testing "Enrich complete descriptor"
    (let [descriptor {:file [{:name "test.proto"
                             :package "test"
                             :message-type [{:name "TestMessage"
                                            :field [{:name "value"
                                                    :type "TYPE_FLOAT"
                                                    :options {(keyword "[buf.validate.field]")
                                                             {:float {:gte -90 :lte 90}}}}]}]}]}
          enriched (enricher/enrich-descriptor descriptor)
          message (first (:message-type (first (:file enriched))))
          field (first (:field message))]
      (is (enricher/has-constraints? field))
      (is (= {:buf.validate {:float {:gte -90 :lte 90}}}
             (enricher/get-constraints field))))))

;; =============================================================================
;; Metadata Access Helpers
;; =============================================================================

(deftest test-metadata-helpers
  (testing "Get constraints from enriched field"
    (let [field (with-meta {:name "test"} 
                          {:constraints {:buf.validate {:string {:min-len 5}}}})]
      (is (= {:buf.validate {:string {:min-len 5}}}
             (enricher/get-constraints field)))))
  
  (testing "Get field type from metadata"
    (let [field (with-meta {:name "test"} {:field-type :field})]
      (is (= :field (enricher/get-field-type field)))))
  
  (testing "Check if has constraints"
    (let [with-constraints (with-meta {} {:constraints {:some "constraint"}})
          without-constraints {}]
      (is (enricher/has-constraints? with-constraints))
      (is (not (enricher/has-constraints? without-constraints)))))
  
  (testing "Get original options"
    (let [field (with-meta {:name "test"}
                          {:original-options {:foo "bar"}})]
      (is (= {:foo "bar"} (enricher/get-original-options field))))))

;; =============================================================================
;; Debug Helpers
;; =============================================================================

(deftest test-debug-helpers
  (testing "Print with metadata shows metadata"
    (let [data {:field (with-meta {:name "test"} 
                                 {:constraints {:buf.validate {:int32 {:gte 0}}}})}
          printed (enricher/print-with-metadata data)]
      (is (contains? (:field printed) ::enricher/metadata))
      (is (= {:constraints {:buf.validate {:int32 {:gte 0}}}}
             (::enricher/metadata (:field printed))))))
  
  (testing "Strip metadata removes all metadata"
    (let [data {:field (with-meta {:name "test"} {:some "metadata"})}
          stripped (enricher/strip-metadata data)]
      (is (nil? (meta (:field stripped)))))))

;; =============================================================================
;; Integration Tests
;; =============================================================================

(deftest test-enrichment-integration
  (testing "Full enrichment workflow"
    (let [descriptor {:file [{:name "example.proto"
                             :message-type [{:name "Example"
                                           :field [{:name "temperature"
                                                   :type "TYPE_FLOAT"
                                                   :options {(keyword "[buf.validate.field]")
                                                            {:float {:gte -273.15}}}}
                                                  {:name "count"
                                                   :type "TYPE_INT32"
                                                   :options {(keyword "[buf.validate.field]")
                                                            {:int32 {:gte 0 :lte 1000}}}}]}]
                             :enum-type [{:name "Status"
                                         :value [{:name "UNKNOWN" :number 0}
                                                {:name "ACTIVE" :number 1}]}]}]}
          enriched (enricher/enrich-descriptor descriptor)
          file (first (:file enriched))
          message (first (:message-type file))
          temp-field (first (:field message))
          count-field (second (:field message))]
      
      ;; Check temperature field
      (is (enricher/has-constraints? temp-field))
      (is (= {:buf.validate {:float {:gte -273.15}}}
             (enricher/get-constraints temp-field)))
      (is (nil? (:options temp-field)))
      
      ;; Check count field
      (is (enricher/has-constraints? count-field))
      (is (= {:buf.validate {:int32 {:gte 0 :lte 1000}}}
             (enricher/get-constraints count-field)))
      (is (nil? (:options count-field)))
      
      ;; Check enum is preserved
      (is (= 1 (count (:enum-type file)))))))