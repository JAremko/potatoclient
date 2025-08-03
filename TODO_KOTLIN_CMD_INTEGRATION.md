# TODO: Kotlin Command Integration - Static Code Generation Approach

## Current Status: Phase 1 - Static Kotlin Handlers Implementation ✅ COMPLETED

We've successfully implemented **static code generation** for Transit handlers. The approach:
1. Generate keyword trees from protobuf definitions ✅ COMPLETED
2. Generate static Kotlin Transit handlers from keyword trees ✅ COMPLETED
3. Replace the entire action-based command system with direct protobuf mapping 🚧 NEXT PHASE
4. Remove all manual builders and handlers ⏳ FUTURE

## Completed Work

### Phase 0: Keyword Tree Generation ✅
- Created `bb generate-keyword-tree-cmd` and `bb generate-keyword-tree-state` commands
- Generated comprehensive keyword trees with all protobuf metadata:
  - `shared/specs/protobuf/proto_keyword_tree_cmd.clj` (15 root commands, 198 total nodes)
  - `shared/specs/protobuf/proto_keyword_tree_state.clj` (13 root state types, 17 total nodes)
- Trees include Java class names, field info, setter methods, and type information

### Phase 1: Static Handler Generation ✅ COMPLETED
- Created `tools/proto-explorer/generate-kotlin-handlers.clj` generator
- Generator creates:
  - `GeneratedCommandHandlers.kt` - Handles all command building and extraction
  - `GeneratedStateHandlers.kt` - Handles all state extraction
- All issues resolved:
  - ✅ Import ordering - proper Kotlin imports with specific classes
  - ✅ Protobuf structure - uses direct setters on Root (setCv, setOsd, etc.)
  - ✅ Logging - uses LoggingUtils instead of external library
  - ✅ Getter method names - properly handles snake_case to camelCase conversion
  - ✅ Class references - uses Kotlin inner class syntax (JonSharedCmdOsd.Root)

## Completed Improvements

### ✅ Fixed Major Issues:
1. **Function Name Conflicts** - All functions now properly namespaced with full path (e.g., `buildGpsStart()`, `buildDayCameraZoomPrevZoomTablePos()`)
2. **Enum Type Conversion** - Added proper enum handling with error handling
3. **Import Ordering** - Proper Kotlin imports with specific classes
4. **Logging** - Uses project's LoggingUtils instead of external library
5. **Class References** - Proper Kotlin inner class syntax (no backticks)
6. **Getter/Setter Names** - Handles snake_case to camelCase conversion
7. **Deprecated Methods** - Changed `toLowerCase()` to `lowercase()`
8. **WriteHandler Interface** - Fixed to use proper 2-parameter generic type

### Current Issues Being Fixed:
1. **CamelCase Field Names** - Some protobuf fields with camelCase (like `fogModeEnabled`) need special handling for getter/setter generation
2. **WriteHandler Implementation** - Need to implement additional interface methods (`stringRep`, `getVerboseHandler`)
3. **Manual Builders** - Old builders in `src/potatoclient/kotlin/transit/builders.old/` still causing compilation errors

### Remaining Minor Issues:
1. **Enum Type-Ref** - Some enum fields lack type information in keyword tree
2. **Testing** - Need comprehensive roundtrip tests to verify correctness

## Next Steps

### Immediate Tasks
1. ✅ Fix function name conflicts by namespacing (e.g., `buildGpsStart()`)
2. ✅ Add enum type conversion with proper error handling
3. ⏳ Test the generated handlers work correctly with roundtrip tests
4. ⏳ Remove manual builders once verified

### Phase 2: Integration (After Handler Generation Works)
- Update `CommandSubprocess` to use `GeneratedCommandHandlers`
- Update `StateSubprocess` to use `GeneratedStateHandlers`
- Remove dependency on action-based routing

### Phase 3: Cleanup (After Integration Works)
- Delete all manual command builders (11 files in `src/potatoclient/kotlin/transit/builders/`)
- Delete `ProtobufCommandBuilder.kt` with its action registry
- Delete manual Transit handlers
- Delete manual Clojure command functions
- Update all UI code to send commands in new format

## Architecture Clarification

### What the Keyword Trees Are For
The keyword trees serve **two distinct purposes**:

1. **Runtime use by Clojure**: The Clojure code uses the trees to know what commands exist and their structure when building Transit messages to send to Kotlin
2. **Build-time code generation**: We use the same trees to generate static Kotlin handlers that convert between Transit maps and protobuf objects

### How Common Sub-Messages Are Handled
The architecture elegantly handles common command names (like `:start`, `:stop`, `:halt`) that appear in multiple contexts:

**From Clojure's perspective**: Just nested maps with keywords
```clojure
{:gps {:start {}}}        ; GPS start command
{:lrf {:start {}}}        ; LRF start command  
{:rotary {:start {}}}     ; Rotary start command
```

**From Kotlin's perspective**: Each gets its own builder function
- `:gps {:start {}}` → `buildGps()` → `buildGpsStart()` → `JonSharedCmdGps.Start`
- `:lrf {:start {}}` → `buildLrf()` → `buildLrfStart()` → `JonSharedCmdLrf.Start`
- `:rotary {:start {}}` → `buildRotary()` → `buildRotaryStart()` → `JonSharedCmdRotary.Start`

The parent context naturally disambiguates which protobuf class to create, with zero configuration needed.

### Static Code Generation Approach
- **At build time**: Generate Kotlin code from keyword trees that knows how to convert Transit ↔ Protobuf
- **At runtime**: Clojure sends clean Transit messages like `{:cv {:start-track-ndc {:channel "heat" :x 0.5}}}`
- **No runtime metadata**: The Transit messages don't include Java class information
- **No reflection**: Everything is statically typed and compile-time checked

### Key Benefits
1. **Performance**: No reflection overhead, direct method calls
2. **Type Safety**: Compile-time checking of all protobuf access
3. **Maintainability**: Regenerate when protos change, no manual updates
4. **Simplicity**: Generated code is straightforward and debuggable
5. **Clean separation**: Transit messages remain pure data, no Java class pollution
6. **Natural disambiguation**: Parent context determines which protobuf class for common commands

## Command Format Change

### Old Format (Action-Based):
```clojure
{:action "cv-start-track-ndc"
 :params {:channel "heat" :x 0.5 :y 0.5}}
```

### New Format (Direct Protobuf Mapping):
```clojure
{:cv {:start-track-ndc {:channel "heat" :x 0.5 :y 0.5}}}
```

This mirrors the protobuf structure exactly, making the system more intuitive and eliminating the need for action string mapping.

## Generator Usage

```bash
# In proto-explorer directory
cd tools/proto-explorer

# Regenerate keyword trees (after proto changes)
bb generate-keyword-tree-cmd
bb generate-keyword-tree-state

# Generate Kotlin handlers
bb generate-kotlin-handlers.clj

# Format generated code
cd ../.. && make fmt-kotlin
```

## Testing Strategy

Once generation is working:
1. Create roundtrip tests for every command type
2. Verify Transit → Protobuf → Binary → Protobuf → Transit
3. Use protobuf's built-in equals() for comparison
4. Ensure all buf.validate constraints are respected

## Success Metrics

- [x] All 15 command types generate correctly
- [x] All 13 state types generate correctly  
- [ ] Generated code compiles without errors (in progress - fixing camelCase issues)
- [x] Generated code passes linting (after formatting)
- [ ] Roundtrip tests pass for all message types
- [ ] Performance better than reflection approach (expected due to static code)
- [x] Zero manual code for new commands (achieved with generation)