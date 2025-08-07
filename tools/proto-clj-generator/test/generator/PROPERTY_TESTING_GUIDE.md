# Property Testing Guide for IR Transformations

## Summary

We successfully implemented property-based testing for the IR transformation pipeline using Malli specs with custom generators. This ensures our tests use valid data that matches production constraints.

## Key Files Created

1. **`spec_generators.clj`** - Custom generators for all IR specs
   - Generates valid package names, proto filenames, type references
   - Ensures consistent data (e.g., all definitions in a file share the same package)
   - Filters out self-dependencies automatically

2. **`malli_real_spec_property_test.clj`** - Property tests using real production specs
   - Tests enrichment preserves structure
   - Validates symbol registry correctness
   - Checks dependency ordering
   - Verifies field type enrichment

3. **`property_test_summary.clj`** - Comprehensive example and edge case tests
   - Shows the recommended pattern for property testing
   - Includes edge case tests (empty, single file, circular deps)
   - Tests generator quality

## Key Learnings

### 1. Use Custom Generators for Complex Specs

The IR structure has many constraints that Malli's automatic generators struggle with:
- `TypeReference` must start with a dot (e.g., ".com.example.Message")
- All definitions in a file must have the same package
- Field numbers must be unique within a message
- No self-dependencies in files

Custom generators ensure these constraints are met.

### 2. Attach Generators to Real Specs

Instead of creating simplified test-only specs, we use the actual production specs with custom generators. This ensures our tests validate against the same specs used in production.

### 3. Handle Expected Failures Gracefully

Circular dependencies are a valid reason for enrichment to fail. Property tests should catch these exceptions and consider them passing cases:

```clojure
(catch Exception e
  (boolean (re-find #"[Cc]ircular" (.getMessage e))))
```

### 4. Test Invariants, Not Implementation

Focus on properties that should always hold:
- File count is preserved
- All symbols are collected in the registry
- Dependency ordering is respected
- Output validates against the `EnrichedDescriptorSet` spec

### 5. Fix the Source of Failures

We fixed a bug where `collect-file-symbols` could be called with nil when a file was missing from the file-map. Added a nil check:

```clojure
(if-let [file (get file-map filename)]
  (let [symbols (collect-file-symbols file)]
    ...)
  registry)
```

## Running the Tests

```bash
# Run all property tests
clojure -X:test :patterns '[".*property.*"]'

# Run specific test files
clojure -X:test :patterns '["malli_real_spec_property_test"]'
clojure -X:test :patterns '["property_test_summary"]'
```

## Next Steps

1. Consider adding more edge cases to the generators (nested messages, oneofs, etc.)
2. Add generators for the enriched specs if needed for downstream testing
3. Consider property tests for other parts of the system (code generation, etc.)

## Example Property Test Pattern

```clojure
(defspec my-property-test
  50  ; number of test cases
  (prop/for-all [input my-custom-generator]
    (try
      (let [result (function-under-test input)]
        (and 
         ;; Test invariants
         (invariant-1? result)
         (invariant-2? result)
         ;; Validate against spec
         (m/validate ::my-spec result {:registry my-registry})))
      ;; Handle expected failures
      (catch Exception e
        (valid-failure? e)))))
```