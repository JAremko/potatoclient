# TODO: Kotlin Command Integration - Static Code Generation Approach

## Current Status: Phase 1 - Implementing Static Kotlin Handlers

We've decided to use **static code generation** instead of reflection for better performance and type safety. The approach:
1. Generate keyword trees from protobuf definitions ✅ COMPLETED
2. Generate static Kotlin Transit handlers from keyword trees 🚧 IN PROGRESS
3. Replace the entire action-based command system with direct protobuf mapping
4. Remove all manual builders and handlers

## Completed Work

### Phase 0: Keyword Tree Generation ✅
- Created `bb generate-keyword-tree-cmd` and `bb generate-keyword-tree-state` commands
- Generated comprehensive keyword trees with all protobuf metadata:
  - `shared/specs/protobuf/proto_keyword_tree_cmd.clj` (15 root commands, 198 total nodes)
  - `shared/specs/protobuf/proto_keyword_tree_state.clj` (13 root state types, 17 total nodes)
- Trees include Java class names, field info, setter methods, and type information

### Phase 1: Static Handler Generation 🚧 IN PROGRESS
- Created `tools/proto-explorer/generate-kotlin-handlers.clj` generator
- Generator creates:
  - `GeneratedCommandHandlers.kt` - Handles all command building and extraction
  - `GeneratedStateHandlers.kt` - Handles all state extraction
- Current issues to fix:
  - [ ] Import ordering (wildcard imports causing linter warnings)
  - [ ] Protobuf structure - Root has direct setters (setCv, setOsd) not nested cmd field
  - [ ] Missing imports for some packages (mu.KotlinLogging)
  - [ ] Incorrect getter method names in state handlers

## Current Task: Fix Code Generation Issues

### Issue 1: Protobuf Command Structure
The current generator assumes a nested structure but protobuf uses direct setters:
```kotlin
// WRONG (what generator currently produces):
builder.cmd = JonSharedCmd.Cmd.newBuilder().setCv(buildCv(data)).build()

// CORRECT (how protobuf actually works):
builder.setCv(buildCv(data))
```

### Issue 2: Import Ordering
Need to fix import order to satisfy Kotlin linter:
- Regular imports first
- Wildcard imports last
- Alphabetical ordering within groups

### Issue 3: Method Name Generation
Some getter methods are incorrectly generated:
```kotlin
// WRONG:
msg.getCur_video_rec_dir_month()

// CORRECT:
msg.getCurVideoRecDirMonth()
```

## Next Steps

### Immediate Tasks
1. Fix the generator to produce correct protobuf structure
2. Fix import ordering in generated files
3. Fix getter/setter method name generation
4. Ensure all required imports are included

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

## Key Benefits of Static Generation Approach

1. **Performance**: No reflection overhead, direct method calls
2. **Type Safety**: Compile-time checking of all protobuf access
3. **Maintainability**: Regenerate when protos change, no manual updates
4. **Simplicity**: Generated code is straightforward and debuggable
5. **Size**: Smaller than reflection-based approach (no need to ship keyword trees)

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

- [ ] All 15 command types generate correctly
- [ ] All 13 state types generate correctly  
- [ ] Generated code compiles without errors
- [ ] Generated code passes linting
- [ ] Roundtrip tests pass for all message types
- [ ] Performance better than reflection approach
- [ ] Zero manual code for new commands