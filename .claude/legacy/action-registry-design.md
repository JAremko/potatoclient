# Action Registry Design Document

## Overview

The Action Registry provides a single source of truth for all commands in the PotatoClient system. It enables:
- Shared action definitions between Clojure and Kotlin
- Automatic validation of command parameters
- Discovery of implemented vs unimplemented actions
- Type-safe command handling with Transit keywords
- Comprehensive testing and validation

## Architecture

### Core Components

```
potatoclient.transit/
├── ActionRegistry.java          # Core registry and public API (NO protobuf imports!)
├── ActionDefinition.java        # Action metadata class (NO protobuf imports!)
└── commands/                    # Command modules (mirrors proto structure)
    ├── BasicCommands.java       # ping, noop, frozen
    ├── RotaryCommands.java      # Rotary platform commands
    ├── SystemCommands.java      # System settings commands
    ├── GPSCommands.java         # GPS control commands
    ├── CompassCommands.java     # Compass commands
    ├── CVCommands.java          # Computer vision commands
    ├── CameraCommands.java      # Camera control (heat/day)
    ├── LRFCommands.java         # Laser range finder commands
    └── GlassHeaterCommands.java # Glass heater control

test/
├── ValidationSubprocess.kt      # Separate process for buf.validate tests
├── TransitValidationBridge.kt   # Transit IPC for validation results
└── ProtobufValidationTests.kt   # All protobuf validation (SUBPROCESS ONLY)
```

### Design Principles

1. **Pure Java Implementation** - No Kotlin to ensure Clojure compatibility
2. **Keyword-Based Storage** - All actions and parameters stored as Transit keywords internally
3. **No Reflection** - Direct method calls only (avoids Clojure coverage issues)
4. **Modular Structure** - Each command module corresponds to a protobuf file
5. **Immutable Registration** - Actions registered at class load time
6. **Type Safety** - Transit keywords created once and reused
7. **No Protobuf Dependencies** - Java registry code has NO protobuf imports/dependencies
8. **Validation Only in Dev/Test** - No validation overhead in production builds

## Production vs Development Separation

### Core Principle: Zero Validation in Production

The Action Registry provides command metadata and structure ONLY. All validation is strictly development/test time:

```java
// ActionRegistry.java - PRODUCTION CODE
public class ActionRegistry {
    // NO imports of protobuf classes
    // NO imports of validation libraries
    // NO validation logic
    
    public static boolean isKnownAction(String action) {
        // Simple lookup - no validation
        return actions.containsKey(action);
    }
    
    public static Set<String> getRequiredParams(String action) {
        // Metadata only - no validation
        ActionDefinition def = actions.get(action);
        return def != null ? def.getRequiredParams() : null;
    }
}
```

### Validation Architecture

```
PRODUCTION BUILD:
┌─────────────────┐
│ Clojure Process │
│  - No Malli     │
│  - No specs     │
│  - Just lookup  │
└─────────────────┘

DEV/TEST BUILD:
┌─────────────────┐         ┌──────────────────────┐
│ Clojure Process │ Transit │ Validation Subprocess│
│  - Malli specs  │ ◄─────► │  - Protobuf classes  │
│  - No protobuf  │   IPC   │  - buf.validate      │
│  - No reflection│         │  - Reflection OK     │
└─────────────────┘         └──────────────────────┘
```

### Build Configuration

```clojure
;; deps.edn
{:aliases
 {:run {:jvm-opts ["-Dpotatoclient.validation=false"]}
  :dev {:extra-deps {metosin/malli {:mvn/version "..."}}
        :jvm-opts ["-Dpotatoclient.validation=true"]}
  :test {:extra-deps {metosin/malli {:mvn/version "..."}}
         :jvm-opts ["-Dpotatoclient.validation=true"]}
  :nrepl {:extra-deps {metosin/malli {:mvn/version "..."}}
          :jvm-opts ["-Dpotatoclient.validation=true"]}}}
```

### Test Isolation Strategy

1. **Main Java Code**: Pure metadata, no validation logic
2. **Clojure Tests**: Use Malli for in-process validation
3. **Kotlin Subprocess**: Handles all protobuf validation
4. **Coverage**: Subprocess tests excluded from Clojure coverage
5. **Performance**: Subprocess started once, reused for all tests

### Validation Subprocess Design

The validation subprocess mimics a command subprocess but adds validation and JSON export:

```kotlin
// ValidationCommandSubprocess.kt
class ValidationCommandSubprocess : CommandSubprocess() {
    override fun handleCommand(message: Map<Keyword, Any>) {
        val action = message[TransitKeys.ACTION] as? String ?: return
        val params = message[TransitKeys.PARAMS] as? Map<*, *> ?: emptyMap<String, Any>()
        
        try {
            // 1. Build protobuf command (may throw)
            val protoCmd = SimpleCommandBuilder.buildCommand(action, params)
            
            // 2. Validate with buf.validate
            val violations = Validator.validate(protoCmd)
            
            // 3. Convert to JSON for comparison
            val jsonOutput = JsonFormat.printer()
                .includingDefaultValueFields()
                .preservingProtoFieldNames()
                .print(protoCmd)
            
            // 4. Send validation result as special log message
            sendValidationLog(mapOf(
                "type" to "validation-result",
                "action" to action,
                "valid" to violations.isEmpty(),
                "violations" to violations.map { v -> 
                    mapOf(
                        "field" to v.fieldPath,
                        "message" to v.message,
                        "constraint" to v.constraintId
                    )
                },
                "protobuf-json" to jsonOutput,
                "original-params" to params
            ))
            
        } catch (e: Exception) {
            // Build failed - send error log
            sendValidationLog(mapOf(
                "type" to "validation-error",
                "action" to action,
                "error" to e.message,
                "error-type" to e.javaClass.simpleName,
                "original-params" to params
            ))
        }
    }
    
    private fun sendValidationLog(data: Map<String, Any>) {
        // Send as JSON log message through Transit
        val logMessage = mapOf(
            TransitKeys.MSG_TYPE to MessageType.LOG.keyword,
            TransitKeys.LEVEL to "INFO",
            TransitKeys.LOGGER to "validation",
            TransitKeys.MESSAGE to "Validation result",
            TransitKeys.DATA to data  // JSON-serializable validation data
        )
        sendMessage(logMessage)
    }
}
```

## Implementation Plan

### Phase 1: Core Infrastructure

#### TODO: Create Base Classes
- [ ] Create `ActionDefinition.java` with builder pattern
  - [ ] Fields: keyword, name, description, requiredParams, optionalParams, implemented
  - [ ] Builder methods: `required()`, `optional()`
  - [ ] No reflection - simple field access
- [ ] Create `ActionRegistry.java` main class
  - [ ] Static map for action storage
  - [ ] Static map for handler storage  
  - [ ] Public API methods (no reflection)
  - [ ] Module initialization in static block

#### TODO: Define Testing Infrastructure
- [ ] Create test helper utilities
  - [ ] Mock Transit message creation
  - [ ] Invalid data generators
  - [ ] Assertion helpers
- [ ] Setup test fixtures for each command type

### Phase 2: Command Modules

#### TODO: Implement Command Modules

The following command modules need to be implemented, each corresponding to a protobuf command file:

- [ ] `BasicCommands.java` - Basic system commands (ping, noop, frozen)
- [ ] `RotaryCommands.java` - Rotary platform control (movement, scanning, positioning)
- [ ] `SystemCommands.java` - System-level operations (power, recording, configuration)
- [ ] `GPSCommands.java` - GPS functionality (position management)
- [ ] `CompassCommands.java` - Compass operations (calibration, offsets)
- [ ] `CVCommands.java` - Computer vision tracking (auto-focus, stabilization, tracking)
- [ ] `DayCameraCommands.java` - Day camera operations (zoom, focus, iris, FX modes)
- [ ] `HeatCameraCommands.java` - Thermal camera operations (zoom, focus, AGC, DDE)
- [ ] `LRFCommands.java` - Laser range finder (measurement, scanning, target designation)
- [ ] `LRFAlignCommands.java` - LRF alignment operations
- [ ] `GlassHeaterCommands.java` - Glass heater control for day camera
- [ ] `LIRACommands.java` - LIRA system commands
- [ ] `OSDCommands.java` - On-screen display commands

**IMPORTANT**: See `/TODO_PROTO_COMMANDS.md` for the complete, exhaustive list of all commands that must be implemented from each proto file. This file contains:
- Every command message from each proto file
- Parameter names and types for each command
- Nested command structures (e.g., DayCamera.Focus.SetValue)
- Commands with no parameters vs. parameterized commands

### Phase 3: Unit Testing

#### TODO: Java Unit Tests
- [ ] `ActionRegistryTest.java`
  - [ ] Test action registration and retrieval
  - [ ] Test parameter validation
  - [ ] Test unknown action handling
  - [ ] Test implemented/unimplemented tracking
- [ ] `ActionDefinitionTest.java`
  - [ ] Test builder pattern
  - [ ] Test keyword creation
  - [ ] Test parameter management
- [ ] Per-module tests (e.g., `RotaryCommandsTest.java`)
  - [ ] Test each command registration
  - [ ] Test parameter requirements

#### TODO: Validation Subprocess Tests
- [ ] `ValidationCommandSubprocessTest.kt`
  - [ ] Test protobuf building and validation
  - [ ] Test JSON log message format
  - [ ] Test error handling for invalid commands
  - [ ] Test protobuf JSON conversion accuracy
- [ ] `ProtobufJsonComparisonTest.clj`
  - [ ] Test float comparison with epsilon
  - [ ] Test enum name conversion
  - [ ] Test nested structure navigation
  - [ ] Test all ~170 commands round-trip

#### TODO: Validation Tests
- [ ] Valid parameter combinations
- [ ] Missing required parameters
- [ ] Extra unknown parameters
- [ ] Type mismatches
- [ ] Null/empty values
- [ ] Edge cases (empty maps, null params)

### Phase 4: Clojure Integration Tests

#### TODO: Clojure-side Tests
- [ ] `test/potatoclient/transit/action_registry_test.clj`
  - [ ] Test action discovery functions
  - [ ] Test parameter validation from Clojure
  - [ ] Test keyword conversion
  - [ ] Test error messages
- [ ] Malli spec integration tests
  - [ ] Test spec generation from registry
  - [ ] Test validation against specs

#### TODO: Invalid Data Tests
- [ ] Unknown actions
- [ ] Invalid parameter types
- [ ] Malformed Transit messages
- [ ] Security tests (injection attempts)

### Phase 5: Kotlin Integration

#### TODO: Kotlin Handler Registration
- [ ] Update `SimpleCommandHandlers.kt`
  - [ ] Add handler registration methods
  - [ ] Register all implemented handlers
  - [ ] Remove string-based switching
- [ ] Update `SimpleCommandBuilder.kt`
  - [ ] Use registry for validation
  - [ ] Use registry for dispatch
  - [ ] Integrate buf.validate for protobuf validation

#### TODO: Kotlin Tests
- [ ] Handler registration tests
- [ ] Command building with registry
- [ ] Error handling tests
- [ ] Protobuf validation tests
  - [ ] Test buf.validate annotations work correctly
  - [ ] Verify validation errors are properly reported
  - [ ] Test all constraint types (ranges, enums, required fields)

### Phase 6: End-to-End Testing

#### TODO: Full Transit Flow Tests with Dual Validation
- [ ] Create `TransitActionFlowTest.kt`
  - [ ] Clojure creates command
  - [ ] Transit encoding/decoding
  - [ ] Kotlin receives and validates
  - [ ] Kotlin builds protobuf command
  - [ ] Response flow back
- [ ] Test all command types end-to-end
- [ ] Test error propagation
- [ ] Create test data generators
  - [ ] Valid data generator for each command type
  - [ ] Invalid data generator with specific violation types
  - [ ] Edge case data (boundary values, empty collections, etc.)
- [ ] Create validation subprocess infrastructure
  - [ ] `ValidationCommandSubprocess.kt` extending CommandSubprocess
  - [ ] JSON log message type for validation results
  - [ ] Protobuf to JSON conversion with full field names
  - [ ] Subprocess lifecycle management
  - [ ] Result caching to avoid repeated validations
  - [ ] Timeout handling (30 second max)
- [ ] Create test-only validation helpers
  - [ ] Float comparison with epsilon (0.0001)
  - [ ] Canonical format verification (fail if not canonical)
  - [ ] JSON parsing with Cheshire (test deps only)
  - [ ] Specter navigation for test exploration
  - [ ] NO production conversion helpers (force canonical format)
- [ ] Implement JSON transformation in tests
  - [ ] Use Cheshire with camel-snake-kebab key-fn
  - [ ] Transform UPPER_CASE enums to lowercase
  - [ ] Convert all keys to kebab-case keywords
  - [ ] Verify transformed data matches Transit format
  - [ ] Document transformation is TEST ONLY

#### TODO: Comprehensive Validation Testing
- [ ] **Clojure Side Validation (Malli specs)**
  - [ ] Test all ~170 commands with valid data
  - [ ] Verify Malli specs catch invalid parameters
  - [ ] Test edge cases (boundary values)
  - [ ] Test required vs optional parameters
  - [ ] **ONLY ACTIVE IN**: dev, test, nrepl aliases
  - [ ] Verify NO validation in production builds
  
- [ ] **Kotlin Side Validation (buf.validate)**
  - [ ] Run as SUBPROCESS to avoid reflection in Clojure
  - [ ] Verify protobuf validation annotations work
  - [ ] Test numeric ranges (e.g., angles -90 to 90)
  - [ ] Test enum constraints (defined_only, not_in: [0])
  - [ ] Test string patterns and lengths
  - [ ] **CRITICAL**: These tests excluded from Clojure coverage
  
- [ ] **Invalid Data Sanity Checks** (CRITICAL!)
  - [ ] Missing required parameters
  - [ ] Wrong parameter types (string instead of number)
  - [ ] Out-of-range values (e.g., latitude > 90)
  - [ ] Invalid enum values
  - [ ] Null/undefined values
  - [ ] Empty objects/maps
  - [ ] Extra unknown parameters
  - [ ] Malformed Transit messages
  - [ ] Invalid action names
  - [ ] Nested command structure violations

#### TODO: Validation Synchronization Tests
- [ ] Ensure Malli specs match protobuf constraints
  - [ ] Float ranges match exactly
  - [ ] Enum values align
  - [ ] Required fields consistent
- [ ] Test that both validators reject the same invalid data
- [ ] Test that both validators accept the same valid data
- [ ] Document any intentional differences

#### TODO: Performance Tests
- [ ] Measure registry lookup performance
- [ ] Measure validation overhead (Malli + buf.validate)
- [ ] Compare with current implementation
- [ ] Ensure validation doesn't impact real-time performance
- [ ] **Verify production builds have ZERO validation overhead**
- [ ] Test timeout: Ensure validation tests complete < 30 seconds
- [ ] Memory usage: No protobuf classes loaded in main process

### Phase 7: Integration into Codebase

#### TODO: Update Core Components
- [ ] Update `transit/commands.clj`
  - [ ] Add registry lookup (no validation in prod)
  - [ ] Conditional spec generation (dev only)
- [ ] Update `transit/validation.clj`
  - [ ] Use registry for command structure
  - [ ] Validation only when system property set
- [ ] Update `CommandSubprocess.kt`
  - [ ] Use registry for dispatch
  - [ ] NO validation in command subprocess
- [ ] Update `specs.clj`
  - [ ] Conditional loading (dev/test only)
  - [ ] Generate specs from registry metadata
- [ ] Create `test/validation_subprocess.clj`
  - [ ] Subprocess launcher for protobuf validation
  - [ ] Transit IPC bridge
  - [ ] Result aggregation

#### TODO: Migration Strategy
- [ ] Parallel implementation (keep old code)
- [ ] A/B testing in development
- [ ] Gradual rollout
- [ ] Remove old implementation

## Testing Strategy

### Validation Testing Philosophy

The Action Registry serves as the bridge between Clojure's Malli validation and Kotlin's buf.validate protobuf validation. Our testing must ensure:

1. **Dual Validation Coverage**: Every command is tested with both Malli specs and protobuf validation
2. **Constraint Alignment**: Numeric ranges, enum values, and required fields match exactly
3. **Invalid Data Testing**: Comprehensive suite of invalid inputs to ensure robust error handling
4. **Real-world Scenarios**: Test data that mimics actual usage patterns
5. **Zero Production Impact**: Validation ONLY in dev/test/nrepl builds
6. **No Reflection**: Protobuf validation runs in subprocess to avoid Clojure reflection
7. **Clean Dependencies**: Main Java code has NO protobuf dependencies

### Invalid Data Test Categories

```java
// InvalidDataGenerator.java
public class InvalidDataGenerator {
    public static List<InvalidTestCase> generateForAction(String action) {
        List<InvalidTestCase> cases = new ArrayList<>();
        
        // 1. Missing required parameters
        cases.add(new InvalidTestCase(action, 
            Collections.emptyMap(), 
            "Missing all required parameters"));
        
        // 2. Wrong types
        cases.add(new InvalidTestCase(action,
            Map.of("x", "not-a-number"),  // String instead of float
            "Wrong parameter type"));
            
        // 3. Out of range values
        cases.add(new InvalidTestCase(action,
            Map.of("latitude", 91.0),  // > 90
            "Latitude out of range"));
            
        // 4. Invalid enum values  
        cases.add(new InvalidTestCase(action,
            Map.of("channel", "unknown-channel"),
            "Invalid enum value"));
            
        // 5. Null values
        cases.add(new InvalidTestCase(action,
            Map.of("x", null),
            "Null value for required field"));
            
        // 6. Extra unknown parameters
        cases.add(new InvalidTestCase(action,
            Map.of("x", 0.5, "unknown-param", "value"),
            "Unknown parameter included"));
            
        return cases;
    }
}
```

### Validation Message Protocol

The validation subprocess sends results as JSON log messages:

```clojure
;; Success case
{:msg-type :log
 :level "INFO"
 :logger "validation"
 :message "Validation result"
 :data {:type "validation-result"
        :action "rotary-goto-ndc"
        :valid true
        :violations []
        :protobuf-json "{\"rotary\":{\"rotateToNdc\":{\"channel\":\"HEAT\",\"x\":0.5,\"y\":-0.5}}}"
        :original-params {:channel "heat" :x 0.5 :y -0.5}}}

;; Failure case
{:msg-type :log
 :level "INFO"
 :logger "validation"
 :message "Validation result"
 :data {:type "validation-result"
        :action "rotary-goto-ndc"
        :valid false
        :violations [{:field "rotary.rotate_to_ndc.x"
                      :message "value must be between -1.0 and 1.0"
                      :constraint "float.gte"}]
        :protobuf-json nil
        :original-params {:channel "heat" :x 1.5 :y -0.5}}}
```

### Test Dependencies

```clojure
;; deps.edn - test alias only
{:test {:extra-deps {cheshire/cheshire {:mvn/version "5.12.0"}
                     camel-snake-kebab/camel-snake-kebab {:mvn/version "0.4.3"}
                     com.rpl/specter {:mvn/version "1.1.4"}
                     metosin/malli {:mvn/version "0.13.0"}}}}
```

### Canonical Data Format Requirements

**CRITICAL**: Production code uses canonical format only. Test code transforms protobuf JSON to match!

1. **Action Names**: Always kebab-case strings (e.g., "rotary-goto-ndc")
2. **Parameter Keys**: Always kebab-case keywords (e.g., `:x`, `:y`, `:channel`)
3. **Enum Values**: Always lowercase strings (e.g., "heat", not "HEAT")
4. **Field Names**: Kebab-case in Transit, transformed from camelCase in tests

**Test-Time JSON Transformation**:
- Protobuf JSON uses Java conventions (camelCase fields, UPPER_CASE enums)
- Test code transforms this to Clojure conventions (kebab-case, lowercase)
- Production NEVER sees or transforms protobuf JSON
- Transformation uses Cheshire + camel-snake-kebab (test deps only)

### Test-Only Comparison Helpers

```clojure
(ns potatoclient.test.validation-helpers
  "Test-only helpers for protobuf JSON validation.
   These transformations NEVER happen in production code!"
  (:require [cheshire.core :as json]
            [camel-snake-kebab.core :as csk]
            [com.rpl.specter :as sp]
            [clojure.string :as str])
  (:import [java.lang Math]))

(def FLOAT-EPSILON 0.0001)

(defn parse-validation-log
  "Extract validation data from Transit log message"
  [log-msg]
  (when (= (:logger log-msg) "validation")
    (let [data (:data log-msg)]
      (when (= (:type data) "validation-result")
        {:action (:action data)
         :valid (:valid data)
         :violations (:violations data)
         :protobuf-edn (when-let [json-str (:protobuf-json data)]
                         (parse-protobuf-json json-str))
         :original-params (:original-params data)}))))

(defn parse-protobuf-json
  "Parse protobuf JSON and transform to canonical EDN format"
  [json-str]
  ;; Use Cheshire with camel-snake-kebab for canonical transformation
  (-> json-str
      (json/parse-string csk/->kebab-case-keyword)
      (transform-protobuf-values)))

(defn transform-protobuf-values
  "Transform protobuf values to canonical format (TEST ONLY!)"
  [data]
  ;; Walk the data structure and transform values
  (sp/transform [sp/ALL-VALS string?]
                (fn [s]
                  ;; Convert UPPER_CASE enums to lowercase
                  (if (re-matches #"^[A-Z_]+$" s)
                    (-> s
                        (str/replace "_" "-")
                        str/lower-case)
                    s))
                data))

(defn float-equal?
  "Compare floats with epsilon tolerance"
  [a b epsilon]
  (< (Math/abs (- (double a) (double b))) epsilon))

(defn find-command-data
  "Navigate protobuf JSON to find command data using Specter"
  [proto-edn action]
  ;; After transformation, structure should be canonical:
  ;; {:rotary {:rotate-to-ndc {:channel "heat" :x 0.5 :y -0.5}}}
  (let [category (action->category action)
        command (action->command-key action)]
    (get-in proto-edn [category command])))

(defn verify-data-match!
  "Verify Transit params match protobuf data after transformation"
  [params proto-data action]
  ;; After JSON transformation, data should match exactly
  (doseq [[k v] params]
    (testing (str "Parameter " k " in action " action)
      ;; Keys and values should match exactly
      (let [proto-val (get proto-data k)]
        (cond
          (float? v)
          (is (float-equal? v proto-val FLOAT-EPSILON)
              (str "Float mismatch: " v " != " proto-val))
          
          :else
          (is (= v proto-val)
              (str "Value mismatch for " k ": " v " != " proto-val
                   " (proto keys: " (keys proto-data) ")")))))))

;; Helper functions for canonical transformation
(defn action->category
  "Extract category from action name"
  [action]
  (let [parts (str/split action #"-")]
    (case (first parts)
      "rotary" :rotary
      "gps" :gps
      "compass" :compass
      "cv" :cv
      "day-camera" :day-camera
      "heat-camera" :heat-camera
      "lrf" :lrf
      "osd" :osd
      "system" :system
      ;; Basic commands have no category
      (keyword action))))

(defn action->command-key
  "Extract command key from action name"
  [action]
  (if (contains? #{"ping" "noop" "frozen"} action)
    (keyword action)
    (let [parts (str/split action #"-" 2)]
      (keyword (second parts)))))
```

### Unit Test Structure

```java
// Example: ActionRegistryTest.java
public class ActionRegistryTest {
    @Test
    public void testKnownAction() {
        assertTrue(ActionRegistry.isKnownAction("ping"));
        assertTrue(ActionRegistry.isKnownAction(TransitFactory.keyword("ping")));
    }
    
    @Test
    public void testUnknownAction() {
        assertFalse(ActionRegistry.isKnownAction("unknown-action"));
    }
    
    @Test
    public void testParameterValidation() {
        Keyword action = TransitFactory.keyword("rotary-goto-ndc");
        Map<Keyword, Object> validParams = new HashMap<>();
        validParams.put(TransitFactory.keyword("channel"), "heat");
        validParams.put(TransitFactory.keyword("x"), 0.5);
        validParams.put(TransitFactory.keyword("y"), -0.5);
        
        assertTrue(ActionRegistry.validateParams(action, validParams));
    }
    
    @Test
    public void testMissingRequiredParam() {
        Keyword action = TransitFactory.keyword("rotary-goto-ndc");
        Map<Keyword, Object> invalidParams = new HashMap<>();
        invalidParams.put(TransitFactory.keyword("channel"), "heat");
        // Missing x and y
        
        assertFalse(ActionRegistry.validateParams(action, invalidParams));
    }
}
```

### Integration Test Structure

```clojure
;; test/potatoclient/transit/action_registry_test.clj
(deftest test-action-registry-integration
  (testing "Action discovery from Clojure"
    (let [actions (vec (.getAllActionKeywords ActionRegistry))]
      (is (contains? (set actions) :ping))
      (is (contains? (set actions) :rotary-goto-ndc))))
  
  (testing "Parameter validation from Clojure"
    (let [valid-params {:channel "heat" :x 0.5 :y -0.5}
          invalid-params {:channel "heat"}] ; missing x, y
      (is (.validateParams ActionRegistry :rotary-goto-ndc valid-params))
      (is (not (.validateParams ActionRegistry :rotary-goto-ndc invalid-params)))))
  
  (testing "Spec generation"
    (let [spec (generate-command-spec :rotary-goto-ndc)]
      (is (m/validate spec {:action :rotary-goto-ndc
                            :params {:channel "heat" :x 0.5 :y -0.5}}))
      (is (not (m/validate spec {:action :rotary-goto-ndc
                                 :params {:channel "heat"}}))))))

(deftest test-comprehensive-validation
  ;; IMPORTANT: This test only runs with :test alias (not in production)
  (when (validation-enabled?)
    (testing "Valid data passes both Malli and protobuf validation"
      (doseq [command (get-all-test-commands)]
        (let [{:keys [action params]} command
              spec (generate-command-spec action)]
          ;; Malli validation (in-process)
          (is (m/validate spec {:action action :params params})
              (str "Malli validation failed for " action))
          
          ;; Protobuf validation (subprocess via Transit IPC)
          (let [result (validate-in-subprocess action params)
                {:keys [valid protobuf-json violations]} result]
            (is valid (str "Protobuf validation failed for " action))
            
            ;; Verify protobuf JSON matches our params
            (when valid
              (let [proto-data (json/parse-string protobuf-json true)]
                (verify-proto-matches-params proto-data params action)))))))
    
    (testing "Both validators reject same invalid data"
      (doseq [invalid-case (get-invalid-test-cases)]
        (let [{:keys [action params expected-field]} invalid-case
              spec (generate-command-spec action)]
          ;; Both should reject
          (is (not (m/validate spec {:action action :params params})))
          (let [result (validate-in-subprocess action params)]
            (is (not (:valid result)))
            (is (some #(str/includes? (:field %) expected-field) 
                      (:violations result)))))))))

(defn verify-proto-matches-params
  "Verify protobuf JSON representation matches our Transit params"
  [proto-data params action]
  ;; Handle nested command structures
  (let [proto-cmd (get-proto-command-data proto-data action)]
    (doseq [[k v] params]
      (let [proto-key (->proto-field-name k)
            proto-val (get proto-cmd proto-key)]
        (cond
          ;; Float comparison with epsilon
          (float? v)
          (is (float-equal? v proto-val 0.0001)
              (str "Float mismatch for " k ": " v " != " proto-val))
          
          ;; Enum comparison (string in params, may be different in proto)
          (enum-field? action k)
          (is (= (str/upper-case v) (str/upper-case proto-val))
              (str "Enum mismatch for " k))
          
          ;; Direct comparison
          :else
          (is (= v proto-val)
              (str "Value mismatch for " k ": " v " != " proto-val)))))))

(defn float-equal?
  "Compare floats with epsilon tolerance"
  [a b epsilon]
  (< (Math/abs (- a b)) epsilon))
  
  (testing "Invalid data is rejected by both validators"
    (doseq [invalid-case (get-invalid-test-cases)]
      (let [{:keys [action params reason]} invalid-case
            spec (generate-command-spec action)]
        ;; Malli should reject
        (is (not (m/validate spec {:action action :params params}))
            (str "Malli should reject: " reason))
        
        ;; Protobuf should also reject
        (let [result (send-and-validate-command action params)]
          (is (= :invalid (:validation-status result))
              (str "Protobuf should reject: " reason))
          (is (contains? (:violations result) (:expected-field invalid-case))
              (str "Expected violation on field " (:expected-field invalid-case))))))))

(deftest test-validation-edge-cases
  (testing "Boundary values"
    ;; Test exact boundary values for numeric constraints
    (let [test-cases [{:action :rotary-set-platform-azimuth
                       :params {:value 359.99999} ; Just under 360
                       :valid? true}
                      {:action :rotary-set-platform-azimuth
                       :params {:value 360.0} ; Exactly at boundary (invalid)
                       :valid? false}
                      {:action :gps-set-manual-position
                       :params {:latitude 90.0 :longitude 0 :altitude 0}
                       :valid? true}
                      {:action :gps-set-manual-position
                       :params {:latitude 90.1 :longitude 0 :altitude 0}
                       :valid? false}]]
      (doseq [{:keys [action params valid?]} test-cases]
        (let [spec (generate-command-spec action)]
          (is (= valid? (m/validate spec {:action action :params params}))
              (str "Boundary test failed for " action " with " params)))))))
```

### End-to-End Test Structure

```kotlin
// ValidationSubprocess.kt - Runs as separate process
class ValidationSubprocess {
    fun main(args: Array<String>) {
        // This subprocess handles all protobuf validation
        // Communicates via Transit over stdin/stdout
        TransitCommunicator().start { message ->
            when (message["type"]) {
                "validate" -> {
                    val action = message["action"] as String
                    val params = message["params"] as Map<*, *>
                    
                    // Build protobuf (with reflection allowed here)
                    val protoCmd = SimpleCommandHandlers.buildCommand(action, params)
                    
                    // Validate with buf.validate
                    val violations = Validator.validate(protoCmd)
                    
                    // Return validation result via Transit
                    mapOf(
                        "validation-status" to if (violations.isEmpty()) "valid" else "invalid",
                        "violations" to violations.map { it.toMap() }
                    )
                }
            }
        }
    }
}

// TransitActionFlowTest.kt - Main test process
class TransitActionFlowTest {
    @Test
    fun testCompleteCommandFlow() {
        // Test runs WITHOUT loading protobuf classes
        val command = mapOf(
            "action" to "rotary-goto-ndc",
            "params" to mapOf(
                "channel" to "heat",
                "x" to 0.5,
                "y" to -0.5
            )
        )
        
        // Validate with registry (no protobuf)
        val action = command["action"] as String
        assertTrue(ActionRegistry.isKnownAction(action))
        
        // Only in dev/test: validate via subprocess
        if (isTestEnvironment()) {
            val result = validateViaSubprocess(command)
            assertTrue(result["validation-status"] == "valid")
        }
    }
    
    @Test
    fun testInvalidDataRejection() {
        // Test 1: Out of range coordinates
        val invalidCommand1 = mapOf(
            "action" to "rotary-goto-ndc",
            "params" to mapOf(
                "channel" to "heat",
                "x" to 2.5,  // Invalid: > 1.0
                "y" to -0.5
            )
        )
        
        // Should fail Action Registry validation
        assertFalse(ActionRegistry.validateParams("rotary-goto-ndc", invalidCommand1["params"]))
        
        // Should also fail protobuf validation if we force-build it
        val protoCmd = forceBuilldCommand(invalidCommand1)
        val violations = Validator.validate(protoCmd)
        assertTrue(violations.isNotEmpty())
        assertTrue(violations.any { it.fieldPath.contains("x") })
        
        // Test 2: Missing required parameter
        val invalidCommand2 = mapOf(
            "action" to "rotary-goto-ndc",
            "params" to mapOf(
                "channel" to "heat"
                // Missing x and y
            )
        )
        
        assertFalse(ActionRegistry.validateParams("rotary-goto-ndc", invalidCommand2["params"]))
        
        // Test 3: Invalid enum value
        val invalidCommand3 = mapOf(
            "action" to "rotary-goto-ndc",
            "params" to mapOf(
                "channel" to "invalid-channel",  // Not a valid enum
                "x" to 0.5,
                "y" to -0.5
            )
        )
        
        assertFalse(ActionRegistry.validateParams("rotary-goto-ndc", invalidCommand3["params"]))
    }
}
```

## Success Criteria

1. **100% Test Coverage** - All registry methods tested (excluding subprocess validation)
2. **All Commands Registered** - Every protobuf command has registry entry
3. **Zero Reflection** - No reflection in main process (protobuf isolated to subprocess)
4. **Performance** - Registry lookups < 1ms, validation < 5ms (test mode only)
5. **Error Messages** - Clear, actionable error messages
6. **Documentation** - Every action documented
7. **Type Safety** - No runtime type errors
8. **Validation Parity** - Malli and buf.validate constraints aligned
9. **Invalid Data Handling** - All invalid cases properly rejected
10. **Comprehensive Test Suite** - Valid + invalid data for all ~170 commands
11. **Zero Production Validation** - No validation code runs in release builds
12. **Clean Dependencies** - No protobuf classes in main Java/Clojure code
13. **Test Performance** - All validation tests complete in < 30 seconds

## Rollback Plan

If issues arise:
1. Registry can be disabled via feature flag
2. Old implementation remains until fully validated
3. Gradual rollout by command type
4. A/B testing in development environment

## Validation Test Matrix

### Canonical Data Format Enforcement

**IMPORTANT**: We enforce canonical data format across all systems. Any format mismatch is a BUG that must be fixed, not worked around!

1. **Expected Canonical Format**:
   - Transit: `{:channel "heat"}` (kebab-case keywords, lowercase strings)
   - Protobuf JSON: Must also be `{"channel": "heat"}` (kebab-case, lowercase)
   - Action names: Always `"rotary-goto-ndc"` (kebab-case strings)

2. **Test-Time Validation**:
   - Tests FAIL if formats don't match exactly
   - No conversion helpers in production code
   - Force alignment at source (protobuf serialization config)

3. **Type Comparisons**:
   - Floats: Epsilon comparison (0.0001) for precision only
   - All other types: Exact match required
   - No case conversion, no field name mapping

4. **JSON Transformation Approach**:
   - Use Cheshire + camel-snake-kebab in tests only
   - Transform protobuf JSON to canonical EDN format
   - Convert camelCase/PascalCase to kebab-case
   - Convert UPPER_CASE enums to lowercase
   - After transformation, data should match exactly

### Protobuf JSON Transformation Strategy

```clojure
(ns potatoclient.test.validation-helpers
  (:require [cheshire.core :as json]
            [camel-snake-kebab.core :as csk]
            [com.rpl.specter :as sp]))

;; Example transformation:
;; Input:  {"rotary": {"rotateToNdc": {"channel": "HEAT", "x": 0.5}}}
;; Output: {:rotary {:rotate-to-ndc {:channel "heat" :x 0.5}}}

;; This transformation happens ONLY in tests
;; Production code never sees protobuf JSON
```

### Example: RotateToNDC Command

| Test Case | Input | Expected Result | Validation Layer |
|-----------|-------|-----------------|------------------|
| Valid | `{channel: "heat", x: 0.5, y: -0.5}` | ✓ Pass | Both |
| Missing channel | `{x: 0.5, y: -0.5}` | ✗ Fail | Both |
| Invalid channel | `{channel: "invalid", x: 0.5, y: -0.5}` | ✗ Fail | Both |
| X out of range | `{channel: "heat", x: 1.5, y: -0.5}` | ✗ Fail | Both |
| Y out of range | `{channel: "heat", x: 0.5, y: -1.1}` | ✗ Fail | Both |
| Wrong type | `{channel: "heat", x: "0.5", y: -0.5}` | ✗ Fail | Malli |
| Null values | `{channel: "heat", x: null, y: -0.5}` | ✗ Fail | Both |
| Extra params | `{channel: "heat", x: 0.5, y: -0.5, z: 0}` | ✗ Fail | Registry |
| Boundary exact | `{channel: "heat", x: 1.0, y: -1.0}` | ✓ Pass | Both |
| Boundary over | `{channel: "heat", x: 1.0001, y: -1.0001}` | ✗ Fail | Both |

### Testing All ~170 Commands

Each command will have:
1. **5-10 valid test cases** covering typical usage
2. **10-15 invalid test cases** covering all failure modes
3. **Boundary tests** for numeric constraints
4. **Type confusion tests** (string vs number, etc.)
5. **Null/undefined tests**
6. **Performance benchmarks**

Total estimated test cases: ~3,000-4,000

## Future Enhancements

1. **Command Aliases** - Alternative names for commands
2. **Deprecation Support** - Mark old commands as deprecated
3. **Versioning** - Support multiple protocol versions
4. **Metrics** - Track command usage statistics
5. **Dynamic Registration** - Plugin system for custom commands
6. **Validation Reports** - Detailed HTML reports of validation mismatches
7. **Fuzzing Support** - Automatic invalid data generation

## Implementation Notes

### Critical Design Decisions

1. **Subprocess as Command Processor**: The validation subprocess extends CommandSubprocess to reuse existing Transit infrastructure
2. **JSON Log Messages**: Validation results sent as structured JSON within Transit log messages for easy parsing
3. **Protobuf JSON Format**: Using protobuf's native JSON conversion for accurate field representation
4. **Epsilon Comparisons**: Float values compared with 0.0001 tolerance to handle conversion precision
5. **Canonical Format**: No name mapping in production - test failures force format alignment
6. **No Production Impact**: All validation code isolated to test builds only
7. **JSON Transformation**: Protobuf JSON transformed to EDN only in tests, never in production

### Reference Implementation Examples

#### 1. Protobuf Validation Annotations

See proto files in `examples/protogen/proto/` for buf.validate annotations:

```protobuf
// jon_shared_cmd_rotary.proto
message RotateToNDC {
  ser.JonGuiDataVideoChannel channel = 1 [(buf.validate.field).enum = {
    defined_only: true,
    not_in: [0]
  }];
  float x = 2 [(buf.validate.field).float = {
    gte: -1.0,
    lte: 1.0
  }];
  float y = 3 [(buf.validate.field).float = {
    gte: -1.0,
    lte: 1.0
  }];
}

// jon_shared_cmd_gps.proto
message SetManualPosition {
  float latitude = 1 [(buf.validate.field).float = {
    gte: -90.0,
    lte: 90.0
  }];
  float longitude = 2 [(buf.validate.field).float = {
    gte: -180.0,
    lt: 180.0
  }];
  float altitude = 3 [(buf.validate.field).float = {
    gte: -432.0,  // Dead Sea
    lte: 8848.0   // Everest
  }];
}
```

#### 2. TypeScript Command Implementation

Reference implementation in `examples/web/frontend/ts/cmd/cmdSender/`:

```typescript
// Shows how commands are built and validated in TypeScript
// Useful for understanding parameter names and structure
// Example: rotaryGotoNDC command builder
export function rotaryGotoNDC(channel: VideoChannel, x: number, y: number) {
  validateRange(x, -1.0, 1.0, "x coordinate");
  validateRange(y, -1.0, 1.0, "y coordinate");
  return {
    rotary: {
      rotateToNdc: {
        channel: channel,
        x: x,
        y: y
      }
    }
  };
}
```

#### 3. Existing Command Usage

Search the codebase for command usage patterns:

```clojure
;; In potatoclient.gestures.handler
(defn handle-tap [gesture-data]
  (commands/send-command! 
    (:stream-key gesture-data)
    "rotary-goto-ndc"
    {:channel (:stream-type gesture-data)
     :x (:ndc-x gesture-data)
     :y (:ndc-y gesture-data)}))

;; In potatoclient.transit.commands
(defn rotary-goto-ndc
  [stream-key channel x y]
  (send-command stream-key "rotary-goto-ndc" 
                {:channel channel :x x :y y}))
```

#### 4. Validation Patterns to Study

- **Numeric Ranges**: Most float validations in proto files
- **Enum Constraints**: `defined_only: true, not_in: [0]` pattern
- **Complex Objects**: See LIRA target with nested validation
- **Optional vs Required**: Check which fields have validation vs which don't

#### 5. Key Files to Reference

- `examples/protogen/proto/jon_shared_cmd_*.proto` - All command definitions with validation
- `examples/web/frontend/ts/cmd/cmdSender/` - TypeScript reference implementation
- `src/potatoclient/transit/commands.clj` - Current command sending implementation
- `src/potatoclient/kotlin/SimpleCommandBuilder.kt` - Kotlin command building logic
- `TODO_PROTO_COMMANDS.md` - Exhaustive list of all ~170 commands with parameters