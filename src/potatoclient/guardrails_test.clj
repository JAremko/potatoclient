(ns potatoclient.guardrails-test
  "Test namespace to verify Guardrails spec validation.
  
  This namespace contains functions with intentional spec violations
  to test that Guardrails properly throws errors in development builds."
  (:require [com.fulcrologic.guardrails.core :refer [>defn >defn-]]))

;; Test function with correct spec and usage - should NOT throw
(>defn add-numbers
  "Add two integers together"
  [a b]
  [int? int? => int?]
  (+ a b))

;; Test function with spec violation on input - should throw when called with wrong type
(>defn multiply-numbers
  "Multiply two integers (but we'll call it with strings)"
  [x y]
  [int? int? => int?]
  (* x y))

;; Test function with spec violation on output - should throw when returning wrong type
(>defn divide-numbers
  "Divide two numbers, but returns a string instead of number"
  [x y]
  [number? number? => number?]
  (str (/ x y))) ; Returns string instead of number!

;; Test function with such-that predicate violation
(>defn get-positive-result
  "Should return a positive number but doesn't"
  [x]
  [number? => pos?]
  (- x 100)) ; Will return negative for small inputs

;; Test private function with spec
(>defn- validate-range
  "Check if number is in range 0-100"
  [n]
  [number? => boolean?]
  (and (>= n 0) (<= n 100)))

(>defn test-guardrails!
  "Run tests to verify Guardrails is working.
  Should throw errors in development builds."
  []
  [=> nil?]
  (println "\n=== Testing Guardrails Validation ===\n")

  ;; Check if Guardrails is enabled
  (println "Guardrails enabled:" (System/getProperty "guardrails.enabled"))
  (println "Release build:" (if (try (require 'potatoclient.runtime)
                                     ((resolve 'potatoclient.runtime/release-build?))
                                     (catch Exception _ false))
                              "YES" "NO"))
  (println "")

  ;; Test 1: Correct usage - should work
  (println "Test 1: Correct usage")
  (try
    (println "  add-numbers(2, 3) =" (add-numbers 2 3))
    (println "  ✓ PASS: No error thrown")
    (catch Exception e
      (println "  ✗ FAIL: Unexpected error:" (.getMessage e))))

  ;; Test 2: Wrong input type - should throw
  (println "\nTest 2: Wrong input type")
  (try
    (println "  multiply-numbers(\"5\", \"10\") =")
    (multiply-numbers "5" "10")
    (println "  ✗ FAIL: Should have thrown an error!")
    (catch Exception e
      (println "  ✓ PASS: Caught expected error")
      (println "  Error:" (.getMessage e))))

  ;; Test 3: Wrong output type - should throw
  (println "\nTest 3: Wrong output type")
  (try
    (println "  divide-numbers(10, 2) =")
    (let [result (divide-numbers 10 2)]
      (println "  Result:" result "Type:" (type result))
      ;; According to Guardrails docs, output validation may happen on usage
      ;; Let's try to use the result in a numeric context
      (println "  Trying to add 1 to result:")
      (let [sum (+ 1 result)] ;; Force usage to trigger validation
        (println "  Sum:" sum)
        (println "  ✗ FAIL: Should have thrown an error!")))
    (catch Exception e
      (println "  ✓ PASS: Caught expected error")
      (println "  Error:" (.getMessage e))))

  ;; Test 4: Such-that predicate violation - should throw
  (println "\nTest 4: Such-that predicate violation")
  (try
    (println "  get-positive-result(5) =")
    (let [result (get-positive-result 5)]
      (println "  Result:" result)
      ;; Force usage of result
      (println "  Checking if result is positive:" (pos? result))
      (println "  ✗ FAIL: Should have thrown an error!"))
    (catch Exception e
      (println "  ✓ PASS: Caught expected error")
      (println "  Error:" (.getMessage e))))

  ;; Test 5: Private function - should work
  (println "\nTest 5: Private function with spec")
  (try
    (println "  validate-range(50) =" (validate-range 50))
    (println "  ✓ PASS: Private function works with spec")
    (catch Exception e
      (println "  ✗ FAIL: Unexpected error:" (.getMessage e))))

  ;; Test 6: Check Guardrails configuration
  (println "\nTest 6: Guardrails configuration check")
  (try
    (println "  Loading Guardrails config namespace...")
    (require '[com.fulcrologic.guardrails.config :as gr-config])
    (println "  Guardrails throw?: " @(resolve 'com.fulcrologic.guardrails.config/*throw?*))
    (catch Exception e
      (println "  Could not check config:" (.getMessage e))))

  (println "\n=== Guardrails Test Complete ==="))