# !!! IMPORTANT! ALWAYS READ THE WHOLE DOCUMENT !!!!

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

### Proto-Explorer Reference Implementation
- `/home/jare/git/potatoclient/tools/proto-explorer/src/proto_explorer/spec_generator.clj` - Malli spec generation from JSON descriptors
- `/home/jare/git/potatoclient/tools/proto-explorer/src/proto_explorer/buf_validate_extractor.clj` - buf.validate constraint extraction
- `/home/jare/git/potatoclient/tools/proto-explorer/src/proto_explorer/constraints/compiler.clj` - Constraint to Malli schema compilation
- `/home/jare/git/potatoclient/tools/proto-explorer/src/proto_explorer/constraints/metadata_enricher.clj` - Metadata-based constraint attachment
- `/home/jare/git/potatoclient/tools/proto-explorer/test/proto_explorer/constraints/compiler_test.clj` - Constraint compiler tests

### JSON Descriptor Documentation
- `/home/jare/git/potatoclient/examples/protogen/docs/json-descriptor-reference.md` - Complete JSON descriptor format reference

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

## 🎯 Core Principles

### NO BACKWARD COMPATIBILITY
- **Clean slate approach** - We can break anything to make it better
- **No versioning needed** - Single source of truth
- **Delete legacy code** - Don't maintain parallel implementations
- **Radical simplification** - If it's complex, redesign it

### ROCK SOLID FOUNDATION
- **Test-first development** - Write tests before implementation
- **test coverage** - Every function, every path
- **Property-based testing** - Find edge cases automatically
- **Integration tests** - Validate entire pipeline end-to-end

### MAINTAINABLE CODE
- **Strong typing** - Malli specs on everything
- **Single responsibility** - Each function does one thing
- **Clear naming** - Self-documenting code
- **No magic** - Explicit over implicit

## 🏗️ Implementation Order (REVISED FOR CLEAN FOUNDATION)

### Phase -1: clj-kondo Configuration
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
  
### Phase 0: Clean House
**Goal**: Remove all technical debt and establish clean base

1. **Archive (for future references) Legacy Code**
   - [ ] single-namespace mode completely
   - [ ] non-guardrails templates
   - [ ] all commented-out code
   - [ ] unused functions

2. **Establish Testing Framework**
   - [ ] Set up property-based testing infrastructure
   - [ ] Create test data generators for all proto types
   - [ ] Establish test coverage reporting (goal: ~100%)

**Validation**: 
- All tests pass
- Zero legacy code remains
- Test coverage baseline established
- NO SKIPPING TESTS

### Phase 1: Naming & Structure Foundation
**Goal**: Bulletproof naming system that handles all edge cases

1. **Complete Centralized Naming Module**
   - [x] Create lossless proto<->keyword conversions
   - [x] Create lossy conversions for filesystem
   - [ ] Add comprehensive property-based tests
   - [ ] Document all naming rules

2. **Fix All Namespace Issues**
   - [x] Fix duplicate aliases with numeric suffixes
   - [ ] Fix enum qualification issues
   - [ ] Implement proper index file generation
   - [ ] Test cross-file dependencies exhaustively

**Validation Gates**:
- [ ] ~100% naming test coverage
- [ ] Property tests pass with 1000 iterations
- [ ] All proto files generate correctly and compile
- [ ] Zero namespace conflicts in generated code

### Phase 2: Type System & Dependency Graph
**Goal**: Complete understanding of all type relationships

1. **Build Comprehensive Type Registry**
   - [ ] Parse all type information from JSON descriptors
   - [ ] Build bidirectional type reference map
   - [ ] Detect circular dependencies
   - [ ] Create topological sort for generation order

2. **Test Type Resolution**
   - [ ] Unit tests for every type resolution function
   - [ ] Property tests for type reference parsing
   - [ ] Integration tests with complex proto hierarchies
   - [ ] Edge cases: self-references, mutual recursion

**Validation Gates**:
- [ ] Can resolve any type from any context
- [ ] Circular dependency detection works
- [ ] Generation order is always correct
- [ ] ~100% test coverage on type system

### Phase 3: Intermediate Representation (IR)
**Goal**: Rich IR that captures everything needed for generation

1. **Design Comprehensive IR Schema**
   - [ ] Define Malli specs for entire IR
   - [ ] Include validation rules from buf.validate
   - [ ] Include all metadata from JSON descriptors
   - [ ] Design for extensibility

2. **Implement IR Generation**
   - [ ] Parse JSON descriptors into IR
   - [ ] Validate IR against specs
   - [ ] Add derived metadata
   - [ ] Test every field type and option

**Validation Gates**:
- [ ] IR captures 100% of JSON descriptor information
- [ ] IR validates against Malli specs
- [ ] Roundtrip: JSON -> IR -> JSON preserves everything

### Phase 4: Code Generation Pipeline
**Goal**: Clean, predictable code generation

1. **Implement Multi-Stage Pipeline**
   - [ ] Parser Stage: JSON -> Raw data
   - [ ] Analysis Stage: Raw -> Analyzed
   - [ ] Enrichment Stage: Analyzed -> Enriched IR
   - [ ] Generation Stage: IR -> Clojure AST
   - [ ] Emission Stage: AST -> Formatted code

2. **Test Each Stage Independently**
   - [ ] Unit tests for each stage
   - [ ] Property tests for stage transitions
   - [ ] Integration tests for full pipeline
   - [ ] Benchmark each stage

**Validation Gates**:
- [ ] Each stage has ~100% test coverage
- [ ] Stage contracts enforced by Malli
- [ ] Pipeline handles all proto constructs
- [ ] Generated code compiles and passes tests

### Phase 5: Validation & Guardrails Integration
**Goal**: Generated code that validates itself

1. **Extract buf.validate Rules**
   - [ ] Parse all validation annotations
   - [ ] Convert rules to Malli predicates
   - [ ] Generate validation functions
   - [ ] Test validation logic

2. **Integrate with Guardrails**
   - [ ] All functions use >defn
   - [ ] Specs from shared/specs when available
   - [ ] Generated specs for all messages
   - [ ] Runtime validation hooks

**Validation Gates**:
- [ ] All validation rules enforced
- [ ] Guardrails active on all functions
- [ ] Invalid data rejected at runtime

## 📊 Testing Strategy

### Unit Testing
- Every public function has tests
- Every edge case covered
- Use `clojure.test` with good descriptions
- Test pure functions in isolation

### Property-Based Testing
```clojure
(defspec naming-roundtrip-spec
  1000 ; Run MANY iterations
  (prop/for-all [proto-name (gen-valid-proto-name)]
    (= proto-name
       (-> proto-name
           proto->keyword
           keyword->proto))))
```

### Integration Testing
- Full pipeline tests with real proto files
- Compare generated code against golden files
- Test with main app's actual protos
- Verify compilation and runtime behavior

## 🚫 What We're NOT Doing

1. **No Backward Compatibility**
   - Delete old code freely
   - Change APIs without deprecation
   - Rename everything for clarity

2. **No Incremental Migration**
   - Big bang approach
   - Replace entire system at once
   - Test thoroughly before switching

3. **No Legacy Support**
   - Single output format
   - Single namespace structure
   - Single way to do things

4. **No Premature Optimization**
   - Clean code first
   - Measure before optimizing
   - Optimize only bottlenecks

## ✅ Definition of Done

Each phase is complete when:
1. All tests pass (unit, property, integration)
2. ~100% test coverage achieved
3. Code reviewed and refactored
4. Documentation updated
5. Performance benchmarks met
6. No TODO comments remain

## 🎯 Success Metrics

1. **Code Quality**
   - ~100% test coverage
   - Zero linter warnings
   - All functions have guardrails
   - Malli specs on all data

2. **Maintainability**
   - New developer productive in < 1 hour
   - Average function < 20 lines
   - Cyclomatic complexity < 5
   - Clear separation of concerns

3. **Reliability**
   - Zero runtime errors
   - Handles all valid proto files
   - Clear errors for invalid input
   - Deterministic output

## 🔬 Deep Analysis: Proto-Explorer Implementation Insights

### Key Learnings from Proto-Explorer

#### 1. Spec Generation Architecture
Proto-Explorer uses a **multimethod-based approach** for spec generation:
- Dispatches on field type (`:type-string`, `:type-message`, etc.)
- Handles field modifiers (repeated, optional) as schema transformations
- Processes oneofs as special `:oneof` schemas with keyword-keyed alternatives

#### 2. buf.validate Constraint Extraction
The constraint extraction is **sophisticated and complete**:
- Extracts from `options["[buf.validate.field]"]` in JSON descriptors
- Supports all constraint types: numeric, string, collection, message, enum
- Preserves CEL expressions for complex validations
- Groups constraints by type (numeric, string, etc.) for targeted processing

#### 3. Constraint Compilation Strategy
Proto-Explorer's **constraint compiler** is elegant:
- Uses multimethod dispatch on `[constraint-type spec-type]` pairs
- Generates both Malli schemas AND generator hints
- Handles edge cases (e.g., `gt` vs `gte` for floats vs integers)
- Produces clean, composable schema fragments

#### 4. Metadata-Driven Design
The **metadata enricher** pattern is powerful:
- Attaches constraints as metadata to preserve original schema structure
- Allows multiple passes over the schema without loss of information
- Enables clean separation between schema structure and validation rules

### Critical Implementation Details from JSON Descriptors

#### 1. Field Structure in JSON
```json
{
  "name": "temperature",
  "number": 3,
  "label": "LABEL_OPTIONAL",  // or LABEL_REPEATED, LABEL_REQUIRED
  "type": "TYPE_FLOAT",       // TYPE_STRING, TYPE_MESSAGE, etc.
  "typeName": ".package.MessageName",  // For MESSAGE/ENUM types
  "oneofIndex": 0,            // If part of a oneof
  "options": {
    "[buf.validate.field]": {
      "float": {
        "gte": -50.0,
        "lte": 150.0
      }
    }
  }
}
```

#### 2. Oneof Representation
- Fields have `oneofIndex` pointing to their oneof group
- `oneofDecl` array defines oneof names
- Must generate as Malli `:oneof` with proper structure

#### 3. Type Resolution Requirements
- Type names are **fully qualified** with leading dot
- Must resolve across file boundaries
- Need to track file dependencies for proper requires

#### 4. Validation Rule Categories

**Numeric Constraints**:
- `const`, `lt`, `lte`, `gt`, `gte`, `in`, `not_in`
- Different handling for floats (epsilon adjustments) vs integers

**String Constraints**:
- Length: `min_len`, `max_len`, `len`
- Pattern: `pattern`, `prefix`, `suffix`, `contains`
- Format: `email`, `hostname`, `ip`, `ipv4`, `ipv6`, `uri`, `uuid`
- Membership: `in`, `not_in`

**Collection Constraints**:
- `min_items`, `max_items`, `unique`
- Applied to repeated fields

**Message Constraints**:
- `required` - field cannot be null
- `skip` - skip validation

### Improvements We Should Port to proto-clj-generator

#### 1. Constraint-Aware Code Generation
Generate guardrails specs that include buf.validate constraints:
```clojure
(>defn build-coordinate
  [m]
  [[:map 
    [:latitude [:and :double [:>= -90.0] [:<= 90.0]]]
    [:longitude [:and :double [:>= -180.0] [:<= 180.0]]]]
   => #(instance? Coordinate %)]
  ...)
```

#### 2. Enhanced Type Registry
Build a global type registry during parsing:
- Track all messages, enums, and their fully qualified names
- Resolve type dependencies before code generation
- Generate proper namespace requires based on actual usage

#### 3. Validation Function Generation
For each constrained field, generate validation predicates:
```clojure
(defn- valid-latitude? [v]
  (and (double? v) (>= v -90.0) (<= v 90.0)))
```

#### 4. Test Data Generation
Use constraint information for property-based testing:
```clojure
(def latitude-gen
  (gen/double* {:min -90.0 :max 90.0}))
```

## 🚀 Pipeline Architecture Improvements

### Current Pipeline Overview
1. **Backend Stage**: Parse JSON descriptors → EDN representation
2. **Frontend Stage**: EDN → Clojure code generation

### Proposed Enhanced Pipeline

#### 1. Multi-Stage Architecture
```
JSON Descriptors 
  → Parser Stage (Enhanced)
  → Analysis Stage (NEW)
  → Enrichment Stage (NEW)
  → Code Generation Stage
  → Post-Processing Stage (NEW)
```

#### 2. Enhanced Intermediate Representation

**Current IR Limitations**:
- Missing validation rules from buf.validate annotations
- No cross-file dependency resolution metadata
- Limited type relationship information
- No performance hints or optimization metadata

**Proposed IR Enhancements**:
```clojure
{:messages {...}
 :enums {...}
 :metadata
 {:validation-rules   ; Extracted from buf.validate
  {:field-path [:message :field]
   :rules [{:type :range :min -90 :max 90}
           {:type :cel :expression "..."}]}
  
  :type-graph         ; Complete type dependency graph
  {:nodes {"Message1" {:deps #{"Message2" "Enum1"}}
          "Message2" {:deps #{}}}
   :topological-order ["Message2" "Enum1" "Message1"]}
  
  :naming-registry    ; Centralized naming decisions
  {:proto->clojure {"cmd.DayCamera" "cmd.daycamera"}
   :file->namespace {"types.proto" "ser.types"}
   :package->alias {"cmd.DayCamera" "daycamera"}}
  
  :performance-hints  ; For optimized code generation
  {:hot-paths #{[:parse-command :oneofs :cmd]}
   :large-messages #{"JonGuiDataVideoFrame"}
   :frequent-enums #{"JonGuiDataVideoChannel"}}}}
```

#### 3. Global Knowledge Repositories

**Type Registry**:
```clojure
(def global-type-registry
  "Central registry of all proto types across all files"
  (atom {:messages {}
         :enums {}
         :services {}
         :extensions {}}))

(defn register-type! [type-key type-info]
  (swap! global-type-registry assoc-in [:messages type-key] type-info))

(defn resolve-type [type-ref]
  (get-in @global-type-registry [:messages type-ref]))
```

**Naming Registry**:
```clojure
(def global-naming-registry
  "Centralized naming decisions to ensure consistency"
  (atom {:proto->clojure {}
         :namespace-aliases {}
         :reserved-names #{}}))
```

**Validation Registry**:
```clojure
(def global-validation-registry
  "Central registry of validation rules and CEL expressions"
  (atom {:field-rules {}
         :message-rules {}
         :cel-library {}}))
```

#### 4. New Pipeline Stages

**Analysis Stage**:
- Extract all validation rules from buf.validate annotations
- Build complete type dependency graph
- Identify circular dependencies
- Detect naming conflicts
- Analyze field usage patterns

**Enrichment Stage**:
- Add derived metadata (e.g., has-required-fields?)
- Inject performance hints
- Add documentation from source locations
- Resolve all type references
- Add Malli spec associations

**Post-Processing Stage**:
- Validate generated code against specs
- Format code consistently
- Add copyright headers
- Generate index files
- Run clj-kondo checks

### Proto-Explorer Testing Patterns

#### 1. Constraint Compiler Testing
Proto-Explorer tests each constraint type independently:
```clojure
(deftest test-float-constraints
  (testing "Float constraints with gte and lt"
    (let [field {:type :type-float
                 :constraints {:buf.validate {:float {:gte 0 :lt 360}}}}
          result (compiler/compile-field-constraints field)]
      (is (= [:>= 0] (first (:schema result))))
      (is (= [:< 360] (second (:schema result))))
      (is (= 0 (:min (:generator result))))
      (is (= 359.9999 (:max (:generator result)))))))
```

Key insights:
- Tests constraint compilation separately from spec generation
- Verifies both schema output AND generator hints
- Tests edge cases like gt vs gte handling

#### 2. Spec Generation Testing
Tests the complete spec generation pipeline:
- Simple messages with basic fields
- Messages with repeated fields
- Oneof handling
- Nested message references
- Enum processing

#### 3. Integration Testing
Proto-Explorer includes integration tests that:
- Load actual proto JSON descriptors
- Generate complete specs
- Validate generated specs with Malli
- Test roundtrip data generation

### Backend Enhancement: Leveraging JSON Descriptors

Based on analysis of `/home/jare/git/potatoclient/examples/protogen/docs/json-descriptor-reference.md`:

#### 1. Extract Validation Rules
```clojure
(defn extract-validation-rules [field]
  (when-let [validate-field (get-in field [:options "[buf.validate.field]"])]
    {:field (:name field)
     :rules (merge
             (extract-numeric-rules validate-field)
             (extract-string-rules validate-field)
             (extract-collection-rules validate-field)
             (extract-message-rules validate-field))
     :cel-expressions (get-in field [:options "[buf.validate.predefined]" :cel])}))
```

#### 2. Preserve Source Information
```clojure
(defn enrich-with-source-info [descriptor]
  (let [source-info (:sourceCodeInfo descriptor)]
    ;; Map source locations to generated code for better errors
    ...))
```

#### 3. Extract Service Definitions (Future)
```clojure
(defn extract-services [file-descriptor]
  (for [service (:service file-descriptor)]
    {:name (:name service)
     :methods (map extract-method-info (:method service))}))
```

#### 4. Handle Proto Options
```clojure
(defn extract-proto-options [descriptor]
  {:java-package (get-in descriptor [:options :javaPackage])
   :java-outer-classname (get-in descriptor [:options :javaOuterClassname])
   :optimize-for (get-in descriptor [:options :optimizeFor])
   :deprecated (get-in descriptor [:options :deprecated])})
```

### Stronger Malli Specs

#### 1. Generator Function Specs
```clojure
(def generator-fn-spec
  [:=> {:registry registry}
   [:cat
    ::ir/message        ; Input intermediate representation
    ::config/options]   ; Generator options
   ::ast/clojure-code]) ; Output AST

(>defn generate-builder
  [message options]
  [::ir/message ::config/options => ::ast/clojure-code]
  ...)
```

#### 2. Intermediate Representation Specs
```clojure
(def ::field-type
  [:or
   [:map [:scalar ::scalar-type]]
   [:map [:message [:map [:type-ref ::type-ref]]]]
   [:map [:enum [:map [:type-ref ::type-ref]]]]])

(def ::field
  [:map
   [:name ::identifier]
   [:number pos-int?]
   [:type ::field-type]
   [:repeated? boolean?]
   [:optional? boolean?]
   [:validation {:optional true} ::validation-rules]])

(def ::message
  [:map
   [:name ::identifier]
   [:fields [:vector ::field]]
   [:oneofs [:vector ::oneof]]
   [:metadata {:optional true} ::message-metadata]])
```

#### 3. Pipeline Stage Specs
```clojure
(def ::pipeline-stage
  [:=> {:registry registry}
   [:cat ::stage-input]
   [:or 
    [:tuple [:enum :ok] ::stage-output]
    [:tuple [:enum :error] ::error-info]]])
```

### Backend Testing Improvements

#### 1. Property-Based Testing
```clojure
(deftest proto-name-roundtrip-test
  (checking "proto names roundtrip correctly" 100
    [proto-name (gen/such-that valid-proto-name? gen/string-alphanumeric)]
    (is (= proto-name
           (-> proto-name
               proto->clojure-namespace
               clojure-namespace->proto)))))
```

#### 2. Validation Rule Testing
```clojure
(deftest validation-extraction-test
  (testing "Extract all buf.validate rules correctly"
    (let [descriptor (load-test-descriptor "validation-heavy.json")]
      (is (= expected-rules
             (extract-all-validation-rules descriptor))))))
```

#### 3. Dependency Resolution Testing
```clojure
(deftest circular-dependency-test
  (testing "Detect circular dependencies"
    (let [descriptor (load-test-descriptor "circular-deps.json")]
      (is (thrown? CircularDependencyException
                   (build-dependency-graph descriptor))))))
```

### Benefits of Enhanced Pipeline

1. **Better Error Messages**: Source location tracking
2. **Optimized Code**: Performance hints guide generation
3. **Validation Integration**: buf.validate rules in generated code
4. **Maintainability**: Clear separation of concerns
5. **Extensibility**: Easy to add new stages/features
6. **Testing**: Each stage can be tested independently
7. **Documentation**: Metadata flows through pipeline

### Implementation Priority

1. **High Priority**:
   - Extract validation rules from JSON descriptors
   - Build proper type dependency graph
   - Create global registries

2. **Medium Priority**:
   - Add analysis stage
   - Enhance IR with metadata
   - Add pipeline specs

3. **Low Priority**:
   - Performance optimization hints
   - Service definition support
   - Source location tracking

## 🎯 Actionable Improvements for proto-clj-generator

### Phase 1: Port Core Constraint Features
1. **Create constraint extraction namespace**
   - Port `buf_validate_extractor.clj` concepts
   - Extract constraints from JSON descriptor options
   - Build constraint maps for each field

2. **Implement constraint compiler**
   - Port multimethod approach from `constraints/compiler.clj`
   - Generate Malli schema fragments from constraints
   - Include generator hints for property testing

3. **Enhance spec generation in templates**
   - Modify guardrails templates to include constraint specs
   - Generate validation predicates for runtime checks
   - Add constraint documentation to generated functions

### Phase 2: Improve Type System
1. **Build comprehensive type registry**
   - Parse all types from JSON descriptors first
   - Create bidirectional lookup maps
   - Track cross-file dependencies

2. **Fix type resolution**
   - Handle fully qualified names properly
   - Resolve types across file boundaries
   - Generate correct namespace requires

### Phase 3: Enhanced Testing
1. **Add constraint-aware test generation**
   - Generate property-based tests using constraints
   - Create valid test data respecting all rules
   - Test edge cases based on constraint boundaries

2. **Implement roundtrip validation**
   - Proto → Map → Proto with full fidelity
   - Validate constraints at each step
   - Clear error messages for violations

### Key Files to Create/Modify
1. `src/generator/constraints/extractor.clj` - Extract buf.validate rules
2. `src/generator/constraints/compiler.clj` - Compile to Malli schemas  
3. `src/generator/validation.clj` - Runtime validation functions
4. `src/generator/type_registry.clj` - Global type tracking
5. Update all templates to use constraint-aware specs

### Success Metrics
- All buf.validate constraints enforced in generated code
- Property-based tests use constraint information
- Clear validation errors with field context
- Zero false positives in constraint validation
