(ns generator.realistic-spec-test
  "Tests demonstrating realistic data generation with integrated specs"
  (:require [clojure.test :refer [deftest is testing]]
            [clojure.test.check.generators :as gen]
            [malli.core :as m]
            [malli.generator :as mg]
            [generator.specs-with-generators-v2 :as specs]
            [generator.deps :as deps]))

(deftest show-realistic-examples
  (testing "Show realistic generated data"
    (println "\n=== Realistic Package Names ===")
    (doseq [_ (range 10)]
      (let [pkg (:package (gen/generate 
                            (:gen/gen (m/properties specs/FileDef {:registry specs/registry}))
                            30))]
        (println " " pkg)))
    
    (println "\n=== Realistic Type References ===")
    (doseq [_ (range 10)]
      (let [ref (gen/generate 
                  (:gen/gen (m/properties specs/TypeReference {:registry specs/registry}))
                  30)]
        (println " " ref)))
    
    (println "\n=== Realistic Message Definitions ===")
    (doseq [_ (range 5)]
      (let [msg (gen/generate 
                  (:gen/gen (m/properties specs/MessageDef {:registry specs/registry}))
                  30)]
        (println "\nMessage:" (:proto-name msg))
        (println "Package:" (:package msg))
        (println "Fields:")
        (doseq [field (:fields msg)]
          (println (str "  - " (:proto-name field) 
                        " (" (name (:label field)) " " 
                        (or (get-in field [:type :scalar])
                            (get-in field [:type :message :type-ref])
                            (get-in field [:type :enum :type-ref]))
                        ")")))))
    
    (println "\n=== Realistic Enum Definitions ===")
    (doseq [_ (range 3)]
      (let [enum (gen/generate 
                   (:gen/gen (m/properties specs/EnumDef {:registry specs/registry}))
                   30)]
        (println "\nEnum:" (:proto-name enum))
        (println "Values:" (map :proto-name (:values enum)))))
    
    (println "\n=== Complete Realistic File ===")
    (let [file (gen/generate 
                 (:gen/gen (m/properties specs/FileDef {:registry specs/registry}))
                 50)]
      (println "\n```proto")
      (println "// File:" (:name file))
      (println "syntax = \"proto3\";")
      (println)
      (println "package" (str (:package file) ";"))
      (when (seq (:dependencies file))
        (println)
        (doseq [dep (:dependencies file)]
          (println "import" (str "\"" dep "\";"))))
      (println)
      
      ;; Print enums
      (doseq [enum (:enums file)]
        (println "enum" (:proto-name enum) "{")
        (doseq [value (:values enum)]
          (println "  " (:proto-name value) "=" (:number value) ";"))
        (println "}")
        (println))
      
      ;; Print messages
      (doseq [msg (:messages file)]
        (println "message" (:proto-name msg) "{")
        (doseq [field (:fields msg)]
          (let [label (case (:label field)
                        :label-required "required"
                        :label-repeated "repeated"
                        :label-optional "")
                type-str (or (when-let [scalar (get-in field [:type :scalar])]
                               (name scalar))
                             (last (clojure.string/split 
                                     (or (get-in field [:type :message :type-ref])
                                         (get-in field [:type :enum :type-ref])
                                         "Unknown")
                                     #"\.")))]
            (println " " (if (empty? label) "" (str label " "))
                     type-str (:proto-name field) "=" (:number field) ";")))
        (println "}")
        (println))
      
      (println "```"))
    
    ;; This test always passes, it's just for demonstration
    (is true)))

(deftest test-realistic-data-properties
  (testing "Generated packages look realistic"
    (let [packages (repeatedly 50 #(:package (gen/generate 
                                               (:gen/gen (m/properties specs/FileDef {:registry specs/registry}))
                                               30)))]
      ;; All should be valid package names
      (doseq [pkg packages]
        (is (re-matches #"^[a-z][a-z0-9]*(\.(?:v\d+|[a-z][a-z0-9]*))*$" pkg)))
      
      ;; Should use common components
      (let [all-parts (mapcat #(clojure.string/split % #"\.") packages)
            common-parts #{"com" "org" "io" "api" "service" "proto" "v1" "v2"}]
        (is (> (count (filter common-parts all-parts)) 20)
            "Should frequently use common package components"))))
  
  (testing "Generated field names look realistic"
    (let [fields (repeatedly 50 #(gen/generate 
                                   (:gen/gen (m/properties specs/Field {:registry specs/registry}))
                                   30))
          names (map :proto-name fields)]
      ;; Check for common patterns
      (is (some #(re-matches #".*_id$" %) names) "Should have fields ending in _id")
      (is (some #(re-matches #".*_at$" %) names) "Should have fields ending in _at")
      (is (some #(re-matches #"created_.*" %) names) "Should have created_ fields")
      (is (some #{"id" "name" "email" "status" "type"} names) "Should have common field names")))
  
  (testing "Generated messages have sensible structure"
    (let [messages (repeatedly 20 #(gen/generate 
                                     (:gen/gen (m/properties specs/MessageDef {:registry specs/registry}))
                                     30))]
      ;; Check for Request/Response patterns
      (is (some #(re-matches #".*Request$" (:proto-name %)) messages))
      (is (some #(re-matches #".*Response$" (:proto-name %)) messages))
      
      ;; Check field counts are reasonable
      (let [field-counts (map #(count (:fields %)) messages)]
        (is (every? #(and (>= % 1) (<= % 10)) field-counts)
            "Messages should have 1-10 fields")))))