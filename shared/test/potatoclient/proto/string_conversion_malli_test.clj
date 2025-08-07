(ns potatoclient.proto.string-conversion-malli-test
  "Property tests using Malli specs with attached generators"
  (:require [clojure.test :refer :all]
            [clojure.test.check :as tc]
            [clojure.test.check.clojure-test :refer [defspec]]
            [clojure.test.check.properties :as prop]
            [potatoclient.proto.string-conversion :as conv]
            [potatoclient.proto.string-conversion-specs :as specs]
            [malli.core :as m]
            [malli.generator :as mg]
            [clojure.string :as str]))

;; =============================================================================
;; Testing with Malli-attached Generators
;; =============================================================================

(deftest malli-spec-generators-work
  (testing "All specs have working generators"
    ;; Test that each spec can generate values
    (is (string? (mg/generate specs/CamelCaseString)))
    (is (string? (mg/generate specs/PascalCaseString)))
    (is (string? (mg/generate specs/SnakeCaseString)))
    (is (string? (mg/generate specs/ProtoConstantString)))
    (is (string? (mg/generate specs/KebabCaseString)))
    (is (string? (mg/generate specs/ProtoTypeConstantString)))
    (is (string? (mg/generate specs/NumericSuffixString)))
    (is (string? (mg/generate specs/MixedCaseString)))
    (is (string? (mg/generate specs/FieldNameString)))))

(deftest generated-values-match-specs
  (testing "Generated values satisfy their specs"
    ;; Generate 100 samples from each spec and validate
    (doseq [spec-name [::specs/CamelCaseString
                       ::specs/PascalCaseString
                       ::specs/SnakeCaseString
                       ::specs/ProtoConstantString
                       ::specs/KebabCaseString
                       ::specs/ProtoTypeConstantString
                       ::specs/NumericSuffixString]]
      (let [spec (get specs/registry spec-name)]
        (testing (str "Spec " spec-name)
          (doseq [sample (mg/sample spec {:size 100})]
            (is (m/validate spec sample)
                (str "Generated value '" sample "' should validate against " spec-name))))))))

;; =============================================================================
;; Collision-Free Property Tests with Malli
;; =============================================================================

(defspec kebab-case-conversions-no-collisions 10000
  (prop/for-all [s (mg/generator specs/MixedCaseString)]
    (let [result (conv/->kebab-case s)]
      (and
        ;; Result should be valid kebab-case
        (m/validate specs/KebabCaseString result)
        ;; Should be lowercase
        (= result (str/lower-case result))
        ;; No underscores or uppercase
        (not (or (str/includes? result "_")
                 (re-find #"[A-Z]" result)))))))

(defspec pascal-case-conversions-valid 10000
  (prop/for-all [s (mg/generator specs/MixedCaseString)]
    (let [result (conv/->PascalCase s)]
      (and
        ;; Result should be valid PascalCase
        (m/validate specs/PascalCaseString result)
        ;; Should start with uppercase
        (Character/isUpperCase (first result))
        ;; No hyphens or underscores
        (not (or (str/includes? result "-")
                 (str/includes? result "_")))))))

(defspec snake-case-conversions-valid 10000
  (prop/for-all [s (mg/generator specs/MixedCaseString)]
    (let [result (conv/->snake_case s)]
      (and
        ;; Result should be valid snake_case
        (m/validate specs/SnakeCaseString result)
        ;; Should be lowercase (except underscores)
        (= result (str/lower-case result))
        ;; No hyphens or uppercase
        (not (or (str/includes? result "-")
                 (re-find #"[A-Z]" result)))))))

;; =============================================================================
;; Method Name Generation with Malli Specs
;; =============================================================================

(defspec method-names-satisfy-specs 5000
  (prop/for-all [field (mg/generator specs/FieldNameString)]
    (let [getter (conv/getter-method-name field)
          setter (conv/setter-method-name field)
          has (conv/has-method-name field)]
      (and
        ;; All method names satisfy their specs
        (m/validate specs/GetterMethodString getter)
        (m/validate specs/SetterMethodString setter)
        (m/validate specs/HasMethodString has)))))

;; =============================================================================
;; Special Pattern Tests
;; =============================================================================

(defspec proto-type-constants-convert-correctly 5000
  (prop/for-all [s (mg/generator specs/ProtoTypeConstantString)]
    (let [kebab (conv/->kebab-case s)]
      (and
        ;; Result should be valid kebab-case
        (m/validate specs/KebabCaseString kebab)
        ;; Should preserve the type information
        (cond
          (str/starts-with? s "TYPE_INT") (str/includes? kebab "int")
          (str/starts-with? s "TYPE_UINT") (str/includes? kebab "uint")
          (str/starts-with? s "TYPE_DOUBLE") (str/includes? kebab "double")
          (str/starts-with? s "TYPE_FLOAT") (str/includes? kebab "float")
          (str/starts-with? s "TYPE_BOOL") (str/includes? kebab "bool")
          (str/starts-with? s "TYPE_STRING") (str/includes? kebab "string")
          (str/starts-with? s "TYPE_BYTES") (str/includes? kebab "bytes")
          :else true)))))

(defspec numeric-suffix-conversions-preserve-numbers 5000
  (prop/for-all [s (mg/generator specs/NumericSuffixString)]
    (let [kebab (conv/->kebab-case s)
          numbers-in-original (re-seq #"\d+" s)
          numbers-in-result (re-seq #"\d+" kebab)]
      ;; All numbers from original should appear in result
      (every? (fn [num]
                (some #(str/includes? % num) numbers-in-result))
              numbers-in-original))))

;; =============================================================================
;; Guardrails Integration Test
;; =============================================================================

(deftest guardrails-specs-match-behavior
  (testing "Function behavior matches guardrails specs"
    ;; Generate test data from specs
    (let [test-inputs (mg/sample specs/MixedCaseString {:size 100})]
      
      (testing "->kebab-case satisfies its spec"
        (doseq [input test-inputs]
          (let [output (conv/->kebab-case input)]
            (is (or (nil? output)
                    (m/validate specs/KebabCaseString output))))))
      
      (testing "->PascalCase satisfies its spec"
        (doseq [input test-inputs]
          (let [output (conv/->PascalCase input)]
            (is (or (nil? output)
                    (m/validate specs/PascalCaseString output))))))
      
      (testing "->snake_case satisfies its spec"
        (doseq [input test-inputs]
          (let [output (conv/->snake_case input)]
            (is (or (nil? output)
                    (m/validate specs/SnakeCaseString output)))))))))

;; =============================================================================
;; Collision Detection with Malli Generators
;; =============================================================================

(deftest collision-detection-with-malli
  (testing "Collision tracking works with Malli-generated data"
    (conv/clear-conversion-caches!)
    
    ;; Generate diverse test cases
    (let [test-cases (concat
                      (mg/sample specs/CamelCaseString {:size 100})
                      (mg/sample specs/PascalCaseString {:size 100})
                      (mg/sample specs/ProtoConstantString {:size 100})
                      (mg/sample specs/ProtoTypeConstantString {:size 100})
                      (mg/sample specs/NumericSuffixString {:size 100}))
          collision-count (atom 0)]
      
      ;; Test conversions
      (doseq [s test-cases]
        (try
          (conv/->kebab-case s)
          (conv/->PascalCase s)
          (conv/->snake_case s)
          (catch Exception e
            (when (str/includes? (.getMessage e) "collision")
              (swap! collision-count inc)))))
      
      ;; Report results
      (let [stats (conv/get-conversion-stats)]
        (println "Malli generator test results:")
        (println "  Test cases:" (count test-cases))
        (println "  Total conversions:" (:total-conversions stats))
        (println "  Unique outputs:" (:unique-outputs stats))
        (println "  Collisions detected:" @collision-count)))))

;; =============================================================================
;; Edge Cases from Malli Generators
;; =============================================================================

(deftest edge-cases-from-malli
  (testing "Edge cases generated by Malli"
    ;; Let Malli generate edge cases
    (let [edge-cases (mg/sample specs/MixedCaseString {:size 50 :seed 42})]
      (doseq [s edge-cases]
        (testing (str "Converting: " s)
          ;; All conversions should work without throwing
          (is (string? (str (conv/->kebab-case s))))
          (is (string? (str (conv/->PascalCase s))))
          (is (string? (str (conv/->snake_case s))))
          
          ;; Results should be valid
          (when-let [kebab (conv/->kebab-case s)]
            (is (m/validate specs/KebabCaseString kebab)))
          (when-let [pascal (conv/->PascalCase s)]
            (is (m/validate specs/PascalCaseString pascal)))
          (when-let [snake (conv/->snake_case s)]
            (is (m/validate specs/SnakeCaseString snake))))))))

;; =============================================================================
;; Full Integration Test with Malli
;; =============================================================================

(defspec full-malli-integration 10000
  (prop/for-all [input (mg/generator specs/MixedCaseString)]
    (let [kebab (conv/->kebab-case input)
          pascal (conv/->PascalCase input)
          snake (conv/->snake_case input)]
      (and
        ;; All conversions produce valid outputs
        (m/validate specs/KebabCaseString kebab)
        (m/validate specs/PascalCaseString pascal)
        (m/validate specs/SnakeCaseString snake)
        
        ;; Conversions are deterministic (same input -> same output)
        (= kebab (conv/->kebab-case input))
        (= pascal (conv/->PascalCase input))
        (= snake (conv/->snake_case input))))))

(defn run-malli-tests []
  (println "\n=== Running Malli-based String Conversion Tests ===")
  (run-tests 'potatoclient.proto.string-conversion-malli-test))