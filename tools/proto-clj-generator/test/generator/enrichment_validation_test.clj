(ns generator.enrichment-validation-test
  "Test to understand validation errors in enrichment"
  (:require [clojure.test :refer [deftest is testing]]
            [generator.deps :as deps]
            [generator.specs :as specs]
            [malli.core :as m]
            [clojure.pprint :as pp]))

(deftest test-basic-enrichment-validation
  (testing "Minimal enrichment should pass validation"
    (let [descriptor {:type :descriptor-set
                      :files [{:type :file
                               :name "test.proto"
                               :package "com.test"
                               :imports []
                               :dependencies []
                               :enums []
                               :messages []}]}]
      (try
        (let [enriched (deps/enrich-descriptor-set descriptor)]
          (is (= :combined (:type enriched))))
        (catch Exception e
          (println "Validation error:")
          (pp/pprint (ex-data e))
          (throw e))))))

(deftest test-enrichment-with-scalar-field
  (testing "Enrichment with scalar field should pass"
    (let [descriptor {:type :descriptor-set
                      :files [{:type :file
                               :name "test.proto"
                               :package "com.test"
                               :imports []
                               :dependencies []
                               :enums []
                               :messages [{:type :message
                                           :name :request
                                           :proto-name "Request"
                                           :package "com.test"
                                           :fields [{:name :id
                                                     :proto-name "id"
                                                     :type {:scalar :string}
                                                     :label :label-optional
                                                     :number 1}]}]}]}]
      (try
        (let [enriched (deps/enrich-descriptor-set descriptor)]
          (is enriched))
        (catch Exception e
          (println "\nValidation error for scalar field:")
          (pp/pprint (ex-data e))
          (throw e))))))

(deftest test-field-structure-after-enrichment
  (testing "Check field structure details"
    (let [descriptor {:type :descriptor-set
                      :files [{:type :file
                               :name "test.proto"
                               :package "com.test"
                               :imports []
                               :dependencies []
                               :enums []
                               :messages [{:type :message
                                           :name :request
                                           :proto-name "Request"
                                           :package "com.test"
                                           :fields [{:name :id
                                                     :proto-name "id"
                                                     :type {:scalar :string}
                                                     :label :label-optional
                                                     :number 1}]}]}]}]
      ;; Disable validation temporarily to see the structure
      (with-redefs [specs/validate! (fn [_ _ _] nil)]
        (let [enriched (deps/enrich-descriptor-set descriptor)
              field (-> enriched :files first :messages first :fields first)]
          (println "\n=== Field structure after enrichment ===")
          (pp/pprint field)
          
          ;; Check against expected spec
          (let [valid? (m/validate specs/EnrichedField field {:registry specs/registry})]
            (when-not valid?
              (println "\nValidation errors:")
              (pp/pprint (m/explain specs/EnrichedField field {:registry specs/registry})))
            (is valid? "Field should match EnrichedField spec")))))))

(deftest test-enum-ref-enrichment
  (testing "Enum reference enrichment structure"
    (let [descriptor {:type :descriptor-set
                      :files [{:type :file
                               :name "types.proto"
                               :package "com.types"
                               :imports []
                               :dependencies []
                               :enums [{:type :enum
                                        :name :status
                                        :proto-name "Status"
                                        :package "com.types"
                                        :values [{:name :ok :proto-name "OK" :number 0}]}]
                               :messages []}
                              {:type :file
                               :name "api.proto"
                               :package "com.api"
                               :imports ["types.proto"]
                               :dependencies ["types.proto"]
                               :enums []
                               :messages [{:type :message
                                           :name :response
                                           :proto-name "Response"
                                           :package "com.api"
                                           :fields [{:name :status
                                                     :proto-name "status"
                                                     :type {:enum {:type-ref ".com.types.Status"}}
                                                     :label :label-optional
                                                     :number 1}]}]}]}]
      ;; Disable validation to see structure
      (with-redefs [specs/validate! (fn [_ _ _] nil)]
        (let [enriched (deps/enrich-descriptor-set descriptor)
              status-field (-> enriched :files second :messages first :fields first)]
          (println "\n=== Enum field after enrichment ===")
          (pp/pprint status-field)
          
          ;; Check the type structure
          (println "\nType structure:")
          (pp/pprint (:type status-field))
          
          ;; Validate just the field type
          (let [field-type (:type status-field)
                valid? (m/validate specs/EnrichedFieldType field-type {:registry specs/registry})]
            (when-not valid?
              (println "\nField type validation errors:")
              (pp/pprint (m/explain specs/EnrichedFieldType field-type {:registry specs/registry})))
            (is valid? "Field type should match EnrichedFieldType spec")))))))