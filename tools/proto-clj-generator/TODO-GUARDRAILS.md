
# !!! IMPORTANT! ALWAYS READ THE WHOLE DOCUMENT !!!!

## 🔍 Key Files to Research

### Main App Files
- `/home/jare/git/potatoclient/.clj-kondo/` - linting config
- `/home/jare/git/potatoclient/Makefile` - build/lint setup
- Examples of `>defn` usage in main app code
- `/home/jare/git/potatoclient/shared/specs/` - Malli specs

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

### Phase -1. clj-kondo Configuration
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

## 🚀 Pipeline Architecture Improvements (NEW SECTION)

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
