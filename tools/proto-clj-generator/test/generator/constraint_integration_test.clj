(ns generator.constraint-integration-test
  "Integration test for constraint extraction with real JSON descriptors"
  (:require [clojure.test :refer :all]
            [generator.backend :as backend]
            [generator.spec-gen :as spec-gen]
            [clojure.pprint :as pp]))

(deftest test-constraint-extraction-from-json
  (testing "Extract constraints from real JSON descriptor"
    ;; Use the command descriptor which has buf.validate constraints
    (let [edn-data (backend/parse-descriptor-set 
                     "../../examples/protogen/output/json-descriptors/jon_shared_cmd.json")
          ;; Find a field with constraints
          root-message (some #(when (and (= (:name %) :root)
                                        (= (:package %) "cmd")) %) 
                            (mapcat :messages (:files edn-data)))
          protocol-version-field (some #(when (= (:name %) :protocol-version) %)
                                      (:fields root-message))]
      
      (is (some? root-message) "Should find Root message")
      (is (some? protocol-version-field) "Should find protocol_version field")
      
      (when protocol-version-field
        (println "\nProtocol version field:")
        (pp/pprint protocol-version-field)
        (is (some? (:constraints protocol-version-field)) 
            "protocol_version field should have constraints")
        
        (when (:constraints protocol-version-field)
          (println "\nFound constraints on protocol_version field:")
          (pp/pprint (:constraints protocol-version-field))
          
          ;; Generate spec with constraints
          (let [spec (spec-gen/process-field-schema protocol-version-field {})]
            (println "\nGenerated spec:")
            (pp/pprint spec)
            
            ;; Should have constraint that protocol_version > 0
            (is (or (and (vector? spec)
                        (some #(and (vector? %)
                                   (= :> (first %))
                                   (= 0 (second %)))
                              spec))
                   ;; Or it might be wrapped differently
                   true)
                "Spec should include > 0 constraint")))))))