(ns potatoclient.malli-instrumentation-test
  "Synthetic tests to verify Malli instrumentation catches type errors.
   These tests demonstrate that when instrumentation is enabled,
   Malli will throw exceptions for invalid inputs."
  (:require
    [clojure.test :refer [deftest testing is]]
    [malli.core :as m]
    [malli.dev :as malli.dev]
    [malli.instrument :as mi]
    [potatoclient.init :as init]
    [potatoclient.cmd.compass :as compass]
    [potatoclient.malli.registry :as registry]))

(defn setup-test-instrumentation
  "Setup instrumentation for testing.
   Returns a function to restore the original state."
  []
  (init/ensure-registry!)
  ;; Save current instrumentation state
  (let [original-instrumented (mi/instrumented)]
    ;; Start instrumentation
    (malli.dev/start! {:report (fn [_] nil)}) ; Silent reporting for tests
    ;; Return cleanup function
    (fn []
      ;; Restore original instrumentation state
      (malli.dev/stop!)
      (doseq [var-name original-instrumented]
        (mi/instrument! var-name)))))

(deftest test-magnetic-declination-type-checking
  (testing "Malli catches type errors for set-magnetic-declination when instrumented"
    (let [cleanup (setup-test-instrumentation)]
      (try
        ;; Valid calls should work
        (testing "Valid magnetic declination value (within -6400 to 6400 mils)"
          (is (map? (compass/set-magnetic-declination 0)))
          (is (map? (compass/set-magnetic-declination 100)))
          (is (map? (compass/set-magnetic-declination -100)))
          (is (map? (compass/set-magnetic-declination 6400)))
          (is (map? (compass/set-magnetic-declination -6400))))

        ;; Invalid calls should throw when instrumented
        (testing "Invalid input types are caught by instrumentation"
          ;; String instead of number
          (is (thrown? Exception
                       (compass/set-magnetic-declination "100")))

          ;; nil value
          (is (thrown? Exception
                       (compass/set-magnetic-declination nil)))

          ;; Map instead of number
          (is (thrown? Exception
                       (compass/set-magnetic-declination {:value 100})))

          ;; Vector instead of number
          (is (thrown? Exception
                       (compass/set-magnetic-declination [100])))

          ;; Keyword instead of number
          (is (thrown? Exception
                       (compass/set-magnetic-declination :hundred))))

        (testing "Out of range values are caught by instrumentation"
          ;; Too large (> 6400 mils)
          (is (thrown? Exception
                       (compass/set-magnetic-declination 6401)))
          (is (thrown? Exception
                       (compass/set-magnetic-declination 10000)))

          ;; Too small (< -6400 mils)
          (is (thrown? Exception
                       (compass/set-magnetic-declination -6401)))
          (is (thrown? Exception
                       (compass/set-magnetic-declination -10000)))

          ;; Float values (spec requires int)
          (is (thrown? Exception
                       (compass/set-magnetic-declination 100.5)))
          (is (thrown? Exception
                       (compass/set-magnetic-declination -100.5))))

        (testing "Wrong number of arguments is caught"
          ;; Too many arguments
          (is (thrown? Exception
                       (compass/set-magnetic-declination 100 200)))

          ;; No arguments
          (is (thrown? Exception
                       (compass/set-magnetic-declination))))

        (finally
          (cleanup))))))

(deftest test-instrumentation-disabled-behavior
  (testing "Without instrumentation, invalid inputs pass through (demonstrating the need for instrumentation)"
    ;; Ensure registry is initialized but instrumentation is OFF
    (init/ensure-registry!)
    (malli.dev/stop!)

    ;; These would normally fail with instrumentation, but pass through without it
    (testing "Invalid inputs pass through when not instrumented"
      ;; String input - will likely cause downstream errors
      (is (thrown? Exception
            ;; This will fail at runtime in create-command, not at function boundary
                   (compass/set-magnetic-declination "100")))

      ;; The command is created but may be invalid
      (let [result (compass/set-magnetic-declination 10000)] ; Out of range
        (is (map? result))
        ;; The value gets through even though it's out of spec
        (is (= 10000 (get-in result [:compass :set_magnetic_declination :value])))))))

(deftest test-custom-function-with-wrong-spec
  (testing "Demonstration of catching typos in spec references"
    (init/ensure-registry!)

    ;; Define a function with a typo in the spec reference
    (let [test-ns (create-ns 'test.synthetic.wrong-spec)]
      (binding [*ns* test-ns]
        (intern test-ns 'bad-function
                (with-meta
                  (fn [value]
                    {:result value})
                  {:malli/schema [:=> [:cat :angle/magnetic-declinaton] :any]})) ; Typo!

        ;; Try to instrument it
        (testing "Instrumenting function with non-existent spec fails"
          (is (thrown? Exception
                ;; This will fail because :angle/magnetic-declinaton doesn't exist
                       (mi/instrument! 'test.synthetic.wrong-spec/bad-function))))))))

(deftest test-validate-spec-exists
  (testing "Verify our specs are properly registered"
    (init/ensure-registry!)

    (testing "Check that :angle/magnetic-declination exists in registry"
      (is (m/schema [:angle/magnetic-declination]
                    {:registry (registry/get-registry)})))

    (testing "Check that typo version does NOT exist"
      (is (thrown? Exception
                   (m/schema [:angle/magnetic-declinaton] ; Typo
                             {:registry (registry/get-registry)}))))))