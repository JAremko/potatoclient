# TODO: Kotlin Command Integration and Testing

## Overview
This document tracks the remaining work to finalize the Kotlin command system integration with Clojure and implement comprehensive roundtrip testing.

## Phase 0: Proto-Explorer Usage for Finding Correct Names

Proto-Explorer is an essential tool for discovering the correct protobuf class names, field names, and structure. Use it before making any code changes.

### Basic Usage Workflow

**Note**: Proto-Explorer now outputs JSON by default for easy tool integration!

```bash
# 1. Find specs using fuzzy search (from proto-explorer directory)
cd tools/proto-explorer
bb find "track"           # Find all track-related specs
bb find "start track"     # Multi-word search
bb find CV                # Find all CV-related specs

# JSON output example:
{
  "found" : 2,
  "specs" : [ {
    "spec" : "potatoclient.specs.cmd.CV/start-track-ndc",
    "namespace" : "potatoclient.specs.cmd.CV",
    "name" : "start-track-ndc"
  }, ... ]
}

# 2. Get exact spec definition with constraints
bb spec :potatoclient.specs.cmd.CV/start-track-ndc
# Returns JSON with definition

# 3. Generate example data respecting constraints
bb example :potatoclient.specs.cmd.CV/start-track-ndc
# Returns: {"spec": "...", "example": {"channel": 1, "x": 0.5, "y": -0.3}}

# 4. Get Java class information (slower - starts JVM)
bb java-class StartTrackNDC
bb java-fields StartTrackNDC
bb java-builder StartTrackNDC

# 5. Use EDN output if needed (for Clojure tools)
./proto-explorer --edn find "track"
```

### Finding Correct Protobuf Names
The protobuf class names don't always match what you'd expect:
- `StartTrackNdc` in Kotlin → `StartTrackNDC` in Java protobuf
- `setSetAgc` method → `setAgc` actual setter
- Package names: `cmd.CV` not `cmd.cv`

### Example: Fixing CV Command Builder
```bash
# 1. Find all CV commands
bb find CV
# Returns list of CV-related specs

# 2. Check specific command structure
bb spec :potatoclient.specs.cmd.CV/start-track-ndc
# Shows the exact field names and types

# 3. Verify Java class name
bb java-class "cmd.CV.JonSharedCmdCv\$StartTrackNDC"
# Shows actual Java class structure
```

### Proto-Explorer Key Features
- **JSON output by default**: Easy integration with tools and scripts
- **Fuzzy search**: Typo-tolerant, case-insensitive searching
- **Constraint awareness**: Shows buf.validate constraints in specs
- **Example generation**: Creates valid test data respecting all constraints
- **Java reflection**: Can inspect actual protobuf classes (via JVM)
- **Batch processing**: Can process multiple queries via stdin
- **Proto type mapping**: Auto-generates domain→Java class mapping
- **EDN output option**: Use `./proto-explorer --edn` for Clojure tools

### Regenerating Proto Type Mapping
When protobuf definitions change, regenerate the mapping:
```bash
cd tools/proto-explorer
bb generate-proto-mapping
# Generates: shared/specs/protobuf/proto_type_mapping.clj
```

### Batch Processing with Proto-Explorer
For complex investigations, use batch mode:
```bash
# Create batch queries
cat > queries.edn <<EOF
[{:op :find :pattern "rotary"}
 {:op :spec :spec :potatoclient.specs.cmd.RotaryPlatform/rotate-to-ndc}
 {:op :example :spec :potatoclient.specs.cmd.RotaryPlatform/rotate-to-ndc}]
EOF

# Execute batch
bb batch < queries.edn

# Returns JSON with all results:
{
  "batch-results" : [ 
    {"op": "find", "pattern": "rotary", "results": [...]},
    {"op": "spec", "spec": "...", "definition": [...]},
    {"op": "example", "spec": "...", "example": {...}}
  ]
}
```

## Phase 1: Kotlin Command System Finalization

### 1.1 Fix Protobuf Builder Compilation Errors
- [ ] Use proto-explorer to find correct class names for each error
- [ ] Fix naming mismatches (CV → Cv, OSD → Osd, StartTrackNdc → StartTrackNDC, etc.)
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

### The Problem
In deeply nested protobuf structures, the same message names appear in different contexts:
```protobuf
// Different "Start" messages in different contexts
message RotaryPlatform {
  message Root {
    oneof data {
      Start start = 1;    // cmd.JonSharedCmdRotaryPlatform$Start
      Stop stop = 2;
    }
  }
}

message VideoRecording {
  message Root {
    oneof data {
      Start start = 1;    // cmd.JonSharedCmdVideoRecording$Start (different class!)
      Stop stop = 2;
    }
  }
}
```

### The Solution: Auto-Generated Proto Type Mapping
Proto-explorer now generates a mapping from command domains to Java protobuf classes automatically!

**Generated Mapping** (from `bb generate-proto-mapping`):
```clojure
;; Auto-generated in shared/specs/protobuf/proto_type_mapping.clj
{:rotary-platform "cmd.RotaryPlatform.JonSharedCmdRotary$Root"
 :cv              "cmd.CV.JonSharedCmdCv$Root"
 :compass         "cmd.Compass.JonSharedCmdCompass$Root"
 :day-camera      "cmd.DayCamera.JonSharedCmdDayCamera$Root"
 :heat-camera     "cmd.HeatCamera.JonSharedCmdHeatCamera$Root"
 ;; ... etc
}
```

**Handling Nested Protobuf Structures**:
The mapping correctly handles deeply nested protobuf structures like those in `jon_shared_cmd_rotary.proto`:

```protobuf
// Proto structure
package cmd.RotaryPlatform;

message Root {
  oneof cmd {
    RotateToNDC rotate_to_ndc = 12;
    ScanStart scan_start = 13;
    // ... many more nested messages
  }
}
```

This generates the Java class `cmd.RotaryPlatform.JonSharedCmdRotary$Root` which contains all nested command types. The inference function correctly maps:
- `{:rotary-platform {:rotate-to-ndc {:x 0.5}}}` → `"cmd.RotaryPlatform.JonSharedCmdRotary$Root"`
- `{:cv {:start-track-ndc {:channel 1}}}` → `"cmd.CV.JonSharedCmdCv$Root"`

The Root message type contains the oneof with all possible commands for that domain.

**Clojure Side:**
```clojure
;; Old string-based approach:
(send-command "rotaryplatform-goto" {:azimuth 180.0 :elevation 45.0})

;; New approach - proto type is inferred automatically:
(send-command {:rotary-platform {:goto {:azimuth 180.0 :elevation 45.0}}})

;; The system automatically:
;; 1. Detects :rotary-platform key
;; 2. Looks up proto type: "cmd.RotaryPlatform.JonSharedCmdRotary$Root"
;; 3. Attaches metadata for Kotlin to use
```

**Key Benefits**:
1. **Zero manual mapping** - Proto-explorer generates it from descriptors
2. **Always up-to-date** - Regenerate when protos change
3. **No spec paths needed** - Proto type is sufficient
4. **Natural command structure** - Commands look like data
5. **Automatic validation** - Against the base command spec

**Kotlin Side:**
The Transit handler reads the metadata and instantiates the correct protobuf class:
```kotlin
class ProtobufReadHandler : ReadHandler<Message, Map<*, *>> {
    override fun fromRep(rep: Map<*, *>): Message {
        val protoType = rep["proto-type"] as String
        
        // Use reflection to instantiate the exact protobuf class
        val clazz = Class.forName(protoType)
        val builder = clazz.getMethod("newBuilder").invoke(null) as Message.Builder
        
        // Populate from the data
        populateBuilder(builder, data)
        return builder.build()
    }
}
```

### Phase 1: Core Infrastructure (Week 1)
- [ ] **1.1 Fix Kotlin Compilation Errors** (Priority: CRITICAL)
  - [ ] Fix protobuf class naming mismatches (CV → Cv, OSD → Osd)
  - [ ] Update all builder methods to match generated classes
  - [ ] Ensure basic compilation succeeds
  - **Test**: Run `make test-kotlin` to verify compilation
  
- [x] **1.2 Create Metadata Embedding System**
  - [x] Implement `metadata-handler.clj` with embed/extract functions
  - [x] Create type-wrapped command structure
  - [x] Add proto-type registry mapping paths to classes
  - **Test**: `metadata_command_test.clj` - verify metadata attachment

- [ ] **1.3 Update Transit Communication**
  - [ ] Modify `transit/core.clj` to use metadata handlers
  - [ ] Ensure wrapped commands serialize correctly
  - [ ] Test roundtrip through Transit
  - **Test**: Verify metadata survives Transit encode/decode cycle

### Phase 2: Kotlin Handler Implementation (Week 1-2)
- [ ] **2.1 Implement ProtobufTransitHandler**
  - [ ] Complete `fromRep` method with reflection
  - [ ] Handle nested message building
  - [ ] Support all field types (scalar, repeated, nested)
  - **Test**: `ProtobufRoundtripTest.kt` - test each field type

- [x] **2.2 Create MetadataCommandSubprocess**
  - [x] Replace action-based routing
  - [x] Use metadata to instantiate protobuf classes
  - [x] Handle errors gracefully
  - **Test**: `MetadataCommandSubprocessTest.kt` - mock Transit messages

- [ ] **2.3 Integration Testing**
  - [ ] Test simple commands (ping, noop)
  - [ ] Test parameterized commands (goto, zoom)
  - [ ] Test deeply nested structures
  - **Test**: `kotlin_integration_test.clj` with actual subprocess

### Phase 3: Command Migration (Week 2)
- [ ] **3.1 Update Command Functions**
  - [ ] Migrate `transit/commands.clj` to use `send-command`
  - [ ] Keep backward compatibility temporarily
  - [ ] Add deprecation warnings to old functions
  - **Test**: All existing command tests should still pass

- [ ] **3.2 Proto-Explorer Integration**
  - [ ] Use proto-explorer specs for validation
  - [ ] Generate test data for all command types
  - [ ] Validate commands before sending
  - **Test**: `command_roundtrip_test.clj` with generated data

- [x] **3.3 Buf.Validate Integration**
  - [x] Validate protobuf after building
  - [x] Return validation errors to Clojure
  - [x] Test constraint violations
  - **Test**: `BufValidateTest.kt` - test all constraints

### Phase 4: Full System Testing (Week 2-3)
- [ ] **4.1 End-to-End Testing**
  - [ ] Test all commands with real subprocess
  - [ ] Verify correct protobuf generation
  - [ ] Check WebSocket transmission
  - **Test**: Create `system_integration_test.clj`

- [ ] **4.2 Performance Testing**
  - [ ] Benchmark metadata vs action approach
  - [ ] Measure reflection overhead
  - [ ] Test with high command volume
  - **Test**: `performance_benchmark_test.clj`

- [ ] **4.3 Error Scenario Testing**
  - [ ] Unknown proto types
  - [ ] Malformed metadata
  - [ ] Missing required fields
  - [ ] Type mismatches
  - **Test**: `error_handling_test.clj`

### Phase 5: Cleanup and Documentation (Week 3)
- [ ] **5.1 Remove Legacy Code**
  - [ ] Delete action-based routing
  - [ ] Remove ProtobufCommandBuilder switch statements
  - [ ] Clean up old command builders
  - **Test**: All tests still pass after removal

- [ ] **5.2 Remove Outdated Key Conversion Code**
  - [ ] Delete `src/potatoclient/transit/keyword_handlers.clj` (manual conversion obsolete)
  - [ ] Delete `test/potatoclient/transit/keyword_conversion_test.clj`
  - [ ] Review/update `src/potatoclient/java/transit/EnumUtils.java`
  - [ ] Remove manual case conversion in Kotlin files:
    - `MetadataCommandSubprocess.kt`
    - `ProtobufTransitHandler.kt`
    - `ProtobufStateHandlers.kt`
    - `StateSubprocess.kt`
  - **Test**: Automatic Transit conversion still works

- [ ] **5.3 Clean Up Outdated Specs**
  - [ ] Delete the entire `src/potatoclient/specs/` directory EXCEPT:
    - Keep `src/potatoclient/specs/transit_messages.clj` (subprocess communication, not protobuf-related)
  - [ ] This removes:
    - `src/potatoclient/specs/cmd/rotary.clj` (manual command specs)
    - All files in `src/potatoclient/specs/data/` (9 files: camera, compass, gps, lrf, rotary, state, system, time, types)
  - [ ] Clean up `src/potatoclient/specs.clj`:
    - Remove legacy command payload specs (lines 172-188)
    - Remove temporary enum definitions (lines 585-633)
    - Remove redundant command domain schemas (lines 420-546)
    - Remove protobuf-related type specs (lines 550-850)
    - Keep only core app specs (themes, locales, UI components, process management)
  - [ ] Delete empty placeholder specs in shared:
    - `shared/specs/protobuf/cmd_specs.clj` (empty placeholder)
    - `shared/specs/protobuf/state_specs.clj` (empty placeholder)
  - [ ] Use only proto-explorer generated specs from `shared/specs/protobuf/`
  - **Test**: All spec validations use generated specs

- [ ] **5.4 Remove Legacy Command Functions**
  - [ ] Delete `src/potatoclient/transit/commands.clj` (263 lines of individual functions)
  - [ ] Delete `src/potatoclient/transit/command_sender.clj` (hardcoded registry)
  - [ ] Migrate any still-needed functionality to metadata approach
  - **Test**: Commands still work without legacy functions

- [ ] **5.5 Clean Up Test Files**
  - [ ] Delete all `.skip` test files:
    - `test/potatoclient/transit/unified_transit_test.clj.skip`
    - `test/potatoclient/state/*.clj.skip` (7 files)
    - `test/potatoclient/proto/serialization_test.clj.skip`
  - [ ] Review and update/delete legacy handler tests:
    - `test/potatoclient/protobuf_handler_test.clj`
    - `test/potatoclient/transit_handler_test.clj`
    - `test/potatoclient/transit_handlers_working_test.clj`
  - **Test**: Remaining tests provide adequate coverage

- [ ] **5.6 Update IPC Message Handling**
  - [ ] Update `src/potatoclient/ipc.clj` to work with proto-explorer types
  - [ ] Remove any hardcoded message type handling
  - **Test**: IPC works with new message types

- [ ] **5.7 Documentation Updates**
  - [ ] Update CLAUDE.md with new architecture
  - [ ] Create developer guide for adding commands
  - [ ] Document troubleshooting steps
  - [ ] Remove references to old approaches
  - **Test**: Follow docs to add a new command

- [ ] **5.8 Final Validation**
  - [ ] Run full test suite
  - [ ] Manual testing of UI commands
  - [ ] Performance comparison report
  - [ ] Verify no old code references remain
  - **Test**: `make test-all` passes

### Benefits
- **No Name Collisions**: Each command carries its full type information
- **No Action Registry**: No need to maintain action-to-class mappings
- **Type Safety with Validation**: Validate against the appropriate spec
- **Natural Clojure Syntax**: Commands look like data, not string-based actions
- **Automatic Proto Discovery**: Type path can be derived from proto structure

### Simplified Validation Approach

**Instead of spec-per-command**, we use a unified validation strategy:

```clojure
;; Single base spec that validates all commands
(def command-spec
  ;; This spec comes from proto-explorer and includes ALL command variations
  :potatoclient.specs.cmd/root)

;; Validate any command against the base spec
(defn validate-command [command]
  (m/validate command-spec command))

;; No need to know the specific sub-spec!
```

**Benefits of this approach**:
1. **Simpler API**: Just pass the command data, no spec selection needed
2. **Single source of truth**: Proto-explorer generates the complete spec
3. **Automatic validation**: The spec knows all valid command shapes
4. **Better errors**: Malli provides clear validation errors for the entire command tree

### Example Usage
```clojure
;; Simple Commands - no spec path needed!
(send-command {:ping {}} [:root :ping])

;; Nested Commands
(send-command {:goto {:azimuth 180.0 :elevation 45.0}}
              [:rotary-platform :goto])

;; Complex nesting
(send-command {:day {:offsets {:set {:x-offset 10 :y-offset -5}}}}
              [:lrf-calib :day :offsets :set])
```

### Testing Strategy at Each Stage
- **Unit Tests**: Test metadata attachment, command building, validation
- **Integration Tests**: Test actual Transit communication, roundtrip data integrity
- **System Tests**: Full command flow from UI to WebSocket
- **Performance Tests**: Measure latency and throughput

### Success Criteria
1. All commands use metadata approach
2. No action-based routing remains
3. All tests pass (unit, integration, system)
4. Documentation is complete
5. Zero compilation warnings

### Risk Mitigation
1. **Reflection Performance**: Cache reflection lookups
2. **Unknown Edge Cases**: Extensive testing at each phase

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

## Implementation Order

1. **First**: Fix Kotlin compilation (Phase 1.1) - CRITICAL BLOCKER
2. **Second**: Complete metadata-based architecture (Phase 1.5)
3. **Third**: Implement Kotlin handlers (Phase 2)
4. **Fourth**: Migrate commands and integrate validation (Phase 3)
5. **Then**: Full system testing (Phase 4)
6. **Finally**: Clean up all legacy code and documentation (Phase 5)

## Summary of Major Cleanup Items

### Legacy Code to Remove:
1. **Manual Key Conversion**: `keyword_handlers.clj` and related code
2. **Placeholder Specs**: Empty `cmd_specs.clj` and `state_specs.clj`
3. **Manual Command Specs**: All hardcoded specs in `specs/cmd/` and `specs/data/`
4. **Legacy Command Functions**: 263 lines in `commands.clj` with individual functions
5. **Skipped Tests**: 9 `.skip` test files from old approaches
6. **Outdated Handlers**: Manual Transit and protobuf handlers

### What to Keep:
- Proto-explorer generated specs (in `shared/specs/protobuf/`)
- Metadata-based command architecture
- Video stream management code (unrelated to commands)
- Core Transit communication infrastructure

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
