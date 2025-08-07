(ns generator.integrated-spec-test
  "Tests demonstrating the integrated specs with generators"
  (:require [clojure.test :refer [deftest is testing]]
            [clojure.test.check.clojure-test :refer [defspec]]
            [clojure.test.check.properties :as prop]
            [clojure.test.check.generators :as gen]
            [malli.core :as m]
            [malli.generator :as mg]
            [generator.specs-with-generators :as specs]
            [generator.deps :as deps]))

;; =============================================================================
;; Test Data Variety
;; =============================================================================

(deftest test-regal-generates-diverse-patterns
  (testing "Package names have variety"
    (let [packages (repeatedly 20 #(gen/generate 
                                     (:gen/gen (m/properties specs/FileDef {:registry specs/registry}))
                                     30))]
      ;; Extract unique package names
      (let [unique-packages (set (map :package packages))]
        (is (> (count unique-packages) 5)
            "Should generate diverse package names")
        ;; Check they all match the pattern
        (doseq [pkg unique-packages]
          (is (re-matches #"^[a-z][a-z0-9_]*(\.[a-z][a-z0-9_]*)*$" pkg)
              (str "Package " pkg " should match pattern"))))))
  
  (testing "Proto filenames have variety"
    (let [files (repeatedly 20 #(gen/generate 
                                  (:gen/gen (m/properties specs/FileDef {:registry specs/registry}))
                                  30))]
      (let [unique-names (set (map :name files))]
        (is (> (count unique-names) 5)
            "Should generate diverse filenames")
        (doseq [name unique-names]
          (is (re-matches #"^[a-z_]+\.proto$" name)
              (str "Filename " name " should match pattern"))))))
  
  (testing "Type names follow PascalCase"
    (let [messages (repeatedly 20 #(gen/generate 
                                     (:gen/gen (m/properties specs/MessageDef {:registry specs/registry}))
                                     30))]
      (let [proto-names (map :proto-name messages)]
        (doseq [name proto-names]
          (is (re-matches #"^[A-Z][a-zA-Z0-9]*$" name)
              (str "Type name " name " should be PascalCase")))
        ;; Check variety
        (is (> (count (set proto-names)) 10)
            "Should generate diverse type names"))))
  
  (testing "Field names follow snake_case"
    (let [fields (repeatedly 30 #(gen/generate 
                                   (:gen/gen (m/properties specs/Field {:registry specs/registry}))
                                   30))]
      (let [proto-names (map :proto-name fields)]
        (doseq [name proto-names]
          (is (re-matches #"^[a-z][a-z0-9_]*$" name)
              (str "Field name " name " should be snake_case")))
        ;; Check variety
        (is (> (count (set proto-names)) 15)
            "Should generate diverse field names")))))

;; =============================================================================
;; Property Tests with Integrated Specs
;; =============================================================================

(defspec integrated-enrichment-test
  30
  (prop/for-all [descriptor (mg/generator specs/DescriptorSet {:registry specs/registry})]
    (try
      (let [enriched (deps/enrich-descriptor-set descriptor)]
        (and 
         ;; Basic invariants
         (= :combined (:type enriched))
         (= (count (:files descriptor))
            (count (:files enriched)))
         
         ;; All generated data should be valid
         (m/validate specs/DescriptorSet descriptor {:registry specs/registry})))
      (catch Exception e
        (boolean (re-find #"[Cc]ircular" (.getMessage e)))))))

(defspec type-references-always-valid
  50
  (prop/for-all [type-ref (mg/generator specs/TypeReference {:registry specs/registry})]
    (and (string? type-ref)
         (clojure.string/starts-with? type-ref ".")
         ;; Should have at least package.Type format
         (>= (count (clojure.string/split type-ref #"\.")) 3))))

(defspec consistent-package-names
  30
  (prop/for-all [file (mg/generator specs/FileDef {:registry specs/registry})]
    (let [file-package (:package file)]
      (and
       ;; All messages have the same package
       (every? #(= file-package (:package %)) (:messages file))
       ;; All enums have the same package
       (every? #(= file-package (:package %)) (:enums file))))))

(defspec field-numbers-unique
  30
  (prop/for-all [message (mg/generator specs/MessageDef {:registry specs/registry})]
    (let [field-numbers (map :number (:fields message))]
      (= (count field-numbers)
         (count (set field-numbers))))))

;; =============================================================================
;; Show Generated Examples
;; =============================================================================

(deftest show-generated-examples
  (testing "Show variety in generated data"
    (println "\n=== Sample Generated Package Names ===")
    (doseq [_ (range 5)]
      (let [pkg (:package (gen/generate 
                            (:gen/gen (m/properties specs/FileDef {:registry specs/registry}))
                            30))]
        (println " " pkg)))
    
    (println "\n=== Sample Generated Type References ===")
    (doseq [_ (range 5)]
      (let [ref (gen/generate 
                  (:gen/gen (m/properties specs/TypeReference {:registry specs/registry}))
                  30)]
        (println " " ref)))
    
    (println "\n=== Sample Generated Message Names ===")
    (doseq [_ (range 5)]
      (let [msg (gen/generate 
                  (:gen/gen (m/properties specs/MessageDef {:registry specs/registry}))
                  30)]
        (println "  Proto name:" (:proto-name msg) "-> Clojure name:" (:name msg))))
    
    (println "\n=== Sample Generated Field Names ===")
    (doseq [_ (range 5)]
      (let [field (gen/generate 
                    (:gen/gen (m/properties specs/Field {:registry specs/registry}))
                    30)]
        (println "  Proto name:" (:proto-name field) "-> Clojure name:" (:name field))))
    
    (println "\n=== Sample Complete File ===")
    (let [file (gen/generate 
                 (:gen/gen (m/properties specs/FileDef {:registry specs/registry}))
                 50)]
      (println "File:" (:name file))
      (println "Package:" (:package file))
      (println "Dependencies:" (:dependencies file))
      (println "Messages:" (count (:messages file)))
      (doseq [msg (:messages file)]
        (println "  -" (:proto-name msg) "with" (count (:fields msg)) "fields"))
      (println "Enums:" (count (:enums file)))
      (doseq [enum (:enums file)]
        (println "  -" (:proto-name enum) "with" (count (:values enum)) "values")))
    
    ;; This test always passes, it's just for demonstration
    (is true)))