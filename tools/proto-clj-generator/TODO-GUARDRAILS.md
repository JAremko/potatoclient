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
- [x] Fix duplicate aliases (two `camera` aliases)
  - ~~Currently both `cmd.daycamera` and `ser.camera` use alias `camera`~~
  - ~~Need to disambiguate by using full package name or numbered suffixes~~
  - Fixed by adding numeric suffixes in dependency_graph.clj when conflicts detected
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

3. **Enum Reference Qualification** (NEW ISSUE - 2025-08-06 Evening)
   - Enum references in generated code are not being properly qualified with namespace aliases
   - Generated: `(get jon-gui-data-gps-fix-type-values ...)`
   - Expected: `(get ser/jon-gui-data-gps-fix-type-values ...)`
   - The issue is that while the dependency analysis correctly identifies cross-file dependencies and the namespace generation includes the proper requires with aliases, the enum reference resolution is not using the qualified form
   - Fixed `resolve-enum-reference-with-aliases` and `resolve-enum-keyword-map-with-aliases` to properly handle ns-alias-map
   - Updated `generate-field-setter` to use the alias-aware versions
   - Still need to investigate why the qualification is not being applied in the generated code

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
   - [x] Fix duplicate aliases
   - [ ] Update all generators to use naming module
   - [ ] Fix index file generation

2. **Phase 2: Testing**
   - [ ] Update test fixtures for new structure
   - [ ] Add generative tests for naming
   - [ ] Implement comprehensive roundtrip tests
   - [ ] Add buf.validate integration

3. **Phase 3: Guardrails**
   - [x] Update templates to use >defn
   - [ ] Integrate Malli specs from shared directory
   - [ ] Configure clj-kondo
   - [ ] Add guardrails-check tool

4. **Phase 4: Pipeline Architecture Improvements** (NEW)
   - [ ] Enrich intermediate representation
   - [ ] Split pipeline into more stages
   - [ ] Add global knowledge repositories
   - [ ] Strengthen Malli specs throughout

5. **Phase 5: Polish**
   - [ ] Documentation
   - [ ] Performance optimization
   - [ ] Error message improvements
   - [ ] Final validation

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