# Testing Guide

Comprehensive testing strategies and tools for PotatoClient development.

## Testing Philosophy

1. **Test at multiple levels** - Unit, integration, and end-to-end
2. **Specs are tests** - Malli specs provide property-based testing
3. **Fast feedback** - Tests should run quickly
4. **Isolation** - Tests shouldn't depend on external services
5. **Clarity** - Test names should clearly describe behavior

## Test Organization

```
test/
├── potatoclient/           # Clojure tests
│   ├── *_test.clj         # Unit tests
│   ├── e2e_*_test.clj     # End-to-end tests
│   └── integration_test.clj # Integration tests
├── kotlin/                 # Kotlin tests
│   ├── *Test.kt           # Unit tests
│   └── *IntegrationTest.kt # Integration tests
└── resources/             # Test fixtures
    └── test-data/
```

## Running Tests

### All Tests

```bash
# Run all tests with detailed output
make test

# View summary of latest test run
make test-summary

# Run with coverage analysis
make test-coverage
```

### Specific Tests

```bash
# Run specific Clojure namespace
clojure -M:test -n potatoclient.transit-test

# Run tests matching pattern
clojure -M:test -i "transit"

# Run specific Kotlin test
./gradlew test --tests "TransitIntegrationTest"
```

### Continuous Testing

```bash
# Watch mode (using Kaocha)
clojure -M:test --watch

# Or with REPL
(require '[kaocha.repl :as k])
(k/run-all)  ; Run all tests
(k/run #'my-test)  ; Run specific test
```

## Writing Tests

### Clojure Unit Tests

```clojure
(ns potatoclient.example-test
  (:require [clojure.test :refer [deftest testing is]]
            [potatoclient.example :as example]
            [malli.generator :as mg]))

(deftest simple-function-test
  (testing "Basic functionality"
    (is (= 4 (example/add 2 2))))
  
  (testing "Edge cases"
    (is (nil? (example/process nil)))
    (is (thrown? Exception (example/process "invalid")))))

;; Property-based testing with Malli
(deftest property-based-test
  (testing "Generated input validation"
    (doseq [input (mg/sample ::example/input-spec 100)]
      (is (m/validate ::example/output-spec
                      (example/process input))))))
```

### Integration Tests

```clojure
(ns potatoclient.integration-test
  (:require [clojure.test :refer [deftest use-fixtures]]
            [potatoclient.test-utils :as tu]))

;; Setup/teardown
(use-fixtures :each tu/reset-app-db-fixture)

(deftest subprocess-communication-test
  (testing "Command subprocess integration"
    (tu/with-test-subprocess :command
      (let [response (tu/send-test-command {:ping {}})]
        (is (= :pong (:type response)))))))
```

### Kotlin Tests

```kotlin
class GestureRecognizerTest {
    private lateinit var recognizer: GestureRecognizer
    
    @BeforeEach
    fun setup() {
        recognizer = GestureRecognizer(TestConfig())
    }
    
    @Test
    fun `detects tap gesture`() {
        val events = listOf(
            MouseEvent(PRESS, 100, 100, currentTimeMillis()),
            MouseEvent(RELEASE, 100, 100, currentTimeMillis() + 50)
        )
        
        val gesture = recognizer.recognize(events)
        
        assertThat(gesture).isInstanceOf(TapGesture::class.java)
        assertThat((gesture as TapGesture).position).isEqualTo(Point(100, 100))
    }
}
```

### End-to-End Tests

```clojure
(deftest full-command-flow-test
  (testing "Command flows through entire system"
    (tu/with-all-subprocesses
      ;; Send command
      (cmd/send-command! {:rotary {:goto-ndc {:x 0.5 :y 0.5}}})
      
      ;; Wait for state update
      (tu/wait-for-state
        #(= 0.5 (get-in % [:rotary :target :x]))
        5000)
      
      ;; Verify final state
      (is (= {:x 0.5 :y 0.5}
             (get-in @state/app-db [:rotary :target]))))))
```

## Test Utilities

### Common Test Helpers

```clojure
;; test/potatoclient/test_utils.clj

(defn with-test-subprocess [type f]
  "Run test with a specific subprocess"
  (let [process (start-test-subprocess type)]
    (try
      (f)
      (finally
        (stop-subprocess process)))))

(defn wait-for-state [pred timeout-ms]
  "Wait for app state to match predicate"
  (loop [elapsed 0]
    (cond
      (pred @state/app-db) true
      (> elapsed timeout-ms) false
      :else (do (Thread/sleep 100)
                (recur (+ elapsed 100))))))

(defn mock-video-stream []
  "Start mock video stream for testing"
  {:pre [(available-port? 8080)]}
  (sh "make" "-C" "tools/mock-video-stream" "server"))
```

### Test Fixtures

```clojure
(def reset-app-db-fixture
  "Reset app state between tests"
  {:before #(reset! state/app-db state/initial-state)
   :after  #(reset! state/app-db state/initial-state)})

(def subprocess-fixture
  "Start/stop all subprocesses"
  {:before #(start-all-subprocesses!)
   :after  #(stop-all-subprocesses!)})
```

## Property-Based Testing

### Using Malli Generators

```clojure
(require '[malli.generator :as mg]
         '[malli.provider :as mp])

;; Generate test data from specs
(deftest command-generation-test
  (testing "All generated commands are valid"
    (doseq [cmd (mg/sample ::cmd/command 1000)]
      (is (m/validate ::cmd/command cmd))
      (is (transit-roundtrip-works? cmd)))))

;; Infer specs from examples
(def inferred-spec
  (mp/provide [{:x 1 :y 2}
               {:x 3 :y 4 :z 5}]))
```

### Stateful Property Testing

```clojure
(require '[clojure.test.check.generators :as gen]
         '[clojure.test.check.properties :as prop])

(defspec gesture-state-machine 100
  (prop/for-all [events (gen/vector mouse-event-gen)]
    (let [states (reductions gesture/process-event
                            gesture/initial-state
                            events)]
      (every? valid-state? states))))
```

## Coverage Analysis

### Running Coverage

```bash
# Generate coverage report
make test-coverage

# View HTML report
open target/coverage/index.html

# Clojure-only coverage (faster)
make coverage-clojure
```

### Coverage Goals

- **Overall**: > 80% line coverage
- **Core namespaces**: > 90% coverage
- **Transit layer**: 100% critical path coverage
- **UI code**: > 60% coverage (harder to test)

### Analyzing Coverage

```bash
# Find untested code
make coverage-analyze

# Focus on specific namespace
clojure -M:test-coverage --ns-regex "potatoclient.transit.*"
```

## Test Data Management

### Using Transit Test Generator

```bash
# Generate test commands
cd tools/transit-test-generator
java -jar target/*.jar batch --output-dir ../../test/resources/commands/

# Validate test data
java -jar target/*.jar validate-batch --input-dir ../../test/resources/commands/
```

### Test Fixtures

```clojure
;; Load test data
(def test-commands
  (read-transit-file "test/resources/commands/valid-commands.transit"))

;; Use in tests
(deftest command-processing-test
  (doseq [cmd test-commands]
    (is (process-command cmd))))
```

## Debugging Tests

### Verbose Output

```bash
# Run with debug logging
DEBUG=true make test

# Or in REPL
(require '[clojure.tools.logging :as log])
(log/set-level! :debug)
```

### Failed Test Investigation

```clojure
;; Re-run specific failing test
(require '[kaocha.repl :as k])
(k/run #'failing-test {:fail-fast? true
                       :capture-output? false})

;; Debug with print statements
(deftest debug-test
  (testing "Debug output"
    (println "State before:" @state/app-db)
    (process-action :test)
    (println "State after:" @state/app-db)
    (is (= expected @state/app-db))))
```

## Performance Testing

### Benchmarking

```clojure
(require '[criterium.core :as crit])

(deftest performance-test
  (testing "Transit encoding performance"
    (let [data (generate-large-state)]
      (crit/quick-bench
        (transit/encode data)))))
```

### Load Testing

```clojure
(deftest load-test
  (testing "System handles concurrent commands"
    (let [commands (repeat 1000 {:ping {}})
          futures (doall
                   (map #(future (cmd/send-command! %))
                        commands))]
      (is (every? #(= :pong (:type @%)) futures)))))
```

## CI/CD Integration

### GitHub Actions

```yaml
# .github/workflows/test.yml
name: Tests
on: [push, pull_request]

jobs:
  test:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      - uses: actions/setup-java@v3
        with:
          java-version: '17'
      - uses: DeLaGuardo/setup-clojure@10.0
        with:
          cli: latest
      - run: make test
      - run: make test-coverage
      - uses: codecov/codecov-action@v3
```

## Best Practices

### Do's

1. ✓ Write tests first (TDD)
2. ✓ Keep tests fast and isolated
3. ✓ Use descriptive test names
4. ✓ Test edge cases and error paths
5. ✓ Clean up resources in fixtures

### Don'ts

1. ✗ Don't test implementation details
2. ✗ Don't use `Thread/sleep` without timeouts
3. ✗ Don't share state between tests
4. ✗ Don't ignore flaky tests
5. ✗ Don't test external services directly

## Troubleshooting

### Common Issues

**Tests hang**
- Check for blocking I/O
- Add timeouts to async operations
- Use `with-test-subprocess` for isolation

**Flaky tests**
- Remove timing dependencies
- Use `wait-for-state` instead of sleep
- Mock external dependencies

**Coverage gaps**
- Generate coverage report
- Focus on untested namespaces
- Add property-based tests

## See Also

- [Development Workflow](./workflow.md)
- [Code Standards](./code-standards.md)
- [Transit Test Generator](../tools/transit-test-generator.md)
- [Mock Video Stream](../tools/mock-video-stream.md)