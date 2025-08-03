# TODO: Kotlin Command Integration and Testing

## Overview
This document tracks the remaining work to finalize the Kotlin command system integration with Clojure and implement comprehensive roundtrip testing.

## Phase 1: Kotlin Command System Finalization

### 1.1 Fix Protobuf Builder Compilation Errors
- [ ] Fix naming mismatches (CV → Cv, OSD → Osd, etc.)
- [ ] Update builder methods to match actual protobuf generated classes
- [ ] Handle special cases (refine_target, stop-a-ll → stopAll)
- [ ] Ensure all builders compile without errors

### 1.2 Complete Protobuf Builder Coverage
- [ ] Verify all commands from `transit/commands.clj` have corresponding builders
- [ ] Add missing builders for any commands
- [ ] Test each builder individually with sample data

## Phase 1.5: Metadata-Based Architecture (NEW APPROACH)

### Overview
Instead of action-based routing, use Transit metadata to specify protobuf types:
- Clojure sends full EDN structure with metadata containing proto class info
- Kotlin Transit handlers use metadata to instantiate correct protobuf class
- Eliminates action registry and naming collisions

### Implementation
- [x] Design metadata architecture (see docs/TRANSIT_METADATA_ARCHITECTURE.md)
- [x] Create `command-sender.clj` with metadata attachment
- [x] Create `ProtobufTransitHandler.kt` for Kotlin side
- [ ] Implement custom Transit handlers for metadata preservation
- [ ] Test with actual protobuf classes
- [ ] Migrate existing commands to new approach

### Benefits
- No action naming schemes needed
- Handles nested messages with same names
- Natural Clojure data structures
- No manual routing in Kotlin
- Type safety via metadata

## Phase 2: Key Canonicalization Strategy

### 2.1 Define Key Format Standards
- [ ] Document Transit keyword format (kebab-case: `frame-timestamp`)
- [ ] Document Protobuf field format (snake_case: `frame_timestamp`)
- [ ] Document JSON field format (which convention to use?)
- [ ] Create conversion utilities for consistent transformation

### 2.2 Implement Key Canonicalization
```kotlin
// Example approach
object KeyCanonicalizer {
    // Transit keyword → Protobuf field name
    fun transitToProto(key: String): String = key.replace("-", "_")
    
    // Protobuf field → Transit keyword
    fun protoToTransit(key: String): String = key.replace("_", "-")
    
    // Ensure consistent JSON representation
    fun canonicalizeJson(json: String): String {
        // Sort keys, normalize format
    }
}
```

### 2.3 JSON Comparison Strategy
- [ ] Decide on JSON key format (snake_case to match proto? kebab-case to match Transit?)
- [ ] Implement JSON normalizer that:
  - Sorts keys alphabetically
  - Handles numeric precision consistently
  - Normalizes enum values (UPPER_CASE vs lower-case)
  - Handles null/missing fields consistently

## Phase 3: Clojure-side Test Data Generation

### 3.1 Leverage Proto-Explorer Specs
- [ ] Use `proto-explorer.test-data-generator` to generate valid test data
- [ ] Create test data generator that produces commands via `transit/commands.clj` functions
- [ ] Ensure generated data respects buf.validate constraints

### 3.2 Test Data Generator
```clojure
(ns potatoclient.test.command-generator
  (:require [proto-explorer.generated-specs :as specs]
            [proto-explorer.test-data-generator :as gen]
            [potatoclient.transit.commands :as cmd]))

(defn generate-test-command [action-name]
  ;; 1. Look up spec for the action
  ;; 2. Generate valid data using proto-explorer
  ;; 3. Call appropriate command function
  ;; 4. Return Transit message
  )

(defn generate-all-test-commands []
  ;; Generate test cases for all known commands
  )
```

## Phase 4: Roundtrip Testing Implementation

### 4.1 Clojure → Kotlin → Protobuf → Validation
- [ ] Clojure generates command using specs
- [ ] Sends via Transit to Kotlin subprocess
- [ ] Kotlin builds protobuf
- [ ] Validate with buf.validate (if protovalidate-java added)
- [ ] Serialize to JSON
- [ ] Send JSON back to Clojure
- [ ] Compare original command data with JSON

### 4.2 Test Harness Structure
```clojure
;; Clojure side
(deftest roundtrip-all-commands
  (doseq [cmd (generate-all-test-commands)]
    (let [json-response (send-to-kotlin-and-get-json cmd)
          original-data (:params cmd)
          proto-data (parse-json json-response)]
      ;; Compare with canonicalization
      (is (= (canonicalize original-data)
             (canonicalize proto-data))))))
```

```kotlin
// Kotlin side - test mode that returns JSON instead of sending
class TestCommandProcessor {
    fun processForTest(action: String, params: Map<*, *>): String {
        val result = ProtobufCommandBuilder.build(action, params)
        return when (result) {
            is Success -> {
                val proto = result.value
                // Validate if protovalidate available
                // Convert to canonical JSON
                JsonFormat.printer()
                    .includingDefaultValueFields()
                    .sortingMapKeys()  // If available
                    .print(proto)
            }
            is Failure -> {
                // Return error as JSON
            }
        }
    }
}
```

### 4.3 Validation Integration ✅
- [x] protovalidate-java dependency already included (build.buf/protovalidate 0.13.0)
- [x] Protobuf bindings generated with buf.validate annotations
- [x] Validator integrated in TestCommandProcessor
- [x] BufValidateTest created to verify constraint validation
- [ ] Add validation to production CommandSubprocess (optional - may impact performance)

## Phase 5: Edge Cases and Error Handling

### 5.1 Test Edge Cases
- [ ] Missing required fields
- [ ] Out-of-range numeric values
- [ ] Invalid enum values
- [ ] Null vs missing optional fields
- [ ] Very large/small numbers (precision limits)
- [ ] String length constraints
- [ ] Special characters in strings

### 5.2 Error Reporting
- [ ] Standardize error format from Kotlin
- [ ] Include field path in validation errors
- [ ] Distinguish between build errors vs validation errors
- [ ] Test error propagation back to Clojure

## Phase 6: Performance Testing

### 6.1 Benchmark Suite
- [ ] Measure Transit parsing time
- [ ] Measure protobuf building time
- [ ] Measure validation time (if using buf.validate)
- [ ] Measure JSON serialization time
- [ ] Compare with direct protobuf construction

### 6.2 Optimization Opportunities
- [ ] Cache protobuf builders?
- [ ] Reuse Transit readers/writers?
- [ ] Batch command processing?

## Phase 7: Documentation and Maintenance

### 7.1 Documentation Updates
- [ ] Document the command flow without Action Registry
- [ ] Explain key format conversions
- [ ] Provide examples of adding new commands
- [ ] Document the test strategy

### 7.2 Maintenance Tools
- [ ] Script to verify all commands have tests
- [ ] Script to check command coverage
- [ ] GitHub Action for roundtrip tests

## Implementation Order

1. **First**: Fix Kotlin compilation (Phase 1.1)
2. **Second**: Implement key canonicalization (Phase 2)
3. **Third**: Create Clojure test generators (Phase 3)
4. **Fourth**: Implement basic roundtrip tests (Phase 4)
5. **Then**: Add validation and edge cases (Phase 5)
6. **Finally**: Performance testing and documentation (Phases 6-7)

## Notes

### Key Format Considerations
- Transit uses keywords with kebab-case (`:frame-timestamp`)
- Protobuf uses snake_case (`frame_timestamp`)
- JSON could use either - need to decide
- Canonicalization must handle all three formats

### Validation Options
Without protovalidate-java:
- Basic type checking via protobuf
- Range validation would need manual implementation
- Regex patterns would need manual implementation

With protovalidate-java:
- Full buf.validate constraint checking
- Better error messages
- Additional dependency and complexity

### Testing Strategy
The roundtrip tests will catch:
- Key naming mismatches
- Type conversion issues
- Missing/extra fields
- Precision loss
- Enum handling differences

This comprehensive testing will ensure the Transit → Protobuf conversion is correct and reliable.