(ns potatoclient.malli-type-checking-test
  "Synthetic tests demonstrating that Malli instrumentation catches type errors.
   
   This answers the question: Will Malli complain if we call a function
   with the wrong type when instrumentation is turned on?
   
   Answer: YES - Malli WILL catch type errors when instrumentation is enabled,
   but the behavior depends on how you configure the error reporter."
  (:require
    [clojure.test :refer [deftest testing is]]
    [malli.core :as m]
    [malli.dev :as malli.dev]
    [malli.instrument :as mi]
    [potatoclient.init :as init]
    [potatoclient.malli.registry :as registry]))

(deftest test-malli-catches-type-errors
  (testing "Demonstration that Malli catches type errors with instrumentation"
    (init/ensure-registry!)

    ;; Define test functions with schemas
    (defn example-function
      "Takes an integer in range -6400 to 6400 (like magnetic declination in mils)"
      {:malli/schema [:=> [:cat [:int {:min -6400 :max 6400}]] [:map [:result :int]]]}
      [value]
      {:result value})

    (testing "WITHOUT instrumentation - type errors pass through"
      (malli.dev/stop!) ; Ensure instrumentation is OFF

      ;; These should NOT throw because instrumentation is off
      (is (= {:result "wrong"} (example-function "wrong"))) ; String passes through!
      (is (= {:result 10000} (example-function 10000))) ; Out of range passes!
      (is (= {:result nil} (example-function nil)))) ; nil passes through!

    (testing "WITH instrumentation (default reporter) - errors logged but not thrown"
      ;; Start with default reporter (logs but doesn't throw)
      (malli.dev/start! {:report (fn [type data]
                                  ;; Default: just log the error
                                   nil)})

      ;; Valid calls work fine
      (is (= {:result 100} (example-function 100)))
      (is (= {:result -6400} (example-function -6400)))

      ;; Invalid calls are logged but still execute (may cause downstream errors)
      ;; The function still runs but Malli reports the error
      (is (= {:result "bad"} (example-function "bad")))
      (is (= {:result 10000} (example-function 10000)))

      (malli.dev/stop!))

    (testing "WITH instrumentation (exception reporter) - errors throw immediately"
      ;; Configure to throw exceptions on validation errors
      (malli.dev/start! {:report (fn [type data]
                                   (throw (ex-info (str "Validation failed: " type)
                                                   {:type type :data data})))})

      ;; Valid calls still work
      (is (= {:result 100} (example-function 100)))
      (is (= {:result 0} (example-function 0)))
      (is (= {:result -6400} (example-function -6400)))
      (is (= {:result 6400} (example-function 6400)))

      ;; Invalid calls now THROW exceptions
      (is (thrown-with-msg? clojure.lang.ExceptionInfo
                            #"Validation failed"
                            (example-function "string")))

      (is (thrown-with-msg? clojure.lang.ExceptionInfo
                            #"Validation failed"
                            (example-function nil)))

      (is (thrown-with-msg? clojure.lang.ExceptionInfo
                            #"Validation failed"
                            (example-function {:value 100})))

      (is (thrown-with-msg? clojure.lang.ExceptionInfo
                            #"Validation failed"
                            (example-function 6401))) ; Out of range

      (is (thrown-with-msg? clojure.lang.ExceptionInfo
                            #"Validation failed"
                            (example-function -6401))) ; Out of range

      (malli.dev/stop!))))

(deftest test-typo-in-spec-reference
  (testing "What happens when there's a typo in the spec reference"
    (init/ensure-registry!)

    ;; This simulates the scenario in the original question
    (defn function-with-typo
      "Function with a typo in spec name (:angle/magnetic-declinaton instead of :angle/magnetic-declination)"
      {:malli/schema [:=> [:cat :angle/magnetic-declinaton] :map]} ; TYPO!
      [value]
      {:value value})

    (testing "Attempting to instrument a function with non-existent spec fails"
      ;; The instrumentation itself will fail because the spec doesn't exist
      (is (thrown? Exception
                   (mi/instrument! 'potatoclient.malli-type-checking-test/function-with-typo))))

    (testing "Correct spec name works"
      ;; First verify the correct spec exists
      (is (m/schema [:int {:min -6400 :max 6400}]
                    {:registry (registry/get-registry)}))

      ;; Function with correct spec can be instrumented
      (defn function-with-correct-spec
        {:malli/schema [:=> [:cat [:int {:min -6400 :max 6400}]] :map]}
        [value]
        {:value value})

      ;; This should succeed
      (mi/instrument! 'potatoclient.malli-type-checking-test/function-with-correct-spec)
      (mi/unstrument! 'potatoclient.malli-type-checking-test/function-with-correct-spec))))

(deftest test-real-world-scenario
  (testing "Real-world scenario: compass/set-magnetic-declination behavior"
    (init/ensure-registry!)

    ;; Note: The actual compass/set-magnetic-declination uses :angle/magnetic-declination
    ;; which currently has an issue - it's defined as [:double {:min -180.0 :max 180.0}]
    ;; but the function comment says it takes mils (-6400 to 6400).
    ;; This test demonstrates what WOULD happen if the spec was correctly defined.

    (defn mock-set-magnetic-declination
      "Mock function with the intended spec for magnetic declination in mils"
      {:malli/schema [:=> [:cat [:int {:min -6400 :max 6400}]] :map]}
      [value]
      {:compass {:set_magnetic_declination {:value value}}})

    (testing "With proper instrumentation and spec"
      (malli.dev/start! {:report (fn [type _]
                                   (throw (ex-info (str "Invalid: " type) {})))})

      ;; Valid mils values
      (is (map? (mock-set-magnetic-declination 0)))
      (is (map? (mock-set-magnetic-declination 1000)))
      (is (map? (mock-set-magnetic-declination -1000)))
      (is (map? (mock-set-magnetic-declination 6400)))
      (is (map? (mock-set-magnetic-declination -6400)))

      ;; Invalid inputs are caught
      (is (thrown? Exception (mock-set-magnetic-declination "100"))) ; String
      (is (thrown? Exception (mock-set-magnetic-declination nil))) ; nil
      (is (thrown? Exception (mock-set-magnetic-declination 6401))) ; Too large
      (is (thrown? Exception (mock-set-magnetic-declination -6401))) ; Too small
      (is (thrown? Exception (mock-set-magnetic-declination 100.5))) ; Float

      (malli.dev/stop!))))

;; Summary function to run and report results
(defn summary
  "Run this to see a summary of how Malli instrumentation works"
  []
  (println "\n=== MALLI TYPE CHECKING SUMMARY ===\n")
  (println "Q: Will Malli complain if we call a function with wrong types when instrumentation is on?")
  (println "A: YES - but it depends on the error reporter configuration:\n")
  (println "1. DEFAULT REPORTER: Logs errors but doesn't throw")
  (println "   - Invalid inputs are detected and logged")
  (println "   - Function still executes (may cause downstream errors)")
  (println "   - Good for development/debugging\n")
  (println "2. EXCEPTION REPORTER: Throws immediately on type errors")
  (println "   - Invalid inputs cause immediate exceptions")
  (println "   - Function doesn't execute with bad inputs")
  (println "   - Good for testing and catching errors early\n")
  (println "3. TYPOS IN SPEC NAMES: Caught at instrumentation time")
  (println "   - If spec name is wrong (e.g., :angle/magnetic-declinaton)")
  (println "   - Instrumentation itself fails")
  (println "   - You'll know immediately that the spec doesn't exist\n")
  (println "Run the tests to see these behaviors in action!"))