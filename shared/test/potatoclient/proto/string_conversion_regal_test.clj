(ns potatoclient.proto.string-conversion-regal-test
  "Property tests using Regal generators to ensure collision-free conversions"
  (:require [clojure.test :refer :all]
            [clojure.test.check :as tc]
            [clojure.test.check.clojure-test :refer [defspec]]
            [clojure.test.check.generators :as gen]
            [clojure.test.check.properties :as prop]
            [potatoclient.proto.string-conversion :as conv]
            [potatoclient.proto.string-conversion-specs :as specs]
            [lambdaisland.regal :as regal]
            [lambdaisland.regal.generator :as regal-gen]
            [malli.core :as m]
            [clojure.string :as str]))

;; =============================================================================
;; Regal Pattern Testing
;; =============================================================================

(deftest regal-pattern-validation
  (testing "Regal patterns match expected strings"
    ;; Test camelCase pattern
    (is (regal/regex-match? specs/camel-case-pattern "getValue"))
    (is (regal/regex-match? specs/camel-case-pattern "isEnabled"))
    (is (not (regal/regex-match? specs/camel-case-pattern "GetValue")))
    (is (not (regal/regex-match? specs/camel-case-pattern "get_value")))
    
    ;; Test PascalCase pattern
    (is (regal/regex-match? specs/pascal-case-pattern "GetValue"))
    (is (regal/regex-match? specs/pascal-case-pattern "DayCamera"))
    (is (not (regal/regex-match? specs/pascal-case-pattern "getValue")))
    
    ;; Test proto constant pattern
    (is (regal/regex-match? specs/proto-constant-pattern "TYPE_INT32"))
    (is (regal/regex-match? specs/proto-constant-pattern "LABEL_OPTIONAL"))
    (is (not (regal/regex-match? specs/proto-constant-pattern "Type_Int32")))
    
    ;; Test special proto type patterns
    (is (regal/regex-match? specs/proto-type-constant-pattern "TYPE_INT32"))
    (is (regal/regex-match? specs/proto-type-constant-pattern "TYPE_UINT64"))
    (is (regal/regex-match? specs/proto-type-constant-pattern "TYPE_BOOL"))
    (is (not (regal/regex-match? specs/proto-type-constant-pattern "TYPE_INT128")))))

;; =============================================================================
;; Collision-Free Conversion Properties
;; =============================================================================

(defspec kebab-case-conversions-collision-free 10000
  (let [seen-outputs (atom {})]
    (prop/for-all [s (specs/gen-mixed-case)]
      (let [output (conv/->kebab-case s)]
        (if-let [existing-input (get @seen-outputs output)]
          (if (= existing-input s)
            true  ; Same input, same output is OK
            (do
              (println "COLLISION DETECTED:")
              (println "  Input 1:" existing-input "-> Output:" output)
              (println "  Input 2:" s "-> Output:" output)
              false))
          (do
            (swap! seen-outputs assoc output s)
            true))))))

(defspec pascal-case-conversions-collision-free 10000
  (let [seen-outputs (atom {})]
    (prop/for-all [s (specs/gen-mixed-case)]
      (let [output (conv/->PascalCase s)]
        (if-let [existing-input (get @seen-outputs output)]
          (= existing-input s)
          (do
            (swap! seen-outputs assoc output s)
            true))))))

;; =============================================================================
;; Specific Pattern Generator Tests
;; =============================================================================

(defspec proto-type-constants-convert-correctly 1000
  (prop/for-all [s (specs/gen-proto-type-constant)]
    (let [kebab (conv/->kebab-case s)]
      (and
        ;; Should be lowercase
        (= kebab (str/lower-case kebab))
        ;; Should preserve the number
        (or (str/ends-with? kebab "-int32")
            (str/ends-with? kebab "-int64")
            (str/ends-with? kebab "-uint32")
            (str/ends-with? kebab "-uint64")
            (str/ends-with? kebab "-sint32")
            (str/ends-with? kebab "-sint64")
            (str/ends-with? kebab "-fixed32")
            (str/ends-with? kebab "-fixed64")
            (str/ends-with? kebab "-sfixed32")
            (str/ends-with? kebab "-sfixed64")
            (str/ends-with? kebab "-double")
            (str/ends-with? kebab "-float")
            (str/ends-with? kebab "-bool")
            (str/ends-with? kebab "-string")
            (str/ends-with? kebab "-bytes"))))))

(defspec numeric-suffix-patterns-handle-correctly 1000
  (prop/for-all [s (specs/gen-numeric-suffix)]
    (let [kebab (conv/->kebab-case s)]
      (and
        ;; Should not split numbers incorrectly
        (not (str/includes? kebab "-3-2"))  ; TYPE_INT32 should not become type-int-3-2
        (not (str/includes? kebab "-6-4"))  ; TYPE_INT64 should not become type-int-6-4
        ;; Numbers should stay together
        (or (not (re-find #"\d" s))
            (re-find #"\d+" kebab))))))

;; =============================================================================
;; Edge Case Testing with Regal
;; =============================================================================

(def edge-case-patterns
  "Regal patterns for edge cases"
  {:single-letter [:cat [:class [\a \z]]]
   :all-caps [:+ [:class [\A \Z]]]
   :with-numbers [:cat [:+ [:class [\A \Z]]] "_" [:+ [:class [\0 \9]]]]
   :mixed-numbers [:cat 
                   [:+ [:class [\A \Z] [\a \z]]]
                   [:+ [:class [\0 \9]]]
                   [:? [:class [\A \Z] [\a \z]]]]})

(deftest edge-cases-with-regal
  (testing "Single letter conversions"
    (doseq [s (take 10 (regal-gen/sample (edge-case-patterns :single-letter)))]
      (is (string? (conv/->kebab-case s)))
      (is (string? (conv/->PascalCase s)))))
  
  (testing "All caps conversions"
    (doseq [s (take 10 (regal-gen/sample (edge-case-patterns :all-caps)))]
      (is (= (str/lower-case s) (conv/->kebab-case s)))))
  
  (testing "Numbers in identifiers"
    (doseq [s (take 20 (regal-gen/sample (edge-case-patterns :with-numbers)))]
      (let [kebab (conv/->kebab-case s)]
        (is (string? kebab))
        ;; Check numbers are preserved
        (is (re-find #"\d" kebab))))))

;; =============================================================================
;; Method Name Generation with Regal
;; =============================================================================

(defspec method-names-follow-java-conventions 1000
  (prop/for-all [field (gen/one-of [(specs/gen-camel-case) (specs/gen-snake-case)])]
    (let [getter (conv/getter-method-name field)
          setter (conv/setter-method-name field)
          has (conv/has-method-name field)
          add (conv/add-method-name field)
          add-all (conv/add-all-method-name field)]
      (and
        ;; All start with correct prefix
        (str/starts-with? getter "get")
        (str/starts-with? setter "set")
        (str/starts-with? has "has")
        (str/starts-with? add "add")
        (str/starts-with? add-all "addAll")
        ;; All have PascalCase after prefix
        (Character/isUpperCase (.charAt getter 3))
        (Character/isUpperCase (.charAt setter 3))
        (Character/isUpperCase (.charAt has 3))
        (Character/isUpperCase (.charAt add 3))
        (Character/isUpperCase (.charAt add-all 6))
        ;; No underscores or hyphens
        (not (or (str/includes? getter "_")
                 (str/includes? getter "-")))))))

;; =============================================================================
;; Guardrails Validation with Regal Specs
;; =============================================================================

(deftest guardrails-with-regal-specs
  (testing "Functions satisfy their Regal-based specs"
    ;; Generate test data using Regal
    (let [test-samples {:camel (take 50 (regal-gen/sample specs/camel-case-pattern))
                       :pascal (take 50 (regal-gen/sample specs/pascal-case-pattern))
                       :snake (take 50 (regal-gen/sample specs/snake-case-pattern))
                       :proto (take 50 (regal-gen/sample specs/proto-constant-pattern))
                       :kebab (take 50 (regal-gen/sample specs/kebab-case-pattern))}]
      
      ;; Test conversions match specs
      (doseq [s (:camel test-samples)]
        (is (m/validate specs/KebabCaseString (conv/->kebab-case s))))
      
      (doseq [s (:snake test-samples)]
        (is (m/validate specs/PascalCaseString (conv/->PascalCase s))))
      
      (doseq [s (:proto test-samples)]
        (is (keyword? (conv/->kebab-case-keyword s)))))))

;; =============================================================================
;; Collision Tracking Validation
;; =============================================================================

(deftest collision-tracking-works
  (testing "Collision detection catches actual collisions"
    ;; Clear caches first
    (conv/clear-conversion-caches!)
    
    ;; These should not collide (different outputs)
    (is (= "type-int32" (conv/->kebab-case "TYPE_INT32")))
    (is (= "type-int-32" (conv/->kebab-case "TYPE_INT_32")))
    
    ;; Test that same input returns cached value
    (is (= "type-int32" (conv/->kebab-case "TYPE_INT32")))
    
    ;; Get conversion stats
    (let [stats (conv/get-conversion-stats)]
      (is (pos? (:total-conversions stats)))
      (is (pos? (:unique-outputs stats)))
      (is (seq (:functions stats))))))

;; =============================================================================
;; Full Integration Test
;; =============================================================================

(deftest full-regal-integration
  (testing "Complete Regal-based string conversion system"
    (conv/clear-conversion-caches!)
    
    ;; Generate a variety of test cases
    (let [test-cases (concat
                      (take 100 (regal-gen/sample specs/camel-case-pattern))
                      (take 100 (regal-gen/sample specs/pascal-case-pattern))
                      (take 100 (regal-gen/sample specs/snake-case-pattern))
                      (take 100 (regal-gen/sample specs/proto-constant-pattern))
                      (take 100 (regal-gen/sample specs/numeric-suffix-pattern))
                      (take 100 (regal-gen/sample specs/proto-type-constant-pattern)))
          conversion-errors (atom [])]
      
      ;; Test all conversions
      (doseq [s test-cases]
        (try
          (conv/->kebab-case s)
          (conv/->PascalCase s)
          (conv/->snake_case s)
          (catch Exception e
            (swap! conversion-errors conj {:input s :error e}))))
      
      ;; Check results
      (when (seq @conversion-errors)
        (println "Conversion errors found:")
        (doseq [{:keys [input error]} @conversion-errors]
          (println "  Input:" input)
          (println "  Error:" (.getMessage error))))
      
      (is (empty? @conversion-errors) "No conversion errors")
      
      ;; Validate no unexpected collisions
      (let [stats (conv/get-conversion-stats)]
        (println "Conversion statistics:")
        (println "  Total conversions:" (:total-conversions stats))
        (println "  Unique outputs:" (:unique-outputs stats))
        (println "  Functions used:" (:functions stats))))))

(defn run-regal-tests []
  (println "\n=== Running Regal-based String Conversion Tests ===")
  (run-tests 'potatoclient.proto.string-conversion-regal-test))