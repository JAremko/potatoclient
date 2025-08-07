(ns generator.real-proto-test
  "Test cross-namespace resolution with real protobuf files"
  (:require [clojure.test :refer [deftest is testing]]
            [clojure.java.io :as io]
            [cheshire.core :as json]
            [generator.backend :as backend]
            [generator.deps :as deps]
            [generator.spec-gen :as spec-gen]))

(defn load-json-descriptor
  "Load a JSON descriptor file"
  [filename]
  (let [file (io/file "../proto-explorer/output/json-descriptors" filename)]
    (when (.exists file)
      (json/parse-string (slurp file) true))))

(deftest test-real-proto-cross-namespace
  (testing "Real protobuf files cross-namespace detection"
    ;; Load a smaller descriptor file
    (when-let [cmd-json (load-json-descriptor "jon_shared_cmd_rotary.json")]
      (let [;; Parse to basic IR
            basic-ir (backend/parse-descriptor-set cmd-json)
            ;; Check if we got valid IR
            _ (is (= :descriptor-set (:type basic-ir)))
            ;; Enrich the IR
            enriched (deps/enrich-descriptor-set basic-ir)
            ;; Check enrichment worked
            _ (is (= :combined (:type enriched)))]
        
        ;; Look for cross-namespace references
        (doseq [file (:files enriched)]
          (let [package (:package file)]
            (doseq [msg (:messages file)]
              (doseq [field (:fields msg)]
                (let [field-type (:type field)]
                  ;; Check for cross-namespace enum refs
                  (when-let [enum-type (:enum field-type)]
                    (when (:cross-namespace enum-type)
                      (println (format "Found cross-namespace enum: %s in %s.%s -> %s (target: %s)"
                                       (:name field)
                                       package
                                       (:proto-name msg)
                                       (:type-ref enum-type)
                                       (:target-package enum-type)))))
                  ;; Check for cross-namespace message refs
                  (when-let [msg-type (:message field-type)]
                    (when (:cross-namespace msg-type)
                      (println (format "Found cross-namespace message: %s in %s.%s -> %s (target: %s)"
                                       (:name field)
                                       package
                                       (:proto-name msg)
                                       (:type-ref msg-type)
                                       (:target-package msg-type))))))))))
        
        ;; Also print summary
        (println "\nSummary:")
        (println "Total files:" (count (:files enriched)))
        (println "Sorted order:" (:sorted-files enriched))
        (println "Symbol registry size:" (count (:symbol-registry enriched)))))))

(deftest test-spec-generation-with-real-protos
  (testing "Spec generation with real cross-namespace references"
    (when-let [cmd-json (load-json-descriptor "jon_shared_cmd_rotary.json")]
      (let [basic-ir (backend/parse-descriptor-set cmd-json)
            enriched (deps/enrich-descriptor-set basic-ir)
            ;; Take the first file for testing
            first-file (first (:files enriched))
            package (:package first-file)
            ;; Build mock package mappings based on dependencies
            package-mappings (into {}
                                   (for [dep-file (:files enriched)
                                         :when (not= (:package dep-file) package)]
                                     [(:package dep-file) 
                                      (str (last (clojure.string/split (:package dep-file) #"\.")))]))
            ;; Generate specs
            spec-result (spec-gen/generate-specs-for-namespace
                         {:messages (:messages first-file)
                          :enums (:enums first-file)
                          :current-package package
                          :package-mappings package-mappings})]
        
        (println "\nGenerated enum specs:")
        (println (:enum-specs spec-result))
        (println "\nGenerated message specs:")
        (println (subs (:message-specs spec-result) 0 (min 500 (count (:message-specs spec-result)))))))))