(ns generator.debug-enum-test
  "Debug enum handling"
  (:require [clojure.test :refer :all]
            [generator.core :as core]
            [malli.registry :as mr]
            [malli.core :as m]
            [potatoclient.specs.malli-oneof :as oneof]))

;; Set up registry
(mr/set-default-registry!
  (merge (m/default-schemas)
         (mr/schemas m/default-registry)
         {:oneof oneof/-oneof-schema}))

(deftest test-enum-generation
  (testing "Enum value generation and lookup"
    ;; Generate code
    (let [result (core/generate-all {:input-dir "../proto-explorer/output/json-descriptors"
                                     :output-dir "test-enum-output"
                                     :namespace-prefix "test.enum"
                                     :debug? false
                                     :guardrails? true})]
      (is (:success result)))
    
    ;; Load the generated types namespace
    (load-file "test-enum-output/test/enum/ser.clj")
    
    ;; Check if enum values exist
    (let [types-ns (find-ns 'test.enum.ser)]
      (is (some? types-ns) "Types namespace should exist")
      
      (when types-ns
        (let [enum-values-var (ns-resolve types-ns 'jon-gui-data-client-type-values)
              enum-keywords-var (ns-resolve types-ns 'jon-gui-data-client-type-keywords)]
          
          (is (some? enum-values-var) "Enum values var should exist")
          (is (some? enum-keywords-var) "Enum keywords var should exist")
          
          (when (and enum-values-var enum-keywords-var)
            (let [enum-values @enum-values-var
                  enum-keywords @enum-keywords-var]
              
              (println "Enum values map:" enum-values)
              (println "Enum keywords map:" enum-keywords)
              
              ;; Test lookups
              (is (map? enum-values) "Enum values should be a map")
              (is (map? enum-keywords) "Enum keywords should be a map")
              
              ;; Test specific lookup
              (let [local-network-key :jon-gui-data-client-type-local-network]
                (is (contains? enum-values local-network-key)
                    (str "Should contain " local-network-key))
                
                ;; Test nil lookup
                (is (nil? (get enum-values nil))
                    "Looking up nil should return nil")))))))))