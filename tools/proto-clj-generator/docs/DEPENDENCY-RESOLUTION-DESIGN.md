# Dependency Resolution Design for Proto-Clj-Generator

## Overview

This document describes the multi-pass dependency resolution system for the proto-clj-generator. The system ensures that cross-namespace references (particularly enums from the `ser` namespace used in command namespaces) are properly resolved during code generation.

## Problem Statement

### The Core Issue
When generating Clojure code from protobuf descriptors, we encounter cross-namespace references that cannot be resolved at namespace load time:

1. **Namespace A** (e.g., `cmd.daycamera`) references an enum from **Namespace B** (e.g., `ser`)
2. The generated code uses qualified keywords like `:ser/jon-gui-data-fx-mode-day`
3. At namespace load time, these keywords cannot be resolved because:
   - Each namespace sets its own Malli registry
   - The registries aren't available at compile time
   - Guardrails validation fails with `:malli.core/invalid-schema` errors

### Example Failure Case
```clojure
;; In cmd.daycamera namespace
(def some-spec
  [:map
   [:fx-mode {:optional true} :ser/jon-gui-data-fx-mode-day]]) ; This fails!
```

## Solution Architecture

### Multi-Pass IR Generation

The solution involves enriching the Intermediate Representation (IR) through multiple passes:

```
JSON Descriptors → Pass 1 → Basic IR → Pass 2 → Enriched IR → Pass 3 → Final IR
                    ↓                     ↓                       ↓
                Parse Files          Build Deps              Resolve Refs
```

### Pass 1: Basic Parsing
- Parse JSON descriptors into basic EDN structure
- Extract file metadata (name, package, dependencies)
- Convert protobuf types to internal representation
- **Output**: Basic IR with raw type references

### Pass 2: Dependency Analysis
- Build dependency graph from file imports
- Perform topological sort for correct processing order
- Create global symbol registry
- **Output**: IR enriched with dependency information

### Pass 3: Reference Resolution
- Resolve all type references using symbol registry
- Identify cross-namespace references
- Add metadata for proper code generation
- **Output**: Fully enriched IR ready for code generation

## Detailed Design

### 1. Dependency Graph Structure

```clojure
{:dependency-graph
 {"jon_shared_cmd_day_camera.proto" 
  {:package "cmd.DayCamera"
   :depends-on ["jon_shared_data_types.proto"]}
  
  "jon_shared_data_types.proto"
  {:package "ser"
   :depends-on []}}
   
 :sorted-files ["jon_shared_data_types.proto" 
                "jon_shared_cmd_day_camera.proto"]}
```

### 2. Symbol Registry

The symbol registry maps Fully Qualified Names (FQNs) to their definitions:

```clojure
{".ser.JonGuiDataFxModeDay"
 {:fqn ".ser.JonGuiDataFxModeDay"
  :type :enum
  :definition {:name :jon-gui-data-fx-mode-day
               :proto-name "JonGuiDataFxModeDay"
               :package "ser"
               :values [...]}}
               
 ".cmd.DayCamera.SetFxMode"
 {:fqn ".cmd.DayCamera.SetFxMode"
  :type :message
  :definition {:name :set-fx-mode
               :proto-name "SetFxMode"
               :package "cmd.DayCamera"
               :fields [...]}}}
```

### 3. Enriched Type References

Type references in the IR are enriched with resolution information:

```clojure
;; Before enrichment
{:type {:enum {:type-ref ".ser.JonGuiDataFxModeDay"}}}

;; After enrichment
{:type {:enum {:type-ref ".ser.JonGuiDataFxModeDay"
               :resolved {:fqn ".ser.JonGuiDataFxModeDay"
                          :type :enum
                          :definition {...}}
               :cross-namespace true
               :target-package "ser"}}}
```

### 4. Code Generation Strategy

Based on the enriched IR, the code generator can make informed decisions:

#### For Cross-Namespace References
```clojure
;; Instead of generating:
[:fx-mode {:optional true} :ser/jon-gui-data-fx-mode-day]

;; Generate:
[:fx-mode {:optional true} ser/jon-gui-data-fx-mode-day-spec]
;; Where 'ser' is a namespace alias
```

#### For Same-Namespace References
```clojure
;; Continue using keywords for local references
[:status {:optional true} :status-enum]
```

## Implementation Details

### Module: `generator.deps`

This module provides the core dependency resolution functionality:

```clojure
(ns generator.deps
  "Dependency resolution for protobuf files.")

;; Main API function
(defn enrich-descriptor-set
  "Enrich a descriptor set with dependency information."
  [descriptor-set]
  ...)
```

### Key Functions

1. **`build-dependency-graph`**
   - Extracts dependency information from each file
   - Filters out system dependencies (google.protobuf, buf.validate)
   - Returns a map of filename → dependencies

2. **`topological-sort`**
   - Implements Kahn's algorithm for DAG sorting
   - Detects circular dependencies
   - Returns files in correct processing order

3. **`build-symbol-registry`**
   - Processes files in dependency order
   - Collects all symbols (enums, messages) with FQNs
   - Handles nested types recursively

4. **`enrich-field`**
   - Enriches field type references with resolution data
   - Identifies cross-namespace references
   - Adds metadata for code generation

### Integration with Backend

The backend module should be updated to use the enriched IR:

```clojure
(defn parse-all-descriptors
  [descriptor-dir]
  (let [cmd-edn (parse-descriptor-set cmd-file)
        state-edn (parse-descriptor-set state-file)
        ;; Combine descriptors
        combined {:files (concat (:files cmd-edn) (:files state-edn))}
        ;; Enrich with dependencies
        enriched (deps/enrich-descriptor-set combined)]
    enriched))
```

## Benefits of This Approach

### 1. **Complete Information at Generation Time**
- All type references are pre-resolved
- No runtime lookups needed
- Generator has full context for each reference

### 2. **Explicit Dependency Handling**
- Clear dependency graph
- Files processed in correct order
- Circular dependencies detected early

### 3. **Flexible Code Generation**
- Can choose different strategies for cross-namespace vs local references
- Can generate proper namespace aliases
- Can optimize registry setup

### 4. **Better Error Messages**
- Unresolved references caught during IR enrichment
- Clear context about where references come from
- Easier debugging of cross-namespace issues

## Migration Path

### Phase 1: Add Dependency Resolution (Current)
1. Implement `generator.deps` module ✓
2. Create enriched IR structure ✓
3. Test dependency graph building ✓

### Phase 2: Update Backend
1. Integrate dependency resolution into backend
2. Update IR structure to include enrichment
3. Ensure backward compatibility

### Phase 3: Update Code Generation
1. Modify spec generation to use enriched IR
2. Generate proper cross-namespace references
3. Update registry handling

### Phase 4: Testing & Validation
1. Test with complex cross-namespace scenarios
2. Verify guardrails compatibility
3. Performance testing with large descriptor sets

## Alternative Approaches Considered

### 1. **Runtime Registry Merging**
- **Approach**: Merge all registries at runtime
- **Rejected because**: Requires runtime coordination, breaks modularity

### 2. **Global Registry Module**
- **Approach**: Generate a single module with all specs
- **Rejected because**: Loses namespace organization, creates mega-module

### 3. **Lazy Resolution**
- **Approach**: Resolve references lazily at first use
- **Rejected because**: Adds runtime overhead, complex implementation

### 4. **UUID Placeholders**
- **Approach**: Use UUIDs during generation, replace later
- **Rejected because**: Extra complexity, two-phase generation

## Future Enhancements

### 1. **Incremental Processing**
- Only reprocess files that changed
- Cache symbol registry between runs
- Speed up large codebases

### 2. **Parallel Processing**
- Process independent files in parallel
- Maintain dependency ordering
- Utilize multi-core systems

### 3. **Enhanced Validation**
- Validate all references are resolvable
- Check for naming conflicts
- Warn about unused imports

### 4. **IDE Integration**
- Export dependency graph for tooling
- Provide symbol navigation data
- Enable cross-namespace refactoring

## Conclusion

The multi-pass dependency resolution system provides a robust solution to cross-namespace reference issues. By enriching the IR with complete dependency and resolution information, we enable the code generator to make informed decisions about how to handle each reference, resulting in correct and efficient generated code.