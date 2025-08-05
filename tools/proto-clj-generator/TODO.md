# Proto-CLJ-Generator TODO List

## Clean Implementation Principles
- No backward compatibility needed - we're building from scratch
- Clean, robust implementation without versioning
- Remove all old/legacy code paths
- Focus on maintainability and clarity

## Completed Tasks ✅

### 1. Namespace Organization and Code Structure
- [x] **Problem**: Generated code had many duplicate function names (e.g., 13 instances of "build-root" in single file)
- [x] **Solution**: Implemented namespace separation matching protobuf package structure
  - Created `frontend-namespaced.clj` for namespace-based generation
  - Maps protobuf packages to Clojure namespaces (e.g., `cmd.Compass` → `potatoclient.proto.cmd.compass`)
  - Each package now gets its own file with no duplicate function names
  - Added `:gen-ns` alias to generate with namespace separation

### 2. Comprehensive End-to-End Testing
- [x] Created `full_roundtrip_validation_test.clj` with complete validation pipeline
- [x] Tests include:
  - Malli generation of 300+ samples for both command and state messages
  - Full roundtrip: EDN → Java → buf.validate → binary → Java → EDN
  - Validation at each stage
  - EDN comparison using diff utility

### 3. Negative and Sanity Testing
- [x] Implemented negative tests for:
  - Invalid EDN structure
  - Invalid enum values
  - Type mismatches
  - Malformed binary data
- [x] Implemented sanity tests for:
  - Empty messages
  - Messages with only required fields
  - Integer boundary values
  - Performance benchmarks

### 4. Code Cleanup
- [x] Removed old/versioned files:
  - `frontend_old.clj`
  - `frontend_string.clj`
  - `enhanced_gen.clj`
  - `working_gen.clj`
  - `frontend_proper.clj`

## Remaining Tasks

## Medium Priority Tasks

### 4. Code Quality Improvements
- [x] ~~Template-based oneof handling~~ (Already implemented in frontend.clj)
- [ ] Implement `resolve-builder-name` function (edn_test.clj:156)
- [ ] Enable `builder-name-resolution-test` (edn_test.clj:157)

## Implementation Plan

### Phase 1: Analysis and Design
1. Study protobuf package structure from JSON descriptors
2. Design namespace mapping strategy
3. Create test data generation utilities

### Phase 2: Namespace Refactoring
1. Update backend to preserve package information
2. Modify frontend to generate separate namespace files
3. Update imports and cross-references
4. Test with existing tests

### Phase 3: Comprehensive Testing
1. Create test data generators using Malli
2. Implement full round-trip test pipeline
3. Add negative test cases
4. Add sanity test cases
5. Ensure all tests pass

### Phase 4: Oneof Templates
1. Complete oneof template implementation
2. Refactor existing inline oneof handling
3. Test with complex oneof scenarios

## Notes

- The current implementation puts all generated code in single files (command.clj, state.clj)
- Protobuf namespaces (packages) should map to Clojure namespaces for better organization
- buf.validate provides built-in validation we should leverage
- Use existing diff functionality for EDN comparison
- Consider performance implications of namespace separation