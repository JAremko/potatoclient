# Using Enriched IR for Spec Generation

## Overview

The enriched IR (Intermediate Representation) provides additional metadata that improves cross-namespace reference handling in spec generation. This document explains how to use the enriched IR effectively.

## Key Benefits

1. **Automatic Cross-Namespace Detection**: The enriched IR includes `:cross-namespace` flags on all type references
2. **Target Package Information**: Each cross-namespace reference includes `:target-package` for precise alias resolution
3. **Resolved Type Metadata**: Full type definitions are available in the `:resolved` field

## Data Flow

```
JSON Descriptors
      ↓
  Basic IR
      ↓
Enriched IR (via deps/enrich-descriptor-set)
      ↓
Spec Generation (uses enriched metadata)
```

## Enriched Type Reference Structure

### Enum References
```clojure
{:type {:enum {:type-ref ".other.pkg.Status"
               :cross-namespace true        ; Added by enrichment
               :target-package "other.pkg"  ; Added by enrichment
               :resolved {...}}}}           ; Added by enrichment
```

### Message References
```clojure
{:type {:message {:type-ref ".other.pkg.Request"
                  :cross-namespace true
                  :target-package "other.pkg"
                  :resolved {...}}}}
```

## Using Enriched IR in Frontend

When generating specs with enriched IR:

1. **Pass Package Mappings**: Include `:package-mappings` in the namespace data
```clojure
{:messages enriched-messages
 :enums enriched-enums
 :current-package "current.pkg"
 :package-mappings {"other.pkg" "other-alias"
                    "third.pkg" "third-alias"}}
```

2. **Spec Generation Behavior**:
   - Cross-namespace enums → Generate aliased spec references (e.g., `other-alias/status-spec`)
   - Cross-namespace messages → Use `:any` (specs not generated for external messages)
   - Same-namespace types → Use keyword references as before

## Example Usage

```clojure
(require '[generator.deps :as deps]
         '[generator.spec-gen :as spec-gen])

;; 1. Load and enrich the descriptor set
(let [descriptor-set (load-descriptors "path/to/descriptors")
      enriched (deps/enrich-descriptor-set descriptor-set)]
  
  ;; 2. Process each file with enriched data
  (doseq [file (:files enriched)]
    (let [package (:package file)
          ;; Build package mappings from dependency info
          package-mappings (build-package-mappings enriched package)
          
          ;; Generate specs with enriched data
          specs (spec-gen/generate-specs-for-namespace
                 {:messages (:messages file)
                  :enums (:enums file)
                  :current-package package
                  :package-mappings package-mappings})]
      ;; Use generated specs
      (println (:enum-specs specs))
      (println (:message-specs specs)))))
```

## Backward Compatibility

The spec generation code maintains backward compatibility:
- If `:package-mappings` is not provided, falls back to namespace suffix matching
- If enriched metadata is not present, uses the original type reference logic
- Existing code generation pipelines continue to work without modification

## Best Practices

1. **Always Enrich Before Spec Generation**: Run the descriptor set through `deps/enrich-descriptor-set` before generating specs
2. **Provide Package Mappings**: Build and pass `:package-mappings` for accurate cross-namespace handling
3. **Handle Missing Aliases**: The code gracefully falls back to keywords if aliases are not found
4. **Test Cross-Namespace Scenarios**: Ensure your tests cover cross-namespace enum and message references

## Debugging Tips

1. **Check Enrichment**: Verify that type references have `:cross-namespace` and `:target-package` fields
2. **Verify Package Mappings**: Ensure the package mappings match the actual package names in the enriched IR
3. **Inspect Generated Specs**: Look for aliased references (symbols) vs keyword references

## Future Improvements

- Support for generating specs for cross-namespace messages (currently returns `:any`)
- Automatic package mapping generation from enriched dependency graph
- Better error messages when aliases are missing