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
  ;; Start instrumentation with thrower to make tests fail on invalid inputs
  (require '[malli.error :as me])
  (malli.dev/start! {:report (fn [type data]
                               ;; Throw on validation errors to make tests fail appropriately
                               (let [{:keys [input args output value]} data
                                     ex-data (cond-> {:type type}
                                              input (assoc :input input)
                                              args (assoc :args args)
                                              output (assoc :output output)
                                              value (assoc :value value))]
                                 (throw (ex-info (str "Schema validation error: " type) ex-data))))})
  ;; Return cleanup function
  (fn []
    ;; Stop instrumentation
    (malli.dev/stop!)))

(deftest test-magnetic-declination-type-checking
  (testing "Malli catches type errors for set-magnetic-declination when instrumented"
    (let [cleanup (setup-test-instrumentation)]
      (try
        ;; Valid calls should work
        (testing "Valid magnetic declination value (within -180.0 to 180.0 degrees)"
          (is (map? (compass/set-magnetic-declination 0.0)))
          (is (map? (compass/set-magnetic-declination 45.0)))
          (is (map? (compass/set-magnetic-declination -45.0)))
          (is (map? (compass/set-magnetic-declination 179.9)))
          (is (map? (compass/set-magnetic-declination -179.9))))

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
          ;; Too large (>= 180.0 degrees)
          (is (thrown? Exception
                       (compass/set-magnetic-declination 180.0)))
          (is (thrown? Exception
                       (compass/set-magnetic-declination 200.0)))

          ;; Too small (< -180.0 degrees)
          (is (thrown? Exception
                       (compass/set-magnetic-declination -180.1)))
          (is (thrown? Exception
                       (compass/set-magnetic-declination -200.0)))

          ;; Integer values (spec requires double)
          (is (thrown? Exception
                       (compass/set-magnetic-declination 100)))
          (is (thrown? Exception
                       (compass/set-magnetic-declination -100))))

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
      ;; String input passes through to the function (no validation at boundary)
      ;; It might work or fail later depending on implementation
      (let [result (compass/set-magnetic-declination "100")]
        (is (map? result) "Function executes even with wrong type"))

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
      (is (m/schema :angle/magnetic-declination)))

    (testing "Check that typo version does NOT exist"
      (is (thrown? Exception
                   (m/schema :angle/magnetic-declinaton))))))