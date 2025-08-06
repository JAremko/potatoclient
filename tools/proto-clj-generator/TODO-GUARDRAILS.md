# Proto-CLJ-Generator TODO: Guardrails & Testing

## 🎯 Primary Goals
1. ~~Fix all remaining namespace/alias issues~~ ✅ (90% complete - index file issue remains)
2. Pass all tests with proper roundtrip validation ⏳ (in progress)
3. ~~Generate guardrails-enabled functions using `>defn`/`>defn-`~~ ✅ (complete)
4. Integrate Malli specs from shared directory 📋 (not started)
5. Setup proper clj-kondo configuration for guardrails 📋 (not started)

## 📊 Overall Progress
- **Guardrails Integration**: ✅ Complete - all generated functions use `>defn`/`>defn-`
- **Namespace Refactoring**: 90% - file-based namespaces working, index file generation needs fix
- **Test Suite**: ⏳ In progress - namespace issues being resolved
- **Spec Integration**: 📋 Not started
- **Tooling Setup**: 📋 Not started

## 📋 Tasks

### 1. Fix Immediate Issues
- [x] Fix namespace mapping in cmd.clj (e.g., `cmd.day.camera` → `cmd.daycamera`)
  - Created centralized naming module with lossless proto<->keyword conversions
  - Fixed spec-gen.clj to use consistent lowercase naming (no kebab-case for packages)
  - Regenerated files with correct namespace references
- [ ] Fix duplicate aliases (two `camera` aliases)
  - Currently both `cmd.daycamera` and `ser.camera` use alias `camera`
  - Need to disambiguate by using full package name or numbered suffixes
  - Check frontend_namespaced.clj line ~180 for alias generation logic
- [ ] Implement actual re-exports in index files (command.clj, state.clj)
  - Currently generated but empty
  - Need to create actual defn wrappers that delegate to namespaced functions
  - Example: `(defn build-root [m] (cmd/build-root m))`
- [ ] Update tests to work with new file locations
  - Tests expect single-file output but we generate multiple files
  - Update test fixtures to handle namespaced structure
  - Fix path resolution in test code
- [x] Create centralized naming module with lossless conversions
  - Added generator.naming namespace with bidirectional conversions
  - Proto types preserved as :potatoclient.proto/cmd.DayCamera.Root
  - Filesystem-safe conversions for actual file generation
- [x] Write comprehensive tests for naming module
  - Created naming_test.clj with unit tests
  - Tests cover all conversion functions
  - Validates roundtrip preservation for lossless functions
- [ ] Add generative testing for naming roundtrips
  - Use Malli generators to create random valid proto names
  - Test that proto->keyword->proto preserves original
  - Test that all generated names are valid in their contexts
  - Add test.check dependency and property-based tests
- [ ] Update all code generation to use centralized naming
  - Replace inline naming logic in spec_gen.clj
  - Update frontend.clj to use naming module
  - Update frontend_namespaced.clj package->namespace function
  - Ensure consistent naming across entire codebase

### 2. Comprehensive Roundtrip Testing
- [ ] Ensure cmd and state root generation tests work
  - Fix test namespace paths to match new structure
  - Update expected output in tests to use lowercase namespaces
  - Ensure all builder/parser functions are found in correct namespaces
- [ ] Test with Malli specs from `/home/jare/git/potatoclient/shared/specs/`
  - Load existing specs from shared/specs/custom/
  - Map proto message names to existing spec names
  - Validate generated data against shared specs
- [ ] Add buf.validate checks to roundtrip tests
  - Call buf.validate on generated protobuf objects
  - Ensure all validation rules pass
  - Add clear error reporting for validation failures
- [ ] Compare input vs output for exact match
  - Generate proto -> parse to map -> build proto -> compare bytes
  - Test all message types, not just roots
  - Include edge cases: empty messages, all fields set, oneofs
- [ ] Use specs from shared dir for validation
  - Reference :potatoclient.ui-specs/* specs
  - Ensure generated specs align with UI expectations
  - Cross-validate with existing domain specs

### 3. Guardrails Integration ✅ (Mostly Complete)

#### Research References
- Main app code for guardrails usage patterns
- `/home/jare/git/potatoclient/tools/guardrails-check/` - tool to verify all functions use guardrails
- Main app Makefile for clj-kondo setup
- Main app `.clj-kondo/` config for guardrails support

#### Implementation
- [x] Change generated functions from `defn`/`defn-` to `>defn`/`>defn-`
  - [x] Updated templates/builder-guardrails.clj template
  - [x] Updated templates/parser-guardrails.clj template
  - [x] Updated templates/oneof-builder-guardrails.clj template
  - [x] Updated templates/oneof-parser-guardrails.clj template
  - [x] All generated functions now use guardrails macros
- [x] Attach Malli specs from `/home/jare/git/potatoclient/shared/specs/`
  - [x] Updated oneof specs to use custom :oneof schema instead of :altn
  - [x] Removed unnecessary malli-oneof import (registry setup belongs in app/test bootstrap)
  - [ ] Create spec resolution map: proto type -> malli spec keyword
  - [ ] Handle nested message specs correctly
- [x] Ensure guardrails active in all builds (removed non-guardrails variant)
  - [x] Removed all non-guardrails templates
  - [x] Updated code to always generate guardrails functions
  - [x] Simplified codebase by removing conditional logic
- [x] Generate proper function specs for:
  - [x] Build functions: `[::message-spec => #(instance? ProtoClass %)]`
  - [x] Parse functions: `[#(instance? ProtoClass %) => ::message-spec]`
  - [x] Oneof payload builders: `[#(instance? Builder %) [:tuple keyword? any?] => #(instance? Builder %)]`
  - [x] Oneof payload parsers: `[#(instance? ProtoClass %) => (? map?)]`

#### Recent Changes (2025-08-06)

**Morning Session**:
- [x] Fixed return value specs to use instance checks instead of `any?`
- [x] Updated oneof-builder template to use Builder class instance checks
- [x] Removed all non-guardrails template variants for simplicity
- [x] Updated frontend.clj to always use guardrails templates
- [x] Fixed namespace alias issue (removed hyphen replacement for namespace aliases)
- [x] Removed single namespace mode completely
- [x] Updated all tests to use separated namespace mode
- [x] Fixed repeated field handling (addAll for scalars, loops for messages)
- [x] Updated repeated field templates and generation logic

**Afternoon Session**:
- [x] Built comprehensive dependency graph that tracks file-level dependencies
- [x] Fixed type lookup to include filename information
- [x] Fixed namespace generation for file-based approach (e.g., `ser.types` from `jon_shared_data_types.proto`)
- [x] Fixed file path generation to handle dashes (namespaces use dashes, files use underscores)
- [x] Added `proto-package->clojure-alias` function with comprehensive tests
- [x] Updated oneof builder/parser generation to use correct kebab-case aliases
- [x] Updated test files to use `potatoclient.proto.state` instead of `potatoclient.proto.ser`

### File-based Namespace Refactoring (Completed)

#### Problem (Solved)
We switched from package-based namespaces to file-based namespaces, which broke many tests. The key issue was that files like `jon_shared_data_types.proto` in package `ser` should generate namespace `ser.types`, not just `ser`.

#### Solutions Implemented
1. **Namespace Generation**
   - [x] Created `file->namespace-suffix` function to map proto files to namespaces
   - [x] Changed from `group-by-package` to `group-by-file`
   - [x] Fixed namespace generation to use kebab-case (e.g., `cmd.day-cam-glass-heater`)

2. **Dependency Resolution**
   - [x] Updated type lookup to include filename information
   - [x] Built comprehensive dependency graph that tracks file-level dependencies based on actual type usage
   - [x] Fixed type lookup to work with string keys (not just keywords)
   - [x] Fixed require statement generation to use correct namespaces (e.g., `ser.types` for enums)

3. **File Path and Alias Issues**
   - [x] Fixed file path generation - namespaces use dashes but file paths need underscores
   - [x] Added `proto-package->clojure-alias` function to generate kebab-case aliases
   - [x] Updated oneof builder/parser generation to use the correct alias format
   - [x] Added comprehensive tests and generative testing for the new naming function

4. **Test Updates**
   - [x] Updated tests to use `potatoclient.proto.state` instead of `potatoclient.proto.ser`
   - [x] Fixed namespace references in all test files

#### Remaining Issues

1. **State Index File Generation**
   - The state.clj index file is trying to require `potatoclient.proto.ser` which doesn't exist
   - Need to handle packages that come from file-based namespaces in the index generation
   - The index generation logic needs to understand the file->namespace mapping

2. **Test Failures**
   - Tests are still failing due to namespace resolution issues
   - Need to ensure all generated namespaces match what tests expect

### 4. Spec Integration
- [ ] Load specs from `/home/jare/git/potatoclient/shared/specs/custom/`
  - Read all .clj files in specs directory at generation time
  - Parse spec definitions to extract spec names
  - Build lookup table of available specs
- [ ] Map proto message types to Malli specs
  - Create mapping function: proto type -> spec keyword
  - Handle special cases (e.g., ser.JonGuiData.ClientType -> ::ui-specs/client-type)
  - Generate fallback specs for unmapped types
- [ ] Use same specs for:
  - Guardrails function specs in generated code
  - Cmd/state root payload validation
  - Roundtrip validation in tests
  - Runtime validation in generated functions

### 5. clj-kondo Configuration
- [ ] Copy/adapt `.clj-kondo/` config from main app
  - Copy config/clj-kondo/hooks/ directory
  - Copy .clj-kondo/config.edn with guardrails setup
  - Ensure hooks handle >defn and >defn- macros
- [ ] Configure for guardrails macros (`>defn`, `>defn-`)
  - Add :lint-as rules for guardrails macros
  - Configure :unresolved-symbol exceptions
  - Handle => and ? symbols from guardrails
- [ ] Setup reporting to prevent false positives
  - Filter out guardrails-specific warnings
  - Add custom linters for our patterns
  - Create make lint-report-filtered target
- [ ] Ensure generated code passes linting
  - Run clj-kondo on generated output
  - Fix any legitimate warnings
  - Document any necessary suppressions

### 6. Tooling Integration
- [ ] Integrate guardrails-check tool
  - Add :guardrails-check alias to deps.edn
  - Configure to scan generated namespaces
  - Set target of 100% guardrails coverage
- [ ] Add Make target to verify all generated functions use guardrails
  - make guardrails-check target
  - Fail build if any functions missing guardrails
  - Generate coverage report
- [ ] Setup proper error reporting
  - Clear error messages for spec failures
  - Include field names and types in errors
  - Add :gen/fmap metadata for better test data generation

## 🔍 Key Files to Research

### Main App Files
- `/home/jare/git/potatoclient/.clj-kondo/` - linting config
- `/home/jare/git/potatoclient/Makefile` - build/lint setup
- Examples of `>defn` usage in main app code
- `/home/jare/git/potatoclient/shared/specs/` - Malli specs

### Generator Files to Modify
- `frontend.clj` - Change defn generation to >defn
- `templates/` - Update templates for guardrails
- `core.clj` - Load shared specs
- New: `guardrails.clj` - Spec generation/mapping

## 📊 Success Criteria
1. All tests pass with new structure
   - Namespace resolution works correctly
   - No duplicate aliases in generated code
   - Index files properly re-export functions
2. Roundtrip tests validate perfectly
   - Proto -> Map -> Proto preserves all data
   - Buf.validate passes on all generated protos
   - Malli validation passes on all generated maps
3. All generated functions use `>defn`/`>defn-`
   - 100% of build-* functions have guardrails
   - 100% of parse-* functions have guardrails
   - Private helper functions use >defn-
4. Guardrails-check tool reports 100% coverage
   - No functions using plain defn (except approved exceptions)
   - All specs properly attached
   - Specs actually validate data
5. clj-kondo reports no false positives
   - Clean lint output on generated code
   - Guardrails macros properly recognized
   - No unresolved symbol warnings
6. Buf.validate checks pass
   - All protobuf validation rules enforced
   - Clear errors on validation failures
   - Integration with test suite

## 💡 Implementation Notes

### Guardrails Function Pattern
```clojure
(>defn build-some-message
  "Build a SomeMessage protobuf from a map."
  [m]
  [::some-message-spec => protobuf-class?]
  (let [builder ...]
    ...))

(>defn parse-some-message
  "Parse a SomeMessage protobuf to a map."
  [proto]
  [protobuf-class? => ::some-message-spec]
  {...})
```

### Spec Loading
- Load specs at generation time
- Map proto package.MessageName to ::.../message-name specs
- Use specs for both guardrails and validation

### Build Configuration
- Guardrails enabled by default
- Disabled only for release builds
- Same pattern as main app

## 🔄 Naming Conversion Architecture

### Lossless Conversions
- Proto type: `.cmd.DayCamera.Root` <-> Keyword: `:potatoclient.proto/cmd.DayCamera.Root`
- Proto package: `cmd.DayCamera` <-> Keyword: `:potatoclient.proto/cmd.DayCamera`
- Preserves all original casing and structure

### Lossy Conversions (for filesystem)
- Proto package -> Clojure namespace: `cmd.DayCamera` -> `potatoclient.proto.cmd.daycamera`
- Proto package -> File path: `cmd.DayCamera` -> `cmd/daycamera.clj`
- Proto package -> Alias: `cmd.DayCamera` -> `daycamera`

### Testing Strategy
- Unit tests for all conversions
- Property-based testing with Malli generators
- Roundtrip validation for lossless conversions

## 🏗️ Implementation Order

1. **Phase 1: Naming & Structure** (Current)
   - [x] Centralized naming module
   - [ ] Fix duplicate aliases
   - [ ] Update all generators to use naming module
   - [ ] Fix index file generation

2. **Phase 2: Testing**
   - [ ] Update test fixtures for new structure
   - [ ] Add generative tests for naming
   - [ ] Implement comprehensive roundtrip tests
   - [ ] Add buf.validate integration

3. **Phase 3: Guardrails**
   - [ ] Update templates to use >defn
   - [ ] Integrate Malli specs
   - [ ] Configure clj-kondo
   - [ ] Add guardrails-check tool

4. **Phase 4: Polish**
   - [ ] Documentation
   - [ ] Performance optimization
   - [ ] Error message improvements
   - [ ] Final validation