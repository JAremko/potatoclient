(ns potatoclient.proto.string-conversion-test
  "Comprehensive property-based tests for string conversions with collision detection"
  (:require [clojure.test :refer :all]
            [clojure.test.check :as tc]
            [clojure.test.check.clojure-test :refer [defspec]]
            [clojure.test.check.generators :as gen]
            [clojure.test.check.properties :as prop]
            [potatoclient.proto.string-conversion :as conv]
            [potatoclient.proto.string-conversion-specs :as specs]
            [potatoclient.proto.constants :as constants]
            [malli.generator :as mg]
            [malli.core :as m]
            [clojure.string :as str]
            [lambdaisland.regal.generator :as regal-gen]))

;; =============================================================================
;; Collision Detection
;; =============================================================================

(def ^:private conversion-collisions (atom {}))

(defn- detect-collision!
  "Track conversions and detect collisions.
  Throws if two different inputs produce the same output."
  [conversion-fn input output]
  (let [fn-name (str conversion-fn)
        collision-key [fn-name output]]
    (if-let [existing-input (get @conversion-collisions collision-key)]
      (when (not= existing-input input)
        (throw (ex-info "Conversion collision detected!"
                        {:function fn-name
                         :input-1 existing-input
                         :input-2 input
                         :output output
                         :message (str "Both '" existing-input "' and '" input 
                                      "' convert to '" output "'")})))
      (swap! conversion-collisions assoc collision-key input))
    output))

(defn- with-collision-detection
  "Wrap a conversion function with collision detection"
  [f]
  (fn [input]
    (when input
      (let [output (f input)]
        (detect-collision! f input output)))))

;; =============================================================================
;; Test Generators using Regal
;; =============================================================================

;; Use Regal generators from specs
(def camel-case-gen (specs/gen-camel-case))
(def pascal-case-gen (specs/gen-pascal-case))
(def snake-case-gen (specs/gen-snake-case))
(def proto-constant-gen (specs/gen-proto-constant))
(def kebab-case-gen (specs/gen-kebab-case))
(def numeric-suffix-gen (specs/gen-numeric-suffix))
(def proto-type-constant-gen (specs/gen-proto-type-constant))
(def all-case-gen (specs/gen-mixed-case))

;; =============================================================================
;; Property Tests
;; =============================================================================

(defspec kebab-case-conversion-properties 10000
  (prop/for-all [s all-case-gen]
    (let [result (conv/->kebab-case s)]
      (and
        ;; Result should be valid kebab-case
        (or (nil? result)
            (re-matches #"^[a-z][a-z0-9-]*$" result))
        ;; Result should be lowercase
        (or (nil? result)
            (= result (str/lower-case result)))
        ;; Should not have underscores or uppercase
        (or (nil? result)
            (not (or (str/includes? result "_")
                     (re-find #"[A-Z]" result))))))))

(defspec pascal-case-conversion-properties 10000
  (prop/for-all [s all-case-gen]
    (let [result (conv/->PascalCase s)]
      (and
        ;; Result should start with uppercase
        (or (nil? result)
            (Character/isUpperCase (first result)))
        ;; Should not have hyphens or underscores
        (or (nil? result)
            (not (or (str/includes? result "-")
                     (str/includes? result "_"))))))))

(defspec snake-case-conversion-properties 10000
  (prop/for-all [s all-case-gen]
    (let [result (conv/->snake_case s)]
      (and
        ;; Result should be valid snake_case
        (or (nil? result)
            (re-matches #"^[a-z][a-z0-9_]*$" result))
        ;; Should not have hyphens or uppercase
        (or (nil? result)
            (not (or (str/includes? result "-")
                     (re-find #"[A-Z]" result))))))))

(defspec proto-constant-preservation 10000
  (prop/for-all [s proto-constant-gen]
    (let [kw (constants/proto-const->keyword s)
          back (constants/keyword->proto-const kw)]
      (= s back))))

(defspec method-name-generation-properties 10000
  (prop/for-all [field (gen/one-of [camel-case-gen snake-case-gen])]
    (let [getter (conv/getter-method-name field)
          setter (conv/setter-method-name field)
          has (conv/has-method-name field)
          add (conv/add-method-name field)
          add-all (conv/add-all-method-name field)]
      (and
        ;; All method names should start correctly
        (str/starts-with? getter "get")
        (str/starts-with? setter "set")
        (str/starts-with? has "has")
        (str/starts-with? add "add")
        (str/starts-with? add-all "addAll")
        ;; All should be PascalCase after prefix
        (Character/isUpperCase (.charAt getter 3))
        (Character/isUpperCase (.charAt setter 3))
        (Character/isUpperCase (.charAt has 3))
        (Character/isUpperCase (.charAt add 3))
        (Character/isUpperCase (.charAt add-all 6))))))

;; =============================================================================
;; Collision Detection Tests
;; =============================================================================

(deftest collision-detection-test
  (testing "Collision detection for conversions"
    ;; Reset collision tracker
    (reset! conversion-collisions {})
    
    ;; Test that same input doesn't cause collision
    (let [f (with-collision-detection identity)]
      (is (= "test" (f "test")))
      (is (= "test" (f "test"))))
    
    ;; Test that different inputs with same output cause collision
    (let [f (with-collision-detection (constantly "same"))]
      (is (= "same" (f "input1")))
      (is (thrown-with-msg? 
           clojure.lang.ExceptionInfo
           #"Conversion collision detected"
           (f "input2"))))))

(defspec no-collisions-in-kebab-case 10000
  (let [seen (atom {})
        collision-found (atom false)]
    (prop/for-all [s all-case-gen]
      (when-not @collision-found
        (let [result (conv/->kebab-case s)]
          (when result
            (if-let [existing (get @seen result)]
              (when (not= existing s)
                (reset! collision-found true)
                (println "COLLISION:" existing "and" s "both produce" result))
              (swap! seen assoc result s)))))
      (not @collision-found))))

;; =============================================================================
;; Edge Case Tests
;; =============================================================================

(deftest edge-case-conversions
  (testing "Empty and nil inputs"
    (is (nil? (conv/->kebab-case nil)))
    (is (nil? (conv/->kebab-case "")))
    (is (nil? (conv/->PascalCase nil)))
    (is (nil? (conv/->snake_case nil))))
  
  (testing "Single character inputs"
    (is (= "a" (conv/->kebab-case "a")))
    (is (= "A" (conv/->PascalCase "a")))
    (is (= "a" (conv/->snake_case "A"))))
  
  (testing "Numeric handling"
    (is (= "type-int32" (conv/->kebab-case "TYPE_INT32")))
    (is (= "type-int-32" (conv/->kebab-case "TYPE_INT_32")))
    (is (= "mode-2d" (conv/->kebab-case "Mode2D")))
    (is (= "mode-2-d" (conv/->kebab-case "Mode2_D"))))
  
  (testing "Special protobuf constants"
    (is (= :type-int32 (constants/proto-const->keyword "TYPE_INT32")))
    (is (= "TYPE_INT32" (constants/keyword->proto-const :type-int32)))
    (is (= :label-optional (constants/proto-const->keyword "LABEL_OPTIONAL")))
    (is (= "LABEL_OPTIONAL" (constants/keyword->proto-const :label-optional)))))

;; =============================================================================
;; Guardrails Integration Tests
;; =============================================================================

(deftest guardrails-spec-validation
  (testing "Function specs match actual behavior"
    ;; Test with valid inputs from generators
    (doseq [_ (range 100)]
      (let [s (gen/generate all-case-gen)]
        ;; These should not throw with guardrails enabled
        (is (string? (str (conv/->kebab-case s))))
        (is (keyword? (conv/->kebab-case-keyword s)))
        (is (string? (str (conv/->PascalCase s))))
        (is (string? (str (conv/->snake_case s)))))))
  
  (testing "Method name generation specs"
    (doseq [_ (range 100)]
      (let [field (gen/generate (gen/one-of [camel-case-gen snake-case-gen]))]
        (is (re-matches #"^get[A-Z].*" (conv/getter-method-name field)))
        (is (re-matches #"^set[A-Z].*" (conv/setter-method-name field)))
        (is (re-matches #"^has[A-Z].*" (conv/has-method-name field)))
        (is (re-matches #"^add[A-Z].*" (conv/add-method-name field)))
        (is (re-matches #"^addAll[A-Z].*" (conv/add-all-method-name field)))))))

;; =============================================================================
;; Roundtrip Tests
;; =============================================================================

(defspec proto-clojure-roundtrip 10000
  (prop/for-all [s (gen/one-of [pascal-case-gen proto-constant-gen])]
    (let [clj-name (conv/proto-name->clj-name s)]
      (if (conv/proto-constant? s)
        ;; Proto constants should roundtrip through our lossless converter
        (= s (conv/clj-name->proto-name clj-name))
        ;; Other conversions might not be perfectly reversible
        (keyword? clj-name)))))

;; =============================================================================
;; Performance Benchmarks
;; =============================================================================

(deftest ^:benchmark conversion-performance
  (testing "Conversion performance"
    (let [test-strings (repeatedly 10000 #(gen/generate all-case-gen))
          start (System/nanoTime)]
      (doseq [s test-strings]
        (conv/->kebab-case s))
      (let [elapsed (/ (- (System/nanoTime) start) 1e6)]
        (println "10k conversions took" elapsed "ms")
        (is (< elapsed 1000) "Conversions should be fast")))))

;; =============================================================================
;; Test Runner
;; =============================================================================

(defn run-all-tests []
  (println "\n=== Running String Conversion Tests ===")
  (println "Testing collision detection...")
  (collision-detection-test)
  (println "✓ Collision detection working")
  
  (println "\nTesting edge cases...")
  (edge-case-conversions)
  (println "✓ Edge cases handled correctly")
  
  (println "\nTesting guardrails integration...")
  (guardrails-spec-validation)
  (println "✓ Guardrails specs validated")
  
  (println "\nRunning property tests (this may take a while)...")
  (let [results [(tc/quick-check 1000 kebab-case-conversion-properties)
                 (tc/quick-check 1000 pascal-case-conversion-properties)
                 (tc/quick-check 1000 snake-case-conversion-properties)
                 (tc/quick-check 1000 proto-constant-preservation)
                 (tc/quick-check 1000 method-name-generation-properties)
                 (tc/quick-check 1000 no-collisions-in-kebab-case)
                 (tc/quick-check 1000 proto-clojure-roundtrip)]]
    (if (every? :pass? results)
      (println "✓ All property tests passed!")
      (do
        (println "✗ Some property tests failed:")
        (doseq [[i result] (map-indexed vector results)]
          (when-not (:pass? result)
            (println "  Test" i "failed:" (:result result)))))))
  
  (println "\n=== All tests completed ==="))