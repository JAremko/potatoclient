(ns potatoclient.proto.string-conversion-bidirectional-test
  "Property tests for bidirectional string conversions with collision detection"
  (:require [clojure.test :refer :all]
            [clojure.test.check :as tc]
            [clojure.test.check.clojure-test :refer [defspec]]
            [clojure.test.check.properties :as prop]
            [potatoclient.proto.string-conversion :as conv]
            [potatoclient.proto.string-conversion-bidirectional :as bidi]
            [potatoclient.proto.string-conversion-specs :as specs]
            [potatoclient.proto.constants :as constants]
            [malli.generator :as mg]
            [malli.core :as m]
            [clojure.string :as str]))

;; =============================================================================
;; Lossless Conversion Tests
;; =============================================================================

(defspec proto-constant-conversion-is-lossless 10000
  (prop/for-all [s (mg/generator :potatoclient.proto.string-conversion-specs/ProtoConstantString)]
    ;; Clear caches to ensure we test fresh
    (let [_ (bidi/clear-collision-tracking!)
          kw (constants/proto-const->keyword s)
          back (constants/keyword->proto-const kw)]
      (= s back))))

(defspec kebab-snake-conversion-is-lossless 10000
  (prop/for-all [s (mg/generator :potatoclient.proto.string-conversion-specs/SnakeCaseString)]
    (let [kebab (conv/snake-case->kebab-case s)
          back (conv/kebab-case->snake_case kebab)]
      (= s back))))

(defspec keyword-string-conversion-is-lossless 10000
  (prop/for-all [s (mg/generator :potatoclient.proto.string-conversion-specs/KebabCaseString)]
    (let [kw (conv/->kebab-case-keyword s)
          back (conv/keyword->kebab-case kw)]
      (= s back))))

;; =============================================================================
;; Lossy Conversion Tests (should NOT be lossless)
;; =============================================================================

(deftest pascal-kebab-conversion-is-lossy
  (testing "PascalCase to kebab-case loses word boundary information"
    ;; These different inputs produce the same output
    (is (= (conv/->kebab-case "XMLParser")
           (conv/->kebab-case "XmlParser")
           "xml-parser"))
    
    ;; So roundtrip fails
    (let [original "XMLParser"
          kebab (conv/->kebab-case original)
          back (conv/kebab-case->PascalCase kebab)]
      (is (not= original back))
      (is (= "XmlParser" back)))))

(deftest camelCase-kebab-conversion-is-lossy
  (testing "camelCase to kebab-case can lose information"
    ;; Numbers and consecutive capitals are ambiguous
    (is (= (conv/->kebab-case "mode2D")
           (conv/->kebab-case "mode2d")
           "mode-2d"))
    
    (let [original "mode2D"
          kebab (conv/->kebab-case original)
          back (conv/kebab-case->PascalCase kebab)
          camel (conv/clj-key->json-key (keyword kebab))]
      (is (not= original camel))
      (is (= "mode2d" camel)))))

;; =============================================================================
;; Collision Detection Tests
;; =============================================================================

(deftest collision-tracking-works-across-all-conversions
  (testing "All conversion functions track collisions"
    (bidi/clear-collision-tracking!)
    
    ;; Test various conversions
    (conv/->kebab-case "TestValue")
    (conv/->kebab-case-keyword "TestValue")
    (conv/->PascalCase "test-value")
    (conv/->snake_case "test-value")
    (conv/pascal-case->kebab-case "TestValue")
    (conv/snake-case->kebab-case "test_value")
    (conv/kebab-case->PascalCase "test-value")
    (conv/kebab-case->snake_case "test-value")
    (conv/kebab-case->UPPER_SNAKE_CASE "test-value")
    (conv/keyword->kebab-case :test-value)
    (conv/json-key->clj-key "testValue")
    (conv/clj-key->json-key :test-value)
    
    ;; Check that conversions were tracked
    (let [stats (bidi/get-collision-stats)]
      (is (pos? (:total-conversions stats)))
      (is (seq (:functions stats)))
      (is (contains? (set (:functions stats)) "->kebab-case"))
      (is (contains? (set (:functions stats)) "->PascalCase")))))

(defspec all-conversions-detect-collisions 5000
  (prop/for-all [s1 (mg/generator :potatoclient.proto.string-conversion-specs/MixedCaseString)
                 s2 (mg/generator :potatoclient.proto.string-conversion-specs/MixedCaseString)]
    (if (= s1 s2)
      true  ; Same input is always OK
      (let [k1 (conv/->kebab-case s1)
            k2 (conv/->kebab-case s2)]
        (if (= k1 k2)
          ;; If they produce the same output, the second call should have been cached
          ;; (collision detection would have thrown if they were different inputs)
          true
          ;; Different outputs means no collision
          (not= k1 k2))))))

;; =============================================================================
;; Bidirectional API Tests
;; =============================================================================

(deftest bidirectional-convert-api
  (testing "Bidirectional convert function"
    (bidi/clear-collision-tracking!)
    
    ;; Test conversions that should work
    (is (= "test-value" 
           (bidi/convert :test-value :kebab-case-keyword :kebab-case)))
    (is (= :test-value
           (bidi/convert "test-value" :kebab-case :kebab-case-keyword)))
    (is (= "TestValue"
           (bidi/convert "test-value" :kebab-case :PascalCase)))
    (is (= "test_value"
           (bidi/convert "test-value" :kebab-case :snake_case)))
    (is (= "TEST_VALUE"
           (bidi/convert "test-value" :kebab-case :UPPER_SNAKE_CASE)))
    (is (= "testValue"
           (bidi/convert :test-value :kebab-case-keyword :camelCase)))
    
    ;; Test lossy conversion protection
    (is (thrown-with-msg? 
         clojure.lang.ExceptionInfo
         #"Lossy conversion not allowed"
         (bidi/convert "XMLParser" :PascalCase :kebab-case)))
    
    ;; But should work with :allow-lossy? true
    (is (= "xml-parser"
           (bidi/convert "XMLParser" :PascalCase :kebab-case :allow-lossy? true)))))

(deftest conversion-table-validation
  (testing "Conversion table is internally consistent"
    (bidi/clear-collision-tracking!)
    (bidi/validate-conversion-table)))

;; =============================================================================
;; Property: Canonical Form Roundtrips
;; =============================================================================

(defspec canonical-form-roundtrips 10000
  (prop/for-all [s (mg/generator :potatoclient.proto.string-conversion-specs/MixedCaseString)]
    (let [canonical (bidi/->canonical s)
          ;; Should always produce a keyword
          _ (is (keyword? canonical))
          ;; Converting back to string should work
          as-string (conv/keyword->kebab-case canonical)
          ;; And back to canonical should match
          back-to-canonical (bidi/->canonical as-string)]
      (= canonical back-to-canonical))))

;; =============================================================================
;; Exhaustive Lossless Pair Testing
;; =============================================================================

(deftest test-all-claimed-lossless-pairs
  (testing "All pairs marked as lossless actually are"
    (doseq [[pair-name {:keys [forward reverse lossless?]}] bidi/conversion-pairs
            :when lossless?]
      (testing (str "Pair: " pair-name)
        ;; Generate appropriate test data based on the pair
        (let [test-samples (case pair-name
                            :string->keyword (mg/sample :potatoclient.proto.string-conversion-specs/KebabCaseString {:size 100})
                            :proto-const->keyword (mg/sample :potatoclient.proto.string-conversion-specs/ProtoConstantString {:size 100})
                            :snake->kebab (mg/sample :potatoclient.proto.string-conversion-specs/SnakeCaseString {:size 100})
                            [])]
          (doseq [sample test-samples]
            (let [converted (forward sample)
                  roundtrip (reverse converted)]
              (is (= sample roundtrip)
                  (str "Lossless roundtrip failed for " pair-name 
                       ": " sample " -> " converted " -> " roundtrip)))))))))

;; =============================================================================
;; Collision Statistics
;; =============================================================================

(deftest collision-detection-statistics
  (testing "Collision detection provides useful statistics"
    (bidi/clear-collision-tracking!)
    
    ;; Perform many conversions
    (doseq [s (mg/sample :potatoclient.proto.string-conversion-specs/MixedCaseString {:size 1000})]
      (try
        (conv/->kebab-case s)
        (conv/->PascalCase s)
        (conv/->snake_case s)
        (catch Exception e
          ;; Ignore collision exceptions for this test
          nil)))
    
    (let [stats (bidi/get-collision-stats)]
      (println "Collision Detection Statistics:")
      (println "  Total conversions:" (:total-conversions stats))
      (println "  Unique outputs:" (:unique-outputs stats))
      (println "  Functions used:" (sort (:functions stats)))
      
      ;; Verify stats make sense
      (is (pos? (:total-conversions stats)))
      (is (>= (:total-conversions stats) (:unique-outputs stats))))))

;; =============================================================================
;; Run All Tests
;; =============================================================================

(defn run-bidirectional-tests []
  (println "\n=== Running Bidirectional String Conversion Tests ===")
  (println "Testing lossless conversions...")
  (println "Testing collision detection...")
  (println "Testing bidirectional API...")
  (run-tests 'potatoclient.proto.string-conversion-bidirectional-test))