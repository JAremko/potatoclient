# Proto-CLJ Generator

A custom code generator that creates Clojure functions for bidirectional protobuf conversion with runtime validation using Guardrails and Malli specs.

## Overview

This tool generates Clojure code that can:
- Convert Clojure maps to protobuf messages (builder functions)
- Convert protobuf messages to Clojure maps (parser functions)
- Handle all protobuf types including oneofs, enums, and nested messages
- Generate runtime validation using Guardrails (`>defn`/`>defn-`)
- Generate Malli specs alongside protobuf bindings
- Support namespace separation (each protobuf package gets its own namespace)
- Generate enum↔keyword conversion maps
- **Handle cross-namespace type references with dependency resolution**
- **Validate data at each stage of IR transformation**

## Key Features

### Multi-Pass IR Generation
The generator uses a sophisticated multi-pass approach to handle complex dependencies:

1. **Parse Phase**: Convert JSON descriptors to basic IR
2. **Dependency Analysis**: Build dependency graph and topologically sort files  
3. **Symbol Collection**: Build global registry of all types (enums, messages)
4. **Type Resolution**: Enrich IR with resolved type references
5. **Code Generation**: Generate code with full cross-namespace awareness

### Robust Validation
Every data structure is validated using Malli specs:
- Input JSON descriptors
- Intermediate representations at each transformation
- Final generated code structure
- Runtime validation in generated code

## Usage

### Using Makefile (Recommended)

```bash
# Generate all protobuf bindings with guardrails
make generate

# Run tests
make test

# Check guardrails coverage
make guardrails-check

# Run REPL for development
make nrepl
```

### Using deps.edn alias

```bash
# From the proto-clj-generator directory
clojure -X:gen

# With custom paths
clojure -X:gen :input-dir '"../custom/descriptors"' :output-dir '"./out"' :namespace-prefix '"my.proto"'

# Without guardrails (for production builds)
clojure -X:gen :guardrails? false
```

### Command line

```bash
clojure -M -m generator.main <input-dir> <output-dir> <namespace-prefix> [guardrails?]

# Example with guardrails
clojure -M -m generator.main ../../tools/proto-explorer/output/json-descriptors /home/jare/git/potatoclient/shared potatoclient.proto true

# Example without guardrails
clojure -M -m generator.main ../../tools/proto-explorer/output/json-descriptors generated potatoclient.proto false
```

### From REPL

```clojure
(require '[generator.main :as main])

;; With guardrails (default)
(main/generate
  {:input-dir "../../tools/proto-explorer/output/json-descriptors"
   :output-dir "/home/jare/git/potatoclient/shared"
   :namespace-prefix "potatoclient.proto"
   :guardrails? true})
```

## Generated Code Structure

The generator uses namespace separation - each protobuf package generates to its own namespace:

### Root Namespaces
- `<namespace-prefix>.cmd` - Root command namespace with re-exports
- `<namespace-prefix>.ser` - Root state/serialization namespace with re-exports

### Package Namespaces
Each protobuf package generates to:
- `<namespace-prefix>.<package>.<subpackage>` - Contains all types from that package

Example structure:
```
potatoclient/proto/
├── cmd.clj                    # Root command namespace
├── cmd/
│   ├── daycamera.clj         # cmd.DayCamera messages
│   ├── heatcamera.clj        # cmd.HeatCamera messages
│   ├── rotaryplatform.clj    # cmd.RotaryPlatform messages
│   └── ...
└── ser.clj                    # Root state namespace
```

## Generated Code Features

### 1. Guardrails Runtime Validation

All generated functions use `>defn` with runtime specs:

```clojure
(>defn build-root
  "Build a Root protobuf message from a map."
  [m]
  [root-spec => any?]  ; Runtime validation
  (let [builder (cmd.JonSharedCmd$Root/newBuilder)]
    ;; ... builder code
    (.build builder)))
```

### 2. Malli Specs

Specs are generated in the same file as bindings:

```clojure
(def root-spec
  "Malli spec for root message"
  [:map
   [:protocol-version [:maybe :int]]
   [:session-id [:maybe :int]]
   [:payload [:altn {:day-camera [:map [:day-camera :cmd.day-camera/root]]
                     :heat-camera [:map [:heat-camera :cmd.heat-camera/root]]
                     ;; ... other variants
                     }]]])
```

### 3. Enum Conversion Maps

Generated in the same file as enum usage:

```clojure
;; Enum to keyword map
(def jon-gui-data-client-type-keywords
  {ser.JonGUIDataClientType/UNKNOWN :unknown
   ser.JonGUIDataClientType/SMARTPHONE :smartphone
   ser.JonGUIDataClientType/SMARTWATCH :smartwatch})

;; Keyword to enum map
(def jon-gui-data-client-type-values
  {:unknown ser.JonGUIDataClientType/UNKNOWN
   :smartphone ser.JonGUIDataClientType/SMARTPHONE
   :smartwatch ser.JonGUIDataClientType/SMARTWATCH})
```

### 4. Cross-Namespace References

Oneof fields correctly reference builders in other namespaces:

```clojure
(>defn- build-root-payload
  "Build the oneof payload for Root."
  [builder [field-key value]]
  [any? [:tuple keyword? any?] => any?]
  (case field-key
    :day-camera (.setDayCamera builder (daycamera/build-root value))
    :heat-camera (.setHeatCamera builder (heatcamera/build-root value))
    ;; ... other cases
    ))
```

## Example Usage

```clojure
(require '[potatoclient.proto.cmd :as cmd])

;; Build a command with validation
(def command-bytes
  (-> {:protocol-version 1
       :session-id 42
       :payload {:rotary {:goto-ndc {:x 0.5 :y -0.5 :channel :heat}}}}
      cmd/build-root  ; Runtime validation happens here
      .toByteArray))

;; Parse a command
(def parsed-command
  (-> command-bytes
      (cmd.JonSharedCmd$Root/parseFrom)
      cmd/parse-root))  ; Returns validated Clojure map
```

## Architecture

### Module Structure

```
src/generator/
├── backend.clj           # JSON descriptor parsing & IR generation
├── deps.clj              # Dependency resolution & IR enrichment (Specter + core.match)
├── specs.clj             # Malli specs for all data structures
├── frontend.clj          # Code generation for single-file mode
├── frontend_namespaced.clj # Code generation for namespace-separated mode
├── spec_gen.clj          # Malli spec generation from IR
├── type_resolution.clj   # Type reference resolution
└── validation_helpers.clj # Constraint compilation helpers
```

### Data Flow

```
JSON Descriptors
    ↓ (backend/parse-descriptor-set)
Basic IR
    ↓ (deps/enrich-descriptor-set) 
Enriched IR with:
  - Dependency graph
  - Sorted file order
  - Symbol registry
  - Resolved type refs
    ↓ (frontend/generate-*)
Generated Clojure Code
```

### Key Data Structures

All data structures are defined and validated in `generator.specs`:

- **DescriptorSet**: Raw parsed protobuf descriptors
- **EnrichedDescriptorSet**: Descriptors with dependency info and resolved types
- **DependencyGraph**: DAG of file dependencies  
- **SymbolRegistry**: Global FQN → type definition mapping
- **EnrichedField**: Field with resolved cross-namespace references

## Crucial Findings and Implementation Details

### 1. Dependency Graph Building with Stuart Sierra's Library

We use `com.stuartsierra/dependency` for robust graph algorithms. A crucial finding was handling files with no dependencies:

```clojure
;; Files with no dependencies aren't automatically added as nodes
;; Solution: Add dummy dependency then remove it
(if (empty? (:depends-on node))
  (-> g
      (dep/depend filename ::dummy)
      (dep/remove-edge filename ::dummy)
      (dep/remove-all ::dummy))
  ;; Normal dependency addition
  (reduce #(dep/depend %1 filename %2) g (:depends-on node)))
```

### 2. Specter for Nested Transformations

Specter dramatically simplifies nested data transformations. Key patterns:

```clojure
;; Define reusable paths
(def ALL-FIELDS-PATH
  [:messages sp/ALL :fields sp/ALL])

;; Transform all fields in one pass
(sp/transform ALL-FIELDS-PATH
              (fn [field]
                (update field :type enrich-fn))
              file)
```

### 3. Core.match for Type Pattern Matching

Instead of complex nested conditionals, we use core.match for clear type handling:

```clojure
(match field-type
  ;; Scalar type - return as-is
  {:scalar _} field-type
  
  ;; Message type with type-ref
  {:message msg-type} 
  (if-let [type-ref (:type-ref msg-type)]
    {:message (merge msg-type (enrich-type-reference type-ref ...))}
    field-type)
  
  ;; Enum type with type-ref
  {:enum enum-type}
  (if-let [type-ref (:type-ref enum-type)]
    {:enum (merge enum-type (enrich-type-reference type-ref ...))}
    field-type)
  
  ;; Any other structure
  :else field-type)
```

### 4. Malli-based Guardrails

We use `com.fulcrologic.guardrails.malli.core` for better schema integration:

```clojure
(require '[com.fulcrologic.guardrails.malli.core :refer [>defn =>]])

(>defn enrich-field
  [field symbol-registry current-package]
  [[:map [:name keyword?] [:type map?]]
   [:map-of string? any?]
   string? 
   => 
   map?]
  ;; Implementation
  )
```

### 5. Granular Testing Strategy

Comprehensive unit tests make debugging much easier:

```clojure
;; Test individual functions
(deftest test-extract-file-dependencies ...)
(deftest test-build-dependency-graph ...)
(deftest test-topological-sort ...)

;; Test edge cases
(deftest test-circular-dependency-detection ...)
(deftest test-empty-and-nil-handling ...)

;; Performance sanity checks
(deftest test-performance-sanity ...)
```

## Guardrails Integration

The generated code integrates with the project's guardrails setup:

1. **Runtime Validation**: All generated functions validate inputs/outputs
2. **clj-kondo Support**: `.clj-kondo/config.edn` is configured for guardrails
3. **Coverage Checking**: Use `make guardrails-check` to verify all functions are spec'd
4. **Development vs Production**: Can disable guardrails for production builds

### Checking Guardrails Coverage

```bash
# Check which functions lack specs
make guardrails-check

# Or directly
clojure -X:guardrails-check :namespaces '[potatoclient.proto.cmd]'
```

## Custom Malli Specs

You can add custom specs that compose with generated specs:

```clojure
;; In shared/specs/custom/my_specs.clj
(ns potatoclient.specs.custom.my-specs
  (:require [potatoclient.proto.cmd :as cmd]))

;; Extend generated spec with additional constraints
(def strict-command-spec
  [:and
   cmd/root-spec
   [:fn {:error/message "Protocol version must be > 0"}
    #(pos? (:protocol-version %))]])
```

## Dependencies

- JSON descriptors from proto-explorer (must run proto-explorer first)
- Custom oneof schema from shared/specs
- Protobuf Java classes (must compile protos first)
- Guardrails for runtime validation
- Malli for spec definitions

## Development

### Run tests:
```bash
# All tests
make test

# Specific test namespace
clojure -X:test :nses '[generator.simple-roundtrip-test]'
```

### Check generated code:
```bash
# Generate code
make generate

# Inspect generated files
cat /home/jare/git/potatoclient/shared/potatoclient/proto/cmd.clj
cat /home/jare/git/potatoclient/shared/potatoclient/proto/cmd/rotaryplatform.clj
```

### Debug generation:
```bash
# Check intermediate EDN representation
cat /home/jare/git/potatoclient/shared/debug/command-edn.edn
cat /home/jare/git/potatoclient/shared/debug/type-lookup.edn
```

## Performance Considerations

1. **Guardrails Overhead**: Runtime validation adds overhead. For production:
   ```bash
   # Generate without guardrails
   clojure -X:gen :guardrails? false
   ```

2. **Namespace Loading**: With namespace separation, only load what you need:
   ```clojure
   ;; Load only specific command namespace
   (require '[potatoclient.proto.cmd.rotaryplatform :as rotary])
   ```

## Integration with WebSocket Refactoring

This tool is part of the WebSocket refactoring effort to replace Kotlin subprocesses with pure Clojure implementations. The generated code provides:

1. **Type Safety**: Runtime validation ensures messages conform to protobuf schema
2. **Better Errors**: Malli specs provide detailed validation errors
3. **REPL-Friendly**: All generated code is pure Clojure, easy to test at REPL
4. **Performance**: No reflection, direct Java interop with type hints

## Documentation

### Core Documentation
- [TODO.md](TODO.md) - Current tasks and roadmap
- [DEPENDENCY-RESOLUTION-DESIGN.md](DEPENDENCY-RESOLUTION-DESIGN.md) - Multi-pass IR system design
- [Multi-Pass IR Generation](docs/MULTI-PASS-IR-GENERATION.md) - Detailed IR transformation pipeline

### Module Documentation
- `generator.specs` - Malli specifications for all data structures
- `generator.deps` - Dependency resolution and IR enrichment
- `generator.constraints.*` - buf.validate constraint extraction and compilation

## Testing

### Run All Tests
```bash
make test  # Generates code and runs full test suite
```

### Run Specific Tests
```bash
# Dependency resolution tests
clojure -X:test :nses '[generator.deps-test generator.deps-integration-test]'

# Constraint tests  
clojure -X:test :nses '[generator.constraints-test generator.constraint-integration-test]'

# Roundtrip tests
clojure -X:test :nses '[generator.comprehensive-roundtrip-test]'
```

### Test Coverage
The generator includes comprehensive testing:
- Unit tests for all modules
- Integration tests for the full pipeline
- Property-based tests using Malli generators
- Roundtrip validation tests (Proto → Map → Proto)
- Constraint boundary testing

## Current Status (January 2025)

### ✅ Completed Features
- **Multi-pass IR generation** with dependency resolution
- **Cross-namespace type references** with proper imports
- **buf.validate constraint extraction** and Malli compilation
- **Guardrails integration** for runtime validation
- **Custom :oneof schema** support
- **Comprehensive Malli specs** for all data structures
- **Property-based testing** infrastructure

### ✅ Recently Completed (January 2025)
- **Property-based testing infrastructure** for IR transformations
- Random generators for protobuf IR structures
- Comprehensive invariant and roundtrip property tests
- Edge case coverage for circular dependencies and empty files

### 🚧 In Progress
- Fine-tuning property tests for 100% pass rate
- Performance optimizations (parallel processing, caching)

### 📋 Planned
- Enhanced error messages with source locations
- IDE integration support
- Incremental compilation support

## Contributing

### Code Quality Standards
- All functions must use `>defn` or `>defn-` (guardrails)
- All data structures must have Malli specs
- 100% test coverage for new code
- Clean separation of concerns

### Testing Requirements
- Unit tests for all public functions
- Integration tests for cross-module interactions
- Property-based tests for data transformations
- Roundtrip validation for generated code

## Troubleshooting

### "Cannot find proto class" errors
Ensure protobuf Java classes are compiled:
```bash
cd ../.. && make proto
```

### Validation failures
Check the Malli spec error - it shows exactly which field failed:
```clojure
;; Example error
{:type :malli.core/invalid-input
 :data {:protocol-version -1}  ; negative not allowed
 :schema [:map [:protocol-version [:maybe :int]]]}