# Testing and Validation Guide

This document describes the testing infrastructure, protobuf validation system, and the relationship between the Clojure implementation and the TypeScript reference implementation.

## Table of Contents

- [Test Organization](#test-organization)
- [Command System Tests](#command-system-tests)
- [Protobuf Validation](#protobuf-validation)
- [TypeScript Reference Implementation](#typescript-reference-implementation)
- [Running Tests](#running-tests)
- [Debugging Test Failures](#debugging-test-failures)

## Test Organization

The PotatoClient test suite is organized into several categories:

### Command System Tests (`test/potatoclient/cmd/`)

The command system tests ensure that all 200+ command functions correctly generate and send protobuf messages:

1. **`comprehensive_command_test.clj`**
   - Tests every single command function in the system
   - Verifies proper protobuf message construction
   - Ensures all command namespaces are covered
   - Uses test helpers for enum conversions

2. **`generator_test.clj`**
   - Property-based testing using Malli generators
   - Tests commands with randomly generated valid inputs
   - Verifies protobuf serialization/deserialization
   - Tests edge cases and boundary values

3. **`validation_safety_test.clj`**
   - Tests that validation specs catch out-of-range values
   - Verifies protobuf builder validation
   - Documents all validation boundaries
   - Performance tests for validation overhead

4. **`test_helpers.clj`**
   - Helper functions for converting keywords to protobuf enums
   - Provides convenient mappings for test readability
   - Example: `(h/direction :cw)` → `JonGuiDataRotaryDirection/CLOCKWISE`

### Frame Timing Tests

- **`frame_timing_test.clj`** - Unit tests for CV tracking with frame timing
- **`frame_timing_integration_test.clj`** - Integration tests for double-click events with frame timing

### WebSocket Test Infrastructure

The test suite uses a sophisticated stubbing approach for WebSocket communication:

**`test/potatoclient/test_utils.clj`** provides:
- Mock WebSocket managers that capture commands
- State simulation capabilities
- Async command verification
- Connection lifecycle management

Example usage:
```clojure
(ns potatoclient.my-test
  (:require [clojure.test :refer [deftest testing is use-fixtures]]
            [potatoclient.test-utils :as test-utils]))

(use-fixtures :each test-utils/websocket-fixture)

(deftest test-websocket-commands
  (testing "Commands are captured correctly"
    ;; Send a command
    (some-command-function)
    ;; Verify it was captured
    (let [commands (test-utils/get-captured-commands)]
      (is (= 1 (count commands)))
      (is (= expected-proto-type (type (first commands)))))))
```

Benefits over real WebSocket servers:
- No port conflicts or "address already in use" errors
- Instant test execution (no server startup/shutdown delays)
- Deterministic behavior (no network timing issues)
- Better test isolation
- Easier debugging with captured command history

## Command System Tests

### Comprehensive Command Test

This test file ensures complete coverage of all command functions:

```clojure
(deftest test-all-rotary-commands
  (testing "All rotary platform commands"
    (rotary/rotary-start)
    (is (some? (capture-command-sync!)) "Start command sent")
    
    (rotary/rotary-azimuth-set-value 180.0 (h/direction :normal))
    (is (some? (capture-command-sync!)) "Set azimuth value sent")))
```

Key features:
- Synchronous command capture for testing
- Validation of protobuf message structure
- Test coverage tracking for all namespaces

### Generator-Based Testing

Uses Malli schemas to generate valid test data:

```clojure
(deftest test-property-numeric-command-parameters
  (testing "Commands handle valid numeric inputs"
    (doseq [azimuth (mg/sample ::specs/azimuth-degrees 5)]
      (rotary/rotary-set-platform-azimuth azimuth)
      (is (some? (capture-command-sync!))))))
```

### Validation Safety Testing

Ensures specs properly validate against protobuf constraints:

```clojure
(deftest malli-specs-catch-invalid-values
  (testing "Zoom level specs"
    (is (m/validate ::specs/zoom-level 0.0))      ; Valid: min value
    (is (m/validate ::specs/zoom-level 1.0))      ; Valid: max value
    (is (not (m/validate ::specs/zoom-level 1.1))) ; Invalid: over max
    (is (not (m/validate ::specs/zoom-level -0.1))))) ; Invalid: under min
```

## Protobuf Validation

### Proto Directory Structure

All protobuf definitions are located in the `./proto` directory:

```
proto/
├── jon_shared_cmd.proto           # Root command message
├── jon_shared_cmd_rotary.proto    # Rotary platform commands
├── jon_shared_cmd_day_camera.proto # Day camera commands
├── jon_shared_cmd_cv.proto        # Computer vision commands
├── jon_shared_data_types.proto    # Shared enum types
└── ... (other command and data proto files)
```

### Buf Validate Annotations

The proto files use `buf.validate` annotations to define field constraints:

```protobuf
message SetValue {
  float value = 1 [(buf.validate.field).float = {
    gte: 0.0,
    lte: 1.0
  }];
}

message SetPlatformAzimuth {
  double value = 1 [(buf.validate.field).double = {
    gte: 0.0,
    lt: 360.0
  }];
}
```

These constraints are mirrored in Clojure specs:

```clojure
(def zoom-level
  "Camera zoom level normalized (0.0 to 1.0)"
  [:double {:min 0.0 :max 1.0}])

(def azimuth-degrees
  "Azimuth angle in degrees [0, 360)"
  [:double {:min 0.0 :max 359.999999}])
```

### Validation Flow

1. **Proto Definition** → Defines valid ranges using `buf.validate`
2. **Clojure Specs** → Mirror the protobuf constraints in `potatoclient.specs`
3. **Guardrails** → Runtime validation during development
4. **Tests** → Verify specs match protobuf constraints

For detailed information about the protobuf implementation, see:
- **[.claude/protobuf-command-system.md](../.claude/protobuf-command-system.md)**

## TypeScript Reference Implementation

The Clojure command system is based on the TypeScript web frontend implementation located at:

**`examples/web/frontend/ts/`**

### Key TypeScript Files

1. **Command System** (`examples/web/frontend/ts/cmd/`)
   - `rotary.ts` - Rotary platform commands
   - `dayCamera.ts` - Day camera commands  
   - `cv.ts` - Computer vision commands
   - `core.ts` - Core command infrastructure

2. **State Management** (`examples/web/frontend/ts/statePub/`)
   - State publishing and subscription system
   - Protobuf state deserialization
   - Event handling patterns

### Implementation Parity

The Clojure implementation maintains feature parity with TypeScript:

| Feature | TypeScript | Clojure |
|---------|------------|---------|
| Command Creation | Builder pattern | Builder pattern via Java interop |
| Validation | Runtime + TypeScript types | Malli specs + Guardrails |
| Async Handling | Promises/async-await | core.async channels |
| Enum Handling | TypeScript enums | Java enum imports |
| Case Convention | camelCase | kebab-case with auto-conversion |

## Running Tests

### Run All Tests
```bash
make test
```

### Run Command System Tests Only
```bash
clojure -M:test -n potatoclient.cmd.comprehensive-command-test \
                -n potatoclient.cmd.generator-test \
                -n potatoclient.cmd.validation-safety-test
```

### Run with Guardrails Validation
```bash
make dev  # Runs with full validation
```

### Generate Test Reports
```bash
make report-test       # HTML test report
make report-unspecced  # Find functions without specs
make test-summary      # View latest test run summary
make coverage          # Generate code coverage report
```

### Test Infrastructure Features

#### Automated Test Logging
- All test runs automatically logged to `logs/test-runs/YYYYMMDD_HHMMSS/`
- Includes full output, compact summary, and extracted failures
- Logs analyzed by `scripts/compact-test-logs.sh`

#### WebSocket Stubbing
- Tests use mock WebSocket managers instead of real servers
- No port conflicts or network delays
- Deterministic command capture and verification
- See `test/potatoclient/test_utils.clj` for infrastructure

#### Coverage Analysis
- Uses jacoco for JVM bytecode coverage
- HTML reports show line-by-line coverage
- Identifies untested code paths
- Run `make coverage` then view `target/coverage/index.html`

## Debugging Test Failures

### Common Issues and Solutions

1. **Async Test Failures**
   - Use `capture-command-sync!` instead of `(<! (capture-command!))`
   - Ensure test runs in synchronous context

2. **Enum Type Mismatches**
   - Use test helpers: `(h/direction :cw)` not raw `:cw`
   - Check correct enum import in test file

3. **Validation Failures**
   - Check proto file for `buf.validate` constraints
   - Ensure spec matches proto definition exactly
   - Remember: zoom normalized [0,1], digital zoom [1,∞)

4. **Protobuf Method Not Found**
   - Check generated Java code for correct method name
   - Common: `hasClientType` → check non-null `getClientType`
   - `setLrfAlignment` → `setLrfCalib`

### Debug Utilities

Use the debug namespace to inspect commands:

```clojure
(require '[potatoclient.cmd.debug :as debug])

;; Decode a Base64 command payload
(debug/decode-base64-command "CAEaAggBGgI=")

;; Compare two commands
(debug/compare-commands
  #(camera/zoom-in) "Zoom In"
  #(camera/zoom-out) "Zoom Out")
```

## Best Practices

1. **Always Check Proto Files First**
   - Validation constraints are source of truth
   - Use `grep -n "validate" ./proto/*.proto`

2. **Use Domain-Specific Specs**
   - Don't use `number?` when `::specs/azimuth-degrees` exists
   - Specs should match proto constraints exactly

3. **Test Helpers for Enums**
   - Always use `test-helpers` for enum conversions
   - Makes tests more readable and maintainable

4. **Async Testing**
   - Use synchronous helpers in test context
   - `capture-command-sync!` blocks appropriately

5. **Coverage**
   - Comprehensive test covers all functions
   - Generator test covers edge cases
   - Validation test ensures safety