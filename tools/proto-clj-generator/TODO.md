# Proto-CLJ-Generator TODO List and Goals

## 🎯 Primary Goal

Create a rock-solid protobuf-to-Clojure code generator that:
1. **Handles ALL valid protobuf schemas** - no hardcoded workarounds
2. **Validates data at every stage** - using Malli specs and Guardrails ✅
3. **Resolves cross-namespace dependencies** - with multi-pass IR enrichment ✅
4. **Generates idiomatic Clojure code** - with runtime validation
5. **Is thoroughly tested** - with property-based and roundtrip tests 🚧

## 🏗️ Architecture Goals

### Multi-Pass IR Generation ✅
Our system uses a sophisticated pipeline:
```
JSON Descriptors → Basic IR → Enriched IR → Generated Code
                     ↓           ↓              ↓
                  Validated   Validated     Validated
```

Each transformation is:
- **Validated** with Malli specs ✅
- **Instrumented** with Guardrails ✅
- **Tested** comprehensively 🚧
- **Pure** and deterministic ✅

### Key Implementation Patterns
1. **Stuart Sierra's dependency library** - Robust graph algorithms with circular detection
2. **Specter** - Powerful nested data transformations (ALL-FIELDS-PATH)
3. **core.match** - Clean pattern matching for type handling
4. **Malli + Guardrails** - Runtime validation with detailed specs

### Key Components
1. **Backend** - Parses JSON descriptors into basic IR ✅
2. **Deps** - Enriches IR with dependency resolution ✅
3. **Specs** - Validates all data structures ✅
4. **Frontend** - Generates code from enriched IR 🚧

## 📋 Immediate Tasks (High Priority)

### 1. Comprehensive Testing of Enriched IR
**Goal**: Ensure the multi-pass IR system is bulletproof

**Tasks**:
- [x] Create test suite for `generator.deps` module
  - [x] Test dependency graph building
  - [x] Test topological sort with various graph shapes
  - [x] Test circular dependency detection
  - [x] Test symbol collection from nested types
  - [x] Test type enrichment with cross-namespace refs
  - [x] Test full pipeline with real descriptors

- [x] Add property-based tests
  - [x] Generate random dependency graphs
  - [x] Generate random type hierarchies
  - [x] Test invariants (e.g., topological sort properties)
  - [x] Test roundtrip: IR → enriched IR → code → parsed data

### 2. Guardrails Instrumentation ✅
**Goal**: Every function validates its inputs and outputs

**Tasks**:
- [x] Update `generator.deps` functions to use `>defn`/`>defn-`
  - [x] `extract-file-dependencies` with file spec
  - [x] `build-dependency-graph` with descriptor spec
  - [x] `topological-sort` with graph spec
  - [x] `collect-file-symbols` with file spec
  - [x] `build-symbol-registry` with full validation
  - [x] `enrich-field` with field specs (private)
  - [x] `enrich-file` with file specs
  - [x] `enrich-descriptor-set`

- [x] Add runtime instrumentation controls
  - [x] Enable/disable validation per namespace (via Guardrails)
  - [x] Clear error messages with context (Malli provides)

### 3. Update Spec Generation
**Goal**: Use enriched IR metadata for better code generation

**Tasks**:
- [ ] Modify `spec_gen.clj` to use enriched type references
  - [ ] Check `:cross-namespace` flag on type refs
  - [ ] Use `:target-package` for proper aliasing
  - [ ] Generate namespace aliases from enriched data
  - [ ] Handle resolved type information

- [ ] Update `frontend_namespaced.clj`
  - [ ] Process files in dependency order
  - [ ] Use enriched requires from IR
  - [ ] Generate proper imports based on usage

## 🧪 Testing Strategy

### 1. Unit Tests
Every public function must have:
- [x] Basic functionality tests (deps module)
- [x] Edge case tests (deps module)
- [x] Error condition tests (deps module)

### 2. Integration Tests
Test the full pipeline:
- [ ] Load real JSON descriptors
- [ ] Run through all IR passes
- [ ] Generate code
- [ ] Compile generated code
- [ ] Run roundtrip tests

### 3. Property-Based Tests
Use Malli generators:
```clojure
(defspec enriched-ir-preserves-structure
  100
  (prop/for-all [descriptor (gen/generate specs/DescriptorSet)]
    (let [enriched (deps/enrich-descriptor-set descriptor)]
      (and (specs/valid? specs/EnrichedDescriptorSet enriched)
           (= (count (:files descriptor))
              (count (:files enriched)))))))
```

### 4. Roundtrip Validation Tests
The ultimate test:
```clojure
(deftest full-roundtrip-with-enriched-ir
  (testing "Data survives proto→map→proto with enriched IR"
    (let [descriptor (load-real-descriptor)
          enriched (deps/enrich-descriptor-set descriptor)
          code (generate-from-enriched enriched)
          ;; Compile and load generated code
          _ (compile-and-load code)
          ;; Generate test data
          test-data (generate-valid-data)
          ;; Full roundtrip
          proto (build-proto test-data)
          parsed (parse-proto proto)
          round2 (build-proto parsed)]
      (is (= test-data parsed))
      (is (proto-equal? proto round2)))))
```

## 🎯 Success Criteria

### For Enriched IR System
1. **All type references resolved** - No unresolved types in generated code ✅
2. **Cross-namespace deps work** - ser enums usable in cmd namespaces ✅
3. **Circular deps detected** - Clear error messages ✅
4. **Performance acceptable** - <1s for typical schemas 🚧

### For Generated Code
1. **Compiles without warnings** - Clean clj-kondo output
2. **Passes all validations** - Malli specs and Guardrails
3. **Roundtrips perfectly** - No data loss or corruption
4. **Fast enough** - <10ms per message typical

### For Testing
1. **100% function coverage** - Every function tested
2. **100% branch coverage** - Every code path tested
3. **Property tests pass** - 1000+ iterations
4. **No flaky tests** - Deterministic results

## 📊 Metrics to Track

### Code Quality
- Guardrails coverage (current: ~90% in deps.clj, target: 100%)
- Malli spec coverage (current: 100% for data structures, target: 100%)
- Test coverage (current: ~80% in deps module, target: 100%)
- Cyclomatic complexity (target: <5)

### Performance
- IR enrichment time
- Code generation time
- Generated code performance
- Memory usage

### Reliability
- Test success rate
- Property test iterations
- Roundtrip success rate
- Error clarity score

## 🚀 Future Enhancements

### Phase 1: Current Focus
- [x] Multi-pass IR with dependency resolution
- [x] Full Guardrails instrumentation for deps module
- [x] Comprehensive unit tests for dependency resolution
- [ ] Complete test coverage across all modules
- [ ] Documentation updates

## 📝 Design Principles

### 1. Correctness First
- Validate everything
- Fail fast with clear errors
- No silent failures
- Test thoroughly

### 2. General Solutions
- No special cases
- Work for any schema
- Document the why
- Keep it simple

### 3. Performance Second
- Measure first
- Optimize hotspots
- Cache when beneficial
- Profile regularly

### 4. Developer Experience
- Clear error messages
- Good documentation
- Fast feedback
- REPL-friendly

## 🔍 Current Focus

**This Week**: 
1. ✅ Add Guardrails to all deps.clj functions
2. ✅ Create comprehensive test suite for enriched IR
3. Update spec generation to use enriched metadata
4. Fix validation errors in enriched IR integration tests

**Next Week**:
1. Property-based testing for all modules
2. Performance profiling and optimization
3. Documentation updates

**This Month**:
1. 100% test coverage achieved
2. All cross-namespace issues resolved
3. Ready for production use

## 📌 Remember

- **Every function needs Guardrails** - No exceptions
- **Every data structure needs specs** - Full validation
- **Every feature needs tests** - Comprehensive coverage
- **Every decision needs documentation** - Explain the why

The goal is not just working code, but **bulletproof** code that handles any valid protobuf schema correctly and fails gracefully with clear errors for invalid input.

## 🎉 Recent Achievements

### Property-Based Testing for IR Transformations (January 2025)
- ✅ Implemented comprehensive property-based tests using test.check
- ✅ Created custom generators for IR data structures (files, messages, enums)
- ✅ Added property tests for:
  - Dependency graph building and topological sorting
  - Symbol collection from nested types
  - Type enrichment preservation
  - Circular dependency detection
  - Invariant checking (file count, FQN validity, no nil values)
  - IR roundtrip properties
- ✅ Tests cover edge cases like empty files, circular dependencies, and cross-namespace references
- ✅ Performance sanity tests ensure enrichment completes in reasonable time

### Dependency Resolution System (January 2025)
- ✅ Implemented multi-pass IR generation with Stuart Sierra's dependency library
- ✅ Added Specter for powerful nested transformations  
- ✅ Integrated core.match for clean type pattern matching
- ✅ Full Guardrails instrumentation with Malli specs
- ✅ Created comprehensive unit test suite with:
  - Edge case testing
  - Sanity testing
  - Negative testing
  - Performance sanity checks
- ✅ Solved circular dependency detection
- ✅ Handled files with no dependencies (dummy dependency pattern)

### Key Findings
1. **Dummy dependency pattern** - Files with no deps need special handling in Stuart Sierra's library
2. **Specter transformation patterns** - ALL-FIELDS-PATH enables clean nested updates
3. **core.match limitations** - :as bindings don't work as expected, use if-let instead
4. **Malli + Guardrails** - Use `com.fulcrologic.guardrails.malli.core` for better integration

## 🚦 Next Immediate Steps

1. ✅ **Fix integration test failures** - Enriched IR validation errors
2. ✅ **Update spec generation** - Use enriched metadata for cross-namespace refs
3. ✅ **Test with real protos** - cmd/ser cross-namespace usage
4. ✅ **Add property tests** - Random graph/type generation
   - Added type hierarchy property tests
   - Added code generation property tests  
   - Added full EDN->Binary->EDN roundtrip tests
   - Added string conversion roundtrip tests
5. **Performance profiling** - Measure IR enrichment overhead (skipped - not needed with few proto files)
