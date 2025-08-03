# TODO: Kotlin Command Integration with Reflection-Based Architecture

## Overview
With the keyword tree generation in place, we can now implement a powerful reflection-based architecture that:
1. Automatically converts Transit EDN to Protobuf using keyword trees
2. Validates using buf.validate annotations
3. Tests roundtrip integrity: Transit → Protobuf → Binary → Protobuf → Transit
4. Uses protobuf's built-in equals() and hashCode() for comparison

## Phase 0: Proto-Explorer and Keyword Tree Usage

### Essential Babashka Commands for Development

```bash
# Navigate to proto-explorer directory first
cd tools/proto-explorer

# 1. Generate/regenerate keyword trees (after proto changes)
bb generate-keyword-tree-cmd    # Creates proto_keyword_tree_cmd.clj
bb generate-keyword-tree-state  # Creates proto_keyword_tree_state.clj

# 2. Find protobuf specs using fuzzy search
bb find "track"           # Find all track-related specs
bb find "start track"     # Multi-word search
bb find CV                # Find all CV-related specs

# 3. Get exact spec definition with constraints
bb spec :potatoclient.specs.cmd.CV/start-track-ndc

# 4. Generate example data respecting constraints
bb example :potatoclient.specs.cmd.CV/start-track-ndc

# 5. Get Java class information (slower - starts JVM)
bb java-class StartTrackNDC
bb java-fields StartTrackNDC
bb java-builder StartTrackNDC

# 6. Check statistics
bb stats                  # Shows all loaded specs by package

# 7. List all messages in a package
bb list cmd.CV           # List all CV commands
bb list ser              # List all state messages

# 8. Batch processing for multiple queries
cat > queries.edn <<EOF
[{:op :find :pattern "rotary"}
 {:op :spec :spec :potatoclient.specs.cmd.RotaryPlatform/set-velocity}
 {:op :example :spec :potatoclient.specs.cmd.RotaryPlatform/set-velocity}]
EOF
bb batch < queries.edn
```

### Using Keyword Trees in Development

```clojure
;; Load and examine keyword trees in REPL
(require '[clojure.edn :as edn])

;; Load command tree
(def cmd-tree 
  (-> "shared/specs/protobuf/proto_keyword_tree_cmd.clj"
      slurp
      (subs (+ (.indexOf content "(def keyword-tree") 17))
      (subs 0 (- (count content) 1))
      edn/read-string))

;; Explore available commands
(keys cmd-tree)
;; => (:compass :cv :day-cam-glass-heater :day-camera :frozen :gps ...)

;; Check specific command structure
(get-in cmd-tree [:cv :children :start-track-ndc])
;; => {:java-class "cmd.CV.JonSharedCmdCv$StartTrackNDC" ...}

;; Load state tree
(def state-tree 
  (-> "shared/specs/protobuf/proto_keyword_tree_state.clj" 
      ;; same loading pattern
      ))
```

## New Architecture Benefits

### Automatic Transit Handler Generation
With keyword trees, we can:
- Generate Transit write handlers that know how to serialize each protobuf type
- Generate Transit read handlers that use reflection to build protobuf messages
- No manual routing or action registry needed
- Support both commands and state with the same infrastructure

### Comprehensive Roundtrip Testing
```
Transit EDN → Java Protobuf → buf.validate → Proto Binary → 
Java Protobuf → buf.validate → Transit EDN
```

**Comparison Strategy**:
- Compare the Java Protobuf representations (before serialization and after deserialization)
- Use protobuf's built-in `equals()` method for deep equality check
- Use protobuf's `hashCode()` for consistent hashing
- If comparison fails, use protobuf's built-in JSON converter to visualize differences

**Debugging Failed Comparisons**:
```kotlin
if (protobuf1 != protobuf2) {
    // Convert both to JSON for detailed diff
    val json1 = JsonFormat.printer().print(protobuf1)
    val json2 = JsonFormat.printer().print(protobuf2)
    
    // Log or diff the JSON representations
    println("Expected: $json1")
    println("Actual: $json2")
    
    // Can use any JSON diff tool to see exact differences
}
```

## Phase 1: Reflection-Based Transit Handlers

### 1.1 Generate Transit Write Handlers
Using the keyword trees, generate Kotlin code that:
- [ ] Creates Transit write handlers for each protobuf message type
- [ ] Uses the tree structure to know which fields to extract
- [ ] Handles nested messages recursively
- [ ] Preserves field types and repeated fields

Example generated handler:
```kotlin
// Auto-generated from keyword tree
class StartTrackNDCWriteHandler : WriteHandler<JonSharedCmdCv.StartTrackNDC> {
    override fun tag(o: StartTrackNDC) = "protobuf"
    override fun rep(o: StartTrackNDC) = mapOf(
        "channel" to o.channel,
        "x" to o.x,
        "y" to o.y
    )
}
```

### 1.2 Implement Reflection-Based Read Handler
- [ ] Create `ReflectionProtobufReadHandler` that uses keyword tree
- [ ] Navigate tree based on EDN structure
- [ ] Use reflection to call setter methods
- [ ] Handle all protobuf field types
- [ ] Support repeated fields and oneofs

```kotlin
class ReflectionProtobufReadHandler(
    private val keywordTree: Map<String, Any>
) : ReadHandler<Map<*, *>, Message> {
    
    override fun fromRep(rep: Map<*, *>): Message {
        // Use keyword tree to build protobuf via reflection
        return buildProtobuf(rep, keywordTree)
    }
    
    private fun buildProtobuf(data: Map<*, *>, node: Map<String, Any>): Message {
        val javaClass = Class.forName(node["java-class"] as String)
        val builder = javaClass.getMethod("newBuilder").invoke(null) as Message.Builder
        
        val fields = node["fields"] as Map<String, Map<String, Any>>
        
        data.forEach { (key, value) ->
            val fieldInfo = fields[key.toString()]
            if (fieldInfo != null) {
                setField(builder, fieldInfo, value, node)
            }
        }
        
        return builder.build()
    }
}
```

## Phase 2: Roundtrip Testing Infrastructure

### 2.1 Create Comprehensive Test Framework
- [ ] Load both command and state keyword trees
- [ ] Generate test data using proto-explorer specs
- [ ] Implement roundtrip test for each message type
- [ ] Use protobuf equals() for comparison

```kotlin
class ProtobufRoundtripTest {
    @Test
    fun testCommandRoundtrip() {
        val original = generateTestCommand() // From proto-explorer
        
        // Transit → Protobuf
        val protobuf1 = transitHandler.fromRep(original)
        
        // Validate with buf.validate
        val violations1 = validator.validate(protobuf1)
        assertTrue(violations1.isEmpty())
        
        // Protobuf → Binary
        val binary = protobuf1.toByteArray()
        
        // Binary → Protobuf
        val protobuf2 = parseFrom(binary)
        
        // Validate again
        val violations2 = validator.validate(protobuf2)
        assertTrue(violations2.isEmpty())
        
        // Compare Java Protobuf representations using equals()
        if (protobuf1 != protobuf2) {
            // Debug using JSON representation
            val json1 = JsonFormat.printer()
                .includingDefaultValueFields()
                .preservingProtoFieldNames()
                .print(protobuf1)
            val json2 = JsonFormat.printer()
                .includingDefaultValueFields()
                .preservingProtoFieldNames()
                .print(protobuf2)
            
            fail("Protobuf roundtrip failed!\nExpected:\n$json1\nActual:\n$json2")
        }
        
        // Also verify hashCode consistency
        assertEquals(protobuf1.hashCode(), protobuf2.hashCode())
        
        // Protobuf → Transit
        val reconstructed = transitHandler.toRep(protobuf2)
        
        // Compare Transit EDN structures (optional verification)
        assertEquals(original, reconstructed)
    }
}
```

### 2.2 Test All Message Types
- [ ] Iterate through keyword tree to find all message types
- [ ] Generate valid test data for each
- [ ] Run roundtrip test
- [ ] Report any failures with clear diagnostics

## Phase 3: Integration with Existing System

### 3.1 Update CommandSubprocess
- [ ] Replace action-based routing with keyword tree navigation
- [ ] Use reflection-based handler for all commands
- [ ] Remove all manual command builders
- [ ] Add comprehensive error handling

### 3.2 Update StateSubprocess
- [ ] Use state keyword tree for protobuf → Transit conversion
- [ ] Replace manual field extraction with reflection
- [ ] Ensure all state fields are captured
- [ ] Test with real state updates

### 3.3 Performance Optimization
- [ ] Cache reflection lookups (Method objects)
- [ ] Pre-compile setter method references
- [ ] Benchmark against manual approach
- [ ] Optimize hot paths if needed

## Phase 4: Code Generation Options

### 4.1 Static Handler Generation (Optional)
If reflection performance is inadequate:
- [ ] Generate Kotlin source files from keyword trees
- [ ] Create specific handlers for each message type
- [ ] Compile for zero-reflection operation
- [ ] Trade-off: Larger JAR, faster runtime

### 4.2 Hybrid Approach
- [ ] Use reflection for development/testing
- [ ] Generate static handlers for production
- [ ] Same code paths, different implementations
- [ ] Best of both worlds

## Phase 5: Comprehensive Legacy Code Cleanup

### 5.1 Remove Manual Command Builders
- [ ] Delete all individual command builder files in `src/potatoclient/kotlin/command/builders/`
  - [ ] `CVCommandBuilder.kt`
  - [ ] `CompassCommandBuilder.kt`
  - [ ] `DayCameraCommandBuilder.kt`
  - [ ] `HeatCameraCommandBuilder.kt`
  - [ ] `LrfCommandBuilder.kt`
  - [ ] `OSDCommandBuilder.kt`
  - [ ] `RotaryPlatformCommandBuilder.kt`
  - [ ] `SystemCommandBuilder.kt`
- [ ] Delete `ProtobufCommandBuilder.kt` with its giant switch statement
- [ ] Delete action registry in `CommandSubprocess.kt`

### 5.2 Remove Manual Key Conversion Code
- [ ] Delete `src/potatoclient/transit/keyword_handlers.clj`
- [ ] Delete `test/potatoclient/transit/keyword_conversion_test.clj`
- [ ] Remove manual case conversion in Kotlin files:
  - [ ] `MetadataCommandSubprocess.kt` - remove kebab/snake case conversions
  - [ ] `ProtobufTransitHandler.kt` - remove manual field mapping
  - [ ] `ProtobufStateHandlers.kt` - replace with reflection
  - [ ] `StateSubprocess.kt` - use state keyword tree
- [ ] Update `src/potatoclient/java/transit/EnumUtils.java` if needed

### 5.3 Clean Up Outdated Specs
- [ ] Delete manual command specs directory: `src/potatoclient/specs/`
  - [ ] Keep only `transit_messages.clj` (subprocess communication)
  - [ ] Delete `cmd/rotary.clj` and all manual command specs
  - [ ] Delete all 9 files in `specs/data/` directory
- [ ] Clean up `src/potatoclient/specs.clj`:
  - [ ] Remove command payload specs (lines 172-188)
  - [ ] Remove temporary enum definitions (lines 585-633)
  - [ ] Remove command domain schemas (lines 420-546)
  - [ ] Remove protobuf type specs (lines 550-850)
  - [ ] Keep only: themes, locales, UI components, process management
- [ ] Delete placeholder files in shared:
  - [ ] `shared/specs/protobuf/cmd_specs.clj`
  - [ ] `shared/specs/protobuf/state_specs.clj`

### 5.4 Remove Legacy Command System
- [ ] Delete `src/potatoclient/transit/commands.clj` (263 lines of functions)
- [ ] Delete `src/potatoclient/transit/command_sender.clj` (hardcoded registry)
- [ ] Update all UI code to use new command system
- [ ] Remove references in `core.clj` menu actions

### 5.5 Clean Up Test Files
- [ ] Delete all `.skip` test files (9 files):
  - [ ] `test/potatoclient/transit/unified_transit_test.clj.skip`
  - [ ] `test/potatoclient/state/camera_day_test.clj.skip`
  - [ ] `test/potatoclient/state/camera_heat_test.clj.skip`
  - [ ] `test/potatoclient/state/compass_test.clj.skip`
  - [ ] `test/potatoclient/state/gps_test.clj.skip`
  - [ ] `test/potatoclient/state/lrf_test.clj.skip`
  - [ ] `test/potatoclient/state/rotary_test.clj.skip`
  - [ ] `test/potatoclient/state/system_test.clj.skip`
  - [ ] `test/potatoclient/proto/serialization_test.clj.skip`
- [ ] Update or delete legacy handler tests:
  - [ ] `test/potatoclient/protobuf_handler_test.clj`
  - [ ] `test/potatoclient/transit_handler_test.clj`
  - [ ] `test/potatoclient/transit_handlers_working_test.clj`

### 5.6 Update Core Systems
- [ ] Update `src/potatoclient/ipc.clj`:
  - [ ] Remove hardcoded message type handling
  - [ ] Use proto-explorer generated types
  - [ ] Simplify message dispatch
- [ ] Update `src/potatoclient/process.clj`:
  - [ ] Remove references to old command system
  - [ ] Ensure subprocess launching works with new handlers

### 5.7 Documentation Updates
- [ ] Update `CLAUDE.md`:
  - [ ] Remove references to action-based commands
  - [ ] Document reflection-based architecture
  - [ ] Update examples to use new command format
- [ ] Update `.claude/kotlin-subprocess.md`:
  - [ ] Document keyword tree usage
  - [ ] Remove manual builder documentation
  - [ ] Add reflection handler examples
- [ ] Create new guide: `docs/ADDING_COMMANDS.md`:
  - [ ] Show how to add new proto commands
  - [ ] Explain keyword tree regeneration
  - [ ] Document testing approach

### 5.8 Final Validation
- [ ] Run `make clean` and rebuild everything
- [ ] Run full test suite: `make test`
- [ ] Manually test each command type from UI
- [ ] Verify no references to old code remain:
  ```bash
  # Search for old patterns
  grep -r "ProtobufCommandBuilder" src/
  grep -r "action-registry" src/
  grep -r "kebab->snake" src/
  grep -r "snake->kebab" src/
  ```
- [ ] Performance comparison report
- [ ] Create migration guide for any external code

## Testing Strategy

### Unit Tests
- [ ] Test each field type conversion
- [ ] Test nested message handling
- [ ] Test repeated field handling
- [ ] Test oneof handling
- [ ] Test enum conversions

### Integration Tests
- [ ] Full command roundtrips
- [ ] Full state roundtrips
- [ ] Error scenarios
- [ ] Performance benchmarks

### System Tests
- [ ] End-to-end command flow
- [ ] UI → Subprocess → WebSocket
- [ ] State updates → UI
- [ ] Concurrent command handling

## Success Criteria

1. **Zero Manual Mapping**: All conversions use keyword trees
2. **100% Roundtrip Success**: Every message type passes roundtrip test
3. **Validation Coverage**: All buf.validate constraints enforced
4. **Performance**: < 1ms overhead vs manual approach
5. **Maintainability**: Adding new commands requires zero code changes

## Benefits Over Previous Approach

1. **Native Protobuf Comparison**: Use Java's `equals()` and `hashCode()` methods directly
2. **Built-in Debugging**: Protobuf's `JsonFormat` provides detailed JSON for failed comparisons
3. **No JSON Canonicalization**: Compare Java objects, not JSON strings
4. **No Manual Builders**: Everything driven by keyword trees
5. **Automatic Updates**: Regenerate trees when protos change
6. **Type Safety**: Compiler catches type mismatches
7. **Comprehensive Testing**: Every field of every message tested

## Implementation Order

1. **Week 1**: Implement reflection-based handlers
2. **Week 1-2**: Create roundtrip testing framework
3. **Week 2**: Integrate with subprocesses
4. **Week 2-3**: Performance optimization
5. **Week 3**: Cleanup and documentation

This approach leverages the keyword trees to create a truly automated, maintainable system that eliminates all manual protobuf handling code.

## Summary of Major Changes

### What We're Removing (Legacy):
1. **Manual Command Builders**: 8+ Kotlin files with hardcoded field mappings
2. **Action Registry**: String-based command routing system
3. **Manual Specs**: 10+ manually maintained Clojure spec files
4. **Key Conversion**: Manual kebab-case ↔ snake_case converters
5. **Legacy Tests**: 9 skipped test files from old approaches
6. **Command Functions**: 263 lines of individual command functions

### What We're Adding (New):
1. **Keyword Trees**: Auto-generated EDN → Java class mappings
2. **Reflection Handlers**: Universal Transit ↔ Protobuf converters
3. **Roundtrip Testing**: Comprehensive validation of every message type
4. **Proto-Explorer Integration**: Leverage existing specs and generators
5. **Native Comparison**: Use protobuf's equals() instead of JSON

### Development Workflow:
1. **Proto Changes**: When protos change, regenerate keyword trees with `bb`
2. **Finding Commands**: Use `bb find` to discover command names and structure
3. **Testing**: Use `bb example` to generate valid test data
4. **Validation**: buf.validate constraints enforced automatically
5. **Zero Code Changes**: New commands work without touching Kotlin/Clojure code

This represents a complete paradigm shift from manual, error-prone mapping to automated, reflection-based conversion that stays in sync with protobuf definitions.