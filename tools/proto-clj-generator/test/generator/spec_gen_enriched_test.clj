(ns generator.spec-gen-enriched-test
  "Test spec generation with enriched IR metadata"
  (:require [clojure.test :refer [deftest is testing]]
            [generator.spec-gen :as spec-gen]
            [generator.deps :as deps]))

(deftest test-enriched-enum-spec-generation
  (testing "Cross-namespace enum reference uses alias with enriched IR"
    (let [;; Create a field with enriched cross-namespace enum reference
          field {:name :status
                 :proto-name "status"
                 :number 1
                 :label :label-optional
                 :type {:enum {:type-ref ".other.pkg.Status"
                               :cross-namespace true
                               :target-package "other.pkg"
                               :resolved {:fqn ".other.pkg.Status"
                                          :type :enum
                                          :definition {:package "other.pkg"}}}}}
          ;; Context with package mappings
          context {:current-package "current.pkg"
                   :ns-aliases {"other.pkg" "other-alias"}}
          ;; Process the field type
          result (spec-gen/process-field-type field context)]
      ;; Should return a symbol referencing the spec through alias
      (is (symbol? result))
      (is (= "other-alias/status-spec" (str result)))))
  
  (testing "Same namespace enum reference returns keyword"
    (let [field {:name :mode
                 :proto-name "mode"
                 :number 2
                 :label :label-optional
                 :type {:enum {:type-ref ".current.pkg.Mode"
                               ;; No cross-namespace flag means same namespace
                               :resolved {:fqn ".current.pkg.Mode"
                                          :type :enum
                                          :definition {:package "current.pkg"}}}}}
          context {:current-package "current.pkg"
                   :ns-aliases {}}
          result (spec-gen/process-field-type field context)]
      ;; Should return keyword for same namespace
      (is (keyword? result))
      (is (= :current.pkg/mode result))))
  
  (testing "Cross-namespace without alias falls back to keyword"
    (let [field {:name :type
                 :proto-name "type"
                 :number 3
                 :label :label-optional
                 :type {:enum {:type-ref ".unknown.Type"
                               :cross-namespace true
                               :target-package "unknown"
                               :resolved {:fqn ".unknown.Type"
                                          :type :enum
                                          :definition {:package "unknown"}}}}}
          ;; No alias for "unknown" package
          context {:current-package "current"
                   :ns-aliases {"other" "other-alias"}}
          result (spec-gen/process-field-type field context)]
      ;; Should fall back to keyword
      (is (keyword? result))
      (is (= :unknown/type result)))))

(deftest test-enriched-message-spec-generation
  (testing "Cross-namespace message reference returns :any"
    (let [field {:name :request
                 :proto-name "request"
                 :number 1
                 :label :label-optional
                 :type {:message {:type-ref ".other.Request"
                                  :cross-namespace true
                                  :target-package "other"
                                  :resolved {:fqn ".other.Request"
                                             :type :message
                                             :definition {:package "other"}}}}}
          context {:current-package "current"
                   :ns-aliases {"other" "other-alias"}}
          result (spec-gen/process-field-type field context)]
      ;; Cross-namespace messages should return :any
      (is (= :any result))))
  
  (testing "Same namespace message reference returns keyword"
    (let [field {:name :response
                 :proto-name "response"
                 :number 2
                 :label :label-optional
                 :type {:message {:type-ref ".current.Response"
                                  ;; No cross-namespace flag
                                  :resolved {:fqn ".current.Response"
                                             :type :message
                                             :definition {:package "current"}}}}}
          context {:current-package "current"
                   :ns-aliases {}}
          result (spec-gen/process-field-type field context)]
      ;; Should return keyword for same namespace
      (is (keyword? result))
      (is (= :current/response result)))))

(deftest test-package-mappings-in-context
  (testing "Package mappings are used when provided"
    (let [namespace-data {:messages []
                          :enums []
                          :current-package "test.pkg"
                          :require-specs []
                          ;; Enriched IR provides package mappings
                          :package-mappings {"other.pkg" "other-ns"
                                             "third.pkg" "third-ns"}}
          {:keys [enum-specs message-specs]} (spec-gen/generate-specs-for-namespace namespace-data)]
      ;; The function should complete without error
      (is (string? enum-specs))
      (is (string? message-specs))))
  
  (testing "Falls back to old behavior without package mappings"
    (let [namespace-data {:messages []
                          :enums []
                          :current-package "test.pkg"
                          ;; Old style require specs
                          :require-specs '[[test.pkg.other :as other-alias]
                                           [test.pkg.third :as third-alias]]}
          {:keys [enum-specs message-specs]} (spec-gen/generate-specs-for-namespace namespace-data)]
      ;; Should still work with old behavior
      (is (string? enum-specs))
      (is (string? message-specs)))))