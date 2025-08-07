# Multi-Pass IR Generation Documentation

## Overview

The proto-clj-generator uses a sophisticated multi-pass Intermediate Representation (IR) generation system to handle complex cross-namespace dependencies and ensure correct code generation. This document details each pass and the transformations applied.

## Why Multi-Pass?

Traditional single-pass code generation fails when:
1. Type A references Type B from a different namespace
2. The generator needs to know if a reference is cross-namespace to generate proper imports
3. Circular dependencies exist between files (though not between types)
4. Validation constraints reference types from other files

Our multi-pass approach solves these issues by building a complete picture before generating any code.

## Pass 1: Basic Parsing

**Module**: `generator.backend`  
**Input**: JSON descriptors from protobuf compiler  
**Output**: Basic IR with raw type references

### What happens:
1. Parse JSON descriptors into EDN structure
2. Convert protobuf types to internal representation
3. Extract basic metadata (packages, imports, options)
4. Preserve type references as strings (e.g., ".com.example.Message")

### Example transformation:
```json
// Input JSON
{
  "messageType": [{
    "name": "Request",
    "field": [{
      "name": "mode",
      "type": "TYPE_ENUM",
      "typeName": ".ser.FxMode"
    }]
  }]
}
```

```clojure
;; Output Basic IR
{:type :message
 :name :request
 :proto-name "Request"
 :fields [{:name :mode
           :type {:enum {:type-ref ".ser.FxMode"}}}]}
```

### Validation:
- Input validated against JSON schema
- Output validated against `specs/DescriptorSet`

## Pass 2: Dependency Analysis

**Module**: `generator.deps/build-dependency-graph`  
**Input**: Basic IR  
**Output**: Dependency graph and topological sort

### What happens:
1. Extract file dependencies from each proto file
2. Build directed acyclic graph (DAG) of dependencies
3. Perform topological sort to determine processing order
4. Detect circular dependencies (throws exception)

### Example:
```clojure
;; Input files
[{:name "types.proto" :package "ser" :dependencies []}
 {:name "commands.proto" :package "cmd" :dependencies ["types.proto"]}]

;; Output dependency graph
{:nodes #{"ser" "cmd"}
 :edges {"cmd" #{"ser"}}
 :sorted-files ["types.proto" "commands.proto"]}
```

### Validation:
- Graph structure validated
- Circular dependencies cause immediate failure

## Pass 3: Symbol Collection

**Module**: `generator.deps/build-symbol-registry`  
**Input**: Basic IR + sorted file order  
**Output**: Global symbol registry

### What happens:
1. Process files in dependency order
2. For each file, collect all type definitions (messages, enums)
3. Build Fully Qualified Names (FQNs) for each type
4. Handle nested types recursively
5. Create global registry mapping FQN → type definition

### Example:
```clojure
;; Symbol registry
{".ser.FxMode" {:fqn ".ser.FxMode"
                :type :enum
                :definition {:name :fx-mode
                             :package "ser"
                             :values [...]}}
 ".cmd.Request" {:fqn ".cmd.Request"
                 :type :message
                 :definition {:name :request
                              :package "cmd"
                              :fields [...]}}}
```

### Special handling:
- Nested types get compound FQNs (e.g., ".pkg.Outer.Inner")
- Package names preserved for cross-namespace detection

## Pass 4: Type Resolution & Enrichment

**Module**: `generator.deps/enrich-*` functions  
**Input**: Basic IR + Symbol Registry  
**Output**: Enriched IR with resolved references

### What happens:
1. For each type reference in the IR:
   - Look up in symbol registry
   - Determine if cross-namespace
   - Add resolution metadata
2. Enrich fields with target package info
3. Mark cross-namespace references
4. Validate all references resolve

### Example enrichment:
```clojure
;; Before enrichment
{:type {:enum {:type-ref ".ser.FxMode"}}}

;; After enrichment  
{:type {:enum {:type-ref ".ser.FxMode"
               :resolved {:fqn ".ser.FxMode"
                          :type :enum
                          :definition {...}}
               :cross-namespace true
               :target-package "ser"}}}
```

### Validation:
- All type references must resolve
- Output validated against `specs/EnrichedDescriptorSet`

## Pass 5: Code Generation

**Module**: `generator.frontend` / `generator.frontend-namespaced`  
**Input**: Enriched IR  
**Output**: Clojure source code

### What happens:
1. Process files in dependency order
2. For each namespace:
   - Generate requires based on cross-namespace refs
   - Generate proper aliases
   - Handle local vs external type references differently
3. Generate specs using enriched type info
4. Generate builder/parser functions

### Cross-namespace handling:
```clojure
;; Local reference (same namespace)
[:mode {:optional true} :fx-mode-enum]

;; Cross-namespace reference  
[:mode {:optional true} ser/fx-mode-enum-spec]
;; Where 'ser' is the required namespace alias
```

## Data Structure Evolution

### Stage 1: Raw JSON
```json
{
  "file": [{
    "name": "example.proto",
    "messageType": [...]
  }]
}
```

### Stage 2: Basic IR
```clojure
{:type :descriptor-set
 :files [{:type :file
          :name "example.proto"
          :messages [...]}]}
```

### Stage 3: With Dependencies
```clojure
{:type :descriptor-set
 :files [...]
 :dependency-graph {:nodes #{...} :edges {...}}
 :sorted-files ["base.proto" "derived.proto"]}
```

### Stage 4: With Symbol Registry
```clojure
{:type :combined
 :files [...]
 :dependency-graph {...}
 :symbol-registry {".pkg.Type" {:fqn "..." :definition ...}}}
```

### Stage 5: Fully Enriched
```clojure
{:type :combined
 :files [{:messages [{:fields [{:type {:enum {:cross-namespace true
                                               :target-package "other.pkg"
                                               :resolved {...}}}}]}]}]
 :dependency-graph {...}
 :symbol-registry {...}}
```

## Validation Points

Each transformation is validated:

1. **Input JSON** → validated against protobuf JSON schema
2. **Basic IR** → validated against `specs/DescriptorSet`  
3. **Dependency Graph** → validated against `specs/DependencyGraph`
4. **Symbol Registry** → validated against `specs/SymbolRegistry`
5. **Enriched IR** → validated against `specs/EnrichedDescriptorSet`
6. **Generated Code** → validated by Clojure compiler & guardrails

## Benefits of Multi-Pass Approach

### 1. Complete Information
- All type information available during code generation
- Can make intelligent decisions about imports and aliases

### 2. Early Error Detection  
- Unresolved types caught during enrichment
- Circular dependencies detected immediately

### 3. Optimization Opportunities
- Process files in optimal order
- Generate minimal imports
- Reuse common type references

### 4. Maintainability
- Each pass has single responsibility
- Easy to debug specific transformations
- Clear data flow

### 5. Extensibility
- Easy to add new passes
- Can insert validation/transformation steps
- Support for new features without major refactoring

## Future Enhancements

### Incremental Generation
- Cache symbol registry between runs
- Only reprocess changed files
- Speed up large codebases

### Parallel Processing
- Process independent files concurrently
- Maintain dependency ordering
- Utilize multi-core systems

### Enhanced Validation
- Custom validation rules
- Cross-file constraint validation
- Better error messages with source locations

## Debugging Tips

### Enable debug output:
```clojure
(generate-all {:debug? true ...})
```

### Inspect intermediate files:
```bash
cat output/debug/dependency-graph.edn
cat output/debug/symbol-registry.edn  
```

### Validate specific stages:
```clojure
(require '[generator.specs :as specs])
(specs/validate! specs/EnrichedDescriptorSet my-data "context")
```

## Conclusion

The multi-pass IR generation system provides a robust foundation for handling complex protobuf schemas with cross-namespace dependencies. By building a complete picture before generating code, we ensure correctness, provide better error messages, and enable sophisticated code generation strategies.